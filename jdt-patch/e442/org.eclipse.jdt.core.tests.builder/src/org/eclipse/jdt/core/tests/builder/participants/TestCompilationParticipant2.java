/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder.participants;

import org.eclipse.jdt.core.compiler.CompilationParticipant;

/**
 * Participant without dependency on other participants
 */
public class TestCompilationParticipant2 extends ParticipantBase {
	public static CompilationParticipant PARTICIPANT;

	@Override
	protected CompilationParticipant getParticipant() {
		return PARTICIPANT;
	}
}
