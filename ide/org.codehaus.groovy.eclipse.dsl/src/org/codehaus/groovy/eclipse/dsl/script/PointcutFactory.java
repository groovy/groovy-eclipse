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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.FinalPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PrivatePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PublicPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.StaticPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.SynchronizedPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.BindPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentIdentifierPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypeIsEnclosingTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.DeclaringTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingCallDeclaringTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingCallNamePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingCallPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClassPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClosurePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingMethodPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingScriptPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileExtensionPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileNamePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindAnnotationPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindMethodPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindPropertyPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.HasArgumentsPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.HasAttributesPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.IsThisTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.NamePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.NotPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.OrPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.PackageFolderPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.ProjectNaturePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.SourceFolderOfFilePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.SourceFolderOfTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.SubTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.TypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.UserExtensiblePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.ValuePointcut;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

/**
 * Generates {@link IPointcut} objects
 * @author andrew
 * @created Feb 11, 2011
 */
public class PointcutFactory {

    private static final Map<String, Class<? extends IPointcut>> registry = new HashMap<String, Class<? extends IPointcut>>();
    private static final Map<String, String> docRegistry = new HashMap<String, String>();
    private static final Set<String> deprecatedRegistry = new HashSet<String>();
    static {
        // combinatorial pointcuts
        registerGlobalPointcut(
                "and",
                AndPointcut.class,
                createDoc("The exclusive combination of two or more pointcuts.  The 'and' "
                        + "pointcut matches when all containing pointcuts match.  Also, the bindings of all containing pointcuts "
                        + "are combined.  This pointcut is implicitly created when using the '<em>&</em>' operator to combine two or more pointcuts.", 
                        "This pointcut expects one or more pointcuts as arguments.  These pointcuts can be of any kind.",
                        "a combined set of all of the matches of all contained pointcuts"), false);
        registerGlobalPointcut(
                "or",
                OrPointcut.class,
                createDoc(
                        "The non-exclusive combination of two or more pointcuts.  The 'or' "
                        + "pointcut matches when all containing pointcuts match.  Also, the bindings of all containing pointcuts "
                        + "are combined.  This pointcut is implicitly created when using the '<em>|</em>' operator to combine two or more pointcuts.", 
                        "This pointcut expects one or more pointcuts as arguments.  These pointcuts can be of any kind.",
                        "a combined set of all of the matches of all contained pointcuts"), false);
        registerGlobalPointcut(
                "not",
                NotPointcut.class,
                createDoc("Negates the match of the contained pointcut.  This pointcut will match if the contained pointcut does not match and the reverse is true", 
                        "any pointcut",
                        "If the contained pointcut is not matched, then the return value is a singleton set consisting of <code>new Object()</code>, " +
                        "or else it is null"), false);

        // binding pointcuts
        registerGlobalPointcut(
                "bind",
                BindPointcut.class,
                createDoc("Adds a named binding for the contained pointcut.  This pointcut is implicitly used when a named argument is applied to any other pointcut",
                        "any pointcut",
                        "the return value of the contained pointcut"), false);

        // semantic pointcuts
        registerGlobalPointcut(
                "currentType",
                CurrentTypePointcut.class,
                createDoc(
                        "Attempts to match on the declared type of the current expression.",
                        "A String, Class, or ClassNode to match against.  Alternatively, another pointcut can be passed in to match against",
                        "The singleton set of the current type as a ClassNode."), false);
        registerGlobalPointcut(
                "isThisType",
                IsThisTypePointcut.class,
                createDoc(
                        "Matches when the current type being inferred is the same as the enclosing type declaration.  "
                                + "This happens on references to <code>this</code> or when inferencing is occurring in the new statement position.",
                                "This pointcut does not take any arguments", 
                        "The singleton set of the current type as a ClassNode."), false);

        // filtering pointcuts
        registerGlobalPointcut("subType", SubTypePointcut.class, 
                createDoc(
                        "Matches when the containing pointcut passes in a type (or a field or method declaration whose type) is a sub-type of the argument.", 
                        "A String, Class, or ClassNode specifying a type.  The type passed in must be a sub-type of this argument.", 
                        "The type specified by the argument (i.e., it will be the super-type that matches the type that is passed in)."), false);
        registerGlobalPointcut(
                "annotatedBy",
                FindAnnotationPointcut.class,
                createDoc(
                        "Matches when the containing pointcut passes in an <code>AnnotatedNode</code> that is annotated "
                                + "by the argument to this pointcut.",
                        "A String, Class, or ClassNode corresponding to an annotation, or another pointcut that specifies a set of annotations to match.",
                        "A set of <code>AnnotationNode</code>s that are matched by the argument."), false);
        registerGlobalPointcut(
                "fields",
                FindFieldPointcut.class,
                createFind("field", "fields"), false);
        registerGlobalPointcut(
                "methods",
                FindMethodPointcut.class,
                createFind("method", "methods"), false);
        registerGlobalPointcut(
                "properties",
                FindPropertyPointcut.class,
                createFind("property", "properties"), false);
        registerGlobalPointcut(
                "name",
                NamePointcut.class,
                createDoc(
                        "Matches when the items passed in equal the name specified as the argument.  "
                                + "Often, this pointcut is superfluous as the properties, methods, and fields pointcuts already take a String as a name.  "
                                + "However, this pointcut can be useful if you want to match a field both on name and something else.  Eg- "
                                + "The following will only match fields in the current type whose name is reference <em>and</em> are static:"
                                + "<pre>currentType( fields( name ('reference') & isStatic() ) )</pre>",
                        "A string corresponding to the name on which to match.  If a non-null object is an argument, then its <code>toString()</code> method will " +
                        "be called in order to determine the string to match on.",
                        "The matched objects as a set."), false);
        registerGlobalPointcut(
                "isFinal",
                FinalPointcut.class,
                createModifier("final"), false);
        registerGlobalPointcut(
                "isPrivate",
                PrivatePointcut.class,
                createModifier("private"), false);
        registerGlobalPointcut(
                "isPublic",
                PublicPointcut.class,
                createModifier("public"), false);
        registerGlobalPointcut(
                "isStatic",
                StaticPointcut.class,
                createModifier("static"), false);
        registerGlobalPointcut(
                "isSynchronized",
                SynchronizedPointcut.class,
                createModifier("synchronized"), false);
        registerGlobalPointcut(
                "sourceFolderOfCurrentType",
                SourceFolderOfTypePointcut.class,
                createDoc(
                        "Matches on the source folder of the current type.",
                        "the name of the source folder to match on.  Do not include the project name or a slash at the beginning of the name.  For example, the following will match the controller folder:"
                            + "<pre>SourceFolderOfTypePointcut('grails-app/controllers')</pre>",
                        "If there is a match, then the source folder name is returned as a singleton set, otherwise null."), false);

        // inside of method calls, declarations and annotations
        registerGlobalPointcut("hasAttribute", HasAttributesPointcut.class, 
                createDoc(
                        "Matches if the enclosing <code>annotatedBy</code> pointcut has attributes specified by the pointcut argument.",
                        "If the enclosing argument is a String, then the match will be on the attribute name.  Otherwise, the <code>name</code> and <code>value</code> pointcuts can be used instead.", 
                        "The value expression of the annotation argument as a Groovy AST node"), false);
        registerGlobalPointcut("hasArgument", HasArgumentsPointcut.class, 
                createDoc(
                        "Matches if the enclosing <code>enclosingCall</code> or <code>enclosingMethod</code> pointcut has named arguments specified by the pointcut argument." +
                        "Note that when this pointcut is used inside of <code>enclosingCall</code>, the <code>value</code> pointcut can be used, but when it is used" +
                        "inside of an <code>enclosingMethod</code>, <code>value</code> cannot be used.", 
                        "If the enclosing argument is a string, then the match is on the name of the named argumemt. Otherwise, the <code>name</code> and <code>value</code> pointcuts can be used instead.", 
                        "The value expression of the method call as a Groovy AST node"), false);
        registerGlobalPointcut("value", ValuePointcut.class, createDoc(
                "Matches on the value of an argument to a method call or an annotation.", 
                "A constant or literal to match against, or empty to match against any value.", 
                "A reifed representation of the matched value."), false);
        registerGlobalPointcut("type", TypePointcut.class, createDoc(
                "Matches on the type of an expression, method, field, property, parameter, or variable.", 
                "A String, Class object, or ClassNode corresponding to the type to match.", 
                "A singleton set of the type as a Groovy ClassNode."), false);
        registerGlobalPointcut("declaringType", DeclaringTypePointcut.class, createDoc(
                "Matches on the declaring type of an method, field,  or property.", 
                "A String, Class object, or ClassNode corresponding to the type to match.", 
                "A singleton set of the type as a Groovy ClassNode."), false);
        

        registerGlobalPointcut("enclosingCall", EnclosingCallPointcut.class, 
                createDoc(
                        "Matches on the method call that is enclosing the current location.", 
                        "Can match on the name of the method call and the arguments (using the <code>hasArguments</code> pointcut.",
                        "The method call expression as a Groovy AST node."), false);
        

        // lexical pointcuts
        registerGlobalPointcut(
                "enclosingClass",
                EnclosingClassPointcut.class,
                createDoc(
                        "Matches if the current inferencing location is inside of a class or enum declaration.  A synonym for <code>isClass</code>",
                        "A string, Class, ClassNode, or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing type is performed.",
                        "The matched ClassNode as a singleton set or null if there was no match"), false);
        registerGlobalPointcut(
                "isClass",
                EnclosingClassPointcut.class,
                createDoc(
                        "Matches if the current inferencing location is inside of a class or enum declaration.  A synonym for <code>enclosingClass</code>",
                        "A string, Class, ClassNode, or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing type is performed.",
                        "The matched ClassNode as a singleton set or null if there was no match"), false); // synonym
        registerGlobalPointcut(
                "enclosingScript",
                EnclosingScriptPointcut.class,
                createDoc(
                        "Matches if the current inferencing location is inside of a script declaration.  A synonym for <code>isScript</code>",
                        "A string, Class, ClassNode, or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing type is performed.",
                        "The matched ClassNode as a singleton set or null if there was no match"), false);
        registerGlobalPointcut(
                "isScript",
                EnclosingScriptPointcut.class,
                createDoc(
                        "Matches if the current inferencing location is inside of a script declaration.  A synonym for <code>enclosingScript</code>",
                        "A string, Class, ClassNode, or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing type is performed.",
                        "The matched ClassNode as a singleton set or null if there was no match"), false); // synonym
        registerGlobalPointcut(
                "enclosingField",
                EnclosingFieldPointcut.class,
                createDoc(
                        "Matches if the current inferencing location is inside of a field declaration.",
                        "A string or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing field is performed.",
                        "The matched FieldNode as a singleton set or null if there was no match"), false);
        registerGlobalPointcut(
                "enclosingMethod",
                EnclosingMethodPointcut.class,
                createDoc(
                        "Matches if the current inferencing location is inside of a method declaration.",
                        "A string or Pointcut further constraining what to match on.  If there are no arguments, then a simple check on the enclosing methoid is performed.",
                        "The matched MethodNode as a singleton set or null if there was no match"), false);
        registerGlobalPointcut(
                "enclosingCallName",
                EnclosingCallNamePointcut.class,
                createDoc(
                        "Matches on the name of the enclosing method call.  The current inferencing location is enclosed by a method call if it is in the argument list of a method call",
                        "The method name to match on", "The name of the method that was matched as a singleton set."), false);
        registerGlobalPointcut(
                "enclosingCallDeclaringType",
                EnclosingCallDeclaringTypePointcut.class,
                createDoc(
                        "Matches on the declaring type of the enclosing method call.  The current inferencing location is enclosed by a method call if it is in the argument list of a method call.",
                        "The declaring type of the method to match on.   This could be a string, Class, ClassNode, or a pointcut.",
                        "The declaring type of the method that was matched as a singleton set."), false);

        registerGlobalPointcut(
                "enclosingClosure",
                EnclosingClosurePointcut.class,
                createDoc(
                        "Matches if the inferencing location is inside of a ClosureExpression. A synonnym for <code>inClosure</code>.",
                        "none", "A set of <code>ClosureExpression</code>s corresponding to all of the closures enclosing the current expression."), false);
        registerGlobalPointcut(
                "inClosure",
                EnclosingClosurePointcut.class,
                createDoc(
                        "Matches if the inferencing location is inside of a ClosureExpression. A synonnym for <code>enclosingClosure</code>.",
                        "none", "A set of <code>ClosureExpression</code>s corresponding to all of the closures enclosing the current expression."), false);
        registerGlobalPointcut(
                "currentIdentifier",
                CurrentIdentifierPointcut.class,
                createDoc("Matches when the current node being evaluated is a VariableExpression", "The identifier name on which to match",
                        "The matchd variable expression as a singleton set"), false);

        // structural pointcuts
        registerGlobalPointcut(
                "fileExtension",
                FileExtensionPointcut.class,
                createDoc("Matches on the file extension of the file being inferred.", "The file extension without the '.'",
                        "The full file name being matched, or null if there was no match."), false);
        registerGlobalPointcut(
                "fileName",
                FileNamePointcut.class,
                createDoc("Matches on the simple file name of the file being inferred.",
                        "The file name to match. Should include the file extension, but not the path.",
                        "The simple file name that was matched, or null if there was no match."), false);
        registerGlobalPointcut(
                "packageFolder",
                PackageFolderPointcut.class,
                createDoc("Matches on the package folder name of the file being inferred.  The package folder is the sub-path from the package root to the file name (exclusive).",
                        "The package folder name to match.  Should not include the file name itself.",
                "The package folder name that was matched, or null if there was no match."), false);
        registerGlobalPointcut(
                "nature",
                ProjectNaturePointcut.class,
                createDoc("Matches on the Eclipse project nature for the current project.  "
                        + "For example:<blockquote>Groovy proejcts: <code>org.eclipse.jdt.groovy.core.groovyNature</code><br>"
                        + "Grails project: <code>org.grails.ide.eclipse.core.nature</code></blockquote>",
                        "The name of the project nature to check",
                        "The project nature that was matched, or null if there was no match."), false);
        registerGlobalPointcut(
                "sourceFolderOfCurrentFile",
                SourceFolderOfFilePointcut.class,
                createDoc(
                        "Matches on the source folder of the file being inferred. Do not include the project name or a slash at the beginning of the name.  For example, the following will match the controller folder:"
                                + "<pre>sourceFolderOfCurrentFile('grails-app/controllers')</pre>",
                        "The name of the source folder to match",
                        "The full name of the source folder, or null if there was no match."), false);
        
        
        
        // deprecated
        registerGlobalPointcut(
                "currentTypeIsEnclosingType",
                CurrentTypeIsEnclosingTypePointcut.class,
                createDoc(
                        "<b>Deprecated:</b> use <code>isThisType</code> instead.<br/><br/>" +
                        "Matches when the current type being inferred is the same as the enclosing type declaration.  "
                                + "Generally, this happens on references to <code>this</code> or when inferencing is occurring in the new statement position." +
                                "However, when inside of closures, <code>this</code> may have been set to something else, and so the pointcut may not match.",
                        "This pointcut does not take any arguments", 
                        "The singleton set of the current type as a ClassNode."), true);
    }

