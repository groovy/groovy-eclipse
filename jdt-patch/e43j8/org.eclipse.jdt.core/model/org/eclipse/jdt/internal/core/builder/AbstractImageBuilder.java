/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

import java.io.*;
import java.util.*;

/**
 * The abstract superclass of Java builders.
 * Provides the building and compilation mechanism
 * in common with the batch and incremental builders.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractImageBuilder implements ICompilerRequestor, ICompilationUnitLocator {

protected JavaBuilder javaBuilder;
protected State newState;

// local copies
protected NameEnvironment nameEnvironment;
protected ClasspathMultiDirectory[] sourceLocations;
protected BuildNotifier notifier;

protected Compiler compiler;
protected WorkQueue workQueue;
protected ArrayList problemSourceFiles;
protected boolean compiledAllAtOnce;

private boolean inCompiler;

protected boolean keepStoringProblemMarkers;
protected SimpleSet filesWithAnnotations = null;

//2000 is best compromise between space used and speed
public static int MAX_AT_ONCE = Integer.getInteger(JavaModelManager.MAX_COMPILED_UNITS_AT_ONCE, 2000).intValue();
public final static String[] JAVA_PROBLEM_MARKER_ATTRIBUTE_NAMES = {
	IMarker.MESSAGE,
	IMarker.SEVERITY,
	IJavaModelMarker.ID,
	IMarker.CHAR_START,
	IMarker.CHAR_END,
	IMarker.LINE_NUMBER,
	IJavaModelMarker.ARGUMENTS,
	IJavaModelMarker.CATEGORY_ID,
};
public final static String[] JAVA_TASK_MARKER_ATTRIBUTE_NAMES = {
	IMarker.MESSAGE,
	IMarker.PRIORITY,
	IJavaModelMarker.ID,
	IMarker.CHAR_START,
	IMarker.CHAR_END,
	IMarker.LINE_NUMBER,
	IMarker.USER_EDITABLE,
	IMarker.SOURCE_ID,
};
public final static Integer S_ERROR = new Integer(IMarker.SEVERITY_ERROR);
public final static Integer S_WARNING = new Integer(IMarker.SEVERITY_WARNING);
public final static Integer P_HIGH = new Integer(IMarker.PRIORITY_HIGH);
public final static Integer P_NORMAL = new Integer(IMarker.PRIORITY_NORMAL);
public final static Integer P_LOW = new Integer(IMarker.PRIORITY_LOW);

protected AbstractImageBuilder(JavaBuilder javaBuilder, boolean buildStarting, State newState) {
	// local copies
	this.javaBuilder = javaBuilder;
	this.nameEnvironment = javaBuilder.nameEnvironment;
	this.sourceLocations = this.nameEnvironment.sourceLocations;
	this.notifier = javaBuilder.notifier;
	this.keepStoringProblemMarkers = true; // may get disabled when missing classfiles are encountered

	if (buildStarting) {
		this.newState = newState == null ? new State(javaBuilder) : newState;
		this.compiler = newCompiler();
		this.workQueue = new WorkQueue();
		this.problemSourceFiles = new ArrayList(3);

		if (this.javaBuilder.participants != null) {
			for (int i = 0, l = this.javaBuilder.participants.length; i < l; i++) {
				if (this.javaBuilder.participants[i].isAnnotationProcessor()) {
					// initialize this set so the builder knows to gather CUs that define Annotation types
					// each Annotation processor participant is then asked to process these files AFTER
					// the compile loop. The normal dependency loop will then recompile all affected types
					this.filesWithAnnotations = new SimpleSet(1);
					break;
				}
			}
		}
	}
}

public void acceptResult(CompilationResult result) {
	// In Batch mode, we write out the class files, hold onto the dependency info
	// & additional types and report problems.

	// In Incremental mode, when writing out a class file we need to compare it
	// against the previous file, remembering if structural changes occured.
	// Before reporting the new problems, we need to update the problem count &
	// remove the old problems. Plus delete additional class files that no longer exist.

	SourceFile compilationUnit = (SourceFile) result.getCompilationUnit(); // go directly back to the sourceFile
	if (!this.workQueue.isCompiled(compilationUnit)) {
		this.workQueue.finished(compilationUnit);

		try {
			updateProblemsFor(compilationUnit, result); // record compilation problems before potentially adding duplicate errors
			updateTasksFor(compilationUnit, result); // record tasks
		} catch (CoreException e) {
			throw internalException(e);
		}

		if (result.hasInconsistentToplevelHierarchies)
			// ensure that this file is always retrieved from source for the rest of the build
			if (!this.problemSourceFiles.contains(compilationUnit))
				this.problemSourceFiles.add(compilationUnit);

		IType mainType = null;
		String mainTypeName = null;
		String typeLocator = compilationUnit.typeLocator();
		ClassFile[] classFiles = result.getClassFiles();
		int length = classFiles.length;
		ArrayList duplicateTypeNames = null;
		ArrayList definedTypeNames = new ArrayList(length);
		for (int i = 0; i < length; i++) {
			ClassFile classFile = classFiles[i];

			char[][] compoundName = classFile.getCompoundName();
			char[] typeName = compoundName[compoundName.length - 1];
			boolean isNestedType = classFile.isNestedType;

			// Look for a possible collision, if one exists, report an error but do not write the class file
			if (isNestedType) {
				String qualifiedTypeName = new String(classFile.outerMostEnclosingClassFile().fileName());
				if (this.newState.isDuplicateLocator(qualifiedTypeName, typeLocator))
					continue;
			} else {
				String qualifiedTypeName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
				if (this.newState.isDuplicateLocator(qualifiedTypeName, typeLocator)) {
					if (duplicateTypeNames == null)
						duplicateTypeNames = new ArrayList();
					duplicateTypeNames.add(compoundName);
					if (mainType == null) {
						try {
							mainTypeName = compilationUnit.initialTypeName; // slash separated qualified name "p1/p1/A"
							mainType = this.javaBuilder.javaProject.findType(mainTypeName.replace('/', '.'));
						} catch (JavaModelException e) {
							// ignore
						}
					}
					IType type;
					if (qualifiedTypeName.equals(mainTypeName)) {
						type = mainType;
					} else {
						String simpleName = qualifiedTypeName.substring(qualifiedTypeName.lastIndexOf('/')+1);
						type = mainType == null ? null : mainType.getCompilationUnit().getType(simpleName);
					}
					createProblemFor(compilationUnit.resource, type, Messages.bind(Messages.build_duplicateClassFile, new String(typeName)), JavaCore.ERROR);
					continue;
				}
				this.newState.recordLocatorForType(qualifiedTypeName, typeLocator);
				if (result.checkSecondaryTypes && !qualifiedTypeName.equals(compilationUnit.initialTypeName))
					acceptSecondaryType(classFile);
			}
			try {
				definedTypeNames.add(writeClassFile(classFile, compilationUnit, !isNestedType));
			} catch (CoreException e) {
				Util.log(e, "JavaBuilder handling CoreException"); //$NON-NLS-1$
				if (e.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS)
					createProblemFor(compilationUnit.resource, null, Messages.bind(Messages.build_classFileCollision, e.getMessage()), JavaCore.ERROR);
				else
					createProblemFor(compilationUnit.resource, null, Messages.build_inconsistentClassFile, JavaCore.ERROR);
			}
		}
		if (result.hasAnnotations && this.filesWithAnnotations != null) // only initialized if an annotation processor is attached
			this.filesWithAnnotations.add(compilationUnit);

		this.compiler.lookupEnvironment.releaseClassFiles(classFiles);
		finishedWith(typeLocator, result, compilationUnit.getMainTypeName(), definedTypeNames, duplicateTypeNames);
		this.notifier.compiled(compilationUnit);
	}
}

protected void acceptSecondaryType(ClassFile classFile) {
	// noop
}

protected void addAllSourceFiles(final ArrayList sourceFiles) throws CoreException {
	for (int i = 0, l = this.sourceLocations.length; i < l; i++) {
		final ClasspathMultiDirectory sourceLocation = this.sourceLocations[i];
		final char[][] exclusionPatterns = sourceLocation.exclusionPatterns;
		final char[][] inclusionPatterns = sourceLocation.inclusionPatterns;
		final boolean isAlsoProject = sourceLocation.sourceFolder.equals(this.javaBuilder.currentProject);
		final int segmentCount = sourceLocation.sourceFolder.getFullPath().segmentCount();
		final IContainer outputFolder = sourceLocation.binaryFolder;
		final boolean isOutputFolder = sourceLocation.sourceFolder.equals(outputFolder);
		sourceLocation.sourceFolder.accept(
			new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					switch(proxy.getType()) {
						case IResource.FILE :
							if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(proxy.getName())) {
								IResource resource = proxy.requestResource();
								if (exclusionPatterns != null || inclusionPatterns != null)
									if (Util.isExcluded(resource.getFullPath(), inclusionPatterns, exclusionPatterns, false))
										return false;
								sourceFiles.add(new SourceFile((IFile) resource, sourceLocation));
							}
							return false;
						case IResource.FOLDER :
							IPath folderPath = null;
							if (isAlsoProject)
								if (isExcludedFromProject(folderPath = proxy.requestFullPath()))
									return false;
							if (exclusionPatterns != null) {
								if (folderPath == null)
									folderPath = proxy.requestFullPath();
								if (Util.isExcluded(folderPath, inclusionPatterns, exclusionPatterns, true)) {
									// must walk children if inclusionPatterns != null, can skip them if == null
									// but folder is excluded so do not create it in the output folder
									return inclusionPatterns != null;
								}
							}
							if (!isOutputFolder) {
								if (folderPath == null)
									folderPath = proxy.requestFullPath();
								String packageName = folderPath.lastSegment();
								if (packageName.length() > 0) {
									String sourceLevel = AbstractImageBuilder.this.javaBuilder.javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
									String complianceLevel = AbstractImageBuilder.this.javaBuilder.javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
									if (JavaConventions.validatePackageName(packageName, sourceLevel, complianceLevel).getSeverity() != IStatus.ERROR)
										createFolder(folderPath.removeFirstSegments(segmentCount), outputFolder);
								}
							}
					}
					return true;
				}
			},
			IResource.NONE
		);
		this.notifier.checkCancel();
	}
}

protected void cleanUp() {
	this.nameEnvironment.cleanup();

	this.javaBuilder = null;
	this.nameEnvironment = null;
	this.sourceLocations = null;
	this.notifier = null;
	this.compiler = null;
	this.workQueue = null;
	this.problemSourceFiles = null;
}

/* Compile the given elements, adding more elements to the work queue
* if they are affected by the changes.
*/
protected void compile(SourceFile[] units) {
	if (this.filesWithAnnotations != null && this.filesWithAnnotations.elementSize > 0)
		// will add files that have annotations in acceptResult() & then processAnnotations() before exitting this method
		this.filesWithAnnotations.clear();

	// notify CompilationParticipants which source files are about to be compiled
	CompilationParticipantResult[] participantResults = this.javaBuilder.participants == null ? null : notifyParticipants(units);
	if (participantResults != null && participantResults.length > units.length) {
		units = new SourceFile[participantResults.length];
		for (int i = participantResults.length; --i >= 0;)
			units[i] = participantResults[i].sourceFile;
	}

	int unitsLength = units.length;
	this.compiledAllAtOnce = MAX_AT_ONCE == 0 || unitsLength <= MAX_AT_ONCE;
	if (this.compiledAllAtOnce) {
		// do them all now
		if (JavaBuilder.DEBUG)
			for (int i = 0; i < unitsLength; i++)
				System.out.println("About to compile " + units[i].typeLocator()); //$NON-NLS-1$
		compile(units, null, true);
	} else {
		SourceFile[] remainingUnits = new SourceFile[unitsLength]; // copy of units, removing units when about to compile
		System.arraycopy(units, 0, remainingUnits, 0, unitsLength);
		int doNow = unitsLength < MAX_AT_ONCE ? unitsLength : MAX_AT_ONCE;
		SourceFile[] toCompile = new SourceFile[doNow];
		int remainingIndex = 0;
		boolean compilingFirstGroup = true;
		while (remainingIndex < unitsLength) {
			int count = 0;
			while (remainingIndex < unitsLength && count < doNow) {
				// Although it needed compiling when this method was called, it may have
				// already been compiled when it was referenced by another unit.
				SourceFile unit = remainingUnits[remainingIndex];
				if (unit != null && (compilingFirstGroup || this.workQueue.isWaiting(unit))) {
					if (JavaBuilder.DEBUG)
						System.out.println("About to compile #" + remainingIndex + " : "+ unit.typeLocator()); //$NON-NLS-1$ //$NON-NLS-2$
					toCompile[count++] = unit;
				}
				remainingUnits[remainingIndex++] = null;
			}
			if (count < doNow)
				System.arraycopy(toCompile, 0, toCompile = new SourceFile[count], 0, count);
			if (!compilingFirstGroup)
				for (int a = remainingIndex; a < unitsLength; a++)
					if (remainingUnits[a] != null && this.workQueue.isCompiled(remainingUnits[a]))
						remainingUnits[a] = null; // use the class file for this source file since its been compiled
			compile(toCompile, remainingUnits, compilingFirstGroup);
			compilingFirstGroup = false;
		}
	}

	if (participantResults != null) {
		for (int i = participantResults.length; --i >= 0;)
			if (participantResults[i] != null)
				recordParticipantResult(participantResults[i]);

		processAnnotations(participantResults);
	}
}

