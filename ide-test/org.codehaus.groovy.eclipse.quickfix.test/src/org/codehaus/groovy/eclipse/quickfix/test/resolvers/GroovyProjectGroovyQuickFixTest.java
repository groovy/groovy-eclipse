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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.IProblemType;
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

/**
 * Tests Groovy quick fixes in a Groovy file contained in a Groovy Project
 * 
 * @author Nieraj Singh
 * 
 */
public class GroovyProjectGroovyQuickFixTest extends
		GroovyProjectQuickFixHarness {

	private static final String SUBTEST = "com.test.subtest";
	private static final String SUBSUBTEST = "com.test.subtest.subtest";

	protected void setUp() throws Exception {
		super.setUp();
		init();
	}

	/**
	 * Create a Groovy type with inner types in two different packages.
	 */
	protected void init() throws Exception {
		IPackageFragment subtestPackFrag = testProject.createPackage(SUBTEST);
		assertNotNull(subtestPackFrag);

		String topLeveLContent = "class TopLevelType { class InnerType { class InnerInnerType { } } }";

		createGroovyType(subtestPackFrag, "TopLevelType.groovy",
				topLeveLContent);

		subtestPackFrag = testProject.createPackage(SUBSUBTEST);
		assertNotNull(subtestPackFrag);

		createGroovyType(subtestPackFrag, "TopLevelType.groovy",
				topLeveLContent);
	}

	public void testAddImportField() throws Exception {

		String typeToImport = "TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
		String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
		String typeToAddImport = "BarField";
		String typeToAddImportContent = "class BarField { TopLevelType typeVar }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				fullQualifiedTypeToImport, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	public void testBasicAddImportInnerType() throws Exception {

		// When an InnerType is referenced with its declaring type, for example,
		// Map.Entry,
		// "Map" is imported. When the InnerType is referenced by it's simple
		// name, there may
		// be further suggestions as other top level types might have inner
		// types with the same name
		// therefore "Inner" is imported and the actual fully qualified top
		// level is shown within parenthesis

		// This tests the inner type reference by itself: InnerType
		String typeToImport = "InnerType";
		String innerFullyQualified = "com.test.subtest.TopLevelType.InnerType";

		String expectedQuickFixDisplay = "Import 'InnerType' (com.test.subtest.TopLevelType)";

		String typeToAddImport = "BarUsingInner";
		String typeToAddImportContent = "class BarUsingInner { InnerType innerTypeVar }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				innerFullyQualified, expectedQuickFixDisplay, typeToAddImport,
				typeToAddImportContent);

	}

	public void testBasicAddImportInnerType2() throws Exception {
		// When an InnerType is referenced with its declaring type, for example,
		// Map.Entry,
		// "Map" is imported. When the InnerType is referenced by it's simple
		// name, there may
		// be further suggestions as other top level types might have inner
		// types with the same name
		// therefore "Inner" is imported and the actual fully qualified top
		// level is shown within parenthesis

		// This tests the inner type when it also contains the top level type:
		// TopLevelType.InnerType
		String typeToImport = "TopLevelType";
		String typeToImportFullyQualified = "com.test.subtest.TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";

		String typeToAddImport = "BarUsingInnerB";
		String typeToAddImportContent = "class BarUsingInnerB { TopLevelType.InnerType innerTypeVar }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				typeToImportFullyQualified, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	public void testBasicAddImportInnerInnerType() throws Exception {

		String typeToImport = "InnerInnerType";
		String typeToImportFullyQualified = "com.test.subtest.TopLevelType.InnerType.InnerInnerType";

		String expectedQuickFixDisplay = "Import 'InnerInnerType' (com.test.subtest.TopLevelType.InnerType)";

		String typeToAddImport = "BarUsingInnerInner";
		String typeToAddImportContent = "class BarUsingInnerInner { InnerInnerType innerTypeVar }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				typeToImportFullyQualified, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	public void testAddImportReturnType() throws Exception {

		String typeToImport = "TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
		String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
		String typeToAddImport = "BarReturnType";

		String typeToAddImportContent = "class BarReturnType { public TopLevelType doSomething() { \n return null \n } }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				fullQualifiedTypeToImport, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	/**
	 * Tests if an add import resolver can be found if the unresolved type is in a local variable declaration
	 * @throws Exception
	 */
	public void testAddImportMethodParameter() throws Exception {

		String typeToImport = "TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
		String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
		String typeToAddImport = "BarMethodParameter";

		String typeToAddImportContent = "class BarMethodParameter { public void doSomething(TopLevelType ttI) {  } }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				fullQualifiedTypeToImport, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	/**
	 * Tests if an add import resolver can be found if the unresolved type is a generic
	 * @throws Exception
	 */
	public void testAddImportGeneric() throws Exception {

		String typeToImport = "TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
		String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
		String typeToAddImport = "BarGeneric";

		String typeToAddImportContent = "class BarGeneric { List<TopLevelType> aList }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				fullQualifiedTypeToImport, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	/**
	 * Tests if an add import resolver can be found if a class is extending an unresolved type
	 * @throws Exception
	 */
	public void testAddImportSubclassing() throws Exception {

		String typeToImport = "TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
		String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
		String typeToAddImport = "BarSubclassing";

		String typeToAddImportContent = "class BarSubclassing extends TopLevelType {  }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				fullQualifiedTypeToImport, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	/**
	 * Tests if an add import resolver can be found if the unresolved type is in a local variable declaration
	 * @throws Exception
	 */
	public void testAddImportLocalVariable() throws Exception {

		String typeToImport = "TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
		String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
		String typeToAddImport = "BarLocalVariable";

		String typeToAddImportContent = "class BarLocalVariable  { public void doSomething () { TopLevelType localVar  }  }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				fullQualifiedTypeToImport, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	/**
	 * Tests that a Groovy add import quick fix resolver can be obtained when
	 * the unresolved type is encountered in multiple places in the code.
	 * 
	 * @throws Exception
	 */
	public void testAddImportMultipleLocations() throws Exception {

		String typeToImport = "TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
		String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
		String typeToAddImport = "BarMultipleLocations";

		String typeToAddImportContent = "class BarMultipleLocations extends TopLevelType { public List<TopLevelType> doSomething () {\n TopLevelType localVar \n return null }  }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				fullQualifiedTypeToImport, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	/**
	 * Tests if a Groovy add import quick fix can be obtained when other
	 * unresolved types exist in the Groovy file
	 * 
	 * @throws Exception
	 */
	public void testAddImportMultipleUnresolved() throws Exception {

		String typeToImport = "TopLevelType";

		String expectedQuickFixDisplay = "Import 'TopLevelType' (com.test.subtest)";
		String fullQualifiedTypeToImport = "com.test.subtest.TopLevelType";
		String typeToAddImport = "BarMultipleUnresolved";

		String typeToAddImportContent = "class BarMultipleUnresolved extends TopLevelType { \n CSS css \n HTML val = new Entry() \n  }";

		testSelectImportGroovyTypeFromNewPackage(typeToImport,
				fullQualifiedTypeToImport, expectedQuickFixDisplay,
				typeToAddImport, typeToAddImportContent);

	}

	/**
	 * Tests if a Groovy add import resolver has multiple suggestions for the
	 * same unresolved simple name.
	 * 
	 * @throws Exception
	 */
	public void testAddImportMultipleProposalsForSameType() throws Exception {

		String typeToImport = "TopLevelType";

		String typeToAddImport = "BarLocalMultipleSameType";

		String typeToAddImportContent = "class BarLocalMultipleSameType  { public void doSomething () { TopLevelType localVar  }  }";

		Map<String, String> expectedQuickFixes = new HashMap<String, String>();

		expectedQuickFixes.put("Import 'TopLevelType' (com.test.subtest)",
				"com.test.subtest.TopLevelType");
		expectedQuickFixes.put(
				"Import 'TopLevelType' (com.test.subtest.subtest)",
				"com.test.subtest.subtest.TopLevelType");

		testMultipleProposalsSameTypeName(typeToImport, expectedQuickFixes,
				typeToAddImport, typeToAddImportContent);
	}

	/**
	 * Tests that no Groovy add import quick fix resolvers are obtained for an
	 * unresolved type that does not exist.
	 * 
	 * @throws Exception
	 */
	public void testAddImportNoProposals() throws Exception {

		String typeToAddImport = "BarAddImportNoProposal";
		String nonExistantType = "DoesNotExistTopLevelType";

		String typeToAddImportContent = "class BarAddImportNoProposal  { public void doSomething () { DoesNotExistTopLevelType localVar  }  }";

		ICompilationUnit unit = createGroovyTypeInTestPackage(typeToAddImport
				+ ".groovy", typeToAddImportContent);

		AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver(
				nonExistantType, unit);

		assertNull("Expected no resolver for nonexistant type: "
				+ nonExistantType, resolver);

	}

	/**
	 * Tests that no Groovy quick fix resolvers are encountered for unrecognised
	 * errors
	 * 
	 * @throws Exception
	 */
	public void testUnrecognisedErrorNoProposals() throws Exception {

		String typeToAddImport = "BarUnrecognisedError";

		String typeToAddImportContent = "class BarUnrecognisedError  { public void doSomething () { 222  }  }";
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
					"Encountered resolvers for unknown compilation error. None expected.",
					resolvers == null || resolvers.isEmpty());
		}

	}
}
