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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode.VisitKind;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.code.PromoteTempToFieldRefactoring;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * See GRECLIPSE-1436 for areas where this refactoring can be improved.
 * 
 * @author Daniel Phan
 * @created 2012-01-26
 */
public class ConvertGroovyLocalToFieldRefactoring extends PromoteTempToFieldRefactoring {

    private GroovyCompilationUnit unit;

    private String fieldName;
    private int fieldVisibility = -1;

    private int selectionStart;
    private int selectionLength;
    private IASTFragment selectionFragment;

    private VariableExpression variableExpressionInDeclaration;
    private DeclarationExpression declarationExpression;
    private ClassNode containingClassNode;
    private ModuleNode moduleNode;

    private MethodNode methodNode;

    private CompilationUnitChange change;

    public ConvertGroovyLocalToFieldRefactoring(GroovyCompilationUnit unit, int selectionStart, int selectionLength) {
        super(unit, selectionStart, selectionLength);
        this.unit = unit;
        this.selectionStart = selectionStart;
        this.selectionLength = selectionLength;
    }

    @Override
    public int getVisibility() {
        if (fieldVisibility == -1) {
            fieldVisibility = (Integer) ReflectionUtils.getPrivateField(PromoteTempToFieldRefactoring.class, "fVisibility", this);
        }
        return fieldVisibility;
    }

    @Override
    public boolean getDeclareFinal() {
        return false;
    }

    @Override
    public boolean getDeclareStatic() {
        return false;
    }

    public String getFieldName() {
        if (fieldName == null) {
            fieldName = (String) ReflectionUtils.getPrivateField(PromoteTempToFieldRefactoring.class, "fFieldName", this);
        }
        if ((fieldName == null || fieldName.length() == 0) && variableExpressionInDeclaration != null) {
            fieldName = variableExpressionInDeclaration.getName();
        }
        return fieldName;
    }

    @Override
    public int getInitializeIn() {
        return INITIALIZE_IN_METHOD;
    }

    @Override
    public void setVisibility(int accessModifier) {
        super.setVisibility(accessModifier);
        fieldVisibility = accessModifier;
    }

    @Override
    public void setDeclareFinal(boolean declareFinal) {
    }

    @Override
    public void setDeclareStatic(boolean declareStatic) {
    }

    @Override
    public void setFieldName(String fieldName) {
        super.setFieldName(fieldName);
        this.fieldName = fieldName;
    }

    @Override
    public void setInitializeIn(int initializeIn) {
    }

    @Override
    public boolean canEnableSettingStatic() {
        return false;
    }

    @Override
    public boolean canEnableSettingFinal() {
        return false;
    }

    @Override
    public boolean canEnableSettingDeclareInConstructors() {
        return false;
    }

    @Override
    public boolean canEnableSettingDeclareInMethod() {
        return true;
    }

    @Override
    public boolean canEnableSettingDeclareInFieldDeclaration() {
        return false;
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        try {
            pm.beginTask("", 5);

            RefactoringStatus result = Checks.validateEdit(unit, getValidationContext());
            if (result.hasFatalError()) {
                return result;
            }
            pm.worked(1);

            IASTFragment selectionFragment = getSelectionFragment();
            if (selectionFragment == null) {
                result.merge(RefactoringStatus
                        .createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_select_declaration));
                return result;
            }
            Expression selectedExpression = selectionFragment.getAssociatedExpression();
            if (!(selectedExpression instanceof VariableExpression)) {
                result.merge(RefactoringStatus
                        .createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_select_declaration));
                return result;
            }
            pm.worked(1);

