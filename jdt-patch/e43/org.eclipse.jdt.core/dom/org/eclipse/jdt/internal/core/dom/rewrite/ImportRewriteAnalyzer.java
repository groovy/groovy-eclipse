/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contribution for Bug 378024 - Ordering of comments between imports not preserved
 *******************************************************************************/
package org.eclipse.jdt.internal.core.dom.rewrite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

public final class ImportRewriteAnalyzer {

	private final ICompilationUnit compilationUnit;
	private final ArrayList packageEntries;

	private final List importsCreated;
	private final List staticImportsCreated;

	private final IRegion replaceRange;

	private final int importOnDemandThreshold;
	private final int staticImportOnDemandThreshold;

	private boolean filterImplicitImports;
	private boolean useContextToFilterImplicitImports;
	private boolean findAmbiguousImports;

	private IRegion[] preserveExistingCommentsRanges;

	private int flags= 0;

	private static final int F_NEEDS_LEADING_DELIM= 2;
	private static final int F_NEEDS_TRAILING_DELIM= 4;

	private static final String JAVA_LANG= "java.lang"; //$NON-NLS-1$

	public ImportRewriteAnalyzer(
			ICompilationUnit cu,
			CompilationUnit root,
			String[] importOrder,
			int threshold,
			int staticThreshold,
			boolean restoreExistingImports,
			boolean useContextToFilterImplicitImports) {
		this.compilationUnit= cu;
		this.importOnDemandThreshold= threshold;
		this.staticImportOnDemandThreshold= staticThreshold;
		this.useContextToFilterImplicitImports = useContextToFilterImplicitImports;

		this.filterImplicitImports= true;
		this.findAmbiguousImports= true; //!restoreExistingImports;

		this.packageEntries= new ArrayList(20);
		this.importsCreated= new ArrayList();
		this.staticImportsCreated= new ArrayList();
		this.flags= 0;

		this.replaceRange= evaluateReplaceRange(root);
		if (restoreExistingImports) {
			addExistingImports(root);
		} else {
			// collect all existing comments inside imports and concatenate them
			this.preserveExistingCommentsRanges = retrieveExistingCommentsInImports(root);
		}

		PackageEntry[] order= new PackageEntry[importOrder.length];
		for (int i= 0; i < order.length; i++) {
			String curr= importOrder[i];
			if (curr.length() > 0 && curr.charAt(0) == '#') {
				curr= curr.substring(1);
				order[i]= new PackageEntry(curr, curr, true); // static import group
			} else {
				order[i]= new PackageEntry(curr, curr, false); // normal import group
			}
		}

		addPreferenceOrderHolders(order);
	}

