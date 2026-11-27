// Generated from GroovyParser.g4 by ANTLR 4.13.2.20
package org.apache.groovy.parser.antlr4;

    import java.util.Map;
    import org.codehaus.groovy.ast.NodeMetaDataHandler;

import groovyjarjarantlr4.v4.runtime.atn.*;
import groovyjarjarantlr4.v4.runtime.dfa.DFA;
import groovyjarjarantlr4.v4.runtime.*;
import groovyjarjarantlr4.v4.runtime.misc.*;
import groovyjarjarantlr4.v4.runtime.tree.*;
import java.util.List;
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
		RULE_referenceType = 36, RULE_matchingType = 37, RULE_notInstanceofType = 38, 
		RULE_standardType = 39, RULE_standardClassOrInterfaceType = 40, RULE_typeArguments = 41, 
		RULE_typeArgument = 42, RULE_annotatedQualifiedClassName = 43, RULE_qualifiedClassNameList = 44, 
		RULE_formalParameters = 45, RULE_formalParameterList = 46, RULE_thisFormalParameter = 47, 
		RULE_formalParameter = 48, RULE_methodBody = 49, RULE_qualifiedName = 50, 
		RULE_qualifiedNameElement = 51, RULE_qualifiedNameElements = 52, RULE_qualifiedClassName = 53, 
		RULE_qualifiedStandardClassName = 54, RULE_literal = 55, RULE_gstring = 56, 
		RULE_gstringValue = 57, RULE_gstringPath = 58, RULE_lambdaExpression = 59, 
		RULE_standardLambdaExpression = 60, RULE_lambdaParameters = 61, RULE_standardLambdaParameters = 62, 
		RULE_lambdaBody = 63, RULE_closure = 64, RULE_closureOrLambdaExpression = 65, 
		RULE_blockStatementsOpt = 66, RULE_blockStatements = 67, RULE_annotationsOpt = 68, 
		RULE_annotation = 69, RULE_elementValues = 70, RULE_annotationName = 71, 
		RULE_elementValuePairs = 72, RULE_elementValuePair = 73, RULE_elementValuePairName = 74, 
		RULE_elementValue = 75, RULE_elementValueArrayInitializer = 76, RULE_block = 77, 
		RULE_blockStatement = 78, RULE_localVariableDeclaration = 79, RULE_variableDeclaration = 80, 
		RULE_typeNamePairs = 81, RULE_typeNamePair = 82, RULE_keyedPair = 83, 
		RULE_variableNames = 84, RULE_conditionalStatement = 85, RULE_ifElseStatement = 86, 
		RULE_switchStatement = 87, RULE_loopStatement = 88, RULE_continueStatement = 89, 
		RULE_breakStatement = 90, RULE_yieldStatement = 91, RULE_tryCatchStatement = 92, 
		RULE_assertStatement = 93, RULE_statement = 94, RULE_catchClause = 95, 
		RULE_catchType = 96, RULE_finallyBlock = 97, RULE_resources = 98, RULE_resourceList = 99, 
		RULE_resource = 100, RULE_switchBlockStatementGroup = 101, RULE_switchLabel = 102, 
		RULE_forControl = 103, RULE_enhancedForControl = 104, RULE_indexVariable = 105, 
		RULE_originalForControl = 106, RULE_forInit = 107, RULE_forUpdate = 108, 
		RULE_castParExpression = 109, RULE_intersectionType = 110, RULE_coercionType = 111, 
		RULE_parExpression = 112, RULE_expressionInPar = 113, RULE_expressionList = 114, 
		RULE_expressionListElement = 115, RULE_enhancedExpression = 116, RULE_enhancedStatementExpression = 117, 
		RULE_statementExpression = 118, RULE_postfixExpression = 119, RULE_switchExpression = 120, 
		RULE_switchBlockStatementExpressionGroup = 121, RULE_switchExpressionLabel = 122, 
		RULE_expression = 123, RULE_castOperandExpression = 124, RULE_commandExpression = 125, 
		RULE_commandArgument = 126, RULE_pathExpression = 127, RULE_pathElement = 128, 
		RULE_namePart = 129, RULE_dynamicMemberName = 130, RULE_indexPropertyArgs = 131, 
		RULE_namedPropertyArgs = 132, RULE_primary = 133, RULE_namedPropertyArgPrimary = 134, 
		RULE_namedArgPrimary = 135, RULE_commandPrimary = 136, RULE_list = 137, 
		RULE_map = 138, RULE_mapEntryList = 139, RULE_namedPropertyArgList = 140, 
		RULE_mapEntry = 141, RULE_namedPropertyArg = 142, RULE_namedArg = 143, 
		RULE_mapEntryLabel = 144, RULE_namedPropertyArgLabel = 145, RULE_namedArgLabel = 146, 
		RULE_creator = 147, RULE_dim0 = 148, RULE_dim1 = 149, RULE_arrayInitializer = 150, 
		RULE_anonymousInnerClassDeclaration = 151, RULE_createdName = 152, RULE_nonWildcardTypeArguments = 153, 
		RULE_typeArgumentsOrDiamond = 154, RULE_arguments = 155, RULE_argumentList = 156, 
		RULE_enhancedArgumentListInPar = 157, RULE_firstArgumentListElement = 158, 
		RULE_argumentListElement = 159, RULE_enhancedArgumentListElement = 160, 
		RULE_stringLiteral = 161, RULE_className = 162, RULE_identifier = 163, 
		RULE_builtInType = 164, RULE_keywords = 165, RULE_sep = 166;
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
			"notInstanceofType", "standardType", "standardClassOrInterfaceType", 
			"typeArguments", "typeArgument", "annotatedQualifiedClassName", "qualifiedClassNameList", 
			"formalParameters", "formalParameterList", "thisFormalParameter", "formalParameter", 
			"methodBody", "qualifiedName", "qualifiedNameElement", "qualifiedNameElements", 
			"qualifiedClassName", "qualifiedStandardClassName", "literal", "gstring", 
			"gstringValue", "gstringPath", "lambdaExpression", "standardLambdaExpression", 
			"lambdaParameters", "standardLambdaParameters", "lambdaBody", "closure", 
			"closureOrLambdaExpression", "blockStatementsOpt", "blockStatements", 
			"annotationsOpt", "annotation", "elementValues", "annotationName", "elementValuePairs", 
			"elementValuePair", "elementValuePairName", "elementValue", "elementValueArrayInitializer", 
			"block", "blockStatement", "localVariableDeclaration", "variableDeclaration", 
			"typeNamePairs", "typeNamePair", "keyedPair", "variableNames", "conditionalStatement", 
			"ifElseStatement", "switchStatement", "loopStatement", "continueStatement", 
			"breakStatement", "yieldStatement", "tryCatchStatement", "assertStatement", 
			"statement", "catchClause", "catchType", "finallyBlock", "resources", 
			"resourceList", "resource", "switchBlockStatementGroup", "switchLabel", 
			"forControl", "enhancedForControl", "indexVariable", "originalForControl", 
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
			"className", "identifier", "builtInType", "keywords", "sep"
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
			null, "';'", "','", "'.'", "'='", "'>'", "'<'", "'!'", "'~'", "'?'", 
			"':'", "'=='", "'<='", "'>='", "'!='", "'&&'", "'||'", "'++'", "'--'", 
			"'+'", "'-'", "'*'", null, "'&'", "'|'", "'^'", "'%'", "'+='", "'-='", 
			"'*='", "'/='", "'&='", "'|='", "'^='", "'%='", "'<<='", "'>>='", "'>>>='", 
			"'?='", null, null, "'@'", "'...'"
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
	    private int inAsyncClosureLevel = 0;

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
		public TerminalNode EOF() { return getToken(GroovyParser.EOF, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			_matchStarPredict(337, 334, 339, 0, 1, NL);

			if ( _syncPredict(344, 2)==1 ) {
				{
				setState(340);
				packageDeclaration();

				if ( _syncPredict(342, 1)==1 ) {
					{
					setState(341);
					sep();
					}
				}

				}
			}

			if ( _syncPredict(347, 3)==1 ) {
				{
				setState(346);
				scriptStatements();
				}
			}

			setState(349);
			_matchEOF();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			setState(351);
			scriptStatement();

			_alt = _syncPredict(357, 4);
			while (_alt==1) {
				{
				{
				setState(352);
				sep();
				setState(353);
				scriptStatement();
				}
				}
				_alt = _syncPredict(359, 4);
			}

			_sync(361);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(360);
				sep();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			switch ( _syncPredict(368, 6) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(363);
				importDeclaration();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(364);
				typeDeclaration();
				}

				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(365);
				if (!( !SemanticPredicates.isInvalidMethodDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidMethodDeclaration(_input) ");
				setState(366);
				methodDeclaration(3, 9);
				}

				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(367);
				statement();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			setState(370);
			annotationsOpt();
			setState(371);
			_match(PACKAGE);
			setState(372);
			qualifiedName();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode MODULE() { return getToken(GroovyParser.MODULE, 0); }
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
			enterOuterAlt(_localctx, 1);
			{
			setState(374);
			annotationsOpt();
			setState(375);
			_match(IMPORT);

			switch ( _syncPredict(388, 9) ) {
			case 1:
				{
				setState(376);
				_match(MODULE);
				setState(377);
				qualifiedName();
				}
				break;
			case 2:
				{
				_sync(379);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(378);
					_match(STATIC);
					}
				}

				setState(381);
				qualifiedName();
				_sync(386);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(382);
					_match(DOT);
					setState(383);
					_match(MUL);
					}
				}
				else if (_la==AS) {
					{
					setState(384);
					_match(AS);
					setState(385);
					_localctx.alias = identifier();
					}
				}

				}
				break;
			}
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			setState(390);
			classOrInterfaceModifiersOpt();
			setState(391);
			classDeclaration();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			_sync(395);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABSTRACT) | (1L << DEFAULT) | (1L << FINAL) | (1L << NON_SEALED) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP))) != 0) || _la==AT) {
				enterOuterAlt(_localctx, 1);
				{
				setState(393);
				classOrInterfaceModifier();
				}

			}
			else if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << NATIVE) | (1L << SYNCHRONIZED) | (1L << TRANSIENT) | (1L << VAL) | (1L << VAR) | (1L << VOLATILE))) != 0)) {
				enterOuterAlt(_localctx, 2);
				{
				setState(394);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
					_localctx.m = _matchSet(_st, (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << NATIVE) | (1L << SYNCHRONIZED) | (1L << TRANSIENT) | (1L << VAL) | (1L << VAR) | (1L << VOLATILE))) != 0));
				}
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			if ( _syncPredict(404, 12)==1 ) {
				{
				setState(397);
				modifiers();

				_matchStar(401, 398, 403, NL);

				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(406);
			modifier();

			_alt = _syncPredict(416, 14);
			while (_alt==1) {
				{
				{
				_matchStar(410, 407, 412, NL);

				setState(413);
				modifier();
				}
				}
				_alt = _syncPredict(418, 14);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			if ( _syncPredict(426, 16)==1 ) {
				{
				setState(419);
				classOrInterfaceModifiers();

				_matchStar(423, 420, 425, NL);

				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(428);
			classOrInterfaceModifier();

			_alt = _syncPredict(438, 18);
			while (_alt==1) {
				{
				{
				_matchStar(432, 429, 434, NL);

				setState(435);
				classOrInterfaceModifier();
				}
				}
				_alt = _syncPredict(440, 18);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			_sync(443);
			_la = _input.LA(1);
			if (_la==AT) {
				enterOuterAlt(_localctx, 1);
				{
				setState(441);
				annotation();
				}

			}
			else if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABSTRACT) | (1L << DEFAULT) | (1L << FINAL) | (1L << NON_SEALED) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP))) != 0)) {
				enterOuterAlt(_localctx, 2);
				{
				setState(442);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
					_localctx.m = _matchSet(_st, (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABSTRACT) | (1L << DEFAULT) | (1L << FINAL) | (1L << NON_SEALED) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP))) != 0));
				}
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			_sync(447);
			_la = _input.LA(1);
			if (_la==AT) {
				enterOuterAlt(_localctx, 1);
				{
				setState(445);
				annotation();
				}

			}
			else if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << ABSTRACT) | (1L << FINAL) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << STATIC) | (1L << STRICTFP) | (1L << VAL) | (1L << VAR))) != 0)) {
				enterOuterAlt(_localctx, 2);
				{
				setState(446);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
					_localctx.m = _matchSet(_st, (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << ABSTRACT) | (1L << FINAL) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << STATIC) | (1L << STRICTFP) | (1L << VAL) | (1L << VAR))) != 0));
				}
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			if ( _syncPredict(456, 22)==1 ) {
				{
				setState(449);
				variableModifiers();

				_matchStar(453, 450, 455, NL);

				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(458);
			variableModifier();

			_alt = _syncPredict(468, 24);
			while (_alt==1) {
				{
				{
				_matchStar(462, 459, 464, NL);

				setState(465);
				variableModifier();
				}
				}
				_alt = _syncPredict(470, 24);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeParametersContext extends GroovyParserRuleContext {
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public List<? extends TypeParameterContext> typeParameter() {
			return getRuleContexts(TypeParameterContext.class);
		}
		public TypeParameterContext typeParameter(int i) {
			return getRuleContext(TypeParameterContext.class,i);
		}
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
			setState(471);
			_match(LT);

			_matchStar(475, 472, 477, NL);

			setState(478);
			typeParameter();

			_sync(489);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(479);
				_match(COMMA);

				_matchStar(483, 480, 485, NL);

				setState(486);
				typeParameter();
				}
				}
				_sync(491);
				_la = _input.LA(1);
			}

			_matchStar(495, 492, 497, NL);

			setState(498);
			_match(GT);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TypeBoundContext typeBound() {
			return getRuleContext(TypeBoundContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(500);
			annotationsOpt();
			setState(501);
			className();
			_sync(510);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(502);
				_match(EXTENDS);

				_matchStar(506, 503, 508, NL);

				setState(509);
				typeBound();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(512);
			type();

			_sync(523);
			_la = _input.LA(1);
			while (_la==BITAND) {
				{
				{
				setState(513);
				_match(BITAND);

				_matchStar(517, 514, 519, NL);

				setState(520);
				type();
				}
				}
				_sync(525);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(526);
			type();

			_sync(537);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(527);
				_match(COMMA);

				_matchStar(531, 528, 533, NL);

				setState(534);
				type();
				}
				}
				_sync(539);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
			_sync(553);
			switch (_input.LA(1)) {
			case CLASS:
				{
				setState(540);
				_match(CLASS);
				 _localctx.t =  0; 
				}
				break;
			case INTERFACE:
				{
				setState(542);
				_match(INTERFACE);
				 _localctx.t =  1; 
				}
				break;
			case ENUM:
				{
				setState(544);
				_match(ENUM);
				 _localctx.t =  2; 
				}
				break;
			case AT:
				{
				setState(546);
				_match(AT);
				setState(547);
				_match(INTERFACE);
				 _localctx.t =  3; 
				}
				break;
			case TRAIT:
				{
				setState(549);
				_match(TRAIT);
				 _localctx.t =  4; 
				}
				break;
			case RECORD:
				{
				setState(551);
				_match(RECORD);
				 _localctx.t =  5; 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}

			setState(555);
			identifier();

			if ( _syncPredict(563, 37)==1 ) {
				{
				_matchStar(559, 556, 561, NL);

				setState(562);
				typeParameters();
				}
			}

			if ( _syncPredict(572, 39)==1 ) {
				{
				_matchStar(568, 565, 570, NL);

				setState(571);
				formalParameters();
				}
			}

			if ( _syncPredict(588, 42)==1 ) {
				{
				_matchStar(577, 574, 579, NL);

				setState(580);
				_match(EXTENDS);

				_matchStar(584, 581, 586, NL);

				setState(587);
				_localctx.scs = typeList();
				}
			}

			if ( _syncPredict(604, 45)==1 ) {
				{
				_matchStar(593, 590, 595, NL);

				setState(596);
				_match(IMPLEMENTS);

				_matchStar(600, 597, 602, NL);

				setState(603);
				_localctx.is = typeList();
				}
			}

			if ( _syncPredict(620, 48)==1 ) {
				{
				_matchStar(609, 606, 611, NL);

				setState(612);
				_match(PERMITS);

				_matchStar(616, 613, 618, NL);

				setState(619);
				_localctx.ps = typeList();
				}
			}

			_matchStar(625, 622, 627, NL);

			setState(628);
			classBody(_localctx.t);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassBodyContext extends GroovyParserRuleContext {
		public int t;
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public EnumConstantsContext enumConstants() {
			return getRuleContext(EnumConstantsContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(630);
			_match(LBRACE);

			_matchStarPredict(634, 631, 636, 50, 1, NL);

			switch ( _syncPredict(694, 62) ) {
			case 1:
				{
				setState(637);
				if (!( _localctx.t == 2 )) throw createFailedPredicateException(" $t == 2 ");
				setState(638);
				enumConstants();

				switch ( _syncPredict(681, 59) ) {
				case 1:
					{
					if ( _syncPredict(646, 52)==1 ) {
						{
						_matchStar(642, 639, 644, NL);

						setState(645);
						_match(COMMA);
						}
					}

					}
					break;
				case 2:
					{
					if ( _syncPredict(664, 56)==1 ) {
						{
						if ( _syncPredict(655, 54)==1 ) {
							{
							_matchStar(651, 648, 653, NL);

							setState(654);
							_match(COMMA);
							}
						}

						_matchStar(660, 657, 662, NL);

						setState(663);
						_match(SEMI);
						}
					}

					_matchStar(669, 666, 671, NL);

					setState(672);
					classBodyDeclaration(_localctx.t);

					_alt = _syncPredict(678, 58);
					while (_alt==1) {
						{
						{
						setState(673);
						sep();
						setState(674);
						classBodyDeclaration(_localctx.t);
						}
						}
						_alt = _syncPredict(680, 58);
					}

					}
					break;
				}
				}
				break;
			case 2:
				{
				_sync(692);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << ASYNC) | (1L << AWAIT) | (1L << DEFER) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << CLASS) | (1L << DEFAULT) | (1L << ENUM) | (1L << FINAL) | (1L << INTERFACE) | (1L << MODULE) | (1L << NATIVE) | (1L << NON_SEALED) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << SYNCHRONIZED) | (1L << TRANSIENT) | (1L << VAL) | (1L << VAR) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & ((1L << (YIELD - 65)) | (1L << (LBRACE - 65)) | (1L << (LT - 65)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)))) != 0)) {
					{
					setState(683);
					classBodyDeclaration(_localctx.t);

					_alt = _syncPredict(689, 60);
					while (_alt==1) {
						{
						{
						setState(684);
						sep();
						setState(685);
						classBodyDeclaration(_localctx.t);
						}
						}
						_alt = _syncPredict(691, 60);
					}

					}
				}

				}
				break;
			}
			_sync(697);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(696);
				sep();
				}
			}

			setState(699);
			_match(RBRACE);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(701);
			enumConstant();

			_alt = _syncPredict(718, 66);
			while (_alt==1) {
				{
				{
				_matchStar(705, 702, 707, NL);

				setState(708);
				_match(COMMA);

				_matchStar(712, 709, 714, NL);

				setState(715);
				enumConstant();
				}
				}
				_alt = _syncPredict(720, 66);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			setState(721);
			annotationsOpt();
			setState(722);
			identifier();
			_sync(724);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(723);
				arguments();
				}
			}

			if ( _syncPredict(727, 68)==1 ) {
				{
				setState(726);
				anonymousInnerClassDeclaration(1);
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			switch ( _syncPredict(740, 71) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				_sync(736);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(729);
					_match(STATIC);

					_matchStar(733, 730, 735, NL);

					}
				}

				setState(738);
				block();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(739);
				memberDeclaration(_localctx.t);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			switch ( _syncPredict(749, 73) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(742);
				methodDeclaration(0, _localctx.t);
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(743);
				fieldDeclaration();
				}

				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(744);
				modifiersOpt();

				switch ( _syncPredict(747, 72) ) {
				case 1:
					{
					setState(745);
					classDeclaration();
					}
					break;
				case 2:
					{
					setState(746);
					compactConstructorDeclaration();
					}
					break;
				}
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(751);
			modifiersOpt();
			_sync(753);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(752);
				typeParameters();
				}
			}

			if ( _syncPredict(762, 76)==1 ) {
				{
				setState(755);
				returnType(_localctx.ct);

				_matchStar(759, 756, 761, NL);

				}
			}

			setState(764);
			methodName();
			setState(765);
			formalParameters();

			switch ( _syncPredict(805, 83) ) {
			case 1:
				{
				setState(766);
				if (!( _localctx.ct == 3 )) throw createFailedPredicateException(" $ct == 3 ");
				{
				setState(767);
				_match(DEFAULT);

				_matchStarPredict(771, 768, 773, 77, 1, NL);

				setState(774);
				elementValue();
				}
				}
				break;
			case 2:
				{
				_matchStar(778, 775, 780, NL);

				setState(781);
				_match(THROWS);

				_matchStar(785, 782, 787, NL);

				setState(788);
				qualifiedClassNameList();

				if ( _syncPredict(796, 81)==1 ) {
					{
					_matchStar(792, 789, 794, NL);

					setState(795);
					methodBody();
					}
				}

				}
				break;
			case 3:
				{
				_matchStar(801, 798, 803, NL);

				setState(804);
				methodBody();
				}
				break;
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public MethodBodyContext methodBody() {
			return getRuleContext(MethodBodyContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
			setState(807);
			methodName();

			_matchStar(811, 808, 813, NL);

			setState(814);
			methodBody();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		int _la;
		try {
			_sync(818);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				enterOuterAlt(_localctx, 1);
				{
				setState(816);
				identifier();
				}

			}
			else if (_la==StringLiteral) {
				enterOuterAlt(_localctx, 2);
				{
				setState(817);
				stringLiteral();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		int _la;
		try {
			_sync(822);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)))) != 0)) {
				enterOuterAlt(_localctx, 1);
				{
				setState(820);
				standardType();
				}

			}
			else if (_la==VOID) {
				enterOuterAlt(_localctx, 2);
				{
				setState(821);
				_match(VOID);
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			setState(824);
			variableDeclaration(1);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDeclaratorsContext extends GroovyParserRuleContext {
		public boolean arrayType;
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public VariableDeclaratorsContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public VariableDeclaratorsContext(ParserRuleContext parent, int invokingState, boolean arrayType) {
			super(parent, invokingState);
			this.arrayType = arrayType;
		}
		@Override public int getRuleIndex() { return RULE_variableDeclarators; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableDeclarators(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableDeclaratorsContext variableDeclarators(boolean arrayType) throws RecognitionException {
		VariableDeclaratorsContext _localctx = new VariableDeclaratorsContext(_ctx, getState(), arrayType);
		enterRule(_localctx, 60, RULE_variableDeclarators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(826);
			variableDeclarator(_localctx.arrayType);

			_sync(837);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(827);
				_match(COMMA);

				_matchStar(831, 828, 833, NL);

				setState(834);
				variableDeclarator(_localctx.arrayType);
				}
				}
				_sync(839);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDeclaratorContext extends GroovyParserRuleContext {
		public boolean arrayType;
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public VariableInitializerContext variableInitializer() {
			return getRuleContext(VariableInitializerContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public VariableDeclaratorContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public VariableDeclaratorContext(ParserRuleContext parent, int invokingState, boolean arrayType) {
			super(parent, invokingState);
			this.arrayType = arrayType;
		}
		@Override public int getRuleIndex() { return RULE_variableDeclarator; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableDeclaratorContext variableDeclarator(boolean arrayType) throws RecognitionException {
		VariableDeclaratorContext _localctx = new VariableDeclaratorContext(_ctx, getState(), arrayType);
		enterRule(_localctx, 62, RULE_variableDeclarator);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(840);
			variableDeclaratorId();

			if ( _syncPredict(859, 92)==1 ) {
				{
				_matchStar(844, 841, 846, NL);

				setState(847);
				_match(ASSIGN);

				_matchStarPredict(851, 848, 853, 90, 1, NL);

				switch ( _syncPredict(857, 91) ) {
				case 1:
					{
					setState(854);
					if (!(_localctx.arrayType)) throw createFailedPredicateException("$arrayType");
					setState(855);
					arrayInitializer();
					}
					break;
				case 2:
					{
					setState(856);
					variableInitializer();
					}
					break;
				}
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public BuiltInTypeContext builtInType() {
			return getRuleContext(BuiltInTypeContext.class,0);
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
		int _la;
		try {
			_sync(863);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				enterOuterAlt(_localctx, 1);
				{
				setState(861);
				identifier();
				}

			}
			else if (_la==BuiltInPrimitiveType || _la==VOID) {
				enterOuterAlt(_localctx, 2);
				{
				setState(862);
				builtInType();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			setState(865);
			enhancedStatementExpression();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(867);
			annotationsOpt();

			_sync(871);
			_la = _input.LA(1);
			if (_la==VOID) {
				{
				setState(868);
				_match(VOID);
				}
			}
			else if (_la==BuiltInPrimitiveType) {
				{
				setState(869);
				primitiveType();
				}
			}
			else if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(870);
				referenceType();
				}
			}
			else {
				throw new NoViableAltException(this);
			}

			_alt = _syncPredict(876, 95);
			while (_alt==1) {
				{
				{
				setState(873);
				dim0();
				}
				}
				_alt = _syncPredict(878, 95);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			setState(879);
			_match(BuiltInPrimitiveType);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TypeArgumentsContext> typeArguments() {
			return getRuleContexts(TypeArgumentsContext.class);
		}
		public TypeArgumentsContext typeArguments(int i) {
			return getRuleContext(TypeArgumentsContext.class,i);
		}
		public List<? extends TerminalNode> DOT() { return getTokens(GroovyParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(GroovyParser.DOT, i);
		}
		public List<? extends IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public QualifiedStandardClassNameContext qualifiedStandardClassName() {
			return getRuleContext(QualifiedStandardClassNameContext.class,0);
		}
		public List<? extends ClassNameContext> className() {
			return getRuleContexts(ClassNameContext.class);
		}
		public ClassNameContext className(int i) {
			return getRuleContext(ClassNameContext.class,i);
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
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(881);
			qualifiedClassName();

			if ( _syncPredict(893, 98)==1 ) {
				{
				setState(882);
				typeArguments();

				_alt = _syncPredict(890, 97);
				while (_alt==1) {
					{
					{
					setState(883);
					_match(DOT);
					setState(884);
					identifier();

					if ( _syncPredict(886, 96)==1 ) {
						{
						setState(885);
						typeArguments();
						}
					}

					}
					}
					_alt = _syncPredict(892, 97);
				}

				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			setState(895);
			standardType();

			if ( _syncPredict(897, 99)==1 ) {
				{
				setState(896);
				identifier();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NotInstanceofTypeContext extends GroovyParserRuleContext {
		public MatchingTypeContext matchingType() {
			return getRuleContext(MatchingTypeContext.class,0);
		}
		public CastParExpressionContext castParExpression() {
			return getRuleContext(CastParExpressionContext.class,0);
		}
		public NotInstanceofTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notInstanceofType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitNotInstanceofType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final NotInstanceofTypeContext notInstanceofType() throws RecognitionException {
		NotInstanceofTypeContext _localctx = new NotInstanceofTypeContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_notInstanceofType);
		int _la;
		try {
			_sync(901);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)))) != 0)) {
				enterOuterAlt(_localctx, 1);
				{
				setState(899);
				matchingType();
				}

			}
			else if (_la==LPAREN) {
				enterOuterAlt(_localctx, 2);
				{
				setState(900);
				castParExpression();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final TypeContext standardType() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_standardType);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(903);
			annotationsOpt();

			_sync(906);
			_la = _input.LA(1);
			if (_la==BuiltInPrimitiveType) {
				{
				setState(904);
				primitiveType();
				}
			}
			else if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(905);
				standardClassOrInterfaceType();
				}
			}
			else {
				throw new NoViableAltException(this);
			}

			_alt = _syncPredict(911, 102);
			while (_alt==1) {
				{
				{
				setState(908);
				dim0();
				}
				}
				_alt = _syncPredict(913, 102);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final ReferenceTypeContext standardClassOrInterfaceType() throws RecognitionException {
		ReferenceTypeContext _localctx = new ReferenceTypeContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_standardClassOrInterfaceType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(914);
			qualifiedStandardClassName();

			if ( _syncPredict(926, 105)==1 ) {
				{
				setState(915);
				typeArguments();

				_alt = _syncPredict(923, 104);
				while (_alt==1) {
					{
					{
					setState(916);
					_match(DOT);
					setState(917);
					className();

					if ( _syncPredict(919, 103)==1 ) {
						{
						setState(918);
						typeArguments();
						}
					}

					}
					}
					_alt = _syncPredict(925, 104);
				}

				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeArgumentsContext extends GroovyParserRuleContext {
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public List<? extends TypeArgumentContext> typeArgument() {
			return getRuleContexts(TypeArgumentContext.class);
		}
		public TypeArgumentContext typeArgument(int i) {
			return getRuleContext(TypeArgumentContext.class,i);
		}
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 82, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(928);
			_match(LT);

			_matchStar(932, 929, 934, NL);

			setState(935);
			typeArgument();

			_sync(946);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(936);
				_match(COMMA);

				_matchStar(940, 937, 942, NL);

				setState(943);
				typeArgument();
				}
				}
				_sync(948);
				_la = _input.LA(1);
			}

			_matchStar(952, 949, 954, NL);

			setState(955);
			_match(GT);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode EXTENDS() { return getToken(GroovyParser.EXTENDS, 0); }
		public TerminalNode SUPER() { return getToken(GroovyParser.SUPER, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 84, RULE_typeArgument);
		int _la;
		try {
			switch ( _syncPredict(970, 112) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(957);
				type();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(958);
				annotationsOpt();
				setState(959);
				_match(QUESTION);
				_sync(968);
				_la = _input.LA(1);
				if (_la==EXTENDS || _la==SUPER) {
					{
					setState(960);
					{
						Token _st = _input.LT(1);
						_la = _st.getType();
					_matchSet(_st, _la==EXTENDS || _la==SUPER);
					}

					_matchStar(964, 961, 966, NL);

					setState(967);
					type();
					}
				}

				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 86, RULE_annotatedQualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(972);
			annotationsOpt();
			setState(973);
			qualifiedClassName();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 88, RULE_qualifiedClassNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(975);
			annotatedQualifiedClassName();

			_sync(986);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(976);
				_match(COMMA);

				_matchStar(980, 977, 982, NL);

				setState(983);
				annotatedQualifiedClassName();
				}
				}
				_sync(988);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 90, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(989);
			_match(LPAREN);
			_sync(991);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (ABSTRACT - 7)) | (1L << (FINAL - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (PRIVATE - 7)) | (1L << (PROTECTED - 7)) | (1L << (PUBLIC - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (STATIC - 7)) | (1L << (STRICTFP - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (YIELD - 7)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)) | (1L << (ELLIPSIS - 136)))) != 0)) {
				{
				setState(990);
				formalParameterList();
				}
			}

			setState(993);
			_match(RPAREN);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 92, RULE_formalParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			switch ( _syncPredict(997, 116) ) {
			case 1:
				{
				setState(995);
				formalParameter();
				}
				break;
			case 2:
				{
				setState(996);
				thisFormalParameter();
				}
				break;
			}

			_sync(1009);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(999);
				_match(COMMA);

				_matchStar(1003, 1000, 1005, NL);

				setState(1006);
				formalParameter();
				}
				}
				_sync(1011);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 94, RULE_thisFormalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1012);
			type();
			setState(1013);
			_match(THIS);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 96, RULE_formalParameter);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1015);
			variableModifiersOpt();

			if ( _syncPredict(1017, 119)==1 ) {
				{
				setState(1016);
				type();
				}
			}

			_sync(1020);
			_la = _input.LA(1);
			if (_la==ELLIPSIS) {
				{
				setState(1019);
				_match(ELLIPSIS);
				}
			}

			setState(1022);
			variableDeclaratorId();

			if ( _syncPredict(1037, 123)==1 ) {
				{
				_matchStar(1026, 1023, 1028, NL);

				setState(1029);
				_match(ASSIGN);

				_matchStarPredict(1033, 1030, 1035, 122, 1, NL);

				setState(1036);
				expression(0);
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 98, RULE_methodBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1039);
			block();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 100, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1041);
			qualifiedNameElement();

			_alt = _syncPredict(1046, 124);
			while (_alt==1) {
				{
				{
				setState(1042);
				_match(DOT);
				setState(1043);
				qualifiedNameElement();
				}
				}
				_alt = _syncPredict(1048, 124);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 102, RULE_qualifiedNameElement);
		try {
			switch ( _syncPredict(1054, 125) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1049);
				identifier();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1050);
				_match(DEF);
				}

				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1051);
				_match(IN);
				}

				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1052);
				_match(AS);
				}

				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1053);
				_match(TRAIT);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 104, RULE_qualifiedNameElements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			_alt = _syncPredict(1061, 126);
			while (_alt==1) {
				{
				{
				setState(1056);
				qualifiedNameElement();
				setState(1057);
				_match(DOT);
				}
				}
				_alt = _syncPredict(1063, 126);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 106, RULE_qualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1064);
			qualifiedNameElements();
			setState(1065);
			identifier();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 108, RULE_qualifiedStandardClassName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1067);
			qualifiedNameElements();
			setState(1068);
			className();

			_alt = _syncPredict(1073, 127);
			while (_alt==1) {
				{
				{
				setState(1069);
				_match(DOT);
				setState(1070);
				className();
				}
				}
				_alt = _syncPredict(1075, 127);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public IntegerLiteralAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public IntegerLiteralAltContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitIntegerLiteralAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FloatingPointLiteralAltContext extends LiteralContext {
		public TerminalNode FloatingPointLiteral() { return getToken(GroovyParser.FloatingPointLiteral, 0); }
		public FloatingPointLiteralAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public StringLiteralAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public BooleanLiteralAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public BooleanLiteralAltContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitBooleanLiteralAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NullLiteralAltContext extends LiteralContext {
		public TerminalNode NullLiteral() { return getToken(GroovyParser.NullLiteral, 0); }
		public NullLiteralAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public NullLiteralAltContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitNullLiteralAlt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final LiteralContext literal() throws RecognitionException {
		int _parentState = getState();
		LiteralContext _localctx = null;
		boolean _synced = false;
		try {
			for (;;) {
				switch (_input.LA(1)) {
				case IntegerLiteral:
					if (_localctx == null) {
						_localctx = new IntegerLiteralAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 110, RULE_literal, 1081);
					} else {
						_localctx = new IntegerLiteralAltContext(_localctx);
					}enterOuterAlt(_localctx, 1);
					{
					setState(1076);
					_match(IntegerLiteral);
					}

					break;
				case FloatingPointLiteral:
					if (_localctx == null) {
						_localctx = new FloatingPointLiteralAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 110, RULE_literal, 1081);
					} else {
						_localctx = new FloatingPointLiteralAltContext(_localctx);
					}enterOuterAlt(_localctx, 2);
					{
					setState(1077);
					_match(FloatingPointLiteral);
					}

					break;
				case StringLiteral:
					if (_localctx == null) {
						_localctx = new StringLiteralAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 110, RULE_literal, 1081);
					} else {
						_localctx = new StringLiteralAltContext(_localctx);
					}enterOuterAlt(_localctx, 3);
					{
					setState(1078);
					stringLiteral();
					}

					break;
				case BooleanLiteral:
					if (_localctx == null) {
						_localctx = new BooleanLiteralAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 110, RULE_literal, 1081);
					} else {
						_localctx = new BooleanLiteralAltContext(_localctx);
					}enterOuterAlt(_localctx, 4);
					{
					setState(1079);
					_match(BooleanLiteral);
					}

					break;
				case NullLiteral:
					if (_localctx == null) {
						_localctx = new NullLiteralAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 110, RULE_literal, 1081);
					} else {
						_localctx = new NullLiteralAltContext(_localctx);
					}enterOuterAlt(_localctx, 5);
					{
					setState(1080);
					_match(NullLiteral);
					}

					break;
				default:
					if (!_synced) {
						_synced = true;
						_localctx = new LiteralContext(_ctx, _parentState);
						_enterDeferred(_localctx, 110, RULE_literal, 1081);
						continue;
					}
					throw new NoViableAltException(this);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			if (_localctx == null) {
				_localctx = new LiteralContext(_ctx, _parentState);
				enterRule(_localctx, 110, RULE_literal);
			}
			_recover(_localctx, re);
		}
		finally {
			if (_localctx != null) {
				exitRule();
			}
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
		enterRule(_localctx, 112, RULE_gstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1083);
			_match(GStringBegin);
			setState(1084);
			gstringValue();

			_sync(1089);
			_la = _input.LA(1);
			while (_la==GStringPart) {
				{
				{
				setState(1085);
				_match(GStringPart);
				setState(1086);
				gstringValue();
				}
				}
				_sync(1091);
				_la = _input.LA(1);
			}

			setState(1092);
			_match(GStringEnd);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 114, RULE_gstringValue);
		int _la;
		try {
			_sync(1096);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				enterOuterAlt(_localctx, 1);
				{
				setState(1094);
				gstringPath();
				}

			}
			else if (_la==LBRACE) {
				enterOuterAlt(_localctx, 2);
				{
				setState(1095);
				closure();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 116, RULE_gstringPath);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1098);
			identifier();

			_matchStar(1102, 1099, 1104, GStringPathPart);

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final StandardLambdaExpressionContext lambdaExpression() throws RecognitionException {
		StandardLambdaExpressionContext _localctx = new StandardLambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_lambdaExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1105);
			lambdaParameters();

			_matchStar(1109, 1106, 1111, NL);

			setState(1112);
			_match(ARROW);

			_matchStarPredict(1116, 1113, 1118, 133, 1, NL);

			setState(1119);
			lambdaBody();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode ARROW() { return getToken(GroovyParser.ARROW, 0); }
		public LambdaBodyContext lambdaBody() {
			return getRuleContext(LambdaBodyContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 120, RULE_standardLambdaExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1121);
			standardLambdaParameters();

			_matchStar(1125, 1122, 1127, NL);

			setState(1128);
			_match(ARROW);

			_matchStarPredict(1132, 1129, 1134, 135, 1, NL);

			setState(1135);
			lambdaBody();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final StandardLambdaParametersContext lambdaParameters() throws RecognitionException {
		StandardLambdaParametersContext _localctx = new StandardLambdaParametersContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_lambdaParameters);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1137);
			formalParameters();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 124, RULE_standardLambdaParameters);
		int _la;
		try {
			_sync(1141);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				enterOuterAlt(_localctx, 1);
				{
				setState(1139);
				formalParameters();
				}

			}
			else if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				enterOuterAlt(_localctx, 2);
				{
				setState(1140);
				variableDeclaratorId();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 126, RULE_lambdaBody);
		try {
			switch ( _syncPredict(1145, 137) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1143);
				block();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1144);
				statementExpression();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode ARROW() { return getToken(GroovyParser.ARROW, 0); }
		public SepContext sep() {
			return getRuleContext(SepContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 128, RULE_closure);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1147);
			_match(LBRACE);

			if ( _syncPredict(1164, 141)==1 ) {
				{
				_matchStar(1151, 1148, 1153, NL);

				_sync(1161);
				_la = _input.LA(1);
				if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (ABSTRACT - 7)) | (1L << (FINAL - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (PRIVATE - 7)) | (1L << (PROTECTED - 7)) | (1L << (PUBLIC - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (STATIC - 7)) | (1L << (STRICTFP - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (YIELD - 7)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)) | (1L << (ELLIPSIS - 136)))) != 0)) {
					{
					setState(1154);
					formalParameterList();

					_matchStar(1158, 1155, 1160, NL);

					}
				}

				setState(1163);
				_match(ARROW);
				}
			}

			if ( _syncPredict(1167, 142)==1 ) {
				{
				setState(1166);
				sep();
				}
			}

			setState(1169);
			blockStatementsOpt();
			setState(1170);
			_match(RBRACE);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 130, RULE_closureOrLambdaExpression);
		int _la;
		try {
			_sync(1174);
			_la = _input.LA(1);
			if (_la==LBRACE) {
				enterOuterAlt(_localctx, 1);
				{
				setState(1172);
				closure();
				}

			}
			else if (_la==LPAREN) {
				enterOuterAlt(_localctx, 2);
				{
				setState(1173);
				lambdaExpression();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 132, RULE_blockStatementsOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			if ( _syncPredict(1177, 144)==1 ) {
				{
				setState(1176);
				blockStatements();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 134, RULE_blockStatements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1179);
			blockStatement();

			_alt = _syncPredict(1185, 145);
			while (_alt==1) {
				{
				{
				setState(1180);
				sep();
				setState(1181);
				blockStatement();
				}
				}
				_alt = _syncPredict(1187, 145);
			}

			if ( _syncPredict(1189, 146)==1 ) {
				{
				setState(1188);
				sep();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 136, RULE_annotationsOpt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			_sync(1210);
			_la = _input.LA(1);
			if (_la==AT) {
				{
				setState(1191);
				annotation();

				_alt = _syncPredict(1201, 148);
				while (_alt==1) {
					{
					{
					_matchStar(1195, 1192, 1197, NL);

					setState(1198);
					annotation();
					}
					}
					_alt = _syncPredict(1203, 148);
				}

				_matchStar(1207, 1204, 1209, NL);

				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 138, RULE_annotation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1212);
			_match(AT);
			setState(1213);
			annotationName();

			if ( _syncPredict(1225, 153)==1 ) {
				{
				_matchStar(1217, 1214, 1219, NL);

				setState(1220);
				_match(LPAREN);

				if ( _syncPredict(1222, 152)==1 ) {
					{
					setState(1221);
					elementValues();
					}
				}

				setState(1224);
				_match(RPAREN);
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 140, RULE_elementValues);
		try {
			switch ( _syncPredict(1230, 154) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1227);
				elementValuePairs();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1228);
				if (!( !SemanticPredicates.isIdentifierAssign(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isIdentifierAssign(_input) ");
				setState(1229);
				elementValue();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 142, RULE_annotationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1232);
			qualifiedClassName();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 144, RULE_elementValuePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1234);
			elementValuePair();

			_sync(1239);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1235);
				_match(COMMA);
				setState(1236);
				elementValuePair();
				}
				}
				_sync(1241);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 146, RULE_elementValuePair);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1242);
			elementValuePairName();

			_matchStar(1246, 1243, 1248, NL);

			setState(1249);
			_match(ASSIGN);

			_matchStarPredict(1253, 1250, 1255, 157, 1, NL);

			setState(1256);
			elementValue();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 148, RULE_elementValuePairName);
		try {
			switch ( _syncPredict(1260, 158) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1258);
				identifier();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1259);
				keywords();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 150, RULE_elementValue);
		try {
			switch ( _syncPredict(1265, 159) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1262);
				elementValueArrayInitializer();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1263);
				annotation();
				}

				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1264);
				expression(0);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 152, RULE_elementValueArrayInitializer);
		int _la;
		try {
			int _alt;
			_sync(1295);
			_la = _input.LA(1);
			if (_la==LBRACK) {
				enterOuterAlt(_localctx, 1);
				{
				setState(1267);
				_match(LBRACK);

				if ( _syncPredict(1279, 162)==1 ) {
					{
					setState(1268);
					elementValue();

					_alt = _syncPredict(1273, 160);
					while (_alt==1) {
						{
						{
						setState(1269);
						_match(COMMA);
						setState(1270);
						elementValue();
						}
						}
						_alt = _syncPredict(1275, 160);
					}

					_sync(1277);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(1276);
						_match(COMMA);
						}
					}

					}
				}

				setState(1281);
				_match(RBRACK);
				}

			}
			else if (_la==LBRACE) {
				enterOuterAlt(_localctx, 2);
				{
				setState(1282);
				_match(LBRACE);
				_sync(1286); 
				do {
					{
					{
					setState(1283);
					elementValue();
					setState(1284);
					_match(COMMA);
					}
					}
					_alt = _syncPredict(1288, 163); 
				} while (_alt==1);

				if ( _syncPredict(1291, 164)==1 ) {
					{
					setState(1290);
					elementValue();
					}
				}

				setState(1293);
				_match(RBRACE);
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 154, RULE_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1297);
			_match(LBRACE);

			if ( _syncPredict(1299, 166)==1 ) {
				{
				setState(1298);
				sep();
				}
			}

			setState(1301);
			blockStatementsOpt();
			setState(1302);
			_match(RBRACE);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlockStatementContext extends GroovyParserRuleContext {
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
		enterRule(_localctx, 156, RULE_blockStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1304);
			statement();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 158, RULE_localVariableDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1306);
			if (!( !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ");
			setState(1307);
			variableDeclaration(0);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDeclarationContext extends GroovyParserRuleContext {
		public int t;
		public TypeContext type;
		public ModifiersContext modifiers() {
			return getRuleContext(ModifiersContext.class,0);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 160, RULE_variableDeclaration);
		int _la;
		try {
			int _alt;
			switch ( _syncPredict(1341, 172) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1309);
				modifiers();

				_matchStar(1313, 1310, 1315, NL);

				_sync(1336);
				_la = _input.LA(1);
				if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (YIELD - 7)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)))) != 0)) {
					{
					if ( _syncPredict(1317, 168)==1 ) {
						{
						setState(1316);
						_localctx.type = type();
						}
					}

					setState(1319);
					variableDeclarators((_localctx.type)!=null && !(_localctx.type).dim0().isEmpty());
					}
				}
				else if (_la==LPAREN) {
					{
					setState(1320);
					typeNamePairs();

					_matchStar(1324, 1321, 1326, NL);

					setState(1327);
					_match(ASSIGN);

					_matchStarPredict(1331, 1328, 1333, 170, 1, NL);

					setState(1334);
					variableInitializer();
					}
				}
				else {
					throw new NoViableAltException(this);
				}

				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1338);
				_localctx.type = type();
				setState(1339);
				variableDeclarators(!(_localctx.type).dim0().isEmpty());
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeNamePairsContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public List<? extends TypeNamePairContext> typeNamePair() {
			return getRuleContexts(TypeNamePairContext.class);
		}
		public TypeNamePairContext typeNamePair(int i) {
			return getRuleContext(TypeNamePairContext.class,i);
		}
		public List<? extends KeyedPairContext> keyedPair() {
			return getRuleContexts(KeyedPairContext.class);
		}
		public KeyedPairContext keyedPair(int i) {
			return getRuleContext(KeyedPairContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
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
		enterRule(_localctx, 162, RULE_typeNamePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1343);
			_match(LPAREN);

			switch ( _syncPredict(1360, 175) ) {
			case 1:
				{
				setState(1344);
				typeNamePair();

				_sync(1349);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1345);
					_match(COMMA);
					setState(1346);
					typeNamePair();
					}
					}
					_sync(1351);
					_la = _input.LA(1);
				}

				}
				break;
			case 2:
				{
				setState(1352);
				keyedPair();

				_sync(1357);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1353);
					_match(COMMA);
					setState(1354);
					keyedPair();
					}
					}
					_sync(1359);
					_la = _input.LA(1);
				}

				}
				break;
			}
			setState(1362);
			_match(RPAREN);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 164, RULE_typeNamePair);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			switch ( _syncPredict(1368, 176) ) {
			case 1:
				{
				setState(1364);
				_match(DEF);
				}
				break;
			case 2:
				{
				setState(1365);
				_match(VAL);
				}
				break;
			case 3:
				{
				setState(1366);
				_match(VAR);
				}
				break;
			case 4:
				{
				setState(1367);
				type();
				}
				break;
			}

			_sync(1371);
			_la = _input.LA(1);
			if (_la==MUL) {
				{
				setState(1370);
				_match(MUL);
				}
			}

			setState(1373);
			variableDeclaratorId();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 166, RULE_keyedPair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1375);
			_localctx.key = identifier();
			setState(1376);
			_match(COLON);

			switch ( _syncPredict(1381, 178) ) {
			case 1:
				{
				setState(1377);
				_match(DEF);
				}
				break;
			case 2:
				{
				setState(1378);
				_match(VAL);
				}
				break;
			case 3:
				{
				setState(1379);
				_match(VAR);
				}
				break;
			case 4:
				{
				setState(1380);
				type();
				}
				break;
			}

			setState(1383);
			variableDeclaratorId();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 168, RULE_variableNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1385);
			_match(LPAREN);
			setState(1386);
			variableDeclaratorId();
			_sync(1389); 
			do {
				{
				{
				setState(1387);
				_match(COMMA);
				setState(1388);
				variableDeclaratorId();
				}
				}
				_sync(1391); 
				_la = _input.LA(1);
			} while ( _la==COMMA );
			setState(1393);
			_match(RPAREN);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 170, RULE_conditionalStatement);
		int _la;
		try {
			_sync(1397);
			_la = _input.LA(1);
			if (_la==IF) {
				enterOuterAlt(_localctx, 1);
				{
				setState(1395);
				ifElseStatement();
				}

			}
			else if (_la==SWITCH) {
				enterOuterAlt(_localctx, 2);
				{
				setState(1396);
				switchStatement();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 172, RULE_ifElseStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1399);
			_match(IF);
			setState(1400);
			expressionInPar();

			_matchStarPredict(1404, 1401, 1406, 181, 1, NL);

			setState(1407);
			_localctx.tb = statement();

			if ( _syncPredict(1425, 185)==1 ) {
				{
				switch ( _syncPredict(1415, 183) ) {
				case 1:
					{
					_matchStar(1411, 1408, 1413, NL);

					}
					break;
				case 2:
					{
					setState(1414);
					sep();
					}
					break;
				}
				setState(1417);
				_match(ELSE);

				_matchStarPredict(1421, 1418, 1423, 184, 1, NL);

				setState(1424);
				_localctx.fb = statement();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 174, RULE_switchStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1427);
			_match(SWITCH);
			setState(1428);
			expressionInPar();

			_matchStar(1432, 1429, 1434, NL);

			setState(1435);
			_match(LBRACE);

			_matchStar(1439, 1436, 1441, NL);

			_sync(1453);
			_la = _input.LA(1);
			if (_la==CASE || _la==DEFAULT) {
				{
				_sync(1443); 
				do {
					{
					{
					setState(1442);
					switchBlockStatementGroup();
					}
					}
					_sync(1445); 
					_la = _input.LA(1);
				} while ( _la==CASE || _la==DEFAULT );

				_matchStar(1450, 1447, 1452, NL);

				}
			}

			setState(1455);
			_match(RBRACE);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode AWAIT() { return getToken(GroovyParser.AWAIT, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public ForStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public WhileStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode WHILE() { return getToken(GroovyParser.WHILE, 0); }
		public ExpressionInParContext expressionInPar() {
			return getRuleContext(ExpressionInParContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public DoWhileStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		enterRule(_localctx, 176, RULE_loopStatement);
		int _la;
		try {
			int _alt;
			switch ( _syncPredict(1502, 196) ) {
			case 1:
				_localctx = new ForStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1457);
				annotationsOpt();
				setState(1458);
				_match(FOR);
				_sync(1460);
				_la = _input.LA(1);
				if (_la==AWAIT) {
					{
					setState(1459);
					_match(AWAIT);
					}
				}

				setState(1462);
				_match(LPAREN);
				setState(1463);
				forControl();
				setState(1464);
				_match(RPAREN);

				_matchStarPredict(1468, 1465, 1470, 192, 1, NL);

				setState(1471);
				statement();
				}

				break;
			case 2:
				_localctx = new WhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1473);
				annotationsOpt();
				setState(1474);
				_match(WHILE);
				setState(1475);
				expressionInPar();

				_matchStarPredict(1479, 1476, 1481, 193, 1, NL);

				setState(1482);
				statement();
				}

				break;
			case 3:
				_localctx = new DoWhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1484);
				annotationsOpt();
				setState(1485);
				_match(DO);

				_matchStarPredict(1489, 1486, 1491, 194, 1, NL);

				setState(1492);
				statement();

				_matchStar(1496, 1493, 1498, NL);

				setState(1499);
				_match(WHILE);
				setState(1500);
				expressionInPar();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 178, RULE_continueStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1504);
			_match(CONTINUE);
			_sync(1506);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(1505);
				identifier();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 180, RULE_breakStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1508);
			_match(BREAK);
			_sync(1510);
			_la = _input.LA(1);
			if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(1509);
				identifier();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 182, RULE_yieldStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1512);
			_match(YIELD);
			setState(1513);
			expression(0);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TryCatchStatementContext extends GroovyParserRuleContext {
		public TerminalNode TRY() { return getToken(GroovyParser.TRY, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ResourcesContext resources() {
			return getRuleContext(ResourcesContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 184, RULE_tryCatchStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1515);
			_match(TRY);
			_sync(1517);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(1516);
				resources();
				}
			}

			_matchStar(1522, 1519, 1524, NL);

			setState(1525);
			block();

			_alt = _syncPredict(1535, 202);
			while (_alt==1) {
				{
				{
				_matchStar(1529, 1526, 1531, NL);

				setState(1532);
				catchClause();
				}
				}
				_alt = _syncPredict(1537, 202);
			}

			if ( _syncPredict(1545, 204)==1 ) {
				{
				_matchStar(1541, 1538, 1543, NL);

				setState(1544);
				finallyBlock();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode COMMA() { return getToken(GroovyParser.COMMA, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 186, RULE_assertStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1547);
			_match(ASSERT);
			setState(1548);
			_localctx.ce = expression(0);

			if ( _syncPredict(1563, 207)==1 ) {
				{
				_matchStar(1552, 1549, 1554, NL);

				setState(1555);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
				_matchSet(_st, _la==COMMA || _la==COLON);
				}

				_matchStarPredict(1559, 1556, 1561, 206, 1, NL);

				setState(1562);
				_localctx.me = expression(0);
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public BlockStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ConditionalStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public LoopStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public TryCatchStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public SynchronizedStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ReturnStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ThrowStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public BreakStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ContinueStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public YieldStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public YieldReturnStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public StatementExpressionContext statementExpression() {
			return getRuleContext(StatementExpressionContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public DeferStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public LabeledStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public AssertStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public LocalVariableDeclarationStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ExpressionStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public EmptyStmtAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		enterRule(_localctx, 188, RULE_statement);
		try {
			int _alt;
			switch ( _syncPredict(1621, 213) ) {
			case 1:
				_localctx = new BlockStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1565);
				block();
				}

				break;
			case 2:
				_localctx = new ConditionalStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1566);
				conditionalStatement();
				}

				break;
			case 3:
				_localctx = new LoopStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1567);
				loopStatement();
				}

				break;
			case 4:
				_localctx = new TryCatchStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1568);
				tryCatchStatement();
				}

				break;
			case 5:
				_localctx = new SynchronizedStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1569);
				_match(SYNCHRONIZED);
				setState(1570);
				expressionInPar();

				_matchStar(1574, 1571, 1576, NL);

				setState(1577);
				block();
				}

				break;
			case 6:
				_localctx = new ReturnStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1579);
				_match(RETURN);

				if ( _syncPredict(1581, 209)==1 ) {
					{
					setState(1580);
					expression(0);
					}
				}

				}

				break;
			case 7:
				_localctx = new ThrowStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1583);
				_match(THROW);
				setState(1584);
				expression(0);
				}

				break;
			case 8:
				_localctx = new BreakStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1585);
				breakStatement();
				}

				break;
			case 9:
				_localctx = new ContinueStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1586);
				continueStatement();
				}

				break;
			case 10:
				_localctx = new YieldStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1587);
				if (!( inSwitchExpressionLevel > 0 )) throw createFailedPredicateException(" inSwitchExpressionLevel > 0 ");
				setState(1588);
				yieldStatement();
				}

				break;
			case 11:
				_localctx = new YieldReturnStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1589);
				_match(YIELD);
				setState(1590);
				_match(RETURN);

				_matchStarPredict(1594, 1591, 1596, 210, 1, NL);

				setState(1597);
				expression(0);
				}

				break;
			case 12:
				_localctx = new DeferStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(1598);
				if (!( inAsyncClosureLevel > 0 )) throw createFailedPredicateException(" inAsyncClosureLevel > 0 ");
				setState(1599);
				_match(DEFER);

				_matchStarPredict(1603, 1600, 1605, 211, 1, NL);

				setState(1606);
				statementExpression();
				}

				break;
			case 13:
				_localctx = new LabeledStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(1607);
				identifier();
				setState(1608);
				_match(COLON);

				_matchStarPredict(1612, 1609, 1614, 212, 1, NL);

				setState(1615);
				statement();
				}

				break;
			case 14:
				_localctx = new AssertStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(1617);
				assertStatement();
				}

				break;
			case 15:
				_localctx = new LocalVariableDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(1618);
				localVariableDeclaration();
				}

				break;
			case 16:
				_localctx = new ExpressionStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(1619);
				statementExpression();
				}

				break;
			case 17:
				_localctx = new EmptyStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(1620);
				_match(SEMI);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public CatchTypeContext catchType() {
			return getRuleContext(CatchTypeContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 190, RULE_catchClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1623);
			_match(CATCH);
			setState(1624);
			_match(LPAREN);
			setState(1625);
			variableModifiersOpt();

			if ( _syncPredict(1627, 214)==1 ) {
				{
				setState(1626);
				catchType();
				}
			}

			setState(1629);
			variableDeclaratorId();
			setState(1630);
			_match(RPAREN);

			_matchStar(1634, 1631, 1636, NL);

			setState(1637);
			block();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 192, RULE_catchType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1639);
			qualifiedClassName();

			_sync(1644);
			_la = _input.LA(1);
			while (_la==BITOR) {
				{
				{
				setState(1640);
				_match(BITOR);
				setState(1641);
				qualifiedClassName();
				}
				}
				_sync(1646);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FinallyBlockContext extends GroovyParserRuleContext {
		public TerminalNode FINALLY() { return getToken(GroovyParser.FINALLY, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 194, RULE_finallyBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1647);
			_match(FINALLY);

			_matchStar(1651, 1648, 1653, NL);

			setState(1654);
			block();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResourcesContext extends GroovyParserRuleContext {
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public ResourceListContext resourceList() {
			return getRuleContext(ResourceListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 196, RULE_resources);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1656);
			_match(LPAREN);

			_matchStarPredict(1660, 1657, 1662, 218, 1, NL);

			setState(1663);
			resourceList();
			_sync(1665);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(1664);
				sep();
				}
			}

			setState(1667);
			_match(RPAREN);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 198, RULE_resourceList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1669);
			resource();

			_alt = _syncPredict(1675, 220);
			while (_alt==1) {
				{
				{
				setState(1670);
				sep();
				setState(1671);
				resource();
				}
				}
				_alt = _syncPredict(1677, 220);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 200, RULE_resource);
		try {
			switch ( _syncPredict(1680, 221) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1678);
				localVariableDeclaration();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1679);
				expression(0);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public BlockStatementsContext blockStatements() {
			return getRuleContext(BlockStatementsContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 202, RULE_switchBlockStatementGroup);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1682);
			switchLabel();

			_alt = _syncPredict(1692, 223);
			while (_alt==1) {
				{
				{
				_matchStar(1686, 1683, 1688, NL);

				setState(1689);
				switchLabel();
				}
				}
				_alt = _syncPredict(1694, 223);
			}

			_matchStarPredict(1698, 1695, 1700, 224, 1, NL);

			switch ( _syncPredict(1703, 225) ) {
			case 1:
				{
				setState(1701);
				blockStatements();
				}
				break;
			case 2:
				{
				setState(1702);
				if (!( _input.LT(1).getType() == RBRACE )) throw createFailedPredicateException(" _input.LT(1).getType() == RBRACE ");
				}
				break;
			}
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 204, RULE_switchLabel);
		int _la;
		try {
			_sync(1711);
			_la = _input.LA(1);
			if (_la==CASE) {
				enterOuterAlt(_localctx, 1);
				{
				setState(1705);
				_match(CASE);
				setState(1706);
				expression(0);
				setState(1707);
				_match(COLON);
				}

			}
			else if (_la==DEFAULT) {
				enterOuterAlt(_localctx, 2);
				{
				setState(1709);
				_match(DEFAULT);
				setState(1710);
				_match(COLON);
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 206, RULE_forControl);
		try {
			switch ( _syncPredict(1715, 227) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1713);
				enhancedForControl();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1714);
				originalForControl();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
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
		enterRule(_localctx, 208, RULE_enhancedForControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			if ( _syncPredict(1720, 228)==1 ) {
				{
				setState(1717);
				indexVariable();
				setState(1718);
				_match(COMMA);
				}
			}

			setState(1722);
			variableModifiersOpt();

			if ( _syncPredict(1724, 229)==1 ) {
				{
				setState(1723);
				type();
				}
			}

			setState(1726);
			variableDeclaratorId();
			setState(1727);
			{
				Token _st = _input.LT(1);
				_la = _st.getType();
			_matchSet(_st, _la==IN || _la==COLON);
			}
			setState(1728);
			expression(0);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 210, RULE_indexVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			if ( _syncPredict(1731, 230)==1 ) {
				{
				setState(1730);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
				_matchSet(_st, (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << BuiltInPrimitiveType) | (1L << VAL) | (1L << VAR))) != 0));
				}
				}
			}

			setState(1733);
			identifier();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 212, RULE_originalForControl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			if ( _syncPredict(1736, 231)==1 ) {
				{
				setState(1735);
				forInit();
				}
			}

			setState(1738);
			_match(SEMI);

			if ( _syncPredict(1740, 232)==1 ) {
				{
				setState(1739);
				expression(0);
				}
			}

			setState(1742);
			_match(SEMI);

			if ( _syncPredict(1744, 233)==1 ) {
				{
				setState(1743);
				forUpdate();
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 214, RULE_forInit);
		try {
			switch ( _syncPredict(1748, 234) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1746);
				localVariableDeclaration();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1747);
				expressionList(false);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 216, RULE_forUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1750);
			expressionList(false);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 218, RULE_castParExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1752);
			_match(LPAREN);
			setState(1753);
			intersectionType();
			setState(1754);
			_match(RPAREN);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 220, RULE_intersectionType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1756);
			type();

			_sync(1767);
			_la = _input.LA(1);
			while (_la==BITAND) {
				{
				{
				setState(1757);
				_match(BITAND);

				_matchStar(1761, 1758, 1763, NL);

				setState(1764);
				type();
				}
				}
				_sync(1769);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 222, RULE_coercionType);
		int _la;
		try {
			_sync(1772);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				enterOuterAlt(_localctx, 1);
				{
				setState(1770);
				castParExpression();
				}

			}
			else if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (YIELD - 7)))) != 0) || ((((_la - 136)) & ~0x3f) == 0 && ((1L << (_la - 136)) & ((1L << (CapitalizedIdentifier - 136)) | (1L << (Identifier - 136)) | (1L << (AT - 136)))) != 0)) {
				enterOuterAlt(_localctx, 2);
				{
				setState(1771);
				type();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 224, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1774);
			expressionInPar();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 226, RULE_expressionInPar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1776);
			_match(LPAREN);
			setState(1777);
			enhancedStatementExpression();
			setState(1778);
			_match(RPAREN);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 228, RULE_expressionList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1780);
			expressionListElement(_localctx.canSpread);

			_alt = _syncPredict(1791, 239);
			while (_alt==1) {
				{
				{
				setState(1781);
				_match(COMMA);

				_matchStarPredict(1785, 1782, 1787, 238, 1, NL);

				setState(1788);
				expressionListElement(_localctx.canSpread);
				}
				}
				_alt = _syncPredict(1793, 239);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 230, RULE_expressionListElement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			if ( _syncPredict(1795, 240)==1 ) {
				{
				setState(1794);
				_match(MUL);
				}
			}

			setState(1797);
			expression(0);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 232, RULE_enhancedExpression);
		try {
			switch ( _syncPredict(1801, 241) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1799);
				expression(0);
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1800);
				standardLambdaExpression();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 234, RULE_enhancedStatementExpression);
		try {
			switch ( _syncPredict(1805, 242) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1803);
				statementExpression();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1804);
				standardLambdaExpression();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public CommandExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		StatementExpressionContext _localctx = new CommandExprAltContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_statementExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1807);
			commandExpression();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 238, RULE_postfixExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1809);
			pathExpression();

			if ( _syncPredict(1811, 243)==1 ) {
				{
				setState(1810);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
					_localctx.op = _matchSet(_st, _la==INC || _la==DEC);
				}
				}
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 240, RULE_switchExpression);

		    inSwitchExpressionLevel++;

		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1813);
			_match(SWITCH);
			setState(1814);
			expressionInPar();

			_matchStar(1818, 1815, 1820, NL);

			setState(1821);
			_match(LBRACE);

			_matchStarPredict(1825, 1822, 1827, 245, 1, NL);

			_sync(1831);
			_la = _input.LA(1);
			while (_la==CASE || _la==DEFAULT) {
				{
				{
				setState(1828);
				switchBlockStatementExpressionGroup();
				}
				}
				_sync(1833);
				_la = _input.LA(1);
			}

			_matchStar(1837, 1834, 1839, NL);

			setState(1840);
			_match(RBRACE);
			}

			_ctx.stop = _input.LT(-1);

			    inSwitchExpressionLevel--;

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 242, RULE_switchBlockStatementExpressionGroup);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			_sync(1849); 
			do {
				{
				{
				setState(1842);
				switchExpressionLabel();

				_matchStarPredict(1846, 1843, 1848, 248, 1, NL);

				}
				}
				_alt = _syncPredict(1851, 249); 
			} while (_alt==1);
			setState(1853);
			blockStatements();
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 244, RULE_switchExpressionLabel);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			_sync(1858);
			_la = _input.LA(1);
			if (_la==CASE) {
				{
				setState(1855);
				_match(CASE);
				setState(1856);
				expressionList(true);
				}
			}
			else if (_la==DEFAULT) {
				{
				setState(1857);
				_match(DEFAULT);
				}
			}
			else {
				throw new NoViableAltException(this);
			}

			setState(1860);
			{
				Token _st = _input.LT(1);
				_la = _st.getType();
				_localctx.ac = _matchSet(_st, _la==ARROW || _la==COLON);
			}
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public CastExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ClosureOrLambdaExpressionContext closureOrLambdaExpression() {
			return getRuleContext(ClosureOrLambdaExpressionContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public AsyncClosureExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<? extends TerminalNode> COMMA() { return getTokens(GroovyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(GroovyParser.COMMA, i);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public AwaitExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public PostfixExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public SwitchExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public SwitchExprAltContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitSwitchExprAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnaryNotExprAltContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode BITNOT() { return getToken(GroovyParser.BITNOT, 0); }
		public TerminalNode NOT() { return getToken(GroovyParser.NOT, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public UnaryNotExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode POWER() { return getToken(GroovyParser.POWER, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public PowerExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public UnaryAddExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode MUL() { return getToken(GroovyParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(GroovyParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(GroovyParser.MOD, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public MultiplicativeExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ADD() { return getToken(GroovyParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(GroovyParser.SUB, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public AdditiveExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		public ShiftExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public NotInstanceofTypeContext notInstanceofType() {
			return getRuleContext(NotInstanceofTypeContext.class,0);
		}
		public TerminalNode NOT_INSTANCEOF() { return getToken(GroovyParser.NOT_INSTANCEOF, 0); }
		public CoercionTypeContext coercionType() {
			return getRuleContext(CoercionTypeContext.class,0);
		}
		public TerminalNode AS() { return getToken(GroovyParser.AS, 0); }
		public TerminalNode LE() { return getToken(GroovyParser.LE, 0); }
		public TerminalNode GE() { return getToken(GroovyParser.GE, 0); }
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
		public TerminalNode NOT_IN() { return getToken(GroovyParser.NOT_IN, 0); }
		public RelationalExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public EqualityExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode REGEX_FIND() { return getToken(GroovyParser.REGEX_FIND, 0); }
		public TerminalNode REGEX_MATCH() { return getToken(GroovyParser.REGEX_MATCH, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public RegexExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BITAND() { return getToken(GroovyParser.BITAND, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public AndExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode XOR() { return getToken(GroovyParser.XOR, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public ExclusiveOrExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BITOR() { return getToken(GroovyParser.BITOR, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public InclusiveOrExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode AND() { return getToken(GroovyParser.AND, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public LogicalAndExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode OR() { return getToken(GroovyParser.OR, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public LogicalOrExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IMPLIES() { return getToken(GroovyParser.IMPLIES, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public ImplicationExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode QUESTION() { return getToken(GroovyParser.QUESTION, 0); }
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode ELVIS() { return getToken(GroovyParser.ELVIS, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public ConditionalExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public VariableNamesContext variableNames() {
			return getRuleContext(VariableNamesContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(GroovyParser.ASSIGN, 0); }
		public StatementExpressionContext statementExpression() {
			return getRuleContext(StatementExpressionContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public MultipleAssignmentExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public AssignmentExprAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		int _startState = 246;
		enterRecursionRule(_localctx, 246, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			switch ( _syncPredict(1921, 257) ) {
			case 1:
				{
				_localctx = new CastExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1863);
				castParExpression();
				setState(1864);
				castOperandExpression();
				}
				break;
			case 2:
				{
				_localctx = new AsyncClosureExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1866);
				_match(ASYNC);

				_matchStar(1870, 1867, 1872, NL);

				 inAsyncClosureLevel++; 
				setState(1874);
				closureOrLambdaExpression();
				 inAsyncClosureLevel--; 
				}
				break;
			case 3:
				{
				_localctx = new AwaitExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1877);
				if (!( _input.LA(2) != LPAREN )) throw createFailedPredicateException(" _input.LA(2) != LPAREN ");
				setState(1878);
				_match(AWAIT);
				setState(1879);
				expression(0);

				_alt = _syncPredict(1890, 253);
				while (_alt==1) {
					{
					{
					setState(1880);
					_match(COMMA);

					_matchStarPredict(1884, 1881, 1886, 252, 1, NL);

					setState(1887);
					expression(0);
					}
					}
					_alt = _syncPredict(1892, 253);
				}

				}
				break;
			case 4:
				{
				_localctx = new PostfixExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1893);
				postfixExpression();
				}
				break;
			case 5:
				{
				_localctx = new SwitchExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1894);
				switchExpression();
				}
				break;
			case 6:
				{
				_localctx = new UnaryNotExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1895);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
				_matchSet(_st, _la==NOT || _la==BITNOT);
				}

				_matchStarPredict(1899, 1896, 1901, 254, 1, NL);

				setState(1902);
				expression(21);
				}
				break;
			case 7:
				{
				_localctx = new UnaryAddExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1903);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
					((UnaryAddExprAltContext)_localctx).op = _matchSet(_st, ((((_la - 114)) & ~0x3f) == 0 && ((1L << (_la - 114)) & ((1L << (INC - 114)) | (1L << (DEC - 114)) | (1L << (ADD - 114)) | (1L << (SUB - 114)))) != 0));
				}
				setState(1904);
				expression(19);
				}
				break;
			case 8:
				{
				_localctx = new MultipleAssignmentExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1905);
				((MultipleAssignmentExprAltContext)_localctx).left = variableNames();

				_matchStar(1909, 1906, 1911, NL);

				setState(1912);
				((MultipleAssignmentExprAltContext)_localctx).op = _match(ASSIGN);

				_matchStarPredict(1916, 1913, 1918, 256, 1, NL);

				setState(1919);
				((MultipleAssignmentExprAltContext)_localctx).right = statementExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);

			_alt = _syncPredict(2217, 299);
			while (_alt==1) {
				if ( _parseListeners!=null ) triggerExitRuleEvent();
				_prevctx = _localctx;
				{
				switch ( _syncPredict(2215, 298) ) {
				case 1:
					{
					PowerExprAltContext _altctx = new PowerExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(1923);
					if (!(_prec(20))) throw createFailedPredicateException("_prec(20)");
					setState(1924);
					((PowerExprAltContext)_localctx).op = _match(POWER);

					_matchStarPredict(1928, 1925, 1930, 258, 1, NL);

					setState(1931);
					((PowerExprAltContext)_localctx).right = expression(21);
					}
					break;
				case 2:
					{
					MultiplicativeExprAltContext _altctx = new MultiplicativeExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(1932);
					if (!(_prec(18))) throw createFailedPredicateException("_prec(18)");

					_matchStar(1936, 1933, 1938, NL);

					setState(1939);
					{
						Token _st = _input.LT(1);
						_la = _st.getType();
						((MultiplicativeExprAltContext)_localctx).op = _matchSet(_st, ((((_la - 118)) & ~0x3f) == 0 && ((1L << (_la - 118)) & ((1L << (MUL - 118)) | (1L << (DIV - 118)) | (1L << (MOD - 118)))) != 0));
					}

					_matchStarPredict(1943, 1940, 1945, 260, 1, NL);

					setState(1946);
					((MultiplicativeExprAltContext)_localctx).right = expression(19);
					}
					break;
				case 3:
					{
					AdditiveExprAltContext _altctx = new AdditiveExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(1947);
					if (!(_prec(17))) throw createFailedPredicateException("_prec(17)");
					setState(1948);
					{
						Token _st = _input.LT(1);
						_la = _st.getType();
						((AdditiveExprAltContext)_localctx).op = _matchSet(_st, _la==ADD || _la==SUB);
					}

					_matchStarPredict(1952, 1949, 1954, 261, 1, NL);

					setState(1955);
					((AdditiveExprAltContext)_localctx).right = expression(18);
					}
					break;
				case 4:
					{
					ShiftExprAltContext _altctx = new ShiftExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(1956);
					if (!(_prec(16))) throw createFailedPredicateException("_prec(16)");

					_matchStar(1960, 1957, 1962, NL);

					_sync(1973);
					switch (_input.LA(1)) {
					case GT:
					case LT:
						{
						switch ( _syncPredict(1970, 263) ) {
						case 1:
							{
							setState(1963);
							((ShiftExprAltContext)_localctx).dlOp = _match(LT);
							setState(1964);
							_match(LT);
							}
							break;
						case 2:
							{
							setState(1965);
							((ShiftExprAltContext)_localctx).tgOp = _match(GT);
							setState(1966);
							_match(GT);
							setState(1967);
							_match(GT);
							}
							break;
						case 3:
							{
							setState(1968);
							((ShiftExprAltContext)_localctx).dgOp = _match(GT);
							setState(1969);
							_match(GT);
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
						setState(1972);
						{
							Token _st = _input.LT(1);
							_la = _st.getType();
							((ShiftExprAltContext)_localctx).rangeOp = _matchSet(_st, ((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & ((1L << (RANGE_INCLUSIVE - 70)) | (1L << (RANGE_EXCLUSIVE_LEFT - 70)) | (1L << (RANGE_EXCLUSIVE_RIGHT - 70)) | (1L << (RANGE_EXCLUSIVE_FULL - 70)))) != 0));
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}

					_matchStarPredict(1978, 1975, 1980, 265, 1, NL);

					setState(1981);
					((ShiftExprAltContext)_localctx).right = expression(17);
					}
					break;
				case 5:
					{
					RelationalExprAltContext _altctx = new RelationalExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(1982);
					if (!(_prec(12))) throw createFailedPredicateException("_prec(12)");

					_matchStar(1986, 1983, 1988, NL);

					setState(1989);
					{
						Token _st = _input.LT(1);
						_la = _st.getType();
						((RelationalExprAltContext)_localctx).op = _matchSet(_st, _la==IN || ((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & ((1L << (NOT_IN - 91)) | (1L << (GT - 91)) | (1L << (LT - 91)) | (1L << (LE - 91)) | (1L << (GE - 91)))) != 0));
					}

					_matchStarPredict(1993, 1990, 1995, 267, 1, NL);

					setState(1996);
					((RelationalExprAltContext)_localctx).right = expression(13);
					}
					break;
				case 6:
					{
					EqualityExprAltContext _altctx = new EqualityExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(1997);
					if (!(_prec(11))) throw createFailedPredicateException("_prec(11)");

					_matchStar(2001, 1998, 2003, NL);

					setState(2004);
					{
						Token _st = _input.LT(1);
						_la = _st.getType();
						((EqualityExprAltContext)_localctx).op = _matchSet(_st, ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & ((1L << (SPACESHIP - 85)) | (1L << (IDENTICAL - 85)) | (1L << (NOT_IDENTICAL - 85)) | (1L << (EQUAL - 85)) | (1L << (NOTEQUAL - 85)))) != 0));
					}

					_matchStarPredict(2008, 2005, 2010, 269, 1, NL);

					setState(2011);
					((EqualityExprAltContext)_localctx).right = expression(12);
					}
					break;
				case 7:
					{
					RegexExprAltContext _altctx = new RegexExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2012);
					if (!(_prec(10))) throw createFailedPredicateException("_prec(10)");

					_matchStar(2016, 2013, 2018, NL);

					setState(2019);
					{
						Token _st = _input.LT(1);
						_la = _st.getType();
						((RegexExprAltContext)_localctx).op = _matchSet(_st, _la==REGEX_FIND || _la==REGEX_MATCH);
					}

					_matchStarPredict(2023, 2020, 2025, 271, 1, NL);

					setState(2026);
					((RegexExprAltContext)_localctx).right = expression(11);
					}
					break;
				case 8:
					{
					AndExprAltContext _altctx = new AndExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2027);
					if (!(_prec(9))) throw createFailedPredicateException("_prec(9)");

					_matchStar(2031, 2028, 2033, NL);

					setState(2034);
					((AndExprAltContext)_localctx).op = _match(BITAND);

					_matchStarPredict(2038, 2035, 2040, 273, 1, NL);

					setState(2041);
					((AndExprAltContext)_localctx).right = expression(10);
					}
					break;
				case 9:
					{
					ExclusiveOrExprAltContext _altctx = new ExclusiveOrExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2042);
					if (!(_prec(8))) throw createFailedPredicateException("_prec(8)");

					_matchStar(2046, 2043, 2048, NL);

					setState(2049);
					((ExclusiveOrExprAltContext)_localctx).op = _match(XOR);

					_matchStarPredict(2053, 2050, 2055, 275, 1, NL);

					setState(2056);
					((ExclusiveOrExprAltContext)_localctx).right = expression(9);
					}
					break;
				case 10:
					{
					InclusiveOrExprAltContext _altctx = new InclusiveOrExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2057);
					if (!(_prec(7))) throw createFailedPredicateException("_prec(7)");

					_matchStar(2061, 2058, 2063, NL);

					setState(2064);
					((InclusiveOrExprAltContext)_localctx).op = _match(BITOR);

					_matchStarPredict(2068, 2065, 2070, 277, 1, NL);

					setState(2071);
					((InclusiveOrExprAltContext)_localctx).right = expression(8);
					}
					break;
				case 11:
					{
					LogicalAndExprAltContext _altctx = new LogicalAndExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2072);
					if (!(_prec(6))) throw createFailedPredicateException("_prec(6)");

					_matchStar(2076, 2073, 2078, NL);

					setState(2079);
					((LogicalAndExprAltContext)_localctx).op = _match(AND);

					_matchStarPredict(2083, 2080, 2085, 279, 1, NL);

					setState(2086);
					((LogicalAndExprAltContext)_localctx).right = expression(7);
					}
					break;
				case 12:
					{
					LogicalOrExprAltContext _altctx = new LogicalOrExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2087);
					if (!(_prec(5))) throw createFailedPredicateException("_prec(5)");

					_matchStar(2091, 2088, 2093, NL);

					setState(2094);
					((LogicalOrExprAltContext)_localctx).op = _match(OR);

					_matchStarPredict(2098, 2095, 2100, 281, 1, NL);

					setState(2101);
					((LogicalOrExprAltContext)_localctx).right = expression(6);
					}
					break;
				case 13:
					{
					ImplicationExprAltContext _altctx = new ImplicationExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2102);
					if (!(_prec(4))) throw createFailedPredicateException("_prec(4)");

					_matchStar(2106, 2103, 2108, NL);

					setState(2109);
					((ImplicationExprAltContext)_localctx).op = _match(IMPLIES);

					_matchStarPredict(2113, 2110, 2115, 283, 1, NL);

					setState(2116);
					((ImplicationExprAltContext)_localctx).right = expression(4);
					}
					break;
				case 14:
					{
					ConditionalExprAltContext _altctx = new ConditionalExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.con = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2117);
					if (!(_prec(3))) throw createFailedPredicateException("_prec(3)");

					_matchStar(2121, 2118, 2123, NL);

					_sync(2152);
					_la = _input.LA(1);
					if (_la==QUESTION) {
						{
						setState(2124);
						_match(QUESTION);

						_matchStarPredict(2128, 2125, 2130, 285, 1, NL);

						setState(2131);
						((ConditionalExprAltContext)_localctx).tb = expression(0);

						_matchStar(2135, 2132, 2137, NL);

						setState(2138);
						_match(COLON);

						_matchStarPredict(2142, 2139, 2144, 287, 1, NL);

						}
					}
					else if (_la==ELVIS) {
						{
						setState(2145);
						_match(ELVIS);

						_matchStarPredict(2149, 2146, 2151, 288, 1, NL);

						}
					}
					else {
						throw new NoViableAltException(this);
					}

					setState(2154);
					((ConditionalExprAltContext)_localctx).fb = expression(3);
					}
					break;
				case 15:
					{
					RelationalExprAltContext _altctx = new RelationalExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2155);
					if (!(_prec(15))) throw createFailedPredicateException("_prec(15)");

					_matchStar(2159, 2156, 2161, NL);

					setState(2162);
					((RelationalExprAltContext)_localctx).op = _match(INSTANCEOF);

					_matchStar(2166, 2163, 2168, NL);

					setState(2169);
					matchingType();
					}
					break;
				case 16:
					{
					RelationalExprAltContext _altctx = new RelationalExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2170);
					if (!(_prec(14))) throw createFailedPredicateException("_prec(14)");

					_matchStar(2174, 2171, 2176, NL);

					setState(2177);
					((RelationalExprAltContext)_localctx).op = _match(NOT_INSTANCEOF);

					_matchStar(2181, 2178, 2183, NL);

					setState(2184);
					notInstanceofType();
					}
					break;
				case 17:
					{
					RelationalExprAltContext _altctx = new RelationalExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2185);
					if (!(_prec(13))) throw createFailedPredicateException("_prec(13)");

					_matchStar(2189, 2186, 2191, NL);

					setState(2192);
					((RelationalExprAltContext)_localctx).op = _match(AS);

					_matchStar(2196, 2193, 2198, NL);

					setState(2199);
					coercionType();
					}
					break;
				case 18:
					{
					AssignmentExprAltContext _altctx = new AssignmentExprAltContext(_parentctx, _parentState);
					_localctx = _altctx;
					_altctx.left = _prevctx;
					pushNewRecursionContext(_altctx, _startState, RULE_expression);

					setState(2200);
					if (!(_prec(1))) throw createFailedPredicateException("_prec(1)");

					_matchStar(2204, 2201, 2206, NL);

					setState(2207);
					{
						Token _st = _input.LT(1);
						_la = _st.getType();
						((AssignmentExprAltContext)_localctx).op = _matchSet(_st, ((((_la - 84)) & ~0x3f) == 0 && ((1L << (_la - 84)) & ((1L << (POWER_ASSIGN - 84)) | (1L << (ASSIGN - 84)) | (1L << (ADD_ASSIGN - 84)) | (1L << (SUB_ASSIGN - 84)) | (1L << (MUL_ASSIGN - 84)) | (1L << (DIV_ASSIGN - 84)) | (1L << (AND_ASSIGN - 84)) | (1L << (OR_ASSIGN - 84)) | (1L << (XOR_ASSIGN - 84)) | (1L << (MOD_ASSIGN - 84)) | (1L << (LSHIFT_ASSIGN - 84)) | (1L << (RSHIFT_ASSIGN - 84)) | (1L << (URSHIFT_ASSIGN - 84)) | (1L << (ELVIS_ASSIGN - 84)))) != 0));
					}

					_matchStarPredict(2211, 2208, 2213, 297, 1, NL);

					setState(2214);
					((AssignmentExprAltContext)_localctx).right = enhancedStatementExpression();
					}
					break;
				}
				}
				_alt = _syncPredict(2219, 299);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final ExpressionContext castOperandExpression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_castOperandExpression);
		int _la;
		try {
			int _alt;
			switch ( _syncPredict(2261, 304) ) {
			case 1:
				_localctx = new CastExprAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2220);
				castParExpression();
				setState(2221);
				castOperandExpression();
				}

				break;
			case 2:
				_localctx = new AsyncClosureExprAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2223);
				_match(ASYNC);

				_matchStar(2227, 2224, 2229, NL);

				 inAsyncClosureLevel++; 
				setState(2231);
				closureOrLambdaExpression();
				 inAsyncClosureLevel--; 
				}

				break;
			case 3:
				_localctx = new AwaitExprAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2234);
				if (!( _input.LA(2) != LPAREN )) throw createFailedPredicateException(" _input.LA(2) != LPAREN ");
				setState(2235);
				_match(AWAIT);
				setState(2236);
				expression(0);

				_alt = _syncPredict(2247, 302);
				while (_alt==1) {
					{
					{
					setState(2237);
					_match(COMMA);

					_matchStarPredict(2241, 2238, 2243, 301, 1, NL);

					setState(2244);
					expression(0);
					}
					}
					_alt = _syncPredict(2249, 302);
				}

				}

				break;
			case 4:
				_localctx = new PostfixExprAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2250);
				postfixExpression();
				}

				break;
			case 5:
				_localctx = new UnaryNotExprAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2251);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
				_matchSet(_st, _la==NOT || _la==BITNOT);
				}

				_matchStarPredict(2255, 2252, 2257, 303, 1, NL);

				setState(2258);
				castOperandExpression();
				}

				break;
			case 6:
				_localctx = new UnaryAddExprAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(2259);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
					((UnaryAddExprAltContext)_localctx).op = _matchSet(_st, ((((_la - 114)) & ~0x3f) == 0 && ((1L << (_la - 114)) & ((1L << (INC - 114)) | (1L << (DEC - 114)) | (1L << (ADD - 114)) | (1L << (SUB - 114)))) != 0));
				}
				setState(2260);
				castOperandExpression();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 250, RULE_commandExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2263);
			_localctx.expression = expression(0);

			switch ( _syncPredict(2267, 305) ) {
			case 1:
				{
				setState(2264);
				if (!( !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) )) throw createFailedPredicateException(" !SemanticPredicates.isFollowingArgumentsOrClosure($expression.ctx) ");
				setState(2265);
				argumentList();
				}
				break;
			case 2:
				{
				}
				break;
			}

			_alt = _syncPredict(2272, 306);
			while (_alt==1) {
				{
				{
				setState(2269);
				commandArgument();
				}
				}
				_alt = _syncPredict(2274, 306);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 252, RULE_commandArgument);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2275);
			commandPrimary();

			switch ( _syncPredict(2282, 308) ) {
			case 1:
				{
				_sync(2277); 
				do {
					{
					{
					setState(2276);
					pathElement();
					}
					}
					_alt = _syncPredict(2279, 307); 
				} while (_alt==1);
				}
				break;
			case 2:
				{
				setState(2281);
				argumentList();
				}
				break;
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 254, RULE_pathExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			switch ( _syncPredict(2287, 309) ) {
			case 1:
				{
				setState(2284);
				primary();
				}
				break;
			case 2:
				{
				setState(2285);
				if (!( _input.LT(2).getType() == DOT )) throw createFailedPredicateException(" _input.LT(2).getType() == DOT ");
				setState(2286);
				_match(STATIC);
				}
				break;
			}

			_alt = _syncPredict(2294, 310);
			while (_alt==1) {
				{
				{
				setState(2289);
				_localctx.pathElement = pathElement();
				 _localctx.t =  _localctx.pathElement.t; 
				}
				}
				_alt = _syncPredict(2296, 310);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathElementContext extends GroovyParserRuleContext {
		public int t;
		public TerminalNode DOT() { return getToken(GroovyParser.DOT, 0); }
		public NamePartContext namePart() {
			return getRuleContext(NamePartContext.class,0);
		}
		public TerminalNode METHOD_POINTER() { return getToken(GroovyParser.METHOD_POINTER, 0); }
		public TerminalNode METHOD_REFERENCE() { return getToken(GroovyParser.METHOD_REFERENCE, 0); }
		public ClosureOrLambdaExpressionContext closureOrLambdaExpression() {
			return getRuleContext(ClosureOrLambdaExpressionContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public TerminalNode SPREAD_DOT() { return getToken(GroovyParser.SPREAD_DOT, 0); }
		public TerminalNode SAFE_DOT() { return getToken(GroovyParser.SAFE_DOT, 0); }
		public TerminalNode SAFE_CHAIN_DOT() { return getToken(GroovyParser.SAFE_CHAIN_DOT, 0); }
		public TerminalNode NEW() { return getToken(GroovyParser.NEW, 0); }
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
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
		enterRule(_localctx, 256, RULE_pathElement);
		int _la;
		try {
			switch ( _syncPredict(2377, 322) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				_matchStar(2300, 2297, 2302, NL);

				_sync(2366);
				switch (_input.LA(1)) {
				case DOT:
					{
					setState(2303);
					_match(DOT);

					_matchStar(2307, 2304, 2309, NL);

					switch ( _syncPredict(2324, 315) ) {
					case 1:
						{
						setState(2310);
						_match(NEW);
						_sync(2312);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(2311);
							nonWildcardTypeArguments();
							}
						}

						setState(2314);
						creator(1);
						 _localctx.t =  6; 
						}
						break;
					case 2:
						{
						_sync(2319);
						_la = _input.LA(1);
						if (_la==AT) {
							{
							setState(2317);
							_match(AT);
							}
						}
						else if (_la==LT) {
							{
							setState(2318);
							nonWildcardTypeArguments();
							}
						}

						setState(2321);
						namePart();
						 _localctx.t =  1; 
						}
						break;
					}
					}
					break;
				case SPREAD_DOT:
				case SAFE_DOT:
				case SAFE_CHAIN_DOT:
					{
					setState(2326);
					{
						Token _st = _input.LT(1);
						_la = _st.getType();
					_matchSet(_st, ((((_la - 74)) & ~0x3f) == 0 && ((1L << (_la - 74)) & ((1L << (SPREAD_DOT - 74)) | (1L << (SAFE_DOT - 74)) | (1L << (SAFE_CHAIN_DOT - 74)))) != 0));
					}

					_matchStar(2330, 2327, 2332, NL);

					_sync(2335);
					_la = _input.LA(1);
					if (_la==AT) {
						{
						setState(2333);
						_match(AT);
						}
					}
					else if (_la==LT) {
						{
						setState(2334);
						nonWildcardTypeArguments();
						}
					}

					setState(2337);
					namePart();
					 _localctx.t =  1; 
					}
					break;
				case METHOD_POINTER:
					{
					setState(2340);
					_match(METHOD_POINTER);

					_matchStar(2344, 2341, 2346, NL);

					setState(2347);
					namePart();
					 _localctx.t =  1; 
					}
					break;
				case METHOD_REFERENCE:
					{
					setState(2350);
					_match(METHOD_REFERENCE);

					_matchStar(2354, 2351, 2356, NL);

					_sync(2358);
					_la = _input.LA(1);
					if (_la==LT) {
						{
						setState(2357);
						nonWildcardTypeArguments();
						}
					}

					setState(2360);
					namePart();
					 _localctx.t =  1; 
					}
					break;
				case LPAREN:
				case LBRACE:
					{
					setState(2363);
					closureOrLambdaExpression();
					 _localctx.t =  3; 
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
				setState(2368);
				arguments();
				 _localctx.t =  2; 
				}

				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2371);
				indexPropertyArgs();
				 _localctx.t =  4; 
				}

				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2374);
				namedPropertyArgs();
				 _localctx.t =  5; 
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 258, RULE_namePart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			switch ( _syncPredict(2383, 323) ) {
			case 1:
				{
				setState(2379);
				identifier();
				}
				break;
			case 2:
				{
				setState(2380);
				stringLiteral();
				}
				break;
			case 3:
				{
				setState(2381);
				dynamicMemberName();
				}
				break;
			case 4:
				{
				setState(2382);
				keywords();
				}
				break;
			}
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 260, RULE_dynamicMemberName);
		int _la;
		try {
			_sync(2387);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				enterOuterAlt(_localctx, 1);
				{
				setState(2385);
				parExpression();
				}

			}
			else if (_la==GStringBegin) {
				enterOuterAlt(_localctx, 2);
				{
				setState(2386);
				gstring();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 262, RULE_indexPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2389);
			{
				Token _st = _input.LT(1);
				_la = _st.getType();
			_matchSet(_st, _la==SAFE_INDEX || _la==LBRACK);
			}

			if ( _syncPredict(2391, 325)==1 ) {
				{
				setState(2390);
				expressionList(true);
				}
			}

			setState(2393);
			_match(RBRACK);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 264, RULE_namedPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2395);
			{
				Token _st = _input.LT(1);
				_la = _st.getType();
			_matchSet(_st, _la==SAFE_INDEX || _la==LBRACK);
			}

			_sync(2398);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << GStringBegin) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << THREADSAFE) | (1L << ASYNC) | (1L << AWAIT) | (1L << DEFER) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << ASSERT) | (1L << BREAK) | (1L << CASE) | (1L << CATCH) | (1L << CLASS) | (1L << CONST) | (1L << CONTINUE) | (1L << DEFAULT) | (1L << DO) | (1L << ELSE) | (1L << ENUM) | (1L << EXTENDS) | (1L << FINAL) | (1L << FINALLY) | (1L << FOR) | (1L << IF) | (1L << GOTO) | (1L << IMPLEMENTS) | (1L << IMPORT) | (1L << INSTANCEOF) | (1L << INTERFACE) | (1L << MODULE) | (1L << NATIVE) | (1L << NEW) | (1L << NON_SEALED) | (1L << PACKAGE) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << RETURN) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << SUPER) | (1L << SWITCH) | (1L << SYNCHRONIZED) | (1L << THIS) | (1L << THROW) | (1L << THROWS) | (1L << TRANSIENT) | (1L << TRY) | (1L << VAL) | (1L << VAR) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHILE - 64)) | (1L << (YIELD - 64)) | (1L << (IntegerLiteral - 64)) | (1L << (FloatingPointLiteral - 64)) | (1L << (BooleanLiteral - 64)) | (1L << (NullLiteral - 64)) | (1L << (LPAREN - 64)) | (1L << (LBRACK - 64)) | (1L << (MUL - 64)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(2396);
				namedPropertyArgList();
				}
			}
			else if (_la==COLON) {
				{
				setState(2397);
				_match(COLON);
				}
			}
			else {
				throw new NoViableAltException(this);
			}

			setState(2400);
			_match(RBRACK);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
	public static class AwaitPrmrAltContext extends PrimaryContext {
		public TerminalNode AWAIT() { return getToken(GroovyParser.AWAIT, 0); }
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public AwaitPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public AwaitPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitAwaitPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IdentifierPrmrAltContext extends PrimaryContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public IdentifierPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public LiteralPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public GstringPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public NewPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public ThisPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public ThisPrmrAltContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitThisPrmrAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SuperPrmrAltContext extends PrimaryContext {
		public TerminalNode SUPER() { return getToken(GroovyParser.SUPER, 0); }
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public SuperPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
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
		public ParenPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ClosureOrLambdaExpressionPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public ListPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public MapPrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		public BuiltInTypePrmrAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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
		enterRule(_localctx, 266, RULE_primary);
		int _la;
		try {
			int _alt;
			switch ( _syncPredict(2450, 334) ) {
			case 1:
				_localctx = new AwaitPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2402);
				_match(AWAIT);
				setState(2403);
				_match(LPAREN);
				setState(2404);
				expression(0);

				_sync(2415);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(2405);
					_match(COMMA);

					_matchStarPredict(2409, 2406, 2411, 327, 1, NL);

					setState(2412);
					expression(0);
					}
					}
					_sync(2417);
					_la = _input.LA(1);
				}

				setState(2418);
				_match(RPAREN);
				}

				break;
			case 2:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2420);
				identifier();

				if ( _syncPredict(2422, 329)==1 ) {
					{
					setState(2421);
					typeArguments();
					}
				}

				}

				break;
			case 3:
				_localctx = new LiteralPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2424);
				literal();
				}

				break;
			case 4:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2425);
				gstring();
				}

				break;
			case 5:
				_localctx = new NewPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2426);
				_match(NEW);

				_matchStar(2430, 2427, 2432, NL);

				_sync(2434);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(2433);
					nonWildcardTypeArguments();
					}
				}

				setState(2436);
				creator(0);
				}

				break;
			case 6:
				_localctx = new ThisPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				_sync(2438);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(2437);
					nonWildcardTypeArguments();
					}
				}

				setState(2440);
				_match(THIS);
				}

				break;
			case 7:
				_localctx = new SuperPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				_sync(2442);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(2441);
					nonWildcardTypeArguments();
					}
				}

				setState(2444);
				_match(SUPER);
				}

				break;
			case 8:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(2445);
				parExpression();
				}

				break;
			case 9:
				_localctx = new ClosureOrLambdaExpressionPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(2446);
				closureOrLambdaExpression();
				}

				break;
			case 10:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(2447);
				list();
				}

				break;
			case 11:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(2448);
				map();
				}

				break;
			case 12:
				_localctx = new BuiltInTypePrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(2449);
				builtInType();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final PrimaryContext namedPropertyArgPrimary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_namedPropertyArgPrimary);
		try {
			switch ( _syncPredict(2458, 335) ) {
			case 1:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(2452);
				identifier();
				}

				break;
			case 2:
				_localctx = new LiteralPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(2453);
				literal();
				}

				break;
			case 3:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(2454);
				gstring();
				}

				break;
			case 4:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(2455);
				parExpression();
				}

				break;
			case 5:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(2456);
				list();
				}

				break;
			case 6:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(2457);
				map();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final PrimaryContext namedArgPrimary() throws RecognitionException {
		int _parentState = getState();
		PrimaryContext _localctx = null;
		boolean _synced = false;
		int _la;
		try {
			for (;;) {
				_la = _input.LA(1);
				if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
					if (_localctx == null) {
						_localctx = new IdentifierPrmrAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 270, RULE_namedArgPrimary, 2463);
					} else {
						_localctx = new IdentifierPrmrAltContext(_localctx);
					}enterOuterAlt(_localctx, 1);
					{
					setState(2460);
					identifier();
					}

				}
				else if (_la==StringLiteral || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & ((1L << (IntegerLiteral - 66)) | (1L << (FloatingPointLiteral - 66)) | (1L << (BooleanLiteral - 66)) | (1L << (NullLiteral - 66)))) != 0)) {
					if (_localctx == null) {
						_localctx = new LiteralPrmrAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 270, RULE_namedArgPrimary, 2463);
					} else {
						_localctx = new LiteralPrmrAltContext(_localctx);
					}enterOuterAlt(_localctx, 2);
					{
					setState(2461);
					literal();
					}

				}
				else if (_la==GStringBegin) {
					if (_localctx == null) {
						_localctx = new GstringPrmrAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 270, RULE_namedArgPrimary, 2463);
					} else {
						_localctx = new GstringPrmrAltContext(_localctx);
					}enterOuterAlt(_localctx, 3);
					{
					setState(2462);
					gstring();
					}

				}
				else {
					if (!_synced) {
						_synced = true;
						_localctx = new PrimaryContext(_ctx, _parentState);
						_enterDeferred(_localctx, 270, RULE_namedArgPrimary, 2463);
						continue;
					}
					throw new NoViableAltException(this);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			if (_localctx == null) {
				_localctx = new PrimaryContext(_ctx, _parentState);
				enterRule(_localctx, 270, RULE_namedArgPrimary);
			}
			_recover(_localctx, re);
		}
		finally {
			if (_localctx != null) {
				exitRule();
			}
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final PrimaryContext commandPrimary() throws RecognitionException {
		int _parentState = getState();
		PrimaryContext _localctx = null;
		boolean _synced = false;
		int _la;
		try {
			for (;;) {
				_la = _input.LA(1);
				if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
					if (_localctx == null) {
						_localctx = new IdentifierPrmrAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 272, RULE_commandPrimary, 2468);
					} else {
						_localctx = new IdentifierPrmrAltContext(_localctx);
					}enterOuterAlt(_localctx, 1);
					{
					setState(2465);
					identifier();
					}

				}
				else if (_la==StringLiteral || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & ((1L << (IntegerLiteral - 66)) | (1L << (FloatingPointLiteral - 66)) | (1L << (BooleanLiteral - 66)) | (1L << (NullLiteral - 66)))) != 0)) {
					if (_localctx == null) {
						_localctx = new LiteralPrmrAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 272, RULE_commandPrimary, 2468);
					} else {
						_localctx = new LiteralPrmrAltContext(_localctx);
					}enterOuterAlt(_localctx, 2);
					{
					setState(2466);
					literal();
					}

				}
				else if (_la==GStringBegin) {
					if (_localctx == null) {
						_localctx = new GstringPrmrAltContext(_ctx, _parentState);
						_enterDeferred(_localctx, 272, RULE_commandPrimary, 2468);
					} else {
						_localctx = new GstringPrmrAltContext(_localctx);
					}enterOuterAlt(_localctx, 3);
					{
					setState(2467);
					gstring();
					}

				}
				else {
					if (!_synced) {
						_synced = true;
						_localctx = new PrimaryContext(_ctx, _parentState);
						_enterDeferred(_localctx, 272, RULE_commandPrimary, 2468);
						continue;
					}
					throw new NoViableAltException(this);
				}

				break;
			}
		}
		catch (RecognitionException re) {
			if (_localctx == null) {
				_localctx = new PrimaryContext(_ctx, _parentState);
				enterRule(_localctx, 272, RULE_commandPrimary);
			}
			_recover(_localctx, re);
		}
		finally {
			if (_localctx != null) {
				exitRule();
			}
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
		enterRule(_localctx, 274, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2470);
			_match(LBRACK);

			if ( _syncPredict(2472, 338)==1 ) {
				{
				setState(2471);
				expressionList(true);
				}
			}

			_sync(2475);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2474);
				_match(COMMA);
				}
			}

			setState(2477);
			_match(RBRACK);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 276, RULE_map);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2479);
			_match(LBRACK);

			_sync(2485);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << GStringBegin) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << THREADSAFE) | (1L << ASYNC) | (1L << AWAIT) | (1L << DEFER) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << ASSERT) | (1L << BREAK) | (1L << CASE) | (1L << CATCH) | (1L << CLASS) | (1L << CONST) | (1L << CONTINUE) | (1L << DEFAULT) | (1L << DO) | (1L << ELSE) | (1L << ENUM) | (1L << EXTENDS) | (1L << FINAL) | (1L << FINALLY) | (1L << FOR) | (1L << IF) | (1L << GOTO) | (1L << IMPLEMENTS) | (1L << IMPORT) | (1L << INSTANCEOF) | (1L << INTERFACE) | (1L << MODULE) | (1L << NATIVE) | (1L << NEW) | (1L << NON_SEALED) | (1L << PACKAGE) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << RETURN) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << SUPER) | (1L << SWITCH) | (1L << SYNCHRONIZED) | (1L << THIS) | (1L << THROW) | (1L << THROWS) | (1L << TRANSIENT) | (1L << TRY) | (1L << VAL) | (1L << VAR) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHILE - 64)) | (1L << (YIELD - 64)) | (1L << (IntegerLiteral - 64)) | (1L << (FloatingPointLiteral - 64)) | (1L << (BooleanLiteral - 64)) | (1L << (NullLiteral - 64)) | (1L << (LPAREN - 64)) | (1L << (LBRACE - 64)) | (1L << (LBRACK - 64)) | (1L << (LT - 64)) | (1L << (MUL - 64)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(2480);
				mapEntryList();
				_sync(2482);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2481);
					_match(COMMA);
					}
				}

				}
			}
			else if (_la==COLON) {
				{
				setState(2484);
				_match(COLON);
				}
			}
			else {
				throw new NoViableAltException(this);
			}

			setState(2487);
			_match(RBRACK);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 278, RULE_mapEntryList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2489);
			mapEntry();

			_alt = _syncPredict(2494, 342);
			while (_alt==1) {
				{
				{
				setState(2490);
				_match(COMMA);
				setState(2491);
				mapEntry();
				}
				}
				_alt = _syncPredict(2496, 342);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryListContext namedPropertyArgList() throws RecognitionException {
		MapEntryListContext _localctx = new MapEntryListContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_namedPropertyArgList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2497);
			namedPropertyArg();

			_sync(2502);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(2498);
				_match(COMMA);
				setState(2499);
				namedPropertyArg();
				}
				}
				_sync(2504);
				_la = _input.LA(1);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public EnhancedExpressionContext enhancedExpression() {
			return getRuleContext(EnhancedExpressionContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 282, RULE_mapEntry);
		int _la;
		try {
			int _alt;
			_sync(2524);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << GStringBegin) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << THREADSAFE) | (1L << ASYNC) | (1L << AWAIT) | (1L << DEFER) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << ASSERT) | (1L << BREAK) | (1L << CASE) | (1L << CATCH) | (1L << CLASS) | (1L << CONST) | (1L << CONTINUE) | (1L << DEFAULT) | (1L << DO) | (1L << ELSE) | (1L << ENUM) | (1L << EXTENDS) | (1L << FINAL) | (1L << FINALLY) | (1L << FOR) | (1L << IF) | (1L << GOTO) | (1L << IMPLEMENTS) | (1L << IMPORT) | (1L << INSTANCEOF) | (1L << INTERFACE) | (1L << MODULE) | (1L << NATIVE) | (1L << NEW) | (1L << NON_SEALED) | (1L << PACKAGE) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << RETURN) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << SUPER) | (1L << SWITCH) | (1L << SYNCHRONIZED) | (1L << THIS) | (1L << THROW) | (1L << THROWS) | (1L << TRANSIENT) | (1L << TRY) | (1L << VAL) | (1L << VAR) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHILE - 64)) | (1L << (YIELD - 64)) | (1L << (IntegerLiteral - 64)) | (1L << (FloatingPointLiteral - 64)) | (1L << (BooleanLiteral - 64)) | (1L << (NullLiteral - 64)) | (1L << (LPAREN - 64)) | (1L << (LBRACE - 64)) | (1L << (LBRACK - 64)) | (1L << (LT - 64)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				enterOuterAlt(_localctx, 1);
				{
				setState(2505);
				mapEntryLabel();
				setState(2506);
				_match(COLON);

				_matchStarPredict(2510, 2507, 2512, 344, 1, NL);

				setState(2513);
				enhancedExpression();
				}

			}
			else if (_la==MUL) {
				enterOuterAlt(_localctx, 2);
				{
				setState(2515);
				_match(MUL);
				setState(2516);
				_match(COLON);

				_matchStarPredict(2520, 2517, 2522, 345, 1, NL);

				setState(2523);
				enhancedExpression();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryContext namedPropertyArg() throws RecognitionException {
		MapEntryContext _localctx = new MapEntryContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_namedPropertyArg);
		int _la;
		try {
			int _alt;
			_sync(2545);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << GStringBegin) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << THREADSAFE) | (1L << ASYNC) | (1L << AWAIT) | (1L << DEFER) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << ASSERT) | (1L << BREAK) | (1L << CASE) | (1L << CATCH) | (1L << CLASS) | (1L << CONST) | (1L << CONTINUE) | (1L << DEFAULT) | (1L << DO) | (1L << ELSE) | (1L << ENUM) | (1L << EXTENDS) | (1L << FINAL) | (1L << FINALLY) | (1L << FOR) | (1L << IF) | (1L << GOTO) | (1L << IMPLEMENTS) | (1L << IMPORT) | (1L << INSTANCEOF) | (1L << INTERFACE) | (1L << MODULE) | (1L << NATIVE) | (1L << NEW) | (1L << NON_SEALED) | (1L << PACKAGE) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << RETURN) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << SUPER) | (1L << SWITCH) | (1L << SYNCHRONIZED) | (1L << THIS) | (1L << THROW) | (1L << THROWS) | (1L << TRANSIENT) | (1L << TRY) | (1L << VAL) | (1L << VAR) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHILE - 64)) | (1L << (YIELD - 64)) | (1L << (IntegerLiteral - 64)) | (1L << (FloatingPointLiteral - 64)) | (1L << (BooleanLiteral - 64)) | (1L << (NullLiteral - 64)) | (1L << (LPAREN - 64)) | (1L << (LBRACK - 64)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				enterOuterAlt(_localctx, 1);
				{
				setState(2526);
				namedPropertyArgLabel();
				setState(2527);
				_match(COLON);

				_matchStarPredict(2531, 2528, 2533, 347, 1, NL);

				setState(2534);
				enhancedExpression();
				}

			}
			else if (_la==MUL) {
				enterOuterAlt(_localctx, 2);
				{
				setState(2536);
				_match(MUL);
				setState(2537);
				_match(COLON);

				_matchStarPredict(2541, 2538, 2543, 348, 1, NL);

				setState(2544);
				enhancedExpression();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryContext namedArg() throws RecognitionException {
		MapEntryContext _localctx = new MapEntryContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_namedArg);
		int _la;
		try {
			int _alt;
			_sync(2566);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << GStringBegin) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << THREADSAFE) | (1L << ASYNC) | (1L << AWAIT) | (1L << DEFER) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << ASSERT) | (1L << BREAK) | (1L << CASE) | (1L << CATCH) | (1L << CLASS) | (1L << CONST) | (1L << CONTINUE) | (1L << DEFAULT) | (1L << DO) | (1L << ELSE) | (1L << ENUM) | (1L << EXTENDS) | (1L << FINAL) | (1L << FINALLY) | (1L << FOR) | (1L << IF) | (1L << GOTO) | (1L << IMPLEMENTS) | (1L << IMPORT) | (1L << INSTANCEOF) | (1L << INTERFACE) | (1L << MODULE) | (1L << NATIVE) | (1L << NEW) | (1L << NON_SEALED) | (1L << PACKAGE) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << RETURN) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << SUPER) | (1L << SWITCH) | (1L << SYNCHRONIZED) | (1L << THIS) | (1L << THROW) | (1L << THROWS) | (1L << TRANSIENT) | (1L << TRY) | (1L << VAL) | (1L << VAR) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHILE - 64)) | (1L << (YIELD - 64)) | (1L << (IntegerLiteral - 64)) | (1L << (FloatingPointLiteral - 64)) | (1L << (BooleanLiteral - 64)) | (1L << (NullLiteral - 64)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				enterOuterAlt(_localctx, 1);
				{
				setState(2547);
				namedArgLabel();
				setState(2548);
				_match(COLON);

				_matchStarPredict(2552, 2549, 2554, 350, 1, NL);

				setState(2555);
				enhancedExpression();
				}

			}
			else if (_la==MUL) {
				enterOuterAlt(_localctx, 2);
				{
				setState(2557);
				_match(MUL);
				setState(2558);
				_match(COLON);

				_matchStarPredict(2562, 2559, 2564, 351, 1, NL);

				setState(2565);
				enhancedExpression();
				}

			}
			else {
				throw new NoViableAltException(this);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 288, RULE_mapEntryLabel);
		try {
			switch ( _syncPredict(2570, 353) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2568);
				keywords();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2569);
				primary();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryLabelContext namedPropertyArgLabel() throws RecognitionException {
		MapEntryLabelContext _localctx = new MapEntryLabelContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_namedPropertyArgLabel);
		try {
			switch ( _syncPredict(2574, 354) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2572);
				keywords();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2573);
				namedPropertyArgPrimary();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final MapEntryLabelContext namedArgLabel() throws RecognitionException {
		MapEntryLabelContext _localctx = new MapEntryLabelContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_namedArgLabel);
		try {
			switch ( _syncPredict(2578, 355) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2576);
				keywords();
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2577);
				namedArgPrimary();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 294, RULE_creator);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2580);
			createdName();

			switch ( _syncPredict(2615, 362) ) {
			case 1:
				{
				_matchStar(2584, 2581, 2586, NL);

				setState(2587);
				arguments();

				if ( _syncPredict(2589, 357)==1 ) {
					{
					setState(2588);
					anonymousInnerClassDeclaration(0);
					}
				}

				}
				break;
			case 2:
				{
				_sync(2592); 
				do {
					{
					{
					setState(2591);
					dim0();
					}
					}
					_sync(2594); 
					_la = _input.LA(1);
				} while ( _la==LBRACK || _la==AT );

				_matchStar(2599, 2596, 2601, NL);

				setState(2602);
				arrayInitializer();
				}
				break;
			case 3:
				{
				_sync(2605); 
				do {
					{
					{
					setState(2604);
					dim1();
					}
					}
					_alt = _syncPredict(2607, 360); 
				} while (_alt==1);

				_alt = _syncPredict(2612, 361);
				while (_alt==1) {
					{
					{
					setState(2609);
					dim0();
					}
					}
					_alt = _syncPredict(2614, 361);
				}

				}
				break;
			}
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 296, RULE_dim0);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2617);
			annotationsOpt();
			setState(2618);
			_match(LBRACK);
			setState(2619);
			_match(RBRACK);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 298, RULE_dim1);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2621);
			annotationsOpt();
			setState(2622);
			_match(LBRACK);
			setState(2623);
			expression(0);
			setState(2624);
			_match(RBRACK);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayInitializerContext extends GroovyParserRuleContext {
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 300, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2626);
			_match(LBRACE);

			_matchStarPredict(2630, 2627, 2632, 363, 1, NL);

			if ( _syncPredict(2665, 370)==1 ) {
				{
				switch ( _syncPredict(2635, 364) ) {
				case 1:
					{
					setState(2633);
					arrayInitializer();
					}
					break;
				case 2:
					{
					setState(2634);
					variableInitializer();
					}
					break;
				}

				_matchStarPredict(2640, 2637, 2642, 365, 1, NL);

				_alt = _syncPredict(2662, 369);
				while (_alt==1) {
					{
					{
					setState(2643);
					_match(COMMA);

					_matchStarPredict(2647, 2644, 2649, 366, 1, NL);

					switch ( _syncPredict(2652, 367) ) {
					case 1:
						{
						setState(2650);
						arrayInitializer();
						}
						break;
					case 2:
						{
						setState(2651);
						variableInitializer();
						}
						break;
					}

					_matchStarPredict(2657, 2654, 2659, 368, 1, NL);

					}
					}
					_alt = _syncPredict(2664, 369);
				}

				}
			}

			_sync(2668);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2667);
				_match(COMMA);
				}
			}

			_matchStar(2673, 2670, 2675, NL);

			setState(2676);
			_match(RBRACE);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 302, RULE_anonymousInnerClassDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2678);
			classBody(0);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TypeArgumentsOrDiamondContext> typeArgumentsOrDiamond() {
			return getRuleContexts(TypeArgumentsOrDiamondContext.class);
		}
		public TypeArgumentsOrDiamondContext typeArgumentsOrDiamond(int i) {
			return getRuleContext(TypeArgumentsOrDiamondContext.class,i);
		}
		public List<? extends TerminalNode> DOT() { return getTokens(GroovyParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(GroovyParser.DOT, i);
		}
		public List<? extends IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
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
		enterRule(_localctx, 304, RULE_createdName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2680);
			annotationsOpt();

			_sync(2696);
			_la = _input.LA(1);
			if (_la==BuiltInPrimitiveType) {
				{
				setState(2681);
				primitiveType();
				}
			}
			else if (((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(2682);
				qualifiedClassName();
				_sync(2694);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(2683);
					typeArgumentsOrDiamond();

					_sync(2691);
					_la = _input.LA(1);
					while (_la==DOT) {
						{
						{
						setState(2684);
						_match(DOT);
						setState(2685);
						identifier();
						_sync(2687);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(2686);
							typeArgumentsOrDiamond();
							}
						}

						}
						}
						_sync(2693);
						_la = _input.LA(1);
					}

					}
				}

				}
			}
			else {
				throw new NoViableAltException(this);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonWildcardTypeArgumentsContext extends GroovyParserRuleContext {
		public TerminalNode LT() { return getToken(GroovyParser.LT, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public TerminalNode GT() { return getToken(GroovyParser.GT, 0); }
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
		}
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
		enterRule(_localctx, 306, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2698);
			_match(LT);

			_matchStar(2702, 2699, 2704, NL);

			setState(2705);
			typeList();

			_matchStar(2709, 2706, 2711, NL);

			setState(2712);
			_match(GT);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 308, RULE_typeArgumentsOrDiamond);
		try {
			switch ( _syncPredict(2717, 379) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2714);
				_match(LT);
				setState(2715);
				_match(GT);
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2716);
				typeArguments();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 310, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2719);
			_match(LPAREN);

			if ( _syncPredict(2721, 380)==1 ) {
				{
				setState(2720);
				enhancedArgumentListInPar();
				}
			}

			_sync(2724);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2723);
				_match(COMMA);
				}
			}

			setState(2726);
			_match(RPAREN);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final EnhancedArgumentListInParContext argumentList() throws RecognitionException {
		EnhancedArgumentListInParContext _localctx = new EnhancedArgumentListInParContext(_ctx, getState());
		enterRule(_localctx, 312, RULE_argumentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2728);
			firstArgumentListElement();

			_alt = _syncPredict(2739, 383);
			while (_alt==1) {
				{
				{
				setState(2729);
				_match(COMMA);

				_matchStarPredict(2733, 2730, 2735, 382, 1, NL);

				setState(2736);
				argumentListElement();
				}
				}
				_alt = _syncPredict(2741, 383);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		public List<? extends TerminalNode> NL() { return getTokens(GroovyParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(GroovyParser.NL, i);
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
		enterRule(_localctx, 314, RULE_enhancedArgumentListInPar);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2742);
			enhancedArgumentListElement();

			_alt = _syncPredict(2753, 385);
			while (_alt==1) {
				{
				{
				setState(2743);
				_match(COMMA);

				_matchStarPredict(2747, 2744, 2749, 384, 1, NL);

				setState(2750);
				enhancedArgumentListElement();
				}
				}
				_alt = _syncPredict(2755, 385);
			}

			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final EnhancedArgumentListElementContext firstArgumentListElement() throws RecognitionException {
		EnhancedArgumentListElementContext _localctx = new EnhancedArgumentListElementContext(_ctx, getState());
		enterRule(_localctx, 316, RULE_firstArgumentListElement);
		try {
			switch ( _syncPredict(2758, 386) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2756);
				expressionListElement(true);
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2757);
				namedArg();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final EnhancedArgumentListElementContext argumentListElement() throws RecognitionException {
		EnhancedArgumentListElementContext _localctx = new EnhancedArgumentListElementContext(_ctx, getState());
		enterRule(_localctx, 318, RULE_argumentListElement);
		try {
			switch ( _syncPredict(2762, 387) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2760);
				expressionListElement(true);
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2761);
				namedPropertyArg();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 320, RULE_enhancedArgumentListElement);
		try {
			switch ( _syncPredict(2767, 388) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2764);
				expressionListElement(true);
				}

				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2765);
				standardLambdaExpression();
				}

				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2766);
				namedPropertyArg();
				}

				break;
			}
		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 322, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2769);
			_match(StringLiteral);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 324, RULE_className);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2771);
			_match(CapitalizedIdentifier);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 326, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2773);
			{
				Token _st = _input.LT(1);
				_la = _st.getType();
			_matchSet(_st, ((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (MODULE - 7)) | (1L << (PERMITS - 7)) | (1L << (RECORD - 7)) | (1L << (SEALED - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (YIELD - 7)))) != 0) || _la==CapitalizedIdentifier || _la==Identifier);
			}
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 328, RULE_builtInType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2775);
			{
				Token _st = _input.LT(1);
				_la = _st.getType();
			_matchSet(_st, _la==BuiltInPrimitiveType || _la==VOID);
			}
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
		enterRule(_localctx, 330, RULE_keywords);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2777);
			{
				Token _st = _input.LT(1);
				_la = _st.getType();
			_matchSet(_st, ((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (THREADSAFE - 7)) | (1L << (ASYNC - 7)) | (1L << (AWAIT - 7)) | (1L << (DEFER - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (ABSTRACT - 7)) | (1L << (ASSERT - 7)) | (1L << (BREAK - 7)) | (1L << (CASE - 7)) | (1L << (CATCH - 7)) | (1L << (CLASS - 7)) | (1L << (CONST - 7)) | (1L << (CONTINUE - 7)) | (1L << (DEFAULT - 7)) | (1L << (DO - 7)) | (1L << (ELSE - 7)) | (1L << (ENUM - 7)) | (1L << (EXTENDS - 7)) | (1L << (FINAL - 7)) | (1L << (FINALLY - 7)) | (1L << (FOR - 7)) | (1L << (IF - 7)) | (1L << (GOTO - 7)) | (1L << (IMPLEMENTS - 7)) | (1L << (IMPORT - 7)) | (1L << (INSTANCEOF - 7)) | (1L << (INTERFACE - 7)) | (1L << (NATIVE - 7)) | (1L << (NEW - 7)) | (1L << (NON_SEALED - 7)) | (1L << (PACKAGE - 7)) | (1L << (PERMITS - 7)) | (1L << (PRIVATE - 7)) | (1L << (PROTECTED - 7)) | (1L << (PUBLIC - 7)) | (1L << (RECORD - 7)) | (1L << (RETURN - 7)) | (1L << (SEALED - 7)) | (1L << (STATIC - 7)) | (1L << (STRICTFP - 7)) | (1L << (SUPER - 7)) | (1L << (SWITCH - 7)) | (1L << (SYNCHRONIZED - 7)) | (1L << (THIS - 7)) | (1L << (THROW - 7)) | (1L << (THROWS - 7)) | (1L << (TRANSIENT - 7)) | (1L << (TRY - 7)) | (1L << (VAL - 7)) | (1L << (VAR - 7)) | (1L << (VOID - 7)) | (1L << (VOLATILE - 7)) | (1L << (WHILE - 7)) | (1L << (YIELD - 7)) | (1L << (BooleanLiteral - 7)) | (1L << (NullLiteral - 7)))) != 0));
			}
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
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
			_sync(2780); 
			do {
				{
				{
				setState(2779);
				{
					Token _st = _input.LT(1);
					_la = _st.getType();
				_matchSet(_st, _la==SEMI || _la==NL);
				}
				}
				}
				_alt = _syncPredict(2782, 389); 
			} while (_alt==1);
			}

		}
		catch (RecognitionException re) {
			_recover(_localctx, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@Override
	public boolean sempred(RuleContext _localctx, int _ruleIndex, int predIndex) {
		switch (predIndex) {
		case 0:
			return  !SemanticPredicates.isInvalidMethodDeclaration(_input) ;
		case 1:
			return _sempred_1((ClassBodyContext)_localctx);
		case 2:
			return _sempred_2((MethodDeclarationContext)_localctx);
		case 3:
			return _sempred_3((VariableDeclaratorContext)_localctx);
		case 4:
			return  !SemanticPredicates.isIdentifierAssign(_input) ;
		case 5:
			return  !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ;
		case 6:
			return  inSwitchExpressionLevel > 0 ;
		case 7:
			return  inAsyncClosureLevel > 0 ;
		case 8:
			return  _input.LT(1).getType() == RBRACE ;
		case 9:
			return  _input.LA(2) != LPAREN ;
		case 10:
			return _prec(20);
		case 11:
			return _prec(18);
		case 12:
			return _prec(17);
		case 13:
			return _prec(16);
		case 14:
			return _prec(12);
		case 15:
			return _prec(11);
		case 16:
			return _prec(10);
		case 17:
			return _prec(9);
		case 18:
			return _prec(8);
		case 19:
			return _prec(7);
		case 20:
			return _prec(6);
		case 21:
			return _prec(5);
		case 22:
			return _prec(4);
		case 23:
			return _prec(3);
		case 24:
			return _prec(15);
		case 25:
			return _prec(14);
		case 26:
			return _prec(13);
		case 27:
			return _prec(1);
		case 28:
			return  _input.LA(2) != LPAREN ;
		case 29:
			return _sempred_29((CommandExpressionContext)_localctx);
		case 30:
			return  _input.LT(2).getType() == DOT ;
		}
		return true;
	}
	private boolean _sempred_1(ClassBodyContext _localctx) {
		return  _localctx.t == 2 ;
	}
	private boolean _sempred_2(MethodDeclarationContext _localctx) {
		return  _localctx.ct == 3 ;
	}
	private boolean _sempred_3(VariableDeclaratorContext _localctx) {
		return _localctx.arrayType;
	}
	private boolean _sempred_29(CommandExpressionContext _localctx) {
		return  !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) ;
	}
	private static final String _serializedATNSegment0 =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\3\u0091\u0ae3\4\2\t"+
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
		"\4\u00a5\t\u00a5\4\u00a6\t\u00a6\4\u00a7\t\u00a7\4\u00a8\t\u00a8\3\2\7"+
		"\2\u0152\n\2\f\2\16\2\u0155\13\2\3\2\3\2\5\2\u0159\n\2\5\2\u015b\n\2\3"+
		"\2\5\2\u015e\n\2\3\2\3\2\3\3\3\3\3\3\3\3\7\3\u0166\n\3\f\3\16\3\u0169"+
		"\13\3\3\3\5\3\u016c\n\3\3\4\3\4\3\4\3\4\3\4\5\4\u0173\n\4\3\5\3\5\3\5"+
		"\3\5\3\6\3\6\3\6\3\6\3\6\5\6\u017e\n\6\3\6\3\6\3\6\3\6\3\6\5\6\u0185\n"+
		"\6\5\6\u0187\n\6\3\7\3\7\3\7\3\b\3\b\5\b\u018e\n\b\3\t\3\t\7\t\u0192\n"+
		"\t\f\t\16\t\u0195\13\t\5\t\u0197\n\t\3\n\3\n\7\n\u019b\n\n\f\n\16\n\u019e"+
		"\13\n\3\n\7\n\u01a1\n\n\f\n\16\n\u01a4\13\n\3\13\3\13\7\13\u01a8\n\13"+
		"\f\13\16\13\u01ab\13\13\5\13\u01ad\n\13\3\f\3\f\7\f\u01b1\n\f\f\f\16\f"+
		"\u01b4\13\f\3\f\7\f\u01b7\n\f\f\f\16\f\u01ba\13\f\3\r\3\r\5\r\u01be\n"+
		"\r\3\16\3\16\5\16\u01c2\n\16\3\17\3\17\7\17\u01c6\n\17\f\17\16\17\u01c9"+
		"\13\17\5\17\u01cb\n\17\3\20\3\20\7\20\u01cf\n\20\f\20\16\20\u01d2\13\20"+
		"\3\20\7\20\u01d5\n\20\f\20\16\20\u01d8\13\20\3\21\3\21\7\21\u01dc\n\21"+
		"\f\21\16\21\u01df\13\21\3\21\3\21\3\21\7\21\u01e4\n\21\f\21\16\21\u01e7"+
		"\13\21\3\21\7\21\u01ea\n\21\f\21\16\21\u01ed\13\21\3\21\7\21\u01f0\n\21"+
		"\f\21\16\21\u01f3\13\21\3\21\3\21\3\22\3\22\3\22\3\22\7\22\u01fb\n\22"+
		"\f\22\16\22\u01fe\13\22\3\22\5\22\u0201\n\22\3\23\3\23\3\23\7\23\u0206"+
		"\n\23\f\23\16\23\u0209\13\23\3\23\7\23\u020c\n\23\f\23\16\23\u020f\13"+
		"\23\3\24\3\24\3\24\7\24\u0214\n\24\f\24\16\24\u0217\13\24\3\24\7\24\u021a"+
		"\n\24\f\24\16\24\u021d\13\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\3\25\5\25\u022c\n\25\3\25\3\25\7\25\u0230\n\25\f\25"+
		"\16\25\u0233\13\25\3\25\5\25\u0236\n\25\3\25\7\25\u0239\n\25\f\25\16\25"+
		"\u023c\13\25\3\25\5\25\u023f\n\25\3\25\7\25\u0242\n\25\f\25\16\25\u0245"+
		"\13\25\3\25\3\25\7\25\u0249\n\25\f\25\16\25\u024c\13\25\3\25\5\25\u024f"+
		"\n\25\3\25\7\25\u0252\n\25\f\25\16\25\u0255\13\25\3\25\3\25\7\25\u0259"+
		"\n\25\f\25\16\25\u025c\13\25\3\25\5\25\u025f\n\25\3\25\7\25\u0262\n\25"+
		"\f\25\16\25\u0265\13\25\3\25\3\25\7\25\u0269\n\25\f\25\16\25\u026c\13"+
		"\25\3\25\5\25\u026f\n\25\3\25\7\25\u0272\n\25\f\25\16\25\u0275\13\25\3"+
		"\25\3\25\3\26\3\26\7\26\u027b\n\26\f\26\16\26\u027e\13\26\3\26\3\26\3"+
		"\26\7\26\u0283\n\26\f\26\16\26\u0286\13\26\3\26\5\26\u0289\n\26\3\26\7"+
		"\26\u028c\n\26\f\26\16\26\u028f\13\26\3\26\5\26\u0292\n\26\3\26\7\26\u0295"+
		"\n\26\f\26\16\26\u0298\13\26\3\26\5\26\u029b\n\26\3\26\7\26\u029e\n\26"+
		"\f\26\16\26\u02a1\13\26\3\26\3\26\3\26\3\26\7\26\u02a7\n\26\f\26\16\26"+
		"\u02aa\13\26\5\26\u02ac\n\26\3\26\3\26\3\26\3\26\7\26\u02b2\n\26\f\26"+
		"\16\26\u02b5\13\26\5\26\u02b7\n\26\5\26\u02b9\n\26\3\26\5\26\u02bc\n\26"+
		"\3\26\3\26\3\27\3\27\7\27\u02c2\n\27\f\27\16\27\u02c5\13\27\3\27\3\27"+
		"\7\27\u02c9\n\27\f\27\16\27\u02cc\13\27\3\27\7\27\u02cf\n\27\f\27\16\27"+
		"\u02d2\13\27\3\30\3\30\3\30\5\30\u02d7\n\30\3\30\5\30\u02da\n\30\3\31"+
		"\3\31\7\31\u02de\n\31\f\31\16\31\u02e1\13\31\5\31\u02e3\n\31\3\31\3\31"+
		"\5\31\u02e7\n\31\3\32\3\32\3\32\3\32\3\32\5\32\u02ee\n\32\5\32\u02f0\n"+
		"\32\3\33\3\33\5\33\u02f4\n\33\3\33\3\33\7\33\u02f8\n\33\f\33\16\33\u02fb"+
		"\13\33\5\33\u02fd\n\33\3\33\3\33\3\33\3\33\3\33\7\33\u0304\n\33\f\33\16"+
		"\33\u0307\13\33\3\33\3\33\7\33\u030b\n\33\f\33\16\33\u030e\13\33\3\33"+
		"\3\33\7\33\u0312\n\33\f\33\16\33\u0315\13\33\3\33\3\33\7\33\u0319\n\33"+
		"\f\33\16\33\u031c\13\33\3\33\5\33\u031f\n\33\3\33\7\33\u0322\n\33\f\33"+
		"\16\33\u0325\13\33\3\33\5\33\u0328\n\33\3\34\3\34\7\34\u032c\n\34\f\34"+
		"\16\34\u032f\13\34\3\34\3\34\3\35\3\35\5\35\u0335\n\35\3\36\3\36\5\36"+
		"\u0339\n\36\3\37\3\37\3 \3 \3 \7 \u0340\n \f \16 \u0343\13 \3 \7 \u0346"+
		"\n \f \16 \u0349\13 \3!\3!\7!\u034d\n!\f!\16!\u0350\13!\3!\3!\7!\u0354"+
		"\n!\f!\16!\u0357\13!\3!\3!\3!\5!\u035c\n!\5!\u035e\n!\3\"\3\"\5\"\u0362"+
		"\n\"\3#\3#\3$\3$\3$\3$\5$\u036a\n$\3$\7$\u036d\n$\f$\16$\u0370\13$\3%"+
		"\3%\3&\3&\3&\3&\3&\5&\u0379\n&\7&\u037b\n&\f&\16&\u037e\13&\5&\u0380\n"+
		"&\3\'\3\'\5\'\u0384\n\'\3(\3(\5(\u0388\n(\3)\3)\3)\5)\u038d\n)\3)\7)\u0390"+
		"\n)\f)\16)\u0393\13)\3*\3*\3*\3*\3*\5*\u039a\n*\7*\u039c\n*\f*\16*\u039f"+
		"\13*\5*\u03a1\n*\3+\3+\7+\u03a5\n+\f+\16+\u03a8\13+\3+\3+\3+\7+\u03ad"+
		"\n+\f+\16+\u03b0\13+\3+\7+\u03b3\n+\f+\16+\u03b6\13+\3+\7+\u03b9\n+\f"+
		"+\16+\u03bc\13+\3+\3+\3,\3,\3,\3,\3,\7,\u03c5\n,\f,\16,\u03c8\13,\3,\5"+
		",\u03cb\n,\5,\u03cd\n,\3-\3-\3-\3.\3.\3.\7.\u03d5\n.\f.\16.\u03d8\13."+
		"\3.\7.\u03db\n.\f.\16.\u03de\13.\3/\3/\5/\u03e2\n/\3/\3/\3\60\3\60\5\60"+
		"\u03e8\n\60\3\60\3\60\7\60\u03ec\n\60\f\60\16\60\u03ef\13\60\3\60\7\60"+
		"\u03f2\n\60\f\60\16\60\u03f5\13\60\3\61\3\61\3\61\3\62\3\62\5\62\u03fc"+
		"\n\62\3\62\5\62\u03ff\n\62\3\62\3\62\7\62\u0403\n\62\f\62\16\62\u0406"+
		"\13\62\3\62\3\62\7\62\u040a\n\62\f\62\16\62\u040d\13\62\3\62\5\62\u0410"+
		"\n\62\3\63\3\63\3\64\3\64\3\64\7\64\u0417\n\64\f\64\16\64\u041a\13\64"+
		"\3\65\3\65\3\65\3\65\3\65\5\65\u0421\n\65\3\66\3\66\3\66\7\66\u0426\n"+
		"\66\f\66\16\66\u0429\13\66\3\67\3\67\3\67\38\38\38\38\78\u0432\n8\f8\16"+
		"8\u0435\138\39\39\39\39\39\59\u043c\n9\3:\3:\3:\3:\7:\u0442\n:\f:\16:"+
		"\u0445\13:\3:\3:\3;\3;\5;\u044b\n;\3<\3<\7<\u044f\n<\f<\16<\u0452\13<"+
		"\3=\3=\7=\u0456\n=\f=\16=\u0459\13=\3=\3=\7=\u045d\n=\f=\16=\u0460\13"+
		"=\3=\3=\3>\3>\7>\u0466\n>\f>\16>\u0469\13>\3>\3>\7>\u046d\n>\f>\16>\u0470"+
		"\13>\3>\3>\3?\3?\3@\3@\5@\u0478\n@\3A\3A\5A\u047c\nA\3B\3B\7B\u0480\n"+
		"B\fB\16B\u0483\13B\3B\3B\7B\u0487\nB\fB\16B\u048a\13B\5B\u048c\nB\3B\5"+
		"B\u048f\nB\3B\5B\u0492\nB\3B\3B\3B\3C\3C\5C\u0499\nC\3D\5D\u049c\nD\3"+
		"E\3E\3E\3E\7E\u04a2\nE\fE\16E\u04a5\13E\3E\5E\u04a8\nE\3F\3F\7F\u04ac"+
		"\nF\fF\16F\u04af\13F\3F\7F\u04b2\nF\fF\16F\u04b5\13F\3F\7F\u04b8\nF\f"+
		"F\16F\u04bb\13F\5F\u04bd\nF\3G\3G\3G\7G\u04c2\nG\fG\16G\u04c5\13G\3G\3"+
		"G\5G\u04c9\nG\3G\5G\u04cc\nG\3H\3H\3H\5H\u04d1\nH\3I\3I\3J\3J\3J\7J\u04d8"+
		"\nJ\fJ\16J\u04db\13J\3K\3K\7K\u04df\nK\fK\16K\u04e2\13K\3K\3K\7K\u04e6"+
		"\nK\fK\16K\u04e9\13K\3K\3K\3L\3L\5L\u04ef\nL\3M\3M\3M\5M\u04f4\nM\3N\3"+
		"N\3N\3N\7N\u04fa\nN\fN\16N\u04fd\13N\3N\5N\u0500\nN\5N\u0502\nN\3N\3N"+
		"\3N\3N\3N\6N\u0509\nN\rN\16N\u050a\3N\5N\u050e\nN\3N\3N\5N\u0512\nN\3"+
		"O\3O\5O\u0516\nO\3O\3O\3O\3P\3P\3Q\3Q\3Q\3R\3R\7R\u0522\nR\fR\16R\u0525"+
		"\13R\3R\5R\u0528\nR\3R\3R\3R\7R\u052d\nR\fR\16R\u0530\13R\3R\3R\7R\u0534"+
		"\nR\fR\16R\u0537\13R\3R\3R\5R\u053b\nR\3R\3R\3R\5R\u0540\nR\3S\3S\3S\3"+
		"S\7S\u0546\nS\fS\16S\u0549\13S\3S\3S\3S\7S\u054e\nS\fS\16S\u0551\13S\5"+
		"S\u0553\nS\3S\3S\3T\3T\3T\3T\5T\u055b\nT\3T\5T\u055e\nT\3T\3T\3U\3U\3"+
		"U\3U\3U\3U\5U\u0568\nU\3U\3U\3V\3V\3V\3V\6V\u0570\nV\rV\16V\u0571\3V\3"+
		"V\3W\3W\5W\u0578\nW\3X\3X\3X\7X\u057d\nX\fX\16X\u0580\13X\3X\3X\7X\u0584"+
		"\nX\fX\16X\u0587\13X\3X\5X\u058a\nX\3X\3X\7X\u058e\nX\fX\16X\u0591\13"+
		"X\3X\5X\u0594\nX\3Y\3Y\3Y\7Y\u0599\nY\fY\16Y\u059c\13Y\3Y\3Y\7Y\u05a0"+
		"\nY\fY\16Y\u05a3\13Y\3Y\6Y\u05a6\nY\rY\16Y\u05a7\3Y\7Y\u05ab\nY\fY\16"+
		"Y\u05ae\13Y\5Y\u05b0\nY\3Y\3Y\3Z\3Z\3Z\5Z\u05b7\nZ\3Z\3Z\3Z\3Z\7Z\u05bd"+
		"\nZ\fZ\16Z\u05c0\13Z\3Z\3Z\3Z\3Z\3Z\3Z\7Z\u05c8\nZ\fZ\16Z\u05cb\13Z\3"+
		"Z\3Z\3Z\3Z\3Z\7Z\u05d2\nZ\fZ\16Z\u05d5\13Z\3Z\3Z\7Z\u05d9\nZ\fZ\16Z\u05dc"+
		"\13Z\3Z\3Z\3Z\5Z\u05e1\nZ\3[\3[\5[\u05e5\n[\3\\\3\\\5\\\u05e9\n\\\3]\3"+
		"]\3]\3^\3^\5^\u05f0\n^\3^\7^\u05f3\n^\f^\16^\u05f6\13^\3^\3^\7^\u05fa"+
		"\n^\f^\16^\u05fd\13^\3^\7^\u0600\n^\f^\16^\u0603\13^\3^\7^\u0606\n^\f"+
		"^\16^\u0609\13^\3^\5^\u060c\n^\3_\3_\3_\7_\u0611\n_\f_\16_\u0614\13_\3"+
		"_\3_\7_\u0618\n_\f_\16_\u061b\13_\3_\5_\u061e\n_\3`\3`\3`\3`\3`\3`\3`"+
		"\7`\u0627\n`\f`\16`\u062a\13`\3`\3`\3`\3`\5`\u0630\n`\3`\3`\3`\3`\3`\3"+
		"`\3`\3`\3`\7`\u063b\n`\f`\16`\u063e\13`\3`\3`\3`\3`\7`\u0644\n`\f`\16"+
		"`\u0647\13`\3`\3`\3`\3`\7`\u064d\n`\f`\16`\u0650\13`\3`\3`\3`\3`\3`\3"+
		"`\5`\u0658\n`\3a\3a\3a\3a\5a\u065e\na\3a\3a\3a\7a\u0663\na\fa\16a\u0666"+
		"\13a\3a\3a\3b\3b\3b\7b\u066d\nb\fb\16b\u0670\13b\3c\3c\7c\u0674\nc\fc"+
		"\16c\u0677\13c\3c\3c\3d\3d\7d\u067d\nd\fd\16d\u0680\13d\3d\3d\5d\u0684"+
		"\nd\3d\3d\3e\3e\3e\3e\7e\u068c\ne\fe\16e\u068f\13e\3f\3f\5f\u0693\nf\3"+
		"g\3g\7g\u0697\ng\fg\16g\u069a\13g\3g\7g\u069d\ng\fg\16g\u06a0\13g\3g\7"+
		"g\u06a3\ng\fg\16g\u06a6\13g\3g\3g\5g\u06aa\ng\3h\3h\3h\3h\3h\3h\5h\u06b2"+
		"\nh\3i\3i\5i\u06b6\ni\3j\3j\3j\5j\u06bb\nj\3j\3j\5j\u06bf\nj\3j\3j\3j"+
		"\3j\3k\5k\u06c6\nk\3k\3k\3l\5l\u06cb\nl\3l\3l\5l\u06cf\nl\3l\3l\5l\u06d3"+
		"\nl\3m\3m\5m\u06d7\nm\3n\3n\3o\3o\3o\3o\3p\3p\3p\7p\u06e2\np\fp\16p\u06e5"+
		"\13p\3p\7p\u06e8\np\fp\16p\u06eb\13p\3q\3q\5q\u06ef\nq\3r\3r\3s\3s\3s"+
		"\3s\3t\3t\3t\7t\u06fa\nt\ft\16t\u06fd\13t\3t\7t\u0700\nt\ft\16t\u0703"+
		"\13t\3u\5u\u0706\nu\3u\3u\3v\3v\5v\u070c\nv\3w\3w\5w\u0710\nw\3x\3x\3"+
		"y\3y\5y\u0716\ny\3z\3z\3z\7z\u071b\nz\fz\16z\u071e\13z\3z\3z\7z\u0722"+
		"\nz\fz\16z\u0725\13z\3z\7z\u0728\nz\fz\16z\u072b\13z\3z\7z\u072e\nz\f"+
		"z\16z\u0731\13z\3z\3z\3{\3{\7{\u0737\n{\f{\16{\u073a\13{\6{\u073c\n{\r"+
		"{\16{\u073d\3{\3{\3|\3|\3|\5|\u0745\n|\3|\3|\3}\3}\3}\3}\3}\3}\7}\u074f"+
		"\n}\f}\16}\u0752\13}\3}\3}\3}\3}\3}\3}\3}\3}\3}\7}\u075d\n}\f}\16}\u0760"+
		"\13}\3}\7}\u0763\n}\f}\16}\u0766\13}\3}\3}\3}\3}\7}\u076c\n}\f}\16}\u076f"+
		"\13}\3}\3}\3}\3}\3}\7}\u0776\n}\f}\16}\u0779\13}\3}\3}\7}\u077d\n}\f}"+
		"\16}\u0780\13}\3}\3}\5}\u0784\n}\3}\3}\3}\7}\u0789\n}\f}\16}\u078c\13"+
		"}\3}\3}\3}\7}\u0791\n}\f}\16}\u0794\13}\3}\3}\7}\u0798\n}\f}\16}\u079b"+
		"\13}\3}\3}\3}\3}\7}\u07a1\n}\f}\16}\u07a4\13}\3}\3}\3}\7}\u07a9\n}\f}"+
		"\16}\u07ac\13}\3}\3}\3}\3}\3}\3}\3}\5}\u07b5\n}\3}\5}\u07b8\n}\3}\7}\u07bb"+
		"\n}\f}\16}\u07be\13}\3}\3}\3}\7}\u07c3\n}\f}\16}\u07c6\13}\3}\3}\7}\u07ca"+
		"\n}\f}\16}\u07cd\13}\3}\3}\3}\7}\u07d2\n}\f}\16}\u07d5\13}\3}\3}\7}\u07d9"+
		"\n}\f}\16}\u07dc\13}\3}\3}\3}\7}\u07e1\n}\f}\16}\u07e4\13}\3}\3}\7}\u07e8"+
		"\n}\f}\16}\u07eb\13}\3}\3}\3}\7}\u07f0\n}\f}\16}\u07f3\13}\3}\3}\7}\u07f7"+
		"\n}\f}\16}\u07fa\13}\3}\3}\3}\7}\u07ff\n}\f}\16}\u0802\13}\3}\3}\7}\u0806"+
		"\n}\f}\16}\u0809\13}\3}\3}\3}\7}\u080e\n}\f}\16}\u0811\13}\3}\3}\7}\u0815"+
		"\n}\f}\16}\u0818\13}\3}\3}\3}\7}\u081d\n}\f}\16}\u0820\13}\3}\3}\7}\u0824"+
		"\n}\f}\16}\u0827\13}\3}\3}\3}\7}\u082c\n}\f}\16}\u082f\13}\3}\3}\7}\u0833"+
		"\n}\f}\16}\u0836\13}\3}\3}\3}\7}\u083b\n}\f}\16}\u083e\13}\3}\3}\7}\u0842"+
		"\n}\f}\16}\u0845\13}\3}\3}\3}\7}\u084a\n}\f}\16}\u084d\13}\3}\3}\7}\u0851"+
		"\n}\f}\16}\u0854\13}\3}\3}\7}\u0858\n}\f}\16}\u085b\13}\3}\3}\7}\u085f"+
		"\n}\f}\16}\u0862\13}\3}\3}\7}\u0866\n}\f}\16}\u0869\13}\5}\u086b\n}\3"+
		"}\3}\3}\7}\u0870\n}\f}\16}\u0873\13}\3}\3}\7}\u0877\n}\f}\16}\u087a\13"+
		"}\3}\3}\3}\7}\u087f\n}\f}\16}\u0882\13}\3}\3}\7}\u0886\n}\f}\16}\u0889"+
		"\13}\3}\3}\3}\7}\u088e\n}\f}\16}\u0891\13}\3}\3}\7}\u0895\n}\f}\16}\u0898"+
		"\13}\3}\3}\3}\7}\u089d\n}\f}\16}\u08a0\13}\3}\3}\7}\u08a4\n}\f}\16}\u08a7"+
		"\13}\3}\7}\u08aa\n}\f}\16}\u08ad\13}\3~\3~\3~\3~\3~\7~\u08b4\n~\f~\16"+
		"~\u08b7\13~\3~\3~\3~\3~\3~\3~\3~\3~\3~\7~\u08c2\n~\f~\16~\u08c5\13~\3"+
		"~\7~\u08c8\n~\f~\16~\u08cb\13~\3~\3~\3~\7~\u08d0\n~\f~\16~\u08d3\13~\3"+
		"~\3~\3~\5~\u08d8\n~\3\177\3\177\3\177\3\177\5\177\u08de\n\177\3\177\7"+
		"\177\u08e1\n\177\f\177\16\177\u08e4\13\177\3\u0080\3\u0080\6\u0080\u08e8"+
		"\n\u0080\r\u0080\16\u0080\u08e9\3\u0080\5\u0080\u08ed\n\u0080\3\u0081"+
		"\3\u0081\3\u0081\5\u0081\u08f2\n\u0081\3\u0081\3\u0081\3\u0081\7\u0081"+
		"\u08f7\n\u0081\f\u0081\16\u0081\u08fa\13\u0081\3\u0082\7\u0082\u08fd\n"+
		"\u0082\f\u0082\16\u0082\u0900\13\u0082\3\u0082\3\u0082\7\u0082\u0904\n"+
		"\u0082\f\u0082\16\u0082\u0907\13\u0082\3\u0082\3\u0082\5\u0082\u090b\n"+
		"\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\5\u0082\u0912\n\u0082\3"+
		"\u0082\3\u0082\3\u0082\5\u0082\u0917\n\u0082\3\u0082\3\u0082\7\u0082\u091b"+
		"\n\u0082\f\u0082\16\u0082\u091e\13\u0082\3\u0082\3\u0082\5\u0082\u0922"+
		"\n\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\7\u0082\u0929\n\u0082"+
		"\f\u0082\16\u0082\u092c\13\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082"+
		"\7\u0082\u0933\n\u0082\f\u0082\16\u0082\u0936\13\u0082\3\u0082\5\u0082"+
		"\u0939\n\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\5\u0082"+
		"\u0941\n\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082"+
		"\3\u0082\3\u0082\5\u0082\u094c\n\u0082\3\u0083\3\u0083\3\u0083\3\u0083"+
		"\5\u0083\u0952\n\u0083\3\u0084\3\u0084\5\u0084\u0956\n\u0084\3\u0085\3"+
		"\u0085\5\u0085\u095a\n\u0085\3\u0085\3\u0085\3\u0086\3\u0086\3\u0086\5"+
		"\u0086\u0961\n\u0086\3\u0086\3\u0086\3\u0087\3\u0087\3\u0087\3\u0087\3"+
		"\u0087\7\u0087\u096a\n\u0087\f\u0087\16\u0087\u096d\13\u0087\3\u0087\7"+
		"\u0087\u0970\n\u0087\f\u0087\16\u0087\u0973\13\u0087\3\u0087\3\u0087\3"+
		"\u0087\3\u0087\5\u0087\u0979\n\u0087\3\u0087\3\u0087\3\u0087\3\u0087\7"+
		"\u0087\u097f\n\u0087\f\u0087\16\u0087\u0982\13\u0087\3\u0087\5\u0087\u0985"+
		"\n\u0087\3\u0087\3\u0087\5\u0087\u0989\n\u0087\3\u0087\3\u0087\5\u0087"+
		"\u098d\n\u0087\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087\5\u0087"+
		"\u0995\n\u0087\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\5\u0088"+
		"\u099d\n\u0088\3\u0089\3\u0089\3\u0089\5\u0089\u09a2\n\u0089\3\u008a\3"+
		"\u008a\3\u008a\5\u008a\u09a7\n\u008a\3\u008b\3\u008b\5\u008b\u09ab\n\u008b"+
		"\3\u008b\5\u008b\u09ae\n\u008b\3\u008b\3\u008b\3\u008c\3\u008c\3\u008c"+
		"\5\u008c\u09b5\n\u008c\3\u008c\5\u008c\u09b8\n\u008c\3\u008c\3\u008c\3"+
		"\u008d\3\u008d\3\u008d\7\u008d\u09bf\n\u008d\f\u008d\16\u008d\u09c2\13"+
		"\u008d\3\u008e\3\u008e\3\u008e\7\u008e\u09c7\n\u008e\f\u008e\16\u008e"+
		"\u09ca\13\u008e\3\u008f\3\u008f\3\u008f\7\u008f\u09cf\n\u008f\f\u008f"+
		"\16\u008f\u09d2\13\u008f\3\u008f\3\u008f\3\u008f\3\u008f\3\u008f\7\u008f"+
		"\u09d9\n\u008f\f\u008f\16\u008f\u09dc\13\u008f\3\u008f\5\u008f\u09df\n"+
		"\u008f\3\u0090\3\u0090\3\u0090\7\u0090\u09e4\n\u0090\f\u0090\16\u0090"+
		"\u09e7\13\u0090\3\u0090\3\u0090\3\u0090\3\u0090\3\u0090\7\u0090\u09ee"+
		"\n\u0090\f\u0090\16\u0090\u09f1\13\u0090\3\u0090\5\u0090\u09f4\n\u0090"+
		"\3\u0091\3\u0091\3\u0091\7\u0091\u09f9\n\u0091\f\u0091\16\u0091\u09fc"+
		"\13\u0091\3\u0091\3\u0091\3\u0091\3\u0091\3\u0091\7\u0091\u0a03\n\u0091"+
		"\f\u0091\16\u0091\u0a06\13\u0091\3\u0091\5\u0091\u0a09\n\u0091\3\u0092"+
		"\3\u0092\5\u0092\u0a0d\n\u0092\3\u0093\3\u0093\5\u0093\u0a11\n\u0093\3"+
		"\u0094\3\u0094\5\u0094\u0a15\n\u0094\3\u0095\3\u0095\7\u0095\u0a19\n\u0095"+
		"\f\u0095\16\u0095\u0a1c\13\u0095\3\u0095\3\u0095\5\u0095\u0a20\n\u0095"+
		"\3\u0095\6\u0095\u0a23\n\u0095\r\u0095\16\u0095\u0a24\3\u0095\7\u0095"+
		"\u0a28\n\u0095\f\u0095\16\u0095\u0a2b\13\u0095\3\u0095\3\u0095\3\u0095"+
		"\6\u0095\u0a30\n\u0095\r\u0095\16\u0095\u0a31\3\u0095\7\u0095\u0a35\n"+
		"\u0095\f\u0095\16\u0095\u0a38\13\u0095\5\u0095\u0a3a\n\u0095\3\u0096\3"+
		"\u0096\3\u0096\3\u0096\3\u0097\3\u0097\3\u0097\3\u0097\3\u0097\3\u0098"+
		"\3\u0098\7\u0098\u0a47\n\u0098\f\u0098\16\u0098\u0a4a\13\u0098\3\u0098"+
		"\3\u0098\5\u0098\u0a4e\n\u0098\3\u0098\7\u0098\u0a51\n\u0098\f\u0098\16"+
		"\u0098\u0a54\13\u0098\3\u0098\3\u0098\7\u0098\u0a58\n\u0098\f\u0098\16"+
		"\u0098\u0a5b\13\u0098\3\u0098\3\u0098\5\u0098\u0a5f\n\u0098\3\u0098\7"+
		"\u0098\u0a62\n\u0098\f\u0098\16\u0098\u0a65\13\u0098\7\u0098\u0a67\n\u0098"+
		"\f\u0098\16\u0098\u0a6a\13\u0098\5\u0098\u0a6c\n\u0098\3\u0098\5\u0098"+
		"\u0a6f\n\u0098\3\u0098\7\u0098\u0a72\n\u0098\f\u0098\16\u0098\u0a75\13"+
		"\u0098\3\u0098\3\u0098\3\u0099\3\u0099\3\u009a\3\u009a\3\u009a\3\u009a"+
		"\3\u009a\3\u009a\3\u009a\5\u009a\u0a82\n\u009a\7\u009a\u0a84\n\u009a\f"+
		"\u009a\16\u009a\u0a87\13\u009a\5\u009a\u0a89\n\u009a\5\u009a\u0a8b\n\u009a"+
		"\3\u009b\3\u009b\7\u009b\u0a8f\n\u009b\f\u009b\16\u009b\u0a92\13\u009b"+
		"\3\u009b\3\u009b\7\u009b\u0a96\n\u009b\f\u009b\16\u009b\u0a99\13\u009b"+
		"\3\u009b\3\u009b\3\u009c\3\u009c\3\u009c\5\u009c\u0aa0\n\u009c\3\u009d"+
		"\3\u009d\5\u009d\u0aa4\n\u009d\3\u009d\5\u009d\u0aa7\n\u009d\3\u009d\3"+
		"\u009d\3\u009e\3\u009e\3\u009e\7\u009e\u0aae\n\u009e\f\u009e\16\u009e"+
		"\u0ab1\13\u009e\3\u009e\7\u009e\u0ab4\n\u009e\f\u009e\16\u009e\u0ab7\13"+
		"\u009e\3\u009f\3\u009f\3\u009f\7\u009f\u0abc\n\u009f\f\u009f\16\u009f"+
		"\u0abf\13\u009f\3\u009f\7\u009f\u0ac2\n\u009f\f\u009f\16\u009f\u0ac5\13"+
		"\u009f\3\u00a0\3\u00a0\5\u00a0\u0ac9\n\u00a0\3\u00a1\3\u00a1\5\u00a1\u0acd"+
		"\n\u00a1\3\u00a2\3\u00a2\3\u00a2\5\u00a2\u0ad2\n\u00a2\3\u00a3\3\u00a3"+
		"\3\u00a4\3\u00a4\3\u00a5\3\u00a5\3\u00a6\3\u00a6\3\u00a7\3\u00a7\3\u00a8"+
		"\6\u00a8\u0adf\n\u00a8\r\u00a8\16\u00a8\u0ae0\3\u00a8\2\2\3\u00f8\u00a9"+
		"\2\2\4\2\6\2\b\2\n\2\f\2\16\2\20\2\22\2\24\2\26\2\30\2\32\2\34\2\36\2"+
		" \2\"\2$\2&\2(\2*\2,\2.\2\60\2\62\2\64\2\66\28\2:\2<\2>\2@\2B\2D\2F\2"+
		"H\2J\2L\2N\2P\2R\2T\2V\2X\2Z\2\\\2^\2`\2b\2d\2f\2h\2j\2l\2n\2p\2r\2t\2"+
		"v\2x\2z\2|\2~\2\u0080\2\u0082\2\u0084\2\u0086\2\u0088\2\u008a\2\u008c"+
		"\2\u008e\2\u0090\2\u0092\2\u0094\2\u0096\2\u0098\2\u009a\2\u009c\2\u009e"+
		"\2\u00a0\2\u00a2\2\u00a4\2\u00a6\2\u00a8\2\u00aa\2\u00ac\2\u00ae\2\u00b0"+
		"\2\u00b2\2\u00b4\2\u00b6\2\u00b8\2\u00ba\2\u00bc\2\u00be\2\u00c0\2\u00c2"+
		"\2\u00c4\2\u00c6\2\u00c8\2\u00ca\2\u00cc\2\u00ce\2\u00d0\2\u00d2\2\u00d4"+
		"\2\u00d6\2\u00d8\2\u00da\2\u00dc\2\u00de\2\u00e0\2\u00e2\2\u00e4\2\u00e6"+
		"\2\u00e8\2\u00ea\2\u00ec\2\u00ee\2\u00f0\2\u00f2\2\u00f4\2\u00f6\2\u00f8"+
		"\2\u00fa\2\u00fc\2\u00fe\2\u0100\2\u0102\2\u0104\2\u0106\2\u0108\2\u010a"+
		"\2\u010c\2\u010e\2\u0110\2\u0112\2\u0114\2\u0116\2\u0118\2\u011a\2\u011c"+
		"\2\u011e\2\u0120\2\u0122\2\u0124\2\u0126\2\u0128\2\u012a\2\u012c\2\u012e"+
		"\2\u0130\2\u0132\2\u0134\2\u0136\2\u0138\2\u013a\2\u013c\2\u013e\2\u0140"+
		"\2\u0142\2\u0144\2\u0146\2\u0148\2\u014a\2\u014c\2\u014e\2\2\32\b\2\n"+
		"\n))88<<>?AA\b\2\22\22\32\32\37\37++.\60\63\65\b\2\n\n\22\22\37\37.\60"+
		"\64\65>?\4\2\36\36\66\66\4\2eemm\4\2\13\13mm\5\2\n\n\21\21>?\3\2tu\4\2"+
		"[[mm\3\2jk\3\2tw\4\2xy}}\3\2vw\3\2HK\6\2\13\13]]hiop\6\2WXZZnnqq\3\2S"+
		"T\5\2VVgg~\u0089\4\2LMOO\4\2NNbb\f\2\t\t\13\f\16\20((--\61\61\63\63>?"+
		"CC\u008a\u008b\4\2\21\21@@\5\2\t\')CFG\4\2dd\u008f\u008f\2\u0c20\2\u0153"+
		"\3\2\2\2\4\u0161\3\2\2\2\6\u0172\3\2\2\2\b\u0174\3\2\2\2\n\u0178\3\2\2"+
		"\2\f\u0188\3\2\2\2\16\u018d\3\2\2\2\20\u0196\3\2\2\2\22\u0198\3\2\2\2"+
		"\24\u01ac\3\2\2\2\26\u01ae\3\2\2\2\30\u01bd\3\2\2\2\32\u01c1\3\2\2\2\34"+
		"\u01ca\3\2\2\2\36\u01cc\3\2\2\2 \u01d9\3\2\2\2\"\u01f6\3\2\2\2$\u0202"+
		"\3\2\2\2&\u0210\3\2\2\2(\u022b\3\2\2\2*\u0278\3\2\2\2,\u02bf\3\2\2\2."+
		"\u02d3\3\2\2\2\60\u02e6\3\2\2\2\62\u02ef\3\2\2\2\64\u02f1\3\2\2\2\66\u0329"+
		"\3\2\2\28\u0334\3\2\2\2:\u0338\3\2\2\2<\u033a\3\2\2\2>\u033c\3\2\2\2@"+
		"\u034a\3\2\2\2B\u0361\3\2\2\2D\u0363\3\2\2\2F\u0365\3\2\2\2H\u0371\3\2"+
		"\2\2J\u0373\3\2\2\2L\u0381\3\2\2\2N\u0387\3\2\2\2P\u0389\3\2\2\2R\u0394"+
		"\3\2\2\2T\u03a2\3\2\2\2V\u03cc\3\2\2\2X\u03ce\3\2\2\2Z\u03d1\3\2\2\2\\"+
		"\u03df\3\2\2\2^\u03e7\3\2\2\2`\u03f6\3\2\2\2b\u03f9\3\2\2\2d\u0411\3\2"+
		"\2\2f\u0413\3\2\2\2h\u0420\3\2\2\2j\u0427\3\2\2\2l\u042a\3\2\2\2n\u042d"+
		"\3\2\2\2p\u043b\3\2\2\2r\u043d\3\2\2\2t\u044a\3\2\2\2v\u044c\3\2\2\2x"+
		"\u0453\3\2\2\2z\u0463\3\2\2\2|\u0473\3\2\2\2~\u0477\3\2\2\2\u0080\u047b"+
		"\3\2\2\2\u0082\u047d\3\2\2\2\u0084\u0498\3\2\2\2\u0086\u049b\3\2\2\2\u0088"+
		"\u049d\3\2\2\2\u008a\u04bc\3\2\2\2\u008c\u04be\3\2\2\2\u008e\u04d0\3\2"+
		"\2\2\u0090\u04d2\3\2\2\2\u0092\u04d4\3\2\2\2\u0094\u04dc\3\2\2\2\u0096"+
		"\u04ee\3\2\2\2\u0098\u04f3\3\2\2\2\u009a\u0511\3\2\2\2\u009c\u0513\3\2"+
		"\2\2\u009e\u051a\3\2\2\2\u00a0\u051c\3\2\2\2\u00a2\u053f\3\2\2\2\u00a4"+
		"\u0541\3\2\2\2\u00a6\u055a\3\2\2\2\u00a8\u0561\3\2\2\2\u00aa\u056b\3\2"+
		"\2\2\u00ac\u0577\3\2\2\2\u00ae\u0579\3\2\2\2\u00b0\u0595\3\2\2\2\u00b2"+
		"\u05e0\3\2\2\2\u00b4\u05e2\3\2\2\2\u00b6\u05e6\3\2\2\2\u00b8\u05ea\3\2"+
		"\2\2\u00ba\u05ed\3\2\2\2\u00bc\u060d\3\2\2\2\u00be\u0657\3\2\2\2\u00c0"+
		"\u0659\3\2\2\2\u00c2\u0669\3\2\2\2\u00c4\u0671\3\2\2\2\u00c6\u067a\3\2"+
		"\2\2\u00c8\u0687\3\2\2\2\u00ca\u0692\3\2\2\2\u00cc\u0694\3\2\2\2\u00ce"+
		"\u06b1\3\2\2\2\u00d0\u06b5\3\2\2\2\u00d2\u06ba\3\2\2\2\u00d4\u06c5\3\2"+
		"\2\2\u00d6\u06ca\3\2\2\2\u00d8\u06d6\3\2\2\2\u00da\u06d8\3\2\2\2\u00dc"+
		"\u06da\3\2\2\2\u00de\u06de\3\2\2\2\u00e0\u06ee\3\2\2\2\u00e2\u06f0\3\2"+
		"\2\2\u00e4\u06f2\3\2\2\2\u00e6\u06f6\3\2\2\2\u00e8\u0705\3\2\2\2\u00ea"+
		"\u070b\3\2\2\2\u00ec\u070f\3\2\2\2\u00ee\u0711\3\2\2\2\u00f0\u0713\3\2"+
		"\2\2\u00f2\u0717\3\2\2\2\u00f4\u073b\3\2\2\2\u00f6\u0744\3\2\2\2\u00f8"+
		"\u0783\3\2\2\2\u00fa\u08d7\3\2\2\2\u00fc\u08d9\3\2\2\2\u00fe\u08e5\3\2"+
		"\2\2\u0100\u08f1\3\2\2\2\u0102\u094b\3\2\2\2\u0104\u0951\3\2\2\2\u0106"+
		"\u0955\3\2\2\2\u0108\u0957\3\2\2\2\u010a\u095d\3\2\2\2\u010c\u0994\3\2"+
		"\2\2\u010e\u099c\3\2\2\2\u0110\u09a1\3\2\2\2\u0112\u09a6\3\2\2\2\u0114"+
		"\u09a8\3\2\2\2\u0116\u09b1\3\2\2\2\u0118\u09bb\3\2\2\2\u011a\u09c3\3\2"+
		"\2\2\u011c\u09de\3\2\2\2\u011e\u09f3\3\2\2\2\u0120\u0a08\3\2\2\2\u0122"+
		"\u0a0c\3\2\2\2\u0124\u0a10\3\2\2\2\u0126\u0a14\3\2\2\2\u0128\u0a16\3\2"+
		"\2\2\u012a\u0a3b\3\2\2\2\u012c\u0a3f\3\2\2\2\u012e\u0a44\3\2\2\2\u0130"+
		"\u0a78\3\2\2\2\u0132\u0a7a\3\2\2\2\u0134\u0a8c\3\2\2\2\u0136\u0a9f\3\2"+
		"\2\2\u0138\u0aa1\3\2\2\2\u013a\u0aaa\3\2\2\2\u013c\u0ab8\3\2\2\2\u013e"+
		"\u0ac8\3\2\2\2\u0140\u0acc\3\2\2\2\u0142\u0ad1\3\2\2\2\u0144\u0ad3\3\2"+
		"\2\2\u0146\u0ad5\3\2\2\2\u0148\u0ad7\3\2\2\2\u014a\u0ad9\3\2\2\2\u014c"+
		"\u0adb\3\2\2\2\u014e\u0ade\3\2\2\2\u0150\u0152\7\u008f\2\2\u0151\u0150"+
		"\3\2\2\2\u0152\u0155\3\2\2\2\u0153\u0151\3\2\2\2\u0153\u0154\3\2\2\2\u0154"+
		"\u015a\3\2\2\2\u0155\u0153\3\2\2\2\u0156\u0158\5\b\5\2\u0157\u0159\5\u014e"+
		"\u00a8\2\u0158\u0157\3\2\2\2\u0158\u0159\3\2\2\2\u0159\u015b\3\2\2\2\u015a"+
		"\u0156\3\2\2\2\u015a\u015b\3\2\2\2\u015b\u015d\3\2\2\2\u015c\u015e\5\4"+
		"\3\2\u015d\u015c\3\2\2\2\u015d\u015e\3\2\2\2\u015e\u015f\3\2\2\2\u015f"+
		"\u0160\7\2\2\3\u0160\3\3\2\2\2\u0161\u0167\5\6\4\2\u0162\u0163\5\u014e"+
		"\u00a8\2\u0163\u0164\5\6\4\2\u0164\u0166\3\2\2\2\u0165\u0162\3\2\2\2\u0166"+
		"\u0169\3\2\2\2\u0167\u0165\3\2\2\2\u0167\u0168\3\2\2\2\u0168\u016b\3\2"+
		"\2\2\u0169\u0167\3\2\2\2\u016a\u016c\5\u014e\u00a8\2\u016b\u016a\3\2\2"+
		"\2\u016b\u016c\3\2\2\2\u016c\5\3\2\2\2\u016d\u0173\5\n\6\2\u016e\u0173"+
		"\5\f\7\2\u016f\u0170\6\4\2\2\u0170\u0173\5\64\33\2\u0171\u0173\5\u00be"+
		"`\2\u0172\u016d\3\2\2\2\u0172\u016e\3\2\2\2\u0172\u016f\3\2\2\2\u0172"+
		"\u0171\3\2\2\2\u0173\7\3\2\2\2\u0174\u0175\5\u008aF\2\u0175\u0176\7,\2"+
		"\2\u0176\u0177\5f\64\2\u0177\t\3\2\2\2\u0178\u0179\5\u008aF\2\u0179\u0186"+
		"\7%\2\2\u017a\u017b\7(\2\2\u017b\u0187\5f\64\2\u017c\u017e\7\64\2\2\u017d"+
		"\u017c\3\2\2\2\u017d\u017e\3\2\2\2\u017e\u017f\3\2\2\2\u017f\u0184\5f"+
		"\64\2\u0180\u0181\7f\2\2\u0181\u0185\7x\2\2\u0182\u0183\7\t\2\2\u0183"+
		"\u0185\5\u0148\u00a5\2\u0184\u0180\3\2\2\2\u0184\u0182\3\2\2\2\u0184\u0185"+
		"\3\2\2\2\u0185\u0187\3\2\2\2\u0186\u017a\3\2\2\2\u0186\u017d\3\2\2\2\u0187"+
		"\13\3\2\2\2\u0188\u0189\5\24\13\2\u0189\u018a\5(\25\2\u018a\r\3\2\2\2"+
		"\u018b\u018e\5\30\r\2\u018c\u018e\t\2\2\2\u018d\u018b\3\2\2\2\u018d\u018c"+
		"\3\2\2\2\u018e\17\3\2\2\2\u018f\u0193\5\22\n\2\u0190\u0192\7\u008f\2\2"+
		"\u0191\u0190\3\2\2\2\u0192\u0195\3\2\2\2\u0193\u0191\3\2\2\2\u0193\u0194"+
		"\3\2\2\2\u0194\u0197\3\2\2\2\u0195\u0193\3\2\2\2\u0196\u018f\3\2\2\2\u0196"+
		"\u0197\3\2\2\2\u0197\21\3\2\2\2\u0198\u01a2\5\16\b\2\u0199\u019b\7\u008f"+
		"\2\2\u019a\u0199\3\2\2\2\u019b\u019e\3\2\2\2\u019c\u019a\3\2\2\2\u019c"+
		"\u019d\3\2\2\2\u019d\u019f\3\2\2\2\u019e\u019c\3\2\2\2\u019f\u01a1\5\16"+
		"\b\2\u01a0\u019c\3\2\2\2\u01a1\u01a4\3\2\2\2\u01a2\u01a0\3\2\2\2\u01a2"+
		"\u01a3\3\2\2\2\u01a3\23\3\2\2\2\u01a4\u01a2\3\2\2\2\u01a5\u01a9\5\26\f"+
		"\2\u01a6\u01a8\7\u008f\2\2\u01a7\u01a6\3\2\2\2\u01a8\u01ab\3\2\2\2\u01a9"+
		"\u01a7\3\2\2\2\u01a9\u01aa\3\2\2\2\u01aa\u01ad\3\2\2\2\u01ab\u01a9\3\2"+
		"\2\2\u01ac\u01a5\3\2\2\2\u01ac\u01ad\3\2\2\2\u01ad\25\3\2\2\2\u01ae\u01b8"+
		"\5\30\r\2\u01af\u01b1\7\u008f\2\2\u01b0\u01af\3\2\2\2\u01b1\u01b4\3\2"+
		"\2\2\u01b2\u01b0\3\2\2\2\u01b2\u01b3\3\2\2\2\u01b3\u01b5\3\2\2\2\u01b4"+
		"\u01b2\3\2\2\2\u01b5\u01b7\5\30\r\2\u01b6\u01b2\3\2\2\2\u01b7\u01ba\3"+
		"\2\2\2\u01b8\u01b6\3\2\2\2\u01b8\u01b9\3\2\2\2\u01b9\27\3\2\2\2\u01ba"+
		"\u01b8\3\2\2\2\u01bb\u01be\5\u008cG\2\u01bc\u01be\t\3\2\2\u01bd\u01bb"+
		"\3\2\2\2\u01bd\u01bc\3\2\2\2\u01be\31\3\2\2\2\u01bf\u01c2\5\u008cG\2\u01c0"+
		"\u01c2\t\4\2\2\u01c1\u01bf\3\2\2\2\u01c1\u01c0\3\2\2\2\u01c2\33\3\2\2"+
		"\2\u01c3\u01c7\5\36\20\2\u01c4\u01c6\7\u008f\2\2\u01c5\u01c4\3\2\2\2\u01c6"+
		"\u01c9\3\2\2\2\u01c7\u01c5\3\2\2\2\u01c7\u01c8\3\2\2\2\u01c8\u01cb\3\2"+
		"\2\2\u01c9\u01c7\3\2\2\2\u01ca\u01c3\3\2\2\2\u01ca\u01cb\3\2\2\2\u01cb"+
		"\35\3\2\2\2\u01cc\u01d6\5\32\16\2\u01cd\u01cf\7\u008f\2\2\u01ce\u01cd"+
		"\3\2\2\2\u01cf\u01d2\3\2\2\2\u01d0\u01ce\3\2\2\2\u01d0\u01d1\3\2\2\2\u01d1"+
		"\u01d3\3\2\2\2\u01d2\u01d0\3\2\2\2\u01d3\u01d5\5\32\16\2\u01d4\u01d0\3"+
		"\2\2\2\u01d5\u01d8\3\2\2\2\u01d6\u01d4\3\2\2\2\u01d6\u01d7\3\2\2\2\u01d7"+
		"\37\3\2\2\2\u01d8\u01d6\3\2\2\2\u01d9\u01dd\7i\2\2\u01da\u01dc\7\u008f"+
		"\2\2\u01db\u01da\3\2\2\2\u01dc\u01df\3\2\2\2\u01dd\u01db\3\2\2\2\u01dd"+
		"\u01de\3\2\2\2\u01de\u01e0\3\2\2\2\u01df\u01dd\3\2\2\2\u01e0\u01eb\5\""+
		"\22\2\u01e1\u01e5\7e\2\2\u01e2\u01e4\7\u008f\2\2\u01e3\u01e2\3\2\2\2\u01e4"+
		"\u01e7\3\2\2\2\u01e5\u01e3\3\2\2\2\u01e5\u01e6\3\2\2\2\u01e6\u01e8\3\2"+
		"\2\2\u01e7\u01e5\3\2\2\2\u01e8\u01ea\5\"\22\2\u01e9\u01e1\3\2\2\2\u01ea"+
		"\u01ed\3\2\2\2\u01eb\u01e9\3\2\2\2\u01eb\u01ec\3\2\2\2\u01ec\u01f1\3\2"+
		"\2\2\u01ed\u01eb\3\2\2\2\u01ee\u01f0\7\u008f\2\2\u01ef\u01ee\3\2\2\2\u01f0"+
		"\u01f3\3\2\2\2\u01f1\u01ef\3\2\2\2\u01f1\u01f2\3\2\2\2\u01f2\u01f4\3\2"+
		"\2\2\u01f3\u01f1\3\2\2\2\u01f4\u01f5\7h\2\2\u01f5!\3\2\2\2\u01f6\u01f7"+
		"\5\u008aF\2\u01f7\u0200\5\u0146\u00a4\2\u01f8\u01fc\7\36\2\2\u01f9\u01fb"+
		"\7\u008f\2\2\u01fa\u01f9\3\2\2\2\u01fb\u01fe\3\2\2\2\u01fc\u01fa\3\2\2"+
		"\2\u01fc\u01fd\3\2\2\2\u01fd\u01ff\3\2\2\2\u01fe\u01fc\3\2\2\2\u01ff\u0201"+
		"\5$\23\2\u0200\u01f8\3\2\2\2\u0200\u0201\3\2\2\2\u0201#\3\2\2\2\u0202"+
		"\u020d\5F$\2\u0203\u0207\7z\2\2\u0204\u0206\7\u008f\2\2\u0205\u0204\3"+
		"\2\2\2\u0206\u0209\3\2\2\2\u0207\u0205\3\2\2\2\u0207\u0208\3\2\2\2\u0208"+
		"\u020a\3\2\2\2\u0209\u0207\3\2\2\2\u020a\u020c\5F$\2\u020b\u0203\3\2\2"+
		"\2\u020c\u020f\3\2\2\2\u020d\u020b\3\2\2\2\u020d\u020e\3\2\2\2\u020e%"+
		"\3\2\2\2\u020f\u020d\3\2\2\2\u0210\u021b\5F$\2\u0211\u0215\7e\2\2\u0212"+
		"\u0214\7\u008f\2\2\u0213\u0212\3\2\2\2\u0214\u0217\3\2\2\2\u0215\u0213"+
		"\3\2\2\2\u0215\u0216\3\2\2\2\u0216\u0218\3\2\2\2\u0217\u0215\3\2\2\2\u0218"+
		"\u021a\5F$\2\u0219\u0211\3\2\2\2\u021a\u021d\3\2\2\2\u021b\u0219\3\2\2"+
		"\2\u021b\u021c\3\2\2\2\u021c\'\3\2\2\2\u021d\u021b\3\2\2\2\u021e\u021f"+
		"\7\27\2\2\u021f\u022c\b\25\1\2\u0220\u0221\7\'\2\2\u0221\u022c\b\25\1"+
		"\2\u0222\u0223\7\35\2\2\u0223\u022c\b\25\1\2\u0224\u0225\7\u008c\2\2\u0225"+
		"\u0226\7\'\2\2\u0226\u022c\b\25\1\2\u0227\u0228\7\f\2\2\u0228\u022c\b"+
		"\25\1\2\u0229\u022a\7\61\2\2\u022a\u022c\b\25\1\2\u022b\u021e\3\2\2\2"+
		"\u022b\u0220\3\2\2\2\u022b\u0222\3\2\2\2\u022b\u0224\3\2\2\2\u022b\u0227"+
		"\3\2\2\2\u022b\u0229\3\2\2\2\u022c\u022d\3\2\2\2\u022d\u0235\5\u0148\u00a5"+
		"\2\u022e\u0230\7\u008f\2\2\u022f\u022e\3\2\2\2\u0230\u0233\3\2\2\2\u0231"+
		"\u022f\3\2\2\2\u0231\u0232\3\2\2\2\u0232\u0234\3\2\2\2\u0233\u0231\3\2"+
		"\2\2\u0234\u0236\5 \21\2\u0235\u0231\3\2\2\2\u0235\u0236\3\2\2\2\u0236"+
		"\u023e\3\2\2\2\u0237\u0239\7\u008f\2\2\u0238\u0237\3\2\2\2\u0239\u023c"+
		"\3\2\2\2\u023a\u0238\3\2\2\2\u023a\u023b\3\2\2\2\u023b\u023d\3\2\2\2\u023c"+
		"\u023a\3\2\2\2\u023d\u023f\5\\/\2\u023e\u023a\3\2\2\2\u023e\u023f\3\2"+
		"\2\2\u023f\u024e\3\2\2\2\u0240\u0242\7\u008f\2\2\u0241\u0240\3\2\2\2\u0242"+
		"\u0245\3\2\2\2\u0243\u0241\3\2\2\2\u0243\u0244\3\2\2\2\u0244\u0246\3\2"+
		"\2\2\u0245\u0243\3\2\2\2\u0246\u024a\7\36\2\2\u0247\u0249\7\u008f\2\2"+
		"\u0248\u0247\3\2\2\2\u0249\u024c\3\2\2\2\u024a\u0248\3\2\2\2\u024a\u024b"+
		"\3\2\2\2\u024b\u024d\3\2\2\2\u024c\u024a\3\2\2\2\u024d\u024f\5&\24\2\u024e"+
		"\u0243\3\2\2\2\u024e\u024f\3\2\2\2\u024f\u025e\3\2\2\2\u0250\u0252\7\u008f"+
		"\2\2\u0251\u0250\3\2\2\2\u0252\u0255\3\2\2\2\u0253\u0251\3\2\2\2\u0253"+
		"\u0254\3\2\2\2\u0254\u0256\3\2\2\2\u0255\u0253\3\2\2\2\u0256\u025a\7$"+
		"\2\2\u0257\u0259\7\u008f\2\2\u0258\u0257\3\2\2\2\u0259\u025c\3\2\2\2\u025a"+
		"\u0258\3\2\2\2\u025a\u025b\3\2\2\2\u025b\u025d\3\2\2\2\u025c\u025a\3\2"+
		"\2\2\u025d\u025f\5&\24\2\u025e\u0253\3\2\2\2\u025e\u025f\3\2\2\2\u025f"+
		"\u026e\3\2\2\2\u0260\u0262\7\u008f\2\2\u0261\u0260\3\2\2\2\u0262\u0265"+
		"\3\2\2\2\u0263\u0261\3\2\2\2\u0263\u0264\3\2\2\2\u0264\u0266\3\2\2\2\u0265"+
		"\u0263\3\2\2\2\u0266\u026a\7-\2\2\u0267\u0269\7\u008f\2\2\u0268\u0267"+
		"\3\2\2\2\u0269\u026c\3\2\2\2\u026a\u0268\3\2\2\2\u026a\u026b\3\2\2\2\u026b"+
		"\u026d\3\2\2\2\u026c\u026a\3\2\2\2\u026d\u026f\5&\24\2\u026e\u0263\3\2"+
		"\2\2\u026e\u026f\3\2\2\2\u026f\u0273\3\2\2\2\u0270\u0272\7\u008f\2\2\u0271"+
		"\u0270\3\2\2\2\u0272\u0275\3\2\2\2\u0273\u0271\3\2\2\2\u0273\u0274\3\2"+
		"\2\2\u0274\u0276\3\2\2\2\u0275\u0273\3\2\2\2\u0276\u0277\5*\26\2\u0277"+
		")\3\2\2\2\u0278\u027c\7`\2\2\u0279\u027b\7\u008f\2\2\u027a\u0279\3\2\2"+
		"\2\u027b\u027e\3\2\2\2\u027c\u027a\3\2\2\2\u027c\u027d\3\2\2\2\u027d\u02b8"+
		"\3\2\2\2\u027e\u027c\3\2\2\2\u027f\u0280\6\26\3\3\u0280\u02ab\5,\27\2"+
		"\u0281\u0283\7\u008f\2\2\u0282\u0281\3\2\2\2\u0283\u0286\3\2\2\2\u0284"+
		"\u0282\3\2\2\2\u0284\u0285\3\2\2\2\u0285\u0287\3\2\2\2\u0286\u0284\3\2"+
		"\2\2\u0287\u0289\7e\2\2\u0288\u0284\3\2\2\2\u0288\u0289\3\2\2\2\u0289"+
		"\u02ac\3\2\2\2\u028a\u028c\7\u008f\2\2\u028b\u028a\3\2\2\2\u028c\u028f"+
		"\3\2\2\2\u028d\u028b\3\2\2\2\u028d\u028e\3\2\2\2\u028e\u0290\3\2\2\2\u028f"+
		"\u028d\3\2\2\2\u0290\u0292\7e\2\2\u0291\u028d\3\2\2\2\u0291\u0292\3\2"+
		"\2\2\u0292\u0296\3\2\2\2\u0293\u0295\7\u008f\2\2\u0294\u0293\3\2\2\2\u0295"+
		"\u0298\3\2\2\2\u0296\u0294\3\2\2\2\u0296\u0297\3\2\2\2\u0297\u0299\3\2"+
		"\2\2\u0298\u0296\3\2\2\2\u0299\u029b\7d\2\2\u029a\u0291\3\2\2\2\u029a"+
		"\u029b\3\2\2\2\u029b\u029f\3\2\2\2\u029c\u029e\7\u008f\2\2\u029d\u029c"+
		"\3\2\2\2\u029e\u02a1\3\2\2\2\u029f\u029d\3\2\2\2\u029f\u02a0\3\2\2\2\u02a0"+
		"\u02a2\3\2\2\2\u02a1\u029f\3\2\2\2\u02a2\u02a8\5\60\31\2\u02a3\u02a4\5"+
		"\u014e\u00a8\2\u02a4\u02a5\5\60\31\2\u02a5\u02a7\3\2\2\2\u02a6\u02a3\3"+
		"\2\2\2\u02a7\u02aa\3\2\2\2\u02a8\u02a6\3\2\2\2\u02a8\u02a9\3\2\2\2\u02a9"+
		"\u02ac\3\2\2\2\u02aa\u02a8\3\2\2\2\u02ab\u0288\3\2\2\2\u02ab\u029a\3\2"+
		"\2\2\u02ac\u02b9\3\2\2\2\u02ad\u02b3\5\60\31\2\u02ae\u02af\5\u014e\u00a8"+
		"\2\u02af\u02b0\5\60\31\2\u02b0\u02b2\3\2\2\2\u02b1\u02ae\3\2\2\2\u02b2"+
		"\u02b5\3\2\2\2\u02b3\u02b1\3\2\2\2\u02b3\u02b4\3\2\2\2\u02b4\u02b7\3\2"+
		"\2\2\u02b5\u02b3\3\2\2\2\u02b6\u02ad\3\2\2\2\u02b6\u02b7\3\2\2\2\u02b7"+
		"\u02b9\3\2\2\2\u02b8\u027f\3\2\2\2\u02b8\u02b6\3\2\2\2\u02b9\u02bb\3\2"+
		"\2\2\u02ba\u02bc\5\u014e\u00a8\2\u02bb\u02ba\3\2\2\2\u02bb\u02bc\3\2\2"+
		"\2\u02bc\u02bd\3\2\2\2\u02bd\u02be\7a\2\2\u02be+\3\2\2\2\u02bf\u02d0\5"+
		".\30\2\u02c0\u02c2\7\u008f\2\2\u02c1\u02c0\3\2\2\2\u02c2\u02c5\3\2\2\2"+
		"\u02c3\u02c1\3\2\2\2\u02c3\u02c4\3\2\2\2\u02c4\u02c6\3\2\2\2\u02c5\u02c3"+
		"\3\2\2\2\u02c6\u02ca\7e\2\2\u02c7\u02c9\7\u008f\2\2\u02c8\u02c7\3\2\2"+
		"\2\u02c9\u02cc\3\2\2\2\u02ca\u02c8\3\2\2\2\u02ca\u02cb\3\2\2\2\u02cb\u02cd"+
		"\3\2\2\2\u02cc\u02ca\3\2\2\2\u02cd\u02cf\5.\30\2\u02ce\u02c3\3\2\2\2\u02cf"+
		"\u02d2\3\2\2\2\u02d0\u02ce\3\2\2\2\u02d0\u02d1\3\2\2\2\u02d1-\3\2\2\2"+
		"\u02d2\u02d0\3\2\2\2\u02d3\u02d4\5\u008aF\2\u02d4\u02d6\5\u0148\u00a5"+
		"\2\u02d5\u02d7\5\u0138\u009d\2\u02d6\u02d5\3\2\2\2\u02d6\u02d7\3\2\2\2"+
		"\u02d7\u02d9\3\2\2\2\u02d8\u02da\5\u0130\u0099\2\u02d9\u02d8\3\2\2\2\u02d9"+
		"\u02da\3\2\2\2\u02da/\3\2\2\2\u02db\u02df\7\64\2\2\u02dc\u02de\7\u008f"+
		"\2\2\u02dd\u02dc\3\2\2\2\u02de\u02e1\3\2\2\2\u02df\u02dd\3\2\2\2\u02df"+
		"\u02e0\3\2\2\2\u02e0\u02e3\3\2\2\2\u02e1\u02df\3\2\2\2\u02e2\u02db\3\2"+
		"\2\2\u02e2\u02e3\3\2\2\2\u02e3\u02e4\3\2\2\2\u02e4\u02e7\5\u009cO\2\u02e5"+
		"\u02e7\5\62\32\2\u02e6\u02e2\3\2\2\2\u02e6\u02e5\3\2\2\2\u02e7\61\3\2"+
		"\2\2\u02e8\u02f0\5\64\33\2\u02e9\u02f0\5<\37\2\u02ea\u02ed\5\20\t\2\u02eb"+
		"\u02ee\5(\25\2\u02ec\u02ee\5\66\34\2\u02ed\u02eb\3\2\2\2\u02ed\u02ec\3"+
		"\2\2\2\u02ee\u02f0\3\2\2\2\u02ef\u02e8\3\2\2\2\u02ef\u02e9\3\2\2\2\u02ef"+
		"\u02ea\3\2\2\2\u02f0\63\3\2\2\2\u02f1\u02f3\5\20\t\2\u02f2\u02f4\5 \21"+
		"\2\u02f3\u02f2\3\2\2\2\u02f3\u02f4\3\2\2\2\u02f4\u02fc\3\2\2\2\u02f5\u02f9"+
		"\5:\36\2\u02f6\u02f8\7\u008f\2\2\u02f7\u02f6\3\2\2\2\u02f8\u02fb\3\2\2"+
		"\2\u02f9\u02f7\3\2\2\2\u02f9\u02fa\3\2\2\2\u02fa\u02fd\3\2\2\2\u02fb\u02f9"+
		"\3\2\2\2\u02fc\u02f5\3\2\2\2\u02fc\u02fd\3\2\2\2\u02fd\u02fe\3\2\2\2\u02fe"+
		"\u02ff\58\35\2\u02ff\u0327\5\\/\2\u0300\u0301\6\33\4\3\u0301\u0305\7\32"+
		"\2\2\u0302\u0304\7\u008f\2\2\u0303\u0302\3\2\2\2\u0304\u0307\3\2\2\2\u0305"+
		"\u0303\3\2\2\2\u0305\u0306\3\2\2\2\u0306\u0308\3\2\2\2\u0307\u0305\3\2"+
		"\2\2\u0308\u0328\5\u0098M\2\u0309\u030b\7\u008f\2\2\u030a\u0309\3\2\2"+
		"\2\u030b\u030e\3\2\2\2\u030c\u030a\3\2\2\2\u030c\u030d\3\2\2\2\u030d\u030f"+
		"\3\2\2\2\u030e\u030c\3\2\2\2\u030f\u0313\7;\2\2\u0310\u0312\7\u008f\2"+
		"\2\u0311\u0310\3\2\2\2\u0312\u0315\3\2\2\2\u0313\u0311\3\2\2\2\u0313\u0314"+
		"\3\2\2\2\u0314\u0316\3\2\2\2\u0315\u0313\3\2\2\2\u0316\u031e\5Z.\2\u0317"+
		"\u0319\7\u008f\2\2\u0318\u0317\3\2\2\2\u0319\u031c\3\2\2\2\u031a\u0318"+
		"\3\2\2\2\u031a\u031b\3\2\2\2\u031b\u031d\3\2\2\2\u031c\u031a\3\2\2\2\u031d"+
		"\u031f\5d\63\2\u031e\u031a\3\2\2\2\u031e\u031f\3\2\2\2\u031f\u0328\3\2"+
		"\2\2\u0320\u0322\7\u008f\2\2\u0321\u0320\3\2\2\2\u0322\u0325\3\2\2\2\u0323"+
		"\u0321\3\2\2\2\u0323\u0324\3\2\2\2\u0324\u0326\3\2\2\2\u0325\u0323\3\2"+
		"\2\2\u0326\u0328\5d\63\2\u0327\u0300\3\2\2\2\u0327\u030c\3\2\2\2\u0327"+
		"\u0323\3\2\2\2\u0327\u0328\3\2\2\2\u0328\65\3\2\2\2\u0329\u032d\58\35"+
		"\2\u032a\u032c\7\u008f\2\2\u032b\u032a\3\2\2\2\u032c\u032f\3\2\2\2\u032d"+
		"\u032b\3\2\2\2\u032d\u032e\3\2\2\2\u032e\u0330\3\2\2\2\u032f\u032d\3\2"+
		"\2\2\u0330\u0331\5d\63\2\u0331\67\3\2\2\2\u0332\u0335\5\u0148\u00a5\2"+
		"\u0333\u0335\5\u0144\u00a3\2\u0334\u0332\3\2\2\2\u0334\u0333\3\2\2\2\u0335"+
		"9\3\2\2\2\u0336\u0339\5P)\2\u0337\u0339\7@\2\2\u0338\u0336\3\2\2\2\u0338"+
		"\u0337\3\2\2\2\u0339;\3\2\2\2\u033a\u033b\5\u00a2R\2\u033b=\3\2\2\2\u033c"+
		"\u0347\5@!\2\u033d\u0341\7e\2\2\u033e\u0340\7\u008f\2\2\u033f\u033e\3"+
		"\2\2\2\u0340\u0343\3\2\2\2\u0341\u033f\3\2\2\2\u0341\u0342\3\2\2\2\u0342"+
		"\u0344\3\2\2\2\u0343\u0341\3\2\2\2\u0344\u0346\5@!\2\u0345\u033d\3\2\2"+
		"\2\u0346\u0349\3\2\2\2\u0347\u0345\3\2\2\2\u0347\u0348\3\2\2\2\u0348?"+
		"\3\2\2\2\u0349\u0347\3\2\2\2\u034a\u035d\5B\"\2\u034b\u034d\7\u008f\2"+
		"\2\u034c\u034b\3\2\2\2\u034d\u0350\3\2\2\2\u034e\u034c\3\2\2\2\u034e\u034f"+
		"\3\2\2\2\u034f\u0351\3\2\2\2\u0350\u034e\3\2\2\2\u0351\u0355\7g\2\2\u0352"+
		"\u0354\7\u008f\2\2\u0353\u0352\3\2\2\2\u0354\u0357\3\2\2\2\u0355\u0353"+
		"\3\2\2\2\u0355\u0356\3\2\2\2\u0356\u035b\3\2\2\2\u0357\u0355\3\2\2\2\u0358"+
		"\u0359\6!\5\3\u0359\u035c\5\u012e\u0098\2\u035a\u035c\5D#\2\u035b\u0358"+
		"\3\2\2\2\u035b\u035a\3\2\2\2\u035c\u035e\3\2\2\2\u035d\u034e\3\2\2\2\u035d"+
		"\u035e\3\2\2\2\u035eA\3\2\2\2\u035f\u0362\5\u0148\u00a5\2\u0360\u0362"+
		"\5\u014a\u00a6\2\u0361\u035f\3\2\2\2\u0361\u0360\3\2\2\2\u0362C\3\2\2"+
		"\2\u0363\u0364\5\u00ecw\2\u0364E\3\2\2\2\u0365\u0369\5\u008aF\2\u0366"+
		"\u036a\7@\2\2\u0367\u036a\5H%\2\u0368\u036a\5J&\2\u0369\u0366\3\2\2\2"+
		"\u0369\u0367\3\2\2\2\u0369\u0368\3\2\2\2\u036a\u036e\3\2\2\2\u036b\u036d"+
		"\5\u012a\u0096\2\u036c\u036b\3\2\2\2\u036d\u0370\3\2\2\2\u036e\u036c\3"+
		"\2\2\2\u036e\u036f\3\2\2\2\u036fG\3\2\2\2\u0370\u036e\3\2\2\2\u0371\u0372"+
		"\7\21\2\2\u0372I\3\2\2\2\u0373\u037f\5l\67\2\u0374\u037c\5T+\2\u0375\u0376"+
		"\7f\2\2\u0376\u0378\5\u0148\u00a5\2\u0377\u0379\5T+\2\u0378\u0377\3\2"+
		"\2\2\u0378\u0379\3\2\2\2\u0379\u037b\3\2\2\2\u037a\u0375\3\2\2\2\u037b"+
		"\u037e\3\2\2\2\u037c\u037a\3\2\2\2\u037c\u037d\3\2\2\2\u037d\u0380\3\2"+
		"\2\2\u037e\u037c\3\2\2\2\u037f\u0374\3\2\2\2\u037f\u0380\3\2\2\2\u0380"+
		"K\3\2\2\2\u0381\u0383\5P)\2\u0382\u0384\5\u0148\u00a5\2\u0383\u0382\3"+
		"\2\2\2\u0383\u0384\3\2\2\2\u0384M\3\2\2\2\u0385\u0388\5L\'\2\u0386\u0388"+
		"\5\u00dco\2\u0387\u0385\3\2\2\2\u0387\u0386\3\2\2\2\u0388O\3\2\2\2\u0389"+
		"\u038c\5\u008aF\2\u038a\u038d\5H%\2\u038b\u038d\5R*\2\u038c\u038a\3\2"+
		"\2\2\u038c\u038b\3\2\2\2\u038d\u0391\3\2\2\2\u038e\u0390\5\u012a\u0096"+
		"\2\u038f\u038e\3\2\2\2\u0390\u0393\3\2\2\2\u0391\u038f\3\2\2\2\u0391\u0392"+
		"\3\2\2\2\u0392Q\3\2\2\2\u0393\u0391\3\2\2\2\u0394\u03a0\5n8\2\u0395\u039d"+
		"\5T+\2\u0396\u0397\7f\2\2\u0397\u0399\5\u0146\u00a4\2\u0398\u039a\5T+"+
		"\2\u0399\u0398\3\2\2\2\u0399\u039a\3\2\2\2\u039a\u039c\3\2\2\2\u039b\u0396"+
		"\3\2\2\2\u039c\u039f\3\2\2\2\u039d\u039b\3\2\2\2\u039d\u039e\3\2\2\2\u039e"+
		"\u03a1\3\2\2\2\u039f\u039d\3\2\2\2\u03a0\u0395\3\2\2\2\u03a0\u03a1\3\2"+
		"\2\2\u03a1S\3\2\2\2\u03a2\u03a6\7i\2\2\u03a3\u03a5\7\u008f\2\2\u03a4\u03a3"+
		"\3\2\2\2\u03a5\u03a8\3\2\2\2\u03a6\u03a4\3\2\2\2\u03a6\u03a7\3\2\2\2\u03a7"+
		"\u03a9\3\2\2\2\u03a8\u03a6\3\2\2\2\u03a9\u03b4\5V,\2\u03aa\u03ae\7e\2"+
		"\2\u03ab\u03ad\7\u008f\2\2\u03ac\u03ab\3\2\2\2\u03ad\u03b0\3\2\2\2\u03ae"+
		"\u03ac\3\2\2\2\u03ae\u03af\3\2\2\2\u03af\u03b1\3\2\2\2\u03b0\u03ae\3\2"+
		"\2\2\u03b1\u03b3\5V,\2\u03b2\u03aa\3\2\2\2\u03b3\u03b6\3\2\2\2\u03b4\u03b2"+
		"\3\2\2\2\u03b4\u03b5\3\2\2\2\u03b5\u03ba\3\2\2\2\u03b6\u03b4\3\2\2\2\u03b7"+
		"\u03b9\7\u008f\2\2\u03b8\u03b7\3\2\2\2\u03b9\u03bc\3\2\2\2\u03ba\u03b8"+
		"\3\2\2\2\u03ba\u03bb\3\2\2\2\u03bb\u03bd\3\2\2\2\u03bc\u03ba\3\2\2\2\u03bd"+
		"\u03be\7h\2\2\u03beU\3\2\2\2\u03bf\u03cd\5F$\2\u03c0\u03c1\5\u008aF\2"+
		"\u03c1\u03ca\7l\2\2\u03c2\u03c6\t\5\2\2\u03c3\u03c5\7\u008f\2\2\u03c4"+
		"\u03c3\3\2\2\2\u03c5\u03c8\3\2\2\2\u03c6\u03c4\3\2\2\2\u03c6\u03c7\3\2"+
		"\2\2\u03c7\u03c9\3\2\2\2\u03c8\u03c6\3\2\2\2\u03c9\u03cb\5F$\2\u03ca\u03c2"+
		"\3\2\2\2\u03ca\u03cb\3\2\2\2\u03cb\u03cd\3\2\2\2\u03cc\u03bf\3\2\2\2\u03cc"+
		"\u03c0\3\2\2\2\u03cdW\3\2\2\2\u03ce\u03cf\5\u008aF\2\u03cf\u03d0\5l\67"+
		"\2\u03d0Y\3\2\2\2\u03d1\u03dc\5X-\2\u03d2\u03d6\7e\2\2\u03d3\u03d5\7\u008f"+
		"\2\2\u03d4\u03d3\3\2\2\2\u03d5\u03d8\3\2\2\2\u03d6\u03d4\3\2\2\2\u03d6"+
		"\u03d7\3\2\2\2\u03d7\u03d9\3\2\2\2\u03d8\u03d6\3\2\2\2\u03d9\u03db\5X"+
		"-\2\u03da\u03d2\3\2\2\2\u03db\u03de\3\2\2\2\u03dc\u03da\3\2\2\2\u03dc"+
		"\u03dd\3\2\2\2\u03dd[\3\2\2\2\u03de\u03dc\3\2\2\2\u03df\u03e1\7^\2\2\u03e0"+
		"\u03e2\5^\60\2\u03e1\u03e0\3\2\2\2\u03e1\u03e2\3\2\2\2\u03e2\u03e3\3\2"+
		"\2\2\u03e3\u03e4\7_\2\2\u03e4]\3\2\2\2\u03e5\u03e8\5b\62\2\u03e6\u03e8"+
		"\5`\61\2\u03e7\u03e5\3\2\2\2\u03e7\u03e6\3\2\2\2\u03e8\u03f3\3\2\2\2\u03e9"+
		"\u03ed\7e\2\2\u03ea\u03ec\7\u008f\2\2\u03eb\u03ea\3\2\2\2\u03ec\u03ef"+
		"\3\2\2\2\u03ed\u03eb\3\2\2\2\u03ed\u03ee\3\2\2\2\u03ee\u03f0\3\2\2\2\u03ef"+
		"\u03ed\3\2\2\2\u03f0\u03f2\5b\62\2\u03f1\u03e9\3\2\2\2\u03f2\u03f5\3\2"+
		"\2\2\u03f3\u03f1\3\2\2\2\u03f3\u03f4\3\2\2\2\u03f4_\3\2\2\2\u03f5\u03f3"+
		"\3\2\2\2\u03f6\u03f7\5F$\2\u03f7\u03f8\79\2\2\u03f8a\3\2\2\2\u03f9\u03fb"+
		"\5\34\17\2\u03fa\u03fc\5F$\2\u03fb\u03fa\3\2\2\2\u03fb\u03fc\3\2\2\2\u03fc"+
		"\u03fe\3\2\2\2\u03fd\u03ff\7\u008d\2\2\u03fe\u03fd\3\2\2\2\u03fe\u03ff"+
		"\3\2\2\2\u03ff\u0400\3\2\2\2\u0400\u040f\5B\"\2\u0401\u0403\7\u008f\2"+
		"\2\u0402\u0401\3\2\2\2\u0403\u0406\3\2\2\2\u0404\u0402\3\2\2\2\u0404\u0405"+
		"\3\2\2\2\u0405\u0407\3\2\2\2\u0406\u0404\3\2\2\2\u0407\u040b\7g\2\2\u0408"+
		"\u040a\7\u008f\2\2\u0409\u0408\3\2\2\2\u040a\u040d\3\2\2\2\u040b\u0409"+
		"\3\2\2\2\u040b\u040c\3\2\2\2\u040c\u040e\3\2\2\2\u040d\u040b\3\2\2\2\u040e"+
		"\u0410\5\u00f8}\2\u040f\u0404\3\2\2\2\u040f\u0410\3\2\2\2\u0410c\3\2\2"+
		"\2\u0411\u0412\5\u009cO\2\u0412e\3\2\2\2\u0413\u0418\5h\65\2\u0414\u0415"+
		"\7f\2\2\u0415\u0417\5h\65\2\u0416\u0414\3\2\2\2\u0417\u041a\3\2\2\2\u0418"+
		"\u0416\3\2\2\2\u0418\u0419\3\2\2\2\u0419g\3\2\2\2\u041a\u0418\3\2\2\2"+
		"\u041b\u0421\5\u0148\u00a5\2\u041c\u0421\7\n\2\2\u041d\u0421\7\13\2\2"+
		"\u041e\u0421\7\t\2\2\u041f\u0421\7\f\2\2\u0420\u041b\3\2\2\2\u0420\u041c"+
		"\3\2\2\2\u0420\u041d\3\2\2\2\u0420\u041e\3\2\2\2\u0420\u041f\3\2\2\2\u0421"+
		"i\3\2\2\2\u0422\u0423\5h\65\2\u0423\u0424\7f\2\2\u0424\u0426\3\2\2\2\u0425"+
		"\u0422\3\2\2\2\u0426\u0429\3\2\2\2\u0427\u0425\3\2\2\2\u0427\u0428\3\2"+
		"\2\2\u0428k\3\2\2\2\u0429\u0427\3\2\2\2\u042a\u042b\5j\66\2\u042b\u042c"+
		"\5\u0148\u00a5\2\u042cm\3\2\2\2\u042d\u042e\5j\66\2\u042e\u0433\5\u0146"+
		"\u00a4\2\u042f\u0430\7f\2\2\u0430\u0432\5\u0146\u00a4\2\u0431\u042f\3"+
		"\2\2\2\u0432\u0435\3\2\2\2\u0433\u0431\3\2\2\2\u0433\u0434\3\2\2\2\u0434"+
		"o\3\2\2\2\u0435\u0433\3\2\2\2\u0436\u043c\7D\2\2\u0437\u043c\7E\2\2\u0438"+
		"\u043c\5\u0144\u00a3\2\u0439\u043c\7F\2\2\u043a\u043c\7G\2\2\u043b\u0436"+
		"\3\2\2\2\u043b\u0437\3\2\2\2\u043b\u0438\3\2\2\2\u043b\u0439\3\2\2\2\u043b"+
		"\u043a\3\2\2\2\u043cq\3\2\2\2\u043d\u043e\7\4\2\2\u043e\u0443\5t;\2\u043f"+
		"\u0440\7\6\2\2\u0440\u0442\5t;\2\u0441\u043f\3\2\2\2\u0442\u0445\3\2\2"+
		"\2\u0443\u0441\3\2\2\2\u0443\u0444\3\2\2\2\u0444\u0446\3\2\2\2\u0445\u0443"+
		"\3\2\2\2\u0446\u0447\7\5\2\2\u0447s\3\2\2\2\u0448\u044b\5v<\2\u0449\u044b"+
		"\5\u0082B\2\u044a\u0448\3\2\2\2\u044a\u0449\3\2\2\2\u044bu\3\2\2\2\u044c"+
		"\u0450\5\u0148\u00a5\2\u044d\u044f\7\7\2\2\u044e\u044d\3\2\2\2\u044f\u0452"+
		"\3\2\2\2\u0450\u044e\3\2\2\2\u0450\u0451\3\2\2\2\u0451w\3\2\2\2\u0452"+
		"\u0450\3\2\2\2\u0453\u0457\5|?\2\u0454\u0456\7\u008f\2\2\u0455\u0454\3"+
		"\2\2\2\u0456\u0459\3\2\2\2\u0457\u0455\3\2\2\2\u0457\u0458\3\2\2\2\u0458"+
		"\u045a\3\2\2\2\u0459\u0457\3\2\2\2\u045a\u045e\7[\2\2\u045b\u045d\7\u008f"+
		"\2\2\u045c\u045b\3\2\2\2\u045d\u0460\3\2\2\2\u045e\u045c\3\2\2\2\u045e"+
		"\u045f\3\2\2\2\u045f\u0461\3\2\2\2\u0460\u045e\3\2\2\2\u0461\u0462\5\u0080"+
		"A\2\u0462y\3\2\2\2\u0463\u0467\5~@\2\u0464\u0466\7\u008f\2\2\u0465\u0464"+
		"\3\2\2\2\u0466\u0469\3\2\2\2\u0467\u0465\3\2\2\2\u0467\u0468\3\2\2\2\u0468"+
		"\u046a\3\2\2\2\u0469\u0467\3\2\2\2\u046a\u046e\7[\2\2\u046b\u046d\7\u008f"+
		"\2\2\u046c\u046b\3\2\2\2\u046d\u0470\3\2\2\2\u046e\u046c\3\2\2\2\u046e"+
		"\u046f\3\2\2\2\u046f\u0471\3\2\2\2\u0470\u046e\3\2\2\2\u0471\u0472\5\u0080"+
		"A\2\u0472{\3\2\2\2\u0473\u0474\5\\/\2\u0474}\3\2\2\2\u0475\u0478\5\\/"+
		"\2\u0476\u0478\5B\"\2\u0477\u0475\3\2\2\2\u0477\u0476\3\2\2\2\u0478\177"+
		"\3\2\2\2\u0479\u047c\5\u009cO\2\u047a\u047c\5\u00eex\2\u047b\u0479\3\2"+
		"\2\2\u047b\u047a\3\2\2\2\u047c\u0081\3\2\2\2\u047d\u048e\7`\2\2\u047e"+
		"\u0480\7\u008f\2\2\u047f\u047e\3\2\2\2\u0480\u0483\3\2\2\2\u0481\u047f"+
		"\3\2\2\2\u0481\u0482\3\2\2\2\u0482\u048b\3\2\2\2\u0483\u0481\3\2\2\2\u0484"+
		"\u0488\5^\60\2\u0485\u0487\7\u008f\2\2\u0486\u0485\3\2\2\2\u0487\u048a"+
		"\3\2\2\2\u0488\u0486\3\2\2\2\u0488\u0489\3\2\2\2\u0489\u048c\3\2\2\2\u048a"+
		"\u0488\3\2\2\2\u048b\u0484\3\2\2\2\u048b\u048c\3\2\2\2\u048c\u048d\3\2"+
		"\2\2\u048d\u048f\7[\2\2\u048e\u0481\3\2\2\2\u048e\u048f\3\2\2\2\u048f"+
		"\u0491\3\2\2\2\u0490\u0492\5\u014e\u00a8\2\u0491\u0490\3\2\2\2\u0491\u0492"+
		"\3\2\2\2\u0492\u0493\3\2\2\2\u0493\u0494\5\u0086D\2\u0494\u0495\7a\2\2"+
		"\u0495\u0083\3\2\2\2\u0496\u0499\5\u0082B\2\u0497\u0499\5x=\2\u0498\u0496"+
		"\3\2\2\2\u0498\u0497\3\2\2\2\u0499\u0085\3\2\2\2\u049a\u049c\5\u0088E"+
		"\2\u049b\u049a\3\2\2\2\u049b\u049c\3\2\2\2\u049c\u0087\3\2\2\2\u049d\u04a3"+
		"\5\u009eP\2\u049e\u049f\5\u014e\u00a8\2\u049f\u04a0\5\u009eP\2\u04a0\u04a2"+
		"\3\2\2\2\u04a1\u049e\3\2\2\2\u04a2\u04a5\3\2\2\2\u04a3\u04a1\3\2\2\2\u04a3"+
		"\u04a4\3\2\2\2\u04a4\u04a7\3\2\2\2\u04a5\u04a3\3\2\2\2\u04a6\u04a8\5\u014e"+
		"\u00a8\2\u04a7\u04a6\3\2\2\2\u04a7\u04a8\3\2\2\2\u04a8\u0089\3\2\2\2\u04a9"+
		"\u04b3\5\u008cG\2\u04aa\u04ac\7\u008f\2\2\u04ab\u04aa\3\2\2\2\u04ac\u04af"+
		"\3\2\2\2\u04ad\u04ab\3\2\2\2\u04ad\u04ae\3\2\2\2\u04ae\u04b0\3\2\2\2\u04af"+
		"\u04ad\3\2\2\2\u04b0\u04b2\5\u008cG\2\u04b1\u04ad\3\2\2\2\u04b2\u04b5"+
		"\3\2\2\2\u04b3\u04b1\3\2\2\2\u04b3\u04b4\3\2\2\2\u04b4\u04b9\3\2\2\2\u04b5"+
		"\u04b3\3\2\2\2\u04b6\u04b8\7\u008f\2\2\u04b7\u04b6\3\2\2\2\u04b8\u04bb"+
		"\3\2\2\2\u04b9\u04b7\3\2\2\2\u04b9\u04ba\3\2\2\2\u04ba\u04bd\3\2\2\2\u04bb"+
		"\u04b9\3\2\2\2\u04bc\u04a9\3\2\2\2\u04bc\u04bd\3\2\2\2\u04bd\u008b\3\2"+
		"\2\2\u04be\u04bf\7\u008c\2\2\u04bf\u04cb\5\u0090I\2\u04c0\u04c2\7\u008f"+
		"\2\2\u04c1\u04c0\3\2\2\2\u04c2\u04c5\3\2\2\2\u04c3\u04c1\3\2\2\2\u04c3"+
		"\u04c4\3\2\2\2\u04c4\u04c6\3\2\2\2\u04c5\u04c3\3\2\2\2\u04c6\u04c8\7^"+
		"\2\2\u04c7\u04c9\5\u008eH\2\u04c8\u04c7\3\2\2\2\u04c8\u04c9\3\2\2\2\u04c9"+
		"\u04ca\3\2\2\2\u04ca\u04cc\7_\2\2\u04cb\u04c3\3\2\2\2\u04cb\u04cc\3\2"+
		"\2\2\u04cc\u008d\3\2\2\2\u04cd\u04d1\5\u0092J\2\u04ce\u04cf\6H\6\2\u04cf"+
		"\u04d1\5\u0098M\2\u04d0\u04cd\3\2\2\2\u04d0\u04ce\3\2\2\2\u04d1\u008f"+
		"\3\2\2\2\u04d2\u04d3\5l\67\2\u04d3\u0091\3\2\2\2\u04d4\u04d9\5\u0094K"+
		"\2\u04d5\u04d6\7e\2\2\u04d6\u04d8\5\u0094K\2\u04d7\u04d5\3\2\2\2\u04d8"+
		"\u04db\3\2\2\2\u04d9\u04d7\3\2\2\2\u04d9\u04da\3\2\2\2\u04da\u0093\3\2"+
		"\2\2\u04db\u04d9\3\2\2\2\u04dc\u04e0\5\u0096L\2\u04dd\u04df\7\u008f\2"+
		"\2\u04de\u04dd\3\2\2\2\u04df\u04e2\3\2\2\2\u04e0\u04de\3\2\2\2\u04e0\u04e1"+
		"\3\2\2\2\u04e1\u04e3\3\2\2\2\u04e2\u04e0\3\2\2\2\u04e3\u04e7\7g\2\2\u04e4"+
		"\u04e6\7\u008f\2\2\u04e5\u04e4\3\2\2\2\u04e6\u04e9\3\2\2\2\u04e7\u04e5"+
		"\3\2\2\2\u04e7\u04e8\3\2\2\2\u04e8\u04ea\3\2\2\2\u04e9\u04e7\3\2\2\2\u04ea"+
		"\u04eb\5\u0098M\2\u04eb\u0095\3\2\2\2\u04ec\u04ef\5\u0148\u00a5\2\u04ed"+
		"\u04ef\5\u014c\u00a7\2\u04ee\u04ec\3\2\2\2\u04ee\u04ed\3\2\2\2\u04ef\u0097"+
		"\3\2\2\2\u04f0\u04f4\5\u009aN\2\u04f1\u04f4\5\u008cG\2\u04f2\u04f4\5\u00f8"+
		"}\2\u04f3\u04f0\3\2\2\2\u04f3\u04f1\3\2\2\2\u04f3\u04f2\3\2\2\2\u04f4"+
		"\u0099\3\2\2\2\u04f5\u0501\7b\2\2\u04f6\u04fb\5\u0098M\2\u04f7\u04f8\7"+
		"e\2\2\u04f8\u04fa\5\u0098M\2\u04f9\u04f7\3\2\2\2\u04fa\u04fd\3\2\2\2\u04fb"+
		"\u04f9\3\2\2\2\u04fb\u04fc\3\2\2\2\u04fc\u04ff\3\2\2\2\u04fd\u04fb\3\2"+
		"\2\2\u04fe\u0500\7e\2\2\u04ff\u04fe\3\2\2\2\u04ff\u0500\3\2\2\2\u0500"+
		"\u0502\3\2\2\2\u0501\u04f6\3\2\2\2\u0501\u0502\3\2\2\2\u0502\u0503\3\2"+
		"\2\2\u0503\u0512\7c\2\2\u0504\u0508\7`\2\2\u0505\u0506\5\u0098M\2\u0506"+
		"\u0507\7e\2\2\u0507\u0509\3\2\2\2\u0508\u0505\3\2\2\2\u0509\u050a\3\2"+
		"\2\2\u050a\u0508\3\2\2\2\u050a\u050b\3\2\2\2\u050b\u050d\3\2\2\2\u050c"+
		"\u050e\5\u0098M\2\u050d\u050c\3\2\2\2\u050d\u050e\3\2\2\2\u050e\u050f"+
		"\3\2\2\2\u050f\u0510\7a\2\2\u0510\u0512\3\2\2\2\u0511\u04f5\3\2\2\2\u0511"+
		"\u0504\3\2\2\2\u0512\u009b\3\2\2\2\u0513\u0515\7`\2\2\u0514\u0516\5\u014e"+
		"\u00a8\2\u0515\u0514\3\2\2\2\u0515\u0516\3\2\2\2\u0516\u0517\3\2\2\2\u0517"+
		"\u0518\5\u0086D\2\u0518\u0519\7a\2\2\u0519\u009d\3\2\2\2\u051a\u051b\5"+
		"\u00be`\2\u051b\u009f\3\2\2\2\u051c\u051d\6Q\7\2\u051d\u051e\5\u00a2R"+
		"\2\u051e\u00a1\3\2\2\2\u051f\u0523\5\22\n\2\u0520\u0522\7\u008f\2\2\u0521"+
		"\u0520\3\2\2\2\u0522\u0525\3\2\2\2\u0523\u0521\3\2\2\2\u0523\u0524\3\2"+
		"\2\2\u0524\u053a\3\2\2\2\u0525\u0523\3\2\2\2\u0526\u0528\5F$\2\u0527\u0526"+
		"\3\2\2\2\u0527\u0528\3\2\2\2\u0528\u0529\3\2\2\2\u0529\u053b\5> \2\u052a"+
		"\u052e\5\u00a4S\2\u052b\u052d\7\u008f\2\2\u052c\u052b\3\2\2\2\u052d\u0530"+
		"\3\2\2\2\u052e\u052c\3\2\2\2\u052e\u052f\3\2\2\2\u052f\u0531\3\2\2\2\u0530"+
		"\u052e\3\2\2\2\u0531\u0535\7g\2\2\u0532\u0534\7\u008f\2\2\u0533\u0532"+
		"\3\2\2\2\u0534\u0537\3\2\2\2\u0535\u0533\3\2\2\2\u0535\u0536\3\2\2\2\u0536"+
		"\u0538\3\2\2\2\u0537\u0535\3\2\2\2\u0538\u0539\5D#\2\u0539\u053b\3\2\2"+
		"\2\u053a\u0527\3\2\2\2\u053a\u052a\3\2\2\2\u053b\u0540\3\2\2\2\u053c\u053d"+
		"\5F$\2\u053d\u053e\5> \2\u053e\u0540\3\2\2\2\u053f\u051f\3\2\2\2\u053f"+
		"\u053c\3\2\2\2\u0540\u00a3\3\2\2\2\u0541\u0552\7^\2\2\u0542\u0547\5\u00a6"+
		"T\2\u0543\u0544\7e\2\2\u0544\u0546\5\u00a6T\2\u0545\u0543\3\2\2\2\u0546"+
		"\u0549\3\2\2\2\u0547\u0545\3\2\2\2\u0547\u0548\3\2\2\2\u0548\u0553\3\2"+
		"\2\2\u0549\u0547\3\2\2\2\u054a\u054f\5\u00a8U\2\u054b\u054c\7e\2\2\u054c"+
		"\u054e\5\u00a8U\2\u054d\u054b\3\2\2\2\u054e\u0551\3\2\2\2\u054f\u054d"+
		"\3\2\2\2\u054f\u0550\3\2\2\2\u0550\u0553\3\2\2\2\u0551\u054f\3\2\2\2\u0552"+
		"\u0542\3\2\2\2\u0552\u054a\3\2\2\2\u0553\u0554\3\2\2\2\u0554\u0555\7_"+
		"\2\2\u0555\u00a5\3\2\2\2\u0556\u055b\7\n\2\2\u0557\u055b\7>\2\2\u0558"+
		"\u055b\7?\2\2\u0559\u055b\5F$\2\u055a\u0556\3\2\2\2\u055a\u0557\3\2\2"+
		"\2\u055a\u0558\3\2\2\2\u055a\u0559\3\2\2\2\u055a\u055b\3\2\2\2\u055b\u055d"+
		"\3\2\2\2\u055c\u055e\7x\2\2\u055d\u055c\3\2\2\2\u055d\u055e\3\2\2\2\u055e"+
		"\u055f\3\2\2\2\u055f\u0560\5B\"\2\u0560\u00a7\3\2\2\2\u0561\u0562\5\u0148"+
		"\u00a5\2\u0562\u0567\7m\2\2\u0563\u0568\7\n\2\2\u0564\u0568\7>\2\2\u0565"+
		"\u0568\7?\2\2\u0566\u0568\5F$\2\u0567\u0563\3\2\2\2\u0567\u0564\3\2\2"+
		"\2\u0567\u0565\3\2\2\2\u0567\u0566\3\2\2\2\u0567\u0568\3\2\2\2\u0568\u0569"+
		"\3\2\2\2\u0569\u056a\5B\"\2\u056a\u00a9\3\2\2\2\u056b\u056c\7^\2\2\u056c"+
		"\u056f\5B\"\2\u056d\u056e\7e\2\2\u056e\u0570\5B\"\2\u056f\u056d\3\2\2"+
		"\2\u0570\u0571\3\2\2\2\u0571\u056f\3\2\2\2\u0571\u0572\3\2\2\2\u0572\u0573"+
		"\3\2\2\2\u0573\u0574\7_\2\2\u0574\u00ab\3\2\2\2\u0575\u0578\5\u00aeX\2"+
		"\u0576\u0578\5\u00b0Y\2\u0577\u0575\3\2\2\2\u0577\u0576\3\2\2\2\u0578"+
		"\u00ad\3\2\2\2\u0579\u057a\7\"\2\2\u057a\u057e\5\u00e4s\2\u057b\u057d"+
		"\7\u008f\2\2\u057c\u057b\3\2\2\2\u057d\u0580\3\2\2\2\u057e\u057c\3\2\2"+
		"\2\u057e\u057f\3\2\2\2\u057f\u0581\3\2\2\2\u0580\u057e\3\2\2\2\u0581\u0593"+
		"\5\u00be`\2\u0582\u0584\7\u008f\2\2\u0583\u0582\3\2\2\2\u0584\u0587\3"+
		"\2\2\2\u0585\u0583\3\2\2\2\u0585\u0586\3\2\2\2\u0586\u058a\3\2\2\2\u0587"+
		"\u0585\3\2\2\2\u0588\u058a\5\u014e\u00a8\2\u0589\u0585\3\2\2\2\u0589\u0588"+
		"\3\2\2\2\u058a\u058b\3\2\2\2\u058b\u058f\7\34\2\2\u058c\u058e\7\u008f"+
		"\2\2\u058d\u058c\3\2\2\2\u058e\u0591\3\2\2\2\u058f\u058d\3\2\2\2\u058f"+
		"\u0590\3\2\2\2\u0590\u0592\3\2\2\2\u0591\u058f\3\2\2\2\u0592\u0594\5\u00be"+
		"`\2\u0593\u0589\3\2\2\2\u0593\u0594\3\2\2\2\u0594\u00af\3\2\2\2\u0595"+
		"\u0596\7\67\2\2\u0596\u059a\5\u00e4s\2\u0597\u0599\7\u008f\2\2\u0598\u0597"+
		"\3\2\2\2\u0599\u059c\3\2\2\2\u059a\u0598\3\2\2\2\u059a\u059b\3\2\2\2\u059b"+
		"\u059d\3\2\2\2\u059c\u059a\3\2\2\2\u059d\u05a1\7`\2\2\u059e\u05a0\7\u008f"+
		"\2\2\u059f\u059e\3\2\2\2\u05a0\u05a3\3\2\2\2\u05a1\u059f\3\2\2\2\u05a1"+
		"\u05a2\3\2\2\2\u05a2\u05af\3\2\2\2\u05a3\u05a1\3\2\2\2\u05a4\u05a6\5\u00cc"+
		"g\2\u05a5\u05a4\3\2\2\2\u05a6\u05a7\3\2\2\2\u05a7\u05a5\3\2\2\2\u05a7"+
		"\u05a8\3\2\2\2\u05a8\u05ac\3\2\2\2\u05a9\u05ab\7\u008f\2\2\u05aa\u05a9"+
		"\3\2\2\2\u05ab\u05ae\3\2\2\2\u05ac\u05aa\3\2\2\2\u05ac\u05ad\3\2\2\2\u05ad"+
		"\u05b0\3\2\2\2\u05ae\u05ac\3\2\2\2\u05af\u05a5\3\2\2\2\u05af\u05b0\3\2"+
		"\2\2\u05b0\u05b1\3\2\2\2\u05b1\u05b2\7a\2\2\u05b2\u00b1\3\2\2\2\u05b3"+
		"\u05b4\5\u008aF\2\u05b4\u05b6\7!\2\2\u05b5\u05b7\7\17\2\2\u05b6\u05b5"+
		"\3\2\2\2\u05b6\u05b7\3\2\2\2\u05b7\u05b8\3\2\2\2\u05b8\u05b9\7^\2\2\u05b9"+
		"\u05ba\5\u00d0i\2\u05ba\u05be\7_\2\2\u05bb\u05bd\7\u008f\2\2\u05bc\u05bb"+
		"\3\2\2\2\u05bd\u05c0\3\2\2\2\u05be\u05bc\3\2\2\2\u05be\u05bf\3\2\2\2\u05bf"+
		"\u05c1\3\2\2\2\u05c0\u05be\3\2\2\2\u05c1\u05c2\5\u00be`\2\u05c2\u05e1"+
		"\3\2\2\2\u05c3\u05c4\5\u008aF\2\u05c4\u05c5\7B\2\2\u05c5\u05c9\5\u00e4"+
		"s\2\u05c6\u05c8\7\u008f\2\2\u05c7\u05c6\3\2\2\2\u05c8\u05cb\3\2\2\2\u05c9"+
		"\u05c7\3\2\2\2\u05c9\u05ca\3\2\2\2\u05ca\u05cc\3\2\2\2\u05cb\u05c9\3\2"+
		"\2\2\u05cc\u05cd\5\u00be`\2\u05cd\u05e1\3\2\2\2\u05ce\u05cf\5\u008aF\2"+
		"\u05cf\u05d3\7\33\2\2\u05d0\u05d2\7\u008f\2\2\u05d1\u05d0\3\2\2\2\u05d2"+
		"\u05d5\3\2\2\2\u05d3\u05d1\3\2\2\2\u05d3\u05d4\3\2\2\2\u05d4\u05d6\3\2"+
		"\2\2\u05d5\u05d3\3\2\2\2\u05d6\u05da\5\u00be`\2\u05d7\u05d9\7\u008f\2"+
		"\2\u05d8\u05d7\3\2\2\2\u05d9\u05dc\3\2\2\2\u05da\u05d8\3\2\2\2\u05da\u05db"+
		"\3\2\2\2\u05db\u05dd\3\2\2\2\u05dc\u05da\3\2\2\2\u05dd\u05de\7B\2\2\u05de"+
		"\u05df\5\u00e4s\2\u05df\u05e1\3\2\2\2\u05e0\u05b3\3\2\2\2\u05e0\u05c3"+
		"\3\2\2\2\u05e0\u05ce\3\2\2\2\u05e1\u00b3\3\2\2\2\u05e2\u05e4\7\31\2\2"+
		"\u05e3\u05e5\5\u0148\u00a5\2\u05e4\u05e3\3\2\2\2\u05e4\u05e5\3\2\2\2\u05e5"+
		"\u00b5\3\2\2\2\u05e6\u05e8\7\24\2\2\u05e7\u05e9\5\u0148\u00a5\2\u05e8"+
		"\u05e7\3\2\2\2\u05e8\u05e9\3\2\2\2\u05e9\u00b7\3\2\2\2\u05ea\u05eb\7C"+
		"\2\2\u05eb\u05ec\5\u00f8}\2\u05ec\u00b9\3\2\2\2\u05ed\u05ef\7=\2\2\u05ee"+
		"\u05f0\5\u00c6d\2\u05ef\u05ee\3\2\2\2\u05ef\u05f0\3\2\2\2\u05f0\u05f4"+
		"\3\2\2\2\u05f1\u05f3\7\u008f\2\2\u05f2\u05f1\3\2\2\2\u05f3\u05f6\3\2\2"+
		"\2\u05f4\u05f2\3\2\2\2\u05f4\u05f5\3\2\2\2\u05f5\u05f7\3\2\2\2\u05f6\u05f4"+
		"\3\2\2\2\u05f7\u0601\5\u009cO\2\u05f8\u05fa\7\u008f\2\2\u05f9\u05f8\3"+
		"\2\2\2\u05fa\u05fd\3\2\2\2\u05fb\u05f9\3\2\2\2\u05fb\u05fc\3\2\2\2\u05fc"+
		"\u05fe\3\2\2\2\u05fd\u05fb\3\2\2\2\u05fe\u0600\5\u00c0a\2\u05ff\u05fb"+
		"\3\2\2\2\u0600\u0603\3\2\2\2\u0601\u05ff\3\2\2\2\u0601\u0602\3\2\2\2\u0602"+
		"\u060b\3\2\2\2\u0603\u0601\3\2\2\2\u0604\u0606\7\u008f\2\2\u0605\u0604"+
		"\3\2\2\2\u0606\u0609\3\2\2\2\u0607\u0605\3\2\2\2\u0607\u0608\3\2\2\2\u0608"+
		"\u060a\3\2\2\2\u0609\u0607\3\2\2\2\u060a\u060c\5\u00c4c\2\u060b\u0607"+
		"\3\2\2\2\u060b\u060c\3\2\2\2\u060c\u00bb\3\2\2\2\u060d\u060e\7\23\2\2"+
		"\u060e\u061d\5\u00f8}\2\u060f\u0611\7\u008f\2\2\u0610\u060f\3\2\2\2\u0611"+
		"\u0614\3\2\2\2\u0612\u0610\3\2\2\2\u0612\u0613\3\2\2\2\u0613\u0615\3\2"+
		"\2\2\u0614\u0612\3\2\2\2\u0615\u0619\t\6\2\2\u0616\u0618\7\u008f\2\2\u0617"+
		"\u0616\3\2\2\2\u0618\u061b\3\2\2\2\u0619\u0617\3\2\2\2\u0619\u061a\3\2"+
		"\2\2\u061a\u061c\3\2\2\2\u061b\u0619\3\2\2\2\u061c\u061e\5\u00f8}\2\u061d"+
		"\u0612\3\2\2\2\u061d\u061e\3\2\2\2\u061e\u00bd\3\2\2\2\u061f\u0658\5\u009c"+
		"O\2\u0620\u0658\5\u00acW\2\u0621\u0658\5\u00b2Z\2\u0622\u0658\5\u00ba"+
		"^\2\u0623\u0624\78\2\2\u0624\u0628\5\u00e4s\2\u0625\u0627\7\u008f\2\2"+
		"\u0626\u0625\3\2\2\2\u0627\u062a\3\2\2\2\u0628\u0626\3\2\2\2\u0628\u0629"+
		"\3\2\2\2\u0629\u062b\3\2\2\2\u062a\u0628\3\2\2\2\u062b\u062c\5\u009cO"+
		"\2\u062c\u0658\3\2\2\2\u062d\u062f\7\62\2\2\u062e\u0630\5\u00f8}\2\u062f"+
		"\u062e\3\2\2\2\u062f\u0630\3\2\2\2\u0630\u0658\3\2\2\2\u0631\u0632\7:"+
		"\2\2\u0632\u0658\5\u00f8}\2\u0633\u0658\5\u00b6\\\2\u0634\u0658\5\u00b4"+
		"[\2\u0635\u0636\6`\b\2\u0636\u0658\5\u00b8]\2\u0637\u0638\7C\2\2\u0638"+
		"\u063c\7\62\2\2\u0639\u063b\7\u008f\2\2\u063a\u0639\3\2\2\2\u063b\u063e"+
		"\3\2\2\2\u063c\u063a\3\2\2\2\u063c\u063d\3\2\2\2\u063d\u063f\3\2\2\2\u063e"+
		"\u063c\3\2\2\2\u063f\u0658\5\u00f8}\2\u0640\u0641\6`\t\2\u0641\u0645\7"+
		"\20\2\2\u0642\u0644\7\u008f\2\2\u0643\u0642\3\2\2\2\u0644\u0647\3\2\2"+
		"\2\u0645\u0643\3\2\2\2\u0645\u0646\3\2\2\2\u0646\u0648\3\2\2\2\u0647\u0645"+
		"\3\2\2\2\u0648\u0658\5\u00eex\2\u0649\u064a\5\u0148\u00a5\2\u064a\u064e"+
		"\7m\2\2\u064b\u064d\7\u008f\2\2\u064c\u064b\3\2\2\2\u064d\u0650\3\2\2"+
		"\2\u064e\u064c\3\2\2\2\u064e\u064f\3\2\2\2\u064f\u0651\3\2\2\2\u0650\u064e"+
		"\3\2\2\2\u0651\u0652\5\u00be`\2\u0652\u0658\3\2\2\2\u0653\u0658\5\u00bc"+
		"_\2\u0654\u0658\5\u00a0Q\2\u0655\u0658\5\u00eex\2\u0656\u0658\7d\2\2\u0657"+
		"\u061f\3\2\2\2\u0657\u0620\3\2\2\2\u0657\u0621\3\2\2\2\u0657\u0622\3\2"+
		"\2\2\u0657\u0623\3\2\2\2\u0657\u062d\3\2\2\2\u0657\u0631\3\2\2\2\u0657"+
		"\u0633\3\2\2\2\u0657\u0634\3\2\2\2\u0657\u0635\3\2\2\2\u0657\u0637\3\2"+
		"\2\2\u0657\u0640\3\2\2\2\u0657\u0649\3\2\2\2\u0657\u0653\3\2\2\2\u0657"+
		"\u0654\3\2\2\2\u0657\u0655\3\2\2\2\u0657\u0656\3\2\2\2\u0658\u00bf\3\2"+
		"\2\2\u0659\u065a\7\26\2\2\u065a\u065b\7^\2\2\u065b\u065d\5\34\17\2\u065c"+
		"\u065e\5\u00c2b\2\u065d\u065c\3\2\2\2\u065d\u065e\3\2\2\2\u065e\u065f"+
		"\3\2\2\2\u065f\u0660\5B\"\2\u0660\u0664\7_\2\2\u0661\u0663\7\u008f\2\2"+
		"\u0662\u0661\3\2\2\2\u0663\u0666\3\2\2\2\u0664\u0662\3\2\2\2\u0664\u0665"+
		"\3\2\2\2\u0665\u0667\3\2\2\2\u0666\u0664\3\2\2\2\u0667\u0668\5\u009cO"+
		"\2\u0668\u00c1\3\2\2\2\u0669\u066e\5l\67\2\u066a\u066b\7{\2\2\u066b\u066d"+
		"\5l\67\2\u066c\u066a\3\2\2\2\u066d\u0670\3\2\2\2\u066e\u066c\3\2\2\2\u066e"+
		"\u066f\3\2\2\2\u066f\u00c3\3\2\2\2\u0670\u066e\3\2\2\2\u0671\u0675\7 "+
		"\2\2\u0672\u0674\7\u008f\2\2\u0673\u0672\3\2\2\2\u0674\u0677\3\2\2\2\u0675"+
		"\u0673\3\2\2\2\u0675\u0676\3\2\2\2\u0676\u0678\3\2\2\2\u0677\u0675\3\2"+
		"\2\2\u0678\u0679\5\u009cO\2\u0679\u00c5\3\2\2\2\u067a\u067e\7^\2\2\u067b"+
		"\u067d\7\u008f\2\2\u067c\u067b\3\2\2\2\u067d\u0680\3\2\2\2\u067e\u067c"+
		"\3\2\2\2\u067e\u067f\3\2\2\2\u067f\u0681\3\2\2\2\u0680\u067e\3\2\2\2\u0681"+
		"\u0683\5\u00c8e\2\u0682\u0684\5\u014e\u00a8\2\u0683\u0682\3\2\2\2\u0683"+
		"\u0684\3\2\2\2\u0684\u0685\3\2\2\2\u0685\u0686\7_\2\2\u0686\u00c7\3\2"+
		"\2\2\u0687\u068d\5\u00caf\2\u0688\u0689\5\u014e\u00a8\2\u0689\u068a\5"+
		"\u00caf\2\u068a\u068c\3\2\2\2\u068b\u0688\3\2\2\2\u068c\u068f\3\2\2\2"+
		"\u068d\u068b\3\2\2\2\u068d\u068e\3\2\2\2\u068e\u00c9\3\2\2\2\u068f\u068d"+
		"\3\2\2\2\u0690\u0693\5\u00a0Q\2\u0691\u0693\5\u00f8}\2\u0692\u0690\3\2"+
		"\2\2\u0692\u0691\3\2\2\2\u0693\u00cb\3\2\2\2\u0694\u069e\5\u00ceh\2\u0695"+
		"\u0697\7\u008f\2\2\u0696\u0695\3\2\2\2\u0697\u069a\3\2\2\2\u0698\u0696"+
		"\3\2\2\2\u0698\u0699\3\2\2\2\u0699\u069b\3\2\2\2\u069a\u0698\3\2\2\2\u069b"+
		"\u069d\5\u00ceh\2\u069c\u0698\3\2\2\2\u069d\u06a0\3\2\2\2\u069e\u069c"+
		"\3\2\2\2\u069e\u069f\3\2\2\2\u069f\u06a4\3\2\2\2\u06a0\u069e\3\2\2\2\u06a1"+
		"\u06a3\7\u008f\2\2\u06a2\u06a1\3\2\2\2\u06a3\u06a6\3\2\2\2\u06a4\u06a2"+
		"\3\2\2\2\u06a4\u06a5\3\2\2\2\u06a5\u06a9\3\2\2\2\u06a6\u06a4\3\2\2\2\u06a7"+
		"\u06aa\5\u0088E\2\u06a8\u06aa\6g\n\2\u06a9\u06a7\3\2\2\2\u06a9\u06a8\3"+
		"\2\2\2\u06aa\u00cd\3\2\2\2\u06ab\u06ac\7\25\2\2\u06ac\u06ad\5\u00f8}\2"+
		"\u06ad\u06ae\7m\2\2\u06ae\u06b2\3\2\2\2\u06af\u06b0\7\32\2\2\u06b0\u06b2"+
		"\7m\2\2\u06b1\u06ab\3\2\2\2\u06b1\u06af\3\2\2\2\u06b2\u00cf\3\2\2\2\u06b3"+
		"\u06b6\5\u00d2j\2\u06b4\u06b6\5\u00d6l\2\u06b5\u06b3\3\2\2\2\u06b5\u06b4"+
		"\3\2\2\2\u06b6\u00d1\3\2\2\2\u06b7\u06b8\5\u00d4k\2\u06b8\u06b9\7e\2\2"+
		"\u06b9\u06bb\3\2\2\2\u06ba\u06b7\3\2\2\2\u06ba\u06bb\3\2\2\2\u06bb\u06bc"+
		"\3\2\2\2\u06bc\u06be\5\34\17\2\u06bd\u06bf\5F$\2\u06be\u06bd\3\2\2\2\u06be"+
		"\u06bf\3\2\2\2\u06bf\u06c0\3\2\2\2\u06c0\u06c1\5B\"\2\u06c1\u06c2\t\7"+
		"\2\2\u06c2\u06c3\5\u00f8}\2\u06c3\u00d3\3\2\2\2\u06c4\u06c6\t\b\2\2\u06c5"+
		"\u06c4\3\2\2\2\u06c5\u06c6\3\2\2\2\u06c6\u06c7\3\2\2\2\u06c7\u06c8\5\u0148"+
		"\u00a5\2\u06c8\u00d5\3\2\2\2\u06c9\u06cb\5\u00d8m\2\u06ca\u06c9\3\2\2"+
		"\2\u06ca\u06cb\3\2\2\2\u06cb\u06cc\3\2\2\2\u06cc\u06ce\7d\2\2\u06cd\u06cf"+
		"\5\u00f8}\2\u06ce\u06cd\3\2\2\2\u06ce\u06cf\3\2\2\2\u06cf\u06d0\3\2\2"+
		"\2\u06d0\u06d2\7d\2\2\u06d1\u06d3\5\u00dan\2\u06d2\u06d1\3\2\2\2\u06d2"+
		"\u06d3\3\2\2\2\u06d3\u00d7\3\2\2\2\u06d4\u06d7\5\u00a0Q\2\u06d5\u06d7"+
		"\5\u00e6t\2\u06d6\u06d4\3\2\2\2\u06d6\u06d5\3\2\2\2\u06d7\u00d9\3\2\2"+
		"\2\u06d8\u06d9\5\u00e6t\2\u06d9\u00db\3\2\2\2\u06da\u06db\7^\2\2\u06db"+
		"\u06dc\5\u00dep\2\u06dc\u06dd\7_\2\2\u06dd\u00dd\3\2\2\2\u06de\u06e9\5"+
		"F$\2\u06df\u06e3\7z\2\2\u06e0\u06e2\7\u008f\2\2\u06e1\u06e0\3\2\2\2\u06e2"+
		"\u06e5\3\2\2\2\u06e3\u06e1\3\2\2\2\u06e3\u06e4\3\2\2\2\u06e4\u06e6\3\2"+
		"\2\2\u06e5\u06e3\3\2\2\2\u06e6\u06e8\5F$\2\u06e7\u06df\3\2\2\2\u06e8\u06eb"+
		"\3\2\2\2\u06e9\u06e7\3\2\2\2\u06e9\u06ea\3\2\2\2\u06ea\u00df\3\2\2\2\u06eb"+
		"\u06e9\3\2\2\2\u06ec\u06ef\5\u00dco\2\u06ed\u06ef\5F$\2\u06ee\u06ec\3"+
		"\2\2\2\u06ee\u06ed\3\2\2\2\u06ef\u00e1\3\2\2\2\u06f0\u06f1\5\u00e4s\2"+
		"\u06f1\u00e3\3\2\2\2\u06f2\u06f3\7^\2\2\u06f3\u06f4\5\u00ecw\2\u06f4\u06f5"+
		"\7_\2\2\u06f5\u00e5\3\2\2\2\u06f6\u0701\5\u00e8u\2\u06f7\u06fb\7e\2\2"+
		"\u06f8\u06fa\7\u008f\2\2\u06f9\u06f8\3\2\2\2\u06fa\u06fd\3\2\2\2\u06fb"+
		"\u06f9\3\2\2\2\u06fb\u06fc\3\2\2\2\u06fc\u06fe\3\2\2\2\u06fd\u06fb\3\2"+
		"\2\2\u06fe\u0700\5\u00e8u\2\u06ff\u06f7\3\2\2\2\u0700\u0703\3\2\2\2\u0701"+
		"\u06ff\3\2\2\2\u0701\u0702\3\2\2\2\u0702\u00e7\3\2\2\2\u0703\u0701\3\2"+
		"\2\2\u0704\u0706\7x\2\2\u0705\u0704\3\2\2\2\u0705\u0706\3\2\2\2\u0706"+
		"\u0707\3\2\2\2\u0707\u0708\5\u00f8}\2\u0708\u00e9\3\2\2\2\u0709\u070c"+
		"\5\u00f8}\2\u070a\u070c\5z>\2\u070b\u0709\3\2\2\2\u070b\u070a\3\2\2\2"+
		"\u070c\u00eb\3\2\2\2\u070d\u0710\5\u00eex\2\u070e\u0710\5z>\2\u070f\u070d"+
		"\3\2\2\2\u070f\u070e\3\2\2\2\u0710\u00ed\3\2\2\2\u0711\u0712\5\u00fc\177"+
		"\2\u0712\u00ef\3\2\2\2\u0713\u0715\5\u0100\u0081\2\u0714\u0716\t\t\2\2"+
		"\u0715\u0714\3\2\2\2\u0715\u0716\3\2\2\2\u0716\u00f1\3\2\2\2\u0717\u0718"+
		"\7\67\2\2\u0718\u071c\5\u00e4s\2\u0719\u071b\7\u008f\2\2\u071a\u0719\3"+
		"\2\2\2\u071b\u071e\3\2\2\2\u071c\u071a\3\2\2\2\u071c\u071d\3\2\2\2\u071d"+
		"\u071f\3\2\2\2\u071e\u071c\3\2\2\2\u071f\u0723\7`\2\2\u0720\u0722\7\u008f"+
		"\2\2\u0721\u0720\3\2\2\2\u0722\u0725\3\2\2\2\u0723\u0721\3\2\2\2\u0723"+
		"\u0724\3\2\2\2\u0724\u0729\3\2\2\2\u0725\u0723\3\2\2\2\u0726\u0728\5\u00f4"+
		"{\2\u0727\u0726\3\2\2\2\u0728\u072b\3\2\2\2\u0729\u0727\3\2\2\2\u0729"+
		"\u072a\3\2\2\2\u072a\u072f\3\2\2\2\u072b\u0729\3\2\2\2\u072c\u072e\7\u008f"+
		"\2\2\u072d\u072c\3\2\2\2\u072e\u0731\3\2\2\2\u072f\u072d\3\2\2\2\u072f"+
		"\u0730\3\2\2\2\u0730\u0732\3\2\2\2\u0731\u072f\3\2\2\2\u0732\u0733\7a"+
		"\2\2\u0733\u00f3\3\2\2\2\u0734\u0738\5\u00f6|\2\u0735\u0737\7\u008f\2"+
		"\2\u0736\u0735\3\2\2\2\u0737\u073a\3\2\2\2\u0738\u0736\3\2\2\2\u0738\u0739"+
		"\3\2\2\2\u0739\u073c\3\2\2\2\u073a\u0738\3\2\2\2\u073b\u0734\3\2\2\2\u073c"+
		"\u073d\3\2\2\2\u073d\u073b\3\2\2\2\u073d\u073e\3\2\2\2\u073e\u073f\3\2"+
		"\2\2\u073f\u0740\5\u0088E\2\u0740\u00f5\3\2\2\2\u0741\u0742\7\25\2\2\u0742"+
		"\u0745\5\u00e6t\2\u0743\u0745\7\32\2\2\u0744\u0741\3\2\2\2\u0744\u0743"+
		"\3\2\2\2\u0745\u0746\3\2\2\2\u0746\u0747\t\n\2\2\u0747\u00f7\3\2\2\2\u0748"+
		"\u0749\b}\1\2\u0749\u074a\5\u00dco\2\u074a\u074b\5\u00fa~\2\u074b\u0784"+
		"\3\2\2\2\u074c\u0750\7\16\2\2\u074d\u074f\7\u008f\2\2\u074e\u074d\3\2"+
		"\2\2\u074f\u0752\3\2\2\2\u0750\u074e\3\2\2\2\u0750\u0751\3\2\2\2\u0751"+
		"\u0753\3\2\2\2\u0752\u0750\3\2\2\2\u0753\u0754\b}\1\2\u0754\u0755\5\u0084"+
		"C\2\u0755\u0756\b}\1\2\u0756\u0784\3\2\2\2\u0757\u0758\6}\13\2\u0758\u0759"+
		"\7\17\2\2\u0759\u0764\5\u00f8}\2\u075a\u075e\7e\2\2\u075b\u075d\7\u008f"+
		"\2\2\u075c\u075b\3\2\2\2\u075d\u0760\3\2\2\2\u075e\u075c\3\2\2\2\u075e"+
		"\u075f\3\2\2\2\u075f\u0761\3\2\2\2\u0760\u075e\3\2\2\2\u0761\u0763\5\u00f8"+
		"}\2\u0762\u075a\3\2\2\2\u0763\u0766\3\2\2\2\u0764\u0762\3\2\2\2\u0764"+
		"\u0765\3\2\2\2\u0765\u0784\3\2\2\2\u0766\u0764\3\2\2\2\u0767\u0784\5\u00f0"+
		"y\2\u0768\u0784\5\u00f2z\2\u0769\u076d\t\13\2\2\u076a\u076c\7\u008f\2"+
		"\2\u076b\u076a\3\2\2\2\u076c\u076f\3\2\2\2\u076d\u076b\3\2\2\2\u076d\u076e"+
		"\3\2\2\2\u076e\u0770\3\2\2\2\u076f\u076d\3\2\2\2\u0770\u0784\5\u00f8}"+
		"\27\u0771\u0772\t\f\2\2\u0772\u0784\5\u00f8}\25\u0773\u0777\5\u00aaV\2"+
		"\u0774\u0776\7\u008f\2\2\u0775\u0774\3\2\2\2\u0776\u0779\3\2\2\2\u0777"+
		"\u0775\3\2\2\2\u0777\u0778\3\2\2\2\u0778\u077a\3\2\2\2\u0779\u0777\3\2"+
		"\2\2\u077a\u077e\7g\2\2\u077b\u077d\7\u008f\2\2\u077c\u077b\3\2\2\2\u077d"+
		"\u0780\3\2\2\2\u077e\u077c\3\2\2\2\u077e\u077f\3\2\2\2\u077f\u0781\3\2"+
		"\2\2\u0780\u077e\3\2\2\2\u0781\u0782\5\u00eex\2\u0782\u0784\3\2\2\2\u0783"+
		"\u0748\3\2\2\2\u0783\u074c\3\2\2\2\u0783\u0757\3\2\2\2\u0783\u0767\3\2"+
		"\2\2\u0783\u0768\3\2\2\2\u0783\u0769\3\2\2\2\u0783\u0771\3\2\2\2\u0783"+
		"\u0773\3\2\2\2\u0784\u08ab\3\2\2\2\u0785\u0786\f\26\2\2\u0786\u078a\7"+
		"U\2\2\u0787\u0789\7\u008f\2\2\u0788\u0787\3\2\2\2\u0789\u078c\3\2\2\2"+
		"\u078a\u0788\3\2\2\2\u078a\u078b\3\2\2\2\u078b\u078d\3\2\2\2\u078c\u078a"+
		"\3\2\2\2\u078d\u08aa\5\u00f8}\27\u078e\u0792\f\24\2\2\u078f\u0791\7\u008f"+
		"\2\2\u0790\u078f\3\2\2\2\u0791\u0794\3\2\2\2\u0792\u0790\3\2\2\2\u0792"+
		"\u0793\3\2\2\2\u0793\u0795\3\2\2\2\u0794\u0792\3\2\2\2\u0795\u0799\t\r"+
		"\2\2\u0796\u0798\7\u008f\2\2\u0797\u0796\3\2\2\2\u0798\u079b\3\2\2\2\u0799"+
		"\u0797\3\2\2\2\u0799\u079a\3\2\2\2\u079a\u079c\3\2\2\2\u079b\u0799\3\2"+
		"\2\2\u079c\u08aa\5\u00f8}\25\u079d\u079e\f\23\2\2\u079e\u07a2\t\16\2\2"+
		"\u079f\u07a1\7\u008f\2\2\u07a0\u079f\3\2\2\2\u07a1\u07a4\3\2\2\2\u07a2"+
		"\u07a0\3\2\2\2\u07a2\u07a3\3\2\2\2\u07a3\u07a5\3\2\2\2\u07a4\u07a2\3\2"+
		"\2\2\u07a5\u08aa\5\u00f8}\24\u07a6\u07aa\f\22\2\2\u07a7\u07a9\7\u008f"+
		"\2\2\u07a8\u07a7\3\2\2\2\u07a9\u07ac\3\2\2\2\u07aa\u07a8\3\2\2\2\u07aa"+
		"\u07ab\3\2\2\2\u07ab\u07b7\3\2\2\2\u07ac\u07aa\3\2\2\2\u07ad\u07ae\7i"+
		"\2\2\u07ae\u07b5\7i\2\2\u07af\u07b0\7h\2\2\u07b0\u07b1\7h\2\2\u07b1\u07b5"+
		"\7h\2\2\u07b2\u07b3\7h\2\2\u07b3\u07b5\7h\2\2\u07b4\u07ad\3\2\2\2\u07b4"+
		"\u07af\3\2\2\2\u07b4\u07b2\3\2\2\2\u07b5\u07b8\3\2\2\2\u07b6\u07b8\t\17"+
		"\2\2\u07b7\u07b4\3\2\2\2\u07b7\u07b6\3\2\2\2\u07b8\u07bc\3\2\2\2\u07b9"+
		"\u07bb\7\u008f\2\2\u07ba\u07b9\3\2\2\2\u07bb\u07be\3\2\2\2\u07bc\u07ba"+
		"\3\2\2\2\u07bc\u07bd\3\2\2\2\u07bd\u07bf\3\2\2\2\u07be\u07bc\3\2\2\2\u07bf"+
		"\u08aa\5\u00f8}\23\u07c0\u07c4\f\16\2\2\u07c1\u07c3\7\u008f\2\2\u07c2"+
		"\u07c1\3\2\2\2\u07c3\u07c6\3\2\2\2\u07c4\u07c2\3\2\2\2\u07c4\u07c5\3\2"+
		"\2\2\u07c5\u07c7\3\2\2\2\u07c6\u07c4\3\2\2\2\u07c7\u07cb\t\20\2\2\u07c8"+
		"\u07ca\7\u008f\2\2\u07c9\u07c8\3\2\2\2\u07ca\u07cd\3\2\2\2\u07cb\u07c9"+
		"\3\2\2\2\u07cb\u07cc\3\2\2\2\u07cc\u07ce\3\2\2\2\u07cd\u07cb\3\2\2\2\u07ce"+
		"\u08aa\5\u00f8}\17\u07cf\u07d3\f\r\2\2\u07d0\u07d2\7\u008f\2\2\u07d1\u07d0"+
		"\3\2\2\2\u07d2\u07d5\3\2\2\2\u07d3\u07d1\3\2\2\2\u07d3\u07d4\3\2\2\2\u07d4"+
		"\u07d6\3\2\2\2\u07d5\u07d3\3\2\2\2\u07d6\u07da\t\21\2\2\u07d7\u07d9\7"+
		"\u008f\2\2\u07d8\u07d7\3\2\2\2\u07d9\u07dc\3\2\2\2\u07da\u07d8\3\2\2\2"+
		"\u07da\u07db\3\2\2\2\u07db\u07dd\3\2\2\2\u07dc\u07da\3\2\2\2\u07dd\u08aa"+
		"\5\u00f8}\16\u07de\u07e2\f\f\2\2\u07df\u07e1\7\u008f\2\2\u07e0\u07df\3"+
		"\2\2\2\u07e1\u07e4\3\2\2\2\u07e2\u07e0\3\2\2\2\u07e2\u07e3\3\2\2\2\u07e3"+
		"\u07e5\3\2\2\2\u07e4\u07e2\3\2\2\2\u07e5\u07e9\t\22\2\2\u07e6\u07e8\7"+
		"\u008f\2\2\u07e7\u07e6\3\2\2\2\u07e8\u07eb\3\2\2\2\u07e9\u07e7\3\2\2\2"+
		"\u07e9\u07ea\3\2\2\2\u07ea\u07ec\3\2\2\2\u07eb\u07e9\3\2\2\2\u07ec\u08aa"+
		"\5\u00f8}\r\u07ed\u07f1\f\13\2\2\u07ee\u07f0\7\u008f\2\2\u07ef\u07ee\3"+
		"\2\2\2\u07f0\u07f3\3\2\2\2\u07f1\u07ef\3\2\2\2\u07f1\u07f2\3\2\2\2\u07f2"+
		"\u07f4\3\2\2\2\u07f3\u07f1\3\2\2\2\u07f4\u07f8\7z\2\2\u07f5\u07f7\7\u008f"+
		"\2\2\u07f6\u07f5\3\2\2\2\u07f7\u07fa\3\2\2\2\u07f8\u07f6\3\2\2\2\u07f8"+
		"\u07f9\3\2\2\2\u07f9\u07fb\3\2\2\2\u07fa\u07f8\3\2\2\2\u07fb\u08aa\5\u00f8"+
		"}\f\u07fc\u0800\f\n\2\2\u07fd\u07ff\7\u008f\2\2\u07fe\u07fd\3\2\2\2\u07ff"+
		"\u0802\3\2\2\2\u0800\u07fe\3\2\2\2\u0800\u0801\3\2\2\2\u0801\u0803\3\2"+
		"\2\2\u0802\u0800\3\2\2\2\u0803\u0807\7|\2\2\u0804\u0806\7\u008f\2\2\u0805"+
		"\u0804\3\2\2\2\u0806\u0809\3\2\2\2\u0807\u0805\3\2\2\2\u0807\u0808\3\2"+
		"\2\2\u0808\u080a\3\2\2\2\u0809\u0807\3\2\2\2\u080a\u08aa\5\u00f8}\13\u080b"+
		"\u080f\f\t\2\2\u080c\u080e\7\u008f\2\2\u080d\u080c\3\2\2\2\u080e\u0811"+
		"\3\2\2\2\u080f\u080d\3\2\2\2\u080f\u0810\3\2\2\2\u0810\u0812\3\2\2\2\u0811"+
		"\u080f\3\2\2\2\u0812\u0816\7{\2\2\u0813\u0815\7\u008f\2\2\u0814\u0813"+
		"\3\2\2\2\u0815\u0818\3\2\2\2\u0816\u0814\3\2\2\2\u0816\u0817\3\2\2\2\u0817"+
		"\u0819\3\2\2\2\u0818\u0816\3\2\2\2\u0819\u08aa\5\u00f8}\n\u081a\u081e"+
		"\f\b\2\2\u081b\u081d\7\u008f\2\2\u081c\u081b\3\2\2\2\u081d\u0820\3\2\2"+
		"\2\u081e\u081c\3\2\2\2\u081e\u081f\3\2\2\2\u081f\u0821\3\2\2\2\u0820\u081e"+
		"\3\2\2\2\u0821\u0825\7r\2\2\u0822\u0824\7\u008f\2\2\u0823\u0822\3\2\2"+
		"\2\u0824\u0827\3\2\2\2\u0825\u0823\3\2\2\2\u0825\u0826\3\2\2\2\u0826\u0828"+
		"\3\2\2\2\u0827\u0825\3\2\2\2\u0828\u08aa\5\u00f8}\t\u0829\u082d\f\7\2"+
		"\2\u082a\u082c\7\u008f\2\2\u082b\u082a\3\2\2\2\u082c\u082f\3\2\2\2\u082d"+
		"\u082b\3\2\2\2\u082d\u082e\3\2\2\2\u082e\u0830\3\2\2\2\u082f\u082d\3\2"+
		"\2\2\u0830\u0834\7s\2\2\u0831\u0833\7\u008f\2\2\u0832\u0831\3\2\2\2\u0833"+
		"\u0836\3\2\2\2\u0834\u0832\3\2\2\2\u0834\u0835\3\2\2\2\u0835\u0837\3\2"+
		"\2\2\u0836\u0834\3\2\2\2\u0837\u08aa\5\u00f8}\b\u0838\u083c\f\6\2\2\u0839"+
		"\u083b\7\u008f\2\2\u083a\u0839\3\2\2\2\u083b\u083e\3\2\2\2\u083c\u083a"+
		"\3\2\2\2\u083c\u083d\3\2\2\2\u083d\u083f\3\2\2\2\u083e\u083c\3\2\2\2\u083f"+
		"\u0843\7Y\2\2\u0840\u0842\7\u008f\2\2\u0841\u0840\3\2\2\2\u0842\u0845"+
		"\3\2\2\2\u0843\u0841\3\2\2\2\u0843\u0844\3\2\2\2\u0844\u0846\3\2\2\2\u0845"+
		"\u0843\3\2\2\2\u0846\u08aa\5\u00f8}\6\u0847\u084b\f\5\2\2\u0848\u084a"+
		"\7\u008f\2\2\u0849\u0848\3\2\2\2\u084a\u084d\3\2\2\2\u084b\u0849\3\2\2"+
		"\2\u084b\u084c\3\2\2\2\u084c\u086a\3\2\2\2\u084d\u084b\3\2\2\2\u084e\u0852"+
		"\7l\2\2\u084f\u0851\7\u008f\2\2\u0850\u084f\3\2\2\2\u0851\u0854\3\2\2"+
		"\2\u0852\u0850\3\2\2\2\u0852\u0853\3\2\2\2\u0853\u0855\3\2\2\2\u0854\u0852"+
		"\3\2\2\2\u0855\u0859\5\u00f8}\2\u0856\u0858\7\u008f\2\2\u0857\u0856\3"+
		"\2\2\2\u0858\u085b\3\2\2\2\u0859\u0857\3\2\2\2\u0859\u085a\3\2\2\2\u085a"+
		"\u085c\3\2\2\2\u085b\u0859\3\2\2\2\u085c\u0860\7m\2\2\u085d\u085f\7\u008f"+
		"\2\2\u085e\u085d\3\2\2\2\u085f\u0862\3\2\2\2\u0860\u085e\3\2\2\2\u0860"+
		"\u0861\3\2\2\2\u0861\u086b\3\2\2\2\u0862\u0860\3\2\2\2\u0863\u0867\7P"+
		"\2\2\u0864\u0866\7\u008f\2\2\u0865\u0864\3\2\2\2\u0866\u0869\3\2\2\2\u0867"+
		"\u0865\3\2\2\2\u0867\u0868\3\2\2\2\u0868\u086b\3\2\2\2\u0869\u0867\3\2"+
		"\2\2\u086a\u084e\3\2\2\2\u086a\u0863\3\2\2\2\u086b\u086c\3\2\2\2\u086c"+
		"\u08aa\5\u00f8}\5\u086d\u0871\f\21\2\2\u086e\u0870\7\u008f\2\2\u086f\u086e"+
		"\3\2\2\2\u0870\u0873\3\2\2\2\u0871\u086f\3\2\2\2\u0871\u0872\3\2\2\2\u0872"+
		"\u0874\3\2\2\2\u0873\u0871\3\2\2\2\u0874\u0878\7&\2\2\u0875\u0877\7\u008f"+
		"\2\2\u0876\u0875\3\2\2\2\u0877\u087a\3\2\2\2\u0878\u0876\3\2\2\2\u0878"+
		"\u0879\3\2\2\2\u0879\u087b\3\2\2\2\u087a\u0878\3\2\2\2\u087b\u08aa\5L"+
		"\'\2\u087c\u0880\f\20\2\2\u087d\u087f\7\u008f\2\2\u087e\u087d\3\2\2\2"+
		"\u087f\u0882\3\2\2\2\u0880\u087e\3\2\2\2\u0880\u0881\3\2\2\2\u0881\u0883"+
		"\3\2\2\2\u0882\u0880\3\2\2\2\u0883\u0887\7\\\2\2\u0884\u0886\7\u008f\2"+
		"\2\u0885\u0884\3\2\2\2\u0886\u0889\3\2\2\2\u0887\u0885\3\2\2\2\u0887\u0888"+
		"\3\2\2\2\u0888\u088a\3\2\2\2\u0889\u0887\3\2\2\2\u088a\u08aa\5N(\2\u088b"+
		"\u088f\f\17\2\2\u088c\u088e\7\u008f\2\2\u088d\u088c\3\2\2\2\u088e\u0891"+
		"\3\2\2\2\u088f\u088d\3\2\2\2\u088f\u0890\3\2\2\2\u0890\u0892\3\2\2\2\u0891"+
		"\u088f\3\2\2\2\u0892\u0896\7\t\2\2\u0893\u0895\7\u008f\2\2\u0894\u0893"+
		"\3\2\2\2\u0895\u0898\3\2\2\2\u0896\u0894\3\2\2\2\u0896\u0897\3\2\2\2\u0897"+
		"\u0899\3\2\2\2\u0898\u0896\3\2\2\2\u0899\u08aa\5\u00e0q\2\u089a\u089e"+
		"\f\3\2\2\u089b\u089d\7\u008f\2\2\u089c\u089b\3\2\2\2\u089d\u08a0\3\2\2"+
		"\2\u089e\u089c\3\2\2\2\u089e\u089f\3\2\2\2\u089f\u08a1\3\2\2\2\u08a0\u089e"+
		"\3\2\2\2\u08a1\u08a5\t\23\2\2\u08a2\u08a4\7\u008f\2\2\u08a3\u08a2\3\2"+
		"\2\2\u08a4\u08a7\3\2\2\2\u08a5\u08a3\3\2\2\2\u08a5\u08a6\3\2\2\2\u08a6"+
		"\u08a8\3\2\2\2\u08a7\u08a5\3\2\2\2\u08a8\u08aa\5\u00ecw\2\u08a9\u0785"+
		"\3\2\2\2\u08a9\u078e\3\2\2\2\u08a9\u079d\3\2\2\2\u08a9\u07a6\3\2\2\2\u08a9"+
		"\u07c0\3\2\2\2\u08a9\u07cf\3\2\2\2\u08a9\u07de\3\2\2\2\u08a9\u07ed\3\2"+
		"\2\2\u08a9\u07fc\3\2\2\2\u08a9\u080b\3\2\2\2\u08a9\u081a\3\2\2\2\u08a9"+
		"\u0829\3\2\2\2\u08a9\u0838\3\2\2\2\u08a9\u0847\3\2\2\2\u08a9\u086d\3\2"+
		"\2\2\u08a9\u087c\3\2\2\2\u08a9\u088b\3\2\2\2\u08a9\u089a\3\2\2\2\u08aa"+
		"\u08ad\3\2\2\2\u08ab\u08a9\3\2\2\2\u08ab\u08ac\3\2\2\2\u08ac\u00f9\3\2"+
		"\2\2\u08ad\u08ab\3\2\2\2\u08ae\u08af\5\u00dco\2\u08af\u08b0\5\u00fa~\2"+
		"\u08b0\u08d8\3\2\2\2\u08b1\u08b5\7\16\2\2\u08b2\u08b4\7\u008f\2\2\u08b3"+
		"\u08b2\3\2\2\2\u08b4\u08b7\3\2\2\2\u08b5\u08b3\3\2\2\2\u08b5\u08b6\3\2"+
		"\2\2\u08b6\u08b8\3\2\2\2\u08b7\u08b5\3\2\2\2\u08b8\u08b9\b~\1\2\u08b9"+
		"\u08ba\5\u0084C\2\u08ba\u08bb\b~\1\2\u08bb\u08d8\3\2\2\2\u08bc\u08bd\6"+
		"~\36\2\u08bd\u08be\7\17\2\2\u08be\u08c9\5\u00f8}\2\u08bf\u08c3\7e\2\2"+
		"\u08c0\u08c2\7\u008f\2\2\u08c1\u08c0\3\2\2\2\u08c2\u08c5\3\2\2\2\u08c3"+
		"\u08c1\3\2\2\2\u08c3\u08c4\3\2\2\2\u08c4\u08c6\3\2\2\2\u08c5\u08c3\3\2"+
		"\2\2\u08c6\u08c8\5\u00f8}\2\u08c7\u08bf\3\2\2\2\u08c8\u08cb\3\2\2\2\u08c9"+
		"\u08c7\3\2\2\2\u08c9\u08ca\3\2\2\2\u08ca\u08d8\3\2\2\2\u08cb\u08c9\3\2"+
		"\2\2\u08cc\u08d8\5\u00f0y\2\u08cd\u08d1\t\13\2\2\u08ce\u08d0\7\u008f\2"+
		"\2\u08cf\u08ce\3\2\2\2\u08d0\u08d3\3\2\2\2\u08d1\u08cf\3\2\2\2\u08d1\u08d2"+
		"\3\2\2\2\u08d2\u08d4\3\2\2\2\u08d3\u08d1\3\2\2\2\u08d4\u08d8\5\u00fa~"+
		"\2\u08d5\u08d6\t\f\2\2\u08d6\u08d8\5\u00fa~\2\u08d7\u08ae\3\2\2\2\u08d7"+
		"\u08b1\3\2\2\2\u08d7\u08bc\3\2\2\2\u08d7\u08cc\3\2\2\2\u08d7\u08cd\3\2"+
		"\2\2\u08d7\u08d5\3\2\2\2\u08d8\u00fb\3\2\2\2\u08d9\u08dd\5\u00f8}\2\u08da"+
		"\u08db\6\177\37\3\u08db\u08de\5\u013a\u009e\2\u08dc\u08de\3\2\2\2\u08dd"+
		"\u08da\3\2\2\2\u08dd\u08dc\3\2\2\2\u08de\u08e2\3\2\2\2\u08df\u08e1\5\u00fe"+
		"\u0080\2\u08e0\u08df\3\2\2\2\u08e1\u08e4\3\2\2\2\u08e2\u08e0\3\2\2\2\u08e2"+
		"\u08e3\3\2\2\2\u08e3\u00fd\3\2\2\2\u08e4\u08e2\3\2\2\2\u08e5\u08ec\5\u0112"+
		"\u008a\2\u08e6\u08e8\5\u0102\u0082\2\u08e7\u08e6\3\2\2\2\u08e8\u08e9\3"+
		"\2\2\2\u08e9\u08e7\3\2\2\2\u08e9\u08ea\3\2\2\2\u08ea\u08ed\3\2\2\2\u08eb"+
		"\u08ed\5\u013a\u009e\2\u08ec\u08e7\3\2\2\2\u08ec\u08eb\3\2\2\2\u08ec\u08ed"+
		"\3\2\2\2\u08ed\u00ff\3\2\2\2\u08ee\u08f2\5\u010c\u0087\2\u08ef\u08f0\6"+
		"\u0081 \2\u08f0\u08f2\7\64\2\2\u08f1\u08ee\3\2\2\2\u08f1\u08ef\3\2\2\2"+
		"\u08f2\u08f8\3\2\2\2\u08f3\u08f4\5\u0102\u0082\2\u08f4\u08f5\b\u0081\1"+
		"\2\u08f5\u08f7\3\2\2\2\u08f6\u08f3\3\2\2\2\u08f7\u08fa\3\2\2\2\u08f8\u08f6"+
		"\3\2\2\2\u08f8\u08f9\3\2\2\2\u08f9\u0101\3\2\2\2\u08fa\u08f8\3\2\2\2\u08fb"+
		"\u08fd\7\u008f\2\2\u08fc\u08fb\3\2\2\2\u08fd\u0900\3\2\2\2\u08fe\u08fc"+
		"\3\2\2\2\u08fe\u08ff\3\2\2\2\u08ff\u0940\3\2\2\2\u0900\u08fe\3\2\2\2\u0901"+
		"\u0905\7f\2\2\u0902\u0904\7\u008f\2\2\u0903\u0902\3\2\2\2\u0904\u0907"+
		"\3\2\2\2\u0905\u0903\3\2\2\2\u0905";
	private static final String _serializedATNSegment1 =
		"\u0906\3\2\2\2\u0906\u0916\3\2\2\2\u0907\u0905\3\2\2\2\u0908\u090a\7*"+
		"\2\2\u0909\u090b\5\u0134\u009b\2\u090a\u0909\3\2\2\2\u090a\u090b\3\2\2"+
		"\2\u090b\u090c\3\2\2\2\u090c\u090d\5\u0128\u0095\2\u090d\u090e\b\u0082"+
		"\1\2\u090e\u0917\3\2\2\2\u090f\u0912\7\u008c\2\2\u0910\u0912\5\u0134\u009b"+
		"\2\u0911\u090f\3\2\2\2\u0911\u0910\3\2\2\2\u0911\u0912\3\2\2\2\u0912\u0913"+
		"\3\2\2\2\u0913\u0914\5\u0104\u0083\2\u0914\u0915\b\u0082\1\2\u0915\u0917"+
		"\3\2\2\2\u0916\u0908\3\2\2\2\u0916\u0911\3\2\2\2\u0917\u0941\3\2\2\2\u0918"+
		"\u091c\t\24\2\2\u0919\u091b\7\u008f\2\2\u091a\u0919\3\2\2\2\u091b\u091e"+
		"\3\2\2\2\u091c\u091a\3\2\2\2\u091c\u091d\3\2\2\2\u091d\u0921\3\2\2\2\u091e"+
		"\u091c\3\2\2\2\u091f\u0922\7\u008c\2\2\u0920\u0922\5\u0134\u009b\2\u0921"+
		"\u091f\3\2\2\2\u0921\u0920\3\2\2\2\u0921\u0922\3\2\2\2\u0922\u0923\3\2"+
		"\2\2\u0923\u0924\5\u0104\u0083\2\u0924\u0925\b\u0082\1\2\u0925\u0941\3"+
		"\2\2\2\u0926\u092a\7Q\2\2\u0927\u0929\7\u008f\2\2\u0928\u0927\3\2\2\2"+
		"\u0929\u092c\3\2\2\2\u092a\u0928\3\2\2\2\u092a\u092b\3\2\2\2\u092b\u092d"+
		"\3\2\2\2\u092c\u092a\3\2\2\2\u092d\u092e\5\u0104\u0083\2\u092e\u092f\b"+
		"\u0082\1\2\u092f\u0941\3\2\2\2\u0930\u0934\7R\2\2\u0931\u0933\7\u008f"+
		"\2\2\u0932\u0931\3\2\2\2\u0933\u0936\3\2\2\2\u0934\u0932\3\2\2\2\u0934"+
		"\u0935\3\2\2\2\u0935\u0938\3\2\2\2\u0936\u0934\3\2\2\2\u0937\u0939\5\u0134"+
		"\u009b\2\u0938\u0937\3\2\2\2\u0938\u0939\3\2\2\2\u0939\u093a\3\2\2\2\u093a"+
		"\u093b\5\u0104\u0083\2\u093b\u093c\b\u0082\1\2\u093c\u0941\3\2\2\2\u093d"+
		"\u093e\5\u0084C\2\u093e\u093f\b\u0082\1\2\u093f\u0941\3\2\2\2\u0940\u0901"+
		"\3\2\2\2\u0940\u0918\3\2\2\2\u0940\u0926\3\2\2\2\u0940\u0930\3\2\2\2\u0940"+
		"\u093d\3\2\2\2\u0941\u094c\3\2\2\2\u0942\u0943\5\u0138\u009d\2\u0943\u0944"+
		"\b\u0082\1\2\u0944\u094c\3\2\2\2\u0945\u0946\5\u0108\u0085\2\u0946\u0947"+
		"\b\u0082\1\2\u0947\u094c\3\2\2\2\u0948\u0949\5\u010a\u0086\2\u0949\u094a"+
		"\b\u0082\1\2\u094a\u094c\3\2\2\2\u094b\u08fe\3\2\2\2\u094b\u0942\3\2\2"+
		"\2\u094b\u0945\3\2\2\2\u094b\u0948\3\2\2\2\u094c\u0103\3\2\2\2\u094d\u0952"+
		"\5\u0148\u00a5\2\u094e\u0952\5\u0144\u00a3\2\u094f\u0952\5\u0106\u0084"+
		"\2\u0950\u0952\5\u014c\u00a7\2\u0951\u094d\3\2\2\2\u0951\u094e\3\2\2\2"+
		"\u0951\u094f\3\2\2\2\u0951\u0950\3\2\2\2\u0952\u0105\3\2\2\2\u0953\u0956"+
		"\5\u00e2r\2\u0954\u0956\5r:\2\u0955\u0953\3\2\2\2\u0955\u0954\3\2\2\2"+
		"\u0956\u0107\3\2\2\2\u0957\u0959\t\25\2\2\u0958\u095a\5\u00e6t\2\u0959"+
		"\u0958\3\2\2\2\u0959\u095a\3\2\2\2\u095a\u095b\3\2\2\2\u095b\u095c\7c"+
		"\2\2\u095c\u0109\3\2\2\2\u095d\u0960\t\25\2\2\u095e\u0961\5\u011a\u008e"+
		"\2\u095f\u0961\7m\2\2\u0960\u095e\3\2\2\2\u0960\u095f\3\2\2\2\u0961\u0962"+
		"\3\2\2\2\u0962\u0963\7c\2\2\u0963\u010b\3\2\2\2\u0964\u0965\7\17\2\2\u0965"+
		"\u0966\7^\2\2\u0966\u0971\5\u00f8}\2\u0967\u096b\7e\2\2\u0968\u096a\7"+
		"\u008f\2\2\u0969\u0968\3\2\2\2\u096a\u096d\3\2\2\2\u096b\u0969\3\2\2\2"+
		"\u096b\u096c\3\2\2\2\u096c\u096e\3\2\2\2\u096d\u096b\3\2\2\2\u096e\u0970"+
		"\5\u00f8}\2\u096f\u0967\3\2\2\2\u0970\u0973\3\2\2\2\u0971\u096f\3\2\2"+
		"\2\u0971\u0972\3\2\2\2\u0972\u0974\3\2\2\2\u0973\u0971\3\2\2\2\u0974\u0975"+
		"\7_\2\2\u0975\u0995\3\2\2\2\u0976\u0978\5\u0148\u00a5\2\u0977\u0979\5"+
		"T+\2\u0978\u0977\3\2\2\2\u0978\u0979\3\2\2\2\u0979\u0995\3\2\2\2\u097a"+
		"\u0995\5p9\2\u097b\u0995\5r:\2\u097c\u0980\7*\2\2\u097d\u097f\7\u008f"+
		"\2\2\u097e\u097d\3\2\2\2\u097f\u0982\3\2\2\2\u0980\u097e\3\2\2\2\u0980"+
		"\u0981\3\2\2\2\u0981\u0984\3\2\2\2\u0982\u0980\3\2\2\2\u0983\u0985\5\u0134"+
		"\u009b\2\u0984\u0983\3\2\2\2\u0984\u0985\3\2\2\2\u0985\u0986\3\2\2\2\u0986"+
		"\u0995\5\u0128\u0095\2\u0987\u0989\5\u0134\u009b\2\u0988\u0987\3\2\2\2"+
		"\u0988\u0989\3\2\2\2\u0989\u098a\3\2\2\2\u098a\u0995\79\2\2\u098b\u098d"+
		"\5\u0134\u009b\2\u098c\u098b\3\2\2\2\u098c\u098d\3\2\2\2\u098d\u098e\3"+
		"\2\2\2\u098e\u0995\7\66\2\2\u098f\u0995\5\u00e2r\2\u0990\u0995\5\u0084"+
		"C\2\u0991\u0995\5\u0114\u008b\2\u0992\u0995\5\u0116\u008c\2\u0993\u0995"+
		"\5\u014a\u00a6\2\u0994\u0964\3\2\2\2\u0994\u0976\3\2\2\2\u0994\u097a\3"+
		"\2\2\2\u0994\u097b\3\2\2\2\u0994\u097c\3\2\2\2\u0994\u0988\3\2\2\2\u0994"+
		"\u098c\3\2\2\2\u0994\u098f\3\2\2\2\u0994\u0990\3\2\2\2\u0994\u0991\3\2"+
		"\2\2\u0994\u0992\3\2\2\2\u0994\u0993\3\2\2\2\u0995\u010d\3\2\2\2\u0996"+
		"\u099d\5\u0148\u00a5\2\u0997\u099d\5p9\2\u0998\u099d\5r:\2\u0999\u099d"+
		"\5\u00e2r\2\u099a\u099d\5\u0114\u008b\2\u099b\u099d\5\u0116\u008c\2\u099c"+
		"\u0996\3\2\2\2\u099c\u0997\3\2\2\2\u099c\u0998\3\2\2\2\u099c\u0999\3\2"+
		"\2\2\u099c\u099a\3\2\2\2\u099c\u099b\3\2\2\2\u099d\u010f\3\2\2\2\u099e"+
		"\u09a2\5\u0148\u00a5\2\u099f\u09a2\5p9\2\u09a0\u09a2\5r:\2\u09a1\u099e"+
		"\3\2\2\2\u09a1\u099f\3\2\2\2\u09a1\u09a0\3\2\2\2\u09a2\u0111\3\2\2\2\u09a3"+
		"\u09a7\5\u0148\u00a5\2\u09a4\u09a7\5p9\2\u09a5\u09a7\5r:\2\u09a6\u09a3"+
		"\3\2\2\2\u09a6\u09a4\3\2\2\2\u09a6\u09a5\3\2\2\2\u09a7\u0113\3\2\2\2\u09a8"+
		"\u09aa\7b\2\2\u09a9\u09ab\5\u00e6t\2\u09aa\u09a9\3\2\2\2\u09aa\u09ab\3"+
		"\2\2\2\u09ab\u09ad\3\2\2\2\u09ac\u09ae\7e\2\2\u09ad\u09ac\3\2\2\2\u09ad"+
		"\u09ae\3\2\2\2\u09ae\u09af\3\2\2\2\u09af\u09b0\7c\2\2\u09b0\u0115\3\2"+
		"\2\2\u09b1\u09b7\7b\2\2\u09b2\u09b4\5\u0118\u008d\2\u09b3\u09b5\7e\2\2"+
		"\u09b4\u09b3\3\2\2\2\u09b4\u09b5\3\2\2\2\u09b5\u09b8\3\2\2\2\u09b6\u09b8"+
		"\7m\2\2\u09b7\u09b2\3\2\2\2\u09b7\u09b6\3\2\2\2\u09b8\u09b9\3\2\2\2\u09b9"+
		"\u09ba\7c\2\2\u09ba\u0117\3\2\2\2\u09bb\u09c0\5\u011c\u008f\2\u09bc\u09bd"+
		"\7e\2\2\u09bd\u09bf\5\u011c\u008f\2\u09be\u09bc\3\2\2\2\u09bf\u09c2\3"+
		"\2\2\2\u09c0\u09be\3\2\2\2\u09c0\u09c1\3\2\2\2\u09c1\u0119\3\2\2\2\u09c2"+
		"\u09c0\3\2\2\2\u09c3\u09c8\5\u011e\u0090\2\u09c4\u09c5\7e\2\2\u09c5\u09c7"+
		"\5\u011e\u0090\2\u09c6\u09c4\3\2\2\2\u09c7\u09ca\3\2\2\2\u09c8\u09c6\3"+
		"\2\2\2\u09c8\u09c9\3\2\2\2\u09c9\u011b\3\2\2\2\u09ca\u09c8\3\2\2\2\u09cb"+
		"\u09cc\5\u0122\u0092\2\u09cc\u09d0\7m\2\2\u09cd\u09cf\7\u008f\2\2\u09ce"+
		"\u09cd\3\2\2\2\u09cf\u09d2\3\2\2\2\u09d0\u09ce\3\2\2\2\u09d0\u09d1\3\2"+
		"\2\2\u09d1\u09d3\3\2\2\2\u09d2\u09d0\3\2\2\2\u09d3\u09d4\5\u00eav\2\u09d4"+
		"\u09df\3\2\2\2\u09d5\u09d6\7x\2\2\u09d6\u09da\7m\2\2\u09d7\u09d9\7\u008f"+
		"\2\2\u09d8\u09d7\3\2\2\2\u09d9\u09dc\3\2\2\2\u09da\u09d8\3\2\2\2\u09da"+
		"\u09db\3\2\2\2\u09db\u09dd\3\2\2\2\u09dc\u09da\3\2\2\2\u09dd\u09df\5\u00ea"+
		"v\2\u09de\u09cb\3\2\2\2\u09de\u09d5\3\2\2\2\u09df\u011d\3\2\2\2\u09e0"+
		"\u09e1\5\u0124\u0093\2\u09e1\u09e5\7m\2\2\u09e2\u09e4\7\u008f\2\2\u09e3"+
		"\u09e2\3\2\2\2\u09e4\u09e7\3\2\2\2\u09e5\u09e3\3\2\2\2\u09e5\u09e6\3\2"+
		"\2\2\u09e6\u09e8\3\2\2\2\u09e7\u09e5\3\2\2\2\u09e8\u09e9\5\u00eav\2\u09e9"+
		"\u09f4\3\2\2\2\u09ea\u09eb\7x\2\2\u09eb\u09ef\7m\2\2\u09ec\u09ee\7\u008f"+
		"\2\2\u09ed\u09ec\3\2\2\2\u09ee\u09f1\3\2\2\2\u09ef\u09ed\3\2\2\2\u09ef"+
		"\u09f0\3\2\2\2\u09f0\u09f2\3\2\2\2\u09f1\u09ef\3\2\2\2\u09f2\u09f4\5\u00ea"+
		"v\2\u09f3\u09e0\3\2\2\2\u09f3\u09ea\3\2\2\2\u09f4\u011f\3\2\2\2\u09f5"+
		"\u09f6\5\u0126\u0094\2\u09f6\u09fa\7m\2\2\u09f7\u09f9\7\u008f\2\2\u09f8"+
		"\u09f7\3\2\2\2\u09f9\u09fc\3\2\2\2\u09fa\u09f8\3\2\2\2\u09fa\u09fb\3\2"+
		"\2\2\u09fb\u09fd\3\2\2\2\u09fc\u09fa\3\2\2\2\u09fd\u09fe\5\u00eav\2\u09fe"+
		"\u0a09\3\2\2\2\u09ff\u0a00\7x\2\2\u0a00\u0a04\7m\2\2\u0a01\u0a03\7\u008f"+
		"\2\2\u0a02\u0a01\3\2\2\2\u0a03\u0a06\3\2\2\2\u0a04\u0a02\3\2\2\2\u0a04"+
		"\u0a05\3\2\2\2\u0a05\u0a07\3\2\2\2\u0a06\u0a04\3\2\2\2\u0a07\u0a09\5\u00ea"+
		"v\2\u0a08\u09f5\3\2\2\2\u0a08\u09ff\3\2\2\2\u0a09\u0121\3\2\2\2\u0a0a"+
		"\u0a0d\5\u014c\u00a7\2\u0a0b\u0a0d\5\u010c\u0087\2\u0a0c\u0a0a\3\2\2\2"+
		"\u0a0c\u0a0b\3\2\2\2\u0a0d\u0123\3\2\2\2\u0a0e\u0a11\5\u014c\u00a7\2\u0a0f"+
		"\u0a11\5\u010e\u0088\2\u0a10\u0a0e\3\2\2\2\u0a10\u0a0f\3\2\2\2\u0a11\u0125"+
		"\3\2\2\2\u0a12\u0a15\5\u014c\u00a7\2\u0a13\u0a15\5\u0110\u0089\2\u0a14"+
		"\u0a12\3\2\2\2\u0a14\u0a13\3\2\2\2\u0a15\u0127\3\2\2\2\u0a16\u0a39\5\u0132"+
		"\u009a\2\u0a17\u0a19\7\u008f\2\2\u0a18\u0a17\3\2\2\2\u0a19\u0a1c\3\2\2"+
		"\2\u0a1a\u0a18\3\2\2\2\u0a1a\u0a1b\3\2\2\2\u0a1b\u0a1d\3\2\2\2\u0a1c\u0a1a"+
		"\3\2\2\2\u0a1d\u0a1f\5\u0138\u009d\2\u0a1e\u0a20\5\u0130\u0099\2\u0a1f"+
		"\u0a1e\3\2\2\2\u0a1f\u0a20\3\2\2\2\u0a20\u0a3a\3\2\2\2\u0a21\u0a23\5\u012a"+
		"\u0096\2\u0a22\u0a21\3\2\2\2\u0a23\u0a24\3\2\2\2\u0a24\u0a22\3\2\2\2\u0a24"+
		"\u0a25\3\2\2\2\u0a25\u0a29\3\2\2\2\u0a26\u0a28\7\u008f\2\2\u0a27\u0a26"+
		"\3\2\2\2\u0a28\u0a2b\3\2\2\2\u0a29\u0a27\3\2\2\2\u0a29\u0a2a\3\2\2\2\u0a2a"+
		"\u0a2c\3\2\2\2\u0a2b\u0a29\3\2\2\2\u0a2c\u0a2d\5\u012e\u0098\2\u0a2d\u0a3a"+
		"\3\2\2\2\u0a2e\u0a30\5\u012c\u0097\2\u0a2f\u0a2e\3\2\2\2\u0a30\u0a31\3"+
		"\2\2\2\u0a31\u0a2f\3\2\2\2\u0a31\u0a32\3\2\2\2\u0a32\u0a36\3\2\2\2\u0a33"+
		"\u0a35\5\u012a\u0096\2\u0a34\u0a33\3\2\2\2\u0a35\u0a38\3\2\2\2\u0a36\u0a34"+
		"\3\2\2\2\u0a36\u0a37\3\2\2\2\u0a37\u0a3a\3\2\2\2\u0a38\u0a36\3\2\2\2\u0a39"+
		"\u0a1a\3\2\2\2\u0a39\u0a22\3\2\2\2\u0a39\u0a2f\3\2\2\2\u0a3a\u0129\3\2"+
		"\2\2\u0a3b\u0a3c\5\u008aF\2\u0a3c\u0a3d\7b\2\2\u0a3d\u0a3e\7c\2\2\u0a3e"+
		"\u012b\3\2\2\2\u0a3f\u0a40\5\u008aF\2\u0a40\u0a41\7b\2\2\u0a41\u0a42\5"+
		"\u00f8}\2\u0a42\u0a43\7c\2\2\u0a43\u012d\3\2\2\2\u0a44\u0a48\7`\2\2\u0a45"+
		"\u0a47\7\u008f\2\2\u0a46\u0a45\3\2\2\2\u0a47\u0a4a\3\2\2\2\u0a48\u0a46"+
		"\3\2\2\2\u0a48\u0a49\3\2\2\2\u0a49\u0a6b\3\2\2\2\u0a4a\u0a48\3\2\2\2\u0a4b"+
		"\u0a4e\5\u012e\u0098\2\u0a4c\u0a4e\5D#\2\u0a4d\u0a4b\3\2\2\2\u0a4d\u0a4c"+
		"\3\2\2\2\u0a4e\u0a52\3\2\2\2\u0a4f\u0a51\7\u008f\2\2\u0a50\u0a4f\3\2\2"+
		"\2\u0a51\u0a54\3\2\2\2\u0a52\u0a50\3\2\2\2\u0a52\u0a53\3\2\2\2\u0a53\u0a68"+
		"\3\2\2\2\u0a54\u0a52\3\2\2\2\u0a55\u0a59\7e\2\2\u0a56\u0a58\7\u008f\2"+
		"\2\u0a57\u0a56\3\2\2\2\u0a58\u0a5b\3\2\2\2\u0a59\u0a57\3\2\2\2\u0a59\u0a5a"+
		"\3\2\2\2\u0a5a\u0a5e\3\2\2\2\u0a5b\u0a59\3\2\2\2\u0a5c\u0a5f\5\u012e\u0098"+
		"\2\u0a5d\u0a5f\5D#\2\u0a5e\u0a5c\3\2\2\2\u0a5e\u0a5d\3\2\2\2\u0a5f\u0a63"+
		"\3\2\2\2\u0a60\u0a62\7\u008f\2\2\u0a61\u0a60\3\2\2\2\u0a62\u0a65\3\2\2"+
		"\2\u0a63\u0a61\3\2\2\2\u0a63\u0a64\3\2\2\2\u0a64\u0a67\3\2\2\2\u0a65\u0a63"+
		"\3\2\2\2\u0a66\u0a55\3\2\2\2\u0a67\u0a6a\3\2\2\2\u0a68\u0a66\3\2\2\2\u0a68"+
		"\u0a69\3\2\2\2\u0a69\u0a6c\3\2\2\2\u0a6a\u0a68\3\2\2\2\u0a6b\u0a4d\3\2"+
		"\2\2\u0a6b\u0a6c\3\2\2\2\u0a6c\u0a6e\3\2\2\2\u0a6d\u0a6f\7e\2\2\u0a6e"+
		"\u0a6d\3\2\2\2\u0a6e\u0a6f\3\2\2\2\u0a6f\u0a73\3\2\2\2\u0a70\u0a72\7\u008f"+
		"\2\2\u0a71\u0a70\3\2\2\2\u0a72\u0a75\3\2\2\2\u0a73\u0a71\3\2\2\2\u0a73"+
		"\u0a74\3\2\2\2\u0a74\u0a76\3\2\2\2\u0a75\u0a73\3\2\2\2\u0a76\u0a77\7a"+
		"\2\2\u0a77\u012f\3\2\2\2\u0a78\u0a79\5*\26\2\u0a79\u0131\3\2\2\2\u0a7a"+
		"\u0a8a\5\u008aF\2\u0a7b\u0a8b\5H%\2\u0a7c\u0a88\5l\67\2\u0a7d\u0a85\5"+
		"\u0136\u009c\2\u0a7e\u0a7f\7f\2\2\u0a7f\u0a81\5\u0148\u00a5\2\u0a80\u0a82"+
		"\5\u0136\u009c\2\u0a81\u0a80\3\2\2\2\u0a81\u0a82\3\2\2\2\u0a82\u0a84\3"+
		"\2\2\2\u0a83\u0a7e\3\2\2\2\u0a84\u0a87\3\2\2\2\u0a85\u0a83\3\2\2\2\u0a85"+
		"\u0a86\3\2\2\2\u0a86\u0a89\3\2\2\2\u0a87\u0a85\3\2\2\2\u0a88\u0a7d\3\2"+
		"\2\2\u0a88\u0a89\3\2\2\2\u0a89\u0a8b\3\2\2\2\u0a8a\u0a7b\3\2\2\2\u0a8a"+
		"\u0a7c\3\2\2\2\u0a8b\u0133\3\2\2\2\u0a8c\u0a90\7i\2\2\u0a8d\u0a8f\7\u008f"+
		"\2\2\u0a8e\u0a8d\3\2\2\2\u0a8f\u0a92\3\2\2\2\u0a90\u0a8e\3\2\2\2\u0a90"+
		"\u0a91\3\2\2\2\u0a91\u0a93\3\2\2\2\u0a92\u0a90\3\2\2\2\u0a93\u0a97\5&"+
		"\24\2\u0a94\u0a96\7\u008f\2\2\u0a95\u0a94\3\2\2\2\u0a96\u0a99\3\2\2\2"+
		"\u0a97\u0a95\3\2\2\2\u0a97\u0a98\3\2\2\2\u0a98\u0a9a\3\2\2\2\u0a99\u0a97"+
		"\3\2\2\2\u0a9a\u0a9b\7h\2\2\u0a9b\u0135\3\2\2\2\u0a9c\u0a9d\7i\2\2\u0a9d"+
		"\u0aa0\7h\2\2\u0a9e\u0aa0\5T+\2\u0a9f\u0a9c\3\2\2\2\u0a9f\u0a9e\3\2\2"+
		"\2\u0aa0\u0137\3\2\2\2\u0aa1\u0aa3\7^\2\2\u0aa2\u0aa4\5\u013c\u009f\2"+
		"\u0aa3\u0aa2\3\2\2\2\u0aa3\u0aa4\3\2\2\2\u0aa4\u0aa6\3\2\2\2\u0aa5\u0aa7"+
		"\7e\2\2\u0aa6\u0aa5\3\2\2\2\u0aa6\u0aa7\3\2\2\2\u0aa7\u0aa8\3\2\2\2\u0aa8"+
		"\u0aa9\7_\2\2\u0aa9\u0139\3\2\2\2\u0aaa\u0ab5\5\u013e\u00a0\2\u0aab\u0aaf"+
		"\7e\2\2\u0aac\u0aae\7\u008f\2\2\u0aad\u0aac\3\2\2\2\u0aae\u0ab1\3\2\2"+
		"\2\u0aaf\u0aad\3\2\2\2\u0aaf\u0ab0\3\2\2\2\u0ab0\u0ab2\3\2\2\2\u0ab1\u0aaf"+
		"\3\2\2\2\u0ab2\u0ab4\5\u0140\u00a1\2\u0ab3\u0aab\3\2\2\2\u0ab4\u0ab7\3"+
		"\2\2\2\u0ab5\u0ab3\3\2\2\2\u0ab5\u0ab6\3\2\2\2\u0ab6\u013b\3\2\2\2\u0ab7"+
		"\u0ab5\3\2\2\2\u0ab8\u0ac3\5\u0142\u00a2\2\u0ab9\u0abd\7e\2\2\u0aba\u0abc"+
		"\7\u008f\2\2\u0abb\u0aba\3\2\2\2\u0abc\u0abf\3\2\2\2\u0abd\u0abb\3\2\2"+
		"\2\u0abd\u0abe\3\2\2\2\u0abe\u0ac0\3\2\2\2\u0abf\u0abd\3\2\2\2\u0ac0\u0ac2"+
		"\5\u0142\u00a2\2\u0ac1\u0ab9\3\2\2\2\u0ac2\u0ac5\3\2\2\2\u0ac3\u0ac1\3"+
		"\2\2\2\u0ac3\u0ac4\3\2\2\2\u0ac4\u013d\3\2\2\2\u0ac5\u0ac3\3\2\2\2\u0ac6"+
		"\u0ac9\5\u00e8u\2\u0ac7\u0ac9\5\u0120\u0091\2\u0ac8\u0ac6\3\2\2\2\u0ac8"+
		"\u0ac7\3\2\2\2\u0ac9\u013f\3\2\2\2\u0aca\u0acd\5\u00e8u\2\u0acb\u0acd"+
		"\5\u011e\u0090\2\u0acc\u0aca\3\2\2\2\u0acc\u0acb\3\2\2\2\u0acd\u0141\3"+
		"\2\2\2\u0ace\u0ad2\5\u00e8u\2\u0acf\u0ad2\5z>\2\u0ad0\u0ad2\5\u011e\u0090"+
		"\2\u0ad1\u0ace\3\2\2\2\u0ad1\u0acf\3\2\2\2\u0ad1\u0ad0\3\2\2\2\u0ad2\u0143"+
		"\3\2\2\2\u0ad3\u0ad4\7\3\2\2\u0ad4\u0145\3\2\2\2\u0ad5\u0ad6\7\u008a\2"+
		"\2\u0ad6\u0147\3\2\2\2\u0ad7\u0ad8\t\26\2\2\u0ad8\u0149\3\2\2\2\u0ad9"+
		"\u0ada\t\27\2\2\u0ada\u014b\3\2\2\2\u0adb\u0adc\t\30\2\2\u0adc\u014d\3"+
		"\2\2\2\u0add\u0adf\t\31\2\2\u0ade\u0add\3\2\2\2\u0adf\u0ae0\3\2\2\2\u0ae0"+
		"\u0ade\3\2\2\2\u0ae0\u0ae1\3\2\2\2\u0ae1\u014f\3\2\2\2\u0188\u0153\u0158"+
		"\u015a\u015d\u0167\u016b\u0172\u017d\u0184\u0186\u018d\u0193\u0196\u019c"+
		"\u01a2\u01a9\u01ac\u01b2\u01b8\u01bd\u01c1\u01c7\u01ca\u01d0\u01d6\u01dd"+
		"\u01e5\u01eb\u01f1\u01fc\u0200\u0207\u020d\u0215\u021b\u022b\u0231\u0235"+
		"\u023a\u023e\u0243\u024a\u024e\u0253\u025a\u025e\u0263\u026a\u026e\u0273"+
		"\u027c\u0284\u0288\u028d\u0291\u0296\u029a\u029f\u02a8\u02ab\u02b3\u02b6"+
		"\u02b8\u02bb\u02c3\u02ca\u02d0\u02d6\u02d9\u02df\u02e2\u02e6\u02ed\u02ef"+
		"\u02f3\u02f9\u02fc\u0305\u030c\u0313\u031a\u031e\u0323\u0327\u032d\u0334"+
		"\u0338\u0341\u0347\u034e\u0355\u035b\u035d\u0361\u0369\u036e\u0378\u037c"+
		"\u037f\u0383\u0387\u038c\u0391\u0399\u039d\u03a0\u03a6\u03ae\u03b4\u03ba"+
		"\u03c6\u03ca\u03cc\u03d6\u03dc\u03e1\u03e7\u03ed\u03f3\u03fb\u03fe\u0404"+
		"\u040b\u040f\u0418\u0420\u0427\u0433\u043b\u0443\u044a\u0450\u0457\u045e"+
		"\u0467\u046e\u0477\u047b\u0481\u0488\u048b\u048e\u0491\u0498\u049b\u04a3"+
		"\u04a7\u04ad\u04b3\u04b9\u04bc\u04c3\u04c8\u04cb\u04d0\u04d9\u04e0\u04e7"+
		"\u04ee\u04f3\u04fb\u04ff\u0501\u050a\u050d\u0511\u0515\u0523\u0527\u052e"+
		"\u0535\u053a\u053f\u0547\u054f\u0552\u055a\u055d\u0567\u0571\u0577\u057e"+
		"\u0585\u0589\u058f\u0593\u059a\u05a1\u05a7\u05ac\u05af\u05b6\u05be\u05c9"+
		"\u05d3\u05da\u05e0\u05e4\u05e8\u05ef\u05f4\u05fb\u0601\u0607\u060b\u0612"+
		"\u0619\u061d\u0628\u062f\u063c\u0645\u064e\u0657\u065d\u0664\u066e\u0675"+
		"\u067e\u0683\u068d\u0692\u0698\u069e\u06a4\u06a9\u06b1\u06b5\u06ba\u06be"+
		"\u06c5\u06ca\u06ce\u06d2\u06d6\u06e3\u06e9\u06ee\u06fb\u0701\u0705\u070b"+
		"\u070f\u0715\u071c\u0723\u0729\u072f\u0738\u073d\u0744\u0750\u075e\u0764"+
		"\u076d\u0777\u077e\u0783\u078a\u0792\u0799\u07a2\u07aa\u07b4\u07b7\u07bc"+
		"\u07c4\u07cb\u07d3\u07da\u07e2\u07e9\u07f1\u07f8\u0800\u0807\u080f\u0816"+
		"\u081e\u0825\u082d\u0834\u083c\u0843\u084b\u0852\u0859\u0860\u0867\u086a"+
		"\u0871\u0878\u0880\u0887\u088f\u0896\u089e\u08a5\u08a9\u08ab\u08b5\u08c3"+
		"\u08c9\u08d1\u08d7\u08dd\u08e2\u08e9\u08ec\u08f1\u08f8\u08fe\u0905\u090a"+
		"\u0911\u0916\u091c\u0921\u092a\u0934\u0938\u0940\u094b\u0951\u0955\u0959"+
		"\u0960\u096b\u0971\u0978\u0980\u0984\u0988\u098c\u0994\u099c\u09a1\u09a6"+
		"\u09aa\u09ad\u09b4\u09b7\u09c0\u09c8\u09d0\u09da\u09de\u09e5\u09ef\u09f3"+
		"\u09fa\u0a04\u0a08\u0a0c\u0a10\u0a14\u0a1a\u0a1f\u0a24\u0a29\u0a31\u0a36"+
		"\u0a39\u0a48\u0a4d\u0a52\u0a59\u0a5e\u0a63\u0a68\u0a6b\u0a6e\u0a73\u0a81"+
		"\u0a85\u0a88\u0a8a\u0a90\u0a97\u0a9f\u0aa3\u0aa6\u0aaf\u0ab5\u0abd\u0ac3"+
		"\u0ac8\u0acc\u0ad1\u0ae0";
	public static final String _serializedATN = Utils.join(
		new String[] {
			_serializedATNSegment0,
			_serializedATNSegment1
		},
		""
	);
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN);
	static {
	}
}