/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A subtype of the Groovy ErrorCollector that can be made more JDT friendly
 * (not throwing exceptions when errors occur and doing nicer mapping from
 * errors to JDT problems). Still much to be done with this.
 */
public class GroovyErrorCollectorForJDT extends ErrorCollector {

    private static final long serialVersionUID = -5358192603029491124L;

    public GroovyErrorCollectorForJDT(final CompilerConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void addErrorAndContinue(final Message message) {
        try {
            if (GroovyLogManager.manager.hasLoggers()) {
                String error;
                if (message instanceof SimpleMessage) {
                    error = ((SimpleMessage) message).getMessage();
                } else {
                    StringWriter writer = new StringWriter();
                    message.write(new PrintWriter(writer));
                    error = writer.toString().trim();
                }
                GroovyLogManager.manager.log(TraceCategory.COMPILER, error);
            }
        } catch (Throwable t) {
            Util.log(t);
        }
        super.addErrorAndContinue(message);
    }

    @Override
    public void addWarning(final WarningMessage message) {
        try {
            if (GroovyLogManager.manager.hasLoggers()) {
                StringWriter writer = new StringWriter();
                message.write(new PrintWriter(writer));
                // reformat to place message first followed by the source sample
                String[] lines = writer.toString().substring(9).split(System.lineSeparator());
                if (lines.length < 3) lines = new String[]{"", "", ": "}; // prevent exception
                String warning = String.format("Warning: %s%n%s%n%s%n%s", message.getMessage(),
                            lines[0], lines[1], lines[2].substring(0, lines[2].indexOf(": ")));

                GroovyLogManager.manager.log(TraceCategory.COMPILER, warning.trim());
            }
        } catch (Throwable t) {
            Util.log(t);
        }
        super.addWarning(message);
    }

    @Override
    protected void failIfErrors() {
        if (transformActive) {
            super.failIfErrors();
        }
    }
}
