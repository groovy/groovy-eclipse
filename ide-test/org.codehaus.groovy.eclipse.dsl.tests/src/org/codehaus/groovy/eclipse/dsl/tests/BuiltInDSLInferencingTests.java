/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests type inferencing for DSL scripts included with Groovy plugin
 * 
 * @author Andrew Eisenberg
 * @created Jun 16, 2011
 */
public class BuiltInDSLInferencingTests extends AbstractDSLInferencingTest {
    public BuiltInDSLInferencingTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(BuiltInDSLInferencingTests.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        doRemoveClasspathContainer = false;
        super.setUp();
    }
    
    public void testSingleton() throws Exception {
        String contents = "@Singleton class Foo { }\nFoo.instance\nFoo.getInstance()";
        int start = contents.lastIndexOf("instance");
        int end = start + "instance".length();
        assertType(contents, start, end, "Foo", "Singleton", true);
        
        start = contents.lastIndexOf("getInstance");
        end = start + "getInstance".length();
        assertType(contents, start, end, "Foo", "Singleton", true);
    }
    public void testDelegate1() throws Exception {
        String contents = "class Foo { @Delegate List<Integer> myList }\nnew Foo().get(0)";
        int start = contents.lastIndexOf("get");
        int end = start + "get".length();
        assertType(contents, start, end, "java.lang.Integer", "Delegate", true);
        assertDeclaringType(contents, start, end, "java.util.List<java.lang.Integer>", true);
    }
    
    public void testDelegate2() throws Exception {
        String contents = "class Foo { @Delegate List<Integer> myList\n @Delegate URL myUrl }\nnew Foo().get(0)\nnew Foo().getFile()";
        int start = contents.indexOf("get");
        int end = start + "get".length();
        assertType(contents, start, end, "java.lang.Integer", "Delegate", true);
        assertDeclaringType(contents, start, end, "java.util.List<java.lang.Integer>", true);
        start = contents.lastIndexOf("getFile");
        end = start + "getFile".length();
        assertType(contents, start, end, "java.lang.String", "Delegate", true);
        assertDeclaringType(contents, start, end, "java.net.URL", true);
    }
    
    public void testMixin() throws Exception {
        String contents = 
                "class FlyingAbility {\n" + 
        		"    String fly() { \"I'm the ${name} and I fly!\" }\n" + 
        		"}\n" + 
        		"class DivingAbility {\n" + 
        		"    String dive() { \"I'm the ${name} and I dive!\" }\n" + 
        		"}\n" + 
        		"\n" + 
        		"interface Vehicle {\n" + 
        		"    String getName()\n" + 
        		"}\n" + 
        		"\n" + 
        		"@Mixin(DivingAbility)\n" + 
        		"class Submarine implements Vehicle {\n" + 
        		"    String getName() { \"Yellow Submarine\" }\n" + 
        		"}\n" + 
        		"\n" + 
        		"@Mixin(FlyingAbility)\n" + 
        		"class Plane implements Vehicle {\n" + 
        		"    String getName() { \"Concorde\" }\n" + 
        		"}\n" + 
        		"\n" + 
        		"@Mixin([DivingAbility, FlyingAbility])\n" + 
        		"class JamesBondVehicle implements Vehicle {\n" + 
        		"    String getName() { \"James Bond's vehicle\" }\n" + 
        		"}\n" + 
        		"assert new Plane().fly() ==\n" + 
        		"       \"I'm the Concorde and I FLY!\"\n" + 
        		"assert new Submarine().dive() ==\n" + 
        		"       \"I'm the Yellow Submarine and I DIVE!\"\n" + 
        		"\n" + 
        		"assert new JamesBondVehicle().fly() ==\n" + 
        		"       \"I'm the James Bond's vehicle and I FLY!\"\n" + 
        		"assert new JamesBondVehicle().dive() ==\n" + 
        		"       \"I'm the James Bond's vehicle and I DIVE!\"";
        int start = contents.lastIndexOf("dive");
        int end = start + "dive".length();
        assertType(contents, start, end, "java.lang.String", "Mixin", true);
        assertDeclaringType(contents, start, end, "DivingAbility", true);

        start = contents.lastIndexOf("fly", start);
        end = start + "fly".length();
        assertType(contents, start, end, "java.lang.String", "Mixin", true);
        assertDeclaringType(contents, start, end, "FlyingAbility", true);
        
        start = contents.lastIndexOf("dive", start);
        end = start + "dive".length();
        assertType(contents, start, end, "java.lang.String", "Mixin", true);
        assertDeclaringType(contents, start, end, "DivingAbility", true);

        start = contents.lastIndexOf("fly", start);
        end = start + "fly".length();
        assertType(contents, start, end, "java.lang.String", "Mixin", true);
        assertDeclaringType(contents, start, end, "FlyingAbility", true);
    }
    
    public void testSwingBuilder1() throws Exception {
        String contents = "new groovy.swing.SwingBuilder().edt { frame }";
        int start = contents.lastIndexOf("frame");
        int end = start + "frame".length();
        assertType(contents, start, end, "javax.swing.JFrame", "SwingBuilder", true);
        assertDeclaringType(contents, start, end, "groovy.swing.SwingBuilder", true);
    }
    public void testSwingBuilder2() throws Exception {
        String contents = "groovy.swing.SwingBuilder.edtBuilder { frame }";
        int start = contents.lastIndexOf("frame");
        int end = start + "frame".length();
        assertType(contents, start, end, "javax.swing.JFrame", "SwingBuilder", true);
        assertDeclaringType(contents, start, end, "groovy.swing.SwingBuilder", true);
    }
}
