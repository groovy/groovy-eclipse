// Generated from GroovyParser.g4 by ANTLR 4.7.4
package org.apache.groovy.parser.antlr4;

    import java.util.Map;
    import org.codehaus.groovy.ast.NodeMetaDataHandler;
    import org.apache.groovy.parser.antlr4.SemanticPredicates;

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
		RollBackOne=6, AS=7, DEF=8, IN=9, TRAIT=10, THREADSAFE=11, VAR=12, BuiltInPrimitiveType=13, 
		ABSTRACT=14, ASSERT=15, BREAK=16, CASE=17, CATCH=18, CLASS=19, CONST=20, 
		CONTINUE=21, DEFAULT=22, DO=23, ELSE=24, ENUM=25, EXTENDS=26, FINAL=27, 
		FINALLY=28, FOR=29, IF=30, GOTO=31, IMPLEMENTS=32, IMPORT=33, INSTANCEOF=34, 
		INTERFACE=35, NATIVE=36, NEW=37, PACKAGE=38, PRIVATE=39, PROTECTED=40, 
		PUBLIC=41, RETURN=42, STATIC=43, STRICTFP=44, SUPER=45, SWITCH=46, SYNCHRONIZED=47, 
		THIS=48, THROW=49, THROWS=50, TRANSIENT=51, TRY=52, VOID=53, VOLATILE=54, 
		WHILE=55, IntegerLiteral=56, FloatingPointLiteral=57, BooleanLiteral=58, 
		NullLiteral=59, RANGE_INCLUSIVE=60, RANGE_EXCLUSIVE=61, SPREAD_DOT=62, 
		SAFE_DOT=63, SAFE_CHAIN_DOT=64, ELVIS=65, METHOD_POINTER=66, METHOD_REFERENCE=67, 
		REGEX_FIND=68, REGEX_MATCH=69, POWER=70, POWER_ASSIGN=71, SPACESHIP=72, 
		IDENTICAL=73, NOT_IDENTICAL=74, ARROW=75, NOT_INSTANCEOF=76, NOT_IN=77, 
		LPAREN=78, RPAREN=79, LBRACE=80, RBRACE=81, LBRACK=82, RBRACK=83, SEMI=84, 
		COMMA=85, DOT=86, ASSIGN=87, GT=88, LT=89, NOT=90, BITNOT=91, QUESTION=92, 
		COLON=93, EQUAL=94, LE=95, GE=96, NOTEQUAL=97, AND=98, OR=99, INC=100, 
		DEC=101, ADD=102, SUB=103, MUL=104, DIV=105, BITAND=106, BITOR=107, XOR=108, 
		MOD=109, ADD_ASSIGN=110, SUB_ASSIGN=111, MUL_ASSIGN=112, DIV_ASSIGN=113, 
		AND_ASSIGN=114, OR_ASSIGN=115, XOR_ASSIGN=116, MOD_ASSIGN=117, LSHIFT_ASSIGN=118, 
		RSHIFT_ASSIGN=119, URSHIFT_ASSIGN=120, ELVIS_ASSIGN=121, CapitalizedIdentifier=122, 
		Identifier=123, AT=124, ELLIPSIS=125, WS=126, NL=127, SH_COMMENT=128, 
		UNEXPECTED_CHAR=129;
	public static final int
		RULE_compilationUnit = 0, RULE_scriptStatements = 1, RULE_scriptStatement = 2, 
		RULE_packageDeclaration = 3, RULE_importDeclaration = 4, RULE_typeDeclaration = 5, 
		RULE_modifier = 6, RULE_modifiersOpt = 7, RULE_modifiers = 8, RULE_classOrInterfaceModifiersOpt = 9, 
		RULE_classOrInterfaceModifiers = 10, RULE_classOrInterfaceModifier = 11, 
		RULE_variableModifier = 12, RULE_variableModifiersOpt = 13, RULE_variableModifiers = 14, 
		RULE_typeParameters = 15, RULE_typeParameter = 16, RULE_typeBound = 17, 
		RULE_typeList = 18, RULE_classDeclaration = 19, RULE_classBody = 20, RULE_enumConstants = 21, 
		RULE_enumConstant = 22, RULE_classBodyDeclaration = 23, RULE_memberDeclaration = 24, 
		RULE_methodDeclaration = 25, RULE_methodName = 26, RULE_returnType = 27, 
		RULE_fieldDeclaration = 28, RULE_variableDeclarators = 29, RULE_variableDeclarator = 30, 
		RULE_variableDeclaratorId = 31, RULE_variableInitializer = 32, RULE_variableInitializers = 33, 
		RULE_emptyDims = 34, RULE_emptyDimsOpt = 35, RULE_standardType = 36, RULE_type = 37, 
		RULE_classOrInterfaceType = 38, RULE_generalClassOrInterfaceType = 39, 
		RULE_standardClassOrInterfaceType = 40, RULE_primitiveType = 41, RULE_typeArguments = 42, 
		RULE_typeArgument = 43, RULE_annotatedQualifiedClassName = 44, RULE_qualifiedClassNameList = 45, 
		RULE_formalParameters = 46, RULE_formalParameterList = 47, RULE_thisFormalParameter = 48, 
		RULE_formalParameter = 49, RULE_methodBody = 50, RULE_qualifiedName = 51, 
		RULE_qualifiedNameElement = 52, RULE_qualifiedNameElements = 53, RULE_qualifiedClassName = 54, 
		RULE_qualifiedStandardClassName = 55, RULE_literal = 56, RULE_gstring = 57, 
		RULE_gstringValue = 58, RULE_gstringPath = 59, RULE_lambdaExpression = 60, 
		RULE_standardLambdaExpression = 61, RULE_lambdaParameters = 62, RULE_standardLambdaParameters = 63, 
		RULE_lambdaBody = 64, RULE_closure = 65, RULE_closureOrLambdaExpression = 66, 
		RULE_blockStatementsOpt = 67, RULE_blockStatements = 68, RULE_annotationsOpt = 69, 
		RULE_annotation = 70, RULE_elementValues = 71, RULE_annotationName = 72, 
		RULE_elementValuePairs = 73, RULE_elementValuePair = 74, RULE_elementValuePairName = 75, 
		RULE_elementValue = 76, RULE_elementValueArrayInitializer = 77, RULE_block = 78, 
		RULE_blockStatement = 79, RULE_localVariableDeclaration = 80, RULE_classifiedModifiers = 81, 
		RULE_variableDeclaration = 82, RULE_typeNamePairs = 83, RULE_typeNamePair = 84, 
		RULE_variableNames = 85, RULE_conditionalStatement = 86, RULE_ifElseStatement = 87, 
		RULE_switchStatement = 88, RULE_loopStatement = 89, RULE_continueStatement = 90, 
		RULE_breakStatement = 91, RULE_tryCatchStatement = 92, RULE_assertStatement = 93, 
		RULE_statement = 94, RULE_catchClause = 95, RULE_catchType = 96, RULE_finallyBlock = 97, 
		RULE_resources = 98, RULE_resourceList = 99, RULE_resource = 100, RULE_switchBlockStatementGroup = 101, 
		RULE_switchLabel = 102, RULE_forControl = 103, RULE_enhancedForControl = 104, 
		RULE_classicalForControl = 105, RULE_forInit = 106, RULE_forUpdate = 107, 
		RULE_castParExpression = 108, RULE_parExpression = 109, RULE_expressionInPar = 110, 
		RULE_expressionList = 111, RULE_expressionListElement = 112, RULE_enhancedStatementExpression = 113, 
		RULE_statementExpression = 114, RULE_postfixExpression = 115, RULE_expression = 116, 
		RULE_castOperandExpression = 117, RULE_commandExpression = 118, RULE_commandArgument = 119, 
		RULE_pathExpression = 120, RULE_pathElement = 121, RULE_namePart = 122, 
		RULE_dynamicMemberName = 123, RULE_indexPropertyArgs = 124, RULE_namedPropertyArgs = 125, 
		RULE_primary = 126, RULE_list = 127, RULE_map = 128, RULE_mapEntryList = 129, 
		RULE_mapEntry = 130, RULE_mapEntryLabel = 131, RULE_creator = 132, RULE_dim = 133, 
		RULE_arrayInitializer = 134, RULE_anonymousInnerClassDeclaration = 135, 
		RULE_createdName = 136, RULE_nonWildcardTypeArguments = 137, RULE_typeArgumentsOrDiamond = 138, 
		RULE_arguments = 139, RULE_argumentList = 140, RULE_enhancedArgumentList = 141, 
		RULE_argumentListElement = 142, RULE_enhancedArgumentListElement = 143, 
		RULE_stringLiteral = 144, RULE_className = 145, RULE_identifier = 146, 
		RULE_builtInType = 147, RULE_keywords = 148, RULE_rparen = 149, RULE_nls = 150, 
		RULE_sep = 151;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "scriptStatements", "scriptStatement", "packageDeclaration", 
			"importDeclaration", "typeDeclaration", "modifier", "modifiersOpt", "modifiers", 
			"classOrInterfaceModifiersOpt", "classOrInterfaceModifiers", "classOrInterfaceModifier", 
			"variableModifier", "variableModifiersOpt", "variableModifiers", "typeParameters", 
			"typeParameter", "typeBound", "typeList", "classDeclaration", "classBody", 
			"enumConstants", "enumConstant", "classBodyDeclaration", "memberDeclaration", 
			"methodDeclaration", "methodName", "returnType", "fieldDeclaration", 
			"variableDeclarators", "variableDeclarator", "variableDeclaratorId", 
			"variableInitializer", "variableInitializers", "emptyDims", "emptyDimsOpt", 
			"standardType", "type", "classOrInterfaceType", "generalClassOrInterfaceType", 
			"standardClassOrInterfaceType", "primitiveType", "typeArguments", "typeArgument", 
			"annotatedQualifiedClassName", "qualifiedClassNameList", "formalParameters", 
			"formalParameterList", "thisFormalParameter", "formalParameter", "methodBody", 
			"qualifiedName", "qualifiedNameElement", "qualifiedNameElements", "qualifiedClassName", 
			"qualifiedStandardClassName", "literal", "gstring", "gstringValue", "gstringPath", 
			"lambdaExpression", "standardLambdaExpression", "lambdaParameters", "standardLambdaParameters", 
			"lambdaBody", "closure", "closureOrLambdaExpression", "blockStatementsOpt", 
			"blockStatements", "annotationsOpt", "annotation", "elementValues", "annotationName", 
			"elementValuePairs", "elementValuePair", "elementValuePairName", "elementValue", 
			"elementValueArrayInitializer", "block", "blockStatement", "localVariableDeclaration", 
			"classifiedModifiers", "variableDeclaration", "typeNamePairs", "typeNamePair", 
			"variableNames", "conditionalStatement", "ifElseStatement", "switchStatement", 
			"loopStatement", "continueStatement", "breakStatement", "tryCatchStatement", 
			"assertStatement", "statement", "catchClause", "catchType", "finallyBlock", 
			"resources", "resourceList", "resource", "switchBlockStatementGroup", 
			"switchLabel", "forControl", "enhancedForControl", "classicalForControl", 
			"forInit", "forUpdate", "castParExpression", "parExpression", "expressionInPar", 
			"expressionList", "expressionListElement", "enhancedStatementExpression", 
			"statementExpression", "postfixExpression", "expression", "castOperandExpression", 
			"commandExpression", "commandArgument", "pathExpression", "pathElement", 
			"namePart", "dynamicMemberName", "indexPropertyArgs", "namedPropertyArgs", 
			"primary", "list", "map", "mapEntryList", "mapEntry", "mapEntryLabel", 
			"creator", "dim", "arrayInitializer", "anonymousInnerClassDeclaration", 
			"createdName", "nonWildcardTypeArguments", "typeArgumentsOrDiamond", 
			"arguments", "argumentList", "enhancedArgumentList", "argumentListElement", 
			"enhancedArgumentListElement", "stringLiteral", "className", "identifier", 
			"builtInType", "keywords", "rparen", "nls", "sep"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, "'as'", "'def'", "'in'", "'trait'", 
			"'threadsafe'", "'var'", null, "'abstract'", "'assert'", "'break'", "'case'", 
			"'catch'", "'class'", "'const'", "'continue'", "'default'", "'do'", "'else'", 
			"'enum'", "'extends'", "'final'", "'finally'", "'for'", "'if'", "'goto'", 
			"'implements'", "'import'", "'instanceof'", "'interface'", "'native'", 
			"'new'", "'package'", "'private'", "'protected'", "'public'", "'return'", 
			"'static'", "'strictfp'", "'super'", "'switch'", "'synchronized'", "'this'", 
			"'throw'", "'throws'", "'transient'", "'try'", "'void'", "'volatile'", 
			"'while'", null, null, null, "'null'", "'..'", "'..<'", "'*.'", "'?.'", 
			"'??.'", "'?:'", "'.&'", "'::'", "'=~'", "'==~'", "'**'", "'**='", "'<=>'", 
			"'==='", "'!=='", "'->'", "'!instanceof'", "'!in'", null, null, null, 
			null, null, null, "';'", "','", null, "'='", "'>'", "'<'", "'!'", "'~'", 
			"'?'", "':'", "'=='", "'<='", "'>='", "'!='", "'&&'", "'||'", "'++'", 
			"'--'", "'+'", "'-'", "'*'", null, "'&'", "'|'", "'^'", "'%'", "'+='", 
			"'-='", "'*='", "'/='", "'&='", "'|='", "'^='", "'%='", "'<<='", "'>>='", 
			"'>>>='", "'?='", null, null, "'@'", "'...'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "StringLiteral", "GStringBegin", "GStringEnd", "GStringPart", "GStringPathPart", 
			"RollBackOne", "AS", "DEF", "IN", "TRAIT", "THREADSAFE", "VAR", "BuiltInPrimitiveType", 
			"ABSTRACT", "ASSERT", "BREAK", "CASE", "CATCH", "CLASS", "CONST", "CONTINUE", 
			"DEFAULT", "DO", "ELSE", "ENUM", "EXTENDS", "FINAL", "FINALLY", "FOR", 
			"IF", "GOTO", "IMPLEMENTS", "IMPORT", "INSTANCEOF", "INTERFACE", "NATIVE", 
			"NEW", "PACKAGE", "PRIVATE", "PROTECTED", "PUBLIC", "RETURN", "STATIC", 
			"STRICTFP", "SUPER", "SWITCH", "SYNCHRONIZED", "THIS", "THROW", "THROWS", 
			"TRANSIENT", "TRY", "VOID", "VOLATILE", "WHILE", "IntegerLiteral", "FloatingPointLiteral", 
			"BooleanLiteral", "NullLiteral", "RANGE_INCLUSIVE", "RANGE_EXCLUSIVE", 
			"SPREAD_DOT", "SAFE_DOT", "SAFE_CHAIN_DOT", "ELVIS", "METHOD_POINTER", 
			"METHOD_REFERENCE", "REGEX_FIND", "REGEX_MATCH", "POWER", "POWER_ASSIGN", 
			"SPACESHIP", "IDENTICAL", "NOT_IDENTICAL", "ARROW", "NOT_INSTANCEOF", 
			"NOT_IN", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", 
			"SEMI", "COMMA", "DOT", "ASSIGN", "GT", "LT", "NOT", "BITNOT", "QUESTION", 
			"COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", "OR", "INC", "DEC", 
			"ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "XOR", "MOD", "ADD_ASSIGN", 
			"SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", 
			"XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", "URSHIFT_ASSIGN", 
			"ELVIS_ASSIGN", "CapitalizedIdentifier", "Identifier", "AT", "ELLIPSIS", 
			"WS", "NL", "SH_COMMENT", "UNEXPECTED_CHAR"
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
		public SepContext sep() {
			return getRuleContext(SepContext.class,0);
		}
		public ScriptStatementsContext scriptStatements() {
			return getRuleContext(ScriptStatementsContext.class,0);
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
			setState(304);
			nls();
			setState(306);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(305);
				packageDeclaration();
				}
				break;
			}
			setState(309);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(308);
				sep();
				}
				break;
			}
			setState(312);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(311);
				scriptStatements();
				}
				break;
			}
			setState(314);
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
			setState(316);
			scriptStatement();
			setState(322);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(317);
					sep();
					setState(318);
					scriptStatement();
					}
					} 
				}
				setState(324);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(326);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(325);
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
			setState(331);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(328);
				importDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(329);
				typeDeclaration();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(330);
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
			setState(333);
			annotationsOpt();
			setState(334);
			match(PACKAGE);
			setState(335);
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(337);
			annotationsOpt();
			setState(338);
			match(IMPORT);
			setState(340);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(339);
				match(STATIC);
				}
				break;
			}
			setState(342);
			qualifiedName();
			setState(347);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
				{
				setState(343);
				match(DOT);
				setState(344);
				match(MUL);
				}
				break;
			case AS:
				{
				setState(345);
				match(AS);
				setState(346);
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
			setState(349);
			classOrInterfaceModifiersOpt();
			setState(350);
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
			setState(354);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case DEFAULT:
			case FINAL:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
			case STRICTFP:
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(352);
				classOrInterfaceModifier();
				}
				break;
			case DEF:
			case VAR:
			case NATIVE:
			case SYNCHRONIZED:
			case TRANSIENT:
			case VOLATILE:
				enterOuterAlt(_localctx, 2);
				{
				setState(353);
				_localctx.m = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << VAR) | (1L << NATIVE) | (1L << SYNCHRONIZED) | (1L << TRANSIENT) | (1L << VOLATILE))) != 0)) ) {
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
			setState(359);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(356);
				modifiers();
				setState(357);
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
			setState(361);
			modifier();
			setState(367);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(362);
					nls();
					setState(363);
					modifier();
					}
					} 
				}
				setState(369);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
			setState(377);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(370);
				classOrInterfaceModifiers();
				setState(374);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NL) {
					{
					{
					setState(371);
					match(NL);
					}
					}
					setState(376);
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
			setState(379);
			classOrInterfaceModifier();
			setState(385);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(380);
					nls();
					setState(381);
					classOrInterfaceModifier();
					}
					} 
				}
				setState(387);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
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
			setState(390);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(388);
				annotation();
				}
				break;
			case ABSTRACT:
			case DEFAULT:
			case FINAL:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
			case STRICTFP:
				enterOuterAlt(_localctx, 2);
				{
				setState(389);
				_localctx.m = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABSTRACT) | (1L << DEFAULT) | (1L << FINAL) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << STATIC) | (1L << STRICTFP))) != 0)) ) {
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
			setState(394);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(392);
				annotation();
				}
				break;
			case DEF:
			case VAR:
			case ABSTRACT:
			case FINAL:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
			case STRICTFP:
				enterOuterAlt(_localctx, 2);
				{
				setState(393);
				_localctx.m = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEF) | (1L << VAR) | (1L << ABSTRACT) | (1L << FINAL) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << STATIC) | (1L << STRICTFP))) != 0)) ) {
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
			setState(399);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(396);
				variableModifiers();
				setState(397);
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
			setState(401);
			variableModifier();
			setState(407);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(402);
					nls();
					setState(403);
					variableModifier();
					}
					} 
				}
				setState(409);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
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
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(410);
			match(LT);
			setState(411);
			nls();
			setState(412);
			typeParameter();
			setState(419);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(413);
					match(COMMA);
					setState(414);
					nls();
					setState(415);
					typeParameter();
					}
					} 
				}
				setState(421);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			setState(422);
			nls();
			setState(423);
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(425);
			className();
			setState(430);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(426);
				match(EXTENDS);
				setState(427);
				nls();
				setState(428);
				typeBound();
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
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(432);
			type();
			setState(439);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(433);
					match(BITAND);
					setState(434);
					nls();
					setState(435);
					type();
					}
					} 
				}
				setState(441);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
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
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(442);
			type();
			setState(449);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(443);
					match(COMMA);
					setState(444);
					nls();
					setState(445);
					type();
					}
					} 
				}
				setState(451);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
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
		public TypeContext sc;
		public TypeListContext is;
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
		public TerminalNode EXTENDS() { return getToken(GroovyParser.EXTENDS, 0); }
		public TerminalNode IMPLEMENTS() { return getToken(GroovyParser.IMPLEMENTS, 0); }
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public List<? extends TypeListContext> typeList() {
			return getRuleContexts(TypeListContext.class);
		}
		public TypeListContext typeList(int i) {
			return getRuleContext(TypeListContext.class,i);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
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
			setState(463);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CLASS:
				{
				setState(452);
				match(CLASS);
				 _localctx.t =  0; 
				}
				break;
			case INTERFACE:
				{
				setState(454);
				match(INTERFACE);
				 _localctx.t =  1; 
				}
				break;
			case ENUM:
				{
				setState(456);
				match(ENUM);
				 _localctx.t =  2; 
				}
				break;
			case AT:
				{
				setState(458);
				match(AT);
				setState(459);
				match(INTERFACE);
				 _localctx.t =  3; 
				}
				break;
			case TRAIT:
				{
				setState(461);
				match(TRAIT);
				 _localctx.t =  4; 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(465);
			identifier();
			setState(466);
			nls();
			setState(496);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				setState(467);
				if (!( 3 != _localctx.t )) throw createFailedPredicateException(" 3 != $t ");
				setState(471);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(468);
					typeParameters();
					setState(469);
					nls();
					}
					break;
				}
				setState(484);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
				case 1:
					{
					setState(473);
					if (!( 2 != _localctx.t )) throw createFailedPredicateException(" 2 != $t ");
					setState(474);
					match(EXTENDS);
					setState(475);
					nls();
					setState(479);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
					case 1:
						{
						setState(476);
						if (!(1 == _localctx.t)) throw createFailedPredicateException("1 == $t");
						setState(477);
						_localctx.scs = typeList();
						}
						break;

					case 2:
						{
						setState(478);
						_localctx.sc = type();
						}
						break;
					}
					setState(481);
					nls();
					}
					break;

				case 2:
					{
					}
					break;
				}
				setState(493);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(486);
					if (!(1 != _localctx.t)) throw createFailedPredicateException("1 != $t");
					setState(487);
					match(IMPLEMENTS);
					setState(488);
					nls();
					setState(489);
					_localctx.is = typeList();
					setState(490);
					nls();
					}
					break;

				case 2:
					{
					}
					break;
				}
				}
				break;

			case 2:
				{
				}
				break;
			}
			setState(498);
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
		public List<? extends ClassBodyDeclarationContext> classBodyDeclaration() {
			return getRuleContexts(ClassBodyDeclarationContext.class);
		}
		public ClassBodyDeclarationContext classBodyDeclaration(int i) {
			return getRuleContext(ClassBodyDeclarationContext.class,i);
		}
		public List<? extends SepContext> sep() {
			return getRuleContexts(SepContext.class);
		}
		public SepContext sep(int i) {
			return getRuleContext(SepContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(GroovyParser.COMMA, 0); }
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
			setState(500);
			match(LBRACE);
			setState(501);
			nls();
			setState(513);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(502);
				if (!( 2 == _localctx.t )) throw createFailedPredicateException(" 2 == $t ");
				setState(503);
				enumConstants();
				setState(507);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
				case 1:
					{
					setState(504);
					nls();
					setState(505);
					match(COMMA);
					}
					break;
				}
				setState(510);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(509);
					sep();
					}
					break;
				}
				}
				break;

			case 2:
				{
				}
				break;
			}
			setState(524);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(515);
				classBodyDeclaration(_localctx.t);
				setState(521);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(516);
						sep();
						setState(517);
						classBodyDeclaration(_localctx.t);
						}
						} 
					}
					setState(523);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
				}
				}
				break;
			}
			setState(527);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(526);
				sep();
				}
			}

			setState(529);
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
			setState(531);
			enumConstant();
			setState(539);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(532);
					nls();
					setState(533);
					match(COMMA);
					setState(534);
					nls();
					setState(535);
					enumConstant();
					}
					} 
				}
				setState(541);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(542);
			annotationsOpt();
			setState(543);
			identifier();
			setState(545);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				{
				setState(544);
				arguments();
				}
				break;
			}
			setState(548);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(547);
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
			setState(556);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(552);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(550);
					match(STATIC);
					setState(551);
					nls();
					}
				}

				setState(554);
				block();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(555);
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
			setState(563);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(558);
				methodDeclaration(0, _localctx.t);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(559);
				fieldDeclaration();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(560);
				modifiersOpt();
				setState(561);
				classDeclaration();
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
		public ReturnTypeContext returnType() {
			return getRuleContext(ReturnTypeContext.class,0);
		}
		public MethodNameContext methodName() {
			return getRuleContext(MethodNameContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(GroovyParser.DEFAULT, 0); }
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public ModifiersOptContext modifiersOpt() {
			return getRuleContext(ModifiersOptContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode THROWS() { return getToken(GroovyParser.THROWS, 0); }
		public QualifiedClassNameListContext qualifiedClassNameList() {
			return getRuleContext(QualifiedClassNameListContext.class,0);
		}
		public MethodBodyContext methodBody() {
			return getRuleContext(MethodBodyContext.class,0);
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
		try {
			setState(597);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(565);
				if (!( 3 == _localctx.ct )) throw createFailedPredicateException(" 3 == $ct ");
				setState(566);
				returnType(_localctx.ct);
				setState(567);
				methodName();
				setState(568);
				match(LPAREN);
				setState(569);
				rparen();
				setState(574);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
				case 1:
					{
					setState(570);
					match(DEFAULT);
					setState(571);
					nls();
					setState(572);
					elementValue();
					}
					break;
				}
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(576);
				modifiersOpt();
				setState(578);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
				case 1:
					{
					setState(577);
					typeParameters();
					}
					break;
				}
				setState(581);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
				case 1:
					{
					setState(580);
					returnType(_localctx.ct);
					}
					break;
				}
				setState(583);
				methodName();
				setState(584);
				formalParameters();
				setState(590);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
				case 1:
					{
					setState(585);
					nls();
					setState(586);
					match(THROWS);
					setState(587);
					nls();
					setState(588);
					qualifiedClassNameList();
					}
					break;
				}
				setState(595);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(592);
					nls();
					setState(593);
					methodBody();
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
		enterRule(_localctx, 52, RULE_methodName);
		try {
			setState(601);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(599);
				identifier();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(600);
				stringLiteral();
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
		enterRule(_localctx, 54, RULE_returnType);
		try {
			setState(605);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(603);
				standardType();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(604);
				match(VOID);
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
		enterRule(_localctx, 56, RULE_fieldDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(607);
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
		enterRule(_localctx, 58, RULE_variableDeclarators);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(609);
			variableDeclarator();
			setState(616);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(610);
					match(COMMA);
					setState(611);
					nls();
					setState(612);
					variableDeclarator();
					}
					} 
				}
				setState(618);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
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
		enterRule(_localctx, 60, RULE_variableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(619);
			variableDeclaratorId();
			setState(625);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
			case 1:
				{
				setState(620);
				nls();
				setState(621);
				match(ASSIGN);
				setState(622);
				nls();
				setState(623);
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
		enterRule(_localctx, 62, RULE_variableDeclaratorId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(627);
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
		enterRule(_localctx, 64, RULE_variableInitializer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(629);
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

	public static class VariableInitializersContext extends GroovyParserRuleContext {
		public List<? extends VariableInitializerContext> variableInitializer() {
			return getRuleContexts(VariableInitializerContext.class);
		}
		public VariableInitializerContext variableInitializer(int i) {
			return getRuleContext(VariableInitializerContext.class,i);
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
		public VariableInitializersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableInitializers; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitVariableInitializers(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final VariableInitializersContext variableInitializers() throws RecognitionException {
		VariableInitializersContext _localctx = new VariableInitializersContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_variableInitializers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(631);
			variableInitializer();
			setState(639);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(632);
					nls();
					setState(633);
					match(COMMA);
					setState(634);
					nls();
					setState(635);
					variableInitializer();
					}
					} 
				}
				setState(641);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			}
			setState(642);
			nls();
			setState(644);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				{
				setState(643);
				match(COMMA);
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

	public static class EmptyDimsContext extends GroovyParserRuleContext {
		public List<? extends AnnotationsOptContext> annotationsOpt() {
			return getRuleContexts(AnnotationsOptContext.class);
		}
		public AnnotationsOptContext annotationsOpt(int i) {
			return getRuleContext(AnnotationsOptContext.class,i);
		}
		public List<? extends TerminalNode> LBRACK() { return getTokens(GroovyParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(GroovyParser.LBRACK, i);
		}
		public List<? extends TerminalNode> RBRACK() { return getTokens(GroovyParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(GroovyParser.RBRACK, i);
		}
		public EmptyDimsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_emptyDims; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEmptyDims(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EmptyDimsContext emptyDims() throws RecognitionException {
		EmptyDimsContext _localctx = new EmptyDimsContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_emptyDims);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(650); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(646);
					annotationsOpt();
					setState(647);
					match(LBRACK);
					setState(648);
					match(RBRACK);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(652); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
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

	public static class EmptyDimsOptContext extends GroovyParserRuleContext {
		public EmptyDimsContext emptyDims() {
			return getRuleContext(EmptyDimsContext.class,0);
		}
		public EmptyDimsOptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_emptyDimsOpt; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEmptyDimsOpt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EmptyDimsOptContext emptyDimsOpt() throws RecognitionException {
		EmptyDimsOptContext _localctx = new EmptyDimsOptContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_emptyDimsOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(655);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				{
				setState(654);
				emptyDims();
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
		enterRule(_localctx, 72, RULE_standardType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(657);
			annotationsOpt();
			setState(660);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				{
				setState(658);
				primitiveType();
				}
				break;

			case 2:
				{
				setState(659);
				standardClassOrInterfaceType();
				}
				break;
			}
			setState(662);
			emptyDimsOpt();
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
		public EmptyDimsOptContext emptyDimsOpt() {
			return getRuleContext(EmptyDimsOptContext.class,0);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public TerminalNode VOID() { return getToken(GroovyParser.VOID, 0); }
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
		enterRule(_localctx, 74, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(664);
			annotationsOpt();
			setState(670);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				{
				setState(667);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case BuiltInPrimitiveType:
					{
					setState(665);
					primitiveType();
					}
					break;
				case VOID:
					{
					setState(666);
					match(VOID);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;

			case 2:
				{
				setState(669);
				generalClassOrInterfaceType();
				}
				break;
			}
			setState(672);
			emptyDimsOpt();
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

	public static class ClassOrInterfaceTypeContext extends GroovyParserRuleContext {
		public QualifiedClassNameContext qualifiedClassName() {
			return getRuleContext(QualifiedClassNameContext.class,0);
		}
		public QualifiedStandardClassNameContext qualifiedStandardClassName() {
			return getRuleContext(QualifiedStandardClassNameContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ClassOrInterfaceTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceType; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassOrInterfaceType(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassOrInterfaceTypeContext classOrInterfaceType() throws RecognitionException {
		ClassOrInterfaceTypeContext _localctx = new ClassOrInterfaceTypeContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_classOrInterfaceType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(676);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(674);
				qualifiedClassName();
				}
				break;

			case 2:
				{
				setState(675);
				qualifiedStandardClassName();
				}
				break;
			}
			setState(679);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(678);
				typeArguments();
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


	@RuleVersion(0)
	public final ClassOrInterfaceTypeContext generalClassOrInterfaceType() throws RecognitionException {
		ClassOrInterfaceTypeContext _localctx = new ClassOrInterfaceTypeContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_generalClassOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(681);
			qualifiedClassName();
			setState(683);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				{
				setState(682);
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


	@RuleVersion(0)
	public final ClassOrInterfaceTypeContext standardClassOrInterfaceType() throws RecognitionException {
		ClassOrInterfaceTypeContext _localctx = new ClassOrInterfaceTypeContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_standardClassOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(685);
			qualifiedStandardClassName();
			setState(687);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				{
				setState(686);
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
		enterRule(_localctx, 82, RULE_primitiveType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(689);
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
		enterRule(_localctx, 84, RULE_typeArguments);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(691);
			match(LT);
			setState(692);
			nls();
			setState(693);
			typeArgument();
			setState(700);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(694);
					match(COMMA);
					setState(695);
					nls();
					setState(696);
					typeArgument();
					}
					} 
				}
				setState(702);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			}
			setState(703);
			nls();
			setState(704);
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
		enterRule(_localctx, 86, RULE_typeArgument);
		int _la;
		try {
			setState(715);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(706);
				type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(707);
				annotationsOpt();
				setState(708);
				match(QUESTION);
				setState(713);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
				case 1:
					{
					setState(709);
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
					setState(710);
					nls();
					setState(711);
					type();
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
		enterRule(_localctx, 88, RULE_annotatedQualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(717);
			annotationsOpt();
			setState(718);
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
		enterRule(_localctx, 90, RULE_qualifiedClassNameList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(720);
			annotatedQualifiedClassName();
			setState(727);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(721);
					match(COMMA);
					setState(722);
					nls();
					setState(723);
					annotatedQualifiedClassName();
					}
					} 
				}
				setState(729);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
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
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
		}
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
		enterRule(_localctx, 92, RULE_formalParameters);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(730);
			match(LPAREN);
			setState(732);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				{
				setState(731);
				formalParameterList();
				}
				break;
			}
			setState(734);
			rparen();
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
		enterRule(_localctx, 94, RULE_formalParameterList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(738);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				{
				setState(736);
				formalParameter();
				}
				break;

			case 2:
				{
				setState(737);
				thisFormalParameter();
				}
				break;
			}
			setState(746);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(740);
					match(COMMA);
					setState(741);
					nls();
					setState(742);
					formalParameter();
					}
					} 
				}
				setState(748);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
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
		enterRule(_localctx, 96, RULE_thisFormalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(749);
			type();
			setState(750);
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
		enterRule(_localctx, 98, RULE_formalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(752);
			variableModifiersOpt();
			setState(754);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,68,_ctx) ) {
			case 1:
				{
				setState(753);
				type();
				}
				break;
			}
			setState(757);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(756);
				match(ELLIPSIS);
				}
				break;
			}
			setState(759);
			variableDeclaratorId();
			setState(765);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(760);
				nls();
				setState(761);
				match(ASSIGN);
				setState(762);
				nls();
				setState(763);
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
		enterRule(_localctx, 100, RULE_methodBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(767);
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
		enterRule(_localctx, 102, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(769);
			qualifiedNameElement();
			setState(774);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(770);
					match(DOT);
					setState(771);
					qualifiedNameElement();
					}
					} 
				}
				setState(776);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
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
		enterRule(_localctx, 104, RULE_qualifiedNameElement);
		try {
			setState(782);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(777);
				identifier();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(778);
				match(DEF);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(779);
				match(IN);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(780);
				match(AS);
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(781);
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
		enterRule(_localctx, 106, RULE_qualifiedNameElements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(789);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(784);
					qualifiedNameElement();
					setState(785);
					match(DOT);
					}
					} 
				}
				setState(791);
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
		enterRule(_localctx, 108, RULE_qualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(792);
			qualifiedNameElements();
			setState(793);
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
		enterRule(_localctx, 110, RULE_qualifiedStandardClassName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(795);
			qualifiedNameElements();
			setState(796);
			className();
			setState(801);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(797);
					match(DOT);
					setState(798);
					className();
					}
					} 
				}
				setState(803);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
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
		enterRule(_localctx, 112, RULE_literal);
		try {
			setState(809);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IntegerLiteral:
				_localctx = new IntegerLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(804);
				match(IntegerLiteral);
				}
				break;
			case FloatingPointLiteral:
				_localctx = new FloatingPointLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(805);
				match(FloatingPointLiteral);
				}
				break;
			case StringLiteral:
				_localctx = new StringLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(806);
				stringLiteral();
				}
				break;
			case BooleanLiteral:
				_localctx = new BooleanLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(807);
				match(BooleanLiteral);
				}
				break;
			case NullLiteral:
				_localctx = new NullLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(808);
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
		enterRule(_localctx, 114, RULE_gstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(811);
			match(GStringBegin);
			setState(812);
			gstringValue();
			setState(817);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPart) {
				{
				{
				setState(813);
				match(GStringPart);
				setState(814);
				gstringValue();
				}
				}
				setState(819);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(820);
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
		public TerminalNode LBRACE() { return getToken(GroovyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public StatementExpressionContext statementExpression() {
			return getRuleContext(StatementExpressionContext.class,0);
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
		enterRule(_localctx, 116, RULE_gstringValue);
		try {
			setState(829);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(822);
				gstringPath();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(823);
				match(LBRACE);
				setState(825);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
				case 1:
					{
					setState(824);
					statementExpression();
					}
					break;
				}
				setState(827);
				match(RBRACE);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(828);
				closure();
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
		enterRule(_localctx, 118, RULE_gstringPath);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(831);
			identifier();
			setState(835);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPathPart) {
				{
				{
				setState(832);
				match(GStringPathPart);
				}
				}
				setState(837);
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
		enterRule(_localctx, 120, RULE_lambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(838);
			lambdaParameters();
			setState(839);
			nls();
			setState(840);
			match(ARROW);
			setState(841);
			nls();
			setState(842);
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
		enterRule(_localctx, 122, RULE_standardLambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(844);
			standardLambdaParameters();
			setState(845);
			nls();
			setState(846);
			match(ARROW);
			setState(847);
			nls();
			setState(848);
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
		enterRule(_localctx, 124, RULE_lambdaParameters);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(850);
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
		enterRule(_localctx, 126, RULE_standardLambdaParameters);
		try {
			setState(854);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(852);
				formalParameters();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(853);
				variableDeclaratorId();
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
		enterRule(_localctx, 128, RULE_lambdaBody);
		try {
			setState(858);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(856);
				block();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(857);
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
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public BlockStatementsOptContext blockStatementsOpt() {
			return getRuleContext(BlockStatementsOptContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
		public TerminalNode ARROW() { return getToken(GroovyParser.ARROW, 0); }
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
		enterRule(_localctx, 130, RULE_closure);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(860);
			match(LBRACE);
			setState(861);
			nls();
			setState(869);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
			case 1:
				{
				setState(863);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
				case 1:
					{
					setState(862);
					formalParameterList();
					}
					break;
				}
				setState(865);
				nls();
				setState(866);
				match(ARROW);
				setState(867);
				nls();
				}
				break;
			}
			setState(871);
			blockStatementsOpt();
			setState(872);
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
		enterRule(_localctx, 132, RULE_closureOrLambdaExpression);
		try {
			setState(876);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(874);
				closure();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(875);
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
		enterRule(_localctx, 134, RULE_blockStatementsOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(879);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
			case 1:
				{
				setState(878);
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
		enterRule(_localctx, 136, RULE_blockStatements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(881);
			blockStatement();
			setState(887);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,86,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(882);
					sep();
					setState(883);
					blockStatement();
					}
					} 
				}
				setState(889);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,86,_ctx);
			}
			setState(891);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
			case 1:
				{
				setState(890);
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
		enterRule(_localctx, 138, RULE_annotationsOpt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(904);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
			case 1:
				{
				setState(893);
				annotation();
				setState(899);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(894);
						nls();
						setState(895);
						annotation();
						}
						} 
					}
					setState(901);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
				}
				setState(902);
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

	public static class AnnotationContext extends GroovyParserRuleContext {
		public TerminalNode AT() { return getToken(GroovyParser.AT, 0); }
		public AnnotationNameContext annotationName() {
			return getRuleContext(AnnotationNameContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
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
		enterRule(_localctx, 140, RULE_annotation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(906);
			match(AT);
			setState(907);
			annotationName();
			setState(913);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				{
				setState(908);
				match(LPAREN);
				setState(910);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(909);
					elementValues();
					}
					break;
				}
				setState(912);
				rparen();
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
		enterRule(_localctx, 142, RULE_elementValues);
		try {
			setState(917);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(915);
				elementValuePairs();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(916);
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
		enterRule(_localctx, 144, RULE_annotationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(919);
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
		enterRule(_localctx, 146, RULE_elementValuePairs);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(921);
			elementValuePair();
			setState(926);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(922);
					match(COMMA);
					setState(923);
					elementValuePair();
					}
					} 
				}
				setState(928);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
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
		enterRule(_localctx, 148, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(929);
			elementValuePairName();
			setState(930);
			nls();
			setState(931);
			match(ASSIGN);
			setState(932);
			nls();
			setState(933);
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
		enterRule(_localctx, 150, RULE_elementValuePairName);
		try {
			setState(937);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(935);
				identifier();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(936);
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
		enterRule(_localctx, 152, RULE_elementValue);
		try {
			setState(942);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(939);
				elementValueArrayInitializer();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(940);
				annotation();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(941);
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
		enterRule(_localctx, 154, RULE_elementValueArrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(944);
			match(LBRACK);
			setState(956);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				{
				setState(945);
				elementValue();
				setState(950);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(946);
						match(COMMA);
						setState(947);
						elementValue();
						}
						} 
					}
					setState(952);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
				}
				setState(954);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(953);
					match(COMMA);
					}
				}

				}
				break;
			}
			setState(958);
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
		enterRule(_localctx, 156, RULE_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(960);
			match(LBRACE);
			setState(962);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				{
				setState(961);
				sep();
				}
				break;
			}
			setState(964);
			blockStatementsOpt();
			setState(965);
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
		enterRule(_localctx, 158, RULE_blockStatement);
		try {
			setState(969);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(967);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(968);
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
		enterRule(_localctx, 160, RULE_localVariableDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(971);
			if (!( !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ");
			setState(972);
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

	public static class ClassifiedModifiersContext extends GroovyParserRuleContext {
		public int t;
		public ModifiersContext modifiers() {
			return getRuleContext(ModifiersContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public ClassifiedModifiersContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public ClassifiedModifiersContext(ParserRuleContext parent, int invokingState, int t) {
			super(parent, invokingState);
			this.t = t;
		}
		@Override public int getRuleIndex() { return RULE_classifiedModifiers; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassifiedModifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassifiedModifiersContext classifiedModifiers(int t) throws RecognitionException {
		ClassifiedModifiersContext _localctx = new ClassifiedModifiersContext(_ctx, getState(), t);
		enterRule(_localctx, 162, RULE_classifiedModifiers);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(974);
			modifiers();
			setState(975);
			nls();
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
		public ClassifiedModifiersContext classifiedModifiers() {
			return getRuleContext(ClassifiedModifiersContext.class,0);
		}
		public VariableDeclaratorsContext variableDeclarators() {
			return getRuleContext(VariableDeclaratorsContext.class,0);
		}
		public TypeNamePairsContext typeNamePairs() {
			return getRuleContext(TypeNamePairsContext.class,0);
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
		enterRule(_localctx, 164, RULE_variableDeclaration);
		try {
			setState(993);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,103,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(977);
				classifiedModifiers(_localctx.t);
				setState(988);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
				case 1:
					{
					setState(979);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
					case 1:
						{
						setState(978);
						type();
						}
						break;
					}
					setState(981);
					variableDeclarators();
					}
					break;

				case 2:
					{
					setState(982);
					typeNamePairs();
					setState(983);
					nls();
					setState(984);
					match(ASSIGN);
					setState(985);
					nls();
					setState(986);
					variableInitializer();
					}
					break;
				}
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(990);
				type();
				setState(991);
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
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
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
		enterRule(_localctx, 166, RULE_typeNamePairs);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(995);
			match(LPAREN);
			setState(996);
			typeNamePair();
			setState(1001);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(997);
					match(COMMA);
					setState(998);
					typeNamePair();
					}
					} 
				}
				setState(1003);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
			}
			setState(1004);
			rparen();
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
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
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
		enterRule(_localctx, 168, RULE_typeNamePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1007);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
			case 1:
				{
				setState(1006);
				type();
				}
				break;
			}
			setState(1009);
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
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
		}
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
		enterRule(_localctx, 170, RULE_variableNames);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1011);
			match(LPAREN);
			setState(1012);
			variableDeclaratorId();
			setState(1015); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1013);
					match(COMMA);
					setState(1014);
					variableDeclaratorId();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1017); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
			} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(1019);
			rparen();
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
		enterRule(_localctx, 172, RULE_conditionalStatement);
		try {
			setState(1023);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IF:
				enterOuterAlt(_localctx, 1);
				{
				setState(1021);
				ifElseStatement();
				}
				break;
			case SWITCH:
				enterOuterAlt(_localctx, 2);
				{
				setState(1022);
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
		enterRule(_localctx, 174, RULE_ifElseStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1025);
			match(IF);
			setState(1026);
			expressionInPar();
			setState(1027);
			nls();
			setState(1028);
			_localctx.tb = statement();
			setState(1037);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
			case 1:
				{
				setState(1031);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,108,_ctx) ) {
				case 1:
					{
					setState(1029);
					nls();
					}
					break;

				case 2:
					{
					setState(1030);
					sep();
					}
					break;
				}
				setState(1033);
				match(ELSE);
				setState(1034);
				nls();
				setState(1035);
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
		enterRule(_localctx, 176, RULE_switchStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1039);
			match(SWITCH);
			setState(1040);
			expressionInPar();
			setState(1041);
			nls();
			setState(1042);
			match(LBRACE);
			setState(1043);
			nls();
			setState(1051);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CASE || _la==DEFAULT) {
				{
				setState(1045); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1044);
						switchBlockStatementGroup();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1047); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1049);
				nls();
				}
			}

			setState(1053);
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
		public TerminalNode FOR() { return getToken(GroovyParser.FOR, 0); }
		public TerminalNode LPAREN() { return getToken(GroovyParser.LPAREN, 0); }
		public ForControlContext forControl() {
			return getRuleContext(ForControlContext.class,0);
		}
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
		}
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public ForStmtAltContext(LoopStatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitForStmtAlt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class WhileStmtAltContext extends LoopStatementContext {
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
		enterRule(_localctx, 178, RULE_loopStatement);
		try {
			setState(1074);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FOR:
				_localctx = new ForStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1055);
				match(FOR);
				setState(1056);
				match(LPAREN);
				setState(1057);
				forControl();
				setState(1058);
				rparen();
				setState(1059);
				nls();
				setState(1060);
				statement();
				}
				break;
			case WHILE:
				_localctx = new WhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1062);
				match(WHILE);
				setState(1063);
				expressionInPar();
				setState(1064);
				nls();
				setState(1065);
				statement();
				}
				break;
			case DO:
				_localctx = new DoWhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1067);
				match(DO);
				setState(1068);
				nls();
				setState(1069);
				statement();
				setState(1070);
				nls();
				setState(1071);
				match(WHILE);
				setState(1072);
				expressionInPar();
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
		enterRule(_localctx, 180, RULE_continueStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1076);
			match(CONTINUE);
			setState(1078);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
			case 1:
				{
				setState(1077);
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
		enterRule(_localctx, 182, RULE_breakStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1080);
			match(BREAK);
			setState(1082);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
			case 1:
				{
				setState(1081);
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
		enterRule(_localctx, 184, RULE_tryCatchStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1084);
			match(TRY);
			setState(1086);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				{
				setState(1085);
				resources();
				}
				break;
			}
			setState(1088);
			nls();
			setState(1089);
			block();
			setState(1095);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,116,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1090);
					nls();
					setState(1091);
					catchClause();
					}
					} 
				}
				setState(1097);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,116,_ctx);
			}
			setState(1101);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
			case 1:
				{
				setState(1098);
				nls();
				setState(1099);
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
		enterRule(_localctx, 186, RULE_assertStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1103);
			match(ASSERT);
			setState(1104);
			_localctx.ce = expression(0);
			setState(1110);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
			case 1:
				{
				setState(1105);
				nls();
				setState(1106);
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
				setState(1107);
				nls();
				setState(1108);
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
	public static class MethodDeclarationStmtAltContext extends StatementContext {
		public MethodDeclarationContext methodDeclaration() {
			return getRuleContext(MethodDeclarationContext.class,0);
		}
		public MethodDeclarationStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitMethodDeclarationStmtAlt(this);
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
		enterRule(_localctx, 188, RULE_statement);
		try {
			setState(1140);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
			case 1:
				_localctx = new BlockStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1112);
				block();
				}
				break;

			case 2:
				_localctx = new ConditionalStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1113);
				conditionalStatement();
				}
				break;

			case 3:
				_localctx = new LoopStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1114);
				loopStatement();
				}
				break;

			case 4:
				_localctx = new TryCatchStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1115);
				tryCatchStatement();
				}
				break;

			case 5:
				_localctx = new SynchronizedStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1116);
				match(SYNCHRONIZED);
				setState(1117);
				expressionInPar();
				setState(1118);
				nls();
				setState(1119);
				block();
				}
				break;

			case 6:
				_localctx = new ReturnStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1121);
				match(RETURN);
				setState(1123);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,119,_ctx) ) {
				case 1:
					{
					setState(1122);
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
				setState(1125);
				match(THROW);
				setState(1126);
				expression(0);
				}
				break;

			case 8:
				_localctx = new BreakStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1127);
				breakStatement();
				}
				break;

			case 9:
				_localctx = new ContinueStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1128);
				continueStatement();
				}
				break;

			case 10:
				_localctx = new LabeledStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1129);
				identifier();
				setState(1130);
				match(COLON);
				setState(1131);
				nls();
				setState(1132);
				statement();
				}
				break;

			case 11:
				_localctx = new AssertStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1134);
				assertStatement();
				}
				break;

			case 12:
				_localctx = new LocalVariableDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(1135);
				localVariableDeclaration();
				}
				break;

			case 13:
				_localctx = new MethodDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(1136);
				if (!( !SemanticPredicates.isInvalidMethodDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidMethodDeclaration(_input) ");
				setState(1137);
				methodDeclaration(3, 9);
				}
				break;

			case 14:
				_localctx = new ExpressionStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(1138);
				statementExpression();
				}
				break;

			case 15:
				_localctx = new EmptyStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(1139);
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
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
		}
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
		enterRule(_localctx, 190, RULE_catchClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1142);
			match(CATCH);
			setState(1143);
			match(LPAREN);
			setState(1144);
			variableModifiersOpt();
			setState(1146);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
			case 1:
				{
				setState(1145);
				catchType();
				}
				break;
			}
			setState(1148);
			identifier();
			setState(1149);
			rparen();
			setState(1150);
			nls();
			setState(1151);
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
		enterRule(_localctx, 192, RULE_catchType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1153);
			qualifiedClassName();
			setState(1158);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,122,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1154);
					match(BITOR);
					setState(1155);
					qualifiedClassName();
					}
					} 
				}
				setState(1160);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,122,_ctx);
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
		enterRule(_localctx, 194, RULE_finallyBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1161);
			match(FINALLY);
			setState(1162);
			nls();
			setState(1163);
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
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1165);
			match(LPAREN);
			setState(1166);
			nls();
			setState(1167);
			resourceList();
			setState(1169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,123,_ctx) ) {
			case 1:
				{
				setState(1168);
				sep();
				}
				break;
			}
			setState(1171);
			rparen();
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
		enterRule(_localctx, 198, RULE_resourceList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1173);
			resource();
			setState(1179);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,124,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1174);
					sep();
					setState(1175);
					resource();
					}
					} 
				}
				setState(1181);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,124,_ctx);
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
		enterRule(_localctx, 200, RULE_resource);
		try {
			setState(1184);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1182);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1183);
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
		enterRule(_localctx, 202, RULE_switchBlockStatementGroup);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1186);
			switchLabel();
			setState(1192);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,126,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1187);
					nls();
					setState(1188);
					switchLabel();
					}
					} 
				}
				setState(1194);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,126,_ctx);
			}
			setState(1195);
			nls();
			setState(1196);
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
		enterRule(_localctx, 204, RULE_switchLabel);
		try {
			setState(1204);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1198);
				match(CASE);
				setState(1199);
				expression(0);
				setState(1200);
				match(COLON);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1202);
				match(DEFAULT);
				setState(1203);
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
		public ClassicalForControlContext classicalForControl() {
			return getRuleContext(ClassicalForControlContext.class,0);
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
			setState(1208);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1206);
				enhancedForControl();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1207);
				classicalForControl();
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
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
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
			setState(1210);
			variableModifiersOpt();
			setState(1212);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,129,_ctx) ) {
			case 1:
				{
				setState(1211);
				type();
				}
				break;
			}
			setState(1214);
			variableDeclaratorId();
			setState(1215);
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
			setState(1216);
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

	public static class ClassicalForControlContext extends GroovyParserRuleContext {
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
		public ClassicalForControlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classicalForControl; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitClassicalForControl(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final ClassicalForControlContext classicalForControl() throws RecognitionException {
		ClassicalForControlContext _localctx = new ClassicalForControlContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_classicalForControl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1219);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
			case 1:
				{
				setState(1218);
				forInit();
				}
				break;
			}
			setState(1221);
			match(SEMI);
			setState(1223);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
			case 1:
				{
				setState(1222);
				expression(0);
				}
				break;
			}
			setState(1225);
			match(SEMI);
			setState(1227);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				{
				setState(1226);
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
			setState(1231);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,133,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1229);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1230);
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
			setState(1233);
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
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
		}
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
			setState(1235);
			match(LPAREN);
			setState(1236);
			type();
			setState(1237);
			rparen();
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
		enterRule(_localctx, 218, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1239);
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
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
		}
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
		enterRule(_localctx, 220, RULE_expressionInPar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1241);
			match(LPAREN);
			setState(1242);
			enhancedStatementExpression();
			setState(1243);
			rparen();
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
		enterRule(_localctx, 222, RULE_expressionList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1245);
			expressionListElement(_localctx.canSpread);
			setState(1250);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1246);
					match(COMMA);
					setState(1247);
					expressionListElement(_localctx.canSpread);
					}
					} 
				}
				setState(1252);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
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
		enterRule(_localctx, 224, RULE_expressionListElement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1254);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,135,_ctx) ) {
			case 1:
				{
				setState(1253);
				match(MUL);
				}
				break;
			}
			setState(1256);
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
		enterRule(_localctx, 226, RULE_enhancedStatementExpression);
		try {
			setState(1260);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,136,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1258);
				statementExpression();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1259);
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
		enterRule(_localctx, 228, RULE_statementExpression);
		try {
			_localctx = new CommandExprAltContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1262);
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
		enterRule(_localctx, 230, RULE_postfixExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1264);
			pathExpression();
			setState(1266);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
			case 1:
				{
				setState(1265);
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
		public TerminalNode RANGE_EXCLUSIVE() { return getToken(GroovyParser.RANGE_EXCLUSIVE, 0); }
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
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode AS() { return getToken(GroovyParser.AS, 0); }
		public TerminalNode INSTANCEOF() { return getToken(GroovyParser.INSTANCEOF, 0); }
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
		public List<? extends NlsContext> nls() {
			return getRuleContexts(NlsContext.class);
		}
		public NlsContext nls(int i) {
			return getRuleContext(NlsContext.class,i);
		}
		public EnhancedStatementExpressionContext enhancedStatementExpression() {
			return getRuleContext(EnhancedStatementExpressionContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
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
		int _startState = 232;
		enterRecursionRule(_localctx, 232, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1285);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,138,_ctx) ) {
			case 1:
				{
				_localctx = new CastExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1269);
				castParExpression();
				setState(1270);
				castOperandExpression();
				}
				break;

			case 2:
				{
				_localctx = new PostfixExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1272);
				postfixExpression();
				}
				break;

			case 3:
				{
				_localctx = new UnaryNotExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1273);
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
				setState(1274);
				nls();
				setState(1275);
				expression(18);
				}
				break;

			case 4:
				{
				_localctx = new UnaryAddExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1277);
				((UnaryAddExprAltContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 100)) & ~0x3f) == 0 && ((1L << (_la - 100)) & ((1L << (INC - 100)) | (1L << (DEC - 100)) | (1L << (ADD - 100)) | (1L << (SUB - 100)))) != 0)) ) {
					((UnaryAddExprAltContext)_localctx).op = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1278);
				expression(16);
				}
				break;

			case 5:
				{
				_localctx = new MultipleAssignmentExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1279);
				((MultipleAssignmentExprAltContext)_localctx).left = variableNames();
				setState(1280);
				nls();
				setState(1281);
				((MultipleAssignmentExprAltContext)_localctx).op = match(ASSIGN);
				setState(1282);
				nls();
				setState(1283);
				((MultipleAssignmentExprAltContext)_localctx).right = statementExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1397);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,143,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1395);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,142,_ctx) ) {
					case 1:
						{
						_localctx = new PowerExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((PowerExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1287);
						if (!(precpred(_ctx, 17))) throw createFailedPredicateException("precpred(_ctx, 17)");
						setState(1288);
						((PowerExprAltContext)_localctx).op = match(POWER);
						setState(1289);
						nls();
						setState(1290);
						((PowerExprAltContext)_localctx).right = expression(18);
						}
						break;

					case 2:
						{
						_localctx = new MultiplicativeExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((MultiplicativeExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1292);
						if (!(precpred(_ctx, 15))) throw createFailedPredicateException("precpred(_ctx, 15)");
						setState(1293);
						nls();
						setState(1294);
						((MultiplicativeExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 104)) & ~0x3f) == 0 && ((1L << (_la - 104)) & ((1L << (MUL - 104)) | (1L << (DIV - 104)) | (1L << (MOD - 104)))) != 0)) ) {
							((MultiplicativeExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1295);
						nls();
						setState(1296);
						((MultiplicativeExprAltContext)_localctx).right = expression(16);
						}
						break;

					case 3:
						{
						_localctx = new AdditiveExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AdditiveExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1298);
						if (!(precpred(_ctx, 14))) throw createFailedPredicateException("precpred(_ctx, 14)");
						setState(1299);
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
						setState(1300);
						nls();
						setState(1301);
						((AdditiveExprAltContext)_localctx).right = expression(15);
						}
						break;

					case 4:
						{
						_localctx = new ShiftExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ShiftExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1303);
						if (!(precpred(_ctx, 13))) throw createFailedPredicateException("precpred(_ctx, 13)");
						setState(1304);
						nls();
						setState(1315);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case GT:
						case LT:
							{
							setState(1312);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,139,_ctx) ) {
							case 1:
								{
								setState(1305);
								((ShiftExprAltContext)_localctx).dlOp = match(LT);
								setState(1306);
								match(LT);
								}
								break;

							case 2:
								{
								setState(1307);
								((ShiftExprAltContext)_localctx).tgOp = match(GT);
								setState(1308);
								match(GT);
								setState(1309);
								match(GT);
								}
								break;

							case 3:
								{
								setState(1310);
								((ShiftExprAltContext)_localctx).dgOp = match(GT);
								setState(1311);
								match(GT);
								}
								break;
							}
							}
							break;
						case RANGE_INCLUSIVE:
						case RANGE_EXCLUSIVE:
							{
							setState(1314);
							((ShiftExprAltContext)_localctx).rangeOp = _input.LT(1);
							_la = _input.LA(1);
							if ( !(_la==RANGE_INCLUSIVE || _la==RANGE_EXCLUSIVE) ) {
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
						setState(1317);
						nls();
						setState(1318);
						((ShiftExprAltContext)_localctx).right = expression(14);
						}
						break;

					case 5:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1320);
						if (!(precpred(_ctx, 11))) throw createFailedPredicateException("precpred(_ctx, 11)");
						setState(1321);
						nls();
						setState(1322);
						((RelationalExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==IN || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & ((1L << (NOT_IN - 77)) | (1L << (GT - 77)) | (1L << (LT - 77)) | (1L << (LE - 77)) | (1L << (GE - 77)))) != 0)) ) {
							((RelationalExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1323);
						nls();
						setState(1324);
						((RelationalExprAltContext)_localctx).right = expression(12);
						}
						break;

					case 6:
						{
						_localctx = new EqualityExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((EqualityExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1326);
						if (!(precpred(_ctx, 10))) throw createFailedPredicateException("precpred(_ctx, 10)");
						setState(1327);
						nls();
						setState(1328);
						((EqualityExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & ((1L << (SPACESHIP - 72)) | (1L << (IDENTICAL - 72)) | (1L << (NOT_IDENTICAL - 72)) | (1L << (EQUAL - 72)) | (1L << (NOTEQUAL - 72)))) != 0)) ) {
							((EqualityExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1329);
						nls();
						setState(1330);
						((EqualityExprAltContext)_localctx).right = expression(11);
						}
						break;

					case 7:
						{
						_localctx = new RegexExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RegexExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1332);
						if (!(precpred(_ctx, 9))) throw createFailedPredicateException("precpred(_ctx, 9)");
						setState(1333);
						nls();
						setState(1334);
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
						setState(1335);
						nls();
						setState(1336);
						((RegexExprAltContext)_localctx).right = expression(10);
						}
						break;

					case 8:
						{
						_localctx = new AndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1338);
						if (!(precpred(_ctx, 8))) throw createFailedPredicateException("precpred(_ctx, 8)");
						setState(1339);
						nls();
						setState(1340);
						((AndExprAltContext)_localctx).op = match(BITAND);
						setState(1341);
						nls();
						setState(1342);
						((AndExprAltContext)_localctx).right = expression(9);
						}
						break;

					case 9:
						{
						_localctx = new ExclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ExclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1344);
						if (!(precpred(_ctx, 7))) throw createFailedPredicateException("precpred(_ctx, 7)");
						setState(1345);
						nls();
						setState(1346);
						((ExclusiveOrExprAltContext)_localctx).op = match(XOR);
						setState(1347);
						nls();
						setState(1348);
						((ExclusiveOrExprAltContext)_localctx).right = expression(8);
						}
						break;

					case 10:
						{
						_localctx = new InclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((InclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1350);
						if (!(precpred(_ctx, 6))) throw createFailedPredicateException("precpred(_ctx, 6)");
						setState(1351);
						nls();
						setState(1352);
						((InclusiveOrExprAltContext)_localctx).op = match(BITOR);
						setState(1353);
						nls();
						setState(1354);
						((InclusiveOrExprAltContext)_localctx).right = expression(7);
						}
						break;

					case 11:
						{
						_localctx = new LogicalAndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalAndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1356);
						if (!(precpred(_ctx, 5))) throw createFailedPredicateException("precpred(_ctx, 5)");
						setState(1357);
						nls();
						setState(1358);
						((LogicalAndExprAltContext)_localctx).op = match(AND);
						setState(1359);
						nls();
						setState(1360);
						((LogicalAndExprAltContext)_localctx).right = expression(6);
						}
						break;

					case 12:
						{
						_localctx = new LogicalOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1362);
						if (!(precpred(_ctx, 4))) throw createFailedPredicateException("precpred(_ctx, 4)");
						setState(1363);
						nls();
						setState(1364);
						((LogicalOrExprAltContext)_localctx).op = match(OR);
						setState(1365);
						nls();
						setState(1366);
						((LogicalOrExprAltContext)_localctx).right = expression(5);
						}
						break;

					case 13:
						{
						_localctx = new ConditionalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ConditionalExprAltContext)_localctx).con = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1368);
						if (!(precpred(_ctx, 3))) throw createFailedPredicateException("precpred(_ctx, 3)");
						setState(1369);
						nls();
						setState(1379);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case QUESTION:
							{
							setState(1370);
							match(QUESTION);
							setState(1371);
							nls();
							setState(1372);
							((ConditionalExprAltContext)_localctx).tb = expression(0);
							setState(1373);
							nls();
							setState(1374);
							match(COLON);
							setState(1375);
							nls();
							}
							break;
						case ELVIS:
							{
							setState(1377);
							match(ELVIS);
							setState(1378);
							nls();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1381);
						((ConditionalExprAltContext)_localctx).fb = expression(3);
						}
						break;

					case 14:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1383);
						if (!(precpred(_ctx, 12))) throw createFailedPredicateException("precpred(_ctx, 12)");
						setState(1384);
						nls();
						setState(1385);
						((RelationalExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==AS || _la==INSTANCEOF || _la==NOT_INSTANCEOF) ) {
							((RelationalExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1386);
						nls();
						setState(1387);
						type();
						}
						break;

					case 15:
						{
						_localctx = new AssignmentExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AssignmentExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1389);
						if (!(precpred(_ctx, 1))) throw createFailedPredicateException("precpred(_ctx, 1)");
						setState(1390);
						nls();
						setState(1391);
						((AssignmentExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & ((1L << (POWER_ASSIGN - 71)) | (1L << (ASSIGN - 71)) | (1L << (ADD_ASSIGN - 71)) | (1L << (SUB_ASSIGN - 71)) | (1L << (MUL_ASSIGN - 71)) | (1L << (DIV_ASSIGN - 71)) | (1L << (AND_ASSIGN - 71)) | (1L << (OR_ASSIGN - 71)) | (1L << (XOR_ASSIGN - 71)) | (1L << (MOD_ASSIGN - 71)) | (1L << (LSHIFT_ASSIGN - 71)) | (1L << (RSHIFT_ASSIGN - 71)) | (1L << (URSHIFT_ASSIGN - 71)) | (1L << (ELVIS_ASSIGN - 71)))) != 0)) ) {
							((AssignmentExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1392);
						nls();
						setState(1393);
						enhancedStatementExpression();
						}
						break;
					}
					} 
				}
				setState(1399);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,143,_ctx);
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
		enterRule(_localctx, 234, RULE_castOperandExpression);
		int _la;
		try {
			setState(1410);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
			case 1:
				_localctx = new CastExprAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1400);
				castParExpression();
				setState(1401);
				castOperandExpression();
				}
				break;

			case 2:
				_localctx = new PostfixExprAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1403);
				postfixExpression();
				}
				break;

			case 3:
				_localctx = new UnaryNotExprAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1404);
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
				setState(1405);
				nls();
				setState(1406);
				castOperandExpression();
				}
				break;

			case 4:
				_localctx = new UnaryAddExprAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1408);
				((UnaryAddExprAltContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 100)) & ~0x3f) == 0 && ((1L << (_la - 100)) & ((1L << (INC - 100)) | (1L << (DEC - 100)) | (1L << (ADD - 100)) | (1L << (SUB - 100)))) != 0)) ) {
					((UnaryAddExprAltContext)_localctx).op = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1409);
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
		public EnhancedArgumentListContext enhancedArgumentList() {
			return getRuleContext(EnhancedArgumentListContext.class,0);
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
		enterRule(_localctx, 236, RULE_commandExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1412);
			_localctx.expression = expression(0);
			setState(1416);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
			case 1:
				{
				setState(1413);
				if (!( !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) )) throw createFailedPredicateException(" !SemanticPredicates.isFollowingArgumentsOrClosure($expression.ctx) ");
				setState(1414);
				argumentList();
				}
				break;

			case 2:
				{
				}
				break;
			}
			setState(1421);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1418);
					commandArgument();
					}
					} 
				}
				setState(1423);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
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
		public EnhancedArgumentListContext enhancedArgumentList() {
			return getRuleContext(EnhancedArgumentListContext.class,0);
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
		enterRule(_localctx, 238, RULE_commandArgument);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1424);
			primary();
			setState(1431);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
			case 1:
				{
				setState(1426); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1425);
						pathElement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1428); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;

			case 2:
				{
				setState(1430);
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
		enterRule(_localctx, 240, RULE_pathExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1433);
			primary();
			setState(1439);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,149,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1434);
					_localctx.pathElement = pathElement();
					 _localctx.t =  _localctx.pathElement.t; 
					}
					} 
				}
				setState(1441);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,149,_ctx);
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
		public NamePartContext namePart() {
			return getRuleContext(NamePartContext.class,0);
		}
		public TerminalNode DOT() { return getToken(GroovyParser.DOT, 0); }
		public TerminalNode NEW() { return getToken(GroovyParser.NEW, 0); }
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
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
		enterRule(_localctx, 242, RULE_pathElement);
		int _la;
		try {
			setState(1478);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,153,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1442);
				nls();
				setState(1467);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,152,_ctx) ) {
				case 1:
					{
					setState(1453);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case SPREAD_DOT:
					case SAFE_DOT:
					case SAFE_CHAIN_DOT:
					case DOT:
						{
						setState(1443);
						_la = _input.LA(1);
						if ( !(((((_la - 62)) & ~0x3f) == 0 && ((1L << (_la - 62)) & ((1L << (SPREAD_DOT - 62)) | (1L << (SAFE_DOT - 62)) | (1L << (SAFE_CHAIN_DOT - 62)) | (1L << (DOT - 62)))) != 0)) ) {
						_errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1444);
						nls();
						setState(1447);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,150,_ctx) ) {
						case 1:
							{
							setState(1445);
							match(AT);
							}
							break;

						case 2:
							{
							setState(1446);
							nonWildcardTypeArguments();
							}
							break;
						}
						}
						break;
					case METHOD_POINTER:
						{
						setState(1449);
						match(METHOD_POINTER);
						setState(1450);
						nls();
						}
						break;
					case METHOD_REFERENCE:
						{
						setState(1451);
						match(METHOD_REFERENCE);
						setState(1452);
						nls();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1455);
					namePart();
					 _localctx.t =  1; 
					}
					break;

				case 2:
					{
					setState(1458);
					match(DOT);
					setState(1459);
					nls();
					setState(1460);
					match(NEW);
					setState(1461);
					creator(1);
					 _localctx.t =  6; 
					}
					break;

				case 3:
					{
					setState(1464);
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
				setState(1469);
				arguments();
				 _localctx.t =  2; 
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1472);
				indexPropertyArgs();
				 _localctx.t =  4; 
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1475);
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
		enterRule(_localctx, 244, RULE_namePart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1484);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,154,_ctx) ) {
			case 1:
				{
				setState(1480);
				identifier();
				}
				break;

			case 2:
				{
				setState(1481);
				stringLiteral();
				}
				break;

			case 3:
				{
				setState(1482);
				dynamicMemberName();
				}
				break;

			case 4:
				{
				setState(1483);
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
		enterRule(_localctx, 246, RULE_dynamicMemberName);
		try {
			setState(1488);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1486);
				parExpression();
				}
				break;
			case GStringBegin:
				enterOuterAlt(_localctx, 2);
				{
				setState(1487);
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
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public TerminalNode QUESTION() { return getToken(GroovyParser.QUESTION, 0); }
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
		enterRule(_localctx, 248, RULE_indexPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1491);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==QUESTION) {
				{
				setState(1490);
				match(QUESTION);
				}
			}

			setState(1493);
			match(LBRACK);
			setState(1495);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,157,_ctx) ) {
			case 1:
				{
				setState(1494);
				expressionList(true);
				}
				break;
			}
			setState(1497);
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
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public MapEntryListContext mapEntryList() {
			return getRuleContext(MapEntryListContext.class,0);
		}
		public TerminalNode COLON() { return getToken(GroovyParser.COLON, 0); }
		public TerminalNode QUESTION() { return getToken(GroovyParser.QUESTION, 0); }
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
		enterRule(_localctx, 250, RULE_namedPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1500);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==QUESTION) {
				{
				setState(1499);
				match(QUESTION);
				}
			}

			setState(1502);
			match(LBRACK);
			setState(1505);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				{
				setState(1503);
				mapEntryList();
				}
				break;

			case 2:
				{
				setState(1504);
				match(COLON);
				}
				break;
			}
			setState(1507);
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
		enterRule(_localctx, 252, RULE_primary);
		try {
			setState(1526);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,161,_ctx) ) {
			case 1:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1509);
				identifier();
				setState(1511);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
				case 1:
					{
					setState(1510);
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
				setState(1513);
				literal();
				}
				break;

			case 3:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1514);
				gstring();
				}
				break;

			case 4:
				_localctx = new NewPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1515);
				match(NEW);
				setState(1516);
				nls();
				setState(1517);
				creator(0);
				}
				break;

			case 5:
				_localctx = new ThisPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1519);
				match(THIS);
				}
				break;

			case 6:
				_localctx = new SuperPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1520);
				match(SUPER);
				}
				break;

			case 7:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1521);
				parExpression();
				}
				break;

			case 8:
				_localctx = new ClosureOrLambdaExpressionPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1522);
				closureOrLambdaExpression();
				}
				break;

			case 9:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1523);
				list();
				}
				break;

			case 10:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1524);
				map();
				}
				break;

			case 11:
				_localctx = new BuiltInTypePrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1525);
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
		enterRule(_localctx, 254, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1528);
			match(LBRACK);
			setState(1530);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,162,_ctx) ) {
			case 1:
				{
				setState(1529);
				expressionList(true);
				}
				break;
			}
			setState(1533);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1532);
				match(COMMA);
				}
			}

			setState(1535);
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
		enterRule(_localctx, 256, RULE_map);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1537);
			match(LBRACK);
			setState(1543);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,165,_ctx) ) {
			case 1:
				{
				setState(1538);
				mapEntryList();
				setState(1540);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1539);
					match(COMMA);
					}
				}

				}
				break;

			case 2:
				{
				setState(1542);
				match(COLON);
				}
				break;
			}
			setState(1545);
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
		enterRule(_localctx, 258, RULE_mapEntryList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1547);
			mapEntry();
			setState(1552);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,166,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1548);
					match(COMMA);
					setState(1549);
					mapEntry();
					}
					} 
				}
				setState(1554);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,166,_ctx);
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
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
		enterRule(_localctx, 260, RULE_mapEntry);
		try {
			setState(1565);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,167,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1555);
				mapEntryLabel();
				setState(1556);
				match(COLON);
				setState(1557);
				nls();
				setState(1558);
				expression(0);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1560);
				match(MUL);
				setState(1561);
				match(COLON);
				setState(1562);
				nls();
				setState(1563);
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
		enterRule(_localctx, 262, RULE_mapEntryLabel);
		try {
			setState(1569);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1567);
				keywords();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1568);
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
		public AnonymousInnerClassDeclarationContext anonymousInnerClassDeclaration() {
			return getRuleContext(AnonymousInnerClassDeclarationContext.class,0);
		}
		public List<? extends DimContext> dim() {
			return getRuleContexts(DimContext.class);
		}
		public DimContext dim(int i) {
			return getRuleContext(DimContext.class,i);
		}
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
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
		enterRule(_localctx, 264, RULE_creator);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1571);
			createdName();
			setState(1587);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,172,_ctx) ) {
			case 1:
				{
				setState(1572);
				nls();
				setState(1573);
				arguments();
				setState(1575);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,169,_ctx) ) {
				case 1:
					{
					setState(1574);
					anonymousInnerClassDeclaration(0);
					}
					break;
				}
				}
				break;

			case 2:
				{
				setState(1578); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1577);
						dim();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1580); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1585);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,171,_ctx) ) {
				case 1:
					{
					setState(1582);
					nls();
					setState(1583);
					arrayInitializer();
					}
					break;
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

	public static class DimContext extends GroovyParserRuleContext {
		public AnnotationsOptContext annotationsOpt() {
			return getRuleContext(AnnotationsOptContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(GroovyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(GroovyParser.RBRACK, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public DimContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dim; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitDim(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final DimContext dim() throws RecognitionException {
		DimContext _localctx = new DimContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_dim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1589);
			annotationsOpt();
			setState(1590);
			match(LBRACK);
			setState(1592);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,173,_ctx) ) {
			case 1:
				{
				setState(1591);
				expression(0);
				}
				break;
			}
			setState(1594);
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
		public VariableInitializersContext variableInitializers() {
			return getRuleContext(VariableInitializersContext.class,0);
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
		enterRule(_localctx, 268, RULE_arrayInitializer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1596);
			match(LBRACE);
			setState(1597);
			nls();
			setState(1601);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				{
				setState(1598);
				variableInitializers();
				setState(1599);
				nls();
				}
				break;
			}
			setState(1603);
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
		enterRule(_localctx, 270, RULE_anonymousInnerClassDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1605);
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
		enterRule(_localctx, 272, RULE_createdName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1607);
			annotationsOpt();
			setState(1613);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
			case 1:
				{
				setState(1608);
				primitiveType();
				}
				break;

			case 2:
				{
				setState(1609);
				qualifiedClassName();
				setState(1611);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,175,_ctx) ) {
				case 1:
					{
					setState(1610);
					typeArgumentsOrDiamond();
					}
					break;
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
		enterRule(_localctx, 274, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1615);
			match(LT);
			setState(1616);
			nls();
			setState(1617);
			typeList();
			setState(1618);
			nls();
			setState(1619);
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
		enterRule(_localctx, 276, RULE_typeArgumentsOrDiamond);
		try {
			setState(1624);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1621);
				match(LT);
				setState(1622);
				match(GT);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1623);
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
		public RparenContext rparen() {
			return getRuleContext(RparenContext.class,0);
		}
		public EnhancedArgumentListContext enhancedArgumentList() {
			return getRuleContext(EnhancedArgumentListContext.class,0);
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
		enterRule(_localctx, 278, RULE_arguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1626);
			match(LPAREN);
			setState(1628);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
			case 1:
				{
				setState(1627);
				enhancedArgumentList();
				}
				break;
			}
			setState(1631);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,179,_ctx) ) {
			case 1:
				{
				setState(1630);
				match(COMMA);
				}
				break;
			}
			setState(1633);
			rparen();
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
	public final EnhancedArgumentListContext argumentList() throws RecognitionException {
		EnhancedArgumentListContext _localctx = new EnhancedArgumentListContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_argumentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1635);
			argumentListElement();
			setState(1642);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,180,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1636);
					match(COMMA);
					setState(1637);
					nls();
					setState(1638);
					argumentListElement();
					}
					} 
				}
				setState(1644);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,180,_ctx);
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

	public static class EnhancedArgumentListContext extends GroovyParserRuleContext {
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
		public EnhancedArgumentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enhancedArgumentList; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitEnhancedArgumentList(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final EnhancedArgumentListContext enhancedArgumentList() throws RecognitionException {
		EnhancedArgumentListContext _localctx = new EnhancedArgumentListContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_enhancedArgumentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1645);
			enhancedArgumentListElement();
			setState(1652);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,181,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1646);
					match(COMMA);
					setState(1647);
					nls();
					setState(1648);
					enhancedArgumentListElement();
					}
					} 
				}
				setState(1654);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,181,_ctx);
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
	public final EnhancedArgumentListElementContext argumentListElement() throws RecognitionException {
		EnhancedArgumentListElementContext _localctx = new EnhancedArgumentListElementContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_argumentListElement);
		try {
			setState(1657);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,182,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1655);
				expressionListElement(true);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1656);
				mapEntry();
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
		enterRule(_localctx, 286, RULE_enhancedArgumentListElement);
		try {
			setState(1662);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1659);
				expressionListElement(true);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1660);
				standardLambdaExpression();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1661);
				mapEntry();
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
		enterRule(_localctx, 288, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1664);
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
		enterRule(_localctx, 290, RULE_className);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1666);
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
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
		public TerminalNode TRAIT() { return getToken(GroovyParser.TRAIT, 0); }
		public TerminalNode AS() { return getToken(GroovyParser.AS, 0); }
		public TerminalNode STATIC() { return getToken(GroovyParser.STATIC, 0); }
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
		enterRule(_localctx, 292, RULE_identifier);
		try {
			setState(1676);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,184,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1668);
				match(Identifier);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1669);
				match(CapitalizedIdentifier);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1670);
				match(VAR);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1671);
				match(IN);
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1672);
				match(TRAIT);
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1673);
				match(AS);
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1674);
				if (!( DOT == _input.LT(2).getType() )) throw createFailedPredicateException(" DOT == _input.LT(2).getType() ");
				setState(1675);
				match(STATIC);
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
		enterRule(_localctx, 294, RULE_builtInType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1678);
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
		public TerminalNode BREAK() { return getToken(GroovyParser.BREAK, 0); }
		public TerminalNode CASE() { return getToken(GroovyParser.CASE, 0); }
		public TerminalNode CATCH() { return getToken(GroovyParser.CATCH, 0); }
		public TerminalNode CLASS() { return getToken(GroovyParser.CLASS, 0); }
		public TerminalNode CONST() { return getToken(GroovyParser.CONST, 0); }
		public TerminalNode CONTINUE() { return getToken(GroovyParser.CONTINUE, 0); }
		public TerminalNode DEF() { return getToken(GroovyParser.DEF, 0); }
		public TerminalNode DEFAULT() { return getToken(GroovyParser.DEFAULT, 0); }
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
		public TerminalNode PACKAGE() { return getToken(GroovyParser.PACKAGE, 0); }
		public TerminalNode RETURN() { return getToken(GroovyParser.RETURN, 0); }
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
		public TerminalNode VAR() { return getToken(GroovyParser.VAR, 0); }
		public TerminalNode VOLATILE() { return getToken(GroovyParser.VOLATILE, 0); }
		public TerminalNode WHILE() { return getToken(GroovyParser.WHILE, 0); }
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
		enterRule(_localctx, 296, RULE_keywords);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1680);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << THREADSAFE) | (1L << VAR) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << ASSERT) | (1L << BREAK) | (1L << CASE) | (1L << CATCH) | (1L << CLASS) | (1L << CONST) | (1L << CONTINUE) | (1L << DEFAULT) | (1L << DO) | (1L << ELSE) | (1L << ENUM) | (1L << EXTENDS) | (1L << FINAL) | (1L << FINALLY) | (1L << FOR) | (1L << IF) | (1L << GOTO) | (1L << IMPLEMENTS) | (1L << IMPORT) | (1L << INSTANCEOF) | (1L << INTERFACE) | (1L << NATIVE) | (1L << NEW) | (1L << PACKAGE) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RETURN) | (1L << STATIC) | (1L << STRICTFP) | (1L << SUPER) | (1L << SWITCH) | (1L << SYNCHRONIZED) | (1L << THIS) | (1L << THROW) | (1L << THROWS) | (1L << TRANSIENT) | (1L << TRY) | (1L << VOID) | (1L << VOLATILE) | (1L << WHILE) | (1L << BooleanLiteral) | (1L << NullLiteral))) != 0)) ) {
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

	public static class RparenContext extends GroovyParserRuleContext {
		public TerminalNode RPAREN() { return getToken(GroovyParser.RPAREN, 0); }
		public RparenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rparen; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitRparen(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final RparenContext rparen() throws RecognitionException {
		RparenContext _localctx = new RparenContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_rparen);
		try {
			setState(1684);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,185,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1682);
				match(RPAREN);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				 require(false, "Missing ')'"); 
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
		enterRule(_localctx, 300, RULE_nls);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1689);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1686);
					match(NL);
					}
					} 
				}
				setState(1691);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
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
		enterRule(_localctx, 302, RULE_sep);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1693); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1692);
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
				setState(1695); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,187,_ctx);
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
		case 19:
			return classDeclaration_sempred((ClassDeclarationContext)_localctx, predIndex);

		case 20:
			return classBody_sempred((ClassBodyContext)_localctx, predIndex);

		case 25:
			return methodDeclaration_sempred((MethodDeclarationContext)_localctx, predIndex);

		case 80:
			return localVariableDeclaration_sempred((LocalVariableDeclarationContext)_localctx, predIndex);

		case 94:
			return statement_sempred((StatementContext)_localctx, predIndex);

		case 116:
			return expression_sempred((ExpressionContext)_localctx, predIndex);

		case 118:
			return commandExpression_sempred((CommandExpressionContext)_localctx, predIndex);

		case 146:
			return identifier_sempred((IdentifierContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean classDeclaration_sempred(ClassDeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return  3 != _localctx.t ;

		case 1:
			return  2 != _localctx.t ;

		case 2:
			return 1 == _localctx.t;

		case 3:
			return 1 != _localctx.t;
		}
		return true;
	}
	private boolean classBody_sempred(ClassBodyContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return  2 == _localctx.t ;
		}
		return true;
	}
	private boolean methodDeclaration_sempred(MethodDeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return  3 == _localctx.ct ;
		}
		return true;
	}
	private boolean localVariableDeclaration_sempred(LocalVariableDeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 6:
			return  !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ;
		}
		return true;
	}
	private boolean statement_sempred(StatementContext _localctx, int predIndex) {
		switch (predIndex) {
		case 7:
			return  !SemanticPredicates.isInvalidMethodDeclaration(_input) ;
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 8:
			return precpred(_ctx, 17);

		case 9:
			return precpred(_ctx, 15);

		case 10:
			return precpred(_ctx, 14);

		case 11:
			return precpred(_ctx, 13);

		case 12:
			return precpred(_ctx, 11);

		case 13:
			return precpred(_ctx, 10);

		case 14:
			return precpred(_ctx, 9);

		case 15:
			return precpred(_ctx, 8);

		case 16:
			return precpred(_ctx, 7);

		case 17:
			return precpred(_ctx, 6);

		case 18:
			return precpred(_ctx, 5);

		case 19:
			return precpred(_ctx, 4);

		case 20:
			return precpred(_ctx, 3);

		case 21:
			return precpred(_ctx, 12);

		case 22:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean commandExpression_sempred(CommandExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 23:
			return  !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) ;
		}
		return true;
	}
	private boolean identifier_sempred(IdentifierContext _localctx, int predIndex) {
		switch (predIndex) {
		case 24:
			return  DOT == _input.LT(2).getType() ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\3\u0083\u06a4\4\2\t"+
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
		"\t\u0097\4\u0098\t\u0098\4\u0099\t\u0099\3\2\3\2\5\2\u0135\n\2\3\2\5\2"+
		"\u0138\n\2\3\2\5\2\u013b\n\2\3\2\3\2\3\3\3\3\3\3\3\3\7\3\u0143\n\3\f\3"+
		"\16\3\u0146\13\3\3\3\5\3\u0149\n\3\3\4\3\4\3\4\5\4\u014e\n\4\3\5\3\5\3"+
		"\5\3\5\3\6\3\6\3\6\5\6\u0157\n\6\3\6\3\6\3\6\3\6\3\6\5\6\u015e\n\6\3\7"+
		"\3\7\3\7\3\b\3\b\5\b\u0165\n\b\3\t\3\t\3\t\5\t\u016a\n\t\3\n\3\n\3\n\3"+
		"\n\7\n\u0170\n\n\f\n\16\n\u0173\13\n\3\13\3\13\7\13\u0177\n\13\f\13\16"+
		"\13\u017a\13\13\5\13\u017c\n\13\3\f\3\f\3\f\3\f\7\f\u0182\n\f\f\f\16\f"+
		"\u0185\13\f\3\r\3\r\5\r\u0189\n\r\3\16\3\16\5\16\u018d\n\16\3\17\3\17"+
		"\3\17\5\17\u0192\n\17\3\20\3\20\3\20\3\20\7\20\u0198\n\20\f\20\16\20\u019b"+
		"\13\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\7\21\u01a4\n\21\f\21\16\21\u01a7"+
		"\13\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\5\22\u01b1\n\22\3\23\3"+
		"\23\3\23\3\23\3\23\7\23\u01b8\n\23\f\23\16\23\u01bb\13\23\3\24\3\24\3"+
		"\24\3\24\3\24\7\24\u01c2\n\24\f\24\16\24\u01c5\13\24\3\25\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u01d2\n\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\5\25\u01da\n\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u01e2"+
		"\n\25\3\25\3\25\3\25\5\25\u01e7\n\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\5\25\u01f0\n\25\3\25\5\25\u01f3\n\25\3\25\3\25\3\26\3\26\3\26\3\26\3"+
		"\26\3\26\3\26\5\26\u01fe\n\26\3\26\5\26\u0201\n\26\3\26\5\26\u0204\n\26"+
		"\3\26\3\26\3\26\3\26\7\26\u020a\n\26\f\26\16\26\u020d\13\26\5\26\u020f"+
		"\n\26\3\26\5\26\u0212\n\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\7\27"+
		"\u021c\n\27\f\27\16\27\u021f\13\27\3\30\3\30\3\30\5\30\u0224\n\30\3\30"+
		"\5\30\u0227\n\30\3\31\3\31\5\31\u022b\n\31\3\31\3\31\5\31\u022f\n\31\3"+
		"\32\3\32\3\32\3\32\3\32\5\32\u0236\n\32\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\5\33\u0241\n\33\3\33\3\33\5\33\u0245\n\33\3\33\5\33\u0248"+
		"\n\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u0251\n\33\3\33\3\33\3\33"+
		"\5\33\u0256\n\33\5\33\u0258\n\33\3\34\3\34\5\34\u025c\n\34\3\35\3\35\5"+
		"\35\u0260\n\35\3\36\3\36\3\37\3\37\3\37\3\37\3\37\7\37\u0269\n\37\f\37"+
		"\16\37\u026c\13\37\3 \3 \3 \3 \3 \3 \5 \u0274\n \3!\3!\3\"\3\"\3#\3#\3"+
		"#\3#\3#\3#\7#\u0280\n#\f#\16#\u0283\13#\3#\3#\5#\u0287\n#\3$\3$\3$\3$"+
		"\6$\u028d\n$\r$\16$\u028e\3%\5%\u0292\n%\3&\3&\3&\5&\u0297\n&\3&\3&\3"+
		"\'\3\'\3\'\5\'\u029e\n\'\3\'\5\'\u02a1\n\'\3\'\3\'\3(\3(\5(\u02a7\n(\3"+
		"(\5(\u02aa\n(\3)\3)\5)\u02ae\n)\3*\3*\5*\u02b2\n*\3+\3+\3,\3,\3,\3,\3"+
		",\3,\3,\7,\u02bd\n,\f,\16,\u02c0\13,\3,\3,\3,\3-\3-\3-\3-\3-\3-\3-\5-"+
		"\u02cc\n-\5-\u02ce\n-\3.\3.\3.\3/\3/\3/\3/\3/\7/\u02d8\n/\f/\16/\u02db"+
		"\13/\3\60\3\60\5\60\u02df\n\60\3\60\3\60\3\61\3\61\5\61\u02e5\n\61\3\61"+
		"\3\61\3\61\3\61\7\61\u02eb\n\61\f\61\16\61\u02ee\13\61\3\62\3\62\3\62"+
		"\3\63\3\63\5\63\u02f5\n\63\3\63\5\63\u02f8\n\63\3\63\3\63\3\63\3\63\3"+
		"\63\3\63\5\63\u0300\n\63\3\64\3\64\3\65\3\65\3\65\7\65\u0307\n\65\f\65"+
		"\16\65\u030a\13\65\3\66\3\66\3\66\3\66\3\66\5\66\u0311\n\66\3\67\3\67"+
		"\3\67\7\67\u0316\n\67\f\67\16\67\u0319\13\67\38\38\38\39\39\39\39\79\u0322"+
		"\n9\f9\169\u0325\139\3:\3:\3:\3:\3:\5:\u032c\n:\3;\3;\3;\3;\7;\u0332\n"+
		";\f;\16;\u0335\13;\3;\3;\3<\3<\3<\5<\u033c\n<\3<\3<\5<\u0340\n<\3=\3="+
		"\7=\u0344\n=\f=\16=\u0347\13=\3>\3>\3>\3>\3>\3>\3?\3?\3?\3?\3?\3?\3@\3"+
		"@\3A\3A\5A\u0359\nA\3B\3B\5B\u035d\nB\3C\3C\3C\5C\u0362\nC\3C\3C\3C\3"+
		"C\5C\u0368\nC\3C\3C\3C\3D\3D\5D\u036f\nD\3E\5E\u0372\nE\3F\3F\3F\3F\7"+
		"F\u0378\nF\fF\16F\u037b\13F\3F\5F\u037e\nF\3G\3G\3G\3G\7G\u0384\nG\fG"+
		"\16G\u0387\13G\3G\3G\5G\u038b\nG\3H\3H\3H\3H\5H\u0391\nH\3H\5H\u0394\n"+
		"H\3I\3I\5I\u0398\nI\3J\3J\3K\3K\3K\7K\u039f\nK\fK\16K\u03a2\13K\3L\3L"+
		"\3L\3L\3L\3L\3M\3M\5M\u03ac\nM\3N\3N\3N\5N\u03b1\nN\3O\3O\3O\3O\7O\u03b7"+
		"\nO\fO\16O\u03ba\13O\3O\5O\u03bd\nO\5O\u03bf\nO\3O\3O\3P\3P\5P\u03c5\n"+
		"P\3P\3P\3P\3Q\3Q\5Q\u03cc\nQ\3R\3R\3R\3S\3S\3S\3T\3T\5T\u03d6\nT\3T\3"+
		"T\3T\3T\3T\3T\3T\5T\u03df\nT\3T\3T\3T\5T\u03e4\nT\3U\3U\3U\3U\7U\u03ea"+
		"\nU\fU\16U\u03ed\13U\3U\3U\3V\5V\u03f2\nV\3V\3V\3W\3W\3W\3W\6W\u03fa\n"+
		"W\rW\16W\u03fb\3W\3W\3X\3X\5X\u0402\nX\3Y\3Y\3Y\3Y\3Y\3Y\5Y\u040a\nY\3"+
		"Y\3Y\3Y\3Y\5Y\u0410\nY\3Z\3Z\3Z\3Z\3Z\3Z\6Z\u0418\nZ\rZ\16Z\u0419\3Z\3"+
		"Z\5Z\u041e\nZ\3Z\3Z\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3"+
		"[\3[\3[\5[\u0435\n[\3\\\3\\\5\\\u0439\n\\\3]\3]\5]\u043d\n]\3^\3^\5^\u0441"+
		"\n^\3^\3^\3^\3^\3^\7^\u0448\n^\f^\16^\u044b\13^\3^\3^\3^\5^\u0450\n^\3"+
		"_\3_\3_\3_\3_\3_\3_\5_\u0459\n_\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\5`\u0466"+
		"\n`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\5`\u0477\n`\3a\3a\3a"+
		"\3a\5a\u047d\na\3a\3a\3a\3a\3a\3b\3b\3b\7b\u0487\nb\fb\16b\u048a\13b\3"+
		"c\3c\3c\3c\3d\3d\3d\3d\5d\u0494\nd\3d\3d\3e\3e\3e\3e\7e\u049c\ne\fe\16"+
		"e\u049f\13e\3f\3f\5f\u04a3\nf\3g\3g\3g\3g\7g\u04a9\ng\fg\16g\u04ac\13"+
		"g\3g\3g\3g\3h\3h\3h\3h\3h\3h\5h\u04b7\nh\3i\3i\5i\u04bb\ni\3j\3j\5j\u04bf"+
		"\nj\3j\3j\3j\3j\3k\5k\u04c6\nk\3k\3k\5k\u04ca\nk\3k\3k\5k\u04ce\nk\3l"+
		"\3l\5l\u04d2\nl\3m\3m\3n\3n\3n\3n\3o\3o\3p\3p\3p\3p\3q\3q\3q\7q\u04e3"+
		"\nq\fq\16q\u04e6\13q\3r\5r\u04e9\nr\3r\3r\3s\3s\5s\u04ef\ns\3t\3t\3u\3"+
		"u\5u\u04f5\nu\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\5v\u0508"+
		"\nv\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v"+
		"\3v\3v\3v\5v\u0523\nv\3v\5v\u0526\nv\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v"+
		"\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v"+
		"\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v"+
		"\3v\3v\3v\3v\3v\5v\u0566\nv\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v"+
		"\7v\u0576\nv\fv\16v\u0579\13v\3w\3w\3w\3w\3w\3w\3w\3w\3w\3w\5w\u0585\n"+
		"w\3x\3x\3x\3x\5x\u058b\nx\3x\7x\u058e\nx\fx\16x\u0591\13x\3y\3y\6y\u0595"+
		"\ny\ry\16y\u0596\3y\5y\u059a\ny\3z\3z\3z\3z\7z\u05a0\nz\fz\16z\u05a3\13"+
		"z\3{\3{\3{\3{\3{\5{\u05aa\n{\3{\3{\3{\3{\5{\u05b0\n{\3{\3{\3{\3{\3{\3"+
		"{\3{\3{\3{\3{\3{\3{\5{\u05be\n{\3{\3{\3{\3{\3{\3{\3{\3{\3{\5{\u05c9\n"+
		"{\3|\3|\3|\3|\5|\u05cf\n|\3}\3}\5}\u05d3\n}\3~\5~\u05d6\n~\3~\3~\5~\u05da"+
		"\n~\3~\3~\3\177\5\177\u05df\n\177\3\177\3\177\3\177\5\177\u05e4\n\177"+
		"\3\177\3\177\3\u0080\3\u0080\5\u0080\u05ea\n\u0080\3\u0080\3\u0080\3\u0080"+
		"\3\u0080\3\u0080\3\u0080\3\u0080\3\u0080\3\u0080\3\u0080\3\u0080\3\u0080"+
		"\3\u0080\5\u0080\u05f9\n\u0080\3\u0081\3\u0081\5\u0081\u05fd\n\u0081\3"+
		"\u0081\5\u0081\u0600\n\u0081\3\u0081\3\u0081\3\u0082\3\u0082\3\u0082\5"+
		"\u0082\u0607\n\u0082\3\u0082\5\u0082\u060a\n\u0082\3\u0082\3\u0082\3\u0083"+
		"\3\u0083\3\u0083\7\u0083\u0611\n\u0083\f\u0083\16\u0083\u0614\13\u0083"+
		"\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084"+
		"\3\u0084\5\u0084\u0620\n\u0084\3\u0085\3\u0085\5\u0085\u0624\n\u0085\3"+
		"\u0086\3\u0086\3\u0086\3\u0086\5\u0086\u062a\n\u0086\3\u0086\6\u0086\u062d"+
		"\n\u0086\r\u0086\16\u0086\u062e\3\u0086\3\u0086\3\u0086\5\u0086\u0634"+
		"\n\u0086\5\u0086\u0636\n\u0086\3\u0087\3\u0087\3\u0087\5\u0087\u063b\n"+
		"\u0087\3\u0087\3\u0087\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\5\u0088"+
		"\u0644\n\u0088\3\u0088\3\u0088\3\u0089\3\u0089\3\u008a\3\u008a\3\u008a"+
		"\3\u008a\5\u008a\u064e\n\u008a\5\u008a\u0650\n\u008a\3\u008b\3\u008b\3"+
		"\u008b\3\u008b\3\u008b\3\u008b\3\u008c\3\u008c\3\u008c\5\u008c\u065b\n"+
		"\u008c\3\u008d\3\u008d\5\u008d\u065f\n\u008d\3\u008d\5\u008d\u0662\n\u008d"+
		"\3\u008d\3\u008d\3\u008e\3\u008e\3\u008e\3\u008e\3\u008e\7\u008e\u066b"+
		"\n\u008e\f\u008e\16\u008e\u066e\13\u008e\3\u008f\3\u008f\3\u008f\3\u008f"+
		"\3\u008f\7\u008f\u0675\n\u008f\f\u008f\16\u008f\u0678\13\u008f\3\u0090"+
		"\3\u0090\5\u0090\u067c\n\u0090\3\u0091\3\u0091\3\u0091\5\u0091\u0681\n"+
		"\u0091\3\u0092\3\u0092\3\u0093\3\u0093\3\u0094\3\u0094\3\u0094\3\u0094"+
		"\3\u0094\3\u0094\3\u0094\3\u0094\5\u0094\u068f\n\u0094\3\u0095\3\u0095"+
		"\3\u0096\3\u0096\3\u0097\3\u0097\5\u0097\u0697\n\u0097\3\u0098\7\u0098"+
		"\u069a\n\u0098\f\u0098\16\u0098\u069d\13\u0098\3\u0099\6\u0099\u06a0\n"+
		"\u0099\r\u0099\16\u0099\u06a1\3\u0099\2\2\3\u00ea\u009a\2\2\4\2\6\2\b"+
		"\2\n\2\f\2\16\2\20\2\22\2\24\2\26\2\30\2\32\2\34\2\36\2 \2\"\2$\2&\2("+
		"\2*\2,\2.\2\60\2\62\2\64\2\66\28\2:\2<\2>\2@\2B\2D\2F\2H\2J\2L\2N\2P\2"+
		"R\2T\2V\2X\2Z\2\\\2^\2`\2b\2d\2f\2h\2j\2l\2n\2p\2r\2t\2v\2x\2z\2|\2~\2"+
		"\u0080\2\u0082\2\u0084\2\u0086\2\u0088\2\u008a\2\u008c\2\u008e\2\u0090"+
		"\2\u0092\2\u0094\2\u0096\2\u0098\2\u009a\2\u009c\2\u009e\2\u00a0\2\u00a2"+
		"\2\u00a4\2\u00a6\2\u00a8\2\u00aa\2\u00ac\2\u00ae\2\u00b0\2\u00b2\2\u00b4"+
		"\2\u00b6\2\u00b8\2\u00ba\2\u00bc\2\u00be\2\u00c0\2\u00c2\2\u00c4\2\u00c6"+
		"\2\u00c8\2\u00ca\2\u00cc\2\u00ce\2\u00d0\2\u00d2\2\u00d4\2\u00d6\2\u00d8"+
		"\2\u00da\2\u00dc\2\u00de\2\u00e0\2\u00e2\2\u00e4\2\u00e6\2\u00e8\2\u00ea"+
		"\2\u00ec\2\u00ee\2\u00f0\2\u00f2\2\u00f4\2\u00f6\2\u00f8\2\u00fa\2\u00fc"+
		"\2\u00fe\2\u0100\2\u0102\2\u0104\2\u0106\2\u0108\2\u010a\2\u010c\2\u010e"+
		"\2\u0110\2\u0112\2\u0114\2\u0116\2\u0118\2\u011a\2\u011c\2\u011e\2\u0120"+
		"\2\u0122\2\u0124\2\u0126\2\u0128\2\u012a\2\u012c\2\u012e\2\u0130\2\2\27"+
		"\b\2\n\n\16\16&&\61\61\65\6588\7\2\20\20\30\30\35\35)+-.\b\2\n\n\16\16"+
		"\20\20\35\35)+-.\4\2\34\34//\4\2WW__\4\2\13\13__\3\2fg\3\2\\]\3\2fi\4"+
		"\2jkoo\3\2hi\3\2>?\6\2\13\13OOZ[ab\5\2JL``cc\3\2FG\5\2\t\t$$NN\5\2IIY"+
		"Yp{\4\2@BXX\4\2\17\17\67\67\4\2\t9<=\4\2VV\u0081\u0081\2\u070d\2\u0132"+
		"\3\2\2\2\4\u013e\3\2\2\2\6\u014d\3\2\2\2\b\u014f\3\2\2\2\n\u0153\3\2\2"+
		"\2\f\u015f\3\2\2\2\16\u0164\3\2\2\2\20\u0169\3\2\2\2\22\u016b\3\2\2\2"+
		"\24\u017b\3\2\2\2\26\u017d\3\2\2\2\30\u0188\3\2\2\2\32\u018c\3\2\2\2\34"+
		"\u0191\3\2\2\2\36\u0193\3\2\2\2 \u019c\3\2\2\2\"\u01ab\3\2\2\2$\u01b2"+
		"\3\2\2\2&\u01bc\3\2\2\2(\u01d1\3\2\2\2*\u01f6\3\2\2\2,\u0215\3\2\2\2."+
		"\u0220\3\2\2\2\60\u022e\3\2\2\2\62\u0235\3\2\2\2\64\u0257\3\2\2\2\66\u025b"+
		"\3\2\2\28\u025f\3\2\2\2:\u0261\3\2\2\2<\u0263\3\2\2\2>\u026d\3\2\2\2@"+
		"\u0275\3\2\2\2B\u0277\3\2\2\2D\u0279\3\2\2\2F\u028c\3\2\2\2H\u0291\3\2"+
		"\2\2J\u0293\3\2\2\2L\u029a\3\2\2\2N\u02a6\3\2\2\2P\u02ab\3\2\2\2R\u02af"+
		"\3\2\2\2T\u02b3\3\2\2\2V\u02b5\3\2\2\2X\u02cd\3\2\2\2Z\u02cf\3\2\2\2\\"+
		"\u02d2\3\2\2\2^\u02dc\3\2\2\2`\u02e4\3\2\2\2b\u02ef\3\2\2\2d\u02f2\3\2"+
		"\2\2f\u0301\3\2\2\2h\u0303\3\2\2\2j\u0310\3\2\2\2l\u0317\3\2\2\2n\u031a"+
		"\3\2\2\2p\u031d\3\2\2\2r\u032b\3\2\2\2t\u032d\3\2\2\2v\u033f\3\2\2\2x"+
		"\u0341\3\2\2\2z\u0348\3\2\2\2|\u034e\3\2\2\2~\u0354\3\2\2\2\u0080\u0358"+
		"\3\2\2\2\u0082\u035c\3\2\2\2\u0084\u035e\3\2\2\2\u0086\u036e\3\2\2\2\u0088"+
		"\u0371\3\2\2\2\u008a\u0373\3\2\2\2\u008c\u038a\3\2\2\2\u008e\u038c\3\2"+
		"\2\2\u0090\u0397\3\2\2\2\u0092\u0399\3\2\2\2\u0094\u039b\3\2\2\2\u0096"+
		"\u03a3\3\2\2\2\u0098\u03ab\3\2\2\2\u009a\u03b0\3\2\2\2\u009c\u03b2\3\2"+
		"\2\2\u009e\u03c2\3\2\2\2\u00a0\u03cb\3\2\2\2\u00a2\u03cd\3\2\2\2\u00a4"+
		"\u03d0\3\2\2\2\u00a6\u03e3\3\2\2\2\u00a8\u03e5\3\2\2\2\u00aa\u03f1\3\2"+
		"\2\2\u00ac\u03f5\3\2\2\2\u00ae\u0401\3\2\2\2\u00b0\u0403\3\2\2\2\u00b2"+
		"\u0411\3\2\2\2\u00b4\u0434\3\2\2\2\u00b6\u0436\3\2\2\2\u00b8\u043a\3\2"+
		"\2\2\u00ba\u043e\3\2\2\2\u00bc\u0451\3\2\2\2\u00be\u0476\3\2\2\2\u00c0"+
		"\u0478\3\2\2\2\u00c2\u0483\3\2\2\2\u00c4\u048b\3\2\2\2\u00c6\u048f\3\2"+
		"\2\2\u00c8\u0497\3\2\2\2\u00ca\u04a2\3\2\2\2\u00cc\u04a4\3\2\2\2\u00ce"+
		"\u04b6\3\2\2\2\u00d0\u04ba\3\2\2\2\u00d2\u04bc\3\2\2\2\u00d4\u04c5\3\2"+
		"\2\2\u00d6\u04d1\3\2\2\2\u00d8\u04d3\3\2\2\2\u00da\u04d5\3\2\2\2\u00dc"+
		"\u04d9\3\2\2\2\u00de\u04db\3\2\2\2\u00e0\u04df\3\2\2\2\u00e2\u04e8\3\2"+
		"\2\2\u00e4\u04ee\3\2\2\2\u00e6\u04f0\3\2\2\2\u00e8\u04f2\3\2\2\2\u00ea"+
		"\u0507\3\2\2\2\u00ec\u0584\3\2\2\2\u00ee\u0586\3\2\2\2\u00f0\u0592\3\2"+
		"\2\2\u00f2\u059b\3\2\2\2\u00f4\u05c8\3\2\2\2\u00f6\u05ce\3\2\2\2\u00f8"+
		"\u05d2\3\2\2\2\u00fa\u05d5\3\2\2\2\u00fc\u05de\3\2\2\2\u00fe\u05f8\3\2"+
		"\2\2\u0100\u05fa\3\2\2\2\u0102\u0603\3\2\2\2\u0104\u060d\3\2\2\2\u0106"+
		"\u061f\3\2\2\2\u0108\u0623\3\2\2\2\u010a\u0625\3\2\2\2\u010c\u0637\3\2"+
		"\2\2\u010e\u063e\3\2\2\2\u0110\u0647\3\2\2\2\u0112\u0649\3\2\2\2\u0114"+
		"\u0651\3\2\2\2\u0116\u065a\3\2\2\2\u0118\u065c\3\2\2\2\u011a\u0665\3\2"+
		"\2\2\u011c\u066f\3\2\2\2\u011e\u067b\3\2\2\2\u0120\u0680\3\2\2\2\u0122"+
		"\u0682\3\2\2\2\u0124\u0684\3\2\2\2\u0126\u068e\3\2\2\2\u0128\u0690\3\2"+
		"\2\2\u012a\u0692\3\2\2\2\u012c\u0696\3\2\2\2\u012e\u069b\3\2\2\2\u0130"+
		"\u069f\3\2\2\2\u0132\u0134\5\u012e\u0098\2\u0133\u0135\5\b\5\2\u0134\u0133"+
		"\3\2\2\2\u0134\u0135\3\2\2\2\u0135\u0137\3\2\2\2\u0136\u0138\5\u0130\u0099"+
		"\2\u0137\u0136\3\2\2\2\u0137\u0138\3\2\2\2\u0138\u013a\3\2\2\2\u0139\u013b"+
		"\5\4\3\2\u013a\u0139\3\2\2\2\u013a\u013b\3\2\2\2\u013b\u013c\3\2\2\2\u013c"+
		"\u013d\7\2\2\3\u013d\3\3\2\2\2\u013e\u0144\5\6\4\2\u013f\u0140\5\u0130"+
		"\u0099\2\u0140\u0141\5\6\4\2\u0141\u0143\3\2\2\2\u0142\u013f\3\2\2\2\u0143"+
		"\u0146\3\2\2\2\u0144\u0142\3\2\2\2\u0144\u0145\3\2\2\2\u0145\u0148\3\2"+
		"\2\2\u0146\u0144\3\2\2\2\u0147\u0149\5\u0130\u0099\2\u0148\u0147\3\2\2"+
		"\2\u0148\u0149\3\2\2\2\u0149\5\3\2\2\2\u014a\u014e\5\n\6\2\u014b\u014e"+
		"\5\f\7\2\u014c\u014e\5\u00be`\2\u014d\u014a\3\2\2\2\u014d\u014b\3\2\2"+
		"\2\u014d\u014c\3\2\2\2\u014e\7\3\2\2\2\u014f\u0150\5\u008cG\2\u0150\u0151"+
		"\7(\2\2\u0151\u0152\5h\65\2\u0152\t\3\2\2\2\u0153\u0154\5\u008cG\2\u0154"+
		"\u0156\7#\2\2\u0155\u0157\7-\2\2\u0156\u0155\3\2\2\2\u0156\u0157\3\2\2"+
		"\2\u0157\u0158\3\2\2\2\u0158\u015d\5h\65\2\u0159\u015a\7X\2\2\u015a\u015e"+
		"\7j\2\2\u015b\u015c\7\t\2\2\u015c\u015e\5\u0126\u0094\2\u015d\u0159\3"+
		"\2\2\2\u015d\u015b\3\2\2\2\u015d\u015e\3\2\2\2\u015e\13\3\2\2\2\u015f"+
		"\u0160\5\24\13\2\u0160\u0161\5(\25\2\u0161\r\3\2\2\2\u0162\u0165\5\30"+
		"\r\2\u0163\u0165\t\2\2\2\u0164\u0162\3\2\2\2\u0164\u0163\3\2\2\2\u0165"+
		"\17\3\2\2\2\u0166\u0167\5\22\n\2\u0167\u0168\5\u012e\u0098\2\u0168\u016a"+
		"\3\2\2\2\u0169\u0166\3\2\2\2\u0169\u016a\3\2\2\2\u016a\21\3\2\2\2\u016b"+
		"\u0171\5\16\b\2\u016c\u016d\5\u012e\u0098\2\u016d\u016e\5\16\b\2\u016e"+
		"\u0170\3\2\2\2\u016f\u016c\3\2\2\2\u0170\u0173\3\2\2\2\u0171\u016f\3\2"+
		"\2\2\u0171\u0172\3\2\2\2\u0172\23\3\2\2\2\u0173\u0171\3\2\2\2\u0174\u0178"+
		"\5\26\f\2\u0175\u0177\7\u0081\2\2\u0176\u0175\3\2\2\2\u0177\u017a\3\2"+
		"\2\2\u0178\u0176\3\2\2\2\u0178\u0179\3\2\2\2\u0179\u017c\3\2\2\2\u017a"+
		"\u0178\3\2\2\2\u017b\u0174\3\2\2\2\u017b\u017c\3\2\2\2\u017c\25\3\2\2"+
		"\2\u017d\u0183\5\30\r\2\u017e\u017f\5\u012e\u0098\2\u017f\u0180\5\30\r"+
		"\2\u0180\u0182\3\2\2\2\u0181\u017e\3\2\2\2\u0182\u0185\3\2\2\2\u0183\u0181"+
		"\3\2\2\2\u0183\u0184\3\2\2\2\u0184\27\3\2\2\2\u0185\u0183\3\2\2\2\u0186"+
		"\u0189\5\u008eH\2\u0187\u0189\t\3\2\2\u0188\u0186\3\2\2\2\u0188\u0187"+
		"\3\2\2\2\u0189\31\3\2\2\2\u018a\u018d\5\u008eH\2\u018b\u018d\t\4\2\2\u018c"+
		"\u018a\3\2\2\2\u018c\u018b\3\2\2\2\u018d\33\3\2\2\2\u018e\u018f\5\36\20"+
		"\2\u018f\u0190\5\u012e\u0098\2\u0190\u0192\3\2\2\2\u0191\u018e\3\2\2\2"+
		"\u0191\u0192\3\2\2\2\u0192\35\3\2\2\2\u0193\u0199\5\32\16\2\u0194\u0195"+
		"\5\u012e\u0098\2\u0195\u0196\5\32\16\2\u0196\u0198\3\2\2\2\u0197\u0194"+
		"\3\2\2\2\u0198\u019b\3\2\2\2\u0199\u0197\3\2\2\2\u0199\u019a\3\2\2\2\u019a"+
		"\37\3\2\2\2\u019b\u0199\3\2\2\2\u019c\u019d\7[\2\2\u019d\u019e\5\u012e"+
		"\u0098\2\u019e\u01a5\5\"\22\2\u019f\u01a0\7W\2\2\u01a0\u01a1\5\u012e\u0098"+
		"\2\u01a1\u01a2\5\"\22\2\u01a2\u01a4\3\2\2\2\u01a3\u019f\3\2\2\2\u01a4"+
		"\u01a7\3\2\2\2\u01a5\u01a3\3\2\2\2\u01a5\u01a6\3\2\2\2\u01a6\u01a8\3\2"+
		"\2\2\u01a7\u01a5\3\2\2\2\u01a8\u01a9\5\u012e\u0098\2\u01a9\u01aa\7Z\2"+
		"\2\u01aa!\3\2\2\2\u01ab\u01b0\5\u0124\u0093\2\u01ac\u01ad\7\34\2\2\u01ad"+
		"\u01ae\5\u012e\u0098\2\u01ae\u01af\5$\23\2\u01af\u01b1\3\2\2\2\u01b0\u01ac"+
		"\3\2\2\2\u01b0\u01b1\3\2\2\2\u01b1#\3\2\2\2\u01b2\u01b9\5L\'\2\u01b3\u01b4"+
		"\7l\2\2\u01b4\u01b5\5\u012e\u0098\2\u01b5\u01b6\5L\'\2\u01b6\u01b8\3\2"+
		"\2\2\u01b7\u01b3\3\2\2\2\u01b8\u01bb\3\2\2\2\u01b9\u01b7\3\2\2\2\u01b9"+
		"\u01ba\3\2\2\2\u01ba%\3\2\2\2\u01bb\u01b9\3\2\2\2\u01bc\u01c3\5L\'\2\u01bd"+
		"\u01be\7W\2\2\u01be\u01bf\5\u012e\u0098\2\u01bf\u01c0\5L\'\2\u01c0\u01c2"+
		"\3\2\2\2\u01c1\u01bd\3\2\2\2\u01c2\u01c5\3\2\2\2\u01c3\u01c1\3\2\2\2\u01c3"+
		"\u01c4\3\2\2\2\u01c4\'\3\2\2\2\u01c5\u01c3\3\2\2\2\u01c6\u01c7\7\25\2"+
		"\2\u01c7\u01d2\b\25\1\2\u01c8\u01c9\7%\2\2\u01c9\u01d2\b\25\1\2\u01ca"+
		"\u01cb\7\33\2\2\u01cb\u01d2\b\25\1\2\u01cc\u01cd\7~\2\2\u01cd\u01ce\7"+
		"%\2\2\u01ce\u01d2\b\25\1\2\u01cf\u01d0\7\f\2\2\u01d0\u01d2\b\25\1\2\u01d1"+
		"\u01c6\3\2\2\2\u01d1\u01c8\3\2\2\2\u01d1\u01ca\3\2\2\2\u01d1\u01cc\3\2"+
		"\2\2\u01d1\u01cf\3\2\2\2\u01d2\u01d3\3\2\2\2\u01d3\u01d4\5\u0126\u0094"+
		"\2\u01d4\u01f2\5\u012e\u0098\2\u01d5\u01d9\6\25\2\3\u01d6\u01d7\5 \21"+
		"\2\u01d7\u01d8\5\u012e\u0098\2\u01d8\u01da\3\2\2\2\u01d9\u01d6\3\2\2\2"+
		"\u01d9\u01da\3\2\2\2\u01da\u01e6\3\2\2\2\u01db\u01dc\6\25\3\3\u01dc\u01dd"+
		"\7\34\2\2\u01dd\u01e1\5\u012e\u0098\2\u01de\u01df\6\25\4\3\u01df\u01e2"+
		"\5&\24\2\u01e0\u01e2\5L\'\2\u01e1\u01de\3\2\2\2\u01e1\u01e0\3\2\2\2\u01e2"+
		"\u01e3\3\2\2\2\u01e3\u01e4\5\u012e\u0098\2\u01e4\u01e7\3\2\2\2\u01e5\u01e7"+
		"\3\2\2\2\u01e6\u01db\3\2\2\2\u01e6\u01e5\3\2\2\2\u01e7\u01ef\3\2\2\2\u01e8"+
		"\u01e9\6\25\5\3\u01e9\u01ea\7\"\2\2\u01ea\u01eb\5\u012e\u0098\2\u01eb"+
		"\u01ec\5&\24\2\u01ec\u01ed\5\u012e\u0098\2\u01ed\u01f0\3\2\2\2\u01ee\u01f0"+
		"\3\2\2\2\u01ef\u01e8\3\2\2\2\u01ef\u01ee\3\2\2\2\u01f0\u01f3\3\2\2\2\u01f1"+
		"\u01f3\3\2\2\2\u01f2\u01d5\3\2\2\2\u01f2\u01f1\3\2\2\2\u01f3\u01f4\3\2"+
		"\2\2\u01f4\u01f5\5*\26\2\u01f5)\3\2\2\2\u01f6\u01f7\7R\2\2\u01f7\u0203"+
		"\5\u012e\u0098\2\u01f8\u01f9\6\26\6\3\u01f9\u01fd\5,\27\2\u01fa\u01fb"+
		"\5\u012e\u0098\2\u01fb\u01fc\7W\2\2\u01fc\u01fe\3\2\2\2\u01fd\u01fa\3"+
		"\2\2\2\u01fd\u01fe\3\2\2\2\u01fe\u0200\3\2\2\2\u01ff\u0201\5\u0130\u0099"+
		"\2\u0200\u01ff\3\2\2\2\u0200\u0201\3\2\2\2\u0201\u0204\3\2\2\2\u0202\u0204"+
		"\3\2\2\2\u0203\u01f8\3\2\2\2\u0203\u0202\3\2\2\2\u0204\u020e\3\2\2\2\u0205"+
		"\u020b\5\60\31\2\u0206\u0207\5\u0130\u0099\2\u0207\u0208\5\60\31\2\u0208"+
		"\u020a\3\2\2\2\u0209\u0206\3\2\2\2\u020a\u020d\3\2\2\2\u020b\u0209\3\2"+
		"\2\2\u020b\u020c\3\2\2\2\u020c\u020f\3\2\2\2\u020d\u020b\3\2\2\2\u020e"+
		"\u0205\3\2\2\2\u020e\u020f\3\2\2\2\u020f\u0211\3\2\2\2\u0210\u0212\5\u0130"+
		"\u0099\2\u0211\u0210\3\2\2\2\u0211\u0212\3\2\2\2\u0212\u0213\3\2\2\2\u0213"+
		"\u0214\7S\2\2\u0214+\3\2\2\2\u0215\u021d\5.\30\2\u0216\u0217\5\u012e\u0098"+
		"\2\u0217\u0218\7W\2\2\u0218\u0219\5\u012e\u0098\2\u0219\u021a\5.\30\2"+
		"\u021a\u021c\3\2\2\2\u021b\u0216\3\2\2\2\u021c\u021f\3\2\2\2\u021d\u021b"+
		"\3\2\2\2\u021d\u021e\3\2\2\2\u021e-\3\2\2\2\u021f\u021d\3\2\2\2\u0220"+
		"\u0221\5\u008cG\2\u0221\u0223\5\u0126\u0094\2\u0222\u0224\5\u0118\u008d"+
		"\2\u0223\u0222\3\2\2\2\u0223\u0224\3\2\2\2\u0224\u0226\3\2\2\2\u0225\u0227"+
		"\5\u0110\u0089\2\u0226\u0225\3\2\2\2\u0226\u0227\3\2\2\2\u0227/\3\2\2"+
		"\2\u0228\u0229\7-\2\2\u0229\u022b\5\u012e\u0098\2\u022a\u0228\3\2\2\2"+
		"\u022a\u022b\3\2\2\2\u022b\u022c\3\2\2\2\u022c\u022f\5\u009eP\2\u022d"+
		"\u022f\5\62\32\2\u022e\u022a\3\2\2\2\u022e\u022d\3\2\2\2\u022f\61\3\2"+
		"\2\2\u0230\u0236\5\64\33\2\u0231\u0236\5:\36\2\u0232\u0233\5\20\t\2\u0233"+
		"\u0234\5(\25\2\u0234\u0236\3\2\2\2\u0235\u0230\3\2\2\2\u0235\u0231\3\2"+
		"\2\2\u0235\u0232\3\2\2\2\u0236\63\3\2\2\2\u0237\u0238\6\33\7\3\u0238\u0239"+
		"\58\35\2\u0239\u023a\5\66\34\2\u023a\u023b\7P\2\2\u023b\u0240\5\u012c"+
		"\u0097\2\u023c\u023d\7\30\2\2\u023d\u023e\5\u012e\u0098\2\u023e\u023f"+
		"\5\u009aN\2\u023f\u0241\3\2\2\2\u0240\u023c\3\2\2\2\u0240\u0241\3\2\2"+
		"\2\u0241\u0258\3\2\2\2\u0242\u0244\5\20\t\2\u0243\u0245\5 \21\2\u0244"+
		"\u0243\3\2\2\2\u0244\u0245\3\2\2\2\u0245\u0247\3\2\2\2\u0246\u0248\58"+
		"\35\2\u0247\u0246\3\2\2\2\u0247\u0248\3\2\2\2\u0248\u0249\3\2\2\2\u0249"+
		"\u024a\5\66\34\2\u024a\u0250\5^\60\2\u024b\u024c\5\u012e\u0098\2\u024c"+
		"\u024d\7\64\2\2\u024d\u024e\5\u012e\u0098\2\u024e\u024f\5\\/\2\u024f\u0251"+
		"\3\2\2\2\u0250\u024b\3\2\2\2\u0250\u0251\3\2\2\2\u0251\u0255\3\2\2\2\u0252"+
		"\u0253\5\u012e\u0098\2\u0253\u0254\5f\64\2\u0254\u0256\3\2\2\2\u0255\u0252"+
		"\3\2\2\2\u0255\u0256\3\2\2\2\u0256\u0258\3\2\2\2\u0257\u0237\3\2\2\2\u0257"+
		"\u0242\3\2\2\2\u0258\65\3\2\2\2\u0259\u025c\5\u0126\u0094\2\u025a\u025c"+
		"\5\u0122\u0092\2\u025b\u0259\3\2\2\2\u025b\u025a\3\2\2\2\u025c\67\3\2"+
		"\2\2\u025d\u0260\5J&\2\u025e\u0260\7\67\2\2\u025f\u025d\3\2\2\2\u025f"+
		"\u025e\3\2\2\2\u02609\3\2\2\2\u0261\u0262\5\u00a6T\2\u0262;\3\2\2\2\u0263"+
		"\u026a\5> \2\u0264\u0265\7W\2\2\u0265\u0266\5\u012e\u0098\2\u0266\u0267"+
		"\5> \2\u0267\u0269\3\2\2\2\u0268\u0264\3\2\2\2\u0269\u026c\3\2\2\2\u026a"+
		"\u0268\3\2\2\2\u026a\u026b\3\2\2\2\u026b=\3\2\2\2\u026c\u026a\3\2\2\2"+
		"\u026d\u0273\5@!\2\u026e\u026f\5\u012e\u0098\2\u026f\u0270\7Y\2\2\u0270"+
		"\u0271\5\u012e\u0098\2\u0271\u0272\5B\"\2\u0272\u0274\3\2\2\2\u0273\u026e"+
		"\3\2\2\2\u0273\u0274\3\2\2\2\u0274?\3\2\2\2\u0275\u0276\5\u0126\u0094"+
		"\2\u0276A\3\2\2\2\u0277\u0278\5\u00e4s\2\u0278C\3\2\2\2\u0279\u0281\5"+
		"B\"\2\u027a\u027b\5\u012e\u0098\2\u027b\u027c\7W\2\2\u027c\u027d\5\u012e"+
		"\u0098\2\u027d\u027e\5B\"\2\u027e\u0280\3\2\2\2\u027f\u027a\3\2\2\2\u0280"+
		"\u0283\3\2\2\2\u0281\u027f\3\2\2\2\u0281\u0282\3\2\2\2\u0282\u0284\3\2"+
		"\2\2\u0283\u0281\3\2\2\2\u0284\u0286\5\u012e\u0098\2\u0285\u0287\7W\2"+
		"\2\u0286\u0285\3\2\2\2\u0286\u0287\3\2\2\2\u0287E\3\2\2\2\u0288\u0289"+
		"\5\u008cG\2\u0289\u028a\7T\2\2\u028a\u028b\7U\2\2\u028b\u028d\3\2\2\2"+
		"\u028c\u0288\3\2\2\2\u028d\u028e\3\2\2\2\u028e\u028c\3\2\2\2\u028e\u028f"+
		"\3\2\2\2\u028fG\3\2\2\2\u0290\u0292\5F$\2\u0291\u0290\3\2\2\2\u0291\u0292"+
		"\3\2\2\2\u0292I\3\2\2\2\u0293\u0296\5\u008cG\2\u0294\u0297\5T+\2\u0295"+
		"\u0297\5R*\2\u0296\u0294\3\2\2\2\u0296\u0295\3\2\2\2\u0297\u0298\3\2\2"+
		"\2\u0298\u0299\5H%\2\u0299K\3\2\2\2\u029a\u02a0\5\u008cG\2\u029b\u029e"+
		"\5T+\2\u029c\u029e\7\67\2\2\u029d\u029b\3\2\2\2\u029d\u029c\3\2\2\2\u029e"+
		"\u02a1\3\2\2\2\u029f\u02a1\5P)\2\u02a0\u029d\3\2\2\2\u02a0\u029f\3\2\2"+
		"\2\u02a1\u02a2\3\2\2\2\u02a2\u02a3\5H%\2\u02a3M\3\2\2\2\u02a4\u02a7\5"+
		"n8\2\u02a5\u02a7\5p9\2\u02a6\u02a4\3\2\2\2\u02a6\u02a5\3\2\2\2\u02a7\u02a9"+
		"\3\2\2\2\u02a8\u02aa\5V,\2\u02a9\u02a8\3\2\2\2\u02a9\u02aa\3\2\2\2\u02aa"+
		"O\3\2\2\2\u02ab\u02ad\5n8\2\u02ac\u02ae\5V,\2\u02ad\u02ac\3\2\2\2\u02ad"+
		"\u02ae\3\2\2\2\u02aeQ\3\2\2\2\u02af\u02b1\5p9\2\u02b0\u02b2\5V,\2\u02b1"+
		"\u02b0\3\2\2\2\u02b1\u02b2\3\2\2\2\u02b2S\3\2\2\2\u02b3\u02b4\7\17\2\2"+
		"\u02b4U\3\2\2\2\u02b5\u02b6\7[\2\2\u02b6\u02b7\5\u012e\u0098\2\u02b7\u02be"+
		"\5X-\2\u02b8\u02b9\7W\2\2\u02b9\u02ba\5\u012e\u0098\2\u02ba\u02bb\5X-"+
		"\2\u02bb\u02bd\3\2\2\2\u02bc\u02b8\3\2\2\2\u02bd\u02c0\3\2\2\2\u02be\u02bc"+
		"\3\2\2\2\u02be\u02bf\3\2\2\2\u02bf\u02c1\3\2\2\2\u02c0\u02be\3\2\2\2\u02c1"+
		"\u02c2\5\u012e\u0098\2\u02c2\u02c3\7Z\2\2\u02c3W\3\2\2\2\u02c4\u02ce\5"+
		"L\'\2\u02c5\u02c6\5\u008cG\2\u02c6\u02cb\7^\2\2\u02c7\u02c8\t\5\2\2\u02c8"+
		"\u02c9\5\u012e\u0098\2\u02c9\u02ca\5L\'\2\u02ca\u02cc\3\2\2\2\u02cb\u02c7"+
		"\3\2\2\2\u02cb\u02cc\3\2\2\2\u02cc\u02ce\3\2\2\2\u02cd\u02c4\3\2\2\2\u02cd"+
		"\u02c5\3\2\2\2\u02ceY\3\2\2\2\u02cf\u02d0\5\u008cG\2\u02d0\u02d1\5n8\2"+
		"\u02d1[\3\2\2\2\u02d2\u02d9\5Z.\2\u02d3\u02d4\7W\2\2\u02d4\u02d5\5\u012e"+
		"\u0098\2\u02d5\u02d6\5Z.\2\u02d6\u02d8\3\2\2\2\u02d7\u02d3\3\2\2\2\u02d8"+
		"\u02db\3\2\2\2\u02d9\u02d7\3\2\2\2\u02d9\u02da\3\2\2\2\u02da]\3\2\2\2"+
		"\u02db\u02d9\3\2\2\2\u02dc\u02de\7P\2\2\u02dd\u02df\5`\61\2\u02de\u02dd"+
		"\3\2\2\2\u02de\u02df\3\2\2\2\u02df\u02e0\3\2\2\2\u02e0\u02e1\5\u012c\u0097"+
		"\2\u02e1_\3\2\2\2\u02e2\u02e5\5d\63\2\u02e3\u02e5\5b\62\2\u02e4\u02e2"+
		"\3\2\2\2\u02e4\u02e3\3\2\2\2\u02e5\u02ec\3\2\2\2\u02e6\u02e7\7W\2\2\u02e7"+
		"\u02e8\5\u012e\u0098\2\u02e8\u02e9\5d\63\2\u02e9\u02eb\3\2\2\2\u02ea\u02e6"+
		"\3\2\2\2\u02eb\u02ee\3\2\2\2\u02ec\u02ea\3\2\2\2\u02ec\u02ed\3\2\2\2\u02ed"+
		"a\3\2\2\2\u02ee\u02ec\3\2\2\2\u02ef\u02f0\5L\'\2\u02f0\u02f1\7\62\2\2"+
		"\u02f1c\3\2\2\2\u02f2\u02f4\5\34\17\2\u02f3\u02f5\5L\'\2\u02f4\u02f3\3"+
		"\2\2\2\u02f4\u02f5\3\2\2\2\u02f5\u02f7\3\2\2\2\u02f6\u02f8\7\177\2\2\u02f7"+
		"\u02f6\3\2\2\2\u02f7\u02f8\3\2\2\2\u02f8\u02f9\3\2\2\2\u02f9\u02ff\5@"+
		"!\2\u02fa\u02fb\5\u012e\u0098\2\u02fb\u02fc\7Y\2\2\u02fc\u02fd\5\u012e"+
		"\u0098\2\u02fd\u02fe\5\u00eav\2\u02fe\u0300\3\2\2\2\u02ff\u02fa\3\2\2"+
		"\2\u02ff\u0300\3\2\2\2\u0300e\3\2\2\2\u0301\u0302\5\u009eP\2\u0302g\3"+
		"\2\2\2\u0303\u0308\5j\66\2\u0304\u0305\7X\2\2\u0305\u0307\5j\66\2\u0306"+
		"\u0304\3\2\2\2\u0307\u030a\3\2\2\2\u0308\u0306\3\2\2\2\u0308\u0309\3\2"+
		"\2\2\u0309i\3\2\2\2\u030a\u0308\3\2\2\2\u030b\u0311\5\u0126\u0094\2\u030c"+
		"\u0311\7\n\2\2\u030d\u0311\7\13\2\2\u030e\u0311\7\t\2\2\u030f\u0311\7"+
		"\f\2\2\u0310\u030b\3\2\2\2\u0310\u030c\3\2\2\2\u0310\u030d\3\2\2\2\u0310"+
		"\u030e\3\2\2\2\u0310\u030f\3\2\2\2\u0311k\3\2\2\2\u0312\u0313\5j\66\2"+
		"\u0313\u0314\7X\2\2\u0314\u0316\3\2\2\2\u0315\u0312\3\2\2\2\u0316\u0319"+
		"\3\2\2\2\u0317\u0315\3\2\2\2\u0317\u0318\3\2\2\2\u0318m\3\2\2\2\u0319"+
		"\u0317\3\2\2\2\u031a\u031b\5l\67\2\u031b\u031c\5\u0126\u0094\2\u031co"+
		"\3\2\2\2\u031d\u031e\5l\67\2\u031e\u0323\5\u0124\u0093\2\u031f\u0320\7"+
		"X\2\2\u0320\u0322\5\u0124\u0093\2\u0321\u031f\3\2\2\2\u0322\u0325\3\2"+
		"\2\2\u0323\u0321\3\2\2\2\u0323\u0324\3\2\2\2\u0324q\3\2\2\2\u0325\u0323"+
		"\3\2\2\2\u0326\u032c\7:\2\2\u0327\u032c\7;\2\2\u0328\u032c\5\u0122\u0092"+
		"\2\u0329\u032c\7<\2\2\u032a\u032c\7=\2\2\u032b\u0326\3\2\2\2\u032b\u0327"+
		"\3\2\2\2\u032b\u0328\3\2\2\2\u032b\u0329\3\2\2\2\u032b\u032a\3\2\2\2\u032c"+
		"s\3\2\2\2\u032d\u032e\7\4\2\2\u032e\u0333\5v<\2\u032f\u0330\7\6\2\2\u0330"+
		"\u0332\5v<\2\u0331\u032f\3\2\2\2\u0332\u0335\3\2\2\2\u0333\u0331\3\2\2"+
		"\2\u0333\u0334\3\2\2\2\u0334\u0336\3\2\2\2\u0335\u0333\3\2\2\2\u0336\u0337"+
		"\7\5\2\2\u0337u\3\2\2\2\u0338\u0340\5x=\2\u0339\u033b\7R\2\2\u033a\u033c"+
		"\5\u00e6t\2\u033b\u033a\3\2\2\2\u033b\u033c\3\2\2\2\u033c\u033d\3\2\2"+
		"\2\u033d\u0340\7S\2\2\u033e\u0340\5\u0084C\2\u033f\u0338\3\2\2\2\u033f"+
		"\u0339\3\2\2\2\u033f\u033e\3\2\2\2\u0340w\3\2\2\2\u0341\u0345\5\u0126"+
		"\u0094\2\u0342\u0344\7\7\2\2\u0343\u0342\3\2\2\2\u0344\u0347\3\2\2\2\u0345"+
		"\u0343\3\2\2\2\u0345\u0346\3\2\2\2\u0346y\3\2\2\2\u0347\u0345\3\2\2\2"+
		"\u0348\u0349\5~@\2\u0349\u034a\5\u012e\u0098\2\u034a\u034b\7M\2\2\u034b"+
		"\u034c\5\u012e\u0098\2\u034c\u034d\5\u0082B\2\u034d{\3\2\2\2\u034e\u034f"+
		"\5\u0080A\2\u034f\u0350\5\u012e\u0098\2\u0350\u0351\7M\2\2\u0351\u0352"+
		"\5\u012e\u0098\2\u0352\u0353\5\u0082B\2\u0353}\3\2\2\2\u0354\u0355\5^"+
		"\60\2\u0355\177\3\2\2\2\u0356\u0359\5^\60\2\u0357\u0359\5@!\2\u0358\u0356"+
		"\3\2\2\2\u0358\u0357\3\2\2\2\u0359\u0081\3\2\2\2\u035a\u035d\5\u009eP"+
		"\2\u035b\u035d\5\u00e6t\2\u035c\u035a\3\2\2\2\u035c\u035b\3\2\2\2\u035d"+
		"\u0083\3\2\2\2\u035e\u035f\7R\2\2\u035f\u0367\5\u012e\u0098\2\u0360\u0362"+
		"\5`\61\2\u0361\u0360\3\2\2\2\u0361\u0362\3\2\2\2\u0362\u0363\3\2\2\2\u0363"+
		"\u0364\5\u012e\u0098\2\u0364\u0365\7M\2\2\u0365\u0366\5\u012e\u0098\2"+
		"\u0366\u0368\3\2\2\2\u0367\u0361\3\2\2\2\u0367\u0368\3\2\2\2\u0368\u0369"+
		"\3\2\2\2\u0369\u036a\5\u0088E\2\u036a\u036b\7S\2\2\u036b\u0085\3\2\2\2"+
		"\u036c\u036f\5\u0084C\2\u036d\u036f\5z>\2\u036e\u036c\3\2\2\2\u036e\u036d"+
		"\3\2\2\2\u036f\u0087\3\2\2\2\u0370\u0372\5\u008aF\2\u0371\u0370\3\2\2"+
		"\2\u0371\u0372\3\2\2\2\u0372\u0089\3\2\2\2\u0373\u0379\5\u00a0Q\2\u0374"+
		"\u0375\5\u0130\u0099\2\u0375\u0376\5\u00a0Q\2\u0376\u0378\3\2\2\2\u0377"+
		"\u0374\3\2\2\2\u0378\u037b\3\2\2\2\u0379\u0377\3\2\2\2\u0379\u037a\3\2"+
		"\2\2\u037a\u037d\3\2\2\2\u037b\u0379\3\2\2\2\u037c\u037e\5\u0130\u0099"+
		"\2\u037d\u037c\3\2\2\2\u037d\u037e\3\2\2\2\u037e\u008b\3\2\2\2\u037f\u0385"+
		"\5\u008eH\2\u0380\u0381\5\u012e\u0098\2\u0381\u0382\5\u008eH\2\u0382\u0384"+
		"\3\2\2\2\u0383\u0380\3\2\2\2\u0384\u0387\3\2\2\2\u0385\u0383\3\2\2\2\u0385"+
		"\u0386\3\2\2\2\u0386\u0388\3\2\2\2\u0387\u0385\3\2\2\2\u0388\u0389\5\u012e"+
		"\u0098\2\u0389\u038b\3\2\2\2\u038a\u037f\3\2\2\2\u038a\u038b\3\2\2\2\u038b"+
		"\u008d\3\2\2\2\u038c\u038d\7~\2\2\u038d\u0393\5\u0092J\2\u038e\u0390\7"+
		"P\2\2\u038f\u0391\5\u0090I\2\u0390\u038f\3\2\2\2\u0390\u0391\3\2\2\2\u0391"+
		"\u0392\3\2\2\2\u0392\u0394\5\u012c\u0097\2\u0393\u038e\3\2\2\2\u0393\u0394"+
		"\3\2\2\2\u0394\u008f\3\2\2\2\u0395\u0398\5\u0094K\2\u0396\u0398\5\u009a"+
		"N\2\u0397\u0395\3\2\2\2\u0397\u0396\3\2\2\2\u0398\u0091\3\2\2\2\u0399"+
		"\u039a\5n8\2\u039a\u0093\3\2\2\2\u039b\u03a0\5\u0096L\2\u039c\u039d\7"+
		"W\2\2\u039d\u039f\5\u0096L\2\u039e\u039c\3\2\2\2\u039f\u03a2\3\2\2\2\u03a0"+
		"\u039e\3\2\2\2\u03a0\u03a1\3\2\2\2\u03a1\u0095\3\2\2\2\u03a2\u03a0\3\2"+
		"\2\2\u03a3\u03a4\5\u0098M\2\u03a4\u03a5\5\u012e\u0098\2\u03a5\u03a6\7"+
		"Y\2\2\u03a6\u03a7\5\u012e\u0098\2\u03a7\u03a8\5\u009aN\2\u03a8\u0097\3"+
		"\2\2\2\u03a9\u03ac\5\u0126\u0094\2\u03aa\u03ac\5\u012a\u0096\2\u03ab\u03a9"+
		"\3\2\2\2\u03ab\u03aa\3\2\2\2\u03ac\u0099\3\2\2\2\u03ad\u03b1\5\u009cO"+
		"\2\u03ae\u03b1\5\u008eH\2\u03af\u03b1\5\u00eav\2\u03b0\u03ad\3\2\2\2\u03b0"+
		"\u03ae\3\2\2\2\u03b0\u03af\3\2\2\2\u03b1\u009b\3\2\2\2\u03b2\u03be\7T"+
		"\2\2\u03b3\u03b8\5\u009aN\2\u03b4\u03b5\7W\2\2\u03b5\u03b7\5\u009aN\2"+
		"\u03b6\u03b4\3\2\2\2\u03b7\u03ba\3\2\2\2\u03b8\u03b6\3\2\2\2\u03b8\u03b9"+
		"\3\2\2\2\u03b9\u03bc\3\2\2\2\u03ba\u03b8\3\2\2\2\u03bb\u03bd\7W\2\2\u03bc"+
		"\u03bb\3\2\2\2\u03bc\u03bd\3\2\2\2\u03bd\u03bf\3\2\2\2\u03be\u03b3\3\2"+
		"\2\2\u03be\u03bf\3\2\2\2\u03bf\u03c0\3\2\2\2\u03c0\u03c1\7U\2\2\u03c1"+
		"\u009d\3\2\2\2\u03c2\u03c4\7R\2\2\u03c3\u03c5\5\u0130\u0099\2\u03c4\u03c3"+
		"\3\2\2\2\u03c4\u03c5\3\2\2\2\u03c5\u03c6\3\2\2\2\u03c6\u03c7\5\u0088E"+
		"\2\u03c7\u03c8\7S\2\2\u03c8\u009f\3\2\2\2\u03c9\u03cc\5\u00a2R\2\u03ca"+
		"\u03cc\5\u00be`\2\u03cb\u03c9\3\2\2\2\u03cb\u03ca\3\2\2\2\u03cc\u00a1"+
		"\3\2\2\2\u03cd\u03ce\6R\b\2\u03ce\u03cf\5\u00a6T\2\u03cf\u00a3\3\2\2\2"+
		"\u03d0\u03d1\5\22\n\2\u03d1\u03d2\5\u012e\u0098\2\u03d2\u00a5\3\2\2\2"+
		"\u03d3\u03de\5\u00a4S\2\u03d4\u03d6\5L\'\2\u03d5\u03d4\3\2\2\2\u03d5\u03d6"+
		"\3\2\2\2\u03d6\u03d7\3\2\2\2\u03d7\u03df\5<\37\2\u03d8\u03d9\5\u00a8U"+
		"\2\u03d9\u03da\5\u012e\u0098\2\u03da\u03db\7Y\2\2\u03db\u03dc\5\u012e"+
		"\u0098\2\u03dc\u03dd\5B\"\2\u03dd\u03df\3\2\2\2\u03de\u03d5\3\2\2\2\u03de"+
		"\u03d8\3\2\2\2\u03df\u03e4\3\2\2\2\u03e0\u03e1\5L\'\2\u03e1\u03e2\5<\37"+
		"\2\u03e2\u03e4\3\2\2\2\u03e3\u03d3\3\2\2\2\u03e3\u03e0\3\2\2\2\u03e4\u00a7"+
		"\3\2\2\2\u03e5\u03e6\7P\2\2\u03e6\u03eb\5\u00aaV\2\u03e7\u03e8\7W\2\2"+
		"\u03e8\u03ea\5\u00aaV\2\u03e9\u03e7\3\2\2\2\u03ea\u03ed\3\2\2\2\u03eb"+
		"\u03e9\3\2\2\2\u03eb\u03ec\3\2\2\2\u03ec\u03ee\3\2\2\2\u03ed\u03eb\3\2"+
		"\2\2\u03ee\u03ef\5\u012c\u0097\2\u03ef\u00a9\3\2\2\2\u03f0\u03f2\5L\'"+
		"\2\u03f1\u03f0\3\2\2\2\u03f1\u03f2\3\2\2\2\u03f2\u03f3\3\2\2\2\u03f3\u03f4"+
		"\5@!\2\u03f4\u00ab\3\2\2\2\u03f5\u03f6\7P\2\2\u03f6\u03f9\5@!\2\u03f7"+
		"\u03f8\7W\2\2\u03f8\u03fa\5@!\2\u03f9\u03f7\3\2\2\2\u03fa\u03fb\3\2\2"+
		"\2\u03fb\u03f9\3\2\2\2\u03fb\u03fc\3\2\2\2\u03fc\u03fd\3\2\2\2\u03fd\u03fe"+
		"\5\u012c\u0097\2\u03fe\u00ad\3\2\2\2\u03ff\u0402\5\u00b0Y\2\u0400\u0402"+
		"\5\u00b2Z\2\u0401\u03ff\3\2\2\2\u0401\u0400\3\2\2\2\u0402\u00af\3\2\2"+
		"\2\u0403\u0404\7 \2\2\u0404\u0405\5\u00dep\2\u0405\u0406\5\u012e\u0098"+
		"\2\u0406\u040f\5\u00be`\2\u0407\u040a\5\u012e\u0098\2\u0408\u040a\5\u0130"+
		"\u0099\2\u0409\u0407\3\2\2\2\u0409\u0408\3\2\2\2\u040a\u040b\3\2\2\2\u040b"+
		"\u040c\7\32\2\2\u040c\u040d\5\u012e\u0098\2\u040d\u040e\5\u00be`\2\u040e"+
		"\u0410\3\2\2\2\u040f\u0409\3\2\2\2\u040f\u0410\3\2\2\2\u0410\u00b1\3\2"+
		"\2\2\u0411\u0412\7\60\2\2\u0412\u0413\5\u00dep\2\u0413\u0414\5\u012e\u0098"+
		"\2\u0414\u0415\7R\2\2\u0415\u041d\5\u012e\u0098\2\u0416\u0418\5\u00cc"+
		"g\2\u0417\u0416\3\2\2\2\u0418\u0419\3\2\2\2\u0419\u0417\3\2\2\2\u0419"+
		"\u041a\3\2\2\2\u041a\u041b\3\2\2\2\u041b\u041c\5\u012e\u0098\2\u041c\u041e"+
		"\3\2\2\2\u041d\u0417\3\2\2\2\u041d\u041e\3\2\2\2\u041e\u041f\3\2\2\2\u041f"+
		"\u0420\7S\2\2\u0420\u00b3\3\2\2\2\u0421\u0422\7\37\2\2\u0422\u0423\7P"+
		"\2\2\u0423\u0424\5\u00d0i\2\u0424\u0425\5\u012c\u0097\2\u0425\u0426\5"+
		"\u012e\u0098\2\u0426\u0427\5\u00be`\2\u0427\u0435\3\2\2\2\u0428\u0429"+
		"\79\2\2\u0429\u042a\5\u00dep\2\u042a\u042b\5\u012e\u0098\2\u042b\u042c"+
		"\5\u00be`\2\u042c\u0435\3\2\2\2\u042d\u042e\7\31\2\2\u042e\u042f\5\u012e"+
		"\u0098\2\u042f\u0430\5\u00be`\2\u0430\u0431\5\u012e\u0098\2\u0431\u0432"+
		"\79\2\2\u0432\u0433\5\u00dep\2\u0433\u0435\3\2\2\2\u0434\u0421\3\2\2\2"+
		"\u0434\u0428\3\2\2\2\u0434\u042d\3\2\2\2\u0435\u00b5\3\2\2\2\u0436\u0438"+
		"\7\27\2\2\u0437\u0439\5\u0126\u0094\2\u0438\u0437\3\2\2\2\u0438\u0439"+
		"\3\2\2\2\u0439\u00b7\3\2\2\2\u043a\u043c\7\22\2\2\u043b\u043d\5\u0126"+
		"\u0094\2\u043c\u043b\3\2\2\2\u043c\u043d\3\2\2\2\u043d\u00b9\3\2\2\2\u043e"+
		"\u0440\7\66\2\2\u043f\u0441\5\u00c6d\2\u0440\u043f\3\2\2\2\u0440\u0441"+
		"\3\2\2\2\u0441\u0442\3\2\2\2\u0442\u0443\5\u012e\u0098\2\u0443\u0449\5"+
		"\u009eP\2\u0444\u0445\5\u012e\u0098\2\u0445\u0446\5\u00c0a\2\u0446\u0448"+
		"\3\2\2\2\u0447\u0444\3\2\2\2\u0448\u044b\3\2\2\2\u0449\u0447\3\2\2\2\u0449"+
		"\u044a\3\2\2\2\u044a\u044f\3\2\2\2\u044b\u0449\3\2\2\2\u044c\u044d\5\u012e"+
		"\u0098\2\u044d\u044e\5\u00c4c\2\u044e\u0450\3\2\2\2\u044f\u044c\3\2\2"+
		"\2\u044f\u0450\3\2\2\2\u0450\u00bb\3\2\2\2\u0451\u0452\7\21\2\2\u0452"+
		"\u0458\5\u00eav\2\u0453\u0454\5\u012e\u0098\2\u0454\u0455\t\6\2\2\u0455"+
		"\u0456\5\u012e\u0098\2\u0456\u0457\5\u00eav\2\u0457\u0459\3\2\2\2\u0458"+
		"\u0453\3\2\2\2\u0458\u0459\3\2\2\2\u0459\u00bd\3\2\2\2\u045a\u0477\5\u009e"+
		"P\2\u045b\u0477\5\u00aeX\2\u045c\u0477\5\u00b4[\2\u045d\u0477\5\u00ba"+
		"^\2\u045e\u045f\7\61\2\2\u045f\u0460\5\u00dep\2\u0460\u0461\5\u012e\u0098"+
		"\2\u0461\u0462\5\u009eP\2\u0462\u0477\3\2\2\2\u0463\u0465\7,\2\2\u0464"+
		"\u0466\5\u00eav\2\u0465\u0464\3\2\2\2\u0465\u0466\3\2\2\2\u0466\u0477"+
		"\3\2\2\2\u0467\u0468\7\63\2\2\u0468\u0477\5\u00eav\2\u0469\u0477\5\u00b8"+
		"]\2\u046a\u0477\5\u00b6\\\2\u046b\u046c\5\u0126\u0094\2\u046c\u046d\7"+
		"_\2\2\u046d\u046e\5\u012e\u0098\2\u046e\u046f\5\u00be`\2\u046f\u0477\3"+
		"\2\2\2\u0470\u0477\5\u00bc_\2\u0471\u0477\5\u00a2R\2\u0472\u0473\6`\t"+
		"\2\u0473\u0477\5\64\33\2\u0474\u0477\5\u00e6t\2\u0475\u0477\7V\2\2\u0476"+
		"\u045a\3\2\2\2\u0476\u045b\3\2\2\2\u0476\u045c\3\2\2\2\u0476\u045d\3\2"+
		"\2\2\u0476\u045e\3\2\2\2\u0476\u0463\3\2\2\2\u0476\u0467\3\2\2\2\u0476"+
		"\u0469\3\2\2\2\u0476\u046a\3\2\2\2\u0476\u046b\3\2\2\2\u0476\u0470\3\2"+
		"\2\2\u0476\u0471\3\2\2\2\u0476\u0472\3\2\2\2\u0476\u0474\3\2\2\2\u0476"+
		"\u0475\3\2\2\2\u0477\u00bf\3\2\2\2\u0478\u0479\7\24\2\2\u0479\u047a\7"+
		"P\2\2\u047a\u047c\5\34\17\2\u047b\u047d\5\u00c2b\2\u047c\u047b\3\2\2\2"+
		"\u047c\u047d\3\2\2\2\u047d\u047e\3\2\2\2\u047e\u047f\5\u0126\u0094\2\u047f"+
		"\u0480\5\u012c\u0097\2\u0480\u0481\5\u012e\u0098\2\u0481\u0482\5\u009e"+
		"P\2\u0482\u00c1\3\2\2\2\u0483\u0488\5n8\2\u0484\u0485\7m\2\2\u0485\u0487"+
		"\5n8\2\u0486\u0484\3\2\2\2\u0487\u048a\3\2\2\2\u0488\u0486\3\2\2\2\u0488"+
		"\u0489\3\2\2\2\u0489\u00c3\3\2\2\2\u048a\u0488\3\2\2\2\u048b\u048c\7\36"+
		"\2\2\u048c\u048d\5\u012e\u0098\2\u048d\u048e\5\u009eP\2\u048e\u00c5\3"+
		"\2\2\2\u048f\u0490\7P\2\2\u0490\u0491\5\u012e\u0098\2\u0491\u0493\5\u00c8"+
		"e\2\u0492\u0494\5\u0130\u0099\2\u0493\u0492\3\2\2\2\u0493\u0494\3\2\2"+
		"\2\u0494\u0495\3\2\2\2\u0495\u0496\5\u012c\u0097\2\u0496\u00c7\3\2\2\2"+
		"\u0497\u049d\5\u00caf\2\u0498\u0499\5\u0130\u0099\2\u0499\u049a\5\u00ca"+
		"f\2\u049a\u049c\3\2\2\2\u049b\u0498\3\2\2\2\u049c\u049f\3\2\2\2\u049d"+
		"\u049b\3\2\2\2\u049d\u049e\3\2\2\2\u049e\u00c9\3\2\2\2\u049f\u049d\3\2"+
		"\2\2\u04a0\u04a3\5\u00a2R\2\u04a1\u04a3\5\u00eav\2\u04a2\u04a0\3\2\2\2"+
		"\u04a2\u04a1\3\2\2\2\u04a3\u00cb\3\2\2\2\u04a4\u04aa\5\u00ceh\2\u04a5"+
		"\u04a6\5\u012e\u0098\2\u04a6\u04a7\5\u00ceh\2\u04a7\u04a9\3\2\2\2\u04a8"+
		"\u04a5\3\2\2\2\u04a9\u04ac\3\2\2\2\u04aa\u04a8\3\2\2\2\u04aa\u04ab\3\2"+
		"\2\2\u04ab\u04ad\3\2\2\2\u04ac\u04aa\3\2\2\2\u04ad\u04ae\5\u012e\u0098"+
		"\2\u04ae\u04af\5\u008aF\2\u04af\u00cd\3\2\2\2\u04b0\u04b1\7\23\2\2\u04b1"+
		"\u04b2\5\u00eav\2\u04b2\u04b3\7_\2\2\u04b3\u04b7\3\2\2\2\u04b4\u04b5\7"+
		"\30\2\2\u04b5\u04b7\7_\2\2\u04b6\u04b0\3\2\2\2\u04b6\u04b4\3\2\2\2\u04b7"+
		"\u00cf\3\2\2\2\u04b8\u04bb\5\u00d2j\2\u04b9\u04bb\5\u00d4k\2\u04ba\u04b8"+
		"\3\2\2\2\u04ba\u04b9\3\2\2\2\u04bb\u00d1\3\2\2\2\u04bc\u04be\5\34\17\2"+
		"\u04bd\u04bf\5L\'\2\u04be\u04bd\3\2\2\2\u04be\u04bf\3\2\2\2\u04bf\u04c0"+
		"\3\2\2\2\u04c0\u04c1\5@!\2\u04c1\u04c2\t\7\2\2\u04c2\u04c3\5\u00eav\2"+
		"\u04c3\u00d3\3\2\2\2\u04c4\u04c6\5\u00d6l\2\u04c5\u04c4\3\2\2\2\u04c5"+
		"\u04c6\3\2\2\2\u04c6\u04c7\3\2\2\2\u04c7\u04c9\7V\2\2\u04c8\u04ca\5\u00ea"+
		"v\2\u04c9\u04c8\3\2\2\2\u04c9\u04ca\3\2\2\2\u04ca\u04cb\3\2\2\2\u04cb"+
		"\u04cd\7V\2\2\u04cc\u04ce\5\u00d8m\2\u04cd\u04cc\3\2\2\2\u04cd\u04ce\3"+
		"\2\2\2\u04ce\u00d5\3\2\2\2\u04cf\u04d2\5\u00a2R\2\u04d0\u04d2\5\u00e0"+
		"q\2\u04d1\u04cf\3\2\2\2\u04d1\u04d0\3\2\2\2\u04d2\u00d7\3\2\2\2\u04d3"+
		"\u04d4\5\u00e0q\2\u04d4\u00d9\3\2\2\2\u04d5\u04d6\7P\2\2\u04d6\u04d7\5"+
		"L\'\2\u04d7\u04d8\5\u012c\u0097\2\u04d8\u00db\3\2\2\2\u04d9\u04da\5\u00de"+
		"p\2\u04da\u00dd\3\2\2\2\u04db\u04dc\7P\2\2\u04dc\u04dd\5\u00e4s\2\u04dd"+
		"\u04de\5\u012c\u0097\2\u04de\u00df\3\2\2\2\u04df\u04e4\5\u00e2r\2\u04e0"+
		"\u04e1\7W\2\2\u04e1\u04e3\5\u00e2r\2\u04e2\u04e0\3\2\2\2\u04e3\u04e6\3"+
		"\2\2\2\u04e4\u04e2\3\2\2\2\u04e4\u04e5\3\2\2\2\u04e5\u00e1\3\2\2\2\u04e6"+
		"\u04e4\3\2\2\2\u04e7\u04e9\7j\2\2\u04e8\u04e7\3\2\2\2\u04e8\u04e9\3\2"+
		"\2\2\u04e9\u04ea\3\2\2\2\u04ea\u04eb\5\u00eav\2\u04eb\u00e3\3\2\2\2\u04ec"+
		"\u04ef\5\u00e6t\2\u04ed\u04ef\5|?\2\u04ee\u04ec\3\2\2\2\u04ee\u04ed\3"+
		"\2\2\2\u04ef\u00e5\3\2\2\2\u04f0\u04f1\5\u00eex\2\u04f1\u00e7\3\2\2\2"+
		"\u04f2\u04f4\5\u00f2z\2\u04f3\u04f5\t\b\2\2\u04f4\u04f3\3\2\2\2\u04f4"+
		"\u04f5\3\2\2\2\u04f5\u00e9\3\2\2\2\u04f6\u04f7\bv\1\2\u04f7\u04f8\5\u00da"+
		"n\2\u04f8\u04f9\5\u00ecw\2\u04f9\u0508\3\2\2\2\u04fa\u0508\5\u00e8u\2"+
		"\u04fb\u04fc\t\t\2\2\u04fc\u04fd\5\u012e\u0098\2\u04fd\u04fe\5\u00eav"+
		"\24\u04fe\u0508\3\2\2\2\u04ff\u0500\t\n\2\2\u0500\u0508\5\u00eav\22\u0501"+
		"\u0502\5\u00acW\2\u0502\u0503\5\u012e\u0098\2\u0503\u0504\7Y\2\2\u0504"+
		"\u0505\5\u012e\u0098\2\u0505\u0506\5\u00e6t\2\u0506\u0508\3\2\2\2\u0507"+
		"\u04f6\3\2\2\2\u0507\u04fa\3\2\2\2\u0507\u04fb\3\2\2\2\u0507\u04ff\3\2"+
		"\2\2\u0507\u0501\3\2\2\2\u0508\u0577\3\2\2\2\u0509\u050a\f\23\2\2\u050a"+
		"\u050b\7H\2\2\u050b\u050c\5\u012e\u0098\2\u050c\u050d\5\u00eav\24\u050d"+
		"\u0576\3\2\2\2\u050e\u050f\f\21\2\2\u050f\u0510\5\u012e\u0098\2\u0510"+
		"\u0511\t\13\2\2\u0511\u0512\5\u012e\u0098\2\u0512\u0513\5\u00eav\22\u0513"+
		"\u0576\3\2\2\2\u0514\u0515\f\20\2\2\u0515\u0516\t\f\2\2\u0516\u0517\5"+
		"\u012e\u0098\2\u0517\u0518\5\u00eav\21\u0518\u0576\3\2\2\2\u0519\u051a"+
		"\f\17\2\2\u051a\u0525\5\u012e\u0098\2\u051b\u051c\7[\2\2\u051c\u0523\7"+
		"[\2\2\u051d\u051e\7Z\2\2\u051e\u051f\7Z\2\2\u051f\u0523\7Z\2\2\u0520\u0521"+
		"\7Z\2\2\u0521\u0523\7Z\2\2\u0522\u051b\3\2\2\2\u0522\u051d\3\2\2\2\u0522"+
		"\u0520\3\2\2\2\u0523\u0526\3\2\2\2\u0524\u0526\t\r\2\2\u0525\u0522\3\2"+
		"\2\2\u0525\u0524\3\2\2\2\u0526\u0527\3\2\2\2\u0527\u0528\5\u012e\u0098"+
		"\2\u0528\u0529\5\u00eav\20\u0529\u0576\3\2\2\2\u052a\u052b\f\r\2\2\u052b"+
		"\u052c\5\u012e\u0098\2\u052c\u052d\t\16\2\2\u052d\u052e\5\u012e\u0098"+
		"\2\u052e\u052f\5\u00eav\16\u052f\u0576\3\2\2\2\u0530\u0531\f\f\2\2\u0531"+
		"\u0532\5\u012e\u0098\2\u0532\u0533\t\17\2\2\u0533\u0534\5\u012e\u0098"+
		"\2\u0534\u0535\5\u00eav\r\u0535\u0576\3\2\2\2\u0536\u0537\f\13\2\2\u0537"+
		"\u0538\5\u012e\u0098\2\u0538\u0539\t\20\2\2\u0539\u053a\5\u012e\u0098"+
		"\2\u053a\u053b\5\u00eav\f\u053b\u0576\3\2\2\2\u053c\u053d\f\n\2\2\u053d"+
		"\u053e\5\u012e\u0098\2\u053e\u053f\7l\2\2\u053f\u0540\5\u012e\u0098\2"+
		"\u0540\u0541\5\u00eav\13\u0541\u0576\3\2\2\2\u0542\u0543\f\t\2\2\u0543"+
		"\u0544\5\u012e\u0098\2\u0544\u0545\7n\2\2\u0545\u0546\5\u012e\u0098\2"+
		"\u0546\u0547\5\u00eav\n\u0547\u0576\3\2\2\2\u0548\u0549\f\b\2\2\u0549"+
		"\u054a\5\u012e\u0098\2\u054a\u054b\7m\2\2\u054b\u054c\5\u012e\u0098\2"+
		"\u054c\u054d\5\u00eav\t\u054d\u0576\3\2\2\2\u054e\u054f\f\7\2\2\u054f"+
		"\u0550\5\u012e\u0098\2\u0550\u0551\7d\2\2\u0551\u0552\5\u012e\u0098\2"+
		"\u0552\u0553\5\u00eav\b\u0553\u0576\3\2\2\2\u0554\u0555\f\6\2\2\u0555"+
		"\u0556\5\u012e\u0098\2\u0556\u0557\7e\2\2\u0557\u0558\5\u012e\u0098\2"+
		"\u0558\u0559\5\u00eav\7\u0559\u0576\3\2\2\2\u055a\u055b\f\5\2\2\u055b"+
		"\u0565\5\u012e\u0098\2\u055c\u055d\7^\2\2\u055d\u055e\5\u012e\u0098\2"+
		"\u055e\u055f\5\u00eav\2\u055f\u0560\5\u012e\u0098\2\u0560\u0561\7_\2\2"+
		"\u0561\u0562\5\u012e\u0098\2\u0562\u0566\3\2\2\2\u0563\u0564\7C\2\2\u0564"+
		"\u0566\5\u012e\u0098\2\u0565\u055c\3\2\2\2\u0565\u0563\3\2\2\2\u0566\u0567"+
		"\3\2\2\2\u0567\u0568\5\u00eav\5\u0568\u0576\3\2\2\2\u0569\u056a\f\16\2"+
		"\2\u056a\u056b\5\u012e\u0098\2\u056b\u056c\t\21\2\2\u056c\u056d\5\u012e"+
		"\u0098\2\u056d\u056e\5L\'\2\u056e\u0576\3\2\2\2\u056f\u0570\f\3\2\2\u0570"+
		"\u0571\5\u012e\u0098\2\u0571\u0572\t\22\2\2\u0572\u0573\5\u012e\u0098"+
		"\2\u0573\u0574\5\u00e4s\2\u0574\u0576\3\2\2\2\u0575\u0509\3\2\2\2\u0575"+
		"\u050e\3\2\2\2\u0575\u0514\3\2\2\2\u0575\u0519\3\2\2\2\u0575\u052a\3\2"+
		"\2\2\u0575\u0530\3\2\2\2\u0575\u0536\3\2\2\2\u0575\u053c\3\2\2\2\u0575"+
		"\u0542\3\2\2\2\u0575\u0548\3\2\2\2\u0575\u054e\3\2\2\2\u0575\u0554\3\2"+
		"\2\2\u0575\u055a\3\2\2\2\u0575\u0569\3\2\2\2\u0575\u056f\3\2\2\2\u0576"+
		"\u0579\3\2\2\2\u0577\u0575\3\2\2\2\u0577\u0578\3\2\2\2\u0578\u00eb\3\2"+
		"\2\2\u0579\u0577\3\2\2\2\u057a\u057b\5\u00dan\2\u057b\u057c\5\u00ecw\2"+
		"\u057c\u0585\3\2\2\2\u057d\u0585\5\u00e8u\2\u057e\u057f\t\t\2\2\u057f"+
		"\u0580\5\u012e\u0098\2\u0580\u0581\5\u00ecw\2\u0581\u0585\3\2\2\2\u0582"+
		"\u0583\t\n\2\2\u0583\u0585\5\u00ecw\2\u0584\u057a\3\2\2\2\u0584\u057d"+
		"\3\2\2\2\u0584\u057e\3\2\2\2\u0584\u0582\3\2\2\2\u0585\u00ed\3\2\2\2\u0586"+
		"\u058a\5\u00eav\2\u0587\u0588\6x\31\3\u0588\u058b\5\u011a\u008e\2\u0589"+
		"\u058b\3\2\2\2\u058a\u0587\3\2\2\2\u058a\u0589\3\2\2\2\u058b\u058f\3\2"+
		"\2\2\u058c\u058e\5\u00f0y\2\u058d\u058c\3\2\2\2\u058e\u0591\3\2\2\2\u058f"+
		"\u058d\3\2\2\2\u058f\u0590\3\2\2\2\u0590\u00ef\3\2\2\2\u0591\u058f\3\2"+
		"\2\2\u0592\u0599\5\u00fe\u0080\2\u0593\u0595\5\u00f4{\2\u0594\u0593\3"+
		"\2\2\2\u0595\u0596\3\2\2\2\u0596\u0594\3\2\2\2\u0596\u0597\3\2\2\2\u0597"+
		"\u059a\3\2\2\2\u0598\u059a\5\u011a\u008e\2\u0599\u0594\3\2\2\2\u0599\u0598"+
		"\3\2\2\2\u0599\u059a\3\2\2\2\u059a\u00f1\3\2\2\2\u059b\u05a1\5\u00fe\u0080"+
		"\2\u059c\u059d\5\u00f4{\2\u059d\u059e\bz\1\2\u059e\u05a0\3\2\2\2\u059f"+
		"\u059c\3\2\2\2\u05a0\u05a3\3\2\2\2\u05a1\u059f\3\2\2\2\u05a1\u05a2\3\2"+
		"\2\2\u05a2\u00f3\3\2\2\2\u05a3\u05a1\3\2\2\2\u05a4\u05bd\5\u012e\u0098"+
		"\2\u05a5\u05a6\t\23\2\2\u05a6\u05a9\5\u012e\u0098\2\u05a7\u05aa\7~\2\2"+
		"\u05a8\u05aa\5\u0114\u008b\2\u05a9\u05a7\3\2\2\2\u05a9\u05a8\3\2\2\2\u05a9"+
		"\u05aa\3\2\2\2\u05aa\u05b0\3\2\2\2\u05ab\u05ac\7D\2\2\u05ac\u05b0\5\u012e"+
		"\u0098\2\u05ad\u05ae\7E\2\2\u05ae\u05b0\5\u012e\u0098\2\u05af\u05a5\3"+
		"\2\2\2\u05af\u05ab\3\2\2\2\u05af\u05ad\3\2\2\2\u05b0\u05b1\3\2\2\2\u05b1"+
		"\u05b2\5\u00f6|\2\u05b2\u05b3\b{\1\2\u05b3\u05be\3\2\2\2\u05b4\u05b5\7"+
		"X\2\2\u05b5\u05b6\5\u012e\u0098\2\u05b6\u05b7\7\'\2\2\u05b7\u05b8\5\u010a"+
		"\u0086\2\u05b8\u05b9\b{\1\2\u05b9\u05be\3\2\2\2\u05ba\u05bb\5\u0086D\2"+
		"\u05bb\u05bc\b{\1\2\u05bc\u05be\3\2\2\2\u05bd\u05af\3\2\2\2\u05bd\u05b4"+
		"\3\2\2\2\u05bd\u05ba\3\2\2\2\u05be\u05c9\3\2\2\2\u05bf\u05c0\5\u0118\u008d"+
		"\2\u05c0\u05c1\b{\1\2\u05c1\u05c9\3\2\2\2\u05c2\u05c3\5\u00fa~\2\u05c3"+
		"\u05c4\b{\1\2\u05c4\u05c9\3\2\2\2\u05c5\u05c6\5\u00fc\177\2\u05c6\u05c7"+
		"\b{\1\2\u05c7\u05c9\3\2\2\2\u05c8\u05a4\3\2\2\2\u05c8\u05bf\3\2\2\2\u05c8"+
		"\u05c2\3\2\2\2\u05c8\u05c5\3\2\2\2\u05c9\u00f5\3\2\2\2\u05ca\u05cf\5\u0126"+
		"\u0094\2\u05cb\u05cf\5\u0122\u0092\2\u05cc\u05cf\5\u00f8}\2\u05cd\u05cf"+
		"\5\u012a\u0096\2\u05ce\u05ca\3\2\2\2\u05ce\u05cb\3\2\2\2\u05ce\u05cc\3"+
		"\2\2\2\u05ce\u05cd\3\2\2\2\u05cf\u00f7\3\2\2\2\u05d0\u05d3\5\u00dco\2"+
		"\u05d1\u05d3\5t;\2\u05d2\u05d0\3\2\2\2\u05d2\u05d1\3\2\2\2\u05d3\u00f9"+
		"\3\2\2\2\u05d4\u05d6\7^\2\2\u05d5\u05d4\3\2\2\2\u05d5\u05d6\3\2\2\2\u05d6"+
		"\u05d7\3\2\2\2\u05d7\u05d9\7T\2\2\u05d8\u05da\5\u00e0q\2\u05d9\u05d8\3"+
		"\2\2\2\u05d9\u05da\3\2\2\2\u05da\u05db\3\2\2\2\u05db\u05dc\7U\2\2\u05dc"+
		"\u00fb\3\2\2\2\u05dd\u05df\7^\2\2\u05de\u05dd\3\2\2\2\u05de\u05df\3\2"+
		"\2\2\u05df\u05e0\3\2\2\2\u05e0\u05e3\7T\2\2\u05e1\u05e4\5\u0104\u0083"+
		"\2\u05e2\u05e4\7_\2\2\u05e3\u05e1\3\2\2\2\u05e3\u05e2\3\2\2\2\u05e4\u05e5"+
		"\3\2\2\2\u05e5\u05e6\7U\2\2\u05e6\u00fd\3\2\2\2\u05e7\u05e9\5\u0126\u0094"+
		"\2\u05e8\u05ea\5V,\2\u05e9\u05e8\3\2\2\2\u05e9\u05ea\3\2\2\2\u05ea\u05f9"+
		"\3\2\2\2\u05eb\u05f9\5r:\2\u05ec\u05f9\5t;\2\u05ed\u05ee\7\'\2\2\u05ee"+
		"\u05ef\5\u012e\u0098\2\u05ef\u05f0\5\u010a\u0086\2\u05f0\u05f9\3\2\2\2"+
		"\u05f1\u05f9\7\62\2\2\u05f2\u05f9\7/\2\2\u05f3\u05f9\5\u00dco\2\u05f4"+
		"\u05f9\5\u0086D\2\u05f5\u05f9\5\u0100\u0081\2\u05f6\u05f9\5\u0102\u0082"+
		"\2\u05f7\u05f9\5\u0128\u0095\2\u05f8\u05e7\3\2\2\2\u05f8\u05eb\3\2\2\2"+
		"\u05f8\u05ec\3\2\2\2\u05f8\u05ed\3\2\2\2\u05f8\u05f1\3\2\2\2\u05f8\u05f2"+
		"\3\2\2\2\u05f8\u05f3\3\2\2\2\u05f8\u05f4\3\2\2\2\u05f8\u05f5\3\2\2\2\u05f8"+
		"\u05f6\3\2\2\2\u05f8\u05f7\3\2\2\2\u05f9\u00ff\3\2\2\2\u05fa\u05fc\7T"+
		"\2\2\u05fb\u05fd\5\u00e0q\2\u05fc\u05fb\3\2\2\2\u05fc\u05fd\3\2\2\2\u05fd"+
		"\u05ff\3\2\2\2\u05fe\u0600\7W\2\2\u05ff\u05fe\3\2\2\2\u05ff\u0600\3\2"+
		"\2\2\u0600\u0601\3\2\2\2\u0601\u0602\7U\2\2\u0602\u0101\3\2\2\2\u0603"+
		"\u0609\7T\2\2\u0604\u0606\5\u0104\u0083\2\u0605\u0607\7W\2\2\u0606\u0605"+
		"\3\2\2\2\u0606\u0607\3\2\2\2\u0607\u060a\3\2\2\2\u0608\u060a\7_\2\2\u0609"+
		"\u0604\3\2\2\2\u0609\u0608\3\2\2\2\u060a\u060b\3\2\2\2\u060b\u060c\7U"+
		"\2\2\u060c\u0103\3\2\2\2\u060d\u0612\5\u0106\u0084\2\u060e\u060f\7W\2"+
		"\2\u060f\u0611\5\u0106\u0084\2\u0610\u060e\3\2\2\2\u0611\u0614\3\2\2\2"+
		"\u0612\u0610\3\2\2\2\u0612\u0613\3\2\2\2\u0613\u0105\3\2\2\2\u0614\u0612"+
		"\3\2\2\2\u0615\u0616\5\u0108\u0085\2\u0616\u0617\7_\2\2\u0617\u0618\5"+
		"\u012e\u0098\2\u0618\u0619\5\u00eav\2\u0619\u0620\3\2\2\2\u061a\u061b"+
		"\7j\2\2\u061b\u061c\7_\2\2\u061c\u061d\5\u012e\u0098\2\u061d\u061e\5\u00ea"+
		"v\2\u061e\u0620\3\2\2\2\u061f\u0615\3\2\2\2\u061f\u061a\3\2\2\2\u0620"+
		"\u0107\3\2\2\2\u0621\u0624\5\u012a\u0096\2\u0622\u0624\5\u00fe\u0080\2"+
		"\u0623\u0621\3\2\2\2\u0623\u0622\3\2\2\2\u0624\u0109\3\2\2\2\u0625\u0635"+
		"\5\u0112\u008a\2\u0626\u0627\5\u012e\u0098\2\u0627\u0629\5\u0118\u008d"+
		"\2\u0628\u062a\5\u0110\u0089\2\u0629\u0628\3\2\2\2\u0629\u062a\3\2\2\2"+
		"\u062a\u0636\3\2\2\2\u062b\u062d\5\u010c\u0087\2\u062c\u062b\3\2\2\2\u062d"+
		"\u062e\3\2\2\2\u062e\u062c\3\2\2\2\u062e\u062f\3\2\2\2\u062f\u0633\3\2"+
		"\2\2\u0630\u0631\5\u012e\u0098\2\u0631\u0632\5\u010e\u0088\2\u0632\u0634"+
		"\3\2\2\2\u0633\u0630\3\2\2\2\u0633\u0634\3\2\2\2\u0634\u0636\3\2\2\2\u0635"+
		"\u0626\3\2\2\2\u0635\u062c\3\2\2\2\u0636\u010b\3\2\2\2\u0637\u0638\5\u008c"+
		"G\2\u0638\u063a\7T\2\2\u0639\u063b\5\u00eav\2\u063a\u0639\3\2\2\2\u063a"+
		"\u063b\3\2\2\2\u063b\u063c\3\2\2\2\u063c\u063d\7U\2\2\u063d\u010d\3\2"+
		"\2\2\u063e\u063f\7R\2\2\u063f\u0643\5\u012e\u0098\2\u0640\u0641\5D#\2"+
		"\u0641\u0642\5\u012e\u0098\2\u0642\u0644\3\2\2\2\u0643\u0640\3\2\2\2\u0643"+
		"\u0644\3\2\2\2\u0644\u0645\3\2\2\2\u0645\u0646\7S\2\2\u0646\u010f\3\2"+
		"\2\2\u0647\u0648\5*\26\2\u0648\u0111\3\2\2\2\u0649\u064f\5\u008cG\2\u064a"+
		"\u0650\5T+\2\u064b\u064d\5n8\2\u064c\u064e\5\u0116\u008c\2\u064d\u064c"+
		"\3\2\2\2\u064d\u064e\3\2\2\2\u064e\u0650\3\2\2\2\u064f\u064a\3\2\2\2\u064f"+
		"\u064b\3\2\2\2\u0650\u0113\3\2\2\2\u0651\u0652\7[\2\2\u0652\u0653\5\u012e"+
		"\u0098\2\u0653\u0654\5&\24\2\u0654\u0655\5\u012e\u0098\2\u0655\u0656\7"+
		"Z\2\2\u0656\u0115\3\2\2\2\u0657\u0658\7[\2\2\u0658\u065b\7Z\2\2\u0659"+
		"\u065b\5V,\2\u065a\u0657\3\2\2\2\u065a\u0659\3\2\2\2\u065b\u0117\3\2\2"+
		"\2\u065c\u065e\7P\2\2\u065d\u065f\5\u011c\u008f\2\u065e\u065d\3\2\2\2"+
		"\u065e\u065f\3\2\2\2\u065f\u0661\3\2\2\2\u0660\u0662\7W\2\2\u0661\u0660"+
		"\3\2\2\2\u0661\u0662\3\2\2\2\u0662\u0663\3\2\2\2\u0663\u0664\5\u012c\u0097"+
		"\2\u0664\u0119\3\2\2\2\u0665\u066c\5\u011e\u0090\2\u0666\u0667\7W\2\2"+
		"\u0667\u0668\5\u012e\u0098\2\u0668\u0669\5\u011e\u0090\2\u0669\u066b\3"+
		"\2\2\2\u066a\u0666\3\2\2\2\u066b\u066e\3\2\2\2\u066c\u066a\3\2\2\2\u066c"+
		"\u066d\3\2\2\2\u066d\u011b\3\2\2\2\u066e\u066c\3\2\2\2\u066f\u0676\5\u0120"+
		"\u0091\2\u0670\u0671\7W\2\2\u0671\u0672\5\u012e\u0098\2\u0672\u0673\5"+
		"\u0120\u0091\2\u0673\u0675\3\2\2\2\u0674\u0670\3\2\2\2\u0675\u0678\3\2"+
		"\2\2\u0676\u0674\3\2\2\2\u0676\u0677\3\2\2\2\u0677\u011d\3\2\2\2\u0678"+
		"\u0676\3\2\2\2\u0679\u067c\5\u00e2r\2\u067a\u067c\5\u0106\u0084\2\u067b"+
		"\u0679\3\2\2\2\u067b\u067a\3\2\2\2\u067c\u011f\3\2\2\2\u067d\u0681\5\u00e2"+
		"r\2\u067e\u0681\5|?\2\u067f\u0681\5\u0106\u0084\2\u0680\u067d\3\2\2\2"+
		"\u0680\u067e\3\2\2\2\u0680\u067f\3\2\2\2\u0681\u0121\3\2\2\2\u0682\u0683"+
		"\7\3\2\2\u0683\u0123\3\2\2\2\u0684\u0685\7|\2\2\u0685\u0125\3\2\2\2\u0686"+
		"\u068f\7}\2\2\u0687\u068f\7|\2\2\u0688\u068f\7\16\2\2\u0689\u068f\7\13"+
		"\2\2\u068a\u068f\7\f\2\2\u068b\u068f\7\t\2\2\u068c\u068d\6\u0094\32\2"+
		"\u068d\u068f\7-\2\2\u068e\u0686\3\2\2\2\u068e\u0687\3\2\2\2\u068e\u0688"+
		"\3\2\2\2\u068e\u0689\3\2\2\2\u068e\u068a\3\2\2\2\u068e\u068b\3\2\2\2\u068e"+
		"\u068c\3\2\2\2\u068f\u0127\3\2\2\2\u0690\u0691\t\24\2\2\u0691\u0129\3"+
		"\2\2\2\u0692\u0693\t\25\2\2\u0693\u012b\3\2\2\2\u0694\u0697\7Q\2\2\u0695"+
		"\u0697\b\u0097\1\2\u0696\u0694\3\2\2\2\u0696\u0695\3\2\2\2\u0697\u012d"+
		"\3\2\2\2\u0698\u069a\7\u0081\2\2\u0699\u0698\3\2\2\2\u069a\u069d\3\2\2"+
		"\2\u069b\u0699\3\2\2\2\u069b\u069c\3\2\2\2\u069c\u012f\3\2\2\2\u069d\u069b"+
		"\3\2\2\2\u069e\u06a0\t\26\2\2\u069f\u069e\3\2\2\2\u06a0\u06a1\3\2\2\2"+
		"\u06a1\u069f\3\2\2\2\u06a1\u06a2\3\2\2\2\u06a2\u0131\3\2\2\2\u00be\u0134"+
		"\u0137\u013a\u0144\u0148\u014d\u0156\u015d\u0164\u0169\u0171\u0178\u017b"+
		"\u0183\u0188\u018c\u0191\u0199\u01a5\u01b0\u01b9\u01c3\u01d1\u01d9\u01e1"+
		"\u01e6\u01ef\u01f2\u01fd\u0200\u0203\u020b\u020e\u0211\u021d\u0223\u0226"+
		"\u022a\u022e\u0235\u0240\u0244\u0247\u0250\u0255\u0257\u025b\u025f\u026a"+
		"\u0273\u0281\u0286\u028e\u0291\u0296\u029d\u02a0\u02a6\u02a9\u02ad\u02b1"+
		"\u02be\u02cb\u02cd\u02d9\u02de\u02e4\u02ec\u02f4\u02f7\u02ff\u0308\u0310"+
		"\u0317\u0323\u032b\u0333\u033b\u033f\u0345\u0358\u035c\u0361\u0367\u036e"+
		"\u0371\u0379\u037d\u0385\u038a\u0390\u0393\u0397\u03a0\u03ab\u03b0\u03b8"+
		"\u03bc\u03be\u03c4\u03cb\u03d5\u03de\u03e3\u03eb\u03f1\u03fb\u0401\u0409"+
		"\u040f\u0419\u041d\u0434\u0438\u043c\u0440\u0449\u044f\u0458\u0465\u0476"+
		"\u047c\u0488\u0493\u049d\u04a2\u04aa\u04b6\u04ba\u04be\u04c5\u04c9\u04cd"+
		"\u04d1\u04e4\u04e8\u04ee\u04f4\u0507\u0522\u0525\u0565\u0575\u0577\u0584"+
		"\u058a\u058f\u0596\u0599\u05a1\u05a9\u05af\u05bd\u05c8\u05ce\u05d2\u05d5"+
		"\u05d9\u05de\u05e3\u05e9\u05f8\u05fc\u05ff\u0606\u0609\u0612\u061f\u0623"+
		"\u0629\u062e\u0633\u0635\u063a\u0643\u064d\u064f\u065a\u065e\u0661\u066c"+
		"\u0676\u067b\u0680\u068e\u0696\u069b\u06a1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
	}
}