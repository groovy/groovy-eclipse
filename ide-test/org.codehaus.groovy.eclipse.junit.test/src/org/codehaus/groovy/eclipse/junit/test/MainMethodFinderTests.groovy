 /*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.eclipse.junit.test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.util.MainMethodSearchEngine;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.core.IType;

/**
 * @author Andrew Eisenberg
 * @created Oct 20, 2009
 *
 */
class MainMethodFinderTests extends JUnitTestCase {
	
    public MainMethodFinderTests(String name) {
        super(name);
    }
    
    void testMainMethodFinder1() throws Exception {
		IPath projectPath = createGenericProject()
		IPath root = projectPath.append("src")
		env.addGroovyClass(root, "p2", "Hello",
		        """
		        class Foo {
		            static def main(args) {
		            }
		        }
		        """
				)
		
		incrementalBuild(projectPath)
		expectingNoProblems()
		MainMethodSearchEngine engine = new MainMethodSearchEngine()
		IType[] types = engine.searchMainMethods ( (IProgressMonitor) null, new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
		checkTypes(types, [ 'p2.Foo' ])
    }
    void testMainMethodFinder2() throws Exception {
        IPath projectPath = createGenericProject()
        IPath root = projectPath.append("src")
        env.addGroovyClass(root, "p2", "Hello",
                """
                class Foo {
                static def main(String ... args) {
                }
                }
                """
        )
        
        incrementalBuild(projectPath)
        expectingNoProblems()
        MainMethodSearchEngine engine = new MainMethodSearchEngine()
        IType[] types = engine.searchMainMethods ( (IProgressMonitor) null, new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
        checkTypes(types, [  ])
    }
	
    void testMainMethodFinder3() throws Exception {
        IPath projectPath = createGenericProject()
        IPath root = projectPath.append("src")
        env.addGroovyClass(root, "p2", "Hello",
                """
                class Foo {
                static def main(String[] args) {
                }
                }
                """
        )
        
        incrementalBuild(projectPath)
        expectingNoProblems()
        MainMethodSearchEngine engine = new MainMethodSearchEngine()
        IType[] types = engine.searchMainMethods ( (IProgressMonitor) null, new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
        checkTypes(types, [ ])
    }
	
    void testMainMethodFinder4() throws Exception {
        IPath projectPath = createGenericProject()
        IPath root = projectPath.append("src")
        env.addGroovyClass(root, "p2", "Hello",
                """
                class Foo {
                private static def main(String[] args) {
                }
                }
                """
        )
        
        incrementalBuild(projectPath)
        expectingNoProblems()
        MainMethodSearchEngine engine = new MainMethodSearchEngine()
        IType[] types = engine.searchMainMethods ( (IProgressMonitor) null, new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
        checkTypes(types, [ ])
    }
	
	
    void testMainMethodFinder5() throws Exception {
        IPath projectPath = createGenericProject()
        IPath root = projectPath.append("src")
        env.addGroovyClass(root, "p2", "Hello",
                """
                class Foo {
                def main(String[] args) {
                }
                }
                """
        )
        
        incrementalBuild(projectPath)
        expectingNoProblems()
        MainMethodSearchEngine engine = new MainMethodSearchEngine()
        IType[] types = engine.searchMainMethods ( (IProgressMonitor) null, new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
        checkTypes(types, [ ])
    }
    
	void testMainMethodFinder6() throws Exception {
		IPath projectPath = createGenericProject()
		IPath root = projectPath.append("src")
		env.addGroovyClass(root, "p2", "Hello",
				"""
		        print "Nothing"
                """
				)
		
		incrementalBuild(projectPath)
		expectingNoProblems()
		MainMethodSearchEngine engine = new MainMethodSearchEngine()
		IType[] types = engine.searchMainMethods ( (IProgressMonitor) null, new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
		checkTypes(types, [ "p2.Hello" ])
	}
	
	void testMainMethodFinder7() throws Exception {
	    IPath projectPath = createGenericProject()
	    IPath root = projectPath.append("src")
	    env.addGroovyClass(root, "p2", "Hello",
	            """
	            print "Hello"

	            class Foo {
	                static def main(args) {
	                    
	                }
	            }
	            """
	    )
	    
	    incrementalBuild(projectPath)
	    expectingNoProblems()
	    MainMethodSearchEngine engine = new MainMethodSearchEngine()
	    IType[] types = engine.searchMainMethods ( (IProgressMonitor) null, new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
	    checkTypes(types, [ "p2.Hello", "p2.Foo" ])
	}
	
	void testMainMethodFinder8() throws Exception {
	    IPath projectPath = createGenericProject()
	    IPath root = projectPath.append("src")
	    env.addGroovyClass(root, "p2", "Hello",
	            """
	            print "Hello"
	            
	            class Foo {
	            static def main(args) {
	            
	            }
	            }
	            class Bar {
	            static def main(args) {
	            
	            }
	            }
	            """
	    )
	    
	    incrementalBuild(projectPath)
	    expectingNoProblems()
	    MainMethodSearchEngine engine = new MainMethodSearchEngine()
	    IType[] types = engine.searchMainMethods ( (IProgressMonitor) null, new JavaWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES)
	    checkTypes(types, [ "p2.Hello", "p2.Foo", "p2.Bar" ])
	}
	
	private checkTypes(IType[] types, List<String> expected) {
		assertEquals("Wrong number main methods found: ${printTypes(types)}", expected.size(), types.length)
		for (int i = 0; i < types.length; i++) {
		    assertEquals expected[i], types[i].fullyQualifiedName
		}
	}
	
	private def printTypes(types) {
		StringBuilder sb = new StringBuilder()
	    types.each { IType it -> 
			sb << "${it.elementName}, " 
	    }
	}
	
}