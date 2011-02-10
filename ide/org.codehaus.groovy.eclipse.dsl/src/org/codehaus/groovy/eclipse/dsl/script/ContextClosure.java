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
package org.codehaus.groovy.eclipse.dsl.script;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.contexts.Context;

/**
 * A closure that generates a context
 * @author andrew
 * @created Nov 17, 2010
 */
public class ContextClosure extends Closure {

    private static final long serialVersionUID = 2571362489618585069L;

    private final String containerIdentifier;
    
    public ContextClosure(String containerIdentifier) {
        super(null, null);
        this.containerIdentifier = containerIdentifier;
    }
    
    
    /**
     * takes a map of context arguments and returns an IContext
     */
    @Override
    public Object call(Object arguments) {
        Map<Object, Object> map = (Map<Object, Object>) arguments;
        Context context = new Context(containerIdentifier);
        context.setTargetTypeQuery(getCType(map));
        context.setFileExtensions(getFileTypes(map));
        context.setScope(getScope(map));
            
        return context;
    }


    private IContextQuery getScope(Map<Object, Object> map) {
        Object query = map.get("scope");
        if (query instanceof IContextQuery) {
            return (IContextQuery) query;
        } else {
            return null;
        }
    }


    /**
     * @param map
     * @param context
     */
    public String[] getFileTypes(Map<Object, Object> map) {
        Object maybeFileTypes = map.get("filetypes");
        if (maybeFileTypes instanceof List) {
            return ((List<String>) maybeFileTypes).toArray(new String[0]);
        } else if (maybeFileTypes instanceof String[]) {
            return (String[]) maybeFileTypes;
        } else if (maybeFileTypes != null) {
            return new String[] { maybeFileTypes.toString() };
        } else {
            return null;
        }
    }


    protected IContextQuery getCType(Map<Object, Object> map) {
        Object query = map.get("ctype");
        if (query instanceof IContextQuery) {
            return (IContextQuery) query;
        } else if (query != null) {
            return new TypeNameQuery(query.toString());
        } else {
            return null;
        }
    }
}
