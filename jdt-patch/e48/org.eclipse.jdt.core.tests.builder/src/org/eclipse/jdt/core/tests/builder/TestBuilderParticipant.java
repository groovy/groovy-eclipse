/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.*;

public class TestBuilderParticipant extends CompilationParticipant {

	public static CompilationParticipant PARTICIPANT;

public int aboutToBuild(IJavaProject project) {
	return PARTICIPANT.aboutToBuild(project);
}

public void buildFinished(IJavaProject project) {
	PARTICIPANT.buildFinished(project);
}

public void buildStarting(BuildContext[] files, boolean isBatchBuild) {
	PARTICIPANT.buildStarting(files, isBatchBuild);
}

public void cleanStarting(IJavaProject project) {
	PARTICIPANT.cleanStarting(project);
}

public boolean isActive(IJavaProject project) {
	return PARTICIPANT != null;
}

public boolean isAnnotationProcessor() {
	return PARTICIPANT != null && PARTICIPANT.isAnnotationProcessor();
}

public void processAnnotations(BuildContext[] files) {
	PARTICIPANT.processAnnotations(files);
}
}
