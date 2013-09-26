// $ANTLR 2.7.7 (20060906): "groovy.g" -> "GroovyRecognizer.java"$

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

import groovyjarjarantlr.TokenBuffer;
import groovyjarjarantlr.TokenStreamException;
import groovyjarjarantlr.TokenStreamIOException;
import groovyjarjarantlr.ANTLRException;
import groovyjarjarantlr.LLkParser;
import groovyjarjarantlr.Token;
import groovyjarjarantlr.TokenStream;
import groovyjarjarantlr.RecognitionException;
import groovyjarjarantlr.NoViableAltException;
import groovyjarjarantlr.MismatchedTokenException;
import groovyjarjarantlr.SemanticException;
import groovyjarjarantlr.ParserSharedInputState;
import groovyjarjarantlr.collections.impl.BitSet;
import groovyjarjarantlr.collections.AST;
import java.util.Hashtable;
import groovyjarjarantlr.ASTFactory;
import groovyjarjarantlr.ASTPair;
import groovyjarjarantlr.collections.impl.ASTArray;

/** JSR-241 Groovy Recognizer.
 *
 * Run 'java Main [-showtree] directory-full-of-groovy-files'
 *
 * [The -showtree option pops up a Swing frame that shows
 *  the AST constructed from the parser.]
 *
 * Contributing authors:
 *              John Mitchell           johnm@non.net
 *              Terence Parr            parrt@magelang.com
 *              John Lilley             jlilley@empathy.com
 *              Scott Stanchfield       thetick@magelang.com
 *              Markus Mohnen           mohnen@informatik.rwth-aachen.de
 *              Peter Williams          pete.williams@sun.com
 *              Allan Jacobs            Allan.Jacobs@eng.sun.com
 *              Steve Messick           messick@redhills.com
 *              James Strachan          jstrachan@protique.com
 *              John Pybus              john@pybus.org
 *              John Rose               rose00@mac.com
 *              Jeremy Rayner           groovy@ross-rayner.com
 *              Alex Popescu            the.mindstorm@gmail.com
 *              Martin Kempf            mkempf@hsr.ch
 *              Reto Kleeb              rkleeb@hsr.ch
 *
 * Version 1.00 December 9, 1997 -- initial release
 * Version 1.01 December 10, 1997
 *              fixed bug in octal def (0..7 not 0..8)
 * Version 1.10 August 1998 (parrt)
 *              added tree construction
 *              fixed definition of WS,comments for mac,pc,unix newlines
 *              added unary plus
 * Version 1.11 (Nov 20, 1998)
 *              Added "shutup" option to turn off last ambig warning.
 *              Fixed inner class def to allow named class defs as statements
 *              synchronized requires compound not simple statement
 *              add [] after builtInType DOT class in primaryExpression
 *              "const" is reserved but not valid..removed from modifiers
 * Version 1.12 (Feb 2, 1999)
 *              Changed LITERAL_xxx to xxx in tree grammar.
 *              Updated java.g to use tokens {...} now for 2.6.0 (new feature).
 *
 * Version 1.13 (Apr 23, 1999)
 *              Didn't have (stat)? for else clause in tree parser.
 *              Didn't gen ASTs for interface extends.  Updated tree parser too.
 *              Updated to 2.6.0.
 * Version 1.14 (Jun 20, 1999)
 *              Allowed final/abstract on local classes.
 *              Removed local interfaces from methods
 *              Put instanceof precedence where it belongs...in relationalExpr
 *                      It also had expr not type as arg; fixed it.
 *              Missing ! on SEMI in classBlock
 *              fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
 *              fixed: didn't like Object[].class in parser or tree parser
 * Version 1.15 (Jun 26, 1999)
 *              Screwed up rule with instanceof in it. :(  Fixed.
 *              Tree parser didn't like (expr).something; fixed.
 *              Allowed multiple inheritance in tree grammar. oops.
 * Version 1.16 (August 22, 1999)
 *              Extending an interface built a wacky tree: had extra EXTENDS.
 *              Tree grammar didn't allow multiple superinterfaces.
 *              Tree grammar didn't allow empty var initializer: {}
 * Version 1.17 (October 12, 1999)
 *              ESC lexer rule allowed 399 max not 377 max.
 *              java.tree.g didn't handle the expression of synchronized
 *              statements.
 * Version 1.18 (August 12, 2001)
 *              Terence updated to Java 2 Version 1.3 by
 *              observing/combining work of Allan Jacobs and Steve
 *              Messick.  Handles 1.3 src.  Summary:
 *              o  primary didn't include boolean.class kind of thing
 *              o  constructor calls parsed explicitly now:
 *                 see explicitConstructorInvocation
 *              o  add strictfp modifier
 *              o  missing objBlock after new expression in tree grammar
 *              o  merged local class definition alternatives, moved after declaration
 *              o  fixed problem with ClassName.super.field
 *              o  reordered some alternatives to make things more efficient
 *              o  long and double constants were not differentiated from int/float
 *              o  whitespace rule was inefficient: matched only one char
 *              o  add an examples directory with some nasty 1.3 cases
 *              o  made Main.java use buffered IO and a Reader for Unicode support
 *              o  supports UNICODE?
 *                 Using Unicode charVocabulary makes code file big, but only
 *                 in the bitsets at the end. I need to make ANTLR generate
 *                 unicode bitsets more efficiently.
 * Version 1.19 (April 25, 2002)
 *              Terence added in nice fixes by John Pybus concerning floating
 *              constants and problems with super() calls.  John did a nice
 *              reorg of the primary/postfix expression stuff to read better
 *              and makes f.g.super() parse properly (it was METHOD_CALL not
 *              a SUPER_CTOR_CALL).  Also:
 *
 *              o  "finally" clause was a root...made it a child of "try"
 *              o  Added stuff for asserts too for Java 1.4, but *commented out*
 *                 as it is not backward compatible.
 *
 * Version 1.20 (October 27, 2002)
 *
 *        Terence ended up reorging John Pybus' stuff to
 *        remove some nondeterminisms and some syntactic predicates.
 *        Note that the grammar is stricter now; e.g., this(...) must
 *      be the first statement.
 *
 *        Trinary ?: operator wasn't working as array name:
 *                (isBig ? bigDigits : digits)[i];
 *
 *        Checked parser/tree parser on source for
 *                Resin-2.0.5, jive-2.1.1, jdk 1.3.1, Lucene, antlr 2.7.2a4,
 *              and the 110k-line jGuru server source.
 *
 * Version 1.21 (October 17, 2003)
 *  Fixed lots of problems including:
 *  Ray Waldin: add typeDefinition to interfaceBlock in java.tree.g
 *  He found a problem/fix with floating point that start with 0
 *  Ray also fixed problem that (int.class) was not recognized.
 *  Thorsten van Ellen noticed that \n are allowed incorrectly in strings.
 *  TJP fixed CHAR_LITERAL analogously.
 *
 * Version 1.21.2 (March, 2003)
 *        Changes by Matt Quail to support generics (as per JDK1.5/JSR14)
 *        Notes:
 *        o We only allow the "extends" keyword and not the "implements"
 *              keyword, since that's what JSR14 seems to imply.
 *        o Thanks to Monty Zukowski for his help on the antlr-interest
 *              mail list.
 *        o Thanks to Alan Eliasen for testing the grammar over his
 *              Fink source base
 *
 * Version 1.22 (July, 2004)
 *        Changes by Michael Studman to support Java 1.5 language extensions
 *        Notes:
 *        o Added support for annotations types
 *        o Finished off Matt Quail's generics enhancements to support bound type arguments
 *        o Added support for new for statement syntax
 *        o Added support for static import syntax
 *        o Added support for enum types
 *        o Tested against JDK 1.5 source base and source base of jdigraph project
 *        o Thanks to Matt Quail for doing the hard part by doing most of the generics work
 *
 * Version 1.22.1 (July 28, 2004)
 *        Bug/omission fixes for Java 1.5 language support
 *        o Fixed tree structure bug with classOrInterface - thanks to Pieter Vangorpto for
 *              spotting this
 *        o Fixed bug where incorrect handling of SR and BSR tokens would cause type
 *              parameters to be recognised as type arguments.
 *        o Enabled type parameters on constructors, annotations on enum constants
 *              and package definitions
 *        o Fixed problems when parsing if ((char.class.equals(c))) {} - solution by Matt Quail at Cenqua
 *
 * Version 1.22.2 (July 28, 2004)
 *        Slight refactoring of Java 1.5 language support
 *        o Refactored for/"foreach" productions so that original literal "for" literal
 *          is still used but the for sub-clauses vary by token type
 *        o Fixed bug where type parameter was not included in generic constructor's branch of AST
 *
 * Version 1.22.3 (August 26, 2004)
 *        Bug fixes as identified by Michael Stahl; clean up of tabs/spaces
 *        and other refactorings
 *        o Fixed typeParameters omission in identPrimary and newStatement
 *        o Replaced GT reconcilliation code with simple semantic predicate
 *        o Adapted enum/assert keyword checking support from Michael Stahl's java15 grammar
 *        o Refactored typeDefinition production and field productions to reduce duplication
 *
 * Version 1.22.4 (October 21, 2004)
 *    Small bux fixes
 *    o Added typeArguments to explicitConstructorInvocation, e.g. new <String>MyParameterised()
 *    o Added typeArguments to postfixExpression productions for anonymous inner class super
 *      constructor invocation, e.g. new Outer().<String>super()
 *    o Fixed bug in array declarations identified by Geoff Roy
 *
 * Version 1.22.4.g.1
 *    o I have taken java.g for Java1.5 from Michael Studman (1.22.4)
 *      and have applied the groovy.diff from java.g (1.22) by John Rose
 *      back onto the new root (1.22.4) - Jeremy Rayner (Jan 2005)
 *    o for a map of the task see...
 *      http://groovy.javanicus.com/java-g.png
 *
 * Version 1.22.4.g.2
 *    o mkempf, rkleeb, Dec 2007
 *    o fixed various rules so that they call the correct Create Method
 *      to make sure that the line information are correct
 *
 * This grammar is in the PUBLIC DOMAIN
 */