protected void compile(SourceFile[] units, SourceFile[] additionalUnits, boolean compilingFirstGroup) {
	if (units.length == 0) return;
	this.notifier.aboutToCompile(units[0]); // just to change the message

	// extend additionalFilenames with all hierarchical problem types found during this entire build
	if (!this.problemSourceFiles.isEmpty()) {
		int toAdd = this.problemSourceFiles.size();
		int length = additionalUnits == null ? 0 : additionalUnits.length;
		if (length == 0)
			additionalUnits = new SourceFile[toAdd];
		else
			System.arraycopy(additionalUnits, 0, additionalUnits = new SourceFile[length + toAdd], 0, length);
		for (int i = 0; i < toAdd; i++)
			additionalUnits[length + i] = (SourceFile) this.problemSourceFiles.get(i);
	}
	String[] initialTypeNames = new String[units.length];
	for (int i = 0, l = units.length; i < l; i++)
		initialTypeNames[i] = units[i].initialTypeName;
	this.nameEnvironment.setNames(initialTypeNames, additionalUnits);
	this.notifier.checkCancel();
	try {
		this.inCompiler = true;
		this.compiler.compile(units);
	} catch (AbortCompilation ignored) {
		// ignore the AbortCompilcation coming from BuildNotifier.checkCancelWithinCompiler()
		// the Compiler failed after the user has chose to cancel... likely due to an OutOfMemory error
	} finally {
		this.inCompiler = false;
	}
	// Check for cancel immediately after a compile, because the compiler may
	// have been cancelled but without propagating the correct exception
	this.notifier.checkCancel();
}

