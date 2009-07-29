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
package examples.local;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
* This is just a marker interface that will trigger a local transformation. 
* The 3rd Annotation down is the important one: @GroovyASTTransformationClass
* The parameter is the String form of a fully qualified class name. 
*
* @author Hamlet D'Arcy
*/ 
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@GroovyASTTransformationClass({"examples.local.LoggingASTTransformation"})
public @interface WithLogging {
}
