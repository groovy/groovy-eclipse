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

import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.FinalPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PrivatePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PublicPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.StaticPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.BindPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClassPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingMethodPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingScriptPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileExtensionPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindAnnotationPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindPropertyPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.NamePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.OrPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.ProjectNaturePointcut;
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
        assertValidPointcut(pc);
        assertEquals("java.lang.String", pc.getFirstArgument());
    }
    public void testPointcutCreation2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(fields(annotatedBy(\"java.lang.String\")))");
        assertTrue("Should have been a currentType pointcut", pc instanceof CurrentTypePointcut);
        assertValidPointcut(pc);

        Object firstArgument = pc.getFirstArgument();
        assertTrue(firstArgument instanceof FindFieldPointcut);
        pc = (IPointcut) firstArgument;
        
        firstArgument = pc.getFirstArgument();
        assertTrue(firstArgument instanceof FindAnnotationPointcut);
        pc = (IPointcut) firstArgument;
    }
    
    public void testValidPointcutCreation1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType()");
        assertTrue("Should have been a currentType pointcut", pc instanceof CurrentTypePointcut);
        assertValidPointcut(pc);
    }
    
    public void testInvalidPointcutCreation1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("fileExtension()");
        assertTrue("Should have been a currentType pointcut", pc instanceof FileExtensionPointcut);
        assertInvalidPointcut("Expecting 1 argument, but found 0.  Consider using '&' or '|' to connect arguments.", pc);
    }

    public void testValidPointcutCreation2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(fields(annotatedBy()))");
        assertTrue("Should have been a currentType pointcut", pc instanceof CurrentTypePointcut);
        assertValidPointcut(pc);
    }
    
    public void testInvalidPointcutCreation2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(fields(isStatic(\"foo\")))");
        assertTrue("Should have been a currentType pointcut", pc instanceof CurrentTypePointcut);
        assertInvalidPointcut("This pointcut does not take any arguments.", pc);
    }
    
    public void testAnd1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & currentType(\"java.lang.String\")");
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());

        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals("Should have 2 argument", 2, pc.getArgumentValues().length);
    }
    
    public void testAnd2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & currentType(\"java.lang.String\") & currentType(\"java.lang.String\")");
        pc = pc.normalize();
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
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
        pc = pc.normalize();
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
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
        pc = pc.normalize();
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[2].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument());
        
        assertEquals("Should have 3 argument", 3, pc.getArgumentValues().length);
    }
    
    public void testValidAnd1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & currentType(\"java.lang.String\") & currentType()");
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertValidPointcut(pc);
    }

    public void testInvalidAnd1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") & currentType(\"java.lang.String\") & fileExtension()");
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getClass());
        assertInvalidPointcut("Expecting 1 argument, but found 0.  Consider using '&' or '|' to connect arguments.", pc);
    }
    
    public void testOr1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") | currentType(\"java.lang.String\")");
        assertEquals("Should have been an and pointcut", OrPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
        assertEquals(CurrentTypePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
        
        assertEquals("Should have 2 argument", 2, pc.getArgumentValues().length);
    }
    
    public void testOr2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("currentType(\"java.lang.String\") | currentType(\"java.lang.String\") | currentType(\"java.lang.String\")");
        pc = pc.normalize();
        assertEquals("Should have been an and pointcut", OrPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
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
        pc = pc.normalize();
        assertEquals("Should have been an and pointcut", OrPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
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
        pc = pc.normalize();
        assertEquals("Should have been an and pointcut", OrPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
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
        assertValidPointcut(pc);
        
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
        assertValidPointcut(pc);
        assertEquals("Should have 2 arguments", 2, pc.getArgumentValues().length);
        
        assertEquals(AndPointcut.class, pc.getArgumentValues()[0].getClass());
        
        assertEquals(CurrentTypePointcut.class, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass());
        assertEquals(CurrentTypePointcut.class, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[1].getClass());
        
        assertEquals("java.lang.String", ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getFirstArgument());
        assertEquals("java.lang.String", ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[1]).getFirstArgument());
        
        assertEquals(CurrentTypePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("java.lang.String", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
    }
    
    public void testEnclosing1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("enclosingClass(properties(isStatic()) & name(\"yes\"))");
        assertEquals("Should have been an enclosingClass pointcut", EnclosingClassPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getFirstArgument().getClass());
        pc = (IPointcut) pc.getFirstArgument();
        assertEquals("Should have 2 arguments", 2, pc.getArgumentValues().length);

        assertEquals(FindPropertyPointcut.class, pc.getArgumentValues()[0].getClass());
        assertEquals(StaticPointcut.class, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass());
        assertEquals("Expecting no arguments", 0, ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getArgumentValues().length);
        
        assertEquals(NamePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("yes", ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0]);
    }
    
    public void testEnclosing2() throws Exception {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut("enclosingField(properties(isPublic()) & name(\"yes\"))");
        assertEquals("Should have been an enclosingField pointcut", EnclosingFieldPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getFirstArgument().getClass());
        pc = (IPointcut) pc.getFirstArgument();
        assertEquals("Should have 2 arguments", 2, pc.getArgumentValues().length);
        
        assertEquals(FindPropertyPointcut.class, pc.getArgumentValues()[0].getClass());
        assertEquals(PublicPointcut.class, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass());
        assertEquals("Expecting no arguments", 0, ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getArgumentValues().length);
        
        assertEquals(NamePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("yes", ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0]);
    }
    
    public void testEnclosing3() throws Exception {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut("enclosingMethod(properties(isPrivate()) & name(\"yes\"))");
        assertEquals("Should have been an enclosingMethod pointcut", EnclosingMethodPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getFirstArgument().getClass());
        pc = (IPointcut) pc.getFirstArgument();
        assertEquals("Should have 2 arguments", 2, pc.getArgumentValues().length);
        
        assertEquals(FindPropertyPointcut.class, pc.getArgumentValues()[0].getClass());
        assertEquals(PrivatePointcut.class, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass());
        assertEquals("Expecting no arguments", 0, ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getArgumentValues().length);
        
        assertEquals(NamePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("yes", ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0]);
    }
    
    public void testEnclosing4() throws Exception {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut("enclosingScript(properties(isFinal()) & name(\"yes\"))");
        assertEquals("Should have been an enclosingScript pointcut", EnclosingScriptPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        
        assertEquals("Should have been an and pointcut", AndPointcut.class, pc.getFirstArgument().getClass());
        pc = (IPointcut) pc.getFirstArgument();
        assertEquals("Should have 2 arguments", 2, pc.getArgumentValues().length);
        
        assertEquals(FindPropertyPointcut.class, pc.getArgumentValues()[0].getClass());
        assertEquals(FinalPointcut.class, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass());
        assertEquals("Expecting no arguments", 0, ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getArgumentValues().length);
        
        assertEquals(NamePointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("yes", ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0]);
    }
    
    public void testBindAndFileExtension() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("bind(b: fileExtension(\"fdafdsfds\") )");
        assertEquals(BindPointcut.class, pc.getClass());
        assertEquals(FileExtensionPointcut.class, pc.getFirstArgument().getClass());
        assertEquals("b", pc.getFirstArgumentName());
        assertValidPointcut(pc);
        assertEquals("fdafdsfds", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
    }
    
    public void testBindAndNature() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("bind(b: nature(\"fdafdsfds\") )");
        assertEquals(BindPointcut.class, pc.getClass());
        assertEquals(ProjectNaturePointcut.class, pc.getFirstArgument().getClass());
        assertEquals("b", pc.getFirstArgumentName());
        assertValidPointcut(pc);
        assertEquals("fdafdsfds", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
    }
    
    public void testBindAndFileExtensionInvalid() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("bind(fileExtension(\"fdafdsfds\") )");
        assertEquals(BindPointcut.class, pc.getClass());
        assertEquals(FileExtensionPointcut.class, pc.getFirstArgument().getClass());
        assertInvalidPointcut("bind requires a named argument", pc);
        assertEquals("fdafdsfds", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
        
    }
    
    
    public void testVariable1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("def x = fileExtension(\"fdafdsfds\")\nbind(b:x)");
        assertEquals(BindPointcut.class, pc.getClass());
        assertEquals("b", pc.getFirstArgumentName());
        assertEquals(FileExtensionPointcut.class, pc.getFirstArgument().getClass());
        assertValidPointcut(pc);
        assertEquals("fdafdsfds", ((IPointcut) pc.getFirstArgument()).getFirstArgument());
    }
    
    public void testVariable2() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut("def x = fileExtension(\"fdafdsfds\")\nx & x");
        assertEquals(AndPointcut.class, pc.getClass());
        assertValidPointcut(pc);
        assertEquals(FileExtensionPointcut.class, pc.getArgumentValues()[0].getClass());
        assertEquals(FileExtensionPointcut.class, pc.getArgumentValues()[1].getClass());
        assertEquals("fdafdsfds", ((IPointcut) pc.getArgumentValues()[0]).getFirstArgument());
        assertEquals("fdafdsfds", ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument());
    }
    
    public void testCustomPointcut1() throws Exception {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
        		"registerPointcut('mine', { pattern -> null })\n" +
        		"mine()");
        assertEquals("org.codehaus.groovy.eclipse.dsl.pointcuts.impl.UserExtensiblePointcut", pc.getClass().getName());
    }
    /**
     * @param pc
     * @throws PointcutVerificationException
     */
    protected void assertValidPointcut(IPointcut pc)
            throws PointcutVerificationException {
        try {
            pc.verify();
        } catch (PointcutVerificationException e) {
            fail("Unexpected invalid pointcut: " + e.getPointcutMessage());
        }
    }
    
    protected void assertInvalidPointcut(String expectedMessage, IPointcut pc) {
        try {
            pc.verify();
            fail("Expected invalid pointcut, but wasn't\n" +  pc);
        } catch (PointcutVerificationException e) {
            if (! e.getMessage().equals(expectedMessage)) {
                fail("Unexpected message on invalid pointcut:\nExpected: " + expectedMessage + "\nBut was:\n"
                        + e.getPointcutMessage());
            }
        }
    }
}
