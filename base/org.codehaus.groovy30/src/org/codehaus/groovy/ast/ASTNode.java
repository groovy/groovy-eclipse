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
package org.codehaus.groovy.ast;

import java.util.Map;
import java.util.Objects;

/**
 * Base class for any AST node. This class supports basic information used in all nodes of the AST:
 * <ul>
 * <li> line and column number information. Usually a node represents a certain
 * area in a text file determined by a starting position and an ending position.
 * For nodes that do not represent this, this information will be -1. A node can
 * also be configured in its line/col information using another node through 
 * setSourcePosition(otherNode).</li>
 * <li> every node can store meta data. A phase operation or transform can use 
 * this to transport arbitrary information to another phase operation or 
 * transform. The only requirement is that the other phase operation or transform
 * runs after the part storing the information. If the information transport is 
 * done it is strongly recommended to remove that meta data.</li> 
 * <li> a text representation of this node trough getText(). This was in the
 * past used for assertion messages. Since the usage of power asserts this 
 * method will not be called for this purpose anymore and might be removed in
 * future versions of Groovy</li>
 * </ul>
 */
public class ASTNode implements NodeMetaDataHandler {

    private int lineNumber = -1;
    private int columnNumber = -1;
    private int lastLineNumber = -1;
    private int lastColumnNumber = -1;
    // GRECLIPSE add
    private final int[] offsets = new int[2];
    // GRECLIPSE end
    private Map metaDataMap;

    public void visit(GroovyCodeVisitor visitor) {
        throw new RuntimeException("No visit() method implemented for class: " + getClass().getName());
    }

    public String getText() {
        return "<not implemented yet for class: " + getClass().getName() + ">";
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getLastLineNumber() {
        return lastLineNumber;
    }

    public void setLastLineNumber(int lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
    }

    public int getLastColumnNumber() {
        return lastColumnNumber;
    }

    public void setLastColumnNumber(int lastColumnNumber) {
        this.lastColumnNumber = lastColumnNumber;
    }

    // GRECLIPSE add
    public int getStart() {
        return offsets[0];
    }
    public void setStart(int offset) {
        offsets[0] = offset;
    }
    public int getEnd() {
        return offsets[1];
    }
    public void setEnd(int offset) {
        offsets[1] = offset;
    }
    public int getLength() {
        return (offsets[1] >= 0 && offsets[0] >= 0 ? offsets[1] - offsets[0] : -1);
    }
    // GRECLIPSE end

    /**
     * Sets the source position using another ASTNode.
     * The sourcePosition consists of a line/column pair for
     * the start and a line/column pair for the end of the
     * expression or statement 
     * 
     * @param node - the node used to configure the position information
     */
    public void setSourcePosition(ASTNode node) {
        this.columnNumber = node.getColumnNumber();
        this.lastLineNumber = node.getLastLineNumber();
        this.lastColumnNumber = node.getLastColumnNumber();
        this.lineNumber = node.getLineNumber();
        // GRECLIPSE add
        this.offsets[0] = node.getStart();
        this.offsets[1] = node.getEnd();
        // GRECLIPSE end
    }

    /**
     * Copies all node meta data from the other node to this one
     * @param other - the other node
     */
    public void copyNodeMetaData(ASTNode other) {
        copyNodeMetaData((NodeMetaDataHandler) other);
    }

    @Override
    public Map getMetaDataMap() {
        return metaDataMap;
    }

    @Override
    public void setMetaDataMap(Map metaDataMap) {
        this.metaDataMap = metaDataMap;
    }

    /* GRECLIPSE edit
    @Override
    public boolean equals(Object o) {
        return this == o;
    }
    */

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, columnNumber, lastLineNumber, lastColumnNumber);
    }
}
