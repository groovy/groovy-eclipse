package org.codehaus.groovy.eclipse.quickfix.templates;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.core.runtime.IAdapterFactory;

public class SurroundWithAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adaptableObject instanceof GroovyEditor) {
            return (T) new SurroundWithFactory();
        }
        return null;
    }

    public Class<?>[] getAdapterList() {
        return new Class[] { SurroundWithFactory.class };
    }
}
