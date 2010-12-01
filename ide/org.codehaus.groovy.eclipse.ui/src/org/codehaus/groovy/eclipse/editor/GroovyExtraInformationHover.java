package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.codebrowsing.elements.IGroovyResolvedElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;

public class GroovyExtraInformationHover extends JavadocHover {

    @Override
    public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
        IJavaElement[] elements= getJavaElementsAt(textViewer, hoverRegion);
        Object hover = null;
        if (elements != null && elements.length == 1 &&
                elements[0] instanceof IGroovyResolvedElement) {

            hover = ReflectionUtils.executePrivateMethod(
            		JavadocHover.class,
            		"getHoverInfo",
            		new Class[] { IJavaElement[].class, ITypeRoot.class, IRegion.class, JavadocBrowserInformationControlInput.class },
            		this,
            		new Object[] { elements, getEditorInputJavaElement(), hoverRegion, null });
            if (hover instanceof JavadocBrowserInformationControlInput) {
        		JavadocBrowserInformationControlInput input = (JavadocBrowserInformationControlInput) hover;
        	    hover = new JavadocBrowserInformationControlInput((JavadocBrowserInformationControlInput) input.getPrevious(), input.getElement(), wrapHTML(input, (IGroovyResolvedElement) elements[0]), input.getLeadingImageWidth());
        	}
        }
        if (hover != null) {
            return hover;
        } else {
            return super.getHoverInfo2(textViewer, hoverRegion);
        }
    }

    /**
     * @param input
     * @return
     */
    protected String wrapHTML(JavadocBrowserInformationControlInput input, IGroovyResolvedElement elt) {
        if (elt.getExtraDoc() != null) {
            return input.getHtml() + elt.getExtraDoc();
        } else {
            return input.getHtml();
        }
    }

}
