package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.codebrowsing.elements.IGroovyResolvedElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;

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

    @Override
    public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
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
            if (elements[0] instanceof IGroovyResolvedElement) {
                IGroovyResolvedElement resolvedElt = (IGroovyResolvedElement) elements[0];
                if (resolvedElt.getExtraDoc() != null && resolvedElt.getExtraDoc().length() > 0) {
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
        if (hover instanceof JavadocBrowserInformationControlInput) {
            JavadocBrowserInformationControlInput input = (JavadocBrowserInformationControlInput) hover;
            hover = new JavadocBrowserInformationControlInput((JavadocBrowserInformationControlInput) input.getPrevious(),
                    input.getElement(), wrapHTML(input, (IGroovyResolvedElement) elements[0]), input.getLeadingImageWidth());
        }
        return hover;
    }

    protected String wrapHTML(JavadocBrowserInformationControlInput input, IGroovyResolvedElement elt) {
        if (elt.getExtraDoc() != null) {
            return input.getHtml() + elt.getExtraDoc();
        } else {
            return input.getHtml();
        }
    }

}
