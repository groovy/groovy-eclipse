/*******************************************************************************
 * Copyright (c) 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Constants
 *     Andrew Eisenberg    - Interface methods
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.List;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * Common interface for Groovy ASTNodes backed by a JDT binding
 * 
 * @author Andrew Eisenberg
 * @created Dec 13, 2010
 */
public interface JDTNode {
	int ANNOTATIONS_INITIALIZED = 0x0001;
	int PROPERTIES_INITIALIZED = 0x0002;

	JDTResolver getResolver();

	List<AnnotationNode> getAnnotations();

	List<AnnotationNode> getAnnotations(ClassNode type);

	Binding getJdtBinding();

	boolean isDeprecated();
}
