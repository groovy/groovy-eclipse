/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.test.internal.performance.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.test.internal.performance.InternalDimensions;
import org.eclipse.test.internal.performance.data.Dim;

/**
 * Abstract class to store performance results.
 * 
 * Each results gives access to specific children depending on model.
 */
public abstract class AbstractResults implements Comparable {

	private static final int ONE_MINUTE = 60000;

	private static final long ONE_HOUR = 3600000L;

	/**
	 * The list of supported dimensions.
	 * <p>
	 * Currently only {@link InternalDimensions#ELAPSED_PROCESS}
	 * and {@link InternalDimensions#CPU_TIME}.
	 */
	public final static Dim[] SUPPORTED_DIMS = {
		InternalDimensions.ELAPSED_PROCESS,
		InternalDimensions.CPU_TIME
	};

	static final int DEFAULT_DIM_INDEX = 0;
	/**
	 * The default dimension used to display results (typically in fingerprints).
	 * <p>
	 * Currently {@link InternalDimensions#ELAPSED_PROCESS}
	 */
	public static final Dim DEFAULT_DIM = SUPPORTED_DIMS[DEFAULT_DIM_INDEX];

	/**
	 * The list of possible configurations.
	 * <p>
	 * Only used if no specific configurations are specified
	 * (see {@link PerformanceResults#read(File)}.
	 */
	public final static String[] CONFIGS;

	/**
	 * The list of possible test boxes.
	 * <p>
	 * Only used if no specific configurations are specified
	 * (see {@link PerformanceResults#read(File)}.
	 * </p>
	 * Note that this is a copy of the the property "eclipse.perf.config.descriptors"
	 * defined in org.eclipse.releng.eclipsebuilder/eclipse/helper.xml file
	 */
	public final static String[] BOXES;
	static {
		String descriptors = "Win XP Sun 1.5.0_10 (2 x 3.00GHz - 3GB RAM), SLED 10 Sun 1.5.0_10 (2 x 3.00GHz - 3GB RAM), RHEL 5.0 Sun 6.0_04 (2 x 3.00GHz - 3GB RAM)"; //$NON-NLS-1$
		StringTokenizer tokenizer = new StringTokenizer(descriptors, ","); //$NON-NLS-1$
		List boxes = new ArrayList();
		while (tokenizer.hasMoreTokens()) {
			boxes.add(tokenizer.nextToken().trim());
		}
		BOXES = new String[boxes.size()];
		boxes.toArray(BOXES);
	}

	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm"); //$NON-NLS-1$

	// Initialize constant fields depending on DB version
	public final static String VERSION_REF, VERSION;
	static {
		// Initialize reference version and database directory
		VERSION_REF = "R-3.4"; //$NON-NLS-1$
		VERSION = "3.5"; //$NON-NLS-1$
	
		// Initialize configuration names
		CONFIGS = new String[] {
			"epwin2", //$NON-NLS-1$
			"eplnx1", //$NON-NLS-1$
			"eplnx2", //$NON-NLS-1$
		};
	}