public class GroovyRecognizer extends groovyjarjarantlr.LLkParser       implements GroovyTokenTypes
 {

        /** This factory is the correct way to wire together a Groovy parser and lexer. */
    public static GroovyRecognizer make(GroovyLexer lexer) {
        GroovyRecognizer parser = new GroovyRecognizer(lexer.plumb());
        // TODO: set up a common error-handling control block, to avoid excessive tangle between these guys
        parser.lexer = lexer;
        lexer.parser = parser;
        parser.getASTFactory().setASTNodeClass(GroovySourceAST.class);
        parser.warningList = new ArrayList();
        parser.errorList = new ArrayList();
        return parser;
    }
    // Create a scanner that reads from the input stream passed to us...
    public static GroovyRecognizer make(InputStream in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(Reader in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(InputBuffer in) { return make(new GroovyLexer(in)); }
    public static GroovyRecognizer make(LexerSharedInputState in) { return make(new GroovyLexer(in)); }

    private static GroovySourceAST dummyVariableToforceClassLoaderToFindASTClass = new GroovySourceAST();

    List warningList;
    public List getWarningList() { return warningList; }

    List errorList;
    public List getErrorList() { return errorList; }

	List<Comment> comments = new ArrayList<Comment>();
	public List<Comment> getComments() { return comments; }
	
    GroovyLexer lexer;
    public GroovyLexer getLexer() { return lexer; }
    public void setFilename(String f) { super.setFilename(f); lexer.setFilename(f); }
    private SourceBuffer sourceBuffer;
    public void setSourceBuffer(SourceBuffer sourceBuffer) {
        this.sourceBuffer = sourceBuffer;
    }

    /** Create an AST node with the token type and text passed in, but
     *  with the same background information as another supplied Token (e.g.&nbsp;line numbers).
     * To be used in place of antlr tree construction syntax,
     * i.e. #[TOKEN,"text"]  becomes  create(TOKEN,"text",anotherToken)
     *
     * todo - change antlr.ASTFactory to do this instead...
     */
    public AST create(int type, String txt, AST first) {
        AST t = astFactory.create(type,txt);
        if ( t != null && first != null) {
            // first copy details from first token
            t.initialize(first);
            // then ensure that type and txt are specific to this new node
            t.initialize(type,txt);
        }
        return t;
    }

    // GRE292
    public AST create2(int type, String txt, Token first, Token last) {
        return setEndLocationBasedOnThisNode(create(type, txt, astFactory.create(first)), last);
    }
    
    // GRE292
    private AST setEndLocationBasedOnThisNode(AST ast, Object node) {
    	if ((ast instanceof GroovySourceAST) && (node instanceof SourceInfo)) {
            SourceInfo lastInfo = (SourceInfo) node;
            GroovySourceAST groovySourceAst = (GroovySourceAST)ast;
            groovySourceAst.setColumnLast(lastInfo.getColumnLast());
            groovySourceAst.setLineLast(lastInfo.getLineLast());
      }
      return ast;
    }
    
    private AST attachLast(AST t, Object last) {
        if ((t instanceof GroovySourceAST) && (last instanceof SourceInfo)) {
            SourceInfo lastInfo = (SourceInfo) last;
            GroovySourceAST node = (GroovySourceAST)t;
            node.setColumnLast(lastInfo.getColumn());
            node.setLineLast(lastInfo.getLine());
            // This is a good point to call node.setSnippet(),
            // but it bulks up the AST too much for production code.
        }
        return t;
    }

    public AST create(int type, String txt, Token first, Token last) {
        return attachLast(create(type, txt, astFactory.create(first)), last);
    }

    public AST create(int type, String txt, AST first, Token last) {
        return attachLast(create(type, txt, first), last);
    }

    public AST create(int type, String txt, AST first, AST last) {
        return attachLast(create(type, txt, first), last);
    }
    
    // GRECLIPSE start
	private Stack<Integer> commentStartPositions = new Stack<Integer>();

	public void startComment(int line, int column) {
		// System.out.println(">> comment at l"+line+"c"+column);
		commentStartPositions.push((line<<16)+column);
	}

	public void endComment(int type, int line, int column,String text) {
		// System.out.println("<< comment at l"+line+"c"+column+" ["+text+"]");
		int lineAndColumn = commentStartPositions.pop();
		int startLine = lineAndColumn>>>16;
		int startColumn = lineAndColumn&0xffff;
		if (type==0) {
			Comment comment = Comment.makeSingleLineComment(startLine,startColumn,line,column,text);
			comments.add(comment);
		} else if (type==1) {
			Comment comment = Comment.makeMultiLineComment(startLine,startColumn,line,column,text);
			comments.add(comment);
		} 
	}
    // GRECLIPSE end
	
	
    /** 
    *   Clones the token
    */
    public Token cloneToken(Token t) {
        CommonToken clone = new CommonToken(t.getType(),t.getText());
        clone.setLine(t.getLine());
        clone.setColumn(t.getColumn());
        return clone;
    }


    // stuff to adjust ANTLR's tracing machinery
    public static boolean tracing = false;  // only effective if antlr.Tool is run with -traceParser
    public void traceIn(String rname) throws TokenStreamException {
        if (!GroovyRecognizer.tracing)  return;
        super.traceIn(rname);
    }
    public void traceOut(String rname) throws TokenStreamException {
        if (!GroovyRecognizer.tracing)  return;
        if (returnAST != null)  rname += returnAST.toStringList();
        super.traceOut(rname);
    }

    // Error handling.  This is a funnel through which parser errors go, when the parser can suggest a solution.
    public void requireFailed(String problem, String solution) throws SemanticException {
        // TODO: Needs more work.
        Token lt = null;
        int lineNum = Token.badToken.getLine(), colNum = Token.badToken.getColumn();
        try {
            lt = LT(1);
            if(lt != null) {
                lineNum = lt.getLine();
                colNum = lt.getColumn();
            }
        }
        catch (TokenStreamException ee) {
            if(ee instanceof TokenStreamRecognitionException) {
                lineNum = ((TokenStreamRecognitionException) ee).recog.getLine();
                colNum = ((TokenStreamRecognitionException) ee).recog.getColumn();
            }
        }
        throw new SemanticException(problem + ";\n   solution: " + solution,
                                    getFilename(), lineNum, colNum);
    }

    public void addWarning(String warning, String solution) {
        Token lt = null;
        try { lt = LT(1); }
        catch (TokenStreamException ee) { }
        if (lt == null)  lt = Token.badToken;

        Map row = new HashMap();
        row.put("warning",  warning);
        row.put("solution", solution);
        row.put("filename", getFilename());
        row.put("line",     Integer.valueOf(lt.getLine()));
        row.put("column",   Integer.valueOf(lt.getColumn()));
        // System.out.println(row);
        warningList.add(row);
    }

    /**
     * Report a recovered error.
     */
    public void reportError(String message) {
        Token lt = null;
        try { lt = LT(1); }
        catch (TokenStreamException ee) { }
        if (lt == null)  lt = Token.badToken;

        Map row = new HashMap();
        row.put("error" ,message);
        row.put("filename", getFilename());
        row.put("line", new Integer(lt.getLine()));
        row.put("column", new Integer(lt.getColumn()));
        errorList.add(row);
    }
    
    /**
     * Report a recovered error and specify the token.
     */
    public void reportError(String message, Token lt) {
        Map row = new HashMap();
        row.put("error" ,message);
        row.put("filename", getFilename());
        row.put("line", new Integer(lt.getLine()));
        row.put("column", new Integer(lt.getColumn()));
        errorList.add(row);
    }
    
    /**
     * Report a recovered error and specify the token.
     */
    public void reportError(String message, AST lt) {
        Map row = new HashMap();
        row.put("error" ,message);
        row.put("filename", getFilename());
        row.put("line", new Integer(lt.getLine()));
        row.put("column", new Integer(lt.getColumn()));
        errorList.add(row);
    }
    
    /**
     * Report a recovered exception.
     */
    public void reportError(RecognitionException e) {
        Token lt = null;
        try { lt = LT(1); }
        catch (TokenStreamException ee) { }
        if (lt == null)  lt = Token.badToken;

        Map row = new HashMap();
        row.put("error", e.getMessage());
        row.put("filename", getFilename());
        row.put("line",     Integer.valueOf(lt.getLine()));
        row.put("column",   Integer.valueOf(lt.getColumn()));
        errorList.add(row);
    }

    // Convenience method for checking of expected error syndromes.
    private void require(boolean z, String problem, String solution) throws SemanticException {
        if (!z)  requireFailed(problem, solution);
    }

    private boolean matchGenericTypeBrackets(boolean z, String problem, String solution) throws SemanticException {
        if (!z)  matchGenericTypeBracketsFailed(problem, solution);
        return z;
    }

    public void matchGenericTypeBracketsFailed(String problem, String solution) throws SemanticException {
        Token lt = null;
        int lineNum = Token.badToken.getLine(), colNum = Token.badToken.getColumn();

        try {
            lt = LT(1);
            if(lt != null) {
                lineNum = lt.getLine();
                colNum = lt.getColumn();
            }
        }
        catch (TokenStreamException ee) {
            if(ee instanceof TokenStreamRecognitionException) {
                lineNum = ((TokenStreamRecognitionException) ee).recog.getLine();
                colNum = ((TokenStreamRecognitionException) ee).recog.getColumn();
            }
        }

        throw new SemanticException(problem + ";\n   solution: " + solution,
                                    getFilename(), lineNum, colNum);
   }

    // Query a name token to see if it begins with a capital letter.
    // This is used to tell the difference (w/o symbol table access) between {String x} and {println x}.
    private boolean isUpperCase(Token x) {
        if (x == null || x.getType() != IDENT)  return false;  // cannot happen?
        String xtext = x.getText();
        return (xtext.length() > 0 && Character.isUpperCase(xtext.charAt(0)));
    }

    private AST currentClass = null;  // current enclosing class (for constructor recognition)
    // Query a name token to see if it is identical with the current class name.
    // This is used to distinguish constructors from other methods.
    private boolean isConstructorIdent(Token x) {
        if (currentClass == null)  return false;
        if (currentClass.getType() != IDENT)  return false;  // cannot happen?
        String cname = currentClass.getText();

        if (x == null || x.getType() != IDENT)  return false;  // cannot happen?
        return cname.equals(x.getText());
    }
    
    private void dumpTree(AST ast, String offset) {
    	dump(ast, offset);
        for (AST node = ast.getFirstChild(); node != null; node = node.getNextSibling()) {
            dumpTree(node, offset+"\t");
        }
    }

    private void dump(AST node, String offset) {
        System.out.println(offset+"Type: " + getTokenName(node) + " text: " + node.getText());
    }
    
    private String getTokenName(AST node) {
        if (node == null) return "null";
        return getTokenName(node.getType());
    }

    // Scratch variable for last 'sep' token.
    // Written by the 'sep' rule, read only by immediate callers of 'sep'.
    // (Not entirely clean, but better than a million xx=sep occurrences.)
    private int sepToken = EOF;

    // Scratch variable for last argument list; tells whether there was a label.
    // Written by 'argList' rule, read only by immediate callers of 'argList'.
    private boolean argListHasLabels = false;

    // Scratch variable, holds most recently completed pathExpression.
    // Read only by immediate callers of 'pathExpression' and 'expression'.
    private AST lastPathExpression = null;

    // Inherited attribute pushed into most expression rules.
    // If not zero, it means that the left context of the expression
    // being parsed is a statement boundary or an initializer sign '='.
    // Only such expressions are allowed to reach across newlines
    // to pull in an LCURLY and appended block.
    private final int LC_STMT = 1, LC_INIT = 2;

    /**
     * Counts the number of LT seen in the typeArguments production.
     * It is used in semantic predicates to ensure we have seen
     * enough closing '>' characters; which actually may have been
     * either GT, SR or BSR tokens.
     */
    private int ltCounter = 0;

    /* This symbol is used to work around a known ANTLR limitation.
     * In a loop with syntactic predicate, ANTLR needs help knowing
     * that the loop exit is a second alternative.
     * Example usage:  ( (LCURLY)=> block | {ANTLR_LOOP_EXIT}? )*
     * Probably should be an ANTLR RFE.
     */
    ////// Original comment in Java grammar:
    // Unfortunately a syntactic predicate can only select one of
    // multiple alternatives on the same level, not break out of
    // an enclosing loop, which is why this ugly hack (a fake
    // empty alternative with always-false semantic predicate)
    // is necessary.
    private static final boolean ANTLR_LOOP_EXIT = false;

protected GroovyRecognizer(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public GroovyRecognizer(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected GroovyRecognizer(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public GroovyRecognizer(TokenStream lexer) {
  this(lexer,2);
}

public GroovyRecognizer(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void compilationUnit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compilationUnit_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case SH_COMMENT:
			{
				match(SH_COMMENT);
				break;
			}
			case EOF:
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_package:
			case LITERAL_import:
			case LITERAL_static:
			case LITERAL_def:
			case LBRACK:
			case IDENT:
			case STRING_LITERAL:
			case LPAREN:
			case LITERAL_class:
			case LITERAL_interface:
			case LITERAL_enum:
			case AT:
			case LITERAL_super:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			case SEMI:
			case LITERAL_this:
			case LITERAL_if:
			case LITERAL_while:
			case LITERAL_switch:
			case LITERAL_for:
			case LITERAL_return:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_throw:
			case LITERAL_assert:
			case PLUS:
			case MINUS:
			case LITERAL_try:
			case LITERAL_false:
			case LITERAL_new:
			case LITERAL_null:
			case LITERAL_true:
			case INC:
			case DEC:
			case BNOT:
			case LNOT:
			case STRING_CTOR_START:
			case NUM_INT:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			case NUM_BIG_INT:
			case NUM_BIG_DECIMAL:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			nls();
			{
			boolean synPredMatched5 = false;
			if (((LA(1)==LITERAL_package||LA(1)==AT) && (_tokenSet_0.member(LA(2))))) {
				int _m5 = mark();
				synPredMatched5 = true;
				inputState.guessing++;
				try {
					{
					annotationsOpt();
					match(LITERAL_package);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched5 = false;
				}
				rewind(_m5);
inputState.guessing--;
			}
			if ( synPredMatched5 ) {
				packageDefinition();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_1.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_import:
				case LITERAL_static:
				case LITERAL_def:
				case LBRACK:
				case IDENT:
				case STRING_LITERAL:
				case LPAREN:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				case LITERAL_this:
				case LITERAL_if:
				case LITERAL_while:
				case LITERAL_switch:
				case LITERAL_for:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case PLUS:
				case MINUS:
				case LITERAL_try:
				case LITERAL_false:
				case LITERAL_new:
				case LITERAL_null:
				case LITERAL_true:
				case INC:
				case DEC:
				case BNOT:
				case LNOT:
				case STRING_CTOR_START:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				{
					statement(EOF);
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			_loop9:
			do {
				if ((LA(1)==SEMI||LA(1)==NLS)) {
					sep();
					{
					switch ( LA(1)) {
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case LITERAL_import:
					case LITERAL_static:
					case LITERAL_def:
					case LBRACK:
					case IDENT:
					case STRING_LITERAL:
					case LPAREN:
					case LITERAL_class:
					case LITERAL_interface:
					case LITERAL_enum:
					case AT:
					case LITERAL_super:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					case LCURLY:
					case LITERAL_this:
					case LITERAL_if:
					case LITERAL_while:
					case LITERAL_switch:
					case LITERAL_for:
					case LITERAL_return:
					case LITERAL_break:
					case LITERAL_continue:
					case LITERAL_throw:
					case LITERAL_assert:
					case PLUS:
					case MINUS:
					case LITERAL_try:
					case LITERAL_false:
					case LITERAL_new:
					case LITERAL_null:
					case LITERAL_true:
					case INC:
					case DEC:
					case BNOT:
					case LNOT:
					case STRING_CTOR_START:
					case NUM_INT:
					case NUM_FLOAT:
					case NUM_LONG:
					case NUM_DOUBLE:
					case NUM_BIG_INT:
					case NUM_BIG_DECIMAL:
					{
						statement(sepToken);
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case EOF:
					case SEMI:
					case NLS:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop9;
				}
				
			} while (true);
			}
			match(Token.EOF_TYPE);
			compilationUnit_AST = (AST)currentAST.root;
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
				// report the error but don't throw away what we've successfully parsed
					reportError(e);
							compilationUnit_AST = (AST)currentAST.root;
				
			} else {
				throw e;
			}
		}
		returnAST = compilationUnit_AST;
	}
	
/** Zero or more insignificant newlines, all gobbled up and thrown away. */
	public final void nls() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nls_AST = null;
		
		{
		if ((LA(1)==NLS) && (_tokenSet_3.member(LA(2)))) {
			match(NLS);
		}
		else if ((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		returnAST = nls_AST;
	}
	
	public final void annotationsOpt() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationsOpt_AST = null;
		Token first = LT(1);
		
		{
		if ((_tokenSet_5.member(LA(1))) && (_tokenSet_6.member(LA(2)))) {
			annotationsInternal();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_7.member(LA(1))) && (_tokenSet_8.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			annotationsOpt_AST = (AST)currentAST.root;
			annotationsOpt_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(ANNOTATIONS,"ANNOTATIONS",first,LT(1))).add(annotationsOpt_AST));
			currentAST.root = annotationsOpt_AST;
			currentAST.child = annotationsOpt_AST!=null &&annotationsOpt_AST.getFirstChild()!=null ?
				annotationsOpt_AST.getFirstChild() : annotationsOpt_AST;
			currentAST.advanceChildToEnd();
		}
		annotationsOpt_AST = (AST)currentAST.root;
		returnAST = annotationsOpt_AST;
	}
	
	public final void packageDefinition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST packageDefinition_AST = null;
		AST an_AST = null;
		AST id_AST = null;
		Token first = LT(1);
		
		annotationsOpt();
		an_AST = (AST)returnAST;
		match(LITERAL_package);
		{
		switch ( LA(1)) {
		case IDENT:
		{
			identifier();
			id_AST = (AST)returnAST;
			break;
		}
		case EOF:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			packageDefinition_AST = (AST)currentAST.root;
			// error recovery for missing package name
			if (id_AST==null) {
							reportError("Invalid package specification",LT(0));
						} else {
			packageDefinition_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(PACKAGE_DEF,"package",first,LT(1))).add(an_AST).add(id_AST));
						}
			
			currentAST.root = packageDefinition_AST;
			currentAST.child = packageDefinition_AST!=null &&packageDefinition_AST.getFirstChild()!=null ?
				packageDefinition_AST.getFirstChild() : packageDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		packageDefinition_AST = (AST)currentAST.root;
		returnAST = packageDefinition_AST;
	}
	
/** A statement is an element of a block.
 *  Typical statements are declarations (which are scoped to the block)
 *  and expressions.
 */
	public final void statement(
		int prevToken
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST statement_AST = null;
		AST pfx_AST = null;
		AST es_AST = null;
		AST ale_AST = null;
		AST ifCbs_AST = null;
		AST elseCbs_AST = null;
		AST while_sce_AST = null;
		Token  s = null;
		AST s_AST = null;
		AST while_cbs_AST = null;
		AST m_AST = null;
		AST switchSce_AST = null;
		AST cg_AST = null;
		AST synch_sce_AST = null;
		AST synch_cs_AST = null;
		boolean sce=false; Token first = LT(1); AST casesGroup_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_if:
			{
				match(LITERAL_if);
				match(LPAREN);
				assignmentLessExpression();
				ale_AST = (AST)returnAST;
				match(RPAREN);
				nlsWarn();
				compatibleBodyStatement();
				ifCbs_AST = (AST)returnAST;
				{
				boolean synPredMatched310 = false;
				if (((_tokenSet_9.member(LA(1))) && (_tokenSet_10.member(LA(2))))) {
					int _m310 = mark();
					synPredMatched310 = true;
					inputState.guessing++;
					try {
						{
						{
						switch ( LA(1)) {
						case SEMI:
						case NLS:
						{
							sep();
							break;
						}
						case LITERAL_else:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						match(LITERAL_else);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched310 = false;
					}
					rewind(_m310);
inputState.guessing--;
				}
				if ( synPredMatched310 ) {
					{
					switch ( LA(1)) {
					case SEMI:
					case NLS:
					{
						sep();
						break;
					}
					case LITERAL_else:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(LITERAL_else);
					nlsWarn();
					compatibleBodyStatement();
					elseCbs_AST = (AST)returnAST;
				}
				else if ((_tokenSet_11.member(LA(1))) && (_tokenSet_12.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					statement_AST = (AST)currentAST.root;
					statement_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(LITERAL_if,"if",first,LT(1))).add(ale_AST).add(ifCbs_AST).add(elseCbs_AST));
					currentAST.root = statement_AST;
					currentAST.child = statement_AST!=null &&statement_AST.getFirstChild()!=null ?
						statement_AST.getFirstChild() : statement_AST;
					currentAST.advanceChildToEnd();
				}
				statement_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_for:
			{
				forStatement();
				astFactory.addASTChild(currentAST, returnAST);
				statement_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_while:
			{
				match(LITERAL_while);
				match(LPAREN);
				sce=strictContextExpression(false);
				while_sce_AST = (AST)returnAST;
				match(RPAREN);
				nlsWarn();
				{
				switch ( LA(1)) {
				case SEMI:
				{
					s = LT(1);
					s_AST = astFactory.create(s);
					match(SEMI);
					break;
				}
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_import:
				case LITERAL_static:
				case LITERAL_def:
				case LBRACK:
				case IDENT:
				case STRING_LITERAL:
				case LPAREN:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				case LITERAL_this:
				case LITERAL_if:
				case LITERAL_while:
				case LITERAL_switch:
				case LITERAL_for:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case PLUS:
				case MINUS:
				case LITERAL_try:
				case LITERAL_false:
				case LITERAL_new:
				case LITERAL_null:
				case LITERAL_true:
				case INC:
				case DEC:
				case BNOT:
				case LNOT:
				case STRING_CTOR_START:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				{
					compatibleBodyStatement();
					while_cbs_AST = (AST)returnAST;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					statement_AST = (AST)currentAST.root;
					
					if (s_AST != null)
					statement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_while,"Literal_while",first,LT(1))).add(while_sce_AST).add(s_AST));
					else
					statement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_while,"Literal_while",first,LT(1))).add(while_sce_AST).add(while_cbs_AST));
					
					currentAST.root = statement_AST;
					currentAST.child = statement_AST!=null &&statement_AST.getFirstChild()!=null ?
						statement_AST.getFirstChild() : statement_AST;
					currentAST.advanceChildToEnd();
				}
				statement_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_switch:
			{
				match(LITERAL_switch);
				match(LPAREN);
				sce=strictContextExpression(false);
				switchSce_AST = (AST)returnAST;
				match(RPAREN);
				nlsWarn();
				match(LCURLY);
				nls();
				{
				_loop316:
				do {
					if ((LA(1)==LITERAL_default||LA(1)==LITERAL_case)) {
						casesGroup();
						cg_AST = (AST)returnAST;
						if ( inputState.guessing==0 ) {
							casesGroup_AST = (AST)astFactory.make( (new ASTArray(3)).add(null).add(casesGroup_AST).add(cg_AST));
						}
					}
					else {
						break _loop316;
					}
					
				} while (true);
				}
				match(RCURLY);
				if ( inputState.guessing==0 ) {
					statement_AST = (AST)currentAST.root;
					statement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_switch,"switch",first,LT(1))).add(switchSce_AST).add(casesGroup_AST));
					currentAST.root = statement_AST;
					currentAST.child = statement_AST!=null &&statement_AST.getFirstChild()!=null ?
						statement_AST.getFirstChild() : statement_AST;
					currentAST.advanceChildToEnd();
				}
				statement_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_try:
			{
				tryBlock();
				astFactory.addASTChild(currentAST, returnAST);
				statement_AST = (AST)currentAST.root;
				break;
			}
			case LITERAL_return:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_throw:
			case LITERAL_assert:
			{
				branchStatement();
				astFactory.addASTChild(currentAST, returnAST);
				statement_AST = (AST)currentAST.root;
				break;
			}
			default:
				boolean synPredMatched297 = false;
				if (((_tokenSet_13.member(LA(1))) && (_tokenSet_14.member(LA(2))))) {
					int _m297 = mark();
					synPredMatched297 = true;
					inputState.guessing++;
					try {
						{
						genericMethodStart();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched297 = false;
					}
					rewind(_m297);
inputState.guessing--;
				}
				if ( synPredMatched297 ) {
					genericMethod();
					astFactory.addASTChild(currentAST, returnAST);
					statement_AST = (AST)currentAST.root;
				}
				else {
					boolean synPredMatched299 = false;
					if (((_tokenSet_13.member(LA(1))) && (_tokenSet_15.member(LA(2))))) {
						int _m299 = mark();
						synPredMatched299 = true;
						inputState.guessing++;
						try {
							{
							multipleAssignmentDeclarationStart();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched299 = false;
						}
						rewind(_m299);
inputState.guessing--;
					}
					if ( synPredMatched299 ) {
						multipleAssignmentDeclaration();
						astFactory.addASTChild(currentAST, returnAST);
						statement_AST = (AST)currentAST.root;
					}
					else {
						boolean synPredMatched301 = false;
						if (((_tokenSet_16.member(LA(1))) && (_tokenSet_17.member(LA(2))))) {
							int _m301 = mark();
							synPredMatched301 = true;
							inputState.guessing++;
							try {
								{
								declarationStart();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched301 = false;
							}
							rewind(_m301);
inputState.guessing--;
						}
						if ( synPredMatched301 ) {
							declaration();
							astFactory.addASTChild(currentAST, returnAST);
							statement_AST = (AST)currentAST.root;
						}
						else {
							boolean synPredMatched303 = false;
							if (((LA(1)==IDENT) && (LA(2)==COLON))) {
								int _m303 = mark();
								synPredMatched303 = true;
								inputState.guessing++;
								try {
									{
									match(IDENT);
									match(COLON);
									}
								}
								catch (RecognitionException pe) {
									synPredMatched303 = false;
								}
								rewind(_m303);
inputState.guessing--;
							}
							if ( synPredMatched303 ) {
								statementLabelPrefix();
								pfx_AST = (AST)returnAST;
								if ( inputState.guessing==0 ) {
									statement_AST = (AST)currentAST.root;
									statement_AST = pfx_AST;
									currentAST.root = statement_AST;
									currentAST.child = statement_AST!=null &&statement_AST.getFirstChild()!=null ?
										statement_AST.getFirstChild() : statement_AST;
									currentAST.advanceChildToEnd();
								}
								{
								boolean synPredMatched306 = false;
								if (((LA(1)==LCURLY) && (_tokenSet_18.member(LA(2))))) {
									int _m306 = mark();
									synPredMatched306 = true;
									inputState.guessing++;
									try {
										{
										match(LCURLY);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched306 = false;
									}
									rewind(_m306);
inputState.guessing--;
								}
								if ( synPredMatched306 ) {
									openOrClosableBlock();
									astFactory.addASTChild(currentAST, returnAST);
								}
								else if ((_tokenSet_19.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
									statement(COLON);
									astFactory.addASTChild(currentAST, returnAST);
								}
								else {
									throw new NoViableAltException(LT(1), getFilename());
								}
								
								}
								statement_AST = (AST)currentAST.root;
							}
							else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
								expressionStatement(prevToken);
								es_AST = (AST)returnAST;
								astFactory.addASTChild(currentAST, returnAST);
								statement_AST = (AST)currentAST.root;
							}
							else {
								boolean synPredMatched314 = false;
								if (((LA(1)==LITERAL_import||LA(1)==AT) && (_tokenSet_21.member(LA(2))))) {
									int _m314 = mark();
									synPredMatched314 = true;
									inputState.guessing++;
									try {
										{
										annotationsOpt();
										match(LITERAL_import);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched314 = false;
									}
									rewind(_m314);
inputState.guessing--;
								}
								if ( synPredMatched314 ) {
									importStatement();
									astFactory.addASTChild(currentAST, returnAST);
									statement_AST = (AST)currentAST.root;
								}
								else if ((_tokenSet_22.member(LA(1))) && (_tokenSet_23.member(LA(2)))) {
									modifiersOpt();
									m_AST = (AST)returnAST;
									typeDefinitionInternal(m_AST);
									astFactory.addASTChild(currentAST, returnAST);
									statement_AST = (AST)currentAST.root;
								}
								else if ((LA(1)==LITERAL_synchronized) && (LA(2)==LPAREN)) {
									match(LITERAL_synchronized);
									match(LPAREN);
									sce=strictContextExpression(false);
									synch_sce_AST = (AST)returnAST;
									match(RPAREN);
									nlsWarn();
									compoundStatement();
									synch_cs_AST = (AST)returnAST;
									if ( inputState.guessing==0 ) {
										statement_AST = (AST)currentAST.root;
										statement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_synchronized,"synchronized",first,LT(1))).add(synch_sce_AST).add(synch_cs_AST));
										currentAST.root = statement_AST;
										currentAST.child = statement_AST!=null &&statement_AST.getFirstChild()!=null ?
											statement_AST.getFirstChild() : statement_AST;
										currentAST.advanceChildToEnd();
									}
									statement_AST = (AST)currentAST.root;
								}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							}}}}}
						}
						catch (RecognitionException e) {
							if (inputState.guessing==0) {
								
								// GRECLIPSE1048
								// If the pfx_AST is not null (i.e. a label was encountered) then attempt recovery if something has gone
								// wrong.  Recovery means reporting the error and then proceeding as best we can.  Basically if the 
								// NoViableAltException hit a problem and the token it encountered was on the same line as the prefix,
								// skip to the end of the line, otherwise assume we can continue from where we are.
								// GRECLIPSE1046
								// two situations to support: 'if (f.) ' where the 'then' condition is missing.  THis is now handled
								// by a recovery rule in then then clause parsing.  And 'if (f.' where even the trailing paren is
								// missing, that is dealt with here by noticing the condition exists but there is no then clause value.
								// we build a basic if clause and soldier on.
								boolean bang = true;
								
								if (pfx_AST!=null) {
									bang=false;	
									reportError(e);
									if (e instanceof NoViableAltException) {
										NoViableAltException nvae = (NoViableAltException)e;
										if (pfx_AST.getLine()==nvae.token.getLine()) {
											consumeUntil(NLS);										
										}
									}
								}
								if (ale_AST!=null && ifCbs_AST==null) {	
									// likely missing close paren
									statement_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(LITERAL_if,"if",first,LT(1))).add(ale_AST).add(ifCbs_AST).add(elseCbs_AST));
									bang=false;
								}
								if (bang) {
									throw e;
								}
								
							} else {
								throw e;
							}
						}
						returnAST = statement_AST;
					}
					
/** A statement separator is either a semicolon or a significant newline.
 *  Any number of additional (insignificant) newlines may accompany it.
 */
	public final void sep() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST sep_AST = null;
		
		switch ( LA(1)) {
		case SEMI:
		{
			match(SEMI);
			{
			_loop584:
			do {
				if ((LA(1)==NLS) && (_tokenSet_24.member(LA(2)))) {
					match(NLS);
				}
				else {
					break _loop584;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				sepToken = SEMI;
			}
			break;
		}
		case NLS:
		{
			match(NLS);
			if ( inputState.guessing==0 ) {
				sepToken = NLS;
			}
			{
			_loop588:
			do {
				if ((LA(1)==SEMI) && (_tokenSet_24.member(LA(2)))) {
					match(SEMI);
					{
					_loop587:
					do {
						if ((LA(1)==NLS) && (_tokenSet_24.member(LA(2)))) {
							match(NLS);
						}
						else {
							break _loop587;
						}
						
					} while (true);
					}
					if ( inputState.guessing==0 ) {
						sepToken = SEMI;
					}
				}
				else {
					break _loop588;
				}
				
			} while (true);
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = sep_AST;
	}
	
/** A Groovy script or simple expression.  Can be anything legal inside {...}. */
	public final void snippetUnit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST snippetUnit_AST = null;
		
		nls();
		blockBody(EOF);
		astFactory.addASTChild(currentAST, returnAST);
		snippetUnit_AST = (AST)currentAST.root;
		returnAST = snippetUnit_AST;
	}
	
/** A block body is a parade of zero or more statements or expressions. */
	public final void blockBody(
		int prevToken
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST blockBody_AST = null;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case AT:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case LITERAL_if:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case PLUS:
		case MINUS:
		case LITERAL_try:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			statement(prevToken);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop291:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_import:
				case LITERAL_static:
				case LITERAL_def:
				case LBRACK:
				case IDENT:
				case STRING_LITERAL:
				case LPAREN:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				case LITERAL_this:
				case LITERAL_if:
				case LITERAL_while:
				case LITERAL_switch:
				case LITERAL_for:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case PLUS:
				case MINUS:
				case LITERAL_try:
				case LITERAL_false:
				case LITERAL_new:
				case LITERAL_null:
				case LITERAL_true:
				case INC:
				case DEC:
				case BNOT:
				case LNOT:
				case STRING_CTOR_START:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				{
					statement(sepToken);
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop291;
			}
			
		} while (true);
		}
		blockBody_AST = (AST)currentAST.root;
		returnAST = blockBody_AST;
	}
	
	public final void identifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifier_AST = null;
		Token  i1 = null;
		AST i1_AST = null;
		Token  d = null;
		AST d_AST = null;
		Token  i2 = null;
		AST i2_AST = null;
		Token first = LT(1);
		
		i1 = LT(1);
		i1_AST = astFactory.create(i1);
		match(IDENT);
		{
		_loop74:
		do {
			if ((LA(1)==DOT)) {
				d = LT(1);
				d_AST = astFactory.create(d);
				match(DOT);
				nls();
				i2 = LT(1);
				i2_AST = astFactory.create(i2);
				match(IDENT);
				if ( inputState.guessing==0 ) {
					i1_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(DOT,".",first,LT(1))).add(i1_AST).add(i2_AST));
				}
			}
			else {
				break _loop74;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			identifier_AST = (AST)currentAST.root;
			identifier_AST = i1_AST;
			currentAST.root = identifier_AST;
			currentAST.child = identifier_AST!=null &&identifier_AST.getFirstChild()!=null ?
				identifier_AST.getFirstChild() : identifier_AST;
			currentAST.advanceChildToEnd();
		}
		identifier_AST = (AST)currentAST.root;
		returnAST = identifier_AST;
	}
	
	public final void importStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST importStatement_AST = null;
		AST an_AST = null;
		AST is_AST = null;
		Token first = LT(1); boolean isStatic = false;
		
		annotationsOpt();
		an_AST = (AST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		match(LITERAL_import);
		{
		switch ( LA(1)) {
		case LITERAL_static:
		{
			match(LITERAL_static);
			if ( inputState.guessing==0 ) {
				isStatic=true;
			}
			break;
		}
		case EOF:
		case IDENT:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case IDENT:
		{
			identifierStar();
			is_AST = (AST)returnAST;
			break;
		}
		case EOF:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			importStatement_AST = (AST)currentAST.root;
			
			if (isStatic) {
			if (is_AST==null) {
			reportError("Invalid import static specification",first);
			importStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(STATIC_IMPORT,"static_import",first,null)).add(an_AST).add(is_AST));
			} else {
			importStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(STATIC_IMPORT,"static_import",first,LT(1))).add(an_AST).add(is_AST));
			}
			} else {
			if (is_AST==null) {
			reportError("Invalid import specification",LT(0));
			importStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(IMPORT,"import",first,null)).add(an_AST).add(is_AST));
			} else {
			importStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(IMPORT,"import",first,LT(1))).add(an_AST).add(is_AST));
			}
			}
			
			currentAST.root = importStatement_AST;
			currentAST.child = importStatement_AST!=null &&importStatement_AST.getFirstChild()!=null ?
				importStatement_AST.getFirstChild() : importStatement_AST;
			currentAST.advanceChildToEnd();
		}
		importStatement_AST = (AST)currentAST.root;
		returnAST = importStatement_AST;
	}
	
	public final void identifierStar() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifierStar_AST = null;
		Token  i1 = null;
		AST i1_AST = null;
		Token  d1 = null;
		AST d1_AST = null;
		Token  i2 = null;
		AST i2_AST = null;
		Token  d2 = null;
		AST d2_AST = null;
		Token  s = null;
		AST s_AST = null;
		Token  alias = null;
		AST alias_AST = null;
		Token first = LT(1); int mark=mark();
		
		try {      // for error handling
			i1 = LT(1);
			i1_AST = astFactory.create(i1);
			match(IDENT);
			{
			_loop77:
			do {
				if ((LA(1)==DOT) && (LA(2)==IDENT||LA(2)==NLS)) {
					d1 = LT(1);
					d1_AST = astFactory.create(d1);
					match(DOT);
					nls();
					i2 = LT(1);
					i2_AST = astFactory.create(i2);
					match(IDENT);
					if ( inputState.guessing==0 ) {
						i1_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(DOT,".",first,LT(1))).add(i1_AST).add(i2_AST));
					}
				}
				else {
					break _loop77;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case DOT:
			{
				d2 = LT(1);
				d2_AST = astFactory.create(d2);
				match(DOT);
				nls();
				s = LT(1);
				s_AST = astFactory.create(s);
				match(STAR);
				if ( inputState.guessing==0 ) {
					i1_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(DOT,".",first,LT(1))).add(i1_AST).add(s_AST));
				}
				break;
			}
			case LITERAL_as:
			{
				match(LITERAL_as);
				nls();
				alias = LT(1);
				alias_AST = astFactory.create(alias);
				match(IDENT);
				if ( inputState.guessing==0 ) {
					i1_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_as,"as",first,LT(1))).add(i1_AST).add(alias_AST));
				}
				break;
			}
			case EOF:
			case RCURLY:
			case SEMI:
			case LITERAL_default:
			case LITERAL_else:
			case LITERAL_case:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				identifierStar_AST = (AST)currentAST.root;
				identifierStar_AST = i1_AST;
				currentAST.root = identifierStar_AST;
				currentAST.child = identifierStar_AST!=null &&identifierStar_AST.getFirstChild()!=null ?
					identifierStar_AST.getFirstChild() : identifierStar_AST;
				currentAST.advanceChildToEnd();
			}
			identifierStar_AST = (AST)currentAST.root;
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
					reportError("Invalid import ",first);
				identifierStar_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(DOT,".",first,LT(1))).add(i1_AST).add((AST)astFactory.make( (new ASTArray(1)).add(create(STAR,"*",null)))));
				// Give up on this line and just go to the next
							rewind(mark);
							consumeUntil(NLS);
				
			} else {
				throw e;
			}
		}
		returnAST = identifierStar_AST;
	}
	
	protected final void typeDefinitionInternal(
		AST mods
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeDefinitionInternal_AST = null;
		AST cd_AST = null;
		AST id_AST = null;
		AST ed_AST = null;
		AST ad_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_class:
		{
			classDefinition(mods);
			cd_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				typeDefinitionInternal_AST = (AST)currentAST.root;
				typeDefinitionInternal_AST = cd_AST;
				currentAST.root = typeDefinitionInternal_AST;
				currentAST.child = typeDefinitionInternal_AST!=null &&typeDefinitionInternal_AST.getFirstChild()!=null ?
					typeDefinitionInternal_AST.getFirstChild() : typeDefinitionInternal_AST;
				currentAST.advanceChildToEnd();
			}
			typeDefinitionInternal_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_interface:
		{
			interfaceDefinition(mods);
			id_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				typeDefinitionInternal_AST = (AST)currentAST.root;
				typeDefinitionInternal_AST = id_AST;
				currentAST.root = typeDefinitionInternal_AST;
				currentAST.child = typeDefinitionInternal_AST!=null &&typeDefinitionInternal_AST.getFirstChild()!=null ?
					typeDefinitionInternal_AST.getFirstChild() : typeDefinitionInternal_AST;
				currentAST.advanceChildToEnd();
			}
			typeDefinitionInternal_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_enum:
		{
			enumDefinition(mods);
			ed_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				typeDefinitionInternal_AST = (AST)currentAST.root;
				typeDefinitionInternal_AST = ed_AST;
				currentAST.root = typeDefinitionInternal_AST;
				currentAST.child = typeDefinitionInternal_AST!=null &&typeDefinitionInternal_AST.getFirstChild()!=null ?
					typeDefinitionInternal_AST.getFirstChild() : typeDefinitionInternal_AST;
				currentAST.advanceChildToEnd();
			}
			typeDefinitionInternal_AST = (AST)currentAST.root;
			break;
		}
		case AT:
		{
			annotationDefinition(mods);
			ad_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				typeDefinitionInternal_AST = (AST)currentAST.root;
				typeDefinitionInternal_AST = ad_AST;
				currentAST.root = typeDefinitionInternal_AST;
				currentAST.child = typeDefinitionInternal_AST!=null &&typeDefinitionInternal_AST.getFirstChild()!=null ?
					typeDefinitionInternal_AST.getFirstChild() : typeDefinitionInternal_AST;
				currentAST.advanceChildToEnd();
			}
			typeDefinitionInternal_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = typeDefinitionInternal_AST;
	}
	
	public final void classDefinition(
		AST modifiers
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classDefinition_AST = null;
		AST tp_AST = null;
		AST sc_AST = null;
		AST ic_AST = null;
		AST cb_AST = null;
		Token first = cloneToken(LT(1));AST prevCurrentClass = currentClass;
		if (modifiers != null) {
		first.setLine(modifiers.getLine());
		first.setColumn(modifiers.getColumn());
		}
		
		match(LITERAL_class);
		AST tmp29_AST = null;
		tmp29_AST = astFactory.create(LT(1));
		match(IDENT);
		nls();
		if ( inputState.guessing==0 ) {
			currentClass = tmp29_AST;
		}
		{
		switch ( LA(1)) {
		case LT:
		{
			typeParameters();
			tp_AST = (AST)returnAST;
			nls();
			break;
		}
		case EOF:
		case LITERAL_extends:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_implements:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		superClassClause();
		sc_AST = (AST)returnAST;
		implementsClause();
		ic_AST = (AST)returnAST;
		{
		switch ( LA(1)) {
		case LCURLY:
		{
			classBlock();
			cb_AST = (AST)returnAST;
			break;
		}
		case EOF:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			classDefinition_AST = (AST)currentAST.root;
			
			if (cb_AST!=null) {
			classDefinition_AST = (AST)astFactory.make( (new ASTArray(7)).add(create(CLASS_DEF,"CLASS_DEF",first,LT(1))).add(modifiers).add(tmp29_AST).add(tp_AST).add(sc_AST).add(ic_AST).add(cb_AST));
					} else {
					  reportError("Malformed class declaration",LT(1));
					  classDefinition_AST = (AST)astFactory.make( (new ASTArray(7)).add(create(CLASS_DEF,"CLASS_DEF",first,LT(1))).add(modifiers).add(tmp29_AST).add(tp_AST).add(sc_AST).add(ic_AST).add(null));    
			}                                            
			
			currentAST.root = classDefinition_AST;
			currentAST.child = classDefinition_AST!=null &&classDefinition_AST.getFirstChild()!=null ?
				classDefinition_AST.getFirstChild() : classDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		if ( inputState.guessing==0 ) {
			currentClass = prevCurrentClass;
		}
		returnAST = classDefinition_AST;
	}
	
	public final void interfaceDefinition(
		AST modifiers
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST interfaceDefinition_AST = null;
		AST tp_AST = null;
		AST ie_AST = null;
		AST ib_AST = null;
		Token first = cloneToken(LT(1));
		if (modifiers != null) {
		first.setLine(modifiers.getLine());
		first.setColumn(modifiers.getColumn());
		}
		
		match(LITERAL_interface);
		AST tmp31_AST = null;
		tmp31_AST = astFactory.create(LT(1));
		match(IDENT);
		nls();
		{
		switch ( LA(1)) {
		case LT:
		{
			typeParameters();
			tp_AST = (AST)returnAST;
			nls();
			break;
		}
		case LITERAL_extends:
		case LCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		interfaceExtends();
		ie_AST = (AST)returnAST;
		interfaceBlock();
		ib_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			interfaceDefinition_AST = (AST)currentAST.root;
			interfaceDefinition_AST = (AST)astFactory.make( (new ASTArray(6)).add(create(INTERFACE_DEF,"INTERFACE_DEF",first,LT(1))).add(modifiers).add(tmp31_AST).add(tp_AST).add(ie_AST).add(ib_AST));
			currentAST.root = interfaceDefinition_AST;
			currentAST.child = interfaceDefinition_AST!=null &&interfaceDefinition_AST.getFirstChild()!=null ?
				interfaceDefinition_AST.getFirstChild() : interfaceDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = interfaceDefinition_AST;
	}
	
	public final void enumDefinition(
		AST modifiers
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumDefinition_AST = null;
		AST ic_AST = null;
		AST eb_AST = null;
		Token first = cloneToken(LT(1)); AST prevCurrentClass = currentClass;
		if (modifiers != null) {
		first.setLine(modifiers.getLine());
		first.setColumn(modifiers.getColumn());
		}
		
		match(LITERAL_enum);
		AST tmp33_AST = null;
		tmp33_AST = astFactory.create(LT(1));
		match(IDENT);
		if ( inputState.guessing==0 ) {
			currentClass = tmp33_AST;
		}
		nls();
		implementsClause();
		ic_AST = (AST)returnAST;
		nls();
		enumBlock();
		eb_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			enumDefinition_AST = (AST)currentAST.root;
			enumDefinition_AST = (AST)astFactory.make( (new ASTArray(5)).add(create(ENUM_DEF,"ENUM_DEF",first,LT(1))).add(modifiers).add(tmp33_AST).add(ic_AST).add(eb_AST));
			currentAST.root = enumDefinition_AST;
			currentAST.child = enumDefinition_AST!=null &&enumDefinition_AST.getFirstChild()!=null ?
				enumDefinition_AST.getFirstChild() : enumDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		if ( inputState.guessing==0 ) {
			currentClass = prevCurrentClass;
		}
		returnAST = enumDefinition_AST;
	}
	
	public final void annotationDefinition(
		AST modifiers
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationDefinition_AST = null;
		AST ab_AST = null;
		Token first = cloneToken(LT(1));
		if (modifiers != null) {
		first.setLine(modifiers.getLine());
		first.setColumn(modifiers.getColumn());
		}
		
		AST tmp34_AST = null;
		tmp34_AST = astFactory.create(LT(1));
		match(AT);
		match(LITERAL_interface);
		AST tmp36_AST = null;
		tmp36_AST = astFactory.create(LT(1));
		match(IDENT);
		annotationBlock();
		ab_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			annotationDefinition_AST = (AST)currentAST.root;
			annotationDefinition_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(ANNOTATION_DEF,"ANNOTATION_DEF",first,LT(1))).add(modifiers).add(tmp36_AST).add(ab_AST));
			currentAST.root = annotationDefinition_AST;
			currentAST.child = annotationDefinition_AST!=null &&annotationDefinition_AST.getFirstChild()!=null ?
				annotationDefinition_AST.getFirstChild() : annotationDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = annotationDefinition_AST;
	}
	
/** A declaration is the creation of a reference or primitive-type variable,
 *  or (if arguments are present) of a method.
 *  Generically, this is called a 'variable' definition, even in the case of a class field or method.
 *  It may start with the modifiers and/or a declaration keyword "def".
 *  It may also start with the modifiers and a capitalized type name.
 *  <p>
 *  AST effect: Create a separate Type/Var tree for each var in the var list.
 *  Must be guarded, as in (declarationStart) => declaration.
 */
	public final void declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST declaration_AST = null;
		AST m_AST = null;
		AST t_AST = null;
		AST v_AST = null;
		AST t2_AST = null;
		AST v2_AST = null;
		
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case AT:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			modifiers();
			m_AST = (AST)returnAST;
			{
			if ((_tokenSet_25.member(LA(1))) && (_tokenSet_26.member(LA(2)))) {
				typeSpec(false);
				t_AST = (AST)returnAST;
			}
			else if ((LA(1)==IDENT||LA(1)==STRING_LITERAL) && (_tokenSet_27.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			variableDefinitions(m_AST, t_AST);
			v_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				declaration_AST = (AST)currentAST.root;
				declaration_AST = v_AST;
				currentAST.root = declaration_AST;
				currentAST.child = declaration_AST!=null &&declaration_AST.getFirstChild()!=null ?
					declaration_AST.getFirstChild() : declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			typeSpec(false);
			t2_AST = (AST)returnAST;
			variableDefinitions(null,t2_AST);
			v2_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				declaration_AST = (AST)currentAST.root;
				declaration_AST = v2_AST;
				currentAST.root = declaration_AST;
				currentAST.child = declaration_AST!=null &&declaration_AST.getFirstChild()!=null ?
					declaration_AST.getFirstChild() : declaration_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = declaration_AST;
	}
	
/** A list of one or more modifier, annotation, or "def". */
	public final void modifiers() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST modifiers_AST = null;
		Token first = LT(1);
		
		modifiersInternal();
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			modifiers_AST = (AST)currentAST.root;
			modifiers_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(MODIFIERS,"MODIFIERS",first,LT(1))).add(modifiers_AST));
			currentAST.root = modifiers_AST;
			currentAST.child = modifiers_AST!=null &&modifiers_AST.getFirstChild()!=null ?
				modifiers_AST.getFirstChild() : modifiers_AST;
			currentAST.advanceChildToEnd();
		}
		modifiers_AST = (AST)currentAST.root;
		returnAST = modifiers_AST;
	}
	
	public final void typeSpec(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeSpec_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			classTypeSpec(addImagNode);
			astFactory.addASTChild(currentAST, returnAST);
			typeSpec_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			builtInTypeSpec(addImagNode);
			astFactory.addASTChild(currentAST, returnAST);
			typeSpec_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = typeSpec_AST;
	}
	
/** The tail of a declaration.
  * Either v1, v2, ... (with possible initializers) or else m(args){body}.
  * The two arguments are the modifier list (if any) and the declaration head (if any).
  * The declaration head is the variable type, or (for a method) the return type.
  * If it is missing, then the variable type is taken from its initializer (if there is one).
  * Otherwise, the variable type defaults to 'any'.
  * DECIDE:  Method return types default to the type of the method body, as an expression.
  */
	public final void variableDefinitions(
		AST mods, AST t
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST variableDefinitions_AST = null;
		Token  id = null;
		AST id_AST = null;
		Token  qid = null;
		AST qid_AST = null;
		AST param_AST = null;
		AST tc_AST = null;
		AST mb_AST = null;
		Token first = cloneToken(LT(1));
		if (mods != null) {
		first.setLine(mods.getLine());
		first.setColumn(mods.getColumn());
		} else if (t != null) {
		first.setLine(t.getLine());
		first.setColumn(t.getColumn());
		}
		
		if ((LA(1)==IDENT) && (_tokenSet_28.member(LA(2)))) {
			listOfVariables(mods,t,first);
			astFactory.addASTChild(currentAST, returnAST);
			variableDefinitions_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==IDENT||LA(1)==STRING_LITERAL) && (LA(2)==LPAREN)) {
			{
			switch ( LA(1)) {
			case IDENT:
			{
				id = LT(1);
				id_AST = astFactory.create(id);
				astFactory.addASTChild(currentAST, id_AST);
				match(IDENT);
				break;
			}
			case STRING_LITERAL:
			{
				qid = LT(1);
				qid_AST = astFactory.create(qid);
				astFactory.addASTChild(currentAST, qid_AST);
				match(STRING_LITERAL);
				if ( inputState.guessing==0 ) {
					qid_AST.setType(IDENT);
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LPAREN);
			parameterDeclarationList();
			param_AST = (AST)returnAST;
			match(RPAREN);
			{
			boolean synPredMatched242 = false;
			if (((LA(1)==LITERAL_throws||LA(1)==NLS) && (_tokenSet_29.member(LA(2))))) {
				int _m242 = mark();
				synPredMatched242 = true;
				inputState.guessing++;
				try {
					{
					nls();
					match(LITERAL_throws);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched242 = false;
				}
				rewind(_m242);
inputState.guessing--;
			}
			if ( synPredMatched242 ) {
				throwsClause();
				tc_AST = (AST)returnAST;
			}
			else if ((_tokenSet_30.member(LA(1))) && (_tokenSet_12.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			boolean synPredMatched245 = false;
			if (((LA(1)==LCURLY||LA(1)==NLS) && (_tokenSet_31.member(LA(2))))) {
				int _m245 = mark();
				synPredMatched245 = true;
				inputState.guessing++;
				try {
					{
					nls();
					match(LCURLY);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched245 = false;
				}
				rewind(_m245);
inputState.guessing--;
			}
			if ( synPredMatched245 ) {
				{
				nlsWarn();
				openBlock();
				mb_AST = (AST)returnAST;
				}
			}
			else if ((_tokenSet_11.member(LA(1))) && (_tokenSet_12.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				variableDefinitions_AST = (AST)currentAST.root;
				if (qid_AST != null)  id_AST = qid_AST;
				variableDefinitions_AST =
				(AST)astFactory.make( (new ASTArray(7)).add(create(METHOD_DEF,"METHOD_DEF",first,LT(1))).add(mods).add((AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(t))).add(id_AST).add(param_AST).add(tc_AST).add(mb_AST));
				
				currentAST.root = variableDefinitions_AST;
				currentAST.child = variableDefinitions_AST!=null &&variableDefinitions_AST.getFirstChild()!=null ?
					variableDefinitions_AST.getFirstChild() : variableDefinitions_AST;
				currentAST.advanceChildToEnd();
			}
			variableDefinitions_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = variableDefinitions_AST;
	}
	
	public final void genericMethod() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST genericMethod_AST = null;
		AST m_AST = null;
		AST p_AST = null;
		AST t_AST = null;
		AST v_AST = null;
		
		modifiers();
		m_AST = (AST)returnAST;
		typeParameters();
		p_AST = (AST)returnAST;
		typeSpec(false);
		t_AST = (AST)returnAST;
		variableDefinitions(m_AST, t_AST);
		v_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			genericMethod_AST = (AST)currentAST.root;
			
			genericMethod_AST = v_AST;
			AST old = v_AST.getFirstChild();
			genericMethod_AST.setFirstChild(p_AST);
			p_AST.setNextSibling(old);
			
			currentAST.root = genericMethod_AST;
			currentAST.child = genericMethod_AST!=null &&genericMethod_AST.getFirstChild()!=null ?
				genericMethod_AST.getFirstChild() : genericMethod_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = genericMethod_AST;
	}
	
	public final void typeParameters() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeParameters_AST = null;
		Token first = LT(1);int currentLtLevel = 0;
		
		if ( inputState.guessing==0 ) {
			currentLtLevel = ltCounter;
		}
		match(LT);
		if ( inputState.guessing==0 ) {
			ltCounter++;
		}
		nls();
		typeParameter();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop113:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				typeParameter();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop113;
			}
			
		} while (true);
		}
		nls();
		{
		switch ( LA(1)) {
		case GT:
		case SR:
		case BSR:
		{
			typeArgumentsOrParametersEnd();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case IDENT:
		case STRING_LITERAL:
		case LITERAL_extends:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_implements:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if (!(matchGenericTypeBrackets(((currentLtLevel != 0) || ltCounter == currentLtLevel),
        "Missing closing bracket '>' for generics types", "Please specify the missing bracket!")))
		  throw new SemanticException("matchGenericTypeBrackets(((currentLtLevel != 0) || ltCounter == currentLtLevel),\r\n        \"Missing closing bracket '>' for generics types\", \"Please specify the missing bracket!\")");
		if ( inputState.guessing==0 ) {
			typeParameters_AST = (AST)currentAST.root;
			typeParameters_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE_PARAMETERS,"TYPE_PARAMETERS",first,LT(1))).add(typeParameters_AST));
			currentAST.root = typeParameters_AST;
			currentAST.child = typeParameters_AST!=null &&typeParameters_AST.getFirstChild()!=null ?
				typeParameters_AST.getFirstChild() : typeParameters_AST;
			currentAST.advanceChildToEnd();
		}
		typeParameters_AST = (AST)currentAST.root;
		returnAST = typeParameters_AST;
	}
	
/** A declaration with one declarator and no initialization, like a parameterDeclaration.
 *  Used to parse loops like <code>for (int x in y)</code> (up to the <code>in</code> keyword).
 */
	public final void singleDeclarationNoInit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST singleDeclarationNoInit_AST = null;
		AST m_AST = null;
		AST t_AST = null;
		AST v_AST = null;
		AST t2_AST = null;
		AST v2_AST = null;
		
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case AT:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			modifiers();
			m_AST = (AST)returnAST;
			{
			if ((_tokenSet_25.member(LA(1))) && (_tokenSet_32.member(LA(2)))) {
				typeSpec(false);
				t_AST = (AST)returnAST;
			}
			else if ((LA(1)==IDENT) && (_tokenSet_33.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			singleVariable(m_AST, t_AST);
			v_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				singleDeclarationNoInit_AST = (AST)currentAST.root;
				singleDeclarationNoInit_AST = v_AST;
				currentAST.root = singleDeclarationNoInit_AST;
				currentAST.child = singleDeclarationNoInit_AST!=null &&singleDeclarationNoInit_AST.getFirstChild()!=null ?
					singleDeclarationNoInit_AST.getFirstChild() : singleDeclarationNoInit_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			typeSpec(false);
			t2_AST = (AST)returnAST;
			singleVariable(null,t2_AST);
			v2_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				singleDeclarationNoInit_AST = (AST)currentAST.root;
				singleDeclarationNoInit_AST = v2_AST;
				currentAST.root = singleDeclarationNoInit_AST;
				currentAST.child = singleDeclarationNoInit_AST!=null &&singleDeclarationNoInit_AST.getFirstChild()!=null ?
					singleDeclarationNoInit_AST.getFirstChild() : singleDeclarationNoInit_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = singleDeclarationNoInit_AST;
	}
	
/** Used in cases where a declaration cannot have commas, or ends with the "in" operator instead of '='. */
	public final void singleVariable(
		AST mods, AST t
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST singleVariable_AST = null;
		AST id_AST = null;
		Token first = LT(1);
		
		variableName();
		id_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			singleVariable_AST = (AST)currentAST.root;
			singleVariable_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(VARIABLE_DEF,"VARIABLE_DEF",first,LT(1))).add(mods).add((AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(t))).add(id_AST));
			currentAST.root = singleVariable_AST;
			currentAST.child = singleVariable_AST!=null &&singleVariable_AST.getFirstChild()!=null ?
				singleVariable_AST.getFirstChild() : singleVariable_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = singleVariable_AST;
	}
	
/** A declaration with one declarator and optional initialization, like a parameterDeclaration.
 *  Used to parse declarations used for both binding and effect, in places like argument
 *  lists and <code>while</code> statements.
 */
	public final void singleDeclaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST singleDeclaration_AST = null;
		AST sd_AST = null;
		
		singleDeclarationNoInit();
		sd_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			singleDeclaration_AST = (AST)currentAST.root;
			singleDeclaration_AST = sd_AST;
			currentAST.root = singleDeclaration_AST;
			currentAST.child = singleDeclaration_AST!=null &&singleDeclaration_AST.getFirstChild()!=null ?
				singleDeclaration_AST.getFirstChild() : singleDeclaration_AST;
			currentAST.advanceChildToEnd();
		}
		{
		switch ( LA(1)) {
		case ASSIGN:
		{
			varInitializer();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case RBRACK:
		case COMMA:
		case RPAREN:
		case SEMI:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		singleDeclaration_AST = (AST)currentAST.root;
		returnAST = singleDeclaration_AST;
	}
	
/** An assignment operator '=' followed by an expression.  (Never empty.) */
	public final void varInitializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST varInitializer_AST = null;
		
		AST tmp41_AST = null;
		tmp41_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp41_AST);
		match(ASSIGN);
		nls();
		expressionStatementNoCheck();
		astFactory.addASTChild(currentAST, returnAST);
		varInitializer_AST = (AST)currentAST.root;
		returnAST = varInitializer_AST;
	}
	
/** Used only as a lookahead predicate, before diving in and parsing a declaration.
 *  A declaration can be unambiguously introduced with "def", an annotation or a modifier token like "final".
 *  It may also be introduced by a simple identifier whose first character is an uppercase letter,
 *  as in {String x}.  A declaration can also be introduced with a built in type like 'int' or 'void'.
 *  Brackets (array and generic) are allowed, as in {List[] x} or {int[][] y}.
 *  Anything else is parsed as a statement of some sort (expression or command).
 *  <p>
 *  (In the absence of explicit method-call parens, we assume a capitalized name is a type name.
 *  Yes, this is a little hacky.  Alternatives are to complicate the declaration or command
 *  syntaxes, or to have the parser query the symbol table.  Parse-time queries are evil.
 *  And we want both {String x} and {println x}.  So we need a syntactic razor-edge to slip
 *  between 'println' and 'String'.)
 *
 *   *TODO* The declarationStart production needs to be strengthened to recognize
 *  things like {List<String> foo}.
 *  Right now it only knows how to skip square brackets after the type, not
 *  angle brackets.
 *  This probably turns out to be tricky because of >> vs. > >. If so,
 *  just put a TODO comment in.
 */
	public final void declarationStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST declarationStart_AST = null;
		
		{
		int _cnt31=0;
		_loop31:
		do {
			switch ( LA(1)) {
			case LITERAL_def:
			{
				{
				match(LITERAL_def);
				nls();
				}
				break;
			}
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			{
				modifier();
				nls();
				break;
			}
			case AT:
			{
				annotation();
				nls();
				break;
			}
			default:
				if ((_tokenSet_25.member(LA(1))) && (_tokenSet_34.member(LA(2)))) {
					{
					if ((LA(1)==IDENT) && (_tokenSet_35.member(LA(2)))) {
						upperCaseIdent();
					}
					else if (((LA(1) >= LITERAL_void && LA(1) <= LITERAL_double))) {
						builtInType();
					}
					else if ((LA(1)==IDENT) && (LA(2)==DOT)) {
						qualifiedTypeName();
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					{
					switch ( LA(1)) {
					case LT:
					{
						typeArguments();
						break;
					}
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case LITERAL_static:
					case LITERAL_def:
					case LBRACK:
					case IDENT:
					case STRING_LITERAL:
					case AT:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					_loop30:
					do {
						if ((LA(1)==LBRACK)) {
							AST tmp43_AST = null;
							tmp43_AST = astFactory.create(LT(1));
							match(LBRACK);
							balancedTokens();
							AST tmp44_AST = null;
							tmp44_AST = astFactory.create(LT(1));
							match(RBRACK);
						}
						else {
							break _loop30;
						}
						
					} while (true);
					}
				}
			else {
				if ( _cnt31>=1 ) { break _loop31; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			}
			_cnt31++;
		} while (true);
		}
		{
		switch ( LA(1)) {
		case IDENT:
		{
			AST tmp45_AST = null;
			tmp45_AST = astFactory.create(LT(1));
			match(IDENT);
			break;
		}
		case STRING_LITERAL:
		{
			AST tmp46_AST = null;
			tmp46_AST = astFactory.create(LT(1));
			match(STRING_LITERAL);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = declarationStart_AST;
	}
	
	public final void modifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST modifier_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_private:
		{
			AST tmp47_AST = null;
			tmp47_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp47_AST);
			match(LITERAL_private);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_public:
		{
			AST tmp48_AST = null;
			tmp48_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp48_AST);
			match(LITERAL_public);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_protected:
		{
			AST tmp49_AST = null;
			tmp49_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp49_AST);
			match(LITERAL_protected);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_static:
		{
			AST tmp50_AST = null;
			tmp50_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp50_AST);
			match(LITERAL_static);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_transient:
		{
			AST tmp51_AST = null;
			tmp51_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp51_AST);
			match(LITERAL_transient);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case FINAL:
		{
			AST tmp52_AST = null;
			tmp52_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp52_AST);
			match(FINAL);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case ABSTRACT:
		{
			AST tmp53_AST = null;
			tmp53_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp53_AST);
			match(ABSTRACT);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_native:
		{
			AST tmp54_AST = null;
			tmp54_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp54_AST);
			match(LITERAL_native);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_threadsafe:
		{
			AST tmp55_AST = null;
			tmp55_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp55_AST);
			match(LITERAL_threadsafe);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_synchronized:
		{
			AST tmp56_AST = null;
			tmp56_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp56_AST);
			match(LITERAL_synchronized);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_volatile:
		{
			AST tmp57_AST = null;
			tmp57_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp57_AST);
			match(LITERAL_volatile);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		case STRICTFP:
		{
			AST tmp58_AST = null;
			tmp58_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp58_AST);
			match(STRICTFP);
			modifier_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = modifier_AST;
	}
	
	public final void annotation() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotation_AST = null;
		AST i_AST = null;
		AST args_AST = null;
		Token first = LT(1);
		
		match(AT);
		identifier();
		i_AST = (AST)returnAST;
		nls();
		{
		if ((LA(1)==LPAREN) && (_tokenSet_36.member(LA(2)))) {
			match(LPAREN);
			{
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case UNUSED_GOTO:
			case UNUSED_CONST:
			case UNUSED_DO:
			case STRICTFP:
			case LITERAL_package:
			case LITERAL_import:
			case LITERAL_static:
			case LITERAL_def:
			case LBRACK:
			case IDENT:
			case STRING_LITERAL:
			case LPAREN:
			case LITERAL_class:
			case LITERAL_interface:
			case LITERAL_enum:
			case AT:
			case LITERAL_extends:
			case LITERAL_super:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_as:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			case LITERAL_default:
			case LITERAL_throws:
			case LITERAL_implements:
			case LITERAL_this:
			case LITERAL_if:
			case LITERAL_else:
			case LITERAL_while:
			case LITERAL_switch:
			case LITERAL_for:
			case LITERAL_in:
			case LITERAL_return:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_throw:
			case LITERAL_assert:
			case PLUS:
			case MINUS:
			case LITERAL_case:
			case LITERAL_try:
			case LITERAL_finally:
			case LITERAL_catch:
			case LITERAL_false:
			case LITERAL_instanceof:
			case LITERAL_new:
			case LITERAL_null:
			case LITERAL_true:
			case INC:
			case DEC:
			case BNOT:
			case LNOT:
			case STRING_CTOR_START:
			case NUM_INT:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			case NUM_BIG_INT:
			case NUM_BIG_DECIMAL:
			{
				annotationArguments();
				args_AST = (AST)returnAST;
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
		}
		else if ((_tokenSet_37.member(LA(1))) && (_tokenSet_38.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			annotation_AST = (AST)currentAST.root;
			annotation_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(ANNOTATION,"ANNOTATION",first,LT(1))).add(i_AST).add(args_AST));
			currentAST.root = annotation_AST;
			currentAST.child = annotation_AST!=null &&annotation_AST.getFirstChild()!=null ?
				annotation_AST.getFirstChild() : annotation_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = annotation_AST;
	}
	
/** An IDENT token whose spelling is required to start with an uppercase letter.
 *  In the case of a simple statement {UpperID name} the identifier is taken to be a type name, not a command name.
 */
	public final void upperCaseIdent() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST upperCaseIdent_AST = null;
		
		if (!(isUpperCase(LT(1))))
		  throw new SemanticException("isUpperCase(LT(1))");
		AST tmp62_AST = null;
		tmp62_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp62_AST);
		match(IDENT);
		upperCaseIdent_AST = (AST)currentAST.root;
		returnAST = upperCaseIdent_AST;
	}
	
	public final void builtInType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST builtInType_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_void:
		{
			AST tmp63_AST = null;
			tmp63_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp63_AST);
			match(LITERAL_void);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_boolean:
		{
			AST tmp64_AST = null;
			tmp64_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp64_AST);
			match(LITERAL_boolean);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_byte:
		{
			AST tmp65_AST = null;
			tmp65_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp65_AST);
			match(LITERAL_byte);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_char:
		{
			AST tmp66_AST = null;
			tmp66_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp66_AST);
			match(LITERAL_char);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_short:
		{
			AST tmp67_AST = null;
			tmp67_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp67_AST);
			match(LITERAL_short);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_int:
		{
			AST tmp68_AST = null;
			tmp68_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp68_AST);
			match(LITERAL_int);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_float:
		{
			AST tmp69_AST = null;
			tmp69_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp69_AST);
			match(LITERAL_float);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_long:
		{
			AST tmp70_AST = null;
			tmp70_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp70_AST);
			match(LITERAL_long);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_double:
		{
			AST tmp71_AST = null;
			tmp71_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp71_AST);
			match(LITERAL_double);
			builtInType_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = builtInType_AST;
	}
	
	public final void qualifiedTypeName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST qualifiedTypeName_AST = null;
		
		AST tmp72_AST = null;
		tmp72_AST = astFactory.create(LT(1));
		match(IDENT);
		AST tmp73_AST = null;
		tmp73_AST = astFactory.create(LT(1));
		match(DOT);
		{
		_loop38:
		do {
			if ((LA(1)==IDENT) && (LA(2)==DOT)) {
				AST tmp74_AST = null;
				tmp74_AST = astFactory.create(LT(1));
				match(IDENT);
				AST tmp75_AST = null;
				tmp75_AST = astFactory.create(LT(1));
				match(DOT);
			}
			else {
				break _loop38;
			}
			
		} while (true);
		}
		upperCaseIdent();
		returnAST = qualifiedTypeName_AST;
	}
	
	public final void typeArguments() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArguments_AST = null;
		Token first = LT(1);
		int currentLtLevel = 0;
		
		if ( inputState.guessing==0 ) {
			currentLtLevel = ltCounter;
		}
		match(LT);
		if ( inputState.guessing==0 ) {
			ltCounter++;
		}
		nls();
		typeArgument();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop60:
		do {
			if (((LA(1)==COMMA) && (_tokenSet_39.member(LA(2))))&&(inputState.guessing !=0 || ltCounter == currentLtLevel + 1)) {
				match(COMMA);
				nls();
				typeArgument();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop60;
			}
			
		} while (true);
		}
		nls();
		{
		if ((_tokenSet_40.member(LA(1))) && (_tokenSet_41.member(LA(2)))) {
			typeArgumentsOrParametersEnd();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_41.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if (!(matchGenericTypeBrackets(((currentLtLevel != 0) || ltCounter == currentLtLevel),
        "Missing closing bracket '>' for generics types", "Please specify the missing bracket!")))
		  throw new SemanticException("matchGenericTypeBrackets(((currentLtLevel != 0) || ltCounter == currentLtLevel),\r\n        \"Missing closing bracket '>' for generics types\", \"Please specify the missing bracket!\")");
		if ( inputState.guessing==0 ) {
			typeArguments_AST = (AST)currentAST.root;
			typeArguments_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE_ARGUMENTS,"TYPE_ARGUMENTS",first,LT(1))).add(typeArguments_AST));
			currentAST.root = typeArguments_AST;
			currentAST.child = typeArguments_AST!=null &&typeArguments_AST.getFirstChild()!=null ?
				typeArguments_AST.getFirstChild() : typeArguments_AST;
			currentAST.advanceChildToEnd();
		}
		typeArguments_AST = (AST)currentAST.root;
		returnAST = typeArguments_AST;
	}
	
	public final void balancedTokens() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST balancedTokens_AST = null;
		
		{
		_loop581:
		do {
			if ((_tokenSet_42.member(LA(1)))) {
				balancedBrackets();
			}
			else if ((_tokenSet_43.member(LA(1)))) {
				{
				match(_tokenSet_43);
				}
			}
			else {
				break _loop581;
			}
			
		} while (true);
		}
		returnAST = balancedTokens_AST;
	}
	
/**
 * lookahead predicate for usage of generics in methods
 * as parameter for the method. Example:
 * static <T> T foo(){}
 * <T> must be first after the modifier.
 * This rule allows more and does no exact match, but it
 * is only a lookahead, not the real rule.
 */
	public final void genericMethodStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST genericMethodStart_AST = null;
		
		{
		int _cnt35=0;
		_loop35:
		do {
			switch ( LA(1)) {
			case LITERAL_def:
			{
				match(LITERAL_def);
				nls();
				break;
			}
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			{
				modifier();
				nls();
				break;
			}
			case AT:
			{
				annotation();
				nls();
				break;
			}
			default:
			{
				if ( _cnt35>=1 ) { break _loop35; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			}
			_cnt35++;
		} while (true);
		}
		AST tmp80_AST = null;
		tmp80_AST = astFactory.create(LT(1));
		match(LT);
		returnAST = genericMethodStart_AST;
	}
	
/** Used to look ahead for a constructor
 */
	public final void constructorStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constructorStart_AST = null;
		Token  id = null;
		AST id_AST = null;
		
		modifiersOpt();
		id = LT(1);
		id_AST = astFactory.create(id);
		match(IDENT);
		if (!(isConstructorIdent(id)))
		  throw new SemanticException("isConstructorIdent(id)");
		nls();
		match(LPAREN);
		returnAST = constructorStart_AST;
	}
	
/** A list of zero or more modifiers, annotations, or "def". */
	public final void modifiersOpt() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST modifiersOpt_AST = null;
		Token first = LT(1);
		
		{
		if ((_tokenSet_13.member(LA(1))) && (_tokenSet_44.member(LA(2)))) {
			modifiersInternal();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_45.member(LA(1))) && (_tokenSet_46.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			modifiersOpt_AST = (AST)currentAST.root;
			modifiersOpt_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(MODIFIERS,"MODIFIERS",first,LT(1))).add(modifiersOpt_AST));
			currentAST.root = modifiersOpt_AST;
			currentAST.child = modifiersOpt_AST!=null &&modifiersOpt_AST.getFirstChild()!=null ?
				modifiersOpt_AST.getFirstChild() : modifiersOpt_AST;
			currentAST.advanceChildToEnd();
		}
		modifiersOpt_AST = (AST)currentAST.root;
		returnAST = modifiersOpt_AST;
	}
	
/** Used only as a lookahead predicate for nested type declarations. */
	public final void typeDeclarationStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeDeclarationStart_AST = null;
		
		modifiersOpt();
		{
		switch ( LA(1)) {
		case LITERAL_class:
		{
			match(LITERAL_class);
			break;
		}
		case LITERAL_interface:
		{
			match(LITERAL_interface);
			break;
		}
		case LITERAL_enum:
		{
			match(LITERAL_enum);
			break;
		}
		case AT:
		{
			AST tmp85_AST = null;
			tmp85_AST = astFactory.create(LT(1));
			match(AT);
			match(LITERAL_interface);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = typeDeclarationStart_AST;
	}
	
	public final void classTypeSpec(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classTypeSpec_AST = null;
		AST ct_AST = null;
		Token first = LT(1);
		
		classOrInterfaceType(false);
		ct_AST = (AST)returnAST;
		declaratorBrackets(ct_AST);
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			classTypeSpec_AST = (AST)currentAST.root;
			
			if ( addImagNode ) {
			classTypeSpec_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(classTypeSpec_AST));
			}
			
			currentAST.root = classTypeSpec_AST;
			currentAST.child = classTypeSpec_AST!=null &&classTypeSpec_AST.getFirstChild()!=null ?
				classTypeSpec_AST.getFirstChild() : classTypeSpec_AST;
			currentAST.advanceChildToEnd();
		}
		classTypeSpec_AST = (AST)currentAST.root;
		returnAST = classTypeSpec_AST;
	}
	
	public final void builtInTypeSpec(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST builtInTypeSpec_AST = null;
		AST bt_AST = null;
		Token first = LT(1);
		
		builtInType();
		bt_AST = (AST)returnAST;
		declaratorBrackets(bt_AST);
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			builtInTypeSpec_AST = (AST)currentAST.root;
			
			if ( addImagNode ) {
			builtInTypeSpec_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(builtInTypeSpec_AST));
			}
			
			currentAST.root = builtInTypeSpec_AST;
			currentAST.child = builtInTypeSpec_AST!=null &&builtInTypeSpec_AST.getFirstChild()!=null ?
				builtInTypeSpec_AST.getFirstChild() : builtInTypeSpec_AST;
			currentAST.advanceChildToEnd();
		}
		builtInTypeSpec_AST = (AST)currentAST.root;
		returnAST = builtInTypeSpec_AST;
	}
	
	public final void classOrInterfaceType(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classOrInterfaceType_AST = null;
		Token  i1 = null;
		AST i1_AST = null;
		Token  d = null;
		AST d_AST = null;
		Token  i2 = null;
		AST i2_AST = null;
		AST ta_AST = null;
		Token first = LT(1);
		
		i1 = LT(1);
		i1_AST = astFactory.create(i1);
		astFactory.makeASTRoot(currentAST, i1_AST);
		match(IDENT);
		{
		if ((LA(1)==LT) && (_tokenSet_39.member(LA(2)))) {
			typeArguments();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((LA(1)==LT) && (LA(2)==GT)) {
			typeArgumentsDiamond();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_41.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		{
		_loop49:
		do {
			if ((LA(1)==DOT) && (LA(2)==IDENT)) {
				d = LT(1);
				d_AST = astFactory.create(d);
				match(DOT);
				i2 = LT(1);
				i2_AST = astFactory.create(i2);
				match(IDENT);
				{
				switch ( LA(1)) {
				case LT:
				{
					typeArguments();
					ta_AST = (AST)returnAST;
					break;
				}
				case EOF:
				case FINAL:
				case ABSTRACT:
				case UNUSED_GOTO:
				case UNUSED_CONST:
				case UNUSED_DO:
				case STRICTFP:
				case LITERAL_package:
				case LITERAL_import:
				case LITERAL_static:
				case LITERAL_def:
				case LBRACK:
				case RBRACK:
				case IDENT:
				case STRING_LITERAL:
				case DOT:
				case LPAREN:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case QUESTION:
				case LITERAL_extends:
				case LITERAL_super:
				case GT:
				case COMMA:
				case SR:
				case BSR:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_as:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case RPAREN:
				case ASSIGN:
				case BAND:
				case LCURLY:
				case RCURLY:
				case SEMI:
				case LITERAL_default:
				case LITERAL_throws:
				case LITERAL_implements:
				case LITERAL_this:
				case TRIPLE_DOT:
				case BOR:
				case CLOSABLE_BLOCK_OP:
				case COLON:
				case LITERAL_if:
				case LITERAL_else:
				case LITERAL_while:
				case LITERAL_switch:
				case LITERAL_for:
				case LITERAL_in:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case PLUS:
				case MINUS:
				case LITERAL_case:
				case LITERAL_try:
				case LITERAL_finally:
				case LITERAL_catch:
				case LITERAL_false:
				case LITERAL_instanceof:
				case LITERAL_new:
				case LITERAL_null:
				case LITERAL_true:
				case PLUS_ASSIGN:
				case MINUS_ASSIGN:
				case STAR_ASSIGN:
				case DIV_ASSIGN:
				case MOD_ASSIGN:
				case SR_ASSIGN:
				case BSR_ASSIGN:
				case SL_ASSIGN:
				case BAND_ASSIGN:
				case BXOR_ASSIGN:
				case BOR_ASSIGN:
				case STAR_STAR_ASSIGN:
				case ELVIS_OPERATOR:
				case LOR:
				case LAND:
				case BXOR:
				case REGEX_FIND:
				case REGEX_MATCH:
				case NOT_EQUAL:
				case EQUAL:
				case IDENTICAL:
				case NOT_IDENTICAL:
				case COMPARE_TO:
				case INC:
				case DEC:
				case BNOT:
				case LNOT:
				case STRING_CTOR_START:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					i1_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(DOT,".",first,LT(1))).add(i1_AST).add(i2_AST).add(ta_AST));
				}
			}
			else {
				break _loop49;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			classOrInterfaceType_AST = (AST)currentAST.root;
			
			classOrInterfaceType_AST = i1_AST;
			if ( addImagNode ) {
			classOrInterfaceType_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(classOrInterfaceType_AST));
			}
			
			currentAST.root = classOrInterfaceType_AST;
			currentAST.child = classOrInterfaceType_AST!=null &&classOrInterfaceType_AST.getFirstChild()!=null ?
				classOrInterfaceType_AST.getFirstChild() : classOrInterfaceType_AST;
			currentAST.advanceChildToEnd();
		}
		classOrInterfaceType_AST = (AST)currentAST.root;
		returnAST = classOrInterfaceType_AST;
	}
	
/** After some type names, where zero or more empty bracket pairs are allowed.
 *  We use ARRAY_DECLARATOR to represent this.
 *  TODO:  Is there some more Groovy way to view this in terms of the indexed property syntax?
 */
	public final void declaratorBrackets(
		AST typ
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST declaratorBrackets_AST = null;
		
		if ( inputState.guessing==0 ) {
			declaratorBrackets_AST = (AST)currentAST.root;
			declaratorBrackets_AST=typ;
			currentAST.root = declaratorBrackets_AST;
			currentAST.child = declaratorBrackets_AST!=null &&declaratorBrackets_AST.getFirstChild()!=null ?
				declaratorBrackets_AST.getFirstChild() : declaratorBrackets_AST;
			currentAST.advanceChildToEnd();
		}
		{
		_loop257:
		do {
			if ((LA(1)==LBRACK) && (LA(2)==RBRACK)) {
				match(LBRACK);
				match(RBRACK);
				if ( inputState.guessing==0 ) {
					declaratorBrackets_AST = (AST)currentAST.root;
					declaratorBrackets_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(ARRAY_DECLARATOR,"[",typ,LT(1))).add(declaratorBrackets_AST));
					currentAST.root = declaratorBrackets_AST;
					currentAST.child = declaratorBrackets_AST!=null &&declaratorBrackets_AST.getFirstChild()!=null ?
						declaratorBrackets_AST.getFirstChild() : declaratorBrackets_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				break _loop257;
			}
			
		} while (true);
		}
		declaratorBrackets_AST = (AST)currentAST.root;
		returnAST = declaratorBrackets_AST;
	}
	
	public final void typeArgumentsDiamond() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgumentsDiamond_AST = null;
		Token first = LT(1);
		
		match(LT);
		match(GT);
		nls();
		if ( inputState.guessing==0 ) {
			typeArgumentsDiamond_AST = (AST)currentAST.root;
			typeArgumentsDiamond_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE_ARGUMENTS,"TYPE_ARGUMENTS",first,LT(1))).add(typeArgumentsDiamond_AST));
			currentAST.root = typeArgumentsDiamond_AST;
			currentAST.child = typeArgumentsDiamond_AST!=null &&typeArgumentsDiamond_AST.getFirstChild()!=null ?
				typeArgumentsDiamond_AST.getFirstChild() : typeArgumentsDiamond_AST;
			currentAST.advanceChildToEnd();
		}
		typeArgumentsDiamond_AST = (AST)currentAST.root;
		returnAST = typeArgumentsDiamond_AST;
	}
	
	public final void typeArgumentSpec() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgumentSpec_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			classTypeSpec(true);
			astFactory.addASTChild(currentAST, returnAST);
			typeArgumentSpec_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			builtInTypeArraySpec(true);
			astFactory.addASTChild(currentAST, returnAST);
			typeArgumentSpec_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = typeArgumentSpec_AST;
	}
	
	public final void builtInTypeArraySpec(
		boolean addImagNode
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST builtInTypeArraySpec_AST = null;
		AST bt_AST = null;
		Token first = LT(1);
		
		builtInType();
		bt_AST = (AST)returnAST;
		{
		boolean synPredMatched68 = false;
		if (((_tokenSet_41.member(LA(1))) && (_tokenSet_4.member(LA(2))))) {
			int _m68 = mark();
			synPredMatched68 = true;
			inputState.guessing++;
			try {
				{
				match(LBRACK);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched68 = false;
			}
			rewind(_m68);
inputState.guessing--;
		}
		if ( synPredMatched68 ) {
			declaratorBrackets(bt_AST);
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_41.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
			if ( inputState.guessing==0 ) {
				require(false,
				"primitive type parameters not allowed here",
				"use the corresponding wrapper type, such as Integer for int"
				);
			}
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			builtInTypeArraySpec_AST = (AST)currentAST.root;
			
			if ( addImagNode ) {
			builtInTypeArraySpec_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(builtInTypeArraySpec_AST));
			}
			
			currentAST.root = builtInTypeArraySpec_AST;
			currentAST.child = builtInTypeArraySpec_AST!=null &&builtInTypeArraySpec_AST.getFirstChild()!=null ?
				builtInTypeArraySpec_AST.getFirstChild() : builtInTypeArraySpec_AST;
			currentAST.advanceChildToEnd();
		}
		builtInTypeArraySpec_AST = (AST)currentAST.root;
		returnAST = builtInTypeArraySpec_AST;
	}
	
	public final void typeArgument() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgument_AST = null;
		Token first = LT(1);
		
		{
		switch ( LA(1)) {
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			typeArgumentSpec();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case QUESTION:
		{
			wildcardType();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			typeArgument_AST = (AST)currentAST.root;
			typeArgument_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE_ARGUMENT,"TYPE_ARGUMENT",first,LT(1))).add(typeArgument_AST));
			currentAST.root = typeArgument_AST;
			currentAST.child = typeArgument_AST!=null &&typeArgument_AST.getFirstChild()!=null ?
				typeArgument_AST.getFirstChild() : typeArgument_AST;
			currentAST.advanceChildToEnd();
		}
		typeArgument_AST = (AST)currentAST.root;
		returnAST = typeArgument_AST;
	}
	
	public final void wildcardType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST wildcardType_AST = null;
		
		AST tmp91_AST = null;
		tmp91_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp91_AST);
		match(QUESTION);
		{
		boolean synPredMatched56 = false;
		if (((LA(1)==LITERAL_extends||LA(1)==LITERAL_super) && (LA(2)==IDENT||LA(2)==NLS))) {
			int _m56 = mark();
			synPredMatched56 = true;
			inputState.guessing++;
			try {
				{
				switch ( LA(1)) {
				case LITERAL_extends:
				{
					match(LITERAL_extends);
					break;
				}
				case LITERAL_super:
				{
					match(LITERAL_super);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched56 = false;
			}
			rewind(_m56);
inputState.guessing--;
		}
		if ( synPredMatched56 ) {
			typeArgumentBounds();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_41.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			wildcardType_AST = (AST)currentAST.root;
			wildcardType_AST.setType(WILDCARD_TYPE);
		}
		wildcardType_AST = (AST)currentAST.root;
		returnAST = wildcardType_AST;
	}
	
	public final void typeArgumentBounds() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgumentBounds_AST = null;
		Token first = LT(1);boolean isUpperBounds = false;
		
		{
		switch ( LA(1)) {
		case LITERAL_extends:
		{
			match(LITERAL_extends);
			if ( inputState.guessing==0 ) {
				isUpperBounds=true;
			}
			break;
		}
		case LITERAL_super:
		{
			match(LITERAL_super);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		nls();
		classOrInterfaceType(true);
		astFactory.addASTChild(currentAST, returnAST);
		nls();
		if ( inputState.guessing==0 ) {
			typeArgumentBounds_AST = (AST)currentAST.root;
			
			if (isUpperBounds)
			{
			typeArgumentBounds_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS",first,LT(1))).add(typeArgumentBounds_AST));
			}
			else
			{
			typeArgumentBounds_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE_LOWER_BOUNDS,"TYPE_LOWER_BOUNDS",first,LT(1))).add(typeArgumentBounds_AST));
			}
			
			currentAST.root = typeArgumentBounds_AST;
			currentAST.child = typeArgumentBounds_AST!=null &&typeArgumentBounds_AST.getFirstChild()!=null ?
				typeArgumentBounds_AST.getFirstChild() : typeArgumentBounds_AST;
			currentAST.advanceChildToEnd();
		}
		typeArgumentBounds_AST = (AST)currentAST.root;
		returnAST = typeArgumentBounds_AST;
	}
	
	protected final void typeArgumentsOrParametersEnd() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeArgumentsOrParametersEnd_AST = null;
		
		switch ( LA(1)) {
		case GT:
		{
			match(GT);
			if ( inputState.guessing==0 ) {
				ltCounter-=1;
			}
			typeArgumentsOrParametersEnd_AST = (AST)currentAST.root;
			break;
		}
		case SR:
		{
			match(SR);
			if ( inputState.guessing==0 ) {
				ltCounter-=2;
			}
			typeArgumentsOrParametersEnd_AST = (AST)currentAST.root;
			break;
		}
		case BSR:
		{
			match(BSR);
			if ( inputState.guessing==0 ) {
				ltCounter-=3;
			}
			typeArgumentsOrParametersEnd_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = typeArgumentsOrParametersEnd_AST;
	}
	
	public final void type() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST type_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			classOrInterfaceType(false);
			astFactory.addASTChild(currentAST, returnAST);
			type_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			builtInType();
			astFactory.addASTChild(currentAST, returnAST);
			type_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = type_AST;
	}
	
	public final void modifiersInternal() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST modifiersInternal_AST = null;
		int seenDef = 0;
		
		{
		int _cnt81=0;
		_loop81:
		do {
			if (((LA(1)==LITERAL_def))&&(seenDef++ == 0)) {
				match(LITERAL_def);
				nls();
			}
			else if ((_tokenSet_47.member(LA(1)))) {
				modifier();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
			}
			else if ((LA(1)==AT) && (LA(2)==LITERAL_interface)) {
				if ( inputState.guessing==0 ) {
					break; /* go out of the ()+ loop*/
				}
				AST tmp98_AST = null;
				tmp98_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp98_AST);
				match(AT);
				AST tmp99_AST = null;
				tmp99_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp99_AST);
				match(LITERAL_interface);
			}
			else if ((LA(1)==AT) && (LA(2)==IDENT)) {
				annotation();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
			}
			else {
				if ( _cnt81>=1 ) { break _loop81; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt81++;
		} while (true);
		}
		modifiersInternal_AST = (AST)currentAST.root;
		returnAST = modifiersInternal_AST;
	}
	
	public final void annotationArguments() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationArguments_AST = null;
		AST v_AST = null;
		
		if ((_tokenSet_48.member(LA(1))) && (_tokenSet_49.member(LA(2)))) {
			annotationMemberValueInitializer();
			v_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				annotationArguments_AST = (AST)currentAST.root;
				Token itkn = new Token(IDENT, "value");
				AST i;
				i = (AST)astFactory.make( (new ASTArray(1)).add(create(IDENT,"value",itkn,itkn)));
				annotationArguments_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR",LT(1),LT(1))).add(i).add(v_AST));
				currentAST.root = annotationArguments_AST;
				currentAST.child = annotationArguments_AST!=null &&annotationArguments_AST.getFirstChild()!=null ?
					annotationArguments_AST.getFirstChild() : annotationArguments_AST;
				currentAST.advanceChildToEnd();
			}
			annotationArguments_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_50.member(LA(1))) && (LA(2)==ASSIGN)) {
			annotationMemberValuePairs();
			astFactory.addASTChild(currentAST, returnAST);
			annotationArguments_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = annotationArguments_AST;
	}
	
	public final void annotationsInternal() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationsInternal_AST = null;
		
		{
		_loop91:
		do {
			if ((LA(1)==AT) && (LA(2)==LITERAL_interface)) {
				if ( inputState.guessing==0 ) {
					break; /* go out of the ()* loop*/
				}
				AST tmp100_AST = null;
				tmp100_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp100_AST);
				match(AT);
				AST tmp101_AST = null;
				tmp101_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp101_AST);
				match(LITERAL_interface);
			}
			else if ((LA(1)==AT) && (LA(2)==IDENT)) {
				annotation();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
			}
			else {
				break _loop91;
			}
			
		} while (true);
		}
		annotationsInternal_AST = (AST)currentAST.root;
		returnAST = annotationsInternal_AST;
	}
	
	public final void annotationMemberValueInitializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationMemberValueInitializer_AST = null;
		
		switch ( LA(1)) {
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LCURLY:
		case LITERAL_this:
		case PLUS:
		case MINUS:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			conditionalExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			annotationMemberValueInitializer_AST = (AST)currentAST.root;
			break;
		}
		case AT:
		{
			annotation();
			astFactory.addASTChild(currentAST, returnAST);
			annotationMemberValueInitializer_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = annotationMemberValueInitializer_AST;
	}
	
	public final void annotationMemberValuePairs() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationMemberValuePairs_AST = null;
		
		annotationMemberValuePair();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop97:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				annotationMemberValuePair();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop97;
			}
			
		} while (true);
		}
		annotationMemberValuePairs_AST = (AST)currentAST.root;
		returnAST = annotationMemberValuePairs_AST;
	}
	
	public final void annotationMemberValuePair() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationMemberValuePair_AST = null;
		AST i_AST = null;
		AST v_AST = null;
		Token first = LT(1);
		
		annotationIdent();
		i_AST = (AST)returnAST;
		match(ASSIGN);
		nls();
		annotationMemberValueInitializer();
		v_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			annotationMemberValuePair_AST = (AST)currentAST.root;
			annotationMemberValuePair_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR",first,LT(1))).add(i_AST).add(v_AST));
			currentAST.root = annotationMemberValuePair_AST;
			currentAST.child = annotationMemberValuePair_AST!=null &&annotationMemberValuePair_AST.getFirstChild()!=null ?
				annotationMemberValuePair_AST.getFirstChild() : annotationMemberValuePair_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = annotationMemberValuePair_AST;
	}
	
	public final void annotationIdent() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationIdent_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			AST tmp104_AST = null;
			tmp104_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp104_AST);
			match(IDENT);
			annotationIdent_AST = (AST)currentAST.root;
			break;
		}
		case FINAL:
		case ABSTRACT:
		case UNUSED_GOTO:
		case UNUSED_CONST:
		case UNUSED_DO:
		case STRICTFP:
		case LITERAL_package:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case LITERAL_extends:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_as:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LITERAL_default:
		case LITERAL_throws:
		case LITERAL_implements:
		case LITERAL_this:
		case LITERAL_if:
		case LITERAL_else:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case LITERAL_case:
		case LITERAL_try:
		case LITERAL_finally:
		case LITERAL_catch:
		case LITERAL_false:
		case LITERAL_instanceof:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		{
			keywordPropertyNames();
			astFactory.addASTChild(currentAST, returnAST);
			annotationIdent_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = annotationIdent_AST;
	}
	
	public final void keywordPropertyNames() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST keywordPropertyNames_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_as:
		{
			AST tmp105_AST = null;
			tmp105_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp105_AST);
			match(LITERAL_as);
			break;
		}
		case LITERAL_assert:
		{
			AST tmp106_AST = null;
			tmp106_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp106_AST);
			match(LITERAL_assert);
			break;
		}
		case LITERAL_break:
		{
			AST tmp107_AST = null;
			tmp107_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp107_AST);
			match(LITERAL_break);
			break;
		}
		case LITERAL_case:
		{
			AST tmp108_AST = null;
			tmp108_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp108_AST);
			match(LITERAL_case);
			break;
		}
		case LITERAL_catch:
		{
			AST tmp109_AST = null;
			tmp109_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp109_AST);
			match(LITERAL_catch);
			break;
		}
		case LITERAL_class:
		{
			AST tmp110_AST = null;
			tmp110_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp110_AST);
			match(LITERAL_class);
			break;
		}
		case UNUSED_CONST:
		{
			AST tmp111_AST = null;
			tmp111_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp111_AST);
			match(UNUSED_CONST);
			break;
		}
		case LITERAL_continue:
		{
			AST tmp112_AST = null;
			tmp112_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp112_AST);
			match(LITERAL_continue);
			break;
		}
		case LITERAL_def:
		{
			AST tmp113_AST = null;
			tmp113_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp113_AST);
			match(LITERAL_def);
			break;
		}
		case LITERAL_default:
		{
			AST tmp114_AST = null;
			tmp114_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp114_AST);
			match(LITERAL_default);
			break;
		}
		case UNUSED_DO:
		{
			AST tmp115_AST = null;
			tmp115_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp115_AST);
			match(UNUSED_DO);
			break;
		}
		case LITERAL_else:
		{
			AST tmp116_AST = null;
			tmp116_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp116_AST);
			match(LITERAL_else);
			break;
		}
		case LITERAL_enum:
		{
			AST tmp117_AST = null;
			tmp117_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp117_AST);
			match(LITERAL_enum);
			break;
		}
		case LITERAL_extends:
		{
			AST tmp118_AST = null;
			tmp118_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp118_AST);
			match(LITERAL_extends);
			break;
		}
		case LITERAL_false:
		{
			AST tmp119_AST = null;
			tmp119_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp119_AST);
			match(LITERAL_false);
			break;
		}
		case LITERAL_finally:
		{
			AST tmp120_AST = null;
			tmp120_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp120_AST);
			match(LITERAL_finally);
			break;
		}
		case LITERAL_for:
		{
			AST tmp121_AST = null;
			tmp121_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp121_AST);
			match(LITERAL_for);
			break;
		}
		case UNUSED_GOTO:
		{
			AST tmp122_AST = null;
			tmp122_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp122_AST);
			match(UNUSED_GOTO);
			break;
		}
		case LITERAL_if:
		{
			AST tmp123_AST = null;
			tmp123_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp123_AST);
			match(LITERAL_if);
			break;
		}
		case LITERAL_implements:
		{
			AST tmp124_AST = null;
			tmp124_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp124_AST);
			match(LITERAL_implements);
			break;
		}
		case LITERAL_import:
		{
			AST tmp125_AST = null;
			tmp125_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp125_AST);
			match(LITERAL_import);
			break;
		}
		case LITERAL_in:
		{
			AST tmp126_AST = null;
			tmp126_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp126_AST);
			match(LITERAL_in);
			break;
		}
		case LITERAL_instanceof:
		{
			AST tmp127_AST = null;
			tmp127_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp127_AST);
			match(LITERAL_instanceof);
			break;
		}
		case LITERAL_interface:
		{
			AST tmp128_AST = null;
			tmp128_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp128_AST);
			match(LITERAL_interface);
			break;
		}
		case LITERAL_new:
		{
			AST tmp129_AST = null;
			tmp129_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp129_AST);
			match(LITERAL_new);
			break;
		}
		case LITERAL_null:
		{
			AST tmp130_AST = null;
			tmp130_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp130_AST);
			match(LITERAL_null);
			break;
		}
		case LITERAL_package:
		{
			AST tmp131_AST = null;
			tmp131_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp131_AST);
			match(LITERAL_package);
			break;
		}
		case LITERAL_return:
		{
			AST tmp132_AST = null;
			tmp132_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp132_AST);
			match(LITERAL_return);
			break;
		}
		case LITERAL_super:
		{
			AST tmp133_AST = null;
			tmp133_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp133_AST);
			match(LITERAL_super);
			break;
		}
		case LITERAL_switch:
		{
			AST tmp134_AST = null;
			tmp134_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp134_AST);
			match(LITERAL_switch);
			break;
		}
		case LITERAL_this:
		{
			AST tmp135_AST = null;
			tmp135_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp135_AST);
			match(LITERAL_this);
			break;
		}
		case LITERAL_throw:
		{
			AST tmp136_AST = null;
			tmp136_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp136_AST);
			match(LITERAL_throw);
			break;
		}
		case LITERAL_throws:
		{
			AST tmp137_AST = null;
			tmp137_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp137_AST);
			match(LITERAL_throws);
			break;
		}
		case LITERAL_true:
		{
			AST tmp138_AST = null;
			tmp138_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp138_AST);
			match(LITERAL_true);
			break;
		}
		case LITERAL_try:
		{
			AST tmp139_AST = null;
			tmp139_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp139_AST);
			match(LITERAL_try);
			break;
		}
		case LITERAL_while:
		{
			AST tmp140_AST = null;
			tmp140_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp140_AST);
			match(LITERAL_while);
			break;
		}
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			modifier();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			builtInType();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			keywordPropertyNames_AST = (AST)currentAST.root;
			keywordPropertyNames_AST.setType(IDENT);
		}
		keywordPropertyNames_AST = (AST)currentAST.root;
		returnAST = keywordPropertyNames_AST;
	}
	
	public final void conditionalExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST conditionalExpression_AST = null;
		
		logicalOrExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case ELVIS_OPERATOR:
		{
			AST tmp141_AST = null;
			tmp141_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp141_AST);
			match(ELVIS_OPERATOR);
			nls();
			conditionalExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case QUESTION:
		{
			AST tmp142_AST = null;
			tmp142_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp142_AST);
			match(QUESTION);
			nls();
			assignmentExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			nls();
			match(COLON);
			nls();
			conditionalExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case FINAL:
		case ABSTRACT:
		case UNUSED_GOTO:
		case UNUSED_CONST:
		case UNUSED_DO:
		case STRICTFP:
		case LITERAL_package:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case LBRACK:
		case RBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case LITERAL_extends:
		case LITERAL_super:
		case COMMA:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_as:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case RPAREN:
		case ASSIGN:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_throws:
		case LITERAL_implements:
		case LITERAL_this:
		case CLOSABLE_BLOCK_OP:
		case COLON:
		case LITERAL_if:
		case LITERAL_else:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case PLUS:
		case MINUS:
		case LITERAL_case:
		case LITERAL_try:
		case LITERAL_finally:
		case LITERAL_catch:
		case LITERAL_false:
		case LITERAL_instanceof:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case PLUS_ASSIGN:
		case MINUS_ASSIGN:
		case STAR_ASSIGN:
		case DIV_ASSIGN:
		case MOD_ASSIGN:
		case SR_ASSIGN:
		case BSR_ASSIGN:
		case SL_ASSIGN:
		case BAND_ASSIGN:
		case BXOR_ASSIGN:
		case BOR_ASSIGN:
		case STAR_STAR_ASSIGN:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		conditionalExpression_AST = (AST)currentAST.root;
		returnAST = conditionalExpression_AST;
	}
	
	public final void annotationMemberArrayValueInitializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationMemberArrayValueInitializer_AST = null;
		
		switch ( LA(1)) {
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LCURLY:
		case LITERAL_this:
		case PLUS:
		case MINUS:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			conditionalExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			annotationMemberArrayValueInitializer_AST = (AST)currentAST.root;
			break;
		}
		case AT:
		{
			annotation();
			astFactory.addASTChild(currentAST, returnAST);
			nls();
			annotationMemberArrayValueInitializer_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = annotationMemberArrayValueInitializer_AST;
	}
	
	public final void superClassClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST superClassClause_AST = null;
		AST c_AST = null;
		Token first = LT(1);
		
		{
		switch ( LA(1)) {
		case LITERAL_extends:
		{
			match(LITERAL_extends);
			nls();
			classOrInterfaceType(false);
			c_AST = (AST)returnAST;
			nls();
			break;
		}
		case EOF:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_implements:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			superClassClause_AST = (AST)currentAST.root;
			superClassClause_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(EXTENDS_CLAUSE,"EXTENDS_CLAUSE",first,LT(1))).add(c_AST));
			currentAST.root = superClassClause_AST;
			currentAST.child = superClassClause_AST!=null &&superClassClause_AST.getFirstChild()!=null ?
				superClassClause_AST.getFirstChild() : superClassClause_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = superClassClause_AST;
	}
	
	public final void implementsClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST implementsClause_AST = null;
		Token  i = null;
		AST i_AST = null;
		Token first = LT(1);
		
		{
		switch ( LA(1)) {
		case LITERAL_implements:
		{
			i = LT(1);
			i_AST = astFactory.create(i);
			match(LITERAL_implements);
			nls();
			classOrInterfaceType(true);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop196:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					nls();
					classOrInterfaceType(true);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop196;
				}
				
			} while (true);
			}
			nls();
			break;
		}
		case EOF:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			implementsClause_AST = (AST)currentAST.root;
			implementsClause_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(IMPLEMENTS_CLAUSE,"IMPLEMENTS_CLAUSE",first,LT(1))).add(implementsClause_AST));
			currentAST.root = implementsClause_AST;
			currentAST.child = implementsClause_AST!=null &&implementsClause_AST.getFirstChild()!=null ?
				implementsClause_AST.getFirstChild() : implementsClause_AST;
			currentAST.advanceChildToEnd();
		}
		implementsClause_AST = (AST)currentAST.root;
		returnAST = implementsClause_AST;
	}
	
	public final void classBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classBlock_AST = null;
		Token first = LT(1);
		
		try {      // for error handling
			match(LCURLY);
			{
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case LITERAL_def:
			case IDENT:
			case LITERAL_class:
			case LITERAL_interface:
			case LITERAL_enum:
			case AT:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			{
				classField();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RCURLY:
			case SEMI:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop125:
			do {
				if ((LA(1)==SEMI||LA(1)==NLS)) {
					sep();
					{
					switch ( LA(1)) {
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case LITERAL_static:
					case LITERAL_def:
					case IDENT:
					case LITERAL_class:
					case LITERAL_interface:
					case LITERAL_enum:
					case AT:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					case LCURLY:
					{
						classField();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case RCURLY:
					case SEMI:
					case NLS:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop125;
				}
				
			} while (true);
			}
			match(RCURLY);
			if ( inputState.guessing==0 ) {
				classBlock_AST = (AST)currentAST.root;
				classBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(OBJBLOCK,"OBJBLOCK",first,LT(1))).add(classBlock_AST));
				currentAST.root = classBlock_AST;
				currentAST.child = classBlock_AST!=null &&classBlock_AST.getFirstChild()!=null ?
					classBlock_AST.getFirstChild() : classBlock_AST;
				currentAST.advanceChildToEnd();
			}
			classBlock_AST = (AST)currentAST.root;
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
							if (errorList.isEmpty()) { // dirty hack to avoid having trouble with cascading problems
						classBlock_AST = (AST)currentAST.root;
					}
					reportError(e);
				classBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(OBJBLOCK,"OBJBLOCK",first,LT(1))).add(classBlock_AST));  	
					currentAST.root = classBlock_AST;
							currentAST.child = classBlock_AST!=null &&classBlock_AST.getFirstChild()!=null ? classBlock_AST.getFirstChild() : classBlock_AST;
							currentAST.advanceChildToEnd();	
				
			} else {
				throw e;
			}
		}
		returnAST = classBlock_AST;
	}
	
	public final void interfaceExtends() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST interfaceExtends_AST = null;
		Token  e = null;
		AST e_AST = null;
		Token first = LT(1);
		
		{
		switch ( LA(1)) {
		case LITERAL_extends:
		{
			e = LT(1);
			e_AST = astFactory.create(e);
			match(LITERAL_extends);
			nls();
			classOrInterfaceType(true);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop192:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					nls();
					classOrInterfaceType(true);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop192;
				}
				
			} while (true);
			}
			nls();
			break;
		}
		case LCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			interfaceExtends_AST = (AST)currentAST.root;
			interfaceExtends_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(EXTENDS_CLAUSE,"EXTENDS_CLAUSE",first,LT(1))).add(interfaceExtends_AST));
			currentAST.root = interfaceExtends_AST;
			currentAST.child = interfaceExtends_AST!=null &&interfaceExtends_AST.getFirstChild()!=null ?
				interfaceExtends_AST.getFirstChild() : interfaceExtends_AST;
			currentAST.advanceChildToEnd();
		}
		interfaceExtends_AST = (AST)currentAST.root;
		returnAST = interfaceExtends_AST;
	}
	
	public final void interfaceBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST interfaceBlock_AST = null;
		Token first = LT(1);
		
		match(LCURLY);
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case IDENT:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case AT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			interfaceField();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop130:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_static:
				case LITERAL_def:
				case IDENT:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				{
					interfaceField();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop130;
			}
			
		} while (true);
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			interfaceBlock_AST = (AST)currentAST.root;
			interfaceBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(OBJBLOCK,"OBJBLOCK",first,LT(1))).add(interfaceBlock_AST));
			currentAST.root = interfaceBlock_AST;
			currentAST.child = interfaceBlock_AST!=null &&interfaceBlock_AST.getFirstChild()!=null ?
				interfaceBlock_AST.getFirstChild() : interfaceBlock_AST;
			currentAST.advanceChildToEnd();
		}
		interfaceBlock_AST = (AST)currentAST.root;
		returnAST = interfaceBlock_AST;
	}
	
	public final void enumBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumBlock_AST = null;
		Token first = LT(1);
		
		match(LCURLY);
		nls();
		{
		boolean synPredMatched139 = false;
		if (((LA(1)==IDENT||LA(1)==AT) && (_tokenSet_51.member(LA(2))))) {
			int _m139 = mark();
			synPredMatched139 = true;
			inputState.guessing++;
			try {
				{
				enumConstantsStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched139 = false;
			}
			rewind(_m139);
inputState.guessing--;
		}
		if ( synPredMatched139 ) {
			enumConstants();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_52.member(LA(1))) && (_tokenSet_53.member(LA(2)))) {
			{
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case LITERAL_def:
			case IDENT:
			case LITERAL_class:
			case LITERAL_interface:
			case LITERAL_enum:
			case AT:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			{
				classField();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RCURLY:
			case SEMI:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		{
		_loop143:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_static:
				case LITERAL_def:
				case IDENT:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				{
					classField();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop143;
			}
			
		} while (true);
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			enumBlock_AST = (AST)currentAST.root;
			enumBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(OBJBLOCK,"OBJBLOCK",first,LT(1))).add(enumBlock_AST));
			currentAST.root = enumBlock_AST;
			currentAST.child = enumBlock_AST!=null &&enumBlock_AST.getFirstChild()!=null ?
				enumBlock_AST.getFirstChild() : enumBlock_AST;
			currentAST.advanceChildToEnd();
		}
		enumBlock_AST = (AST)currentAST.root;
		returnAST = enumBlock_AST;
	}
	
	public final void annotationBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationBlock_AST = null;
		Token first = LT(1);
		
		match(LCURLY);
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case IDENT:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case AT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			annotationField();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop135:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_static:
				case LITERAL_def:
				case IDENT:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				{
					annotationField();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop135;
			}
			
		} while (true);
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			annotationBlock_AST = (AST)currentAST.root;
			annotationBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(OBJBLOCK,"OBJBLOCK",first,LT(1))).add(annotationBlock_AST));
			currentAST.root = annotationBlock_AST;
			currentAST.child = annotationBlock_AST!=null &&annotationBlock_AST.getFirstChild()!=null ?
				annotationBlock_AST.getFirstChild() : annotationBlock_AST;
			currentAST.advanceChildToEnd();
		}
		annotationBlock_AST = (AST)currentAST.root;
		returnAST = annotationBlock_AST;
	}
	
	public final void typeParameter() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeParameter_AST = null;
		Token  id = null;
		AST id_AST = null;
		Token first = LT(1);
		
		{
		id = LT(1);
		id_AST = astFactory.create(id);
		astFactory.addASTChild(currentAST, id_AST);
		match(IDENT);
		}
		{
		if ((LA(1)==LITERAL_extends) && (LA(2)==IDENT||LA(2)==NLS)) {
			typeParameterBounds();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_54.member(LA(1))) && (_tokenSet_12.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			typeParameter_AST = (AST)currentAST.root;
			typeParameter_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE_PARAMETER,"TYPE_PARAMETER",first,LT(1))).add(typeParameter_AST));
			currentAST.root = typeParameter_AST;
			currentAST.child = typeParameter_AST!=null &&typeParameter_AST.getFirstChild()!=null ?
				typeParameter_AST.getFirstChild() : typeParameter_AST;
			currentAST.advanceChildToEnd();
		}
		typeParameter_AST = (AST)currentAST.root;
		returnAST = typeParameter_AST;
	}
	
	public final void typeParameterBounds() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeParameterBounds_AST = null;
		Token first = LT(1);
		
		match(LITERAL_extends);
		nls();
		classOrInterfaceType(true);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop120:
		do {
			if ((LA(1)==BAND)) {
				match(BAND);
				nls();
				classOrInterfaceType(true);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop120;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			typeParameterBounds_AST = (AST)currentAST.root;
			typeParameterBounds_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS",first,LT(1))).add(typeParameterBounds_AST));
			currentAST.root = typeParameterBounds_AST;
			currentAST.child = typeParameterBounds_AST!=null &&typeParameterBounds_AST.getFirstChild()!=null ?
				typeParameterBounds_AST.getFirstChild() : typeParameterBounds_AST;
			currentAST.advanceChildToEnd();
		}
		typeParameterBounds_AST = (AST)currentAST.root;
		returnAST = typeParameterBounds_AST;
	}
	
	public final void classField() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST classField_AST = null;
		AST mc_AST = null;
		AST ctor_AST = null;
		AST dg_AST = null;
		AST mad_AST = null;
		AST dd_AST = null;
		AST mods_AST = null;
		AST td_AST = null;
		AST s3_AST = null;
		AST s4_AST = null;
		Token first = LT(1);
		
		try {      // for error handling
			boolean synPredMatched199 = false;
			if (((_tokenSet_55.member(LA(1))) && (_tokenSet_56.member(LA(2))))) {
				int _m199 = mark();
				synPredMatched199 = true;
				inputState.guessing++;
				try {
					{
					constructorStart();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched199 = false;
				}
				rewind(_m199);
inputState.guessing--;
			}
			if ( synPredMatched199 ) {
				modifiersOpt();
				mc_AST = (AST)returnAST;
				constructorDefinition(mc_AST);
				ctor_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					classField_AST = (AST)currentAST.root;
					classField_AST = ctor_AST;
					currentAST.root = classField_AST;
					currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
						classField_AST.getFirstChild() : classField_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				boolean synPredMatched201 = false;
				if (((_tokenSet_13.member(LA(1))) && (_tokenSet_14.member(LA(2))))) {
					int _m201 = mark();
					synPredMatched201 = true;
					inputState.guessing++;
					try {
						{
						genericMethodStart();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched201 = false;
					}
					rewind(_m201);
inputState.guessing--;
				}
				if ( synPredMatched201 ) {
					genericMethod();
					dg_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						classField_AST = (AST)currentAST.root;
						classField_AST = dg_AST;
						currentAST.root = classField_AST;
						currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
							classField_AST.getFirstChild() : classField_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else {
					boolean synPredMatched203 = false;
					if (((_tokenSet_13.member(LA(1))) && (_tokenSet_15.member(LA(2))))) {
						int _m203 = mark();
						synPredMatched203 = true;
						inputState.guessing++;
						try {
							{
							multipleAssignmentDeclarationStart();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched203 = false;
						}
						rewind(_m203);
inputState.guessing--;
					}
					if ( synPredMatched203 ) {
						multipleAssignmentDeclaration();
						mad_AST = (AST)returnAST;
						if ( inputState.guessing==0 ) {
							classField_AST = (AST)currentAST.root;
							classField_AST = mad_AST;
							currentAST.root = classField_AST;
							currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
								classField_AST.getFirstChild() : classField_AST;
							currentAST.advanceChildToEnd();
						}
					}
					else {
						boolean synPredMatched205 = false;
						if (((_tokenSet_16.member(LA(1))) && (_tokenSet_17.member(LA(2))))) {
							int _m205 = mark();
							synPredMatched205 = true;
							inputState.guessing++;
							try {
								{
								declarationStart();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched205 = false;
							}
							rewind(_m205);
inputState.guessing--;
						}
						if ( synPredMatched205 ) {
							declaration();
							dd_AST = (AST)returnAST;
							if ( inputState.guessing==0 ) {
								classField_AST = (AST)currentAST.root;
								classField_AST = dd_AST;
								currentAST.root = classField_AST;
								currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
									classField_AST.getFirstChild() : classField_AST;
								currentAST.advanceChildToEnd();
							}
						}
						else {
							boolean synPredMatched207 = false;
							if (((_tokenSet_22.member(LA(1))) && (_tokenSet_23.member(LA(2))))) {
								int _m207 = mark();
								synPredMatched207 = true;
								inputState.guessing++;
								try {
									{
									typeDeclarationStart();
									}
								}
								catch (RecognitionException pe) {
									synPredMatched207 = false;
								}
								rewind(_m207);
inputState.guessing--;
							}
							if ( synPredMatched207 ) {
								modifiersOpt();
								mods_AST = (AST)returnAST;
								{
								typeDefinitionInternal(mods_AST);
								td_AST = (AST)returnAST;
								if ( inputState.guessing==0 ) {
									classField_AST = (AST)currentAST.root;
									classField_AST = td_AST;
									currentAST.root = classField_AST;
									currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
										classField_AST.getFirstChild() : classField_AST;
									currentAST.advanceChildToEnd();
								}
								}
							}
							else if ((LA(1)==LITERAL_static) && (LA(2)==LCURLY||LA(2)==NLS)) {
								match(LITERAL_static);
								nls();
								compoundStatement();
								s3_AST = (AST)returnAST;
								if ( inputState.guessing==0 ) {
									classField_AST = (AST)currentAST.root;
									classField_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(STATIC_INIT,"STATIC_INIT",first,LT(1))).add(s3_AST));
									currentAST.root = classField_AST;
									currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
										classField_AST.getFirstChild() : classField_AST;
									currentAST.advanceChildToEnd();
								}
							}
							else if ((LA(1)==LCURLY)) {
								compoundStatement();
								s4_AST = (AST)returnAST;
								if ( inputState.guessing==0 ) {
									classField_AST = (AST)currentAST.root;
									classField_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(INSTANCE_INIT,"INSTANCE_INIT",first,LT(1))).add(s4_AST));
									currentAST.root = classField_AST;
									currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
										classField_AST.getFirstChild() : classField_AST;
									currentAST.advanceChildToEnd();
								}
							}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							}}}}
						}
						catch (RecognitionException e) {
							if (inputState.guessing==0) {
								
									reportError(e);
									// Create a fake variable definition for this 'thing' and get the position right.  
									// Type is object
									classField_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(VARIABLE_DEF,"VARIABLE_DEF",first,LT(1))).add(null).add(create(TYPE,"java.lang.Object",LT(1),LT(2))).add(create(IDENT,first.getText(),LT(1),LT(2)))); 
									consumeUntil(NLS);
								
							} else {
								throw e;
							}
						}
						returnAST = classField_AST;
					}
					
	public final void interfaceField() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST interfaceField_AST = null;
		AST d_AST = null;
		AST dg_AST = null;
		AST mods_AST = null;
		AST td_AST = null;
		
		boolean synPredMatched211 = false;
		if (((_tokenSet_16.member(LA(1))) && (_tokenSet_17.member(LA(2))))) {
			int _m211 = mark();
			synPredMatched211 = true;
			inputState.guessing++;
			try {
				{
				declarationStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched211 = false;
			}
			rewind(_m211);
inputState.guessing--;
		}
		if ( synPredMatched211 ) {
			declaration();
			d_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				interfaceField_AST = (AST)currentAST.root;
				interfaceField_AST = d_AST;
				currentAST.root = interfaceField_AST;
				currentAST.child = interfaceField_AST!=null &&interfaceField_AST.getFirstChild()!=null ?
					interfaceField_AST.getFirstChild() : interfaceField_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else {
			boolean synPredMatched213 = false;
			if (((_tokenSet_13.member(LA(1))) && (_tokenSet_14.member(LA(2))))) {
				int _m213 = mark();
				synPredMatched213 = true;
				inputState.guessing++;
				try {
					{
					genericMethodStart();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched213 = false;
				}
				rewind(_m213);
inputState.guessing--;
			}
			if ( synPredMatched213 ) {
				genericMethod();
				dg_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					interfaceField_AST = (AST)currentAST.root;
					interfaceField_AST = dg_AST;
					currentAST.root = interfaceField_AST;
					currentAST.child = interfaceField_AST!=null &&interfaceField_AST.getFirstChild()!=null ?
						interfaceField_AST.getFirstChild() : interfaceField_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				boolean synPredMatched215 = false;
				if (((_tokenSet_22.member(LA(1))) && (_tokenSet_23.member(LA(2))))) {
					int _m215 = mark();
					synPredMatched215 = true;
					inputState.guessing++;
					try {
						{
						typeDeclarationStart();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched215 = false;
					}
					rewind(_m215);
inputState.guessing--;
				}
				if ( synPredMatched215 ) {
					modifiersOpt();
					mods_AST = (AST)returnAST;
					{
					typeDefinitionInternal(mods_AST);
					td_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						interfaceField_AST = (AST)currentAST.root;
						interfaceField_AST = td_AST;
						currentAST.root = interfaceField_AST;
						currentAST.child = interfaceField_AST!=null &&interfaceField_AST.getFirstChild()!=null ?
							interfaceField_AST.getFirstChild() : interfaceField_AST;
						currentAST.advanceChildToEnd();
					}
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}}
				returnAST = interfaceField_AST;
			}
			
	public final void annotationField() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST annotationField_AST = null;
		AST mods_AST = null;
		AST td_AST = null;
		AST t_AST = null;
		Token  i = null;
		AST i_AST = null;
		AST amvi_AST = null;
		AST v_AST = null;
		Token first = LT(1);
		
		modifiersOpt();
		mods_AST = (AST)returnAST;
		{
		switch ( LA(1)) {
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case AT:
		{
			typeDefinitionInternal(mods_AST);
			td_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				annotationField_AST = (AST)currentAST.root;
				annotationField_AST = td_AST;
				currentAST.root = annotationField_AST;
				currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
					annotationField_AST.getFirstChild() : annotationField_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			typeSpec(false);
			t_AST = (AST)returnAST;
			{
			boolean synPredMatched163 = false;
			if (((LA(1)==IDENT) && (LA(2)==LPAREN))) {
				int _m163 = mark();
				synPredMatched163 = true;
				inputState.guessing++;
				try {
					{
					match(IDENT);
					match(LPAREN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched163 = false;
				}
				rewind(_m163);
inputState.guessing--;
			}
			if ( synPredMatched163 ) {
				i = LT(1);
				i_AST = astFactory.create(i);
				match(IDENT);
				match(LPAREN);
				match(RPAREN);
				{
				switch ( LA(1)) {
				case LITERAL_default:
				{
					match(LITERAL_default);
					nls();
					annotationMemberValueInitializer();
					amvi_AST = (AST)returnAST;
					break;
				}
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					annotationField_AST = (AST)currentAST.root;
					annotationField_AST =
					(AST)astFactory.make( (new ASTArray(5)).add(create(ANNOTATION_FIELD_DEF,"ANNOTATION_FIELD_DEF",first,LT(1))).add(mods_AST).add((AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(t_AST))).add(i_AST).add(amvi_AST));
					currentAST.root = annotationField_AST;
					currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
						annotationField_AST.getFirstChild() : annotationField_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else if ((LA(1)==IDENT||LA(1)==STRING_LITERAL) && (_tokenSet_57.member(LA(2)))) {
				variableDefinitions(mods_AST,t_AST);
				v_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					annotationField_AST = (AST)currentAST.root;
					annotationField_AST = v_AST;
					currentAST.root = annotationField_AST;
					currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
						annotationField_AST.getFirstChild() : annotationField_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		returnAST = annotationField_AST;
	}
	
/** Guard for enumConstants.  */
	public final void enumConstantsStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstantsStart_AST = null;
		
		annotationsOpt();
		astFactory.addASTChild(currentAST, returnAST);
		AST tmp161_AST = null;
		tmp161_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp161_AST);
		match(IDENT);
		{
		switch ( LA(1)) {
		case LCURLY:
		{
			AST tmp162_AST = null;
			tmp162_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp162_AST);
			match(LCURLY);
			break;
		}
		case LPAREN:
		{
			AST tmp163_AST = null;
			tmp163_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp163_AST);
			match(LPAREN);
			break;
		}
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case IDENT:
		case AT:
		case COMMA:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case RCURLY:
		case SEMI:
		case NLS:
		{
			nls();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case SEMI:
			{
				AST tmp164_AST = null;
				tmp164_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp164_AST);
				match(SEMI);
				break;
			}
			case COMMA:
			{
				AST tmp165_AST = null;
				tmp165_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp165_AST);
				match(COMMA);
				break;
			}
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case LITERAL_def:
			case IDENT:
			case AT:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			{
				declarationStart();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RCURLY:
			{
				AST tmp166_AST = null;
				tmp166_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp166_AST);
				match(RCURLY);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		enumConstantsStart_AST = (AST)currentAST.root;
		returnAST = enumConstantsStart_AST;
	}
	
/** Comma-separated list of one or more enum constant definitions.  */
	public final void enumConstants() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstants_AST = null;
		
		enumConstant();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop158:
		do {
			boolean synPredMatched151 = false;
			if (((_tokenSet_58.member(LA(1))) && (_tokenSet_59.member(LA(2))))) {
				int _m151 = mark();
				synPredMatched151 = true;
				inputState.guessing++;
				try {
					{
					nls();
					{
					switch ( LA(1)) {
					case RCURLY:
					{
						match(RCURLY);
						break;
					}
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case LITERAL_static:
					case LITERAL_def:
					case IDENT:
					case LITERAL_class:
					case LITERAL_interface:
					case LITERAL_enum:
					case AT:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					case LCURLY:
					{
						classField();
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					}
				}
				catch (RecognitionException pe) {
					synPredMatched151 = false;
				}
				rewind(_m151);
inputState.guessing--;
			}
			if ( synPredMatched151 ) {
				if ( inputState.guessing==0 ) {
					break; /* leave ()* loop */
				}
			}
			else if ((LA(1)==COMMA||LA(1)==NLS) && (_tokenSet_60.member(LA(2)))) {
				nls();
				match(COMMA);
				{
				boolean synPredMatched155 = false;
				if (((_tokenSet_58.member(LA(1))) && (_tokenSet_59.member(LA(2))))) {
					int _m155 = mark();
					synPredMatched155 = true;
					inputState.guessing++;
					try {
						{
						nls();
						{
						switch ( LA(1)) {
						case RCURLY:
						{
							match(RCURLY);
							break;
						}
						case FINAL:
						case ABSTRACT:
						case STRICTFP:
						case LITERAL_static:
						case LITERAL_def:
						case IDENT:
						case LITERAL_class:
						case LITERAL_interface:
						case LITERAL_enum:
						case AT:
						case LITERAL_void:
						case LITERAL_boolean:
						case LITERAL_byte:
						case LITERAL_char:
						case LITERAL_short:
						case LITERAL_int:
						case LITERAL_float:
						case LITERAL_long:
						case LITERAL_double:
						case LITERAL_private:
						case LITERAL_public:
						case LITERAL_protected:
						case LITERAL_transient:
						case LITERAL_native:
						case LITERAL_threadsafe:
						case LITERAL_synchronized:
						case LITERAL_volatile:
						case LCURLY:
						{
							classField();
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						}
					}
					catch (RecognitionException pe) {
						synPredMatched155 = false;
					}
					rewind(_m155);
inputState.guessing--;
				}
				if ( synPredMatched155 ) {
					if ( inputState.guessing==0 ) {
						break; /* leave ()* loop */
					}
				}
				else {
					boolean synPredMatched157 = false;
					if (((_tokenSet_61.member(LA(1))) && (_tokenSet_62.member(LA(2))))) {
						int _m157 = mark();
						synPredMatched157 = true;
						inputState.guessing++;
						try {
							{
							nls();
							annotationsOpt();
							match(IDENT);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched157 = false;
						}
						rewind(_m157);
inputState.guessing--;
					}
					if ( synPredMatched157 ) {
						nls();
						enumConstant();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop158;
				}
				
			} while (true);
			}
			enumConstants_AST = (AST)currentAST.root;
			returnAST = enumConstants_AST;
		}
		
	public final void enumConstant() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstant_AST = null;
		AST an_AST = null;
		Token  i = null;
		AST i_AST = null;
		AST a_AST = null;
		AST b_AST = null;
		Token first = LT(1);
		
		annotationsOpt();
		an_AST = (AST)returnAST;
		i = LT(1);
		i_AST = astFactory.create(i);
		match(IDENT);
		{
		switch ( LA(1)) {
		case LPAREN:
		{
			match(LPAREN);
			argList();
			a_AST = (AST)returnAST;
			match(RPAREN);
			break;
		}
		case COMMA:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case LCURLY:
		{
			enumConstantBlock();
			b_AST = (AST)returnAST;
			break;
		}
		case COMMA:
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			enumConstant_AST = (AST)currentAST.root;
			enumConstant_AST = (AST)astFactory.make( (new ASTArray(5)).add(create(ENUM_CONSTANT_DEF,"ENUM_CONSTANT_DEF",first,LT(1))).add(an_AST).add(i_AST).add(a_AST).add(b_AST));
			currentAST.root = enumConstant_AST;
			currentAST.child = enumConstant_AST!=null &&enumConstant_AST.getFirstChild()!=null ?
				enumConstant_AST.getFirstChild() : enumConstant_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = enumConstant_AST;
	}
	
	public final void argList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argList_AST = null;
		
		Token first = LT(1);
		Token lastComma = null;
		int hls=0, hls2=0;
		boolean hasClosureList=false;
		boolean trailingComma=false;
		boolean sce=false;
		
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case UNUSED_GOTO:
		case UNUSED_CONST:
		case UNUSED_DO:
		case STRICTFP:
		case LITERAL_package:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case AT:
		case LITERAL_extends:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case STAR:
		case LITERAL_as:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_default:
		case LITERAL_throws:
		case LITERAL_implements:
		case LITERAL_this:
		case LITERAL_if:
		case LITERAL_else:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case PLUS:
		case MINUS:
		case LITERAL_case:
		case LITERAL_try:
		case LITERAL_finally:
		case LITERAL_catch:
		case LITERAL_false:
		case LITERAL_instanceof:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			hls=argument();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case SEMI:
			{
				{
				{
				int _cnt549=0;
				_loop549:
				do {
					if ((LA(1)==SEMI)) {
						match(SEMI);
						if ( inputState.guessing==0 ) {
							hasClosureList=true;
						}
						{
						switch ( LA(1)) {
						case FINAL:
						case ABSTRACT:
						case STRICTFP:
						case LITERAL_static:
						case LITERAL_def:
						case LBRACK:
						case IDENT:
						case STRING_LITERAL:
						case LPAREN:
						case AT:
						case LITERAL_super:
						case LITERAL_void:
						case LITERAL_boolean:
						case LITERAL_byte:
						case LITERAL_char:
						case LITERAL_short:
						case LITERAL_int:
						case LITERAL_float:
						case LITERAL_long:
						case LITERAL_double:
						case LITERAL_private:
						case LITERAL_public:
						case LITERAL_protected:
						case LITERAL_transient:
						case LITERAL_native:
						case LITERAL_threadsafe:
						case LITERAL_synchronized:
						case LITERAL_volatile:
						case LCURLY:
						case LITERAL_this:
						case LITERAL_return:
						case LITERAL_break:
						case LITERAL_continue:
						case LITERAL_throw:
						case LITERAL_assert:
						case PLUS:
						case MINUS:
						case LITERAL_false:
						case LITERAL_new:
						case LITERAL_null:
						case LITERAL_true:
						case INC:
						case DEC:
						case BNOT:
						case LNOT:
						case STRING_CTOR_START:
						case NUM_INT:
						case NUM_FLOAT:
						case NUM_LONG:
						case NUM_DOUBLE:
						case NUM_BIG_INT:
						case NUM_BIG_DECIMAL:
						{
							sce=strictContextExpression(true);
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						case RBRACK:
						case RPAREN:
						case SEMI:
						{
							if ( inputState.guessing==0 ) {
								astFactory.addASTChild(currentAST,astFactory.create(EMPTY_STAT, "EMPTY_STAT"));
							}
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
					}
					else {
						if ( _cnt549>=1 ) { break _loop549; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt549++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					argList_AST = (AST)currentAST.root;
					argList_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(CLOSURE_LIST,"CLOSURE_LIST",first,LT(1))).add(argList_AST));
					currentAST.root = argList_AST;
					currentAST.child = argList_AST!=null &&argList_AST.getFirstChild()!=null ?
						argList_AST.getFirstChild() : argList_AST;
					currentAST.advanceChildToEnd();
				}
				}
				break;
			}
			case RBRACK:
			case COMMA:
			case RPAREN:
			{
				{
				{
				_loop555:
				do {
					if ((LA(1)==COMMA)) {
						if ( inputState.guessing==0 ) {
							lastComma = LT(1);
						}
						match(COMMA);
						{
						switch ( LA(1)) {
						case FINAL:
						case ABSTRACT:
						case UNUSED_GOTO:
						case UNUSED_CONST:
						case UNUSED_DO:
						case STRICTFP:
						case LITERAL_package:
						case LITERAL_import:
						case LITERAL_static:
						case LITERAL_def:
						case LBRACK:
						case IDENT:
						case STRING_LITERAL:
						case LPAREN:
						case LITERAL_class:
						case LITERAL_interface:
						case LITERAL_enum:
						case AT:
						case LITERAL_extends:
						case LITERAL_super:
						case LITERAL_void:
						case LITERAL_boolean:
						case LITERAL_byte:
						case LITERAL_char:
						case LITERAL_short:
						case LITERAL_int:
						case LITERAL_float:
						case LITERAL_long:
						case LITERAL_double:
						case STAR:
						case LITERAL_as:
						case LITERAL_private:
						case LITERAL_public:
						case LITERAL_protected:
						case LITERAL_transient:
						case LITERAL_native:
						case LITERAL_threadsafe:
						case LITERAL_synchronized:
						case LITERAL_volatile:
						case LCURLY:
						case LITERAL_default:
						case LITERAL_throws:
						case LITERAL_implements:
						case LITERAL_this:
						case LITERAL_if:
						case LITERAL_else:
						case LITERAL_while:
						case LITERAL_switch:
						case LITERAL_for:
						case LITERAL_in:
						case LITERAL_return:
						case LITERAL_break:
						case LITERAL_continue:
						case LITERAL_throw:
						case LITERAL_assert:
						case PLUS:
						case MINUS:
						case LITERAL_case:
						case LITERAL_try:
						case LITERAL_finally:
						case LITERAL_catch:
						case LITERAL_false:
						case LITERAL_instanceof:
						case LITERAL_new:
						case LITERAL_null:
						case LITERAL_true:
						case INC:
						case DEC:
						case BNOT:
						case LNOT:
						case STRING_CTOR_START:
						case NUM_INT:
						case NUM_FLOAT:
						case NUM_LONG:
						case NUM_DOUBLE:
						case NUM_BIG_INT:
						case NUM_BIG_DECIMAL:
						{
							{
							hls2=argument();
							astFactory.addASTChild(currentAST, returnAST);
							if ( inputState.guessing==0 ) {
								hls |= hls2;
							}
							}
							break;
						}
						case RBRACK:
						case COMMA:
						case RPAREN:
						{
							{
							if ( inputState.guessing==0 ) {
								if (trailingComma) throw new NoViableAltException(lastComma, getFilename());
								trailingComma=true;
								
							}
							}
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
					}
					else {
						break _loop555;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					argList_AST = (AST)currentAST.root;
					argList_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(ELIST,"ELIST",first,LT(1))).add(argList_AST));
					currentAST.root = argList_AST;
					currentAST.child = argList_AST!=null &&argList_AST.getFirstChild()!=null ?
						argList_AST.getFirstChild() : argList_AST;
					currentAST.advanceChildToEnd();
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case RBRACK:
		case RPAREN:
		{
			{
			if ( inputState.guessing==0 ) {
				argList_AST = (AST)currentAST.root;
				argList_AST = create(ELIST,"ELIST",first,LT(1));
				currentAST.root = argList_AST;
				currentAST.child = argList_AST!=null &&argList_AST.getFirstChild()!=null ?
					argList_AST.getFirstChild() : argList_AST;
				currentAST.advanceChildToEnd();
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			argListHasLabels = (hls&1)!=0;
		}
		argList_AST = (AST)currentAST.root;
		returnAST = argList_AST;
	}
	
	public final void enumConstantBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstantBlock_AST = null;
		Token first = LT(1);
		
		match(LCURLY);
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case IDENT:
		case LT:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case AT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		{
			enumConstantField();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RCURLY:
		case SEMI:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop172:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_static:
				case LITERAL_def:
				case IDENT:
				case LT:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				{
					enumConstantField();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop172;
			}
			
		} while (true);
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			enumConstantBlock_AST = (AST)currentAST.root;
			enumConstantBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(OBJBLOCK,"OBJBLOCK",first,LT(1))).add(enumConstantBlock_AST));
			currentAST.root = enumConstantBlock_AST;
			currentAST.child = enumConstantBlock_AST!=null &&enumConstantBlock_AST.getFirstChild()!=null ?
				enumConstantBlock_AST.getFirstChild() : enumConstantBlock_AST;
			currentAST.advanceChildToEnd();
		}
		enumConstantBlock_AST = (AST)currentAST.root;
		returnAST = enumConstantBlock_AST;
	}
	
	public final void enumConstantField() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstantField_AST = null;
		AST mods_AST = null;
		AST td_AST = null;
		AST m1_AST = null;
		AST tp1_AST = null;
		AST t1_AST = null;
		AST e1_AST = null;
		AST m2_AST = null;
		AST tp2_AST = null;
		AST t2_AST = null;
		AST e2_AST = null;
		AST cs_AST = null;
		Token first = LT(1);
		
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case IDENT:
		case LT:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case AT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		{
			{
			boolean synPredMatched176 = false;
			if (((_tokenSet_22.member(LA(1))) && (_tokenSet_23.member(LA(2))))) {
				int _m176 = mark();
				synPredMatched176 = true;
				inputState.guessing++;
				try {
					{
					typeDeclarationStart();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched176 = false;
				}
				rewind(_m176);
inputState.guessing--;
			}
			if ( synPredMatched176 ) {
				modifiersOpt();
				mods_AST = (AST)returnAST;
				typeDefinitionInternal(mods_AST);
				td_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					enumConstantField_AST = (AST)currentAST.root;
					enumConstantField_AST = td_AST;
					currentAST.root = enumConstantField_AST;
					currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
						enumConstantField_AST.getFirstChild() : enumConstantField_AST;
					currentAST.advanceChildToEnd();
				}
			}
			else {
				boolean synPredMatched178 = false;
				if (((_tokenSet_13.member(LA(1))) && (_tokenSet_63.member(LA(2))))) {
					int _m178 = mark();
					synPredMatched178 = true;
					inputState.guessing++;
					try {
						{
						modifiers();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched178 = false;
					}
					rewind(_m178);
inputState.guessing--;
				}
				if ( synPredMatched178 ) {
					modifiers();
					m1_AST = (AST)returnAST;
					{
					switch ( LA(1)) {
					case LT:
					{
						typeParameters();
						tp1_AST = (AST)returnAST;
						break;
					}
					case IDENT:
					case STRING_LITERAL:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					{
					if ((_tokenSet_25.member(LA(1))) && (_tokenSet_26.member(LA(2)))) {
						typeSpec(false);
						t1_AST = (AST)returnAST;
					}
					else if ((LA(1)==IDENT||LA(1)==STRING_LITERAL) && (_tokenSet_57.member(LA(2)))) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
					enumConstantFieldInternal(m1_AST, tp1_AST, t1_AST, first);
					e1_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						enumConstantField_AST = (AST)currentAST.root;
						enumConstantField_AST = e1_AST;
						currentAST.root = enumConstantField_AST;
						currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
							enumConstantField_AST.getFirstChild() : enumConstantField_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else if ((_tokenSet_64.member(LA(1))) && (_tokenSet_17.member(LA(2)))) {
					modifiersOpt();
					m2_AST = (AST)returnAST;
					{
					switch ( LA(1)) {
					case LT:
					{
						typeParameters();
						tp2_AST = (AST)returnAST;
						break;
					}
					case IDENT:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					typeSpec(false);
					t2_AST = (AST)returnAST;
					enumConstantFieldInternal(m2_AST, tp2_AST, t2_AST, first);
					e2_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						enumConstantField_AST = (AST)currentAST.root;
						enumConstantField_AST = e2_AST;
						currentAST.root = enumConstantField_AST;
						currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
							enumConstantField_AST.getFirstChild() : enumConstantField_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case LCURLY:
			{
				compoundStatement();
				cs_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					enumConstantField_AST = (AST)currentAST.root;
					enumConstantField_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(INSTANCE_INIT,"INSTANCE_INIT",first,LT(1))).add(cs_AST));
					currentAST.root = enumConstantField_AST;
					currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
						enumConstantField_AST.getFirstChild() : enumConstantField_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			returnAST = enumConstantField_AST;
		}
		
	protected final void enumConstantFieldInternal(
		AST mods, AST tp, AST t, Token first
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST enumConstantFieldInternal_AST = null;
		AST param_AST = null;
		AST tc_AST = null;
		AST s2_AST = null;
		AST v_AST = null;
		
		boolean synPredMatched184 = false;
		if (((LA(1)==IDENT) && (LA(2)==LPAREN))) {
			int _m184 = mark();
			synPredMatched184 = true;
			inputState.guessing++;
			try {
				{
				match(IDENT);
				match(LPAREN);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched184 = false;
			}
			rewind(_m184);
inputState.guessing--;
		}
		if ( synPredMatched184 ) {
			AST tmp174_AST = null;
			tmp174_AST = astFactory.create(LT(1));
			match(IDENT);
			match(LPAREN);
			parameterDeclarationList();
			param_AST = (AST)returnAST;
			match(RPAREN);
			{
			boolean synPredMatched187 = false;
			if (((LA(1)==LITERAL_throws||LA(1)==NLS) && (_tokenSet_29.member(LA(2))))) {
				int _m187 = mark();
				synPredMatched187 = true;
				inputState.guessing++;
				try {
					{
					nls();
					match(LITERAL_throws);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched187 = false;
				}
				rewind(_m187);
inputState.guessing--;
			}
			if ( synPredMatched187 ) {
				throwsClause();
				tc_AST = (AST)returnAST;
			}
			else if ((_tokenSet_65.member(LA(1))) && (_tokenSet_66.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case LCURLY:
			{
				compoundStatement();
				s2_AST = (AST)returnAST;
				break;
			}
			case RCURLY:
			case SEMI:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				enumConstantFieldInternal_AST = (AST)currentAST.root;
				
				enumConstantFieldInternal_AST = (AST)astFactory.make( (new ASTArray(7)).add(create(METHOD_DEF,"METHOD_DEF",first,LT(1))).add(mods).add((AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(t))).add(tmp174_AST).add(param_AST).add(tc_AST).add(s2_AST));
				if (tp != null) {
				AST old = enumConstantFieldInternal_AST.getFirstChild();
				enumConstantFieldInternal_AST.setFirstChild(tp);
				tp.setNextSibling(old);
				}
				
				currentAST.root = enumConstantFieldInternal_AST;
				currentAST.child = enumConstantFieldInternal_AST!=null &&enumConstantFieldInternal_AST.getFirstChild()!=null ?
					enumConstantFieldInternal_AST.getFirstChild() : enumConstantFieldInternal_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else if ((LA(1)==IDENT||LA(1)==STRING_LITERAL) && (_tokenSet_57.member(LA(2)))) {
			variableDefinitions(mods,t);
			v_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				enumConstantFieldInternal_AST = (AST)currentAST.root;
				enumConstantFieldInternal_AST = v_AST;
				currentAST.root = enumConstantFieldInternal_AST;
				currentAST.child = enumConstantFieldInternal_AST!=null &&enumConstantFieldInternal_AST.getFirstChild()!=null ?
					enumConstantFieldInternal_AST.getFirstChild() : enumConstantFieldInternal_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = enumConstantFieldInternal_AST;
	}
	
	public final void compoundStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compoundStatement_AST = null;
		
		openBlock();
		astFactory.addASTChild(currentAST, returnAST);
		compoundStatement_AST = (AST)currentAST.root;
		returnAST = compoundStatement_AST;
	}
	
/** A list of zero or more formal parameters.
 *  If a parameter is variable length (e.g. String... myArg) it should be
 *  to the right of any other parameters of the same kind.
 *  General form:  (req, ..., opt, ..., [rest], key, ..., [restKeys], [block]
 *  This must be sorted out after parsing, since the various declaration forms
 *  are impossible to tell apart without backtracking.
 */
	public final void parameterDeclarationList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterDeclarationList_AST = null;
		Token first = LT(1);
		
		{
		switch ( LA(1)) {
		case FINAL:
		case LITERAL_def:
		case IDENT:
		case AT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case TRIPLE_DOT:
		{
			parameterDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop265:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					nls();
					parameterDeclaration();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop265;
				}
				
			} while (true);
			}
			break;
		}
		case RPAREN:
		case CLOSABLE_BLOCK_OP:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			parameterDeclarationList_AST = (AST)currentAST.root;
			parameterDeclarationList_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(PARAMETERS,"PARAMETERS",first,LT(1))).add(parameterDeclarationList_AST));
			currentAST.root = parameterDeclarationList_AST;
			currentAST.child = parameterDeclarationList_AST!=null &&parameterDeclarationList_AST.getFirstChild()!=null ?
				parameterDeclarationList_AST.getFirstChild() : parameterDeclarationList_AST;
			currentAST.advanceChildToEnd();
		}
		parameterDeclarationList_AST = (AST)currentAST.root;
		returnAST = parameterDeclarationList_AST;
	}
	
	public final void throwsClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST throwsClause_AST = null;
		
		nls();
		AST tmp178_AST = null;
		tmp178_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp178_AST);
		match(LITERAL_throws);
		nls();
		identifier();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop261:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				identifier();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop261;
			}
			
		} while (true);
		}
		throwsClause_AST = (AST)currentAST.root;
		returnAST = throwsClause_AST;
	}
	
/** I've split out constructors separately; we could maybe integrate back into variableDefinitions
 *  later on if we maybe simplified 'def' to be a type declaration?
 */
	public final void constructorDefinition(
		AST mods
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constructorDefinition_AST = null;
		Token  id = null;
		AST id_AST = null;
		AST param_AST = null;
		AST tc_AST = null;
		AST cb_AST = null;
		Token first = cloneToken(LT(1));
		if (mods != null) {
		first.setLine(mods.getLine());
		first.setColumn(mods.getColumn());
		}
		
		id = LT(1);
		id_AST = astFactory.create(id);
		astFactory.addASTChild(currentAST, id_AST);
		match(IDENT);
		match(LPAREN);
		parameterDeclarationList();
		param_AST = (AST)returnAST;
		match(RPAREN);
		{
		boolean synPredMatched250 = false;
		if (((LA(1)==LITERAL_throws||LA(1)==NLS) && (_tokenSet_29.member(LA(2))))) {
			int _m250 = mark();
			synPredMatched250 = true;
			inputState.guessing++;
			try {
				{
				nls();
				match(LITERAL_throws);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched250 = false;
			}
			rewind(_m250);
inputState.guessing--;
		}
		if ( synPredMatched250 ) {
			throwsClause();
			tc_AST = (AST)returnAST;
		}
		else if ((LA(1)==LCURLY||LA(1)==NLS) && (_tokenSet_67.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		nlsWarn();
		if ( inputState.guessing==0 ) {
			isConstructorIdent(id);
		}
		constructorBody();
		cb_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			constructorDefinition_AST = (AST)currentAST.root;
			constructorDefinition_AST =  (AST)astFactory.make( (new ASTArray(5)).add(create(CTOR_IDENT,"CTOR_IDENT",first,LT(1))).add(mods).add(param_AST).add(tc_AST).add(cb_AST));
			
			currentAST.root = constructorDefinition_AST;
			currentAST.child = constructorDefinition_AST!=null &&constructorDefinition_AST.getFirstChild()!=null ?
				constructorDefinition_AST.getFirstChild() : constructorDefinition_AST;
			currentAST.advanceChildToEnd();
		}
		constructorDefinition_AST = (AST)currentAST.root;
		returnAST = constructorDefinition_AST;
	}
	
	public final void multipleAssignmentDeclarationStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multipleAssignmentDeclarationStart_AST = null;
		
		{
		_loop230:
		do {
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			{
				modifier();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case AT:
			{
				annotation();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop230;
			}
			}
		} while (true);
		}
		AST tmp182_AST = null;
		tmp182_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp182_AST);
		match(LITERAL_def);
		nls();
		astFactory.addASTChild(currentAST, returnAST);
		AST tmp183_AST = null;
		tmp183_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp183_AST);
		match(LPAREN);
		multipleAssignmentDeclarationStart_AST = (AST)currentAST.root;
		returnAST = multipleAssignmentDeclarationStart_AST;
	}
	
	public final void multipleAssignmentDeclaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multipleAssignmentDeclaration_AST = null;
		AST mods_AST = null;
		AST t_AST = null;
		Token first = cloneToken(LT(1));
		
		modifiers();
		mods_AST = (AST)returnAST;
		{
		switch ( LA(1)) {
		case IDENT:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			typeSpec(false);
			t_AST = (AST)returnAST;
			break;
		}
		case LPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		AST tmp184_AST = null;
		tmp184_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp184_AST);
		match(LPAREN);
		nls();
		typeNamePairs(mods_AST,first);
		astFactory.addASTChild(currentAST, returnAST);
		match(RPAREN);
		AST tmp186_AST = null;
		tmp186_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp186_AST);
		match(ASSIGN);
		nls();
		assignmentExpression(0);
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			multipleAssignmentDeclaration_AST = (AST)currentAST.root;
			multipleAssignmentDeclaration_AST=(AST)astFactory.make( (new ASTArray(4)).add(create(VARIABLE_DEF,"VARIABLE_DEF",first,LT(1))).add(mods_AST).add((AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(t_AST))).add(multipleAssignmentDeclaration_AST));
			currentAST.root = multipleAssignmentDeclaration_AST;
			currentAST.child = multipleAssignmentDeclaration_AST!=null &&multipleAssignmentDeclaration_AST.getFirstChild()!=null ?
				multipleAssignmentDeclaration_AST.getFirstChild() : multipleAssignmentDeclaration_AST;
			currentAST.advanceChildToEnd();
		}
		multipleAssignmentDeclaration_AST = (AST)currentAST.root;
		returnAST = multipleAssignmentDeclaration_AST;
	}
	
	public final void constructorBody() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constructorBody_AST = null;
		AST eci_AST = null;
		AST bb1_AST = null;
		AST bb2_AST = null;
		Token first = LT(1);
		
		match(LCURLY);
		nls();
		{
		boolean synPredMatched220 = false;
		if (((_tokenSet_68.member(LA(1))) && (_tokenSet_69.member(LA(2))))) {
			int _m220 = mark();
			synPredMatched220 = true;
			inputState.guessing++;
			try {
				{
				explicitConstructorInvocation();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched220 = false;
			}
			rewind(_m220);
inputState.guessing--;
		}
		if ( synPredMatched220 ) {
			explicitConstructorInvocation();
			eci_AST = (AST)returnAST;
			{
			switch ( LA(1)) {
			case SEMI:
			case NLS:
			{
				sep();
				blockBody(sepToken);
				bb1_AST = (AST)returnAST;
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		else if ((_tokenSet_31.member(LA(1))) && (_tokenSet_70.member(LA(2)))) {
			blockBody(EOF);
			bb2_AST = (AST)returnAST;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			constructorBody_AST = (AST)currentAST.root;
			if (eci_AST != null)
			constructorBody_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(SLIST,"{",first,LT(1))).add(eci_AST).add(bb1_AST));
			else
			constructorBody_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(SLIST,"{",first,LT(1))).add(bb2_AST));
			currentAST.root = constructorBody_AST;
			currentAST.child = constructorBody_AST!=null &&constructorBody_AST.getFirstChild()!=null ?
				constructorBody_AST.getFirstChild() : constructorBody_AST;
			currentAST.advanceChildToEnd();
		}
		constructorBody_AST = (AST)currentAST.root;
		returnAST = constructorBody_AST;
	}
	
/** Catch obvious constructor calls, but not the expr.super(...) calls */
	public final void explicitConstructorInvocation() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST explicitConstructorInvocation_AST = null;
		Token  lp1 = null;
		AST lp1_AST = null;
		Token  lp2 = null;
		AST lp2_AST = null;
		
		{
		switch ( LA(1)) {
		case LT:
		{
			typeArguments();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_super:
		case LITERAL_this:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case LITERAL_this:
		{
			match(LITERAL_this);
			lp1 = LT(1);
			lp1_AST = astFactory.create(lp1);
			astFactory.makeASTRoot(currentAST, lp1_AST);
			match(LPAREN);
			argList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				lp1_AST.setType(CTOR_CALL);
			}
			break;
		}
		case LITERAL_super:
		{
			match(LITERAL_super);
			lp2 = LT(1);
			lp2_AST = astFactory.create(lp2);
			astFactory.makeASTRoot(currentAST, lp2_AST);
			match(LPAREN);
			argList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				lp2_AST.setType(SUPER_CTOR_CALL);
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		explicitConstructorInvocation_AST = (AST)currentAST.root;
		returnAST = explicitConstructorInvocation_AST;
	}
	
	public final void listOfVariables(
		AST mods, AST t, Token first
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST listOfVariables_AST = null;
		
		variableDeclarator(getASTFactory().dupTree(mods),
                           getASTFactory().dupTree(t),first);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop227:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				if ( inputState.guessing==0 ) {
					first = LT(1);
				}
				variableDeclarator(getASTFactory().dupTree(mods),
                               getASTFactory().dupTree(t),first);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop227;
			}
			
		} while (true);
		}
		listOfVariables_AST = (AST)currentAST.root;
		returnAST = listOfVariables_AST;
	}
	
/** Declaration of a variable. This can be a class/instance variable,
 *  or a local variable in a method
 *  It can also include possible initialization.
 */
	public final void variableDeclarator(
		AST mods, AST t,Token first
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST variableDeclarator_AST = null;
		AST id_AST = null;
		AST v_AST = null;
		
		variableName();
		id_AST = (AST)returnAST;
		{
		switch ( LA(1)) {
		case ASSIGN:
		{
			varInitializer();
			v_AST = (AST)returnAST;
			break;
		}
		case EOF:
		case COMMA:
		case RPAREN:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_else:
		case LITERAL_case:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			variableDeclarator_AST = (AST)currentAST.root;
			variableDeclarator_AST = (AST)astFactory.make( (new ASTArray(5)).add(create(VARIABLE_DEF,"VARIABLE_DEF",first,LT(1))).add(mods).add((AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(t))).add(id_AST).add(v_AST));
			currentAST.root = variableDeclarator_AST;
			currentAST.child = variableDeclarator_AST!=null &&variableDeclarator_AST.getFirstChild()!=null ?
				variableDeclarator_AST.getFirstChild() : variableDeclarator_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = variableDeclarator_AST;
	}
	
	public final void typeNamePairs(
		AST mods, Token first
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeNamePairs_AST = null;
		AST t_AST = null;
		AST tn_AST = null;
		
		{
		if ((_tokenSet_25.member(LA(1))) && (_tokenSet_32.member(LA(2)))) {
			typeSpec(false);
			t_AST = (AST)returnAST;
		}
		else if ((LA(1)==IDENT) && (LA(2)==COMMA||LA(2)==RPAREN)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		singleVariable(getASTFactory().dupTree(mods),t_AST);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop235:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				if ( inputState.guessing==0 ) {
					first = LT(1);
				}
				{
				if ((_tokenSet_25.member(LA(1))) && (_tokenSet_32.member(LA(2)))) {
					typeSpec(false);
					tn_AST = (AST)returnAST;
				}
				else if ((LA(1)==IDENT) && (LA(2)==COMMA||LA(2)==RPAREN)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				singleVariable(getASTFactory().dupTree(mods),tn_AST);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop235;
			}
			
		} while (true);
		}
		typeNamePairs_AST = (AST)currentAST.root;
		returnAST = typeNamePairs_AST;
	}
	
	public final void assignmentExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignmentExpression_AST = null;
		
		conditionalExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case ASSIGN:
		case PLUS_ASSIGN:
		case MINUS_ASSIGN:
		case STAR_ASSIGN:
		case DIV_ASSIGN:
		case MOD_ASSIGN:
		case SR_ASSIGN:
		case BSR_ASSIGN:
		case SL_ASSIGN:
		case BAND_ASSIGN:
		case BXOR_ASSIGN:
		case BOR_ASSIGN:
		case STAR_STAR_ASSIGN:
		{
			{
			switch ( LA(1)) {
			case ASSIGN:
			{
				AST tmp195_AST = null;
				tmp195_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp195_AST);
				match(ASSIGN);
				break;
			}
			case PLUS_ASSIGN:
			{
				AST tmp196_AST = null;
				tmp196_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp196_AST);
				match(PLUS_ASSIGN);
				break;
			}
			case MINUS_ASSIGN:
			{
				AST tmp197_AST = null;
				tmp197_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp197_AST);
				match(MINUS_ASSIGN);
				break;
			}
			case STAR_ASSIGN:
			{
				AST tmp198_AST = null;
				tmp198_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp198_AST);
				match(STAR_ASSIGN);
				break;
			}
			case DIV_ASSIGN:
			{
				AST tmp199_AST = null;
				tmp199_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp199_AST);
				match(DIV_ASSIGN);
				break;
			}
			case MOD_ASSIGN:
			{
				AST tmp200_AST = null;
				tmp200_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp200_AST);
				match(MOD_ASSIGN);
				break;
			}
			case SR_ASSIGN:
			{
				AST tmp201_AST = null;
				tmp201_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp201_AST);
				match(SR_ASSIGN);
				break;
			}
			case BSR_ASSIGN:
			{
				AST tmp202_AST = null;
				tmp202_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp202_AST);
				match(BSR_ASSIGN);
				break;
			}
			case SL_ASSIGN:
			{
				AST tmp203_AST = null;
				tmp203_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp203_AST);
				match(SL_ASSIGN);
				break;
			}
			case BAND_ASSIGN:
			{
				AST tmp204_AST = null;
				tmp204_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp204_AST);
				match(BAND_ASSIGN);
				break;
			}
			case BXOR_ASSIGN:
			{
				AST tmp205_AST = null;
				tmp205_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp205_AST);
				match(BXOR_ASSIGN);
				break;
			}
			case BOR_ASSIGN:
			{
				AST tmp206_AST = null;
				tmp206_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp206_AST);
				match(BOR_ASSIGN);
				break;
			}
			case STAR_STAR_ASSIGN:
			{
				AST tmp207_AST = null;
				tmp207_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp207_AST);
				match(STAR_STAR_ASSIGN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			nls();
			expressionStatementNoCheck();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		case FINAL:
		case ABSTRACT:
		case UNUSED_GOTO:
		case UNUSED_CONST:
		case UNUSED_DO:
		case STRICTFP:
		case LITERAL_package:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case LBRACK:
		case RBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case LITERAL_extends:
		case LITERAL_super:
		case COMMA:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_as:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case RPAREN:
		case LCURLY:
		case RCURLY:
		case SEMI:
		case LITERAL_default:
		case LITERAL_throws:
		case LITERAL_implements:
		case LITERAL_this:
		case CLOSABLE_BLOCK_OP:
		case COLON:
		case LITERAL_if:
		case LITERAL_else:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case PLUS:
		case MINUS:
		case LITERAL_case:
		case LITERAL_try:
		case LITERAL_finally:
		case LITERAL_catch:
		case LITERAL_false:
		case LITERAL_instanceof:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		assignmentExpression_AST = (AST)currentAST.root;
		returnAST = assignmentExpression_AST;
	}
	
/** Zero or more insignificant newlines, all gobbled up and thrown away,
 *  but a warning message is left for the user, if there was a newline.
 */
	public final void nlsWarn() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nlsWarn_AST = null;
		
		{
		boolean synPredMatched594 = false;
		if (((_tokenSet_71.member(LA(1))) && (_tokenSet_2.member(LA(2))))) {
			int _m594 = mark();
			synPredMatched594 = true;
			inputState.guessing++;
			try {
				{
				match(NLS);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched594 = false;
			}
			rewind(_m594);
inputState.guessing--;
		}
		if ( synPredMatched594 ) {
			if ( inputState.guessing==0 ) {
				addWarning(
				"A newline at this point does not follow the Groovy Coding Conventions.",
				"Keep this statement on one line, or use curly braces to break across multiple lines."
				);
			}
		}
		else if ((_tokenSet_71.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		nls();
		returnAST = nlsWarn_AST;
	}
	
/** An open block is not allowed to have closure arguments. */
	public final void openBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST openBlock_AST = null;
		AST bb_AST = null;
		Token first = LT(1);
		
		match(LCURLY);
		nls();
		blockBody(EOF);
		bb_AST = (AST)returnAST;
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			openBlock_AST = (AST)currentAST.root;
			openBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(SLIST,"{",first,LT(1))).add(bb_AST));
			currentAST.root = openBlock_AST;
			currentAST.child = openBlock_AST!=null &&openBlock_AST.getFirstChild()!=null ?
				openBlock_AST.getFirstChild() : openBlock_AST;
			currentAST.advanceChildToEnd();
		}
		openBlock_AST = (AST)currentAST.root;
		returnAST = openBlock_AST;
	}
	
	public final void variableName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST variableName_AST = null;
		
		AST tmp210_AST = null;
		tmp210_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp210_AST);
		match(IDENT);
		variableName_AST = (AST)currentAST.root;
		returnAST = variableName_AST;
	}
	
	public final void expressionStatementNoCheck() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressionStatementNoCheck_AST = null;
		AST head_AST = null;
		AST cmd_AST = null;
		boolean isPathExpr = true;
		
		expression(LC_STMT);
		head_AST = (AST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			isPathExpr = (head_AST == lastPathExpression);
		}
		{
		if (((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2))))&&(LA(1)!=LITERAL_else && isPathExpr /*&& #head.getType()==METHOD_CALL*/)) {
			commandArgumentsGreedy(head_AST);
			cmd_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				expressionStatementNoCheck_AST = (AST)currentAST.root;
				
				expressionStatementNoCheck_AST = cmd_AST;
				
				currentAST.root = expressionStatementNoCheck_AST;
				currentAST.child = expressionStatementNoCheck_AST!=null &&expressionStatementNoCheck_AST.getFirstChild()!=null ?
					expressionStatementNoCheck_AST.getFirstChild() : expressionStatementNoCheck_AST;
				currentAST.advanceChildToEnd();
			}
		}
		else if ((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		expressionStatementNoCheck_AST = (AST)currentAST.root;
		returnAST = expressionStatementNoCheck_AST;
	}
	
/** A formal parameter for a method or closure. */
	public final void parameterDeclaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterDeclaration_AST = null;
		AST pm_AST = null;
		AST t_AST = null;
		Token  id = null;
		AST id_AST = null;
		AST exp_AST = null;
		Token first = LT(1);boolean spreadParam = false;
		
		parameterModifiersOpt();
		pm_AST = (AST)returnAST;
		{
		if ((_tokenSet_25.member(LA(1))) && (_tokenSet_74.member(LA(2)))) {
			typeSpec(false);
			t_AST = (AST)returnAST;
		}
		else if ((LA(1)==IDENT||LA(1)==TRIPLE_DOT) && (_tokenSet_75.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		{
		switch ( LA(1)) {
		case TRIPLE_DOT:
		{
			match(TRIPLE_DOT);
			if ( inputState.guessing==0 ) {
				spreadParam = true;
			}
			break;
		}
		case IDENT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		id = LT(1);
		id_AST = astFactory.create(id);
		match(IDENT);
		{
		switch ( LA(1)) {
		case ASSIGN:
		{
			varInitializer();
			exp_AST = (AST)returnAST;
			break;
		}
		case COMMA:
		case RPAREN:
		case CLOSABLE_BLOCK_OP:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			parameterDeclaration_AST = (AST)currentAST.root;
			
			if (spreadParam) {
			parameterDeclaration_AST = (AST)astFactory.make( (new ASTArray(5)).add(create(VARIABLE_PARAMETER_DEF,"VARIABLE_PARAMETER_DEF",first,LT(1))).add(pm_AST).add((AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(t_AST))).add(id_AST).add(exp_AST));
			} else {
			parameterDeclaration_AST = (AST)astFactory.make( (new ASTArray(5)).add(create(PARAMETER_DEF,"PARAMETER_DEF",first,LT(1))).add(pm_AST).add((AST)astFactory.make( (new ASTArray(2)).add(create(TYPE,"TYPE",first,LT(1))).add(t_AST))).add(id_AST).add(exp_AST));
			}
			
			currentAST.root = parameterDeclaration_AST;
			currentAST.child = parameterDeclaration_AST!=null &&parameterDeclaration_AST.getFirstChild()!=null ?
				parameterDeclaration_AST.getFirstChild() : parameterDeclaration_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = parameterDeclaration_AST;
	}
	
	public final void parameterModifiersOpt() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterModifiersOpt_AST = null;
		Token first = LT(1);int seenDef = 0;
		
		{
		_loop279:
		do {
			switch ( LA(1)) {
			case FINAL:
			{
				AST tmp212_AST = null;
				tmp212_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp212_AST);
				match(FINAL);
				nls();
				break;
			}
			case AT:
			{
				annotation();
				astFactory.addASTChild(currentAST, returnAST);
				nls();
				break;
			}
			default:
				if (((LA(1)==LITERAL_def))&&(seenDef++ == 0)) {
					match(LITERAL_def);
					nls();
				}
			else {
				break _loop279;
			}
			}
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			parameterModifiersOpt_AST = (AST)currentAST.root;
			parameterModifiersOpt_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(MODIFIERS,"MODIFIERS",first,LT(1))).add(parameterModifiersOpt_AST));
			currentAST.root = parameterModifiersOpt_AST;
			currentAST.child = parameterModifiersOpt_AST!=null &&parameterModifiersOpt_AST.getFirstChild()!=null ?
				parameterModifiersOpt_AST.getFirstChild() : parameterModifiersOpt_AST;
			currentAST.advanceChildToEnd();
		}
		parameterModifiersOpt_AST = (AST)currentAST.root;
		returnAST = parameterModifiersOpt_AST;
	}
	
	public final void multicatch_types() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multicatch_types_AST = null;
		Token first = LT(1);
		
		nls();
		classOrInterfaceType(false);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop272:
		do {
			if ((LA(1)==BOR)) {
				match(BOR);
				nls();
				classOrInterfaceType(false);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop272;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			multicatch_types_AST = (AST)currentAST.root;
			multicatch_types_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(MULTICATCH_TYPES,"MULTICATCH_TYPES",first,LT(1))).add(multicatch_types_AST));
			currentAST.root = multicatch_types_AST;
			currentAST.child = multicatch_types_AST!=null &&multicatch_types_AST.getFirstChild()!=null ?
				multicatch_types_AST.getFirstChild() : multicatch_types_AST;
			currentAST.advanceChildToEnd();
		}
		multicatch_types_AST = (AST)currentAST.root;
		returnAST = multicatch_types_AST;
	}
	
	public final void multicatch() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multicatch_AST = null;
		AST m_AST = null;
		Token  id = null;
		AST id_AST = null;
		Token first = LT(1);
		
		nls();
		{
		switch ( LA(1)) {
		case FINAL:
		{
			AST tmp215_AST = null;
			tmp215_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp215_AST);
			match(FINAL);
			break;
		}
		case LITERAL_def:
		case IDENT:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case LITERAL_def:
		{
			AST tmp216_AST = null;
			tmp216_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp216_AST);
			match(LITERAL_def);
			break;
		}
		case IDENT:
		case NLS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		if ((LA(1)==IDENT||LA(1)==NLS) && (_tokenSet_76.member(LA(2)))) {
			multicatch_types();
			m_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((LA(1)==IDENT) && (LA(2)==RPAREN)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		id = LT(1);
		id_AST = astFactory.create(id);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			multicatch_AST = (AST)currentAST.root;
			
			multicatch_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(MULTICATCH,"MULTICATCH",first,LT(1))).add(m_AST).add(id_AST));
			
			currentAST.root = multicatch_AST;
			currentAST.child = multicatch_AST!=null &&multicatch_AST.getFirstChild()!=null ?
				multicatch_AST.getFirstChild() : multicatch_AST;
			currentAST.advanceChildToEnd();
		}
		multicatch_AST = (AST)currentAST.root;
		returnAST = multicatch_AST;
	}
	
/** Closure parameters are exactly like method parameters,
 *  except that they are not enclosed in parentheses, but rather
 *  are prepended to the front of a block, just after the brace.
 *  They are separated from the closure body by a CLOSABLE_BLOCK_OP token '->'.
 */
	public final void closableBlockParamsOpt(
		boolean addImplicit
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closableBlockParamsOpt_AST = null;
		
		boolean synPredMatched282 = false;
		if (((_tokenSet_77.member(LA(1))) && (_tokenSet_78.member(LA(2))))) {
			int _m282 = mark();
			synPredMatched282 = true;
			inputState.guessing++;
			try {
				{
				parameterDeclarationList();
				nls();
				match(CLOSABLE_BLOCK_OP);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched282 = false;
			}
			rewind(_m282);
inputState.guessing--;
		}
		if ( synPredMatched282 ) {
			parameterDeclarationList();
			astFactory.addASTChild(currentAST, returnAST);
			nls();
			match(CLOSABLE_BLOCK_OP);
			nls();
			closableBlockParamsOpt_AST = (AST)currentAST.root;
		}
		else if (((_tokenSet_31.member(LA(1))) && (_tokenSet_73.member(LA(2))))&&(addImplicit)) {
			implicitParameters();
			astFactory.addASTChild(currentAST, returnAST);
			closableBlockParamsOpt_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_31.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
			closableBlockParamsOpt_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = closableBlockParamsOpt_AST;
	}
	
/** A block known to be a closure, but which omits its arguments, is given this placeholder.
 *  A subsequent pass is responsible for deciding if there is an implicit 'it' parameter,
 *  or if the parameter list should be empty.
 */
	public final void implicitParameters() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST implicitParameters_AST = null;
		Token first = LT(1);
		
		if ( inputState.guessing==0 ) {
			implicitParameters_AST = (AST)currentAST.root;
			implicitParameters_AST = (AST)astFactory.make( (new ASTArray(1)).add(create(IMPLICIT_PARAMETERS,"IMPLICIT_PARAMETERS",first,LT(1))));
			currentAST.root = implicitParameters_AST;
			currentAST.child = implicitParameters_AST!=null &&implicitParameters_AST.getFirstChild()!=null ?
				implicitParameters_AST.getFirstChild() : implicitParameters_AST;
			currentAST.advanceChildToEnd();
		}
		implicitParameters_AST = (AST)currentAST.root;
		returnAST = implicitParameters_AST;
	}
	
/** Lookahead to check whether a block begins with explicit closure arguments. */
	public final void closableBlockParamsStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closableBlockParamsStart_AST = null;
		
		nls();
		parameterDeclarationList();
		nls();
		AST tmp218_AST = null;
		tmp218_AST = astFactory.create(LT(1));
		match(CLOSABLE_BLOCK_OP);
		returnAST = closableBlockParamsStart_AST;
	}
	
/** Simple names, as in {x|...}, are completely equivalent to {(def x)|...}.  Build the right AST. */
	public final void closableBlockParam() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closableBlockParam_AST = null;
		Token  id = null;
		AST id_AST = null;
		Token first = LT(1);
		
		id = LT(1);
		id_AST = astFactory.create(id);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			closableBlockParam_AST = (AST)currentAST.root;
			closableBlockParam_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(PARAMETER_DEF,"PARAMETER_DEF",first,LT(1))).add((AST)astFactory.make( (new ASTArray(1)).add(create(MODIFIERS,"MODIFIERS",first,LT(1))))).add((AST)astFactory.make( (new ASTArray(1)).add(create(TYPE,"TYPE",first,LT(1))))).add(id_AST));
			currentAST.root = closableBlockParam_AST;
			currentAST.child = closableBlockParam_AST!=null &&closableBlockParam_AST.getFirstChild()!=null ?
				closableBlockParam_AST.getFirstChild() : closableBlockParam_AST;
			currentAST.advanceChildToEnd();
		}
		returnAST = closableBlockParam_AST;
	}
	
/** A block which is known to be a closure, even if it has no apparent arguments.
 *  A block inside an expression or after a method call is always assumed to be a closure.
 *  Only labeled, unparameterized blocks which occur directly as substatements are kept open.
 */
	public final void closableBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closableBlock_AST = null;
		AST cbp_AST = null;
		AST bb_AST = null;
		Token first = LT(1);
		
		match(LCURLY);
		nls();
		closableBlockParamsOpt(true);
		cbp_AST = (AST)returnAST;
		blockBody(EOF);
		bb_AST = (AST)returnAST;
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			closableBlock_AST = (AST)currentAST.root;
			closableBlock_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(CLOSABLE_BLOCK,"{",first,LT(1))).add(cbp_AST).add(bb_AST));
			currentAST.root = closableBlock_AST;
			currentAST.child = closableBlock_AST!=null &&closableBlock_AST.getFirstChild()!=null ?
				closableBlock_AST.getFirstChild() : closableBlock_AST;
			currentAST.advanceChildToEnd();
		}
		closableBlock_AST = (AST)currentAST.root;
		returnAST = closableBlock_AST;
	}
	
/** A sub-block of a block can be either open or closable.
 *  It is closable if and only if there are explicit closure arguments.
 *  Compare this to a block which is appended to a method call,
 *  which is given closure arguments, even if they are not explicit in the code.
 */
	public final void openOrClosableBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST openOrClosableBlock_AST = null;
		AST cp_AST = null;
		AST bb_AST = null;
		Token first = LT(1);
		
		match(LCURLY);
		nls();
		closableBlockParamsOpt(false);
		cp_AST = (AST)returnAST;
		blockBody(EOF);
		bb_AST = (AST)returnAST;
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			openOrClosableBlock_AST = (AST)currentAST.root;
			
			if (cp_AST == null)    openOrClosableBlock_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(SLIST,"{",first,LT(1))).add(bb_AST));
			else                openOrClosableBlock_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(CLOSABLE_BLOCK,"{",first,LT(1))).add(cp_AST).add(bb_AST));
			
			currentAST.root = openOrClosableBlock_AST;
			currentAST.child = openOrClosableBlock_AST!=null &&openOrClosableBlock_AST.getFirstChild()!=null ?
				openOrClosableBlock_AST.getFirstChild() : openOrClosableBlock_AST;
			currentAST.advanceChildToEnd();
		}
		openOrClosableBlock_AST = (AST)currentAST.root;
		returnAST = openOrClosableBlock_AST;
	}
	
/** A labeled statement, consisting of a vanilla identifier followed by a colon. */
	public final void statementLabelPrefix() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST statementLabelPrefix_AST = null;
		Token  c = null;
		AST c_AST = null;
		
		AST tmp223_AST = null;
		tmp223_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp223_AST);
		match(IDENT);
		c = LT(1);
		c_AST = astFactory.create(c);
		astFactory.makeASTRoot(currentAST, c_AST);
		match(COLON);
		if ( inputState.guessing==0 ) {
			c_AST.setType(LABELED_STAT);
		}
		nls();
		statementLabelPrefix_AST = (AST)currentAST.root;
		returnAST = statementLabelPrefix_AST;
	}
	
/** An expression statement can be any general expression.
 *  <p>
 *  An expression statement can also be a <em>command</em>,
 *  which is a simple method call in which the outermost parentheses are omitted.
 *  <p>
 *  Certain "suspicious" looking forms are flagged for the user to disambiguate.
 */
	public final void expressionStatement(
		int prevToken
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressionStatement_AST = null;
		AST esn_AST = null;
		Token first = LT(1);
		
		{
		boolean synPredMatched345 = false;
		if (((_tokenSet_20.member(LA(1))) && (_tokenSet_2.member(LA(2))))) {
			int _m345 = mark();
			synPredMatched345 = true;
			inputState.guessing++;
			try {
				{
				suspiciousExpressionStatementStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched345 = false;
			}
			rewind(_m345);
inputState.guessing--;
		}
		if ( synPredMatched345 ) {
			checkSuspiciousExpressionStatement(prevToken);
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		expressionStatementNoCheck();
		esn_AST = (AST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			expressionStatement_AST = (AST)currentAST.root;
			expressionStatement_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(EXPR,"EXPR",first,LT(1))).add(esn_AST));
			currentAST.root = expressionStatement_AST;
			currentAST.child = expressionStatement_AST!=null &&expressionStatement_AST.getFirstChild()!=null ?
				expressionStatement_AST.getFirstChild() : expressionStatement_AST;
			currentAST.advanceChildToEnd();
		}
		expressionStatement_AST = (AST)currentAST.root;
		returnAST = expressionStatement_AST;
	}
	
	public final void assignmentLessExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignmentLessExpression_AST = null;
		Token first = LT(1);
		
		{
		conditionalExpression(0);
		astFactory.addASTChild(currentAST, returnAST);
		}
		if ( inputState.guessing==0 ) {
			assignmentLessExpression_AST = (AST)currentAST.root;
			assignmentLessExpression_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(EXPR,"EXPR",first,LT(1))).add(assignmentLessExpression_AST));
			currentAST.root = assignmentLessExpression_AST;
			currentAST.child = assignmentLessExpression_AST!=null &&assignmentLessExpression_AST.getFirstChild()!=null ?
				assignmentLessExpression_AST.getFirstChild() : assignmentLessExpression_AST;
			currentAST.advanceChildToEnd();
		}
		assignmentLessExpression_AST = (AST)currentAST.root;
		returnAST = assignmentLessExpression_AST;
	}
	
/** In Java, "if", "while", and "for" statements can take random, non-braced statements as their bodies.
 *  Support this practice, even though it isn't very Groovy.
 */
	public final void compatibleBodyStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compatibleBodyStatement_AST = null;
		
		try {      // for error handling
			boolean synPredMatched334 = false;
			if (((LA(1)==LCURLY) && (_tokenSet_31.member(LA(2))))) {
				int _m334 = mark();
				synPredMatched334 = true;
				inputState.guessing++;
				try {
					{
					match(LCURLY);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched334 = false;
				}
				rewind(_m334);
inputState.guessing--;
			}
			if ( synPredMatched334 ) {
				compoundStatement();
				astFactory.addASTChild(currentAST, returnAST);
				compatibleBodyStatement_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_19.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
				statement(EOF);
				astFactory.addASTChild(currentAST, returnAST);
				compatibleBodyStatement_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
				// GRECLIPSE1046
				reportError(e);
				
			} else {
				throw e;
			}
		}
		returnAST = compatibleBodyStatement_AST;
	}
	
	public final void forStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forStatement_AST = null;
		AST cl_AST = null;
		AST fic_AST = null;
		Token  s = null;
		AST s_AST = null;
		AST forCbs_AST = null;
		Token first = LT(1);
		
		match(LITERAL_for);
		match(LPAREN);
		{
		boolean synPredMatched321 = false;
		if (((_tokenSet_79.member(LA(1))) && (_tokenSet_80.member(LA(2))))) {
			int _m321 = mark();
			synPredMatched321 = true;
			inputState.guessing++;
			try {
				{
				switch ( LA(1)) {
				case SEMI:
				{
					match(SEMI);
					break;
				}
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_static:
				case LITERAL_def:
				case LBRACK:
				case IDENT:
				case STRING_LITERAL:
				case LPAREN:
				case AT:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				case LITERAL_this:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case PLUS:
				case MINUS:
				case LITERAL_false:
				case LITERAL_new:
				case LITERAL_null:
				case LITERAL_true:
				case INC:
				case DEC:
				case BNOT:
				case LNOT:
				case STRING_CTOR_START:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				{
					{
					strictContextExpression(true);
					match(SEMI);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched321 = false;
			}
			rewind(_m321);
inputState.guessing--;
		}
		if ( synPredMatched321 ) {
			closureList();
			cl_AST = (AST)returnAST;
		}
		else if ((_tokenSet_16.member(LA(1))) && (_tokenSet_81.member(LA(2)))) {
			forInClause();
			fic_AST = (AST)returnAST;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		match(RPAREN);
		nls();
		{
		switch ( LA(1)) {
		case SEMI:
		{
			s = LT(1);
			s_AST = astFactory.create(s);
			match(SEMI);
			break;
		}
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case AT:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case LITERAL_if:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case PLUS:
		case MINUS:
		case LITERAL_try:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			compatibleBodyStatement();
			forCbs_AST = (AST)returnAST;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			forStatement_AST = (AST)currentAST.root;
			
			if (cl_AST != null) {
			if (s_AST != null)
			forStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_for,"for",first,LT(1))).add(cl_AST).add(s_AST));
			else
			forStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_for,"for",first,LT(1))).add(cl_AST).add(forCbs_AST));
			} else {
			if (s_AST != null)
			forStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_for,"for",first,LT(1))).add(fic_AST).add(s_AST));
			else
			forStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_for,"for",first,LT(1))).add(fic_AST).add(forCbs_AST));
			}
			
			currentAST.root = forStatement_AST;
			currentAST.child = forStatement_AST!=null &&forStatement_AST.getFirstChild()!=null ?
				forStatement_AST.getFirstChild() : forStatement_AST;
			currentAST.advanceChildToEnd();
		}
		forStatement_AST = (AST)currentAST.root;
		returnAST = forStatement_AST;
	}
	
