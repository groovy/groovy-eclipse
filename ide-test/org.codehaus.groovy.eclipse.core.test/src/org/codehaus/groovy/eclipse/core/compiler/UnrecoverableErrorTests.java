/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.compiler;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;

/**
 * All of these tests here should produce {@link ModuleNode}s with
 * encounteredUnrecoverableError set to true
 *
 * @author andrew
 * @created Feb 9, 2011
 */
public class UnrecoverableErrorTests extends EclipseTestCase {
    private GroovySnippetCompiler compiler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyRuntime.addGroovyRuntime(testProject.getProject());
        compiler = new GroovySnippetCompiler(testProject.getGroovyProjectFacade());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        compiler.cleanup();
    }

    public ModuleNode compileScript(String script) {
        long start = System.currentTimeMillis();
        ModuleNode result = compiler.compile(script, "Test");
        System.out.println("Time to compile: " + (System.currentTimeMillis() - start) + " ms");
        return result;
    }

    public void testGRE926() throws Exception {
        ModuleNode result = compileScript("package a\n" +
        		"import javax.swing.text.html.HTML; \n" +
        		"   void nuthin() {\n" +
        		"         if (! (this instanceof HTML/*_*/) {\n" +
        		"            \n" +
        		"         }\n" +
 "    } ");
        // should be true, but is false
        assertTrue(result.encounteredUnrecoverableError());
    }

}
