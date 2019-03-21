/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

@SuppressWarnings("rawtypes")
public class CompilerStats implements Comparable {

	// overall
	public long startTime;
	public long endTime;
	public long lineCount;

	// compile phases
	public long parseTime;
	public long resolveTime;
	public long analyzeTime;
	public long generateTime;

/**
 * Returns the total elapsed time (between start and end)
 * @return the time spent between start and end
 */
public long elapsedTime() {
	return this.endTime - this.startTime;
}

@Override
public int compareTo(Object o) {
	CompilerStats otherStats = (CompilerStats) o;
	long time1 = elapsedTime();
	long time2 = otherStats.elapsedTime();
	return time1 < time2 ? -1 : (time1 == time2 ? 0 : 1);
}
}
