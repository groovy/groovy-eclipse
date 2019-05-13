/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Version;

public final class GenericsTests extends GroovyCompilerTestSuite {

    private void runWarningFreeTest(String[] sources) {
        runNegativeTest(sources, ""); // expect no compiler output (warnings or errors)
    }

    @Test
    public void testGenericField() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  List<String> bar\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericArrayField() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  List<String>[] bar\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericParam() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  public void m(List<String> bar) {}\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericArrayParam() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  public void m(List<String>[] bar) {}\n" +
            "}",
        };
        //@formatter:on

        if (!isAtLeastJava(JDK7)) {
            runWarningFreeTest(sources);
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. WARNING in Foo.groovy (at line 2)\n" +
                "\tpublic void m(List<String>[] bar) {}\n" +
                "\t              ^^^^^^^^^^^^^^^^^^\n" +
                "Type safety: Potential heap pollution via varargs parameter bar\n" +
                "----------\n");
        }
    }

    @Test
    public void testGenericVaragsParam() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  public void m(List<String>... bar) {}\n" +
            "}",
        };
        //@formatter:on

        if (!isAtLeastJava(JDK7)) {
            runWarningFreeTest(sources);
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. WARNING in Foo.groovy (at line 2)\n" +
                "\tpublic void m(List<String>... bar) {}\n" +
                "\t              ^^^^^^^^^^^^^^^^^^^\n" +
                "Type safety: Potential heap pollution via varargs parameter bar\n" +
                "----------\n");
        }
    }

    @Test
    public void testCallingGenericConstructors() {
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new A(35);\n" +
            "    System.out.println('success');\n" +
            "  }\n" +
            "}",

            "p/A.java",
            "package p;\n" +
            "public class A {\n" +
            "  public <T> A(T t) {}\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericsPositions_GRE267_1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  Set<?> setone;\n" +
            "  Set<? extends Serializable> settwo;\n" +
            "  Set<? super Number> setthree;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",

            // this Java class is for comparison - breakpoint on building type bindings and you can check the decls
            "Y.java",
            "import java.util.*;\n" +
            "class Y {\n" +
            "  Set<?> a;\n" +
            "  Set<? extends java.io.Serializable> b;\n" +
            "  Set<? super Number> c;\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        String one = stringify(findField(decl, "setone").type);
        String two = stringify(findField(decl, "settwo").type);
        String three = stringify(findField(decl, "setthree").type);
        assertEquals("(12>14)Set<(16>16)?>", one);
        assertEquals("(29>31)Set<(33>54)? extends (43>54)Serializable>", two);
        assertEquals("(67>69)Set<(71>84)? super (79>84)Number>", three);
    }

    @Test
    public void testGenericsPositions_GRE267_2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  Set<?> setone;\n" +
            "  Set<? extends java.io.Serializable> settwo;\n" +
            "  Set<? super java.lang.Thread> setthree;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        String one = stringify(findField(decl, "setone").type);
        String two = stringify(findField(decl, "settwo").type);
        String three = stringify(findField(decl, "setthree").type);
        assertEquals("(12>14)Set<(16>16)?>", one);
        assertEquals("(29>31)Set<(33>62)? extends (43>62)(43>46)java.(48>49)io.(51>62)Serializable>", two);
        assertEquals("(75>77)Set<(79>102)? super (87>102)(87>90)java.(92>95)lang.(97>102)Thread>", three);
    }

    @Test
    public void testGenericsPositions_GRE267_3() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  Set<?> setone;\n" +
            "  Set<String[]> settwo;\n" +
            "  Set<String[][]> setthree;\n" +
            "  Set<java.lang.Thread[][][]> setfour;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",

            "Y.java",
            "import java.util.*;\n" +
            "class Y {\n" +
            "  Set<String[]> a;\n" +
            "  Set<String[][]> b;\n" +
            "  Set<java.lang.Thread[][][]> c;\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        String one = stringify(findField(decl, "setone").type);
        String two = stringify(findField(decl, "settwo").type);
        String three = stringify(findField(decl, "setthree").type);
        String four = stringify(findField(decl, "setfour").type);
        assertEquals("(12>14)Set<(16>16)?>", one);
        assertEquals("(29>31)Set<(33>40 ose:38)String[]>", two);
        assertEquals("(53>55)Set<(57>66 ose:62)String[][]>", three);
        assertEquals("(81>83)Set<(85>106)(85>88)java.(90>93)lang.(95>100)Thread[][][]>", four);
    }

    @Test
    public void testGenericsPositions_4_GRE267() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  java.util.Set<?> setone;\n" +
            "  java.util.Set<? extends Serializable> settwo;\n" +
            "  java.util.Set<? super Number> setthree;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        String one = stringify(findField(decl, "setone").type);
        String two = stringify(findField(decl, "settwo").type);
        String three = stringify(findField(decl, "setthree").type);
        assertEquals("(12>24)(12>15)java.(17>20)util.(22>24)Set<(26>26)?>", one);
        assertEquals("(39>51)(39>42)java.(44>47)util.(49>51)Set<(53>74)? extends (63>74)Serializable>", two);
        assertEquals("(87>99)(87>90)java.(92>95)util.(97>99)Set<(101>114)? super (109>114)Number>", three);
    }

    @Test
    public void testGenericsPositions_5_GRE267() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  java.util.Set<?> setone;\n" +
            "  java.util.Set<? extends java.io.Serializable> settwo;\n" +
            "  java.util.Set<? super java.lang.Thread> setthree;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        String one = stringify(findField(decl, "setone").type);
        String two = stringify(findField(decl, "settwo").type);
        String three = stringify(findField(decl, "setthree").type);
        assertEquals("(12>24)(12>15)java.(17>20)util.(22>24)Set<(26>26)?>", one);
        assertEquals("(39>51)(39>42)java.(44>47)util.(49>51)Set<(53>82)? extends (63>82)(63>66)java.(68>69)io.(71>82)Serializable>", two);
        assertEquals("(95>107)(95>98)java.(100>103)util.(105>107)Set<(109>132)? super (117>132)(117>120)java.(122>125)lang.(127>132)Thread>", three);
    }

    @Test @Ignore("support for A<X>.B<Y> has not been implemented")
    public void testGenericsPositions_6_GRE267() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  One<String,Integer>.Two<Boolean> whoa;\n" + // multiple generified components in a reference
            "  java.util.Set<? extends java.io.Serializable> settwo;\n" +
            "  java.util.Set<? super java.lang.Number> setthree;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",

            "One.java",
            "public class One<A,B> {\n" +
            "  public class Two<C> {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        String one = stringify(findField(decl, "one").type);
        String two = stringify(findField(decl, "settwo").type);
        String three = stringify(findField(decl, "setthree").type);
        assertEquals("(12>14)Set<(16>16)?>", one);
        assertEquals("(29>31)Set<(33>33)? extends (43>61)(43>47)java.(48>50)io.(51>61)Serializable>", two);
        assertEquals("(67>69)Set<(71>71)? super (79>84)Number>", three);
    }

    @Test
    public void testGenericsPositions_7_GRE267() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  java.util.Set<?> setone;\n" +
            "  java.util.Set<String[]> settwo;\n" +
            "  java.util.Set<java.lang.Number[][][]> setthree;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        String one = stringify(findField(decl, "setone").type);
        String two = stringify(findField(decl, "settwo").type);
        String three = stringify(findField(decl, "setthree").type);
        assertEquals("(12>24)(12>15)java.(17>20)util.(22>24)Set<(26>26)?>", one);
        assertEquals("(39>51)(39>42)java.(44>47)util.(49>51)Set<(53>60 ose:58)String[]>", two);
        assertEquals("(73>85)(73>76)java.(78>81)util.(83>85)Set<(87>108)(87>90)java.(92>95)lang.(97>102)Number[][][]>", three);
    }

    @Test
    public void testGenericsPositions_8_GRE267() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  Set<Map.Entry<String,List<String>>> foo;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        assertEquals("(12>14)Set<(16>24)(16>18)Map.(20>24)Entry<(26>31)String(33>36)List<(38>43)String>>>", stringify(findField(decl, "foo").type));
    }

    @Test
    public void testGenericsPositions_9_GRE267() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  Map.Entry<String,List<String>> foo;\n" +
            "  public static void main(String[]argv){ print 'y' }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("X.groovy");
        assertEquals("(12>20)(12>14)Map.(16>20)Entry<(22>27)String(29>32)List<(34>39)String>>", stringify(findField(decl, "foo").type));
    }

    @Test
    public void testGenericsAndGroovyJava_GRE278_1() {
        //@formatter:off
        String[] sources = {
            "p/Field.java",
            "package test;\n" +
            "public interface Field<T> extends Comparable<T> {\n" +
            "    public String getFieldTypeName();\n" +
            "    public String getName();\n" +
            "    public T getValue();\n" +
            "    public void setValue(T o);\n" +
            "}",

            "p/Structure.java",
            "package test;\n" +
            "import java.util.Map;\n" +
            "import java.nio.ByteBuffer;\n" +
            "public interface Structure extends Map<String, Field<?>> {\n" +
            "   public void reset();\n" +
            "   public void setup(ByteBuffer clientBuff);\n" +
            "}",

            "p/StructureBase.groovy",
            "package test;\n" +
            "import java.nio.ByteBuffer;\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "public class StructureBase implements Structure {\n" +
            "   protected final Structure str = null;\n" +
            "   StructureBase(Structure struct){\n" +
            "       this.str = struct;\n" +
            "   }\n" +
            "   public void clear() {\n" +
            "       str.clear()\n" +
            "   }\n" +
            "   public boolean containsKey(Object arg0) {\n" +
            "       return str.containsKey(arg0);\n" +
            "   }\n" +
            "   public boolean containsValue(Object arg0) {\n" +
            "       return str.containsValue(arg0);\n" +
            "   }\n" +
            "   public Set<java.util.Map.Entry<String, Field<?>>> entrySet() {\n" +
            "       return Collections.unmodifiableSet(str.entrySet());\n" +
            "   }\n" +
            "   public Field<?> get(Object arg0) {\n" +
            "       return str.get(arg0);\n" +
            "   }\n" +
            "   public boolean isEmpty() {\n" +
            "       return str.isEmpty();\n" +
            "   }\n" +
            "   public Set<String> keySet() {\n" +
            "       return Collections.unmodifiableSet(str.keySet());\n" +
            "   }\n" +
            "   public Field<?> put(String arg0, Field<?> arg1) {\n" +
            "       return str.put(arg0, arg1);\n" +
            "   }\n" +
            "   //public Object put(Object key, Object value) {\n" +
            "   //    return str.put(key, value)\n" +
            "   //}\n" +
            "   public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n" +
            "       str.putAll(arg0);\n" +
            "   }\n" +
            "   public Field<?> remove(Object key) {\n" +
            "       return str.remove(key);\n" +
            "   }\n" +
            "   public int size() {\n" +
            "       return str.size();\n" +
            "   }\n" +
            "   public Collection<Field<?>> values() {\n" +
            "       return Collections.unmodifiableCollection(str.values());\n" +
            "   }\n" +
            "   //public void reset(){\n" +
            "   //    str.reset();\n" +
            "   //}\n" +
            "   //public void setup(ByteBuffer buff) {\n" +
            "   //    str.setup(buff);\n" +
            "   //}\n" +
            "}",

            "p/StructureBaseTest.groovy",
            "package test;\n" +
            "public final class StructureBaseTest {\n" +
            "   public static void main(String[] args) {\n" +
            "        Structure str = new StructureBase(new TestStructure());\n" +
            "        str.put('test', new TestField());\n" +
            "           def content = str.get('test');\n" +
            "           if (!TestField.FIELD_NAME.equals(str.get('test').name)) {\n" +
            "               System.out.println('Failed');\n" +
            "           }\n" +
            "   }\n" +
            "}",

            "p/TestField.java",
            "package test;\n" +
            "public class TestField implements Field<String> {\n" +
            "   public static final String FIELD_NAME = \"Test\";\n" +
            "   private StringBuilder buffer = new StringBuilder();\n" +
            "   private String value = null;\n" +
            "   public String getFieldTypeName() {\n" +
            "       return String.class.getSimpleName();\n" +
            "   }\n" +
            "   public String getName() {\n" +
            "       return FIELD_NAME;\n" +
            "   }\n" +
            "   public String getValue() {\n" +
            "       if (null == value)\n" +
            "           value = buffer.toString();\n" +
            "       return value;\n" +
            "   }\n" +
            "   public void setValue(String o) {\n" +
            "       value = o;\n" +
            "       buffer.replace(0, buffer.length(), o);\n" +
            "   }\n" +
            "   public int compareTo(String arg0) {\n" +
            "       return getValue().compareTo(arg0);\n" +
            "   }\n" +
            "}",

            "p/TestStructure.java",
            "package test;\n" +
            "import java.nio.ByteBuffer;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.Map;\n" +
            "@SuppressWarnings(\"serial\")\n" +
            "public class TestStructure extends HashMap<String, Field<?>> implements Structure {\n" +
            "   public void reset() {\n" +
            "       // TODO Auto-generated method stub\n" +
            "   }\n" +
            "   public void setup(ByteBuffer clientBuff) {\n" +
            "       // TODO Auto-generated method stub\n" +
            "   }\n" +
            "   public Field<?> put(String arg0, Field<?> arg1) {\n" +
            "       return super.put(arg0, arg1);\n" +
            "}\n" +
            "   public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n" +
            "       super.putAll(arg0);\n" +
            "   }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\StructureBase.groovy (at line 4)\n" +
            "\tpublic class StructureBase implements Structure {\n" +
            "\t             ^^^^^^^^^^^^^\n" +
            "Groovy:Can\'t have an abstract method in a non-abstract class. The class \'test.StructureBase\'" +
            " must be declared abstract or the method \'void setup(java.nio.ByteBuffer)\' must be implemented.\n" +
            "----------\n" +
            "2. ERROR in p\\StructureBase.groovy (at line 4)\n" +
            "\tpublic class StructureBase implements Structure {\n" +
            "\t             ^^^^^^^^^^^^^\n" +
            "Groovy:Can\'t have an abstract method in a non-abstract class. The class \'test.StructureBase\'" +
            " must be declared abstract or the method \'void reset()\' must be implemented.\n" +
            "----------\n");
    }

    @Test
    public void testGenericsAndGroovyJava_GRE278_2() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[]argv) {\n" +
            "    test.StructureBaseTest.main(argv);\n" +
            "  }\n" +
            "}",

            "p/Field.java",
            "package test;\n" +
            "public interface Field<T> extends Comparable<T> {\n" +
            "    public String getFieldTypeName();\n" +
            "    public String getName();\n" +
            "    public T getValue();\n" +
            "    public void setValue(T o);\n" +
            "}",

            "p/Structure.java",
            "package test;\n" +
            "import java.util.Map;\n" +
            "import java.nio.ByteBuffer;\n" +
            "public interface Structure extends Map<String, Field<?>> {\n" +
            "   public void reset();\n" +
            "   public void setup(ByteBuffer clientBuff);\n" +
            "}",

            "p/StructureBase.groovy",
            "package test;\n" +
            "import java.nio.ByteBuffer;\n" +
            "public class StructureBase implements Structure {\n" +
            "   protected final Structure str = null;\n" +
            "   StructureBase(Structure struct){\n" +
            "       this.str = struct;\n" +
            "   }\n" +
            "   public void clear() {\n" +
            "       str.clear()\n" +
            "   }\n" +
            "   public boolean containsKey(Object arg0) {\n" +
            "       return str.containsKey(arg0);\n" +
            "   }\n" +
            "   public boolean containsValue(Object arg0) {\n" +
            "       return str.containsValue(arg0);\n" +
            "   }\n" +
            "   public Set<java.util.Map.Entry<String, Field<?>>> entrySet() {\n" +
            "       return Collections.unmodifiableSet(str.entrySet());\n" +
            "   }\n" +
            "   public Field<?> get(Object arg0) {\n" +
            "       return str.get(arg0);\n" +
            "   }\n" +
            "   public boolean isEmpty() {\n" +
            "       return str.isEmpty();\n" +
            "   }\n" +
            "   public Set<String> keySet() {\n" +
            "       return Collections.unmodifiableSet(str.keySet());\n" +
            "   }\n" +
            "   public Field<?> put(String arg0, Field<?> arg1) {\n" +
            "       return str.put(arg0, arg1);\n" +
            "   }\n" +
            "   public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n" +
            "       str.putAll(arg0);\n" +
            "   }\n" +
            "   public Field<?> remove(Object key) {\n" +
            "       return str.remove(key);\n" +
            "   }\n" +
            "   public int size() {\n" +
            "       return str.size();\n" +
            "   }\n" +
            "   public Collection<Field<?>> values() {\n" +
            "       return Collections.unmodifiableCollection(str.values());\n" +
            "   }\n" +
            "   public void reset() {\n" +
            "       str.reset();\n" +
            "   }\n" +
            "   public void setup(ByteBuffer buff) {\n" +
            "       str.setup(buff);\n" +
            "   }\n" +
            "}",

            "p/StructureBaseTest.groovy",
            "package test;\n" +
            "public final class StructureBaseTest {\n" +
            "   public static void main(String[] args) {\n" +
            "        Structure str = new StructureBase(new TestStructure());\n" +
            "        str.put('test', new TestField());\n" +
            "           def content = str.get('test');\n" +
            "           if (!TestField.FIELD_NAME.equals(str.get('test').name)) {\n" +
            "               println('Failed');\n" +
            "           }\n" +
            "   }\n" +
            "}",

            "p/TestField.java",
            "package test;\n" +
            "public class TestField implements Field<String> {\n" +
            "   public static final String FIELD_NAME = \"Test\";\n" +
            "   private StringBuilder buffer = new StringBuilder();\n" +
            "   private String value = null;\n" +
            "   public String getFieldTypeName() {\n" +
            "       return String.class.getSimpleName();\n" +
            "   }\n" +
            "   public String getName() {\n" +
            "       return FIELD_NAME;\n" +
            "   }\n" +
            "   public String getValue() {\n" +
            "       if (null == value)\n" +
            "           value = buffer.toString();\n" +
            "       return value;\n" +
            "   }\n" +
            "   public void setValue(String o) {\n" +
            "       value = o;\n" +
            "       buffer.replace(0, buffer.length(), o);\n" +
            "   }\n" +
            "   public int compareTo(String arg0) {\n" +
            "       return getValue().compareTo(arg0);\n" +
            "   }\n" +
            "}",

            "p/TestStructure.java",
            "package test;\n" +
            "import java.nio.ByteBuffer;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.Map;\n" +
            "@SuppressWarnings(\"serial\")\n" +
            "public class TestStructure extends HashMap<String, Field<?>> implements Structure {\n" +
            "   public void reset() {\n" +
            "       // TODO Auto-generated method stub\n" +
            "   }\n" +
            "   public void setup(ByteBuffer clientBuff) {\n" +
            "       // TODO Auto-generated method stub\n" +
            "   }\n" +
            "   public Field<?> put(String arg0, Field<?> arg1) {\n" +
            "       return super.put(arg0, arg1);\n" +
            "}\n" +
            "   public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n" +
            "       super.putAll(arg0);\n" +
            "   }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericsAndGroovyJava_GRE278_3() {
        //@formatter:off
        String[] sources = {
            "Field.java",
            "public interface Field<T extends java.io.Serializable> extends Comparable<T> {\n" +
            "}",

            "Other.groovy",
            "class Other {\n" +
            "  Field<?> get(Object obj) {\n" +
            "    null\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericsAndGroovyJava_GRE278_4() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    StructureBaseTest.main(args);\n" +
            "  }\n" +
            "}",

            "Field.java",
            "public interface Field<T extends java.io.Serializable> extends Comparable<T> {\n" +
            "  public String getFieldTypeName();\n" +
            "  public String getName();\n" +
            "  public T getValue();\n" +
            "  public void setValue(T o);\n" +
            "}",

            "Structure.java",
            "import java.util.Map;\n" +
            "import java.nio.ByteBuffer;\n" +
            "public interface Structure extends Map<String, Field<?>> {\n" +
            "  public void reset();\n" +
            "  public void setup(ByteBuffer clientBuff);\n" +
            "}",

            "StructureBase.groovy",
            "import java.nio.ByteBuffer;\n" +
            "class StructureBase implements Structure {\n" +
            "  protected final Structure str = null\n" +
            "  StructureBase(Structure struct) {\n" +
            "    this.str = struct\n" +
            "  }\n" +
            "  void clear() {\n" +
            "    str.clear()\n" +
            "  }\n" +
            "  boolean containsKey(Object arg0) {\n" +
            "    str.containsKey(arg0)\n" +
            "  }\n" +
            "  boolean containsValue(Object arg0) {\n" +
            "    str.containsValue(arg0)\n" +
            "  }\n" +
            "  Set<java.util.Map.Entry<String, Field<?>>> entrySet() {\n" +
            "    Collections.unmodifiableSet(str.entrySet())\n" +
            "  }\n" +
            "  Field<?> get(Object arg0) {\n" +
            "    str.get(arg0)\n" +
            "  }\n" +
            "  boolean isEmpty() {\n" +
            "    str.isEmpty()\n" +
            "  }\n" +
            "  Set<String> keySet() {\n" +
            "    Collections.unmodifiableSet(str.keySet())\n" +
            "  }\n" +
            "  Field<?> put(String key, Field<?> val) {\n" +
            "    str.put(key, val)\n" +
            "  }\n" +
            "  void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n" +
            "    str.putAll(arg0)\n" +
            "  }\n" +
            "  Field<?> remove(Object key) {\n" +
            "    str.remove(key)\n" +
            "  }\n" +
            "  int size() {\n" +
            "    str.size();\n" +
            "  }\n" +
            "  Collection<Field<?>> values() {\n" +
            "    Collections.unmodifiableCollection(str.values())\n" +
            "  }\n" +
            "  void reset() {\n" +
            "    str.reset()\n" +
            "  }\n" +
            "  void setup(ByteBuffer buff) {\n" +
            "    str.setup(buff)\n" +
            "  }\n" +
            "}",

            "StructureBaseTest.groovy",
            "final class StructureBaseTest {\n" +
            "  static void main(String[] args) {\n" +
            "    Structure str = new StructureBase(new TestStructure())\n" +
            "    str.put('test', new TestField())\n" +
            "    def content = str.get('test')\n" +
            "    if (!TestField.FIELD_NAME == str.get('test').name) {\n" +
            "      println 'Failed'\n" +
            "    } else {\n" +
            "      println 'Success'\n" +
            "    }\n" +
            "  }\n" +
            "}",

            "TestField.java",
            "public class TestField implements Field<String> {\n" +
            "  public static final String FIELD_NAME = \"Test\";\n" +
            "  private StringBuilder buffer = new StringBuilder();\n" +
            "  private String value = null;\n" +
            "  public String getFieldTypeName() {\n" +
            "    return String.class.getSimpleName();\n" +
            "  }\n" +
            "  public String getName() {\n" +
            "    return FIELD_NAME;\n" +
            "  }\n" +
            "  public String getValue() {\n" +
            "    if (null == value)\n" +
            "      value = buffer.toString();\n" +
            "    return value;\n" +
            "  }\n" +
            "  public void setValue(String o) {\n" +
            "    value = o;\n" +
            "    buffer.replace(0, buffer.length(), o);\n" +
            "  }\n" +
            "  public int compareTo(String arg0) {\n" +
            "    return getValue().compareTo(arg0);\n" +
            "  }\n" +
            "}",

            "TestStructure.java",
            "import java.nio.ByteBuffer;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.Map;\n" +
            "@SuppressWarnings(\"serial\")\n" +
            "public class TestStructure extends HashMap<String, Field<?>> implements Structure {\n" +
            "  public void reset() {\n" +
            "  }\n" +
            "  public void setup(ByteBuffer clientBuff) {\n" +
            "  }\n" +
            "  public Field<?> put(String arg0, Field<?> arg1) {\n" +
            "    return super.put(arg0, arg1);\n" +
            "  }\n" +
            "  public void putAll(Map<? extends String, ? extends Field<?>> arg0) {\n" +
            "    super.putAll(arg0);\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericsAndGroovyJava_GRE278_5() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "public class Main {\n" +
            "  public static void main(String[]argv) {\n" +
            "    def content = new StructureBase().get('test');\n" +
            "    print 'test';\n" +
            "  }\n" +
            "}",

            "MyMap.java",
            "public interface MyMap<A,B> {" +
            "  B get(Object key);\n" +
            "}",

            "Structure.java",
            "public interface Structure extends MyMap<String, Integer> {\n" +
            "}",

            "StructureBase.groovy",
            "public class StructureBase implements Structure {\n" +
            "   public Integer get(Object key) {\n" +
            "       return null;\n" +
            "   }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericFields_JcallingG() {
        //@formatter:off
        String[] sources = {
            "p/Code.java",
            "package p;\n" +
            "public class Code extends G<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    Code c = new Code();\n" +
            "    c.setField(\"success\");\n" +
            "    System.out.print(c.getField());\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "class G<T> { T field; }",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGenericFields_GcallingJ() {
        //@formatter:off
        String[] sources = {
            "p/Code.groovy",
            "package p;\n" +
            "public class Code extends G<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    Code c = new Code();\n" +
            "    c.field=\"success\";\n" +
            "    System.out.print(c.field);\n" +
            "  }\n" +
            "}\n",

            "p/G.java",
            "package p;\n" +
            "class G<T> { public T field; }", // TODO why must this be public for the groovy code to see it?  If non public should it be instead defined as a property on the JDTClassNode rather than a field?
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGroovyPropertyAccessorsGenerics() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    for (Integer s: o.getB()) {\n" +
            "      System.out.print(s);\n" +
            "    }\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  List<Integer> b = [1,2,3]\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGroovyGenerics() {
        // GroovyBug: this surfaced the problem that the generics declarations are checked before resolution is complete -
        // had to change CompilationUnit so that resolve and checkGenerics are different stages in the SEMANTIC_ANALYSIS phase
        // otherwise it depends on whether the super type is resolved before the subtype has its generic decl checked
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B();\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/A.groovy",
            "package p;\n" +
            "public class A<T> {}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGreclipse1563() {
        //@formatter:off
        String[] sources = {
            "ab/Inter.java",
            "package ab;\n" +
            "public interface Inter {\n" +
            "    public Number getItem(Object itemId);\n" +
            "}\n",

            "ab/Clazz.java",
            "package ab;\n" +
            "public abstract class Clazz<ITEM extends Number> implements Inter {\n" +
            "   public ITEM getItem(Object itemId) {\n" +
            "       return null;\n" +
            "   }\n" +
            "}\n",

            "ab/GClazz.groovy",
            "package ab;\n" +
            "class GClazz extends Clazz<Number> {}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testGreclipse1563_2() {
        //@formatter:off
        String[] sources = {
            "ab/Clazz.java",
            "package ab;\n" +
            "public abstract class Clazz<ITEM extends MyItem> implements Inter {\n" +
            "   public ITEM getItem(Object itemId) {\n" +
            "       return null;\n" +
            "   }\n" +
            "}\n",

            "ab/Inter.java",
            "package ab;\n" +
            "public interface Inter {\n" +
            "    public MyItem getItem(Object itemId);\n" +
            "}\n",

            "ab/MyItem.java",
            "package ab;\n" +
            "public class MyItem {}\n",

            "ab/GClazz.groovy",
            "package ab;\n" +
            "class GClazz extends Clazz<MyItem> {}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test // WMTW: GroovyCompilationUnit builds a correct representation of the groovy type A
    public void testExtendingGenerics_JavaExtendsGroovy() {
        //@formatter:off
        String[] sources = {
            "p/B.java",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B();\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/A.groovy",
            "package p;\n" +
            "public class A<T> {}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test // WMTW: JDT ClassNode builds a correct groovy representation of the A type
    public void testExtendingGenerics_GroovyExtendsJava01() {

        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B()\n" +
            "    println 'success'\n" +
            "  }\n" +
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T> {public void set(T t) { }}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava02() {
        // test when the upper bound is not just 'Object'
        // WMTW: notice I and Impl are classes and not interfaces, because right now only the superclass stuff is set up correctly for nodes.
        // In order for no error to occur we have to override getUnresolvedSuperClass() in our JDTClassNode so that the code in
        // GenericsVisitor.checkGenericsUsage() correctly determines Impl isDerivedFrom I
        // the rule seems to be coming out that there is no redirection from JDTClassNode, they are absolute
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<Impl> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B()\n" +
            "    println 'success'\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p; class I {}",

            "p/Impl.java",
            "package p; class Impl extends I {}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends I> {}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava03() {
        // TODO create more variations around mixing types up (including generics bounds)
        // variation of above - the interface type is a java file and not a groovy file
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<Impl> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B()\n" +
            "    println 'success'\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p; interface I {}", // class->interface

            "p/Impl.java",
            "package p; class Impl implements I {}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends I> {}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava04() {
        // WMTW: JDTClassNode correctly initializes interfaces based on binding interfaces
        // It needs the interface set for Impl to be defined correctly so that groovy can determine Impl extends I
        // test when the upper bound is not just 'Object'
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<Impl> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B()\n" +
            "    println 'success'\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy", // java->groovy
            "package p; interface I {}", // class->interface

            "p/Impl.groovy",
            "package p; class Impl implements I {}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends I> {}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava05() {
        // GRECLIPSE-430: the declaration of B violates the 'T extends I' specification of A
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    println 'success'\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p; interface I {}",

            "p/Impl.java",
            "package p; class Impl implements I {}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends I> {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\B.groovy (at line 2)\n" +
            "\tpublic class B extends A<String> {\n" +
            "\t                         ^^^^^^\n" +
            "Groovy:The type String is not a valid substitute for the bounded parameter <T extends p.I>\n" +
            "----------\n");
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava06() {
        assumeTrue(!isAtLeastJava(JDK8));

        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends java.util.ArrayList<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    B b = new B()\n" +
            "    b.add('abc')\n" +
            "    print b.get(0)\n" +
            "    println 'success'\n" +
            "  }\n" +
            "  void print(String msg) { print msg; }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava07() {
        assumeTrue(!isAtLeastJava(JDK8));

        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends java.util.ArrayList<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B()\n" +
            "    println 'success'\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava08() {
        assumeTrue(!isAtLeastJava(JDK8));

        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends ArrayList<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B()\n" +
            "    println 'success'\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava09() {
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B()\n" +
            "    println 'success'\n" +
            "  }\n" +
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T> {public void set(T t) { }}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava10() {
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends q.A<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B().set(\"abc\");\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "q/A.java",
            "package q;\n" +
            "public class A<T> {public void set(T t) { }}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava11() {
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<int[]> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B().foo([1,2,3]);\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends Object> {\n" +
            "  public void foo(T t) {}\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava12() {
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B().foo([1,2,3]);\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/C.java",
            "package p;\n" +
            "public class C extends A<int[]> {}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends Object> {\n" +
            "  public void foo(T t) {}\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingGenerics_GroovyExtendsJava13() {
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends C<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B().foo([1,2,3],\"hello\");\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}",

            "p/C.java",
            "package p;\n" +
            "public class C<Q> extends A<int[],Q> {\n" +
            "}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends Object,R> {\n" +
            "  public void foo(T t, R r) {}\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    /**
     * https://issuetracker.springsource.com/browse/STS-3930
     *
     * @see org.codehaus.jdt.groovy.internal.compiler.ast.GroovyClassScope#buildFieldsAndMethods()
     */
    @Test
    public void testExtendingGenerics_GroovyExtendsJava14() {
        assumeTrue(JavaCore.getPlugin().getBundle().getVersion().compareTo(Version.parseVersion("3.10")) >= 0);

        //@formatter:off
        String[] sources = {
            "Groovy.groovy",
            "class Groovy {\n" +
            "  static <T> List<T> method(Class<T> factory, ClassLoader loader = Groovy.class.classLoader) {\n" +
            "    null\n" +
            "  }\n" +
            "}",

            "Java.java",
            "public class Java {\n" +
            "  public static void method() {\n" +
            "    Groovy.method(Java.class);\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    /**
     * https://github.com/groovy/groovy-eclipse/issues/144
     *
     * java.lang.NullPointerException
     *     at org.codehaus.jdt.groovy.internal.compiler.ast.GroovyClassScope.fixupTypeParameters(GroovyClassScope.java:559)
     */
    @Test
    public void testExtendingGenerics_GroovyExtendsJava15() {
        //@formatter:off
        String[] sources = {
            "Template.java",
            "public interface Template<S> {\n" +
            "  interface Callback<T,S> {\n" +
            "    T apply(S context);\n" +
            "  }\n" +
            "  <T> T execute(Callback<T,S> callback);\n" +
            "}",

            "TemplateImpl.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class TemplateImpl<S> implements Template<S> {\n" +
            "  @Override\n" +
            "  public <T> T execute(Callback<T,S> callback) {\n" +
            "    return callback.apply(null)\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    /**
     * https://github.com/groovy/groovy-eclipse/issues/148
     */
    @Test
    public void testExtendingGenerics_GroovyExtendsJava16() {
        //@formatter:off
        String[] sources = {
            "A.java",
            "public interface A<Q extends A<? super Q>> {\n" +
            "}",

            "B.groovy",
            "class B {\n" +
            "  public void test(A<?> a) {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    /**
     * https://github.com/groovy/groovy-eclipse/issues/174
     *
     * java.lang.NullPointerException
     *     at com.sun.beans.TypeResolver.resolve(TypeResolver.java:203)
     *     at com.sun.beans.TypeResolver.resolve(TypeResolver.java:162)
     *     at com.sun.beans.TypeResolver.resolveInClass(TypeResolver.java:81)
     *     at java.beans.FeatureDescriptor.getReturnType(FeatureDescriptor.java:370)
     *     at java.beans.Introspector.getTargetEventInfo(Introspector.java:1052)
     *     at java.beans.Introspector.getBeanInfo(Introspector.java:427)
     *     at java.beans.Introspector.getBeanInfo(Introspector.java:173)
     *     at groovy.lang.MetaClassImpl$15.run(MetaClassImpl.java:3290)
     *     at java.security.AccessController.doPrivileged(Native Method)
     *     at groovy.lang.MetaClassImpl.addProperties(MetaClassImpl.java:3288)
     *     at groovy.lang.MetaClassImpl.initialize(MetaClassImpl.java:3265)
     *     at org.codehaus.groovy.reflection.ClassInfo.getMetaClassUnderLock(ClassInfo.java:254)
     *     at org.codehaus.groovy.reflection.ClassInfo.getMetaClass(ClassInfo.java:285)
     *     at MIData.$getStaticMetaClass(MIData.groovy)
     *     at MIData.<init>(MIData.groovy)
     *     at Main.main(Main.groovy:3)
     */
    @Test
    public void testExtendingGenerics_GroovyExtendsJava17() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    def data = new MIData('V', 1, 'B')\n" +
            "    print 'no error'\n" +
            "  }\n" +
            "}",

            "MultiIndexed.java",
            "public interface MultiIndexed<PK, SK> {\n" +
            "  PK getPrimaryKey();\n" +
            "  SK[] getSecondaryKeys();\n" +
            "}",

            "MIData.groovy",
            "class MIData implements MultiIndexed<Integer, String> {\n" +
            "  final String value\n" +
            "  final Integer primaryKey\n" +
            "  final String[] secondaryKeys\n" +
            "\n" +
            "  MIData(String val, Integer pk, String... sk) {\n" +
            "    this.value = val\n" +
            "    this.primaryKey = pk\n" +
            "    this.secondaryKeys = sk\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources, "no error");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/221
    public void testExtendingGenerics_GroovyExtendsJava18() {
        //@formatter:off
        String[] sources = {
            "AttributeConverter.java",
            "interface AttributeConverter<X,Y> {\n" +
            "  Y encode(X eks);\n" +
            "  X decode(Y why);\n" +
            "}",

            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Main implements AttributeConverter<String,Object> {\n" +
            "  @Override\n" +
            "  Object encode(String s) { return null; }\n" +
            "  @Override\n" +
            "  String decode(Object o) { return null; }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyGenericType() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  public List<?> m() { return null;}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  List<?> m();\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testImplementingInterface_JavaGenericsIncorrectlyExtendingGroovyGenerics() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I<String> {\n" +
            "  public List<String> m() { return null;}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}",

            "p/I.groovy",
            "package p;\n" +
            "public interface I<T extends Number> {\n" +
            "  List<T> m();\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 3)\n" +
            "\tpublic class C extends groovy.lang.GroovyObjectSupport implements I<String> {\n" +
            "\t                                                                    ^^^^^^\n" +
            "Bound mismatch: The type String is not a valid substitute for the bounded parameter <T extends Number> of the type I<T>\n" +
            "----------\n");
    }

    @Test
    public void testImplementingInterface_GroovyGenericsIncorrectlyExtendingJavaGenerics() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C implements Iii<String> {\n" +
            "  public List<String> m() { return null;}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "import java.util.List;\n" +
            "public interface Iii<T extends Number> {\n" +
            "  List<T> m();\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.groovy (at line 2)\n" +
            "\tpublic class C implements Iii<String> {\n" +
            "\t                              ^^^^^^\n" +
            "Groovy:The type String is not a valid substitute for the bounded parameter <T extends java.lang.Number>\n" +
            "----------\n");
    }

    @Test
    public void testReferencingFieldsGenerics_JreferingToG() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    OtherClass oClass = new OtherClass();\n" +
            "    for (String message: oClass.messages) {\n" +
            "      System.out.print(message);\n" +
            "    }\n" +
            "  }\n" +
            "}",

            "p/OtherClass.groovy",
            "package p;\n" +
            "public class OtherClass {\n" +
            "  public List<String> messages = new ArrayList<String>();\n" + // auto imports of java.util
            "  public OtherClass() {\n" +
            "    messages.add(\"hello\");\n" +
            "    messages.add(\" \");\n" +
            "    messages.add(\"world\");\n" +
            "    messages.add(\"\\n\");\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testReferencingFieldsGenerics_GreferingToJ() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    OtherClass oClass = new OtherClass();\n" +
            "    for (String message: oClass.messages) {\n" +
            "      System.out.print(message);\n" +
            "    }\n" +
            "  }\n" +
            "}",

            "p/OtherClass.java",
            "package p;\n" +
            "import java.util.*;\n" +
            "public class OtherClass {\n" +
            "  public List<String> messages = new ArrayList<String>();\n" + // auto imports of java.util
            "  public OtherClass() {\n" +
            "    messages.add(\"hello\");\n" +
            "    messages.add(\" \");\n" +
            "    messages.add(\"world\");\n" +
            "    messages.add(\"\\n\");\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testAbstractCovariance_1() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo implements Comparable<String> {\n" +
            "  @Override\n" +
            "  int compareTo(String string) { this <=> string }\n" +
            "}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testAbstractCovariance_2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo implements Comparable<String> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tclass Foo implements Comparable<String> {\n" +
            "\t      ^^^\n" +
            "Groovy:Can't have an abstract method in a non-abstract class. The class 'Foo'" +
            " must be declared abstract or the method 'int compareTo(java.lang.Object)' must be implemented.\n" +
            "----------\n");
    }

    @Test
    public void testAbstractCovariance_3() {
        //@formatter:off
        String[] sources = {
            "Face1.java",
            "@FunctionalInterface\n" +
            "interface Face1<I, O> {\n" +
            "  O apply(I in);\n" +
            "}\n",

            "Face2.java",
            "interface Face2<X, Y> extends Face1<X, Y> {\n" +
            "  Object another();\n" +
            "}\n",

            "Impl.groovy",
            "class Impl implements Face2<Number, String> {\n" +
            "  @Override String apply(Number n) { '' }\n" +
            "  @Override Object another() { null }\n" +
            "}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test // https://issues.apache.org/jira/projects/GROOVY/issues/GROOVY-9059
    public void testAbstractCovariance_GROOVY9059() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Face.Java",
            "interface Face<T> {\n" +
            "  <O extends T> O process(O o);\n" +
            "}\n",

            "Impl.groovy",
            "class Impl implements Face<CharSequence> { \n" +
            "  @Override\n" +
            "  public <Chars extends CharSequence> Chars process(Chars chars) { chars }\n" +
            "}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test // https://issues.apache.org/jira/projects/GROOVY/issues/GROOVY-9059
    public void testAbstractCovariance_GROOVY9059a() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Face.Java",
            "interface Face<T> {\n" +
            "  <O extends T> O process(O o);\n" +
            "}\n",

            "Impl.groovy",
            "def impl = new Face<CharSequence>() { \n" +
            "  @Override\n" +
            "  public <Chars extends CharSequence> Chars process(Chars chars) { chars }\n" +
            "}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test // https://issues.apache.org/jira/projects/GROOVY/issues/GROOVY-9059
    public void testAbstractCovariance_GROOVY9059b() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Face.Java",
            "interface Face<T> {\n" +
            "  <O extends T> O process(O o);\n" +
            "}\n",

            "Impl.groovy",
            "def impl = new Face<CharSequence>() { \n" +
            "  @Override @SuppressWarnings('unchecked')\n" +
            "  public CharSequence process(CharSequence chars) { chars }\n" +
            "}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test // https://issues.apache.org/jira/projects/GROOVY/issues/GROOVY-9059
    public void testAbstractCovariance_GROOVY9059c() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Face.Java",
            "interface Face<T> {\n" +
            "  <O extends T> O process(O o);\n" +
            "}\n",

            "Impl.groovy",
            "def impl = new Face<String>() { \n" +
            "  @Override @SuppressWarnings('unchecked')\n" +
            "  public String process(String string) { string }\n" +
            "}\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testExtendingRawJavaType() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "public class Foo extends Supertype {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.print(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/Supertype.java",
            "package p;\n" +
            "class Supertype<T> extends Supertype2 { }",

            "p/Supertype2.java",
            "package p;\n" +
            "class Supertype2<T> { }",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testHalfFinishedGenericsProgram() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "public class Demo {\n" +
            "\n" +
            "List myList;\n" +
            "\n" +
            "           def funkyMethod(Map map) {\n" +
            "               print \"Groovy!\"\n" +
            "       }\n" +
            "   }\n" +
            "\n" +
            "class MyMap<K,V> extends Map {\n" +
            "\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Demo.groovy (at line 3)\n" +
            "\tList myList;\n" +
            "\t^^^^\n" +
            "List is a raw type. References to generic type List<E> should be parameterized\n" +
            "----------\n" +
            "2. WARNING in Demo.groovy (at line 5)\n" +
            "\tdef funkyMethod(Map map) {\n" +
            "\t                ^^^\n" +
            "Map is a raw type. References to generic type Map<K,V> should be parameterized\n" +
            "----------\n" +
            "3. ERROR in Demo.groovy (at line 10)\n" +
            "\tclass MyMap<K,V> extends Map {\n" +
            "\t      ^^^^^\n" +
            "Groovy:You are not allowed to extend the interface \'java.util.Map\', use implements instead.\n" +
            "----------\n" +
            "4. WARNING in Demo.groovy (at line 10)\n" +
            "\tclass MyMap<K,V> extends Map {\n" +
            "\t                         ^^^\n" +
            "Map is a raw type. References to generic type Map<K,V> should be parameterized\n" +
            "----------\n");
    }

    @Test
    public void testHalfFinishedGenericsProgramWithCorrectSuppression() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "class Demo {\n" +
            "  @SuppressWarnings('rawtypes')\n" + // should suppress the warning
            "  List myList\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testHalfFinishedGenericsProgramWithCorrectSuppressionAtTheTypeLevel() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "@SuppressWarnings('rawtypes')\n" + // should suppress the warning
            "class Demo {\n" +
            "  List myList\n" +
            "}",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testHalfFinishedGenericsProgramWithUnnecessarySuppression() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "class Demo {\n" +
            "  @SuppressWarnings('unchecked')\n" + // unnecessary suppression
            "  List<String> myList\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Demo.groovy (at line 2)\n" +
            "\t@SuppressWarnings('unchecked')\n" +
            "\t                  ^^^^^^^^^^^\n" +
            "Unnecessary @SuppressWarnings(\"unchecked\")\n" +
            "----------\n");
    }

    @Test
    public void testHalfFinishedGenericsProgramWithSuppressionValueSpeltWrong() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "public class Demo {\n" +
            "\n" +
            "@SuppressWarnings(\"unchecked2\")\n" + // spelt wrong
            "List<String> myList;\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Demo.groovy (at line 3)\n" +
            "\t@SuppressWarnings(\"unchecked2\")\n" +
            "\t                  ^^^^^^^^^^^^\n" +
            "Unsupported @SuppressWarnings(\"unchecked2\")\n" +
            "----------\n");
    }

    @Test
    public void testHalfFinishedGenericsProgramWithMultipleSuppressionValues() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "class Demo {\n" +
            "  @SuppressWarnings(['rawtypes','cast'])\n" +
            "  List list\n" +
            "}",
        };
        //@formatter:on

        // Eclipse Oxygen (i.e. JDT Core 3.13) added warning for mixed mode
        Version v = Platform.getBundle("org.eclipse.jdt.core").getVersion();
        runNegativeTest(sources, (v.getMajor() == 3 && v.getMinor() < 13) ? "" : "----------\n" +
            "1. ## in Demo.groovy (at line 2)\n" + // '##' could be INFO or WARNING
            "\t@SuppressWarnings(['rawtypes','cast'])\n" +
            "\t                              ^^^^^^\n" +
            "At least one of the problems in category 'cast' is not analysed due to a compiler option being ignored\n" +
            "----------\n");
    }

    @Test
    public void testHalfFinishedGenericsProgramWithMultipleSuppressionValuesWithOneSpeltWrong() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "public class Demo {\n" +
            "\n" +
            "@SuppressWarnings([\"rawtypes\",\"cast2\"])\n" +
            "List myList;\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Demo.groovy (at line 3)\n" +
            "\t@SuppressWarnings([\"rawtypes\",\"cast2\"])\n" +
            "\t                              ^^^^^^^\n" +
            "Unsupported @SuppressWarnings(\"cast2\")\n" +
            "----------\n");
    }

    @Test
    public void testJava7() {
        assumeTrue(isAtLeastJava(JDK7));

        //@formatter:off
        String[] sources = {
            "A.java",
            "import java.util.*;\n" +
            "public class A {\n" +
            "public static void main(String[]argv) {\n" +
            "  List<String> ls = new ArrayList<>();" +
            "  int i = 1_000_000;\n" +
            "  int b = 0b110101;\n" +
            "  try {\n" +
            "    foo();\n" +
            "  } catch (java.io.IOException | IllegalStateException re) {\n" +
            "  }\n" +
            "}\n" +
            "  public static void foo() throws java.io.IOException {}\n" +
            "}",

            "B.groovy",
            "print 'a'\n",
        };
        //@formatter:on

        runWarningFreeTest(sources);
    }

    @Test
    public void testJava7_2() {
        assumeTrue(!isAtLeastJava(JDK7));

        //@formatter:off
        String[] sources = {
            "A.java",
            "import java.util.*;\n" +
            "public class A {\n" +
            "public static void main(String[]argv) {\n" +
            "  List<String> ls = new ArrayList<>();" +
            "  int i = 1_000_000;\n" +
            "  int b = 0b110101;\n" +
            "}\n" +
            "  public static void foo() throws java.io.IOException {}\n" +
            "}",

            "B.groovy",
            "print 'a'\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.java (at line 4)\n" +
            "\tList<String> ls = new ArrayList<>();  int i = 1_000_000;\n" +
            "\t                      ^^^^^^^^^\n" +
            "\'<>\' operator is not allowed for source level below 1.7\n" +
            "----------\n" +
            "2. ERROR in A.java (at line 4)\n" +
            "\tList<String> ls = new ArrayList<>();  int i = 1_000_000;\n" +
            "\t                                              ^^^^^^^^^\n" +
            "Underscores can only be used with source level 1.7 or greater\n" +
            "----------\n");
    }

    @Test // https://jira.spring.io/browse/STS-3930
    public void testSts3930() {
        //@formatter:off
        String[] sources = {
            "demo/GroovyDemo.groovy",
            "package demo\n" +
            "class GroovyDemo {\n" +
            "  static <T> List someMethod(Class<T> factoryClass, ClassLoader classLoader = this.classLoader) {\n" +
            "  }\n" +
            "}\n",

            "demo/JavaDemo.java",
            "package demo;\n" +
            "public class JavaDemo {\n" +
            "  public static void staticMethod() {\n" +
            "    GroovyDemo.someMethod(JavaDemo.class);\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test
    public void testWildcards1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(String.class)\n" +
            "public class X {\n" +
            "  public void foo(String s = \"abc\") {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<? extends Number> value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" +
            "----------\n");
    }

    @Test
    public void testWildcards2() {
        //@formatter:off
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "@Anno(String.class)\n" +
            "public class X {\n" +
            "  public void foo(String s) {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<? extends Number> value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.java (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" +
            "----------\n");
    }

    @Test
    public void testWildcards3() {
        //@formatter:off
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "@Anno(String.class)\n" +
            "public class X {\n" +
            "  public void foo(String s) {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<? extends Number> value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.java (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" +
            "----------\n");
    }

    @Test
    public void testWildcards4() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(String.class)\n" +
            "public class X {\n" +
            "  public void foo(String s) {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<? extends Number> value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? extends Number>\n" +
            "----------\n");
    }

    @Test
    public void testWildcards5() {
        //@formatter:off
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "@Anno(Integer.class)\n" +
            "public class X {\n" +
            "  public void foo(String s) {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/Anno.groovy",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<? extends Number> value(); }\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testWildcards6() {
        //@formatter:off
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "@Anno(Number.class)\n" +
            "public class X {\n" +
            "  public void foo(String s) {}\n" +
            "  public static void main(String[]argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<? super Integer> value(); }\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testWildcards7() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "@Anno(String.class)\n" +
            "public class X {\n" +
            "  public static void main(String[]argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/Anno.java",
            "package p;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@interface Anno { Class<? super Integer> value(); }\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\t@Anno(String.class)\n" +
            "\t      ^^^^^^^^^^^^^\n" +
            "Type mismatch: cannot convert from Class<String> to Class<? super Integer>\n" +
            "----------\n");
    }

    @Test
    public void testUpperBounds1() {
        //@formatter:off
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public static void main(String[] args) {\n" +
            "    new J<Integer>().run();\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "}\n",

            "p/J.java",
            "package p;\n" +
            "public class J<T extends Number & I> {\n" +
            "  Class<T> run() { return null; }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.java (at line 4)\n" +
            "\tnew J<Integer>().run();\n" +
            "\t      ^^^^^^^\n" +
            "Bound mismatch: The type Integer is not a valid substitute for the bounded parameter <T extends Number & I> of the type J<T>\n" +
            "----------\n");
    }

    @Test
    public void testUpperBounds2() {
        //@formatter:off
        String[] sources = {
            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public static void main(String[] args) {\n" +
            "    new G<Integer>().run();\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "}\n",

            "p/G.groovy",
            "package p\n" +
            "class G<T extends Number & I> {\n" +
            "  Class<T> run() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.java (at line 4)\n" +
            "\tnew G<Integer>().run();\n" +
            "\t      ^^^^^^^\n" +
            "Bound mismatch: The type Integer is not a valid substitute for the bounded parameter <T extends Number & I> of the type G<T>\n" +
            "----------\n");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8990
    public void testUpperBounds3() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p\n" +
            "class X {\n" +
            "  static main(args) {\n" +
            "    new G<Integer>().run()\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "}\n",

            "p/G.groovy",
            "package p\n" +
            "class G<T extends Number & I> {\n" +
            "  Class<T> run() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 4)\n" +
            "\tnew G<Integer>().run()\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:The type Integer is not a valid substitute for the bounded parameter <T extends java.lang.Number & p.I>\n" +
            "----------\n");
    }

    @Test
    public void testUpperBounds4() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p\n" +
            "class W implements I {}\n" +
            "class X extends G<W> {\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "}\n",

            "p/G.groovy",
            "package p\n" +
            "class G<T extends Number & I> {\n" +
            "  Class<T> run() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 3)\n" +
            "\tclass X extends G<W> {\n" +
            "\t                  ^\n" +
            "Groovy:The type W is not a valid substitute for the bounded parameter <T extends java.lang.Number & p.I>\n" +
            "----------\n");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8990
    public void testUpperBounds5() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p\n" +
            "class X extends G<Integer> {\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "}\n",

            "p/G.groovy",
            "package p\n" +
            "class G<T extends Number & I> {\n" +
            "  Class<T> run() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\tclass X extends G<Integer> {\n" +
            "\t                  ^^^^^^^\n" +
            "Groovy:The type Integer is not a valid substitute for the bounded parameter <T extends java.lang.Number & p.I>\n" +
            "----------\n");
    }
}
