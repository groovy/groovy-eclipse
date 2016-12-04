/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test;

import java.util.Hashtable;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.testplugin.TestOptions;


public class AbstractRefactoringTestSetup extends TestSetup {

	private boolean fWasAutobuild;
	private Hashtable<String, String> fWasOptions;

	public AbstractRefactoringTestSetup(Test test) {
		super(test);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fWasOptions = JavaCore.getOptions();
		fWasAutobuild= CoreUtility.setAutoBuilding(false);

		Hashtable<String, String> options= TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "0");
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, String.valueOf(9999));

		JavaCore.setOptions(options);
		TestOptions.initializeCodeGenerationOptions();
		JavaPlugin.getDefault().getCodeTemplateStore().load();

		StringBuffer comment= new StringBuffer();
		comment.append("/**\n");
		comment.append(" * ${tags}\n");
		comment.append(" */");
		StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORCOMMENT_ID, comment.toString(), null);
	}

	protected void tearDown() throws Exception {
		if (fWasOptions!=null) {
			//Must restore options or it messes up other test running after us.
			JavaCore.setOptions(fWasOptions);
		}
		CoreUtility.setAutoBuilding(fWasAutobuild);
		/*
		 * ensure the workbench state gets saved when running with the Automated Testing Framework
         * TODO: remove when https://bugs.eclipse.org/bugs/show_bug.cgi?id=71362 is fixed
         */
		/* Not needed for JDT/UI tests right now.
		StackTraceElement[] elements=  new Throwable().getStackTrace();
		for (int i= 0; i < elements.length; i++) {
			StackTraceElement element= elements[i];
			if (element.getClassName().equals("org.eclipse.test.EclipseTestRunner")) {
				PlatformUI.getWorkbench().close();
				break;
			}
		}
		*/
		super.tearDown();
	}
}
