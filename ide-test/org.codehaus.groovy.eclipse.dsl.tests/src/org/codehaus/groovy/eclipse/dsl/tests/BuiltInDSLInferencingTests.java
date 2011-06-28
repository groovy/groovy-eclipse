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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.classpath.DSLDContainerInitializer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ExternalPackageFragmentRoot;

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
        try {
            wait(60*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean containsGroovyDSLD() {
        IStorage[] allContextKeys = GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(project).getAllContextKeys();
        for (IStorage storage : allContextKeys) {
            if (storage.getName().equals("groovy.dsld")) {
                return true;
            }
        }
        return false;
    }
    
    public void testSanity() throws Exception {
        IJavaProject javaProject = JavaCore.create(project);
        assertTrue("Should have DSL support classpath container", 
                GroovyRuntime.hasClasspathContainer(javaProject, 
                        GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID));
        
        IClasspathContainer container = JavaCore.getClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, javaProject);
        IClasspathEntry[] cpes = container.getClasspathEntries();
        assertEquals("Wrong number of classpath entries found: " + Arrays.toString(cpes), 2, cpes.length);
        
        IClasspathEntry pluginEntry = null;
        IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
        for (IClasspathEntry entry : entries) {
            if (entry.getPath().toString().contains("plugin_dsld")) {
                pluginEntry = entry;
            }
        }
        
        IPackageFragmentRoot root = null;
        List<String> elements = new ArrayList<String>();
        for (IJavaElement elt : javaProject.getChildren()) {
            elements.add(elt.getElementName());
            if (elt.getElementName().contains("plugin_dsld")) {
                root = (IPackageFragmentRoot) elt;
            }
        }

        List<String> possibleFrags = new ArrayList<String>();
        for (IPackageFragment frag : javaProject.getPackageFragments()) {
            if (frag.getElementName().equals("dsld")) {
                possibleFrags.add(frag.toString());
                possibleFrags.add("  [");
                for (IJavaElement child : frag.getChildren()) {
                    possibleFrags.add("    " + child.getElementName());
                }
                possibleFrags.add("  ]");
            }
        }
        
        assertNotNull("Did not find the Plugin DSLD classpath entry.  Exsting resolved roots:\n" + printList(elements) + "\nOther DSLD fragments:\n" + printList(possibleFrags), pluginEntry);
        assertNotNull("Plugin DSLD classpath entry should exist.  Exsting resolved roots:\n" + printList(elements) + "\nOther DSLD fragments:\n" + printList(possibleFrags), root);
        assertTrue("Plugin DSLD classpath entry should exist", root.exists());

        ExternalPackageFragmentRoot ext = (ExternalPackageFragmentRoot) root;
        ext.resource().refreshLocal(IResource.DEPTH_INFINITE, null);
        root.close();
        root.open(null);
        
        
        IPackageFragment frag = root.getPackageFragment("dsld");
        assertTrue("DSLD package fragment should exist", frag.exists());
    }

    private String printList(List<String> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (String elt : elements) {
            sb.append(elt).append("\n");
        }
        sb.append("]");
        return sb.toString();
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
