/*******************************************************************************
 * Copyright (c) 2009-2014 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.core.tests.basic;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import junit.framework.Test;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GroovySimpleTests_Compliance_1_8 extends AbstractRegressionTest {

	public GroovySimpleTests_Compliance_1_8(String name) {
		super(name);
	}

	public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(),
				ClassFileConstants.JDK1_8);
	}

	public static Class testClass() {
		return GroovySimpleTests_Compliance_1_8.class;
	}

    protected void setUp() throws Exception {
		super.setUp();
		GroovyCompilationUnitDeclaration.defaultCheckGenerics=true;
		GroovyParser.debugRequestor = new DebugRequestor();
		complianceLevel = ClassFileConstants.JDK1_8;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		GroovyCompilationUnitDeclaration.defaultCheckGenerics=false;
		GroovyParser.debugRequestor = null; 
	}

	protected String[] getDefaultClassPaths() {
        String[] cps = super.getDefaultClassPaths();
        String[] newcps = new String[cps.length+2];
        System.arraycopy(cps,0,newcps,0,cps.length);
        try {
        	URL groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-2.3.0-rc-2.jar");
        	if (groovyJar==null) {
	        	groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-2.2.2.jar");
	        	if (groovyJar==null) {
		        	groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-2.1.8.jar");
		        	if (groovyJar==null) {
			            groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-2.0.7.jar");
			            if (groovyJar==null) {
							groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-1.8.6.jar");
			            }
		        	}
	        	}
        	}
            newcps[newcps.length-1] = FileLocator.resolve(groovyJar).getFile();
	        // FIXASC think more about why this is here... the tests that need it specify the option but that is just for
	        // the groovy class loader to access it.  The annotation within this jar needs to be resolvable by the compiler when
	        // building the annotated source - and so I suspect that the groovyclassloaderpath does need merging onto the project
	        // classpath for just this reason, hmm.
	        newcps[newcps.length-2] = FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar")).getFile();
	        // newcps[newcps.length-4] = new File("astTransformations/spock-core-0.1.jar").getAbsolutePath();
        } catch (IOException e) {
            fail("IOException thrown " + e.getMessage());
        }
        return newcps;
    }
    
    public void testDefaultAndStaticMethodInInterface() {
		assertTrue("Groovy compiler levele is less than 2.3",
				GroovyUtils.GROOVY_LEVEL >= 23);
		assertTrue("JRE Compliance level is less than 1.8",
				isJRELevel(AbstractCompilerTest.F_1_8));
		Map customOptions= getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);		
		this.runConformTest(
				// test directory preparation
				true, /* flush output directory */
				new String[] { /* test files */
				"p/IExample.java",
				"package p;\n" + "public interface IExample {\n"
						+ "   void testExample();\n"
						+ "   static void callExample() {}\n" 
						+ "   default void callDefault() {}\n"
						+ "}\n",
				"p/ExampleGr.groovy",
				"package p\n" + "class ExampleGr implements IExample {\n"
						+ "public void testExample() {}\n" + "}\n" }, 		// compiler options
						null /* no class libraries */,
						customOptions /* custom options */,
						// compiler results
						"" /* expected compiler log */,
						// runtime results
						"" /* expected output string */,
						null /* do not check error string */,
						// javac options
						new JavacTestOptions("-source 1.8") /* javac test options */);
	}

}
