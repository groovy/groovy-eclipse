/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.eclipse.codebrowsing.elements.IGroovyResolvedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.debug.ui.JavaDebugHover;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

/**
 * Overrides the JavadocHover to allow Groovy elements to contribute custom
 * pieces to the doc hovers. The hover is non-null only if the element being
 * hovered over is an {@link IGroovyResolvedElement} and has a non-empty extraDoc.
 * <p>
 * If this hover is used, then due to ordering problems with content assist
 * hovers, this hover will override the variable info hover.
 */
public class GroovyExtraInformationHover extends JavadocHover {

    private final JavaDebugHover debugHover = new JavaDebugHover();

    private final boolean alwaysReturnInformation;

    public GroovyExtraInformationHover() {
        this(false);
    }

    public GroovyExtraInformationHover(final boolean alwaysReturnInformation) {
        this.alwaysReturnInformation = alwaysReturnInformation;
    }

    @Override
    public void setEditor(final IEditorPart editor) {
        super.setEditor(editor);
        debugHover.setEditor(editor);
    }

    @Override
    public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
        IEditorPart editor = getEditor();
        if (editor == null) {
            return null;
        }
        IFile file = Adapters.adapt(editor.getEditorInput(), IFile.class);
        if (file == null) {
            return null;
        }
        if (!ContentTypeUtils.isGroovyLikeFileName(file.getName())) {
            return null;
        }

        // first check to see if there would be a debug hover
        // if so, don't do any more work
        if (!alwaysReturnInformation) {
            Object o = debugHover.getHoverInfo2(textViewer, hoverRegion);
            if (o != null) {
                // don't actually return anything since we
                // want the real debug hover to do the actual work
                return null;
            }
        }

