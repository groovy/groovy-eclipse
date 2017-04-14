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
package org.codehaus.groovy.eclipse.codebrowsing.elements;

import static org.eclipse.jdt.groovy.core.util.ReflectionUtils.executePrivateMethod;

import java.lang.reflect.Method;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.SourceFieldElementInfo;

/**
 * A resolved java element suitable for hovers. Includes extra Javadoc information to appear in the hover.
 */
public class GroovyResolvedSourceField extends ResolvedSourceField implements IGroovyResolvedElement {

    private final String extraDoc;
    private final ASTNode inferredElement;

    public GroovyResolvedSourceField(JavaElement parent, String name, String uniqueKey, String extraDoc, ASTNode inferredElement) {
        super(parent, name, uniqueKey);
        this.extraDoc = extraDoc;
        this.inferredElement = inferredElement;
    }

    public String getExtraDoc() {
        return extraDoc;
    }

    public ASTNode getInferredElement() {
        return inferredElement;
    }

    public String getInferredElementName() {
        if (inferredElement instanceof Variable) {
            return ((Variable) inferredElement).getName();
        } else if (inferredElement instanceof MethodNode) {
            return ((MethodNode) inferredElement).getName();
        } else if (inferredElement instanceof ClassNode) {
            return ((ClassNode) inferredElement).getName();
        }
        return inferredElement.getText();
    }

    public Object getElementInfo() throws JavaModelException {
        return isTraitField(inferredElement) ? newSourceFieldElementInfo((FieldNode) inferredElement) : super.getElementInfo();
    }

    /**
     * @see org.codehaus.groovy.transform.trait.Traits#isTrait(ClassNode)
     */
    private static boolean isTraitField(ASTNode node) {
        if (node instanceof FieldNode) {
            ClassNode cNode = ((FieldNode) node).getDeclaringClass();
            return (null != cNode.getNodeMetaData("trait.fields"));
            /*if (cNode.isInterface()) {
                List<AnnotationNode> aNodes = cNode.getAnnotations();
                if (aNodes != null && !aNodes.isEmpty()) {
                    for (AnnotationNode aNode : aNodes) {
                        String aName = aNode.getClassNode().getName();
                        if (aName.equals("groovy.transform.Trait")) {
                            return true;
                        }
                    }
                }
            }*/
        }
        return false;
    }

    private static SourceFieldElementInfo newSourceFieldElementInfo(FieldNode node) {
        SourceFieldElementInfo info = new SourceFieldElementInfo();
        setInt("NameSourceStart", info, node.getNameStart());
        setInt("NameSourceEnd", info, node.getNameEnd());
        setInt("SourceRangeStart", info, node.getStart());
        setInt("SourceRangeEnd", info, node.getEnd());
        setInt("Flags", info, node.getModifiers());

        executePrivateMethod(SourceFieldElementInfo.class, "setTypeName", new Class[] {char[].class}, info, new Object[] {
            JavaModelManager.getJavaModelManager().intern(node.getType().getNameWithoutPackage().toCharArray())});
        //acceptAnnotation(annotation, info, handle);

        return info;
    }

    private static void setInt(String property, Object target, int value) {
        String setter = "set" + property;
        Class<?> clazz = target.getClass();
        Method method = null;
        do {
            try {
                method = clazz.getDeclaredMethod(setter, int.class);
                method.setAccessible(true);
            } catch (NoSuchMethodException ex) {
            }
        } while (method == null && (clazz = clazz.getSuperclass()) != null);

        try {
            method.invoke(target, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
