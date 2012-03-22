package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.codebrowsing.elements.IGroovyResolvedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
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
 * hovered
 * over is an {@link IGroovyResolvedElement} and has a non-empty extraDoc.
 *
 * If this hover is used, then due to ordering problems with content assist
 * hovers,
 * this hover will override the variable info hover.
 *
 * @author andrew
 * @created Dec 1, 2010
 */
public class GroovyExtraInformationHover extends JavadocHover {

    private final boolean alwaysReturnInformation;

    private final JavaDebugHover debugHover;

    public GroovyExtraInformationHover() {
        alwaysReturnInformation = false;
        this.debugHover = new JavaDebugHover();
    }

    public GroovyExtraInformationHover(boolean alwaysReturnInformation) {
        this.alwaysReturnInformation = alwaysReturnInformation;
        this.debugHover = new JavaDebugHover();
    }

    @Override
    public void setEditor(IEditorPart editor) {
        super.setEditor(editor);
        debugHover.setEditor(editor);
    }

    @Override
    public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
        IEditorPart editor = getEditor();
        if (editor == null) {
            return null;
        }
        IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
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

        IJavaElement[] elements= getJavaElementsAt(textViewer, hoverRegion);
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
     *
     * @param elements
     * @return
     */
    private boolean shouldComputeHover(IJavaElement[] elements) {
        if (elements != null && elements.length == 1) {
            if (alwaysReturnInformation) {
                return true;
            }
            if (elements[0] instanceof IGroovyResolvedElement) {
                IGroovyResolvedElement resolvedElt = (IGroovyResolvedElement) elements[0];
                if ((resolvedElt.getExtraDoc() != null && resolvedElt.getExtraDoc().length() > 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Possibly compute the hover. Might return null
     *
     * @param hoverRegion
     * @param elements
     * @return
     */
    private Object computeHover(IRegion hoverRegion, IJavaElement[] elements) {
        Object hover;
        hover = ReflectionUtils.executePrivateMethod(JavadocHover.class, "getHoverInfo", new Class[] { IJavaElement[].class,
            ITypeRoot.class, IRegion.class, JavadocBrowserInformationControlInput.class }, this, new Object[] { elements,
            getEditorInputJavaElement(), hoverRegion, null });
        if (hover instanceof JavadocBrowserInformationControlInput && elements[0] instanceof IGroovyResolvedElement) {
            JavadocBrowserInformationControlInput input = (JavadocBrowserInformationControlInput) hover;
            hover = new JavadocBrowserInformationControlInput((JavadocBrowserInformationControlInput) input.getPrevious(),
                    input.getElement(), wrapHTML(input, (IGroovyResolvedElement) elements[0]),
                    input.getLeadingImageWidth());
        }
        return hover;
    }


    protected String wrapHTML(JavadocBrowserInformationControlInput input, IGroovyResolvedElement elt) {
        // only use a preamble if the name of the inferred element is not the
        // same as the resolved element.
        String preamble;
        if (!elt.getElementName().equals(elt.getInferredElementName())) {
            preamble = createLabel(elt.getInferredElement());
        } else {
            preamble = "";
        }
        if (elt.getExtraDoc() != null) {

            String wrapped = preamble + extraDocAsHtml(elt) + "\n<br/><hr/><br/>\n" + input.getHtml();
            //            System.out.println(wrapped);
            return wrapped;
        } else {
            return preamble + input.getHtml();
        }
    }

    protected String extraDocAsHtml(IGroovyResolvedElement elt) {
        String extraDoc = "/**" + elt.getExtraDoc() + "*/";
        if (!extraDoc.startsWith("/**")) {
            extraDoc = "/**" + extraDoc;
        }
        if (!extraDoc.endsWith("*/")) {
            extraDoc = extraDoc + "*/";
        }

        return (String) ReflectionUtils.executePrivateMethod(JavadocContentAccess2.class, "javadoc2HTML", new Class[] {
            IMember.class, String.class }, null, new Object[] { elt, extraDoc });
    }



    /**
     * @param inferredElement
     * @return
     */
    private String createLabel(ASTNode inferredElement) {
        if (inferredElement instanceof PropertyNode) {
            inferredElement = ((PropertyNode) inferredElement).getField();
        }
        String label;
        if (inferredElement instanceof ClassNode) {
            label = createClassLabel((ClassNode) inferredElement);
        } else if (inferredElement instanceof MethodNode) {
            label = createMethodLabel((MethodNode) inferredElement);
        } else if (inferredElement instanceof FieldNode) {
            label = createFieldLabel((FieldNode) inferredElement);
        } else {
            label = inferredElement.getText();
        }
        return "<b>" + label + "</b><br>\n";
    }

    private String createFieldLabel(FieldNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(createClassLabel(node.getType()));
        sb.append(" ");
        sb.append(createClassLabel(node.getDeclaringClass()));
        sb.append(".");
        sb.append(node.getName());
        return sb.toString();
    }

    private String createMethodLabel(MethodNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(createClassLabel(node.getReturnType()));
        sb.append(" ");
        sb.append(createClassLabel(node.getDeclaringClass()));
        sb.append(".");
        sb.append(node.getName());
        sb.append("(");
        Parameter[] params = node.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                sb.append(createClassLabel(params[i].getType()));
                sb.append(" " + params[i].getName());
                if (i < params.length - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(")");

        return sb.toString();
    }

    private String createClassLabel(ClassNode node) {
        StringBuilder sb = new StringBuilder();
        node = node.redirect();
        if (ClassHelper.DYNAMIC_TYPE == node) {
            return "def";
        }
        sb.append(node.getNameWithoutPackage());
        GenericsType[] genericsTypes = node.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0) {
            sb.append(" <");
            for (int i = 0; i < genericsTypes.length; i++) {
                sb.append(genericsTypes[i].getName());
                if (i < genericsTypes.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("> ");
        }
        return sb.toString();
    }

}
