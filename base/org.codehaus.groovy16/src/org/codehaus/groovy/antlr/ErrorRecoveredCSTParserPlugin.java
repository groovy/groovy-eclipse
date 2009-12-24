/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.antlr;

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.internal.antlr.parser.GroovyLexer;
import org.codehaus.groovy.internal.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.syntax.SyntaxException;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;
// FIXASC (groovychange) new type
/**
 * Parser plugin using grammar which has error recovery enabled/implemented in
 * select places.
 * 
 * @author empovazan
 */
public class ErrorRecoveredCSTParserPlugin extends AntlrParserPlugin {
	private final ICSTReporter reporter;

	ErrorRecoveredCSTParserPlugin(ICSTReporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void transformCSTIntoAST(final SourceUnit sourceUnit, Reader reader, SourceBuffer sourceBuffer) throws CompilationFailedException {
		super.ast = null;

		setController(sourceUnit);

		UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(reader, sourceBuffer);
		GroovyLexer lexer = new GroovyLexer(unicodeReader);
		unicodeReader.setLexer(lexer);
		GroovyRecognizer parser = GroovyRecognizer.make(lexer);
		parser.setSourceBuffer(sourceBuffer);
		super.tokenNames = parser.getTokenNames();
		parser.setFilename(sourceUnit.getName());

		// start parsing at the compilationUnit rule
		try {
			parser.compilationUnit();
			configureLocationSupport(sourceBuffer);
		} catch (TokenStreamRecognitionException tsre) {
            configureLocationSupport(sourceBuffer);
			RecognitionException e = tsre.recog;
			SyntaxException se = new SyntaxException(e.getMessage(), e, e
					.getLine(), e.getColumn());
			se.setFatal(true);
			sourceUnit.addError(se);
		} catch (RecognitionException e) {
            configureLocationSupport(sourceBuffer);
		    // sometimes the line/column is after the end of the file
		    // why is this?  Fix if possible
		    int origLine = e.getLine();
		    int origColumn = e.getColumn();
		    int[] newInts = fixLineColumn(origLine, origColumn);
		    int newLine = newInts[0];
		    int newColumn = newInts[1];
		    SyntaxException se = new SyntaxException(
		            e.getMessage(), e, newLine, newColumn);
//			se.setFatal(true);
			sourceUnit.addError(se);
		} catch (TokenStreamException e) {
            configureLocationSupport(sourceBuffer);
			sourceUnit.addException(e);
		}
		
		super.ast = parser.getAST();

	    sourceUnit.setComments(parser.getComments());
		reportCST(sourceUnit, parser);
	}

	@SuppressWarnings("unchecked")
	private void reportCST(final SourceUnit sourceUnit,
			final GroovyRecognizer parser) {
		final List errorList = parser.getErrorList();
		final GroovySourceAST cst = (GroovySourceAST) parser.getAST();

		if (reporter != null) {
			if (cst != null)
				reporter.generatedCST(sourceUnit.getName(), cst);
			if (errorList.size() != 0)
				// Unmodifiable necessary?
				reporter.reportErrors(sourceUnit.getName(), Collections.unmodifiableList(errorList));
		} else {
		    // report directly on the SourceUnit
		    for (Map<String, Object> error : (List<Map<String, Object>>) errorList) {
		        
		        // sometimes the line/column is after the end of the file
		        // why is this?  Fix if possible
	            int origLine = ((Integer) error.get("line")).intValue();
	            int origColumn = ((Integer) error.get("column")).intValue();
	            int[] newInts = fixLineColumn(origLine, origColumn);
	            int newLine = newInts[0];
	            int newColumn = newInts[1];
		        SyntaxException se = new SyntaxException((String) error.get("error"),
		                newLine, newColumn);
		        sourceUnit.addError(se);
            }
		}
	}

    /*
     * Fix the column so that it is not past the end of the file
     */
    private int[] fixLineColumn(int origLine, int origColumn) {
        if (locations.isPopulated()) {
            int offset = locations.findOffset(origLine, origColumn);
            if (offset >= locations.getEnd()-1) {
                return locations.getRowCol(locations.getEnd()-1);
            }
        }
        return new int[] { origLine, origColumn };
    }
}
