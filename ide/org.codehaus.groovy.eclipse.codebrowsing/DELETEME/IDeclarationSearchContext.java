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
package org.codehaus.groovy.eclipse.codebrowsing;

/**
 * Get the current declaration search context.
 * 
 * This is an optional interface to implement. Most declaration search
 * processors will use the global context. However some declaration searches are
 * specific to some context.
 * 
 * For example, a Grails application can have declaration search processors
 * implemented for navigation between domain, view and controller classes. These
 * have no purpose in any other projects. An implementation of
 * IDeclarationSearchContext that determines if the current project is a Grails
 * project is created to limit the scope of the Grails specific search
 * processors.
 * 
 * @author emp
 */
public interface IDeclarationSearchContext {
	public boolean isActiveContext();
}