//#TAB_SPACING:4
// Set your editor to 4 spaces per tab befor editing this file.

/**
 * Test source code for ScriptContextFactoryTests.java.
 *
 * In a script class it is possible to have moduleScope as well as classScope. This is because all variables are 
 * considered to be locals or dynamic. But they are declared/used in the same area as package and imports.
 *
 * Be sure to set the tab spacing so that tabs can be converted to spaces. This preserves the column location as seen
 * in the IDE, to make it easy to identify columns, regardless of tabs.<br>
 * To tag a test location, use //#<testName>,<col[,col]*>. The line number will be the line following the tag.
 * Note, no spaces between colons and commas.
 *
 * @author empovazan
 */

//#testInModule1,1
package org.codehaus.groovy.eclipse.core.context.impl
//#testInModule1,1

//#testInModule1,1,5
import javax.swing.JFrame

//#testInModuleBody,1

//#testInModule2,1
def a = 10

//#testInClosureBody1,21,25,35
def clos = { blah -> println blah }
	
//#testInMethodParams,16,19,23 #testInMethodBody,26
def someMethod(a, b, c) {
	//#testInMethod,1
	//#testInClosureBody2,13,21,25
	a.each { println it }
	//#testNotInMethodParams,17,18
	println b
	
	//#testInMethod,5
}