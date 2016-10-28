/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;

/**
 * A subtype of the Groovy ErrorCollector that can be made more JDT friendly (not throwing exceptions when errors occur and doing
 * nicer mapping from errors to JDT problems). Still much to be done with this.
 *
 * @author Andy Clement
 */
public class GroovyErrorCollectorForJDT extends ErrorCollector {

    public GroovyErrorCollectorForJDT(CompilerConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void addErrorAndContinue(Message message) {
        // System.err.println(message);
        // FIXASC SimpleMessage can be an error, it just isn't a syntax error - should be recorded with appropriate priority.
        // Look at creators of SimpleMessage - are they all errors?
        if (message instanceof SimpleMessage) {
            System.err.println("SimpleMessage: " + ((SimpleMessage) message).getMessage());
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