protected void copyResource(IResource source, IResource destination) throws CoreException {
	IPath destPath = destination.getFullPath();
	try {
		source.copy(destPath, IResource.FORCE | IResource.DERIVED, null);
	} catch (CoreException e) {
		// handle the case when the source resource is deleted
		source.refreshLocal(0, null);
		if (!source.exists()) return; // source resource was deleted so skip it
		throw e;
	}
	Util.setReadOnly(destination, false); // just in case the original was read only
}

protected void createProblemFor(IResource resource, IMember javaElement, String message, String problemSeverity) {
	try {
		IMarker marker = resource.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		int severity = problemSeverity.equals(JavaCore.WARNING) ? IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;

		ISourceRange range = null;
		if (javaElement != null) {
			try {
				range = javaElement.getNameRange();
			} catch (JavaModelException e) {
				if (e.getJavaModelStatus().getCode() != IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
					throw e;
				}
				if (!CharOperation.equals(javaElement.getElementName().toCharArray(), TypeConstants.PACKAGE_INFO_NAME)) {
					throw e;
				}
				// else silently swallow the exception as the synthetic interface type package-info has no
				// source range really. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=258145
			}
		}
		int start = range == null ? 0 : range.getOffset();
		int end = range == null ? 1 : start + range.getLength();
		marker.setAttributes(
			new String[] {IMarker.MESSAGE, IMarker.SEVERITY, IMarker.CHAR_START, IMarker.CHAR_END, IMarker.SOURCE_ID},
			new Object[] {message, new Integer(severity), new Integer(start), new Integer(end), JavaBuilder.SOURCE_ID});
	} catch (CoreException e) {
		throw internalException(e);
	}
}

