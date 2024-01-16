// Generated from GroovyParser.g4 by ANTLR 4.13.1.3
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
		ABSTRACT=14, ASSERT=15, BREAK=16, YIELD=17, CASE=18, CATCH=19, CLASS=20, 
		CONST=21, CONTINUE=22, DEFAULT=23, DO=24, ELSE=25, ENUM=26, EXTENDS=27, 
		FINAL=28, FINALLY=29, FOR=30, IF=31, GOTO=32, IMPLEMENTS=33, IMPORT=34, 
		INSTANCEOF=35, INTERFACE=36, NATIVE=37, NEW=38, NON_SEALED=39, PACKAGE=40, 
		PERMITS=41, PRIVATE=42, PROTECTED=43, PUBLIC=44, RECORD=45, RETURN=46, 
		SEALED=47, STATIC=48, STRICTFP=49, SUPER=50, SWITCH=51, SYNCHRONIZED=52, 
		THIS=53, THROW=54, THROWS=55, TRANSIENT=56, TRY=57, VOID=58, VOLATILE=59, 
		WHILE=60, IntegerLiteral=61, FloatingPointLiteral=62, BooleanLiteral=63, 
		NullLiteral=64, RANGE_INCLUSIVE=65, RANGE_EXCLUSIVE_LEFT=66, RANGE_EXCLUSIVE_RIGHT=67, 
		RANGE_EXCLUSIVE_FULL=68, SPREAD_DOT=69, SAFE_DOT=70, SAFE_INDEX=71, SAFE_CHAIN_DOT=72, 
		ELVIS=73, METHOD_POINTER=74, METHOD_REFERENCE=75, REGEX_FIND=76, REGEX_MATCH=77, 
		POWER=78, POWER_ASSIGN=79, SPACESHIP=80, IDENTICAL=81, IMPLIES=82, NOT_IDENTICAL=83, 
		ARROW=84, NOT_INSTANCEOF=85, NOT_IN=86, LPAREN=87, RPAREN=88, LBRACE=89, 
		RBRACE=90, LBRACK=91, RBRACK=92, SEMI=93, COMMA=94, DOT=95, ASSIGN=96, 
		GT=97, LT=98, NOT=99, BITNOT=100, QUESTION=101, COLON=102, EQUAL=103, 
		LE=104, GE=105, NOTEQUAL=106, AND=107, OR=108, INC=109, DEC=110, ADD=111, 
		SUB=112, MUL=113, DIV=114, BITAND=115, BITOR=116, XOR=117, MOD=118, ADD_ASSIGN=119, 
		SUB_ASSIGN=120, MUL_ASSIGN=121, DIV_ASSIGN=122, AND_ASSIGN=123, OR_ASSIGN=124, 
		XOR_ASSIGN=125, MOD_ASSIGN=126, LSHIFT_ASSIGN=127, RSHIFT_ASSIGN=128, 
		URSHIFT_ASSIGN=129, ELVIS_ASSIGN=130, CapitalizedIdentifier=131, Identifier=132, 
		AT=133, ELLIPSIS=134, WS=135, NL=136, SH_COMMENT=137, UNEXPECTED_CHAR=138;
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
		RULE_variableInitializer = 33, RULE_variableInitializers = 34, RULE_emptyDims = 35, 
		RULE_emptyDimsOpt = 36, RULE_standardType = 37, RULE_type = 38, RULE_classOrInterfaceType = 39, 
		RULE_generalClassOrInterfaceType = 40, RULE_standardClassOrInterfaceType = 41, 
		RULE_primitiveType = 42, RULE_typeArguments = 43, RULE_typeArgument = 44, 
		RULE_annotatedQualifiedClassName = 45, RULE_qualifiedClassNameList = 46, 
		RULE_formalParameters = 47, RULE_formalParameterList = 48, RULE_thisFormalParameter = 49, 
		RULE_formalParameter = 50, RULE_methodBody = 51, RULE_qualifiedName = 52, 
		RULE_qualifiedNameElement = 53, RULE_qualifiedNameElements = 54, RULE_qualifiedClassName = 55, 
		RULE_qualifiedStandardClassName = 56, RULE_literal = 57, RULE_gstring = 58, 
		RULE_gstringValue = 59, RULE_gstringPath = 60, RULE_lambdaExpression = 61, 
		RULE_standardLambdaExpression = 62, RULE_lambdaParameters = 63, RULE_standardLambdaParameters = 64, 
		RULE_lambdaBody = 65, RULE_closure = 66, RULE_closureOrLambdaExpression = 67, 
		RULE_blockStatementsOpt = 68, RULE_blockStatements = 69, RULE_annotationsOpt = 70, 
		RULE_annotation = 71, RULE_elementValues = 72, RULE_annotationName = 73, 
		RULE_elementValuePairs = 74, RULE_elementValuePair = 75, RULE_elementValuePairName = 76, 
		RULE_elementValue = 77, RULE_elementValueArrayInitializer = 78, RULE_block = 79, 
		RULE_blockStatement = 80, RULE_localVariableDeclaration = 81, RULE_variableDeclaration = 82, 
		RULE_typeNamePairs = 83, RULE_typeNamePair = 84, RULE_variableNames = 85, 
		RULE_conditionalStatement = 86, RULE_ifElseStatement = 87, RULE_switchStatement = 88, 
		RULE_loopStatement = 89, RULE_continueStatement = 90, RULE_breakStatement = 91, 
		RULE_yieldStatement = 92, RULE_tryCatchStatement = 93, RULE_assertStatement = 94, 
		RULE_statement = 95, RULE_catchClause = 96, RULE_catchType = 97, RULE_finallyBlock = 98, 
		RULE_resources = 99, RULE_resourceList = 100, RULE_resource = 101, RULE_switchBlockStatementGroup = 102, 
		RULE_switchLabel = 103, RULE_forControl = 104, RULE_enhancedForControl = 105, 
		RULE_classicalForControl = 106, RULE_forInit = 107, RULE_forUpdate = 108, 
		RULE_castParExpression = 109, RULE_parExpression = 110, RULE_expressionInPar = 111, 
		RULE_expressionList = 112, RULE_expressionListElement = 113, RULE_enhancedStatementExpression = 114, 
		RULE_statementExpression = 115, RULE_postfixExpression = 116, RULE_switchExpression = 117, 
		RULE_switchBlockStatementExpressionGroup = 118, RULE_switchExpressionLabel = 119, 
		RULE_expression = 120, RULE_castOperandExpression = 121, RULE_commandExpression = 122, 
		RULE_commandArgument = 123, RULE_pathExpression = 124, RULE_pathElement = 125, 
		RULE_namePart = 126, RULE_dynamicMemberName = 127, RULE_indexPropertyArgs = 128, 
		RULE_namedPropertyArgs = 129, RULE_primary = 130, RULE_namedPropertyArgPrimary = 131, 
		RULE_namedArgPrimary = 132, RULE_commandPrimary = 133, RULE_list = 134, 
		RULE_map = 135, RULE_mapEntryList = 136, RULE_namedPropertyArgList = 137, 
		RULE_mapEntry = 138, RULE_namedPropertyArg = 139, RULE_namedArg = 140, 
		RULE_mapEntryLabel = 141, RULE_namedPropertyArgLabel = 142, RULE_namedArgLabel = 143, 
		RULE_creator = 144, RULE_dim = 145, RULE_arrayInitializer = 146, RULE_anonymousInnerClassDeclaration = 147, 
		RULE_createdName = 148, RULE_nonWildcardTypeArguments = 149, RULE_typeArgumentsOrDiamond = 150, 
		RULE_arguments = 151, RULE_argumentList = 152, RULE_enhancedArgumentListInPar = 153, 
		RULE_firstArgumentListElement = 154, RULE_argumentListElement = 155, RULE_enhancedArgumentListElement = 156, 
		RULE_stringLiteral = 157, RULE_className = 158, RULE_identifier = 159, 
		RULE_builtInType = 160, RULE_keywords = 161, RULE_rparen = 162, RULE_nls = 163, 
		RULE_sep = 164;
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
			"continueStatement", "breakStatement", "yieldStatement", "tryCatchStatement", 
			"assertStatement", "statement", "catchClause", "catchType", "finallyBlock", 
			"resources", "resourceList", "resource", "switchBlockStatementGroup", 
			"switchLabel", "forControl", "enhancedForControl", "classicalForControl", 
			"forInit", "forUpdate", "castParExpression", "parExpression", "expressionInPar", 
			"expressionList", "expressionListElement", "enhancedStatementExpression", 
			"statementExpression", "postfixExpression", "switchExpression", "switchBlockStatementExpressionGroup", 
			"switchExpressionLabel", "expression", "castOperandExpression", "commandExpression", 
			"commandArgument", "pathExpression", "pathElement", "namePart", "dynamicMemberName", 
			"indexPropertyArgs", "namedPropertyArgs", "primary", "namedPropertyArgPrimary", 
			"namedArgPrimary", "commandPrimary", "list", "map", "mapEntryList", "namedPropertyArgList", 
			"mapEntry", "namedPropertyArg", "namedArg", "mapEntryLabel", "namedPropertyArgLabel", 
			"namedArgLabel", "creator", "dim", "arrayInitializer", "anonymousInnerClassDeclaration", 
			"createdName", "nonWildcardTypeArguments", "typeArgumentsOrDiamond", 
			"arguments", "argumentList", "enhancedArgumentListInPar", "firstArgumentListElement", 
			"argumentListElement", "enhancedArgumentListElement", "stringLiteral", 
			"className", "identifier", "builtInType", "keywords", "rparen", "nls", 
			"sep"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, "'as'", "'def'", "'in'", "'trait'", 
			"'threadsafe'", "'var'", null, "'abstract'", "'assert'", "'break'", "'yield'", 
			"'case'", "'catch'", "'class'", "'const'", "'continue'", "'default'", 
			"'do'", "'else'", "'enum'", "'extends'", "'final'", "'finally'", "'for'", 
			"'if'", "'goto'", "'implements'", "'import'", "'instanceof'", "'interface'", 
			"'native'", "'new'", "'non-sealed'", "'package'", "'permits'", "'private'", 
			"'protected'", "'public'", "'record'", "'return'", "'sealed'", "'static'", 
			"'strictfp'", "'super'", "'switch'", "'synchronized'", "'this'", "'throw'", 
			"'throws'", "'transient'", "'try'", "'void'", "'volatile'", "'while'", 
			null, null, null, "'null'", "'..'", "'<..'", "'..<'", "'<..<'", "'*.'", 
			"'?.'", null, "'??.'", "'?:'", "'.&'", "'::'", "'=~'", "'==~'", "'**'", 
			"'**='", "'<=>'", "'==='", "'==>'", "'!=='", "'->'", "'!instanceof'", 
			"'!in'", null, null, null, null, null, null, "';'", "','", null, "'='", 
			"'>'", "'<'", "'!'", "'~'", "'?'", "':'", "'=='", "'<='", "'>='", "'!='", 
			"'&&'", "'||'", "'++'", "'--'", "'+'", "'-'", "'*'", null, "'&'", "'|'", 
			"'^'", "'%'", "'+='", "'-='", "'*='", "'/='", "'&='", "'|='", "'^='", 
			"'%='", "'<<='", "'>>='", "'>>>='", "'?='", null, null, "'@'", "'...'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "StringLiteral", "GStringBegin", "GStringEnd", "GStringPart", "GStringPathPart", 
			"RollBackOne", "AS", "DEF", "IN", "TRAIT", "THREADSAFE", "VAR", "BuiltInPrimitiveType", 
			"ABSTRACT", "ASSERT", "BREAK", "YIELD", "CASE", "CATCH", "CLASS", "CONST", 
			"CONTINUE", "DEFAULT", "DO", "ELSE", "ENUM", "EXTENDS", "FINAL", "FINALLY", 
			"FOR", "IF", "GOTO", "IMPLEMENTS", "IMPORT", "INSTANCEOF", "INTERFACE", 
			"NATIVE", "NEW", "NON_SEALED", "PACKAGE", "PERMITS", "PRIVATE", "PROTECTED", 
			"PUBLIC", "RECORD", "RETURN", "SEALED", "STATIC", "STRICTFP", "SUPER", 
			"SWITCH", "SYNCHRONIZED", "THIS", "THROW", "THROWS", "TRANSIENT", "TRY", 
			"VOID", "VOLATILE", "WHILE", "IntegerLiteral", "FloatingPointLiteral", 
			"BooleanLiteral", "NullLiteral", "RANGE_INCLUSIVE", "RANGE_EXCLUSIVE_LEFT", 
			"RANGE_EXCLUSIVE_RIGHT", "RANGE_EXCLUSIVE_FULL", "SPREAD_DOT", "SAFE_DOT", 
			"SAFE_INDEX", "SAFE_CHAIN_DOT", "ELVIS", "METHOD_POINTER", "METHOD_REFERENCE", 
			"REGEX_FIND", "REGEX_MATCH", "POWER", "POWER_ASSIGN", "SPACESHIP", "IDENTICAL", 
			"IMPLIES", "NOT_IDENTICAL", "ARROW", "NOT_INSTANCEOF", "NOT_IN", "LPAREN", 
			"RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", "DOT", 
			"ASSIGN", "GT", "LT", "NOT", "BITNOT", "QUESTION", "COLON", "EQUAL", 
			"LE", "GE", "NOTEQUAL", "AND", "OR", "INC", "DEC", "ADD", "SUB", "MUL", 
			"DIV", "BITAND", "BITOR", "XOR", "MOD", "ADD_ASSIGN", "SUB_ASSIGN", "MUL_ASSIGN", 
			"DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", "XOR_ASSIGN", "MOD_ASSIGN", 
			"LSHIFT_ASSIGN", "RSHIFT_ASSIGN", "URSHIFT_ASSIGN", "ELVIS_ASSIGN", "CapitalizedIdentifier", 
			"Identifier", "AT", "ELLIPSIS", "WS", "NL", "SH_COMMENT", "UNEXPECTED_CHAR"
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
			setState(330);
			nls();
			setState(335);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(331);
				packageDeclaration();
				setState(333);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(332);
					sep();
					}
					break;
				}
				}
				break;
			}
			setState(338);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(337);
				scriptStatements();
				}
				break;
			}
			setState(340);
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
			setState(342);
			scriptStatement();
			setState(348);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(343);
					sep();
					setState(344);
					scriptStatement();
					}
					} 
				}
				setState(350);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(352);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(351);
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
			setState(359);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(354);
				importDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(355);
				typeDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(356);
				if (!( !SemanticPredicates.isInvalidMethodDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidMethodDeclaration(_input) ");
				setState(357);
				methodDeclaration(3, 9);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(358);
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
			setState(361);
			annotationsOpt();
			setState(362);
			match(PACKAGE);
			setState(363);
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
			setState(365);
			annotationsOpt();
			setState(366);
			match(IMPORT);
			setState(368);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STATIC) {
				{
				setState(367);
				match(STATIC);
				}
			}

			setState(370);
			qualifiedName();
			setState(375);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
				{
				setState(371);
				match(DOT);
				setState(372);
				match(MUL);
				}
				break;
			case AS:
				{
				setState(373);
				match(AS);
				setState(374);
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
			setState(377);
			classOrInterfaceModifiersOpt();
			setState(378);
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
			setState(382);
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
				setState(380);
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
				setState(381);
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
			setState(387);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(384);
				modifiers();
				setState(385);
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
			setState(389);
			modifier();
			setState(395);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(390);
					nls();
					setState(391);
					modifier();
					}
					} 
				}
				setState(397);
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
			setState(405);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(398);
				classOrInterfaceModifiers();
				setState(402);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NL) {
					{
					{
					setState(399);
					match(NL);
					}
					}
					setState(404);
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
			setState(407);
			classOrInterfaceModifier();
			setState(413);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(408);
					nls();
					setState(409);
					classOrInterfaceModifier();
					}
					} 
				}
				setState(415);
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
			setState(418);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(416);
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
				setState(417);
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
			setState(422);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(420);
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
				setState(421);
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
			setState(427);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(424);
				variableModifiers();
				setState(425);
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
			setState(429);
			variableModifier();
			setState(435);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(430);
					nls();
					setState(431);
					variableModifier();
					}
					} 
				}
				setState(437);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(438);
			match(LT);
			setState(439);
			nls();
			setState(440);
			typeParameter();
			setState(447);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(441);
				match(COMMA);
				setState(442);
				nls();
				setState(443);
				typeParameter();
				}
				}
				setState(449);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(450);
			nls();
			setState(451);
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
			setState(453);
			annotationsOpt();
			setState(454);
			className();
			setState(459);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(455);
				match(EXTENDS);
				setState(456);
				nls();
				setState(457);
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
			setState(461);
			type();
			setState(468);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITAND) {
				{
				{
				setState(462);
				match(BITAND);
				setState(463);
				nls();
				setState(464);
				type();
				}
				}
				setState(470);
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
			setState(471);
			type();
			setState(478);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(472);
				match(COMMA);
				setState(473);
				nls();
				setState(474);
				type();
				}
				}
				setState(480);
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
			setState(494);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CLASS:
				{
				setState(481);
				match(CLASS);
				 _localctx.t =  0; 
				}
				break;
			case INTERFACE:
				{
				setState(483);
				match(INTERFACE);
				 _localctx.t =  1; 
				}
				break;
			case ENUM:
				{
				setState(485);
				match(ENUM);
				 _localctx.t =  2; 
				}
				break;
			case AT:
				{
				setState(487);
				match(AT);
				setState(488);
				match(INTERFACE);
				 _localctx.t =  3; 
				}
				break;
			case TRAIT:
				{
				setState(490);
				match(TRAIT);
				 _localctx.t =  4; 
				}
				break;
			case RECORD:
				{
				setState(492);
				match(RECORD);
				 _localctx.t =  5; 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(496);
			identifier();
			setState(500);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(497);
				nls();
				setState(498);
				typeParameters();
				}
				break;
			}
			setState(505);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				{
				setState(502);
				nls();
				setState(503);
				formalParameters();
				}
				break;
			}
			setState(512);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				{
				setState(507);
				nls();
				setState(508);
				match(EXTENDS);
				setState(509);
				nls();
				setState(510);
				_localctx.scs = typeList();
				}
				break;
			}
			setState(519);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(514);
				nls();
				setState(515);
				match(IMPLEMENTS);
				setState(516);
				nls();
				setState(517);
				_localctx.is = typeList();
				}
				break;
			}
			setState(526);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				setState(521);
				nls();
				setState(522);
				match(PERMITS);
				setState(523);
				nls();
				setState(524);
				_localctx.ps = typeList();
				}
				break;
			}
			setState(528);
			nls();
			setState(529);
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
			setState(531);
			match(LBRACE);
			setState(532);
			nls();
			setState(544);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(533);
				if (!( 2 == _localctx.t )) throw createFailedPredicateException(" 2 == $t ");
				setState(534);
				enumConstants();
				setState(538);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
				case 1:
					{
					setState(535);
					nls();
					setState(536);
					match(COMMA);
					}
					break;
				}
				setState(541);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(540);
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
			setState(555);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << StringLiteral) | (1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << YIELD) | (1L << CLASS) | (1L << DEFAULT) | (1L << ENUM) | (1L << FINAL) | (1L << INTERFACE) | (1L << NATIVE) | (1L << NON_SEALED) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << SYNCHRONIZED) | (1L << TRANSIENT) | (1L << VOID) | (1L << VOLATILE))) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & ((1L << (LBRACE - 89)) | (1L << (LT - 89)) | (1L << (CapitalizedIdentifier - 89)) | (1L << (Identifier - 89)) | (1L << (AT - 89)))) != 0)) {
				{
				setState(546);
				classBodyDeclaration(_localctx.t);
				setState(552);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(547);
						sep();
						setState(548);
						classBodyDeclaration(_localctx.t);
						}
						} 
					}
					setState(554);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
				}
				}
			}

			setState(558);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(557);
				sep();
				}
			}

			setState(560);
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
			setState(562);
			enumConstant();
			setState(570);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(563);
					nls();
					setState(564);
					match(COMMA);
					setState(565);
					nls();
					setState(566);
					enumConstant();
					}
					} 
				}
				setState(572);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(573);
			annotationsOpt();
			setState(574);
			identifier();
			setState(576);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(575);
				arguments();
				}
			}

			setState(579);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(578);
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
			setState(587);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(583);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(581);
					match(STATIC);
					setState(582);
					nls();
					}
				}

				setState(585);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(586);
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
			setState(596);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(589);
				methodDeclaration(0, _localctx.t);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(590);
				fieldDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(591);
				modifiersOpt();
				setState(594);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
				case 1:
					{
					setState(592);
					classDeclaration();
					}
					break;
				case 2:
					{
					setState(593);
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
		public TerminalNode DEFAULT() { return getToken(GroovyParser.DEFAULT, 0); }
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
			setState(598);
			modifiersOpt();
			setState(600);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(599);
				typeParameters();
				}
			}

			setState(605);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				{
				setState(602);
				returnType(_localctx.ct);
				setState(603);
				nls();
				}
				break;
			}
			setState(607);
			methodName();
			setState(608);
			formalParameters();
			setState(625);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				{
				setState(609);
				match(DEFAULT);
				setState(610);
				nls();
				setState(611);
				elementValue();
				}
				break;
			case 2:
				{
				setState(618);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
				case 1:
					{
					setState(613);
					nls();
					setState(614);
					match(THROWS);
					setState(615);
					nls();
					setState(616);
					qualifiedClassNameList();
					}
					break;
				}
				setState(623);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(620);
					nls();
					setState(621);
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
			setState(627);
			methodName();
			setState(628);
			nls();
			setState(629);
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
			setState(633);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(631);
				identifier();
				}
				break;
			case StringLiteral:
				enterOuterAlt(_localctx, 2);
				{
				setState(632);
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
			setState(637);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case VAR:
			case BuiltInPrimitiveType:
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(635);
				standardType();
				}
				break;
			case VOID:
				enterOuterAlt(_localctx, 2);
				{
				setState(636);
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
			setState(639);
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
			setState(641);
			variableDeclarator();
			setState(648);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(642);
				match(COMMA);
				setState(643);
				nls();
				setState(644);
				variableDeclarator();
				}
				}
				setState(650);
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
			setState(651);
			variableDeclaratorId();
			setState(657);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
			case 1:
				{
				setState(652);
				nls();
				setState(653);
				match(ASSIGN);
				setState(654);
				nls();
				setState(655);
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
			setState(659);
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
			setState(661);
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
		enterRule(_localctx, 68, RULE_variableInitializers);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(663);
			variableInitializer();
			setState(671);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(664);
					nls();
					setState(665);
					match(COMMA);
					setState(666);
					nls();
					setState(667);
					variableInitializer();
					}
					} 
				}
				setState(673);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			}
			setState(674);
			nls();
			setState(676);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(675);
				match(COMMA);
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
		enterRule(_localctx, 70, RULE_emptyDims);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(682); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(678);
					annotationsOpt();
					setState(679);
					match(LBRACK);
					setState(680);
					match(RBRACK);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(684); 
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
		enterRule(_localctx, 72, RULE_emptyDimsOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(687);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				{
				setState(686);
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
		enterRule(_localctx, 74, RULE_standardType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(689);
			annotationsOpt();
			setState(692);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BuiltInPrimitiveType:
				{
				setState(690);
				primitiveType();
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case VAR:
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(691);
				standardClassOrInterfaceType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(694);
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
		enterRule(_localctx, 76, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(696);
			annotationsOpt();
			setState(702);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BuiltInPrimitiveType:
			case VOID:
				{
				setState(699);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case BuiltInPrimitiveType:
					{
					setState(697);
					primitiveType();
					}
					break;
				case VOID:
					{
					setState(698);
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
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(701);
				generalClassOrInterfaceType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(704);
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
		enterRule(_localctx, 78, RULE_classOrInterfaceType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(708);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(706);
				qualifiedClassName();
				}
				break;
			case 2:
				{
				setState(707);
				qualifiedStandardClassName();
				}
				break;
			}
			setState(711);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(710);
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
		enterRule(_localctx, 80, RULE_generalClassOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(713);
			qualifiedClassName();
			setState(715);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				{
				setState(714);
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
		enterRule(_localctx, 82, RULE_standardClassOrInterfaceType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(717);
			qualifiedStandardClassName();
			setState(719);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(718);
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
		enterRule(_localctx, 84, RULE_primitiveType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(721);
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
		enterRule(_localctx, 86, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(723);
			match(LT);
			setState(724);
			nls();
			setState(725);
			typeArgument();
			setState(732);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(726);
				match(COMMA);
				setState(727);
				nls();
				setState(728);
				typeArgument();
				}
				}
				setState(734);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(735);
			nls();
			setState(736);
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
		enterRule(_localctx, 88, RULE_typeArgument);
		int _la;
		try {
			setState(747);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(738);
				type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(739);
				annotationsOpt();
				setState(740);
				match(QUESTION);
				setState(745);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXTENDS || _la==SUPER) {
					{
					setState(741);
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
					setState(742);
					nls();
					setState(743);
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
		enterRule(_localctx, 90, RULE_annotatedQualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(749);
			annotationsOpt();
			setState(750);
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
		enterRule(_localctx, 92, RULE_qualifiedClassNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(752);
			annotatedQualifiedClassName();
			setState(759);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(753);
				match(COMMA);
				setState(754);
				nls();
				setState(755);
				annotatedQualifiedClassName();
				}
				}
				setState(761);
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
		enterRule(_localctx, 94, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(762);
			match(LPAREN);
			setState(764);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << YIELD) | (1L << FINAL) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << VOID))) != 0) || ((((_la - 131)) & ~0x3f) == 0 && ((1L << (_la - 131)) & ((1L << (CapitalizedIdentifier - 131)) | (1L << (Identifier - 131)) | (1L << (AT - 131)) | (1L << (ELLIPSIS - 131)))) != 0)) {
				{
				setState(763);
				formalParameterList();
				}
			}

			setState(766);
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
		enterRule(_localctx, 96, RULE_formalParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(770);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				{
				setState(768);
				formalParameter();
				}
				break;
			case 2:
				{
				setState(769);
				thisFormalParameter();
				}
				break;
			}
			setState(778);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(772);
				match(COMMA);
				setState(773);
				nls();
				setState(774);
				formalParameter();
				}
				}
				setState(780);
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
		enterRule(_localctx, 98, RULE_thisFormalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(781);
			type();
			setState(782);
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
		enterRule(_localctx, 100, RULE_formalParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(784);
			variableModifiersOpt();
			setState(786);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,68,_ctx) ) {
			case 1:
				{
				setState(785);
				type();
				}
				break;
			}
			setState(789);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELLIPSIS) {
				{
				setState(788);
				match(ELLIPSIS);
				}
			}

			setState(791);
			variableDeclaratorId();
			setState(797);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(792);
				nls();
				setState(793);
				match(ASSIGN);
				setState(794);
				nls();
				setState(795);
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
		enterRule(_localctx, 102, RULE_methodBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(799);
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
		enterRule(_localctx, 104, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(801);
			qualifiedNameElement();
			setState(806);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(802);
					match(DOT);
					setState(803);
					qualifiedNameElement();
					}
					} 
				}
				setState(808);
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
		enterRule(_localctx, 106, RULE_qualifiedNameElement);
		try {
			setState(814);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(809);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(810);
				match(DEF);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(811);
				match(IN);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(812);
				match(AS);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(813);
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
		enterRule(_localctx, 108, RULE_qualifiedNameElements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(821);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(816);
					qualifiedNameElement();
					setState(817);
					match(DOT);
					}
					} 
				}
				setState(823);
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
		enterRule(_localctx, 110, RULE_qualifiedClassName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(824);
			qualifiedNameElements();
			setState(825);
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
		enterRule(_localctx, 112, RULE_qualifiedStandardClassName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(827);
			qualifiedNameElements();
			setState(828);
			className();
			setState(833);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(829);
				match(DOT);
				setState(830);
				className();
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
		enterRule(_localctx, 114, RULE_literal);
		try {
			setState(841);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IntegerLiteral:
				_localctx = new IntegerLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(836);
				match(IntegerLiteral);
				}
				break;
			case FloatingPointLiteral:
				_localctx = new FloatingPointLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(837);
				match(FloatingPointLiteral);
				}
				break;
			case StringLiteral:
				_localctx = new StringLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(838);
				stringLiteral();
				}
				break;
			case BooleanLiteral:
				_localctx = new BooleanLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(839);
				match(BooleanLiteral);
				}
				break;
			case NullLiteral:
				_localctx = new NullLiteralAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(840);
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
		enterRule(_localctx, 116, RULE_gstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(843);
			match(GStringBegin);
			setState(844);
			gstringValue();
			setState(849);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPart) {
				{
				{
				setState(845);
				match(GStringPart);
				setState(846);
				gstringValue();
				}
				}
				setState(851);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(852);
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
		enterRule(_localctx, 118, RULE_gstringValue);
		try {
			setState(856);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(854);
				gstringPath();
				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 2);
				{
				setState(855);
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
		enterRule(_localctx, 120, RULE_gstringPath);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(858);
			identifier();
			setState(862);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==GStringPathPart) {
				{
				{
				setState(859);
				match(GStringPathPart);
				}
				}
				setState(864);
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
		enterRule(_localctx, 122, RULE_lambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(865);
			lambdaParameters();
			setState(866);
			nls();
			setState(867);
			match(ARROW);
			setState(868);
			nls();
			setState(869);
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
		enterRule(_localctx, 124, RULE_standardLambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(871);
			standardLambdaParameters();
			setState(872);
			nls();
			setState(873);
			match(ARROW);
			setState(874);
			nls();
			setState(875);
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
		enterRule(_localctx, 126, RULE_lambdaParameters);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(877);
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
		enterRule(_localctx, 128, RULE_standardLambdaParameters);
		try {
			setState(881);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(879);
				formalParameters();
				}
				break;
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
				enterOuterAlt(_localctx, 2);
				{
				setState(880);
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
		enterRule(_localctx, 130, RULE_lambdaBody);
		try {
			setState(885);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(883);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(884);
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
		enterRule(_localctx, 132, RULE_closure);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(887);
			match(LBRACE);
			setState(896);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				{
				setState(888);
				nls();
				setState(892);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << DEF) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << BuiltInPrimitiveType) | (1L << ABSTRACT) | (1L << YIELD) | (1L << FINAL) | (1L << PERMITS) | (1L << PRIVATE) | (1L << PROTECTED) | (1L << PUBLIC) | (1L << RECORD) | (1L << SEALED) | (1L << STATIC) | (1L << STRICTFP) | (1L << VOID))) != 0) || ((((_la - 131)) & ~0x3f) == 0 && ((1L << (_la - 131)) & ((1L << (CapitalizedIdentifier - 131)) | (1L << (Identifier - 131)) | (1L << (AT - 131)) | (1L << (ELLIPSIS - 131)))) != 0)) {
					{
					setState(889);
					formalParameterList();
					setState(890);
					nls();
					}
				}

				setState(894);
				match(ARROW);
				}
				break;
			}
			setState(899);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
			case 1:
				{
				setState(898);
				sep();
				}
				break;
			}
			setState(901);
			blockStatementsOpt();
			setState(902);
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
		enterRule(_localctx, 134, RULE_closureOrLambdaExpression);
		try {
			setState(906);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(904);
				closure();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(905);
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
		enterRule(_localctx, 136, RULE_blockStatementsOpt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(909);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
			case 1:
				{
				setState(908);
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
		enterRule(_localctx, 138, RULE_blockStatements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(911);
			blockStatement();
			setState(917);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,86,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(912);
					sep();
					setState(913);
					blockStatement();
					}
					} 
				}
				setState(919);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,86,_ctx);
			}
			setState(921);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
			case 1:
				{
				setState(920);
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
		enterRule(_localctx, 140, RULE_annotationsOpt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(934);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT) {
				{
				setState(923);
				annotation();
				setState(929);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(924);
						nls();
						setState(925);
						annotation();
						}
						} 
					}
					setState(931);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
				}
				setState(932);
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
		enterRule(_localctx, 142, RULE_annotation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(936);
			match(AT);
			setState(937);
			annotationName();
			setState(945);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				{
				setState(938);
				nls();
				setState(939);
				match(LPAREN);
				setState(941);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(940);
					elementValues();
					}
					break;
				}
				setState(943);
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
		enterRule(_localctx, 144, RULE_elementValues);
		try {
			setState(949);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(947);
				elementValuePairs();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(948);
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
		enterRule(_localctx, 146, RULE_annotationName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(951);
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
		enterRule(_localctx, 148, RULE_elementValuePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(953);
			elementValuePair();
			setState(958);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(954);
				match(COMMA);
				setState(955);
				elementValuePair();
				}
				}
				setState(960);
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
		enterRule(_localctx, 150, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(961);
			elementValuePairName();
			setState(962);
			nls();
			setState(963);
			match(ASSIGN);
			setState(964);
			nls();
			setState(965);
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
		enterRule(_localctx, 152, RULE_elementValuePairName);
		try {
			setState(969);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(967);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(968);
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
		enterRule(_localctx, 154, RULE_elementValue);
		try {
			setState(974);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(971);
				elementValueArrayInitializer();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(972);
				annotation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(973);
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
		enterRule(_localctx, 156, RULE_elementValueArrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(976);
			match(LBRACK);
			setState(988);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				{
				setState(977);
				elementValue();
				setState(982);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
				while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(978);
						match(COMMA);
						setState(979);
						elementValue();
						}
						} 
					}
					setState(984);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
				}
				setState(986);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(985);
					match(COMMA);
					}
				}

				}
				break;
			}
			setState(990);
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
		enterRule(_localctx, 158, RULE_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(992);
			match(LBRACE);
			setState(994);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				{
				setState(993);
				sep();
				}
				break;
			}
			setState(996);
			blockStatementsOpt();
			setState(997);
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
		enterRule(_localctx, 160, RULE_blockStatement);
		try {
			setState(1001);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(999);
				localVariableDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1000);
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
		enterRule(_localctx, 162, RULE_localVariableDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1003);
			if (!( !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) )) throw createFailedPredicateException(" !SemanticPredicates.isInvalidLocalVariableDeclaration(_input) ");
			setState(1004);
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
		enterRule(_localctx, 164, RULE_variableDeclaration);
		try {
			setState(1023);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,103,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1006);
				modifiers();
				setState(1007);
				nls();
				setState(1018);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case AS:
				case DEF:
				case IN:
				case TRAIT:
				case VAR:
				case BuiltInPrimitiveType:
				case YIELD:
				case PERMITS:
				case RECORD:
				case SEALED:
				case VOID:
				case CapitalizedIdentifier:
				case Identifier:
				case AT:
					{
					setState(1009);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
					case 1:
						{
						setState(1008);
						type();
						}
						break;
					}
					setState(1011);
					variableDeclarators();
					}
					break;
				case LPAREN:
					{
					setState(1012);
					typeNamePairs();
					setState(1013);
					nls();
					setState(1014);
					match(ASSIGN);
					setState(1015);
					nls();
					setState(1016);
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
				setState(1020);
				type();
				setState(1021);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1025);
			match(LPAREN);
			setState(1026);
			typeNamePair();
			setState(1031);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1027);
				match(COMMA);
				setState(1028);
				typeNamePair();
				}
				}
				setState(1033);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1034);
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
			setState(1037);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
			case 1:
				{
				setState(1036);
				type();
				}
				break;
			}
			setState(1039);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1041);
			match(LPAREN);
			setState(1042);
			variableDeclaratorId();
			setState(1045); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1043);
				match(COMMA);
				setState(1044);
				variableDeclaratorId();
				}
				}
				setState(1047); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==COMMA );
			setState(1049);
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
			setState(1053);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IF:
				enterOuterAlt(_localctx, 1);
				{
				setState(1051);
				ifElseStatement();
				}
				break;
			case SWITCH:
				enterOuterAlt(_localctx, 2);
				{
				setState(1052);
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
			setState(1055);
			match(IF);
			setState(1056);
			expressionInPar();
			setState(1057);
			nls();
			setState(1058);
			_localctx.tb = statement();
			setState(1067);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
			case 1:
				{
				setState(1061);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,108,_ctx) ) {
				case 1:
					{
					setState(1059);
					nls();
					}
					break;
				case 2:
					{
					setState(1060);
					sep();
					}
					break;
				}
				setState(1063);
				match(ELSE);
				setState(1064);
				nls();
				setState(1065);
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
			enterOuterAlt(_localctx, 1);
			{
			setState(1069);
			match(SWITCH);
			setState(1070);
			expressionInPar();
			setState(1071);
			nls();
			setState(1072);
			match(LBRACE);
			setState(1073);
			nls();
			setState(1081);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CASE || _la==DEFAULT) {
				{
				setState(1075); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1074);
					switchBlockStatementGroup();
					}
					}
					setState(1077); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==CASE || _la==DEFAULT );
				setState(1079);
				nls();
				}
			}

			setState(1083);
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
			setState(1104);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FOR:
				_localctx = new ForStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1085);
				match(FOR);
				setState(1086);
				match(LPAREN);
				setState(1087);
				forControl();
				setState(1088);
				rparen();
				setState(1089);
				nls();
				setState(1090);
				statement();
				}
				break;
			case WHILE:
				_localctx = new WhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1092);
				match(WHILE);
				setState(1093);
				expressionInPar();
				setState(1094);
				nls();
				setState(1095);
				statement();
				}
				break;
			case DO:
				_localctx = new DoWhileStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1097);
				match(DO);
				setState(1098);
				nls();
				setState(1099);
				statement();
				setState(1100);
				nls();
				setState(1101);
				match(WHILE);
				setState(1102);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1106);
			match(CONTINUE);
			setState(1108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << YIELD) | (1L << PERMITS) | (1L << RECORD) | (1L << SEALED))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(1107);
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
		enterRule(_localctx, 182, RULE_breakStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1110);
			match(BREAK);
			setState(1112);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << YIELD) | (1L << PERMITS) | (1L << RECORD) | (1L << SEALED))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) {
				{
				setState(1111);
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
		enterRule(_localctx, 184, RULE_yieldStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1114);
			match(YIELD);
			setState(1115);
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
		enterRule(_localctx, 186, RULE_tryCatchStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1117);
			match(TRY);
			setState(1119);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(1118);
				resources();
				}
			}

			setState(1121);
			nls();
			setState(1122);
			block();
			setState(1128);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,116,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1123);
					nls();
					setState(1124);
					catchClause();
					}
					} 
				}
				setState(1130);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,116,_ctx);
			}
			setState(1134);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
			case 1:
				{
				setState(1131);
				nls();
				setState(1132);
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
		enterRule(_localctx, 188, RULE_assertStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1136);
			match(ASSERT);
			setState(1137);
			_localctx.ce = expression(0);
			setState(1143);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
			case 1:
				{
				setState(1138);
				nls();
				setState(1139);
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
				setState(1140);
				nls();
				setState(1141);
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
		enterRule(_localctx, 190, RULE_statement);
		try {
			setState(1173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
			case 1:
				_localctx = new BlockStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1145);
				block();
				}
				break;
			case 2:
				_localctx = new ConditionalStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1146);
				conditionalStatement();
				}
				break;
			case 3:
				_localctx = new LoopStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1147);
				loopStatement();
				}
				break;
			case 4:
				_localctx = new TryCatchStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1148);
				tryCatchStatement();
				}
				break;
			case 5:
				_localctx = new SynchronizedStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1149);
				match(SYNCHRONIZED);
				setState(1150);
				expressionInPar();
				setState(1151);
				nls();
				setState(1152);
				block();
				}
				break;
			case 6:
				_localctx = new ReturnStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1154);
				match(RETURN);
				setState(1156);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,119,_ctx) ) {
				case 1:
					{
					setState(1155);
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
				setState(1158);
				match(THROW);
				setState(1159);
				expression(0);
				}
				break;
			case 8:
				_localctx = new BreakStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1160);
				breakStatement();
				}
				break;
			case 9:
				_localctx = new ContinueStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1161);
				continueStatement();
				}
				break;
			case 10:
				_localctx = new YieldStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1162);
				if (!( inSwitchExpressionLevel > 0 )) throw createFailedPredicateException(" inSwitchExpressionLevel > 0 ");
				setState(1163);
				yieldStatement();
				}
				break;
			case 11:
				_localctx = new LabeledStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1164);
				identifier();
				setState(1165);
				match(COLON);
				setState(1166);
				nls();
				setState(1167);
				statement();
				}
				break;
			case 12:
				_localctx = new AssertStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(1169);
				assertStatement();
				}
				break;
			case 13:
				_localctx = new LocalVariableDeclarationStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(1170);
				localVariableDeclaration();
				}
				break;
			case 14:
				_localctx = new ExpressionStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(1171);
				statementExpression();
				}
				break;
			case 15:
				_localctx = new EmptyStmtAltContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(1172);
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
		enterRule(_localctx, 192, RULE_catchClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1175);
			match(CATCH);
			setState(1176);
			match(LPAREN);
			setState(1177);
			variableModifiersOpt();
			setState(1179);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
			case 1:
				{
				setState(1178);
				catchType();
				}
				break;
			}
			setState(1181);
			identifier();
			setState(1182);
			rparen();
			setState(1183);
			nls();
			setState(1184);
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
		enterRule(_localctx, 194, RULE_catchType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1186);
			qualifiedClassName();
			setState(1191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITOR) {
				{
				{
				setState(1187);
				match(BITOR);
				setState(1188);
				qualifiedClassName();
				}
				}
				setState(1193);
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
		enterRule(_localctx, 196, RULE_finallyBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1194);
			match(FINALLY);
			setState(1195);
			nls();
			setState(1196);
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
		enterRule(_localctx, 198, RULE_resources);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1198);
			match(LPAREN);
			setState(1199);
			nls();
			setState(1200);
			resourceList();
			setState(1202);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI || _la==NL) {
				{
				setState(1201);
				sep();
				}
			}

			setState(1204);
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
		enterRule(_localctx, 200, RULE_resourceList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1206);
			resource();
			setState(1212);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,124,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1207);
					sep();
					setState(1208);
					resource();
					}
					} 
				}
				setState(1214);
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
		enterRule(_localctx, 202, RULE_resource);
		try {
			setState(1217);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1215);
				localVariableDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1216);
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
		enterRule(_localctx, 204, RULE_switchBlockStatementGroup);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1219);
			switchLabel();
			setState(1225);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,126,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1220);
					nls();
					setState(1221);
					switchLabel();
					}
					} 
				}
				setState(1227);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,126,_ctx);
			}
			setState(1228);
			nls();
			setState(1229);
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
		enterRule(_localctx, 206, RULE_switchLabel);
		try {
			setState(1237);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1231);
				match(CASE);
				setState(1232);
				expression(0);
				setState(1233);
				match(COLON);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1235);
				match(DEFAULT);
				setState(1236);
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
		enterRule(_localctx, 208, RULE_forControl);
		try {
			setState(1241);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1239);
				enhancedForControl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1240);
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
		enterRule(_localctx, 210, RULE_enhancedForControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1243);
			variableModifiersOpt();
			setState(1245);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,129,_ctx) ) {
			case 1:
				{
				setState(1244);
				type();
				}
				break;
			}
			setState(1247);
			variableDeclaratorId();
			setState(1248);
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
			setState(1249);
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
		enterRule(_localctx, 212, RULE_classicalForControl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1252);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
			case 1:
				{
				setState(1251);
				forInit();
				}
				break;
			}
			setState(1254);
			match(SEMI);
			setState(1256);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
			case 1:
				{
				setState(1255);
				expression(0);
				}
				break;
			}
			setState(1258);
			match(SEMI);
			setState(1260);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				{
				setState(1259);
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
		enterRule(_localctx, 214, RULE_forInit);
		try {
			setState(1264);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,133,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1262);
				localVariableDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1263);
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
		enterRule(_localctx, 216, RULE_forUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1266);
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
		enterRule(_localctx, 218, RULE_castParExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1268);
			match(LPAREN);
			setState(1269);
			type();
			setState(1270);
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
		enterRule(_localctx, 220, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1272);
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
		enterRule(_localctx, 222, RULE_expressionInPar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1274);
			match(LPAREN);
			setState(1275);
			enhancedStatementExpression();
			setState(1276);
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
		enterRule(_localctx, 224, RULE_expressionList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1278);
			expressionListElement(_localctx.canSpread);
			setState(1285);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1279);
					match(COMMA);
					setState(1280);
					nls();
					setState(1281);
					expressionListElement(_localctx.canSpread);
					}
					} 
				}
				setState(1287);
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
		enterRule(_localctx, 226, RULE_expressionListElement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1289);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,135,_ctx) ) {
			case 1:
				{
				setState(1288);
				match(MUL);
				}
				break;
			}
			setState(1291);
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
		enterRule(_localctx, 228, RULE_enhancedStatementExpression);
		try {
			setState(1295);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,136,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1293);
				statementExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1294);
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
		enterRule(_localctx, 230, RULE_statementExpression);
		try {
			_localctx = new CommandExprAltContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1297);
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
		enterRule(_localctx, 232, RULE_postfixExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1299);
			pathExpression();
			setState(1301);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
			case 1:
				{
				setState(1300);
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
		enterRule(_localctx, 234, RULE_switchExpression);

		    inSwitchExpressionLevel++;

		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1303);
			match(SWITCH);
			setState(1304);
			expressionInPar();
			setState(1305);
			nls();
			setState(1306);
			match(LBRACE);
			setState(1307);
			nls();
			setState(1311);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CASE || _la==DEFAULT) {
				{
				{
				setState(1308);
				switchBlockStatementExpressionGroup();
				}
				}
				setState(1313);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1314);
			nls();
			setState(1315);
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
		enterRule(_localctx, 236, RULE_switchBlockStatementExpressionGroup);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1320); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1317);
					switchExpressionLabel();
					setState(1318);
					nls();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1322); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,139,_ctx);
			} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(1324);
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
		enterRule(_localctx, 238, RULE_switchExpressionLabel);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1329);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				{
				setState(1326);
				match(CASE);
				setState(1327);
				expressionList(true);
				}
				break;
			case DEFAULT:
				{
				setState(1328);
				match(DEFAULT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1331);
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
		int _startState = 240;
		enterRecursionRule(_localctx, 240, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1351);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
			case 1:
				{
				_localctx = new CastExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1334);
				castParExpression();
				setState(1335);
				castOperandExpression();
				}
				break;
			case 2:
				{
				_localctx = new PostfixExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1337);
				postfixExpression();
				}
				break;
			case 3:
				{
				_localctx = new SwitchExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1338);
				switchExpression();
				}
				break;
			case 4:
				{
				_localctx = new UnaryNotExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1339);
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
				setState(1340);
				nls();
				setState(1341);
				expression(19);
				}
				break;
			case 5:
				{
				_localctx = new UnaryAddExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1343);
				((UnaryAddExprAltContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 109)) & ~0x3f) == 0 && ((1L << (_la - 109)) & ((1L << (INC - 109)) | (1L << (DEC - 109)) | (1L << (ADD - 109)) | (1L << (SUB - 109)))) != 0)) ) {
					((UnaryAddExprAltContext)_localctx).op = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1344);
				expression(17);
				}
				break;
			case 6:
				{
				_localctx = new MultipleAssignmentExprAltContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1345);
				((MultipleAssignmentExprAltContext)_localctx).left = variableNames();
				setState(1346);
				nls();
				setState(1347);
				((MultipleAssignmentExprAltContext)_localctx).op = match(ASSIGN);
				setState(1348);
				nls();
				setState(1349);
				((MultipleAssignmentExprAltContext)_localctx).right = statementExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1469);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1467);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
					case 1:
						{
						_localctx = new PowerExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((PowerExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1353);
						if (!(precpred(_ctx, 18))) throw createFailedPredicateException("precpred(_ctx, 18)");
						setState(1354);
						((PowerExprAltContext)_localctx).op = match(POWER);
						setState(1355);
						nls();
						setState(1356);
						((PowerExprAltContext)_localctx).right = expression(19);
						}
						break;
					case 2:
						{
						_localctx = new MultiplicativeExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((MultiplicativeExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1358);
						if (!(precpred(_ctx, 16))) throw createFailedPredicateException("precpred(_ctx, 16)");
						setState(1359);
						nls();
						setState(1360);
						((MultiplicativeExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 113)) & ~0x3f) == 0 && ((1L << (_la - 113)) & ((1L << (MUL - 113)) | (1L << (DIV - 113)) | (1L << (MOD - 113)))) != 0)) ) {
							((MultiplicativeExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1361);
						nls();
						setState(1362);
						((MultiplicativeExprAltContext)_localctx).right = expression(17);
						}
						break;
					case 3:
						{
						_localctx = new AdditiveExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AdditiveExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1364);
						if (!(precpred(_ctx, 15))) throw createFailedPredicateException("precpred(_ctx, 15)");
						setState(1365);
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
						setState(1366);
						nls();
						setState(1367);
						((AdditiveExprAltContext)_localctx).right = expression(16);
						}
						break;
					case 4:
						{
						_localctx = new ShiftExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ShiftExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1369);
						if (!(precpred(_ctx, 14))) throw createFailedPredicateException("precpred(_ctx, 14)");
						setState(1370);
						nls();
						setState(1381);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case GT:
						case LT:
							{
							setState(1378);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,142,_ctx) ) {
							case 1:
								{
								setState(1371);
								((ShiftExprAltContext)_localctx).dlOp = match(LT);
								setState(1372);
								match(LT);
								}
								break;
							case 2:
								{
								setState(1373);
								((ShiftExprAltContext)_localctx).tgOp = match(GT);
								setState(1374);
								match(GT);
								setState(1375);
								match(GT);
								}
								break;
							case 3:
								{
								setState(1376);
								((ShiftExprAltContext)_localctx).dgOp = match(GT);
								setState(1377);
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
							setState(1380);
							((ShiftExprAltContext)_localctx).rangeOp = _input.LT(1);
							_la = _input.LA(1);
							if ( !(((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & ((1L << (RANGE_INCLUSIVE - 65)) | (1L << (RANGE_EXCLUSIVE_LEFT - 65)) | (1L << (RANGE_EXCLUSIVE_RIGHT - 65)) | (1L << (RANGE_EXCLUSIVE_FULL - 65)))) != 0)) ) {
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
						setState(1383);
						nls();
						setState(1384);
						((ShiftExprAltContext)_localctx).right = expression(15);
						}
						break;
					case 5:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1386);
						if (!(precpred(_ctx, 12))) throw createFailedPredicateException("precpred(_ctx, 12)");
						setState(1387);
						nls();
						setState(1388);
						((RelationalExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==IN || ((((_la - 86)) & ~0x3f) == 0 && ((1L << (_la - 86)) & ((1L << (NOT_IN - 86)) | (1L << (GT - 86)) | (1L << (LT - 86)) | (1L << (LE - 86)) | (1L << (GE - 86)))) != 0)) ) {
							((RelationalExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1389);
						nls();
						setState(1390);
						((RelationalExprAltContext)_localctx).right = expression(13);
						}
						break;
					case 6:
						{
						_localctx = new EqualityExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((EqualityExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1392);
						if (!(precpred(_ctx, 11))) throw createFailedPredicateException("precpred(_ctx, 11)");
						setState(1393);
						nls();
						setState(1394);
						((EqualityExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 80)) & ~0x3f) == 0 && ((1L << (_la - 80)) & ((1L << (SPACESHIP - 80)) | (1L << (IDENTICAL - 80)) | (1L << (NOT_IDENTICAL - 80)) | (1L << (EQUAL - 80)) | (1L << (NOTEQUAL - 80)))) != 0)) ) {
							((EqualityExprAltContext)_localctx).op = _errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1395);
						nls();
						setState(1396);
						((EqualityExprAltContext)_localctx).right = expression(12);
						}
						break;
					case 7:
						{
						_localctx = new RegexExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RegexExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1398);
						if (!(precpred(_ctx, 10))) throw createFailedPredicateException("precpred(_ctx, 10)");
						setState(1399);
						nls();
						setState(1400);
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
						setState(1401);
						nls();
						setState(1402);
						((RegexExprAltContext)_localctx).right = expression(11);
						}
						break;
					case 8:
						{
						_localctx = new AndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1404);
						if (!(precpred(_ctx, 9))) throw createFailedPredicateException("precpred(_ctx, 9)");
						setState(1405);
						nls();
						setState(1406);
						((AndExprAltContext)_localctx).op = match(BITAND);
						setState(1407);
						nls();
						setState(1408);
						((AndExprAltContext)_localctx).right = expression(10);
						}
						break;
					case 9:
						{
						_localctx = new ExclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ExclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1410);
						if (!(precpred(_ctx, 8))) throw createFailedPredicateException("precpred(_ctx, 8)");
						setState(1411);
						nls();
						setState(1412);
						((ExclusiveOrExprAltContext)_localctx).op = match(XOR);
						setState(1413);
						nls();
						setState(1414);
						((ExclusiveOrExprAltContext)_localctx).right = expression(9);
						}
						break;
					case 10:
						{
						_localctx = new InclusiveOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((InclusiveOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1416);
						if (!(precpred(_ctx, 7))) throw createFailedPredicateException("precpred(_ctx, 7)");
						setState(1417);
						nls();
						setState(1418);
						((InclusiveOrExprAltContext)_localctx).op = match(BITOR);
						setState(1419);
						nls();
						setState(1420);
						((InclusiveOrExprAltContext)_localctx).right = expression(8);
						}
						break;
					case 11:
						{
						_localctx = new LogicalAndExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalAndExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1422);
						if (!(precpred(_ctx, 6))) throw createFailedPredicateException("precpred(_ctx, 6)");
						setState(1423);
						nls();
						setState(1424);
						((LogicalAndExprAltContext)_localctx).op = match(AND);
						setState(1425);
						nls();
						setState(1426);
						((LogicalAndExprAltContext)_localctx).right = expression(7);
						}
						break;
					case 12:
						{
						_localctx = new LogicalOrExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((LogicalOrExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1428);
						if (!(precpred(_ctx, 5))) throw createFailedPredicateException("precpred(_ctx, 5)");
						setState(1429);
						nls();
						setState(1430);
						((LogicalOrExprAltContext)_localctx).op = match(OR);
						setState(1431);
						nls();
						setState(1432);
						((LogicalOrExprAltContext)_localctx).right = expression(6);
						}
						break;
					case 13:
						{
						_localctx = new ImplicationExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ImplicationExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1434);
						if (!(precpred(_ctx, 4))) throw createFailedPredicateException("precpred(_ctx, 4)");
						setState(1435);
						nls();
						setState(1436);
						((ImplicationExprAltContext)_localctx).op = match(IMPLIES);
						setState(1437);
						nls();
						setState(1438);
						((ImplicationExprAltContext)_localctx).right = expression(4);
						}
						break;
					case 14:
						{
						_localctx = new ConditionalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((ConditionalExprAltContext)_localctx).con = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1440);
						if (!(precpred(_ctx, 3))) throw createFailedPredicateException("precpred(_ctx, 3)");
						setState(1441);
						nls();
						setState(1451);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case QUESTION:
							{
							setState(1442);
							match(QUESTION);
							setState(1443);
							nls();
							setState(1444);
							((ConditionalExprAltContext)_localctx).tb = expression(0);
							setState(1445);
							nls();
							setState(1446);
							match(COLON);
							setState(1447);
							nls();
							}
							break;
						case ELVIS:
							{
							setState(1449);
							match(ELVIS);
							setState(1450);
							nls();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1453);
						((ConditionalExprAltContext)_localctx).fb = expression(3);
						}
						break;
					case 15:
						{
						_localctx = new RelationalExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((RelationalExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1455);
						if (!(precpred(_ctx, 13))) throw createFailedPredicateException("precpred(_ctx, 13)");
						setState(1456);
						nls();
						setState(1457);
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
						setState(1458);
						nls();
						setState(1459);
						type();
						}
						break;
					case 16:
						{
						_localctx = new AssignmentExprAltContext(new ExpressionContext(_parentctx, _parentState));
						((AssignmentExprAltContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1461);
						if (!(precpred(_ctx, 1))) throw createFailedPredicateException("precpred(_ctx, 1)");
						setState(1462);
						nls();
						setState(1463);
						((AssignmentExprAltContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & ((1L << (POWER_ASSIGN - 79)) | (1L << (ASSIGN - 79)) | (1L << (ADD_ASSIGN - 79)) | (1L << (SUB_ASSIGN - 79)) | (1L << (MUL_ASSIGN - 79)) | (1L << (DIV_ASSIGN - 79)) | (1L << (AND_ASSIGN - 79)) | (1L << (OR_ASSIGN - 79)) | (1L << (XOR_ASSIGN - 79)) | (1L << (MOD_ASSIGN - 79)) | (1L << (LSHIFT_ASSIGN - 79)) | (1L << (RSHIFT_ASSIGN - 79)) | (1L << (URSHIFT_ASSIGN - 79)) | (1L << (ELVIS_ASSIGN - 79)))) != 0)) ) {
							((AssignmentExprAltContext)_localctx).op = _errHandler.recoverInline(this);
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
						((AssignmentExprAltContext)_localctx).right = enhancedStatementExpression();
						}
						break;
					}
					} 
				}
				setState(1471);
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
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}


	@RuleVersion(0)
	public final ExpressionContext castOperandExpression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_castOperandExpression);
		int _la;
		try {
			setState(1482);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,147,_ctx) ) {
			case 1:
				_localctx = new CastExprAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1472);
				castParExpression();
				setState(1473);
				castOperandExpression();
				}
				break;
			case 2:
				_localctx = new PostfixExprAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1475);
				postfixExpression();
				}
				break;
			case 3:
				_localctx = new UnaryNotExprAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1476);
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
				setState(1477);
				nls();
				setState(1478);
				castOperandExpression();
				}
				break;
			case 4:
				_localctx = new UnaryAddExprAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1480);
				((UnaryAddExprAltContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 109)) & ~0x3f) == 0 && ((1L << (_la - 109)) & ((1L << (INC - 109)) | (1L << (DEC - 109)) | (1L << (ADD - 109)) | (1L << (SUB - 109)))) != 0)) ) {
					((UnaryAddExprAltContext)_localctx).op = _errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) {
						matchedEOF = true;
					}

					_errHandler.reportMatch(this);
					consume();
				}
				setState(1481);
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
		enterRule(_localctx, 244, RULE_commandExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1484);
			_localctx.expression = expression(0);
			setState(1488);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
			case 1:
				{
				setState(1485);
				if (!( !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) )) throw createFailedPredicateException(" !SemanticPredicates.isFollowingArgumentsOrClosure($expression.ctx) ");
				setState(1486);
				argumentList();
				}
				break;
			case 2:
				{
				}
				break;
			}
			setState(1493);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,149,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1490);
					commandArgument();
					}
					} 
				}
				setState(1495);
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
		enterRule(_localctx, 246, RULE_commandArgument);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1496);
			commandPrimary();
			setState(1503);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
			case 1:
				{
				setState(1498); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1497);
						pathElement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1500); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,150,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 2:
				{
				setState(1502);
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
		enterRule(_localctx, 248, RULE_pathExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1508);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,152,_ctx) ) {
			case 1:
				{
				setState(1505);
				primary();
				}
				break;
			case 2:
				{
				setState(1506);
				if (!( _input.LT(2).getType() == DOT )) throw createFailedPredicateException(" _input.LT(2).getType() == DOT ");
				setState(1507);
				match(STATIC);
				}
				break;
			}
			setState(1515);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,153,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1510);
					_localctx.pathElement = pathElement();
					 _localctx.t =  _localctx.pathElement.t; 
					}
					} 
				}
				setState(1517);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,153,_ctx);
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
		enterRule(_localctx, 250, RULE_pathElement);
		int _la;
		try {
			setState(1557);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1518);
				nls();
				setState(1546);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,157,_ctx) ) {
				case 1:
					{
					setState(1519);
					match(DOT);
					setState(1520);
					nls();
					setState(1521);
					match(NEW);
					setState(1522);
					creator(1);
					 _localctx.t =  6; 
					}
					break;
				case 2:
					{
					setState(1538);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case SPREAD_DOT:
					case SAFE_DOT:
					case SAFE_CHAIN_DOT:
					case DOT:
						{
						setState(1525);
						_la = _input.LA(1);
						if ( !(((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & ((1L << (SPREAD_DOT - 69)) | (1L << (SAFE_DOT - 69)) | (1L << (SAFE_CHAIN_DOT - 69)) | (1L << (DOT - 69)))) != 0)) ) {
						_errHandler.recoverInline(this);
						} else {
							if (_input.LA(1) == Token.EOF) {
								matchedEOF = true;
							}

							_errHandler.reportMatch(this);
							consume();
						}
						setState(1526);
						nls();
						setState(1529);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case AT:
							{
							setState(1527);
							match(AT);
							}
							break;
						case LT:
							{
							setState(1528);
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
						case YIELD:
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
						setState(1531);
						match(METHOD_POINTER);
						setState(1532);
						nls();
						}
						break;
					case METHOD_REFERENCE:
						{
						setState(1533);
						match(METHOD_REFERENCE);
						setState(1534);
						nls();
						setState(1536);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(1535);
							nonWildcardTypeArguments();
							}
						}

						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1540);
					namePart();
					 _localctx.t =  1; 
					}
					break;
				case 3:
					{
					setState(1543);
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
				setState(1548);
				arguments();
				 _localctx.t =  2; 
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1551);
				indexPropertyArgs();
				 _localctx.t =  4; 
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1554);
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
		enterRule(_localctx, 252, RULE_namePart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1563);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				{
				setState(1559);
				identifier();
				}
				break;
			case 2:
				{
				setState(1560);
				stringLiteral();
				}
				break;
			case 3:
				{
				setState(1561);
				dynamicMemberName();
				}
				break;
			case 4:
				{
				setState(1562);
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
		enterRule(_localctx, 254, RULE_dynamicMemberName);
		try {
			setState(1567);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1565);
				parExpression();
				}
				break;
			case GStringBegin:
				enterOuterAlt(_localctx, 2);
				{
				setState(1566);
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
		enterRule(_localctx, 256, RULE_indexPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1569);
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
			setState(1571);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,161,_ctx) ) {
			case 1:
				{
				setState(1570);
				expressionList(true);
				}
				break;
			}
			setState(1573);
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
		enterRule(_localctx, 258, RULE_namedPropertyArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1575);
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
			setState(1578);
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
			case YIELD:
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
			case VOID:
			case VOLATILE:
			case WHILE:
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
				setState(1576);
				namedPropertyArgList();
				}
				break;
			case COLON:
				{
				setState(1577);
				match(COLON);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1580);
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
		enterRule(_localctx, 260, RULE_primary);
		try {
			setState(1599);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,164,_ctx) ) {
			case 1:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1582);
				identifier();
				setState(1584);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,163,_ctx) ) {
				case 1:
					{
					setState(1583);
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
				setState(1586);
				literal();
				}
				break;
			case 3:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1587);
				gstring();
				}
				break;
			case 4:
				_localctx = new NewPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1588);
				match(NEW);
				setState(1589);
				nls();
				setState(1590);
				creator(0);
				}
				break;
			case 5:
				_localctx = new ThisPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1592);
				match(THIS);
				}
				break;
			case 6:
				_localctx = new SuperPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1593);
				match(SUPER);
				}
				break;
			case 7:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1594);
				parExpression();
				}
				break;
			case 8:
				_localctx = new ClosureOrLambdaExpressionPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(1595);
				closureOrLambdaExpression();
				}
				break;
			case 9:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(1596);
				list();
				}
				break;
			case 10:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(1597);
				map();
				}
				break;
			case 11:
				_localctx = new BuiltInTypePrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(1598);
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
		enterRule(_localctx, 262, RULE_namedPropertyArgPrimary);
		try {
			setState(1607);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,165,_ctx) ) {
			case 1:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1601);
				identifier();
				}
				break;
			case 2:
				_localctx = new LiteralPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1602);
				literal();
				}
				break;
			case 3:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1603);
				gstring();
				}
				break;
			case 4:
				_localctx = new ParenPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1604);
				parExpression();
				}
				break;
			case 5:
				_localctx = new ListPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1605);
				list();
				}
				break;
			case 6:
				_localctx = new MapPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1606);
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
		enterRule(_localctx, 264, RULE_namedArgPrimary);
		try {
			setState(1612);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1609);
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
				setState(1610);
				literal();
				}
				break;
			case GStringBegin:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1611);
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
		enterRule(_localctx, 266, RULE_commandPrimary);
		try {
			setState(1617);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
			case IN:
			case TRAIT:
			case VAR:
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
				_localctx = new IdentifierPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1614);
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
				setState(1615);
				literal();
				}
				break;
			case GStringBegin:
				_localctx = new GstringPrmrAltContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1616);
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
		enterRule(_localctx, 268, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1619);
			match(LBRACK);
			setState(1621);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
			case 1:
				{
				setState(1620);
				expressionList(true);
				}
				break;
			}
			setState(1624);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1623);
				match(COMMA);
				}
			}

			setState(1626);
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
		enterRule(_localctx, 270, RULE_map);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1628);
			match(LBRACK);
			setState(1634);
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
			case YIELD:
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
				setState(1629);
				mapEntryList();
				setState(1631);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1630);
					match(COMMA);
					}
				}

				}
				break;
			case COLON:
				{
				setState(1633);
				match(COLON);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1636);
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
		enterRule(_localctx, 272, RULE_mapEntryList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1638);
			mapEntry();
			setState(1643);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,172,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1639);
					match(COMMA);
					setState(1640);
					mapEntry();
					}
					} 
				}
				setState(1645);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,172,_ctx);
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
		enterRule(_localctx, 274, RULE_namedPropertyArgList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1646);
			namedPropertyArg();
			setState(1651);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1647);
				match(COMMA);
				setState(1648);
				namedPropertyArg();
				}
				}
				setState(1653);
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
		enterRule(_localctx, 276, RULE_mapEntry);
		try {
			setState(1664);
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
			case YIELD:
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
				setState(1654);
				mapEntryLabel();
				setState(1655);
				match(COLON);
				setState(1656);
				nls();
				setState(1657);
				expression(0);
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1659);
				match(MUL);
				setState(1660);
				match(COLON);
				setState(1661);
				nls();
				setState(1662);
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
		enterRule(_localctx, 278, RULE_namedPropertyArg);
		try {
			setState(1676);
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
			case YIELD:
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
			case VOID:
			case VOLATILE:
			case WHILE:
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
				setState(1666);
				namedPropertyArgLabel();
				setState(1667);
				match(COLON);
				setState(1668);
				nls();
				setState(1669);
				expression(0);
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1671);
				match(MUL);
				setState(1672);
				match(COLON);
				setState(1673);
				nls();
				setState(1674);
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
		enterRule(_localctx, 280, RULE_namedArg);
		try {
			setState(1688);
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
			case YIELD:
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
				setState(1678);
				namedArgLabel();
				setState(1679);
				match(COLON);
				setState(1680);
				nls();
				setState(1681);
				expression(0);
				}
				break;
			case MUL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1683);
				match(MUL);
				setState(1684);
				match(COLON);
				setState(1685);
				nls();
				setState(1686);
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
		enterRule(_localctx, 282, RULE_mapEntryLabel);
		try {
			setState(1692);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1690);
				keywords();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1691);
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
		enterRule(_localctx, 284, RULE_namedPropertyArgLabel);
		try {
			setState(1696);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1694);
				keywords();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1695);
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
		enterRule(_localctx, 286, RULE_namedArgLabel);
		try {
			setState(1700);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,179,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1698);
				keywords();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1699);
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
		enterRule(_localctx, 288, RULE_creator);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1702);
			createdName();
			setState(1718);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
			case NL:
				{
				setState(1703);
				nls();
				setState(1704);
				arguments();
				setState(1706);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
				case 1:
					{
					setState(1705);
					anonymousInnerClassDeclaration(0);
					}
					break;
				}
				}
				break;
			case LBRACK:
			case AT:
				{
				setState(1709); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1708);
						dim();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1711); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,181,_ctx);
				} while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1716);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,182,_ctx) ) {
				case 1:
					{
					setState(1713);
					nls();
					setState(1714);
					arrayInitializer();
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
		enterRule(_localctx, 290, RULE_dim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1720);
			annotationsOpt();
			setState(1721);
			match(LBRACK);
			setState(1723);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,184,_ctx) ) {
			case 1:
				{
				setState(1722);
				expression(0);
				}
				break;
			}
			setState(1725);
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
		enterRule(_localctx, 292, RULE_arrayInitializer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1727);
			match(LBRACE);
			setState(1728);
			nls();
			setState(1732);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,185,_ctx) ) {
			case 1:
				{
				setState(1729);
				variableInitializers();
				setState(1730);
				nls();
				}
				break;
			}
			setState(1734);
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
		enterRule(_localctx, 294, RULE_anonymousInnerClassDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1736);
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
		enterRule(_localctx, 296, RULE_createdName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1738);
			annotationsOpt();
			setState(1744);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BuiltInPrimitiveType:
				{
				setState(1739);
				primitiveType();
				}
				break;
			case AS:
			case DEF:
			case IN:
			case TRAIT:
			case VAR:
			case YIELD:
			case PERMITS:
			case RECORD:
			case SEALED:
			case CapitalizedIdentifier:
			case Identifier:
				{
				setState(1740);
				qualifiedClassName();
				setState(1742);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(1741);
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
		enterRule(_localctx, 298, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1746);
			match(LT);
			setState(1747);
			nls();
			setState(1748);
			typeList();
			setState(1749);
			nls();
			setState(1750);
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
		enterRule(_localctx, 300, RULE_typeArgumentsOrDiamond);
		try {
			setState(1755);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,188,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1752);
				match(LT);
				setState(1753);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1754);
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
		enterRule(_localctx, 302, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1757);
			match(LPAREN);
			setState(1759);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,189,_ctx) ) {
			case 1:
				{
				setState(1758);
				enhancedArgumentListInPar();
				}
				break;
			}
			setState(1762);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1761);
				match(COMMA);
				}
			}

			setState(1764);
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
		enterRule(_localctx, 304, RULE_argumentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1766);
			firstArgumentListElement();
			setState(1773);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,191,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1767);
					match(COMMA);
					setState(1768);
					nls();
					setState(1769);
					argumentListElement();
					}
					} 
				}
				setState(1775);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,191,_ctx);
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
		enterRule(_localctx, 306, RULE_enhancedArgumentListInPar);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1776);
			enhancedArgumentListElement();
			setState(1783);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,192,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1777);
					match(COMMA);
					setState(1778);
					nls();
					setState(1779);
					enhancedArgumentListElement();
					}
					} 
				}
				setState(1785);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,192,_ctx);
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
		enterRule(_localctx, 308, RULE_firstArgumentListElement);
		try {
			setState(1788);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,193,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1786);
				expressionListElement(true);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1787);
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
		enterRule(_localctx, 310, RULE_argumentListElement);
		try {
			setState(1792);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1790);
				expressionListElement(true);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1791);
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
		enterRule(_localctx, 312, RULE_enhancedArgumentListElement);
		try {
			setState(1797);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1794);
				expressionListElement(true);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1795);
				standardLambdaExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1796);
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
		enterRule(_localctx, 314, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1799);
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
		enterRule(_localctx, 316, RULE_className);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1801);
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
		public TerminalNode IN() { return getToken(GroovyParser.IN, 0); }
		public TerminalNode PERMITS() { return getToken(GroovyParser.PERMITS, 0); }
		public TerminalNode RECORD() { return getToken(GroovyParser.RECORD, 0); }
		public TerminalNode SEALED() { return getToken(GroovyParser.SEALED, 0); }
		public TerminalNode TRAIT() { return getToken(GroovyParser.TRAIT, 0); }
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
		enterRule(_localctx, 318, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1803);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << IN) | (1L << TRAIT) | (1L << VAR) | (1L << YIELD) | (1L << PERMITS) | (1L << RECORD) | (1L << SEALED))) != 0) || _la==CapitalizedIdentifier || _la==Identifier) ) {
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
		enterRule(_localctx, 320, RULE_builtInType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1805);
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
		enterRule(_localctx, 322, RULE_keywords);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1807);
			_la = _input.LA(1);
			if ( !(((((_la - 7)) & ~0x3f) == 0 && ((1L << (_la - 7)) & ((1L << (AS - 7)) | (1L << (DEF - 7)) | (1L << (IN - 7)) | (1L << (TRAIT - 7)) | (1L << (THREADSAFE - 7)) | (1L << (VAR - 7)) | (1L << (BuiltInPrimitiveType - 7)) | (1L << (ABSTRACT - 7)) | (1L << (ASSERT - 7)) | (1L << (BREAK - 7)) | (1L << (YIELD - 7)) | (1L << (CASE - 7)) | (1L << (CATCH - 7)) | (1L << (CLASS - 7)) | (1L << (CONST - 7)) | (1L << (CONTINUE - 7)) | (1L << (DEFAULT - 7)) | (1L << (DO - 7)) | (1L << (ELSE - 7)) | (1L << (ENUM - 7)) | (1L << (EXTENDS - 7)) | (1L << (FINAL - 7)) | (1L << (FINALLY - 7)) | (1L << (FOR - 7)) | (1L << (IF - 7)) | (1L << (GOTO - 7)) | (1L << (IMPLEMENTS - 7)) | (1L << (IMPORT - 7)) | (1L << (INSTANCEOF - 7)) | (1L << (INTERFACE - 7)) | (1L << (NATIVE - 7)) | (1L << (NEW - 7)) | (1L << (NON_SEALED - 7)) | (1L << (PACKAGE - 7)) | (1L << (PERMITS - 7)) | (1L << (PRIVATE - 7)) | (1L << (PROTECTED - 7)) | (1L << (PUBLIC - 7)) | (1L << (RECORD - 7)) | (1L << (RETURN - 7)) | (1L << (SEALED - 7)) | (1L << (STATIC - 7)) | (1L << (STRICTFP - 7)) | (1L << (SUPER - 7)) | (1L << (SWITCH - 7)) | (1L << (SYNCHRONIZED - 7)) | (1L << (THIS - 7)) | (1L << (THROW - 7)) | (1L << (THROWS - 7)) | (1L << (TRANSIENT - 7)) | (1L << (TRY - 7)) | (1L << (VOID - 7)) | (1L << (VOLATILE - 7)) | (1L << (WHILE - 7)) | (1L << (BooleanLiteral - 7)) | (1L << (NullLiteral - 7)))) != 0)) ) {
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
		enterRule(_localctx, 324, RULE_rparen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1809);
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
		enterRule(_localctx, 326, RULE_nls);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1814);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,196,_ctx);
			while ( _alt!=2 && _alt!=groovyjarjarantlr4.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1811);
					match(NL);
					}
					} 
				}
				setState(1816);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,196,_ctx);
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
		enterRule(_localctx, 328, RULE_sep);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1818); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1817);
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
				setState(1820); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,197,_ctx);
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
		case 81:
			return localVariableDeclaration_sempred((LocalVariableDeclarationContext)_localctx, predIndex);
		case 95:
			return statement_sempred((StatementContext)_localctx, predIndex);
		case 120:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 122:
			return commandExpression_sempred((CommandExpressionContext)_localctx, predIndex);
		case 124:
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
	private boolean statement_sempred(StatementContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return  inSwitchExpressionLevel > 0 ;
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return precpred(_ctx, 18);
		case 5:
			return precpred(_ctx, 16);
		case 6:
			return precpred(_ctx, 15);
		case 7:
			return precpred(_ctx, 14);
		case 8:
			return precpred(_ctx, 12);
		case 9:
			return precpred(_ctx, 11);
		case 10:
			return precpred(_ctx, 10);
		case 11:
			return precpred(_ctx, 9);
		case 12:
			return precpred(_ctx, 8);
		case 13:
			return precpred(_ctx, 7);
		case 14:
			return precpred(_ctx, 6);
		case 15:
			return precpred(_ctx, 5);
		case 16:
			return precpred(_ctx, 4);
		case 17:
			return precpred(_ctx, 3);
		case 18:
			return precpred(_ctx, 13);
		case 19:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean commandExpression_sempred(CommandExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 20:
			return  !SemanticPredicates.isFollowingArgumentsOrClosure(_localctx.expression) ;
		}
		return true;
	}
	private boolean pathExpression_sempred(PathExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 21:
			return  _input.LT(2).getType() == DOT ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\3\u008c\u0721\4\2\t"+
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
		"\4\u00a5\t\u00a5\4\u00a6\t\u00a6\3\2\3\2\3\2\5\2\u0150\n\2\5\2\u0152\n"+
		"\2\3\2\5\2\u0155\n\2\3\2\3\2\3\3\3\3\3\3\3\3\7\3\u015d\n\3\f\3\16\3\u0160"+
		"\13\3\3\3\5\3\u0163\n\3\3\4\3\4\3\4\3\4\3\4\5\4\u016a\n\4\3\5\3\5\3\5"+
		"\3\5\3\6\3\6\3\6\5\6\u0173\n\6\3\6\3\6\3\6\3\6\3\6\5\6\u017a\n\6\3\7\3"+
		"\7\3\7\3\b\3\b\5\b\u0181\n\b\3\t\3\t\3\t\5\t\u0186\n\t\3\n\3\n\3\n\3\n"+
		"\7\n\u018c\n\n\f\n\16\n\u018f\13\n\3\13\3\13\7\13\u0193\n\13\f\13\16\13"+
		"\u0196\13\13\5\13\u0198\n\13\3\f\3\f\3\f\3\f\7\f\u019e\n\f\f\f\16\f\u01a1"+
		"\13\f\3\r\3\r\5\r\u01a5\n\r\3\16\3\16\5\16\u01a9\n\16\3\17\3\17\3\17\5"+
		"\17\u01ae\n\17\3\20\3\20\3\20\3\20\7\20\u01b4\n\20\f\20\16\20\u01b7\13"+
		"\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\7\21\u01c0\n\21\f\21\16\21\u01c3"+
		"\13\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u01ce\n\22\3"+
		"\23\3\23\3\23\3\23\3\23\7\23\u01d5\n\23\f\23\16\23\u01d8\13\23\3\24\3"+
		"\24\3\24\3\24\3\24\7\24\u01df\n\24\f\24\16\24\u01e2\13\24\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u01f1\n\25"+
		"\3\25\3\25\3\25\3\25\5\25\u01f7\n\25\3\25\3\25\3\25\5\25\u01fc\n\25\3"+
		"\25\3\25\3\25\3\25\3\25\5\25\u0203\n\25\3\25\3\25\3\25\3\25\3\25\5\25"+
		"\u020a\n\25\3\25\3\25\3\25\3\25\3\25\5\25\u0211\n\25\3\25\3\25\3\25\3"+
		"\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u021d\n\26\3\26\5\26\u0220\n\26"+
		"\3\26\5\26\u0223\n\26\3\26\3\26\3\26\3\26\7\26\u0229\n\26\f\26\16\26\u022c"+
		"\13\26\5\26\u022e\n\26\3\26\5\26\u0231\n\26\3\26\3\26\3\27\3\27\3\27\3"+
		"\27\3\27\3\27\7\27\u023b\n\27\f\27\16\27\u023e\13\27\3\30\3\30\3\30\5"+
		"\30\u0243\n\30\3\30\5\30\u0246\n\30\3\31\3\31\5\31\u024a\n\31\3\31\3\31"+
		"\5\31\u024e\n\31\3\32\3\32\3\32\3\32\3\32\5\32\u0255\n\32\5\32\u0257\n"+
		"\32\3\33\3\33\5\33\u025b\n\33\3\33\3\33\3\33\5\33\u0260\n\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u026d\n\33\3\33\3\33"+
		"\3\33\5\33\u0272\n\33\5\33\u0274\n\33\3\34\3\34\3\34\3\34\3\35\3\35\5"+
		"\35\u027c\n\35\3\36\3\36\5\36\u0280\n\36\3\37\3\37\3 \3 \3 \3 \3 \7 \u0289"+
		"\n \f \16 \u028c\13 \3!\3!\3!\3!\3!\3!\5!\u0294\n!\3\"\3\"\3#\3#\3$\3"+
		"$\3$\3$\3$\3$\7$\u02a0\n$\f$\16$\u02a3\13$\3$\3$\5$\u02a7\n$\3%\3%\3%"+
		"\3%\6%\u02ad\n%\r%\16%\u02ae\3&\5&\u02b2\n&\3\'\3\'\3\'\5\'\u02b7\n\'"+
		"\3\'\3\'\3(\3(\3(\5(\u02be\n(\3(\5(\u02c1\n(\3(\3(\3)\3)\5)\u02c7\n)\3"+
		")\5)\u02ca\n)\3*\3*\5*\u02ce\n*\3+\3+\5+\u02d2\n+\3,\3,\3-\3-\3-\3-\3"+
		"-\3-\3-\7-\u02dd\n-\f-\16-\u02e0\13-\3-\3-\3-\3.\3.\3.\3.\3.\3.\3.\5."+
		"\u02ec\n.\5.\u02ee\n.\3/\3/\3/\3\60\3\60\3\60\3\60\3\60\7\60\u02f8\n\60"+
		"\f\60\16\60\u02fb\13\60\3\61\3\61\5\61\u02ff\n\61\3\61\3\61\3\62\3\62"+
		"\5\62\u0305\n\62\3\62\3\62\3\62\3\62\7\62\u030b\n\62\f\62\16\62\u030e"+
		"\13\62\3\63\3\63\3\63\3\64\3\64\5\64\u0315\n\64\3\64\5\64\u0318\n\64\3"+
		"\64\3\64\3\64\3\64\3\64\3\64\5\64\u0320\n\64\3\65\3\65\3\66\3\66\3\66"+
		"\7\66\u0327\n\66\f\66\16\66\u032a\13\66\3\67\3\67\3\67\3\67\3\67\5\67"+
		"\u0331\n\67\38\38\38\78\u0336\n8\f8\168\u0339\138\39\39\39\3:\3:\3:\3"+
		":\7:\u0342\n:\f:\16:\u0345\13:\3;\3;\3;\3;\3;\5;\u034c\n;\3<\3<\3<\3<"+
		"\7<\u0352\n<\f<\16<\u0355\13<\3<\3<\3=\3=\5=\u035b\n=\3>\3>\7>\u035f\n"+
		">\f>\16>\u0362\13>\3?\3?\3?\3?\3?\3?\3@\3@\3@\3@\3@\3@\3A\3A\3B\3B\5B"+
		"\u0374\nB\3C\3C\5C\u0378\nC\3D\3D\3D\3D\3D\5D\u037f\nD\3D\3D\5D\u0383"+
		"\nD\3D\5D\u0386\nD\3D\3D\3D\3E\3E\5E\u038d\nE\3F\5F\u0390\nF\3G\3G\3G"+
		"\3G\7G\u0396\nG\fG\16G\u0399\13G\3G\5G\u039c\nG\3H\3H\3H\3H\7H\u03a2\n"+
		"H\fH\16H\u03a5\13H\3H\3H\5H\u03a9\nH\3I\3I\3I\3I\3I\5I\u03b0\nI\3I\3I"+
		"\5I\u03b4\nI\3J\3J\5J\u03b8\nJ\3K\3K\3L\3L\3L\7L\u03bf\nL\fL\16L\u03c2"+
		"\13L\3M\3M\3M\3M\3M\3M\3N\3N\5N\u03cc\nN\3O\3O\3O\5O\u03d1\nO\3P\3P\3"+
		"P\3P\7P\u03d7\nP\fP\16P\u03da\13P\3P\5P\u03dd\nP\5P\u03df\nP\3P\3P\3Q"+
		"\3Q\5Q\u03e5\nQ\3Q\3Q\3Q\3R\3R\5R\u03ec\nR\3S\3S\3S\3T\3T\3T\5T\u03f4"+
		"\nT\3T\3T\3T\3T\3T\3T\3T\5T\u03fd\nT\3T\3T\3T\5T\u0402\nT\3U\3U\3U\3U"+
		"\7U\u0408\nU\fU\16U\u040b\13U\3U\3U\3V\5V\u0410\nV\3V\3V\3W\3W\3W\3W\6"+
		"W\u0418\nW\rW\16W\u0419\3W\3W\3X\3X\5X\u0420\nX\3Y\3Y\3Y\3Y\3Y\3Y\5Y\u0428"+
		"\nY\3Y\3Y\3Y\3Y\5Y\u042e\nY\3Z\3Z\3Z\3Z\3Z\3Z\6Z\u0436\nZ\rZ\16Z\u0437"+
		"\3Z\3Z\5Z\u043c\nZ\3Z\3Z\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3[\3["+
		"\3[\3[\3[\3[\5[\u0453\n[\3\\\3\\\5\\\u0457\n\\\3]\3]\5]\u045b\n]\3^\3"+
		"^\3^\3_\3_\5_\u0462\n_\3_\3_\3_\3_\3_\7_\u0469\n_\f_\16_\u046c\13_\3_"+
		"\3_\3_\5_\u0471\n_\3`\3`\3`\3`\3`\3`\3`\5`\u047a\n`\3a\3a\3a\3a\3a\3a"+
		"\3a\3a\3a\3a\3a\5a\u0487\na\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a"+
		"\3a\5a\u0498\na\3b\3b\3b\3b\5b\u049e\nb\3b\3b\3b\3b\3b\3c\3c\3c\7c\u04a8"+
		"\nc\fc\16c\u04ab\13c\3d\3d\3d\3d\3e\3e\3e\3e\5e\u04b5\ne\3e\3e\3f\3f\3"+
		"f\3f\7f\u04bd\nf\ff\16f\u04c0\13f\3g\3g\5g\u04c4\ng\3h\3h\3h\3h\7h\u04ca"+
		"\nh\fh\16h\u04cd\13h\3h\3h\3h\3i\3i\3i\3i\3i\3i\5i\u04d8\ni\3j\3j\5j\u04dc"+
		"\nj\3k\3k\5k\u04e0\nk\3k\3k\3k\3k\3l\5l\u04e7\nl\3l\3l\5l\u04eb\nl\3l"+
		"\3l\5l\u04ef\nl\3m\3m\5m\u04f3\nm\3n\3n\3o\3o\3o\3o\3p\3p\3q\3q\3q\3q"+
		"\3r\3r\3r\3r\3r\7r\u0506\nr\fr\16r\u0509\13r\3s\5s\u050c\ns\3s\3s\3t\3"+
		"t\5t\u0512\nt\3u\3u\3v\3v\5v\u0518\nv\3w\3w\3w\3w\3w\3w\7w\u0520\nw\f"+
		"w\16w\u0523\13w\3w\3w\3w\3x\3x\3x\6x\u052b\nx\rx\16x\u052c\3x\3x\3y\3"+
		"y\3y\5y\u0534\ny\3y\3y\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3"+
		"z\3z\3z\5z\u054a\nz\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3"+
		"z\3z\3z\3z\3z\3z\3z\3z\3z\5z\u0565\nz\3z\5z\u0568\nz\3z\3z\3z\3z\3z\3"+
		"z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3"+
		"z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3"+
		"z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\5z\u05ae\nz\3z\3z\3"+
		"z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\7z\u05be\nz\fz\16z\u05c1\13z\3{\3{"+
		"\3{\3{\3{\3{\3{\3{\3{\3{\5{\u05cd\n{\3|\3|\3|\3|\5|\u05d3\n|\3|\7|\u05d6"+
		"\n|\f|\16|\u05d9\13|\3}\3}\6}\u05dd\n}\r}\16}\u05de\3}\5}\u05e2\n}\3~"+
		"\3~\3~\5~\u05e7\n~\3~\3~\3~\7~\u05ec\n~\f~\16~\u05ef\13~\3\177\3\177\3"+
		"\177\3\177\3\177\3\177\3\177\3\177\3\177\3\177\3\177\5\177\u05fc\n\177"+
		"\3\177\3\177\3\177\3\177\3\177\5\177\u0603\n\177\5\177\u0605\n\177\3\177"+
		"\3\177\3\177\3\177\3\177\3\177\5\177\u060d\n\177\3\177\3\177\3\177\3\177"+
		"\3\177\3\177\3\177\3\177\3\177\5\177\u0618\n\177\3\u0080\3\u0080\3\u0080"+
		"\3\u0080\5\u0080\u061e\n\u0080\3\u0081\3\u0081\5\u0081\u0622\n\u0081\3"+
		"\u0082\3\u0082\5\u0082\u0626\n\u0082\3\u0082\3\u0082\3\u0083\3\u0083\3"+
		"\u0083\5\u0083\u062d\n\u0083\3\u0083\3\u0083\3\u0084\3\u0084\5\u0084\u0633"+
		"\n\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084"+
		"\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\5\u0084\u0642\n\u0084\3\u0085"+
		"\3\u0085\3\u0085\3\u0085\3\u0085\3\u0085\5\u0085\u064a\n\u0085\3\u0086"+
		"\3\u0086\3\u0086\5\u0086\u064f\n\u0086\3\u0087\3\u0087\3\u0087\5\u0087"+
		"\u0654\n\u0087\3\u0088\3\u0088\5\u0088\u0658\n\u0088\3\u0088\5\u0088\u065b"+
		"\n\u0088\3\u0088\3\u0088\3\u0089\3\u0089\3\u0089\5\u0089\u0662\n\u0089"+
		"\3\u0089\5\u0089\u0665\n\u0089\3\u0089\3\u0089\3\u008a\3\u008a\3\u008a"+
		"\7\u008a\u066c\n\u008a\f\u008a\16\u008a\u066f\13\u008a\3\u008b\3\u008b"+
		"\3\u008b\7\u008b\u0674\n\u008b\f\u008b\16\u008b\u0677\13\u008b\3\u008c"+
		"\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c\3\u008c"+
		"\5\u008c\u0683\n\u008c\3\u008d\3\u008d\3\u008d\3\u008d\3\u008d\3\u008d"+
		"\3\u008d\3\u008d\3\u008d\3\u008d\5\u008d\u068f\n\u008d\3\u008e\3\u008e"+
		"\3\u008e\3\u008e\3\u008e\3\u008e\3\u008e\3\u008e\3\u008e\3\u008e\5\u008e"+
		"\u069b\n\u008e\3\u008f\3\u008f\5\u008f\u069f\n\u008f\3\u0090\3\u0090\5"+
		"\u0090\u06a3\n\u0090\3\u0091\3\u0091\5\u0091\u06a7\n\u0091\3\u0092\3\u0092"+
		"\3\u0092\3\u0092\5\u0092\u06ad\n\u0092\3\u0092\6\u0092\u06b0\n\u0092\r"+
		"\u0092\16\u0092\u06b1\3\u0092\3\u0092\3\u0092\5\u0092\u06b7\n\u0092\5"+
		"\u0092\u06b9\n\u0092\3\u0093\3\u0093\3\u0093\5\u0093\u06be\n\u0093\3\u0093"+
		"\3\u0093\3\u0094\3\u0094\3\u0094\3\u0094\3\u0094\5\u0094\u06c7\n\u0094"+
		"\3\u0094\3\u0094\3\u0095\3\u0095\3\u0096\3\u0096\3\u0096\3\u0096\5\u0096"+
		"\u06d1\n\u0096\5\u0096\u06d3\n\u0096\3\u0097\3\u0097\3\u0097\3\u0097\3"+
		"\u0097\3\u0097\3\u0098\3\u0098\3\u0098\5\u0098\u06de\n\u0098\3\u0099\3"+
		"\u0099\5\u0099\u06e2\n\u0099\3\u0099\5\u0099\u06e5\n\u0099\3\u0099\3\u0099"+
		"\3\u009a\3\u009a\3\u009a\3\u009a\3\u009a\7\u009a\u06ee\n\u009a\f\u009a"+
		"\16\u009a\u06f1\13\u009a\3\u009b\3\u009b\3\u009b\3\u009b\3\u009b\7\u009b"+
		"\u06f8\n\u009b\f\u009b\16\u009b\u06fb\13\u009b\3\u009c\3\u009c\5\u009c"+
		"\u06ff\n\u009c\3\u009d\3\u009d\5\u009d\u0703\n\u009d\3\u009e\3\u009e\3"+
		"\u009e\5\u009e\u0708\n\u009e\3\u009f\3\u009f\3\u00a0\3\u00a0\3\u00a1\3"+
		"\u00a1\3\u00a2\3\u00a2\3\u00a3\3\u00a3\3\u00a4\3\u00a4\3\u00a5\7\u00a5"+
		"\u0717\n\u00a5\f\u00a5\16\u00a5\u071a\13\u00a5\3\u00a6\6\u00a6\u071d\n"+
		"\u00a6\r\u00a6\16\u00a6\u071e\3\u00a6\2\2\3\u00f2\u00a7\2\2\4\2\6\2\b"+
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
		"\2\u0122\2\u0124\2\u0126\2\u0128\2\u012a\2\u012c\2\u012e\2\u0130\2\u0132"+
		"\2\u0134\2\u0136\2\u0138\2\u013a\2\u013c\2\u013e\2\u0140\2\u0142\2\u0144"+
		"\2\u0146\2\u0148\2\u014a\2\2\32\b\2\n\n\16\16\'\'\66\66::==\b\2\20\20"+
		"\31\31\36\36)),.\61\63\b\2\n\n\16\16\20\20\36\36,.\62\63\4\2\35\35\64"+
		"\64\4\2``hh\4\2\13\13hh\3\2op\4\2VVhh\3\2ef\3\2or\4\2stxx\3\2qr\3\2CF"+
		"\6\2\13\13XXcdjk\6\2RSUUiill\3\2NO\5\2\t\t%%WW\5\2QQbby\u0084\5\2GHJJ"+
		"aa\4\2II]]\n\2\t\t\13\f\16\16\23\23++//\61\61\u0085\u0086\4\2\17\17<<"+
		"\4\2\t>AB\4\2__\u008a\u008a\2\u078c\2\u014c\3\2\2\2\4\u0158\3\2\2\2\6"+
		"\u0169\3\2\2\2\b\u016b\3\2\2\2\n\u016f\3\2\2\2\f\u017b\3\2\2\2\16\u0180"+
		"\3\2\2\2\20\u0185\3\2\2\2\22\u0187\3\2\2\2\24\u0197\3\2\2\2\26\u0199\3"+
		"\2\2\2\30\u01a4\3\2\2\2\32\u01a8\3\2\2\2\34\u01ad\3\2\2\2\36\u01af\3\2"+
		"\2\2 \u01b8\3\2\2\2\"\u01c7\3\2\2\2$\u01cf\3\2\2\2&\u01d9\3\2\2\2(\u01f0"+
		"\3\2\2\2*\u0215\3\2\2\2,\u0234\3\2\2\2.\u023f\3\2\2\2\60\u024d\3\2\2\2"+
		"\62\u0256\3\2\2\2\64\u0258\3\2\2\2\66\u0275\3\2\2\28\u027b\3\2\2\2:\u027f"+
		"\3\2\2\2<\u0281\3\2\2\2>\u0283\3\2\2\2@\u028d\3\2\2\2B\u0295\3\2\2\2D"+
		"\u0297\3\2\2\2F\u0299\3\2\2\2H\u02ac\3\2\2\2J\u02b1\3\2\2\2L\u02b3\3\2"+
		"\2\2N\u02ba\3\2\2\2P\u02c6\3\2\2\2R\u02cb\3\2\2\2T\u02cf\3\2\2\2V\u02d3"+
		"\3\2\2\2X\u02d5\3\2\2\2Z\u02ed\3\2\2\2\\\u02ef\3\2\2\2^\u02f2\3\2\2\2"+
		"`\u02fc\3\2\2\2b\u0304\3\2\2\2d\u030f\3\2\2\2f\u0312\3\2\2\2h\u0321\3"+
		"\2\2\2j\u0323\3\2\2\2l\u0330\3\2\2\2n\u0337\3\2\2\2p\u033a\3\2\2\2r\u033d"+
		"\3\2\2\2t\u034b\3\2\2\2v\u034d\3\2\2\2x\u035a\3\2\2\2z\u035c\3\2\2\2|"+
		"\u0363\3\2\2\2~\u0369\3\2\2\2\u0080\u036f\3\2\2\2\u0082\u0373\3\2\2\2"+
		"\u0084\u0377\3\2\2\2\u0086\u0379\3\2\2\2\u0088\u038c\3\2\2\2\u008a\u038f"+
		"\3\2\2\2\u008c\u0391\3\2\2\2\u008e\u03a8\3\2\2\2\u0090\u03aa\3\2\2\2\u0092"+
		"\u03b7\3\2\2\2\u0094\u03b9\3\2\2\2\u0096\u03bb\3\2\2\2\u0098\u03c3\3\2"+
		"\2\2\u009a\u03cb\3\2\2\2\u009c\u03d0\3\2\2\2\u009e\u03d2\3\2\2\2\u00a0"+
		"\u03e2\3\2\2\2\u00a2\u03eb\3\2\2\2\u00a4\u03ed\3\2\2\2\u00a6\u0401\3\2"+
		"\2\2\u00a8\u0403\3\2\2\2\u00aa\u040f\3\2\2\2\u00ac\u0413\3\2\2\2\u00ae"+
		"\u041f\3\2\2\2\u00b0\u0421\3\2\2\2\u00b2\u042f\3\2\2\2\u00b4\u0452\3\2"+
		"\2\2\u00b6\u0454\3\2\2\2\u00b8\u0458\3\2\2\2\u00ba\u045c\3\2\2\2\u00bc"+
		"\u045f\3\2\2\2\u00be\u0472\3\2\2\2\u00c0\u0497\3\2\2\2\u00c2\u0499\3\2"+
		"\2\2\u00c4\u04a4\3\2\2\2\u00c6\u04ac\3\2\2\2\u00c8\u04b0\3\2\2\2\u00ca"+
		"\u04b8\3\2\2\2\u00cc\u04c3\3\2\2\2\u00ce\u04c5\3\2\2\2\u00d0\u04d7\3\2"+
		"\2\2\u00d2\u04db\3\2\2\2\u00d4\u04dd\3\2\2\2\u00d6\u04e6\3\2\2\2\u00d8"+
		"\u04f2\3\2\2\2\u00da\u04f4\3\2\2\2\u00dc\u04f6\3\2\2\2\u00de\u04fa\3\2"+
		"\2\2\u00e0\u04fc\3\2\2\2\u00e2\u0500\3\2\2\2\u00e4\u050b\3\2\2\2\u00e6"+
		"\u0511\3\2\2\2\u00e8\u0513\3\2\2\2\u00ea\u0515\3\2\2\2\u00ec\u0519\3\2"+
		"\2\2\u00ee\u052a\3\2\2\2\u00f0\u0533\3\2\2\2\u00f2\u0549\3\2\2\2\u00f4"+
		"\u05cc\3\2\2\2\u00f6\u05ce\3\2\2\2\u00f8\u05da\3\2\2\2\u00fa\u05e6\3\2"+
		"\2\2\u00fc\u0617\3\2\2\2\u00fe\u061d\3\2\2\2\u0100\u0621\3\2\2\2\u0102"+
		"\u0623\3\2\2\2\u0104\u0629\3\2\2\2\u0106\u0641\3\2\2\2\u0108\u0649\3\2"+
		"\2\2\u010a\u064e\3\2\2\2\u010c\u0653\3\2\2\2\u010e\u0655\3\2\2\2\u0110"+
		"\u065e\3\2\2\2\u0112\u0668\3\2\2\2\u0114\u0670\3\2\2\2\u0116\u0682\3\2"+
		"\2\2\u0118\u068e\3\2\2\2\u011a\u069a\3\2\2\2\u011c\u069e\3\2\2\2\u011e"+
		"\u06a2\3\2\2\2\u0120\u06a6\3\2\2\2\u0122\u06a8\3\2\2\2\u0124\u06ba\3\2"+
		"\2\2\u0126\u06c1\3\2\2\2\u0128\u06ca\3\2\2\2\u012a\u06cc\3\2\2\2\u012c"+
		"\u06d4\3\2\2\2\u012e\u06dd\3\2\2\2\u0130\u06df\3\2\2\2\u0132\u06e8\3\2"+
		"\2\2\u0134\u06f2\3\2\2\2\u0136\u06fe\3\2\2\2\u0138\u0702\3\2\2\2\u013a"+
		"\u0707\3\2\2\2\u013c\u0709\3\2\2\2\u013e\u070b\3\2\2\2\u0140\u070d\3\2"+
		"\2\2\u0142\u070f\3\2\2\2\u0144\u0711\3\2\2\2\u0146\u0713\3\2\2\2\u0148"+
		"\u0718\3\2\2\2\u014a\u071c\3\2\2\2\u014c\u0151\5\u0148\u00a5\2\u014d\u014f"+
		"\5\b\5\2\u014e\u0150\5\u014a\u00a6\2\u014f\u014e\3\2\2\2\u014f\u0150\3"+
		"\2\2\2\u0150\u0152\3\2\2\2\u0151\u014d\3\2\2\2\u0151\u0152\3\2\2\2\u0152"+
		"\u0154\3\2\2\2\u0153\u0155\5\4\3\2\u0154\u0153\3\2\2\2\u0154\u0155\3\2"+
		"\2\2\u0155\u0156\3\2\2\2\u0156\u0157\7\2\2\3\u0157\3\3\2\2\2\u0158\u015e"+
		"\5\6\4\2\u0159\u015a\5\u014a\u00a6\2\u015a\u015b\5\6\4\2\u015b\u015d\3"+
		"\2\2\2\u015c\u0159\3\2\2\2\u015d\u0160\3\2\2\2\u015e\u015c\3\2\2\2\u015e"+
		"\u015f\3\2\2\2\u015f\u0162\3\2\2\2\u0160\u015e\3\2\2\2\u0161\u0163\5\u014a"+
		"\u00a6\2\u0162\u0161\3\2\2\2\u0162\u0163\3\2\2\2\u0163\5\3\2\2\2\u0164"+
		"\u016a\5\n\6\2\u0165\u016a\5\f\7\2\u0166\u0167\6\4\2\2\u0167\u016a\5\64"+
		"\33\2\u0168\u016a\5\u00c0a\2\u0169\u0164\3\2\2\2\u0169\u0165\3\2\2\2\u0169"+
		"\u0166\3\2\2\2\u0169\u0168\3\2\2\2\u016a\7\3\2\2\2\u016b\u016c\5\u008e"+
		"H\2\u016c\u016d\7*\2\2\u016d\u016e\5j\66\2\u016e\t\3\2\2\2\u016f\u0170"+
		"\5\u008eH\2\u0170\u0172\7$\2\2\u0171\u0173\7\62\2\2\u0172\u0171\3\2\2"+
		"\2\u0172\u0173\3\2\2\2\u0173\u0174\3\2\2\2\u0174\u0179\5j\66\2\u0175\u0176"+
		"\7a\2\2\u0176\u017a\7s\2\2\u0177\u0178\7\t\2\2\u0178\u017a\5\u0140\u00a1"+
		"\2\u0179\u0175\3\2\2\2\u0179\u0177\3\2\2\2\u0179\u017a\3\2\2\2\u017a\13"+
		"\3\2\2\2\u017b\u017c\5\24\13\2\u017c\u017d\5(\25\2\u017d\r\3\2\2\2\u017e"+
		"\u0181\5\30\r\2\u017f\u0181\t\2\2\2\u0180\u017e\3\2\2\2\u0180\u017f\3"+
		"\2\2\2\u0181\17\3\2\2\2\u0182\u0183\5\22\n\2\u0183\u0184\5\u0148\u00a5"+
		"\2\u0184\u0186\3\2\2\2\u0185\u0182\3\2\2\2\u0185\u0186\3\2\2\2\u0186\21"+
		"\3\2\2\2\u0187\u018d\5\16\b\2\u0188\u0189\5\u0148\u00a5\2\u0189\u018a"+
		"\5\16\b\2\u018a\u018c\3\2\2\2\u018b\u0188\3\2\2\2\u018c\u018f\3\2\2\2"+
		"\u018d\u018b\3\2\2\2\u018d\u018e\3\2\2\2\u018e\23\3\2\2\2\u018f\u018d"+
		"\3\2\2\2\u0190\u0194\5\26\f\2\u0191\u0193\7\u008a\2\2\u0192\u0191\3\2"+
		"\2\2\u0193\u0196\3\2\2\2\u0194\u0192\3\2\2\2\u0194\u0195\3\2\2\2\u0195"+
		"\u0198\3\2\2\2\u0196\u0194\3\2\2\2\u0197\u0190\3\2\2\2\u0197\u0198\3\2"+
		"\2\2\u0198\25\3\2\2\2\u0199\u019f\5\30\r\2\u019a\u019b\5\u0148\u00a5\2"+
		"\u019b\u019c\5\30\r\2\u019c\u019e\3\2\2\2\u019d\u019a\3\2\2\2\u019e\u01a1"+
		"\3\2\2\2\u019f\u019d\3\2\2\2\u019f\u01a0\3\2\2\2\u01a0\27\3\2\2\2\u01a1"+
		"\u019f\3\2\2\2\u01a2\u01a5\5\u0090I\2\u01a3\u01a5\t\3\2\2\u01a4\u01a2"+
		"\3\2\2\2\u01a4\u01a3\3\2\2\2\u01a5\31\3\2\2\2\u01a6\u01a9\5\u0090I\2\u01a7"+
		"\u01a9\t\4\2\2\u01a8\u01a6\3\2\2\2\u01a8\u01a7\3\2\2\2\u01a9\33\3\2\2"+
		"\2\u01aa\u01ab\5\36\20\2\u01ab\u01ac\5\u0148\u00a5\2\u01ac\u01ae\3\2\2"+
		"\2\u01ad\u01aa\3\2\2\2\u01ad\u01ae\3\2\2\2\u01ae\35\3\2\2\2\u01af\u01b5"+
		"\5\32\16\2\u01b0\u01b1\5\u0148\u00a5\2\u01b1\u01b2\5\32\16\2\u01b2\u01b4"+
		"\3\2\2\2\u01b3\u01b0\3\2\2\2\u01b4\u01b7\3\2\2\2\u01b5\u01b3\3\2\2\2\u01b5"+
		"\u01b6\3\2\2\2\u01b6\37\3\2\2\2\u01b7\u01b5\3\2\2\2\u01b8\u01b9\7d\2\2"+
		"\u01b9\u01ba\5\u0148\u00a5\2\u01ba\u01c1\5\"\22\2\u01bb\u01bc\7`\2\2\u01bc"+
		"\u01bd\5\u0148\u00a5\2\u01bd\u01be\5\"\22\2\u01be\u01c0\3\2\2\2\u01bf"+
		"\u01bb\3\2\2\2\u01c0\u01c3\3\2\2\2\u01c1\u01bf\3\2\2\2\u01c1\u01c2\3\2"+
		"\2\2\u01c2\u01c4\3\2\2\2\u01c3\u01c1\3\2\2\2\u01c4\u01c5\5\u0148\u00a5"+
		"\2\u01c5\u01c6\7c\2\2\u01c6!\3\2\2\2\u01c7\u01c8\5\u008eH\2\u01c8\u01cd"+
		"\5\u013e\u00a0\2\u01c9\u01ca\7\35\2\2\u01ca\u01cb\5\u0148\u00a5\2\u01cb"+
		"\u01cc\5$\23\2\u01cc\u01ce\3\2\2\2\u01cd\u01c9\3\2\2\2\u01cd\u01ce\3\2"+
		"\2\2\u01ce#\3\2\2\2\u01cf\u01d6\5N(\2\u01d0\u01d1\7u\2\2\u01d1\u01d2\5"+
		"\u0148\u00a5\2\u01d2\u01d3\5N(\2\u01d3\u01d5\3\2\2\2\u01d4\u01d0\3\2\2"+
		"\2\u01d5\u01d8\3\2\2\2\u01d6\u01d4\3\2\2\2\u01d6\u01d7\3\2\2\2\u01d7%"+
		"\3\2\2\2\u01d8\u01d6\3\2\2\2\u01d9\u01e0\5N(\2\u01da\u01db\7`\2\2\u01db"+
		"\u01dc\5\u0148\u00a5\2\u01dc\u01dd\5N(\2\u01dd\u01df\3\2\2\2\u01de\u01da"+
		"\3\2\2\2\u01df\u01e2\3\2\2\2\u01e0\u01de\3\2\2\2\u01e0\u01e1\3\2\2\2\u01e1"+
		"\'\3\2\2\2\u01e2\u01e0\3\2\2\2\u01e3\u01e4\7\26\2\2\u01e4\u01f1\b\25\1"+
		"\2\u01e5\u01e6\7&\2\2\u01e6\u01f1\b\25\1\2\u01e7\u01e8\7\34\2\2\u01e8"+
		"\u01f1\b\25\1\2\u01e9\u01ea\7\u0087\2\2\u01ea\u01eb\7&\2\2\u01eb\u01f1"+
		"\b\25\1\2\u01ec\u01ed\7\f\2\2\u01ed\u01f1\b\25\1\2\u01ee\u01ef\7/\2\2"+
		"\u01ef\u01f1\b\25\1\2\u01f0\u01e3\3\2\2\2\u01f0\u01e5\3\2\2\2\u01f0\u01e7"+
		"\3\2\2\2\u01f0\u01e9\3\2\2\2\u01f0\u01ec\3\2\2\2\u01f0\u01ee\3\2\2\2\u01f1"+
		"\u01f2\3\2\2\2\u01f2\u01f6\5\u0140\u00a1\2\u01f3\u01f4\5\u0148\u00a5\2"+
		"\u01f4\u01f5\5 \21\2\u01f5\u01f7\3\2\2\2\u01f6\u01f3\3\2\2\2\u01f6\u01f7"+
		"\3\2\2\2\u01f7\u01fb\3\2\2\2\u01f8\u01f9\5\u0148\u00a5\2\u01f9\u01fa\5"+
		"`\61\2\u01fa\u01fc\3\2\2\2\u01fb\u01f8\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc"+
		"\u0202\3\2\2\2\u01fd\u01fe\5\u0148\u00a5\2\u01fe\u01ff\7\35\2\2\u01ff"+
		"\u0200\5\u0148\u00a5\2\u0200\u0201\5&\24\2\u0201\u0203\3\2\2\2\u0202\u01fd"+
		"\3\2\2\2\u0202\u0203\3\2\2\2\u0203\u0209\3\2\2\2\u0204\u0205\5\u0148\u00a5"+
		"\2\u0205\u0206\7#\2\2\u0206\u0207\5\u0148\u00a5\2\u0207\u0208\5&\24\2"+
		"\u0208\u020a\3\2\2\2\u0209\u0204\3\2\2\2\u0209\u020a\3\2\2\2\u020a\u0210"+
		"\3\2\2\2\u020b\u020c\5\u0148\u00a5\2\u020c\u020d\7+\2\2\u020d\u020e\5"+
		"\u0148\u00a5\2\u020e\u020f\5&\24\2\u020f\u0211\3\2\2\2\u0210\u020b\3\2"+
		"\2\2\u0210\u0211\3\2\2\2\u0211\u0212\3\2\2\2\u0212\u0213\5\u0148\u00a5"+
		"\2\u0213\u0214\5*\26\2\u0214)\3\2\2\2\u0215\u0216\7[\2\2\u0216\u0222\5"+
		"\u0148\u00a5\2\u0217\u0218\6\26\3\3\u0218\u021c\5,\27\2\u0219\u021a\5"+
		"\u0148\u00a5\2\u021a\u021b\7`\2\2\u021b\u021d\3\2\2\2\u021c\u0219\3\2"+
		"\2\2\u021c\u021d\3\2\2\2\u021d\u021f\3\2\2\2\u021e\u0220\5\u014a\u00a6"+
		"\2\u021f\u021e\3\2\2\2\u021f\u0220\3\2\2\2\u0220\u0223\3\2\2\2\u0221\u0223"+
		"\3\2\2\2\u0222\u0217\3\2\2\2\u0222\u0221\3\2\2\2\u0223\u022d\3\2\2\2\u0224"+
		"\u022a\5\60\31\2\u0225\u0226\5\u014a\u00a6\2\u0226\u0227\5\60\31\2\u0227"+
		"\u0229\3\2\2\2\u0228\u0225\3\2\2\2\u0229\u022c\3\2\2\2\u022a\u0228\3\2"+
		"\2\2\u022a\u022b\3\2\2\2\u022b\u022e\3\2\2\2\u022c\u022a\3\2\2\2\u022d"+
		"\u0224\3\2\2\2\u022d\u022e\3\2\2\2\u022e\u0230\3\2\2\2\u022f\u0231\5\u014a"+
		"\u00a6\2\u0230\u022f\3\2\2\2\u0230\u0231\3\2\2\2\u0231\u0232\3\2\2\2\u0232"+
		"\u0233\7\\\2\2\u0233+\3\2\2\2\u0234\u023c\5.\30\2\u0235\u0236\5\u0148"+
		"\u00a5\2\u0236\u0237\7`\2\2\u0237\u0238\5\u0148\u00a5\2\u0238\u0239\5"+
		".\30\2\u0239\u023b\3\2\2\2\u023a\u0235\3\2\2\2\u023b\u023e\3\2\2\2\u023c"+
		"\u023a\3\2\2\2\u023c\u023d\3\2\2\2\u023d-\3\2\2\2\u023e\u023c\3\2\2\2"+
		"\u023f\u0240\5\u008eH\2\u0240\u0242\5\u0140\u00a1\2\u0241\u0243\5\u0130"+
		"\u0099\2\u0242\u0241\3\2\2\2\u0242\u0243\3\2\2\2\u0243\u0245\3\2\2\2\u0244"+
		"\u0246\5\u0128\u0095\2\u0245\u0244\3\2\2\2\u0245\u0246\3\2\2\2\u0246/"+
		"\3\2\2\2\u0247\u0248\7\62\2\2\u0248\u024a\5\u0148\u00a5\2\u0249\u0247"+
		"\3\2\2\2\u0249\u024a\3\2\2\2\u024a\u024b\3\2\2\2\u024b\u024e\5\u00a0Q"+
		"\2\u024c\u024e\5\62\32\2\u024d\u0249\3\2\2\2\u024d\u024c\3\2\2\2\u024e"+
		"\61\3\2\2\2\u024f\u0257\5\64\33\2\u0250\u0257\5<\37\2\u0251\u0254\5\20"+
		"\t\2\u0252\u0255\5(\25\2\u0253\u0255\5\66\34\2\u0254\u0252\3\2\2\2\u0254"+
		"\u0253\3\2\2\2\u0255\u0257\3\2\2\2\u0256\u024f\3\2\2\2\u0256\u0250\3\2"+
		"\2\2\u0256\u0251\3\2\2\2\u0257\63\3\2\2\2\u0258\u025a\5\20\t\2\u0259\u025b"+
		"\5 \21\2\u025a\u0259\3\2\2\2\u025a\u025b\3\2\2\2\u025b\u025f\3\2\2\2\u025c"+
		"\u025d\5:\36\2\u025d\u025e\5\u0148\u00a5\2\u025e\u0260\3\2\2\2\u025f\u025c"+
		"\3\2\2\2\u025f\u0260\3\2\2\2\u0260\u0261\3\2\2\2\u0261\u0262\58\35\2\u0262"+
		"\u0273\5`\61\2\u0263\u0264\7\31\2\2\u0264\u0265\5\u0148\u00a5\2\u0265"+
		"\u0266\5\u009cO\2\u0266\u0274\3\2\2\2\u0267\u0268\5\u0148\u00a5\2\u0268"+
		"\u0269\79\2\2\u0269\u026a\5\u0148\u00a5\2\u026a\u026b\5^\60\2\u026b\u026d"+
		"\3\2\2\2\u026c\u0267\3\2\2\2\u026c\u026d\3\2\2\2\u026d\u0271\3\2\2\2\u026e"+
		"\u026f\5\u0148\u00a5\2\u026f\u0270\5h\65\2\u0270\u0272\3\2\2\2\u0271\u026e"+
		"\3\2\2\2\u0271\u0272\3\2\2\2\u0272\u0274\3\2\2\2\u0273\u0263\3\2\2\2\u0273"+
		"\u026c\3\2\2\2\u0273\u0274\3\2\2\2\u0274\65\3\2\2\2\u0275\u0276\58\35"+
		"\2\u0276\u0277\5\u0148\u00a5\2\u0277\u0278\5h\65\2\u0278\67\3\2\2\2\u0279"+
		"\u027c\5\u0140\u00a1\2\u027a\u027c\5\u013c\u009f\2\u027b\u0279\3\2\2\2"+
		"\u027b\u027a\3\2\2\2\u027c9\3\2\2\2\u027d\u0280\5L\'\2\u027e\u0280\7<"+
		"\2\2\u027f\u027d\3\2\2\2\u027f\u027e\3\2\2\2\u0280;\3\2\2\2\u0281\u0282"+
		"\5\u00a6T\2\u0282=\3\2\2\2\u0283\u028a\5@!\2\u0284\u0285\7`\2\2\u0285"+
		"\u0286\5\u0148\u00a5\2\u0286\u0287\5@!\2\u0287\u0289\3\2\2\2\u0288\u0284"+
		"\3\2\2\2\u0289\u028c\3\2\2\2\u028a\u0288\3\2\2\2\u028a\u028b\3\2\2\2\u028b"+
		"?\3\2\2\2\u028c\u028a\3\2\2\2\u028d\u0293\5B\"\2\u028e\u028f\5\u0148\u00a5"+
		"\2\u028f\u0290\7b\2\2\u0290\u0291\5\u0148\u00a5\2\u0291\u0292\5D#\2\u0292"+
		"\u0294\3\2\2\2\u0293\u028e\3\2\2\2\u0293\u0294\3\2\2\2\u0294A\3\2\2\2"+
		"\u0295\u0296\5\u0140\u00a1\2\u0296C\3\2\2\2\u0297\u0298\5\u00e6t\2\u0298"+
		"E\3\2\2\2\u0299\u02a1\5D#\2\u029a\u029b\5\u0148\u00a5\2\u029b\u029c\7"+
		"`\2\2\u029c\u029d\5\u0148\u00a5\2\u029d\u029e\5D#\2\u029e\u02a0\3\2\2"+
		"\2\u029f\u029a\3\2\2\2\u02a0\u02a3\3\2\2\2\u02a1\u029f\3\2\2\2\u02a1\u02a2"+
		"\3\2\2\2\u02a2\u02a4\3\2\2\2\u02a3\u02a1\3\2\2\2\u02a4\u02a6\5\u0148\u00a5"+
		"\2\u02a5\u02a7\7`\2\2\u02a6\u02a5\3\2\2\2\u02a6\u02a7\3\2\2\2\u02a7G\3"+
		"\2\2\2\u02a8\u02a9\5\u008eH\2\u02a9\u02aa\7]\2\2\u02aa\u02ab\7^\2\2\u02ab"+
		"\u02ad\3\2\2\2\u02ac\u02a8\3\2\2\2\u02ad\u02ae\3\2\2\2\u02ae\u02ac\3\2"+
		"\2\2\u02ae\u02af\3\2\2\2\u02afI\3\2\2\2\u02b0\u02b2\5H%\2\u02b1\u02b0"+
		"\3\2\2\2\u02b1\u02b2\3\2\2\2\u02b2K\3\2\2\2\u02b3\u02b6\5\u008eH\2\u02b4"+
		"\u02b7\5V,\2\u02b5\u02b7\5T+\2\u02b6\u02b4\3\2\2\2\u02b6\u02b5\3\2\2\2"+
		"\u02b7\u02b8\3\2\2\2\u02b8\u02b9\5J&\2\u02b9M\3\2\2\2\u02ba\u02c0\5\u008e"+
		"H\2\u02bb\u02be\5V,\2\u02bc\u02be\7<\2\2\u02bd\u02bb\3\2\2\2\u02bd\u02bc"+
		"\3\2\2\2\u02be\u02c1\3\2\2\2\u02bf\u02c1\5R*\2\u02c0\u02bd\3\2\2\2\u02c0"+
		"\u02bf\3\2\2\2\u02c1\u02c2\3\2\2\2\u02c2\u02c3\5J&\2\u02c3O\3\2\2\2\u02c4"+
		"\u02c7\5p9\2\u02c5\u02c7\5r:\2\u02c6\u02c4\3\2\2\2\u02c6\u02c5\3\2\2\2"+
		"\u02c7\u02c9\3\2\2\2\u02c8\u02ca\5X-\2\u02c9\u02c8\3\2\2\2\u02c9\u02ca"+
		"\3\2\2\2\u02caQ\3\2\2\2\u02cb\u02cd\5p9\2\u02cc\u02ce\5X-\2\u02cd\u02cc"+
		"\3\2\2\2\u02cd\u02ce\3\2\2\2\u02ceS\3\2\2\2\u02cf\u02d1\5r:\2\u02d0\u02d2"+
		"\5X-\2\u02d1\u02d0\3\2\2\2\u02d1\u02d2\3\2\2\2\u02d2U\3\2\2\2\u02d3\u02d4"+
		"\7\17\2\2\u02d4W\3\2\2\2\u02d5\u02d6\7d\2\2\u02d6\u02d7\5\u0148\u00a5"+
		"\2\u02d7\u02de\5Z.\2\u02d8\u02d9\7`\2\2\u02d9\u02da\5\u0148\u00a5\2\u02da"+
		"\u02db\5Z.\2\u02db\u02dd\3\2\2\2\u02dc\u02d8\3\2\2\2\u02dd\u02e0\3\2\2"+
		"\2\u02de\u02dc\3\2\2\2\u02de\u02df\3\2\2\2\u02df\u02e1\3\2\2\2\u02e0\u02de"+
		"\3\2\2\2\u02e1\u02e2\5\u0148\u00a5\2\u02e2\u02e3\7c\2\2\u02e3Y\3\2\2\2"+
		"\u02e4\u02ee\5N(\2\u02e5\u02e6\5\u008eH\2\u02e6\u02eb\7g\2\2\u02e7\u02e8"+
		"\t\5\2\2\u02e8\u02e9\5\u0148\u00a5\2\u02e9\u02ea\5N(\2\u02ea\u02ec\3\2"+
		"\2\2\u02eb\u02e7\3\2\2\2\u02eb\u02ec\3\2\2\2\u02ec\u02ee\3\2\2\2\u02ed"+
		"\u02e4\3\2\2\2\u02ed\u02e5\3\2\2\2\u02ee[\3\2\2\2\u02ef\u02f0\5\u008e"+
		"H\2\u02f0\u02f1\5p9\2\u02f1]\3\2\2\2\u02f2\u02f9\5\\/\2\u02f3\u02f4\7"+
		"`\2\2\u02f4\u02f5\5\u0148\u00a5\2\u02f5\u02f6\5\\/\2\u02f6\u02f8\3\2\2"+
		"\2\u02f7\u02f3\3\2\2\2\u02f8\u02fb\3\2\2\2\u02f9\u02f7\3\2\2\2\u02f9\u02fa"+
		"\3\2\2\2\u02fa_\3\2\2\2\u02fb\u02f9\3\2\2\2\u02fc\u02fe\7Y\2\2\u02fd\u02ff"+
		"\5b\62\2\u02fe\u02fd\3\2\2\2\u02fe\u02ff\3\2\2\2\u02ff\u0300\3\2\2\2\u0300"+
		"\u0301\5\u0146\u00a4\2\u0301a\3\2\2\2\u0302\u0305\5f\64\2\u0303\u0305"+
		"\5d\63\2\u0304\u0302\3\2\2\2\u0304\u0303\3\2\2\2\u0305\u030c\3\2\2\2\u0306"+
		"\u0307\7`\2\2\u0307\u0308\5\u0148\u00a5\2\u0308\u0309\5f\64\2\u0309\u030b"+
		"\3\2\2\2\u030a\u0306\3\2\2\2\u030b\u030e\3\2\2\2\u030c\u030a\3\2\2\2\u030c"+
		"\u030d\3\2\2\2\u030dc\3\2\2\2\u030e\u030c\3\2\2\2\u030f\u0310\5N(\2\u0310"+
		"\u0311\7\67\2\2\u0311e\3\2\2\2\u0312\u0314\5\34\17\2\u0313\u0315\5N(\2"+
		"\u0314\u0313\3\2\2\2\u0314\u0315\3\2\2\2\u0315\u0317\3\2\2\2\u0316\u0318"+
		"\7\u0088\2\2\u0317\u0316\3\2\2\2\u0317\u0318\3\2\2\2\u0318\u0319\3\2\2"+
		"\2\u0319\u031f\5B\"\2\u031a\u031b\5\u0148\u00a5\2\u031b\u031c\7b\2\2\u031c"+
		"\u031d\5\u0148\u00a5\2\u031d\u031e\5\u00f2z\2\u031e\u0320\3\2\2\2\u031f"+
		"\u031a\3\2\2\2\u031f\u0320\3\2\2\2\u0320g\3\2\2\2\u0321\u0322\5\u00a0"+
		"Q\2\u0322i\3\2\2\2\u0323\u0328\5l\67\2\u0324\u0325\7a\2\2\u0325\u0327"+
		"\5l\67\2\u0326\u0324\3\2\2\2\u0327\u032a\3\2\2\2\u0328\u0326\3\2\2\2\u0328"+
		"\u0329\3\2\2\2\u0329k\3\2\2\2\u032a\u0328\3\2\2\2\u032b\u0331\5\u0140"+
		"\u00a1\2\u032c\u0331\7\n\2\2\u032d\u0331\7\13\2\2\u032e\u0331\7\t\2\2"+
		"\u032f\u0331\7\f\2\2\u0330\u032b\3\2\2\2\u0330\u032c\3\2\2\2\u0330\u032d"+
		"\3\2\2\2\u0330\u032e\3\2\2\2\u0330\u032f\3\2\2\2\u0331m\3\2\2\2\u0332"+
		"\u0333\5l\67\2\u0333\u0334\7a\2\2\u0334\u0336\3\2\2\2\u0335\u0332\3\2"+
		"\2\2\u0336\u0339\3\2\2\2\u0337\u0335\3\2\2\2\u0337\u0338\3\2\2\2\u0338"+
		"o\3\2\2\2\u0339\u0337\3\2\2\2\u033a\u033b\5n8\2\u033b\u033c\5\u0140\u00a1"+
		"\2\u033cq\3\2\2\2\u033d\u033e\5n8\2\u033e\u0343\5\u013e\u00a0\2\u033f"+
		"\u0340\7a\2\2\u0340\u0342\5\u013e\u00a0\2\u0341\u033f\3\2\2\2\u0342\u0345"+
		"\3\2\2\2\u0343\u0341\3\2\2\2\u0343\u0344\3\2\2\2\u0344s\3\2\2\2\u0345"+
		"\u0343\3\2\2\2\u0346\u034c\7?\2\2\u0347\u034c\7@\2\2\u0348\u034c\5\u013c"+
		"\u009f\2\u0349\u034c\7A\2\2\u034a\u034c\7B\2\2\u034b\u0346\3\2\2\2\u034b"+
		"\u0347\3\2\2\2\u034b\u0348\3\2\2\2\u034b\u0349\3\2\2\2\u034b\u034a\3\2"+
		"\2\2\u034cu\3\2\2\2\u034d\u034e\7\4\2\2\u034e\u0353\5x=\2\u034f\u0350"+
		"\7\6\2\2\u0350\u0352\5x=\2\u0351\u034f\3\2\2\2\u0352\u0355\3\2\2\2\u0353"+
		"\u0351\3\2\2\2\u0353\u0354\3\2\2\2\u0354\u0356\3\2\2\2\u0355\u0353\3\2"+
		"\2\2\u0356\u0357\7\5\2\2\u0357w\3\2\2\2\u0358\u035b\5z>\2\u0359\u035b"+
		"\5\u0086D\2\u035a\u0358\3\2\2\2\u035a\u0359\3\2\2\2\u035by\3\2\2\2\u035c"+
		"\u0360\5\u0140\u00a1\2\u035d\u035f\7\7\2\2\u035e\u035d\3\2\2\2\u035f\u0362"+
		"\3\2\2\2\u0360\u035e\3\2\2\2\u0360\u0361\3\2\2\2\u0361{\3\2\2\2\u0362"+
		"\u0360\3\2\2\2\u0363\u0364\5\u0080A\2\u0364\u0365\5\u0148\u00a5\2\u0365"+
		"\u0366\7V\2\2\u0366\u0367\5\u0148\u00a5\2\u0367\u0368\5\u0084C\2\u0368"+
		"}\3\2\2\2\u0369\u036a\5\u0082B\2\u036a\u036b\5\u0148\u00a5\2\u036b\u036c"+
		"\7V\2\2\u036c\u036d\5\u0148\u00a5\2\u036d\u036e\5\u0084C\2\u036e\177\3"+
		"\2\2\2\u036f\u0370\5`\61\2\u0370\u0081\3\2\2\2\u0371\u0374\5`\61\2\u0372"+
		"\u0374\5B\"\2\u0373\u0371\3\2\2\2\u0373\u0372\3\2\2\2\u0374\u0083\3\2"+
		"\2\2\u0375\u0378\5\u00a0Q\2\u0376\u0378\5\u00e8u\2\u0377\u0375\3\2\2\2"+
		"\u0377\u0376\3\2\2\2\u0378\u0085\3\2\2\2\u0379\u0382\7[\2\2\u037a\u037e"+
		"\5\u0148\u00a5\2\u037b\u037c\5b\62\2\u037c\u037d\5\u0148\u00a5\2\u037d"+
		"\u037f\3\2\2\2\u037e\u037b\3\2\2\2\u037e\u037f\3\2\2\2\u037f\u0380\3\2"+
		"\2\2\u0380\u0381\7V\2\2\u0381\u0383\3\2\2\2\u0382\u037a\3\2\2\2\u0382"+
		"\u0383\3\2\2\2\u0383\u0385\3\2\2\2\u0384\u0386\5\u014a\u00a6\2\u0385\u0384"+
		"\3\2\2\2\u0385\u0386\3\2\2\2\u0386\u0387\3\2\2\2\u0387\u0388\5\u008aF"+
		"\2\u0388\u0389\7\\\2\2\u0389\u0087\3\2\2\2\u038a\u038d\5\u0086D\2\u038b"+
		"\u038d\5|?\2\u038c\u038a\3\2\2\2\u038c\u038b\3\2\2\2\u038d\u0089\3\2\2"+
		"\2\u038e\u0390\5\u008cG\2\u038f\u038e\3\2\2\2\u038f\u0390\3\2\2\2\u0390"+
		"\u008b\3\2\2\2\u0391\u0397\5\u00a2R\2\u0392\u0393\5\u014a\u00a6\2\u0393"+
		"\u0394\5\u00a2R\2\u0394\u0396\3\2\2\2\u0395\u0392\3\2\2\2\u0396\u0399"+
		"\3\2\2\2\u0397\u0395\3\2\2\2\u0397\u0398\3\2\2\2\u0398\u039b\3\2\2\2\u0399"+
		"\u0397\3\2\2\2\u039a\u039c\5\u014a\u00a6\2\u039b\u039a\3\2\2\2\u039b\u039c"+
		"\3\2\2\2\u039c\u008d\3\2\2\2\u039d\u03a3\5\u0090I\2\u039e\u039f\5\u0148"+
		"\u00a5\2\u039f\u03a0\5\u0090I\2\u03a0\u03a2\3\2\2\2\u03a1\u039e\3\2\2"+
		"\2\u03a2\u03a5\3\2\2\2\u03a3\u03a1\3\2\2\2\u03a3\u03a4\3\2\2\2\u03a4\u03a6"+
		"\3\2\2\2\u03a5\u03a3\3\2\2\2\u03a6\u03a7\5\u0148\u00a5\2\u03a7\u03a9\3"+
		"\2\2\2\u03a8\u039d\3\2\2\2\u03a8\u03a9\3\2\2\2\u03a9\u008f\3\2\2\2\u03aa"+
		"\u03ab\7\u0087\2\2\u03ab\u03b3\5\u0094K\2\u03ac\u03ad\5\u0148\u00a5\2"+
		"\u03ad\u03af\7Y\2\2\u03ae\u03b0\5\u0092J\2\u03af\u03ae\3\2\2\2\u03af\u03b0"+
		"\3\2\2\2\u03b0\u03b1\3\2\2\2\u03b1\u03b2\5\u0146\u00a4\2\u03b2\u03b4\3"+
		"\2\2\2\u03b3\u03ac\3\2\2\2\u03b3\u03b4\3\2\2\2\u03b4\u0091\3\2\2\2\u03b5"+
		"\u03b8\5\u0096L\2\u03b6\u03b8\5\u009cO\2\u03b7\u03b5\3\2\2\2\u03b7\u03b6"+
		"\3\2\2\2\u03b8\u0093\3\2\2\2\u03b9\u03ba\5p9\2\u03ba\u0095\3\2\2\2\u03bb"+
		"\u03c0\5\u0098M\2\u03bc\u03bd\7`\2\2\u03bd\u03bf\5\u0098M\2\u03be\u03bc"+
		"\3\2\2\2\u03bf\u03c2\3\2\2\2\u03c0\u03be\3\2\2\2\u03c0\u03c1\3\2\2\2\u03c1"+
		"\u0097\3\2\2\2\u03c2\u03c0\3\2\2\2\u03c3\u03c4\5\u009aN\2\u03c4\u03c5"+
		"\5\u0148\u00a5\2\u03c5\u03c6\7b\2\2\u03c6\u03c7\5\u0148\u00a5\2\u03c7"+
		"\u03c8\5\u009cO\2\u03c8\u0099\3\2\2\2\u03c9\u03cc\5\u0140\u00a1\2\u03ca"+
		"\u03cc\5\u0144\u00a3\2\u03cb\u03c9\3\2\2\2\u03cb\u03ca\3\2\2\2\u03cc\u009b"+
		"\3\2\2\2\u03cd\u03d1\5\u009eP\2\u03ce\u03d1\5\u0090I\2\u03cf\u03d1\5\u00f2"+
		"z\2\u03d0\u03cd\3\2\2\2\u03d0\u03ce\3\2\2\2\u03d0\u03cf\3\2\2\2\u03d1"+
		"\u009d\3\2\2\2\u03d2\u03de\7]\2\2\u03d3\u03d8\5\u009cO\2\u03d4\u03d5\7"+
		"`\2\2\u03d5\u03d7\5\u009cO\2\u03d6\u03d4\3\2\2\2\u03d7\u03da\3\2\2\2\u03d8"+
		"\u03d6\3\2\2\2\u03d8\u03d9\3\2\2\2\u03d9\u03dc\3\2\2\2\u03da\u03d8\3\2"+
		"\2\2\u03db\u03dd\7`\2\2\u03dc\u03db\3\2\2\2\u03dc\u03dd\3\2\2\2\u03dd"+
		"\u03df\3\2\2\2\u03de\u03d3\3\2\2\2\u03de\u03df\3\2\2\2\u03df\u03e0\3\2"+
		"\2\2\u03e0\u03e1\7^\2\2\u03e1\u009f\3\2\2\2\u03e2\u03e4\7[\2\2\u03e3\u03e5"+
		"\5\u014a\u00a6\2\u03e4\u03e3\3\2\2\2\u03e4\u03e5\3\2\2\2\u03e5\u03e6\3"+
		"\2\2\2\u03e6\u03e7\5\u008aF\2\u03e7\u03e8\7\\\2\2\u03e8\u00a1\3\2\2\2"+
		"\u03e9\u03ec\5\u00a4S\2\u03ea\u03ec\5\u00c0a\2\u03eb\u03e9\3\2\2\2\u03eb"+
		"\u03ea\3\2\2\2\u03ec\u00a3\3\2\2\2\u03ed\u03ee\6S\4\2\u03ee\u03ef\5\u00a6"+
		"T\2\u03ef\u00a5\3\2\2\2\u03f0\u03f1\5\22\n\2\u03f1\u03fc\5\u0148\u00a5"+
		"\2\u03f2\u03f4\5N(\2\u03f3\u03f2\3\2\2\2\u03f3\u03f4\3\2\2\2\u03f4\u03f5"+
		"\3\2\2\2\u03f5\u03fd\5> \2\u03f6\u03f7\5\u00a8U\2\u03f7\u03f8\5\u0148"+
		"\u00a5\2\u03f8\u03f9\7b\2\2\u03f9\u03fa\5\u0148\u00a5\2\u03fa\u03fb\5"+
		"D#\2\u03fb\u03fd\3\2\2\2\u03fc\u03f3\3\2\2\2\u03fc\u03f6\3\2\2\2\u03fd"+
		"\u0402\3\2\2\2\u03fe\u03ff\5N(\2\u03ff\u0400\5> \2\u0400\u0402\3\2\2\2"+
		"\u0401\u03f0\3\2\2\2\u0401\u03fe\3\2\2\2\u0402\u00a7\3\2\2\2\u0403\u0404"+
		"\7Y\2\2\u0404\u0409\5\u00aaV\2\u0405\u0406\7`\2\2\u0406\u0408\5\u00aa"+
		"V\2\u0407\u0405\3\2\2\2\u0408\u040b\3\2\2\2\u0409\u0407\3\2\2\2\u0409"+
		"\u040a\3\2\2\2\u040a\u040c\3\2\2\2\u040b\u0409\3\2\2\2\u040c\u040d\5\u0146"+
		"\u00a4\2\u040d\u00a9\3\2\2\2\u040e\u0410\5N(\2\u040f\u040e\3\2\2\2\u040f"+
		"\u0410\3\2\2\2\u0410\u0411\3\2\2\2\u0411\u0412\5B\"\2\u0412\u00ab\3\2"+
		"\2\2\u0413\u0414\7Y\2\2\u0414\u0417\5B\"\2\u0415\u0416\7`\2\2\u0416\u0418"+
		"\5B\"\2\u0417\u0415\3\2\2\2\u0418\u0419\3\2\2\2\u0419\u0417\3\2\2\2\u0419"+
		"\u041a\3\2\2\2\u041a\u041b\3\2\2\2\u041b\u041c\5\u0146\u00a4\2\u041c\u00ad"+
		"\3\2\2\2\u041d\u0420\5\u00b0Y\2\u041e\u0420\5\u00b2Z\2\u041f\u041d\3\2"+
		"\2\2\u041f\u041e\3\2\2\2\u0420\u00af\3\2\2\2\u0421\u0422\7!\2\2\u0422"+
		"\u0423\5\u00e0q\2\u0423\u0424\5\u0148\u00a5\2\u0424\u042d\5\u00c0a\2\u0425"+
		"\u0428\5\u0148\u00a5\2\u0426\u0428\5\u014a\u00a6\2\u0427\u0425\3\2\2\2"+
		"\u0427\u0426\3\2\2\2\u0428\u0429\3\2\2\2\u0429\u042a\7\33\2\2\u042a\u042b"+
		"\5\u0148\u00a5\2\u042b\u042c\5\u00c0a\2\u042c\u042e\3\2\2\2\u042d\u0427"+
		"\3\2\2\2\u042d\u042e\3\2\2\2\u042e\u00b1\3\2\2\2\u042f\u0430\7\65\2\2"+
		"\u0430\u0431\5\u00e0q\2\u0431\u0432\5\u0148\u00a5\2\u0432\u0433\7[\2\2"+
		"\u0433\u043b\5\u0148\u00a5\2\u0434\u0436\5\u00ceh\2\u0435\u0434\3\2\2"+
		"\2\u0436\u0437\3\2\2\2\u0437\u0435\3\2\2\2\u0437\u0438\3\2\2\2\u0438\u0439"+
		"\3\2\2\2\u0439\u043a\5\u0148\u00a5\2\u043a\u043c\3\2\2\2\u043b\u0435\3"+
		"\2\2\2\u043b\u043c\3\2\2\2\u043c\u043d\3\2\2\2\u043d\u043e\7\\\2\2\u043e"+
		"\u00b3\3\2\2\2\u043f\u0440\7 \2\2\u0440\u0441\7Y\2\2\u0441\u0442\5\u00d2"+
		"j\2\u0442\u0443\5\u0146\u00a4\2\u0443\u0444\5\u0148\u00a5\2\u0444\u0445"+
		"\5\u00c0a\2\u0445\u0453\3\2\2\2\u0446\u0447\7>\2\2\u0447\u0448\5\u00e0"+
		"q\2\u0448\u0449\5\u0148\u00a5\2\u0449\u044a\5\u00c0a\2\u044a\u0453\3\2"+
		"\2\2\u044b\u044c\7\32\2\2\u044c\u044d\5\u0148\u00a5\2\u044d\u044e\5\u00c0"+
		"a\2\u044e\u044f\5\u0148\u00a5\2\u044f\u0450\7>\2\2\u0450\u0451\5\u00e0"+
		"q\2\u0451\u0453\3\2\2\2\u0452\u043f\3\2\2\2\u0452\u0446\3\2\2\2\u0452"+
		"\u044b\3\2\2\2\u0453\u00b5\3\2\2\2\u0454\u0456\7\30\2\2\u0455\u0457\5"+
		"\u0140\u00a1\2\u0456\u0455\3\2\2\2\u0456\u0457\3\2\2\2\u0457\u00b7\3\2"+
		"\2\2\u0458\u045a\7\22\2\2\u0459\u045b\5\u0140\u00a1\2\u045a\u0459\3\2"+
		"\2\2\u045a\u045b\3\2\2\2\u045b\u00b9\3\2\2\2\u045c\u045d\7\23\2\2\u045d"+
		"\u045e\5\u00f2z\2\u045e\u00bb\3\2\2\2\u045f\u0461\7;\2\2\u0460\u0462\5"+
		"\u00c8e\2\u0461\u0460\3\2\2\2\u0461\u0462\3\2\2\2\u0462\u0463\3\2\2\2"+
		"\u0463\u0464\5\u0148\u00a5\2\u0464\u046a\5\u00a0Q\2\u0465\u0466\5\u0148"+
		"\u00a5\2\u0466\u0467\5\u00c2b\2\u0467\u0469\3\2\2\2\u0468\u0465\3\2\2"+
		"\2\u0469\u046c\3\2\2\2\u046a\u0468\3\2\2\2\u046a\u046b\3\2\2\2\u046b\u0470"+
		"\3\2\2\2\u046c\u046a\3\2\2\2\u046d\u046e\5\u0148\u00a5\2\u046e\u046f\5"+
		"\u00c6d\2\u046f\u0471\3\2\2\2\u0470\u046d\3\2\2\2\u0470\u0471\3\2\2\2"+
		"\u0471\u00bd\3\2\2\2\u0472\u0473\7\21\2\2\u0473\u0479\5\u00f2z\2\u0474"+
		"\u0475\5\u0148\u00a5\2\u0475\u0476\t\6\2\2\u0476\u0477\5\u0148\u00a5\2"+
		"\u0477\u0478\5\u00f2z\2\u0478\u047a\3\2\2\2\u0479\u0474\3\2\2\2\u0479"+
		"\u047a\3\2\2\2\u047a\u00bf\3\2\2\2\u047b\u0498\5\u00a0Q\2\u047c\u0498"+
		"\5\u00aeX\2\u047d\u0498\5\u00b4[\2\u047e\u0498\5\u00bc_\2\u047f\u0480"+
		"\7\66\2\2\u0480\u0481\5\u00e0q\2\u0481\u0482\5\u0148\u00a5\2\u0482\u0483"+
		"\5\u00a0Q\2\u0483\u0498\3\2\2\2\u0484\u0486\7\60\2\2\u0485\u0487\5\u00f2"+
		"z\2\u0486\u0485\3\2\2\2\u0486\u0487\3\2\2\2\u0487\u0498\3\2\2\2\u0488"+
		"\u0489\78\2\2\u0489\u0498\5\u00f2z\2\u048a\u0498\5\u00b8]\2\u048b\u0498"+
		"\5\u00b6\\\2\u048c\u048d\6a\5\2\u048d\u0498\5\u00ba^\2\u048e\u048f\5\u0140"+
		"\u00a1\2\u048f\u0490\7h\2\2\u0490\u0491\5\u0148\u00a5\2\u0491\u0492\5"+
		"\u00c0a\2\u0492\u0498\3\2\2\2\u0493\u0498\5\u00be`\2\u0494\u0498\5\u00a4"+
		"S\2\u0495\u0498\5\u00e8u\2\u0496\u0498\7_\2\2\u0497\u047b\3\2\2\2\u0497"+
		"\u047c\3\2\2\2\u0497\u047d\3\2\2\2\u0497\u047e\3\2\2\2\u0497\u047f\3\2"+
		"\2\2\u0497\u0484\3\2\2\2\u0497\u0488\3\2\2\2\u0497\u048a\3\2\2\2\u0497"+
		"\u048b\3\2\2\2\u0497\u048c\3\2\2\2\u0497\u048e\3\2\2\2\u0497\u0493\3\2"+
		"\2\2\u0497\u0494\3\2\2\2\u0497\u0495\3\2\2\2\u0497\u0496\3\2\2\2\u0498"+
		"\u00c1\3\2\2\2\u0499\u049a\7\25\2\2\u049a\u049b\7Y\2\2\u049b\u049d\5\34"+
		"\17\2\u049c\u049e\5\u00c4c\2\u049d\u049c\3\2\2\2\u049d\u049e\3\2\2\2\u049e"+
		"\u049f\3\2\2\2\u049f\u04a0\5\u0140\u00a1\2\u04a0\u04a1\5\u0146\u00a4\2"+
		"\u04a1\u04a2\5\u0148\u00a5\2\u04a2\u04a3\5\u00a0Q\2\u04a3\u00c3\3\2\2"+
		"\2\u04a4\u04a9\5p9\2\u04a5\u04a6\7v\2\2\u04a6\u04a8\5p9\2\u04a7\u04a5"+
		"\3\2\2\2\u04a8\u04ab\3\2\2\2\u04a9\u04a7\3\2\2\2\u04a9\u04aa\3\2\2\2\u04aa"+
		"\u00c5\3\2\2\2\u04ab\u04a9\3\2\2\2\u04ac\u04ad\7\37\2\2\u04ad\u04ae\5"+
		"\u0148\u00a5\2\u04ae\u04af\5\u00a0Q\2\u04af\u00c7\3\2\2\2\u04b0\u04b1"+
		"\7Y\2\2\u04b1\u04b2\5\u0148\u00a5\2\u04b2\u04b4\5\u00caf\2\u04b3\u04b5"+
		"\5\u014a\u00a6\2\u04b4\u04b3\3\2\2\2\u04b4\u04b5\3\2\2\2\u04b5\u04b6\3"+
		"\2\2\2\u04b6\u04b7\5\u0146\u00a4\2\u04b7\u00c9\3\2\2\2\u04b8\u04be\5\u00cc"+
		"g\2\u04b9\u04ba\5\u014a\u00a6\2\u04ba\u04bb\5\u00ccg\2\u04bb\u04bd\3\2"+
		"\2\2\u04bc\u04b9\3\2\2\2\u04bd\u04c0\3\2\2\2\u04be\u04bc\3\2\2\2\u04be"+
		"\u04bf\3\2\2\2\u04bf\u00cb\3\2\2\2\u04c0\u04be\3\2\2\2\u04c1\u04c4\5\u00a4"+
		"S\2\u04c2\u04c4\5\u00f2z\2\u04c3\u04c1\3\2\2\2\u04c3\u04c2\3\2\2\2\u04c4"+
		"\u00cd\3\2\2\2\u04c5\u04cb\5\u00d0i\2\u04c6\u04c7\5\u0148\u00a5\2\u04c7"+
		"\u04c8\5\u00d0i\2\u04c8\u04ca\3\2\2\2\u04c9\u04c6\3\2\2\2\u04ca\u04cd"+
		"\3\2\2\2\u04cb\u04c9\3\2\2\2\u04cb\u04cc\3\2\2\2\u04cc\u04ce\3\2\2\2\u04cd"+
		"\u04cb\3\2\2\2\u04ce\u04cf\5\u0148\u00a5\2\u04cf\u04d0\5\u008cG\2\u04d0"+
		"\u00cf\3\2\2\2\u04d1\u04d2\7\24\2\2\u04d2\u04d3\5\u00f2z\2\u04d3\u04d4"+
		"\7h\2\2\u04d4\u04d8\3\2\2\2\u04d5\u04d6\7\31\2\2\u04d6\u04d8\7h\2\2\u04d7"+
		"\u04d1\3\2\2\2\u04d7\u04d5\3\2\2\2\u04d8\u00d1\3\2\2\2\u04d9\u04dc\5\u00d4"+
		"k\2\u04da\u04dc\5\u00d6l\2\u04db\u04d9\3\2\2\2\u04db\u04da\3\2\2\2\u04dc"+
		"\u00d3\3\2\2\2\u04dd\u04df\5\34\17\2\u04de\u04e0\5N(\2\u04df\u04de\3\2"+
		"\2\2\u04df\u04e0\3\2\2\2\u04e0\u04e1\3\2\2\2\u04e1\u04e2\5B\"\2\u04e2"+
		"\u04e3\t\7\2\2\u04e3\u04e4\5\u00f2z\2\u04e4\u00d5\3\2\2\2\u04e5\u04e7"+
		"\5\u00d8m\2\u04e6\u04e5\3\2\2\2\u04e6\u04e7\3\2\2\2\u04e7\u04e8\3\2\2"+
		"\2\u04e8\u04ea\7_\2\2\u04e9\u04eb\5\u00f2z\2\u04ea\u04e9\3\2\2\2\u04ea"+
		"\u04eb\3\2\2\2\u04eb\u04ec\3\2\2\2\u04ec\u04ee\7_\2\2\u04ed\u04ef\5\u00da"+
		"n\2\u04ee\u04ed\3\2\2\2\u04ee\u04ef\3\2\2\2\u04ef\u00d7\3\2\2\2\u04f0"+
		"\u04f3\5\u00a4S\2\u04f1\u04f3\5\u00e2r\2\u04f2\u04f0\3\2\2\2\u04f2\u04f1"+
		"\3\2\2\2\u04f3\u00d9\3\2\2\2\u04f4\u04f5\5\u00e2r\2\u04f5\u00db\3\2\2"+
		"\2\u04f6\u04f7\7Y\2\2\u04f7\u04f8\5N(\2\u04f8\u04f9\5\u0146\u00a4\2\u04f9"+
		"\u00dd\3\2\2\2\u04fa\u04fb\5\u00e0q\2\u04fb\u00df\3\2\2\2\u04fc\u04fd"+
		"\7Y\2\2\u04fd\u04fe\5\u00e6t\2\u04fe\u04ff\5\u0146\u00a4\2\u04ff\u00e1"+
		"\3\2\2\2\u0500\u0507\5\u00e4s\2\u0501\u0502\7`\2\2\u0502\u0503\5\u0148"+
		"\u00a5\2\u0503\u0504\5\u00e4s\2\u0504\u0506\3\2\2\2\u0505\u0501\3\2\2"+
		"\2\u0506\u0509\3\2\2\2\u0507\u0505\3\2\2\2\u0507\u0508\3\2\2\2\u0508\u00e3"+
		"\3\2\2\2\u0509\u0507\3\2\2\2\u050a\u050c\7s\2\2\u050b\u050a\3\2\2\2\u050b"+
		"\u050c\3\2\2\2\u050c\u050d\3\2\2\2\u050d\u050e\5\u00f2z\2\u050e\u00e5"+
		"\3\2\2\2\u050f\u0512\5\u00e8u\2\u0510\u0512\5~@\2\u0511\u050f\3\2\2\2"+
		"\u0511\u0510\3\2\2\2\u0512\u00e7\3\2\2\2\u0513\u0514\5\u00f6|\2\u0514"+
		"\u00e9\3\2\2\2\u0515\u0517\5\u00fa~\2\u0516\u0518\t\b\2\2\u0517\u0516"+
		"\3\2\2\2\u0517\u0518\3\2\2\2\u0518\u00eb\3\2\2\2\u0519\u051a\7\65\2\2"+
		"\u051a\u051b\5\u00e0q\2\u051b\u051c\5\u0148\u00a5\2\u051c\u051d\7[\2\2"+
		"\u051d\u0521\5\u0148\u00a5\2\u051e\u0520\5\u00eex\2\u051f\u051e\3\2\2"+
		"\2\u0520\u0523\3\2\2\2\u0521\u051f\3\2\2\2\u0521\u0522\3\2\2\2\u0522\u0524"+
		"\3\2\2\2\u0523\u0521\3\2\2\2\u0524\u0525\5\u0148\u00a5\2\u0525\u0526\7"+
		"\\\2\2\u0526\u00ed\3\2\2\2\u0527\u0528\5\u00f0y\2\u0528\u0529\5\u0148"+
		"\u00a5\2\u0529\u052b\3\2\2\2\u052a\u0527\3\2\2\2\u052b\u052c\3\2\2\2\u052c"+
		"\u052a\3\2\2\2\u052c\u052d\3\2\2\2\u052d\u052e\3\2\2\2\u052e\u052f\5\u008c"+
		"G\2\u052f\u00ef\3\2\2\2\u0530\u0531\7\24\2\2\u0531\u0534\5\u00e2r\2\u0532"+
		"\u0534\7\31\2\2\u0533\u0530\3\2\2\2\u0533\u0532\3\2\2\2\u0534\u0535\3"+
		"\2\2\2\u0535\u0536\t\t\2\2\u0536\u00f1\3\2\2\2\u0537\u0538\bz\1\2\u0538"+
		"\u0539\5\u00dco\2\u0539\u053a\5\u00f4{\2\u053a\u054a\3\2\2\2\u053b\u054a"+
		"\5\u00eav\2\u053c\u054a\5\u00ecw\2\u053d\u053e\t\n\2\2\u053e\u053f\5\u0148"+
		"\u00a5\2\u053f\u0540\5\u00f2z\25\u0540\u054a\3\2\2\2\u0541\u0542\t\13"+
		"\2\2\u0542\u054a\5\u00f2z\23\u0543\u0544\5\u00acW\2\u0544\u0545\5\u0148"+
		"\u00a5\2\u0545\u0546\7b\2\2\u0546\u0547\5\u0148\u00a5\2\u0547\u0548\5"+
		"\u00e8u\2\u0548\u054a\3\2\2\2\u0549\u0537\3\2\2\2\u0549\u053b\3\2\2\2"+
		"\u0549\u053c\3\2\2\2\u0549\u053d\3\2\2\2\u0549\u0541\3\2\2\2\u0549\u0543"+
		"\3\2\2\2\u054a\u05bf\3\2\2\2\u054b\u054c\f\24\2\2\u054c\u054d\7P\2\2\u054d"+
		"\u054e\5\u0148\u00a5\2\u054e\u054f\5\u00f2z\25\u054f\u05be\3\2\2\2\u0550"+
		"\u0551\f\22\2\2\u0551\u0552\5\u0148\u00a5\2\u0552\u0553\t\f\2\2\u0553"+
		"\u0554\5\u0148\u00a5\2\u0554\u0555\5\u00f2z\23\u0555\u05be\3\2\2\2\u0556"+
		"\u0557\f\21\2\2\u0557\u0558\t\r\2\2\u0558\u0559\5\u0148\u00a5\2\u0559"+
		"\u055a\5\u00f2z\22\u055a\u05be\3\2\2\2\u055b\u055c\f\20\2\2\u055c\u0567"+
		"\5\u0148\u00a5\2\u055d\u055e\7d\2\2\u055e\u0565\7d\2\2\u055f\u0560\7c"+
		"\2\2\u0560\u0561\7c\2\2\u0561\u0565\7c\2\2\u0562\u0563\7c\2\2\u0563\u0565"+
		"\7c\2\2\u0564\u055d\3\2\2\2\u0564\u055f\3\2\2\2\u0564\u0562\3\2\2\2\u0565"+
		"\u0568\3\2\2\2\u0566\u0568\t\16\2\2\u0567\u0564\3\2\2\2\u0567\u0566\3"+
		"\2\2\2\u0568\u0569\3\2\2\2\u0569\u056a\5\u0148\u00a5\2\u056a\u056b\5\u00f2"+
		"z\21\u056b\u05be\3\2\2\2\u056c\u056d\f\16\2\2\u056d\u056e\5\u0148\u00a5"+
		"\2\u056e\u056f\t\17\2\2\u056f\u0570\5\u0148\u00a5\2\u0570\u0571\5\u00f2"+
		"z\17\u0571\u05be\3\2\2\2\u0572\u0573\f\r\2\2\u0573\u0574\5\u0148\u00a5"+
		"\2\u0574\u0575\t\20\2\2\u0575\u0576\5\u0148\u00a5\2\u0576\u0577\5\u00f2"+
		"z\16\u0577\u05be\3\2\2\2\u0578\u0579\f\f\2\2\u0579\u057a\5\u0148\u00a5"+
		"\2\u057a\u057b\t\21\2\2\u057b\u057c\5\u0148\u00a5\2\u057c\u057d\5\u00f2"+
		"z\r\u057d\u05be\3\2\2\2\u057e\u057f\f\13\2\2\u057f\u0580\5\u0148\u00a5"+
		"\2\u0580\u0581\7u\2\2\u0581\u0582\5\u0148\u00a5\2\u0582\u0583\5\u00f2"+
		"z\f\u0583\u05be\3\2\2\2\u0584\u0585\f\n\2\2\u0585\u0586\5\u0148\u00a5"+
		"\2\u0586\u0587\7w\2\2\u0587\u0588\5\u0148\u00a5\2\u0588\u0589\5\u00f2"+
		"z\13\u0589\u05be\3\2\2\2\u058a\u058b\f\t\2\2\u058b\u058c\5\u0148\u00a5"+
		"\2\u058c\u058d\7v\2\2\u058d\u058e\5\u0148\u00a5\2\u058e\u058f\5\u00f2"+
		"z\n\u058f\u05be\3\2\2\2\u0590\u0591\f\b\2\2\u0591\u0592\5\u0148\u00a5"+
		"\2\u0592\u0593\7m\2\2\u0593\u0594\5\u0148\u00a5\2\u0594\u0595\5\u00f2"+
		"z\t\u0595\u05be\3\2\2\2\u0596\u0597\f\7\2\2\u0597\u0598\5\u0148\u00a5"+
		"\2\u0598\u0599\7n\2\2\u0599\u059a\5\u0148\u00a5\2\u059a\u059b\5\u00f2"+
		"z\b\u059b\u05be\3\2\2\2\u059c\u059d\f\6\2\2\u059d\u059e\5\u0148\u00a5"+
		"\2\u059e\u059f\7T\2\2\u059f\u05a0\5\u0148\u00a5\2\u05a0\u05a1\5\u00f2"+
		"z\6\u05a1\u05be\3\2\2\2\u05a2\u05a3\f\5\2\2\u05a3\u05ad\5\u0148\u00a5"+
		"\2\u05a4\u05a5\7g\2\2\u05a5\u05a6\5\u0148\u00a5\2\u05a6\u05a7\5\u00f2"+
		"z\2\u05a7\u05a8\5\u0148\u00a5\2\u05a8\u05a9\7h\2\2\u05a9\u05aa\5\u0148"+
		"\u00a5\2\u05aa\u05ae\3\2\2\2\u05ab\u05ac\7K\2\2\u05ac\u05ae\5\u0148\u00a5"+
		"\2\u05ad\u05a4\3\2\2\2\u05ad\u05ab\3\2\2\2\u05ae\u05af\3\2\2\2\u05af\u05b0"+
		"\5\u00f2z\5\u05b0\u05be\3\2\2\2\u05b1\u05b2\f\17\2\2\u05b2\u05b3\5\u0148"+
		"\u00a5\2\u05b3\u05b4\t\22\2\2\u05b4\u05b5\5\u0148\u00a5\2\u05b5\u05b6"+
		"\5N(\2\u05b6\u05be\3\2\2\2\u05b7\u05b8\f\3\2\2\u05b8\u05b9\5\u0148\u00a5"+
		"\2\u05b9\u05ba\t\23\2\2\u05ba\u05bb\5\u0148\u00a5\2\u05bb\u05bc\5\u00e6"+
		"t\2\u05bc\u05be\3\2\2\2\u05bd\u054b\3\2\2\2\u05bd\u0550\3\2\2\2\u05bd"+
		"\u0556\3\2\2\2\u05bd\u055b\3\2\2\2\u05bd\u056c\3\2\2\2\u05bd\u0572\3\2"+
		"\2\2\u05bd\u0578\3\2\2\2\u05bd\u057e\3\2\2\2\u05bd\u0584\3\2\2\2\u05bd"+
		"\u058a\3\2\2\2\u05bd\u0590\3\2\2\2\u05bd\u0596\3\2\2\2\u05bd\u059c\3\2"+
		"\2\2\u05bd\u05a2\3\2\2\2\u05bd\u05b1\3\2\2\2\u05bd\u05b7\3\2\2\2\u05be"+
		"\u05c1\3\2\2\2\u05bf\u05bd\3\2\2\2\u05bf\u05c0\3\2\2\2\u05c0\u00f3\3\2"+
		"\2\2\u05c1\u05bf\3\2\2\2\u05c2\u05c3\5\u00dco\2\u05c3\u05c4\5\u00f4{\2"+
		"\u05c4\u05cd\3\2\2\2\u05c5\u05cd\5\u00eav\2\u05c6\u05c7\t\n\2\2\u05c7"+
		"\u05c8\5\u0148\u00a5\2\u05c8\u05c9\5\u00f4{\2\u05c9\u05cd\3\2\2\2\u05ca"+
		"\u05cb\t\13\2\2\u05cb\u05cd\5\u00f4{\2\u05cc\u05c2\3\2\2\2\u05cc\u05c5"+
		"\3\2\2\2\u05cc\u05c6\3\2\2\2\u05cc\u05ca\3\2\2\2\u05cd\u00f5\3\2\2\2\u05ce"+
		"\u05d2\5\u00f2z\2\u05cf\u05d0\6|\26\3\u05d0\u05d3\5\u0132\u009a\2\u05d1"+
		"\u05d3\3\2\2\2\u05d2\u05cf\3\2\2\2\u05d2\u05d1\3\2\2\2\u05d3\u05d7\3\2"+
		"\2\2\u05d4\u05d6\5\u00f8}\2\u05d5\u05d4\3\2\2\2\u05d6\u05d9\3\2\2\2\u05d7"+
		"\u05d5\3\2\2\2\u05d7\u05d8\3\2\2\2\u05d8\u00f7\3\2\2\2\u05d9\u05d7\3\2"+
		"\2\2\u05da\u05e1\5\u010c\u0087\2\u05db\u05dd\5\u00fc\177\2\u05dc\u05db"+
		"\3\2\2\2\u05dd\u05de\3\2\2\2\u05de\u05dc\3\2\2\2\u05de\u05df\3\2\2\2\u05df"+
		"\u05e2\3\2\2\2\u05e0\u05e2\5\u0132\u009a\2\u05e1\u05dc\3\2\2\2\u05e1\u05e0"+
		"\3\2\2\2\u05e1\u05e2\3\2\2\2\u05e2\u00f9\3\2\2\2\u05e3\u05e7\5\u0106\u0084"+
		"\2\u05e4\u05e5\6~\27\2\u05e5\u05e7\7\62\2\2\u05e6\u05e3\3\2\2\2\u05e6"+
		"\u05e4\3\2\2\2\u05e7\u05ed\3\2\2\2\u05e8\u05e9\5\u00fc\177\2\u05e9\u05ea"+
		"\b~\1\2\u05ea\u05ec\3\2\2\2\u05eb\u05e8\3\2\2\2\u05ec\u05ef\3\2\2\2\u05ed"+
		"\u05eb\3\2\2\2\u05ed\u05ee\3\2\2\2\u05ee\u00fb\3\2\2\2\u05ef\u05ed\3\2"+
		"\2\2\u05f0\u060c\5\u0148\u00a5\2\u05f1\u05f2\7a\2\2\u05f2\u05f3\5\u0148"+
		"\u00a5\2\u05f3\u05f4\7(\2\2\u05f4\u05f5\5\u0122\u0092\2\u05f5\u05f6\b"+
		"\177\1\2\u05f6\u060d\3\2\2\2\u05f7\u05f8\t\24\2\2\u05f8\u05fb\5\u0148"+
		"\u00a5\2\u05f9\u05fc\7\u0087\2\2\u05fa\u05fc\5\u012c\u0097\2\u05fb\u05f9"+
		"\3\2\2\2\u05fb\u05fa\3\2\2\2\u05fb\u05fc\3\2\2\2\u05fc\u0605\3\2\2\2\u05fd"+
		"\u05fe\7L\2\2\u05fe\u0605\5\u0148\u00a5\2\u05ff\u0600\7M\2\2\u0600\u0602"+
		"\5\u0148\u00a5\2\u0601\u0603\5\u012c\u0097\2\u0602\u0601\3\2\2\2\u0602"+
		"\u0603\3\2\2\2\u0603\u0605\3\2\2\2\u0604\u05f7\3\2\2\2\u0604\u05fd\3\2"+
		"\2\2\u0604\u05ff\3\2\2\2\u0605\u0606\3\2\2\2\u0606\u0607\5\u00fe\u0080"+
		"\2\u0607\u0608\b\177\1\2\u0608\u060d\3\2\2\2\u0609\u060a\5\u0088E\2\u060a"+
		"\u060b\b\177\1\2\u060b\u060d\3\2\2\2\u060c\u05f1\3\2\2\2\u060c\u0604\3"+
		"\2\2\2\u060c\u0609\3\2\2\2\u060d\u0618\3\2\2\2\u060e\u060f\5\u0130\u0099"+
		"\2\u060f\u0610\b\177\1\2\u0610\u0618\3\2\2\2\u0611\u0612\5\u0102\u0082"+
		"\2\u0612\u0613\b\177\1\2\u0613\u0618\3\2\2\2\u0614\u0615\5\u0104\u0083"+
		"\2\u0615\u0616\b\177\1\2\u0616\u0618\3\2\2\2\u0617\u05f0\3\2\2\2\u0617"+
		"\u060e\3\2\2\2\u0617\u0611\3\2\2\2\u0617\u0614\3\2\2\2\u0618\u00fd\3\2"+
		"\2\2\u0619\u061e\5\u0140\u00a1\2\u061a\u061e\5\u013c\u009f\2\u061b\u061e"+
		"\5\u0100\u0081\2\u061c\u061e\5\u0144\u00a3\2\u061d\u0619\3\2\2\2\u061d"+
		"\u061a\3\2\2\2\u061d\u061b\3\2\2\2\u061d\u061c\3\2\2\2\u061e\u00ff\3\2"+
		"\2\2\u061f\u0622\5\u00dep\2\u0620\u0622\5v<\2\u0621\u061f\3\2\2\2\u0621"+
		"\u0620\3\2\2\2\u0622\u0101\3\2\2\2\u0623\u0625\t\25\2\2\u0624\u0626\5"+
		"\u00e2r\2\u0625\u0624\3\2\2\2\u0625\u0626\3\2\2\2\u0626\u0627\3\2\2\2"+
		"\u0627\u0628\7^\2\2\u0628\u0103\3\2\2\2\u0629\u062c\t\25\2\2\u062a\u062d"+
		"\5\u0114\u008b\2\u062b\u062d\7h\2\2\u062c\u062a\3\2\2\2\u062c\u062b\3"+
		"\2\2\2\u062d\u062e\3\2\2\2\u062e\u062f\7^\2\2\u062f\u0105\3\2\2\2\u0630"+
		"\u0632\5\u0140\u00a1\2\u0631\u0633\5X-\2\u0632\u0631\3\2\2\2\u0632\u0633"+
		"\3\2\2\2\u0633\u0642\3\2\2\2\u0634\u0642\5t;\2\u0635\u0642\5v<\2\u0636"+
		"\u0637\7(\2\2\u0637\u0638\5\u0148\u00a5\2\u0638\u0639\5\u0122\u0092\2"+
		"\u0639\u0642\3\2\2\2\u063a\u0642\7\67\2\2\u063b\u0642\7\64\2\2\u063c\u0642"+
		"\5\u00dep\2\u063d\u0642\5\u0088E\2\u063e\u0642\5\u010e\u0088\2\u063f\u0642"+
		"\5\u0110\u0089\2\u0640\u0642\5\u0142\u00a2\2\u0641\u0630\3\2\2\2\u0641"+
		"\u0634\3\2\2\2\u0641\u0635\3\2\2\2\u0641\u0636\3\2\2\2\u0641\u063a\3\2"+
		"\2\2\u0641\u063b\3\2\2\2\u0641\u063c\3\2\2\2\u0641\u063d\3\2\2\2\u0641"+
		"\u063e\3\2\2\2\u0641\u063f\3\2\2\2\u0641\u0640\3\2\2\2\u0642\u0107\3\2"+
		"\2\2\u0643\u064a\5\u0140\u00a1\2\u0644\u064a\5t;\2\u0645\u064a\5v<\2\u0646"+
		"\u064a\5\u00dep\2\u0647\u064a\5\u010e\u0088\2\u0648\u064a\5\u0110\u0089"+
		"\2\u0649\u0643\3\2\2\2\u0649\u0644\3\2\2\2\u0649\u0645\3\2\2\2\u0649\u0646"+
		"\3\2\2\2\u0649\u0647\3\2\2\2\u0649\u0648\3\2\2\2\u064a\u0109\3\2\2\2\u064b"+
		"\u064f\5\u0140\u00a1\2\u064c\u064f\5t;\2\u064d\u064f\5v<\2\u064e\u064b"+
		"\3\2\2\2\u064e\u064c\3\2\2\2\u064e\u064d\3\2\2\2\u064f\u010b\3\2\2\2\u0650"+
		"\u0654\5\u0140\u00a1\2\u0651\u0654\5t;\2\u0652\u0654\5v<\2\u0653\u0650"+
		"\3\2\2\2\u0653\u0651\3\2\2\2\u0653\u0652\3\2\2\2\u0654\u010d\3\2\2\2\u0655"+
		"\u0657\7]\2\2\u0656\u0658\5\u00e2r\2\u0657\u0656\3\2\2\2\u0657\u0658\3"+
		"\2\2\2\u0658\u065a\3\2\2\2\u0659\u065b\7`\2\2\u065a\u0659\3\2\2\2\u065a"+
		"\u065b\3\2\2\2\u065b\u065c\3\2\2\2\u065c\u065d\7^\2\2\u065d\u010f\3\2"+
		"\2\2\u065e\u0664\7]\2\2\u065f\u0661\5\u0112\u008a\2\u0660\u0662\7`\2\2"+
		"\u0661\u0660\3\2\2\2\u0661\u0662\3\2\2\2\u0662\u0665\3\2\2\2\u0663\u0665"+
		"\7h\2\2\u0664\u065f\3\2\2\2\u0664\u0663\3\2\2\2\u0665\u0666\3\2\2\2\u0666"+
		"\u0667\7^\2\2\u0667\u0111\3\2\2\2\u0668\u066d\5\u0116\u008c\2\u0669\u066a"+
		"\7`\2\2\u066a\u066c\5\u0116\u008c\2\u066b\u0669\3\2\2\2\u066c\u066f\3"+
		"\2\2\2\u066d\u066b\3\2\2\2\u066d\u066e\3\2\2\2\u066e\u0113\3\2\2\2\u066f"+
		"\u066d\3\2\2\2\u0670\u0675\5\u0118\u008d\2\u0671\u0672\7`\2\2\u0672\u0674"+
		"\5\u0118\u008d\2\u0673\u0671\3\2\2\2\u0674\u0677\3\2\2\2\u0675\u0673\3"+
		"\2\2\2\u0675\u0676\3\2\2\2\u0676\u0115\3\2\2\2\u0677\u0675\3\2\2\2\u0678"+
		"\u0679\5\u011c\u008f\2\u0679\u067a\7h\2\2\u067a\u067b\5\u0148\u00a5\2"+
		"\u067b\u067c\5\u00f2z\2\u067c\u0683\3\2\2\2\u067d\u067e\7s\2\2\u067e\u067f"+
		"\7h\2\2\u067f\u0680\5\u0148\u00a5\2\u0680\u0681\5\u00f2z\2\u0681\u0683"+
		"\3\2\2\2\u0682\u0678\3\2\2\2\u0682\u067d\3\2\2\2\u0683\u0117\3\2\2\2\u0684"+
		"\u0685\5\u011e\u0090\2\u0685\u0686\7h\2\2\u0686\u0687\5\u0148\u00a5\2"+
		"\u0687\u0688\5\u00f2z\2\u0688\u068f\3\2\2\2\u0689\u068a\7s\2\2\u068a\u068b"+
		"\7h\2\2\u068b\u068c\5\u0148\u00a5\2\u068c\u068d\5\u00f2z\2\u068d\u068f"+
		"\3\2\2\2\u068e\u0684\3\2\2\2\u068e\u0689\3\2\2\2\u068f\u0119\3\2\2\2\u0690"+
		"\u0691\5\u0120\u0091\2\u0691\u0692\7h\2\2\u0692\u0693\5\u0148\u00a5\2"+
		"\u0693\u0694\5\u00f2z\2\u0694\u069b\3\2\2\2\u0695\u0696\7s\2\2\u0696\u0697"+
		"\7h\2\2\u0697\u0698\5\u0148\u00a5\2\u0698\u0699\5\u00f2z\2\u0699\u069b"+
		"\3\2\2\2\u069a\u0690\3\2\2\2\u069a\u0695\3\2\2\2\u069b\u011b\3\2\2\2\u069c"+
		"\u069f\5\u0144\u00a3\2\u069d\u069f\5\u0106\u0084\2\u069e\u069c\3\2\2\2"+
		"\u069e\u069d\3\2\2\2\u069f\u011d\3\2\2\2\u06a0\u06a3\5\u0144\u00a3\2\u06a1"+
		"\u06a3\5\u0108\u0085\2\u06a2\u06a0\3\2\2\2\u06a2\u06a1\3\2\2\2\u06a3\u011f"+
		"\3\2\2\2\u06a4\u06a7\5\u0144\u00a3\2\u06a5\u06a7\5\u010a\u0086\2\u06a6"+
		"\u06a4\3\2\2\2\u06a6\u06a5\3\2\2\2\u06a7\u0121\3\2\2\2\u06a8\u06b8\5\u012a"+
		"\u0096\2\u06a9\u06aa\5\u0148\u00a5\2\u06aa\u06ac\5\u0130\u0099\2\u06ab"+
		"\u06ad\5\u0128\u0095\2\u06ac\u06ab\3\2\2\2\u06ac\u06ad\3\2\2\2\u06ad\u06b9"+
		"\3\2\2\2\u06ae\u06b0\5\u0124\u0093\2\u06af\u06ae\3\2\2\2\u06b0\u06b1\3"+
		"\2\2\2\u06b1\u06af\3\2\2\2\u06b1\u06b2\3\2\2\2\u06b2\u06b6\3\2\2\2\u06b3"+
		"\u06b4\5\u0148\u00a5\2\u06b4\u06b5\5\u0126\u0094\2\u06b5\u06b7\3\2\2\2"+
		"\u06b6\u06b3\3\2\2\2\u06b6\u06b7\3\2\2\2\u06b7\u06b9\3\2\2\2\u06b8\u06a9"+
		"\3\2\2\2\u06b8\u06af\3\2\2\2\u06b9\u0123\3\2\2\2\u06ba\u06bb\5\u008eH"+
		"\2\u06bb\u06bd\7]\2\2\u06bc\u06be\5\u00f2z\2\u06bd\u06bc\3\2\2\2\u06bd"+
		"\u06be\3\2\2\2\u06be\u06bf\3\2\2\2\u06bf\u06c0\7^\2\2\u06c0\u0125\3\2"+
		"\2\2\u06c1\u06c2\7[\2\2\u06c2\u06c6\5\u0148\u00a5\2\u06c3\u06c4\5F$\2"+
		"\u06c4\u06c5\5\u0148\u00a5\2\u06c5\u06c7\3\2\2\2\u06c6\u06c3\3\2\2\2\u06c6"+
		"\u06c7\3\2\2\2\u06c7\u06c8\3\2\2\2\u06c8\u06c9\7\\\2\2\u06c9\u0127\3\2"+
		"\2\2\u06ca\u06cb\5*\26\2\u06cb\u0129\3\2\2\2\u06cc\u06d2\5\u008eH\2\u06cd"+
		"\u06d3\5V,\2\u06ce\u06d0\5p9\2\u06cf\u06d1\5\u012e\u0098\2\u06d0\u06cf"+
		"\3\2\2\2\u06d0\u06d1\3\2\2\2\u06d1\u06d3\3\2\2\2\u06d2\u06cd\3\2\2\2\u06d2"+
		"\u06ce\3\2\2\2\u06d3\u012b\3\2\2\2\u06d4\u06d5\7d\2\2\u06d5\u06d6\5\u0148"+
		"\u00a5\2\u06d6\u06d7\5&\24\2\u06d7\u06d8\5\u0148\u00a5\2\u06d8\u06d9\7"+
		"c\2\2\u06d9\u012d\3\2\2\2\u06da\u06db\7d\2\2\u06db\u06de\7c\2\2\u06dc"+
		"\u06de\5X-\2\u06dd\u06da\3\2\2\2\u06dd\u06dc\3\2\2\2\u06de\u012f\3\2\2"+
		"\2\u06df\u06e1\7Y\2\2\u06e0\u06e2\5\u0134\u009b\2\u06e1\u06e0\3\2\2\2"+
		"\u06e1\u06e2\3\2\2\2\u06e2\u06e4\3\2\2\2\u06e3\u06e5\7`\2\2\u06e4\u06e3"+
		"\3\2\2\2\u06e4\u06e5\3\2\2\2\u06e5\u06e6\3\2\2\2\u06e6\u06e7\5\u0146\u00a4"+
		"\2\u06e7\u0131\3\2\2\2\u06e8\u06ef\5\u0136\u009c\2\u06e9\u06ea\7`\2\2"+
		"\u06ea\u06eb\5\u0148\u00a5\2\u06eb\u06ec\5\u0138\u009d\2\u06ec\u06ee\3"+
		"\2\2\2\u06ed\u06e9\3\2\2\2\u06ee\u06f1\3\2\2\2\u06ef\u06ed\3\2\2\2\u06ef"+
		"\u06f0\3\2\2\2\u06f0\u0133\3\2\2\2\u06f1\u06ef\3\2\2\2\u06f2\u06f9\5\u013a"+
		"\u009e\2\u06f3\u06f4\7`\2\2\u06f4\u06f5\5\u0148\u00a5\2\u06f5\u06f6\5"+
		"\u013a\u009e\2\u06f6\u06f8\3\2\2\2\u06f7\u06f3\3\2\2\2\u06f8\u06fb\3\2"+
		"\2\2\u06f9\u06f7\3\2\2\2\u06f9\u06fa\3\2\2\2\u06fa\u0135\3\2\2\2\u06fb"+
		"\u06f9\3\2\2\2\u06fc\u06ff\5\u00e4s\2\u06fd\u06ff\5\u011a\u008e\2\u06fe"+
		"\u06fc\3\2\2\2\u06fe\u06fd\3\2\2\2\u06ff\u0137\3\2\2\2\u0700\u0703\5\u00e4"+
		"s\2\u0701\u0703\5\u0118\u008d\2\u0702\u0700\3\2\2\2\u0702\u0701\3\2\2"+
		"\2\u0703\u0139\3\2\2\2\u0704\u0708\5\u00e4s\2\u0705\u0708\5~@\2\u0706"+
		"\u0708\5\u0118\u008d\2\u0707\u0704\3\2\2\2\u0707\u0705\3\2\2\2\u0707\u0706"+
		"\3\2\2\2\u0708\u013b\3\2\2\2\u0709\u070a\7\3\2\2\u070a\u013d\3\2\2\2\u070b"+
		"\u070c\7\u0085\2\2\u070c\u013f\3\2\2\2\u070d\u070e\t\26\2\2\u070e\u0141"+
		"\3\2\2\2\u070f\u0710\t\27\2\2\u0710\u0143\3\2\2\2\u0711\u0712\t\30\2\2"+
		"\u0712\u0145\3\2\2\2\u0713\u0714\7Z\2\2\u0714\u0147\3\2\2\2\u0715\u0717"+
		"\7\u008a\2\2\u0716\u0715\3\2\2\2\u0717\u071a\3\2\2\2\u0718\u0716\3\2\2"+
		"\2\u0718\u0719\3\2\2\2\u0719\u0149\3\2\2\2\u071a\u0718\3\2\2\2\u071b\u071d"+
		"\t\31\2\2\u071c\u071b\3\2\2\2\u071d\u071e\3\2\2\2\u071e\u071c\3\2\2\2"+
		"\u071e\u071f\3\2\2\2\u071f\u014b\3\2\2\2\u00c8\u014f\u0151\u0154\u015e"+
		"\u0162\u0169\u0172\u0179\u0180\u0185\u018d\u0194\u0197\u019f\u01a4\u01a8"+
		"\u01ad\u01b5\u01c1\u01cd\u01d6\u01e0\u01f0\u01f6\u01fb\u0202\u0209\u0210"+
		"\u021c\u021f\u0222\u022a\u022d\u0230\u023c\u0242\u0245\u0249\u024d\u0254"+
		"\u0256\u025a\u025f\u026c\u0271\u0273\u027b\u027f\u028a\u0293\u02a1\u02a6"+
		"\u02ae\u02b1\u02b6\u02bd\u02c0\u02c6\u02c9\u02cd\u02d1\u02de\u02eb\u02ed"+
		"\u02f9\u02fe\u0304\u030c\u0314\u0317\u031f\u0328\u0330\u0337\u0343\u034b"+
		"\u0353\u035a\u0360\u0373\u0377\u037e\u0382\u0385\u038c\u038f\u0397\u039b"+
		"\u03a3\u03a8\u03af\u03b3\u03b7\u03c0\u03cb\u03d0\u03d8\u03dc\u03de\u03e4"+
		"\u03eb\u03f3\u03fc\u0401\u0409\u040f\u0419\u041f\u0427\u042d\u0437\u043b"+
		"\u0452\u0456\u045a\u0461\u046a\u0470\u0479\u0486\u0497\u049d\u04a9\u04b4"+
		"\u04be\u04c3\u04cb\u04d7\u04db\u04df\u04e6\u04ea\u04ee\u04f2\u0507\u050b"+
		"\u0511\u0517\u0521\u052c\u0533\u0549\u0564\u0567\u05ad\u05bd\u05bf\u05cc"+
		"\u05d2\u05d7\u05de\u05e1\u05e6\u05ed\u05fb\u0602\u0604\u060c\u0617\u061d"+
		"\u0621\u0625\u062c\u0632\u0641\u0649\u064e\u0653\u0657\u065a\u0661\u0664"+
		"\u066d\u0675\u0682\u068e\u069a\u069e\u06a2\u06a6\u06ac\u06b1\u06b6\u06b8"+
		"\u06bd\u06c6\u06d0\u06d2\u06dd\u06e1\u06e4\u06ef\u06f9\u06fe\u0702\u0707"+
		"\u0718\u071e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
	}
}