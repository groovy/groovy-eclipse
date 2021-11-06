// Generated from GroovyLexer.g4 by ANTLR 4.9.0
package org.apache.groovy.parser.antlr4;

    import java.util.*;
    import org.apache.groovy.util.Maps;
    import org.codehaus.groovy.ast.Comment;
    import static org.apache.groovy.parser.antlr4.SemanticPredicates.*;

import groovyjarjarantlr4.v4.runtime.Lexer;
import groovyjarjarantlr4.v4.runtime.CharStream;
import groovyjarjarantlr4.v4.runtime.Token;
import groovyjarjarantlr4.v4.runtime.TokenStream;
import groovyjarjarantlr4.v4.runtime.*;
import groovyjarjarantlr4.v4.runtime.atn.*;
import groovyjarjarantlr4.v4.runtime.dfa.DFA;
import groovyjarjarantlr4.v4.runtime.misc.*;

public class GroovyLexer extends AbstractLexer {
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
		POWER=78, POWER_ASSIGN=79, SPACESHIP=80, IDENTICAL=81, NOT_IDENTICAL=82, 
		ARROW=83, NOT_INSTANCEOF=84, NOT_IN=85, LPAREN=86, RPAREN=87, LBRACE=88, 
		RBRACE=89, LBRACK=90, RBRACK=91, SEMI=92, COMMA=93, DOT=94, ASSIGN=95, 
		GT=96, LT=97, NOT=98, BITNOT=99, QUESTION=100, COLON=101, EQUAL=102, LE=103, 
		GE=104, NOTEQUAL=105, AND=106, OR=107, INC=108, DEC=109, ADD=110, SUB=111, 
		MUL=112, DIV=113, BITAND=114, BITOR=115, XOR=116, MOD=117, ADD_ASSIGN=118, 
		SUB_ASSIGN=119, MUL_ASSIGN=120, DIV_ASSIGN=121, AND_ASSIGN=122, OR_ASSIGN=123, 
		XOR_ASSIGN=124, MOD_ASSIGN=125, LSHIFT_ASSIGN=126, RSHIFT_ASSIGN=127, 
		URSHIFT_ASSIGN=128, ELVIS_ASSIGN=129, CapitalizedIdentifier=130, Identifier=131, 
		AT=132, ELLIPSIS=133, WS=134, NL=135, SH_COMMENT=136, UNEXPECTED_CHAR=137;
	public static final int
		DQ_GSTRING_MODE=1, TDQ_GSTRING_MODE=2, SLASHY_GSTRING_MODE=3, DOLLAR_SLASHY_GSTRING_MODE=4, 
		GSTRING_TYPE_SELECTOR_MODE=5, GSTRING_PATH_MODE=6;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "DQ_GSTRING_MODE", "TDQ_GSTRING_MODE", "SLASHY_GSTRING_MODE", 
		"DOLLAR_SLASHY_GSTRING_MODE", "GSTRING_TYPE_SELECTOR_MODE", "GSTRING_PATH_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"StringLiteral", "GStringBegin", "TdqGStringBegin", "SlashyGStringBegin", 
			"DollarSlashyGStringBegin", "GStringEnd", "GStringPart", "GStringCharacter", 
			"TdqGStringEnd", "TdqGStringPart", "TdqGStringCharacter", "SlashyGStringEnd", 
			"SlashyGStringPart", "SlashyGStringCharacter", "DollarSlashyGStringEnd", 
			"DollarSlashyGStringPart", "DollarSlashyGStringCharacter", "GStringLBrace", 
			"GStringIdentifier", "GStringPathPart", "RollBackOne", "DqStringCharacter", 
			"SqStringCharacter", "TdqStringCharacter", "TsqStringCharacter", "SlashyStringCharacter", 
			"DollarSlashyStringCharacter", "AS", "DEF", "IN", "TRAIT", "THREADSAFE", 
			"VAR", "BuiltInPrimitiveType", "ABSTRACT", "ASSERT", "BOOLEAN", "BREAK", 
			"YIELD", "BYTE", "CASE", "CATCH", "CHAR", "CLASS", "CONST", "CONTINUE", 
			"DEFAULT", "DO", "DOUBLE", "ELSE", "ENUM", "EXTENDS", "FINAL", "FINALLY", 
			"FLOAT", "FOR", "IF", "GOTO", "IMPLEMENTS", "IMPORT", "INSTANCEOF", "INT", 
			"INTERFACE", "LONG", "NATIVE", "NEW", "NON_SEALED", "PACKAGE", "PERMITS", 
			"PRIVATE", "PROTECTED", "PUBLIC", "RECORD", "RETURN", "SEALED", "SHORT", 
			"STATIC", "STRICTFP", "SUPER", "SWITCH", "SYNCHRONIZED", "THIS", "THROW", 
			"THROWS", "TRANSIENT", "TRY", "VOID", "VOLATILE", "WHILE", "IntegerLiteral", 
			"Zero", "DecimalIntegerLiteral", "HexIntegerLiteral", "OctalIntegerLiteral", 
			"BinaryIntegerLiteral", "IntegerTypeSuffix", "DecimalNumeral", "Digits", 
			"Digit", "NonZeroDigit", "DigitOrUnderscore", "Underscores", "Underscore", 
			"HexNumeral", "HexDigits", "HexDigit", "HexDigitOrUnderscore", "OctalNumeral", 
			"OctalDigits", "OctalDigit", "OctalDigitOrUnderscore", "BinaryNumeral", 
			"BinaryDigits", "BinaryDigit", "BinaryDigitOrUnderscore", "FloatingPointLiteral", 
			"DecimalFloatingPointLiteral", "ExponentPart", "ExponentIndicator", "SignedInteger", 
			"Sign", "FloatTypeSuffix", "HexadecimalFloatingPointLiteral", "HexSignificand", 
			"BinaryExponent", "BinaryExponentIndicator", "Dot", "BooleanLiteral", 
			"EscapeSequence", "OctalEscape", "UnicodeEscape", "ZeroToThree", "DollarEscape", 
			"LineEscape", "LineTerminator", "SlashEscape", "Backslash", "Slash", 
			"Dollar", "GStringQuotationMark", "SqStringQuotationMark", "TdqStringQuotationMark", 
			"TsqStringQuotationMark", "DollarSlashyGStringQuotationMarkBegin", "DollarSlashyGStringQuotationMarkEnd", 
			"DollarSlashEscape", "DollarDollarEscape", "NullLiteral", "RANGE_INCLUSIVE", 
			"RANGE_EXCLUSIVE_LEFT", "RANGE_EXCLUSIVE_RIGHT", "RANGE_EXCLUSIVE_FULL", 
			"SPREAD_DOT", "SAFE_DOT", "SAFE_INDEX", "SAFE_CHAIN_DOT", "ELVIS", "METHOD_POINTER", 
			"METHOD_REFERENCE", "REGEX_FIND", "REGEX_MATCH", "POWER", "POWER_ASSIGN", 
			"SPACESHIP", "IDENTICAL", "NOT_IDENTICAL", "ARROW", "NOT_INSTANCEOF", 
			"NOT_IN", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", 
			"SEMI", "COMMA", "DOT", "ASSIGN", "GT", "LT", "NOT", "BITNOT", "QUESTION", 
			"COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", "OR", "INC", "DEC", 
			"ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "XOR", "MOD", "ADD_ASSIGN", 
			"SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", 
			"XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", "URSHIFT_ASSIGN", 
			"ELVIS_ASSIGN", "CapitalizedIdentifier", "Identifier", "IdentifierInGString", 
			"JavaLetter", "JavaLetterInGString", "JavaLetterOrDigit", "JavaLetterOrDigitInGString", 
			"ShCommand", "AT", "ELLIPSIS", "WS", "NL", "ML_COMMENT", "SL_COMMENT", 
			"SH_COMMENT", "UNEXPECTED_CHAR"
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
			"'**='", "'<=>'", "'==='", "'!=='", "'->'", "'!instanceof'", "'!in'", 
			null, null, null, null, null, null, "';'", "','", null, "'='", "'>'", 
			"'<'", "'!'", "'~'", "'?'", "':'", "'=='", "'<='", "'>='", "'!='", "'&&'", 
			"'||'", "'++'", "'--'", "'+'", "'-'", "'*'", null, "'&'", "'|'", "'^'", 
			"'%'", "'+='", "'-='", "'*='", "'/='", "'&='", "'|='", "'^='", "'%='", 
			"'<<='", "'>>='", "'>>>='", "'?='", null, null, "'@'", "'...'"
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
			"NOT_IDENTICAL", "ARROW", "NOT_INSTANCEOF", "NOT_IN", "LPAREN", "RPAREN", 
			"LBRACE", "RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", "DOT", "ASSIGN", 
			"GT", "LT", "NOT", "BITNOT", "QUESTION", "COLON", "EQUAL", "LE", "GE", 
			"NOTEQUAL", "AND", "OR", "INC", "DEC", "ADD", "SUB", "MUL", "DIV", "BITAND", 
			"BITOR", "XOR", "MOD", "ADD_ASSIGN", "SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", 
			"AND_ASSIGN", "OR_ASSIGN", "XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", 
			"RSHIFT_ASSIGN", "URSHIFT_ASSIGN", "ELVIS_ASSIGN", "CapitalizedIdentifier", 
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


	    private boolean errorIgnored;
	    private long tokenIndex;
	    private int  lastTokenType;
	    private int  invalidDigitCount;

	    /**
	     * Record the index and token type of the current token while emitting tokens.
	     */
	    @Override
	    public void emit(Token token) {
	        this.tokenIndex++;

	        int tokenType = token.getType();
	        if (Token.DEFAULT_CHANNEL == token.getChannel()) {
	            this.lastTokenType = tokenType;
	        }

	        if (RollBackOne == tokenType) {
	            this.rollbackOneChar();
	        }

	        super.emit(token);
	    }

	    private static final int[] REGEX_CHECK_ARRAY = {
	        DEC,
	        INC,
	        THIS,
	        RBRACE,
	        RBRACK,
	        RPAREN,
	        GStringEnd,
	        NullLiteral,
	        StringLiteral,
	        BooleanLiteral,
	        IntegerLiteral,
	        FloatingPointLiteral,
	        Identifier, CapitalizedIdentifier
	    };
	    static {
	        Arrays.sort(REGEX_CHECK_ARRAY);
	    }

	    private boolean isRegexAllowed() {
	        return (Arrays.binarySearch(REGEX_CHECK_ARRAY, this.lastTokenType) < 0);
	    }

	    /**
	     * just a hook, which will be overrided by GroovyLangLexer
	     */
	    protected void rollbackOneChar() {}

	    private static class Paren {
	        private String text;
	        private int lastTokenType;
	        private int line;
	        private int column;

	        public Paren(String text, int lastTokenType, int line, int column) {
	            this.text = text;
	            this.lastTokenType = lastTokenType;
	            this.line = line;
	            this.column = column;
	        }

	        public String getText() {
	            return this.text;
	        }

	        public int getLastTokenType() {
	            return this.lastTokenType;
	        }

	        @SuppressWarnings("unused")
	        public int getLine() {
	            return line;
	        }

	        @SuppressWarnings("unused")
	        public int getColumn() {
	            return column;
	        }

	        @Override
	        public int hashCode() {
	            return (int) (text.hashCode() * line + column);
	        }

	        @Override
	        public boolean equals(Object obj) {
	            if (!(obj instanceof Paren)) {
	                return false;
	            }

	            Paren other = (Paren) obj;

	            return this.text.equals(other.text) && (this.line == other.line && this.column == other.column);
	        }
	    }

	    protected void enterParenCallback(String text) {}

	    protected void exitParenCallback(String text) {}

	    private final Deque<Paren> parenStack = new ArrayDeque<>(32);

	    private void enterParen() {
	        String text = getText();
	        enterParenCallback(text);

	        parenStack.push(new Paren(text, this.lastTokenType, getLine(), getCharPositionInLine()));
	    }

	    private void exitParen() {
	        String text = getText();
	        exitParenCallback(text);

	        Paren paren = parenStack.peek();
	        if (null == paren) return;
	        parenStack.pop();
	    }
	    private boolean isInsideParens() {
	        Paren paren = parenStack.peek();

	        // We just care about "(", "[" and "?[", inside which the new lines will be ignored.
	        // Notice: the new lines between "{" and "}" can not be ignored.
	        if (null == paren) {
	            return false;
	        }

	        String text = paren.getText();

	        return ("(".equals(text) && TRY != paren.getLastTokenType()) // we don't treat try-paren(i.e. try (....)) as parenthesis
	                    || "[".equals(text) || "?[".equals(text);
	    }
	    private void ignoreTokenInsideParens() {
	        if (!this.isInsideParens()) {
	            return;
	        }

	        this.setChannel(Token.HIDDEN_CHANNEL);
	    }
	    private void ignoreMultiLineCommentConditionally() {
	        if (!this.isInsideParens() && isFollowedByWhiteSpaces(_input)) {
	            return;
	        }

	        this.setChannel(Token.HIDDEN_CHANNEL);
	    }

	    @Override
	    public int getSyntaxErrorSource() {
	        return GroovySyntaxError.LEXER;
	    }

	    @Override
	    public int getErrorLine() {
	        return getLine();
	    }

	    @Override
	    public int getErrorColumn() {
	        return getCharPositionInLine() + 1;
	    }

	    @Override
	    public int popMode() {
	        try {
	            return super.popMode();
	        } catch (EmptyStackException ignore) { // raised when parens are unmatched: too many ), ], or }
	        }

	        return Integer.MIN_VALUE;
	    }

	    // GRECLIPSE add
	    private void addComment(int type) {
	        String text = _input.getText(Interval.of(_tokenStartCharIndex, getCharIndex() - 1));
	        Comment comment;
	        if (type == 0) {
	            comment = Comment.makeMultiLineComment( _tokenStartLine, _tokenStartCharPositionInLine + 1, getLine(), getCharPositionInLine() + 1, text);
	        } else {
	            comment = Comment.makeSingleLineComment(_tokenStartLine, _tokenStartCharPositionInLine + 1, getLine(), getCharPositionInLine() + 1, text);
	        }
	        comments.add(comment);
	    }
	    public List<Comment> getComments() { return comments; }
	    private final List<Comment> comments = new ArrayList<>();
	    // GRECLIPSE end

	    private static boolean isJavaIdentifierStartAndNotIdentifierIgnorable(int codePoint) {
	        return Character.isJavaIdentifierStart(codePoint) && !Character.isIdentifierIgnorable(codePoint);
	    }

	    private static boolean isJavaIdentifierPartAndNotIdentifierIgnorable(int codePoint) {
	        return Character.isJavaIdentifierPart(codePoint) && !Character.isIdentifierIgnorable(codePoint);
	    }

	    public boolean isErrorIgnored() {
	        return errorIgnored;
	    }

	    public void setErrorIgnored(boolean errorIgnored) {
	        this.errorIgnored = errorIgnored;
	    }


