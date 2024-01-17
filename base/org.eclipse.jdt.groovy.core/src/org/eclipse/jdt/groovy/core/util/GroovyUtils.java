/*
 * Copyright 2009-2024 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import groovy.lang.GroovySystem;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImmutableClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTNode;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;
import org.osgi.framework.Version;

/**
 * Helper methods - can be made more eclipse friendly or replaced if the groovy infrastructure provides the information.
 */
public class GroovyUtils {

    private GroovyUtils() {}

    public static Version getGroovyVersion() {
        String version = GroovySystem.getVersion();
        // convert "3.0.0-beta-2" -> "3.0.0.beta-2"
        version = version.replaceFirst("-", ".");
        return Version.valueOf(version);
    }

    // FIXASC don't use this any more?
    public static int[] getSourceLineSeparatorsIn(char[] code) {
        List<Integer> lineSeparatorsCollection = new ArrayList<>();
        for (int i = 0, max = code.length; i < max; i += 1) {
            if (code[i] == '\r') {
                if ((i + 1) < max && code[i + 1] == '\n') {// \r\n
                    lineSeparatorsCollection.add(i + 1); // add the position of the \n
                    i += 1;
                } else {
                    lineSeparatorsCollection.add(i); // add the position of the \r
                }
            } else if (code[i] == '\n') {
                lineSeparatorsCollection.add(i);
            }
        }
        int[] lineSepPositions = new int[lineSeparatorsCollection.size()];
        for (int i = 0; i < lineSeparatorsCollection.size(); i += 1) {
            lineSepPositions[i] = lineSeparatorsCollection.get(i);
        }
        return lineSepPositions;
    }

    /**
     * Finds the rightmost node given an annotation.  This could be the node
     * itself, its source annotation (in the case of an annotation collector),
     * or the last member expression.
     */
    public static ASTNode lastElement(AnnotationNode node) {
        ASTNode result = node;
        Iterable<AnnotationNode> more = node.getNodeMetaData("AnnotationCollector");
        if (more != null) {
            for (AnnotationNode an : more) {
                result = an;
            }
        }
        if (((AnnotationNode) result).getMembers() != null) {
            for (Expression expr : ((AnnotationNode) result).getMembers().values()) {
                expr = ClassCodeVisitorSupport.getNonInlinedExpression(expr);
                if (expr.getEnd() > result.getEnd()) {
                    result = expr;
                }
            }
        }
        return result;
    }

    /**
     * @return qualifier and type name
     */
    public static String[] splitName(ClassNode node) {
        String name = node.getName();
        int index = name.lastIndexOf('$');
        if (index == -1) index = name.lastIndexOf('.');
        return new String[] {name.substring(0, Math.max(0, index)), name.substring(index + 1)};
    }

    public static Set<ClassNode> getAllInterfaces(ClassNode node) {
        Set<ClassNode> result = new LinkedHashSet<>();
        if (node.isInterface()) result.add(node);
        addAllInterfaces(result, node);
        return result;
    }

    private static void addAllInterfaces(Set<ClassNode> result, ClassNode source) {
        for (ClassNode face : source.getInterfaces()) {
            face = GenericsUtils.parameterizeType(source, face);
            if (result.add(face)) addAllInterfaces(result, face);
        }
        ClassNode sc = source.redirect().getUnresolvedSuperClass(false);
        if (sc != null && !sc.equals(ClassHelper.OBJECT_TYPE)) {
            addAllInterfaces(result, GenericsUtils.parameterizeType(source, sc));
        }
    }

    public static Stream<AnnotationNode> getAnnotations(AnnotatedNode node, String name) {
        return node.getAnnotations().stream().filter(an -> an.getClassNode().getName().equals(name));
    }

    public static ClassNode getBaseType(ClassNode classNode) {
        while (classNode.isArray()) {
            classNode = classNode.getComponentType();
        }
        return classNode;
    }

    public static GenericsType[] getGenericsTypes(ClassNode classNode) {
        classNode = getBaseType(classNode); // discard array(s)
        GenericsType[] generics = classNode.getGenericsTypes();
        if (generics == null ||
                classNode.isGenericsPlaceHolder() ||
                classNode instanceof LowestUpperBoundClassNode) {
            generics = GenericsType.EMPTY_ARRAY;
        }
        return generics;
    }

