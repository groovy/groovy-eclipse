package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathMultiReleaseJar extends ClasspathJar {
	private static final int VERSIONS_PREFIX_LENGTH = Util.METAINF_VERSIONS.length();

	/** Applicable META-INF/versions directory names, sorted in descending order. */
	private List<String> releaseVersions;
	private final int releaseVersion;
	/** Whether the manifest declares this JAR as multi-release, <code>null</code> until read from the archive. */
	private Boolean multiRelease;

	public ClasspathMultiReleaseJar(File file, boolean closeZipFileAtEnd,
			AccessRuleSet accessRuleSet, String destinationPath, String compliance) {
		super(file, closeZipFileAtEnd, accessRuleSet, destinationPath);
		this.releaseVersions = Collections.emptyList();
		int version;
		try {
			version = Integer.parseInt(compliance);
		} catch (NumberFormatException e) {
			version = 0;
		}
		this.releaseVersion = version;
	}

	/**
	 * Populates the package cache and applicable release versions with one pass over the JAR.
	 * JEP 238 requires class lookup to start at the requested release, continue through lower
	 * versioned directories down to 9, and finally fall back to the JAR root.
	 *
	 * @see <a href="https://openjdk.org/jeps/238">JEP 238: Multi-Release JAR Files</a>
	 * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/jar/jar.html#multi-release-jar-files">
	 *      JAR File Specification: Multi-release JAR files</a>
	 */
	private void initializeMultiReleaseIndex() {
		this.packageCache = new HashSet<>(41);
		this.packageCache.add(Util.EMPTY_STRING);
		Set<Integer> versions = new HashSet<>();
		for (Enumeration<? extends ZipEntry> entries = this.zipFile.entries(); entries.hasMoreElements();) {
			String entryName = entries.nextElement().getName();
			int prefixLength = versionedPrefixLength(entryName);
			if (prefixLength == -1) {
				continue;
			}
			if (prefixLength > 0) {
				versions.add(Integer.valueOf(entryName.substring(VERSIONS_PREFIX_LENGTH, prefixLength - 1)));
			}
			addToPackageCache(entryName.substring(prefixLength), false);
		}
		List<Integer> sortedVersions = new ArrayList<>(versions);
		sortedVersions.sort(Comparator.reverseOrder());
		List<String> result = new ArrayList<>(sortedVersions.size());
		for (Integer version : sortedVersions) {
			result.add(version.toString());
		}
		this.releaseVersions = result;
	}

	private boolean isMultiRelease() {
		Boolean result = this.multiRelease;
		if (result == null) {
			this.multiRelease = result = Boolean.valueOf(Util.isMultiRelease(this.zipFile));
		}
		return result.booleanValue();
	}

	/**
	 * Answers the length of the <code>META-INF/versions/N/</code> prefix of an entry when <code>N</code> applies to
	 * the release, <code>0</code> when the entry is not below a version directory of a multi-release JAR, or
	 * <code>-1</code> when the entry is below a version directory which does not apply and must be ignored.
	 */
	private int versionedPrefixLength(String entryName) {
		if (!entryName.startsWith(Util.METAINF_VERSIONS) || !isMultiRelease()) {
			return 0;
		}
		int separator = entryName.indexOf('/', VERSIONS_PREFIX_LENGTH);
		if (separator == -1) {
			return -1;
		}
		String versionSegment = entryName.substring(VERSIONS_PREFIX_LENGTH, separator);
		try {
			int version = Integer.parseInt(versionSegment);
			if (version < 9 || version > this.releaseVersion) {
				return -1;
			}
			// The JAR specification requires N to match {1-9}{0-9}*, excluding signs and leading zeroes.
			if (!Integer.toString(version).equals(versionSegment)) {
				return -1;
			}
		} catch (NumberFormatException e) {
			// Ignore malformed version directories as required by the JAR specification.
			return -1;
		}
		return separator + 1;
	}

	/**
	 * The class files of a multi-release JAR are at its root and in the <code>META-INF/versions/N/</code> folders
	 * whose <code>N</code> applies to the release. Entries in other version folders are ignored.
	 */
	@Override
	protected String toClassRootRelativeName(String entryName) {
		int prefixLength = versionedPrefixLength(entryName);
		return prefixLength == -1 ? null : entryName.substring(prefixLength);
	}

	@Override
	public char[][][] findTypeNames(String qualifiedPackageName, String moduleName) {
		char[][][] typeNames = super.findTypeNames(qualifiedPackageName, moduleName);
		if (typeNames == null || this.releaseVersions.isEmpty()) {
			return typeNames;
		}
		// a type can be both at the root and in version folders, answer it once
		Set<String> simpleNames = new HashSet<>();
		List<char[][]> result = new ArrayList<>(typeNames.length);
		for (char[][] typeName : typeNames) {
			if (simpleNames.add(new String(typeName[typeName.length - 1]))) {
				result.add(typeName);
			}
		}
		return result.size() == typeNames.length ? typeNames : result.toArray(new char[result.size()][][]);
	}

	@Override
	public synchronized char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		if (this.packageCache == null) {
			initializeMultiReleaseIndex();
		}
		return singletonModuleNameIf(this.packageCache.contains(qualifiedPackageName));
	}
	@Override
	public NameEnvironmentAnswer findClass(char[] binaryFileName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
		if (!isPackage(qualifiedPackageName, moduleName)) return null; // most common case
		for (String version : this.releaseVersions) {
			try {
				String entryName = Util.METAINF_VERSIONS + version + '/' + qualifiedBinaryFileName;
				IBinaryType reader = ClassFileReader.read(this.zipFile, entryName);
				if (reader != null) {
					char[] modName = this.module == null ? null : this.module.name();
					if (reader instanceof ClassFileReader) {
						ClassFileReader classReader = (ClassFileReader) reader;
						if (classReader.moduleName == null)
							classReader.moduleName = modName;
						else
							modName = classReader.moduleName;
						}
					String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
					searchPaths:
						if (this.annotationPaths != null) {
							String qualifiedClassName = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length()-SuffixConstants.EXTENSION_CLASS.length()-1);
							for (String annotationPath : this.annotationPaths) {
								try {
									if (this.annotationZipFile == null) {
										this.annotationZipFile = ExternalAnnotationDecorator.getAnnotationZipFile(annotationPath, null);
									}
									reader = ExternalAnnotationDecorator.create(reader, annotationPath, qualifiedClassName, this.annotationZipFile);

									if (reader.getExternalAnnotationStatus() == ExternalAnnotationStatus.TYPE_IS_ANNOTATED) {
										break searchPaths;
									}
								} catch (IOException e) {
									// don't let error on annotations fail class reading
								}
							}
							// location is configured for external annotations, but no .eea found, decorate in order to answer NO_EEA_FILE:
							reader = new ExternalAnnotationDecorator(reader, null);
						}
					if (this.accessRuleSet == null)
						return new NameEnvironmentAnswer(reader, null, modName);
					return new NameEnvironmentAnswer(reader,
							this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()),
							modName);
				}
			} catch (IOException | ClassFormatException e) {
				// treat as if class file is missing
			}
		}
		return super.findClass(binaryFileName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, asBinaryOnly);
	}

	@Override
	public void reset() {
		super.reset();
		this.releaseVersions = Collections.emptyList();
		this.multiRelease = null;
	}
}
