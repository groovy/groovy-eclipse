/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jdt.groovy.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration.ENUM_CONSTANT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.LazilyResolvedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.core.nd.util.CharArrayMap;

public class GroovyClassScope extends ClassScope {

    // SET FOR TESTING ONLY, enables tests to listen for interesting events
    public static EventListener debugListener;

    private final TraitHelper traitHelper = new TraitHelper();

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
        SourceTypeBinding typeBinding = referenceContext.binding;
        if (typeBinding == null || typeBinding.isInterface() || typeBinding.isAnnotationType()) {
            return methodBindings;
        }

        ReferenceBinding[] superInterfaces = Optional.ofNullable(typeBinding.superInterfaces).orElse(Binding.NO_SUPERINTERFACES);

        boolean implementsGroovyLangObject = false;
        for (ReferenceBinding face : superInterfaces) {
            if (CharOperation.equals(face.compoundName, GroovyCompilationUnitScope.GROOVY_LANG_GROOVYOBJECT)) {
                implementsGroovyLangObject = true;
                break;
            }
        }

        List<MethodBinding> groovyMethods = new ArrayList<>();

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

            createMethod("invokeMethod", false, new TypeBinding[] {bindingJLS, bindingJLO}, bindingJLO, methodBindings)
                .ifPresent(groovyMethods::add);
            createMethod("getProperty", false, new TypeBinding[] {bindingJLS}, bindingJLO, methodBindings)
                .ifPresent(groovyMethods::add);
            createMethod("setProperty", false, new TypeBinding[] {bindingJLS, bindingJLO}, TypeBinding.VOID, methodBindings)
                .ifPresent(groovyMethods::add);
            createMethod("getMetaClass", false, Binding.NO_TYPES, bindingGLM, methodBindings)
                .ifPresent(groovyMethods::add);
            createMethod("setMetaClass", false, new TypeBinding[] {bindingGLM}, TypeBinding.VOID, methodBindings)
                .ifPresent(groovyMethods::add);
        }

        // create property accessors without resolving the types
        if (referenceContext instanceof GroovyTypeDeclaration) {
            for (PropertyNode property : ((GroovyTypeDeclaration) referenceContext).getClassNode().getProperties()) {
                int modifiers = getModifiers(property);
                if (Flags.isPackageDefault(modifiers)) continue;

                String name = property.getName(), capitalizedName = MetaClassHelper.capitalize(name);

                if (ClassHelper.boolean_TYPE.equals(property.getType())) {
                    createGetterMethod(name, "is" + capitalizedName, modifiers, methodBindings)
                        .ifPresent(binding -> {
                            groovyMethods.add(binding);
                            // GROOVY-9382: no getter generated if isser declared
                            createGetterMethod(name, "get" + capitalizedName, modifiers, methodBindings)
                                .ifPresent(groovyMethods::add);
                        });
                } else {
                    createGetterMethod(name, "get" + capitalizedName, modifiers, methodBindings)
                        .ifPresent(groovyMethods::add);
                }

                if (!Flags.isFinal(property.getModifiers())) {
                    createSetterMethod(name, "set" + capitalizedName, modifiers, property.getType().getName(), methodBindings)
                        .ifPresent(groovyMethods::add);
                }
            }
        }

        Map<String, MethodBinding> traitMethods = new HashMap<>();
        for (ReferenceBinding face : superInterfaces) {
            if (traitHelper.isTrait(face)) {
                ReferenceBinding helperBinding = traitHelper.getHelperBinding(face);
                for (MethodBinding method : face.availableMethods()) {
                    if (!method.isPrivate() && !method.isStatic() &&
                            isNotActuallyAbstract(method, helperBinding)) {
                        traitMethods.put(getMethodAsString(method), method);
                    }
                }
            }
        }
        if (!traitMethods.isEmpty()) {
            Set<String> canBeOverridden = new HashSet<>();

            ReferenceBinding superclass = typeBinding;
            while ((superclass = superclass.superclass()) != null) {
                for (MethodBinding method : superclass.availableMethods()) {
                    if (!method.isPrivate() && !method.isPublic() && !method.isStatic()) {
                        canBeOverridden.add(getMethodAsString(method));
                    }
                }
            }
            for (MethodBinding method : methodBindings) {
                canBeOverridden.remove(getMethodAsString(method));
            }

            for (String key : canBeOverridden) {
                MethodBinding method = traitMethods.remove(key);
                if (method != null) {
                    method = new MethodBinding(method, typeBinding);
                    method.modifiers &= ~Flags.AccAbstract;
                    groovyMethods.add(method);
                }
            }

            for (MethodBinding method : traitMethods.values()) {
                method.modifiers &= ~Flags.AccAbstract;
            }
        }

        return Stream.concat(Stream.of(methodBindings), groovyMethods.stream()).toArray(MethodBinding[]::new);
    }

    private int getModifiers(PropertyNode property) {
        int modifiers = (property.getModifiers() & 0xF);

        if (property.getType().isUsingGenerics()) {
            modifiers |= ExtraCompilerModifiers.AccGenericSignature;
        }

        // if @PackageScope was detected by GCUD, field's modifiers will show it
        char[] nameChars = property.getName().toCharArray();
        for (FieldDeclaration field : referenceContext.fields) {
            if (CharOperation.equals(field.name, nameChars)) {
                if (Flags.isPackageDefault(field.modifiers)) {
                    modifiers &= ~Flags.AccPublic;
                }
                break;
            }
        }

        if (property.getField().getAnnotations().stream().map(anno -> anno.getClassNode().getName())
                .anyMatch(name -> name.equals("Deprecated") || name.equals("java.lang.Deprecated"))) {
            modifiers |= Flags.AccDeprecated;
        }

        return modifiers;
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
            AbstractMethodDeclaration methodDeclaration =
                ((SourceTypeBinding) methodBinding.declaringClass).scope.referenceContext.declarationOf(methodBinding);
            if (methodDeclaration != null) {
                return !Flags.isAbstract(methodDeclaration.modifiers);
            }
        } else if (methodBinding.declaringClass instanceof BinaryTypeBinding) {
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

    private Optional<MethodBinding> createMethod(String name, boolean isStatic, TypeBinding[] parameterTypes, TypeBinding returnType,
            MethodBinding[] existingMethods) {
        boolean found = false;
        char[] nameAsCharArray = name.toCharArray();
        for (MethodBinding existingMethod : existingMethods) {
            if (CharOperation.equals(nameAsCharArray, existingMethod.selector)) {
                // TODO: Is it safe to do this resolution so early?
                ((SourceTypeBinding) existingMethod.declaringClass).resolveTypesFor(existingMethod);
                boolean equalParameters = (parameterTypes.length == existingMethod.parameters.length);
                if (equalParameters) {
                    for (int i = 0, n = parameterTypes.length; i < n; i += 1) {
                        if (!CharOperation.equals(parameterTypes[i].signature(), existingMethod.parameters[i].signature())) {
                            equalParameters = false;
                            break;
                        }
                    }
                }
                // TODO: Check return type?
                if (equalParameters) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            int modifiers = Flags.AccPublic;
            if (isStatic) {
                modifiers |= Flags.AccStatic;
            }
            if (referenceContext.binding.isInterface()) {
                modifiers |= Flags.AccAbstract;
            }

            return Optional.of(new MethodBinding(modifiers, nameAsCharArray, returnType, parameterTypes, null, referenceContext.binding));
        }
        return Optional.empty();
    }

    private Optional<MethodBinding> createGetterMethod(String propertyName, String name, int modifiers,
            MethodBinding[] existingMethods) {
        boolean found = false;
        char[] nameAsCharArray = name.toCharArray();
        for (MethodBinding existingMethod : existingMethods) {
            if (CharOperation.equals(nameAsCharArray, existingMethod.selector)) {
                if ((existingMethod.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
                    Argument[] arguments = existingMethod.sourceMethod().arguments;
                    if (arguments == null || arguments.length == 0) {
                        found = true;
                        break;
                    }
                } else {
                    TypeBinding[] parameters = existingMethod.parameters;
                    if (parameters == null || parameters.length == 0) {
                        found = true;
                        break;
                    }
                }
            }
        }

        if (!found) {
            if (referenceContext.binding.isInterface()) {
                modifiers |= Flags.AccAbstract;
            }
            return Optional.of(new LazilyResolvedMethodBinding(true, propertyName, modifiers, nameAsCharArray, null, referenceContext.binding));
        }
        return Optional.empty();
    }

    private Optional<MethodBinding> createSetterMethod(String propertyName, String name, int modifiers, String propertyType,
            MethodBinding[] existingMethods) {
        boolean found = false;
        char[] nameAsCharArray = name.toCharArray();
        for (MethodBinding existingMethod : existingMethods) {
            if (CharOperation.equals(nameAsCharArray, existingMethod.selector)) {
                if ((existingMethod.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
                    Argument[] arguments = existingMethod.sourceMethod().arguments;
                    if (arguments != null && arguments.length == 1) {
                        // TODO: Check argument type vs property type?
                        found = true;
                        break;
                    }
                } else {
                    TypeBinding[] parameters = existingMethod.parameters;
                    if (parameters != null && parameters.length == 1) {
                        // TODO: Check parameter type vs property type?
                        found = true;
                        break;
                    }
                }
            }
        }

        if (!found) {
            if (referenceContext.binding.isInterface()) {
                modifiers |= Flags.AccAbstract;
            }
            return Optional.of(new LazilyResolvedMethodBinding(false, propertyName, modifiers, nameAsCharArray, null, referenceContext.binding));
        }
        return Optional.empty();
    }

    @Override
    protected void buildFieldsAndMethods() {
        super.buildFieldsAndMethods();

        for (FieldDeclaration field : referenceContext.fields) {
            Expression initialization = field.initialization;
            // unwrap "field = (Type) (Object) new Anon() {}"
            while (initialization instanceof CastExpression) {
                initialization = ((CastExpression) initialization).expression;
            }
            if (initialization instanceof QualifiedAllocationExpression) {
                QualifiedAllocationExpression allocation = (QualifiedAllocationExpression) initialization;
                if (allocation.anonymousType != null && allocation.anonymousType.scope == null) { // anon. inner initialization
                    MethodScope scope = (field.isStatic() ? referenceContext.staticInitializerScope : referenceContext.initializerScope);
                    if (field.binding.type == null) {
                        field.binding.type = (field.getKind() == ENUM_CONSTANT ? scope.enclosingSourceType() : field.type.resolveType(scope));
                    }
                    field.resolve(scope);
                }
            }
        }

        /*
         * Fix generic methods with default parameter values. For those methods
         * type variables and parameter arguments should be the same as it is
         * for all other methods.
         */
        for (MethodBinding method : referenceContext.binding.methods()) {
            if (method.parameters != null && method.parameters.length > 0 &&
                    method.typeVariables != null && method.typeVariables.length > 0) {
                CharArrayMap<TypeVariableBinding> bindings = new CharArrayMap<>();
                for (TypeVariableBinding tvb : method.typeVariables) {
                    bindings.put(tvb.sourceName, tvb);
                }
                for (TypeBinding parameter : method.parameters) {
                    if (parameter instanceof ParameterizedTypeBinding) {
                        TypeBinding[] arguments = ((ParameterizedTypeBinding) parameter).arguments;
                        if (arguments != null) {
                            for (int i = 0, n = arguments.length; i < n; i += 1) {
                                if (arguments[i] instanceof TypeVariableBinding) {
                                    TypeBinding argument = bindings.get(arguments[i].sourceName());
                                    if (argument != null && arguments[i].id != argument.id) {
                                        arguments[i] = argument;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldReport(int problem) {
        switch (problem) {
        case IProblem.EnumConstantMustImplementAbstractMethod:
        case IProblem.AbstractMethodMustBeImplemented:
        case IProblem.MissingValueForAnnotationMember:
        case IProblem.FinalMethodCannotBeOverridden:
        case IProblem.MethodMustOverrideOrImplement:
        case IProblem.MethodReducesVisibility:
        case IProblem.IncompatibleReturnType:
        case IProblem.SuperclassMustBeAClass:
        case IProblem.UndefinedConstructor:
        case IProblem.MethodNameClash:
        case IProblem.VarargsConflict:
            return false;
        default:
            return true;
        }
    }

    /**
     * Checks if some class node is trait.
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
            if (referenceBinding != null) {
                if (toBeInitialized) {
                    initialize();
                }
                AnnotationBinding[] annotations = referenceBinding.getAnnotations();
                if (annotations != null) {
                    for (AnnotationBinding annotation : annotations) {
                        if (annotation != null) {
                            ReferenceBinding annotationType = annotation.getAnnotationType();
                            String annotationName = CharOperation.toString(annotationType.compoundName);
                            if ("groovy.transform.Trait".equals(annotationName)) {
                                return true;
                            }
                            if (lookForTraitAlias && "Trait".equals(annotationName)) {
                                return true;
                            }
                        }
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
                ReferenceBinding helperBinding = compilationUnitScope().findType(
                    nameBuilder.toString().toCharArray(), interfaceBinding.fPackage, interfaceBinding.fPackage);
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
