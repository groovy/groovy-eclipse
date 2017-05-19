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
import org.eclipse.ui.internal.views.log.AbstractEntry
import org.eclipse.ui.internal.views.log.LogView
import org.junit.Test
import org.osgi.framework.Version

/**
 * Verifies that there are no spurious errors or warnings in the log on startup.
 */
final class ErrorLogTest {

    private static final List<String> KNOWN_MSGS = [
        'Could not locate the running profile instance.',
        'Listener failed',
        'Monitor UI start failed',
        'One or more bundles are not resolved',
        'org.eclipse.mylyn.tasks.core',
        'org.eclipse.test.performance.win32.translated_host_properties',
        'The following is a complete list of bundles which are not resolved',
        'Unable to run embedded server'
    ].asImmutable()

    @Test
    void testNoWarningsOnStartup() {
        assumeTrue(JavaCore.getPlugin().getBundle().getVersion().compareTo(Version.parseVersion('3.8')) >= 0)

        LogView view = SynchronizationUtils.showView('org.eclipse.pde.runtime.LogView')
        Collection<AbstractEntry> errorsAndWarnings = view.elements.findAll { AbstractEntry e ->
            (e.severity == IStatus.ERROR || e.severity == IStatus.WARNING) && !(KNOWN_MSGS.any { e.message =~ it })
        }

        if (errorsAndWarnings) {
            StringBuilder report = new StringBuilder()
            errorsAndWarnings.eachWithIndex { e, i ->
                report.append("=================== Log entry ${i + 1} ===================\n")
                report.append("$e.message ($e.pluginId)\n")
                if (e.hasChildren()) {
                    e.getChildren(null).each { c ->
                        if (c.class.simpleName == 'LogEntry') {
                            report.append("    $c.message ($c.pluginId)\n")
                            if (c.stack) report.append("    Stack trace: $c.stack\n")
                        }
                    }
                }
            }

            fail('There should be no unexpected entries in the error log. Found:\n' + report)
        }
    }
}
