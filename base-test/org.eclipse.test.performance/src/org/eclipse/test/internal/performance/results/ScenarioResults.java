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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;


/**
 * Class to handle performance results of a component's scenario
 * (for example 'org.eclipse.jdt.core.FullSourceWorkspaceSearchTest#searchAllTypeNames()').
 * 
 * It gives access to results for each configuration run on this scenario.
 * 
 * @see ConfigResults
 */
public class ScenarioResults extends AbstractResults {
	String fileName;
	String label;
	String shortName;

public ScenarioResults(int id, String name, String shortName) {
	super(null, id);
	this.name = name;
	this.label = shortName;
}

/*
 * Complete results with additional database information.
 */
void completeResults(String lastBuildName) {
	String[] builds = DB_Results.getBuilds();
	class BuildDateComparator implements Comparator {
		public int compare(Object o1, Object o2) {
	        String s1 = (String) o1;
	        String s2 = (String) o2;
	        return AbstractResults.getBuildDate(s1).compareTo(AbstractResults.getBuildDate(s2));
	    }
	}
	BuildDateComparator comparator = new BuildDateComparator();
	Arrays.sort(builds, comparator);
	int idx = Arrays.binarySearch(builds, lastBuildName, comparator);
	if (idx < 0) {
		builds = null;
	} else {
		int size = builds.length - ++idx;
		System.arraycopy(builds, idx, builds = new String[size], 0, size);
	}
//	String[] builds = null;
	int size = size();
	for (int i=0; i<size; i++) {
		ConfigResults configResults = (ConfigResults) this.children.get(i);
		configResults.completeResults(builds);
	}
}

/**
 * Returns the first configuration baseline build name.
 *
 * @return The name of the baseline build
 * @see ConfigResults#getBaselineBuildName()
 */
public String getBaselineBuildName() {
	int size = size();
	StringBuffer buffer = new StringBuffer();
	for (int i=0; i<size; i++) {
		ConfigResults configResults = (ConfigResults) this.children.get(i);
		if (configResults.isValid()) {
			return configResults.getBaselineBuildName();
			/* TODO (frederic) decide what return when baseline is not the same on all configs...
			 * Currently returns the first found, but may be a comma-separated list?
			String baselineName = configResults.getBaselineBuildName();
			if (buffer.indexOf(baselineName) < 0) {
				if (buffer.length() > 0) buffer.append('|');
				buffer.append(baselineName);
			}
			*/
		}
	}
	return buffer.toString();
}

/**
 * Return the results of the given configuration.
 * 
 * @param config The configuration name
 * @return The {@link ConfigResults results} for the given configuration
 * 	or <code>null</code> if none was found.
 */
public ConfigResults getConfigResults(String config) {
	return (ConfigResults) getResults(config);
}

/**
 * Return a name which can be used as a file name to store information
 * related to this scenario. This name does not contain the extension.
 * 
 * @return The file name
 */
public String getFileName() {
	if (this.fileName == null) {
		this.fileName = "Scenario" + this.id; //$NON-NLS-1$
	}
	return this.fileName;
}

/**
 * Returns the scenario label. If no label exist as there's no associated summary,
 * then the short name is returned
 *
 * @return The label of the scenario or it's short name if no summary exists
 */
public String getLabel() {
	return this.label;
}

/**
 * Returns the short name of the scenario. Short name is the name scenario
 * from which package declaration has been removed.
 * 
 * @return The scenario short name
 */
public String getShortName() {
	if (this.shortName == null) {
		// Remove class name qualification
		int testSeparator = this.name.indexOf('#');
		boolean hasClassName = testSeparator >= 0;
		if (!hasClassName) {
			testSeparator = this.name.lastIndexOf('.');
			if (testSeparator <= 0) {
				return this.shortName = this.name;
			}
		}
		int classSeparator = this.name.substring(0, testSeparator).lastIndexOf('.');
		if (classSeparator < 0) {
			return this.shortName = this.name;
		}
		int length = this.name.length();
		String testName = this.name.substring(classSeparator+1, length);
		if (!hasClassName && testName.startsWith("test.")) { // specific case for swt... //$NON-NLS-1$
			testName = testName.substring(5);
		}
	
		// Remove qualification from test name
		StringTokenizer tokenizer = new StringTokenizer(testName, " :,", true); //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer(tokenizer.nextToken());
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			char fc = token.charAt(0);
			while (fc == ' ' || fc == ',' || fc == ':') {
				buffer.append(token); // add the separator
				token = tokenizer.nextToken();
				fc = token.charAt(0);
			}
			int last = token.lastIndexOf('.');
			if (last >= 3) {
				int first = token .indexOf('.');
				if (first == last) {
					buffer.append(token);
				} else {
					buffer.append(token.substring(last+1));
				}
			} else {
				buffer.append(token);
			}
		}
		this.shortName = buffer.toString();
	}
	return this.shortName;
}