/** Things that can show up as expressions, but only in strict
 *  contexts like inside parentheses, argument lists, and list constructors.
 */
	public final boolean  strictContextExpression(
		boolean allowDeclaration
	) throws RecognitionException, TokenStreamException {
		boolean hasDeclaration=false;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strictContextExpression_AST = null;
		Token first = LT(1);
		
		{
		boolean synPredMatched528 = false;
		if (((_tokenSet_16.member(LA(1))) && (_tokenSet_82.member(LA(2))))) {
			int _m528 = mark();
			synPredMatched528 = true;
			inputState.guessing++;
			try {
				{
				if (!(allowDeclaration))
				  throw new SemanticException("allowDeclaration");
				declarationStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched528 = false;
			}
			rewind(_m528);
inputState.guessing--;
		}
		if ( synPredMatched528 ) {
			if ( inputState.guessing==0 ) {
				hasDeclaration=true;
			}
			singleDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_38.member(LA(2)))) {
			expression(0);
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if (((LA(1) >= LITERAL_return && LA(1) <= LITERAL_assert))) {
			branchStatement();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((LA(1)==AT) && (LA(2)==IDENT)) {
			annotation();
			astFactory.addASTChild(currentAST, returnAST);
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			strictContextExpression_AST = (AST)currentAST.root;
			strictContextExpression_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(EXPR,"EXPR",first,LT(1))).add(strictContextExpression_AST));
			currentAST.root = strictContextExpression_AST;
			currentAST.child = strictContextExpression_AST!=null &&strictContextExpression_AST.getFirstChild()!=null ?
				strictContextExpression_AST.getFirstChild() : strictContextExpression_AST;
			currentAST.advanceChildToEnd();
		}
		strictContextExpression_AST = (AST)currentAST.root;
		returnAST = strictContextExpression_AST;
		return hasDeclaration;
	}
	
	public final void casesGroup() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST casesGroup_AST = null;
		Token first = LT(1);
		
		{
		int _cnt358=0;
		_loop358:
		do {
			if ((LA(1)==LITERAL_default||LA(1)==LITERAL_case)) {
				aCase();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt358>=1 ) { break _loop358; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt358++;
		} while (true);
		}
		caseSList();
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			casesGroup_AST = (AST)currentAST.root;
			casesGroup_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(CASE_GROUP,"CASE_GROUP",first,LT(1))).add(casesGroup_AST));
			currentAST.root = casesGroup_AST;
			currentAST.child = casesGroup_AST!=null &&casesGroup_AST.getFirstChild()!=null ?
				casesGroup_AST.getFirstChild() : casesGroup_AST;
			currentAST.advanceChildToEnd();
		}
		casesGroup_AST = (AST)currentAST.root;
		returnAST = casesGroup_AST;
	}
	
	public final void tryBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tryBlock_AST = null;
		AST tryCs_AST = null;
		AST h_AST = null;
		AST fc_AST = null;
		Token first = LT(1);List catchNodes = new ArrayList();AST newHandler_AST = null;
		
		match(LITERAL_try);
		nlsWarn();
		compoundStatement();
		tryCs_AST = (AST)returnAST;
		{
		_loop375:
		do {
			if (((LA(1)==LITERAL_catch||LA(1)==NLS) && (LA(2)==LPAREN||LA(2)==LITERAL_catch))&&(!(LA(1) == NLS && LA(2) == LPAREN))) {
				nls();
				handler();
				h_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					newHandler_AST = (AST)astFactory.make( (new ASTArray(3)).add(null).add(newHandler_AST).add(h_AST));
				}
			}
			else {
				break _loop375;
			}
			
		} while (true);
		}
		{
		if ((LA(1)==LITERAL_finally||LA(1)==NLS) && (_tokenSet_83.member(LA(2)))) {
			nls();
			finallyClause();
			fc_AST = (AST)returnAST;
		}
		else if ((_tokenSet_11.member(LA(1))) && (_tokenSet_12.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			tryBlock_AST = (AST)currentAST.root;
			tryBlock_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(LITERAL_try,"try",first,LT(1))).add(tryCs_AST).add(newHandler_AST).add(fc_AST));
			currentAST.root = tryBlock_AST;
			currentAST.child = tryBlock_AST!=null &&tryBlock_AST.getFirstChild()!=null ?
				tryBlock_AST.getFirstChild() : tryBlock_AST;
			currentAST.advanceChildToEnd();
		}
		tryBlock_AST = (AST)currentAST.root;
		returnAST = tryBlock_AST;
	}
	
