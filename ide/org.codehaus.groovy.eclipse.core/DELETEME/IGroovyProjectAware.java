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
package org.codehaus.groovy.eclipse.core;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;

/**
 * Interface implemented by instances that are aware of Groovy projects. The single
 * {@link #setGroovyProject(GroovyProject)} method is called whenever the project changes. The method is not guaranteed
 * to be called at all, so implementers must check that it has in fact been set.
 * 
 * @author empovazan
 */
public interface IGroovyProjectAware {
	public void setGroovyProject(GroovyProjectFacade facade);
}
