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
	/** Applicable META-INF/versions directory names, sorted in descending order. */
	private List<String> releaseVersions;
	private final int releaseVersion;

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
		int prefixLength = Util.METAINF_VERSIONS.length();
		Set<Integer> versions = new HashSet<>();
		boolean multiRelease = Util.isMultiRelease(this.zipFile);
		for (Enumeration<? extends ZipEntry> entries = this.zipFile.entries(); entries.hasMoreElements();) {
			String fileName = entries.nextElement().getName();
			if (multiRelease && fileName.startsWith(Util.METAINF_VERSIONS)) {
				int separator = fileName.indexOf('/', prefixLength);
				if (separator == -1) {
					continue;
				}
				String versionSegment = fileName.substring(prefixLength, separator);
				try {
					int version = Integer.parseInt(versionSegment);
					if (version < 9 || version > this.releaseVersion) {
						continue;
					}
					// The JAR specification requires N to match {1-9}{0-9}*, excluding signs and leading zeroes.
					if (!Integer.toString(version).equals(versionSegment)) {
						continue;
					}
					versions.add(version);
					fileName = fileName.substring(separator + 1);
				} catch (NumberFormatException e) {
					// Ignore malformed version directories as required by the JAR specification.
					continue;
				}
			}
			addToPackageCache(fileName, false);
		}
		List<Integer> sortedVersions = new ArrayList<>(versions);
		sortedVersions.sort(Comparator.reverseOrder());
		List<String> result = new ArrayList<>(sortedVersions.size());
		for (Integer version : sortedVersions) {
			result.add(version.toString());
		}
		this.releaseVersions = result;
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
	}
}
