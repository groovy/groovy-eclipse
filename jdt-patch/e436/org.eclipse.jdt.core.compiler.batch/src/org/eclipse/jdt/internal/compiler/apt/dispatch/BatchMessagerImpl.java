/*******************************************************************************
 * Copyright (c) 2006-2009 BEA Systems, Inc. and others
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

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.batch.Main;

/**
 * An implementation of Messager that reports messages via the Compiler
 */
public class BatchMessagerImpl extends BaseMessagerImpl implements Messager {

	private final Main _compiler;
	private final BaseProcessingEnvImpl _processingEnv;

	public BatchMessagerImpl(BaseProcessingEnvImpl processingEnv, Main compiler) {
		this._compiler = compiler;
		this._processingEnv = processingEnv;
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg) {
		printMessage(kind, msg, null, null, null);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e) {
		printMessage(kind, msg, e, null, null);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element, javax.lang.model.element.AnnotationMirror)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a) {
		printMessage(kind, msg, e, a, null);

	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element, javax.lang.model.element.AnnotationMirror, javax.lang.model.element.AnnotationValue)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a, AnnotationValue v) {
		if (kind == Kind.ERROR) {
			this._processingEnv.setErrorRaised(true);
		}
		CategorizedProblem problem = createProblem(kind, msg, e, a, v);
		if (problem != null) {
			this._compiler.addExtraProblems(problem);
		}
	}
}
