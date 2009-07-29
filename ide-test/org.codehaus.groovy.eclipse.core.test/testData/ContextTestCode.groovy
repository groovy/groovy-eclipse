//#TAB_SPACING:4
// Set your editor to 4 spaces per tab befor editing this file.
//#testInModule1,1
package org.codehaus.groovy.eclipse.core.context.impl
//#testInModule1,1

//#testInModule1,1,5
import javax.swing.JFrame

//#testInModuleBody,1

/**
 * Test source code for ClassContextFactoryTests.java.
 * Be sure to set the tab spacing so that tabs can be converted to spaces. This preserves the column location as seen
 * in the IDE, to make it easy to identify columns, regardless of tabs.<br>
 * To tag a test location, use //#<testName>,<col[,col]*>. The line number will be the line following the tag.
 * Note, no spaces between colons and commas.
 *
 * @author empovazan
 */
//#testInModuleBody,1,17,23 #testNotInModuleBody,24 #testNotInClass,23 #testInClass,24
class ContextTestCode {
	//#testInClass,11 #testInClassBody1,11 #testInModule2,1 #testNotInModuleBody,1
	def a = 10
	
	//#testInClosureBody1,25,29,39
	def clos = { blah -> println blah }
	
	//#testInClassBody2,12,20 #testInCtorParams,21,32,39 #testInCtor,42 #testNotInCtor,41
	ContextTestCode(Integer a, float b) {
		//#testInCtor,25
		a = 10
		//#testInCtor,5
	}
	 
	//#testInClassBody3,19 #testInMethodParams,20,23,27 #testInMethodBody,30 #testNotInMethodBody,29
	def someMethod(a, b, c) {
		//#testInMethod,1
		//#testInClosureBody2,17,25,29
		a.each { println it }
		
		//#testNotInMethodParams,17,18
		println b
		
		//#testInMethod,5
	}
	//#testInClass,1
}