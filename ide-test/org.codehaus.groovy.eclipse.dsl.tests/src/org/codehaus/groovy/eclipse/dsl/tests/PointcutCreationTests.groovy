/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.dsl.tests

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.BindPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypePointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClassPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingFieldPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingMethodPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingScriptPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileExtensionPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindAnnotationPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindFieldPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindPropertyPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.NamePointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.OrPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.ProjectNaturePointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.FinalPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PrivatePointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PublicPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.StaticPointcut
import org.codehaus.groovy.eclipse.dsl.tests.internal.PointcutScriptExecutor
import org.junit.Test

final class PointcutCreationTests {

    private static void assertValidPointcut(IPointcut pc) {
        try {
            pc.verify()
        } catch (PointcutVerificationException e) {
            fail('Unexpected invalid pointcut: ' + e.pointcutMessage)
        }
    }

    private static void assertInvalidPointcut(String expectedMessage, IPointcut pc) {
        try {
            pc.verify()
            fail('Expected invalid pointcut\n' +  pc)
        } catch (PointcutVerificationException e) {
            if (!e.message == expectedMessage) {
                fail('Unexpected message on invalid pointcut:\nExpected: ' + expectedMessage + '\nBut was:\n' + e.pointcutMessage)
            }
        }
    }

