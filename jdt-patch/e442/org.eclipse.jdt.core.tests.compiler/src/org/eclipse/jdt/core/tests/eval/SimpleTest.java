/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.eval;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.runtime.LocalVMLauncher;
import org.eclipse.jdt.core.tests.runtime.LocalVirtualMachine;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.runtime.TargetInterface;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.eval.EvaluationContext;
import org.eclipse.jdt.internal.eval.EvaluationResult;
import org.eclipse.jdt.internal.eval.GlobalVariable;
import org.eclipse.jdt.internal.eval.IRequestor;

public class SimpleTest {
	static final String JRE_PATH = Util.getJREDirectory();
	static final String[] COMPILATION_CLASSPATH = Util.concatWithClassLibs(Util.getOutputDirectory(), false);
	static final String[] RUNTIME_CLASSPATH =  new String[] {Util.getOutputDirectory()};
	static final String TARGET_PATH = Util.getOutputDirectory() + File.separator + "evaluation";
	protected EvaluationContext context;
	protected LocalVirtualMachine launchedVM;
	protected TargetInterface target;
	protected Requestor requestor;

	class Requestor implements IRequestor {
		int globalProblemCount = 0;
		public boolean acceptClassFiles(ClassFile[] classFiles, char[] codeSnippetClassName) {
			try {
				SimpleTest.this.target.sendClasses(codeSnippetClassName != null, classFiles);
			} catch (TargetException e) {
				return false;
			}
			if (codeSnippetClassName != null) {
				TargetInterface.Result result = SimpleTest.this.target.getResult();
				if (result.displayString == null) {
					System.out.println("(No explicit return value)");
				} else {
					System.out.print("(");
					System.out.print(result.typeName);
					System.out.print(") ");
					System.out.println(result.displayString);
				}
			} else {
				for (int i = 0, length = classFiles.length; i < length; i++) {
					char[][] compoundName = classFiles[i].getCompoundName();
					if (new String(compoundName[compoundName.length-1]).startsWith("GlobalVariable")) {
						try {
							IBinaryField[] fields = new ClassFileReader(classFiles[i].getBytes(), null).getFields();
							if (fields != null) {
								for (int j = 0; j < fields.length; j++) {
									TargetInterface.Result result = SimpleTest.this.target.getResult();
									if (result.displayString == null) {
										System.out.println("(No explicit return value)");
									} else {
										System.out.print("(");
										System.out.print(result.typeName);
										System.out.print(") ");
										System.out.println(result.displayString);
									}
								}
							}
						} catch (ClassFormatException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return true;
		}
		public void acceptProblem(CategorizedProblem problem, char[] fragmentSource, int fragmentKind) {
			int localErrorCount = 0;
			this.globalProblemCount++;
			char[] source = fragmentSource;
			if (localErrorCount == 0)
				System.out.println("----------");
			if (fragmentKind == EvaluationResult.T_INTERNAL) {
				System.out.print(this.globalProblemCount + (problem.isError() ? ". INTERNAL ERROR" : ". INTERNAL WARNING"));
				System.out.print(" in generated compilation unit");
			} else {
				System.out.print(this.globalProblemCount + (problem.isError() ? ". ERROR" : ". WARNING"));
				System.out.print(" in ");
				switch (fragmentKind) {
					case EvaluationResult.T_PACKAGE:
						System.out.print("package");
						break;
					case EvaluationResult.T_IMPORT:
						System.out.print("import");
						break;
					case EvaluationResult.T_CODE_SNIPPET:
						System.out.print("code snippet");
						break;
					case EvaluationResult.T_VARIABLE:
						int line = problem.getSourceLineNumber();
						if (line == -1) {
							System.out.print("variable type");
							source = findVar(fragmentSource).getTypeName();
						} else if (line == 0) {
							System.out.print("variable name");
							source = findVar(fragmentSource).getName();
						} else {
							System.out.print("variable initializer");
							source = findVar(fragmentSource).getInitializer();
						}
						break;
				}
			}
			System.out.println(errorReportSource((DefaultProblem)problem, source));
			System.out.println(problem.getMessage());
			System.out.println("----------");
			if (problem.isError())
				localErrorCount++;
		}
	}
/**
 * Build a char array from the given lines
 */
protected char[] buildCharArray(String[] lines) {
	String buffer = String.join("\n", lines);
	int length = buffer.length();
	char[] result = new char[length];
	buffer.getChars(0, length, result, 0);
	return result;
}
protected String errorReportSource(DefaultProblem problem, char[] source) {
	//extra from the source the innacurate     token
	//and "highlight" it using some underneath ^^^^^
	//put some context around too.

	//this code assumes that the font used in the console is fixed size

	//sanity .....
	if ((problem.getSourceStart() > problem.getSourceEnd()) || ((problem.getSourceStart() < 0) && (problem.getSourceEnd() < 0)))
		return "\n!! no source information available !!";

	//regular behavior....(slow code)

	final char SPACE = '\u0020';
	final char MARK = '^';
	final char TAB = '\t';
	//the next code tries to underline the token.....
	//it assumes (for a good display) that token source does not
	//contain any \r \n. This is false on statements !
	//(the code still works but the display is not optimal !)

	//compute the how-much-char we are displaying around the inaccurate token
	int begin = problem.getSourceStart() >= source.length ? source.length - 1 : problem.getSourceStart();
	int relativeStart = 0;
	int end = problem.getSourceEnd() >= source.length ? source.length - 1 : problem.getSourceEnd();
	label : for (relativeStart = 0;; relativeStart++) {
		if (begin == 0)
			break label;
		if ((source[begin - 1] == '\n') || (source[begin - 1] == '\r'))
			break label;
		begin--;
	}
	label : for (;;) {
		if ((end + 1) >= source.length)
			break label;
		if ((source[end + 1] == '\r') || (source[end + 1] == '\n')) {
			break label;
		}
		end++;
	}
	//extract the message form the source
	char[] extract = new char[end - begin + 1];
	System.arraycopy(source, begin, extract, 0, extract.length);
	char c;
	//remove all SPACE and TAB that begin the error message...
	int trimLeftIndex = 0;
	while (((c = extract[trimLeftIndex++]) == TAB) || (c == SPACE)) {
	}
	System.arraycopy(extract, trimLeftIndex - 1, extract = new char[extract.length - trimLeftIndex + 1], 0, extract.length);
	relativeStart -= trimLeftIndex;
	//buffer spaces and tabs in order to reach the error position
	int pos = 0;
	char[] underneath = new char[extract.length]; // can't be bigger
	for (int i = 0; i <= relativeStart; i++) {
		if (extract[i] == TAB) {
			underneath[pos++] = TAB;
		} else {
			underneath[pos++] = SPACE;
		}
	}
	//mark the error position
	for (int i = problem.getSourceStart(); i <= (problem.getSourceEnd() >= source.length ? source.length - 1 : problem.getSourceEnd()); i++)
		underneath[pos++] = MARK;
	//resize underneathto remove 'null' chars
	System.arraycopy(underneath, 0, underneath = new char[pos], 0, pos);
	return
		((problem.getSourceLineNumber() > 0) ?
			(" (at line " + String.valueOf(problem.getSourceLineNumber()) + ")") :
			""
		) +
		"\n\t" + new String(extract) + "\n\t" + new String(underneath);
}
protected GlobalVariable findVar(char[] varName) {
	GlobalVariable[] vars = this.context.allVariables();
	for (int i = 0; i < vars.length; i++) {
		GlobalVariable var = vars[i];
		if (CharOperation.equals(var.getName(), varName)) {
			return var;
		}
	}
	return null;
}
protected INameEnvironment getEnv() {

	return new FileSystem(COMPILATION_CLASSPATH, new String[0], null);
}
protected IProblemFactory getProblemFactory() {
	return new DefaultProblemFactory(java.util.Locale.getDefault());
}
protected void startEvaluationContext() throws TargetException {
	try (ServerSocket server = new ServerSocket(0)) {
		LocalVMLauncher launcher = LocalVMLauncher.getLauncher();
		launcher.setVMPath(JRE_PATH);
		launcher.setClassPath(RUNTIME_CLASSPATH);
		int evalPort = server.getLocalPort();
		launcher.setEvalPort(evalPort);
		launcher.setEvalTargetPath(TARGET_PATH);
		this.launchedVM = launcher.launch();

		(new Thread() {
			@Override
			public void run() {
				try {
					java.io.InputStream in = SimpleTest.this.launchedVM.getInputStream();
					int read = 0;
					while (read != -1) {
						try {
							read = in.read();
						} catch (java.io.IOException e) {
							read = -1;
						}
						if (read != -1) {
							System.out.print((char)read);
						}
					}
				} catch (TargetException e) {
				}
			}
		}).start();

		(new Thread() {
			@Override
			public void run() {
				try {
					java.io.InputStream in = SimpleTest.this.launchedVM.getErrorStream();
					int read = 0;
					while (read != -1) {
						try {
							read = in.read();
						} catch (java.io.IOException e) {
							read = -1;
						}
						if (read != -1) {
							System.out.print((char)read);
						}
					}
				} catch (TargetException e) {
				}
			}
		}).start();

		this.requestor = new Requestor();
		this.target = new TargetInterface();
		this.target.connect(server, 30000); // allow 30s max to connect (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=188127)
		this.context = new EvaluationContext();
	} catch (IOException e) {
		throw new Error("Failed to open socket", e);
	}
}
protected void stopEvaluationContext() {
	try {
		this.target.disconnect(); // Close the socket first so that the OS resource has a chance to be freed.
		int retry = 0;
		while (this.launchedVM.isRunning() && (++retry < 20)) {
			try {
				Thread.sleep(retry * 100);
			} catch (InterruptedException e) {
			}
		}
		if (this.launchedVM.isRunning()) {
			this.launchedVM.shutDown();
		}
		this.context = null;
	} catch (TargetException e) {
		throw new Error(e.getMessage());
	}
}
}