protected void deleteGeneratedFiles(IFile[] deletedGeneratedFiles) {
	// no op by default
}

protected SourceFile findSourceFile(IFile file, boolean mustExist) {
	if (mustExist && !file.exists()) return null;

	// assumes the file exists in at least one of the source folders & is not excluded
	ClasspathMultiDirectory md = this.sourceLocations[0];
	if (this.sourceLocations.length > 1) {
		IPath sourceFileFullPath = file.getFullPath();
		for (int j = 0, m = this.sourceLocations.length; j < m; j++) {
			if (this.sourceLocations[j].sourceFolder.getFullPath().isPrefixOf(sourceFileFullPath)) {
				md = this.sourceLocations[j];
				if (md.exclusionPatterns == null && md.inclusionPatterns == null)
					break;
				if (!Util.isExcluded(file, md.inclusionPatterns, md.exclusionPatterns))
					break;
			}
		}
	}
	return new SourceFile(file, md);
}

protected void finishedWith(String sourceLocator, CompilationResult result, char[] mainTypeName, ArrayList definedTypeNames, ArrayList duplicateTypeNames) {
	if (duplicateTypeNames == null) {
		this.newState.record(sourceLocator, result.qualifiedReferences, result.simpleNameReferences, result.rootReferences, mainTypeName, definedTypeNames);
		return;
	}

	char[][] simpleRefs = result.simpleNameReferences;
	// for each duplicate type p1.p2.A, add the type name A (package was already added)
	next : for (int i = 0, l = duplicateTypeNames.size(); i < l; i++) {
		char[][] compoundName = (char[][]) duplicateTypeNames.get(i);
		char[] typeName = compoundName[compoundName.length - 1];
		int sLength = simpleRefs.length;
		for (int j = 0; j < sLength; j++)
			if (CharOperation.equals(simpleRefs[j], typeName))
				continue next;
		System.arraycopy(simpleRefs, 0, simpleRefs = new char[sLength + 1][], 0, sLength);
		simpleRefs[sLength] = typeName;
	}
	this.newState.record(sourceLocator, result.qualifiedReferences, simpleRefs, result.rootReferences, mainTypeName, definedTypeNames);
}

protected IContainer createFolder(IPath packagePath, IContainer outputFolder) throws CoreException {
	if (packagePath.isEmpty()) return outputFolder;
	IFolder folder = outputFolder.getFolder(packagePath);
	if (!folder.exists()) {
		createFolder(packagePath.removeLastSegments(1), outputFolder);
		folder.create(IResource.FORCE | IResource.DERIVED, true, null);
	}
	return folder;
}



