/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.eclipse.refactoring.core.participation;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.IParticipantDescriptorFilter;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * @author Ajay Singh Rathore
 * @created Dec 15, 2009
 *
 */

public class GroovyParticipantManager {
	
	private static final String RENAME_PARTICIPANT_EXT_POINT= "renameParticipants"; //$NON-NLS-1$
	private static GroovyParticipantExtensionPoint fgRenameInstance=
		new GroovyParticipantExtensionPoint(RefactoringCorePlugin.getPluginId(), RENAME_PARTICIPANT_EXT_POINT, RenameParticipant.class);

	
	public static RenameParticipant[] loadRenameParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, RenameArguments arguments, String[] affectedNatures, IParticipantDescriptorFilter filter, GroovySharableParticipants shared) {
		RefactoringParticipant[] participants= fgRenameInstance.getParticipants(status, processor, element, arguments, filter, affectedNatures, shared);
		RenameParticipant[] result= new RenameParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

}