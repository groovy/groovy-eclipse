/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.CodeStyleConfiguration;

/**
 *
 * @author Andrew Eisenberg
 * @author Nieraj Singh
 * @created Oct 27, 2009
 */
public class GroovyImportRewriteFactory {

	private static final Pattern IMPORTS_PATTERN = Pattern
			.compile("(\\A|[\\n\\r])import\\s");

	private static final Pattern PACKAGE_PATTERN = Pattern
			.compile("(\\A|[\\n\\r])package\\s");

	private static final Pattern EOL_PATTERN = Pattern.compile("($|[\\n\\r])");

	private ImportRewrite rewrite;
	// set to true if there is a problem creating the rewrite
	private boolean cantCreateRewrite = false;

	/**
	 * This should never be null
	 */
	private GroovyCompilationUnit unit;

	/**
	 * This may be null
	 */
	private ModuleNode module;

	/**
	 * If this constructor is used, the a check may be performed on the module
	 * for unrecoverable errors before generating an import rewrite. Compilation
	 * unit should never be null, although the module can be null
	 */
	public GroovyImportRewriteFactory(GroovyCompilationUnit unit, ModuleNode module) {
		this.unit = unit;
		this.module = module;
	}

	/**
	 * Module is null in this case. Only a compilation unit is passed.
	 */
	public GroovyImportRewriteFactory(GroovyCompilationUnit unit) {
		this.unit = unit;
	}

	/**
	 * Returns an import rewrite for the module node only if
	 * ModuleNode.encounteredUnrecoverableError()
	 *
	 * Tries to find the start and end locations of the import statements. Makes
	 * a best guess using regular expression. This method ensures that even if
	 * the ComplationUnit is unparseable, the imports are still placed in the
	 * correct location.
	 *
	 * @return an {@link ImportRewrite} for the ModuleNode if it encountered an
	 *         unrecoverable error, or null if no problems.
	 */
	public ImportRewrite getImportRewrite(IProgressMonitor monitor) {

        // For the case of organize imports, if no unrecoverable error has been
        // found,
        // then we don't do any work here instead, a standard import rewrite is
        // used.
        // For the case of add import, this special rewrite will always be used
        // (and the module field will be null)
		if (module != null && !module.encounteredUnrecoverableError()) {
			return null;
		}

		if (rewrite == null && !cantCreateRewrite) {

			// find a reasonable substring that contains
			// what looks to be the import dependencies
			CharArraySequence contents = new CharArraySequence(
					unit.getContents());
			CharArraySequence imports = findImportsRegion(contents);

			// Now send this to a parser
			// need to be very careful here that if we can't parse, then
			// don't send to rewriter
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(unit.cloneCachingContents(CharOperation.concat(
					imports.chars(), "\nclass X { }".toCharArray())));
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			ASTNode result = null;
			try {
				result = parser.createAST(monitor);
			} catch (IllegalStateException e) {
				GroovyCore.logException("Can't create ImportRewrite for:\n"
						+ imports, e);
			}
			if (result instanceof CompilationUnit) {
				rewrite = CodeStyleConfiguration.createImportRewrite(
						(CompilationUnit) result, true);

			} else {
				// something wierd happened.
				// ensure we don't try again
				cantCreateRewrite = true;
			}
		}

		return rewrite;
	}

	/**
	 * Convenience methpd for
	 * {@link GroovyProposalTypeSearchRequestor#findImportsRegion(CharArraySequence)}
	 */
	public static CharArraySequence findImportsRegion(String contents) {
		return findImportsRegion(new CharArraySequence(contents));
	}

	/**
	 * Finds a region of text that kind of looks like where the imports should
	 * be placed. Uses regular expressions.
	 *
	 * @param contents
	 *            the contents of a compilation unit
	 * @return a presumed region
	 */
	public static CharArraySequence findImportsRegion(CharArraySequence contents) {
		// heuristics:
		// look for last index of ^import
		// if that returns -1, then look for ^package
		Matcher matcher = IMPORTS_PATTERN.matcher(contents);
		int importsEnd = 0;
		while (matcher.find(importsEnd)) {
			importsEnd = matcher.end();
		}

		if (importsEnd == 0) {
			// no imports found, look for package declaration
			matcher = PACKAGE_PATTERN.matcher(contents);
			if (matcher.find()) {
				importsEnd = matcher.end();
			}

		}

		if (importsEnd > 0) {
			// look for end of line
			matcher = EOL_PATTERN.matcher(contents);
			if (matcher.find(importsEnd)) {
				importsEnd = matcher.end();
			}
		}

		return contents.subSequence(0, importsEnd);
	}

}
