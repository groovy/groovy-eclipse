/*
 * Copyright 2009-2026 the original author or authors.
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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PropertyNode;
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
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
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
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class GroovyClassScope extends ClassScope {

    // SET FOR TESTING ONLY; enables tests to listen for interesting events
    public static EventListener debugListener;

    private final TraitHelper traitHelper = new TraitHelper();

    public GroovyClassScope(final Scope parent, final TypeDeclaration typeDecl) {
        super(parent, typeDecl);
    }

    /**
     * Ensures classes implement {@code groovy.lang.GroovyObject}.
     */
    @Override
    protected boolean connectSuperInterfaces() {
        boolean noProblems = super.connectSuperInterfaces();
        SourceTypeBinding sourceType = referenceContext.binding;
        if (!sourceType.isInterface() && !sourceType.isAnnotationType()) { // TODO: @POGO
            ReferenceBinding groovyObject = getGroovyLangGroovyObject();
            if (!sourceType.implementsInterface(groovyObject, true)) {
                int n = sourceType.superInterfaces.length;
                if (n == 0) {
                    sourceType.superInterfaces = new ReferenceBinding[1];
                } else {
                    ReferenceBinding[] types = sourceType.superInterfaces;
                    sourceType.superInterfaces = new ReferenceBinding[n + 1];
                    System.arraycopy(types, 0, sourceType.superInterfaces, 0, n);
                }
                sourceType.superInterfaces[n] = groovyObject;
            }
        }
        return noProblems;
    }

    /**
     * Adds any Groovy-specific method bindings to the set determined by the compiler.
     */
    @Override
    protected MethodBinding[] augmentMethodBindings(final MethodBinding[] methodBindings) {
        SourceTypeBinding sourceType = referenceContext.binding;
        if (sourceType == null || sourceType.isAnnotationType() ||
                (sourceType.isInterface() && !traitHelper.isTrait(sourceType))) {
            return methodBindings;
        }

        boolean implementsGroovyObject = false;
        ReferenceBinding[] superInterfaces = sourceType.superInterfaces();
        if (superInterfaces == null) superInterfaces = Binding.NO_SUPERINTERFACES;

        for (int i = superInterfaces.length; i != 0;) {
            if (CharOperation.equals(superInterfaces[--i].compoundName, GroovyCompilationUnitScope.GROOVY_LANG_GROOVYOBJECT)) {
                implementsGroovyObject = true;
                break;
            }
        }

        List<MethodBinding> groovyMethods = new ArrayList<>();

        if (implementsGroovyObject) {
            if (debugListener != null) {
                debugListener.record(new StringBuilder("augment: type ").append(referenceContext.name).append(" having GroovyObject methods added").toString());
            }
            TypeBinding bindingJLO = getJavaLangObject();
            TypeBinding bindingJLS = getJavaLangString();
            TypeBinding bindingGLM = getGroovyLangMetaClass();

            // Now add the groovy.lang.GroovyObject methods:
            //   Object invokeMethod(String name, Object args);
            //   Object getProperty(String propertyName);
            //   void setProperty(String propertyName, Object newValue);
            //   MetaClass getMetaClass();
            //   void setMetaClass(MetaClass metaClass);

            // Note: javac/ecj doesn't see synthetic methods when considering if a type implements an interface; so don't make these synthetic

            createMethod("invokeMethod", new TypeBinding[] {bindingJLS, bindingJLO}, bindingJLO, methodBindings)
                .ifPresent(groovyMethods::add);
            createMethod("getProperty", new TypeBinding[] {bindingJLS}, bindingJLO, methodBindings)
                .ifPresent(groovyMethods::add);
            createMethod("setProperty", new TypeBinding[] {bindingJLS, bindingJLO}, TypeBinding.VOID, methodBindings)
                .ifPresent(groovyMethods::add);
            createMethod("getMetaClass", Binding.NO_TYPES, bindingGLM, methodBindings) // TODO: @java.beans.Transient
                .ifPresent(groovyMethods::add);
            createMethod("setMetaClass", new TypeBinding[] {bindingGLM}, TypeBinding.VOID, methodBindings)
                .ifPresent(groovyMethods::add);
        }

        // create property accessors without resolving the types
        if (referenceContext instanceof GroovyTypeDeclaration groovyTypeDecl) {
            for (PropertyNode property : groovyTypeDecl.getClassNode().getProperties()) {
                int modifiers = getModifiers(property);
                if (Flags.isPackageDefault(modifiers)) continue;
                if (Flags.isStatic(modifiers) && sourceType.isInterface()) continue; // trait

                String capitalizedName = org.apache.groovy.util.BeanUtils.capitalize(property.getName());

                if (!groovyTypeDecl.isRecord() && ClassHelper.boolean_TYPE.equals(property.getType())) {
                    if (!createGetterMethod(property, "get" + capitalizedName, modifiers, methodBindings).isPresent()) {
                        continue; // only generate accessor method(s) if one or both are not explicitly declared
                    }
                    createGetterMethod(property, "is" + capitalizedName, modifiers, methodBindings) // TODO: PropertyNode#getGetterNameOrDefault
                        .ifPresent(binding -> {
                            groovyMethods.add(binding);
                            createGetterMethod(property, "get" + capitalizedName, modifiers, methodBindings)
                                .ifPresent(groovyMethods::add);
                        });
                } else {
                    String propertyAccessorName = groovyTypeDecl.isRecord() ? property.getName() : "get" + capitalizedName;
                    createGetterMethod(property, propertyAccessorName, modifiers, methodBindings) // TODO: PropertyNode#getGetterNameOrDefault
                        .ifPresent(groovyMethods::add);
                }

                if (!Flags.isFinal(property.getModifiers())) {
                    createSetterMethod(property, "set" + capitalizedName, modifiers, methodBindings) // TODO: PropertyNode#getSetterNameOrDefault
                        .ifPresent(groovyMethods::add);
                }
            }
        }

        Map<String, MethodBinding> traitMethods = new HashMap<>();
        for (ReferenceBinding face : superInterfaces) {
            if (traitHelper.isTrait(face)) {
                ReferenceBinding helperBinding = traitHelper.getHelperBinding(face);
                for (MethodBinding method : face.availableMethods()) {
                    if (!method.isSynthetic() && isNotActuallyAbstract(method, helperBinding)) {
                        if ((method.modifiers & ExtraCompilerModifiers.AccModifierProblem) != 0) { // Java 7: +static, -abstract
                            method.modifiers ^= Flags.AccStatic | Flags.AccAbstract | ExtraCompilerModifiers.AccModifierProblem;
                        }
                        if ((method.modifiers & ExtraCompilerModifiers.AccBlankFinal) != 0) { // +final
                            method.modifiers ^= Flags.AccFinal | ExtraCompilerModifiers.AccBlankFinal;
                        }
                        if (method.isPublic() || method.isStatic()) {
                            traitMethods.putIfAbsent(getMethodAsString(method), method);
                        }
                    }
                }
            }
        }
        if (!traitMethods.isEmpty()) {
            Set<String> canBeOverridden = new HashSet<>();

            ReferenceBinding superclass = sourceType;
            while ((superclass = superclass.superclass()) != null) {
                for (MethodBinding method : superclass.availableMethods()) {
                    if (!method.isConstructor() && !method.isPrivate() && !method.isStatic() && !method.isFinal()) {
                        canBeOverridden.add(getMethodAsString(method));
                    }
                }
            }
            for (MethodBinding method : methodBindings) {
                if (!method.isConstructor()) {
                    String signature = getMethodAsString(method);
                    canBeOverridden.remove(signature);
                    traitMethods.remove(signature);
                }
            }

            for (String key : canBeOverridden) {
                MethodBinding method = traitMethods.remove(key);
                if (method != null) {
                    // the trait method overrides a superclass method
                    method = new MethodBinding(method, sourceType);
                    method.modifiers &= ~Flags.AccAbstract;
                    method.modifiers &= ~Flags.AccPrivate;
                    groovyMethods.add(method);
                }
            }

            for (MethodBinding method : traitMethods.values()) {
                if (method.isStatic()) {
                    method = new MethodBinding(method, sourceType);
                    method.modifiers &= ~Flags.AccPrivate;
                    method.modifiers |=  Flags.AccPublic;
                    groovyMethods.add(method);
                }
            }
        }

        if (groovyMethods.isEmpty()) {
            return methodBindings;
        }

        int m = methodBindings.length, n = m + groovyMethods.size();
        MethodBinding[] methods = Arrays.copyOf(methodBindings, n);
        for (int i = m, j = 0; i < n; i += 1, j += 1) {
            methods[i] = groovyMethods.get(j);
        }
        return methods;
    }

    private ReferenceBinding getGroovyLangMetaClass() {
        CompilationUnitScope unitScope = compilationUnitScope();
        unitScope.recordQualifiedReference(GroovyCompilationUnitScope.GROOVY_LANG_METACLASS);
        return unitScope.environment.getResolvedType(GroovyCompilationUnitScope.GROOVY_LANG_METACLASS, this);
    }

    private ReferenceBinding getGroovyLangGroovyObject() {
        CompilationUnitScope unitScope = compilationUnitScope();
        unitScope.recordQualifiedReference(GroovyCompilationUnitScope.GROOVY_LANG_GROOVYOBJECT);
        return unitScope.environment.getResolvedType(GroovyCompilationUnitScope.GROOVY_LANG_GROOVYOBJECT, this);
    }

    private ReferenceBinding getGroovyTraitsImplemented() {
        final char[][] groovyTraitsImplemented = CharOperation.splitOn('.',
            "org.codehaus.groovy.transform.trait.Traits$Implemented".toCharArray());

        CompilationUnitScope unitScope = compilationUnitScope();
        unitScope.recordQualifiedReference(groovyTraitsImplemented);
        return unitScope.environment.getResolvedType(groovyTraitsImplemented, this);
    }

    private ReferenceBinding getGroovyTransformInternal() {
        CompilationUnitScope unitScope = compilationUnitScope();
        unitScope.recordQualifiedReference(GroovyCompilationUnitScope.GROOVY_TRANSFORM_INTERNAL);
        return unitScope.environment.getResolvedType(GroovyCompilationUnitScope.GROOVY_TRANSFORM_INTERNAL, this);
    }

    private ReferenceBinding getGroovyTransformGenerated() {
        CompilationUnitScope unitScope = compilationUnitScope();
        unitScope.recordQualifiedReference(GroovyCompilationUnitScope.GROOVY_TRANSFORM_GENERATED);
        return unitScope.environment.getResolvedType(GroovyCompilationUnitScope.GROOVY_TRANSFORM_GENERATED, this);
    }

    private int getModifiers(final PropertyNode propertyNode) {
        int modifiers = (propertyNode.getModifiers() & 0xF);

        if (traitHelper.isTrait(referenceContext.binding)) {
            modifiers |= Flags.AccAbstract;
        }

        if (propertyNode.getType().isUsingGenerics()) {
            modifiers |= ExtraCompilerModifiers.AccGenericSignature;
        }

        // if @PackageScope was detected by GCUD, field's modifiers will show it
        char[] nameChars = propertyNode.getName().toCharArray();
        for (FieldDeclaration field : referenceContext.fields) {
            if (CharOperation.equals(field.name, nameChars)) {
                if (Flags.isPackageDefault(field.modifiers)) {
                    modifiers &= ~Flags.AccPublic;
                }
                break;
            }
        }

        if (propertyNode.getField().getAnnotations().stream().map(anno -> anno.getClassNode().getName())
                .anyMatch(name -> name.equals("Deprecated") || name.equals("java.lang.Deprecated"))) {
            modifiers |= Flags.AccDeprecated;
        }

        return modifiers;
    }

    /**
     * @see MethodBinding#readableName()
     */
    private String getMethodAsString(final MethodBinding methodBinding) {
        StringBuilder key = new StringBuilder();
        key.append(methodBinding.selector).append('(');
        for (TypeBinding tb : methodBinding.parameters) {
            if (tb instanceof ReferenceBinding) {
                key.append(((ReferenceBinding) tb).readableName(false));
            } else if (tb != null) {
                key.append(tb.readableName());
            }
            key.append(';');
        }
        key.append(')');

        return key.toString();
    }

    private boolean isNotActuallyAbstract(final MethodBinding methodBinding, final ReferenceBinding helperBinding) {
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

    private Optional<MethodBinding> createMethod(final String methodName, final TypeBinding[] parameterTypes, final TypeBinding returnType, final MethodBinding[] methodBindings) {
        final char[] nameChars = methodName.toCharArray();
        for (MethodBinding methodBinding : methodBindings) {
            if (CharOperation.equals(nameChars, methodBinding.selector)) {
                ((SourceTypeBinding) methodBinding.declaringClass).resolveTypesFor(methodBinding);
                boolean equalParameters = (parameterTypes.length == methodBinding.parameters.length);
                if (equalParameters) {
                    for (int i = 0, n = parameterTypes.length; i < n; i += 1) {
                        if (!CharOperation.equals(parameterTypes[i].signature(), methodBinding.parameters[i].signature())) {
                            equalParameters = false;
                            break;
                        }
                    }
                }
                if (equalParameters) {
                    return Optional.empty();
                }
            }
        }

        return asInternal(new MethodBinding(Flags.AccPublic, nameChars, returnType, parameterTypes, Binding.NO_EXCEPTIONS, referenceContext.binding));
    }

    private Optional<MethodBinding> createGetterMethod(final PropertyNode propertyNode, final String methodName, final int modifiers, final MethodBinding[] methodBindings) {
        final char[] nameChars = methodName.toCharArray();
        for (MethodBinding methodBinding : methodBindings) {
            if (CharOperation.equals(nameChars, methodBinding.selector)) {
                if ((methodBinding.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
                    Argument[] arguments = methodBinding.sourceMethod().arguments;
                    if (arguments == null || arguments.length == 0) {
                        return Optional.empty();
                    }
                } else {
                    TypeBinding[] parameters = methodBinding.parameters;
                    if (parameters == null || parameters.length == 0) {
                        return Optional.empty();
                    }
                }
            }
        }

        MethodBinding methodBinding;
        TypeReference typeReference = propertyNode.getNodeMetaData(TypeReference.class);

        ClassNode propertyType = propertyNode.getType();
        if (ClassHelper.isPrimitiveType(propertyType) &&
                propertyType.getTypeAnnotations().isEmpty()) {
            TypeBinding returnType = Scope.getBaseType(propertyType.getName().toCharArray());
            methodBinding = new MethodBinding(modifiers, nameChars, returnType, Binding.NO_PARAMETERS, Binding.NO_EXCEPTIONS, referenceContext.binding);
        } else if (typeReference != null) {
            TypeBinding returnType = typeReference.resolveType(this);
            methodBinding = new MethodBinding(modifiers, nameChars, returnType, Binding.NO_PARAMETERS, Binding.NO_EXCEPTIONS, referenceContext.binding);
        } else {
            String propertyName = propertyNode.getName();
            methodBinding = new LazilyResolvedMethodBinding(true, propertyName, modifiers, nameChars, Binding.NO_EXCEPTIONS, referenceContext.binding);
        }
        return traitHelper.isTrait(referenceContext.binding) ? asImplemented(methodBinding) : asGenerated(methodBinding);
    }

    private Optional<MethodBinding> createSetterMethod(final PropertyNode propertyNode, final String methodName, final int modifiers, final MethodBinding[] methodBindings) {
        final char[] nameChars = methodName.toCharArray();
        for (MethodBinding methodBinding : methodBindings) {
            if (CharOperation.equals(nameChars, methodBinding.selector)) {
                if ((methodBinding.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
                    Argument[] arguments = methodBinding.sourceMethod().arguments;
                    if (arguments != null && arguments.length == 1) {
                        // TODO: Check argument type vs property type?
                        return Optional.empty();
                    }
                } else {
                    TypeBinding[] parameters = methodBinding.parameters;
                    if (parameters != null && parameters.length == 1) {
                        // TODO: Check parameter type vs property type?
                        return Optional.empty();
                    }
                }
            }
        }

        MethodBinding methodBinding;
        TypeReference typeReference = propertyNode.getNodeMetaData(TypeReference.class);

        ClassNode propertyType = propertyNode.getType();
        int va = (propertyType.isArray() ? Flags.AccVarargs : 0); // GROOVY-10249: see AsmClassGenerator#visitConstructorOrMethod

        if (ClassHelper.isPrimitiveType(propertyType) &&
                propertyType.getTypeAnnotations().isEmpty()) {
            TypeBinding[] parameterTypes = {Scope.getBaseType(propertyType.getName().toCharArray())};
            methodBinding = new MethodBinding(modifiers, nameChars, TypeBinding.VOID, parameterTypes, Binding.NO_EXCEPTIONS, referenceContext.binding);
        } else if (typeReference != null) {
            TypeBinding[] parameterTypes = {typeReference.resolveType(this)};
            methodBinding = new MethodBinding(modifiers | va, nameChars, TypeBinding.VOID, parameterTypes, Binding.NO_EXCEPTIONS, referenceContext.binding);
        } else {
            String propertyName = propertyNode.getName();
            methodBinding = new LazilyResolvedMethodBinding(false, propertyName, modifiers | va, nameChars, Binding.NO_EXCEPTIONS, referenceContext.binding);
        }
        return traitHelper.isTrait(referenceContext.binding) ? asImplemented(methodBinding) : asGenerated(methodBinding);
    }

    private Optional<MethodBinding> asGenerated  (final MethodBinding methodBinding) {
        AnnotationBinding atGenerated = new AnnotationBinding(getGroovyTransformGenerated(), Binding.NO_ELEMENT_VALUE_PAIRS);
        return asOptional(methodBinding, atGenerated);
    }

    private Optional<MethodBinding> asImplemented(final MethodBinding methodBinding) {
        AnnotationBinding atGenerated = new AnnotationBinding(getGroovyTransformGenerated(), Binding.NO_ELEMENT_VALUE_PAIRS);
        AnnotationBinding atImplemented = new AnnotationBinding(getGroovyTraitsImplemented(), Binding.NO_ELEMENT_VALUE_PAIRS);
        return asOptional(methodBinding, atGenerated, atImplemented);
    }

    private Optional<MethodBinding> asInternal(final MethodBinding methodBinding) {
        AnnotationBinding atGenerated = new AnnotationBinding(getGroovyTransformGenerated(), Binding.NO_ELEMENT_VALUE_PAIRS);
        AnnotationBinding atInternal = new AnnotationBinding(getGroovyTransformInternal(), Binding.NO_ELEMENT_VALUE_PAIRS);
        return asOptional(methodBinding, atGenerated, atInternal);
    }

    private Optional<MethodBinding> asOptional(final MethodBinding methodBinding,
        final AnnotationBinding... annotationBindings) {
        methodBinding.setAnnotations(annotationBindings, false);
        methodBinding.modifiers |= 0x400000; // see JDTClassNode#methodBindingToMethodNode
        methodBinding.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
        return Optional.of(methodBinding);
    }

    @Override
    protected void buildFieldsAndMethods() {
        super.buildFieldsAndMethods();

        if (referenceContext.fields == null) return;
        for (FieldDeclaration field : referenceContext.fields) {
            Expression initialization = field.initialization;
            // unwrap "field = (Type) (Object) new Type() {}"
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
    }

    @Override
    public boolean shouldReport(final int problem) {
        switch (problem) {
        case IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod:
        case IProblem.CannotHideAnInstanceMethodWithAStaticMethod:
        case IProblem.EnumConstantMustImplementAbstractMethod:
        case IProblem.IncorrectArityForParameterizedType:
        case IProblem.InheritedMethodReducesVisibility:
        case IProblem.AbstractMethodMustBeImplemented:
        case IProblem.MissingValueForAnnotationMember:
        case IProblem.SuperInterfaceMustBeAnInterface:
        case IProblem.FinalMethodCannotBeOverridden:
        case IProblem.MethodMustOverrideOrImplement:
        case IProblem.MethodReducesVisibility:
        case IProblem.IncompatibleReturnType:
        case IProblem.SuperclassMustBeAClass:
        case IProblem.HierarchyCircularity:
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

        private boolean isTrait(final ReferenceBinding referenceBinding) {
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

        private ReferenceBinding getHelperBinding(final ReferenceBinding interfaceBinding) {
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
