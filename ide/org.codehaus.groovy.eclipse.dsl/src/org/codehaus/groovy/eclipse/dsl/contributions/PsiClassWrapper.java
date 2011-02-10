/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.contributions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.dsl.script.IContextQueryResult;

/**
 * A wrapper for the 'psiClass' parameter
 * From the IntelliJ variant of the DSL.
 * This is not a complete wrapper, but exposes some
 * of the more important fields/methods to make it usable
 * in the DSL
 * @author andrew
 * @created Nov 22, 2010
 */
public class PsiClassWrapper {

    private List<FieldNode> cachedFields;
    private List<MethodNode> cachedMethods;
    private List<ClassNode> cachedClasses;
    
    private List<AnnotatedNode> cachedResult;
    
    public PsiClassWrapper(IContextQueryResult<?> result) {
        Object resultObj = result.getResult();
        if (resultObj instanceof AnnotatedNode) {
            cachedResult = Collections.singletonList((AnnotatedNode) resultObj); 
        } else if (resultObj instanceof List<?>) {
            cachedResult = (List<AnnotatedNode>) resultObj;
        } else if (resultObj instanceof AnnotatedNode[]) {
            cachedResult = Arrays.asList((AnnotatedNode[]) resultObj);
        } else {
            cachedResult = Collections.emptyList();
        }
    }

    public List<FieldNode> getFields() {
        if (cachedFields == null) {
            cachedFields = new ArrayList<FieldNode>(cachedResult.size());
            for (AnnotatedNode node : cachedResult) {
                if (node instanceof FieldNode) {
                    cachedFields.add((FieldNode) node);
                }
            }
        }
        return cachedFields;
    }

    public List<MethodNode> getMethods() {
        if (cachedMethods == null) {
            cachedMethods = new ArrayList<MethodNode>(cachedResult.size());
            for (AnnotatedNode node : cachedResult) {
                if (node instanceof MethodNode) {
                    cachedMethods.add((MethodNode) node);
                }
            }
        }
        return cachedMethods;
    }

    public List<ClassNode> getClasses() {
        if (cachedClasses == null) {
            cachedClasses = new ArrayList<ClassNode>(cachedResult.size());
            for (AnnotatedNode node : cachedResult) {
                if (node instanceof ClassNode) {
                    cachedClasses.add((ClassNode) node);
                }
            }
        }
        return cachedClasses;
    }
}
