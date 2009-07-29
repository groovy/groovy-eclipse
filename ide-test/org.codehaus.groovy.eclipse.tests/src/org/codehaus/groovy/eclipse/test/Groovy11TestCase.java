/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.test;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.resources.IMarker;

/**
 * This class is to test building with the new Groovy 1.1 Language features.
 * 
 * @author Aaron J Tarter
 */
public class Groovy11TestCase extends EclipseTestCase {
	
	public void testStaticImport() throws Exception {
		String staticPkg = "statictest";
		String importPkg = "importtest";
		testProject.createGroovyTypeAndPackage(
				staticPkg,
				"Constants.groovy",
				"class Constants { " +
				"   static Integer myVar = new Integer(5);" +
				"}");
		testProject.createGroovyTypeAndPackage(
				importPkg, 
				"TryToImport.groovy", 
		    	"import static statictest.Constants.myVar;" +
				"class TryToImport {" +
				"   public void testVar(){" +
				"       int v = myVar.intValue();" +
				"   }" +
				"}");
		buildAndCheckErrors();
		testProject.deletePackage(staticPkg);
		testProject.deletePackage(importPkg);
    }
	
	public void testGeneric() throws Exception {
		String genericPkg = "generictest";
		testProject.createGroovyTypeAndPackage(
				genericPkg,
				"GenericTest.groovy",
				"List<String> l = new ArrayList<String>();" +
				"String s = l.get(0);"
				);
		buildAndCheckErrors();
		testProject.deletePackage(genericPkg);
    }
	
	public void testAnnotation() throws Exception {
		String annotationTestPkg = "annotationtest";
		String annotationPkg = "annotation";
		testProject.createJavaTypeAndPackage(
				annotationPkg,
				"FeatureRequest.java",
				"public @interface FeatureRequest {" + 
				"    String key();" +
				"    String summary();" +
				"    String assignee() default \"[unassigned]\";" +
				"    String status() default \"[open]\";" + 
				"    String targetVersion() default \"[unassigned]\";" +
				"}");
		testProject.createGroovyTypeAndPackage(
				annotationTestPkg,
				"AnnotationTest.groovy",
				"import annotation.FeatureRequest;" +
				"@FeatureRequest(" +
                "    key=\"GROOVY-9999\"," +
                "    summary=\"Support Graphical Annotations\"," + 
                "    assignee=\"Pete\"," + 
                "    status=\"Open\"," +  
                "    targetVersion=\"5.0\"" + 
                ")" + 
                "class AnnotationTest {" +                    
                "}"
				);
		buildAndCheckErrors();		
		testProject.deletePackage(annotationPkg);
		testProject.deletePackage(annotationTestPkg);
	}
	
	public void testForLoop() throws Exception {
		String forLoopPkg = "forloop";
		testProject.createGroovyTypeAndPackage(
				forLoopPkg,
				"ForLoop.groovy",
                "for(int i = 0; i < 100; i++) {" +                    
                "    println i" +
                "}"
				);
		buildAndCheckErrors();		
		testProject.deletePackage(forLoopPkg);
	}
	
	public void testFuncCallWithNoParens() throws Exception {
		String noParensPkg = "noparens";
		testProject.createGroovyTypeAndPackage(
				noParensPkg,
				"NoParens.groovy",
				"compare fund: \"SuperInvestment\", withBench: \"NIKEI\"\n" +
				"monster.move from: [3,4], to: [4,5]\n" + 
                "def myMethod(def parm1, def parm2 ) {}\n" +
                "myMethod parm1:0, parm2:\"test\"\n" + 
                "println \"This is an unnamed one argument test\"\n"
				);
		buildAndCheckErrors();		
		testProject.deletePackage(noParensPkg);
	}
	
	public void testElvisOperator() throws Exception {
		String elvisPkg = "elvis";
		testProject.createGroovyTypeAndPackage(
				elvisPkg,
				"Elvis.groovy",
                "def foo = foo ?: bar"
				);
		buildAndCheckErrors();		
		testProject.deletePackage(elvisPkg);
	}
	
	public void testIssue2435() throws Exception {
		String testPkg = "test";
		testProject.createGroovyTypeAndPackage(
				testPkg,
				"TheClass2435.groovy",
                "interface IFace { List getFoos()\n" + 
		         "    String getBar() }\n" +
                 "abstract class AbClass { abstract IFace getFace() }\n" +
                 "class TheClass extends AbClass {\n" +
		         "    IFace face = [\n" + 
		         "        getFoos : { [1, 2] },\n" +
		         "        getBar : { 'aBar' }\n" +
		         "    ] as IFace\n" +
		         "}\n" +
		         "class Bugg {\n" +
		         "    static void main(args) { new TheClass().face.foos }\n" +
		         "}\n"
		);
		buildAndCheckErrors();		
		testProject.deletePackage(testPkg);
	}
	
	public void buildAndCheckErrors() throws Exception {
		fullProjectBuild();
		IMarker[] markers = getFailureMarkers();
		StringBuffer msg = new StringBuffer();
		for(int i = 0; i < markers.length; i++) {
			msg.append(markers[i].getResource()+"\n");
		}
		assertEquals("Unexpected error markers found in reosurces:\n"+msg.toString(), 0, markers.length);
	}
	@Override
	protected void setUp() throws Exception {
		super.setUp();
        GroovyRuntime.addGroovyRuntime(testProject.getProject());
	}
}
