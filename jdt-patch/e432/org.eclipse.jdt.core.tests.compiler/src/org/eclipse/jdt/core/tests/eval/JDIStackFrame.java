/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jdt.core.tests.runtime.RuntimeConstants;
import org.eclipse.jdt.internal.eval.EvaluationConstants;
import org.eclipse.jdt.internal.eval.EvaluationResult;
import org.eclipse.jdt.internal.eval.InstallException;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JDIStackFrame implements EvaluationConstants, RuntimeConstants {
	VirtualMachine jdiVM;
	ThreadReference jdiThread;
	String userCode;
	String breakpointClassName;
	String breakpointMethodName;
	int breakpointLine;
	long timeout;
public JDIStackFrame(VirtualMachine jdiVM, DebugEvaluationTest test, String userCode) {
	this(jdiVM, test, userCode, "_JDIStackFrame_", "foo", Integer.MAX_VALUE);
}
public JDIStackFrame(
	VirtualMachine jdiVM,
	DebugEvaluationTest test,
	String userCode,
	String breakpointClassName,
	String breakpointMethodName,
	int breakpointLine) {

	this(jdiVM, test, userCode, breakpointClassName, breakpointMethodName, breakpointLine, 10000/*timeout*/);

}
public JDIStackFrame(
	VirtualMachine jdiVM,
	DebugEvaluationTest test,
	String userCode,
	String breakpointClassName,
	String breakpointMethodName,
	int breakpointLine,
	long timeout) {

	this.jdiVM = jdiVM;
	this.userCode = userCode;
	this.breakpointClassName = breakpointClassName;
	this.breakpointMethodName = breakpointMethodName;
	this.breakpointLine = breakpointLine;
	this.timeout = timeout;

	test.jdiStackFrame = null;
	this.jdiThread = getDebuggedThread(test);
	test.jdiStackFrame = this;
}
public char[] declaringTypeName() {
	if (this.breakpointLine != Integer.MAX_VALUE) { // if not in a code snippet
		StackFrame frame = getStackFrame();
		return frame.location().declaringType().name().toCharArray();
	}
	return null;
}
protected ThreadReference getDebuggedThread(DebugEvaluationTest test) {
	try {
		// desintall previous breakpoints
		this.jdiVM.eventRequestManager().deleteAllBreakpoints();

		// install a breakpoint at the breakpointLine
		List classes = this.jdiVM.classesByName(this.breakpointClassName);
		if (classes.size() == 0) {
			if (this.breakpointClassName.equals("_JDIStackFrame_")) {
				// install special class
				String source =
					"public class _JDIStackFrame_ {\n" +
					"  public int foo() {\n" +
					"    return -1;\n" +
					"  }\n" +
					"}";
				test.compileAndDeploy(source, "_JDIStackFrame_");
			}

			// force load of class
			test.evaluateWithExpectedDisplayString(
				("return Class.forName(\"" + this.breakpointClassName + "\");").toCharArray(),
				("class " + this.breakpointClassName).toCharArray()
			);
			classes = this.jdiVM.classesByName(this.breakpointClassName);
			if (classes.size() == 0) {
				// workaround bug in Standard VM
				Iterator iterator = this.jdiVM.allClasses().iterator();
				while (iterator.hasNext()) {
					ReferenceType type = (ReferenceType)iterator.next();
					if (type.name().equals(this.breakpointClassName)) {
						classes = new ArrayList(1);
						classes.add(type);
						break;
					}
				}
				if (classes.size() == 0) {
					throw new Error("JDI could not retrieve class for " + this.breakpointClassName);
				}
			}
		}
		ClassType clazz = (ClassType)classes.get(0);
		Method method = clazz.methodsByName(this.breakpointMethodName).get(0);
		Location location;
		if (this.breakpointLine < 0 || this.breakpointLine == Integer.MAX_VALUE) {
			location = method.location();
		} else {
			location = method.locationsOfLine(this.breakpointLine).get(0);
		}
		BreakpointRequest request = this.jdiVM.eventRequestManager().createBreakpointRequest(location);
		request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		request.enable();

		// create a new thread that hit the breakpoint
		EvaluationTest.Requestor requestor = test.new Requestor();
		try {
			test.resetEnv();
			test.context.evaluate(
				("(new Thread() {\n" +
				"  public void run() {\n" +
				     this.userCode + "\n" +
				(this.breakpointClassName.equals("_JDIStackFrame_") ? "    new _JDIStackFrame_().foo();\n" : "") +
				"  }\n" +
				"  public String toString() {\n" +
				"    return \"my thread\";\n" +
				"  }\n" +
				"}).start();\n").toCharArray(),
				test.getEnv(),
				test.getCompilerOptions(),
				requestor,
				test.getProblemFactory());
		} catch (InstallException e) {
			TestCase.assertTrue("Target exception " + e.getMessage(), false);
		}
		EvaluationResult[] results = requestor.results;
		for (int i = 0; i < requestor.resultIndex + 1; i++){
			if (results[i].hasErrors()) {
				TestCase.assertTrue("Compilation error in user code", false);
			}
		}

		// Wait for the breakpoint event
		Event event = null;
		do {
			EventSet set = this.jdiVM.eventQueue().remove();
			Iterator iterator = set.eventIterator();
			while (iterator.hasNext()) {
				event = (Event)iterator.next();
				if (event instanceof BreakpointEvent)
					break;
			}
		} while (!(event instanceof BreakpointEvent));

		// Return the suspended thread
		return ((BreakpointEvent)event).thread();
	} catch (AbsentInformationException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	return null;
}
protected StackFrame getStackFrame() {
	try {
		if (this.breakpointLine == Integer.MAX_VALUE) {
			return this.jdiThread.frame(1);
		}
		return this.jdiThread.frame(0);
	} catch (IncompatibleThreadStateException e) {
		e.printStackTrace();
		return null;
	}
}
public boolean isConstructorCall() {
	if (this.breakpointLine != Integer.MAX_VALUE) { // if not in a code snippet
		StackFrame frame = getStackFrame();
		return frame.location().method().isConstructor();
	}
	return false;
}
public boolean isStatic() {
	if (this.breakpointLine != Integer.MAX_VALUE) { // if not in a code snippet
		StackFrame frame = getStackFrame();
		return frame.location().method().isStatic();
	}
	return false;
}
public int[] localVariableModifiers() {
	try {
		StackFrame frame = getStackFrame();
		List variables = frame.visibleVariables();
		int[] modifiers = new int[variables.size()];
/*		Iterator iterator = variables.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			LocalVariable var = (LocalVariable)iterator.next();
			// TBD modifiers[i++] = var. ??? ;
		}
*/		return modifiers;
	} catch (AbsentInformationException e) {
		return null;
	}
}
public char[][] localVariableNames() {
	try {
		StackFrame frame = getStackFrame();
		Iterator variables = frame.visibleVariables().iterator();
		List<char[]> names = new ArrayList<>();
		while (variables.hasNext()) {
			LocalVariable var = (LocalVariable)variables.next();
			names.add(var.name().toCharArray());
		}
		char[][] result = new char[names.size()][];
		names.toArray(result);
		return result;
	} catch (AbsentInformationException e) {
		return null;
	}
}
public char[][] localVariableTypeNames() {
	try {
		StackFrame frame = getStackFrame();
		Iterator variables = frame.visibleVariables().iterator();
		List<char[]> names = new ArrayList();
		while (variables.hasNext()) {
			LocalVariable var = (LocalVariable)variables.next();
			names.add(var.typeName().toCharArray());
		}
		char[][] result = new char[names.size()][];
		names.toArray(result);
		return result;
	} catch (AbsentInformationException e) {
		return null;
	}
}
public boolean run(String codeSnippetClassName) {
		ClassType codeSnippetClass;
		ObjectReference codeSnippet;
		Method method;
		List arguments;
		ObjectReference codeSnippetRunner;
		try {
			// Get the code snippet class
			long start = System.currentTimeMillis();
			List classes = this.jdiVM.classesByName(codeSnippetClassName);
			while (classes.size() == 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				classes = this.jdiVM.classesByName(codeSnippetClassName);
				if (classes.size() == 0) {
					// workaround bug in Standard VM
					Iterator iterator = this.jdiVM.allClasses().iterator();
					while (iterator.hasNext()) {
						ReferenceType type = (ReferenceType)iterator.next();
						if (type.name().equals(codeSnippetClassName)) {
							classes = new ArrayList(1);
							classes.add(type);
							break;
						}
					}
					if (classes.size() == 0 && (System.currentTimeMillis()-start) > this.timeout) {
						return false;
					}
				}
			}
			codeSnippetClass = (ClassType)classes.get(0);

			// Create a new code snippet
			Method constructor = codeSnippetClass.methodsByName("<init>").get(0);
			codeSnippet = codeSnippetClass.newInstance(this.jdiThread, constructor, new ArrayList(), ClassType.INVOKE_SINGLE_THREADED);

			// Install local variables and "this" into generated fields
			StackFrame stackFrame = getStackFrame();
			try {
				Iterator variables = stackFrame.visibleVariables().iterator();
				while (variables.hasNext()) {
					LocalVariable jdiVariable = (LocalVariable)variables.next();
					Value value = stackFrame.getValue(jdiVariable);
					Field field = codeSnippetClass.fieldByName(new String(LOCAL_VAR_PREFIX) + jdiVariable.name());
					codeSnippet.setValue(field, value);
				}
			} catch (AbsentInformationException e) {
				// No variables
			}
			Field delegateThis = codeSnippetClass.fieldByName(new String(DELEGATE_THIS));
			ObjectReference thisObject;
			if (delegateThis != null && ((thisObject = stackFrame.thisObject()) != null)) {
				codeSnippet.setValue(delegateThis, thisObject);
			}

			// Get the code snippet runner
			ClassType codeSnippetRunnerClass = (ClassType)this.jdiVM.classesByName(CODE_SNIPPET_RUNNER_CLASS_NAME).get(0);
			Field theRunner = codeSnippetRunnerClass.fieldByName(THE_RUNNER_FIELD);
			codeSnippetRunner = (ObjectReference)codeSnippetRunnerClass.getValue(theRunner);

			// Get the method 'runCodeSnippet' and its arguments
			method = codeSnippetRunnerClass.methodsByName(RUN_CODE_SNIPPET_METHOD).get(0);
			arguments = new ArrayList();
			arguments.add(codeSnippet);
		} catch (ClassNotLoadedException e) {
			e.printStackTrace();
			return false;
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
			return false;
		} catch (InvalidTypeException e) {
			e.printStackTrace();
			return false;
		} catch (InvocationException e) {
			e.printStackTrace();
			return false;
		}

		try {
			// Invoke runCodeSnippet(CodeSnippet)
			codeSnippetRunner.invokeMethod(this.jdiThread, method, arguments, ClassType.INVOKE_SINGLE_THREADED);

			// Retrieve values of local variables and put them back in the stack frame
			StackFrame stackFrame = getStackFrame();
			try {
				Iterator variables = stackFrame.visibleVariables().iterator();
				while (variables.hasNext()) {
					LocalVariable jdiVariable = (LocalVariable)variables.next();
					Field field = codeSnippetClass.fieldByName(new String(LOCAL_VAR_PREFIX) + jdiVariable.name());
					Value value = codeSnippet.getValue(field);
					stackFrame.setValue(jdiVariable, value);
				}
			} catch (AbsentInformationException e) {
				// No variables
			}
		} catch (ClassNotLoadedException e) {
			e.printStackTrace();
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (InvocationException e) {
			e.printStackTrace();
		}
		return true;
	}
}
