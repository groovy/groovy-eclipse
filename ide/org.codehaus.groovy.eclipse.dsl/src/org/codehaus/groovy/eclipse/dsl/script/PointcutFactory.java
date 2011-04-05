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
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingCallDeclaringTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingCallNamePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClassPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClosurePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingMethodPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingScriptPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileExtensionPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileNamePointcut;
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
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.VariableExpressionPointcut;
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
        registerGlobalPointcut("and", AndPointcut.class, createDoc("The exclusive combination of two or more pointcuts.  The 'and' " +
        		"pointcut matches when all containing pointcuts match.  Also, the bindings of all containing pointcuts " +
        		"are combined.", 
        		"this pointcut takes one or more pointcuts as arguments", 
        		"this pointcut returns the value of the last pointcut in the list"));
        registerGlobalPointcut("or", OrPointcut.class, createDoc("The non-exclusive combination of two or more pointcuts.  The 'or'" +
                " pointcut matches when any containing pointcut matches.  Also, the bindings of all containing pointcuts " +
                "are combined.  All contained pointcuts are always evaluated to ensure that their bindings are correctly resolved.",
                "this pointcut takes one or more pointcuts as arguments", 
                "this pointcut returns the value of the last matched pointcut in the list"));
        registerGlobalPointcut("not", NotPointcut.class, createDoc("Negates the match of the contained pointcut.", "a single pointcut", 
                "If the contained pointcut is not matched, then the return value is new Object(), or else it is null"));

        // binding pointcuts
        registerGlobalPointcut("bind", BindPointcut.class, createDoc("Adds a named binding for the contained pointcut.", "a single pointcut", 
                "the return value of the contained pointcut"));

        // semantic pointcuts
        registerGlobalPointcut("currentType", CurrentTypePointcut.class, 
                createDoc("Attempts to match on the declared type of the current expression.", 
                "A String, Class, or ClassNode to match against.  Alternatively, another pointcut can be passed in to match against", 
                "the current type as a ClassNode if there is a match with the argument"));
        registerGlobalPointcut("currentTypeIsEnclosingType", CurrentTypePointcut.class, 
                createDoc("Matches when the current type being inferred is the same as the enclosing type declaration.  " +
                		"This happens on references to <code>this</code> or when inferencing is occurring in the new statement position.", 
                        "No args", 
                        "the current type as a ClassNode if there is a match."));

        // filtering pointcuts
        registerGlobalPointcut("annotatedBy", AnnotatedByPointcut.class, 
                createDoc("Matches when the containing pointcut passes in an AnnotatedNode that is annotated " +
        		"by the argument in this pointcut.", "A String, Class, or ClassNode corresponding to an annotation", 
        		"the thing or things annotated by the given annotation.  Eg- If the surrounding pointcut passes in a single field, then the value returned will be that field (if " +
        		"the annotation matches), or null.  If the surrounding pointcut passes in a list of AnnotatedNodes, thne the result will be a sublist of those " +
        		"annotated nodes containing only nodes with the correct annotation."));
        registerGlobalPointcut("findField", FindFieldPointcut.class, createDoc("Matches when the containing pointcut passes in a type that has a field " +
                "specified by the argument in this pointcut.", "A String corresponding to a field name.  Alternatively, a pointcut, such as annotatedBy, wich " +
                "would return all fields with the given annotation",
                "the field or fields matched by the argument. " +
                "Eg- If the surrounding pointcut passes in a type, then the value returned will be all fields in that type that match the contained pointcut, or that have the specified name." +
                "  If the surrounding pointcut passes in a list of AnnotatedNodes, thne the result will be a sublist of those " +
                "annotated nodes containing only nodes with the correct annotation."));
        registerGlobalPointcut("variableExpression", VariableExpressionPointcut.class, createDoc("Matches when the current node being evaluated is a VariableExpression",
                "Variable name", "The matchd variable expression"));
        registerGlobalPointcut("findMethod", FindMethodPointcut.class, createDoc("Matches when the containing pointcut passes in a type that has a method " +
                "specified by the argument in this pointcut.", "A String corresponding to a method name.  Alternatively, a pointcut, such as annotatedBy, wich " +
                "would return all fields with the given annotation",
                "the method or methods matched by the argument. " +
                "Eg- If the surrounding pointcut passes in a type, then the value returned will be all methods in that type that match the contained pointcut, or that have the specified name." +
                "  If the surrounding pointcut passes in a list of AnnotatedNodes, thne the result will be a sublist of those " +
                "annotated nodes containing only nodes with the correct annotation."));
        registerGlobalPointcut("findProperty", FindPropertyPointcut.class, createDoc("Matches when the containing pointcut passes in a type that has a property " +
                "specified by the argument in this pointcut.", "A String corresponding to a property name.  Alternatively, a pointcut, such as annotatedBy, wich " +
                "would return all fields with the given annotation",
                "the method or methods matched by the argument. " +
                "Eg- If the surrounding pointcut passes in a type, then the value returned will be all properties in that type that match the contained pointcut, or that have the specified name." +
                "  If the surrounding pointcut passes in a list of AnnotatedNodes, thne the result will be a sublist of those " +
                "annotated nodes containing only nodes with the correct annotation."));
        registerGlobalPointcut("name", NamePointcut.class, createDoc("Checks that the items passed in will match the argument of this pointcut.  " +
        		"Often, this pointcut is superfluous as the findProperty, findMethod, and findField pointcuts already take a name.  " +
        		"However, this pointcut can be useful if you want to match a field both on name and something else.  Eg- " +
        		"The following will only match fields in the current type whose name is reference <em>and</em> are static:" +
        		"<pre>currentType( findField( name ('reference') & isStatic() ) )</pre>",
        		"A string corresponding to the name on which to match, or an object with a toString() method to match.",
        		"The matched object or objects."));
        registerGlobalPointcut("isFinal", FinalPointcut.class, createDoc("Matches if one or more of the passed in items are final", "none", "A sub-list of passed in items that are all final."));
        registerGlobalPointcut("isPrivate", PrivatePointcut.class, createDoc("Matches if one or more of the passed in items are private", "none", "A sub-list of passed in items that are all private."));
        registerGlobalPointcut("isPublic", PublicPointcut.class, createDoc("Matches if one or more of the passed in items are public", "none", "A sub-list of passed in items that are all public."));
        registerGlobalPointcut("isStatic", StaticPointcut.class, createDoc("Matches if one or more of the passed in items are static", "none", "A sub-list of passed in items that are all static."));
        registerGlobalPointcut("isSynchronized", SynchronizedPointcut.class, createDoc("Matches if one or more of the passed in items are synchronized", "none", "A sub-list of passed in items that are all synchronized."));
        registerGlobalPointcut("sourceFolderOfCurrentType", SourceFolderOfTypePointcut.class, createDoc("Matches on the source folder of the current type. Do not include the project name or a slash at the beginning of the name.  For example, the following will match the controller folder:" + 
        		    "<pre>SourceFolderOfTypePointcut('grails-app/controllers')</pre>", "the name of the source folder to match on", "If there is a match, then the current type is returned, otherwise null."));
        
        // lexical pointcuts
        registerGlobalPointcut("enclosingClass", EnclosingClassPointcut.class, createDoc("Matches if the current inferencing location is inside of a class or enum declaration.  A synonym for <code>isClass</code>", "A string, Class, ClassNode, or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing type is performed.", "The matched ClassNode or null if there was no match"));
        registerGlobalPointcut("isClass", EnclosingClassPointcut.class, createDoc("Matches if the current inferencing location is inside of a class or enum declaration.  A synonym for <code>enclosingClass</code>", "A string, Class, ClassNode, or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing type is performed.", "The matched ClassNode or null if there was no match"));  // synonym
        registerGlobalPointcut("enclosingScript", EnclosingScriptPointcut.class, createDoc("Matches if the current inferencing location is inside of a script declaration.  A synonym for <code>isScript</code>", "A string, Class, ClassNode, or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing type is performed.", "The matched ClassNode or null if there was no match"));
        registerGlobalPointcut("isScript", EnclosingScriptPointcut.class, createDoc("Matches if the current inferencing location is inside of a script declaration.  A synonym for <code>enclosingScript</code>", "A string, Class, ClassNode, or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing type is performed.", "The matched ClassNode or null if there was no match"));  // synonym
        registerGlobalPointcut("enclosingField", EnclosingFieldPointcut.class, createDoc("Matches if the current inferencing location is inside of a field declaration.", "A string or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing field is performed.", "The matched FieldNode or null if there was no match"));
        registerGlobalPointcut("enclosingMethod", EnclosingMethodPointcut.class, createDoc("Matches if the current inferencing location is inside of a field declaration.", "A string or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing methoid is performed.", "The matched MethodNode or null if there was no match"));
        registerGlobalPointcut("enclosingCallName", EnclosingCallNamePointcut.class, createDoc("Matches on the name of the enclosing method call.  The current inferencing location is enclosed by a method call if it is in the argument list of a method call", "The method name to match on", "The name of the method that was matched."));
        registerGlobalPointcut("enclosingCallDeclaringType", EnclosingCallDeclaringTypePointcut.class, createDoc("Matches on the declaring type of the enclosing method call.  The current inferencing location is enclosed by a method call if it is in the argument list of a method call.", "The declaring type of the method to match on.   This could be a string, Class, ClassNode, or a pointcut.", "The declaring type of the method that was matched."));
        registerGlobalPointcut("enclosingClosure", EnclosingClosurePointcut.class, createDoc("Matches if the inferencing location is inside of a ClosureExpression. A synonnym for <code>inClosure</code>.", "none", "A ClosureExpression corresponding to the lexically closest enclosing closure."));
        registerGlobalPointcut("inClosure", EnclosingClosurePointcut.class, createDoc("Matches if the inferencing location is inside of a ClosureExpression. A synonnym for <code>enclosingClosure</code>.", "none", "A ClosureExpression corresponding to the lexically closest enclosing closure."));  // synonym
        
        // structural pointcuts
        registerGlobalPointcut("fileExtension", FileExtensionPointcut.class, createDoc("Matches on the file extension of the file being inferred.", "The file extension without the '.'", "The full file name being matched, or null if there was no match."));
        registerGlobalPointcut("fileName", FileNamePointcut.class, createDoc("Matches on the simple file name of the file being inferred.", "The file name to match. Should include the file extension, but not the path.", "The simple file name that was matched, or null if there was no match."));
        registerGlobalPointcut("nature", ProjectNaturePointcut.class, createDoc("Matches on the Eclipse project nature for the current project.  " +
        		"For example:<blockquote>Groovy proejcts: <code>org.eclipse.jdt.groovy.core.groovyNature</code><br>" +
        		"Grails project: <code>com.springsource.sts.grails.core.nature</code></blockquote>", "The name of the project nature to check", 
        		"The project nature that was matched, or null if there was no match."));
        registerGlobalPointcut("sourceFolderOfCurrentFile", SourceFolderOfFilePointcut.class, createDoc("Matches on the source folder of the file being inferred. Do not include the project name or a slash at the beginning of the name.  For example, the following will match the controller folder:" +
        		"<pre>sourceFolderOfCurrentFile('grails-app/controllers')</pre>", "The name of the source folder to match", "The full name of the source folder, or null if there was no match."));
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
            UserExtensiblePointcut userExtensiblePointcut = new UserExtensiblePointcut(uniqueID, name, c);
            userExtensiblePointcut.setProject(project);
            return userExtensiblePointcut;
        } 
        
        Class<? extends IPointcut> pc = registry.get(name);
        if (pc != null) {
            try {
                // try the two arg constructor and the no-arg constructor
                try {
                    IPointcut p = pc.getConstructor(String.class, String.class).newInstance(uniqueID, name);
                    p.setProject(project);
                    return p;
                } catch (NoSuchMethodException e) {
                    IPointcut p = pc.getConstructor(String.class).newInstance();
                    if (p instanceof AbstractPointcut) {
                        ((AbstractPointcut) p).setPointcutName(name);
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
    
    private static String createDoc(String description, String arg, String returns) {
        return description + "<br /><br /><b>Parameters:</b><blockquote>" + arg + 
            "</blockquote><b>Return:</b><blockquote>" + returns + "</blockquote>"; 
    }
}
