// Generated from GroovyParser.g4 by ANTLR 4.13.2.7
package org.apache.groovy.parser.antlr4;

    import java.util.Map;
    import org.codehaus.groovy.ast.NodeMetaDataHandler;

import groovyjarjarantlr4.v4.runtime.atn.*;
import groovyjarjarantlr4.v4.runtime.dfa.DFA;
import groovyjarjarantlr4.v4.runtime.*;
import groovyjarjarantlr4.v4.runtime.misc.*;
import groovyjarjarantlr4.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

public class GroovyParser extends AbstractParser {
	public static final int
		StringLiteral=1, GStringBegin=2, GStringEnd=3, GStringPart=4, GStringPathPart=5, 
		RollBackOne=6, AS=7, DEF=8, IN=9, TRAIT=10, THREADSAFE=11, ASYNC=12, AWAIT=13, 
		DEFER=14, BuiltInPrimitiveType=15, ABSTRACT=16, ASSERT=17, BREAK=18, CASE=19, 
		CATCH=20, CLASS=21, CONST=22, CONTINUE=23, DEFAULT=24, DO=25, ELSE=26, 
		ENUM=27, EXTENDS=28, FINAL=29, FINALLY=30, FOR=31, IF=32, GOTO=33, IMPLEMENTS=34, 
		IMPORT=35, INSTANCEOF=36, INTERFACE=37, MODULE=38, NATIVE=39, NEW=40, 
		NON_SEALED=41, PACKAGE=42, PERMITS=43, PRIVATE=44, PROTECTED=45, PUBLIC=46, 
		RECORD=47, RETURN=48, SEALED=49, STATIC=50, STRICTFP=51, SUPER=52, SWITCH=53, 
		SYNCHRONIZED=54, THIS=55, THROW=56, THROWS=57, TRANSIENT=58, TRY=59, VAL=60, 
		VAR=61, VOID=62, VOLATILE=63, WHILE=64, YIELD=65, IntegerLiteral=66, FloatingPointLiteral=67, 
		BooleanLiteral=68, NullLiteral=69, RANGE_INCLUSIVE=70, RANGE_EXCLUSIVE_LEFT=71, 
		RANGE_EXCLUSIVE_RIGHT=72, RANGE_EXCLUSIVE_FULL=73, SPREAD_DOT=74, SAFE_DOT=75, 
		SAFE_INDEX=76, SAFE_CHAIN_DOT=77, ELVIS=78, METHOD_POINTER=79, METHOD_REFERENCE=80, 
		REGEX_FIND=81, REGEX_MATCH=82, POWER=83, POWER_ASSIGN=84, SPACESHIP=85, 
		IDENTICAL=86, IMPLIES=87, NOT_IDENTICAL=88, ARROW=89, NOT_INSTANCEOF=90, 
		NOT_IN=91, LPAREN=92, RPAREN=93, LBRACE=94, RBRACE=95, LBRACK=96, RBRACK=97, 
		SEMI=98, COMMA=99, DOT=100, ASSIGN=101, GT=102, LT=103, NOT=104, BITNOT=105, 
		QUESTION=106, COLON=107, EQUAL=108, LE=109, GE=110, NOTEQUAL=111, AND=112, 
		OR=113, INC=114, DEC=115, ADD=116, SUB=117, MUL=118, DIV=119, BITAND=120, 
		BITOR=121, XOR=122, MOD=123, ADD_ASSIGN=124, SUB_ASSIGN=125, MUL_ASSIGN=126, 
		DIV_ASSIGN=127, AND_ASSIGN=128, OR_ASSIGN=129, XOR_ASSIGN=130, MOD_ASSIGN=131, 
		LSHIFT_ASSIGN=132, RSHIFT_ASSIGN=133, URSHIFT_ASSIGN=134, ELVIS_ASSIGN=135, 
		CapitalizedIdentifier=136, Identifier=137, AT=138, ELLIPSIS=139, WS=140, 
		NL=141, SH_COMMENT=142, UNEXPECTED_CHAR=143;
	public static final int
		RULE_compilationUnit = 0, RULE_scriptStatements = 1, RULE_scriptStatement = 2, 
		RULE_packageDeclaration = 3, RULE_importDeclaration = 4, RULE_typeDeclaration = 5, 
		RULE_modifier = 6, RULE_modifiersOpt = 7, RULE_modifiers = 8, RULE_classOrInterfaceModifiersOpt = 9, 
		RULE_classOrInterfaceModifiers = 10, RULE_classOrInterfaceModifier = 11, 
		RULE_variableModifier = 12, RULE_variableModifiersOpt = 13, RULE_variableModifiers = 14, 
		RULE_typeParameters = 15, RULE_typeParameter = 16, RULE_typeBound = 17, 
		RULE_typeList = 18, RULE_classDeclaration = 19, RULE_classBody = 20, RULE_enumConstants = 21, 
		RULE_enumConstant = 22, RULE_classBodyDeclaration = 23, RULE_memberDeclaration = 24, 
		RULE_methodDeclaration = 25, RULE_compactConstructorDeclaration = 26, 
		RULE_methodName = 27, RULE_returnType = 28, RULE_fieldDeclaration = 29, 
		RULE_variableDeclarators = 30, RULE_variableDeclarator = 31, RULE_variableDeclaratorId = 32, 
		RULE_variableInitializer = 33, RULE_type = 34, RULE_primitiveType = 35, 
		RULE_referenceType = 36, RULE_matchingType = 37, RULE_standardType = 38, 
		RULE_standardClassOrInterfaceType = 39, RULE_typeArguments = 40, RULE_typeArgument = 41, 
		RULE_annotatedQualifiedClassName = 42, RULE_qualifiedClassNameList = 43, 
		RULE_formalParameters = 44, RULE_formalParameterList = 45, RULE_thisFormalParameter = 46, 
		RULE_formalParameter = 47, RULE_methodBody = 48, RULE_qualifiedName = 49, 
		RULE_qualifiedNameElement = 50, RULE_qualifiedNameElements = 51, RULE_qualifiedClassName = 52, 
		RULE_qualifiedStandardClassName = 53, RULE_literal = 54, RULE_gstring = 55, 
		RULE_gstringValue = 56, RULE_gstringPath = 57, RULE_lambdaExpression = 58, 
		RULE_standardLambdaExpression = 59, RULE_lambdaParameters = 60, RULE_standardLambdaParameters = 61, 
		RULE_lambdaBody = 62, RULE_closure = 63, RULE_closureOrLambdaExpression = 64, 
		RULE_blockStatementsOpt = 65, RULE_blockStatements = 66, RULE_annotationsOpt = 67, 
		RULE_annotation = 68, RULE_elementValues = 69, RULE_annotationName = 70, 
		RULE_elementValuePairs = 71, RULE_elementValuePair = 72, RULE_elementValuePairName = 73, 
		RULE_elementValue = 74, RULE_elementValueArrayInitializer = 75, RULE_block = 76, 
		RULE_blockStatement = 77, RULE_localVariableDeclaration = 78, RULE_variableDeclaration = 79, 
		RULE_typeNamePairs = 80, RULE_typeNamePair = 81, RULE_keyedPair = 82, 
		RULE_variableNames = 83, RULE_conditionalStatement = 84, RULE_ifElseStatement = 85, 
		RULE_switchStatement = 86, RULE_loopStatement = 87, RULE_continueStatement = 88, 
		RULE_breakStatement = 89, RULE_yieldStatement = 90, RULE_tryCatchStatement = 91, 
		RULE_assertStatement = 92, RULE_statement = 93, RULE_catchClause = 94, 
		RULE_catchType = 95, RULE_finallyBlock = 96, RULE_resources = 97, RULE_resourceList = 98, 
		RULE_resource = 99, RULE_switchBlockStatementGroup = 100, RULE_switchLabel = 101, 
		RULE_forControl = 102, RULE_enhancedForControl = 103, RULE_indexVariable = 104, 
		RULE_originalForControl = 105, RULE_forInit = 106, RULE_forUpdate = 107, 
		RULE_castParExpression = 108, RULE_intersectionType = 109, RULE_coercionType = 110, 
		RULE_parExpression = 111, RULE_expressionInPar = 112, RULE_expressionList = 113, 
		RULE_expressionListElement = 114, RULE_enhancedExpression = 115, RULE_enhancedStatementExpression = 116, 
		RULE_statementExpression = 117, RULE_postfixExpression = 118, RULE_switchExpression = 119, 
		RULE_switchBlockStatementExpressionGroup = 120, RULE_switchExpressionLabel = 121, 
		RULE_expression = 122, RULE_castOperandExpression = 123, RULE_commandExpression = 124, 
		RULE_commandArgument = 125, RULE_pathExpression = 126, RULE_pathElement = 127, 
		RULE_namePart = 128, RULE_dynamicMemberName = 129, RULE_indexPropertyArgs = 130, 
		RULE_namedPropertyArgs = 131, RULE_primary = 132, RULE_namedPropertyArgPrimary = 133, 
		RULE_namedArgPrimary = 134, RULE_commandPrimary = 135, RULE_list = 136, 
		RULE_map = 137, RULE_mapEntryList = 138, RULE_namedPropertyArgList = 139, 
		RULE_mapEntry = 140, RULE_namedPropertyArg = 141, RULE_namedArg = 142, 
		RULE_mapEntryLabel = 143, RULE_namedPropertyArgLabel = 144, RULE_namedArgLabel = 145, 
		RULE_creator = 146, RULE_dim0 = 147, RULE_dim1 = 148, RULE_arrayInitializer = 149, 
		RULE_anonymousInnerClassDeclaration = 150, RULE_createdName = 151, RULE_nonWildcardTypeArguments = 152, 
		RULE_typeArgumentsOrDiamond = 153, RULE_arguments = 154, RULE_argumentList = 155, 
		RULE_enhancedArgumentListInPar = 156, RULE_firstArgumentListElement = 157, 
		RULE_argumentListElement = 158, RULE_enhancedArgumentListElement = 159, 
		RULE_stringLiteral = 160, RULE_className = 161, RULE_identifier = 162, 
		RULE_builtInType = 163, RULE_keywords = 164, RULE_nls = 165, RULE_sep = 166;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "scriptStatements", "scriptStatement", "packageDeclaration", 
			"importDeclaration", "typeDeclaration", "modifier", "modifiersOpt", "modifiers", 
			"classOrInterfaceModifiersOpt", "classOrInterfaceModifiers", "classOrInterfaceModifier", 
			"variableModifier", "variableModifiersOpt", "variableModifiers", "typeParameters", 
			"typeParameter", "typeBound", "typeList", "classDeclaration", "classBody", 
			"enumConstants", "enumConstant", "classBodyDeclaration", "memberDeclaration", 
			"methodDeclaration", "compactConstructorDeclaration", "methodName", "returnType", 
			"fieldDeclaration", "variableDeclarators", "variableDeclarator", "variableDeclaratorId", 
			"variableInitializer", "type", "primitiveType", "referenceType", "matchingType", 
			"standardType", "standardClassOrInterfaceType", "typeArguments", "typeArgument", 
			"annotatedQualifiedClassName", "qualifiedClassNameList", "formalParameters", 
			"formalParameterList", "thisFormalParameter", "formalParameter", "methodBody", 
			"qualifiedName", "qualifiedNameElement", "qualifiedNameElements", "qualifiedClassName", 
			"qualifiedStandardClassName", "literal", "gstring", "gstringValue", "gstringPath", 
			"lambdaExpression", "standardLambdaExpression", "lambdaParameters", "standardLambdaParameters", 
			"lambdaBody", "closure", "closureOrLambdaExpression", "blockStatementsOpt", 
			"blockStatements", "annotationsOpt", "annotation", "elementValues", "annotationName", 
			"elementValuePairs", "elementValuePair", "elementValuePairName", "elementValue", 
			"elementValueArrayInitializer", "block", "blockStatement", "localVariableDeclaration", 
			"variableDeclaration", "typeNamePairs", "typeNamePair", "keyedPair", 
			"variableNames", "conditionalStatement", "ifElseStatement", "switchStatement", 
			"loopStatement", "continueStatement", "breakStatement", "yieldStatement", 
			"tryCatchStatement", "assertStatement", "statement", "catchClause", "catchType", 
			"finallyBlock", "resources", "resourceList", "resource", "switchBlockStatementGroup", 
			"switchLabel", "forControl", "enhancedForControl", "indexVariable", "originalForControl", 
			"forInit", "forUpdate", "castParExpression", "intersectionType", "coercionType", 
			"parExpression", "expressionInPar", "expressionList", "expressionListElement", 
			"enhancedExpression", "enhancedStatementExpression", "statementExpression", 
			"postfixExpression", "switchExpression", "switchBlockStatementExpressionGroup", 
			"switchExpressionLabel", "expression", "castOperandExpression", "commandExpression", 
			"commandArgument", "pathExpression", "pathElement", "namePart", "dynamicMemberName", 
			"indexPropertyArgs", "namedPropertyArgs", "primary", "namedPropertyArgPrimary", 
			"namedArgPrimary", "commandPrimary", "list", "map", "mapEntryList", "namedPropertyArgList", 
			"mapEntry", "namedPropertyArg", "namedArg", "mapEntryLabel", "namedPropertyArgLabel", 
			"namedArgLabel", "creator", "dim0", "dim1", "arrayInitializer", "anonymousInnerClassDeclaration", 
			"createdName", "nonWildcardTypeArguments", "typeArgumentsOrDiamond", 
			"arguments", "argumentList", "enhancedArgumentListInPar", "firstArgumentListElement", 
			"argumentListElement", "enhancedArgumentListElement", "stringLiteral", 
			"className", "identifier", "builtInType", "keywords", "nls", "sep"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, "'as'", "'def'", "'in'", "'trait'", 
			"'threadsafe'", "'async'", "'await'", "'defer'", null, "'abstract'", 
			"'assert'", "'break'", "'case'", "'catch'", "'class'", "'const'", "'continue'", 
			"'default'", "'do'", "'else'", "'enum'", "'extends'", "'final'", "'finally'", 
			"'for'", "'if'", "'goto'", "'implements'", "'import'", "'instanceof'", 
			"'interface'", "'module'", "'native'", "'new'", "'non-sealed'", "'package'", 
			"'permits'", "'private'", "'protected'", "'public'", "'record'", "'return'", 
			"'sealed'", "'static'", "'strictfp'", "'super'", "'switch'", "'synchronized'", 
			"'this'", "'throw'", "'throws'", "'transient'", "'try'", "'val'", "'var'", 
			"'void'", "'volatile'", "'while'", "'yield'", null, null, null, "'null'", 
			"'..'", "'<..'", "'..<'", "'<..<'", "'*.'", "'?.'", null, "'??.'", "'?:'", 
			"'.&'", "'::'", "'=~'", "'==~'", "'**'", "'**='", "'<=>'", "'==='", "'==>'", 
			"'!=='", "'->'", "'!instanceof'", "'!in'", null, null, null, null, null, 
			null, "';'", "','", null, "'='", "'>'", "'<'", "'!'", "'~'", "'?'", "':'", 
			"'=='", "'<='", "'>='", "'!='", "'&&'", "'||'", "'++'", "'--'", "'+'", 
			"'-'", "'*'", null, "'&'", "'|'", "'^'", "'%'", "'+='", "'-='", "'*='", 
			"'/='", "'&='", "'|='", "'^='", "'%='", "'<<='", "'>>='", "'>>>='", "'?='", 
			null, null, "'@'", "'...'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "StringLiteral", "GStringBegin", "GStringEnd", "GStringPart", "GStringPathPart", 
			"RollBackOne", "AS", "DEF", "IN", "TRAIT", "THREADSAFE", "ASYNC", "AWAIT", 
			"DEFER", "BuiltInPrimitiveType", "ABSTRACT", "ASSERT", "BREAK", "CASE", 
			"CATCH", "CLASS", "CONST", "CONTINUE", "DEFAULT", "DO", "ELSE", "ENUM", 
			"EXTENDS", "FINAL", "FINALLY", "FOR", "IF", "GOTO", "IMPLEMENTS", "IMPORT", 
			"INSTANCEOF", "INTERFACE", "MODULE", "NATIVE", "NEW", "NON_SEALED", "PACKAGE", 
			"PERMITS", "PRIVATE", "PROTECTED", "PUBLIC", "RECORD", "RETURN", "SEALED", 
			"STATIC", "STRICTFP", "SUPER", "SWITCH", "SYNCHRONIZED", "THIS", "THROW", 
			"THROWS", "TRANSIENT", "TRY", "VAL", "VAR", "VOID", "VOLATILE", "WHILE", 
			"YIELD", "IntegerLiteral", "FloatingPointLiteral", "BooleanLiteral", 
			"NullLiteral", "RANGE_INCLUSIVE", "RANGE_EXCLUSIVE_LEFT", "RANGE_EXCLUSIVE_RIGHT", 
			"RANGE_EXCLUSIVE_FULL", "SPREAD_DOT", "SAFE_DOT", "SAFE_INDEX", "SAFE_CHAIN_DOT", 
			"ELVIS", "METHOD_POINTER", "METHOD_REFERENCE", "REGEX_FIND", "REGEX_MATCH", 
			"POWER", "POWER_ASSIGN", "SPACESHIP", "IDENTICAL", "IMPLIES", "NOT_IDENTICAL", 
			"ARROW", "NOT_INSTANCEOF", "NOT_IN", "LPAREN", "RPAREN", "LBRACE", "RBRACE", 
			"LBRACK", "RBRACK", "SEMI", "COMMA", "DOT", "ASSIGN", "GT", "LT", "NOT", 
			"BITNOT", "QUESTION", "COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", 
			"OR", "INC", "DEC", "ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "XOR", 
			"MOD", "ADD_ASSIGN", "SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", 
			"OR_ASSIGN", "XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", 
			"URSHIFT_ASSIGN", "ELVIS_ASSIGN", "CapitalizedIdentifier", "Identifier", 
			"AT", "ELLIPSIS", "WS", "NL", "SH_COMMENT", "UNEXPECTED_CHAR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override
	@NotNull
	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "GroovyParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@NotNull
	private FailedPredicateException createFailedPredicateException() {
		return createFailedPredicateException(null);
	}

	@NotNull
	private FailedPredicateException createFailedPredicateException(@Nullable String predicate) {
		return createFailedPredicateException(predicate, null);
	}

	@NotNull
	protected FailedPredicateException createFailedPredicateException(@Nullable String predicate, @Nullable String message) {
		return new FailedPredicateException(this, predicate, message);
	}


	    private int inSwitchExpressionLevel = 0;

	    public static class GroovyParserRuleContext extends ParserRuleContext implements NodeMetaDataHandler {
	        private Map metaDataMap = null;

	        public GroovyParserRuleContext() {}

	        public GroovyParserRuleContext(ParserRuleContext parent, int invokingStateNumber) {
	            super(parent, invokingStateNumber);
	        }

	        @Override
	        public Map<?, ?> getMetaDataMap() {
	            return this.metaDataMap;
	        }

	        @Override
	        public void setMetaDataMap(Map<?, ?> metaDataMap) {
	            this.metaDataMap = metaDataMap;
	        }
	    }

	    @Override
	    public int getSyntaxErrorSource() {
	        return GroovySyntaxError.PARSER;
	    }

	    @Override
	    public int getErrorLine() {
	        Token token = _input.LT(-1);

	        if (null == token) {
	            return -1;
	        }

	        return token.getLine();
	    }

	    @Override
	    public int getErrorColumn() {
	        Token token = _input.LT(-1);

	        if (null == token) {
	            return -1;
	        }

	        return token.getCharPositionInLine() + 1 + token.getText().length();
	    }

