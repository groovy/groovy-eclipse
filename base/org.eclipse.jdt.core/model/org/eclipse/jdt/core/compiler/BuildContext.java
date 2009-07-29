/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.core.compiler;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.core.builder.CompilationParticipantResult;
import org.eclipse.jdt.internal.core.builder.SourceFile;

/**
 * The context of a build event that is notified to interested compilation 
 * participants when {@link CompilationParticipant#buildStarting(BuildContext[], boolean) a build is starting},
 * or to annotations processors when {@link CompilationParticipant#processAnnotations(BuildContext[]) a source file has annotations}.
 * 
 * @since 3.2
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class BuildContext extends CompilationParticipantResult {

/**
 * Creates a build context for the given source file.
 * <p>
 * This constructor is not intended to be called by clients.
 * </p>
 * 
 * @param sourceFile the source file being built
 */
public BuildContext(SourceFile sourceFile) {
	super(sourceFile);
}

/**
 * Returns the contents of the compilation unit.
 * 
 * @return the contents of the compilation unit
 */
public char[] getContents() {
	return this.sourceFile.getContents();
}

/**
 * Returns the <code>IFile</code> representing the compilation unit.
 * 
 * @return the <code>IFile</code> representing the compilation unit
 */
public IFile getFile() {
	return this.sourceFile.resource;
}

/**
 * Returns whether the compilation unit contained any annotations when it was compiled.
 * 
 * NOTE: This is only valid during {@link CompilationParticipant#processAnnotations(BuildContext[])}.
 * 
 * @return whether the compilation unit contained any annotations when it was compiled
 */
public boolean hasAnnotations() {
	return this.hasAnnotations; // only set during processAnnotations
}

/**
 * Record the added/changed generated files that need to be compiled.
 * 
 * @param addedGeneratedFiles the added/changed files
 */
public void recordAddedGeneratedFiles(IFile[] addedGeneratedFiles) {
	int length2 = addedGeneratedFiles.length;
	if (length2 == 0) return;

	int length1 = this.addedFiles == null ? 0 : this.addedFiles.length;
	IFile[] merged = new IFile[length1 + length2];
	if (length1 > 0) // always make a copy even if currently empty
		System.arraycopy(this.addedFiles, 0, merged, 0, length1);
	System.arraycopy(addedGeneratedFiles, 0, merged, length1, length2);
	this.addedFiles = merged;
}

/**
 * Record the generated files that need to be deleted.
 * 
 * @param deletedGeneratedFiles the files that need to be deleted
 */
public void recordDeletedGeneratedFiles(IFile[] deletedGeneratedFiles) {
	int length2 = deletedGeneratedFiles.length;
	if (length2 == 0) return;

	int length1 = this.deletedFiles == null ? 0 : this.deletedFiles.length;
	IFile[] merged = new IFile[length1 + length2];
	if (length1 > 0) // always make a copy even if currently empty
		System.arraycopy(this.deletedFiles, 0, merged, 0, length1);
	System.arraycopy(deletedGeneratedFiles, 0, merged, length1, length2);
	this.deletedFiles = merged;
}

/**
 * Record the fully-qualified type names of any new dependencies, each name is of the form "p1.p2.A.B".
 * 
 * @param typeNameDependencies the fully-qualified type names of new dependencies
 */
public void recordDependencies(String[] typeNameDependencies) {
	int length2 = typeNameDependencies.length;
	if (length2 == 0) return;

	int length1 = this.dependencies == null ? 0 : this.dependencies.length;
	String[] merged = new String[length1 + length2];
	if (length1 > 0) // always make a copy even if currently empty
		System.arraycopy(this.dependencies, 0, merged, 0, length1);
	System.arraycopy(typeNameDependencies, 0, merged, length1, length2);
	this.dependencies = merged;
}

/**
 * Record new problems to report against this compilationUnit.
 * Markers are persisted for these problems only for the declared managed marker type
 * (see the 'compilationParticipant' extension point).
 * 
 * @param newProblems the problems to report
 */
public void recordNewProblems(CategorizedProblem[] newProblems) {
	int length2 = newProblems.length;
	if (length2 == 0) return;

	int length1 = this.problems == null ? 0 : this.problems.length;
	CategorizedProblem[] merged = new CategorizedProblem[length1 + length2];
	if (length1 > 0) // always make a copy even if currently empty
		System.arraycopy(this.problems, 0, merged, 0, length1);	
	System.arraycopy(newProblems, 0, merged, length1, length2);	
	this.problems = merged;
}

}
