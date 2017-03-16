/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import static org.eclipse.jdt.core.Signature.createTypeSignature;
import static org.eclipse.jdt.core.Signature.getSimpleName;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.SourceUnit;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Helper methods - can be made more eclipse friendly or replaced if the groovy infrastructure provides the information.
 */
public abstract class GroovyUtils {

    public static char[] readSourceRange(SourceUnit unit, int offset, int length) {
        Reader reader = null;
        try {
            reader = unit.getSource().getReader();
            reader.skip(offset);
            int n = length;
            final char[] code = new char[n];
            while (n > 0) {
                n -= reader.read(code, length - n, n);
            }
            return code;
        } catch (Exception e) {
            Util.log(e);
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException ignored) {}
        }
        return null;
    }

    // FIXASC don't use this any more?
    public static int[] getSourceLineSeparatorsIn(char[] code) {
        List<Integer> lineSeparatorsCollection = new ArrayList<Integer>();
        for (int i = 0, max = code.length; i < max; i++) {
            if (code[i] == '\r') {
                if ((i + 1) < max && code[i + 1] == '\n') {// \r\n
                    lineSeparatorsCollection.add(i + 1); // add the position of the \n
                    i++;
                } else {
                    lineSeparatorsCollection.add(i); // add the position of the \r
                }
            } else if (code[i] == '\n') {
                lineSeparatorsCollection.add(i);
            }
        }
        int[] lineSepPositions = new int[lineSeparatorsCollection.size()];
        for (int i = 0; i < lineSeparatorsCollection.size(); i++) {
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
        @SuppressWarnings("unchecked")
        Iterable<AnnotationNode> more =
            (Iterable<AnnotationNode>) node.getNodeMetaData("AnnotationCollector");
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
        Long offsets = (Long) node.getNodeMetaData("source.offsets");
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
        Long offsets = (Long) node.getNodeMetaData("source.offsets");
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

    public static ClassNode getBaseType(ClassNode node) {
        while (node.isArray()) node = node.getComponentType();
        return node;
    }

    public static List<ClassNode> getParameterTypes(Parameter... params) {
        final int n = params.length;
        if (n == 0) return Collections.emptyList();
        List<ClassNode> types = new ArrayList<ClassNode>(n);
        for (Parameter param : params) {
            types.add(param.getType());
        }
        return types;
    }

    /**
     * Creates a type signature string for the specified class node, including
     * its generics if any are present.
     *
     * @see org.eclipse.jdt.core.Signature
     */
    public static String getTypeSignature(ClassNode node, boolean qualified) {
        StringBuilder builder = new StringBuilder();
        while (node.isArray()) {
            builder.append('[');
            node = node.getComponentType();
        }
        String name = qualified ? node.getName() : getSimpleName(node.getName());
        assert !name.startsWith("[") && !name.contains("<") && !name.endsWith(";");

        builder.append(createTypeSignature(name, false));

        if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
            builder.setCharAt(builder.length() - 1, '<');
            for (GenericsType gen : node.getGenericsTypes()) {
                if (gen.isPlaceholder()) {
                    builder.append(createTypeSignature(gen.getName(), false));
                    // TODO: Can lower bound or upper bounds exist in this case?
                } else if (!gen.isWildcard()) {
                    builder.append(getTypeSignature(gen.getType(), qualified));
                } else if (gen.getLowerBound() != null) {
                    builder.append('-').append(getTypeSignature(gen.getLowerBound(), qualified));
                } else if (gen.getUpperBounds() != null && gen.getUpperBounds().length > 0) {
                    builder.append('+').append(getTypeSignature(gen.getUpperBounds()[0], qualified));
                } else {
                    builder.append('*');
                }
            }
            builder.append(">;");
        }

        return builder.toString();
    }

    public static ClassNode getWrapperTypeIfPrimitive(ClassNode type) {
        if (ClassHelper.isPrimitiveType(type)) {
            return ClassHelper.getWrapper(type);
        }
        return type;
    }

    public static void updateClosureWithInferredTypes(ClassNode closure, ClassNode returnType, Parameter[] parameters) {
        if (!"groovy.lang.Closure".equals(closure.getName())) {
            return;
        }

        ClassNode redir =
            closure.redirect();
        closure.setRedirect(null);
        closure.setInterfaces(redir.getInterfaces());
        returnType = getWrapperTypeIfPrimitive(returnType);
        closure.setGenericsTypes(new GenericsType[] {new GenericsType(returnType)});
        ReflectionUtils.setPrivateField(ClassNode.class, "clazz", closure, redir.getTypeClass());
    }
}
