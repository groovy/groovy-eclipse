/*
 * Copyright 2009 the original author or authors.
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
package org.codehaus.groovy.antlr;

import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.ParserPluginFactory;

// FIXASC (groovychange) new type
/**
 * Plugin factory creating plugin that reports on creation of error recovered ASTs.
 * 
 * @author empovazan
 */
public class ErrorRecoveredCSTParserPluginFactory extends ParserPluginFactory {
	private ICSTReporter cstReporter;
    public ErrorRecoveredCSTParserPluginFactory(ICSTReporter cstReporter) {
        this.cstReporter = cstReporter;
    }
    public ErrorRecoveredCSTParserPluginFactory() {
        this.cstReporter = null;
    }

	public ParserPlugin createParserPlugin() {
		return new ErrorRecoveredCSTParserPlugin(cstReporter);
	}
}