/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;


/**
 *
 * @author Andrew Eisenberg
 * @created Dec 31, 2009
 */
public class Region {

    private final int start;
    private final int length;

    public Region(ASTNode node) {
    	this.start = node.getStart();
    	this.length = node.getLength();
    }

    public Region(IASTFragment node) {
        this.start = node.getStart();
        this.length = node.getLength();
    }

    public Region(int start, int length) {
        this.start = start;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return start;
    }

    public int getEnd() {
        return start + length;
    }

    /**
     * @param node ASTNode to compare with this region
     * @return true if this region covers the node (ie- region's start and end
     *         surround the node)
     */
    public boolean regionCoversNode(ASTNode node) {
        return this.start <= node.getStart() && this.getEnd() >= node.getEnd();
    }

    /**
     * @param node ASTNode to compare with this region
     * @return true if the ast node covers this region (ie- node's start and end
     *         surround the region)
     */
    public boolean regionIsCoveredByNode(ASTNode node) {
        return this.start >= node.getStart() && this.getEnd() <= node.getEnd();
    }

    /**
     * Checks for coverage on this node's name range
     * 
     * @param node the node to check
     * @return true iff the node's name range covers the region
     */
    public boolean regionIsCoveredByNameRange(AnnotatedNode node) {
        return this.start >= node.getNameStart() && this.getEnd() <= node.getNameEnd()+1;  // FIXADE why +1?
    }

    /**
     * FIXADE with the fix for GRECLIPSE-829, I don't think this method is necessary any more
     * slocs in the ast are now correct for VarExprs inside of gstrings.  Consider deleting
     *  
     * variable expression start locations include the '$' in the groovy code,
     * but not in the java model, so subtract 1 from the starting node
     *
     * @param node
     * @return true iff the region is covered by the node inside the GString.
     */
    public boolean regionIsGStringCoveredByNode(ASTNode node) {
        if (node instanceof VariableExpression) {
            return this.start >= node.getStart()-1 && this.getEnd() <= node.getEnd();
        } else {
            return regionIsCoveredByNode(node);
        }
    }

    public boolean isNonOverlapping(ASTNode node) {
        return (this.getEnd() <= node.getStart()) ||
               (this.start >= node.getEnd());
    }

    public boolean isSame(ASTNode node) {
        return this.start == node.getStart() && this.getEnd() == node.getEnd();
    }

    public boolean isSame(IASTFragment node) {
        return this.start == node.getStart() && this.getEnd() == node.getEnd();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        result = prime * result + start;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Region other = (Region) obj;
        if (length != other.length)
            return false;
        if (start != other.start)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Region [start=" + start + ", length=" + length + "]";
    }

    public boolean endsIn(ASTNode node) {
        return node.getStart() < getEnd() && length < node.getLength();
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return start == 0 && length == 0;
    }

}
