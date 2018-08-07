/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

public class CompletionOnProvidesInterfacesQualifiedTypeReference extends CompletionOnQualifiedTypeReference {

	public CompletionOnProvidesInterfacesQualifiedTypeReference(char[][] previousIdentifiers, char[] completionIdentifier,
			long[] positions) {
		super(previousIdentifiers, completionIdentifier, positions);
	}

}
