/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
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

    public GroovyErrorCollectorForJDT(CompilerConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void addErrorAndContinue(Message message) {
        try {
            String error;
            if (message instanceof SimpleMessage) {
                error = ((SimpleMessage) message).getMessage();
            } else {
                StringWriter writer = new StringWriter();
                message.write(new PrintWriter(writer));
                error = writer.toString().trim();
            }
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.COMPILER, error);
            }
        } catch (Throwable t) {
            Util.log(t);
        }
        super.addErrorAndContinue(message);
    }

    @Override
    protected void failIfErrors() throws CompilationFailedException {
        if (transformActive) {
            super.failIfErrors();
        }
    }
}
