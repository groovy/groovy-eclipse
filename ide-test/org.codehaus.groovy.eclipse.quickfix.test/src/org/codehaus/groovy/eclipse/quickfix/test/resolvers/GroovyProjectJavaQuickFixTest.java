/*
 * Copyright 2010 the original author or authors.
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

package org.codehaus.groovy.eclipse.quickfix.test.resolvers;

import java.util.List;

import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.ConvertToGroovyFileResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.GroovyProblemFactory;
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests Groovy quick fixes in a Java file contained in a Groovy Project
 * 
 * @author Nieraj Singh
 * 
 */
public class GroovyProjectJavaQuickFixTest extends GroovyProjectQuickFixHarness {

	/**
	 * Tests that no Groovy add import resolvers are found for unresolved types
	 * in a Java file in Groovy Project, as those should be handle by JDT
	 * 
	 * @throws Exception
	 */
	public void testNoGroovyAddImportQuickFix() throws Exception {
		String typeToAddImport = "TestJavaC";
		String typeToImport = "ImageBuilder";

		String typeToAddImportContent = "class TestJavaC  { public void doSomething () { ImageBuilder imageBuilder = null; }  }";

		ICompilationUnit unit = createJavaTypeInTestPackage(typeToAddImport
				+ ".java", typeToAddImportContent);

		AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver(
				typeToImport, unit);

		assertNull(
				"Expected no Groovy add import quick fix resolver for unresolved type: "
						+ typeToImport + " in " + unit.getResource().getName()
						+ ", as it is a Java file", resolver);
	}

	/**
	 * Tests that a convert to Groovy quick fix is found when a missing ';' is
	 * encountered in a Java file in Groovy Project
	 * 
	 * @throws Exception
	 */
	public void testConverToGroovyQuickFixMissingSemiColon() throws Exception {
		String typeToAddImport = "TestJavaC";

		String typeToAddImportContent = "class TestJavaC  { public void doSomething () { ImageBuilder imageBuilder = null }  }";

		ICompilationUnit unit = createJavaTypeInTestPackage(typeToAddImport
				+ ".java", typeToAddImportContent);

		ConvertToGroovyFileResolver resolver = getConvertToGroovyQuickFixResolver(unit);

		assertNotNull(
				"Expected a quick fix resolver for converting to Groovy. None found.",
				resolver);

		String expectedDisplayString = "Convert to Groovy file and open in Groovy editor";

		List<ICompletionProposal> proposals = resolver.getQuickFixProposals();

		assertTrue(
				"Expected a convert to Groovy file quick fix proposal. None found.",
				proposals != null && proposals.size() > 0);

		// Test the first proposal

		ICompletionProposal firstProposal = proposals.get(0);

		assertEquals("Display string mismatch for convert to Groovy quick fix",
				expectedDisplayString, firstProposal.getDisplayString());

	}

	protected ConvertToGroovyFileResolver getConvertToGroovyQuickFixResolver(
			ICompilationUnit unit) throws Exception {
		IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit);

		List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(
				markers, GroovyProblemFactory.MISSING_SEMI_COLON_TYPE, unit);

		assertNotNull(resolvers);

		assertTrue(resolvers.size() > 0);

		for (IQuickFixResolver resolver : resolvers) {
			if (resolver instanceof ConvertToGroovyFileResolver) {
				return (ConvertToGroovyFileResolver) resolver;
			}
		}
		return null;
	}

}
