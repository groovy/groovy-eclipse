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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.codehaus.groovy.eclipse.codeassist.mock.MockProposalFilter1
import org.codehaus.groovy.eclipse.codeassist.mock.MockProposalFilter2
import org.codehaus.groovy.eclipse.codeassist.mock.MockProposalProvider1
import org.codehaus.groovy.eclipse.codeassist.mock.MockProposalProvider2
import org.codehaus.groovy.eclipse.test.ui.Extender1
import org.codehaus.groovy.eclipse.test.ui.Extender2
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link IProposalFilter} and {@link IProposalProvider}.
 */
final class ProposalProviderAndFilterTests extends CompletionTestSuite {

    @Before
    void setUp() {
        MockProposalFilter1.reset()
        MockProposalFilter2.reset()
        MockProposalProvider1.reset()
        MockProposalProvider2.reset()
    }

    @After
    void tearDown() {
        removeNature(Extender1.NATURE1, Extender2.NATURE2)
    }

    @Test
    void testProvidersAndFilters1() {
        addNature(Extender2.NATURE2)

        String contents = 'println th'
        createProposalsAtOffset(contents, getIndexOf(contents, ' th'))

        assert !MockProposalProvider1.wasProviderCalled() : 'MockProposalProvider1 should not have been called'
        assert !MockProposalFilter1.wasFilterCalled()     : 'MockProposalFilter1 should not have been called'
        assert  MockProposalProvider2.wasProviderCalled() : 'MockProposalProvider2 should have been called'
        assert  MockProposalFilter2.wasFilterCalled()     : 'MockProposalFilter2 should have been called'
    }

    @Test
    void testProvidersAndFilters2() {
        addNature(Extender1.NATURE1, Extender2.NATURE2)

        String contents = 'println th'
        createProposalsAtOffset(contents, getIndexOf(contents, ' th'))

        assert MockProposalProvider1.wasProviderCalled() : 'MockProposalProvider1 should have been called'
        assert MockProposalFilter1.wasFilterCalled()     : 'MockProposalFilter1 should have been called'
        assert MockProposalProvider2.wasProviderCalled() : 'MockProposalProvider2 should have been called'
        assert MockProposalFilter2.wasFilterCalled()     : 'MockProposalFilter2 should have been called'
    }
}
