/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.grails.ide.eclipse.groovy.debug.core.evaluation;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.MetaClass;
import groovy.lang.Script;
import groovy.util.Proxy;

import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.JDIThisVariable;
import org.grails.ide.eclipse.groovy.debug.core.GroovyDebugCoreActivator;

import com.sun.jdi.InvocationException;

/**
 * Evaluates a Groovy snippet in the context
 * of a stepping application.  This class creates a
 * Groovy script based on the snippet.  The script's {@link MetaClass}and {@link Binding} are set to a JDI variant that communicates 
 * with the application being debugged.
 * @author Andrew Eisenberg
 * @since 2.5.1
 */
public class GroovyJDIEvaluator {

    public static final String SCRIPT_FILE_NAME = "____Eval.groovy";
    public static final String SCRIPT_CLASS_NAME = "____Eval";

    private class GroovyEvaluationResult implements IEvaluationResult {
    	
    	private final IJavaValue value;
    	private final DebugException exception;
    	private final String snippet;
    	private final IJavaThread thread;
        private String completeSnippet;
    	
    	GroovyEvaluationResult(IJavaValue value,
    			DebugException exception, String snippet, String completeSnippet, IJavaThread thread) {
    		super();
    		this.value = value;
    		this.exception = exception;
    		this.snippet = snippet;
            this.completeSnippet = completeSnippet;
    		this.thread = thread;
    	}
    
    	public IJavaValue getValue() {
    		return value;
    	}
    
    	public boolean hasErrors() {
    		return exception != null;
    	}
    
    	public Message[] getErrors() {
    		return exception != null ? new Message[] { new Message("(Groovy) " + exception.getLocalizedMessage(), -1), new Message("Evaluation snippet:\n" + completeSnippet, -1) } : new Message[0];
    	}
    
    	public String[] getErrorMessages() {
    	    if (exception == null) {
    	        return new String[0];
    	    }
    	    
    	    if (exception.getStatus().getException() instanceof InvocationException) {
    	        return new String[] { "(Groovy) " + ((InvocationException) exception.getStatus().getException()).exception().toString(), "Evaluation snippet:\n" + completeSnippet  };
    	    }
    		return new String[] { "(Groovy) " + exception.getLocalizedMessage(), "Evaluation snippet:\n" + completeSnippet };
    	}
    
    	public String getSnippet() {
    		return snippet;
    	}
    
    	public DebugException getException() {
    		return exception;
    	}
    
    	public IJavaThread getThread() {
    		return thread;
    	}
    
    	public IEvaluationEngine getEvaluationEngine() {
    		return null;
    	}
    
    	public boolean isTerminated() {
    		return false;
    	}
    	
    }

    private final IJavaProject javaProject;
    private String packageName;
    
    public GroovyJDIEvaluator(IJavaProject javaProject, IJavaDebugTarget target) {
        super();
        this.javaProject = javaProject;
    }

	public void evaluate(final String snippet, final IJavaObject object, final IJavaStackFrame frame, 
	        final IEvaluationListener listener, final int evaluationDetail, final boolean hitBreakpoints) throws CoreException {
        final IJavaThread thread = (IJavaThread) frame.getThread();
        final JDITargetDelegate delegate = new JDITargetDelegate((IJavaDebugTarget) thread.getDebugTarget(), thread);
        // really shouldn't queue if thread is already in the middle of an explicit evaluation
        thread.queueRunnable(new Runnable() {
            public void run() {
                
                IEvaluationRunnable evalRunnable = new IEvaluationRunnable() {
                    
                    public void run(IJavaThread thread, IProgressMonitor monitor)
                            throws DebugException {
                        performEvaluate(snippet, frame, listener, delegate, evaluationDetail, hitBreakpoints);
                    }
                };
                // finally, run the script
                try {
                    thread.runEvaluation(evalRunnable, null, evaluationDetail, hitBreakpoints);
                } catch (DebugException e) {
                    GroovyDebugCoreActivator.log(e);
                }

            }
        });
    }

