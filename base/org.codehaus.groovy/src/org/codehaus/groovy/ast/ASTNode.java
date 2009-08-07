/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.ast;


/**
 * Base class for any AST node
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision: 6778 $
 */
public class ASTNode {

    private int lineNumber = -1;
    private int columnNumber = -1;
    private int lastLineNumber = -1;
    private int lastColumnNumber = -1;
    // FIXASC (groovychange)
    private int start = 0;
    private int end = 0;
    // end

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
    // FIXASC (groovychange)
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public int getEnd() {
        return end;
    }
    public void setEnd(int end) {
        this.end = end;
    }
    public int getLength() {
        return end >= 0 && start >= 0 ? end-start : -1;
    }
    // end
    
    /**
     * Sets the source position using another ASTNode.
     * The sourcePosition consists of a line/column pair for
     * the start and a line/column pair for the end of the
     * expression or statement 
     // FIXASC (groovychange)
     * as well as the absolute start and end positions in the file
     // end
     * 
     */
    public void setSourcePosition(ASTNode node) {
        this.columnNumber = node.getColumnNumber();
        this.lastLineNumber = node.getLastLineNumber();
        this.lastColumnNumber = node.getLastColumnNumber();
        this.lineNumber = node.getLineNumber();
        // FIXASC (groovychange)
        this.start = node.getStart();
        this.end = node.getEnd();
        // end
    }
}
