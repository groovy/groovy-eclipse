// Generated from GroovyParser.g4 by ANTLR 4.7.4
package org.apache.groovy.parser.antlr4;

    import java.util.Map;
    import org.codehaus.groovy.ast.NodeMetaDataHandler;

import groovyjarjarantlr4.v4.runtime.Token;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import groovyjarjarantlr4.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link GroovyParser}.
 *
 * @param <Result> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface GroovyParserVisitor<Result> extends ParseTreeVisitor<Result> {
	/**
	 * Visit a parse tree produced by the {@code identifierPrmrAlt}
	 * labeled alternative in {@link GroovyParser#commandPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitIdentifierPrmrAlt(@NotNull GroovyParser.IdentifierPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code literalPrmrAlt}
	 * labeled alternative in {@link GroovyParser#commandPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLiteralPrmrAlt(@NotNull GroovyParser.LiteralPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code gstringPrmrAlt}
	 * labeled alternative in {@link GroovyParser#commandPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitGstringPrmrAlt(@NotNull GroovyParser.GstringPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code newPrmrAlt}
	 * labeled alternative in {@link GroovyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitNewPrmrAlt(@NotNull GroovyParser.NewPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code thisPrmrAlt}
	 * labeled alternative in {@link GroovyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitThisPrmrAlt(@NotNull GroovyParser.ThisPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code superPrmrAlt}
	 * labeled alternative in {@link GroovyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitSuperPrmrAlt(@NotNull GroovyParser.SuperPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code parenPrmrAlt}
	 * labeled alternative in {@link GroovyParser#namedPropertyArgPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitParenPrmrAlt(@NotNull GroovyParser.ParenPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code closureOrLambdaExpressionPrmrAlt}
	 * labeled alternative in {@link GroovyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClosureOrLambdaExpressionPrmrAlt(@NotNull GroovyParser.ClosureOrLambdaExpressionPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code listPrmrAlt}
	 * labeled alternative in {@link GroovyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitListPrmrAlt(@NotNull GroovyParser.ListPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code mapPrmrAlt}
	 * labeled alternative in {@link GroovyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMapPrmrAlt(@NotNull GroovyParser.MapPrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code builtInTypePrmrAlt}
	 * labeled alternative in {@link GroovyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBuiltInTypePrmrAlt(@NotNull GroovyParser.BuiltInTypePrmrAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code integerLiteralAlt}
	 * labeled alternative in {@link GroovyParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitIntegerLiteralAlt(@NotNull GroovyParser.IntegerLiteralAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code floatingPointLiteralAlt}
	 * labeled alternative in {@link GroovyParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitFloatingPointLiteralAlt(@NotNull GroovyParser.FloatingPointLiteralAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code stringLiteralAlt}
	 * labeled alternative in {@link GroovyParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitStringLiteralAlt(@NotNull GroovyParser.StringLiteralAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code booleanLiteralAlt}
	 * labeled alternative in {@link GroovyParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBooleanLiteralAlt(@NotNull GroovyParser.BooleanLiteralAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code nullLiteralAlt}
	 * labeled alternative in {@link GroovyParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitNullLiteralAlt(@NotNull GroovyParser.NullLiteralAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code castExprAlt}
	 * labeled alternative in {@link GroovyParser#castOperandExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCastExprAlt(@NotNull GroovyParser.CastExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code postfixExprAlt}
	 * labeled alternative in {@link GroovyParser#castOperandExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitPostfixExprAlt(@NotNull GroovyParser.PostfixExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code unaryNotExprAlt}
	 * labeled alternative in {@link GroovyParser#castOperandExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitUnaryNotExprAlt(@NotNull GroovyParser.UnaryNotExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code powerExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitPowerExprAlt(@NotNull GroovyParser.PowerExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code unaryAddExprAlt}
	 * labeled alternative in {@link GroovyParser#castOperandExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitUnaryAddExprAlt(@NotNull GroovyParser.UnaryAddExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code multiplicativeExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMultiplicativeExprAlt(@NotNull GroovyParser.MultiplicativeExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code additiveExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAdditiveExprAlt(@NotNull GroovyParser.AdditiveExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code shiftExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitShiftExprAlt(@NotNull GroovyParser.ShiftExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code relationalExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitRelationalExprAlt(@NotNull GroovyParser.RelationalExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code equalityExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEqualityExprAlt(@NotNull GroovyParser.EqualityExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code regexExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitRegexExprAlt(@NotNull GroovyParser.RegexExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code andExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAndExprAlt(@NotNull GroovyParser.AndExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code exclusiveOrExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitExclusiveOrExprAlt(@NotNull GroovyParser.ExclusiveOrExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code inclusiveOrExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitInclusiveOrExprAlt(@NotNull GroovyParser.InclusiveOrExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code logicalAndExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLogicalAndExprAlt(@NotNull GroovyParser.LogicalAndExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code logicalOrExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLogicalOrExprAlt(@NotNull GroovyParser.LogicalOrExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code conditionalExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitConditionalExprAlt(@NotNull GroovyParser.ConditionalExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code multipleAssignmentExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMultipleAssignmentExprAlt(@NotNull GroovyParser.MultipleAssignmentExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code assignmentExprAlt}
	 * labeled alternative in {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAssignmentExprAlt(@NotNull GroovyParser.AssignmentExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code blockStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBlockStmtAlt(@NotNull GroovyParser.BlockStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code conditionalStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitConditionalStmtAlt(@NotNull GroovyParser.ConditionalStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code loopStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLoopStmtAlt(@NotNull GroovyParser.LoopStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code tryCatchStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTryCatchStmtAlt(@NotNull GroovyParser.TryCatchStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code synchronizedStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitSynchronizedStmtAlt(@NotNull GroovyParser.SynchronizedStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code returnStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitReturnStmtAlt(@NotNull GroovyParser.ReturnStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code throwStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitThrowStmtAlt(@NotNull GroovyParser.ThrowStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code breakStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBreakStmtAlt(@NotNull GroovyParser.BreakStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code continueStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitContinueStmtAlt(@NotNull GroovyParser.ContinueStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code labeledStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLabeledStmtAlt(@NotNull GroovyParser.LabeledStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code assertStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAssertStmtAlt(@NotNull GroovyParser.AssertStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code localVariableDeclarationStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLocalVariableDeclarationStmtAlt(@NotNull GroovyParser.LocalVariableDeclarationStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code expressionStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitExpressionStmtAlt(@NotNull GroovyParser.ExpressionStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code emptyStmtAlt}
	 * labeled alternative in {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEmptyStmtAlt(@NotNull GroovyParser.EmptyStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code commandExprAlt}
	 * labeled alternative in {@link GroovyParser#statementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCommandExprAlt(@NotNull GroovyParser.CommandExprAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code forStmtAlt}
	 * labeled alternative in {@link GroovyParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitForStmtAlt(@NotNull GroovyParser.ForStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code whileStmtAlt}
	 * labeled alternative in {@link GroovyParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitWhileStmtAlt(@NotNull GroovyParser.WhileStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by the {@code doWhileStmtAlt}
	 * labeled alternative in {@link GroovyParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitDoWhileStmtAlt(@NotNull GroovyParser.DoWhileStmtAltContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCompilationUnit(@NotNull GroovyParser.CompilationUnitContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#scriptStatements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitScriptStatements(@NotNull GroovyParser.ScriptStatementsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#scriptStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitScriptStatement(@NotNull GroovyParser.ScriptStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#packageDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitPackageDeclaration(@NotNull GroovyParser.PackageDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitImportDeclaration(@NotNull GroovyParser.ImportDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeDeclaration(@NotNull GroovyParser.TypeDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#modifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitModifier(@NotNull GroovyParser.ModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#modifiersOpt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitModifiersOpt(@NotNull GroovyParser.ModifiersOptContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#modifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitModifiers(@NotNull GroovyParser.ModifiersContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#classOrInterfaceModifiersOpt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassOrInterfaceModifiersOpt(@NotNull GroovyParser.ClassOrInterfaceModifiersOptContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#classOrInterfaceModifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassOrInterfaceModifiers(@NotNull GroovyParser.ClassOrInterfaceModifiersContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#classOrInterfaceModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassOrInterfaceModifier(@NotNull GroovyParser.ClassOrInterfaceModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableModifier(@NotNull GroovyParser.VariableModifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableModifiersOpt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableModifiersOpt(@NotNull GroovyParser.VariableModifiersOptContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableModifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableModifiers(@NotNull GroovyParser.VariableModifiersContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeParameters(@NotNull GroovyParser.TypeParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeParameter(@NotNull GroovyParser.TypeParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeBound(@NotNull GroovyParser.TypeBoundContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeList(@NotNull GroovyParser.TypeListContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassDeclaration(@NotNull GroovyParser.ClassDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#classBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassBody(@NotNull GroovyParser.ClassBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#enumConstants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEnumConstants(@NotNull GroovyParser.EnumConstantsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#enumConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEnumConstant(@NotNull GroovyParser.EnumConstantContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#classBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassBodyDeclaration(@NotNull GroovyParser.ClassBodyDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#memberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMemberDeclaration(@NotNull GroovyParser.MemberDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMethodDeclaration(@NotNull GroovyParser.MethodDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#methodName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMethodName(@NotNull GroovyParser.MethodNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#returnType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitReturnType(@NotNull GroovyParser.ReturnTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitFieldDeclaration(@NotNull GroovyParser.FieldDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableDeclarators}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableDeclarators(@NotNull GroovyParser.VariableDeclaratorsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableDeclarator(@NotNull GroovyParser.VariableDeclaratorContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableDeclaratorId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableDeclaratorId(@NotNull GroovyParser.VariableDeclaratorIdContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableInitializer(@NotNull GroovyParser.VariableInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableInitializers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableInitializers(@NotNull GroovyParser.VariableInitializersContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#emptyDims}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEmptyDims(@NotNull GroovyParser.EmptyDimsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#emptyDimsOpt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEmptyDimsOpt(@NotNull GroovyParser.EmptyDimsOptContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitType(@NotNull GroovyParser.TypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#classOrInterfaceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassOrInterfaceType(@NotNull GroovyParser.ClassOrInterfaceTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitPrimitiveType(@NotNull GroovyParser.PrimitiveTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeArguments(@NotNull GroovyParser.TypeArgumentsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeArgument(@NotNull GroovyParser.TypeArgumentContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#annotatedQualifiedClassName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAnnotatedQualifiedClassName(@NotNull GroovyParser.AnnotatedQualifiedClassNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#qualifiedClassNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitQualifiedClassNameList(@NotNull GroovyParser.QualifiedClassNameListContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitFormalParameters(@NotNull GroovyParser.FormalParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#formalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitFormalParameterList(@NotNull GroovyParser.FormalParameterListContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#thisFormalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitThisFormalParameter(@NotNull GroovyParser.ThisFormalParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitFormalParameter(@NotNull GroovyParser.FormalParameterContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#methodBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMethodBody(@NotNull GroovyParser.MethodBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitQualifiedName(@NotNull GroovyParser.QualifiedNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#qualifiedNameElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitQualifiedNameElement(@NotNull GroovyParser.QualifiedNameElementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#qualifiedNameElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitQualifiedNameElements(@NotNull GroovyParser.QualifiedNameElementsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#qualifiedClassName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitQualifiedClassName(@NotNull GroovyParser.QualifiedClassNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#qualifiedStandardClassName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitQualifiedStandardClassName(@NotNull GroovyParser.QualifiedStandardClassNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLiteral(@NotNull GroovyParser.LiteralContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#gstring}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitGstring(@NotNull GroovyParser.GstringContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#gstringValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitGstringValue(@NotNull GroovyParser.GstringValueContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#gstringPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitGstringPath(@NotNull GroovyParser.GstringPathContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#standardLambdaExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitStandardLambdaExpression(@NotNull GroovyParser.StandardLambdaExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#standardLambdaParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitStandardLambdaParameters(@NotNull GroovyParser.StandardLambdaParametersContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#lambdaBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLambdaBody(@NotNull GroovyParser.LambdaBodyContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#closure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClosure(@NotNull GroovyParser.ClosureContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#closureOrLambdaExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClosureOrLambdaExpression(@NotNull GroovyParser.ClosureOrLambdaExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#blockStatementsOpt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBlockStatementsOpt(@NotNull GroovyParser.BlockStatementsOptContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#blockStatements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBlockStatements(@NotNull GroovyParser.BlockStatementsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#annotationsOpt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAnnotationsOpt(@NotNull GroovyParser.AnnotationsOptContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAnnotation(@NotNull GroovyParser.AnnotationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#elementValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitElementValues(@NotNull GroovyParser.ElementValuesContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#annotationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAnnotationName(@NotNull GroovyParser.AnnotationNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#elementValuePairs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitElementValuePairs(@NotNull GroovyParser.ElementValuePairsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#elementValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitElementValuePair(@NotNull GroovyParser.ElementValuePairContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#elementValuePairName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitElementValuePairName(@NotNull GroovyParser.ElementValuePairNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#elementValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitElementValue(@NotNull GroovyParser.ElementValueContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitElementValueArrayInitializer(@NotNull GroovyParser.ElementValueArrayInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBlock(@NotNull GroovyParser.BlockContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#blockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBlockStatement(@NotNull GroovyParser.BlockStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#localVariableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLocalVariableDeclaration(@NotNull GroovyParser.LocalVariableDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableDeclaration(@NotNull GroovyParser.VariableDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeNamePairs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeNamePairs(@NotNull GroovyParser.TypeNamePairsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeNamePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeNamePair(@NotNull GroovyParser.TypeNamePairContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#variableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitVariableNames(@NotNull GroovyParser.VariableNamesContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#conditionalStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitConditionalStatement(@NotNull GroovyParser.ConditionalStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#ifElseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitIfElseStatement(@NotNull GroovyParser.IfElseStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#switchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitSwitchStatement(@NotNull GroovyParser.SwitchStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#loopStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitLoopStatement(@NotNull GroovyParser.LoopStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#continueStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitContinueStatement(@NotNull GroovyParser.ContinueStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#breakStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBreakStatement(@NotNull GroovyParser.BreakStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTryCatchStatement(@NotNull GroovyParser.TryCatchStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#assertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAssertStatement(@NotNull GroovyParser.AssertStatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitStatement(@NotNull GroovyParser.StatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#catchClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCatchClause(@NotNull GroovyParser.CatchClauseContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#catchType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCatchType(@NotNull GroovyParser.CatchTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#finallyBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitFinallyBlock(@NotNull GroovyParser.FinallyBlockContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#resources}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitResources(@NotNull GroovyParser.ResourcesContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#resourceList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitResourceList(@NotNull GroovyParser.ResourceListContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitResource(@NotNull GroovyParser.ResourceContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#switchBlockStatementGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitSwitchBlockStatementGroup(@NotNull GroovyParser.SwitchBlockStatementGroupContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#switchLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitSwitchLabel(@NotNull GroovyParser.SwitchLabelContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#forControl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitForControl(@NotNull GroovyParser.ForControlContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#enhancedForControl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEnhancedForControl(@NotNull GroovyParser.EnhancedForControlContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#classicalForControl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassicalForControl(@NotNull GroovyParser.ClassicalForControlContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#forInit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitForInit(@NotNull GroovyParser.ForInitContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#forUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitForUpdate(@NotNull GroovyParser.ForUpdateContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#castParExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCastParExpression(@NotNull GroovyParser.CastParExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#parExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitParExpression(@NotNull GroovyParser.ParExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#expressionInPar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitExpressionInPar(@NotNull GroovyParser.ExpressionInParContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitExpressionList(@NotNull GroovyParser.ExpressionListContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#expressionListElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitExpressionListElement(@NotNull GroovyParser.ExpressionListElementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#enhancedStatementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEnhancedStatementExpression(@NotNull GroovyParser.EnhancedStatementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#statementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitStatementExpression(@NotNull GroovyParser.StatementExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitPostfixExpression(@NotNull GroovyParser.PostfixExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitExpression(@NotNull GroovyParser.ExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#commandExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCommandExpression(@NotNull GroovyParser.CommandExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#commandArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCommandArgument(@NotNull GroovyParser.CommandArgumentContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#pathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitPathExpression(@NotNull GroovyParser.PathExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#pathElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitPathElement(@NotNull GroovyParser.PathElementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#namePart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitNamePart(@NotNull GroovyParser.NamePartContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#dynamicMemberName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitDynamicMemberName(@NotNull GroovyParser.DynamicMemberNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#indexPropertyArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitIndexPropertyArgs(@NotNull GroovyParser.IndexPropertyArgsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#namedPropertyArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitNamedPropertyArgs(@NotNull GroovyParser.NamedPropertyArgsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitPrimary(@NotNull GroovyParser.PrimaryContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitList(@NotNull GroovyParser.ListContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#map}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMap(@NotNull GroovyParser.MapContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#mapEntryList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMapEntryList(@NotNull GroovyParser.MapEntryListContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#mapEntry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMapEntry(@NotNull GroovyParser.MapEntryContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#mapEntryLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitMapEntryLabel(@NotNull GroovyParser.MapEntryLabelContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#creator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCreator(@NotNull GroovyParser.CreatorContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#dim}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitDim(@NotNull GroovyParser.DimContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#arrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitArrayInitializer(@NotNull GroovyParser.ArrayInitializerContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#anonymousInnerClassDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitAnonymousInnerClassDeclaration(@NotNull GroovyParser.AnonymousInnerClassDeclarationContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#createdName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitCreatedName(@NotNull GroovyParser.CreatedNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#nonWildcardTypeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitNonWildcardTypeArguments(@NotNull GroovyParser.NonWildcardTypeArgumentsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#typeArgumentsOrDiamond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitTypeArgumentsOrDiamond(@NotNull GroovyParser.TypeArgumentsOrDiamondContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitArguments(@NotNull GroovyParser.ArgumentsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#enhancedArgumentListInPar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEnhancedArgumentListInPar(@NotNull GroovyParser.EnhancedArgumentListInParContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#enhancedArgumentListElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitEnhancedArgumentListElement(@NotNull GroovyParser.EnhancedArgumentListElementContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#stringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitStringLiteral(@NotNull GroovyParser.StringLiteralContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#className}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitClassName(@NotNull GroovyParser.ClassNameContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitIdentifier(@NotNull GroovyParser.IdentifierContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#builtInType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitBuiltInType(@NotNull GroovyParser.BuiltInTypeContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#keywords}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitKeywords(@NotNull GroovyParser.KeywordsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#rparen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitRparen(@NotNull GroovyParser.RparenContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#nls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitNls(@NotNull GroovyParser.NlsContext ctx);

	/**
	 * Visit a parse tree produced by {@link GroovyParser#sep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	Result visitSep(@NotNull GroovyParser.SepContext ctx);
}