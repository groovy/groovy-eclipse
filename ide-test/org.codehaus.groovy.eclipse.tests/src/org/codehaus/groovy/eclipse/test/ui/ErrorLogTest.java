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
package org.codehaus.groovy.eclipse.test.ui;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.views.log.AbstractEntry;
import org.eclipse.ui.internal.views.log.LogEntry;
import org.eclipse.ui.internal.views.log.LogView;

/**
 * @author Andrew Eisenberg
 * @created Aug 7, 2009
 * Test that we don't get any spurious errors or warnings in the log on startup
 *
 */
public class ErrorLogTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        System.out.println("------------------------------");
        System.out.println("Starting: " + getName());
        super.setUp();
    }

    private static final String[] KNOWN_MSGS = new String[] { 
        "Could not locate the running profile instance.",
        "The following is a complete list",
        "One or more bundles",
        "Monitor UI start failed",
        "was not resolved",
        "org.eclipse.test.performance.win32.translated_host_properties",
        "Listener failed",
        "org.eclipse.mylyn.tasks.core",
        "Unable to run embedded server"
    };
    
    private boolean canIgnoreMessage(String msg) {
        for (String ignore : KNOWN_MSGS) {
            if (msg.contains(ignore)) {
                return true;
            }
        }
        return false;
    }

    public void testNoWarningsOnStartup() throws Exception {
        IViewPart view = Workbench.getInstance().getActiveWorkbenchWindow()
                .getActivePage().getActivePart().getSite().getPage().showView(
                        "org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
        if (view instanceof LogView) {
            LogView logView = (LogView) view;
            AbstractEntry[] logs = logView.getElements();
            // Ignore information entries in the log
            List<AbstractEntry> errorsAndWarnings = new ArrayList<AbstractEntry>();
            for (int i = 0; i < logs.length; i++) {
                LogEntry entry = (LogEntry) logs[i];
                if (entry.getSeverity() == IStatus.ERROR
                        || entry.getSeverity() == IStatus.WARNING) {
                    String msg = entry.getMessage();
                    if (!canIgnoreMessage(msg)) {
                        errorsAndWarnings.add(logs[i]);
                    }
                }
            }
            if (errorsAndWarnings.size() > 0) {
                StringBuffer errors = new StringBuffer();
                boolean ignore = false;
                for (AbstractEntry element : errorsAndWarnings) {
                    LogEntry log = (LogEntry) element;
                    errors.append(log.getMessage());
                    errors.append(" (" + log.getPluginId() + ")\n"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (element.hasChildren()) {
                        Object[] sub = element.getChildren(null);
                        for (int i = 0; i < sub.length; i++) {
                            if (sub[i] instanceof LogEntry) {
                                LogEntry s = (LogEntry) sub[i];
                                String msg = s.getMessage();
                                errors.append("    " + msg); //$NON-NLS-1$
                                errors.append(" (" + s.getPluginId() + ")\n");
                                ignore = false;
                            }
                        }
                    }
                }
                if (!ignore) {
                    fail("There should be no unexpected entries in the error log. Found:\n"
                            + errors.toString()); //$NON-NLS-1$
                }
            }
        } else {
            fail("Could not find the Error log."); //$NON-NLS-1$
        }
    }

}