    public static GenericsType[] getGenericsTypes(MethodNode methodNode) {
        GenericsType[] generics = methodNode.getGenericsTypes();
        if (generics == null) return GenericsType.EMPTY_ARRAY;
        return generics;
    }

    public static List<ClassNode> getParameterTypes(Parameter... params) {
        final int n = params.length;
        if (n == 0) return Collections.emptyList();
        List<ClassNode> types = new ArrayList<>(n);
        for (Parameter param : params) {
            types.add(param.getType());
        }
        return types;
    }

    public static String[] getParameterTypeSignatures(MethodNode methodNode, boolean resolved) {
        List<ClassNode> types = getParameterTypes(methodNode.getParameters());
        return types.stream().map(type ->
            getTypeSignatureWithoutGenerics(type, true, resolved)
        ).toArray(String[]::new);
    }

    public static Set<ASTNode> getTransformNodes(ClassNode classNode, Class<? extends ASTTransformation> xformType) {
        CompilePhase phase = xformType.getAnnotation(GroovyASTTransformation.class).phase();
        Map<?, Set<ASTNode>> map = classNode.getTransforms(phase);
        Set<ASTNode> nodes = map.get(xformType);

        return nodes != null ? Collections.unmodifiableSet(nodes) : Collections.EMPTY_SET;
    }

    public static ClassNode[] getTypeParameterBounds(ClassNode typeParam) {
        if (typeParam.isGenericsPlaceHolder()) {
            GenericsType[] generics = typeParam.getGenericsTypes();
            if (generics != null && generics.length > 0) {
                ClassNode[] bounds = generics[0].getUpperBounds();
                if (bounds != null) {
                    return bounds;
                }
            }
        }
        return ClassNode.EMPTY_ARRAY;
    }

    /**
     * Creates a type signature string for the specified class node, including
     * its generics if any are present.
     *
     * @see org.eclipse.jdt.core.Signature
     */
    public static String getTypeSignature(ClassNode node, boolean qualified, boolean resolved) {
        ClassNode baseType = getBaseType(node);
        if (baseType.getName().startsWith("<UnionType:") ||
                (baseType instanceof LowestUpperBoundClassNode &&
                !baseType.getUnresolvedName().startsWith("CommonAssignOf$"))) {
            return getUnionTypeSignature(node, type -> getTypeSignature(type, qualified, resolved));
        }
        if (baseType instanceof IntersectionType) {
            return getIntersectionTypeSignature(node, type -> getTypeSignature(type, qualified, resolved));
        }

        String signature = getTypeSignatureWithoutGenerics(node, qualified, resolved);

        GenericsType[] generics = getGenericsTypes(baseType);
        if (generics.length > 0) {
            StringBuilder builder = new StringBuilder(signature);

            builder.setCharAt(builder.length() - 1, Signature.C_GENERIC_START);
            for (GenericsType gt : generics) {
                if (gt.isPlaceholder() || !gt.isWildcard()) {
                    // TODO: Can lower bound or upper bounds exist in this case?
                    builder.append(getTypeSignature(gt.getType(), qualified, resolved));
                } else if (gt.getLowerBound() != null) {
                    builder.append(Signature.C_SUPER).append(getTypeSignature(gt.getLowerBound(), qualified, resolved));
                } else if (gt.getUpperBounds() != null && gt.getUpperBounds().length > 0) {
                    builder.append(Signature.C_EXTENDS).append(getTypeSignature(gt.getUpperBounds()[0], qualified, resolved));
                } else {
                    builder.append(Signature.C_STAR);
                }
            }
            builder.append(Signature.C_GENERIC_END).append(Signature.C_NAME_END);

            return builder.toString();
        }

        return signature;
    }

