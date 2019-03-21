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

/**
 * 
 * @author andrew
 * @created Jun 9, 2010
 */
class ExtractLocalTestsData {
    
    static int findLocation(toFind, test) {
        String contents = ExtractLocalTestsData."${test}In" as String
        contents.indexOf(toFind)
    }
    
    static String test1In =
    """
package p

def foo
def bar
foo(foo+bar)

foo + bar + foo(foo+bar, foo+ bar + baz) + foo + bar   
foo + bar

def x() {
	foo + bar
}
def x = {
	foo + bar
}
"""
    
    static String test1Out =
    """
package p

def foo
def bar
def fooBar = foo + bar
foo(fooBar)

fooBar + foo(fooBar, fooBar + baz) + fooBar   
fooBar

def x() {
	foo + bar
}
def x = {
	fooBar
}
""" 
    
    static String test2In = """
package p

foo.bar.foo.bar(foo.bar.foo.bar)
"""
    static String test2Out = """
package p

def fooBar = foo.bar
fooBar.foo.bar(fooBar.foo.bar)
"""
    
    static String test3In = """
package p

baz.foo.&bar
"""
    static String test3Out = """
package p

def bazFooBar = baz.foo.&bar
bazFooBar
""" 
    static String test4In = """
package p

first + 1
first+1
"""
    static String test4Out = """
package p

def first1 = first + 1
first1
first+1
""" 
    
    static String test5In = """
package p

foo + bar
foo + // fdsafhds
	bar
"""
    static String test5Out = """
package p

def fooBar = foo + bar
fooBar
fooBar
""" 
    
    static String test6In = """
package p

class Outer {
	def x() {
		foo + bar
	}
}
""" 
    static String test6Out = """
package p

class Outer {
	def x() {
		def fooBar = foo + bar
		fooBar
	}
}
""" 
    
    static String test7In = """
package p

class Outer {
	class Inner {
		def x() {
			foo + bar
		}
	}
}
""" 
    static String test7Out = """
package p

class Outer {
	class Inner {
		def x() {
			def fooBar = foo + bar
			fooBar
		}
	}
}
"""
    
    static String test8In = """
package p

foo + bar
if (foo+bar) {
	while (foo+bar) {
		foo+  bar
	}
	foo+bar
}
"""
    static String test8Out = """
package p

def fooBar = foo+  bar
fooBar
if (fooBar) {
	while (fooBar) {
		fooBar
	}
	fooBar
}
"""
    static String test9In = """
class Simple {
     def test() {
          def map
          def foo = {
               println map.one
               println map.one
               println map.one
               println map.one
          }
     }
}
"""
    static String test9Out = """
class Simple {
     def test() {
          def map
          def foo = {
               def mapOne = map.one
               println mapOne
               println mapOne
               println mapOne
               println mapOne
          }
     }
}
"""    

static String test10In = """
class Simple {
     def test() {
          model.farInstance()  
     }
}
"""
static String test10Out = """
class Simple {
     def test() {
          def modelFarInstance = model.farInstance()
          modelFarInstance
     }
}
"""

static String test11In = """
class Simple {
     def test() {
          println "here"
     }
}
"""
static String test11Out = """
class Simple {
     def test() {
          def println = println "here"
          println
     }
}
"""

static String test12In = """
class Simple {
     def test() {
          println "here"  
     }
}
"""
static String test12Out = """
class Simple {
     def test() {
          def println = println "here"
          println
     }
}
"""

static String test13In = """
class Foo {
	int bar(int a, int b) {
		def aB
		a + b
	}
}
"""
static String test13Out = """
class Foo {
	int bar(int a, int b) {
		def aB
		def aB2 = a + b
		aB2
	}
}
"""

}