/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.core.builder.ICompilationUnitLocator#fromIFile(org.eclipse.core.resources.IFile)
 */
public ICompilationUnit fromIFile(IFile file) {
	return findSourceFile(file, true);
}

protected void initializeAnnotationProcessorManager(Compiler newCompiler) {
	AbstractAnnotationProcessorManager annotationManager = JavaModelManager.getJavaModelManager().createAnnotationProcessorManager();
	if (annotationManager != null) {
		annotationManager.configureFromPlatform(newCompiler, this, this.javaBuilder.javaProject);
		annotationManager.setErr(new PrintWriter(System.err));
		annotationManager.setOut(new PrintWriter(System.out));
	}
	newCompiler.annotationProcessorManager = annotationManager;
}

protected RuntimeException internalException(CoreException t) {
	ImageBuilderInternalException imageBuilderException = new ImageBuilderInternalException(t);
	if (this.inCompiler)
		return new AbortCompilation(true, imageBuilderException);
	return imageBuilderException;
}

protected boolean isExcludedFromProject(IPath childPath) throws JavaModelException {
	// answer whether the folder should be ignored when walking the project as a source folder
	if (childPath.segmentCount() > 2) return false; // is a subfolder of a package

	for (int j = 0, k = this.sourceLocations.length; j < k; j++) {
		if (childPath.equals(this.sourceLocations[j].binaryFolder.getFullPath())) return true;
		if (childPath.equals(this.sourceLocations[j].sourceFolder.getFullPath())) return true;
	}
	// skip default output folder which may not be used by any source folder
	return childPath.equals(this.javaBuilder.javaProject.getOutputLocation());
}

protected Compiler newCompiler() {
	// disable entire javadoc support if not interested in diagnostics
	Map projectOptions = this.javaBuilder.javaProject.getOptions(true);
	String option = (String) projectOptions.get(JavaCore.COMPILER_PB_INVALID_JAVADOC);
	if (option == null || option.equals(JavaCore.IGNORE)) { // TODO (frederic) see why option is null sometimes while running model tests!?
		option = (String) projectOptions.get(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS);
		if (option == null || option.equals(JavaCore.IGNORE)) {
			option = (String) projectOptions.get(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS);
			if (option == null || option.equals(JavaCore.IGNORE)) {
				option = (String) projectOptions.get(JavaCore.COMPILER_PB_UNUSED_IMPORT);
				if (option == null || option.equals(JavaCore.IGNORE)) { // Unused import need also to look inside javadoc comment
					projectOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
				}
			}
		}
	}

	// called once when the builder is initialized... can override if needed
	CompilerOptions compilerOptions = new CompilerOptions(projectOptions);
	compilerOptions.performMethodsFullRecovery = true;
	compilerOptions.performStatementsRecovery = true;
	Compiler newCompiler = new Compiler(
		this.nameEnvironment,
		DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		compilerOptions,
		this,
		ProblemFactory.getProblemFactory(Locale.getDefault()));
	CompilerOptions options = newCompiler.options;
	// temporary code to allow the compiler to revert to a single thread
	String setting = System.getProperty("jdt.compiler.useSingleThread"); //$NON-NLS-1$
	newCompiler.useSingleThread = setting != null && setting.equals("true"); //$NON-NLS-1$

	// enable the compiler reference info support
	options.produceReferenceInfo = true;

	if (options.complianceLevel >= ClassFileConstants.JDK1_6
			&& options.processAnnotations) {
		// support for Java 6 annotation processors
		initializeAnnotationProcessorManager(newCompiler);
	}

	return newCompiler;
}

protected CompilationParticipantResult[] notifyParticipants(SourceFile[] unitsAboutToCompile) {
	CompilationParticipantResult[] results = new CompilationParticipantResult[unitsAboutToCompile.length];
	for (int i = unitsAboutToCompile.length; --i >= 0;)
		results[i] = new CompilationParticipantResult(unitsAboutToCompile[i]);

	// TODO (kent) do we expect to have more than one participant?
	// and if so should we pass the generated files from the each processor to the others to process?
	// and what happens if some participants do not expect to be called with only a few files, after seeing 'all' the files?
	for (int i = 0, l = this.javaBuilder.participants.length; i < l; i++)
		this.javaBuilder.participants[i].buildStarting(results, this instanceof BatchImageBuilder);

	SimpleSet uniqueFiles = null;
	CompilationParticipantResult[] toAdd = null;
	int added = 0;
	for (int i = results.length; --i >= 0;) {
		CompilationParticipantResult result = results[i];
		if (result == null) continue;

		IFile[] deletedGeneratedFiles = result.deletedFiles;
		if (deletedGeneratedFiles != null)
			deleteGeneratedFiles(deletedGeneratedFiles);

		IFile[] addedGeneratedFiles = result.addedFiles;
		if (addedGeneratedFiles != null) {
			for (int j = addedGeneratedFiles.length; --j >= 0;) {
				SourceFile sourceFile = findSourceFile(addedGeneratedFiles[j], true);
				if (sourceFile == null) continue;
				if (uniqueFiles == null) {
					uniqueFiles = new SimpleSet(unitsAboutToCompile.length + 3);
					for (int f = unitsAboutToCompile.length; --f >= 0;)
						uniqueFiles.add(unitsAboutToCompile[f]);
				}
				if (uniqueFiles.addIfNotIncluded(sourceFile) == sourceFile) {
					CompilationParticipantResult newResult = new CompilationParticipantResult(sourceFile);
					// is there enough room to add all the addedGeneratedFiles.length ?
					if (toAdd == null) {
						toAdd = new CompilationParticipantResult[addedGeneratedFiles.length];
					} else {
						int length = toAdd.length;
						if (added == length)
							System.arraycopy(toAdd, 0, toAdd = new CompilationParticipantResult[length + addedGeneratedFiles.length], 0, length);
					}
					toAdd[added++] = newResult;
				}
			}
		}
	}

	if (added >0 ) {
		int length = results.length;
		System.arraycopy(results, 0, results = new CompilationParticipantResult[length + added], 0 , length);
		System.arraycopy(toAdd, 0, results, length, added);
	}
	return results;
}

