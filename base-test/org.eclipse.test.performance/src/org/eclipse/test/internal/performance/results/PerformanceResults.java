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
import java.io.PrintStream;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Root class to handle performance results.
 * 
 * Usually performance results are built for a current build vs. a baseline build.
 * 
 * This class allow to read all data from releng performance database for given
 * configurations and scenario pattern.
 * 
 * Then it provides easy and speedy access to all stored results.
 */
public class PerformanceResults extends AbstractResults {

	String baselineName; // Name of the baseline build used for comparison
	String baselinePrefix;
	private String scenarioPattern;
	private String[] components;
	String[] configNames, sortedConfigNames;
	String[] configBoxes, sortedConfigBoxes;
	private String configPattern;
	String updatedName = null;

public static PerformanceResults createPerformanceResults(String scenarioPattern, File dataDir, PrintStream stream, IProgressMonitor monitor) {
	String[] builds = DB_Results.getBuilds(); // Init build names
	if (DB_Results.LAST_CURRENT_BUILD == null) {
		System.err.println("Could not find the last current build amongst the following builds list:");	//$NON-NLS-1$
		int length = builds.length;
		for (int i=0; i<length; i++) {
			System.err.println("	- "+builds[i]); //$NON-NLS-1$
		}
		return null;
	}
	if (DB_Results.LAST_BASELINE_BUILD == null) {
		System.err.println("Could not find the last baseline build amongst the following builds list:");	//$NON-NLS-1$
		int length = builds.length;
		for (int i=0; i<length; i++) {
			System.err.println("	- "+builds[i]); //$NON-NLS-1$
		}
		return null;
	}
	PerformanceResults performanceResults = new PerformanceResults(DB_Results.LAST_CURRENT_BUILD, DB_Results.LAST_BASELINE_BUILD, stream);
	performanceResults.updatedName = DB_Results.LAST_CURRENT_BUILD; // allow name to be updated
	performanceResults.read(null, scenarioPattern, dataDir, DEFAULT_FAILURE_THRESHOLD, monitor);
	return performanceResults;
}

