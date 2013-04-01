package org.codehaus.groovy.eclipse.quickfix.templates;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.core.runtime.IAdapterFactory;

@SuppressWarnings("rawtypes")
public class SurroundWithAdapterFactory implements IAdapterFactory {
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof GroovyEditor) {
            return new SurroundWithFactory();
        }
        return null;
    }

    public Class[] getAdapterList() {
        return new Class[] { SurroundWithFactory.class };
    }
}
