// Generated from GroovyLexer.g4 by ANTLR 4.13.2.20
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
			"ASYNC", "AWAIT", "DEFER", "BuiltInPrimitiveType", "ABSTRACT", "ASSERT", 
			"BOOLEAN", "BREAK", "BYTE", "CASE", "CATCH", "CHAR", "CLASS", "CONST", 
			"CONTINUE", "DEFAULT", "DO", "DOUBLE", "ELSE", "ENUM", "EXTENDS", "FINAL", 
			"FINALLY", "FLOAT", "FOR", "IF", "GOTO", "IMPLEMENTS", "IMPORT", "INSTANCEOF", 
			"INTERFACE", "INT", "LONG", "MODULE", "NATIVE", "NEW", "NON_SEALED", 
			"PACKAGE", "PERMITS", "PRIVATE", "PROTECTED", "PUBLIC", "RECORD", "RETURN", 
			"SEALED", "SHORT", "STATIC", "STRICTFP", "SUPER", "SWITCH", "SYNCHRONIZED", 
			"THIS", "THROW", "THROWS", "TRANSIENT", "TRY", "VAL", "VAR", "VOID", 
			"VOLATILE", "WHILE", "YIELD", "IntegerLiteral", "DecimalIntegerLiteral", 
			"HexIntegerLiteral", "OctalIntegerLiteral", "BinaryIntegerLiteral", "IntegerTypeSuffix", 
			"DecimalNumeral", "Digits", "Underscores", "Underscore", "HexNumeral", 
			"HexDigits", "HexDigit", "OctalNumeral", "OctalDigits", "BinaryNumeral", 
			"BinaryDigits", "FloatingPointLiteral", "DecimalFloatingPointLiteral", 
			"ExponentPart", "FloatTypeSuffix", "HexadecimalFloatingPointLiteral", 
			"HexSignificand", "BinaryExponent", "BooleanLiteral", "EscapeSequence", 
			"OctalEscape", "UnicodeEscape", "DollarEscape", "LineEscape", "LineTerminator", 
			"SlashEscape", "Backslash", "Slash", "Dollar", "GStringQuotationMark", 
			"SqStringQuotationMark", "TdqStringQuotationMark", "TsqStringQuotationMark", 
			"DollarSlashyGStringQuotationMarkBegin", "DollarSlashyGStringQuotationMarkEnd", 
			"DollarSlashEscape", "DollarDollarEscape", "DollarSlashDollarEscape", 
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
			"IdentifierInGString", "JavaLetter", "JavaLetterInGString", "JavaLetterOrDigit", 
			"JavaLetterOrDigitInGString", "ShCommand", "AT", "ELLIPSIS", "WS", "NL", 
			"ML_COMMENT", "SL_COMMENT", "SH_COMMENT", "UNEXPECTED_CHAR"
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


	    private boolean errorIgnored;
	    private long tokenIndex;
	    /** Previous default-channel token. {@link Token#INVALID_TYPE} at the start of an input. */
	    private int  lastTokenType = Token.INVALID_TYPE;
	    /**
	     * Last default-channel token whose type is not {@code NL}.
	     * Stays {@link Token#INVALID_TYPE} until one is seen. Not {@link Token#EOF}:
	     * a negative index is rejected by {@link java.util.BitSet#get(int)}.
	     */
	    private int  lastRealTokenType = Token.INVALID_TYPE;
	    /**
	     * True after a member selector ({@code . ?. *. ??. .& :: @}) until the
	     * name that follows it is emitted. Newlines and comments (both {@code NL})
	     * do not clear it. Type arguments ({@code foo.<T>name}, including
	     * qualified names inside them) stay inside this window.
	     */
	    private boolean pendingMemberName;
	    /** True when {@link #lastRealTokenType} is that name. */
	    private boolean lastRealIsMemberName;
	    /**
	     * True when a value was emitted immediately before {@link #lastRealTokenType}
	     * (newlines ignored). Distinguishes infix {@code in} from the name {@code in}.
	     */
	    private boolean lastRealPrecededByValue;
	    /** Nesting of {@code <...>} while {@link #pendingMemberName} is set. */
	    private int memberTypeArgDepth;
	    /**
	     * Nesting of {@code ()}, {@code []}, and {@code {}} inside that
	     * {@code <...>}. Comparisons there must not move {@link #memberTypeArgDepth}.
	     */
	    private int memberTypeArgGroupDepth;

	    /**
	     * When {@code false}, the {@code val} keyword is treated as a regular
	     * identifier (lexed as IDENTIFIER, not VAL). This can be used as a porting
	     * aid for migrating to Groovy 6 if affected by the known breaking edge cases.
	     * Controlled by system property {@code groovy.val.enabled} (default: {@code true}).
	     */
	    private static final boolean VAL_ENABLED =
	            Boolean.parseBoolean(System.getProperty("groovy.val.enabled", "true"));
	    private boolean isValEnabled() { return VAL_ENABLED; }

	    /**
	     * Record the index and token type of the current token while emitting tokens.
	     */
	    @Override
	    public void emit(Token token) {
	        this.tokenIndex++;

	        int tokenType = token.getType();
	        // EOF is negative. Recording it makes the next slashy decision call
	        // BitSet.get(-1). setInputStream rewinds only through reset().
	        if (tokenType >= 0 && Token.DEFAULT_CHANNEL == token.getChannel()) {
	            if (NL != tokenType) {
	                this.noteRealToken(tokenType);
	            }
	            this.lastTokenType = tokenType;
	        }

	        if (RollBackOne == tokenType) {
	            this.rollbackOneChar();
	        }

	        super.emit(token);
	    }

	    @Override
	    public void reset() {
	        super.reset();
	        this.lastTokenType = Token.INVALID_TYPE;
	        this.lastRealTokenType = Token.INVALID_TYPE;
	        this.pendingMemberName = false;
	        this.lastRealIsMemberName = false;
	        this.lastRealPrecededByValue = false;
	        this.memberTypeArgDepth = 0;
	        this.memberTypeArgGroupDepth = 0;
	    }

	    /**
	     * Token types after which {@code /.../} is division, not a slashy string
	     * ({@code a++ / b}, {@code list[i] / n}, {@code obj.class / 2}).
	     * Keywords that introduce an expression stay out of this set so
	     * {@code return /a/} and {@code x in /a/} remain strings.
	     * {@link java.util.BitSet} gives O(1) tests on this hot decision.
	     */
	    private static final BitSet REGEX_CHECK_SET = new BitSet();
	    /**
	     * Keywords followed by an expression or a statement. A slashy after them
	     * is a string ({@code return /a/}, {@code else /a/}) unless the keyword
	     * is a member name ({@code obj.return / 2}).
	     */
	    private static final BitSet STATEMENT_INTRODUCER_SET = new BitSet();
	    /** {@code . ?. *. ??. .& :: @} — the name that follows is a member. */
	    private static final BitSet MEMBER_SELECTOR_SET = new BitSet();
	    static {
	        int[] regexCheckTypes = {
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
	            Identifier, CapitalizedIdentifier,
	            // Value-ending keywords. Not statement introducers, and not {@code in}
	            // (infix {@code x in /a/} must stay a string). See isSlashyForbidden.
	            AS, DEF, TRAIT, THREADSAFE, ASYNC,
	            ABSTRACT, BREAK, CATCH, CLASS, CONST, CONTINUE, DEFAULT,
	            BuiltInPrimitiveType,
	            ENUM, EXTENDS, FINAL, FINALLY, FOR, GOTO, IF, IMPLEMENTS, IMPORT,
	            INSTANCEOF, INTERFACE, MODULE, NATIVE, NEW, NON_SEALED, PACKAGE,
	            PERMITS, PRIVATE, PROTECTED, PUBLIC, RECORD, SEALED, STATIC,
	            STRICTFP, SUPER, SWITCH, SYNCHRONIZED, THROWS, TRANSIENT, TRY,
	            VAL, VAR, VOID, VOLATILE, WHILE
	        };
	        int[] statementIntroducers = {
	            ASSERT, AWAIT, CASE, DEFER, DO, ELSE, RETURN, THROW, YIELD
	        };
	        int[] memberSelectors = {
	            DOT, SAFE_DOT, SPREAD_DOT, SAFE_CHAIN_DOT,
	            METHOD_POINTER, METHOD_REFERENCE, AT
	        };
	        for (int t : regexCheckTypes) {
	            REGEX_CHECK_SET.set(t);
	        }
	        for (int t : statementIntroducers) {
	            STATEMENT_INTRODUCER_SET.set(t);
	        }
	        for (int t : memberSelectors) {
	            MEMBER_SELECTOR_SET.set(t);
	        }
	    }

	    /**
	     * Remember whether {@code tokenType} is a member name, and whether a
	     * value preceded it. {@code NL} is not passed here.
	     */
	    private void noteRealToken(final int tokenType) {
	        // A member name is a value even when it is not an introducer (obj.in),
	        // and so is a preceding in (in in /a/). yield, await, and defer are
	        // not values: yield in / 2 stays division, so for (yield in /a/) is
	        // not a slashy.
	        this.lastRealPrecededByValue = isDivisionOperand(this.lastRealTokenType)
	                || this.lastRealIsMemberName
	                || IN == this.lastRealTokenType;
	        if (this.pendingMemberName && this.memberTypeArgDepth > 0) {
	            // foo.<a.b>name and foo.<Map<K, V>>name: dots and names inside
	            // the arguments are not a new member selection.
	            this.noteTypeArgumentToken(tokenType);
	            this.lastRealIsMemberName = false;
	        } else if (isMemberSelector(tokenType)) {
	            this.lastRealIsMemberName = false;
	            this.pendingMemberName = true;
	            this.memberTypeArgDepth = 0;
	            this.memberTypeArgGroupDepth = 0;
	        } else if (this.pendingMemberName && LT == tokenType) {
	            this.lastRealIsMemberName = false;
	            this.memberTypeArgDepth = 1;
	            this.memberTypeArgGroupDepth = 0;
	        } else {
	            this.lastRealIsMemberName = this.pendingMemberName;
	            this.pendingMemberName = false;
	            this.memberTypeArgDepth = 0;
	            this.memberTypeArgGroupDepth = 0;
	        }
	        this.lastRealTokenType = tokenType;
	    }

	    /**
	     * Track {@code <}/{@code >} that belong to the type argument. {@code >}
	     * inside {@code ()}, {@code []}, or {@code {}} is a comparison or a shift
	     * ({@code foo.<@Ann(x > 1) T>name}).
	     */
	    private void noteTypeArgumentToken(final int tokenType) {
	        if (this.memberTypeArgGroupDepth > 0) {
	            if (isTypeArgGroupOpen(tokenType)) {
	                this.memberTypeArgGroupDepth++;
	            } else if (isTypeArgGroupClose(tokenType)) {
	                this.memberTypeArgGroupDepth--;
	            }
	            return;
	        }
	        if (isTypeArgGroupOpen(tokenType)) {
	            this.memberTypeArgGroupDepth = 1;
	            return;
	        }
	        if (LT == tokenType) {
	            this.memberTypeArgDepth++;
	        } else if (GT == tokenType) {
	            this.memberTypeArgDepth--;
	        }
	    }

	    private static boolean isTypeArgGroupOpen(final int tokenType) {
	        return LPAREN == tokenType || LBRACK == tokenType || LBRACE == tokenType;
	    }

	    private static boolean isTypeArgGroupClose(final int tokenType) {
	        return RPAREN == tokenType || RBRACK == tokenType || RBRACE == tokenType;
	    }

	    private boolean isRegexAllowed() {
	        return !isSlashyForbidden(this.lastTokenType);
	    }

	    /**
	     * A {@code /} that reaches EOF is an unclosed slashy string only when a
	     * closed slashy would have been allowed there, looking through {@code NL}
	     * to the last real token. {@code 9 \n / \n 3} and {@code obj.class \n / \n 2}
	     * stay division; {@code s = /hello} and {@code return /hello} do not.
	     * {@link Token#INVALID_TYPE} (no real token yet, including a leading newline)
	     * is not an operand. {@code NL} itself still allows a closed slashy to
	     * start the next line ({@code obj.return \n /a/}).
	     */
	    private boolean isUnclosedSlashy() {
	        int decision = this.lastTokenType == NL ? this.lastRealTokenType : this.lastTokenType;
	        return !isSlashyForbidden(decision);
	    }

	    /**
	     * Whether {@code /} after {@code tokenType} is division.
	     * {@code return /a/} is a string; {@code obj.return / 2} and {@code obj.class / 2}
	     * are division; {@code x in /a/} is a string; {@code in / 2} is division.
	     * Passing {@code NL} (not a real token) returns false so a closed slashy
	     * may start a line.
	     */
	    private boolean isSlashyForbidden(final int tokenType) {
	        if (isDivisionOperand(tokenType)) {
	            return true;
	        }
	        // The decision token is not the one noteRealToken just described
	        // (isRegexAllowed sees NL). Do not apply the member-name or {@code in} rule.
	        if (tokenType != this.lastRealTokenType) {
	            return false;
	        }
	        if (IN == tokenType) {
	            return !this.lastRealPrecededByValue;
	        }
	        return this.lastRealIsMemberName && isStatementIntroducer(tokenType);
	    }

	    /** {@code REGEX_CHECK_SET} rejects a negative type, and {@link Token#EOF} is not an operand. */
	    private static boolean isDivisionOperand(final int tokenType) {
	        return tokenType >= 0 && REGEX_CHECK_SET.get(tokenType);
	    }

	    private static boolean isStatementIntroducer(final int tokenType) {
	        return STATEMENT_INTRODUCER_SET.get(tokenType);
	    }

	    private static boolean isMemberSelector(final int tokenType) {
	        return MEMBER_SELECTOR_SET.get(tokenType);
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

	    /**
	     * Code point just consumed. After a UTF-16 surrogate pair, {@code LA(-1)}
	     * is the low surrogate and is never uppercase — {@code Character.isUpperCase}
	     * must see the pair (GROOVY-12398).
	     */
	    private int lastConsumedCodePoint() {
	        int c1 = _input.LA(-1);
	        int c2 = _input.LA(-2);
	        if (c1 >= Character.MIN_LOW_SURROGATE && c1 <= Character.MAX_LOW_SURROGATE
	                && c2 >= Character.MIN_HIGH_SURROGATE && c2 <= Character.MAX_HIGH_SURROGATE) {
	            return Character.toCodePoint((char) c2, (char) c1);
	        }
	        return c1;
	    }

	    private boolean isSupplementaryCapitalizedIdentifierStart() {
	        int cp = lastConsumedCodePoint();
	        return Character.isJavaIdentifierStart(cp) && Character.isUpperCase(cp);
	    }

	    private boolean isSupplementaryUncapitalizedIdentifierStart() {
	        int cp = lastConsumedCodePoint();
	        return Character.isJavaIdentifierStart(cp) && !Character.isUpperCase(cp);
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
	public void action(RuleContext _localctx, int _ruleIndex, int actionIndex) {
		switch (actionIndex) {
		case 0: {
			 requireUnclosedString(errorIgnored); 
			}
			break;
		case 1: {
			 this.enterParen();  
			}
			break;
		case 2: {

			            // a trick to handle GStrings followed by EOF properly
			            int readChar = _input.LA(-1);
			            if (EOF == _input.LA(1) && ('"' == readChar || '/' == readChar)) {
			                setType(GStringEnd);
			            } else {
			                setChannel(HIDDEN);
			            }
			          
			}
			break;
		case 3: {
			 require(errorIgnored, "Number ending with underscores is invalid", -1, false); 
			}
			break;
		case 4: {
			 requireInvalidOctal(errorIgnored); 
			}
			break;
		case 5: {
			 require(errorIgnored, "Number ending with underscores is invalid", -1, false); 
			}
			break;
		case 6: {
			 this.enterParen();     
			}
			break;
		case 7: {
			 this.enterParen();     
			}
			break;
		case 8: {
			 this.exitParen();      
			}
			break;
		case 9: {
			 this.enterParen();     
			}
			break;
		case 10: {
			 this.exitParen();      
			}
			break;
		case 11: {
			 this.enterParen();     
			}
			break;
		case 12: {
			 this.exitParen();      
			}
			break;
		case 13: {
			 ignoreTokenInsideParens(); 
			}
			break;
		case 14: {
			 requireUnclosedComment(errorIgnored); 
			}
			break;
		case 15: {
			 addComment(0); ignoreMultiLineCommentConditionally(); 
			}
			break;
		case 16: {
			 addComment(1); ignoreTokenInsideParens(); 
			}
			break;
		case 17: {
			 require(errorIgnored || 0 == this.tokenIndex, "Shebang comment should appear at the first line", -2, false); 
			}
			break;
		case 18: {
			int c = getText().codePointAt(0);
			if (Character.isIdentifierIgnorable(c) || Character.getType(c) == Character.CONTROL && c != '\t' && c != '\r' && c != '\n') {
				require(errorIgnored, String.format("Unexpected character: 0x%02X (%s)", c, Character.getName(c)), -1, false);
			} else {
				requireUnexpectedCharacter(errorIgnored);
			}
			}
			break;
		}
	}
	@Override
	public boolean sempred(RuleContext _localctx, int _ruleIndex, int predIndex) {
		switch (predIndex) {
		case 0:
			return  this.isRegexAllowed() && _input.LA(1) != '*' ;
		case 1:
			return  this.isUnclosedSlashy() ;
		case 2:
			return  this.isRegexAllowed() && _input.LA(1) != '*' ;
		case 3:
			return  isFollowedByJavaLetterInGString(_input) ;
		case 4:
			return  isFollowedByJavaLetterInGString(_input) ;
		case 5:
			return  isFollowedByJavaLetterInGString(_input) ;
		case 6:
			return  isFollowedByJavaLetterInGString(_input) ;
		case 7:
			return  _input.LA(1) != '"' || _input.LA(2) != '"' || _input.LA(3) == '"' && (_input.LA(4) != '"' || _input.LA(5) != '"') ;
		case 8:
			return  _input.LA(1) != '\'' || _input.LA(2) != '\'' || _input.LA(3) == '\'' && (_input.LA(4) != '\'' || _input.LA(5) != '\'') ;
		case 9:
			return  !isFollowedByJavaLetterInGString(_input) ;
		case 10:
			return  _input.LA(-4) != '$' ;
		case 11:
			return  _input.LA(1) != '$' ;
		case 12:
			return  _input.LA(1) != '$' ;
		case 13:
			return  !isFollowedByJavaLetterInGString(_input) ;
		case 14:
			return isValEnabled();
		case 15:
			return  isFollowedBy(_input, ' ', '\t', '\r', '\n') ;
		case 16:
			return  isFollowedBy(_input, ' ', '\t', '\r', '\n', '[', '(', '{') ;
		case 17:
			return  isJavaIdentifierStartAndNotIdentifierIgnorable(_input.LA(-1)) && Character.isUpperCase(_input.LA(-1)) ;
		case 18:
			return  isSupplementaryCapitalizedIdentifierStart() ;
		case 19:
			return  isJavaIdentifierStartAndNotIdentifierIgnorable(_input.LA(-1)) && !Character.isUpperCase(_input.LA(-1)) ;
		case 20:
			return  isSupplementaryUncapitalizedIdentifierStart() ;
		case 21:
			return  isJavaIdentifierStartAndNotIdentifierIgnorable(_input.LA(-1)) ;
		case 22:
			return  Character.isJavaIdentifierStart(Character.toCodePoint((char) _input.LA(-2), (char) _input.LA(-1))) ;
		case 23:
			return  _input.LA(-1) != '$' ;
		case 24:
			return  isJavaIdentifierPartAndNotIdentifierIgnorable(_input.LA(-1)) ;
		case 25:
			return  Character.isJavaIdentifierPart(Character.toCodePoint((char) _input.LA(-2), (char) _input.LA(-1))) ;
		case 26:
			return  _input.LA(-1) != '$' ;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uc91d\ucaba\u058d\uafba\u4f53\u0607\uea8b\uc241\2\u0091\u0713\b\1\b"+
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
		"\t\u00dd\4\u00de\t\u00de\3\2\3\2\7\2\u01c6\n\2\f\2\16\2\u01c9\13\2\3\2"+
		"\3\2\3\2\3\2\7\2\u01cf\n\2\f\2\16\2\u01d2\13\2\3\2\3\2\3\2\3\2\3\2\6\2"+
		"\u01d9\n\2\r\2\16\2\u01da\3\2\3\2\3\2\3\2\7\2\u01e1\n\2\f\2\16\2\u01e4"+
		"\13\2\3\2\3\2\5\2\u01e8\n\2\3\2\3\2\7\2\u01ec\n\2\f\2\16\2\u01ef\13\2"+
		"\3\2\3\2\3\2\3\2\7\2\u01f5\n\2\f\2\16\2\u01f8\13\2\3\2\3\2\3\2\3\2\6\2"+
		"\u01fe\n\2\r\2\16\2\u01ff\3\2\3\2\5\2\u0204\n\2\3\3\3\3\7\3\u0208\n\3"+
		"\f\3\16\3\u020b\13\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\7\4\u0214\n\4\f\4\16"+
		"\4\u0217\13\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\7\5\u0222\n\5\f\5\16"+
		"\5\u0225\13\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\7\6\u0230\n\6\f\6\16"+
		"\6\u0233\13\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b"+
		"\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f"+
		"\3\f\3\f\3\f\3\r\5\r\u0257\n\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26"+
		"\3\27\3\27\5\27\u028e\n\27\3\30\3\30\5\30\u0292\n\30\3\31\3\31\3\31\3"+
		"\31\3\31\5\31\u0299\n\31\3\32\3\32\3\32\3\32\3\32\5\32\u02a0\n\32\3\33"+
		"\3\33\3\33\3\33\3\33\5\33\u02a7\n\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u02b7\n\34\3\35\3\35\3\35\3\36"+
		"\3\36\3\36\3\36\3\37\3\37\3\37\3 \3 \3 \3 \3 \3 \3!\3!\3!\3!\3!\3!\3!"+
		"\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3#\3$\3$\3$\3$\3$"+
		"\3$\3%\3%\3%\3%\3%\3%\3%\3%\5%\u02ee\n%\3&\3&\3&\3&\3&\3&\3&\3&\3&\3\'"+
		"\3\'\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\3(\3(\3(\3)\3)\3)\3)\3)\3)\3*"+
		"\3*\3*\3*\3*\3+\3+\3+\3+\3+\3,\3,\3,\3,\3,\3,\3-\3-\3-\3-\3-\3.\3.\3."+
		"\3.\3.\3.\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3"+
		"\60\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\62\3\62\3\62\3\63\3\63\3"+
		"\63\3\63\3\63\3\63\3\63\3\64\3\64\3\64\3\64\3\64\3\65\3\65\3\65\3\65\3"+
		"\65\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\67\3\67\3\67\3\67\3\67\3"+
		"\67\38\38\38\38\38\38\38\38\39\39\39\39\39\39\3:\3:\3:\3:\3;\3;\3;\3<"+
		"\3<\3<\3<\3<\3=\3=\3=\3=\3=\3=\3=\3=\3=\3=\3=\3>\3>\3>\3>\3>\3>\3>\3?"+
		"\3?\3?\3?\3?\3?\3?\3?\3?\3?\3?\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3A\3A\3A"+
		"\3A\3B\3B\3B\3B\3B\3C\3C\3C\3C\3C\3C\3C\3D\3D\3D\3D\3D\3D\3D\3E\3E\3E"+
		"\3E\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\3G\3G\3G\3G\3G\3G\3G\3G\3H\3H\3H"+
		"\3H\3H\3H\3H\3H\3I\3I\3I\3I\3I\3I\3I\3I\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J"+
		"\3K\3K\3K\3K\3K\3K\3K\3L\3L\3L\3L\3L\3L\3L\3M\3M\3M\3M\3M\3M\3M\3N\3N"+
		"\3N\3N\3N\3N\3N\3O\3O\3O\3O\3O\3O\3P\3P\3P\3P\3P\3P\3P\3Q\3Q\3Q\3Q\3Q"+
		"\3Q\3Q\3Q\3Q\3R\3R\3R\3R\3R\3R\3S\3S\3S\3S\3S\3S\3S\3T\3T\3T\3T\3T\3T"+
		"\3T\3T\3T\3T\3T\3T\3T\3U\3U\3U\3U\3U\3V\3V\3V\3V\3V\3V\3W\3W\3W\3W\3W"+
		"\3W\3W\3X\3X\3X\3X\3X\3X\3X\3X\3X\3X\3Y\3Y\3Y\3Y\3Z\3Z\3Z\3Z\3Z\3Z\3["+
		"\3[\3[\3[\3\\\3\\\3\\\3\\\3\\\3]\3]\3]\3]\3]\3]\3]\3]\3]\3^\3^\3^\3^\3"+
		"^\3^\3_\3_\3_\3_\3_\3_\3`\3`\3`\3`\5`\u047f\n`\3`\3`\3`\5`\u0484\n`\3"+
		"`\3`\6`\u0488\n`\r`\16`\u0489\3`\3`\5`\u048e\n`\5`\u0490\n`\3a\3a\5a\u0494"+
		"\na\3b\3b\5b\u0498\nb\3c\3c\5c\u049c\nc\3d\3d\5d\u04a0\nd\3e\3e\3f\3f"+
		"\3f\5f\u04a7\nf\3f\3f\3f\5f\u04ac\nf\5f\u04ae\nf\3g\3g\7g\u04b2\ng\fg"+
		"\16g\u04b5\13g\3g\5g\u04b8\ng\3h\6h\u04bb\nh\rh\16h\u04bc\3i\3i\3j\3j"+
		"\3j\3j\3k\3k\7k\u04c7\nk\fk\16k\u04ca\13k\3k\5k\u04cd\nk\3l\3l\3m\3m\7"+
		"m\u04d3\nm\fm\16m\u04d6\13m\3m\3m\3n\3n\7n\u04dc\nn\fn\16n\u04df\13n\3"+
		"n\5n\u04e2\nn\3o\3o\3o\3o\3p\3p\7p\u04ea\np\fp\16p\u04ed\13p\3p\5p\u04f0"+
		"\np\3q\3q\5q\u04f4\nq\3q\3q\3q\5q\u04f9\nq\3r\5r\u04fc\nr\3r\3r\3r\5r"+
		"\u0501\nr\3r\5r\u0504\nr\3r\3r\3r\5r\u0509\nr\3r\3r\3r\5r\u050e\nr\3s"+
		"\3s\5s\u0512\ns\3s\3s\3t\3t\3u\3u\3u\5u\u051b\nu\3v\3v\5v\u051f\nv\3v"+
		"\3v\3v\5v\u0524\nv\3v\3v\5v\u0528\nv\3w\3w\5w\u052c\nw\3w\3w\3x\3x\3x"+
		"\3x\3x\3x\3x\3x\3x\5x\u0539\nx\3y\3y\3y\3y\3y\3y\3y\5y\u0542\ny\3z\3z"+
		"\3z\3z\3z\3z\3z\3z\3z\3z\3z\3z\5z\u0550\nz\3{\3{\3{\3{\3{\3{\3{\3|\3|"+
		"\3|\3}\3}\3}\3~\5~\u0560\n~\3~\3~\5~\u0564\n~\3\177\3\177\3\177\3\u0080"+
		"\3\u0080\3\u0081\3\u0081\3\u0082\3\u0082\3\u0083\3\u0083\3\u0084\3\u0084"+
		"\3\u0085\3\u0085\3\u0085\3\u0085\3\u0086\3\u0086\3\u0086\3\u0086\3\u0087"+
		"\3\u0087\3\u0087\3\u0088\3\u0088\3\u0088\3\u0089\3\u0089\3\u0089\3\u008a"+
		"\3\u008a\3\u008a\3\u008b\3\u008b\3\u008b\3\u008b\3\u008c\3\u008c\3\u008c"+
		"\3\u008c\3\u008c\3\u008d\3\u008d\3\u008d\3\u008e\3\u008e\3\u008e\3\u008e"+
		"\3\u008f\3\u008f\3\u008f\3\u008f\3\u0090\3\u0090\3\u0090\3\u0090\3\u0090"+
		"\3\u0091\3\u0091\3\u0091\3\u0092\3\u0092\3\u0092\3\u0093\3\u0093\3\u0093"+
		"\3\u0093\3\u0093\3\u0093\3\u0093\3\u0094\3\u0094\3\u0094\3\u0094\3\u0095"+
		"\3\u0095\3\u0095\3\u0096\3\u0096\3\u0096\3\u0097\3\u0097\3\u0097\3\u0098"+
		"\3\u0098\3\u0098\3\u0099\3\u0099\3\u0099\3\u0099\3\u009a\3\u009a\3\u009a"+
		"\3\u009b\3\u009b\3\u009b\3\u009b\3\u009c\3\u009c\3\u009c\3\u009c\3\u009d"+
		"\3\u009d\3\u009d\3\u009d\3\u009e\3\u009e\3\u009e\3\u009e\3\u009f\3\u009f"+
		"\3\u009f\3\u009f\3\u00a0\3\u00a0\3\u00a0\3\u00a1\3\u00a1\3\u00a1\3\u00a1"+
		"\3\u00a1\3\u00a1\3\u00a1\3\u00a1\3\u00a1\3\u00a1\3\u00a1\3\u00a1\3\u00a1"+
		"\3\u00a1\3\u00a2\3\u00a2\3\u00a2\3\u00a2\3\u00a2\3\u00a2\3\u00a3\3\u00a3"+
		"\3\u00a3\3\u00a3\3\u00a3\3\u00a4\3\u00a4\3\u00a4\3\u00a4\3\u00a4\3\u00a5"+
		"\3\u00a5\3\u00a5\3\u00a5\3\u00a5\3\u00a6\3\u00a6\3\u00a6\3\u00a6\3\u00a6"+
		"\3\u00a7\3\u00a7\3\u00a7\3\u00a7\3\u00a7\3\u00a8\3\u00a8\3\u00a8\3\u00a8"+
		"\3\u00a8\3\u00a9\3\u00a9\3\u00aa\3\u00aa\3\u00ab\3\u00ab\3\u00ac\3\u00ac"+
		"\3\u00ad\3\u00ad\3\u00ae\3\u00ae\3\u00af\3\u00af\3\u00b0\3\u00b0\3\u00b1"+
		"\3\u00b1\3\u00b2\3\u00b2\3\u00b3\3\u00b3\3\u00b3\3\u00b4\3\u00b4\3\u00b4"+
		"\3\u00b5\3\u00b5\3\u00b5\3\u00b6\3\u00b6\3\u00b6\3\u00b7\3\u00b7\3\u00b7"+
		"\3\u00b8\3\u00b8\3\u00b8\3\u00b9\3\u00b9\3\u00b9\3\u00ba\3\u00ba\3\u00ba"+
		"\3\u00bb\3\u00bb\3\u00bc\3\u00bc\3\u00bd\3\u00bd\3\u00be\3\u00be\3\u00bf"+
		"\3\u00bf\3\u00c0\3\u00c0\3\u00c1\3\u00c1\3\u00c2\3\u00c2\3\u00c3\3\u00c3"+
		"\3\u00c3\3\u00c4\3\u00c4\3\u00c4\3\u00c5\3\u00c5\3\u00c5\3\u00c6\3\u00c6"+
		"\3\u00c6\3\u00c7\3\u00c7\3\u00c7\3\u00c8\3\u00c8\3\u00c8\3\u00c9\3\u00c9"+
		"\3\u00c9\3\u00ca\3\u00ca\3\u00ca\3\u00cb\3\u00cb\3\u00cb\3\u00cb\3\u00cc"+
		"\3\u00cc\3\u00cc\3\u00cc\3\u00cd\3\u00cd\3\u00cd\3\u00cd\3\u00cd\3\u00ce"+
		"\3\u00ce\3\u00ce\3\u00cf\3\u00cf\7\u00cf\u0673\n\u00cf\f\u00cf\16\u00cf"+
		"\u0676\13\u00cf\3\u00cf\3\u00cf\3\u00cf\7\u00cf\u067b\n\u00cf\f\u00cf"+
		"\16\u00cf\u067e\13\u00cf\3\u00cf\3\u00cf\3\u00cf\3\u00cf\7\u00cf\u0684"+
		"\n\u00cf\f\u00cf\16\u00cf\u0687\13\u00cf\5\u00cf\u0689\n\u00cf\3\u00d0"+
		"\3\u00d0\7\u00d0\u068d\n\u00d0\f\u00d0\16\u00d0\u0690\13\u00d0\3\u00d0"+
		"\3\u00d0\3\u00d0\7\u00d0\u0695\n\u00d0\f\u00d0\16\u00d0\u0698\13\u00d0"+
		"\3\u00d0\3\u00d0\3\u00d0\3\u00d0\7\u00d0\u069e\n\u00d0\f\u00d0\16\u00d0"+
		"\u06a1\13\u00d0\5\u00d0\u06a3\n\u00d0\3\u00d1\3\u00d1\7\u00d1\u06a7\n"+
		"\u00d1\f\u00d1\16\u00d1\u06aa\13\u00d1\3\u00d2\3\u00d2\3\u00d2\3\u00d2"+
		"\3\u00d2\3\u00d2\5\u00d2\u06b2\n\u00d2\3\u00d3\3\u00d3\3\u00d3\3\u00d4"+
		"\3\u00d4\3\u00d4\3\u00d4\3\u00d4\3\u00d4\5\u00d4\u06bd\n\u00d4\3\u00d5"+
		"\3\u00d5\3\u00d5\3\u00d6\7\u00d6\u06c3\n\u00d6\f\u00d6\16\u00d6\u06c6"+
		"\13\u00d6\3\u00d7\3\u00d7\3\u00d8\3\u00d8\3\u00d8\3\u00d8\3\u00d9\6\u00d9"+
		"\u06cf\n\u00d9\r\u00d9\16\u00d9\u06d0\3\u00d9\6\u00d9\u06d4\n\u00d9\r"+
		"\u00d9\16\u00d9\u06d5\5\u00d9\u06d8\n\u00d9\3\u00d9\3\u00d9\3\u00da\3"+
		"\u00da\3\u00da\3\u00db\3\u00db\3\u00db\3\u00db\7\u00db\u06e3\n\u00db\f"+
		"\u00db\16\u00db\u06e6\13\u00db\3\u00db\3\u00db\3\u00db\3\u00db\5\u00db"+
		"\u06ec\n\u00db\3\u00db\3\u00db\3\u00db\3\u00db\3\u00dc\3\u00dc\3\u00dc"+
		"\3\u00dc\7\u00dc\u06f6\n\u00dc\f\u00dc\16\u00dc\u06f9\13\u00dc\3\u00dc"+
		"\3\u00dc\3\u00dc\3\u00dc\3\u00dd\3\u00dd\3\u00dd\3\u00dd\3\u00dd\3\u00dd"+
		"\3\u00dd\3\u00dd\3\u00dd\3\u00dd\3\u00dd\7\u00dd\u070a\n\u00dd\f\u00dd"+
		"\16\u00dd\u070d\13\u00dd\3\u00dd\3\u00dd\3\u00de\3\u00de\3\u00de\3\u06e4"+
		"\2\2\u00df\t\2\3\13\2\4\r\2\2\17\2\2\21\2\2\23\2\5\25\2\6\27\2\2\31\2"+
		"\2\33\2\2\35\2\2\37\2\2!\2\2#\2\2%\2\2\'\2\2)\2\2+\2\2-\2\2/\2\7\61\2"+
		"\b\63\2\2\65\2\2\67\2\29\2\2;\2\2=\2\2?\2\tA\2\nC\2\13E\2\fG\2\rI\2\16"+
		"K\2\17M\2\20O\2\21Q\2\22S\2\23U\2\2W\2\24Y\2\2[\2\25]\2\26_\2\2a\2\27"+
		"c\2\30e\2\31g\2\32i\2\33k\2\2m\2\34o\2\35q\2\36s\2\37u\2 w\2\2y\2!{\2"+
		"\"}\2#\177\2$\u0081\2%\u0083\2&\u0085\2\'\u0087\2\2\u0089\2\2\u008b\2"+
		"(\u008d\2)\u008f\2*\u0091\2+\u0093\2,\u0095\2-\u0097\2.\u0099\2/\u009b"+
		"\2\60\u009d\2\61\u009f\2\62\u00a1\2\63\u00a3\2\2\u00a5\2\64\u00a7\2\65"+
		"\u00a9\2\66\u00ab\2\67\u00ad\28\u00af\29\u00b1\2:\u00b3\2;\u00b5\2<\u00b7"+
		"\2=\u00b9\2>\u00bb\2?\u00bd\2@\u00bf\2A\u00c1\2B\u00c3\2C\u00c5\2D\u00c7"+
		"\2\2\u00c9\2\2\u00cb\2\2\u00cd\2\2\u00cf\2\2\u00d1\2\2\u00d3\2\2\u00d5"+
		"\2\2\u00d7\2\2\u00d9\2\2\u00db\2\2\u00dd\2\2\u00df\2\2\u00e1\2\2\u00e3"+
		"\2\2\u00e5\2\2\u00e7\2E\u00e9\2\2\u00eb\2\2\u00ed\2\2\u00ef\2\2\u00f1"+
		"\2\2\u00f3\2\2\u00f5\2F\u00f7\2\2\u00f9\2\2\u00fb\2\2\u00fd\2\2\u00ff"+
		"\2\2\u0101\2\2\u0103\2\2\u0105\2\2\u0107\2\2\u0109\2\2\u010b\2\2\u010d"+
		"\2\2\u010f\2\2\u0111\2\2\u0113\2\2\u0115\2\2\u0117\2\2\u0119\2\2\u011b"+
		"\2\2\u011d\2G\u011f\2H\u0121\2I\u0123\2J\u0125\2K\u0127\2L\u0129\2M\u012b"+
		"\2N\u012d\2O\u012f\2P\u0131\2Q\u0133\2R\u0135\2S\u0137\2T\u0139\2U\u013b"+
		"\2V\u013d\2W\u013f\2X\u0141\2Y\u0143\2Z\u0145\2[\u0147\2\\\u0149\2]\u014b"+
		"\2^\u014d\2_\u014f\2`\u0151\2a\u0153\2b\u0155\2c\u0157\2d\u0159\2e\u015b"+
		"\2f\u015d\2g\u015f\2h\u0161\2i\u0163\2j\u0165\2k\u0167\2l\u0169\2m\u016b"+
		"\2n\u016d\2o\u016f\2p\u0171\2q\u0173\2r\u0175\2s\u0177\2t\u0179\2u\u017b"+
		"\2v\u017d\2w\u017f\2x\u0181\2y\u0183\2z\u0185\2{\u0187\2|\u0189\2}\u018b"+
		"\2~\u018d\2\177\u018f\2\u0080\u0191\2\u0081\u0193\2\u0082\u0195\2\u0083"+
		"\u0197\2\u0084\u0199\2\u0085\u019b\2\u0086\u019d\2\u0087\u019f\2\u0088"+
		"\u01a1\2\u0089\u01a3\2\u008a\u01a5\2\u008b\u01a7\2\2\u01a9\2\2\u01ab\2"+
		"\2\u01ad\2\2\u01af\2\2\u01b1\2\2\u01b3\2\u008c\u01b5\2\u008d\u01b7\2\u008e"+
		"\u01b9\2\u008f\u01bb\2\2\u01bd\2\2\u01bf\2\u0090\u01c1\2\u0091\t\2\3\4"+
		"\5\6\7\b\"\7\2\f\f\17\17$$&&^^\6\2\f\f\17\17))^^\5\2$$&&^^\4\2))^^\5\2"+
		"\2\2&&\61\61\3\2\62;\b\2IIKKNNiikknn\3\2\63;\4\2\62;aa\4\2ZZzz\6\2\62"+
		";CHaach\5\2\62;CHch\3\2\629\4\2\629aa\4\2DDdd\3\2\62\63\4\2\62\63aa\4"+
		"\2GGgg\4\2--//\6\2FFHIffhi\4\2RRrr\t\2$$))^^ddhhpptv\3\2\62\65\3\2C\\"+
		"\4\2\2\u0081\ud802\udc01\3\2\ud802\udc01\3\2\udc02\ue001\5\2&&aac|\6\2"+
		"&&C\\aac|\7\2&&\62;C\\aac|\5\2\f\f\17\17\1\1\4\2\13\13\"\"\2\u073b\2\t"+
		"\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2?\3\2\2\2"+
		"\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M"+
		"\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2W\3\2\2\2\2[\3\2\2\2\2]\3\2"+
		"\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\2m\3\2\2\2"+
		"\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2y\3\2\2\2\2{\3\2\2\2\2}"+
		"\3\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2\u0083\3\2\2\2\2\u0085\3\2\2\2"+
		"\2\u008b\3\2\2\2\2\u008d\3\2\2\2\2\u008f\3\2\2\2\2\u0091\3\2\2\2\2\u0093"+
		"\3\2\2\2\2\u0095\3\2\2\2\2\u0097\3\2\2\2\2\u0099\3\2\2\2\2\u009b\3\2\2"+
		"\2\2\u009d\3\2\2\2\2\u009f\3\2\2\2\2\u00a1\3\2\2\2\2\u00a5\3\2\2\2\2\u00a7"+
		"\3\2\2\2\2\u00a9\3\2\2\2\2\u00ab\3\2\2\2\2\u00ad\3\2\2\2\2\u00af\3\2\2"+
		"\2\2\u00b1\3\2\2\2\2\u00b3\3\2\2\2\2\u00b5\3\2\2\2\2\u00b7\3\2\2\2\2\u00b9"+
		"\3\2\2\2\2\u00bb\3\2\2\2\2\u00bd\3\2\2\2\2\u00bf\3\2\2\2\2\u00c1\3\2\2"+
		"\2\2\u00c3\3\2\2\2\2\u00c5\3\2\2\2\2\u00e7\3\2\2\2\2\u00f5\3\2\2\2\2\u011d"+
		"\3\2\2\2\2\u011f\3\2\2\2\2\u0121\3\2\2\2\2\u0123\3\2\2\2\2\u0125\3\2\2"+
		"\2\2\u0127\3\2\2\2\2\u0129\3\2\2\2\2\u012b\3\2\2\2\2\u012d\3\2\2\2\2\u012f"+
		"\3\2\2\2\2\u0131\3\2\2\2\2\u0133\3\2\2\2\2\u0135\3\2\2\2\2\u0137\3\2\2"+
		"\2\2\u0139\3\2\2\2\2\u013b\3\2\2\2\2\u013d\3\2\2\2\2\u013f\3\2\2\2\2\u0141"+
		"\3\2\2\2\2\u0143\3\2\2\2\2\u0145\3\2\2\2\2\u0147\3\2\2\2\2\u0149\3\2\2"+
		"\2\2\u014b\3\2\2\2\2\u014d\3\2\2\2\2\u014f\3\2\2\2\2\u0151\3\2\2\2\2\u0153"+
		"\3\2\2\2\2\u0155\3\2\2\2\2\u0157\3\2\2\2\2\u0159\3\2\2\2\2\u015b\3\2\2"+
		"\2\2\u015d\3\2\2\2\2\u015f\3\2\2\2\2\u0161\3\2\2\2\2\u0163\3\2\2\2\2\u0165"+
		"\3\2\2\2\2\u0167\3\2\2\2\2\u0169\3\2\2\2\2\u016b\3\2\2\2\2\u016d\3\2\2"+
		"\2\2\u016f\3\2\2\2\2\u0171\3\2\2\2\2\u0173\3\2\2\2\2\u0175\3\2\2\2\2\u0177"+
		"\3\2\2\2\2\u0179\3\2\2\2\2\u017b\3\2\2\2\2\u017d\3\2\2\2\2\u017f\3\2\2"+
		"\2\2\u0181\3\2\2\2\2\u0183\3\2\2\2\2\u0185\3\2\2\2\2\u0187\3\2\2\2\2\u0189"+
		"\3\2\2\2\2\u018b\3\2\2\2\2\u018d\3\2\2\2\2\u018f\3\2\2\2\2\u0191\3\2\2"+
		"\2\2\u0193\3\2\2\2\2\u0195\3\2\2\2\2\u0197\3\2\2\2\2\u0199\3\2\2\2\2\u019b"+
		"\3\2\2\2\2\u019d\3\2\2\2\2\u019f\3\2\2\2\2\u01a1\3\2\2\2\2\u01a3\3\2\2"+
		"\2\2\u01a5\3\2\2\2\2\u01b3\3\2\2\2\2\u01b5\3\2\2\2\2\u01b7\3\2\2\2\2\u01b9"+
		"\3\2\2\2\2\u01bb\3\2\2\2\2\u01bd\3\2\2\2\2\u01bf\3\2\2\2\2\u01c1\3\2\2"+
		"\2\3\23\3\2\2\2\3\25\3\2\2\2\3\27\3\2\2\2\4\31\3\2\2\2\4\33\3\2\2\2\4"+
		"\35\3\2\2\2\5\37\3\2\2\2\5!\3\2\2\2\5#\3\2\2\2\6%\3\2\2\2\6\'\3\2\2\2"+
		"\6)\3\2\2\2\7+\3\2\2\2\7-\3\2\2\2\b/\3\2\2\2\b\61\3\2\2\2\t\u0203\3\2"+
		"\2\2\13\u0205\3\2\2\2\r\u0211\3\2\2\2\17\u021e\3\2\2\2\21\u022d\3\2\2"+
		"\2\23\u023b\3\2\2\2\25\u023f\3\2\2\2\27\u0243\3\2\2\2\31\u0247\3\2\2\2"+
		"\33\u024c\3\2\2\2\35\u0251\3\2\2\2\37\u0256\3\2\2\2!\u025d\3\2\2\2#\u0263"+
		"\3\2\2\2%\u0267\3\2\2\2\'\u026c\3\2\2\2)\u0272\3\2\2\2+\u0276\3\2\2\2"+
		"-\u027d\3\2\2\2/\u0283\3\2\2\2\61\u0286\3\2\2\2\63\u028d\3\2\2\2\65\u0291"+
		"\3\2\2\2\67\u0298\3\2\2\29\u029f\3\2\2\2;\u02a6\3\2\2\2=\u02b6\3\2\2\2"+
		"?\u02b8\3\2\2\2A\u02bb\3\2\2\2C\u02bf\3\2\2\2E\u02c2\3\2\2\2G\u02c8\3"+
		"\2\2\2I\u02d3\3\2\2\2K\u02d9\3\2\2\2M\u02df\3\2\2\2O\u02ed\3\2\2\2Q\u02ef"+
		"\3\2\2\2S\u02f8\3\2\2\2U\u02ff\3\2\2\2W\u0307\3\2\2\2Y\u030d\3\2\2\2["+
		"\u0312\3\2\2\2]\u0317\3\2\2\2_\u031d\3\2\2\2a\u0322\3\2\2\2c\u0328\3\2"+
		"\2\2e\u032e\3\2\2\2g\u0337\3\2\2\2i\u033f\3\2\2\2k\u0342\3\2\2\2m\u0349"+
		"\3\2\2\2o\u034e\3\2\2\2q\u0353\3\2\2\2s\u035b\3\2\2\2u\u0361\3\2\2\2w"+
		"\u0369\3\2\2\2y\u036f\3\2\2\2{\u0373\3\2\2\2}\u0376\3\2\2\2\177\u037b"+
		"\3\2\2\2\u0081\u0386\3\2\2\2\u0083\u038d\3\2\2\2\u0085\u0398\3\2\2\2\u0087"+
		"\u03a2\3\2\2\2\u0089\u03a6\3\2\2\2\u008b\u03ab\3\2\2\2\u008d\u03b2\3\2"+
		"\2\2\u008f\u03b9\3\2\2\2\u0091\u03bd\3\2\2\2\u0093\u03c8\3\2\2\2\u0095"+
		"\u03d0\3\2\2\2\u0097\u03d8\3\2\2\2\u0099\u03e0\3\2\2\2\u009b\u03ea\3\2"+
		"\2\2\u009d\u03f1\3\2\2\2\u009f\u03f8\3\2\2\2\u00a1\u03ff\3\2\2\2\u00a3"+
		"\u0406\3\2\2\2\u00a5\u040c\3\2\2\2\u00a7\u0413\3\2\2\2\u00a9\u041c\3\2"+
		"\2\2\u00ab\u0422\3\2\2\2\u00ad\u0429\3\2\2\2\u00af\u0436\3\2\2\2\u00b1"+
		"\u043b\3\2\2\2\u00b3\u0441\3\2\2\2\u00b5\u0448\3\2\2\2\u00b7\u0452\3\2"+
		"\2\2\u00b9\u0456\3\2\2\2\u00bb\u045c\3\2\2\2\u00bd\u0460\3\2\2\2\u00bf"+
		"\u0465\3\2\2\2\u00c1\u046e\3\2\2\2\u00c3\u0474\3\2\2\2\u00c5\u048f\3\2"+
		"\2\2\u00c7\u0491\3\2\2\2\u00c9\u0495\3\2\2\2\u00cb\u0499\3\2\2\2\u00cd"+
		"\u049d\3\2\2\2\u00cf\u04a1\3\2\2\2\u00d1\u04ad\3\2\2\2\u00d3\u04af\3\2"+
		"\2\2\u00d5\u04ba\3\2\2\2\u00d7\u04be\3\2\2\2\u00d9\u04c0\3\2\2\2\u00db"+
		"\u04c4\3\2\2\2\u00dd\u04ce\3\2\2\2\u00df\u04d0\3\2\2\2\u00e1\u04d9\3\2"+
		"\2\2\u00e3\u04e3\3\2\2\2\u00e5\u04e7\3\2\2\2\u00e7\u04f3\3\2\2\2\u00e9"+
		"\u050d\3\2\2\2\u00eb\u050f\3\2\2\2\u00ed\u0515\3\2\2\2\u00ef\u0517\3\2"+
		"\2\2\u00f1\u0527\3\2\2\2\u00f3\u0529\3\2\2\2\u00f5\u0538\3\2\2\2\u00f7"+
		"\u0541\3\2\2\2\u00f9\u054f\3\2\2\2\u00fb\u0551\3\2\2\2\u00fd\u0558\3\2"+
		"\2\2\u00ff\u055b\3\2\2\2\u0101\u0563\3\2\2\2\u0103\u0565\3\2\2\2\u0105"+
		"\u0568\3\2\2\2\u0107\u056a\3\2\2\2\u0109\u056c\3\2\2\2\u010b\u056e\3\2"+
		"\2\2\u010d\u0570\3\2\2\2\u010f\u0572\3\2\2\2\u0111\u0576\3\2\2\2\u0113"+
		"\u057a\3\2\2\2\u0115\u057d\3\2\2\2\u0117\u0580\3\2\2\2\u0119\u0583\3\2"+
		"\2\2\u011b\u0586\3\2\2\2\u011d\u058a\3\2\2\2\u011f\u058f\3\2\2\2\u0121"+
		"\u0592\3\2\2\2\u0123\u0596\3\2\2\2\u0125\u059a\3\2\2\2\u0127\u059f\3\2"+
		"\2\2\u0129\u05a2\3\2\2\2\u012b\u05a5\3\2\2\2\u012d\u05ac\3\2\2\2\u012f"+
		"\u05b0\3\2\2\2\u0131\u05b3\3\2\2\2\u0133\u05b6\3\2\2\2\u0135\u05b9\3\2"+
		"\2\2\u0137\u05bc\3\2\2\2\u0139\u05c0\3\2\2\2\u013b\u05c3\3\2\2\2\u013d"+
		"\u05c7\3\2\2\2\u013f\u05cb\3\2\2\2\u0141\u05cf\3\2\2\2\u0143\u05d3\3\2"+
		"\2\2\u0145\u05d7\3\2\2\2\u0147\u05da\3\2\2\2\u0149\u05e8\3\2\2\2\u014b"+
		"\u05ee\3\2\2\2\u014d\u05f3\3\2\2\2\u014f\u05f8\3\2\2\2\u0151\u05fd\3\2"+
		"\2\2\u0153\u0602\3\2\2\2\u0155\u0607\3\2\2\2\u0157\u060c\3\2\2\2\u0159"+
		"\u060e\3\2\2\2\u015b\u0610\3\2\2\2\u015d\u0612\3\2\2\2\u015f\u0614\3\2"+
		"\2\2\u0161\u0616\3\2\2\2\u0163\u0618\3\2\2\2\u0165\u061a\3\2\2\2\u0167"+
		"\u061c\3\2\2\2\u0169\u061e\3\2\2\2\u016b\u0620\3\2\2\2\u016d\u0623\3\2"+
		"\2\2\u016f\u0626\3\2\2\2\u0171\u0629\3\2\2\2\u0173\u062c\3\2\2\2\u0175"+
		"\u062f\3\2\2\2\u0177\u0632\3\2\2\2\u0179\u0635\3\2\2\2\u017b\u0638\3\2"+
		"\2\2\u017d\u063a\3\2\2\2\u017f\u063c\3\2\2\2\u0181\u063e\3\2\2\2\u0183"+
		"\u0640\3\2\2\2\u0185\u0642\3\2\2\2\u0187\u0644\3\2\2\2\u0189\u0646\3\2"+
		"\2\2\u018b\u0648\3\2\2\2\u018d\u064b\3\2\2\2\u018f\u064e\3\2\2\2\u0191"+
		"\u0651\3\2\2\2\u0193\u0654\3\2\2\2\u0195\u0657\3\2\2\2\u0197\u065a\3\2"+
		"\2\2\u0199\u065d\3\2\2\2\u019b\u0660\3\2\2\2\u019d\u0664\3\2\2\2\u019f"+
		"\u0668\3\2\2\2\u01a1\u066d\3\2\2\2\u01a3\u0688\3\2\2\2\u01a5\u06a2\3\2"+
		"\2\2\u01a7\u06a4\3\2\2\2\u01a9\u06b1\3\2\2\2\u01ab\u06b3\3\2\2\2\u01ad"+
		"\u06bc\3\2\2\2\u01af\u06be\3\2\2\2\u01b1\u06c4\3\2\2\2\u01b3\u06c7\3\2"+
		"\2\2\u01b5\u06c9\3\2\2\2\u01b7\u06d7\3\2\2\2\u01b9\u06db\3\2\2\2\u01bb"+
		"\u06de\3\2\2\2\u01bd\u06f1\3\2\2\2\u01bf\u06fe\3\2\2\2\u01c1\u0710\3\2"+
		"\2\2\u01c3\u01c7\5\u010b\u0083\2\u01c4\u01c6\5\63\27\2\u01c5\u01c4\3\2"+
		"\2\2\u01c6\u01c9\3\2\2\2\u01c7\u01c5\3\2\2\2\u01c7\u01c8\3\2\2\2\u01c8"+
		"\u01ca\3\2\2\2\u01c9\u01c7\3\2\2\2\u01ca\u01cb\5\u010b\u0083\2\u01cb\u0204"+
		"\3\2\2\2\u01cc\u01d0\5\u010d\u0084\2\u01cd\u01cf\5\65\30\2\u01ce\u01cd"+
		"\3\2\2\2\u01cf\u01d2\3\2\2\2\u01d0\u01ce\3\2\2\2\u01d0\u01d1\3\2\2\2\u01d1"+
		"\u01d3\3\2\2\2\u01d2\u01d0\3\2\2\2\u01d3\u01d4\5\u010d\u0084\2\u01d4\u0204"+
		"\3\2\2\2\u01d5\u01d6\5\u0107\u0081\2\u01d6\u01e7\6\2\2\2\u01d7\u01d9\5"+
		";\33\2\u01d8\u01d7\3\2\2\2\u01d9\u01da\3\2\2\2\u01da\u01d8\3\2\2\2\u01da"+
		"\u01db\3\2\2\2\u01db\u01dc\3\2\2\2\u01dc\u01dd\5\u0107\u0081\2\u01dd\u01e8"+
		"\3\2\2\2\u01de\u01e2\6\2\3\2\u01df\u01e1\5;\33\2\u01e0\u01df\3\2\2\2\u01e1"+
		"\u01e4\3\2\2\2\u01e2\u01e0\3\2\2\2\u01e2\u01e3\3\2\2\2\u01e3\u01e5\3\2"+
		"\2\2\u01e4\u01e2\3\2\2\2\u01e5\u01e6\7\2\2\3\u01e6\u01e8\b\2\2\2\u01e7"+
		"\u01d8\3\2\2\2\u01e7\u01de\3\2\2\2\u01e8\u0204\3\2\2\2\u01e9\u01ed\5\u010f"+
		"\u0085\2\u01ea\u01ec\5\67\31\2\u01eb\u01ea\3\2\2\2\u01ec\u01ef\3\2\2\2"+
		"\u01ed\u01eb\3\2\2\2\u01ed\u01ee\3\2\2\2\u01ee\u01f0\3\2\2\2\u01ef\u01ed"+
		"\3\2\2\2\u01f0\u01f1\5\u010f\u0085\2\u01f1\u0204\3\2\2\2\u01f2\u01f6\5"+
		"\u0111\u0086\2\u01f3\u01f5\59\32\2\u01f4\u01f3\3\2\2\2\u01f5\u01f8\3\2"+
		"\2\2\u01f6\u01f4\3\2\2\2\u01f6\u01f7\3\2\2\2\u01f7\u01f9\3\2\2\2\u01f8"+
		"\u01f6\3\2\2\2\u01f9\u01fa\5\u0111\u0086\2\u01fa\u0204\3\2\2\2\u01fb\u01fd"+
		"\5\u0113\u0087\2\u01fc\u01fe\5=\34\2\u01fd\u01fc\3\2\2\2\u01fe\u01ff\3"+
		"\2\2\2\u01ff\u01fd\3\2\2\2\u01ff\u0200\3\2\2\2\u0200\u0201\3\2\2\2\u0201"+
		"\u0202\5\u0115\u0088\2\u0202\u0204\3\2\2\2\u0203\u01c3\3\2\2\2\u0203\u01cc"+
		"\3\2\2\2\u0203\u01d5\3\2\2\2\u0203\u01e9\3\2\2\2\u0203\u01f2\3\2\2\2\u0203"+
		"\u01fb\3\2\2\2\u0204\n\3\2\2\2\u0205\u0209\5\u010b\u0083\2\u0206\u0208"+
		"\5\63\27\2\u0207\u0206\3\2\2\2\u0208\u020b\3\2\2\2\u0209\u0207\3\2\2\2"+
		"\u0209\u020a\3\2\2\2\u020a\u020c\3\2\2\2\u020b\u0209\3\2\2\2\u020c\u020d"+
		"\5\u0109\u0082\2\u020d\u020e\3\2\2\2\u020e\u020f\b\3\3\2\u020f\u0210\b"+
		"\3\4\2\u0210\f\3\2\2\2\u0211\u0215\5\u010f\u0085\2\u0212\u0214\5\67\31"+
		"\2\u0213\u0212\3\2\2\2\u0214\u0217\3\2\2\2\u0215\u0213\3\2\2\2\u0215\u0216"+
		"\3\2\2\2\u0216\u0218\3\2\2\2\u0217\u0215\3\2\2\2\u0218\u0219\5\u0109\u0082"+
		"\2\u0219\u021a\3\2\2\2\u021a\u021b\b\4\5\2\u021b\u021c\b\4\6\2\u021c\u021d"+
		"\b\4\4\2\u021d\16\3\2\2\2\u021e\u021f\5\u0107\u0081\2\u021f\u0223\6\5"+
		"\4\2\u0220\u0222\5;\33\2\u0221\u0220\3\2\2\2\u0222\u0225\3\2\2\2\u0223"+
		"\u0221\3\2\2\2\u0223\u0224\3\2\2\2\u0224\u0226\3\2\2\2\u0225\u0223\3\2"+
		"\2\2\u0226\u0227\5\u0109\u0082\2\u0227\u0228\6\5\5\2\u0228\u0229\3\2\2"+
		"\2\u0229\u022a\b\5\5\2\u022a\u022b\b\5\7\2\u022b\u022c\b\5\4\2\u022c\20"+
		"\3\2\2\2\u022d\u0231\5\u0113\u0087\2\u022e\u0230\5=\34\2\u022f\u022e\3"+
		"\2\2\2\u0230\u0233\3\2\2\2\u0231\u022f\3\2\2\2\u0231\u0232\3\2\2\2\u0232"+
		"\u0234\3\2\2\2\u0233\u0231\3\2\2\2\u0234\u0235\5\u0109\u0082\2\u0235\u0236"+
		"\6\6\6\2\u0236\u0237\3\2\2\2\u0237\u0238\b\6\5\2\u0238\u0239\b\6\b\2\u0239"+
		"\u023a\b\6\4\2\u023a\22\3\2\2\2\u023b\u023c\5\u010b\u0083\2\u023c\u023d"+
		"\3\2\2\2\u023d\u023e\b\7\t\2\u023e\24\3\2\2\2\u023f\u0240\5\u0109\u0082"+
		"\2\u0240\u0241\3\2\2\2\u0241\u0242\b\b\4\2\u0242\26\3\2\2\2\u0243\u0244"+
		"\5\63\27\2\u0244\u0245\3\2\2\2\u0245\u0246\b\t\n\2\u0246\30\3\2\2\2\u0247"+
		"\u0248\5\u010f\u0085\2\u0248\u0249\3\2\2\2\u0249\u024a\b\n\13\2\u024a"+
		"\u024b\b\n\t\2\u024b\32\3\2\2\2\u024c\u024d\5\u0109\u0082\2\u024d\u024e"+
		"\3\2\2\2\u024e\u024f\b\13\f\2\u024f\u0250\b\13\4\2\u0250\34\3\2\2\2\u0251"+
		"\u0252\5\67\31\2\u0252\u0253\3\2\2\2\u0253\u0254\b\f\n\2\u0254\36\3\2"+
		"\2\2\u0255\u0257\5\u0109\u0082\2\u0256\u0255\3\2\2\2\u0256\u0257\3\2\2"+
		"\2\u0257\u0258\3\2\2\2\u0258\u0259\5\u0107\u0081\2\u0259\u025a\3\2\2\2"+
		"\u025a\u025b\b\r\13\2\u025b\u025c\b\r\t\2\u025c \3\2\2\2\u025d\u025e\5"+
		"\u0109\u0082\2\u025e\u025f\6\16\7\2\u025f\u0260\3\2\2\2\u0260\u0261\b"+
		"\16\f\2\u0261\u0262\b\16\4\2\u0262\"\3\2\2\2\u0263\u0264\5;\33\2\u0264"+
		"\u0265\3\2\2\2\u0265\u0266\b\17\n\2\u0266$\3\2\2\2\u0267\u0268\5\u0115"+
		"\u0088\2\u0268\u0269\3\2\2\2\u0269\u026a\b\20\13\2\u026a\u026b\b\20\t"+
		"\2\u026b&\3\2\2\2\u026c\u026d\5\u0109\u0082\2\u026d\u026e\6\21\b\2\u026e"+
		"\u026f\3\2\2\2\u026f\u0270\b\21\f\2\u0270\u0271\b\21\4\2\u0271(\3\2\2"+
		"\2\u0272\u0273\5=\34\2\u0273\u0274\3\2\2\2\u0274\u0275\b\22\n\2\u0275"+
		"*\3\2\2\2\u0276\u0277\7}\2\2\u0277\u0278\b\23\r\2\u0278\u0279\3\2\2\2"+
		"\u0279\u027a\b\23\16\2\u027a\u027b\b\23\t\2\u027b\u027c\b\23\17\2\u027c"+
		",\3\2\2\2\u027d\u027e\5\u01a7\u00d1\2\u027e\u027f\3\2\2\2\u027f\u0280"+
		"\b\24\20\2\u0280\u0281\b\24\t\2\u0281\u0282\b\24\21\2\u0282.\3\2\2\2\u0283"+
		"\u0284\7\60\2\2\u0284\u0285\5\u01a7\u00d1\2\u0285\60\3\2\2\2\u0286\u0287"+
		"\13\2\2\2\u0287\u0288\b\26\22\2\u0288\u0289\3\2\2\2\u0289\u028a\b\26\t"+
		"\2\u028a\62\3\2\2\2\u028b\u028e\n\2\2\2\u028c\u028e\5\u00f7y\2\u028d\u028b"+
		"\3\2\2\2\u028d\u028c\3\2\2\2\u028e\64\3\2\2\2\u028f\u0292\n\3\2\2\u0290"+
		"\u0292\5\u00f7y\2\u0291\u028f\3\2\2\2\u0291\u0290\3\2\2\2\u0292\66\3\2"+
		"\2\2\u0293\u0299\n\4\2\2\u0294\u0295\5\u010b\u0083\2\u0295\u0296\6\31"+
		"\t\2\u0296\u0299\3\2\2\2\u0297\u0299\5\u00f7y\2\u0298\u0293\3\2\2\2\u0298"+
		"\u0294\3\2\2\2\u0298\u0297\3\2\2\2\u02998\3\2\2\2\u029a\u02a0\n\5\2\2"+
		"\u029b\u029c\5\u010d\u0084\2\u029c\u029d\6\32\n\2\u029d\u02a0\3\2\2\2"+
		"\u029e\u02a0\5\u00f7y\2\u029f\u029a\3\2\2\2\u029f\u029b\3\2\2\2\u029f"+
		"\u029e\3\2\2\2\u02a0:\3\2\2\2\u02a1\u02a7\5\u0103\177\2\u02a2\u02a3\5"+
		"\u0109\u0082\2\u02a3\u02a4\6\33\13\2\u02a4\u02a7\3\2\2\2\u02a5\u02a7\n"+
		"\6\2\2\u02a6\u02a1\3\2\2\2\u02a6\u02a2\3\2\2\2\u02a6\u02a5\3\2\2\2\u02a7"+
		"<\3\2\2\2\u02a8\u02b7\5\u0119\u008a\2\u02a9\u02aa\5\u011b\u008b\2\u02aa"+
		"\u02ab\6\34\f\2\u02ab\u02b7\3\2\2\2\u02ac\u02ad\5\u0117\u0089\2\u02ad"+
		"\u02ae\6\34\r\2\u02ae\u02b7\3\2\2\2\u02af\u02b0\5\u0107\u0081\2\u02b0"+
		"\u02b1\6\34\16\2\u02b1\u02b7\3\2\2\2\u02b2\u02b3\5\u0109\u0082\2\u02b3"+
		"\u02b4\6\34\17\2\u02b4\u02b7\3\2\2\2\u02b5\u02b7\n\6\2\2\u02b6\u02a8\3"+
		"\2\2\2\u02b6\u02a9\3\2\2\2\u02b6\u02ac\3\2\2\2\u02b6\u02af\3\2\2\2\u02b6"+
		"\u02b2\3\2\2\2\u02b6\u02b5\3\2\2\2\u02b7>\3\2\2\2\u02b8\u02b9\7c\2\2\u02b9"+
		"\u02ba\7u\2\2\u02ba@\3\2\2\2\u02bb\u02bc\7f\2\2\u02bc\u02bd\7g\2\2\u02bd"+
		"\u02be\7h\2\2\u02beB\3\2\2\2\u02bf\u02c0\7k\2\2\u02c0\u02c1\7p\2\2\u02c1"+
		"D\3\2\2\2\u02c2\u02c3\7v\2\2\u02c3\u02c4\7t\2\2\u02c4\u02c5\7c\2\2\u02c5"+
		"\u02c6\7k\2\2\u02c6\u02c7\7v\2\2\u02c7F\3\2\2\2\u02c8\u02c9\7v\2\2\u02c9"+
		"\u02ca\7j\2\2\u02ca\u02cb\7t\2\2\u02cb\u02cc\7g\2\2\u02cc\u02cd\7c\2\2"+
		"\u02cd\u02ce\7f\2\2\u02ce\u02cf\7u\2\2\u02cf\u02d0\7c\2\2\u02d0\u02d1"+
		"\7h\2\2\u02d1\u02d2\7g\2\2\u02d2H\3\2\2\2\u02d3\u02d4\7c\2\2\u02d4\u02d5"+
		"\7u\2\2\u02d5\u02d6\7{\2\2\u02d6\u02d7\7p\2\2\u02d7\u02d8\7e\2\2\u02d8"+
		"J\3\2\2\2\u02d9\u02da\7c\2\2\u02da\u02db\7y\2\2\u02db\u02dc\7c\2\2\u02dc"+
		"\u02dd\7k\2\2\u02dd\u02de\7v\2\2\u02deL\3\2\2\2\u02df\u02e0\7f\2\2\u02e0"+
		"\u02e1\7g\2\2\u02e1\u02e2\7h\2\2\u02e2\u02e3\7g\2\2\u02e3\u02e4\7t\2\2"+
		"\u02e4N\3\2\2\2\u02e5\u02ee\5U(\2\u02e6\u02ee\5_-\2\u02e7\u02ee\5Y*\2"+
		"\u02e8\u02ee\5\u00a3O\2\u02e9\u02ee\5\u0087A\2\u02ea\u02ee\5\u0089B\2"+
		"\u02eb\u02ee\5w9\2\u02ec\u02ee\5k\63\2\u02ed\u02e5\3\2\2\2\u02ed\u02e6"+
		"\3\2\2\2\u02ed\u02e7\3\2\2\2\u02ed\u02e8\3\2\2\2\u02ed\u02e9\3\2\2\2\u02ed"+
		"\u02ea\3\2\2\2\u02ed\u02eb\3\2\2\2\u02ed\u02ec\3\2\2\2\u02eeP\3\2\2\2"+
		"\u02ef\u02f0\7c\2\2\u02f0\u02f1\7d\2\2\u02f1\u02f2\7u\2\2\u02f2\u02f3"+
		"\7v\2\2\u02f3\u02f4\7t\2\2\u02f4\u02f5\7c\2\2\u02f5\u02f6\7e\2\2\u02f6"+
		"\u02f7\7v\2\2\u02f7R\3\2\2\2\u02f8\u02f9\7c\2\2\u02f9\u02fa\7u\2\2\u02fa"+
		"\u02fb\7u\2\2\u02fb\u02fc\7g\2\2\u02fc\u02fd\7t\2\2\u02fd\u02fe\7v\2\2"+
		"\u02feT\3\2\2\2\u02ff\u0300\7d\2\2\u0300\u0301\7q\2\2\u0301\u0302\7q\2"+
		"\2\u0302\u0303\7n\2\2\u0303\u0304\7g\2\2\u0304\u0305\7c\2\2\u0305\u0306"+
		"\7p\2\2\u0306V\3\2\2\2\u0307\u0308\7d\2\2\u0308\u0309\7t\2\2\u0309\u030a"+
		"\7g\2\2\u030a\u030b\7c\2\2\u030b\u030c\7m\2\2\u030cX\3\2\2\2\u030d\u030e"+
		"\7d\2\2\u030e\u030f\7{\2\2\u030f\u0310\7v\2\2\u0310\u0311\7g\2\2\u0311"+
		"Z\3\2\2\2\u0312\u0313\7e\2\2\u0313\u0314\7c\2\2\u0314\u0315\7u\2\2\u0315"+
		"\u0316\7g\2\2\u0316\\\3\2\2\2\u0317\u0318\7e\2\2\u0318\u0319\7c\2\2\u0319"+
		"\u031a\7v\2\2\u031a\u031b\7e\2\2\u031b\u031c\7j\2\2\u031c^\3\2\2\2\u031d"+
		"\u031e\7e\2\2\u031e\u031f\7j\2\2\u031f\u0320\7c\2\2\u0320\u0321\7t\2\2"+
		"\u0321`\3\2\2\2\u0322\u0323\7e\2\2\u0323\u0324\7n\2\2\u0324\u0325\7c\2"+
		"\2\u0325\u0326\7u\2\2\u0326\u0327\7u\2\2\u0327b\3\2\2\2\u0328\u0329\7"+
		"e\2\2\u0329\u032a\7q\2\2\u032a\u032b\7p\2\2\u032b\u032c\7u\2\2\u032c\u032d"+
		"\7v\2\2\u032dd\3\2\2\2\u032e\u032f\7e\2\2\u032f\u0330\7q\2\2\u0330\u0331"+
		"\7p\2\2\u0331\u0332\7v\2\2\u0332\u0333\7k\2\2\u0333\u0334\7p\2\2\u0334"+
		"\u0335\7w\2\2\u0335\u0336\7g\2\2\u0336f\3\2\2\2\u0337\u0338\7f\2\2\u0338"+
		"\u0339\7g\2\2\u0339\u033a\7h\2\2\u033a\u033b\7c\2\2\u033b\u033c\7w\2\2"+
		"\u033c\u033d\7n\2\2\u033d\u033e\7v\2\2\u033eh\3\2\2\2\u033f\u0340\7f\2"+
		"\2\u0340\u0341\7q\2\2\u0341j\3\2\2\2\u0342\u0343\7f\2\2\u0343\u0344\7"+
		"q\2\2\u0344\u0345\7w\2\2\u0345\u0346\7d\2\2\u0346\u0347\7n\2\2\u0347\u0348"+
		"\7g\2\2\u0348l\3\2\2\2\u0349\u034a\7g\2\2\u034a\u034b\7n\2\2\u034b\u034c"+
		"\7u\2\2\u034c\u034d\7g\2\2\u034dn\3\2\2\2\u034e\u034f\7g\2\2\u034f\u0350"+
		"\7p\2\2\u0350\u0351\7w\2\2\u0351\u0352\7o\2\2\u0352p\3\2\2\2\u0353\u0354"+
		"\7g\2\2\u0354\u0355\7z\2\2\u0355\u0356\7v\2\2\u0356\u0357\7g\2\2\u0357"+
		"\u0358\7p\2\2\u0358\u0359\7f\2\2\u0359\u035a\7u\2\2\u035ar\3\2\2\2\u035b"+
		"\u035c\7h\2\2\u035c\u035d\7k\2\2\u035d\u035e\7p\2\2\u035e\u035f\7c\2\2"+
		"\u035f\u0360\7n\2\2\u0360t\3\2\2\2\u0361\u0362\7h\2\2\u0362\u0363\7k\2"+
		"\2\u0363\u0364\7p\2\2\u0364\u0365\7c\2\2\u0365\u0366\7n\2\2\u0366\u0367"+
		"\7n\2\2\u0367\u0368\7{\2\2\u0368v\3\2\2\2\u0369\u036a\7h\2\2\u036a\u036b"+
		"\7n\2\2\u036b\u036c\7q\2\2\u036c\u036d\7c\2\2\u036d\u036e\7v\2\2\u036e"+
		"x\3\2\2\2\u036f\u0370\7h\2\2\u0370\u0371\7q\2\2\u0371\u0372\7t\2\2\u0372"+
		"z\3\2\2\2\u0373\u0374\7k\2\2\u0374\u0375\7h\2\2\u0375|\3\2\2\2\u0376\u0377"+
		"\7i\2\2\u0377\u0378\7q\2\2\u0378\u0379\7v\2\2\u0379\u037a\7q\2\2\u037a"+
		"~\3\2\2\2\u037b\u037c\7k\2\2\u037c\u037d\7o\2\2\u037d\u037e\7r\2\2\u037e"+
		"\u037f\7n\2\2\u037f\u0380\7g\2\2\u0380\u0381\7o\2\2\u0381\u0382\7g\2\2"+
		"\u0382\u0383\7p\2\2\u0383\u0384\7v\2\2\u0384\u0385\7u\2\2\u0385\u0080"+
		"\3\2\2\2\u0386\u0387\7k\2\2\u0387\u0388\7o\2\2\u0388\u0389\7r\2\2\u0389"+
		"\u038a\7q\2\2\u038a\u038b\7t\2\2\u038b\u038c\7v\2\2\u038c\u0082\3\2\2"+
		"\2\u038d\u038e\7k\2\2\u038e\u038f\7p\2\2\u038f\u0390\7u\2\2\u0390\u0391"+
		"\7v\2\2\u0391\u0392\7c\2\2\u0392\u0393\7p\2\2\u0393\u0394\7e\2\2\u0394"+
		"\u0395\7g\2\2\u0395\u0396\7q\2\2\u0396\u0397\7h\2\2\u0397\u0084\3\2\2"+
		"\2\u0398\u0399\7k\2\2\u0399\u039a\7p\2\2\u039a\u039b\7v\2\2\u039b\u039c"+
		"\7g\2\2\u039c\u039d\7t\2\2\u039d\u039e\7h\2\2\u039e\u039f\7c\2\2\u039f"+
		"\u03a0\7e\2\2\u03a0\u03a1\7g\2\2\u03a1\u0086\3\2\2\2\u03a2\u03a3\7k\2"+
		"\2\u03a3\u03a4\7p\2\2\u03a4\u03a5\7v\2\2\u03a5\u0088\3\2\2\2\u03a6\u03a7"+
		"\7n\2\2\u03a7\u03a8\7q\2\2\u03a8\u03a9\7p\2\2\u03a9\u03aa\7i\2\2\u03aa"+
		"\u008a\3\2\2\2\u03ab\u03ac\7o\2\2\u03ac\u03ad\7q\2\2\u03ad\u03ae\7f\2"+
		"\2\u03ae\u03af\7w\2\2\u03af\u03b0\7n\2\2\u03b0\u03b1\7g\2\2\u03b1\u008c"+
		"\3\2\2\2\u03b2\u03b3\7p\2\2\u03b3\u03b4\7c\2\2\u03b4\u03b5\7v\2\2\u03b5"+
		"\u03b6\7k\2\2\u03b6\u03b7\7x\2\2\u03b7\u03b8\7g\2\2\u03b8\u008e\3\2\2"+
		"\2\u03b9\u03ba\7p\2\2\u03ba\u03bb\7g\2\2\u03bb\u03bc\7y\2\2\u03bc\u0090"+
		"\3\2\2\2\u03bd\u03be\7p\2\2\u03be\u03bf\7q\2\2\u03bf\u03c0\7p\2\2\u03c0"+
		"\u03c1\7/\2\2\u03c1\u03c2\7u\2\2\u03c2\u03c3\7g\2\2\u03c3\u03c4\7c\2\2"+
		"\u03c4\u03c5\7n\2\2\u03c5\u03c6\7g\2\2\u03c6\u03c7\7f\2\2\u03c7\u0092"+
		"\3\2\2\2\u03c8\u03c9\7r\2\2\u03c9\u03ca\7c\2\2\u03ca\u03cb\7e\2\2\u03cb"+
		"\u03cc\7m\2\2\u03cc\u03cd\7c\2\2\u03cd\u03ce\7i\2\2\u03ce\u03cf\7g\2\2"+
		"\u03cf\u0094\3\2\2\2\u03d0\u03d1\7r\2\2\u03d1\u03d2\7g\2\2\u03d2\u03d3"+
		"\7t\2\2\u03d3\u03d4\7o\2\2\u03d4\u03d5\7k\2\2\u03d5\u03d6\7v\2\2\u03d6"+
		"\u03d7\7u\2\2\u03d7\u0096\3\2\2\2\u03d8\u03d9\7r\2\2\u03d9\u03da\7t\2"+
		"\2\u03da\u03db\7k\2\2\u03db\u03dc\7x\2\2\u03dc\u03dd\7c\2\2\u03dd\u03de"+
		"\7v\2\2\u03de\u03df\7g\2\2\u03df\u0098\3\2\2\2\u03e0\u03e1\7r\2\2\u03e1"+
		"\u03e2\7t\2\2\u03e2\u03e3\7q\2\2\u03e3\u03e4\7v\2\2\u03e4\u03e5\7g\2\2"+
		"\u03e5\u03e6\7e\2\2\u03e6\u03e7\7v\2\2\u03e7\u03e8\7g\2\2\u03e8\u03e9"+
		"\7f\2\2\u03e9\u009a\3\2\2\2\u03ea\u03eb\7r\2\2\u03eb\u03ec\7w\2\2\u03ec"+
		"\u03ed\7d\2\2\u03ed\u03ee\7n\2\2\u03ee\u03ef\7k\2\2\u03ef\u03f0\7e\2\2"+
		"\u03f0\u009c\3\2\2\2\u03f1\u03f2\7t\2\2\u03f2\u03f3\7g\2\2\u03f3\u03f4"+
		"\7e\2\2\u03f4\u03f5\7q\2\2\u03f5\u03f6\7t\2\2\u03f6\u03f7\7f\2\2\u03f7"+
		"\u009e\3\2\2\2\u03f8\u03f9\7t\2\2\u03f9\u03fa\7g\2\2\u03fa\u03fb\7v\2"+
		"\2\u03fb\u03fc\7w\2\2\u03fc\u03fd\7t\2\2\u03fd\u03fe\7p\2\2\u03fe\u00a0"+
		"\3\2\2\2\u03ff\u0400\7u\2\2\u0400\u0401\7g\2\2\u0401\u0402\7c\2\2\u0402"+
		"\u0403\7n\2\2\u0403\u0404\7g\2\2\u0404\u0405\7f\2\2\u0405\u00a2\3\2\2"+
		"\2\u0406\u0407\7u\2\2\u0407\u0408\7j\2\2\u0408\u0409\7q\2\2\u0409\u040a"+
		"\7t\2\2\u040a\u040b\7v\2\2\u040b\u00a4\3\2\2\2\u040c\u040d\7u\2\2\u040d"+
		"\u040e\7v\2\2\u040e\u040f\7c\2\2\u040f\u0410\7v\2\2\u0410\u0411\7k\2\2"+
		"\u0411\u0412\7e\2\2\u0412\u00a6\3\2\2\2\u0413\u0414\7u\2\2\u0414\u0415"+
		"\7v\2\2\u0415\u0416\7t\2\2\u0416\u0417\7k\2\2\u0417\u0418\7e\2\2\u0418"+
		"\u0419\7v\2\2\u0419\u041a\7h\2\2\u041a\u041b\7r\2\2\u041b\u00a8\3\2\2"+
		"\2\u041c\u041d\7u\2\2\u041d\u041e\7w\2\2\u041e\u041f\7r\2\2\u041f\u0420"+
		"\7g\2\2\u0420\u0421\7t\2\2\u0421\u00aa\3\2\2\2\u0422\u0423\7u\2\2\u0423"+
		"\u0424\7y\2\2\u0424\u0425\7k\2\2\u0425\u0426\7v\2\2\u0426\u0427\7e\2\2"+
		"\u0427\u0428\7j\2\2\u0428\u00ac\3\2\2\2\u0429\u042a\7u\2\2\u042a\u042b"+
		"\7{\2\2\u042b\u042c\7p\2\2\u042c\u042d\7e\2\2\u042d\u042e\7j\2\2\u042e"+
		"\u042f\7t\2\2\u042f\u0430\7q\2\2\u0430\u0431\7p\2\2\u0431\u0432\7k\2\2"+
		"\u0432\u0433\7|\2\2\u0433\u0434\7g\2\2\u0434\u0435\7f\2\2\u0435\u00ae"+
		"\3\2\2\2\u0436\u0437\7v\2\2\u0437\u0438\7j\2\2\u0438\u0439\7k\2\2\u0439"+
		"\u043a\7u\2\2\u043a\u00b0\3\2\2\2\u043b\u043c\7v\2\2\u043c\u043d\7j\2"+
		"\2\u043d\u043e\7t\2\2\u043e\u043f\7q\2\2\u043f\u0440\7y\2\2\u0440\u00b2"+
		"\3\2\2\2\u0441\u0442\7v\2\2\u0442\u0443\7j\2\2\u0443\u0444\7t\2\2\u0444"+
		"\u0445\7q\2\2\u0445\u0446\7y\2\2\u0446\u0447\7u\2\2\u0447\u00b4\3\2\2"+
		"\2\u0448\u0449\7v\2\2\u0449\u044a\7t\2\2\u044a\u044b\7c\2\2\u044b\u044c"+
		"\7p\2\2\u044c\u044d\7u\2\2\u044d\u044e\7k\2\2\u044e\u044f\7g\2\2\u044f"+
		"\u0450\7p\2\2\u0450\u0451\7v\2\2\u0451\u00b6\3\2\2\2\u0452\u0453\7v\2"+
		"\2\u0453\u0454\7t\2\2\u0454\u0455\7{\2\2\u0455\u00b8\3\2\2\2\u0456\u0457"+
		"\7x\2\2\u0457\u0458\7c\2\2\u0458\u0459\7n\2\2\u0459\u045a\3\2\2\2\u045a"+
		"\u045b\6Z\20\2\u045b\u00ba\3\2\2\2\u045c\u045d\7x\2\2\u045d\u045e\7c\2"+
		"\2\u045e\u045f\7t\2\2\u045f\u00bc\3\2\2\2\u0460\u0461\7x\2\2\u0461\u0462"+
		"\7q\2\2\u0462\u0463\7k\2\2\u0463\u0464\7f\2\2\u0464\u00be\3\2\2\2\u0465"+
		"\u0466\7x\2\2\u0466\u0467\7q\2\2\u0467\u0468\7n\2\2\u0468\u0469\7c\2\2"+
		"\u0469\u046a\7v\2\2\u046a\u046b\7k\2\2\u046b\u046c\7n\2\2\u046c\u046d"+
		"\7g\2\2\u046d\u00c0\3\2\2\2\u046e\u046f\7y\2\2\u046f\u0470\7j\2\2\u0470"+
		"\u0471\7k\2\2\u0471\u0472\7n\2\2\u0472\u0473\7g\2\2\u0473\u00c2\3\2\2"+
		"\2\u0474\u0475\7{\2\2\u0475\u0476\7k\2\2\u0476\u0477\7g\2\2\u0477\u0478"+
		"\7n\2\2\u0478\u0479\7f\2\2\u0479\u00c4\3\2\2\2\u047a\u047f\5\u00c7a\2"+
		"\u047b\u047f\5\u00c9b\2\u047c\u047f\5\u00cbc\2\u047d\u047f\5\u00cdd\2"+
		"\u047e\u047a\3\2\2\2\u047e\u047b\3\2\2\2\u047e\u047c\3\2\2\2\u047e\u047d"+
		"\3\2\2\2\u047f\u0483\3\2\2\2\u0480\u0481\5\u00d7i\2\u0481\u0482\b`\23"+
		"\2\u0482\u0484\3\2\2\2\u0483\u0480\3\2\2\2\u0483\u0484\3\2\2\2\u0484\u0490"+
		"\3\2\2\2\u0485\u0487\7\62\2\2\u0486\u0488\t\7\2\2\u0487\u0486\3\2\2\2"+
		"\u0488\u0489\3\2\2\2\u0489\u0487\3\2\2\2\u0489\u048a\3\2\2\2\u048a\u048b"+
		"\3\2\2\2\u048b\u048d\b`\24\2\u048c\u048e\5\u00cfe\2\u048d\u048c\3\2\2"+
		"\2\u048d\u048e\3\2\2\2\u048e\u0490\3\2\2\2\u048f\u047e\3\2\2\2\u048f\u0485"+
		"\3\2\2\2\u0490\u00c6\3\2\2\2\u0491\u0493\5\u00d1f\2\u0492\u0494\5\u00cf"+
		"e\2\u0493\u0492\3\2\2\2\u0493\u0494\3\2\2\2\u0494\u00c8\3\2\2\2\u0495"+
		"\u0497\5\u00d9j\2\u0496\u0498\5\u00cfe\2\u0497\u0496\3\2\2\2\u0497\u0498"+
		"\3\2\2\2\u0498\u00ca\3\2\2\2\u0499\u049b\5\u00dfm\2\u049a\u049c\5\u00cf"+
		"e\2\u049b\u049a\3\2\2\2\u049b\u049c\3\2\2\2\u049c\u00cc\3\2\2\2\u049d"+
		"\u049f\5\u00e3o\2\u049e\u04a0\5\u00cfe\2\u049f\u049e\3\2\2\2\u049f\u04a0"+
		"\3\2\2\2\u04a0\u00ce\3\2\2\2\u04a1\u04a2\t\b\2\2\u04a2\u00d0\3\2\2\2\u04a3"+
		"\u04ae\7\62\2\2\u04a4\u04ab\t\t\2\2\u04a5\u04a7\5\u00d3g\2\u04a6\u04a5"+
		"\3\2\2\2\u04a6\u04a7\3\2\2\2\u04a7\u04ac\3\2\2\2\u04a8\u04a9\5\u00d5h"+
		"\2\u04a9\u04aa\5\u00d3g\2\u04aa\u04ac\3\2\2\2\u04ab\u04a6\3\2\2\2\u04ab"+
		"\u04a8\3\2\2\2\u04ac\u04ae\3\2\2\2\u04ad\u04a3\3\2\2\2\u04ad\u04a4\3\2"+
		"\2\2\u04ae\u00d2\3\2\2\2\u04af\u04b7\t\7\2\2\u04b0\u04b2\t\n\2\2\u04b1"+
		"\u04b0\3\2\2\2\u04b2\u04b5\3\2\2\2\u04b3\u04b1\3\2\2\2\u04b3\u04b4\3\2"+
		"\2\2\u04b4\u04b6\3\2\2\2\u04b5\u04b3\3\2\2\2\u04b6\u04b8\t\7\2\2\u04b7"+
		"\u04b3\3\2\2\2\u04b7\u04b8\3\2\2\2\u04b8\u00d4\3\2\2\2\u04b9\u04bb\7a"+
		"\2\2\u04ba\u04b9\3\2\2\2\u04bb\u04bc\3\2\2\2\u04bc\u04ba\3\2\2\2\u04bc"+
		"\u04bd\3\2\2\2\u04bd\u00d6\3\2\2\2\u04be\u04bf\7a\2\2\u04bf\u00d8\3\2"+
		"\2\2\u04c0\u04c1\7\62\2\2\u04c1\u04c2\t\13\2\2\u04c2\u04c3\5\u00dbk\2"+
		"\u04c3\u00da\3\2\2\2\u04c4\u04cc\5\u00ddl\2\u04c5\u04c7\t\f\2\2\u04c6"+
		"\u04c5\3\2\2\2\u04c7\u04ca\3\2\2\2\u04c8\u04c6\3\2\2\2\u04c8\u04c9\3\2"+
		"\2\2\u04c9\u04cb\3\2\2\2\u04ca\u04c8\3\2\2\2\u04cb\u04cd\5\u00ddl\2\u04cc"+
		"\u04c8\3\2\2\2\u04cc\u04cd\3\2\2\2\u04cd\u00dc\3\2\2\2\u04ce\u04cf\t\r"+
		"\2\2\u04cf\u00de\3\2\2\2\u04d0\u04d4\7\62\2\2\u04d1\u04d3\7a\2\2\u04d2"+
		"\u04d1\3\2\2\2\u04d3\u04d6\3\2\2\2\u04d4\u04d2\3\2\2\2\u04d4\u04d5\3\2"+
		"\2\2\u04d5\u04d7\3\2\2\2\u04d6\u04d4\3\2\2\2\u04d7\u04d8\5\u00e1n\2\u04d8"+
		"\u00e0\3\2\2\2\u04d9\u04e1\t\16\2\2\u04da\u04dc\t\17\2\2\u04db\u04da\3"+
		"\2\2\2\u04dc\u04df\3\2\2\2\u04dd\u04db\3\2\2\2\u04dd\u04de\3\2\2\2\u04de"+
		"\u04e0\3\2\2\2\u04df\u04dd\3\2\2\2\u04e0\u04e2\t\16\2\2\u04e1\u04dd\3"+
		"\2\2\2\u04e1\u04e2\3\2\2\2\u04e2\u00e2\3\2\2\2\u04e3\u04e4\7\62\2\2\u04e4"+
		"\u04e5\t\20\2\2\u04e5\u04e6\5\u00e5p\2\u04e6\u00e4\3\2\2\2\u04e7\u04ef"+
		"\t\21\2\2\u04e8\u04ea\t\22\2\2\u04e9\u04e8\3\2\2\2\u04ea\u04ed\3\2\2\2"+
		"\u04eb\u04e9\3\2\2\2\u04eb\u04ec\3\2\2\2\u04ec\u04ee\3\2\2\2\u04ed\u04eb"+
		"\3\2\2\2\u04ee\u04f0\t\21\2\2\u04ef\u04eb\3\2\2\2\u04ef\u04f0\3\2\2\2"+
		"\u04f0\u00e6\3\2\2\2\u04f1\u04f4\5\u00e9r\2\u04f2\u04f4\5\u00efu\2\u04f3"+
		"\u04f1\3\2\2\2\u04f3\u04f2\3\2\2\2\u04f4\u04f8\3\2\2\2\u04f5\u04f6\5\u00d7"+
		"i\2\u04f6\u04f7\bq\25\2\u04f7\u04f9\3\2\2\2\u04f8\u04f5\3\2\2\2\u04f8"+
		"\u04f9\3\2\2\2\u04f9\u00e8\3\2\2\2\u04fa\u04fc\5\u00d3g\2\u04fb\u04fa"+
		"\3\2\2\2\u04fb\u04fc\3\2\2\2\u04fc\u04fd\3\2\2\2\u04fd\u04fe\7\60\2\2"+
		"\u04fe\u0500\5\u00d3g\2\u04ff\u0501\5\u00ebs\2\u0500\u04ff\3\2\2\2\u0500"+
		"\u0501\3\2\2\2\u0501\u0503\3\2\2\2\u0502\u0504\5\u00edt\2\u0503\u0502"+
		"\3\2\2\2\u0503\u0504\3\2\2\2\u0504\u050e\3\2\2\2\u0505\u0506\5\u00d3g"+
		"\2\u0506\u0508\5\u00ebs\2\u0507\u0509\5\u00edt\2\u0508\u0507\3\2\2\2\u0508"+
		"\u0509\3\2\2\2\u0509\u050e\3\2\2\2\u050a\u050b\5\u00d3g\2\u050b\u050c"+
		"\5\u00edt\2\u050c\u050e\3\2\2\2\u050d\u04fb\3\2\2\2\u050d\u0505\3\2\2"+
		"\2\u050d\u050a\3\2\2\2\u050e\u00ea\3\2\2\2\u050f\u0511\t\23\2\2\u0510"+
		"\u0512\t\24\2\2\u0511\u0510\3\2\2\2\u0511\u0512\3\2\2\2\u0512\u0513\3"+
		"\2\2\2\u0513\u0514\5\u00d3g\2\u0514\u00ec\3\2\2\2\u0515\u0516\t\25\2\2"+
		"\u0516\u00ee\3\2\2\2\u0517\u0518\5\u00f1v\2\u0518\u051a\5\u00f3w\2\u0519"+
		"\u051b\5\u00edt\2\u051a\u0519\3\2\2\2\u051a\u051b\3\2\2\2\u051b\u00f0"+
		"\3\2\2\2\u051c\u051e\5\u00d9j\2\u051d\u051f\7\60\2\2\u051e\u051d\3\2\2"+
		"\2\u051e\u051f\3\2\2\2\u051f\u0528\3\2\2\2\u0520\u0521\7\62\2\2\u0521"+
		"\u0523\t\13\2\2\u0522\u0524\5\u00dbk\2\u0523\u0522\3\2\2\2\u0523\u0524"+
		"\3\2\2\2\u0524\u0525\3\2\2\2\u0525\u0526\7\60\2\2\u0526\u0528\5\u00db"+
		"k\2\u0527\u051c\3\2\2\2\u0527\u0520\3\2\2\2\u0528\u00f2\3\2\2\2\u0529"+
		"\u052b\t\26\2\2\u052a\u052c\t\24\2\2\u052b\u052a\3\2\2\2\u052b\u052c\3"+
		"\2\2\2\u052c\u052d\3\2\2\2\u052d\u052e\5\u00d3g\2\u052e\u00f4\3\2\2\2"+
		"\u052f\u0530\7v\2\2\u0530\u0531\7t\2\2\u0531\u0532\7w\2\2\u0532\u0539"+
		"\7g\2\2\u0533\u0534\7h\2\2\u0534\u0535\7c\2\2\u0535\u0536\7n\2\2\u0536"+
		"\u0537\7u\2\2\u0537\u0539\7g\2\2\u0538\u052f\3\2\2\2\u0538\u0533\3\2\2"+
		"\2\u0539\u00f6\3\2\2\2\u053a\u053b\5\u0105\u0080\2\u053b\u053c\t\27\2"+
		"\2\u053c\u0542\3\2\2\2\u053d\u0542\5\u00f9z\2\u053e\u0542\5\u00fb{\2\u053f"+
		"\u0542\5\u00fd|\2\u0540\u0542\5\u00ff}\2\u0541\u053a\3\2\2\2\u0541\u053d"+
		"\3\2\2\2\u0541\u053e\3\2\2\2\u0541\u053f\3\2\2\2\u0541\u0540\3\2\2\2\u0542"+
		"\u00f8\3\2\2\2\u0543\u0544\5\u0105\u0080\2\u0544\u0545\t\16\2\2\u0545"+
		"\u0550\3\2\2\2\u0546\u0547\5\u0105\u0080\2\u0547\u0548\t\16\2\2\u0548"+
		"\u0549\t\16\2\2\u0549\u0550\3\2\2\2\u054a\u054b\5\u0105\u0080\2\u054b"+
		"\u054c\t\30\2\2\u054c\u054d\t\16\2\2\u054d\u054e\t\16\2\2\u054e\u0550"+
		"\3\2\2\2\u054f\u0543\3\2\2\2\u054f\u0546\3\2\2\2\u054f\u054a\3\2\2\2\u0550"+
		"\u00fa\3\2\2\2\u0551\u0552\5\u0105\u0080\2\u0552\u0553\7w\2\2\u0553\u0554"+
		"\5\u00ddl\2\u0554\u0555\5\u00ddl\2\u0555\u0556\5\u00ddl\2\u0556\u0557"+
		"\5\u00ddl\2\u0557\u00fc\3\2\2\2\u0558\u0559\5\u0105\u0080\2\u0559\u055a"+
		"\5\u0109\u0082\2\u055a\u00fe\3\2\2\2\u055b\u055c\5\u0105\u0080\2\u055c"+
		"\u055d\5\u0101~\2\u055d\u0100\3\2\2\2\u055e\u0560\7\17\2\2\u055f\u055e"+
		"\3\2\2\2\u055f\u0560\3\2\2\2\u0560\u0561\3\2\2\2\u0561\u0564\7\f\2\2\u0562"+
		"\u0564\7\17\2\2\u0563\u055f\3\2\2\2\u0563\u0562\3\2\2\2\u0564\u0102\3"+
		"\2\2\2\u0565\u0566\5\u0105\u0080\2\u0566\u0567\5\u0107\u0081\2\u0567\u0104"+
		"\3\2\2\2\u0568\u0569\7^\2\2\u0569\u0106\3\2\2\2\u056a\u056b\7\61\2\2\u056b"+
		"\u0108\3\2\2\2\u056c\u056d\7&\2\2\u056d\u010a\3\2\2\2\u056e\u056f\7$\2"+
		"\2\u056f\u010c\3\2\2\2\u0570\u0571\7)\2\2\u0571\u010e\3\2\2\2\u0572\u0573"+
		"\7$\2\2\u0573\u0574\7$\2\2\u0574\u0575\7$\2\2\u0575\u0110\3\2\2\2\u0576"+
		"\u0577\7)\2\2\u0577\u0578\7)\2\2\u0578\u0579\7)\2\2\u0579\u0112\3\2\2"+
		"\2\u057a\u057b\7&\2\2\u057b\u057c\7\61\2\2\u057c\u0114\3\2\2\2\u057d\u057e"+
		"\7\61\2\2\u057e\u057f\7&\2\2\u057f\u0116\3\2\2\2\u0580\u0581\7&\2\2\u0581"+
		"\u0582\7\61\2\2\u0582\u0118\3\2\2\2\u0583\u0584\7&\2\2\u0584\u0585\7&"+
		"\2\2\u0585\u011a\3\2\2\2\u0586\u0587\7&\2\2\u0587\u0588\7\61\2\2\u0588"+
		"\u0589\7&\2\2\u0589\u011c\3\2\2\2\u058a\u058b\7p\2\2\u058b\u058c\7w\2"+
		"\2\u058c\u058d\7n\2\2\u058d\u058e\7n\2\2\u058e\u011e\3\2\2\2\u058f\u0590"+
		"\7\60\2\2\u0590\u0591\7\60\2\2\u0591\u0120\3\2\2\2\u0592\u0593\7>\2\2"+
		"\u0593\u0594\7\60\2\2\u0594\u0595\7\60\2\2\u0595\u0122\3\2\2\2\u0596\u0597"+
		"\7\60\2\2\u0597\u0598\7\60\2\2\u0598\u0599\7>\2\2\u0599\u0124\3\2\2\2"+
		"\u059a\u059b\7>\2\2\u059b\u059c\7\60\2\2\u059c\u059d\7\60\2\2\u059d\u059e"+
		"\7>\2\2\u059e\u0126\3\2\2\2\u059f\u05a0\7,\2\2\u05a0\u05a1\7\60\2\2\u05a1"+
		"\u0128\3\2\2\2\u05a2\u05a3\7A\2\2\u05a3\u05a4\7\60\2\2\u05a4\u012a\3\2"+
		"\2\2\u05a5\u05a6\7A\2\2\u05a6\u05a7\7]\2\2\u05a7\u05a8\3\2\2\2\u05a8\u05a9"+
		"\b\u0093\26\2\u05a9\u05aa\3\2\2\2\u05aa\u05ab\b\u0093\17\2\u05ab\u012c"+
		"\3\2\2\2\u05ac\u05ad\7A\2\2\u05ad\u05ae\7A\2\2\u05ae\u05af\7\60\2\2\u05af"+
		"\u012e\3\2\2\2\u05b0\u05b1\7A\2\2\u05b1\u05b2\7<\2\2\u05b2\u0130\3\2\2"+
		"\2\u05b3\u05b4\7\60\2\2\u05b4\u05b5\7(\2\2\u05b5\u0132\3\2\2\2\u05b6\u05b7"+
		"\7<\2\2\u05b7\u05b8\7<\2\2\u05b8\u0134\3\2\2\2\u05b9\u05ba\7?\2\2\u05ba"+
		"\u05bb\7\u0080\2\2\u05bb\u0136\3\2\2\2\u05bc\u05bd\7?\2\2\u05bd\u05be"+
		"\7?\2\2\u05be\u05bf\7\u0080\2\2\u05bf\u0138\3\2\2\2\u05c0\u05c1\7,\2\2"+
		"\u05c1\u05c2\7,\2\2\u05c2\u013a\3\2\2\2\u05c3\u05c4\7,\2\2\u05c4\u05c5"+
		"\7,\2\2\u05c5\u05c6\7?\2\2\u05c6\u013c\3\2\2\2\u05c7\u05c8\7>\2\2\u05c8"+
		"\u05c9\7?\2\2\u05c9\u05ca\7@\2\2\u05ca\u013e\3\2\2\2\u05cb\u05cc\7?\2"+
		"\2\u05cc\u05cd\7?\2\2\u05cd\u05ce\7?\2\2\u05ce\u0140\3\2\2\2\u05cf\u05d0"+
		"\7?\2\2\u05d0\u05d1\7?\2\2\u05d1\u05d2\7@\2\2\u05d2\u0142\3\2\2\2\u05d3"+
		"\u05d4\7#\2\2\u05d4\u05d5\7?\2\2\u05d5\u05d6\7?\2\2\u05d6\u0144\3\2\2"+
		"\2\u05d7\u05d8\7/\2\2\u05d8\u05d9\7@\2\2\u05d9\u0146\3\2\2\2\u05da\u05db"+
		"\7#\2\2\u05db\u05dc\7k\2\2\u05dc\u05dd\7p\2\2\u05dd\u05de\7u\2\2\u05de"+
		"\u05df\7v\2\2\u05df\u05e0\7c\2\2\u05e0\u05e1\7p\2\2\u05e1\u05e2\7e\2\2"+
		"\u05e2\u05e3\7g\2\2\u05e3\u05e4\7q\2\2\u05e4\u05e5\7h\2\2\u05e5\u05e6"+
		"\3\2\2\2\u05e6\u05e7\6\u00a1\21\2\u05e7\u0148\3\2\2\2\u05e8\u05e9\7#\2"+
		"\2\u05e9\u05ea\7k\2\2\u05ea\u05eb\7p\2\2\u05eb\u05ec\3\2\2\2\u05ec\u05ed"+
		"\6\u00a2\22\2\u05ed\u014a\3\2\2\2\u05ee\u05ef\7*\2\2\u05ef\u05f0\b\u00a3"+
		"\27\2\u05f0\u05f1\3\2\2\2\u05f1\u05f2\b\u00a3\17\2\u05f2\u014c\3\2\2\2"+
		"\u05f3\u05f4\7+\2\2\u05f4\u05f5\b\u00a4\30\2\u05f5\u05f6\3\2\2\2\u05f6"+
		"\u05f7\b\u00a4\t\2\u05f7\u014e\3\2\2\2\u05f8\u05f9\7}\2\2\u05f9\u05fa"+
		"\b\u00a5\31\2\u05fa\u05fb\3\2\2\2\u05fb\u05fc\b\u00a5\17\2\u05fc\u0150"+
		"\3\2\2\2\u05fd\u05fe\7\177\2\2\u05fe\u05ff\b\u00a6\32\2\u05ff\u0600\3"+
		"\2\2\2\u0600\u0601\b\u00a6\t\2\u0601\u0152\3\2\2\2\u0602\u0603\7]\2\2"+
		"\u0603\u0604\b\u00a7\33\2\u0604\u0605\3\2\2\2\u0605\u0606\b\u00a7\17\2"+
		"\u0606\u0154\3\2\2\2\u0607\u0608\7_\2\2\u0608\u0609\b\u00a8\34\2\u0609"+
		"\u060a\3\2\2\2\u060a\u060b\b\u00a8\t\2\u060b\u0156\3\2\2\2\u060c\u060d"+
		"\7=\2\2\u060d\u0158\3\2\2\2\u060e\u060f\7.\2\2\u060f\u015a\3\2\2\2\u0610"+
		"\u0611\7\60\2\2\u0611\u015c\3\2\2\2\u0612\u0613\7?\2\2\u0613\u015e\3\2"+
		"\2\2\u0614\u0615\7@\2\2\u0615\u0160\3\2\2\2\u0616\u0617\7>\2\2\u0617\u0162"+
		"\3\2\2\2\u0618\u0619\7#\2\2\u0619\u0164\3\2\2\2\u061a\u061b\7\u0080\2"+
		"\2\u061b\u0166\3\2\2\2\u061c\u061d\7A\2\2\u061d\u0168\3\2\2\2\u061e\u061f"+
		"\7<\2\2\u061f\u016a\3\2\2\2\u0620\u0621\7?\2\2\u0621\u0622\7?\2\2\u0622"+
		"\u016c\3\2\2\2\u0623\u0624\7>\2\2\u0624\u0625\7?\2\2\u0625\u016e\3\2\2"+
		"\2\u0626\u0627\7@\2\2\u0627\u0628\7?\2\2\u0628\u0170\3\2\2\2\u0629\u062a"+
		"\7#\2\2\u062a\u062b\7?\2\2\u062b\u0172\3\2\2\2\u062c\u062d\7(\2\2\u062d"+
		"\u062e\7(\2\2\u062e\u0174\3\2\2\2\u062f\u0630\7~\2\2\u0630\u0631\7~\2"+
		"\2\u0631\u0176\3\2\2\2\u0632\u0633\7-\2\2\u0633\u0634\7-\2\2\u0634\u0178"+
		"\3\2\2\2\u0635\u0636\7/\2\2\u0636\u0637\7/\2\2\u0637\u017a\3\2\2\2\u0638"+
		"\u0639\7-\2\2\u0639\u017c\3\2\2\2\u063a\u063b\7/\2\2\u063b\u017e\3\2\2"+
		"\2\u063c\u063d\7,\2\2\u063d\u0180\3\2\2\2\u063e\u063f\5\u0107\u0081\2"+
		"\u063f\u0182\3\2\2\2\u0640\u0641\7(\2\2\u0641\u0184\3\2\2\2\u0642\u0643"+
		"\7~\2\2\u0643\u0186\3\2\2\2\u0644\u0645\7`\2\2\u0645\u0188\3\2\2\2\u0646"+
		"\u0647\7\'\2\2\u0647\u018a\3\2\2\2\u0648\u0649\7-\2\2\u0649\u064a\7?\2"+
		"\2\u064a\u018c\3\2\2\2\u064b\u064c\7/\2\2\u064c\u064d\7?\2\2\u064d\u018e"+
		"\3\2\2\2\u064e\u064f\7,\2\2\u064f\u0650\7?\2\2\u0650\u0190\3\2\2\2\u0651"+
		"\u0652\7\61\2\2\u0652\u0653\7?\2\2\u0653\u0192\3\2\2\2\u0654\u0655\7("+
		"\2\2\u0655\u0656\7?\2\2\u0656\u0194\3\2\2\2\u0657\u0658\7~\2\2\u0658\u0659"+
		"\7?\2\2\u0659\u0196\3\2\2\2\u065a\u065b\7`\2\2\u065b\u065c\7?\2\2\u065c"+
		"\u0198\3\2\2\2\u065d\u065e\7\'\2\2\u065e\u065f\7?\2\2\u065f\u019a\3\2"+
		"\2\2\u0660\u0661\7>\2\2\u0661\u0662\7>\2\2\u0662\u0663\7?\2\2\u0663\u019c"+
		"\3\2\2\2\u0664\u0665\7@\2\2\u0665\u0666\7@\2\2\u0666\u0667\7?\2\2\u0667"+
		"\u019e\3\2\2\2\u0668\u0669\7@\2\2\u0669\u066a\7@\2\2\u066a\u066b\7@\2"+
		"\2\u066b\u066c\7?\2\2\u066c\u01a0\3\2\2\2\u066d\u066e\7A\2\2\u066e\u066f"+
		"\7?\2\2\u066f\u01a2\3\2\2\2\u0670\u0674\t\31\2\2\u0671\u0673\5\u01ad\u00d4"+
		"\2\u0672\u0671\3\2\2\2\u0673\u0676\3\2\2\2\u0674\u0672\3\2\2\2\u0674\u0675"+
		"\3\2\2\2\u0675\u0689\3\2\2\2\u0676\u0674\3\2\2\2\u0677\u0678\n\32\2\2"+
		"\u0678\u067c\6\u00cf\23\2\u0679\u067b\5\u01ad\u00d4\2\u067a\u0679\3\2"+
		"\2\2\u067b\u067e\3\2\2\2\u067c\u067a\3\2\2\2\u067c\u067d\3\2\2\2\u067d"+
		"\u0689\3\2\2\2\u067e\u067c\3\2\2\2\u067f\u0680\t\33\2\2\u0680\u0681\t"+
		"\34\2\2\u0681\u0685\6\u00cf\24\2\u0682\u0684\5\u01ad\u00d4\2\u0683\u0682"+
		"\3\2\2\2\u0684\u0687\3\2\2\2\u0685\u0683\3\2\2\2\u0685\u0686\3\2\2\2\u0686"+
		"\u0689\3\2\2\2\u0687\u0685\3\2\2\2\u0688\u0670\3\2\2\2\u0688\u0677\3\2"+
		"\2\2\u0688\u067f\3\2\2\2\u0689\u01a4\3\2\2\2\u068a\u068e\t\35\2\2\u068b"+
		"\u068d\5\u01ad\u00d4\2\u068c\u068b\3\2\2\2\u068d\u0690\3\2\2\2\u068e\u068c"+
		"\3\2\2\2\u068e\u068f\3\2\2\2\u068f\u06a3\3\2\2\2\u0690\u068e\3\2\2\2\u0691"+
		"\u0692\n\32\2\2\u0692\u0696\6\u00d0\25\2\u0693\u0695\5\u01ad\u00d4\2\u0694"+
		"\u0693\3\2\2\2\u0695\u0698\3\2\2\2\u0696\u0694\3\2\2\2\u0696\u0697\3\2"+
		"\2\2\u0697\u06a3\3\2\2\2\u0698\u0696\3\2\2\2\u0699\u069a\t\33\2\2\u069a"+
		"\u069b\t\34\2\2\u069b\u069f\6\u00d0\26\2\u069c\u069e\5\u01ad\u00d4\2\u069d"+
		"\u069c\3\2\2\2\u069e\u06a1\3\2\2\2\u069f\u069d\3\2\2\2\u069f\u06a0\3\2"+
		"\2\2\u06a0\u06a3\3\2\2\2\u06a1\u069f\3\2\2\2\u06a2\u068a\3\2\2\2\u06a2"+
		"\u0691\3\2\2\2\u06a2\u0699\3\2\2\2\u06a3\u01a6\3\2\2\2\u06a4\u06a8\5\u01ab"+
		"\u00d3\2\u06a5\u06a7\5\u01af\u00d5\2\u06a6\u06a5\3\2\2\2\u06a7\u06aa\3"+
		"\2\2\2\u06a8\u06a6\3\2\2\2\u06a8\u06a9\3\2\2\2\u06a9\u01a8\3\2\2\2\u06aa"+
		"\u06a8\3\2\2\2\u06ab\u06b2\t\36\2\2\u06ac\u06ad\n\32\2\2\u06ad\u06b2\6"+
		"\u00d2\27\2\u06ae\u06af\t\33\2\2\u06af\u06b0\t\34\2\2\u06b0\u06b2\6\u00d2"+
		"\30\2\u06b1\u06ab\3\2\2\2\u06b1\u06ac\3\2\2\2\u06b1\u06ae\3\2\2\2\u06b2"+
		"\u01aa\3\2\2\2\u06b3\u06b4\5\u01a9\u00d2\2\u06b4\u06b5\6\u00d3\31\2\u06b5"+
		"\u01ac\3\2\2\2\u06b6\u06bd\t\37\2\2\u06b7\u06b8\n\32\2\2\u06b8\u06bd\6"+
		"\u00d4\32\2\u06b9\u06ba\t\33\2\2\u06ba\u06bb\t\34\2\2\u06bb\u06bd\6\u00d4"+
		"\33\2\u06bc\u06b6\3\2\2\2\u06bc\u06b7\3\2\2\2\u06bc\u06b9\3\2\2\2\u06bd"+
		"\u01ae\3\2\2\2\u06be\u06bf\5\u01ad\u00d4\2\u06bf\u06c0\6\u00d5\34\2\u06c0"+
		"\u01b0\3\2\2\2\u06c1\u06c3\n \2\2\u06c2\u06c1\3\2\2\2\u06c3\u06c6\3\2"+
		"\2\2\u06c4\u06c2\3\2\2\2\u06c4\u06c5\3\2\2\2\u06c5\u01b2\3\2\2\2\u06c6"+
		"\u06c4\3\2\2\2\u06c7\u06c8\7B\2\2\u06c8\u01b4\3\2\2\2\u06c9\u06ca\7\60"+
		"\2\2\u06ca\u06cb\7\60\2\2\u06cb\u06cc\7\60\2\2\u06cc\u01b6\3\2\2\2\u06cd"+
		"\u06cf\t!\2\2\u06ce\u06cd\3\2\2\2\u06cf\u06d0\3\2\2\2\u06d0\u06ce\3\2"+
		"\2\2\u06d0\u06d1\3\2\2\2\u06d1\u06d8\3\2\2\2\u06d2\u06d4\5\u00ff}\2\u06d3"+
		"\u06d2\3\2\2\2\u06d4\u06d5\3\2\2\2\u06d5\u06d3\3\2\2\2\u06d5\u06d6\3\2"+
		"\2\2\u06d6\u06d8\3\2\2\2\u06d7\u06ce\3\2\2\2\u06d7\u06d3\3\2\2\2\u06d8"+
		"\u06d9\3\2\2\2\u06d9\u06da\b\u00d9\35\2\u06da\u01b8\3\2\2\2\u06db\u06dc"+
		"\5\u0101~\2\u06dc\u06dd\b\u00da\36\2\u06dd\u01ba\3\2\2\2\u06de\u06df\7"+
		"\61\2\2\u06df\u06e0\7,\2\2\u06e0\u06e4\3\2\2\2\u06e1\u06e3\13\2\2\2\u06e2"+
		"\u06e1\3\2\2\2\u06e3\u06e6\3\2\2\2\u06e4\u06e5\3\2\2\2\u06e4\u06e2\3\2"+
		"\2\2\u06e5\u06eb\3\2\2\2\u06e6\u06e4\3\2\2\2\u06e7\u06e8\7,\2\2\u06e8"+
		"\u06ec\7\61\2\2\u06e9\u06ea\7\2\2\3\u06ea\u06ec\b\u00db\37\2\u06eb\u06e7"+
		"\3\2\2\2\u06eb\u06e9\3\2\2\2\u06ec\u06ed\3\2\2\2\u06ed\u06ee\b\u00db "+
		"\2\u06ee\u06ef\3\2\2\2\u06ef\u06f0\b\u00db!\2\u06f0\u01bc\3\2\2\2\u06f1"+
		"\u06f2\7\61\2\2\u06f2\u06f3\7\61\2\2\u06f3\u06f7\3\2\2\2\u06f4\u06f6\n"+
		" \2\2\u06f5\u06f4\3\2\2\2\u06f6\u06f9\3\2\2\2\u06f7\u06f5\3\2\2\2\u06f7"+
		"\u06f8\3\2\2\2\u06f8\u06fa\3\2\2\2\u06f9\u06f7\3\2\2\2\u06fa\u06fb\b\u00dc"+
		"\"\2\u06fb\u06fc\3\2\2\2\u06fc\u06fd\b\u00dc!\2\u06fd\u01be\3\2\2\2\u06fe"+
		"\u06ff\7%\2\2\u06ff\u0700\7#\2\2\u0700\u0701\3\2\2\2\u0701\u0702\b\u00dd"+
		"#\2\u0702\u070b\5\u01b1\u00d6\2\u0703\u0704\5\u0101~\2\u0704\u0705\7%"+
		"\2\2\u0705\u0706\7#\2\2\u0706\u0707\3\2\2\2\u0707\u0708\5\u01b1\u00d6"+
		"\2\u0708\u070a\3\2\2\2\u0709\u0703\3\2\2\2\u070a\u070d\3\2\2\2\u070b\u0709"+
		"\3\2\2\2\u070b\u070c\3\2\2\2\u070c\u070e\3\2\2\2\u070d\u070b\3\2\2\2\u070e"+
		"\u070f\b\u00dd\35\2\u070f\u01c0\3\2\2\2\u0710\u0711\13\2\2\2\u0711\u0712"+
		"\b\u00de$\2\u0712\u01c2\3\2\2\2Y\2\3\4\5\6\7\b\u01c7\u01d0\u01da\u01e2"+
		"\u01e7\u01ed\u01f6\u01ff\u0203\u0209\u0215\u0223\u0231\u0256\u028d\u0291"+
		"\u0298\u029f\u02a6\u02b6\u02ed\u047e\u0483\u0489\u048d\u048f\u0493\u0497"+
		"\u049b\u049f\u04a6\u04ab\u04ad\u04b3\u04b7\u04bc\u04c8\u04cc\u04d4\u04dd"+
		"\u04e1\u04eb\u04ef\u04f3\u04f8\u04fb\u0500\u0503\u0508\u050d\u0511\u051a"+
		"\u051e\u0523\u0527\u052b\u0538\u0541\u054f\u055f\u0563\u0674\u067c\u0685"+
		"\u0688\u068e\u0696\u069f\u06a2\u06a8\u06b1\u06bc\u06c4\u06d0\u06d5\u06d7"+
		"\u06e4\u06eb\u06f7\u070b%\3\2\2\7\3\2\7\7\2\t\4\2\7\4\2\7\5\2\7\6\2\6"+
		"\2\2\5\2\2\t\5\2\t\6\2\3\23\3\t`\2\7\2\2\t\u008b\2\7\b\2\3\26\4\3`\5\3"+
		"`\6\3q\7\3\u0093\b\3\u00a3\t\3\u00a4\n\3\u00a5\13\3\u00a6\f\3\u00a7\r"+
		"\3\u00a8\16\b\2\2\3\u00da\17\3\u00db\20\3\u00db\21\t\u008f\2\3\u00dc\22"+
		"\3\u00dd\23\3\u00de\24";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN);
	static {
	}
}