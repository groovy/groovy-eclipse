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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle performance results of an eclipse component
 * (for example 'org.eclipse.jdt.core').
 * 
 * It gives access to results for each scenario run for this component.
 * 
 * @see ScenarioResults
 */
public class ComponentResults extends AbstractResults {

public ComponentResults(AbstractResults parent, String name) {
	super(parent, name);
	this.printStream = parent.printStream;
}

ComponentResults getComponentResults() {
	return this;
}

private ScenarioResults getScenarioResults(List scenarios, int searchedId) {
	int size = scenarios.size();
	for (int i=0; i<size; i++) {
		ScenarioResults scenarioResults = (ScenarioResults) scenarios.get(i);
		if (scenarioResults.id == searchedId) {
			return scenarioResults;
		}
	}
	return null;
}

/**
 * Returns a list of scenario results which have a summary
 * 
 * @param global Indicates whether the summary must be global or not.
 * @param config Configuration name
 * @return A list of {@link ScenarioResults scenario results} which have a summary
 */
public List getSummaryScenarios(boolean global, String config) {
	int size= size();
	List scenarios = new ArrayList(size);
	for (int i=0; i<size; i++) {
		ScenarioResults scenarioResults = (ScenarioResults) this.children.get(i);
		ConfigResults configResults = scenarioResults.getConfigResults(config);
		if (configResults != null) {
			BuildResults buildResults = configResults.getCurrentBuildResults();
			if ((global && buildResults.summaryKind == 1) || (!global && buildResults.summaryKind >= 0)) {
				scenarios.add(scenarioResults);
			}
		}
	}
	return scenarios;
}

/*
 * Read performance results information of the given scenarios.
 * First try to read local data if given directory is not null.
 */
void read(List scenarios, File dataDir) {
	println("Component '"+this.name+"':"); //$NON-NLS-1$ //$NON-NLS-2$
	long start = System.currentTimeMillis();
	boolean dirty = false;
	if (dataDir != null) {
		try {
	        dirty = readData(dataDir, scenarios);
        } catch (IOException e) {
	        e.printStackTrace();
        }
	}
	int size = scenarios.size();
	long time = System.currentTimeMillis();
	boolean first = true;
	for (int i=0; i<size; i++) {
		ScenarioResults scenarioResults= (ScenarioResults) scenarios.get(i);
		if (scenarioResults.parent == null) {
			if (first) {
				println(" - read new scenarios:"); //$NON-NLS-1$
				first = false;
			}
			scenarioResults.parent = this;
			scenarioResults.printStream = this.printStream;
			scenarioResults.read();
			dirty = true;
			addChild(scenarioResults, true);
		}
		if (dataDir != null && dirty && (System.currentTimeMillis() - time) > 300000) { // save every 5mn
			writeData(dataDir, true, true);
			time = System.currentTimeMillis();
			dirty = false;
		}
	}
	if (dataDir != null) {
		writeData(dataDir, false, dirty);
	}
	printGlobalTime(start);
}

/*
 * Read the data stored locally in the given directory for all given scenarios.
 */
boolean readData(File dir, List scenarios) throws IOException {
	if (!dir.exists()) return true;
	File dataFile = new File(dir, getName()+".dat");	//$NON-NLS-1$
	if (!dataFile.exists()) return true;
	PerformanceResults performanceResults = getPerformance();
	DB_Results.queryAllVariations(performanceResults.getConfigurationsPattern());
	DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(dataFile)));
	boolean valid = false, dirty = false;
	int size = 0;
	try {
		// Read local file info
		print(" - read local files info"); //$NON-NLS-1$
		String lastBuildName = stream.readUTF(); // first string is the build name
		
		// Update last build name if local data file has a more recent one
		String lastBuildDate = getBuildDate(lastBuildName);
		if (performanceResults.canUpdateName() && lastBuildDate.compareTo(performanceResults.getBuildDate()) > 0) {
			performanceResults.updatedName = lastBuildName;
		}

		// Next field is the number of scenarios for the component
		size = stream.readInt();

		// Then follows all the scenario information
		for (int i=0; i<size; i++) {
			// ... which starts with the scenario id
			int scenario_id = stream.readInt();
			ScenarioResults scenarioResults = getScenarioResults(scenarios, scenario_id);
			if (scenarioResults == null) {
				// this can happen if scenario pattern does not cover all those stored in local data file
				// hence, creates a fake scenario to read the numbers and skip to the next scenario
				scenarioResults = new ScenarioResults(-1, null, null);
				scenarioResults.parent = this;
				scenarioResults.readData(stream);
			} else {
				scenarioResults.parent = this;
				scenarioResults.printStream = this.printStream;
				scenarioResults.readData(stream);
				addChild(scenarioResults, true);
			}
			if (this.printStream != null) this.printStream.print('.');
		}
		println(""); //$NON-NLS-1$

		// Read new values for the local result
		boolean first = true;
		long readTime = System.currentTimeMillis();
		size = size();
		for (int i=0; i<size; i++) {
			ScenarioResults scenarioResults = (ScenarioResults) this.children.get(i);
			if (first) {
				println(" - read DB contents:"); //$NON-NLS-1$
				first = false;
			}
			long start = System.currentTimeMillis();
			boolean newData = scenarioResults.readNewData(lastBuildName);
			long time = System.currentTimeMillis()-start;
			if (newData) {
				dirty = true;
				print(", infos..."); //$NON-NLS-1$
				start = System.currentTimeMillis();
				scenarioResults.completeResults(lastBuildName);
				time = System.currentTimeMillis()-start;
				println(timeString(time));
			}
			if (dirty && (System.currentTimeMillis() - readTime) > 300000) { // save every 5mn
				writeData(dir, true, true);
				readTime = System.currentTimeMillis();
				dirty = false;
			}
		}
		valid = true;
	} finally {
		stream.close();
		if (valid) {
			println("	=> "+size+" scenarios data were read from file "+dataFile); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			dataFile.delete();
			println("	=> deleted file "+dataFile+" as it contained invalid data!!!"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	return dirty;
}

/*
 * Write the component results data to the file '<component name>.dat' in the given directory.
 */
void writeData(File dir, boolean temp, boolean dirty) {
	if (!dir.exists() && !dir.mkdirs()) {
		System.err.println("can't create directory "+dir); //$NON-NLS-1$
	}
	File tmpFile = new File(dir, getName()+".tmp"); //$NON-NLS-1$
	File dataFile = new File(dir, getName()+".dat"); //$NON-NLS-1$
	if (!dirty) { // only possible on final write
		if (tmpFile.exists()) {
			if (dataFile.exists()) dataFile.delete();
			tmpFile.renameTo(dataFile);
			println("	=> rename temporary file to "+dataFile); //$NON-NLS-1$
		}
		return;
	}
	if (tmpFile.exists()) {
		tmpFile.delete();
	}
	File file;
	if (temp) {
		file = tmpFile;
	} else {
		if (dataFile.exists()) {
			dataFile.delete();
		}
		file = dataFile;
	}
	try {
		DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		int size = this.children.size();
		stream.writeUTF(getPerformance().getName());
		stream.writeInt(size);
		for (int i=0; i<size; i++) {
			ScenarioResults scenarioResults = (ScenarioResults) this.children.get(i);
			scenarioResults.write(stream);
		}
		stream.close();
		println("	=> extracted data "+(temp?"temporarily ":"")+"written in file "+file); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	} catch (FileNotFoundException e) {
		System.err.println("can't create output file"+file); //$NON-NLS-1$
	} catch (IOException e) {
		e.printStackTrace();
	}
}

}
