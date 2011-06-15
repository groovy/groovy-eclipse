package org.codehaus.groovy.eclipse.dsl.classpath;

import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

public class DSLDClasspathContainerDecorator extends LabelProvider implements ILightweightLabelDecorator {

    public void decorate(Object element, IDecoration decoration) {
        // decorate the class path container and add the originating target runtime
        if (element instanceof ClassPathContainer) {
            ClassPathContainer container = (ClassPathContainer) element;
            if (container.getClasspathEntry().getPath().equals(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID)) {
                if (container.getJavaProject().getProject().isAccessible() && GroovyDSLCoreActivator.getDefault().getPreferenceStore().getBoolean(DSLPreferencesInitializer.DSLD_DISABLED)) {
                    decoration.addSuffix(" (Disabled)");
                }
            }
        }
    }

}
