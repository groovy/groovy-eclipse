/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

/**
 * A requestor that is called back when GroovyCompilationUnitDeclarations are created. This enables the writing of tests that check
 * the structure of the constructed declaration. Only tests should define implementations and plug them into GroovyParser.
 * 
 * @author Andy Clement
 */
public interface IGroovyDebugRequestor {

	void acceptCompilationUnitDeclaration(GroovyCompilationUnitDeclaration gcuDeclaration);

}