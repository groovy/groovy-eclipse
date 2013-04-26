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

import groovyjarjarasm.asm.Opcodes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.StringTokenizer;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetParser;
import org.codehaus.groovy.eclipse.refactoring.Activator;
import org.codehaus.groovy.eclipse.refactoring.core.rewriter.ASTWriter;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences;
import org.codehaus.groovy.eclipse.refactoring.ui.extract.GroovyRefactoringMessages;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * @author Michael Klenk mklenk@hsr.ch
 * @author Andrew Eisenberg
 */
public class ExtractGroovyMethodRefactoring extends Refactoring {

    private class GroovyRefactoringObservable extends Observable {
        @Override
        protected synchronized void setChanged() {
            super.setChanged();
        }

        @Override
        public void notifyObservers(Object arg) {
            super.notifyObservers(arg);
            clearChanged();
        }
    }

    private GroovyRefactoringObservable observable = new GroovyRefactoringObservable();

    private String newMethodName = "";

    private MethodNode newMethod;

    private BlockStatement block;

    private int newMethodModifier = Flags.AccDefault;

    /**
     * Text that will be replaced by the refactoring
     */
    private Region replaceScope;

    /**
     * Text that is currently selected
     */
    private Region selectedText;

    private StatementFinder methodCodeFinder;

    private boolean returnMustBeDeclared = false;

    /**
     * Two collections since the variables in the methodCall
     * and in the signature of the method can be different
     */
    private List<Variable> actualParameters;

    private List<ClassNode> inferredTypeOfActualParameters;

    private List<Variable> originalParametersBeforeRename;

    /**
     * Although we can determine if there are multiple return parameters
     * we only support on return parameter
     */
    private Set<Variable> returnParameters;

    private List<ClassNode> inferredReturnTypes;


    private Map<String, String> variablesToRename;

    protected IPreferenceStore refactoringPreferences;

    private GroovyCompilationUnit unit;

    private CompilationUnitChange change;

    public ExtractGroovyMethodRefactoring(GroovyCompilationUnit unit, int offset, int length, RefactoringStatus status) {
        this.refactoringPreferences = Activator.getDefault().getPreferenceStore();
        this.selectedText = new Region(offset, length);
        this.unit = unit;
        initializeExtractedStatements(status);
    }

