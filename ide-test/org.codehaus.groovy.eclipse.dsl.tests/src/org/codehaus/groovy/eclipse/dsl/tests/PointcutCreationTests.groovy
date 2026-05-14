/*
 * Copyright 2009-2020 the original author or authors.
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
        assertEquals('java.lang.String', pc.firstArgument)
    }

    @Test
    void testPointcutCreation2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('currentType(fields(annotatedBy("java.lang.String")))')
        assertTrue('Should have been a currentType pointcut', pc instanceof CurrentTypePointcut)
        assertValidPointcut(pc)

        Object firstArgument = pc.firstArgument
        assertTrue(firstArgument instanceof FindFieldPointcut)
        pc = (IPointcut) firstArgument

        firstArgument = pc.firstArgument
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
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") & currentType("java.lang.String")')
        assertEquals('Should have been an and pointcut', AndPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)

        assertEquals('Should have 2 argument', 2, pc.argumentValues.length)
    }

    @Test
    void testAnd2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") & currentType("java.lang.String") & currentType("java.lang.String")')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', AndPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[2].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[2]).firstArgument)

        assertEquals('Should have 3 argument', 3, pc.argumentValues.length)
    }

    @Test
    void testAnd3() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            '(currentType("java.lang.String") & currentType("java.lang.String")) & currentType("java.lang.String")')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', AndPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[2].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[2]).firstArgument)

        assertEquals('Should have 3 argument', 3, pc.argumentValues.length)
    }

    @Test
    void testAnd4() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") & (currentType("java.lang.String") & currentType("java.lang.String"))')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', AndPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[2].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[2]).firstArgument)

        assertEquals('Should have 3 argument', 3, pc.argumentValues.length)
    }

    @Test
    void testValidAnd1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") & currentType("java.lang.String") & currentType()')
        assertEquals('Should have been an and pointcut', AndPointcut, pc.class)
        assertValidPointcut(pc)
    }

    @Test
    void testInvalidAnd1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") & currentType("java.lang.String") & fileExtension()')
        assertEquals('Should have been an and pointcut', AndPointcut, pc.class)
        assertInvalidPointcut('Expecting 1 argument, but found 0.  Consider using "&" or "|" to connect arguments.', pc)
    }

    @Test
    void testOr1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") | currentType("java.lang.String")')
        assertEquals('Should have been an and pointcut', OrPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)

        assertEquals('Should have 2 argument', 2, pc.argumentValues.length)
    }

    @Test
    void testOr2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") | currentType("java.lang.String") | currentType("java.lang.String")')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', OrPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[2].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[2]).firstArgument)

        assertEquals('Should have 3 argument', 3, pc.argumentValues.length)
    }

    @Test
    void testOr3() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            '(currentType("java.lang.String") | currentType("java.lang.String")) | currentType("java.lang.String")')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', OrPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[2].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[2]).firstArgument)

        assertEquals('Should have 3 argument', 3, pc.argumentValues.length)
    }

    @Test
    void testOr4() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") | (currentType("java.lang.String") | currentType("java.lang.String"))')
        pc = pc.normalize()
        assertEquals('Should have been an and pointcut', OrPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[2].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[2]).firstArgument)

        assertEquals('Should have 3 argument', 3, pc.argumentValues.length)
    }

    @Test
    void testOrAnd1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") | currentType("java.lang.String") & currentType("java.lang.String")')
        assertEquals('Should have been an or pointcut', OrPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals(CurrentTypePointcut, pc.firstArgument.class)
        assertEquals('java.lang.String', ((IPointcut) pc.firstArgument).firstArgument)

        assertEquals('Should have 2 arguments', 2, pc.argumentValues.length)
        assertEquals(AndPointcut, pc.argumentValues[1].class)

        assertEquals(CurrentTypePointcut, ((IPointcut) pc.argumentValues[1]).argumentValues[0].class)
        assertEquals(CurrentTypePointcut, ((IPointcut) pc.argumentValues[1]).argumentValues[1].class)

        assertEquals('java.lang.String', ((IPointcut) ((IPointcut) pc.argumentValues[1]).argumentValues[0]).firstArgument)
        assertEquals('java.lang.String', ((IPointcut) ((IPointcut) pc.argumentValues[1]).argumentValues[1]).firstArgument)
    }

    @Test
    void testAndOr1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(
            'currentType("java.lang.String") & currentType("java.lang.String") | currentType("java.lang.String")')
        assertEquals('Should have been an or pointcut', OrPointcut, pc.class)
        assertValidPointcut(pc)
        assertEquals('Should have 2 arguments', 2, pc.argumentValues.length)

        assertEquals(AndPointcut, pc.argumentValues[0].class)

        assertEquals(CurrentTypePointcut, ((IPointcut) pc.argumentValues[0]).argumentValues[0].class)
        assertEquals(CurrentTypePointcut, ((IPointcut) pc.argumentValues[0]).argumentValues[1].class)

        assertEquals('java.lang.String', ((IPointcut) ((IPointcut) pc.argumentValues[0]).argumentValues[0]).firstArgument)
        assertEquals('java.lang.String', ((IPointcut) ((IPointcut) pc.argumentValues[0]).argumentValues[1]).firstArgument)

        assertEquals(CurrentTypePointcut, pc.argumentValues[1].class)
        assertEquals('java.lang.String', ((IPointcut) pc.argumentValues[1]).firstArgument)
    }

    @Test
    void testEnclosing1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('enclosingClass(properties(isStatic()) & name("yes"))')
        assertEquals('Should have been an enclosingClass pointcut', EnclosingClassPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals('Should have been an and pointcut', AndPointcut, pc.firstArgument.class)
        pc = (IPointcut) pc.firstArgument
        assertEquals('Should have 2 arguments', 2, pc.argumentValues.length)

        assertEquals(FindPropertyPointcut, pc.argumentValues[0].class)
        assertEquals(StaticPointcut, ((IPointcut) pc.argumentValues[0]).argumentValues[0].class)
        assertEquals('Expecting no arguments', 0, ((IPointcut) ((IPointcut) pc.argumentValues[0]).argumentValues[0]).argumentValues.length)

        assertEquals(NamePointcut, pc.argumentValues[1].class)
        assertEquals('yes', ((IPointcut) pc.argumentValues[1]).argumentValues[0])
    }

    @Test
    void testEnclosing2() {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut('enclosingField(properties(isPublic()) & name("yes"))')
        assertEquals('Should have been an enclosingField pointcut', EnclosingFieldPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals('Should have been an and pointcut', AndPointcut, pc.firstArgument.class)
        pc = (IPointcut) pc.firstArgument
        assertEquals('Should have 2 arguments', 2, pc.argumentValues.length)

        assertEquals(FindPropertyPointcut, pc.argumentValues[0].class)
        assertEquals(PublicPointcut, ((IPointcut) pc.argumentValues[0]).argumentValues[0].class)
        assertEquals('Expecting no arguments', 0, ((IPointcut) ((IPointcut) pc.argumentValues[0]).argumentValues[0]).argumentValues.length)

        assertEquals(NamePointcut, pc.argumentValues[1].class)
        assertEquals('yes', ((IPointcut) pc.argumentValues[1]).argumentValues[0])
    }

    @Test
    void testEnclosing3() {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut('enclosingMethod(properties(isPrivate()) & name("yes"))')
        assertEquals('Should have been an enclosingMethod pointcut', EnclosingMethodPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals('Should have been an and pointcut', AndPointcut, pc.firstArgument.class)
        pc = (IPointcut) pc.firstArgument
        assertEquals('Should have 2 arguments', 2, pc.argumentValues.length)

        assertEquals(FindPropertyPointcut, pc.argumentValues[0].class)
        assertEquals(PrivatePointcut, ((IPointcut) pc.argumentValues[0]).argumentValues[0].class)
        assertEquals('Expecting no arguments', 0, ((IPointcut) ((IPointcut) pc.argumentValues[0]).argumentValues[0]).argumentValues.length)

        assertEquals(NamePointcut, pc.argumentValues[1].class)
        assertEquals('yes', ((IPointcut) pc.argumentValues[1]).argumentValues[0])
    }

    @Test
    void testEnclosing4() {
        // a meaningless pointcut
        IPointcut pc = new PointcutScriptExecutor().createPointcut('enclosingScript(properties(isFinal()) & name("yes"))')
        assertEquals('Should have been an enclosingScript pointcut', EnclosingScriptPointcut, pc.class)
        assertValidPointcut(pc)

        assertEquals('Should have been an and pointcut', AndPointcut, pc.firstArgument.class)
        pc = (IPointcut) pc.firstArgument
        assertEquals('Should have 2 arguments', 2, pc.argumentValues.length)

        assertEquals(FindPropertyPointcut, pc.argumentValues[0].class)
        assertEquals(FinalPointcut, ((IPointcut) pc.argumentValues[0]).argumentValues[0].class)
        assertEquals('Expecting no arguments', 0, ((IPointcut) ((IPointcut) pc.argumentValues[0]).argumentValues[0]).argumentValues.length)

        assertEquals(NamePointcut, pc.argumentValues[1].class)
        assertEquals('yes', ((IPointcut) pc.argumentValues[1]).argumentValues[0])
    }

    @Test
    void testBindAndFileExtension() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('bind(b: fileExtension("fdafdsfds") )')
        assertEquals(BindPointcut, pc.class)
        assertEquals(FileExtensionPointcut, pc.firstArgument.class)
        assertEquals('b', pc.firstArgumentName)
        assertValidPointcut(pc)
        assertEquals('fdafdsfds', ((IPointcut) pc.firstArgument).firstArgument)
    }

    @Test
    void testBindAndNature() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('bind(b: nature("fdafdsfds") )')
        assertEquals(BindPointcut, pc.class)
        assertEquals(ProjectNaturePointcut, pc.firstArgument.class)
        assertEquals('b', pc.firstArgumentName)
        assertValidPointcut(pc)
        assertEquals('fdafdsfds', ((IPointcut) pc.firstArgument).firstArgument)
    }

    @Test
    void testBindAndFileExtensionInvalid() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('bind(fileExtension("fdafdsfds") )')
        assertEquals(BindPointcut, pc.class)
        assertEquals(FileExtensionPointcut, pc.firstArgument.class)
        assertInvalidPointcut('bind requires a named argument', pc)
        assertEquals('fdafdsfds', ((IPointcut) pc.firstArgument).firstArgument)
    }

    @Test
    void testVariable1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('def x = fileExtension("fdafdsfds")\nbind(b:x)')
        assertEquals(BindPointcut, pc.class)
        assertEquals('b', pc.firstArgumentName)
        assertEquals(FileExtensionPointcut, pc.firstArgument.class)
        assertValidPointcut(pc)
        assertEquals('fdafdsfds', ((IPointcut) pc.firstArgument).firstArgument)
    }

    @Test
    void testVariable2() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('def x = fileExtension("fdafdsfds")\nx & x')
        assertEquals(AndPointcut, pc.class)
        assertValidPointcut(pc)
        assertEquals(FileExtensionPointcut, pc.argumentValues[0].class)
        assertEquals(FileExtensionPointcut, pc.argumentValues[1].class)
        assertEquals('fdafdsfds', ((IPointcut) pc.argumentValues[0]).firstArgument)
        assertEquals('fdafdsfds', ((IPointcut) pc.argumentValues[1]).firstArgument)
    }

    @Test
    void testCustomPointcut1() {
        IPointcut pc = new PointcutScriptExecutor().createPointcut('registerPointcut("mine", { pattern -> null })\nmine()')
        assertEquals('org.codehaus.groovy.eclipse.dsl.pointcuts.impl.UserExtensiblePointcut', pc.class.name)
    }
}
