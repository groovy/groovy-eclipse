package org.codehaus.groovy.activator;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class GroovyActivator extends Plugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy"; //$NON-NLS-1$
    
    public static final String GROOVY_ALL_JAR = "lib/groovy-all-1.7.8.jar"; //$NON-NLS-1$
    public static final String GROOVY_JAR = "lib/groovy-1.7.8.jar"; //$NON-NLS-1$
    public static final String ASM_JAR = "lib/asm-3.2.jar"; //$NON-NLS-1$
    public static final int GROOVY_LEVEL = 17;

    public static URL GROOVY_JAR_URL;
    public static URL GROOVY_ALL_JAR_URL;
    public static URL ASM_JAR_URL;
    
    private static GroovyActivator DEFAULT;
    
    public GroovyActivator() {
        DEFAULT = this;
    }
    
    public static GroovyActivator getDefault() {
        return DEFAULT;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        GROOVY_JAR_URL = FileLocator.resolve(Platform.getBundle(PLUGIN_ID).getEntry(GroovyActivator.GROOVY_JAR));
        GROOVY_ALL_JAR_URL = FileLocator.resolve(Platform.getBundle(PLUGIN_ID).getEntry(GroovyActivator.GROOVY_ALL_JAR));
        ASM_JAR_URL = FileLocator.resolve(Platform.getBundle(PLUGIN_ID).getEntry(GroovyActivator.ASM_JAR));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }
}