protected abstract void processAnnotationResults(CompilationParticipantResult[] results);

protected void processAnnotations(CompilationParticipantResult[] results) {
	boolean hasAnnotationProcessor = false;
	for (int i = 0, l = this.javaBuilder.participants.length; !hasAnnotationProcessor && i < l; i++)
		hasAnnotationProcessor = this.javaBuilder.participants[i].isAnnotationProcessor();
	if (!hasAnnotationProcessor) return;

	boolean foundAnnotations = this.filesWithAnnotations != null && this.filesWithAnnotations.elementSize > 0;
	for (int i = results.length; --i >= 0;)
		results[i].reset(foundAnnotations && this.filesWithAnnotations.includes(results[i].sourceFile));

	// even if no files have annotations, must still tell every annotation processor in case the file used to have them
	for (int i = 0, l = this.javaBuilder.participants.length; i < l; i++)
		if (this.javaBuilder.participants[i].isAnnotationProcessor())
			this.javaBuilder.participants[i].processAnnotations(results);
	processAnnotationResults(results);
}

protected void recordParticipantResult(CompilationParticipantResult result) {
	// any added/changed/deleted generated files have already been taken care
	// just record the problems and dependencies - do not expect there to be many
	// must be called after we're finished with the compilation unit results but before incremental loop adds affected files
	CategorizedProblem[] problems = result.problems;
	if (problems != null && problems.length > 0) {
		// existing problems have already been removed so just add these as new problems
		this.notifier.updateProblemCounts(problems);
		try {
			storeProblemsFor(result.sourceFile, problems);
		} catch (CoreException e) {
			// must continue with compile loop so just log the CoreException
			Util.log(e, "JavaBuilder logging CompilationParticipant's CoreException to help debugging"); //$NON-NLS-1$
		}
	}

	String[] dependencies = result.dependencies;
	if (dependencies != null) {
		ReferenceCollection refs = (ReferenceCollection) this.newState.references.get(result.sourceFile.typeLocator());
		if (refs != null)
			refs.addDependencies(dependencies);
	}
}

/**
 * Creates a marker from each problem and adds it to the resource.
 * The marker is as follows:
 *   - its type is T_PROBLEM
 *   - its plugin ID is the JavaBuilder's plugin ID
 *	 - its message is the problem's message
 *	 - its priority reflects the severity of the problem
 *	 - its range is the problem's range
 *	 - it has an extra attribute "ID" which holds the problem's id
 *   - it's {@link IMarker#SOURCE_ID} attribute is positioned to {@link JavaBuilder#SOURCE_ID} if
 *     the problem was generated by JDT; else the {@link IMarker#SOURCE_ID} attribute is
 *     carried from the problem to the marker in extra attributes, if present.
 */