            VariableExpression selectedVariableExpression = (VariableExpression) selectedExpression;
            Variable declaredVariable = selectedVariableExpression.getAccessedVariable();
            if (declaredVariable instanceof DynamicVariable) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot convert dynamic variable."));
                return result;
            }
            if (!(declaredVariable instanceof VariableExpression)) {
                result.merge(RefactoringStatus
                        .createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_select_declaration));
                return result;
            }
            pm.worked(1);

            VariableExpression variableExpressionInDeclaration = (VariableExpression) declaredVariable;
            DeclarationExpression declarationExpression = getDeclarationExpression(variableExpressionInDeclaration);

            if (declarationExpression == null) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot find variable declaration."));
                return result;
            }

            if (declarationExpression.isMultipleAssignmentDeclaration()) {
                result.merge(RefactoringStatus
                        .createFatalErrorStatus("Cannot convert a variable declared using multiple assignment."));
                return result;
            }
            pm.worked(1);

            // We should check declaration for local type usage here

            this.variableExpressionInDeclaration = variableExpressionInDeclaration;

            ClassNode containingClassNode = getContainingClassNode();
            if (containingClassNode == null) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot find enclosing class declaration."));
                return result;
            }
            if (containingClassNode.isScript()) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot add field to a script."));
                return result;
            }
            if (containingClassNode.isInterface() || containingClassNode.isAnnotationDefinition()) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot add field to an interface or annotation definition."));
                return result;
            }
            pm.worked(1);

            return result;
        } finally {
            pm.done();
        }
    }

    @Override
    public String[] guessFieldNames() {
        return new String[] { variableExpressionInDeclaration.getName() };
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
        try {
            pm.beginTask("", 4);

            FieldNode conflictingField = getContainingClassNode().getDeclaredField(getFieldName());
            if (conflictingField != null) {
                return RefactoringStatus
                        .createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_Name_conflict_with_field);
            }
            pm.worked(1);

            CompilationUnitChange change = new CompilationUnitChange("Convert Groovy Local Variable To Field", unit);
            change.setEdit(new MultiTextEdit());

            // Create field
            TextEditGroup group = createFieldTextEditGroup();
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            for (TextEdit edit : group.getTextEdits()) {
                change.addEdit(edit);
            }
            pm.worked(1);

            // Convert declaration to a reference
            group = declarationToReferenceTextEditGroup();
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            for (TextEdit edit : group.getTextEdits()) {
                change.addEdit(edit);
            }
            pm.worked(1);

            // Rename variable references
            RefactoringStatus status = new RefactoringStatus();
            group = renameVariableReferencesTextEditGroup(status);
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            for (TextEdit edit : group.getTextEdits()) {
                change.addEdit(edit);
            }
            pm.worked(1);

            this.change = change;

            return status;
        } finally {
            pm.done();
        }
    }

    private TextEditGroup createFieldTextEditGroup() {
        ClassNode classNode = getContainingClassNode();
        char[] contents = unit.getContents();

        MethodNode method = getContainingMethodNode();
        int methodLineOffset = method.getStart() - method.getColumnNumber() + 1;
        int methodOffset = method.getStart();
        String methodIndentation = String.valueOf(CharOperation.subarray(contents, methodLineOffset, methodOffset));
        int indentLevel = ASTTools.getCurrentIntentation(methodIndentation);

        String fieldText = null;
        try {

            fieldText = createFieldText(indentLevel);
        } catch (Exception e) {}

        TextEditGroup group = new TextEditGroup("Create field.");
        if (fieldText != null) {
            int insertOffset = CharOperation.indexOf('{', contents, classNode.getStart()) + 1;
            String newline = TextUtilities.determineLineDelimiter(String.valueOf(contents), "\n");
            group.addTextEdit(new InsertEdit(insertOffset, newline + fieldText));
        }
        return group;
    }

    /**
     * Adapted from
     * ExtractGroovyConstantRefactoring#createConstantText
     */
    private String createFieldText(int indentLevel) throws MalformedTreeException, BadLocationException {
        StringBuilder sb = new StringBuilder();

        String indentation = CodeFormatterUtil.createIndentString(indentLevel, unit.getJavaProject());
        sb.append(indentation);

        String visibility = JdtFlags.getVisibilityString(getVisibility());
        sb.append(visibility);
        if (!visibility.equals("")) {
            sb.append(" ");
        }

        char[] contents = unit.getContents();

        // typeOrDef is the value of the token that comes before the variable
        // name in the declaration expression (e.g. "String", "int", "def").
        String typeOrDef = new String(CharOperation.subarray(contents, declarationExpression.getStart(),
                variableExpressionInDeclaration.getStart()));
        sb.append(typeOrDef).append(getFieldName());

        IDocument doc = new Document(sb.toString());
        DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(doc, new FormatterPreferences(unit), indentLevel);
        TextEdit edit = formatter.format();
        edit.apply(doc);

        return doc.get();
    }

    private TextEditGroup declarationToReferenceTextEditGroup() {
        TextEditGroup group = new TextEditGroup("Convert local variable declaration to reference.");
        int typeOrDefLength = variableExpressionInDeclaration.getStart() - declarationExpression.getStart();
        group.addTextEdit(new ReplaceEdit(declarationExpression.getStart(), typeOrDefLength, ""));
        return group;
    }

    private TextEditGroup renameVariableReferencesTextEditGroup(RefactoringStatus status) {
        final Set<VariableExpression> references = new HashSet<VariableExpression>();
        ClassCodeVisitorSupport referencesVisitor = new ClassCodeVisitorSupport() {
            @Override
            public void visitVariableExpression(VariableExpression variableExpression) {
                if (variableExpression.getAccessedVariable() == variableExpressionInDeclaration
                        && variableExpression.getLineNumber() >= 0) {
                    references.add(variableExpression);
                }
            }

            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }
        };

        referencesVisitor.visitClass(getContainingClassNode());
        Iterator<InnerClassNode> innerClasses = getContainingClassNode().getInnerClasses();
        while (innerClasses != null && innerClasses.hasNext()) {
            ClassNode innerClass = innerClasses.next();
            referencesVisitor.visitClass(innerClass);
        }

        TextEditGroup group = new TextEditGroup("Update local variables to reference field.");
        for (VariableExpression reference : references) {
            if (getUsedVariableAndFieldNames(reference).contains(getFieldName())) {
                status.merge(RefactoringStatus.createWarningStatus("New field conflicts with existing name."));
            }

            group.addTextEdit(new ReplaceEdit(reference.getStart(), reference.getLength(), getFieldName()));
        }
        return group;
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException {
        return change;
    }

    private IASTFragment getSelectionFragment() {
        if (selectionFragment == null) {
            selectionFragment = ASTTools.getSelectionFragment(getModuleNode(), selectionStart, selectionLength);
        }
        return selectionFragment;
    }

    private ClassNode getContainingClassNode() {

        if (containingClassNode == null) {
            ModuleNode moduleNode = getModuleNode();
            if (moduleNode == null) {
                return null;
            }
            if (declarationExpression == null) {
                return null;
            }
            containingClassNode = ASTTools.getContainingClassNode(moduleNode, declarationExpression.getStart());
        }
        return containingClassNode;
    }

    private ModuleNode getModuleNode() {
        if (moduleNode == null) {
            moduleNode = unit.getModuleNode();
        }
        return moduleNode;
    }

    private DeclarationExpression getDeclarationExpression(final VariableExpression variableExpressionInDeclaration) {
        if (declarationExpression != null) {
            return declarationExpression;
        }

        ClassCodeVisitorSupport visitor = new ClassCodeVisitorSupport() {
            @Override
            public void visitDeclarationExpression(DeclarationExpression declarationExpression) {
                // Remember the most recent DeclarationExpression we visited.
                ConvertGroovyLocalToFieldRefactoring.this.declarationExpression = declarationExpression;
                super.visitDeclarationExpression(declarationExpression);
            }

            @Override
            public void visitVariableExpression(VariableExpression variableExpression) {
                // The moment we visit the variableExpressionInDeclaration node,
                // we know that the the most recent DeclarationExpression we've
                // visited is the one that contains
                // variableExpressionInDeclaration.
                // That's why we throw the VisitCompleteException to stop
                // visiting nodes.
                if (variableExpression == variableExpressionInDeclaration) {
                    throw new VisitCompleteException();
                }
                super.visitVariableExpression(variableExpression);
            }

            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }
        };

        for (ClassNode classNode : getModuleNode().getClasses()) {
            try {
                visitor.visitClass(classNode);
            } catch (VisitCompleteException expected) {
                break;
            }
            // If a VisitCompleteException was not thrown, we did not find the
            // declarationExpression that contains
            // variableExpressionInDeclaration.
            // Therefore, we set declarationExpression to null, since it's just
            // some irrelevant DeclarationExpression.
            declarationExpression = null;
        }

        return declarationExpression;
    }

    /**
     * Same as getDeclarationExpression except with the containing method
     */
    private MethodNode getContainingMethodNode() {
        if (methodNode != null) {
            return methodNode;
        }

        ClassCodeVisitorSupport visitor = new ClassCodeVisitorSupport() {
            @Override
            public void visitConstructorOrMethod(MethodNode methodNode, boolean isConstructor) {
                ConvertGroovyLocalToFieldRefactoring.this.methodNode = methodNode;
                super.visitConstructorOrMethod(methodNode, isConstructor);
            }

            @Override
            public void visitVariableExpression(VariableExpression variableExpression) {
                if (variableExpression == variableExpressionInDeclaration) {
                    throw new VisitCompleteException();
                }
                super.visitVariableExpression(variableExpression);
            }

            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }
        };

        for (ClassNode classNode : getModuleNode().getClasses()) {
            try {
                visitor.visitClass(classNode);
            } catch (VisitCompleteException expected) {
                break;
            }
            methodNode = null;
        }

        return methodNode;
    }

    /**
     * Adapted from ExtractGroovyLocalRefactoring#getParentStack and
     * ExtractGroovyLocalRefactoring#getExcludedVariableNames
     */
    private Set<String> getUsedVariableAndFieldNames(VariableExpression variableExpression) {
        FindSurroundingNode find = new FindSurroundingNode(new Region(variableExpression), VisitKind.PARENT_STACK);
        find.doVisitSurroundingNode(moduleNode);
        List<IASTFragment> parentStack = new ArrayList<IASTFragment>(find.getParentStack());
        Collections.reverse(parentStack);

        Set<String> result = new HashSet<String>();
        for (IASTFragment fragment : parentStack) {
            ASTNode astNode = fragment.getAssociatedNode();
            VariableScope scope = null;
            if (astNode instanceof BlockStatement) {
                scope = ((BlockStatement) astNode).getVariableScope();
            } else if (astNode instanceof MethodNode) {
                scope = ((MethodNode) astNode).getVariableScope();
            } else if (astNode instanceof ClosureExpression) {
                scope = ((ClosureExpression) astNode).getVariableScope();
            } else if (astNode instanceof ClassNode) {
                for (FieldNode field : ((ClassNode) astNode).getFields()) {
                    if (field.getLineNumber() > 0) {
                        result.add(field.getName());
                    }
                }
            }
            if (scope != null) {
                Iterator<Variable> declaredVariables = scope.getDeclaredVariablesIterator();
                while (declaredVariables.hasNext()) {
                    Variable variable = declaredVariables.next();
                    if (variable instanceof VariableExpression) {
                        VariableExpression varExpression = (VariableExpression) variable;
                        if (varExpression.getAccessedVariable() != variableExpression.getAccessedVariable()) {
                            result.add(variable.getName());
                        }
                    } else {
                        result.add(variable.getName());
                    }
                }
            }
        }

        return result;
    }
}
