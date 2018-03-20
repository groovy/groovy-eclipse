/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.LazilyResolvedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class GroovyClassScope extends ClassScope {

    // SET FOR TESTING ONLY, enables tests to listen for interesting events
    public static EventListener debugListener;

    private TraitHelper traitHelper = new TraitHelper();

    public GroovyClassScope(Scope parent, TypeDeclaration typeDecl) {
        super(parent, typeDecl);
    }

    public final ReferenceBinding getGroovyLangMetaClassBinding() {
        CompilationUnitScope unitScope = compilationUnitScope();
        unitScope.recordQualifiedReference(GroovyCompilationUnitScope.GROOVY_LANG_METACLASS);
        return unitScope.environment.getResolvedType(GroovyCompilationUnitScope.GROOVY_LANG_METACLASS, this);
    }

    /**
     * Adds any Groovy-specific method bindings to the set determined by the compiler.
     */
    @Override
    protected MethodBinding[] augmentMethodBindings(MethodBinding[] methodBindings) {
        // don't add these methods to annotations
        SourceTypeBinding binding = referenceContext.binding;
        if (binding != null && (binding.isAnnotationType() || binding.isInterface())) {
            return methodBindings;
        }
        boolean implementsGroovyLangObject = false;

        ReferenceBinding[] superInterfaces = binding.superInterfaces != null ? binding.superInterfaces : new ReferenceBinding[0];
        for (int i = 0, n = superInterfaces.length; i < n; i += 1) {
            char[][] interfaceName = superInterfaces[i].compoundName;
            if (CharOperation.equals(interfaceName, GroovyCompilationUnitScope.GROOVY_LANG_GROOVYOBJECT)) {
                implementsGroovyLangObject = true;
                break;
            }
        }

        List<MethodBinding> groovyMethods = new ArrayList<>();

        // If we don't then a supertype did and these methods do not have to be added here
        if (implementsGroovyLangObject) {
            if (debugListener != null) {
                debugListener.record("augment: type " + String.valueOf(referenceContext.name) + " having GroovyObject methods added");
            }
            TypeBinding bindingJLO = getJavaLangObject();
            TypeBinding bindingJLS = getJavaLangString();
            TypeBinding bindingGLM = getGroovyLangMetaClassBinding();

            // Now add the groovy.lang.GroovyObject methods:
            //   Object invokeMethod(String name, Object args);
            //   Object getProperty(String propertyName);
            //   void setProperty(String propertyName, Object newValue);
            //   MetaClass getMetaClass();
            //   void setMetaClass(MetaClass metaClass);

            // Note on synthetic: javac/ecj doesn't see synthetic methods when considering if a type implements an interface; so don't make these synthetic

            // visibility is public and possibly static/abstract depending on the containing type
            createMethod("invokeMethod", false, "", new TypeBinding[] {bindingJLS, bindingJLO}, bindingJLO, groovyMethods, methodBindings, null);
            createMethod("getProperty", false, "", new TypeBinding[] {bindingJLS}, bindingJLO, groovyMethods, methodBindings, null);
            createMethod("setProperty", false, "", new TypeBinding[] {bindingJLS, bindingJLO}, TypeBinding.VOID, groovyMethods, methodBindings, null);
            createMethod("getMetaClass", false, "", null, bindingGLM, groovyMethods, methodBindings, null);
            createMethod("setMetaClass", false, "", new TypeBinding[] {bindingGLM}, TypeBinding.VOID, groovyMethods, methodBindings, null);
        }
        // FIXASC decide what difference this makes - should we not be adding anything at all?
        // will not be an instance of GroovyTypeDeclaration if created through SourceTypeConverter
        if (this.referenceContext instanceof GroovyTypeDeclaration) {
            GroovyTypeDeclaration typeDeclaration = (GroovyTypeDeclaration) this.referenceContext;

            boolean useOldWay = false;
            if (useOldWay) {
                // FIXASC the methods created here need to be a subtype of
                // MethodBinding because they need their source position to be the
                // property
                List<PropertyNode> properties = typeDeclaration.properties;
                for (PropertyNode property : properties) {
                    String name = property.getName();
                    FieldBinding fBinding = typeDeclaration.binding.getField(name.toCharArray(), false);
                    // null binding indicates there was a problem resolving its type
                    if (fBinding != null && !(fBinding.type instanceof MissingTypeBinding)) {
                        String getterName = "get" + MetaClassHelper.capitalize(name);
                        createMethod(getterName, property.isStatic(), "", null, fBinding.type, groovyMethods, methodBindings, typeDeclaration);
                        if (!fBinding.isFinal()) {
                            String setterName = "set" + MetaClassHelper.capitalize(name);
                            createMethod(setterName, property.isStatic(), "", new TypeBinding[] {fBinding.type}, TypeBinding.VOID, groovyMethods, methodBindings, typeDeclaration);
                        }
                        if (fBinding.type == TypeBinding.BOOLEAN) {
                            createMethod("is" + MetaClassHelper.capitalize(name), property.isStatic(), "", null, fBinding.type, groovyMethods, methodBindings, typeDeclaration);
                        }
                    }
                }
            } else {
                // Create getters/setters without resolving the types.
                List<PropertyNode> properties = typeDeclaration.properties;
                for (PropertyNode property : properties) {
                    String name = property.getName();
                    String capitalizedName = MetaClassHelper.capitalize(name);
                    // Create getter
                    createGetterMethod(name, "get" + capitalizedName, property.isStatic(), groovyMethods, methodBindings, typeDeclaration);
                    // Create setter if non-final property
                    if (!Modifier.isFinal(property.getModifiers())) {
                        createSetterMethod(name, "set" + capitalizedName, property.isStatic(), groovyMethods, methodBindings, typeDeclaration, property.getType().getName());
                    }
                    // Create isA if type is boolean
                    String propertyType = property.getType().getName();
                    if ("boolean".equals(propertyType)) {
                        createGetterMethod(name, "is" + capitalizedName, property.isStatic(), groovyMethods, methodBindings, typeDeclaration);
                    }
                }
            }
        }

        Map<String, MethodBinding> methodsMap = new HashMap<>();
        for (ReferenceBinding face : superInterfaces) {
            if (traitHelper.isTrait(face)) {
                ReferenceBinding helperBinding = traitHelper.getHelperBinding(face);
                for (MethodBinding method : face.availableMethods()) {
                    if (method.isPrivate() || method.isStatic()) {
                        continue;
                    }
                    if (isNotActuallyAbstract(method, helperBinding)) {
                        methodsMap.put(getMethodAsString(method), method);
                    }
                }
            }
        }
        if (!methodsMap.isEmpty()) {
            Set<String> canBeOverridden = new HashSet<>();
            ReferenceBinding superclass = binding.superclass();
            while (superclass != null) {
                for (MethodBinding method : superclass.availableMethods()) {
                    if (method.isPrivate() || method.isPublic() || method.isStatic()) {
                        continue;
                    }
                    canBeOverridden.add(getMethodAsString(method));
                }
                superclass = superclass.superclass();
            }
            for (MethodBinding method : methodBindings) {
                canBeOverridden.remove(getMethodAsString(method));
            }
            for (String key : canBeOverridden) {
                MethodBinding method = methodsMap.get(key);
                if (method != null) {
                    method = new MethodBinding(method, binding);
                    method.modifiers &= ~Modifier.ABSTRACT;
                    groovyMethods.add(method);
                }
            }
        }

        MethodBinding[] newMethodBindings = groovyMethods.toArray(new MethodBinding[methodBindings.length + groovyMethods.size()]);
        System.arraycopy(methodBindings, 0, newMethodBindings, groovyMethods.size(), methodBindings.length);
        return newMethodBindings;
    }

    private String getMethodAsString(MethodBinding method) {
        StringBuilder key = new StringBuilder(new String(method.selector));
        key.append(" ");
        for (TypeBinding param : method.parameters) {
            char[] type = param.readableName();
            if (type != null) {
                key.append(type);
                key.append(" ");
            } else {
                key.append("null ");
            }
        }
        return key.toString();
    }

    private boolean isNotActuallyAbstract(MethodBinding methodBinding, ReferenceBinding helperBinding) {
        if (methodBinding.declaringClass instanceof SourceTypeBinding) {
            AbstractMethodDeclaration methodDeclaration = ((SourceTypeBinding) methodBinding.declaringClass).scope.referenceContext.declarationOf(methodBinding);
            if (methodDeclaration != null) {
                return !Flags.isAbstract(methodDeclaration.modifiers);
            }
        }
        if (methodBinding.declaringClass instanceof BinaryTypeBinding) {
            if (helperBinding != null) {
                for (MethodBinding m : helperBinding.methods()) {
                    if (!Arrays.equals(methodBinding.selector, m.selector)) {
                        continue;
                    }
                    TypeBinding[] actualParameters = m.parameters;
                    TypeBinding[] expectedParameters = methodBinding.parameters;
                    if (actualParameters.length != expectedParameters.length + 1) {
                        continue;
                    }
                    if (!actualParameters[0].equals(methodBinding.declaringClass)) {
                        continue;
                    }
                    boolean same = true;
                    for (int i = 0, n = expectedParameters.length; i < n; i += 1) {
                        if (!actualParameters[i + 1].equals(expectedParameters[i])) {
                            same = false;
                            break;
                        }
                    }
                    return same && !m.isAbstract();
                }
            }
        }
        return true;
    }

    private void createMethod(String name, boolean isStatic, String signature, TypeBinding[] parameterTypes,
            TypeBinding returnType, List<MethodBinding> groovyMethods, MethodBinding[] existingMethods,
            GroovyTypeDeclaration typeDeclaration) {
        boolean found = false;
        for (MethodBinding existingMethod : existingMethods) {
            if (new String(existingMethod.selector).equals(name)) {
                // FIXASC safe to do this resolution so early?
                ((SourceTypeBinding) existingMethod.declaringClass).resolveTypesFor(existingMethod);
                boolean equalParameters = true;
                if (parameterTypes == null) {
                    // not looking for parameters, if this has none, that is OK
                    if (existingMethod.parameters.length != 0) {
                        equalParameters = false;
                    }
                } else if (existingMethod.parameters.length == parameterTypes.length) {
                    TypeBinding[] existingParams = existingMethod.parameters;
                    for (int p = 0, max = parameterTypes.length; p < max; p++) {
                        if (!CharOperation.equals(parameterTypes[p].signature(), existingParams[p].signature())) {
                            equalParameters = false;
                            break;
                        }
                    }
                }
                // FIXASC consider return type?
                if (equalParameters) {
                    found = true;
                    break;
                }
                // FIXASC what about inherited methods - what if the supertype
                // provides an implementation, does the subtype get a new method?
            }
        }
        if (!found) {
            int modifiers = Flags.AccPublic;
            if (isStatic) {
                modifiers |= Flags.AccStatic;
            }
            if (this.referenceContext.binding.isInterface()) {
                modifiers |= Flags.AccAbstract;
            }
            char[] methodName = name.toCharArray();
            /*
             * if (typeDeclaration != null) { // check we are not attempting to override a final method MethodBinding[]
             * existingBindings = typeDeclaration.binding.getMethods(name.toCharArray()); int stop = 1; }
             */
            MethodBinding mb = new MethodBinding(modifiers, methodName, returnType, parameterTypes, null, referenceContext.binding);
            // FIXASC parameter names - what value would it have to set them correctly?
            groovyMethods.add(mb);
        }
    }

    private void createGetterMethod(String propertyName, String name, boolean isStatic, List<MethodBinding> groovyMethods,
            MethodBinding[] existingMethods, GroovyTypeDeclaration typeDeclaration) {
        boolean found = false;

        char[] nameAsCharArray = name.toCharArray();
        for (MethodBinding existingMethod : existingMethods) {
            if (CharOperation.equals(nameAsCharArray, existingMethod.selector)) {
                // check if this possible candidate has parameters (if it does, it can't be our getter)
                if ((existingMethod.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
                    // need some intelligence here
                    AbstractMethodDeclaration methodDecl = existingMethod.sourceMethod();
                    if (methodDecl == null) {
                        // FIXASC decide what we can do here
                    } else {
                        Argument[] arguments = methodDecl.arguments;
                        if (arguments == null || arguments.length == 0) {
                            found = true;
                        }
                    }
                } else {
                    TypeBinding[] existingParams = existingMethod.parameters;
                    if (existingParams == null || existingParams.length == 0) {
                        found = true;
                    }
                }
            }
        }

        // FIXASC what about inherited methods - what if the supertype
        // provides an implementation, does the subtype get a new method?
        if (!found) {
            int modifiers = Flags.AccPublic;
            if (isStatic) {
                modifiers |= Flags.AccStatic;
            }
            if (this.referenceContext.binding.isInterface()) {
                modifiers |= Flags.AccAbstract;
            }
            /*
             * if (typeDeclaration != null) { // check we are not attempting to override a final method MethodBinding[]
             * existingBindings = typeDeclaration.binding.getMethods(name.toCharArray()); int stop = 1; }
             */
            MethodBinding mb = new LazilyResolvedMethodBinding(true, propertyName, modifiers, nameAsCharArray, null, referenceContext.binding);
            // FIXASC parameter names - what value would it have to set them correctly?
            groovyMethods.add(mb);
        }
    }

    private void createSetterMethod(String propertyName, String name, boolean isStatic, List<MethodBinding> groovyMethods,
            MethodBinding[] existingMethods, GroovyTypeDeclaration typeDeclaration, String propertyType) {
        boolean found = false;

        char[] nameAsCharArray = name.toCharArray();
        for (MethodBinding existingMethod : existingMethods) {
            if (CharOperation.equals(nameAsCharArray, existingMethod.selector)) {
                // check if this possible candidate has parameters (if it does, it can't be our getter)
                if ((existingMethod.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
                    // lets look at the declaration
                    AbstractMethodDeclaration methodDecl = existingMethod.sourceMethod();
                    if (methodDecl == null) {
                        // FIXASC decide what we can do here
                    } else {
                        Argument[] arguments = methodDecl.arguments;
                        if (arguments != null && arguments.length == 1) {
                            // might be a candidate, it takes one parameter
                            // TypeReference tr = arguments[0].type;
                            // String typename = new String(CharOperation.concatWith(tr.getTypeName(), '.'));
                            // // not really an exact comparison here...
                            // if (typename.endsWith(propertyName)) {
                            found = true;
                            // }
                        }
                    }
                } else {
                    TypeBinding[] existingParams = existingMethod.parameters;
                    if (existingParams != null && existingParams.length == 1) {
                        // if (CharOperation.equals(existingParams[0].signature(),)) {
                        // might be a candidate, it takes one parameter
                        found = true;
                        // }
                    }
                }
            }
        }

        // FIXASC what about inherited methods - what if the supertype
        // provides an implementation, does the subtype get a new method?
        if (!found) {
            int modifiers = Flags.AccPublic;
            if (isStatic) {
                modifiers |= Flags.AccStatic;
            }
            if (this.referenceContext.binding.isInterface()) {
                modifiers |= Flags.AccAbstract;
            }
            char[] methodName = name.toCharArray();
            MethodBinding mb = new LazilyResolvedMethodBinding(false, propertyName, modifiers, methodName, null, referenceContext.binding);
            // FIXASC parameter names - what value would it have to set them correctly?
            groovyMethods.add(mb);
        }
    }

    @Override
    public boolean shouldReport(int problem) {
        if (problem == IProblem.SuperclassMustBeAClass) {
            return false;
        }
        if (problem == IProblem.IncompatibleReturnType) {
            return false;
        }
        if (problem == IProblem.AbstractMethodMustBeImplemented) {
            return false;
        }
        if (problem == IProblem.MethodNameClash) {
            return false;
        }
        if (problem == IProblem.VarargsConflict) {
            return false;
        }
        return true;
    }

    @Override
    public MethodBinding[] getAnyExtraMethods(char[] selector, TypeBinding[] argumentTypes) {
        MethodBinding[] bindings = null;

        List<MethodNode> methods = ((GroovyTypeDeclaration) referenceContext).getClassNode().getMethods(String.valueOf(selector));
        if (methods != null && !methods.isEmpty()) {
            int n = methods.size();
            bindings = new MethodBinding[n];
            for (int i = 0; i < n; i += 1) {
                MethodNode method = methods.get(i);
                TypeBinding[] parameterTypes = null; // TODO: resolve these
                TypeBinding returnType = compilationUnitScope().environment.getResolvedType(
                    CharOperation.splitAndTrimOn('.', method.getReturnType().getName().toCharArray()), this);
                bindings[i] = new MethodBinding(method.getModifiers(), selector, returnType, parameterTypes, null, referenceContext.binding);
            }
        }

        return bindings;
    }

    @Override
    protected ClassScope buildClassScope(Scope parent, TypeDeclaration typeDecl) {
        return new GroovyClassScope(parent, typeDecl);
    }

    @Override
    public void buildFieldsAndMethods() {
        super.buildFieldsAndMethods();
        GroovyTypeDeclaration[] anonymousTypes = ((GroovyTypeDeclaration) referenceContext).getAnonymousTypes();
        if (anonymousTypes != null) {
            for (GroovyTypeDeclaration anonType : anonymousTypes) {
                anonType.scope = new GroovyClassScope(this, anonType);
                anonType.resolve(anonType.enclosingScope);
            }
        }
        // STS-3930 start
        for (MethodBinding method : referenceContext.binding.methods()) {
            fixupTypeParameters(method);
        }
        // STS-3930 end
    }

    /**
     * This is fix for generic methods with default parameter values. For those methods type variables and parameter arguments
     * should be the same as it is for all other methods.
     *
     * @param method method to be fixed
     */
    private void fixupTypeParameters(MethodBinding method) {
        if (method.typeVariables == null || method.typeVariables.length == 0) {
            return;
        }
        if (method.parameters == null || method.parameters.length == 0) {
            return;
        }
        Map<String, TypeBinding> bindings = new HashMap<>();
        for (TypeVariableBinding v : method.typeVariables) {
            bindings.put(new String(v.sourceName), v);
        }
        for (TypeBinding parameter : method.parameters) {
            if (!(parameter instanceof ParameterizedTypeBinding)) {
                continue;
            }
            TypeBinding[] arguments = ((ParameterizedTypeBinding) parameter).arguments;
            if (arguments == null) {
                continue;
            }
            for (int i = 0, n = arguments.length; i < n; i += 1) {
                if (arguments[i] instanceof TypeVariableBinding) {
                    String name = new String(arguments[i].sourceName());
                    TypeBinding argument = bindings.get(name);
                    if (argument != null && arguments[i].id != argument.id) {
                        arguments[i] = argument;
                    }
                }
            }
        }
    }

    /**
     * The class helps to check if some class node is trait.
     */
    private class TraitHelper {

        private boolean lookForTraitAlias;
        private boolean toBeInitialized = true;

        private void initialize() {
            ImportBinding[] imports = referenceContext.scope.compilationUnitScope().imports;
            if (imports != null) {
                for (ImportBinding i : imports) {
                    String importedType = new String(i.readableName());
                    if ("groovy.transform.Trait".equals(importedType)) {
                        lookForTraitAlias = true;
                        break;
                    }
                    if (importedType.endsWith(".Trait")) {
                        lookForTraitAlias = false;
                        break;
                    }
                    if ("groovy.transform.*".equals(importedType)) {
                        lookForTraitAlias = true;
                    }
                }
                toBeInitialized = true;
            }
        }

        private boolean isTrait(ReferenceBinding referenceBinding) {
            if (referenceBinding == null) {
                return false;
            }
            if (toBeInitialized) {
                initialize();
            }
            AnnotationBinding[] annotations = referenceBinding.getAnnotations();
            if (annotations != null) {
                for (AnnotationBinding annotation : annotations) {
                    String annotationName = CharOperation.toString(annotation.getAnnotationType().compoundName);
                    if ("groovy.transform.Trait".equals(annotationName)) {
                        return true;
                    }
                    if (lookForTraitAlias && "Trait".equals(annotationName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private ReferenceBinding getHelperBinding(ReferenceBinding interfaceBinding) {
            if (interfaceBinding instanceof BinaryTypeBinding) {
                StringBuilder nameBuilder = new StringBuilder();
                nameBuilder.append(interfaceBinding.sourceName);
                nameBuilder.append("$Trait$Helper");
                ReferenceBinding helperBinding = compilationUnitScope().findType(nameBuilder.toString().toCharArray(), interfaceBinding.fPackage, interfaceBinding.fPackage);
                if (helperBinding != null) {
                    if (helperBinding instanceof ProblemReferenceBinding) {
                        helperBinding = ((ProblemReferenceBinding) helperBinding).closestReferenceMatch();
                    }
                }
                return helperBinding;
            }
            return null;
        }
    }
}
