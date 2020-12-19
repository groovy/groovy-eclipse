// Generated from GroovyParser.g4 by ANTLR 4.7.4
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
		RULE_blockStatement = 79, RULE_localVariableDeclaration = 80, RULE_variableDeclaration = 81, 
		RULE_typeNamePairs = 82, RULE_typeNamePair = 83, RULE_variableNames = 84, 
		RULE_conditionalStatement = 85, RULE_ifElseStatement = 86, RULE_switchStatement = 87, 
		RULE_loopStatement = 88, RULE_continueStatement = 89, RULE_breakStatement = 90, 
		RULE_tryCatchStatement = 91, RULE_assertStatement = 92, RULE_statement = 93, 
		RULE_catchClause = 94, RULE_catchType = 95, RULE_finallyBlock = 96, RULE_resources = 97, 
		RULE_resourceList = 98, RULE_resource = 99, RULE_switchBlockStatementGroup = 100, 
		RULE_switchLabel = 101, RULE_forControl = 102, RULE_enhancedForControl = 103, 
		RULE_classicalForControl = 104, RULE_forInit = 105, RULE_forUpdate = 106, 
		RULE_castParExpression = 107, RULE_parExpression = 108, RULE_expressionInPar = 109, 
		RULE_expressionList = 110, RULE_expressionListElement = 111, RULE_enhancedStatementExpression = 112, 
		RULE_statementExpression = 113, RULE_postfixExpression = 114, RULE_expression = 115, 
		RULE_castOperandExpression = 116, RULE_commandExpression = 117, RULE_commandArgument = 118, 
		RULE_pathExpression = 119, RULE_pathElement = 120, RULE_namePart = 121, 
		RULE_dynamicMemberName = 122, RULE_indexPropertyArgs = 123, RULE_namedPropertyArgs = 124, 
		RULE_primary = 125, RULE_namedPropertyArgPrimary = 126, RULE_namedArgPrimary = 127, 
		RULE_commandPrimary = 128, RULE_list = 129, RULE_map = 130, RULE_mapEntryList = 131, 
		RULE_namedPropertyArgList = 132, RULE_mapEntry = 133, RULE_namedPropertyArg = 134, 
		RULE_namedArg = 135, RULE_mapEntryLabel = 136, RULE_namedPropertyArgLabel = 137, 
		RULE_namedArgLabel = 138, RULE_creator = 139, RULE_dim = 140, RULE_arrayInitializer = 141, 
		RULE_anonymousInnerClassDeclaration = 142, RULE_createdName = 143, RULE_nonWildcardTypeArguments = 144, 
		RULE_typeArgumentsOrDiamond = 145, RULE_arguments = 146, RULE_argumentList = 147, 
		RULE_enhancedArgumentList = 148, RULE_enhancedArgumentListInPar = 149, 
		RULE_firstArgumentListElement = 150, RULE_argumentListElement = 151, RULE_firstEnhancedArgumentListElement = 152, 
		RULE_enhancedArgumentListElement = 153, RULE_stringLiteral = 154, RULE_className = 155, 
		RULE_identifier = 156, RULE_builtInType = 157, RULE_keywords = 158, RULE_rparen = 159, 
		RULE_nls = 160, RULE_sep = 161;
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
			"variableDeclaration", "typeNamePairs", "typeNamePair", "variableNames", 
			"conditionalStatement", "ifElseStatement", "switchStatement", "loopStatement", 
			"continueStatement", "breakStatement", "tryCatchStatement", "assertStatement", 
			"statement", "catchClause", "catchType", "finallyBlock", "resources", 
			"resourceList", "resource", "switchBlockStatementGroup", "switchLabel", 
			"forControl", "enhancedForControl", "classicalForControl", "forInit", 
			"forUpdate", "castParExpression", "parExpression", "expressionInPar", 
			"expressionList", "expressionListElement", "enhancedStatementExpression", 
			"statementExpression", "postfixExpression", "expression", "castOperandExpression", 
			"commandExpression", "commandArgument", "pathExpression", "pathElement", 
			"namePart", "dynamicMemberName", "indexPropertyArgs", "namedPropertyArgs", 
			"primary", "namedPropertyArgPrimary", "namedArgPrimary", "commandPrimary", 
			"list", "map", "mapEntryList", "namedPropertyArgList", "mapEntry", "namedPropertyArg", 
			"namedArg", "mapEntryLabel", "namedPropertyArgLabel", "namedArgLabel", 
			"creator", "dim", "arrayInitializer", "anonymousInnerClassDeclaration", 
			"createdName", "nonWildcardTypeArguments", "typeArgumentsOrDiamond", 
			"arguments", "argumentList", "enhancedArgumentList", "enhancedArgumentListInPar", 
			"firstArgumentListElement", "argumentListElement", "firstEnhancedArgumentListElement", 
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
			setState(324);
			nls();
			setState(329);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(325);
				packageDeclaration();
				setState(327);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(326);
					sep();
					}
					break;
				}
				}
				break;
			}
			setState(332);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(331);
				scriptStatements();
				}
				break;
			}
			setState(334);
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
			setState(336);
			scriptStatement();
			setState(342);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(337);
					sep();
					setState(338);
					scriptStatement();
					}
					} 
				}
				setState(344);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(346);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(345);
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
			setState(353);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(348);
				importDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(349);
				typeDeclaration();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(350);
				if (!( !SemanticPredicates.isInvalidMethodDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidMethodDeclaration(_input) ");
				setState(351);
				methodDeclaration(3, 9);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(352);
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
			setState(355);
			annotationsOpt();
			setState(356);
			match(PACKAGE);
			setState(357);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(359);
			annotationsOpt();
			setState(360);
			match(IMPORT);
			setState(362);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STATIC) {
				{
				setState(361);
				match(STATIC);
				}
			}

			setState(364);
			qualifiedName();
			setState(369);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
				{
				setState(365);
				match(DOT);
				setState(366);
				match(MUL);
				}
				break;
			case AS:
				{
				setState(367);
				match(AS);
				setState(368);
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
			setState(371);
			classOrInterfaceModifiersOpt();
			setState(372);
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
			setState(376);
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
				setState(374);
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
				setState(375);
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
			setState(381);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(378);
				modifiers();
				setState(379);
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
			setState(383);
			modifier();
			setState(389);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(384);
					nls();
					setState(385);
					modifier();
					}
					} 
				}
				setState(391);
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
			setState(399);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(392);
				classOrInterfaceModifiers();
				setState(396);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NL) {
					{
					{
					setState(393);
					match(NL);
					}
					}
					setState(398);
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
			setState(401);
			classOrInterfaceModifier();
			setState(407);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(402);
					nls();
					setState(403);
					classOrInterfaceModifier();
					}
					} 
				}
				setState(409);
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
			setState(412);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(410);
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
				setState(411);
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
			setState(416);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(414);
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
				setState(415);
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
			setState(421);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(418);
				variableModifiers();
				setState(419);
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
			setState(423);
			variableModifier();
			setState(429);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(424);
					nls();
					setState(425);
					variableModifier();
					}
					} 
				}
				setState(431);
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
			setState(432);
			match(LT);
			setState(433);
			nls();
			setState(434);
			typeParameter();
			setState(441);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(435);
					match(COMMA);
					setState(436);
					nls();
					setState(437);
					typeParameter();
					}
					} 
				}
				setState(443);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			setState(444);
			nls();
			setState(445);
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
			setState(447);
			className();
			setState(452);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(448);
				match(EXTENDS);
				setState(449);
				nls();
				setState(450);
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
			setState(454);
			type();
			setState(461);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(455);
					match(BITAND);
					setState(456);
					nls();
					setState(457);
					type();
					}
					} 
				}
				setState(463);
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
			setState(464);
			type();
			setState(471);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(465);
					match(COMMA);
					setState(466);
					nls();
					setState(467);
					type();
					}
					} 
				}
				setState(473);
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
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode EXTENDS() { return getToken(GroovyParser.EXTENDS, 0); }
		public TerminalNode IMPLEMENTS() { return getToken(GroovyParser.IMPLEMENTS, 0); }
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
			setState(485);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CLASS:
				{
				setState(474);
				match(CLASS);
				 _localctx.t =  0; 
				}
				break;
			case INTERFACE:
				{
				setState(476);
				match(INTERFACE);
				 _localctx.t =  1; 
				}
				break;
			case ENUM:
				{
				setState(478);
				match(ENUM);
				 _localctx.t =  2; 
				}
				break;
			case AT:
				{
				setState(480);
				match(AT);
				setState(481);
				match(INTERFACE);
				 _localctx.t =  3; 
				}
				break;
			case TRAIT:
				{
				setState(483);
				match(TRAIT);
				 _localctx.t =  4; 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(487);
			identifier();
			setState(491);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(488);
				nls();
				setState(489);
				typeParameters();
				}
				break;
			}
			setState(498);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				{
				setState(493);
				nls();
				setState(494);
				match(EXTENDS);
				setState(495);
				nls();
				setState(496);
				_localctx.scs = typeList();
				}
				break;
			}
			setState(505);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				{
				setState(500);
				nls();
				setState(501);
				match(IMPLEMENTS);
				setState(502);
				nls();
				setState(503);
				_localctx.is = typeList();
				}
				break;
			}
			setState(507);
			nls();
			setState(508);
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
			setState(510);
			match(LBRACE);
			setState(511);
			nls();
			setState(523);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				{
				setState(512);
				if (!( 2 == _localctx.t )) throw createFailedPredicateException(" 2 == $t ");
				setState(513);
				enumConstants();
				setState(517);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(514);
					nls();
					setState(515);
					match(COMMA);
					}
					break;
				}
				setState(520);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
				case 1:
					{
					setState(519);
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
			setState(534);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << CLASS) | (1L << DEFAULT) | (1L << ENUM) | (1L << FINAL) | (1L << IMPORT) | (1L << INTERFACE) | (1L << NATIVE) | (1L << PACKAGE) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << STATIC) | (1L << STRICTFP) | (1L << SYNCHRONIZED) | (1L << TRANSIENT) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 80)) & ~0x3f) == 0 && ((1L << (_la - 80)) & ((1L << (LBRACE - 80)) | (1L << (LBRACK - 80)) | (1L << (LT - 80)) | (1L << (QUESTION - 80)) | (1L << (CapitalizedIdentifier - 80)) | (1L << (Identifier - 80)) | (1L << (AT - 80)))) != 0)) {
				{
				setState(525);
				classBodyDeclaration(_localctx.t);
				setState(531);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(526);
						sep();
						setState(527);
						classBodyDeclaration(_localctx.t);
						}
						} 
					}
					setState(533);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				}
				}
			}

			setState(537);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(536);
				sep();
				}
			}

			setState(539);
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
			setState(541);
			enumConstant();
			setState(549);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(542);
					nls();
					setState(543);
					match(COMMA);
					setState(544);
					nls();
					setState(545);
					enumConstant();
					}
					} 
				}
				setState(551);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
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
			setState(552);
			annotationsOpt();
			setState(553);
			identifier();
			setState(555);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				{
				setState(554);
				arguments();
				}
				break;
			}
			setState(558);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				{
				setState(557);
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
			setState(566);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(562);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(560);
					match(STATIC);
					setState(561);
					nls();
					}
				}

				setState(564);
				block();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(565);
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
			setState(573);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(568);
				methodDeclaration(0, _localctx.t);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(569);
				fieldDeclaration();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(570);
				modifiersOpt();
				setState(571);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(575);
			modifiersOpt();
			setState(577);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(576);
				typeParameters();
				}
			}

			setState(580);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				{
				setState(579);
				returnType(_localctx.ct);
				}
				break;
			}
			setState(582);
			methodName();
			setState(583);
			formalParameters();
			setState(600);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				{
				setState(584);
				match(DEFAULT);
				setState(585);
				nls();
				setState(586);
				elementValue();
				}
				break;

			case 2:
				{
				setState(593);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
				case 1:
					{
					setState(588);
					nls();
					setState(589);
					match(THROWS);
					setState(590);
					nls();
					setState(591);
					qualifiedClassNameList();
					}
					break;
				}
				setState(598);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
				case 1:
					{
					setState(595);
					nls();
					setState(596);
					methodBody();
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
			setState(604);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(602);
				identifier();
				}
				break;
			case StringLiteral:
				enterOuterAlt(_localctx, 2);
				{
				setState(603);
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
		enterRule(_localctx, 54, RULE_returnType);
		try {
			setState(608);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(606);
				standardType();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(607);
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
			setState(610);
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
			setState(612);
			variableDeclarator();
			setState(619);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(613);
					match(COMMA);
					setState(614);
					nls();
					setState(615);
					variableDeclarator();
					}
					} 
				}
				setState(621);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
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
			setState(622);
			variableDeclaratorId();
			setState(628);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				{
				setState(623);
				nls();
				setState(624);
				match(ASSIGN);
				setState(625);
				nls();
				setState(626);
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
			setState(630);
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
			setState(632);
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
			setState(634);
			variableInitializer();
			setState(642);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(635);
					nls();
					setState(636);
					match(COMMA);
					setState(637);
					nls();
					setState(638);
					variableInitializer();
					}
					} 
				}
				setState(644);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
			}
			setState(645);
			nls();
			setState(647);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				{
				setState(646);
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
			setState(653); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(649);
					annotationsOpt();
					setState(650);
					match(LBRACK);
					setState(651);
					match(RBRACK);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(655); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
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
			setState(658);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				{
				setState(657);
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
			setState(660);
			annotationsOpt();
			setState(663);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BuiltInPrimitiveType:
				{
				setState(661);
				primitiveType();
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(662);
				standardClassOrInterfaceType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(665);
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
			setState(667);
			annotationsOpt();
			setState(673);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BuiltInPrimitiveType:
			case VOID:
				{
				setState(670);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case BuiltInPrimitiveType:
					{
					setState(668);
					primitiveType();
					}
					break;
				case VOID:
					{
					setState(669);
					match(VOID);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(672);
				generalClassOrInterfaceType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(675);
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
			setState(679);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				{
				setState(677);
				qualifiedClassName();
				}
				break;

			case 2:
				{
				setState(678);
				qualifiedStandardClassName();
				}
				break;
			}
			setState(682);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(681);
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
			setState(684);
			qualifiedClassName();
			setState(686);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				{
				setState(685);
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
			setState(688);
			qualifiedStandardClassName();
			setState(690);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(689);
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
			setState(692);
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
			setState(694);
			match(LT);
			setState(695);
			nls();
			setState(696);
			typeArgument();
			setState(703);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(697);
					match(COMMA);
					setState(698);
					nls();
					setState(699);
					typeArgument();
					}
					} 
				}
				setState(705);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
			}
			setState(706);
			nls();
			setState(707);
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
			setState(718);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(709);
				type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(710);
				annotationsOpt();
				setState(711);
				match(QUESTION);
				setState(716);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
				case 1:
					{
					setState(712);
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
					setState(713);
					nls();
					setState(714);
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
			setState(720);
			annotationsOpt();
			setState(721);
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
			setState(723);
			annotatedQualifiedClassName();
			setState(730);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(724);
					match(COMMA);
					setState(725);
					nls();
					setState(726);
					annotatedQualifiedClassName();
					}
					} 
				}
				setState(732);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(733);
			match(LPAREN);
			setState(735);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << FINAL) | (1L << IMPORT) | (1L << PACKAGE) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << STATIC) | (1L << STRICTFP) | (1L << VOID))) != 0) || ((((_la - 82)) & ~0x3f) == 0 && ((1L << (_la - 82)) & ((1L << (LBRACK - 82)) | (1L << (QUESTION - 82)) | (1L << (CapitalizedIdentifier - 82)) | (1L << (Identifier - 82)) | (1L << (AT - 82)) | (1L << (ELLIPSIS - 82)))) != 0)) {
				{
				setState(734);
				formalParameterList();
				}
			}

			setState(737);
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
			setState(741);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
			case 1:
				{
				setState(739);
				formalParameter();
				}
				break;

			case 2:
				{
				setState(740);
				thisFormalParameter();
				}
				break;
			}
			setState(749);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(743);
					match(COMMA);
					setState(744);
					nls();
					setState(745);
					formalParameter();
					}
					} 
				}
				setState(751);
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
			setState(752);
			type();
			setState(753);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(755);
			variableModifiersOpt();
			setState(757);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				{
				setState(756);
				type();
				}
				break;
			}
			setState(760);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELLIPSIS) {
				{
				setState(759);
				match(ELLIPSIS);
				}
			}

			setState(762);
			variableDeclaratorId();
			setState(768);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
			case 1:
				{
				setState(763);
				nls();
				setState(764);
				match(ASSIGN);
				setState(765);
				nls();
				setState(766);
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
			setState(770);
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
			setState(772);
			qualifiedNameElement();
			setState(777);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(773);
					match(DOT);
					setState(774);
					qualifiedNameElement();
					}
					} 
				}
				setState(779);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
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
			setState(785);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(780);
				identifier();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(781);
				match(DEF);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(782);
				match(IN);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(783);
				match(AS);
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(784);
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
			setState(792);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(787);
					qualifiedNameElement();
					setState(788);
					match(DOT);
					}
					} 
				}
				setState(794);
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
			setState(795);
			qualifiedNameElements();
			setState(796);
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
			setState(798);
			qualifiedNameElements();
			setState(799);
			className();
			setState(804);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(800);
					match(DOT);
					setState(801);
					className();
					}
					} 
				}
				setState(806);
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
			setState(812);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IntegerLiteral:
				_localctx = new IntegerLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(807);
				match(IntegerLiteral);
				}
				break;
			case FloatingPointLiteral:
				_localctx = new FloatingPointLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(808);
				match(FloatingPointLiteral);
				}
				break;
			case StringLiteral:
				_localctx = new StringLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(809);
				stringLiteral();
				}
				break;
			case BooleanLiteral:
				_localctx = new BooleanLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(810);
				match(BooleanLiteral);
				}
				break;
			case NullLiteral:
				_localctx = new NullLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(811);
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
			setState(814);
			match(GStringBegin);
			setState(815);
			gstringValue();
			setState(820);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPart) {
				{
				{
				setState(816);
				match(GStringPart);
				setState(817);
				gstringValue();
				}
				}
				setState(822);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(823);
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
		enterRule(_localctx, 116, RULE_gstringValue);
		try {
			setState(827);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(825);
				gstringPath();
				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 2);
				{
				setState(826);
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
		enterRule(_localctx, 118, RULE_gstringPath);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(829);
			identifier();
			setState(833);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPathPart) {
				{
				{
				setState(830);
				match(GStringPathPart);
				}
				}
				setState(835);
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
			setState(836);
			lambdaParameters();
			setState(837);
			nls();
			setState(838);
			match(ARROW);
			setState(839);
			nls();
			setState(840);
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
			setState(842);
			standardLambdaParameters();
			setState(843);
			nls();
			setState(844);
			match(ARROW);
			setState(845);
			nls();
			setState(846);
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
			setState(848);
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
			setState(852);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(850);
				formalParameters();
				}
				break;
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 2);
				{
				setState(851);
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
		enterRule(_localctx, 128, RULE_lambdaBody);
		try {
			setState(856);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(854);
				block();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(855);
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
		enterRule(_localctx, 130, RULE_closure);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(858);
			match(LBRACE);
			setState(867);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				{
				setState(859);
				nls();
				setState(863);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << FINAL) | (1L << IMPORT) | (1L << PACKAGE) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << STATIC) | (1L << STRICTFP) | (1L << VOID))) != 0) || ((((_la - 82)) & ~0x3f) == 0 && ((1L << (_la - 82)) & ((1L << (LBRACK - 82)) | (1L << (QUESTION - 82)) | (1L << (CapitalizedIdentifier - 82)) | (1L << (Identifier - 82)) | (1L << (AT - 82)) | (1L << (ELLIPSIS - 82)))) != 0)) {
					{
					setState(860);
					formalParameterList();
					setState(861);
					nls();
					}
				}

				setState(865);
				match(ARROW);
				}
				break;
			}
			setState(870);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
			case 1:
				{
				setState(869);
				sep();
				}
				break;
			}
			setState(872);
			blockStatementsOpt();
			setState(873);
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
			setState(877);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(875);
				closure();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(876);
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
			setState(880);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				{
				setState(879);
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
			setState(882);
			blockStatement();
			setState(888);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(883);
					sep();
					setState(884);
					blockStatement();
					}
					} 
				}
				setState(890);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			}
			setState(892);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
			case 1:
				{
				setState(891);
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
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(905);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT) {
				{
				setState(894);
				annotation();
				setState(900);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(895);
						nls();
						setState(896);
						annotation();
						}
						} 
					}
					setState(902);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
				}
				setState(903);
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
			setState(907);
			match(AT);
			setState(908);
			annotationName();
			setState(916);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
			case 1:
				{
				setState(909);
				nls();
				setState(910);
				match(LPAREN);
				setState(912);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
				case 1:
					{
					setState(911);
					elementValues();
					}
					break;
				}
				setState(914);
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
			setState(920);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(918);
				elementValuePairs();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(919);
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
			setState(922);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(924);
			elementValuePair();
			setState(929);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(925);
				match(COMMA);
				setState(926);
				elementValuePair();
				}
				}
				setState(931);
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
		enterRule(_localctx, 148, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(932);
			elementValuePairName();
			setState(933);
			nls();
			setState(934);
			match(ASSIGN);
			setState(935);
			nls();
			setState(936);
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
			setState(940);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(938);
				identifier();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(939);
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
			setState(945);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(942);
				elementValueArrayInitializer();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(943);
				annotation();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(944);
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
			setState(947);
			match(LBRACK);
			setState(959);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
			case 1:
				{
				setState(948);
				elementValue();
				setState(953);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(949);
						match(COMMA);
						setState(950);
						elementValue();
						}
						} 
					}
					setState(955);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
				}
				setState(957);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(956);
					match(COMMA);
					}
				}

				}
				break;
			}
			setState(961);
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
			setState(963);
			match(LBRACE);
			setState(965);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,96,_ctx) ) {
			case 1:
				{
				setState(964);
				sep();
				}
				break;
			}
			setState(967);
			blockStatementsOpt();
			setState(968);
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
			setState(972);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(970);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(971);
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
			setState(974);
			if (!( !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ");
			setState(975);
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
		enterRule(_localctx, 162, RULE_variableDeclaration);
		try {
			setState(994);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(977);
				modifiers();
				setState(978);
				nls();
				setState(989);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case AS:
				case DEF:
				case IN:
				case TRAIT:
				case VAR:
				case BuiltInPrimitiveType:
				case IMPORT:
				case PACKAGE:
				case VOID:
				case LBRACK:
				case QUESTION:
				case CapitalizedIdentifier:
				case Identifier:
				case AT:
					{
					setState(980);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
					case 1:
						{
						setState(979);
						type();
						}
						break;
					}
					setState(982);
					variableDeclarators();
					}
					break;
				case LPAREN:
					{
					setState(983);
					typeNamePairs();
					setState(984);
					nls();
					setState(985);
					match(ASSIGN);
					setState(986);
					nls();
					setState(987);
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
				setState(991);
				type();
				setState(992);
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
		enterRule(_localctx, 164, RULE_typeNamePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(996);
			match(LPAREN);
			setState(997);
			typeNamePair();
			setState(1002);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(998);
				match(COMMA);
				setState(999);
				typeNamePair();
				}
				}
				setState(1004);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1005);
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
		enterRule(_localctx, 166, RULE_typeNamePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1008);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
			case 1:
				{
				setState(1007);
				type();
				}
				break;
			}
			setState(1010);
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
		enterRule(_localctx, 168, RULE_variableNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1012);
			match(LPAREN);
			setState(1013);
			variableDeclaratorId();
			setState(1016); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1014);
				match(COMMA);
				setState(1015);
				variableDeclaratorId();
				}
				}
				setState(1018); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==COMMA );
			setState(1020);
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
		enterRule(_localctx, 170, RULE_conditionalStatement);
		try {
			setState(1024);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IF:
				enterOuterAlt(_localctx, 1);
				{
				setState(1022);
				ifElseStatement();
				}
				break;
			case SWITCH:
				enterOuterAlt(_localctx, 2);
				{
				setState(1023);
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
		enterRule(_localctx, 172, RULE_ifElseStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1026);
			match(IF);
			setState(1027);
			expressionInPar();
			setState(1028);
			nls();
			setState(1029);
			_localctx.tb = statement();
			setState(1038);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,106,_ctx) ) {
			case 1:
				{
				setState(1032);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
				case 1:
					{
					setState(1030);
					nls();
					}
					break;

				case 2:
					{
					setState(1031);
					sep();
					}
					break;
				}
				setState(1034);
				match(ELSE);
				setState(1035);
				nls();
				setState(1036);
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
		enterRule(_localctx, 174, RULE_switchStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1040);
			match(SWITCH);
			setState(1041);
			expressionInPar();
			setState(1042);
			nls();
			setState(1043);
			match(LBRACE);
			setState(1044);
			nls();
			setState(1052);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CASE || _la==DEFAULT) {
				{
				setState(1046); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1045);
						switchBlockStatementGroup();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1048); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1050);
				nls();
				}
			}

			setState(1054);
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
		enterRule(_localctx, 176, RULE_loopStatement);
		try {
			setState(1075);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FOR:
				_localctx = new ForStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1056);
				match(FOR);
				setState(1057);
				match(LPAREN);
				setState(1058);
				forControl();
				setState(1059);
				rparen();
				setState(1060);
				nls();
				setState(1061);
				statement();
				}
				break;
			case WHILE:
				_localctx = new WhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1063);
				match(WHILE);
				setState(1064);
				expressionInPar();
				setState(1065);
				nls();
				setState(1066);
				statement();
				}
				break;
			case DO:
				_localctx = new DoWhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1068);
				match(DO);
				setState(1069);
				nls();
				setState(1070);
				statement();
				setState(1071);
				nls();
				setState(1072);
				match(WHILE);
				setState(1073);
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
		enterRule(_localctx, 178, RULE_continueStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1077);
			match(CONTINUE);
			setState(1079);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,110,_ctx) ) {
			case 1:
				{
				setState(1078);
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
		enterRule(_localctx, 180, RULE_breakStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1081);
			match(BREAK);
			setState(1083);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
			case 1:
				{
				setState(1082);
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
		enterRule(_localctx, 182, RULE_tryCatchStatement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1085);
			match(TRY);
			setState(1087);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,112,_ctx) ) {
			case 1:
				{
				setState(1086);
				resources();
				}
				break;
			}
			setState(1089);
			nls();
			setState(1090);
			block();
			setState(1096);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,113,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1091);
					nls();
					setState(1092);
					catchClause();
					}
					} 
				}
				setState(1098);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,113,_ctx);
			}
			setState(1102);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
			case 1:
				{
				setState(1099);
				nls();
				setState(1100);
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
			setState(1104);
			match(ASSERT);
			setState(1105);
			_localctx.ce = expression(0);
			setState(1111);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				{
				setState(1106);
				nls();
				setState(1107);
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
				setState(1108);
				nls();
				setState(1109);
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
			setState(1139);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
			case 1:
				_localctx = new BlockStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1113);
				block();
				}
				break;

			case 2:
				_localctx = new ConditionalStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1114);
				conditionalStatement();
				}
				break;

			case 3:
				_localctx = new LoopStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1115);
				loopStatement();
				}
				break;

			case 4:
				_localctx = new TryCatchStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1116);
				tryCatchStatement();
				}
				break;

			case 5:
				_localctx = new SynchronizedStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1117);
				match(SYNCHRONIZED);
				setState(1118);
				expressionInPar();
				setState(1119);
				nls();
				setState(1120);
				block();
				}
				break;

			case 6:
				_localctx = new ReturnStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1122);
				match(RETURN);
				setState(1124);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,116,_ctx) ) {
				case 1:
					{
					setState(1123);
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
				setState(1126);
				match(THROW);
				setState(1127);
				expression(0);
				}
				break;

			case 8:
				_localctx = new BreakStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1128);
				breakStatement();
				}
				break;

			case 9:
				_localctx = new ContinueStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1129);
				continueStatement();
				}
				break;

			case 10:
				_localctx = new LabeledStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1130);
				identifier();
				setState(1131);
				match(COLON);
				setState(1132);
				nls();
				setState(1133);
				statement();
				}
				break;

			case 11:
				_localctx = new AssertStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1135);
				assertStatement();
				}
				break;

			case 12:
				_localctx = new LocalVariableDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(1136);
				localVariableDeclaration();
				}
				break;

			case 13:
				_localctx = new ExpressionStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(1137);
				statementExpression();
				}
				break;

			case 14:
				_localctx = new EmptyStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(1138);
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
		enterRule(_localctx, 188, RULE_catchClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1141);
			match(CATCH);
			setState(1142);
			match(LPAREN);
			setState(1143);
			variableModifiersOpt();
			setState(1145);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
			case 1:
				{
				setState(1144);
				catchType();
				}
				break;
			}
			setState(1147);
			identifier();
			setState(1148);
			rparen();
			setState(1149);
			nls();
			setState(1150);
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
			setState(1152);
			qualifiedClassName();
			setState(1157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITOR) {
				{
				{
				setState(1153);
				match(BITOR);
				setState(1154);
				qualifiedClassName();
				}
				}
				setState(1159);
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
			setState(1160);
			match(FINALLY);
			setState(1161);
			nls();
			setState(1162);
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
		enterRule(_localctx, 194, RULE_resources);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1164);
			match(LPAREN);
			setState(1165);
			nls();
			setState(1166);
			resourceList();
			setState(1168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(1167);
				sep();
				}
			}

			setState(1170);
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
		enterRule(_localctx, 196, RULE_resourceList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1172);
			resource();
			setState(1178);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,121,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1173);
					sep();
					setState(1174);
					resource();
					}
					} 
				}
				setState(1180);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,121,_ctx);
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
			setState(1183);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,122,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1181);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1182);
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
			setState(1185);
			switchLabel();
			setState(1191);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1186);
					nls();
					setState(1187);
					switchLabel();
					}
					} 
				}
				setState(1193);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			}
			setState(1194);
			nls();
			setState(1195);
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
			setState(1203);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1197);
				match(CASE);
				setState(1198);
				expression(0);
				setState(1199);
				match(COLON);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1201);
				match(DEFAULT);
				setState(1202);
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
		enterRule(_localctx, 204, RULE_forControl);
		try {
			setState(1207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1205);
				enhancedForControl();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1206);
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
		enterRule(_localctx, 206, RULE_enhancedForControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1209);
			variableModifiersOpt();
			setState(1211);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
			case 1:
				{
				setState(1210);
				type();
				}
				break;
			}
			setState(1213);
			variableDeclaratorId();
			setState(1214);
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
			setState(1215);
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
		enterRule(_localctx, 208, RULE_classicalForControl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1218);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,127,_ctx) ) {
			case 1:
				{
				setState(1217);
				forInit();
				}
				break;
			}
			setState(1220);
			match(SEMI);
			setState(1222);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
			case 1:
				{
				setState(1221);
				expression(0);
				}
				break;
			}
			setState(1224);
			match(SEMI);
			setState(1226);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,129,_ctx) ) {
			case 1:
				{
				setState(1225);
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
		enterRule(_localctx, 210, RULE_forInit);
		try {
			setState(1230);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1228);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1229);
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
		enterRule(_localctx, 212, RULE_forUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1232);
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
		enterRule(_localctx, 214, RULE_castParExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1234);
			match(LPAREN);
			setState(1235);
			type();
			setState(1236);
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
		enterRule(_localctx, 216, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1238);
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
		enterRule(_localctx, 218, RULE_expressionInPar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1240);
			match(LPAREN);
			setState(1241);
			enhancedStatementExpression();
			setState(1242);
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
		enterRule(_localctx, 220, RULE_expressionList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1244);
			expressionListElement(_localctx.canSpread);
			setState(1249);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,131,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1245);
					match(COMMA);
					setState(1246);
					expressionListElement(_localctx.canSpread);
					}
					} 
				}
				setState(1251);
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
		enterRule(_localctx, 222, RULE_expressionListElement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1253);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				{
				setState(1252);
				match(MUL);
				}
				break;
			}
			setState(1255);
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
		enterRule(_localctx, 224, RULE_enhancedStatementExpression);
		try {
			setState(1259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,133,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1257);
				statementExpression();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1258);
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
		enterRule(_localctx, 226, RULE_statementExpression);
		try {
			_localctx = new CommandExprAltContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1261);
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
		enterRule(_localctx, 228, RULE_postfixExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1263);
			pathExpression();
			setState(1265);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,134,_ctx) ) {
			case 1:
				{
				setState(1264);
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
		int _startState = 230;
		enterRecursionRule(_localctx, 230, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1284);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,135,_ctx) ) {
			case 1:
				{
				_localctx = new CastExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1268);
				castParExpression();
				setState(1269);
				castOperandExpression();
				}
				break;

			case 2:
				{
				_localctx = new PostfixExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1271);
				postfixExpression();
				}
				break;

			case 3:
				{
				_localctx = new UnaryNotExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1272);
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
				setState(1273);
				nls();
				setState(1274);
				expression(18);
				}
				break;

			case 4:
				{
				_localctx = new UnaryAddExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1276);
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
				setState(1277);
				expression(16);
				}
				break;

			case 5:
				{
				_localctx = new MultipleAssignmentExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1278);
				((MultipleAssignmentExprAltContext)_localctx).left = variableNames();
				setState(1279);
				nls();
				setState(1280);
				((MultipleAssignmentExprAltContext)_localctx).op = match(ASSIGN);
				setState(1281);
				nls();
				setState(1282);
				((MultipleAssignmentExprAltContext)_localctx).right = statementExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1396);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,140,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1394);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,139,_ctx) ) {
					case 1:
						{
						_localctx = new PowerExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((PowerExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1286);
						if (!(precpred(_ctx, 17))) throw createFailedPredicateException("precpred(_ctx, 17)");
						setState(1287);
						((PowerExprAltContext)_localctx).op = match(POWER);
						setState(1288);
						nls();
						setState(1289);
						((PowerExprAltContext)_localctx).right = expression(18);
						}
						break;

					case 2:
						{
						_localctx = new MultiplicativeExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((MultiplicativeExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1291);
						if (!(precpred(_ctx, 15))) throw createFailedPredicateException("precpred(_ctx, 15)");
						setState(1292);
						nls();
						setState(1293);
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
						setState(1294);
						nls();
						setState(1295);
						((MultiplicativeExprAltContext)_localctx).right = expression(16);
						}
						break;

					case 3:
						{
						_localctx = new AdditiveExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AdditiveExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1297);
						if (!(precpred(_ctx, 14))) throw createFailedPredicateException("precpred(_ctx, 14)");
						setState(1298);
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
						setState(1299);
						nls();
						setState(1300);
						((AdditiveExprAltContext)_localctx).right = expression(15);
						}
						break;

					case 4:
						{
						_localctx = new ShiftExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ShiftExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1302);
						if (!(precpred(_ctx, 13))) throw createFailedPredicateException("precpred(_ctx, 13)");
						setState(1303);
						nls();
						setState(1314);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case GT:
						case LT:
							{
							setState(1311);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,136,_ctx) ) {
							case 1:
								{
								setState(1304);
								((ShiftExprAltContext)_localctx).dlOp = match(LT);
								setState(1305);
								match(LT);
								}
								break;

							case 2:
								{
								setState(1306);
								((ShiftExprAltContext)_localctx).tgOp = match(GT);
								setState(1307);
								match(GT);
								setState(1308);
								match(GT);
								}
								break;

							case 3:
								{
								setState(1309);
								((ShiftExprAltContext)_localctx).dgOp = match(GT);
								setState(1310);
								match(GT);
								}
								break;
							}
							}
							break;
						case RANGE_INCLUSIVE:
						case RANGE_EXCLUSIVE:
							{
							setState(1313);
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
						setState(1316);
						nls();
						setState(1317);
						((ShiftExprAltContext)_localctx).right = expression(14);
						}
						break;

					case 5:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1319);
						if (!(precpred(_ctx, 11))) throw createFailedPredicateException("precpred(_ctx, 11)");
						setState(1320);
						nls();
						setState(1321);
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
						setState(1322);
						nls();
						setState(1323);
						((RelationalExprAltContext)_localctx).right = expression(12);
						}
						break;

					case 6:
						{
						_localctx = new EqualityExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((EqualityExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1325);
						if (!(precpred(_ctx, 10))) throw createFailedPredicateException("precpred(_ctx, 10)");
						setState(1326);
						nls();
						setState(1327);
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
						setState(1328);
						nls();
						setState(1329);
						((EqualityExprAltContext)_localctx).right = expression(11);
						}
						break;

					case 7:
						{
						_localctx = new RegexExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RegexExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1331);
						if (!(precpred(_ctx, 9))) throw createFailedPredicateException("precpred(_ctx, 9)");
						setState(1332);
						nls();
						setState(1333);
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
						setState(1334);
						nls();
						setState(1335);
						((RegexExprAltContext)_localctx).right = expression(10);
						}
						break;

					case 8:
						{
						_localctx = new AndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1337);
						if (!(precpred(_ctx, 8))) throw createFailedPredicateException("precpred(_ctx, 8)");
						setState(1338);
						nls();
						setState(1339);
						((AndExprAltContext)_localctx).op = match(BITAND);
						setState(1340);
						nls();
						setState(1341);
						((AndExprAltContext)_localctx).right = expression(9);
						}
						break;

					case 9:
						{
						_localctx = new ExclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ExclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1343);
						if (!(precpred(_ctx, 7))) throw createFailedPredicateException("precpred(_ctx, 7)");
						setState(1344);
						nls();
						setState(1345);
						((ExclusiveOrExprAltContext)_localctx).op = match(XOR);
						setState(1346);
						nls();
						setState(1347);
						((ExclusiveOrExprAltContext)_localctx).right = expression(8);
						}
						break;

					case 10:
						{
						_localctx = new InclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((InclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1349);
						if (!(precpred(_ctx, 6))) throw createFailedPredicateException("precpred(_ctx, 6)");
						setState(1350);
						nls();
						setState(1351);
						((InclusiveOrExprAltContext)_localctx).op = match(BITOR);
						setState(1352);
						nls();
						setState(1353);
						((InclusiveOrExprAltContext)_localctx).right = expression(7);
						}
						break;

					case 11:
						{
						_localctx = new LogicalAndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalAndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1355);
						if (!(precpred(_ctx, 5))) throw createFailedPredicateException("precpred(_ctx, 5)");
						setState(1356);
						nls();
						setState(1357);
						((LogicalAndExprAltContext)_localctx).op = match(AND);
						setState(1358);
						nls();
						setState(1359);
						((LogicalAndExprAltContext)_localctx).right = expression(6);
						}
						break;

					case 12:
						{
						_localctx = new LogicalOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1361);
						if (!(precpred(_ctx, 4))) throw createFailedPredicateException("precpred(_ctx, 4)");
						setState(1362);
						nls();
						setState(1363);
						((LogicalOrExprAltContext)_localctx).op = match(OR);
						setState(1364);
						nls();
						setState(1365);
						((LogicalOrExprAltContext)_localctx).right = expression(5);
						}
						break;

					case 13:
						{
						_localctx = new ConditionalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ConditionalExprAltContext)_localctx).con = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1367);
						if (!(precpred(_ctx, 3))) throw createFailedPredicateException("precpred(_ctx, 3)");
						setState(1368);
						nls();
						setState(1378);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case QUESTION:
							{
							setState(1369);
							match(QUESTION);
							setState(1370);
							nls();
							setState(1371);
							((ConditionalExprAltContext)_localctx).tb = expression(0);
							setState(1372);
							nls();
							setState(1373);
							match(COLON);
							setState(1374);
							nls();
							}
							break;
						case ELVIS:
							{
							setState(1376);
							match(ELVIS);
							setState(1377);
							nls();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1380);
						((ConditionalExprAltContext)_localctx).fb = expression(3);
						}
						break;

					case 14:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1382);
						if (!(precpred(_ctx, 12))) throw createFailedPredicateException("precpred(_ctx, 12)");
						setState(1383);
						nls();
						setState(1384);
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
						setState(1385);
						nls();
						setState(1386);
						type();
						}
						break;

					case 15:
						{
						_localctx = new AssignmentExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AssignmentExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1388);
						if (!(precpred(_ctx, 1))) throw createFailedPredicateException("precpred(_ctx, 1)");
						setState(1389);
						nls();
						setState(1390);
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
						setState(1391);
						nls();
						setState(1392);
						enhancedStatementExpression();
						}
						break;
					}
					} 
				}
				setState(1398);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,140,_ctx);
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
		enterRule(_localctx, 232, RULE_castOperandExpression);
		int _la;
		try {
			setState(1409);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
			case 1:
				_localctx = new CastExprAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1399);
				castParExpression();
				setState(1400);
				castOperandExpression();
				}
				break;

			case 2:
				_localctx = new PostfixExprAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1402);
				postfixExpression();
				}
				break;

			case 3:
				_localctx = new UnaryNotExprAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1403);
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
				setState(1404);
				nls();
				setState(1405);
				castOperandExpression();
				}
				break;

			case 4:
				_localctx = new UnaryAddExprAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1407);
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
				setState(1408);
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
		enterRule(_localctx, 234, RULE_commandExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1411);
			_localctx.expression = expression(0);
			setState(1415);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,142,_ctx) ) {
			case 1:
				{
				setState(1412);
				if (!( !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) )) throw createFailedPredicateException(" !SemanticPredicates.isFollowingArgumentsOrClosure($expression.ctx) ");
				setState(1413);
				argumentList();
				}
				break;

			case 2:
				{
				}
				break;
			}
			setState(1420);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,143,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1417);
					commandArgument();
					}
					} 
				}
				setState(1422);
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
		enterRule(_localctx, 236, RULE_commandArgument);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1423);
			commandPrimary();
			setState(1430);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
			case 1:
				{
				setState(1425); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1424);
						pathElement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1427); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;

			case 2:
				{
				setState(1429);
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
		enterRule(_localctx, 238, RULE_pathExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1435);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,146,_ctx) ) {
			case 1:
				{
				setState(1432);
				primary();
				}
				break;

			case 2:
				{
				setState(1433);
				if (!( _input.LT(2).getType() == DOT )) throw createFailedPredicateException(" _input.LT(2).getType() == DOT ");
				setState(1434);
				match(STATIC);
				}
				break;
			}
			setState(1442);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1437);
					_localctx.pathElement = pathElement();
					 _localctx.t =  _localctx.pathElement.t; 
					}
					} 
				}
				setState(1444);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
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
		enterRule(_localctx, 240, RULE_pathElement);
		int _la;
		try {
			setState(1481);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1445);
				nls();
				setState(1470);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,150,_ctx) ) {
				case 1:
					{
					setState(1456);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case SPREAD_DOT:
					case SAFE_DOT:
					case SAFE_CHAIN_DOT:
					case DOT:
						{
						setState(1446);
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
						setState(1447);
						nls();
						setState(1450);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case AT:
							{
							setState(1448);
							match(AT);
							}
							break;
						case LT:
							{
							setState(1449);
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
						case VAR:
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
						case NATIVE:
						case NEW:
						case PACKAGE:
						case PRIVATE:
						case PROTECTED:
						case PUBLIC:
						case RETURN:
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
						case VOID:
						case VOLATILE:
						case WHILE:
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
						setState(1452);
						match(METHOD_POINTER);
						setState(1453);
						nls();
						}
						break;
					case METHOD_REFERENCE:
						{
						setState(1454);
						match(METHOD_REFERENCE);
						setState(1455);
						nls();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1458);
					namePart();
					 _localctx.t =  1; 
					}
					break;

				case 2:
					{
					setState(1461);
					match(DOT);
					setState(1462);
					nls();
					setState(1463);
					match(NEW);
					setState(1464);
					creator(1);
					 _localctx.t =  6; 
					}
					break;

				case 3:
					{
					setState(1467);
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
				setState(1472);
				arguments();
				 _localctx.t =  2; 
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1475);
				indexPropertyArgs();
				 _localctx.t =  4; 
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1478);
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
		enterRule(_localctx, 242, RULE_namePart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1487);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,152,_ctx) ) {
			case 1:
				{
				setState(1483);
				identifier();
				}
				break;

			case 2:
				{
				setState(1484);
				stringLiteral();
				}
				break;

			case 3:
				{
				setState(1485);
				dynamicMemberName();
				}
				break;

			case 4:
				{
				setState(1486);
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
		enterRule(_localctx, 244, RULE_dynamicMemberName);
		try {
			setState(1491);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1489);
				parExpression();
				}
				break;
			case GStringBegin:
				enterOuterAlt(_localctx, 2);
				{
				setState(1490);
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
		enterRule(_localctx, 246, RULE_indexPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1494);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==QUESTION) {
				{
				setState(1493);
				match(QUESTION);
				}
			}

			setState(1496);
			match(LBRACK);
			setState(1498);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				{
				setState(1497);
				expressionList(true);
				}
				break;
			}
			setState(1500);
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
		enterRule(_localctx, 248, RULE_namedPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1503);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==QUESTION) {
				{
				setState(1502);
				match(QUESTION);
				}
			}

			setState(1505);
			match(LBRACK);
			setState(1508);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case VAR:
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
			case NATIVE:
			case NEW:
			case PACKAGE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RETURN:
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
			case VOID:
			case VOLATILE:
			case WHILE:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
			case LPAREN:
			case MUL:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(1506);
				namedPropertyArgList();
				}
				break;
			case COLON:
				{
				setState(1507);
				match(COLON);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1510);
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
		enterRule(_localctx, 250, RULE_primary);
		try {
			setState(1529);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1512);
				identifier();
				setState(1514);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
				case 1:
					{
					setState(1513);
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
				setState(1516);
				literal();
				}
				break;

			case 3:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1517);
				gstring();
				}
				break;

			case 4:
				_localctx = new NewPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1518);
				match(NEW);
				setState(1519);
				nls();
				setState(1520);
				creator(0);
				}
				break;

			case 5:
				_localctx = new ThisPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1522);
				match(THIS);
				}
				break;

			case 6:
				_localctx = new SuperPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1523);
				match(SUPER);
				}
				break;

			case 7:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1524);
				parExpression();
				}
				break;

			case 8:
				_localctx = new ClosureOrLambdaExpressionPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1525);
				closureOrLambdaExpression();
				}
				break;

			case 9:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1526);
				list();
				}
				break;

			case 10:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1527);
				map();
				}
				break;

			case 11:
				_localctx = new BuiltInTypePrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1528);
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
		enterRule(_localctx, 252, RULE_namedPropertyArgPrimary);
		try {
			setState(1535);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1531);
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
				setState(1532);
				literal();
				}
				break;
			case GStringBegin:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1533);
				gstring();
				}
				break;
			case LPAREN:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1534);
				parExpression();
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
	public final PrimaryContext namedArgPrimary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_namedArgPrimary);
		try {
			setState(1540);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1537);
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
				setState(1538);
				literal();
				}
				break;
			case GStringBegin:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1539);
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
		enterRule(_localctx, 256, RULE_commandPrimary);
		try {
			setState(1545);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1542);
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
				setState(1543);
				literal();
				}
				break;
			case GStringBegin:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1544);
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
		enterRule(_localctx, 258, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1547);
			match(LBRACK);
			setState(1549);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,163,_ctx) ) {
			case 1:
				{
				setState(1548);
				expressionList(true);
				}
				break;
			}
			setState(1552);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1551);
				match(COMMA);
				}
			}

			setState(1554);
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
		enterRule(_localctx, 260, RULE_map);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1556);
			match(LBRACK);
			setState(1562);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case VAR:
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
			case NATIVE:
			case NEW:
			case PACKAGE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RETURN:
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
			case VOID:
			case VOLATILE:
			case WHILE:
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
				setState(1557);
				mapEntryList();
				setState(1559);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1558);
					match(COMMA);
					}
				}

				}
				break;
			case COLON:
				{
				setState(1561);
				match(COLON);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1564);
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
		enterRule(_localctx, 262, RULE_mapEntryList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1566);
			mapEntry();
			setState(1571);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,167,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1567);
					match(COMMA);
					setState(1568);
					mapEntry();
					}
					} 
				}
				setState(1573);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,167,_ctx);
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
		enterRule(_localctx, 264, RULE_namedPropertyArgList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1574);
			namedPropertyArg();
			setState(1579);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1575);
				match(COMMA);
				setState(1576);
				namedPropertyArg();
				}
				}
				setState(1581);
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
		enterRule(_localctx, 266, RULE_mapEntry);
		try {
			setState(1592);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case VAR:
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
			case NATIVE:
			case NEW:
			case PACKAGE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RETURN:
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
			case VOID:
			case VOLATILE:
			case WHILE:
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
				setState(1582);
				mapEntryLabel();
				setState(1583);
				match(COLON);
				setState(1584);
				nls();
				setState(1585);
				expression(0);
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1587);
				match(MUL);
				setState(1588);
				match(COLON);
				setState(1589);
				nls();
				setState(1590);
				expression(0);
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
		enterRule(_localctx, 268, RULE_namedPropertyArg);
		try {
			setState(1604);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case VAR:
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
			case NATIVE:
			case NEW:
			case PACKAGE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RETURN:
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
			case VOID:
			case VOLATILE:
			case WHILE:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
			case LPAREN:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(1594);
				namedPropertyArgLabel();
				setState(1595);
				match(COLON);
				setState(1596);
				nls();
				setState(1597);
				expression(0);
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1599);
				match(MUL);
				setState(1600);
				match(COLON);
				setState(1601);
				nls();
				setState(1602);
				expression(0);
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
		enterRule(_localctx, 270, RULE_namedArg);
		try {
			setState(1616);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
			case GStringBegin:
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case THREADSAFE:
			case VAR:
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
			case NATIVE:
			case NEW:
			case PACKAGE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case RETURN:
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
			case VOID:
			case VOLATILE:
			case WHILE:
			case IntegerLiteral:
			case FloatingPointLiteral:
			case BooleanLiteral:
			case NullLiteral:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(1606);
				namedArgLabel();
				setState(1607);
				match(COLON);
				setState(1608);
				nls();
				setState(1609);
				expression(0);
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1611);
				match(MUL);
				setState(1612);
				match(COLON);
				setState(1613);
				nls();
				setState(1614);
				expression(0);
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
		enterRule(_localctx, 272, RULE_mapEntryLabel);
		try {
			setState(1620);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,172,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1618);
				keywords();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1619);
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
		enterRule(_localctx, 274, RULE_namedPropertyArgLabel);
		try {
			setState(1624);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,173,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1622);
				keywords();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1623);
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
		enterRule(_localctx, 276, RULE_namedArgLabel);
		try {
			setState(1628);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1626);
				keywords();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1627);
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
		enterRule(_localctx, 278, RULE_creator);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1630);
			createdName();
			setState(1646);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
			case 1:
				{
				setState(1631);
				nls();
				setState(1632);
				arguments();
				setState(1634);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,175,_ctx) ) {
				case 1:
					{
					setState(1633);
					anonymousInnerClassDeclaration(0);
					}
					break;
				}
				}
				break;

			case 2:
				{
				setState(1637); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1636);
						dim();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1639); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,176,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1644);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
				case 1:
					{
					setState(1641);
					nls();
					setState(1642);
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
		enterRule(_localctx, 280, RULE_dim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1648);
			annotationsOpt();
			setState(1649);
			match(LBRACK);
			setState(1651);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,179,_ctx) ) {
			case 1:
				{
				setState(1650);
				expression(0);
				}
				break;
			}
			setState(1653);
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
		enterRule(_localctx, 282, RULE_arrayInitializer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1655);
			match(LBRACE);
			setState(1656);
			nls();
			setState(1660);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
			case 1:
				{
				setState(1657);
				variableInitializers();
				setState(1658);
				nls();
				}
				break;
			}
			setState(1662);
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
		enterRule(_localctx, 284, RULE_anonymousInnerClassDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1664);
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
		enterRule(_localctx, 286, RULE_createdName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1666);
			annotationsOpt();
			setState(1672);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BuiltInPrimitiveType:
				{
				setState(1667);
				primitiveType();
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case VAR:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(1668);
				qualifiedClassName();
				setState(1670);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
				case 1:
					{
					setState(1669);
					typeArgumentsOrDiamond();
					}
					break;
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
		enterRule(_localctx, 288, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1674);
			match(LT);
			setState(1675);
			nls();
			setState(1676);
			typeList();
			setState(1677);
			nls();
			setState(1678);
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
		enterRule(_localctx, 290, RULE_typeArgumentsOrDiamond);
		try {
			setState(1683);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1680);
				match(LT);
				setState(1681);
				match(GT);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1682);
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
		enterRule(_localctx, 292, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1685);
			match(LPAREN);
			setState(1687);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,184,_ctx) ) {
			case 1:
				{
				setState(1686);
				enhancedArgumentListInPar();
				}
				break;
			}
			setState(1690);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1689);
				match(COMMA);
				}
			}

			setState(1692);
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
	public final EnhancedArgumentListInParContext argumentList() throws RecognitionException {
		EnhancedArgumentListInParContext _localctx = new EnhancedArgumentListInParContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_argumentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1694);
			firstArgumentListElement();
			setState(1701);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1695);
					match(COMMA);
					setState(1696);
					nls();
					setState(1697);
					argumentListElement();
					}
					} 
				}
				setState(1703);
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


	@RuleVersion(0)
	public final EnhancedArgumentListInParContext enhancedArgumentList() throws RecognitionException {
		EnhancedArgumentListInParContext _localctx = new EnhancedArgumentListInParContext(_ctx, getState());
		enterRule(_localctx, 296, RULE_enhancedArgumentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1704);
			firstEnhancedArgumentListElement();
			setState(1711);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1705);
				match(COMMA);
				setState(1706);
				nls();
				setState(1707);
				enhancedArgumentListElement();
				}
				}
				setState(1713);
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
		enterRule(_localctx, 298, RULE_enhancedArgumentListInPar);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1714);
			enhancedArgumentListElement();
			setState(1721);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,188,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1715);
					match(COMMA);
					setState(1716);
					nls();
					setState(1717);
					enhancedArgumentListElement();
					}
					} 
				}
				setState(1723);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,188,_ctx);
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
		enterRule(_localctx, 300, RULE_firstArgumentListElement);
		try {
			setState(1726);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,189,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1724);
				expressionListElement(true);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1725);
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
		enterRule(_localctx, 302, RULE_argumentListElement);
		try {
			setState(1730);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,190,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1728);
				expressionListElement(true);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1729);
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


	@RuleVersion(0)
	public final EnhancedArgumentListElementContext firstEnhancedArgumentListElement() throws RecognitionException {
		EnhancedArgumentListElementContext _localctx = new EnhancedArgumentListElementContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_firstEnhancedArgumentListElement);
		try {
			setState(1735);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,191,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1732);
				expressionListElement(true);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1733);
				standardLambdaExpression();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1734);
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
		enterRule(_localctx, 306, RULE_enhancedArgumentListElement);
		try {
			setState(1740);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1737);
				expressionListElement(true);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1738);
				standardLambdaExpression();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1739);
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
		enterRule(_localctx, 308, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1742);
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
		enterRule(_localctx, 310, RULE_className);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1744);
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
		enterRule(_localctx, 312, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1746);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << IN) | (1L << TRAIT) | (1L << VAR))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) ) {
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
		enterRule(_localctx, 314, RULE_builtInType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1748);
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
		enterRule(_localctx, 316, RULE_keywords);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1750);
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
		enterRule(_localctx, 318, RULE_rparen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1752);
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
		enterRule(_localctx, 320, RULE_nls);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1757);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,193,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1754);
					match(NL);
					}
					} 
				}
				setState(1759);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,193,_ctx);
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
		enterRule(_localctx, 322, RULE_sep);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1761); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1760);
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
				setState(1763); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,194,_ctx);
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

		case 80:
			return localVariableDeclaration_sempred((LocalVariableDeclarationContext)_localctx, predIndex);

		case 115:
			return expression_sempred((ExpressionContext)_localctx, predIndex);

		case 117:
			return commandExpression_sempred((CommandExpressionContext)_localctx, predIndex);

		case 119:
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
			return  2 == _localctx.t ;
		}
		return true;
	}
	private boolean localVariableDeclaration_sempred(LocalVariableDeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return  !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ;
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 17);

		case 4:
			return precpred(_ctx, 15);

		case 5:
			return precpred(_ctx, 14);

		case 6:
			return precpred(_ctx, 13);

		case 7:
			return precpred(_ctx, 11);

		case 8:
			return precpred(_ctx, 10);

		case 9:
			return precpred(_ctx, 9);

		case 10:
			return precpred(_ctx, 8);

		case 11:
			return precpred(_ctx, 7);

		case 12:
			return precpred(_ctx, 6);

		case 13:
			return precpred(_ctx, 5);

		case 14:
			return precpred(_ctx, 4);

		case 15:
			return precpred(_ctx, 3);

		case 16:
			return precpred(_ctx, 12);

		case 17:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean commandExpression_sempred(CommandExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 18:
			return  !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) ;
		}
		return true;
	}
	private boolean pathExpression_sempred(PathExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 19:
			return  _input.LT(2).getType() == DOT ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\3\u0083\u06e8\4\2\t"+
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
		"\t\u00a0\4\u00a1\t\u00a1\4\u00a2\t\u00a2\4\u00a3\t\u00a3\3\2\3\2\3\2\5"+
		"\2\u014a\n\2\5\2\u014c\n\2\3\2\5\2\u014f\n\2\3\2\3\2\3\3\3\3\3\3\3\3\7"+
		"\3\u0157\n\3\f\3\16\3\u015a\13\3\3\3\5\3\u015d\n\3\3\4\3\4\3\4\3\4\3\4"+
		"\5\4\u0164\n\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\5\6\u016d\n\6\3\6\3\6\3\6\3"+
		"\6\3\6\5\6\u0174\n\6\3\7\3\7\3\7\3\b\3\b\5\b\u017b\n\b\3\t\3\t\3\t\5\t"+
		"\u0180\n\t\3\n\3\n\3\n\3\n\7\n\u0186\n\n\f\n\16\n\u0189\13\n\3\13\3\13"+
		"\7\13\u018d\n\13\f\13\16\13\u0190\13\13\5\13\u0192\n\13\3\f\3\f\3\f\3"+
		"\f\7\f\u0198\n\f\f\f\16\f\u019b\13\f\3\r\3\r\5\r\u019f\n\r\3\16\3\16\5"+
		"\16\u01a3\n\16\3\17\3\17\3\17\5\17\u01a8\n\17\3\20\3\20\3\20\3\20\7\20"+
		"\u01ae\n\20\f\20\16\20\u01b1\13\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\7\21\u01ba\n\21\f\21\16\21\u01bd\13\21\3\21\3\21\3\21\3\22\3\22\3\22"+
		"\3\22\3\22\5\22\u01c7\n\22\3\23\3\23\3\23\3\23\3\23\7\23\u01ce\n\23\f"+
		"\23\16\23\u01d1\13\23\3\24\3\24\3\24\3\24\3\24\7\24\u01d8\n\24\f\24\16"+
		"\24\u01db\13\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\5\25\u01e8\n\25\3\25\3\25\3\25\3\25\5\25\u01ee\n\25\3\25\3\25\3\25\3"+
		"\25\3\25\5\25\u01f5\n\25\3\25\3\25\3\25\3\25\3\25\5\25\u01fc\n\25\3\25"+
		"\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u0208\n\26\3\26\5\26"+
		"\u020b\n\26\3\26\5\26\u020e\n\26\3\26\3\26\3\26\3\26\7\26\u0214\n\26\f"+
		"\26\16\26\u0217\13\26\5\26\u0219\n\26\3\26\5\26\u021c\n\26\3\26\3\26\3"+
		"\27\3\27\3\27\3\27\3\27\3\27\7\27\u0226\n\27\f\27\16\27\u0229\13\27\3"+
		"\30\3\30\3\30\5\30\u022e\n\30\3\30\5\30\u0231\n\30\3\31\3\31\5\31\u0235"+
		"\n\31\3\31\3\31\5\31\u0239\n\31\3\32\3\32\3\32\3\32\3\32\5\32\u0240\n"+
		"\32\3\33\3\33\5\33\u0244\n\33\3\33\5\33\u0247\n\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u0254\n\33\3\33\3\33\3\33\5\33"+
		"\u0259\n\33\5\33\u025b\n\33\3\34\3\34\5\34\u025f\n\34\3\35\3\35\5\35\u0263"+
		"\n\35\3\36\3\36\3\37\3\37\3\37\3\37\3\37\7\37\u026c\n\37\f\37\16\37\u026f"+
		"\13\37\3 \3 \3 \3 \3 \3 \5 \u0277\n \3!\3!\3\"\3\"\3#\3#\3#\3#\3#\3#\7"+
		"#\u0283\n#\f#\16#\u0286\13#\3#\3#\5#\u028a\n#\3$\3$\3$\3$\6$\u0290\n$"+
		"\r$\16$\u0291\3%\5%\u0295\n%\3&\3&\3&\5&\u029a\n&\3&\3&\3\'\3\'\3\'\5"+
		"\'\u02a1\n\'\3\'\5\'\u02a4\n\'\3\'\3\'\3(\3(\5(\u02aa\n(\3(\5(\u02ad\n"+
		"(\3)\3)\5)\u02b1\n)\3*\3*\5*\u02b5\n*\3+\3+\3,\3,\3,\3,\3,\3,\3,\7,\u02c0"+
		"\n,\f,\16,\u02c3\13,\3,\3,\3,\3-\3-\3-\3-\3-\3-\3-\5-\u02cf\n-\5-\u02d1"+
		"\n-\3.\3.\3.\3/\3/\3/\3/\3/\7/\u02db\n/\f/\16/\u02de\13/\3\60\3\60\5\60"+
		"\u02e2\n\60\3\60\3\60\3\61\3\61\5\61\u02e8\n\61\3\61\3\61\3\61\3\61\7"+
		"\61\u02ee\n\61\f\61\16\61\u02f1\13\61\3\62\3\62\3\62\3\63\3\63\5\63\u02f8"+
		"\n\63\3\63\5\63\u02fb\n\63\3\63\3\63\3\63\3\63\3\63\3\63\5\63\u0303\n"+
		"\63\3\64\3\64\3\65\3\65\3\65\7\65\u030a\n\65\f\65\16\65\u030d\13\65\3"+
		"\66\3\66\3\66\3\66\3\66\5\66\u0314\n\66\3\67\3\67\3\67\7\67\u0319\n\67"+
		"\f\67\16\67\u031c\13\67\38\38\38\39\39\39\39\79\u0325\n9\f9\169\u0328"+
		"\139\3:\3:\3:\3:\3:\5:\u032f\n:\3;\3;\3;\3;\7;\u0335\n;\f;\16;\u0338\13"+
		";\3;\3;\3<\3<\5<\u033e\n<\3=\3=\7=\u0342\n=\f=\16=\u0345\13=\3>\3>\3>"+
		"\3>\3>\3>\3?\3?\3?\3?\3?\3?\3@\3@\3A\3A\5A\u0357\nA\3B\3B\5B\u035b\nB"+
		"\3C\3C\3C\3C\3C\5C\u0362\nC\3C\3C\5C\u0366\nC\3C\5C\u0369\nC\3C\3C\3C"+
		"\3D\3D\5D\u0370\nD\3E\5E\u0373\nE\3F\3F\3F\3F\7F\u0379\nF\fF\16F\u037c"+
		"\13F\3F\5F\u037f\nF\3G\3G\3G\3G\7G\u0385\nG\fG\16G\u0388\13G\3G\3G\5G"+
		"\u038c\nG\3H\3H\3H\3H\3H\5H\u0393\nH\3H\3H\5H\u0397\nH\3I\3I\5I\u039b"+
		"\nI\3J\3J\3K\3K\3K\7K\u03a2\nK\fK\16K\u03a5\13K\3L\3L\3L\3L\3L\3L\3M\3"+
		"M\5M\u03af\nM\3N\3N\3N\5N\u03b4\nN\3O\3O\3O\3O\7O\u03ba\nO\fO\16O\u03bd"+
		"\13O\3O\5O\u03c0\nO\5O\u03c2\nO\3O\3O\3P\3P\5P\u03c8\nP\3P\3P\3P\3Q\3"+
		"Q\5Q\u03cf\nQ\3R\3R\3R\3S\3S\3S\5S\u03d7\nS\3S\3S\3S\3S\3S\3S\3S\5S\u03e0"+
		"\nS\3S\3S\3S\5S\u03e5\nS\3T\3T\3T\3T\7T\u03eb\nT\fT\16T\u03ee\13T\3T\3"+
		"T\3U\5U\u03f3\nU\3U\3U\3V\3V\3V\3V\6V\u03fb\nV\rV\16V\u03fc\3V\3V\3W\3"+
		"W\5W\u0403\nW\3X\3X\3X\3X\3X\3X\5X\u040b\nX\3X\3X\3X\3X\5X\u0411\nX\3"+
		"Y\3Y\3Y\3Y\3Y\3Y\6Y\u0419\nY\rY\16Y\u041a\3Y\3Y\5Y\u041f\nY\3Y\3Y\3Z\3"+
		"Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\5Z\u0436\nZ\3[\3"+
		"[\5[\u043a\n[\3\\\3\\\5\\\u043e\n\\\3]\3]\5]\u0442\n]\3]\3]\3]\3]\3]\7"+
		"]\u0449\n]\f]\16]\u044c\13]\3]\3]\3]\5]\u0451\n]\3^\3^\3^\3^\3^\3^\3^"+
		"\5^\u045a\n^\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\5_\u0467\n_\3_\3_\3_\3_"+
		"\3_\3_\3_\3_\3_\3_\3_\3_\3_\5_\u0476\n_\3`\3`\3`\3`\5`\u047c\n`\3`\3`"+
		"\3`\3`\3`\3a\3a\3a\7a\u0486\na\fa\16a\u0489\13a\3b\3b\3b\3b\3c\3c\3c\3"+
		"c\5c\u0493\nc\3c\3c\3d\3d\3d\3d\7d\u049b\nd\fd\16d\u049e\13d\3e\3e\5e"+
		"\u04a2\ne\3f\3f\3f\3f\7f\u04a8\nf\ff\16f\u04ab\13f\3f\3f\3f\3g\3g\3g\3"+
		"g\3g\3g\5g\u04b6\ng\3h\3h\5h\u04ba\nh\3i\3i\5i\u04be\ni\3i\3i\3i\3i\3"+
		"j\5j\u04c5\nj\3j\3j\5j\u04c9\nj\3j\3j\5j\u04cd\nj\3k\3k\5k\u04d1\nk\3"+
		"l\3l\3m\3m\3m\3m\3n\3n\3o\3o\3o\3o\3p\3p\3p\7p\u04e2\np\fp\16p\u04e5\13"+
		"p\3q\5q\u04e8\nq\3q\3q\3r\3r\5r\u04ee\nr\3s\3s\3t\3t\5t\u04f4\nt\3u\3"+
		"u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\5u\u0507\nu\3u\3u\3u\3"+
		"u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\5u\u0522"+
		"\nu\3u\5u\u0525\nu\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u"+
		"\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u"+
		"\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\5u"+
		"\u0565\nu\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\7u\u0575\nu\fu\16"+
		"u\u0578\13u\3v\3v\3v\3v\3v\3v\3v\3v\3v\3v\5v\u0584\nv\3w\3w\3w\3w\5w\u058a"+
		"\nw\3w\7w\u058d\nw\fw\16w\u0590\13w\3x\3x\6x\u0594\nx\rx\16x\u0595\3x"+
		"\5x\u0599\nx\3y\3y\3y\5y\u059e\ny\3y\3y\3y\7y\u05a3\ny\fy\16y\u05a6\13"+
		"y\3z\3z\3z\3z\3z\5z\u05ad\nz\3z\3z\3z\3z\5z\u05b3\nz\3z\3z\3z\3z\3z\3"+
		"z\3z\3z\3z\3z\3z\3z\5z\u05c1\nz\3z\3z\3z\3z\3z\3z\3z\3z\3z\5z\u05cc\n"+
		"z\3{\3{\3{\3{\5{\u05d2\n{\3|\3|\5|\u05d6\n|\3}\5}\u05d9\n}\3}\3}\5}\u05dd"+
		"\n}\3}\3}\3~\5~\u05e2\n~\3~\3~\3~\5~\u05e7\n~\3~\3~\3\177\3\177\5\177"+
		"\u05ed\n\177\3\177\3\177\3\177\3\177\3\177\3\177\3\177\3\177\3\177\3\177"+
		"\3\177\3\177\3\177\5\177\u05fc\n\177\3\u0080\3\u0080\3\u0080\3\u0080\5"+
		"\u0080\u0602\n\u0080\3\u0081\3\u0081\3\u0081\5\u0081\u0607\n\u0081\3\u0082"+
		"\3\u0082\3\u0082\5\u0082\u060c\n\u0082\3\u0083\3\u0083\5\u0083\u0610\n"+
		"\u0083\3\u0083\5\u0083\u0613\n\u0083\3\u0083\3\u0083\3\u0084\3\u0084\3"+
		"\u0084\5\u0084\u061a\n\u0084\3\u0084\5\u0084\u061d\n\u0084\3\u0084\3\u0084"+
		"\3\u0085\3\u0085\3\u0085\7\u0085\u0624\n\u0085\f\u0085\16\u0085\u0627"+
		"\13\u0085\3\u0086\3\u0086\3\u0086\7\u0086\u062c\n\u0086\f\u0086\16\u0086"+
		"\u062f\13\u0086\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087\3\u0087"+
		"\3\u0087\3\u0087\3\u0087\5\u0087\u063b\n\u0087\3\u0088\3\u0088\3\u0088"+
		"\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\3\u0088\5\u0088\u0647"+
		"\n\u0088\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089"+
		"\3\u0089\3\u0089\5\u0089\u0653\n\u0089\3\u008a\3\u008a\5\u008a\u0657\n"+
		"\u008a\3\u008b\3\u008b\5\u008b\u065b\n\u008b\3\u008c\3\u008c\5\u008c\u065f"+
		"\n\u008c\3\u008d\3\u008d\3\u008d\3\u008d\5\u008d\u0665\n\u008d\3\u008d"+
		"\6\u008d\u0668\n\u008d\r\u008d\16\u008d\u0669\3\u008d\3\u008d\3\u008d"+
		"\5\u008d\u066f\n\u008d\5\u008d\u0671\n\u008d\3\u008e\3\u008e\3\u008e\5"+
		"\u008e\u0676\n\u008e\3\u008e\3\u008e\3\u008f\3\u008f\3\u008f\3\u008f\3"+
		"\u008f\5\u008f\u067f\n\u008f\3\u008f\3\u008f\3\u0090\3\u0090\3\u0091\3"+
		"\u0091\3\u0091\3\u0091\5\u0091\u0689\n\u0091\5\u0091\u068b\n\u0091\3\u0092"+
		"\3\u0092\3\u0092\3\u0092\3\u0092\3\u0092\3\u0093\3\u0093\3\u0093\5\u0093"+
		"\u0696\n\u0093\3\u0094\3\u0094\5\u0094\u069a\n\u0094\3\u0094\5\u0094\u069d"+
		"\n\u0094\3\u0094\3\u0094\3\u0095\3\u0095\3\u0095\3\u0095\3\u0095\7\u0095"+
		"\u06a6\n\u0095\f\u0095\16\u0095\u06a9\13\u0095\3\u0096\3\u0096\3\u0096"+
		"\3\u0096\3\u0096\7\u0096\u06b0\n\u0096\f\u0096\16\u0096\u06b3\13\u0096"+
		"\3\u0097\3\u0097\3\u0097\3\u0097\3\u0097\7\u0097\u06ba\n\u0097\f\u0097"+
		"\16\u0097\u06bd\13\u0097\3\u0098\3\u0098\5\u0098\u06c1\n\u0098\3\u0099"+
		"\3\u0099\5\u0099\u06c5\n\u0099\3\u009a\3\u009a\3\u009a\5\u009a\u06ca\n"+
		"\u009a\3\u009b\3\u009b\3\u009b\5\u009b\u06cf\n\u009b\3\u009c\3\u009c\3"+
		"\u009d\3\u009d\3\u009e\3\u009e\3\u009f\3\u009f\3\u00a0\3\u00a0\3\u00a1"+
		"\3\u00a1\3\u00a2\7\u00a2\u06de\n\u00a2\f\u00a2\16\u00a2\u06e1\13\u00a2"+
		"\3\u00a3\6\u00a3\u06e4\n\u00a3\r\u00a3\16\u00a3\u06e5\3\u00a3\2\2\3\u00e8"+
		"\u00a4\2\2\4\2\6\2\b\2\n\2\f\2\16\2\20\2\22\2\24\2\26\2\30\2\32\2\34\2"+
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
		"\2\u0140\2\u0142\2\u0144\2\2\30\b\2\n\n\16\16&&\61\61\65\6588\7\2\20\20"+
		"\30\30\35\35)+-.\b\2\n\n\16\16\20\20\35\35)+-.\4\2\34\34//\4\2WW__\4\2"+
		"\13\13__\3\2fg\3\2\\]\3\2fi\4\2jkoo\3\2hi\3\2>?\6\2\13\13OOZ[ab\5\2JL"+
		"``cc\3\2FG\5\2\t\t$$NN\5\2IIYYp{\4\2@BXX\6\2\t\t\13\f\16\16|}\4\2\17\17"+
		"\67\67\4\2\t9<=\4\2VV\u0081\u0081\2\u074e\2\u0146\3\2\2\2\4\u0152\3\2"+
		"\2\2\6\u0163\3\2\2\2\b\u0165\3\2\2\2\n\u0169\3\2\2\2\f\u0175\3\2\2\2\16"+
		"\u017a\3\2\2\2\20\u017f\3\2\2\2\22\u0181\3\2\2\2\24\u0191\3\2\2\2\26\u0193"+
		"\3\2\2\2\30\u019e\3\2\2\2\32\u01a2\3\2\2\2\34\u01a7\3\2\2\2\36\u01a9\3"+
		"\2\2\2 \u01b2\3\2\2\2\"\u01c1\3\2\2\2$\u01c8\3\2\2\2&\u01d2\3\2\2\2(\u01e7"+
		"\3\2\2\2*\u0200\3\2\2\2,\u021f\3\2\2\2.\u022a\3\2\2\2\60\u0238\3\2\2\2"+
		"\62\u023f\3\2\2\2\64\u0241\3\2\2\2\66\u025e\3\2\2\28\u0262\3\2\2\2:\u0264"+
		"\3\2\2\2<\u0266\3\2\2\2>\u0270\3\2\2\2@\u0278\3\2\2\2B\u027a\3\2\2\2D"+
		"\u027c\3\2\2\2F\u028f\3\2\2\2H\u0294\3\2\2\2J\u0296\3\2\2\2L\u029d\3\2"+
		"\2\2N\u02a9\3\2\2\2P\u02ae\3\2\2\2R\u02b2\3\2\2\2T\u02b6\3\2\2\2V\u02b8"+
		"\3\2\2\2X\u02d0\3\2\2\2Z\u02d2\3\2\2\2\\\u02d5\3\2\2\2^\u02df\3\2\2\2"+
		"`\u02e7\3\2\2\2b\u02f2\3\2\2\2d\u02f5\3\2\2\2f\u0304\3\2\2\2h\u0306\3"+
		"\2\2\2j\u0313\3\2\2\2l\u031a\3\2\2\2n\u031d\3\2\2\2p\u0320\3\2\2\2r\u032e"+
		"\3\2\2\2t\u0330\3\2\2\2v\u033d\3\2\2\2x\u033f\3\2\2\2z\u0346\3\2\2\2|"+
		"\u034c\3\2\2\2~\u0352\3\2\2\2\u0080\u0356\3\2\2\2\u0082\u035a\3\2\2\2"+
		"\u0084\u035c\3\2\2\2\u0086\u036f\3\2\2\2\u0088\u0372\3\2\2\2\u008a\u0374"+
		"\3\2\2\2\u008c\u038b\3\2\2\2\u008e\u038d\3\2\2\2\u0090\u039a\3\2\2\2\u0092"+
		"\u039c\3\2\2\2\u0094\u039e\3\2\2\2\u0096\u03a6\3\2\2\2\u0098\u03ae\3\2"+
		"\2\2\u009a\u03b3\3\2\2\2\u009c\u03b5\3\2\2\2\u009e\u03c5\3\2\2\2\u00a0"+
		"\u03ce\3\2\2\2\u00a2\u03d0\3\2\2\2\u00a4\u03e4\3\2\2\2\u00a6\u03e6\3\2"+
		"\2\2\u00a8\u03f2\3\2\2\2\u00aa\u03f6\3\2\2\2\u00ac\u0402\3\2\2\2\u00ae"+
		"\u0404\3\2\2\2\u00b0\u0412\3\2\2\2\u00b2\u0435\3\2\2\2\u00b4\u0437\3\2"+
		"\2\2\u00b6\u043b\3\2\2\2\u00b8\u043f\3\2\2\2\u00ba\u0452\3\2\2\2\u00bc"+
		"\u0475\3\2\2\2\u00be\u0477\3\2\2\2\u00c0\u0482\3\2\2\2\u00c2\u048a\3\2"+
		"\2\2\u00c4\u048e\3\2\2\2\u00c6\u0496\3\2\2\2\u00c8\u04a1\3\2\2\2\u00ca"+
		"\u04a3\3\2\2\2\u00cc\u04b5\3\2\2\2\u00ce\u04b9\3\2\2\2\u00d0\u04bb\3\2"+
		"\2\2\u00d2\u04c4\3\2\2\2\u00d4\u04d0\3\2\2\2\u00d6\u04d2\3\2\2\2\u00d8"+
		"\u04d4\3\2\2\2\u00da\u04d8\3\2\2\2\u00dc\u04da\3\2\2\2\u00de\u04de\3\2"+
		"\2\2\u00e0\u04e7\3\2\2\2\u00e2\u04ed\3\2\2\2\u00e4\u04ef\3\2\2\2\u00e6"+
		"\u04f1\3\2\2\2\u00e8\u0506\3\2\2\2\u00ea\u0583\3\2\2\2\u00ec\u0585\3\2"+
		"\2\2\u00ee\u0591\3\2\2\2\u00f0\u059d\3\2\2\2\u00f2\u05cb\3\2\2\2\u00f4"+
		"\u05d1\3\2\2\2\u00f6\u05d5\3\2\2\2\u00f8\u05d8\3\2\2\2\u00fa\u05e1\3\2"+
		"\2\2\u00fc\u05fb\3\2\2\2\u00fe\u0601\3\2\2\2\u0100\u0606\3\2\2\2\u0102"+
		"\u060b\3\2\2\2\u0104\u060d\3\2\2\2\u0106\u0616\3\2\2\2\u0108\u0620\3\2"+
		"\2\2\u010a\u0628\3\2\2\2\u010c\u063a\3\2\2\2\u010e\u0646\3\2\2\2\u0110"+
		"\u0652\3\2\2\2\u0112\u0656\3\2\2\2\u0114\u065a\3\2\2\2\u0116\u065e\3\2"+
		"\2\2\u0118\u0660\3\2\2\2\u011a\u0672\3\2\2\2\u011c\u0679\3\2\2\2\u011e"+
		"\u0682\3\2\2\2\u0120\u0684\3\2\2\2\u0122\u068c\3\2\2\2\u0124\u0695\3\2"+
		"\2\2\u0126\u0697\3\2\2\2\u0128\u06a0\3\2\2\2\u012a\u06aa\3\2\2\2\u012c"+
		"\u06b4\3\2\2\2\u012e\u06c0\3\2\2\2\u0130\u06c4\3\2\2\2\u0132\u06c9\3\2"+
		"\2\2\u0134\u06ce\3\2\2\2\u0136\u06d0\3\2\2\2\u0138\u06d2\3\2\2\2\u013a"+
		"\u06d4\3\2\2\2\u013c\u06d6\3\2\2\2\u013e\u06d8\3\2\2\2\u0140\u06da\3\2"+
		"\2\2\u0142\u06df\3\2\2\2\u0144\u06e3\3\2\2\2\u0146\u014b\5\u0142\u00a2"+
		"\2\u0147\u0149\5\b\5\2\u0148\u014a\5\u0144\u00a3\2\u0149\u0148\3\2\2\2"+
		"\u0149\u014a\3\2\2\2\u014a\u014c\3\2\2\2\u014b\u0147\3\2\2\2\u014b\u014c"+
		"\3\2\2\2\u014c\u014e\3\2\2\2\u014d\u014f\5\4\3\2\u014e\u014d\3\2\2\2\u014e"+
		"\u014f\3\2\2\2\u014f\u0150\3\2\2\2\u0150\u0151\7\2\2\3\u0151\3\3\2\2\2"+
		"\u0152\u0158\5\6\4\2\u0153\u0154\5\u0144\u00a3\2\u0154\u0155\5\6\4\2\u0155"+
		"\u0157\3\2\2\2\u0156\u0153\3\2\2\2\u0157\u015a\3\2\2\2\u0158\u0156\3\2"+
		"\2\2\u0158\u0159\3\2\2\2\u0159\u015c\3\2\2\2\u015a\u0158\3\2\2\2\u015b"+
		"\u015d\5\u0144\u00a3\2\u015c\u015b\3\2\2\2\u015c\u015d\3\2\2\2\u015d\5"+
		"\3\2\2\2\u015e\u0164\5\n\6\2\u015f\u0164\5\f\7\2\u0160\u0161\6\4\2\2\u0161"+
		"\u0164\5\64\33\2\u0162\u0164\5\u00bc_\2\u0163\u015e\3\2\2\2\u0163\u015f"+
		"\3\2\2\2\u0163\u0160\3\2\2\2\u0163\u0162\3\2\2\2\u0164\7\3\2\2\2\u0165"+
		"\u0166\5\u008cG\2\u0166\u0167\7(\2\2\u0167\u0168\5h\65\2\u0168\t\3\2\2"+
		"\2\u0169\u016a\5\u008cG\2\u016a\u016c\7#\2\2\u016b\u016d\7-\2\2\u016c"+
		"\u016b\3\2\2\2\u016c\u016d\3\2\2\2\u016d\u016e\3\2\2\2\u016e\u0173\5h"+
		"\65\2\u016f\u0170\7X\2\2\u0170\u0174\7j\2\2\u0171\u0172\7\t\2\2\u0172"+
		"\u0174\5\u013a\u009e\2\u0173\u016f\3\2\2\2\u0173\u0171\3\2\2\2\u0173\u0174"+
		"\3\2\2\2\u0174\13\3\2\2\2\u0175\u0176\5\24\13\2\u0176\u0177\5(\25\2\u0177"+
		"\r\3\2\2\2\u0178\u017b\5\30\r\2\u0179\u017b\t\2\2\2\u017a\u0178\3\2\2"+
		"\2\u017a\u0179\3\2\2\2\u017b\17\3\2\2\2\u017c\u017d\5\22\n\2\u017d\u017e"+
		"\5\u0142\u00a2\2\u017e\u0180\3\2\2\2\u017f\u017c\3\2\2\2\u017f\u0180\3"+
		"\2\2\2\u0180\21\3\2\2\2\u0181\u0187\5\16\b\2\u0182\u0183\5\u0142\u00a2"+
		"\2\u0183\u0184\5\16\b\2\u0184\u0186\3\2\2\2\u0185\u0182\3\2\2\2\u0186"+
		"\u0189\3\2\2\2\u0187\u0185\3\2\2\2\u0187\u0188\3\2\2\2\u0188\23\3\2\2"+
		"\2\u0189\u0187\3\2\2\2\u018a\u018e\5\26\f\2\u018b\u018d\7\u0081\2\2\u018c"+
		"\u018b\3\2\2\2\u018d\u0190\3\2\2\2\u018e\u018c\3\2\2\2\u018e\u018f\3\2"+
		"\2\2\u018f\u0192\3\2\2\2\u0190\u018e\3\2\2\2\u0191\u018a\3\2\2\2\u0191"+
		"\u0192\3\2\2\2\u0192\25\3\2\2\2\u0193\u0199\5\30\r\2\u0194\u0195\5\u0142"+
		"\u00a2\2\u0195\u0196\5\30\r\2\u0196\u0198\3\2\2\2\u0197\u0194\3\2\2\2"+
		"\u0198\u019b\3\2\2\2\u0199\u0197\3\2\2\2\u0199\u019a\3\2\2\2\u019a\27"+
		"\3\2\2\2\u019b\u0199\3\2\2\2\u019c\u019f\5\u008eH\2\u019d\u019f\t\3\2"+
		"\2\u019e\u019c\3\2\2\2\u019e\u019d\3\2\2\2\u019f\31\3\2\2\2\u01a0\u01a3"+
		"\5\u008eH\2\u01a1\u01a3\t\4\2\2\u01a2\u01a0\3\2\2\2\u01a2\u01a1\3\2\2"+
		"\2\u01a3\33\3\2\2\2\u01a4\u01a5\5\36\20\2\u01a5\u01a6\5\u0142\u00a2\2"+
		"\u01a6\u01a8\3\2\2\2\u01a7\u01a4\3\2\2\2\u01a7\u01a8\3\2\2\2\u01a8\35"+
		"\3\2\2\2\u01a9\u01af\5\32\16\2\u01aa\u01ab\5\u0142\u00a2\2\u01ab\u01ac"+
		"\5\32\16\2\u01ac\u01ae\3\2\2\2\u01ad\u01aa\3\2\2\2\u01ae\u01b1\3\2\2\2"+
		"\u01af\u01ad\3\2\2\2\u01af\u01b0\3\2\2\2\u01b0\37\3\2\2\2\u01b1\u01af"+
		"\3\2\2\2\u01b2\u01b3\7[\2\2\u01b3\u01b4\5\u0142\u00a2\2\u01b4\u01bb\5"+
		"\"\22\2\u01b5\u01b6\7W\2\2\u01b6\u01b7\5\u0142\u00a2\2\u01b7\u01b8\5\""+
		"\22\2\u01b8\u01ba\3\2\2\2\u01b9\u01b5\3\2\2\2\u01ba\u01bd\3\2\2\2\u01bb"+
		"\u01b9\3\2\2\2\u01bb\u01bc\3\2\2\2\u01bc\u01be\3\2\2\2\u01bd\u01bb\3\2"+
		"\2\2\u01be\u01bf\5\u0142\u00a2\2\u01bf\u01c0\7Z\2\2\u01c0!\3\2\2\2\u01c1"+
		"\u01c6\5\u0138\u009d\2\u01c2\u01c3\7\34\2\2\u01c3\u01c4\5\u0142\u00a2"+
		"\2\u01c4\u01c5\5$\23\2\u01c5\u01c7\3\2\2\2\u01c6\u01c2\3\2\2\2\u01c6\u01c7"+
		"\3\2\2\2\u01c7#\3\2\2\2\u01c8\u01cf\5L\'\2\u01c9\u01ca\7l\2\2\u01ca\u01cb"+
		"\5\u0142\u00a2\2\u01cb\u01cc\5L\'\2\u01cc\u01ce\3\2\2\2\u01cd\u01c9\3"+
		"\2\2\2\u01ce\u01d1\3\2\2\2\u01cf\u01cd\3\2\2\2\u01cf\u01d0\3\2\2\2\u01d0"+
		"%\3\2\2\2\u01d1\u01cf\3\2\2\2\u01d2\u01d9\5L\'\2\u01d3\u01d4\7W\2\2\u01d4"+
		"\u01d5\5\u0142\u00a2\2\u01d5\u01d6\5L\'\2\u01d6\u01d8\3\2\2\2\u01d7\u01d3"+
		"\3\2\2\2\u01d8\u01db\3\2\2\2\u01d9\u01d7\3\2\2\2\u01d9\u01da\3\2\2\2\u01da"+
		"\'\3\2\2\2\u01db\u01d9\3\2\2\2\u01dc\u01dd\7\25\2\2\u01dd\u01e8\b\25\1"+
		"\2\u01de\u01df\7%\2\2\u01df\u01e8\b\25\1\2\u01e0\u01e1\7\33\2\2\u01e1"+
		"\u01e8\b\25\1\2\u01e2\u01e3\7~\2\2\u01e3\u01e4\7%\2\2\u01e4\u01e8\b\25"+
		"\1\2\u01e5\u01e6\7\f\2\2\u01e6\u01e8\b\25\1\2\u01e7\u01dc\3\2\2\2\u01e7"+
		"\u01de\3\2\2\2\u01e7\u01e0\3\2\2\2\u01e7\u01e2\3\2\2\2\u01e7\u01e5\3\2"+
		"\2\2\u01e8\u01e9\3\2\2\2\u01e9\u01ed\5\u013a\u009e\2\u01ea\u01eb\5\u0142"+
		"\u00a2\2\u01eb\u01ec\5 \21\2\u01ec\u01ee\3\2\2\2\u01ed\u01ea\3\2\2\2\u01ed"+
		"\u01ee\3\2\2\2\u01ee\u01f4\3\2\2\2\u01ef\u01f0\5\u0142\u00a2\2\u01f0\u01f1"+
		"\7\34\2\2\u01f1\u01f2\5\u0142\u00a2\2\u01f2\u01f3\5&\24\2\u01f3\u01f5"+
		"\3\2\2\2\u01f4\u01ef\3\2\2\2\u01f4\u01f5\3\2\2\2\u01f5\u01fb\3\2\2\2\u01f6"+
		"\u01f7\5\u0142\u00a2\2\u01f7\u01f8\7\"\2\2\u01f8\u01f9\5\u0142\u00a2\2"+
		"\u01f9\u01fa\5&\24\2\u01fa\u01fc\3\2\2\2\u01fb\u01f6\3\2\2\2\u01fb\u01fc"+
		"\3\2\2\2\u01fc\u01fd\3\2\2\2\u01fd\u01fe\5\u0142\u00a2\2\u01fe\u01ff\5"+
		"*\26\2\u01ff)\3\2\2\2\u0200\u0201\7R\2\2\u0201\u020d\5\u0142\u00a2\2\u0202"+
		"\u0203\6\26\3\3\u0203\u0207\5,\27\2\u0204\u0205\5\u0142\u00a2\2\u0205"+
		"\u0206\7W\2\2\u0206\u0208\3\2\2\2\u0207\u0204\3\2\2\2\u0207\u0208\3\2"+
		"\2\2\u0208\u020a\3\2\2\2\u0209\u020b\5\u0144\u00a3\2\u020a\u0209\3\2\2"+
		"\2\u020a\u020b\3\2\2\2\u020b\u020e\3\2\2\2\u020c\u020e\3\2\2\2\u020d\u0202"+
		"\3\2\2\2\u020d\u020c\3\2\2\2\u020e\u0218\3\2\2\2\u020f\u0215\5\60\31\2"+
		"\u0210\u0211\5\u0144\u00a3\2\u0211\u0212\5\60\31\2\u0212\u0214\3\2\2\2"+
		"\u0213\u0210\3\2\2\2\u0214\u0217\3\2\2\2\u0215\u0213\3\2\2\2\u0215\u0216"+
		"\3\2\2\2\u0216\u0219\3\2\2\2\u0217\u0215\3\2\2\2\u0218\u020f\3\2\2\2\u0218"+
		"\u0219\3\2\2\2\u0219\u021b\3\2\2\2\u021a\u021c\5\u0144\u00a3\2\u021b\u021a"+
		"\3\2\2\2\u021b\u021c\3\2\2\2\u021c\u021d\3\2\2\2\u021d\u021e\7S\2\2\u021e"+
		"+\3\2\2\2\u021f\u0227\5.\30\2\u0220\u0221\5\u0142\u00a2\2\u0221\u0222"+
		"\7W\2\2\u0222\u0223\5\u0142\u00a2\2\u0223\u0224\5.\30\2\u0224\u0226\3"+
		"\2\2\2\u0225\u0220\3\2\2\2\u0226\u0229\3\2\2\2\u0227\u0225\3\2\2\2\u0227"+
		"\u0228\3\2\2\2\u0228-\3\2\2\2\u0229\u0227\3\2\2\2\u022a\u022b\5\u008c"+
		"G\2\u022b\u022d\5\u013a\u009e\2\u022c\u022e\5\u0126\u0094\2\u022d\u022c"+
		"\3\2\2\2\u022d\u022e\3\2\2\2\u022e\u0230\3\2\2\2\u022f\u0231\5\u011e\u0090"+
		"\2\u0230\u022f\3\2\2\2\u0230\u0231\3\2\2\2\u0231/\3\2\2\2\u0232\u0233"+
		"\7-\2\2\u0233\u0235\5\u0142\u00a2\2\u0234\u0232\3\2\2\2\u0234\u0235\3"+
		"\2\2\2\u0235\u0236\3\2\2\2\u0236\u0239\5\u009eP\2\u0237\u0239\5\62\32"+
		"\2\u0238\u0234\3\2\2\2\u0238\u0237\3\2\2\2\u0239\61\3\2\2\2\u023a\u0240"+
		"\5\64\33\2\u023b\u0240\5:\36\2\u023c\u023d\5\20\t\2\u023d\u023e\5(\25"+
		"\2\u023e\u0240\3\2\2\2\u023f\u023a\3\2\2\2\u023f\u023b\3\2\2\2\u023f\u023c"+
		"\3\2\2\2\u0240\63\3\2\2\2\u0241\u0243\5\20\t\2\u0242\u0244\5 \21\2\u0243"+
		"\u0242\3\2\2\2\u0243\u0244\3\2\2\2\u0244\u0246\3\2\2\2\u0245\u0247\58"+
		"\35\2\u0246\u0245\3\2\2\2\u0246\u0247\3\2\2\2\u0247\u0248\3\2\2\2\u0248"+
		"\u0249\5\66\34\2\u0249\u025a\5^\60\2\u024a\u024b\7\30\2\2\u024b\u024c"+
		"\5\u0142\u00a2\2\u024c\u024d\5\u009aN\2\u024d\u025b\3\2\2\2\u024e\u024f"+
		"\5\u0142\u00a2\2\u024f\u0250\7\64\2\2\u0250\u0251\5\u0142\u00a2\2\u0251"+
		"\u0252\5\\/\2\u0252\u0254\3\2\2\2\u0253\u024e\3\2\2\2\u0253\u0254\3\2"+
		"\2\2\u0254\u0258\3\2\2\2\u0255\u0256\5\u0142\u00a2\2\u0256\u0257\5f\64"+
		"\2\u0257\u0259\3\2\2\2\u0258\u0255\3\2\2\2\u0258\u0259\3\2\2\2\u0259\u025b"+
		"\3\2\2\2\u025a\u024a\3\2\2\2\u025a\u0253\3\2\2\2\u025a\u025b\3\2\2\2\u025b"+
		"\65\3\2\2\2\u025c\u025f\5\u013a\u009e\2\u025d\u025f\5\u0136\u009c\2\u025e"+
		"\u025c\3\2\2\2\u025e\u025d\3\2\2\2\u025f\67\3\2\2\2\u0260\u0263\5J&\2"+
		"\u0261\u0263\7\67\2\2\u0262\u0260\3\2\2\2\u0262\u0261\3\2\2\2\u02639\3"+
		"\2\2\2\u0264\u0265\5\u00a4S\2\u0265;\3\2\2\2\u0266\u026d\5> \2\u0267\u0268"+
		"\7W\2\2\u0268\u0269\5\u0142\u00a2\2\u0269\u026a\5> \2\u026a\u026c\3\2"+
		"\2\2\u026b\u0267\3\2\2\2\u026c\u026f\3\2\2\2\u026d\u026b\3\2\2\2\u026d"+
		"\u026e\3\2\2\2\u026e=\3\2\2\2\u026f\u026d\3\2\2\2\u0270\u0276\5@!\2\u0271"+
		"\u0272\5\u0142\u00a2\2\u0272\u0273\7Y\2\2\u0273\u0274\5\u0142\u00a2\2"+
		"\u0274\u0275\5B\"\2\u0275\u0277\3\2\2\2\u0276\u0271\3\2\2\2\u0276\u0277"+
		"\3\2\2\2\u0277?\3\2\2\2\u0278\u0279\5\u013a\u009e\2\u0279A\3\2\2\2\u027a"+
		"\u027b\5\u00e2r\2\u027bC\3\2\2\2\u027c\u0284\5B\"\2\u027d\u027e\5\u0142"+
		"\u00a2\2\u027e\u027f\7W\2\2\u027f\u0280\5\u0142\u00a2\2\u0280\u0281\5"+
		"B\"\2\u0281\u0283\3\2\2\2\u0282\u027d\3\2\2\2\u0283\u0286\3\2\2\2\u0284"+
		"\u0282\3\2\2\2\u0284\u0285\3\2\2\2\u0285\u0287\3\2\2\2\u0286\u0284\3\2"+
		"\2\2\u0287\u0289\5\u0142\u00a2\2\u0288\u028a\7W\2\2\u0289\u0288\3\2\2"+
		"\2\u0289\u028a\3\2\2\2\u028aE\3\2\2\2\u028b\u028c\5\u008cG\2\u028c\u028d"+
		"\7T\2\2\u028d\u028e\7U\2\2\u028e\u0290\3\2\2\2\u028f\u028b\3\2\2\2\u0290"+
		"\u0291\3\2\2\2\u0291\u028f\3\2\2\2\u0291\u0292\3\2\2\2\u0292G\3\2\2\2"+
		"\u0293\u0295\5F$\2\u0294\u0293\3\2\2\2\u0294\u0295\3\2\2\2\u0295I\3\2"+
		"\2\2\u0296\u0299\5\u008cG\2\u0297\u029a\5T+\2\u0298\u029a\5R*\2\u0299"+
		"\u0297\3\2\2\2\u0299\u0298\3\2\2\2\u029a\u029b\3\2\2\2\u029b\u029c\5H"+
		"%\2\u029cK\3\2\2\2\u029d\u02a3\5\u008cG\2\u029e\u02a1\5T+\2\u029f\u02a1"+
		"\7\67\2\2\u02a0\u029e\3\2\2\2\u02a0\u029f\3\2\2\2\u02a1\u02a4\3\2\2\2"+
		"\u02a2\u02a4\5P)\2\u02a3\u02a0\3\2\2\2\u02a3\u02a2\3\2\2\2\u02a4\u02a5"+
		"\3\2\2\2\u02a5\u02a6\5H%\2\u02a6M\3\2\2\2\u02a7\u02aa\5n8\2\u02a8\u02aa"+
		"\5p9\2\u02a9\u02a7\3\2\2\2\u02a9\u02a8\3\2\2\2\u02aa\u02ac\3\2\2\2\u02ab"+
		"\u02ad\5V,\2\u02ac\u02ab\3\2\2\2\u02ac\u02ad\3\2\2\2\u02adO\3\2\2\2\u02ae"+
		"\u02b0\5n8\2\u02af\u02b1\5V,\2\u02b0\u02af\3\2\2\2\u02b0\u02b1\3\2\2\2"+
		"\u02b1Q\3\2\2\2\u02b2\u02b4\5p9\2\u02b3\u02b5\5V,\2\u02b4\u02b3\3\2\2"+
		"\2\u02b4\u02b5\3\2\2\2\u02b5S\3\2\2\2\u02b6\u02b7\7\17\2\2\u02b7U\3\2"+
		"\2\2\u02b8\u02b9\7[\2\2\u02b9\u02ba\5\u0142\u00a2\2\u02ba\u02c1\5X-\2"+
		"\u02bb\u02bc\7W\2\2\u02bc\u02bd\5\u0142\u00a2\2\u02bd\u02be\5X-\2\u02be"+
		"\u02c0\3\2\2\2\u02bf\u02bb\3\2\2\2\u02c0\u02c3\3\2\2\2\u02c1\u02bf\3\2"+
		"\2\2\u02c1\u02c2\3\2\2\2\u02c2\u02c4\3\2\2\2\u02c3\u02c1\3\2\2\2\u02c4"+
		"\u02c5\5\u0142\u00a2\2\u02c5\u02c6\7Z\2\2\u02c6W\3\2\2\2\u02c7\u02d1\5"+
		"L\'\2\u02c8\u02c9\5\u008cG\2\u02c9\u02ce\7^\2\2\u02ca\u02cb\t\5\2\2\u02cb"+
		"\u02cc\5\u0142\u00a2\2\u02cc\u02cd\5L\'\2\u02cd\u02cf\3\2\2\2\u02ce\u02ca"+
		"\3\2\2\2\u02ce\u02cf\3\2\2\2\u02cf\u02d1\3\2\2\2\u02d0\u02c7\3\2\2\2\u02d0"+
		"\u02c8\3\2\2\2\u02d1Y\3\2\2\2\u02d2\u02d3\5\u008cG\2\u02d3\u02d4\5n8\2"+
		"\u02d4[\3\2\2\2\u02d5\u02dc\5Z.\2\u02d6\u02d7\7W\2\2\u02d7\u02d8\5\u0142"+
		"\u00a2\2\u02d8\u02d9\5Z.\2\u02d9\u02db\3\2\2\2\u02da\u02d6\3\2\2\2\u02db"+
		"\u02de\3\2\2\2\u02dc\u02da\3\2\2\2\u02dc\u02dd\3\2\2\2\u02dd]\3\2\2\2"+
		"\u02de\u02dc\3\2\2\2\u02df\u02e1\7P\2\2\u02e0\u02e2\5`\61\2\u02e1\u02e0"+
		"\3\2\2\2\u02e1\u02e2\3\2\2\2\u02e2\u02e3\3\2\2\2\u02e3\u02e4\5\u0140\u00a1"+
		"\2\u02e4_\3\2\2\2\u02e5\u02e8\5d\63\2\u02e6\u02e8\5b\62\2\u02e7\u02e5"+
		"\3\2\2\2\u02e7\u02e6\3\2\2\2\u02e8\u02ef\3\2\2\2\u02e9\u02ea\7W\2\2\u02ea"+
		"\u02eb\5\u0142\u00a2\2\u02eb\u02ec\5d\63\2\u02ec\u02ee\3\2\2\2\u02ed\u02e9"+
		"\3\2\2\2\u02ee\u02f1\3\2\2\2\u02ef\u02ed\3\2\2\2\u02ef\u02f0\3\2\2\2\u02f0"+
		"a\3\2\2\2\u02f1\u02ef\3\2\2\2\u02f2\u02f3\5L\'\2\u02f3\u02f4\7\62\2\2"+
		"\u02f4c\3\2\2\2\u02f5\u02f7\5\34\17\2\u02f6\u02f8\5L\'\2\u02f7\u02f6\3"+
		"\2\2\2\u02f7\u02f8\3\2\2\2\u02f8\u02fa\3\2\2\2\u02f9\u02fb\7\177\2\2\u02fa"+
		"\u02f9\3\2\2\2\u02fa\u02fb\3\2\2\2\u02fb\u02fc\3\2\2\2\u02fc\u0302\5@"+
		"!\2\u02fd\u02fe\5\u0142\u00a2\2\u02fe\u02ff\7Y\2\2\u02ff\u0300\5\u0142"+
		"\u00a2\2\u0300\u0301\5\u00e8u\2\u0301\u0303\3\2\2\2\u0302\u02fd\3\2\2"+
		"\2\u0302\u0303\3\2\2\2\u0303e\3\2\2\2\u0304\u0305\5\u009eP\2\u0305g\3"+
		"\2\2\2\u0306\u030b\5j\66\2\u0307\u0308\7X\2\2\u0308\u030a\5j\66\2\u0309"+
		"\u0307\3\2\2\2\u030a\u030d\3\2\2\2\u030b\u0309\3\2\2\2\u030b\u030c\3\2"+
		"\2\2\u030ci\3\2\2\2\u030d\u030b\3\2\2\2\u030e\u0314\5\u013a\u009e\2\u030f"+
		"\u0314\7\n\2\2\u0310\u0314\7\13\2\2\u0311\u0314\7\t\2\2\u0312\u0314\7"+
		"\f\2\2\u0313\u030e\3\2\2\2\u0313\u030f\3\2\2\2\u0313\u0310\3\2\2\2\u0313"+
		"\u0311\3\2\2\2\u0313\u0312\3\2\2\2\u0314k\3\2\2\2\u0315\u0316\5j\66\2"+
		"\u0316\u0317\7X\2\2\u0317\u0319\3\2\2\2\u0318\u0315\3\2\2\2\u0319\u031c"+
		"\3\2\2\2\u031a\u0318\3\2\2\2\u031a\u031b\3\2\2\2\u031bm\3\2\2\2\u031c"+
		"\u031a\3\2\2\2\u031d\u031e\5l\67\2\u031e\u031f\5\u013a\u009e\2\u031fo"+
		"\3\2\2\2\u0320\u0321\5l\67\2\u0321\u0326\5\u0138\u009d\2\u0322\u0323\7"+
		"X\2\2\u0323\u0325\5\u0138\u009d\2\u0324\u0322\3\2\2\2\u0325\u0328\3\2"+
		"\2\2\u0326\u0324\3\2\2\2\u0326\u0327\3\2\2\2\u0327q\3\2\2\2\u0328\u0326"+
		"\3\2\2\2\u0329\u032f\7:\2\2\u032a\u032f\7;\2\2\u032b\u032f\5\u0136\u009c"+
		"\2\u032c\u032f\7<\2\2\u032d\u032f\7=\2\2\u032e\u0329\3\2\2\2\u032e\u032a"+
		"\3\2\2\2\u032e\u032b\3\2\2\2\u032e\u032c\3\2\2\2\u032e\u032d\3\2\2\2\u032f"+
		"s\3\2\2\2\u0330\u0331\7\4\2\2\u0331\u0336\5v<\2\u0332\u0333\7\6\2\2\u0333"+
		"\u0335\5v<\2\u0334\u0332\3\2\2\2\u0335\u0338\3\2\2\2\u0336\u0334\3\2\2"+
		"\2\u0336\u0337\3\2\2\2\u0337\u0339\3\2\2\2\u0338\u0336\3\2\2\2\u0339\u033a"+
		"\7\5\2\2\u033au\3\2\2\2\u033b\u033e\5x=\2\u033c\u033e\5\u0084C\2\u033d"+
		"\u033b\3\2\2\2\u033d\u033c\3\2\2\2\u033ew\3\2\2\2\u033f\u0343\5\u013a"+
		"\u009e\2\u0340\u0342\7\7\2\2\u0341\u0340\3\2\2\2\u0342\u0345\3\2\2\2\u0343"+
		"\u0341\3\2\2\2\u0343\u0344\3\2\2\2\u0344y\3\2\2\2\u0345\u0343\3\2\2\2"+
		"\u0346\u0347\5~@\2\u0347\u0348\5\u0142\u00a2\2\u0348\u0349\7M\2\2\u0349"+
		"\u034a\5\u0142\u00a2\2\u034a\u034b\5\u0082B\2\u034b{\3\2\2\2\u034c\u034d"+
		"\5\u0080A\2\u034d\u034e\5\u0142\u00a2\2\u034e\u034f\7M\2\2\u034f\u0350"+
		"\5\u0142\u00a2\2\u0350\u0351\5\u0082B\2\u0351}\3\2\2\2\u0352\u0353\5^"+
		"\60\2\u0353\177\3\2\2\2\u0354\u0357\5^\60\2\u0355\u0357\5@!\2\u0356\u0354"+
		"\3\2\2\2\u0356\u0355\3\2\2\2\u0357\u0081\3\2\2\2\u0358\u035b\5\u009eP"+
		"\2\u0359\u035b\5\u00e4s\2\u035a\u0358\3\2\2\2\u035a\u0359\3\2\2\2\u035b"+
		"\u0083\3\2\2\2\u035c\u0365\7R\2\2\u035d\u0361\5\u0142\u00a2\2\u035e\u035f"+
		"\5`\61\2\u035f\u0360\5\u0142\u00a2\2\u0360\u0362\3\2\2\2\u0361\u035e\3"+
		"\2\2\2\u0361\u0362\3\2\2\2\u0362\u0363\3\2\2\2\u0363\u0364\7M\2\2\u0364"+
		"\u0366\3\2\2\2\u0365\u035d\3\2\2\2\u0365\u0366\3\2\2\2\u0366\u0368\3\2"+
		"\2\2\u0367\u0369\5\u0144\u00a3\2\u0368\u0367\3\2\2\2\u0368\u0369\3\2\2"+
		"\2\u0369\u036a\3\2\2\2\u036a\u036b\5\u0088E\2\u036b\u036c\7S\2\2\u036c"+
		"\u0085\3\2\2\2\u036d\u0370\5\u0084C\2\u036e\u0370\5z>\2\u036f\u036d\3"+
		"\2\2\2\u036f\u036e\3\2\2\2\u0370\u0087\3\2\2\2\u0371\u0373\5\u008aF\2"+
		"\u0372\u0371\3\2\2\2\u0372\u0373\3\2\2\2\u0373\u0089\3\2\2\2\u0374\u037a"+
		"\5\u00a0Q\2\u0375\u0376\5\u0144\u00a3\2\u0376\u0377\5\u00a0Q\2\u0377\u0379"+
		"\3\2\2\2\u0378\u0375\3\2\2\2\u0379\u037c\3\2\2\2\u037a\u0378\3\2\2\2\u037a"+
		"\u037b\3\2\2\2\u037b\u037e\3\2\2\2\u037c\u037a\3\2\2\2\u037d\u037f\5\u0144"+
		"\u00a3\2\u037e\u037d\3\2\2\2\u037e\u037f\3\2\2\2\u037f\u008b\3\2\2\2\u0380"+
		"\u0386\5\u008eH\2\u0381\u0382\5\u0142\u00a2\2\u0382\u0383\5\u008eH\2\u0383"+
		"\u0385\3\2\2\2\u0384\u0381\3\2\2\2\u0385\u0388\3\2\2\2\u0386\u0384\3\2"+
		"\2\2\u0386\u0387\3\2\2\2\u0387\u0389\3\2\2\2\u0388\u0386\3\2\2\2\u0389"+
		"\u038a\5\u0142\u00a2\2\u038a\u038c\3\2\2\2\u038b\u0380\3\2\2\2\u038b\u038c"+
		"\3\2\2\2\u038c\u008d\3\2\2\2\u038d\u038e\7~\2\2\u038e\u0396\5\u0092J\2"+
		"\u038f\u0390\5\u0142\u00a2\2\u0390\u0392\7P\2\2\u0391\u0393\5\u0090I\2"+
		"\u0392\u0391\3\2\2\2\u0392\u0393\3\2\2\2\u0393\u0394\3\2\2\2\u0394\u0395"+
		"\5\u0140\u00a1\2\u0395\u0397\3\2\2\2\u0396\u038f\3\2\2\2\u0396\u0397\3"+
		"\2\2\2\u0397\u008f\3\2\2\2\u0398\u039b\5\u0094K\2\u0399\u039b\5\u009a"+
		"N\2\u039a\u0398\3\2\2\2\u039a\u0399\3\2\2\2\u039b\u0091\3\2\2\2\u039c"+
		"\u039d\5n8\2\u039d\u0093\3\2\2\2\u039e\u03a3\5\u0096L\2\u039f\u03a0\7"+
		"W\2\2\u03a0\u03a2\5\u0096L\2\u03a1\u039f\3\2\2\2\u03a2\u03a5\3\2\2\2\u03a3"+
		"\u03a1\3\2\2\2\u03a3\u03a4\3\2\2\2\u03a4\u0095\3\2\2\2\u03a5\u03a3\3\2"+
		"\2\2\u03a6\u03a7\5\u0098M\2\u03a7\u03a8\5\u0142\u00a2\2\u03a8\u03a9\7"+
		"Y\2\2\u03a9\u03aa\5\u0142\u00a2\2\u03aa\u03ab\5\u009aN\2\u03ab\u0097\3"+
		"\2\2\2\u03ac\u03af\5\u013a\u009e\2\u03ad\u03af\5\u013e\u00a0\2\u03ae\u03ac"+
		"\3\2\2\2\u03ae\u03ad\3\2\2\2\u03af\u0099\3\2\2\2\u03b0\u03b4\5\u009cO"+
		"\2\u03b1\u03b4\5\u008eH\2\u03b2\u03b4\5\u00e8u\2\u03b3\u03b0\3\2\2\2\u03b3"+
		"\u03b1\3\2\2\2\u03b3\u03b2\3\2\2\2\u03b4\u009b\3\2\2\2\u03b5\u03c1\7T"+
		"\2\2\u03b6\u03bb\5\u009aN\2\u03b7\u03b8\7W\2\2\u03b8\u03ba\5\u009aN\2"+
		"\u03b9\u03b7\3\2\2\2\u03ba\u03bd\3\2\2\2\u03bb\u03b9\3\2\2\2\u03bb\u03bc"+
		"\3\2\2\2\u03bc\u03bf\3\2\2\2\u03bd\u03bb\3\2\2\2\u03be\u03c0\7W\2\2\u03bf"+
		"\u03be\3\2\2\2\u03bf\u03c0\3\2\2\2\u03c0\u03c2\3\2\2\2\u03c1\u03b6\3\2"+
		"\2\2\u03c1\u03c2\3\2\2\2\u03c2\u03c3\3\2\2\2\u03c3\u03c4\7U\2\2\u03c4"+
		"\u009d\3\2\2\2\u03c5\u03c7\7R\2\2\u03c6\u03c8\5\u0144\u00a3\2\u03c7\u03c6"+
		"\3\2\2\2\u03c7\u03c8\3\2\2\2\u03c8\u03c9\3\2\2\2\u03c9\u03ca\5\u0088E"+
		"\2\u03ca\u03cb\7S\2\2\u03cb\u009f\3\2\2\2\u03cc\u03cf\5\u00a2R\2\u03cd"+
		"\u03cf\5\u00bc_\2\u03ce\u03cc\3\2\2\2\u03ce\u03cd\3\2\2\2\u03cf\u00a1"+
		"\3\2\2\2\u03d0\u03d1\6R\4\2\u03d1\u03d2\5\u00a4S\2\u03d2\u00a3\3\2\2\2"+
		"\u03d3\u03d4\5\22\n\2\u03d4\u03df\5\u0142\u00a2\2\u03d5\u03d7\5L\'\2\u03d6"+
		"\u03d5\3\2\2\2\u03d6\u03d7\3\2\2\2\u03d7\u03d8\3\2\2\2\u03d8\u03e0\5<"+
		"\37\2\u03d9\u03da\5\u00a6T\2\u03da\u03db\5\u0142\u00a2\2\u03db\u03dc\7"+
		"Y\2\2\u03dc\u03dd\5\u0142\u00a2\2\u03dd\u03de\5B\"\2\u03de\u03e0\3\2\2"+
		"\2\u03df\u03d6\3\2\2\2\u03df\u03d9\3\2\2\2\u03e0\u03e5\3\2\2\2\u03e1\u03e2"+
		"\5L\'\2\u03e2\u03e3\5<\37\2\u03e3\u03e5\3\2\2\2\u03e4\u03d3\3\2\2\2\u03e4"+
		"\u03e1\3\2\2\2\u03e5\u00a5\3\2\2\2\u03e6\u03e7\7P\2\2\u03e7\u03ec\5\u00a8"+
		"U\2\u03e8\u03e9\7W\2\2\u03e9\u03eb\5\u00a8U\2\u03ea\u03e8\3\2\2\2\u03eb"+
		"\u03ee\3\2\2\2\u03ec\u03ea\3\2\2\2\u03ec\u03ed\3\2\2\2\u03ed\u03ef\3\2"+
		"\2\2\u03ee\u03ec\3\2\2\2\u03ef\u03f0\5\u0140\u00a1\2\u03f0\u00a7\3\2\2"+
		"\2\u03f1\u03f3\5L\'\2\u03f2\u03f1\3\2\2\2\u03f2\u03f3\3\2\2\2\u03f3\u03f4"+
		"\3\2\2\2\u03f4\u03f5\5@!\2\u03f5\u00a9\3\2\2\2\u03f6\u03f7\7P\2\2\u03f7"+
		"\u03fa\5@!\2\u03f8\u03f9\7W\2\2\u03f9\u03fb\5@!\2\u03fa\u03f8\3\2\2\2"+
		"\u03fb\u03fc\3\2\2\2\u03fc\u03fa\3\2\2\2\u03fc\u03fd\3\2\2\2\u03fd\u03fe"+
		"\3\2\2\2\u03fe\u03ff\5\u0140\u00a1\2\u03ff\u00ab\3\2\2\2\u0400\u0403\5"+
		"\u00aeX\2\u0401\u0403\5\u00b0Y\2\u0402\u0400\3\2\2\2\u0402\u0401\3\2\2"+
		"\2\u0403\u00ad\3\2\2\2\u0404\u0405\7 \2\2\u0405\u0406\5\u00dco\2\u0406"+
		"\u0407\5\u0142\u00a2\2\u0407\u0410\5\u00bc_\2\u0408\u040b\5\u0142\u00a2"+
		"\2\u0409\u040b\5\u0144\u00a3\2\u040a\u0408\3\2\2\2\u040a\u0409\3\2\2\2"+
		"\u040b\u040c\3\2\2\2\u040c\u040d\7\32\2\2\u040d\u040e\5\u0142\u00a2\2"+
		"\u040e\u040f\5\u00bc_\2\u040f\u0411\3\2\2\2\u0410\u040a\3\2\2\2\u0410"+
		"\u0411\3\2\2\2\u0411\u00af\3\2\2\2\u0412\u0413\7\60\2\2\u0413\u0414\5"+
		"\u00dco\2\u0414\u0415\5\u0142\u00a2\2\u0415\u0416\7R\2\2\u0416\u041e\5"+
		"\u0142\u00a2\2\u0417\u0419\5\u00caf\2\u0418\u0417\3\2\2\2\u0419\u041a"+
		"\3\2\2\2\u041a\u0418\3\2\2\2\u041a\u041b\3\2\2\2\u041b\u041c\3\2\2\2\u041c"+
		"\u041d\5\u0142\u00a2\2\u041d\u041f\3\2\2\2\u041e\u0418\3\2\2\2\u041e\u041f"+
		"\3\2\2\2\u041f\u0420\3\2\2\2\u0420\u0421\7S\2\2\u0421\u00b1\3\2\2\2\u0422"+
		"\u0423\7\37\2\2\u0423\u0424\7P\2\2\u0424\u0425\5\u00ceh\2\u0425\u0426"+
		"\5\u0140\u00a1\2\u0426\u0427\5\u0142\u00a2\2\u0427\u0428\5\u00bc_\2\u0428"+
		"\u0436\3\2\2\2\u0429\u042a\79\2\2\u042a\u042b\5\u00dco\2\u042b\u042c\5"+
		"\u0142\u00a2\2\u042c\u042d\5\u00bc_\2\u042d\u0436\3\2\2\2\u042e\u042f"+
		"\7\31\2\2\u042f\u0430\5\u0142\u00a2\2\u0430\u0431\5\u00bc_\2\u0431\u0432"+
		"\5\u0142\u00a2\2\u0432\u0433\79\2\2\u0433\u0434\5\u00dco\2\u0434\u0436"+
		"\3\2\2\2\u0435\u0422\3\2\2\2\u0435\u0429\3\2\2\2\u0435\u042e\3\2\2\2\u0436"+
		"\u00b3\3\2\2\2\u0437\u0439\7\27\2\2\u0438\u043a\5\u013a\u009e\2\u0439"+
		"\u0438\3\2\2\2\u0439\u043a\3\2\2\2\u043a\u00b5\3\2\2\2\u043b\u043d\7\22"+
		"\2\2\u043c\u043e\5\u013a\u009e\2\u043d\u043c\3\2\2\2\u043d\u043e\3\2\2"+
		"\2\u043e\u00b7\3\2\2\2\u043f\u0441\7\66\2\2\u0440\u0442\5\u00c4c\2\u0441"+
		"\u0440\3\2\2\2\u0441\u0442\3\2\2\2\u0442\u0443\3\2\2\2\u0443\u0444\5\u0142"+
		"\u00a2\2\u0444\u044a\5\u009eP\2\u0445\u0446\5\u0142\u00a2\2\u0446\u0447"+
		"\5\u00be`\2\u0447\u0449\3\2\2\2\u0448\u0445\3\2\2\2\u0449\u044c\3\2\2"+
		"\2\u044a\u0448\3\2\2\2\u044a\u044b\3\2\2\2\u044b\u0450\3\2\2\2\u044c\u044a"+
		"\3\2\2\2\u044d\u044e\5\u0142\u00a2\2\u044e\u044f\5\u00c2b\2\u044f\u0451"+
		"\3\2\2\2\u0450\u044d\3\2\2\2\u0450\u0451\3\2\2\2\u0451\u00b9\3\2\2\2\u0452"+
		"\u0453\7\21\2\2\u0453\u0459\5\u00e8u\2\u0454\u0455\5\u0142\u00a2\2\u0455"+
		"\u0456\t\6\2\2\u0456\u0457\5\u0142\u00a2\2\u0457\u0458\5\u00e8u\2\u0458"+
		"\u045a\3\2\2\2\u0459\u0454\3\2\2\2\u0459\u045a\3\2\2\2\u045a\u00bb\3\2"+
		"\2\2\u045b\u0476\5\u009eP\2\u045c\u0476\5\u00acW\2\u045d\u0476\5\u00b2"+
		"Z\2\u045e\u0476\5\u00b8]\2\u045f\u0460\7\61\2\2\u0460\u0461\5\u00dco\2"+
		"\u0461\u0462\5\u0142\u00a2\2\u0462\u0463\5\u009eP\2\u0463\u0476\3\2\2"+
		"\2\u0464\u0466\7,\2\2\u0465\u0467\5\u00e8u\2\u0466\u0465\3\2\2\2\u0466"+
		"\u0467\3\2\2\2\u0467\u0476\3\2\2\2\u0468\u0469\7\63\2\2\u0469\u0476\5"+
		"\u00e8u\2\u046a\u0476\5\u00b6\\\2\u046b\u0476\5\u00b4[\2\u046c\u046d\5"+
		"\u013a\u009e\2\u046d\u046e\7_\2\2\u046e\u046f\5\u0142\u00a2\2\u046f\u0470"+
		"\5\u00bc_\2\u0470\u0476\3\2\2\2\u0471\u0476\5\u00ba^\2\u0472\u0476\5\u00a2"+
		"R\2\u0473\u0476\5\u00e4s\2\u0474\u0476\7V\2\2\u0475\u045b\3\2\2\2\u0475"+
		"\u045c\3\2\2\2\u0475\u045d\3\2\2\2\u0475\u045e\3\2\2\2\u0475\u045f\3\2"+
		"\2\2\u0475\u0464\3\2\2\2\u0475\u0468\3\2\2\2\u0475\u046a\3\2\2\2\u0475"+
		"\u046b\3\2\2\2\u0475\u046c\3\2\2\2\u0475\u0471\3\2\2\2\u0475\u0472\3\2"+
		"\2\2\u0475\u0473\3\2\2\2\u0475\u0474\3\2\2\2\u0476\u00bd\3\2\2\2\u0477"+
		"\u0478\7\24\2\2\u0478\u0479\7P\2\2\u0479\u047b\5\34\17\2\u047a\u047c\5"+
		"\u00c0a\2\u047b\u047a\3\2\2\2\u047b\u047c\3\2\2\2\u047c\u047d\3\2\2\2"+
		"\u047d\u047e\5\u013a\u009e\2\u047e\u047f\5\u0140\u00a1\2\u047f\u0480\5"+
		"\u0142\u00a2\2\u0480\u0481\5\u009eP\2\u0481\u00bf\3\2\2\2\u0482\u0487"+
		"\5n8\2\u0483\u0484\7m\2\2\u0484\u0486\5n8\2\u0485\u0483\3\2\2\2\u0486"+
		"\u0489\3\2\2\2\u0487\u0485\3\2\2\2\u0487\u0488\3\2\2\2\u0488\u00c1\3\2"+
		"\2\2\u0489\u0487\3\2\2\2\u048a\u048b\7\36\2\2\u048b\u048c\5\u0142\u00a2"+
		"\2\u048c\u048d\5\u009eP\2\u048d\u00c3\3\2\2\2\u048e\u048f\7P\2\2\u048f"+
		"\u0490\5\u0142\u00a2\2\u0490\u0492\5\u00c6d\2\u0491\u0493\5\u0144\u00a3"+
		"\2\u0492\u0491\3\2\2\2\u0492\u0493\3\2\2\2\u0493\u0494\3\2\2\2\u0494\u0495"+
		"\5\u0140\u00a1\2\u0495\u00c5\3\2\2\2\u0496\u049c\5\u00c8e\2\u0497\u0498"+
		"\5\u0144\u00a3\2\u0498\u0499\5\u00c8e\2\u0499\u049b\3\2\2\2\u049a\u0497"+
		"\3\2\2\2\u049b\u049e\3\2\2\2\u049c\u049a\3\2\2\2\u049c\u049d\3\2\2\2\u049d"+
		"\u00c7\3\2\2\2\u049e\u049c\3\2\2\2\u049f\u04a2\5\u00a2R\2\u04a0\u04a2"+
		"\5\u00e8u\2\u04a1\u049f\3\2\2\2\u04a1\u04a0\3\2\2\2\u04a2\u00c9\3\2\2"+
		"\2\u04a3\u04a9\5\u00ccg\2\u04a4\u04a5\5\u0142\u00a2\2\u04a5\u04a6\5\u00cc"+
		"g\2\u04a6\u04a8\3\2\2\2\u04a7\u04a4\3\2\2\2\u04a8\u04ab\3\2\2\2\u04a9"+
		"\u04a7\3\2\2\2\u04a9\u04aa\3\2\2\2\u04aa\u04ac\3\2\2\2\u04ab\u04a9\3\2"+
		"\2\2\u04ac\u04ad\5\u0142\u00a2\2\u04ad\u04ae\5\u008aF\2\u04ae\u00cb\3"+
		"\2\2\2\u04af\u04b0\7\23\2\2\u04b0\u04b1\5\u00e8u\2\u04b1\u04b2\7_\2\2"+
		"\u04b2\u04b6\3\2\2\2\u04b3\u04b4\7\30\2\2\u04b4\u04b6\7_\2\2\u04b5\u04af"+
		"\3\2\2\2\u04b5\u04b3\3\2\2\2\u04b6\u00cd\3\2\2\2\u04b7\u04ba\5\u00d0i"+
		"\2\u04b8\u04ba\5\u00d2j\2\u04b9\u04b7\3\2\2\2\u04b9\u04b8\3\2\2\2\u04ba"+
		"\u00cf\3\2\2\2\u04bb\u04bd\5\34\17\2\u04bc\u04be\5L\'\2\u04bd\u04bc\3"+
		"\2\2\2\u04bd\u04be\3\2\2\2\u04be\u04bf\3\2\2\2\u04bf\u04c0\5@!\2\u04c0"+
		"\u04c1\t\7\2\2\u04c1\u04c2\5\u00e8u\2\u04c2\u00d1\3\2\2\2\u04c3\u04c5"+
		"\5\u00d4k\2\u04c4\u04c3\3\2\2\2\u04c4\u04c5\3\2\2\2\u04c5\u04c6\3\2\2"+
		"\2\u04c6\u04c8\7V\2\2\u04c7\u04c9\5\u00e8u\2\u04c8\u04c7\3\2\2\2\u04c8"+
		"\u04c9\3\2\2\2\u04c9\u04ca\3\2\2\2\u04ca\u04cc\7V\2\2\u04cb\u04cd\5\u00d6"+
		"l\2\u04cc\u04cb\3\2\2\2\u04cc\u04cd\3\2\2\2\u04cd\u00d3\3\2\2\2\u04ce"+
		"\u04d1\5\u00a2R\2\u04cf\u04d1\5\u00dep\2\u04d0\u04ce\3\2\2\2\u04d0\u04cf"+
		"\3\2\2\2\u04d1\u00d5\3\2\2\2\u04d2\u04d3\5\u00dep\2\u04d3\u00d7\3\2\2"+
		"\2\u04d4\u04d5\7P\2\2\u04d5\u04d6\5L\'\2\u04d6\u04d7\5\u0140\u00a1\2\u04d7"+
		"\u00d9\3\2\2\2\u04d8\u04d9\5\u00dco\2\u04d9\u00db\3\2\2\2\u04da\u04db"+
		"\7P\2\2\u04db\u04dc\5\u00e2r\2\u04dc\u04dd\5\u0140\u00a1\2\u04dd\u00dd"+
		"\3\2\2\2\u04de\u04e3\5\u00e0q\2\u04df\u04e0\7W\2\2\u04e0\u04e2\5\u00e0"+
		"q\2\u04e1\u04df\3\2\2\2\u04e2\u04e5\3\2\2\2\u04e3\u04e1\3\2\2\2\u04e3"+
		"\u04e4\3\2\2\2\u04e4\u00df\3\2\2\2\u04e5\u04e3\3\2\2\2\u04e6\u04e8\7j"+
		"\2\2\u04e7\u04e6\3\2\2\2\u04e7\u04e8\3\2\2\2\u04e8\u04e9\3\2\2\2\u04e9"+
		"\u04ea\5\u00e8u\2\u04ea\u00e1\3\2\2\2\u04eb\u04ee\5\u00e4s\2\u04ec\u04ee"+
		"\5|?\2\u04ed\u04eb\3\2\2\2\u04ed\u04ec\3\2\2\2\u04ee\u00e3\3\2\2\2\u04ef"+
		"\u04f0\5\u00ecw\2\u04f0\u00e5\3\2\2\2\u04f1\u04f3\5\u00f0y\2\u04f2\u04f4"+
		"\t\b\2\2\u04f3\u04f2\3\2\2\2\u04f3\u04f4\3\2\2\2\u04f4\u00e7\3\2\2\2\u04f5"+
		"\u04f6\bu\1\2\u04f6\u04f7\5\u00d8m\2\u04f7\u04f8\5\u00eav\2\u04f8\u0507"+
		"\3\2\2\2\u04f9\u0507\5\u00e6t\2\u04fa\u04fb\t\t\2\2\u04fb\u04fc\5\u0142"+
		"\u00a2\2\u04fc\u04fd\5\u00e8u\24\u04fd\u0507\3\2\2\2\u04fe\u04ff\t\n\2"+
		"\2\u04ff\u0507\5\u00e8u\22\u0500\u0501\5\u00aaV\2\u0501\u0502\5\u0142"+
		"\u00a2\2\u0502\u0503\7Y\2\2\u0503\u0504\5\u0142\u00a2\2\u0504\u0505\5"+
		"\u00e4s\2\u0505\u0507\3\2\2\2\u0506\u04f5\3\2\2\2\u0506\u04f9\3\2\2\2"+
		"\u0506\u04fa\3\2\2\2\u0506\u04fe\3\2\2\2\u0506\u0500\3\2\2\2\u0507\u0576"+
		"\3\2\2\2\u0508\u0509\f\23\2\2\u0509\u050a\7H\2\2\u050a\u050b\5\u0142\u00a2"+
		"\2\u050b\u050c\5\u00e8u\24\u050c\u0575\3\2\2\2\u050d\u050e\f\21\2\2\u050e"+
		"\u050f\5\u0142\u00a2\2\u050f\u0510\t\13\2\2\u0510\u0511\5\u0142\u00a2"+
		"\2\u0511\u0512\5\u00e8u\22\u0512\u0575\3\2\2\2\u0513\u0514\f\20\2\2\u0514"+
		"\u0515\t\f\2\2\u0515\u0516\5\u0142\u00a2\2\u0516\u0517\5\u00e8u\21\u0517"+
		"\u0575\3\2\2\2\u0518\u0519\f\17\2\2\u0519\u0524\5\u0142\u00a2\2\u051a"+
		"\u051b\7[\2\2\u051b\u0522\7[\2\2\u051c\u051d\7Z\2\2\u051d\u051e\7Z\2\2"+
		"\u051e\u0522\7Z\2\2\u051f\u0520\7Z\2\2\u0520\u0522\7Z\2\2\u0521\u051a"+
		"\3\2\2\2\u0521\u051c\3\2\2\2\u0521\u051f\3\2\2\2\u0522\u0525\3\2\2\2\u0523"+
		"\u0525\t\r\2\2\u0524\u0521\3\2\2\2\u0524\u0523\3\2\2\2\u0525\u0526\3\2"+
		"\2\2\u0526\u0527\5\u0142\u00a2\2\u0527\u0528\5\u00e8u\20\u0528\u0575\3"+
		"\2\2\2\u0529\u052a\f\r\2\2\u052a\u052b\5\u0142\u00a2\2\u052b\u052c\t\16"+
		"\2\2\u052c\u052d\5\u0142\u00a2\2\u052d\u052e\5\u00e8u\16\u052e\u0575\3"+
		"\2\2\2\u052f\u0530\f\f\2\2\u0530\u0531\5\u0142\u00a2\2\u0531\u0532\t\17"+
		"\2\2\u0532\u0533\5\u0142\u00a2\2\u0533\u0534\5\u00e8u\r\u0534\u0575\3"+
		"\2\2\2\u0535\u0536\f\13\2\2\u0536\u0537\5\u0142\u00a2\2\u0537\u0538\t"+
		"\20\2\2\u0538\u0539\5\u0142\u00a2\2\u0539\u053a\5\u00e8u\f\u053a\u0575"+
		"\3\2\2\2\u053b\u053c\f\n\2\2\u053c\u053d\5\u0142\u00a2\2\u053d\u053e\7"+
		"l\2\2\u053e\u053f\5\u0142\u00a2\2\u053f\u0540\5\u00e8u\13\u0540\u0575"+
		"\3\2\2\2\u0541\u0542\f\t\2\2\u0542\u0543\5\u0142\u00a2\2\u0543\u0544\7"+
		"n\2\2\u0544\u0545\5\u0142\u00a2\2\u0545\u0546\5\u00e8u\n\u0546\u0575\3"+
		"\2\2\2\u0547\u0548\f\b\2\2\u0548\u0549\5\u0142\u00a2\2\u0549\u054a\7m"+
		"\2\2\u054a\u054b\5\u0142\u00a2\2\u054b\u054c\5\u00e8u\t\u054c\u0575\3"+
		"\2\2\2\u054d\u054e\f\7\2\2\u054e\u054f\5\u0142\u00a2\2\u054f\u0550\7d"+
		"\2\2\u0550\u0551\5\u0142\u00a2\2\u0551\u0552\5\u00e8u\b\u0552\u0575\3"+
		"\2\2\2\u0553\u0554\f\6\2\2\u0554\u0555\5\u0142\u00a2\2\u0555\u0556\7e"+
		"\2\2\u0556\u0557\5\u0142\u00a2\2\u0557\u0558\5\u00e8u\7\u0558\u0575\3"+
		"\2\2\2\u0559\u055a\f\5\2\2\u055a\u0564\5\u0142\u00a2\2\u055b\u055c\7^"+
		"\2\2\u055c\u055d\5\u0142\u00a2\2\u055d\u055e\5\u00e8u\2\u055e\u055f\5"+
		"\u0142\u00a2\2\u055f\u0560\7_\2\2\u0560\u0561\5\u0142\u00a2\2\u0561\u0565"+
		"\3\2\2\2\u0562\u0563\7C\2\2\u0563\u0565\5\u0142\u00a2\2\u0564\u055b\3"+
		"\2\2\2\u0564\u0562\3\2\2\2\u0565\u0566\3\2\2\2\u0566\u0567\5\u00e8u\5"+
		"\u0567\u0575\3\2\2\2\u0568\u0569\f\16\2\2\u0569\u056a\5\u0142\u00a2\2"+
		"\u056a\u056b\t\21\2\2\u056b\u056c\5\u0142\u00a2\2\u056c\u056d\5L\'\2\u056d"+
		"\u0575\3\2\2\2\u056e\u056f\f\3\2\2\u056f\u0570\5\u0142\u00a2\2\u0570\u0571"+
		"\t\22\2\2\u0571\u0572\5\u0142\u00a2\2\u0572\u0573\5\u00e2r\2\u0573\u0575"+
		"\3\2\2\2\u0574\u0508\3\2\2\2\u0574\u050d\3\2\2\2\u0574\u0513\3\2\2\2\u0574"+
		"\u0518\3\2\2\2\u0574\u0529\3\2\2\2\u0574\u052f\3\2\2\2\u0574\u0535\3\2"+
		"\2\2\u0574\u053b\3\2\2\2\u0574\u0541\3\2\2\2\u0574\u0547\3\2\2\2\u0574"+
		"\u054d\3\2\2\2\u0574\u0553\3\2\2\2\u0574\u0559\3\2\2\2\u0574\u0568\3\2"+
		"\2\2\u0574\u056e\3\2\2\2\u0575\u0578\3\2\2\2\u0576\u0574\3\2\2\2\u0576"+
		"\u0577\3\2\2\2\u0577\u00e9\3\2\2\2\u0578\u0576\3\2\2\2\u0579\u057a\5\u00d8"+
		"m\2\u057a\u057b\5\u00eav\2\u057b\u0584\3\2\2\2\u057c\u0584\5\u00e6t\2"+
		"\u057d\u057e\t\t\2\2\u057e\u057f\5\u0142\u00a2\2\u057f\u0580\5\u00eav"+
		"\2\u0580\u0584\3\2\2\2\u0581\u0582\t\n\2\2\u0582\u0584\5\u00eav\2\u0583"+
		"\u0579\3\2\2\2\u0583\u057c\3\2\2\2\u0583\u057d\3\2\2\2\u0583\u0581\3\2"+
		"\2\2\u0584\u00eb\3\2\2\2\u0585\u0589\5\u00e8u\2\u0586\u0587\6w\24\3\u0587"+
		"\u058a\5\u0128\u0095\2\u0588\u058a\3\2\2\2\u0589\u0586\3\2\2\2\u0589\u0588"+
		"\3\2\2\2\u058a\u058e\3\2\2\2\u058b\u058d\5\u00eex\2\u058c\u058b\3\2\2"+
		"\2\u058d\u0590\3\2\2\2\u058e\u058c\3\2\2\2\u058e\u058f\3\2\2\2\u058f\u00ed"+
		"\3\2\2\2\u0590\u058e\3\2\2\2\u0591\u0598\5\u0102\u0082\2\u0592\u0594\5"+
		"\u00f2z\2\u0593\u0592\3\2\2\2\u0594\u0595\3\2\2\2\u0595\u0593\3\2\2\2"+
		"\u0595\u0596\3\2\2\2\u0596\u0599\3\2\2\2\u0597\u0599\5\u0128\u0095\2\u0598"+
		"\u0593\3\2\2\2\u0598\u0597\3\2\2\2\u0598\u0599\3\2\2\2\u0599\u00ef\3\2"+
		"\2\2\u059a\u059e\5\u00fc\177\2\u059b\u059c\6y\25\2\u059c\u059e\7-\2\2"+
		"\u059d\u059a\3\2\2\2\u059d\u059b\3\2\2\2\u059e\u05a4\3\2\2\2\u059f\u05a0"+
		"\5\u00f2z\2\u05a0\u05a1\by\1\2\u05a1\u05a3\3\2\2\2\u05a2\u059f\3\2\2\2"+
		"\u05a3\u05a6\3\2\2\2\u05a4\u05a2\3\2\2\2\u05a4\u05a5\3\2\2\2\u05a5\u00f1"+
		"\3\2\2\2\u05a6\u05a4\3\2\2\2\u05a7\u05c0\5\u0142\u00a2\2\u05a8\u05a9\t"+
		"\23\2\2\u05a9\u05ac\5\u0142\u00a2\2\u05aa\u05ad\7~\2\2\u05ab\u05ad\5\u0122"+
		"\u0092\2\u05ac\u05aa\3\2\2\2\u05ac\u05ab\3\2\2\2\u05ac\u05ad\3\2\2\2\u05ad"+
		"\u05b3\3\2\2\2\u05ae\u05af\7D\2\2\u05af\u05b3\5\u0142\u00a2\2\u05b0\u05b1"+
		"\7E\2\2\u05b1\u05b3\5\u0142\u00a2\2\u05b2\u05a8\3\2\2\2\u05b2\u05ae\3"+
		"\2\2\2\u05b2\u05b0\3\2\2\2\u05b3\u05b4\3\2\2\2\u05b4\u05b5\5\u00f4{\2"+
		"\u05b5\u05b6\bz\1\2\u05b6\u05c1\3\2\2\2\u05b7\u05b8\7X\2\2\u05b8\u05b9"+
		"\5\u0142\u00a2\2\u05b9\u05ba\7\'\2\2\u05ba\u05bb\5\u0118\u008d\2\u05bb"+
		"\u05bc\bz\1\2\u05bc\u05c1\3\2\2\2\u05bd\u05be\5\u0086D\2\u05be\u05bf\b"+
		"z\1\2\u05bf\u05c1\3\2\2\2\u05c0\u05b2\3\2\2\2\u05c0\u05b7\3\2\2\2\u05c0"+
		"\u05bd\3\2\2\2\u05c1\u05cc\3\2\2\2\u05c2\u05c3\5\u0126\u0094\2\u05c3\u05c4"+
		"\bz\1\2\u05c4\u05cc\3\2\2\2\u05c5\u05c6\5\u00f8}\2\u05c6\u05c7\bz\1\2"+
		"\u05c7\u05cc\3\2\2\2\u05c8\u05c9\5\u00fa~\2\u05c9\u05ca\bz\1\2\u05ca\u05cc"+
		"\3\2\2\2\u05cb\u05a7\3\2\2\2\u05cb\u05c2\3\2\2\2\u05cb\u05c5\3\2\2\2\u05cb"+
		"\u05c8\3\2\2\2\u05cc\u00f3\3\2\2\2\u05cd\u05d2\5\u013a\u009e\2\u05ce\u05d2"+
		"\5\u0136\u009c\2\u05cf\u05d2\5\u00f6|\2\u05d0\u05d2\5\u013e\u00a0\2\u05d1"+
		"\u05cd\3\2\2\2\u05d1\u05ce\3\2\2\2\u05d1\u05cf\3\2\2\2\u05d1\u05d0\3\2"+
		"\2\2\u05d2\u00f5\3\2\2\2\u05d3\u05d6\5\u00dan\2\u05d4\u05d6\5t;\2\u05d5"+
		"\u05d3\3\2\2\2\u05d5\u05d4\3\2\2\2\u05d6\u00f7\3\2\2\2\u05d7\u05d9\7^"+
		"\2\2\u05d8\u05d7\3\2\2\2\u05d8\u05d9\3\2\2\2\u05d9\u05da\3\2\2\2\u05da"+
		"\u05dc\7T\2\2\u05db\u05dd\5\u00dep\2\u05dc\u05db\3\2\2\2\u05dc\u05dd\3"+
		"\2\2\2\u05dd\u05de\3\2\2\2\u05de\u05df\7U\2\2\u05df\u00f9\3\2\2\2\u05e0"+
		"\u05e2\7^\2\2\u05e1\u05e0\3\2\2\2\u05e1\u05e2\3\2\2\2\u05e2\u05e3\3\2"+
		"\2\2\u05e3\u05e6\7T\2\2\u05e4\u05e7\5\u010a\u0086\2\u05e5\u05e7\7_\2\2"+
		"\u05e6\u05e4\3\2\2\2\u05e6\u05e5\3\2\2\2\u05e7\u05e8\3\2\2\2\u05e8\u05e9"+
		"\7U\2\2\u05e9\u00fb\3\2\2\2\u05ea\u05ec\5\u013a\u009e\2\u05eb\u05ed\5"+
		"V,\2\u05ec\u05eb\3\2\2\2\u05ec\u05ed\3\2\2\2\u05ed\u05fc\3\2\2\2\u05ee"+
		"\u05fc\5r:\2\u05ef\u05fc\5t;\2\u05f0\u05f1\7\'\2\2\u05f1\u05f2\5\u0142"+
		"\u00a2\2\u05f2\u05f3\5\u0118\u008d\2\u05f3\u05fc\3\2\2\2\u05f4\u05fc\7"+
		"\62\2\2\u05f5\u05fc\7/\2\2\u05f6\u05fc\5\u00dan\2\u05f7\u05fc\5\u0086"+
		"D\2\u05f8\u05fc\5\u0104\u0083\2\u05f9\u05fc\5\u0106\u0084\2\u05fa\u05fc"+
		"\5\u013c\u009f\2\u05fb\u05ea\3\2\2\2\u05fb\u05ee\3\2\2\2\u05fb\u05ef\3"+
		"\2\2\2\u05fb\u05f0\3\2\2\2\u05fb\u05f4\3\2\2\2\u05fb\u05f5\3\2\2\2\u05fb"+
		"\u05f6\3\2\2\2\u05fb\u05f7\3\2\2\2\u05fb\u05f8\3\2\2\2\u05fb\u05f9\3\2"+
		"\2\2\u05fb\u05fa\3\2\2\2\u05fc\u00fd\3\2\2\2\u05fd\u0602\5\u013a\u009e"+
		"\2\u05fe\u0602\5r:\2\u05ff\u0602\5t;\2\u0600\u0602\5\u00dan\2\u0601\u05fd"+
		"\3\2\2\2\u0601\u05fe\3\2\2\2\u0601\u05ff\3\2\2\2\u0601\u0600\3\2\2\2\u0602"+
		"\u00ff\3\2\2\2\u0603\u0607\5\u013a\u009e\2\u0604\u0607\5r:\2\u0605\u0607"+
		"\5t;\2\u0606\u0603\3\2\2\2\u0606\u0604\3\2\2\2\u0606\u0605\3\2\2\2\u0607"+
		"\u0101\3\2\2\2\u0608\u060c\5\u013a\u009e\2\u0609\u060c\5r:\2\u060a\u060c"+
		"\5t;\2\u060b\u0608\3\2\2\2\u060b\u0609\3\2\2\2\u060b\u060a\3\2\2\2\u060c"+
		"\u0103\3\2\2\2\u060d\u060f\7T\2\2\u060e\u0610\5\u00dep\2\u060f\u060e\3"+
		"\2\2\2\u060f\u0610\3\2\2\2\u0610\u0612\3\2\2\2\u0611\u0613\7W\2\2\u0612"+
		"\u0611\3\2\2\2\u0612\u0613\3\2\2\2\u0613\u0614\3\2\2\2\u0614\u0615\7U"+
		"\2\2\u0615\u0105\3\2\2\2\u0616\u061c\7T\2\2\u0617\u0619\5\u0108\u0085"+
		"\2\u0618\u061a\7W\2\2\u0619\u0618\3\2\2\2\u0619\u061a\3\2\2\2\u061a\u061d"+
		"\3\2\2\2\u061b\u061d\7_\2\2\u061c\u0617\3\2\2\2\u061c\u061b\3\2\2\2\u061d"+
		"\u061e\3\2\2\2\u061e\u061f\7U\2\2\u061f\u0107\3\2\2\2\u0620\u0625\5\u010c"+
		"\u0087\2\u0621\u0622\7W\2\2\u0622\u0624\5\u010c\u0087\2\u0623\u0621\3"+
		"\2\2\2\u0624\u0627\3\2\2\2\u0625\u0623\3\2\2\2\u0625\u0626\3\2\2\2\u0626"+
		"\u0109\3\2\2\2\u0627\u0625\3\2\2\2\u0628\u062d\5\u010e\u0088\2\u0629\u062a"+
		"\7W\2\2\u062a\u062c\5\u010e\u0088\2\u062b\u0629\3\2\2\2\u062c\u062f\3"+
		"\2\2\2\u062d\u062b\3\2\2\2\u062d\u062e\3\2\2\2\u062e\u010b\3\2\2\2\u062f"+
		"\u062d\3\2\2\2\u0630\u0631\5\u0112\u008a\2\u0631\u0632\7_\2\2\u0632\u0633"+
		"\5\u0142\u00a2\2\u0633\u0634\5\u00e8u\2\u0634\u063b\3\2\2\2\u0635\u0636"+
		"\7j\2\2\u0636\u0637\7_\2\2\u0637\u0638\5\u0142\u00a2\2\u0638\u0639\5\u00e8"+
		"u\2\u0639\u063b\3\2\2\2\u063a\u0630\3\2\2\2\u063a\u0635\3\2\2\2\u063b"+
		"\u010d\3\2\2\2\u063c\u063d\5\u0114\u008b\2\u063d\u063e\7_\2\2\u063e\u063f"+
		"\5\u0142\u00a2\2\u063f\u0640\5\u00e8u\2\u0640\u0647\3\2\2\2\u0641\u0642"+
		"\7j\2\2\u0642\u0643\7_\2\2\u0643\u0644\5\u0142\u00a2\2\u0644\u0645\5\u00e8"+
		"u\2\u0645\u0647\3\2\2\2\u0646\u063c\3\2\2\2\u0646\u0641\3\2\2\2\u0647"+
		"\u010f\3\2\2\2\u0648\u0649\5\u0116\u008c\2\u0649\u064a\7_\2\2\u064a\u064b"+
		"\5\u0142\u00a2\2\u064b\u064c\5\u00e8u\2\u064c\u0653\3\2\2\2\u064d\u064e"+
		"\7j\2\2\u064e\u064f\7_\2\2\u064f\u0650\5\u0142\u00a2\2\u0650\u0651\5\u00e8"+
		"u\2\u0651\u0653\3\2\2\2\u0652\u0648\3\2\2\2\u0652\u064d\3\2\2\2\u0653"+
		"\u0111\3\2\2\2\u0654\u0657\5\u013e\u00a0\2\u0655\u0657\5\u00fc\177\2\u0656"+
		"\u0654\3\2\2\2\u0656\u0655\3\2\2\2\u0657\u0113\3\2\2\2\u0658\u065b\5\u013e"+
		"\u00a0\2\u0659\u065b\5\u00fe\u0080\2\u065a\u0658\3\2\2\2\u065a\u0659\3"+
		"\2\2\2\u065b\u0115\3\2\2\2\u065c\u065f\5\u013e\u00a0\2\u065d\u065f\5\u0100"+
		"\u0081\2\u065e\u065c\3\2\2\2\u065e\u065d\3\2\2\2\u065f\u0117\3\2\2\2\u0660"+
		"\u0670\5\u0120\u0091\2\u0661\u0662\5\u0142\u00a2\2\u0662\u0664\5\u0126"+
		"\u0094\2\u0663\u0665\5\u011e\u0090\2\u0664\u0663\3\2\2\2\u0664\u0665\3"+
		"\2\2\2\u0665\u0671\3\2\2\2\u0666\u0668\5\u011a\u008e\2\u0667\u0666\3\2"+
		"\2\2\u0668\u0669\3\2\2\2\u0669\u0667\3\2\2\2\u0669\u066a\3\2\2\2\u066a"+
		"\u066e\3\2\2\2\u066b\u066c\5\u0142\u00a2\2\u066c\u066d\5\u011c\u008f\2"+
		"\u066d\u066f\3\2\2\2\u066e\u066b\3\2\2\2\u066e\u066f\3\2\2\2\u066f\u0671"+
		"\3\2\2\2\u0670\u0661\3\2\2\2\u0670\u0667\3\2\2\2\u0671\u0119\3\2\2\2\u0672"+
		"\u0673\5\u008cG\2\u0673\u0675\7T\2\2\u0674\u0676\5\u00e8u\2\u0675\u0674"+
		"\3\2\2\2\u0675\u0676\3\2\2\2\u0676\u0677\3\2\2\2\u0677\u0678\7U\2\2\u0678"+
		"\u011b\3\2\2\2\u0679\u067a\7R\2\2\u067a\u067e\5\u0142\u00a2\2\u067b\u067c"+
		"\5D#\2\u067c\u067d\5\u0142\u00a2\2\u067d\u067f\3\2\2\2\u067e\u067b\3\2"+
		"\2\2\u067e\u067f\3\2\2\2\u067f\u0680\3\2\2\2\u0680\u0681\7S\2\2\u0681"+
		"\u011d\3\2\2\2\u0682\u0683\5*\26\2\u0683\u011f\3\2\2\2\u0684\u068a\5\u008c"+
		"G\2\u0685\u068b\5T+\2\u0686\u0688\5n8\2\u0687\u0689\5\u0124\u0093\2\u0688"+
		"\u0687\3\2\2\2\u0688\u0689\3\2\2\2\u0689\u068b\3\2\2\2\u068a\u0685\3\2"+
		"\2\2\u068a\u0686\3\2\2\2\u068b\u0121\3\2\2\2\u068c\u068d\7[\2\2\u068d"+
		"\u068e\5\u0142\u00a2\2\u068e\u068f\5&\24\2\u068f\u0690\5\u0142\u00a2\2"+
		"\u0690\u0691\7Z\2\2\u0691\u0123\3\2\2\2\u0692\u0693\7[\2\2\u0693\u0696"+
		"\7Z\2\2\u0694\u0696\5V,\2\u0695\u0692\3\2\2\2\u0695\u0694\3\2\2\2\u0696"+
		"\u0125\3\2\2\2\u0697\u0699\7P\2\2\u0698\u069a\5\u012c\u0097\2\u0699\u0698"+
		"\3\2\2\2\u0699\u069a\3\2\2\2\u069a\u069c\3\2\2\2\u069b\u069d\7W\2\2\u069c"+
		"\u069b\3\2\2\2\u069c\u069d\3\2\2\2\u069d\u069e\3\2\2\2\u069e\u069f\5\u0140"+
		"\u00a1\2\u069f\u0127\3\2\2\2\u06a0\u06a7\5\u012e\u0098\2\u06a1\u06a2\7"+
		"W\2\2\u06a2\u06a3\5\u0142\u00a2\2\u06a3\u06a4\5\u0130\u0099\2\u06a4\u06a6"+
		"\3\2\2\2\u06a5\u06a1\3\2\2\2\u06a6\u06a9\3\2\2\2\u06a7\u06a5\3\2\2\2\u06a7"+
		"\u06a8\3\2\2\2\u06a8\u0129\3\2\2\2\u06a9\u06a7\3\2\2\2\u06aa\u06b1\5\u0132"+
		"\u009a\2\u06ab\u06ac\7W\2\2\u06ac\u06ad\5\u0142\u00a2\2\u06ad\u06ae\5"+
		"\u0134\u009b\2\u06ae\u06b0\3\2\2\2\u06af\u06ab\3\2\2\2\u06b0\u06b3\3\2"+
		"\2\2\u06b1\u06af\3\2\2\2\u06b1\u06b2\3\2\2\2\u06b2\u012b\3\2\2\2\u06b3"+
		"\u06b1\3\2\2\2\u06b4\u06bb\5\u0134\u009b\2\u06b5\u06b6\7W\2\2\u06b6\u06b7"+
		"\5\u0142\u00a2\2\u06b7\u06b8\5\u0134\u009b\2\u06b8\u06ba\3\2\2\2\u06b9"+
		"\u06b5\3\2\2\2\u06ba\u06bd\3\2\2\2\u06bb\u06b9\3\2\2\2\u06bb\u06bc\3\2"+
		"\2\2\u06bc\u012d\3\2\2\2\u06bd\u06bb\3\2\2\2\u06be\u06c1\5\u00e0q\2\u06bf"+
		"\u06c1\5\u0110\u0089\2\u06c0\u06be\3\2\2\2\u06c0\u06bf\3\2\2\2\u06c1\u012f"+
		"\3\2\2\2\u06c2\u06c5\5\u00e0q\2\u06c3\u06c5\5\u010e\u0088\2\u06c4\u06c2"+
		"\3\2\2\2\u06c4\u06c3\3\2\2\2\u06c5\u0131\3\2\2\2\u06c6\u06ca\5\u00e0q"+
		"\2\u06c7\u06ca\5|?\2\u06c8\u06ca\5\u0110\u0089\2\u06c9\u06c6\3\2\2\2\u06c9"+
		"\u06c7\3\2\2\2\u06c9\u06c8\3\2\2\2\u06ca\u0133\3\2\2\2\u06cb\u06cf\5\u00e0"+
		"q\2\u06cc\u06cf\5|?\2\u06cd\u06cf\5\u010e\u0088\2\u06ce\u06cb\3\2\2\2"+
		"\u06ce\u06cc\3\2\2\2\u06ce\u06cd\3\2\2\2\u06cf\u0135\3\2\2\2\u06d0\u06d1"+
		"\7\3\2\2\u06d1\u0137\3\2\2\2\u06d2\u06d3\7|\2\2\u06d3\u0139\3\2\2\2\u06d4"+
		"\u06d5\t\24\2\2\u06d5\u013b\3\2\2\2\u06d6\u06d7\t\25\2\2\u06d7\u013d\3"+
		"\2\2\2\u06d8\u06d9\t\26\2\2\u06d9\u013f\3\2\2\2\u06da\u06db\7Q\2\2\u06db"+
		"\u0141\3\2\2\2\u06dc\u06de\7\u0081\2\2\u06dd\u06dc\3\2\2\2\u06de\u06e1"+
		"\3\2\2\2\u06df\u06dd\3\2\2\2\u06df\u06e0\3\2\2\2\u06e0\u0143\3\2\2\2\u06e1"+
		"\u06df\3\2\2\2\u06e2\u06e4\t\27\2\2\u06e3\u06e2\3\2\2\2\u06e4\u06e5\3"+
		"\2\2\2\u06e5\u06e3\3\2\2\2\u06e5\u06e6\3\2\2\2\u06e6\u0145\3\2\2\2\u00c5"+
		"\u0149\u014b\u014e\u0158\u015c\u0163\u016c\u0173\u017a\u017f\u0187\u018e"+
		"\u0191\u0199\u019e\u01a2\u01a7\u01af\u01bb\u01c6\u01cf\u01d9\u01e7\u01ed"+
		"\u01f4\u01fb\u0207\u020a\u020d\u0215\u0218\u021b\u0227\u022d\u0230\u0234"+
		"\u0238\u023f\u0243\u0246\u0253\u0258\u025a\u025e\u0262\u026d\u0276\u0284"+
		"\u0289\u0291\u0294\u0299\u02a0\u02a3\u02a9\u02ac\u02b0\u02b4\u02c1\u02ce"+
		"\u02d0\u02dc\u02e1\u02e7\u02ef\u02f7\u02fa\u0302\u030b\u0313\u031a\u0326"+
		"\u032e\u0336\u033d\u0343\u0356\u035a\u0361\u0365\u0368\u036f\u0372\u037a"+
		"\u037e\u0386\u038b\u0392\u0396\u039a\u03a3\u03ae\u03b3\u03bb\u03bf\u03c1"+
		"\u03c7\u03ce\u03d6\u03df\u03e4\u03ec\u03f2\u03fc\u0402\u040a\u0410\u041a"+
		"\u041e\u0435\u0439\u043d\u0441\u044a\u0450\u0459\u0466\u0475\u047b\u0487"+
		"\u0492\u049c\u04a1\u04a9\u04b5\u04b9\u04bd\u04c4\u04c8\u04cc\u04d0\u04e3"+
		"\u04e7\u04ed\u04f3\u0506\u0521\u0524\u0564\u0574\u0576\u0583\u0589\u058e"+
		"\u0595\u0598\u059d\u05a4\u05ac\u05b2\u05c0\u05cb\u05d1\u05d5\u05d8\u05dc"+
		"\u05e1\u05e6\u05ec\u05fb\u0601\u0606\u060b\u060f\u0612\u0619\u061c\u0625"+
		"\u062d\u063a\u0646\u0652\u0656\u065a\u065e\u0664\u0669\u066e\u0670\u0675"+
		"\u067e\u0688\u068a\u0695\u0699\u069c\u06a7\u06b1\u06bb\u06c0\u06c4\u06c9"+
		"\u06ce\u06df\u06e5";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
	}
}