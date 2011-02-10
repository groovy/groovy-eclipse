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
package org.codehaus.groovy.eclipse.dsl.contexts;

import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.script.EmptyResult;
import org.codehaus.groovy.eclipse.dsl.script.IContextQuery;
import org.codehaus.groovy.eclipse.dsl.script.IContextQueryResult;
import org.codehaus.groovy.eclipse.dsl.script.SingleNodeResult;
import org.eclipse.jdt.groovy.search.VariableScope;


/**
 * A pattern to match contexts against
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class ContextPattern {
    
    public ContextPattern(String fileName, ClassNode targetType, VariableScope currentScope) {
        this.fileName = fileName;
        this.targetType = targetType;
        this.currentScope = currentScope;
    }

    public final String fileName;

    public final VariableScope currentScope;
    
    private ClassNode targetType;
    private Set<ClassNode> cachedHierarchy;
    
    
    public ContextPattern copy(VariableScope newScope) {
        return new ContextPattern(fileName, targetType, newScope);
    }

    public void setTargetType(ClassNode targetType) {
        cachedHierarchy = null;
        this.targetType = targetType;
    }
    

    /**
     * @param targetTypeQuery
     * @return
     */
    public IContextQueryResult<?> matchesType(IContextQuery targetTypeQuery) {
        if (targetTypeQuery == null || targetType == null) {
            return new SingleNodeResult<ClassNode>(targetType);
        }
        
        if (cachedHierarchy == null) {
            // use linked hash set because order is important
            cachedHierarchy = new LinkedHashSet<ClassNode>();
            getAllSupers(targetType, cachedHierarchy);
        }
        
        for (ClassNode node : cachedHierarchy) {
            IContextQueryResult<?> result = targetTypeQuery.evaluate(node, currentScope);
            if (result != null) {
                return result;
            }
        }
        return EmptyResult.INSTANCE;
    }
    
    @SuppressWarnings("cast") // keep cast to make 1.6 compile
    protected void getAllSupers(ClassNode type, Set<ClassNode> set) {
        if (type == null) {
            return;
        }
        set.add(type);
        getAllSupers(type.getSuperClass(), set);
        for (ClassNode inter : (Iterable<ClassNode>) type.getAllInterfaces()) {
            if (! inter.getName().equals(type.getName())) {
                getAllSupers(inter, set);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ContextPattern [fileName=");
        builder.append(fileName);
        builder.append(", targetType=");
        builder.append(targetType);
        builder.append(", currentScope=");
        builder.append(currentScope);
        builder.append("]");
        return builder.toString();
    }
}
