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

import java.io.Reader;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Reduction;
// FIXASC (groovychange) new type
/**
 * AST parser plugin which hooks into the CST parsing and reports on CSTs built.
 * 
 * @author empovazan
 */
public class CSTParserPlugin extends AntlrParserPlugin {
	private ICSTReporter cstReporter;
	CSTParserPlugin(ICSTReporter cstReporter) {
		this.cstReporter = cstReporter;
	}
	
    public Reduction parseCST(final SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
    	Reduction reduction = super.parseCST(sourceUnit, reader);
    	GroovySourceAST cst = (GroovySourceAST) super.ast;
    	super.ast = null;
    	if (cst != null) {
    		cstReporter.generatedCST(sourceUnit.getName(), cst);
    	}
		return reduction;
    }
}