	private int getSpacesBetweenImportGroups() {
		try {
			int num= Integer.parseInt(this.compilationUnit.getJavaProject().getOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, true));
			if (num >= 0)
				return num;
		} catch (NumberFormatException e) {
			// fall through
		}
		return 1;
	}
	
	private boolean insertSpaceBeforeSemicolon() {
		return JavaCore.INSERT.equals(this.compilationUnit.getJavaProject().getOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, true));
	}

	private void addPreferenceOrderHolders(PackageEntry[] preferenceOrder) {
		if (this.packageEntries.isEmpty()) {
			// all new: copy the elements
			for (int i= 0; i < preferenceOrder.length; i++) {
				this.packageEntries.add(preferenceOrder[i]);
			}
		} else {
			// match the preference order entries to existing imports
			// entries not found are appended after the last successfully matched entry

			PackageEntry[] lastAssigned= new PackageEntry[preferenceOrder.length];

			// find an existing package entry that matches most
			for (int k= 0; k < this.packageEntries.size(); k++) {
				PackageEntry entry= (PackageEntry) this.packageEntries.get(k);
				if (!entry.isComment()) {
					String currName= entry.getName();
					int currNameLen= currName.length();
					int bestGroupIndex= -1;
					int bestGroupLen= -1;
					for (int i= 0; i < preferenceOrder.length; i++) {
						boolean currPrevStatic= preferenceOrder[i].isStatic();
						if (currPrevStatic == entry.isStatic()) {
							String currPrefEntry= preferenceOrder[i].getName();
							int currPrefLen= currPrefEntry.length();
							if (currName.startsWith(currPrefEntry) && currPrefLen >= bestGroupLen) {
								if (currPrefLen == currNameLen || currName.charAt(currPrefLen) == '.') {
									if (bestGroupIndex == -1 || currPrefLen > bestGroupLen) {
										bestGroupLen= currPrefLen;
										bestGroupIndex= i;
									}
								}
							}
						}
					}
					if (bestGroupIndex != -1) {
						entry.setGroupID(preferenceOrder[bestGroupIndex].getName());
						lastAssigned[bestGroupIndex]= entry; // remember last entry
					}
				}
			}
			// fill in not-assigned categories, keep partial order
			int currAppendIndex= 0;
			for (int i= 0; i < lastAssigned.length; i++) {
				PackageEntry entry= lastAssigned[i];
				if (entry == null) {
					PackageEntry newEntry= preferenceOrder[i];
					if (currAppendIndex == 0 && !newEntry.isStatic()) {
						currAppendIndex= getIndexAfterStatics();
					}
					this.packageEntries.add(currAppendIndex, newEntry);
					currAppendIndex++;
				} else {
					currAppendIndex= this.packageEntries.indexOf(entry) + 1;
				}
			}
		}
	}

	private String getQualifier(ImportDeclaration decl) {
		String name = decl.getName().getFullyQualifiedName();
		/*
		 * If it's on demand import, return the fully qualified name. (e.g. pack1.Foo.* => pack.Foo, pack1.* => pack1)
		 * This is because we need to have pack1.Foo.* and pack1.Bar under different qualifier groups.
		 */
		if (decl.isOnDemand()) {
			return name;
		}
		return getQualifier(name, decl.isStatic());
	}

	private String getQualifier(String name, boolean isStatic) {
		// For static imports, return the Type name as well as part of the qualifier
		if (isStatic || !this.useContextToFilterImplicitImports) {
			return Signature.getQualifier(name);
		}

		char[] searchedName = name.toCharArray();
		int index = name.length();
		/* Stop at the last fragment */
		JavaProject project = (JavaProject) this.compilationUnit.getJavaProject();
		do {
			String testedName = new String(searchedName, 0, index);
			IJavaElement fragment = null;
			try {
				fragment = project.findPackageFragment(testedName);
			} catch (JavaModelException e) {
				return name;
			}
			if (fragment != null) {
				return testedName;
			}
			try {
				fragment = project.findType(testedName);
			} catch (JavaModelException e) {
				return name;
			}
			if (fragment != null) {
				index = CharOperation.lastIndexOf(Signature.C_DOT, searchedName, 0, index - 1);
			} else {
				// we use the heuristic that a name starting with a lowercase is a package name
				index = CharOperation.lastIndexOf(Signature.C_DOT, searchedName, 0, index - 1);
				if (Character.isLowerCase(searchedName[index + 1])) {
					return testedName;
				}
			}
		} while (index >= 0); 
		return name;
	}

	private static String getFullName(ImportDeclaration decl) {
		String name= decl.getName().getFullyQualifiedName();
		return decl.isOnDemand() ? name + ".*": name; //$NON-NLS-1$
	}

	private void addExistingImports(CompilationUnit root) {
		List/*ImportDeclaration*/ decls= root.imports();
		if (decls.isEmpty()) {
			return;
		}
		PackageEntry currPackage= null;

		ImportDeclaration curr= (ImportDeclaration) decls.get(0);
		int currOffset= curr.getStartPosition();
		int currLength= curr.getLength();
		int currEndLine= root.getLineNumber(currOffset + currLength);

		for (int i= 1; i < decls.size(); i++) {
			boolean isStatic= curr.isStatic();
			String name= getFullName(curr);
			String packName= getQualifier(curr);
			if (currPackage == null || currPackage.compareTo(packName, isStatic) != 0) {
				currPackage= new PackageEntry(packName, null, isStatic);
				this.packageEntries.add(currPackage);
			}

			ImportDeclaration next= (ImportDeclaration) decls.get(i);
			int nextOffset= next.getStartPosition();
			int nextLength= next.getLength();
			int nextOffsetLine= root.getLineNumber(nextOffset);

			int extendedStart = root.getExtendedStartPosition(curr);
			int extendedLength = root.getExtendedLength(curr);
			if (extendedStart < this.replaceRange.getOffset()) {
				// don't touch the first comments before the start of import declarations
				extendedLength -=  (currOffset - extendedStart);
				extendedStart = currOffset;
			}
			
			// if next import is on a different line, modify the end position to the next line begin offset
			int nextLineOffset = nextOffset; // offset at the start of next line. Next import may not start here
			if (currEndLine < nextOffsetLine) {
				currEndLine++;
				nextLineOffset = root.getPosition(currEndLine, 0); 
				// There may be some leading comments (or line delimiters) before the next import. The start of those comments
				// is not the real start offset of the next import. So don't change nextOffset
			}
			// retrieve preceding and trailing comments if any
			IRegion rangeBefore = null;
			IRegion rangeAfter = null;
			
			if (currOffset > extendedStart) {
				rangeBefore = new Region(extendedStart, currOffset - extendedStart);
			}
			int currLen = curr.getLength();
			if (currLen < extendedLength - (currOffset - extendedStart)) {
				int currEndOffset = currOffset + currLen;
				int rangeBeforeLen = rangeBefore != null? rangeBefore.getLength() : 0;
				rangeAfter = new Region(currEndOffset, extendedLength - rangeBeforeLen - currLen);
			}
			currPackage.add(
					new ImportDeclEntry(
							packName.length(), 
							name, 
							isStatic, 
							new Region(currOffset, nextLineOffset - currOffset), // should not include leading comments of next import, line delimiters, etc.
							rangeBefore,
							rangeAfter));
			currOffset= nextOffset;
			curr= next;

			// add a comment entry for spacing between imports
			if (currEndLine < nextOffsetLine) {
				nextOffset= root.getPosition(nextOffsetLine, 0);

				int length = nextOffset - nextLineOffset;
				if (length > 2) { // valid comment has at least two chars
					currPackage.add(new ImportDeclEntry(packName.length(), null, false, new Region(nextLineOffset, length)));
				}

				currOffset= nextOffset;
			}
			currEndLine= root.getLineNumber(nextOffset + nextLength);
		}

		boolean isStatic= curr.isStatic();
		String name= getFullName(curr);
		String packName= getQualifier(curr);
		if (currPackage == null || currPackage.compareTo(packName, isStatic) != 0) {
			currPackage= new PackageEntry(packName, null, isStatic);
			this.packageEntries.add(currPackage);
		}
		int currStartOffset = curr.getStartPosition();
		int currLen = curr.getLength();
		int extendedStartOffset = root.getExtendedStartPosition(curr);
		IRegion leadingComments = null;
		IRegion allTrailingComments = null;
		
		if (currStartOffset > extendedStartOffset) {
			leadingComments = new Region(extendedStartOffset, currOffset - extendedStartOffset);
		}
		int length= this.replaceRange.getOffset() + this.replaceRange.getLength() - currStartOffset;
		int extendedLength = root.getExtendedLength(curr);
		if (currLen < extendedLength - (currOffset - extendedStartOffset)) {
			int currEndOffset = currOffset + currLen;
			int leadingCommentsLen = leadingComments != null? leadingComments.getLength() : 0;
			allTrailingComments = new Region(currEndOffset, extendedLength - leadingCommentsLen - currLen);
		}
		currPackage.add(new ImportDeclEntry(packName.length(), name, isStatic, new Region(curr.getStartPosition(), length), leadingComments, allTrailingComments));
	}

	private IRegion[] retrieveExistingCommentsInImports(CompilationUnit root) {
		List/*ImportDeclaration*/ decls= root.imports();
		if (decls.isEmpty()) {
			return null;
		}
		
		List commentList = root.getCommentList();
		int numberOfComments = commentList.size();
		List regions = null;
		int currentExtendedEnd = -1;
		int currEndLine= -1;

		/* for the first comment, we only take the trailing comment if any and the replace range doesn't 
		 * include the preceding comment
		 */
		for (int i= 0; i < decls.size(); i++) {
			ImportDeclaration next= (ImportDeclaration) decls.get(i);
			int nextOffset= next.getStartPosition();
			int nextLength= next.getLength();

			int extendedStart = root.getExtendedStartPosition(next);
			int extendedLength = root.getExtendedLength(next);
			int nextOffsetLine= root.getLineNumber(nextOffset);

			if (nextOffset != extendedStart) {
				// preceding comment
				int lengthOfPrecedingComment = nextOffset - extendedStart;
				if (i != 0) {
					if (regions == null) {
						regions = new ArrayList();
					}
					regions.add(new Region(extendedStart, lengthOfPrecedingComment));
				}
				
				if (extendedLength != (nextLength + lengthOfPrecedingComment)) {
					// Preceding and trailing comments 
					int regionLength = extendedLength - (nextLength + lengthOfPrecedingComment);
					if (regions == null) {
						regions = new ArrayList();
					}
					regions.add(new Region(nextOffset + nextLength, regionLength));
				}
			} else if (nextLength != extendedLength) {
				// no extended start - only trailing comment
				int regionLength = extendedLength - nextLength;
				if (regions == null) {
					regions = new ArrayList();
				}
				regions.add(new Region(nextOffset + nextLength, regionLength));
			}
			if (i > 0) {
				// record comments between the previous comment and the current one that are not part
				// of any comment extended range.
				if ((nextOffsetLine - currEndLine) > 1) {
					// check for comments between the two imports
					LineComment comment = root.getAST().newLineComment();
					comment.setSourceRange(currentExtendedEnd + 1, 0);
					int index = Collections.binarySearch(commentList, comment, new Comparator() {
						public int compare(Object o1, Object o2) {
							return ((Comment) o1).getStartPosition() - ((Comment) o2).getStartPosition();
						}
					});
					// index = -(insertion point) - 1. 
					if (index < 0) {
						loop: for (int j = -(index + 1); j < numberOfComments; j++) {
							Comment currentComment = (Comment) commentList.get(j);
							int commentStartPosition = currentComment.getStartPosition();
							int commentLength = currentComment.getLength();
							if ((commentStartPosition > currentExtendedEnd)
									&& ((commentStartPosition + commentLength - 1) < extendedStart)) {
								if (regions == null) {
									regions = new ArrayList();
								}
								regions.add(new Region(commentStartPosition, commentLength));
							} else {
								break loop;
							}
						}
					}
				}
			}
			currentExtendedEnd = extendedStart + extendedLength - 1;
			currEndLine = root.getLineNumber(currentExtendedEnd);
		}
		if (regions == null) {
			return null;
		}
		// sort regions according to their positions to restore comments in the same order
		IRegion[] result = (IRegion[]) regions.toArray(new IRegion[regions.size()]);
		Arrays.sort(result, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((IRegion) o1).getOffset() - ((IRegion) o2).getOffset();
			}
		});
		return result;
	}
	/**
	 * Specifies that implicit imports (for types in <code>java.lang</code>, types in the same package as the rewrite
	 * compilation unit and types in the compilation unit's main type) should not be created, except if necessary to
	 * resolve an on-demand import conflict.
	 * <p>
	 * The filter is enabled by default.
	 * </p>
	 * <p>
	 * Note: {@link #ImportRewriteAnalyzer(ICompilationUnit, CompilationUnit, String[], int, int, boolean, boolean)} with true as the last
	 * parameter can be used to filter implicit imports when a context is used.
	 * </p>
	 * 
	 * @param filterImplicitImports
	 *            if <code>true</code>, implicit imports will be filtered
	 * 
	 * @see #ImportRewriteAnalyzer(ICompilationUnit, CompilationUnit, String[], int, int, boolean, boolean)
	 */
	public void setFilterImplicitImports(boolean filterImplicitImports) {
		this.filterImplicitImports= filterImplicitImports;
	}
	/**
	 * When set searches for imports that can not be folded into on-demand
	 * imports but must be specified explicitly
	 * @param findAmbiguousImports The new value
	 */
	public void setFindAmbiguousImports(boolean findAmbiguousImports) {
		this.findAmbiguousImports= findAmbiguousImports;
	}

	private static class PackageMatcher {
		private String newName;
		private String bestName;
		private int bestMatchLen;

		public PackageMatcher() {
			// initialization in 'initialize'
		}

		public void initialize(String newImportName, String bestImportName) {
			this.newName= newImportName;
			this.bestName= bestImportName;
			this.bestMatchLen= getCommonPrefixLength(bestImportName, newImportName);
		}

		public boolean isBetterMatch(String currName, boolean preferCurr) {
			boolean isBetter;
			int currMatchLen= getCommonPrefixLength(currName, this.newName);
			int matchDiff= currMatchLen - this.bestMatchLen;
			if (matchDiff == 0) {
				if (currMatchLen == this.newName.length() && currMatchLen == currName.length() && currMatchLen == this.bestName.length()) {
					// duplicate entry and complete match
					isBetter= preferCurr;
				} else {
					isBetter= sameMatchLenTest(currName);
				}
			} else {
				isBetter= (matchDiff > 0); // curr has longer match
			}
			if (isBetter) {
				this.bestName= currName;
				this.bestMatchLen= currMatchLen;
			}
			return isBetter;
		}

		private boolean sameMatchLenTest(String currName) {
			int matchLen= this.bestMatchLen;
			// known: bestName and currName differ from newName at position 'matchLen'
			// currName and bestName don't have to differ at position 'matchLen'

			// determine the order and return true if currName is closer to newName
			char newChar= getCharAt(this.newName, matchLen);
			char currChar= getCharAt(currName, matchLen);
			char bestChar= getCharAt(this.bestName, matchLen);

			if (newChar < currChar) {
				if (bestChar < newChar) {								// b < n < c
					return (currChar - newChar) < (newChar - bestChar);	// -> (c - n) < (n - b)
				} else {												// n < b  && n < c
					if (currChar == bestChar) { // longer match between curr and best
						return false; // keep curr and best together, new should be before both
					} else {
						return currChar < bestChar; // -> (c < b)
					}
				}
			} else {
				if (bestChar > newChar) {								// c < n < b
					return (newChar - currChar) < (bestChar - newChar);	// -> (n - c) < (b - n)
				} else {												// n > b  && n > c
					if (currChar == bestChar) {  // longer match between curr and best
						return true; // keep curr and best together, new should be ahead of both
					} else {
						return currChar > bestChar; // -> (c > b)
					}
				}
			}
		}
	}

	/* package */ static int getCommonPrefixLength(String s, String t) {
		int len= Math.min(s.length(), t.length());
		for (int i= 0; i < len; i++) {
			if (s.charAt(i) != t.charAt(i)) {
				return i;
			}
		}
		return len;
	}

	/* package */ static char getCharAt(String str, int index) {
		if (str.length() > index) {
			return str.charAt(index);
		}
		return 0;
	}

	private PackageEntry findBestMatch(String newName, boolean isStatic) {
		if (this.packageEntries.isEmpty()) {
			return null;
		}
		String groupId= null;
		int longestPrefix= -1;
		PackageEntry matchingCommentEntry = null;
		// find the matching group
		for (int i= 0; i < this.packageEntries.size(); i++) {
			PackageEntry curr= (PackageEntry) this.packageEntries.get(i);
			if (isStatic == curr.isStatic()) {
				String currGroup= curr.getGroupID();
				if (currGroup != null && newName.startsWith(currGroup)) {
					int prefixLen= currGroup.length();
					if (prefixLen == newName.length() && !curr.isComment()) {
						return curr; // perfect fit, use entry
					} else if (curr.isComment()) {
						matchingCommentEntry = curr; // may be the only fit if no actual import of this group is already present
						continue;
					}
					if ((newName.charAt(prefixLen) == '.' || prefixLen == 0) && prefixLen > longestPrefix) {
						longestPrefix= prefixLen;
						groupId= currGroup;
					}
				}
			}
		}
		if (matchingCommentEntry != null) {
			return matchingCommentEntry;
		}
		PackageEntry bestMatch= null;
		PackageMatcher matcher= new PackageMatcher();
		matcher.initialize(newName, ""); //$NON-NLS-1$
		for (int i= 0; i < this.packageEntries.size(); i++) { // find the best match with the same group
			PackageEntry curr= (PackageEntry) this.packageEntries.get(i);
			if (!curr.isComment() && curr.isStatic() == isStatic) {
				if (groupId == null || groupId.equals(curr.getGroupID())) {
					boolean preferrCurr= (bestMatch == null) || (curr.getNumberOfImports() > bestMatch.getNumberOfImports());
					if (matcher.isBetterMatch(curr.getName(), preferrCurr)) {
						bestMatch= curr;
					}
				}
			}
		}
		return bestMatch;
	}

	private boolean isImplicitImport(String qualifier) {
		if (JAVA_LANG.equals(qualifier)) {
			return true;
		}
		ICompilationUnit cu= this.compilationUnit;
		String packageName= cu.getParent().getElementName();
		if (qualifier.equals(packageName)) {
			return true;
		}
		String mainTypeName= JavaCore.removeJavaLikeExtension(cu.getElementName());
		if (packageName.length() == 0) {
			return qualifier.equals(mainTypeName);
		}
		return qualifier.equals(packageName +'.' + mainTypeName);
	}

	public void addImport(String fullTypeName, boolean isStatic, CompilationUnit root, boolean restoreExistingImports) {
		String typeContainerName= getQualifier(fullTypeName, isStatic);
		ImportDeclEntry decl;
		if (restoreExistingImports) {
			decl = new ImportDeclEntry(typeContainerName.length(), fullTypeName, isStatic, null);
		} else {
			decl = addImportDeclEntry(typeContainerName, fullTypeName, isStatic, root);
		}
		sortIn(typeContainerName, decl, isStatic);
	}

	/**
	 * adds the import entry, but if its an existing import entry then preserves the comments surrounding the import
	 */
	private ImportDeclEntry addImportDeclEntry(String containerName, String fullTypeName, boolean isStatic, CompilationUnit root) {
		List/*ImportDeclaration*/ decls= root.imports();
		if (decls.isEmpty() || this.preserveExistingCommentsRanges == null || this.preserveExistingCommentsRanges.length == 0) {
			return new ImportDeclEntry(containerName.length(), fullTypeName, isStatic, null);
		}
		IRegion precedingCommentRange = null;
		IRegion trailingCommentRange = null;
		int prevOffset = this.replaceRange.getOffset();  // will store offset of the previous import's extended end
		int numOfImports = decls.size();
		for (int i= 0; i < numOfImports; i++) {
			ImportDeclaration curr= (ImportDeclaration) decls.get(i);
			int currOffset= curr.getStartPosition();
			int currLength= curr.getLength();
			int currExtendedStart = root.getExtendedStartPosition(curr);
			int currExtendedLen = root.getExtendedLength(curr);
			String name= getFullName(curr);
			String packName= getQualifier(curr);
			if (packName.equals(containerName) && (name.equals(fullTypeName) || name.endsWith("*"))) {//$NON-NLS-1$
				int preserveCommentsLen = this.preserveExistingCommentsRanges.length;
				for (int j = 0; j < preserveCommentsLen; j++) {
					int offset = this.preserveExistingCommentsRanges[j].getOffset();
					boolean wasRangeUsed = false;
					int existingCommentLength = this.preserveExistingCommentsRanges[j].getLength();
					if (offset == currExtendedStart) {
						// comments belonging to this import's extended start
						precedingCommentRange = new Region(offset, existingCommentLength);
						wasRangeUsed = true;
					} else if (offset < currExtendedStart && offset > prevOffset) {
						// comment between two imports but not inside either's extended ranges
						// to preserve the position of these comments add a dummy comment entry
						PackageEntry commentEntry = new PackageEntry(); // create a comment package entry for this
						commentEntry.setGroupID(packName);	// the comment should belong to the current group
						this.packageEntries.add(commentEntry);
						commentEntry.add(new ImportDeclEntry(packName.length(), null, false, new Region(offset, existingCommentLength)));
						wasRangeUsed = true;
					} else if ((currExtendedStart + currExtendedLen) != (currOffset + currLength)){
						if (offset == currOffset + currLength) {
							// comment is in the extended end of the import
							trailingCommentRange = new Region(offset, existingCommentLength);
							wasRangeUsed = true;
						} else if (offset > (currOffset + currLength)) {
							break;
						}
					}
					if (wasRangeUsed) {
						// remove this comment from preserveExistingCommentsRanges array
						IRegion[] tempRegions = new IRegion[--preserveCommentsLen];
						System.arraycopy(this.preserveExistingCommentsRanges, 0, tempRegions, 0, j);
						System.arraycopy(this.preserveExistingCommentsRanges, j+1, tempRegions, j, tempRegions.length - j);
						this.preserveExistingCommentsRanges = tempRegions;
						j--;
					}
				}
				return new ImportDeclEntry(containerName.length(), fullTypeName, isStatic, null, precedingCommentRange, trailingCommentRange);
			}
			prevOffset = currExtendedStart + currExtendedLen - 1;
		}
		return new ImportDeclEntry(containerName.length(), fullTypeName, isStatic, null);
	}

	public boolean removeImport(String qualifiedName, boolean isStatic) {
		String containerName= getQualifier(qualifiedName, isStatic);

		int nPackages= this.packageEntries.size();
		for (int i= 0; i < nPackages; i++) {
			PackageEntry entry= (PackageEntry) this.packageEntries.get(i);
			if (entry.compareTo(containerName, isStatic) == 0) {
				if (entry.remove(qualifiedName, isStatic)) {
					return true;
				}
			}
		}
		return false;
	}

	private int getIndexAfterStatics() {
		for (int i= 0; i < this.packageEntries.size(); i++) {
			if (!((PackageEntry) this.packageEntries.get(i)).isStatic()) {
				return i;
			}
		}
		return this.packageEntries.size();
	}


	private void sortIn(String typeContainerName, ImportDeclEntry decl, boolean isStatic) {
		PackageEntry bestMatch= findBestMatch(typeContainerName, isStatic);
		if (bestMatch == null) {
			PackageEntry packEntry= new PackageEntry(typeContainerName, null, isStatic);
			packEntry.add(decl);
			int insertPos= packEntry.isStatic() ? 0 : getIndexAfterStatics();
			this.packageEntries.add(insertPos, packEntry);
		} else {
			int cmp= typeContainerName.compareTo(bestMatch.getName());
			if (cmp == 0) {
				bestMatch.sortIn(decl);
			} else {
				// create a new package entry
				String group= bestMatch.getGroupID();
				if (group != null) {
					if (!typeContainerName.startsWith(group)) {
						group= null;
					}
				}
				PackageEntry packEntry= new PackageEntry(typeContainerName, group, isStatic);
				packEntry.add(decl);
				int index= this.packageEntries.indexOf(bestMatch);
				if (cmp < 0) {
					// insert ahead of best match
					this.packageEntries.add(index, packEntry);
				} else {
					// insert after best match
					this.packageEntries.add(index + 1, packEntry);
				}
			}
		}
	}

	private IRegion evaluateReplaceRange(CompilationUnit root) {
		List imports= root.imports();
		if (!imports.isEmpty()) {
			ImportDeclaration first= (ImportDeclaration) imports.get(0);
			ImportDeclaration last= (ImportDeclaration) imports.get(imports.size() - 1);

			int startPos= first.getStartPosition(); // no extended range for first: bug 121428
			int endPos= root.getExtendedStartPosition(last) + root.getExtendedLength(last);
			int endLine= root.getLineNumber(endPos);
			if (endLine > 0) {
				int nextLinePos= root.getPosition(endLine + 1, 0);
				if (nextLinePos >= 0) {
					int firstTypePos= getFirstTypeBeginPos(root);
					if (firstTypePos != -1 && firstTypePos < nextLinePos) {
						endPos= firstTypePos;
					} else {
						endPos= nextLinePos;
					}
				}
			}
			return new Region(startPos, endPos - startPos);
		} else {
			int start= getPackageStatementEndPos(root);
			return new Region(start, 0);
		}
	}

	public MultiTextEdit getResultingEdits(IProgressMonitor monitor) throws JavaModelException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		try {
			int importsStart=  this.replaceRange.getOffset();
			int importsLen= this.replaceRange.getLength();

			String lineDelim= this.compilationUnit.findRecommendedLineSeparator();
			IBuffer buffer= this.compilationUnit.getBuffer();

			int currPos= importsStart;
			MultiTextEdit resEdit= new MultiTextEdit();

			if ((this.flags & F_NEEDS_LEADING_DELIM) != 0) {
				// new import container
				resEdit.addChild(new InsertEdit(currPos, lineDelim));
			}

			PackageEntry lastPackage= null;

			Set onDemandConflicts= null;
			if (this.findAmbiguousImports) {
				onDemandConflicts= evaluateStarImportConflicts(monitor);
			}

			int spacesBetweenGroups= getSpacesBetweenImportGroups();

			ArrayList stringsToInsert= new ArrayList();

			int nPackageEntries= this.packageEntries.size();
			for (int i= 0; i < nPackageEntries; i++) {
				PackageEntry pack= (PackageEntry) this.packageEntries.get(i);
				if (this.filterImplicitImports && !pack.isStatic() && isImplicitImport(pack.getName())) {
					pack.filterImplicitImports(this.useContextToFilterImplicitImports);
				}
				int nImports= pack.getNumberOfImports();
				if (nImports == 0) {
					continue;
				}

				if (spacesBetweenGroups > 0 && lastPackage != null) {
					// add a space between two different groups by looking at the two adjacent imports
					if (!lastPackage.isComment() && !pack.isComment() && !pack.isSameGroup(lastPackage)) {
						for (int k= spacesBetweenGroups; k > 0; k--) {
							stringsToInsert.add(lineDelim);
						}
					} else if (lastPackage.isComment() && pack.isSameGroup(lastPackage)) {
						// the last pack may be a dummy for a comment which doesn't belong to any extended range
						stringsToInsert.add(lineDelim);
					}
				}
				lastPackage= pack;

				boolean isStatic= pack.isStatic();
				int threshold= isStatic ? this.staticImportOnDemandThreshold : this.importOnDemandThreshold;

				boolean doStarImport= pack.hasStarImport(threshold, onDemandConflicts);
				boolean allImportsAddedToStar = false;
				if (doStarImport && (pack.find("*") == null)) { //$NON-NLS-1$
					String[] imports = getNewImportStrings(buffer, pack, isStatic, lineDelim);
					for (int j = 0, max = imports.length; j < max; j++) {
						stringsToInsert.add(imports[j]);
					}
					allImportsAddedToStar = true; // may still need to handle onDemandConflicts below
				}

				for (int k= 0; k < nImports; k++) {
					ImportDeclEntry currDecl= pack.getImportAt(k);
					IRegion region= currDecl.getSourceRange();
					boolean isConflict = !currDecl.isComment() && onDemandConflicts != null && onDemandConflicts.contains(currDecl.getSimpleName());
					boolean addRegularToStar = doStarImport && !currDecl.isOnDemand();
					
					if (region == null) { // new entry
						if (!addRegularToStar || isConflict) {
							IRegion rangeBefore = currDecl.getPrecedingCommentRange();
							IRegion rangeAfter = currDecl.getTrailingCommentRange();
							if (rangeBefore != null) {
								stringsToInsert.add(buffer.getText(rangeBefore.getOffset(), rangeBefore.getLength()));
							}
							
							String trailingComment = null;
							if (rangeAfter != null) {
								trailingComment = buffer.getText(rangeAfter.getOffset(), rangeAfter.getLength());
							}
							String str= getNewImportString(currDecl.getElementName(), isStatic, trailingComment, lineDelim);
							stringsToInsert.add(str);
						} else if (addRegularToStar && !allImportsAddedToStar) {
							String simpleName = currDecl.getTypeQualifiedName();
							if (simpleName.indexOf('.') != -1) {
								String str= getNewImportString(currDecl.getElementName(), isStatic, lineDelim);
								if (stringsToInsert.indexOf(str) == -1) {
									stringsToInsert.add(str);
								}
							}
						}
					} else if (!addRegularToStar || isConflict) {
						int offset= region.getOffset();
						IRegion rangeBefore = currDecl.getPrecedingCommentRange();
						if (rangeBefore != null && currPos > rangeBefore.getOffset()) {
							// moved ahead of the leading comments, bring the currPos back
							currPos = rangeBefore.getOffset();
						}
						if (rangeBefore != null) {
							stringsToInsert.add(buffer.getText(rangeBefore.getOffset(), rangeBefore.getLength()));
						}
						removeAndInsertNew(buffer, currPos, offset, stringsToInsert, resEdit);
						stringsToInsert.clear();
						currPos= offset + region.getLength();
					} else if (addRegularToStar && !allImportsAddedToStar && !currDecl.isComment()) {
						String simpleName = currDecl.getTypeQualifiedName();
						if (simpleName.indexOf('.') != -1) {
							IRegion rangeBefore = currDecl.getPrecedingCommentRange();
							if (rangeBefore != null && currPos > rangeBefore.getOffset()) {
								// moved ahead of the leading comments, bring the currPos back
								currPos = rangeBefore.getOffset();
							}
							if (rangeBefore != null) {
								stringsToInsert.add(buffer.getText(rangeBefore.getOffset(), rangeBefore.getLength()));
							}
							IRegion rangeAfter = currDecl.getTrailingCommentRange();
							String trailingComment = null;
							if (rangeAfter != null) {
								trailingComment = buffer.getText(rangeAfter.getOffset(), rangeAfter.getLength());
							}
							String str= getNewImportString(currDecl.getElementName(), isStatic, trailingComment, lineDelim);
							if (stringsToInsert.indexOf(str) == -1) {
								stringsToInsert.add(str);
							}
						}
					}
				}
			}

			// insert back all existing imports comments since existing imports were not preserved
			if (this.preserveExistingCommentsRanges != null) {
				for (int i = 0, max = this.preserveExistingCommentsRanges.length; (i < max && this.preserveExistingCommentsRanges[i] != null); i++) {
					IRegion region = this.preserveExistingCommentsRanges[i];
					String text = buffer.getText(region.getOffset(), region.getLength());
					// remove preceding whitespaces
					int index = 0;
					int length = text.length();
					loop: while (index < length) {
						if (Character.isWhitespace(text.charAt(index))) {
							index++;
						} else {
							break loop;
						}
					}
					if (index != 0) {
						text = text.substring(index);
					}
					if (!text.endsWith(lineDelim)) {
						text += lineDelim;
					}
					stringsToInsert.add(text);
				}
			}
			int end= importsStart + importsLen;
			removeAndInsertNew(buffer, currPos, end, stringsToInsert, resEdit);

			if (importsLen == 0) {
				if (!this.importsCreated.isEmpty() || !this.staticImportsCreated.isEmpty()) { // new import container
					if ((this.flags & F_NEEDS_TRAILING_DELIM) != 0) {
						resEdit.addChild(new InsertEdit(currPos, lineDelim));
					}
				} else {
					return new MultiTextEdit(); // no changes
				}
			}
			return resEdit;
		} finally {
			monitor.done();
		}
	}

	private void removeAndInsertNew(IBuffer buffer, int contentOffset, int contentEnd, ArrayList stringsToInsert, MultiTextEdit resEdit) {
		int pos= contentOffset;
		for (int i= 0; i < stringsToInsert.size(); i++) {
			String curr= (String) stringsToInsert.get(i);
			int idx= findInBuffer(buffer, curr, pos, contentEnd);
			if (idx != -1) {
				if (idx != pos) {
					resEdit.addChild(new DeleteEdit(pos, idx - pos));
				}
				pos= idx + curr.length();
			} else {
				resEdit.addChild(new InsertEdit(pos, curr));
			}
		}
		if (pos < contentEnd) {
			resEdit.addChild(new DeleteEdit(pos, contentEnd - pos));
		}
	}

	private int findInBuffer(IBuffer buffer, String str, int start, int end) {
		int pos= start;
		int len= str.length();
		if (pos + len > end || str.length() == 0) {
			return -1;
		}
		char first= str.charAt(0);
		int step= str.indexOf(first, 1);
		if (step == -1) {
			step= len;
		}
		while (pos + len <= end) {
			if (buffer.getChar(pos) == first) {
				int k= 1;
				while (k < len && buffer.getChar(pos + k) == str.charAt(k)) {
					k++;
				}
				if (k == len) {
					return pos; // found
				}
				if (k < step) {
					pos+= k;
				} else {
					pos+= step;
				}
			} else {
				pos++;
			}
		}
		return -1;
	}

	private Set evaluateStarImportConflicts(IProgressMonitor monitor) throws JavaModelException {
		//long start= System.currentTimeMillis();

		final HashSet/*String*/ onDemandConflicts= new HashSet();

		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(new IJavaElement[] { this.compilationUnit.getJavaProject() });

		ArrayList/*<char[][]>*/  starImportPackages= new ArrayList();
		ArrayList/*<char[][]>*/ simpleTypeNames= new ArrayList();
		int nPackageEntries= this.packageEntries.size();
		for (int i= 0; i < nPackageEntries; i++) {
			PackageEntry pack= (PackageEntry) this.packageEntries.get(i);
			if (!pack.isStatic() && pack.hasStarImport(this.importOnDemandThreshold, null)) {
				starImportPackages.add(pack.getName().toCharArray());
				for (int k= 0; k < pack.getNumberOfImports(); k++) {
					ImportDeclEntry curr= pack.getImportAt(k);
					if (!curr.isOnDemand() && !curr.isComment()) {
						simpleTypeNames.add(curr.getSimpleName().toCharArray());
					}
				}
			}
		}
		if (starImportPackages.isEmpty()) {
			return null;
		}

		starImportPackages.add(this.compilationUnit.getParent().getElementName().toCharArray());
		starImportPackages.add(JAVA_LANG.toCharArray());

		char[][] allPackages= (char[][]) starImportPackages.toArray(new char[starImportPackages.size()][]);
		char[][] allTypes= (char[][]) simpleTypeNames.toArray(new char[simpleTypeNames.size()][]);

		TypeNameRequestor requestor= new TypeNameRequestor() {
			HashMap foundTypes= new HashMap();

			private String getTypeContainerName(char[] packageName, char[][] enclosingTypeNames) {
				StringBuffer buf= new StringBuffer();
				buf.append(packageName);
				for (int i= 0; i < enclosingTypeNames.length; i++) {
					if (buf.length() > 0)
						buf.append('.');
					buf.append(enclosingTypeNames[i]);
				}
				return buf.toString();
			}

			public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
				String name= new String(simpleTypeName);
				String containerName= getTypeContainerName(packageName, enclosingTypeNames);

				String oldContainer= (String) this.foundTypes.put(name, containerName);
				if (oldContainer != null && !oldContainer.equals(containerName)) {
					onDemandConflicts.add(name);
				}
			}
		};
		new SearchEngine().searchAllTypeNames(allPackages, allTypes, scope, requestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
		return onDemandConflicts;
	}

	private String getNewImportString(String importName, boolean isStatic, String lineDelim) {
		return getNewImportString(importName, isStatic, null, lineDelim);
	}
	
	private String getNewImportString(String importName, boolean isStatic, String trailingComment, String lineDelim) {
		StringBuffer buf= new StringBuffer();
		buf.append("import "); //$NON-NLS-1$
		if (isStatic) {
			buf.append("static "); //$NON-NLS-1$
		}
		buf.append(importName);
		if (insertSpaceBeforeSemicolon()) buf.append(' ');
		buf.append(';');
		if (trailingComment != null) {
			buf.append(trailingComment);
		}
		buf.append(lineDelim);

		if (isStatic) {
			this.staticImportsCreated.add(importName);
		} else {
			this.importsCreated.add(importName);
		}
		return buf.toString();
	}
	
	private String[] getNewImportStrings(IBuffer buffer, PackageEntry packageEntry, boolean isStatic, String lineDelim) {
		boolean isStarImportAdded = false;
		List allImports = new ArrayList();
		int nImports = packageEntry.getNumberOfImports();
		StringBuffer allComments = null;
		StringBuffer allCommentsLead = null;
		for (int i= 0; i < nImports; i++) {
			ImportDeclEntry curr= packageEntry.getImportAt(i);
			if (curr.isComment()) {
				IRegion rangeBefore = curr.getPrecedingCommentRange();
				if (rangeBefore != null) {
					allImports.add(buffer.getText(rangeBefore.getOffset(), rangeBefore.getLength()));
				}
				IRegion rangeAfter = curr.getTrailingCommentRange();
				String trailingComment = null;
				if (rangeAfter != null) {
					trailingComment = buffer.getText(rangeAfter.getOffset(), rangeAfter.getLength());
				}
				if (trailingComment != null) {
					allImports.add(buffer.getText(rangeAfter.getOffset(), rangeAfter.getLength()));
				}
			} else {
				String simpleName = curr.getTypeQualifiedName();
				if (simpleName.indexOf('.') != -1) {
					// member type imports - we preserve it
					IRegion rangeBefore = curr.getPrecedingCommentRange();
					if (rangeBefore != null) {
						allImports.add(buffer.getText(rangeBefore.getOffset(), rangeBefore.getLength()));
					}
					IRegion rangeAfter = curr.getTrailingCommentRange();
					String trailingComment = null;
					if (rangeAfter != null) {
						trailingComment = buffer.getText(rangeAfter.getOffset(), rangeAfter.getLength());
					}
					allImports.add(getNewImportString(curr.getElementName(), isStatic, trailingComment, lineDelim));
				} else if (!isStarImportAdded) {
					String starImportString= packageEntry.getName() + ".*"; //$NON-NLS-1$
					// collect all comments
					IRegion rangeBefore = curr.getPrecedingCommentRange();
					if (rangeBefore != null) {
						allImports.add(buffer.getText(rangeBefore.getOffset(), rangeBefore.getLength()));
					}
					IRegion rangeAfter = curr.getTrailingCommentRange();
					String trailComments = null;
					if (rangeAfter != null) {
						trailComments = buffer.getText(rangeAfter.getOffset(), rangeAfter.getLength());
					}
					allImports.add(getNewImportString(starImportString, isStatic, trailComments, lineDelim));
					isStarImportAdded = true;
				} else {
					// collect all comments
					IRegion rangeBefore = curr.getPrecedingCommentRange();
					if (rangeBefore != null) {
						if (allCommentsLead == null) {
							allCommentsLead = new StringBuffer();
						}
						allCommentsLead.append(buffer.getText(rangeBefore.getOffset(), rangeBefore.getLength()));
					}
					IRegion rangeAfter = curr.getTrailingCommentRange();
					if (rangeAfter != null) {
						if (allComments == null) {
							allComments = new StringBuffer();
						}
						allComments.append(buffer.getText(rangeAfter.getOffset(), rangeAfter.getLength()));
					}
				}
			}
		}
		if (allCommentsLead != null) {
			allImports.add(0, String.valueOf(allCommentsLead));
		}
		if (allComments != null) {
			allImports.add(String.valueOf(allComments.append(lineDelim)));
		}
		return (String[]) allImports.toArray(new String[allImports.size()]);
	}

	private static int getFirstTypeBeginPos(CompilationUnit root) {
		List types= root.types();
		if (!types.isEmpty()) {
			return root.getExtendedStartPosition(((ASTNode) types.get(0)));
		}
		return -1;
	}

	private int getPackageStatementEndPos(CompilationUnit root) {
		PackageDeclaration packDecl= root.getPackage();
		if (packDecl != null) {
			int afterPackageStatementPos= -1;
			int lineNumber= root.getLineNumber(packDecl.getStartPosition() + packDecl.getLength());
			if (lineNumber >= 0) {
				int lineAfterPackage= lineNumber + 1;
				afterPackageStatementPos= root.getPosition(lineAfterPackage, 0);
			}
			if (afterPackageStatementPos < 0) {
				this.flags|= F_NEEDS_LEADING_DELIM;
				return packDecl.getStartPosition() + packDecl.getLength();
			}
			int firstTypePos= getFirstTypeBeginPos(root);
			if (firstTypePos != -1 && firstTypePos <= afterPackageStatementPos) {
				this.flags|= F_NEEDS_TRAILING_DELIM;
				if (firstTypePos == afterPackageStatementPos) {
					this.flags|= F_NEEDS_LEADING_DELIM;
				}
				return firstTypePos;
			}
			this.flags|= F_NEEDS_LEADING_DELIM;
			return afterPackageStatementPos; // insert a line after after package statement
		}
		this.flags |= F_NEEDS_TRAILING_DELIM;
		return 0;
	}

	public String toString() {
		int nPackages= this.packageEntries.size();
		StringBuffer buf= new StringBuffer("\n-----------------------\n"); //$NON-NLS-1$
		for (int i= 0; i < nPackages; i++) {
			PackageEntry entry= (PackageEntry) this.packageEntries.get(i);
			if (entry.isStatic()) {
				buf.append("static "); //$NON-NLS-1$
			}
			buf.append(entry.toString());
		}
		return buf.toString();
	}

	private static final class ImportDeclEntry {

		private String elementName;
		private IRegion sourceRange;
		private final boolean isStatic;
		private int containerNameLength;
		IRegion precedingCommentRange;
		IRegion trailingCommentRange;

		public ImportDeclEntry(
				int containerNameLength,
				String elementName,
				boolean isStatic,
				IRegion sourceRange,
				IRegion precedingCommentRange,
				IRegion trailingCommentRange) {
			this(containerNameLength, elementName, isStatic, sourceRange);
			this.precedingCommentRange = precedingCommentRange;
			this.trailingCommentRange = trailingCommentRange;
		}

		public ImportDeclEntry(int containerNameLength, String elementName, boolean isStatic, IRegion sourceRange) {
			this.elementName= elementName;
			this.sourceRange= sourceRange;
			this.isStatic= isStatic;
			this.containerNameLength = containerNameLength;
		}

		public String getElementName() {
			return this.elementName;
		}

		public int compareTo(String fullName, boolean isStaticImport) {
			int cmp= this.elementName.compareTo(fullName);
			if (cmp == 0) {
				if (this.isStatic == isStaticImport) {
					return 0;
				}
				return this.isStatic ? -1 : 1;
			}
			return cmp;
		}

		public String getSimpleName() {
			return Signature.getSimpleName(this.elementName);
		}

		public String getTypeQualifiedName() {
			return this.elementName.substring(this.containerNameLength + 1);
		}
		
		public boolean isOnDemand() {
			return this.elementName != null && this.elementName.endsWith(".*"); //$NON-NLS-1$
		}

		public boolean isStatic() {
			return this.isStatic;
		}

		public boolean isNew() {
			return this.sourceRange == null;
		}

		public boolean isComment() {
			return this.elementName == null;
		}

		public IRegion getSourceRange() {
			return this.sourceRange;
		}
		
		public IRegion getPrecedingCommentRange() {
			return this.precedingCommentRange;
		}

		public IRegion getTrailingCommentRange() {
			return this.trailingCommentRange;
		}
	}

	/*
	 * Internal element for the import structure: A container for imports
	 * of all types from the same package
	 */
	private final static class PackageEntry {
		private String name;
		private ArrayList importEntries;
		private String group;
		private boolean isStatic;

		/**
		 * Comment package entry
		 */
		public PackageEntry() {
			this("!", null, false); //$NON-NLS-1$
		}

		/**
		 * @param name Name of the package entry. e.g. org.eclipse.jdt.ui, containing imports like
		 * org.eclipse.jdt.ui.JavaUI.
		 * @param group The index of the preference order entry assigned
		 *    different group id's will result in spacers between the entries
		 */
		public PackageEntry(String name, String group, boolean isStatic) {
			this.name= name;
			this.importEntries= new ArrayList(5);
			this.group= group;
			this.isStatic= isStatic;
		}

		public boolean isStatic() {
			return this.isStatic;
		}

		public int compareTo(String otherName, boolean isOtherStatic) {
			int cmp= this.name.compareTo(otherName);
			if (cmp == 0) {
				if (this.isStatic == isOtherStatic) {
					return 0;
				}
				return this.isStatic ? -1 : 1;
			}
			return cmp;
		}

		public void sortIn(ImportDeclEntry imp) {
			String fullImportName= imp.getElementName();
			int insertPosition= -1;
			int nInports= this.importEntries.size();
			for (int i= 0; i < nInports; i++) {
				ImportDeclEntry curr= getImportAt(i);
				if (!curr.isComment()) {
					int cmp= curr.compareTo(fullImportName, imp.isStatic());
					if (cmp == 0) {
						return; // exists already
					} else if (cmp > 0 && insertPosition == -1) {
						insertPosition= i;
					}
				}
			}
			if (insertPosition == -1) {
				this.importEntries.add(imp);
			} else {
				this.importEntries.add(insertPosition, imp);
			}
		}


		public void add(ImportDeclEntry imp) {
			this.importEntries.add(imp);
		}

		public ImportDeclEntry find(String simpleName) {
			int nInports= this.importEntries.size();
			for (int i= 0; i < nInports; i++) {
				ImportDeclEntry curr= getImportAt(i);
				if (!curr.isComment()) {
					String currName= curr.getElementName();
					if (currName.endsWith(simpleName)) {
						int dotPos= currName.length() - simpleName.length() - 1;
						if ((dotPos == -1) || (dotPos > 0 && currName.charAt(dotPos) == '.')) {
							return curr;
						}
					}
				}
			}
			return null;
		}

		public boolean remove(String fullName, boolean isStaticImport) {
			int nInports= this.importEntries.size();
			for (int i= 0; i < nInports; i++) {
				ImportDeclEntry curr= getImportAt(i);
				if (!curr.isComment() && curr.compareTo(fullName, isStaticImport) == 0) {
					this.importEntries.remove(i);
					return true;
				}
			}
			return false;
		}

		public void filterImplicitImports(boolean useContextToFilterImplicitImports) {
			int nInports= this.importEntries.size();
			for (int i= nInports - 1; i >= 0; i--) {
				ImportDeclEntry curr= getImportAt(i);
				if (curr.isNew()) {
					if (!useContextToFilterImplicitImports) {
						this.importEntries.remove(i);
					} else {
						String elementName = curr.getElementName();
						int lastIndexOf = elementName.lastIndexOf('.');
						boolean internalClassImport = lastIndexOf > getName().length();
						if (!internalClassImport) {
							this.importEntries.remove(i);
						}
					}
				}
			}
		}

		public ImportDeclEntry getImportAt(int index) {
			return (ImportDeclEntry) this.importEntries.get(index);
		}

		public boolean hasStarImport(int threshold, Set explicitImports) {
			if (isComment() || isDefaultPackage()) { // can not star import default package
				return false;
			}
			int nImports= getNumberOfImports();
			int count= 0;
			boolean containsNew= false;
			for (int i= 0; i < nImports; i++) {
				ImportDeclEntry curr= getImportAt(i);
				if (curr.isOnDemand()) {
					return true;
				}
				if (!curr.isComment()) {
					count++;
					boolean isExplicit= !curr.isStatic() && (explicitImports != null) && explicitImports.contains(curr.getSimpleName());
					containsNew |= curr.isNew() && !isExplicit;
				}
			}
			return (count >= threshold) && containsNew;
		}

		public int getNumberOfImports() {
			return this.importEntries.size();
		}

		public String getName() {
			return this.name;
		}

		public String getGroupID() {
			return this.group;
		}

		public void setGroupID(String groupID) {
			this.group= groupID;
		}

		public boolean isSameGroup(PackageEntry other) {
			if (this.group == null) {
				return other.getGroupID() == null;
			} else {
				return this.group.equals(other.getGroupID()) && (this.isStatic == other.isStatic());
			}
		}

		public boolean isComment() {
			return "!".equals(this.name); //$NON-NLS-1$
		}

		public boolean isDefaultPackage() {
			return this.name.length() == 0;
		}

		public String toString() {
			StringBuffer buf= new StringBuffer();
			if (isComment()) {
				buf.append("comment\n"); //$NON-NLS-1$
			} else {
				buf.append(this.name); buf.append(", groupId: "); buf.append(this.group); buf.append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
				int nImports= getNumberOfImports();
				for (int i= 0; i < nImports; i++) {
					ImportDeclEntry curr= getImportAt(i);
					buf.append(" "); //$NON-NLS-1$
					if (curr.isComment()) {
						buf.append("comment"); //$NON-NLS-1$
					} else {
						if (curr.isStatic()) {
							buf.append("static "); //$NON-NLS-1$
						}
						buf.append(curr.getTypeQualifiedName());
					}
					if (curr.isNew()) {
						buf.append(" (new)"); //$NON-NLS-1$
					}
					buf.append("\n"); //$NON-NLS-1$
				}
			}
			return buf.toString();
		}
	}

	public String[] getCreatedImports() {
	    return (String[]) this.importsCreated.toArray(new String[this.importsCreated.size()]);
	}

	public String[] getCreatedStaticImports() {
	    return (String[]) this.staticImportsCreated.toArray(new String[this.staticImportsCreated.size()]);
	}

}