protected void storeProblemsFor(SourceFile sourceFile, CategorizedProblem[] problems) throws CoreException {
	if (sourceFile == null || problems == null || problems.length == 0) return;
	 // once a classpath error is found, ignore all other problems for this project so the user can see the main error
	// but still try to compile as many source files as possible to help the case when the base libraries are in source
	if (!this.keepStoringProblemMarkers) return; // only want the one error recorded on this source file

	HashSet managedMarkerTypes = JavaModelManager.getJavaModelManager().compilationParticipants.managedMarkerTypes();
	problems: for (int i = 0, l = problems.length; i < l; i++) {
		CategorizedProblem problem = problems[i];
		int id = problem.getID();
		// we may use a different resource for certain problems such as IProblem.MissingNonNullByDefaultAnnotationOnPackage
		// but at the start of the next problem we should reset it to the source file's resource
		IResource resource = sourceFile.resource;
		
		// handle missing classfile situation
		if (id == IProblem.IsClassPathCorrect) {
			String missingClassfileName = problem.getArguments()[0];
			if (JavaBuilder.DEBUG)
				System.out.println(Messages.bind(Messages.build_incompleteClassPath, missingClassfileName));
			boolean isInvalidClasspathError = JavaCore.ERROR.equals(this.javaBuilder.javaProject.getOption(JavaCore.CORE_INCOMPLETE_CLASSPATH, true));
			// insert extra classpath problem, and make it the only problem for this project (optional)
			if (isInvalidClasspathError && JavaCore.ABORT.equals(this.javaBuilder.javaProject.getOption(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, true))) {
				JavaBuilder.removeProblemsAndTasksFor(this.javaBuilder.currentProject); // make this the only problem for this project
				this.keepStoringProblemMarkers = false;
			}
			IMarker marker = this.javaBuilder.currentProject.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] {IMarker.MESSAGE, IMarker.SEVERITY, IJavaModelMarker.CATEGORY_ID, IMarker.SOURCE_ID},
				new Object[] {
					Messages.bind(Messages.build_incompleteClassPath, missingClassfileName),
					new Integer(isInvalidClasspathError ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING),
					new Integer(CategorizedProblem.CAT_BUILDPATH),
					JavaBuilder.SOURCE_ID
				}
			);
			// even if we're not keeping more markers, still fall through rest of the problem reporting, so that offending
			// IsClassPathCorrect problem gets recorded since it may help locate the offending reference
		}

		String markerType = problem.getMarkerType();
		boolean managedProblem = false;
		if (IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER.equals(markerType)
				|| (managedProblem = managedMarkerTypes.contains(markerType))) {
			if (id == IProblem.MissingNonNullByDefaultAnnotationOnPackage && !(CharOperation.equals(sourceFile.getMainTypeName(), TypeConstants.PACKAGE_INFO_NAME))) {
				// for this kind of problem, marker needs to be created on the package instead of on the source file
				// see bug 372012
				char[] fileName = sourceFile.getFileName();
				int pkgEnd = CharOperation.lastIndexOf('/', fileName);
				if (pkgEnd == -1)
					pkgEnd = CharOperation.lastIndexOf(File.separatorChar, fileName);
				PackageFragment pkg = null;
				if (pkgEnd != -1)
					pkg = (PackageFragment) Util.getPackageFragment(sourceFile.getFileName(), pkgEnd, -1 /*no jar separator for java files*/);
				
				if (pkg != null) {
					try {
						IMarker[] existingMarkers = pkg.resource().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
						int len = existingMarkers.length;
						for (int j=0; j < len; j++) {
							if (((Integer)existingMarkers[j].getAttribute(IJavaModelMarker.ID)).intValue() == IProblem.MissingNonNullByDefaultAnnotationOnPackage) {
								continue problems; // marker already present
							}
						}
					} catch (CoreException e) {
						// marker retrieval failed, cannot do much
						if (JavaModelManager.VERBOSE) {
							e.printStackTrace();
						}
					}
					IResource tempRes = pkg.resource();
					if (tempRes != null) {
						resource = tempRes;
					}
				}
			}
			IMarker marker = resource.createMarker(markerType);

			String[] attributeNames = JAVA_PROBLEM_MARKER_ATTRIBUTE_NAMES;
			int standardLength = attributeNames.length;
			String[] allNames = attributeNames;
			int managedLength = managedProblem ? 0 : 1;
			String[] extraAttributeNames = problem.getExtraMarkerAttributeNames();
			int extraLength = extraAttributeNames == null ? 0 : extraAttributeNames.length;
			if (managedLength > 0 || extraLength > 0) {
				allNames = new String[standardLength + managedLength + extraLength];
				System.arraycopy(attributeNames, 0, allNames, 0, standardLength);
				if (managedLength > 0)
					allNames[standardLength] = IMarker.SOURCE_ID;
				System.arraycopy(extraAttributeNames, 0, allNames, standardLength + managedLength, extraLength);
			}

			Object[] allValues = new Object[allNames.length];
			// standard attributes
			int index = 0;
			allValues[index++] = problem.getMessage(); // message
			allValues[index++] = problem.isError() ? S_ERROR : S_WARNING; // severity
			allValues[index++] = new Integer(id); // ID
			allValues[index++] = new Integer(problem.getSourceStart()); // start
			allValues[index++] = new Integer(problem.getSourceEnd() + 1); // end
			allValues[index++] = new Integer(problem.getSourceLineNumber()); // line
			allValues[index++] = Util.getProblemArgumentsForMarker(problem.getArguments()); // arguments
			allValues[index++] = new Integer(problem.getCategoryID()); // category ID
			// SOURCE_ID attribute for JDT problems
			if (managedLength > 0)
				allValues[index++] = JavaBuilder.SOURCE_ID;
			// optional extra attributes
			if (extraLength > 0)
				System.arraycopy(problem.getExtraMarkerAttributeValues(), 0, allValues, index, extraLength);

			marker.setAttributes(allNames, allValues);

			if (!this.keepStoringProblemMarkers) return; // only want the one error recorded on this source file
		}
	}
}

