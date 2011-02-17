/*
 * Copyright 2003-2009 the original author or authors.
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.BindPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileExtensionPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AnnotatedByPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.OrPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.ProjectNaturePointcut;
import org.eclipse.core.internal.resources.NatureManager;
import org.eclipse.jdt.core.groovy.tests.search.AbstractGroovySearchTest;


/**
 * 
 * @author Andrew Eisenberg
 * @created Feb 11, 2011
 */
public class PointcutCreationTests extends AbstractGroovySearchTest {

    public static Test suite() {
        return new TestSuite(PointcutCreationTests.class);
    }
    public PointcutCreationTests(String name) {
        super(name);
    }
    
    public void testPointcutCreation1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\")");
        assertTrue("Should have been a currentType pointcut", pc instanceof CurrentTypePointcut);
        assertNull(pc.verify());
        assertEquals("java.lang.String", pc.getFirstArgument());
    }

    public void testPointcutCreation2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(findField(annotatedBy(\"java.lang.String\")))");
        assertTrue("Should have been a currentType pointcut", pc instanceof CurrentTypePointcut);
        assertNull(pc.verify());

        Object firstArgument = pc.getFirstArgument();
        assertTrue(firstArgument instanceof FindFieldPointcut);
        pc = (IPointcut) firstArgument;
        
        firstArgument = pc.getFirstArgument();
        assertTrue(firstArgument instanceof AnnotatedByPointcut);
        pc = (IPointcut) firstArgument;
    }
    
    public void testInvalidPointcutCreation1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType()");
        assertTrue("Should have been a currentType pointcut", pc instanceof CurrentTypePointcut);
        assertEquals("Expecting 1 argument, but found 0", pc.verify());
    }
    
    public void testInvalidPointcutCreation2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(findField(annotatedBy()))");
        assertTrue("Should have been a currentType pointcut", pc instanceof CurrentTypePointcut);
        assertEquals("This pointcut supports exactly one argument of type Pointcut or String", pc.verify());
    }
    
    public void testAnd1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & currentType(\"java.lang.String\")");
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());

        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals("Should have 2 argument", 2, pc.getArgumentValues().length);
    }
    
    public void testAnd2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & currentType(\"java.lang.String\") & currentType(\"java.lang.String\")");
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());

        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[2].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument());

        assertEquals("Should have 3 argument", 3, pc.getArgumentValues().length);
    }
    
    public void testAnd3() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("(currentType(\"java.lang.String\") & currentType(\"java.lang.String\")) & currentType(\"java.lang.String\")");
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[2].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument());
        
        assertEquals("Should have 3 argument", 3, pc.getArgumentValues().length);
    }
    
    public void testAnd4() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & (currentType(\"java.lang.String\") & currentType(\"java.lang.String\"))");
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[2].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument());
        
        assertEquals("Should have 3 argument", 3, pc.getArgumentValues().length);
    }
    
    public void testInvalidAnd1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & currentType(\"java.lang.String\") & currentType()");
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertEquals("Expecting 1 argument, but found 0", pc.verify());
    }
    
    public void testOr1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") | currentType(\"java.lang.String\")");
        assertEquals("Should have been an and pointcut", OrPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals("Should have 2 argument", 2, pc.getArgumentValues().length);
    }
    
    public void testOr2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") | currentType(\"java.lang.String\") | currentType(\"java.lang.String\")");
        assertEquals("Should have been an and pointcut", OrPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[2].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument());
        
        assertEquals("Should have 3 argument", 3, pc.getArgumentValues().length);
    }
    
    public void testOr3() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("(currentType(\"java.lang.String\") | currentType(\"java.lang.String\")) | currentType(\"java.lang.String\")");
        assertEquals("Should have been an and pointcut", OrPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[2].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument());
        
        assertEquals("Should have 3 argument", 3, pc.getArgumentValues().length);
    }
    
    public void testOr4() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") | (currentType(\"java.lang.String\") | currentType(\"java.lang.String\"))");
        assertEquals("Should have been an and pointcut", OrPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[2].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument());
        
        assertEquals("Should have 3 argument", 3, pc.getArgumentValues().length);
    }
    
    public void testOrAnd1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") | currentType(\"java.lang.String\") & currentType(\"java.lang.String\")");
        assertEquals("Should have been an or pointcut", OrPointcut.class, pc.getClass());
        assertNull(pc.verify());
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals("Should have 2 arguments", 2, pc.getArgumentValues().length);
        
        
        assertEquals(AndPointcut.class, pc.getArgumentValues()[1].getClass());
        
        assertEquals(CurrentTypePointcut.class, ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0].getClass());
        assertEquals(CurrentTypePointcut.class, ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[1].getClass());
        
        assertEquals("java.lang.String", ((IPointcut) ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0]).getFirstArgument());
        assertEquals("java.lang.String", ((IPointcut) ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[1]).getFirstArgument());
    }
    
    public void testAndOr1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & currentType(\"java.lang.String\") | currentType(\"java.lang.String\")");
        assertEquals("Should have been an or pointcut", OrPointcut.class, pc.getClass());
        assertNull(pc.verify());
        assertEquals("Should have 2 arguments", 2, pc.getArgumentValues().length);
        
        assertEquals(AndPointcut.class, pc.getArgumentValues()[0].getClass());
        
        assertEquals(CurrentTypePointcut.class, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass());
        assertEquals(CurrentTypePointcut.class, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[1].getClass());
        
        assertEquals("java.lang.String", ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getFirstArgument());
        assertEquals("java.lang.String", ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[1]).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
    }
    
    public void testBindAndFileExtension() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("bind(b: fileExtension(\"fdafdsfds\") )");
        assertEquals(BindPointcut.class, pc.getClass());
        assertEquals(FileExtensionPointcut.class, pc.getFirstArgument().getClass());
        assertEquals("b", pc.getFirstArgumentName());
        assertNull(pc.verify());
        assertEquals("fdafdsfds", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
    }
    
    public void testBindAndNature() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("bind(b: nature(\"fdafdsfds\") )");
        assertEquals(BindPointcut.class, pc.getClass());
        assertEquals(ProjectNaturePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("b", pc.getFirstArgumentName());
        assertNull(pc.verify());
        assertEquals("fdafdsfds", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
    }
    
    public void testBindAndFileExtensionInvalid() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("bind(fileExtension(\"fdafdsfds\") )");
        assertEquals(BindPointcut.class, pc.getClass());
        assertEquals(FileExtensionPointcut.class, pc.getFirstArgument().getClass());
        assertEquals("bind requires a named argument", pc.verify());
        assertEquals("fdafdsfds", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
    }
    
    
    public void testVariable1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("def x = fileExtension(\"fdafdsfds\")\nbind(b:x)");
        assertEquals(BindPointcut.class, pc.getClass());
        assertEquals("b", pc.getFirstArgumentName());
        assertEquals(FileExtensionPointcut.class, pc.getFirstArgument().getClass());
        assertNull(pc.verify());
        assertEquals("fdafdsfds", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
    }
    
    public void testVariable2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("def x = fileExtension(\"fdafdsfds\")\nx & x");
        assertEquals(AndPointcut.class, pc.getClass());
        assertNull(pc.verify());
        assertEquals(FileExtensionPointcut.class, pc.getArgumentValues()[0].getClass());
        assertEquals(FileExtensionPointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("fdafdsfds", ((IPointcut) pc.getArgumentValues()[0]).getFirstArgument());
        assertEquals("fdafdsfds", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
    }
    
    public void testCustomPointcut1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
        		"registerPointcut('mine', { pattern -> null })\n" +
        		"mine()");
        assertEquals("MyPointcut", pc.getClass().getName());
    }
}
