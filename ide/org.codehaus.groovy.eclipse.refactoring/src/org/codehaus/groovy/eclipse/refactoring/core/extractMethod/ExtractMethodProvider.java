/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.extractMethod;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.SingleFileRefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rewriter.ASTWriter;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.DocumentHelpers;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.objectweb.asm.Opcodes;

/**
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class ExtractMethodProvider extends SingleFileRefactoringProvider {

	private String newMethodname = "";
	private MethodNode newMethod;
	private BlockStatement block;
	private int newMethodModifier = 0x0;
	private UserSelection replaceScope;
	private final StatementFinder methodCode;
	private boolean returnMustBeDeclared = false;
	
	/**
	 * Two collections since the variables in the methodCall
	 * and in the signature of the method can be different
	 */
	private List<Variable> actualParameters; 
	private Vector<Variable> originalParametersBeforeRename; 
	
	private Vector<Variable> returnParameters;
	private Document codeOfTheNewMethod;
	private Map<String, String> variablesToRename;
	
	protected Preferences pref;

	

	/**
	 * @param docProvider
	 * @param selection
	 */
	public ExtractMethodProvider(IGroovyDocumentProvider docProvider, UserSelection selection, Preferences preferences) {
		super(docProvider, selection);
		pref = preferences;
		methodCode = new StatementFinder(getSelection(), getDocument(), getRootNode());
		codeOfTheNewMethod = new Document();
		updateMethod();
		saveOriginalParameters();
	}

	private void saveOriginalParameters() {
		originalParametersBeforeRename = new Vector<Variable>();
		if(newMethod != null && newMethod.getParameters() != null){
			for(Parameter p : newMethod.getParameters()){
				originalParametersBeforeRename.add(p);
			}
		}
	}

	public void setNewMethodname(String newMethodname) {
		this.newMethodname = newMethodname;
		updateMethod();
	}

	public String getNewMethodname() {
		return newMethodname;
	}

	public void setModifier(int modifier) {
		this.newMethodModifier = modifier;
		updateMethod();
	}

	public int getModifier() {
		return newMethodModifier;
	}

	/**
	 * @return the whole new method
	 */
	public MethodNode getNewMethod() {
		if (newMethod == null)
			updateMethod();
		return newMethod;
	}

	private void setCallAndMethHeadParameters(Vector<Variable> params) {
		actualParameters = params;
		updateMethod();
	}

	public Parameter[] getCallAndMethHeadParameters() {
		Parameter[] params = new Parameter[actualParameters.size()];
		for(Variable v : actualParameters){
			Parameter tmpParam = new Parameter(v.getOriginType(), v.getName());
			params[actualParameters.indexOf(v)] = tmpParam;
		}
		return params;
	}

	/**
	 * MethodCallExpression to call the newly generated method
	 * @return String containing the call
	 */
	public String getMethodCall() {
		
		VariableExpression objExp = new VariableExpression("this");
		ArgumentListExpression arguments = new ArgumentListExpression();

		for(Variable param : originalParametersBeforeRename){
			arguments.addExpression(new VariableExpression(param.getName(), param.getOriginType()));
		}
	
		MethodCallExpression newMethodCall = new MethodCallExpression(objExp, newMethodname, arguments);
	
		ASTWriter writer = new ASTWriter(getRootNode(),replaceScope.getOffset(), getDocument());
	
		if (returnParameters.size() > 0) {
			visitExpressionsForReturnStmt(newMethodCall, writer);
		} else {
			writer.visitMethodCallExpression(newMethodCall);
		}
	
		return writer.getGroovyCode();
	}

	private void visitExpressionsForReturnStmt(MethodCallExpression newMethodCall, ASTWriter astw) {
		
		Variable retVar = returnParameters.firstElement();
		
		if (returnMustBeDeclared) {
			VariableExpression varExp = new VariableExpression(retVar);
			DeclarationExpression declarationExpression = new DeclarationExpression(
					varExp, Token.newSymbol(Types.ASSIGN, -1, -1), newMethodCall);
			astw.visitDeclarationExpression(declarationExpression);
		} else {
			BinaryExpression binaryExpression = new BinaryExpression(
					new VariableExpression(retVar), Token.newSymbol(Types.ASSIGN, -1, -1), newMethodCall);
			astw.visitBinaryExpression(binaryExpression);
		}
	}

	/**
	 * Return a method head for preview
	 * @return
	 */
	public String getMethodHead() {
		updateMethod();
		
		ASTWriter astw = new ASTWriter(getRootNode(), getDocument());
		astw.visitMethod(newMethod);
		String head = astw.getGroovyCode();
		
		int headEndPos = head.indexOf(")"); 
		return head.substring(0, headEndPos + 1).trim();
	}
	
	/*private String getMethodHeadBeforeRename() {
		if (variablesToRename != null) {
			Parameter[] parameters = newMethod.getParameters();
			for (int i = 0; i < parameters.length; i++ ){
				String oldName = parameters[i].getName();
				ClassNode type = parameters[i].getType();
				parameters[i] = new Parameter(type, variablesToRename.get(oldName));
			}
			ASTWriter rewriter = new ASTWriter(getRootNode(),getDocument());
			rewriter.printMethodHead(newMethod);
			return rewriter.getGroovyCode();
		} else {
			return getMethodHead();
		}
	}*/

	public BlockStatement getBlockStatement() {
		return block;
	}

	/**
	 * create the method node with all given parameters
	 */
	private void updateMethod() {
		
		updateBlockStatement();
		
		if (block.getStatements().size() > 0) {
			Parameter[] params = getCallAndMethHeadParameters();
			ClassNode returnType = ClassHelper.DYNAMIC_TYPE;
			if (returnParameters.size() > 0) {
				returnType = returnParameters.firstElement().getOriginType();
			}
			newMethod = new MethodNode(newMethodname, 0, returnType, params, null, block);
			
			checkStaticModifier();
		}
	}

	private void checkStaticModifier() {
		if(methodCode.isStatic()) {
			newMethod.setModifiers(newMethodModifier | Opcodes.ACC_STATIC);
		} else {
			newMethod.setModifiers(newMethodModifier);
		}
	}

	/**
	 * arrange all nodes in the method and find the properties:
	 * - return values
	 * - used variables
	 */
	private void updateBlockStatement() {
		if (block == null) {
			block = new BlockStatement();
			block.addStatements(methodCode.getInSelection());

			replaceScope = ASTTools.getPositionOfBlockStatements(block, getDocument());
			defineReturnParameters();
		}
	}

	private void defineReturnParameters() {
		
		// Read used Variables
		ASTScopeScanner scanner = new ASTScopeScanner(methodCode.selectionIsInLoopOrClosure());
		scanner.visitNode(block);

		ASTScopeScanner postSelectionScanner = new ASTScopeScanner(methodCode.selectionIsInLoopOrClosure());
		BlockStatement postBlock = new BlockStatement();
		postBlock.addStatements(methodCode.getPostSelection());
		postSelectionScanner.visitNode(postBlock);
		List<Variable> postUsedVar = postSelectionScanner.getUsedVariables();
		List<Variable> selReturnVar = scanner.getReturnVariables();
		List<Variable> innerLoopAssigned = scanner.getInnerLoopAssignedVariables();

		actualParameters = scanner.getUsedVariables();
		returnParameters = new Vector<Variable>();

		// Add Variables which are used after selected Block
		checkReturnCandidates(scanner, postUsedVar, selReturnVar, innerLoopAssigned);
	}

	private void checkReturnCandidates(ASTScopeScanner scanner,
			List<Variable> postUsedVar, List<Variable> selReturnVar,
			List<Variable> innerLoopAssigned) {
		
		for (Variable var : selReturnVar) {
			if (postUsedVar.contains(var) || innerLoopAssigned.contains(var)) {
				returnParameters.add(var);
				if (scanner.getDeclaratedVariables().contains(var)) {
					returnMustBeDeclared = true;
				}
			}
		}
	}

	@Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus stat = new RefactoringStatus();
		checkDuplicateMethod(stat);
		return stat;
	}

	private void checkDuplicateMethod(RefactoringStatus stat) {
		if (getMethodNames().contains(newMethodname)) {
			Object[] message = { newMethodname, getClassName() };
			String messageString = MessageFormat.format(
					GroovyRefactoringMessages.ExtractMethodWizard_MethodNameAlreadyExists, message);
			stat.addWarning(messageString);
		}
	}

	@Override
	public void addInitialConditionsCheckStatus(RefactoringStatus status) {
		updateMethod();
		
		checkNrOfReturnValues(status);
		checkStatementSelection(status);
		checkExtractFromConstructor(status);
	}

	private void checkExtractFromConstructor(RefactoringStatus stat) {
		if(methodCode.isInConstructor()) {
			if(new ExtractConstructorTest().containsConstructorCall(getNewMethod())){
				String errorMsg = GroovyRefactoringMessages.ExtractMethodInfo_NoExtractionOfConstructorCallinConstructor;
				stat.addFatalError(errorMsg);
			}
		}
	}

	private void checkStatementSelection(RefactoringStatus stat) {
		String errorMsg = GroovyRefactoringMessages.ExtractMethodInfo_NoStatementSelected;
		int selectionLength = getSelection().getLength();
		if (block.isEmpty() && selectionLength >= 0) {
			stat.addFatalError(errorMsg);
		}
	}

	private void checkNrOfReturnValues(RefactoringStatus stat) {
		if (returnParameters != null && returnParameters.size() > 1) {
			StringBuilder retValues = new StringBuilder();
			for(Variable var : returnParameters) {
				retValues.append(var.getType().getNameWithoutPackage() + " " + var.getName() + "\n");
			}
			String errorMsg = GroovyRefactoringMessages.ExtractMethodInfo_ToMuchReturnValues + retValues.toString();
			stat.addFatalError(errorMsg);
		}
	}
	
	/**
	 * Return the Code of the new Method as a formated IDocument
	 * @return
	 */
	public IDocument getCopiedMethodCode() {
		
		String lineDelimiter = codeOfTheNewMethod.getDefaultLineDelimiter();
		int defaultIndentation = 1;
		if(methodCode.getClassNode().isScript()){
			defaultIndentation = 0;
		}
		
		try {
			block.setSourcePosition(replaceScope.getASTNode(getDocument()));
			
			UserSelection blockWithLeadingGap = ASTTools.includeLeedingGap(block, getDocument());
			
			StringBuilder methodString = new StringBuilder(lineDelimiter);
			
			methodString.append(getMethodHead() + " {" + lineDelimiter);
			
			String copyOfSourceCode = getDocument().get(blockWithLeadingGap.getOffset(), blockWithLeadingGap.getLength());
			methodString.append(copyOfSourceCode);
			methodString.append(lineDelimiter);
			
			ASTWriter astw = writeReturnStatements();
			
			writeMethodEnd(methodString, astw);
			
			Document doc = new Document(methodString.toString());
			DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(doc,pref,defaultIndentation);
			formatter.format().apply(doc);
			codeOfTheNewMethod.set(doc.get());
			
			if(variablesToRename != null){
				renameVariableInExtractedMethod();
			}

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return codeOfTheNewMethod;
	}

	private void renameVariableInExtractedMethod() {
		codeOfTheNewMethod = DocumentHelpers.applyRenameEditsToDocument(newMethodname,variablesToRename, codeOfTheNewMethod);
	}

	private void writeMethodEnd(StringBuilder methodString, ASTWriter astw) {
		if(astw.getGroovyCode().length() > 0){
			methodString.append(astw.getGroovyCode());
		}
		methodString.append("}");
	}

	private ASTWriter writeReturnStatements() {
		ASTWriter astw = new ASTWriter(getRootNode(),getDocument());
		
		for (Variable var : returnParameters) {
			ReturnStatement ret = new ReturnStatement(new VariableExpression(var));
			astw.visitReturnStatement(ret);
			astw.insertLineFeed();
		}
		return astw;
	}

	@Override
    public GroovyChange createGroovyChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
	
		MultiTextEdit edit = new MultiTextEdit();
		if (getNewMethod() != null) {
			edit.addChild(createReplaceEdit());
			edit.addChild(createInsertEdit());
		}
		GroovyChange change = new GroovyChange(GroovyRefactoringMessages.ExtractMethodRefactoring);
		change.addEdit(getDocumentProvider(), edit);
		return change;
	}

	private InsertEdit createInsertEdit() {
		int lastLineNumber = methodCode.getMethodNode().getLastLineNumber();
		int lastColumnNumber = methodCode.getMethodNode().getLastColumnNumber();
		SourceCodePoint insertPosition = new SourceCodePoint(lastLineNumber, lastColumnNumber);
		
		if(insertPosition.getRow() == -1 && insertPosition.getCol() == -1) {
			insertPosition = methodCode.getPositionAfterMethodWithSelection();
		}
		
		int offset = insertPosition.getOffset(getDocument());
		String newMethodCode = getCopiedMethodCode().get();
		return new InsertEdit(offset, newMethodCode);
	}

	private ReplaceEdit createReplaceEdit() {
		int offset = replaceScope.getOffset();
		int length = replaceScope.getLength();
		
		ReplaceEdit insertMethodCall = new ReplaceEdit(	offset, length, getMethodCall());
		return insertMethodCall;
	}

	public List<String> getMethodNames() {
		return methodCode.getMethodNames();
	}

	public String getClassName() {
		return methodCode.getClassName();
	}
	
	/**
	 * @param variName
	 * @param upEvent true if the move is upwards
	 * @param numberOfMoves mostly 1, can be more for tests
	 * @return the index of the selected variable in the collection
	 */
	public int setMoveParameter(String variName, boolean upEvent, int numberOfMoves) {
		
		Parameter[] originalParams = getCallAndMethHeadParameters();
		Vector<Variable> newParamList = new Vector<Variable>();
		
		int indexOfSelectedParam = -1;
		for(Parameter param : originalParams){
			newParamList.add(param);
			if(param.getName().equals(variName)){
				indexOfSelectedParam = newParamList.indexOf(param);
			}
		}
		
		indexOfSelectedParam = reorderParameters(upEvent, numberOfMoves, newParamList, indexOfSelectedParam);

		setCallAndMethHeadParameters(newParamList);
		
		return indexOfSelectedParam;
	}

	private int reorderParameters(boolean upEvent, int numberOfMoves,
			Vector<Variable> newParamList, int index) {
		int indexOfSelectedParam = index;
		//also reorder in originals!
		Variable variToMove = newParamList.remove(indexOfSelectedParam);
		Variable originalToMove = originalParametersBeforeRename.remove(indexOfSelectedParam);
		
		indexOfSelectedParam = calculateNewIndexAfterMove(upEvent, numberOfMoves, newParamList, indexOfSelectedParam);
		
		newParamList.add(indexOfSelectedParam, variToMove);
		originalParametersBeforeRename.add(indexOfSelectedParam, originalToMove);
		return indexOfSelectedParam;
	}

	private int calculateNewIndexAfterMove(boolean upEvent, int numberOfMoves,
			Vector<Variable> newParamList, int index) {
		int indexOfSelectedParam = index;
		if(upEvent){
			if (indexOfSelectedParam < 1) indexOfSelectedParam = 0;
			else indexOfSelectedParam -= numberOfMoves;
		}else{
			if (indexOfSelectedParam > newParamList.size()-1) indexOfSelectedParam = newParamList.size()-1;
			else indexOfSelectedParam += numberOfMoves;
		}
		return indexOfSelectedParam;
	}

	public void setParameterRename(Map<String, String> variablesToRename) {
		
		this.variablesToRename = variablesToRename;
		Vector<Variable> newParamList = new Vector<Variable>();
		
		for(Variable param : originalParametersBeforeRename){
			if(variablesToRename.containsKey(param.getName())){
				//there's an entry for this variable in the map, therefore rename
				newParamList.add(new Parameter(param.getOriginType(),variablesToRename.get(param.getName())));
			}else{
				newParamList.add(param);
			}
		}
		setCallAndMethHeadParameters(newParamList);
	}

	public String getOriginalParameterName(int selectionIndex) {
		return originalParametersBeforeRename.get(selectionIndex).getName();
	}

}