        IJavaElement[] elements = getJavaElementsAt(textViewer, hoverRegion);
        if (shouldComputeHover(elements)) {
            // might be null and if so, punt to the JavadocHover
            return computeHover(hoverRegion, elements);
        } else {
            return null;
        }
    }

    /**
     * Only compute hover if thie is an {@link IGroovyResolvedElement} that has
     * an extraDoc.
     */
    private boolean shouldComputeHover(final IJavaElement[] elements) {
        return (elements != null && elements.length == 1 && (alwaysReturnInformation ||
            (elements[0] instanceof IGroovyResolvedElement && isNotBlank(((IGroovyResolvedElement) elements[0]).getExtraDoc()))));
    }

    private static boolean isNotBlank(final String string) {
        return (string != null && !string.trim().isEmpty());
    }

    /**
     * Possibly compute the hover. Might return null.
     */
    private Object computeHover(final IRegion hoverRegion, final IJavaElement[] elements) {
        Class<?>[] types = {IJavaElement[].class, ITypeRoot.class, IRegion.class, JavadocBrowserInformationControlInput.class};
        Object[] values = {elements, getEditorInputJavaElement(), hoverRegion, null};

        JavadocBrowserInformationControlInput hover = ReflectionUtils.executePrivateMethod(JavadocHover.class, "getHoverInfo", types, null, values);
        if (hover != null && elements[0] instanceof IGroovyResolvedElement) {
            hover = new JavadocBrowserInformationControlInput(
                (JavadocBrowserInformationControlInput) hover.getPrevious(), hover.getElement(),
                wrapHTML(hover, (IGroovyResolvedElement) elements[0]), hover.getLeadingImageWidth());
        }
        return hover;
    }

    protected String wrapHTML(final JavadocBrowserInformationControlInput input, final IGroovyResolvedElement elt) {
        // only use a preamble if the name of the inferred element is not the same as the resolved element
        String preamble;
        if (!elt.getElementName().equals(elt.getInferredElementName())) {
            preamble = createLabel(elt.getInferredElement());
        } else {
            preamble = "";
        }
        if (elt.getExtraDoc() != null) {
            return preamble + extraDocAsHtml(elt) + "\n<br/><hr/><br/>\n" + input.getHtml();
        } else {
            return preamble + input.getHtml();
        }
    }

    protected String extraDocAsHtml(final IGroovyResolvedElement elem) {
        String extraDoc = elem.getExtraDoc();
        if (!extraDoc.startsWith("/**")) {
            extraDoc = "/**" + extraDoc;
        }
        if (!extraDoc.endsWith("*/")) {
            extraDoc = extraDoc + "*/";
        }

        String html;
        try {
            html = ReflectionUtils.throwableExecutePrivateMethod(JavadocContentAccess2.class, "javadoc2HTML",
                new Class[] {IMember.class, IJavaElement.class, String.class}, null, new Object[] {elem, elem, extraDoc});
        } catch (NoSuchMethodException e) {
            html = ReflectionUtils.executePrivateMethod(JavadocContentAccess2.class, "javadoc2HTML",
                new Class[] {IMember.class, String.class}, null, new Object[] {elem, extraDoc});
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return html;
    }

    private String createLabel(ASTNode inferredElement) {
        if (inferredElement instanceof PropertyNode) {
            inferredElement = ((PropertyNode) inferredElement).getField();
        }
        String label;
        if (inferredElement instanceof ClassNode) {
            label = createTypeLabel((ClassNode) inferredElement);
        } else if (inferredElement instanceof MethodNode) {
            label = createMethodLabel((MethodNode) inferredElement);
        } else if (inferredElement instanceof FieldNode) {
            label = createFieldLabel((FieldNode) inferredElement);
        } else {
            label = inferredElement.getText();
        }
        return "<b>" + label + "</b><br>\n";
    }

    private String createTypeLabel(final ClassNode node) { // TODO: create link html
        return Signature.toString(GroovyUtils.getTypeSignature(node, false, false));
    }

    private String createFieldLabel(final FieldNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(createTypeLabel(node.getType()));
        sb.append(' ');
        sb.append(createTypeLabel(node.getDeclaringClass()));
        sb.append('.');
        sb.append(node.getName());

        return sb.toString();
    }

    private String createMethodLabel(final MethodNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(createTypeLabel(node.getReturnType()));
        sb.append(' ');
        sb.append(createTypeLabel(node.getDeclaringClass()));
        sb.append('.');
        sb.append(node.getName());
        sb.append('(');
        Parameter[] params = node.getParameters();
        if (params != null) {
            for (int i = 0, n = params.length; i < n; i += 1) {
                if (i > 0) sb.append(", ");

                List<AnnotationNode> annotations = params[i].getAnnotations();
                if (!annotations.isEmpty()) {
                    sb.append("<span style='font-weight:normal;'>");
                    for (AnnotationNode annotation : annotations) {
                        sb.append('@').append(createTypeLabel(annotation.getClassNode()));

                        Map<String, Expression> attributes = annotation.getMembers();
                        if (!attributes.isEmpty()) {
                            sb.append('(');
                            int j = 0;
                            for (Map.Entry<String, Expression> e : attributes.entrySet()) {
                                if (j++ > 0) sb.append(", ");
                                sb.append(e.getKey());
                                sb.append('=');
                                sb.append(createValueLabel(e.getValue()));
                            }
                            sb.append(')');
                        }
                        sb.append(' ');
                    }
                    sb.append("</span>");
                }

                sb.append(createTypeLabel(params[i].getType()));
                sb.append(' ').append(params[i].getName());
            }
        }
        sb.append(')');

        return sb.toString();
    }

    private String createValueLabel(final Expression value) {
        if (value instanceof ListExpression) {
            StringJoiner sj = new StringJoiner(", ", "[", "]");
            for (Expression e : ((ListExpression) value).getExpressions()) {
                sj.add(createValueLabel(e));
            }
            return sj.toString();
        }
        if (value instanceof ClassExpression) {
            return createTypeLabel(value.getType()) + ".class";
        }
        if (value instanceof ConstantExpression) {
            if (value.getType().equals(ClassHelper.STRING_TYPE)) {
                return "\"" + value.getText() + "\"";
            }
            if (value.getType().equals(ClassHelper.char_TYPE)) {
                return "'" + value.getText() + "'";
            }
        }
        return value.getText();
    }
}
