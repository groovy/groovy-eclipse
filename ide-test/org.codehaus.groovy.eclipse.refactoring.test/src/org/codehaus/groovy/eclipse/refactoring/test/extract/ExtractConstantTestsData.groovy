/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.extract

class ExtractConstantTestsData {

	static int findLocation(toFind, test) {
		String contents = ExtractConstantTestsData."${test}In" as String
		contents.indexOf(toFind)
	}
	
static String test1In = """
package p;
class A{
	static Foo
	static Bar
	int f = Foo + Bar;
}
"""
static String test1Out = """
package p;
class A{
	static Foo
	static Bar

	static final FOO_BAR = Foo + Bar
	int f = FOO_BAR;
}
"""

static String test2In = """
package p;
class A{
	static Foo
	static Bar
	int f = Foo + Bar;
	int g = Foo + // some useless crap
	
	
	Bar;
}
"""
static String test2Out = """
package p;
class A{
	static Foo
	static Bar

	static final FOO_BAR = Foo + Bar
	int f = FOO_BAR;
	int g = FOO_BAR;
}
"""

static String test3In = """
package p;
class A{
	static Foo
	static Bar
	static frax() { }
	int f() {
		Foo+Bar+A.frax()
	}
}
"""
static String test3Out = """
package p;
class A{
	static Foo
	static Bar

	static final FOO_BAR_FRAX = Foo+Bar+A.frax()
	static frax() { }
	int f() {
		FOO_BAR_FRAX
	}
}
"""

static String test4In = """
package p;
class A{
	class B {
		static Foo
		static Bar
		static frax() { }
		int f() {
			Foo+Bar+A.frax()+ 7
			7 + Foo+Bar+A.frax()+ 7
			7 + 7 + Foo+Bar+A.frax()+ 7 + 7
		}
	}
}
"""
static String test4Out = """
package p;
class A{
	class B {
		static Foo
		static Bar

		static final FOO_BAR_FRAX = Foo+Bar+A.frax()
		static frax() { }
		int f() {
			FOO_BAR_FRAX+ 7
			7 + FOO_BAR_FRAX+ 7
			7 + 7 + FOO_BAR_FRAX+ 7 + 7
		}
	}
}
"""

static String test5aIn = """
package p;
class A{
	static Foo
	static Bar
	static frax() { }
	int f() {
		Foo+Bar+A.frax()+ 7
		7 + Foo+Bar+A.frax()+ 7
		7 + 7 + Foo+Bar+A.frax()+ 7 + 7
	}
}
"""
static String test5aOut = """
package p;
class A{
	static Foo
	static Bar

	static final FOO_BAR_FRAX = Foo+Bar+A.frax()
	static frax() { }
	int f() {
		FOO_BAR_FRAX+ 7
		7 + FOO_BAR_FRAX+ 7
		7 + 7 + FOO_BAR_FRAX+ 7 + 7
	}
}
"""

static String test6aIn = """
package p;
class A{
	class B {
		static Foo
		static Bar
		static frax() { }
		int f() {
			Foo+Bar+A.frax()+ 7
			7 + Foo+Bar+A.frax()+ 7
			7 + 7 + Foo+Bar+A.frax()+ 7 + 7
		}
	}
}
"""
static String test6aOut = """
package p;
class A{
	class B {
		static Foo
		static Bar

		static final FOO_BAR_FRAX = Foo+Bar+A.frax()
		static frax() { }
		int f() {
			FOO_BAR_FRAX+ 7
			7 + FOO_BAR_FRAX+ 7
			7 + 7 + FOO_BAR_FRAX+ 7 + 7
		}
	}
}
"""

static String test7In = """
package p;
class A {
	static foo() {
		def Foo = 2
		def Bar = 3
		Foo + Bar
	}
}
"""

static String test8In = """
package p;
class A {
	static Foo
	static Bar
	static final FOO_BAR = 'Something'
	static foo() {
		Foo + Bar
	}
}
"""
static String test8Out = """
package p;
class A {
	static Foo
	static Bar
	static final FOO_BAR = 'Something'

	static final FOO_BAR2 = Foo + Bar
	static foo() {
		FOO_BAR2
	}
}
"""

static String testNoReplaceOccurrences1In = """
package p;
class A{
	static Foo
	static Bar
	static frax() { }
	int f() {
		Foo+Bar+A.frax()
	}
	int g() {
		def x = Foo+Bar+A.frax() + 7
	}
}
"""
static String testNoReplaceOccurrences1Out = """
package p;
class A{
	static Foo
	static Bar

	static final FOO_BAR_FRAX = Foo+Bar+A.frax()
	static frax() { }
	int f() {
		FOO_BAR_FRAX
	}
	int g() {
		def x = Foo+Bar+A.frax() + 7
	}
}
"""

static String testQualifiedReplace1In = """
package p;
class A{
	static Foo
	static Bar
	static frax() { }
	int f() {
		Foo+Bar+A.frax()
	}
	int g() {
		def x = Foo+Bar+A.frax() + 7
	}
}
"""
static String testQualifiedReplace1Out = """
package p;
class A{
	static Foo
	static Bar

	static final FOO_BAR_FRAX = Foo+Bar+A.frax()
	static frax() { }
	int f() {
		A.FOO_BAR_FRAX
	}
	int g() {
		def x = A.FOO_BAR_FRAX+ 7
	}
}
"""

}
