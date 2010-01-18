 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.eclipse.debug.ui;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.junit.ui.FailureTrace;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.Workbench;

/**
 * @author Andrew Eisenberg
 * @created Aug 7, 2009
 * 
 * This class enables the JUnit results view to show results in a monospace font
 * This is particularly useful for testing frameworks that rely on a formatted output
 * such as the spock framework.
 * <p>
 * Forcing the font to be monospace occurs in 3 locations:
 * <ol>
 * <li>When the org.codehaus.groovy.eclipse.ui plugin starts (only works if the JUnit view is already visible,
 * which it often is not).
 * <li>Whenever the JUnit view becomes visible
 * <li>Whenever the force monospace preference changes
 * </ol>
 */
public class EnsureJUnitFont implements IPartListener2, IPropertyChangeListener {
    
    private static final String JUNIT_RESULT_VIEW = "org.eclipse.jdt.junit.ResultView";

    public void maybeForceMonospaceFont() {
        forceMonospaceFont(isMonospace());
    }
    
    public void forceMonospaceFont(boolean isMonospace) {
        try {
            IWorkbenchPage page = Workbench.getInstance().getActiveWorkbenchWindow()
                    .getActivePage();
            if (page == null) {
                // occurred too early---window not open yet
                return;
            }
            
            TestRunnerViewPart view = (TestRunnerViewPart) page.findView(JUNIT_RESULT_VIEW);
            if (view == null) {
                // not open---can ignore
                return;
            }
            internalSetMonospaceFont(isMonospace, view);
        } catch (Exception e) {
            GroovyCore.logException("Error setting monospace font for JUnit pane to " + isMonospace, e);
        }
    }

    private boolean isMonospace() {
        try {
            IPreferenceStore prefs = GroovyPlugin.getDefault().getPreferenceStore();
            return prefs.getBoolean(PreferenceConstants.GROOVY_JUNIT_MONOSPACE_FONT);
        } catch (Exception e) {
            return false;
        }
    }

    private void internalSetMonospaceFont(boolean isMonospace,
            TestRunnerViewPart view) {
        FailureTrace trace = view.getFailureTrace();
        Composite widget = (Composite) ReflectionUtils.getPrivateField(FailureTrace.class, "fTable", trace);
        
        if (isMonospace) {
            widget.setFont(JFaceResources.getTextFont());
        } else {
            widget.setFont(JFaceResources.getDefaultFont());
        }
    }
    
    private void internalForceMonospaceFont(IWorkbenchPartReference partRef) {
        if (partRef.getId().equals(JUNIT_RESULT_VIEW)) {
            TestRunnerViewPart view = (TestRunnerViewPart) partRef.getPage().findView(JUNIT_RESULT_VIEW);
            if (view != null) {
                internalSetMonospaceFont(isMonospace(), view);
            }
        }
    }

    
    /*************
     * implemented for the listener interfaces
     */
    public void partActivated(IWorkbenchPartReference partRef) {
        internalForceMonospaceFont(partRef);
    }

    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        internalForceMonospaceFont(partRef);
    }

    public void partOpened(IWorkbenchPartReference partRef) {        
        internalForceMonospaceFont(partRef);
    }

    public void partVisible(IWorkbenchPartReference partRef) {
        internalForceMonospaceFont(partRef);
    }

    public void partClosed(IWorkbenchPartReference partRef) { }

    public void partDeactivated(IWorkbenchPartReference partRef) { }

    public void partHidden(IWorkbenchPartReference partRef) { }

    public void partInputChanged(IWorkbenchPartReference partRef) { }

    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(PreferenceConstants.GROOVY_JUNIT_MONOSPACE_FONT) ||
                event.getProperty().equals(JFaceResources.TEXT_FONT) ||
                event.getProperty().equals(JFaceResources.DEFAULT_FONT)) {
            maybeForceMonospaceFont();
        }
    }
}