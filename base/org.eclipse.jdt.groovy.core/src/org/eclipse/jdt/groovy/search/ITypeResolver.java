/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;

/**
 * Used by the inferencing engine and type lookups that require the ability to resolve types from strings
 *
 * @author Andrew Eisenberg
 * @created Oct 18, 2011
 */
public interface ITypeResolver {
    void setResolverInformation(ModuleNode module, JDTResolver resolver);
}