    protected void performEvaluate(String snippet, IJavaStackFrame frame,
            IEvaluationListener listener,
            JDITargetDelegate delegate, int evaluationDetail, boolean hitBreakpoints) {
        Object result = null;
        Throwable thrownException = null;
        String completeSource = null;
        try {
            completeSource = createEvaluationSourceFromSnippet(snippet, frame);
            JDIGroovyClassLoader loader = createClassLoader();
            final Script script = convertSnippetToScript(completeSource, loader);
    		script.setMetaClass(new JDIMetaClass(delegate.getThis(), delegate));
    		script.setBinding(createBinding(frame, delegate));
    		script.getBinding().setMetaClass(script.getMetaClass());
    	    delegate.initialize(loader, packageName + SCRIPT_CLASS_NAME);
    	    
    	    result = script.run();
    	} catch (Exception e) {
    	    // only print to sysout when this is an explicit evaluation
    	    if (DebugEvent.EVALUATION == evaluationDetail) {
        	    System.out.println("Exception during evaluation:");
        	    e.printStackTrace();
    	    }
    	    if (e.getCause() instanceof DebugException) {
    	        thrownException = e.getCause();
    	    } else {
    	        thrownException = e;
    	    }
    	    
    	    thrownException = new Exception("(Groovy) Complete snippet:\n" + completeSource, e);
        } finally {
    	    try {
        	    IEvaluationResult evalResult = createEvalResult(snippet, completeSource, result, delegate, thrownException, evaluationDetail);
        	    if (JDIDebugPlugin.getDefault() != null) {
        	        listener.evaluationComplete(evalResult);
        	    }
    	    } finally {
        	    // replace original metaclass
        	    delegate.cleanup();
    	    }
    	}
    }

    private String createEvaluationSourceFromSnippet(String snippet, IJavaStackFrame frame) throws CoreException {
		StringBuffer sb = new StringBuffer();
		sb.append("/////start\n");
		
		IJavaReferenceType jdiType = frame.getReferenceType();
		IType iType = JavaDebugUtils.resolveType(jdiType);
		
		// could be a closure type that doesn't exist in source
		if (iType != null && !iType.exists() && iType.getParent().getElementType() == IJavaElement.TYPE) {
		    iType = (IType) iType.getParent();
		}
		
        if (iType != null && !iType.isInterface()) {
            ITypeRoot root = iType.getTypeRoot();
            if (root instanceof ICompilationUnit) { 
                // really, a GroovyCompilationUnit
                ICompilationUnit unit = (ICompilationUnit) root;
                
                // package statement
                IPackageDeclaration[] pDecls = unit.getPackageDeclarations();
                if (pDecls.length > 0) {
                    sb.append("package " + pDecls[0].getElementName() + ";\n");
                    packageName = pDecls[0].getElementName() + ".";
                } else {
                    packageName = "";
                }
                
                // imports
                IImportContainer container = unit.getImportContainer();
                if (container != null && container.exists()) {
                    IJavaElement[] children = container.getChildren();
                    for (int j = 0; j < children.length; j++) {
                        IImportDeclaration importChild = (IImportDeclaration) children[j];
                        sb.append("import ");
                        if (Flags.isStatic(importChild.getFlags())) {
                            sb.append("static ");
                        }
                        sb.append(importChild.getElementName());
                        if (importChild.isOnDemand() && ! (importChild.getElementName().endsWith(".*"))) {
                            sb.append(".*");
                        }
                        sb.append(";\n");
                    }
                }
                
                // types...create stubs for the types just so that they can be instantiated and referenced
                IType[] allTypes = unit.getAllTypes();
                for (IType otherType : allTypes) {
                    if (!otherType.equals(iType)) {
                        if (otherType.isInterface()) {
                            sb.append("interface ");
                        } else if (otherType.isAnnotation()) {
                            // probably don't need this
                            sb.append("@interface ");
                        } else if (otherType.isEnum()) {
                            sb.append("enum ");
                        } else {
                            sb.append("class ");
                        }
                        
                        // use '$' so that inner classes can be remembered
                        String qualifiedTypeName = otherType.getFullyQualifiedName('$');
                        int dotIndex = qualifiedTypeName.lastIndexOf('.')+1;
                        String simpleName = qualifiedTypeName.substring(dotIndex);
                        sb.append(simpleName + "{ }\n");
                    }
                }
            }
        }
		
		sb.append(snippet);
		sb.append("\n/////end");
		return sb.toString();
	}
	
