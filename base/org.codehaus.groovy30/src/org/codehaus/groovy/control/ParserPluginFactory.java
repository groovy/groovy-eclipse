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
 * A factory of parser plugin instances
 *
 */
public abstract class ParserPluginFactory {

    /**
     * creates the ANTLR 4 parser
     * @return the factory for the parser
     */
    public static ParserPluginFactory antlr4(CompilerConfiguration compilerConfiguration) {
        /* GRECLIPSE edit
        return new Antlr4PluginFactory(compilerConfiguration);
        */
        return new ParserPluginFactory() {
            @Override
            public ParserPlugin createParserPlugin() {
                return new ParserPlugin() {
                    @Override
                    public Reduction parseCST(SourceUnit sourceUnit, java.io.Reader reader) throws CompilationFailedException {
                        return null;
                    }
                    @Override
                    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {
                        assert sourceUnit.getSource() != null && sourceUnit.getSource().canReopenSource();
                        return new org.apache.groovy.parser.antlr4.AstBuilder(sourceUnit, compilerConfiguration).buildAST();
                    }
                };
            }
        };
        // GRECLIPSE end
    }

    /**
     * creates the ANTLR 2.7 parser
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

    /**
     * creates the ANTLR 2.7 parser. This method was used to switch between the pre JSR
     * parser and the new ANTLR 2.7 based parser, but even before Groovy 1.0 this
     * method was changed to always return the ANTLR 2.7 parser.
     * @param useNewParser - ignored
     * @return the ANTLR 2.7 based parser
     */
    @Deprecated
    public static ParserPluginFactory newInstance(boolean useNewParser) {
        return newInstance();
    }

    /**
     * creates the ANTLR 2.7 parser. This method was used to switch between the pre JSR
     * parser and the new ANTLR 2.7 based parser, but even before Groovy 1.0 this
     * method was changed to always return the ANTLR 2.7 parser.
     *
     * @return the new parser factory.
     */
    @Deprecated
    public static ParserPluginFactory newInstance() {
        return antlr2();
    }

    public abstract ParserPlugin createParserPlugin();
}
