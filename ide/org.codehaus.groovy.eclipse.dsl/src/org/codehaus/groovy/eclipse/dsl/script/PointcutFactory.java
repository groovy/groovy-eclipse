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
package org.codehaus.groovy.eclipse.dsl.script;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.FinalPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PrivatePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PublicPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.StaticPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.SynchronizedPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AnnotatedByPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.BindPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingCallNamePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingCallReturnTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClassPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClosurePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingMethodPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingScriptPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileExtensionPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindMethodPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindPropertyPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.NamePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.NotPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.OrPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.ProjectNaturePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.SourceFolderOfFilePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.SourceFolderOfTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.UserExtensiblePointcut;
import org.eclipse.core.resources.IProject;

/**
 * Generates {@link IPointcut} objects
 * @author andrew
 * @created Feb 11, 2011
 */
public class PointcutFactory {

    private static final Map<String, Class<? extends IPointcut>> registry = new HashMap<String, Class<? extends IPointcut>>();
    private static final Map<String, String> docRegistry    = new HashMap<String, String>();
    static {
        // combinatorial pointcuts
        registerGlobalPointcut("and", AndPointcut.class, "The exclusive combination of two or more pointcuts.  The 'and'" +
        		" pointcut matches when all containing pointcuts match.  Also, the bindings of all containing pointcuts " +
        		"are combined.\n\nArguments: this pointcut takes one or more pointcuts as arguments\n\nReturn: this " +
        		"pointcut returns the value of the last pointcut in the list");
        registerGlobalPointcut("or", OrPointcut.class, "The non-exclusive combination of two or more pointcuts.  The 'or'" +
                " pointcut matches when any containing pointcut matches.  Also, the bindings of all containing pointcuts " +
                "are combined.  All contained pointcuts are always evaluated to ensure that their bindings are correctly resolved.\n\n" +
                "Arguments: this pointcut takes one or more pointcuts as arguments\n\nReturn: this " +
                "pointcut returns the value of the last matched pointcut in the list");
        registerGlobalPointcut("not", NotPointcut.class, "Negates the match of the contained pointcut.\n\nArguments: a single pointcut\n\nReturns: If the contained pointcut is not matched, then the return value is new Object(), or else it is null");

        // binding pointcuts
        registerGlobalPointcut("bind", BindPointcut.class, "Adds a named binding for the contained pointcut\n\nArguments: a single pointcut\n\nReturns: the return value of the contained pointcut");

        // semantic pointcuts
        registerGlobalPointcut("currentType", CurrentTypePointcut.class, "Attempts to match on the declared type of the current expression.\n\nArgument: A String, Class, or ClassNode to match against.  Alternatively, another pointcut can be passed in to match against\n\nReturns: the current type as a ClassNode if there is a match with the argument");

        // filtering pointcuts
        registerGlobalPointcut("annotatedBy", AnnotatedByPointcut.class, "Matches when the containing pointcut passes in an AnnotatedNode that is annotated " +
        		"by the argument in this pointcut.\n\nArgument: A String, Class, or ClassNode corresponding to an annotation\n\nReturns: the thing or things " +
        		"annotated by the given annotation.  Eg- If the surrounding pointcut passes in a single field, then the value returned will be that field (if " +
        		"the annotation matches), or null.  If the surrounding pointcut passes in a list of AnnotatedNodes, thne the result will be a sublist of those " +
        		"annotated nodes containing only nodes with the correct annotation.");
        registerGlobalPointcut("findField", FindFieldPointcut.class, "Matches when the containing pointcut passes in a type that has a field " +
                "specified by the argument in this pointcut.\n\nArgument: A String corresponding to a field name.  Alternatively, a pointcut, such as annotatedBy, wich " +
                "would return all fields with the given annotation" +
                "\n\nReturns: the field or fields matched by the argument. " +
                "Eg- If the surrounding pointcut passes in a type, then the value returned will be all fields in that type that match the contained pointcut, or that have the specified name." +
                "  If the surrounding pointcut passes in a list of AnnotatedNodes, thne the result will be a sublist of those " +
                "annotated nodes containing only nodes with the correct annotation.");
        registerGlobalPointcut("findMethod", FindMethodPointcut.class, null);
        registerGlobalPointcut("findProperty", FindPropertyPointcut.class, null);
        registerGlobalPointcut("name", NamePointcut.class, null);
        registerGlobalPointcut("isFinal", FinalPointcut.class, null);
        registerGlobalPointcut("isPrivate", PrivatePointcut.class, null);
        registerGlobalPointcut("isPublic", PublicPointcut.class, null);
        registerGlobalPointcut("isStatic", StaticPointcut.class, null);
        registerGlobalPointcut("isSynchronized", SynchronizedPointcut.class, null);
        registerGlobalPointcut("sourceFolderOfCurrentType", SourceFolderOfTypePointcut.class, null);
        
        // lexical pointcuts
        registerGlobalPointcut("enclosingClass", EnclosingClassPointcut.class, null);
        registerGlobalPointcut("isClass", EnclosingClassPointcut.class, null);  // synonym
        registerGlobalPointcut("enclosingScript", EnclosingScriptPointcut.class, null);
        registerGlobalPointcut("isScript", EnclosingScriptPointcut.class, null);  // synonym
        registerGlobalPointcut("enclosingField", EnclosingFieldPointcut.class, null);
        registerGlobalPointcut("enclosingMethod", EnclosingMethodPointcut.class, null);
        registerGlobalPointcut("enclosingCallName", EnclosingCallNamePointcut.class, null);
        registerGlobalPointcut("enclosingCallReturnType", EnclosingCallReturnTypePointcut.class, null);
        registerGlobalPointcut("enclosingClosure", EnclosingClosurePointcut.class, null);
        registerGlobalPointcut("inClosure", EnclosingClosurePointcut.class, null);  // synonym
        
        // structural pointcuts
        registerGlobalPointcut("fileExtension", FileExtensionPointcut.class, null);
        registerGlobalPointcut("nature", ProjectNaturePointcut.class, null);
        registerGlobalPointcut("sourceFolderOfCurrentFile", SourceFolderOfFilePointcut.class, null);
    }
    