    public ExtractGroovyMethodRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
        status.merge(initialize(arguments));
        initializeExtractedStatements(status);
    }

    private void initializeExtractedStatements(RefactoringStatus status) {
        StatementFinder f;
        try {
            f = new StatementFinder(selectedText, this.unit.getModuleNode());
        } catch (Exception e) {
            status.addFatalError(e.getMessage(), createErrorContext());
            f = null;
        }
        methodCodeFinder = f;
        createBlockStatement();
        updateMethod();
        saveOriginalParameters();
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        RefactoringStatus status = new RefactoringStatus();
        pm.beginTask("Checking initial conditions for extract method", 100); //$NON-NLS-1$

        updateMethod();

        if (pm.isCanceled()) {
            throw new OperationCanceledException();
        }
        status.merge(checkNrOfReturnValues(pm));

        if (pm.isCanceled()) {
            throw new OperationCanceledException();
        }

        status.merge(checkStatementSelection(pm));

        if (pm.isCanceled()) {
            throw new OperationCanceledException();
        }

        status.merge(checkExtractFromConstructor(pm));
        if (pm.isCanceled()) {
            throw new OperationCanceledException();
        }

        pm.done();
        return status;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        RefactoringStatus stat = new RefactoringStatus();
        stat.merge(checkDuplicateMethod(pm));

        change = new CompilationUnitChange(GroovyRefactoringMessages.ExtractMethodRefactoring, unit);
        change.setEdit(new MultiTextEdit());
        if (newMethod != null) {
            TextEditGroup group = new TextEditGroup("Replace existing code with call to new method", createMethodCallEdit());
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            change.addEdit(group.getTextEdits()[0]);

            group = new TextEditGroup("Declaration of extracted method", createMethodDeclarationEdit(stat));
            change.addChangeGroup(new TextEditChangeGroup(change, group));
            change.addEdit(group.getTextEdits()[0]);
        }

        return stat;
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return change;
    }

    @Override
    public String getName() {
        return "Extract Groovy Method";
    }

    /**
     * For testing, override actual preferences with test-specific ones
     *
     * @param preferences
     */
    public void setPreferences(IPreferenceStore preferences) {
        this.refactoringPreferences = preferences;
    }

    private RefactoringStatus initialize(JavaRefactoringArguments arguments) {
        final String selection = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION);
        if (selection == null) {
            return RefactoringStatus.createFatalErrorStatus(Messages.format(
                    RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                    JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION));
        }

        int offset = -1;
        int length = -1;
        final StringTokenizer tokenizer = new StringTokenizer(selection);
        if (tokenizer.hasMoreTokens())
            offset = Integer.valueOf(tokenizer.nextToken()).intValue();
        if (tokenizer.hasMoreTokens())
            length = Integer.valueOf(tokenizer.nextToken()).intValue();
        if (offset < 0 || length < 0)
            return RefactoringStatus.createFatalErrorStatus(Messages.format(
                    RefactoringCoreMessages.InitializableRefactoring_illegal_argument, new Object[] { selection,
                            JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION }));
        selectedText = new Region(offset, length);

        final String handle = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
        if (handle == null)
            return RefactoringStatus.createFatalErrorStatus(Messages.format(
                    RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                    JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));

        IJavaElement element = JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
        if (element == null || !element.exists() || element.getElementType() != IJavaElement.COMPILATION_UNIT
                || !(element instanceof GroovyCompilationUnit))
            return JavaRefactoringDescriptorUtil.createInputFatalStatus(element, getName(), IJavaRefactorings.EXTRACT_METHOD);
        unit = (GroovyCompilationUnit) element;

        final String name = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
        if (name == null || name.length() == 0)
            return RefactoringStatus.createFatalErrorStatus(Messages.format(
                    RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                    JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME));
        newMethodName = name;

        return new RefactoringStatus();
    }

    private void saveOriginalParameters() {
        originalParametersBeforeRename = new ArrayList<Variable>();
        if (newMethod != null && newMethod.getParameters() != null) {
            for (Parameter p : newMethod.getParameters()) {
                originalParametersBeforeRename.add(p);
            }
        }
    }

    public void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void setNewMethodname(String newMethodname) {
        this.newMethodName = newMethodname;
        updateMethod();
        observable.setChanged();
        observable.notifyObservers();
    }

    public String getNewMethodName() {
        return newMethodName;
    }

    public void setModifier(int modifier) {
        this.newMethodModifier = modifier;
        updateMethod();
        observable.setChanged();
        observable.notifyObservers();
    }

    public int getModifier() {
        return newMethodModifier;
    }

    private void setCallAndMethHeadParameters(List<Variable> params) {
        actualParameters = params;
        inferredTypeOfActualParameters.clear();
        for (Variable variable : params) {
            inferredTypeOfActualParameters.add(variable.getType());
        }
        updateMethod();
    }

    public Parameter[] getCallAndMethHeadParameters() {
        Parameter[] params = new Parameter[actualParameters.size()];

        for (int i = 0; i < params.length; i++) {
            Variable v = actualParameters.get(i);
            ClassNode t = inferredTypeOfActualParameters.get(i);
            Parameter tmpParam = new Parameter(t, v.getName());
            params[i] = tmpParam;
        }
        return params;
    }

    /**
     * MethodCallExpression to call the newly generated method
     *
     * @return String containing the call
     */
    public String getMethodCall() {

        Expression objExp = new VariableExpression("this");
        ArgumentListExpression arguments = new ArgumentListExpression();

        for (Variable param : originalParametersBeforeRename) {
            arguments.addExpression(new VariableExpression(param.getName(),
                    param.getOriginType() == null ? ClassHelper.DYNAMIC_TYPE : param.getOriginType()));
        }

        MethodCallExpression newMethodCall = new MethodCallExpression(objExp, newMethodName, arguments);

        ASTWriter writer = new ASTWriter(unit.getModuleNode(), replaceScope.getOffset(), null);

        if (returnParameters.size() > 0) {
            visitExpressionsForReturnStmt(newMethodCall, writer);
        } else {
            writer.visitMethodCallExpression(newMethodCall);
        }

        return writer.getGroovyCode();
    }

    private void visitExpressionsForReturnStmt(MethodCallExpression newMethodCall, ASTWriter astw) {
        Assert.isTrue(returnParameters.size() > 0);
        Variable retVar = returnParameters.iterator().next();

        if (returnMustBeDeclared) {
            VariableExpression varExp = new VariableExpression(retVar);
            DeclarationExpression declarationExpression = new DeclarationExpression(varExp, Token.newSymbol(Types.ASSIGN, -1, -1),
                    newMethodCall);
            astw.visitDeclarationExpression(declarationExpression);
        } else {
            BinaryExpression binaryExpression = new BinaryExpression(new VariableExpression(retVar), Token.newSymbol(Types.ASSIGN,
                    -1, -1), newMethodCall);
            astw.visitBinaryExpression(binaryExpression);
        }
    }

    /**
     * Return a method head for preview
     *
     * @return
     */
    public String getMethodHead() {
        updateMethod();

        ASTWriter astw = new ASTWriter(unit.getModuleNode(), null);
        astw.visitMethod(newMethod);
        String head = astw.getGroovyCode();

        int headEndPos = head.indexOf(")");
        return head.substring(0, headEndPos + 1).trim();
    }

    /**
     * create the method node with all given parameters
     */
    private void updateMethod() {

        // rearrange parameters if necessary
        if (block.getStatements().size() > 0) {
            Parameter[] params = getCallAndMethHeadParameters();
            ClassNode returnType = ClassHelper.DYNAMIC_TYPE;
            if (returnParameters.size() > 0) {
                returnType = inferredReturnTypes.get(0);
                if (returnType.equals(VariableScope.OBJECT_CLASS_NODE)) {
                    returnType = ClassHelper.DYNAMIC_TYPE;
                }
            }
            newMethod = new MethodNode(newMethodName, 0, returnType, params, null, block);

            checkStaticModifier();
        }
    }

    private void checkStaticModifier() {
        if (methodCodeFinder.isStatic()) {
            newMethod.setModifiers(newMethodModifier | Opcodes.ACC_STATIC);
        } else {
            newMethod.setModifiers(newMethodModifier);
        }
    }

    public boolean isStatic() {
        return methodCodeFinder.isStatic();
    }

    /**
     * determine the statements in the new method
     * also determine the parameters and return type
     */
    private void createBlockStatement() {
        block = new BlockStatement();
        block.addStatements(methodCodeFinder.getInSelection());

        replaceScope = ASTTools.getPositionOfBlockStatements(block);
        defineActualAndReturnParameters();
    }

    /**
     * Determines parameters, return parameters, and inferred types
     */
    private void defineActualAndReturnParameters() {

        // Read used Variables
        ASTVariableScanner scanner = new ASTVariableScanner(methodCodeFinder.isInLoopOrClosure());
        scanner.visitNode(block);

        ASTVariableScanner postSelectionScanner = new ASTVariableScanner(methodCodeFinder.isInLoopOrClosure());
        BlockStatement postBlock = new BlockStatement();
        postBlock.addStatements(methodCodeFinder.getPostSelection());
        postSelectionScanner.visitNode(postBlock);
        Set<Variable> postUsedVar = postSelectionScanner.getUsedVariables();
        Set<Variable> selReturnVar = scanner.getAssignedVariables();
        Set<Variable> innerLoopAssigned = scanner.getInnerLoopAssignedVariables();

        actualParameters = new ArrayList<Variable>(scanner.getUsedVariables());
        inferredTypeOfActualParameters = new ArrayList<ClassNode>(actualParameters.size());
        returnParameters = new HashSet<Variable>();
        inferredReturnTypes = new ArrayList<ClassNode>();

        // Variables that are assigned in the block AND used after it are the
        // ones that should be added as return parameters.
        Set<Variable> assignedInBlockAndUsedAfterBlock = new HashSet<Variable>(postUsedVar);
        assignedInBlockAndUsedAfterBlock.retainAll(selReturnVar);

        returnParameters.addAll(assignedInBlockAndUsedAfterBlock);
        // add variables used in the loop
        returnParameters.addAll(innerLoopAssigned);

        // check to see if we need to declare the return
        for (Variable variable : returnParameters) {
            if (postUsedVar.contains(variable) && scanner.getDeclaratedVariables().contains(variable)) {
                returnMustBeDeclared = true;
                break;
            }
        }

        // now try to infer the variable types
        InferParameterAndReturnTypesRequestor inferRequestor = new InferParameterAndReturnTypesRequestor(actualParameters,
                returnParameters, selectedText);
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
        visitor.visitCompilationUnit(inferRequestor);

        Map<Variable, ClassNode> inferredTypes = inferRequestor.getInferredTypes();
        for (Variable variable : actualParameters) {
            if (inferredTypes.containsKey(variable)) {
                ClassNode type = inferredTypes.get(variable);
                if (type == null || VariableScope.isVoidOrObject(type)) {
                    inferredTypeOfActualParameters.add(ClassHelper.DYNAMIC_TYPE);
                } else {
                    // force using a cached type so that getUnwrapper will work
                    inferredTypeOfActualParameters.add(maybeConvertToPrimitiveType(type));
                }
            } else {
                inferredTypeOfActualParameters.add(ClassHelper.DYNAMIC_TYPE);
            }
        }

        for (Variable variable : returnParameters) {
            if (inferredTypes.containsKey(variable)) {
                // force using a cached type so that getUnwrapper will work
                inferredReturnTypes.add(maybeConvertToPrimitiveType(inferredTypes.get(variable)));
            } else {
                inferredReturnTypes.add(variable.getOriginType());
            }
        }
    }

    private ClassNode maybeConvertToPrimitiveType(ClassNode type) {
        return ClassHelper.getUnwrapper(type).getPlainNodeReference();
    }

    private RefactoringStatus checkDuplicateMethod(IProgressMonitor pm) {
        SubProgressMonitor sub = new SubProgressMonitor(pm, 25);
        sub.beginTask("Checking for duplicate methods", 25);
        RefactoringStatus stat = new RefactoringStatus();
        if (getMethodNames().contains(newMethodName)) {
            Object[] message = { newMethodName, getClassName() };
            String messageString = MessageFormat.format(GroovyRefactoringMessages.ExtractMethodWizard_MethodNameAlreadyExists,
                    message);
            stat.addError(messageString);
        }
        sub.done();
        return stat;
    }

    private RefactoringStatus checkExtractFromConstructor(IProgressMonitor pm) {
        SubProgressMonitor sub = new SubProgressMonitor(pm, 25);
        sub.beginTask("Checking for constructor calls", 25);
        RefactoringStatus stat = new RefactoringStatus();
        if (methodCodeFinder.isInConstructor()) {
            if (new ExtractConstructorTest().containsConstructorCall(newMethod)) {
                stat.addFatalError(GroovyRefactoringMessages.ExtractMethodInfo_NoExtractionOfConstructorCallinConstructor,
                        createErrorContext());
            }
        }
        sub.done();
        return stat;
    }

    private RefactoringStatus checkStatementSelection(IProgressMonitor pm) {
        SubProgressMonitor sub = new SubProgressMonitor(pm, 25);
        sub.beginTask("Checking statement selection", 25);
        RefactoringStatus stat = new RefactoringStatus();
        int selectionLength = selectedText.getLength();
        if (block.isEmpty() && selectionLength >= 0) {
            stat.addFatalError(GroovyRefactoringMessages.ExtractMethodInfo_NoStatementSelected, createErrorContext());
        }
        sub.done();
        return stat;
    }

    private RefactoringStatus checkNrOfReturnValues(IProgressMonitor pm) {
        SubProgressMonitor sub = new SubProgressMonitor(pm, 25);
        sub.beginTask("Checking number of return values", 25);
        RefactoringStatus stat = new RefactoringStatus();
        if (returnParameters != null && returnParameters.size() > 1) {
            StringBuilder retValues = new StringBuilder();
            for (Variable var : returnParameters) {
                retValues.append(var.getType().getNameWithoutPackage() + " " + var.getName() + "\n");
            }
            String errorMsg = GroovyRefactoringMessages.ExtractMethodInfo_ToMuchReturnValues + retValues.toString();
            stat.addFatalError(errorMsg, createErrorContext());
        }
        sub.done();
        return stat;
    }

    /**
     * Return the Code of the new Method as a formated IDocument
     *
     * @param status
     *
     * @return
     */
    private String createCopiedMethodCode(RefactoringStatus status) {

        IDocument unitDocument = new Document(String.valueOf(unit.getContents()));
        String lineDelimiter = TextUtilities.getDefaultLineDelimiter(unitDocument);

        StringBuilder sb = new StringBuilder();
        try {
            final FormatterPreferences formmatterPrefs = new FormatterPreferences(unit);
            int indentLevel = calculateIndentation();
            String indentation = CodeFormatterUtil.createIndentString(indentLevel, unit.getJavaProject());
            sb.append(lineDelimiter + lineDelimiter + indentation);
            sb.append(getMethodHead()).append(" {").append(lineDelimiter);
            // copy the source code
            String copyOfSourceCode = unitDocument.get(replaceScope.getOffset(), replaceScope.getLength());
            sb.append(copyOfSourceCode);
            sb.append(lineDelimiter);

            ASTWriter astw = writeReturnStatements(unitDocument);
            if (astw.getGroovyCode().length() > 0) {
                sb.append(astw.getGroovyCode());
            }
            sb.append("}");

            MethodNode newMethod = createNewMethodForValidation(sb.toString(), status);

            IDocument newMethodDocument = new Document(sb.toString());
            if (newMethod != null && variablesToRename != null) {
                MultiTextEdit edits = renameVariableInExtractedMethod(newMethod);
                edits.apply(newMethodDocument);
            }

            DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(newMethodDocument, formmatterPrefs, indentLevel);
            formatter.format().apply(newMethodDocument);
            return newMethodDocument.get();
        } catch (BadLocationException e) {
            status
                    .addFatalError("Problem when creating the body of the extracted method.\n" + e.getMessage(),
                            createErrorContext());
            GroovyCore.logException("Problem when creating the body of the extracted method.", e);
        }
        return sb.toString();
    }

    /**
     * @param string
     * @param status
     * @return may return null if there is a parse problem
     */
    private MethodNode createNewMethodForValidation(String methodText, RefactoringStatus status) {
        try {
            GroovySnippetParser parser = new GroovySnippetParser();
            ModuleNode module = parser.parse(methodText);
            if (module.getMethods() == null || module.getMethods().size() != 1) {
                status.addError("Problem parsing extracted method", createErrorContext());
                if (module.getMethods() == null) {
                    return null;
                }
            }
            MethodNode method = (MethodNode) module.getMethods().get(0);
            new VariableScopeVisitor(null).visitClass(method.getDeclaringClass());
            return method;
        } catch (Exception e) {
            // probably bad syntax
            status.addError("Problem parsing extracted method.\n" + e.getMessage(), createErrorContext());
        }
        return null;
    }

    /**
     * @return
     */
    private FileStatusContext createErrorContext() {
        return new FileStatusContext((IFile) unit.getResource(), new org.eclipse.jface.text.Region(selectedText.getOffset(),
                selectedText.getLength()));
    }

    /**
     * @return indentation level, given in 'indentation units'.
     */
    private int calculateIndentation() {
        int defaultIndentation;
        if (methodCodeFinder.getClassNode().isScript()) {
            defaultIndentation = 0;
        } else {
            // must handle inner classes
            int innerClassCount = 0;
            ClassNode current = methodCodeFinder.getClassNode();
            while (current != null) {
                innerClassCount++;
                if (current.getEnclosingMethod() != null) {
                    innerClassCount++;
                    current = current.getEnclosingMethod().getDeclaringClass();
                    continue;
                }
                current = current.getDeclaringClass();
            }
            defaultIndentation = innerClassCount;
        }
        return defaultIndentation;
    }

    private MultiTextEdit renameVariableInExtractedMethod(MethodNode method) {
        VariableRenamer renamer = new VariableRenamer();
        return renamer.rename(method, variablesToRename);
    }

    private ASTWriter writeReturnStatements(IDocument document) {
        ASTWriter astw = new ASTWriter(unit.getModuleNode(), document);

        for (Variable var : returnParameters) {
            ReturnStatement ret = new ReturnStatement(new VariableExpression(var));
            astw.visitReturnStatement(ret);
            astw.insertLineFeed();
        }
        return astw;
    }

    private InsertEdit createMethodDeclarationEdit(RefactoringStatus status) {
        String newMethodCode = createCopiedMethodCode(status);
        return new InsertEdit(methodCodeFinder.getSelectedDeclaration().getEnd(), newMethodCode);
    }

    private ReplaceEdit createMethodCallEdit() {
        int offset = replaceScope.getOffset();
        int length = replaceScope.getLength();

        ReplaceEdit insertMethodCall = new ReplaceEdit(offset, length, getMethodCall());
        return insertMethodCall;
    }

    public List<String> getMethodNames() {
        return methodCodeFinder.getMethodNames();
    }

    public String getClassName() {
        return methodCodeFinder.getClassName();
    }

    /**
     * @param variName
     * @param upEvent true if the move is upwards
     * @param numberOfMoves mostly 1, can be more for tests
     * @return the index of the selected variable in the collection
     */
    public int setMoveParameter(String variName, boolean upEvent, int numberOfMoves) {

        Parameter[] originalParams = getCallAndMethHeadParameters();
        List<Variable> newParamList = new ArrayList<Variable>();

        int indexOfSelectedParam = -1;
        for (Parameter param : originalParams) {
            newParamList.add(param);
            if (param.getName().equals(variName)) {
                indexOfSelectedParam = newParamList.indexOf(param);
            }
        }

        indexOfSelectedParam = reorderParameters(upEvent, numberOfMoves, newParamList, indexOfSelectedParam);

        setCallAndMethHeadParameters(newParamList);

        return indexOfSelectedParam;
    }

    private int reorderParameters(boolean upEvent, int numberOfMoves, List<Variable> newParamList, int index) {
        int indexOfSelectedParam = index;
        // also reorder in originals!
        Variable variToMove = newParamList.remove(indexOfSelectedParam);
        Variable originalToMove = originalParametersBeforeRename.remove(indexOfSelectedParam);

        indexOfSelectedParam = calculateNewIndexAfterMove(upEvent, numberOfMoves, newParamList, indexOfSelectedParam);

        newParamList.add(indexOfSelectedParam, variToMove);
        originalParametersBeforeRename.add(indexOfSelectedParam, originalToMove);
        return indexOfSelectedParam;
    }

    private int calculateNewIndexAfterMove(boolean upEvent, int numberOfMoves, List<Variable> newParamList, int index) {
        int indexOfSelectedParam = index;
        if (upEvent) {
            if (indexOfSelectedParam < 1)
                indexOfSelectedParam = 0;
            else
                indexOfSelectedParam -= numberOfMoves;
        } else {
            if (indexOfSelectedParam > newParamList.size() - 1)
                indexOfSelectedParam = newParamList.size() - 1;
            else
                indexOfSelectedParam += numberOfMoves;
        }
        return indexOfSelectedParam;
    }

    public void setParameterRename(Map<String, String> variablesToRename) {

        this.variablesToRename = variablesToRename;
        List<Variable> newParamList = new ArrayList<Variable>();

        for (Variable param : originalParametersBeforeRename) {
            if (variablesToRename.containsKey(param.getName())) {
                // there's an entry for this variable in the map, therefore
                // rename
                newParamList.add(new Parameter(param.getOriginType(), variablesToRename.get(param.getName())));
            } else {
                newParamList.add(param);
            }
        }
        setCallAndMethHeadParameters(newParamList);
        observable.setChanged();
        observable.notifyObservers();
    }

    public String getOriginalParameterName(int selectionIndex) {
        return originalParametersBeforeRename.get(selectionIndex).getName();
    }

}
