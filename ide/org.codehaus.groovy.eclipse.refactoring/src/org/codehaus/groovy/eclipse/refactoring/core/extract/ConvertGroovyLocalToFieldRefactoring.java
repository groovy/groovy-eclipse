/*
 * Copyright 2009-2017 the original author or authors.
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
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
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
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.code.PromoteTempToFieldRefactoring;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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
    public String getName() {
        return RefactoringCoreMessages.PromoteTempToFieldRefactoring_editName;
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
    public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask("", 5);

            RefactoringStatus result = Checks.validateEdit(unit, getValidationContext());
            if (result.hasFatalError()) {
                return result;
            }
            monitor.worked(1);

            IASTFragment selectionFragment = getSelectionFragment();
            if (selectionFragment == null) {
                result.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_select_declaration));
                return result;
            }
            Expression selectedExpression = selectionFragment.getAssociatedExpression();
            if (!(selectedExpression instanceof VariableExpression)) {
                result.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_select_declaration));
                return result;
            }
            monitor.worked(1);

            VariableExpression selectedVariableExpression = (VariableExpression) selectedExpression;
            Variable declaredVariable = selectedVariableExpression.getAccessedVariable();
            if (declaredVariable instanceof DynamicVariable) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot convert dynamic variable."));
                return result;
            }

            if (declaredVariable instanceof Parameter || isTailRecursiveMethodParameter(selectedVariableExpression)) {
                result.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_method_parameters));
                return result;
            }
            if (!(declaredVariable instanceof VariableExpression)) {
                result.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_select_declaration));
                return result;
            }
            monitor.worked(1);

            VariableExpression variableExpressionInDeclaration = (VariableExpression) declaredVariable;
            DeclarationExpression declarationExpression = getDeclarationExpression(variableExpressionInDeclaration);

            if (declarationExpression == null) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot find variable declaration."));
                return result;
            }

            if (declarationExpression.isMultipleAssignmentDeclaration()) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot convert a variable declared using multiple assignment."));
                return result;
            }
            monitor.worked(1);

            // We should check declaration for local type usage here

            this.variableExpressionInDeclaration = variableExpressionInDeclaration;

            ClassNode containingClassNode = getContainingClassNode();
            if (containingClassNode == null) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot find enclosing class declaration."));
                return result;
            }
            if (containingClassNode.isInterface() || containingClassNode.isAnnotationDefinition()) {
                result.merge(RefactoringStatus.createFatalErrorStatus("Cannot add field to an interface or annotation definition."));
                return result;
            }
            monitor.worked(1);

            return result;
        } finally {
            monitor.done();
        }
    }

    @Override
    public String[] guessFieldNames() {
        return new String[] {variableExpressionInDeclaration.getName()};
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask("", 4);

            FieldNode conflictingField = getContainingClassNode().getDeclaredField(getFieldName());
            if (conflictingField != null) {
                return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.PromoteTempToFieldRefactoring_Name_conflict_with_field);
            }
            monitor.worked(1);

            CompilationUnitChange change = new CompilationUnitChange("Convert Groovy Local Variable To Field", unit);
            change.setEdit(new MultiTextEdit());

            // Create field
            TextEditGroup group = createFieldTextEditGroup();
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            for (TextEdit edit : group.getTextEdits()) {
                change.addEdit(edit);
            }
            monitor.worked(1);

            // Convert declaration to a reference
            group = declarationToReferenceTextEditGroup();
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            for (TextEdit edit : group.getTextEdits()) {
                change.addEdit(edit);
            }
            monitor.worked(1);

            // Rename variable references
            RefactoringStatus status = new RefactoringStatus();
            group = renameVariableReferencesTextEditGroup(status);
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            for (TextEdit edit : group.getTextEdits()) {
                change.addEdit(edit);
            }
            monitor.worked(1);

            this.change = change;

            return status;
        } finally {
            monitor.done();
        }
    }

    private TextEditGroup createFieldTextEditGroup() {
        TextEdit textEdit = null;

        ClassNode classNode = getContainingClassNode();
        MethodNode methodNode = getContainingMethodNode();
        if (methodNode.isScriptBody() && getContainingClosureExpression() == null) {
            textEdit = new InsertEdit(getDeclarationOffset(), "@groovy.transform.Field ");
        } else {
            try {
                char[] contents = unit.getContents();
                int methodOffset = methodNode.getStart();
                int methodLineOffset = methodNode.getStart() - methodNode.getColumnNumber() + 1;
                int insertOffset = classNode.isScript() ? classNode.getStart() : CharOperation.indexOf('{', contents, classNode.getStart()) + 1;

                String methodIndentation = String.valueOf(CharOperation.subarray(contents, methodLineOffset, methodOffset));
                String fieldText = createFieldText(ASTTools.getCurrentIntentation(methodIndentation));
                String newline = ASTTools.getLineDelimeter(unit);

                textEdit = new InsertEdit(insertOffset, classNode.isScript() ? fieldText + newline : newline + fieldText);
            } catch (Exception e) {
            }
        }

        TextEditGroup group = new TextEditGroup("Create field.");
        if (textEdit != null) {
            group.addTextEdit(textEdit);
        }
        return group;
    }

    private String createFieldText(int indentLevel) throws BadLocationException, MalformedTreeException {
        StringBuilder sb = new StringBuilder();

        String indentation = CodeFormatterUtil.createIndentString(indentLevel, unit.getJavaProject());
        sb.append(indentation);

        if (getContainingClassNode().isScript()) {
            sb.append("@groovy.transform.Field ");
        } else {
            String visibility = JdtFlags.getVisibilityString(getVisibility());
            if (!visibility.equals("")) {
                sb.append(visibility).append(' ');
            }
        }

        // typeOrDef is the value of the token that comes before the variable
        // name in the declaration expression (e.g. "String", "int", "def").
        char[] typeOrDef = CharOperation.subarray(unit.getContents(), getDeclarationOffset(), variableExpressionInDeclaration.getStart());
        sb.append(typeOrDef).append(getFieldName());

        IDocument doc = new Document(sb.toString());
        DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(doc, new FormatterPreferences(unit), indentLevel);
        TextEdit edit = formatter.format();
        edit.apply(doc);

        return doc.get();
    }

    private TextEditGroup declarationToReferenceTextEditGroup() {
        TextEditGroup group = new TextEditGroup("Convert local variable declaration to reference.");
        if (!getContainingMethodNode().isScriptBody() || getContainingClosureExpression() != null) {
            int typeOrDefLength = variableExpressionInDeclaration.getStart() - getDeclarationOffset();
            group.addTextEdit(new ReplaceEdit(getDeclarationOffset(), typeOrDefLength, ""));
        }
        return group;
    }

    private TextEditGroup renameVariableReferencesTextEditGroup(RefactoringStatus status) {
        TextEditGroup group = new TextEditGroup("Update local variables to reference field.");
        if (!getContainingMethodNode().isScriptBody() || getContainingClosureExpression() != null) {
            final Set<VariableExpression> references = new HashSet<>();
            GroovyClassVisitor referencesVisitor = new DepthFirstVisitor() {
                @Override
                public void visitVariableExpression(VariableExpression variableExpression) {
                    if (variableExpression.getAccessedVariable() == variableExpressionInDeclaration && variableExpression.getLineNumber() >= 0) {
                        references.add(variableExpression);
                    }
                }
            };

            referencesVisitor.visitClass(getContainingClassNode());
            Iterator<InnerClassNode> innerClasses = getContainingClassNode().getInnerClasses();
            while (innerClasses != null && innerClasses.hasNext()) {
                ClassNode innerClass = innerClasses.next();
                referencesVisitor.visitClass(innerClass);
            }

            for (VariableExpression reference : references) {
                if (getUsedVariableAndFieldNames(reference).contains(getFieldName())) {
                    status.merge(RefactoringStatus.createWarningStatus("New field conflicts with existing name."));
                }

                group.addTextEdit(new ReplaceEdit(reference.getStart(), reference.getLength(), getFieldName()));
            }
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

    private ModuleNode getModuleNode() {
        if (moduleNode == null) {
            moduleNode = unit.getModuleNode();
        }
        return moduleNode;
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
            containingClassNode = ASTTools.getContainingClassNode(moduleNode, variableExpressionInDeclaration.getStart());
        }
        return containingClassNode;
    }

    private MethodNode getContainingMethodNode() {
        if (methodNode == null) {
            methodNode = new VariableExpressionFinder(variableExpressionInDeclaration).method;
        }
        return methodNode;
    }

    private ClosureExpression getContainingClosureExpression() {
        return new VariableExpressionFinder(variableExpressionInDeclaration).closure;
    }

    private DeclarationExpression getDeclarationExpression(final VariableExpression variableExpressionInDeclaration) {
        if (declarationExpression == null) {
            declarationExpression = new VariableExpressionFinder(variableExpressionInDeclaration).declaration;
        }
        return declarationExpression;
    }

    private int getDeclarationOffset() {
        if (declarationExpression.getEnd() > 0) {
            return declarationExpression.getStart();
        }
        if (!declarationExpression.getAnnotations().isEmpty()) {
            return declarationExpression.getAnnotations().get(0).getStart() - 1;
        }
        // declaration expression sloc may not be set (ex: @Newify local variable expression)
        throw new IllegalStateException("No start offset for declaration expression on line " + variableExpressionInDeclaration.getLineNumber());
    }

    /**
     * Adapted from ExtractGroovyLocalRefactoring#getParentStack and
     * ExtractGroovyLocalRefactoring#getExcludedVariableNames
     */
    private Set<String> getUsedVariableAndFieldNames(VariableExpression variableExpression) {
        FindSurroundingNode find = new FindSurroundingNode(new Region(variableExpression), VisitKind.PARENT_STACK);
        find.doVisitSurroundingNode(moduleNode);
        List<IASTFragment> parentStack = new ArrayList<>(find.getParentStack());
        Collections.reverse(parentStack);

        Set<String> result = new HashSet<>();
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

    private boolean isTailRecursiveMethodParameter(VariableExpression variableExpression) {
        MethodNode method = new VariableExpressionFinder(variableExpression).method;
        if (method != null) {
            for (AnnotationNode annotation : method.getAnnotations()) {
                if (annotation.getClassNode().getName().equals("groovy.transform.TailRecursive")) {
                    return variableExpression.getName().matches("_\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*_");
                }
            }
        }
        return false;
    }

    private class VariableExpressionFinder extends DepthFirstVisitor {
        DeclarationExpression declaration;
        ClosureExpression closure;
        MethodNode method;

        VariableExpressionFinder(final VariableExpression variableExpressionInDeclaration) {
            DepthFirstVisitor visitor = new DepthFirstVisitor() {
                @Override
                public void visitMethod(MethodNode node) {
                    method = node;
                    super.visitMethod(node);
                    method = null;
                }

                @Override
                public void visitClosureExpression(ClosureExpression expr) {
                    closure = expr;
                    super.visitClosureExpression(expr);
                    closure = null;
                }

                @Override
                public void visitDeclarationExpression(DeclarationExpression expr) {
                    declaration = expr;
                    super.visitDeclarationExpression(expr);
                    declaration = null;
                }

                @Override
                public void visitVariableExpression(VariableExpression expr) {
                    if (expr == variableExpressionInDeclaration) {
                        throw new VisitCompleteException();
                    }
                    super.visitVariableExpression(expr);
                }
            };

            try {
                visitor.visitModule(getModuleNode());
            } catch (VisitCompleteException expected) {
                ;
            }
        }
    }
}
