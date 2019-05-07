/*
 * Copyright 2009-2019 the original author or authors.
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

import org.eclipse.jdt.core.IPackageFragment
import org.junit.Test

final class CodeSelectPackageTests extends BrowsingTestSuite {

    @Test
    void testCodeSelectOnPackageDeclaration() {
        def unit = addGroovySource('def pi = Math.PI', 'script', 'w.x.y.z')
        unit.becomeWorkingCopy(null)

        def elems = unit.codeSelect(unit.source.indexOf('z'), 1)
        assert elems.size() == 1
        assert elems[0].elementName == 'w.x.y.z'
        assert elems[0] instanceof IPackageFragment

        elems = unit.codeSelect(unit.source.indexOf('y'), 1)
        assert elems.size() == 1
        assert elems[0].elementName == 'w.x.y'
        assert elems[0] instanceof IPackageFragment
    }
}
