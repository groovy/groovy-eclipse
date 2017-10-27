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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;

public class RecoveredModuleReference extends RecoveredElement {

	public ModuleReference moduleReference;
public RecoveredModuleReference(ModuleReference moduleReference, RecoveredElement parent, int bracketBalance){
	super(parent, bracketBalance);
	this.moduleReference = moduleReference;
}
/*
 * Answer the associated parsed structure
 */
public ASTNode parseTree(){
	return this.moduleReference;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.moduleReference.sourceEnd;
}
public String toString(int tab) {
	return tabString(tab) + "Recovered ModuleReference: " + this.moduleReference.toString(); //$NON-NLS-1$
}
public ModuleReference updatedModuleReference(){

	return this.moduleReference;
}
public void updateParseTree(){
	updatedModuleReference();
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
//public void updateSourceEndIfNecessary(int bodyStart, int bodyEnd){
//	if (this.moduleReference.declarationSourceEnd == 0) {
//		this.moduleReference.declarationSourceEnd = bodyEnd;
//		this.moduleReference.declarationEnd = bodyEnd;
//	}
//}

}
