/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - initial version
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation;

// Andys version of GrailsAwareInjectionOperation
// FIXASC ensure we have a grails nature and a way to determine grails level
/**
 * In Grails 1.1 an extra compiler phase is added to the compiler by grails. During compilation this phase operation adds properties
 * like 'identity' to domain objects, based upon whether they are in the directory grails-app/domain. Under Grails 1.2 this is a
 * global ast transform, but in order for things to work properly under 1.1, we have this injector operation. Without a grails
 * nature (which we may have shortly), the decision about whether this injector should be added to any compiler is based upon with a
 * grails-app folder exists in the project (crude, but it works). Once added it will try and access the real grails injector via
 * reflection - caching results to try and limit performance issues. If anything goes wrong it moves to a broken state and no longer
 * attempts anything. This is probably likely to happen for users on Grails 1.2 since the signature of the method will have changed.
 * For M2 we should determine what level of grails a project is and created the nature.
 * 
 * @author Andy Clement
 * @since 2.0
 */
public class GrailsInjector extends PrimaryClassNodeOperation {

	private GroovyClassLoader groovyClassLoader;

	// If true then some part of the reflection has broken down so avoid trying again
	private boolean broken = false;

	private Class<?> /* DefaultGrailsDomainClassInjector */injectorClazz;
	private Object/* DefaultGrailsDomainClassInjector */injectorInstance;
	private Method injectorMethod;

	public GrailsInjector(GroovyClassLoader groovyClassLoader) {
		this.groovyClassLoader = groovyClassLoader;
	}

	@Override
	public void call(SourceUnit sourceUnit, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
		if (broken) {
			return;
		}
		if (injectorClazz == null) {
			// Attempt to load the grails class
			try {
				injectorClazz = groovyClassLoader
						.loadClass("org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector");
				injectorInstance = injectorClazz.newInstance();
			} catch (Throwable t) {
				broken = true;
				System.err
						.println("GrailsInjector: Unable to load and create 'org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector'");
				t.printStackTrace();
				return;
			}
			try {
				// retrieve the method we want
				injectorMethod = injectorClazz.getDeclaredMethod("performInjection", new Class[] { SourceUnit.class,
						GeneratorContext.class, ClassNode.class });
			} catch (SecurityException se) {
				broken = true;
				System.err
						.println("GrailsInjector: Cannot find correct performInjection method on 'org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector'");
				se.printStackTrace();
				return;
			} catch (NoSuchMethodException nsme) {
				broken = true;
				System.err
						.println("GrailsInjector: Cannot find correct performInjection method on 'org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector'");
				nsme.printStackTrace();
				return;
			}
			if (injectorMethod == null) {
				broken = true;
				return;
			}
		}

		try {
			// Check if it should be run - rather crude check but will cover the basics I think
			if (sourceUnit.getName().indexOf("grails-app/domain/") != -1) {
				injectorMethod.invoke(injectorInstance, sourceUnit, context, classNode);
			}
		} catch (IllegalArgumentException iae) {
			System.err.println("GrailsInjector: Problem invoking performInjection");
			iae.printStackTrace();
		} catch (IllegalAccessException iae) {
			System.err.println("GrailsInjector: Problem invoking performInjection");
			iae.printStackTrace();
		} catch (InvocationTargetException e) {
			System.err.println("GrailsInjector: Problem invoking performInjection");
			e.printStackTrace();
		}
	}
}
