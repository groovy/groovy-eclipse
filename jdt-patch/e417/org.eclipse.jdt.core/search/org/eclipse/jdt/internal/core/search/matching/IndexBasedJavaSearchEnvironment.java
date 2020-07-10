/*******************************************************************************
 * Copyright (c) 2015, 2018 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeId;
import org.eclipse.jdt.internal.core.nd.java.TypeRef;
import org.eclipse.jdt.internal.core.nd.java.model.IndexBinaryType;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.nd.util.PathMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IndexBasedJavaSearchEnvironment implements INameEnvironment, SuffixConstants {

	private Map<String, ICompilationUnit> workingCopies;
	private PathMap<Integer> mapPathsToRoots = new PathMap<>();
	private IPackageFragmentRoot[] roots;
	private int sourceEntryPosition;
	private List<ClasspathLocation> unindexedEntries = new ArrayList<>();

	public IndexBasedJavaSearchEnvironment(List<IJavaProject> javaProject, org.eclipse.jdt.core.ICompilationUnit[] copies) {
		this.workingCopies = JavaSearchNameEnvironment.getWorkingCopyMap(copies);

		try {
			List<IPackageFragmentRoot> localRoots = new ArrayList<>();

			for (IJavaProject next : javaProject) {
				for (IPackageFragmentRoot nextRoot : next.getAllPackageFragmentRoots()) {
					IPath path = nextRoot.getPath();
					if (!nextRoot.isArchive()) {
						Object target = JavaModel.getTarget(path, true);
						if (target != null) {
							ClasspathLocation cp;
							if (nextRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
								PackageFragmentRoot root = (PackageFragmentRoot)nextRoot;
								cp = new ClasspathSourceDirectory((IContainer)target, root.fullExclusionPatternChars(), root.fullInclusionPatternChars());
								this.unindexedEntries.add(cp);
							}
						}
					}

					localRoots.add(nextRoot);
				}
			}

			this.roots = localRoots.toArray(new IPackageFragmentRoot[0]);
		} catch (JavaModelException e) {
			this.roots = new IPackageFragmentRoot[0];
			// project doesn't exist
		}

		// Build the map of paths onto root indices
		int length = this.roots.length;
		for (int i = 0; i < length; i++) {
			IPath nextPath = JavaIndex.getLocationForElement(this.roots[i]);
			this.mapPathsToRoots.put(nextPath, i);
		}

		// Locate the position of the first source entry
		this.sourceEntryPosition = Integer.MAX_VALUE;
		for (int i = 0; i < length; i++) {
			IPackageFragmentRoot nextRoot = this.roots[i];
			try {
				if (nextRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
					this.sourceEntryPosition = i;
					break;
				}
			} catch (JavaModelException e) {
				// project doesn't exist
			}
		}
	}

	public static boolean isEnabled() {
		return Platform.getPreferencesService().getBoolean(JavaCore.PLUGIN_ID, "useIndexBasedSearchEnvironment", false, //$NON-NLS-1$
				null);
	}

	@Override
	public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		char[] binaryName = CharOperation.concatWith(compoundTypeName, '/');

		int bestEntryPosition = Integer.MAX_VALUE;
		NameEnvironmentAnswer result = findClassInUnindexedLocations(new String(binaryName), compoundTypeName[compoundTypeName.length - 1]);
		if (result != null) {
			bestEntryPosition = this.sourceEntryPosition;
		}

		char[] fieldDescriptor = JavaNames.binaryNameToFieldDescriptor(binaryName);
		JavaIndex index = JavaIndex.getIndex();
		Nd nd = index.getNd();
		try (IReader lock = nd.acquireReadLock()) {
			NdTypeId typeId = index.findType(fieldDescriptor);

			if (typeId != null) {
				List<NdType> types = typeId.getTypes();
				for (NdType next : types) {
					NdResourceFile resource = next.getFile();

					IPath path = resource.getPath();
					Integer nextRoot = this.mapPathsToRoots.getMostSpecific(path);
					if (nextRoot != null) {
						IPackageFragmentRoot root = this.roots[nextRoot];

						ClasspathEntry classpathEntry = (ClasspathEntry)root.getRawClasspathEntry();
						AccessRuleSet ruleSet = classpathEntry.getAccessRuleSet();
						AccessRestriction accessRestriction = ruleSet == null? null : ruleSet.getViolatedRestriction(binaryName);
						TypeRef typeRef = TypeRef.create(next);
						String fileName = new String(binaryName) + ".class"; //$NON-NLS-1$
						IBinaryType binaryType = new IndexBinaryType(typeRef, fileName.toCharArray());
						NameEnvironmentAnswer nextAnswer = new NameEnvironmentAnswer(binaryType, accessRestriction);

						boolean useNewAnswer = isBetter(result, bestEntryPosition, nextAnswer, nextRoot);

						if (useNewAnswer) {
							bestEntryPosition = nextRoot;
							result = nextAnswer;
						}
					}
				}
			}
		} catch (JavaModelException e) {
			// project doesn't exist
		}

		return result;
	}

	/**
	 * Search unindexed locations on the classpath for the given class
	 */
	private NameEnvironmentAnswer findClassInUnindexedLocations(String qualifiedTypeName, char[] typeName) {
		String
			binaryFileName = null, qBinaryFileName = null,
			sourceFileName = null, qSourceFileName = null,
			qPackageName = null;
		NameEnvironmentAnswer suggestedAnswer = null;
		Iterator <ClasspathLocation> iter = this.unindexedEntries.iterator();
		while (iter.hasNext()) {
			ClasspathLocation location = iter.next();
			NameEnvironmentAnswer answer;
			if (location instanceof ClasspathSourceDirectory) {
				if (sourceFileName == null) {
					qSourceFileName = qualifiedTypeName; // doesn't include the file extension
					sourceFileName = qSourceFileName;
					qPackageName =  ""; //$NON-NLS-1$
					if (qualifiedTypeName.length() > typeName.length) {
						int typeNameStart = qSourceFileName.length() - typeName.length;
						qPackageName =  qSourceFileName.substring(0, typeNameStart - 1);
						sourceFileName = qSourceFileName.substring(typeNameStart);
					}
				}
				org.eclipse.jdt.internal.compiler.env.ICompilationUnit workingCopy = (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) this.workingCopies.get(qualifiedTypeName);
				if (workingCopy != null) {
					answer = new NameEnvironmentAnswer(workingCopy, null /*no access restriction*/);
				} else {
					answer = location.findClass(
						sourceFileName, // doesn't include the file extension
						qPackageName,
						null, // TODO(SHMOD): don't have a module name, but while looking in unindexed classpath locations, this is probably OK
						qSourceFileName,  // doesn't include the file extension
						false,
						null /*no module filtering on source dir*/);
				}
			} else {
				if (binaryFileName == null) {
					qBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
					binaryFileName = qBinaryFileName;
					qPackageName =  ""; //$NON-NLS-1$
					if (qualifiedTypeName.length() > typeName.length) {
						int typeNameStart = qBinaryFileName.length() - typeName.length - 6; // size of ".class"
						qPackageName =  qBinaryFileName.substring(0, typeNameStart - 1);
						binaryFileName = qBinaryFileName.substring(typeNameStart);
					}
				}
				answer =
					location.findClass(
						binaryFileName,
						qPackageName,
						null,  // TODO(SHMOD): don't have a module name, but while looking in unindexed classpath locations, this is probably OK
						qBinaryFileName,
						false,
						null /*no module filtering, this env is not module aware*/);
			}
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggestedAnswer))
						return answer;
				} else if (answer.isBetter(suggestedAnswer))
					// remember suggestion and keep looking
					suggestedAnswer = answer;
			}
		}
		if (suggestedAnswer != null)
			// no better answer was found
			return suggestedAnswer;
		return null;
	}

	public boolean isBetter(NameEnvironmentAnswer currentBest, int currentBestClasspathPosition,
			NameEnvironmentAnswer toTest, int toTestClasspathPosition) {
		boolean useNewAnswer = false;

		if (currentBest == null) {
			useNewAnswer = true;
		} else {
			if (toTest.isBetter(currentBest)) {
				useNewAnswer = true;
			} else {
				// If neither one is better, use the one with the earlier classpath position
				if (!currentBest.isBetter(toTest)) {
					useNewAnswer = (toTestClasspathPosition < currentBestClasspathPosition);
				}
			}
		}
		return useNewAnswer;
	}

	@Override
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		char[][] newArray = new char[packageName.length + 1][];
		System.arraycopy(packageName, 0, newArray, 0, packageName.length);
		newArray[packageName.length] = typeName;
		return findType(newArray);
	}

	@Override
	public boolean isPackage(char[][] parentPackageName, char[] packageName) {
		char[] binaryPackageName = CharOperation.concatWith(parentPackageName, '/');
		final char[] fieldDescriptorPrefix;

		if (parentPackageName == null || parentPackageName.length == 0) {
			fieldDescriptorPrefix = CharArrayUtils.concat(JavaNames.FIELD_DESCRIPTOR_PREFIX, packageName,
					new char[] { '/' });
		} else {
			fieldDescriptorPrefix = CharArrayUtils.concat(JavaNames.FIELD_DESCRIPTOR_PREFIX, binaryPackageName,
					new char[] { '/' }, packageName, new char[] { '/' });
		}

		// Search all the types that are a subpackage of the given package name. Return if we find any one of them on
		// the classpath of this project.
		JavaIndex index = JavaIndex.getIndex();
		Nd nd = index.getNd();
		try (IReader lock = nd.acquireReadLock()) {
			return !index.visitFieldDescriptorsStartingWith(fieldDescriptorPrefix,
					new FieldSearchIndex.Visitor<NdTypeId>() {
						@Override
						public boolean visit(NdTypeId typeId) {
							//String fd = typeId.getFieldDescriptor().getString();
							// If this is an exact match for the field descriptor prefix we're looking for then
							// this class can't be part of the package we're searching for (and, most likely, the
							// "package" we're searching for is actually a class name - not a package).
							if (typeId.getFieldDescriptor().length() <= fieldDescriptorPrefix.length + 1) {
								return true;
							}
							List<NdType> types = typeId.getTypes();
							for (NdType next : types) {
								if (next.isMember() || next.isLocal() || next.isAnonymous()) {
									continue;
								}
								NdResourceFile resource = next.getFile();

								IPath path = resource.getPath();

								if (containsPrefixOf(path)) {
									// Terminate the search -- we've found a class belonging to the package
									// we're searching for.
									return false;
								}
							}
							return true;
						}
					});
		}
	}

	boolean containsPrefixOf(IPath path) {
		return this.mapPathsToRoots.containsPrefixOf(path);
	}

	@Override
	public void cleanup() {
		// No explicit cleanup required for this class
	}

	public static INameEnvironment create(List<IJavaProject> javaProjects, org.eclipse.jdt.core.ICompilationUnit[] copies) {
		if (JavaIndex.isEnabled() && isEnabled()) {
			return new IndexBasedJavaSearchEnvironment(javaProjects, copies);
		} else {
			Iterator<IJavaProject> next = javaProjects.iterator();
			JavaSearchNameEnvironment result = new JavaSearchNameEnvironment(next.next(), copies);

			while (next.hasNext()) {
				result.addProjectClassPath((JavaProject)next.next());
			}
			return result;
		}
	}
}
