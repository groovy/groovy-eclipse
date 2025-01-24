/*******************************************************************************
 * Copyright (c) 2006, 2024 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.tools.JavaFileManager;
import org.eclipse.jdt.internal.compiler.apt.util.EclipseFileManager;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * The implementation of ProcessingEnvironment that is used when compilation is
 * driven by the command line or by the Tool interface.  This environment uses
 * the JavaFileManager provided by the compiler.
 */
public class BatchProcessingEnvImpl extends BaseProcessingEnvImpl {

	protected final BaseAnnotationProcessorManager _dispatchManager;
	protected final JavaFileManager _fileManager;
	protected final Main _compilerOwner;

	public BatchProcessingEnvImpl(BaseAnnotationProcessorManager dispatchManager, Main batchCompiler,
			String[] commandLineArguments)
	{
		super();
		this._compilerOwner = batchCompiler;
		this._compiler = batchCompiler.batchCompiler;
		this._dispatchManager = dispatchManager;
		Class<?> c = null;
		try {
			c = Class.forName("org.eclipse.jdt.internal.compiler.tool.EclipseCompilerImpl"); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			// ignore
		}
		Field field = null;
		JavaFileManager javaFileManager = null;
		if (c != null) {
			try {
				field = c.getField("fileManager"); //$NON-NLS-1$
			} catch (SecurityException e) {
				// ignore
			} catch (IllegalArgumentException e) {
				// ignore
			} catch (NoSuchFieldException e) {
				// ignore
			}
		}
		if (field != null) {
			try {
				javaFileManager = (JavaFileManager) field.get(batchCompiler);
			} catch (IllegalArgumentException e) {
				// ignore
			} catch (IllegalAccessException e) {
				// ignore
			}
		}
		if (javaFileManager != null) {
			this._fileManager = javaFileManager;
		} else {
			String encoding = batchCompiler.options.get(CompilerOptions.OPTION_Encoding);
			Charset charset = encoding != null ? Charset.forName(encoding) : null;
			JavaFileManager manager = new EclipseFileManager(batchCompiler.compilerLocale, charset);
			ArrayList<String> options = new ArrayList<>();
			options.addAll(Arrays.asList(commandLineArguments));
			for (Iterator<String> iterator = options.iterator(); iterator.hasNext(); ) {
				manager.handleOption(iterator.next(), iterator);
			}
			this._fileManager = manager;
		}
		this._processorOptions = Collections.unmodifiableMap(parseProcessorOptions(commandLineArguments));
		this._filer = new BatchFilerImpl(this._dispatchManager, this, this._compilerOwner);
		this._messager = new BatchMessagerImpl(this, this._compilerOwner);
	}

	/**
	 * Parse the -A command line arguments so that they can be delivered to
	 * processors with {@link javax.annotation.processing.ProcessingEnvironment#getOptions()}.  In Sun's Java 6
	 * version of javac, unlike in the Java 5 apt tool, only the -A options are
	 * passed to processors, not the other command line options; that behavior
	 * is repeated here.
	 * @param args the equivalent of the args array from the main() method.
	 * @return a map of key to value, or key to null if there is no value for
	 * a particular key.  The "-A" is stripped from the key, so a command-line
	 * argument like "-Afoo=bar" will result in an entry with key "foo" and
	 * value "bar".
	 */
	private Map<String, String> parseProcessorOptions(String[] args) {
		Map<String, String> options = new LinkedHashMap<>();
		for (String arg : args) {
			if (!arg.startsWith("-A")) { //$NON-NLS-1$
				continue;
			}
			int equals = arg.indexOf('=');
			if (equals == 2) {
				// option begins "-A=" - not valid
				Exception e = new IllegalArgumentException("-A option must have a key before the equals sign"); //$NON-NLS-1$
				throw new AbortCompilation(null, e);
			}
			if (equals == arg.length() - 1) {
				// option ends with "=" - not valid
				options.put(arg.substring(2, equals), null);
			} else if (equals == -1) {
				// no value
				options.put(arg.substring(2), null);
			} else {
				// value and key
				options.put(arg.substring(2, equals), arg.substring(equals + 1));
			}
		}
		return options;
	}
	@Override
	public JavaFileManager getFileManager() {
		return this._fileManager;
	}

	@Override
	public Locale getLocale() {
		return this._compilerOwner.compilerLocale;
	}

	public boolean shouldIgnoreOptionalProblems(char[] fileName) {
		return Main.shouldIgnoreOptionalProblems(this._compilerOwner.ignoreOptionalProblemsFromFolders, fileName);
	}
}
