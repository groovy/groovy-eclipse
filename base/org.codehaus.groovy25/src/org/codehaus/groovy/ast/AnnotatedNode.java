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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for any AST node which is capable of being annotated
 */
public class AnnotatedNode extends ASTNode {

    private List<AnnotationNode> annotations = Collections.emptyList();
    private boolean hasNoRealSourcePositionFlag;
    private boolean synthetic;
    ClassNode declaringClass;
    // GRECLIPSE add
    private int nameStart = 0;
    private int nameStop = -1;
    // GRECLIPSE end

    public AnnotatedNode() {
    }

    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }

    public List<AnnotationNode> getAnnotations(ClassNode type) {
        List<AnnotationNode> annotations = new ArrayList<>();
        for (AnnotationNode node : getAnnotations()) {
            if (type.equals(node.getClassNode())) {
                annotations.add(node);
            }
        }
        return annotations;
    }

    public void addAnnotation(AnnotationNode value) {
        if (value != null) {
            checkInit();
            annotations.add(value);
        }
    }

    private void checkInit() {
        if (annotations == Collections.EMPTY_LIST)
            annotations = new ArrayList<AnnotationNode>(3);
    }

    public void addAnnotations(List<AnnotationNode> annotations) {
        for (AnnotationNode node : annotations) {
            addAnnotation(node);
        }
    }

    /**
     * Returns true if this node is added by the compiler.
     * <b>NOTE</b>:
     * This method has nothing to do with the synthetic flag
     * for fields, methods or classes.
     * @return true if this node is added by the compiler
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * Sets this node as a node added by the compiler.
     * <b>NOTE</b>:
     * This method has nothing to do with the synthetic flag
     * for fields, methods or classes.
     * @param synthetic - if true this node is marked as
     *                    added by the compiler
     */
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    public ClassNode getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(ClassNode declaringClass) {
        this.declaringClass = declaringClass;
    }

    // GRECLIPSE add
    public int getNameStart() {
        return nameStart;
    }

    public void setNameStart(int offset) {
        nameStart = offset;
    }

    public int getNameEnd() {
        return nameStop;
    }

    public void setNameEnd(int offset) {
        nameStop = offset;
    }

    @Override
    public void setSourcePosition(ASTNode node) {
        super.setSourcePosition(node);
        if (node instanceof AnnotatedNode) {
            AnnotatedNode aNode = (AnnotatedNode) node;
            setNameStart(aNode.getNameStart());
            setNameEnd(aNode.getNameEnd());
        }
    }
    // GRECLIPSE end

    /**
     * Returns true for default constructors added by the compiler.
     *
     * @see https://issues.apache.org/jira/browse/GROOVY-4161
     */
    public boolean hasNoRealSourcePosition() {
        return hasNoRealSourcePositionFlag;
    }

    public void setHasNoRealSourcePosition(boolean value) {
        this.hasNoRealSourcePositionFlag = value;
    }
}
