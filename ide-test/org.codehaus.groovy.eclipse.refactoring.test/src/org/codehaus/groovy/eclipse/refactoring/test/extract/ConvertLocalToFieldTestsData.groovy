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

class ConvertLocalToFieldTestsData {

	static class TestCase {
		String input
		String expected
		String fieldName
		boolean expectWarning

		public TestCase(input, expected = null, fieldName = "target", expectWarning = false) {
			this.input = input
			this.expected = expected
			this.fieldName = fieldName
			this.expectWarning = expectWarning
		}

		int getSelectionOffset() { return input.indexOf("target/**/") }
		int getSelectionLength() { return "target".length() }
	}

	static Map<String, TestCase> testCases = [
		testMethodToModule:
		new TestCase('def test() { def target/**/ }', '@groovy.transform.Field def target\ndef test() { target/**/ }'),
		testClosureToModule:
		new TestCase('def test = { def target/**/ }', '@groovy.transform.Field def target\ndef test = { target/**/ }'),
		testDeclarationWithDef:
		new TestCase(
			"""
class Test {
	def test() {
		def target/**/
	}
}
			""",
			"""
class Test {
	private def target
	def test() {
		target/**/
	}
}
			"""
		),
		testDeclarationWithType:
		new TestCase(
			"""
class Test {
	def test() {
		String target/**/
	}
}
			""",
			"""
class Test {
	private String target
	def test() {
		target/**/
	}
}
			"""
		),
		testReference:
		new TestCase(
			"""
class Test {
	def test() {
		def target
		target/**/
	}
}
			""",
			"""
class Test {
	private def target
	def test() {
		target
		target/**/
	}
}
			"""
		),
		testTupleDeclaration:
		new TestCase(
			"""
class Test {
	def test() {
		def (target/**/, _) = [0, 0]
	}
}
			"""
		),
		testRename:
		new TestCase(
			"""
class Test {
	def test() {
		def target/**/
		target
	}
}
			""",
			"""
class Test {
	private def renamed
	def test() {
		renamed/**/
		renamed
	}
}
			""",
			"renamed"
		),
		testInitialization:
		new TestCase(
			"""
class Test {
	def test() {
		def target/**/ = 15
	}
}
			""",
			"""
class Test {
	private def target
	def test() {
		target/**/ = 15
	}
}
			"""
		),
		testVariableConflict:
		new TestCase(
			"""
class Test {
	def test() {
		def variableConflict
		def target/**/
	}
}
			""",
			"""
class Test {
	private def variableConflict
	def test() {
		def variableConflict
		variableConflict/**/
	}
}
			""",
			"variableConflict",
			true
		),
		testFieldConflict:
		new TestCase(
			"""
class Test {
	def fieldConflict
	def test() {
		def target/**/
	}
}
			""",
			null,
			"fieldConflict"
		),
		testFieldReference:
		new TestCase(
			"""
class Test {
	def target
	def test() {
		target/**/
	}
}
			"""
		),
		testException:
		new TestCase(
			"""
class Test {
	def test() {
		try {
		} catch(Exception target/**/) {
		}
	}
}
			"""
		),
		testForLoop:
		new TestCase(
			"""
class Test {
	def test() {
		for(int target/**/ = 0; target < 10; target++);
	}
}
			""",
			"""
class Test {
	private int renamed
	def test() {
		for(renamed/**/ = 0; renamed < 10; renamed++);
	}
}
			""",
			"renamed"
		),
		testPostfix:
		new TestCase(
			"""
class Test {
	def test() {
		def target = 0
		target/**/++
	}
}
			""",
			"""
class Test {
	private def renamed
	def test() {
		renamed = 0
		renamed/**/++
	}
}
			""",
			"renamed"
		),
		testPrefix:
		new TestCase(
			"""
class Test {
	def test() {
		def target = false
		!target/**/
	}
}
			""",
			"""
class Test {
	private def target
	def test() {
		target = false
		!target/**/
	}
}
			"""
		),
		testMethodInvocation:
		new TestCase(
			"""
class Test {
	def test() {
		def target = new Object()
		target/**/.toString()
	}
}
			""",
			"""
class Test {
	private def target
	def test() {
		target = new Object()
		target/**/.toString()
	}
}
			"""
		),
		testParameterList:
		new TestCase(
			"""
class Test {
	def test(target/**/) {
	}
}
			"""
		),
		testArgumentList:
		new TestCase(
			"""
class Test {
	def test() {
		def target = "Hello, World!"
		println(target/**/)
	}
}
			""",
			"""
class Test {
	private def target
	def test() {
		target = "Hello, World!"
		println(target/**/)
	}
}
			"""
		),
		testInnerClass:
		new TestCase(
			"""
class Test {
	def test() {
		new Object() {
			def test() {
				def target/**/
			}
		}
	}
}
			""",
			"""
class Test {
	def test() {
		new Object() {
			private def target
			def test() {
				target/**/
			}
		}
	}
}
			"""
		),
		testInnerFieldConflict:
		new TestCase(
			"""
class Test {
	def test() {
		def target/**/
		new Object() {
			def innerFieldConflict
			def test() {
				target
			}
		}
	}
}
			""",
			"""
class Test {
	private def innerFieldConflict
	def test() {
		innerFieldConflict/**/
		new Object() {
			def innerFieldConflict
			def test() {
				innerFieldConflict
			}
		}
	}
}
			""",
			"innerFieldConflict",
			true
		),
		testFakeField:
		new TestCase(
			"""
class Test {
	def test() {
		def target/**/
		new Object() {
			def test() {
				target
			}
		}
	}
}
			""",
			"""
class Test {
	private def target
	def test() {
		target/**/
		new Object() {
			def test() {
				target
			}
		}
	}
}
			"""
		),
		testClosure:
		new TestCase(
			"""
class Test {
	def test() {
		def target
		def closure = {
			target/**/
		}
	}
}
			""",
			"""
class Test {
	private def renamed
	def test() {
		renamed
		def closure = {
			renamed/**/
		}
	}
}
			""",
			"renamed"
		),
		testClosureVariableConflict:
		new TestCase(
			"""
class Test {
	def test() {
		def target/**/
		def closure = {
			def closureVariableConflict
			target
		}
	}
}
			""",
			"""
class Test {
	private def closureVariableConflict
	def test() {
		closureVariableConflict/**/
		def closure = {
			def closureVariableConflict
			closureVariableConflict
		}
	}
}
			""",
			"closureVariableConflict",
			true
		),
		testClosureParameterList:
		new TestCase(
			"""
class Test {
	def test() {
		def closure = { target/**/ -> target }
	}
}
			"""
		),
		testClosureImplicitIt:
		new TestCase(
			"""
class Test {
	def test() {
		def target/**/
		def closure = { target }
	}
}
			""",
			"""
class Test {
	private def it
	def test() {
		it/**/
		def closure = { it }
	}
}
			""",
			"it",
			true
		),
	]
}
