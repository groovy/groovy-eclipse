/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.NodeMetaDataHandler;

import java.util.Map;

/**
 * Represents an empty statement.
 */
public class EmptyStatement extends Statement {
    // GRECLIPSE edit
    public static final EmptyStatement INSTANCE = new EmptyStatement() {
        @Override
        public void setStatementLabel(String label) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void addStatementLabel(String label) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void setLineNumber(int lineNumber) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void setColumnNumber(int columnNumber) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void setLastLineNumber(int lastLineNumber) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void setLastColumnNumber(int lastColumnNumber) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void setSourcePosition(ASTNode node) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void copyNodeMetaData(NodeMetaDataHandler other) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void setNodeMetaData(Object key, Object value) {
            throw createUnsupportedOperationException();
        }

        @Override
        public Object putNodeMetaData(Object key, Object value) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void removeNodeMetaData(Object key) {
            throw createUnsupportedOperationException();
        }

        @Override
        public void setMetaDataMap(Map metaDataMap) {
            throw createUnsupportedOperationException();
        }

        private UnsupportedOperationException createUnsupportedOperationException() {
            return new UnsupportedOperationException("EmptyStatement.INSTANCE is immutable");
        }
    };
    // GRECLIPSE end

    @Override
    public void visit(GroovyCodeVisitor visitor) {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