    @Test
    void testPointcutCreation1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String")')
        assertTrue('Should have been a currentType pointcut', pc instanceof CurrentTypePointcut)
        assertValidPointcut(pc)
        assertEquals('java.lang.String', pc.getFirstArgument())
    }

    @Test
    void testPointcutCreation2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType(fields(annotatedBy("java.lang.String")))')
        assertTrue('Should have been a currentType pointcut', pc instanceof CurrentTypePointcut)
        assertValidPointcut(pc)

        Object firstArgument = pc.getFirstArgument()
        assertTrue(firstArgument instanceof FindFieldPointcut)
        pc = (IPointcut) firstArgument

        firstArgument = pc.getFirstArgument()
        assertTrue(firstArgument instanceof FindAnnotationPointcut)
        pc = (IPointcut) firstArgument
    }

    @Test
    void testValidPointcutCreation1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType()')
        assertTrue('Should have been a currentType pointcut', pc instanceof CurrentTypePointcut)
        assertValidPointcut(pc)
    }

    @Test
    void testInvalidPointcutCreation1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('fileExtension()')
        assertTrue('Should have been a currentType pointcut', pc instanceof FileExtensionPointcut)
        assertInvalidPointcut('Expecting 1 argument, but found 0.  Consider using "&" or "|" to connect arguments.', pc)
    }

    @Test
    void testValidPointcutCreation2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType(fields(annotatedBy()))')
        assertTrue('Should have been a currentType pointcut', pc instanceof CurrentTypePointcut)
        assertValidPointcut(pc)
    }

    @Test
    void testInvalidPointcutCreation2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType(fields(isStatic("foo")))')
        assertTrue('Should have been a currentType pointcut', pc instanceof CurrentTypePointcut)
        assertInvalidPointcut('This pointcut does not take any arguments.', pc)
    }

    @Test
    void testAnd1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") & currentType("java.lang.String")')
        assertEquals('Should have been an and pointcut', AndPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())

        assertEquals('Should have 2 argument', 2, pc.getArgumentValues().length)
    }

    @Test
    void testAnd2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") & currentType("java.lang.String") & currentType("java.lang.String")')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', AndPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[2].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument())

        assertEquals('Should have 3 argument', 3, pc.getArgumentValues().length)
    }

    @Test
    void testAnd3() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('(currentType("java.lang.String") & currentType("java.lang.String")) & currentType("java.lang.String")')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', AndPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[2].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument())

        assertEquals('Should have 3 argument', 3, pc.getArgumentValues().length)
    }

    @Test
    void testAnd4() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") & (currentType("java.lang.String") & currentType("java.lang.String"))')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', AndPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[2].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument())

        assertEquals('Should have 3 argument', 3, pc.getArgumentValues().length)
    }

    @Test
    void testValidAnd1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") & currentType("java.lang.String") & currentType()')
        assertEquals('Should have been an and pointcut', AndPointcut, pc.getClass())
        assertValidPointcut(pc)
    }

    @Test
    void testInvalidAnd1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") & currentType("java.lang.String") & fileExtension()')
        assertEquals('Should have been an and pointcut', AndPointcut, pc.getClass())
        assertInvalidPointcut('Expecting 1 argument, but found 0.  Consider using "&" or "|" to connect arguments.', pc)
    }

    @Test
    void testOr1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") | currentType("java.lang.String")')
        assertEquals('Should have been an and pointcut', OrPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())

        assertEquals('Should have 2 argument', 2, pc.getArgumentValues().length)
    }

    @Test
    void testOr2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") | currentType("java.lang.String") | currentType("java.lang.String")')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', OrPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[2].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument())

        assertEquals('Should have 3 argument', 3, pc.getArgumentValues().length)
    }

    @Test
    void testOr3() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('(currentType("java.lang.String") | currentType("java.lang.String")) | currentType("java.lang.String")')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', OrPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[2].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument())

        assertEquals('Should have 3 argument', 3, pc.getArgumentValues().length)
    }

    @Test
    void testOr4() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") | (currentType("java.lang.String") | currentType("java.lang.String"))')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', OrPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[2].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[2]).getFirstArgument())

        assertEquals('Should have 3 argument', 3, pc.getArgumentValues().length)
    }

    @Test
    void testOrAnd1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") | currentType("java.lang.String") & currentType("java.lang.String")')
        assertEquals('Should have been an or pointcut', OrPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.getFirstArgument().getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getFirstArgument()).getFirstArgument())

        assertEquals('Should have 2 arguments', 2, pc.getArgumentValues().length)


        assertEquals(AndPointcut, pc.getArgumentValues()[1].getClass())

        assertEquals(CurrentTypePointcut, ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0].getClass())
        assertEquals(CurrentTypePointcut, ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[1].getClass())

        assertEquals('java.lang.String', ((IPointcut) ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0]).getFirstArgument())
        assertEquals('java.lang.String', ((IPointcut) ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[1]).getFirstArgument())
    }

    @Test
    void testAndOr1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType("java.lang.String") & currentType("java.lang.String") | currentType("java.lang.String")')
        assertEquals('Should have been an or pointcut', OrPointcut, pc.getClass())
        assertValidPointcut(pc)
        assertEquals('Should have 2 arguments', 2, pc.getArgumentValues().length)

        assertEquals(AndPointcut, pc.getArgumentValues()[0].getClass())

        assertEquals(CurrentTypePointcut, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass())
        assertEquals(CurrentTypePointcut, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[1].getClass())

        assertEquals('java.lang.String', ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getFirstArgument())
        assertEquals('java.lang.String', ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[1]).getFirstArgument())

        assertEquals(CurrentTypePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('java.lang.String', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())
    }

    @Test
    void testEnclosing1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('enclosingClass(properties(isStatic()) & name("yes"))')
        assertEquals('Should have been an enclosingClass pointcut', EnclosingClassPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals('Should have been an and pointcut', AndPointcut, pc.getFirstArgument().getClass())
        pc = (IPointcut) pc.getFirstArgument()
        assertEquals('Should have 2 arguments', 2, pc.getArgumentValues().length)

        assertEquals(FindPropertyPointcut, pc.getArgumentValues()[0].getClass())
        assertEquals(StaticPointcut, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass())
        assertEquals('Expecting no arguments', 0, ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getArgumentValues().length)

        assertEquals(NamePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('yes', ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0])
    }

    @Test
    void testEnclosing2() {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut('enclosingField(properties(isPublic()) & name("yes"))')
        assertEquals('Should have been an enclosingField pointcut', EnclosingFieldPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals('Should have been an and pointcut', AndPointcut, pc.getFirstArgument().getClass())
        pc = (IPointcut) pc.getFirstArgument()
        assertEquals('Should have 2 arguments', 2, pc.getArgumentValues().length)

        assertEquals(FindPropertyPointcut, pc.getArgumentValues()[0].getClass())
        assertEquals(PublicPointcut, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass())
        assertEquals('Expecting no arguments', 0, ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getArgumentValues().length)

        assertEquals(NamePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('yes', ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0])
    }

    @Test
    void testEnclosing3() {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut('enclosingMethod(properties(isPrivate()) & name("yes"))')
        assertEquals('Should have been an enclosingMethod pointcut', EnclosingMethodPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals('Should have been an and pointcut', AndPointcut, pc.getFirstArgument().getClass())
        pc = (IPointcut) pc.getFirstArgument()
        assertEquals('Should have 2 arguments', 2, pc.getArgumentValues().length)

        assertEquals(FindPropertyPointcut, pc.getArgumentValues()[0].getClass())
        assertEquals(PrivatePointcut, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass())
        assertEquals('Expecting no arguments', 0, ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getArgumentValues().length)

        assertEquals(NamePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('yes', ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0])
    }

    @Test
    void testEnclosing4() {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut('enclosingScript(properties(isFinal()) & name("yes"))')
        assertEquals('Should have been an enclosingScript pointcut', EnclosingScriptPointcut, pc.getClass())
        assertValidPointcut(pc)

        assertEquals('Should have been an and pointcut', AndPointcut, pc.getFirstArgument().getClass())
        pc = (IPointcut) pc.getFirstArgument()
        assertEquals('Should have 2 arguments', 2, pc.getArgumentValues().length)

        assertEquals(FindPropertyPointcut, pc.getArgumentValues()[0].getClass())
        assertEquals(FinalPointcut, ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0].getClass())
        assertEquals('Expecting no arguments', 0, ((IPointcut) ((IPointcut) pc.getArgumentValues()[0]).getArgumentValues()[0]).getArgumentValues().length)

        assertEquals(NamePointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('yes', ((IPointcut) pc.getArgumentValues()[1]).getArgumentValues()[0])
    }

    @Test
    void testBindAndFileExtension() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('bind(b: fileExtension("fdafdsfds") )')
        assertEquals(BindPointcut, pc.getClass())
        assertEquals(FileExtensionPointcut, pc.getFirstArgument().getClass())
        assertEquals('b', pc.getFirstArgumentName())
        assertValidPointcut(pc)
        assertEquals('fdafdsfds', ((IPointcut) pc.getFirstArgument()).getFirstArgument())
    }

    @Test
    void testBindAndNature() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('bind(b: nature("fdafdsfds") )')
        assertEquals(BindPointcut, pc.getClass())
        assertEquals(ProjectNaturePointcut, pc.getFirstArgument().getClass())
        assertEquals('b', pc.getFirstArgumentName())
        assertValidPointcut(pc)
        assertEquals('fdafdsfds', ((IPointcut) pc.getFirstArgument()).getFirstArgument())
    }

    @Test
    void testBindAndFileExtensionInvalid() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('bind(fileExtension("fdafdsfds") )')
        assertEquals(BindPointcut, pc.getClass())
        assertEquals(FileExtensionPointcut, pc.getFirstArgument().getClass())
        assertInvalidPointcut('bind requires a named argument', pc)
        assertEquals('fdafdsfds', ((IPointcut) pc.getFirstArgument()).getFirstArgument())
    }

    @Test
    void testVariable1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('def x = fileExtension("fdafdsfds")\nbind(b:x)')
        assertEquals(BindPointcut, pc.getClass())
        assertEquals('b', pc.getFirstArgumentName())
        assertEquals(FileExtensionPointcut, pc.getFirstArgument().getClass())
        assertValidPointcut(pc)
        assertEquals('fdafdsfds', ((IPointcut) pc.getFirstArgument()).getFirstArgument())
    }

    @Test
    void testVariable2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('def x = fileExtension("fdafdsfds")\nx & x')
        assertEquals(AndPointcut, pc.getClass())
        assertValidPointcut(pc)
        assertEquals(FileExtensionPointcut, pc.getArgumentValues()[0].getClass())
        assertEquals(FileExtensionPointcut, pc.getArgumentValues()[1].getClass())
        assertEquals('fdafdsfds', ((IPointcut) pc.getArgumentValues()[0]).getFirstArgument())
        assertEquals('fdafdsfds', ((IPointcut) pc.getArgumentValues()[1]).getFirstArgument())
    }

    @Test
    void testCustomPointcut1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('registerPointcut("mine", { pattern -> null })\nmine()')
        assertEquals('org.codehaus.groovy.eclipse.dsl.pointcuts.impl.UserExtensiblePointcut', pc.getClass().getName())
    }
}
