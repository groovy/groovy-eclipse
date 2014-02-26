/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index;

import java.io.*;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;

/**
 * An <code>Index</code> maps document names to their referenced words in various categories.
 * <p>
 * Queries can search a single category or several at the same time.
 * </p>
 * Indexes are not synchronized structures and should only be queried/updated one at a time.
 */
public class Index {

public String containerPath;
public ReadWriteMonitor monitor;

// Separator to use after the container path
static final char DEFAULT_SEPARATOR = '/';
public char separator = DEFAULT_SEPARATOR;
static final char JAR_SEPARATOR = IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR.charAt(0);

protected DiskIndex diskIndex;
protected MemoryIndex memoryIndex;

/**
 * Mask used on match rule for indexing.
 */
static final int MATCH_RULE_INDEX_MASK =
	SearchPattern.R_EXACT_MATCH |
	SearchPattern.R_PREFIX_MATCH |
	SearchPattern.R_PATTERN_MATCH |
	SearchPattern.R_REGEXP_MATCH |
	SearchPattern.R_CASE_SENSITIVE |
	SearchPattern.R_CAMELCASE_MATCH |
	SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;

public static boolean isMatch(char[] pattern, char[] word, int matchRule) {
	if (pattern == null) return true;
	int patternLength = pattern.length;
	int wordLength = word.length;
	if (patternLength == 0) return matchRule != SearchPattern.R_EXACT_MATCH;
	if (wordLength == 0) return (matchRule & SearchPattern.R_PATTERN_MATCH) != 0 && patternLength == 1 && pattern[0] == '*';

	// need to mask some bits of pattern rule (bug 79790)
	switch(matchRule & MATCH_RULE_INDEX_MASK) {
		case SearchPattern.R_EXACT_MATCH :
			return patternLength == wordLength && CharOperation.equals(pattern, word, false);
		case SearchPattern.R_PREFIX_MATCH :
			return patternLength <= wordLength && CharOperation.prefixEquals(pattern, word, false);
		case SearchPattern.R_PATTERN_MATCH :
			return CharOperation.match(pattern, word, false);
		case SearchPattern.R_CAMELCASE_MATCH:
		// same part count is not activated because index key may have uppercase letters after the type name
		case SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH:
			if (CharOperation.camelCaseMatch(pattern, word, false)) {
				return true;
			}
			return patternLength <= wordLength && CharOperation.prefixEquals(pattern, word, false);
		case SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE :
			return pattern[0] == word[0] && patternLength == wordLength && CharOperation.equals(pattern, word);
		case SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE :
			return pattern[0] == word[0] && patternLength <= wordLength && CharOperation.prefixEquals(pattern, word);
		case SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE :
			return CharOperation.match(pattern, word, true);
		case SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE :
		// same part count is not activated because index key may have uppercase letters after the type name
		case SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE :
			return (pattern[0] == word[0] && CharOperation.camelCaseMatch(pattern, word, false));
	}
	return false;
}


public Index(IndexLocation location, String containerPath, boolean reuseExistingFile) throws IOException {
	this.containerPath = containerPath;
	this.monitor = new ReadWriteMonitor();

	this.memoryIndex = new MemoryIndex();
	this.diskIndex = new DiskIndex(location);
	this.diskIndex.initialize(reuseExistingFile);
	if (reuseExistingFile) this.separator = this.diskIndex.separator;
}
public void addIndexEntry(char[] category, char[] key, String containerRelativePath) {
	this.memoryIndex.addIndexEntry(category, key, containerRelativePath);
}
public String containerRelativePath(String documentPath) {
	int index = documentPath.indexOf(IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR);
	if (index == -1) {
		index = this.containerPath.length();
		if (documentPath.length() <= index)
			throw new IllegalArgumentException("Document path " + documentPath + " must be relative to " + this.containerPath); //$NON-NLS-1$ //$NON-NLS-2$
	}
	return documentPath.substring(index + 1);
}
public File getIndexFile() {
	return this.diskIndex == null ? null : this.diskIndex.indexLocation.getIndexFile();
}
public IndexLocation getIndexLocation() {
	return this.diskIndex == null ? null : this.diskIndex.indexLocation;
}
public long getIndexLastModified() {
	return this.diskIndex == null? -1 : this.diskIndex.indexLocation.lastModified();
}
public boolean hasChanged() {
	return this.memoryIndex.hasChanged();
}
/**
 * Returns the entries containing the given key in a group of categories, or null if no matches are found.
 * The matchRule dictates whether its an exact, prefix or pattern match, as well as case sensitive or insensitive.
 * If the key is null then all entries in specified categories are returned.
 */
public EntryResult[] query(char[][] categories, char[] key, int matchRule) throws IOException {
	if (this.memoryIndex.shouldMerge() && this.monitor.exitReadEnterWrite()) {
		try {
			save();
		} finally {
			this.monitor.exitWriteEnterRead();
		}
	}

	HashtableOfObject results;
	int rule = matchRule & MATCH_RULE_INDEX_MASK;
	if (this.memoryIndex.hasChanged()) {
		results = this.diskIndex.addQueryResults(categories, key, rule, this.memoryIndex);
		results = this.memoryIndex.addQueryResults(categories, key, rule, results);
	} else {
		results = this.diskIndex.addQueryResults(categories, key, rule, null);
	}
	if (results == null) return null;

	EntryResult[] entryResults = new EntryResult[results.elementSize];
	int count = 0;
	Object[] values = results.valueTable;
	for (int i = 0, l = values.length; i < l; i++) {
		EntryResult result = (EntryResult) values[i];
		if (result != null)
			entryResults[count++] = result;
	}
	return entryResults;
}
/**
 * Returns the document names that contain the given substring, if null then returns all of them.
 */
public String[] queryDocumentNames(String substring) throws IOException {
	SimpleSet results;
	if (this.memoryIndex.hasChanged()) {
		results = this.diskIndex.addDocumentNames(substring, this.memoryIndex);
		this.memoryIndex.addDocumentNames(substring, results);
	} else {
		results = this.diskIndex.addDocumentNames(substring, null);
	}
	if (results.elementSize == 0) return null;

	String[] documentNames = new String[results.elementSize];
	int count = 0;
	Object[] paths = results.values;
	for (int i = 0, l = paths.length; i < l; i++)
		if (paths[i] != null)
			documentNames[count++] = (String) paths[i];
	return documentNames;
}
public void remove(String containerRelativePath) {
	this.memoryIndex.remove(containerRelativePath);
}
/**
 * Reset memory and disk indexes.
 * 
 * @throws IOException
 */
public void reset() throws IOException {
	this.memoryIndex = new MemoryIndex();
	this.diskIndex = new DiskIndex(this.diskIndex.indexLocation);
	this.diskIndex.initialize(false/*do not reuse the index file*/);
}
public void save() throws IOException {
	// must own the write lock of the monitor
	if (!hasChanged()) return;

	int numberOfChanges = this.memoryIndex.docsToReferences.elementSize;
	this.diskIndex.separator = this.separator;
	this.diskIndex = this.diskIndex.mergeWith(this.memoryIndex);
	this.memoryIndex = new MemoryIndex();
	if (numberOfChanges > 1000)
		System.gc(); // reclaim space if the MemoryIndex was very BIG
}
public void startQuery() {
	if (this.diskIndex != null)
		this.diskIndex.startQuery();
}
public void stopQuery() {
	if (this.diskIndex != null)
		this.diskIndex.stopQuery();
}
public String toString() {
	return "Index for " + this.containerPath; //$NON-NLS-1$
}
public boolean isIndexForJar()
{
	return this.separator == JAR_SEPARATOR;
}
}
