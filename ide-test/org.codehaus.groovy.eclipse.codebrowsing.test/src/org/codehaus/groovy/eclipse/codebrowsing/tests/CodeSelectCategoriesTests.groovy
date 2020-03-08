/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy

import org.junit.Test

final class CodeSelectCategoriesTests extends BrowsingTestSuite {

    @Test
    void testDGM1() {
        def sources = [
            'this.each { }'
        ]
        assertCodeSelect(sources, 'each')
    }

    @Test
    void testDGM2() {
        def sources = [
            '[str: String.class].getAt(String.class)'
        ]
        def elem = assertCodeSelect(sources, 'getAt')
        assert elem.parameterTypes.length == 2
        assert elem.parameterTypes[0] == 'Ljava.util.Map<TK;TV;>;'
        assert elem.parameterTypes[1] == isAtLeastGroovy(30) ? 'java.lang.Object' : 'TK;'
    }

    @Test
    void testGroovyCategory() {
        def sources = [
            'class MyCategory { static doNothing(Object o) { } }',
            'use(MyCategory) { doNothing() }'
        ]
        assertCodeSelect(sources, 'doNothing')
    }
}
