/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import groovyjarjarasm.asm.Opcodes;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * the match returns true if the pattern passed in has a modifier of the kind specified
 * @author andrew
 * @created Feb 11, 2011
 */
public class AbstractModifierPointcut extends FilteringPointcut<AnnotatedNode> {
    
    public static class FinalPointcut extends AbstractModifierPointcut {
        public FinalPointcut(IStorage containerIdentifier, String pointcutName) {
            super(containerIdentifier, pointcutName, Opcodes.ACC_FINAL);
        }
    }

    public static class StaticPointcut extends AbstractModifierPointcut {
        public StaticPointcut(IStorage containerIdentifier, String pointcutName) {
            super(containerIdentifier, pointcutName, Opcodes.ACC_STATIC);
        }
    }
    
    public static class PublicPointcut extends AbstractModifierPointcut {
        public PublicPointcut(IStorage containerIdentifier, String pointcutName) {
            super(containerIdentifier, pointcutName, Opcodes.ACC_PUBLIC);
        }
    }
    
    public static class PrivatePointcut extends AbstractModifierPointcut {
        public PrivatePointcut(IStorage containerIdentifier, String pointcutName) {
            super(containerIdentifier, pointcutName, Opcodes.ACC_PRIVATE);
        }
    }
    
    public static class SynchronizedPointcut extends AbstractModifierPointcut {
        public SynchronizedPointcut(IStorage containerIdentifier, String pointcutName) {
            super(containerIdentifier, pointcutName, Opcodes.ACC_SYNCHRONIZED);
        }
    }
    

    private final int modifier;
    
    public AbstractModifierPointcut(IStorage containerIdentifier, String pointcutName, int modifier) {
        super(containerIdentifier, pointcutName, AnnotatedNode.class);
        this.modifier = modifier;
    }

    /**
     * filters the passed in object based on the modifier
     * @param result
     * @return
     */
    @Override
    protected AnnotatedNode filterObject(AnnotatedNode result, GroovyDSLDContext pattern, String firstArgAsString) {
        boolean success = false;
        if (result instanceof ClassNode) {
            success = (((ClassNode) result).getModifiers() & modifier) != 0;
        } else if (result instanceof FieldNode) {
            success = (((FieldNode) result).getModifiers() & modifier) != 0;
        } else if (result instanceof MethodNode) {
            success = (((MethodNode) result).getModifiers() & modifier) != 0;
        } else if (result instanceof PropertyNode) {
            success = (((PropertyNode) result).getModifiers() & modifier) != 0;
        }
        return success ? result : null;
    }
    
    @Override
    public void verify() throws PointcutVerificationException {
        if(getArgumentValues().length > 0) {
            throw new PointcutVerificationException("This pointcut does not take any arguments.", this);
        }
    }
}