/**
 * Returns whether one of the scenario's config has a summary or not.
 * 
 * @return <code>true</code> if one of the scenario's config has a summary
 * 	<code>false</code> otherwise.
 */
public boolean hasSummary() {
	int size = size();
	for (int i=0; i<size; i++) {
		ConfigResults configResults = (ConfigResults) this.children.get(i);
		if (configResults.getCurrentBuildResults().hasSummary()) return true;
	}
	return false;
}

/* (non-Javadoc)
 * @see org.eclipse.test.internal.performance.results.AbstractResults#hashCode()
 */
public int hashCode() {
	return this.id;
}

/**
 * Returns whether the current build of the given config has valid results or not.
 * 
 * @param config The name of the configuration
 * @return <code>true</code> if the build has valid results
 * 	<code>false</code> otherwise.
 */
public boolean isValid(String config) {
	return getResults(config) != null;
}

/*
 * Read scenario results information from database.
 */
void read() {

	// Get values
	print("+ scenario '"+getShortName()+"': "); //$NON-NLS-1$ //$NON-NLS-2$
	long start = System.currentTimeMillis();
	String configPattern = getPerformance().getConfigurationsPattern();
	DB_Results.queryScenarioValues(this, configPattern);
	print(" values for "+size()+" configs ("+(System.currentTimeMillis()-start)+"ms), "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Set baseline and current builds
	start = System.currentTimeMillis();
	int size = size();
	for (int i=0; i<size; i++) {
		ConfigResults configResults = (ConfigResults) this.children.get(i);
		configResults.completeResults(null);
	}
	println("summaries ("+(System.currentTimeMillis()-start)+"ms)."); //$NON-NLS-1$ //$NON-NLS-2$
}

/*
 * Read data from a local file
 */
void readData(DataInputStream stream) throws IOException {

	// Read data stored locally
	int size = stream.readInt();
	for (int i=0; i<size; i++) {
		int config_id = stream.readInt();
		ConfigResults configResults = (ConfigResults) getResults(config_id);
		if (configResults == null) {
			configResults = new ConfigResults(this, config_id);
			addChild(configResults, true);
		}
		configResults.readData(stream);
	}
}

/*
 * Read new data from the database.
 * This is typically needed when the build results are not in the local file...
 */
boolean readNewData(String lastBuildName) throws IOException {
	PerformanceResults performanceResults = getPerformance();
	String configPattern = performanceResults.getConfigurationsPattern();
	String lastBuildDate = getBuildDate(lastBuildName, performanceResults.getBaselinePrefix());
	if (performanceResults.getBuildDate().compareTo(lastBuildDate) > 0) {
		long lastBuildTime = 0;
	    try {
		    lastBuildTime = DATE_FORMAT.parse(lastBuildDate).getTime();
	    } catch (ParseException e) {
		    // should not happen
	    }
		long start = System.currentTimeMillis();
		print("	+ scenario '"+getShortName()+"': values..."); //$NON-NLS-1$ //$NON-NLS-2$
		DB_Results.queryScenarioValues(this, configPattern, lastBuildName, lastBuildTime);
		print(timeString(System.currentTimeMillis()-start));
		return true;
	}
	return false;
}

/*
 * Set value from database information.
 */
void setInfos(int config_id, int build_id, int summaryKind, String comment) {
	ConfigResults configResults = (ConfigResults) getResults(config_id);
	if (configResults == null) {
		configResults = new ConfigResults(this, config_id);
		addChild(configResults, true);
	}
	configResults.setInfos(build_id, summaryKind, comment);
}

/*
 * Set value from database information.
 */
void setValue(int build_id, int dim_id, int config_id, int step, long value) {
	ConfigResults configResults = (ConfigResults) getResults(config_id);
	if (configResults == null) {
		configResults = new ConfigResults(this, config_id);
		addChild(configResults, true);
	}
	configResults.setValue(build_id, dim_id, step, value);
}

void write(DataOutputStream stream) throws IOException {
	int size = size();
	stream.writeInt(this.id);
	stream.writeInt(size);
	for (int i=0; i<size; i++) {
		ConfigResults configResults = (ConfigResults) this.children.get(i);
		configResults.write(stream);
	}
}

}
