 /*
 * Copyright 2003-2013 the original author or authors.
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

package org.eclipse.jdt.core.groovy.tests.search;

import java.util.Arrays;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.Activator;

/**
 * Tests for all Groovy 2.1 specific things
 * for example, {@link groovy.lang.DelegatesTo}
 * @author Andrew Eisenberg
 * @created Feb 5, 2013
 */
public class Groovy21InferencingTests extends AbstractInferencingTest {
 
    public static Test suite() {
        return buildTestSuite(Groovy21InferencingTests.class);
    }

    public Groovy21InferencingTests(String name) {
        super(name);
    }
    
    // tests CompareToNullExpression
    public void testDelegatesTo1() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21 ) {
            return;
        }
        
        String contents = 
                "class Other { }\n" +
        		"def meth(@DelegatesTo(Other) Closure c) { }\n" +
        		"meth { delegate }";
        
        String toFind = "delegate";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "Other");
   }
    public void testDelegatesTo1a() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21 ) {
            return;
        }
        
        String contents = 
                "class Other { }\n" +
                "def meth(@DelegatesTo(Other) c) { }\n" +
                "meth { delegate }";
        
        String toFind = "delegate";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "Other");
   }
   public void testDelegatesTo2() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21 ) {
            return;
        }
        
        String contents = 
                "class Other { int xxx }\n" +
                "def meth(@DelegatesTo(Other) Closure c) { }\n" +
                "meth { xxx }";
        
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
   }
   public void testDelegatesTo3() throws Exception {
       if (GroovyUtils.GROOVY_LEVEL < 21 ) {
           return;
       }
       
       String contents = 
               "def meth(@DelegatesTo(List) Closure c) { }\n" +
               "meth { delegate }";
       
       String toFind = "delegate";
       int start = contents.lastIndexOf(toFind);
       int end = start + toFind.length();
       assertType(contents, start, end, "java.util.List");
   }
   public void testDelegatesTo4() throws Exception {
       if (GroovyUtils.GROOVY_LEVEL < 21 ) {
           return;
       }
       
       String contents = 
               "def meth(int x, int y, @DelegatesTo(List) Closure c) { }\n" +
               "meth 1, 2, { delegate }";
       
       String toFind = "delegate";
       int start = contents.lastIndexOf(toFind);
       int end = start + toFind.length();
       assertType(contents, start, end, "java.util.List");
   }
   
   // expected to be broken
   public void testDelegatesTo5() throws Exception {
       if (GroovyUtils.GROOVY_LEVEL < 21 ) {
           return;
       }
       
       String contents = 
               "def meth(int x, int y, @DelegatesTo(List<String) Closure c) { }\n" +
               "meth { delegate }";
       
       String toFind = "delegate";
       int start = contents.lastIndexOf(toFind);
       int end = start + toFind.length();
       assertType(contents, start, end, "Search");
   }
   
   
   public void testStaticCompile1() throws Exception {
       if (GroovyUtils.GROOVY_LEVEL < 21 ) {
           return;
       }
       boolean origEnabled = Activator.getDefault().getBooleanPreference(null, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
       String origPatterns = Activator.getDefault().getStringPreference(null, Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);

       try {
           // the type checking script
           IPath robotPath = env.addPackage(project.getFolder("src").getFullPath(), "robot");
           env.addGroovyClass(robotPath, "RobotMove", "package robot\n" + 
           		"import org.codehaus.groovy.ast.expr.MethodCall\n" + 
           		"import org.codehaus.groovy.ast.expr.VariableExpression\n" + 
           		"unresolvedVariable { VariableExpression var ->\n" + 
           		"    if ('robot' == var.name) {\n" + 
           		"        def robotClass = context.source.AST.classes.find { it.name == 'Robot' }\n" + 
           		"        storeType(var, robotClass)\n" + 
           		"        handled = true\n" + 
           		"    }\n" + 
           		"}\n" + 
           		"afterMethodCall { MethodCall mc ->\n" + 
           		"    def method = getTargetMethod(mc)\n" + 
           		"    if (mc.objectExpression.name == 'robot' && method.name == 'move') {\n" + 
           		"        def args = getArguments(mc)\n" + 
           		"        if (args && isConstantExpression(args[0]) && args[0].value instanceof String) {\n" + 
           		"            def content = args[0].text\n" + 
           		"            if (!(content in ['left', 'right', 'backward', 'forward'])) {\n" + 
           		"                addStaticTypeError(\"'${content}' is not a valid direction\", args[0])\n" + 
           		"            }\n" + 
           		"        }\n" + 
           		"    }\n" + 
           		"}");
           
           // set the script folders
           Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Boolean.TRUE.toString());
           Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS, "src/robot/*Move.groovy,y");
    
           String contents = 
                "import groovy.transform.TypeChecked\n" + 
           		"class Robot {\n" + 
           		"    void move(String dist) { println \"Moved $dist\" }\n" + 
           		"}\n" + 
           		"@TypeChecked(extensions = 'robot/RobotMove.groovy')\n" + 
           		"void operate() {\n" + 
           		"    robot.move \"left\"\n" + 
           		"}";
           
           int start = contents.lastIndexOf("move");
           int end = start + "move".length();
           assertType(contents, start, end, "java.lang.Void");
           start = contents.lastIndexOf("robot");
           end = start + "robot".length();
           assertType(contents, start, end, "Robot");

           // also, just make sure no problems
           env.fullBuild(project.getFullPath());
           Problem[] problems = env.getProblemsFor(project.getFullPath());
           assertEquals("Should have found no problems in:\n" + Arrays.toString(problems), 0, problems.length);
       } finally {
           Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, String.valueOf(origEnabled));
           Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS, origPatterns);
       }       
   }
}