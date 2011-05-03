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

package org.codehaus.groovy.eclipse.codeassist.requestor;

/**
 * @author Andrew Eisenberg
 * @created Nov 9, 2009
 * The set of different contexts available for content assist
 */
public enum ContentAssistLocation {
    IMPORT, // import statements.  Type proposals are available.  They do not cause an additional import statement, and filter out proposals of types already imported
    PARAMETER, // types for parameters.  Types proposals are available only.  They cause the standard import statement to appear if required
    IMPLEMENTS,  // types for implements statements
    EXTENDS,  // types for extends statements
    EXCEPTIONS,  // exception types
    EXPRESSION, // part of an expression.  (eg- foo.bar^, or foo().bar^)
    CONSTRUCTOR, // a constructor call types and their constructors are available
    STATEMENT, // start of a new statement.  So, everything from expressions are available, but also local variables and types should be included
    CLASS_BODY,  // inside a class body.  Here, type proposals, modifiers, and overridable methods should appear
    SCRIPT, // inside a script, but not in an expression. Here, type proposals,
            // modifiers, and overridable methods, as well as statements should
            // appear
}
