/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.core.extract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ASTNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.BinaryExpressionFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.FragmentVisitor;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.MethodCallFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.PropertyExpressionFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.SimpleExpressionASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindAllOccurrencesVisitor;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode.VisitKind;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractLocalDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 *
 * @author Andrew Eisenberg
 * @created May 10, 2010
 */
public class ExtractGroovyLocalRefactoring extends Refactoring {

    private static final String[] KNOWN_METHOD_NAME_PREFIXES= { "get", "is", "to", "set" }; //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$

    private static final String ATTRIBUTE_REPLACE = "replace"; //$NON-NLS-1$

    private IASTFragment selectedExpression;

    private GroovyCompilationUnit unit;

    private ModuleNode module;
    private int start = -1, length = -1;

    private String localName;

    private boolean replaceAllOccurrences;

    private CompilationUnitChange change;

    private Map<IASTFragment, List<IASTFragment>> allParentStack;

    private List<IASTFragment> matchingFragments;

    public ExtractGroovyLocalRefactoring(JavaRefactoringArguments arguments,
            RefactoringStatus status) {
    }


    public ExtractGroovyLocalRefactoring(
            GroovyCompilationUnit unit, int offset, int length) {
        this.unit = unit;
        this.start = offset;
        this.length = length;
    }



    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        try {
            pm.beginTask("", 6); //$NON-NLS-1$

            IASTFragment expr = getSelectedFragment();
            int trimmedLength = expr.getTrimmedLength(unit);
            int exprLength = expr.getLength();
            // problem is that some expressions include whitespace in the end of
            // their sloc.
            // need to handle this case.
            // the selected length must be somewhere >= the trimmed (no
            // whitespace) length and <= the non-trimeed (w/ whitespace) length
            if (expr == null || expr.getStart() != start || length > exprLength || length < trimmedLength) {
                return RefactoringStatus.createFatalErrorStatus("Must select a full expression", JavaStatusContext.create(unit,
                        new SourceRange(start, length)));
            }

            RefactoringStatus result = Checks.validateModifiesFiles(ResourceUtil.getFiles(new ICompilationUnit[] { unit }),
                    getValidationContext());
            if (result.hasFatalError()) {
                return result;
            }

            module = unit.getModuleNode();
            if (module == null) {
                result.addFatalError("Cannot build module node for file.  Possible syntax error.");
                return result;
            }

            result.merge(checkSelection(new SubProgressMonitor(pm, 3)));
            if (!result.hasFatalError() && isLiteralNodeSelected()) {
                replaceAllOccurrences = false;
            }
            return result;

        } finally {
            pm.done();
        }
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
        try {
            pm.beginTask(RefactoringCoreMessages.ExtractTempRefactoring_checking_preconditions, 4);

            RefactoringStatus result = new RefactoringStatus();
            change = doCreateChange(result, new SubProgressMonitor(pm, 2));

            if (getExcludedVariableNames().contains(localName)) {
                result.addWarning(Messages.format(RefactoringCoreMessages.ExtractTempRefactoring_another_variable,
                        BasicElementLabels.getJavaElementName(localName)));
            }

            result.merge(checkMatchingFragments());

            change.setKeepPreviewEdits(true);

            return result;
        } finally {
            pm.done();
        }
    }

    /**
     * Finds all variable names that are currently in use
     * in the scope
     */
    private Set<String> getExcludedVariableNames() {
        List<IASTFragment> parentStack = getParentStack(getSelectedFragment());
        Set<String> usedNames = new HashSet<String>();
        for (IASTFragment astNode : parentStack) {
            VariableScope scope;
            if (astNode instanceof BlockStatement) {
                scope = ((BlockStatement) astNode).getVariableScope();
            } else if (astNode instanceof MethodNode) {
                scope = ((MethodNode) astNode).getVariableScope();
            } else if (astNode instanceof ClosureExpression) {
                scope = ((ClosureExpression) astNode).getVariableScope();
            } else {
                scope = null;
            }
            if (scope != null) {
                Iterator<Variable> declared = scope.getDeclaredVariablesIterator();
                while (declared.hasNext()) {
                    usedNames.add(declared.next().getName());
                }
            }
        }

        // now check to see if the selected expression itself is a keyword
        String selectedText = getTextAt(getSelectedFragment().getStart(), getSelectedFragment().getEnd());
        if (JavaConventionsUtil.validateIdentifier(selectedText, null) == Status.OK_STATUS) {
            usedNames.add(selectedText);
        }

        return usedNames;
    }

    /**
     * Ensure that the matching expressions are not LHS of an assignment
     *
     * @return
     * @throws JavaModelException
     */
    private RefactoringStatus checkMatchingFragments() {
        RefactoringStatus result = new RefactoringStatus();
        List<IASTFragment> matchingExprs = getMatchingExpressions();
        for (IASTFragment matchingExpr : matchingExprs) {
            if (isDeclaration(matchingExpr)) {
                String msg = "The selected expression is a declaration.  Extracting may cause an error.";
                result.addError(msg, JavaStatusContext.create(unit, new SourceRange(matchingExpr.getStart(), matchingExpr
                        .getLength())));
            }
            if (isLeftValue(matchingExpr)) {
                String msg = RefactoringCoreMessages.ExtractTempRefactoring_assigned_to;
                result.addWarning(msg, JavaStatusContext.create(unit, new SourceRange(matchingExpr.getStart(), matchingExpr
                        .getLength())));
            }
        }
        return result;
    }

    private boolean isDeclaration(IASTFragment matchingExpr) {
        Expression associatedExpression = matchingExpr.getAssociatedExpression();
        if (associatedExpression instanceof VariableExpression) {
            if (((VariableExpression) associatedExpression).getAccessedVariable() == associatedExpression) {
                return true;
            }
        }
        return false;
    }

    private boolean isLeftValue(IASTFragment frag) {
        return frag.kind() == ASTFragmentKind.BINARY && ((BinaryExpressionFragment) frag).getToken().getType() == Types.ASSIGN;
    }

    private List<IASTFragment> getMatchingExpressions() {
        if (matchingFragments != null) {
            return matchingFragments;
        }
        IASTFragment origFragment = getSelectedFragment();
        List<IASTFragment> parentStack = getParentStack(origFragment);
        AnnotatedNode limitTo = null;
        for (IASTFragment fragment : parentStack) {
            ASTNode astNode = fragment.getAssociatedNode();
            if (astNode instanceof FieldNode) {
                limitTo = (FieldNode) astNode;
                break;
            }
            if (astNode instanceof MethodNode) {
                limitTo = (MethodNode) astNode;
                break;
            }
            if (astNode instanceof ClassNode) {
                limitTo = (ClassNode) astNode;
                break;
            }
            if (astNode instanceof ModuleNode) {
                limitTo = ASTNodeCompatibilityWrapper.getScriptClassDummy((ModuleNode) astNode);
            }
        }
        if (replaceAllOccurrences) {
            FindAllOccurrencesVisitor v = new FindAllOccurrencesVisitor(unit.getModuleNode(), limitTo);
            matchingFragments = v.findOccurrences(origFragment);
        } else {
            matchingFragments = Collections.singletonList(origFragment);
        }
        return matchingFragments;
    }



    private CompilationUnitChange doCreateChange(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        CompilationUnitChange newChange = new CompilationUnitChange("Extract Local Variable", unit);
        try {
            pm.beginTask(RefactoringCoreMessages.ExtractTempRefactoring_checking_preconditions, 1);
            newChange.setEdit(new MultiTextEdit());
            createTempDeclaration(newChange, status);
            if (!status.hasFatalError()) {
                addReplaceExpressionWithTemp(newChange);
            }
        } finally {
            pm.done();
        }
        return newChange;
    }

    /**
     * Replace all occurrences with the new variable name
     *
     * @param newChange
     */
    private void addReplaceExpressionWithTemp(CompilationUnitChange newChange) {
        List<IASTFragment> matchingExpressions;
        if (replaceAllOccurrences) {
            matchingExpressions = getMatchingExpressions();
        } else {
            matchingExpressions = Collections.singletonList(getSelectedFragment());
        }
        for (IASTFragment matchingExpr : matchingExpressions) {
            TextEditGroup group = new TextEditGroup(RefactoringCoreMessages.ExtractTempRefactoring_replace_occurrences);
            ReplaceEdit edit = new ReplaceEdit(matchingExpr.getStart(), matchingExpr.getLength(), localName);
            group.addTextEdit(edit);
            newChange.addChangeGroup(new TextEditChangeGroup(newChange, group));
            newChange.addEdit(edit);
        }
    }

    private void createTempDeclaration(CompilationUnitChange newChange, RefactoringStatus status) {
        List<IASTFragment> matchingExpressions = replaceAllOccurrences ? getMatchingExpressions() : Collections
                .singletonList(getSelectedFragment());

        int insertLoc = insertAt(matchingExpressions, status);

        if (insertLoc == -1 && !status.hasFatalError()) {
            status.addFatalError("Could not find a suitable extraction location", createContext());
        }
        if (status.hasFatalError()) {
            return;
        }

        int lineStart = findLineStart(insertLoc);

        // prefix the declaration with the same whitespace as the next line
        String prefix = getTextAt(lineStart, insertLoc);

        TextEditGroup group = new TextEditGroup(RefactoringCoreMessages.ExtractTempRefactoring_declare_local_variable);
        InsertEdit edit = new InsertEdit(lineStart, createExpressionText(prefix, status));
        group.addTextEdit(edit);
        newChange.addChangeGroup(new TextEditChangeGroup(newChange, group));
        newChange.addEdit(edit);
    }


    private String getTextAt(int start, int end) {
        return String.valueOf(CharOperation.subarray(unit.getContents(), start, end));
    }

    /**
     * Based on all of the matching expressions, determine where to insert the
     * declaration.
     *
     * @param matchingExpressions
     * @param decl
     * @param status
     * @return
     */
    private int insertAt(List<IASTFragment> matchingExpressions, RefactoringStatus status) {
        // find the first matching expression.
        // determine the start of its associated statement
        // must ensure that it is in the correct code block. uggh
        // insert at position before statement

        // find the longest matching parent prefix
        List<IASTFragment>[] parentsStack = new List[matchingExpressions.size()];
        int i = 0;
        IASTFragment firstExpression = null;
        for (IASTFragment matchingExpr : matchingExpressions) {
            if (firstExpression == null || matchingExpr.getStart() < firstExpression.getStart()) {
                firstExpression = matchingExpr;
            }
            parentsStack[i++] = getParentStack(matchingExpr);
        }
        IASTFragment[] commonPrefix = getLongestStackPrefix(parentsStack);

        if (commonPrefix.length == 0) {
            status.addFatalError("Could not find a common root for extracted occurrences.", createContext());
            return -1;
        }

        // now we have the common prefix and the first expression.
        // need to find the first statement in the array.
        // if it is not a block statement, then we know to insert immediately
        // before
        // if it is a block statement, then take the parent stack of the first
        // statement
        // and walk up it to find the statement immediately under the block and
        // insert there
        Statement firstCommonStatement = null;

        // it's possible there is no statement here, like if inside a field or
        // parameter initializer.
        // in this case, we should fail.
        for (IASTFragment fragment : commonPrefix) {
            if (fragment.getAssociatedNode() instanceof Statement) {
                firstCommonStatement = (Statement) fragment.getAssociatedNode();
                break;
            }
        }

        int insertLoc;

        if (firstCommonStatement instanceof BlockStatement) {
            // if the first common statement is a block statement,
            // then we actually want to place the declaration inside the
            // block. To figure out where, we look at the firstParentStack
            // and walk to determine where its statement is inside the
            // containing block statement.
            insertLoc = -1;
            List<IASTFragment> firstParentStack = getParentStack(firstExpression);
            for (int j = 1; j < firstParentStack.size(); j++) {
				if (firstParentStack.get(j).getAssociatedNode() == firstCommonStatement) {
                    insertLoc = firstParentStack.get(j - 1).getStart();
                    break;
                }
            }
        } else if (firstCommonStatement != null) {
            insertLoc = firstCommonStatement.getStart();
        } else { // is null
            insertLoc = -1;
        }

        return insertLoc;
    }

    private int findLineStart(int insertLoc) {
        char[] contents = unit.getContents();
        while (insertLoc >= 0 && contents[insertLoc] != '\n' && contents[insertLoc] != '\r') {
            insertLoc--;
        }
        return insertLoc;
    }

    private IASTFragment[] getLongestStackPrefix(List<IASTFragment>[] parentsStack) {
        int length = -1;
        if (parentsStack.length == 0) {
            return new IASTFragment[0];
//        } else if (parentsStack.length == 1) {
//            return (IASTFragment[]) parentsStack[0].toArray(new IASTFragment[0]);
        }
        int minArrayLength = parentsStack[0].size();
        for (int i = 1; i < parentsStack.length; i++) {
            minArrayLength = Math.min(minArrayLength, parentsStack[i].size());
        }

        for (int i = 0; i < minArrayLength; i++) {
            if (!allStacksEqual(parentsStack, i)) {
                break;
            }
            length++;
        }
        if (length <= -1) {
            return new IASTFragment[0];
        }
        return getStackPrefix(parentsStack[0], length);
    }

    /**
     * Creates a sub-array of <code>length</code> elements
     */
    private IASTFragment[] getStackPrefix(List<IASTFragment> stack, int length) {
        IASTFragment[] array = new IASTFragment[length + 1];
        int i = 0;
        while (i <= length) {
            array[length - i] = stack.get(stack.size() - 1 - i);
            i++;
        }
        return array;
    }

    /**
     * Checks to see if the ith element of all stacks are ==
     *
     * @param parentsStack
     * @param i
     * @return true iff all elements are equal in the ith position
     */
    private boolean allStacksEqual(List<IASTFragment>[] parentsStack, int i) {
        IASTFragment candidate = parentsStack[0].get(parentsStack[0].size() - 1 - i);
        for (List<IASTFragment> stack : parentsStack) {
			if (stack.get(stack.size() - 1 - i).getAssociatedNode() != candidate.getAssociatedNode()) {
                return false;
            }
        }
        return true;
    }

    private List<IASTFragment> getParentStack(IASTFragment expr) {
        if (allParentStack == null) {
            allParentStack = new HashMap<IASTFragment, List<IASTFragment>>();
        } else if (allParentStack.containsKey(expr)) {
            return allParentStack.get(expr);
        }
        FindSurroundingNode find = new FindSurroundingNode(new Region(expr), VisitKind.PARENT_STACK);
        find.doVisitSurroundingNode(module);
        List<IASTFragment> parentStack = new ArrayList<IASTFragment>(find.getParentStack());
        Collections.reverse(parentStack);
        allParentStack.put(expr, parentStack);
        return parentStack;
    }

    private boolean isLiteralNodeSelected() throws JavaModelException {
        ASTNode expr = getSelectedFragment().getAssociatedExpression();
        if (expr == null) {
            return false;
        }
        if (expr instanceof ConstantExpression) {
            ConstantExpression constExpr = (ConstantExpression) expr;
            return constExpr.isEmptyStringExpression() || constExpr.isFalseExpression() || constExpr.isNullExpression()
                    || constExpr.isTrueExpression() || constExpr.getValue() instanceof Number
                    || unit.getContents()[constExpr.getStart()] == '"';
        } else {
            return false;
        }
    }


    private String createExpressionText(String prefix, RefactoringStatus status) {
        StringBuilder sb = new StringBuilder();
        sb.append("def ").append(localName).append(" = ").append(
                getTextAt(getSelectedFragment().getStart(), getSelectedFragment().getEnd()));
        IDocument doc = new Document(sb.toString());
        DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(doc, new FormatterPreferences(unit), 0);
        try {
            formatter.format().apply(doc);
        } catch (MalformedTreeException e) {
            GroovyCore.logException("Exception during extract local variable refactoring", e);
            status.addFatalError(e.getMessage(), createContext());
        } catch (BadLocationException e) {
            GroovyCore.logException("Exception during extract local variable refactoring", e);
            status.addFatalError(e.getMessage(), createContext());
        }
        return prefix + doc.get();
    }

    private RefactoringStatus checkSelection(IProgressMonitor pm) throws JavaModelException {
        try {
            pm.beginTask("", 2); //$NON-NLS-1$

            IASTFragment selectedExpression = getSelectedFragment();

            if (selectedExpression == null) {
                String message = RefactoringCoreMessages.ExtractTempRefactoring_select_expression;
                return RefactoringStatus.createFatalErrorStatus(message, createContext());
            }
            pm.worked(1);

            RefactoringStatus result= new RefactoringStatus();
            result.merge(checkExpression());
            if (result.hasFatalError())
                return result;
            pm.worked(1);

            return result;
        } finally {
            pm.done();
        }
    }

    private RefactoringStatus checkExpression() throws JavaModelException {
        RefactoringStatus result= new RefactoringStatus();
        IASTFragment selectedExpression = getSelectedFragment();
        result.merge(checkExpressionFragmentIsRValue(selectedExpression));
        if(result.hasFatalError())
            return result;

        if ((selectedExpression instanceof ConstantExpression) && ((ConstantExpression) selectedExpression).isNullExpression()) {
            result.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.ExtractTempRefactoring_null_literals));
        }

        return result;
    }

    private RefactoringStatus checkExpressionFragmentIsRValue(IASTFragment fragment) throws JavaModelException {
        if (isDeclaration(fragment)) {
            return RefactoringStatus.createFatalErrorStatus("Target expression is a variable declaration.  Cannot extract.");
        }
        if (isLeftValue(fragment)) {
            return RefactoringStatus.createFatalErrorStatus("Target expression is target of an assignment.  Cannot extract.");
        }
        return new RefactoringStatus();
    }


    private RefactoringStatusContext createContext() {
        IJavaElement elt;
        try {
            elt = unit.getElementAt(start);
            if (elt instanceof IMember) {
                return JavaStatusContext.create((IMember) elt);
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Error finding refactoring context", e);
        }
        return null;
    }


    private IASTFragment getSelectedFragment() {
        if(selectedExpression != null) {
            return selectedExpression;
        }
        FindSurroundingNode finder = new FindSurroundingNode(new Region(start, length), VisitKind.SURROUNDING_NODE);
        IASTFragment fragment = finder.doVisitSurroundingNode(unit.getModuleNode());
        if (ASTFragmentKind.isExpressionKind(fragment)) {
            selectedExpression = fragment;
        }
        return selectedExpression;
    }


    // !! similar to ExtractTempRefactoring equivalent
    public String getSignaturePreview() throws JavaModelException {
        String space= " "; //$NON-NLS-1$
        return "def" + space + localName;
    }

    public void setReplaceAllOccurrences(boolean replaceAllOccurrences) {
        this.replaceAllOccurrences = replaceAllOccurrences;
    }

    public boolean isReplaceAllOccurrences() {
        return replaceAllOccurrences;
    }

    public RefactoringStatus checkLocalNameOnChange(String newName) {
        Assert.isTrue(newName.equals(getLocalName()));
        String selectedText = getTextAt(getSelectedFragment().getStart(), getSelectedFragment().getEnd());
        if (newName.equals(selectedText)) {
            return RefactoringStatus.createFatalErrorStatus("Extracted variable name must be different from original text");
        }
        return Checks.checkTempName(newName, unit);
    }

    public String[] guessLocalNames() {
		String text = getBaseNameFromExpression(getSelectedFragment());
        return NamingConventions.suggestVariableNames(NamingConventions.VK_LOCAL, NamingConventions.BK_NAME, text, unit
                .getJavaProject(), 0, null, true);
    }

	class GuessBaseNameVisitor extends FragmentVisitor {

		StringBuilder sb = new StringBuilder();

		String getGuessedName() {
			String name = sb.toString();

			if (name.length() == 0) {
				name = "local";
			} else {
				for (int i = 0; i < KNOWN_METHOD_NAME_PREFIXES.length; i++) {
					String curr = KNOWN_METHOD_NAME_PREFIXES[i];
					if (name.startsWith(curr)) {
						if (name.equals(curr)) {
							return "local"; // don't suggest 'get' as variable
											// name
						} else if (Character.isUpperCase(name.charAt(curr.length()))) {
							return name.substring(curr.length());
						}
					}
				}

				try {
					Integer.parseInt(name);
					name = "_" + name;
				} catch (NumberFormatException e) {
					// ignore
				}

            }
            // make first char lower case
            if (name.length() > 0) {
                name = "" + Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }
			return name;
		}

		@Override
		public boolean visit(BinaryExpressionFragment fragment) {
			String nextPart = nameFromExpression(fragment.getAssociatedExpression());
			if (nextPart != null) {
				sb.append(nextPart);
			}
			return true;
		}

		@Override
		public boolean visit(MethodCallFragment fragment) {
			String nextPart = nameFromExpression(fragment.getAssociatedExpression());
			if (nextPart != null) {
				sb.append(nextPart);
			}
			return true;
		}

		@Override
		public boolean visit(PropertyExpressionFragment fragment) {
			String nextPart = nameFromExpression(fragment.getAssociatedExpression());
			if (nextPart != null) {
				sb.append(nextPart);
			}
			return true;
		}

		@Override
		public boolean visit(SimpleExpressionASTFragment fragment) {
			String nextPart = nameFromExpression(fragment.getAssociatedExpression());
			if (nextPart != null) {
				sb.append(nextPart);
			}
			return true;
		}

		private String nameFromExpression(Expression expr) {
			String name = null;
			if (expr instanceof org.codehaus.groovy.ast.expr.CastExpression) {
				expr = ((CastExpression) expr).getExpression();
			}

			if (expr instanceof ConstantExpression) {
				name = ((ConstantExpression) expr).getText();
			} else if (expr instanceof MethodCallExpression) {
				name = ((MethodCallExpression) expr).getMethodAsString();
			} else if (expr instanceof StaticMethodCallExpression) {
				name = ((StaticMethodCallExpression) expr).getMethod();
			} else if (expr instanceof BinaryExpression) {
                name = nameFromExpression(((BinaryExpression) expr).getLeftExpression())
						+ nameFromExpression(((BinaryExpression) expr).getRightExpression());
			} else if (expr instanceof Variable) {
				name = ((Variable) expr).getName();
			}

            // always capitalize
            // the first character will be made lower case later
            if (name != null && name.length() > 0) {
                name = "" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
			return name;
		}

	}

	private String getBaseNameFromExpression(IASTFragment assignedFragment) {
		if (assignedFragment == null) {
			return "local";
		}
		GuessBaseNameVisitor visitor = new GuessBaseNameVisitor();
		assignedFragment.accept(visitor);
		return visitor.getGuessedName();
    }


    public void setLocalName(String newName) {
        Assert.isNotNull(newName);
        localName = newName;
    }

    public String getLocalName() {
        return localName;
    }


    @Override
    public String getName() {
        return "Extract local variable";
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        try {
            pm.beginTask(RefactoringCoreMessages.ExtractTempRefactoring_checking_preconditions, 1);

            ExtractLocalDescriptor descriptor = createRefactoringDescriptor();
            change.setDescriptor(new RefactoringChangeDescriptor(descriptor));
            return change;
        } finally {
            pm.done();
        }
    }

    /**
     * @return
     */
    private ExtractLocalDescriptor createRefactoringDescriptor() {
        final Map<String, String> arguments = new HashMap<String, String>();
        String project = null;
        IJavaProject javaProject = unit.getJavaProject();
        if (javaProject != null)
            project = javaProject.getElementName();
        final String description = Messages.format(RefactoringCoreMessages.ExtractTempRefactoring_descriptor_description_short,
                BasicElementLabels.getJavaElementName(localName));
        final String expression = getTextAt(getSelectedFragment().getStart(), getSelectedFragment().getEnd());
        final String header = Messages.format(RefactoringCoreMessages.ExtractTempRefactoring_descriptor_description, new String[] {
                BasicElementLabels.getJavaElementName(localName), BasicElementLabels.getJavaCodeString(expression) });
        final JDTRefactoringDescriptorComment comment = new JDTRefactoringDescriptorComment(project, this, header);
        comment.addSetting(Messages.format(RefactoringCoreMessages.ExtractTempRefactoring_name_pattern, BasicElementLabels
                .getJavaElementName(localName)));
        comment.addSetting(Messages.format(RefactoringCoreMessages.ExtractTempRefactoring_expression_pattern, BasicElementLabels
                .getJavaCodeString(expression)));
        if (replaceAllOccurrences)
            comment.addSetting(RefactoringCoreMessages.ExtractTempRefactoring_replace_occurrences);
        final ExtractLocalDescriptor descriptor = RefactoringSignatureDescriptorFactory.createExtractLocalDescriptor(project,
                description, comment.asString(), arguments, RefactoringDescriptor.NONE);
        arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(project, unit));
        arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, localName);
        arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION, new Integer(start).toString()
                + " " + new Integer(length).toString()); //$NON-NLS-1$
        arguments.put(ATTRIBUTE_REPLACE, Boolean.valueOf(replaceAllOccurrences).toString());
        return descriptor;
    }
}
