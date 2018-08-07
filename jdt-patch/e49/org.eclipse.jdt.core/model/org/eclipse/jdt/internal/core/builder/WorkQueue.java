/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WorkQueue {

private Set<SourceFile> needsCompileList;
private Set<SourceFile> compiledList;

public WorkQueue() {
	this.needsCompileList = new HashSet<>();
	this.compiledList = new HashSet<>();
}

public void add(SourceFile element) {
	this.needsCompileList.add(element);
}

public void addAll(SourceFile[] elements) {
	this.needsCompileList.addAll(Arrays.asList(elements));
}

public void clear() {
	this.needsCompileList.clear();
	this.compiledList.clear();
}

public void finished(SourceFile element) {
	this.needsCompileList.remove(element);
	this.compiledList.add(element);
}

public boolean isCompiled(SourceFile element) {
	return this.compiledList.contains(element);
}

public boolean isWaiting(SourceFile element) {
	return this.needsCompileList.contains(element);
}

@Override
public String toString() {
	return "WorkQueue: " + this.needsCompileList; //$NON-NLS-1$
}
}
