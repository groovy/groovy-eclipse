// $ANTLR 2.7.7 (20060906): "groovy.g" -> "GroovyLexer.java"$

package org.codehaus.groovy.antlr.parser;
import org.codehaus.groovy.antlr.*;
import java.util.*;
import java.io.InputStream;
import java.io.Reader;
import groovyjarjarantlr.InputBuffer;
import groovyjarjarantlr.LexerSharedInputState;
import groovyjarjarantlr.CommonToken;
import org.codehaus.groovy.GroovyBugError;
import groovyjarjarantlr.TokenStreamRecognitionException;
import org.codehaus.groovy.ast.Comment;

import java.io.InputStream;
import groovyjarjarantlr.TokenStreamException;
import groovyjarjarantlr.TokenStreamIOException;
import groovyjarjarantlr.TokenStreamRecognitionException;
import groovyjarjarantlr.CharStreamException;
import groovyjarjarantlr.CharStreamIOException;
import groovyjarjarantlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import groovyjarjarantlr.CharScanner;
import groovyjarjarantlr.InputBuffer;
import groovyjarjarantlr.ByteBuffer;
import groovyjarjarantlr.CharBuffer;
import groovyjarjarantlr.Token;
import groovyjarjarantlr.CommonToken;
import groovyjarjarantlr.RecognitionException;
import groovyjarjarantlr.NoViableAltForCharException;
import groovyjarjarantlr.MismatchedCharException;
import groovyjarjarantlr.TokenStream;
import groovyjarjarantlr.ANTLRHashString;
import groovyjarjarantlr.LexerSharedInputState;
import groovyjarjarantlr.collections.impl.BitSet;
import groovyjarjarantlr.SemanticException;

public class GroovyLexer extends groovyjarjarantlr.CharScanner implements GroovyTokenTypes, TokenStream
 {

    /** flag for enabling the "assert" keyword */
    private boolean assertEnabled = true;
    /** flag for enabling the "enum" keyword */
    private boolean enumEnabled = true;
    /** flag for including whitespace tokens (for IDE preparsing) */
    private boolean whitespaceIncluded = false;

    /** Enable the "assert" keyword */
    public void enableAssert(boolean shouldEnable) { assertEnabled = shouldEnable; }
    /** Query the "assert" keyword state */
    public boolean isAssertEnabled() { return assertEnabled; }
    /** Enable the "enum" keyword */
    public void enableEnum(boolean shouldEnable) { enumEnabled = shouldEnable; }
    /** Query the "enum" keyword state */
    public boolean isEnumEnabled() { return enumEnabled; }

    /** Include whitespace tokens.  Note that this breaks the parser.   */
    public void setWhitespaceIncluded(boolean z) { whitespaceIncluded = z; }
    /** Are whitespace tokens included? */
    public boolean isWhitespaceIncluded() { return whitespaceIncluded; }

    {
        // Initialization actions performed on construction.
        setTabSize(1);  // get rid of special tab interpretation, for IDEs and general clarity
    }

    /** Bumped when inside '[x]' or '(x)', reset inside '{x}'.  See ONE_NL.  */
    protected int parenLevel = 0;
    protected int suppressNewline = 0;  // be really mean to newlines inside strings
    protected static final int SCS_TYPE = 3, SCS_VAL = 4, SCS_LIT = 8, SCS_LIMIT = 16;
    protected static final int SCS_SQ_TYPE = 0, SCS_TQ_TYPE = 1, SCS_RE_TYPE = 2, SCS_DRE_TYPE = 3;
    protected int stringCtorState = 0;  // hack string and regexp constructor boundaries
    /** Push parenLevel here and reset whenever inside '{x}'. */
    protected ArrayList parenLevelStack = new ArrayList();
    protected int lastSigTokenType = EOF;  // last returned non-whitespace token

    public void setTokenObjectClass(String name) {/*ignore*/}

    protected Token makeToken(int t) {
        GroovySourceToken tok = new GroovySourceToken(t);
        tok.setColumn(inputState.getTokenStartColumn());
        tok.setLine(inputState.getTokenStartLine());
        tok.setColumnLast(inputState.getColumn());
        tok.setLineLast(inputState.getLine());
        return tok;
    }

    protected void pushParenLevel() {
        parenLevelStack.add(Integer.valueOf(parenLevel*SCS_LIMIT + stringCtorState));
        parenLevel = 0;
        stringCtorState = 0;
    }

    protected void popParenLevel() {
        int npl = parenLevelStack.size();
        if (npl == 0)  return;
        int i = ((Integer) parenLevelStack.remove(--npl)).intValue();
        parenLevel      = i / SCS_LIMIT;
        stringCtorState = i % SCS_LIMIT;
    }

    protected void restartStringCtor(boolean expectLiteral) {
        if (stringCtorState != 0) {
            stringCtorState = (expectLiteral? SCS_LIT: SCS_VAL) + (stringCtorState & SCS_TYPE);
        }
    }

    protected boolean allowRegexpLiteral() {
        return !isExpressionEndingToken(lastSigTokenType);
    }

    /** Return true for an operator or punctuation which can end an expression.
     *  Return true for keywords, identifiers, and literals.
     *  Return true for tokens which can end expressions (right brackets, ++, --).
     *  Return false for EOF and all other operator and punctuation tokens.
     *  Used to suppress the recognition of /foo/ as opposed to the simple division operator '/'.
     */
    // Cf. 'constant' and 'balancedBrackets' rules in the grammar.)
    protected static boolean isExpressionEndingToken(int ttype) {
        switch (ttype) {
        case INC:               // x++ / y
        case DEC:               // x-- / y
        case RPAREN:            // (x) / y
        case RBRACK:            // f[x] / y
        case RCURLY:            // f{x} / y
        case STRING_LITERAL:    // "x" / y
        case STRING_CTOR_END:   // "$x" / y
        case NUM_INT:           // 0 / y
        case NUM_FLOAT:         // 0f / y
        case NUM_LONG:          // 0l / y
        case NUM_DOUBLE:        // 0.0 / y
        case NUM_BIG_INT:       // 0g / y
        case NUM_BIG_DECIMAL:   // 0.0g / y
        case IDENT:             // x / y
        // and a bunch of keywords (all of them; no sense picking and choosing):
        case LITERAL_as:
        case LITERAL_assert:
        case LITERAL_boolean:
        case LITERAL_break:
        case LITERAL_byte:
        case LITERAL_case:
        case LITERAL_catch:
        case LITERAL_char:
        case LITERAL_class:
        case LITERAL_continue:
        case LITERAL_def:
        case LITERAL_default:
        case LITERAL_double:
        case LITERAL_else:
        case LITERAL_enum:
        case LITERAL_extends:
        case LITERAL_false:
        case LITERAL_finally:
        case LITERAL_float:
        case LITERAL_for:
        case LITERAL_if:
        case LITERAL_implements:
        case LITERAL_import:
        case LITERAL_in:
        case LITERAL_instanceof:
        case LITERAL_int:
        case LITERAL_interface:
        case LITERAL_long:
        case LITERAL_native:
        case LITERAL_new:
        case LITERAL_null:
        case LITERAL_package:
        case LITERAL_private:
        case LITERAL_protected:
        case LITERAL_public:
        case LITERAL_return:
        case LITERAL_short:
        case LITERAL_static:
        case LITERAL_super:
        case LITERAL_switch:
        case LITERAL_synchronized:
        case LITERAL_this:
        case LITERAL_threadsafe:
        case LITERAL_throw:
        case LITERAL_throws:
        case LITERAL_transient:
        case LITERAL_true:
        case LITERAL_try:
        case LITERAL_void:
        case LITERAL_volatile:
        case LITERAL_while:
            return true;
        default:
            return false;
        }
    }

    protected void newlineCheck(boolean check) throws RecognitionException {
        if (check && suppressNewline > 0) {
            require(suppressNewline == 0,
                "end of line reached within a simple string 'x' or \"x\" or /x/",
                "for multi-line literals, use triple quotes '''x''' or \"\"\"x\"\"\" or /x/ or $/x/$");
            suppressNewline = 0;  // shut down any flood of errors
        }
        newline();
    }

    protected boolean atValidDollarEscape() throws CharStreamException {
        // '$' (('*')? ('{' | LETTER)) =>
        int k = 1;
        char lc = LA(k++);
        if (lc != '$')  return false;
        lc = LA(k++);
        if (lc == '*')  lc = LA(k++);
        return (lc == '{' || (lc != '$' && Character.isJavaIdentifierStart(lc)));
    }

    protected boolean atDollarDollarEscape() throws CharStreamException {
        return LA(1) == '$' && LA(2) == '$';
    }

    protected boolean atDollarSlashEscape() throws CharStreamException {
        return LA(1) == '$' && LA(2) == '/';
    }

    /** This is a bit of plumbing which resumes collection of string constructor bodies,
     *  after an embedded expression has been parsed.
     *  Usage:  new GroovyRecognizer(new GroovyLexer(in).plumb()).
     */
    public TokenStream plumb() {
        return new TokenStream() {
            public Token nextToken() throws TokenStreamException {
                if (stringCtorState >= SCS_LIT) {
                    // This goo is modeled upon the ANTLR code for nextToken:
                    int quoteType = (stringCtorState & SCS_TYPE);
                    stringCtorState = 0;  // get out of this mode, now
                    resetText();
                    try {
                        switch (quoteType) {
                        case SCS_SQ_TYPE:
                            mSTRING_CTOR_END(true, /*fromStart:*/false, false); break;
                        case SCS_TQ_TYPE:
                            mSTRING_CTOR_END(true, /*fromStart:*/false, true); break;
                        case SCS_RE_TYPE:
                            mREGEXP_CTOR_END(true, /*fromStart:*/false); break;
                        case SCS_DRE_TYPE:
                            mDOLLAR_REGEXP_CTOR_END(true, /*fromStart:*/false); break;
                        default:  throw new AssertionError(false);
                        }
                        lastSigTokenType = _returnToken.getType();
                        return _returnToken;
                    } catch (RecognitionException e) {
                        throw new TokenStreamRecognitionException(e);
                    } catch (CharStreamException cse) {
                        if ( cse instanceof CharStreamIOException ) {
                            throw new TokenStreamIOException(((CharStreamIOException)cse).io);
                        }
                        else {
                            throw new TokenStreamException(cse.getMessage());
                        }
                    }
                }
                Token token = GroovyLexer.this.nextToken();
                int lasttype = token.getType();
                if (whitespaceIncluded) {
                    switch (lasttype) {  // filter out insignificant types
                    case WS:
                    case ONE_NL:
                    case SL_COMMENT:
                    case ML_COMMENT:
                        lasttype = lastSigTokenType;  // back up!
                    }
                }
                lastSigTokenType = lasttype;
                return token;
            }
        };
    }

        // stuff to adjust ANTLR's tracing machinery
    public static boolean tracing = false;  // only effective if antlr.Tool is run with -traceLexer
    public void traceIn(String rname) throws CharStreamException {
        if (!GroovyLexer.tracing)  return;
        super.traceIn(rname);
    }
    public void traceOut(String rname) throws CharStreamException {
        if (!GroovyLexer.tracing)  return;
        if (_returnToken != null)  rname += tokenStringOf(_returnToken);
        super.traceOut(rname);
    }
    private static java.util.HashMap ttypes;
    private static String tokenStringOf(Token t) {
        if (ttypes == null) {
            java.util.HashMap map = new java.util.HashMap();
            java.lang.reflect.Field[] fields = GroovyTokenTypes.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getType() != int.class)  continue;
                try {
                    map.put(fields[i].get(null), fields[i].getName());
                } catch (IllegalAccessException ee) {
                }
            }
            ttypes = map;
        }
        Integer tt = Integer.valueOf(t.getType());
        Object ttn = ttypes.get(tt);
        if (ttn == null)  ttn = "<"+tt+">";
        return "["+ttn+",\""+t.getText()+"\"]";
    }

    protected GroovyRecognizer parser;  // little-used link; TODO: get rid of
    private void require(boolean z, String problem, String solution) throws SemanticException {
        // TODO: Direct to a common error handler, rather than through the parser.
        if (!z)  parser.requireFailed(problem, solution);
    }
public GroovyLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public GroovyLexer(Reader in) {
	this(new CharBuffer(in));
}
public GroovyLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public GroovyLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("byte", this), new Integer(104));
	literals.put(new ANTLRHashString("public", this), new Integer(114));
	literals.put(new ANTLRHashString("case", this), new Integer(148));
	literals.put(new ANTLRHashString("short", this), new Integer(106));
	literals.put(new ANTLRHashString("break", this), new Integer(142));
	literals.put(new ANTLRHashString("while", this), new Integer(137));
	literals.put(new ANTLRHashString("new", this), new Integer(157));
	literals.put(new ANTLRHashString("instanceof", this), new Integer(156));
	literals.put(new ANTLRHashString("implements", this), new Integer(129));
	literals.put(new ANTLRHashString("synchronized", this), new Integer(119));
	literals.put(new ANTLRHashString("const", this), new Integer(40));
	literals.put(new ANTLRHashString("float", this), new Integer(108));
	literals.put(new ANTLRHashString("package", this), new Integer(80));
	literals.put(new ANTLRHashString("return", this), new Integer(141));
	literals.put(new ANTLRHashString("throw", this), new Integer(144));
	literals.put(new ANTLRHashString("null", this), new Integer(158));
	literals.put(new ANTLRHashString("def", this), new Integer(83));
	literals.put(new ANTLRHashString("threadsafe", this), new Integer(118));
	literals.put(new ANTLRHashString("protected", this), new Integer(115));
	literals.put(new ANTLRHashString("class", this), new Integer(91));
	literals.put(new ANTLRHashString("throws", this), new Integer(128));
	literals.put(new ANTLRHashString("do", this), new Integer(41));
	literals.put(new ANTLRHashString("strictfp", this), new Integer(42));
	literals.put(new ANTLRHashString("super", this), new Integer(97));
	literals.put(new ANTLRHashString("transient", this), new Integer(116));
	literals.put(new ANTLRHashString("native", this), new Integer(117));
	literals.put(new ANTLRHashString("interface", this), new Integer(92));
	literals.put(new ANTLRHashString("final", this), new Integer(37));
	literals.put(new ANTLRHashString("if", this), new Integer(135));
	literals.put(new ANTLRHashString("double", this), new Integer(110));
	literals.put(new ANTLRHashString("volatile", this), new Integer(120));
	literals.put(new ANTLRHashString("as", this), new Integer(112));
	literals.put(new ANTLRHashString("assert", this), new Integer(145));
	literals.put(new ANTLRHashString("catch", this), new Integer(151));
	literals.put(new ANTLRHashString("try", this), new Integer(149));
	literals.put(new ANTLRHashString("goto", this), new Integer(39));
	literals.put(new ANTLRHashString("enum", this), new Integer(93));
	literals.put(new ANTLRHashString("int", this), new Integer(107));
	literals.put(new ANTLRHashString("for", this), new Integer(139));
	literals.put(new ANTLRHashString("extends", this), new Integer(96));
	literals.put(new ANTLRHashString("boolean", this), new Integer(103));
	literals.put(new ANTLRHashString("char", this), new Integer(105));
	literals.put(new ANTLRHashString("private", this), new Integer(113));
	literals.put(new ANTLRHashString("default", this), new Integer(127));
	literals.put(new ANTLRHashString("false", this), new Integer(155));
	literals.put(new ANTLRHashString("this", this), new Integer(130));
	literals.put(new ANTLRHashString("static", this), new Integer(82));
	literals.put(new ANTLRHashString("abstract", this), new Integer(38));
	literals.put(new ANTLRHashString("continue", this), new Integer(143));
	literals.put(new ANTLRHashString("finally", this), new Integer(150));
	literals.put(new ANTLRHashString("else", this), new Integer(136));
	literals.put(new ANTLRHashString("import", this), new Integer(81));
	literals.put(new ANTLRHashString("in", this), new Integer(140));
	literals.put(new ANTLRHashString("void", this), new Integer(102));
	literals.put(new ANTLRHashString("switch", this), new Integer(138));
	literals.put(new ANTLRHashString("true", this), new Integer(159));
	literals.put(new ANTLRHashString("long", this), new Integer(109));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '(':
				{
					mLPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mRPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '[':
				{
					mLBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case ']':
				{
					mRBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case '{':
				{
					mLCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case '}':
				{
					mRCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case ':':
				{
					mCOLON(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOMMA(true);
					theRetToken=_returnToken;
					break;
				}
				case '~':
				{
					mBNOT(true);
					theRetToken=_returnToken;
					break;
				}
				case ';':
				{
					mSEMI(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\u000c':  case ' ':  case '\\':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				case '\n':  case '\r':
				{
					mNLS(true);
					theRetToken=_returnToken;
					break;
				}
				case '"':  case '\'':
				{
					mSTRING_LITERAL(true);
					theRetToken=_returnToken;
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					mNUM_INT(true);
					theRetToken=_returnToken;
					break;
				}
				case '@':
				{
					mAT(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='>') && (LA(4)=='=')) {
						mBSR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='=') && (LA(3)=='>')) {
						mCOMPARE_TO(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='=') && (LA(3)=='=')) {
						mIDENTICAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (LA(2)=='=') && (LA(3)=='=')) {
						mNOT_IDENTICAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='=')) {
						mSR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='>') && (true)) {
						mBSR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='<') && (LA(3)=='=')) {
						mSL_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='.') && (LA(2)=='.') && (LA(3)=='<')) {
						mRANGE_EXCLUSIVE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='.') && (LA(2)=='.') && (LA(3)=='.')) {
						mTRIPLE_DOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='=') && (LA(3)=='~')) {
						mREGEX_MATCH(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='*') && (LA(3)=='=')) {
						mSTAR_STAR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='=') && (true)) {
						mEQUAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (LA(2)=='=') && (true)) {
						mNOT_EQUAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (LA(2)=='=')) {
						mPLUS_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (LA(2)=='+')) {
						mINC(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='=')) {
						mMINUS_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='-')) {
						mDEC(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='=')) {
						mSTAR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (LA(2)=='=')) {
						mMOD_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (true)) {
						mSR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='=')) {
						mGE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='<') && (true)) {
						mSL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='=') && (true)) {
						mLE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (LA(2)=='=')) {
						mBXOR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (LA(2)=='=')) {
						mBOR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (LA(2)=='|')) {
						mLOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (LA(2)=='=')) {
						mBAND_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (LA(2)=='&')) {
						mLAND(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='.') && (LA(2)=='.') && (true)) {
						mRANGE_INCLUSIVE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='.')) {
						mSPREAD_DOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='?') && (LA(2)=='.')) {
						mOPTIONAL_DOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='?') && (LA(2)==':')) {
						mELVIS_OPERATOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='.') && (LA(2)=='&')) {
						mMEMBER_POINTER(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='~')) {
						mREGEX_FIND(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='*') && (true)) {
						mSTAR_STAR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='>')) {
						mCLOSABLE_BLOCK_OP(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='/')) {
						mSL_COMMENT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='*')) {
						mML_COMMENT(true);
						theRetToken=_returnToken;
					}
					else if (((LA(1)=='$') && (LA(2)=='/'))&&(allowRegexpLiteral())) {
						mDOLLAR_REGEXP_LITERAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='?') && (true)) {
						mQUESTION(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='.') && (true)) {
						mDOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (true)) {
						mASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (true)) {
						mLNOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (true)) {
						mPLUS(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (true)) {
						mMINUS(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (true)) {
						mSTAR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (true)) {
						mMOD(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (true)) {
						mGT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (true)) {
						mLT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (true)) {
						mBXOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (true)) {
						mBOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (true)) {
						mBAND(true);
						theRetToken=_returnToken;
					}
					else if (((LA(1)=='#'))&&(getLine() == 1 && getColumn() == 1)) {
						mSH_COMMENT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (true)) {
						mREGEXP_LITERAL(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_0.member(LA(1))) && (true)) {
						mIDENT(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mQUESTION(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTION;
		int _saveIndex;
		
		match('?');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if ( inputState.guessing==0 ) {
			++parenLevel;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if ( inputState.guessing==0 ) {
			--parenLevel;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LBRACK;
		int _saveIndex;
		
		match('[');
		if ( inputState.guessing==0 ) {
			++parenLevel;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RBRACK;
		int _saveIndex;
		
		match(']');
		if ( inputState.guessing==0 ) {
			--parenLevel;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LCURLY;
		int _saveIndex;
		
		match('{');
		if ( inputState.guessing==0 ) {
			pushParenLevel();
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RCURLY;
		int _saveIndex;
		
		match('}');
		if ( inputState.guessing==0 ) {
			popParenLevel(); if(stringCtorState!=0) restartStringCtor(true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLON;
		int _saveIndex;
		
		match(':');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOT;
		int _saveIndex;
		
		match('.');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ASSIGN;
		int _saveIndex;
		
		match('=');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMPARE_TO(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMPARE_TO;
		int _saveIndex;
		
		match("<=>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQUAL;
		int _saveIndex;
		
		match("==");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mIDENTICAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IDENTICAL;
		int _saveIndex;
		
		match("===");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLNOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LNOT;
		int _saveIndex;
		
		match('!');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBNOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BNOT;
		int _saveIndex;
		
		match('~');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_EQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NOT_EQUAL;
		int _saveIndex;
		
		match("!=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_IDENTICAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NOT_IDENTICAL;
		int _saveIndex;
		
		match("!==");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIV(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIV;
		int _saveIndex;
		
		match('/');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIV_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIV_ASSIGN;
		int _saveIndex;
		
		match("/=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS_ASSIGN;
		int _saveIndex;
		
		match("+=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mINC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INC;
		int _saveIndex;
		
		match("++");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		match('-');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS_ASSIGN;
		int _saveIndex;
		
		match("-=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDEC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DEC;
		int _saveIndex;
		
		match("--");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR;
		int _saveIndex;
		
		match('*');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_ASSIGN;
		int _saveIndex;
		
		match("*=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMOD(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MOD;
		int _saveIndex;
		
		match('%');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMOD_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MOD_ASSIGN;
		int _saveIndex;
		
		match("%=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SR;
		int _saveIndex;
		
		match(">>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SR_ASSIGN;
		int _saveIndex;
		
		match(">>=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBSR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BSR;
		int _saveIndex;
		
		match(">>>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBSR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BSR_ASSIGN;
		int _saveIndex;
		
		match(">>>=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GE;
		int _saveIndex;
		
		match(">=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GT;
		int _saveIndex;
		
		match(">");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL;
		int _saveIndex;
		
		match("<<");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL_ASSIGN;
		int _saveIndex;
		
		match("<<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LE;
		int _saveIndex;
		
		match("<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LT;
		int _saveIndex;
		
		match('<');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBXOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BXOR;
		int _saveIndex;
		
		match('^');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBXOR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BXOR_ASSIGN;
		int _saveIndex;
		
		match("^=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOR;
		int _saveIndex;
		
		match('|');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBOR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOR_ASSIGN;
		int _saveIndex;
		
		match("|=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LOR;
		int _saveIndex;
		
		match("||");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBAND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BAND;
		int _saveIndex;
		
		match('&');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBAND_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BAND_ASSIGN;
		int _saveIndex;
		
		match("&=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLAND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LAND;
		int _saveIndex;
		
		match("&&");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEMI(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEMI;
		int _saveIndex;
		
		match(';');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDOLLAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOLLAR;
		int _saveIndex;
		
		match('$');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRANGE_INCLUSIVE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RANGE_INCLUSIVE;
		int _saveIndex;
		
		match("..");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRANGE_EXCLUSIVE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RANGE_EXCLUSIVE;
		int _saveIndex;
		
		match("..<");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mTRIPLE_DOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = TRIPLE_DOT;
		int _saveIndex;
		
		match("...");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSPREAD_DOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SPREAD_DOT;
		int _saveIndex;
		
		match("*.");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mOPTIONAL_DOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OPTIONAL_DOT;
		int _saveIndex;
		
		match("?.");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mELVIS_OPERATOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ELVIS_OPERATOR;
		int _saveIndex;
		
		match("?:");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMEMBER_POINTER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MEMBER_POINTER;
		int _saveIndex;
		
		match(".&");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mREGEX_FIND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = REGEX_FIND;
		int _saveIndex;
		
		match("=~");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mREGEX_MATCH(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = REGEX_MATCH;
		int _saveIndex;
		
		match("==~");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_STAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_STAR;
		int _saveIndex;
		
		match("**");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_STAR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_STAR_ASSIGN;
		int _saveIndex;
		
		match("**=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCLOSABLE_BLOCK_OP(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CLOSABLE_BLOCK_OP;
		int _saveIndex;
		
		match("->");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		int _cnt659=0;
		_loop659:
		do {
			if ((LA(1)=='\\') && (LA(2)=='\n'||LA(2)=='\r') && (true) && (true)) {
				match('\\');
				mONE_NL(false,false);
			}
			else if ((LA(1)==' ') && (true) && (true) && (true)) {
				match(' ');
			}
			else if ((LA(1)=='\t') && (true) && (true) && (true)) {
				match('\t');
			}
			else if ((LA(1)=='\u000c') && (true) && (true) && (true)) {
				match('\f');
			}
			else {
				if ( _cnt659>=1 ) { break _loop659; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt659++;
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			if (!whitespaceIncluded)  _ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mONE_NL(boolean _createToken,
		boolean check
	) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ONE_NL;
		int _saveIndex;
		
		{
		if ((LA(1)=='\r') && (LA(2)=='\n') && (true) && (true)) {
			_saveIndex=text.length();
			match("\r\n");
			text.setLength(_saveIndex);
		}
		else if ((LA(1)=='\r') && (true) && (true) && (true)) {
			_saveIndex=text.length();
			match('\r');
			text.setLength(_saveIndex);
		}
		else if ((LA(1)=='\n')) {
			_saveIndex=text.length();
			match('\n');
			text.setLength(_saveIndex);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		if ( inputState.guessing==0 ) {
			
			// update current line number for error reporting
			newlineCheck(check);
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mONE_NL_KEEP(boolean _createToken,
		boolean check
	) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ONE_NL_KEEP;
		int _saveIndex;
		
		{
		if ((LA(1)=='\r') && (LA(2)=='\n') && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && ((LA(4) >= '\u0000' && LA(4) <= '\ufffe'))) {
			match("\r\n");
		}
		else if ((LA(1)=='\r') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && (true)) {
			match('\r');
		}
		else if ((LA(1)=='\n')) {
			match('\n');
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		if ( inputState.guessing==0 ) {
			
			// update current line number for error reporting
			newlineCheck(check);
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNLS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NLS;
		int _saveIndex;
		
		mONE_NL(false,true);
		{
		if (((LA(1)=='\t'||LA(1)=='\n'||LA(1)=='\u000c'||LA(1)=='\r'||LA(1)==' '||LA(1)=='/'||LA(1)=='\\'))&&(!whitespaceIncluded)) {
			{
			int _cnt667=0;
			_loop667:
			do {
				switch ( LA(1)) {
				case '\n':  case '\r':
				{
					mONE_NL(false,true);
					break;
				}
				case '\t':  case '\u000c':  case ' ':  case '\\':
				{
					mWS(false);
					break;
				}
				default:
					if ((LA(1)=='/') && (LA(2)=='/')) {
						mSL_COMMENT(false);
					}
					else if ((LA(1)=='/') && (LA(2)=='*')) {
						mML_COMMENT(false);
					}
				else {
					if ( _cnt667>=1 ) { break _loop667; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				_cnt667++;
			} while (true);
			}
		}
		else {
		}
		
		}
		if ( inputState.guessing==0 ) {
			if (whitespaceIncluded) {
			// keep the token as-is
			} else if (parenLevel != 0) {
			// when directly inside parens, all newlines are ignored here
			_ttype = Token.SKIP;
			} else {
			// inside {...}, newlines must be explicitly matched as 'nls!'
			text.setLength(_begin); text.append("<newline>");
			}
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL_COMMENT;
		int _saveIndex;
		
		match("//");
		if ( inputState.guessing==0 ) {
			if (parser!=null) {
			parser.startComment(inputState.getLine(),inputState.getColumn()-2); }
			
		}
		{
		_loop671:
		do {
			if ((_tokenSet_1.member(LA(1))) && (true) && (true) && (true)) {
				{
				match(_tokenSet_1);
				}
			}
			else {
				break _loop671;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			if (parser!=null) {
			parser.endComment(0,inputState.getLine(),inputState.getColumn(),new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			if (!whitespaceIncluded)  _ttype = Token.SKIP; 
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mML_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ML_COMMENT;
		int _saveIndex;
		
		match("/*");
		if ( inputState.guessing==0 ) {
			if (parser!=null) { parser.startComment(inputState.getLine(),inputState.getColumn()-2); }
		}
		{
		_loop681:
		do {
			boolean synPredMatched679 = false;
			if (((LA(1)=='*') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && (true))) {
				int _m679 = mark();
				synPredMatched679 = true;
				inputState.guessing++;
				try {
					{
					match('*');
					matchNot('/');
					}
				}
				catch (RecognitionException pe) {
					synPredMatched679 = false;
				}
				rewind(_m679);
inputState.guessing--;
			}
			if ( synPredMatched679 ) {
				match('*');
			}
			else if ((LA(1)=='\n'||LA(1)=='\r')) {
				mONE_NL_KEEP(false,true);
			}
			else if ((_tokenSet_2.member(LA(1)))) {
				{
				match(_tokenSet_2);
				}
			}
			else {
				break _loop681;
			}
			
		} while (true);
		}
		match("*/");
		if ( inputState.guessing==0 ) {
			
			if (parser!=null) {
			parser.endComment(1,inputState.getLine(),inputState.getColumn(),new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			if (!whitespaceIncluded)  _ttype = Token.SKIP; 
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSH_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SH_COMMENT;
		int _saveIndex;
		
		if (!(getLine() == 1 && getColumn() == 1))
		  throw new SemanticException("getLine() == 1 && getColumn() == 1");
		match("#!");
		{
		_loop675:
		do {
			if ((_tokenSet_1.member(LA(1)))) {
				{
				match(_tokenSet_1);
				}
			}
			else {
				break _loop675;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			if (!whitespaceIncluded)  _ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTRING_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_LITERAL;
		int _saveIndex;
		int tt=0;
		
		boolean synPredMatched684 = false;
		if (((LA(1)=='\'') && (LA(2)=='\'') && (LA(3)=='\'') && ((LA(4) >= '\u0000' && LA(4) <= '\ufffe')))) {
			int _m684 = mark();
			synPredMatched684 = true;
			inputState.guessing++;
			try {
				{
				match("'''");
				}
			}
			catch (RecognitionException pe) {
				synPredMatched684 = false;
			}
			rewind(_m684);
inputState.guessing--;
		}
		if ( synPredMatched684 ) {
			_saveIndex=text.length();
			match("'''");
			text.setLength(_saveIndex);
			{
			_loop689:
			do {
				switch ( LA(1)) {
				case '\\':
				{
					mESC(false);
					break;
				}
				case '"':
				{
					match('"');
					break;
				}
				case '$':
				{
					match('$');
					break;
				}
				case '\n':  case '\r':
				{
					mSTRING_NL(false,true);
					break;
				}
				default:
					boolean synPredMatched688 = false;
					if (((LA(1)=='\'') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && ((LA(4) >= '\u0000' && LA(4) <= '\ufffe')))) {
						int _m688 = mark();
						synPredMatched688 = true;
						inputState.guessing++;
						try {
							{
							match('\'');
							{
							if ((_tokenSet_3.member(LA(1)))) {
								matchNot('\'');
							}
							else if ((LA(1)=='\'')) {
								match('\'');
								matchNot('\'');
							}
							else {
								throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
							}
							
							}
							}
						}
						catch (RecognitionException pe) {
							synPredMatched688 = false;
						}
						rewind(_m688);
inputState.guessing--;
					}
					if ( synPredMatched688 ) {
						match('\'');
					}
					else if ((_tokenSet_4.member(LA(1)))) {
						mSTRING_CH(false);
					}
				else {
					break _loop689;
				}
				}
			} while (true);
			}
			_saveIndex=text.length();
			match("'''");
			text.setLength(_saveIndex);
		}
		else {
			boolean synPredMatched693 = false;
			if (((LA(1)=='"') && (LA(2)=='"') && (LA(3)=='"') && ((LA(4) >= '\u0000' && LA(4) <= '\ufffe')))) {
				int _m693 = mark();
				synPredMatched693 = true;
				inputState.guessing++;
				try {
					{
					match("\"\"\"");
					}
				}
				catch (RecognitionException pe) {
					synPredMatched693 = false;
				}
				rewind(_m693);
inputState.guessing--;
			}
			if ( synPredMatched693 ) {
				_saveIndex=text.length();
				match("\"\"\"");
				text.setLength(_saveIndex);
				tt=mSTRING_CTOR_END(false,true, /*tripleQuote:*/ true);
				if ( inputState.guessing==0 ) {
					_ttype = tt;
				}
			}
			else if ((LA(1)=='\'') && (_tokenSet_1.member(LA(2))) && (true) && (true)) {
				_saveIndex=text.length();
				match('\'');
				text.setLength(_saveIndex);
				if ( inputState.guessing==0 ) {
					++suppressNewline;
				}
				{
				_loop691:
				do {
					switch ( LA(1)) {
					case '\\':
					{
						mESC(false);
						break;
					}
					case '"':
					{
						match('"');
						break;
					}
					case '$':
					{
						match('$');
						break;
					}
					default:
						if ((_tokenSet_4.member(LA(1)))) {
							mSTRING_CH(false);
						}
					else {
						break _loop691;
					}
					}
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					--suppressNewline;
				}
				_saveIndex=text.length();
				match('\'');
				text.setLength(_saveIndex);
			}
			else if ((LA(1)=='"') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true)) {
				_saveIndex=text.length();
				match('"');
				text.setLength(_saveIndex);
				if ( inputState.guessing==0 ) {
					++suppressNewline;
				}
				tt=mSTRING_CTOR_END(false,true, /*tripleQuote:*/ false);
				if ( inputState.guessing==0 ) {
					_ttype = tt;
				}
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
				_token = makeToken(_ttype);
				_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			_returnToken = _token;
		}
		
	protected final void mSTRING_CH(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_CH;
		int _saveIndex;
		
		{
		match(_tokenSet_4);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESC;
		int _saveIndex;
		
		if ((LA(1)=='\\') && (LA(2)=='"'||LA(2)=='$'||LA(2)=='\''||LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='\\'||LA(2)=='b'||LA(2)=='f'||LA(2)=='n'||LA(2)=='r'||LA(2)=='t'||LA(2)=='u')) {
			_saveIndex=text.length();
			match('\\');
			text.setLength(_saveIndex);
			{
			switch ( LA(1)) {
			case 'n':
			{
				match('n');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\n");
				}
				break;
			}
			case 'r':
			{
				match('r');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\r");
				}
				break;
			}
			case 't':
			{
				match('t');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\t");
				}
				break;
			}
			case 'b':
			{
				match('b');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\b");
				}
				break;
			}
			case 'f':
			{
				match('f');
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("\f");
				}
				break;
			}
			case '"':
			{
				match('"');
				break;
			}
			case '\'':
			{
				match('\'');
				break;
			}
			case '\\':
			{
				match('\\');
				break;
			}
			case '$':
			{
				match('$');
				break;
			}
			case 'u':
			{
				{
				int _cnt735=0;
				_loop735:
				do {
					if ((LA(1)=='u')) {
						match('u');
					}
					else {
						if ( _cnt735>=1 ) { break _loop735; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt735++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					text.setLength(_begin); text.append("");
				}
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
				if ( inputState.guessing==0 ) {
					char ch = (char)Integer.parseInt(new String(text.getBuffer(),_begin,text.length()-_begin),16); text.setLength(_begin); text.append(ch);
				}
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			{
				matchRange('0','3');
				{
				if (((LA(1) >= '0' && LA(1) <= '7')) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true)) {
					matchRange('0','7');
					{
					if (((LA(1) >= '0' && LA(1) <= '7')) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true)) {
						matchRange('0','7');
					}
					else if (((LA(1) >= '\u0000' && LA(1) <= '\ufffe')) && (true) && (true) && (true)) {
					}
					else {
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					
					}
				}
				else if (((LA(1) >= '\u0000' && LA(1) <= '\ufffe')) && (true) && (true) && (true)) {
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
				if ( inputState.guessing==0 ) {
					char ch = (char)Integer.parseInt(new String(text.getBuffer(),_begin,text.length()-_begin),8); text.setLength(_begin); text.append(ch);
				}
				break;
			}
			case '4':  case '5':  case '6':  case '7':
			{
				matchRange('4','7');
				{
				if (((LA(1) >= '0' && LA(1) <= '7')) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true)) {
					matchRange('0','7');
				}
				else if (((LA(1) >= '\u0000' && LA(1) <= '\ufffe')) && (true) && (true) && (true)) {
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
				if ( inputState.guessing==0 ) {
					char ch = (char)Integer.parseInt(new String(text.getBuffer(),_begin,text.length()-_begin),8); text.setLength(_begin); text.append(ch);
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
		}
		else if ((LA(1)=='\\') && (LA(2)=='\n'||LA(2)=='\r')) {
			_saveIndex=text.length();
			match('\\');
			text.setLength(_saveIndex);
			_saveIndex=text.length();
			mONE_NL(false,false);
			text.setLength(_saveIndex);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mSTRING_NL(boolean _createToken,
		boolean allowNewline
	) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_NL;
		int _saveIndex;
		
		if ( inputState.guessing==0 ) {
			if (!allowNewline) throw new MismatchedCharException('\n', '\n', true, this);
		}
		mONE_NL(false,false);
		if ( inputState.guessing==0 ) {
			text.setLength(_begin); text.append('\n');
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final int  mSTRING_CTOR_END(boolean _createToken,
		boolean fromStart, boolean tripleQuote
	) throws RecognitionException, CharStreamException, TokenStreamException {
		int tt=STRING_CTOR_END;
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_CTOR_END;
		int _saveIndex;
		boolean dollarOK = false;
		
		{
		_loop699:
		do {
			switch ( LA(1)) {
			case '\\':
			{
				mESC(false);
				break;
			}
			case '\'':
			{
				match('\'');
				break;
			}
			case '\n':  case '\r':
			{
				mSTRING_NL(false,tripleQuote);
				break;
			}
			default:
				boolean synPredMatched698 = false;
				if ((((LA(1)=='"') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true))&&(tripleQuote))) {
					int _m698 = mark();
					synPredMatched698 = true;
					inputState.guessing++;
					try {
						{
						match('"');
						{
						if ((_tokenSet_5.member(LA(1)))) {
							matchNot('"');
						}
						else if ((LA(1)=='"')) {
							match('"');
							matchNot('"');
						}
						else {
							throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
						}
						
						}
						}
					}
					catch (RecognitionException pe) {
						synPredMatched698 = false;
					}
					rewind(_m698);
inputState.guessing--;
				}
				if ( synPredMatched698 ) {
					match('"');
				}
				else if ((_tokenSet_4.member(LA(1)))) {
					mSTRING_CH(false);
				}
			else {
				break _loop699;
			}
			}
		} while (true);
		}
		{
		switch ( LA(1)) {
		case '"':
		{
			{
			if (((LA(1)=='"') && (LA(2)=='"'))&&(  tripleQuote )) {
				_saveIndex=text.length();
				match("\"\"\"");
				text.setLength(_saveIndex);
			}
			else if (((LA(1)=='"') && (true))&&( !tripleQuote )) {
				_saveIndex=text.length();
				match("\"");
				text.setLength(_saveIndex);
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
				if (fromStart)      tt = STRING_LITERAL;  // plain string literal!
				if (!tripleQuote)   {--suppressNewline;}
				// done with string constructor!
				//assert(stringCtorState == 0);
				
			}
			break;
		}
		case '$':
		{
			if ( inputState.guessing==0 ) {
				dollarOK = atValidDollarEscape();
			}
			_saveIndex=text.length();
			match('$');
			text.setLength(_saveIndex);
			if ( inputState.guessing==0 ) {
				
				require(dollarOK,
				"illegal string body character after dollar sign",
				"either escape a literal dollar sign \"\\$5\" or bracket the value expression \"${5}\"");
				// Yes, it's a string constructor, and we've got a value part.
				tt = (fromStart ? STRING_CTOR_START : STRING_CTOR_MIDDLE);
				stringCtorState = SCS_VAL + (tripleQuote? SCS_TQ_TYPE: SCS_SQ_TYPE);
				
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			_ttype = tt;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
		return tt;
	}
	
	public final void mREGEXP_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = REGEXP_LITERAL;
		int _saveIndex;
		int tt=0;
		
		if (((LA(1)=='/') && (_tokenSet_6.member(LA(2))) && (true) && (true))&&(allowRegexpLiteral())) {
			_saveIndex=text.length();
			match('/');
			text.setLength(_saveIndex);
			if ( inputState.guessing==0 ) {
				++suppressNewline;
			}
			{
			if (((LA(1)=='$') && (_tokenSet_7.member(LA(2))))&&(!atValidDollarEscape())) {
				match('$');
				tt=mREGEXP_CTOR_END(false,true);
			}
			else if ((_tokenSet_8.member(LA(1)))) {
				mREGEXP_SYMBOL(false);
				tt=mREGEXP_CTOR_END(false,true);
			}
			else if ((LA(1)=='$') && (true)) {
				_saveIndex=text.length();
				match('$');
				text.setLength(_saveIndex);
				if ( inputState.guessing==0 ) {
					
					// Yes, it's a regexp constructor, and we've got a value part.
					tt = STRING_CTOR_START;
					stringCtorState = SCS_VAL + SCS_RE_TYPE;
					
				}
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			if ( inputState.guessing==0 ) {
				_ttype = tt;
			}
		}
		else if ((LA(1)=='/') && (LA(2)=='=') && (true) && (true)) {
			mDIV_ASSIGN(false);
			if ( inputState.guessing==0 ) {
				_ttype = DIV_ASSIGN;
			}
		}
		else if ((LA(1)=='/') && (true)) {
			mDIV(false);
			if ( inputState.guessing==0 ) {
				_ttype = DIV;
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mREGEXP_SYMBOL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = REGEXP_SYMBOL;
		int _saveIndex;
		
		{
		if ((LA(1)=='\\') && (LA(2)=='/') && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && (true)) {
			match('\\');
			match('/');
			if ( inputState.guessing==0 ) {
				text.setLength(_begin); text.append('/');
			}
		}
		else if ((LA(1)=='\\') && (LA(2)=='\n'||LA(2)=='\r') && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && (true)) {
			_saveIndex=text.length();
			match('\\');
			text.setLength(_saveIndex);
			_saveIndex=text.length();
			mONE_NL(false,false);
			text.setLength(_saveIndex);
		}
		else if (((LA(1)=='\\') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true))&&( LA(2)!='/' && LA(2)!='\n' && LA(2)!='\r' )) {
			match('\\');
		}
		else if ((_tokenSet_9.member(LA(1)))) {
			{
			match(_tokenSet_9);
			}
		}
		else if ((LA(1)=='\n'||LA(1)=='\r')) {
			mSTRING_NL(false,true);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		{
		_loop726:
		do {
			if ((LA(1)=='*')) {
				match('*');
			}
			else {
				break _loop726;
			}
			
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final int  mREGEXP_CTOR_END(boolean _createToken,
		boolean fromStart
	) throws RecognitionException, CharStreamException, TokenStreamException {
		int tt=STRING_CTOR_END;
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = REGEXP_CTOR_END;
		int _saveIndex;
		
		{
		_loop710:
		do {
			if (((LA(1)=='$') && (_tokenSet_7.member(LA(2))))&&(!atValidDollarEscape())) {
				match('$');
			}
			else if ((_tokenSet_8.member(LA(1)))) {
				mREGEXP_SYMBOL(false);
			}
			else {
				break _loop710;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case '/':
		{
			_saveIndex=text.length();
			match('/');
			text.setLength(_saveIndex);
			if ( inputState.guessing==0 ) {
				
				if (fromStart)      tt = STRING_LITERAL;  // plain regexp literal!
				{--suppressNewline;}
				// done with regexp constructor!
				//assert(stringCtorState == 0);
				
			}
			break;
		}
		case '$':
		{
			_saveIndex=text.length();
			match('$');
			text.setLength(_saveIndex);
			if ( inputState.guessing==0 ) {
				
				// Yes, it's a regexp constructor, and we've got a value part.
				tt = (fromStart ? STRING_CTOR_START : STRING_CTOR_MIDDLE);
				stringCtorState = SCS_VAL + SCS_RE_TYPE;
				
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			_ttype = tt;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
		return tt;
	}
	
	public final void mDOLLAR_REGEXP_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOLLAR_REGEXP_LITERAL;
		int _saveIndex;
		int tt=0;
		
		if (!(allowRegexpLiteral()))
		  throw new SemanticException("allowRegexpLiteral()");
		_saveIndex=text.length();
		match("$/");
		text.setLength(_saveIndex);
		{
		if (((LA(1)=='$') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')))&&(!atValidDollarEscape())) {
			match('$');
			tt=mDOLLAR_REGEXP_CTOR_END(false,true);
		}
		else if ((_tokenSet_10.member(LA(1)))) {
			mDOLLAR_REGEXP_SYMBOL(false);
			tt=mDOLLAR_REGEXP_CTOR_END(false,true);
		}
		else if ((LA(1)=='$') && (true)) {
			_saveIndex=text.length();
			match('$');
			text.setLength(_saveIndex);
			if ( inputState.guessing==0 ) {
				
				// Yes, it's a regexp constructor, and we've got a value part.
				tt = STRING_CTOR_START;
				stringCtorState = SCS_VAL + SCS_DRE_TYPE;
				
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		if ( inputState.guessing==0 ) {
			_ttype = tt;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDOLLAR_REGEXP_SYMBOL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOLLAR_REGEXP_SYMBOL;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '/':
		{
			match('/');
			break;
		}
		case '\n':  case '\r':
		{
			mSTRING_NL(false,true);
			break;
		}
		default:
			if ((LA(1)=='\\') && (LA(2)=='\n'||LA(2)=='\r') && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && (true)) {
				_saveIndex=text.length();
				match('\\');
				text.setLength(_saveIndex);
				_saveIndex=text.length();
				mONE_NL(false,false);
				text.setLength(_saveIndex);
			}
			else if (((LA(1)=='\\') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true))&&( LA(2)!='\n' && LA(2)!='\r' )) {
				match('\\');
			}
			else if ((_tokenSet_11.member(LA(1)))) {
				{
				match(_tokenSet_11);
				}
			}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final int  mDOLLAR_REGEXP_CTOR_END(boolean _createToken,
		boolean fromStart
	) throws RecognitionException, CharStreamException, TokenStreamException {
		int tt=STRING_CTOR_END;
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOLLAR_REGEXP_CTOR_END;
		int _saveIndex;
		
		{
		_loop718:
		do {
			boolean synPredMatched715 = false;
			if (((LA(1)=='$') && (LA(2)=='/') && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && (true))) {
				int _m715 = mark();
				synPredMatched715 = true;
				inputState.guessing++;
				try {
					{
					match('$');
					match('/');
					}
				}
				catch (RecognitionException pe) {
					synPredMatched715 = false;
				}
				rewind(_m715);
inputState.guessing--;
			}
			if ( synPredMatched715 ) {
				mESCAPED_SLASH(false);
			}
			else {
				boolean synPredMatched717 = false;
				if (((LA(1)=='$') && (LA(2)=='$') && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && (true))) {
					int _m717 = mark();
					synPredMatched717 = true;
					inputState.guessing++;
					try {
						{
						match('$');
						match('$');
						}
					}
					catch (RecognitionException pe) {
						synPredMatched717 = false;
					}
					rewind(_m717);
inputState.guessing--;
				}
				if ( synPredMatched717 ) {
					mESCAPED_DOLLAR(false);
				}
				else if (((_tokenSet_10.member(LA(1))) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true))&&( !(LA(1) == '/' && LA(2) == '$') )) {
					mDOLLAR_REGEXP_SYMBOL(false);
				}
				else if (((LA(1)=='$') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true))&&(!atValidDollarEscape() && !atDollarSlashEscape() && !atDollarDollarEscape())) {
					match('$');
				}
				else {
					break _loop718;
				}
				}
			} while (true);
			}
			{
			switch ( LA(1)) {
			case '/':
			{
				_saveIndex=text.length();
				match("/$");
				text.setLength(_saveIndex);
				if ( inputState.guessing==0 ) {
					
					if (fromStart)      tt = STRING_LITERAL;  // plain regexp literal!
					
				}
				break;
			}
			case '$':
			{
				_saveIndex=text.length();
				match('$');
				text.setLength(_saveIndex);
				if ( inputState.guessing==0 ) {
					
					// Yes, it's a regexp constructor, and we've got a value part.
					tt = (fromStart ? STRING_CTOR_START : STRING_CTOR_MIDDLE);
					stringCtorState = SCS_VAL + SCS_DRE_TYPE;
					
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				_ttype = tt;
			}
			if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
				_token = makeToken(_ttype);
				_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			_returnToken = _token;
			return tt;
		}
		
	protected final void mESCAPED_SLASH(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESCAPED_SLASH;
		int _saveIndex;
		
		match('$');
		match('/');
		if ( inputState.guessing==0 ) {
			text.setLength(_begin); text.append('/');
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESCAPED_DOLLAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESCAPED_DOLLAR;
		int _saveIndex;
		
		match('$');
		match('$');
		if ( inputState.guessing==0 ) {
			text.setLength(_begin); text.append('$');
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mHEX_DIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEX_DIGIT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			matchRange('0','9');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':
		{
			matchRange('A','F');
			break;
		}
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':
		{
			matchRange('a','f');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mVOCAB(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = VOCAB;
		int _saveIndex;
		
		matchRange('\3','\377');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mIDENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IDENT;
		int _saveIndex;
		
		{
		if (((_tokenSet_0.member(LA(1))) && (true) && (true) && (true))&&(stringCtorState == 0)) {
			{
			if ((LA(1)=='$')) {
				mDOLLAR(false);
			}
			else if ((_tokenSet_12.member(LA(1)))) {
				mLETTER(false);
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			{
			_loop747:
			do {
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					mDIGIT(false);
					break;
				}
				case '$':
				{
					mDOLLAR(false);
					break;
				}
				default:
					if ((_tokenSet_12.member(LA(1)))) {
						mLETTER(false);
					}
				else {
					break _loop747;
				}
				}
			} while (true);
			}
		}
		else if ((_tokenSet_12.member(LA(1))) && (true) && (true) && (true)) {
			mLETTER(false);
			{
			_loop749:
			do {
				if ((_tokenSet_12.member(LA(1)))) {
					mLETTER(false);
				}
				else if (((LA(1) >= '0' && LA(1) <= '9'))) {
					mDIGIT(false);
				}
				else {
					break _loop749;
				}
				
			} while (true);
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		if ( inputState.guessing==0 ) {
			
			if (stringCtorState != 0) {
			if (LA(1) == '.' && LA(2) != '$' &&
			Character.isJavaIdentifierStart(LA(2))) {
			// pick up another name component before going literal again:
			restartStringCtor(false);
			} else {
			// go back to the string
			restartStringCtor(true);
			}
			}
			int ttype = testLiteralsTable(IDENT);
			// Java doesn't have the keywords 'as', 'in' or 'def so we make some allowances
			// for them in package names for better integration with existing Java packages
			if ((ttype == LITERAL_as || ttype == LITERAL_def || ttype == LITERAL_in) &&
			(LA(1) == '.' || lastSigTokenType == DOT || lastSigTokenType == LITERAL_package)) {
			ttype = IDENT;
			}
			// allow access to classes with the name package
			if ((ttype == LITERAL_package) &&
			(LA(1) == '.' || lastSigTokenType == DOT || lastSigTokenType == LITERAL_import
			|| (LA(1) == ')' && lastSigTokenType == LPAREN))) {
			ttype = IDENT;
			}
			if (ttype == LITERAL_static && LA(1) == '.') {
			ttype = IDENT;
			}
			
			/* The grammar allows a few keywords to follow dot.
			* TODO: Reinstate this logic if we change or remove keywordPropertyNames.
			if (ttype != IDENT && lastSigTokenType == DOT) {
			// A few keywords can follow a dot:
			switch (ttype) {
			case LITERAL_this: case LITERAL_super: case LITERAL_class:
			break;
			default:
			ttype = LITERAL_in;  // the poster child for bad dotted names
			}
			}
			*/
			_ttype = ttype;
			
			// check if "assert" keyword is enabled
			if (assertEnabled && "assert".equals(new String(text.getBuffer(),_begin,text.length()-_begin))) {
			_ttype = LITERAL_assert; // set token type for the rule in the parser
			}
			// check if "enum" keyword is enabled
			if (enumEnabled && "enum".equals(new String(text.getBuffer(),_begin,text.length()-_begin))) {
			_ttype = LITERAL_enum; // set token type for the rule in the parser
			}
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mLETTER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LETTER;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('a','z');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			matchRange('A','Z');
			break;
		}
		case '\u00c0':  case '\u00c1':  case '\u00c2':  case '\u00c3':
		case '\u00c4':  case '\u00c5':  case '\u00c6':  case '\u00c7':
		case '\u00c8':  case '\u00c9':  case '\u00ca':  case '\u00cb':
		case '\u00cc':  case '\u00cd':  case '\u00ce':  case '\u00cf':
		case '\u00d0':  case '\u00d1':  case '\u00d2':  case '\u00d3':
		case '\u00d4':  case '\u00d5':  case '\u00d6':
		{
			matchRange('\u00C0','\u00D6');
			break;
		}
		case '\u00d8':  case '\u00d9':  case '\u00da':  case '\u00db':
		case '\u00dc':  case '\u00dd':  case '\u00de':  case '\u00df':
		case '\u00e0':  case '\u00e1':  case '\u00e2':  case '\u00e3':
		case '\u00e4':  case '\u00e5':  case '\u00e6':  case '\u00e7':
		case '\u00e8':  case '\u00e9':  case '\u00ea':  case '\u00eb':
		case '\u00ec':  case '\u00ed':  case '\u00ee':  case '\u00ef':
		case '\u00f0':  case '\u00f1':  case '\u00f2':  case '\u00f3':
		case '\u00f4':  case '\u00f5':  case '\u00f6':
		{
			matchRange('\u00D8','\u00F6');
			break;
		}
		case '\u00f8':  case '\u00f9':  case '\u00fa':  case '\u00fb':
		case '\u00fc':  case '\u00fd':  case '\u00fe':  case '\u00ff':
		{
			matchRange('\u00F8','\u00FF');
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		default:
			if (((LA(1) >= '\u0100' && LA(1) <= '\ufffe'))) {
				matchRange('\u0100','\uFFFE');
			}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIGIT;
		int _saveIndex;
		
		matchRange('0','9');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIGITS_WITH_UNDERSCORE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIGITS_WITH_UNDERSCORE;
		int _saveIndex;
		
		mDIGIT(false);
		{
		if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9'||LA(1)=='_')) {
			mDIGITS_WITH_UNDERSCORE_OPT(false);
		}
		else {
		}
		
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIGITS_WITH_UNDERSCORE_OPT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIGITS_WITH_UNDERSCORE_OPT;
		int _saveIndex;
		
		{
		_loop756:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
				mDIGIT(false);
			}
			else if ((LA(1)=='_')) {
				match('_');
			}
			else {
				break _loop756;
			}
			
		} while (true);
		}
		mDIGIT(false);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNUM_INT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NUM_INT;
		int _saveIndex;
		Token e=null;
		Token f2=null;
		Token g2=null;
		Token f3=null;
		Token g3=null;
		Token f4=null;
		boolean isDecimal=false; Token t=null;
		
		{
		switch ( LA(1)) {
		case '0':
		{
			match('0');
			if ( inputState.guessing==0 ) {
				isDecimal = true;
			}
			{
			switch ( LA(1)) {
			case 'X':  case 'x':
			{
				{
				switch ( LA(1)) {
				case 'x':
				{
					match('x');
					break;
				}
				case 'X':
				{
					match('X');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					isDecimal = false;
				}
				mHEX_DIGIT(false);
				{
				if ((_tokenSet_13.member(LA(1))) && (true) && (true) && (true)) {
					{
					_loop763:
					do {
						if ((_tokenSet_14.member(LA(1))) && (_tokenSet_13.member(LA(2))) && (true) && (true)) {
							mHEX_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop763;
						}
						
					} while (true);
					}
					mHEX_DIGIT(false);
				}
				else {
				}
				
				}
				break;
			}
			case 'B':  case 'b':
			{
				{
				switch ( LA(1)) {
				case 'b':
				{
					match('b');
					break;
				}
				case 'B':
				{
					match('B');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				switch ( LA(1)) {
				case '0':
				{
					match('0');
					break;
				}
				case '1':
				{
					match('1');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='_')) {
					{
					_loop768:
					do {
						if ((LA(1)=='0') && (LA(2)=='0'||LA(2)=='1'||LA(2)=='_')) {
							match('0');
						}
						else if ((LA(1)=='1') && (LA(2)=='0'||LA(2)=='1'||LA(2)=='_')) {
							match('1');
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop768;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case '0':
					{
						match('0');
						break;
					}
					case '1':
					{
						match('1');
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
				}
				else {
				}
				
				}
				if ( inputState.guessing==0 ) {
					isDecimal = false;
				}
				break;
			}
			default:
				boolean synPredMatched772 = false;
				if ((((LA(1) >= '0' && LA(1) <= '9')) && (true) && (true) && (true))) {
					int _m772 = mark();
					synPredMatched772 = true;
					inputState.guessing++;
					try {
						{
						mDIGITS_WITH_UNDERSCORE(false);
						{
						switch ( LA(1)) {
						case '.':
						{
							match('.');
							mDIGITS_WITH_UNDERSCORE(false);
							break;
						}
						case 'E':  case 'e':
						{
							mEXPONENT(false);
							break;
						}
						case 'D':  case 'F':  case 'd':  case 'f':
						{
							mFLOAT_SUFFIX(false);
							break;
						}
						default:
						{
							throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
						}
						}
						}
						}
					}
					catch (RecognitionException pe) {
						synPredMatched772 = false;
					}
					rewind(_m772);
inputState.guessing--;
				}
				if ( synPredMatched772 ) {
					mDIGITS_WITH_UNDERSCORE(false);
				}
				else if (((LA(1) >= '0' && LA(1) <= '7')) && (true) && (true) && (true)) {
					{
					matchRange('0','7');
					}
					{
					if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='_')) {
						{
						_loop776:
						do {
							if (((LA(1) >= '0' && LA(1) <= '7')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='_')) {
								matchRange('0','7');
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop776;
							}
							
						} while (true);
						}
						{
						matchRange('0','7');
						}
					}
					else {
					}
					
					}
					if ( inputState.guessing==0 ) {
						isDecimal = false;
					}
				}
				else {
				}
			}
			}
			break;
		}
		case '1':  case '2':  case '3':  case '4':
		case '5':  case '6':  case '7':  case '8':
		case '9':
		{
			{
			matchRange('1','9');
			}
			{
			if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9'||LA(1)=='_')) {
				mDIGITS_WITH_UNDERSCORE_OPT(false);
			}
			else {
			}
			
			}
			if ( inputState.guessing==0 ) {
				isDecimal=true;
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		switch ( LA(1)) {
		case 'L':  case 'l':
		{
			{
			switch ( LA(1)) {
			case 'l':
			{
				match('l');
				break;
			}
			case 'L':
			{
				match('L');
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				_ttype = NUM_LONG;
			}
			break;
		}
		case 'I':  case 'i':
		{
			{
			switch ( LA(1)) {
			case 'i':
			{
				match('i');
				break;
			}
			case 'I':
			{
				match('I');
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				_ttype = NUM_INT;
			}
			break;
		}
		case 'G':  case 'g':
		{
			mBIG_SUFFIX(false);
			if ( inputState.guessing==0 ) {
				_ttype = NUM_BIG_INT;
			}
			break;
		}
		default:
			boolean synPredMatched785 = false;
			if ((((LA(1)=='.'||LA(1)=='D'||LA(1)=='E'||LA(1)=='F'||LA(1)=='d'||LA(1)=='e'||LA(1)=='f'))&&(isDecimal))) {
				int _m785 = mark();
				synPredMatched785 = true;
				inputState.guessing++;
				try {
					{
					if ((_tokenSet_15.member(LA(1)))) {
						matchNot('.');
					}
					else if ((LA(1)=='.')) {
						match('.');
						{
						matchRange('0','9');
						}
					}
					else {
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					
					}
				}
				catch (RecognitionException pe) {
					synPredMatched785 = false;
				}
				rewind(_m785);
inputState.guessing--;
			}
			if ( synPredMatched785 ) {
				{
				switch ( LA(1)) {
				case '.':
				{
					match('.');
					mDIGITS_WITH_UNDERSCORE(false);
					{
					if ((LA(1)=='E'||LA(1)=='e')) {
						mEXPONENT(true);
						e=_returnToken;
					}
					else {
					}
					
					}
					{
					switch ( LA(1)) {
					case 'D':  case 'F':  case 'd':  case 'f':
					{
						mFLOAT_SUFFIX(true);
						f2=_returnToken;
						if ( inputState.guessing==0 ) {
							t=f2;
						}
						break;
					}
					case 'G':  case 'g':
					{
						mBIG_SUFFIX(true);
						g2=_returnToken;
						if ( inputState.guessing==0 ) {
							t=g2;
						}
						break;
					}
					default:
						{
						}
					}
					}
					break;
				}
				case 'E':  case 'e':
				{
					mEXPONENT(false);
					{
					switch ( LA(1)) {
					case 'D':  case 'F':  case 'd':  case 'f':
					{
						mFLOAT_SUFFIX(true);
						f3=_returnToken;
						if ( inputState.guessing==0 ) {
							t=f3;
						}
						break;
					}
					case 'G':  case 'g':
					{
						mBIG_SUFFIX(true);
						g3=_returnToken;
						if ( inputState.guessing==0 ) {
							t=g3;
						}
						break;
					}
					default:
						{
						}
					}
					}
					break;
				}
				case 'D':  case 'F':  case 'd':  case 'f':
				{
					mFLOAT_SUFFIX(true);
					f4=_returnToken;
					if ( inputState.guessing==0 ) {
						t=f4;
					}
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
					String txt = (t == null ? "" : t.getText().toUpperCase());
					if (txt.indexOf('F') >= 0) {
					_ttype = NUM_FLOAT;
					} else if (txt.indexOf('G') >= 0) {
					_ttype = NUM_BIG_DECIMAL;
					} else {
					_ttype = NUM_DOUBLE; // assume double
					}
					
				}
			}
			else {
			}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mEXPONENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EXPONENT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'e':
		{
			match('e');
			break;
		}
		case 'E':
		{
			match('E');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		switch ( LA(1)) {
		case '+':
		{
			match('+');
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':  case '_':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		_loop795:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
				matchRange('0','9');
			}
			else if ((LA(1)=='_')) {
				match('_');
			}
			else {
				break _loop795;
			}
			
		} while (true);
		}
		{
		matchRange('0','9');
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mFLOAT_SUFFIX(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FLOAT_SUFFIX;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'f':
		{
			match('f');
			break;
		}
		case 'F':
		{
			match('F');
			break;
		}
		case 'd':
		{
			match('d');
			break;
		}
		case 'D':
		{
			match('D');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mBIG_SUFFIX(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BIG_SUFFIX;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'g':
		{
			match('g');
			break;
		}
		case 'G':
		{
			match('G');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT;
		int _saveIndex;
		
		match('@');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[2560];
		data[0]=68719476736L;
		data[1]=576460745995190270L;
		data[3]=-36028797027352577L;
		for (int i = 4; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[2048];
		data[0]=-9217L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[2048];
		data[0]=-4398046520321L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[2048];
		data[0]=-549755813889L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[2048];
		data[0]=-635655169025L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[2048];
		data[0]=-17179869185L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[2048];
		data[0]=-145135534866433L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = new long[2048];
		data[0]=-4398046511105L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = new long[2048];
		data[0]=-145204254343169L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = new long[2048];
		data[0]=-145204254352385L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[2048];
		data[0]=-68719476737L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = new long[2048];
		data[0]=-140806207841281L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = new long[2560];
		data[1]=576460745995190270L;
		data[3]=-36028797027352577L;
		for (int i = 4; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=543313363070L;
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=541165879422L;
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = new long[2048];
		data[0]=-70368744177665L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	
	}
