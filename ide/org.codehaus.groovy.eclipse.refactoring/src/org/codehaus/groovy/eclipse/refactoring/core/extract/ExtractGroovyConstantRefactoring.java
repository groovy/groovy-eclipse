/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.extract;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.BinaryExpressionFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.MethodCallFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.PropertyExpressionFragment;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindAllOccurrencesVisitor;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.Activator;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.StatusHelper;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractConstantDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractConstantRefactoring;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class ExtractGroovyConstantRefactoring extends ExtractConstantRefactoring {

    private IASTFragment selectedFragment;

    private GroovyCompilationUnit unit;

    private int start, length;

    private String constantName;

    private boolean insertFirst;

    private FieldNode toInsertAfter;

    private String constantText;

    private String[] fExcludedVariableNames;

    public ExtractGroovyConstantRefactoring(final JavaRefactoringArguments arguments, final RefactoringStatus status) {
        super(arguments, status);
        length = -1;
        start = -1;
    }

    public ExtractGroovyConstantRefactoring(final GroovyCompilationUnit unit, final int offset, final int length) {
        super(unit, offset, length);
        this.unit = unit;
        this.start = offset;
        // some nodes include whitespace after their ends, so expand the selection so that these
        // kinds of nodes can be selected without the user having to explicitly include whitespace
        this.length = expandSelection(start, length);
        setSelectionLength(this.length);
    }

    @Override
    public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) throws CoreException {
        SubMonitor submon = SubMonitor.convert(monitor, RefactoringCoreMessages.ExtractConstantRefactoring_checking_preconditions, 7);

        RefactoringStatus status;
        try {
            try {
                //status = Checks.validateEdit(unit, getValidationContext(), submon.split(4));
                status = (RefactoringStatus) Checks.class.getDeclaredMethod("validateEdit", ICompilationUnit.class, Object.class, IProgressMonitor.class)
                        .invoke(Checks.class, unit, getValidationContext(), submon.split(4));
            } catch (NoSuchMethodException e) {
                //status = Checks.validateEdit(unit, getValidationContext());
                status = (RefactoringStatus) Checks.class.getDeclaredMethod("validateEdit", ICompilationUnit.class, Object.class)
                        .invoke(Checks.class, unit, getValidationContext());
                submon.worked(4);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        if (status.hasFatalError()) {
            return status;
        }

        IASTFragment astFragment = getSelectedFragment();
        if (astFragment == null) {
            status.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.ExtractConstantRefactoring_select_expression, createContext()));
            return status;
        }
        submon.worked(1);

        status.merge(checkFragment());
        if (status.hasFatalError()) {
            return status;
        }
        submon.worked(1);

        ClassNode targetType = getContainingClassNode();
        if (targetType == null) {
            status.merge(RefactoringStatus.createFatalErrorStatus("Cannot find enclosing Class declaration."));
        }
        if (targetType.isScript()) {
            status.merge(RefactoringStatus.createFatalErrorStatus("Cannot extract a constant to a Script."));
        }

        if (targetType.isAnnotationDefinition() || targetType.isInterface()) {
            setVisibility(JdtFlags.VISIBILITY_STRING_PUBLIC);
            setTargetIsInterface(true);
        }
        submon.worked(1);

        return status;
    }

    private int expandSelection(final int s, final int l) {
        int end = s + l;
        char[] contents = unit.getContents();
        while (end < contents.length && (contents[end] == ' ' || contents[end] == '\t')) {
            end++;
        }
        return end - s;
    }

    private CompilationUnitChange getChange() {
        return ReflectionUtils.getPrivateField(ExtractConstantRefactoring.class, "fChange", this);
    }

    private void setChange(final CompilationUnitChange change) {
        ReflectionUtils.setPrivateField(ExtractConstantRefactoring.class, "fChange", this, change);
    }

    @Override
    public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException {
        try {
            RefactoringStatus status = new RefactoringStatus();
            CompilationUnitChange change = new CompilationUnitChange("Extract Groovy Constant", getCu());
            change.setEdit(new MultiTextEdit());
            TextEditGroup group = createConstantDeclaration();
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            for (TextEdit edit : group.getTextEdits()) {
                change.addEdit(edit);
            }
            group = replaceExpressionsWithConstant();
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            for (TextEdit edit : group.getTextEdits()) {
                change.addEdit(edit);
            }
            setChange(change);
            return status;
        } catch (MalformedTreeException e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        } catch (BadLocationException e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }
    }

    private TextEditGroup replaceExpressionsWithConstant() throws JavaModelException {
        IASTFragment origExpr = getSelectedFragment();
        List<IASTFragment> occurrences;
        if (getReplaceAllOccurrences()) {
            FindAllOccurrencesVisitor v = new FindAllOccurrencesVisitor(getCu().getModuleNode());
            occurrences = v.findOccurrences(origExpr);
        } else {
            occurrences = Collections.singletonList(origExpr);
        }

        TextEditGroup msg = new TextEditGroup(RefactoringCoreMessages.ExtractConstantRefactoring_replace);
        for (IASTFragment fragment : occurrences) {
            String replaceText;
            if (getQualifyReferencesWithDeclaringClassName()) {
                replaceText = getContainingClassNode().getNameWithoutPackage() + "." + getConstantName();
            } else {
                replaceText = getConstantName();
            }
            msg.addTextEdit(new ReplaceEdit(fragment.getStart(), fragment.getLength(), replaceText));
        }
        return msg;
    }

    private TextEditGroup createConstantDeclaration() throws MalformedTreeException, BadLocationException {
        String constant = getConstantText();
        TextEditGroup msg = new TextEditGroup(RefactoringCoreMessages.ExtractConstantRefactoring_declare_constant);
        int insertLocation = findInsertLocation();
        msg.addTextEdit(new InsertEdit(insertLocation, constant));
        return msg;
    }

    private int findInsertLocation() {
        if (insertFirst()) {
            ClassNode node = getContainingClassNode();
            if (node.isScript()) {
                int statementStart = node.getModule().getStatementBlock().getStart();
                int methodStart;
                if (!node.getModule().getMethods().isEmpty()) {
                    methodStart = node.getModule().getMethods().get(0).getStart();
                } else {
                    methodStart = Integer.MAX_VALUE;
                }
                return Math.min(methodStart, statementStart);
            } else {
                return CharOperation.indexOf('{', getCu().getContents(), node.getNameEnd()) + 1;
            }
        } else {
            return toInsertAfter.getEnd();
        }
    }

    private String getConstantText() throws MalformedTreeException, BadLocationException {
        if (constantText == null) {
            constantText = createConstantText();
        }
        return constantText;
    }

    private String createConstantText() throws MalformedTreeException, BadLocationException {
        StringBuilder sb = new StringBuilder();
        IJavaProject javaProject = getCu().getJavaProject();

        sb.append(CodeFormatterUtil.createIndentString(getIndentLevel(), javaProject));
        if (getVisibility().length() > 0) {
            sb.append(getVisibility()).append(" ");
        }
        sb.append("static final ").append(getConstantTypeName()).append(constantName).append(" = ").append(createExpressionText());

        IDocument doc = new Document(sb.toString());
        DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(doc, new FormatterPreferences(unit), getIndentLevel());
        TextEdit edit = formatter.format();
        edit.apply(doc);

        return getDefaultNewlineCharacterTwice() + doc.get();
    }

    private String getDefaultNewlineCharacterTwice() {
        String newline = ASTTools.getLineDelimeter(unit);
        return newline + newline;
    }

    private int getIndentLevel() {
        ClassNode node = getContainingClassNode();
        ClassNode containing = node;
        int indentLevel = 0;
        while (containing != null) {
            indentLevel++;
            if (containing.getEnclosingMethod() != null) {
                indentLevel++;
                containing = containing.getEnclosingMethod().getDeclaringClass();
            } else {
                containing = containing.getOuterClass();
            }
        }
        return indentLevel;
    }

    private String createExpressionText() {
        IASTFragment fragment = getSelectedFragment();
        return String.valueOf(unit.getContents()).substring(fragment.getStart(), fragment.getEnd());
    }

    private boolean insertFirst() {
        if (!isDeclarationLocationComputed())
            computeConstantDeclarationLocation();
        return insertFirst;
    }

    private boolean isDeclarationLocationComputed() {
        return insertFirst || toInsertAfter != null;
    }

    @Override
    public Change createChange(final IProgressMonitor monitor) throws CoreException {
        ExtractConstantDescriptor descriptor = createRefactoringDescriptor();
        getChange().setDescriptor(new RefactoringChangeDescriptor(descriptor));
        return getChange();
    }

    private ExtractConstantDescriptor createRefactoringDescriptor() {
        Map<String, String> arguments = new HashMap<>();
        String project = null;
        IJavaProject javaProject = getCu().getJavaProject();
        if (javaProject != null)
            project = javaProject.getElementName();
        int flags = JavaRefactoringDescriptor.JAR_REFACTORING | JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT | RefactoringDescriptor.STRUCTURAL_CHANGE;

        String expression = createExpressionText();
        String description = RefactoringCoreMessages.bind(RefactoringCoreMessages.ExtractConstantRefactoring_descriptor_description_short, BasicElementLabels.getJavaElementName(constantName));
        String header = RefactoringCoreMessages.bind(RefactoringCoreMessages.ExtractConstantRefactoring_descriptor_description, BasicElementLabels.getJavaElementName(constantName), BasicElementLabels.getJavaCodeString(expression));
        final JDTRefactoringDescriptorComment comment = new JDTRefactoringDescriptorComment(project, this, header);
        comment.addSetting(RefactoringCoreMessages.bind(RefactoringCoreMessages.ExtractConstantRefactoring_constant_name_pattern, BasicElementLabels.getJavaElementName(constantName)));
        comment.addSetting(RefactoringCoreMessages.bind(RefactoringCoreMessages.ExtractConstantRefactoring_constant_expression_pattern, BasicElementLabels.getJavaCodeString(expression)));
        comment.addSetting(RefactoringCoreMessages.bind(RefactoringCoreMessages.ExtractConstantRefactoring_visibility_pattern, RefactoringCoreMessages.ExtractConstantRefactoring_default_visibility));
        if (getReplaceAllOccurrences()) {
            comment.addSetting(RefactoringCoreMessages.ExtractConstantRefactoring_replace_occurrences);
        }
        if (getQualifyReferencesWithDeclaringClassName()) {
            comment.addSetting(RefactoringCoreMessages.ExtractConstantRefactoring_qualify_references);
        }
        arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, constantName);
        arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION, String.valueOf(start) + " " + String.valueOf(length));
        arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(project, getCu()));
        arguments.put("replace", Boolean.toString(getReplaceAllOccurrences()));
        arguments.put("qualify", Boolean.toString(getQualifyReferencesWithDeclaringClassName()));
        arguments.put("visibility", Integer.toString(JdtFlags.getVisibilityCode(getVisibility())));

        ExtractConstantDescriptor descriptor = RefactoringSignatureDescriptorFactory.createExtractConstantDescriptor(project, description, comment.asString(), arguments, flags);
        return descriptor;
    }

    private boolean getReplaceAllOccurrences() {
        return ((Boolean) ReflectionUtils.getPrivateField(ExtractConstantRefactoring.class, "fReplaceAllOccurrences", this)).booleanValue();
    }

    private boolean getQualifyReferencesWithDeclaringClassName() {
        return ((Boolean) ReflectionUtils.getPrivateField(ExtractConstantRefactoring.class, "fQualifyReferencesWithDeclaringClassName", this)).booleanValue();
    }

    @Override
    public String getName() {
        return "Extract to constant" + (getReplaceAllOccurrences() ? " (replace all occurrences)" : "");
    }

    private void computeConstantDeclarationLocation() {
        if (isDeclarationLocationComputed()) {
            return;
        }

        FieldNode lastStaticDependency = null;
        Iterator<FieldNode> decls = getContainingClassNode().getFields().iterator();

        while (decls.hasNext()) {
            FieldNode decl = decls.next();
            if (decl.isStatic() && decl.getEnd() > 0) {
                lastStaticDependency = decl;
            }
        }

        if (lastStaticDependency == null) {
            insertFirst = true;
        } else {
            toInsertAfter = lastStaticDependency;
        }
    }

    private ClassNode getContainingClassNode() {
        return ASTTools.getContainingClassNode(getCu().getModuleNode(), getSelectionStart());
    }

    private void setTargetIsInterface(boolean b) {
        ReflectionUtils.setPrivateField(ExtractConstantRefactoring.class, "fTargetIsInterface", this, true);
    }

    private RefactoringStatus checkFragment() throws JavaModelException {
        RefactoringStatus status = new RefactoringStatus();
        IASTFragment astFragment = getSelectedFragment();
        status.merge(checkExpressionFragmentIsRValue(astFragment));
        if (status.hasFatalError())
            return status;
        checkAllStaticFinal();
        if (!selectionAllStaticFinal()) {
            status.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.ExtractConstantRefactoring_not_load_time_constant));
        }

        Expression expression = astFragment.getAssociatedExpression();

        if ((expression instanceof ConstantExpression) && ((ConstantExpression) expression).isNullExpression()) {
            status.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.ExtractConstantRefactoring_null_literals));
        }

        return status;
    }

    private void checkAllStaticFinal() throws JavaModelException {
        StaticFragmentChecker checker = new StaticFragmentChecker();
        boolean maybeStatic = checker.mayNotBeStatic(getSelectedFragment());
        ReflectionUtils.setPrivateField(ExtractConstantRefactoring.class, "fSelectionAllStaticFinal", this, maybeStatic);
        ReflectionUtils.setPrivateField(ExtractConstantRefactoring.class, "fAllStaticFinalCheckPerformed", this, true);
    }

    private RefactoringStatus checkExpressionFragmentIsRValue(final IASTFragment fragment) throws JavaModelException {
        if (fragment.kind() == ASTFragmentKind.SIMPLE_EXPRESSION) {
            Expression expr = fragment.getAssociatedExpression();
            if (expr instanceof VariableExpression) {
                VariableExpression var = (VariableExpression) expr;
                if (var.getAccessedVariable() == var) {
                    return RefactoringStatus.createFatalErrorStatus("Target expression is a variable declaration.  Cannot extract a constant.");
                }
                // should also check to see if expression is on the left side of an assignment
            }
        }
        return new RefactoringStatus();
    }

    private RefactoringStatusContext createContext() {
        IJavaElement elt;
        try {
            elt = getCu().getElementAt(getSelectionStart());
            if (elt instanceof IMember) {
                return StatusHelper.createContext((IMember) elt);
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Error finding refactoring context", e);
        }
        return null;
    }

    private IASTFragment getSelectedFragment() {
        if (selectedFragment == null) {
            selectedFragment =
                ASTTools.getSelectionFragment(getCu().getModuleNode(), getSelectionStart(), getSelectionLength());
        }
        return selectedFragment;
    }

    /**
     * This method is really pretty useless unless we use the inferencing engine
     */
    private String getConstantTypeName() {
        Expression e = getSelectedFragment().getAssociatedExpression();
        if (e != null) {
            String name = e.getType().getNameWithoutPackage();
            if (!"Object".equals(name)) {
                return name + " ";
            }
        }
        return "";
    }

    // !! similar to ExtractTempRefactoring equivalent
    @Override
    public String getConstantSignaturePreview() throws JavaModelException {
        return getVisibility() + " static final " + getConstantTypeName() + ' ' + constantName;
    }

    @Override
    public RefactoringStatus checkConstantNameOnChange() throws JavaModelException {
        return Checks.checkConstantName(getConstantName(), getCu());
    }

    @Override
    public String[] guessConstantNames() {
        String text = getBaseNameFromExpression(getCu().getJavaProject(), getSelectedFragment(),
            NamingConventions.VK_STATIC_FINAL_FIELD);
        if (text == null) {
            return CharOperation.NO_STRINGS;
        }
        try {
            Integer.parseInt(text);
            text = "_" + text;
        } catch (NumberFormatException e) {
            // ignore
        }
        return NamingConventions.suggestVariableNames(NamingConventions.VK_STATIC_FINAL_FIELD, NamingConventions.BK_NAME, text, getCu().getJavaProject(), getContainingClassNode().isArray() ? 1 : 0, getExcludedVariableNames(), true);
    }

    private String[] getExcludedVariableNames() {
        if (fExcludedVariableNames == null) {
            Set<String> usedNames = new HashSet<>();
            for (ClassNode classNode : unit.getModuleNode().getClasses()) {
                for (FieldNode fieldNode : classNode.getFields()) {
                    usedNames.add(fieldNode.getName());
                }
            }
            fExcludedVariableNames = usedNames.toArray(new String[usedNames.size()]);
        }
        return fExcludedVariableNames;
    }

    private static final String[] KNOWN_METHOD_NAME_PREFIXES = {"get", "set", "is", "to"};

    private static String getBaseNameFromExpression(final IJavaProject project, final IASTFragment assignedFragment, final int variableKind) {
        if (assignedFragment == null) {
            return null;
        }
        String name = null;
        Expression assignedExpression = assignedFragment.getAssociatedExpression();
        if (assignedExpression instanceof CastExpression) {
            assignedExpression = ((CastExpression) assignedExpression).getExpression();
        }

        // extract some kind of name if possible
        String candidate = null;
        if (assignedExpression instanceof ConstantExpression) {
            candidate = ((ConstantExpression) assignedExpression).getText();
        } else if (assignedExpression instanceof VariableExpression) {
            candidate = ((VariableExpression) assignedExpression).getName();
        } else if (assignedExpression instanceof ClassExpression) {
            candidate = ((ClassExpression) assignedExpression).getType().getNameWithoutPackage();
        } else if (assignedExpression instanceof MethodCallExpression) {
            candidate = ((MethodCallExpression) assignedExpression).getMethodAsString();
        } else if (assignedExpression instanceof StaticMethodCallExpression) {
            candidate = ((StaticMethodCallExpression) assignedExpression).getMethod();
        }

        // now process the name into a good variable name
        if (candidate != null) {
            StringBuffer res = new StringBuffer();
            boolean needsUnderscore = false;
            for (int i = 0; i < candidate.length(); i++) {
                char ch = candidate.charAt(i);
                if (Character.isJavaIdentifierPart(ch)) {
                    if (res.length() == 0 && !Character.isJavaIdentifierStart(ch) || needsUnderscore) {
                        res.append('_');
                    }
                    res.append(ch);
                    needsUnderscore = false;
                } else {
                    needsUnderscore = res.length() > 0;
                }
            }
            if (res.length() > 0) {
                name = res.toString();
            }
        }

        // now recur through the rest of the fragment
        IASTFragment next;
        switch (assignedFragment.kind()) {
        case PROPERTY:
        case SAFE_PROPERTY:
        case SPREAD_SAFE_PROPERTY:
        case METHOD_POINTER:
            next = ((PropertyExpressionFragment) assignedFragment).getNext();
            break;
        case METHOD_CALL:
            next = ((MethodCallFragment) assignedFragment).getNext();
            break;
        case BINARY:
            next = ((BinaryExpressionFragment) assignedFragment).getNext();
            break;
        default:
            next = null;
        }

        if (next != null) {
            name = name + "_" + getBaseNameFromExpression(project, next, variableKind);
        }

        if (name != null) {
            for (int i = 0, n = KNOWN_METHOD_NAME_PREFIXES.length; i < n; i += 1) {
                String curr = KNOWN_METHOD_NAME_PREFIXES[i];
                if (name.startsWith(curr)) {
                    if (name.equals(curr)) {
                        return null; // don't suggest 'get' as variable name
                    } else if (Character.isUpperCase(name.charAt(curr.length()))) {
                        return name.substring(curr.length());
                    }
                }
            }
        } else {
            name = "CONSTANT";
        }

        return name;
    }

    @Override
    public void setConstantName(final String newName) {
        super.setConstantName(newName);
        Assert.isNotNull(newName);
        constantName = newName;
    }

    @Override
    public String getConstantName() {
        return constantName;
    }

    private GroovyCompilationUnit getCu() {
        if (unit == null) {
            unit = ReflectionUtils.getPrivateField(ExtractConstantRefactoring.class, "fCu", this);
        }
        return unit;
    }

    private int getSelectionStart() {
        if (start == -1) {
            start = (Integer) ReflectionUtils.getPrivateField(ExtractConstantRefactoring.class, "fSelectionStart", this);
        }
        return start;
    }

    private int getSelectionLength() {
        if (length == -1) {
            length = (Integer) ReflectionUtils.getPrivateField(ExtractConstantRefactoring.class, "fSelectionLength", this);
        }
        return length;
    }

    private void setSelectionLength(final int newLength) {
        ReflectionUtils.setPrivateField(ExtractConstantRefactoring.class, "fSelectionLength", this, newLength);
    }
}
