/* 
 * Copyright (C) 2007, 2008 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.pages.rename;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaCodeScanner;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * @author andrew
 *
 */
public class MinimalGroovyConfiguration extends JavaSourceViewerConfiguration {


    public MinimalGroovyConfiguration() {
        super(JavaPlugin.getDefault().getJavaTextTools().getColorManager(), 
                JavaPlugin.getDefault().getPreferenceStore(), null, null);
    }

    /**
     * Would be best to be able to create our own Groovy code scanner here
     * Cannot use the UI plugin's code scanner because this plugin cannot add a dependency to it.
     */
    @Override
    protected RuleBasedScanner getCodeScanner() {
        return new JavaCodeScanner(getColorManager(), JavaPlugin.getDefault().getPreferenceStore());
    }
}