	AbstractResults parent;
	int id = -1;
	String name;
	List children;
	private static boolean NEW_LINE = true;
	PrintStream printStream = null;

/**
 * Copy a file to another location.
 *
 * @param src the source file.
 * @param dest the destination.
 * @return <code>true</code> if the file was successfully copied,
 * 	<code>false</code> otherwise.
 */
public static boolean copyFile(File src, File dest) {

	try {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		return false;
	} catch (IOException e) {
		e.printStackTrace();
		return false;
	}
	return true;
}

/*
 * Return the build date as yyyyMMddHHmm
 */
public static String getBuildDate(String buildName) {
	return getBuildDate(buildName, VERSION_REF);
}
/*
 * Return the build date as yyyyMMddHHmm
 */
public static String getBuildDate(String buildName, String baselinePrefix) {

	// Baseline name
	if (baselinePrefix != null && buildName.startsWith(baselinePrefix)) {
		int length = buildName.length();
		return buildName.substring(length-12, length);
	}
	
	// Build name
	char first = buildName.charAt(0);
	if (first == 'N' || first == 'I' || first == 'M') { // TODO (frederic) should be buildIdPrefixes...
		return buildName.substring(1, 9)+buildName.substring(10, 14);
	}
	
	// Try with date format
	int length = buildName.length() - 12 /* length of date */;
	for (int i=0; i<=length; i++) {
		try {
			String substring = i == 0 ? buildName : buildName.substring(i);
			DATE_FORMAT.parse(substring);
			return substring; // if no exception is raised then the substring has a correct date format => return it
		} catch(ParseException ex) {
			// skip
		}
	}
	return null;
}

static Dim getDimension(int id) {
	int length = SUPPORTED_DIMS.length;
	for (int i=0; i<length; i++) {
		if (SUPPORTED_DIMS[i].getId() == id) {
			return SUPPORTED_DIMS[i];
		}
	}
	return null;
}

public static String timeString(long time) {
	NumberFormat format = NumberFormat.getInstance();
	format.setMaximumFractionDigits(1);
	StringBuffer buffer = new StringBuffer();
	if (time == 0) {
		// print nothing
	} if (time < 100) { // less than 0.1s
		buffer.append(time);
		buffer.append("ms"); //$NON-NLS-1$
	} else if (time < 1000) { // less than 1s
		if ((time%100) != 0) {
			format.setMaximumFractionDigits(2);
		}
		buffer.append(format.format(time/1000.0));
		buffer.append("s"); //$NON-NLS-1$
	} else if (time < ONE_MINUTE) {  // less than 1mn
		if ((time%1000) == 0) {
			buffer.append(time/1000);
		} else {
			buffer.append(format.format(time/1000.0));
		}
		buffer.append("s"); //$NON-NLS-1$
	} else if (time < ONE_HOUR) {  // less than 1h
		buffer.append(time/ONE_MINUTE).append("mn "); //$NON-NLS-1$
		long seconds = time%ONE_MINUTE;
		if ((seconds%1000) == 0) {
			buffer.append(seconds/1000);
		} else {
			buffer.append(format.format(seconds/1000.0));
		}
		buffer.append("s"); //$NON-NLS-1$
	} else {  // more than 1h
		long h = time / ONE_HOUR;
		buffer.append(h).append("h "); //$NON-NLS-1$
		long m = (time % ONE_HOUR) / ONE_MINUTE;
		buffer.append(m).append("mn "); //$NON-NLS-1$
		long seconds = m%ONE_MINUTE;
		if ((seconds%1000) == 0) {
			buffer.append(seconds/1000);
		} else {
			buffer.append(format.format(seconds/1000.0));
		}
		buffer.append("s"); //$NON-NLS-1$
	}
	return buffer.toString();
}

AbstractResults(AbstractResults parent, String name) {
	this.parent = parent;
	this.children = new ArrayList();
	this.name = name;
}

AbstractResults(AbstractResults parent, int id) {
	this.parent = parent;
	this.children = new ArrayList();
	this.id = id;
}

/*
 * Add a child to current results, using specific sort
 * order if specified.
 */
void addChild(Comparable child, boolean sort) {
	if (sort) {
		int size = this.children.size();
		for (int i=0; i<size; i++) {
			Object results = this.children.get(i);
			if (child.compareTo(results) < 0) {
				this.children.add(i, child);
				return;
			}
		}
	}
	this.children.add(child);
}

/**
 * Compare the results to the given one using the name.
 * 
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
public int compareTo(Object obj) {
	if (obj instanceof AbstractResults) {
		AbstractResults res = (AbstractResults) obj;
		return getName().compareTo(res.getName());
	}
	return -1;
}

/**
 * Returns whether two results are equals using the name
 * to compare them.
 * 
 * @param obj  The results to compare with
 * @return <code>true</code> if the name are equals,
 * 	<code>false</code> otherwise
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
public boolean equals(Object obj) {
	if (obj instanceof AbstractResults) {
		return this.name.equals(((AbstractResults)obj).getName());
	}
	return super.equals(obj);
}

/**
 * Return an array built on the current results children list.
 * 
 * @return An array of the children list
 */
public AbstractResults[] getChildren() {
	AbstractResults[] elements = new AbstractResults[size()];
	this.children.toArray(elements);
	return elements;
}

ComponentResults getComponentResults() {
	if (this.parent != null) {
		return this.parent.getComponentResults();
	}
	return null;
}

int getId() {
	return this.id;
}

/**
 * Returns the name of the results object.
 * 
 * @return The name of the results
 */
public String getName() {
	return this.name;
}

/**
 * Returns the parent
 * 
 * @return The parent
 */
public AbstractResults getParent() {
	return this.parent;
}

PerformanceResults getPerformance() {
	if (this.parent != null) {
		return this.parent.getPerformance();
	}
	return null;
}

String getPath() {
	String path = this.parent==null || this.parent.parent==null ? "" : this.parent.getPath() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
	return path+this.name;
}

/**
 * Return the children list of the current results.
 * 
 * @return An iterator on the children list
 */
public Iterator getResults() {
	return this.children.iterator();
}

AbstractResults getResults(String resultName) {
	int size = this.children.size();
	for (int i=0; i<size; i++) {
		AbstractResults searchedResults = (AbstractResults) this.children.get(i);
		if (searchedResults.getName().equals(resultName)) {
			return searchedResults;
		}
	}
	return null;
}

AbstractResults getResults(int searchedId) {
	int size = this.children.size();
	for (int i=0; i<size; i++) {
		AbstractResults searchedResults = (AbstractResults) this.children.get(i);
		if (searchedResults.id == searchedId) {
			return searchedResults;
		}
	}
	return null;
}

public int hashCode() {
	return this.name.hashCode();
}

void printTab() {
	if (this.parent != null) {
		if (this.printStream != null) this.printStream.print("\t"); //$NON-NLS-1$
		this.parent.printTab();
	}
}
void print(String text) {
	if (this.printStream != null) {
		if (NEW_LINE) printTab();
		this.printStream.print(text);
		NEW_LINE = false;
	}
}

void printGlobalTime(long start) {
	printGlobalTime(start, null);
}

void printGlobalTime(long start, String end) {
	long time = System.currentTimeMillis();
	StringBuffer buffer = new StringBuffer(" => time spent in '"); //$NON-NLS-1$
	buffer.append(this.name);
	buffer.append("' was "); //$NON-NLS-1$
	buffer.append(timeString(time-start));
	if (end != null) {
		buffer.append(". "); //$NON-NLS-1$
		buffer.append(end.trim());
	}
	println(buffer);
}

void println(String text) {
	if (this.printStream != null) {
		if (NEW_LINE) printTab();
		this.printStream.println(text);
		NEW_LINE = true;
	}
}

void println(StringBuffer buffer) {
	println(buffer.toString());
}

public int size() {
	return this.children == null ? 0 : this.children.size();
}

public String toString() {
	return getPath();
}

}
