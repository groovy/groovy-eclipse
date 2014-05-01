/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.builder;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Will pass on status to another progress monitor.
 * 
 * @author Andy Clement
 */
public class DelegatingProgressMonitor implements IProgressMonitor {

	public IProgressMonitor delegate;

	public DelegatingProgressMonitor(IProgressMonitor delegate) {
		this.delegate = delegate;
	}
	
	public void beginTask(String name, int totalWork) {
		this.delegate.beginTask(name, totalWork);
	}

	public void done() {
		this.delegate.done();
	}

	public void internalWorked(double work) {
		this.internalWorked(work);
	}

	public boolean isCanceled() {
		return this.delegate.isCanceled();
	}

	public void setCanceled(boolean value) {
		this.delegate.setCanceled(value);
	}

	public void setTaskName(String name) {
		this.delegate.setTaskName(name);
	}

	public void subTask(String name) {
		this.delegate.subTask(name);
	}

	public void worked(int work) {
		this.delegate.worked(work);
	}

}
