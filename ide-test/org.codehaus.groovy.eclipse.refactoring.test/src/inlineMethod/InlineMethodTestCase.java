/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package inlineMethod;

import java.io.File;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tests.RefactoringTestCase;

/**
 * Test Case to test the InlineMethod refactoring
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class InlineMethodTestCase extends RefactoringTestCase {
	
	InlineMethodInfo info; 

	public InlineMethodTestCase(String arg0, File arg1) {
		super(arg0, arg1);
	}

	@Override
    public void preAction() {
		InlineMethodProvider provider = new InlineMethodProvider(getDocumentProvider(),selection);
		info = new InlineMethodInfo(provider);
	}
	
	@Override
    public RefactoringStatus checkInitialCondition()
			throws OperationCanceledException, CoreException {
		return info.checkInitialConditions(new NullProgressMonitor());
	}
	
	@Override
    public void simulateUserInput() {
		if(properties.get("deleteMethod").equals("true"))
			info.setDeleteMethod(true);
		if(properties.get("allInvocations").equals("true"))
			info.setInlineAllInvocations(true);
	}

	@Override
    public RefactoringStatus checkFinalCondition()
			throws OperationCanceledException, CoreException {
		return info.checkFinalConditions(new NullProgressMonitor());
	}

	@Override
    public GroovyChange createChange() throws OperationCanceledException,
			CoreException {
		return info.createGroovyChange(new NullProgressMonitor());
	}
}