	private IEvaluationResult createEvalResult(String snippet, String completeSource, Object result, JDITargetDelegate delegate, Throwable thrownException, int evaluationDetail) {
	    if (thrownException == null) {
	        try {
    	        IJavaValue jdiResult;
        	    if (result instanceof Proxy) {
        	        jdiResult = (IJavaValue) ((Proxy) result).getAdaptee();
        	    } else if (result instanceof IJavaValue) {
        	        jdiResult = (IJavaValue) result;
        	    } else if (result == null) {
                        jdiResult = delegate.getTarget().nullValue();
                } else {
                    // might be a constant expression
                    jdiResult = delegate.toJDIObject(result);
                }
                return new GroovyEvaluationResult(jdiResult, null, snippet, completeSource, delegate.getThread());
            } catch (DebugException de) {
                thrownException = de;
            }
	    }
	    
	    // if we get to this point, then we have an exception
	    DebugException debugException;
	    if (! (thrownException instanceof DebugException)) {
	        debugException = new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), 
	                "An exception occurred durring evaluation.", thrownException));
	    } else {
	        debugException = (DebugException) thrownException;
	    }
	    
	    if (DebugEvent.EVALUATION == evaluationDetail) {
	        // only log events from explicitly invoked evaluations
	        GroovyDebugCoreActivator.log(debugException);
	    }
	    return new GroovyEvaluationResult(delegate.getTarget().newValue("See error log: " + thrownException.getLocalizedMessage()), debugException, snippet, completeSource, delegate.getThread());
	}

	private Binding createBinding(IJavaStackFrame frame, JDITargetDelegate delegate) throws DebugException {
		IVariable[] vars = frame.getVariables();
		JDIBinding binding = new JDIBinding(delegate, frame);
		for (int i = 0; i < vars.length; i++) {
		    if (! (vars[i] instanceof JDIThisVariable)) {
		        binding.setVariable(vars[i].getName(), delegate.createProxyFor((IJavaValue) vars[i].getValue()));
		    }
		}
		
		// add a JDIComparator for invoking comparisons
		JDIComparator comparator = new JDIComparator(delegate);
		binding.setProperty("__comparator", comparator);
		binding.markAsInitialized();
		return binding;
	}

	private Script convertSnippetToScript(final String completeSource, JDIGroovyClassLoader loader) throws CoreException {
	    GroovyCodeSource gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
            public GroovyCodeSource run() {
                return new GroovyCodeSource(completeSource, getCompilationUnitName(), /*GroovyShell.DEFAULT_CODE_BASE*/ "/groovy/shell");
            }
        });
	    return InvokerHelper.createScript(loader.parseClass(gcs, false), new Binding());
	}

	/**
	 * Must create a separate, unparented class loader with the same classpath as the
	 * debugged application for 2 reasons:
	 * 1. So that all metaclasses we set do not affect the types loaded by
	 * other class loaders
	 * 2. So that we can load classes from the debugged application
	 * 
	 * However, if this debugged application does something funky with its classpath (eg as in grails), 
	 * then we will not catch it here.
	 * @return
	 * @throws CoreException
	 */
    private JDIGroovyClassLoader createClassLoader() throws CoreException {
        JDIGroovyClassLoader groovyLoader = new JDIGroovyClassLoader();
        IJavaProject currentProject = this.javaProject;
        addClasspathEntries(groovyLoader, currentProject, true);
        return groovyLoader;
    }

    private void addClasspathEntries(JDIGroovyClassLoader groovyLoader,
            IJavaProject currentProject, boolean includeAll) throws JavaModelException,
            CoreException {
        IClasspathEntry[] entries = currentProject.getResolvedClasspath(true);
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        for (int i = 0; i < entries.length; i++) {
            if (!includeAll && !entries[i].isExported()) {
                continue;
            }
            switch (entries[i].getEntryKind()) {
                case IClasspathEntry.CPE_LIBRARY:
                    try {
                        groovyLoader.addURL(entries[i].getPath().toFile().toURL());
                    } catch (MalformedURLException e) {
                        throw new CoreException(new Status(IStatus.ERROR, GroovyDebugCoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
                    }
                    break;
                    
                case IClasspathEntry.CPE_SOURCE:
                    IPath outLocation = entries[i].getOutputLocation();
                    if (outLocation != null) {
                        // using non-default output location
                        try {
                            groovyLoader.addURL(workspaceLocation.append(outLocation).toFile().toURL());
                        } catch (MalformedURLException e) {
                            throw new CoreException(new Status(IStatus.ERROR, GroovyDebugCoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
                        }
                    }
                    break;
                    
                case IClasspathEntry.CPE_PROJECT:
                    IProject otherProject = ResourcesPlugin.getWorkspace().getRoot().getProject(entries[i].getPath().lastSegment());
                    if (otherProject.isAccessible()) {
                        IJavaProject otherJavaProject = JavaCore.create(otherProject);
                        addClasspathEntries(groovyLoader, otherJavaProject, false);
                    }
                    break;
                    
                default:
                    break;
            }
        }
        
        // now add default out location
        IPath outLocation = currentProject.getOutputLocation();
        if (outLocation != null) {
            try {
                groovyLoader.addURL(workspaceLocation.append(outLocation).toFile().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCompilationUnitName() {
        return SCRIPT_FILE_NAME;
    }
}
