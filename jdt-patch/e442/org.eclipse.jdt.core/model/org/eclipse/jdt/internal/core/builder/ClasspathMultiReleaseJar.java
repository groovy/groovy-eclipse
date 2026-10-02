package org.eclipse.jdt.internal.core.builder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathMultiReleaseJar extends ClasspathJar {
	private static final String META_INF_VERSIONS = Util.METAINF_VERSIONS;
	private static final int META_INF_LENGTH = META_INF_VERSIONS.length();
	private volatile List<String> supportedVersions;

	ClasspathMultiReleaseJar(IFile resource, AccessRuleSet accessRuleSet, IPath externalAnnotationPath,
			boolean isOnModulePath, String compliance) {
		super(resource, accessRuleSet, externalAnnotationPath, isOnModulePath);
		this.compliance = compliance;
	}

	ClasspathMultiReleaseJar(String zipFilename, long lastModified, AccessRuleSet accessRuleSet,
			IPath externalAnnotationPath, boolean isOnModulePath, String compliance) {
		super(zipFilename, lastModified, accessRuleSet, externalAnnotationPath, isOnModulePath);
		this.compliance = compliance;
	}

	public ClasspathMultiReleaseJar(ZipFile zipFile, AccessRuleSet accessRuleSet, boolean isOnModulePath, String compliance) {
		this(zipFile.getName(), 0, accessRuleSet, null, isOnModulePath, compliance);
		this.zipFile = zipFile;
	}

	@Override
	IModule initializeModule() {
		IModule mod = null;
		try (ZipFile file = new ZipFile(this.zipFilename)){
			ClassFileReader classfile = null;
			try {
				for (String version : supportedVersions(file)) {
					classfile = ClassFileReader.read(file, Util.METAINF_VERSIONS + version + '/' + IModule.MODULE_INFO_CLASS);
					if (classfile != null) {
						break;
					}
				}

			} catch (Exception e) {
				Util.log(e, "Failed to initialize module for: " + this);  //$NON-NLS-1$
				// move on to the default
			}
			if (classfile == null) {
				classfile = ClassFileReader.read(file, IModule.MODULE_INFO_CLASS); // FIXME: use jar cache
			}
			if (classfile != null) {
				mod = classfile.getModuleDeclaration();
			}
		} catch (ClassFormatException | IOException e) {
			Util.log(e, "Failed to initialize module for: " + this);  //$NON-NLS-1$
		}
		return mod;
	}

	/**
	 * Retrieve versions contained in a multi-release jar in descending order.
	 * @param projectVersion this version sets the upper limit of versions to consider
	 */
	private List<String> scanMultiReleaseVersions(ZipFile jar, String projectVersion) {
		int maxVersion = Util.parseIntOrElse(projectVersion, Integer.parseInt(JavaCore.latestSupportedJavaVersion()));
		int prefixLength = Util.METAINF_VERSIONS.length();
		List<String> versions = jar.stream()
			.map(ZipEntry::getName)
			.filter(n -> n.startsWith(Util.METAINF_VERSIONS))
			.map(name -> {
				int filterMe = maxVersion + 1; // to be filtered out at the next stage
				int delim = name.indexOf('/', prefixLength);
				if (delim == -1 )
					return filterMe; // ignore the directory entry "META-INF/versions/"
				String version = name.substring(prefixLength, delim);
				return Util.parseIntOrElse(version, filterMe);
			})
			.filter(v -> v <= maxVersion)
			.sorted(Comparator.reverseOrder())
			.distinct()
			.map(v -> Integer.toString(v))
			.collect(Collectors.toList());
		return versions;
	}

	private List<String> supportedVersions(ZipFile file) {
		List<String> versions = this.supportedVersions;
		if (versions == null) {
			versions = Util.isMultiRelease(getManifest())
						? scanMultiReleaseVersions(file, this.compliance)
						: Collections.emptyList();
			this.supportedVersions = versions;
		}
		return versions;
	}

	@Override
	protected Set<String> readPackageNames() {
		final Set<String> packageSet = new HashSet<>();
		packageSet.add(""); //$NON-NLS-1$
		for (Enumeration<? extends ZipEntry> e = this.zipFile.entries(); e.hasMoreElements(); ) {
			String fileName = e.nextElement().getName();
			if (fileName.startsWith(META_INF_VERSIONS) && fileName.length() > META_INF_LENGTH) {
				int i = fileName.indexOf('/', META_INF_LENGTH);
				fileName = fileName.substring(i + 1);
			} else if (fileName.startsWith("META-INF/")) //$NON-NLS-1$
				continue;
			addToPackageSet(packageSet, fileName, false);
		}
		return packageSet;
	}

	@Override
	public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String moduleName,
			String qualifiedBinaryFileName, boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
		if (!isPackage(qualifiedPackageName, moduleName)) {
			return null; // most common case
		}
		for (String version : supportedVersions(this.zipFile)) {
			String s = Util.METAINF_VERSIONS + version + '/' + qualifiedBinaryFileName;
			try {
				ZipEntry entry = this.zipFile.getEntry(s);
				if (entry == null)
					continue;
				IBinaryType reader = ClassFileReader.read(this.zipFile, s);
				if (reader != null) {
					char[] modName = this.module == null ? null : this.module.name();
					if (reader instanceof ClassFileReader) {
						ClassFileReader classReader = (ClassFileReader) reader;
						if (classReader.moduleName == null) {
							classReader.moduleName = modName;
						} else {
							modName = classReader.moduleName;
						}
					}
					String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0,
							qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
					return createAnswer(fileNameWithoutExtension, reader, modName);
				}
			} catch (IOException | ClassFormatException e) {
				Util.log(e, "Failed to find class for: " + s + " in: " + this);  //$NON-NLS-1$ //$NON-NLS-2$
				// treat as if class file is missing
			}
		}
		return super.findClass(binaryFileName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, asBinaryOnly,
				moduleNameFilter);
	}

}
