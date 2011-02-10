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

import org.codehaus.groovy.eclipse.dsl.script.IContextQueryResult;

/**
 * A context that can be defined in a GDSL file
 * @author andrew
 * @created Nov 17, 2010
 */
public interface IContext {

    IContextQueryResult<?> matches(ContextPattern pattern);
    
    /**
     * A unique identifier for the container that created this context.
     * This could be a script name.
     * @return
     */
    String getContainerIdentifier();
    
}
