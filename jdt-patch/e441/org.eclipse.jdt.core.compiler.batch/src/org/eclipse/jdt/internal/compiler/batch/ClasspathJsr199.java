/*******************************************************************************
 * Copyright (c) 2015, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kenneth Olson - initial API and implementation
 *     Dennis Hendriks - initial API and implementation
 *     IBM Corporation - Contribution for bug 188796
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.tool.ModuleLocationHandler.LocationWrapper;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClasspathJsr199 extends ClasspathLocation {

	private final static String NO_PATH = ""; //$NON-NLS-1$

	private final Set<JavaFileObject.Kind> fileTypes;
	private final JavaFileManager fileManager;
	private final JavaFileManager.Location location;
	private Classpath jrt;
	private Supplier<Parser> parserSupplier;
	private String encoding;

	/**
	 * FileSystem.internalFindClass() detects request for initial files by filename,
	 * which is not suitable for JavaFileObjects with custom URI format.
	 * Thus we need to compare JavaFileObjects, too.
	 */
	private Set<JavaFileObject> initialJavaFileObjects;

	public ClasspathJsr199(JavaFileManager file, JavaFileManager.Location location) {
		super(null, null);
		this.fileManager = file;
		this.location = location;
		this.fileTypes = location == StandardLocation.SOURCE_PATH
				? Collections.singleton(JavaFileObject.Kind.SOURCE)
				: Collections.singleton(JavaFileObject.Kind.CLASS);
	}
	public ClasspathJsr199(Classpath jrt, JavaFileManager file, JavaFileManager.Location location) {
		this(file, location);
		this.jrt = jrt;
	}
	/*
	 * Maintain two separate constructors to avoid this being constructed with any other kind of classpath
	 * (other than ClasspathJrt and ClasspathJep249
	 */
	public ClasspathJsr199(ClasspathJep247 older, JavaFileManager file, JavaFileManager.Location location) {
		this(file, location);
		this.jrt = older;
	}

	/** Constructor for mapping SOURCE_PATH to ClasspathLocation. */
	public ClasspathJsr199(JavaFileManager fileManager, JavaFileManager.Location location, Set<JavaFileObject> initialJavaFileObjects, Supplier<Parser> parserSupplier) {
		this(fileManager, location);
		this.initialJavaFileObjects = initialJavaFileObjects;
		this.parserSupplier = parserSupplier;
	}

	@Override
	public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
		// Assume no linked jars
		return null;
	}

	@Override
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName,
			String aQualifiedBinaryFileName, boolean asBinaryOnly) {
		if (this.jrt != null) {
			return this.jrt.findClass(typeName, qualifiedPackageName, moduleName, aQualifiedBinaryFileName, asBinaryOnly);
		}
		String qualifiedBinaryFileName = File.separatorChar == '/'
				? aQualifiedBinaryFileName
				: aQualifiedBinaryFileName.replace(File.separatorChar, '/');

		try {
			int lastDot = qualifiedBinaryFileName.lastIndexOf('.');
			String className = lastDot < 0 ? qualifiedBinaryFileName : qualifiedBinaryFileName.substring(0, lastDot);
			JavaFileObject jfo = null;
			try {
				jfo = this.fileManager.getJavaFileForInput(this.location, className, this.fileTypes.iterator().next());
			} catch (IOException e) {
				// treat as if class file is missing
			}

			if (jfo == null)
				return null; // most common case

			char[] answerModule = this.module != null ? this.module.name() : null;
			if (jfo.getKind() == Kind.CLASS) {
				ClassFileReader reader = readJavaClass(jfo, qualifiedBinaryFileName);
				// To avoid false compiler errors "package collides with type" on case insensitive file systems
				// (e. g. Windows), make a case sensitive comparison of class name and type name from reader. The
				// reader contains the CASE SENSITIVE type name.
				return  reader != null && className.equals(new String(reader.getName()))
					? new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName), answerModule)
					: null;
			} else {
				if (this.initialJavaFileObjects != null && this.initialJavaFileObjects.contains(jfo))
					return null; // refuse to re-add an initial file (possibly via a wrong module?)
				CompilationUnit cu = readCompilationUnit(jfo, this.encoding);
				cu.module = answerModule;
				return new NameEnvironmentAnswer(cu, fetchAccessRestriction(qualifiedBinaryFileName), answerModule);
			}
		} catch (ClassFormatException e) {
			// treat as if class file is missing
		} catch (IOException e) {
			// treat as if class file is missing
		}
		return null;
	}

	@Override
	public char[][][] findTypeNames(String aQualifiedPackageName, String moduleName) {
		if (this.jrt != null) {
			return this.jrt.findTypeNames(aQualifiedPackageName, moduleName);
		}
		String qualifiedPackageName = File.separatorChar == '/' ? aQualifiedPackageName : aQualifiedPackageName.replace(
				File.separatorChar, '/');

		Iterable<JavaFileObject> files = null;
		try {
			files = this.fileManager.list(this.location, qualifiedPackageName, this.fileTypes, false);
		} catch (IOException e) {
			// treat as if empty
		}
		if (files == null) {
			return null;
		}
		ArrayList answers = new ArrayList();
		char[][] packageName = CharOperation.splitOn(File.separatorChar, qualifiedPackageName.toCharArray());

		for (JavaFileObject file : files) {
			String fileName = file.toUri().getPath();

			int last = fileName.lastIndexOf('/');
			if (last > 0) {
				int indexOfDot = fileName.lastIndexOf('.');
				if (indexOfDot != -1) {
					String typeName = fileName.substring(last + 1, indexOfDot);
					answers.add(CharOperation.arrayConcat(packageName, typeName.toCharArray()));
				}
			}
		}
		int size = answers.size();
		if (size != 0) {
			char[][][] result = new char[size][][];
			answers.toArray(result);
			return result;
		}
		return null;
	}

	@Override
	public void initialize() throws IOException {
		if (this.jrt != null) {
			this.jrt.initialize();
		} else if (this.location instanceof LocationWrapper wrapper) {
			for (Path locPath : wrapper.getPaths()) {
				File file = locPath.toFile();
				IModule mod = ModuleFinder.scanForModule(this, file, null, true, null);
				if (mod != null)
					return;
			}
		} else {
			if (this.location == StandardLocation.SOURCE_PATH) {
				for (JavaFileObject javaFileObject : this.fileManager.list(this.location, NO_PATH, Collections.singleton(JavaFileObject.Kind.SOURCE), false)) {
					if (javaFileObject.getName().equals(IModule.MODULE_INFO_JAVA)) {
						this.module = ClasspathJsr199.extractModuleFromFileObject(javaFileObject, this.parserSupplier, this, this.encoding);
						return;
					}
				}
			} else {
				for (JavaFileObject javaFileObject : this.fileManager.list(this.location, NO_PATH, Collections.singleton(JavaFileObject.Kind.CLASS), false)) {
					if (javaFileObject.getName().equals(IModule.MODULE_INFO_CLASS)) {
						this.module = ClasspathJsr199.extractModuleFromFileObject(javaFileObject, null, this, this.encoding);
						return;
					}
				}
			}
		}
	}

	@Override
	public void acceptModule(IModule mod) {
		if (this.jrt != null)
			return; // do nothing
		this.module = mod;
	}

	@Override
	public char[][] getModulesDeclaringPackage(String aQualifiedPackageName, String moduleName) {
		if (this.jrt != null) {
			return this.jrt.getModulesDeclaringPackage(aQualifiedPackageName, moduleName);
		}
		String qualifiedPackageName = File.separatorChar == '/' ? aQualifiedPackageName : aQualifiedPackageName.replace(
				File.separatorChar, '/');

		boolean result = false;
		try {
			Iterable<JavaFileObject> files = this.fileManager.list(this.location, qualifiedPackageName, this.fileTypes, false);
			Iterator f = files.iterator();
			// if there is any content, assume a package
			if (f.hasNext()) {
				result = true;
			} else {
				// I hate to do this since it is expensive and will throw off garbage
				// but can't think of an alternative now
				files = this.fileManager.list(this.location, qualifiedPackageName, this.fileTypes, true);
				f = files.iterator();
				// if there is any content, assume a package
				if (f.hasNext()) {
					result = true;
				}
			}
		} catch (IOException e) {
			// treat as if missing
		}
		return singletonModuleNameIf(result);
	}

	@Override
	public char[][] listPackages() {
		Set<String> packageNames = new HashSet<>();
		try {
			for (JavaFileObject fileObject : this.fileManager.list(this.location, NO_PATH, this.fileTypes, true)) {
				String name = fileObject.getName();
				int lastSlash = name.lastIndexOf('/');
				if (lastSlash != -1) {
					packageNames.add(name.substring(0, lastSlash).replace('/', '.'));
				}
			}
			char[][] result = new char[packageNames.size()][];
			int i = 0;
			for (String s : packageNames) {
				result[i++] = s.toCharArray();
			}
			return result;
		} catch (IOException e) {
			// treat as if empty
		}
		return CharOperation.NO_CHAR_CHAR;
	}

	@Override
	public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
		if (this.jrt != null)
			return this.jrt.hasCompilationUnit(qualifiedPackageName, moduleName);
		return false;
	}

	@Override
	public void reset() {
		try {
			super.reset();
			this.fileManager.flush();
		} catch (IOException e) {
			// ignore
		}
		if (this.jrt != null) {
			this.jrt.reset();
		}
	}

	@Override
	public String toString() {
		return "Classpath for Jsr 199 JavaFileManager: " + this.location; //$NON-NLS-1$
	}

	@Override
	public char[] normalizedPath() {
		if (this.normalizedPath == null) {
			this.normalizedPath = this.getPath().toCharArray();
		}
		return this.normalizedPath;
	}

	@Override
	public String getPath() {
		if (this.path == null) {
			this.path = this.location.getName();
		}
		return this.path;
	}

	@Override
	public int getMode() {
		return BINARY;
	}

	@Override
	public boolean hasAnnotationFileFor(String qualifiedTypeName) {
		return false;
	}

	@Override
	public Collection<String> getModuleNames(Collection<String> limitModules) {
		if (this.jrt != null)
			return this.jrt.getModuleNames(limitModules);
		if (this.module != null) {
			return Collections.singletonList(String.valueOf(this.module.name()));
		}
		return Collections.emptyList();
	}

	@Override
	public boolean hasModule() {
		if (this.jrt != null) {
			return this.jrt.hasModule();
		}
		return super.hasModule();
	}

	@Override
	public IModule getModule(char[] name) {
		if (this.jrt != null) {
			return this.jrt.getModule(name);
		}
		return super.getModule(name);
	}

	@Override
	public IModule getModule() {
		return this.module;
	}

	@Override
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName,
			String moduleName, String qualifiedBinaryFileName) {
		//
		return findClass(typeName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, false);
	}

	public static ClassFileReader readJavaClass(JavaFileObject jfo, String name) throws ClassFormatException, IOException {
		try (InputStream inputStream = jfo.openInputStream()) {
			return ClassFileReader.read(inputStream.readAllBytes(), name);
		}
	}

	public static CompilationUnit readCompilationUnit(JavaFileObject jfo, String encoding) throws IOException {
		return new CompilationUnit(jfo.getCharContent(false).toString().toCharArray(), jfo.getName(), encoding);
	}

	public static IModule extractModuleFromFileObject(JavaFileObject javaFileObject, Supplier<Parser> parserSupplier, Classpath pathEntry, String encoding) {
		try {
			switch (javaFileObject.getKind()) {
				case SOURCE:
					return extractModuleFromSource(javaFileObject, parserSupplier.get(), pathEntry, encoding);
				case CLASS:
					return extractModuleFromClass(javaFileObject, pathEntry);
				default:
					throw new IllegalArgumentException("Unexpected kind "+javaFileObject.getKind()); //$NON-NLS-1$
			}
		} catch (IOException e) {
			String error = "Failed to read module from " + pathEntry; //$NON-NLS-1$
			if (JRTUtil.PROPAGATE_IO_ERRORS) {
				throw new IllegalStateException(error, e);
			} else {
				System.err.println(error);
				e.printStackTrace();
			}
		} catch (ClassFormatException e) {
			e.printStackTrace();
		}
		return null;
	}
	static IModule extractModuleFromSource(JavaFileObject javaFileObject, Parser parser, Classpath pathEntry, String encoding) throws IOException {
		CompilationUnit cu = readCompilationUnit(javaFileObject, encoding);
		CompilationResult compilationResult = new CompilationResult(cu, 0, 1, 10);
		CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
		if (unit.isModuleInfo() && unit.moduleDeclaration != null) {
			cu.module = unit.moduleDeclaration.moduleName;
			return new BasicModule(unit.moduleDeclaration, pathEntry);
		}
		return null;
	}
	static IModule extractModuleFromClass(JavaFileObject javaFileObject, Classpath pathEntry) throws ClassFormatException, IOException{
		ClassFileReader reader = readJavaClass(javaFileObject, IModule.MODULE_INFO_CLASS);
		if (reader != null) {
			IModule module =  reader.getModuleDeclaration();
			if (module != null)
				return reader.getModuleDeclaration();
		}
		return null;
	}
}
