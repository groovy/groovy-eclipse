/* 
 * Copyright (C) 2007, 2008 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.pages.rename;

import java.lang.reflect.Method;
import java.util.List;

import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.groovy.eclipse.refactoring.Activator;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * @author andrew
 *
 */
public class MinimalGroovyConfiguration extends JavaSourceViewerConfiguration {

    /**
     * @author andrew
     *
     */
    public class MinimalGroovyCodeScanner extends RuleBasedScanner {
        /**
         * 
         */
        public MinimalGroovyCodeScanner() {
            try {
                Class<RuleBasedScanner> scannerClass = (Class<RuleBasedScanner>) Activator.getDefault().getBundle().loadClass("org.codehaus.groovy.eclipse.editor.GroovyTagScanner");
                List<IRule> rules = (List<IRule>) ReflectionUtils.executePrivateMethod(scannerClass, "createRules", new Class<?>[0], scannerClass, new Object[0]);
                IRule[] rulesArr = rules.toArray(new IRule[0]);
                setRules(rulesArr);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param colorManager
     * @param preferenceStore
     * @param editor
     * @param partitioning
     */
    public MinimalGroovyConfiguration() {
        super(JavaPlugin.getDefault().getJavaTextTools().getColorManager(), 
                JavaPlugin.getDefault().getPreferenceStore(), null, null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration#getCodeScanner()
     */
    @Override
    protected RuleBasedScanner getCodeScanner() {
        return new MinimalGroovyCodeScanner();
    }
}