    public static String getTypeSignatureWithoutGenerics(ClassNode node, boolean qualified, boolean resolved) {
        ClassNode baseType = getBaseType(node);
        if (baseType.getName().startsWith("<UnionType:") ||
                (baseType instanceof LowestUpperBoundClassNode &&
                !baseType.getUnresolvedName().startsWith("CommonAssignOf$"))) {
            return getUnionTypeSignature(node, type -> getTypeSignatureWithoutGenerics(type, qualified, resolved));
        }
        if (baseType instanceof IntersectionType) {
            return getIntersectionTypeSignature(node, type -> getTypeSignatureWithoutGenerics(type, qualified, resolved));
        }

        StringBuilder builder = new StringBuilder();
        while (node.isArray()) {
            builder.append('[');
            node = node.getComponentType();
        }

        String name;
        if (node.isGenericsPlaceHolder()) {
            // use "T" instead of "Object"
            name = node.getUnresolvedName();
        } else if (resolved || node.getOuterClass() == null) {
            name = qualified ? node.getName() : node.getNameWithoutPackage();
        } else {
            List<ClassNode> nodes = node.getOuterClasses();
            nodes.add(0, node);
            int i = nodes.size();
            StringBuilder sb = new StringBuilder(nodes.get(--i).getName());
            while (i != 0) {
                sb.append('.').append(nodes.get(--i).getName().substring(sb.length()));
            }
            if (!qualified && node.getPackageName() != null) sb.delete(0, node.getPackageName().length() + 1);

            name = sb.toString();
        }
        assert !name.startsWith("[") && !name.contains("<") && !name.endsWith(";");

        final int pos = builder.length();
        builder.append(Signature.createTypeSignature(name, resolved));
        if (resolved && node.isGenericsPlaceHolder()) {
            if (node.getDeclaringClass() != null) // type parameter provenance
                return getTypeSignatureWithoutGenerics(node.getDeclaringClass(), qualified, true) +
                            Signature.C_COLON + Signature.C_TYPE_VARIABLE + builder.substring(1);
            builder.setCharAt(pos, Signature.C_TYPE_VARIABLE); // replace the "L" or "Q" with "T"
        }
        return builder.toString();
    }

    private static String getUnionTypeSignature(ClassNode node, java.util.function.Function<ClassNode, String> signer) {
        StringBuilder builder = new StringBuilder();
        while (node.isArray()) {
            builder.append('[');
            node = node.getComponentType();
        }

        ClassNode[] types = node instanceof LowestUpperBoundClassNode
                            ? node.asGenericsType().getUpperBounds() // non-Object super and interface(s)
                            : ReflectionUtils.executePrivateMethod(node.getClass(), "getDelegates", node);
        String signature = Signature.createUnionTypeSignature(Stream.of(types).map(signer).toArray(String[]::new));

        return builder.append(signature).toString();
    }

    private static String getIntersectionTypeSignature(ClassNode node, java.util.function.Function<ClassNode, String> signer) {
        StringBuilder builder = new StringBuilder();
        while (node.isArray()) {
            builder.append('[');
            node = node.getComponentType();
        }

        Stream<ClassNode> types = ((IntersectionType) node).types.stream();
        String signature = Signature.createIntersectionTypeSignature(types.map(signer).toArray(String[]::new));

        return builder.append(signature).toString();
    }

    public static ClassNode getWrapperTypeIfPrimitive(ClassNode type) {
        if (type != null && ClassHelper.isPrimitiveType(type) && !ClassHelper.VOID_TYPE.equals(type)) {
            return ClassHelper.getWrapper(type);
        }
        return type;
    }

    public static List<ImportNode> getAllImportNodes(ModuleNode moduleNode) {
        List<ImportNode> importNodes = new ArrayList<>();

        importNodes.addAll(moduleNode.getImports());
        importNodes.addAll(moduleNode.getStarImports());
        importNodes.addAll(moduleNode.getStaticImports().values());
        importNodes.addAll(moduleNode.getStaticStarImports().values());

        // order imports by source position
        Collections.sort(importNodes, Comparator.comparing(ImportNode::getEnd));

        return importNodes;
    }

