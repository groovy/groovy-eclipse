/*
 * Copyright 2009-2021 the original author or authors.
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

import static org.codehaus.groovy.ast.tools.GeneralUtils.ASSIGN;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.Expression;
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
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
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

public class ExtractGroovyMethodRefactoring extends Refactoring {

    private String newMethodName = "";

    private MethodNode newMethod;

    private BlockStatement block;

    private int newMethodModifier = Flags.AccDefault;

    /**
     * Text that will be replaced by the refactoring.
     */
    private Region replaceScope;

    /**
     * Text that is currently selected.
     */
    private Region selectedText;

    private StatementFinder methodCodeFinder;

    private boolean returnMustBeDeclared;

    private List<Variable> actualParameters;

    private List<ClassNode> inferredTypeOfActualParameters;

    private List<Variable> originalParametersBeforeRename;

    // Although we can determine if there are multiple return parameters we only support on return parameter.
    private Set<Variable> returnParameters;

    private List<ClassNode> inferredReturnTypes;

    private Map<String, String> variablesToRename;

    protected IPreferenceStore refactoringPreferences;

    private GroovyCompilationUnit unit;

    private CompilationUnitChange change;

    private final GroovyRefactoringObservable observable = new GroovyRefactoringObservable();

    public ExtractGroovyMethodRefactoring(final GroovyCompilationUnit unit, final int offset, final int length, final RefactoringStatus status) {
        this.unit = unit;
        this.selectedText = new Region(offset, length);
        this.refactoringPreferences = Activator.getDefault().getPreferenceStore();
        initializeExtractedStatements(status);
    }

    public ExtractGroovyMethodRefactoring(final JavaRefactoringArguments arguments, final RefactoringStatus status) {
        status.merge(initialize(arguments));
        initializeExtractedStatements(status);
    }

    private void initializeExtractedStatements(final RefactoringStatus status) {
        try {
            methodCodeFinder = new StatementFinder(selectedText, unit.getModuleNode());
            createBlockStatement();
            updateMethod();
            saveOriginalParameters();
        } catch (Exception e) {
            status.addFatalError(e.getMessage(), createErrorContext());
        }
    }

    @Override
    public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        RefactoringStatus status = new RefactoringStatus();
        monitor.beginTask("Checking initial conditions for extract method", 100);

        updateMethod();

        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        status.merge(checkNrOfReturnValues(monitor));

        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        status.merge(checkStatementSelection(monitor));

        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        status.merge(checkExtractFromConstructor(monitor));
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        return status;
    }

    @Override
    public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        RefactoringStatus stat = new RefactoringStatus();
        stat.merge(checkDuplicateMethod(monitor));

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
    public Change createChange(final IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        return change;
    }

    @Override
    public String getName() {
        return "Extract Groovy Method";
    }

    /**
     * For testing, override actual preferences with test-specific ones
     */
    public void setPreferences(final IPreferenceStore preferences) {
        this.refactoringPreferences = preferences;
    }

    private RefactoringStatus initialize(final JavaRefactoringArguments arguments) {
        String selection = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION);
        if (selection == null) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.bind(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION));
        }

        int offset = -1;
        int length = -1;
        StringTokenizer tokenizer = new StringTokenizer(selection);
        if (tokenizer.hasMoreTokens())
            offset = Integer.valueOf(tokenizer.nextToken()).intValue();
        if (tokenizer.hasMoreTokens())
            length = Integer.valueOf(tokenizer.nextToken()).intValue();
        if (offset < 0 || length < 0) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.bind(
                RefactoringCoreMessages.InitializableRefactoring_illegal_argument, selection, JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION));
        }
        selectedText = new Region(offset, length);

        String handle = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
        if (handle == null) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.bind(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
        }

        IJavaElement element = JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
        if (element == null || !element.exists() || element.getElementType() != IJavaElement.COMPILATION_UNIT || !(element instanceof GroovyCompilationUnit)) {
            return JavaRefactoringDescriptorUtil.createInputFatalStatus(element, getName(), IJavaRefactorings.EXTRACT_METHOD);
        }
        unit = (GroovyCompilationUnit) element;

        String name = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
        if (name == null || name.length() == 0) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.bind(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME));
        }
        newMethodName = name;

        return new RefactoringStatus();
    }

    private void saveOriginalParameters() {
        originalParametersBeforeRename = new ArrayList<>();
        if (newMethod != null && newMethod.getParameters() != null) {
            for (Parameter p : newMethod.getParameters()) {
                originalParametersBeforeRename.add(p);
            }
        }
    }

    public void addObserver(final Observer observer) {
        observable.addObserver(observer);
    }

    public void setNewMethodname(final String newMethodname) {
        this.newMethodName = newMethodname;
        updateMethod();
        observable.setChanged();
        observable.notifyObservers();
    }

    public String getNewMethodName() {
        return newMethodName;
    }

    public void setModifier(final int modifier) {
        this.newMethodModifier = modifier;
        updateMethod();
        observable.setChanged();
        observable.notifyObservers();
    }

    public int getModifier() {
        return newMethodModifier;
    }

    private void setCallAndMethHeadParameters(final List<Variable> params) {
        actualParameters = params;
        inferredTypeOfActualParameters.clear();
        for (Variable variable : params) {
            inferredTypeOfActualParameters.add(variable.getType());
        }
        updateMethod();
    }

    public Parameter[] getCallAndMethHeadParameters() {
        Parameter[] params = new Parameter[actualParameters.size()];
        for (int i = 0, n = params.length; i < n; i += 1) {
            Variable v = actualParameters.get(i);
            ClassNode t = inferredTypeOfActualParameters.get(i);
            Parameter tmpParam = new Parameter(t, v.getName());
            params[i] = tmpParam;
        }
        return params;
    }

    /**
     * Generates the new method call source code.
     */
    public String getMethodCall() {
        List<Expression> arguments = originalParametersBeforeRename.stream().map(p ->
            varX(p.getName(), Optional.ofNullable(p.getOriginType()).orElseGet(ClassHelper::dynamicType))
        ).collect(Collectors.toList());
        Expression newMethodCall = callThisX(newMethodName, args(arguments));

        ASTWriter writer = new ASTWriter(unit.getModuleNode(), replaceScope.getOffset(), null);

        if (!returnParameters.isEmpty()) {
            Expression retVar = varX(returnParameters.iterator().next());
            if (returnMustBeDeclared) {
                declS(retVar, newMethodCall).visit(writer);
            } else {
                binX(retVar, ASSIGN, newMethodCall).visit(writer);
            }
        } else {
            newMethodCall.visit(writer);
        }

        return writer.getGroovyCode();
    }

    public String getMethodHead() {
        updateMethod();

        ASTWriter astw = new ASTWriter(unit.getModuleNode(), null);
        astw.visitMethod(newMethod);
        String head = astw.getGroovyCode();

        int headEndPos = head.indexOf(")");
        return head.substring(0, headEndPos + 1).trim();
    }

    private void updateMethod() {
        // rearrange parameters if necessary
        if (!block.getStatements().isEmpty()) {
            Parameter[] params = getCallAndMethHeadParameters();
            ClassNode returnType;
            if (!returnParameters.isEmpty()) {
                returnType = inferredReturnTypes.get(0);
            } else {
                returnType = ClassHelper.dynamicType();
            }
            newMethod = new MethodNode(newMethodName, 0, returnType, params, null, block);

            checkStaticModifier();
        }
    }

    private void checkStaticModifier() {
        if (methodCodeFinder.isStatic()) {
            newMethod.setModifiers(newMethodModifier | Flags.AccStatic);
        } else {
            newMethod.setModifiers(newMethodModifier);
        }
    }

    public boolean isStatic() {
        return methodCodeFinder.isStatic();
    }

    /**
     * Determines the statements in the new method and the parameters and return type.
     */
    private void createBlockStatement() {
        block = new BlockStatement();
        block.addStatements(methodCodeFinder.getInSelection());
        replaceScope = ASTTools.getPositionOfBlockStatements(block);
        Assert.isLegal(replaceScope.getOffset() >= 0, "Replace scope has bad offset: " + replaceScope.getOffset());
        Assert.isLegal(replaceScope.getLength() >= 0, "Replace scope has bad length: " + replaceScope.getLength());

        defineActualAndReturnParameters();
    }

    /**
     * Determines parameters, return parameters, and inferred types.
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

        actualParameters = new ArrayList<>(scanner.getUsedVariables());
        inferredTypeOfActualParameters = new ArrayList<>(actualParameters.size());
        returnParameters = new HashSet<>();
        inferredReturnTypes = new ArrayList<>();

        // Variables that are assigned in the block AND used after it are the
        // ones that should be added as return parameters.
        Set<Variable> assignedInBlockAndUsedAfterBlock = new HashSet<>(postUsedVar);
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
        InferParameterAndReturnTypesRequestor requestor = new InferParameterAndReturnTypesRequestor(actualParameters, returnParameters, selectedText);
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
        visitor.visitCompilationUnit(requestor);

        Map<Variable, ClassNode> inferredTypes = requestor.getInferredTypes();
        for (Variable variable : actualParameters) {
            ClassNode type = Optional.ofNullable(inferredTypes.get(variable)).filter(t -> !VariableScope.isVoidOrObject(t))
                .map(ExtractGroovyMethodRefactoring::normalizeInferredType)
                .orElseGet(ClassHelper::dynamicType);
            inferredTypeOfActualParameters.add(type);
        }

        for (Variable variable : returnParameters) {
            ClassNode type = Optional.ofNullable(inferredTypes.get(variable))
                .map(ExtractGroovyMethodRefactoring::normalizeInferredType)
                .orElseGet(ClassHelper::dynamicType);
            inferredReturnTypes.add(type);
        }
    }

    private static ClassNode normalizeInferredType(final ClassNode t) {
        if (ClassHelper.isDynamicTyped(t)) {
            return null;
        }
        if (ClassHelper.isPrimitiveType(t)) {
            return t.redirect();
        }
        if (t.equals(VariableScope.OBJECT_CLASS_NODE)) {
            return VariableScope.OBJECT_CLASS_NODE;
        }
        if (t.equals(VariableScope.GSTRING_CLASS_NODE)) {
            return VariableScope.STRING_CLASS_NODE;
        }
        return ClassHelper.getUnwrapper(t).getPlainNodeReference();
    }

    private RefactoringStatus checkDuplicateMethod(final IProgressMonitor monitor) {
        SubMonitor.convert(monitor, "Checking for duplicate methods", 25);
        RefactoringStatus stat = new RefactoringStatus();
        if (getMethodNames().contains(newMethodName)) {
            stat.addError(GroovyRefactoringMessages.bind(GroovyRefactoringMessages.ExtractMethodWizard_MethodNameAlreadyExists, newMethodName, getClassName()));
        }
        return stat;
    }

    private RefactoringStatus checkExtractFromConstructor(final IProgressMonitor monitor) {
        SubMonitor.convert(monitor, "Checking for constructor calls", 25);
        RefactoringStatus stat = new RefactoringStatus();
        if (methodCodeFinder.isInConstructor()) {
            if (new ExtractConstructorTest().containsConstructorCall(newMethod)) {
                stat.addFatalError(GroovyRefactoringMessages.ExtractMethodInfo_NoExtractionOfConstructorCallinConstructor, createErrorContext());
            }
        }
        return stat;
    }

    private RefactoringStatus checkStatementSelection(final IProgressMonitor monitor) {
        SubMonitor.convert(monitor, "Checking statement selection", 25);
        RefactoringStatus stat = new RefactoringStatus();
        int selectionLength = selectedText.getLength();
        if (block.isEmpty() && selectionLength >= 0) {
            stat.addFatalError(GroovyRefactoringMessages.ExtractMethodInfo_NoStatementSelected, createErrorContext());
        }
        return stat;
    }

    private RefactoringStatus checkNrOfReturnValues(final IProgressMonitor monitor) {
        SubMonitor.convert(monitor, "Checking number of return values", 25);
        RefactoringStatus stat = new RefactoringStatus();
        if (returnParameters != null && returnParameters.size() > 1) {
            StringBuilder retValues = new StringBuilder();
            for (Variable var : returnParameters) {
                retValues.append(var.getType().getNameWithoutPackage() + " " + var.getName() + "\n");
            }
            String errorMsg = GroovyRefactoringMessages.ExtractMethodInfo_ToMuchReturnValues + retValues.toString();
            stat.addFatalError(errorMsg, createErrorContext());
        }
        return stat;
    }

    /**
     * Returns the Code of the new Method as a formated IDocument.
     */
    private String createCopiedMethodCode(final RefactoringStatus status) {
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

            MethodNode method = createNewMethodForValidation(sb.toString(), status);
            IDocument newMethodDocument = new Document(sb.toString());
            if (method != null && variablesToRename != null) {
                MultiTextEdit edits = renameVariableInExtractedMethod(method);
                edits.apply(newMethodDocument);
            }

            DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(newMethodDocument, formmatterPrefs, indentLevel);
            formatter.format().apply(newMethodDocument);
            return newMethodDocument.get();
        } catch (BadLocationException e) {
            status.addFatalError("Problem when creating the body of the extracted method.\n" + e.getMessage(), createErrorContext());
            GroovyCore.logException("Problem when creating the body of the extracted method.", e);
        }
        return sb.toString();
    }

    /**
     * @return may return null if there is a parse problem
     */
    private MethodNode createNewMethodForValidation(final String methodText, final RefactoringStatus status) {
        try {
            GroovySnippetParser parser = new GroovySnippetParser();
            ModuleNode module = parser.parse(methodText);
            if (module.getMethods() == null || module.getMethods().size() != 1) {
                status.addError("Problem parsing extracted method", createErrorContext());
                if (module.getMethods() == null) {
                    return null;
                }
            }
            MethodNode method = module.getMethods().get(0);
            new VariableScopeVisitor(null).visitClass(method.getDeclaringClass());
            return method;
        } catch (Exception e) {
            // probably bad syntax
            status.addError("Problem parsing extracted method.\n" + e.getMessage(), createErrorContext());
        }
        return null;
    }

    private FileStatusContext createErrorContext() {
        return new FileStatusContext((IFile) unit.getResource(), new org.eclipse.jface.text.Region(selectedText.getOffset(), selectedText.getLength()));
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
                innerClassCount += 1;
                if (current.getEnclosingMethod() != null) {
                    innerClassCount += 1;
                    current = current.getEnclosingMethod().getDeclaringClass();
                    continue;
                }
                current = current.getDeclaringClass();
            }
            defaultIndentation = innerClassCount;
        }
        return defaultIndentation;
    }

    private MultiTextEdit renameVariableInExtractedMethod(final MethodNode method) {
        VariableRenamer renamer = new VariableRenamer();
        return renamer.rename(method, variablesToRename);
    }

    private ASTWriter writeReturnStatements(final IDocument document) {
        ASTWriter writer = new ASTWriter(unit.getModuleNode(), document);
        for (Variable var : returnParameters) {
            ReturnStatement ret = new ReturnStatement(varX(var));
            writer.visitReturnStatement(ret);
            writer.insertLineFeed();
        }
        return writer;
    }

    private InsertEdit createMethodDeclarationEdit(final RefactoringStatus status) {
        return new InsertEdit(methodCodeFinder.getSelectedDeclaration().getEnd(), createCopiedMethodCode(status));
    }

    private ReplaceEdit createMethodCallEdit() {
        return new ReplaceEdit(replaceScope.getOffset(), replaceScope.getLength(), getMethodCall());
    }

    public List<String> getMethodNames() {
        return methodCodeFinder.getMethodNames();
    }

    public String getClassName() {
        return methodCodeFinder.getClassName();
    }

    /**
     * @param variName TODO
     * @param upEvent true if the move is upwards
     * @param numberOfMoves mostly 1, can be more for tests
     * @return the index of the selected variable in the collection
     */
    public int setMoveParameter(final String variName, final boolean upEvent, final int numberOfMoves) {
        Parameter[] originalParams = getCallAndMethHeadParameters();
        List<Variable> newParamList = new ArrayList<>();

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

    private int reorderParameters(final boolean upEvent, final int numberOfMoves, final List<Variable> newParamList, final int index) {
        int indexOfSelectedParam = index;
        // also reorder in originals!
        Variable variToMove = newParamList.remove(indexOfSelectedParam);
        Variable originalToMove = originalParametersBeforeRename.remove(indexOfSelectedParam);

        indexOfSelectedParam = calculateNewIndexAfterMove(upEvent, numberOfMoves, newParamList, indexOfSelectedParam);

        newParamList.add(indexOfSelectedParam, variToMove);
        originalParametersBeforeRename.add(indexOfSelectedParam, originalToMove);
        return indexOfSelectedParam;
    }

    private int calculateNewIndexAfterMove(final boolean upEvent, final int numberOfMoves, final List<Variable> newParamList, final int index) {
        int indexOfSelectedParam = index;
        if (upEvent) {
            if (indexOfSelectedParam < 1) {
                indexOfSelectedParam = 0;
            } else {
                indexOfSelectedParam -= numberOfMoves;
            }
        } else {
            if (indexOfSelectedParam > newParamList.size() - 1) {
                indexOfSelectedParam = newParamList.size() - 1;
            } else {
                indexOfSelectedParam += numberOfMoves;
            }
        }
        return indexOfSelectedParam;
    }

    public void setParameterRename(final Map<String, String> variablesToRename) {
        this.variablesToRename = variablesToRename;
        List<Variable> newParamList = new ArrayList<>();

        for (Variable param : originalParametersBeforeRename) {
            if (variablesToRename.containsKey(param.getName())) {
                // there's an entry for this variable in the map, therefore rename
                newParamList.add(new Parameter(param.getOriginType(), variablesToRename.get(param.getName())));
            } else {
                newParamList.add(param);
            }
        }
        setCallAndMethHeadParameters(newParamList);
        observable.setChanged();
        observable.notifyObservers();
    }

    public String getOriginalParameterName(final int selectionIndex) {
        return originalParametersBeforeRename.get(selectionIndex).getName();
    }

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
}