/** In Groovy, return, break, continue, throw, and assert can be used in a parenthesized expression context.
 *  Example:  println (x || (return));  println assert x, "won't print a false value!"
 *  If an optional expression is missing, its value is void (this coerces to null when a value is required).
 */
	public final void branchStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST branchStatement_AST = null;
		AST returnE_AST = null;
		Token  breakI = null;
		AST breakI_AST = null;
		Token  contI = null;
		AST contI_AST = null;
		AST throwE_AST = null;
		AST assertAle_AST = null;
		AST assertE_AST = null;
		Token first = LT(1);
		
		switch ( LA(1)) {
		case LITERAL_return:
		{
			match(LITERAL_return);
			{
			switch ( LA(1)) {
			case LBRACK:
			case IDENT:
			case STRING_LITERAL:
			case LPAREN:
			case LITERAL_super:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LCURLY:
			case LITERAL_this:
			case PLUS:
			case MINUS:
			case LITERAL_false:
			case LITERAL_new:
			case LITERAL_null:
			case LITERAL_true:
			case INC:
			case DEC:
			case BNOT:
			case LNOT:
			case STRING_CTOR_START:
			case NUM_INT:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			case NUM_BIG_INT:
			case NUM_BIG_DECIMAL:
			{
				expression(0);
				returnE_AST = (AST)returnAST;
				break;
			}
			case EOF:
			case RBRACK:
			case COMMA:
			case RPAREN:
			case RCURLY:
			case SEMI:
			case LITERAL_default:
			case LITERAL_else:
			case LITERAL_case:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				branchStatement_AST = (AST)currentAST.root;
				branchStatement_AST = (AST)astFactory.make( (new ASTArray(2)).add(create2(LITERAL_return,"return",first,LT(0))).add(returnE_AST));
				currentAST.root = branchStatement_AST;
				currentAST.child = branchStatement_AST!=null &&branchStatement_AST.getFirstChild()!=null ?
					branchStatement_AST.getFirstChild() : branchStatement_AST;
				currentAST.advanceChildToEnd();
			}
			branchStatement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_break:
		{
			match(LITERAL_break);
			{
			switch ( LA(1)) {
			case IDENT:
			{
				breakI = LT(1);
				breakI_AST = astFactory.create(breakI);
				match(IDENT);
				break;
			}
			case EOF:
			case RBRACK:
			case COMMA:
			case RPAREN:
			case RCURLY:
			case SEMI:
			case LITERAL_default:
			case LITERAL_else:
			case LITERAL_case:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				branchStatement_AST = (AST)currentAST.root;
				branchStatement_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(LITERAL_break,"break",first,LT(1))).add(breakI_AST));
				currentAST.root = branchStatement_AST;
				currentAST.child = branchStatement_AST!=null &&branchStatement_AST.getFirstChild()!=null ?
					branchStatement_AST.getFirstChild() : branchStatement_AST;
				currentAST.advanceChildToEnd();
			}
			branchStatement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_continue:
		{
			match(LITERAL_continue);
			{
			switch ( LA(1)) {
			case IDENT:
			{
				contI = LT(1);
				contI_AST = astFactory.create(contI);
				match(IDENT);
				break;
			}
			case EOF:
			case RBRACK:
			case COMMA:
			case RPAREN:
			case RCURLY:
			case SEMI:
			case LITERAL_default:
			case LITERAL_else:
			case LITERAL_case:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				branchStatement_AST = (AST)currentAST.root;
				branchStatement_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(LITERAL_continue,"continue",first,LT(1))).add(contI_AST));
				currentAST.root = branchStatement_AST;
				currentAST.child = branchStatement_AST!=null &&branchStatement_AST.getFirstChild()!=null ?
					branchStatement_AST.getFirstChild() : branchStatement_AST;
				currentAST.advanceChildToEnd();
			}
			branchStatement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_throw:
		{
			match(LITERAL_throw);
			expression(0);
			throwE_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				branchStatement_AST = (AST)currentAST.root;
				branchStatement_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(LITERAL_throw,"throw",first,LT(1))).add(throwE_AST));
				currentAST.root = branchStatement_AST;
				currentAST.child = branchStatement_AST!=null &&branchStatement_AST.getFirstChild()!=null ?
					branchStatement_AST.getFirstChild() : branchStatement_AST;
				currentAST.advanceChildToEnd();
			}
			branchStatement_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_assert:
		{
			match(LITERAL_assert);
			assignmentLessExpression();
			assertAle_AST = (AST)returnAST;
			{
			if ((LA(1)==COMMA||LA(1)==COLON) && (_tokenSet_84.member(LA(2)))) {
				{
				switch ( LA(1)) {
				case COMMA:
				{
					match(COMMA);
					nls();
					break;
				}
				case COLON:
				{
					match(COLON);
					nls();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				expression(0);
				assertE_AST = (AST)returnAST;
			}
			else if ((_tokenSet_85.member(LA(1))) && (_tokenSet_12.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				branchStatement_AST = (AST)currentAST.root;
				branchStatement_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_assert,"assert",first,LT(1))).add(assertAle_AST).add(assertE_AST));
				currentAST.root = branchStatement_AST;
				currentAST.child = branchStatement_AST!=null &&branchStatement_AST.getFirstChild()!=null ?
					branchStatement_AST.getFirstChild() : branchStatement_AST;
				currentAST.advanceChildToEnd();
			}
			branchStatement_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = branchStatement_AST;
	}
	
	public final void closureList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closureList_AST = null;
		Token first = LT(1); boolean sce=false;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case AT:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case PLUS:
		case MINUS:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			sce=strictContextExpression(true);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case SEMI:
		{
			if ( inputState.guessing==0 ) {
				astFactory.addASTChild(currentAST,astFactory.create(EMPTY_STAT, "EMPTY_STAT"));
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		int _cnt326=0;
		_loop326:
		do {
			if ((LA(1)==SEMI) && (_tokenSet_86.member(LA(2)))) {
				match(SEMI);
				sce=strictContextExpression(true);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((LA(1)==SEMI) && (LA(2)==RPAREN||LA(2)==SEMI)) {
				match(SEMI);
				if ( inputState.guessing==0 ) {
					astFactory.addASTChild(currentAST,astFactory.create(EMPTY_STAT, "EMPTY_STAT"));
				}
			}
			else {
				if ( _cnt326>=1 ) { break _loop326; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt326++;
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			closureList_AST = (AST)currentAST.root;
			closureList_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(CLOSURE_LIST,"CLOSURE_LIST",first,LT(1))).add(closureList_AST));
			currentAST.root = closureList_AST;
			currentAST.child = closureList_AST!=null &&closureList_AST.getFirstChild()!=null ?
				closureList_AST.getFirstChild() : closureList_AST;
			currentAST.advanceChildToEnd();
		}
		closureList_AST = (AST)currentAST.root;
		returnAST = closureList_AST;
	}
	
	public final void forInClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forInClause_AST = null;
		AST decl_AST = null;
		Token  i = null;
		AST i_AST = null;
		Token  c = null;
		AST c_AST = null;
		
		{
		boolean synPredMatched330 = false;
		if (((_tokenSet_16.member(LA(1))) && (_tokenSet_82.member(LA(2))))) {
			int _m330 = mark();
			synPredMatched330 = true;
			inputState.guessing++;
			try {
				{
				declarationStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched330 = false;
			}
			rewind(_m330);
inputState.guessing--;
		}
		if ( synPredMatched330 ) {
			singleDeclarationNoInit();
			decl_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((LA(1)==IDENT) && (LA(2)==COLON||LA(2)==LITERAL_in)) {
			AST tmp237_AST = null;
			tmp237_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp237_AST);
			match(IDENT);
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		{
		switch ( LA(1)) {
		case LITERAL_in:
		{
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.makeASTRoot(currentAST, i_AST);
			match(LITERAL_in);
			if ( inputState.guessing==0 ) {
				i_AST.setType(FOR_IN_ITERABLE);
			}
			shiftExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case COLON:
		{
			if ( inputState.guessing==0 ) {
				addWarning(
				"A colon at this point is legal Java but not recommended in Groovy.",
				"Use the 'in' keyword."
				);
				require(decl_AST != null,
				"Java-style for-each statement requires a type declaration."
				,
				"Use the 'in' keyword, as for (x in y) {...}"
				);
				
			}
			c = LT(1);
			c_AST = astFactory.create(c);
			astFactory.makeASTRoot(currentAST, c_AST);
			match(COLON);
			if ( inputState.guessing==0 ) {
				c_AST.setType(FOR_IN_ITERABLE);
			}
			expression(0);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		forInClause_AST = (AST)currentAST.root;
		returnAST = forInClause_AST;
	}
	
	public final void shiftExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST shiftExpression_AST = null;
		
		additiveExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop479:
		do {
			if ((_tokenSet_87.member(LA(1)))) {
				{
				switch ( LA(1)) {
				case SR:
				case BSR:
				case SL:
				{
					{
					switch ( LA(1)) {
					case SL:
					{
						AST tmp238_AST = null;
						tmp238_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp238_AST);
						match(SL);
						break;
					}
					case SR:
					{
						AST tmp239_AST = null;
						tmp239_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp239_AST);
						match(SR);
						break;
					}
					case BSR:
					{
						AST tmp240_AST = null;
						tmp240_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp240_AST);
						match(BSR);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					break;
				}
				case RANGE_INCLUSIVE:
				{
					AST tmp241_AST = null;
					tmp241_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp241_AST);
					match(RANGE_INCLUSIVE);
					break;
				}
				case RANGE_EXCLUSIVE:
				{
					AST tmp242_AST = null;
					tmp242_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp242_AST);
					match(RANGE_EXCLUSIVE);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				additiveExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop479;
			}
			
		} while (true);
		}
		shiftExpression_AST = (AST)currentAST.root;
		returnAST = shiftExpression_AST;
	}
	
	public final void expression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expression_AST = null;
		Token  lp = null;
		AST lp_AST = null;
		AST m_AST = null;
		
		boolean synPredMatched401 = false;
		if (((LA(1)==LPAREN) && (_tokenSet_25.member(LA(2))))) {
			int _m401 = mark();
			synPredMatched401 = true;
			inputState.guessing++;
			try {
				{
				match(LPAREN);
				typeSpec(true);
				match(RPAREN);
				expression(lc_stmt);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched401 = false;
			}
			rewind(_m401);
inputState.guessing--;
		}
		if ( synPredMatched401 ) {
			lp = LT(1);
			lp_AST = astFactory.create(lp);
			astFactory.makeASTRoot(currentAST, lp_AST);
			match(LPAREN);
			if ( inputState.guessing==0 ) {
				lp_AST.setType(TYPECAST);
			}
			typeSpec(true);
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			expression(lc_stmt);
			astFactory.addASTChild(currentAST, returnAST);
			expression_AST = (AST)currentAST.root;
		}
		else {
			boolean synPredMatched405 = false;
			if (((LA(1)==LPAREN) && (LA(2)==IDENT||LA(2)==NLS))) {
				int _m405 = mark();
				synPredMatched405 = true;
				inputState.guessing++;
				try {
					{
					match(LPAREN);
					nls();
					match(IDENT);
					{
					_loop404:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							nls();
							match(IDENT);
						}
						else {
							break _loop404;
						}
						
					} while (true);
					}
					match(RPAREN);
					match(ASSIGN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched405 = false;
				}
				rewind(_m405);
inputState.guessing--;
			}
			if ( synPredMatched405 ) {
				multipleAssignment(lc_stmt);
				m_AST = (AST)returnAST;
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					expression_AST = (AST)currentAST.root;
					expression_AST=m_AST;
					currentAST.root = expression_AST;
					currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
						expression_AST.getFirstChild() : expression_AST;
					currentAST.advanceChildToEnd();
				}
				expression_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_38.member(LA(2)))) {
				assignmentExpression(lc_stmt);
				astFactory.addASTChild(currentAST, returnAST);
				expression_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			returnAST = expression_AST;
		}
		
/** Lookahead for suspicious statement warnings and errors. */
	public final void suspiciousExpressionStatementStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST suspiciousExpressionStatementStart_AST = null;
		
		{
		switch ( LA(1)) {
		case PLUS:
		case MINUS:
		{
			{
			switch ( LA(1)) {
			case PLUS:
			{
				AST tmp244_AST = null;
				tmp244_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp244_AST);
				match(PLUS);
				break;
			}
			case MINUS:
			{
				AST tmp245_AST = null;
				tmp245_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp245_AST);
				match(MINUS);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		case LBRACK:
		case LPAREN:
		case LCURLY:
		{
			{
			switch ( LA(1)) {
			case LBRACK:
			{
				AST tmp246_AST = null;
				tmp246_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp246_AST);
				match(LBRACK);
				break;
			}
			case LPAREN:
			{
				AST tmp247_AST = null;
				tmp247_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp247_AST);
				match(LPAREN);
				break;
			}
			case LCURLY:
			{
				AST tmp248_AST = null;
				tmp248_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp248_AST);
				match(LCURLY);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		suspiciousExpressionStatementStart_AST = (AST)currentAST.root;
		returnAST = suspiciousExpressionStatementStart_AST;
	}
	
/**
 *  If two statements are separated by newline (not SEMI), the second had
 *  better not look like the latter half of an expression.  If it does, issue a warning.
 *  <p>
 *  Also, if the expression starts with a closure, it needs to
 *  have an explicit parameter list, in order to avoid the appearance of a
 *  compound statement.  This is a hard error.
 *  <p>
 *  These rules are different from Java's "dumb expression" restriction.
 *  Unlike Java, Groovy blocks can end with arbitrary (even dumb) expressions,
 *  as a consequence of optional 'return' and 'continue' tokens.
 * <p>
 *  To make the programmer's intention clear, a leading closure must have an
 *  explicit parameter list, and must not follow a previous statement separated
 *  only by newlines.
 */
	public final void checkSuspiciousExpressionStatement(
		int prevToken
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST checkSuspiciousExpressionStatement_AST = null;
		
		boolean synPredMatched350 = false;
		if (((_tokenSet_20.member(LA(1))) && (_tokenSet_2.member(LA(2))))) {
			int _m350 = mark();
			synPredMatched350 = true;
			inputState.guessing++;
			try {
				{
				if ((_tokenSet_88.member(LA(1)))) {
					matchNot(LCURLY);
				}
				else if ((LA(1)==LCURLY)) {
					match(LCURLY);
					closableBlockParamsStart();
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			catch (RecognitionException pe) {
				synPredMatched350 = false;
			}
			rewind(_m350);
inputState.guessing--;
		}
		if ( synPredMatched350 ) {
			{
			if (((_tokenSet_20.member(LA(1))) && (_tokenSet_2.member(LA(2))))&&(prevToken == NLS)) {
				if ( inputState.guessing==0 ) {
					addWarning(
					"Expression statement looks like it may continue a previous statement",
					"Either remove the previous newline, or add an explicit semicolon ';'.");
					
				}
			}
			else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			checkSuspiciousExpressionStatement_AST = (AST)currentAST.root;
		}
		else if (((_tokenSet_20.member(LA(1))) && (_tokenSet_2.member(LA(2))))&&(prevToken == NLS)) {
			if ( inputState.guessing==0 ) {
				require(false,
				"Ambiguous expression could be a parameterless closure expression, "+
				"an isolated open code block, or it may continue a previous statement",
				"Add an explicit parameter list, e.g. {it -> ...}, or force it to be treated "+
				"as an open block by giving it a label, e.g. L:{...}, "+
				"and also either remove the previous newline, or add an explicit semicolon ';'"
				);
				
			}
			checkSuspiciousExpressionStatement_AST = (AST)currentAST.root;
		}
		else if (((_tokenSet_20.member(LA(1))) && (_tokenSet_2.member(LA(2))))&&(prevToken != NLS)) {
			if ( inputState.guessing==0 ) {
				require(false,
				"Ambiguous expression could be either a parameterless closure expression or "+
				"an isolated open code block",
				"Add an explicit closure parameter list, e.g. {it -> ...}, or force it to "+
				"be treated as an open block by giving it a label, e.g. L:{...}");
				
			}
			checkSuspiciousExpressionStatement_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = checkSuspiciousExpressionStatement_AST;
	}
	
	public final void commandArgumentsGreedy(
		AST head
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST commandArgumentsGreedy_AST = null;
		AST first_AST = null;
		AST pre_AST = null;
		AST pc_AST = null;
		AST ca_AST = null;
		
			AST prev = head;
		
		
		{
		boolean synPredMatched385 = false;
		if (((_tokenSet_89.member(LA(1))) && (_tokenSet_38.member(LA(2))))) {
			int _m385 = mark();
			synPredMatched385 = true;
			inputState.guessing++;
			try {
				{
				if (!(prev==null || prev.getType()!=METHOD_CALL))
				  throw new SemanticException("prev==null || prev.getType()!=METHOD_CALL");
				commandArgument();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched385 = false;
			}
			rewind(_m385);
inputState.guessing--;
		}
		if ( synPredMatched385 ) {
			{
			commandArguments(head);
			first_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				prev = first_AST;
			}
			}
		}
		else if ((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		{
		{
		_loop394:
		do {
			if ((_tokenSet_90.member(LA(1))) && (_tokenSet_91.member(LA(2)))) {
				primaryExpression();
				pre_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					prev = (AST)astFactory.make( (new ASTArray(3)).add(create(DOT,".",prev)).add(prev).add(pre_AST));
				}
				{
				boolean synPredMatched391 = false;
				if (((_tokenSet_92.member(LA(1))) && (_tokenSet_93.member(LA(2))))) {
					int _m391 = mark();
					synPredMatched391 = true;
					inputState.guessing++;
					try {
						{
						pathElementStart();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched391 = false;
					}
					rewind(_m391);
inputState.guessing--;
				}
				if ( synPredMatched391 ) {
					{
					pathChain(LC_STMT,prev);
					pc_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						prev = pc_AST;
					}
					}
				}
				else if ((_tokenSet_89.member(LA(1))) && (_tokenSet_38.member(LA(2)))) {
					{
					commandArguments(prev);
					ca_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						prev = ca_AST;
					}
					}
				}
				else if ((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else {
				break _loop394;
			}
			
		} while (true);
		}
		}
		if ( inputState.guessing==0 ) {
			commandArgumentsGreedy_AST = (AST)currentAST.root;
			commandArgumentsGreedy_AST = prev;
			currentAST.root = commandArgumentsGreedy_AST;
			currentAST.child = commandArgumentsGreedy_AST!=null &&commandArgumentsGreedy_AST.getFirstChild()!=null ?
				commandArgumentsGreedy_AST.getFirstChild() : commandArgumentsGreedy_AST;
			currentAST.advanceChildToEnd();
		}
		commandArgumentsGreedy_AST = (AST)currentAST.root;
		returnAST = commandArgumentsGreedy_AST;
	}
	
	public final void aCase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST aCase_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_case:
		{
			AST tmp249_AST = null;
			tmp249_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp249_AST);
			match(LITERAL_case);
			expression(0);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_default:
		{
			AST tmp250_AST = null;
			tmp250_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp250_AST);
			match(LITERAL_default);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(COLON);
		nls();
		aCase_AST = (AST)currentAST.root;
		returnAST = aCase_AST;
	}
	
	public final void caseSList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST caseSList_AST = null;
		Token first = LT(1);
		
		statement(COLON);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop364:
		do {
			if ((LA(1)==SEMI||LA(1)==NLS)) {
				sep();
				{
				switch ( LA(1)) {
				case FINAL:
				case ABSTRACT:
				case STRICTFP:
				case LITERAL_import:
				case LITERAL_static:
				case LITERAL_def:
				case LBRACK:
				case IDENT:
				case STRING_LITERAL:
				case LPAREN:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				case LITERAL_this:
				case LITERAL_if:
				case LITERAL_while:
				case LITERAL_switch:
				case LITERAL_for:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case PLUS:
				case MINUS:
				case LITERAL_try:
				case LITERAL_false:
				case LITERAL_new:
				case LITERAL_null:
				case LITERAL_true:
				case INC:
				case DEC:
				case BNOT:
				case LNOT:
				case STRING_CTOR_START:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				{
					statement(sepToken);
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RCURLY:
				case SEMI:
				case LITERAL_default:
				case LITERAL_case:
				case NLS:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				break _loop364;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			caseSList_AST = (AST)currentAST.root;
			caseSList_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(SLIST,"SLIST",first,LT(1))).add(caseSList_AST));
			currentAST.root = caseSList_AST;
			currentAST.child = caseSList_AST!=null &&caseSList_AST.getFirstChild()!=null ?
				caseSList_AST.getFirstChild() : caseSList_AST;
			currentAST.advanceChildToEnd();
		}
		caseSList_AST = (AST)currentAST.root;
		returnAST = caseSList_AST;
	}
	
	public final void forInit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forInit_AST = null;
		Token first = LT(1);
		
		boolean synPredMatched367 = false;
		if (((_tokenSet_16.member(LA(1))) && (_tokenSet_17.member(LA(2))))) {
			int _m367 = mark();
			synPredMatched367 = true;
			inputState.guessing++;
			try {
				{
				declarationStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched367 = false;
			}
			rewind(_m367);
inputState.guessing--;
		}
		if ( synPredMatched367 ) {
			declaration();
			astFactory.addASTChild(currentAST, returnAST);
			forInit_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_94.member(LA(1))) && (_tokenSet_95.member(LA(2)))) {
			{
			switch ( LA(1)) {
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case LITERAL_def:
			case LBRACK:
			case IDENT:
			case STRING_LITERAL:
			case LPAREN:
			case AT:
			case LITERAL_super:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			case LITERAL_this:
			case LITERAL_return:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_throw:
			case LITERAL_assert:
			case PLUS:
			case MINUS:
			case LITERAL_false:
			case LITERAL_new:
			case LITERAL_null:
			case LITERAL_true:
			case INC:
			case DEC:
			case BNOT:
			case LNOT:
			case STRING_CTOR_START:
			case NUM_INT:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			case NUM_BIG_INT:
			case NUM_BIG_DECIMAL:
			{
				controlExpressionList();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				forInit_AST = (AST)currentAST.root;
				forInit_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(FOR_INIT,"FOR_INIT",first,LT(1))).add(forInit_AST));
				currentAST.root = forInit_AST;
				currentAST.child = forInit_AST!=null &&forInit_AST.getFirstChild()!=null ?
					forInit_AST.getFirstChild() : forInit_AST;
				currentAST.advanceChildToEnd();
			}
			forInit_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = forInit_AST;
	}
	
	public final void controlExpressionList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST controlExpressionList_AST = null;
		Token first = LT(1); boolean sce=false;
		
		sce=strictContextExpression(false);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop409:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				nls();
				sce=strictContextExpression(false);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop409;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			controlExpressionList_AST = (AST)currentAST.root;
			controlExpressionList_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(ELIST,"ELIST",first,LT(1))).add(controlExpressionList_AST));
			currentAST.root = controlExpressionList_AST;
			currentAST.child = controlExpressionList_AST!=null &&controlExpressionList_AST.getFirstChild()!=null ?
				controlExpressionList_AST.getFirstChild() : controlExpressionList_AST;
			currentAST.advanceChildToEnd();
		}
		controlExpressionList_AST = (AST)currentAST.root;
		returnAST = controlExpressionList_AST;
	}
	
	public final void forCond() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forCond_AST = null;
		Token first = LT(1); boolean sce=false;
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case AT:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case PLUS:
		case MINUS:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			sce=strictContextExpression(false);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			forCond_AST = (AST)currentAST.root;
			forCond_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(FOR_CONDITION,"FOR_CONDITION",first,LT(1))).add(forCond_AST));
			currentAST.root = forCond_AST;
			currentAST.child = forCond_AST!=null &&forCond_AST.getFirstChild()!=null ?
				forCond_AST.getFirstChild() : forCond_AST;
			currentAST.advanceChildToEnd();
		}
		forCond_AST = (AST)currentAST.root;
		returnAST = forCond_AST;
	}
	
	public final void forIter() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forIter_AST = null;
		Token first = LT(1);
		
		{
		switch ( LA(1)) {
		case FINAL:
		case ABSTRACT:
		case STRICTFP:
		case LITERAL_static:
		case LITERAL_def:
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case AT:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_this:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case PLUS:
		case MINUS:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case INC:
		case DEC:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			controlExpressionList();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			forIter_AST = (AST)currentAST.root;
			forIter_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(FOR_ITERATOR,"FOR_ITERATOR",first,LT(1))).add(forIter_AST));
			currentAST.root = forIter_AST;
			currentAST.child = forIter_AST!=null &&forIter_AST.getFirstChild()!=null ?
				forIter_AST.getFirstChild() : forIter_AST;
			currentAST.advanceChildToEnd();
		}
		forIter_AST = (AST)currentAST.root;
		returnAST = forIter_AST;
	}
	
	public final void handler() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST handler_AST = null;
		AST pd_AST = null;
		AST handlerCs_AST = null;
		Token first = LT(1);
		
		match(LITERAL_catch);
		match(LPAREN);
		multicatch();
		pd_AST = (AST)returnAST;
		match(RPAREN);
		nlsWarn();
		compoundStatement();
		handlerCs_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			handler_AST = (AST)currentAST.root;
			handler_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_catch,"catch",first,LT(1))).add(pd_AST).add(handlerCs_AST));
			currentAST.root = handler_AST;
			currentAST.child = handler_AST!=null &&handler_AST.getFirstChild()!=null ?
				handler_AST.getFirstChild() : handler_AST;
			currentAST.advanceChildToEnd();
		}
		handler_AST = (AST)currentAST.root;
		returnAST = handler_AST;
	}
	
	public final void finallyClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST finallyClause_AST = null;
		AST finallyCs_AST = null;
		Token first = LT(1);
		
		match(LITERAL_finally);
		nlsWarn();
		compoundStatement();
		finallyCs_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			finallyClause_AST = (AST)currentAST.root;
			finallyClause_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(LITERAL_finally,"finally",first,LT(1))).add(finallyCs_AST));
			currentAST.root = finallyClause_AST;
			currentAST.child = finallyClause_AST!=null &&finallyClause_AST.getFirstChild()!=null ?
				finallyClause_AST.getFirstChild() : finallyClause_AST;
			currentAST.advanceChildToEnd();
		}
		finallyClause_AST = (AST)currentAST.root;
		returnAST = finallyClause_AST;
	}
	
/** A member name (x.y) or element name (x[y]) can serve as a command name,
 *  which may be followed by a list of arguments.
 *  Unlike parenthesized arguments, these must be plain expressions,
 *  without labels or spread operators.
 */
	public final void commandArguments(
		AST head
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST commandArguments_AST = null;
		
		Token first = LT(1);
		
		
		try {      // for error handling
			commandArgument();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop381:
			do {
				if ((LA(1)==COMMA) && (_tokenSet_96.member(LA(2)))) {
					match(COMMA);
					nls();
					commandArgument();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop381;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				commandArguments_AST = (AST)currentAST.root;
				
				AST elist = (AST)astFactory.make( (new ASTArray(2)).add(create(ELIST,"ELIST",first,LT(1))).add(commandArguments_AST));
				AST headid = (AST)astFactory.make( (new ASTArray(3)).add(create(METHOD_CALL,"<command>",first,LT(1))).add(head).add(elist));
				commandArguments_AST = headid;
				
				currentAST.root = commandArguments_AST;
				currentAST.child = commandArguments_AST!=null &&commandArguments_AST.getFirstChild()!=null ?
					commandArguments_AST.getFirstChild() : commandArguments_AST;
				currentAST.advanceChildToEnd();
			}
			commandArguments_AST = (AST)currentAST.root;
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
				// GRECLIPSE1192
				// Do we need better recognition of the specific problem here? 
				// (if so, see the label recovery for GRECLIPSE1048)
				reportError(e);
				
			} else {
				throw e;
			}
		}
		returnAST = commandArguments_AST;
	}
	
	public final void commandArgument() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST commandArgument_AST = null;
		Token  c = null;
		AST c_AST = null;
		
		boolean synPredMatched397 = false;
		if (((_tokenSet_97.member(LA(1))) && (_tokenSet_98.member(LA(2))))) {
			int _m397 = mark();
			synPredMatched397 = true;
			inputState.guessing++;
			try {
				{
				argumentLabel();
				match(COLON);
				nls();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched397 = false;
			}
			rewind(_m397);
inputState.guessing--;
		}
		if ( synPredMatched397 ) {
			{
			argumentLabel();
			astFactory.addASTChild(currentAST, returnAST);
			c = LT(1);
			c_AST = astFactory.create(c);
			astFactory.makeASTRoot(currentAST, c_AST);
			match(COLON);
			nls();
			expression(0);
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				c_AST.setType(LABELED_ARG);
			}
			}
			commandArgument_AST = (AST)currentAST.root;
		}
		else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_38.member(LA(2)))) {
			expression(0);
			astFactory.addASTChild(currentAST, returnAST);
			commandArgument_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = commandArgument_AST;
	}
	
	public final void primaryExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST primaryExpression_AST = null;
		AST pe_AST = null;
		Token first = LT(1);
		
		switch ( LA(1)) {
		case IDENT:
		{
			AST tmp258_AST = null;
			tmp258_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp258_AST);
			match(IDENT);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case STRING_LITERAL:
		case LITERAL_false:
		case LITERAL_null:
		case LITERAL_true:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			constant();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_new:
		{
			newExpression();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_this:
		{
			AST tmp259_AST = null;
			tmp259_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp259_AST);
			match(LITERAL_this);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_super:
		{
			AST tmp260_AST = null;
			tmp260_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp260_AST);
			match(LITERAL_super);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LPAREN:
		{
			parenthesizedExpression();
			pe_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				primaryExpression_AST = (AST)currentAST.root;
				primaryExpression_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(EXPR,"EXPR",first,LT(1))).add(pe_AST));
				currentAST.root = primaryExpression_AST;
				currentAST.child = primaryExpression_AST!=null &&primaryExpression_AST.getFirstChild()!=null ?
					primaryExpression_AST.getFirstChild() : primaryExpression_AST;
				currentAST.advanceChildToEnd();
			}
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LCURLY:
		{
			closableBlockConstructorExpression();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		{
			listOrMapConstructorExpression();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case STRING_CTOR_START:
		{
			stringConstructorExpression();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		{
			builtInType();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = primaryExpression_AST;
	}
	
	public final void pathElementStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathElementStart_AST = null;
		
		switch ( LA(1)) {
		case DOT:
		case NLS:
		{
			{
			nls();
			AST tmp261_AST = null;
			tmp261_AST = astFactory.create(LT(1));
			match(DOT);
			}
			break;
		}
		case SPREAD_DOT:
		{
			AST tmp262_AST = null;
			tmp262_AST = astFactory.create(LT(1));
			match(SPREAD_DOT);
			break;
		}
		case OPTIONAL_DOT:
		{
			AST tmp263_AST = null;
			tmp263_AST = astFactory.create(LT(1));
			match(OPTIONAL_DOT);
			break;
		}
		case MEMBER_POINTER:
		{
			AST tmp264_AST = null;
			tmp264_AST = astFactory.create(LT(1));
			match(MEMBER_POINTER);
			break;
		}
		case LBRACK:
		{
			AST tmp265_AST = null;
			tmp265_AST = astFactory.create(LT(1));
			match(LBRACK);
			break;
		}
		case LPAREN:
		{
			AST tmp266_AST = null;
			tmp266_AST = astFactory.create(LT(1));
			match(LPAREN);
			break;
		}
		case LCURLY:
		{
			AST tmp267_AST = null;
			tmp267_AST = astFactory.create(LT(1));
			match(LCURLY);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = pathElementStart_AST;
	}
	
	public final void pathChain(
		int lc_stmt, AST prefix
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathChain_AST = null;
		AST pe_AST = null;
		AST apb_AST = null;
		
		{
		int _cnt416=0;
		_loop416:
		do {
			boolean synPredMatched413 = false;
			if (((_tokenSet_92.member(LA(1))) && (_tokenSet_93.member(LA(2))))) {
				int _m413 = mark();
				synPredMatched413 = true;
				inputState.guessing++;
				try {
					{
					pathElementStart();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched413 = false;
				}
				rewind(_m413);
inputState.guessing--;
			}
			if ( synPredMatched413 ) {
				nls();
				pathElement(prefix);
				pe_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					prefix = pe_AST;
				}
			}
			else {
				boolean synPredMatched415 = false;
				if ((((LA(1)==LCURLY||LA(1)==NLS) && (_tokenSet_18.member(LA(2))))&&(lc_stmt == LC_STMT || lc_stmt == LC_INIT))) {
					int _m415 = mark();
					synPredMatched415 = true;
					inputState.guessing++;
					try {
						{
						nls();
						match(LCURLY);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched415 = false;
					}
					rewind(_m415);
inputState.guessing--;
				}
				if ( synPredMatched415 ) {
					nlsWarn();
					appendedBlock(prefix);
					apb_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						prefix = apb_AST;
					}
				}
				else {
					if ( _cnt416>=1 ) { break _loop416; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				}
				_cnt416++;
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				pathChain_AST = (AST)currentAST.root;
				pathChain_AST = prefix;
				currentAST.root = pathChain_AST;
				currentAST.child = pathChain_AST!=null &&pathChain_AST.getFirstChild()!=null ?
					pathChain_AST.getFirstChild() : pathChain_AST;
				currentAST.advanceChildToEnd();
			}
			pathChain_AST = (AST)currentAST.root;
			returnAST = pathChain_AST;
		}
		
/** A label for an argument is of the form a:b, 'a':b, "a":b, (a):b, etc..
 *      The labels in (a:b), ('a':b), and ("a":b) are in all ways equivalent,
 *      except that the quotes allow more spellings.
 *  Equivalent dynamically computed labels are (('a'):b) and ("${'a'}":b)
 *  but not ((a):b) or "$a":b, since the latter cases evaluate (a) as a normal identifier.
 *      Bottom line:  If you want a truly variable label, use parens and say ((a):b).
 */
	public final void argumentLabel() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argumentLabel_AST = null;
		Token  id = null;
		AST id_AST = null;
		AST kw_AST = null;
		
		boolean synPredMatched564 = false;
		if (((LA(1)==IDENT) && (LA(2)==COLON))) {
			int _m564 = mark();
			synPredMatched564 = true;
			inputState.guessing++;
			try {
				{
				match(IDENT);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched564 = false;
			}
			rewind(_m564);
inputState.guessing--;
		}
		if ( synPredMatched564 ) {
			id = LT(1);
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(IDENT);
			if ( inputState.guessing==0 ) {
				id_AST.setType(STRING_LITERAL);
			}
			argumentLabel_AST = (AST)currentAST.root;
		}
		else {
			boolean synPredMatched566 = false;
			if (((_tokenSet_99.member(LA(1))) && (LA(2)==COLON))) {
				int _m566 = mark();
				synPredMatched566 = true;
				inputState.guessing++;
				try {
					{
					keywordPropertyNames();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched566 = false;
				}
				rewind(_m566);
inputState.guessing--;
			}
			if ( synPredMatched566 ) {
				keywordPropertyNames();
				kw_AST = (AST)returnAST;
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					kw_AST.setType(STRING_LITERAL);
				}
				argumentLabel_AST = (AST)currentAST.root;
			}
			else if ((_tokenSet_90.member(LA(1))) && (_tokenSet_98.member(LA(2)))) {
				primaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
				argumentLabel_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			returnAST = argumentLabel_AST;
		}
		
	public final void multipleAssignment(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multipleAssignment_AST = null;
		Token first = cloneToken(LT(1));
		
		AST tmp268_AST = null;
		tmp268_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp268_AST);
		match(LPAREN);
		nls();
		listOfVariables(null,null,first);
		astFactory.addASTChild(currentAST, returnAST);
		match(RPAREN);
		AST tmp270_AST = null;
		tmp270_AST = astFactory.create(LT(1));
		astFactory.makeASTRoot(currentAST, tmp270_AST);
		match(ASSIGN);
		nls();
		assignmentExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		multipleAssignment_AST = (AST)currentAST.root;
		returnAST = multipleAssignment_AST;
	}
	
	public final void pathElement(
		AST prefix
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathElement_AST = null;
		AST ta_AST = null;
		AST np_AST = null;
		AST mca_AST = null;
		AST apb_AST = null;
		AST ipa_AST = null;
		Token operator = LT(1);
		
		try {      // for error handling
			switch ( LA(1)) {
			case DOT:
			case SPREAD_DOT:
			case OPTIONAL_DOT:
			case MEMBER_POINTER:
			case NLS:
			{
				if ( inputState.guessing==0 ) {
					pathElement_AST = (AST)currentAST.root;
					pathElement_AST = prefix;
					currentAST.root = pathElement_AST;
					currentAST.child = pathElement_AST!=null &&pathElement_AST.getFirstChild()!=null ?
						pathElement_AST.getFirstChild() : pathElement_AST;
					currentAST.advanceChildToEnd();
				}
				{
				switch ( LA(1)) {
				case SPREAD_DOT:
				{
					match(SPREAD_DOT);
					break;
				}
				case OPTIONAL_DOT:
				{
					match(OPTIONAL_DOT);
					break;
				}
				case MEMBER_POINTER:
				{
					match(MEMBER_POINTER);
					break;
				}
				case DOT:
				case NLS:
				{
					{
					nls();
					match(DOT);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				{
				switch ( LA(1)) {
				case LT:
				{
					typeArguments();
					ta_AST = (AST)returnAST;
					break;
				}
				case FINAL:
				case ABSTRACT:
				case UNUSED_GOTO:
				case UNUSED_CONST:
				case UNUSED_DO:
				case STRICTFP:
				case LITERAL_package:
				case LITERAL_import:
				case LITERAL_static:
				case LITERAL_def:
				case IDENT:
				case STRING_LITERAL:
				case LPAREN:
				case LITERAL_class:
				case LITERAL_interface:
				case LITERAL_enum:
				case AT:
				case LITERAL_extends:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LITERAL_as:
				case LITERAL_private:
				case LITERAL_public:
				case LITERAL_protected:
				case LITERAL_transient:
				case LITERAL_native:
				case LITERAL_threadsafe:
				case LITERAL_synchronized:
				case LITERAL_volatile:
				case LCURLY:
				case LITERAL_default:
				case LITERAL_throws:
				case LITERAL_implements:
				case LITERAL_this:
				case LITERAL_if:
				case LITERAL_else:
				case LITERAL_while:
				case LITERAL_switch:
				case LITERAL_for:
				case LITERAL_in:
				case LITERAL_return:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_throw:
				case LITERAL_assert:
				case LITERAL_case:
				case LITERAL_try:
				case LITERAL_finally:
				case LITERAL_catch:
				case LITERAL_false:
				case LITERAL_instanceof:
				case LITERAL_new:
				case LITERAL_null:
				case LITERAL_true:
				case STRING_CTOR_START:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				namePart();
				np_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					pathElement_AST = (AST)currentAST.root;
					pathElement_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(operator.getType(),operator.getText(),prefix,LT(1))).add(prefix).add(ta_AST).add(np_AST));
					currentAST.root = pathElement_AST;
					currentAST.child = pathElement_AST!=null &&pathElement_AST.getFirstChild()!=null ?
						pathElement_AST.getFirstChild() : pathElement_AST;
					currentAST.advanceChildToEnd();
				}
				pathElement_AST = (AST)currentAST.root;
				break;
			}
			case LPAREN:
			{
				methodCallArgs(prefix);
				mca_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					pathElement_AST = (AST)currentAST.root;
					pathElement_AST = mca_AST;
					currentAST.root = pathElement_AST;
					currentAST.child = pathElement_AST!=null &&pathElement_AST.getFirstChild()!=null ?
						pathElement_AST.getFirstChild() : pathElement_AST;
					currentAST.advanceChildToEnd();
				}
				pathElement_AST = (AST)currentAST.root;
				break;
			}
			case LCURLY:
			{
				appendedBlock(prefix);
				apb_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					pathElement_AST = (AST)currentAST.root;
					pathElement_AST = apb_AST;
					currentAST.root = pathElement_AST;
					currentAST.child = pathElement_AST!=null &&pathElement_AST.getFirstChild()!=null ?
						pathElement_AST.getFirstChild() : pathElement_AST;
					currentAST.advanceChildToEnd();
				}
				pathElement_AST = (AST)currentAST.root;
				break;
			}
			case LBRACK:
			{
				indexPropertyArgs(prefix);
				ipa_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					pathElement_AST = (AST)currentAST.root;
					pathElement_AST = ipa_AST;
					currentAST.root = pathElement_AST;
					currentAST.child = pathElement_AST!=null &&pathElement_AST.getFirstChild()!=null ?
						pathElement_AST.getFirstChild() : pathElement_AST;
					currentAST.advanceChildToEnd();
				}
				pathElement_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
					if (pathElement_AST==null) {
								throw e;
							}
					reportError(e);
				
			} else {
				throw e;
			}
		}
		returnAST = pathElement_AST;
	}
	
/** An appended block follows any expression.
 *  If the expression is not a method call, it is given an empty argument list.
 */
	public final void appendedBlock(
		AST callee
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST appendedBlock_AST = null;
		AST cb_AST = null;
		
		closableBlock();
		cb_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			appendedBlock_AST = (AST)currentAST.root;
			
			// If the callee is itself a call, flatten the AST.
			if (callee != null && callee.getType() == METHOD_CALL) {
			appendedBlock_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(METHOD_CALL,"(",callee,LT(1))).add(callee.getFirstChild()).add(cb_AST));
			} else {
			appendedBlock_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(METHOD_CALL,"{",callee,LT(1))).add(callee).add(cb_AST));
			}
			
			currentAST.root = appendedBlock_AST;
			currentAST.child = appendedBlock_AST!=null &&appendedBlock_AST.getFirstChild()!=null ?
				appendedBlock_AST.getFirstChild() : appendedBlock_AST;
			currentAST.advanceChildToEnd();
		}
		appendedBlock_AST = (AST)currentAST.root;
		returnAST = appendedBlock_AST;
	}
	
/** A "path expression" is a name or other primary, possibly qualified by various
 *  forms of dot, and/or followed by various kinds of brackets.
 *  It can be used for value or assigned to, or else further qualified, indexed, or called.
 *  It is called a "path" because it looks like a linear path through a data structure.
 *  Examples:  x.y, x?.y, x*.y, x.@y; x[], x[y], x[y,z]; x(), x(y), x(y,z); x{s}; a.b[n].c(x).d{s}
 *  (Compare to a C lvalue, or LeftHandSide in the JLS section 15.26.)
 *  General expressions are built up from path expressions, using operators like '+' and '='.
 */
	public final void pathExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST pathExpression_AST = null;
		AST pre_AST = null;
		AST pe_AST = null;
		AST apb_AST = null;
		AST prefix = null;
		
		primaryExpression();
		pre_AST = (AST)returnAST;
		if ( inputState.guessing==0 ) {
			prefix = pre_AST;
		}
		{
		_loop424:
		do {
			boolean synPredMatched420 = false;
			if (((_tokenSet_92.member(LA(1))) && (_tokenSet_93.member(LA(2))))) {
				int _m420 = mark();
				synPredMatched420 = true;
				inputState.guessing++;
				try {
					{
					pathElementStart();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched420 = false;
				}
				rewind(_m420);
inputState.guessing--;
			}
			if ( synPredMatched420 ) {
				nls();
				pathElement(prefix);
				pe_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					prefix = pe_AST;
				}
			}
			else {
				boolean synPredMatched422 = false;
				if ((((LA(1)==LCURLY||LA(1)==NLS) && (_tokenSet_18.member(LA(2))))&&(lc_stmt == LC_STMT || lc_stmt == LC_INIT))) {
					int _m422 = mark();
					synPredMatched422 = true;
					inputState.guessing++;
					try {
						{
						nls();
						match(LCURLY);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched422 = false;
					}
					rewind(_m422);
inputState.guessing--;
				}
				if ( synPredMatched422 ) {
					nlsWarn();
					appendedBlock(prefix);
					apb_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						prefix = apb_AST;
					}
				}
				else if ((_tokenSet_100.member(LA(1))) && (_tokenSet_101.member(LA(2)))) {
					{
					switch ( LA(1)) {
					case DOT:
					{
						match(DOT);
						break;
					}
					case SPREAD_DOT:
					{
						match(SPREAD_DOT);
						break;
					}
					case OPTIONAL_DOT:
					{
						AST tmp277_AST = null;
						tmp277_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp277_AST);
						match(OPTIONAL_DOT);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						reportError("Expecting an identifier, found a trailing '.' instead.");
					}
				}
				else {
					break _loop424;
				}
				}
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				pathExpression_AST = (AST)currentAST.root;
				
				pathExpression_AST = prefix;
				lastPathExpression = pathExpression_AST;
				
				currentAST.root = pathExpression_AST;
				currentAST.child = pathExpression_AST!=null &&pathExpression_AST.getFirstChild()!=null ?
					pathExpression_AST.getFirstChild() : pathExpression_AST;
				currentAST.advanceChildToEnd();
			}
			pathExpression_AST = (AST)currentAST.root;
			returnAST = pathExpression_AST;
		}
		
/** This is the grammar for what can follow a dot:  x.a, x.@a, x.&a, x.'a', etc.
 *  Note: <code>typeArguments</code> is handled by the caller of <code>namePart</code>.
 */
	public final void namePart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST namePart_AST = null;
		Token  ats = null;
		AST ats_AST = null;
		Token  sl = null;
		AST sl_AST = null;
		Token first = LT(1);
		
		{
		switch ( LA(1)) {
		case AT:
		{
			ats = LT(1);
			ats_AST = astFactory.create(ats);
			astFactory.makeASTRoot(currentAST, ats_AST);
			match(AT);
			if ( inputState.guessing==0 ) {
				ats_AST.setType(SELECT_SLOT);
			}
			break;
		}
		case FINAL:
		case ABSTRACT:
		case UNUSED_GOTO:
		case UNUSED_CONST:
		case UNUSED_DO:
		case STRICTFP:
		case LITERAL_package:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case LITERAL_extends:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_as:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LCURLY:
		case LITERAL_default:
		case LITERAL_throws:
		case LITERAL_implements:
		case LITERAL_this:
		case LITERAL_if:
		case LITERAL_else:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case LITERAL_case:
		case LITERAL_try:
		case LITERAL_finally:
		case LITERAL_catch:
		case LITERAL_false:
		case LITERAL_instanceof:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case STRING_CTOR_START:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case IDENT:
		{
			AST tmp278_AST = null;
			tmp278_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp278_AST);
			match(IDENT);
			break;
		}
		case STRING_LITERAL:
		{
			sl = LT(1);
			sl_AST = astFactory.create(sl);
			astFactory.addASTChild(currentAST, sl_AST);
			match(STRING_LITERAL);
			if ( inputState.guessing==0 ) {
				sl_AST.setType(IDENT);
			}
			break;
		}
		case LPAREN:
		case STRING_CTOR_START:
		{
			dynamicMemberName();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LCURLY:
		{
			openBlock();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case FINAL:
		case ABSTRACT:
		case UNUSED_GOTO:
		case UNUSED_CONST:
		case UNUSED_DO:
		case STRICTFP:
		case LITERAL_package:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case LITERAL_extends:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_as:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LITERAL_default:
		case LITERAL_throws:
		case LITERAL_implements:
		case LITERAL_this:
		case LITERAL_if:
		case LITERAL_else:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case LITERAL_case:
		case LITERAL_try:
		case LITERAL_finally:
		case LITERAL_catch:
		case LITERAL_false:
		case LITERAL_instanceof:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		{
			keywordPropertyNames();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		namePart_AST = (AST)currentAST.root;
		returnAST = namePart_AST;
	}
	
/** An expression may be followed by one or both of (...) and {...}.
 *  Note: If either is (...) or {...} present, it is a method call.
 *  The {...} is appended to the argument list, and matches a formal of type Closure.
 *  If there is no method member, a property (or field) is used instead, and must itself be callable.
 *  <p>
 *  If the methodCallArgs are absent, it is a property reference.
 *  If there is no property, it is treated as a field reference, but never a method reference.
 *  <p>
 *  Arguments in the (...) can be labeled, and the appended block can be labeled also.
 *  If there is a mix of unlabeled and labeled arguments,
 *  all the labeled arguments must follow the unlabeled arguments,
 *  except that the closure (labeled or not) is always a separate final argument.
 *  Labeled arguments are collected up and passed as a single argument to a formal of type Map.
 *  <p>
 *  Therefore, f(x,y, a:p, b:q) {s} is equivalent in all ways to f(x,y, [a:p,b:q], {s}).
 *  Spread arguments of sequence type count as unlabeled arguments,
 *  while spread arguments of map type count as labeled arguments.
 *  (This distinction must sometimes be checked dynamically.)
 *
 *  A plain unlabeled argument is allowed to match a trailing Map or Closure argument:
 *  f(x, a:p) {s}  ===  f(*[ x, [a:p], {s} ])
 */
	public final void methodCallArgs(
		AST callee
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST methodCallArgs_AST = null;
		AST al_AST = null;
		
		try {      // for error handling
			match(LPAREN);
			argList();
			al_AST = (AST)returnAST;
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				methodCallArgs_AST = (AST)currentAST.root;
				if (callee != null && callee.getFirstChild() != null) {
				//method call like obj.method()
				methodCallArgs_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(METHOD_CALL,"(",callee.getFirstChild(),LT(1))).add(callee).add(al_AST));
				} else {
				//method call like method() or new Expr(), in the latter case "callee" is null
				methodCallArgs_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(METHOD_CALL,"(",callee,LT(1))).add(callee).add(al_AST));
				}
				
				currentAST.root = methodCallArgs_AST;
				currentAST.child = methodCallArgs_AST!=null &&methodCallArgs_AST.getFirstChild()!=null ?
					methodCallArgs_AST.getFirstChild() : methodCallArgs_AST;
				currentAST.advanceChildToEnd();
			}
			methodCallArgs_AST = (AST)currentAST.root;
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
				if (al_AST!=null) {
					reportError(e);
					// copy of the block above - lets build it (assuming that all that was missing was the RPAREN)
					if (callee != null && callee.getFirstChild() != null) {
						//method call like obj.method()
						methodCallArgs_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(METHOD_CALL,"(",callee.getFirstChild(),LT(1))).add(callee).add(al_AST));
					} else {
						//method call like method() or new Expr(), in the latter case "callee" is null
						methodCallArgs_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(METHOD_CALL,"(",callee,LT(1))).add(callee).add(al_AST));
					}
				} else {
					throw e;
				}
				
			} else {
				throw e;
			}
		}
		returnAST = methodCallArgs_AST;
	}
	
/** An expression may be followed by [...].
 *  Unlike Java, these brackets may contain a general argument list,
 *  which is passed to the array element operator, which can make of it what it wants.
 *  The brackets may also be empty, as in T[].  This is how Groovy names array types.
 *  <p>Returned AST is [INDEX_OP, indexee, ELIST].
 */
	public final void indexPropertyArgs(
		AST indexee
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST indexPropertyArgs_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		AST al_AST = null;
		
		lb = LT(1);
		lb_AST = astFactory.create(lb);
		astFactory.addASTChild(currentAST, lb_AST);
		match(LBRACK);
		argList();
		al_AST = (AST)returnAST;
		match(RBRACK);
		if ( inputState.guessing==0 ) {
			indexPropertyArgs_AST = (AST)currentAST.root;
			if (indexee != null && indexee.getFirstChild() != null) {
			//expression like obj.index[]
			indexPropertyArgs_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(INDEX_OP,"INDEX_OP",indexee.getFirstChild(),LT(1))).add(lb_AST).add(indexee).add(al_AST));
			} else {
			//expression like obj[]
			indexPropertyArgs_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(INDEX_OP,"INDEX_OP",indexee,LT(1))).add(lb_AST).add(indexee).add(al_AST));
			}
			
			currentAST.root = indexPropertyArgs_AST;
			currentAST.child = indexPropertyArgs_AST!=null &&indexPropertyArgs_AST.getFirstChild()!=null ?
				indexPropertyArgs_AST.getFirstChild() : indexPropertyArgs_AST;
			currentAST.advanceChildToEnd();
		}
		indexPropertyArgs_AST = (AST)currentAST.root;
		returnAST = indexPropertyArgs_AST;
	}
	
