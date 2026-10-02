/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.builder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public class ProblemFactory extends DefaultProblemFactory {

static Map<Locale, ProblemFactory> factories = new HashMap<>();

private ProblemFactory(Locale locale) {
	super(locale);
}

public static ProblemFactory getProblemFactory(Locale locale) {
	ProblemFactory factory = factories.get(locale);
	if (factory == null)
		factories.put(locale, factory = new ProblemFactory(locale));
	return factory;
}
}
