/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.eclipse.dsl.tests;

import org.codehaus.groovy.eclipse.codeassist.tests.CompletionTestCase;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jul 27, 2011
 */
public class DSLContentAssistTests extends CompletionTestCase {

    public DSLContentAssistTests(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createGenericProject();
        IProject project = env.getProject("Project");
        GroovyRuntime.addLibraryToClasspath(JavaCore.create(project), GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID);
        env.fullBuild();
        new RefreshDSLDJob(project).run(null);
        GroovyDSLCoreActivator.getDefault().getContainerListener().ignoreProject(project);
    }

    public void testDSLProposalFirstStaticField() throws Exception {
        String contents = "@Singleton class Foo { static aaa }\n Foo.s";
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ".")));
        assertProposalOrdering(proposals, "instance", "aaa");
    }

    public void testDSLProposalFirstStaticMethod() throws Exception {
        String contents = "@Singleton class Foo { static aaa() { } }\n Foo.s";
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, ".")));
        assertProposalOrdering(proposals, "getInstance", "aaa");
    }
    
    public void _testDSLProposalFirstField() throws Exception {
        // oops...don't have anything that we can do here.
    }
    
    public void testDSLProposalFirstMethod() throws Exception {
        String contents = "import groovy.swing.SwingBuilder\n" +
                "new SwingBuilder().edt {\n" +
                "this.x\n" +
                "}";
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, "this.")));
        assertProposalOrdering(proposals, "frame", "registerBinding");
    }
    
}