protected void storeTasksFor(SourceFile sourceFile, CategorizedProblem[] tasks) throws CoreException {
	if (sourceFile == null || tasks == null || tasks.length == 0) return;

	IResource resource = sourceFile.resource;
	for (int i = 0, l = tasks.length; i < l; i++) {
		CategorizedProblem task = tasks[i];
		if (task.getID() == IProblem.Task) {
			IMarker marker = resource.createMarker(IJavaModelMarker.TASK_MARKER);
			Integer priority = P_NORMAL;
			String compilerPriority = task.getArguments()[2];
			if (JavaCore.COMPILER_TASK_PRIORITY_HIGH.equals(compilerPriority))
				priority = P_HIGH;
			else if (JavaCore.COMPILER_TASK_PRIORITY_LOW.equals(compilerPriority))
				priority = P_LOW;

			String[] attributeNames = JAVA_TASK_MARKER_ATTRIBUTE_NAMES;
			int standardLength = attributeNames.length;
			String[] allNames = attributeNames;
			String[] extraAttributeNames = task.getExtraMarkerAttributeNames();
			int extraLength = extraAttributeNames == null ? 0 : extraAttributeNames.length;
			if (extraLength > 0) {
				allNames = new String[standardLength + extraLength];
				System.arraycopy(attributeNames, 0, allNames, 0, standardLength);
				System.arraycopy(extraAttributeNames, 0, allNames, standardLength, extraLength);
			}

			Object[] allValues = new Object[allNames.length];
			// standard attributes
			int index = 0;
			allValues[index++] = task.getMessage();
			allValues[index++] = priority;
			allValues[index++] = new Integer(task.getID());
			allValues[index++] = new Integer(task.getSourceStart());
			allValues[index++] = new Integer(task.getSourceEnd() + 1);
			allValues[index++] = new Integer(task.getSourceLineNumber());
			allValues[index++] = Boolean.FALSE;
			allValues[index++] = JavaBuilder.SOURCE_ID;
			// optional extra attributes
			if (extraLength > 0)
				System.arraycopy(task.getExtraMarkerAttributeValues(), 0, allValues, index, extraLength);

			marker.setAttributes(allNames, allValues);
		}
	}
}

protected void updateProblemsFor(SourceFile sourceFile, CompilationResult result) throws CoreException {
	CategorizedProblem[] problems = result.getProblems();
	if (problems == null || problems.length == 0) return;

	this.notifier.updateProblemCounts(problems);
	storeProblemsFor(sourceFile, problems);
}

protected void updateTasksFor(SourceFile sourceFile, CompilationResult result) throws CoreException {
	CategorizedProblem[] tasks = result.getTasks();
	if (tasks == null || tasks.length == 0) return;

	storeTasksFor(sourceFile, tasks);
}

protected char[] writeClassFile(ClassFile classFile, SourceFile compilationUnit, boolean isTopLevelType) throws CoreException {
	String fileName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
	IPath filePath = new Path(fileName);
	IContainer outputFolder = compilationUnit.sourceLocation.binaryFolder;
	IContainer container = outputFolder;
	if (filePath.segmentCount() > 1) {
		container = createFolder(filePath.removeLastSegments(1), outputFolder);
		filePath = new Path(filePath.lastSegment());
	}

	IFile file = container.getFile(filePath.addFileExtension(SuffixConstants.EXTENSION_class));
	writeClassFileContents(classFile, file, fileName, isTopLevelType, compilationUnit);
	// answer the name of the class file as in Y or Y$M
	return filePath.lastSegment().toCharArray();
}

protected void writeClassFileContents(ClassFile classFile, IFile file, String qualifiedFileName, boolean isTopLevelType, SourceFile compilationUnit) throws CoreException {
//	InputStream input = new SequenceInputStream(
//			new ByteArrayInputStream(classFile.header, 0, classFile.headerOffset),
//			new ByteArrayInputStream(classFile.contents, 0, classFile.contentsOffset));
	InputStream input = new ByteArrayInputStream(classFile.getBytes());
	if (file.exists()) {
		// Deal with shared output folders... last one wins... no collision cases detected
		if (JavaBuilder.DEBUG)
			System.out.println("Writing changed class file " + file.getName());//$NON-NLS-1$
		if (!file.isDerived())
			file.setDerived(true, null);
		file.setContents(input, true, false, null);
	} else {
		// Default implementation just writes out the bytes for the new class file...
		if (JavaBuilder.DEBUG)
			System.out.println("Writing new class file " + file.getName());//$NON-NLS-1$
		file.create(input, IResource.FORCE | IResource.DERIVED, null);
	}
}
}
