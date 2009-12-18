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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.internal.core.refactoring.ParticipantDescriptor;

/**
 * @author Ajay Singh Rathore
 * @created Dec 15, 2009
 *
 */

public class GroovySharableParticipants {

	private Map fMap= new HashMap();

	/* package */ void put(ParticipantDescriptor descriptor, RefactoringParticipant participant) {
		fMap.put(descriptor, participant);
	}
	/* package */ RefactoringParticipant get(ParticipantDescriptor descriptor) {
		return (RefactoringParticipant)fMap.get(descriptor);
	}
}