    /**
     * @param modifier 
     * @return
     */
    private static String createModifier(String modifier) {
        return createDoc("Matches if one or more of the passed in items are <code>" + modifier + "</code>", "none",
                "A sub-set of passed in items that are all <code>" + modifier + "</code>.");
    }

    /**
     * @param kind 
     * @param kinds 
     * @return
     */
    private static String createFind(String kind, String kinds) {
        return createDoc(
                "Matches when the containing pointcut passes in a type or a list of " + kind + " that has at least one " + kind
                        + " specified by the argument of this pointcut.",
                "A String corresponding to a " + kind + " name.  Alternatively, a pointcut, such as <code>annotatedBy</code>, which "
                        + "would match all " + kinds + " annotated by the inner pointcut.",
                "the " + kind + " or " + kinds + " matched by the argument. "
                        + "Eg- If the surrounding pointcut passes in a type, then the value returned will be a set of all " + kinds + " in that "
                        + "type that match the contained pointcut, or that have the specified name."
                        + "  If the surrounding pointcut passes in a set of " + kinds + ", thne the result will be a subset of those "
                        + kinds + " containing only nodes with the correct annotation.");
    }
    
    @SuppressWarnings("rawtypes")
    private final Map<String, Closure> localRegistry = new HashMap<String, Closure>();

    private final IStorage uniqueID;

    private final IProject project;
    
    public PointcutFactory(IStorage uniqueID, IProject project) {
        this.uniqueID = uniqueID;
        this.project = project;
    }
    
    
    private static void registerGlobalPointcut(String name, Class<? extends IPointcut> pcClazz, String doc, boolean isDeprecated) {
        registry.put(name, pcClazz);
        docRegistry.put(name, doc);
        if (isDeprecated) {
            deprecatedRegistry.add(name);
        }
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
                    IPointcut p = pc.getConstructor(IStorage.class, String.class).newInstance(uniqueID, name);
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
    
    private static String createDoc(String description, String expects, String returns) {
        return description + "<br /><br /><b>Parameters:</b><blockquote>" + expects + 
            "</blockquote><b>Return:</b><blockquote>" + returns + "</blockquote>" +
            "<b>More information:</b><blockquote>See <a href=\"http://docs.codehaus.org/display/GROOVY/DSL+Descriptors+for+Groovy-Eclipse\">" +
            "DSL Descriptors for Groovy-Eclipse</a></blockquote>"; 
    }
}
