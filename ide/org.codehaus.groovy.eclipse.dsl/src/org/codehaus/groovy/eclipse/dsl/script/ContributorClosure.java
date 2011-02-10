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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.contexts.ContextStore;
import org.codehaus.groovy.eclipse.dsl.contexts.IContext;
import org.codehaus.groovy.eclipse.dsl.contributions.ContributionGroup;

/**
 * A closure that generates a contributor
 * @author andrew
 * @created Nov 17, 2010
 */
public class ContributorClosure extends Closure {

    private static final long serialVersionUID = -1075495425966266033L;

    private final ContextStore store;
    
    public ContributorClosure(ContextStore store) {
        super(null, null);
        this.store = store;
    }
    
    /**
     * takes a list of contexts and a closure
     */
    @Override
    public Object call(Object[] args) {
        List<IContext> contexts;
        if (args[0] instanceof IContext) {
            contexts = Collections.singletonList((IContext) args[0]);
        } else if (args[0] instanceof List<?>) {
            contexts = (List<IContext>) args[0];
        } else if (args[0] instanceof Map<?,?>) {
            contexts = null;  // don't support yet
//            contexts = Collections.singletonList();
        } else {
            throw new IllegalArgumentException("Invalid argument for contributor.  Must be a context");
        }
        Closure contribClosure = (Closure) args[1];
        ContributionGroup contribution = new ContributionGroup(contribClosure);
        if (contexts != null) {
            store.addAllContexts(contexts, contribution);
        }
        return contribution;
    }
}
