/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.ui.extract;

import org.eclipse.osgi.util.NLS;

public class GroovyRefactoringMessages extends NLS {
    private static final String BUNDLE_NAME = "org.codehaus.groovy.eclipse.refactoring.ui.extract.groovyRefactoring"; //$NON-NLS-1$
	public static String GroovyRefactoringAction_No_Module_Node;
	public static String GroovyRefactoringAction_Syntax_Errors;
	public static String InlineMethodInfo_Multiple_Methodbodies;
	public static String InlineMethodInfo_Multiple_Returns_found;
	public static String InlineMethodInfo_No_Method_Call_Found;
	public static String InlineMethodInfo_No_Methodbody_Found;
	public static String InlineMethodInfo_No_Methodcall_found;
	public static String InlineMethodWizard_All_Invocations;
	public static String InlineMethodWizard_Delete_Method;
	public static String InlineMethodWizard_Inline;
	public static String InlineMethodWizard_Only_Selected_Invocation;
	public static String RenameLocalInfo_VariableAlreadyExists;
	public static String RenameLocalInfo_NoLocalVariableInSelection;
	public static String RenameClassInfo_RenameNotPossible;
	public static String RenameClassInfo_RenameAliasNotPossible;
	public static String RenameClassInfo_ClassNameAlreadyExists;
	public static String ExtractMethodInfo_NoExtractionOfConstructorCallinConstructor;
	public static String ExtractMethodInfo_NoStatementSelected;
	public static String ExtractMethodInfo_ToMuchReturnValues;
	public static String ExtractMethodWizard_LB_AcessModifier;
	public static String ExtractMethodWizard_LB_BTN_Down;
	public static String ExtractMethodWizard_LB_BTN_Edit;
	public static String ExtractMethodWizard_LB_BTN_UP;
	public static String ExtractMethodWizard_LB_Col_Name;
	public static String ExtractMethodWizard_LB_Col_Type;
	public static String ExtractMethodWizard_LB_MethodSignaturePreview;
	public static String ExtractMethodWizard_LB_NewMethodName;
	public static String ExtractMethodWizard_LB_Parameters;
	public static String ExtractMethodWizard_MethodCall;
	public static String ExtractMethodWizard_MethodNameAlreadyExists;
	public static String ExtractMethodWizard_DefaultMethodName;
	public static String ExtractMethodWizard_DuplicateVariableName;
	public static String GroovyConventions_NameLowChar;
	public static String GroovyConventions_NameUpperChar;
	public static String GroovyConventions_IllegalName;
	public static String GroovyConventions_ProvideName;

	public static String RenameMethodFileSelectionPage_LB_CodePreview;
	public static String RenameMethodFileSelectionPage_LB_DefinitveCandidates;
	public static String RenameMethodFileSelectionPage_LB_AmbiguousCandidates;
	public static String RenameMethod_VariableAlreadyExists;
	public static String RenameMethod_DefaultParamsUsed;

	public static String RenameClassRefactoring;
	public static String RenameFieldRefactoring;
	public static String RenameMethodRefactoring;
	public static String RenameLocalRefactoring;
	public static String ExtractMethodRefactoring;
	public static String InlineMethodRefactoring;

	public static String FormattingAction_Syntax_Errors;
	public static String FormattingAction_RootNode_Errors;

	public static String CandidateSelection_Title;
	public static String CandidateSelection_Message;
	public static String CandidateSelection_SelectionOK;
	public static String CandidateSelection_SelectionNotOK;

	public static String JavaClassUpdateRefactoring;
	public static String JavaFieldUpdateRefactoring;
	public static String JavaMethodUpdateRefactoring;
	public static String JavaSyntheticFieldUpdateRefactoring;

	public static String ParticipantDispatcherError;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, GroovyRefactoringMessages.class);
	}

	private GroovyRefactoringMessages() {
	}
}
