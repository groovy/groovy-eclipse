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
    /**
     * Annotations. Type proposals are available and these proposals are
     * restricted to annotations
     */
    ANNOTATION,

    /**
     * import statements. Type proposals are available. They do not cause an
     * additional import statement, and filter out proposals of types already
     * imported
     */
    IMPORT,

    /**
     * package declarations. package proposals only.
     */
    PACKAGE,

    /**
     * types for parameters. Types proposals are available only. They cause the
     * standard import statement to appear if required
     */
    PARAMETER,
    /**
     * types for implements clauses
     */
    IMPLEMENTS,
    /**
     * types for extends clauses
     */
    EXTENDS,
    /**
     * exception types
     */
    EXCEPTIONS,
    /**
     * part of an expression.  (eg- foo.bar^, or foo().bar^)
     */
    EXPRESSION,
    /**
     * a constructor call types and their constructors are available
     */
    CONSTRUCTOR,
    /**
     * start of a new statement. So, everything from expressions are available,
     * but also local variables and types should be included
     * Eg- all cases of 'A' fall into the STATEMENT location
     *
     * <code>
     * A
     * foo.bar(A)
     * foo.bar(B, A)
     * foo.bar A
     * foo.bar() A // even though syntax error
     * </code>
     *
     * this is not really a 'statement' in the normal sense, but really a
     * new expression that is not part of a dotted expression
     */
    STATEMENT,
    /**
     * inside a class body.  Here, type proposals, modifiers, and overridable methods should appear
     */
    CLASS_BODY,
    /**
     * inside a script, but not in an expression. Here, type proposals,
     * modifiers, and overridable methods, as well as statements should
     * appear
     */
    SCRIPT,
    /**
     * Method call at a paren or a comma. Here should show
     * context information of the relevant method only
     */
    METHOD_CONTEXT
}