	public GroovyParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN);
	}

	public static class CompilationUnitContext extends GroovyParserRuleContext {
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public TerminalNode EOF() { return getToken(GroovyParser.EOF, 0); }
		public PackageDeclarationContext packageDeclaration() {
			return getRuleContext(PackageDeclarationContext.class,0);
		}
		public ScriptStatementsContext scriptStatements() {
			return getRuleContext(ScriptStatementsContext.class,0);
		}
		public SepContext sep() {
			return getRuleContext(SepContext.class,0);
		}
		public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilationUnit; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCompilationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CompilationUnitContext compilationUnit() throws RecognitionException {
		CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_compilationUnit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(334);
			nls();
			setState(339);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(335);
				packageDeclaration();
				setState(337);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(336);
					sep();
					}
					break;
				}
				}
				break;
			}
			setState(342);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(341);
				scriptStatements();
				}
				break;
			}
			setState(344);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScriptStatementsContext extends GroovyParserRuleContext {
		public List<? extends ScriptStatementContext> scriptStatement() {
			return getRuleContexts(ScriptStatementContext.class);
		}
		public ScriptStatementContext scriptStatement(int i) {
			return getRuleContext(ScriptStatementContext.class,i);
		}
		public List<? extends SepContext> sep() {
			return getRuleContexts(SepContext.class);
		}
		public SepContext sep(int i) {
			return getRuleContext(SepContext.class,i);
		}
		public ScriptStatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scriptStatements; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitScriptStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ScriptStatementsContext scriptStatements() throws RecognitionException {
		ScriptStatementsContext _localctx = new ScriptStatementsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_scriptStatements);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(346);
			scriptStatement();
			setState(352);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(347);
					sep();
					setState(348);
					scriptStatement();
					}
					} 
				}
				setState(354);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(356);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(355);
				sep();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScriptStatementContext extends GroovyParserRuleContext {
		public ImportDeclarationContext importDeclaration() {
			return getRuleContext(ImportDeclarationContext.class,0);
		}
		public TypeDeclarationContext typeDeclaration() {
			return getRuleContext(TypeDeclarationContext.class,0);
		}
		public MethodDeclarationContext methodDeclaration() {
			return getRuleContext(MethodDeclarationContext.class,0);
		}
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public ScriptStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scriptStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitScriptStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ScriptStatementContext scriptStatement() throws RecognitionException {
		ScriptStatementContext _localctx = new ScriptStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_scriptStatement);
		try {
			setState(363);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(358);
				importDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(359);
				typeDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(360);
				if (!( !SemanticPredicates.isInvalidMethodDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidMethodDeclaration(_input) ");
				setState(361);
				methodDeclaration(3, 9);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(362);
				statement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PackageDeclarationContext extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode PACKAGE() { return getToken(GroovyParser.PACKAGE, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public PackageDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitPackageDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final PackageDeclarationContext packageDeclaration() throws RecognitionException {
		PackageDeclarationContext _localctx = new PackageDeclarationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_packageDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(365);
			annotationsOpt();
			setState(366);
			match(PACKAGE);
			setState(367);
			qualifiedName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportDeclarationContext extends GroovyParserRuleContext {
		public IdentifierContext alias;
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode IMPORT() { return getToken(GroovyParser.IMPORT, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode STATIC() { return getToken(GroovyParser.STATIC, 0); }
		public TerminalNode DOT() { return getToken(GroovyParser.DOT, 0); }
		public TerminalNode MUL() { return getToken(GroovyParser.MUL, 0); }
		public TerminalNode AS() { return getToken(GroovyParser.AS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode MODULE() { return getToken(GroovyParser.MODULE, 0); }
		public ImportDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitImportDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ImportDeclarationContext importDeclaration() throws RecognitionException {
		ImportDeclarationContext _localctx = new ImportDeclarationContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_importDeclaration);
		int _la;
		try {
			setState(386);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(369);
				annotationsOpt();
				setState(370);
				match(IMPORT);
				setState(372);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(371);
					match(STATIC);
					}
				}

				setState(374);
				qualifiedName();
				setState(379);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case DOT:
					{
					setState(375);
					match(DOT);
					setState(376);
					match(MUL);
					}
					break;
				case AS:
					{
					setState(377);
					match(AS);
					setState(378);
					_localctx.alias = identifier();
					}
					break;
				case EOF:
				case SEMI:
				case NL:
					break;
				default:
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(381);
				annotationsOpt();
				setState(382);
				match(IMPORT);
				setState(383);
				match(MODULE);
				setState(384);
				qualifiedName();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeDeclarationContext extends GroovyParserRuleContext {
		public ClassOrInterfaceModifiersOptContext classOrInterfaceModifiersOpt() {
			return getRuleContext(ClassOrInterfaceModifiersOptContext.class,0);
		}
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
		}
		public TypeDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeDeclarationContext typeDeclaration() throws RecognitionException {
		TypeDeclarationContext _localctx = new TypeDeclarationContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_typeDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(388);
			classOrInterfaceModifiersOpt();
			setState(389);
			classDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModifierContext extends GroovyParserRuleContext {
		public Token m;
		public ClassOrInterfaceModifierContext classOrInterfaceModifier() {
			return getRuleContext(ClassOrInterfaceModifierContext.class,0);
		}
		public TerminalNode NATIVE() { return getToken(GroovyParser.NATIVE, 0); }
		public TerminalNode SYNCHRONIZED() { return getToken(GroovyParser.SYNCHRONIZED, 0); }
		public TerminalNode TRANSIENT() { return getToken(GroovyParser.TRANSIENT, 0); }
		public TerminalNode VOLATILE() { return getToken(GroovyParser.VOLATILE, 0); }
		public TerminalNode DEF() { return getToken(GroovyParser.DEF, 0); }
		public TerminalNode VAL() { return getToken(GroovyParser.VAL, 0); }
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public ModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifier; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ModifierContext modifier() throws RecognitionException {
		ModifierContext _localctx = new ModifierContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_modifier);
		int _la;
		try {
			setState(393);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case DEFAULT:
			case FINAL:
			case NON_SEALED:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case SEALED:
			case STATIC:
			case STRICTFP:
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(391);
				classOrInterfaceModifier();
				}
				break;
			case DEF:
			case NATIVE:
			case SYNCHRONIZED:
			case TRANSIENT:
			case VAL:
			case VAR:
			case VOLATILE:
				enterOuterAlt(_localctx, 2);
				{
				setState(392);
				_localctx.m = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << NATIVE) | (1L << SYNCHRONIZED) | (1L << TRANSIENT) | (1L << VAL) | (1L << VAR) | (1L << VOLATILE))) != 0)) ) {
					_localctx.m = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModifiersOptContext extends GroovyParserRuleContext {
		public ModifiersContext modifiers() {
			return getRuleContext(ModifiersContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public ModifiersOptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifiersOpt; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitModifiersOpt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ModifiersOptContext modifiersOpt() throws RecognitionException {
		ModifiersOptContext _localctx = new ModifiersOptContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_modifiersOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(398);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				setState(395);
				modifiers();
				setState(396);
				nls();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModifiersContext extends GroovyParserRuleContext {
		public List<? extends ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public ModifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifiers; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitModifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ModifiersContext modifiers() throws RecognitionException {
		ModifiersContext _localctx = new ModifiersContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_modifiers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(400);
			modifier();
			setState(406);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(401);
					nls();
					setState(402);
					modifier();
					}
					} 
				}
				setState(408);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassOrInterfaceModifiersOptContext extends GroovyParserRuleContext {
		public ClassOrInterfaceModifiersContext classOrInterfaceModifiers() {
			return getRuleContext(ClassOrInterfaceModifiersContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public ClassOrInterfaceModifiersOptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceModifiersOpt; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassOrInterfaceModifiersOpt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassOrInterfaceModifiersOptContext classOrInterfaceModifiersOpt() throws RecognitionException {
		ClassOrInterfaceModifiersOptContext _localctx = new ClassOrInterfaceModifiersOptContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_classOrInterfaceModifiersOpt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(416);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(409);
				classOrInterfaceModifiers();
				setState(413);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NL) {
					{
					{
					setState(410);
					match(NL);
					}
					}
					setState(415);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassOrInterfaceModifiersContext extends GroovyParserRuleContext {
		public List<? extends ClassOrInterfaceModifierContext> classOrInterfaceModifier() {
			return getRuleContexts(ClassOrInterfaceModifierContext.class);
		}
		public ClassOrInterfaceModifierContext classOrInterfaceModifier(int i) {
			return getRuleContext(ClassOrInterfaceModifierContext.class,i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public ClassOrInterfaceModifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceModifiers; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassOrInterfaceModifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassOrInterfaceModifiersContext classOrInterfaceModifiers() throws RecognitionException {
		ClassOrInterfaceModifiersContext _localctx = new ClassOrInterfaceModifiersContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_classOrInterfaceModifiers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(418);
			classOrInterfaceModifier();
			setState(424);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(419);
					nls();
					setState(420);
					classOrInterfaceModifier();
					}
					} 
				}
				setState(426);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassOrInterfaceModifierContext extends GroovyParserRuleContext {
		public Token m;
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public TerminalNode PUBLIC() { return getToken(GroovyParser.PUBLIC, 0); }
		public TerminalNode PROTECTED() { return getToken(GroovyParser.PROTECTED, 0); }
		public TerminalNode PRIVATE() { return getToken(GroovyParser.PRIVATE, 0); }
		public TerminalNode STATIC() { return getToken(GroovyParser.STATIC, 0); }
		public TerminalNode ABSTRACT() { return getToken(GroovyParser.ABSTRACT, 0); }
		public TerminalNode SEALED() { return getToken(GroovyParser.SEALED, 0); }
		public TerminalNode NON_SEALED() { return getToken(GroovyParser.NON_SEALED, 0); }
		public TerminalNode FINAL() { return getToken(GroovyParser.FINAL, 0); }
		public TerminalNode STRICTFP() { return getToken(GroovyParser.STRICTFP, 0); }
		public TerminalNode DEFAULT() { return getToken(GroovyParser.DEFAULT, 0); }
		public ClassOrInterfaceModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceModifier; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassOrInterfaceModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassOrInterfaceModifierContext classOrInterfaceModifier() throws RecognitionException {
		ClassOrInterfaceModifierContext _localctx = new ClassOrInterfaceModifierContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_classOrInterfaceModifier);
		int _la;
		try {
			setState(429);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(427);
				annotation();
				}
				break;
			case ABSTRACT:
			case DEFAULT:
			case FINAL:
			case NON_SEALED:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case SEALED:
			case STATIC:
			case STRICTFP:
				enterOuterAlt(_localctx, 2);
				{
				setState(428);
				_localctx.m = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABSTRACT) | (1L << DEFAULT) | (1L << FINAL) | (1L << NON_SEALED) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP))) != 0)) ) {
					_localctx.m = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableModifierContext extends GroovyParserRuleContext {
		public Token m;
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public TerminalNode FINAL() { return getToken(GroovyParser.FINAL, 0); }
		public TerminalNode DEF() { return getToken(GroovyParser.DEF, 0); }
		public TerminalNode VAL() { return getToken(GroovyParser.VAL, 0); }
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public TerminalNode PUBLIC() { return getToken(GroovyParser.PUBLIC, 0); }
		public TerminalNode PROTECTED() { return getToken(GroovyParser.PROTECTED, 0); }
		public TerminalNode PRIVATE() { return getToken(GroovyParser.PRIVATE, 0); }
		public TerminalNode STATIC() { return getToken(GroovyParser.STATIC, 0); }
		public TerminalNode ABSTRACT() { return getToken(GroovyParser.ABSTRACT, 0); }
		public TerminalNode STRICTFP() { return getToken(GroovyParser.STRICTFP, 0); }
		public VariableModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableModifier; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableModifierContext variableModifier() throws RecognitionException {
		VariableModifierContext _localctx = new VariableModifierContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_variableModifier);
		int _la;
		try {
			setState(433);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(431);
				annotation();
				}
				break;
			case DEF:
			case ABSTRACT:
			case FINAL:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
			case STRICTFP:
			case VAL:
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(432);
				_localctx.m = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << ABSTRACT) | (1L << FINAL) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << STATIC) | (1L << STRICTFP) | (1L << VAL) | (1L << VAR))) != 0)) ) {
					_localctx.m = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableModifiersOptContext extends GroovyParserRuleContext {
		public VariableModifiersContext variableModifiers() {
			return getRuleContext(VariableModifiersContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public VariableModifiersOptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableModifiersOpt; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableModifiersOpt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableModifiersOptContext variableModifiersOpt() throws RecognitionException {
		VariableModifiersOptContext _localctx = new VariableModifiersOptContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_variableModifiersOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(438);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(435);
				variableModifiers();
				setState(436);
				nls();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableModifiersContext extends GroovyParserRuleContext {
		public List<? extends VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public VariableModifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableModifiers; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableModifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableModifiersContext variableModifiers() throws RecognitionException {
		VariableModifiersContext _localctx = new VariableModifiersContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_variableModifiers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(440);
			variableModifier();
			setState(446);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(441);
					nls();
					setState(442);
					variableModifier();
					}
					} 
				}
				setState(448);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeParametersContext extends GroovyParserRuleContext {
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends TypeParameterContext> typeParameter() {
			return getRuleContexts(TypeParameterContext.class);
		}
		public TypeParameterContext typeParameter(int i) {
			return getRuleContext(TypeParameterContext.class,i);
		}
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public TypeParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameters; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeParametersContext typeParameters() throws RecognitionException {
		TypeParametersContext _localctx = new TypeParametersContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_typeParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(449);
			match(LT);
			setState(450);
			nls();
			setState(451);
			typeParameter();
			setState(458);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(452);
				match(COMMA);
				setState(453);
				nls();
				setState(454);
				typeParameter();
				}
				}
				setState(460);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(461);
			nls();
			setState(462);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeParameterContext extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public ClassNameContext className() {
			return getRuleContext(ClassNameContext.class,0);
		}
		public TerminalNode EXTENDS() { return getToken(GroovyParser.EXTENDS, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public TypeBoundContext typeBound() {
			return getRuleContext(TypeBoundContext.class,0);
		}
		public TypeParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameter; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeParameterContext typeParameter() throws RecognitionException {
		TypeParameterContext _localctx = new TypeParameterContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_typeParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(464);
			annotationsOpt();
			setState(465);
			className();
			setState(470);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(466);
				match(EXTENDS);
				setState(467);
				nls();
				setState(468);
				typeBound();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeBoundContext extends GroovyParserRuleContext {
		public List<? extends TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<? extends TerminalNode> BITAND() { return getTokens(GroovyParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(GroovyParser.BITAND, i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TypeBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeBound; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeBound(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeBoundContext typeBound() throws RecognitionException {
		TypeBoundContext _localctx = new TypeBoundContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_typeBound);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(472);
			type();
			setState(479);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITAND) {
				{
				{
				setState(473);
				match(BITAND);
				setState(474);
				nls();
				setState(475);
				type();
				}
				}
				setState(481);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeListContext extends GroovyParserRuleContext {
		public List<? extends TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeList; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeListContext typeList() throws RecognitionException {
		TypeListContext _localctx = new TypeListContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(482);
			type();
			setState(489);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(483);
				match(COMMA);
				setState(484);
				nls();
				setState(485);
				type();
				}
				}
				setState(491);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassDeclarationContext extends GroovyParserRuleContext {
		public int t;
		public TypeListContext scs;
		public TypeListContext is;
		public TypeListContext ps;
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public TerminalNode CLASS() { return getToken(GroovyParser.CLASS, 0); }
		public TerminalNode INTERFACE() { return getToken(GroovyParser.INTERFACE, 0); }
		public TerminalNode ENUM() { return getToken(GroovyParser.ENUM, 0); }
		public TerminalNode AT() { return getToken(GroovyParser.AT, 0); }
		public TerminalNode TRAIT() { return getToken(GroovyParser.TRAIT, 0); }
		public TerminalNode RECORD() { return getToken(GroovyParser.RECORD, 0); }
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public TerminalNode EXTENDS() { return getToken(GroovyParser.EXTENDS, 0); }
		public TerminalNode IMPLEMENTS() { return getToken(GroovyParser.IMPLEMENTS, 0); }
		public TerminalNode PERMITS() { return getToken(GroovyParser.PERMITS, 0); }
		public List<? extends TypeListContext> typeList() {
			return getRuleContexts(TypeListContext.class);
		}
		public TypeListContext typeList(int i) {
			return getRuleContext(TypeListContext.class,i);
		}
		public ClassDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassDeclarationContext classDeclaration() throws RecognitionException {
		ClassDeclarationContext _localctx = new ClassDeclarationContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_classDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(505);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CLASS:
				{
				setState(492);
				match(CLASS);
				 _localctx.t =  0; 
				}
				break;
			case INTERFACE:
				{
				setState(494);
				match(INTERFACE);
				 _localctx.t =  1; 
				}
				break;
			case ENUM:
				{
				setState(496);
				match(ENUM);
				 _localctx.t =  2; 
				}
				break;
			case AT:
				{
				setState(498);
				match(AT);
				setState(499);
				match(INTERFACE);
				 _localctx.t =  3; 
				}
				break;
			case TRAIT:
				{
				setState(501);
				match(TRAIT);
				 _localctx.t =  4; 
				}
				break;
			case RECORD:
				{
				setState(503);
				match(RECORD);
				 _localctx.t =  5; 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(507);
			identifier();
			setState(511);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				{
				setState(508);
				nls();
				setState(509);
				typeParameters();
				}
				break;
			}
			setState(516);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				{
				setState(513);
				nls();
				setState(514);
				formalParameters();
				}
				break;
			}
			setState(523);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(518);
				nls();
				setState(519);
				match(EXTENDS);
				setState(520);
				nls();
				setState(521);
				_localctx.scs = typeList();
				}
				break;
			}
			setState(530);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				setState(525);
				nls();
				setState(526);
				match(IMPLEMENTS);
				setState(527);
				nls();
				setState(528);
				_localctx.is = typeList();
				}
				break;
			}
			setState(537);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				{
				setState(532);
				nls();
				setState(533);
				match(PERMITS);
				setState(534);
				nls();
				setState(535);
				_localctx.ps = typeList();
				}
				break;
			}
			setState(539);
			nls();
			setState(540);
			classBody(_localctx.t);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassBodyContext extends GroovyParserRuleContext {
		public int t;
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public EnumConstantsContext enumConstants() {
			return getRuleContext(EnumConstantsContext.class,0);
		}
		public List<? extends SepContext> sep() {
			return getRuleContexts(SepContext.class);
		}
		public SepContext sep(int i) {
			return getRuleContext(SepContext.class,i);
		}
		public List<? extends ClassBodyDeclarationContext> classBodyDeclaration() {
			return getRuleContexts(ClassBodyDeclarationContext.class);
		}
		public ClassBodyDeclarationContext classBodyDeclaration(int i) {
			return getRuleContext(ClassBodyDeclarationContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(GroovyParser.COMMA, 0); }
		public TerminalNode SEMI() { return getToken(GroovyParser.SEMI, 0); }
		public ClassBodyContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public ClassBodyContext(ParserRuleContext parent, int invokingState, int t) {
			super(parent, invokingState);
			this.t = t;
		}
		@Override public int getRuleIndex() { return RULE_classBody; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassBody(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassBodyContext classBody(int t) throws RecognitionException {
		ClassBodyContext _localctx = new ClassBodyContext(_ctx, getState(), t);
		enterRule(_localctx, 40, RULE_classBody);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(542);
			match(LBRACE);
			setState(543);
			nls();
			setState(584);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(544);
				if (!( _localctx.t == 2 )) throw createFailedPredicateException(" $t == 2 ");
				setState(545);
				enumConstants();
				setState(571);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
				case 1:
					{
					setState(549);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
					case 1:
						{
						setState(546);
						nls();
						setState(547);
						match(COMMA);
						}
						break;
					}
					}
					break;
				case 2:
					{
					setState(559);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
					case 1:
						{
						setState(554);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
						case 1:
							{
							setState(551);
							nls();
							setState(552);
							match(COMMA);
							}
							break;
						}
						setState(556);
						nls();
						setState(557);
						match(SEMI);
						}
						break;
					}
					setState(561);
					nls();
					setState(562);
					classBodyDeclaration(_localctx.t);
					setState(568);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
					while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(563);
							sep();
							setState(564);
							classBodyDeclaration(_localctx.t);
							}
							} 
						}
						setState(570);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
					}
					}
					break;
				}
				}
				break;
			case 2:
				{
				setState(582);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << ASYNC) | (1L << AWAIT) | (1L << DEFER) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << CLASS) | (1L << DEFAULT) | (1L << ENUM) | (1L << FINAL) | (1L << INTERFACE) | (1L << MODULE) | (1L << NATIVE) | (1L << NON_SEALED) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << SYNCHRONIZED) | (1L << TRANSIENT) | (1L << VAL) | (1L << VAR) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & ((1L << (YIELD - 65)) | (1L << (LBRACE - 65)) | (1L << (LT - 65)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)))) != 0)) {
					{
					setState(573);
					classBodyDeclaration(_localctx.t);
					setState(579);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
					while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(574);
							sep();
							setState(575);
							classBodyDeclaration(_localctx.t);
							}
							} 
						}
						setState(581);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
					}
					}
				}

				}
				break;
			}
			setState(587);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(586);
				sep();
				}
			}

			setState(589);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnumConstantsContext extends GroovyParserRuleContext {
		public List<? extends EnumConstantContext> enumConstant() {
			return getRuleContexts(EnumConstantContext.class);
		}
		public EnumConstantContext enumConstant(int i) {
			return getRuleContext(EnumConstantContext.class,i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public EnumConstantsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumConstants; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEnumConstants(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EnumConstantsContext enumConstants() throws RecognitionException {
		EnumConstantsContext _localctx = new EnumConstantsContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_enumConstants);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(591);
			enumConstant();
			setState(599);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(592);
					nls();
					setState(593);
					match(COMMA);
					setState(594);
					nls();
					setState(595);
					enumConstant();
					}
					} 
				}
				setState(601);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnumConstantContext extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public AnonymousInnerClassDeclarationContext anonymousInnerClassDeclaration() {
			return getRuleContext(AnonymousInnerClassDeclarationContext.class,0);
		}
		public EnumConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumConstant; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEnumConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EnumConstantContext enumConstant() throws RecognitionException {
		EnumConstantContext _localctx = new EnumConstantContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_enumConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(602);
			annotationsOpt();
			setState(603);
			identifier();
			setState(605);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(604);
				arguments();
				}
			}

			setState(608);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				{
				setState(607);
				anonymousInnerClassDeclaration(1);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassBodyDeclarationContext extends GroovyParserRuleContext {
		public int t;
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode STATIC() { return getToken(GroovyParser.STATIC, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public MemberDeclarationContext memberDeclaration() {
			return getRuleContext(MemberDeclarationContext.class,0);
		}
		public ClassBodyDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public ClassBodyDeclarationContext(ParserRuleContext parent, int invokingState, int t) {
			super(parent, invokingState);
			this.t = t;
		}
		@Override public int getRuleIndex() { return RULE_classBodyDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassBodyDeclarationContext classBodyDeclaration(int t) throws RecognitionException {
		ClassBodyDeclarationContext _localctx = new ClassBodyDeclarationContext(_ctx, getState(), t);
		enterRule(_localctx, 46, RULE_classBodyDeclaration);
		int _la;
		try {
			setState(616);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(612);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(610);
					match(STATIC);
					setState(611);
					nls();
					}
				}

				setState(614);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(615);
				memberDeclaration(_localctx.t);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MemberDeclarationContext extends GroovyParserRuleContext {
		public int t;
		public MethodDeclarationContext methodDeclaration() {
			return getRuleContext(MethodDeclarationContext.class,0);
		}
		public FieldDeclarationContext fieldDeclaration() {
			return getRuleContext(FieldDeclarationContext.class,0);
		}
		public ModifiersOptContext modifiersOpt() {
			return getRuleContext(ModifiersOptContext.class,0);
		}
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
		}
		public CompactConstructorDeclarationContext compactConstructorDeclaration() {
			return getRuleContext(CompactConstructorDeclarationContext.class,0);
		}
		public MemberDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public MemberDeclarationContext(ParserRuleContext parent, int invokingState, int t) {
			super(parent, invokingState);
			this.t = t;
		}
		@Override public int getRuleIndex() { return RULE_memberDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMemberDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MemberDeclarationContext memberDeclaration(int t) throws RecognitionException {
		MemberDeclarationContext _localctx = new MemberDeclarationContext(_ctx, getState(), t);
		enterRule(_localctx, 48, RULE_memberDeclaration);
		try {
			setState(625);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(618);
				methodDeclaration(0, _localctx.t);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(619);
				fieldDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(620);
				modifiersOpt();
				setState(623);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
				case 1:
					{
					setState(621);
					classDeclaration();
					}
					break;
				case 2:
					{
					setState(622);
					compactConstructorDeclaration();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MethodDeclarationContext extends GroovyParserRuleContext {
		public int t;
		public int ct;
		public ModifiersOptContext modifiersOpt() {
			return getRuleContext(ModifiersOptContext.class,0);
		}
		public MethodNameContext methodName() {
			return getRuleContext(MethodNameContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public ReturnTypeContext returnType() {
			return getRuleContext(ReturnTypeContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode THROWS() { return getToken(GroovyParser.THROWS, 0); }
		public QualifiedClassNameListContext qualifiedClassNameList() {
			return getRuleContext(QualifiedClassNameListContext.class,0);
		}
		public MethodBodyContext methodBody() {
			return getRuleContext(MethodBodyContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(GroovyParser.DEFAULT, 0); }
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public MethodDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public MethodDeclarationContext(ParserRuleContext parent, int invokingState, int t, int ct) {
			super(parent, invokingState);
			this.t = t;
			this.ct = ct;
		}
		@Override public int getRuleIndex() { return RULE_methodDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MethodDeclarationContext methodDeclaration(int t,int ct) throws RecognitionException {
		MethodDeclarationContext _localctx = new MethodDeclarationContext(_ctx, getState(), t, ct);
		enterRule(_localctx, 50, RULE_methodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(627);
			modifiersOpt();
			setState(629);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(628);
				typeParameters();
				}
			}

			setState(634);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				{
				setState(631);
				returnType(_localctx.ct);
				setState(632);
				nls();
				}
				break;
			}
			setState(636);
			methodName();
			setState(637);
			formalParameters();
			setState(655);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				{
				setState(638);
				if (!( _localctx.ct == 3 )) throw createFailedPredicateException(" $ct == 3 ");
				{
				setState(639);
				match(DEFAULT);
				setState(640);
				nls();
				setState(641);
				elementValue();
				}
				}
				break;
			case 2:
				{
				setState(643);
				nls();
				setState(644);
				match(THROWS);
				setState(645);
				nls();
				setState(646);
				qualifiedClassNameList();
				setState(650);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
				case 1:
					{
					setState(647);
					nls();
					setState(648);
					methodBody();
					}
					break;
				}
				}
				break;
			case 3:
				{
				setState(652);
				nls();
				setState(653);
				methodBody();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CompactConstructorDeclarationContext extends GroovyParserRuleContext {
		public MethodNameContext methodName() {
			return getRuleContext(MethodNameContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public MethodBodyContext methodBody() {
			return getRuleContext(MethodBodyContext.class,0);
		}
		public CompactConstructorDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compactConstructorDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCompactConstructorDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CompactConstructorDeclarationContext compactConstructorDeclaration() throws RecognitionException {
		CompactConstructorDeclarationContext _localctx = new CompactConstructorDeclarationContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_compactConstructorDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(657);
			methodName();
			setState(658);
			nls();
			setState(659);
			methodBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MethodNameContext extends GroovyParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public MethodNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMethodName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MethodNameContext methodName() throws RecognitionException {
		MethodNameContext _localctx = new MethodNameContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_methodName);
		try {
			setState(663);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(661);
				identifier();
				}
				break;
			case StringLiteral:
				enterOuterAlt(_localctx, 2);
				{
				setState(662);
				stringLiteral();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReturnTypeContext extends GroovyParserRuleContext {
		public int ct;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode VOID() { return getToken(GroovyParser.VOID, 0); }
		public ReturnTypeContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public ReturnTypeContext(ParserRuleContext parent, int invokingState, int ct) {
			super(parent, invokingState);
			this.ct = ct;
		}
		@Override public int getRuleIndex() { return RULE_returnType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitReturnType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ReturnTypeContext returnType(int ct) throws RecognitionException {
		ReturnTypeContext _localctx = new ReturnTypeContext(_ctx, getState(), ct);
		enterRule(_localctx, 56, RULE_returnType);
		try {
			setState(667);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case BuiltInPrimitiveType:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(665);
				standardType();
				}
				break;
			case VOID:
				enterOuterAlt(_localctx, 2);
				{
				setState(666);
				match(VOID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldDeclarationContext extends GroovyParserRuleContext {
		public VariableDeclarationContext variableDeclaration() {
			return getRuleContext(VariableDeclarationContext.class,0);
		}
		public FieldDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitFieldDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final FieldDeclarationContext fieldDeclaration() throws RecognitionException {
		FieldDeclarationContext _localctx = new FieldDeclarationContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_fieldDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(669);
			variableDeclaration(1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDeclaratorsContext extends GroovyParserRuleContext {
		public List<? extends VariableDeclaratorContext> variableDeclarator() {
			return getRuleContexts(VariableDeclaratorContext.class);
		}
		public VariableDeclaratorContext variableDeclarator(int i) {
			return getRuleContext(VariableDeclaratorContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public VariableDeclaratorsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableDeclarators; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableDeclarators(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableDeclaratorsContext variableDeclarators() throws RecognitionException {
		VariableDeclaratorsContext _localctx = new VariableDeclaratorsContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_variableDeclarators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(671);
			variableDeclarator();
			setState(678);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(672);
				match(COMMA);
				setState(673);
				nls();
				setState(674);
				variableDeclarator();
				}
				}
				setState(680);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDeclaratorContext extends GroovyParserRuleContext {
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public VariableInitializerContext variableInitializer() {
			return getRuleContext(VariableInitializerContext.class,0);
		}
		public VariableDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableDeclarator; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableDeclaratorContext variableDeclarator() throws RecognitionException {
		VariableDeclaratorContext _localctx = new VariableDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_variableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(681);
			variableDeclaratorId();
			setState(687);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
			case 1:
				{
				setState(682);
				nls();
				setState(683);
				match(ASSIGN);
				setState(684);
				nls();
				setState(685);
				variableInitializer();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDeclaratorIdContext extends GroovyParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public VariableDeclaratorIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableDeclaratorId; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableDeclaratorId(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableDeclaratorIdContext variableDeclaratorId() throws RecognitionException {
		VariableDeclaratorIdContext _localctx = new VariableDeclaratorIdContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_variableDeclaratorId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(689);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableInitializerContext extends GroovyParserRuleContext {
		public EnhancedStatementExpressionContext enhancedStatementExpression() {
			return getRuleContext(EnhancedStatementExpressionContext.class,0);
		}
		public VariableInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableInitializer; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableInitializerContext variableInitializer() throws RecognitionException {
		VariableInitializerContext _localctx = new VariableInitializerContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_variableInitializer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(691);
			enhancedStatementExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeContext extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode VOID() { return getToken(GroovyParser.VOID, 0); }
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public ReferenceTypeContext referenceType() {
			return getRuleContext(ReferenceTypeContext.class,0);
		}
		public List<? extends Dim0Context> dim0() {
			return getRuleContexts(Dim0Context.class);
		}
		public Dim0Context dim0(int i) {
			return getRuleContext(Dim0Context.class,i);
		}
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_type);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(693);
			annotationsOpt();
			setState(697);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VOID:
				{
				setState(694);
				match(VOID);
				}
				break;
			case BuiltInPrimitiveType:
				{
				setState(695);
				primitiveType();
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(696);
				referenceType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(702);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,54,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(699);
					dim0();
					}
					} 
				}
				setState(704);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,54,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimitiveTypeContext extends GroovyParserRuleContext {
		public TerminalNode BuiltInPrimitiveType() { return getToken(GroovyParser.BuiltInPrimitiveType, 0); }
		public PrimitiveTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitPrimitiveType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final PrimitiveTypeContext primitiveType() throws RecognitionException {
		PrimitiveTypeContext _localctx = new PrimitiveTypeContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_primitiveType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(705);
			match(BuiltInPrimitiveType);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReferenceTypeContext extends GroovyParserRuleContext {
		public QualifiedClassNameContext qualifiedClassName() {
			return getRuleContext(QualifiedClassNameContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public QualifiedStandardClassNameContext qualifiedStandardClassName() {
			return getRuleContext(QualifiedStandardClassNameContext.class,0);
		}
		public ReferenceTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitReferenceType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ReferenceTypeContext referenceType() throws RecognitionException {
		ReferenceTypeContext _localctx = new ReferenceTypeContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_referenceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(707);
			qualifiedClassName();
			setState(709);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(708);
				typeArguments();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatchingTypeContext extends GroovyParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MatchingTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchingType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMatchingType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MatchingTypeContext matchingType() throws RecognitionException {
		MatchingTypeContext _localctx = new MatchingTypeContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_matchingType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(711);
			standardType();
			setState(713);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				{
				setState(712);
				identifier();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final TypeContext standardType() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_standardType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(715);
			annotationsOpt();
			setState(718);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BuiltInPrimitiveType:
				{
				setState(716);
				primitiveType();
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(717);
				standardClassOrInterfaceType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(723);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(720);
					dim0();
					}
					} 
				}
				setState(725);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final ReferenceTypeContext standardClassOrInterfaceType() throws RecognitionException {
		ReferenceTypeContext _localctx = new ReferenceTypeContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_standardClassOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(726);
			qualifiedStandardClassName();
			setState(728);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				{
				setState(727);
				typeArguments();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeArgumentsContext extends GroovyParserRuleContext {
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends TypeArgumentContext> typeArgument() {
			return getRuleContexts(TypeArgumentContext.class);
		}
		public TypeArgumentContext typeArgument(int i) {
			return getRuleContext(TypeArgumentContext.class,i);
		}
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public TypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArguments; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeArgumentsContext typeArguments() throws RecognitionException {
		TypeArgumentsContext _localctx = new TypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(730);
			match(LT);
			setState(731);
			nls();
			setState(732);
			typeArgument();
			setState(739);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(733);
				match(COMMA);
				setState(734);
				nls();
				setState(735);
				typeArgument();
				}
				}
				setState(741);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(742);
			nls();
			setState(743);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeArgumentContext extends GroovyParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode QUESTION() { return getToken(GroovyParser.QUESTION, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public TerminalNode EXTENDS() { return getToken(GroovyParser.EXTENDS, 0); }
		public TerminalNode SUPER() { return getToken(GroovyParser.SUPER, 0); }
		public TypeArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArgument; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeArgumentContext typeArgument() throws RecognitionException {
		TypeArgumentContext _localctx = new TypeArgumentContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_typeArgument);
		int _la;
		try {
			setState(754);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(745);
				type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(746);
				annotationsOpt();
				setState(747);
				match(QUESTION);
				setState(752);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXTENDS || _la==SUPER) {
					{
					setState(748);
					_la = _input.LA(1);
					if ( !(_la==EXTENDS || _la==SUPER) ) {
					_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}

						_errHandler.reportMatch(this);
						consume();
					}
					setState(749);
					nls();
					setState(750);
					type();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnnotatedQualifiedClassNameContext extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public QualifiedClassNameContext qualifiedClassName() {
			return getRuleContext(QualifiedClassNameContext.class,0);
		}
		public AnnotatedQualifiedClassNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotatedQualifiedClassName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAnnotatedQualifiedClassName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final AnnotatedQualifiedClassNameContext annotatedQualifiedClassName() throws RecognitionException {
		AnnotatedQualifiedClassNameContext _localctx = new AnnotatedQualifiedClassNameContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_annotatedQualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(756);
			annotationsOpt();
			setState(757);
			qualifiedClassName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedClassNameListContext extends GroovyParserRuleContext {
		public List<? extends AnnotatedQualifiedClassNameContext> annotatedQualifiedClassName() {
			return getRuleContexts(AnnotatedQualifiedClassNameContext.class);
		}
		public AnnotatedQualifiedClassNameContext annotatedQualifiedClassName(int i) {
			return getRuleContext(AnnotatedQualifiedClassNameContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public QualifiedClassNameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedClassNameList; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitQualifiedClassNameList(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final QualifiedClassNameListContext qualifiedClassNameList() throws RecognitionException {
		QualifiedClassNameListContext _localctx = new QualifiedClassNameListContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_qualifiedClassNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(759);
			annotatedQualifiedClassName();
			setState(766);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(760);
				match(COMMA);
				setState(761);
				nls();
				setState(762);
				annotatedQualifiedClassName();
				}
				}
				setState(768);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FormalParametersContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public FormalParameterListContext formalParameterList() {
			return getRuleContext(FormalParameterListContext.class,0);
		}
		public FormalParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameters; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitFormalParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final FormalParametersContext formalParameters() throws RecognitionException {
		FormalParametersContext _localctx = new FormalParametersContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(769);
			match(LPAREN);
			setState(771);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (ABSTRACT - 7)) | (1L << (FINAL - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (PRIVATE - 7)) | (1L << (PROTECTED - 7)) | (1L << (PUBLIC - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (STATIC - 7)) | (1L << (STRICTFP - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (YIELD - 7)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)) | (1L << (ELLIPSIS - 136)))) != 0)) {
				{
				setState(770);
				formalParameterList();
				}
			}

			setState(773);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FormalParameterListContext extends GroovyParserRuleContext {
		public List<? extends FormalParameterContext> formalParameter() {
			return getRuleContexts(FormalParameterContext.class);
		}
		public FormalParameterContext formalParameter(int i) {
			return getRuleContext(FormalParameterContext.class,i);
		}
		public ThisFormalParameterContext thisFormalParameter() {
			return getRuleContext(ThisFormalParameterContext.class,0);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public FormalParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameterList; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitFormalParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final FormalParameterListContext formalParameterList() throws RecognitionException {
		FormalParameterListContext _localctx = new FormalParameterListContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_formalParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(777);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				{
				setState(775);
				formalParameter();
				}
				break;
			case 2:
				{
				setState(776);
				thisFormalParameter();
				}
				break;
			}
			setState(785);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(779);
				match(COMMA);
				setState(780);
				nls();
				setState(781);
				formalParameter();
				}
				}
				setState(787);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ThisFormalParameterContext extends GroovyParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode THIS() { return getToken(GroovyParser.THIS, 0); }
		public ThisFormalParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_thisFormalParameter; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitThisFormalParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ThisFormalParameterContext thisFormalParameter() throws RecognitionException {
		ThisFormalParameterContext _localctx = new ThisFormalParameterContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_thisFormalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(788);
			type();
			setState(789);
			match(THIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FormalParameterContext extends GroovyParserRuleContext {
		public VariableModifiersOptContext variableModifiersOpt() {
			return getRuleContext(VariableModifiersOptContext.class,0);
		}
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ELLIPSIS() { return getToken(GroovyParser.ELLIPSIS, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public FormalParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameter; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitFormalParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final FormalParameterContext formalParameter() throws RecognitionException {
		FormalParameterContext _localctx = new FormalParameterContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_formalParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(791);
			variableModifiersOpt();
			setState(793);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
			case 1:
				{
				setState(792);
				type();
				}
				break;
			}
			setState(796);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELLIPSIS) {
				{
				setState(795);
				match(ELLIPSIS);
				}
			}

			setState(798);
			variableDeclaratorId();
			setState(804);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(799);
				nls();
				setState(800);
				match(ASSIGN);
				setState(801);
				nls();
				setState(802);
				expression(0);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MethodBodyContext extends GroovyParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public MethodBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodBody; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMethodBody(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MethodBodyContext methodBody() throws RecognitionException {
		MethodBodyContext _localctx = new MethodBodyContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_methodBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(806);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedNameContext extends GroovyParserRuleContext {
		public List<? extends QualifiedNameElementContext> qualifiedNameElement() {
			return getRuleContexts(QualifiedNameElementContext.class);
		}
		public QualifiedNameElementContext qualifiedNameElement(int i) {
			return getRuleContext(QualifiedNameElementContext.class,i);
		}
		public List<? extends TerminalNode> DOT() { return getTokens(GroovyParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(GroovyParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(808);
			qualifiedNameElement();
			setState(813);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(809);
					match(DOT);
					setState(810);
					qualifiedNameElement();
					}
					} 
				}
				setState(815);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedNameElementContext extends GroovyParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode DEF() { return getToken(GroovyParser.DEF, 0); }
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
		public TerminalNode AS() { return getToken(GroovyParser.AS, 0); }
		public TerminalNode TRAIT() { return getToken(GroovyParser.TRAIT, 0); }
		public QualifiedNameElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedNameElement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitQualifiedNameElement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final QualifiedNameElementContext qualifiedNameElement() throws RecognitionException {
		QualifiedNameElementContext _localctx = new QualifiedNameElementContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_qualifiedNameElement);
		try {
			setState(821);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(816);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(817);
				match(DEF);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(818);
				match(IN);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(819);
				match(AS);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(820);
				match(TRAIT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedNameElementsContext extends GroovyParserRuleContext {
		public List<? extends QualifiedNameElementContext> qualifiedNameElement() {
			return getRuleContexts(QualifiedNameElementContext.class);
		}
		public QualifiedNameElementContext qualifiedNameElement(int i) {
			return getRuleContext(QualifiedNameElementContext.class,i);
		}
		public List<? extends TerminalNode> DOT() { return getTokens(GroovyParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(GroovyParser.DOT, i);
		}
		public QualifiedNameElementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedNameElements; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitQualifiedNameElements(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final QualifiedNameElementsContext qualifiedNameElements() throws RecognitionException {
		QualifiedNameElementsContext _localctx = new QualifiedNameElementsContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_qualifiedNameElements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(828);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(823);
					qualifiedNameElement();
					setState(824);
					match(DOT);
					}
					} 
				}
				setState(830);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedClassNameContext extends GroovyParserRuleContext {
		public QualifiedNameElementsContext qualifiedNameElements() {
			return getRuleContext(QualifiedNameElementsContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public QualifiedClassNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedClassName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitQualifiedClassName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final QualifiedClassNameContext qualifiedClassName() throws RecognitionException {
		QualifiedClassNameContext _localctx = new QualifiedClassNameContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_qualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(831);
			qualifiedNameElements();
			setState(832);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedStandardClassNameContext extends GroovyParserRuleContext {
		public QualifiedNameElementsContext qualifiedNameElements() {
			return getRuleContext(QualifiedNameElementsContext.class,0);
		}
		public List<? extends ClassNameContext> className() {
			return getRuleContexts(ClassNameContext.class);
		}
		public ClassNameContext className(int i) {
			return getRuleContext(ClassNameContext.class,i);
		}
		public List<? extends TerminalNode> DOT() { return getTokens(GroovyParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(GroovyParser.DOT, i);
		}
		public QualifiedStandardClassNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedStandardClassName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitQualifiedStandardClassName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final QualifiedStandardClassNameContext qualifiedStandardClassName() throws RecognitionException {
		QualifiedStandardClassNameContext _localctx = new QualifiedStandardClassNameContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_qualifiedStandardClassName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(834);
			qualifiedNameElements();
			setState(835);
			className();
			setState(840);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(836);
					match(DOT);
					setState(837);
					className();
					}
					} 
				}
				setState(842);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralContext extends GroovyParserRuleContext {
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
	 
		public LiteralContext() { }
		public void copyFrom(LiteralContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class IntegerLiteralAltContext extends LiteralContext {
		public TerminalNode IntegerLiteral() { return getToken(GroovyParser.IntegerLiteral, 0); }
		public IntegerLiteralAltContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitIntegerLiteralAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FloatingPointLiteralAltContext extends LiteralContext {
		public TerminalNode FloatingPointLiteral() { return getToken(GroovyParser.FloatingPointLiteral, 0); }
		public FloatingPointLiteralAltContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitFloatingPointLiteralAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StringLiteralAltContext extends LiteralContext {
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public StringLiteralAltContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitStringLiteralAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanLiteralAltContext extends LiteralContext {
		public TerminalNode BooleanLiteral() { return getToken(GroovyParser.BooleanLiteral, 0); }
		public BooleanLiteralAltContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBooleanLiteralAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NullLiteralAltContext extends LiteralContext {
		public TerminalNode NullLiteral() { return getToken(GroovyParser.NullLiteral, 0); }
		public NullLiteralAltContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitNullLiteralAlt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_literal);
		try {
			setState(848);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IntegerLiteral:
				_localctx = new IntegerLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(843);
				match(IntegerLiteral);
				}
				break;
			case FloatingPointLiteral:
				_localctx = new FloatingPointLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(844);
				match(FloatingPointLiteral);
				}
				break;
			case StringLiteral:
				_localctx = new StringLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(845);
				stringLiteral();
				}
				break;
			case BooleanLiteral:
				_localctx = new BooleanLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(846);
				match(BooleanLiteral);
				}
				break;
			case NullLiteral:
				_localctx = new NullLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(847);
				match(NullLiteral);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GstringContext extends GroovyParserRuleContext {
		public TerminalNode GStringBegin() { return getToken(GroovyParser.GStringBegin, 0); }
		public List<? extends GstringValueContext> gstringValue() {
			return getRuleContexts(GstringValueContext.class);
		}
		public GstringValueContext gstringValue(int i) {
			return getRuleContext(GstringValueContext.class,i);
		}
		public TerminalNode GStringEnd() { return getToken(GroovyParser.GStringEnd, 0); }
		public List<? extends TerminalNode> GStringPart() { return getTokens(GroovyParser.GStringPart); }
		public TerminalNode GStringPart(int i) {
			return getToken(GroovyParser.GStringPart, i);
		}
		public GstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gstring; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitGstring(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final GstringContext gstring() throws RecognitionException {
		GstringContext _localctx = new GstringContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_gstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(850);
			match(GStringBegin);
			setState(851);
			gstringValue();
			setState(856);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPart) {
				{
				{
				setState(852);
				match(GStringPart);
				setState(853);
				gstringValue();
				}
				}
				setState(858);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(859);
			match(GStringEnd);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GstringValueContext extends GroovyParserRuleContext {
		public GstringPathContext gstringPath() {
			return getRuleContext(GstringPathContext.class,0);
		}
		public ClosureContext closure() {
			return getRuleContext(ClosureContext.class,0);
		}
		public GstringValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gstringValue; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitGstringValue(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final GstringValueContext gstringValue() throws RecognitionException {
		GstringValueContext _localctx = new GstringValueContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_gstringValue);
		try {
			setState(863);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(861);
				gstringPath();
				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 2);
				{
				setState(862);
				closure();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GstringPathContext extends GroovyParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<? extends TerminalNode> GStringPathPart() { return getTokens(GroovyParser.GStringPathPart); }
		public TerminalNode GStringPathPart(int i) {
			return getToken(GroovyParser.GStringPathPart, i);
		}
		public GstringPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gstringPath; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitGstringPath(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final GstringPathContext gstringPath() throws RecognitionException {
		GstringPathContext _localctx = new GstringPathContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_gstringPath);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(865);
			identifier();
			setState(869);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPathPart) {
				{
				{
				setState(866);
				match(GStringPathPart);
				}
				}
				setState(871);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final StandardLambdaExpressionContext lambdaExpression() throws RecognitionException {
		StandardLambdaExpressionContext _localctx = new StandardLambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_lambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(872);
			lambdaParameters();
			setState(873);
			nls();
			setState(874);
			match(ARROW);
			setState(875);
			nls();
			setState(876);
			lambdaBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StandardLambdaExpressionContext extends GroovyParserRuleContext {
		public StandardLambdaParametersContext standardLambdaParameters() {
			return getRuleContext(StandardLambdaParametersContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode ARROW() { return getToken(GroovyParser.ARROW, 0); }
		public LambdaBodyContext lambdaBody() {
			return getRuleContext(LambdaBodyContext.class,0);
		}
		public StandardLambdaExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standardLambdaExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitStandardLambdaExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final StandardLambdaExpressionContext standardLambdaExpression() throws RecognitionException {
		StandardLambdaExpressionContext _localctx = new StandardLambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_standardLambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(878);
			standardLambdaParameters();
			setState(879);
			nls();
			setState(880);
			match(ARROW);
			setState(881);
			nls();
			setState(882);
			lambdaBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final StandardLambdaParametersContext lambdaParameters() throws RecognitionException {
		StandardLambdaParametersContext _localctx = new StandardLambdaParametersContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_lambdaParameters);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(884);
			formalParameters();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StandardLambdaParametersContext extends GroovyParserRuleContext {
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public StandardLambdaParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standardLambdaParameters; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitStandardLambdaParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final StandardLambdaParametersContext standardLambdaParameters() throws RecognitionException {
		StandardLambdaParametersContext _localctx = new StandardLambdaParametersContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_standardLambdaParameters);
		try {
			setState(888);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(886);
				formalParameters();
				}
				break;
			case AS:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 2);
				{
				setState(887);
				variableDeclaratorId();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LambdaBodyContext extends GroovyParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public StatementExpressionContext statementExpression() {
			return getRuleContext(StatementExpressionContext.class,0);
		}
		public LambdaBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaBody; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitLambdaBody(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final LambdaBodyContext lambdaBody() throws RecognitionException {
		LambdaBodyContext _localctx = new LambdaBodyContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_lambdaBody);
		try {
			setState(892);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(890);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(891);
				statementExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClosureContext extends GroovyParserRuleContext {
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public BlockStatementsOptContext blockStatementsOpt() {
			return getRuleContext(BlockStatementsOptContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode ARROW() { return getToken(GroovyParser.ARROW, 0); }
		public SepContext sep() {
			return getRuleContext(SepContext.class,0);
		}
		public FormalParameterListContext formalParameterList() {
			return getRuleContext(FormalParameterListContext.class,0);
		}
		public ClosureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_closure; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClosure(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClosureContext closure() throws RecognitionException {
		ClosureContext _localctx = new ClosureContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_closure);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(894);
			match(LBRACE);
			setState(903);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				{
				setState(895);
				nls();
				setState(899);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (ABSTRACT - 7)) | (1L << (FINAL - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (PRIVATE - 7)) | (1L << (PROTECTED - 7)) | (1L << (PUBLIC - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (STATIC - 7)) | (1L << (STRICTFP - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (YIELD - 7)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)) | (1L << (ELLIPSIS - 136)))) != 0)) {
					{
					setState(896);
					formalParameterList();
					setState(897);
					nls();
					}
				}

				setState(901);
				match(ARROW);
				}
				break;
			}
			setState(906);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				{
				setState(905);
				sep();
				}
				break;
			}
			setState(908);
			blockStatementsOpt();
			setState(909);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClosureOrLambdaExpressionContext extends GroovyParserRuleContext {
		public ClosureContext closure() {
			return getRuleContext(ClosureContext.class,0);
		}
		public StandardLambdaExpressionContext standardLambdaExpression() {
			return getRuleContext(StandardLambdaExpressionContext.class,0);
		}
		public ClosureOrLambdaExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_closureOrLambdaExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClosureOrLambdaExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClosureOrLambdaExpressionContext closureOrLambdaExpression() throws RecognitionException {
		ClosureOrLambdaExpressionContext _localctx = new ClosureOrLambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_closureOrLambdaExpression);
		try {
			setState(913);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(911);
				closure();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(912);
				lambdaExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlockStatementsOptContext extends GroovyParserRuleContext {
		public BlockStatementsContext blockStatements() {
			return getRuleContext(BlockStatementsContext.class,0);
		}
		public BlockStatementsOptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockStatementsOpt; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBlockStatementsOpt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final BlockStatementsOptContext blockStatementsOpt() throws RecognitionException {
		BlockStatementsOptContext _localctx = new BlockStatementsOptContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_blockStatementsOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(916);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
			case 1:
				{
				setState(915);
				blockStatements();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlockStatementsContext extends GroovyParserRuleContext {
		public List<? extends BlockStatementContext> blockStatement() {
			return getRuleContexts(BlockStatementContext.class);
		}
		public BlockStatementContext blockStatement(int i) {
			return getRuleContext(BlockStatementContext.class,i);
		}
		public List<? extends SepContext> sep() {
			return getRuleContexts(SepContext.class);
		}
		public SepContext sep(int i) {
			return getRuleContext(SepContext.class,i);
		}
		public BlockStatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockStatements; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBlockStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final BlockStatementsContext blockStatements() throws RecognitionException {
		BlockStatementsContext _localctx = new BlockStatementsContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_blockStatements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(918);
			blockStatement();
			setState(924);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(919);
					sep();
					setState(920);
					blockStatement();
					}
					} 
				}
				setState(926);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
			}
			setState(928);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				{
				setState(927);
				sep();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnnotationsOptContext extends GroovyParserRuleContext {
		public List<? extends AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public AnnotationsOptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationsOpt; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAnnotationsOpt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final AnnotationsOptContext annotationsOpt() throws RecognitionException {
		AnnotationsOptContext _localctx = new AnnotationsOptContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_annotationsOpt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(941);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT) {
				{
				setState(930);
				annotation();
				setState(936);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(931);
						nls();
						setState(932);
						annotation();
						}
						} 
					}
					setState(938);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
				}
				setState(939);
				nls();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnnotationContext extends GroovyParserRuleContext {
		public TerminalNode AT() { return getToken(GroovyParser.AT, 0); }
		public AnnotationNameContext annotationName() {
			return getRuleContext(AnnotationNameContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public ElementValuesContext elementValues() {
			return getRuleContext(ElementValuesContext.class,0);
		}
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAnnotation(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_annotation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(943);
			match(AT);
			setState(944);
			annotationName();
			setState(952);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
			case 1:
				{
				setState(945);
				nls();
				setState(946);
				match(LPAREN);
				setState(948);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
				case 1:
					{
					setState(947);
					elementValues();
					}
					break;
				}
				setState(950);
				match(RPAREN);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementValuesContext extends GroovyParserRuleContext {
		public ElementValuePairsContext elementValuePairs() {
			return getRuleContext(ElementValuePairsContext.class,0);
		}
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public ElementValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValues; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitElementValues(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ElementValuesContext elementValues() throws RecognitionException {
		ElementValuesContext _localctx = new ElementValuesContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_elementValues);
		try {
			setState(956);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(954);
				elementValuePairs();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(955);
				elementValue();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnnotationNameContext extends GroovyParserRuleContext {
		public QualifiedClassNameContext qualifiedClassName() {
			return getRuleContext(QualifiedClassNameContext.class,0);
		}
		public AnnotationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAnnotationName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final AnnotationNameContext annotationName() throws RecognitionException {
		AnnotationNameContext _localctx = new AnnotationNameContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_annotationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(958);
			qualifiedClassName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementValuePairsContext extends GroovyParserRuleContext {
		public List<? extends ElementValuePairContext> elementValuePair() {
			return getRuleContexts(ElementValuePairContext.class);
		}
		public ElementValuePairContext elementValuePair(int i) {
			return getRuleContext(ElementValuePairContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public ElementValuePairsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePairs; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitElementValuePairs(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ElementValuePairsContext elementValuePairs() throws RecognitionException {
		ElementValuePairsContext _localctx = new ElementValuePairsContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_elementValuePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(960);
			elementValuePair();
			setState(965);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(961);
				match(COMMA);
				setState(962);
				elementValuePair();
				}
				}
				setState(967);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementValuePairContext extends GroovyParserRuleContext {
		public ElementValuePairNameContext elementValuePairName() {
			return getRuleContext(ElementValuePairNameContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public ElementValuePairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePair; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitElementValuePair(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ElementValuePairContext elementValuePair() throws RecognitionException {
		ElementValuePairContext _localctx = new ElementValuePairContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(968);
			elementValuePairName();
			setState(969);
			nls();
			setState(970);
			match(ASSIGN);
			setState(971);
			nls();
			setState(972);
			elementValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementValuePairNameContext extends GroovyParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public KeywordsContext keywords() {
			return getRuleContext(KeywordsContext.class,0);
		}
		public ElementValuePairNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePairName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitElementValuePairName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ElementValuePairNameContext elementValuePairName() throws RecognitionException {
		ElementValuePairNameContext _localctx = new ElementValuePairNameContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_elementValuePairName);
		try {
			setState(976);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(974);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(975);
				keywords();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementValueContext extends GroovyParserRuleContext {
		public ElementValueArrayInitializerContext elementValueArrayInitializer() {
			return getRuleContext(ElementValueArrayInitializerContext.class,0);
		}
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ElementValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValue; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitElementValue(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ElementValueContext elementValue() throws RecognitionException {
		ElementValueContext _localctx = new ElementValueContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_elementValue);
		try {
			setState(981);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(978);
				elementValueArrayInitializer();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(979);
				annotation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(980);
				expression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementValueArrayInitializerContext extends GroovyParserRuleContext {
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public List<? extends ElementValueContext> elementValue() {
			return getRuleContexts(ElementValueContext.class);
		}
		public ElementValueContext elementValue(int i) {
			return getRuleContext(ElementValueContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public ElementValueArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValueArrayInitializer; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitElementValueArrayInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ElementValueArrayInitializerContext elementValueArrayInitializer() throws RecognitionException {
		ElementValueArrayInitializerContext _localctx = new ElementValueArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_elementValueArrayInitializer);
		int _la;
		try {
			int _alt;
			setState(1011);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACK:
				enterOuterAlt(_localctx, 1);
				{
				setState(983);
				match(LBRACK);
				setState(995);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
				case 1:
					{
					setState(984);
					elementValue();
					setState(989);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,95,_ctx);
					while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(985);
							match(COMMA);
							setState(986);
							elementValue();
							}
							} 
						}
						setState(991);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,95,_ctx);
					}
					setState(993);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(992);
						match(COMMA);
						}
					}

					}
					break;
				}
				setState(997);
				match(RBRACK);
				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 2);
				{
				setState(998);
				match(LBRACE);
				setState(1002); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(999);
						elementValue();
						setState(1000);
						match(COMMA);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1004); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,98,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1007);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
				case 1:
					{
					setState(1006);
					elementValue();
					}
					break;
				}
				setState(1009);
				match(RBRACE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlockContext extends GroovyParserRuleContext {
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public BlockStatementsOptContext blockStatementsOpt() {
			return getRuleContext(BlockStatementsOptContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public SepContext sep() {
			return getRuleContext(SepContext.class,0);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1013);
			match(LBRACE);
			setState(1015);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
			case 1:
				{
				setState(1014);
				sep();
				}
				break;
			}
			setState(1017);
			blockStatementsOpt();
			setState(1018);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlockStatementContext extends GroovyParserRuleContext {
		public LocalVariableDeclarationContext localVariableDeclaration() {
			return getRuleContext(LocalVariableDeclarationContext.class,0);
		}
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public BlockStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBlockStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final BlockStatementContext blockStatement() throws RecognitionException {
		BlockStatementContext _localctx = new BlockStatementContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_blockStatement);
		try {
			setState(1022);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1020);
				localVariableDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1021);
				statement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LocalVariableDeclarationContext extends GroovyParserRuleContext {
		public VariableDeclarationContext variableDeclaration() {
			return getRuleContext(VariableDeclarationContext.class,0);
		}
		public LocalVariableDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localVariableDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitLocalVariableDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final LocalVariableDeclarationContext localVariableDeclaration() throws RecognitionException {
		LocalVariableDeclarationContext _localctx = new LocalVariableDeclarationContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_localVariableDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1024);
			if (!( !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ");
			setState(1025);
			variableDeclaration(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDeclarationContext extends GroovyParserRuleContext {
		public int t;
		public ModifiersContext modifiers() {
			return getRuleContext(ModifiersContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public VariableDeclaratorsContext variableDeclarators() {
			return getRuleContext(VariableDeclaratorsContext.class,0);
		}
		public TypeNamePairsContext typeNamePairs() {
			return getRuleContext(TypeNamePairsContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public VariableInitializerContext variableInitializer() {
			return getRuleContext(VariableInitializerContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public VariableDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public VariableDeclarationContext(ParserRuleContext parent, int invokingState, int t) {
			super(parent, invokingState);
			this.t = t;
		}
		@Override public int getRuleIndex() { return RULE_variableDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableDeclarationContext variableDeclaration(int t) throws RecognitionException {
		VariableDeclarationContext _localctx = new VariableDeclarationContext(_ctx, getState(), t);
		enterRule(_localctx, 158, RULE_variableDeclaration);
		try {
			setState(1044);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1027);
				modifiers();
				setState(1028);
				nls();
				setState(1039);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case AS:
				case DEF:
				case IN:
				case TRAIT:
				case ASYNC:
				case AWAIT:
				case DEFER:
				case BuiltInPrimitiveType:
				case MODULE:
				case PERMITS:
				case RECORD:
				case SEALED:
				case VAL:
				case VAR:
				case VOID:
				case YIELD:
				case CapitalizedIdentifier:
				case Identifier:
				case AT:
					{
					setState(1030);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,103,_ctx) ) {
					case 1:
						{
						setState(1029);
						type();
						}
						break;
					}
					setState(1032);
					variableDeclarators();
					}
					break;
				case LPAREN:
					{
					setState(1033);
					typeNamePairs();
					setState(1034);
					nls();
					setState(1035);
					match(ASSIGN);
					setState(1036);
					nls();
					setState(1037);
					variableInitializer();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1041);
				type();
				setState(1042);
				variableDeclarators();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeNamePairsContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public List<? extends TypeNamePairContext> typeNamePair() {
			return getRuleContexts(TypeNamePairContext.class);
		}
		public TypeNamePairContext typeNamePair(int i) {
			return getRuleContext(TypeNamePairContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends KeyedPairContext> keyedPair() {
			return getRuleContexts(KeyedPairContext.class);
		}
		public KeyedPairContext keyedPair(int i) {
			return getRuleContext(KeyedPairContext.class,i);
		}
		public TypeNamePairsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeNamePairs; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeNamePairs(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeNamePairsContext typeNamePairs() throws RecognitionException {
		TypeNamePairsContext _localctx = new TypeNamePairsContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_typeNamePairs);
		int _la;
		try {
			setState(1068);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,108,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1046);
				match(LPAREN);
				setState(1047);
				typeNamePair();
				setState(1052);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1048);
					match(COMMA);
					setState(1049);
					typeNamePair();
					}
					}
					setState(1054);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1055);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1057);
				match(LPAREN);
				setState(1058);
				keyedPair();
				setState(1063);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1059);
					match(COMMA);
					setState(1060);
					keyedPair();
					}
					}
					setState(1065);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1066);
				match(RPAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeNamePairContext extends GroovyParserRuleContext {
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public TerminalNode DEF() { return getToken(GroovyParser.DEF, 0); }
		public TerminalNode VAL() { return getToken(GroovyParser.VAL, 0); }
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode MUL() { return getToken(GroovyParser.MUL, 0); }
		public TypeNamePairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeNamePair; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeNamePair(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeNamePairContext typeNamePair() throws RecognitionException {
		TypeNamePairContext _localctx = new TypeNamePairContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_typeNamePair);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1074);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
			case 1:
				{
				setState(1070);
				match(DEF);
				}
				break;
			case 2:
				{
				setState(1071);
				match(VAL);
				}
				break;
			case 3:
				{
				setState(1072);
				match(VAR);
				}
				break;
			case 4:
				{
				setState(1073);
				type();
				}
				break;
			}
			setState(1077);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MUL) {
				{
				setState(1076);
				match(MUL);
				}
			}

			setState(1079);
			variableDeclaratorId();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyedPairContext extends GroovyParserRuleContext {
		public IdentifierContext key;
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode DEF() { return getToken(GroovyParser.DEF, 0); }
		public TerminalNode VAL() { return getToken(GroovyParser.VAL, 0); }
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public KeyedPairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyedPair; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitKeyedPair(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final KeyedPairContext keyedPair() throws RecognitionException {
		KeyedPairContext _localctx = new KeyedPairContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_keyedPair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1081);
			_localctx.key = identifier();
			setState(1082);
			match(COLON);
			setState(1087);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
			case 1:
				{
				setState(1083);
				match(DEF);
				}
				break;
			case 2:
				{
				setState(1084);
				match(VAL);
				}
				break;
			case 3:
				{
				setState(1085);
				match(VAR);
				}
				break;
			case 4:
				{
				setState(1086);
				type();
				}
				break;
			}
			setState(1089);
			variableDeclaratorId();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableNamesContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public List<? extends VariableDeclaratorIdContext> variableDeclaratorId() {
			return getRuleContexts(VariableDeclaratorIdContext.class);
		}
		public VariableDeclaratorIdContext variableDeclaratorId(int i) {
			return getRuleContext(VariableDeclaratorIdContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public VariableNamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableNames; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableNames(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableNamesContext variableNames() throws RecognitionException {
		VariableNamesContext _localctx = new VariableNamesContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_variableNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1091);
			match(LPAREN);
			setState(1092);
			variableDeclaratorId();
			setState(1095); 
			_errHandler.sync(this);
			do {
				{
				{
				setState(1093);
				match(COMMA);
				setState(1094);
				variableDeclaratorId();
				}
				}
				setState(1097); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==COMMA );
			setState(1099);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionalStatementContext extends GroovyParserRuleContext {
		public IfElseStatementContext ifElseStatement() {
			return getRuleContext(IfElseStatementContext.class,0);
		}
		public SwitchStatementContext switchStatement() {
			return getRuleContext(SwitchStatementContext.class,0);
		}
		public ConditionalStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionalStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitConditionalStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ConditionalStatementContext conditionalStatement() throws RecognitionException {
		ConditionalStatementContext _localctx = new ConditionalStatementContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_conditionalStatement);
		try {
			setState(1103);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IF:
				enterOuterAlt(_localctx, 1);
				{
				setState(1101);
				ifElseStatement();
				}
				break;
			case SWITCH:
				enterOuterAlt(_localctx, 2);
				{
				setState(1102);
				switchStatement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IfElseStatementContext extends GroovyParserRuleContext {
		public StatementContext tb;
		public StatementContext fb;
		public TerminalNode IF() { return getToken(GroovyParser.IF, 0); }
		public ExpressionInParContext expressionInPar() {
			return getRuleContext(ExpressionInParContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(GroovyParser.ELSE, 0); }
		public SepContext sep() {
			return getRuleContext(SepContext.class,0);
		}
		public IfElseStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifElseStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitIfElseStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final IfElseStatementContext ifElseStatement() throws RecognitionException {
		IfElseStatementContext _localctx = new IfElseStatementContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_ifElseStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1105);
			match(IF);
			setState(1106);
			expressionInPar();
			setState(1107);
			nls();
			setState(1108);
			_localctx.tb = statement();
			setState(1117);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				{
				setState(1111);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
				case 1:
					{
					setState(1109);
					nls();
					}
					break;
				case 2:
					{
					setState(1110);
					sep();
					}
					break;
				}
				setState(1113);
				match(ELSE);
				setState(1114);
				nls();
				setState(1115);
				_localctx.fb = statement();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SwitchStatementContext extends GroovyParserRuleContext {
		public TerminalNode SWITCH() { return getToken(GroovyParser.SWITCH, 0); }
		public ExpressionInParContext expressionInPar() {
			return getRuleContext(ExpressionInParContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public List<? extends SwitchBlockStatementGroupContext> switchBlockStatementGroup() {
			return getRuleContexts(SwitchBlockStatementGroupContext.class);
		}
		public SwitchBlockStatementGroupContext switchBlockStatementGroup(int i) {
			return getRuleContext(SwitchBlockStatementGroupContext.class,i);
		}
		public SwitchStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSwitchStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final SwitchStatementContext switchStatement() throws RecognitionException {
		SwitchStatementContext _localctx = new SwitchStatementContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_switchStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1119);
			match(SWITCH);
			setState(1120);
			expressionInPar();
			setState(1121);
			nls();
			setState(1122);
			match(LBRACE);
			setState(1123);
			nls();
			setState(1131);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CASE || _la==DEFAULT) {
				{
				setState(1125); 
				_errHandler.sync(this);
				do {
					{
					{
					setState(1124);
					switchBlockStatementGroup();
					}
					}
					setState(1127); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==CASE || _la==DEFAULT );
				setState(1129);
				nls();
				}
			}

			setState(1133);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LoopStatementContext extends GroovyParserRuleContext {
		public LoopStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopStatement; }
	 
		public LoopStatementContext() { }
		public void copyFrom(LoopStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ForStmtAltContext extends LoopStatementContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode FOR() { return getToken(GroovyParser.FOR, 0); }
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public ForControlContext forControl() {
			return getRuleContext(ForControlContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode AWAIT() { return getToken(GroovyParser.AWAIT, 0); }
		public ForStmtAltContext(LoopStatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitForStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class WhileStmtAltContext extends LoopStatementContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode WHILE() { return getToken(GroovyParser.WHILE, 0); }
		public ExpressionInParContext expressionInPar() {
			return getRuleContext(ExpressionInParContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public WhileStmtAltContext(LoopStatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitWhileStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DoWhileStmtAltContext extends LoopStatementContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode DO() { return getToken(GroovyParser.DO, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode WHILE() { return getToken(GroovyParser.WHILE, 0); }
		public ExpressionInParContext expressionInPar() {
			return getRuleContext(ExpressionInParContext.class,0);
		}
		public DoWhileStmtAltContext(LoopStatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitDoWhileStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final LoopStatementContext loopStatement() throws RecognitionException {
		LoopStatementContext _localctx = new LoopStatementContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_loopStatement);
		int _la;
		try {
			setState(1160);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,119,_ctx) ) {
			case 1:
				_localctx = new ForStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1135);
				annotationsOpt();
				setState(1136);
				match(FOR);
				setState(1138);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AWAIT) {
					{
					setState(1137);
					match(AWAIT);
					}
				}

				setState(1140);
				match(LPAREN);
				setState(1141);
				forControl();
				setState(1142);
				match(RPAREN);
				setState(1143);
				nls();
				setState(1144);
				statement();
				}
				break;
			case 2:
				_localctx = new WhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1146);
				annotationsOpt();
				setState(1147);
				match(WHILE);
				setState(1148);
				expressionInPar();
				setState(1149);
				nls();
				setState(1150);
				statement();
				}
				break;
			case 3:
				_localctx = new DoWhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1152);
				annotationsOpt();
				setState(1153);
				match(DO);
				setState(1154);
				nls();
				setState(1155);
				statement();
				setState(1156);
				nls();
				setState(1157);
				match(WHILE);
				setState(1158);
				expressionInPar();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ContinueStatementContext extends GroovyParserRuleContext {
		public TerminalNode CONTINUE() { return getToken(GroovyParser.CONTINUE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ContinueStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_continueStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitContinueStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ContinueStatementContext continueStatement() throws RecognitionException {
		ContinueStatementContext _localctx = new ContinueStatementContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_continueStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1162);
			match(CONTINUE);
			setState(1164);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(1163);
				identifier();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BreakStatementContext extends GroovyParserRuleContext {
		public TerminalNode BREAK() { return getToken(GroovyParser.BREAK, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public BreakStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_breakStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBreakStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final BreakStatementContext breakStatement() throws RecognitionException {
		BreakStatementContext _localctx = new BreakStatementContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_breakStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1166);
			match(BREAK);
			setState(1168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(1167);
				identifier();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class YieldStatementContext extends GroovyParserRuleContext {
		public TerminalNode YIELD() { return getToken(GroovyParser.YIELD, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public YieldStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yieldStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitYieldStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final YieldStatementContext yieldStatement() throws RecognitionException {
		YieldStatementContext _localctx = new YieldStatementContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_yieldStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1170);
			match(YIELD);
			setState(1171);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TryCatchStatementContext extends GroovyParserRuleContext {
		public TerminalNode TRY() { return getToken(GroovyParser.TRY, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ResourcesContext resources() {
			return getRuleContext(ResourcesContext.class,0);
		}
		public List<? extends CatchClauseContext> catchClause() {
			return getRuleContexts(CatchClauseContext.class);
		}
		public CatchClauseContext catchClause(int i) {
			return getRuleContext(CatchClauseContext.class,i);
		}
		public FinallyBlockContext finallyBlock() {
			return getRuleContext(FinallyBlockContext.class,0);
		}
		public TryCatchStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tryCatchStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTryCatchStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TryCatchStatementContext tryCatchStatement() throws RecognitionException {
		TryCatchStatementContext _localctx = new TryCatchStatementContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_tryCatchStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1173);
			match(TRY);
			setState(1175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(1174);
				resources();
				}
			}

			setState(1177);
			nls();
			setState(1178);
			block();
			setState(1184);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1179);
					nls();
					setState(1180);
					catchClause();
					}
					} 
				}
				setState(1186);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			}
			setState(1190);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,124,_ctx) ) {
			case 1:
				{
				setState(1187);
				nls();
				setState(1188);
				finallyBlock();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssertStatementContext extends GroovyParserRuleContext {
		public ExpressionContext ce;
		public ExpressionContext me;
		public TerminalNode ASSERT() { return getToken(GroovyParser.ASSERT, 0); }
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode COMMA() { return getToken(GroovyParser.COMMA, 0); }
		public AssertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assertStatement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAssertStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final AssertStatementContext assertStatement() throws RecognitionException {
		AssertStatementContext _localctx = new AssertStatementContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_assertStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1192);
			match(ASSERT);
			setState(1193);
			_localctx.ce = expression(0);
			setState(1199);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
			case 1:
				{
				setState(1194);
				nls();
				setState(1195);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==COLON) ) {
				_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1196);
				nls();
				setState(1197);
				_localctx.me = expression(0);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends GroovyParserRuleContext {
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
	 
		public StatementContext() { }
		public void copyFrom(StatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class BlockStmtAltContext extends StatementContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public BlockStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBlockStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ConditionalStmtAltContext extends StatementContext {
		public ConditionalStatementContext conditionalStatement() {
			return getRuleContext(ConditionalStatementContext.class,0);
		}
		public ConditionalStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitConditionalStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LoopStmtAltContext extends StatementContext {
		public LoopStatementContext loopStatement() {
			return getRuleContext(LoopStatementContext.class,0);
		}
		public LoopStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitLoopStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TryCatchStmtAltContext extends StatementContext {
		public TryCatchStatementContext tryCatchStatement() {
			return getRuleContext(TryCatchStatementContext.class,0);
		}
		public TryCatchStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTryCatchStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SynchronizedStmtAltContext extends StatementContext {
		public TerminalNode SYNCHRONIZED() { return getToken(GroovyParser.SYNCHRONIZED, 0); }
		public ExpressionInParContext expressionInPar() {
			return getRuleContext(ExpressionInParContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public SynchronizedStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSynchronizedStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ReturnStmtAltContext extends StatementContext {
		public TerminalNode RETURN() { return getToken(GroovyParser.RETURN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ReturnStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitReturnStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ThrowStmtAltContext extends StatementContext {
		public TerminalNode THROW() { return getToken(GroovyParser.THROW, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ThrowStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitThrowStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BreakStmtAltContext extends StatementContext {
		public BreakStatementContext breakStatement() {
			return getRuleContext(BreakStatementContext.class,0);
		}
		public BreakStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBreakStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ContinueStmtAltContext extends StatementContext {
		public ContinueStatementContext continueStatement() {
			return getRuleContext(ContinueStatementContext.class,0);
		}
		public ContinueStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitContinueStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YieldStmtAltContext extends StatementContext {
		public YieldStatementContext yieldStatement() {
			return getRuleContext(YieldStatementContext.class,0);
		}
		public YieldStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitYieldStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YieldReturnStmtAltContext extends StatementContext {
		public TerminalNode YIELD() { return getToken(GroovyParser.YIELD, 0); }
		public TerminalNode RETURN() { return getToken(GroovyParser.RETURN, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public YieldReturnStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitYieldReturnStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DeferStmtAltContext extends StatementContext {
		public TerminalNode DEFER() { return getToken(GroovyParser.DEFER, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public StatementExpressionContext statementExpression() {
			return getRuleContext(StatementExpressionContext.class,0);
		}
		public DeferStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitDeferStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LabeledStmtAltContext extends StatementContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public LabeledStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitLabeledStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AssertStmtAltContext extends StatementContext {
		public AssertStatementContext assertStatement() {
			return getRuleContext(AssertStatementContext.class,0);
		}
		public AssertStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAssertStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LocalVariableDeclarationStmtAltContext extends StatementContext {
		public LocalVariableDeclarationContext localVariableDeclaration() {
			return getRuleContext(LocalVariableDeclarationContext.class,0);
		}
		public LocalVariableDeclarationStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitLocalVariableDeclarationStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExpressionStmtAltContext extends StatementContext {
		public StatementExpressionContext statementExpression() {
			return getRuleContext(StatementExpressionContext.class,0);
		}
		public ExpressionStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitExpressionStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class EmptyStmtAltContext extends StatementContext {
		public TerminalNode SEMI() { return getToken(GroovyParser.SEMI, 0); }
		public EmptyStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEmptyStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_statement);
		try {
			setState(1238);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,127,_ctx) ) {
			case 1:
				_localctx = new BlockStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1201);
				block();
				}
				break;
			case 2:
				_localctx = new ConditionalStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1202);
				conditionalStatement();
				}
				break;
			case 3:
				_localctx = new LoopStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1203);
				loopStatement();
				}
				break;
			case 4:
				_localctx = new TryCatchStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1204);
				tryCatchStatement();
				}
				break;
			case 5:
				_localctx = new SynchronizedStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1205);
				match(SYNCHRONIZED);
				setState(1206);
				expressionInPar();
				setState(1207);
				nls();
				setState(1208);
				block();
				}
				break;
			case 6:
				_localctx = new ReturnStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1210);
				match(RETURN);
				setState(1212);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
				case 1:
					{
					setState(1211);
					expression(0);
					}
					break;
				}
				}
				break;
			case 7:
				_localctx = new ThrowStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1214);
				match(THROW);
				setState(1215);
				expression(0);
				}
				break;
			case 8:
				_localctx = new BreakStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1216);
				breakStatement();
				}
				break;
			case 9:
				_localctx = new ContinueStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1217);
				continueStatement();
				}
				break;
			case 10:
				_localctx = new YieldStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1218);
				if (!( inSwitchExpressionLevel > 0 )) throw createFailedPredicateException(" inSwitchExpressionLevel > 0 ");
				setState(1219);
				yieldStatement();
				}
				break;
			case 11:
				_localctx = new YieldReturnStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1220);
				match(YIELD);
				setState(1221);
				match(RETURN);
				setState(1222);
				nls();
				setState(1223);
				expression(0);
				}
				break;
			case 12:
				_localctx = new DeferStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(1225);
				match(DEFER);
				setState(1226);
				nls();
				setState(1227);
				statementExpression();
				}
				break;
			case 13:
				_localctx = new LabeledStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(1229);
				identifier();
				setState(1230);
				match(COLON);
				setState(1231);
				nls();
				setState(1232);
				statement();
				}
				break;
			case 14:
				_localctx = new AssertStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(1234);
				assertStatement();
				}
				break;
			case 15:
				_localctx = new LocalVariableDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(1235);
				localVariableDeclaration();
				}
				break;
			case 16:
				_localctx = new ExpressionStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(1236);
				statementExpression();
				}
				break;
			case 17:
				_localctx = new EmptyStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(1237);
				match(SEMI);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CatchClauseContext extends GroovyParserRuleContext {
		public TerminalNode CATCH() { return getToken(GroovyParser.CATCH, 0); }
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public VariableModifiersOptContext variableModifiersOpt() {
			return getRuleContext(VariableModifiersOptContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public CatchTypeContext catchType() {
			return getRuleContext(CatchTypeContext.class,0);
		}
		public CatchClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchClause; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCatchClause(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CatchClauseContext catchClause() throws RecognitionException {
		CatchClauseContext _localctx = new CatchClauseContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_catchClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1240);
			match(CATCH);
			setState(1241);
			match(LPAREN);
			setState(1242);
			variableModifiersOpt();
			setState(1244);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
			case 1:
				{
				setState(1243);
				catchType();
				}
				break;
			}
			setState(1246);
			identifier();
			setState(1247);
			match(RPAREN);
			setState(1248);
			nls();
			setState(1249);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CatchTypeContext extends GroovyParserRuleContext {
		public List<? extends QualifiedClassNameContext> qualifiedClassName() {
			return getRuleContexts(QualifiedClassNameContext.class);
		}
		public QualifiedClassNameContext qualifiedClassName(int i) {
			return getRuleContext(QualifiedClassNameContext.class,i);
		}
		public List<? extends TerminalNode> BITOR() { return getTokens(GroovyParser.BITOR); }
		public TerminalNode BITOR(int i) {
			return getToken(GroovyParser.BITOR, i);
		}
		public CatchTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCatchType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CatchTypeContext catchType() throws RecognitionException {
		CatchTypeContext _localctx = new CatchTypeContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_catchType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1251);
			qualifiedClassName();
			setState(1256);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITOR) {
				{
				{
				setState(1252);
				match(BITOR);
				setState(1253);
				qualifiedClassName();
				}
				}
				setState(1258);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FinallyBlockContext extends GroovyParserRuleContext {
		public TerminalNode FINALLY() { return getToken(GroovyParser.FINALLY, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public FinallyBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_finallyBlock; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitFinallyBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final FinallyBlockContext finallyBlock() throws RecognitionException {
		FinallyBlockContext _localctx = new FinallyBlockContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_finallyBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1259);
			match(FINALLY);
			setState(1260);
			nls();
			setState(1261);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResourcesContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public ResourceListContext resourceList() {
			return getRuleContext(ResourceListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public SepContext sep() {
			return getRuleContext(SepContext.class,0);
		}
		public ResourcesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resources; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitResources(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ResourcesContext resources() throws RecognitionException {
		ResourcesContext _localctx = new ResourcesContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_resources);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1263);
			match(LPAREN);
			setState(1264);
			nls();
			setState(1265);
			resourceList();
			setState(1267);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(1266);
				sep();
				}
			}

			setState(1269);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResourceListContext extends GroovyParserRuleContext {
		public List<? extends ResourceContext> resource() {
			return getRuleContexts(ResourceContext.class);
		}
		public ResourceContext resource(int i) {
			return getRuleContext(ResourceContext.class,i);
		}
		public List<? extends SepContext> sep() {
			return getRuleContexts(SepContext.class);
		}
		public SepContext sep(int i) {
			return getRuleContext(SepContext.class,i);
		}
		public ResourceListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resourceList; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitResourceList(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ResourceListContext resourceList() throws RecognitionException {
		ResourceListContext _localctx = new ResourceListContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_resourceList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1271);
			resource();
			setState(1277);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,131,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1272);
					sep();
					setState(1273);
					resource();
					}
					} 
				}
				setState(1279);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,131,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResourceContext extends GroovyParserRuleContext {
		public LocalVariableDeclarationContext localVariableDeclaration() {
			return getRuleContext(LocalVariableDeclarationContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_resource);
		try {
			setState(1282);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1280);
				localVariableDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1281);
				expression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SwitchBlockStatementGroupContext extends GroovyParserRuleContext {
		public List<? extends SwitchLabelContext> switchLabel() {
			return getRuleContexts(SwitchLabelContext.class);
		}
		public SwitchLabelContext switchLabel(int i) {
			return getRuleContext(SwitchLabelContext.class,i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public BlockStatementsContext blockStatements() {
			return getRuleContext(BlockStatementsContext.class,0);
		}
		public SwitchBlockStatementGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchBlockStatementGroup; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSwitchBlockStatementGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final SwitchBlockStatementGroupContext switchBlockStatementGroup() throws RecognitionException {
		SwitchBlockStatementGroupContext _localctx = new SwitchBlockStatementGroupContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_switchBlockStatementGroup);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1284);
			switchLabel();
			setState(1290);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1285);
					nls();
					setState(1286);
					switchLabel();
					}
					} 
				}
				setState(1292);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
			}
			setState(1293);
			nls();
			setState(1294);
			blockStatements();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SwitchLabelContext extends GroovyParserRuleContext {
		public TerminalNode CASE() { return getToken(GroovyParser.CASE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode DEFAULT() { return getToken(GroovyParser.DEFAULT, 0); }
		public SwitchLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchLabel; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSwitchLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final SwitchLabelContext switchLabel() throws RecognitionException {
		SwitchLabelContext _localctx = new SwitchLabelContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_switchLabel);
		try {
			setState(1302);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1296);
				match(CASE);
				setState(1297);
				expression(0);
				setState(1298);
				match(COLON);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1300);
				match(DEFAULT);
				setState(1301);
				match(COLON);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForControlContext extends GroovyParserRuleContext {
		public EnhancedForControlContext enhancedForControl() {
			return getRuleContext(EnhancedForControlContext.class,0);
		}
		public OriginalForControlContext originalForControl() {
			return getRuleContext(OriginalForControlContext.class,0);
		}
		public ForControlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forControl; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitForControl(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ForControlContext forControl() throws RecognitionException {
		ForControlContext _localctx = new ForControlContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_forControl);
		try {
			setState(1306);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,135,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1304);
				enhancedForControl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1305);
				originalForControl();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnhancedForControlContext extends GroovyParserRuleContext {
		public VariableModifiersOptContext variableModifiersOpt() {
			return getRuleContext(VariableModifiersOptContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
		public IndexVariableContext indexVariable() {
			return getRuleContext(IndexVariableContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(GroovyParser.COMMA, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public EnhancedForControlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enhancedForControl; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEnhancedForControl(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EnhancedForControlContext enhancedForControl() throws RecognitionException {
		EnhancedForControlContext _localctx = new EnhancedForControlContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_enhancedForControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1311);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,136,_ctx) ) {
			case 1:
				{
				setState(1308);
				indexVariable();
				setState(1309);
				match(COMMA);
				}
				break;
			}
			setState(1313);
			variableModifiersOpt();
			setState(1315);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
			case 1:
				{
				setState(1314);
				type();
				}
				break;
			}
			setState(1317);
			identifier();
			setState(1318);
			_la = _input.LA(1);
			if ( !(_la==IN || _la==COLON) ) {
			_errHandler.recoverInline(this);
			} else {
				if (_input.LA(1) == Token.EOF) {
					matchedEOF = true;
				}

				_errHandler.reportMatch(this);
				consume();
			}
			setState(1319);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexVariableContext extends GroovyParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode BuiltInPrimitiveType() { return getToken(GroovyParser.BuiltInPrimitiveType, 0); }
		public TerminalNode DEF() { return getToken(GroovyParser.DEF, 0); }
		public TerminalNode VAL() { return getToken(GroovyParser.VAL, 0); }
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public IndexVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexVariable; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitIndexVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final IndexVariableContext indexVariable() throws RecognitionException {
		IndexVariableContext _localctx = new IndexVariableContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_indexVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1322);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,138,_ctx) ) {
			case 1:
				{
				setState(1321);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << BuiltInPrimitiveType) | (1L << VAL) | (1L << VAR))) != 0)) ) {
				_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			setState(1324);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OriginalForControlContext extends GroovyParserRuleContext {
		public List<? extends TerminalNode> SEMI() { return getTokens(GroovyParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(GroovyParser.SEMI, i);
		}
		public ForInitContext forInit() {
			return getRuleContext(ForInitContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ForUpdateContext forUpdate() {
			return getRuleContext(ForUpdateContext.class,0);
		}
		public OriginalForControlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_originalForControl; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitOriginalForControl(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final OriginalForControlContext originalForControl() throws RecognitionException {
		OriginalForControlContext _localctx = new OriginalForControlContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_originalForControl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1327);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,139,_ctx) ) {
			case 1:
				{
				setState(1326);
				forInit();
				}
				break;
			}
			setState(1329);
			match(SEMI);
			setState(1331);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,140,_ctx) ) {
			case 1:
				{
				setState(1330);
				expression(0);
				}
				break;
			}
			setState(1333);
			match(SEMI);
			setState(1335);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
			case 1:
				{
				setState(1334);
				forUpdate();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForInitContext extends GroovyParserRuleContext {
		public LocalVariableDeclarationContext localVariableDeclaration() {
			return getRuleContext(LocalVariableDeclarationContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ForInitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forInit; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitForInit(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ForInitContext forInit() throws RecognitionException {
		ForInitContext _localctx = new ForInitContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_forInit);
		try {
			setState(1339);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,142,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1337);
				localVariableDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1338);
				expressionList(false);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForUpdateContext extends GroovyParserRuleContext {
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ForUpdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forUpdate; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitForUpdate(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ForUpdateContext forUpdate() throws RecognitionException {
		ForUpdateContext _localctx = new ForUpdateContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_forUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1341);
			expressionList(false);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CastParExpressionContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public IntersectionTypeContext intersectionType() {
			return getRuleContext(IntersectionTypeContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public CastParExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castParExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCastParExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CastParExpressionContext castParExpression() throws RecognitionException {
		CastParExpressionContext _localctx = new CastParExpressionContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_castParExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1343);
			match(LPAREN);
			setState(1344);
			intersectionType();
			setState(1345);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntersectionTypeContext extends GroovyParserRuleContext {
		public List<? extends TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<? extends TerminalNode> BITAND() { return getTokens(GroovyParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(GroovyParser.BITAND, i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public IntersectionTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intersectionType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitIntersectionType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final IntersectionTypeContext intersectionType() throws RecognitionException {
		IntersectionTypeContext _localctx = new IntersectionTypeContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_intersectionType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1347);
			type();
			setState(1354);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITAND) {
				{
				{
				setState(1348);
				match(BITAND);
				setState(1349);
				nls();
				setState(1350);
				type();
				}
				}
				setState(1356);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CoercionTypeContext extends GroovyParserRuleContext {
		public CastParExpressionContext castParExpression() {
			return getRuleContext(CastParExpressionContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public CoercionTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coercionType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCoercionType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CoercionTypeContext coercionType() throws RecognitionException {
		CoercionTypeContext _localctx = new CoercionTypeContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_coercionType);
		try {
			setState(1359);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1357);
				castParExpression();
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case BuiltInPrimitiveType:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case VOID:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
			case AT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1358);
				type();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParExpressionContext extends GroovyParserRuleContext {
		public ExpressionInParContext expressionInPar() {
			return getRuleContext(ExpressionInParContext.class,0);
		}
		public ParExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitParExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ParExpressionContext parExpression() throws RecognitionException {
		ParExpressionContext _localctx = new ParExpressionContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1361);
			expressionInPar();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionInParContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public EnhancedStatementExpressionContext enhancedStatementExpression() {
			return getRuleContext(EnhancedStatementExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public ExpressionInParContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionInPar; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitExpressionInPar(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ExpressionInParContext expressionInPar() throws RecognitionException {
		ExpressionInParContext _localctx = new ExpressionInParContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_expressionInPar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1363);
			match(LPAREN);
			setState(1364);
			enhancedStatementExpression();
			setState(1365);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionListContext extends GroovyParserRuleContext {
		public boolean canSpread;
		public List<? extends ExpressionListElementContext> expressionListElement() {
			return getRuleContexts(ExpressionListElementContext.class);
		}
		public ExpressionListElementContext expressionListElement(int i) {
			return getRuleContext(ExpressionListElementContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public ExpressionListContext(ParserRuleContext parent, int invokingState, boolean canSpread) {
			super(parent, invokingState);
			this.canSpread = canSpread;
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ExpressionListContext expressionList(boolean canSpread) throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState(), canSpread);
		enterRule(_localctx, 226, RULE_expressionList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1367);
			expressionListElement(_localctx.canSpread);
			setState(1374);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,145,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1368);
					match(COMMA);
					setState(1369);
					nls();
					setState(1370);
					expressionListElement(_localctx.canSpread);
					}
					} 
				}
				setState(1376);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,145,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionListElementContext extends GroovyParserRuleContext {
		public boolean canSpread;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode MUL() { return getToken(GroovyParser.MUL, 0); }
		public ExpressionListElementContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public ExpressionListElementContext(ParserRuleContext parent, int invokingState, boolean canSpread) {
			super(parent, invokingState);
			this.canSpread = canSpread;
		}
		@Override public int getRuleIndex() { return RULE_expressionListElement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitExpressionListElement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ExpressionListElementContext expressionListElement(boolean canSpread) throws RecognitionException {
		ExpressionListElementContext _localctx = new ExpressionListElementContext(_ctx, getState(), canSpread);
		enterRule(_localctx, 228, RULE_expressionListElement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1378);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,146,_ctx) ) {
			case 1:
				{
				setState(1377);
				match(MUL);
				}
				break;
			}
			setState(1380);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnhancedExpressionContext extends GroovyParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StandardLambdaExpressionContext standardLambdaExpression() {
			return getRuleContext(StandardLambdaExpressionContext.class,0);
		}
		public EnhancedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enhancedExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEnhancedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EnhancedExpressionContext enhancedExpression() throws RecognitionException {
		EnhancedExpressionContext _localctx = new EnhancedExpressionContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_enhancedExpression);
		try {
			setState(1384);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,147,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1382);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1383);
				standardLambdaExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnhancedStatementExpressionContext extends GroovyParserRuleContext {
		public StatementExpressionContext statementExpression() {
			return getRuleContext(StatementExpressionContext.class,0);
		}
		public StandardLambdaExpressionContext standardLambdaExpression() {
			return getRuleContext(StandardLambdaExpressionContext.class,0);
		}
		public EnhancedStatementExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enhancedStatementExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEnhancedStatementExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EnhancedStatementExpressionContext enhancedStatementExpression() throws RecognitionException {
		EnhancedStatementExpressionContext _localctx = new EnhancedStatementExpressionContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_enhancedStatementExpression);
		try {
			setState(1388);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1386);
				statementExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1387);
				standardLambdaExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementExpressionContext extends GroovyParserRuleContext {
		public StatementExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statementExpression; }
	 
		public StatementExpressionContext() { }
		public void copyFrom(StatementExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CommandExprAltContext extends StatementExpressionContext {
		public CommandExpressionContext commandExpression() {
			return getRuleContext(CommandExpressionContext.class,0);
		}
		public CommandExprAltContext(StatementExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCommandExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final StatementExpressionContext statementExpression() throws RecognitionException {
		StatementExpressionContext _localctx = new StatementExpressionContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_statementExpression);
		try {
			_localctx = new CommandExprAltContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1390);
			commandExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PostfixExpressionContext extends GroovyParserRuleContext {
		public Token op;
		public PathExpressionContext pathExpression() {
			return getRuleContext(PathExpressionContext.class,0);
		}
		public TerminalNode INC() { return getToken(GroovyParser.INC, 0); }
		public TerminalNode DEC() { return getToken(GroovyParser.DEC, 0); }
		public PostfixExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitPostfixExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final PostfixExpressionContext postfixExpression() throws RecognitionException {
		PostfixExpressionContext _localctx = new PostfixExpressionContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_postfixExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1392);
			pathExpression();
			setState(1394);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,149,_ctx) ) {
			case 1:
				{
				setState(1393);
				_localctx.op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==INC || _la==DEC) ) {
					_localctx.op = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SwitchExpressionContext extends GroovyParserRuleContext {
		public TerminalNode SWITCH() { return getToken(GroovyParser.SWITCH, 0); }
		public ExpressionInParContext expressionInPar() {
			return getRuleContext(ExpressionInParContext.class,0);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public List<? extends SwitchBlockStatementExpressionGroupContext> switchBlockStatementExpressionGroup() {
			return getRuleContexts(SwitchBlockStatementExpressionGroupContext.class);
		}
		public SwitchBlockStatementExpressionGroupContext switchBlockStatementExpressionGroup(int i) {
			return getRuleContext(SwitchBlockStatementExpressionGroupContext.class,i);
		}
		public SwitchExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSwitchExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final SwitchExpressionContext switchExpression() throws RecognitionException {
		SwitchExpressionContext _localctx = new SwitchExpressionContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_switchExpression);

		    inSwitchExpressionLevel++;

		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1396);
			match(SWITCH);
			setState(1397);
			expressionInPar();
			setState(1398);
			nls();
			setState(1399);
			match(LBRACE);
			setState(1400);
			nls();
			setState(1404);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CASE || _la==DEFAULT) {
				{
				{
				setState(1401);
				switchBlockStatementExpressionGroup();
				}
				}
				setState(1406);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1407);
			nls();
			setState(1408);
			match(RBRACE);
			}
			_ctx.stop = _input.LT(-1);

			    inSwitchExpressionLevel--;

		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SwitchBlockStatementExpressionGroupContext extends GroovyParserRuleContext {
		public BlockStatementsContext blockStatements() {
			return getRuleContext(BlockStatementsContext.class,0);
		}
		public List<? extends SwitchExpressionLabelContext> switchExpressionLabel() {
			return getRuleContexts(SwitchExpressionLabelContext.class);
		}
		public SwitchExpressionLabelContext switchExpressionLabel(int i) {
			return getRuleContext(SwitchExpressionLabelContext.class,i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public SwitchBlockStatementExpressionGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchBlockStatementExpressionGroup; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSwitchBlockStatementExpressionGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final SwitchBlockStatementExpressionGroupContext switchBlockStatementExpressionGroup() throws RecognitionException {
		SwitchBlockStatementExpressionGroupContext _localctx = new SwitchBlockStatementExpressionGroupContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_switchBlockStatementExpressionGroup);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1413); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1410);
					switchExpressionLabel();
					setState(1411);
					nls();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1415); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,151,_ctx);
			} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(1417);
			blockStatements();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SwitchExpressionLabelContext extends GroovyParserRuleContext {
		public Token ac;
		public TerminalNode CASE() { return getToken(GroovyParser.CASE, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(GroovyParser.DEFAULT, 0); }
		public TerminalNode ARROW() { return getToken(GroovyParser.ARROW, 0); }
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public SwitchExpressionLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchExpressionLabel; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSwitchExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final SwitchExpressionLabelContext switchExpressionLabel() throws RecognitionException {
		SwitchExpressionLabelContext _localctx = new SwitchExpressionLabelContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_switchExpressionLabel);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1422);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				{
				setState(1419);
				match(CASE);
				setState(1420);
				expressionList(true);
				}
				break;
			case DEFAULT:
				{
				setState(1421);
				match(DEFAULT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1424);
			_localctx.ac = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==ARROW || _la==COLON) ) {
				_localctx.ac = _errHandler.recoverInline(this);
			} else {
				if (_input.LA(1) == Token.EOF) {
					matchedEOF = true;
				}

				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends GroovyParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CastExprAltContext extends ExpressionContext {
		public CastParExpressionContext castParExpression() {
			return getRuleContext(CastParExpressionContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CastExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCastExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AsyncClosureExprAltContext extends ExpressionContext {
		public TerminalNode ASYNC() { return getToken(GroovyParser.ASYNC, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public ClosureOrLambdaExpressionContext closureOrLambdaExpression() {
			return getRuleContext(ClosureOrLambdaExpressionContext.class,0);
		}
		public AsyncClosureExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAsyncClosureExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AwaitExprAltContext extends ExpressionContext {
		public TerminalNode AWAIT() { return getToken(GroovyParser.AWAIT, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public AwaitExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAwaitExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PostfixExprAltContext extends ExpressionContext {
		public PostfixExpressionContext postfixExpression() {
			return getRuleContext(PostfixExpressionContext.class,0);
		}
		public PostfixExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitPostfixExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SwitchExprAltContext extends ExpressionContext {
		public SwitchExpressionContext switchExpression() {
			return getRuleContext(SwitchExpressionContext.class,0);
		}
		public SwitchExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSwitchExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnaryNotExprAltContext extends ExpressionContext {
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode BITNOT() { return getToken(GroovyParser.BITNOT, 0); }
		public TerminalNode NOT() { return getToken(GroovyParser.NOT, 0); }
		public UnaryNotExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitUnaryNotExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PowerExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode POWER() { return getToken(GroovyParser.POWER, 0); }
		public PowerExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitPowerExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnaryAddExprAltContext extends ExpressionContext {
		public Token op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode INC() { return getToken(GroovyParser.INC, 0); }
		public TerminalNode DEC() { return getToken(GroovyParser.DEC, 0); }
		public TerminalNode ADD() { return getToken(GroovyParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(GroovyParser.SUB, 0); }
		public UnaryAddExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitUnaryAddExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class MultiplicativeExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode MUL() { return getToken(GroovyParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(GroovyParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(GroovyParser.MOD, 0); }
		public MultiplicativeExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMultiplicativeExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AdditiveExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ADD() { return getToken(GroovyParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(GroovyParser.SUB, 0); }
		public AdditiveExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAdditiveExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ShiftExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token dlOp;
		public Token tgOp;
		public Token dgOp;
		public Token rangeOp;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<? extends TerminalNode> LT() { return getTokens(GroovyParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(GroovyParser.LT, i);
		}
		public List<? extends TerminalNode> GT() { return getTokens(GroovyParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(GroovyParser.GT, i);
		}
		public TerminalNode RANGE_INCLUSIVE() { return getToken(GroovyParser.RANGE_INCLUSIVE, 0); }
		public TerminalNode RANGE_EXCLUSIVE_LEFT() { return getToken(GroovyParser.RANGE_EXCLUSIVE_LEFT, 0); }
		public TerminalNode RANGE_EXCLUSIVE_RIGHT() { return getToken(GroovyParser.RANGE_EXCLUSIVE_RIGHT, 0); }
		public TerminalNode RANGE_EXCLUSIVE_FULL() { return getToken(GroovyParser.RANGE_EXCLUSIVE_FULL, 0); }
		public ShiftExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitShiftExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RelationalExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public MatchingTypeContext matchingType() {
			return getRuleContext(MatchingTypeContext.class,0);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode INSTANCEOF() { return getToken(GroovyParser.INSTANCEOF, 0); }
		public CoercionTypeContext coercionType() {
			return getRuleContext(CoercionTypeContext.class,0);
		}
		public TerminalNode AS() { return getToken(GroovyParser.AS, 0); }
		public TerminalNode NOT_INSTANCEOF() { return getToken(GroovyParser.NOT_INSTANCEOF, 0); }
		public TerminalNode LE() { return getToken(GroovyParser.LE, 0); }
		public TerminalNode GE() { return getToken(GroovyParser.GE, 0); }
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
		public TerminalNode NOT_IN() { return getToken(GroovyParser.NOT_IN, 0); }
		public RelationalExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitRelationalExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class EqualityExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IDENTICAL() { return getToken(GroovyParser.IDENTICAL, 0); }
		public TerminalNode NOT_IDENTICAL() { return getToken(GroovyParser.NOT_IDENTICAL, 0); }
		public TerminalNode EQUAL() { return getToken(GroovyParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(GroovyParser.NOTEQUAL, 0); }
		public TerminalNode SPACESHIP() { return getToken(GroovyParser.SPACESHIP, 0); }
		public EqualityExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEqualityExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RegexExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode REGEX_FIND() { return getToken(GroovyParser.REGEX_FIND, 0); }
		public TerminalNode REGEX_MATCH() { return getToken(GroovyParser.REGEX_MATCH, 0); }
		public RegexExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitRegexExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AndExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BITAND() { return getToken(GroovyParser.BITAND, 0); }
		public AndExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAndExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExclusiveOrExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode XOR() { return getToken(GroovyParser.XOR, 0); }
		public ExclusiveOrExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitExclusiveOrExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class InclusiveOrExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BITOR() { return getToken(GroovyParser.BITOR, 0); }
		public InclusiveOrExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitInclusiveOrExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LogicalAndExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode AND() { return getToken(GroovyParser.AND, 0); }
		public LogicalAndExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitLogicalAndExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LogicalOrExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode OR() { return getToken(GroovyParser.OR, 0); }
		public LogicalOrExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitLogicalOrExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ImplicationExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public ExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IMPLIES() { return getToken(GroovyParser.IMPLIES, 0); }
		public ImplicationExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitImplicationExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ConditionalExprAltContext extends ExpressionContext {
		public ExpressionContext con;
		public ExpressionContext tb;
		public ExpressionContext fb;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode QUESTION() { return getToken(GroovyParser.QUESTION, 0); }
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode ELVIS() { return getToken(GroovyParser.ELVIS, 0); }
		public ConditionalExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitConditionalExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class MultipleAssignmentExprAltContext extends ExpressionContext {
		public VariableNamesContext left;
		public Token op;
		public StatementExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public VariableNamesContext variableNames() {
			return getRuleContext(VariableNamesContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public StatementExpressionContext statementExpression() {
			return getRuleContext(StatementExpressionContext.class,0);
		}
		public MultipleAssignmentExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMultipleAssignmentExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AssignmentExprAltContext extends ExpressionContext {
		public ExpressionContext left;
		public Token op;
		public EnhancedStatementExpressionContext right;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public EnhancedStatementExpressionContext enhancedStatementExpression() {
			return getRuleContext(EnhancedStatementExpressionContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public TerminalNode ADD_ASSIGN() { return getToken(GroovyParser.ADD_ASSIGN, 0); }
		public TerminalNode SUB_ASSIGN() { return getToken(GroovyParser.SUB_ASSIGN, 0); }
		public TerminalNode MUL_ASSIGN() { return getToken(GroovyParser.MUL_ASSIGN, 0); }
		public TerminalNode DIV_ASSIGN() { return getToken(GroovyParser.DIV_ASSIGN, 0); }
		public TerminalNode AND_ASSIGN() { return getToken(GroovyParser.AND_ASSIGN, 0); }
		public TerminalNode OR_ASSIGN() { return getToken(GroovyParser.OR_ASSIGN, 0); }
		public TerminalNode XOR_ASSIGN() { return getToken(GroovyParser.XOR_ASSIGN, 0); }
		public TerminalNode RSHIFT_ASSIGN() { return getToken(GroovyParser.RSHIFT_ASSIGN, 0); }
		public TerminalNode URSHIFT_ASSIGN() { return getToken(GroovyParser.URSHIFT_ASSIGN, 0); }
		public TerminalNode LSHIFT_ASSIGN() { return getToken(GroovyParser.LSHIFT_ASSIGN, 0); }
		public TerminalNode MOD_ASSIGN() { return getToken(GroovyParser.MOD_ASSIGN, 0); }
		public TerminalNode POWER_ASSIGN() { return getToken(GroovyParser.POWER_ASSIGN, 0); }
		public TerminalNode ELVIS_ASSIGN() { return getToken(GroovyParser.ELVIS_ASSIGN, 0); }
		public AssignmentExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAssignmentExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 244;
		enterRecursionRule(_localctx, 244, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1475);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,156,_ctx) ) {
			case 1:
				{
				_localctx = new CastExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1427);
				castParExpression();
				setState(1428);
				castOperandExpression();
				}
				break;
			case 2:
				{
				_localctx = new AsyncClosureExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1430);
				match(ASYNC);
				setState(1431);
				nls();
				setState(1432);
				closureOrLambdaExpression();
				}
				break;
			case 3:
				{
				_localctx = new AwaitExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1434);
				match(AWAIT);
				setState(1435);
				nls();
				setState(1459);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
				case 1:
					{
					setState(1436);
					match(LPAREN);
					setState(1437);
					expression(0);
					setState(1444);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(1438);
						match(COMMA);
						setState(1439);
						nls();
						setState(1440);
						expression(0);
						}
						}
						setState(1446);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(1447);
					match(RPAREN);
					}
					break;
				case 2:
					{
					setState(1449);
					expression(0);
					setState(1456);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,154,_ctx);
					while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(1450);
							match(COMMA);
							setState(1451);
							nls();
							setState(1452);
							expression(0);
							}
							} 
						}
						setState(1458);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,154,_ctx);
					}
					}
					break;
				}
				}
				break;
			case 4:
				{
				_localctx = new PostfixExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1461);
				postfixExpression();
				}
				break;
			case 5:
				{
				_localctx = new SwitchExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1462);
				switchExpression();
				}
				break;
			case 6:
				{
				_localctx = new UnaryNotExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1463);
				_la = _input.LA(1);
				if ( !(_la==NOT || _la==BITNOT) ) {
				_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1464);
				nls();
				setState(1465);
				expression(20);
				}
				break;
			case 7:
				{
				_localctx = new UnaryAddExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1467);
				((UnaryAddExprAltContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 114)) & ~0x3f) == 0 && ((1L << (_la - 114)) & ((1L << (INC - 114)) | (1L << (DEC - 114)) | (1L << (ADD - 114)) | (1L << (SUB - 114)))) != 0)) ) {
					((UnaryAddExprAltContext)_localctx).op = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1468);
				expression(18);
				}
				break;
			case 8:
				{
				_localctx = new MultipleAssignmentExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1469);
				((MultipleAssignmentExprAltContext)_localctx).left = variableNames();
				setState(1470);
				nls();
				setState(1471);
				((MultipleAssignmentExprAltContext)_localctx).op = match(ASSIGN);
				setState(1472);
				nls();
				setState(1473);
				((MultipleAssignmentExprAltContext)_localctx).right = statementExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1599);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,161,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1597);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
					case 1:
						{
						_localctx = new PowerExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((PowerExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1477);
						if (!(precpred(_ctx, 19))) throw createFailedPredicateException("precpred(_ctx, 19)");
						setState(1478);
						((PowerExprAltContext)_localctx).op = match(POWER);
						setState(1479);
						nls();
						setState(1480);
						((PowerExprAltContext)_localctx).right = expression(20);
						}
						break;
					case 2:
						{
						_localctx = new MultiplicativeExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((MultiplicativeExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1482);
						if (!(precpred(_ctx, 17))) throw createFailedPredicateException("precpred(_ctx, 17)");
						setState(1483);
						nls();
						setState(1484);
						((MultiplicativeExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 118)) & ~0x3f) == 0 && ((1L << (_la - 118)) & ((1L << (MUL - 118)) | (1L << (DIV - 118)) | (1L << (MOD - 118)))) != 0)) ) {
							((MultiplicativeExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1485);
						nls();
						setState(1486);
						((MultiplicativeExprAltContext)_localctx).right = expression(18);
						}
						break;
					case 3:
						{
						_localctx = new AdditiveExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AdditiveExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1488);
						if (!(precpred(_ctx, 16))) throw createFailedPredicateException("precpred(_ctx, 16)");
						setState(1489);
						((AdditiveExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==ADD || _la==SUB) ) {
							((AdditiveExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1490);
						nls();
						setState(1491);
						((AdditiveExprAltContext)_localctx).right = expression(17);
						}
						break;
					case 4:
						{
						_localctx = new ShiftExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ShiftExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1493);
						if (!(precpred(_ctx, 15))) throw createFailedPredicateException("precpred(_ctx, 15)");
						setState(1494);
						nls();
						setState(1505);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case GT:
						case LT:
							{
							setState(1502);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,157,_ctx) ) {
							case 1:
								{
								setState(1495);
								((ShiftExprAltContext)_localctx).dlOp = match(LT);
								setState(1496);
								match(LT);
								}
								break;
							case 2:
								{
								setState(1497);
								((ShiftExprAltContext)_localctx).tgOp = match(GT);
								setState(1498);
								match(GT);
								setState(1499);
								match(GT);
								}
								break;
							case 3:
								{
								setState(1500);
								((ShiftExprAltContext)_localctx).dgOp = match(GT);
								setState(1501);
								match(GT);
								}
								break;
							}
							}
							break;
						case RANGE_INCLUSIVE:
						case RANGE_EXCLUSIVE_LEFT:
						case RANGE_EXCLUSIVE_RIGHT:
						case RANGE_EXCLUSIVE_FULL:
							{
							setState(1504);
							((ShiftExprAltContext)_localctx).rangeOp = _input.LT(1);
							_la = _input.LA(1);
							if ( !(((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & ((1L << (RANGE_INCLUSIVE - 70)) | (1L << (RANGE_EXCLUSIVE_LEFT - 70)) | (1L << (RANGE_EXCLUSIVE_RIGHT - 70)) | (1L << (RANGE_EXCLUSIVE_FULL - 70)))) != 0)) ) {
								((ShiftExprAltContext)_localctx).rangeOp = _errHandler.recoverInline(this);
							} else {
								if (_input.LA(1) == Token.EOF) {
									matchedEOF = true;
								}

								_errHandler.reportMatch(this);
								consume();
							}
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1507);
						nls();
						setState(1508);
						((ShiftExprAltContext)_localctx).right = expression(16);
						}
						break;
					case 5:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1510);
						if (!(precpred(_ctx, 12))) throw createFailedPredicateException("precpred(_ctx, 12)");
						setState(1511);
						nls();
						setState(1512);
						((RelationalExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==IN || ((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & ((1L << (NOT_IN - 91)) | (1L << (GT - 91)) | (1L << (LT - 91)) | (1L << (LE - 91)) | (1L << (GE - 91)))) != 0)) ) {
							((RelationalExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1513);
						nls();
						setState(1514);
						((RelationalExprAltContext)_localctx).right = expression(13);
						}
						break;
					case 6:
						{
						_localctx = new EqualityExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((EqualityExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1516);
						if (!(precpred(_ctx, 11))) throw createFailedPredicateException("precpred(_ctx, 11)");
						setState(1517);
						nls();
						setState(1518);
						((EqualityExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & ((1L << (SPACESHIP - 85)) | (1L << (IDENTICAL - 85)) | (1L << (NOT_IDENTICAL - 85)) | (1L << (EQUAL - 85)) | (1L << (NOTEQUAL - 85)))) != 0)) ) {
							((EqualityExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1519);
						nls();
						setState(1520);
						((EqualityExprAltContext)_localctx).right = expression(12);
						}
						break;
					case 7:
						{
						_localctx = new RegexExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RegexExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1522);
						if (!(precpred(_ctx, 10))) throw createFailedPredicateException("precpred(_ctx, 10)");
						setState(1523);
						nls();
						setState(1524);
						((RegexExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==REGEX_FIND || _la==REGEX_MATCH) ) {
							((RegexExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1525);
						nls();
						setState(1526);
						((RegexExprAltContext)_localctx).right = expression(11);
						}
						break;
					case 8:
						{
						_localctx = new AndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1528);
						if (!(precpred(_ctx, 9))) throw createFailedPredicateException("precpred(_ctx, 9)");
						setState(1529);
						nls();
						setState(1530);
						((AndExprAltContext)_localctx).op = match(BITAND);
						setState(1531);
						nls();
						setState(1532);
						((AndExprAltContext)_localctx).right = expression(10);
						}
						break;
					case 9:
						{
						_localctx = new ExclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ExclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1534);
						if (!(precpred(_ctx, 8))) throw createFailedPredicateException("precpred(_ctx, 8)");
						setState(1535);
						nls();
						setState(1536);
						((ExclusiveOrExprAltContext)_localctx).op = match(XOR);
						setState(1537);
						nls();
						setState(1538);
						((ExclusiveOrExprAltContext)_localctx).right = expression(9);
						}
						break;
					case 10:
						{
						_localctx = new InclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((InclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1540);
						if (!(precpred(_ctx, 7))) throw createFailedPredicateException("precpred(_ctx, 7)");
						setState(1541);
						nls();
						setState(1542);
						((InclusiveOrExprAltContext)_localctx).op = match(BITOR);
						setState(1543);
						nls();
						setState(1544);
						((InclusiveOrExprAltContext)_localctx).right = expression(8);
						}
						break;
					case 11:
						{
						_localctx = new LogicalAndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalAndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1546);
						if (!(precpred(_ctx, 6))) throw createFailedPredicateException("precpred(_ctx, 6)");
						setState(1547);
						nls();
						setState(1548);
						((LogicalAndExprAltContext)_localctx).op = match(AND);
						setState(1549);
						nls();
						setState(1550);
						((LogicalAndExprAltContext)_localctx).right = expression(7);
						}
						break;
					case 12:
						{
						_localctx = new LogicalOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1552);
						if (!(precpred(_ctx, 5))) throw createFailedPredicateException("precpred(_ctx, 5)");
						setState(1553);
						nls();
						setState(1554);
						((LogicalOrExprAltContext)_localctx).op = match(OR);
						setState(1555);
						nls();
						setState(1556);
						((LogicalOrExprAltContext)_localctx).right = expression(6);
						}
						break;
					case 13:
						{
						_localctx = new ImplicationExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ImplicationExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1558);
						if (!(precpred(_ctx, 4))) throw createFailedPredicateException("precpred(_ctx, 4)");
						setState(1559);
						nls();
						setState(1560);
						((ImplicationExprAltContext)_localctx).op = match(IMPLIES);
						setState(1561);
						nls();
						setState(1562);
						((ImplicationExprAltContext)_localctx).right = expression(4);
						}
						break;
					case 14:
						{
						_localctx = new ConditionalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ConditionalExprAltContext)_localctx).con = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1564);
						if (!(precpred(_ctx, 3))) throw createFailedPredicateException("precpred(_ctx, 3)");
						setState(1565);
						nls();
						setState(1575);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case QUESTION:
							{
							setState(1566);
							match(QUESTION);
							setState(1567);
							nls();
							setState(1568);
							((ConditionalExprAltContext)_localctx).tb = expression(0);
							setState(1569);
							nls();
							setState(1570);
							match(COLON);
							setState(1571);
							nls();
							}
							break;
						case ELVIS:
							{
							setState(1573);
							match(ELVIS);
							setState(1574);
							nls();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1577);
						((ConditionalExprAltContext)_localctx).fb = expression(3);
						}
						break;
					case 15:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1579);
						if (!(precpred(_ctx, 14))) throw createFailedPredicateException("precpred(_ctx, 14)");
						setState(1580);
						nls();
						setState(1581);
						((RelationalExprAltContext)_localctx).op = match(INSTANCEOF);
						setState(1582);
						nls();
						setState(1583);
						matchingType();
						}
						break;
					case 16:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1585);
						if (!(precpred(_ctx, 13))) throw createFailedPredicateException("precpred(_ctx, 13)");
						setState(1586);
						nls();
						setState(1587);
						((RelationalExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==AS || _la==NOT_INSTANCEOF) ) {
							((RelationalExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1588);
						nls();
						setState(1589);
						coercionType();
						}
						break;
					case 17:
						{
						_localctx = new AssignmentExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AssignmentExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1591);
						if (!(precpred(_ctx, 1))) throw createFailedPredicateException("precpred(_ctx, 1)");
						setState(1592);
						nls();
						setState(1593);
						((AssignmentExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 84)) & ~0x3f) == 0 && ((1L << (_la - 84)) & ((1L << (POWER_ASSIGN - 84)) | (1L << (ASSIGN - 84)) | (1L << (ADD_ASSIGN - 84)) | (1L << (SUB_ASSIGN - 84)) | (1L << (MUL_ASSIGN - 84)) | (1L << (DIV_ASSIGN - 84)) | (1L << (AND_ASSIGN - 84)) | (1L << (OR_ASSIGN - 84)) | (1L << (XOR_ASSIGN - 84)) | (1L << (MOD_ASSIGN - 84)) | (1L << (LSHIFT_ASSIGN - 84)) | (1L << (RSHIFT_ASSIGN - 84)) | (1L << (URSHIFT_ASSIGN - 84)) | (1L << (ELVIS_ASSIGN - 84)))) != 0)) ) {
							((AssignmentExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1594);
						nls();
						setState(1595);
						((AssignmentExprAltContext)_localctx).right = enhancedStatementExpression();
						}
						break;
					}
					} 
				}
				setState(1601);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,161,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final ExpressionContext castOperandExpression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_castOperandExpression);
		int _la;
		try {
			setState(1612);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,162,_ctx) ) {
			case 1:
				_localctx = new CastExprAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1602);
				castParExpression();
				setState(1603);
				castOperandExpression();
				}
				break;
			case 2:
				_localctx = new PostfixExprAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1605);
				postfixExpression();
				}
				break;
			case 3:
				_localctx = new UnaryNotExprAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1606);
				_la = _input.LA(1);
				if ( !(_la==NOT || _la==BITNOT) ) {
				_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1607);
				nls();
				setState(1608);
				castOperandExpression();
				}
				break;
			case 4:
				_localctx = new UnaryAddExprAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1610);
				((UnaryAddExprAltContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 114)) & ~0x3f) == 0 && ((1L << (_la - 114)) & ((1L << (INC - 114)) | (1L << (DEC - 114)) | (1L << (ADD - 114)) | (1L << (SUB - 114)))) != 0)) ) {
					((UnaryAddExprAltContext)_localctx).op = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1611);
				castOperandExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommandExpressionContext extends GroovyParserRuleContext {
		public ExpressionContext expression;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public EnhancedArgumentListInParContext enhancedArgumentListInPar() {
			return getRuleContext(EnhancedArgumentListInParContext.class,0);
		}
		public List<? extends CommandArgumentContext> commandArgument() {
			return getRuleContexts(CommandArgumentContext.class);
		}
		public CommandArgumentContext commandArgument(int i) {
			return getRuleContext(CommandArgumentContext.class,i);
		}
		public CommandExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commandExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCommandExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CommandExpressionContext commandExpression() throws RecognitionException {
		CommandExpressionContext _localctx = new CommandExpressionContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_commandExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1614);
			_localctx.expression = expression(0);
			setState(1618);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,163,_ctx) ) {
			case 1:
				{
				setState(1615);
				if (!( !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) )) throw createFailedPredicateException(" !SemanticPredicates.isFollowingArgumentsOrClosure($expression.ctx) ");
				setState(1616);
				argumentList();
				}
				break;
			case 2:
				{
				}
				break;
			}
			setState(1623);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,164,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1620);
					commandArgument();
					}
					} 
				}
				setState(1625);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,164,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommandArgumentContext extends GroovyParserRuleContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public EnhancedArgumentListInParContext enhancedArgumentListInPar() {
			return getRuleContext(EnhancedArgumentListInParContext.class,0);
		}
		public List<? extends PathElementContext> pathElement() {
			return getRuleContexts(PathElementContext.class);
		}
		public PathElementContext pathElement(int i) {
			return getRuleContext(PathElementContext.class,i);
		}
		public CommandArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commandArgument; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCommandArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CommandArgumentContext commandArgument() throws RecognitionException {
		CommandArgumentContext _localctx = new CommandArgumentContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_commandArgument);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1626);
			commandPrimary();
			setState(1633);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
			case 1:
				{
				setState(1628); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1627);
						pathElement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1630); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,165,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 2:
				{
				setState(1632);
				argumentList();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathExpressionContext extends GroovyParserRuleContext {
		public int t;
		public PathElementContext pathElement;
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public TerminalNode STATIC() { return getToken(GroovyParser.STATIC, 0); }
		public List<? extends PathElementContext> pathElement() {
			return getRuleContexts(PathElementContext.class);
		}
		public PathElementContext pathElement(int i) {
			return getRuleContext(PathElementContext.class,i);
		}
		public PathExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathExpression; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitPathExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final PathExpressionContext pathExpression() throws RecognitionException {
		PathExpressionContext _localctx = new PathExpressionContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_pathExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1638);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,167,_ctx) ) {
			case 1:
				{
				setState(1635);
				primary();
				}
				break;
			case 2:
				{
				setState(1636);
				if (!( _input.LT(2).getType() == DOT )) throw createFailedPredicateException(" _input.LT(2).getType() == DOT ");
				setState(1637);
				match(STATIC);
				}
				break;
			}
			setState(1645);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,168,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1640);
					_localctx.pathElement = pathElement();
					 _localctx.t =  _localctx.pathElement.t; 
					}
					} 
				}
				setState(1647);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,168,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathElementContext extends GroovyParserRuleContext {
		public int t;
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode DOT() { return getToken(GroovyParser.DOT, 0); }
		public TerminalNode NEW() { return getToken(GroovyParser.NEW, 0); }
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public NamePartContext namePart() {
			return getRuleContext(NamePartContext.class,0);
		}
		public ClosureOrLambdaExpressionContext closureOrLambdaExpression() {
			return getRuleContext(ClosureOrLambdaExpressionContext.class,0);
		}
		public TerminalNode METHOD_POINTER() { return getToken(GroovyParser.METHOD_POINTER, 0); }
		public TerminalNode METHOD_REFERENCE() { return getToken(GroovyParser.METHOD_REFERENCE, 0); }
		public TerminalNode SPREAD_DOT() { return getToken(GroovyParser.SPREAD_DOT, 0); }
		public TerminalNode SAFE_DOT() { return getToken(GroovyParser.SAFE_DOT, 0); }
		public TerminalNode SAFE_CHAIN_DOT() { return getToken(GroovyParser.SAFE_CHAIN_DOT, 0); }
		public TerminalNode AT() { return getToken(GroovyParser.AT, 0); }
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public IndexPropertyArgsContext indexPropertyArgs() {
			return getRuleContext(IndexPropertyArgsContext.class,0);
		}
		public NamedPropertyArgsContext namedPropertyArgs() {
			return getRuleContext(NamedPropertyArgsContext.class,0);
		}
		public PathElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathElement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitPathElement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final PathElementContext pathElement() throws RecognitionException {
		PathElementContext _localctx = new PathElementContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_pathElement);
		int _la;
		try {
			setState(1687);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,173,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1648);
				nls();
				setState(1676);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,172,_ctx) ) {
				case 1:
					{
					setState(1649);
					match(DOT);
					setState(1650);
					nls();
					setState(1651);
					match(NEW);
					setState(1652);
					creator(1);
					 _localctx.t =  6; 
					}
					break;
				case 2:
					{
					setState(1668);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case SPREAD_DOT:
					case SAFE_DOT:
					case SAFE_CHAIN_DOT:
					case DOT:
						{
						setState(1655);
						_la = _input.LA(1);
						if ( !(((((_la - 74)) & ~0x3f) == 0 && ((1L << (_la - 74)) & ((1L << (SPREAD_DOT - 74)) | (1L << (SAFE_DOT - 74)) | (1L << (SAFE_CHAIN_DOT - 74)) | (1L << (DOT - 74)))) != 0)) ) {
						_errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1656);
						nls();
						setState(1659);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case AT:
							{
							setState(1657);
							match(AT);
							}
							break;
						case LT:
							{
							setState(1658);
							nonWildcardTypeArguments();
							}
							break;
						case StringLiteral:
						case GStringBegin:
						case AS:
						case DEF:
						case IN:
						case TRAIT:
						case THREADSAFE:
						case ASYNC:
						case AWAIT:
						case DEFER:
						case BuiltInPrimitiveType:
						case ABSTRACT:
						case ASSERT:
						case BREAK:
						case CASE:
						case CATCH:
						case CLASS:
						case CONST:
						case CONTINUE:
						case DEFAULT:
						case DO:
						case ELSE:
						case ENUM:
						case EXTENDS:
						case FINAL:
						case FINALLY:
						case FOR:
						case IF:
						case GOTO:
						case IMPLEMENTS:
						case IMPORT:
						case INSTANCEOF:
						case INTERFACE:
						case MODULE:
						case NATIVE:
						case NEW:
						case NON_SEALED:
						case PACKAGE:
						case PERMITS:
						case PRIVATE:
						case PROTECTED:
						case PUBLIC:
						case RECORD:
						case RETURN:
						case SEALED:
						case STATIC:
						case STRICTFP:
						case SUPER:
						case SWITCH:
						case SYNCHRONIZED:
						case THIS:
						case THROW:
						case THROWS:
						case TRANSIENT:
						case TRY:
						case VAL:
						case VAR:
						case VOID:
						case VOLATILE:
						case WHILE:
						case YIELD:
						case BooleanLiteral:
						case NullLiteral:
						case LPAREN:
						case CapitalizedIdentifier:
						case Identifier:
							break;
						default:
							break;
						}
						}
						break;
					case METHOD_POINTER:
						{
						setState(1661);
						match(METHOD_POINTER);
						setState(1662);
						nls();
						}
						break;
					case METHOD_REFERENCE:
						{
						setState(1663);
						match(METHOD_REFERENCE);
						setState(1664);
						nls();
						setState(1666);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(1665);
							nonWildcardTypeArguments();
							}
						}

						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1670);
					namePart();
					 _localctx.t =  1; 
					}
					break;
				case 3:
					{
					setState(1673);
					closureOrLambdaExpression();
					 _localctx.t =  3; 
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1678);
				arguments();
				 _localctx.t =  2; 
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1681);
				indexPropertyArgs();
				 _localctx.t =  4; 
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1684);
				namedPropertyArgs();
				 _localctx.t =  5; 
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamePartContext extends GroovyParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public DynamicMemberNameContext dynamicMemberName() {
			return getRuleContext(DynamicMemberNameContext.class,0);
		}
		public KeywordsContext keywords() {
			return getRuleContext(KeywordsContext.class,0);
		}
		public NamePartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namePart; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitNamePart(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final NamePartContext namePart() throws RecognitionException {
		NamePartContext _localctx = new NamePartContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_namePart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1693);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				{
				setState(1689);
				identifier();
				}
				break;
			case 2:
				{
				setState(1690);
				stringLiteral();
				}
				break;
			case 3:
				{
				setState(1691);
				dynamicMemberName();
				}
				break;
			case 4:
				{
				setState(1692);
				keywords();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DynamicMemberNameContext extends GroovyParserRuleContext {
		public ParExpressionContext parExpression() {
			return getRuleContext(ParExpressionContext.class,0);
		}
		public GstringContext gstring() {
			return getRuleContext(GstringContext.class,0);
		}
		public DynamicMemberNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dynamicMemberName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitDynamicMemberName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final DynamicMemberNameContext dynamicMemberName() throws RecognitionException {
		DynamicMemberNameContext _localctx = new DynamicMemberNameContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_dynamicMemberName);
		try {
			setState(1697);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1695);
				parExpression();
				}
				break;
			case GStringBegin:
				enterOuterAlt(_localctx, 2);
				{
				setState(1696);
				gstring();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexPropertyArgsContext extends GroovyParserRuleContext {
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public TerminalNode SAFE_INDEX() { return getToken(GroovyParser.SAFE_INDEX, 0); }
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public IndexPropertyArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexPropertyArgs; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitIndexPropertyArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final IndexPropertyArgsContext indexPropertyArgs() throws RecognitionException {
		IndexPropertyArgsContext _localctx = new IndexPropertyArgsContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_indexPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1699);
			_la = _input.LA(1);
			if ( !(_la==SAFE_INDEX || _la==LBRACK) ) {
			_errHandler.recoverInline(this);
			} else {
				if (_input.LA(1) == Token.EOF) {
					matchedEOF = true;
				}

				_errHandler.reportMatch(this);
				consume();
			}
			setState(1701);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
			case 1:
				{
				setState(1700);
				expressionList(true);
				}
				break;
			}
			setState(1703);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamedPropertyArgsContext extends GroovyParserRuleContext {
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public TerminalNode SAFE_INDEX() { return getToken(GroovyParser.SAFE_INDEX, 0); }
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public MapEntryListContext mapEntryList() {
			return getRuleContext(MapEntryListContext.class,0);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public NamedPropertyArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedPropertyArgs; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitNamedPropertyArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final NamedPropertyArgsContext namedPropertyArgs() throws RecognitionException {
		NamedPropertyArgsContext _localctx = new NamedPropertyArgsContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_namedPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1705);
			_la = _input.LA(1);
			if ( !(_la==SAFE_INDEX || _la==LBRACK) ) {
			_errHandler.recoverInline(this);
			} else {
				if (_input.LA(1) == Token.EOF) {
					matchedEOF = true;
				}

				_errHandler.reportMatch(this);
				consume();
			}
			setState(1708);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case BuiltInPrimitiveType:
			case ABSTRACT:
			case ASSERT:
			case BREAK:
			case CASE:
			case CATCH:
			case CLASS:
			case CONST:
			case CONTINUE:
			case DEFAULT:
			case DO:
			case ELSE:
			case ENUM:
			case EXTENDS:
			case FINAL:
			case FINALLY:
			case FOR:
			case IF:
			case GOTO:
			case IMPLEMENTS:
			case IMPORT:
			case INSTANCEOF:
			case INTERFACE:
			case MODULE:
			case NATIVE:
			case NEW:
			case NON_SEALED:
			case PACKAGE:
			case PERMITS:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RECORD:
			case RETURN:
			case SEALED:
			case STATIC:
			case STRICTFP:
			case SUPER:
			case SWITCH:
			case SYNCHRONIZED:
			case THIS:
			case THROW:
			case THROWS:
			case TRANSIENT:
			case TRY:
			case VAL:
			case VAR:
			case VOID:
			case VOLATILE:
			case WHILE:
			case YIELD:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
			case LPAREN:
			case LBRACK:
			case MUL:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(1706);
				namedPropertyArgList();
				}
				break;
			case COLON:
				{
				setState(1707);
				match(COLON);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1710);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimaryContext extends GroovyParserRuleContext {
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
	 
		public PrimaryContext() { }
		public void copyFrom(PrimaryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class IdentifierPrmrAltContext extends PrimaryContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public IdentifierPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitIdentifierPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LiteralPrmrAltContext extends PrimaryContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public LiteralPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitLiteralPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class GstringPrmrAltContext extends PrimaryContext {
		public GstringContext gstring() {
			return getRuleContext(GstringContext.class,0);
		}
		public GstringPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitGstringPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NewPrmrAltContext extends PrimaryContext {
		public TerminalNode NEW() { return getToken(GroovyParser.NEW, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public NewPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitNewPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ThisPrmrAltContext extends PrimaryContext {
		public TerminalNode THIS() { return getToken(GroovyParser.THIS, 0); }
		public ThisPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitThisPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SuperPrmrAltContext extends PrimaryContext {
		public TerminalNode SUPER() { return getToken(GroovyParser.SUPER, 0); }
		public SuperPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSuperPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParenPrmrAltContext extends PrimaryContext {
		public ParExpressionContext parExpression() {
			return getRuleContext(ParExpressionContext.class,0);
		}
		public ParenPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitParenPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ClosureOrLambdaExpressionPrmrAltContext extends PrimaryContext {
		public ClosureOrLambdaExpressionContext closureOrLambdaExpression() {
			return getRuleContext(ClosureOrLambdaExpressionContext.class,0);
		}
		public ClosureOrLambdaExpressionPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClosureOrLambdaExpressionPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ListPrmrAltContext extends PrimaryContext {
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public ListPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitListPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class MapPrmrAltContext extends PrimaryContext {
		public MapContext map() {
			return getRuleContext(MapContext.class,0);
		}
		public MapPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMapPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BuiltInTypePrmrAltContext extends PrimaryContext {
		public BuiltInTypeContext builtInType() {
			return getRuleContext(BuiltInTypeContext.class,0);
		}
		public BuiltInTypePrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBuiltInTypePrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_primary);
		try {
			setState(1729);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,179,_ctx) ) {
			case 1:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1712);
				identifier();
				setState(1714);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
				case 1:
					{
					setState(1713);
					typeArguments();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new LiteralPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1716);
				literal();
				}
				break;
			case 3:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1717);
				gstring();
				}
				break;
			case 4:
				_localctx = new NewPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1718);
				match(NEW);
				setState(1719);
				nls();
				setState(1720);
				creator(0);
				}
				break;
			case 5:
				_localctx = new ThisPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1722);
				match(THIS);
				}
				break;
			case 6:
				_localctx = new SuperPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1723);
				match(SUPER);
				}
				break;
			case 7:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1724);
				parExpression();
				}
				break;
			case 8:
				_localctx = new ClosureOrLambdaExpressionPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1725);
				closureOrLambdaExpression();
				}
				break;
			case 9:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1726);
				list();
				}
				break;
			case 10:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1727);
				map();
				}
				break;
			case 11:
				_localctx = new BuiltInTypePrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1728);
				builtInType();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final PrimaryContext namedPropertyArgPrimary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_namedPropertyArgPrimary);
		try {
			setState(1737);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
			case 1:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1731);
				identifier();
				}
				break;
			case 2:
				_localctx = new LiteralPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1732);
				literal();
				}
				break;
			case 3:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1733);
				gstring();
				}
				break;
			case 4:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1734);
				parExpression();
				}
				break;
			case 5:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1735);
				list();
				}
				break;
			case 6:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1736);
				map();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final PrimaryContext namedArgPrimary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_namedArgPrimary);
		try {
			setState(1742);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1739);
				identifier();
				}
				break;
			case StringLiteral:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
				_localctx = new LiteralPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1740);
				literal();
				}
				break;
			case GStringBegin:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1741);
				gstring();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final PrimaryContext commandPrimary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_commandPrimary);
		try {
			setState(1747);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1744);
				identifier();
				}
				break;
			case StringLiteral:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
				_localctx = new LiteralPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1745);
				literal();
				}
				break;
			case GStringBegin:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1746);
				gstring();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListContext extends GroovyParserRuleContext {
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(GroovyParser.COMMA, 0); }
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitList(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1749);
			match(LBRACK);
			setState(1751);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
			case 1:
				{
				setState(1750);
				expressionList(true);
				}
				break;
			}
			setState(1754);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1753);
				match(COMMA);
				}
			}

			setState(1756);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MapContext extends GroovyParserRuleContext {
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public MapEntryListContext mapEntryList() {
			return getRuleContext(MapEntryListContext.class,0);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode COMMA() { return getToken(GroovyParser.COMMA, 0); }
		public MapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_map; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMap(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MapContext map() throws RecognitionException {
		MapContext _localctx = new MapContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_map);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1758);
			match(LBRACK);
			setState(1764);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case BuiltInPrimitiveType:
			case ABSTRACT:
			case ASSERT:
			case BREAK:
			case CASE:
			case CATCH:
			case CLASS:
			case CONST:
			case CONTINUE:
			case DEFAULT:
			case DO:
			case ELSE:
			case ENUM:
			case EXTENDS:
			case FINAL:
			case FINALLY:
			case FOR:
			case IF:
			case GOTO:
			case IMPLEMENTS:
			case IMPORT:
			case INSTANCEOF:
			case INTERFACE:
			case MODULE:
			case NATIVE:
			case NEW:
			case NON_SEALED:
			case PACKAGE:
			case PERMITS:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RECORD:
			case RETURN:
			case SEALED:
			case STATIC:
			case STRICTFP:
			case SUPER:
			case SWITCH:
			case SYNCHRONIZED:
			case THIS:
			case THROW:
			case THROWS:
			case TRANSIENT:
			case TRY:
			case VAL:
			case VAR:
			case VOID:
			case VOLATILE:
			case WHILE:
			case YIELD:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
			case LPAREN:
			case LBRACE:
			case LBRACK:
			case MUL:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(1759);
				mapEntryList();
				setState(1761);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1760);
					match(COMMA);
					}
				}

				}
				break;
			case COLON:
				{
				setState(1763);
				match(COLON);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1766);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MapEntryListContext extends GroovyParserRuleContext {
		public List<? extends MapEntryContext> mapEntry() {
			return getRuleContexts(MapEntryContext.class);
		}
		public MapEntryContext mapEntry(int i) {
			return getRuleContext(MapEntryContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public MapEntryListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapEntryList; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMapEntryList(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MapEntryListContext mapEntryList() throws RecognitionException {
		MapEntryListContext _localctx = new MapEntryListContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_mapEntryList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1768);
			mapEntry();
			setState(1773);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,187,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1769);
					match(COMMA);
					setState(1770);
					mapEntry();
					}
					} 
				}
				setState(1775);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,187,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryListContext namedPropertyArgList() throws RecognitionException {
		MapEntryListContext _localctx = new MapEntryListContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_namedPropertyArgList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1776);
			namedPropertyArg();
			setState(1781);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1777);
				match(COMMA);
				setState(1778);
				namedPropertyArg();
				}
				}
				setState(1783);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MapEntryContext extends GroovyParserRuleContext {
		public MapEntryLabelContext mapEntryLabel() {
			return getRuleContext(MapEntryLabelContext.class,0);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public EnhancedExpressionContext enhancedExpression() {
			return getRuleContext(EnhancedExpressionContext.class,0);
		}
		public TerminalNode MUL() { return getToken(GroovyParser.MUL, 0); }
		public MapEntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapEntry; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMapEntry(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MapEntryContext mapEntry() throws RecognitionException {
		MapEntryContext _localctx = new MapEntryContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_mapEntry);
		try {
			setState(1794);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case BuiltInPrimitiveType:
			case ABSTRACT:
			case ASSERT:
			case BREAK:
			case CASE:
			case CATCH:
			case CLASS:
			case CONST:
			case CONTINUE:
			case DEFAULT:
			case DO:
			case ELSE:
			case ENUM:
			case EXTENDS:
			case FINAL:
			case FINALLY:
			case FOR:
			case IF:
			case GOTO:
			case IMPLEMENTS:
			case IMPORT:
			case INSTANCEOF:
			case INTERFACE:
			case MODULE:
			case NATIVE:
			case NEW:
			case NON_SEALED:
			case PACKAGE:
			case PERMITS:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RECORD:
			case RETURN:
			case SEALED:
			case STATIC:
			case STRICTFP:
			case SUPER:
			case SWITCH:
			case SYNCHRONIZED:
			case THIS:
			case THROW:
			case THROWS:
			case TRANSIENT:
			case TRY:
			case VAL:
			case VAR:
			case VOID:
			case VOLATILE:
			case WHILE:
			case YIELD:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
			case LPAREN:
			case LBRACE:
			case LBRACK:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(1784);
				mapEntryLabel();
				setState(1785);
				match(COLON);
				setState(1786);
				nls();
				setState(1787);
				enhancedExpression();
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1789);
				match(MUL);
				setState(1790);
				match(COLON);
				setState(1791);
				nls();
				setState(1792);
				enhancedExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryContext namedPropertyArg() throws RecognitionException {
		MapEntryContext _localctx = new MapEntryContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_namedPropertyArg);
		try {
			setState(1806);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case BuiltInPrimitiveType:
			case ABSTRACT:
			case ASSERT:
			case BREAK:
			case CASE:
			case CATCH:
			case CLASS:
			case CONST:
			case CONTINUE:
			case DEFAULT:
			case DO:
			case ELSE:
			case ENUM:
			case EXTENDS:
			case FINAL:
			case FINALLY:
			case FOR:
			case IF:
			case GOTO:
			case IMPLEMENTS:
			case IMPORT:
			case INSTANCEOF:
			case INTERFACE:
			case MODULE:
			case NATIVE:
			case NEW:
			case NON_SEALED:
			case PACKAGE:
			case PERMITS:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RECORD:
			case RETURN:
			case SEALED:
			case STATIC:
			case STRICTFP:
			case SUPER:
			case SWITCH:
			case SYNCHRONIZED:
			case THIS:
			case THROW:
			case THROWS:
			case TRANSIENT:
			case TRY:
			case VAL:
			case VAR:
			case VOID:
			case VOLATILE:
			case WHILE:
			case YIELD:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
			case LPAREN:
			case LBRACK:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(1796);
				namedPropertyArgLabel();
				setState(1797);
				match(COLON);
				setState(1798);
				nls();
				setState(1799);
				enhancedExpression();
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1801);
				match(MUL);
				setState(1802);
				match(COLON);
				setState(1803);
				nls();
				setState(1804);
				enhancedExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryContext namedArg() throws RecognitionException {
		MapEntryContext _localctx = new MapEntryContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_namedArg);
		try {
			setState(1818);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case BuiltInPrimitiveType:
			case ABSTRACT:
			case ASSERT:
			case BREAK:
			case CASE:
			case CATCH:
			case CLASS:
			case CONST:
			case CONTINUE:
			case DEFAULT:
			case DO:
			case ELSE:
			case ENUM:
			case EXTENDS:
			case FINAL:
			case FINALLY:
			case FOR:
			case IF:
			case GOTO:
			case IMPLEMENTS:
			case IMPORT:
			case INSTANCEOF:
			case INTERFACE:
			case MODULE:
			case NATIVE:
			case NEW:
			case NON_SEALED:
			case PACKAGE:
			case PERMITS:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RECORD:
			case RETURN:
			case SEALED:
			case STATIC:
			case STRICTFP:
			case SUPER:
			case SWITCH:
			case SYNCHRONIZED:
			case THIS:
			case THROW:
			case THROWS:
			case TRANSIENT:
			case TRY:
			case VAL:
			case VAR:
			case VOID:
			case VOLATILE:
			case WHILE:
			case YIELD:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(1808);
				namedArgLabel();
				setState(1809);
				match(COLON);
				setState(1810);
				nls();
				setState(1811);
				enhancedExpression();
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1813);
				match(MUL);
				setState(1814);
				match(COLON);
				setState(1815);
				nls();
				setState(1816);
				enhancedExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MapEntryLabelContext extends GroovyParserRuleContext {
		public KeywordsContext keywords() {
			return getRuleContext(KeywordsContext.class,0);
		}
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public MapEntryLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapEntryLabel; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMapEntryLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final MapEntryLabelContext mapEntryLabel() throws RecognitionException {
		MapEntryLabelContext _localctx = new MapEntryLabelContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_mapEntryLabel);
		try {
			setState(1822);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1820);
				keywords();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1821);
				primary();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryLabelContext namedPropertyArgLabel() throws RecognitionException {
		MapEntryLabelContext _localctx = new MapEntryLabelContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_namedPropertyArgLabel);
		try {
			setState(1826);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,193,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1824);
				keywords();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1825);
				namedPropertyArgPrimary();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryLabelContext namedArgLabel() throws RecognitionException {
		MapEntryLabelContext _localctx = new MapEntryLabelContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_namedArgLabel);
		try {
			setState(1830);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1828);
				keywords();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1829);
				namedArgPrimary();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreatorContext extends GroovyParserRuleContext {
		public int t;
		public CreatedNameContext createdName() {
			return getRuleContext(CreatedNameContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public AnonymousInnerClassDeclarationContext anonymousInnerClassDeclaration() {
			return getRuleContext(AnonymousInnerClassDeclarationContext.class,0);
		}
		public List<? extends Dim0Context> dim0() {
			return getRuleContexts(Dim0Context.class);
		}
		public Dim0Context dim0(int i) {
			return getRuleContext(Dim0Context.class,i);
		}
		public List<? extends Dim1Context> dim1() {
			return getRuleContexts(Dim1Context.class);
		}
		public Dim1Context dim1(int i) {
			return getRuleContext(Dim1Context.class,i);
		}
		public CreatorContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public CreatorContext(ParserRuleContext parent, int invokingState, int t) {
			super(parent, invokingState);
			this.t = t;
		}
		@Override public int getRuleIndex() { return RULE_creator; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCreator(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CreatorContext creator(int t) throws RecognitionException {
		CreatorContext _localctx = new CreatorContext(_ctx, getState(), t);
		enterRule(_localctx, 292, RULE_creator);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1832);
			createdName();
			setState(1857);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,199,_ctx) ) {
			case 1:
				{
				setState(1833);
				nls();
				setState(1834);
				arguments();
				setState(1836);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
				case 1:
					{
					setState(1835);
					anonymousInnerClassDeclaration(0);
					}
					break;
				}
				}
				break;
			case 2:
				{
				setState(1839); 
				_errHandler.sync(this);
				do {
					{
					{
					setState(1838);
					dim0();
					}
					}
					setState(1841); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==LBRACK || _la==AT );
				setState(1843);
				nls();
				setState(1844);
				arrayInitializer();
				}
				break;
			case 3:
				{
				setState(1847); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1846);
						dim1();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1849); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,197,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1854);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1851);
						dim0();
						}
						} 
					}
					setState(1856);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Dim0Context extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public Dim0Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dim0; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitDim0(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final Dim0Context dim0() throws RecognitionException {
		Dim0Context _localctx = new Dim0Context(_ctx, getState());
		enterRule(_localctx, 294, RULE_dim0);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1859);
			annotationsOpt();
			setState(1860);
			match(LBRACK);
			setState(1861);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Dim1Context extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public Dim1Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dim1; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitDim1(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final Dim1Context dim1() throws RecognitionException {
		Dim1Context _localctx = new Dim1Context(_ctx, getState());
		enterRule(_localctx, 296, RULE_dim1);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1863);
			annotationsOpt();
			setState(1864);
			match(LBRACK);
			setState(1865);
			expression(0);
			setState(1866);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayInitializerContext extends GroovyParserRuleContext {
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends ArrayInitializerContext> arrayInitializer() {
			return getRuleContexts(ArrayInitializerContext.class);
		}
		public ArrayInitializerContext arrayInitializer(int i) {
			return getRuleContext(ArrayInitializerContext.class,i);
		}
		public List<? extends VariableInitializerContext> variableInitializer() {
			return getRuleContexts(VariableInitializerContext.class);
		}
		public VariableInitializerContext variableInitializer(int i) {
			return getRuleContext(VariableInitializerContext.class,i);
		}
		public ArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayInitializer; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitArrayInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ArrayInitializerContext arrayInitializer() throws RecognitionException {
		ArrayInitializerContext _localctx = new ArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1868);
			match(LBRACE);
			setState(1869);
			nls();
			setState(1888);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,203,_ctx) ) {
			case 1:
				{
				setState(1872);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
				case 1:
					{
					setState(1870);
					arrayInitializer();
					}
					break;
				case 2:
					{
					setState(1871);
					variableInitializer();
					}
					break;
				}
				setState(1874);
				nls();
				setState(1885);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,202,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1875);
						match(COMMA);
						setState(1876);
						nls();
						setState(1879);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,201,_ctx) ) {
						case 1:
							{
							setState(1877);
							arrayInitializer();
							}
							break;
						case 2:
							{
							setState(1878);
							variableInitializer();
							}
							break;
						}
						setState(1881);
						nls();
						}
						} 
					}
					setState(1887);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,202,_ctx);
				}
				}
				break;
			}
			setState(1891);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1890);
				match(COMMA);
				}
			}

			setState(1893);
			nls();
			setState(1894);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnonymousInnerClassDeclarationContext extends GroovyParserRuleContext {
		public int t;
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public AnonymousInnerClassDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public AnonymousInnerClassDeclarationContext(ParserRuleContext parent, int invokingState, int t) {
			super(parent, invokingState);
			this.t = t;
		}
		@Override public int getRuleIndex() { return RULE_anonymousInnerClassDeclaration; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAnonymousInnerClassDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final AnonymousInnerClassDeclarationContext anonymousInnerClassDeclaration(int t) throws RecognitionException {
		AnonymousInnerClassDeclarationContext _localctx = new AnonymousInnerClassDeclarationContext(_ctx, getState(), t);
		enterRule(_localctx, 300, RULE_anonymousInnerClassDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1896);
			classBody(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreatedNameContext extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public QualifiedClassNameContext qualifiedClassName() {
			return getRuleContext(QualifiedClassNameContext.class,0);
		}
		public TypeArgumentsOrDiamondContext typeArgumentsOrDiamond() {
			return getRuleContext(TypeArgumentsOrDiamondContext.class,0);
		}
		public CreatedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createdName; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitCreatedName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final CreatedNameContext createdName() throws RecognitionException {
		CreatedNameContext _localctx = new CreatedNameContext(_ctx, getState());
		enterRule(_localctx, 302, RULE_createdName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1898);
			annotationsOpt();
			setState(1904);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BuiltInPrimitiveType:
				{
				setState(1899);
				primitiveType();
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case ASYNC:
			case AWAIT:
			case DEFER:
			case MODULE:
			case PERMITS:
			case RECORD:
			case SEALED:
			case VAL:
			case VAR:
			case YIELD:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(1900);
				qualifiedClassName();
				setState(1902);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(1901);
					typeArgumentsOrDiamond();
					}
				}

				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonWildcardTypeArgumentsContext extends GroovyParserRuleContext {
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public NonWildcardTypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonWildcardTypeArguments; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitNonWildcardTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final NonWildcardTypeArgumentsContext nonWildcardTypeArguments() throws RecognitionException {
		NonWildcardTypeArgumentsContext _localctx = new NonWildcardTypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1906);
			match(LT);
			setState(1907);
			nls();
			setState(1908);
			typeList();
			setState(1909);
			nls();
			setState(1910);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeArgumentsOrDiamondContext extends GroovyParserRuleContext {
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public TypeArgumentsOrDiamondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArgumentsOrDiamond; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeArgumentsOrDiamond(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final TypeArgumentsOrDiamondContext typeArgumentsOrDiamond() throws RecognitionException {
		TypeArgumentsOrDiamondContext _localctx = new TypeArgumentsOrDiamondContext(_ctx, getState());
		enterRule(_localctx, 306, RULE_typeArgumentsOrDiamond);
		try {
			setState(1915);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1912);
				match(LT);
				setState(1913);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1914);
				typeArguments();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArgumentsContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public EnhancedArgumentListInParContext enhancedArgumentListInPar() {
			return getRuleContext(EnhancedArgumentListInParContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(GroovyParser.COMMA, 0); }
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 308, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1917);
			match(LPAREN);
			setState(1919);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
			case 1:
				{
				setState(1918);
				enhancedArgumentListInPar();
				}
				break;
			}
			setState(1922);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1921);
				match(COMMA);
				}
			}

			setState(1924);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final EnhancedArgumentListInParContext argumentList() throws RecognitionException {
		EnhancedArgumentListInParContext _localctx = new EnhancedArgumentListInParContext(_ctx, getState());
		enterRule(_localctx, 310, RULE_argumentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1926);
			firstArgumentListElement();
			setState(1933);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,210,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1927);
					match(COMMA);
					setState(1928);
					nls();
					setState(1929);
					argumentListElement();
					}
					} 
				}
				setState(1935);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,210,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnhancedArgumentListInParContext extends GroovyParserRuleContext {
		public List<? extends EnhancedArgumentListElementContext> enhancedArgumentListElement() {
			return getRuleContexts(EnhancedArgumentListElementContext.class);
		}
		public EnhancedArgumentListElementContext enhancedArgumentListElement(int i) {
			return getRuleContext(EnhancedArgumentListElementContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public EnhancedArgumentListInParContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enhancedArgumentListInPar; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEnhancedArgumentListInPar(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EnhancedArgumentListInParContext enhancedArgumentListInPar() throws RecognitionException {
		EnhancedArgumentListInParContext _localctx = new EnhancedArgumentListInParContext(_ctx, getState());
		enterRule(_localctx, 312, RULE_enhancedArgumentListInPar);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1936);
			enhancedArgumentListElement();
			setState(1943);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,211,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1937);
					match(COMMA);
					setState(1938);
					nls();
					setState(1939);
					enhancedArgumentListElement();
					}
					} 
				}
				setState(1945);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,211,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final EnhancedArgumentListElementContext firstArgumentListElement() throws RecognitionException {
		EnhancedArgumentListElementContext _localctx = new EnhancedArgumentListElementContext(_ctx, getState());
		enterRule(_localctx, 314, RULE_firstArgumentListElement);
		try {
			setState(1948);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,212,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1946);
				expressionListElement(true);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1947);
				namedArg();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final EnhancedArgumentListElementContext argumentListElement() throws RecognitionException {
		EnhancedArgumentListElementContext _localctx = new EnhancedArgumentListElementContext(_ctx, getState());
		enterRule(_localctx, 316, RULE_argumentListElement);
		try {
			setState(1952);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1950);
				expressionListElement(true);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1951);
				namedPropertyArg();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnhancedArgumentListElementContext extends GroovyParserRuleContext {
		public ExpressionListElementContext expressionListElement() {
			return getRuleContext(ExpressionListElementContext.class,0);
		}
		public MapEntryContext mapEntry() {
			return getRuleContext(MapEntryContext.class,0);
		}
		public StandardLambdaExpressionContext standardLambdaExpression() {
			return getRuleContext(StandardLambdaExpressionContext.class,0);
		}
		public EnhancedArgumentListElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enhancedArgumentListElement; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEnhancedArgumentListElement(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EnhancedArgumentListElementContext enhancedArgumentListElement() throws RecognitionException {
		EnhancedArgumentListElementContext _localctx = new EnhancedArgumentListElementContext(_ctx, getState());
		enterRule(_localctx, 318, RULE_enhancedArgumentListElement);
		try {
			setState(1957);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,214,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1954);
				expressionListElement(true);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1955);
				standardLambdaExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1956);
				namedPropertyArg();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringLiteralContext extends GroovyParserRuleContext {
		public TerminalNode StringLiteral() { return getToken(GroovyParser.StringLiteral, 0); }
		public StringLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiteral; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final StringLiteralContext stringLiteral() throws RecognitionException {
		StringLiteralContext _localctx = new StringLiteralContext(_ctx, getState());
		enterRule(_localctx, 320, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1959);
			match(StringLiteral);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassNameContext extends GroovyParserRuleContext {
		public TerminalNode CapitalizedIdentifier() { return getToken(GroovyParser.CapitalizedIdentifier, 0); }
		public ClassNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_className; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassName(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassNameContext className() throws RecognitionException {
		ClassNameContext _localctx = new ClassNameContext(_ctx, getState());
		enterRule(_localctx, 322, RULE_className);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1961);
			match(CapitalizedIdentifier);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierContext extends GroovyParserRuleContext {
		public TerminalNode Identifier() { return getToken(GroovyParser.Identifier, 0); }
		public TerminalNode CapitalizedIdentifier() { return getToken(GroovyParser.CapitalizedIdentifier, 0); }
		public TerminalNode AS() { return getToken(GroovyParser.AS, 0); }
		public TerminalNode ASYNC() { return getToken(GroovyParser.ASYNC, 0); }
		public TerminalNode AWAIT() { return getToken(GroovyParser.AWAIT, 0); }
		public TerminalNode DEFER() { return getToken(GroovyParser.DEFER, 0); }
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
		public TerminalNode MODULE() { return getToken(GroovyParser.MODULE, 0); }
		public TerminalNode PERMITS() { return getToken(GroovyParser.PERMITS, 0); }
		public TerminalNode RECORD() { return getToken(GroovyParser.RECORD, 0); }
		public TerminalNode SEALED() { return getToken(GroovyParser.SEALED, 0); }
		public TerminalNode TRAIT() { return getToken(GroovyParser.TRAIT, 0); }
		public TerminalNode VAL() { return getToken(GroovyParser.VAL, 0); }
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public TerminalNode YIELD() { return getToken(GroovyParser.YIELD, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 324, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1963);
			_la = _input.LA(1);
			if ( !(((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) ) {
			_errHandler.recoverInline(this);
			} else {
				if (_input.LA(1) == Token.EOF) {
					matchedEOF = true;
				}

				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BuiltInTypeContext extends GroovyParserRuleContext {
		public TerminalNode BuiltInPrimitiveType() { return getToken(GroovyParser.BuiltInPrimitiveType, 0); }
		public TerminalNode VOID() { return getToken(GroovyParser.VOID, 0); }
		public BuiltInTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_builtInType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBuiltInType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final BuiltInTypeContext builtInType() throws RecognitionException {
		BuiltInTypeContext _localctx = new BuiltInTypeContext(_ctx, getState());
		enterRule(_localctx, 326, RULE_builtInType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1965);
			_la = _input.LA(1);
			if ( !(_la==BuiltInPrimitiveType || _la==VOID) ) {
			_errHandler.recoverInline(this);
			} else {
				if (_input.LA(1) == Token.EOF) {
					matchedEOF = true;
				}

				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeywordsContext extends GroovyParserRuleContext {
		public TerminalNode ABSTRACT() { return getToken(GroovyParser.ABSTRACT, 0); }
		public TerminalNode AS() { return getToken(GroovyParser.AS, 0); }
		public TerminalNode ASSERT() { return getToken(GroovyParser.ASSERT, 0); }
		public TerminalNode ASYNC() { return getToken(GroovyParser.ASYNC, 0); }
		public TerminalNode AWAIT() { return getToken(GroovyParser.AWAIT, 0); }
		public TerminalNode BREAK() { return getToken(GroovyParser.BREAK, 0); }
		public TerminalNode CASE() { return getToken(GroovyParser.CASE, 0); }
		public TerminalNode CATCH() { return getToken(GroovyParser.CATCH, 0); }
		public TerminalNode CLASS() { return getToken(GroovyParser.CLASS, 0); }
		public TerminalNode CONST() { return getToken(GroovyParser.CONST, 0); }
		public TerminalNode CONTINUE() { return getToken(GroovyParser.CONTINUE, 0); }
		public TerminalNode DEF() { return getToken(GroovyParser.DEF, 0); }
		public TerminalNode DEFAULT() { return getToken(GroovyParser.DEFAULT, 0); }
		public TerminalNode DEFER() { return getToken(GroovyParser.DEFER, 0); }
		public TerminalNode DO() { return getToken(GroovyParser.DO, 0); }
		public TerminalNode ELSE() { return getToken(GroovyParser.ELSE, 0); }
		public TerminalNode ENUM() { return getToken(GroovyParser.ENUM, 0); }
		public TerminalNode EXTENDS() { return getToken(GroovyParser.EXTENDS, 0); }
		public TerminalNode FINAL() { return getToken(GroovyParser.FINAL, 0); }
		public TerminalNode FINALLY() { return getToken(GroovyParser.FINALLY, 0); }
		public TerminalNode FOR() { return getToken(GroovyParser.FOR, 0); }
		public TerminalNode GOTO() { return getToken(GroovyParser.GOTO, 0); }
		public TerminalNode IF() { return getToken(GroovyParser.IF, 0); }
		public TerminalNode IMPLEMENTS() { return getToken(GroovyParser.IMPLEMENTS, 0); }
		public TerminalNode IMPORT() { return getToken(GroovyParser.IMPORT, 0); }
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
		public TerminalNode INSTANCEOF() { return getToken(GroovyParser.INSTANCEOF, 0); }
		public TerminalNode INTERFACE() { return getToken(GroovyParser.INTERFACE, 0); }
		public TerminalNode NATIVE() { return getToken(GroovyParser.NATIVE, 0); }
		public TerminalNode NEW() { return getToken(GroovyParser.NEW, 0); }
		public TerminalNode NON_SEALED() { return getToken(GroovyParser.NON_SEALED, 0); }
		public TerminalNode PACKAGE() { return getToken(GroovyParser.PACKAGE, 0); }
		public TerminalNode PERMITS() { return getToken(GroovyParser.PERMITS, 0); }
		public TerminalNode RECORD() { return getToken(GroovyParser.RECORD, 0); }
		public TerminalNode RETURN() { return getToken(GroovyParser.RETURN, 0); }
		public TerminalNode SEALED() { return getToken(GroovyParser.SEALED, 0); }
		public TerminalNode STATIC() { return getToken(GroovyParser.STATIC, 0); }
		public TerminalNode STRICTFP() { return getToken(GroovyParser.STRICTFP, 0); }
		public TerminalNode SUPER() { return getToken(GroovyParser.SUPER, 0); }
		public TerminalNode SWITCH() { return getToken(GroovyParser.SWITCH, 0); }
		public TerminalNode SYNCHRONIZED() { return getToken(GroovyParser.SYNCHRONIZED, 0); }
		public TerminalNode THIS() { return getToken(GroovyParser.THIS, 0); }
		public TerminalNode THROW() { return getToken(GroovyParser.THROW, 0); }
		public TerminalNode THROWS() { return getToken(GroovyParser.THROWS, 0); }
		public TerminalNode TRANSIENT() { return getToken(GroovyParser.TRANSIENT, 0); }
		public TerminalNode TRAIT() { return getToken(GroovyParser.TRAIT, 0); }
		public TerminalNode THREADSAFE() { return getToken(GroovyParser.THREADSAFE, 0); }
		public TerminalNode TRY() { return getToken(GroovyParser.TRY, 0); }
		public TerminalNode VAL() { return getToken(GroovyParser.VAL, 0); }
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public TerminalNode VOLATILE() { return getToken(GroovyParser.VOLATILE, 0); }
		public TerminalNode WHILE() { return getToken(GroovyParser.WHILE, 0); }
		public TerminalNode YIELD() { return getToken(GroovyParser.YIELD, 0); }
		public TerminalNode NullLiteral() { return getToken(GroovyParser.NullLiteral, 0); }
		public TerminalNode BooleanLiteral() { return getToken(GroovyParser.BooleanLiteral, 0); }
		public TerminalNode BuiltInPrimitiveType() { return getToken(GroovyParser.BuiltInPrimitiveType, 0); }
		public TerminalNode VOID() { return getToken(GroovyParser.VOID, 0); }
		public TerminalNode PUBLIC() { return getToken(GroovyParser.PUBLIC, 0); }
		public TerminalNode PROTECTED() { return getToken(GroovyParser.PROTECTED, 0); }
		public TerminalNode PRIVATE() { return getToken(GroovyParser.PRIVATE, 0); }
		public KeywordsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keywords; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitKeywords(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final KeywordsContext keywords() throws RecognitionException {
		KeywordsContext _localctx = new KeywordsContext(_ctx, getState());
		enterRule(_localctx, 328, RULE_keywords);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1967);
			_la = _input.LA(1);
			if ( !(((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (THREADSAFE - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (ABSTRACT - 7)) | (1L << (ASSERT - 7)) | (1L << (BREAK - 7)) | (1L << (CASE - 7)) | (1L << (CATCH - 7)) | (1L << (CLASS - 7)) | (1L << (CONST - 7)) | (1L << (CONTINUE - 7)) | (1L << (DEFAULT - 7)) | (1L << (DO - 7)) | (1L << (ELSE - 7)) | (1L << (ENUM - 7)) | (1L << (EXTENDS - 7)) | (1L << (FINAL - 7)) | (1L << (FINALLY - 7)) | (1L << (FOR - 7)) | (1L << (IF - 7)) | (1L << (GOTO - 7)) | (1L << (IMPLEMENTS - 7)) | (1L << (IMPORT - 7)) | (1L << (INSTANCEOF - 7)) | (1L << (INTERFACE - 7)) | (1L << (NATIVE - 7)) | (1L << (NEW - 7)) | (1L << (NON_SEALED - 7)) | (1L << (PACKAGE - 7)) | (1L << (PERMITS - 7)) | (1L << (PRIVATE - 7)) | (1L << (PROTECTED - 7)) | (1L << (PUBLIC - 7)) | (1L << (RECORD - 7)) | (1L << (RETURN - 7)) | (1L << (SEALED - 7)) | (1L << (STATIC - 7)) | (1L << (STRICTFP - 7)) | (1L << (SUPER - 7)) | (1L << (SWITCH - 7)) | (1L << (SYNCHRONIZED - 7)) | (1L << (THIS - 7)) | (1L << (THROW - 7)) | (1L << (THROWS - 7)) | (1L << (TRANSIENT - 7)) | (1L << (TRY - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (VOLATILE - 7)) | (1L << (WHILE - 7)) | (1L << (YIELD - 7)) | (1L << (BooleanLiteral - 7)) | (1L << (NullLiteral - 7)))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				if (_input.LA(1) == Token.EOF) {
					matchedEOF = true;
				}

				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NlsContext extends GroovyParserRuleContext {
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public NlsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nls; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitNls(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final NlsContext nls() throws RecognitionException {
		NlsContext _localctx = new NlsContext(_ctx, getState());
		enterRule(_localctx, 330, RULE_nls);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1972);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,215,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1969);
					match(NL);
					}
					} 
				}
				setState(1974);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,215,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SepContext extends GroovyParserRuleContext {
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public List<? extends TerminalNode> SEMI() { return getTokens(GroovyParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(GroovyParser.SEMI, i);
		}
		public SepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sep; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSep(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final SepContext sep() throws RecognitionException {
		SepContext _localctx = new SepContext(_ctx, getState());
		enterRule(_localctx, 332, RULE_sep);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1976); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1975);
					_la = _input.LA(1);
					if ( !(_la==SEMI || _la==NL) ) {
					_errHandler.recoverInline(this);
					} else {
						if (_input.LA(1) == Token.EOF) {
							matchedEOF = true;
						}

						_errHandler.reportMatch(this);
						consume();
					}
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1978); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,216,_ctx);
			} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 2:
			return scriptStatement_sempred((ScriptStatementContext)_localctx, predIndex);
		case 20:
			return classBody_sempred((ClassBodyContext)_localctx, predIndex);
		case 25:
			return methodDeclaration_sempred((MethodDeclarationContext)_localctx, predIndex);
		case 78:
			return localVariableDeclaration_sempred((LocalVariableDeclarationContext)_localctx, predIndex);
		case 93:
			return statement_sempred((StatementContext)_localctx, predIndex);
		case 122:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 124:
			return commandExpression_sempred((CommandExpressionContext)_localctx, predIndex);
		case 126:
			return pathExpression_sempred((PathExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean scriptStatement_sempred(ScriptStatementContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return  !SemanticPredicates.isInvalidMethodDeclaration(_input) ;
		}
		return true;
	}
	private boolean classBody_sempred(ClassBodyContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return  _localctx.t == 2 ;
		}
		return true;
	}
	private boolean methodDeclaration_sempred(MethodDeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return  _localctx.ct == 3 ;
		}
		return true;
	}
	private boolean localVariableDeclaration_sempred(LocalVariableDeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return  !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ;
		}
		return true;
	}
	private boolean statement_sempred(StatementContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return  inSwitchExpressionLevel > 0 ;
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return precpred(_ctx, 19);
		case 6:
			return precpred(_ctx, 17);
		case 7:
			return precpred(_ctx, 16);
		case 8:
			return precpred(_ctx, 15);
		case 9:
			return precpred(_ctx, 12);
		case 10:
			return precpred(_ctx, 11);
		case 11:
			return precpred(_ctx, 10);
		case 12:
			return precpred(_ctx, 9);
		case 13:
			return precpred(_ctx, 8);
		case 14:
			return precpred(_ctx, 7);
		case 15:
			return precpred(_ctx, 6);
		case 16:
			return precpred(_ctx, 5);
		case 17:
			return precpred(_ctx, 4);
		case 18:
			return precpred(_ctx, 3);
		case 19:
			return precpred(_ctx, 14);
		case 20:
			return precpred(_ctx, 13);
		case 21:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean commandExpression_sempred(CommandExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 22:
			return  !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) ;
		}
		return true;
	}
	private boolean pathExpression_sempred(PathExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 23:
			return  _input.LT(2).getType() == DOT ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\3\u0091\u07bf\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4g\tg\4h\th\4i\ti\4j\tj\4k\t"+
		"k\4l\tl\4m\tm\4n\tn\4o\to\4p\tp\4q\tq\4r\tr\4s\ts\4t\tt\4u\tu\4v\tv\4"+
		"w\tw\4x\tx\4y\ty\4z\tz\4{\t{\4|\t|\4}\t}\4~\t~\4\177\t\177\4\u0080\t\u0080"+
		"\4\u0081\t\u0081\4\u0082\t\u0082\4\u0083\t\u0083\4\u0084\t\u0084\4\u0085"+
		"\t\u0085\4\u0086\t\u0086\4\u0087\t\u0087\4\u0088\t\u0088\4\u0089\t\u0089"+
		"\4\u008a\t\u008a\4\u008b\t\u008b\4\u008c\t\u008c\4\u008d\t\u008d\4\u008e"+
		"\t\u008e\4\u008f\t\u008f\4\u0090\t\u0090\4\u0091\t\u0091\4\u0092\t\u0092"+
		"\4\u0093\t\u0093\4\u0094\t\u0094\4\u0095\t\u0095\4\u0096\t\u0096\4\u0097"+
		"\t\u0097\4\u0098\t\u0098\4\u0099\t\u0099\4\u009a\t\u009a\4\u009b\t\u009b"+
		"\4\u009c\t\u009c\4\u009d\t\u009d\4\u009e\t\u009e\4\u009f\t\u009f\4\u00a0"+
		"\t\u00a0\4\u00a1\t\u00a1\4\u00a2\t\u00a2\4\u00a3\t\u00a3\4\u00a4\t\u00a4"+
		"\4\u00a5\t\u00a5\4\u00a6\t\u00a6\4\u00a7\t\u00a7\4\u00a8\t\u00a8\3\2\3"+
		"\2\3\2\5\2\u0154\n\2\5\2\u0156\n\2\3\2\5\2\u0159\n\2\3\2\3\2\3\3\3\3\3"+
		"\3\3\3\7\3\u0161\n\3\f\3\16\3\u0164\13\3\3\3\5\3\u0167\n\3\3\4\3\4\3\4"+
		"\3\4\3\4\5\4\u016e\n\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\5\6\u0177\n\6\3\6\3"+
		"\6\3\6\3\6\3\6\5\6\u017e\n\6\3\6\3\6\3\6\3\6\3\6\5\6\u0185\n\6\3\7\3\7"+
		"\3\7\3\b\3\b\5\b\u018c\n\b\3\t\3\t\3\t\5\t\u0191\n\t\3\n\3\n\3\n\3\n\7"+
		"\n\u0197\n\n\f\n\16\n\u019a\13\n\3\13\3\13\7\13\u019e\n\13\f\13\16\13"+
		"\u01a1\13\13\5\13\u01a3\n\13\3\f\3\f\3\f\3\f\7\f\u01a9\n\f\f\f\16\f\u01ac"+
		"\13\f\3\r\3\r\5\r\u01b0\n\r\3\16\3\16\5\16\u01b4\n\16\3\17\3\17\3\17\5"+
		"\17\u01b9\n\17\3\20\3\20\3\20\3\20\7\20\u01bf\n\20\f\20\16\20\u01c2\13"+
		"\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\7\21\u01cb\n\21\f\21\16\21\u01ce"+
		"\13\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u01d9\n\22\3"+
		"\23\3\23\3\23\3\23\3\23\7\23\u01e0\n\23\f\23\16\23\u01e3\13\23\3\24\3"+
		"\24\3\24\3\24\3\24\7\24\u01ea\n\24\f\24\16\24\u01ed\13\24\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u01fc\n\25"+
		"\3\25\3\25\3\25\3\25\5\25\u0202\n\25\3\25\3\25\3\25\5\25\u0207\n\25\3"+
		"\25\3\25\3\25\3\25\3\25\5\25\u020e\n\25\3\25\3\25\3\25\3\25\3\25\5\25"+
		"\u0215\n\25\3\25\3\25\3\25\3\25\3\25\5\25\u021c\n\25\3\25\3\25\3\25\3"+
		"\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u0228\n\26\3\26\3\26\3\26\5\26"+
		"\u022d\n\26\3\26\3\26\3\26\5\26\u0232\n\26\3\26\3\26\3\26\3\26\3\26\7"+
		"\26\u0239\n\26\f\26\16\26\u023c\13\26\5\26\u023e\n\26\3\26\3\26\3\26\3"+
		"\26\7\26\u0244\n\26\f\26\16\26\u0247\13\26\5\26\u0249\n\26\5\26\u024b"+
		"\n\26\3\26\5\26\u024e\n\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\7\27"+
		"\u0258\n\27\f\27\16\27\u025b\13\27\3\30\3\30\3\30\5\30\u0260\n\30\3\30"+
		"\5\30\u0263\n\30\3\31\3\31\5\31\u0267\n\31\3\31\3\31\5\31\u026b\n\31\3"+
		"\32\3\32\3\32\3\32\3\32\5\32\u0272\n\32\5\32\u0274\n\32\3\33\3\33\5\33"+
		"\u0278\n\33\3\33\3\33\3\33\5\33\u027d\n\33\3\33\3\33\3\33\3\33\3\33\3"+
		"\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u028d\n\33\3\33\3\33"+
		"\3\33\5\33\u0292\n\33\3\34\3\34\3\34\3\34\3\35\3\35\5\35\u029a\n\35\3"+
		"\36\3\36\5\36\u029e\n\36\3\37\3\37\3 \3 \3 \3 \3 \7 \u02a7\n \f \16 \u02aa"+
		"\13 \3!\3!\3!\3!\3!\3!\5!\u02b2\n!\3\"\3\"\3#\3#\3$\3$\3$\3$\5$\u02bc"+
		"\n$\3$\7$\u02bf\n$\f$\16$\u02c2\13$\3%\3%\3&\3&\5&\u02c8\n&\3\'\3\'\5"+
		"\'\u02cc\n\'\3(\3(\3(\5(\u02d1\n(\3(\7(\u02d4\n(\f(\16(\u02d7\13(\3)\3"+
		")\5)\u02db\n)\3*\3*\3*\3*\3*\3*\3*\7*\u02e4\n*\f*\16*\u02e7\13*\3*\3*"+
		"\3*\3+\3+\3+\3+\3+\3+\3+\5+\u02f3\n+\5+\u02f5\n+\3,\3,\3,\3-\3-\3-\3-"+
		"\3-\7-\u02ff\n-\f-\16-\u0302\13-\3.\3.\5.\u0306\n.\3.\3.\3/\3/\5/\u030c"+
		"\n/\3/\3/\3/\3/\7/\u0312\n/\f/\16/\u0315\13/\3\60\3\60\3\60\3\61\3\61"+
		"\5\61\u031c\n\61\3\61\5\61\u031f\n\61\3\61\3\61\3\61\3\61\3\61\3\61\5"+
		"\61\u0327\n\61\3\62\3\62\3\63\3\63\3\63\7\63\u032e\n\63\f\63\16\63\u0331"+
		"\13\63\3\64\3\64\3\64\3\64\3\64\5\64\u0338\n\64\3\65\3\65\3\65\7\65\u033d"+
		"\n\65\f\65\16\65\u0340\13\65\3\66\3\66\3\66\3\67\3\67\3\67\3\67\7\67\u0349"+
		"\n\67\f\67\16\67\u034c\13\67\38\38\38\38\38\58\u0353\n8\39\39\39\39\7"+
		"9\u0359\n9\f9\169\u035c\139\39\39\3:\3:\5:\u0362\n:\3;\3;\7;\u0366\n;"+
		"\f;\16;\u0369\13;\3<\3<\3<\3<\3<\3<\3=\3=\3=\3=\3=\3=\3>\3>\3?\3?\5?\u037b"+
		"\n?\3@\3@\5@\u037f\n@\3A\3A\3A\3A\3A\5A\u0386\nA\3A\3A\5A\u038a\nA\3A"+
		"\5A\u038d\nA\3A\3A\3A\3B\3B\5B\u0394\nB\3C\5C\u0397\nC\3D\3D\3D\3D\7D"+
		"\u039d\nD\fD\16D\u03a0\13D\3D\5D\u03a3\nD\3E\3E\3E\3E\7E\u03a9\nE\fE\16"+
		"E\u03ac\13E\3E\3E\5E\u03b0\nE\3F\3F\3F\3F\3F\5F\u03b7\nF\3F\3F\5F\u03bb"+
		"\nF\3G\3G\5G\u03bf\nG\3H\3H\3I\3I\3I\7I\u03c6\nI\fI\16I\u03c9\13I\3J\3"+
		"J\3J\3J\3J\3J\3K\3K\5K\u03d3\nK\3L\3L\3L\5L\u03d8\nL\3M\3M\3M\3M\7M\u03de"+
		"\nM\fM\16M\u03e1\13M\3M\5M\u03e4\nM\5M\u03e6\nM\3M\3M\3M\3M\3M\6M\u03ed"+
		"\nM\rM\16M\u03ee\3M\5M\u03f2\nM\3M\3M\5M\u03f6\nM\3N\3N\5N\u03fa\nN\3"+
		"N\3N\3N\3O\3O\5O\u0401\nO\3P\3P\3P\3Q\3Q\3Q\5Q\u0409\nQ\3Q\3Q\3Q\3Q\3"+
		"Q\3Q\3Q\5Q\u0412\nQ\3Q\3Q\3Q\5Q\u0417\nQ\3R\3R\3R\3R\7R\u041d\nR\fR\16"+
		"R\u0420\13R\3R\3R\3R\3R\3R\3R\7R\u0428\nR\fR\16R\u042b\13R\3R\3R\5R\u042f"+
		"\nR\3S\3S\3S\3S\5S\u0435\nS\3S\5S\u0438\nS\3S\3S\3T\3T\3T\3T\3T\3T\5T"+
		"\u0442\nT\3T\3T\3U\3U\3U\3U\6U\u044a\nU\rU\16U\u044b\3U\3U\3V\3V\5V\u0452"+
		"\nV\3W\3W\3W\3W\3W\3W\5W\u045a\nW\3W\3W\3W\3W\5W\u0460\nW\3X\3X\3X\3X"+
		"\3X\3X\6X\u0468\nX\rX\16X\u0469\3X\3X\5X\u046e\nX\3X\3X\3Y\3Y\3Y\5Y\u0475"+
		"\nY\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\5Y\u048b"+
		"\nY\3Z\3Z\5Z\u048f\nZ\3[\3[\5[\u0493\n[\3\\\3\\\3\\\3]\3]\5]\u049a\n]"+
		"\3]\3]\3]\3]\3]\7]\u04a1\n]\f]\16]\u04a4\13]\3]\3]\3]\5]\u04a9\n]\3^\3"+
		"^\3^\3^\3^\3^\3^\5^\u04b2\n^\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\5_\u04bf"+
		"\n_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_"+
		"\3_\3_\5_\u04d9\n_\3`\3`\3`\3`\5`\u04df\n`\3`\3`\3`\3`\3`\3a\3a\3a\7a"+
		"\u04e9\na\fa\16a\u04ec\13a\3b\3b\3b\3b\3c\3c\3c\3c\5c\u04f6\nc\3c\3c\3"+
		"d\3d\3d\3d\7d\u04fe\nd\fd\16d\u0501\13d\3e\3e\5e\u0505\ne\3f\3f\3f\3f"+
		"\7f\u050b\nf\ff\16f\u050e\13f\3f\3f\3f\3g\3g\3g\3g\3g\3g\5g\u0519\ng\3"+
		"h\3h\5h\u051d\nh\3i\3i\3i\5i\u0522\ni\3i\3i\5i\u0526\ni\3i\3i\3i\3i\3"+
		"j\5j\u052d\nj\3j\3j\3k\5k\u0532\nk\3k\3k\5k\u0536\nk\3k\3k\5k\u053a\n"+
		"k\3l\3l\5l\u053e\nl\3m\3m\3n\3n\3n\3n\3o\3o\3o\3o\3o\7o\u054b\no\fo\16"+
		"o\u054e\13o\3p\3p\5p\u0552\np\3q\3q\3r\3r\3r\3r\3s\3s\3s\3s\3s\7s\u055f"+
		"\ns\fs\16s\u0562\13s\3t\5t\u0565\nt\3t\3t\3u\3u\5u\u056b\nu\3v\3v\5v\u056f"+
		"\nv\3w\3w\3x\3x\5x\u0575\nx\3y\3y\3y\3y\3y\3y\7y\u057d\ny\fy\16y\u0580"+
		"\13y\3y\3y\3y\3z\3z\3z\6z\u0588\nz\rz\16z\u0589\3z\3z\3{\3{\3{\5{\u0591"+
		"\n{\3{\3{\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\7|\u05a5\n|"+
		"\f|\16|\u05a8\13|\3|\3|\3|\3|\3|\3|\3|\7|\u05b1\n|\f|\16|\u05b4\13|\5"+
		"|\u05b6\n|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\5|\u05c6\n|\3|\3"+
		"|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3"+
		"|\5|\u05e1\n|\3|\5|\u05e4\n|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3"+
		"|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3"+
		"|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3"+
		"|\3|\3|\3|\3|\3|\3|\3|\3|\5|\u062a\n|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3|\3"+
		"|\3|\3|\3|\3|\3|\3|\3|\3|\3|\7|\u0640\n|\f|\16|\u0643\13|\3}\3}\3}\3}"+
		"\3}\3}\3}\3}\3}\3}\5}\u064f\n}\3~\3~\3~\3~\5~\u0655\n~\3~\7~\u0658\n~"+
		"\f~\16~\u065b\13~\3\177\3\177\6\177\u065f\n\177\r\177\16\177\u0660\3\177"+
		"\5\177\u0664\n\177\3\u0080\3\u0080\3\u0080\5\u0080\u0669\n\u0080\3\u0080"+
		"\3\u0080\3\u0080\7\u0080\u066e\n\u0080\f\u0080\16\u0080\u0671\13\u0080"+
		"\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081"+
		"\3\u0081\3\u0081\5\u0081\u067e\n\u0081\3\u0081\3\u0081\3\u0081\3\u0081"+
		"\3\u0081\5\u0081\u0685\n\u0081\5\u0081\u0687\n\u0081\3\u0081\3\u0081\3"+
		"\u0081\3\u0081\3\u0081\3\u0081\5\u0081\u068f\n\u0081\3\u0081\3\u0081\3"+
		"\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\5\u0081\u069a\n"+
		"\u0081\3\u0082\3\u0082\3\u0082\3\u0082\5\u0082\u06a0\n\u0082\3\u0083\3"+
		"\u0083\5\u0083\u06a4\n\u0083\3\u0084\3\u0084\5\u0084\u06a8\n\u0084\3\u0084"+
		"\3\u0084\3\u0085\3\u0085\3\u0085\5\u0085\u06af\n\u0085\3\u0085\3\u0085"+
		"\3\u0086\3\u0086\5\u0086\u06b5\n\u0086\3\u0086\3\u0086\3\u0086\3\u0086"+
		"\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086\3\u0086"+
		"\5\u0086\u06c4\n\u0086\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087"+
		"\5\u0087\u06cc\n\u0087\3\u0088\3\u0088\3\u0088\5\u0088\u06d1\n\u0088\3"+
		"\u0089\3\u0089\3\u0089\5\u0089\u06d6\n\u0089\3\u008a\3\u008a\5\u008a\u06da"+
		"\n\u008a\3\u008a\5\u008a\u06dd\n\u008a\3\u008a\3\u008a\3\u008b\3\u008b"+
		"\3\u008b\5\u008b\u06e4\n\u008b\3\u008b\5\u008b\u06e7\n\u008b\3\u008b\3"+
		"\u008b\3\u008c\3\u008c\3\u008c\7\u008c\u06ee\n\u008c\f\u008c\16\u008c"+
		"\u06f1\13\u008c\3\u008d\3\u008d\3\u008d\7\u008d\u06f6\n\u008d\f\u008d"+
		"\16\u008d\u06f9\13\u008d\3\u008e\3\u008e\3\u008e\3\u008e\3\u008e\3\u008e"+
		"\3\u008e\3\u008e\3\u008e\3\u008e\5\u008e\u0705\n\u008e\3\u008f\3\u008f"+
		"\3\u008f\3\u008f\3\u008f\3\u008f\3\u008f\3\u008f\3\u008f\3\u008f\5\u008f"+
		"\u0711\n\u008f\3\u0090\3\u0090\3\u0090\3\u0090\3\u0090\3\u0090\3\u0090"+
		"\3\u0090\3\u0090\3\u0090\5\u0090\u071d\n\u0090\3\u0091\3\u0091\5\u0091"+
		"\u0721\n\u0091\3\u0092\3\u0092\5\u0092\u0725\n\u0092\3\u0093\3\u0093\5"+
		"\u0093\u0729\n\u0093\3\u0094\3\u0094\3\u0094\3\u0094\5\u0094\u072f\n\u0094"+
		"\3\u0094\6\u0094\u0732\n\u0094\r\u0094\16\u0094\u0733\3\u0094\3\u0094"+
		"\3\u0094\3\u0094\6\u0094\u073a\n\u0094\r\u0094\16\u0094\u073b\3\u0094"+
		"\7\u0094\u073f\n\u0094\f\u0094\16\u0094\u0742\13\u0094\5\u0094\u0744\n"+
		"\u0094\3\u0095\3\u0095\3\u0095\3\u0095\3\u0096\3\u0096\3\u0096\3\u0096"+
		"\3\u0096\3\u0097\3\u0097\3\u0097\3\u0097\5\u0097\u0753\n\u0097\3\u0097"+
		"\3\u0097\3\u0097\3\u0097\3\u0097\5\u0097\u075a\n\u0097\3\u0097\3\u0097"+
		"\7\u0097\u075e\n\u0097\f\u0097\16\u0097\u0761\13\u0097\5\u0097\u0763\n"+
		"\u0097\3\u0097\5\u0097\u0766\n\u0097\3\u0097\3\u0097\3\u0097\3\u0098\3"+
		"\u0098\3\u0099\3\u0099\3\u0099\3\u0099\5\u0099\u0771\n\u0099\5\u0099\u0773"+
		"\n\u0099\3\u009a\3\u009a\3\u009a\3\u009a\3\u009a\3\u009a\3\u009b\3\u009b"+
		"\3\u009b\5\u009b\u077e\n\u009b\3\u009c\3\u009c\5\u009c\u0782\n\u009c\3"+
		"\u009c\5\u009c\u0785\n\u009c\3\u009c\3\u009c\3\u009d\3\u009d\3\u009d\3"+
		"\u009d\3\u009d\7\u009d\u078e\n\u009d\f\u009d\16\u009d\u0791\13\u009d\3"+
		"\u009e\3\u009e\3\u009e\3\u009e\3\u009e\7\u009e\u0798\n\u009e\f\u009e\16"+
		"\u009e\u079b\13\u009e\3\u009f\3\u009f\5\u009f\u079f\n\u009f\3\u00a0\3"+
		"\u00a0\5\u00a0\u07a3\n\u00a0\3\u00a1\3\u00a1\3\u00a1\5\u00a1\u07a8\n\u00a1"+
		"\3\u00a2\3\u00a2\3\u00a3\3\u00a3\3\u00a4\3\u00a4\3\u00a5\3\u00a5\3\u00a6"+
		"\3\u00a6\3\u00a7\7\u00a7\u07b5\n\u00a7\f\u00a7\16\u00a7\u07b8\13\u00a7"+
		"\3\u00a8\6\u00a8\u07bb\n\u00a8\r\u00a8\16\u00a8\u07bc\3\u00a8\2\2\3\u00f6"+
		"\u00a9\2\2\4\2\6\2\b\2\n\2\f\2\16\2\20\2\22\2\24\2\26\2\30\2\32\2\34\2"+
		"\36\2 \2\"\2$\2&\2(\2*\2,\2.\2\60\2\62\2\64\2\66\28\2:\2<\2>\2@\2B\2D"+
		"\2F\2H\2J\2L\2N\2P\2R\2T\2V\2X\2Z\2\\\2^\2`\2b\2d\2f\2h\2j\2l\2n\2p\2"+
		"r\2t\2v\2x\2z\2|\2~\2\u0080\2\u0082\2\u0084\2\u0086\2\u0088\2\u008a\2"+
		"\u008c\2\u008e\2\u0090\2\u0092\2\u0094\2\u0096\2\u0098\2\u009a\2\u009c"+
		"\2\u009e\2\u00a0\2\u00a2\2\u00a4\2\u00a6\2\u00a8\2\u00aa\2\u00ac\2\u00ae"+
		"\2\u00b0\2\u00b2\2\u00b4\2\u00b6\2\u00b8\2\u00ba\2\u00bc\2\u00be\2\u00c0"+
		"\2\u00c2\2\u00c4\2\u00c6\2\u00c8\2\u00ca\2\u00cc\2\u00ce\2\u00d0\2\u00d2"+
		"\2\u00d4\2\u00d6\2\u00d8\2\u00da\2\u00dc\2\u00de\2\u00e0\2\u00e2\2\u00e4"+
		"\2\u00e6\2\u00e8\2\u00ea\2\u00ec\2\u00ee\2\u00f0\2\u00f2\2\u00f4\2\u00f6"+
		"\2\u00f8\2\u00fa\2\u00fc\2\u00fe\2\u0100\2\u0102\2\u0104\2\u0106\2\u0108"+
		"\2\u010a\2\u010c\2\u010e\2\u0110\2\u0112\2\u0114\2\u0116\2\u0118\2\u011a"+
		"\2\u011c\2\u011e\2\u0120\2\u0122\2\u0124\2\u0126\2\u0128\2\u012a\2\u012c"+
		"\2\u012e\2\u0130\2\u0132\2\u0134\2\u0136\2\u0138\2\u013a\2\u013c\2\u013e"+
		"\2\u0140\2\u0142\2\u0144\2\u0146\2\u0148\2\u014a\2\u014c\2\u014e\2\2\33"+
		"\b\2\n\n))88<<>?AA\b\2\22\22\32\32\37\37++.\60\63\65\b\2\n\n\22\22\37"+
		"\37.\60\64\65>?\4\2\36\36\66\66\4\2eemm\4\2\13\13mm\5\2\n\n\21\21>?\3"+
		"\2tu\4\2[[mm\3\2jk\3\2tw\4\2xy}}\3\2vw\3\2HK\6\2\13\13]]hiop\6\2WXZZn"+
		"nqq\3\2ST\4\2\t\t\\\\\5\2VVgg~\u0089\5\2LMOOff\4\2NNbb\f\2\t\t\13\f\16"+
		"\20((--\61\61\63\63>?CC\u008a\u008b\4\2\21\21@@\5\2\t\')CFG\4\2dd\u008f"+
		"\u008f\2\u0849\2\u0150\3\2\2\2\4\u015c\3\2\2\2\6\u016d\3\2\2\2\b\u016f"+
		"\3\2\2\2\n\u0184\3\2\2\2\f\u0186\3\2\2\2\16\u018b\3\2\2\2\20\u0190\3\2"+
		"\2\2\22\u0192\3\2\2\2\24\u01a2\3\2\2\2\26\u01a4\3\2\2\2\30\u01af\3\2\2"+
		"\2\32\u01b3\3\2\2\2\34\u01b8\3\2\2\2\36\u01ba\3\2\2\2 \u01c3\3\2\2\2\""+
		"\u01d2\3\2\2\2$\u01da\3\2\2\2&\u01e4\3\2\2\2(\u01fb\3\2\2\2*\u0220\3\2"+
		"\2\2,\u0251\3\2\2\2.\u025c\3\2\2\2\60\u026a\3\2\2\2\62\u0273\3\2\2\2\64"+
		"\u0275\3\2\2\2\66\u0293\3\2\2\28\u0299\3\2\2\2:\u029d\3\2\2\2<\u029f\3"+
		"\2\2\2>\u02a1\3\2\2\2@\u02ab\3\2\2\2B\u02b3\3\2\2\2D\u02b5\3\2\2\2F\u02b7"+
		"\3\2\2\2H\u02c3\3\2\2\2J\u02c5\3\2\2\2L\u02c9\3\2\2\2N\u02cd\3\2\2\2P"+
		"\u02d8\3\2\2\2R\u02dc\3\2\2\2T\u02f4\3\2\2\2V\u02f6\3\2\2\2X\u02f9\3\2"+
		"\2\2Z\u0303\3\2\2\2\\\u030b\3\2\2\2^\u0316\3\2\2\2`\u0319\3\2\2\2b\u0328"+
		"\3\2\2\2d\u032a\3\2\2\2f\u0337\3\2\2\2h\u033e\3\2\2\2j\u0341\3\2\2\2l"+
		"\u0344\3\2\2\2n\u0352\3\2\2\2p\u0354\3\2\2\2r\u0361\3\2\2\2t\u0363\3\2"+
		"\2\2v\u036a\3\2\2\2x\u0370\3\2\2\2z\u0376\3\2\2\2|\u037a\3\2\2\2~\u037e"+
		"\3\2\2\2\u0080\u0380\3\2\2\2\u0082\u0393\3\2\2\2\u0084\u0396\3\2\2\2\u0086"+
		"\u0398\3\2\2\2\u0088\u03af\3\2\2\2\u008a\u03b1\3\2\2\2\u008c\u03be\3\2"+
		"\2\2\u008e\u03c0\3\2\2\2\u0090\u03c2\3\2\2\2\u0092\u03ca\3\2\2\2\u0094"+
		"\u03d2\3\2\2\2\u0096\u03d7\3\2\2\2\u0098\u03f5\3\2\2\2\u009a\u03f7\3\2"+
		"\2\2\u009c\u0400\3\2\2\2\u009e\u0402\3\2\2\2\u00a0\u0416\3\2\2\2\u00a2"+
		"\u042e\3\2\2\2\u00a4\u0434\3\2\2\2\u00a6\u043b\3\2\2\2\u00a8\u0445\3\2"+
		"\2\2\u00aa\u0451\3\2\2\2\u00ac\u0453\3\2\2\2\u00ae\u0461\3\2\2\2\u00b0"+
		"\u048a\3\2\2\2\u00b2\u048c\3\2\2\2\u00b4\u0490\3\2\2\2\u00b6\u0494\3\2"+
		"\2\2\u00b8\u0497\3\2\2\2\u00ba\u04aa\3\2\2\2\u00bc\u04d8\3\2\2\2\u00be"+
		"\u04da\3\2\2\2\u00c0\u04e5\3\2\2\2\u00c2\u04ed\3\2\2\2\u00c4\u04f1\3\2"+
		"\2\2\u00c6\u04f9\3\2\2\2\u00c8\u0504\3\2\2\2\u00ca\u0506\3\2\2\2\u00cc"+
		"\u0518\3\2\2\2\u00ce\u051c\3\2\2\2\u00d0\u0521\3\2\2\2\u00d2\u052c\3\2"+
		"\2\2\u00d4\u0531\3\2\2\2\u00d6\u053d\3\2\2\2\u00d8\u053f\3\2\2\2\u00da"+
		"\u0541\3\2\2\2\u00dc\u0545\3\2\2\2\u00de\u0551\3\2\2\2\u00e0\u0553\3\2"+
		"\2\2\u00e2\u0555\3\2\2\2\u00e4\u0559\3\2\2\2\u00e6\u0564\3\2\2\2\u00e8"+
		"\u056a\3\2\2\2\u00ea\u056e\3\2\2\2\u00ec\u0570\3\2\2\2\u00ee\u0572\3\2"+
		"\2\2\u00f0\u0576\3\2\2\2\u00f2\u0587\3\2\2\2\u00f4\u0590\3\2\2\2\u00f6"+
		"\u05c5\3\2\2\2\u00f8\u064e\3\2\2\2\u00fa\u0650\3\2\2\2\u00fc\u065c\3\2"+
		"\2\2\u00fe\u0668\3\2\2\2\u0100\u0699\3\2\2\2\u0102\u069f\3\2\2\2\u0104"+
		"\u06a3\3\2\2\2\u0106\u06a5\3\2\2\2\u0108\u06ab\3\2\2\2\u010a\u06c3\3\2"+
		"\2\2\u010c\u06cb\3\2\2\2\u010e\u06d0\3\2\2\2\u0110\u06d5\3\2\2\2\u0112"+
		"\u06d7\3\2\2\2\u0114\u06e0\3\2\2\2\u0116\u06ea\3\2\2\2\u0118\u06f2\3\2"+
		"\2\2\u011a\u0704\3\2\2\2\u011c\u0710\3\2\2\2\u011e\u071c\3\2\2\2\u0120"+
		"\u0720\3\2\2\2\u0122\u0724\3\2\2\2\u0124\u0728\3\2\2\2\u0126\u072a\3\2"+
		"\2\2\u0128\u0745\3\2\2\2\u012a\u0749\3\2\2\2\u012c\u074e\3\2\2\2\u012e"+
		"\u076a\3\2\2\2\u0130\u076c\3\2\2\2\u0132\u0774\3\2\2\2\u0134\u077d\3\2"+
		"\2\2\u0136\u077f\3\2\2\2\u0138\u0788\3\2\2\2\u013a\u0792\3\2\2\2\u013c"+
		"\u079e\3\2\2\2\u013e\u07a2\3\2\2\2\u0140\u07a7\3\2\2\2\u0142\u07a9\3\2"+
		"\2\2\u0144\u07ab\3\2\2\2\u0146\u07ad\3\2\2\2\u0148\u07af\3\2\2\2\u014a"+
		"\u07b1\3\2\2\2\u014c\u07b6\3\2\2\2\u014e\u07ba\3\2\2\2\u0150\u0155\5\u014c"+
		"\u00a7\2\u0151\u0153\5\b\5\2\u0152\u0154\5\u014e\u00a8\2\u0153\u0152\3"+
		"\2\2\2\u0153\u0154\3\2\2\2\u0154\u0156\3\2\2\2\u0155\u0151\3\2\2\2\u0155"+
		"\u0156\3\2\2\2\u0156\u0158\3\2\2\2\u0157\u0159\5\4\3\2\u0158\u0157\3\2"+
		"\2\2\u0158\u0159\3\2\2\2\u0159\u015a\3\2\2\2\u015a\u015b\7\2\2\3\u015b"+
		"\3\3\2\2\2\u015c\u0162\5\6\4\2\u015d\u015e\5\u014e\u00a8\2\u015e\u015f"+
		"\5\6\4\2\u015f\u0161\3\2\2\2\u0160\u015d\3\2\2\2\u0161\u0164\3\2\2\2\u0162"+
		"\u0160\3\2\2\2\u0162\u0163\3\2\2\2\u0163\u0166\3\2\2\2\u0164\u0162\3\2"+
		"\2\2\u0165\u0167\5\u014e\u00a8\2\u0166\u0165\3\2\2\2\u0166\u0167\3\2\2"+
		"\2\u0167\5\3\2\2\2\u0168\u016e\5\n\6\2\u0169\u016e\5\f\7\2\u016a\u016b"+
		"\6\4\2\2\u016b\u016e\5\64\33\2\u016c\u016e\5\u00bc_\2\u016d\u0168\3\2"+
		"\2\2\u016d\u0169\3\2\2\2\u016d\u016a\3\2\2\2\u016d\u016c\3\2\2\2\u016e"+
		"\7\3\2\2\2\u016f\u0170\5\u0088E\2\u0170\u0171\7,\2\2\u0171\u0172\5d\63"+
		"\2\u0172\t\3\2\2\2\u0173\u0174\5\u0088E\2\u0174\u0176\7%\2\2\u0175\u0177"+
		"\7\64\2\2\u0176\u0175\3\2\2\2\u0176\u0177\3\2\2\2\u0177\u0178\3\2\2\2"+
		"\u0178\u017d\5d\63\2\u0179\u017a\7f\2\2\u017a\u017e\7x\2\2\u017b\u017c"+
		"\7\t\2\2\u017c\u017e\5\u0146\u00a4\2\u017d\u0179\3\2\2\2\u017d\u017b\3"+
		"\2\2\2\u017d\u017e\3\2\2\2\u017e\u0185\3\2\2\2\u017f\u0180\5\u0088E\2"+
		"\u0180\u0181\7%\2\2\u0181\u0182\7(\2\2\u0182\u0183\5d\63\2\u0183\u0185"+
		"\3\2\2\2\u0184\u0173\3\2\2\2\u0184\u017f\3\2\2\2\u0185\13\3\2\2\2\u0186"+
		"\u0187\5\24\13\2\u0187\u0188\5(\25\2\u0188\r\3\2\2\2\u0189\u018c\5\30"+
		"\r\2\u018a\u018c\t\2\2\2\u018b\u0189\3\2\2\2\u018b\u018a\3\2\2\2\u018c"+
		"\17\3\2\2\2\u018d\u018e\5\22\n\2\u018e\u018f\5\u014c\u00a7\2\u018f\u0191"+
		"\3\2\2\2\u0190\u018d\3\2\2\2\u0190\u0191\3\2\2\2\u0191\21\3\2\2\2\u0192"+
		"\u0198\5\16\b\2\u0193\u0194\5\u014c\u00a7\2\u0194\u0195\5\16\b\2\u0195"+
		"\u0197\3\2\2\2\u0196\u0193\3\2\2\2\u0197\u019a\3\2\2\2\u0198\u0196\3\2"+
		"\2\2\u0198\u0199\3\2\2\2\u0199\23\3\2\2\2\u019a\u0198\3\2\2\2\u019b\u019f"+
		"\5\26\f\2\u019c\u019e\7\u008f\2\2\u019d\u019c\3\2\2\2\u019e\u01a1\3\2"+
		"\2\2\u019f\u019d\3\2\2\2\u019f\u01a0\3\2\2\2\u01a0\u01a3\3\2\2\2\u01a1"+
		"\u019f\3\2\2\2\u01a2\u019b\3\2\2\2\u01a2\u01a3\3\2\2\2\u01a3\25\3\2\2"+
		"\2\u01a4\u01aa\5\30\r\2\u01a5\u01a6\5\u014c\u00a7\2\u01a6\u01a7\5\30\r"+
		"\2\u01a7\u01a9\3\2\2\2\u01a8\u01a5\3\2\2\2\u01a9\u01ac\3\2\2\2\u01aa\u01a8"+
		"\3\2\2\2\u01aa\u01ab\3\2\2\2\u01ab\27\3\2\2\2\u01ac\u01aa\3\2\2\2\u01ad"+
		"\u01b0\5\u008aF\2\u01ae\u01b0\t\3\2\2\u01af\u01ad\3\2\2\2\u01af\u01ae"+
		"\3\2\2\2\u01b0\31\3\2\2\2\u01b1\u01b4\5\u008aF\2\u01b2\u01b4\t\4\2\2\u01b3"+
		"\u01b1\3\2\2\2\u01b3\u01b2\3\2\2\2\u01b4\33\3\2\2\2\u01b5\u01b6\5\36\20"+
		"\2\u01b6\u01b7\5\u014c\u00a7\2\u01b7\u01b9\3\2\2\2\u01b8\u01b5\3\2\2\2"+
		"\u01b8\u01b9\3\2\2\2\u01b9\35\3\2\2\2\u01ba\u01c0\5\32\16\2\u01bb\u01bc"+
		"\5\u014c\u00a7\2\u01bc\u01bd\5\32\16\2\u01bd\u01bf\3\2\2\2\u01be\u01bb"+
		"\3\2\2\2\u01bf\u01c2\3\2\2\2\u01c0\u01be\3\2\2\2\u01c0\u01c1\3\2\2\2\u01c1"+
		"\37\3\2\2\2\u01c2\u01c0\3\2\2\2\u01c3\u01c4\7i\2\2\u01c4\u01c5\5\u014c"+
		"\u00a7\2\u01c5\u01cc\5\"\22\2\u01c6\u01c7\7e\2\2\u01c7\u01c8\5\u014c\u00a7"+
		"\2\u01c8\u01c9\5\"\22\2\u01c9\u01cb\3\2\2\2\u01ca\u01c6\3\2\2\2\u01cb"+
		"\u01ce\3\2\2\2\u01cc\u01ca\3\2\2\2\u01cc\u01cd\3\2\2\2\u01cd\u01cf\3\2"+
		"\2\2\u01ce\u01cc\3\2\2\2\u01cf\u01d0\5\u014c\u00a7\2\u01d0\u01d1\7h\2"+
		"\2\u01d1!\3\2\2\2\u01d2\u01d3\5\u0088E\2\u01d3\u01d8\5\u0144\u00a3\2\u01d4"+
		"\u01d5\7\36\2\2\u01d5\u01d6\5\u014c\u00a7\2\u01d6\u01d7\5$\23\2\u01d7"+
		"\u01d9\3\2\2\2\u01d8\u01d4\3\2\2\2\u01d8\u01d9\3\2\2\2\u01d9#\3\2\2\2"+
		"\u01da\u01e1\5F$\2\u01db\u01dc\7z\2\2\u01dc\u01dd\5\u014c\u00a7\2\u01dd"+
		"\u01de\5F$\2\u01de\u01e0\3\2\2\2\u01df\u01db\3\2\2\2\u01e0\u01e3\3\2\2"+
		"\2\u01e1\u01df\3\2\2\2\u01e1\u01e2\3\2\2\2\u01e2%\3\2\2\2\u01e3\u01e1"+
		"\3\2\2\2\u01e4\u01eb\5F$\2\u01e5\u01e6\7e\2\2\u01e6\u01e7\5\u014c\u00a7"+
		"\2\u01e7\u01e8\5F$\2\u01e8\u01ea\3\2\2\2\u01e9\u01e5\3\2\2\2\u01ea\u01ed"+
		"\3\2\2\2\u01eb\u01e9\3\2\2\2\u01eb\u01ec\3\2\2\2\u01ec\'\3\2\2\2\u01ed"+
		"\u01eb\3\2\2\2\u01ee\u01ef\7\27\2\2\u01ef\u01fc\b\25\1\2\u01f0\u01f1\7"+
		"\'\2\2\u01f1\u01fc\b\25\1\2\u01f2\u01f3\7\35\2\2\u01f3\u01fc\b\25\1\2"+
		"\u01f4\u01f5\7\u008c\2\2\u01f5\u01f6\7\'\2\2\u01f6\u01fc\b\25\1\2\u01f7"+
		"\u01f8\7\f\2\2\u01f8\u01fc\b\25\1\2\u01f9\u01fa\7\61\2\2\u01fa\u01fc\b"+
		"\25\1\2\u01fb\u01ee\3\2\2\2\u01fb\u01f0\3\2\2\2\u01fb\u01f2\3\2\2\2\u01fb"+
		"\u01f4\3\2\2\2\u01fb\u01f7\3\2\2\2\u01fb\u01f9\3\2\2\2\u01fc\u01fd\3\2"+
		"\2\2\u01fd\u0201\5\u0146\u00a4\2\u01fe\u01ff\5\u014c\u00a7\2\u01ff\u0200"+
		"\5 \21\2\u0200\u0202\3\2\2\2\u0201\u01fe\3\2\2\2\u0201\u0202\3\2\2\2\u0202"+
		"\u0206\3\2\2\2\u0203\u0204\5\u014c\u00a7\2\u0204\u0205\5Z.\2\u0205\u0207"+
		"\3\2\2\2\u0206\u0203\3\2\2\2\u0206\u0207\3\2\2\2\u0207\u020d\3\2\2\2\u0208"+
		"\u0209\5\u014c\u00a7\2\u0209\u020a\7\36\2\2\u020a\u020b\5\u014c\u00a7"+
		"\2\u020b\u020c\5&\24\2\u020c\u020e\3\2\2\2\u020d\u0208\3\2\2\2\u020d\u020e"+
		"\3\2\2\2\u020e\u0214\3\2\2\2\u020f\u0210\5\u014c\u00a7\2\u0210\u0211\7"+
		"$\2\2\u0211\u0212\5\u014c\u00a7\2\u0212\u0213\5&\24\2\u0213\u0215\3\2"+
		"\2\2\u0214\u020f\3\2\2\2\u0214\u0215\3\2\2\2\u0215\u021b\3\2\2\2\u0216"+
		"\u0217\5\u014c\u00a7\2\u0217\u0218\7-\2\2\u0218\u0219\5\u014c\u00a7\2"+
		"\u0219\u021a\5&\24\2\u021a\u021c\3\2\2\2\u021b\u0216\3\2\2\2\u021b\u021c"+
		"\3\2\2\2\u021c\u021d\3\2\2\2\u021d\u021e\5\u014c\u00a7\2\u021e\u021f\5"+
		"*\26\2\u021f)\3\2\2\2\u0220\u0221\7`\2\2\u0221\u024a\5\u014c\u00a7\2\u0222"+
		"\u0223\6\26\3\3\u0223\u023d\5,\27\2\u0224\u0225\5\u014c\u00a7\2\u0225"+
		"\u0226\7e\2\2\u0226\u0228\3\2\2\2\u0227\u0224\3\2\2\2\u0227\u0228\3\2"+
		"\2\2\u0228\u023e\3\2\2\2\u0229\u022a\5\u014c\u00a7\2\u022a\u022b\7e\2"+
		"\2\u022b\u022d\3\2\2\2\u022c\u0229\3\2\2\2\u022c\u022d\3\2\2\2\u022d\u022e"+
		"\3\2\2\2\u022e\u022f\5\u014c\u00a7\2\u022f\u0230\7d\2\2\u0230\u0232\3"+
		"\2\2\2\u0231\u022c\3\2\2\2\u0231\u0232\3\2\2\2\u0232\u0233\3\2\2\2\u0233"+
		"\u0234\5\u014c\u00a7\2\u0234\u023a\5\60\31\2\u0235\u0236\5\u014e\u00a8"+
		"\2\u0236\u0237\5\60\31\2\u0237\u0239\3\2\2\2\u0238\u0235\3\2\2\2\u0239"+
		"\u023c\3\2\2\2\u023a\u0238\3\2\2\2\u023a\u023b\3\2\2\2\u023b\u023e\3\2"+
		"\2\2\u023c\u023a\3\2\2\2\u023d\u0227\3\2\2\2\u023d\u0231\3\2\2\2\u023e"+
		"\u024b\3\2\2\2\u023f\u0245\5\60\31\2\u0240\u0241\5\u014e\u00a8\2\u0241"+
		"\u0242\5\60\31\2\u0242\u0244\3\2\2\2\u0243\u0240\3\2\2\2\u0244\u0247\3"+
		"\2\2\2\u0245\u0243\3\2\2\2\u0245\u0246\3\2\2\2\u0246\u0249\3\2\2\2\u0247"+
		"\u0245\3\2\2\2\u0248\u023f\3\2\2\2\u0248\u0249\3\2\2\2\u0249\u024b\3\2"+
		"\2\2\u024a\u0222\3\2\2\2\u024a\u0248\3\2\2\2\u024b\u024d\3\2\2\2\u024c"+
		"\u024e\5\u014e\u00a8\2\u024d\u024c\3\2\2\2\u024d\u024e\3\2\2\2\u024e\u024f"+
		"\3\2\2\2\u024f\u0250\7a\2\2\u0250+\3\2\2\2\u0251\u0259\5.\30\2\u0252\u0253"+
		"\5\u014c\u00a7\2\u0253\u0254\7e\2\2\u0254\u0255\5\u014c\u00a7\2\u0255"+
		"\u0256\5.\30\2\u0256\u0258\3\2\2\2\u0257\u0252\3\2\2\2\u0258\u025b\3\2"+
		"\2\2\u0259\u0257\3\2\2\2\u0259\u025a\3\2\2\2\u025a-\3\2\2\2\u025b\u0259"+
		"\3\2\2\2\u025c\u025d\5\u0088E\2\u025d\u025f\5\u0146\u00a4\2\u025e\u0260"+
		"\5\u0136\u009c\2\u025f\u025e\3\2\2\2\u025f\u0260\3\2\2\2\u0260\u0262\3"+
		"\2\2\2\u0261\u0263\5\u012e\u0098\2\u0262\u0261\3\2\2\2\u0262\u0263\3\2"+
		"\2\2\u0263/\3\2\2\2\u0264\u0265\7\64\2\2\u0265\u0267\5\u014c\u00a7\2\u0266"+
		"\u0264\3\2\2\2\u0266\u0267\3\2\2\2\u0267\u0268\3\2\2\2\u0268\u026b\5\u009a"+
		"N\2\u0269\u026b\5\62\32\2\u026a\u0266\3\2\2\2\u026a\u0269\3\2\2\2\u026b"+
		"\61\3\2\2\2\u026c\u0274\5\64\33\2\u026d\u0274\5<\37\2\u026e\u0271\5\20"+
		"\t\2\u026f\u0272\5(\25\2\u0270\u0272\5\66\34\2\u0271\u026f\3\2\2\2\u0271"+
		"\u0270\3\2\2\2\u0272\u0274\3\2\2\2\u0273\u026c\3\2\2\2\u0273\u026d\3\2"+
		"\2\2\u0273\u026e\3\2\2\2\u0274\63\3\2\2\2\u0275\u0277\5\20\t\2\u0276\u0278"+
		"\5 \21\2\u0277\u0276\3\2\2\2\u0277\u0278\3\2\2\2\u0278\u027c\3\2\2\2\u0279"+
		"\u027a\5:\36\2\u027a\u027b\5\u014c\u00a7\2\u027b\u027d\3\2\2\2\u027c\u0279"+
		"\3\2\2\2\u027c\u027d\3\2\2\2\u027d\u027e\3\2\2\2\u027e\u027f\58\35\2\u027f"+
		"\u0291\5Z.\2\u0280\u0281\6\33\4\3\u0281\u0282\7\32\2\2\u0282\u0283\5\u014c"+
		"\u00a7\2\u0283\u0284\5\u0096L\2\u0284\u0292\3\2\2\2\u0285\u0286\5\u014c"+
		"\u00a7\2\u0286\u0287\7;\2\2\u0287\u0288\5\u014c\u00a7\2\u0288\u028c\5"+
		"X-\2\u0289\u028a\5\u014c\u00a7\2\u028a\u028b\5b\62\2\u028b\u028d\3\2\2"+
		"\2\u028c\u0289\3\2\2\2\u028c\u028d\3\2\2\2\u028d\u0292\3\2\2\2\u028e\u028f"+
		"\5\u014c\u00a7\2\u028f\u0290\5b\62\2\u0290\u0292\3\2\2\2\u0291\u0280\3"+
		"\2\2\2\u0291\u0285\3\2\2\2\u0291\u028e\3\2\2\2\u0291\u0292\3\2\2\2\u0292"+
		"\65\3\2\2\2\u0293\u0294\58\35\2\u0294\u0295\5\u014c\u00a7\2\u0295\u0296"+
		"\5b\62\2\u0296\67\3\2\2\2\u0297\u029a\5\u0146\u00a4\2\u0298\u029a\5\u0142"+
		"\u00a2\2\u0299\u0297\3\2\2\2\u0299\u0298\3\2\2\2\u029a9\3\2\2\2\u029b"+
		"\u029e\5N(\2\u029c\u029e\7@\2\2\u029d\u029b\3\2\2\2\u029d\u029c\3\2\2"+
		"\2\u029e;\3\2\2\2\u029f\u02a0\5\u00a0Q\2\u02a0=\3\2\2\2\u02a1\u02a8\5"+
		"@!\2\u02a2\u02a3\7e\2\2\u02a3\u02a4\5\u014c\u00a7\2\u02a4\u02a5\5@!\2"+
		"\u02a5\u02a7\3\2\2\2\u02a6\u02a2\3\2\2\2\u02a7\u02aa\3\2\2\2\u02a8\u02a6"+
		"\3\2\2\2\u02a8\u02a9\3\2\2\2\u02a9?\3\2\2\2\u02aa\u02a8\3\2\2\2\u02ab"+
		"\u02b1\5B\"\2\u02ac\u02ad\5\u014c\u00a7\2\u02ad\u02ae\7g\2\2\u02ae\u02af"+
		"\5\u014c\u00a7\2\u02af\u02b0\5D#\2\u02b0\u02b2\3\2\2\2\u02b1\u02ac\3\2"+
		"\2\2\u02b1\u02b2\3\2\2\2\u02b2A\3\2\2\2\u02b3\u02b4\5\u0146\u00a4\2\u02b4"+
		"C\3\2\2\2\u02b5\u02b6\5\u00eav\2\u02b6E\3\2\2\2\u02b7\u02bb\5\u0088E\2"+
		"\u02b8\u02bc\7@\2\2\u02b9\u02bc\5H%\2\u02ba\u02bc\5J&\2\u02bb\u02b8\3"+
		"\2\2\2\u02bb\u02b9\3\2\2\2\u02bb\u02ba\3\2\2\2\u02bc\u02c0\3\2\2\2\u02bd"+
		"\u02bf\5\u0128\u0095\2\u02be\u02bd\3\2\2\2\u02bf\u02c2\3\2\2\2\u02c0\u02be"+
		"\3\2\2\2\u02c0\u02c1\3\2\2\2\u02c1G\3\2\2\2\u02c2\u02c0\3\2\2\2\u02c3"+
		"\u02c4\7\21\2\2\u02c4I\3\2\2\2\u02c5\u02c7\5j\66\2\u02c6\u02c8\5R*\2\u02c7"+
		"\u02c6\3\2\2\2\u02c7\u02c8\3\2\2\2\u02c8K\3\2\2\2\u02c9\u02cb\5N(\2\u02ca"+
		"\u02cc\5\u0146\u00a4\2\u02cb\u02ca\3\2\2\2\u02cb\u02cc\3\2\2\2\u02ccM"+
		"\3\2\2\2\u02cd\u02d0\5\u0088E\2\u02ce\u02d1\5H%\2\u02cf\u02d1\5P)\2\u02d0"+
		"\u02ce\3\2\2\2\u02d0\u02cf\3\2\2\2\u02d1\u02d5\3\2\2\2\u02d2\u02d4\5\u0128"+
		"\u0095\2\u02d3\u02d2\3\2\2\2\u02d4\u02d7\3\2\2\2\u02d5\u02d3\3\2\2\2\u02d5"+
		"\u02d6\3\2\2\2\u02d6O\3\2\2\2\u02d7\u02d5\3\2\2\2\u02d8\u02da\5l\67\2"+
		"\u02d9\u02db\5R*\2\u02da\u02d9\3\2\2\2\u02da\u02db\3\2\2\2\u02dbQ\3\2"+
		"\2\2\u02dc\u02dd\7i\2\2\u02dd\u02de\5\u014c\u00a7\2\u02de\u02e5\5T+\2"+
		"\u02df\u02e0\7e\2\2\u02e0\u02e1\5\u014c\u00a7\2\u02e1\u02e2\5T+\2\u02e2"+
		"\u02e4\3\2\2\2\u02e3\u02df\3\2\2\2\u02e4\u02e7\3\2\2\2\u02e5\u02e3\3\2"+
		"\2\2\u02e5\u02e6\3\2\2\2\u02e6\u02e8\3\2\2\2\u02e7\u02e5\3\2\2\2\u02e8"+
		"\u02e9\5\u014c\u00a7\2\u02e9\u02ea\7h\2\2\u02eaS\3\2\2\2\u02eb\u02f5\5"+
		"F$\2\u02ec\u02ed\5\u0088E\2\u02ed\u02f2\7l\2\2\u02ee\u02ef\t\5\2\2\u02ef"+
		"\u02f0\5\u014c\u00a7\2\u02f0\u02f1\5F$\2\u02f1\u02f3\3\2\2\2\u02f2\u02ee"+
		"\3\2\2\2\u02f2\u02f3\3\2\2\2\u02f3\u02f5\3\2\2\2\u02f4\u02eb\3\2\2\2\u02f4"+
		"\u02ec\3\2\2\2\u02f5U\3\2\2\2\u02f6\u02f7\5\u0088E\2\u02f7\u02f8\5j\66"+
		"\2\u02f8W\3\2\2\2\u02f9\u0300\5V,\2\u02fa\u02fb\7e\2\2\u02fb\u02fc\5\u014c"+
		"\u00a7\2\u02fc\u02fd\5V,\2\u02fd\u02ff\3\2\2\2\u02fe\u02fa\3\2\2\2\u02ff"+
		"\u0302\3\2\2\2\u0300\u02fe\3\2\2\2\u0300\u0301\3\2\2\2\u0301Y\3\2\2\2"+
		"\u0302\u0300\3\2\2\2\u0303\u0305\7^\2\2\u0304\u0306\5\\/\2\u0305\u0304"+
		"\3\2\2\2\u0305\u0306\3\2\2\2\u0306\u0307\3\2\2\2\u0307\u0308\7_\2\2\u0308"+
		"[\3\2\2\2\u0309\u030c\5`\61\2\u030a\u030c\5^\60\2\u030b\u0309\3\2\2\2"+
		"\u030b\u030a\3\2\2\2\u030c\u0313\3\2\2\2\u030d\u030e\7e\2\2\u030e\u030f"+
		"\5\u014c\u00a7\2\u030f\u0310\5`\61\2\u0310\u0312\3\2\2\2\u0311\u030d\3"+
		"\2\2\2\u0312\u0315\3\2\2\2\u0313\u0311\3\2\2\2\u0313\u0314\3\2\2\2\u0314"+
		"]\3\2\2\2\u0315\u0313\3\2\2\2\u0316\u0317\5F$\2\u0317\u0318\79\2\2\u0318"+
		"_\3\2\2\2\u0319\u031b\5\34\17\2\u031a\u031c\5F$\2\u031b\u031a\3\2\2\2"+
		"\u031b\u031c\3\2\2\2\u031c\u031e\3\2\2\2\u031d\u031f\7\u008d\2\2\u031e"+
		"\u031d\3\2\2\2\u031e\u031f\3\2\2\2\u031f\u0320\3\2\2\2\u0320\u0326\5B"+
		"\"\2\u0321\u0322\5\u014c\u00a7\2\u0322\u0323\7g\2\2\u0323\u0324\5\u014c"+
		"\u00a7\2\u0324\u0325\5\u00f6|\2\u0325\u0327\3\2\2\2\u0326\u0321\3\2\2"+
		"\2\u0326\u0327\3\2\2\2\u0327a\3\2\2\2\u0328\u0329\5\u009aN\2\u0329c\3"+
		"\2\2\2\u032a\u032f\5f\64\2\u032b\u032c\7f\2\2\u032c\u032e\5f\64\2\u032d"+
		"\u032b\3\2\2\2\u032e\u0331\3\2\2\2\u032f\u032d\3\2\2\2\u032f\u0330\3\2"+
		"\2\2\u0330e\3\2\2\2\u0331\u032f\3\2\2\2\u0332\u0338\5\u0146\u00a4\2\u0333"+
		"\u0338\7\n\2\2\u0334\u0338\7\13\2\2\u0335\u0338\7\t\2\2\u0336\u0338\7"+
		"\f\2\2\u0337\u0332\3\2\2\2\u0337\u0333\3\2\2\2\u0337\u0334\3\2\2\2\u0337"+
		"\u0335\3\2\2\2\u0337\u0336\3\2\2\2\u0338g\3\2\2\2\u0339\u033a\5f\64\2"+
		"\u033a\u033b\7f\2\2\u033b\u033d\3\2\2\2\u033c\u0339\3\2\2\2\u033d\u0340"+
		"\3\2\2\2\u033e\u033c\3\2\2\2\u033e\u033f\3\2\2\2\u033fi\3\2\2\2\u0340"+
		"\u033e\3\2\2\2\u0341\u0342\5h\65\2\u0342\u0343\5\u0146\u00a4\2\u0343k"+
		"\3\2\2\2\u0344\u0345\5h\65\2\u0345\u034a\5\u0144\u00a3\2\u0346\u0347\7"+
		"f\2\2\u0347\u0349\5\u0144\u00a3\2\u0348\u0346\3\2\2\2\u0349\u034c\3\2"+
		"\2\2\u034a\u0348\3\2\2\2\u034a\u034b\3\2\2\2\u034bm\3\2\2\2\u034c\u034a"+
		"\3\2\2\2\u034d\u0353\7D\2\2\u034e\u0353\7E\2\2\u034f\u0353\5\u0142\u00a2"+
		"\2\u0350\u0353\7F\2\2\u0351\u0353\7G\2\2\u0352\u034d\3\2\2\2\u0352\u034e"+
		"\3\2\2\2\u0352\u034f\3\2\2\2\u0352\u0350\3\2\2\2\u0352\u0351\3\2\2\2\u0353"+
		"o\3\2\2\2\u0354\u0355\7\4\2\2\u0355\u035a\5r:\2\u0356\u0357\7\6\2\2\u0357"+
		"\u0359\5r:\2\u0358\u0356\3\2\2\2\u0359\u035c\3\2\2\2\u035a\u0358\3\2\2"+
		"\2\u035a\u035b\3\2\2\2\u035b\u035d\3\2\2\2\u035c\u035a\3\2\2\2\u035d\u035e"+
		"\7\5\2\2\u035eq\3\2\2\2\u035f\u0362\5t;\2\u0360\u0362\5\u0080A\2\u0361"+
		"\u035f\3\2\2\2\u0361\u0360\3\2\2\2\u0362s\3\2\2\2\u0363\u0367\5\u0146"+
		"\u00a4\2\u0364\u0366\7\7\2\2\u0365\u0364\3\2\2\2\u0366\u0369\3\2\2\2\u0367"+
		"\u0365\3\2\2\2\u0367\u0368\3\2\2\2\u0368u\3\2\2\2\u0369\u0367\3\2\2\2"+
		"\u036a\u036b\5z>\2\u036b\u036c\5\u014c\u00a7\2\u036c\u036d\7[\2\2\u036d"+
		"\u036e\5\u014c\u00a7\2\u036e\u036f\5~@\2\u036fw\3\2\2\2\u0370\u0371\5"+
		"|?\2\u0371\u0372\5\u014c\u00a7\2\u0372\u0373\7[\2\2\u0373\u0374\5\u014c"+
		"\u00a7\2\u0374\u0375\5~@\2\u0375y\3\2\2\2\u0376\u0377\5Z.\2\u0377{\3\2"+
		"\2\2\u0378\u037b\5Z.\2\u0379\u037b\5B\"\2\u037a\u0378\3\2\2\2\u037a\u0379"+
		"\3\2\2\2\u037b}\3\2\2\2\u037c\u037f\5\u009aN\2\u037d\u037f\5\u00ecw\2"+
		"\u037e\u037c\3\2\2\2\u037e\u037d\3\2\2\2\u037f\177\3\2\2\2\u0380\u0389"+
		"\7`\2\2\u0381\u0385\5\u014c\u00a7\2\u0382\u0383\5\\/\2\u0383\u0384\5\u014c"+
		"\u00a7\2\u0384\u0386\3\2\2\2\u0385\u0382\3\2\2\2\u0385\u0386\3\2\2\2\u0386"+
		"\u0387\3\2\2\2\u0387\u0388\7[\2\2\u0388\u038a\3\2\2\2\u0389\u0381\3\2"+
		"\2\2\u0389\u038a\3\2\2\2\u038a\u038c\3\2\2\2\u038b\u038d\5\u014e\u00a8"+
		"\2\u038c\u038b\3\2\2\2\u038c\u038d\3\2\2\2\u038d\u038e\3\2\2\2\u038e\u038f"+
		"\5\u0084C\2\u038f\u0390\7a\2\2\u0390\u0081\3\2\2\2\u0391\u0394\5\u0080"+
		"A\2\u0392\u0394\5v<\2\u0393\u0391\3\2\2\2\u0393\u0392\3\2\2\2\u0394\u0083"+
		"\3\2\2\2\u0395\u0397\5\u0086D\2\u0396\u0395\3\2\2\2\u0396\u0397\3\2\2"+
		"\2\u0397\u0085\3\2\2\2\u0398\u039e\5\u009cO\2\u0399\u039a\5\u014e\u00a8"+
		"\2\u039a\u039b\5\u009cO\2\u039b\u039d\3\2\2\2\u039c\u0399\3\2\2\2\u039d"+
		"\u03a0\3\2\2\2\u039e\u039c\3\2\2\2\u039e\u039f\3\2\2\2\u039f\u03a2\3\2"+
		"\2\2\u03a0\u039e\3\2\2\2\u03a1\u03a3\5\u014e\u00a8\2\u03a2\u03a1\3\2\2"+
		"\2\u03a2\u03a3\3\2\2\2\u03a3\u0087\3\2\2\2\u03a4\u03aa\5\u008aF\2\u03a5"+
		"\u03a6\5\u014c\u00a7\2\u03a6\u03a7\5\u008aF\2\u03a7\u03a9\3\2\2\2\u03a8"+
		"\u03a5\3\2\2\2\u03a9\u03ac\3\2\2\2\u03aa\u03a8\3\2\2\2\u03aa\u03ab\3\2"+
		"\2\2\u03ab\u03ad\3\2\2\2\u03ac\u03aa\3\2\2\2\u03ad\u03ae\5\u014c\u00a7"+
		"\2\u03ae\u03b0\3\2\2\2\u03af\u03a4\3\2\2\2\u03af\u03b0\3\2\2\2\u03b0\u0089"+
		"\3\2\2\2\u03b1\u03b2\7\u008c\2\2\u03b2\u03ba\5\u008eH\2\u03b3\u03b4\5"+
		"\u014c\u00a7\2\u03b4\u03b6\7^\2\2\u03b5\u03b7\5\u008cG\2\u03b6\u03b5\3"+
		"\2\2\2\u03b6\u03b7\3\2\2\2\u03b7\u03b8\3\2\2\2\u03b8\u03b9\7_\2\2\u03b9"+
		"\u03bb\3\2\2\2\u03ba\u03b3\3\2\2\2\u03ba\u03bb\3\2\2\2\u03bb\u008b\3\2"+
		"\2\2\u03bc\u03bf\5\u0090I\2\u03bd\u03bf\5\u0096L\2\u03be\u03bc\3\2\2\2"+
		"\u03be\u03bd\3\2\2\2\u03bf\u008d\3\2\2\2\u03c0\u03c1\5j\66\2\u03c1\u008f"+
		"\3\2\2\2\u03c2\u03c7\5\u0092J\2\u03c3\u03c4\7e\2\2\u03c4\u03c6\5\u0092"+
		"J\2\u03c5\u03c3\3\2\2\2\u03c6\u03c9\3\2\2\2\u03c7\u03c5\3\2\2\2\u03c7"+
		"\u03c8\3\2\2\2\u03c8\u0091\3\2\2\2\u03c9\u03c7\3\2\2\2\u03ca\u03cb\5\u0094"+
		"K\2\u03cb\u03cc\5\u014c\u00a7\2\u03cc\u03cd\7g\2\2\u03cd\u03ce\5\u014c"+
		"\u00a7\2\u03ce\u03cf\5\u0096L\2\u03cf\u0093\3\2\2\2\u03d0\u03d3\5\u0146"+
		"\u00a4\2\u03d1\u03d3\5\u014a\u00a6\2\u03d2\u03d0\3\2\2\2\u03d2\u03d1\3"+
		"\2\2\2\u03d3\u0095\3\2\2\2\u03d4\u03d8\5\u0098M\2\u03d5\u03d8\5\u008a"+
		"F\2\u03d6\u03d8\5\u00f6|\2\u03d7\u03d4\3\2\2\2\u03d7\u03d5\3\2\2\2\u03d7"+
		"\u03d6\3\2\2\2\u03d8\u0097\3\2\2\2\u03d9\u03e5\7b\2\2\u03da\u03df\5\u0096"+
		"L\2\u03db\u03dc\7e\2\2\u03dc\u03de\5\u0096L\2\u03dd\u03db\3\2\2\2\u03de"+
		"\u03e1\3\2\2\2\u03df\u03dd\3\2\2\2\u03df\u03e0\3\2\2\2\u03e0\u03e3\3\2"+
		"\2\2\u03e1\u03df\3\2\2\2\u03e2\u03e4\7e\2\2\u03e3\u03e2\3\2\2\2\u03e3"+
		"\u03e4\3\2\2\2\u03e4\u03e6\3\2\2\2\u03e5\u03da\3\2\2\2\u03e5\u03e6\3\2"+
		"\2\2\u03e6\u03e7\3\2\2\2\u03e7\u03f6\7c\2\2\u03e8\u03ec\7`\2\2\u03e9\u03ea"+
		"\5\u0096L\2\u03ea\u03eb\7e\2\2\u03eb\u03ed\3\2\2\2\u03ec\u03e9\3\2\2\2"+
		"\u03ed\u03ee\3\2\2\2\u03ee\u03ec\3\2\2\2\u03ee\u03ef\3\2\2\2\u03ef\u03f1"+
		"\3\2\2\2\u03f0\u03f2\5\u0096L\2\u03f1\u03f0\3\2\2\2\u03f1\u03f2\3\2\2"+
		"\2\u03f2\u03f3\3\2\2\2\u03f3\u03f4\7a\2\2\u03f4\u03f6\3\2\2\2\u03f5\u03d9"+
		"\3\2\2\2\u03f5\u03e8\3\2\2\2\u03f6\u0099\3\2\2\2\u03f7\u03f9\7`\2\2\u03f8"+
		"\u03fa\5\u014e\u00a8\2\u03f9\u03f8\3\2\2\2\u03f9\u03fa\3\2\2\2\u03fa\u03fb"+
		"\3\2\2\2\u03fb\u03fc\5\u0084C\2\u03fc\u03fd\7a\2\2\u03fd\u009b\3\2\2\2"+
		"\u03fe\u0401\5\u009eP\2\u03ff\u0401\5\u00bc_\2\u0400\u03fe\3\2\2\2\u0400"+
		"\u03ff\3\2\2\2\u0401\u009d\3\2\2\2\u0402\u0403\6P\5\2\u0403\u0404\5\u00a0"+
		"Q\2\u0404\u009f\3\2\2\2\u0405\u0406\5\22\n\2\u0406\u0411\5\u014c\u00a7"+
		"\2\u0407\u0409\5F$\2\u0408\u0407\3\2\2\2\u0408\u0409\3\2\2\2\u0409\u040a"+
		"\3\2\2\2\u040a\u0412\5> \2\u040b\u040c\5\u00a2R\2\u040c\u040d\5\u014c"+
		"\u00a7\2\u040d\u040e\7g\2\2\u040e\u040f\5\u014c\u00a7\2\u040f\u0410\5"+
		"D#\2\u0410\u0412\3\2\2\2\u0411\u0408\3\2\2\2\u0411\u040b\3\2\2\2\u0412"+
		"\u0417\3\2\2\2\u0413\u0414\5F$\2\u0414\u0415\5> \2\u0415\u0417\3\2\2\2"+
		"\u0416\u0405\3\2\2\2\u0416\u0413\3\2\2\2\u0417\u00a1\3\2\2\2\u0418\u0419"+
		"\7^\2\2\u0419\u041e\5\u00a4S\2\u041a\u041b\7e\2\2\u041b\u041d\5\u00a4"+
		"S\2\u041c\u041a\3\2\2\2\u041d\u0420\3\2\2\2\u041e\u041c\3\2\2\2\u041e"+
		"\u041f\3\2\2\2\u041f\u0421\3\2\2\2\u0420\u041e\3\2\2\2\u0421\u0422\7_"+
		"\2\2\u0422\u042f\3\2\2\2\u0423\u0424\7^\2\2\u0424\u0429\5\u00a6T\2\u0425"+
		"\u0426\7e\2\2\u0426\u0428\5\u00a6T\2\u0427\u0425\3\2\2\2\u0428\u042b\3"+
		"\2\2\2\u0429\u0427\3\2\2\2\u0429\u042a\3\2\2\2\u042a\u042c\3\2\2\2\u042b"+
		"\u0429\3\2\2\2\u042c\u042d\7_\2\2\u042d\u042f\3\2\2\2\u042e\u0418\3\2"+
		"\2\2\u042e\u0423\3\2\2\2\u042f\u00a3\3\2\2\2\u0430\u0435\7\n\2\2\u0431"+
		"\u0435\7>\2\2\u0432\u0435\7?\2\2\u0433\u0435\5F$\2\u0434\u0430\3\2\2\2"+
		"\u0434\u0431\3\2\2\2\u0434\u0432\3\2\2\2\u0434\u0433\3\2\2\2\u0434\u0435"+
		"\3\2\2\2\u0435\u0437\3\2\2\2\u0436\u0438\7x\2\2\u0437\u0436\3\2\2\2\u0437"+
		"\u0438\3\2\2\2\u0438\u0439\3\2\2\2\u0439\u043a\5B\"\2\u043a\u00a5\3\2"+
		"\2\2\u043b\u043c\5\u0146\u00a4\2\u043c\u0441\7m\2\2\u043d\u0442\7\n\2"+
		"\2\u043e\u0442\7>\2\2\u043f\u0442\7?\2\2\u0440\u0442\5F$\2\u0441\u043d"+
		"\3\2\2\2\u0441\u043e\3\2\2\2\u0441\u043f\3\2\2\2\u0441\u0440\3\2\2\2\u0441"+
		"\u0442\3\2\2\2\u0442\u0443\3\2\2\2\u0443\u0444\5B\"\2\u0444\u00a7\3\2"+
		"\2\2\u0445\u0446\7^\2\2\u0446\u0449\5B\"\2\u0447\u0448\7e\2\2\u0448\u044a"+
		"\5B\"\2\u0449\u0447\3\2\2\2\u044a\u044b\3\2\2\2\u044b\u0449\3\2\2\2\u044b"+
		"\u044c\3\2\2\2\u044c\u044d\3\2\2\2\u044d\u044e\7_\2\2\u044e\u00a9\3\2"+
		"\2\2\u044f\u0452\5\u00acW\2\u0450\u0452\5\u00aeX\2\u0451\u044f\3\2\2\2"+
		"\u0451\u0450\3\2\2\2\u0452\u00ab\3\2\2\2\u0453\u0454\7\"\2\2\u0454\u0455"+
		"\5\u00e2r\2\u0455\u0456\5\u014c\u00a7\2\u0456\u045f\5\u00bc_\2\u0457\u045a"+
		"\5\u014c\u00a7\2\u0458\u045a\5\u014e\u00a8\2\u0459\u0457\3\2\2\2\u0459"+
		"\u0458\3\2\2\2\u045a\u045b\3\2\2\2\u045b\u045c\7\34\2\2\u045c\u045d\5"+
		"\u014c\u00a7\2\u045d\u045e\5\u00bc_\2\u045e\u0460\3\2\2\2\u045f\u0459"+
		"\3\2\2\2\u045f\u0460\3\2\2\2\u0460\u00ad\3\2\2\2\u0461\u0462\7\67\2\2"+
		"\u0462\u0463\5\u00e2r\2\u0463\u0464\5\u014c\u00a7\2\u0464\u0465\7`\2\2"+
		"\u0465\u046d\5\u014c\u00a7\2\u0466\u0468\5\u00caf\2\u0467\u0466\3\2\2"+
		"\2\u0468\u0469\3\2\2\2\u0469\u0467\3\2\2\2\u0469\u046a\3\2\2\2\u046a\u046b"+
		"\3\2\2\2\u046b\u046c\5\u014c\u00a7\2\u046c\u046e\3\2\2\2\u046d\u0467\3"+
		"\2\2\2\u046d\u046e\3\2\2\2\u046e\u046f\3\2\2\2\u046f\u0470\7a\2\2\u0470"+
		"\u00af\3\2\2\2\u0471\u0472\5\u0088E\2\u0472\u0474\7!\2\2\u0473\u0475\7"+
		"\17\2\2\u0474\u0473\3\2\2\2\u0474\u0475\3\2\2\2\u0475\u0476\3\2\2\2\u0476"+
		"\u0477\7^\2\2\u0477\u0478\5\u00ceh\2\u0478\u0479\7_\2\2\u0479\u047a\5"+
		"\u014c\u00a7\2\u047a\u047b\5\u00bc_\2\u047b\u048b\3\2\2\2\u047c\u047d"+
		"\5\u0088E\2\u047d\u047e\7B\2\2\u047e\u047f\5\u00e2r\2\u047f\u0480\5\u014c"+
		"\u00a7\2\u0480\u0481\5\u00bc_\2\u0481\u048b\3\2\2\2\u0482\u0483\5\u0088"+
		"E\2\u0483\u0484\7\33\2\2\u0484\u0485\5\u014c\u00a7\2\u0485\u0486\5\u00bc"+
		"_\2\u0486\u0487\5\u014c\u00a7\2\u0487\u0488\7B\2\2\u0488\u0489\5\u00e2"+
		"r\2\u0489\u048b\3\2\2\2\u048a\u0471\3\2\2\2\u048a\u047c\3\2\2\2\u048a"+
		"\u0482\3\2\2\2\u048b\u00b1\3\2\2\2\u048c\u048e\7\31\2\2\u048d\u048f\5"+
		"\u0146\u00a4\2\u048e\u048d\3\2\2\2\u048e\u048f\3\2\2\2\u048f\u00b3\3\2"+
		"\2\2\u0490\u0492\7\24\2\2\u0491\u0493\5\u0146\u00a4\2\u0492\u0491\3\2"+
		"\2\2\u0492\u0493\3\2\2\2\u0493\u00b5\3\2\2\2\u0494\u0495\7C\2\2\u0495"+
		"\u0496\5\u00f6|\2\u0496\u00b7\3\2\2\2\u0497\u0499\7=\2\2\u0498\u049a\5"+
		"\u00c4c\2\u0499\u0498\3\2\2\2\u0499\u049a\3\2\2\2\u049a\u049b\3\2\2\2"+
		"\u049b\u049c\5\u014c\u00a7\2\u049c\u04a2\5\u009aN\2\u049d\u049e\5\u014c"+
		"\u00a7\2\u049e\u049f\5\u00be`\2\u049f\u04a1\3\2\2\2\u04a0\u049d\3\2\2"+
		"\2\u04a1\u04a4\3\2\2\2\u04a2\u04a0\3\2\2\2\u04a2\u04a3\3\2\2\2\u04a3\u04a8"+
		"\3\2\2\2\u04a4\u04a2\3\2\2\2\u04a5\u04a6\5\u014c\u00a7\2\u04a6\u04a7\5"+
		"\u00c2b\2\u04a7\u04a9\3\2\2\2\u04a8\u04a5\3\2\2\2\u04a8\u04a9\3\2\2\2"+
		"\u04a9\u00b9\3\2\2\2\u04aa\u04ab\7\23\2\2\u04ab\u04b1\5\u00f6|\2\u04ac"+
		"\u04ad\5\u014c\u00a7\2\u04ad\u04ae\t\6\2\2\u04ae\u04af\5\u014c\u00a7\2"+
		"\u04af\u04b0\5\u00f6|\2\u04b0\u04b2\3\2\2\2\u04b1\u04ac\3\2\2\2\u04b1"+
		"\u04b2\3\2\2\2\u04b2\u00bb\3\2\2\2\u04b3\u04d9\5\u009aN\2\u04b4\u04d9"+
		"\5\u00aaV\2\u04b5\u04d9\5\u00b0Y\2\u04b6\u04d9\5\u00b8]\2\u04b7\u04b8"+
		"\78\2\2\u04b8\u04b9\5\u00e2r\2\u04b9\u04ba\5\u014c\u00a7\2\u04ba\u04bb"+
		"\5\u009aN\2\u04bb\u04d9\3\2\2\2\u04bc\u04be\7\62\2\2\u04bd\u04bf\5\u00f6"+
		"|\2\u04be\u04bd\3\2\2\2\u04be\u04bf\3\2\2\2\u04bf\u04d9\3\2\2\2\u04c0"+
		"\u04c1\7:\2\2\u04c1\u04d9\5\u00f6|\2\u04c2\u04d9\5\u00b4[\2\u04c3\u04d9"+
		"\5\u00b2Z\2\u04c4\u04c5\6_\6\2\u04c5\u04d9\5\u00b6\\\2\u04c6\u04c7\7C"+
		"\2\2\u04c7\u04c8\7\62\2\2\u04c8\u04c9\5\u014c\u00a7\2\u04c9\u04ca\5\u00f6"+
		"|\2\u04ca\u04d9\3\2\2\2\u04cb\u04cc\7\20\2\2\u04cc\u04cd\5\u014c\u00a7"+
		"\2\u04cd\u04ce\5\u00ecw\2\u04ce\u04d9\3\2\2\2\u04cf\u04d0\5\u0146\u00a4"+
		"\2\u04d0\u04d1\7m\2\2\u04d1\u04d2\5\u014c\u00a7\2\u04d2\u04d3\5\u00bc"+
		"_\2\u04d3\u04d9\3\2\2\2\u04d4\u04d9\5\u00ba^\2\u04d5\u04d9\5\u009eP\2"+
		"\u04d6\u04d9\5\u00ecw\2\u04d7\u04d9\7d\2\2\u04d8\u04b3\3\2\2\2\u04d8\u04b4"+
		"\3\2\2\2\u04d8\u04b5\3\2\2\2\u04d8\u04b6\3\2\2\2\u04d8\u04b7\3\2\2\2\u04d8"+
		"\u04bc\3\2\2\2\u04d8\u04c0\3\2\2\2\u04d8\u04c2\3\2\2\2\u04d8\u04c3\3\2"+
		"\2\2\u04d8\u04c4\3\2\2\2\u04d8\u04c6\3\2\2\2\u04d8\u04cb\3\2\2\2\u04d8"+
		"\u04cf\3\2\2\2\u04d8\u04d4\3\2\2\2\u04d8\u04d5\3\2\2\2\u04d8\u04d6\3\2"+
		"\2\2\u04d8\u04d7\3\2\2\2\u04d9\u00bd\3\2\2\2\u04da\u04db\7\26\2\2\u04db"+
		"\u04dc\7^\2\2\u04dc\u04de\5\34\17\2\u04dd\u04df\5\u00c0a\2\u04de\u04dd"+
		"\3\2\2\2\u04de\u04df\3\2\2\2\u04df\u04e0\3\2\2\2\u04e0\u04e1\5\u0146\u00a4"+
		"\2\u04e1\u04e2\7_\2\2\u04e2\u04e3\5\u014c\u00a7\2\u04e3\u04e4\5\u009a"+
		"N\2\u04e4\u00bf\3\2\2\2\u04e5\u04ea\5j\66\2\u04e6\u04e7\7{\2\2\u04e7\u04e9"+
		"\5j\66\2\u04e8\u04e6\3\2\2\2\u04e9\u04ec\3\2\2\2\u04ea\u04e8\3\2\2\2\u04ea"+
		"\u04eb\3\2\2\2\u04eb\u00c1\3\2\2\2\u04ec\u04ea\3\2\2\2\u04ed\u04ee\7 "+
		"\2\2\u04ee\u04ef\5\u014c\u00a7\2\u04ef\u04f0\5\u009aN\2\u04f0\u00c3\3"+
		"\2\2\2\u04f1\u04f2\7^\2\2\u04f2\u04f3\5\u014c\u00a7\2\u04f3\u04f5\5\u00c6"+
		"d\2\u04f4\u04f6\5\u014e\u00a8\2\u04f5\u04f4\3\2\2\2\u04f5\u04f6\3\2\2"+
		"\2\u04f6\u04f7\3\2\2\2\u04f7\u04f8\7_\2\2\u04f8\u00c5\3\2\2\2\u04f9\u04ff"+
		"\5\u00c8e\2\u04fa\u04fb\5\u014e\u00a8\2\u04fb\u04fc\5\u00c8e\2\u04fc\u04fe"+
		"\3\2\2\2\u04fd\u04fa\3\2\2\2\u04fe\u0501\3\2\2\2\u04ff\u04fd\3\2\2\2\u04ff"+
		"\u0500\3\2\2\2\u0500\u00c7\3\2\2\2\u0501\u04ff\3\2\2\2\u0502\u0505\5\u009e"+
		"P\2\u0503\u0505\5\u00f6|\2\u0504\u0502\3\2\2\2\u0504\u0503\3\2\2\2\u0505"+
		"\u00c9\3\2\2\2\u0506\u050c\5\u00ccg\2\u0507\u0508\5\u014c\u00a7\2\u0508"+
		"\u0509\5\u00ccg\2\u0509\u050b\3\2\2\2\u050a\u0507\3\2\2\2\u050b\u050e"+
		"\3\2\2\2\u050c\u050a\3\2\2\2\u050c\u050d\3\2\2\2\u050d\u050f\3\2\2\2\u050e"+
		"\u050c\3\2\2\2\u050f\u0510\5\u014c\u00a7\2\u0510\u0511\5\u0086D\2\u0511"+
		"\u00cb\3\2\2\2\u0512\u0513\7\25\2\2\u0513\u0514\5\u00f6|\2\u0514\u0515"+
		"\7m\2\2\u0515\u0519\3\2\2\2\u0516\u0517\7\32\2\2\u0517\u0519\7m\2\2\u0518"+
		"\u0512\3\2\2\2\u0518\u0516\3\2\2\2\u0519\u00cd\3\2\2\2\u051a\u051d\5\u00d0"+
		"i\2\u051b\u051d\5\u00d4k\2\u051c\u051a\3\2\2\2\u051c\u051b\3\2\2\2\u051d"+
		"\u00cf\3\2\2\2\u051e\u051f\5\u00d2j\2\u051f\u0520\7e\2\2\u0520\u0522\3"+
		"\2\2\2\u0521\u051e\3\2\2\2\u0521\u0522\3\2\2\2\u0522\u0523\3\2\2\2\u0523"+
		"\u0525\5\34\17\2\u0524\u0526\5F$\2\u0525\u0524\3\2\2\2\u0525\u0526\3\2"+
		"\2\2\u0526\u0527\3\2\2\2\u0527\u0528\5\u0146\u00a4\2\u0528\u0529\t\7\2"+
		"\2\u0529\u052a\5\u00f6|\2\u052a\u00d1\3\2\2\2\u052b\u052d\t\b\2\2\u052c"+
		"\u052b\3\2\2\2\u052c\u052d\3\2\2\2\u052d\u052e\3\2\2\2\u052e\u052f\5\u0146"+
		"\u00a4\2\u052f\u00d3\3\2\2\2\u0530\u0532\5\u00d6l\2\u0531\u0530\3\2\2"+
		"\2\u0531\u0532\3\2\2\2\u0532\u0533\3\2\2\2\u0533\u0535\7d\2\2\u0534\u0536"+
		"\5\u00f6|\2\u0535\u0534\3\2\2\2\u0535\u0536\3\2\2\2\u0536\u0537\3\2\2"+
		"\2\u0537\u0539\7d\2\2\u0538\u053a\5\u00d8m\2\u0539\u0538\3\2\2\2\u0539"+
		"\u053a\3\2\2\2\u053a\u00d5\3\2\2\2\u053b\u053e\5\u009eP\2\u053c\u053e"+
		"\5\u00e4s\2\u053d\u053b\3\2\2\2\u053d\u053c\3\2\2\2\u053e\u00d7\3\2\2"+
		"\2\u053f\u0540\5\u00e4s\2\u0540\u00d9\3\2\2\2\u0541\u0542\7^\2\2\u0542"+
		"\u0543\5\u00dco\2\u0543\u0544\7_\2\2\u0544\u00db\3\2\2\2\u0545\u054c\5"+
		"F$\2\u0546\u0547\7z\2\2\u0547\u0548\5\u014c\u00a7\2\u0548\u0549\5F$\2"+
		"\u0549\u054b\3\2\2\2\u054a\u0546\3\2\2\2\u054b\u054e\3\2\2\2\u054c\u054a"+
		"\3\2\2\2\u054c\u054d\3\2\2\2\u054d\u00dd\3\2\2\2\u054e\u054c\3\2\2\2\u054f"+
		"\u0552\5\u00dan\2\u0550\u0552\5F$\2\u0551\u054f\3\2\2\2\u0551\u0550\3"+
		"\2\2\2\u0552\u00df\3\2\2\2\u0553\u0554\5\u00e2r\2\u0554\u00e1\3\2\2\2"+
		"\u0555\u0556\7^\2\2\u0556\u0557\5\u00eav\2\u0557\u0558\7_\2\2\u0558\u00e3"+
		"\3\2\2\2\u0559\u0560\5\u00e6t\2\u055a\u055b\7e\2\2\u055b\u055c\5\u014c"+
		"\u00a7\2\u055c\u055d\5\u00e6t\2\u055d\u055f\3\2\2\2\u055e\u055a\3\2\2"+
		"\2\u055f\u0562\3\2\2\2\u0560\u055e\3\2\2\2\u0560\u0561\3\2\2\2\u0561\u00e5"+
		"\3\2\2\2\u0562\u0560\3\2\2\2\u0563\u0565\7x\2\2\u0564\u0563\3\2\2\2\u0564"+
		"\u0565\3\2\2\2\u0565\u0566\3\2\2\2\u0566\u0567\5\u00f6|\2\u0567\u00e7"+
		"\3\2\2\2\u0568\u056b\5\u00f6|\2\u0569\u056b\5x=\2\u056a\u0568\3\2\2\2"+
		"\u056a\u0569\3\2\2\2\u056b\u00e9\3\2\2\2\u056c\u056f\5\u00ecw\2\u056d"+
		"\u056f\5x=\2\u056e\u056c\3\2\2\2\u056e\u056d\3\2\2\2\u056f\u00eb\3\2\2"+
		"\2\u0570\u0571\5\u00fa~\2\u0571\u00ed\3\2\2\2\u0572\u0574\5\u00fe\u0080"+
		"\2\u0573\u0575\t\t\2\2\u0574\u0573\3\2\2\2\u0574\u0575\3\2\2\2\u0575\u00ef"+
		"\3\2\2\2\u0576\u0577\7\67\2\2\u0577\u0578\5\u00e2r\2\u0578\u0579\5\u014c"+
		"\u00a7\2\u0579\u057a\7`\2\2\u057a\u057e\5\u014c\u00a7\2\u057b\u057d\5"+
		"\u00f2z\2\u057c\u057b\3\2\2\2\u057d\u0580\3\2\2\2\u057e\u057c\3\2\2\2"+
		"\u057e\u057f\3\2\2\2\u057f\u0581\3\2\2\2\u0580\u057e\3\2\2\2\u0581\u0582"+
		"\5\u014c\u00a7\2\u0582\u0583\7a\2\2\u0583\u00f1\3\2\2\2\u0584\u0585\5"+
		"\u00f4{\2\u0585\u0586\5\u014c\u00a7\2\u0586\u0588\3\2\2\2\u0587\u0584"+
		"\3\2\2\2\u0588\u0589\3\2\2\2\u0589\u0587\3\2\2\2\u0589\u058a\3\2\2\2\u058a"+
		"\u058b\3\2\2\2\u058b\u058c\5\u0086D\2\u058c\u00f3\3\2\2\2\u058d\u058e"+
		"\7\25\2\2\u058e\u0591\5\u00e4s\2\u058f\u0591\7\32\2\2\u0590\u058d\3\2"+
		"\2\2\u0590\u058f\3\2\2\2\u0591\u0592\3\2\2\2\u0592\u0593\t\n\2\2\u0593"+
		"\u00f5\3\2\2\2\u0594\u0595\b|\1\2\u0595\u0596\5\u00dan\2\u0596\u0597\5"+
		"\u00f8}\2\u0597\u05c6\3\2\2\2\u0598\u0599\7\16\2\2\u0599\u059a\5\u014c"+
		"\u00a7\2\u059a\u059b\5\u0082B\2\u059b\u05c6\3\2\2\2\u059c\u059d\7\17\2"+
		"\2\u059d\u05b5\5\u014c\u00a7\2\u059e\u059f\7^\2\2\u059f\u05a6\5\u00f6"+
		"|\2\u05a0\u05a1\7e\2\2\u05a1\u05a2\5\u014c\u00a7\2\u05a2\u05a3\5\u00f6"+
		"|\2\u05a3\u05a5\3\2\2\2\u05a4\u05a0\3\2\2\2\u05a5\u05a8\3\2\2\2\u05a6"+
		"\u05a4\3\2\2\2\u05a6\u05a7\3\2\2\2\u05a7\u05a9\3\2\2\2\u05a8\u05a6\3\2"+
		"\2\2\u05a9\u05aa\7_\2\2\u05aa\u05b6\3\2\2\2\u05ab\u05b2\5\u00f6|\2\u05ac"+
		"\u05ad\7e\2\2\u05ad\u05ae\5\u014c\u00a7\2\u05ae\u05af\5\u00f6|\2\u05af"+
		"\u05b1\3\2\2\2\u05b0\u05ac\3\2\2\2\u05b1\u05b4\3\2\2\2\u05b2\u05b0\3\2"+
		"\2\2\u05b2\u05b3\3\2\2\2\u05b3\u05b6\3\2\2\2\u05b4\u05b2\3\2\2\2\u05b5"+
		"\u059e\3\2\2\2\u05b5\u05ab\3\2\2\2\u05b6\u05c6\3\2\2\2\u05b7\u05c6\5\u00ee"+
		"x\2\u05b8\u05c6\5\u00f0y\2\u05b9\u05ba\t\13\2\2\u05ba\u05bb\5\u014c\u00a7"+
		"\2\u05bb\u05bc\5\u00f6|\26\u05bc\u05c6\3\2\2\2\u05bd\u05be\t\f\2\2\u05be"+
		"\u05c6\5\u00f6|\24\u05bf\u05c0\5\u00a8U\2\u05c0\u05c1\5\u014c\u00a7\2"+
		"\u05c1\u05c2\7g\2\2\u05c2\u05c3\5\u014c\u00a7\2\u05c3\u05c4\5\u00ecw\2"+
		"\u05c4\u05c6\3\2\2\2\u05c5\u0594\3\2\2\2\u05c5\u0598\3\2\2\2\u05c5\u059c"+
		"\3\2\2\2\u05c5\u05b7\3\2\2\2\u05c5\u05b8\3\2\2\2\u05c5\u05b9\3\2\2\2\u05c5"+
		"\u05bd\3\2\2\2\u05c5\u05bf\3\2\2\2\u05c6\u0641\3\2\2\2\u05c7\u05c8\f\25"+
		"\2\2\u05c8\u05c9\7U\2\2\u05c9\u05ca\5\u014c\u00a7\2\u05ca\u05cb\5\u00f6"+
		"|\26\u05cb\u0640\3\2\2\2\u05cc\u05cd\f\23\2\2\u05cd\u05ce\5\u014c\u00a7"+
		"\2\u05ce\u05cf\t\r\2\2\u05cf\u05d0\5\u014c\u00a7\2\u05d0\u05d1\5\u00f6"+
		"|\24\u05d1\u0640\3\2\2\2\u05d2\u05d3\f\22\2\2\u05d3\u05d4\t\16\2\2\u05d4"+
		"\u05d5\5\u014c\u00a7\2\u05d5\u05d6\5\u00f6|\23\u05d6\u0640\3\2\2\2\u05d7"+
		"\u05d8\f\21\2\2\u05d8\u05e3\5\u014c\u00a7\2\u05d9\u05da\7i\2\2\u05da\u05e1"+
		"\7i\2\2\u05db\u05dc\7h\2\2\u05dc\u05dd\7h\2\2\u05dd\u05e1\7h\2\2\u05de"+
		"\u05df\7h\2\2\u05df\u05e1\7h\2\2\u05e0\u05d9\3\2\2\2\u05e0\u05db\3\2\2"+
		"\2\u05e0\u05de\3\2\2\2\u05e1\u05e4\3\2\2\2\u05e2\u05e4\t\17\2\2\u05e3"+
		"\u05e0\3\2\2\2\u05e3\u05e2\3\2\2\2\u05e4\u05e5\3\2\2\2\u05e5\u05e6\5\u014c"+
		"\u00a7\2\u05e6\u05e7\5\u00f6|\22\u05e7\u0640\3\2\2\2\u05e8\u05e9\f\16"+
		"\2\2\u05e9\u05ea\5\u014c\u00a7\2\u05ea\u05eb\t\20\2\2\u05eb\u05ec\5\u014c"+
		"\u00a7\2\u05ec\u05ed\5\u00f6|\17\u05ed\u0640\3\2\2\2\u05ee\u05ef\f\r\2"+
		"\2\u05ef\u05f0\5\u014c\u00a7\2\u05f0\u05f1\t\21\2\2\u05f1\u05f2\5\u014c"+
		"\u00a7\2\u05f2\u05f3\5\u00f6|\16\u05f3\u0640\3\2\2\2\u05f4\u05f5\f\f\2"+
		"\2\u05f5\u05f6\5\u014c\u00a7\2\u05f6\u05f7\t\22\2\2\u05f7\u05f8\5\u014c"+
		"\u00a7\2\u05f8\u05f9\5\u00f6|\r\u05f9\u0640\3\2\2\2\u05fa\u05fb\f\13\2"+
		"\2\u05fb\u05fc\5\u014c\u00a7\2\u05fc\u05fd\7z\2\2\u05fd\u05fe\5\u014c"+
		"\u00a7\2\u05fe\u05ff\5\u00f6|\f\u05ff\u0640\3\2\2\2\u0600\u0601\f\n\2"+
		"\2\u0601\u0602\5\u014c\u00a7\2\u0602\u0603\7|\2\2\u0603\u0604\5\u014c"+
		"\u00a7\2\u0604\u0605\5\u00f6|\13\u0605\u0640\3\2\2\2\u0606\u0607\f\t\2"+
		"\2\u0607\u0608\5\u014c\u00a7\2\u0608\u0609\7{\2\2\u0609\u060a\5\u014c"+
		"\u00a7\2\u060a\u060b\5\u00f6|\n\u060b\u0640\3\2\2\2\u060c\u060d\f\b\2"+
		"\2\u060d\u060e\5\u014c\u00a7\2\u060e\u060f\7r\2\2\u060f\u0610\5\u014c"+
		"\u00a7\2\u0610\u0611\5\u00f6|\t\u0611\u0640\3\2\2\2\u0612\u0613\f\7\2"+
		"\2\u0613\u0614\5\u014c\u00a7\2\u0614\u0615\7s\2\2\u0615\u0616\5\u014c"+
		"\u00a7\2\u0616\u0617\5\u00f6|\b\u0617\u0640\3\2\2\2\u0618\u0619\f\6\2"+
		"\2\u0619\u061a\5\u014c\u00a7\2\u061a\u061b\7Y\2\2\u061b\u061c\5\u014c"+
		"\u00a7\2\u061c\u061d\5\u00f6|\6\u061d\u0640\3\2\2\2\u061e\u061f\f\5\2"+
		"\2\u061f\u0629\5\u014c\u00a7\2\u0620\u0621\7l\2\2\u0621\u0622\5\u014c"+
		"\u00a7\2\u0622\u0623\5\u00f6|\2\u0623\u0624\5\u014c\u00a7\2\u0624\u0625"+
		"\7m\2\2\u0625\u0626\5\u014c\u00a7\2\u0626\u062a\3\2\2\2\u0627\u0628\7"+
		"P\2\2\u0628\u062a\5\u014c\u00a7\2\u0629\u0620\3\2\2\2\u0629\u0627\3\2"+
		"\2\2\u062a\u062b\3\2\2\2\u062b\u062c\5\u00f6|\5\u062c\u0640\3\2\2\2\u062d"+
		"\u062e\f\20\2\2\u062e\u062f\5\u014c\u00a7\2\u062f\u0630\7&\2\2\u0630\u0631"+
		"\5\u014c\u00a7\2\u0631\u0632\5L\'\2\u0632\u0640\3\2\2\2\u0633\u0634\f"+
		"\17\2\2\u0634\u0635\5\u014c\u00a7\2\u0635\u0636\t\23\2\2\u0636\u0637\5"+
		"\u014c\u00a7\2\u0637\u0638\5\u00dep\2\u0638\u0640\3\2\2\2\u0639\u063a"+
		"\f\3\2\2\u063a\u063b\5\u014c\u00a7\2\u063b\u063c\t\24\2\2\u063c\u063d"+
		"\5\u014c\u00a7\2\u063d\u063e\5\u00eav\2\u063e\u0640\3\2\2\2\u063f\u05c7"+
		"\3\2\2\2\u063f\u05cc\3\2\2\2\u063f\u05d2\3\2\2\2\u063f\u05d7\3\2\2\2\u063f"+
		"\u05e8\3\2\2\2\u063f\u05ee\3\2\2\2\u063f\u05f4\3\2\2\2\u063f\u05fa\3\2"+
		"\2\2\u063f\u0600\3\2\2\2\u063f\u0606\3\2\2\2\u063f\u060c\3\2\2\2\u063f"+
		"\u0612\3\2\2\2\u063f\u0618\3\2\2\2\u063f\u061e\3\2\2\2\u063f\u062d\3\2"+
		"\2\2\u063f\u0633\3\2\2\2\u063f\u0639\3\2\2\2\u0640\u0643\3\2\2\2\u0641"+
		"\u063f\3\2\2\2\u0641\u0642\3\2\2\2\u0642\u00f7\3\2\2\2\u0643\u0641\3\2"+
		"\2\2\u0644\u0645\5\u00dan\2\u0645\u0646\5\u00f8}\2\u0646\u064f\3\2\2\2"+
		"\u0647\u064f\5\u00eex\2\u0648\u0649\t\13\2\2\u0649\u064a\5\u014c\u00a7"+
		"\2\u064a\u064b\5\u00f8}\2\u064b\u064f\3\2\2\2\u064c\u064d\t\f\2\2\u064d"+
		"\u064f\5\u00f8}\2\u064e\u0644\3\2\2\2\u064e\u0647\3\2\2\2\u064e\u0648"+
		"\3\2\2\2\u064e\u064c\3\2\2\2\u064f\u00f9\3\2\2\2\u0650\u0654\5\u00f6|"+
		"\2\u0651\u0652\6~\30\3\u0652\u0655\5\u0138\u009d\2\u0653\u0655\3\2\2\2"+
		"\u0654\u0651\3\2\2\2\u0654\u0653\3\2\2\2\u0655\u0659\3\2\2\2\u0656\u0658"+
		"\5\u00fc\177\2\u0657\u0656\3\2\2\2\u0658\u065b\3\2\2\2\u0659\u0657\3\2"+
		"\2\2\u0659\u065a\3\2\2\2\u065a\u00fb\3\2\2\2\u065b\u0659\3\2\2\2\u065c"+
		"\u0663\5\u0110\u0089\2\u065d\u065f\5\u0100\u0081\2\u065e\u065d\3\2\2\2"+
		"\u065f\u0660\3\2\2\2\u0660\u065e\3\2\2\2\u0660\u0661\3\2\2\2\u0661\u0664"+
		"\3\2\2\2\u0662\u0664\5\u0138\u009d\2\u0663\u065e\3\2\2\2\u0663\u0662\3"+
		"\2\2\2\u0663\u0664\3\2\2\2\u0664\u00fd\3\2\2\2\u0665\u0669\5\u010a\u0086"+
		"\2\u0666\u0667\6\u0080\31\2\u0667\u0669\7\64\2\2\u0668\u0665\3\2\2\2\u0668"+
		"\u0666\3\2\2\2\u0669\u066f\3\2\2\2\u066a\u066b\5\u0100\u0081\2\u066b\u066c"+
		"\b\u0080\1\2\u066c\u066e\3\2\2\2\u066d\u066a\3\2\2\2\u066e\u0671\3\2\2"+
		"\2\u066f\u066d\3\2\2\2\u066f\u0670\3\2\2\2\u0670\u00ff\3\2\2\2\u0671\u066f"+
		"\3\2\2\2\u0672\u068e\5\u014c\u00a7\2\u0673\u0674\7f\2\2\u0674\u0675\5"+
		"\u014c\u00a7\2\u0675\u0676\7*\2\2\u0676\u0677\5\u0126\u0094\2\u0677\u0678"+
		"\b\u0081\1\2\u0678\u068f\3\2\2\2\u0679\u067a\t\25\2\2\u067a\u067d\5\u014c"+
		"\u00a7\2\u067b\u067e\7\u008c\2\2\u067c\u067e\5\u0132\u009a\2\u067d\u067b"+
		"\3\2\2\2\u067d\u067c\3\2\2\2\u067d\u067e\3\2\2\2\u067e\u0687\3\2\2\2\u067f"+
		"\u0680\7Q\2\2\u0680\u0687\5\u014c\u00a7\2\u0681\u0682\7R\2\2\u0682\u0684"+
		"\5\u014c\u00a7\2\u0683\u0685\5\u0132\u009a\2\u0684\u0683\3\2\2\2\u0684"+
		"\u0685\3\2\2\2\u0685\u0687\3\2\2\2\u0686\u0679\3\2\2\2\u0686\u067f\3\2"+
		"\2\2\u0686\u0681\3\2\2\2\u0687\u0688\3\2\2\2\u0688\u0689\5\u0102\u0082"+
		"\2\u0689\u068a\b\u0081\1\2\u068a\u068f\3\2\2\2\u068b\u068c\5\u0082B\2"+
		"\u068c\u068d\b\u0081\1\2\u068d\u068f\3\2\2\2\u068e\u0673\3\2\2\2\u068e"+
		"\u0686\3\2\2\2\u068e\u068b\3\2\2\2\u068f\u069a\3\2\2\2\u0690\u0691\5\u0136"+
		"\u009c\2\u0691\u0692\b\u0081\1\2\u0692\u069a\3\2\2\2\u0693\u0694\5\u0106"+
		"\u0084\2\u0694\u0695\b\u0081\1\2\u0695\u069a\3\2\2\2\u0696\u0697\5\u0108"+
		"\u0085\2\u0697\u0698\b\u0081\1\2\u0698\u069a\3\2\2\2\u0699\u0672\3\2\2"+
		"\2\u0699\u0690\3\2\2\2\u0699\u0693\3\2\2\2\u0699\u0696\3\2\2\2\u069a\u0101"+
		"\3\2\2\2\u069b\u06a0\5\u0146\u00a4\2\u069c\u06a0\5\u0142\u00a2\2\u069d"+
		"\u06a0\5\u0104\u0083\2\u069e\u06a0\5\u014a\u00a6\2\u069f\u069b\3\2\2\2"+
		"\u069f\u069c\3\2\2\2\u069f\u069d\3\2\2\2\u069f\u069e\3\2\2\2\u06a0\u0103"+
		"\3\2\2\2\u06a1\u06a4\5\u00e0q\2\u06a2\u06a4\5p9\2\u06a3\u06a1\3\2\2\2"+
		"\u06a3\u06a2\3\2\2\2\u06a4\u0105\3\2\2\2\u06a5\u06a7\t\26\2\2\u06a6\u06a8"+
		"\5\u00e4s\2\u06a7\u06a6\3\2\2\2\u06a7\u06a8\3\2\2\2\u06a8\u06a9\3\2\2"+
		"\2\u06a9\u06aa\7c\2\2\u06aa\u0107\3\2\2\2\u06ab\u06ae\t\26\2\2\u06ac\u06af"+
		"\5\u0118\u008d\2\u06ad\u06af\7m\2\2\u06ae\u06ac\3\2\2\2\u06ae\u06ad\3"+
		"\2\2\2\u06af\u06b0\3\2\2\2\u06b0\u06b1\7c\2\2\u06b1\u0109\3\2\2\2\u06b2"+
		"\u06b4\5\u0146\u00a4\2\u06b3\u06b5\5R*\2\u06b4\u06b3\3\2\2\2\u06b4\u06b5"+
		"\3\2\2\2\u06b5\u06c4\3\2\2\2\u06b6\u06c4\5n8\2\u06b7\u06c4\5p9\2\u06b8"+
		"\u06b9\7*\2\2\u06b9\u06ba\5\u014c\u00a7\2\u06ba\u06bb\5\u0126\u0094\2"+
		"\u06bb\u06c4\3\2\2\2\u06bc\u06c4\79\2\2\u06bd\u06c4\7\66\2\2\u06be\u06c4"+
		"\5\u00e0q\2\u06bf\u06c4\5\u0082B\2\u06c0\u06c4\5\u0112\u008a\2\u06c1\u06c4"+
		"\5\u0114\u008b\2\u06c2\u06c4\5\u0148\u00a5\2\u06c3\u06b2\3\2\2\2\u06c3"+
		"\u06b6\3\2\2\2\u06c3\u06b7\3\2\2\2\u06c3\u06b8\3\2\2\2\u06c3\u06bc\3\2"+
		"\2\2\u06c3\u06bd\3\2\2\2\u06c3\u06be\3\2\2\2\u06c3\u06bf\3\2\2\2\u06c3"+
		"\u06c0\3\2\2\2\u06c3\u06c1\3\2\2\2\u06c3\u06c2\3\2\2\2\u06c4\u010b\3\2"+
		"\2\2\u06c5\u06cc\5\u0146\u00a4\2\u06c6\u06cc\5n8\2\u06c7\u06cc\5p9\2\u06c8"+
		"\u06cc\5\u00e0q\2\u06c9\u06cc\5\u0112\u008a\2\u06ca\u06cc\5\u0114\u008b"+
		"\2\u06cb\u06c5\3\2\2\2\u06cb\u06c6\3\2\2\2\u06cb\u06c7\3\2\2\2\u06cb\u06c8"+
		"\3\2\2\2\u06cb\u06c9\3\2\2\2\u06cb\u06ca\3\2\2\2\u06cc\u010d\3\2\2\2\u06cd"+
		"\u06d1\5\u0146\u00a4\2\u06ce\u06d1\5n8\2\u06cf\u06d1\5p9\2\u06d0\u06cd"+
		"\3\2\2\2\u06d0\u06ce\3\2\2\2\u06d0\u06cf\3\2\2\2\u06d1\u010f\3\2\2\2\u06d2"+
		"\u06d6\5\u0146\u00a4\2\u06d3\u06d6\5n8\2\u06d4\u06d6\5p9\2\u06d5\u06d2"+
		"\3\2\2\2\u06d5\u06d3\3\2\2\2\u06d5\u06d4\3\2\2\2\u06d6\u0111\3\2\2\2\u06d7"+
		"\u06d9\7b\2\2\u06d8\u06da\5\u00e4s\2\u06d9\u06d8\3\2\2\2\u06d9\u06da\3"+
		"\2\2\2\u06da\u06dc\3\2\2\2\u06db\u06dd\7e\2\2\u06dc\u06db\3\2\2\2\u06dc"+
		"\u06dd\3\2\2\2\u06dd\u06de\3\2\2\2\u06de\u06df\7c\2\2\u06df\u0113\3\2"+
		"\2\2\u06e0\u06e6\7b\2\2\u06e1\u06e3\5\u0116\u008c\2\u06e2\u06e4\7e\2\2"+
		"\u06e3\u06e2\3\2\2\2\u06e3\u06e4\3\2\2\2\u06e4\u06e7\3\2\2\2\u06e5\u06e7"+
		"\7m\2\2\u06e6\u06e1\3\2\2\2\u06e6\u06e5\3\2\2\2\u06e7\u06e8\3\2\2\2\u06e8"+
		"\u06e9\7c\2\2\u06e9\u0115\3\2\2\2\u06ea\u06ef\5\u011a\u008e\2\u06eb\u06ec"+
		"\7e\2\2\u06ec\u06ee\5\u011a\u008e\2\u06ed\u06eb\3\2\2\2\u06ee\u06f1\3"+
		"\2\2\2\u06ef\u06ed\3\2\2\2\u06ef\u06f0\3\2\2\2\u06f0\u0117\3\2\2\2\u06f1"+
		"\u06ef\3\2\2\2\u06f2\u06f7\5\u011c\u008f\2\u06f3\u06f4\7e\2\2\u06f4\u06f6"+
		"\5\u011c\u008f\2\u06f5\u06f3\3\2\2\2\u06f6\u06f9\3\2\2\2\u06f7\u06f5\3"+
		"\2\2\2\u06f7\u06f8\3\2\2\2\u06f8\u0119\3\2\2\2\u06f9\u06f7\3\2\2\2\u06fa"+
		"\u06fb\5\u0120\u0091\2\u06fb\u06fc\7m\2\2\u06fc\u06fd\5\u014c\u00a7\2"+
		"\u06fd\u06fe\5\u00e8u\2\u06fe\u0705\3\2\2\2\u06ff\u0700\7x\2\2\u0700\u0701"+
		"\7m\2\2\u0701\u0702\5\u014c\u00a7\2\u0702\u0703\5\u00e8u\2\u0703\u0705"+
		"\3\2\2\2\u0704\u06fa\3\2\2\2\u0704\u06ff\3\2\2\2\u0705\u011b\3\2\2\2\u0706"+
		"\u0707\5\u0122\u0092\2\u0707\u0708\7m\2\2\u0708\u0709\5\u014c\u00a7\2"+
		"\u0709\u070a\5\u00e8u\2\u070a\u0711\3\2\2\2\u070b\u070c\7x\2\2\u070c\u070d"+
		"\7m\2\2\u070d\u070e\5\u014c\u00a7\2\u070e\u070f\5\u00e8u\2\u070f\u0711"+
		"\3\2\2\2\u0710\u0706\3\2\2\2\u0710\u070b\3\2\2\2\u0711\u011d\3\2\2\2\u0712"+
		"\u0713\5\u0124\u0093\2\u0713\u0714\7m\2\2\u0714\u0715\5\u014c\u00a7\2"+
		"\u0715\u0716\5\u00e8u\2\u0716\u071d\3\2\2\2\u0717\u0718\7x\2\2\u0718\u0719"+
		"\7m\2\2\u0719\u071a\5\u014c\u00a7\2\u071a\u071b\5\u00e8u\2\u071b\u071d"+
		"\3\2\2\2\u071c\u0712\3\2\2\2\u071c\u0717\3\2\2\2\u071d\u011f\3\2\2\2\u071e"+
		"\u0721\5\u014a\u00a6\2\u071f\u0721\5\u010a\u0086\2\u0720\u071e\3\2\2\2"+
		"\u0720\u071f\3\2\2\2\u0721\u0121\3\2\2\2\u0722\u0725\5\u014a\u00a6\2\u0723"+
		"\u0725\5\u010c\u0087\2\u0724\u0722\3\2\2\2\u0724\u0723\3\2\2\2\u0725\u0123"+
		"\3\2\2\2\u0726\u0729\5\u014a\u00a6\2\u0727\u0729\5\u010e\u0088\2\u0728"+
		"\u0726\3\2\2\2\u0728\u0727\3\2\2\2\u0729\u0125\3\2\2\2\u072a\u0743\5\u0130"+
		"\u0099\2\u072b\u072c\5\u014c\u00a7\2\u072c\u072e\5\u0136\u009c\2\u072d"+
		"\u072f\5\u012e\u0098\2\u072e\u072d\3\2\2\2\u072e\u072f\3\2\2\2\u072f\u0744"+
		"\3\2\2\2\u0730\u0732\5\u0128\u0095\2\u0731\u0730\3\2\2\2\u0732\u0733\3"+
		"\2\2\2\u0733\u0731\3\2\2\2\u0733\u0734\3\2\2\2\u0734\u0735\3\2\2\2\u0735"+
		"\u0736\5\u014c\u00a7\2\u0736\u0737\5\u012c\u0097\2\u0737\u0744\3\2\2\2"+
		"\u0738\u073a\5\u012a\u0096\2\u0739\u0738\3\2\2\2\u073a\u073b\3\2\2\2\u073b"+
		"\u0739\3\2\2\2\u073b\u073c\3\2\2\2\u073c\u0740\3\2\2\2\u073d\u073f\5\u0128"+
		"\u0095\2\u073e\u073d\3\2\2\2\u073f\u0742\3\2\2\2\u0740\u073e\3\2\2\2\u0740"+
		"\u0741\3\2\2\2\u0741\u0744\3\2\2\2\u0742\u0740\3\2\2\2\u0743\u072b\3\2"+
		"\2\2\u0743\u0731\3\2\2\2\u0743\u0739\3\2\2\2\u0744\u0127\3\2\2\2\u0745"+
		"\u0746\5\u0088E\2\u0746\u0747\7b\2\2\u0747\u0748\7c\2\2\u0748\u0129\3"+
		"\2\2\2\u0749\u074a\5\u0088E\2\u074a\u074b\7b\2\2\u074b\u074c\5\u00f6|"+
		"\2\u074c\u074d\7c\2\2\u074d\u012b\3\2\2\2\u074e\u074f\7`\2\2\u074f\u0762"+
		"\5\u014c\u00a7\2\u0750\u0753\5\u012c\u0097\2\u0751\u0753\5D#\2\u0752\u0750"+
		"\3\2\2\2\u0752\u0751\3\2\2\2\u0753\u0754\3\2\2\2\u0754\u075f\5\u014c\u00a7"+
		"\2\u0755\u0756\7e\2\2\u0756\u0759\5\u014c\u00a7\2\u0757\u075a\5\u012c"+
		"\u0097\2\u0758\u075a\5D#\2\u0759\u0757\3\2\2\2\u0759\u0758\3\2\2\2\u075a"+
		"\u075b\3\2\2\2\u075b\u075c\5\u014c\u00a7\2\u075c\u075e\3\2\2\2\u075d\u0755"+
		"\3\2\2\2\u075e\u0761\3\2\2\2\u075f\u075d\3\2\2\2\u075f\u0760\3\2\2\2\u0760"+
		"\u0763\3\2\2\2\u0761\u075f\3\2\2\2\u0762\u0752\3\2\2\2\u0762\u0763\3\2"+
		"\2\2\u0763\u0765\3\2\2\2\u0764\u0766\7e\2\2\u0765\u0764\3\2\2\2\u0765"+
		"\u0766\3\2\2\2\u0766\u0767\3\2\2\2\u0767\u0768\5\u014c\u00a7\2\u0768\u0769"+
		"\7a\2\2\u0769\u012d\3\2\2\2\u076a\u076b\5*\26\2\u076b\u012f\3\2\2\2\u076c"+
		"\u0772\5\u0088E\2\u076d\u0773\5H%\2\u076e\u0770\5j\66\2\u076f\u0771\5"+
		"\u0134\u009b\2\u0770\u076f\3\2\2\2\u0770\u0771\3\2\2\2\u0771\u0773\3\2"+
		"\2\2\u0772\u076d\3\2\2\2\u0772\u076e\3\2\2\2\u0773\u0131\3\2\2\2\u0774"+
		"\u0775\7i\2\2\u0775\u0776\5\u014c\u00a7\2\u0776\u0777\5&\24\2\u0777\u0778"+
		"\5\u014c\u00a7\2\u0778\u0779\7h\2\2\u0779\u0133\3\2\2\2\u077a\u077b\7"+
		"i\2\2\u077b\u077e\7h\2\2\u077c\u077e\5R*\2\u077d\u077a\3\2\2\2\u077d\u077c"+
		"\3\2\2\2\u077e\u0135\3\2\2\2\u077f\u0781\7^\2\2\u0780\u0782\5\u013a\u009e"+
		"\2\u0781\u0780\3\2\2\2\u0781\u0782\3\2\2\2\u0782\u0784\3\2\2\2\u0783\u0785"+
		"\7e\2\2\u0784\u0783\3\2\2\2\u0784\u0785\3\2\2\2\u0785\u0786\3\2\2\2\u0786"+
		"\u0787\7_\2\2\u0787\u0137\3\2\2\2\u0788\u078f\5\u013c\u009f\2\u0789\u078a"+
		"\7e\2\2\u078a\u078b\5\u014c\u00a7\2\u078b\u078c\5\u013e\u00a0\2\u078c"+
		"\u078e\3\2\2\2\u078d\u0789\3\2\2\2\u078e\u0791\3\2\2\2\u078f\u078d\3\2"+
		"\2\2\u078f\u0790\3\2\2\2\u0790\u0139\3\2\2\2\u0791\u078f\3\2\2\2\u0792"+
		"\u0799\5\u0140\u00a1\2\u0793\u0794\7e\2\2\u0794\u0795\5\u014c\u00a7\2"+
		"\u0795\u0796\5\u0140\u00a1\2\u0796\u0798\3\2\2\2\u0797\u0793\3\2\2\2\u0798"+
		"\u079b\3\2\2\2\u0799\u0797\3\2\2\2\u0799\u079a\3\2\2\2\u079a\u013b\3\2"+
		"\2\2\u079b\u0799\3\2\2\2\u079c\u079f\5\u00e6t\2\u079d\u079f\5\u011e\u0090"+
		"\2\u079e\u079c\3\2\2\2\u079e\u079d\3\2\2\2\u079f\u013d\3\2\2\2\u07a0\u07a3"+
		"\5\u00e6t\2\u07a1\u07a3\5\u011c\u008f\2\u07a2\u07a0\3\2\2\2\u07a2\u07a1"+
		"\3\2\2\2\u07a3\u013f\3\2\2\2\u07a4\u07a8\5\u00e6t\2\u07a5\u07a8\5x=\2"+
		"\u07a6\u07a8\5\u011c\u008f\2\u07a7\u07a4\3\2\2\2\u07a7\u07a5\3\2\2\2\u07a7"+
		"\u07a6\3\2\2\2\u07a8\u0141\3\2\2\2\u07a9\u07aa\7\3\2\2\u07aa\u0143\3\2"+
		"\2\2\u07ab\u07ac\7\u008a\2\2\u07ac\u0145\3\2\2\2\u07ad\u07ae\t\27\2\2"+
		"\u07ae\u0147\3\2\2\2\u07af\u07b0\t\30\2\2\u07b0\u0149\3\2\2\2\u07b1\u07b2"+
		"\t\31\2\2\u07b2\u014b\3\2\2\2\u07b3\u07b5\7\u008f\2\2\u07b4\u07b3\3\2"+
		"\2\2\u07b5\u07b8\3\2\2\2\u07b6\u07b4\3\2\2\2\u07b6\u07b7\3\2\2\2\u07b7"+
		"\u014d\3\2\2\2\u07b8\u07b6\3\2\2\2\u07b9\u07bb\t\32\2\2\u07ba\u07b9\3"+
		"\2\2\2\u07bb\u07bc\3\2\2\2\u07bc\u07ba\3\2\2\2\u07bc\u07bd\3\2\2\2\u07bd"+
		"\u014f\3\2\2\2\u00db\u0153\u0155\u0158\u0162\u0166\u016d\u0176\u017d\u0184"+
		"\u018b\u0190\u0198\u019f\u01a2\u01aa\u01af\u01b3\u01b8\u01c0\u01cc\u01d8"+
		"\u01e1\u01eb\u01fb\u0201\u0206\u020d\u0214\u021b\u0227\u022c\u0231\u023a"+
		"\u023d\u0245\u0248\u024a\u024d\u0259\u025f\u0262\u0266\u026a\u0271\u0273"+
		"\u0277\u027c\u028c\u0291\u0299\u029d\u02a8\u02b1\u02bb\u02c0\u02c7\u02cb"+
		"\u02d0\u02d5\u02da\u02e5\u02f2\u02f4\u0300\u0305\u030b\u0313\u031b\u031e"+
		"\u0326\u032f\u0337\u033e\u034a\u0352\u035a\u0361\u0367\u037a\u037e\u0385"+
		"\u0389\u038c\u0393\u0396\u039e\u03a2\u03aa\u03af\u03b6\u03ba\u03be\u03c7"+
		"\u03d2\u03d7\u03df\u03e3\u03e5\u03ee\u03f1\u03f5\u03f9\u0400\u0408\u0411"+
		"\u0416\u041e\u0429\u042e\u0434\u0437\u0441\u044b\u0451\u0459\u045f\u0469"+
		"\u046d\u0474\u048a\u048e\u0492\u0499\u04a2\u04a8\u04b1\u04be\u04d8\u04de"+
		"\u04ea\u04f5\u04ff\u0504\u050c\u0518\u051c\u0521\u0525\u052c\u0531\u0535"+
		"\u0539\u053d\u054c\u0551\u0560\u0564\u056a\u056e\u0574\u057e\u0589\u0590"+
		"\u05a6\u05b2\u05b5\u05c5\u05e0\u05e3\u0629\u063f\u0641\u064e\u0654\u0659"+
		"\u0660\u0663\u0668\u066f\u067d\u0684\u0686\u068e\u0699\u069f\u06a3\u06a7"+
		"\u06ae\u06b4\u06c3\u06cb\u06d0\u06d5\u06d9\u06dc\u06e3\u06e6\u06ef\u06f7"+
		"\u0704\u0710\u071c\u0720\u0724\u0728\u072e\u0733\u073b\u0740\u0743\u0752"+
		"\u0759\u075f\u0762\u0765\u0770\u0772\u077d\u0781\u0784\u078f\u0799\u079e"+
		"\u07a2\u07a7\u07b6\u07bc";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
	}
}