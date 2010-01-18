/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.CandidateCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.GroovyRefactoringDispatcher;
import org.codehaus.groovy.eclipse.refactoring.core.rename.JavaRefactoringDispatcher;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import core.SelectionHelper;

/**
 * @author Stefan Sidler
 */
public abstract class RenameTestCase extends BaseTestCase {

	public RenameTestCase(String name, File fileToTest) {
		super(name, fileToTest);
	}

	public void setUp() throws Exception {
		super.setUp();
		GroovyRuntime.addGroovyRuntime(testProject.getProject());
		
		addFilesToProject();
		
		String path = getSelectedFilePath();
		IFile file = testProject.getProject().getFile(path);
		selection = SelectionHelper.getSelection(properties, getContent(file));
		
	}

	protected void remoteGroovyRefactoring() throws JavaModelException, PartInitException, CoreException {
		String newName = getNewName();
		IFile file = testProject.getProject().getFile(getSelectedFilePath());
		IJavaElement element = JavaCore.create(file);
		if (element instanceof ICompilationUnit) {

			ICompilationUnit cu = (ICompilationUnit)element;
            IJavaElement[] elements= cu.codeSelect(selection.getOffset() + selection.getLength(), 0);
            
            // don't want to wait for indexes to complete when we don't need them to.
            if (elements.length == 0 || elements[0].isReadOnly()) {
                waitForIndexes();
                elements= cu.codeSelect(selection.getOffset() + selection.getLength(), 0);
            }
            
            
			if(elements.length > 0) {
				try {
					performJavaRefactoring(newName, elements[0]);
				} catch (InterruptedException e) {
				} catch (InvocationTargetException e) {
				    throw new JavaModelException(e, 0);
				}
			}
		}
	}

	protected void localGroovy_remoteJavaRefactoring() throws Exception {

		String newName = getNewName();
		IProject sourceProject = testProject.getProject();
		IFile file = testProject.getProject().getFile(getSelectedFilePath());
		WorkspaceDocumentProvider docProvider = new WorkspaceDocumentProvider(file);
		WorkspaceFileProvider fileProvider = new WorkspaceFileProvider(sourceProject, docProvider);

		CandidateCollector collector = new CandidateCollector(fileProvider.getSelectionDocument(), selection);
		
		waitForIndexes();
		try {
			IJavaElement[] javaCandidates = collector.getJavaCandidates();
			ASTNode[] groovyCandidates = collector.getGroovyCandidates();
			
			if (javaCandidates.length > 1 ||  groovyCandidates.length > 1) {		
				System.err.println("Select first from more than one candidate!");
			} else if (javaCandidates.length == 0 ||  groovyCandidates.length == 0) {
			    // something's weird...maybe indexes aren't complete???
			    waitForIndexes();
	            javaCandidates = collector.getJavaCandidates();
	            groovyCandidates = collector.getGroovyCandidates();
			}
			

			// don't try to do java refactoring on a groovy unit
			if (javaCandidates.length > 0 && !javaCandidates[0].isReadOnly() && javaCandidates[0] instanceof IMember
			    && ((IMember) javaCandidates[0]).getCompilationUnit().getElementName().endsWith(".java")) {
				performJavaRefactoring(newName, javaCandidates[0]);
			} else if (groovyCandidates.length > 0){
				GroovyRefactoringDispatcher dispatcher = 
					new GroovyRefactoringDispatcher(groovyCandidates[0],
							selection, docProvider, null);
				GroovyRefactoring ref = dispatcher.dispatchGroovyRenameRefactoring();
				
				if (ref.getInfo() instanceof RenameInfo) {
					RenameInfo renameInfo = (RenameInfo)ref.getInfo();
					renameInfo.setNewName(newName);
				}
				PerformRefactoringOperation op = 
					new PerformRefactoringOperation(ref,CheckConditionsOperation.ALL_CONDITIONS);
				op.run(null);
			} else {
				throw new RuntimeException("No candidates found!");
			}
		} catch (Exception ex) {
			throw ex;
		}
	}
		
	private void performJavaRefactoring(String newName,
			IJavaElement javaCandidate)
			throws CoreException, InterruptedException,
			InvocationTargetException {
		JavaRefactoringDispatcher dispatcher = new JavaRefactoringDispatcher(javaCandidate);
		
		dispatcher.setNewName(newName);
		RenameSupport renameSupport = dispatcher.dispatchJavaRenameRefactoring();
		assertNotNull("Error in 'dispatchJavaRenameRefactoring' -> no RenameSupport",renameSupport);
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

		renameSupport.perform(shell, dialog);
	}
	


	public void testRefactoring() throws Exception {
	    if (getName().endsWith("RenameGroovyClass_Test_Expression.txt")) {
	        System.out.println("Test is failing...ignore");
	    }
	    
		IMember element = searchVarDeclaration();
		if (element != null) {
			// Local Java Refactoring
			localJavaRefactoring(element);
		} else {
			if (isJavaFile(getSelectedFilePath())) {
				// Remote Groovy Refactoring
				remoteGroovyRefactoring();
			} else {
				// Local Groovy and Remote Java Refactoring
				localGroovy_remoteJavaRefactoring();	
			}
		}
		compareWithExpected();
	}
	
	protected abstract IMember searchVarDeclaration() throws Exception;
	protected abstract void localJavaRefactoring(IMember element);
	protected abstract String getNewName();

}
