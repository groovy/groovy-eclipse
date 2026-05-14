/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * Java 6 annotation processor manager used when compiling from the command line
 * or via the javax.tools.JavaCompiler interface.
 */
public class BatchAnnotationProcessorManager extends BaseAnnotationProcessorManager
{

	/**
	 * Processors that have been set by calling CompilationTask.setProcessors().
	 */
	private List<Processor> _setProcessors = null;
	private Iterator<Processor> _setProcessorIter = null;

	/**
	 * Processors named with the -processor option on the command line.
	 */
	private List<String> _commandLineProcessors;
	private Iterator<String> _commandLineProcessorIter = null;

	private ServiceLoader<Processor> _serviceLoader = null;
	private Iterator<Processor> _serviceLoaderIter;

	private ClassLoader _procLoader;

	// Set this to true in order to trace processor discovery when -XprintProcessorInfo is specified
	private final static boolean VERBOSE_PROCESSOR_DISCOVERY = true;
	private boolean _printProcessorDiscovery = false;

	/**
	 * Zero-arg constructor so this object can be easily created via reflection.
	 * A BatchAnnotationProcessorManager cannot be used until its
	 * {@link #configure(Object, String[])} method has been called.
	 */
	public BatchAnnotationProcessorManager()
	{
	}

	@Override
	public void configure(Object batchCompiler, String[] commandLineArguments) {
		if (null != this._processingEnv) {
			throw new IllegalStateException(
					"Calling configure() more than once on an AnnotationProcessorManager is not supported"); //$NON-NLS-1$
		}
		BatchProcessingEnvImpl processingEnv = new BatchProcessingEnvImpl(this, (Main) batchCompiler, commandLineArguments);
		this._processingEnv = processingEnv;
		@SuppressWarnings("resource") // fileManager is not opened here
		JavaFileManager fileManager = processingEnv.getFileManager();
		if (fileManager instanceof StandardJavaFileManager) {
			Iterable<? extends File> location = null;
			if (SourceVersion.latest().compareTo(SourceVersion.RELEASE_8) > 0) {
				location = ((StandardJavaFileManager) fileManager).getLocation(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
			}
			if (location != null) {
				this._procLoader = fileManager.getClassLoader(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
			} else {
				this._procLoader = fileManager.getClassLoader(StandardLocation.ANNOTATION_PROCESSOR_PATH);
			}
		} else {
			// Fall back to old code
			this._procLoader = fileManager.getClassLoader(StandardLocation.ANNOTATION_PROCESSOR_PATH);
		}
		parseCommandLine(commandLineArguments);
		this._round = 0;
	}

	/**
	 * If a -processor option was specified in command line arguments,
	 * parse it into a list of qualified classnames.
	 * @param commandLineArguments contains one string for every space-delimited token on the command line
	 */
	private void parseCommandLine(String[] commandLineArguments) {
		List<String> commandLineProcessors = null;
		for (int i = 0; i < commandLineArguments.length; ++i) {
			String option = commandLineArguments[i];
			if ("-XprintProcessorInfo".equals(option)) { //$NON-NLS-1$
				this._printProcessorInfo = true;
				this._printProcessorDiscovery = VERBOSE_PROCESSOR_DISCOVERY;
			}
			else if ("-XprintRounds".equals(option)) { //$NON-NLS-1$
				this._printRounds = true;
			}
			else if ("-processor".equals(option)) { //$NON-NLS-1$
				commandLineProcessors = new ArrayList<>();
				String procs = commandLineArguments[++i];
				commandLineProcessors.addAll(Arrays.asList(procs.split(","))); //$NON-NLS-1$
				break;
			}
		}
		this._commandLineProcessors =  commandLineProcessors;
		if (null != this._commandLineProcessors) {
			this._commandLineProcessorIter = this._commandLineProcessors.iterator();
		}
	}

	@Override
	public ProcessorInfo discoverNextProcessor() {
		if (null != this._setProcessors) {
			// If setProcessors() was called, use that list until it's empty and then stop.
			if (this._setProcessorIter.hasNext()) {
				Processor p = this._setProcessorIter.next();
				p.init(this._processingEnv);
				ProcessorInfo pi = new ProcessorInfo(p);
				this._processors.add(pi);
				if (this._printProcessorDiscovery && null != this._out) {
					this._out.println("API specified processor: " + pi); //$NON-NLS-1$
				}
				return pi;
			}
			return null;
		}

		if (null != this._commandLineProcessors) {
			// If there was a -processor option, iterate over processor names,
			// creating and initializing processors, until no more names are found, then stop.
			if (this._commandLineProcessorIter.hasNext()) {
				String proc = this._commandLineProcessorIter.next();
				try {
					Class<?> clazz = this._procLoader.loadClass(proc);
					Object o = clazz.getDeclaredConstructor().newInstance();
					Processor p = (Processor) o;
					p.init(this._processingEnv);
					ProcessorInfo pi = new ProcessorInfo(p);
					this._processors.add(pi);
					if (this._printProcessorDiscovery && null != this._out) {
						this._out.println("Command line specified processor: " + pi); //$NON-NLS-1$
					}
					return pi;
				} catch (Exception e) {
					// TODO: better error handling
					throw new AbortCompilation(null, e);
				}
			}
			return null;
		}

		// if no processors were explicitly specified with setProcessors()
		// or the command line, search the processor path with ServiceLoader.
		if (null == this._serviceLoader ) {
			this._serviceLoader = ServiceLoader.load(Processor.class, this._procLoader);
			this._serviceLoaderIter = this._serviceLoader.iterator();
		}
		try {
			if (this._serviceLoaderIter.hasNext()) {
				Processor p = this._serviceLoaderIter.next();
				p.init(this._processingEnv);
				ProcessorInfo pi = new ProcessorInfo(p);
				this._processors.add(pi);
				if (this._printProcessorDiscovery && null != this._out) {
					StringBuilder sb = new StringBuilder();
					sb.append("Discovered processor service "); //$NON-NLS-1$
					sb.append(pi);
					sb.append("\n  supporting "); //$NON-NLS-1$
					sb.append(pi.getSupportedAnnotationTypesAsString());
					sb.append("\n  in "); //$NON-NLS-1$
					sb.append(getProcessorLocation(p));
					this._out.println(sb.toString());
				}
				return pi;
			}
		} catch (ServiceConfigurationError e) {
			// TODO: better error handling
			throw new AbortCompilation(null, e);
		}
		return null;
	}

	/**
	 * Used only for debugging purposes.  Generates output like "file:jar:D:/temp/jarfiles/myJar.jar!/".
	 * Surely this code already exists in several hundred other places?
	 * @return the location whence a processor class was loaded.
	 */
	private String getProcessorLocation(Processor p) {
		// Get the classname in a form that can be passed to ClassLoader.getResource(),
		// e.g., "pa/pb/pc/Outer$Inner.class"
		boolean isMember = false;
		Class<?> outerClass = p.getClass();
		StringBuilder innerName = new StringBuilder();
		while (outerClass.isMemberClass()) {
			innerName.insert(0, outerClass.getSimpleName());
			innerName.insert(0, '$');
			isMember = true;
			outerClass = outerClass.getEnclosingClass();
		}
		String path = outerClass.getName();
		path = path.replace('.', '/');
		if (isMember) {
			path = path + innerName;
		}
		path = path + ".class"; //$NON-NLS-1$

		// Find the URL for the class resource and strip off the resource name itself
		String location = this._procLoader.getResource(path).toString();
		if (location.endsWith(path)) {
			location = location.substring(0, location.length() - path.length());
		}
		return location;
	}

	@Override
	public void reportProcessorException(Processor p, Exception e) {
		// TODO: if (verbose) report the processor
		throw new AbortCompilation(null, e);
	}

	@Override
	public void setProcessors(Object[] processors) {
		if (!this._isFirstRound) {
			throw new IllegalStateException("setProcessors() cannot be called after processing has begun"); //$NON-NLS-1$
		}
		// Cast all the processors here, rather than failing later.
		// But don't call init() until the processor is actually needed.
		this._setProcessors = new ArrayList<>(processors.length);
		for (Object o : processors) {
			Processor p = (Processor)o;
			this._setProcessors.add(p);
		}
		this._setProcessorIter = this._setProcessors.iterator();

		// processors set this way take precedence over anything on the command line
		this._commandLineProcessors = null;
		this._commandLineProcessorIter = null;
	}

	@Override
	protected void cleanUp() {
		// the classloader needs to be kept open between rounds, close it at the end:
		if (this._procLoader instanceof URLClassLoader) {
			try {
				((URLClassLoader) this._procLoader).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
