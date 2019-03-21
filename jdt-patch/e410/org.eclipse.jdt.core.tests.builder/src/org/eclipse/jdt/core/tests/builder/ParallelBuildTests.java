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

import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParallelBuildTests extends BuilderTests {

	private int initialMaxParallelBuilds;

	public ParallelBuildTests(String name) {
		super(name);
	}

	public static junit.framework.Test suite() {
		return buildTestSuite(ParallelBuildTests.class);
	}

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TimeStampBuilder.clear();
		this.initialMaxParallelBuilds = env.getWorkspace().getDescription().getMaxConcurrentBuilds();
		InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID).putBoolean(JavaBuilder.PREF_NULL_SCHEDULING_RULE, true);
	}

	@After
	@Override
	protected void tearDown() throws Exception {
		setMaxParallelBuilds(this.initialMaxParallelBuilds);
		InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID).remove(JavaBuilder.PREF_NULL_SCHEDULING_RULE);
		super.tearDown();
	}

	@Test
	public void testJDTBuildAllowParallelBuildsForOtherProjects() throws CoreException {
		IProject javaProject1 = env.getProject(env.addProject(getClass().getSimpleName() + "_javaProject1"));
		addBuilder(javaProject1.getProject());
		//
		IProject javaProject2 = env.getProject(env.addProject(getClass().getSimpleName() + "_javaProject2"));
		env.addRequiredProject(javaProject2.getFullPath(), javaProject1.getFullPath());
		addBuilder(javaProject2.getProject());
		//
		IProject rawProject1 = env.getWorkspace().getRoot().getProject(getClass().getSimpleName() + System.currentTimeMillis());
		rawProject1.create(new NullProgressMonitor());
		rawProject1.open(new NullProgressMonitor());
		addBuilder(rawProject1);
		IProject rawProject2 = env.getWorkspace().getRoot().getProject(getClass().getSimpleName() + System.currentTimeMillis());
		rawProject2.create(new NullProgressMonitor());
		rawProject2.open(new NullProgressMonitor());
		addBuilder(rawProject2);
		setMaxParallelBuilds(3);

		env.fullBuild();
		// verify that rawProject could build in parallel
		assertTrue(TimeStampBuilder.start(rawProject1) < TimeStampBuilder.end(rawProject2));
		assertTrue(TimeStampBuilder.start(rawProject2) < TimeStampBuilder.end(rawProject1));
	}

	private void setMaxParallelBuilds(int n) throws CoreException {
		IWorkspaceDescription desc = env.getWorkspace().getDescription();
		desc.setMaxConcurrentBuilds(n);
		env.getWorkspace().setDescription(desc);
	}

	private void addBuilder(IProject project) throws CoreException {
		IProjectDescription desc1 = project.getDescription();
		ICommand[] newCommands = new ICommand[desc1.getBuildSpec().length + 1];
		System.arraycopy(desc1.getBuildSpec(), 0, newCommands, 0, desc1.getBuildSpec().length);
		BuildCommand builderCommand = new BuildCommand();
		builderCommand.setBuilderName(TimeStampBuilder.ID);
		builderCommand.setArguments(Collections.singletonMap(TimeStampBuilder.PAUSE_DURATION, Integer.toString(1000)));
		newCommands[newCommands.length - 1] = builderCommand;
		desc1.setBuildSpec(newCommands);
		project.setDescription(desc1, new NullProgressMonitor());
	}
}
