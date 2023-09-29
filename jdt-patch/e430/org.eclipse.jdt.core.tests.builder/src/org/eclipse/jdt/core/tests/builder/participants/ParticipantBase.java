/*******************************************************************************
 * Copyright (c) 2000, 2023 Advantest Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Advantest Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder.participants;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

/**
 * Base class for compilation participants used in tests.
 *
 * Tests that require {@link CompilationParticipant} to be active during test
 * should provide appropriate {@link #getParticipant()} implementation.
 */
public abstract class ParticipantBase extends CompilationParticipant {

	/**
	 * @return <b>Note:</b> implementation class should return {@code null}
	 *         after the test is finished, otherwise participant will be active
	 *         during unrelated tests!
	 */
	abstract protected CompilationParticipant getParticipant();

	public int aboutToBuild(IJavaProject project) {
		return getParticipant().aboutToBuild(project);
	}

	public void buildFinished(IJavaProject project) {
		getParticipant().buildFinished(project);
	}

	public void buildStarting(BuildContext[] files, boolean isBatchBuild) {
		getParticipant().buildStarting(files, isBatchBuild);
	}

	public void cleanStarting(IJavaProject project) {
		getParticipant().cleanStarting(project);
	}

	public boolean isActive(IJavaProject project) {
		return getParticipant() != null;
	}

	public boolean isAnnotationProcessor() {
		return getParticipant() != null && getParticipant().isAnnotationProcessor();
	}

	public void processAnnotations(BuildContext[] files) {
		getParticipant().processAnnotations(files);
	}

	public Optional<byte[]> postProcess(BuildContext file, ByteArrayInputStream bytes) {
		return getParticipant().postProcess(file, bytes);
	}

	@Override
	public boolean isPostProcessor() {
		return getParticipant().isPostProcessor();
	}
}
