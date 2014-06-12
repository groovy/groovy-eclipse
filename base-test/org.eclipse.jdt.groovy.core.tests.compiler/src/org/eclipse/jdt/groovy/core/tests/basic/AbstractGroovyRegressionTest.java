/*******************************************************************************
 * Copyright (c) 2014 SpringSource and others.
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

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;

public abstract class AbstractGroovyRegressionTest extends AbstractRegressionTest {
	
	public AbstractGroovyRegressionTest(String name) {
		super(name);
	}
	
	/** 
     * Include the groovy runtime jars on the classpath that is used.
     * Other classpath issues can be seen in TestVerifier/VerifyTests and only when
     * the right prefixes are registered in there will it use the classloader with this
     * classpath rather than the one it conjures up just to load the built code.
     */
    protected String[] getDefaultClassPaths() {
        String[] cps = super.getDefaultClassPaths();
        String[] newcps = new String[cps.length+2];
        System.arraycopy(cps,0,newcps,0,cps.length);
        try {
        	URL groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-2.3.3.jar");
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
 
	protected void setUp() throws Exception {
		super.setUp();
		GroovyCompilationUnitDeclaration.defaultCheckGenerics=true;
		GroovyParser.debugRequestor = new DebugRequestor();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		GroovyCompilationUnitDeclaration.defaultCheckGenerics=false;
		GroovyParser.debugRequestor = null; 
	}


}
