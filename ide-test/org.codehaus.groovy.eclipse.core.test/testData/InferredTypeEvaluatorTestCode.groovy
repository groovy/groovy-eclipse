//#TAB_SPACING:4
//Set your editor to 4 spaces per tab befor editing this file.
package org.codehaus.groovy.eclipse.core.type;

/**
* Test source code for InferredTypeEvaluatorTests.java.
* Be sure to set the tab spacing so that tabs can be converted to spaces. This preserves the column location as seen
* in the IDE, to make it easy to identify columns, regardless of tabs.<br>
* To tag a test expression, use //#<testName>,start,end,type. The line number will be the line following the tag.
* Note, no spaces between colons and commas.
* The tests must be unique.
* See the test class itself for how to get valid locations for new tests.
* @author empovazan
*/
class InferredTypeEvaluatorTestCode {
	def greeting = "hello"
	def frame = new javax.swing.JFrame()
	def undefined
	
	InferredTypeEvaluatorTestCode() {
		undefined = 1.0d
	}

	def someMethod() {
		def localVar = new String();
		//#testLocalAsString,9,17,java.lang.String
		localVar.toString()
		
		localVar = 10
		//#testLocalAsInteger,9,17,java.lang.Integer
		localVar.doubleValue()
		
		//#testUndefinedFieldValue,9,18,java.lang.Double
		undefined.intValue()
	}
	
	def greetingFrame() {
		//#testFieldAsJFrame,9,14,javax.swing.JFrame
		frame.title = greeting
		
		//#testFieldAsString,9,17,java.lang.String
		greeting
	}
	
	def doit() {
		return "it"
	}
	
	def done() {
		def l = []
		def m = [:]
		
		//#testListType,9,10,java.util.List
		l[1] = 10
		//#testMapType,9,10,java.util.Map
		m[0] = "hello"
		true
	}
	
	def returns() {
		def a = doit()
		//#testExplicitReturn,9,10,java.lang.String
		a
		
		def b = done()
		//#testImplicitReturn,9,10,java.lang.Boolean
		b
	}
	
	def adder() {
		def sum;
		sum = add(10, 20)
	}
	
	def add(a, b) {
		def result
		//#testFirstParam,18,19,java.lang.Integer
		result = a
		//#testSecondParam,19,20,java.lang.Integer
		result += b
	}
	
	def bitwiseNegateOperator() {
		//#testBitwiseNegateOperatorPattern,23,28,java.util.regex.Pattern
		def pattern = ~/.*/
		
		//#testBitwiseNegateOperatorInteger,19,22,java.lang.Integer
		def val = ~10
	}
}