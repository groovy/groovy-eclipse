/*******************************************************************************
 * Copyright (c) 2019 Sebastian Zarnekow and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sebastian Zarnekow - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class Bug544921Test extends BuilderTests {
	public Bug544921Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Bug544921Test.class);
	}

	public void testCompilerRegression() throws JavaModelException, Exception {
		IPath projectPath = env.addProject("Bug544921Test", "1.8"); //$NON-NLS-1$

		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addClass(projectPath, "a", "Test", //$NON-NLS-1$ //$NON-NLS-2$
				"package a;\n" +
				"import java.util.EnumMap;\n" +
				"enum E {}\n" +
				"public class Test {\n" +
				"    Object x = new EnumMap<E, String>(E.class) {\n" +
				"        static final long serialVersionUID = 1;\n" +
				"        {\n" +
				"            E.values();\n" +
				"        }\n" +
				"    };\n" +
				"}" //$NON-NLS-1$
				);
		fullBuild();
		expectingNoProblems();
	}

	public void testBuildLargeFile_01() throws JavaModelException, Exception {
		IPath projectPath = env.addProject("Bug544921Test", "1.8"); //$NON-NLS-1$
		scaffoldProject(projectPath, 1, 10, 64);
		fullBuild();
		expectingNoProblems();
	}

	public void testBuildLargeFile_02() throws JavaModelException, Exception {
		IPath projectPath = env.addProject("Bug544921Test", "1.8"); //$NON-NLS-1$
		scaffoldProject(projectPath, 2, 500, 64);
		fullBuild();
		expectingNoProblems();
	}

	public void testBuildLargeFile_03() throws JavaModelException, Exception {
		IPath projectPath = env.addProject("Bug544921Test", "1.8"); //$NON-NLS-1$
		scaffoldProject(projectPath, 3, 500, 64);
		fullBuild();
		expectingNoProblems();
	}

	private boolean hasEnoughMemory(long required) {
		long bytes = Runtime.getRuntime().maxMemory();
		long megabytes = bytes / 1024 / 1024;
		return megabytes > required;
	}

	public void testBuildLargeFile_04() throws JavaModelException, Exception {
		if (hasEnoughMemory(2048)) {
			IPath projectPath = env.addProject("Bug544921Test", "1.8"); //$NON-NLS-1$
			scaffoldProject(projectPath, 4, 500, 64);
			fullBuild();
			expectingNoProblems();
		}
	}

	public void testBuildLargeFile_05() throws JavaModelException, Exception {
		if (hasEnoughMemory(2048)) {
			IPath projectPath = env.addProject("Bug544921Test", "1.8"); //$NON-NLS-1$
			scaffoldProject(projectPath, 5, 500, 64);
			fullBuild();
			expectingNoProblems();
		}
	}

	public void testBuildLargeFile_08() throws JavaModelException, Exception {
		if (hasEnoughMemory(2048)) {
			IPath projectPath = env.addProject("Bug544921Test", "1.8"); //$NON-NLS-1$
			scaffoldProject(projectPath, 8, 500, 64);
			fullBuild();
			expectingNoProblems();
		}
	}

	public void testBuildLargeFile_10() throws JavaModelException, Exception {
		if (hasEnoughMemory(2048)) {
			IPath projectPath = env.addProject("Bug544921Test", "1.8"); //$NON-NLS-1$
			scaffoldProject(projectPath, 10, 500, 64);
			fullBuild();
			expectingNoProblems();
		}
	}

	private void scaffoldProject(IPath projectPath, int maxPeripheral, int maxRegister, int maxFields) throws JavaModelException {
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.addClass(projectPath, "reg", "Field", //$NON-NLS-1$ //$NON-NLS-2$
			"package reg;\n" +
			"\n" +
			"import reg.Register.AccessType;\n" +
			"\n" +
			"public class Field {\n" +
			"    String name;\n" +
			"    String description;\n" +
			"    long bitRange;\n" +
			"    AccessType accessType;\n" +
			"\n" +
			"    public Field(String name, String description, long bitRange, AccessType accessType) {\n" +
			"        super();\n" +
			"        this.name = name;\n" +
			"        this.description = description;\n" +
			"        this.bitRange = bitRange;\n" +
			"        this.accessType = accessType;\n" +
			"    }\n" +
			"\n" +
			"	@Override\n" +
			"	public String toString() {\n" +
			"		return \"Field [name=\" + name + \", description=\" + description + \", bitRange=\" + bitRange + \", accessType=\"\n" +
			"				+ accessType + \"]\";\n" +
			"	}\n" +
			"   \n" +
			"}" //$NON-NLS-1$
		);

		env.addClass(projectPath, "reg", "Peripheral", //$NON-NLS-1$ //$NON-NLS-2$
			"package reg;\n" +
			"\n" +
			"import java.util.Collection;\n" +
			"import java.util.Map;\n" +
			"import java.util.TreeMap;\n" +
			"\n" +
			"public class Peripheral {\n" +
			"\n" +
			"    String name;\n" +
			"    String version;\n" +
			"    String description;\n" +
			"    String groupName;\n" +
			"    long baseAddress;\n" +
			"    long size;\n" +
			"\n" +
			"    Map<String, Register> registersMap;\n" +
			"\n" +
			"    public Peripheral(String name, String version, String description, String groupName,\n" +
			"            long baseAddress, long size) {\n" +
			"        super();\n" +
			"        this.name = name;\n" +
			"        this.version = version;\n" +
			"        this.description = description;\n" +
			"        this.groupName = groupName;\n" +
			"        this.baseAddress = baseAddress;\n" +
			"        this.size = size;\n" +
			"    }\n" +
			"\n" +
			"    private void initRegistersMap(){\n" +
			"    	if (registersMap != null) {\n" +
			"    		return;\n" +
			"    	}\n" +
			"    	registersMap = new TreeMap<>();\n" +
			"    	for (java.lang.reflect.Field field : this.getClass().getDeclaredFields()) {\n" +
			"        	if (!Register.class.isAssignableFrom(field.getType())){\n" +
			"        		continue;\n" +
			"        	}\n" +
			"        	try {\n" +
			"        		registersMap.put(field.getName(), (Register) field.get(this));\n" +
			"			} catch (Exception e) {\n" +
			"				e.printStackTrace();\n" +
			"			}\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    public Register getRegister(String name) {\n" +
			"    	return registersMap.get(name);\n" +
			"    }\n" +
			"    public Collection<Register> getRegisters(){\n" +
			"    	initRegistersMap();\n" +
			"    	return registersMap.values();\n" +
			"    }\n" +
			"}\n" //$NON-NLS-1$
		);

		env.addClass(projectPath, "reg", "Reg", //$NON-NLS-1$ //$NON-NLS-2$
				"package reg;\n" +
				"\n" +
				"public final class Reg {\n" +
				"\n" +
				"    Peripheral_TIMER0 peripheral_Timer0 = new Peripheral_TIMER0();\n" +
				"\n" +
				"    public static final class Peripheral_TIMER0 extends Peripheral {\n" +
				"\n" +
				"        public Peripheral_TIMER0() {\n" +
				"            super(\"TIMER0\", \"1.0\", \"desc\", \"groupName\", 0, 32);\n" +
				"        }\n" +
				"\n" +
				"        public Reg_CR regCR = new Reg_CR();\n" +
				"\n" +
				"        public static final class Reg_CR extends Register {\n" +
				"            public Reg_CR() {\n" +
				"                super(\"CR\", \"\", 0, 32, AccessType.readWrite, 0xf, 0x0);\n" +
				"            }\n" +
				"\n" +
				"            // fields of CR\n" +
				"            public Field_EN fieldEn = new Field_EN();\n" +
				"\n" +
				"            public static final class Field_EN extends Field {\n" +
				"                public Field_EN() {\n" +
				"                    super(\"EN\", \"description\", 0, AccessType.readWrite);\n" +
				"                }\n" +
				"            }\n" +
				"\n" +
				"            public Field_RST fieldRST = new Field_RST();\n" +
				"\n" +
				"            public static final class Field_RST extends Field {\n" +
				"                public Field_RST() {\n" +
				"                    super(\"RST\", \"description\", 1, AccessType.readWrite);\n" +
				"                }\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        Reg reg = new Reg();\n" +
				"\n" +
				"        System.out.println(reg.peripheral_Timer0.regCR.name + \": \" + reg.peripheral_Timer0.regCR.resetValue);\n" +
				"        System.out.println(reg.peripheral_Timer0.regCR.fieldEn.name + \": \" + reg.peripheral_Timer0.regCR.fieldEn.bitRange);\n" +
				"    }\n" +
				"}\n" //$NON-NLS-1$
			);

		env.addClass(projectPath, "reg", "Register", //$NON-NLS-1$ //$NON-NLS-2$
				"package reg;\n" +
				"\n" +
				"import java.util.Collection;\n" +
				"import java.util.Map;\n" +
				"import java.util.TreeMap;\n" +
				"\n" +
				"public class Register {\n" +
				"\n" +
				"    public enum AccessType {\n" +
				"        readOnly, readWrite;\n" +
				"    }\n" +
				"\n" +
				"    String name;\n" +
				"    String description;\n" +
				"    long addressOffset;\n" +
				"    long size;\n" +
				"    AccessType accessType;\n" +
				"    long resetValue;\n" +
				"    long resetMask;\n" +
				"    long value;\n" +
				"\n" +
				"    Map<String, Field> fieldsMap;\n" +
				"\n" +
				"    public Register(String name, String description, long addressOffset, long size,\n" +
				"            AccessType accessType, long resetValue, long resetMask) {\n" +
				"        this.name = name;\n" +
				"        this.description = description;\n" +
				"        this.addressOffset = addressOffset;\n" +
				"        this.size = size;\n" +
				"        this.accessType = accessType;\n" +
				"        this.resetValue = resetValue;\n" +
				"        this.resetMask = resetMask;\n" +
				"\n" +
				"    }\n" +
				"\n" +
				"    private void initFieldsMap(){\n" +
				"    	if (fieldsMap != null) {\n" +
				"    		return;\n" +
				"    	}\n" +
				"    	fieldsMap = new TreeMap<>();\n" +
				"    	for (java.lang.reflect.Field field : this.getClass().getDeclaredFields()) {\n" +
				"        	if (!Field.class.isAssignableFrom(field.getType())){\n" +
				"        		continue;\n" +
				"        	}\n" +
				"        	try {\n" +
				"				fieldsMap.put(field.getName(), (Field) field.get(this));\n" +
				"			} catch (Exception e) {\n" +
				"				e.printStackTrace();\n" +
				"			}\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    public void setValue(long value){\n" +
				"    	this.value = value;\n" +
				"    }\n" +
				"\n" +
				"    public long getValue(){\n" +
				"    	return this.value;\n" +
				"    }\n" +
				"\n" +
				"    public Collection<Field> getFields(){\n" +
				"    	initFieldsMap();\n" +
				"    	return fieldsMap.values();\n" +
				"    }\n" +
				"\n" +
				"	@Override\n" +
				"	public String toString() {\n" +
				"		return \"Register [name=\" + name + \", description=\" + description + \", addressOffset=\" + addressOffset\n" +
				"				+ \", size=\" + size + \", accessType=\" + accessType + \", resetValue=\" + resetValue + \", resetMask=\"\n" +
				"				+ resetMask + \"]\";\n" +
				"	}\n" +
				"\n" +
				"\n" +
				"}\n" //$NON-NLS-1$
			);

		env.addClass(projectPath, "reg.generate", "DeviceXY", genSource(maxPeripheral, maxRegister, maxFields));
	}

	public static String genSource(int maxPeripheral, int maxRegister, int maxFields) {
		/*
		 *
		 * The example that took 25 minutes was generated with: int maxPeripheral = 10;
		 * int maxRegister = 500; int maxFields = 64;
		 *
		 * Still performs ok (less than a minute): int maxPeripheral = 2; int
		 * maxRegister = 500; int maxFields = 64;
		 *
		 * With this (8 MB) it gets already slow (few minutes): int maxPeripheral = 3;
		 * int maxRegister = 500; int maxFields = 64;
		 *
		 * This takes 5 minutes (14MB file-size): int maxPeripheral = 5; int maxRegister
		 * = 500; int maxFields = 64;
		 */

		StringBuilder source = new StringBuilder();

		source.append("package reg.generate;").append(System.lineSeparator());
		source.append("public class DeviceXY {").append(System.lineSeparator());
		source.append("private static DeviceXY instance;").append(System.lineSeparator());
		source.append("private DeviceXY() {}").append(System.lineSeparator());
		source.append("public static DeviceXY getInstance() {").append(System.lineSeparator());
		source.append("  if (instance == null) { instance = new DeviceXY(); }").append(System.lineSeparator());
		source.append("  return instance;").append(System.lineSeparator());
		source.append("}").append(System.lineSeparator());

		for (int peripheral = 0; peripheral < maxPeripheral; peripheral++) {
			String peripheralName = "peri_" + peripheral;
			String peripheralClassName = String.valueOf(Character.toUpperCase(peripheralName.charAt(0)))
					+ peripheralName.substring(1);

			source.append("public ").append(peripheralClassName).append(" ").append(peripheralName)
					.append(" = new ").append(peripheralClassName).append("();").append(System.lineSeparator());
			source.append("public static final class ").append(peripheralClassName)
					.append(" extends reg.Peripheral {").append(System.lineSeparator());
			// constructor start
			source.append("public ").append(peripheralClassName).append("() {").append(System.lineSeparator());
			source.append("  super(\"").append(peripheralName).append("\",")
					.append("\"1.0\",\"desc\", \"groupName\", 0, 32);").append(System.lineSeparator());
			source.append("}").append(System.lineSeparator());
			// constructor end

			for (int register = 0; register < maxRegister; register++) {
				String registerName = "reg_" + register;
				String registerClassName = String.valueOf(Character.toUpperCase(registerName.charAt(0)))
						+ registerName.substring(1);

				source.append("public ").append(registerClassName).append(" ").append(registerName)
						.append(" = new ").append(registerClassName).append("();").append(System.lineSeparator());

				source.append("/**").append(System.lineSeparator());
				source.append("* Register ").append(registerName).append(System.lineSeparator());
				source.append("*/").append(System.lineSeparator());
				source.append("public static final class ").append(registerClassName)
						.append(" extends reg.Register {").append(System.lineSeparator());
				// constructor start
				source.append("public ").append(registerClassName).append("() {")
						.append(System.lineSeparator());
				source.append("  super(\"").append(registerName).append("\",")
						.append("\"desc\", 0, 32, AccessType.readWrite, 0xf, 0x0);").append(System.lineSeparator());
				source.append("}").append(System.lineSeparator());
				// constructor end

				source.append("//fields").append(System.lineSeparator());

				for (int field = 0; field < maxFields; field++) {
					String fieldName = "field_" + field;
					source.append("public ").append("reg.Field").append(" ").append(fieldName)
							.append(" = new ").append("reg.Field").append("(\"").append(fieldName).append("\",")
							.append("\"desc\", 0, AccessType.readWrite);").append(System.lineSeparator());

				}

				source.append("}").append(System.lineSeparator());
			}
			source.append("}").append(System.lineSeparator());
		}


		source.append("}").append(System.lineSeparator());

		return source.toString();

	}
}
