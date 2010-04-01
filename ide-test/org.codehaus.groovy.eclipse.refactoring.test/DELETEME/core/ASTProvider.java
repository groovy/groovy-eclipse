/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package core;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetCompiler;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.test.TestProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;

/**
 * Class provides a AST ModuleNode (compiled from a sourcefile)
 */

public class ASTProvider {
    
    public static ModuleNode getAST(String source, String sourcePath) {
	    return new GroovySnippetCompiler(createProject()).compile(source, sourcePath);
	}

    private static GroovyProjectFacade createProject() {
        return new GroovyProjectFacade(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(
                TestProject.TEST_PROJECT_NAME)));
    }
	
	public static Map<String,ModuleNode> getRootNodes(String[] sources, String[] fileNames) {
		Map<String, ModuleNode> moduleNodes = new HashMap<String, ModuleNode>();
		CompilationUnit compilationUnit = new CompilationUnit();
		
		for (int i = 0; i < sources.length; i++) {
			compilationUnit.addSource(fileNames[i], new ByteArrayInputStream(sources[i].getBytes()));
		}
		compilationUnit.compile(Phases.SEMANTIC_ANALYSIS);
		CompileUnit ast = compilationUnit.getAST();
		List<ModuleNode> modules = ast.getModules() ;
		for (ModuleNode node : modules) {
			moduleNodes.put(node.getDescription(), node);
		}
		return moduleNodes;
	}
}
