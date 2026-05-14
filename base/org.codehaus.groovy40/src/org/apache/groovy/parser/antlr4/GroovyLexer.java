// Generated from GroovyLexer.g4 by ANTLR 4.13.2.7
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
			"DollarSlashEscape", "DollarDollarEscape", "DollarSlashDollarEscape", 
			"NullLiteral", "RANGE_INCLUSIVE", "RANGE_EXCLUSIVE_LEFT", "RANGE_EXCLUSIVE_RIGHT", 
			"RANGE_EXCLUSIVE_FULL", "SPREAD_DOT", "SAFE_DOT", "SAFE_INDEX", "SAFE_CHAIN_DOT", 
			"ELVIS", "METHOD_POINTER", "METHOD_REFERENCE", "REGEX_FIND", "REGEX_MATCH", 
			"POWER", "POWER_ASSIGN", "SPACESHIP", "IDENTICAL", "NOT_IDENTICAL", "ARROW", 
			"NOT_INSTANCEOF", "NOT_IN", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", 
			"RBRACK", "SEMI", "COMMA", "DOT", "ASSIGN", "GT", "LT", "NOT", "BITNOT", 
			"QUESTION", "COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", "OR", "INC", 
			"DEC", "ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "XOR", "MOD", "ADD_ASSIGN", 
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

	    private void addComment(int type) {
	        String text = _input.getText(Interval.of(_tokenStartCharIndex, getCharIndex() - 1));
	        // GRECLIPSE add
	        Comment comment;
	        if (type == 0) {
	            comment = Comment.makeMultiLineComment( _tokenStartLine, _tokenStartCharPositionInLine + 1, getLine(), getCharPositionInLine() + 1, text);
	        } else {
	            comment = Comment.makeSingleLineComment(_tokenStartLine, _tokenStartCharPositionInLine + 1, getLine(), getCharPositionInLine() + 1, text);
	        }
	        comments.add(comment);
	        // GRECLIPSE end
	    }

	    public List<Comment> getComments() { return comments; }
	    private final List<Comment> comments = new ArrayList<>();

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
		case 155:
			SAFE_INDEX_action(_localctx, actionIndex);
			break;
		case 170:
			LPAREN_action(_localctx, actionIndex);
			break;
		case 171:
			RPAREN_action(_localctx, actionIndex);
			break;
		case 172:
			LBRACE_action(_localctx, actionIndex);
			break;
		case 173:
			RBRACE_action(_localctx, actionIndex);
			break;
		case 174:
			LBRACK_action(_localctx, actionIndex);
			break;
		case 175:
			RBRACK_action(_localctx, actionIndex);
			break;
		case 225:
			NL_action(_localctx, actionIndex);
			break;
		case 226:
			ML_COMMENT_action(_localctx, actionIndex);
			break;
		case 227:
			SL_COMMENT_action(_localctx, actionIndex);
			break;
		case 228:
			SH_COMMENT_action(_localctx, actionIndex);
			break;
		case 229:
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
			 ignoreTokenInsideParens(); 
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
		case 168:
			return NOT_INSTANCEOF_sempred(_localctx, predIndex);
		case 169:
			return NOT_IN_sempred(_localctx, predIndex);
		case 214:
			return CapitalizedIdentifier_sempred(_localctx, predIndex);
		case 217:
			return JavaLetter_sempred(_localctx, predIndex);
		case 218:
			return JavaLetterInGString_sempred(_localctx, predIndex);
		case 219:
			return JavaLetterOrDigit_sempred(_localctx, predIndex);
		case 220:
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
			return  _input.LA(-4) != '$' ;
		case 10:
			return  _input.LA(1) != '$' ;
		case 11:
			return  _input.LA(1) != '$' ;
		case 12:
			return  !isFollowedByJavaLetterInGString(_input) ;
		}
		return true;
	}
	private boolean NOT_INSTANCEOF_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 13:
			return  isFollowedBy(_input, ' ', '\t', '\r', '\n') ;
		}
		return true;
	}
	private boolean NOT_IN_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 14:
			return  isFollowedBy(_input, ' ', '\t', '\r', '\n', '[', '(', '{') ;
		}
		return true;
	}
	private boolean CapitalizedIdentifier_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 15:
			return Character.isUpperCase(_input.LA(-1));
		}
		return true;
	}
	private boolean JavaLetter_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 16:
			return  isJavaIdentifierStartAndNotIdentifierIgnorable(_input.LA(-1)) ;
		case 17:
			return  Character.isJavaIdentifierStart(Character.toCodePoint((char) _input.LA(-2), (char) _input.LA(-1))) ;
		}
		return true;
	}
	private boolean JavaLetterInGString_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 18:
			return  _input.LA(-1) != '$' ;
		}
		return true;
	}
	private boolean JavaLetterOrDigit_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 19:
			return  isJavaIdentifierPartAndNotIdentifierIgnorable(_input.LA(-1)) ;
		case 20:
			return  Character.isJavaIdentifierPart(Character.toCodePoint((char) _input.LA(-2), (char) _input.LA(-1))) ;
		}
		return true;
	}
	private boolean JavaLetterOrDigitInGString_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 21:
			return  _input.LA(-1) != '$' ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\2\u008b\u06f3\b\1\b"+
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
		"\t\u00e6\4\u00e7\t\u00e7\3\2\3\2\7\2\u01d8\n\2\f\2\16\2\u01db\13\2\3\2"+
		"\3\2\3\2\3\2\7\2\u01e1\n\2\f\2\16\2\u01e4\13\2\3\2\3\2\3\2\3\2\3\2\6\2"+
		"\u01eb\n\2\r\2\16\2\u01ec\3\2\3\2\3\2\3\2\7\2\u01f3\n\2\f\2\16\2\u01f6"+
		"\13\2\3\2\3\2\3\2\3\2\7\2\u01fc\n\2\f\2\16\2\u01ff\13\2\3\2\3\2\3\2\3"+
		"\2\6\2\u0205\n\2\r\2\16\2\u0206\3\2\3\2\5\2\u020b\n\2\3\3\3\3\7\3\u020f"+
		"\n\3\f\3\16\3\u0212\13\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\7\4\u021b\n\4\f\4"+
		"\16\4\u021e\13\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\7\5\u0229\n\5\f\5"+
		"\16\5\u022c\13\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\7\6\u0237\n\6\f\6"+
		"\16\6\u023a\13\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b\3\b\3"+
		"\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3"+
		"\f\3\f\3\f\3\f\3\r\5\r\u025e\n\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3"+
		"\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3"+
		"\23\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3"+
		"\26\3\27\3\27\5\27\u0295\n\27\3\30\3\30\5\30\u0299\n\30\3\31\3\31\3\31"+
		"\3\31\3\31\5\31\u02a0\n\31\3\32\3\32\3\32\3\32\3\32\5\32\u02a7\n\32\3"+
		"\33\3\33\3\33\3\33\3\33\5\33\u02ae\n\33\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u02be\n\34\3\35\3\35\3\35"+
		"\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3 \3 \3 \3 \3 \3 \3!\3!\3!\3!\3!\3"+
		"!\3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3#\3#\3#\5#\u02e7\n#\3"+
		"$\3$\3$\3$\3$\3$\3$\3$\3$\3%\3%\3%\3%\3%\3%\3%\3&\3&\3&\3&\3&\3&\3&\3"+
		"&\3\'\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\3(\3)\3)\3)\3)\3)\3*\3*\3*\3"+
		"*\3*\3+\3+\3+\3+\3+\3+\3,\3,\3,\3,\3,\3-\3-\3-\3-\3-\3-\3.\3.\3.\3.\3"+
		".\3.\3/\3/\3/\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3\60"+
		"\3\61\3\61\3\61\3\62\3\62\3\62\3\62\3\62\3\62\3\62\3\63\3\63\3\63\3\63"+
		"\3\63\3\64\3\64\3\64\3\64\3\64\3\65\3\65\3\65\3\65\3\65\3\65\3\65\3\65"+
		"\3\66\3\66\3\66\3\66\3\66\3\66\3\67\3\67\3\67\3\67\3\67\3\67\3\67\3\67"+
		"\38\38\38\38\38\38\39\39\39\39\3:\3:\3:\3;\3;\3;\3;\3;\3<\3<\3<\3<\3<"+
		"\3<\3<\3<\3<\3<\3<\3=\3=\3=\3=\3=\3=\3=\3>\3>\3>\3>\3>\3>\3>\3>\3>\3>"+
		"\3>\3?\3?\3?\3?\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3A\3A\3A\3A\3A\3B\3B\3B"+
		"\3B\3B\3B\3B\3C\3C\3C\3C\3D\3D\3D\3D\3D\3D\3D\3D\3D\3D\3D\3E\3E\3E\3E"+
		"\3E\3E\3E\3E\3F\3F\3F\3F\3F\3F\3F\3F\3G\3G\3G\3G\3G\3G\3G\3G\3H\3H\3H"+
		"\3H\3H\3H\3H\3H\3H\3H\3I\3I\3I\3I\3I\3I\3I\3J\3J\3J\3J\3J\3J\3J\3K\3K"+
		"\3K\3K\3K\3K\3K\3L\3L\3L\3L\3L\3L\3L\3M\3M\3M\3M\3M\3M\3N\3N\3N\3N\3N"+
		"\3N\3N\3O\3O\3O\3O\3O\3O\3O\3O\3O\3P\3P\3P\3P\3P\3P\3Q\3Q\3Q\3Q\3Q\3Q"+
		"\3Q\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\3S\3S\3S\3S\3S\3T\3T\3T\3T"+
		"\3T\3T\3U\3U\3U\3U\3U\3U\3U\3V\3V\3V\3V\3V\3V\3V\3V\3V\3V\3W\3W\3W\3W"+
		"\3X\3X\3X\3X\3X\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Y\3Z\3Z\3Z\3Z\3Z\3Z\3[\3[\3["+
		"\3[\5[\u0467\n[\3[\3[\3[\5[\u046c\n[\3[\3[\3[\6[\u0471\n[\r[\16[\u0472"+
		"\3[\3[\5[\u0477\n[\5[\u0479\n[\3\\\3\\\3]\3]\5]\u047f\n]\3^\3^\5^\u0483"+
		"\n^\3_\3_\5_\u0487\n_\3`\3`\5`\u048b\n`\3a\3a\3b\3b\3b\5b\u0492\nb\3b"+
		"\3b\3b\5b\u0497\nb\5b\u0499\nb\3c\3c\7c\u049d\nc\fc\16c\u04a0\13c\3c\5"+
		"c\u04a3\nc\3d\3d\5d\u04a7\nd\3e\3e\3f\3f\5f\u04ad\nf\3g\6g\u04b0\ng\r"+
		"g\16g\u04b1\3h\3h\3i\3i\3i\3i\3j\3j\7j\u04bc\nj\fj\16j\u04bf\13j\3j\5"+
		"j\u04c2\nj\3k\3k\3l\3l\5l\u04c8\nl\3m\3m\5m\u04cc\nm\3m\3m\3n\3n\7n\u04d2"+
		"\nn\fn\16n\u04d5\13n\3n\5n\u04d8\nn\3o\3o\3p\3p\5p\u04de\np\3q\3q\3q\3"+
		"q\3r\3r\7r\u04e6\nr\fr\16r\u04e9\13r\3r\5r\u04ec\nr\3s\3s\3t\3t\5t\u04f2"+
		"\nt\3u\3u\5u\u04f6\nu\3u\3u\3u\5u\u04fb\nu\3v\5v\u04fe\nv\3v\3v\3v\5v"+
		"\u0503\nv\3v\5v\u0506\nv\3v\3v\3v\5v\u050b\nv\3v\3v\3v\5v\u0510\nv\3w"+
		"\3w\3w\3x\3x\3y\5y\u0518\ny\3y\3y\3z\3z\3{\3{\3|\3|\3|\5|\u0523\n|\3}"+
		"\3}\5}\u0527\n}\3}\3}\3}\5}\u052c\n}\3}\3}\3}\5}\u0531\n}\3~\3~\3~\3\177"+
		"\3\177\3\u0080\3\u0080\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081\3\u0081"+
		"\3\u0081\3\u0081\3\u0081\5\u0081\u0543\n\u0081\3\u0082\3\u0082\3\u0082"+
		"\3\u0082\3\u0082\3\u0082\3\u0082\5\u0082\u054c\n\u0082\3\u0083\3\u0083"+
		"\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083\3\u0083"+
		"\3\u0083\5\u0083\u055a\n\u0083\3\u0084\3\u0084\3\u0084\3\u0084\3\u0084"+
		"\3\u0084\3\u0084\3\u0085\3\u0085\3\u0086\3\u0086\3\u0086\3\u0087\3\u0087"+
		"\3\u0087\3\u0088\5\u0088\u056c\n\u0088\3\u0088\3\u0088\5\u0088\u0570\n"+
		"\u0088\3\u0089\3\u0089\3\u0089\3\u008a\3\u008a\3\u008b\3\u008b\3\u008c"+
		"\3\u008c\3\u008d\3\u008d\3\u008e\3\u008e\3\u008f\3\u008f\3\u008f\3\u008f"+
		"\3\u0090\3\u0090\3\u0090\3\u0090\3\u0091\3\u0091\3\u0091\3\u0092\3\u0092"+
		"\3\u0092\3\u0093\3\u0093\3\u0093\3\u0094\3\u0094\3\u0094\3\u0095\3\u0095"+
		"\3\u0095\3\u0095\3\u0096\3\u0096\3\u0096\3\u0096\3\u0096\3\u0097\3\u0097"+
		"\3\u0097\3\u0098\3\u0098\3\u0098\3\u0098\3\u0099\3\u0099\3\u0099\3\u0099"+
		"\3\u009a\3\u009a\3\u009a\3\u009a\3\u009a\3\u009b\3\u009b\3\u009b\3\u009c"+
		"\3\u009c\3\u009c\3\u009d\3\u009d\3\u009d\3\u009d\3\u009d\3\u009d\3\u009d"+
		"\3\u009e\3\u009e\3\u009e\3\u009e\3\u009f\3\u009f\3\u009f\3\u00a0\3\u00a0"+
		"\3\u00a0\3\u00a1\3\u00a1\3\u00a1\3\u00a2\3\u00a2\3\u00a2\3\u00a3\3\u00a3"+
		"\3\u00a3\3\u00a3\3\u00a4\3\u00a4\3\u00a4\3\u00a5\3\u00a5\3\u00a5\3\u00a5"+
		"\3\u00a6\3\u00a6\3\u00a6\3\u00a6\3\u00a7\3\u00a7\3\u00a7\3\u00a7\3\u00a8"+
		"\3\u00a8\3\u00a8\3\u00a8\3\u00a9\3\u00a9\3\u00a9\3\u00aa\3\u00aa\3\u00aa"+
		"\3\u00aa\3\u00aa\3\u00aa\3\u00aa\3\u00aa\3\u00aa\3\u00aa\3\u00aa\3\u00aa"+
		"\3\u00aa\3\u00aa\3\u00ab\3\u00ab\3\u00ab\3\u00ab\3\u00ab\3\u00ab\3\u00ac"+
		"\3\u00ac\3\u00ac\3\u00ac\3\u00ac\3\u00ad\3\u00ad\3\u00ad\3\u00ad\3\u00ad"+
		"\3\u00ae\3\u00ae\3\u00ae\3\u00ae\3\u00ae\3\u00af\3\u00af\3\u00af\3\u00af"+
		"\3\u00af\3\u00b0\3\u00b0\3\u00b0\3\u00b0\3\u00b0\3\u00b1\3\u00b1\3\u00b1"+
		"\3\u00b1\3\u00b1\3\u00b2\3\u00b2\3\u00b3\3\u00b3\3\u00b4\3\u00b4\3\u00b5"+
		"\3\u00b5\3\u00b6\3\u00b6\3\u00b7\3\u00b7\3\u00b8\3\u00b8\3\u00b9\3\u00b9"+
		"\3\u00ba\3\u00ba\3\u00bb\3\u00bb\3\u00bc\3\u00bc\3\u00bc\3\u00bd\3\u00bd"+
		"\3\u00bd\3\u00be\3\u00be\3\u00be\3\u00bf\3\u00bf\3\u00bf\3\u00c0\3\u00c0"+
		"\3\u00c0\3\u00c1\3\u00c1\3\u00c1\3\u00c2\3\u00c2\3\u00c2\3\u00c3\3\u00c3"+
		"\3\u00c3\3\u00c4\3\u00c4\3\u00c5\3\u00c5\3\u00c6\3\u00c6\3\u00c7\3\u00c7"+
		"\3\u00c8\3\u00c8\3\u00c9\3\u00c9\3\u00ca\3\u00ca\3\u00cb\3\u00cb\3\u00cc"+
		"\3\u00cc\3\u00cc\3\u00cd\3\u00cd\3\u00cd\3\u00ce\3\u00ce\3\u00ce\3\u00cf"+
		"\3\u00cf\3\u00cf\3\u00d0\3\u00d0\3\u00d0\3\u00d1\3\u00d1\3\u00d1\3\u00d2"+
		"\3\u00d2\3\u00d2\3\u00d3\3\u00d3\3\u00d3\3\u00d4\3\u00d4\3\u00d4\3\u00d4"+
		"\3\u00d5\3\u00d5\3\u00d5\3\u00d5\3\u00d6\3\u00d6\3\u00d6\3\u00d6\3\u00d6"+
		"\3\u00d7\3\u00d7\3\u00d7\3\u00d8\3\u00d8\3\u00d8\7\u00d8\u067c\n\u00d8"+
		"\f\u00d8\16\u00d8\u067f\13\u00d8\3\u00d9\3\u00d9\7\u00d9\u0683\n\u00d9"+
		"\f\u00d9\16\u00d9\u0686\13\u00d9\3\u00da\3\u00da\7\u00da\u068a\n\u00da"+
		"\f\u00da\16\u00da\u068d\13\u00da\3\u00db\3\u00db\3\u00db\3\u00db\3\u00db"+
		"\3\u00db\5\u00db\u0695\n\u00db\3\u00dc\3\u00dc\3\u00dc\3\u00dd\3\u00dd"+
		"\3\u00dd\3\u00dd\3\u00dd\3\u00dd\5\u00dd\u06a0\n\u00dd\3\u00de\3\u00de"+
		"\3\u00de\3\u00df\7\u00df\u06a6\n\u00df\f\u00df\16\u00df\u06a9\13\u00df"+
		"\3\u00e0\3\u00e0\3\u00e1\3\u00e1\3\u00e1\3\u00e1\3\u00e2\6\u00e2\u06b2"+
		"\n\u00e2\r\u00e2\16\u00e2\u06b3\3\u00e2\6\u00e2\u06b7\n\u00e2\r\u00e2"+
		"\16\u00e2\u06b8\5\u00e2\u06bb\n\u00e2\3\u00e2\3\u00e2\3\u00e3\3\u00e3"+
		"\3\u00e3\3\u00e4\3\u00e4\3\u00e4\3\u00e4\7\u00e4\u06c6\n\u00e4\f\u00e4"+
		"\16\u00e4\u06c9\13\u00e4\3\u00e4\3\u00e4\3\u00e4\3\u00e4\3\u00e4\3\u00e4"+
		"\3\u00e4\3\u00e5\3\u00e5\3\u00e5\3\u00e5\7\u00e5\u06d6\n\u00e5\f\u00e5"+
		"\16\u00e5\u06d9\13\u00e5\3\u00e5\3\u00e5\3\u00e5\3\u00e5\3\u00e6\3\u00e6"+
		"\3\u00e6\3\u00e6\3\u00e6\3\u00e6\3\u00e6\3\u00e6\3\u00e6\3\u00e6\3\u00e6"+
		"\7\u00e6\u06ea\n\u00e6\f\u00e6\16\u00e6\u06ed\13\u00e6\3\u00e6\3\u00e6"+
		"\3\u00e7\3\u00e7\3\u00e7\3\u06c7\2\2\u00e8\t\2\3\13\2\4\r\2\2\17\2\2\21"+
		"\2\2\23\2\5\25\2\6\27\2\2\31\2\2\33\2\2\35\2\2\37\2\2!\2\2#\2\2%\2\2\'"+
		"\2\2)\2\2+\2\2-\2\2/\2\7\61\2\b\63\2\2\65\2\2\67\2\29\2\2;\2\2=\2\2?\2"+
		"\tA\2\nC\2\13E\2\fG\2\rI\2\16K\2\17M\2\20O\2\21Q\2\2S\2\22U\2\23W\2\2"+
		"Y\2\24[\2\25]\2\2_\2\26a\2\27c\2\30e\2\31g\2\32i\2\2k\2\33m\2\34o\2\35"+
		"q\2\36s\2\37u\2\2w\2 y\2!{\2\"}\2#\177\2$\u0081\2%\u0083\2\2\u0085\2&"+
		"\u0087\2\2\u0089\2\'\u008b\2(\u008d\2)\u008f\2*\u0091\2+\u0093\2,\u0095"+
		"\2-\u0097\2.\u0099\2/\u009b\2\60\u009d\2\61\u009f\2\2\u00a1\2\62\u00a3"+
		"\2\63\u00a5\2\64\u00a7\2\65\u00a9\2\66\u00ab\2\67\u00ad\28\u00af\29\u00b1"+
		"\2:\u00b3\2;\u00b5\2<\u00b7\2=\u00b9\2>\u00bb\2?\u00bd\2\2\u00bf\2\2\u00c1"+
		"\2\2\u00c3\2\2\u00c5\2\2\u00c7\2\2\u00c9\2\2\u00cb\2\2\u00cd\2\2\u00cf"+
		"\2\2\u00d1\2\2\u00d3\2\2\u00d5\2\2\u00d7\2\2\u00d9\2\2\u00db\2\2\u00dd"+
		"\2\2\u00df\2\2\u00e1\2\2\u00e3\2\2\u00e5\2\2\u00e7\2\2\u00e9\2\2\u00eb"+
		"\2\2\u00ed\2\2\u00ef\2@\u00f1\2\2\u00f3\2\2\u00f5\2\2\u00f7\2\2\u00f9"+
		"\2\2\u00fb\2\2\u00fd\2\2\u00ff\2\2\u0101\2\2\u0103\2\2\u0105\2\2\u0107"+
		"\2A\u0109\2\2\u010b\2\2\u010d\2\2\u010f\2\2\u0111\2\2\u0113\2\2\u0115"+
		"\2\2\u0117\2\2\u0119\2\2\u011b\2\2\u011d\2\2\u011f\2\2\u0121\2\2\u0123"+
		"\2\2\u0125\2\2\u0127\2\2\u0129\2\2\u012b\2\2\u012d\2\2\u012f\2\2\u0131"+
		"\2B\u0133\2C\u0135\2D\u0137\2E\u0139\2F\u013b\2G\u013d\2H\u013f\2I\u0141"+
		"\2J\u0143\2K\u0145\2L\u0147\2M\u0149\2N\u014b\2O\u014d\2P\u014f\2Q\u0151"+
		"\2R\u0153\2S\u0155\2T\u0157\2U\u0159\2V\u015b\2W\u015d\2X\u015f\2Y\u0161"+
		"\2Z\u0163\2[\u0165\2\\\u0167\2]\u0169\2^\u016b\2_\u016d\2`\u016f\2a\u0171"+
		"\2b\u0173\2c\u0175\2d\u0177\2e\u0179\2f\u017b\2g\u017d\2h\u017f\2i\u0181"+
		"\2j\u0183\2k\u0185\2l\u0187\2m\u0189\2n\u018b\2o\u018d\2p\u018f\2q\u0191"+
		"\2r\u0193\2s\u0195\2t\u0197\2u\u0199\2v\u019b\2w\u019d\2x\u019f\2y\u01a1"+
		"\2z\u01a3\2{\u01a5\2|\u01a7\2}\u01a9\2~\u01ab\2\177\u01ad\2\u0080\u01af"+
		"\2\u0081\u01b1\2\u0082\u01b3\2\u0083\u01b5\2\u0084\u01b7\2\u0085\u01b9"+
		"\2\2\u01bb\2\2\u01bd\2\2\u01bf\2\2\u01c1\2\2\u01c3\2\2\u01c5\2\u0086\u01c7"+
		"\2\u0087\u01c9\2\u0088\u01cb\2\u0089\u01cd\2\2\u01cf\2\2\u01d1\2\u008a"+
		"\u01d3\2\u008b\t\2\3\4\5\6\7\b\34\7\2\f\f\17\17$$&&^^\6\2\f\f\17\17))"+
		"^^\5\2$$&&^^\4\2))^^\5\2\2\2&&\61\61\3\2\62;\b\2IIKKNNiikknn\3\2\63;\4"+
		"\2ZZzz\5\2\62;CHch\3\2\629\4\2DDdd\3\2\62\63\4\2GGgg\4\2--//\6\2FFHIf"+
		"fhi\4\2RRrr\t\2$$))^^ddhhpptv\3\2\62\65\6\2&&C\\aac|\4\2\2\u0081\ud802"+
		"\udc01\3\2\ud802\udc01\3\2\udc02\ue001\7\2&&\62;C\\aac|\5\2\f\f\17\17"+
		"\1\1\4\2\13\13\"\"\2\u0705\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3"+
		"\2\2\2\2\21\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2"+
		"\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2"+
		"\2Y\3\2\2\2\2[\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g"+
		"\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2w\3\2"+
		"\2\2\2y\3\2\2\2\2{\3\2\2\2\2}\3\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2"+
		"\u0085\3\2\2\2\2\u0089\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2\2\2\u008f"+
		"\3\2\2\2\2\u0091\3\2\2\2\2\u0093\3\2\2\2\2\u0095\3\2\2\2\2\u0097\3\2\2"+
		"\2\2\u0099\3\2\2\2\2\u009b\3\2\2\2\2\u009d\3\2\2\2\2\u00a1\3\2\2\2\2\u00a3"+
		"\3\2\2\2\2\u00a5\3\2\2\2\2\u00a7\3\2\2\2\2\u00a9\3\2\2\2\2\u00ab\3\2\2"+
		"\2\2\u00ad\3\2\2\2\2\u00af\3\2\2\2\2\u00b1\3\2\2\2\2\u00b3\3\2\2\2\2\u00b5"+
		"\3\2\2\2\2\u00b7\3\2\2\2\2\u00b9\3\2\2\2\2\u00bb\3\2\2\2\2\u00ef\3\2\2"+
		"\2\2\u0107\3\2\2\2\2\u0131\3\2\2\2\2\u0133\3\2\2\2\2\u0135\3\2\2\2\2\u0137"+
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
		"\3\2\2\2\2\u01b7\3\2\2\2\2\u01c5\3\2\2\2\2\u01c7\3\2\2\2\2\u01c9\3\2\2"+
		"\2\2\u01cb\3\2\2\2\2\u01cd\3\2\2\2\2\u01cf\3\2\2\2\2\u01d1\3\2\2\2\2\u01d3"+
		"\3\2\2\2\3\23\3\2\2\2\3\25\3\2\2\2\3\27\3\2\2\2\4\31\3\2\2\2\4\33\3\2"+
		"\2\2\4\35\3\2\2\2\5\37\3\2\2\2\5!\3\2\2\2\5#\3\2\2\2\6%\3\2\2\2\6\'\3"+
		"\2\2\2\6)\3\2\2\2\7+\3\2\2\2\7-\3\2\2\2\b/\3\2\2\2\b\61\3\2\2\2\t\u020a"+
		"\3\2\2\2\13\u020c\3\2\2\2\r\u0218\3\2\2\2\17\u0225\3\2\2\2\21\u0234\3"+
		"\2\2\2\23\u0242\3\2\2\2\25\u0246\3\2\2\2\27\u024a\3\2\2\2\31\u024e\3\2"+
		"\2\2\33\u0253\3\2\2\2\35\u0258\3\2\2\2\37\u025d\3\2\2\2!\u0264\3\2\2\2"+
		"#\u026a\3\2\2\2%\u026e\3\2\2\2\'\u0273\3\2\2\2)\u0279\3\2\2\2+\u027d\3"+
		"\2\2\2-\u0284\3\2\2\2/\u028a\3\2\2\2\61\u028d\3\2\2\2\63\u0294\3\2\2\2"+
		"\65\u0298\3\2\2\2\67\u029f\3\2\2\29\u02a6\3\2\2\2;\u02ad\3\2\2\2=\u02bd"+
		"\3\2\2\2?\u02bf\3\2\2\2A\u02c2\3\2\2\2C\u02c6\3\2\2\2E\u02c9\3\2\2\2G"+
		"\u02cf\3\2\2\2I\u02da\3\2\2\2K\u02e6\3\2\2\2M\u02e8\3\2\2\2O\u02f1\3\2"+
		"\2\2Q\u02f8\3\2\2\2S\u0300\3\2\2\2U\u0306\3\2\2\2W\u030c\3\2\2\2Y\u0311"+
		"\3\2\2\2[\u0316\3\2\2\2]\u031c\3\2\2\2_\u0321\3\2\2\2a\u0327\3\2\2\2c"+
		"\u032d\3\2\2\2e\u0336\3\2\2\2g\u033e\3\2\2\2i\u0341\3\2\2\2k\u0348\3\2"+
		"\2\2m\u034d\3\2\2\2o\u0352\3\2\2\2q\u035a\3\2\2\2s\u0360\3\2\2\2u\u0368"+
		"\3\2\2\2w\u036e\3\2\2\2y\u0372\3\2\2\2{\u0375\3\2\2\2}\u037a\3\2\2\2\177"+
		"\u0385\3\2\2\2\u0081\u038c\3\2\2\2\u0083\u0397\3\2\2\2\u0085\u039b\3\2"+
		"\2\2\u0087\u03a5\3\2\2\2\u0089\u03aa\3\2\2\2\u008b\u03b1\3\2\2\2\u008d"+
		"\u03b5\3\2\2\2\u008f\u03c0\3\2\2\2\u0091\u03c8\3\2\2\2\u0093\u03d0\3\2"+
		"\2\2\u0095\u03d8\3\2\2\2\u0097\u03e2\3\2\2\2\u0099\u03e9\3\2\2\2\u009b"+
		"\u03f0\3\2\2\2\u009d\u03f7\3\2\2\2\u009f\u03fe\3\2\2\2\u00a1\u0404\3\2"+
		"\2\2\u00a3\u040b\3\2\2\2\u00a5\u0414\3\2\2\2\u00a7\u041a\3\2\2\2\u00a9"+
		"\u0421\3\2\2\2\u00ab\u042e\3\2\2\2\u00ad\u0433\3\2\2\2\u00af\u0439\3\2"+
		"\2\2\u00b1\u0440\3\2\2\2\u00b3\u044a\3\2\2\2\u00b5\u044e\3\2\2\2\u00b7"+
		"\u0453\3\2\2\2\u00b9\u045c\3\2\2\2\u00bb\u0478\3\2\2\2\u00bd\u047a\3\2"+
		"\2\2\u00bf\u047c\3\2\2\2\u00c1\u0480\3\2\2\2\u00c3\u0484\3\2\2\2\u00c5"+
		"\u0488\3\2\2\2\u00c7\u048c\3\2\2\2\u00c9\u0498\3\2\2\2\u00cb\u049a\3\2"+
		"\2\2\u00cd\u04a6\3\2\2\2\u00cf\u04a8\3\2\2\2\u00d1\u04ac\3\2\2\2\u00d3"+
		"\u04af\3\2\2\2\u00d5\u04b3\3\2\2\2\u00d7\u04b5\3\2\2\2\u00d9\u04b9\3\2"+
		"\2\2\u00db\u04c3\3\2\2\2\u00dd\u04c7\3\2\2\2\u00df\u04c9\3\2\2\2\u00e1"+
		"\u04cf\3\2\2\2\u00e3\u04d9\3\2\2\2\u00e5\u04dd\3\2\2\2\u00e7\u04df\3\2"+
		"\2\2\u00e9\u04e3\3\2\2\2\u00eb\u04ed\3\2\2\2\u00ed\u04f1\3\2\2\2\u00ef"+
		"\u04f5\3\2\2\2\u00f1\u050f\3\2\2\2\u00f3\u0511\3\2\2\2\u00f5\u0514\3\2"+
		"\2\2\u00f7\u0517\3\2\2\2\u00f9\u051b\3\2\2\2\u00fb\u051d\3\2\2\2\u00fd"+
		"\u051f\3\2\2\2\u00ff\u0530\3\2\2\2\u0101\u0532\3\2\2\2\u0103\u0535\3\2"+
		"\2\2\u0105\u0537\3\2\2\2\u0107\u0542\3\2\2\2\u0109\u054b\3\2\2\2\u010b"+
		"\u0559\3\2\2\2\u010d\u055b\3\2\2\2\u010f\u0562\3\2\2\2\u0111\u0564\3\2"+
		"\2\2\u0113\u0567\3\2\2\2\u0115\u056f\3\2\2\2\u0117\u0571\3\2\2\2\u0119"+
		"\u0574\3\2\2\2\u011b\u0576\3\2\2\2\u011d\u0578\3\2\2\2\u011f\u057a\3\2"+
		"\2\2\u0121\u057c\3\2\2\2\u0123\u057e\3\2\2\2\u0125\u0582\3\2\2\2\u0127"+
		"\u0586\3\2\2\2\u0129\u0589\3\2\2\2\u012b\u058c\3\2\2\2\u012d\u058f\3\2"+
		"\2\2\u012f\u0592\3\2\2\2\u0131\u0596\3\2\2\2\u0133\u059b\3\2\2\2\u0135"+
		"\u059e\3\2\2\2\u0137\u05a2\3\2\2\2\u0139\u05a6\3\2\2\2\u013b\u05ab\3\2"+
		"\2\2\u013d\u05ae\3\2\2\2\u013f\u05b1\3\2\2\2\u0141\u05b8\3\2\2\2\u0143"+
		"\u05bc\3\2\2\2\u0145\u05bf\3\2\2\2\u0147\u05c2\3\2\2\2\u0149\u05c5\3\2"+
		"\2\2\u014b\u05c8\3\2\2\2\u014d\u05cc\3\2\2\2\u014f\u05cf\3\2\2\2\u0151"+
		"\u05d3\3\2\2\2\u0153\u05d7\3\2\2\2\u0155\u05db\3\2\2\2\u0157\u05df\3\2"+
		"\2\2\u0159\u05e2\3\2\2\2\u015b\u05f0\3\2\2\2\u015d\u05f6\3\2\2\2\u015f"+
		"\u05fb\3\2\2\2\u0161\u0600\3\2\2\2\u0163\u0605\3\2\2\2\u0165\u060a\3\2"+
		"\2\2\u0167\u060f\3\2\2\2\u0169\u0614\3\2\2\2\u016b\u0616\3\2\2\2\u016d"+
		"\u0618\3\2\2\2\u016f\u061a\3\2\2\2\u0171\u061c\3\2\2\2\u0173\u061e\3\2"+
		"\2\2\u0175\u0620\3\2\2\2\u0177\u0622\3\2\2\2\u0179\u0624\3\2\2\2\u017b"+
		"\u0626\3\2\2\2\u017d\u0628\3\2\2\2\u017f\u062b\3\2\2\2\u0181\u062e\3\2"+
		"\2\2\u0183\u0631\3\2\2\2\u0185\u0634\3\2\2\2\u0187\u0637\3\2\2\2\u0189"+
		"\u063a\3\2\2\2\u018b\u063d\3\2\2\2\u018d\u0640\3\2\2\2\u018f\u0642\3\2"+
		"\2\2\u0191\u0644\3\2\2\2\u0193\u0646\3\2\2\2\u0195\u0648\3\2\2\2\u0197"+
		"\u064a\3\2\2\2\u0199\u064c\3\2\2\2\u019b\u064e\3\2\2\2\u019d\u0650\3\2"+
		"\2\2\u019f\u0653\3\2\2\2\u01a1\u0656\3\2\2\2\u01a3\u0659\3\2\2\2\u01a5"+
		"\u065c\3\2\2\2\u01a7\u065f\3\2\2\2\u01a9\u0662\3\2\2\2\u01ab\u0665\3\2"+
		"\2\2\u01ad\u0668\3\2\2\2\u01af\u066c\3\2\2\2\u01b1\u0670\3\2\2\2\u01b3"+
		"\u0675\3\2\2\2\u01b5\u0678\3\2\2\2\u01b7\u0680\3\2\2\2\u01b9\u0687\3\2"+
		"\2\2\u01bb\u0694\3\2\2\2\u01bd\u0696\3\2\2\2\u01bf\u069f\3\2\2\2\u01c1"+
		"\u06a1\3\2\2\2\u01c3\u06a7\3\2\2\2\u01c5\u06aa\3\2\2\2\u01c7\u06ac\3\2"+
		"\2\2\u01c9\u06ba\3\2\2\2\u01cb\u06be\3\2\2\2\u01cd\u06c1\3\2\2\2\u01cf"+
		"\u06d1\3\2\2\2\u01d1\u06de\3\2\2\2\u01d3\u06f0\3\2\2\2\u01d5\u01d9\5\u011f"+
		"\u008d\2\u01d6\u01d8\5\63\27\2\u01d7\u01d6\3\2\2\2\u01d8\u01db\3\2\2\2"+
		"\u01d9\u01d7\3\2\2\2\u01d9\u01da\3\2\2\2\u01da\u01dc\3\2\2\2\u01db\u01d9"+
		"\3\2\2\2\u01dc\u01dd\5\u011f\u008d\2\u01dd\u020b\3\2\2\2\u01de\u01e2\5"+
		"\u0121\u008e\2\u01df\u01e1\5\65\30\2\u01e0\u01df\3\2\2\2\u01e1\u01e4\3"+
		"\2\2\2\u01e2\u01e0\3\2\2\2\u01e2\u01e3\3\2\2\2\u01e3\u01e5\3\2\2\2\u01e4"+
		"\u01e2\3\2\2\2\u01e5\u01e6\5\u0121\u008e\2\u01e6\u020b\3\2\2\2\u01e7\u01e8"+
		"\5\u011b\u008b\2\u01e8\u01ea\6\2\2\2\u01e9\u01eb\5;\33\2\u01ea\u01e9\3"+
		"\2\2\2\u01eb\u01ec\3\2\2\2\u01ec\u01ea\3\2\2\2\u01ec\u01ed\3\2\2\2\u01ed"+
		"\u01ee\3\2\2\2\u01ee\u01ef\5\u011b\u008b\2\u01ef\u020b\3\2\2\2\u01f0\u01f4"+
		"\5\u0123\u008f\2\u01f1\u01f3\5\67\31\2\u01f2\u01f1\3\2\2\2\u01f3\u01f6"+
		"\3\2\2\2\u01f4\u01f2\3\2\2\2\u01f4\u01f5\3\2\2\2\u01f5\u01f7\3\2\2\2\u01f6"+
		"\u01f4\3\2\2\2\u01f7\u01f8\5\u0123\u008f\2\u01f8\u020b\3\2\2\2\u01f9\u01fd"+
		"\5\u0125\u0090\2\u01fa\u01fc\59\32\2\u01fb\u01fa\3\2\2\2\u01fc\u01ff\3"+
		"\2\2\2\u01fd\u01fb\3\2\2\2\u01fd\u01fe\3\2\2\2\u01fe\u0200\3\2\2\2\u01ff"+
		"\u01fd\3\2\2\2\u0200\u0201\5\u0125\u0090\2\u0201\u020b\3\2\2\2\u0202\u0204"+
		"\5\u0127\u0091\2\u0203\u0205\5=\34\2\u0204\u0203\3\2\2\2\u0205\u0206\3"+
		"\2\2\2\u0206\u0204\3\2\2\2\u0206\u0207\3\2\2\2\u0207\u0208\3\2\2\2\u0208"+
		"\u0209\5\u0129\u0092\2\u0209\u020b\3\2\2\2\u020a\u01d5\3\2\2\2\u020a\u01de"+
		"\3\2\2\2\u020a\u01e7\3\2\2\2\u020a\u01f0\3\2\2\2\u020a\u01f9\3\2\2\2\u020a"+
		"\u0202\3\2\2\2\u020b\n\3\2\2\2\u020c\u0210\5\u011f\u008d\2\u020d\u020f"+
		"\5\63\27\2\u020e\u020d\3\2\2\2\u020f\u0212\3\2\2\2\u0210\u020e\3\2\2\2"+
		"\u0210\u0211\3\2\2\2\u0211\u0213\3\2\2\2\u0212\u0210\3\2\2\2\u0213\u0214"+
		"\5\u011d\u008c\2\u0214\u0215\3\2\2\2\u0215\u0216\b\3\2\2\u0216\u0217\b"+
		"\3\3\2\u0217\f\3\2\2\2\u0218\u021c\5\u0123\u008f\2\u0219\u021b\5\67\31"+
		"\2\u021a\u0219\3\2\2\2\u021b\u021e\3\2\2\2\u021c\u021a\3\2\2\2\u021c\u021d"+
		"\3\2\2\2\u021d\u021f\3\2\2\2\u021e\u021c\3\2\2\2\u021f\u0220\5\u011d\u008c"+
		"\2\u0220\u0221\3\2\2\2\u0221\u0222\b\4\4\2\u0222\u0223\b\4\5\2\u0223\u0224"+
		"\b\4\3\2\u0224\16\3\2\2\2\u0225\u0226\5\u011b\u008b\2\u0226\u022a\6\5"+
		"\3\2\u0227\u0229\5;\33\2\u0228\u0227\3\2\2\2\u0229\u022c\3\2\2\2\u022a"+
		"\u0228\3\2\2\2\u022a\u022b\3\2\2\2\u022b\u022d\3\2\2\2\u022c\u022a\3\2"+
		"\2\2\u022d\u022e\5\u011d\u008c\2\u022e\u022f\6\5\4\2\u022f\u0230\3\2\2"+
		"\2\u0230\u0231\b\5\4\2\u0231\u0232\b\5\6\2\u0232\u0233\b\5\3\2\u0233\20"+
		"\3\2\2\2\u0234\u0238\5\u0127\u0091\2\u0235\u0237\5=\34\2\u0236\u0235\3"+
		"\2\2\2\u0237\u023a\3\2\2\2\u0238\u0236\3\2\2\2\u0238\u0239\3\2\2\2\u0239"+
		"\u023b\3\2\2\2\u023a\u0238\3\2\2\2\u023b\u023c\5\u011d\u008c\2\u023c\u023d"+
		"\6\6\5\2\u023d\u023e\3\2\2\2\u023e\u023f\b\6\4\2\u023f\u0240\b\6\7\2\u0240"+
		"\u0241\b\6\3\2\u0241\22\3\2\2\2\u0242\u0243\5\u011f\u008d\2\u0243\u0244"+
		"\3\2\2\2\u0244\u0245\b\7\b\2\u0245\24\3\2\2\2\u0246\u0247\5\u011d\u008c"+
		"\2\u0247\u0248\3\2\2\2\u0248\u0249\b\b\3\2\u0249\26\3\2\2\2\u024a\u024b"+
		"\5\63\27\2\u024b\u024c\3\2\2\2\u024c\u024d\b\t\t\2\u024d\30\3\2\2\2\u024e"+
		"\u024f\5\u0123\u008f\2\u024f\u0250\3\2\2\2\u0250\u0251\b\n\n\2\u0251\u0252"+
		"\b\n\b\2\u0252\32\3\2\2\2\u0253\u0254\5\u011d\u008c\2\u0254\u0255\3\2"+
		"\2\2\u0255\u0256\b\13\13\2\u0256\u0257\b\13\3\2\u0257\34\3\2\2\2\u0258"+
		"\u0259\5\67\31\2\u0259\u025a\3\2\2\2\u025a\u025b\b\f\t\2\u025b\36\3\2"+
		"\2\2\u025c\u025e\5\u011d\u008c\2\u025d\u025c\3\2\2\2\u025d\u025e\3\2\2"+
		"\2\u025e\u025f\3\2\2\2\u025f\u0260\5\u011b\u008b\2\u0260\u0261\3\2\2\2"+
		"\u0261\u0262\b\r\n\2\u0262\u0263\b\r\b\2\u0263 \3\2\2\2\u0264\u0265\5"+
		"\u011d\u008c\2\u0265\u0266\6\16\6\2\u0266\u0267\3\2\2\2\u0267\u0268\b"+
		"\16\13\2\u0268\u0269\b\16\3\2\u0269\"\3\2\2\2\u026a\u026b\5;\33\2\u026b"+
		"\u026c\3\2\2\2\u026c\u026d\b\17\t\2\u026d$\3\2\2\2\u026e\u026f\5\u0129"+
		"\u0092\2\u026f\u0270\3\2\2\2\u0270\u0271\b\20\n\2\u0271\u0272\b\20\b\2"+
		"\u0272&\3\2\2\2\u0273\u0274\5\u011d\u008c\2\u0274\u0275\6\21\7\2\u0275"+
		"\u0276\3\2\2\2\u0276\u0277\b\21\13\2\u0277\u0278\b\21\3\2\u0278(\3\2\2"+
		"\2\u0279\u027a\5=\34\2\u027a\u027b\3\2\2\2\u027b\u027c\b\22\t\2\u027c"+
		"*\3\2\2\2\u027d\u027e\7}\2\2\u027e\u027f\b\23\f\2\u027f\u0280\3\2\2\2"+
		"\u0280\u0281\b\23\r\2\u0281\u0282\b\23\b\2\u0282\u0283\b\23\16\2\u0283"+
		",\3\2\2\2\u0284\u0285\5\u01b9\u00da\2\u0285\u0286\3\2\2\2\u0286\u0287"+
		"\b\24\17\2\u0287\u0288\b\24\b\2\u0288\u0289\b\24\20\2\u0289.\3\2\2\2\u028a"+
		"\u028b\5\u0105\u0080\2\u028b\u028c\5\u01b9\u00da\2\u028c\60\3\2\2\2\u028d"+
		"\u028e\13\2\2\2\u028e\u028f\b\26\21\2\u028f\u0290\3\2\2\2\u0290\u0291"+
		"\b\26\b\2\u0291\62\3\2\2\2\u0292\u0295\n\2\2\2\u0293\u0295\5\u0109\u0082"+
		"\2\u0294\u0292\3\2\2\2\u0294\u0293\3\2\2\2\u0295\64\3\2\2\2\u0296\u0299"+
		"\n\3\2\2\u0297\u0299\5\u0109\u0082\2\u0298\u0296\3\2\2\2\u0298\u0297\3"+
		"\2\2\2\u0299\66\3\2\2\2\u029a\u02a0\n\4\2\2\u029b\u029c\5\u011f\u008d"+
		"\2\u029c\u029d\6\31\b\2\u029d\u02a0\3\2\2\2\u029e\u02a0\5\u0109\u0082"+
		"\2\u029f\u029a\3\2\2\2\u029f\u029b\3\2\2\2\u029f\u029e\3\2\2\2\u02a08"+
		"\3\2\2\2\u02a1\u02a7\n\5\2\2\u02a2\u02a3\5\u0121\u008e\2\u02a3\u02a4\6"+
		"\32\t\2\u02a4\u02a7\3\2\2\2\u02a5\u02a7\5\u0109\u0082\2\u02a6\u02a1\3"+
		"\2\2\2\u02a6\u02a2\3\2\2\2\u02a6\u02a5\3\2\2\2\u02a7:\3\2\2\2\u02a8\u02ae"+
		"\5\u0117\u0089\2\u02a9\u02aa\5\u011d\u008c\2\u02aa\u02ab\6\33\n\2\u02ab"+
		"\u02ae\3\2\2\2\u02ac\u02ae\n\6\2\2\u02ad\u02a8\3\2\2\2\u02ad\u02a9\3\2"+
		"\2\2\u02ad\u02ac\3\2\2\2\u02ae<\3\2\2\2\u02af\u02be\5\u012d\u0094\2\u02b0"+
		"\u02b1\5\u012f\u0095\2\u02b1\u02b2\6\34\13\2\u02b2\u02be\3\2\2\2\u02b3"+
		"\u02b4\5\u012b\u0093\2\u02b4\u02b5\6\34\f\2\u02b5\u02be\3\2\2\2\u02b6"+
		"\u02b7\5\u011b\u008b\2\u02b7\u02b8\6\34\r\2\u02b8\u02be\3\2\2\2\u02b9"+
		"\u02ba\5\u011d\u008c\2\u02ba\u02bb\6\34\16\2\u02bb\u02be\3\2\2\2\u02bc"+
		"\u02be\n\6\2\2\u02bd\u02af\3\2\2\2\u02bd\u02b0\3\2\2\2\u02bd\u02b3\3\2"+
		"\2\2\u02bd\u02b6\3\2\2\2\u02bd\u02b9\3\2\2\2\u02bd\u02bc\3\2\2\2\u02be"+
		">\3\2\2\2\u02bf\u02c0\7c\2\2\u02c0\u02c1\7u\2\2\u02c1@\3\2\2\2\u02c2\u02c3"+
		"\7f\2\2\u02c3\u02c4\7g\2\2\u02c4\u02c5\7h\2\2\u02c5B\3\2\2\2\u02c6\u02c7"+
		"\7k\2\2\u02c7\u02c8\7p\2\2\u02c8D\3\2\2\2\u02c9\u02ca\7v\2\2\u02ca\u02cb"+
		"\7t\2\2\u02cb\u02cc\7c\2\2\u02cc\u02cd\7k\2\2\u02cd\u02ce\7v\2\2\u02ce"+
		"F\3\2\2\2\u02cf\u02d0\7v\2\2\u02d0\u02d1\7j\2\2\u02d1\u02d2\7t\2\2\u02d2"+
		"\u02d3\7g\2\2\u02d3\u02d4\7c\2\2\u02d4\u02d5\7f\2\2\u02d5\u02d6\7u\2\2"+
		"\u02d6\u02d7\7c\2\2\u02d7\u02d8\7h\2\2\u02d8\u02d9\7g\2\2\u02d9H\3\2\2"+
		"\2\u02da\u02db\7x\2\2\u02db\u02dc\7c\2\2\u02dc\u02dd\7t\2\2\u02ddJ\3\2"+
		"\2\2\u02de\u02e7\5Q&\2\u02df\u02e7\5],\2\u02e0\u02e7\5W)\2\u02e1\u02e7"+
		"\5\u009fM\2\u02e2\u02e7\5\u0083?\2\u02e3\u02e7\5\u0087A\2\u02e4\u02e7"+
		"\5u8\2\u02e5\u02e7\5i\62\2\u02e6\u02de\3\2\2\2\u02e6\u02df\3\2\2\2\u02e6"+
		"\u02e0\3\2\2\2\u02e6\u02e1\3\2\2\2\u02e6\u02e2\3\2\2\2\u02e6\u02e3\3\2"+
		"\2\2\u02e6\u02e4\3\2\2\2\u02e6\u02e5\3\2\2\2\u02e7L\3\2\2\2\u02e8\u02e9"+
		"\7c\2\2\u02e9\u02ea\7d\2\2\u02ea\u02eb\7u\2\2\u02eb\u02ec\7v\2\2\u02ec"+
		"\u02ed\7t\2\2\u02ed\u02ee\7c\2\2\u02ee\u02ef\7e\2\2\u02ef\u02f0\7v\2\2"+
		"\u02f0N\3\2\2\2\u02f1\u02f2\7c\2\2\u02f2\u02f3\7u\2\2\u02f3\u02f4\7u\2"+
		"\2\u02f4\u02f5\7g\2\2\u02f5\u02f6\7t\2\2\u02f6\u02f7\7v\2\2\u02f7P\3\2"+
		"\2\2\u02f8\u02f9\7d\2\2\u02f9\u02fa\7q\2\2\u02fa\u02fb\7q\2\2\u02fb\u02fc"+
		"\7n\2\2\u02fc\u02fd\7g\2\2\u02fd\u02fe\7c\2\2\u02fe\u02ff\7p\2\2\u02ff"+
		"R\3\2\2\2\u0300\u0301\7d\2\2\u0301\u0302\7t\2\2\u0302\u0303\7g\2\2\u0303"+
		"\u0304\7c\2\2\u0304\u0305\7m\2\2\u0305T\3\2\2\2\u0306\u0307\7{\2\2\u0307"+
		"\u0308\7k\2\2\u0308\u0309\7g\2\2\u0309\u030a\7n\2\2\u030a\u030b\7f\2\2"+
		"\u030bV\3\2\2\2\u030c\u030d\7d\2\2\u030d\u030e\7{\2\2\u030e\u030f\7v\2"+
		"\2\u030f\u0310\7g\2\2\u0310X\3\2\2\2\u0311\u0312\7e\2\2\u0312\u0313\7"+
		"c\2\2\u0313\u0314\7u\2\2\u0314\u0315\7g\2\2\u0315Z\3\2\2\2\u0316\u0317"+
		"\7e\2\2\u0317\u0318\7c\2\2\u0318\u0319\7v\2\2\u0319\u031a\7e\2\2\u031a"+
		"\u031b\7j\2\2\u031b\\\3\2\2\2\u031c\u031d\7e\2\2\u031d\u031e\7j\2\2\u031e"+
		"\u031f\7c\2\2\u031f\u0320\7t\2\2\u0320^\3\2\2\2\u0321\u0322\7e\2\2\u0322"+
		"\u0323\7n\2\2\u0323\u0324\7c\2\2\u0324\u0325\7u\2\2\u0325\u0326\7u\2\2"+
		"\u0326`\3\2\2\2\u0327\u0328\7e\2\2\u0328\u0329\7q\2\2\u0329\u032a\7p\2"+
		"\2\u032a\u032b\7u\2\2\u032b\u032c\7v\2\2\u032cb\3\2\2\2\u032d\u032e\7"+
		"e\2\2\u032e\u032f\7q\2\2\u032f\u0330\7p\2\2\u0330\u0331\7v\2\2\u0331\u0332"+
		"\7k\2\2\u0332\u0333\7p\2\2\u0333\u0334\7w\2\2\u0334\u0335\7g\2\2\u0335"+
		"d\3\2\2\2\u0336\u0337\7f\2\2\u0337\u0338\7g\2\2\u0338\u0339\7h\2\2\u0339"+
		"\u033a\7c\2\2\u033a\u033b\7w\2\2\u033b\u033c\7n\2\2\u033c\u033d\7v\2\2"+
		"\u033df\3\2\2\2\u033e\u033f\7f\2\2\u033f\u0340\7q\2\2\u0340h\3\2\2\2\u0341"+
		"\u0342\7f\2\2\u0342\u0343\7q\2\2\u0343\u0344\7w\2\2\u0344\u0345\7d\2\2"+
		"\u0345\u0346\7n\2\2\u0346\u0347\7g\2\2\u0347j\3\2\2\2\u0348\u0349\7g\2"+
		"\2\u0349\u034a\7n\2\2\u034a\u034b\7u\2\2\u034b\u034c\7g\2\2\u034cl\3\2"+
		"\2\2\u034d\u034e\7g\2\2\u034e\u034f\7p\2\2\u034f\u0350\7w\2\2\u0350\u0351"+
		"\7o\2\2\u0351n\3\2\2\2\u0352\u0353\7g\2\2\u0353\u0354\7z\2\2\u0354\u0355"+
		"\7v\2\2\u0355\u0356\7g\2\2\u0356\u0357\7p\2\2\u0357\u0358\7f\2\2\u0358"+
		"\u0359\7u\2\2\u0359p\3\2\2\2\u035a\u035b\7h\2\2\u035b\u035c\7k\2\2\u035c"+
		"\u035d\7p\2\2\u035d\u035e\7c\2\2\u035e\u035f\7n\2\2\u035fr\3\2\2\2\u0360"+
		"\u0361\7h\2\2\u0361\u0362\7k\2\2\u0362\u0363\7p\2\2\u0363\u0364\7c\2\2"+
		"\u0364\u0365\7n\2\2\u0365\u0366\7n\2\2\u0366\u0367\7{\2\2\u0367t\3\2\2"+
		"\2\u0368\u0369\7h\2\2\u0369\u036a\7n\2\2\u036a\u036b\7q\2\2\u036b\u036c"+
		"\7c\2\2\u036c\u036d\7v\2\2\u036dv\3\2\2\2\u036e\u036f\7h\2\2\u036f\u0370"+
		"\7q\2\2\u0370\u0371\7t\2\2\u0371x\3\2\2\2\u0372\u0373\7k\2\2\u0373\u0374"+
		"\7h\2\2\u0374z\3\2\2\2\u0375\u0376\7i\2\2\u0376\u0377\7q\2\2\u0377\u0378"+
		"\7v\2\2\u0378\u0379\7q\2\2\u0379|\3\2\2\2\u037a\u037b\7k\2\2\u037b\u037c"+
		"\7o\2\2\u037c\u037d\7r\2\2\u037d\u037e\7n\2\2\u037e\u037f\7g\2\2\u037f"+
		"\u0380\7o\2\2\u0380\u0381\7g\2\2\u0381\u0382\7p\2\2\u0382\u0383\7v\2\2"+
		"\u0383\u0384\7u\2\2\u0384~\3\2\2\2\u0385\u0386\7k\2\2\u0386\u0387\7o\2"+
		"\2\u0387\u0388\7r\2\2\u0388\u0389\7q\2\2\u0389\u038a\7t\2\2\u038a\u038b"+
		"\7v\2\2\u038b\u0080\3\2\2\2\u038c\u038d\7k\2\2\u038d\u038e\7p\2\2\u038e"+
		"\u038f\7u\2\2\u038f\u0390\7v\2\2\u0390\u0391\7c\2\2\u0391\u0392\7p\2\2"+
		"\u0392\u0393\7e\2\2\u0393\u0394\7g\2\2\u0394\u0395\7q\2\2\u0395\u0396"+
		"\7h\2\2\u0396\u0082\3\2\2\2\u0397\u0398\7k\2\2\u0398\u0399\7p\2\2\u0399"+
		"\u039a\7v\2\2\u039a\u0084\3\2\2\2\u039b\u039c\7k\2\2\u039c\u039d\7p\2"+
		"\2\u039d\u039e\7v\2\2\u039e\u039f\7g\2\2\u039f\u03a0\7t\2\2\u03a0\u03a1"+
		"\7h\2\2\u03a1\u03a2\7c\2\2\u03a2\u03a3\7e\2\2\u03a3\u03a4\7g\2\2\u03a4"+
		"\u0086\3\2\2\2\u03a5\u03a6\7n\2\2\u03a6\u03a7\7q\2\2\u03a7\u03a8\7p\2"+
		"\2\u03a8\u03a9\7i\2\2\u03a9\u0088\3\2\2\2\u03aa\u03ab\7p\2\2\u03ab\u03ac"+
		"\7c\2\2\u03ac\u03ad\7v\2\2\u03ad\u03ae\7k\2\2\u03ae\u03af\7x\2\2\u03af"+
		"\u03b0\7g\2\2\u03b0\u008a\3\2\2\2\u03b1\u03b2\7p\2\2\u03b2\u03b3\7g\2"+
		"\2\u03b3\u03b4\7y\2\2\u03b4\u008c\3\2\2\2\u03b5\u03b6\7p\2\2\u03b6\u03b7"+
		"\7q\2\2\u03b7\u03b8\7p\2\2\u03b8\u03b9\7/\2\2\u03b9\u03ba\7u\2\2\u03ba"+
		"\u03bb\7g\2\2\u03bb\u03bc\7c\2\2\u03bc\u03bd\7n\2\2\u03bd\u03be\7g\2\2"+
		"\u03be\u03bf\7f\2\2\u03bf\u008e\3\2\2\2\u03c0\u03c1\7r\2\2\u03c1\u03c2"+
		"\7c\2\2\u03c2\u03c3\7e\2\2\u03c3\u03c4\7m\2\2\u03c4\u03c5\7c\2\2\u03c5"+
		"\u03c6\7i\2\2\u03c6\u03c7\7g\2\2\u03c7\u0090\3\2\2\2\u03c8\u03c9\7r\2"+
		"\2\u03c9\u03ca\7g\2\2\u03ca\u03cb\7t\2\2\u03cb\u03cc\7o\2\2\u03cc\u03cd"+
		"\7k\2\2\u03cd\u03ce\7v\2\2\u03ce\u03cf\7u\2\2\u03cf\u0092\3\2\2\2\u03d0"+
		"\u03d1\7r\2\2\u03d1\u03d2\7t\2\2\u03d2\u03d3\7k\2\2\u03d3\u03d4\7x\2\2"+
		"\u03d4\u03d5\7c\2\2\u03d5\u03d6\7v\2\2\u03d6\u03d7\7g\2\2\u03d7\u0094"+
		"\3\2\2\2\u03d8\u03d9\7r\2\2\u03d9\u03da\7t\2\2\u03da\u03db\7q\2\2\u03db"+
		"\u03dc\7v\2\2\u03dc\u03dd\7g\2\2\u03dd\u03de\7e\2\2\u03de\u03df\7v\2\2"+
		"\u03df\u03e0\7g\2\2\u03e0\u03e1\7f\2\2\u03e1\u0096\3\2\2\2\u03e2\u03e3"+
		"\7r\2\2\u03e3\u03e4\7w\2\2\u03e4\u03e5\7d\2\2\u03e5\u03e6\7n\2\2\u03e6"+
		"\u03e7\7k\2\2\u03e7\u03e8\7e\2\2\u03e8\u0098\3\2\2\2\u03e9\u03ea\7t\2"+
		"\2\u03ea\u03eb\7g\2\2\u03eb\u03ec\7e\2\2\u03ec\u03ed\7q\2\2\u03ed\u03ee"+
		"\7t\2\2\u03ee\u03ef\7f\2\2\u03ef\u009a\3\2\2\2\u03f0\u03f1\7t\2\2\u03f1"+
		"\u03f2\7g\2\2\u03f2\u03f3\7v\2\2\u03f3\u03f4\7w\2\2\u03f4\u03f5\7t\2\2"+
		"\u03f5\u03f6\7p\2\2\u03f6\u009c\3\2\2\2\u03f7\u03f8\7u\2\2\u03f8\u03f9"+
		"\7g\2\2\u03f9\u03fa\7c\2\2\u03fa\u03fb\7n\2\2\u03fb\u03fc\7g\2\2\u03fc"+
		"\u03fd\7f\2\2\u03fd\u009e\3\2\2\2\u03fe\u03ff\7u\2\2\u03ff\u0400\7j\2"+
		"\2\u0400\u0401\7q\2\2\u0401\u0402\7t\2\2\u0402\u0403\7v\2\2\u0403\u00a0"+
		"\3\2\2\2\u0404\u0405\7u\2\2\u0405\u0406\7v\2\2\u0406\u0407\7c\2\2\u0407"+
		"\u0408\7v\2\2\u0408\u0409\7k\2\2\u0409\u040a\7e\2\2\u040a\u00a2\3\2\2"+
		"\2\u040b\u040c\7u\2\2\u040c\u040d\7v\2\2\u040d\u040e\7t\2\2\u040e\u040f"+
		"\7k\2\2\u040f\u0410\7e\2\2\u0410\u0411\7v\2\2\u0411\u0412\7h\2\2\u0412"+
		"\u0413\7r\2\2\u0413\u00a4\3\2\2\2\u0414\u0415\7u\2\2\u0415\u0416\7w\2"+
		"\2\u0416\u0417\7r\2\2\u0417\u0418\7g\2\2\u0418\u0419\7t\2\2\u0419\u00a6"+
		"\3\2\2\2\u041a\u041b\7u\2\2\u041b\u041c\7y\2\2\u041c\u041d\7k\2\2\u041d"+
		"\u041e\7v\2\2\u041e\u041f\7e\2\2\u041f\u0420\7j\2\2\u0420\u00a8\3\2\2"+
		"\2\u0421\u0422\7u\2\2\u0422\u0423\7{\2\2\u0423\u0424\7p\2\2\u0424\u0425"+
		"\7e\2\2\u0425\u0426\7j\2\2\u0426\u0427\7t\2\2\u0427\u0428\7q\2\2\u0428"+
		"\u0429\7p\2\2\u0429\u042a\7k\2\2\u042a\u042b\7|\2\2\u042b\u042c\7g\2\2"+
		"\u042c\u042d\7f\2\2\u042d\u00aa\3\2\2\2\u042e\u042f\7v\2\2\u042f\u0430"+
		"\7j\2\2\u0430\u0431\7k\2\2\u0431\u0432\7u\2\2\u0432\u00ac\3\2\2\2\u0433"+
		"\u0434\7v\2\2\u0434\u0435\7j\2\2\u0435\u0436\7t\2\2\u0436\u0437\7q\2\2"+
		"\u0437\u0438\7y\2\2\u0438\u00ae\3\2\2\2\u0439\u043a\7v\2\2\u043a\u043b"+
		"\7j\2\2\u043b\u043c\7t\2\2\u043c\u043d\7q\2\2\u043d\u043e\7y\2\2\u043e"+
		"\u043f\7u\2\2\u043f\u00b0\3\2\2\2\u0440\u0441\7v\2\2\u0441\u0442\7t\2"+
		"\2\u0442\u0443\7c\2\2\u0443\u0444\7p\2\2\u0444\u0445\7u\2\2\u0445\u0446"+
		"\7k\2\2\u0446\u0447\7g\2\2\u0447\u0448\7p\2\2\u0448\u0449\7v\2\2\u0449"+
		"\u00b2\3\2\2\2\u044a\u044b\7v\2\2\u044b\u044c\7t\2\2\u044c\u044d\7{\2"+
		"\2\u044d\u00b4\3\2\2\2\u044e\u044f\7x\2\2\u044f\u0450\7q\2\2\u0450\u0451"+
		"\7k\2\2\u0451\u0452\7f\2\2\u0452\u00b6\3\2\2\2\u0453\u0454\7x\2\2\u0454"+
		"\u0455\7q\2\2\u0455\u0456\7n\2\2\u0456\u0457\7c\2\2\u0457\u0458\7v\2\2"+
		"\u0458\u0459\7k\2\2\u0459\u045a\7n\2\2\u045a\u045b\7g\2\2\u045b\u00b8"+
		"\3\2\2\2\u045c\u045d\7y\2\2\u045d\u045e\7j\2\2\u045e\u045f\7k\2\2\u045f"+
		"\u0460\7n\2\2\u0460\u0461\7g\2\2\u0461\u00ba\3\2\2\2\u0462\u0467\5\u00bf"+
		"]\2\u0463\u0467\5\u00c1^\2\u0464\u0467\5\u00c3_\2\u0465\u0467\5\u00c5"+
		"`\2\u0466\u0462\3\2\2\2\u0466\u0463\3\2\2\2\u0466\u0464\3\2\2\2\u0466"+
		"\u0465\3\2\2\2\u0467\u046b\3\2\2\2\u0468\u0469\5\u00d5h\2\u0469\u046a"+
		"\b[\22\2\u046a\u046c\3\2\2\2\u046b\u0468\3\2\2\2\u046b\u046c\3\2\2\2\u046c"+
		"\u0479\3\2\2\2\u046d\u0470\5\u00bd\\\2\u046e\u046f\t\7\2\2\u046f\u0471"+
		"\b[\23\2\u0470\u046e\3\2\2\2\u0471\u0472\3\2\2\2\u0472\u0470\3\2\2\2\u0472"+
		"\u0473\3\2\2\2\u0473\u0474\3\2\2\2\u0474\u0476\b[\24\2\u0475\u0477\5\u00c7"+
		"a\2\u0476\u0475\3\2\2\2\u0476\u0477\3\2\2\2\u0477\u0479\3\2\2\2\u0478"+
		"\u0466\3\2\2\2\u0478\u046d\3\2\2\2\u0479\u00bc\3\2\2\2\u047a\u047b\7\62"+
		"\2\2\u047b\u00be\3\2\2\2\u047c\u047e\5\u00c9b\2\u047d\u047f\5\u00c7a\2"+
		"\u047e\u047d\3\2\2\2\u047e\u047f\3\2\2\2\u047f\u00c0\3\2\2\2\u0480\u0482"+
		"\5\u00d7i\2\u0481\u0483\5\u00c7a\2\u0482\u0481\3\2\2\2\u0482\u0483\3\2"+
		"\2\2\u0483\u00c2\3\2\2\2\u0484\u0486\5\u00dfm\2\u0485\u0487\5\u00c7a\2"+
		"\u0486\u0485\3\2\2\2\u0486\u0487\3\2\2\2\u0487\u00c4\3\2\2\2\u0488\u048a"+
		"\5\u00e7q\2\u0489\u048b\5\u00c7a\2\u048a\u0489\3\2\2\2\u048a\u048b\3\2"+
		"\2\2\u048b\u00c6\3\2\2\2\u048c\u048d\t\b\2\2\u048d\u00c8\3\2\2\2\u048e"+
		"\u0499\5\u00bd\\\2\u048f\u0496\5\u00cfe\2\u0490\u0492\5\u00cbc\2\u0491"+
		"\u0490\3\2\2\2\u0491\u0492\3\2\2\2\u0492\u0497\3\2\2\2\u0493\u0494\5\u00d3"+
		"g\2\u0494\u0495\5\u00cbc\2\u0495\u0497\3\2\2\2\u0496\u0491\3\2\2\2\u0496"+
		"\u0493\3\2\2\2\u0497\u0499\3\2\2\2\u0498\u048e\3\2\2\2\u0498\u048f\3\2"+
		"\2\2\u0499\u00ca\3\2\2\2\u049a\u04a2\5\u00cdd\2\u049b\u049d\5\u00d1f\2"+
		"\u049c\u049b\3\2\2\2\u049d\u04a0\3\2\2\2\u049e\u049c\3\2\2\2\u049e\u049f"+
		"\3\2\2\2\u049f\u04a1\3\2\2\2\u04a0\u049e\3\2\2\2\u04a1\u04a3\5\u00cdd"+
		"\2\u04a2\u049e\3\2\2\2\u04a2\u04a3\3\2\2\2\u04a3\u00cc\3\2\2\2\u04a4\u04a7"+
		"\5\u00bd\\\2\u04a5\u04a7\5\u00cfe\2\u04a6\u04a4\3\2\2\2\u04a6\u04a5\3"+
		"\2\2\2\u04a7\u00ce\3\2\2\2\u04a8\u04a9\t\t\2\2\u04a9\u00d0\3\2\2\2\u04aa"+
		"\u04ad\5\u00cdd\2\u04ab\u04ad\5\u00d5h\2\u04ac\u04aa\3\2\2\2\u04ac\u04ab"+
		"\3\2\2\2\u04ad\u00d2\3\2\2\2\u04ae\u04b0\5\u00d5h\2\u04af\u04ae\3\2\2"+
		"\2\u04b0\u04b1\3\2\2\2\u04b1\u04af\3\2\2\2\u04b1\u04b2\3\2\2\2\u04b2\u00d4"+
		"\3\2\2\2\u04b3\u04b4\7a\2\2\u04b4\u00d6\3\2\2\2\u04b5\u04b6\5\u00bd\\"+
		"\2\u04b6\u04b7\t\n\2\2\u04b7\u04b8\5\u00d9j\2\u04b8\u00d8\3\2\2\2\u04b9"+
		"\u04c1\5\u00dbk\2\u04ba\u04bc\5\u00ddl\2\u04bb\u04ba\3\2\2\2\u04bc\u04bf"+
		"\3\2\2\2\u04bd\u04bb\3\2\2\2\u04bd\u04be\3\2\2\2\u04be\u04c0\3\2\2\2\u04bf"+
		"\u04bd\3\2\2\2\u04c0\u04c2\5\u00dbk\2\u04c1\u04bd\3\2\2\2\u04c1\u04c2"+
		"\3\2\2\2\u04c2\u00da\3\2\2\2\u04c3\u04c4\t\13\2\2\u04c4\u00dc\3\2\2\2"+
		"\u04c5\u04c8\5\u00dbk\2\u04c6\u04c8\5\u00d5h\2\u04c7\u04c5\3\2\2\2\u04c7"+
		"\u04c6\3\2\2\2\u04c8\u00de\3\2\2\2\u04c9\u04cb\5\u00bd\\\2\u04ca\u04cc"+
		"\5\u00d3g\2\u04cb\u04ca\3\2\2\2\u04cb\u04cc\3\2\2\2\u04cc\u04cd\3\2\2"+
		"\2\u04cd\u04ce\5\u00e1n\2\u04ce\u00e0\3\2\2\2\u04cf\u04d7\5\u00e3o\2\u04d0"+
		"\u04d2\5\u00e5p\2\u04d1\u04d0\3\2\2\2\u04d2\u04d5\3\2\2\2\u04d3\u04d1"+
		"\3\2\2\2\u04d3\u04d4\3\2\2\2\u04d4\u04d6\3\2\2\2\u04d5\u04d3\3\2\2\2\u04d6"+
		"\u04d8\5\u00e3o\2\u04d7\u04d3\3\2\2\2\u04d7\u04d8\3\2\2\2\u04d8\u00e2"+
		"\3\2\2\2\u04d9\u04da\t\f\2\2\u04da\u00e4\3\2\2\2\u04db\u04de\5\u00e3o"+
		"\2\u04dc\u04de\5\u00d5h\2\u04dd\u04db\3\2\2\2\u04dd\u04dc\3\2\2\2\u04de"+
		"\u00e6\3\2\2\2\u04df\u04e0\5\u00bd\\\2\u04e0\u04e1\t\r\2\2\u04e1\u04e2"+
		"\5\u00e9r\2\u04e2\u00e8\3\2\2\2\u04e3\u04eb\5\u00ebs\2\u04e4\u04e6\5\u00ed"+
		"t\2\u04e5\u04e4\3\2\2\2\u04e6\u04e9\3\2\2\2\u04e7\u04e5\3\2\2\2\u04e7"+
		"\u04e8\3\2\2\2\u04e8\u04ea\3\2\2\2\u04e9\u04e7\3\2\2\2\u04ea\u04ec\5\u00eb"+
		"s\2\u04eb\u04e7\3\2\2\2\u04eb\u04ec\3\2\2\2\u04ec\u00ea\3\2\2\2\u04ed"+
		"\u04ee\t\16\2\2\u04ee\u00ec\3\2\2\2\u04ef\u04f2\5\u00ebs\2\u04f0\u04f2"+
		"\5\u00d5h\2\u04f1\u04ef\3\2\2\2\u04f1\u04f0\3\2\2\2\u04f2\u00ee\3\2\2"+
		"\2\u04f3\u04f6\5\u00f1v\2\u04f4\u04f6\5\u00fd|\2\u04f5\u04f3\3\2\2\2\u04f5"+
		"\u04f4\3\2\2\2\u04f6\u04fa\3\2\2\2\u04f7\u04f8\5\u00d5h\2\u04f8\u04f9"+
		"\bu\25\2\u04f9\u04fb\3\2\2\2\u04fa\u04f7\3\2\2\2\u04fa\u04fb\3\2\2\2\u04fb"+
		"\u00f0\3\2\2\2\u04fc\u04fe\5\u00cbc\2\u04fd\u04fc\3\2\2\2\u04fd\u04fe"+
		"\3\2\2\2\u04fe\u04ff\3\2\2\2\u04ff\u0500\5\u0105\u0080\2\u0500\u0502\5"+
		"\u00cbc\2\u0501\u0503\5\u00f3w\2\u0502\u0501\3\2\2\2\u0502\u0503\3\2\2"+
		"\2\u0503\u0505\3\2\2\2\u0504\u0506\5\u00fb{\2\u0505\u0504\3\2\2\2\u0505"+
		"\u0506\3\2\2\2\u0506\u0510\3\2\2\2\u0507\u0508\5\u00cbc\2\u0508\u050a"+
		"\5\u00f3w\2\u0509\u050b\5\u00fb{\2\u050a\u0509\3\2\2\2\u050a\u050b\3\2"+
		"\2\2\u050b\u0510\3\2\2\2\u050c\u050d\5\u00cbc\2\u050d\u050e\5\u00fb{\2"+
		"\u050e\u0510\3\2\2\2\u050f\u04fd\3\2\2\2\u050f\u0507\3\2\2\2\u050f\u050c"+
		"\3\2\2\2\u0510\u00f2\3\2\2\2\u0511\u0512\5\u00f5x\2\u0512\u0513\5\u00f7"+
		"y\2\u0513\u00f4\3\2\2\2\u0514\u0515\t\17\2\2\u0515\u00f6\3\2\2\2\u0516"+
		"\u0518\5\u00f9z\2\u0517\u0516\3\2\2\2\u0517\u0518\3\2\2\2\u0518\u0519"+
		"\3\2\2\2\u0519\u051a\5\u00cbc\2\u051a\u00f8\3\2\2\2\u051b\u051c\t\20\2"+
		"\2\u051c\u00fa\3\2\2\2\u051d\u051e\t\21\2\2\u051e\u00fc\3\2\2\2\u051f"+
		"\u0520\5\u00ff}\2\u0520\u0522\5\u0101~\2\u0521\u0523\5\u00fb{\2\u0522"+
		"\u0521\3\2\2\2\u0522\u0523\3\2\2\2\u0523\u00fe\3\2\2\2\u0524\u0526\5\u00d7"+
		"i\2\u0525\u0527\5\u0105\u0080\2\u0526\u0525\3\2\2\2\u0526\u0527\3\2\2"+
		"\2\u0527\u0531\3\2\2\2\u0528\u0529\5\u00bd\\\2\u0529\u052b\t\n\2\2\u052a"+
		"\u052c\5\u00d9j\2\u052b\u052a\3\2\2\2\u052b\u052c\3\2\2\2\u052c\u052d"+
		"\3\2\2\2\u052d\u052e\5\u0105\u0080\2\u052e\u052f\5\u00d9j\2\u052f\u0531"+
		"\3\2\2\2\u0530\u0524\3\2\2\2\u0530\u0528\3\2\2\2\u0531\u0100\3\2\2\2\u0532"+
		"\u0533\5\u0103\177\2\u0533\u0534\5\u00f7y\2\u0534\u0102\3\2\2\2\u0535"+
		"\u0536\t\22\2\2\u0536\u0104\3\2\2\2\u0537\u0538\7\60\2\2\u0538\u0106\3"+
		"\2\2\2\u0539\u053a\7v\2\2\u053a\u053b\7t\2\2\u053b\u053c\7w\2\2\u053c"+
		"\u0543\7g\2\2\u053d\u053e\7h\2\2\u053e\u053f\7c\2\2\u053f\u0540\7n\2\2"+
		"\u0540\u0541\7u\2\2\u0541\u0543\7g\2\2\u0542\u0539\3\2\2\2\u0542\u053d"+
		"\3\2\2\2\u0543\u0108\3\2\2\2\u0544\u0545\5\u0119\u008a\2\u0545\u0546\t"+
		"\23\2\2\u0546\u054c\3\2\2\2\u0547\u054c\5\u010b\u0083\2\u0548\u054c\5"+
		"\u010d\u0084\2\u0549\u054c\5\u0111\u0086\2\u054a\u054c\5\u0113\u0087\2"+
		"\u054b\u0544\3\2\2\2\u054b\u0547\3\2\2\2\u054b\u0548\3\2\2\2\u054b\u0549"+
		"\3\2\2\2\u054b\u054a\3\2\2\2\u054c\u010a\3\2\2\2\u054d\u054e\5\u0119\u008a"+
		"\2\u054e\u054f\5\u00e3o\2\u054f\u055a\3\2\2\2\u0550\u0551\5\u0119\u008a"+
		"\2\u0551\u0552\5\u00e3o\2\u0552\u0553\5\u00e3o\2\u0553\u055a\3\2\2\2\u0554"+
		"\u0555\5\u0119\u008a\2\u0555\u0556\5\u010f\u0085\2\u0556\u0557\5\u00e3"+
		"o\2\u0557\u0558\5\u00e3o\2\u0558\u055a\3\2\2\2\u0559\u054d\3\2\2\2\u0559"+
		"\u0550\3\2\2\2\u0559\u0554\3\2\2\2\u055a\u010c\3\2\2\2\u055b\u055c\5\u0119"+
		"\u008a\2\u055c\u055d\7w\2\2\u055d\u055e\5\u00dbk\2\u055e\u055f\5\u00db"+
		"k\2\u055f\u0560\5\u00dbk\2\u0560\u0561\5\u00dbk\2\u0561\u010e\3\2\2\2"+
		"\u0562\u0563\t\24\2\2\u0563\u0110\3\2\2\2\u0564\u0565\5\u0119\u008a\2"+
		"\u0565\u0566\5\u011d\u008c\2\u0566\u0112\3\2\2\2\u0567\u0568\5\u0119\u008a"+
		"\2\u0568\u0569\5\u0115\u0088\2\u0569\u0114\3\2\2\2\u056a\u056c\7\17\2"+
		"\2\u056b\u056a\3\2\2\2\u056b\u056c\3\2\2\2\u056c\u056d\3\2\2\2\u056d\u0570"+
		"\7\f\2\2\u056e\u0570\7\17\2\2\u056f\u056b\3\2\2\2\u056f\u056e\3\2\2\2"+
		"\u0570\u0116\3\2\2\2\u0571\u0572\5\u0119\u008a\2\u0572\u0573\5\u011b\u008b"+
		"\2\u0573\u0118\3\2\2\2\u0574\u0575\7^\2\2\u0575\u011a\3\2\2\2\u0576\u0577"+
		"\7\61\2\2\u0577\u011c\3\2\2\2\u0578\u0579\7&\2\2\u0579\u011e\3\2\2\2\u057a"+
		"\u057b\7$\2\2\u057b\u0120\3\2\2\2\u057c\u057d\7)\2\2\u057d\u0122\3\2\2"+
		"\2\u057e\u057f\7$\2\2\u057f\u0580\7$\2\2\u0580\u0581\7$\2\2\u0581\u0124"+
		"\3\2\2\2\u0582\u0583\7)\2\2\u0583\u0584\7)\2\2\u0584\u0585\7)\2\2\u0585"+
		"\u0126\3\2\2\2\u0586\u0587\7&\2\2\u0587\u0588\7\61\2\2\u0588\u0128\3\2"+
		"\2\2\u0589\u058a\7\61\2\2\u058a\u058b\7&\2\2\u058b\u012a\3\2\2\2\u058c"+
		"\u058d\7&\2\2\u058d\u058e\7\61\2\2\u058e\u012c\3\2\2\2\u058f\u0590\7&"+
		"\2\2\u0590\u0591\7&\2\2\u0591\u012e\3\2\2\2\u0592\u0593\7&\2\2\u0593\u0594"+
		"\7\61\2\2\u0594\u0595\7&\2\2\u0595\u0130\3\2\2\2\u0596\u0597\7p\2\2\u0597"+
		"\u0598\7w\2\2\u0598\u0599\7n\2\2\u0599\u059a\7n\2\2\u059a\u0132\3\2\2"+
		"\2\u059b\u059c\7\60\2\2\u059c\u059d\7\60\2\2\u059d\u0134\3\2\2\2\u059e"+
		"\u059f\7>\2\2\u059f\u05a0\7\60\2\2\u05a0\u05a1\7\60\2\2\u05a1\u0136\3"+
		"\2\2\2\u05a2\u05a3\7\60\2\2\u05a3\u05a4\7\60\2\2\u05a4\u05a5\7>\2\2\u05a5"+
		"\u0138\3\2\2\2\u05a6\u05a7\7>\2\2\u05a7\u05a8\7\60\2\2\u05a8\u05a9\7\60"+
		"\2\2\u05a9\u05aa\7>\2\2\u05aa\u013a\3\2\2\2\u05ab\u05ac\7,\2\2\u05ac\u05ad"+
		"\7\60\2\2\u05ad\u013c\3\2\2\2\u05ae\u05af\7A\2\2\u05af\u05b0\7\60\2\2"+
		"\u05b0\u013e\3\2\2\2\u05b1\u05b2\7A\2\2\u05b2\u05b3\7]\2\2\u05b3\u05b4"+
		"\3\2\2\2\u05b4\u05b5\b\u009d\26\2\u05b5\u05b6\3\2\2\2\u05b6\u05b7\b\u009d"+
		"\16\2\u05b7\u0140\3\2\2\2\u05b8\u05b9\7A\2\2\u05b9\u05ba\7A\2\2\u05ba"+
		"\u05bb\7\60\2\2\u05bb\u0142\3\2\2\2\u05bc\u05bd\7A\2\2\u05bd\u05be\7<"+
		"\2\2\u05be\u0144\3\2\2\2\u05bf\u05c0\7\60\2\2\u05c0\u05c1\7(\2\2\u05c1"+
		"\u0146\3\2\2\2\u05c2\u05c3\7<\2\2\u05c3\u05c4\7<\2\2\u05c4\u0148\3\2\2"+
		"\2\u05c5\u05c6\7?\2\2\u05c6\u05c7\7\u0080\2\2\u05c7\u014a\3\2\2\2\u05c8"+
		"\u05c9\7?\2\2\u05c9\u05ca\7?\2\2\u05ca\u05cb\7\u0080\2\2\u05cb\u014c\3"+
		"\2\2\2\u05cc\u05cd\7,\2\2\u05cd\u05ce\7,\2\2\u05ce\u014e\3\2\2\2\u05cf"+
		"\u05d0\7,\2\2\u05d0\u05d1\7,\2\2\u05d1\u05d2\7?\2\2\u05d2\u0150\3\2\2"+
		"\2\u05d3\u05d4\7>\2\2\u05d4\u05d5\7?\2\2\u05d5\u05d6\7@\2\2\u05d6\u0152"+
		"\3\2\2\2\u05d7\u05d8\7?\2\2\u05d8\u05d9\7?\2\2\u05d9\u05da\7?\2\2\u05da"+
		"\u0154\3\2\2\2\u05db\u05dc\7#\2\2\u05dc\u05dd\7?\2\2\u05dd\u05de\7?\2"+
		"\2\u05de\u0156\3\2\2\2\u05df\u05e0\7/\2\2\u05e0\u05e1\7@\2\2\u05e1\u0158"+
		"\3\2\2\2\u05e2\u05e3\7#\2\2\u05e3\u05e4\7k\2\2\u05e4\u05e5\7p\2\2\u05e5"+
		"\u05e6\7u\2\2\u05e6\u05e7\7v\2\2\u05e7\u05e8\7c\2\2\u05e8\u05e9\7p\2\2"+
		"\u05e9\u05ea\7e\2\2\u05ea\u05eb\7g\2\2\u05eb\u05ec\7q\2\2\u05ec\u05ed"+
		"\7h\2\2\u05ed\u05ee\3\2\2\2\u05ee\u05ef\6\u00aa\17\2\u05ef\u015a\3\2\2"+
		"\2\u05f0\u05f1\7#\2\2\u05f1\u05f2\7k\2\2\u05f2\u05f3\7p\2\2\u05f3\u05f4"+
		"\3\2\2\2\u05f4\u05f5\6\u00ab\20\2\u05f5\u015c\3\2\2\2\u05f6\u05f7\7*\2"+
		"\2\u05f7\u05f8\b\u00ac\27\2\u05f8\u05f9\3\2\2\2\u05f9\u05fa\b\u00ac\16"+
		"\2\u05fa\u015e\3\2\2\2\u05fb\u05fc\7+\2\2\u05fc\u05fd\b\u00ad\30\2\u05fd"+
		"\u05fe\3\2\2\2\u05fe\u05ff\b\u00ad\b\2\u05ff\u0160\3\2\2\2\u0600\u0601"+
		"\7}\2\2\u0601\u0602\b\u00ae\31\2\u0602\u0603\3\2\2\2\u0603\u0604\b\u00ae"+
		"\16\2\u0604\u0162\3\2\2\2\u0605\u0606\7\177\2\2\u0606\u0607\b\u00af\32"+
		"\2\u0607\u0608\3\2\2\2\u0608\u0609\b\u00af\b\2\u0609\u0164\3\2\2\2\u060a"+
		"\u060b\7]\2\2\u060b\u060c\b\u00b0\33\2\u060c\u060d\3\2\2\2\u060d\u060e"+
		"\b\u00b0\16\2\u060e\u0166\3\2\2\2\u060f\u0610\7_\2\2\u0610\u0611\b\u00b1"+
		"\34\2\u0611\u0612\3\2\2\2\u0612\u0613\b\u00b1\b\2\u0613\u0168\3\2\2\2"+
		"\u0614\u0615\7=\2\2\u0615\u016a\3\2\2\2\u0616\u0617\7.\2\2\u0617\u016c"+
		"\3\2\2\2\u0618\u0619\5\u0105\u0080\2\u0619\u016e\3\2\2\2\u061a\u061b\7"+
		"?\2\2\u061b\u0170\3\2\2\2\u061c\u061d\7@\2\2\u061d\u0172\3\2\2\2\u061e"+
		"\u061f\7>\2\2\u061f\u0174\3\2\2\2\u0620\u0621\7#\2\2\u0621\u0176\3\2\2"+
		"\2\u0622\u0623\7\u0080\2\2\u0623\u0178\3\2\2\2\u0624\u0625\7A\2\2\u0625"+
		"\u017a\3\2\2\2\u0626\u0627\7<\2\2\u0627\u017c\3\2\2\2\u0628\u0629\7?\2"+
		"\2\u0629\u062a\7?\2\2\u062a\u017e\3\2\2\2\u062b\u062c\7>\2\2\u062c\u062d"+
		"\7?\2\2\u062d\u0180\3\2\2\2\u062e\u062f\7@\2\2\u062f\u0630\7?\2\2\u0630"+
		"\u0182\3\2\2\2\u0631\u0632\7#\2\2\u0632\u0633\7?\2\2\u0633\u0184\3\2\2"+
		"\2\u0634\u0635\7(\2\2\u0635\u0636\7(\2\2\u0636\u0186\3\2\2\2\u0637\u0638"+
		"\7~\2\2\u0638\u0639\7~\2\2\u0639\u0188\3\2\2\2\u063a\u063b\7-\2\2\u063b"+
		"\u063c\7-\2\2\u063c\u018a\3\2\2\2\u063d\u063e\7/\2\2\u063e\u063f\7/\2"+
		"\2\u063f\u018c\3\2\2\2\u0640\u0641\7-\2\2\u0641\u018e\3\2\2\2\u0642\u0643"+
		"\7/\2\2\u0643\u0190\3\2\2\2\u0644\u0645\7,\2\2\u0645\u0192\3\2\2\2\u0646"+
		"\u0647\5\u011b\u008b\2\u0647\u0194\3\2\2\2\u0648\u0649\7(\2\2\u0649\u0196"+
		"\3\2\2\2\u064a\u064b\7~\2\2\u064b\u0198\3\2\2\2\u064c\u064d\7`\2\2\u064d"+
		"\u019a\3\2\2\2\u064e\u064f\7\'\2\2\u064f\u019c\3\2\2\2\u0650\u0651\7-"+
		"\2\2\u0651\u0652\7?\2\2\u0652\u019e\3\2\2\2\u0653\u0654\7/\2\2\u0654\u0655"+
		"\7?\2\2\u0655\u01a0\3\2\2\2\u0656\u0657\7,\2\2\u0657\u0658\7?\2\2\u0658"+
		"\u01a2\3\2\2\2\u0659\u065a\7\61\2\2\u065a\u065b\7?\2\2\u065b\u01a4\3\2"+
		"\2\2\u065c\u065d\7(\2\2\u065d\u065e\7?\2\2\u065e\u01a6\3\2\2\2\u065f\u0660"+
		"\7~\2\2\u0660\u0661\7?\2\2\u0661\u01a8\3\2\2\2\u0662\u0663\7`\2\2\u0663"+
		"\u0664\7?\2\2\u0664\u01aa\3\2\2\2\u0665\u0666\7\'\2\2\u0666\u0667\7?\2"+
		"\2\u0667\u01ac\3\2\2\2\u0668\u0669\7>\2\2\u0669\u066a\7>\2\2\u066a\u066b"+
		"\7?\2\2\u066b\u01ae\3\2\2\2\u066c\u066d\7@\2\2\u066d\u066e\7@\2\2\u066e"+
		"\u066f\7?\2\2\u066f\u01b0\3\2\2\2\u0670\u0671\7@\2\2\u0671\u0672\7@\2"+
		"\2\u0672\u0673\7@\2\2\u0673\u0674\7?\2\2\u0674\u01b2\3\2\2\2\u0675\u0676"+
		"\7A\2\2\u0676\u0677\7?\2\2\u0677\u01b4\3\2\2\2\u0678\u0679\5\u01bb\u00db"+
		"\2\u0679\u067d\6\u00d8\21\2\u067a\u067c\5\u01bf\u00dd\2\u067b\u067a\3"+
		"\2\2\2\u067c\u067f\3\2\2\2\u067d\u067b\3\2\2\2\u067d\u067e\3\2\2\2\u067e"+
		"\u01b6\3\2\2\2\u067f\u067d\3\2\2\2\u0680\u0684\5\u01bb\u00db\2\u0681\u0683"+
		"\5\u01bf\u00dd\2\u0682\u0681\3\2\2\2\u0683\u0686\3\2\2\2\u0684\u0682\3"+
		"\2\2\2\u0684\u0685\3\2\2\2\u0685\u01b8\3\2\2\2\u0686\u0684\3\2\2\2\u0687"+
		"\u068b\5\u01bd\u00dc\2\u0688\u068a\5\u01c1\u00de\2\u0689\u0688\3\2\2\2"+
		"\u068a\u068d\3\2\2\2\u068b\u0689\3\2\2\2\u068b\u068c\3\2\2\2\u068c\u01ba"+
		"\3\2\2\2\u068d\u068b\3\2\2\2\u068e\u0695\t\25\2\2\u068f\u0690\n\26\2\2"+
		"\u0690\u0695\6\u00db\22\2\u0691\u0692\t\27\2\2\u0692\u0693\t\30\2\2\u0693"+
		"\u0695\6\u00db\23\2\u0694\u068e\3\2\2\2\u0694\u068f\3\2\2\2\u0694\u0691"+
		"\3\2\2\2\u0695\u01bc\3\2\2\2\u0696\u0697\5\u01bb\u00db\2\u0697\u0698\6"+
		"\u00dc\24\2\u0698\u01be\3\2\2\2\u0699\u06a0\t\31\2\2\u069a\u069b\n\26"+
		"\2\2\u069b\u06a0\6\u00dd\25\2\u069c\u069d\t\27\2\2\u069d\u069e\t\30\2"+
		"\2\u069e\u06a0\6\u00dd\26\2\u069f\u0699\3\2\2\2\u069f\u069a\3\2\2\2\u069f"+
		"\u069c\3\2\2\2\u06a0\u01c0\3\2\2\2\u06a1\u06a2\5\u01bf\u00dd\2\u06a2\u06a3"+
		"\6\u00de\27\2\u06a3\u01c2\3\2\2\2\u06a4\u06a6\n\32\2\2\u06a5\u06a4\3\2"+
		"\2\2\u06a6\u06a9\3\2\2\2\u06a7\u06a5\3\2\2\2\u06a7\u06a8\3\2\2\2\u06a8"+
		"\u01c4\3\2\2\2\u06a9\u06a7\3\2\2\2\u06aa\u06ab\7B\2\2\u06ab\u01c6\3\2"+
		"\2\2\u06ac\u06ad\7\60\2\2\u06ad\u06ae\7\60\2\2\u06ae\u06af\7\60\2\2\u06af"+
		"\u01c8\3\2\2\2\u06b0\u06b2\t\33\2\2\u06b1\u06b0\3\2\2\2\u06b2\u06b3\3"+
		"\2\2\2\u06b3\u06b1\3\2\2\2\u06b3\u06b4\3\2\2\2\u06b4\u06bb\3\2\2\2\u06b5"+
		"\u06b7\5\u0113\u0087\2\u06b6\u06b5\3\2\2\2\u06b7\u06b8\3\2\2\2\u06b8\u06b6"+
		"\3\2\2\2\u06b8\u06b9\3\2\2\2\u06b9\u06bb\3\2\2\2\u06ba\u06b1\3\2\2\2\u06ba"+
		"\u06b6\3\2\2\2\u06bb\u06bc\3\2\2\2\u06bc\u06bd\b\u00e2\35\2\u06bd\u01ca"+
		"\3\2\2\2\u06be\u06bf\5\u0115\u0088\2\u06bf\u06c0\b\u00e3\36\2\u06c0\u01cc"+
		"\3\2\2\2\u06c1\u06c2\7\61\2\2\u06c2\u06c3\7,\2\2\u06c3\u06c7\3\2\2\2\u06c4"+
		"\u06c6\13\2\2\2\u06c5\u06c4\3\2\2\2\u06c6\u06c9\3\2\2\2\u06c7\u06c8\3"+
		"\2\2\2\u06c7\u06c5\3\2\2\2\u06c8\u06ca\3\2\2\2\u06c9\u06c7\3\2\2\2\u06ca"+
		"\u06cb\7,\2\2\u06cb\u06cc\7\61\2\2\u06cc\u06cd\3\2\2\2\u06cd\u06ce\b\u00e4"+
		"\37\2\u06ce\u06cf\3\2\2\2\u06cf\u06d0\b\u00e4 \2\u06d0\u01ce\3\2\2\2\u06d1"+
		"\u06d2\7\61\2\2\u06d2\u06d3\7\61\2\2\u06d3\u06d7\3\2\2\2\u06d4\u06d6\n"+
		"\32\2\2\u06d5\u06d4\3\2\2\2\u06d6\u06d9\3\2\2\2\u06d7\u06d5\3\2\2\2\u06d7"+
		"\u06d8\3\2\2\2\u06d8\u06da\3\2\2\2\u06d9\u06d7\3\2\2\2\u06da\u06db\b\u00e5"+
		"!\2\u06db\u06dc\3\2\2\2\u06dc\u06dd\b\u00e5 \2\u06dd\u01d0\3\2\2\2\u06de"+
		"\u06df\7%\2\2\u06df\u06e0\7#\2\2\u06e0\u06e1\3\2\2\2\u06e1\u06e2\b\u00e6"+
		"\"\2\u06e2\u06eb\5\u01c3\u00df\2\u06e3\u06e4\5\u0115\u0088\2\u06e4\u06e5"+
		"\7%\2\2\u06e5\u06e6\7#\2\2\u06e6\u06e7\3\2\2\2\u06e7\u06e8\5\u01c3\u00df"+
		"\2\u06e8\u06ea\3\2\2\2\u06e9\u06e3\3\2\2\2\u06ea\u06ed\3\2\2\2\u06eb\u06e9"+
		"\3\2\2\2\u06eb\u06ec\3\2\2\2\u06ec\u06ee\3\2\2\2\u06ed\u06eb\3\2\2\2\u06ee"+
		"\u06ef\b\u00e6\35\2\u06ef\u01d2\3\2\2\2\u06f0\u06f1\13\2\2\2\u06f1\u06f2"+
		"\b\u00e7#\2\u06f2\u01d4\3\2\2\2T\2\3\4\5\6\7\b\u01d9\u01e2\u01ec\u01f4"+
		"\u01fd\u0206\u020a\u0210\u021c\u022a\u0238\u025d\u0294\u0298\u029f\u02a6"+
		"\u02ad\u02bd\u02e6\u0466\u046b\u0472\u0476\u0478\u047e\u0482\u0486\u048a"+
		"\u0491\u0496\u0498\u049e\u04a2\u04a6\u04ac\u04b1\u04bd\u04c1\u04c7\u04cb"+
		"\u04d3\u04d7\u04dd\u04e7\u04eb\u04f1\u04f5\u04fa\u04fd\u0502\u0505\u050a"+
		"\u050f\u0517\u0522\u0526\u052b\u0530\u0542\u054b\u0559\u056b\u056f\u067d"+
		"\u0684\u068b\u0694\u069f\u06a7\u06b3\u06b8\u06ba\u06c7\u06d7\u06eb$\7"+
		"\3\2\7\7\2\t\4\2\7\4\2\7\5\2\7\6\2\6\2\2\5\2\2\t\5\2\t\6\2\3\23\2\tZ\2"+
		"\7\2\2\t\u0085\2\7\b\2\3\26\3\3[\4\3[\5\3[\6\3u\7\3\u009d\b\3\u00ac\t"+
		"\3\u00ad\n\3\u00ae\13\3\u00af\f\3\u00b0\r\3\u00b1\16\b\2\2\3\u00e3\17"+
		"\3\u00e4\20\t\u0089\2\3\u00e5\21\3\u00e6\22\3\u00e7\23";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
	}
}