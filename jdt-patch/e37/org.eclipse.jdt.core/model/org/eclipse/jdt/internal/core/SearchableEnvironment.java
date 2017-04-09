/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - contribution for bug 337868 - [compiler][model] incomplete support for package-info.java when using SearchableEnvironment
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.IRestrictedAccessConstructorRequestor;
import org.eclipse.jdt.internal.core.search.IRestrictedAccessTypeRequestor;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.Util;

/**
 *	This class provides a <code>SearchableBuilderEnvironment</code> for code assist which
 *	uses the Java model as a search tool.
 */
public class SearchableEnvironment
	implements INameEnvironment, IJavaSearchConstants {

	public NameLookup nameLookup;
	protected ICompilationUnit unitToSkip;
	protected org.eclipse.jdt.core.ICompilationUnit[] workingCopies;
	protected WorkingCopyOwner owner;

	protected JavaProject project;
	protected IJavaSearchScope searchScope;

	protected boolean checkAccessRestrictions;

	/**
	 * Creates a SearchableEnvironment on the given project
	 */
	public SearchableEnvironment(JavaProject project, org.eclipse.jdt.core.ICompilationUnit[] workingCopies) throws JavaModelException {
		this.project = project;
		this.checkAccessRestrictions =
			!JavaCore.IGNORE.equals(project.getOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, true))
			|| !JavaCore.IGNORE.equals(project.getOption(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, true));
		this.workingCopies = workingCopies;
		this.nameLookup = project.newNameLookup(workingCopies);
	}

	/**
	 * Creates a SearchableEnvironment on the given project
	 */
	public SearchableEnvironment(JavaProject project, WorkingCopyOwner owner) throws JavaModelException {
		this(project, owner == null ? null : JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary WCs*/));
		this.owner = owner;
	}

	private static int convertSearchFilterToModelFilter(int searchFilter) {
		switch (searchFilter) {
			case IJavaSearchConstants.CLASS:
				return NameLookup.ACCEPT_CLASSES;
			case IJavaSearchConstants.INTERFACE:
				return NameLookup.ACCEPT_INTERFACES;
			case IJavaSearchConstants.ENUM:
				return NameLookup.ACCEPT_ENUMS;
			case IJavaSearchConstants.ANNOTATION_TYPE:
				return NameLookup.ACCEPT_ANNOTATIONS;
			case IJavaSearchConstants.CLASS_AND_ENUM:
				return NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_ENUMS;
			case IJavaSearchConstants.CLASS_AND_INTERFACE:
				return NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES;
			default:
				return NameLookup.ACCEPT_ALL;
		}
	}
	/**
	 * Returns the given type in the the given package if it exists,
	 * otherwise <code>null</code>.
	 */
	protected NameEnvironmentAnswer find(String typeName, String packageName) {
		if (packageName == null)
			packageName = IPackageFragment.DEFAULT_PACKAGE_NAME;
		if (this.owner != null) {
			String source = this.owner.findSource(typeName, packageName);
			if (source != null) {
				ICompilationUnit cu = new BasicCompilationUnit(source.toCharArray(), CharOperation.splitOn('.', packageName.toCharArray()), typeName + Util.defaultJavaExtension());
				return new NameEnvironmentAnswer(cu, null);
			}
		}
		NameLookup.Answer answer =
			this.nameLookup.findType(
				typeName,
				packageName,
				false/*exact match*/,
				NameLookup.ACCEPT_ALL,
				this.checkAccessRestrictions);
		if (answer != null) {
			// construct name env answer
			if (answer.type instanceof BinaryType) { // BinaryType
				try {
					return new NameEnvironmentAnswer((IBinaryType) ((BinaryType) answer.type).getElementInfo(), answer.restriction);
				} catch (JavaModelException npe) {
					// fall back to using owner
				}
			} else { //SourceType
				try {
					// retrieve the requested type
					SourceTypeElementInfo sourceType = (SourceTypeElementInfo)((SourceType) answer.type).getElementInfo();
					ISourceType topLevelType = sourceType;
					while (topLevelType.getEnclosingType() != null) {
						topLevelType = topLevelType.getEnclosingType();
					}
					// find all siblings (other types declared in same unit, since may be used for name resolution)
					IType[] types = sourceType.getHandle().getCompilationUnit().getTypes();
					ISourceType[] sourceTypes = new ISourceType[types.length];

					// in the resulting collection, ensure the requested type is the first one
					sourceTypes[0] = sourceType;
					int length = types.length;
					for (int i = 0, index = 1; i < length; i++) {
						ISourceType otherType =
							(ISourceType) ((JavaElement) types[i]).getElementInfo();
						if (!otherType.equals(topLevelType) && index < length) // check that the index is in bounds (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=62861)
							sourceTypes[index++] = otherType;
					}
					return new NameEnvironmentAnswer(sourceTypes, answer.restriction);
				} catch (JavaModelException jme) {
					if (jme.isDoesNotExist() && String.valueOf(TypeConstants.PACKAGE_INFO_NAME).equals(typeName)) {
						// in case of package-info.java the type doesn't exist in the model,
						// but the CU may still help in order to fetch package level annotations.
						return new NameEnvironmentAnswer((ICompilationUnit)answer.type.getParent(), answer.restriction);
					}
					// no usable answer
				}
			}
		}
		return null;
	}

	/**
	 * Find the packages that start with the given prefix.
	 * A valid prefix is a qualified name separated by periods
	 * (ex. java.util).
	 * The packages found are passed to:
	 *    ISearchRequestor.acceptPackage(char[][] packageName)
	 */
	public void findPackages(char[] prefix, ISearchRequestor requestor) {
		this.nameLookup.seekPackageFragments(
			new String(prefix),
			true,
			new SearchableEnvironmentRequestor(requestor));
	}

	/**
	 * Find the top-level types that are defined
	 * in the current environment and whose simple name matches the given name.
	 *
	 * The types found are passed to one of the following methods (if additional
	 * information is known about the types):
	 *    ISearchRequestor.acceptType(char[][] packageName, char[] typeName)
	 *    ISearchRequestor.acceptClass(char[][] packageName, char[] typeName, int modifiers)
	 *    ISearchRequestor.acceptInterface(char[][] packageName, char[] typeName, int modifiers)
	 *
	 * This method can not be used to find member types... member
	 * types are found relative to their enclosing type.
	 */
	public void findExactTypes(char[] name, final boolean findMembers, int searchFor, final ISearchRequestor storage) {

		try {
			final String excludePath;
			if (this.unitToSkip != null) {
				if (!(this.unitToSkip instanceof IJavaElement)) {
					// revert to model investigation
					findExactTypes(
						new String(name),
						storage,
						convertSearchFilterToModelFilter(searchFor));
					return;
				}
				excludePath = ((IJavaElement) this.unitToSkip).getPath().toString();
			} else {
				excludePath = null;
			}

			IProgressMonitor progressMonitor = new IProgressMonitor() {
				boolean isCanceled = false;
				public void beginTask(String n, int totalWork) {
					// implements interface method
				}
				public void done() {
					// implements interface method
				}
				public void internalWorked(double work) {
					// implements interface method
				}
				public boolean isCanceled() {
					return this.isCanceled;
				}
				public void setCanceled(boolean value) {
					this.isCanceled = value;
				}
				public void setTaskName(String n) {
					// implements interface method
				}
				public void subTask(String n) {
					// implements interface method
				}
				public void worked(int work) {
					// implements interface method
				}
			};
			IRestrictedAccessTypeRequestor typeRequestor = new IRestrictedAccessTypeRequestor() {
				public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path, AccessRestriction access) {
					if (excludePath != null && excludePath.equals(path))
						return;
					if (!findMembers && enclosingTypeNames != null && enclosingTypeNames.length > 0)
						return; // accept only top level types
					storage.acceptType(packageName, simpleTypeName, enclosingTypeNames, modifiers, access);
				}
			};
			try {
				new BasicSearchEngine(this.workingCopies).searchAllTypeNames(
					null,
					SearchPattern.R_EXACT_MATCH,
					name,
					SearchPattern.R_EXACT_MATCH,
					searchFor,
					getSearchScope(),
					typeRequestor,
					CANCEL_IF_NOT_READY_TO_SEARCH,
					progressMonitor);
			} catch (OperationCanceledException e) {
				findExactTypes(
					new String(name),
					storage,
					convertSearchFilterToModelFilter(searchFor));
			}
		} catch (JavaModelException e) {
			findExactTypes(
				new String(name),
				storage,
				convertSearchFilterToModelFilter(searchFor));
		}
	}

	/**
	 * Returns all types whose simple name matches with the given <code>name</code>.
	 */
	private void findExactTypes(String name, ISearchRequestor storage, int type) {
		SearchableEnvironmentRequestor requestor =
			new SearchableEnvironmentRequestor(storage, this.unitToSkip, this.project, this.nameLookup);
		this.nameLookup.seekTypes(name, null, false, type, requestor);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.env.INameEnvironment#findType(char[][])
	 */
	public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		if (compoundTypeName == null) return null;

		int length = compoundTypeName.length;
		if (length <= 1) {
			if (length == 0) return null;
			return find(new String(compoundTypeName[0]), null);
		}

		int lengthM1 = length - 1;
		char[][] packageName = new char[lengthM1][];
		System.arraycopy(compoundTypeName, 0, packageName, 0, lengthM1);

		return find(
			new String(compoundTypeName[lengthM1]),
			CharOperation.toString(packageName));
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.env.INameEnvironment#findType(char[], char[][])
	 */
	public NameEnvironmentAnswer findType(char[] name, char[][] packageName) {
		if (name == null) return null;

		return find(
			new String(name),
			packageName == null || packageName.length == 0 ? null : CharOperation.toString(packageName));
	}

	/**
	 * Find the top-level types that are defined
	 * in the current environment and whose name starts with the
	 * given prefix. The prefix is a qualified name separated by periods
	 * or a simple name (ex. java.util.V or V).
	 *
	 * The types found are passed to one of the following methods (if additional
	 * information is known about the types):
	 *    ISearchRequestor.acceptType(char[][] packageName, char[] typeName)
	 *    ISearchRequestor.acceptClass(char[][] packageName, char[] typeName, int modifiers)
	 *    ISearchRequestor.acceptInterface(char[][] packageName, char[] typeName, int modifiers)
	 *
	 * This method can not be used to find member types... member
	 * types are found relative to their enclosing type.
	 */
	public void findTypes(char[] prefix, final boolean findMembers, boolean camelCaseMatch, int searchFor, final ISearchRequestor storage) {
		findTypes(prefix, findMembers, camelCaseMatch, searchFor, storage, null);
	}
	/**
	 * Must be used only by CompletionEngine.
	 * The progress monitor is used to be able to cancel completion operations
	 * 
	 * Find the top-level types that are defined
	 * in the current environment and whose name starts with the
	 * given prefix. The prefix is a qualified name separated by periods
	 * or a simple name (ex. java.util.V or V).
	 *
	 * The types found are passed to one of the following methods (if additional
	 * information is known about the types):
	 *    ISearchRequestor.acceptType(char[][] packageName, char[] typeName)
	 *    ISearchRequestor.acceptClass(char[][] packageName, char[] typeName, int modifiers)
	 *    ISearchRequestor.acceptInterface(char[][] packageName, char[] typeName, int modifiers)
	 *
	 * This method can not be used to find member types... member
	 * types are found relative to their enclosing type.
	 */
	public void findTypes(char[] prefix, final boolean findMembers, boolean camelCaseMatch, int searchFor, final ISearchRequestor storage, IProgressMonitor monitor) {
		
		/*
			if (true){
				findTypes(new String(prefix), storage, NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
				return;
			}
		*/
		try {
			final String excludePath;
			if (this.unitToSkip != null) {
				if (!(this.unitToSkip instanceof IJavaElement)) {
					// revert to model investigation
					findTypes(
						new String(prefix),
						storage,
						convertSearchFilterToModelFilter(searchFor));
					return;
				}
				excludePath = ((IJavaElement) this.unitToSkip).getPath().toString();
			} else {
				excludePath = null;
			}
			int lastDotIndex = CharOperation.lastIndexOf('.', prefix);
			char[] qualification, simpleName;
			if (lastDotIndex < 0) {
				qualification = null;
				if (camelCaseMatch) {
					simpleName = prefix;
				} else {
					simpleName = CharOperation.toLowerCase(prefix);
				}
			} else {
				qualification = CharOperation.subarray(prefix, 0, lastDotIndex);
				if (camelCaseMatch) {
					simpleName = CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length);
				} else {
					simpleName =
						CharOperation.toLowerCase(
							CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length));
				}
			}

			IProgressMonitor progressMonitor = new IProgressMonitor() {
				boolean isCanceled = false;
				public void beginTask(String name, int totalWork) {
					// implements interface method
				}
				public void done() {
					// implements interface method
				}
				public void internalWorked(double work) {
					// implements interface method
				}
				public boolean isCanceled() {
					return this.isCanceled;
				}
				public void setCanceled(boolean value) {
					this.isCanceled = value;
				}
				public void setTaskName(String name) {
					// implements interface method
				}
				public void subTask(String name) {
					// implements interface method
				}
				public void worked(int work) {
					// implements interface method
				}
			};
			IRestrictedAccessTypeRequestor typeRequestor = new IRestrictedAccessTypeRequestor() {
				public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path, AccessRestriction access) {
					if (excludePath != null && excludePath.equals(path))
						return;
					if (!findMembers && enclosingTypeNames != null && enclosingTypeNames.length > 0)
						return; // accept only top level types
					storage.acceptType(packageName, simpleTypeName, enclosingTypeNames, modifiers, access);
				}
			};
			
			int matchRule = SearchPattern.R_PREFIX_MATCH;
			if (camelCaseMatch) matchRule |= SearchPattern.R_CAMELCASE_MATCH;
			if (monitor != null) {
				IndexManager indexManager = JavaModelManager.getIndexManager();
				if (indexManager.awaitingJobsCount() == 0) {
					// indexes were already there, so perform an immediate search to avoid any index rebuilt
					new BasicSearchEngine(this.workingCopies).searchAllTypeNames(
						qualification,
						SearchPattern.R_EXACT_MATCH,
						simpleName,
						matchRule, // not case sensitive
						searchFor,
						getSearchScope(),
						typeRequestor,
						FORCE_IMMEDIATE_SEARCH,
						progressMonitor);
				} else {
					// indexes were not ready, give the indexing a chance to finish small jobs by sleeping 100ms...
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// Do nothing
					}
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					if (indexManager.awaitingJobsCount() == 0) {
						// indexes are now ready, so perform an immediate search to avoid any index rebuilt
						new BasicSearchEngine(this.workingCopies).searchAllTypeNames(
							qualification,
							SearchPattern.R_EXACT_MATCH,
							simpleName,
							matchRule, // not case sensitive
							searchFor,
							getSearchScope(),
							typeRequestor,
							FORCE_IMMEDIATE_SEARCH,
							progressMonitor);
					} else {
						// Indexes are still not ready, so look for types in the model instead of a search request
						findTypes(
							new String(prefix),
							storage,
							convertSearchFilterToModelFilter(searchFor));
					}
				}
			} else {
				try {
					new BasicSearchEngine(this.workingCopies).searchAllTypeNames(
						qualification,
						SearchPattern.R_EXACT_MATCH,
						simpleName,
						matchRule, // not case sensitive
						searchFor,
						getSearchScope(),
						typeRequestor,
						CANCEL_IF_NOT_READY_TO_SEARCH,
						progressMonitor);
				} catch (OperationCanceledException e) {
					findTypes(
						new String(prefix),
						storage,
						convertSearchFilterToModelFilter(searchFor));
				}
			}
		} catch (JavaModelException e) {
			findTypes(
				new String(prefix),
				storage,
				convertSearchFilterToModelFilter(searchFor));
		}
	}
	
	/**
	 * Must be used only by CompletionEngine.
	 * The progress monitor is used to be able to cancel completion operations
	 * 
	 * Find constructor declarations that are defined
	 * in the current environment and whose name starts with the
	 * given prefix. The prefix is a qualified name separated by periods
	 * or a simple name (ex. java.util.V or V).
	 *
	 * The constructors found are passed to one of the following methods:
	 *    ISearchRequestor.acceptConstructor(...)
	 */
	public void findConstructorDeclarations(char[] prefix, boolean camelCaseMatch, final ISearchRequestor storage, IProgressMonitor monitor) {
		try {
			final String excludePath;
			if (this.unitToSkip != null && this.unitToSkip instanceof IJavaElement) {
				excludePath = ((IJavaElement) this.unitToSkip).getPath().toString();
			} else {
				excludePath = null;
			}
			
			int lastDotIndex = CharOperation.lastIndexOf('.', prefix);
			char[] qualification, simpleName;
			if (lastDotIndex < 0) {
				qualification = null;
				if (camelCaseMatch) {
					simpleName = prefix;
				} else {
					simpleName = CharOperation.toLowerCase(prefix);
				}
			} else {
				qualification = CharOperation.subarray(prefix, 0, lastDotIndex);
				if (camelCaseMatch) {
					simpleName = CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length);
				} else {
					simpleName =
						CharOperation.toLowerCase(
							CharOperation.subarray(prefix, lastDotIndex + 1, prefix.length));
				}
			}

			IProgressMonitor progressMonitor = new IProgressMonitor() {
				boolean isCanceled = false;
				public void beginTask(String name, int totalWork) {
					// implements interface method
				}
				public void done() {
					// implements interface method
				}
				public void internalWorked(double work) {
					// implements interface method
				}
				public boolean isCanceled() {
					return this.isCanceled;
				}
				public void setCanceled(boolean value) {
					this.isCanceled = value;
				}
				public void setTaskName(String name) {
					// implements interface method
				}
				public void subTask(String name) {
					// implements interface method
				}
				public void worked(int work) {
					// implements interface method
				}
			};
			
			IRestrictedAccessConstructorRequestor constructorRequestor = new IRestrictedAccessConstructorRequestor() {
				public void acceptConstructor(
						int modifiers,
						char[] simpleTypeName,
						int parameterCount,
						char[] signature,
						char[][] parameterTypes,
						char[][] parameterNames,
						int typeModifiers,
						char[] packageName,
						int extraFlags,
						String path,
						AccessRestriction access) {
					if (excludePath != null && excludePath.equals(path))
						return;
					
					storage.acceptConstructor(
							modifiers,
							simpleTypeName,
							parameterCount,
							signature,
							parameterTypes,
							parameterNames, 
							typeModifiers,
							packageName,
							extraFlags,
							path,
							access);
				}
			};
			
			int matchRule = SearchPattern.R_PREFIX_MATCH;
			if (camelCaseMatch) matchRule |= SearchPattern.R_CAMELCASE_MATCH;
			if (monitor != null) {
				IndexManager indexManager = JavaModelManager.getIndexManager();
				while (indexManager.awaitingJobsCount() > 0) {
					try {
						Thread.sleep(50); // indexes are not ready,  sleep 50ms...
					} catch (InterruptedException e) {
						// Do nothing
					}
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
				new BasicSearchEngine(this.workingCopies).searchAllConstructorDeclarations(
						qualification,
						simpleName,
						matchRule,
						getSearchScope(),
						constructorRequestor,
						FORCE_IMMEDIATE_SEARCH,
						progressMonitor);
			} else {
				try {
					new BasicSearchEngine(this.workingCopies).searchAllConstructorDeclarations(
							qualification,
							simpleName,
							matchRule,
							getSearchScope(),
							constructorRequestor,
							CANCEL_IF_NOT_READY_TO_SEARCH,
							progressMonitor);
				} catch (OperationCanceledException e) {
					// Do nothing
				}
			}
		} catch (JavaModelException e) {
			// Do nothing
		}
	}

	/**
	 * Returns all types whose name starts with the given (qualified) <code>prefix</code>.
	 *
	 * If the <code>prefix</code> is unqualified, all types whose simple name matches
	 * the <code>prefix</code> are returned.
	 */
	private void findTypes(String prefix, ISearchRequestor storage, int type) {
		//TODO (david) should add camel case support
		SearchableEnvironmentRequestor requestor =
			new SearchableEnvironmentRequestor(storage, this.unitToSkip, this.project, this.nameLookup);
		int index = prefix.lastIndexOf('.');
		if (index == -1) {
			this.nameLookup.seekTypes(prefix, null, true, type, requestor);
		} else {
			String packageName = prefix.substring(0, index);
			JavaElementRequestor elementRequestor = new JavaElementRequestor();
			this.nameLookup.seekPackageFragments(packageName, false, elementRequestor);
			IPackageFragment[] fragments = elementRequestor.getPackageFragments();
			if (fragments != null) {
				String className = prefix.substring(index + 1);
				for (int i = 0, length = fragments.length; i < length; i++)
					if (fragments[i] != null)
						this.nameLookup.seekTypes(className, fragments[i], true, type, requestor);
			}
		}
	}

	private IJavaSearchScope getSearchScope() {
		if (this.searchScope == null) {
			// Create search scope with visible entry on the project's classpath
			if(this.checkAccessRestrictions) {
				this.searchScope = BasicSearchEngine.createJavaSearchScope(new IJavaElement[] {this.project});
			} else {
				this.searchScope = BasicSearchEngine.createJavaSearchScope(this.nameLookup.packageFragmentRoots);
			}
		}
		return this.searchScope;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.env.INameEnvironment#isPackage(char[][], char[])
	 */
	public boolean isPackage(char[][] parentPackageName, char[] subPackageName) {
		String[] pkgName;
		if (parentPackageName == null)
			pkgName = new String[] {new String(subPackageName)};
		else {
			int length = parentPackageName.length;
			pkgName = new String[length+1];
			for (int i = 0; i < length; i++)
				pkgName[i] = new String(parentPackageName[i]);
			pkgName[length] = new String(subPackageName);
		}
		return 
			(this.owner != null && this.owner.isPackage(pkgName))
			|| this.nameLookup.isPackage(pkgName);
	}

	/**
	 * Returns a printable string for the array.
	 */
	protected String toStringChar(char[] name) {
		return "["  //$NON-NLS-1$
		+ new String(name) + "]" ; //$NON-NLS-1$
	}

	/**
	 * Returns a printable string for the array.
	 */
	protected String toStringCharChar(char[][] names) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			result.append(toStringChar(names[i]));
		}
		return result.toString();
	}

	public void cleanup() {
		// nothing to do
	}
}
