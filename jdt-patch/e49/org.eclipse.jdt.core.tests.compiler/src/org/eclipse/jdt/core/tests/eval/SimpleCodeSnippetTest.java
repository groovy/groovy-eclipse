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
package org.eclipse.jdt.core.tests.eval;

import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.eval.InstallException;


public class SimpleCodeSnippetTest
    extends SimpleTest {
    public char[] getCodeSnippetSource() {

        return buildCharArray(new String[] { "1 + 1" });
    }

    public static void main(String[] args)
                     throws TargetException, InstallException {

        SimpleCodeSnippetTest test = new SimpleCodeSnippetTest();
        test.runCodeSnippet();
    }

    void runCodeSnippet()
                 throws TargetException, InstallException {
        startEvaluationContext();

        char[] snippet = getCodeSnippetSource();
        INameEnvironment env = getEnv();
        this.context.evaluate(snippet, env, null, this.requestor,
                              getProblemFactory());
        stopEvaluationContext();
    }
}
