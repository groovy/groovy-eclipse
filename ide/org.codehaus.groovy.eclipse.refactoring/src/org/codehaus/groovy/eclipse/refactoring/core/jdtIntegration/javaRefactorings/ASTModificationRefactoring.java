/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings;

import java.util.HashMap;
import java.util.List;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.JavaModelSearch;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector.SimpleNameCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.TextEdit;

/**
 * Abstract general AST Rewrite based Refactoring. This class has 3 template methods
 * that must be overwritten by subclasses. With these methods, the Refactoring can
 * bee parameterized to work like it should.
 * 
 * @author Stefan Reinhard
 */
public abstract class ASTModificationRefactoring extends Refactoring {
	
	protected HashMap<ICompilationUnit, ASTRewrite> rewrites;
	protected IJavaProject javaProject;

	public ASTModificationRefactoring(IProject project) {
		rewrites = new HashMap<ICompilationUnit, ASTRewrite>();
		javaProject = JavaCore.create(project);
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) 
	throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		try {
			// Search for all Java classes with references
			getReferencingCompilationUnits();
			
			// Rename those references
			createASTRewrites();
		} catch (CoreException ce) {
			status.addError(ce.getMessage());
		}
		return status;
	}
	
	/**
	 * Searches for all CompilationUnits with references to the element we want
	 * to refactor and puts them as keys into the rewrite map.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	protected void getReferencingCompilationUnits() throws CoreException, JavaModelException {	
		SearchPattern pattern = getSearchPattern();
		JavaModelSearch search = new JavaModelSearch(javaProject, pattern);
		List<IJavaElement> references = search.searchAll(IJavaElement.class);
		for (IJavaElement element : references) {
			ICompilationUnit unit = searchCompilationUnit(element);
			if (unit != null) rewrites.put(unit, null);
		}
	}
	
	/**
	 * Recursivley search for declaring Type
	 */
	private ICompilationUnit searchCompilationUnit(IJavaElement element) throws JavaModelException {
		if (element instanceof IType) {
			IType type = (IType)element;
			return type.getCompilationUnit();
		} else if (element instanceof IMember) {
			IMember member = (IMember)element;
			if (member.getParent() != null) {
				return searchCompilationUnit(member.getParent());
			}
		}
		return null;
	}
	
	/**
	 * Replaces all occurrences in a CompilationUnit delivered by <code>getCollector()</code>
	 * with 
	 */
	protected void createASTRewrites() {
		for (ICompilationUnit unit : rewrites.keySet()) {
			CompilationUnit ast = getAST(unit);
			SimpleNameCollector collector = getCollector();
			ast.accept(collector);
			if (collector.getOccurences().size() > 0) {
				ASTRewrite rewrite = ASTRewrite.create(ast.getAST());
				for (SimpleName name : collector.getOccurences()) {
					SimpleName renamed = ast.getAST().newSimpleName(getNewName(name));
					rewrite.replace(name, renamed, null);
				}
				rewrites.put(unit, rewrite);
			}
		}
	}
	
	/**
	 * Template Method used by <code>createASTRewrites()</code> to find
	 * all SimpleNames which should be replaced by <code>getNewName</code>
	 * @return
	 */
	protected abstract SimpleNameCollector getCollector();
	
	/**
	 * Template Method to provide the new name for all SimpleNames found
	 * by <code>getCollector()</code>
	 * @return
	 */
	protected abstract String getNewName(SimpleName oldName);
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException, OperationCanceledException {

		CompositeChange change = new CompositeChange(getName());
		
		for (ICompilationUnit sourceUnit : rewrites.keySet()) {
			String source = sourceUnit.getSource();
			Document document = new Document(source);
			ASTRewrite rewrite = rewrites.get(sourceUnit);
			if (rewrite != null) {
				TextEdit edits = rewrite.rewriteAST(document, javaProject.getOptions(true));
				String name = sourceUnit.getElementName();
				IFile file = (IFile)sourceUnit.getResource();
				TextFileChange textChange = new TextFileChange(name, file);
				textChange.setTextType("java");
				textChange.setEdit(edits);
				change.add(textChange);
			}
		}
		
		Change[] changes = change.getChildren();
		if (changes.length == 0) change.markAsSynthetic();
		
		return change;
	}
	
	/**
	 * Template Method used by <code>getReferencingCompilationUnits()</code>
	 * to search compilation units referencing to matched elements. Must
	 * not return null.
	 * @return
	 * @throws CoreException
	 */
	protected abstract SearchPattern getSearchPattern();

	protected CompilationUnit getAST(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}

