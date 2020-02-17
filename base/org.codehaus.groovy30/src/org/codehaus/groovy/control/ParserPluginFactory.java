/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.syntax.ParserException;
import org.codehaus.groovy.syntax.Reduction;

/**
 * A factory of parser plugin instances.
 */
public abstract class ParserPluginFactory {
    /**
     * Creates the ANTLR 4 parser.
     *
     * @return the factory for the parser
     */
    public static ParserPluginFactory antlr4(final CompilerConfiguration compilerConfiguration) {
        /* GRECLIPSE edit
        return new Antlr4PluginFactory(compilerConfiguration);
        */
        return new ParserPluginFactory() {
            @Override
            public ParserPlugin createParserPlugin() {
                return new ParserPlugin() {
                    @Override
                    public Reduction parseCST(final SourceUnit sourceUnit, final java.io.Reader reader) throws CompilationFailedException {
                        return null;
                    }
                    @Override
                    public ModuleNode buildAST(final SourceUnit sourceUnit, final ClassLoader classLoader, final Reduction cst) throws ParserException {
                        assert sourceUnit.getSource() != null && sourceUnit.getSource().canReopenSource();
                        return new org.apache.groovy.parser.antlr4.AstBuilder(sourceUnit, compilerConfiguration).buildAST();
                    }
                };
            }
        };
        // GRECLIPSE end
    }

    /**
     * Creates the ANTLR 2 parser.
     *
     * @return the factory for the parser
     */
    @Deprecated
    public static ParserPluginFactory antlr2() {
        /* GRECLIPSE edit
        return new AntlrParserPluginFactory();
        */
        return new org.codehaus.groovy.antlr.ErrorRecoveredCSTParserPluginFactory();
        // GRECLIPSE end
    }

    public abstract ParserPlugin createParserPlugin();
}
