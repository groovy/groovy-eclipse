/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration.renameMethod;

import java.util.List;

import jdtIntegration.ProgrammaticalRenameTest;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;

import core.ASTProvider;
import core.FilePathHelper;

/**
 * @author Stefan Reinhard
 */
public class RenameMethodTest extends ProgrammaticalRenameTest {

	
	static String fileName = FilePathHelper.getPathToTestFiles()
	+ "jdtIntegration/startedFromJava/renameMethod/InheritedMethodRename.txt";

	public RenameMethodTest() {
		super(fileName);
	}
	
	public void testRefactoring() {		
		ClassNode cN = ClassHelper.make("MethodClass");
		ModuleNode mN = ASTProvider.getAST(getArea(origin), fileName);
		
		ClassNode org = cN;
		for(ClassNode cl : ((List<ClassNode>)mN.getClasses())) {
			if (cN.equals(cl)) {
				System.out.println(cl.getName());
				org = cl;
			}
		}
		
		MethodNode original = org.getMethods().get(0);
		MethodNode methodNode = new MethodNode("meth", original.getModifiers(),
				original.getReturnType(), original.getParameters(), null, null);
		methodNode.setDeclaringClass(cN);
		
		MethodPattern mP = new MethodPattern(methodNode, cN);
		RenameMethodProvider provider = new RenameMethodProvider(fileProvider, mP);
		provider.setNewName("method");
		
		checkRefactoring(provider);	
	}
	
}