    public static MethodNode getAnnotationMethod(AnnotationNode node, String methodName) {
        MethodNode meth = node.getClassNode().getMethod(methodName, Parameter.EMPTY_ARRAY);
        if (meth != null) {
            return meth;
        }

        List<AnnotationNode> nodes = node.getNodeMetaData("AnnotationCollectorTransform");
        if (nodes != null) {
            for (AnnotationNode aliased : nodes) {
                meth = getAnnotationMethod(aliased, methodName);
                if (meth != null) {
                    return meth;
                }
            }
        }

        return null;
    }

    public static MethodNode getMethod(ClassNode declaringType, String methodName, Parameter... parameters) {
        MethodNode meth = declaringType.getMethod(methodName, parameters);
        if (meth != null) {
            return meth;
        }
        // concrete types (without mixins/traits) return all methods from getMethod(String, Parameter[])
        if (declaringType.isAbstract() || declaringType.isInterface() || implementsTrait(declaringType)) {
            Set<ClassNode> done = new HashSet<>(Collections.singleton(declaringType));
            java.util.Queue<ClassNode> todo = new LinkedList<>();
            ClassNode type = declaringType;
            do {
                ClassNode base = type.getSuperClass();
                if (base != null) {
                    if (!done.contains(base)) {
                        todo.add(base);
                    }
                }
                for (ClassNode face : type.getInterfaces()) {
                    if (!done.contains(face)) {
                        todo.add(face);
                    }
                }
                type = todo.peek();
                if (type != null) {
                    meth = type.getMethod(methodName, parameters);
                    if (meth != null) {
                        return meth;
                    }
                    done.add(todo.remove());
                }
            } while (type != null);
        }

        return null;
    }

    public static Expression getTraitFieldExpression(MethodCallExpression call) {
        Expression objExpr = call.getObjectExpression();
        if (objExpr instanceof TernaryExpression) {
            objExpr = ((TernaryExpression) objExpr).getTrueExpression();
        }
        if (objExpr.getType().getName().endsWith("$Trait$FieldHelper")) {
            Matcher m = Pattern.compile(".+__(\\p{javaJavaIdentifierPart}+)\\$[gs]et").matcher(call.getMethodAsString());
            if (m.matches()) {
                String fieldName = m.group(1);
                List<FieldNode> traitFields = objExpr.getType().getOuterClass().getNodeMetaData("trait.fields");
                for (FieldNode field : traitFields) {
                    if (field.getName().equals(fieldName)) {
                        VariableExpression expr = new VariableExpression(field);
                        expr.setSourcePosition(call);
                        return expr;
                    }
                }
            }
        }
        return null;
    }

    public static boolean implementsTrait(ClassNode concreteType) {
        return concreteType.getNodeMetaData(Traits.class, x -> {
            ClassNode type = concreteType.redirect();
            do {
                if (Traits.isTrait(type) || Stream.of(type.getInterfaces()).anyMatch(Traits::isTrait)) {
                    return Boolean.TRUE;
                }
                type = type.getSuperClass();
            } while (type != null && type != ClassHelper.OBJECT_TYPE);
            return Boolean.FALSE;
        }).booleanValue();
    }

    public static ClassNode intersect(List<ClassNode> types) {
        assert types != null && types.size() > 1;
        return new IntersectionType(types);
    }

    public static boolean isAnonymous(ClassNode node) {
        if (node instanceof InnerClassNode) {
            return ((InnerClassNode) node).isAnonymous();
        }
        if (node != null && node.redirect() instanceof JDTClassNode) {
            return ((JDTClassNode) node.redirect()).isAnonymous();
        }
        return false;
    }

    /**
     * Determines if a value or reference of given source type can be assigned
     * to a receiver of given target type.  Excludes checks for the null value,
     * certain Groovy coercions, etc.
     *
     * @see org.codehaus.groovy.classgen.Verifier#isAssignable(ClassNode, ClassNode)
     * @see org.codehaus.groovy.runtime.MetaClassHelper#isAssignableFrom(Class, Class)
     * @see org.eclipse.jdt.groovy.search.SimpleTypeLookup#isTypeCompatible(ClassNode, ClassNode)
     */
    public static boolean isAssignable(ClassNode source, ClassNode target) {
        return isAssignable(false, source, target);
    }

