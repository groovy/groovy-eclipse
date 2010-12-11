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

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.IProblemType;
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Tests that no Groovy quick fixes are present in either a Java or Groovy file
 * in a non-Groovy Project
 * 
 * @author Nieraj Singh
 * 
 */
public class NonGroovyProjectQuickFixTest extends GroovyProjectQuickFixHarness {

	protected void setUp() throws Exception {
		super.setUp();

		GroovyRuntime.removeGroovyNature(testProject.getProject());
	}

	public void testIsNonGroovyProject() throws Exception {
		assertFalse(
				"This project has a Groovy nature. It only should be a Java project",
				GroovyNature.hasGroovyNature(testProject.getProject()));
	}

	/**
	 * Tests that no Groovy add import quick fix is found in a Java file in a
	 * non-Groovy Project
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
						+ ", as it is a Java project", resolver);
	}

	/**
	 * Tests that no Groovy quick fixes are found in a Groovy file in a
	 * non-Groovy Project
	 * 
	 * @throws Exception
	 */
	public void testNoGroovyQuickFixNonGroovyProject1() throws Exception {
		String typeToAddImport = "TestGroovyC";

		String typeToAddImportContent = "class TestGroovyC  { public void doSomething () { ImageBuilder imageBuilder = null }  }";

		ICompilationUnit unit = createGroovyTypeInTestPackage(typeToAddImport
				+ ".groovy", typeToAddImportContent);
		IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit);

		IProblemType[] knownProblemTypes = getGroovyProblemTypes();

		assertTrue("No Groovy problem types to test", knownProblemTypes != null
				&& knownProblemTypes.length > 0);

		for (IProblemType type : getGroovyProblemTypes()) {
			List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(
					markers, type, unit);
			assertTrue(
					"Encountered Groovy quick fix resolvers in a non-Groovy Project. None expected.",
					resolvers == null || resolvers.isEmpty());
		}

	}

	/**
	 * Tests that no Groovy add import quick fixes are found in a Groovy file in
	 * a non-Groovy Project
	 * 
	 * @throws Exception
	 */
	public void testNoGroovyQuickFixNonGroovyProject2() throws Exception {
		String typeToAddImport = "TestGroovyC";
		String unresolvedType = "ImageBuilder";
		String typeToAddImportContent = "class TestGroovyC  { public void doSomething () { ImageBuilder imageBuilder = null }  }";

		ICompilationUnit unit = createGroovyTypeInTestPackage(typeToAddImport
				+ ".groovy", typeToAddImportContent);

		AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver(
				unresolvedType, unit);

		assertNull(
				"Expected no Groovy add import resolvers as this is a Java project",
				resolver);

	}

}
