/*
 * Copyright 2009-2019 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.locations;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public final class ASTNodeSourceLocationsTests {

    @Parameters(name = "{index}. {0}")
    public static Object[][] params() {
        return new Object[][] {
            {"  x  ", "x", VariableExpression.class},
            {" (x) ", isParrotParser() ? "(x)" : "x", VariableExpression.class},

            {"  !x  ", "!x", NotExpression.class},
            {" (!x) ", isParrotParser() ? "(!x)" : "(!x) ", NotExpression.class},

            {"  ++x  ", "++x", PrefixExpression.class},
            {" (++x) ", isParrotParser() ? "(++x)" : "(++x) ", PrefixExpression.class},

            {"  --x  ", "--x", PrefixExpression.class},
            {" (--x) ", isParrotParser() ? "(--x)" : "(--x) ", PrefixExpression.class},

            {"  x++  ", "x++", PostfixExpression.class},
            {" (x++) ", isParrotParser() ? "(x++)" : "(x++) ", PostfixExpression.class},

            {"  x--  ", "x--", PostfixExpression.class},
            {" (x--) ", isParrotParser() ? "(x--)" : "(x--) ", PostfixExpression.class},

            {"  +x  ", "+x", UnaryPlusExpression.class},
            {" (+x) ", isParrotParser() ? "(+x)" : "(+x) ", UnaryPlusExpression.class},

            {"  -x  ", "-x", UnaryMinusExpression.class},
            {" (-x) ", isParrotParser() ? "(-x)" : "(-x) ", UnaryMinusExpression.class},

            {"  ~x  ", "~x", BitwiseNegationExpression.class},
            {" (~x) ", isParrotParser() ? "(~x)" : "(~x) ", BitwiseNegationExpression.class},

            {"def n =  1;   ",  "1",    ConstantExpression.class},
            {"def n = -1;   ", "-1",    ConstantExpression.class},
            {"def n = +1;   ", "+1",    ConstantExpression.class},
            {"def n =  1.2; ",  "1.2",  ConstantExpression.class},
            {"def n = -1.2; ", "-1.2",  ConstantExpression.class},
            {"def n = +1.2; ", "+1.2",  ConstantExpression.class},
            {"def n =  1.2d;",  "1.2d", ConstantExpression.class},
            {"def n = -1.2d;", "-1.2d", ConstantExpression.class},
            {"def n = +1.2d;", "+1.2d", ConstantExpression.class},
            {"def n = (1)   ", isParrotParser() ? "(1)" : "1", ConstantExpression.class},

            {"def map = [:]\n", "def map = [:]", BinaryExpression.class},
            {"def map;\n" + "map = [:]\n", "map = [:]", BinaryExpression.class},
            {"def foo = [1, 2] as Set\n", "def foo = [1, 2] as Set", BinaryExpression.class},
            {"def foo;\n" + "foo == [1, 2] as Set\n", "foo == [1, 2] as Set", BinaryExpression.class},
            {"def foo;\n" + "(foo == [1, 2] as Set)\n", "(foo == [1, 2] as Set)", BinaryExpression.class},
            {"def foo;\n" + "((foo == [1, 2] as Set))\n", "((foo == [1, 2] as Set))", BinaryExpression.class},
            {"[:] + [:] +  [:]\n", "[:] + [:]", BinaryExpression.class},
            {"[:] + [:] +  [:]\n", "[:] + [:] +  [:]", BinaryExpression.class},
            {"[:] << [:] + [:]\n", "[:] + [:]", BinaryExpression.class},
            {"[:] << [:] + [:]\n", "[:] << [:] + [:]", BinaryExpression.class},
            {"def x =   a == b   ", "a == b", BinaryExpression.class},
            {"def x = ( a == b )", "( a == b )", BinaryExpression.class},
            {"def x =  ( a == b )  ", isParrotParser() ? "( a == b )" : "( a == b )  ", BinaryExpression.class},
            {"def x = ( a ) == b\n", "( a ) == b", BinaryExpression.class},
            {"def x = c == ( d )\n", "c == ( d )", BinaryExpression.class},
            {"def x = a[b]\n", "a[b]", BinaryExpression.class},
            /*{"def x = (a)[b]\n", "(a)[b]", BinaryExpression.class}, parsed as cast of list*/
            {"def x = a[(b)]\n", "a[(b)]", BinaryExpression.class},
            {"def x = (a[b])\n", "(a[b])", BinaryExpression.class},
            {"Map m\n m['k']\n", "m['k']", BinaryExpression.class},
            {"def x = ( a ) in b\n", "( a ) in b", BinaryExpression.class},
            {"def x = a in ( b )\n", "a in ( b )", BinaryExpression.class},
            {"def x = ( a in b )\n", "( a in b )", BinaryExpression.class},
            {"def x = ( ( a ) in b )\n", "( ( a ) in b )", BinaryExpression.class},
            {"def x = ( a in ( b ) )\n", "( a in ( b ) )", BinaryExpression.class},
            {"def x = ( a ) instanceof b\n", "( a ) instanceof b", BinaryExpression.class},
            {"def x = ( a instanceof b )\n", "( a instanceof b )", BinaryExpression.class},
            {"def x = ( ( a ) instanceof b )\n", "( ( a ) instanceof b )", BinaryExpression.class},

            {"def x = a ? b : c\n", "a ? b : c", TernaryExpression.class},
            {"def x = (a) ? b : c\n", "(a) ? b : c", TernaryExpression.class},
            {"def x = a ? (b) : c\n", "a ? (b) : c", TernaryExpression.class},
            {"def x = a ? b : (c)\n", "a ? b : (c)", TernaryExpression.class},
            {"def x = (a ? b : c)\n", "(a ? b : c)", TernaryExpression.class},
            {"def x = ((a) ? b : c)\n", "((a) ? b : c)", TernaryExpression.class},
            {"def x = (a ? (b) : c)\n", "(a ? (b) : c)", TernaryExpression.class},
            {"def x = (a ? b : (c))\n", "(a ? b : (c))", TernaryExpression.class},
            {"def x = (1) ? 2 : (3)\n", "(1) ? 2 : (3)", TernaryExpression.class},
            {"def x = (1) ?: (3)\n", "(1) ?: (3)", TernaryExpression.class},

            {"def r = 1..2\n", "1..2", RangeExpression.class},
            {"def r = (1)..2\n", "(1)..2", RangeExpression.class},
            {"def r = 1..(2)\n", "1..(2)", RangeExpression.class},
            {"def r = (1..2)\n", "(1..2)", RangeExpression.class},
            {"def r = a..b\n", "a..b", RangeExpression.class},
            {"def r = (a)..b\n", "(a)..b", RangeExpression.class},
            {"def r = a..(b)\n", "a..(b)", RangeExpression.class},
            {"def r = (a..b)\n", "(a..b)", RangeExpression.class},

            {"(Set) foo", "(Set) foo", CastExpression.class},
            {"def x = ( Set ) foo ", "( Set ) foo", CastExpression.class},
            {"foo as Set", "foo as Set", CastExpression.class},
            {"def x = foo as Set ", "foo as Set", CastExpression.class},
            {"def x = foo as Set\r\n", "foo as Set", CastExpression.class},

            {"[]", "[]", ListExpression.class},
            {"[  ]", "[  ]", ListExpression.class},
            {"def list = []", "[]", ListExpression.class},
            {"def list = [  ]", "[  ]", ListExpression.class},
            {"def list = [ 1, 2,\n'3' ]", "[ 1, 2,\n'3' ]", ListExpression.class},

            // GRECLIPSE-768 -- empty maps
            {"[:]", "[:]", MapExpression.class},
            {"[ : ]", "[ : ]", MapExpression.class},
            {"def map = [:]", "[:]", MapExpression.class},
            {"def map = [ : ]", "[ : ]", MapExpression.class},

            {"[a:b]", "a:b", MapEntryExpression.class},
            {"[a : b]", "a : b", MapEntryExpression.class},
            {"[a : b  ]", "a : b", MapEntryExpression.class},
            {"[a : b, c : d]", "a : b", MapEntryExpression.class},
            {"[a : b, c : d]", "c : d", MapEntryExpression.class},
            {"def m = [a : b, c : d]", "a : b", MapEntryExpression.class},
            {"def m = [a : b, c : d]", "c : d", MapEntryExpression.class},
            {"def m = [a : b] << [ c : d]", "a : b", MapEntryExpression.class},
            {"def m = [a : b] << [ c : d]", "c : d", MapEntryExpression.class},
            {"def m = [a : b, e : [ c : d]]", "a : b", MapEntryExpression.class},
            {"def m = [a : b, e : [ c : d]]", "c : d", MapEntryExpression.class},
            {"def m = [a : b, e : [ c : d]]", "e : [ c : d]", MapEntryExpression.class},

            {"def meth() {\n  then:\n  assert x == 9\n}", "assert x == 9", AssertStatement.class}, // GRECLIPSE-1270

            // TODO: package, imports (incl. aliases), annotations, anon. inners,
        };
    }

    //--------------------------------------------------------------------------

    @Parameter(0)
    public String source;

    @Parameter(1)
    public String target;

    @Parameter(2)
    public Class<? extends ASTNode> targetType;

    @Test
    public void testSourceLocations() throws Exception {
        CompilerConfiguration config = new CompilerConfiguration();
        SourceUnit sourceUnit = new SourceUnit("TestUnit", source, config, new GroovyClassLoader(), new ErrorCollector(config));
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();

        final int offset = source.indexOf(target), length = target.length();
        Assume.assumeTrue(offset >= 0);

        try {
            new DepthFirstVisitor() {
                @Override
                protected void visitExpression(Expression expression) {
                    if (expression.getEnd() > 0 && targetType.isInstance(expression)) {
                        if (expression.getStart() == offset && expression.getLength() == length) {
                            throw new VisitCompleteException("found expected expression");
                        }
                    }
                    super.visitExpression(expression);
                }

                @Override
                protected void visitStatement(Statement statement) {
                    if (statement.getEnd() > 0 && targetType.isInstance(statement)) {
                        if (statement.getStart() == offset && statement.getLength() == length) {
                            throw new VisitCompleteException("found expected statement");
                        }
                    }
                    super.visitStatement(statement);
                }
            }.visitModule(sourceUnit.getAST());

            Assert.fail("Expected to find " + targetType.getSimpleName() + " at " + offset + ".." + (offset + length));
        } catch (VisitCompleteException complete) {
            // success
        }
    }
}
