package org.eclipse.jdt.internal.core.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathMultiReleaseJar extends ClasspathJar {
	private static final String META_INF_VERSIONS = "META-INF/versions/"; //$NON-NLS-1$
	private static final int META_INF_LENGTH = META_INF_VERSIONS.length();
	String[] supportedVersions;

	ClasspathMultiReleaseJar(IFile resource, AccessRuleSet accessRuleSet, IPath externalAnnotationPath,
			boolean isOnModulePath, String compliance) {
		super(resource, accessRuleSet, externalAnnotationPath, isOnModulePath);
		this.compliance = compliance;
		initializeVersions(this);
	}

	ClasspathMultiReleaseJar(String zipFilename, long lastModified, AccessRuleSet accessRuleSet,
			IPath externalAnnotationPath, boolean isOnModulePath, String compliance) {
		super(zipFilename, lastModified, accessRuleSet, externalAnnotationPath, isOnModulePath);
		this.compliance = compliance;
		initializeVersions(this);
	}

	public ClasspathMultiReleaseJar(ZipFile zipFile, AccessRuleSet accessRuleSet, IPath externalAnnotationPath,
			boolean isOnModulePath, String compliance) {
		this(zipFile.getName(), accessRuleSet, externalAnnotationPath, isOnModulePath, compliance);
		this.zipFile = zipFile;
		this.closeZipFileAtEnd = true;
	}

	public ClasspathMultiReleaseJar(String fileName, AccessRuleSet accessRuleSet, IPath externalAnnotationPath,
			boolean isOnModulePath, String compliance) {
		this(fileName, 0, accessRuleSet, externalAnnotationPath, isOnModulePath, compliance);
		if (externalAnnotationPath != null) {
			this.externalAnnotationPath = externalAnnotationPath.toString();
		}
	}

	@Override
	IModule initializeModule() {
		IModule mod = null;
		try (ZipFile file = new ZipFile(this.zipFilename)){
			ClassFileReader classfile = null;
			try {
				for (String path : this.supportedVersions) {
					classfile = ClassFileReader.read(file, path.toString() + '/' + IModule.MODULE_INFO_CLASS);
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

	private static synchronized void initializeVersions(ClasspathMultiReleaseJar jar) {
		if (jar.zipFile == null) {
			if (org.eclipse.jdt.internal.core.JavaModelManager.ZIP_ACCESS_VERBOSE) {
				System.out.println("(" + Thread.currentThread() + ") [ClasspathMultiReleaseJar.initializeVersions(String)] Creating ZipFile on " + jar.zipFilename); //$NON-NLS-1$	//$NON-NLS-2$
			}
			try {
				jar.zipFile = new ZipFile(jar.zipFilename);
			} catch (IOException e) {
				return;
			}
			jar.closeZipFileAtEnd = true;
		}
		int earliestJavaVersion = ClassFileConstants.MAJOR_VERSION_9;
		long latestJDK = CompilerOptions.versionToJdkLevel(jar.compliance);
		int latestJavaVer = (int) (latestJDK >> 16);
		List<String> versions = new ArrayList<>();
		for (int i = latestJavaVer; i >= earliestJavaVersion; i--) {
			String name = META_INF_VERSIONS+ (i - 44);
			ZipEntry entry = jar.zipFile.getEntry(name);
			if (entry != null) {
				versions.add(name);
			}
		}
		jar.supportedVersions = versions.toArray(new String[versions.size()]);
	}

	@Override
	protected String readJarContent(final SimpleSet packageSet) {
		String modInfo = null;
		for (Enumeration<? extends ZipEntry> e = this.zipFile.entries(); e.hasMoreElements(); ) {
			String fileName = ((ZipEntry) e.nextElement()).getName();
			if (fileName.startsWith(META_INF_VERSIONS) && fileName.length() > META_INF_LENGTH) {
				int i = fileName.indexOf('/', META_INF_LENGTH);
				fileName = fileName.substring(i + 1);
			} else if (fileName.startsWith("META-INF/")) //$NON-NLS-1$
				continue;
			if (modInfo == null) {
				int folderEnd = fileName.lastIndexOf('/');
				folderEnd += 1;
				String className = fileName.substring(folderEnd, fileName.length());
				if (className.equalsIgnoreCase(IModule.MODULE_INFO_CLASS)) {
					modInfo = fileName;
				}
			}
			addToPackageSet(packageSet, fileName, false);
		}
		return modInfo;
	}

	@Override
	public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String moduleName,
			String qualifiedBinaryFileName, boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
		if (!isPackage(qualifiedPackageName, moduleName)) {
			return null; // most common case
		}
		for (String path : this.supportedVersions) {
			String s = null;
			try {
				s = META_INF_VERSIONS + path + "/" + binaryFileName;  //$NON-NLS-1$
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
					if (this.externalAnnotationPath != null) {
						try {
							if (this.annotationZipFile == null) {
								this.annotationZipFile = ExternalAnnotationDecorator
										.getAnnotationZipFile(this.externalAnnotationPath, null);
							}

							reader = ExternalAnnotationDecorator.create(reader, this.externalAnnotationPath,
									fileNameWithoutExtension, this.annotationZipFile);
						} catch (IOException e) {
							// don't let error on annotations fail class reading
						}
						if (reader.getExternalAnnotationStatus() == ExternalAnnotationStatus.NOT_EEA_CONFIGURED) {
							// ensure a reader that answers NO_EEA_FILE
							reader = new ExternalAnnotationDecorator(reader, null);
						}
					}
					if (this.accessRuleSet == null) {
						return new NameEnvironmentAnswer(reader, null, modName);
					}
					return new NameEnvironmentAnswer(reader,
							this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()), modName);
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
