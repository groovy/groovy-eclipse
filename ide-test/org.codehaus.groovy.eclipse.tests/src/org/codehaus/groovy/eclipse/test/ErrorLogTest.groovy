/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.test

import static org.junit.Assert.fail
import static org.junit.Assume.assumeTrue

import org.eclipse.core.runtime.IStatus
import org.eclipse.jdt.core.JavaCore
import org.eclipse.ui.IViewPart
import org.eclipse.ui.internal.Workbench
import org.eclipse.ui.internal.views.log.AbstractEntry
import org.eclipse.ui.internal.views.log.LogEntry
import org.eclipse.ui.internal.views.log.LogView
import org.junit.Test
import org.osgi.framework.Version

/**
 * Test that we don't get any spurious errors or warnings in the log on startup.
 */
final class ErrorLogTest {

    private static final List<String> KNOWN_MSGS = [
        'Could not locate the running profile instance.',
        'The following is a complete list',
        'One or more bundles',
        'Monitor UI start failed',
        'was not resolved',
        'org.eclipse.test.performance.win32.translated_host_properties',
        'Listener failed',
        'org.eclipse.mylyn.tasks.core',
        'Unable to run embedded server',
        'Test.groovy'
    ].asImmutable()

    private boolean canIgnoreMessage(String msg) {
        for (String ignore : KNOWN_MSGS) {
            if (msg.contains(ignore)) {
                return true
            }
        }
        return false
    }

    @Test
    void testNoWarningsOnStartup() {
        assumeTrue(JavaCore.getPlugin().getBundle().getVersion().compareTo(Version.parseVersion('3.8')) >= 0)

        IViewPart view = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPage().showView('org.eclipse.pde.runtime.LogView')
        if (view instanceof LogView) {
            LogView logView = (LogView) view
            AbstractEntry[] logs = logView.getElements()
            // Ignore information entries in the log
            List<AbstractEntry> errorsAndWarnings = new ArrayList<AbstractEntry>()
            for (int i = 0; i < logs.length; i++) {
                LogEntry entry = (LogEntry) logs[i]
                if (entry.getSeverity() == IStatus.ERROR
                        || entry.getSeverity() == IStatus.WARNING) {
                    String msg = entry.getMessage()
                    if (!canIgnoreMessage(msg)) {
                        errorsAndWarnings.add(logs[i])
                    }
                }
            }
            if (errorsAndWarnings) {
                StringBuilder errors = new StringBuilder()
                boolean ignore = false
                int count = 1
                for (AbstractEntry element : errorsAndWarnings) {
                    LogEntry log = (LogEntry) element
                    errors.append('=================== Log entry '+(count++)+' ===================\n')
                    errors.append(log.getMessage())
                    errors.append(' (' + log.getPluginId() + ')\n')
                    if (element.hasChildren()) {
                        element.getChildren(null).grep(LogEntry).each { LogEntry s ->
                            String msg = s.getMessage()
                            errors.append('    ' + msg)
                            errors.append(' (' + s.getPluginId() + ')\n')
                            errors.append('Stack trace: ' + s.getStack() + '\n\n\n')
                            ignore = false
                        }
                    }
                    errors.append('===================\n')
                }
                if (!ignore) {
                    fail('There should be no unexpected entries in the error log. Found:\n' + errors.toString())
                }
            }
        } else {
            fail('Could not find the Error log.')
        }
    }
}
