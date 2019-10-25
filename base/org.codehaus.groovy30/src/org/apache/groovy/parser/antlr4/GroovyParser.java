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
		RULE_compilationUnit = 0, RULE_statements = 1, RULE_packageDeclaration = 2, 
		RULE_importDeclaration = 3, RULE_typeDeclaration = 4, RULE_modifier = 5, 
		RULE_modifiersOpt = 6, RULE_modifiers = 7, RULE_classOrInterfaceModifiersOpt = 8, 
		RULE_classOrInterfaceModifiers = 9, RULE_classOrInterfaceModifier = 10, 
		RULE_variableModifier = 11, RULE_variableModifiersOpt = 12, RULE_variableModifiers = 13, 
		RULE_typeParameters = 14, RULE_typeParameter = 15, RULE_typeBound = 16, 
		RULE_typeList = 17, RULE_classDeclaration = 18, RULE_classBody = 19, RULE_enumConstants = 20, 
		RULE_enumConstant = 21, RULE_classBodyDeclaration = 22, RULE_memberDeclaration = 23, 
		RULE_methodDeclaration = 24, RULE_methodName = 25, RULE_returnType = 26, 
		RULE_fieldDeclaration = 27, RULE_variableDeclarators = 28, RULE_variableDeclarator = 29, 
		RULE_variableDeclaratorId = 30, RULE_variableInitializer = 31, RULE_variableInitializers = 32, 
		RULE_dims = 33, RULE_dimsOpt = 34, RULE_standardType = 35, RULE_type = 36, 
		RULE_classOrInterfaceType = 37, RULE_generalClassOrInterfaceType = 38, 
		RULE_standardClassOrInterfaceType = 39, RULE_primitiveType = 40, RULE_typeArguments = 41, 
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
		RULE_blockStatement = 78, RULE_localVariableDeclaration = 79, RULE_classifiedModifiers = 80, 
		RULE_variableDeclaration = 81, RULE_typeNamePairs = 82, RULE_typeNamePair = 83, 
		RULE_variableNames = 84, RULE_conditionalStatement = 85, RULE_ifElseStatement = 86, 
		RULE_switchStatement = 87, RULE_loopStatement = 88, RULE_continueStatement = 89, 
		RULE_breakStatement = 90, RULE_tryCatchStatement = 91, RULE_assertStatement = 92, 
		RULE_statement = 93, RULE_catchClause = 94, RULE_catchType = 95, RULE_finallyBlock = 96, 
		RULE_resources = 97, RULE_resourceList = 98, RULE_resource = 99, RULE_switchBlockStatementGroup = 100, 
		RULE_switchLabel = 101, RULE_forControl = 102, RULE_enhancedForControl = 103, 
		RULE_classicalForControl = 104, RULE_forInit = 105, RULE_forUpdate = 106, 
		RULE_castParExpression = 107, RULE_parExpression = 108, RULE_expressionInPar = 109, 
		RULE_expressionList = 110, RULE_expressionListElement = 111, RULE_enhancedStatementExpression = 112, 
		RULE_statementExpression = 113, RULE_postfixExpression = 114, RULE_expression = 115, 
		RULE_castOperandExpression = 116, RULE_commandExpression = 117, RULE_commandArgument = 118, 
		RULE_pathExpression = 119, RULE_pathElement = 120, RULE_namePart = 121, 
		RULE_dynamicMemberName = 122, RULE_indexPropertyArgs = 123, RULE_namedPropertyArgs = 124, 
		RULE_primary = 125, RULE_list = 126, RULE_map = 127, RULE_mapEntryList = 128, 
		RULE_mapEntry = 129, RULE_mapEntryLabel = 130, RULE_creator = 131, RULE_arrayInitializer = 132, 
		RULE_anonymousInnerClassDeclaration = 133, RULE_createdName = 134, RULE_nonWildcardTypeArguments = 135, 
		RULE_typeArgumentsOrDiamond = 136, RULE_arguments = 137, RULE_argumentList = 138, 
		RULE_enhancedArgumentList = 139, RULE_argumentListElement = 140, RULE_enhancedArgumentListElement = 141, 
		RULE_stringLiteral = 142, RULE_className = 143, RULE_identifier = 144, 
		RULE_builtInType = 145, RULE_keywords = 146, RULE_rparen = 147, RULE_nls = 148, 
		RULE_sep = 149;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "statements", "packageDeclaration", "importDeclaration", 
			"typeDeclaration", "modifier", "modifiersOpt", "modifiers", "classOrInterfaceModifiersOpt", 
			"classOrInterfaceModifiers", "classOrInterfaceModifier", "variableModifier", 
			"variableModifiersOpt", "variableModifiers", "typeParameters", "typeParameter", 
			"typeBound", "typeList", "classDeclaration", "classBody", "enumConstants", 
			"enumConstant", "classBodyDeclaration", "memberDeclaration", "methodDeclaration", 
			"methodName", "returnType", "fieldDeclaration", "variableDeclarators", 
			"variableDeclarator", "variableDeclaratorId", "variableInitializer", 
			"variableInitializers", "dims", "dimsOpt", "standardType", "type", "classOrInterfaceType", 
			"generalClassOrInterfaceType", "standardClassOrInterfaceType", "primitiveType", 
			"typeArguments", "typeArgument", "annotatedQualifiedClassName", "qualifiedClassNameList", 
			"formalParameters", "formalParameterList", "thisFormalParameter", "formalParameter", 
			"methodBody", "qualifiedName", "qualifiedNameElement", "qualifiedNameElements", 
			"qualifiedClassName", "qualifiedStandardClassName", "literal", "gstring", 
			"gstringValue", "gstringPath", "lambdaExpression", "standardLambdaExpression", 
			"lambdaParameters", "standardLambdaParameters", "lambdaBody", "closure", 
			"closureOrLambdaExpression", "blockStatementsOpt", "blockStatements", 
			"annotationsOpt", "annotation", "elementValues", "annotationName", "elementValuePairs", 
			"elementValuePair", "elementValuePairName", "elementValue", "elementValueArrayInitializer", 
			"block", "blockStatement", "localVariableDeclaration", "classifiedModifiers", 
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
			"primary", "list", "map", "mapEntryList", "mapEntry", "mapEntryLabel", 
			"creator", "arrayInitializer", "anonymousInnerClassDeclaration", "createdName", 
			"nonWildcardTypeArguments", "typeArgumentsOrDiamond", "arguments", "argumentList", 
			"enhancedArgumentList", "argumentListElement", "enhancedArgumentListElement", 
			"stringLiteral", "className", "identifier", "builtInType", "keywords", 
			"rparen", "nls", "sep"
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
		public StatementsContext statements() {
			return getRuleContext(StatementsContext.class,0);
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
			setState(300);
			nls();
			setState(302);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(301);
				packageDeclaration();
				}
				break;
			}
			setState(305);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(304);
				sep();
				}
				break;
			}
			setState(308);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(307);
				statements();
				}
				break;
			}
			setState(310);
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

	public static class StatementsContext extends GroovyParserRuleContext {
		public List<? extends StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<? extends SepContext> sep() {
			return getRuleContexts(SepContext.class);
		}
		public SepContext sep(int i) {
			return getRuleContext(SepContext.class,i);
		}
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statements; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statements);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(312);
			statement();
			setState(318);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(313);
					sep();
					setState(314);
					statement();
					}
					} 
				}
				setState(320);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(321);
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
		enterRule(_localctx, 4, RULE_packageDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			annotationsOpt();
			setState(325);
			match(PACKAGE);
			setState(326);
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
		enterRule(_localctx, 6, RULE_importDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(328);
			annotationsOpt();
			setState(329);
			match(IMPORT);
			setState(331);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(330);
				match(STATIC);
				}
				break;
			}
			setState(333);
			qualifiedName();
			setState(338);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(334);
				match(DOT);
				setState(335);
				match(MUL);
				}
				break;

			case 2:
				{
				setState(336);
				match(AS);
				setState(337);
				_localctx.alias = identifier();
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
		enterRule(_localctx, 8, RULE_typeDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(340);
			classOrInterfaceModifiersOpt();
			setState(341);
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
		enterRule(_localctx, 10, RULE_modifier);
		int _la;
		try {
			setState(345);
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
				setState(343);
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
				setState(344);
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
		enterRule(_localctx, 12, RULE_modifiersOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(347);
				modifiers();
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
		enterRule(_localctx, 14, RULE_modifiers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(353); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(350);
					modifier();
					setState(351);
					nls();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(355); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
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

	public static class ClassOrInterfaceModifiersOptContext extends GroovyParserRuleContext {
		public ClassOrInterfaceModifiersContext classOrInterfaceModifiers() {
			return getRuleContext(ClassOrInterfaceModifiersContext.class,0);
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
		enterRule(_localctx, 16, RULE_classOrInterfaceModifiersOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(358);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				setState(357);
				classOrInterfaceModifiers();
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
		enterRule(_localctx, 18, RULE_classOrInterfaceModifiers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(363); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(360);
					classOrInterfaceModifier();
					setState(361);
					nls();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(365); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
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
		enterRule(_localctx, 20, RULE_classOrInterfaceModifier);
		int _la;
		try {
			setState(369);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(367);
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
				setState(368);
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
		enterRule(_localctx, 22, RULE_variableModifier);
		int _la;
		try {
			setState(373);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(371);
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
				setState(372);
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
		enterRule(_localctx, 24, RULE_variableModifiersOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(376);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(375);
				variableModifiers();
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
		enterRule(_localctx, 26, RULE_variableModifiers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(381); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(378);
					variableModifier();
					setState(379);
					nls();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(383); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
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
		enterRule(_localctx, 28, RULE_typeParameters);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(385);
			match(LT);
			setState(386);
			nls();
			setState(387);
			typeParameter();
			setState(394);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(388);
					match(COMMA);
					setState(389);
					nls();
					setState(390);
					typeParameter();
					}
					} 
				}
				setState(396);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			}
			setState(397);
			nls();
			setState(398);
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
		enterRule(_localctx, 30, RULE_typeParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(400);
			className();
			setState(405);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(401);
				match(EXTENDS);
				setState(402);
				nls();
				setState(403);
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
		enterRule(_localctx, 32, RULE_typeBound);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(407);
			type();
			setState(414);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(408);
					match(BITAND);
					setState(409);
					nls();
					setState(410);
					type();
					}
					} 
				}
				setState(416);
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
		enterRule(_localctx, 34, RULE_typeList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(417);
			type();
			setState(424);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(418);
					match(COMMA);
					setState(419);
					nls();
					setState(420);
					type();
					}
					} 
				}
				setState(426);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
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
		enterRule(_localctx, 36, RULE_classDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(438);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CLASS:
				{
				setState(427);
				match(CLASS);
				 _localctx.t =  0; 
				}
				break;
			case INTERFACE:
				{
				setState(429);
				match(INTERFACE);
				 _localctx.t =  1; 
				}
				break;
			case ENUM:
				{
				setState(431);
				match(ENUM);
				 _localctx.t =  2; 
				}
				break;
			case AT:
				{
				setState(433);
				match(AT);
				setState(434);
				match(INTERFACE);
				 _localctx.t =  3; 
				}
				break;
			case TRAIT:
				{
				setState(436);
				match(TRAIT);
				 _localctx.t =  4; 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(440);
			identifier();
			setState(441);
			nls();
			setState(474);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				setState(442);
				if (!( 3 != _localctx.t )) throw createFailedPredicateException(" 3 != $t ");
				setState(444);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
				case 1:
					{
					setState(443);
					typeParameters();
					}
					break;
				}
				setState(446);
				nls();
				setState(460);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(447);
					if (!( 2 != _localctx.t )) throw createFailedPredicateException(" 2 != $t ");
					setState(457);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
					case 1:
						{
						setState(448);
						match(EXTENDS);
						setState(449);
						nls();
						setState(453);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
						case 1:
							{
							setState(450);
							if (!(1 == _localctx.t)) throw createFailedPredicateException("1 == $t");
							setState(451);
							_localctx.scs = typeList();
							}
							break;

						case 2:
							{
							setState(452);
							_localctx.sc = type();
							}
							break;
						}
						setState(455);
						nls();
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
				setState(471);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(462);
					if (!(1 != _localctx.t)) throw createFailedPredicateException("1 != $t");
					setState(468);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==IMPLEMENTS) {
						{
						setState(463);
						match(IMPLEMENTS);
						setState(464);
						nls();
						setState(465);
						_localctx.is = typeList();
						setState(466);
						nls();
						}
					}

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
			setState(476);
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
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(GroovyParser.RBRACE, 0); }
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
		public EnumConstantsContext enumConstants() {
			return getRuleContext(EnumConstantsContext.class,0);
		}
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
		enterRule(_localctx, 38, RULE_classBody);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(478);
			match(LBRACE);
			setState(479);
			nls();
			setState(488);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(480);
				if (!( 2 == _localctx.t )) throw createFailedPredicateException(" 2 == $t ");
				setState(482);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
				case 1:
					{
					setState(481);
					enumConstants();
					}
					break;
				}
				setState(485);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(484);
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
			setState(491);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				{
				setState(490);
				classBodyDeclaration(_localctx.t);
				}
				break;
			}
			setState(498);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(493);
					sep();
					setState(494);
					classBodyDeclaration(_localctx.t);
					}
					} 
				}
				setState(500);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			}
			setState(502);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(501);
				sep();
				}
			}

			setState(504);
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
		enterRule(_localctx, 40, RULE_enumConstants);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(506);
			enumConstant();
			setState(514);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(507);
					nls();
					setState(508);
					match(COMMA);
					setState(509);
					nls();
					setState(510);
					enumConstant();
					}
					} 
				}
				setState(516);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			}
			setState(520);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				{
				setState(517);
				nls();
				setState(518);
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
		enterRule(_localctx, 42, RULE_enumConstant);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(522);
			annotationsOpt();
			setState(523);
			identifier();
			setState(525);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(524);
				arguments();
				}
				break;
			}
			setState(528);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				{
				setState(527);
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
		public TerminalNode SEMI() { return getToken(GroovyParser.SEMI, 0); }
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
		enterRule(_localctx, 44, RULE_classBodyDeclaration);
		int _la;
		try {
			setState(537);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(530);
				match(SEMI);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(533);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(531);
					match(STATIC);
					setState(532);
					nls();
					}
				}

				setState(535);
				block();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(536);
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
		enterRule(_localctx, 46, RULE_memberDeclaration);
		try {
			setState(544);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(539);
				methodDeclaration(0, _localctx.t);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(540);
				fieldDeclaration();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(541);
				modifiersOpt();
				setState(542);
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
		enterRule(_localctx, 48, RULE_methodDeclaration);
		try {
			setState(577);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(546);
				if (!( 3 == _localctx.ct )) throw createFailedPredicateException(" 3 == $ct ");
				setState(547);
				returnType(_localctx.ct);
				setState(548);
				methodName();
				setState(549);
				match(LPAREN);
				setState(550);
				rparen();
				setState(555);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
				case 1:
					{
					setState(551);
					match(DEFAULT);
					setState(552);
					nls();
					setState(553);
					elementValue();
					}
					break;
				}
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(557);
				modifiersOpt();
				setState(559);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
				case 1:
					{
					setState(558);
					typeParameters();
					}
					break;
				}
				setState(562);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
				case 1:
					{
					setState(561);
					returnType(_localctx.ct);
					}
					break;
				}
				setState(564);
				methodName();
				setState(565);
				formalParameters();
				setState(571);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(566);
					nls();
					setState(567);
					match(THROWS);
					setState(568);
					nls();
					setState(569);
					qualifiedClassNameList();
					}
					break;
				}
				setState(573);
				nls();
				setState(575);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
				case 1:
					{
					setState(574);
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
		enterRule(_localctx, 50, RULE_methodName);
		try {
			setState(581);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(579);
				identifier();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(580);
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
		enterRule(_localctx, 52, RULE_returnType);
		try {
			setState(586);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(583);
				standardType();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(584);
				if (!( 3 != _localctx.ct )) throw createFailedPredicateException(" 3 != $ct ");
				setState(585);
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
		enterRule(_localctx, 54, RULE_fieldDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(588);
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
		enterRule(_localctx, 56, RULE_variableDeclarators);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(590);
			variableDeclarator();
			setState(597);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(591);
					match(COMMA);
					setState(592);
					nls();
					setState(593);
					variableDeclarator();
					}
					} 
				}
				setState(599);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
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
		enterRule(_localctx, 58, RULE_variableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(600);
			variableDeclaratorId();
			setState(606);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				{
				setState(601);
				nls();
				setState(602);
				match(ASSIGN);
				setState(603);
				nls();
				setState(604);
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
		enterRule(_localctx, 60, RULE_variableDeclaratorId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(608);
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
		enterRule(_localctx, 62, RULE_variableInitializer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(610);
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
		enterRule(_localctx, 64, RULE_variableInitializers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(612);
			variableInitializer();
			setState(613);
			nls();
			setState(621);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(614);
					match(COMMA);
					setState(615);
					nls();
					setState(616);
					variableInitializer();
					setState(617);
					nls();
					}
					} 
				}
				setState(623);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
			}
			setState(624);
			nls();
			setState(626);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
			case 1:
				{
				setState(625);
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

	public static class DimsContext extends GroovyParserRuleContext {
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
		public DimsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dims; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitDims(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final DimsContext dims() throws RecognitionException {
		DimsContext _localctx = new DimsContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_dims);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(632); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(628);
					annotationsOpt();
					setState(629);
					match(LBRACK);
					setState(630);
					match(RBRACK);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(634); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
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

	public static class DimsOptContext extends GroovyParserRuleContext {
		public DimsContext dims() {
			return getRuleContext(DimsContext.class,0);
		}
		public DimsOptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dimsOpt; }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitDimsOpt(this);
			else return visitor.visitChildren(this);
		}
	}

	@RuleVersion(0)
	public final DimsOptContext dimsOpt() throws RecognitionException {
		DimsOptContext _localctx = new DimsOptContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_dimsOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(637);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				{
				setState(636);
				dims();
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
		enterRule(_localctx, 70, RULE_standardType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(639);
			annotationsOpt();
			setState(642);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(640);
				primitiveType();
				}
				break;

			case 2:
				{
				setState(641);
				standardClassOrInterfaceType();
				}
				break;
			}
			setState(644);
			dimsOpt();
			}
		}
		catch (RecognitionException re) {
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
		public DimsOptContext dimsOpt() {
			return getRuleContext(DimsOptContext.class,0);
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
		enterRule(_localctx, 72, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(646);
			annotationsOpt();
			setState(652);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(649);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case BuiltInPrimitiveType:
					{
					setState(647);
					primitiveType();
					}
					break;
				case VOID:
					{
					setState(648);
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
				setState(651);
				generalClassOrInterfaceType();
				}
				break;
			}
			setState(654);
			dimsOpt();
			}
		}
		catch (RecognitionException re) {
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
		enterRule(_localctx, 74, RULE_classOrInterfaceType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(658);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,58,_ctx) ) {
			case 1:
				{
				setState(656);
				qualifiedClassName();
				}
				break;

			case 2:
				{
				setState(657);
				qualifiedStandardClassName();
				}
				break;
			}
			setState(661);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(660);
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
		enterRule(_localctx, 76, RULE_generalClassOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(663);
			qualifiedClassName();
			setState(665);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				{
				setState(664);
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
		enterRule(_localctx, 78, RULE_standardClassOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(667);
			qualifiedStandardClassName();
			setState(669);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
			case 1:
				{
				setState(668);
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
		enterRule(_localctx, 80, RULE_primitiveType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(671);
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
		enterRule(_localctx, 82, RULE_typeArguments);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(673);
			match(LT);
			setState(674);
			nls();
			setState(675);
			typeArgument();
			setState(682);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(676);
					match(COMMA);
					setState(677);
					nls();
					setState(678);
					typeArgument();
					}
					} 
				}
				setState(684);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
			}
			setState(685);
			nls();
			setState(686);
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
		enterRule(_localctx, 84, RULE_typeArgument);
		int _la;
		try {
			setState(697);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(688);
				type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(689);
				annotationsOpt();
				setState(690);
				match(QUESTION);
				setState(695);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
				case 1:
					{
					setState(691);
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
					setState(692);
					nls();
					setState(693);
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
		enterRule(_localctx, 86, RULE_annotatedQualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(699);
			annotationsOpt();
			setState(700);
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
		enterRule(_localctx, 88, RULE_qualifiedClassNameList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(702);
			annotatedQualifiedClassName();
			setState(709);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(703);
					match(COMMA);
					setState(704);
					nls();
					setState(705);
					annotatedQualifiedClassName();
					}
					} 
				}
				setState(711);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
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
		enterRule(_localctx, 90, RULE_formalParameters);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(712);
			match(LPAREN);
			setState(714);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				{
				setState(713);
				formalParameterList();
				}
				break;
			}
			setState(716);
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
		enterRule(_localctx, 92, RULE_formalParameterList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(720);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
			case 1:
				{
				setState(718);
				formalParameter();
				}
				break;

			case 2:
				{
				setState(719);
				thisFormalParameter();
				}
				break;
			}
			setState(728);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(722);
					match(COMMA);
					setState(723);
					nls();
					setState(724);
					formalParameter();
					}
					} 
				}
				setState(730);
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
			setState(731);
			type();
			setState(732);
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
		enterRule(_localctx, 96, RULE_formalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(734);
			variableModifiersOpt();
			setState(736);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(735);
				type();
				}
				break;
			}
			setState(739);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(738);
				match(ELLIPSIS);
				}
				break;
			}
			setState(741);
			variableDeclaratorId();
			setState(747);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				{
				setState(742);
				nls();
				setState(743);
				match(ASSIGN);
				setState(744);
				nls();
				setState(745);
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
		enterRule(_localctx, 98, RULE_methodBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(749);
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
		enterRule(_localctx, 100, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(751);
			qualifiedNameElement();
			setState(756);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(752);
					match(DOT);
					setState(753);
					qualifiedNameElement();
					}
					} 
				}
				setState(758);
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
			setState(764);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(759);
				identifier();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(760);
				match(DEF);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(761);
				match(IN);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(762);
				match(AS);
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(763);
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
		enterRule(_localctx, 104, RULE_qualifiedNameElements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(771);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(766);
					qualifiedNameElement();
					setState(767);
					match(DOT);
					}
					} 
				}
				setState(773);
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
			setState(774);
			qualifiedNameElements();
			setState(775);
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
		enterRule(_localctx, 108, RULE_qualifiedStandardClassName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(777);
			qualifiedNameElements();
			setState(778);
			className();
			setState(783);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,75,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(779);
					match(DOT);
					setState(780);
					className();
					}
					} 
				}
				setState(785);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,75,_ctx);
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
		enterRule(_localctx, 110, RULE_literal);
		try {
			setState(791);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IntegerLiteral:
				_localctx = new IntegerLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(786);
				match(IntegerLiteral);
				}
				break;
			case FloatingPointLiteral:
				_localctx = new FloatingPointLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(787);
				match(FloatingPointLiteral);
				}
				break;
			case StringLiteral:
				_localctx = new StringLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(788);
				stringLiteral();
				}
				break;
			case BooleanLiteral:
				_localctx = new BooleanLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(789);
				match(BooleanLiteral);
				}
				break;
			case NullLiteral:
				_localctx = new NullLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(790);
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
		enterRule(_localctx, 112, RULE_gstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(793);
			match(GStringBegin);
			setState(794);
			gstringValue();
			setState(799);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPart) {
				{
				{
				setState(795);
				match(GStringPart);
				setState(796);
				gstringValue();
				}
				}
				setState(801);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(802);
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
		enterRule(_localctx, 114, RULE_gstringValue);
		try {
			setState(811);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(804);
				gstringPath();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(805);
				match(LBRACE);
				setState(807);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
				case 1:
					{
					setState(806);
					statementExpression();
					}
					break;
				}
				setState(809);
				match(RBRACE);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(810);
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
		enterRule(_localctx, 116, RULE_gstringPath);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(813);
			identifier();
			setState(817);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPathPart) {
				{
				{
				setState(814);
				match(GStringPathPart);
				}
				}
				setState(819);
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
		enterRule(_localctx, 118, RULE_lambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(820);
			lambdaParameters();
			setState(821);
			nls();
			setState(822);
			match(ARROW);
			setState(823);
			nls();
			setState(824);
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
		enterRule(_localctx, 120, RULE_standardLambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(826);
			standardLambdaParameters();
			setState(827);
			nls();
			setState(828);
			match(ARROW);
			setState(829);
			nls();
			setState(830);
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
		enterRule(_localctx, 122, RULE_lambdaParameters);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(832);
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
		enterRule(_localctx, 124, RULE_standardLambdaParameters);
		try {
			setState(836);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(834);
				formalParameters();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(835);
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
		enterRule(_localctx, 126, RULE_lambdaBody);
		try {
			setState(840);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(838);
				block();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(839);
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
		enterRule(_localctx, 128, RULE_closure);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(842);
			match(LBRACE);
			setState(843);
			nls();
			setState(851);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
			case 1:
				{
				setState(845);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
				case 1:
					{
					setState(844);
					formalParameterList();
					}
					break;
				}
				setState(847);
				nls();
				setState(848);
				match(ARROW);
				setState(849);
				nls();
				}
				break;
			}
			setState(853);
			blockStatementsOpt();
			setState(854);
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
		enterRule(_localctx, 130, RULE_closureOrLambdaExpression);
		try {
			setState(858);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(856);
				closure();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(857);
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
		enterRule(_localctx, 132, RULE_blockStatementsOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(861);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				{
				setState(860);
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
		enterRule(_localctx, 134, RULE_blockStatements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(863);
			blockStatement();
			setState(869);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(864);
					sep();
					setState(865);
					blockStatement();
					}
					} 
				}
				setState(871);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			}
			setState(873);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
			case 1:
				{
				setState(872);
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
		enterRule(_localctx, 136, RULE_annotationsOpt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(880);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,89,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(875);
					annotation();
					setState(876);
					nls();
					}
					} 
				}
				setState(882);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,89,_ctx);
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
		enterRule(_localctx, 138, RULE_annotation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(883);
			match(AT);
			setState(884);
			annotationName();
			setState(890);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				{
				setState(885);
				match(LPAREN);
				setState(887);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(886);
					elementValues();
					}
					break;
				}
				setState(889);
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
		enterRule(_localctx, 140, RULE_elementValues);
		try {
			setState(894);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(892);
				elementValuePairs();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(893);
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
		enterRule(_localctx, 142, RULE_annotationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(896);
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
		enterRule(_localctx, 144, RULE_elementValuePairs);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(898);
			elementValuePair();
			setState(903);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(899);
					match(COMMA);
					setState(900);
					elementValuePair();
					}
					} 
				}
				setState(905);
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
		enterRule(_localctx, 146, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(906);
			elementValuePairName();
			setState(907);
			nls();
			setState(908);
			match(ASSIGN);
			setState(909);
			nls();
			setState(910);
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
		enterRule(_localctx, 148, RULE_elementValuePairName);
		try {
			setState(914);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(912);
				identifier();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(913);
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
		enterRule(_localctx, 150, RULE_elementValue);
		try {
			setState(919);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(916);
				elementValueArrayInitializer();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(917);
				annotation();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(918);
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
		enterRule(_localctx, 152, RULE_elementValueArrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(921);
			match(LBRACK);
			setState(930);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				{
				setState(922);
				elementValue();
				setState(927);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(923);
						match(COMMA);
						setState(924);
						elementValue();
						}
						} 
					}
					setState(929);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
				}
				}
				break;
			}
			setState(933);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(932);
				match(COMMA);
				}
			}

			setState(935);
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
		public NlsContext nls() {
			return getRuleContext(NlsContext.class,0);
		}
		public List<? extends SepContext> sep() {
			return getRuleContexts(SepContext.class);
		}
		public SepContext sep(int i) {
			return getRuleContext(SepContext.class,i);
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
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(937);
			match(LBRACE);
			setState(945);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				{
				setState(938);
				nls();
				}
				break;

			case 2:
				{
				setState(942);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,99,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(939);
						sep();
						}
						} 
					}
					setState(944);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,99,_ctx);
				}
				}
				break;
			}
			setState(947);
			blockStatementsOpt();
			setState(948);
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
		enterRule(_localctx, 156, RULE_blockStatement);
		try {
			setState(952);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(950);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(951);
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
		enterRule(_localctx, 158, RULE_localVariableDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(954);
			if (!( !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ");
			setState(955);
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
		public VariableModifiersContext variableModifiers() {
			return getRuleContext(VariableModifiersContext.class,0);
		}
		public ModifiersContext modifiers() {
			return getRuleContext(ModifiersContext.class,0);
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
		enterRule(_localctx, 160, RULE_classifiedModifiers);
		try {
			setState(961);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(957);
				if (!( 0 == _localctx.t )) throw createFailedPredicateException(" 0 == $t ");
				setState(958);
				variableModifiers();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(959);
				if (!( 1 == _localctx.t )) throw createFailedPredicateException(" 1 == $t ");
				setState(960);
				modifiers();
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
		enterRule(_localctx, 162, RULE_variableDeclaration);
		try {
			setState(983);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(963);
				classifiedModifiers(_localctx.t);
				setState(974);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,104,_ctx) ) {
				case 1:
					{
					setState(965);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,103,_ctx) ) {
					case 1:
						{
						setState(964);
						type();
						}
						break;
					}
					setState(967);
					variableDeclarators();
					}
					break;

				case 2:
					{
					setState(968);
					typeNamePairs();
					setState(969);
					nls();
					setState(970);
					match(ASSIGN);
					setState(971);
					nls();
					setState(972);
					variableInitializer();
					}
					break;
				}
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(976);
				classifiedModifiers(_localctx.t);
				setState(977);
				type();
				setState(978);
				variableDeclarators();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(980);
				type();
				setState(981);
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
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(985);
			match(LPAREN);
			setState(986);
			typeNamePair();
			setState(991);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(987);
					match(COMMA);
					setState(988);
					typeNamePair();
					}
					} 
				}
				setState(993);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
			}
			setState(994);
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
			setState(997);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,107,_ctx) ) {
			case 1:
				{
				setState(996);
				type();
				}
				break;
			}
			setState(999);
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
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1001);
			match(LPAREN);
			setState(1002);
			variableDeclaratorId();
			setState(1005); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1003);
					match(COMMA);
					setState(1004);
					variableDeclaratorId();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1007); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
			} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(1009);
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
			setState(1013);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IF:
				enterOuterAlt(_localctx, 1);
				{
				setState(1011);
				ifElseStatement();
				}
				break;
			case SWITCH:
				enterOuterAlt(_localctx, 2);
				{
				setState(1012);
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
			setState(1015);
			match(IF);
			setState(1016);
			expressionInPar();
			setState(1017);
			nls();
			setState(1018);
			_localctx.tb = statement();
			setState(1027);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
			case 1:
				{
				setState(1021);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,110,_ctx) ) {
				case 1:
					{
					setState(1019);
					nls();
					}
					break;

				case 2:
					{
					setState(1020);
					sep();
					}
					break;
				}
				setState(1023);
				match(ELSE);
				setState(1024);
				nls();
				setState(1025);
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
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1029);
			match(SWITCH);
			setState(1030);
			expressionInPar();
			setState(1031);
			nls();
			setState(1032);
			match(LBRACE);
			setState(1033);
			nls();
			setState(1037);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1034);
					switchBlockStatementGroup();
					}
					} 
				}
				setState(1039);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
			}
			setState(1040);
			nls();
			setState(1041);
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
			setState(1062);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FOR:
				_localctx = new ForStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1043);
				match(FOR);
				setState(1044);
				match(LPAREN);
				setState(1045);
				forControl();
				setState(1046);
				rparen();
				setState(1047);
				nls();
				setState(1048);
				statement();
				}
				break;
			case WHILE:
				_localctx = new WhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1050);
				match(WHILE);
				setState(1051);
				expressionInPar();
				setState(1052);
				nls();
				setState(1053);
				statement();
				}
				break;
			case DO:
				_localctx = new DoWhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1055);
				match(DO);
				setState(1056);
				nls();
				setState(1057);
				statement();
				setState(1058);
				nls();
				setState(1059);
				match(WHILE);
				setState(1060);
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
			setState(1064);
			match(CONTINUE);
			setState(1066);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
			case 1:
				{
				setState(1065);
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
			setState(1068);
			match(BREAK);
			setState(1070);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				{
				setState(1069);
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
			setState(1072);
			match(TRY);
			setState(1074);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,116,_ctx) ) {
			case 1:
				{
				setState(1073);
				resources();
				}
				break;
			}
			setState(1076);
			nls();
			setState(1077);
			block();
			setState(1083);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1078);
					nls();
					setState(1079);
					catchClause();
					}
					} 
				}
				setState(1085);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
			}
			setState(1089);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
			case 1:
				{
				setState(1086);
				nls();
				setState(1087);
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
			setState(1091);
			match(ASSERT);
			setState(1092);
			_localctx.ce = expression(0);
			setState(1098);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,119,_ctx) ) {
			case 1:
				{
				setState(1093);
				nls();
				setState(1094);
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
				setState(1095);
				nls();
				setState(1096);
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
	public static class ImportStmtAltContext extends StatementContext {
		public ImportDeclarationContext importDeclaration() {
			return getRuleContext(ImportDeclarationContext.class,0);
		}
		public ImportStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitImportStmtAlt(this);
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
	public static class TypeDeclarationStmtAltContext extends StatementContext {
		public TypeDeclarationContext typeDeclaration() {
			return getRuleContext(TypeDeclarationContext.class,0);
		}
		public TypeDeclarationStmtAltContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <Result> Result accept(ParseTreeVisitor<? extends Result> visitor) {
			if ( visitor instanceof GroovyParserVisitor<?> ) return ((GroovyParserVisitor<? extends Result>)visitor).visitTypeDeclarationStmtAlt(this);
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
		enterRule(_localctx, 186, RULE_statement);
		try {
			setState(1130);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
			case 1:
				_localctx = new BlockStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1100);
				block();
				}
				break;

			case 2:
				_localctx = new ConditionalStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1101);
				conditionalStatement();
				}
				break;

			case 3:
				_localctx = new LoopStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1102);
				loopStatement();
				}
				break;

			case 4:
				_localctx = new TryCatchStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1103);
				tryCatchStatement();
				}
				break;

			case 5:
				_localctx = new SynchronizedStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1104);
				match(SYNCHRONIZED);
				setState(1105);
				expressionInPar();
				setState(1106);
				nls();
				setState(1107);
				block();
				}
				break;

			case 6:
				_localctx = new ReturnStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1109);
				match(RETURN);
				setState(1111);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
				case 1:
					{
					setState(1110);
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
				setState(1113);
				match(THROW);
				setState(1114);
				expression(0);
				}
				break;

			case 8:
				_localctx = new BreakStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1115);
				breakStatement();
				}
				break;

			case 9:
				_localctx = new ContinueStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1116);
				continueStatement();
				}
				break;

			case 10:
				_localctx = new LabeledStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1117);
				identifier();
				setState(1118);
				match(COLON);
				setState(1119);
				nls();
				setState(1120);
				statement();
				}
				break;

			case 11:
				_localctx = new ImportStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1122);
				importDeclaration();
				}
				break;

			case 12:
				_localctx = new AssertStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(1123);
				assertStatement();
				}
				break;

			case 13:
				_localctx = new TypeDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(1124);
				typeDeclaration();
				}
				break;

			case 14:
				_localctx = new LocalVariableDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(1125);
				localVariableDeclaration();
				}
				break;

			case 15:
				_localctx = new MethodDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(1126);
				if (!( !SemanticPredicates.isInvalidMethodDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidMethodDeclaration(_input) ");
				setState(1127);
				methodDeclaration(3, 9);
				}
				break;

			case 16:
				_localctx = new ExpressionStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(1128);
				statementExpression();
				}
				break;

			case 17:
				_localctx = new EmptyStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(1129);
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
			setState(1132);
			match(CATCH);
			setState(1133);
			match(LPAREN);
			setState(1134);
			variableModifiersOpt();
			setState(1136);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,122,_ctx) ) {
			case 1:
				{
				setState(1135);
				catchType();
				}
				break;
			}
			setState(1138);
			identifier();
			setState(1139);
			rparen();
			setState(1140);
			nls();
			setState(1141);
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
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1143);
			qualifiedClassName();
			setState(1148);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1144);
					match(BITOR);
					setState(1145);
					qualifiedClassName();
					}
					} 
				}
				setState(1150);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
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
			setState(1151);
			match(FINALLY);
			setState(1152);
			nls();
			setState(1153);
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1155);
			match(LPAREN);
			setState(1156);
			nls();
			setState(1157);
			resourceList();
			setState(1159);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,124,_ctx) ) {
			case 1:
				{
				setState(1158);
				sep();
				}
				break;
			}
			setState(1161);
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
			setState(1163);
			resource();
			setState(1169);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,125,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1164);
					sep();
					setState(1165);
					resource();
					}
					} 
				}
				setState(1171);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,125,_ctx);
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
			setState(1174);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1172);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1173);
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
		public BlockStatementsContext blockStatements() {
			return getRuleContext(BlockStatementsContext.class,0);
		}
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
			setState(1179); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1176);
					switchLabel();
					setState(1177);
					nls();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1181); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(1183);
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
			setState(1191);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1185);
				match(CASE);
				setState(1186);
				expression(0);
				setState(1187);
				match(COLON);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1189);
				match(DEFAULT);
				setState(1190);
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
			setState(1195);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,129,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1193);
				enhancedForControl();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1194);
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
			setState(1197);
			variableModifiersOpt();
			setState(1199);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
			case 1:
				{
				setState(1198);
				type();
				}
				break;
			}
			setState(1201);
			variableDeclaratorId();
			setState(1202);
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
			setState(1203);
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
			setState(1206);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
			case 1:
				{
				setState(1205);
				forInit();
				}
				break;
			}
			setState(1208);
			match(SEMI);
			setState(1210);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				{
				setState(1209);
				expression(0);
				}
				break;
			}
			setState(1212);
			match(SEMI);
			setState(1214);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,133,_ctx) ) {
			case 1:
				{
				setState(1213);
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
			setState(1218);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,134,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1216);
				localVariableDeclaration();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1217);
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
			setState(1220);
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
			setState(1222);
			match(LPAREN);
			setState(1223);
			type();
			setState(1224);
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
			setState(1226);
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
			setState(1228);
			match(LPAREN);
			setState(1229);
			enhancedStatementExpression();
			setState(1230);
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
			setState(1232);
			expressionListElement(_localctx.canSpread);
			setState(1237);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,135,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1233);
					match(COMMA);
					setState(1234);
					expressionListElement(_localctx.canSpread);
					}
					} 
				}
				setState(1239);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,135,_ctx);
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
			setState(1243);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,136,_ctx) ) {
			case 1:
				{
				setState(1240);
				match(MUL);
				 require(_localctx.canSpread, "spread operator is not allowed here", -1); 
				}
				break;

			case 2:
				{
				}
				break;
			}
			setState(1245);
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
			setState(1249);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1247);
				statementExpression();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1248);
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
			setState(1251);
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
			setState(1253);
			pathExpression();
			setState(1255);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,138,_ctx) ) {
			case 1:
				{
				setState(1254);
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
			setState(1274);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,139,_ctx) ) {
			case 1:
				{
				_localctx = new CastExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1258);
				castParExpression();
				setState(1259);
				castOperandExpression();
				}
				break;

			case 2:
				{
				_localctx = new PostfixExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1261);
				postfixExpression();
				}
				break;

			case 3:
				{
				_localctx = new UnaryNotExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1262);
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
				setState(1263);
				nls();
				setState(1264);
				expression(18);
				}
				break;

			case 4:
				{
				_localctx = new UnaryAddExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1266);
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
				setState(1267);
				expression(16);
				}
				break;

			case 5:
				{
				_localctx = new MultipleAssignmentExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1268);
				((MultipleAssignmentExprAltContext)_localctx).left = variableNames();
				setState(1269);
				nls();
				setState(1270);
				((MultipleAssignmentExprAltContext)_localctx).op = match(ASSIGN);
				setState(1271);
				nls();
				setState(1272);
				((MultipleAssignmentExprAltContext)_localctx).right = statementExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1386);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1384);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,143,_ctx) ) {
					case 1:
						{
						_localctx = new PowerExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((PowerExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1276);
						if (!(precpred(_ctx, 17))) throw createFailedPredicateException("precpred(_ctx, 17)");
						setState(1277);
						((PowerExprAltContext)_localctx).op = match(POWER);
						setState(1278);
						nls();
						setState(1279);
						((PowerExprAltContext)_localctx).right = expression(18);
						}
						break;

					case 2:
						{
						_localctx = new MultiplicativeExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((MultiplicativeExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1281);
						if (!(precpred(_ctx, 15))) throw createFailedPredicateException("precpred(_ctx, 15)");
						setState(1282);
						nls();
						setState(1283);
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
						setState(1284);
						nls();
						setState(1285);
						((MultiplicativeExprAltContext)_localctx).right = expression(16);
						}
						break;

					case 3:
						{
						_localctx = new AdditiveExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AdditiveExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1287);
						if (!(precpred(_ctx, 14))) throw createFailedPredicateException("precpred(_ctx, 14)");
						setState(1288);
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
						setState(1289);
						nls();
						setState(1290);
						((AdditiveExprAltContext)_localctx).right = expression(15);
						}
						break;

					case 4:
						{
						_localctx = new ShiftExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ShiftExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1292);
						if (!(precpred(_ctx, 13))) throw createFailedPredicateException("precpred(_ctx, 13)");
						setState(1293);
						nls();
						setState(1304);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case GT:
						case LT:
							{
							setState(1301);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,140,_ctx) ) {
							case 1:
								{
								setState(1294);
								((ShiftExprAltContext)_localctx).dlOp = match(LT);
								setState(1295);
								match(LT);
								}
								break;

							case 2:
								{
								setState(1296);
								((ShiftExprAltContext)_localctx).tgOp = match(GT);
								setState(1297);
								match(GT);
								setState(1298);
								match(GT);
								}
								break;

							case 3:
								{
								setState(1299);
								((ShiftExprAltContext)_localctx).dgOp = match(GT);
								setState(1300);
								match(GT);
								}
								break;
							}
							}
							break;
						case RANGE_INCLUSIVE:
						case RANGE_EXCLUSIVE:
							{
							setState(1303);
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
						setState(1306);
						nls();
						setState(1307);
						((ShiftExprAltContext)_localctx).right = expression(14);
						}
						break;

					case 5:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1309);
						if (!(precpred(_ctx, 11))) throw createFailedPredicateException("precpred(_ctx, 11)");
						setState(1310);
						nls();
						setState(1311);
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
						setState(1312);
						nls();
						setState(1313);
						((RelationalExprAltContext)_localctx).right = expression(12);
						}
						break;

					case 6:
						{
						_localctx = new EqualityExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((EqualityExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1315);
						if (!(precpred(_ctx, 10))) throw createFailedPredicateException("precpred(_ctx, 10)");
						setState(1316);
						nls();
						setState(1317);
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
						setState(1318);
						nls();
						setState(1319);
						((EqualityExprAltContext)_localctx).right = expression(11);
						}
						break;

					case 7:
						{
						_localctx = new RegexExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RegexExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1321);
						if (!(precpred(_ctx, 9))) throw createFailedPredicateException("precpred(_ctx, 9)");
						setState(1322);
						nls();
						setState(1323);
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
						setState(1324);
						nls();
						setState(1325);
						((RegexExprAltContext)_localctx).right = expression(10);
						}
						break;

					case 8:
						{
						_localctx = new AndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1327);
						if (!(precpred(_ctx, 8))) throw createFailedPredicateException("precpred(_ctx, 8)");
						setState(1328);
						nls();
						setState(1329);
						((AndExprAltContext)_localctx).op = match(BITAND);
						setState(1330);
						nls();
						setState(1331);
						((AndExprAltContext)_localctx).right = expression(9);
						}
						break;

					case 9:
						{
						_localctx = new ExclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ExclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1333);
						if (!(precpred(_ctx, 7))) throw createFailedPredicateException("precpred(_ctx, 7)");
						setState(1334);
						nls();
						setState(1335);
						((ExclusiveOrExprAltContext)_localctx).op = match(XOR);
						setState(1336);
						nls();
						setState(1337);
						((ExclusiveOrExprAltContext)_localctx).right = expression(8);
						}
						break;

					case 10:
						{
						_localctx = new InclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((InclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1339);
						if (!(precpred(_ctx, 6))) throw createFailedPredicateException("precpred(_ctx, 6)");
						setState(1340);
						nls();
						setState(1341);
						((InclusiveOrExprAltContext)_localctx).op = match(BITOR);
						setState(1342);
						nls();
						setState(1343);
						((InclusiveOrExprAltContext)_localctx).right = expression(7);
						}
						break;

					case 11:
						{
						_localctx = new LogicalAndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalAndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1345);
						if (!(precpred(_ctx, 5))) throw createFailedPredicateException("precpred(_ctx, 5)");
						setState(1346);
						nls();
						setState(1347);
						((LogicalAndExprAltContext)_localctx).op = match(AND);
						setState(1348);
						nls();
						setState(1349);
						((LogicalAndExprAltContext)_localctx).right = expression(6);
						}
						break;

					case 12:
						{
						_localctx = new LogicalOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1351);
						if (!(precpred(_ctx, 4))) throw createFailedPredicateException("precpred(_ctx, 4)");
						setState(1352);
						nls();
						setState(1353);
						((LogicalOrExprAltContext)_localctx).op = match(OR);
						setState(1354);
						nls();
						setState(1355);
						((LogicalOrExprAltContext)_localctx).right = expression(5);
						}
						break;

					case 13:
						{
						_localctx = new ConditionalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ConditionalExprAltContext)_localctx).con = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1357);
						if (!(precpred(_ctx, 3))) throw createFailedPredicateException("precpred(_ctx, 3)");
						setState(1358);
						nls();
						setState(1368);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case QUESTION:
							{
							setState(1359);
							match(QUESTION);
							setState(1360);
							nls();
							setState(1361);
							((ConditionalExprAltContext)_localctx).tb = expression(0);
							setState(1362);
							nls();
							setState(1363);
							match(COLON);
							setState(1364);
							nls();
							}
							break;
						case ELVIS:
							{
							setState(1366);
							match(ELVIS);
							setState(1367);
							nls();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1370);
						((ConditionalExprAltContext)_localctx).fb = expression(3);
						}
						break;

					case 14:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1372);
						if (!(precpred(_ctx, 12))) throw createFailedPredicateException("precpred(_ctx, 12)");
						setState(1373);
						nls();
						setState(1374);
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
						setState(1375);
						nls();
						setState(1376);
						type();
						}
						break;

					case 15:
						{
						_localctx = new AssignmentExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AssignmentExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1378);
						if (!(precpred(_ctx, 1))) throw createFailedPredicateException("precpred(_ctx, 1)");
						setState(1379);
						nls();
						setState(1380);
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
						setState(1381);
						nls();
						setState(1382);
						enhancedStatementExpression();
						}
						break;
					}
					} 
				}
				setState(1388);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
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
			setState(1399);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
			case 1:
				_localctx = new CastExprAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1389);
				castParExpression();
				setState(1390);
				castOperandExpression();
				}
				break;

			case 2:
				_localctx = new PostfixExprAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1392);
				postfixExpression();
				}
				break;

			case 3:
				_localctx = new UnaryNotExprAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1393);
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
				setState(1394);
				nls();
				setState(1395);
				castOperandExpression();
				}
				break;

			case 4:
				_localctx = new UnaryAddExprAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1397);
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
				setState(1398);
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
		enterRule(_localctx, 234, RULE_commandExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1401);
			_localctx.expression = expression(0);
			setState(1405);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,146,_ctx) ) {
			case 1:
				{
				setState(1402);
				if (!( !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) )) throw createFailedPredicateException(" !SemanticPredicates.isFollowingArgumentsOrClosure($expression.ctx) ");
				setState(1403);
				argumentList();
				}
				break;

			case 2:
				{
				}
				break;
			}
			setState(1410);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1407);
					commandArgument();
					}
					} 
				}
				setState(1412);
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
		enterRule(_localctx, 236, RULE_commandArgument);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1413);
			primary();
			setState(1420);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,149,_ctx) ) {
			case 1:
				{
				setState(1415); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1414);
						pathElement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1417); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,148,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;

			case 2:
				{
				setState(1419);
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
		enterRule(_localctx, 238, RULE_pathExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1422);
			primary();
			setState(1428);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,150,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1423);
					_localctx.pathElement = pathElement();
					 _localctx.t =  _localctx.pathElement.t; 
					}
					} 
				}
				setState(1430);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,150,_ctx);
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
		public TerminalNode METHOD_POINTER() { return getToken(GroovyParser.METHOD_POINTER, 0); }
		public TerminalNode METHOD_REFERENCE() { return getToken(GroovyParser.METHOD_REFERENCE, 0); }
		public TerminalNode DOT() { return getToken(GroovyParser.DOT, 0); }
		public TerminalNode SPREAD_DOT() { return getToken(GroovyParser.SPREAD_DOT, 0); }
		public TerminalNode SAFE_DOT() { return getToken(GroovyParser.SAFE_DOT, 0); }
		public TerminalNode SAFE_CHAIN_DOT() { return getToken(GroovyParser.SAFE_CHAIN_DOT, 0); }
		public TerminalNode AT() { return getToken(GroovyParser.AT, 0); }
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public TerminalNode NEW() { return getToken(GroovyParser.NEW, 0); }
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ClosureOrLambdaExpressionContext closureOrLambdaExpression() {
			return getRuleContext(ClosureOrLambdaExpressionContext.class,0);
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
			setState(1467);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,153,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1431);
				nls();
				setState(1442);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SPREAD_DOT:
				case SAFE_DOT:
				case SAFE_CHAIN_DOT:
				case DOT:
					{
					setState(1432);
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
					setState(1433);
					nls();
					setState(1436);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
					case 1:
						{
						setState(1434);
						match(AT);
						}
						break;

					case 2:
						{
						setState(1435);
						nonWildcardTypeArguments();
						}
						break;
					}
					}
					break;
				case METHOD_POINTER:
					{
					setState(1438);
					match(METHOD_POINTER);
					setState(1439);
					nls();
					}
					break;
				case METHOD_REFERENCE:
					{
					setState(1440);
					match(METHOD_REFERENCE);
					setState(1441);
					nls();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1444);
				namePart();
				 _localctx.t =  1; 
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1447);
				nls();
				setState(1448);
				match(DOT);
				setState(1449);
				nls();
				setState(1450);
				match(NEW);
				setState(1451);
				creator(1);
				 _localctx.t =  6; 
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1454);
				arguments();
				 _localctx.t =  2; 
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1457);
				nls();
				setState(1458);
				closureOrLambdaExpression();
				 _localctx.t =  3; 
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1461);
				indexPropertyArgs();
				 _localctx.t =  4; 
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1464);
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
			setState(1473);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,154,_ctx) ) {
			case 1:
				{
				setState(1469);
				identifier();
				}
				break;

			case 2:
				{
				setState(1470);
				stringLiteral();
				}
				break;

			case 3:
				{
				setState(1471);
				dynamicMemberName();
				}
				break;

			case 4:
				{
				setState(1472);
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
			setState(1477);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1475);
				parExpression();
				}
				break;
			case GStringBegin:
				enterOuterAlt(_localctx, 2);
				{
				setState(1476);
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
			setState(1480);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==QUESTION) {
				{
				setState(1479);
				match(QUESTION);
				}
			}

			setState(1482);
			match(LBRACK);
			setState(1484);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,157,_ctx) ) {
			case 1:
				{
				setState(1483);
				expressionList(true);
				}
				break;
			}
			setState(1486);
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
			setState(1489);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==QUESTION) {
				{
				setState(1488);
				match(QUESTION);
				}
			}

			setState(1491);
			match(LBRACK);
			setState(1494);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				{
				setState(1492);
				mapEntryList();
				}
				break;

			case 2:
				{
				setState(1493);
				match(COLON);
				}
				break;
			}
			setState(1496);
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
			setState(1515);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,161,_ctx) ) {
			case 1:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1498);
				identifier();
				setState(1500);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
				case 1:
					{
					setState(1499);
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
				setState(1502);
				literal();
				}
				break;

			case 3:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1503);
				gstring();
				}
				break;

			case 4:
				_localctx = new NewPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1504);
				match(NEW);
				setState(1505);
				nls();
				setState(1506);
				creator(0);
				}
				break;

			case 5:
				_localctx = new ThisPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1508);
				match(THIS);
				}
				break;

			case 6:
				_localctx = new SuperPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1509);
				match(SUPER);
				}
				break;

			case 7:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1510);
				parExpression();
				}
				break;

			case 8:
				_localctx = new ClosureOrLambdaExpressionPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1511);
				closureOrLambdaExpression();
				}
				break;

			case 9:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1512);
				list();
				}
				break;

			case 10:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1513);
				map();
				}
				break;

			case 11:
				_localctx = new BuiltInTypePrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1514);
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
		enterRule(_localctx, 252, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1517);
			match(LBRACK);
			setState(1519);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,162,_ctx) ) {
			case 1:
				{
				setState(1518);
				expressionList(true);
				}
				break;
			}
			setState(1522);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1521);
				match(COMMA);
				}
			}

			setState(1524);
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
		enterRule(_localctx, 254, RULE_map);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1526);
			match(LBRACK);
			setState(1532);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,165,_ctx) ) {
			case 1:
				{
				setState(1527);
				mapEntryList();
				setState(1529);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1528);
					match(COMMA);
					}
				}

				}
				break;

			case 2:
				{
				setState(1531);
				match(COLON);
				}
				break;
			}
			setState(1534);
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
		enterRule(_localctx, 256, RULE_mapEntryList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1536);
			mapEntry();
			setState(1541);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,166,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1537);
					match(COMMA);
					setState(1538);
					mapEntry();
					}
					} 
				}
				setState(1543);
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
		enterRule(_localctx, 258, RULE_mapEntry);
		try {
			setState(1554);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,167,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1544);
				mapEntryLabel();
				setState(1545);
				match(COLON);
				setState(1546);
				nls();
				setState(1547);
				expression(0);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1549);
				match(MUL);
				setState(1550);
				match(COLON);
				setState(1551);
				nls();
				setState(1552);
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
		enterRule(_localctx, 260, RULE_mapEntryLabel);
		try {
			setState(1558);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1556);
				keywords();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1557);
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
		public DimsOptContext dimsOpt() {
			return getRuleContext(DimsOptContext.class,0);
		}
		public DimsContext dims() {
			return getRuleContext(DimsContext.class,0);
		}
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public AnonymousInnerClassDeclarationContext anonymousInnerClassDeclaration() {
			return getRuleContext(AnonymousInnerClassDeclarationContext.class,0);
		}
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
		public List<? extends ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<? extends TerminalNode> RBRACK() { return getTokens(GroovyParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(GroovyParser.RBRACK, i);
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
		enterRule(_localctx, 262, RULE_creator);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1560);
			createdName();
			setState(1584);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,171,_ctx) ) {
			case 1:
				{
				setState(1561);
				if (!(0 == _localctx.t || 1 == _localctx.t)) throw createFailedPredicateException("0 == $t || 1 == $t");
				setState(1562);
				nls();
				setState(1563);
				arguments();
				setState(1565);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,169,_ctx) ) {
				case 1:
					{
					setState(1564);
					anonymousInnerClassDeclaration(0);
					}
					break;
				}
				}
				break;

			case 2:
				{
				setState(1567);
				if (!(0 == _localctx.t)) throw createFailedPredicateException("0 == $t");
				setState(1573); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1568);
						annotationsOpt();
						setState(1569);
						match(LBRACK);
						setState(1570);
						expression(0);
						setState(1571);
						match(RBRACK);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1575); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1577);
				dimsOpt();
				}
				break;

			case 3:
				{
				setState(1579);
				if (!(0 == _localctx.t)) throw createFailedPredicateException("0 == $t");
				setState(1580);
				dims();
				setState(1581);
				nls();
				setState(1582);
				arrayInitializer();
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
		enterRule(_localctx, 264, RULE_arrayInitializer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1586);
			match(LBRACE);
			setState(1587);
			nls();
			setState(1589);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,172,_ctx) ) {
			case 1:
				{
				setState(1588);
				variableInitializers();
				}
				break;
			}
			setState(1591);
			nls();
			setState(1592);
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
		enterRule(_localctx, 266, RULE_anonymousInnerClassDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1594);
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
		enterRule(_localctx, 268, RULE_createdName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1596);
			annotationsOpt();
			setState(1602);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				{
				setState(1597);
				primitiveType();
				}
				break;

			case 2:
				{
				setState(1598);
				qualifiedClassName();
				setState(1600);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,173,_ctx) ) {
				case 1:
					{
					setState(1599);
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
		enterRule(_localctx, 270, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1604);
			match(LT);
			setState(1605);
			nls();
			setState(1606);
			typeList();
			setState(1607);
			nls();
			setState(1608);
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
		enterRule(_localctx, 272, RULE_typeArgumentsOrDiamond);
		try {
			setState(1613);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,175,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1610);
				match(LT);
				setState(1611);
				match(GT);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1612);
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
		enterRule(_localctx, 274, RULE_arguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1615);
			match(LPAREN);
			setState(1617);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
			case 1:
				{
				setState(1616);
				enhancedArgumentList();
				}
				break;
			}
			setState(1620);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
			case 1:
				{
				setState(1619);
				match(COMMA);
				}
				break;
			}
			setState(1622);
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
		enterRule(_localctx, 276, RULE_argumentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1624);
			argumentListElement();
			setState(1631);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,178,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1625);
					match(COMMA);
					setState(1626);
					nls();
					setState(1627);
					argumentListElement();
					}
					} 
				}
				setState(1633);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,178,_ctx);
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
		enterRule(_localctx, 278, RULE_enhancedArgumentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1634);
			enhancedArgumentListElement();
			setState(1641);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,179,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1635);
					match(COMMA);
					setState(1636);
					nls();
					setState(1637);
					enhancedArgumentListElement();
					}
					} 
				}
				setState(1643);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,179,_ctx);
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
		enterRule(_localctx, 280, RULE_argumentListElement);
		try {
			setState(1646);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1644);
				expressionListElement(true);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1645);
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
		enterRule(_localctx, 282, RULE_enhancedArgumentListElement);
		try {
			setState(1651);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1648);
				expressionListElement(true);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1649);
				standardLambdaExpression();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1650);
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
		enterRule(_localctx, 284, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1653);
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
		enterRule(_localctx, 286, RULE_className);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1655);
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
		public TerminalNode STATIC() { return getToken(GroovyParser.STATIC, 0); }
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
		enterRule(_localctx, 288, RULE_identifier);
		try {
			setState(1665);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,182,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1657);
				match(Identifier);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1658);
				match(CapitalizedIdentifier);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1659);
				match(VAR);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1660);
				if (!( DOT == _input.LT(2).getType() )) throw createFailedPredicateException(" DOT == _input.LT(2).getType() ");
				setState(1661);
				match(STATIC);
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1662);
				match(IN);
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1663);
				match(TRAIT);
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1664);
				match(AS);
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
		enterRule(_localctx, 290, RULE_builtInType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1667);
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
		enterRule(_localctx, 292, RULE_keywords);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1669);
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
		enterRule(_localctx, 294, RULE_rparen);
		try {
			setState(1673);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1671);
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
		enterRule(_localctx, 296, RULE_nls);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1678);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1675);
					match(NL);
					}
					} 
				}
				setState(1680);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
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
		enterRule(_localctx, 298, RULE_sep);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1682); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1681);
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
				setState(1684); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,185,_ctx);
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
		case 18:
			return classDeclaration_sempred((ClassDeclarationContext)_localctx, predIndex);

		case 19:
			return classBody_sempred((ClassBodyContext)_localctx, predIndex);

		case 24:
			return methodDeclaration_sempred((MethodDeclarationContext)_localctx, predIndex);

		case 26:
			return returnType_sempred((ReturnTypeContext)_localctx, predIndex);

		case 79:
			return localVariableDeclaration_sempred((LocalVariableDeclarationContext)_localctx, predIndex);

		case 80:
			return classifiedModifiers_sempred((ClassifiedModifiersContext)_localctx, predIndex);

		case 93:
			return statement_sempred((StatementContext)_localctx, predIndex);

		case 115:
			return expression_sempred((ExpressionContext)_localctx, predIndex);

		case 117:
			return commandExpression_sempred((CommandExpressionContext)_localctx, predIndex);

		case 131:
			return creator_sempred((CreatorContext)_localctx, predIndex);

		case 144:
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
	private boolean returnType_sempred(ReturnTypeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 6:
			return  3 != _localctx.ct ;
		}
		return true;
	}
	private boolean localVariableDeclaration_sempred(LocalVariableDeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 7:
			return  !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ;
		}
		return true;
	}
	private boolean classifiedModifiers_sempred(ClassifiedModifiersContext _localctx, int predIndex) {
		switch (predIndex) {
		case 8:
			return  0 == _localctx.t ;

		case 9:
			return  1 == _localctx.t ;
		}
		return true;
	}
	private boolean statement_sempred(StatementContext _localctx, int predIndex) {
		switch (predIndex) {
		case 10:
			return  !SemanticPredicates.isInvalidMethodDeclaration(_input) ;
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 11:
			return precpred(_ctx, 17);

		case 12:
			return precpred(_ctx, 15);

		case 13:
			return precpred(_ctx, 14);

		case 14:
			return precpred(_ctx, 13);

		case 15:
			return precpred(_ctx, 11);

		case 16:
			return precpred(_ctx, 10);

		case 17:
			return precpred(_ctx, 9);

		case 18:
			return precpred(_ctx, 8);

		case 19:
			return precpred(_ctx, 7);

		case 20:
			return precpred(_ctx, 6);

		case 21:
			return precpred(_ctx, 5);

		case 22:
			return precpred(_ctx, 4);

		case 23:
			return precpred(_ctx, 3);

		case 24:
			return precpred(_ctx, 12);

		case 25:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean commandExpression_sempred(CommandExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 26:
			return  !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) ;
		}
		return true;
	}
	private boolean creator_sempred(CreatorContext _localctx, int predIndex) {
		switch (predIndex) {
		case 27:
			return 0 == _localctx.t || 1 == _localctx.t;

		case 28:
			return 0 == _localctx.t;

		case 29:
			return 0 == _localctx.t;
		}
		return true;
	}
	private boolean identifier_sempred(IdentifierContext _localctx, int predIndex) {
		switch (predIndex) {
		case 30:
			return  DOT == _input.LT(2).getType() ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\3\u0083\u0699\4\2\t"+
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
		"\t\u0097\3\2\3\2\5\2\u0131\n\2\3\2\5\2\u0134\n\2\3\2\5\2\u0137\n\2\3\2"+
		"\3\2\3\3\3\3\3\3\3\3\7\3\u013f\n\3\f\3\16\3\u0142\13\3\3\3\5\3\u0145\n"+
		"\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\5\5\u014e\n\5\3\5\3\5\3\5\3\5\3\5\5\5\u0155"+
		"\n\5\3\6\3\6\3\6\3\7\3\7\5\7\u015c\n\7\3\b\5\b\u015f\n\b\3\t\3\t\3\t\6"+
		"\t\u0164\n\t\r\t\16\t\u0165\3\n\5\n\u0169\n\n\3\13\3\13\3\13\6\13\u016e"+
		"\n\13\r\13\16\13\u016f\3\f\3\f\5\f\u0174\n\f\3\r\3\r\5\r\u0178\n\r\3\16"+
		"\5\16\u017b\n\16\3\17\3\17\3\17\6\17\u0180\n\17\r\17\16\17\u0181\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\7\20\u018b\n\20\f\20\16\20\u018e\13\20"+
		"\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\5\21\u0198\n\21\3\22\3\22\3\22"+
		"\3\22\3\22\7\22\u019f\n\22\f\22\16\22\u01a2\13\22\3\23\3\23\3\23\3\23"+
		"\3\23\7\23\u01a9\n\23\f\23\16\23\u01ac\13\23\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\5\24\u01b9\n\24\3\24\3\24\3\24\3\24\5\24"+
		"\u01bf\n\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\5\24\u01c8\n\24\3\24\3"+
		"\24\5\24\u01cc\n\24\3\24\5\24\u01cf\n\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\5\24\u01d7\n\24\3\24\5\24\u01da\n\24\3\24\5\24\u01dd\n\24\3\24\3\24\3"+
		"\25\3\25\3\25\3\25\5\25\u01e5\n\25\3\25\5\25\u01e8\n\25\3\25\5\25\u01eb"+
		"\n\25\3\25\5\25\u01ee\n\25\3\25\3\25\3\25\7\25\u01f3\n\25\f\25\16\25\u01f6"+
		"\13\25\3\25\5\25\u01f9\n\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26\7"+
		"\26\u0203\n\26\f\26\16\26\u0206\13\26\3\26\3\26\3\26\5\26\u020b\n\26\3"+
		"\27\3\27\3\27\5\27\u0210\n\27\3\27\5\27\u0213\n\27\3\30\3\30\3\30\5\30"+
		"\u0218\n\30\3\30\3\30\5\30\u021c\n\30\3\31\3\31\3\31\3\31\3\31\5\31\u0223"+
		"\n\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u022e\n\32\3\32"+
		"\3\32\5\32\u0232\n\32\3\32\5\32\u0235\n\32\3\32\3\32\3\32\3\32\3\32\3"+
		"\32\3\32\5\32\u023e\n\32\3\32\3\32\5\32\u0242\n\32\5\32\u0244\n\32\3\33"+
		"\3\33\5\33\u0248\n\33\3\34\3\34\3\34\5\34\u024d\n\34\3\35\3\35\3\36\3"+
		"\36\3\36\3\36\3\36\7\36\u0256\n\36\f\36\16\36\u0259\13\36\3\37\3\37\3"+
		"\37\3\37\3\37\3\37\5\37\u0261\n\37\3 \3 \3!\3!\3\"\3\"\3\"\3\"\3\"\3\""+
		"\3\"\7\"\u026e\n\"\f\"\16\"\u0271\13\"\3\"\3\"\5\"\u0275\n\"\3#\3#\3#"+
		"\3#\6#\u027b\n#\r#\16#\u027c\3$\5$\u0280\n$\3%\3%\3%\5%\u0285\n%\3%\3"+
		"%\3&\3&\3&\5&\u028c\n&\3&\5&\u028f\n&\3&\3&\3\'\3\'\5\'\u0295\n\'\3\'"+
		"\5\'\u0298\n\'\3(\3(\5(\u029c\n(\3)\3)\5)\u02a0\n)\3*\3*\3+\3+\3+\3+\3"+
		"+\3+\3+\7+\u02ab\n+\f+\16+\u02ae\13+\3+\3+\3+\3,\3,\3,\3,\3,\3,\3,\5,"+
		"\u02ba\n,\5,\u02bc\n,\3-\3-\3-\3.\3.\3.\3.\3.\7.\u02c6\n.\f.\16.\u02c9"+
		"\13.\3/\3/\5/\u02cd\n/\3/\3/\3\60\3\60\5\60\u02d3\n\60\3\60\3\60\3\60"+
		"\3\60\7\60\u02d9\n\60\f\60\16\60\u02dc\13\60\3\61\3\61\3\61\3\62\3\62"+
		"\5\62\u02e3\n\62\3\62\5\62\u02e6\n\62\3\62\3\62\3\62\3\62\3\62\3\62\5"+
		"\62\u02ee\n\62\3\63\3\63\3\64\3\64\3\64\7\64\u02f5\n\64\f\64\16\64\u02f8"+
		"\13\64\3\65\3\65\3\65\3\65\3\65\5\65\u02ff\n\65\3\66\3\66\3\66\7\66\u0304"+
		"\n\66\f\66\16\66\u0307\13\66\3\67\3\67\3\67\38\38\38\38\78\u0310\n8\f"+
		"8\168\u0313\138\39\39\39\39\39\59\u031a\n9\3:\3:\3:\3:\7:\u0320\n:\f:"+
		"\16:\u0323\13:\3:\3:\3;\3;\3;\5;\u032a\n;\3;\3;\5;\u032e\n;\3<\3<\7<\u0332"+
		"\n<\f<\16<\u0335\13<\3=\3=\3=\3=\3=\3=\3>\3>\3>\3>\3>\3>\3?\3?\3@\3@\5"+
		"@\u0347\n@\3A\3A\5A\u034b\nA\3B\3B\3B\5B\u0350\nB\3B\3B\3B\3B\5B\u0356"+
		"\nB\3B\3B\3B\3C\3C\5C\u035d\nC\3D\5D\u0360\nD\3E\3E\3E\3E\7E\u0366\nE"+
		"\fE\16E\u0369\13E\3E\5E\u036c\nE\3F\3F\3F\7F\u0371\nF\fF\16F\u0374\13"+
		"F\3G\3G\3G\3G\5G\u037a\nG\3G\5G\u037d\nG\3H\3H\5H\u0381\nH\3I\3I\3J\3"+
		"J\3J\7J\u0388\nJ\fJ\16J\u038b\13J\3K\3K\3K\3K\3K\3K\3L\3L\5L\u0395\nL"+
		"\3M\3M\3M\5M\u039a\nM\3N\3N\3N\3N\7N\u03a0\nN\fN\16N\u03a3\13N\5N\u03a5"+
		"\nN\3N\5N\u03a8\nN\3N\3N\3O\3O\3O\7O\u03af\nO\fO\16O\u03b2\13O\5O\u03b4"+
		"\nO\3O\3O\3O\3P\3P\5P\u03bb\nP\3Q\3Q\3Q\3R\3R\3R\3R\5R\u03c4\nR\3S\3S"+
		"\5S\u03c8\nS\3S\3S\3S\3S\3S\3S\3S\5S\u03d1\nS\3S\3S\3S\3S\3S\3S\3S\5S"+
		"\u03da\nS\3T\3T\3T\3T\7T\u03e0\nT\fT\16T\u03e3\13T\3T\3T\3U\5U\u03e8\n"+
		"U\3U\3U\3V\3V\3V\3V\6V\u03f0\nV\rV\16V\u03f1\3V\3V\3W\3W\5W\u03f8\nW\3"+
		"X\3X\3X\3X\3X\3X\5X\u0400\nX\3X\3X\3X\3X\5X\u0406\nX\3Y\3Y\3Y\3Y\3Y\3"+
		"Y\7Y\u040e\nY\fY\16Y\u0411\13Y\3Y\3Y\3Y\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z"+
		"\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\3Z\5Z\u0429\nZ\3[\3[\5[\u042d\n[\3\\\3\\\5\\"+
		"\u0431\n\\\3]\3]\5]\u0435\n]\3]\3]\3]\3]\3]\7]\u043c\n]\f]\16]\u043f\13"+
		"]\3]\3]\3]\5]\u0444\n]\3^\3^\3^\3^\3^\3^\3^\5^\u044d\n^\3_\3_\3_\3_\3"+
		"_\3_\3_\3_\3_\3_\3_\5_\u045a\n_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3_\3"+
		"_\3_\3_\3_\3_\5_\u046d\n_\3`\3`\3`\3`\5`\u0473\n`\3`\3`\3`\3`\3`\3a\3"+
		"a\3a\7a\u047d\na\fa\16a\u0480\13a\3b\3b\3b\3b\3c\3c\3c\3c\5c\u048a\nc"+
		"\3c\3c\3d\3d\3d\3d\7d\u0492\nd\fd\16d\u0495\13d\3e\3e\5e\u0499\ne\3f\3"+
		"f\3f\6f\u049e\nf\rf\16f\u049f\3f\3f\3g\3g\3g\3g\3g\3g\5g\u04aa\ng\3h\3"+
		"h\5h\u04ae\nh\3i\3i\5i\u04b2\ni\3i\3i\3i\3i\3j\5j\u04b9\nj\3j\3j\5j\u04bd"+
		"\nj\3j\3j\5j\u04c1\nj\3k\3k\5k\u04c5\nk\3l\3l\3m\3m\3m\3m\3n\3n\3o\3o"+
		"\3o\3o\3p\3p\3p\7p\u04d6\np\fp\16p\u04d9\13p\3q\3q\3q\5q\u04de\nq\3q\3"+
		"q\3r\3r\5r\u04e4\nr\3s\3s\3t\3t\5t\u04ea\nt\3u\3u\3u\3u\3u\3u\3u\3u\3"+
		"u\3u\3u\3u\3u\3u\3u\3u\3u\5u\u04fd\nu\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3"+
		"u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\5u\u0518\nu\3u\5u\u051b\n"+
		"u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3"+
		"u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3"+
		"u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\5u\u055b\nu\3u\3u\3"+
		"u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\3u\7u\u056b\nu\fu\16u\u056e\13u\3v\3v"+
		"\3v\3v\3v\3v\3v\3v\3v\3v\5v\u057a\nv\3w\3w\3w\3w\5w\u0580\nw\3w\7w\u0583"+
		"\nw\fw\16w\u0586\13w\3x\3x\6x\u058a\nx\rx\16x\u058b\3x\5x\u058f\nx\3y"+
		"\3y\3y\3y\7y\u0595\ny\fy\16y\u0598\13y\3z\3z\3z\3z\3z\5z\u059f\nz\3z\3"+
		"z\3z\3z\5z\u05a5\nz\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3"+
		"z\3z\3z\3z\3z\3z\3z\5z\u05be\nz\3{\3{\3{\3{\5{\u05c4\n{\3|\3|\5|\u05c8"+
		"\n|\3}\5}\u05cb\n}\3}\3}\5}\u05cf\n}\3}\3}\3~\5~\u05d4\n~\3~\3~\3~\5~"+
		"\u05d9\n~\3~\3~\3\177\3\177\5\177\u05df\n\177\3\177\3\177\3\177\3\177"+
		"\3\177\3\177\3\177\3\177\3\177\3\177\3\177\3\177\3\177\5\177\u05ee\n\177"+
		"\3\u0080\3\u0080\5\u0080\u05f2\n\u0080\3\u0080\5\u0080\u05f5\n\u0080\3"+
		"\u0080\3\u0080\3\u0081\3\u0081\3\u0081\5\u0081\u05fc\n\u0081\3\u0081\5"+
		"\u0081\u05ff\n\u0081\3\u0081\3\u0081\3\u0082\3\u0082\3\u0082\7\u0082\u0606"+
		"\n\u0082\f\u0082\16\u0082\u0609\13\u0082\3\u0083\3\u0083\3\u0083\3\u0083"+
		"\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\5\u0083\u0615\n\u0083"+
		"\3\u0084\3\u0084\5\u0084\u0619\n\u0084\3\u0085\3\u0085\3\u0085\3\u0085"+
		"\3\u0085\5\u0085\u0620\n\u0085\3\u0085\3\u0085\3\u0085\3\u0085\3\u0085"+
		"\3\u0085\6\u0085\u0628\n\u0085\r\u0085\16\u0085\u0629\3\u0085\3\u0085"+
		"\3\u0085\3\u0085\3\u0085\3\u0085\3\u0085\5\u0085\u0633\n\u0085\3\u0086"+
		"\3\u0086\3\u0086\5\u0086\u0638\n\u0086\3\u0086\3\u0086\3\u0086\3\u0087"+
		"\3\u0087\3\u0088\3\u0088\3\u0088\3\u0088\5\u0088\u0643\n\u0088\5\u0088"+
		"\u0645\n\u0088\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u0089\3\u008a"+
		"\3\u008a\3\u008a\5\u008a\u0650\n\u008a\3\u008b\3\u008b\5\u008b\u0654\n"+
		"\u008b\3\u008b\5\u008b\u0657\n\u008b\3\u008b\3\u008b\3\u008c\3\u008c\3"+
		"\u008c\3\u008c\3\u008c\7\u008c\u0660\n\u008c\f\u008c\16\u008c\u0663\13"+
		"\u008c\3\u008d\3\u008d\3\u008d\3\u008d\3\u008d\7\u008d\u066a\n\u008d\f"+
		"\u008d\16\u008d\u066d\13\u008d\3\u008e\3\u008e\5\u008e\u0671\n\u008e\3"+
		"\u008f\3\u008f\3\u008f\5\u008f\u0676\n\u008f\3\u0090\3\u0090\3\u0091\3"+
		"\u0091\3\u0092\3\u0092\3\u0092\3\u0092\3\u0092\3\u0092\3\u0092\3\u0092"+
		"\5\u0092\u0684\n\u0092\3\u0093\3\u0093\3\u0094\3\u0094\3\u0095\3\u0095"+
		"\5\u0095\u068c\n\u0095\3\u0096\7\u0096\u068f\n\u0096\f\u0096\16\u0096"+
		"\u0692\13\u0096\3\u0097\6\u0097\u0695\n\u0097\r\u0097\16\u0097\u0696\3"+
		"\u0097\2\2\3\u00e8\u0098\2\2\4\2\6\2\b\2\n\2\f\2\16\2\20\2\22\2\24\2\26"+
		"\2\30\2\32\2\34\2\36\2 \2\"\2$\2&\2(\2*\2,\2.\2\60\2\62\2\64\2\66\28\2"+
		":\2<\2>\2@\2B\2D\2F\2H\2J\2L\2N\2P\2R\2T\2V\2X\2Z\2\\\2^\2`\2b\2d\2f\2"+
		"h\2j\2l\2n\2p\2r\2t\2v\2x\2z\2|\2~\2\u0080\2\u0082\2\u0084\2\u0086\2\u0088"+
		"\2\u008a\2\u008c\2\u008e\2\u0090\2\u0092\2\u0094\2\u0096\2\u0098\2\u009a"+
		"\2\u009c\2\u009e\2\u00a0\2\u00a2\2\u00a4\2\u00a6\2\u00a8\2\u00aa\2\u00ac"+
		"\2\u00ae\2\u00b0\2\u00b2\2\u00b4\2\u00b6\2\u00b8\2\u00ba\2\u00bc\2\u00be"+
		"\2\u00c0\2\u00c2\2\u00c4\2\u00c6\2\u00c8\2\u00ca\2\u00cc\2\u00ce\2\u00d0"+
		"\2\u00d2\2\u00d4\2\u00d6\2\u00d8\2\u00da\2\u00dc\2\u00de\2\u00e0\2\u00e2"+
		"\2\u00e4\2\u00e6\2\u00e8\2\u00ea\2\u00ec\2\u00ee\2\u00f0\2\u00f2\2\u00f4"+
		"\2\u00f6\2\u00f8\2\u00fa\2\u00fc\2\u00fe\2\u0100\2\u0102\2\u0104\2\u0106"+
		"\2\u0108\2\u010a\2\u010c\2\u010e\2\u0110\2\u0112\2\u0114\2\u0116\2\u0118"+
		"\2\u011a\2\u011c\2\u011e\2\u0120\2\u0122\2\u0124\2\u0126\2\u0128\2\u012a"+
		"\2\u012c\2\2\27\b\2\n\n\16\16&&\61\61\65\6588\7\2\20\20\30\30\35\35)+"+
		"-.\b\2\n\n\16\16\20\20\35\35)+-.\4\2\34\34//\4\2WW__\4\2\13\13__\3\2f"+
		"g\3\2\\]\3\2fi\4\2jkoo\3\2hi\3\2>?\6\2\13\13OOZ[ab\5\2JL``cc\3\2FG\5\2"+
		"\t\t$$NN\5\2IIYYp{\4\2@BXX\4\2\17\17\67\67\4\2\t9<=\4\2VV\u0081\u0081"+
		"\2\u0707\2\u012e\3\2\2\2\4\u013a\3\2\2\2\6\u0146\3\2\2\2\b\u014a\3\2\2"+
		"\2\n\u0156\3\2\2\2\f\u015b\3\2\2\2\16\u015e\3\2\2\2\20\u0163\3\2\2\2\22"+
		"\u0168\3\2\2\2\24\u016d\3\2\2\2\26\u0173\3\2\2\2\30\u0177\3\2\2\2\32\u017a"+
		"\3\2\2\2\34\u017f\3\2\2\2\36\u0183\3\2\2\2 \u0192\3\2\2\2\"\u0199\3\2"+
		"\2\2$\u01a3\3\2\2\2&\u01b8\3\2\2\2(\u01e0\3\2\2\2*\u01fc\3\2\2\2,\u020c"+
		"\3\2\2\2.\u021b\3\2\2\2\60\u0222\3\2\2\2\62\u0243\3\2\2\2\64\u0247\3\2"+
		"\2\2\66\u024c\3\2\2\28\u024e\3\2\2\2:\u0250\3\2\2\2<\u025a\3\2\2\2>\u0262"+
		"\3\2\2\2@\u0264\3\2\2\2B\u0266\3\2\2\2D\u027a\3\2\2\2F\u027f\3\2\2\2H"+
		"\u0281\3\2\2\2J\u0288\3\2\2\2L\u0294\3\2\2\2N\u0299\3\2\2\2P\u029d\3\2"+
		"\2\2R\u02a1\3\2\2\2T\u02a3\3\2\2\2V\u02bb\3\2\2\2X\u02bd\3\2\2\2Z\u02c0"+
		"\3\2\2\2\\\u02ca\3\2\2\2^\u02d2\3\2\2\2`\u02dd\3\2\2\2b\u02e0\3\2\2\2"+
		"d\u02ef\3\2\2\2f\u02f1\3\2\2\2h\u02fe\3\2\2\2j\u0305\3\2\2\2l\u0308\3"+
		"\2\2\2n\u030b\3\2\2\2p\u0319\3\2\2\2r\u031b\3\2\2\2t\u032d\3\2\2\2v\u032f"+
		"\3\2\2\2x\u0336\3\2\2\2z\u033c\3\2\2\2|\u0342\3\2\2\2~\u0346\3\2\2\2\u0080"+
		"\u034a\3\2\2\2\u0082\u034c\3\2\2\2\u0084\u035c\3\2\2\2\u0086\u035f\3\2"+
		"\2\2\u0088\u0361\3\2\2\2\u008a\u0372\3\2\2\2\u008c\u0375\3\2\2\2\u008e"+
		"\u0380\3\2\2\2\u0090\u0382\3\2\2\2\u0092\u0384\3\2\2\2\u0094\u038c\3\2"+
		"\2\2\u0096\u0394\3\2\2\2\u0098\u0399\3\2\2\2\u009a\u039b\3\2\2\2\u009c"+
		"\u03ab\3\2\2\2\u009e\u03ba\3\2\2\2\u00a0\u03bc\3\2\2\2\u00a2\u03c3\3\2"+
		"\2\2\u00a4\u03d9\3\2\2\2\u00a6\u03db\3\2\2\2\u00a8\u03e7\3\2\2\2\u00aa"+
		"\u03eb\3\2\2\2\u00ac\u03f7\3\2\2\2\u00ae\u03f9\3\2\2\2\u00b0\u0407\3\2"+
		"\2\2\u00b2\u0428\3\2\2\2\u00b4\u042a\3\2\2\2\u00b6\u042e\3\2\2\2\u00b8"+
		"\u0432\3\2\2\2\u00ba\u0445\3\2\2\2\u00bc\u046c\3\2\2\2\u00be\u046e\3\2"+
		"\2\2\u00c0\u0479\3\2\2\2\u00c2\u0481\3\2\2\2\u00c4\u0485\3\2\2\2\u00c6"+
		"\u048d\3\2\2\2\u00c8\u0498\3\2\2\2\u00ca\u049d\3\2\2\2\u00cc\u04a9\3\2"+
		"\2\2\u00ce\u04ad\3\2\2\2\u00d0\u04af\3\2\2\2\u00d2\u04b8\3\2\2\2\u00d4"+
		"\u04c4\3\2\2\2\u00d6\u04c6\3\2\2\2\u00d8\u04c8\3\2\2\2\u00da\u04cc\3\2"+
		"\2\2\u00dc\u04ce\3\2\2\2\u00de\u04d2\3\2\2\2\u00e0\u04dd\3\2\2\2\u00e2"+
		"\u04e3\3\2\2\2\u00e4\u04e5\3\2\2\2\u00e6\u04e7\3\2\2\2\u00e8\u04fc\3\2"+
		"\2\2\u00ea\u0579\3\2\2\2\u00ec\u057b\3\2\2\2\u00ee\u0587\3\2\2\2\u00f0"+
		"\u0590\3\2\2\2\u00f2\u05bd\3\2\2\2\u00f4\u05c3\3\2\2\2\u00f6\u05c7\3\2"+
		"\2\2\u00f8\u05ca\3\2\2\2\u00fa\u05d3\3\2\2\2\u00fc\u05ed\3\2\2\2\u00fe"+
		"\u05ef\3\2\2\2\u0100\u05f8\3\2\2\2\u0102\u0602\3\2\2\2\u0104\u0614\3\2"+
		"\2\2\u0106\u0618\3\2\2\2\u0108\u061a\3\2\2\2\u010a\u0634\3\2\2\2\u010c"+
		"\u063c\3\2\2\2\u010e\u063e\3\2\2\2\u0110\u0646\3\2\2\2\u0112\u064f\3\2"+
		"\2\2\u0114\u0651\3\2\2\2\u0116\u065a\3\2\2\2\u0118\u0664\3\2\2\2\u011a"+
		"\u0670\3\2\2\2\u011c\u0675\3\2\2\2\u011e\u0677\3\2\2\2\u0120\u0679\3\2"+
		"\2\2\u0122\u0683\3\2\2\2\u0124\u0685\3\2\2\2\u0126\u0687\3\2\2\2\u0128"+
		"\u068b\3\2\2\2\u012a\u0690\3\2\2\2\u012c\u0694\3\2\2\2\u012e\u0130\5\u012a"+
		"\u0096\2\u012f\u0131\5\6\4\2\u0130\u012f\3\2\2\2\u0130\u0131\3\2\2\2\u0131"+
		"\u0133\3\2\2\2\u0132\u0134\5\u012c\u0097\2\u0133\u0132\3\2\2\2\u0133\u0134"+
		"\3\2\2\2\u0134\u0136\3\2\2\2\u0135\u0137\5\4\3\2\u0136\u0135\3\2\2\2\u0136"+
		"\u0137\3\2\2\2\u0137\u0138\3\2\2\2\u0138\u0139\7\2\2\3\u0139\3\3\2\2\2"+
		"\u013a\u0140\5\u00bc_\2\u013b\u013c\5\u012c\u0097\2\u013c\u013d\5\u00bc"+
		"_\2\u013d\u013f\3\2\2\2\u013e\u013b\3\2\2\2\u013f\u0142\3\2\2\2\u0140"+
		"\u013e\3\2\2\2\u0140\u0141\3\2\2\2\u0141\u0144\3\2\2\2\u0142\u0140\3\2"+
		"\2\2\u0143\u0145\5\u012c\u0097\2\u0144\u0143\3\2\2\2\u0144\u0145\3\2\2"+
		"\2\u0145\5\3\2\2\2\u0146\u0147\5\u008aF\2\u0147\u0148\7(\2\2\u0148\u0149"+
		"\5f\64\2\u0149\7\3\2\2\2\u014a\u014b\5\u008aF\2\u014b\u014d\7#\2\2\u014c"+
		"\u014e\7-\2\2\u014d\u014c\3\2\2\2\u014d\u014e\3\2\2\2\u014e\u014f\3\2"+
		"\2\2\u014f\u0154\5f\64\2\u0150\u0151\7X\2\2\u0151\u0155\7j\2\2\u0152\u0153"+
		"\7\t\2\2\u0153\u0155\5\u0122\u0092\2\u0154\u0150\3\2\2\2\u0154\u0152\3"+
		"\2\2\2\u0154\u0155\3\2\2\2\u0155\t\3\2\2\2\u0156\u0157\5\22\n\2\u0157"+
		"\u0158\5&\24\2\u0158\13\3\2\2\2\u0159\u015c\5\26\f\2\u015a\u015c\t\2\2"+
		"\2\u015b\u0159\3\2\2\2\u015b\u015a\3\2\2\2\u015c\r\3\2\2\2\u015d\u015f"+
		"\5\20\t\2\u015e\u015d\3\2\2\2\u015e\u015f\3\2\2\2\u015f\17\3\2\2\2\u0160"+
		"\u0161\5\f\7\2\u0161\u0162\5\u012a\u0096\2\u0162\u0164\3\2\2\2\u0163\u0160"+
		"\3\2\2\2\u0164\u0165\3\2\2\2\u0165\u0163\3\2\2\2\u0165\u0166\3\2\2\2\u0166"+
		"\21\3\2\2\2\u0167\u0169\5\24\13\2\u0168\u0167\3\2\2\2\u0168\u0169\3\2"+
		"\2\2\u0169\23\3\2\2\2\u016a\u016b\5\26\f\2\u016b\u016c\5\u012a\u0096\2"+
		"\u016c\u016e\3\2\2\2\u016d\u016a\3\2\2\2\u016e\u016f\3\2\2\2\u016f\u016d"+
		"\3\2\2\2\u016f\u0170\3\2\2\2\u0170\25\3\2\2\2\u0171\u0174\5\u008cG\2\u0172"+
		"\u0174\t\3\2\2\u0173\u0171\3\2\2\2\u0173\u0172\3\2\2\2\u0174\27\3\2\2"+
		"\2\u0175\u0178\5\u008cG\2\u0176\u0178\t\4\2\2\u0177\u0175\3\2\2\2\u0177"+
		"\u0176\3\2\2\2\u0178\31\3\2\2\2\u0179\u017b\5\34\17\2\u017a\u0179\3\2"+
		"\2\2\u017a\u017b\3\2\2\2\u017b\33\3\2\2\2\u017c\u017d\5\30\r\2\u017d\u017e"+
		"\5\u012a\u0096\2\u017e\u0180\3\2\2\2\u017f\u017c\3\2\2\2\u0180\u0181\3"+
		"\2\2\2\u0181\u017f\3\2\2\2\u0181\u0182\3\2\2\2\u0182\35\3\2\2\2\u0183"+
		"\u0184\7[\2\2\u0184\u0185\5\u012a\u0096\2\u0185\u018c\5 \21\2\u0186\u0187"+
		"\7W\2\2\u0187\u0188\5\u012a\u0096\2\u0188\u0189\5 \21\2\u0189\u018b\3"+
		"\2\2\2\u018a\u0186\3\2\2\2\u018b\u018e\3\2\2\2\u018c\u018a\3\2\2\2\u018c"+
		"\u018d\3\2\2\2\u018d\u018f\3\2\2\2\u018e\u018c\3\2\2\2\u018f\u0190\5\u012a"+
		"\u0096\2\u0190\u0191\7Z\2\2\u0191\37\3\2\2\2\u0192\u0197\5\u0120\u0091"+
		"\2\u0193\u0194\7\34\2\2\u0194\u0195\5\u012a\u0096\2\u0195\u0196\5\"\22"+
		"\2\u0196\u0198\3\2\2\2\u0197\u0193\3\2\2\2\u0197\u0198\3\2\2\2\u0198!"+
		"\3\2\2\2\u0199\u01a0\5J&\2\u019a\u019b\7l\2\2\u019b\u019c\5\u012a\u0096"+
		"\2\u019c\u019d\5J&\2\u019d\u019f\3\2\2\2\u019e\u019a\3\2\2\2\u019f\u01a2"+
		"\3\2\2\2\u01a0\u019e\3\2\2\2\u01a0\u01a1\3\2\2\2\u01a1#\3\2\2\2\u01a2"+
		"\u01a0\3\2\2\2\u01a3\u01aa\5J&\2\u01a4\u01a5\7W\2\2\u01a5\u01a6\5\u012a"+
		"\u0096\2\u01a6\u01a7\5J&\2\u01a7\u01a9\3\2\2\2\u01a8\u01a4\3\2\2\2\u01a9"+
		"\u01ac\3\2\2\2\u01aa\u01a8\3\2\2\2\u01aa\u01ab\3\2\2\2\u01ab%\3\2\2\2"+
		"\u01ac\u01aa\3\2\2\2\u01ad\u01ae\7\25\2\2\u01ae\u01b9\b\24\1\2\u01af\u01b0"+
		"\7%\2\2\u01b0\u01b9\b\24\1\2\u01b1\u01b2\7\33\2\2\u01b2\u01b9\b\24\1\2"+
		"\u01b3\u01b4\7~\2\2\u01b4\u01b5\7%\2\2\u01b5\u01b9\b\24\1\2\u01b6\u01b7"+
		"\7\f\2\2\u01b7\u01b9\b\24\1\2\u01b8\u01ad\3\2\2\2\u01b8\u01af\3\2\2\2"+
		"\u01b8\u01b1\3\2\2\2\u01b8\u01b3\3\2\2\2\u01b8\u01b6\3\2\2\2\u01b9\u01ba"+
		"\3\2\2\2\u01ba\u01bb\5\u0122\u0092\2\u01bb\u01dc\5\u012a\u0096\2\u01bc"+
		"\u01be\6\24\2\3\u01bd\u01bf\5\36\20\2\u01be\u01bd\3\2\2\2\u01be\u01bf"+
		"\3\2\2\2\u01bf\u01c0\3\2\2\2\u01c0\u01ce\5\u012a\u0096\2\u01c1\u01cb\6"+
		"\24\3\3\u01c2\u01c3\7\34\2\2\u01c3\u01c7\5\u012a\u0096\2\u01c4\u01c5\6"+
		"\24\4\3\u01c5\u01c8\5$\23\2\u01c6\u01c8\5J&\2\u01c7\u01c4\3\2\2\2\u01c7"+
		"\u01c6\3\2\2\2\u01c8\u01c9\3\2\2\2\u01c9\u01ca\5\u012a\u0096\2\u01ca\u01cc"+
		"\3\2\2\2\u01cb\u01c2\3\2\2\2\u01cb\u01cc\3\2\2\2\u01cc\u01cf\3\2\2\2\u01cd"+
		"\u01cf\3\2\2\2\u01ce\u01c1\3\2\2\2\u01ce\u01cd\3\2\2\2\u01cf\u01d9\3\2"+
		"\2\2\u01d0\u01d6\6\24\5\3\u01d1\u01d2\7\"\2\2\u01d2\u01d3\5\u012a\u0096"+
		"\2\u01d3\u01d4\5$\23\2\u01d4\u01d5\5\u012a\u0096\2\u01d5\u01d7\3\2\2\2"+
		"\u01d6\u01d1\3\2\2\2\u01d6\u01d7\3\2\2\2\u01d7\u01da\3\2\2\2\u01d8\u01da"+
		"\3\2\2\2\u01d9\u01d0\3\2\2\2\u01d9\u01d8\3\2\2\2\u01da\u01dd\3\2\2\2\u01db"+
		"\u01dd\3\2\2\2\u01dc\u01bc\3\2\2\2\u01dc\u01db\3\2\2\2\u01dd\u01de\3\2"+
		"\2\2\u01de\u01df\5(\25\2\u01df\'\3\2\2\2\u01e0\u01e1\7R\2\2\u01e1\u01ea"+
		"\5\u012a\u0096\2\u01e2\u01e4\6\25\6\3\u01e3\u01e5\5*\26\2\u01e4\u01e3"+
		"\3\2\2\2\u01e4\u01e5\3\2\2\2\u01e5\u01e7\3\2\2\2\u01e6\u01e8\5\u012c\u0097"+
		"\2\u01e7\u01e6\3\2\2\2\u01e7\u01e8\3\2\2\2\u01e8\u01eb\3\2\2\2\u01e9\u01eb"+
		"\3\2\2\2\u01ea\u01e2\3\2\2\2\u01ea\u01e9\3\2\2\2\u01eb\u01ed\3\2\2\2\u01ec"+
		"\u01ee\5.\30\2\u01ed\u01ec\3\2\2\2\u01ed\u01ee\3\2\2\2\u01ee\u01f4\3\2"+
		"\2\2\u01ef\u01f0\5\u012c\u0097\2\u01f0\u01f1\5.\30\2\u01f1\u01f3\3\2\2"+
		"\2\u01f2\u01ef\3\2\2\2\u01f3\u01f6\3\2\2\2\u01f4\u01f2\3\2\2\2\u01f4\u01f5"+
		"\3\2\2\2\u01f5\u01f8\3\2\2\2\u01f6\u01f4\3\2\2\2\u01f7\u01f9\5\u012c\u0097"+
		"\2\u01f8\u01f7\3\2\2\2\u01f8\u01f9\3\2\2\2\u01f9\u01fa\3\2\2\2\u01fa\u01fb"+
		"\7S\2\2\u01fb)\3\2\2\2\u01fc\u0204\5,\27\2\u01fd\u01fe\5\u012a\u0096\2"+
		"\u01fe\u01ff\7W\2\2\u01ff\u0200\5\u012a\u0096\2\u0200\u0201\5,\27\2\u0201"+
		"\u0203\3\2\2\2\u0202\u01fd\3\2\2\2\u0203\u0206\3\2\2\2\u0204\u0202\3\2"+
		"\2\2\u0204\u0205\3\2\2\2\u0205\u020a\3\2\2\2\u0206\u0204\3\2\2\2\u0207"+
		"\u0208\5\u012a\u0096\2\u0208\u0209\7W\2\2\u0209\u020b\3\2\2\2\u020a\u0207"+
		"\3\2\2\2\u020a\u020b\3\2\2\2\u020b+\3\2\2\2\u020c\u020d\5\u008aF\2\u020d"+
		"\u020f\5\u0122\u0092\2\u020e\u0210\5\u0114\u008b\2\u020f\u020e\3\2\2\2"+
		"\u020f\u0210\3\2\2\2\u0210\u0212\3\2\2\2\u0211\u0213\5\u010c\u0087\2\u0212"+
		"\u0211\3\2\2\2\u0212\u0213\3\2\2\2\u0213-\3\2\2\2\u0214\u021c\7V\2\2\u0215"+
		"\u0216\7-\2\2\u0216\u0218\5\u012a\u0096\2\u0217\u0215\3\2\2\2\u0217\u0218"+
		"\3\2\2\2\u0218\u0219\3\2\2\2\u0219\u021c\5\u009cO\2\u021a\u021c\5\60\31"+
		"\2\u021b\u0214\3\2\2\2\u021b\u0217\3\2\2\2\u021b\u021a\3\2\2\2\u021c/"+
		"\3\2\2\2\u021d\u0223\5\62\32\2\u021e\u0223\58\35\2\u021f\u0220\5\16\b"+
		"\2\u0220\u0221\5&\24\2\u0221\u0223\3\2\2\2\u0222\u021d\3\2\2\2\u0222\u021e"+
		"\3\2\2\2\u0222\u021f\3\2\2\2\u0223\61\3\2\2\2\u0224\u0225\6\32\7\3\u0225"+
		"\u0226\5\66\34\2\u0226\u0227\5\64\33\2\u0227\u0228\7P\2\2\u0228\u022d"+
		"\5\u0128\u0095\2\u0229\u022a\7\30\2\2\u022a\u022b\5\u012a\u0096\2\u022b"+
		"\u022c\5\u0098M\2\u022c\u022e\3\2\2\2\u022d\u0229\3\2\2\2\u022d\u022e"+
		"\3\2\2\2\u022e\u0244\3\2\2\2\u022f\u0231\5\16\b\2\u0230\u0232\5\36\20"+
		"\2\u0231\u0230\3\2\2\2\u0231\u0232\3\2\2\2\u0232\u0234\3\2\2\2\u0233\u0235"+
		"\5\66\34\2\u0234\u0233\3\2\2\2\u0234\u0235\3\2\2\2\u0235\u0236\3\2\2\2"+
		"\u0236\u0237\5\64\33\2\u0237\u023d\5\\/\2\u0238\u0239\5\u012a\u0096\2"+
		"\u0239\u023a\7\64\2\2\u023a\u023b\5\u012a\u0096\2\u023b\u023c\5Z.\2\u023c"+
		"\u023e\3\2\2\2\u023d\u0238\3\2\2\2\u023d\u023e\3\2\2\2\u023e\u023f\3\2"+
		"\2\2\u023f\u0241\5\u012a\u0096\2\u0240\u0242\5d\63\2\u0241\u0240\3\2\2"+
		"\2\u0241\u0242\3\2\2\2\u0242\u0244\3\2\2\2\u0243\u0224\3\2\2\2\u0243\u022f"+
		"\3\2\2\2\u0244\63\3\2\2\2\u0245\u0248\5\u0122\u0092\2\u0246\u0248\5\u011e"+
		"\u0090\2\u0247\u0245\3\2\2\2\u0247\u0246\3\2\2\2\u0248\65\3\2\2\2\u0249"+
		"\u024d\5H%\2\u024a\u024b\6\34\b\3\u024b\u024d\7\67\2\2\u024c\u0249\3\2"+
		"\2\2\u024c\u024a\3\2\2\2\u024d\67\3\2\2\2\u024e\u024f\5\u00a4S\2\u024f"+
		"9\3\2\2\2\u0250\u0257\5<\37\2\u0251\u0252\7W\2\2\u0252\u0253\5\u012a\u0096"+
		"\2\u0253\u0254\5<\37\2\u0254\u0256\3\2\2\2\u0255\u0251\3\2\2\2\u0256\u0259"+
		"\3\2\2\2\u0257\u0255\3\2\2\2\u0257\u0258\3\2\2\2\u0258;\3\2\2\2\u0259"+
		"\u0257\3\2\2\2\u025a\u0260\5> \2\u025b\u025c\5\u012a\u0096\2\u025c\u025d"+
		"\7Y\2\2\u025d\u025e\5\u012a\u0096\2\u025e\u025f\5@!\2\u025f\u0261\3\2"+
		"\2\2\u0260\u025b\3\2\2\2\u0260\u0261\3\2\2\2\u0261=\3\2\2\2\u0262\u0263"+
		"\5\u0122\u0092\2\u0263?\3\2\2\2\u0264\u0265\5\u00e2r\2\u0265A\3\2\2\2"+
		"\u0266\u0267\5@!\2\u0267\u026f\5\u012a\u0096\2\u0268\u0269\7W\2\2\u0269"+
		"\u026a\5\u012a\u0096\2\u026a\u026b\5@!\2\u026b\u026c\5\u012a\u0096\2\u026c"+
		"\u026e\3\2\2\2\u026d\u0268\3\2\2\2\u026e\u0271\3\2\2\2\u026f\u026d\3\2"+
		"\2\2\u026f\u0270\3\2\2\2\u0270\u0272\3\2\2\2\u0271\u026f\3\2\2\2\u0272"+
		"\u0274\5\u012a\u0096\2\u0273\u0275\7W\2\2\u0274\u0273\3\2\2\2\u0274\u0275"+
		"\3\2\2\2\u0275C\3\2\2\2\u0276\u0277\5\u008aF\2\u0277\u0278\7T\2\2\u0278"+
		"\u0279\7U\2\2\u0279\u027b\3\2\2\2\u027a\u0276\3\2\2\2\u027b\u027c\3\2"+
		"\2\2\u027c\u027a\3\2\2\2\u027c\u027d\3\2\2\2\u027dE\3\2\2\2\u027e\u0280"+
		"\5D#\2\u027f\u027e\3\2\2\2\u027f\u0280\3\2\2\2\u0280G\3\2\2\2\u0281\u0284"+
		"\5\u008aF\2\u0282\u0285\5R*\2\u0283\u0285\5P)\2\u0284\u0282\3\2\2\2\u0284"+
		"\u0283\3\2\2\2\u0285\u0286\3\2\2\2\u0286\u0287\5F$\2\u0287I\3\2\2\2\u0288"+
		"\u028e\5\u008aF\2\u0289\u028c\5R*\2\u028a\u028c\7\67\2\2\u028b\u0289\3"+
		"\2\2\2\u028b\u028a\3\2\2\2\u028c\u028f\3\2\2\2\u028d\u028f\5N(\2\u028e"+
		"\u028b\3\2\2\2\u028e\u028d\3\2\2\2\u028f\u0290\3\2\2\2\u0290\u0291\5F"+
		"$\2\u0291K\3\2\2\2\u0292\u0295\5l\67\2\u0293\u0295\5n8\2\u0294\u0292\3"+
		"\2\2\2\u0294\u0293\3\2\2\2\u0295\u0297\3\2\2\2\u0296\u0298\5T+\2\u0297"+
		"\u0296\3\2\2\2\u0297\u0298\3\2\2\2\u0298M\3\2\2\2\u0299\u029b\5l\67\2"+
		"\u029a\u029c\5T+\2\u029b\u029a\3\2\2\2\u029b\u029c\3\2\2\2\u029cO\3\2"+
		"\2\2\u029d\u029f\5n8\2\u029e\u02a0\5T+\2\u029f\u029e\3\2\2\2\u029f\u02a0"+
		"\3\2\2\2\u02a0Q\3\2\2\2\u02a1\u02a2\7\17\2\2\u02a2S\3\2\2\2\u02a3\u02a4"+
		"\7[\2\2\u02a4\u02a5\5\u012a\u0096\2\u02a5\u02ac\5V,\2\u02a6\u02a7\7W\2"+
		"\2\u02a7\u02a8\5\u012a\u0096\2\u02a8\u02a9\5V,\2\u02a9\u02ab\3\2\2\2\u02aa"+
		"\u02a6\3\2\2\2\u02ab\u02ae\3\2\2\2\u02ac\u02aa\3\2\2\2\u02ac\u02ad\3\2"+
		"\2\2\u02ad\u02af\3\2\2\2\u02ae\u02ac\3\2\2\2\u02af\u02b0\5\u012a\u0096"+
		"\2\u02b0\u02b1\7Z\2\2\u02b1U\3\2\2\2\u02b2\u02bc\5J&\2\u02b3\u02b4\5\u008a"+
		"F\2\u02b4\u02b9\7^\2\2\u02b5\u02b6\t\5\2\2\u02b6\u02b7\5\u012a\u0096\2"+
		"\u02b7\u02b8\5J&\2\u02b8\u02ba\3\2\2\2\u02b9\u02b5\3\2\2\2\u02b9\u02ba"+
		"\3\2\2\2\u02ba\u02bc\3\2\2\2\u02bb\u02b2\3\2\2\2\u02bb\u02b3\3\2\2\2\u02bc"+
		"W\3\2\2\2\u02bd\u02be\5\u008aF\2\u02be\u02bf\5l\67\2\u02bfY\3\2\2\2\u02c0"+
		"\u02c7\5X-\2\u02c1\u02c2\7W\2\2\u02c2\u02c3\5\u012a\u0096\2\u02c3\u02c4"+
		"\5X-\2\u02c4\u02c6\3\2\2\2\u02c5\u02c1\3\2\2\2\u02c6\u02c9\3\2\2\2\u02c7"+
		"\u02c5\3\2\2\2\u02c7\u02c8\3\2\2\2\u02c8[\3\2\2\2\u02c9\u02c7\3\2\2\2"+
		"\u02ca\u02cc\7P\2\2\u02cb\u02cd\5^\60\2\u02cc\u02cb\3\2\2\2\u02cc\u02cd"+
		"\3\2\2\2\u02cd\u02ce\3\2\2\2\u02ce\u02cf\5\u0128\u0095\2\u02cf]\3\2\2"+
		"\2\u02d0\u02d3\5b\62\2\u02d1\u02d3\5`\61\2\u02d2\u02d0\3\2\2\2\u02d2\u02d1"+
		"\3\2\2\2\u02d3\u02da\3\2\2\2\u02d4\u02d5\7W\2\2\u02d5\u02d6\5\u012a\u0096"+
		"\2\u02d6\u02d7\5b\62\2\u02d7\u02d9\3\2\2\2\u02d8\u02d4\3\2\2\2\u02d9\u02dc"+
		"\3\2\2\2\u02da\u02d8\3\2\2\2\u02da\u02db\3\2\2\2\u02db_\3\2\2\2\u02dc"+
		"\u02da\3\2\2\2\u02dd\u02de\5J&\2\u02de\u02df\7\62\2\2\u02dfa\3\2\2\2\u02e0"+
		"\u02e2\5\32\16\2\u02e1\u02e3\5J&\2\u02e2\u02e1\3\2\2\2\u02e2\u02e3\3\2"+
		"\2\2\u02e3\u02e5\3\2\2\2\u02e4\u02e6\7\177\2\2\u02e5\u02e4\3\2\2\2\u02e5"+
		"\u02e6\3\2\2\2\u02e6\u02e7\3\2\2\2\u02e7\u02ed\5> \2\u02e8\u02e9\5\u012a"+
		"\u0096\2\u02e9\u02ea\7Y\2\2\u02ea\u02eb\5\u012a\u0096\2\u02eb\u02ec\5"+
		"\u00e8u\2\u02ec\u02ee\3\2\2\2\u02ed\u02e8\3\2\2\2\u02ed\u02ee\3\2\2\2"+
		"\u02eec\3\2\2\2\u02ef\u02f0\5\u009cO\2\u02f0e\3\2\2\2\u02f1\u02f6\5h\65"+
		"\2\u02f2\u02f3\7X\2\2\u02f3\u02f5\5h\65\2\u02f4\u02f2\3\2\2\2\u02f5\u02f8"+
		"\3\2\2\2\u02f6\u02f4\3\2\2\2\u02f6\u02f7\3\2\2\2\u02f7g\3\2\2\2\u02f8"+
		"\u02f6\3\2\2\2\u02f9\u02ff\5\u0122\u0092\2\u02fa\u02ff\7\n\2\2\u02fb\u02ff"+
		"\7\13\2\2\u02fc\u02ff\7\t\2\2\u02fd\u02ff\7\f\2\2\u02fe\u02f9\3\2\2\2"+
		"\u02fe\u02fa\3\2\2\2\u02fe\u02fb\3\2\2\2\u02fe\u02fc\3\2\2\2\u02fe\u02fd"+
		"\3\2\2\2\u02ffi\3\2\2\2\u0300\u0301\5h\65\2\u0301\u0302\7X\2\2\u0302\u0304"+
		"\3\2\2\2\u0303\u0300\3\2\2\2\u0304\u0307\3\2\2\2\u0305\u0303\3\2\2\2\u0305"+
		"\u0306\3\2\2\2\u0306k\3\2\2\2\u0307\u0305\3\2\2\2\u0308\u0309\5j\66\2"+
		"\u0309\u030a\5\u0122\u0092\2\u030am\3\2\2\2\u030b\u030c\5j\66\2\u030c"+
		"\u0311\5\u0120\u0091\2\u030d\u030e\7X\2\2\u030e\u0310\5\u0120\u0091\2"+
		"\u030f\u030d\3\2\2\2\u0310\u0313\3\2\2\2\u0311\u030f\3\2\2\2\u0311\u0312"+
		"\3\2\2\2\u0312o\3\2\2\2\u0313\u0311\3\2\2\2\u0314\u031a\7:\2\2\u0315\u031a"+
		"\7;\2\2\u0316\u031a\5\u011e\u0090\2\u0317\u031a\7<\2\2\u0318\u031a\7="+
		"\2\2\u0319\u0314\3\2\2\2\u0319\u0315\3\2\2\2\u0319\u0316\3\2\2\2\u0319"+
		"\u0317\3\2\2\2\u0319\u0318\3\2\2\2\u031aq\3\2\2\2\u031b\u031c\7\4\2\2"+
		"\u031c\u0321\5t;\2\u031d\u031e\7\6\2\2\u031e\u0320\5t;\2\u031f\u031d\3"+
		"\2\2\2\u0320\u0323\3\2\2\2\u0321\u031f\3\2\2\2\u0321\u0322\3\2\2\2\u0322"+
		"\u0324\3\2\2\2\u0323\u0321\3\2\2\2\u0324\u0325\7\5\2\2\u0325s\3\2\2\2"+
		"\u0326\u032e\5v<\2\u0327\u0329\7R\2\2\u0328\u032a\5\u00e4s\2\u0329\u0328"+
		"\3\2\2\2\u0329\u032a\3\2\2\2\u032a\u032b\3\2\2\2\u032b\u032e\7S\2\2\u032c"+
		"\u032e\5\u0082B\2\u032d\u0326\3\2\2\2\u032d\u0327\3\2\2\2\u032d\u032c"+
		"\3\2\2\2\u032eu\3\2\2\2\u032f\u0333\5\u0122\u0092\2\u0330\u0332\7\7\2"+
		"\2\u0331\u0330\3\2\2\2\u0332\u0335\3\2\2\2\u0333\u0331\3\2\2\2\u0333\u0334"+
		"\3\2\2\2\u0334w\3\2\2\2\u0335\u0333\3\2\2\2\u0336\u0337\5|?\2\u0337\u0338"+
		"\5\u012a\u0096\2\u0338\u0339\7M\2\2\u0339\u033a\5\u012a\u0096\2\u033a"+
		"\u033b\5\u0080A\2\u033by\3\2\2\2\u033c\u033d\5~@\2\u033d\u033e\5\u012a"+
		"\u0096\2\u033e\u033f\7M\2\2\u033f\u0340\5\u012a\u0096\2\u0340\u0341\5"+
		"\u0080A\2\u0341{\3\2\2\2\u0342\u0343\5\\/\2\u0343}\3\2\2\2\u0344\u0347"+
		"\5\\/\2\u0345\u0347\5> \2\u0346\u0344\3\2\2\2\u0346\u0345\3\2\2\2\u0347"+
		"\177\3\2\2\2\u0348\u034b\5\u009cO\2\u0349\u034b\5\u00e4s\2\u034a\u0348"+
		"\3\2\2\2\u034a\u0349\3\2\2\2\u034b\u0081\3\2\2\2\u034c\u034d\7R\2\2\u034d"+
		"\u0355\5\u012a\u0096\2\u034e\u0350\5^\60\2\u034f\u034e\3\2\2\2\u034f\u0350"+
		"\3\2\2\2\u0350\u0351\3\2\2\2\u0351\u0352\5\u012a\u0096\2\u0352\u0353\7"+
		"M\2\2\u0353\u0354\5\u012a\u0096\2\u0354\u0356\3\2\2\2\u0355\u034f\3\2"+
		"\2\2\u0355\u0356\3\2\2\2\u0356\u0357\3\2\2\2\u0357\u0358\5\u0086D\2\u0358"+
		"\u0359\7S\2\2\u0359\u0083\3\2\2\2\u035a\u035d\5\u0082B\2\u035b\u035d\5"+
		"x=\2\u035c\u035a\3\2\2\2\u035c\u035b\3\2\2\2\u035d\u0085\3\2\2\2\u035e"+
		"\u0360\5\u0088E\2\u035f\u035e\3\2\2\2\u035f\u0360\3\2\2\2\u0360\u0087"+
		"\3\2\2\2\u0361\u0367\5\u009eP\2\u0362\u0363\5\u012c\u0097\2\u0363\u0364"+
		"\5\u009eP\2\u0364\u0366\3\2\2\2\u0365\u0362\3\2\2\2\u0366\u0369\3\2\2"+
		"\2\u0367\u0365\3\2\2\2\u0367\u0368\3\2\2\2\u0368\u036b\3\2\2\2\u0369\u0367"+
		"\3\2\2\2\u036a\u036c\5\u012c\u0097\2\u036b\u036a\3\2\2\2\u036b\u036c\3"+
		"\2\2\2\u036c\u0089\3\2\2\2\u036d\u036e\5\u008cG\2\u036e\u036f\5\u012a"+
		"\u0096\2\u036f\u0371\3\2\2\2\u0370\u036d\3\2\2\2\u0371\u0374\3\2\2\2\u0372"+
		"\u0370\3\2\2\2\u0372\u0373\3\2\2\2\u0373\u008b\3\2\2\2\u0374\u0372\3\2"+
		"\2\2\u0375\u0376\7~\2\2\u0376\u037c\5\u0090I\2\u0377\u0379\7P\2\2\u0378"+
		"\u037a\5\u008eH\2\u0379\u0378\3\2\2\2\u0379\u037a\3\2\2\2\u037a\u037b"+
		"\3\2\2\2\u037b\u037d\5\u0128\u0095\2\u037c\u0377\3\2\2\2\u037c\u037d\3"+
		"\2\2\2\u037d\u008d\3\2\2\2\u037e\u0381\5\u0092J\2\u037f\u0381\5\u0098"+
		"M\2\u0380\u037e\3\2\2\2\u0380\u037f\3\2\2\2\u0381\u008f\3\2\2\2\u0382"+
		"\u0383\5l\67\2\u0383\u0091\3\2\2\2\u0384\u0389\5\u0094K\2\u0385\u0386"+
		"\7W\2\2\u0386\u0388\5\u0094K\2\u0387\u0385\3\2\2\2\u0388\u038b\3\2\2\2"+
		"\u0389\u0387\3\2\2\2\u0389\u038a\3\2\2\2\u038a\u0093\3\2\2\2\u038b\u0389"+
		"\3\2\2\2\u038c\u038d\5\u0096L\2\u038d\u038e\5\u012a\u0096\2\u038e\u038f"+
		"\7Y\2\2\u038f\u0390\5\u012a\u0096\2\u0390\u0391\5\u0098M\2\u0391\u0095"+
		"\3\2\2\2\u0392\u0395\5\u0122\u0092\2\u0393\u0395\5\u0126\u0094\2\u0394"+
		"\u0392\3\2\2\2\u0394\u0393\3\2\2\2\u0395\u0097\3\2\2\2\u0396\u039a\5\u009a"+
		"N\2\u0397\u039a\5\u008cG\2\u0398\u039a\5\u00e8u\2\u0399\u0396\3\2\2\2"+
		"\u0399\u0397\3\2\2\2\u0399\u0398\3\2\2\2\u039a\u0099\3\2\2\2\u039b\u03a4"+
		"\7T\2\2\u039c\u03a1\5\u0098M\2\u039d\u039e\7W\2\2\u039e\u03a0\5\u0098"+
		"M\2\u039f\u039d\3\2\2\2\u03a0\u03a3\3\2\2\2\u03a1\u039f\3\2\2\2\u03a1"+
		"\u03a2\3\2\2\2\u03a2\u03a5\3\2\2\2\u03a3\u03a1\3\2\2\2\u03a4\u039c\3\2"+
		"\2\2\u03a4\u03a5\3\2\2\2\u03a5\u03a7\3\2\2\2\u03a6\u03a8\7W\2\2\u03a7"+
		"\u03a6\3\2\2\2\u03a7\u03a8\3\2\2\2\u03a8\u03a9\3\2\2\2\u03a9\u03aa\7U"+
		"\2\2\u03aa\u009b\3\2\2\2\u03ab\u03b3\7R\2\2\u03ac\u03b4\5\u012a\u0096"+
		"\2\u03ad\u03af\5\u012c\u0097\2\u03ae\u03ad\3\2\2\2\u03af\u03b2\3\2\2\2"+
		"\u03b0\u03ae\3\2\2\2\u03b0\u03b1\3\2\2\2\u03b1\u03b4\3\2\2\2\u03b2\u03b0"+
		"\3\2\2\2\u03b3\u03ac\3\2\2\2\u03b3\u03b0\3\2\2\2\u03b4\u03b5\3\2\2\2\u03b5"+
		"\u03b6\5\u0086D\2\u03b6\u03b7\7S\2\2\u03b7\u009d\3\2\2\2\u03b8\u03bb\5"+
		"\u00a0Q\2\u03b9\u03bb\5\u00bc_\2\u03ba\u03b8\3\2\2\2\u03ba\u03b9\3\2\2"+
		"\2\u03bb\u009f\3\2\2\2\u03bc\u03bd\6Q\t\2\u03bd\u03be\5\u00a4S\2\u03be"+
		"\u00a1\3\2\2\2\u03bf\u03c0\6R\n\3\u03c0\u03c4\5\34\17\2\u03c1\u03c2\6"+
		"R\13\3\u03c2\u03c4\5\20\t\2\u03c3\u03bf\3\2\2\2\u03c3\u03c1\3\2\2\2\u03c4"+
		"\u00a3\3\2\2\2\u03c5\u03d0\5\u00a2R\2\u03c6\u03c8\5J&\2\u03c7\u03c6\3"+
		"\2\2\2\u03c7\u03c8\3\2\2\2\u03c8\u03c9\3\2\2\2\u03c9\u03d1\5:\36\2\u03ca"+
		"\u03cb\5\u00a6T\2\u03cb\u03cc\5\u012a\u0096\2\u03cc\u03cd\7Y\2\2\u03cd"+
		"\u03ce\5\u012a\u0096\2\u03ce\u03cf\5@!\2\u03cf\u03d1\3\2\2\2\u03d0\u03c7"+
		"\3\2\2\2\u03d0\u03ca\3\2\2\2\u03d1\u03da\3\2\2\2\u03d2\u03d3\5\u00a2R"+
		"\2\u03d3\u03d4\5J&\2\u03d4\u03d5\5:\36\2\u03d5\u03da\3\2\2\2\u03d6\u03d7"+
		"\5J&\2\u03d7\u03d8\5:\36\2\u03d8\u03da\3\2\2\2\u03d9\u03c5\3\2\2\2\u03d9"+
		"\u03d2\3\2\2\2\u03d9\u03d6\3\2\2\2\u03da\u00a5\3\2\2\2\u03db\u03dc\7P"+
		"\2\2\u03dc\u03e1\5\u00a8U\2\u03dd\u03de\7W\2\2\u03de\u03e0\5\u00a8U\2"+
		"\u03df\u03dd\3\2\2\2\u03e0\u03e3\3\2\2\2\u03e1\u03df\3\2\2\2\u03e1\u03e2"+
		"\3\2\2\2\u03e2\u03e4\3\2\2\2\u03e3\u03e1\3\2\2\2\u03e4\u03e5\5\u0128\u0095"+
		"\2\u03e5\u00a7\3\2\2\2\u03e6\u03e8\5J&\2\u03e7\u03e6\3\2\2\2\u03e7\u03e8"+
		"\3\2\2\2\u03e8\u03e9\3\2\2\2\u03e9\u03ea\5> \2\u03ea\u00a9\3\2\2\2\u03eb"+
		"\u03ec\7P\2\2\u03ec\u03ef\5> \2\u03ed\u03ee\7W\2\2\u03ee\u03f0\5> \2\u03ef"+
		"\u03ed\3\2\2\2\u03f0\u03f1\3\2\2\2\u03f1\u03ef\3\2\2\2\u03f1\u03f2\3\2"+
		"\2\2\u03f2\u03f3\3\2\2\2\u03f3\u03f4\5\u0128\u0095\2\u03f4\u00ab\3\2\2"+
		"\2\u03f5\u03f8\5\u00aeX\2\u03f6\u03f8\5\u00b0Y\2\u03f7\u03f5\3\2\2\2\u03f7"+
		"\u03f6\3\2\2\2\u03f8\u00ad\3\2\2\2\u03f9\u03fa\7 \2\2\u03fa\u03fb\5\u00dc"+
		"o\2\u03fb\u03fc\5\u012a\u0096\2\u03fc\u0405\5\u00bc_\2\u03fd\u0400\5\u012a"+
		"\u0096\2\u03fe\u0400\5\u012c\u0097\2\u03ff\u03fd\3\2\2\2\u03ff\u03fe\3"+
		"\2\2\2\u0400\u0401\3\2\2\2\u0401\u0402\7\32\2\2\u0402\u0403\5\u012a\u0096"+
		"\2\u0403\u0404\5\u00bc_\2\u0404\u0406\3\2\2\2\u0405\u03ff\3\2\2\2\u0405"+
		"\u0406\3\2\2\2\u0406\u00af\3\2\2\2\u0407\u0408\7\60\2\2\u0408\u0409\5"+
		"\u00dco\2\u0409\u040a\5\u012a\u0096\2\u040a\u040b\7R\2\2\u040b\u040f\5"+
		"\u012a\u0096\2\u040c\u040e\5\u00caf\2\u040d\u040c\3\2\2\2\u040e\u0411"+
		"\3\2\2\2\u040f\u040d\3\2\2\2\u040f\u0410\3\2\2\2\u0410\u0412\3\2\2\2\u0411"+
		"\u040f\3\2\2\2\u0412\u0413\5\u012a\u0096\2\u0413\u0414\7S\2\2\u0414\u00b1"+
		"\3\2\2\2\u0415\u0416\7\37\2\2\u0416\u0417\7P\2\2\u0417\u0418\5\u00ceh"+
		"\2\u0418\u0419\5\u0128\u0095\2\u0419\u041a\5\u012a\u0096\2\u041a\u041b"+
		"\5\u00bc_\2\u041b\u0429\3\2\2\2\u041c\u041d\79\2\2\u041d\u041e\5\u00dc"+
		"o\2\u041e\u041f\5\u012a\u0096\2\u041f\u0420\5\u00bc_\2\u0420\u0429\3\2"+
		"\2\2\u0421\u0422\7\31\2\2\u0422\u0423\5\u012a\u0096\2\u0423\u0424\5\u00bc"+
		"_\2\u0424\u0425\5\u012a\u0096\2\u0425\u0426\79\2\2\u0426\u0427\5\u00dc"+
		"o\2\u0427\u0429\3\2\2\2\u0428\u0415\3\2\2\2\u0428\u041c\3\2\2\2\u0428"+
		"\u0421\3\2\2\2\u0429\u00b3\3\2\2\2\u042a\u042c\7\27\2\2\u042b\u042d\5"+
		"\u0122\u0092\2\u042c\u042b\3\2\2\2\u042c\u042d\3\2\2\2\u042d\u00b5\3\2"+
		"\2\2\u042e\u0430\7\22\2\2\u042f\u0431\5\u0122\u0092\2\u0430\u042f\3\2"+
		"\2\2\u0430\u0431\3\2\2\2\u0431\u00b7\3\2\2\2\u0432\u0434\7\66\2\2\u0433"+
		"\u0435\5\u00c4c\2\u0434\u0433\3\2\2\2\u0434\u0435\3\2\2\2\u0435\u0436"+
		"\3\2\2\2\u0436\u0437\5\u012a\u0096\2\u0437\u043d\5\u009cO\2\u0438\u0439"+
		"\5\u012a\u0096\2\u0439\u043a\5\u00be`\2\u043a\u043c\3\2\2\2\u043b\u0438"+
		"\3\2\2\2\u043c\u043f\3\2\2\2\u043d\u043b\3\2\2\2\u043d\u043e\3\2\2\2\u043e"+
		"\u0443\3\2\2\2\u043f\u043d\3\2\2\2\u0440\u0441\5\u012a\u0096\2\u0441\u0442"+
		"\5\u00c2b\2\u0442\u0444\3\2\2\2\u0443\u0440\3\2\2\2\u0443\u0444\3\2\2"+
		"\2\u0444\u00b9\3\2\2\2\u0445\u0446\7\21\2\2\u0446\u044c\5\u00e8u\2\u0447"+
		"\u0448\5\u012a\u0096\2\u0448\u0449\t\6\2\2\u0449\u044a\5\u012a\u0096\2"+
		"\u044a\u044b\5\u00e8u\2\u044b\u044d\3\2\2\2\u044c\u0447\3\2\2\2\u044c"+
		"\u044d\3\2\2\2\u044d\u00bb\3\2\2\2\u044e\u046d\5\u009cO\2\u044f\u046d"+
		"\5\u00acW\2\u0450\u046d\5\u00b2Z\2\u0451\u046d\5\u00b8]\2\u0452\u0453"+
		"\7\61\2\2\u0453\u0454\5\u00dco\2\u0454\u0455\5\u012a\u0096\2\u0455\u0456"+
		"\5\u009cO\2\u0456\u046d\3\2\2\2\u0457\u0459\7,\2\2\u0458\u045a\5\u00e8"+
		"u\2\u0459\u0458\3\2\2\2\u0459\u045a\3\2\2\2\u045a\u046d\3\2\2\2\u045b"+
		"\u045c\7\63\2\2\u045c\u046d\5\u00e8u\2\u045d\u046d\5\u00b6\\\2\u045e\u046d"+
		"\5\u00b4[\2\u045f\u0460\5\u0122\u0092\2\u0460\u0461\7_\2\2\u0461\u0462"+
		"\5\u012a\u0096\2\u0462\u0463\5\u00bc_\2\u0463\u046d\3\2\2\2\u0464\u046d"+
		"\5\b\5\2\u0465\u046d\5\u00ba^\2\u0466\u046d\5\n\6\2\u0467\u046d\5\u00a0"+
		"Q\2\u0468\u0469\6_\f\2\u0469\u046d\5\62\32\2\u046a\u046d\5\u00e4s\2\u046b"+
		"\u046d\7V\2\2\u046c\u044e\3\2\2\2\u046c\u044f\3\2\2\2\u046c\u0450\3\2"+
		"\2\2\u046c\u0451\3\2\2\2\u046c\u0452\3\2\2\2\u046c\u0457\3\2\2\2\u046c"+
		"\u045b\3\2\2\2\u046c\u045d\3\2\2\2\u046c\u045e\3\2\2\2\u046c\u045f\3\2"+
		"\2\2\u046c\u0464\3\2\2\2\u046c\u0465\3\2\2\2\u046c\u0466\3\2\2\2\u046c"+
		"\u0467\3\2\2\2\u046c\u0468\3\2\2\2\u046c\u046a\3\2\2\2\u046c\u046b\3\2"+
		"\2\2\u046d\u00bd\3\2\2\2\u046e\u046f\7\24\2\2\u046f\u0470\7P\2\2\u0470"+
		"\u0472\5\32\16\2\u0471\u0473\5\u00c0a\2\u0472\u0471\3\2\2\2\u0472\u0473"+
		"\3\2\2\2\u0473\u0474\3\2\2\2\u0474\u0475\5\u0122\u0092\2\u0475\u0476\5"+
		"\u0128\u0095\2\u0476\u0477\5\u012a\u0096\2\u0477\u0478\5\u009cO\2\u0478"+
		"\u00bf\3\2\2\2\u0479\u047e\5l\67\2\u047a\u047b\7m\2\2\u047b\u047d\5l\67"+
		"\2\u047c\u047a\3\2\2\2\u047d\u0480\3\2\2\2\u047e\u047c\3\2\2\2\u047e\u047f"+
		"\3\2\2\2\u047f\u00c1\3\2\2\2\u0480\u047e\3\2\2\2\u0481\u0482\7\36\2\2"+
		"\u0482\u0483\5\u012a\u0096\2\u0483\u0484\5\u009cO\2\u0484\u00c3\3\2\2"+
		"\2\u0485\u0486\7P\2\2\u0486\u0487\5\u012a\u0096\2\u0487\u0489\5\u00c6"+
		"d\2\u0488\u048a\5\u012c\u0097\2\u0489\u0488\3\2\2\2\u0489\u048a\3\2\2"+
		"\2\u048a\u048b\3\2\2\2\u048b\u048c\5\u0128\u0095\2\u048c\u00c5\3\2\2\2"+
		"\u048d\u0493\5\u00c8e\2\u048e\u048f\5\u012c\u0097\2\u048f\u0490\5\u00c8"+
		"e\2\u0490\u0492\3\2\2\2\u0491\u048e\3\2\2\2\u0492\u0495\3\2\2\2\u0493"+
		"\u0491\3\2\2\2\u0493\u0494\3\2\2\2\u0494\u00c7\3\2\2\2\u0495\u0493\3\2"+
		"\2\2\u0496\u0499\5\u00a0Q\2\u0497\u0499\5\u00e8u\2\u0498\u0496\3\2\2\2"+
		"\u0498\u0497\3\2\2\2\u0499\u00c9\3\2\2\2\u049a\u049b\5\u00ccg\2\u049b"+
		"\u049c\5\u012a\u0096\2\u049c\u049e\3\2\2\2\u049d\u049a\3\2\2\2\u049e\u049f"+
		"\3\2\2\2\u049f\u049d\3\2\2\2\u049f\u04a0\3\2\2\2\u04a0\u04a1\3\2\2\2\u04a1"+
		"\u04a2\5\u0088E\2\u04a2\u00cb\3\2\2\2\u04a3\u04a4\7\23\2\2\u04a4\u04a5"+
		"\5\u00e8u\2\u04a5\u04a6\7_\2\2\u04a6\u04aa\3\2\2\2\u04a7\u04a8\7\30\2"+
		"\2\u04a8\u04aa\7_\2\2\u04a9\u04a3\3\2\2\2\u04a9\u04a7\3\2\2\2\u04aa\u00cd"+
		"\3\2\2\2\u04ab\u04ae\5\u00d0i\2\u04ac\u04ae\5\u00d2j\2\u04ad\u04ab\3\2"+
		"\2\2\u04ad\u04ac\3\2\2\2\u04ae\u00cf\3\2\2\2\u04af\u04b1\5\32\16\2\u04b0"+
		"\u04b2\5J&\2\u04b1\u04b0\3\2\2\2\u04b1\u04b2\3\2\2\2\u04b2\u04b3\3\2\2"+
		"\2\u04b3\u04b4\5> \2\u04b4\u04b5\t\7\2\2\u04b5\u04b6\5\u00e8u\2\u04b6"+
		"\u00d1\3\2\2\2\u04b7\u04b9\5\u00d4k\2\u04b8\u04b7\3\2\2\2\u04b8\u04b9"+
		"\3\2\2\2\u04b9\u04ba\3\2\2\2\u04ba\u04bc\7V\2\2\u04bb\u04bd\5\u00e8u\2"+
		"\u04bc\u04bb\3\2\2\2\u04bc\u04bd\3\2\2\2\u04bd\u04be\3\2\2\2\u04be\u04c0"+
		"\7V\2\2\u04bf\u04c1\5\u00d6l\2\u04c0\u04bf\3\2\2\2\u04c0\u04c1\3\2\2\2"+
		"\u04c1\u00d3\3\2\2\2\u04c2\u04c5\5\u00a0Q\2\u04c3\u04c5\5\u00dep\2\u04c4"+
		"\u04c2\3\2\2\2\u04c4\u04c3\3\2\2\2\u04c5\u00d5\3\2\2\2\u04c6\u04c7\5\u00de"+
		"p\2\u04c7\u00d7\3\2\2\2\u04c8\u04c9\7P\2\2\u04c9\u04ca\5J&\2\u04ca\u04cb"+
		"\5\u0128\u0095\2\u04cb\u00d9\3\2\2\2\u04cc\u04cd\5\u00dco\2\u04cd\u00db"+
		"\3\2\2\2\u04ce\u04cf\7P\2\2\u04cf\u04d0\5\u00e2r\2\u04d0\u04d1\5\u0128"+
		"\u0095\2\u04d1\u00dd\3\2\2\2\u04d2\u04d7\5\u00e0q\2\u04d3\u04d4\7W\2\2"+
		"\u04d4\u04d6\5\u00e0q\2\u04d5\u04d3\3\2\2\2\u04d6\u04d9\3\2\2\2\u04d7"+
		"\u04d5\3\2\2\2\u04d7\u04d8\3\2\2\2\u04d8\u00df\3\2\2\2\u04d9\u04d7\3\2"+
		"\2\2\u04da\u04db\7j\2\2\u04db\u04de\bq\1\2\u04dc\u04de\3\2\2\2\u04dd\u04da"+
		"\3\2\2\2\u04dd\u04dc\3\2\2\2\u04de\u04df\3\2\2\2\u04df\u04e0\5\u00e8u"+
		"\2\u04e0\u00e1\3\2\2\2\u04e1\u04e4\5\u00e4s\2\u04e2\u04e4\5z>\2\u04e3"+
		"\u04e1\3\2\2\2\u04e3\u04e2\3\2\2\2\u04e4\u00e3\3\2\2\2\u04e5\u04e6\5\u00ec"+
		"w\2\u04e6\u00e5\3\2\2\2\u04e7\u04e9\5\u00f0y\2\u04e8\u04ea\t\b\2\2\u04e9"+
		"\u04e8\3\2\2\2\u04e9\u04ea\3\2\2\2\u04ea\u00e7\3\2\2\2\u04eb\u04ec\bu"+
		"\1\2\u04ec\u04ed\5\u00d8m\2\u04ed\u04ee\5\u00eav\2\u04ee\u04fd\3\2\2\2"+
		"\u04ef\u04fd\5\u00e6t\2\u04f0\u04f1\t\t\2\2\u04f1\u04f2\5\u012a\u0096"+
		"\2\u04f2\u04f3\5\u00e8u\24\u04f3\u04fd\3\2\2\2\u04f4\u04f5\t\n\2\2\u04f5"+
		"\u04fd\5\u00e8u\22\u04f6\u04f7\5\u00aaV\2\u04f7\u04f8\5\u012a\u0096\2"+
		"\u04f8\u04f9\7Y\2\2\u04f9\u04fa\5\u012a\u0096\2\u04fa\u04fb\5\u00e4s\2"+
		"\u04fb\u04fd\3\2\2\2\u04fc\u04eb\3\2\2\2\u04fc\u04ef\3\2\2\2\u04fc\u04f0"+
		"\3\2\2\2\u04fc\u04f4\3\2\2\2\u04fc\u04f6\3\2\2\2\u04fd\u056c\3\2\2\2\u04fe"+
		"\u04ff\f\23\2\2\u04ff\u0500\7H\2\2\u0500\u0501\5\u012a\u0096\2\u0501\u0502"+
		"\5\u00e8u\24\u0502\u056b\3\2\2\2\u0503\u0504\f\21\2\2\u0504\u0505\5\u012a"+
		"\u0096\2\u0505\u0506\t\13\2\2\u0506\u0507\5\u012a\u0096\2\u0507\u0508"+
		"\5\u00e8u\22\u0508\u056b\3\2\2\2\u0509\u050a\f\20\2\2\u050a\u050b\t\f"+
		"\2\2\u050b\u050c\5\u012a\u0096\2\u050c\u050d\5\u00e8u\21\u050d\u056b\3"+
		"\2\2\2\u050e\u050f\f\17\2\2\u050f\u051a\5\u012a\u0096\2\u0510\u0511\7"+
		"[\2\2\u0511\u0518\7[\2\2\u0512\u0513\7Z\2\2\u0513\u0514\7Z\2\2\u0514\u0518"+
		"\7Z\2\2\u0515\u0516\7Z\2\2\u0516\u0518\7Z\2\2\u0517\u0510\3\2\2\2\u0517"+
		"\u0512\3\2\2\2\u0517\u0515\3\2\2\2\u0518\u051b\3\2\2\2\u0519\u051b\t\r"+
		"\2\2\u051a\u0517\3\2\2\2\u051a\u0519\3\2\2\2\u051b\u051c\3\2\2\2\u051c"+
		"\u051d\5\u012a\u0096\2\u051d\u051e\5\u00e8u\20\u051e\u056b\3\2\2\2\u051f"+
		"\u0520\f\r\2\2\u0520\u0521\5\u012a\u0096\2\u0521\u0522\t\16\2\2\u0522"+
		"\u0523\5\u012a\u0096\2\u0523\u0524\5\u00e8u\16\u0524\u056b\3\2\2\2\u0525"+
		"\u0526\f\f\2\2\u0526\u0527\5\u012a\u0096\2\u0527\u0528\t\17\2\2\u0528"+
		"\u0529\5\u012a\u0096\2\u0529\u052a\5\u00e8u\r\u052a\u056b\3\2\2\2\u052b"+
		"\u052c\f\13\2\2\u052c\u052d\5\u012a\u0096\2\u052d\u052e\t\20\2\2\u052e"+
		"\u052f\5\u012a\u0096\2\u052f\u0530\5\u00e8u\f\u0530\u056b\3\2\2\2\u0531"+
		"\u0532\f\n\2\2\u0532\u0533\5\u012a\u0096\2\u0533\u0534\7l\2\2\u0534\u0535"+
		"\5\u012a\u0096\2\u0535\u0536\5\u00e8u\13\u0536\u056b\3\2\2\2\u0537\u0538"+
		"\f\t\2\2\u0538\u0539\5\u012a\u0096\2\u0539\u053a\7n\2\2\u053a\u053b\5"+
		"\u012a\u0096\2\u053b\u053c\5\u00e8u\n\u053c\u056b\3\2\2\2\u053d\u053e"+
		"\f\b\2\2\u053e\u053f\5\u012a\u0096\2\u053f\u0540\7m\2\2\u0540\u0541\5"+
		"\u012a\u0096\2\u0541\u0542\5\u00e8u\t\u0542\u056b\3\2\2\2\u0543\u0544"+
		"\f\7\2\2\u0544\u0545\5\u012a\u0096\2\u0545\u0546\7d\2\2\u0546\u0547\5"+
		"\u012a\u0096\2\u0547\u0548\5\u00e8u\b\u0548\u056b\3\2\2\2\u0549\u054a"+
		"\f\6\2\2\u054a\u054b\5\u012a\u0096\2\u054b\u054c\7e\2\2\u054c\u054d\5"+
		"\u012a\u0096\2\u054d\u054e\5\u00e8u\7\u054e\u056b\3\2\2\2\u054f\u0550"+
		"\f\5\2\2\u0550\u055a\5\u012a\u0096\2\u0551\u0552\7^\2\2\u0552\u0553\5"+
		"\u012a\u0096\2\u0553\u0554\5\u00e8u\2\u0554\u0555\5\u012a\u0096\2\u0555"+
		"\u0556\7_\2\2\u0556\u0557\5\u012a\u0096\2\u0557\u055b\3\2\2\2\u0558\u0559"+
		"\7C\2\2\u0559\u055b\5\u012a\u0096\2\u055a\u0551\3\2\2\2\u055a\u0558\3"+
		"\2\2\2\u055b\u055c\3\2\2\2\u055c\u055d\5\u00e8u\5\u055d\u056b\3\2\2\2"+
		"\u055e\u055f\f\16\2\2\u055f\u0560\5\u012a\u0096\2\u0560\u0561\t\21\2\2"+
		"\u0561\u0562\5\u012a\u0096\2\u0562\u0563\5J&\2\u0563\u056b\3\2\2\2\u0564"+
		"\u0565\f\3\2\2\u0565\u0566\5\u012a\u0096\2\u0566\u0567\t\22\2\2\u0567"+
		"\u0568\5\u012a\u0096\2\u0568\u0569\5\u00e2r\2\u0569\u056b\3\2\2\2\u056a"+
		"\u04fe\3\2\2\2\u056a\u0503\3\2\2\2\u056a\u0509\3\2\2\2\u056a\u050e\3\2"+
		"\2\2\u056a\u051f\3\2\2\2\u056a\u0525\3\2\2\2\u056a\u052b\3\2\2\2\u056a"+
		"\u0531\3\2\2\2\u056a\u0537\3\2\2\2\u056a\u053d\3\2\2\2\u056a\u0543\3\2"+
		"\2\2\u056a\u0549\3\2\2\2\u056a\u054f\3\2\2\2\u056a\u055e\3\2\2\2\u056a"+
		"\u0564\3\2\2\2\u056b\u056e\3\2\2\2\u056c\u056a\3\2\2\2\u056c\u056d\3\2"+
		"\2\2\u056d\u00e9\3\2\2\2\u056e\u056c\3\2\2\2\u056f\u0570\5\u00d8m\2\u0570"+
		"\u0571\5\u00eav\2\u0571\u057a\3\2\2\2\u0572\u057a\5\u00e6t\2\u0573\u0574"+
		"\t\t\2\2\u0574\u0575\5\u012a\u0096\2\u0575\u0576\5\u00eav\2\u0576\u057a"+
		"\3\2\2\2\u0577\u0578\t\n\2\2\u0578\u057a\5\u00eav\2\u0579\u056f\3\2\2"+
		"\2\u0579\u0572\3\2\2\2\u0579\u0573\3\2\2\2\u0579\u0577\3\2\2\2\u057a\u00eb"+
		"\3\2\2\2\u057b\u057f\5\u00e8u\2\u057c\u057d\6w\34\3\u057d\u0580\5\u0116"+
		"\u008c\2\u057e\u0580\3\2\2\2\u057f\u057c\3\2\2\2\u057f\u057e\3\2\2\2\u0580"+
		"\u0584\3\2\2\2\u0581\u0583\5\u00eex\2\u0582\u0581\3\2\2\2\u0583\u0586"+
		"\3\2\2\2\u0584\u0582\3\2\2\2\u0584\u0585\3\2\2\2\u0585\u00ed\3\2\2\2\u0586"+
		"\u0584\3\2\2\2\u0587\u058e\5\u00fc\177\2\u0588\u058a\5\u00f2z\2\u0589"+
		"\u0588\3\2\2\2\u058a\u058b\3\2\2\2\u058b\u0589\3\2\2\2\u058b\u058c\3\2"+
		"\2\2\u058c\u058f\3\2\2\2\u058d\u058f\5\u0116\u008c\2\u058e\u0589\3\2\2"+
		"\2\u058e\u058d\3\2\2\2\u058e\u058f\3\2\2\2\u058f\u00ef\3\2\2\2\u0590\u0596"+
		"\5\u00fc\177\2\u0591\u0592\5\u00f2z\2\u0592\u0593\by\1\2\u0593\u0595\3"+
		"\2\2\2\u0594\u0591\3\2\2\2\u0595\u0598\3\2\2\2\u0596\u0594\3\2\2\2\u0596"+
		"\u0597\3\2\2\2\u0597\u00f1\3\2\2\2\u0598\u0596\3\2\2\2\u0599\u05a4\5\u012a"+
		"\u0096\2\u059a\u059b\t\23\2\2\u059b\u059e\5\u012a\u0096\2\u059c\u059f"+
		"\7~\2\2\u059d\u059f\5\u0110\u0089\2\u059e\u059c\3\2\2\2\u059e\u059d\3"+
		"\2\2\2\u059e\u059f\3\2\2\2\u059f\u05a5\3\2\2\2\u05a0\u05a1\7D\2\2\u05a1"+
		"\u05a5\5\u012a\u0096\2\u05a2\u05a3\7E\2\2\u05a3\u05a5\5\u012a\u0096\2"+
		"\u05a4\u059a\3\2\2\2\u05a4\u05a0\3\2\2\2\u05a4\u05a2\3\2\2\2\u05a5\u05a6"+
		"\3\2\2\2\u05a6\u05a7\5\u00f4{\2\u05a7\u05a8\bz\1\2\u05a8\u05be\3\2\2\2"+
		"\u05a9\u05aa\5\u012a\u0096\2\u05aa\u05ab\7X\2\2\u05ab\u05ac\5\u012a\u0096"+
		"\2\u05ac\u05ad\7\'\2\2\u05ad\u05ae\5\u0108\u0085\2\u05ae\u05af\bz\1\2"+
		"\u05af\u05be\3\2\2\2\u05b0\u05b1\5\u0114\u008b\2\u05b1\u05b2\bz\1\2\u05b2"+
		"\u05be\3\2\2\2\u05b3\u05b4\5\u012a\u0096\2\u05b4\u05b5\5\u0084C\2\u05b5"+
		"\u05b6\bz\1\2\u05b6\u05be\3\2\2\2\u05b7\u05b8\5\u00f8}\2\u05b8\u05b9\b"+
		"z\1\2\u05b9\u05be\3\2\2\2\u05ba\u05bb\5\u00fa~\2\u05bb\u05bc\bz\1\2\u05bc"+
		"\u05be\3\2\2\2\u05bd\u0599\3\2\2\2\u05bd\u05a9\3\2\2\2\u05bd\u05b0\3\2"+
		"\2\2\u05bd\u05b3\3\2\2\2\u05bd\u05b7\3\2\2\2\u05bd\u05ba\3\2\2\2\u05be"+
		"\u00f3\3\2\2\2\u05bf\u05c4\5\u0122\u0092\2\u05c0\u05c4\5\u011e\u0090\2"+
		"\u05c1\u05c4\5\u00f6|\2\u05c2\u05c4\5\u0126\u0094\2\u05c3\u05bf\3\2\2"+
		"\2\u05c3\u05c0\3\2\2\2\u05c3\u05c1\3\2\2\2\u05c3\u05c2\3\2\2\2\u05c4\u00f5"+
		"\3\2\2\2\u05c5\u05c8\5\u00dan\2\u05c6\u05c8\5r:\2\u05c7\u05c5\3\2\2\2"+
		"\u05c7\u05c6\3\2\2\2\u05c8\u00f7\3\2\2\2\u05c9\u05cb\7^\2\2\u05ca\u05c9"+
		"\3\2\2\2\u05ca\u05cb\3\2\2\2\u05cb\u05cc\3\2\2\2\u05cc\u05ce\7T\2\2\u05cd"+
		"\u05cf\5\u00dep\2\u05ce\u05cd\3\2\2\2\u05ce\u05cf\3\2\2\2\u05cf\u05d0"+
		"\3\2\2\2\u05d0\u05d1\7U\2\2\u05d1\u00f9\3\2\2\2\u05d2\u05d4\7^\2\2\u05d3"+
		"\u05d2\3\2\2\2\u05d3\u05d4\3\2\2\2\u05d4\u05d5\3\2\2\2\u05d5\u05d8\7T"+
		"\2\2\u05d6\u05d9\5\u0102\u0082\2\u05d7\u05d9\7_\2\2\u05d8\u05d6\3\2\2"+
		"\2\u05d8\u05d7\3\2\2\2\u05d9\u05da\3\2\2\2\u05da\u05db\7U\2\2\u05db\u00fb"+
		"\3\2\2\2\u05dc\u05de\5\u0122\u0092\2\u05dd\u05df\5T+\2\u05de\u05dd\3\2"+
		"\2\2\u05de\u05df\3\2\2\2\u05df\u05ee\3\2\2\2\u05e0\u05ee\5p9\2\u05e1\u05ee"+
		"\5r:\2\u05e2\u05e3\7\'\2\2\u05e3\u05e4\5\u012a\u0096\2\u05e4\u05e5\5\u0108"+
		"\u0085\2\u05e5\u05ee\3\2\2\2\u05e6\u05ee\7\62\2\2\u05e7\u05ee\7/\2\2\u05e8"+
		"\u05ee\5\u00dan\2\u05e9\u05ee\5\u0084C\2\u05ea\u05ee\5\u00fe\u0080\2\u05eb"+
		"\u05ee\5\u0100\u0081\2\u05ec\u05ee\5\u0124\u0093\2\u05ed\u05dc\3\2\2\2"+
		"\u05ed\u05e0\3\2\2\2\u05ed\u05e1\3\2\2\2\u05ed\u05e2\3\2\2\2\u05ed\u05e6"+
		"\3\2\2\2\u05ed\u05e7\3\2\2\2\u05ed\u05e8\3\2\2\2\u05ed\u05e9\3\2\2\2\u05ed"+
		"\u05ea\3\2\2\2\u05ed\u05eb\3\2\2\2\u05ed\u05ec\3\2\2\2\u05ee\u00fd\3\2"+
		"\2\2\u05ef\u05f1\7T\2\2\u05f0\u05f2\5\u00dep\2\u05f1\u05f0\3\2\2\2\u05f1"+
		"\u05f2\3\2\2\2\u05f2\u05f4\3\2\2\2\u05f3\u05f5\7W\2\2\u05f4\u05f3\3\2"+
		"\2\2\u05f4\u05f5\3\2\2\2\u05f5\u05f6\3\2\2\2\u05f6\u05f7\7U\2\2\u05f7"+
		"\u00ff\3\2\2\2\u05f8\u05fe\7T\2\2\u05f9\u05fb\5\u0102\u0082\2\u05fa\u05fc"+
		"\7W\2\2\u05fb\u05fa\3\2\2\2\u05fb\u05fc\3\2\2\2\u05fc\u05ff\3\2\2\2\u05fd"+
		"\u05ff\7_\2\2\u05fe\u05f9\3\2\2\2\u05fe\u05fd\3\2\2\2\u05ff\u0600\3\2"+
		"\2\2\u0600\u0601\7U\2\2\u0601\u0101\3\2\2\2\u0602\u0607\5\u0104\u0083"+
		"\2\u0603\u0604\7W\2\2\u0604\u0606\5\u0104\u0083\2\u0605\u0603\3\2\2\2"+
		"\u0606\u0609\3\2\2\2\u0607\u0605\3\2\2\2\u0607\u0608\3\2\2\2\u0608\u0103"+
		"\3\2\2\2\u0609\u0607\3\2\2\2\u060a\u060b\5\u0106\u0084\2\u060b\u060c\7"+
		"_\2\2\u060c\u060d\5\u012a\u0096\2\u060d\u060e\5\u00e8u\2\u060e\u0615\3"+
		"\2\2\2\u060f\u0610\7j\2\2\u0610\u0611\7_\2\2\u0611\u0612\5\u012a\u0096"+
		"\2\u0612\u0613\5\u00e8u\2\u0613\u0615\3\2\2\2\u0614\u060a\3\2\2\2\u0614"+
		"\u060f\3\2\2\2\u0615\u0105\3\2\2\2\u0616\u0619\5\u0126\u0094\2\u0617\u0619"+
		"\5\u00fc\177\2\u0618\u0616\3\2\2\2\u0618\u0617\3\2\2\2\u0619\u0107\3\2"+
		"\2\2\u061a\u0632\5\u010e\u0088\2\u061b\u061c\6\u0085\35\3\u061c\u061d"+
		"\5\u012a\u0096\2\u061d\u061f\5\u0114\u008b\2\u061e\u0620\5\u010c\u0087"+
		"\2\u061f\u061e\3\2\2\2\u061f\u0620\3\2\2\2\u0620\u0633\3\2\2\2\u0621\u0627"+
		"\6\u0085\36\3\u0622\u0623\5\u008aF\2\u0623\u0624\7T\2\2\u0624\u0625\5"+
		"\u00e8u\2\u0625\u0626\7U\2\2\u0626\u0628\3\2\2\2\u0627\u0622\3\2\2\2\u0628"+
		"\u0629\3\2\2\2\u0629\u0627\3\2\2\2\u0629\u062a\3\2\2\2\u062a\u062b\3\2"+
		"\2\2\u062b\u062c\5F$\2\u062c\u0633\3\2\2\2\u062d\u062e\6\u0085\37\3\u062e"+
		"\u062f\5D#\2\u062f\u0630\5\u012a\u0096\2\u0630\u0631\5\u010a\u0086\2\u0631"+
		"\u0633\3\2\2\2\u0632\u061b\3\2\2\2\u0632\u0621\3\2\2\2\u0632\u062d\3\2"+
		"\2\2\u0633\u0109\3\2\2\2\u0634\u0635\7R\2\2\u0635\u0637\5\u012a\u0096"+
		"\2\u0636\u0638\5B\"\2\u0637\u0636\3\2\2\2\u0637\u0638\3\2\2\2\u0638\u0639"+
		"\3\2\2\2\u0639\u063a\5\u012a\u0096\2\u063a\u063b\7S\2\2\u063b\u010b\3"+
		"\2\2\2\u063c\u063d\5(\25\2\u063d\u010d\3\2\2\2\u063e\u0644\5\u008aF\2"+
		"\u063f\u0645\5R*\2\u0640\u0642\5l\67\2\u0641\u0643\5\u0112\u008a\2\u0642"+
		"\u0641\3\2\2\2\u0642\u0643\3\2\2\2\u0643\u0645\3\2\2\2\u0644\u063f\3\2"+
		"\2\2\u0644\u0640\3\2\2\2\u0645\u010f\3\2\2\2\u0646\u0647\7[\2\2\u0647"+
		"\u0648\5\u012a\u0096\2\u0648\u0649\5$\23\2\u0649\u064a\5\u012a\u0096\2"+
		"\u064a\u064b\7Z\2\2\u064b\u0111\3\2\2\2\u064c\u064d\7[\2\2\u064d\u0650"+
		"\7Z\2\2\u064e\u0650\5T+\2\u064f\u064c\3\2\2\2\u064f\u064e\3\2\2\2\u0650"+
		"\u0113\3\2\2\2\u0651\u0653\7P\2\2\u0652\u0654\5\u0118\u008d\2\u0653\u0652"+
		"\3\2\2\2\u0653\u0654\3\2\2\2\u0654\u0656\3\2\2\2\u0655\u0657\7W\2\2\u0656"+
		"\u0655\3\2\2\2\u0656\u0657\3\2\2\2\u0657\u0658\3\2\2\2\u0658\u0659\5\u0128"+
		"\u0095\2\u0659\u0115\3\2\2\2\u065a\u0661\5\u011a\u008e\2\u065b\u065c\7"+
		"W\2\2\u065c\u065d\5\u012a\u0096\2\u065d\u065e\5\u011a\u008e\2\u065e\u0660"+
		"\3\2\2\2\u065f\u065b\3\2\2\2\u0660\u0663\3\2\2\2\u0661\u065f\3\2\2\2\u0661"+
		"\u0662\3\2\2\2\u0662\u0117\3\2\2\2\u0663\u0661\3\2\2\2\u0664\u066b\5\u011c"+
		"\u008f\2\u0665\u0666\7W\2\2\u0666\u0667\5\u012a\u0096\2\u0667\u0668\5"+
		"\u011c\u008f\2\u0668\u066a\3\2\2\2\u0669\u0665\3\2\2\2\u066a\u066d\3\2"+
		"\2\2\u066b\u0669\3\2\2\2\u066b\u066c\3\2\2\2\u066c\u0119\3\2\2\2\u066d"+
		"\u066b\3\2\2\2\u066e\u0671\5\u00e0q\2\u066f\u0671\5\u0104\u0083\2\u0670"+
		"\u066e\3\2\2\2\u0670\u066f\3\2\2\2\u0671\u011b\3\2\2\2\u0672\u0676\5\u00e0"+
		"q\2\u0673\u0676\5z>\2\u0674\u0676\5\u0104\u0083\2\u0675\u0672\3\2\2\2"+
		"\u0675\u0673\3\2\2\2\u0675\u0674\3\2\2\2\u0676\u011d\3\2\2\2\u0677\u0678"+
		"\7\3\2\2\u0678\u011f\3\2\2\2\u0679\u067a\7|\2\2\u067a\u0121\3\2\2\2\u067b"+
		"\u0684\7}\2\2\u067c\u0684\7|\2\2\u067d\u0684\7\16\2\2\u067e\u067f\6\u0092"+
		" \2\u067f\u0684\7-\2\2\u0680\u0684\7\13\2\2\u0681\u0684\7\f\2\2\u0682"+
		"\u0684\7\t\2\2\u0683\u067b\3\2\2\2\u0683\u067c\3\2\2\2\u0683\u067d\3\2"+
		"\2\2\u0683\u067e\3\2\2\2\u0683\u0680\3\2\2\2\u0683\u0681\3\2\2\2\u0683"+
		"\u0682\3\2\2\2\u0684\u0123\3\2\2\2\u0685\u0686\t\24\2\2\u0686\u0125\3"+
		"\2\2\2\u0687\u0688\t\25\2\2\u0688\u0127\3\2\2\2\u0689\u068c\7Q\2\2\u068a"+
		"\u068c\b\u0095\1\2\u068b\u0689\3\2\2\2\u068b\u068a\3\2\2\2\u068c\u0129"+
		"\3\2\2\2\u068d\u068f\7\u0081\2\2\u068e\u068d\3\2\2\2\u068f\u0692\3\2\2"+
		"\2\u0690\u068e\3\2\2\2\u0690\u0691\3\2\2\2\u0691\u012b\3\2\2\2\u0692\u0690"+
		"\3\2\2\2\u0693\u0695\t\26\2\2\u0694\u0693\3\2\2\2\u0695\u0696\3\2\2\2"+
		"\u0696\u0694\3\2\2\2\u0696\u0697\3\2\2\2\u0697\u012d\3\2\2\2\u00bc\u0130"+
		"\u0133\u0136\u0140\u0144\u014d\u0154\u015b\u015e\u0165\u0168\u016f\u0173"+
		"\u0177\u017a\u0181\u018c\u0197\u01a0\u01aa\u01b8\u01be\u01c7\u01cb\u01ce"+
		"\u01d6\u01d9\u01dc\u01e4\u01e7\u01ea\u01ed\u01f4\u01f8\u0204\u020a\u020f"+
		"\u0212\u0217\u021b\u0222\u022d\u0231\u0234\u023d\u0241\u0243\u0247\u024c"+
		"\u0257\u0260\u026f\u0274\u027c\u027f\u0284\u028b\u028e\u0294\u0297\u029b"+
		"\u029f\u02ac\u02b9\u02bb\u02c7\u02cc\u02d2\u02da\u02e2\u02e5\u02ed\u02f6"+
		"\u02fe\u0305\u0311\u0319\u0321\u0329\u032d\u0333\u0346\u034a\u034f\u0355"+
		"\u035c\u035f\u0367\u036b\u0372\u0379\u037c\u0380\u0389\u0394\u0399\u03a1"+
		"\u03a4\u03a7\u03b0\u03b3\u03ba\u03c3\u03c7\u03d0\u03d9\u03e1\u03e7\u03f1"+
		"\u03f7\u03ff\u0405\u040f\u0428\u042c\u0430\u0434\u043d\u0443\u044c\u0459"+
		"\u046c\u0472\u047e\u0489\u0493\u0498\u049f\u04a9\u04ad\u04b1\u04b8\u04bc"+
		"\u04c0\u04c4\u04d7\u04dd\u04e3\u04e9\u04fc\u0517\u051a\u055a\u056a\u056c"+
		"\u0579\u057f\u0584\u058b\u058e\u0596\u059e\u05a4\u05bd\u05c3\u05c7\u05ca"+
		"\u05ce\u05d3\u05d8\u05de\u05ed\u05f1\u05f4\u05fb\u05fe\u0607\u0614\u0618"+
		"\u061f\u0629\u0632\u0637\u0642\u0644\u064f\u0653\u0656\u0661\u066b\u0670"+
		"\u0675\u0683\u068b\u0690\u0696";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
	}
}