	public GroovyLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN);
		validateInputStream(_ATN, input);
	}

	@Override
	public String getGrammarFileName() { return "GroovyLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	@NotNull
	public String[] getChannelNames() { return channelNames; }

	@Override
	@NotNull
	public String[] getModeNames() { return modeNames; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 17:
			GStringLBrace_action(_localctx, actionIndex);
			break;

		case 20:
			RollBackOne_action(_localctx, actionIndex);
			break;

		case 89:
			IntegerLiteral_action(_localctx, actionIndex);
			break;

		case 115:
			FloatingPointLiteral_action(_localctx, actionIndex);
			break;

		case 154:
			SAFE_INDEX_action(_localctx, actionIndex);
			break;

		case 169:
			LPAREN_action(_localctx, actionIndex);
			break;

		case 170:
			RPAREN_action(_localctx, actionIndex);
			break;

		case 171:
			LBRACE_action(_localctx, actionIndex);
			break;

		case 172:
			RBRACE_action(_localctx, actionIndex);
			break;

		case 173:
			LBRACK_action(_localctx, actionIndex);
			break;

		case 174:
			RBRACK_action(_localctx, actionIndex);
			break;

		case 224:
			NL_action(_localctx, actionIndex);
			break;

		case 225:
			ML_COMMENT_action(_localctx, actionIndex);
			break;

		case 226:
			SL_COMMENT_action(_localctx, actionIndex);
			break;

		case 227:
			SH_COMMENT_action(_localctx, actionIndex);
			break;

		case 228:
			UNEXPECTED_CHAR_action(_localctx, actionIndex);
			break;
		}
	}
	private void GStringLBrace_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:
			 this.enterParen();  
			break;
		}
	}
	private void RollBackOne_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1:

			            // a trick to handle GStrings followed by EOF properly
			            int readChar = _input.LA(-1);
			            if (EOF == _input.LA(1) && ('"' == readChar || '/' == readChar)) {
			                setType(GStringEnd);
			            } else {
			                setChannel(HIDDEN);
			            }
			          
			break;
		}
	}
	private void IntegerLiteral_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 2:
			 require(errorIgnored, "Number ending with underscores is invalid", -1, true); 
			break;

		case 3:
			 invalidDigitCount++; 
			break;

		case 4:
			 require(errorIgnored, "Invalid octal number", -(invalidDigitCount + 1), true); 
			break;
		}
	}
	private void FloatingPointLiteral_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 5:
			 require(errorIgnored, "Number ending with underscores is invalid", -1, true); 
			break;
		}
	}
	private void SAFE_INDEX_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 6:
			 this.enterParen();     
			break;
		}
	}
	private void LPAREN_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 7:
			 this.enterParen();     
			break;
		}
	}
	private void RPAREN_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 8:
			 this.exitParen();      
			break;
		}
	}
	private void LBRACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 9:
			 this.enterParen();     
			break;
		}
	}
	private void RBRACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 10:
			 this.exitParen();      
			break;
		}
	}
	private void LBRACK_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 11:
			 this.enterParen();     
			break;
		}
	}
	private void RBRACK_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 12:
			 this.exitParen();      
			break;
		}
	}
	private void NL_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 13:
			 this.ignoreTokenInsideParens(); 
			break;
		}
	}
	private void ML_COMMENT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 14:
			 addComment(0); ignoreMultiLineCommentConditionally(); 
			break;
		}
	}
	private void SL_COMMENT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 15:
			 addComment(1); ignoreTokenInsideParens(); 
			break;
		}
	}
	private void SH_COMMENT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 16:
			 require(errorIgnored || 0 == this.tokenIndex, "Shebang comment should appear at the first line", -2, true); 
			break;
		}
	}
	private void UNEXPECTED_CHAR_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 17:
			 require(errorIgnored, "Unexpected character: '" + getText().replace("'", "\\'") + "'", -1, false); 
			break;
		}
	}
	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 0:
			return StringLiteral_sempred(_localctx, predIndex);

		case 3:
			return SlashyGStringBegin_sempred(_localctx, predIndex);

		case 4:
			return DollarSlashyGStringBegin_sempred(_localctx, predIndex);

		case 12:
			return SlashyGStringPart_sempred(_localctx, predIndex);

		case 15:
			return DollarSlashyGStringPart_sempred(_localctx, predIndex);

		case 23:
			return TdqStringCharacter_sempred(_localctx, predIndex);

		case 24:
			return TsqStringCharacter_sempred(_localctx, predIndex);

		case 25:
			return SlashyStringCharacter_sempred(_localctx, predIndex);

		case 26:
			return DollarSlashyStringCharacter_sempred(_localctx, predIndex);

		case 167:
			return NOT_INSTANCEOF_sempred(_localctx, predIndex);

		case 168:
			return NOT_IN_sempred(_localctx, predIndex);

		case 213:
			return CapitalizedIdentifier_sempred(_localctx, predIndex);

		case 216:
			return JavaLetter_sempred(_localctx, predIndex);

		case 217:
			return JavaLetterInGString_sempred(_localctx, predIndex);

		case 218:
			return JavaLetterOrDigit_sempred(_localctx, predIndex);

		case 219:
			return JavaLetterOrDigitInGString_sempred(_localctx, predIndex);
		}
		return true;
	}
	private boolean StringLiteral_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return  this.isRegexAllowed() && _input.LA(1) != '*' ;
		}
		return true;
	}
	private boolean SlashyGStringBegin_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return  this.isRegexAllowed() && _input.LA(1) != '*' ;

		case 2:
			return  isFollowedByJavaLetterInGString(_input) ;
		}
		return true;
	}
	private boolean DollarSlashyGStringBegin_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return  isFollowedByJavaLetterInGString(_input) ;
		}
		return true;
	}
	private boolean SlashyGStringPart_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return  isFollowedByJavaLetterInGString(_input) ;
		}
		return true;
	}
	private boolean DollarSlashyGStringPart_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return  isFollowedByJavaLetterInGString(_input) ;
		}
		return true;
	}
	private boolean TdqStringCharacter_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 6:
			return  _input.LA(1) != '"' || _input.LA(2) != '"' || _input.LA(3) == '"' && (_input.LA(4) != '"' || _input.LA(5) != '"') ;
		}
		return true;
	}
	private boolean TsqStringCharacter_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 7:
			return  _input.LA(1) != '\'' || _input.LA(2) != '\'' || _input.LA(3) == '\'' && (_input.LA(4) != '\'' || _input.LA(5) != '\'') ;
		}
		return true;
	}
	private boolean SlashyStringCharacter_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 8:
			return  !isFollowedByJavaLetterInGString(_input) ;
		}
		return true;
	}
	private boolean DollarSlashyStringCharacter_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 9:
			return  _input.LA(1) != '$' ;

		case 10:
			return  !isFollowedByJavaLetterInGString(_input) ;
		}
		return true;
	}
	private boolean NOT_INSTANCEOF_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 11:
			return  isFollowedBy(_input, ' ', '\t', '\r', '\n') ;
		}
		return true;
	}
	private boolean NOT_IN_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 12:
			return  isFollowedBy(_input, ' ', '\t', '\r', '\n', '[', '(', '{') ;
		}
		return true;
	}
	private boolean CapitalizedIdentifier_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 13:
			return Character.isUpperCase(_input.LA(-1));
		}
		return true;
	}
	private boolean JavaLetter_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 14:
			return  isJavaIdentifierStartAndNotIdentifierIgnorable(_input.LA(-1)) ;

		case 15:
			return  Character.isJavaIdentifierStart(Character.toCodePoint((char) _input.LA(-2), (char) _input.LA(-1))) ;
		}
		return true;
	}
	private boolean JavaLetterInGString_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 16:
			return  _input.LA(-1) != '$' ;
		}
		return true;
	}
	private boolean JavaLetterOrDigit_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 17:
			return  isJavaIdentifierPartAndNotIdentifierIgnorable(_input.LA(-1)) ;

		case 18:
			return  Character.isJavaIdentifierPart(Character.toCodePoint((char) _input.LA(-2), (char) _input.LA(-1))) ;
		}
		return true;
	}
	private boolean JavaLetterOrDigitInGString_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 19:
			return  _input.LA(-1) != '$' ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\2\u008b\u06e8\b\1\b"+
		"\1\b\1\b\1\b\1\b\1\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7"+
		"\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17"+
		"\4\20\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26"+
		"\4\27\t\27\4\30\t\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35"+
		"\4\36\t\36\4\37\t\37\4 \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t"+
		"\'\4(\t(\4)\t)\4*\t*\4+\t+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61"+
		"\4\62\t\62\4\63\t\63\4\64\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49"+
		"\t9\4:\t:\4;\t;\4<\t<\4=\t=\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD"+
		"\4E\tE\4F\tF\4G\tG\4H\tH\4I\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P"+
		"\tP\4Q\tQ\4R\tR\4S\tS\4T\tT\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t["+
		"\4\\\t\\\4]\t]\4^\t^\4_\t_\4`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4"+
		"g\tg\4h\th\4i\ti\4j\tj\4k\tk\4l\tl\4m\tm\4n\tn\4o\to\4p\tp\4q\tq\4r\t"+
		"r\4s\ts\4t\tt\4u\tu\4v\tv\4w\tw\4x\tx\4y\ty\4z\tz\4{\t{\4|\t|\4}\t}\4"+
		"~\t~\4\177\t\177\4\u0080\t\u0080\4\u0081\t\u0081\4\u0082\t\u0082\4\u0083"+
		"\t\u0083\4\u0084\t\u0084\4\u0085\t\u0085\4\u0086\t\u0086\4\u0087\t\u0087"+
		"\4\u0088\t\u0088\4\u0089\t\u0089\4\u008a\t\u008a\4\u008b\t\u008b\4\u008c"+
		"\t\u008c\4\u008d\t\u008d\4\u008e\t\u008e\4\u008f\t\u008f\4\u0090\t\u0090"+
		"\4\u0091\t\u0091\4\u0092\t\u0092\4\u0093\t\u0093\4\u0094\t\u0094\4\u0095"+
		"\t\u0095\4\u0096\t\u0096\4\u0097\t\u0097\4\u0098\t\u0098\4\u0099\t\u0099"+
		"\4\u009a\t\u009a\4\u009b\t\u009b\4\u009c\t\u009c\4\u009d\t\u009d\4\u009e"+
		"\t\u009e\4\u009f\t\u009f\4\u00a0\t\u00a0\4\u00a1\t\u00a1\4\u00a2\t\u00a2"+
		"\4\u00a3\t\u00a3\4\u00a4\t\u00a4\4\u00a5\t\u00a5\4\u00a6\t\u00a6\4\u00a7"+
		"\t\u00a7\4\u00a8\t\u00a8\4\u00a9\t\u00a9\4\u00aa\t\u00aa\4\u00ab\t\u00ab"+
		"\4\u00ac\t\u00ac\4\u00ad\t\u00ad\4\u00ae\t\u00ae\4\u00af\t\u00af\4\u00b0"+
		"\t\u00b0\4\u00b1\t\u00b1\4\u00b2\t\u00b2\4\u00b3\t\u00b3\4\u00b4\t\u00b4"+
		"\4\u00b5\t\u00b5\4\u00b6\t\u00b6\4\u00b7\t\u00b7\4\u00b8\t\u00b8\4\u00b9"+
		"\t\u00b9\4\u00ba\t\u00ba\4\u00bb\t\u00bb\4\u00bc\t\u00bc\4\u00bd\t\u00bd"+
		"\4\u00be\t\u00be\4\u00bf\t\u00bf\4\u00c0\t\u00c0\4\u00c1\t\u00c1\4\u00c2"+
		"\t\u00c2\4\u00c3\t\u00c3\4\u00c4\t\u00c4\4\u00c5\t\u00c5\4\u00c6\t\u00c6"+
		"\4\u00c7\t\u00c7\4\u00c8\t\u00c8\4\u00c9\t\u00c9\4\u00ca\t\u00ca\4\u00cb"+
		"\t\u00cb\4\u00cc\t\u00cc\4\u00cd\t\u00cd\4\u00ce\t\u00ce\4\u00cf\t\u00cf"+
		"\4\u00d0\t\u00d0\4\u00d1\t\u00d1\4\u00d2\t\u00d2\4\u00d3\t\u00d3\4\u00d4"+
		"\t\u00d4\4\u00d5\t\u00d5\4\u00d6\t\u00d6\4\u00d7\t\u00d7\4\u00d8\t\u00d8"+
		"\4\u00d9\t\u00d9\4\u00da\t\u00da\4\u00db\t\u00db\4\u00dc\t\u00dc\4\u00dd"+
		"\t\u00dd\4\u00de\t\u00de\4\u00df\t\u00df\4\u00e0\t\u00e0\4\u00e1\t\u00e1"+
		"\4\u00e2\t\u00e2\4\u00e3\t\u00e3\4\u00e4\t\u00e4\4\u00e5\t\u00e5\4\u00e6"+
		"\t\u00e6\3\2\3\2\7\2\u01d6\n\2\f\2\16\2\u01d9\13\2\3\2\3\2\3\2\3\2\7\2"+
		"\u01df\n\2\f\2\16\2\u01e2\13\2\3\2\3\2\3\2\3\2\3\2\6\2\u01e9\n\2\r\2\16"+
		"\2\u01ea\3\2\3\2\3\2\3\2\7\2\u01f1\n\2\f\2\16\2\u01f4\13\2\3\2\3\2\3\2"+
		"\3\2\7\2\u01fa\n\2\f\2\16\2\u01fd\13\2\3\2\3\2\3\2\3\2\6\2\u0203\n\2\r"+
		"\2\16\2\u0204\3\2\3\2\5\2\u0209\n\2\3\3\3\3\7\3\u020d\n\3\f\3\16\3\u0210"+
		"\13\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\7\4\u0219\n\4\f\4\16\4\u021c\13\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\7\5\u0227\n\5\f\5\16\5\u022a\13\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\7\6\u0235\n\6\f\6\16\6\u0238\13\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t"+
		"\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\r"+
		"\5\r\u025c\n\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\17"+
		"\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\27\3\27\5\27"+
		"\u0293\n\27\3\30\3\30\5\30\u0297\n\30\3\31\3\31\3\31\3\31\3\31\5\31\u029e"+
		"\n\31\3\32\3\32\3\32\3\32\3\32\5\32\u02a5\n\32\3\33\3\33\3\33\3\33\3\33"+
		"\5\33\u02ac\n\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u02b7"+
		"\n\34\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3 \3 \3 \3 \3"+
		" \3 \3!\3!\3!\3!\3!\3!\3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3"+
		"#\3#\3#\5#\u02e0\n#\3$\3$\3$\3$\3$\3$\3$\3$\3$\3%\3%\3%\3%\3%\3%\3%\3"+
		"&\3&\3&\3&\3&\3&\3&\3&\3\'\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\3(\3)\3"+
		")\3)\3)\3)\3*\3*\3*\3*\3*\3+\3+\3+\3+\3+\3+\3,\3,\3,\3,\3,\3-\3-\3-\3"+
		"-\3-\3-\3.\3.\3.\3.\3.\3.\3/\3/\3/\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3"+
		"\60\3\60\3\60\3\60\3\60\3\61\3\61\3\61\3\62\3\62\3\62\3\62\3\62\3\62\3"+
		"\62\3\63\3\63\3\63\3\63\3\63\3\64\3\64\3\64\3\64\3\64\3\65\3\65\3\65\3"+
		"\65\3\65\3\65\3\65\3\65\3\66\3\66\3\66\3\66\3\66\3\66\3\67\3\67\3\67\3"+
		"\67\3\67\3\67\3\67\3\67\38\38\38\38\38\38\39\39\39\39\3:\3:\3:\3;\3;\3"+
		";\3;\3;\3<\3<\3<\3<\3<\3<\3<\3<\3<\3<\3<\3=\3=\3=\3=\3=\3=\3=\3>\3>\3"+
		">\3>\3>\3>\3>\3>\3>\3>\3>\3?\3?\3?\3?\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3"+
		"A\3A\3A\3A\3A\3B\3B\3B\3B\3B\3B\3B\3C\3C\3C\3C\3D\3D\3D\3D\3D\3D\3D\3"+
		"D\3D\3D\3D\3E\3E\3E\3E\3E\3E\3E\3E\3F\3F\3F\3F\3F\3F\3F\3F\3G\3G\3G\3"+
		"G\3G\3G\3G\3G\3H\3H\3H\3H\3H\3H\3H\3H\3H\3H\3I\3I\3I\3I\3I\3I\3I\3J\3"+
		"J\3J\3J\3J\3J\3J\3K\3K\3K\3K\3K\3K\3K\3L\3L\3L\3L\3L\3L\3L\3M\3M\3M\3"+
		"M\3M\3M\3N\3N\3N\3N\3N\3N\3N\3O\3O\3O\3O\3O\3O\3O\3O\3O\3P\3P\3P\3P\3"+
		"P\3P\3Q\3Q\3Q\3Q\3Q\3Q\3Q\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3S\3"+
		"S\3S\3S\3S\3T\3T\3T\3T\3T\3T\3U\3U\3U\3U\3U\3U\3U\3V\3V\3V\3V\3V\3V\3"+
		"V\3V\3V\3V\3W\3W\3W\3W\3X\3X\3X\3X\3X\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Z\3"+
		"Z\3Z\3Z\3Z\3Z\3[\3[\3[\3[\5[\u0460\n[\3[\3[\3[\5[\u0465\n[\3[\3[\3[\6"+
		"[\u046a\n[\r[\16[\u046b\3[\3[\5[\u0470\n[\5[\u0472\n[\3\\\3\\\3]\3]\5"+
		"]\u0478\n]\3^\3^\5^\u047c\n^\3_\3_\5_\u0480\n_\3`\3`\5`\u0484\n`\3a\3"+
		"a\3b\3b\3b\5b\u048b\nb\3b\3b\3b\5b\u0490\nb\5b\u0492\nb\3c\3c\7c\u0496"+
		"\nc\fc\16c\u0499\13c\3c\5c\u049c\nc\3d\3d\5d\u04a0\nd\3e\3e\3f\3f\5f\u04a6"+
		"\nf\3g\6g\u04a9\ng\rg\16g\u04aa\3h\3h\3i\3i\3i\3i\3j\3j\7j\u04b5\nj\f"+
		"j\16j\u04b8\13j\3j\5j\u04bb\nj\3k\3k\3l\3l\5l\u04c1\nl\3m\3m\5m\u04c5"+
		"\nm\3m\3m\3n\3n\7n\u04cb\nn\fn\16n\u04ce\13n\3n\5n\u04d1\nn\3o\3o\3p\3"+
		"p\5p\u04d7\np\3q\3q\3q\3q\3r\3r\7r\u04df\nr\fr\16r\u04e2\13r\3r\5r\u04e5"+
		"\nr\3s\3s\3t\3t\5t\u04eb\nt\3u\3u\5u\u04ef\nu\3u\3u\3u\5u\u04f4\nu\3v"+
		"\5v\u04f7\nv\3v\3v\3v\5v\u04fc\nv\3v\5v\u04ff\nv\3v\3v\3v\5v\u0504\nv"+
		"\3v\3v\3v\5v\u0509\nv\3w\3w\3w\3x\3x\3y\5y\u0511\ny\3y\3y\3z\3z\3{\3{"+
		"\3|\3|\3|\5|\u051c\n|\3}\3}\5}\u0520\n}\3}\3}\3}\5}\u0525\n}\3}\3}\3}"+
		"\5}\u052a\n}\3~\3~\3~\3\177\3\177\3\u0080\3\u0080\3\u0081\3\u0081\3\u0081"+
		"\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\5\u0081\u053c\n\u0081"+
		"\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\3\u0082\5\u0082\u0545"+
		"\n\u0082\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083"+
		"\3\u0083\3\u0083\3\u0083\3\u0083\5\u0083\u0553\n\u0083\3\u0084\3\u0084"+
		"\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084\3\u0085\3\u0085\3\u0086\3\u0086"+
		"\3\u0086\3\u0087\3\u0087\3\u0087\3\u0088\5\u0088\u0565\n\u0088\3\u0088"+
		"\3\u0088\5\u0088\u0569\n\u0088\3\u0089\3\u0089\3\u0089\3\u008a\3\u008a"+
		"\3\u008b\3\u008b\3\u008c\3\u008c\3\u008d\3\u008d\3\u008e\3\u008e\3\u008f"+
		"\3\u008f\3\u008f\3\u008f\3\u0090\3\u0090\3\u0090\3\u0090\3\u0091\3\u0091"+
		"\3\u0091\3\u0092\3\u0092\3\u0092\3\u0093\3\u0093\3\u0093\3\u0094\3\u0094"+
		"\3\u0094\3\u0095\3\u0095\3\u0095\3\u0095\3\u0095\3\u0096\3\u0096\3\u0096"+
		"\3\u0097\3\u0097\3\u0097\3\u0097\3\u0098\3\u0098\3\u0098\3\u0098\3\u0099"+
		"\3\u0099\3\u0099\3\u0099\3\u0099\3\u009a\3\u009a\3\u009a\3\u009b\3\u009b"+
		"\3\u009b\3\u009c\3\u009c\3\u009c\3\u009c\3\u009c\3\u009c\3\u009c\3\u009d"+
		"\3\u009d\3\u009d\3\u009d\3\u009e\3\u009e\3\u009e\3\u009f\3\u009f\3\u009f"+
		"\3\u00a0\3\u00a0\3\u00a0\3\u00a1\3\u00a1\3\u00a1\3\u00a2\3\u00a2\3\u00a2"+
		"\3\u00a2\3\u00a3\3\u00a3\3\u00a3\3\u00a4\3\u00a4\3\u00a4\3\u00a4\3\u00a5"+
		"\3\u00a5\3\u00a5\3\u00a5\3\u00a6\3\u00a6\3\u00a6\3\u00a6\3\u00a7\3\u00a7"+
		"\3\u00a7\3\u00a7\3\u00a8\3\u00a8\3\u00a8\3\u00a9\3\u00a9\3\u00a9\3\u00a9"+
		"\3\u00a9\3\u00a9\3\u00a9\3\u00a9\3\u00a9\3\u00a9\3\u00a9\3\u00a9\3\u00a9"+
		"\3\u00a9\3\u00aa\3\u00aa\3\u00aa\3\u00aa\3\u00aa\3\u00aa\3\u00ab\3\u00ab"+
		"\3\u00ab\3\u00ab\3\u00ab\3\u00ac\3\u00ac\3\u00ac\3\u00ac\3\u00ac\3\u00ad"+
		"\3\u00ad\3\u00ad\3\u00ad\3\u00ad\3\u00ae\3\u00ae\3\u00ae\3\u00ae\3\u00ae"+
		"\3\u00af\3\u00af\3\u00af\3\u00af\3\u00af\3\u00b0\3\u00b0\3\u00b0\3\u00b0"+
		"\3\u00b0\3\u00b1\3\u00b1\3\u00b2\3\u00b2\3\u00b3\3\u00b3\3\u00b4\3\u00b4"+
		"\3\u00b5\3\u00b5\3\u00b6\3\u00b6\3\u00b7\3\u00b7\3\u00b8\3\u00b8\3\u00b9"+
		"\3\u00b9\3\u00ba\3\u00ba\3\u00bb\3\u00bb\3\u00bb\3\u00bc\3\u00bc\3\u00bc"+
		"\3\u00bd\3\u00bd\3\u00bd\3\u00be\3\u00be\3\u00be\3\u00bf\3\u00bf\3\u00bf"+
		"\3\u00c0\3\u00c0\3\u00c0\3\u00c1\3\u00c1\3\u00c1\3\u00c2\3\u00c2\3\u00c2"+
		"\3\u00c3\3\u00c3\3\u00c4\3\u00c4\3\u00c5\3\u00c5\3\u00c6\3\u00c6\3\u00c7"+
		"\3\u00c7\3\u00c8\3\u00c8\3\u00c9\3\u00c9\3\u00ca\3\u00ca\3\u00cb\3\u00cb"+
		"\3\u00cb\3\u00cc\3\u00cc\3\u00cc\3\u00cd\3\u00cd\3\u00cd\3\u00ce\3\u00ce"+
		"\3\u00ce\3\u00cf\3\u00cf\3\u00cf\3\u00d0\3\u00d0\3\u00d0\3\u00d1\3\u00d1"+
		"\3\u00d1\3\u00d2\3\u00d2\3\u00d2\3\u00d3\3\u00d3\3\u00d3\3\u00d3\3\u00d4"+
		"\3\u00d4\3\u00d4\3\u00d4\3\u00d5\3\u00d5\3\u00d5\3\u00d5\3\u00d5\3\u00d6"+
		"\3\u00d6\3\u00d6\3\u00d7\3\u00d7\3\u00d7\7\u00d7\u0671\n\u00d7\f\u00d7"+
		"\16\u00d7\u0674\13\u00d7\3\u00d8\3\u00d8\7\u00d8\u0678\n\u00d8\f\u00d8"+
		"\16\u00d8\u067b\13\u00d8\3\u00d9\3\u00d9\7\u00d9\u067f\n\u00d9\f\u00d9"+
		"\16\u00d9\u0682\13\u00d9\3\u00da\3\u00da\3\u00da\3\u00da\3\u00da\3\u00da"+
		"\5\u00da\u068a\n\u00da\3\u00db\3\u00db\3\u00db\3\u00dc\3\u00dc\3\u00dc"+
		"\3\u00dc\3\u00dc\3\u00dc\5\u00dc\u0695\n\u00dc\3\u00dd\3\u00dd\3\u00dd"+
		"\3\u00de\7\u00de\u069b\n\u00de\f\u00de\16\u00de\u069e\13\u00de\3\u00df"+
		"\3\u00df\3\u00e0\3\u00e0\3\u00e0\3\u00e0\3\u00e1\6\u00e1\u06a7\n\u00e1"+
		"\r\u00e1\16\u00e1\u06a8\3\u00e1\6\u00e1\u06ac\n\u00e1\r\u00e1\16\u00e1"+
		"\u06ad\5\u00e1\u06b0\n\u00e1\3\u00e1\3\u00e1\3\u00e2\3\u00e2\3\u00e2\3"+
		"\u00e3\3\u00e3\3\u00e3\3\u00e3\7\u00e3\u06bb\n\u00e3\f\u00e3\16\u00e3"+
		"\u06be\13\u00e3\3\u00e3\3\u00e3\3\u00e3\3\u00e3\3\u00e3\3\u00e3\3\u00e3"+
		"\3\u00e4\3\u00e4\3\u00e4\3\u00e4\7\u00e4\u06cb\n\u00e4\f\u00e4\16\u00e4"+
		"\u06ce\13\u00e4\3\u00e4\3\u00e4\3\u00e4\3\u00e4\3\u00e5\3\u00e5\3\u00e5"+
		"\3\u00e5\3\u00e5\3\u00e5\3\u00e5\3\u00e5\3\u00e5\3\u00e5\3\u00e5\7\u00e5"+
		"\u06df\n\u00e5\f\u00e5\16\u00e5\u06e2\13\u00e5\3\u00e5\3\u00e5\3\u00e6"+
		"\3\u00e6\3\u00e6\3\u06bc\2\2\u00e7\t\2\3\13\2\4\r\2\2\17\2\2\21\2\2\23"+
		"\2\5\25\2\6\27\2\2\31\2\2\33\2\2\35\2\2\37\2\2!\2\2#\2\2%\2\2\'\2\2)\2"+
		"\2+\2\2-\2\2/\2\7\61\2\b\63\2\2\65\2\2\67\2\29\2\2;\2\2=\2\2?\2\tA\2\n"+
		"C\2\13E\2\fG\2\rI\2\16K\2\17M\2\20O\2\21Q\2\2S\2\22U\2\23W\2\2Y\2\24["+
		"\2\25]\2\2_\2\26a\2\27c\2\30e\2\31g\2\32i\2\2k\2\33m\2\34o\2\35q\2\36"+
		"s\2\37u\2\2w\2 y\2!{\2\"}\2#\177\2$\u0081\2%\u0083\2\2\u0085\2&\u0087"+
		"\2\2\u0089\2\'\u008b\2(\u008d\2)\u008f\2*\u0091\2+\u0093\2,\u0095\2-\u0097"+
		"\2.\u0099\2/\u009b\2\60\u009d\2\61\u009f\2\2\u00a1\2\62\u00a3\2\63\u00a5"+
		"\2\64\u00a7\2\65\u00a9\2\66\u00ab\2\67\u00ad\28\u00af\29\u00b1\2:\u00b3"+
		"\2;\u00b5\2<\u00b7\2=\u00b9\2>\u00bb\2?\u00bd\2\2\u00bf\2\2\u00c1\2\2"+
		"\u00c3\2\2\u00c5\2\2\u00c7\2\2\u00c9\2\2\u00cb\2\2\u00cd\2\2\u00cf\2\2"+
		"\u00d1\2\2\u00d3\2\2\u00d5\2\2\u00d7\2\2\u00d9\2\2\u00db\2\2\u00dd\2\2"+
		"\u00df\2\2\u00e1\2\2\u00e3\2\2\u00e5\2\2\u00e7\2\2\u00e9\2\2\u00eb\2\2"+
		"\u00ed\2\2\u00ef\2@\u00f1\2\2\u00f3\2\2\u00f5\2\2\u00f7\2\2\u00f9\2\2"+
		"\u00fb\2\2\u00fd\2\2\u00ff\2\2\u0101\2\2\u0103\2\2\u0105\2\2\u0107\2A"+
		"\u0109\2\2\u010b\2\2\u010d\2\2\u010f\2\2\u0111\2\2\u0113\2\2\u0115\2\2"+
		"\u0117\2\2\u0119\2\2\u011b\2\2\u011d\2\2\u011f\2\2\u0121\2\2\u0123\2\2"+
		"\u0125\2\2\u0127\2\2\u0129\2\2\u012b\2\2\u012d\2\2\u012f\2B\u0131\2C\u0133"+
		"\2D\u0135\2E\u0137\2F\u0139\2G\u013b\2H\u013d\2I\u013f\2J\u0141\2K\u0143"+
		"\2L\u0145\2M\u0147\2N\u0149\2O\u014b\2P\u014d\2Q\u014f\2R\u0151\2S\u0153"+
		"\2T\u0155\2U\u0157\2V\u0159\2W\u015b\2X\u015d\2Y\u015f\2Z\u0161\2[\u0163"+
		"\2\\\u0165\2]\u0167\2^\u0169\2_\u016b\2`\u016d\2a\u016f\2b\u0171\2c\u0173"+
		"\2d\u0175\2e\u0177\2f\u0179\2g\u017b\2h\u017d\2i\u017f\2j\u0181\2k\u0183"+
		"\2l\u0185\2m\u0187\2n\u0189\2o\u018b\2p\u018d\2q\u018f\2r\u0191\2s\u0193"+
		"\2t\u0195\2u\u0197\2v\u0199\2w\u019b\2x\u019d\2y\u019f\2z\u01a1\2{\u01a3"+
		"\2|\u01a5\2}\u01a7\2~\u01a9\2\177\u01ab\2\u0080\u01ad\2\u0081\u01af\2"+
		"\u0082\u01b1\2\u0083\u01b3\2\u0084\u01b5\2\u0085\u01b7\2\2\u01b9\2\2\u01bb"+
		"\2\2\u01bd\2\2\u01bf\2\2\u01c1\2\2\u01c3\2\u0086\u01c5\2\u0087\u01c7\2"+
		"\u0088\u01c9\2\u0089\u01cb\2\2\u01cd\2\2\u01cf\2\u008a\u01d1\2\u008b\t"+
		"\2\3\4\5\6\7\b\34\7\2\f\f\17\17$$&&^^\6\2\f\f\17\17))^^\5\2$$&&^^\4\2"+
		"))^^\5\2\2\2&&\61\61\3\2\62;\b\2IIKKNNiikknn\3\2\63;\4\2ZZzz\5\2\62;C"+
		"Hch\3\2\629\4\2DDdd\3\2\62\63\4\2GGgg\4\2--//\6\2FFHIffhi\4\2RRrr\t\2"+
		"$$))^^ddhhpptv\3\2\62\65\6\2&&C\\aac|\4\2\2\u0081\ud802\udc01\3\2\ud802"+
		"\udc01\3\2\udc02\ue001\7\2&&\62;C\\aac|\5\2\f\f\17\17\1\1\4\2\13\13\""+
		"\"\2\u06fa\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2"+
		"\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2Y\3\2\2\2\2["+
		"\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2k\3\2"+
		"\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2"+
		"\2{\3\2\2\2\2}\3\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2\u0085\3\2\2\2\2"+
		"\u0089\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2\2\2\u008f\3\2\2\2\2\u0091"+
		"\3\2\2\2\2\u0093\3\2\2\2\2\u0095\3\2\2\2\2\u0097\3\2\2\2\2\u0099\3\2\2"+
		"\2\2\u009b\3\2\2\2\2\u009d\3\2\2\2\2\u00a1\3\2\2\2\2\u00a3\3\2\2\2\2\u00a5"+
		"\3\2\2\2\2\u00a7\3\2\2\2\2\u00a9\3\2\2\2\2\u00ab\3\2\2\2\2\u00ad\3\2\2"+
		"\2\2\u00af\3\2\2\2\2\u00b1\3\2\2\2\2\u00b3\3\2\2\2\2\u00b5\3\2\2\2\2\u00b7"+
		"\3\2\2\2\2\u00b9\3\2\2\2\2\u00bb\3\2\2\2\2\u00ef\3\2\2\2\2\u0107\3\2\2"+
		"\2\2\u012f\3\2\2\2\2\u0131\3\2\2\2\2\u0133\3\2\2\2\2\u0135\3\2\2\2\2\u0137"+
		"\3\2\2\2\2\u0139\3\2\2\2\2\u013b\3\2\2\2\2\u013d\3\2\2\2\2\u013f\3\2\2"+
		"\2\2\u0141\3\2\2\2\2\u0143\3\2\2\2\2\u0145\3\2\2\2\2\u0147\3\2\2\2\2\u0149"+
		"\3\2\2\2\2\u014b\3\2\2\2\2\u014d\3\2\2\2\2\u014f\3\2\2\2\2\u0151\3\2\2"+
		"\2\2\u0153\3\2\2\2\2\u0155\3\2\2\2\2\u0157\3\2\2\2\2\u0159\3\2\2\2\2\u015b"+
		"\3\2\2\2\2\u015d\3\2\2\2\2\u015f\3\2\2\2\2\u0161\3\2\2\2\2\u0163\3\2\2"+
		"\2\2\u0165\3\2\2\2\2\u0167\3\2\2\2\2\u0169\3\2\2\2\2\u016b\3\2\2\2\2\u016d"+
		"\3\2\2\2\2\u016f\3\2\2\2\2\u0171\3\2\2\2\2\u0173\3\2\2\2\2\u0175\3\2\2"+
		"\2\2\u0177\3\2\2\2\2\u0179\3\2\2\2\2\u017b\3\2\2\2\2\u017d\3\2\2\2\2\u017f"+
		"\3\2\2\2\2\u0181\3\2\2\2\2\u0183\3\2\2\2\2\u0185\3\2\2\2\2\u0187\3\2\2"+
		"\2\2\u0189\3\2\2\2\2\u018b\3\2\2\2\2\u018d\3\2\2\2\2\u018f\3\2\2\2\2\u0191"+
		"\3\2\2\2\2\u0193\3\2\2\2\2\u0195\3\2\2\2\2\u0197\3\2\2\2\2\u0199\3\2\2"+
		"\2\2\u019b\3\2\2\2\2\u019d\3\2\2\2\2\u019f\3\2\2\2\2\u01a1\3\2\2\2\2\u01a3"+
		"\3\2\2\2\2\u01a5\3\2\2\2\2\u01a7\3\2\2\2\2\u01a9\3\2\2\2\2\u01ab\3\2\2"+
		"\2\2\u01ad\3\2\2\2\2\u01af\3\2\2\2\2\u01b1\3\2\2\2\2\u01b3\3\2\2\2\2\u01b5"+
		"\3\2\2\2\2\u01c3\3\2\2\2\2\u01c5\3\2\2\2\2\u01c7\3\2\2\2\2\u01c9\3\2\2"+
		"\2\2\u01cb\3\2\2\2\2\u01cd\3\2\2\2\2\u01cf\3\2\2\2\2\u01d1\3\2\2\2\3\23"+
		"\3\2\2\2\3\25\3\2\2\2\3\27\3\2\2\2\4\31\3\2\2\2\4\33\3\2\2\2\4\35\3\2"+
		"\2\2\5\37\3\2\2\2\5!\3\2\2\2\5#\3\2\2\2\6%\3\2\2\2\6\'\3\2\2\2\6)\3\2"+
		"\2\2\7+\3\2\2\2\7-\3\2\2\2\b/\3\2\2\2\b\61\3\2\2\2\t\u0208\3\2\2\2\13"+
		"\u020a\3\2\2\2\r\u0216\3\2\2\2\17\u0223\3\2\2\2\21\u0232\3\2\2\2\23\u0240"+
		"\3\2\2\2\25\u0244\3\2\2\2\27\u0248\3\2\2\2\31\u024c\3\2\2\2\33\u0251\3"+
		"\2\2\2\35\u0256\3\2\2\2\37\u025b\3\2\2\2!\u0262\3\2\2\2#\u0268\3\2\2\2"+
		"%\u026c\3\2\2\2\'\u0271\3\2\2\2)\u0277\3\2\2\2+\u027b\3\2\2\2-\u0282\3"+
		"\2\2\2/\u0288\3\2\2\2\61\u028b\3\2\2\2\63\u0292\3\2\2\2\65\u0296\3\2\2"+
		"\2\67\u029d\3\2\2\29\u02a4\3\2\2\2;\u02ab\3\2\2\2=\u02b6\3\2\2\2?\u02b8"+
		"\3\2\2\2A\u02bb\3\2\2\2C\u02bf\3\2\2\2E\u02c2\3\2\2\2G\u02c8\3\2\2\2I"+
		"\u02d3\3\2\2\2K\u02df\3\2\2\2M\u02e1\3\2\2\2O\u02ea\3\2\2\2Q\u02f1\3\2"+
		"\2\2S\u02f9\3\2\2\2U\u02ff\3\2\2\2W\u0305\3\2\2\2Y\u030a\3\2\2\2[\u030f"+
		"\3\2\2\2]\u0315\3\2\2\2_\u031a\3\2\2\2a\u0320\3\2\2\2c\u0326\3\2\2\2e"+
		"\u032f\3\2\2\2g\u0337\3\2\2\2i\u033a\3\2\2\2k\u0341\3\2\2\2m\u0346\3\2"+
		"\2\2o\u034b\3\2\2\2q\u0353\3\2\2\2s\u0359\3\2\2\2u\u0361\3\2\2\2w\u0367"+
		"\3\2\2\2y\u036b\3\2\2\2{\u036e\3\2\2\2}\u0373\3\2\2\2\177\u037e\3\2\2"+
		"\2\u0081\u0385\3\2\2\2\u0083\u0390\3\2\2\2\u0085\u0394\3\2\2\2\u0087\u039e"+
		"\3\2\2\2\u0089\u03a3\3\2\2\2\u008b\u03aa\3\2\2\2\u008d\u03ae\3\2\2\2\u008f"+
		"\u03b9\3\2\2\2\u0091\u03c1\3\2\2\2\u0093\u03c9\3\2\2\2\u0095\u03d1\3\2"+
		"\2\2\u0097\u03db\3\2\2\2\u0099\u03e2\3\2\2\2\u009b\u03e9\3\2\2\2\u009d"+
		"\u03f0\3\2\2\2\u009f\u03f7\3\2\2\2\u00a1\u03fd\3\2\2\2\u00a3\u0404\3\2"+
		"\2\2\u00a5\u040d\3\2\2\2\u00a7\u0413\3\2\2\2\u00a9\u041a\3\2\2\2\u00ab"+
		"\u0427\3\2\2\2\u00ad\u042c\3\2\2\2\u00af\u0432\3\2\2\2\u00b1\u0439\3\2"+
		"\2\2\u00b3\u0443\3\2\2\2\u00b5\u0447\3\2\2\2\u00b7\u044c\3\2\2\2\u00b9"+
		"\u0455\3\2\2\2\u00bb\u0471\3\2\2\2\u00bd\u0473\3\2\2\2\u00bf\u0475\3\2"+
		"\2\2\u00c1\u0479\3\2\2\2\u00c3\u047d\3\2\2\2\u00c5\u0481\3\2\2\2\u00c7"+
		"\u0485\3\2\2\2\u00c9\u0491\3\2\2\2\u00cb\u0493\3\2\2\2\u00cd\u049f\3\2"+
		"\2\2\u00cf\u04a1\3\2\2\2\u00d1\u04a5\3\2\2\2\u00d3\u04a8\3\2\2\2\u00d5"+
		"\u04ac\3\2\2\2\u00d7\u04ae\3\2\2\2\u00d9\u04b2\3\2\2\2\u00db\u04bc\3\2"+
		"\2\2\u00dd\u04c0\3\2\2\2\u00df\u04c2\3\2\2\2\u00e1\u04c8\3\2\2\2\u00e3"+
		"\u04d2\3\2\2\2\u00e5\u04d6\3\2\2\2\u00e7\u04d8\3\2\2\2\u00e9\u04dc\3\2"+
		"\2\2\u00eb\u04e6\3\2\2\2\u00ed\u04ea\3\2\2\2\u00ef\u04ee\3\2\2\2\u00f1"+
		"\u0508\3\2\2\2\u00f3\u050a\3\2\2\2\u00f5\u050d\3\2\2\2\u00f7\u0510\3\2"+
		"\2\2\u00f9\u0514\3\2\2\2\u00fb\u0516\3\2\2\2\u00fd\u0518\3\2\2\2\u00ff"+
		"\u0529\3\2\2\2\u0101\u052b\3\2\2\2\u0103\u052e\3\2\2\2\u0105\u0530\3\2"+
		"\2\2\u0107\u053b\3\2\2\2\u0109\u0544\3\2\2\2\u010b\u0552\3\2\2\2\u010d"+
		"\u0554\3\2\2\2\u010f\u055b\3\2\2\2\u0111\u055d\3\2\2\2\u0113\u0560\3\2"+
		"\2\2\u0115\u0568\3\2\2\2\u0117\u056a\3\2\2\2\u0119\u056d\3\2\2\2\u011b"+
		"\u056f\3\2\2\2\u011d\u0571\3\2\2\2\u011f\u0573\3\2\2\2\u0121\u0575\3\2"+
		"\2\2\u0123\u0577\3\2\2\2\u0125\u057b\3\2\2\2\u0127\u057f\3\2\2\2\u0129"+
		"\u0582\3\2\2\2\u012b\u0585\3\2\2\2\u012d\u0588\3\2\2\2\u012f\u058b\3\2"+
		"\2\2\u0131\u0590\3\2\2\2\u0133\u0593\3\2\2\2\u0135\u0597\3\2\2\2\u0137"+
		"\u059b\3\2\2\2\u0139\u05a0\3\2\2\2\u013b\u05a3\3\2\2\2\u013d\u05a6\3\2"+
		"\2\2\u013f\u05ad\3\2\2\2\u0141\u05b1\3\2\2\2\u0143\u05b4\3\2\2\2\u0145"+
		"\u05b7\3\2\2\2\u0147\u05ba\3\2\2\2\u0149\u05bd\3\2\2\2\u014b\u05c1\3\2"+
		"\2\2\u014d\u05c4\3\2\2\2\u014f\u05c8\3\2\2\2\u0151\u05cc\3\2\2\2\u0153"+
		"\u05d0\3\2\2\2\u0155\u05d4\3\2\2\2\u0157\u05d7\3\2\2\2\u0159\u05e5\3\2"+
		"\2\2\u015b\u05eb\3\2\2\2\u015d\u05f0\3\2\2\2\u015f\u05f5\3\2\2\2\u0161"+
		"\u05fa\3\2\2\2\u0163\u05ff\3\2\2\2\u0165\u0604\3\2\2\2\u0167\u0609\3\2"+
		"\2\2\u0169\u060b\3\2\2\2\u016b\u060d\3\2\2\2\u016d\u060f\3\2\2\2\u016f"+
		"\u0611\3\2\2\2\u0171\u0613\3\2\2\2\u0173\u0615\3\2\2\2\u0175\u0617\3\2"+
		"\2\2\u0177\u0619\3\2\2\2\u0179\u061b\3\2\2\2\u017b\u061d\3\2\2\2\u017d"+
		"\u0620\3\2\2\2\u017f\u0623\3\2\2\2\u0181\u0626\3\2\2\2\u0183\u0629\3\2"+
		"\2\2\u0185\u062c\3\2\2\2\u0187\u062f\3\2\2\2\u0189\u0632\3\2\2\2\u018b"+
		"\u0635\3\2\2\2\u018d\u0637\3\2\2\2\u018f\u0639\3\2\2\2\u0191\u063b\3\2"+
		"\2\2\u0193\u063d\3\2\2\2\u0195\u063f\3\2\2\2\u0197\u0641\3\2\2\2\u0199"+
		"\u0643\3\2\2\2\u019b\u0645\3\2\2\2\u019d\u0648\3\2\2\2\u019f\u064b\3\2"+
		"\2\2\u01a1\u064e\3\2\2\2\u01a3\u0651\3\2\2\2\u01a5\u0654\3\2\2\2\u01a7"+
		"\u0657\3\2\2\2\u01a9\u065a\3\2\2\2\u01ab\u065d\3\2\2\2\u01ad\u0661\3\2"+
		"\2\2\u01af\u0665\3\2\2\2\u01b1\u066a\3\2\2\2\u01b3\u066d\3\2\2\2\u01b5"+
		"\u0675\3\2\2\2\u01b7\u067c\3\2\2\2\u01b9\u0689\3\2\2\2\u01bb\u068b\3\2"+
		"\2\2\u01bd\u0694\3\2\2\2\u01bf\u0696\3\2\2\2\u01c1\u069c\3\2\2\2\u01c3"+
		"\u069f\3\2\2\2\u01c5\u06a1\3\2\2\2\u01c7\u06af\3\2\2\2\u01c9\u06b3\3\2"+
		"\2\2\u01cb\u06b6\3\2\2\2\u01cd\u06c6\3\2\2\2\u01cf\u06d3\3\2\2\2\u01d1"+
		"\u06e5\3\2\2\2\u01d3\u01d7\5\u011f\u008d\2\u01d4\u01d6\5\63\27\2\u01d5"+
		"\u01d4\3\2\2\2\u01d6\u01d9\3\2\2\2\u01d7\u01d5\3\2\2\2\u01d7\u01d8\3\2"+
		"\2\2\u01d8\u01da\3\2\2\2\u01d9\u01d7\3\2\2\2\u01da\u01db\5\u011f\u008d"+
		"\2\u01db\u0209\3\2\2\2\u01dc\u01e0\5\u0121\u008e\2\u01dd\u01df\5\65\30"+
		"\2\u01de\u01dd\3\2\2\2\u01df\u01e2\3\2\2\2\u01e0\u01de\3\2\2\2\u01e0\u01e1"+
		"\3\2\2\2\u01e1\u01e3\3\2\2\2\u01e2\u01e0\3\2\2\2\u01e3\u01e4\5\u0121\u008e"+
		"\2\u01e4\u0209\3\2\2\2\u01e5\u01e6\5\u011b\u008b\2\u01e6\u01e8\6\2\2\2"+
		"\u01e7\u01e9\5;\33\2\u01e8\u01e7\3\2\2\2\u01e9\u01ea\3\2\2\2\u01ea\u01e8"+
		"\3\2\2\2\u01ea\u01eb\3\2\2\2\u01eb\u01ec\3\2\2\2\u01ec\u01ed\5\u011b\u008b"+
		"\2\u01ed\u0209\3\2\2\2\u01ee\u01f2\5\u0123\u008f\2\u01ef\u01f1\5\67\31"+
		"\2\u01f0\u01ef\3\2\2\2\u01f1\u01f4\3\2\2\2\u01f2\u01f0\3\2\2\2\u01f2\u01f3"+
		"\3\2\2\2\u01f3\u01f5\3\2\2\2\u01f4\u01f2\3\2\2\2\u01f5\u01f6\5\u0123\u008f"+
		"\2\u01f6\u0209\3\2\2\2\u01f7\u01fb\5\u0125\u0090\2\u01f8\u01fa\59\32\2"+
		"\u01f9\u01f8\3\2\2\2\u01fa\u01fd\3\2\2\2\u01fb\u01f9\3\2\2\2\u01fb\u01fc"+
		"\3\2\2\2\u01fc\u01fe\3\2\2\2\u01fd\u01fb\3\2\2\2\u01fe\u01ff\5\u0125\u0090"+
		"\2\u01ff\u0209\3\2\2\2\u0200\u0202\5\u0127\u0091\2\u0201\u0203\5=\34\2"+
		"\u0202\u0201\3\2\2\2\u0203\u0204\3\2\2\2\u0204\u0202\3\2\2\2\u0204\u0205"+
		"\3\2\2\2\u0205\u0206\3\2\2\2\u0206\u0207\5\u0129\u0092\2\u0207\u0209\3"+
		"\2\2\2\u0208\u01d3\3\2\2\2\u0208\u01dc\3\2\2\2\u0208\u01e5\3\2\2\2\u0208"+
		"\u01ee\3\2\2\2\u0208\u01f7\3\2\2\2\u0208\u0200\3\2\2\2\u0209\n\3\2\2\2"+
		"\u020a\u020e\5\u011f\u008d\2\u020b\u020d\5\63\27\2\u020c\u020b\3\2\2\2"+
		"\u020d\u0210\3\2\2\2\u020e\u020c\3\2\2\2\u020e\u020f\3\2\2\2\u020f\u0211"+
		"\3\2\2\2\u0210\u020e\3\2\2\2\u0211\u0212\5\u011d\u008c\2\u0212\u0213\3"+
		"\2\2\2\u0213\u0214\b\3\2\2\u0214\u0215\b\3\3\2\u0215\f\3\2\2\2\u0216\u021a"+
		"\5\u0123\u008f\2\u0217\u0219\5\67\31\2\u0218\u0217\3\2\2\2\u0219\u021c"+
		"\3\2\2\2\u021a\u0218\3\2\2\2\u021a\u021b\3\2\2\2\u021b\u021d\3\2\2\2\u021c"+
		"\u021a\3\2\2\2\u021d\u021e\5\u011d\u008c\2\u021e\u021f\3\2\2\2\u021f\u0220"+
		"\b\4\4\2\u0220\u0221\b\4\5\2\u0221\u0222\b\4\3\2\u0222\16\3\2\2\2\u0223"+
		"\u0224\5\u011b\u008b\2\u0224\u0228\6\5\3\2\u0225\u0227\5;\33\2\u0226\u0225"+
		"\3\2\2\2\u0227\u022a\3\2\2\2\u0228\u0226\3\2\2\2\u0228\u0229\3\2\2\2\u0229"+
		"\u022b\3\2\2\2\u022a\u0228\3\2\2\2\u022b\u022c\5\u011d\u008c\2\u022c\u022d"+
		"\6\5\4\2\u022d\u022e\3\2\2\2\u022e\u022f\b\5\4\2\u022f\u0230\b\5\6\2\u0230"+
		"\u0231\b\5\3\2\u0231\20\3\2\2\2\u0232\u0236\5\u0127\u0091\2\u0233\u0235"+
		"\5=\34\2\u0234\u0233\3\2\2\2\u0235\u0238\3\2\2\2\u0236\u0234\3\2\2\2\u0236"+
		"\u0237\3\2\2\2\u0237\u0239\3\2\2\2\u0238\u0236\3\2\2\2\u0239\u023a\5\u011d"+
		"\u008c\2\u023a\u023b\6\6\5\2\u023b\u023c\3\2\2\2\u023c\u023d\b\6\4\2\u023d"+
		"\u023e\b\6\7\2\u023e\u023f\b\6\3\2\u023f\22\3\2\2\2\u0240\u0241\5\u011f"+
		"\u008d\2\u0241\u0242\3\2\2\2\u0242\u0243\b\7\b\2\u0243\24\3\2\2\2\u0244"+
		"\u0245\5\u011d\u008c\2\u0245\u0246\3\2\2\2\u0246\u0247\b\b\3\2\u0247\26"+
		"\3\2\2\2\u0248\u0249\5\63\27\2\u0249\u024a\3\2\2\2\u024a\u024b\b\t\t\2"+
		"\u024b\30\3\2\2\2\u024c\u024d\5\u0123\u008f\2\u024d\u024e\3\2\2\2\u024e"+
		"\u024f\b\n\n\2\u024f\u0250\b\n\b\2\u0250\32\3\2\2\2\u0251\u0252\5\u011d"+
		"\u008c\2\u0252\u0253\3\2\2\2\u0253\u0254\b\13\13\2\u0254\u0255\b\13\3"+
		"\2\u0255\34\3\2\2\2\u0256\u0257\5\67\31\2\u0257\u0258\3\2\2\2\u0258\u0259"+
		"\b\f\t\2\u0259\36\3\2\2\2\u025a\u025c\5\u011d\u008c\2\u025b\u025a\3\2"+
		"\2\2\u025b\u025c\3\2\2\2\u025c\u025d\3\2\2\2\u025d\u025e\5\u011b\u008b"+
		"\2\u025e\u025f\3\2\2\2\u025f\u0260\b\r\n\2\u0260\u0261\b\r\b\2\u0261 "+
		"\3\2\2\2\u0262\u0263\5\u011d\u008c\2\u0263\u0264\6\16\6\2\u0264\u0265"+
		"\3\2\2\2\u0265\u0266\b\16\13\2\u0266\u0267\b\16\3\2\u0267\"\3\2\2\2\u0268"+
		"\u0269\5;\33\2\u0269\u026a\3\2\2\2\u026a\u026b\b\17\t\2\u026b$\3\2\2\2"+
		"\u026c\u026d\5\u0129\u0092\2\u026d\u026e\3\2\2\2\u026e\u026f\b\20\n\2"+
		"\u026f\u0270\b\20\b\2\u0270&\3\2\2\2\u0271\u0272\5\u011d\u008c\2\u0272"+
		"\u0273\6\21\7\2\u0273\u0274\3\2\2\2\u0274\u0275\b\21\13\2\u0275\u0276"+
		"\b\21\3\2\u0276(\3\2\2\2\u0277\u0278\5=\34\2\u0278\u0279\3\2\2\2\u0279"+
		"\u027a\b\22\t\2\u027a*\3\2\2\2\u027b\u027c\7}\2\2\u027c\u027d\b\23\f\2"+
		"\u027d\u027e\3\2\2\2\u027e\u027f\b\23\r\2\u027f\u0280\b\23\b\2\u0280\u0281"+
		"\b\23\16\2\u0281,\3\2\2\2\u0282\u0283\5\u01b7\u00d9\2\u0283\u0284\3\2"+
		"\2\2\u0284\u0285\b\24\17\2\u0285\u0286\b\24\b\2\u0286\u0287\b\24\20\2"+
		"\u0287.\3\2\2\2\u0288\u0289\5\u0105\u0080\2\u0289\u028a\5\u01b7\u00d9"+
		"\2\u028a\60\3\2\2\2\u028b\u028c\13\2\2\2\u028c\u028d\b\26\21\2\u028d\u028e"+
		"\3\2\2\2\u028e\u028f\b\26\b\2\u028f\62\3\2\2\2\u0290\u0293\n\2\2\2\u0291"+
		"\u0293\5\u0109\u0082\2\u0292\u0290\3\2\2\2\u0292\u0291\3\2\2\2\u0293\64"+
		"\3\2\2\2\u0294\u0297\n\3\2\2\u0295\u0297\5\u0109\u0082\2\u0296\u0294\3"+
		"\2\2\2\u0296\u0295\3\2\2\2\u0297\66\3\2\2\2\u0298\u029e\n\4\2\2\u0299"+
		"\u029a\5\u011f\u008d\2\u029a\u029b\6\31\b\2\u029b\u029e\3\2\2\2\u029c"+
		"\u029e\5\u0109\u0082\2\u029d\u0298\3\2\2\2\u029d\u0299\3\2\2\2\u029d\u029c"+
		"\3\2\2\2\u029e8\3\2\2\2\u029f\u02a5\n\5\2\2\u02a0\u02a1\5\u0121\u008e"+
		"\2\u02a1\u02a2\6\32\t\2\u02a2\u02a5\3\2\2\2\u02a3\u02a5\5\u0109\u0082"+
		"\2\u02a4\u029f\3\2\2\2\u02a4\u02a0\3\2\2\2\u02a4\u02a3\3\2\2\2\u02a5:"+
		"\3\2\2\2\u02a6\u02ac\5\u0117\u0089\2\u02a7\u02a8\5\u011d\u008c\2\u02a8"+
		"\u02a9\6\33\n\2\u02a9\u02ac\3\2\2\2\u02aa\u02ac\n\6\2\2\u02ab\u02a6\3"+
		"\2\2\2\u02ab\u02a7\3\2\2\2\u02ab\u02aa\3\2\2\2\u02ac<\3\2\2\2\u02ad\u02b7"+
		"\5\u012b\u0093\2\u02ae\u02b7\5\u012d\u0094\2\u02af\u02b0\5\u011b\u008b"+
		"\2\u02b0\u02b1\6\34\13\2\u02b1\u02b7\3\2\2\2\u02b2\u02b3\5\u011d\u008c"+
		"\2\u02b3\u02b4\6\34\f\2\u02b4\u02b7\3\2\2\2\u02b5\u02b7\n\6\2\2\u02b6"+
		"\u02ad\3\2\2\2\u02b6\u02ae\3\2\2\2\u02b6\u02af\3\2\2\2\u02b6\u02b2\3\2"+
		"\2\2\u02b6\u02b5\3\2\2\2\u02b7>\3\2\2\2\u02b8\u02b9\7c\2\2\u02b9\u02ba"+
		"\7u\2\2\u02ba@\3\2\2\2\u02bb\u02bc\7f\2\2\u02bc\u02bd\7g\2\2\u02bd\u02be"+
		"\7h\2\2\u02beB\3\2\2\2\u02bf\u02c0\7k\2\2\u02c0\u02c1\7p\2\2\u02c1D\3"+
		"\2\2\2\u02c2\u02c3\7v\2\2\u02c3\u02c4\7t\2\2\u02c4\u02c5\7c\2\2\u02c5"+
		"\u02c6\7k\2\2\u02c6\u02c7\7v\2\2\u02c7F\3\2\2\2\u02c8\u02c9\7v\2\2\u02c9"+
		"\u02ca\7j\2\2\u02ca\u02cb\7t\2\2\u02cb\u02cc\7g\2\2\u02cc\u02cd\7c\2\2"+
		"\u02cd\u02ce\7f\2\2\u02ce\u02cf\7u\2\2\u02cf\u02d0\7c\2\2\u02d0\u02d1"+
		"\7h\2\2\u02d1\u02d2\7g\2\2\u02d2H\3\2\2\2\u02d3\u02d4\7x\2\2\u02d4\u02d5"+
		"\7c\2\2\u02d5\u02d6\7t\2\2\u02d6J\3\2\2\2\u02d7\u02e0\5Q&\2\u02d8\u02e0"+
		"\5],\2\u02d9\u02e0\5W)\2\u02da\u02e0\5\u009fM\2\u02db\u02e0\5\u0083?\2"+
		"\u02dc\u02e0\5\u0087A\2\u02dd\u02e0\5u8\2\u02de\u02e0\5i\62\2\u02df\u02d7"+
		"\3\2\2\2\u02df\u02d8\3\2\2\2\u02df\u02d9\3\2\2\2\u02df\u02da\3\2\2\2\u02df"+
		"\u02db\3\2\2\2\u02df\u02dc\3\2\2\2\u02df\u02dd\3\2\2\2\u02df\u02de\3\2"+
		"\2\2\u02e0L\3\2\2\2\u02e1\u02e2\7c\2\2\u02e2\u02e3\7d\2\2\u02e3\u02e4"+
		"\7u\2\2\u02e4\u02e5\7v\2\2\u02e5\u02e6\7t\2\2\u02e6\u02e7\7c\2\2\u02e7"+
		"\u02e8\7e\2\2\u02e8\u02e9\7v\2\2\u02e9N\3\2\2\2\u02ea\u02eb\7c\2\2\u02eb"+
		"\u02ec\7u\2\2\u02ec\u02ed\7u\2\2\u02ed\u02ee\7g\2\2\u02ee\u02ef\7t\2\2"+
		"\u02ef\u02f0\7v\2\2\u02f0P\3\2\2\2\u02f1\u02f2\7d\2\2\u02f2\u02f3\7q\2"+
		"\2\u02f3\u02f4\7q\2\2\u02f4\u02f5\7n\2\2\u02f5\u02f6\7g\2\2\u02f6\u02f7"+
		"\7c\2\2\u02f7\u02f8\7p\2\2\u02f8R\3\2\2\2\u02f9\u02fa\7d\2\2\u02fa\u02fb"+
		"\7t\2\2\u02fb\u02fc\7g\2\2\u02fc\u02fd\7c\2\2\u02fd\u02fe\7m\2\2\u02fe"+
		"T\3\2\2\2\u02ff\u0300\7{\2\2\u0300\u0301\7k\2\2\u0301\u0302\7g\2\2\u0302"+
		"\u0303\7n\2\2\u0303\u0304\7f\2\2\u0304V\3\2\2\2\u0305\u0306\7d\2\2\u0306"+
		"\u0307\7{\2\2\u0307\u0308\7v\2\2\u0308\u0309\7g\2\2\u0309X\3\2\2\2\u030a"+
		"\u030b\7e\2\2\u030b\u030c\7c\2\2\u030c\u030d\7u\2\2\u030d\u030e\7g\2\2"+
		"\u030eZ\3\2\2\2\u030f\u0310\7e\2\2\u0310\u0311\7c\2\2\u0311\u0312\7v\2"+
		"\2\u0312\u0313\7e\2\2\u0313\u0314\7j\2\2\u0314\\\3\2\2\2\u0315\u0316\7"+
		"e\2\2\u0316\u0317\7j\2\2\u0317\u0318\7c\2\2\u0318\u0319\7t\2\2\u0319^"+
		"\3\2\2\2\u031a\u031b\7e\2\2\u031b\u031c\7n\2\2\u031c\u031d\7c\2\2\u031d"+
		"\u031e\7u\2\2\u031e\u031f\7u\2\2\u031f`\3\2\2\2\u0320\u0321\7e\2\2\u0321"+
		"\u0322\7q\2\2\u0322\u0323\7p\2\2\u0323\u0324\7u\2\2\u0324\u0325\7v\2\2"+
		"\u0325b\3\2\2\2\u0326\u0327\7e\2\2\u0327\u0328\7q\2\2\u0328\u0329\7p\2"+
		"\2\u0329\u032a\7v\2\2\u032a\u032b\7k\2\2\u032b\u032c\7p\2\2\u032c\u032d"+
		"\7w\2\2\u032d\u032e\7g\2\2\u032ed\3\2\2\2\u032f\u0330\7f\2\2\u0330\u0331"+
		"\7g\2\2\u0331\u0332\7h\2\2\u0332\u0333\7c\2\2\u0333\u0334\7w\2\2\u0334"+
		"\u0335\7n\2\2\u0335\u0336\7v\2\2\u0336f\3\2\2\2\u0337\u0338\7f\2\2\u0338"+
		"\u0339\7q\2\2\u0339h\3\2\2\2\u033a\u033b\7f\2\2\u033b\u033c\7q\2\2\u033c"+
		"\u033d\7w\2\2\u033d\u033e\7d\2\2\u033e\u033f\7n\2\2\u033f\u0340\7g\2\2"+
		"\u0340j\3\2\2\2\u0341\u0342\7g\2\2\u0342\u0343\7n\2\2\u0343\u0344\7u\2"+
		"\2\u0344\u0345\7g\2\2\u0345l\3\2\2\2\u0346\u0347\7g\2\2\u0347\u0348\7"+
		"p\2\2\u0348\u0349\7w\2\2\u0349\u034a\7o\2\2\u034an\3\2\2\2\u034b\u034c"+
		"\7g\2\2\u034c\u034d\7z\2\2\u034d\u034e\7v\2\2\u034e\u034f\7g\2\2\u034f"+
		"\u0350\7p\2\2\u0350\u0351\7f\2\2\u0351\u0352\7u\2\2\u0352p\3\2\2\2\u0353"+
		"\u0354\7h\2\2\u0354\u0355\7k\2\2\u0355\u0356\7p\2\2\u0356\u0357\7c\2\2"+
		"\u0357\u0358\7n\2\2\u0358r\3\2\2\2\u0359\u035a\7h\2\2\u035a\u035b\7k\2"+
		"\2\u035b\u035c\7p\2\2\u035c\u035d\7c\2\2\u035d\u035e\7n\2\2\u035e\u035f"+
		"\7n\2\2\u035f\u0360\7{\2\2\u0360t\3\2\2\2\u0361\u0362\7h\2\2\u0362\u0363"+
		"\7n\2\2\u0363\u0364\7q\2\2\u0364\u0365\7c\2\2\u0365\u0366\7v\2\2\u0366"+
		"v\3\2\2\2\u0367\u0368\7h\2\2\u0368\u0369\7q\2\2\u0369\u036a\7t\2\2\u036a"+
		"x\3\2\2\2\u036b\u036c\7k\2\2\u036c\u036d\7h\2\2\u036dz\3\2\2\2\u036e\u036f"+
		"\7i\2\2\u036f\u0370\7q\2\2\u0370\u0371\7v\2\2\u0371\u0372\7q\2\2\u0372"+
		"|\3\2\2\2\u0373\u0374\7k\2\2\u0374\u0375\7o\2\2\u0375\u0376\7r\2\2\u0376"+
		"\u0377\7n\2\2\u0377\u0378\7g\2\2\u0378\u0379\7o\2\2\u0379\u037a\7g\2\2"+
		"\u037a\u037b\7p\2\2\u037b\u037c\7v\2\2\u037c\u037d\7u\2\2\u037d~\3\2\2"+
		"\2\u037e\u037f\7k\2\2\u037f\u0380\7o\2\2\u0380\u0381\7r\2\2\u0381\u0382"+
		"\7q\2\2\u0382\u0383\7t\2\2\u0383\u0384\7v\2\2\u0384\u0080\3\2\2\2\u0385"+
		"\u0386\7k\2\2\u0386\u0387\7p\2\2\u0387\u0388\7u\2\2\u0388\u0389\7v\2\2"+
		"\u0389\u038a\7c\2\2\u038a\u038b\7p\2\2\u038b\u038c\7e\2\2\u038c\u038d"+
		"\7g\2\2\u038d\u038e\7q\2\2\u038e\u038f\7h\2\2\u038f\u0082\3\2\2\2\u0390"+
		"\u0391\7k\2\2\u0391\u0392\7p\2\2\u0392\u0393\7v\2\2\u0393\u0084\3\2\2"+
		"\2\u0394\u0395\7k\2\2\u0395\u0396\7p\2\2\u0396\u0397\7v\2\2\u0397\u0398"+
		"\7g\2\2\u0398\u0399\7t\2\2\u0399\u039a\7h\2\2\u039a\u039b\7c\2\2\u039b"+
		"\u039c\7e\2\2\u039c\u039d\7g\2\2\u039d\u0086\3\2\2\2\u039e\u039f\7n\2"+
		"\2\u039f\u03a0\7q\2\2\u03a0\u03a1\7p\2\2\u03a1\u03a2\7i\2\2\u03a2\u0088"+
		"\3\2\2\2\u03a3\u03a4\7p\2\2\u03a4\u03a5\7c\2\2\u03a5\u03a6\7v\2\2\u03a6"+
		"\u03a7\7k\2\2\u03a7\u03a8\7x\2\2\u03a8\u03a9\7g\2\2\u03a9\u008a\3\2\2"+
		"\2\u03aa\u03ab\7p\2\2\u03ab\u03ac\7g\2\2\u03ac\u03ad\7y\2\2\u03ad\u008c"+
		"\3\2\2\2\u03ae\u03af\7p\2\2\u03af\u03b0\7q\2\2\u03b0\u03b1\7p\2\2\u03b1"+
		"\u03b2\7/\2\2\u03b2\u03b3\7u\2\2\u03b3\u03b4\7g\2\2\u03b4\u03b5\7c\2\2"+
		"\u03b5\u03b6\7n\2\2\u03b6\u03b7\7g\2\2\u03b7\u03b8\7f\2\2\u03b8\u008e"+
		"\3\2\2\2\u03b9\u03ba\7r\2\2\u03ba\u03bb\7c\2\2\u03bb\u03bc\7e\2\2\u03bc"+
		"\u03bd\7m\2\2\u03bd\u03be\7c\2\2\u03be\u03bf\7i\2\2\u03bf\u03c0\7g\2\2"+
		"\u03c0\u0090\3\2\2\2\u03c1\u03c2\7r\2\2\u03c2\u03c3\7g\2\2\u03c3\u03c4"+
		"\7t\2\2\u03c4\u03c5\7o\2\2\u03c5\u03c6\7k\2\2\u03c6\u03c7\7v\2\2\u03c7"+
		"\u03c8\7u\2\2\u03c8\u0092\3\2\2\2\u03c9\u03ca\7r\2\2\u03ca\u03cb\7t\2"+
		"\2\u03cb\u03cc\7k\2\2\u03cc\u03cd\7x\2\2\u03cd\u03ce\7c\2\2\u03ce\u03cf"+
		"\7v\2\2\u03cf\u03d0\7g\2\2\u03d0\u0094\3\2\2\2\u03d1\u03d2\7r\2\2\u03d2"+
		"\u03d3\7t\2\2\u03d3\u03d4\7q\2\2\u03d4\u03d5\7v\2\2\u03d5\u03d6\7g\2\2"+
		"\u03d6\u03d7\7e\2\2\u03d7\u03d8\7v\2\2\u03d8\u03d9\7g\2\2\u03d9\u03da"+
		"\7f\2\2\u03da\u0096\3\2\2\2\u03db\u03dc\7r\2\2\u03dc\u03dd\7w\2\2\u03dd"+
		"\u03de\7d\2\2\u03de\u03df\7n\2\2\u03df\u03e0\7k\2\2\u03e0\u03e1\7e\2\2"+
		"\u03e1\u0098\3\2\2\2\u03e2\u03e3\7t\2\2\u03e3\u03e4\7g\2\2\u03e4\u03e5"+
		"\7e\2\2\u03e5\u03e6\7q\2\2\u03e6\u03e7\7t\2\2\u03e7\u03e8\7f\2\2\u03e8"+
		"\u009a\3\2\2\2\u03e9\u03ea\7t\2\2\u03ea\u03eb\7g\2\2\u03eb\u03ec\7v\2"+
		"\2\u03ec\u03ed\7w\2\2\u03ed\u03ee\7t\2\2\u03ee\u03ef\7p\2\2\u03ef\u009c"+
		"\3\2\2\2\u03f0\u03f1\7u\2\2\u03f1\u03f2\7g\2\2\u03f2\u03f3\7c\2\2\u03f3"+
		"\u03f4\7n\2\2\u03f4\u03f5\7g\2\2\u03f5\u03f6\7f\2\2\u03f6\u009e\3\2\2"+
		"\2\u03f7\u03f8\7u\2\2\u03f8\u03f9\7j\2\2\u03f9\u03fa\7q\2\2\u03fa\u03fb"+
		"\7t\2\2\u03fb\u03fc\7v\2\2\u03fc\u00a0\3\2\2\2\u03fd\u03fe\7u\2\2\u03fe"+
		"\u03ff\7v\2\2\u03ff\u0400\7c\2\2\u0400\u0401\7v\2\2\u0401\u0402\7k\2\2"+
		"\u0402\u0403\7e\2\2\u0403\u00a2\3\2\2\2\u0404\u0405\7u\2\2\u0405\u0406"+
		"\7v\2\2\u0406\u0407\7t\2\2\u0407\u0408\7k\2\2\u0408\u0409\7e\2\2\u0409"+
		"\u040a\7v\2\2\u040a\u040b\7h\2\2\u040b\u040c\7r\2\2\u040c\u00a4\3\2\2"+
		"\2\u040d\u040e\7u\2\2\u040e\u040f\7w\2\2\u040f\u0410\7r\2\2\u0410\u0411"+
		"\7g\2\2\u0411\u0412\7t\2\2\u0412\u00a6\3\2\2\2\u0413\u0414\7u\2\2\u0414"+
		"\u0415\7y\2\2\u0415\u0416\7k\2\2\u0416\u0417\7v\2\2\u0417\u0418\7e\2\2"+
		"\u0418\u0419\7j\2\2\u0419\u00a8\3\2\2\2\u041a\u041b\7u\2\2\u041b\u041c"+
		"\7{\2\2\u041c\u041d\7p\2\2\u041d\u041e\7e\2\2\u041e\u041f\7j\2\2\u041f"+
		"\u0420\7t\2\2\u0420\u0421\7q\2\2\u0421\u0422\7p\2\2\u0422\u0423\7k\2\2"+
		"\u0423\u0424\7|\2\2\u0424\u0425\7g\2\2\u0425\u0426\7f\2\2\u0426\u00aa"+
		"\3\2\2\2\u0427\u0428\7v\2\2\u0428\u0429\7j\2\2\u0429\u042a\7k\2\2\u042a"+
		"\u042b\7u\2\2\u042b\u00ac\3\2\2\2\u042c\u042d\7v\2\2\u042d\u042e\7j\2"+
		"\2\u042e\u042f\7t\2\2\u042f\u0430\7q\2\2\u0430\u0431\7y\2\2\u0431\u00ae"+
		"\3\2\2\2\u0432\u0433\7v\2\2\u0433\u0434\7j\2\2\u0434\u0435\7t\2\2\u0435"+
		"\u0436\7q\2\2\u0436\u0437\7y\2\2\u0437\u0438\7u\2\2\u0438\u00b0\3\2\2"+
		"\2\u0439\u043a\7v\2\2\u043a\u043b\7t\2\2\u043b\u043c\7c\2\2\u043c\u043d"+
		"\7p\2\2\u043d\u043e\7u\2\2\u043e\u043f\7k\2\2\u043f\u0440\7g\2\2\u0440"+
		"\u0441\7p\2\2\u0441\u0442\7v\2\2\u0442\u00b2\3\2\2\2\u0443\u0444\7v\2"+
		"\2\u0444\u0445\7t\2\2\u0445\u0446\7{\2\2\u0446\u00b4\3\2\2\2\u0447\u0448"+
		"\7x\2\2\u0448\u0449\7q\2\2\u0449\u044a\7k\2\2\u044a\u044b\7f\2\2\u044b"+
		"\u00b6\3\2\2\2\u044c\u044d\7x\2\2\u044d\u044e\7q\2\2\u044e\u044f\7n\2"+
		"\2\u044f\u0450\7c\2\2\u0450\u0451\7v\2\2\u0451\u0452\7k\2\2\u0452\u0453"+
		"\7n\2\2\u0453\u0454\7g\2\2\u0454\u00b8\3\2\2\2\u0455\u0456\7y\2\2\u0456"+
		"\u0457\7j\2\2\u0457\u0458\7k\2\2\u0458\u0459\7n\2\2\u0459\u045a\7g\2\2"+
		"\u045a\u00ba\3\2\2\2\u045b\u0460\5\u00bf]\2\u045c\u0460\5\u00c1^\2\u045d"+
		"\u0460\5\u00c3_\2\u045e\u0460\5\u00c5`\2\u045f\u045b\3\2\2\2\u045f\u045c"+
		"\3\2\2\2\u045f\u045d\3\2\2\2\u045f\u045e\3\2\2\2\u0460\u0464\3\2\2\2\u0461"+
		"\u0462\5\u00d5h\2\u0462\u0463\b[\22\2\u0463\u0465\3\2\2\2\u0464\u0461"+
		"\3\2\2\2\u0464\u0465\3\2\2\2\u0465\u0472\3\2\2\2\u0466\u0469\5\u00bd\\"+
		"\2\u0467\u0468\t\7\2\2\u0468\u046a\b[\23\2\u0469\u0467\3\2\2\2\u046a\u046b"+
		"\3\2\2\2\u046b\u0469\3\2\2\2\u046b\u046c\3\2\2\2\u046c\u046d\3\2\2\2\u046d"+
		"\u046f\b[\24\2\u046e\u0470\5\u00c7a\2\u046f\u046e\3\2\2\2\u046f\u0470"+
		"\3\2\2\2\u0470\u0472\3\2\2\2\u0471\u045f\3\2\2\2\u0471\u0466\3\2\2\2\u0472"+
		"\u00bc\3\2\2\2\u0473\u0474\7\62\2\2\u0474\u00be\3\2\2\2\u0475\u0477\5"+
		"\u00c9b\2\u0476\u0478\5\u00c7a\2\u0477\u0476\3\2\2\2\u0477\u0478\3\2\2"+
		"\2\u0478\u00c0\3\2\2\2\u0479\u047b\5\u00d7i\2\u047a\u047c\5\u00c7a\2\u047b"+
		"\u047a\3\2\2\2\u047b\u047c\3\2\2\2\u047c\u00c2\3\2\2\2\u047d\u047f\5\u00df"+
		"m\2\u047e\u0480\5\u00c7a\2\u047f\u047e\3\2\2\2\u047f\u0480\3\2\2\2\u0480"+
		"\u00c4\3\2\2\2\u0481\u0483\5\u00e7q\2\u0482\u0484\5\u00c7a\2\u0483\u0482"+
		"\3\2\2\2\u0483\u0484\3\2\2\2\u0484\u00c6\3\2\2\2\u0485\u0486\t\b\2\2\u0486"+
		"\u00c8\3\2\2\2\u0487\u0492\5\u00bd\\\2\u0488\u048f\5\u00cfe\2\u0489\u048b"+
		"\5\u00cbc\2\u048a\u0489\3\2\2\2\u048a\u048b\3\2\2\2\u048b\u0490\3\2\2"+
		"\2\u048c\u048d\5\u00d3g\2\u048d\u048e\5\u00cbc\2\u048e\u0490\3\2\2\2\u048f"+
		"\u048a\3\2\2\2\u048f\u048c\3\2\2\2\u0490\u0492\3\2\2\2\u0491\u0487\3\2"+
		"\2\2\u0491\u0488\3\2\2\2\u0492\u00ca\3\2\2\2\u0493\u049b\5\u00cdd\2\u0494"+
		"\u0496\5\u00d1f\2\u0495\u0494\3\2\2\2\u0496\u0499\3\2\2\2\u0497\u0495"+
		"\3\2\2\2\u0497\u0498\3\2\2\2\u0498\u049a\3\2\2\2\u0499\u0497\3\2\2\2\u049a"+
		"\u049c\5\u00cdd\2\u049b\u0497\3\2\2\2\u049b\u049c\3\2\2\2\u049c\u00cc"+
		"\3\2\2\2\u049d\u04a0\5\u00bd\\\2\u049e\u04a0\5\u00cfe\2\u049f\u049d\3"+
		"\2\2\2\u049f\u049e\3\2\2\2\u04a0\u00ce\3\2\2\2\u04a1\u04a2\t\t\2\2\u04a2"+
		"\u00d0\3\2\2\2\u04a3\u04a6\5\u00cdd\2\u04a4\u04a6\5\u00d5h\2\u04a5\u04a3"+
		"\3\2\2\2\u04a5\u04a4\3\2\2\2\u04a6\u00d2\3\2\2\2\u04a7\u04a9\5\u00d5h"+
		"\2\u04a8\u04a7\3\2\2\2\u04a9\u04aa\3\2\2\2\u04aa\u04a8\3\2\2\2\u04aa\u04ab"+
		"\3\2\2\2\u04ab\u00d4\3\2\2\2\u04ac\u04ad\7a\2\2\u04ad\u00d6\3\2\2\2\u04ae"+
		"\u04af\5\u00bd\\\2\u04af\u04b0\t\n\2\2\u04b0\u04b1\5\u00d9j\2\u04b1\u00d8"+
		"\3\2\2\2\u04b2\u04ba\5\u00dbk\2\u04b3\u04b5\5\u00ddl\2\u04b4\u04b3\3\2"+
		"\2\2\u04b5\u04b8\3\2\2\2\u04b6\u04b4\3\2\2\2\u04b6\u04b7\3\2\2\2\u04b7"+
		"\u04b9\3\2\2\2\u04b8\u04b6\3\2\2\2\u04b9\u04bb\5\u00dbk\2\u04ba\u04b6"+
		"\3\2\2\2\u04ba\u04bb\3\2\2\2\u04bb\u00da\3\2\2\2\u04bc\u04bd\t\13\2\2"+
		"\u04bd\u00dc\3\2\2\2\u04be\u04c1\5\u00dbk\2\u04bf\u04c1\5\u00d5h\2\u04c0"+
		"\u04be\3\2\2\2\u04c0\u04bf\3\2\2\2\u04c1\u00de\3\2\2\2\u04c2\u04c4\5\u00bd"+
		"\\\2\u04c3\u04c5\5\u00d3g\2\u04c4\u04c3\3\2\2\2\u04c4\u04c5\3\2\2\2\u04c5"+
		"\u04c6\3\2\2\2\u04c6\u04c7\5\u00e1n\2\u04c7\u00e0\3\2\2\2\u04c8\u04d0"+
		"\5\u00e3o\2\u04c9\u04cb\5\u00e5p\2\u04ca\u04c9\3\2\2\2\u04cb\u04ce\3\2"+
		"\2\2\u04cc\u04ca\3\2\2\2\u04cc\u04cd\3\2\2\2\u04cd\u04cf\3\2\2\2\u04ce"+
		"\u04cc\3\2\2\2\u04cf\u04d1\5\u00e3o\2\u04d0\u04cc\3\2\2\2\u04d0\u04d1"+
		"\3\2\2\2\u04d1\u00e2\3\2\2\2\u04d2\u04d3\t\f\2\2\u04d3\u00e4\3\2\2\2\u04d4"+
		"\u04d7\5\u00e3o\2\u04d5\u04d7\5\u00d5h\2\u04d6\u04d4\3\2\2\2\u04d6\u04d5"+
		"\3\2\2\2\u04d7\u00e6\3\2\2\2\u04d8\u04d9\5\u00bd\\\2\u04d9\u04da\t\r\2"+
		"\2\u04da\u04db\5\u00e9r\2\u04db\u00e8\3\2\2\2\u04dc\u04e4\5\u00ebs\2\u04dd"+
		"\u04df\5\u00edt\2\u04de\u04dd\3\2\2\2\u04df\u04e2\3\2\2\2\u04e0\u04de"+
		"\3\2\2\2\u04e0\u04e1\3\2\2\2\u04e1\u04e3\3\2\2\2\u04e2\u04e0\3\2\2\2\u04e3"+
		"\u04e5\5\u00ebs\2\u04e4\u04e0\3\2\2\2\u04e4\u04e5\3\2\2\2\u04e5\u00ea"+
		"\3\2\2\2\u04e6\u04e7\t\16\2\2\u04e7\u00ec\3\2\2\2\u04e8\u04eb\5\u00eb"+
		"s\2\u04e9\u04eb\5\u00d5h\2\u04ea\u04e8\3\2\2\2\u04ea\u04e9\3\2\2\2\u04eb"+
		"\u00ee\3\2\2\2\u04ec\u04ef\5\u00f1v\2\u04ed\u04ef\5\u00fd|\2\u04ee\u04ec"+
		"\3\2\2\2\u04ee\u04ed\3\2\2\2\u04ef\u04f3\3\2\2\2\u04f0\u04f1\5\u00d5h"+
		"\2\u04f1\u04f2\bu\25\2\u04f2\u04f4\3\2\2\2\u04f3\u04f0\3\2\2\2\u04f3\u04f4"+
		"\3\2\2\2\u04f4\u00f0\3\2\2\2\u04f5\u04f7\5\u00cbc\2\u04f6\u04f5\3\2\2"+
		"\2\u04f6\u04f7\3\2\2\2\u04f7\u04f8\3\2\2\2\u04f8\u04f9\5\u0105\u0080\2"+
		"\u04f9\u04fb\5\u00cbc\2\u04fa\u04fc\5\u00f3w\2\u04fb\u04fa\3\2\2\2\u04fb"+
		"\u04fc\3\2\2\2\u04fc\u04fe\3\2\2\2\u04fd\u04ff\5\u00fb{\2\u04fe\u04fd"+
		"\3\2\2\2\u04fe\u04ff\3\2\2\2\u04ff\u0509\3\2\2\2\u0500\u0501\5\u00cbc"+
		"\2\u0501\u0503\5\u00f3w\2\u0502\u0504\5\u00fb{\2\u0503\u0502\3\2\2\2\u0503"+
		"\u0504\3\2\2\2\u0504\u0509\3\2\2\2\u0505\u0506\5\u00cbc\2\u0506\u0507"+
		"\5\u00fb{\2\u0507\u0509\3\2\2\2\u0508\u04f6\3\2\2\2\u0508\u0500\3\2\2"+
		"\2\u0508\u0505\3\2\2\2\u0509\u00f2\3\2\2\2\u050a\u050b\5\u00f5x\2\u050b"+
		"\u050c\5\u00f7y\2\u050c\u00f4\3\2\2\2\u050d\u050e\t\17\2\2\u050e\u00f6"+
		"\3\2\2\2\u050f\u0511\5\u00f9z\2\u0510\u050f\3\2\2\2\u0510\u0511\3\2\2"+
		"\2\u0511\u0512\3\2\2\2\u0512\u0513\5\u00cbc\2\u0513\u00f8\3\2\2\2\u0514"+
		"\u0515\t\20\2\2\u0515\u00fa\3\2\2\2\u0516\u0517\t\21\2\2\u0517\u00fc\3"+
		"\2\2\2\u0518\u0519\5\u00ff}\2\u0519\u051b\5\u0101~\2\u051a\u051c\5\u00fb"+
		"{\2\u051b\u051a\3\2\2\2\u051b\u051c\3\2\2\2\u051c\u00fe\3\2\2\2\u051d"+
		"\u051f\5\u00d7i\2\u051e\u0520\5\u0105\u0080\2\u051f\u051e\3\2\2\2\u051f"+
		"\u0520\3\2\2\2\u0520\u052a\3\2\2\2\u0521\u0522\5\u00bd\\\2\u0522\u0524"+
		"\t\n\2\2\u0523\u0525\5\u00d9j\2\u0524\u0523\3\2\2\2\u0524\u0525\3\2\2"+
		"\2\u0525\u0526\3\2\2\2\u0526\u0527\5\u0105\u0080\2\u0527\u0528\5\u00d9"+
		"j\2\u0528\u052a\3\2\2\2\u0529\u051d\3\2\2\2\u0529\u0521\3\2\2\2\u052a"+
		"\u0100\3\2\2\2\u052b\u052c\5\u0103\177\2\u052c\u052d\5\u00f7y\2\u052d"+
		"\u0102\3\2\2\2\u052e\u052f\t\22\2\2\u052f\u0104\3\2\2\2\u0530\u0531\7"+
		"\60\2\2\u0531\u0106\3\2\2\2\u0532\u0533\7v\2\2\u0533\u0534\7t\2\2\u0534"+
		"\u0535\7w\2\2\u0535\u053c\7g\2\2\u0536\u0537\7h\2\2\u0537\u0538\7c\2\2"+
		"\u0538\u0539\7n\2\2\u0539\u053a\7u\2\2\u053a\u053c\7g\2\2\u053b\u0532"+
		"\3\2\2\2\u053b\u0536\3\2\2\2\u053c\u0108\3\2\2\2\u053d\u053e\5\u0119\u008a"+
		"\2\u053e\u053f\t\23\2\2\u053f\u0545\3\2\2\2\u0540\u0545\5\u010b\u0083"+
		"\2\u0541\u0545\5\u010d\u0084\2\u0542\u0545\5\u0111\u0086\2\u0543\u0545"+
		"\5\u0113\u0087\2\u0544\u053d\3\2\2\2\u0544\u0540\3\2\2\2\u0544\u0541\3"+
		"\2\2\2\u0544\u0542\3\2\2\2\u0544\u0543\3\2\2\2\u0545\u010a\3\2\2\2\u0546"+
		"\u0547\5\u0119\u008a\2\u0547\u0548\5\u00e3o\2\u0548\u0553\3\2\2\2\u0549"+
		"\u054a\5\u0119\u008a\2\u054a\u054b\5\u00e3o\2\u054b\u054c\5\u00e3o\2\u054c"+
		"\u0553\3\2\2\2\u054d\u054e\5\u0119\u008a\2\u054e\u054f\5\u010f\u0085\2"+
		"\u054f\u0550\5\u00e3o\2\u0550\u0551\5\u00e3o\2\u0551\u0553\3\2\2\2\u0552"+
		"\u0546\3\2\2\2\u0552\u0549\3\2\2\2\u0552\u054d\3\2\2\2\u0553\u010c\3\2"+
		"\2\2\u0554\u0555\5\u0119\u008a\2\u0555\u0556\7w\2\2\u0556\u0557\5\u00db"+
		"k\2\u0557\u0558\5\u00dbk\2\u0558\u0559\5\u00dbk\2\u0559\u055a\5\u00db"+
		"k\2\u055a\u010e\3\2\2\2\u055b\u055c\t\24\2\2\u055c\u0110\3\2\2\2\u055d"+
		"\u055e\5\u0119\u008a\2\u055e\u055f\5\u011d\u008c\2\u055f\u0112\3\2\2\2"+
		"\u0560\u0561\5\u0119\u008a\2\u0561\u0562\5\u0115\u0088\2\u0562\u0114\3"+
		"\2\2\2\u0563\u0565\7\17\2\2\u0564\u0563\3\2\2\2\u0564\u0565\3\2\2\2\u0565"+
		"\u0566\3\2\2\2\u0566\u0569\7\f\2\2\u0567\u0569\7\17\2\2\u0568\u0564\3"+
		"\2\2\2\u0568\u0567\3\2\2\2\u0569\u0116\3\2\2\2\u056a\u056b\5\u0119\u008a"+
		"\2\u056b\u056c\5\u011b\u008b\2\u056c\u0118\3\2\2\2\u056d\u056e\7^\2\2"+
		"\u056e\u011a\3\2\2\2\u056f\u0570\7\61\2\2\u0570\u011c\3\2\2\2\u0571\u0572"+
		"\7&\2\2\u0572\u011e\3\2\2\2\u0573\u0574\7$\2\2\u0574\u0120\3\2\2\2\u0575"+
		"\u0576\7)\2\2\u0576\u0122\3\2\2\2\u0577\u0578\7$\2\2\u0578\u0579\7$\2"+
		"\2\u0579\u057a\7$\2\2\u057a\u0124\3\2\2\2\u057b\u057c\7)\2\2\u057c\u057d"+
		"\7)\2\2\u057d\u057e\7)\2\2\u057e\u0126\3\2\2\2\u057f\u0580\7&\2\2\u0580"+
		"\u0581\7\61\2\2\u0581\u0128\3\2\2\2\u0582\u0583\7\61\2\2\u0583\u0584\7"+
		"&\2\2\u0584\u012a\3\2\2\2\u0585\u0586\7&\2\2\u0586\u0587\7\61\2\2\u0587"+
		"\u012c\3\2\2\2\u0588\u0589\7&\2\2\u0589\u058a\7&\2\2\u058a\u012e\3\2\2"+
		"\2\u058b\u058c\7p\2\2\u058c\u058d\7w\2\2\u058d\u058e\7n\2\2\u058e\u058f"+
		"\7n\2\2\u058f\u0130\3\2\2\2\u0590\u0591\7\60\2\2\u0591\u0592\7\60\2\2"+
		"\u0592\u0132\3\2\2\2\u0593\u0594\7>\2\2\u0594\u0595\7\60\2\2\u0595\u0596"+
		"\7\60\2\2\u0596\u0134\3\2\2\2\u0597\u0598\7\60\2\2\u0598\u0599\7\60\2"+
		"\2\u0599\u059a\7>\2\2\u059a\u0136\3\2\2\2\u059b\u059c\7>\2\2\u059c\u059d"+
		"\7\60\2\2\u059d\u059e\7\60\2\2\u059e\u059f\7>\2\2\u059f\u0138\3\2\2\2"+
		"\u05a0\u05a1\7,\2\2\u05a1\u05a2\7\60\2\2\u05a2\u013a\3\2\2\2\u05a3\u05a4"+
		"\7A\2\2\u05a4\u05a5\7\60\2\2\u05a5\u013c\3\2\2\2\u05a6\u05a7\7A\2\2\u05a7"+
		"\u05a8\7]\2\2\u05a8\u05a9\3\2\2\2\u05a9\u05aa\b\u009c\26\2\u05aa\u05ab"+
		"\3\2\2\2\u05ab\u05ac\b\u009c\16\2\u05ac\u013e\3\2\2\2\u05ad\u05ae\7A\2"+
		"\2\u05ae\u05af\7A\2\2\u05af\u05b0\7\60\2\2\u05b0\u0140\3\2\2\2\u05b1\u05b2"+
		"\7A\2\2\u05b2\u05b3\7<\2\2\u05b3\u0142\3\2\2\2\u05b4\u05b5\7\60\2\2\u05b5"+
		"\u05b6\7(\2\2\u05b6\u0144\3\2\2\2\u05b7\u05b8\7<\2\2\u05b8\u05b9\7<\2"+
		"\2\u05b9\u0146\3\2\2\2\u05ba\u05bb\7?\2\2\u05bb\u05bc\7\u0080\2\2\u05bc"+
		"\u0148\3\2\2\2\u05bd\u05be\7?\2\2\u05be\u05bf\7?\2\2\u05bf\u05c0\7\u0080"+
		"\2\2\u05c0\u014a\3\2\2\2\u05c1\u05c2\7,\2\2\u05c2\u05c3\7,\2\2\u05c3\u014c"+
		"\3\2\2\2\u05c4\u05c5\7,\2\2\u05c5\u05c6\7,\2\2\u05c6\u05c7\7?\2\2\u05c7"+
		"\u014e\3\2\2\2\u05c8\u05c9\7>\2\2\u05c9\u05ca\7?\2\2\u05ca\u05cb\7@\2"+
		"\2\u05cb\u0150\3\2\2\2\u05cc\u05cd\7?\2\2\u05cd\u05ce\7?\2\2\u05ce\u05cf"+
		"\7?\2\2\u05cf\u0152\3\2\2\2\u05d0\u05d1\7#\2\2\u05d1\u05d2\7?\2\2\u05d2"+
		"\u05d3\7?\2\2\u05d3\u0154\3\2\2\2\u05d4\u05d5\7/\2\2\u05d5\u05d6\7@\2"+
		"\2\u05d6\u0156\3\2\2\2\u05d7\u05d8\7#\2\2\u05d8\u05d9\7k\2\2\u05d9\u05da"+
		"\7p\2\2\u05da\u05db\7u\2\2\u05db\u05dc\7v\2\2\u05dc\u05dd\7c\2\2\u05dd"+
		"\u05de\7p\2\2\u05de\u05df\7e\2\2\u05df\u05e0\7g\2\2\u05e0\u05e1\7q\2\2"+
		"\u05e1\u05e2\7h\2\2\u05e2\u05e3\3\2\2\2\u05e3\u05e4\6\u00a9\r\2\u05e4"+
		"\u0158\3\2\2\2\u05e5\u05e6\7#\2\2\u05e6\u05e7\7k\2\2\u05e7\u05e8\7p\2"+
		"\2\u05e8\u05e9\3\2\2\2\u05e9\u05ea\6\u00aa\16\2\u05ea\u015a\3\2\2\2\u05eb"+
		"\u05ec\7*\2\2\u05ec\u05ed\b\u00ab\27\2\u05ed\u05ee\3\2\2\2\u05ee\u05ef"+
		"\b\u00ab\16\2\u05ef\u015c\3\2\2\2\u05f0\u05f1\7+\2\2\u05f1\u05f2\b\u00ac"+
		"\30\2\u05f2\u05f3\3\2\2\2\u05f3\u05f4\b\u00ac\b\2\u05f4\u015e\3\2\2\2"+
		"\u05f5\u05f6\7}\2\2\u05f6\u05f7\b\u00ad\31\2\u05f7\u05f8\3\2\2\2\u05f8"+
		"\u05f9\b\u00ad\16\2\u05f9\u0160\3\2\2\2\u05fa\u05fb\7\177\2\2\u05fb\u05fc"+
		"\b\u00ae\32\2\u05fc\u05fd\3\2\2\2\u05fd\u05fe\b\u00ae\b\2\u05fe\u0162"+
		"\3\2\2\2\u05ff\u0600\7]\2\2\u0600\u0601\b\u00af\33\2\u0601\u0602\3\2\2"+
		"\2\u0602\u0603\b\u00af\16\2\u0603\u0164\3\2\2\2\u0604\u0605\7_\2\2\u0605"+
		"\u0606\b\u00b0\34\2\u0606\u0607\3\2\2\2\u0607\u0608\b\u00b0\b\2\u0608"+
		"\u0166\3\2\2\2\u0609\u060a\7=\2\2\u060a\u0168\3\2\2\2\u060b\u060c\7.\2"+
		"\2\u060c\u016a\3\2\2\2\u060d\u060e\5\u0105\u0080\2\u060e\u016c\3\2\2\2"+
		"\u060f\u0610\7?\2\2\u0610\u016e\3\2\2\2\u0611\u0612\7@\2\2\u0612\u0170"+
		"\3\2\2\2\u0613\u0614\7>\2\2\u0614\u0172\3\2\2\2\u0615\u0616\7#\2\2\u0616"+
		"\u0174\3\2\2\2\u0617\u0618\7\u0080\2\2\u0618\u0176\3\2\2\2\u0619\u061a"+
		"\7A\2\2\u061a\u0178\3\2\2\2\u061b\u061c\7<\2\2\u061c\u017a\3\2\2\2\u061d"+
		"\u061e\7?\2\2\u061e\u061f\7?\2\2\u061f\u017c\3\2\2\2\u0620\u0621\7>\2"+
		"\2\u0621\u0622\7?\2\2\u0622\u017e\3\2\2\2\u0623\u0624\7@\2\2\u0624\u0625"+
		"\7?\2\2\u0625\u0180\3\2\2\2\u0626\u0627\7#\2\2\u0627\u0628\7?\2\2\u0628"+
		"\u0182\3\2\2\2\u0629\u062a\7(\2\2\u062a\u062b\7(\2\2\u062b\u0184\3\2\2"+
		"\2\u062c\u062d\7~\2\2\u062d\u062e\7~\2\2\u062e\u0186\3\2\2\2\u062f\u0630"+
		"\7-\2\2\u0630\u0631\7-\2\2\u0631\u0188\3\2\2\2\u0632\u0633\7/\2\2\u0633"+
		"\u0634\7/\2\2\u0634\u018a\3\2\2\2\u0635\u0636\7-\2\2\u0636\u018c\3\2\2"+
		"\2\u0637\u0638\7/\2\2\u0638\u018e\3\2\2\2\u0639\u063a\7,\2\2\u063a\u0190"+
		"\3\2\2\2\u063b\u063c\5\u011b\u008b\2\u063c\u0192\3\2\2\2\u063d\u063e\7"+
		"(\2\2\u063e\u0194\3\2\2\2\u063f\u0640\7~\2\2\u0640\u0196\3\2\2\2\u0641"+
		"\u0642\7`\2\2\u0642\u0198\3\2\2\2\u0643\u0644\7\'\2\2\u0644\u019a\3\2"+
		"\2\2\u0645\u0646\7-\2\2\u0646\u0647\7?\2\2\u0647\u019c\3\2\2\2\u0648\u0649"+
		"\7/\2\2\u0649\u064a\7?\2\2\u064a\u019e\3\2\2\2\u064b\u064c\7,\2\2\u064c"+
		"\u064d\7?\2\2\u064d\u01a0\3\2\2\2\u064e\u064f\7\61\2\2\u064f\u0650\7?"+
		"\2\2\u0650\u01a2\3\2\2\2\u0651\u0652\7(\2\2\u0652\u0653\7?\2\2\u0653\u01a4"+
		"\3\2\2\2\u0654\u0655\7~\2\2\u0655\u0656\7?\2\2\u0656\u01a6\3\2\2\2\u0657"+
		"\u0658\7`\2\2\u0658\u0659\7?\2\2\u0659\u01a8\3\2\2\2\u065a\u065b\7\'\2"+
		"\2\u065b\u065c\7?\2\2\u065c\u01aa\3\2\2\2\u065d\u065e\7>\2\2\u065e\u065f"+
		"\7>\2\2\u065f\u0660\7?\2\2\u0660\u01ac\3\2\2\2\u0661\u0662\7@\2\2\u0662"+
		"\u0663\7@\2\2\u0663\u0664\7?\2\2\u0664\u01ae\3\2\2\2\u0665\u0666\7@\2"+
		"\2\u0666\u0667\7@\2\2\u0667\u0668\7@\2\2\u0668\u0669\7?\2\2\u0669\u01b0"+
		"\3\2\2\2\u066a\u066b\7A\2\2\u066b\u066c\7?\2\2\u066c\u01b2\3\2\2\2\u066d"+
		"\u066e\5\u01b9\u00da\2\u066e\u0672\6\u00d7\17\2\u066f\u0671\5\u01bd\u00dc"+
		"\2\u0670\u066f\3\2\2\2\u0671\u0674\3\2\2\2\u0672\u0670\3\2\2\2\u0672\u0673"+
		"\3\2\2\2\u0673\u01b4\3\2\2\2\u0674\u0672\3\2\2\2\u0675\u0679\5\u01b9\u00da"+
		"\2\u0676\u0678\5\u01bd\u00dc\2\u0677\u0676\3\2\2\2\u0678\u067b\3\2\2\2"+
		"\u0679\u0677\3\2\2\2\u0679\u067a\3\2\2\2\u067a\u01b6\3\2\2\2\u067b\u0679"+
		"\3\2\2\2\u067c\u0680\5\u01bb\u00db\2\u067d\u067f\5\u01bf\u00dd\2\u067e"+
		"\u067d\3\2\2\2\u067f\u0682\3\2\2\2\u0680\u067e\3\2\2\2\u0680\u0681\3\2"+
		"\2\2\u0681\u01b8\3\2\2\2\u0682\u0680\3\2\2\2\u0683\u068a\t\25\2\2\u0684"+
		"\u0685\n\26\2\2\u0685\u068a\6\u00da\20\2\u0686\u0687\t\27\2\2\u0687\u0688"+
		"\t\30\2\2\u0688\u068a\6\u00da\21\2\u0689\u0683\3\2\2\2\u0689\u0684\3\2"+
		"\2\2\u0689\u0686\3\2\2\2\u068a\u01ba\3\2\2\2\u068b\u068c\5\u01b9\u00da"+
		"\2\u068c\u068d\6\u00db\22\2\u068d\u01bc\3\2\2\2\u068e\u0695\t\31\2\2\u068f"+
		"\u0690\n\26\2\2\u0690\u0695\6\u00dc\23\2\u0691\u0692\t\27\2\2\u0692\u0693"+
		"\t\30\2\2\u0693\u0695\6\u00dc\24\2\u0694\u068e\3\2\2\2\u0694\u068f\3\2"+
		"\2\2\u0694\u0691\3\2\2\2\u0695\u01be\3\2\2\2\u0696\u0697\5\u01bd\u00dc"+
		"\2\u0697\u0698\6\u00dd\25\2\u0698\u01c0\3\2\2\2\u0699\u069b\n\32\2\2\u069a"+
		"\u0699\3\2\2\2\u069b\u069e\3\2\2\2\u069c\u069a\3\2\2\2\u069c\u069d\3\2"+
		"\2\2\u069d\u01c2\3\2\2\2\u069e\u069c\3\2\2\2\u069f\u06a0\7B\2\2\u06a0"+
		"\u01c4\3\2\2\2\u06a1\u06a2\7\60\2\2\u06a2\u06a3\7\60\2\2\u06a3\u06a4\7"+
		"\60\2\2\u06a4\u01c6\3\2\2\2\u06a5\u06a7\t\33\2\2\u06a6\u06a5\3\2\2\2\u06a7"+
		"\u06a8\3\2\2\2\u06a8\u06a6\3\2\2\2\u06a8\u06a9\3\2\2\2\u06a9\u06b0\3\2"+
		"\2\2\u06aa\u06ac\5\u0113\u0087\2\u06ab\u06aa\3\2\2\2\u06ac\u06ad\3\2\2"+
		"\2\u06ad\u06ab\3\2\2\2\u06ad\u06ae\3\2\2\2\u06ae\u06b0\3\2\2\2\u06af\u06a6"+
		"\3\2\2\2\u06af\u06ab\3\2\2\2\u06b0\u06b1\3\2\2\2\u06b1\u06b2\b\u00e1\35"+
		"\2\u06b2\u01c8\3\2\2\2\u06b3\u06b4\5\u0115\u0088\2\u06b4\u06b5\b\u00e2"+
		"\36\2\u06b5\u01ca\3\2\2\2\u06b6\u06b7\7\61\2\2\u06b7\u06b8\7,\2\2\u06b8"+
		"\u06bc\3\2\2\2\u06b9\u06bb\13\2\2\2\u06ba\u06b9\3\2\2\2\u06bb\u06be\3"+
		"\2\2\2\u06bc\u06bd\3\2\2\2\u06bc\u06ba\3\2\2\2\u06bd\u06bf\3\2\2\2\u06be"+
		"\u06bc\3\2\2\2\u06bf\u06c0\7,\2\2\u06c0\u06c1\7\61\2\2\u06c1\u06c2\3\2"+
		"\2\2\u06c2\u06c3\b\u00e3\37\2\u06c3\u06c4\3\2\2\2\u06c4\u06c5\b\u00e3"+
		" \2\u06c5\u01cc\3\2\2\2\u06c6\u06c7\7\61\2\2\u06c7\u06c8\7\61\2\2\u06c8"+
		"\u06cc\3\2\2\2\u06c9\u06cb\n\32\2\2\u06ca\u06c9\3\2\2\2\u06cb\u06ce\3"+
		"\2\2\2\u06cc\u06ca\3\2\2\2\u06cc\u06cd\3\2\2\2\u06cd\u06cf\3\2\2\2\u06ce"+
		"\u06cc\3\2\2\2\u06cf\u06d0\b\u00e4!\2\u06d0\u06d1\3\2\2\2\u06d1\u06d2"+
		"\b\u00e4 \2\u06d2\u01ce\3\2\2\2\u06d3\u06d4\7%\2\2\u06d4\u06d5\7#\2\2"+
		"\u06d5\u06d6\3\2\2\2\u06d6\u06d7\b\u00e5\"\2\u06d7\u06e0\5\u01c1\u00de"+
		"\2\u06d8\u06d9\5\u0115\u0088\2\u06d9\u06da\7%\2\2\u06da\u06db\7#\2\2\u06db"+
		"\u06dc\3\2\2\2\u06dc\u06dd\5\u01c1\u00de\2\u06dd\u06df\3\2\2\2\u06de\u06d8"+
		"\3\2\2\2\u06df\u06e2\3\2\2\2\u06e0\u06de\3\2\2\2\u06e0\u06e1\3\2\2\2\u06e1"+
		"\u06e3\3\2\2\2\u06e2\u06e0\3\2\2\2\u06e3\u06e4\b\u00e5\35\2\u06e4\u01d0"+
		"\3\2\2\2\u06e5\u06e6\13\2\2\2\u06e6\u06e7\b\u00e6#\2\u06e7\u01d2\3\2\2"+
		"\2T\2\3\4\5\6\7\b\u01d7\u01e0\u01ea\u01f2\u01fb\u0204\u0208\u020e\u021a"+
		"\u0228\u0236\u025b\u0292\u0296\u029d\u02a4\u02ab\u02b6\u02df\u045f\u0464"+
		"\u046b\u046f\u0471\u0477\u047b\u047f\u0483\u048a\u048f\u0491\u0497\u049b"+
		"\u049f\u04a5\u04aa\u04b6\u04ba\u04c0\u04c4\u04cc\u04d0\u04d6\u04e0\u04e4"+
		"\u04ea\u04ee\u04f3\u04f6\u04fb\u04fe\u0503\u0508\u0510\u051b\u051f\u0524"+
		"\u0529\u053b\u0544\u0552\u0564\u0568\u0672\u0679\u0680\u0689\u0694\u069c"+
		"\u06a8\u06ad\u06af\u06bc\u06cc\u06e0$\7\3\2\7\7\2\t\4\2\7\4\2\7\5\2\7"+
		"\6\2\6\2\2\5\2\2\t\5\2\t\6\2\3\23\2\tZ\2\7\2\2\t\u0085\2\7\b\2\3\26\3"+
		"\3[\4\3[\5\3[\6\3u\7\3\u009c\b\3\u00ab\t\3\u00ac\n\3\u00ad\13\3\u00ae"+
		"\f\3\u00af\r\3\u00b0\16\b\2\2\3\u00e2\17\3\u00e3\20\t\u0089\2\3\u00e4"+
		"\21\3\u00e5\22\3\u00e6\23";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
	}
}