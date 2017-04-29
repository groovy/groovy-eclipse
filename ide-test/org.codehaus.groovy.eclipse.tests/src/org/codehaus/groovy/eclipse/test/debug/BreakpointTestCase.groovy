/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.debug

import org.codehaus.groovy.eclipse.test.EclipseTestSetup
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName

abstract class BreakpointTestCase {

    protected static GroovyCompilationUnit unit

    @BeforeClass
    static final void setUpTestSuite() {
        new EclipseTestSetup(null).setUp()
        unit = EclipseTestSetup.addGroovySource('''\
            |
            |def t = [ x:1, y:2 ] // 1
            |
            |def shiftTriangle = { it ->
            |    it.x += 1 // 2
            |    it.y += 1 // 3
            |
            |    it.getX()
            |}
            |
            |t.getX() // 4
            |
            |println "Triangle is at $t.centerLocation"
            |shiftTriangle(t)
            |println "Triangle is at $t.centerLocation"
            |
            |println "Triangle is at $t.centerLocation"
            |
            |t = ""
            |
            |def x() { // 12
            |    print "Hi"  // 5
            |}
            |
            |def xx() {
            |    print "Hi"  // 16
            |}
            |
            |def p = { g -> print g } // 13
            |
            |t = [ x: 1,
            |      y: 2, // 6
            |      z:4 ] // 7
            |t = [ 1, // 8
            |      2, // 9
            |      3] // 10
            |t = []; // 11
            |
            |
            |class Class {
            |    def m() {  // 22
            |        here()
            |        here() // 14
            |        here()
            |        here()
            |    }
            |
            |    def t = { here() } // 15
            |
            |    static h = {
            |        here() // 17
            |    }
            |}
            |
            |public class Printing {
            |    Printing() {  // 21
            |        print 8  // 18
            |    }
            |
            |    static y() {  // 23
            |    }
            |    def x = {
            |        print 9  // 19
            |    }
            |    static z = {
            |        print 9  // 20
            |    }
            |}'''.stripMargin(), 'BreakpointTesting')
        unit.makeConsistent(null)
    }

    @AfterClass
    static final void tearDownTestSuite() {
        new EclipseTestSetup(null).tearDown()
    }

    @Rule
    public TestName test = new TestName()

    @Before
    final void setUpTestCase() {
        println '----------------------------------------'
        println 'Starting: ' + test.getMethodName()
    }
}
