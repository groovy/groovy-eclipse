/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;
import junit.framework.Test;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IAttributeNamesConstants;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IModuleAttribute;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class ModuleAttributeTests extends AbstractRegressionTest9 {

	public ModuleAttributeTests(String name) {
		super(name);
	}

	public static Class<?> testClass() {
		return ModuleAttributeTests.class;
	}

	private static String[] allowedAttributes = {
			new String(IAttributeNamesConstants.MODULE),
			new String(IAttributeNamesConstants.MODULE_MAIN_CLASS),
			new String(IAttributeNamesConstants.MODULE_PACKAGES),
			new String(IAttributeNamesConstants.RUNTIME_VISIBLE_ANNOTATIONS),
			new String(IAttributeNamesConstants.RUNTIME_INVISIBLE_ANNOTATIONS),
			new String(IAttributeNamesConstants.SOURCE)
	};
	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug508889_003" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	private IModuleAttribute getModuleAttribute(String[] contents) {
		this.runConformTest(contents);
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		IClassFileAttribute attr = Arrays.stream(cfr.getAttributes())
			.filter(e -> new String(e.getAttributeName()).equals("Module"))
			.findFirst()
			.orElse(null);
		assertNotNull("Module attribute not found", attr);
		assertTrue("Not a module attribute", attr instanceof IModuleAttribute);
		return (IModuleAttribute) attr;
	}

	// basic test to check for presence of module attribute in module-info.class
	public void test001() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module test {\n" +
				"}\n",
				});
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		IClassFileAttribute moduleAttribute = null;
		IClassFileAttribute[] attrs = cfr.getAttributes();
		for (int i=0,max=attrs.length;i<max;i++) {
			if (new String(attrs[i].getAttributeName()).equals("Module")) {
				moduleAttribute = attrs[i];
			}
		}
		assertNotNull("Module attribute not found", moduleAttribute);
	}
	// Test that there is at most one Module attribute in the attributes table of a ClassFile structure- JVMS Sec 4.7.25
	public void testBug508889_002() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module first {\n" +
				"}\n",
				});
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		long count = Arrays.stream(cfr.getAttributes())
			.filter(e -> new String(e.getAttributeName()).equals("Module"))
			.count();
		assertEquals("Unexpected number of module attributes", 1,  count);
	}
	// Module Atrribute sanity
	public void _testBug508889_003() throws Exception {
		String[] contents = {
			"module-info.java",
			"module first {\n" +
				"exports pack1;\n" +
				"exports pack2 to zero;\n" +
			"}\n",
			"pack1/X11.java",
			"package pack1;\n" +
			"public class X11 {}\n",
			"pack2/X21.java",
			"package pack2;\n" +
			"public class X21 {}\n",
		};
		IModuleAttribute module = getModuleAttribute(contents);
		assertEquals("Wrong Module Name", "first", new String(module.getModuleName()));
		assertTrue("Unexpected attribute length", module.getAttributeLength() > 0);
		//int flags = module.getModuleFlags();
	}
	public void testBug521521() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module test {\n" +
				"}\n",
				});
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		int flags = cfr.getAccessFlags();
		assertTrue("Invalid access flags", (flags & ~ClassFileConstants.AccModule) == 0);
	}
	public void testBug521521a() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"open module test {\n" +
				"}\n",
				});
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		int flags = cfr.getAccessFlags();
		assertTrue("Invalid access flags", (flags & ~ClassFileConstants.AccModule) == 0);
	}

	public void testModuleCompile() throws Exception {
		String pack1_x11java = "pack1/X11.java";
		String pack2_x21java = "pack2/X21.java";
		associateToModule("first", pack1_x11java, pack2_x21java);
		String[] contents = {
			"module-info.java",
			"module first {\n" +
				"exports pack1;\n" +
				"exports pack2 to zero;\n" +
			"}\n",
			pack1_x11java,
			"package pack1;\n" +
			"public class X11 {}\n",
			pack2_x21java,
			"package pack2;\n" +
			"public class X21 {}\n",
		};
		this.runConformTest(contents);
	}
	public void testBug495967() throws Exception {
		String pack1_x11java = "pack1/pack2/pack3/pack4/X11.java";
		String pack2_x21java = "pack21/pack22/pack23/pack24/X21.java";
		String pack3_x31java = "pack31/pack32/pack33/pack34/X31.java";
		String pack4_x41java = "pack41/pack42/pack43/pack44/X41.java";
		String pack5_x51java = "pack51/pack52/pack53/pack54/X51.java";
		String pack6_x61java = "pack61/pack62/pack63/pack64/X61.java";
		String pack7_x71java = "pack71/pack72/pack73/pack74/X71.java";
		String pack8_x81java = "pack81/pack82/pack83/pack84/X81.java";
		String pack9_x91java = "pack91/pack92/pack93/pack94/X91.java";
		associateToModule("first", pack1_x11java, pack2_x21java, pack3_x31java, pack4_x41java, pack5_x51java, pack6_x61java, pack7_x71java, pack8_x81java, pack9_x91java);
		String[] contents = {
			"module-info.java",
			"module first {\n" +
				"exports pack1.pack2.pack3.pack4 to zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty;\n" +
				"exports pack21.pack22.pack23.pack24 to zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty;\n" +
				"exports pack31.pack32.pack33.pack34 to zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty;\n" +
				"exports pack41.pack42.pack43.pack44 to zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty;\n" +
				"exports pack51.pack52.pack53.pack54 to zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty;\n" +
				"exports pack61.pack62.pack63.pack64 to zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty;\n" +
				"exports pack71.pack72.pack73.pack74 to zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty;\n" +
				"exports pack81.pack82.pack83.pack84 to zero, one, two, three, four, five;\n" +
				"exports pack91.pack92.pack93.pack94 to zero, one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen, fourteen, fifteen, sixteen, seventeen, eighteen, nineteen, twenty;\n" +
			"}\n",
			pack1_x11java,
			"package pack1.pack2.pack3.pack4;\n" +
			"public class X11 {}\n",
			pack2_x21java,
			"package pack21.pack22.pack23.pack24;\n" +
			"public class X21 {}\n",
			pack3_x31java,
			"package pack31.pack32.pack33.pack34;\n" +
			"public class X31 {}\n",
			pack4_x41java,
			"package pack41.pack42.pack43.pack44;\n" +
			"public class X41 {}\n",
			pack5_x51java,
			"package pack51.pack52.pack53.pack54;\n" +
			"public class X51 {}\n",
			pack6_x61java,
			"package pack61.pack62.pack63.pack64;\n" +
			"public class X61 {}\n",
			pack7_x71java,
			"package pack71.pack72.pack73.pack74;\n" +
			"public class X71 {}\n",
			pack8_x81java,
			"package pack81.pack82.pack83.pack84;\n" +
			"public class X81 {}\n",
			pack9_x91java,
			"package pack91.pack92.pack93.pack94;\n" +
			"public class X91 {}\n",
		};
		this.runConformTest(contents);
	}
	public void testBug519330() throws Exception {
		String[] contents =  {
			"module-info.java",
			"module java.base {\n" +
			"}\n",
			};
		IModuleAttribute moduleAttribute = getModuleAttribute(contents);
		assertTrue("module java.base should not require any other modules", moduleAttribute.getRequiresCount() == 0);
	}
	public void testBug533134() throws Exception {
		String[] contents =  {
			"module-info.java",
			"@Deprecated\n" +
			"module test {\n" +
			"}\n",
			};
		this.runConformTest(contents);
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		IClassFileAttribute[] attrs = cfr.getAttributes();
		for (IClassFileAttribute attr : attrs) {
			String name = new String(attr.getAttributeName());
			assertTrue("Attribute " + name + " is not allowed", Stream.of(allowedAttributes).anyMatch(a -> a.equals(name)));
		}
	}
}
