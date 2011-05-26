/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *     Kris De Volder - Grails 1.4 support
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.xbean.classloader.NonLockingJarFileClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation;
import org.codehaus.groovy.control.CompilationUnit.ProgressListener;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.builder.BatchImageBuilder;
import org.eclipse.jdt.internal.core.builder.BuildNotifier;
import org.eclipse.jdt.internal.core.builder.SourceFile;

/**
 * The mapping layer between the groovy parser and the JDT. This class communicates with the groovy parser and translates results
 * back for JDT to consume.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class GroovyParser {

	// LookupEnvironment lookupEnvironment;
	public ProblemReporter problemReporter;
	CompilationUnit groovyCompilationUnit;
	private JDTResolver resolver;
	private String projectName;
	private String gclClasspath;
	public static IGroovyDebugRequestor debugRequestor;
	private CompilerOptions compilerOptions;
	public Object requestor;
	private boolean allowTransforms;
	private boolean isReconcile;
	private ScriptFolderSelector scriptFolderSelector;

	/*
	 * Each project is allowed a GroovyClassLoader that will be used to load transform definitions and supporting classes. A cache
	 * is maintained from project names to the current classpath and associated loader. If the classpath matches the cached version
	 * on a call to build a parser then it is reused. If it does not match then a new loader is created and stored (storing it
	 * orphans the previously cached one). When either a full build or a clean or project close occurs, we also discard the loader
	 * instances associated with the project.
	 */

	private static Map<String, PathLoaderPair> projectToLoaderCache = Collections
			.synchronizedMap(new HashMap<String, PathLoaderPair>());

	static class PathLoaderPair {
		String classpath;
		GroovyClassLoader groovyClassLoader;

		PathLoaderPair(String classpath) {
			this.classpath = classpath;
			this.groovyClassLoader = new GroovyClassLoader(createConfigureLoader(classpath));
		}
	}

	/**
	 * Close the jar files that have been kept open by the URLClassLoader
	 */
	public static void close(GroovyClassLoader groovyClassLoader) {
		// System.out.println("Running close for GCL " + groovyClassLoader);
		try {
			Class<?> clazz = java.net.URLClassLoader.class;
			Field field_urlClasspath = clazz.getDeclaredField("ucp");
			field_urlClasspath.setAccessible(true);
			Object urlClasspath = field_urlClasspath.get(groovyClassLoader);
			Field field_loaders = urlClasspath.getClass().getDeclaredField("loaders");
			field_loaders.setAccessible(true);
			Object[] jarLoaders = ((java.util.Collection<?>) field_loaders.get(urlClasspath)).toArray();
			// int count = 0;
			for (Object jarLoader : jarLoaders) {
				try {
					Field field_jarFile = jarLoader.getClass().getDeclaredField("jar");
					field_jarFile.setAccessible(true);
					JarFile jarFile = (JarFile) field_jarFile.get(jarLoader);
					String jarFileName = jarFile.getName();
					if (jarFileName.indexOf("cache") != -1 || jarFileName.indexOf("plugins") != -1) {
						// System.out.println("Closing " + jarFile.getName());
						// count++;
						jarFile.close();
					}
				} catch (Throwable t) {
					// t.printStackTrace();
					// Probably not a JarLoader
				}
			}
			// System.out.println("Closed " + count + " jars");
		} catch (Throwable t) {
			// Not the kind of VM we thought it was...
			// t.printStackTrace();
		}
	}

	/**
	 * Remove all cached classloaders for this project
	 */
	public static void tidyCache(String projectName) {
		// This will orphan the loader on the heap
		projectToLoaderCache.remove(projectName);
	}

	public static void closeClassLoader(String projectName) {
		PathLoaderPair pathLoaderPair = projectToLoaderCache.get(projectName);
		if (pathLoaderPair != null) {
			close(pathLoaderPair.groovyClassLoader);
		}
	}

	// public GroovyParser(CompilerOptions options, ProblemReporter problemReporter) {
	// this(null, options, problemReporter);
	// }

	public GroovyParser(CompilerOptions options, ProblemReporter problemReporter, boolean allowTransforms, boolean isReconcile) {
		this(null, options, problemReporter, allowTransforms, isReconcile);
	}

	// public GroovyParser(Object requestor, CompilerOptions options, ProblemReporter problemReporter) {
	// this(requestor, options, problemReporter, true);
	// }

	private GroovyClassLoader gclForBatch = null;

	private GroovyClassLoader getLoaderFor(String path) {
		GroovyClassLoader gcl = null;
		if (projectName == null && path == null) {
			if (gclForBatch == null) {
				try {
					// Batch compilation
					if (requestor instanceof org.eclipse.jdt.internal.compiler.Compiler) {
						org.eclipse.jdt.internal.compiler.Compiler compiler = ((org.eclipse.jdt.internal.compiler.Compiler) requestor);
						LookupEnvironment lookupEnvironment = compiler.lookupEnvironment;
						if (lookupEnvironment != null) {
							INameEnvironment nameEnvironment = lookupEnvironment.nameEnvironment;
							if (nameEnvironment instanceof FileSystem) {
								FileSystem fileSystem = (FileSystem) nameEnvironment;
								if (fileSystem != null) {
									Field f = FileSystem.class.getDeclaredField("classpaths");
									if (f != null) {
										f.setAccessible(true);
										gclForBatch = new GroovyClassLoader();
										Classpath[] classpaths = (Classpath[]) f.get(fileSystem);
										if (classpaths != null) {
											for (int i = 0; i < classpaths.length; i++) {
												gclForBatch.addClasspath(classpaths[i].getPath());
											}
										}
									} else {
										System.err.println("Cannot find classpaths field on FileSystem class");
									}
								}
							}
						}
					}
				} catch (Exception e) {
					System.err.println("Unexpected problem computing classpath for ast transform loader:");
					e.printStackTrace(System.err);
				}
			}
			return gclForBatch;
		}
		if (path != null) {
			if (projectName == null) {
				// throw new IllegalStateException("Cannot build without knowing project name");
			} else {
				PathLoaderPair pathAndLoader = projectToLoaderCache.get(projectName);
				if (pathAndLoader == null) {
					if (GroovyLogManager.manager.hasLoggers()) {
						GroovyLogManager.manager.log(TraceCategory.AST_TRANSFORM,
								"Classpath for GroovyClassLoader (used to discover transforms): " + path);
					}
					pathAndLoader = new PathLoaderPair(path);
					projectToLoaderCache.put(projectName, pathAndLoader);
				} else {
					if (!path.equals(pathAndLoader.classpath)) {
						// classpath change detected
						// System.out.println("Classpath change detected for " + projectName);
						pathAndLoader = new PathLoaderPair(path);
						projectToLoaderCache.put(projectName, pathAndLoader);
					}
				}
				// System.out.println("Using loader with path " + pathAndLoader.classpath);
				gcl = pathAndLoader.groovyClassLoader;
			}
		}
		return gcl;
	}

	// FIXASC review callers who pass null for options
	public GroovyParser(Object requestor, CompilerOptions options, ProblemReporter problemReporter, boolean allowTransforms,
			boolean isReconcile) {
		String path = (options == null ? null : options.groovyClassLoaderPath);
		this.requestor = requestor;
		// FIXASC set parent of the loader to system or context class loader?

		// record any paths we use for a project so that when the project is cleared,
		// the paths (which point to cached classloaders) can be cleared

		this.allowTransforms = allowTransforms;
		this.isReconcile = isReconcile;
		this.gclClasspath = path;
		this.compilerOptions = options;
		this.projectName = options.groovyProjectName;
		GroovyClassLoader gcl = getLoaderFor(path);
		// Grab needs them on, even for reconciling to behave *sigh*, so might as well turn them on for everything
		// allowTransforms = true;

		// Basic grab support: the design here is that a special classloader is created that will be augmented
		// with URLs when grab processing is running. This classloader is used as a last resort when resolving
		// types and is *only* called if a grab has occurred somewhere during compilation.
		// Currently it is not cached but created each time - we'll have to decide if there is a need to cache
		GrapeAwareGroovyClassLoader grabbyLoader = new GrapeAwareGroovyClassLoader();
		this.groovyCompilationUnit = makeCompilationUnit(grabbyLoader, allowTransforms ? gcl : null);
		this.groovyCompilationUnit.tweak(isReconcile);
		if (grabbyLoader != null) {
			grabbyLoader.setCompilationUnit(groovyCompilationUnit);
		}
		if (gcl == null && allowTransforms) {
			this.groovyCompilationUnit.ensureASTTransformVisitorAdded();
		}
		this.groovyCompilationUnit.removeOutputPhaseOperation();
		if ((options.groovyFlags & 0x01) != 0) {
			// its probably grails!
			// nothing up my sleeve, abracadabra!
			this.groovyCompilationUnit.addPhaseOperation(new GrailsInjector(gcl), Phases.CANONICALIZATION);
			if (allowTransforms && gcl != null) {
				this.groovyCompilationUnit.addPhaseOperation(new GrailsGlobalPluginAwareEntityInjector(gcl),
						Phases.CANONICALIZATION);
			}
		}
		// this.lookupEnvironment = lookupEnvironment;
		this.problemReporter = problemReporter;
		this.resolver = new JDTResolver(groovyCompilationUnit);
		// groovyCompilationUnit.setClassLoader(gcl);
		groovyCompilationUnit.setResolveVisitor(resolver);
	}

	static class GrapeAwareGroovyClassLoader extends GroovyClassLoader {

		// Could be prodded to indicate a grab has occurred within this compilation unit
		private CompilationUnit groovyCompilationUnit;

		public boolean grabbed = false; // set to true if any grabbing is done

		@Override
		public void addURL(URL url) {
			// System.out.println("Grape aware classloader was augmented with " + url);
			this.grabbed = true;
			super.addURL(url);
		}

		public void setCompilationUnit(CompilationUnit groovyCompilationUnit) {
			this.groovyCompilationUnit = groovyCompilationUnit;
		}

	}

	// // FIXASC perf ok?
	// private static void configureClasspath(URLClassLoader gcl, String path) {
	// if (path != null) {
	// if (path.indexOf(File.pathSeparator) != -1) {
	// int pos = 0;
	// while (pos != -1) {
	// int nextSep = path.indexOf(File.pathSeparator, pos);
	// if (nextSep == -1) {
	// // last piece
	// String p = path.substring(pos);
	// gcl.addClasspath(p);
	// pos = -1;
	// } else {
	// String p = path.substring(pos, nextSep);
	// gcl.addClasspath(p);
	// pos = nextSep + 1;
	// }
	// }
	// } else {
	// gcl.addClasspath(path);
	// }
	// }
	// }

	private static boolean NONLOCKING = false;

	static {
		try {
			boolean value = System.getProperty("greclipse.nonlocking", "false").equalsIgnoreCase("true");
			NONLOCKING = value;
			if (value) {
				System.out.println("property set: greclipse.nonlocking: will try to avoid locking jars");
			}
		} catch (Throwable t) {
		}
	}

	private static URLClassLoader createLoader(URL[] urls, ClassLoader parent) {
		if (NONLOCKING) {
			return new NonLockingJarFileClassLoader("AST Transform loader", urls, parent);
		} else {
			return new URLClassLoader(urls, parent);
		}
	}

	private static URLClassLoader createConfigureLoader(String path) {
		if (path == null) {
			return createLoader(null, Thread.currentThread().getContextClassLoader());
		}
		List<URL> urls = new ArrayList<URL>();
		if (path.indexOf(File.pathSeparator) != -1) {
			int pos = 0;
			while (pos != -1) {
				int nextSep = path.indexOf(File.pathSeparator, pos);
				if (nextSep == -1) {
					// last piece
					addNewURL(path.substring(pos), urls);
					pos = -1;
				} else {
					addNewURL(path.substring(pos, nextSep), urls);
					pos = nextSep + 1;
				}
			}
		} else {
			addNewURL(path, urls);
		}
		return createLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
	}

	private static void addNewURL(String path, List<URL> existingURLs) {
		try {
			File f = new File(path);
			URL newURL = f.toURI().toURL();
			for (URL url : existingURLs) {
				if (url.equals(newURL)) {
					return;
				}
			}
			existingURLs.add(newURL);
		} catch (MalformedURLException e) {
			// It was a busted URL anyway
		}
	}

	/**
	 * Call the groovy parser to drive the first few phases of
	 */
	public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
		char[] sourceCode = sourceUnit.getContents();
		if (sourceCode == null) {
			sourceCode = CharOperation.NO_CHAR; // pretend empty from thereon
		}

		// FIXASC (M3) need our own tweaked subclass of CompilerConfiguration?
		CompilerConfiguration groovyCompilerConfig = new CompilerConfiguration();
		// groovyCompilerConfig.setPluginFactory(new ErrorRecoveredCSTParserPluginFactory(null));
		ErrorCollector errorCollector = new GroovyErrorCollectorForJDT(groovyCompilerConfig);
		String filepath = null;

		// This check is necessary because the filename is short (as in the last part, eg. Foo.groovy) for types coming in
		// from the hierarchy resolver. If there is the same type in two different packages then the compilation process
		// is going to go wrong because the filename is used as a key in some groovy data structures. This can lead to false
		// complaints about the same file defining duplicate types.
		if (sourceUnit instanceof org.eclipse.jdt.internal.compiler.batch.CompilationUnit) {
			filepath = new String(((org.eclipse.jdt.internal.compiler.batch.CompilationUnit) sourceUnit).fileName);
		} else {
			filepath = new String(sourceUnit.getFileName());
		}

		{ // Try to turn this into a 'real' absolute file system reference
			Path path = new Path(filepath);
			if (path.segmentCount() >= 2) { //Needs 2 segments: a project and file name or eclipse throws assertion failed here.
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(filepath));
				filepath = file.getLocation().toFile().getAbsolutePath();
			}
		}

		SourceUnit groovySourceUnit = new SourceUnit(filepath, new String(sourceCode), groovyCompilerConfig,
				groovyCompilationUnit.getClassLoader(), errorCollector);
		GroovyCompilationUnitDeclaration gcuDeclaration = new GroovyCompilationUnitDeclaration(problemReporter, compilationResult,
				sourceCode.length, groovyCompilationUnit, groovySourceUnit, compilerOptions);
		// FIXASC get this from the Antlr parser
		compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(sourceCode);
		groovyCompilationUnit.addSource(groovySourceUnit);

		// Check if it is worth plugging in a callback listener for parse/generation
		if (requestor instanceof org.eclipse.jdt.internal.compiler.Compiler) {
			org.eclipse.jdt.internal.compiler.Compiler compiler = ((org.eclipse.jdt.internal.compiler.Compiler) requestor);
			if (compiler.requestor instanceof BatchImageBuilder) {
				BuildNotifier notifier = ((BatchImageBuilder) compiler.requestor).notifier;
				if (notifier != null) {
					groovyCompilationUnit.setProgressListener(new ProgressListenerImpl(notifier));
				}
			}
		}
		gcuDeclaration.processToPhase(Phases.CONVERSION);

		// Groovy moduleNode is null when there is a fatal error
		// Otherwise, recover what we can
		if (gcuDeclaration.getModuleNode() != null) {
			gcuDeclaration.populateCompilationUnitDeclaration();
			for (TypeDeclaration decl : gcuDeclaration.types) {
				GroovyTypeDeclaration gtDeclaration = (GroovyTypeDeclaration) decl;
				resolver.record(gtDeclaration);
			}
		}
		// Is this a script?
		// If allowTransforms is TRUE then this is a 'full build' and we should remember which are scripts so that
		// .class file output can be suppressed
		if (allowTransforms && (sourceUnit instanceof SourceFile)) {
			if (this.scriptFolderSelector == null) {
				this.scriptFolderSelector = new ScriptFolderSelector();
			}
			SourceFile file = (SourceFile) sourceUnit;
			if (scriptFolderSelector.isScript(file.resource)) {
				gcuDeclaration.tagAsScript();
			}
			// System.out.println(sourceUnit + " " + (isScript ? "IS" : "is NOT") + " a script");
		}
		if (debugRequestor != null) {
			debugRequestor.acceptCompilationUnitDeclaration(gcuDeclaration);
		}
		return gcuDeclaration;
	}

	/**
	 * ProgressListener is called back when parsing of a file or generation of a classfile completes. By calling back to the build
	 * notifier we ignore those long pauses where it look likes it has hung!
	 * 
	 * Note: this does not move the progress bar, it merely updates the text
	 */
	static class ProgressListenerImpl implements ProgressListener {

		private BuildNotifier notifier;

		public ProgressListenerImpl(BuildNotifier notifier) {
			this.notifier = notifier;
		}

		public void parseComplete(int phase, String sourceUnitName) {
			try {
				// Chop it down to the containing package folder
				int lastSlash = sourceUnitName.lastIndexOf("/");
				if (lastSlash == -1) {
					lastSlash = sourceUnitName.lastIndexOf("\\");
				}
				if (lastSlash != -1) {
					StringBuffer msg = new StringBuffer();
					msg.append("Parsing groovy source in ");
					msg.append(sourceUnitName, 0, lastSlash);
					notifier.subTask(msg.toString());
				}
			} catch (Exception e) {
				// doesn't matter
			}
			notifier.checkCancel();
		}

		public void generateComplete(int phase, ClassNode classNode) {
			try {
				String pkgName = classNode.getPackageName();
				if (pkgName != null && pkgName.length() > 0) {
					StringBuffer msg = new StringBuffer();
					msg.append("Generating groovy classes in ");
					msg.append(pkgName);
					notifier.subTask(msg.toString());
				}
			} catch (Exception e) {
				// doesn't matter
			}
			notifier.checkCancel();
		}
	}

	public void reset() {
		GroovyClassLoader gcl = getLoaderFor(gclClasspath);
		GrapeAwareGroovyClassLoader grabbyLoader = new GrapeAwareGroovyClassLoader();
		this.groovyCompilationUnit = makeCompilationUnit(grabbyLoader, gcl);
		this.scriptFolderSelector = null;
		grabbyLoader.setCompilationUnit(this.groovyCompilationUnit);
		if (gcl == null) {
			this.groovyCompilationUnit.ensureASTTransformVisitorAdded();
		}
		this.resolver = new JDTResolver(groovyCompilationUnit);
		this.groovyCompilationUnit.setResolveVisitor(resolver);
	}

	private CompilationUnit makeCompilationUnit(GroovyClassLoader loader, GroovyClassLoader transformLoader) {
		CompilationUnit it = new CompilationUnit(null, null, loader, transformLoader);
		// Grails: start
		// This code makes Grails 1.4.M1 AST transforms work.
		if (transformLoader != null && compilerOptions != null && ((compilerOptions.groovyFlags & 0x01) != 0)) {
			try {
				Class<?> klass = Class.forName("org.codehaus.groovy.grails.compiler.injection.GrailsAwareInjectionOperation", true,
						transformLoader);
				if (klass != null) {
					ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(transformLoader);
						PrimaryClassNodeOperation op = (PrimaryClassNodeOperation) klass.newInstance();
						it.addPhaseOperation(op, Phases.CANONICALIZATION);
					} finally {
						Thread.currentThread().setContextClassLoader(savedLoader);
					}
				}
			} catch (Exception e) {
				// Ignore... probably means its not grails 1.4 project
			}
		}
		// Grails: end
		return it;
	}

	public CompilerOptions getCompilerOptions() {
		return compilerOptions;
	}

	public boolean getAllowTranforms() {
		return allowTransforms;
	}

	public boolean getIsReconcile() {
		return isReconcile;
	}

	public CompilationUnit getGroovyCompilationUnit() {
		return groovyCompilationUnit;
	}

	public String getProjectName() {
		return projectName;
	}

}