/** If a dot is followed by a parenthesized or quoted expression, the member is computed dynamically,
 *  and the member selection is done only at runtime.  This forces a statically unchecked member access.
 */
	public final void dynamicMemberName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST dynamicMemberName_AST = null;
		AST pe_AST = null;
		Token first = LT(1);
		
		{
		switch ( LA(1)) {
		case LPAREN:
		{
			parenthesizedExpression();
			pe_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				dynamicMemberName_AST = (AST)currentAST.root;
				dynamicMemberName_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(EXPR,"EXPR",first,LT(1))).add(pe_AST));
				currentAST.root = dynamicMemberName_AST;
				currentAST.child = dynamicMemberName_AST!=null &&dynamicMemberName_AST.getFirstChild()!=null ?
					dynamicMemberName_AST.getFirstChild() : dynamicMemberName_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		case STRING_CTOR_START:
		{
			stringConstructorExpression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			dynamicMemberName_AST = (AST)currentAST.root;
			dynamicMemberName_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(DYNAMIC_MEMBER,"DYNAMIC_MEMBER",first,LT(1))).add(dynamicMemberName_AST));
			currentAST.root = dynamicMemberName_AST;
			currentAST.child = dynamicMemberName_AST!=null &&dynamicMemberName_AST.getFirstChild()!=null ?
				dynamicMemberName_AST.getFirstChild() : dynamicMemberName_AST;
			currentAST.advanceChildToEnd();
		}
		dynamicMemberName_AST = (AST)currentAST.root;
		returnAST = dynamicMemberName_AST;
	}
	
	public final void parenthesizedExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parenthesizedExpression_AST = null;
		Token first = LT(1);
		Token declaration = null;
		boolean hasClosureList=false;
		boolean firstContainsDeclaration=false;
		boolean sce=false;
		
		
		try {      // for error handling
			match(LPAREN);
			if ( inputState.guessing==0 ) {
				declaration=LT(1);
			}
			firstContainsDeclaration=strictContextExpression(true);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop524:
			do {
				if ((LA(1)==SEMI)) {
					match(SEMI);
					if ( inputState.guessing==0 ) {
						hasClosureList=true;
					}
					{
					switch ( LA(1)) {
					case FINAL:
					case ABSTRACT:
					case STRICTFP:
					case LITERAL_static:
					case LITERAL_def:
					case LBRACK:
					case IDENT:
					case STRING_LITERAL:
					case LPAREN:
					case AT:
					case LITERAL_super:
					case LITERAL_void:
					case LITERAL_boolean:
					case LITERAL_byte:
					case LITERAL_char:
					case LITERAL_short:
					case LITERAL_int:
					case LITERAL_float:
					case LITERAL_long:
					case LITERAL_double:
					case LITERAL_private:
					case LITERAL_public:
					case LITERAL_protected:
					case LITERAL_transient:
					case LITERAL_native:
					case LITERAL_threadsafe:
					case LITERAL_synchronized:
					case LITERAL_volatile:
					case LCURLY:
					case LITERAL_this:
					case LITERAL_return:
					case LITERAL_break:
					case LITERAL_continue:
					case LITERAL_throw:
					case LITERAL_assert:
					case PLUS:
					case MINUS:
					case LITERAL_false:
					case LITERAL_new:
					case LITERAL_null:
					case LITERAL_true:
					case INC:
					case DEC:
					case BNOT:
					case LNOT:
					case STRING_CTOR_START:
					case NUM_INT:
					case NUM_FLOAT:
					case NUM_LONG:
					case NUM_DOUBLE:
					case NUM_BIG_INT:
					case NUM_BIG_DECIMAL:
					{
						sce=strictContextExpression(true);
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case RPAREN:
					case SEMI:
					{
						if ( inputState.guessing==0 ) {
							astFactory.addASTChild(currentAST,astFactory.create(EMPTY_STAT, "EMPTY_STAT"));
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop524;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
				if (firstContainsDeclaration && !hasClosureList)
				throw new NoViableAltException(declaration, getFilename());
				
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				parenthesizedExpression_AST = (AST)currentAST.root;
				
				if (hasClosureList) {
				parenthesizedExpression_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(CLOSURE_LIST,"CLOSURE_LIST",first,LT(1))).add(parenthesizedExpression_AST));
				}
				
				currentAST.root = parenthesizedExpression_AST;
				currentAST.child = parenthesizedExpression_AST!=null &&parenthesizedExpression_AST.getFirstChild()!=null ?
					parenthesizedExpression_AST.getFirstChild() : parenthesizedExpression_AST;
				currentAST.advanceChildToEnd();
			}
			parenthesizedExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
					// GRECLIPSE1213 - missing closing paren
					reportError(e); 
					parenthesizedExpression_AST = (AST)currentAST.root;
				
			} else {
				throw e;
			}
		}
		returnAST = parenthesizedExpression_AST;
	}
	
	public final void stringConstructorExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST stringConstructorExpression_AST = null;
		Token  cs = null;
		AST cs_AST = null;
		Token  cm = null;
		AST cm_AST = null;
		Token  ce = null;
		AST ce_AST = null;
		Token first = LT(1);
		
		cs = LT(1);
		cs_AST = astFactory.create(cs);
		astFactory.addASTChild(currentAST, cs_AST);
		match(STRING_CTOR_START);
		if ( inputState.guessing==0 ) {
			cs_AST.setType(STRING_LITERAL);
		}
		stringConstructorValuePart();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop534:
		do {
			if ((LA(1)==STRING_CTOR_MIDDLE)) {
				cm = LT(1);
				cm_AST = astFactory.create(cm);
				astFactory.addASTChild(currentAST, cm_AST);
				match(STRING_CTOR_MIDDLE);
				if ( inputState.guessing==0 ) {
					cm_AST.setType(STRING_LITERAL);
				}
				stringConstructorValuePart();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop534;
			}
			
		} while (true);
		}
		ce = LT(1);
		ce_AST = astFactory.create(ce);
		astFactory.addASTChild(currentAST, ce_AST);
		match(STRING_CTOR_END);
		if ( inputState.guessing==0 ) {
			stringConstructorExpression_AST = (AST)currentAST.root;
			ce_AST.setType(STRING_LITERAL);
			stringConstructorExpression_AST =
			(AST)astFactory.make( (new ASTArray(2)).add(create(STRING_CONSTRUCTOR,"STRING_CONSTRUCTOR",first,LT(1))).add(stringConstructorExpression_AST));
			
			currentAST.root = stringConstructorExpression_AST;
			currentAST.child = stringConstructorExpression_AST!=null &&stringConstructorExpression_AST.getFirstChild()!=null ?
				stringConstructorExpression_AST.getFirstChild() : stringConstructorExpression_AST;
			currentAST.advanceChildToEnd();
		}
		stringConstructorExpression_AST = (AST)currentAST.root;
		returnAST = stringConstructorExpression_AST;
	}
	
	public final void logicalOrExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalOrExpression_AST = null;
		
		logicalAndExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop450:
		do {
			if ((LA(1)==LOR)) {
				AST tmp285_AST = null;
				tmp285_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp285_AST);
				match(LOR);
				nls();
				logicalAndExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop450;
			}
			
		} while (true);
		}
		logicalOrExpression_AST = (AST)currentAST.root;
		returnAST = logicalOrExpression_AST;
	}
	
	public final void logicalAndExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalAndExpression_AST = null;
		
		inclusiveOrExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop453:
		do {
			if ((LA(1)==LAND)) {
				AST tmp286_AST = null;
				tmp286_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp286_AST);
				match(LAND);
				nls();
				inclusiveOrExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop453;
			}
			
		} while (true);
		}
		logicalAndExpression_AST = (AST)currentAST.root;
		returnAST = logicalAndExpression_AST;
	}
	
	public final void inclusiveOrExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST inclusiveOrExpression_AST = null;
		
		exclusiveOrExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop456:
		do {
			if ((LA(1)==BOR)) {
				AST tmp287_AST = null;
				tmp287_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp287_AST);
				match(BOR);
				nls();
				exclusiveOrExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop456;
			}
			
		} while (true);
		}
		inclusiveOrExpression_AST = (AST)currentAST.root;
		returnAST = inclusiveOrExpression_AST;
	}
	
	public final void exclusiveOrExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST exclusiveOrExpression_AST = null;
		
		andExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop459:
		do {
			if ((LA(1)==BXOR)) {
				AST tmp288_AST = null;
				tmp288_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp288_AST);
				match(BXOR);
				nls();
				andExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop459;
			}
			
		} while (true);
		}
		exclusiveOrExpression_AST = (AST)currentAST.root;
		returnAST = exclusiveOrExpression_AST;
	}
	
	public final void andExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST andExpression_AST = null;
		
		regexExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop462:
		do {
			if ((LA(1)==BAND)) {
				AST tmp289_AST = null;
				tmp289_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp289_AST);
				match(BAND);
				nls();
				regexExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop462;
			}
			
		} while (true);
		}
		andExpression_AST = (AST)currentAST.root;
		returnAST = andExpression_AST;
	}
	
	public final void regexExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST regexExpression_AST = null;
		
		equalityExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop466:
		do {
			if ((LA(1)==REGEX_FIND||LA(1)==REGEX_MATCH)) {
				{
				switch ( LA(1)) {
				case REGEX_FIND:
				{
					AST tmp290_AST = null;
					tmp290_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp290_AST);
					match(REGEX_FIND);
					break;
				}
				case REGEX_MATCH:
				{
					AST tmp291_AST = null;
					tmp291_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp291_AST);
					match(REGEX_MATCH);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				equalityExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop466;
			}
			
		} while (true);
		}
		regexExpression_AST = (AST)currentAST.root;
		returnAST = regexExpression_AST;
	}
	
	public final void equalityExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST equalityExpression_AST = null;
		
		relationalExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop470:
		do {
			if (((LA(1) >= NOT_EQUAL && LA(1) <= COMPARE_TO))) {
				{
				switch ( LA(1)) {
				case NOT_EQUAL:
				{
					AST tmp292_AST = null;
					tmp292_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp292_AST);
					match(NOT_EQUAL);
					break;
				}
				case EQUAL:
				{
					AST tmp293_AST = null;
					tmp293_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp293_AST);
					match(EQUAL);
					break;
				}
				case IDENTICAL:
				{
					AST tmp294_AST = null;
					tmp294_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp294_AST);
					match(IDENTICAL);
					break;
				}
				case NOT_IDENTICAL:
				{
					AST tmp295_AST = null;
					tmp295_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp295_AST);
					match(NOT_IDENTICAL);
					break;
				}
				case COMPARE_TO:
				{
					AST tmp296_AST = null;
					tmp296_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp296_AST);
					match(COMPARE_TO);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				relationalExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop470;
			}
			
		} while (true);
		}
		equalityExpression_AST = (AST)currentAST.root;
		returnAST = equalityExpression_AST;
	}
	
	public final void relationalExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relationalExpression_AST = null;
		
		shiftExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		if ((_tokenSet_102.member(LA(1))) && (_tokenSet_84.member(LA(2)))) {
			{
			{
			switch ( LA(1)) {
			case LT:
			{
				AST tmp297_AST = null;
				tmp297_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp297_AST);
				match(LT);
				break;
			}
			case GT:
			{
				AST tmp298_AST = null;
				tmp298_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp298_AST);
				match(GT);
				break;
			}
			case LE:
			{
				AST tmp299_AST = null;
				tmp299_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp299_AST);
				match(LE);
				break;
			}
			case GE:
			{
				AST tmp300_AST = null;
				tmp300_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp300_AST);
				match(GE);
				break;
			}
			case LITERAL_in:
			{
				AST tmp301_AST = null;
				tmp301_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp301_AST);
				match(LITERAL_in);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			nls();
			shiftExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			}
		}
		else if ((LA(1)==LITERAL_instanceof) && (_tokenSet_103.member(LA(2)))) {
			AST tmp302_AST = null;
			tmp302_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp302_AST);
			match(LITERAL_instanceof);
			nls();
			typeSpec(true);
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((LA(1)==LITERAL_as) && (_tokenSet_103.member(LA(2)))) {
			AST tmp303_AST = null;
			tmp303_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp303_AST);
			match(LITERAL_as);
			nls();
			typeSpec(true);
			astFactory.addASTChild(currentAST, returnAST);
		}
		else if ((_tokenSet_104.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		relationalExpression_AST = (AST)currentAST.root;
		returnAST = relationalExpression_AST;
	}
	
	public final void additiveExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST additiveExpression_AST = null;
		
		multiplicativeExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop483:
		do {
			if ((LA(1)==PLUS||LA(1)==MINUS) && (_tokenSet_84.member(LA(2)))) {
				{
				switch ( LA(1)) {
				case PLUS:
				{
					AST tmp304_AST = null;
					tmp304_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp304_AST);
					match(PLUS);
					break;
				}
				case MINUS:
				{
					AST tmp305_AST = null;
					tmp305_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp305_AST);
					match(MINUS);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				nls();
				multiplicativeExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop483;
			}
			
		} while (true);
		}
		additiveExpression_AST = (AST)currentAST.root;
		returnAST = additiveExpression_AST;
	}
	
	public final void multiplicativeExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multiplicativeExpression_AST = null;
		
		switch ( LA(1)) {
		case INC:
		{
			{
			AST tmp306_AST = null;
			tmp306_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp306_AST);
			match(INC);
			nls();
			powerExpressionNotPlusMinus(0);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop488:
			do {
				if ((_tokenSet_105.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp307_AST = null;
						tmp307_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp307_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp308_AST = null;
						tmp308_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp308_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp309_AST = null;
						tmp309_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp309_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression(0);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop488;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		case DEC:
		{
			{
			AST tmp310_AST = null;
			tmp310_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp310_AST);
			match(DEC);
			nls();
			powerExpressionNotPlusMinus(0);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop492:
			do {
				if ((_tokenSet_105.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp311_AST = null;
						tmp311_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp311_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp312_AST = null;
						tmp312_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp312_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp313_AST = null;
						tmp313_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp313_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression(0);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop492;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		case MINUS:
		{
			{
			AST tmp314_AST = null;
			tmp314_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp314_AST);
			match(MINUS);
			if ( inputState.guessing==0 ) {
				tmp314_AST.setType(UNARY_MINUS);
			}
			nls();
			powerExpressionNotPlusMinus(0);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop496:
			do {
				if ((_tokenSet_105.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp315_AST = null;
						tmp315_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp315_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp316_AST = null;
						tmp316_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp316_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp317_AST = null;
						tmp317_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp317_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression(0);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop496;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		case PLUS:
		{
			{
			AST tmp318_AST = null;
			tmp318_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp318_AST);
			match(PLUS);
			if ( inputState.guessing==0 ) {
				tmp318_AST.setType(UNARY_PLUS);
			}
			nls();
			powerExpressionNotPlusMinus(0);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop500:
			do {
				if ((_tokenSet_105.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp319_AST = null;
						tmp319_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp319_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp320_AST = null;
						tmp320_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp320_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp321_AST = null;
						tmp321_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp321_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression(0);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop500;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LCURLY:
		case LITERAL_this:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			{
			powerExpressionNotPlusMinus(lc_stmt);
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop504:
			do {
				if ((_tokenSet_105.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp322_AST = null;
						tmp322_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp322_AST);
						match(STAR);
						break;
					}
					case DIV:
					{
						AST tmp323_AST = null;
						tmp323_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp323_AST);
						match(DIV);
						break;
					}
					case MOD:
					{
						AST tmp324_AST = null;
						tmp324_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp324_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					nls();
					powerExpression(0);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop504;
				}
				
			} while (true);
			}
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = multiplicativeExpression_AST;
	}
	
	public final void powerExpressionNotPlusMinus(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST powerExpressionNotPlusMinus_AST = null;
		
		unaryExpressionNotPlusMinus(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop510:
		do {
			if ((LA(1)==STAR_STAR)) {
				AST tmp325_AST = null;
				tmp325_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp325_AST);
				match(STAR_STAR);
				nls();
				unaryExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop510;
			}
			
		} while (true);
		}
		powerExpressionNotPlusMinus_AST = (AST)currentAST.root;
		returnAST = powerExpressionNotPlusMinus_AST;
	}
	
	public final void powerExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST powerExpression_AST = null;
		
		unaryExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop507:
		do {
			if ((LA(1)==STAR_STAR)) {
				AST tmp326_AST = null;
				tmp326_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp326_AST);
				match(STAR_STAR);
				nls();
				unaryExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop507;
			}
			
		} while (true);
		}
		powerExpression_AST = (AST)currentAST.root;
		returnAST = powerExpression_AST;
	}
	
	public final void unaryExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpression_AST = null;
		
		switch ( LA(1)) {
		case INC:
		{
			AST tmp327_AST = null;
			tmp327_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp327_AST);
			match(INC);
			nls();
			unaryExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case DEC:
		{
			AST tmp328_AST = null;
			tmp328_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp328_AST);
			match(DEC);
			nls();
			unaryExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case MINUS:
		{
			AST tmp329_AST = null;
			tmp329_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp329_AST);
			match(MINUS);
			if ( inputState.guessing==0 ) {
				tmp329_AST.setType(UNARY_MINUS);
			}
			nls();
			unaryExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case PLUS:
		{
			AST tmp330_AST = null;
			tmp330_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp330_AST);
			match(PLUS);
			if ( inputState.guessing==0 ) {
				tmp330_AST.setType(UNARY_PLUS);
			}
			nls();
			unaryExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LCURLY:
		case LITERAL_this:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case BNOT:
		case LNOT:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			unaryExpressionNotPlusMinus(lc_stmt);
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = unaryExpression_AST;
	}
	
	public final void unaryExpressionNotPlusMinus(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpressionNotPlusMinus_AST = null;
		Token  lpb = null;
		AST lpb_AST = null;
		Token  lp = null;
		AST lp_AST = null;
		
		switch ( LA(1)) {
		case BNOT:
		{
			AST tmp331_AST = null;
			tmp331_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp331_AST);
			match(BNOT);
			nls();
			unaryExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
			break;
		}
		case LNOT:
		{
			AST tmp332_AST = null;
			tmp332_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp332_AST);
			match(LNOT);
			nls();
			unaryExpression(0);
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
			break;
		}
		case LBRACK:
		case IDENT:
		case STRING_LITERAL:
		case LPAREN:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LCURLY:
		case LITERAL_this:
		case LITERAL_false:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		case STRING_CTOR_START:
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			{
			boolean synPredMatched515 = false;
			if (((LA(1)==LPAREN) && ((LA(2) >= LITERAL_void && LA(2) <= LITERAL_double)))) {
				int _m515 = mark();
				synPredMatched515 = true;
				inputState.guessing++;
				try {
					{
					match(LPAREN);
					builtInTypeSpec(true);
					match(RPAREN);
					unaryExpression(0);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched515 = false;
				}
				rewind(_m515);
inputState.guessing--;
			}
			if ( synPredMatched515 ) {
				lpb = LT(1);
				lpb_AST = astFactory.create(lpb);
				astFactory.makeASTRoot(currentAST, lpb_AST);
				match(LPAREN);
				if ( inputState.guessing==0 ) {
					lpb_AST.setType(TYPECAST);
				}
				builtInTypeSpec(true);
				astFactory.addASTChild(currentAST, returnAST);
				match(RPAREN);
				unaryExpression(0);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				boolean synPredMatched517 = false;
				if (((LA(1)==LPAREN) && (LA(2)==IDENT))) {
					int _m517 = mark();
					synPredMatched517 = true;
					inputState.guessing++;
					try {
						{
						match(LPAREN);
						classTypeSpec(true);
						match(RPAREN);
						unaryExpressionNotPlusMinus(0);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched517 = false;
					}
					rewind(_m517);
inputState.guessing--;
				}
				if ( synPredMatched517 ) {
					lp = LT(1);
					lp_AST = astFactory.create(lp);
					astFactory.makeASTRoot(currentAST, lp_AST);
					match(LPAREN);
					if ( inputState.guessing==0 ) {
						lp_AST.setType(TYPECAST);
					}
					classTypeSpec(true);
					astFactory.addASTChild(currentAST, returnAST);
					match(RPAREN);
					unaryExpressionNotPlusMinus(0);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((_tokenSet_90.member(LA(1))) && (_tokenSet_38.member(LA(2)))) {
					postfixExpression(lc_stmt);
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			returnAST = unaryExpressionNotPlusMinus_AST;
		}
		
	public final void postfixExpression(
		int lc_stmt
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST postfixExpression_AST = null;
		Token  in = null;
		AST in_AST = null;
		Token  de = null;
		AST de_AST = null;
		
		pathExpression(lc_stmt);
		astFactory.addASTChild(currentAST, returnAST);
		{
		if ((LA(1)==INC) && (_tokenSet_106.member(LA(2)))) {
			in = LT(1);
			in_AST = astFactory.create(in);
			astFactory.makeASTRoot(currentAST, in_AST);
			match(INC);
			if ( inputState.guessing==0 ) {
				in_AST.setType(POST_INC);
			}
		}
		else if ((LA(1)==DEC) && (_tokenSet_106.member(LA(2)))) {
			de = LT(1);
			de_AST = astFactory.create(de);
			astFactory.makeASTRoot(currentAST, de_AST);
			match(DEC);
			if ( inputState.guessing==0 ) {
				de_AST.setType(POST_DEC);
			}
		}
		else if ((_tokenSet_106.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		postfixExpression_AST = (AST)currentAST.root;
		returnAST = postfixExpression_AST;
	}
	
/** Numeric, string, regexp, boolean, or null constant. */
	public final void constant() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constant_AST = null;
		
		switch ( LA(1)) {
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			constantNumber();
			astFactory.addASTChild(currentAST, returnAST);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case STRING_LITERAL:
		{
			AST tmp335_AST = null;
			tmp335_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp335_AST);
			match(STRING_LITERAL);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_true:
		{
			AST tmp336_AST = null;
			tmp336_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp336_AST);
			match(LITERAL_true);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_false:
		{
			AST tmp337_AST = null;
			tmp337_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp337_AST);
			match(LITERAL_false);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_null:
		{
			AST tmp338_AST = null;
			tmp338_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp338_AST);
			match(LITERAL_null);
			constant_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = constant_AST;
	}
	
/** object instantiation.
 *  Trees are built as illustrated by the following input/tree pairs:
 *
 *  new T()
 *
 *  new
 *   |
 *   T --  ELIST
 *                 |
 *                arg1 -- arg2 -- .. -- argn
 *
 *  new int[]
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *
 *  new int[] {1,2}
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR -- ARRAY_INIT
 *                                                                |
 *                                                              EXPR -- EXPR
 *                                                                |   |
 *                                                                1       2
 *
 *  new int[3]
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *                              |
 *                        EXPR
 *                              |
 *                              3
 *
 *  new int[1][2]
 *
 *  new
 *   |
 *  int -- ARRAY_DECLARATOR
 *                         |
 *               ARRAY_DECLARATOR -- EXPR
 *                         |                  |
 *                       EXPR                    1
 *                         |
 *                         2
 *
 */
	public final void newExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST newExpression_AST = null;
		AST ta_AST = null;
		AST t_AST = null;
		AST mca_AST = null;
		AST cb_AST = null;
		AST ad_AST = null;
		Token first = LT(1); int jumpBack = mark();
		
		try {      // for error handling
			match(LITERAL_new);
			nls();
			{
			switch ( LA(1)) {
			case LT:
			{
				typeArguments();
				ta_AST = (AST)returnAST;
				break;
			}
			case LBRACK:
			case IDENT:
			case LPAREN:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case IDENT:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			{
				type();
				t_AST = (AST)returnAST;
				break;
			}
			case LBRACK:
			case LPAREN:
			case NLS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LPAREN:
			case NLS:
			{
				nls();
				methodCallArgs(null);
				mca_AST = (AST)returnAST;
				{
				if ((LA(1)==LCURLY) && (_tokenSet_52.member(LA(2)))) {
					classBlock();
					cb_AST = (AST)returnAST;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((_tokenSet_101.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					newExpression_AST = (AST)currentAST.root;
					mca_AST = mca_AST.getFirstChild();
					newExpression_AST = (AST)astFactory.make( (new ASTArray(5)).add(create(LITERAL_new,"new",first,LT(1))).add(ta_AST).add(t_AST).add(mca_AST).add(cb_AST));
					currentAST.root = newExpression_AST;
					currentAST.child = newExpression_AST!=null &&newExpression_AST.getFirstChild()!=null ?
						newExpression_AST.getFirstChild() : newExpression_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			case LBRACK:
			{
				newArrayDeclarator();
				ad_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					newExpression_AST = (AST)currentAST.root;
					newExpression_AST = (AST)astFactory.make( (new ASTArray(4)).add(create(LITERAL_new,"new",first,LT(1))).add(ta_AST).add(t_AST).add(ad_AST));
					currentAST.root = newExpression_AST;
					currentAST.child = newExpression_AST!=null &&newExpression_AST.getFirstChild()!=null ?
						newExpression_AST.getFirstChild() : newExpression_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			newExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException e) {
			if (inputState.guessing==0) {
				
				if (t_AST==null) {
							    reportError("missing type for constructor call",first);
								newExpression_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_new,"new",first,LT(1))).add(ta_AST).add(null)); 
				// currentAST.root = newExpression_AST;
								// currentAST.child = newExpression_AST!=null &&newExpression_AST.getFirstChild()!=null ?
								// newExpression_AST.getFirstChild() : newExpression_AST;
								// currentAST.advanceChildToEnd();
								// probably others to include - or make this the default?
								if (e instanceof MismatchedTokenException || e instanceof NoViableAltException) {
									// int i = ((MismatchedTokenException)e).token.getType();
									rewind(jumpBack);
									consumeUntil(NLS);
								}      
				} else if (mca_AST==null && ad_AST==null) {
				reportError("expecting '(' or '[' after type name to continue new expression",t_AST);
				newExpression_AST = (AST)astFactory.make( (new ASTArray(3)).add(create(LITERAL_new,"new",first,LT(1))).add(ta_AST).add(t_AST));               
								//currentAST.root = newExpression_AST;
								//currentAST.child = newExpression_AST!=null &&newExpression_AST.getFirstChild()!=null ?
								//newExpression_AST.getFirstChild() : newExpression_AST;
								//currentAST.advanceChildToEnd();
								if (e instanceof MismatchedTokenException) {
									Token t =  ((MismatchedTokenException)e).token;
									int i = ((MismatchedTokenException)e).token.getType();
									rewind(jumpBack);
									consume();
									consumeUntil(NLS);
								}   
				} else {
				throw e;
				}
				
			} else {
				throw e;
			}
		}
		returnAST = newExpression_AST;
	}
	
	public final void closableBlockConstructorExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST closableBlockConstructorExpression_AST = null;
		
		closableBlock();
		astFactory.addASTChild(currentAST, returnAST);
		closableBlockConstructorExpression_AST = (AST)currentAST.root;
		returnAST = closableBlockConstructorExpression_AST;
	}
	
/**
 * A list constructor is a argument list enclosed in square brackets, without labels.
 * Any argument can be decorated with a spread operator (*x), but not a label (a:x).
 * Examples:  [], [1], [1,2], [1,*l1,2], [*l1,*l2].
 * (The l1, l2 must be a sequence or null.)
 * <p>
 * A map constructor is an argument list enclosed in square brackets, with labels everywhere,
 * except on spread arguments, which stand for whole maps spliced in.
 * A colon alone between the brackets also forces the expression to be an empty map constructor.
 * Examples: [:], [a:1], [a:1,b:2], [a:1,*:m1,b:2], [*:m1,*:m2]
 * (The m1, m2 must be a map or null.)
 * Values associated with identical keys overwrite from left to right:
 * [a:1,a:2]  ===  [a:2]
 * <p>
 * Some malformed constructor expressions are not detected in the parser, but in a post-pass.
 * Bad examples: [1,b:2], [a:1,2], [:1].
 * (Note that method call arguments, by contrast, can be a mix of keyworded and non-keyworded arguments.)
 */
	public final void listOrMapConstructorExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST listOrMapConstructorExpression_AST = null;
		Token  lcon = null;
		AST lcon_AST = null;
		AST args_AST = null;
		Token  emcon = null;
		AST emcon_AST = null;
		boolean hasLabels = false;
		
		if ((LA(1)==LBRACK) && (_tokenSet_107.member(LA(2)))) {
			lcon = LT(1);
			lcon_AST = astFactory.create(lcon);
			match(LBRACK);
			argList();
			args_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				hasLabels |= argListHasLabels;
			}
			match(RBRACK);
			if ( inputState.guessing==0 ) {
				listOrMapConstructorExpression_AST = (AST)currentAST.root;
				int type = hasLabels ? MAP_CONSTRUCTOR : LIST_CONSTRUCTOR;
				listOrMapConstructorExpression_AST = (AST)astFactory.make( (new ASTArray(2)).add(create(type,"[",lcon_AST,LT(1))).add(args_AST));
				
				currentAST.root = listOrMapConstructorExpression_AST;
				currentAST.child = listOrMapConstructorExpression_AST!=null &&listOrMapConstructorExpression_AST.getFirstChild()!=null ?
					listOrMapConstructorExpression_AST.getFirstChild() : listOrMapConstructorExpression_AST;
				currentAST.advanceChildToEnd();
			}
			listOrMapConstructorExpression_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==LBRACK) && (LA(2)==COLON)) {
			emcon = LT(1);
			emcon_AST = astFactory.create(emcon);
			astFactory.makeASTRoot(currentAST, emcon_AST);
			match(LBRACK);
			match(COLON);
			match(RBRACK);
			if ( inputState.guessing==0 ) {
				emcon_AST.setType(MAP_CONSTRUCTOR);
			}
			listOrMapConstructorExpression_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = listOrMapConstructorExpression_AST;
	}
	
	public final void stringConstructorValuePart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST stringConstructorValuePart_AST = null;
		
		{
		switch ( LA(1)) {
		case IDENT:
		{
			identifier();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case LITERAL_this:
		{
			AST tmp343_AST = null;
			tmp343_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp343_AST);
			match(LITERAL_this);
			break;
		}
		case LITERAL_super:
		{
			AST tmp344_AST = null;
			tmp344_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp344_AST);
			match(LITERAL_super);
			break;
		}
		case LCURLY:
		{
			openOrClosableBlock();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		stringConstructorValuePart_AST = (AST)currentAST.root;
		returnAST = stringConstructorValuePart_AST;
	}
	
	public final void newArrayDeclarator() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST newArrayDeclarator_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		
		{
		int _cnt574=0;
		_loop574:
		do {
			if ((LA(1)==LBRACK) && (_tokenSet_108.member(LA(2)))) {
				lb = LT(1);
				lb_AST = astFactory.create(lb);
				astFactory.makeASTRoot(currentAST, lb_AST);
				match(LBRACK);
				if ( inputState.guessing==0 ) {
					lb_AST.setType(ARRAY_DECLARATOR);
				}
				{
				switch ( LA(1)) {
				case LBRACK:
				case IDENT:
				case STRING_LITERAL:
				case LPAREN:
				case LITERAL_super:
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				case LCURLY:
				case LITERAL_this:
				case PLUS:
				case MINUS:
				case LITERAL_false:
				case LITERAL_new:
				case LITERAL_null:
				case LITERAL_true:
				case INC:
				case DEC:
				case BNOT:
				case LNOT:
				case STRING_CTOR_START:
				case NUM_INT:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				case NUM_BIG_INT:
				case NUM_BIG_DECIMAL:
				{
					expression(0);
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case RBRACK:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RBRACK);
			}
			else {
				if ( _cnt574>=1 ) { break _loop574; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt574++;
		} while (true);
		}
		newArrayDeclarator_AST = (AST)currentAST.root;
		returnAST = newArrayDeclarator_AST;
	}
	
/** A single argument in (...) or [...].  Corresponds to to a method or closure parameter.
 *  May be labeled.  May be modified by the spread operator '*' ('*:' for keywords).
 */
	public final byte  argument() throws RecognitionException, TokenStreamException {
		byte hasLabelOrSpread = 0;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argument_AST = null;
		Token  c = null;
		AST c_AST = null;
		Token  sp = null;
		AST sp_AST = null;
		boolean sce=false;
		
		{
		boolean synPredMatched560 = false;
		if (((_tokenSet_97.member(LA(1))) && (_tokenSet_98.member(LA(2))))) {
			int _m560 = mark();
			synPredMatched560 = true;
			inputState.guessing++;
			try {
				{
				argumentLabelStart();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched560 = false;
			}
			rewind(_m560);
inputState.guessing--;
		}
		if ( synPredMatched560 ) {
			argumentLabel();
			astFactory.addASTChild(currentAST, returnAST);
			c = LT(1);
			c_AST = astFactory.create(c);
			astFactory.makeASTRoot(currentAST, c_AST);
			match(COLON);
			if ( inputState.guessing==0 ) {
				c_AST.setType(LABELED_ARG);
			}
			if ( inputState.guessing==0 ) {
				hasLabelOrSpread |= 1;
			}
		}
		else if ((LA(1)==STAR)) {
			sp = LT(1);
			sp_AST = astFactory.create(sp);
			astFactory.makeASTRoot(currentAST, sp_AST);
			match(STAR);
			if ( inputState.guessing==0 ) {
				sp_AST.setType(SPREAD_ARG);
			}
			if ( inputState.guessing==0 ) {
				hasLabelOrSpread |= 2;
			}
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				if ( inputState.guessing==0 ) {
					sp_AST.setType(SPREAD_MAP_ARG);
				}
				if ( inputState.guessing==0 ) {
					hasLabelOrSpread |= 1;
				}
				break;
			}
			case FINAL:
			case ABSTRACT:
			case STRICTFP:
			case LITERAL_static:
			case LITERAL_def:
			case LBRACK:
			case IDENT:
			case STRING_LITERAL:
			case LPAREN:
			case AT:
			case LITERAL_super:
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case LITERAL_private:
			case LITERAL_public:
			case LITERAL_protected:
			case LITERAL_transient:
			case LITERAL_native:
			case LITERAL_threadsafe:
			case LITERAL_synchronized:
			case LITERAL_volatile:
			case LCURLY:
			case LITERAL_this:
			case LITERAL_return:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_throw:
			case LITERAL_assert:
			case PLUS:
			case MINUS:
			case LITERAL_false:
			case LITERAL_new:
			case LITERAL_null:
			case LITERAL_true:
			case INC:
			case DEC:
			case BNOT:
			case LNOT:
			case STRING_CTOR_START:
			case NUM_INT:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			case NUM_BIG_INT:
			case NUM_BIG_DECIMAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		else if ((_tokenSet_86.member(LA(1))) && (_tokenSet_109.member(LA(2)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		sce=strictContextExpression(true);
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			
			require(LA(1) != COLON,
			"illegal colon after argument expression",
			"a complex label expression before a colon must be parenthesized");
			
		}
		argument_AST = (AST)currentAST.root;
		returnAST = argument_AST;
		return hasLabelOrSpread;
	}
	
/** For lookahead only.  Fast approximate parse of an argumentLabel followed by a colon. */
	public final void argumentLabelStart() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argumentLabelStart_AST = null;
		
		{
		switch ( LA(1)) {
		case IDENT:
		{
			AST tmp347_AST = null;
			tmp347_AST = astFactory.create(LT(1));
			match(IDENT);
			break;
		}
		case FINAL:
		case ABSTRACT:
		case UNUSED_GOTO:
		case UNUSED_CONST:
		case UNUSED_DO:
		case STRICTFP:
		case LITERAL_package:
		case LITERAL_import:
		case LITERAL_static:
		case LITERAL_def:
		case LITERAL_class:
		case LITERAL_interface:
		case LITERAL_enum:
		case LITERAL_extends:
		case LITERAL_super:
		case LITERAL_void:
		case LITERAL_boolean:
		case LITERAL_byte:
		case LITERAL_char:
		case LITERAL_short:
		case LITERAL_int:
		case LITERAL_float:
		case LITERAL_long:
		case LITERAL_double:
		case LITERAL_as:
		case LITERAL_private:
		case LITERAL_public:
		case LITERAL_protected:
		case LITERAL_transient:
		case LITERAL_native:
		case LITERAL_threadsafe:
		case LITERAL_synchronized:
		case LITERAL_volatile:
		case LITERAL_default:
		case LITERAL_throws:
		case LITERAL_implements:
		case LITERAL_this:
		case LITERAL_if:
		case LITERAL_else:
		case LITERAL_while:
		case LITERAL_switch:
		case LITERAL_for:
		case LITERAL_in:
		case LITERAL_return:
		case LITERAL_break:
		case LITERAL_continue:
		case LITERAL_throw:
		case LITERAL_assert:
		case LITERAL_case:
		case LITERAL_try:
		case LITERAL_finally:
		case LITERAL_catch:
		case LITERAL_false:
		case LITERAL_instanceof:
		case LITERAL_new:
		case LITERAL_null:
		case LITERAL_true:
		{
			keywordPropertyNames();
			break;
		}
		case NUM_INT:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		case NUM_BIG_INT:
		case NUM_BIG_DECIMAL:
		{
			constantNumber();
			break;
		}
		case STRING_LITERAL:
		{
			AST tmp348_AST = null;
			tmp348_AST = astFactory.create(LT(1));
			match(STRING_LITERAL);
			break;
		}
		case LBRACK:
		case LPAREN:
		case LCURLY:
		case STRING_CTOR_START:
		{
			balancedBrackets();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		AST tmp349_AST = null;
		tmp349_AST = astFactory.create(LT(1));
		match(COLON);
		returnAST = argumentLabelStart_AST;
	}
	
/** Numeric constant. */
	public final void constantNumber() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constantNumber_AST = null;
		
		switch ( LA(1)) {
		case NUM_INT:
		{
			AST tmp350_AST = null;
			tmp350_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp350_AST);
			match(NUM_INT);
			constantNumber_AST = (AST)currentAST.root;
			break;
		}
		case NUM_FLOAT:
		{
			AST tmp351_AST = null;
			tmp351_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp351_AST);
			match(NUM_FLOAT);
			constantNumber_AST = (AST)currentAST.root;
			break;
		}
		case NUM_LONG:
		{
			AST tmp352_AST = null;
			tmp352_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp352_AST);
			match(NUM_LONG);
			constantNumber_AST = (AST)currentAST.root;
			break;
		}
		case NUM_DOUBLE:
		{
			AST tmp353_AST = null;
			tmp353_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp353_AST);
			match(NUM_DOUBLE);
			constantNumber_AST = (AST)currentAST.root;
			break;
		}
		case NUM_BIG_INT:
		{
			AST tmp354_AST = null;
			tmp354_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp354_AST);
			match(NUM_BIG_INT);
			constantNumber_AST = (AST)currentAST.root;
			break;
		}
		case NUM_BIG_DECIMAL:
		{
			AST tmp355_AST = null;
			tmp355_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp355_AST);
			match(NUM_BIG_DECIMAL);
			constantNumber_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = constantNumber_AST;
	}
	
/** Fast lookahead across balanced brackets of all sorts. */
	public final void balancedBrackets() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST balancedBrackets_AST = null;
		
		switch ( LA(1)) {
		case LPAREN:
		{
			AST tmp356_AST = null;
			tmp356_AST = astFactory.create(LT(1));
			match(LPAREN);
			balancedTokens();
			AST tmp357_AST = null;
			tmp357_AST = astFactory.create(LT(1));
			match(RPAREN);
			break;
		}
		case LBRACK:
		{
			AST tmp358_AST = null;
			tmp358_AST = astFactory.create(LT(1));
			match(LBRACK);
			balancedTokens();
			AST tmp359_AST = null;
			tmp359_AST = astFactory.create(LT(1));
			match(RBRACK);
			break;
		}
		case LCURLY:
		{
			AST tmp360_AST = null;
			tmp360_AST = astFactory.create(LT(1));
			match(LCURLY);
			balancedTokens();
			AST tmp361_AST = null;
			tmp361_AST = astFactory.create(LT(1));
			match(RCURLY);
			break;
		}
		case STRING_CTOR_START:
		{
			AST tmp362_AST = null;
			tmp362_AST = astFactory.create(LT(1));
			match(STRING_CTOR_START);
			balancedTokens();
			AST tmp363_AST = null;
			tmp363_AST = astFactory.create(LT(1));
			match(STRING_CTOR_END);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = balancedBrackets_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"BLOCK",
		"MODIFIERS",
		"OBJBLOCK",
		"SLIST",
		"METHOD_DEF",
		"VARIABLE_DEF",
		"INSTANCE_INIT",
		"STATIC_INIT",
		"TYPE",
		"CLASS_DEF",
		"INTERFACE_DEF",
		"PACKAGE_DEF",
		"ARRAY_DECLARATOR",
		"EXTENDS_CLAUSE",
		"IMPLEMENTS_CLAUSE",
		"PARAMETERS",
		"PARAMETER_DEF",
		"LABELED_STAT",
		"TYPECAST",
		"INDEX_OP",
		"POST_INC",
		"POST_DEC",
		"METHOD_CALL",
		"EXPR",
		"IMPORT",
		"UNARY_MINUS",
		"UNARY_PLUS",
		"CASE_GROUP",
		"ELIST",
		"FOR_INIT",
		"FOR_CONDITION",
		"FOR_ITERATOR",
		"EMPTY_STAT",
		"\"final\"",
		"\"abstract\"",
		"\"goto\"",
		"\"const\"",
		"\"do\"",
		"\"strictfp\"",
		"SUPER_CTOR_CALL",
		"CTOR_CALL",
		"CTOR_IDENT",
		"VARIABLE_PARAMETER_DEF",
		"STRING_CONSTRUCTOR",
		"STRING_CTOR_MIDDLE",
		"CLOSABLE_BLOCK",
		"IMPLICIT_PARAMETERS",
		"SELECT_SLOT",
		"DYNAMIC_MEMBER",
		"LABELED_ARG",
		"SPREAD_ARG",
		"SPREAD_MAP_ARG",
		"LIST_CONSTRUCTOR",
		"MAP_CONSTRUCTOR",
		"FOR_IN_ITERABLE",
		"STATIC_IMPORT",
		"ENUM_DEF",
		"ENUM_CONSTANT_DEF",
		"FOR_EACH_CLAUSE",
		"ANNOTATION_DEF",
		"ANNOTATIONS",
		"ANNOTATION",
		"ANNOTATION_MEMBER_VALUE_PAIR",
		"ANNOTATION_FIELD_DEF",
		"ANNOTATION_ARRAY_INIT",
		"TYPE_ARGUMENTS",
		"TYPE_ARGUMENT",
		"TYPE_PARAMETERS",
		"TYPE_PARAMETER",
		"WILDCARD_TYPE",
		"TYPE_UPPER_BOUNDS",
		"TYPE_LOWER_BOUNDS",
		"CLOSURE_LIST",
		"MULTICATCH",
		"MULTICATCH_TYPES",
		"a script header",
		"\"package\"",
		"\"import\"",
		"\"static\"",
		"\"def\"",
		"'['",
		"']'",
		"an identifier",
		"a string literal",
		"'<'",
		"'.'",
		"'('",
		"\"class\"",
		"\"interface\"",
		"\"enum\"",
		"'@'",
		"'?'",
		"\"extends\"",
		"\"super\"",
		"'>'",
		"','",
		"'>>'",
		"'>>>'",
		"\"void\"",
		"\"boolean\"",
		"\"byte\"",
		"\"char\"",
		"\"short\"",
		"\"int\"",
		"\"float\"",
		"\"long\"",
		"\"double\"",
		"'*'",
		"\"as\"",
		"\"private\"",
		"\"public\"",
		"\"protected\"",
		"\"transient\"",
		"\"native\"",
		"\"threadsafe\"",
		"\"synchronized\"",
		"\"volatile\"",
		"')'",
		"'='",
		"'&'",
		"'{'",
		"'}'",
		"';'",
		"\"default\"",
		"\"throws\"",
		"\"implements\"",
		"\"this\"",
		"'...'",
		"'|'",
		"'->'",
		"':'",
		"\"if\"",
		"\"else\"",
		"\"while\"",
		"\"switch\"",
		"\"for\"",
		"\"in\"",
		"\"return\"",
		"\"break\"",
		"\"continue\"",
		"\"throw\"",
		"\"assert\"",
		"'+'",
		"'-'",
		"\"case\"",
		"\"try\"",
		"\"finally\"",
		"\"catch\"",
		"'*.'",
		"'?.'",
		"'.&'",
		"\"false\"",
		"\"instanceof\"",
		"\"new\"",
		"\"null\"",
		"\"true\"",
		"'+='",
		"'-='",
		"'*='",
		"'/='",
		"'%='",
		"'>>='",
		"'>>>='",
		"'<<='",
		"'&='",
		"'^='",
		"'|='",
		"'**='",
		"'?:'",
		"'||'",
		"'&&'",
		"'^'",
		"'=~'",
		"'==~'",
		"'!='",
		"'=='",
		"'==='",
		"'!=='",
		"'<=>'",
		"'<='",
		"'>='",
		"'<<'",
		"'..'",
		"'..<'",
		"'++'",
		"'/'",
		"'%'",
		"'--'",
		"'**'",
		"'~'",
		"'!'",
		"STRING_CTOR_START",
		"a string literal end",
		"a numeric literal",
		"NUM_FLOAT",
		"NUM_LONG",
		"NUM_DOUBLE",
		"NUM_BIG_INT",
		"NUM_BIG_DECIMAL",
		"some newlines, whitespace or comments",
		"'$'",
		"whitespace",
		"a newline",
		"a newline",
		"a single line comment",
		"a comment",
		"a string character",
		"a multiline regular expression literal",
		"a multiline dollar escaping regular expression literal",
		"a multiline regular expression literal end",
		"a multiline dollar escaping regular expression literal end",
		"ESCAPED_SLASH",
		"ESCAPED_DOLLAR",
		"a multiline regular expression character",
		"a multiline dollar escaping regular expression character",
		"an escape sequence",
		"a newline inside a string",
		"a hexadecimal digit",
		"a character",
		"a letter",
		"a digit",
		"a sequence of digits and underscores, bordered by digits",
		"a sequence of digits and underscores with maybe underscore starting",
		"an exponent",
		"a float or double suffix",
		"a big decimal suffix"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=4611686018700017664L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[8];
		data[0]=4810363371522L;
		data[1]=5908300234451976192L;
		data[2]=-8070450528352473468L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-144115222435659776L;
		data[2]=-1L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-65536L;
		data[2]=-8034421735228964865L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[16];
		data[0]=-14L;
		for (int i = 1; i<=2; i++) { data[i]=-1L; }
		data[3]=274877906943L;
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 0L, 1078132736L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[8];
		data[0]=4810363371522L;
		data[1]=-1009228768099958784L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 0L, 4390912L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = new long[8];
		data[0]=4810363371522L;
		data[1]=-1009228768368394240L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = new long[8];
		data[1]=4611686018427387904L;
		data[2]=256L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=5908300234451976192L;
		data[2]=-8070450528352473212L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=-2305843009213693952L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = new long[8];
		data[0]=290133630779394L;
		data[1]=-65536L;
		data[2]=-9L;
		data[3]=4095L;
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 4810363371520L, 143552239196962816L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143552239486369792L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143692702147149824L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 4810363371520L, 143692701811605504L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143692702139809792L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=8214143243665670144L;
		data[2]=-8070450528352473428L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=1296614216024588288L;
		data[2]=-8070450528352473468L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = new long[8];
		data[1]=1153061975887970304L;
		data[2]=-8070450528354828284L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=-2305843008940802048L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 4810363371520L, 143552240136486912L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143552240140681216L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = new long[8];
		data[0]=4810363371522L;
		data[1]=-1009228793172328448L;
		data[2]=-8070450528351424636L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 0L, 140462614642688L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 0L, 63963136L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=-2017612598635134976L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=-2017612598702243840L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = new long[8];
		data[1]=4194304L;
		data[2]=1L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=-1152921504606846976L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=8214143243665670144L;
		data[2]=-8070450528352473468L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 0L, 55574528L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { 2L, 5044031617016791040L, 4160L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { 4810363371520L, 143692701871374336L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { 4810363371520L, 143692701837819904L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-7782361153482588160L;
		data[2]=-8070450528070402169L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = new long[8];
		data[0]=4810363371522L;
		data[1]=7205336952922374144L;
		data[2]=8L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-65536L;
		data[2]=-1L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = new long[8];
		data[1]=140464762126336L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { 0L, 223338299392L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-140737505198080L;
		data[2]=-8034421735346405377L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = new long[8];
		data[1]=1152921504675004416L;
		data[3]=8L;
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = new long[16];
		data[0]=-16L;
		data[1]=-3602879701966651393L;
		data[2]=-1L;
		data[3]=274877906919L;
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143692702767906816L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { 0L, 140464644685824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=399507456L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { 4810363371520L, 143552238122696704L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = new long[8];
		data[1]=1153061976961712128L;
		data[2]=-8070450528354828284L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-288230410511515648L;
		data[2]=-17587891077121L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 8658654068736L, -9079397847315578880L, 4176740231L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = new long[8];
		data[1]=8070450566947405824L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=8214143234999058432L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = new long[8];
		data[0]=4810363371522L;
		data[1]=-1009228793138774016L;
		data[2]=-8070450528351424636L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=-1152780779990810624L;
		data[2]=1048834L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { 4810363371520L, 143552239201157120L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143552239536701440L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = new long[8];
		data[1]=7205759438219640832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = new long[8];
		data[1]=6917529062000820224L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = new long[8];
		data[0]=4810363371522L;
		data[1]=-1009228767495979008L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = new long[8];
		data[1]=6917529063078756352L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = new long[8];
		data[1]=1077936128L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = new long[8];
		data[1]=8070450568021147648L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143692702105206784L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { 4810363371520L, 143692701828382720L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = new long[8];
		data[1]=8070450532247928832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=8214143278042185728L;
		data[2]=-8070450528352473468L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=8214143243682447360L;
		data[2]=-8070450528352473468L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = { 0L, 8606711808L, 4L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = new long[8];
		data[1]=140464829235200L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-144115222435659776L;
		data[2]=-1L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=5908300234451976192L;
		data[2]=-8070450528352473468L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	private static final long[] mk_tokenSet_72() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-864832092553412608L;
		data[2]=-8070450528070402073L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	private static final long[] mk_tokenSet_73() {
		long[] data = new long[8];
		data[0]=290133630779394L;
		data[1]=-65536L;
		data[2]=-1L;
		data[3]=4095L;
		return data;
	}
	public static final BitSet _tokenSet_73 = new BitSet(mk_tokenSet_73());
	private static final long[] mk_tokenSet_74() {
		long[] data = { 0L, 55574528L, 8L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_74 = new BitSet(mk_tokenSet_74());
	private static final long[] mk_tokenSet_75() {
		long[] data = new long[8];
		data[1]=432345598591500288L;
		data[2]=32L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_75 = new BitSet(mk_tokenSet_75());
	private static final long[] mk_tokenSet_76() {
		long[] data = { 0L, 54525952L, 16L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_76 = new BitSet(mk_tokenSet_76());
	private static final long[] mk_tokenSet_77() {
		long[] data = new long[8];
		data[0]=137438953472L;
		data[1]=140463688908800L;
		data[2]=40L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_77 = new BitSet(mk_tokenSet_77());
	private static final long[] mk_tokenSet_78() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=8502373654227451904L;
		data[2]=-8070450528352473428L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_78 = new BitSet(mk_tokenSet_78());
	private static final long[] mk_tokenSet_79() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=5908300233512321024L;
		data[2]=-8070450528354574332L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_79 = new BitSet(mk_tokenSet_79());
	private static final long[] mk_tokenSet_80() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-34359803904L;
		data[2]=-1L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_80 = new BitSet(mk_tokenSet_80());
	private static final long[] mk_tokenSet_81() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143692702131421184L;
		data[2]=4160L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_81 = new BitSet(mk_tokenSet_81());
	private static final long[] mk_tokenSet_82() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=143692702131421184L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_82 = new BitSet(mk_tokenSet_82());
	private static final long[] mk_tokenSet_83() {
		long[] data = new long[8];
		data[1]=1152921504606846976L;
		data[2]=4194304L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_83 = new BitSet(mk_tokenSet_83());
	private static final long[] mk_tokenSet_84() {
		long[] data = new long[8];
		data[1]=1153061975887970304L;
		data[2]=-8070450528354828284L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_84 = new BitSet(mk_tokenSet_84());
	private static final long[] mk_tokenSet_85() {
		long[] data = new long[8];
		data[0]=2L;
		data[1]=-2161727786776002560L;
		data[2]=1048832L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_85 = new BitSet(mk_tokenSet_85());
	private static final long[] mk_tokenSet_86() {
		long[] data = new long[8];
		data[0]=4810363371520L;
		data[1]=1296614215084933120L;
		data[2]=-8070450528354574332L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_86 = new BitSet(mk_tokenSet_86());
	private static final long[] mk_tokenSet_87() {
		long[] data = { 0L, 206158430208L, 1008806316530991104L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_87 = new BitSet(mk_tokenSet_87());
	private static final long[] mk_tokenSet_88() {
		long[] data = new long[8];
		data[0]=-16L;
		data[1]=-1152921504606846977L;
		data[2]=-1L;
		data[3]=274877906943L;
		return data;
	}
	public static final BitSet _tokenSet_88 = new BitSet(mk_tokenSet_88());
	private static final long[] mk_tokenSet_89() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-7926476342632185856L;
		data[2]=-8070450528070402169L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_89 = new BitSet(mk_tokenSet_89());
	private static final long[] mk_tokenSet_90() {
		long[] data = new long[8];
		data[1]=1153061975887970304L;
		data[2]=3892314116L;
		data[3]=2024L;
		return data;
	}
	public static final BitSet _tokenSet_90 = new BitSet(mk_tokenSet_90());
	private static final long[] mk_tokenSet_91() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-864691353940983808L;
		data[2]=-8070450527952961553L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_91 = new BitSet(mk_tokenSet_91());
	private static final long[] mk_tokenSet_92() {
		long[] data = new long[8];
		data[1]=1152921504708558848L;
		data[2]=117440512L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_92 = new BitSet(mk_tokenSet_92());
	private static final long[] mk_tokenSet_93() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-864691388300722176L;
		data[2]=-8070450527952961617L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_93 = new BitSet(mk_tokenSet_93());
	private static final long[] mk_tokenSet_94() {
		long[] data = new long[8];
		data[0]=4810363371522L;
		data[1]=1296614215084933120L;
		data[2]=-8070450528354574332L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_94 = new BitSet(mk_tokenSet_94());
	private static final long[] mk_tokenSet_95() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-144115188075921408L;
		data[2]=-1L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_95 = new BitSet(mk_tokenSet_95());
	private static final long[] mk_tokenSet_96() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-7926476342632185856L;
		data[2]=-8070450528070402169L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_96 = new BitSet(mk_tokenSet_96());
	private static final long[] mk_tokenSet_97() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-7926476342632185856L;
		data[2]=4176740231L;
		data[3]=2024L;
		return data;
	}
	public static final BitSet _tokenSet_97 = new BitSet(mk_tokenSet_97());
	private static final long[] mk_tokenSet_98() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-1008806576410132480L;
		data[2]=-8070450528070402065L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_98 = new BitSet(mk_tokenSet_98());
	private static final long[] mk_tokenSet_99() {
		long[] data = { 8658654068736L, -9079397847319773184L, 4176740231L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_99 = new BitSet(mk_tokenSet_99());
	private static final long[] mk_tokenSet_100() {
		long[] data = { 0L, 33554432L, 50331648L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_100 = new BitSet(mk_tokenSet_100());
	private static final long[] mk_tokenSet_101() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-1073807360L;
		data[2]=-9L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_101 = new BitSet(mk_tokenSet_101());
	private static final long[] mk_tokenSet_102() {
		long[] data = { 0L, 17196646400L, 108086391056896000L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_102 = new BitSet(mk_tokenSet_102());
	private static final long[] mk_tokenSet_103() {
		long[] data = new long[8];
		data[1]=140462614642688L;
		data[3]=2048L;
		return data;
	}
	public static final BitSet _tokenSet_103 = new BitSet(mk_tokenSet_103());
	private static final long[] mk_tokenSet_104() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-140961950793728L;
		data[2]=-8034421735346405385L;
		data[3]=4078L;
		return data;
	}
	public static final BitSet _tokenSet_104 = new BitSet(mk_tokenSet_104());
	private static final long[] mk_tokenSet_105() {
		long[] data = { 0L, 140737488355328L, 6917529027641081856L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_105 = new BitSet(mk_tokenSet_105());
	private static final long[] mk_tokenSet_106() {
		long[] data = new long[8];
		data[0]=8658654068738L;
		data[1]=-1107361792L;
		data[2]=-117440521L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_106 = new BitSet(mk_tokenSet_106());
	private static final long[] mk_tokenSet_107() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-7926335604067991552L;
		data[2]=-8070450528070402169L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_107 = new BitSet(mk_tokenSet_107());
	private static final long[] mk_tokenSet_108() {
		long[] data = new long[8];
		data[1]=1153061975890067456L;
		data[2]=-8070450528354828284L;
		data[3]=2030L;
		return data;
	}
	public static final BitSet _tokenSet_108 = new BitSet(mk_tokenSet_108());
	private static final long[] mk_tokenSet_109() {
		long[] data = new long[8];
		data[0]=8658654068736L;
		data[1]=-65536L;
		data[2]=-1L;
		data[3]=4079L;
		return data;
	}
	public static final BitSet _tokenSet_109 = new BitSet(mk_tokenSet_109());
	
	}
