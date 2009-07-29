/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
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
import org.codehaus.groovy.eclipse.core.compiler.GroovyCompiler;
import org.codehaus.groovy.eclipse.core.compiler.GroovyCompilerConfigurationBuilder;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompiler;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompilerConfiguration;

/**
 * Class provides a AST ModuleNode (compiled from a sourcefile)
 */

public class ASTProvider {
	
	public static final String CLASS_PATH = FilePathHelper.getPathToCoreJar();
	
	public static ModuleNode getAST(String source, String sourcePath) {
		ByteArrayInputStream is = new ByteArrayInputStream(source.getBytes());
		TestCompilationReporter reporter = new TestCompilationReporter();
		IGroovyCompiler compiler = new GroovyCompiler();
		IGroovyCompilerConfiguration config = new GroovyCompilerConfigurationBuilder().buildAST().errorRecovery().classPath(CLASS_PATH).done();
		compiler.compile(sourcePath, is, config, reporter);
		ModuleNode root = reporter.moduleNode;
		return root;
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