    @SuppressWarnings("rawtypes")
    private final Map<String, Closure> localRegistry = new HashMap<String, Closure>();

    private final String uniqueID;

    private final IProject project;
    
    public PointcutFactory(String uniqueID) {
        this.uniqueID = uniqueID;
        this.project = null;
    }
    
    public PointcutFactory(String uniqueID, IProject project) {
        this.uniqueID = uniqueID;
        this.project = project;
    }
    
    
    private static void registerGlobalPointcut(String name, Class<? extends IPointcut> pcClazz, String doc) {
        registry.put(name, pcClazz);
        docRegistry.put(name, doc);
    }
    
    

    
    public void registerLocalPointcut(String name, @SuppressWarnings("rawtypes") Closure c) {
        localRegistry.put(name, c);
    }
    
    /**
     * creates a pointcut of the given name, or returns null if not registered
     * @param name
     * @return
     */
    public IPointcut createPointcut(String name) {
        @SuppressWarnings("rawtypes")
        Closure c = localRegistry.get(name);
        if (c != null) {
            return new UserExtensiblePointcut(uniqueID, c);
        } 
        
        Class<? extends IPointcut> pc = registry.get(name);
        if (pc != null) {
            try {
                // try the one arg constructor and the no-arg constructor
                try {
                    IPointcut p = pc.getConstructor(String.class).newInstance(uniqueID);
                    p.setProject(project);
                    return p;
                } catch (NoSuchMethodException e) {
                    IPointcut p = pc.getConstructor(String.class).newInstance();
                    if (p instanceof AbstractPointcut) {
                        ((AbstractPointcut) p).setContainerIdentifier(uniqueID);
                        p.setProject(project);
                        return p;
                    }
                    throw e;
                }
            } catch (Exception e) {
                GroovyDSLCoreActivator.logException(e);
            }
        }
        return null;
    }
}
