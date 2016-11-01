/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import static java.util.Arrays.asList;

/**
 * @author Andrew Eisenberg
 * @created Aug 5, 2009
 */
public final class CodeSelectCategoriesTests extends BrowsingTestCase {

    public static junit.framework.Test suite() {
        return newTestSuite(CodeSelectCategoriesTests.class);
    }

    public void testDGM() throws Exception {
        String contents = "this.each { }";
        assertCodeSelect(asList(contents), "each");
    }

    public void testGroovyCategory() throws Exception {
        String contents[] = {
            "class MyCategory { static doNothing(Object o) { } }",
            "use(MyCategory) { doNothing() }",
        };
        assertCodeSelect(asList(contents), "doNothing");
    }
}