	// Failure threshold
	public static final int DEFAULT_FAILURE_THRESHOLD = 10;
	int failure_threshold = DEFAULT_FAILURE_THRESHOLD;

public PerformanceResults(String name, String baseline, PrintStream stream) {
	super(null, name);
	this.baselineName = baseline;
	if (baseline != null) {
		this.baselinePrefix = baseline.substring(0, baseline.lastIndexOf('_'));
	}
	this.printStream = stream;
}

boolean canUpdateName() {
	return this.updatedName != null;
}

/**
 * Returns the name of the baseline used for extracted results
 * 
 * @return The build name of the baseline of <code>null</code>
 * 	if no specific baseline is used for the extracted results.
 */
public String getBaselineName() {
	return this.baselineName;
}

/*
 * Get the baseline prefix (computed from #baselineName).
 */
String getBaselinePrefix() {
	return this.baselinePrefix;
}

/*
 * Get the build date (see #getBuildDate(String, String)).
 */
public String getBuildDate() {
	return getBuildDate(getName(), this.baselinePrefix);
}

/**
 * Return the list of components concerned by performance results.
 * 
 * @return The list of the components
 */
public String[] getComponents() {
	return this.components;
}

/**
 * Get the scenarios of a given component.
 *
 * @param componentName The component name. Should not be <code>null</code>
 * @return A list of {@link ScenarioResults scenario results}
 */
public List getComponentScenarios(String componentName) {
	ComponentResults componentResults = (ComponentResults) getResults(componentName);
	if (componentResults == null) return null;
	return Collections.unmodifiableList(componentResults.children);
}

/**
 * Get the scenarios which have a summary for a given component.
 *
 * @param componentName The component name
 * @param config Configuration name
 * @return A list of {@link ScenarioResults scenario results} which have a summary
 */
public List getComponentSummaryScenarios(String componentName, String config) {
	if (componentName == null) {
		int size = size();
		List scenarios = new ArrayList();
		for (int i=0; i< size; i++) {
			ComponentResults componentResults = (ComponentResults) this.children.get(i);
			scenarios.addAll(componentResults.getSummaryScenarios(true, config));
		}
		return scenarios;
	}
	ComponentResults componentResults = (ComponentResults) getResults(componentName);
	return componentResults.getSummaryScenarios(false, config);
}

/**
 * Return the configuration boxes considered for this performance results
 * sorted or not depending on the given flag.
 * 
 * @param sort Indicates whether the list must be sorted or not.
 * 	The order is defined by the configuration names, not by the box names
 * @return The list of configuration boxes sorted by configuration names
 */
public String[] getConfigBoxes(boolean sort) {
	return sort ? this.sortedConfigBoxes : this.configBoxes;
}

/**
 * Return the configuration names considered for this performance results
 * sorted or not depending on the given flag.
 * 
 * @param sort Indicates whether the list must be sorted or not
 * @return The list of configuration names
 */
public String[] getConfigNames(boolean sort) {
	return sort ?this.sortedConfigNames : this.configNames;
}

/*
 * Compute a SQL pattern from all stored configuration names.
 * For example 'eclipseperflnx1', 'eclipseperflnx2' and 'eclipseperflnx3'
 * will return 'eclipseperflnx_'.
 */
String getConfigurationsPattern() {
	if (this.configPattern == null) {
		int length = this.sortedConfigNames == null ? 0 : this.sortedConfigNames.length;
		if (length == 0) return null;
		this.configPattern = this.sortedConfigNames[0];
		int refLength = this.configPattern.length();
		for (int i=1; i<length; i++) {
			String config = this.sortedConfigNames[i];
			StringBuffer newConfig = null;
			if (refLength != config.length()) return null; // strings have not the same length => cannot find a pattern
			for (int j=0; j<refLength; j++) {
				char c = this.configPattern.charAt(j);
				if (config.charAt(j) != c) {
					if (newConfig == null) {
						newConfig = new StringBuffer(refLength);
						if (j == 0) return null; // first char is already different => cannot find a pattern
						newConfig.append(this.configPattern.substring(0, j));
					}
					newConfig.append('_');
				} else if (newConfig != null) {
					newConfig.append(c);
				}
			}
			if (newConfig != null) {
				this.configPattern = newConfig.toString();
			}
		}
	}
	return this.configPattern;
}

public String getName() {
	if (this.updatedName != null) return this.updatedName;
	return this.name;
}

/*
 * (non-Javadoc)
 * @see org.eclipse.test.internal.performance.results.AbstractResults#getPerformance()
 */
PerformanceResults getPerformance() {
	return this;
}

/**
 * Get the results of a given scenario.
 *
 * @param scenarioName The scenario name
 * @return The {@link ScenarioResults scenario results}
 */
public ScenarioResults getScenarioResults(String scenarioName) {
	ComponentResults componentResults = (ComponentResults) getResults(DB_Results.getComponentNameFromScenario(scenarioName));
	return componentResults == null ? null : (ScenarioResults) componentResults.getResults(scenarioName);
}

/**
 * Read all data from performance database for the given configurations
 * and scenario pattern.
 * 
 * @param dataDir The directory where data will be stored locally
 * 	if <code>null</code>, then storage will be performed
 */
public void read(File dataDir) {
	read(null, null, dataDir, DEFAULT_FAILURE_THRESHOLD, null);
}

/**
 * Read all data from performance database for the given configurations
 * and scenario pattern.
 * 
 * @param configs All configurations to extract results. If <code>null</code>,
 * 	then all known configurations ({@link #CONFIGS})  are read.
 * @param pattern The pattern of the concerned scenarios
 * @param dataDir The directory where data will be read/stored locally.
 * 	If <code>null</code>, then database will be read instead and no storage
 * 	will be performed
 * @param threshold The failure percentage threshold over which a build result
 * 	value compared to the baseline is considered as failing.
 * @param monitor The progress monitor
 */
public void read(String[][] configs, String pattern, File dataDir, int threshold, IProgressMonitor monitor) {

	try {
		this.scenarioPattern = pattern == null ? DB_Results.DEFAULT_SCENARIO_PATTERN : pattern;
		this.failure_threshold = threshold;
	
		// Print title
		StringBuffer buffer = new StringBuffer("Read performance results until build '"); //$NON-NLS-1$
		buffer.append(this.name);
		String taskName = buffer.toString();
		if (monitor != null) {
			monitor.setTaskName(taskName);
			monitor.worked(1);
			if (monitor.isCanceled()) return;
		}
		if (scenarioPattern == null) {
			buffer.append("':"); //$NON-NLS-1$
		} else {
			buffer.append("' using scenario pattern '"); //$NON-NLS-1$
			buffer.append(scenarioPattern);
			buffer.append("':"); //$NON-NLS-1$
		}
		println(buffer);
	
		// Store given configs
		if (configs == null) {
			int length=CONFIGS.length;
			this.configNames = new String[length];
			this.sortedConfigNames = new String[length];
			this.configBoxes = new String[length];
			for (int i=0; i<length; i++) {
				this.configNames[i] = this.sortedConfigNames[i] = CONFIGS[i];
				this.configBoxes[i] = BOXES[i];
			}
		} else {
			int length = configs.length;
			this.configNames = new String[length];
			this.sortedConfigNames = new String[length];
			this.configBoxes = new String[length];
			for (int i=0; i<length; i++) {
				this.configNames[i] = this.sortedConfigNames[i] = configs[i][0];
				this.configBoxes[i] = configs[i][1];
			}
		}
		Arrays.sort(this.sortedConfigNames);
		int length = this.sortedConfigNames.length;
		this.sortedConfigBoxes = new String[length];
		for (int i=0; i<length; i++) {
			for (int j=0; j<length; j++) {
				if (this.sortedConfigNames[i] == this.configNames[j]) { // == is intentional!
					this.sortedConfigBoxes[i] = this.configBoxes[j];
					break;
				}
			}
		}
	
		// Get scenarios from the given pattern
		print("	+ get corresponding scenarios for build: "+this.name); //$NON-NLS-1$
		if (monitor != null) monitor.subTask("Get all scenarios for build "+this.name+"..."); //$NON-NLS-1$ //$NON-NLS-2$
		long start = System.currentTimeMillis();
		Map allScenarios = DB_Results.queryAllScenarios(this.scenarioPattern);
		println(" -> "+allScenarios.size()+" found in "+(System.currentTimeMillis()-start)+"ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (monitor != null) {
			monitor.worked(99);
			if (monitor.isCanceled()) return;
		}

		// Create corresponding children
		String[] allComponents = DB_Results.getComponents();
		int cLength= allComponents.length;
		this.components = new String[cLength];
		int count = 0;
		int progress = 100, step = 900 / cLength;
		for (int i=0; i<cLength; i++) {
			String componentName = allComponents[i];
			List scenarios = (List) allScenarios.get(componentName);
			if (monitor != null) {
				int percentage = (int) ((progress / 1000.0) * 100);
				monitor.setTaskName(taskName+" ("+percentage+"%)"); //$NON-NLS-1$ //$NON-NLS-2$
				monitor.subTask("Component "+componentName+"..."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (scenarios == null) continue;
			this.components[count++] = componentName;
			ComponentResults componentResults = new ComponentResults(this, componentName);
			addChild(componentResults, true);
			componentResults.read(scenarios, dataDir);
			if (monitor != null) {
				monitor.worked(step);
				if (monitor.isCanceled()) return;
				progress += step;
			}
		}
		if (count < cLength) {
			System.arraycopy(this.components, 0, this.components = new String[count], 0, count);
		}
	
		// Print time
		printGlobalTime(start);
	}
	finally {
		// Shutdown database
		DB_Results.shutdown();
	}
}
}
