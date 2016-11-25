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
package org.codehaus.groovy.eclipse.codeassist.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.test.EclipseTestSetup;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 */
public class AllCompletionTests {
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AllCompletionTests.class.getName());
        suite.addTestSuite(CommandChainCompletionTests.class);
        suite.addTestSuite(ConstructorCompletionTests.class);
        suite.addTestSuite(ContentAssistLocationTests.class);
        suite.addTestSuite(ContextInformationTests.class);
        suite.addTestSuite(DefaultGroovyMethodCompletionTests.class);
        suite.addTestSuite(DefaultMethodContentAssistTests.class);
        suite.addTestSuite(ExtendedCompletionContextTests.class);
        suite.addTestSuite(FieldCompletionTests.class);
        suite.addTestSuite(FindImportsRegionTests.class);
        suite.addTestSuite(GenericCompletionTests.class);
        suite.addTestSuite(GroovyLikeCompletionTests.class);
        suite.addTestSuite(GuessingCompletionTests.class);
        suite.addTestSuite(InferencingCompletionTests.class);
        suite.addTestSuite(InnerTypeCompletionTests.class);
        suite.addTestSuite(LocalVariableCompletionTests.class);
        suite.addTestSuite(MethodCompletionTests.class);
        suite.addTestSuite(NewFieldCompletionTests.class);
        suite.addTestSuite(OtherCompletionTests.class);
        suite.addTestSuite(ProposalProviderAndFilterTests.class);
        suite.addTestSuite(RelevanceTests.class);
        suite.addTestSuite(StaticImportsCompletionTests.class);
        suite.addTestSuite(TypeCompletionTests.class);
        suite.addTestSuite(TypeCompletionTests2.class);
        return new EclipseTestSetup(suite);
    }
}
