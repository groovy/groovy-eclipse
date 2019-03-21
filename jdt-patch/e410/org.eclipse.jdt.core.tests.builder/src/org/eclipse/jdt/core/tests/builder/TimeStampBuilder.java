/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class TimeStampBuilder extends IncrementalProjectBuilder {

	public static final String ID = TimeStampBuilder.class.getName();
	public static final String PAUSE_DURATION = "pauseDuration";
	private static final Map<IProject, Long> starts = Collections.synchronizedMap(new HashMap<>());
	private static final Map<IProject, Long> ends = Collections.synchronizedMap(new HashMap<>());

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		starts.put(getProject(), Long.valueOf(System.currentTimeMillis()));
		try {
			int pauseDuration = 1000;
			if (args.containsKey(PAUSE_DURATION)) {
				pauseDuration = Integer.parseInt(args.get(PAUSE_DURATION));
			}
			Thread.sleep(pauseDuration);
			ends.put(getProject(), Long.valueOf(System.currentTimeMillis()));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new IProject[0];
	}

	@Override
	public ISchedulingRule getRule(int kind, Map<String, String> args) {
		return null;
	}

	public static long end(IProject project) throws CoreException {
		return ends.get(project).longValue();
	}

	public static long start(IProject project) throws CoreException {
		return starts.get(project).longValue();
	}

	public static void clear() {
		starts.clear();
		ends.clear();
	}

}
