/*
 * Copyright 2009-2018 the original author or authors.
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTNode;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.util.Util;
import org.osgi.framework.Version;

/**
 * Helper methods - can be made more eclipse friendly or replaced if the groovy infrastructure provides the information.
 */
public class GroovyUtils {

    private GroovyUtils() {}

    public static Version getGroovyVersion() {
        String version = GroovySystem.getVersion();
        // convert "2.5.0-beta-2" -> "2.5.0.beta-2"
        version = version.replaceFirst("-", ".");
        return new Version(version);
    }

    // TODO: Replace with SourceUnit.readSourceRange when Groovy 2.5 is the minimum supported runtime.
    public static char[] readSourceRange(SourceUnit unit, int offset, int length) {
        try (Reader reader = unit.getSource().getReader()) {
            reader.skip(offset);
            int n = length;
            final char[] code = new char[n];
            while (n > 0) {
                n -= reader.read(code, length - n, n);
            }
            return code;
        } catch (Exception e) {
            Util.log(e);
        }
        return null;
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
     * @return position of '@' (or best approximation) for specified annotation
     */
    public static int startOffset(AnnotationNode node) {
        int start = -1;
        Long offsets = node.getNodeMetaData("source.offsets");
        if (offsets != null) {
            start = (int) (offsets >> 32);
        } else if (node.getEnd() > 0) {
            start = node.getStart() - 1;
        }
        return start;
    }

    /**
     * @return position of ')' (or best approximation) for specified annotation
     */
    public static int endOffset(AnnotationNode node) {
        int end = -1;
        Long offsets = node.getNodeMetaData("source.offsets");
        if (offsets != null) {
            end = (int) (offsets & 0xFFFFFFFF);
        } else {
            end = lastElement(node).getEnd() + 1;
        }
        return end;
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
        GenericsType[] generics = getBaseType(classNode).getGenericsTypes();
        if (generics == null) return GenericsType.EMPTY_ARRAY;
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
        String[] signatures = new String[types.size()];
        for (int i = 0; i < types.size(); i += 1) {
            signatures[i] = getTypeSignatureWithoutGenerics(types.get(i), true, resolved);
        }
        return signatures;
    }

    public static Set<ASTNode> getTransformNodes(ClassNode classNode, Class<? extends ASTTransformation> xformType) {
        CompilePhase phase = xformType.getAnnotation(GroovyASTTransformation.class).phase();
        Map<?, Set<ASTNode>> map = classNode.getTransforms(phase);
        Set<ASTNode> nodes = map.get(xformType);

        return nodes != null ? Collections.unmodifiableSet(nodes) : Collections.EMPTY_SET;
    }

    public static ClassNode[] getTypeParameterBounds(ClassNode typeParam) {
        if (typeParam.isGenericsPlaceHolder()) { assert typeParam.isUsingGenerics();
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
        StringBuilder builder = new StringBuilder(getTypeSignatureWithoutGenerics(node, qualified, resolved));

        if (getBaseType(node).isUsingGenerics() && !getBaseType(node).isGenericsPlaceHolder()) {
            GenericsType[] generics = getGenericsTypes(node);
            if (generics.length > 0) {
                builder.setCharAt(builder.length() - 1, Signature.C_GENERIC_START);
                for (GenericsType gen : generics) {
                    if (gen.isPlaceholder() || !gen.isWildcard()) {
                        // TODO: Can lower bound or upper bounds exist in this case?
                        builder.append(getTypeSignature(gen.getType(), qualified, resolved));
                    } else if (gen.getLowerBound() != null) {
                        builder.append(Signature.C_SUPER).append(getTypeSignature(gen.getLowerBound(), qualified, resolved));
                    } else if (gen.getUpperBounds() != null && gen.getUpperBounds().length > 0) { // TODO: handle more than one
                        builder.append(Signature.C_EXTENDS).append(getTypeSignature(gen.getUpperBounds()[0], qualified, resolved));
                    } else {
                        builder.append(Signature.C_STAR);
                    }
                }
                builder.append(Signature.C_GENERIC_END).append(Signature.C_NAME_END);
            }
        }

        return builder.toString();
    }

    public static String getTypeSignatureWithoutGenerics(ClassNode node, boolean qualified, boolean resolved) {
        StringBuilder builder = new StringBuilder();
        while (node.isArray()) {
            builder.append('[');
            node = node.getComponentType();
        }

        String name = node.getName();
        if (node.isGenericsPlaceHolder()) {
            // use "T" instead of "Object"
            name = node.getUnresolvedName();
        } else if (!qualified) {
            name = node.getNameWithoutPackage();
            if (name.indexOf('$') > 0) {
                name = node.getUnresolvedName();
            }
        }
        assert !name.startsWith("[") && !name.contains("<") && !name.endsWith(";");

        final int pos = builder.length();
        builder.append(Signature.createTypeSignature(name, resolved));
        if (resolved && node.isGenericsPlaceHolder()) builder.setCharAt(pos, 'T');

        return builder.toString();
    }

    public static ClassNode getWrapperTypeIfPrimitive(ClassNode type) {
        if (ClassHelper.isPrimitiveType(type)) {
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

    public static Expression getTraitFieldExpression(MethodCallExpression call) {
        if (call.getObjectExpression().getType().getName().endsWith("$Trait$FieldHelper")) {
            Matcher m = Pattern.compile(".+__(\\p{javaJavaIdentifierPart}+)\\$[gs]et").matcher(call.getMethodAsString());
            if (m.matches()) {
                String fieldName = m.group(1);
                List<FieldNode> traitFields = call.getObjectExpression().getType().getOuterClass().getNodeMetaData("trait.fields");
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

    public static boolean isAnonymous(ClassNode node) {
        return (node instanceof InnerClassNode && ((InnerClassNode) node).isAnonymous());
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
        /*if (source.hasClass() && target.hasClass()) {
            // this matches primitives more thoroughly, but getTypeClass can fail if class has not been loaded
            result = MetaClassHelper.isAssignableFrom(target.getTypeClass(), source.getTypeClass());
        } else*/ if (target.isInterface()) {
            result = source.equals(target) || source.implementsInterface(target);
        } else if (array) {
            // Object or Object[] is universal receiver for an array
            result = ClassHelper.OBJECT_TYPE.equals(target) || source.isDerivedFrom(target);
        } else {
            result = getWrapperTypeIfPrimitive(source).isDerivedFrom(getWrapperTypeIfPrimitive(target));
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

        int flags = 0;
        if (node instanceof ClassNode) {
            flags = ((ClassNode) node).getModifiers();
        } else if (node instanceof FieldNode) {
            flags = ((FieldNode) node).getModifiers();
        } else if (node instanceof MethodNode) {
            flags = ((MethodNode) node).getModifiers();
        }
        if ((flags & ClassNode.ACC_DEPRECATED) != 0) {
            return true;
        }

        if (flags > 0 || node instanceof AnnotatedNode) {
            return getAnnotations((AnnotatedNode) node, "java.lang.Deprecated").anyMatch(x -> true);
        }

        return false;
    }

    public static boolean isSynthetic(FieldNode node) {
        return (node.getModifiers() & FieldNode.ACC_SYNTHETIC) != 0;
    }

    public static boolean isSynthetic(MethodNode node) {
        return (node.getModifiers() & MethodNode.ACC_SYNTHETIC) != 0;
    }

    public static void updateClosureWithInferredTypes(ClassNode closure, ClassNode returnType, Parameter[] parameters) {
        if (!"groovy.lang.Closure".equals(closure.getName()) || closure == closure.redirect()) {
            return;
        }
        closure.setGenericsTypes(new GenericsType[] {new GenericsType(getWrapperTypeIfPrimitive(returnType))});
    }
}