    private static boolean isAssignable(boolean array, ClassNode source, ClassNode target) {
        if (source.isArray() && target.isArray()) {
            return isAssignable(true, source.getComponentType(), target.getComponentType());
        }
        if ((source.isArray() && (!target.equals(ClassHelper.OBJECT_TYPE) && !target.isGenericsPlaceHolder())) || target.isArray()) {
            return false;
        }

        boolean result;
        if (target.isInterface()) {
            result = GeneralUtils.isOrImplements(source, target);
        } else if (array) {
            if (target.isGenericsPlaceHolder() || target.equals(ClassHelper.OBJECT_TYPE)) {
                // T[] or Object[] cannot accept a primitive array
                result = !ClassHelper.isPrimitiveType(source);
            } else {
                result = source.isDerivedFrom(target);
            }
        } else {
            if (source.redirect() instanceof ImmutableClassNode && target.redirect() instanceof ImmutableClassNode) {
                // this matches primitives more thoroughly, but getTypeClass fails if class isn't loaded
                result = MetaClassHelper.isAssignableFrom(target.getTypeClass(), source.getTypeClass());
            } else {
                result = getWrapperTypeIfPrimitive(source).isDerivedFrom(target);
            }
        }

        // if target is like <T extends A & B>, check source against B
        final ClassNode[] bounds = getTypeParameterBounds(target);
        for (int i = 1; i < bounds.length && result; i += 1) {
            result = isAssignable(array, source, bounds[i]);
        }

        return result;
    }

    public static boolean isDeprecated(ASTNode node) {
        if (node instanceof PropertyNode && ((PropertyNode) node).getField() != null) {
            // use the associated field because properties are never the declaration
            node = ((PropertyNode) node).getField();
        } else if (node instanceof ClassNode) {
            node = ((ClassNode) node).redirect();
        }

        if (node instanceof JDTNode) {
            return ((JDTNode) node).isDeprecated();
        }

        // Object is only common parent of ASTNode and Variable; DynamicVariable is one example of non-node type
        Object o = node instanceof VariableExpression ? ((VariableExpression) node).getAccessedVariable() : node;

        int flags = 0;
        if (o instanceof Variable) {
            flags = ((Variable) o).getModifiers();
        } else if (o instanceof ClassNode) {
            flags = ((ClassNode) o).getModifiers();
        } else if (o instanceof MethodNode) {
            flags = ((MethodNode) o).getModifiers();
        }

        if (Flags.isDeprecated(flags)) {
            return true;
        }
        if (!(o instanceof AnnotatedNode)) {
            return false;
        }
        return getAnnotations((AnnotatedNode) node, "java.lang.Deprecated").anyMatch(x -> true);
    }

    public static boolean isScript(ClassNode node) {
        return !node.isAbstract() && node.isScript();
    }

    public static boolean isSynthetic(FieldNode node) {
        return Flags.isSynthetic(node.getModifiers());
    }

    public static boolean isSynthetic(MethodNode node) {
        return Flags.isSynthetic(node.getModifiers());
    }

    public static boolean isUsingGenerics(MethodNode node) {
        if (getGenericsTypes(node).length > 0) {
            return true;
        }
        if (!node.isStatic() && node.getDeclaringClass().isUsingGenerics()) {
            Stream<ClassNode> types = Stream.concat(Stream.of(node.getReturnType()),
                                        getParameterTypes(node.getParameters()).stream());
            return types.anyMatch(type -> type.isUsingGenerics() || type.isGenericsPlaceHolder());
        }
        return false;
    }

    public static ClassNode makeType(String typeNameWithoutGenerics) {
        int i = typeNameWithoutGenerics.lastIndexOf('[');
        if (i < 0) {
            return ClassHelper.make(typeNameWithoutGenerics);
        }
        return makeType(typeNameWithoutGenerics.substring(0, i)).makeArray();
    }

    public static void updateClosureWithInferredTypes(ClassNode closureType, ClassNode returnType, Parameter[] parameters) {
        if (closureType.getName().equals("groovy.lang.Closure") && closureType.isRedirectNode()) {
            closureType.setGenericsTypes(new GenericsType[] {getWrapperTypeIfPrimitive(returnType).asGenericsType()});
        }
    }
}
