/*
 * Copyright 2009-2016 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration
import org.eclipse.core.runtime.Platform
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest
import org.eclipse.jdt.core.tests.util.GroovyUtils
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions

final class GenericsTests extends AbstractGroovyRegressionTest {

    static junit.framework.Test suite() {
        buildMinimalComplianceTestSuite(GenericsTests, F_1_6)
    }

    GenericsTests(String name) {
        super(name)
    }

    void testGenericParam() {
        String[] sources = [
            'A.groovy', '''
            class Foo {
              public void m(List<String> ls) {}
            }'''
        ]

        runConformTest(sources)
    }

    void testCallingGenericConstructors() {
        String[] sources = [
            'p/B.groovy', '''
            package p;
            public class B extends A {
              public static void main(String[] argv) {
                new A(35);
                System.out.println('success');
              }
            }
            ''',

            'p/A.java', '''
            package p;
            public class A {
              public <T> A(T t) {}
            }
            '''
        ]

        runConformTest(sources, 'success')
    }

    void testGenericsPositions_GRE267_1() {
        String[] sources = [
            'X.groovy', '''\
            class X {
              Set<?> setone;
              Set<? extends Serializable> settwo;
              Set<? super Number> setthree;
              public static void main(String[]argv){ print 'y';}
            }'''.stripIndent(),

            // this Java class is for comparison - breakpoint on building type bindings and you can check the decls
            'Y.java', '''\
            import java.util.*;
            class Y {
              Set<?> a;
              Set<? extends java.io.Serializable> b;
              Set<? super Number> c;
            }'''.stripIndent()
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>14)Set<(16>16)?>', stringify(grabField(decl, 'setone').type))
        assertEquals('(29>31)Set<(33>54)? extends (43>54)Serializable>', stringify(grabField(decl, 'settwo').type))
        assertEquals('(67>69)Set<(71>84)? super (79>84)Number>', stringify(grabField(decl,'setthree').type))
    }

    void testGenericsPositions_GRE267_2() {
        String[] sources = [
            'X.groovy', '''\
            class X {
              Set<?> setone;
              Set<? extends java.io.Serializable> settwo;
              Set<? super java.lang.Number> setthree;
              public static void main(String[]argv){ print 'y';}
            }'''.stripIndent()
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>14)Set<(16>16)?>', stringify(grabField(decl,'setone').type))
        assertEquals('(29>31)Set<(33>62)? extends (43>62)(43>46)java.(48>49)io.(51>62)Serializable>', stringify(grabField(decl,'settwo').type))
        assertEquals('(75>77)Set<(79>102)? super (87>102)(87>90)java.(92>95)lang.(97>102)Number>', stringify(grabField(decl,'setthree').type))
    }

    void testGenericsPositions_GRE267_3() {
        String[] sources = [
            'X.groovy', '''\
            class X {
              Set<?> setone;
              Set<String[]> settwo;
              Set<String[][]> setthree;
              Set<java.lang.Number[][][]> setfour;
              public static void main(String[]argv){ print 'y' }
            }'''.stripIndent(),

            'Y.java', '''\
            import java.util.*;
            class Y {
              Set<String[]> a;
              Set<String[][]> b;
              Set<java.lang.Number[][][]> c;
            }'''.stripIndent()
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>14)Set<(16>16)?>', stringify(grabField(decl, 'setone').type))
        assertEquals('(29>31)Set<(33>40 ose:38)String[]>', stringify(grabField(decl, 'settwo').type))
        assertEquals('(53>55)Set<(57>66 ose:62)String[][]>', stringify(grabField(decl, 'setthree').type))
        //assertEquals('(81>83)Set<(85>106)(85>88)java.(90>93)lang.(95>100)Number[][][]>', stringify(grabField(decl, 'setfour')))
    }

    void testGenericsPositions_4_GRE267() {
        String[] sources = [
            'X.groovy', '''\
            class X {
              java.util.Set<?> setone;
              java.util.Set<? extends Serializable> settwo;
              java.util.Set<? super Number> setthree;
              public static void main(String[]argv){ print 'y';}
            }'''.stripIndent()
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>24)(12>15)java.(17>20)util.(22>24)Set<(26>26)?>', stringify(grabField(decl, 'setone').type))
        assertEquals('(39>51)(39>42)java.(44>47)util.(49>51)Set<(53>74)? extends (63>74)Serializable>', stringify(grabField(decl, 'settwo').type))
        assertEquals('(87>99)(87>90)java.(92>95)util.(97>99)Set<(101>114)? super (109>114)Number>', stringify(grabField(decl, 'setthree').type))
    }

    void testGenericsPositions_5_GRE267() {
        String[] sources = [
            'X.groovy', '''\
            class X {
              java.util.Set<?> setone;
              java.util.Set<? extends java.io.Serializable> settwo;
              java.util.Set<? super java.lang.Number> setthree;
              public static void main(String[]argv){ print 'y';}
            }
            '''.stripIndent()
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>24)(12>15)java.(17>20)util.(22>24)Set<(26>26)?>', stringify(grabField(decl, 'setone').type))
        assertEquals('(39>51)(39>42)java.(44>47)util.(49>51)Set<(53>82)? extends (63>82)(63>66)java.(68>69)io.(71>82)Serializable>', stringify(grabField(decl, 'settwo').type))
        assertEquals('(95>107)(95>98)java.(100>103)util.(105>107)Set<(109>132)? super (117>132)(117>120)java.(122>125)lang.(127>132)Number>', stringify(grabField(decl, 'setthree').type))
    }

    void _testGenericsPositions_6_GRE267() {
        // FIXASC check tests after porting to recent 1.7 compiler
        // Multiple generified components in a reference
        String[] sources = [
            'X.groovy', '''\
            class X {
              One<String,Integer>.Two<Boolean> whoa;
              java.util.Set<? extends java.io.Serializable> settwo;
              java.util.Set<? super java.lang.Number> setthree;
              public static void main(String[]argv){ print 'y';}
            }
            '''.stripIndent(),

            'One.java', '''
            public class One<A,B> {
                 class Two<C> {
                 }
               }
            '''
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>14)Set<(16>16)?>', stringify(grabField(decl, 'one').type))
        assertEquals('(29>31)Set<(33>33)? extends (43>61)(43>47)java.(48>50)io.(51>61)Serializable>', stringify(grabField(decl, 'settwo').type))
        assertEquals('(67>69)Set<(71>71)? super (79>84)Number>', stringify(grabField(decl, 'setthree').type))
    }

    void testGenericsPositions_7_GRE267() {
        String[] sources = [
            'X.groovy', '''\
            class X {
              java.util.Set<?> setone;
              java.util.Set<String[]> settwo;
              java.util.Set<java.lang.Number[][][]> setthree;
              public static void main(String[]argv){ print 'y';}
            }
            '''.stripIndent()
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>24)(12>15)java.(17>20)util.(22>24)Set<(26>26)?>', stringify(grabField(decl, 'setone').type))
        assertEquals('(39>51)(39>42)java.(44>47)util.(49>51)Set<(53>60 ose:58)String[]>', stringify(grabField(decl, 'settwo').type))
        assertEquals('(73>85)(73>76)java.(78>81)util.(83>85)Set<(87>108)(87>90)java.(92>95)lang.(97>102)Number[][][]>', stringify(grabField(decl, 'setthree').type))
    }

    void testGenericsPositions_8_GRE267() {
        String[] sources = [
            'X.groovy', '''\
            class X {
              Set<Map.Entry<String,List<String>>> foo;
              public static void main(String[]argv){ print 'y' }
            }
            '''.stripIndent()
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>14)Set<(16>24)(16>18)Map.(20>24)Entry<(26>31)String(33>36)List<(38>43)String>>>', stringify(grabField(decl, 'foo').type))
    }

    void testGenericsPositions_9_GRE267() {
        String[] sources = [
            'X.groovy', '''\
            class X {
              Map.Entry<String,List<String>> foo;
              public static void main(String[]argv){ print 'y';}
            }
            '''.stripIndent()
        ]

        runConformTest(sources, 'y')

        GroovyCompilationUnitDeclaration decl = getCUDeclFor('X.groovy')
        assertEquals('(12>20)(12>14)Map.(16>20)Entry<(22>27)String(29>32)List<(34>39)String>>', stringify(grabField(decl, 'foo').type))
    }

    void testGenericsAndGroovyJava_GRE278_1() {
        String[] sources = [
            'p/Field.java', '''
            package test;
            public interface Field<T> extends Comparable<T> {
                public String getFieldTypeName();
                public String getName();
                public T getValue();
                public void setValue(T o);
            }
            ''',

            'p/Structure.java', '''
            package test;
            import java.util.Map;
            import java.nio.ByteBuffer;
            public interface Structure extends Map<String, Field<?>> {
               public void reset();
               public void setup(ByteBuffer clientBuff);
            }
            ''',

            'p/StructureBase.groovy', '''\
            package test;
            import java.nio.ByteBuffer;
            @SuppressWarnings('rawtypes')
            public class StructureBase implements Structure {
               protected final Structure str = null;
               StructureBase(Structure struct){
                   this.str = struct;
               }
               public void clear() {
                   str.clear()
               }
               public boolean containsKey(Object arg0) {
                   return str.containsKey(arg0);
               }
               public boolean containsValue(Object arg0) {
                   return str.containsValue(arg0);
               }
               public Set<java.util.Map.Entry<String, Field<?>>> entrySet() {
                   return Collections.unmodifiableSet(str.entrySet());
               }
               public Field<?> get(Object arg0) {
                   return str.get(arg0);
               }
               public boolean isEmpty() {
                   return str.isEmpty();
               }
               public Set<String> keySet() {
                   return Collections.unmodifiableSet(str.keySet());
               }
               public Field<?> put(String arg0, Field<?> arg1) {
                   return str.put(arg0, arg1);
               }
               //public Object put(Object key, Object value) {
               //    return str.put(key, value)
               //}
               public void putAll(Map<? extends String, ? extends Field<?>> arg0) {
                   str.putAll(arg0);
               }
               public Field<?> remove(Object key) {
                   return str.remove(key);
               }
               public int size() {
                   return str.size();
               }
               public Collection<Field<?>> values() {
                   return Collections.unmodifiableCollection(str.values());
               }
               //public void reset(){
               //    str.reset();
               //}
               //public void setup(ByteBuffer buff) {
               //    str.setup(buff);
               //}
            }
            ''',

            'p/StructureBaseTest.groovy', '''
            package test;
            public class StructureBaseTest {
               public static void main(String[] args) {
                    Structure str = new StructureBase(new TestStructure());
                    str.put('test', new TestField());
                       def content = str.get('test');
                       if (!TestField.FIELD_NAME.equals(str.get('test').name)) {
                           System.out.println('Failed');
                       }
               }
            }
            ''',

            'p/TestField.java', '''
            package test;
            public class TestField implements Field<String> {
               public static final String FIELD_NAME = "Test";
               private StringBuilder buffer = new StringBuilder();
               private String value = null;
               public String getFieldTypeName() {
                   return String.class.getSimpleName();
               }
               public String getName() {
                   return FIELD_NAME;
               }
               public String getValue() {
                   if (null == value)
                       value = buffer.toString();
                   return value;
               }
               public void setValue(String o) {
                   value = o;
                   buffer.replace(0, buffer.length(), o);
               }
               public int compareTo(String arg0) {
                   return getValue().compareTo(arg0);
               }
            }
            ''',

            'p/TestStructure.java', '''
            package test;
            import java.nio.ByteBuffer;
            import java.util.HashMap;
            import java.util.Map;
            @SuppressWarnings("serial")
            public class TestStructure extends HashMap<String, Field<?>> implements Structure {
               public void reset() {
                   // TODO Auto-generated method stub
               }
               public void setup(ByteBuffer clientBuff) {
                   // TODO Auto-generated method stub
               }
               public Field<?> put(String arg0, Field<?> arg1) {
                   return super.put(arg0, arg1);
            }
               public void putAll(Map<? extends String, ? extends Field<?>> arg0) {
                   super.putAll(arg0);
               }
            }
            '''
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\StructureBase.groovy (at line 4)\n" +
            "\tpublic class StructureBase implements Structure {\n" +
            "\t             ^^^^^^^^^^^^^\n" +
            "Groovy:Can\'t have an abstract method in a non-abstract class. The class \'test.StructureBase\' must be declared abstract or the method \'void setup(java.nio.ByteBuffer)\' must be implemented.\n" +
            "----------\n" +
            "2. ERROR in p\\StructureBase.groovy (at line 4)\n" +
            "\tpublic class StructureBase implements Structure {\n" +
            "\t             ^^^^^^^^^^^^^\n" +
            "Groovy:Can\'t have an abstract method in a non-abstract class. The class \'test.StructureBase\' must be declared abstract or the method \'void reset()\' must be implemented.\n" +
            "----------\n")
    }

    void testGenericsAndGroovyJava_GRE278_2() {
        String[] sources = [
            'Main.java', '''
            public class Main {
              public static void main(String[]argv) {
                test.StructureBaseTest.main(argv);
              }
            }
            ''',

            'p/Field.java', '''
            package test;
            public interface Field<T> extends Comparable<T> {
                public String getFieldTypeName();
                public String getName();
                public T getValue();
                public void setValue(T o);
            }
            ''',

            'p/Structure.java', '''
            package test;
            import java.util.Map;
            import java.nio.ByteBuffer;
            public interface Structure extends Map<String, Field<?>> {
               public void reset();
               public void setup(ByteBuffer clientBuff);
            }
            ''',

            'p/StructureBase.groovy', '''
            package test;
            import java.nio.ByteBuffer;
            @SuppressWarnings('unchecked')
            public class StructureBase implements Structure {
               protected final Structure str = null;
               StructureBase(Structure struct){
                   this.str = struct;
               }
               public void clear() {
                   str.clear()
               }
               public boolean containsKey(Object arg0) {
                   return str.containsKey(arg0);
               }
               public boolean containsValue(Object arg0) {
                   return str.containsValue(arg0);
               }
               public Set<java.util.Map.Entry<String, Field<?>>> entrySet() {
                   return Collections.unmodifiableSet(str.entrySet());
               }
               public Field<?> get(Object arg0) {
                   return str.get(arg0);
               }
               public boolean isEmpty() {
                   return str.isEmpty();
               }
               public Set<String> keySet() {
                   return Collections.unmodifiableSet(str.keySet());
               }
               public Field<?> put(String arg0, Field<?> arg1) {
                   return str.put(arg0, arg1);
               }
               public void putAll(Map<? extends String, ? extends Field<?>> arg0) {
                   str.putAll(arg0);
               }
               public Field<?> remove(Object key) {
                   return str.remove(key);
               }
               public int size() {
                   return str.size();
               }
               public Collection<Field<?>> values() {
                   return Collections.unmodifiableCollection(str.values());
               }
               public void reset(){
                   str.reset();
               }
               public void setup(ByteBuffer buff) {
                   str.setup(buff);
               }
            }
            ''',

            'p/StructureBaseTest.groovy', '''
            package test;
            public class StructureBaseTest {
               public static void main(String[] args) {
                    Structure str = new StructureBase(new TestStructure());
                    str.put('test', new TestField());
                       def content = str.get('test');
                       if (!TestField.FIELD_NAME.equals(str.get('test').name)) {
                           println('Failed');
                       }
               }
            }
            ''',

            'p/TestField.java', '''
            package test;
            public class TestField implements Field<String> {
               public static final String FIELD_NAME = "Test";
               private StringBuilder buffer = new StringBuilder();
               private String value = null;
               public String getFieldTypeName() {
                   return String.class.getSimpleName();
               }
               public String getName() {
                   return FIELD_NAME;
               }
               public String getValue() {
                   if (null == value)
                       value = buffer.toString();
                   return value;
               }
               public void setValue(String o) {
                   value = o;
                   buffer.replace(0, buffer.length(), o);
               }
               public int compareTo(String arg0) {
                   return getValue().compareTo(arg0);
               }
            }
            ''',

            'p/TestStructure.java', '''
            package test;
            import java.nio.ByteBuffer;
            import java.util.HashMap;
            import java.util.Map;
            @SuppressWarnings("serial")
            public class TestStructure extends HashMap<String, Field<?>> implements Structure {
               public void reset() {
                   // TODO Auto-generated method stub
               }
               public void setup(ByteBuffer clientBuff) {
                   // TODO Auto-generated method stub
               }
               public Field<?> put(String arg0, Field<?> arg1) {
                   return super.put(arg0, arg1);
            }
               public void putAll(Map<? extends String, ? extends Field<?>> arg0) {
                   super.putAll(arg0);
               }
            }
            '''
        ]

        runConformTest(sources)
    }

    // when GROOVY-5861 is fixed we can enable these 2 tests:
    void _testGenericsAndGroovyJava_GRE278_3() {
        String[] sources = [
            'p/Field.java', '''
            package test;
            public interface Field<T extends java.io.Serializable> extends Comparable<T> {
            }
            ''',

            'p/StructureBase.groovy', '''
            package test;
            public class StructureBase {
               public Field<?> get(Object arg0) {
                   return str.get(arg0);
               }
            }
            '''
        ]

        runConformTest(sources, 'Success')
    }

    void _testGenericsAndGroovyJava_GRE278_3a() {
        String[] sources = [
            'Main.java', '''
            public class Main {
              public static void main(String[]argv) {
                test.StructureBaseTest.main(argv);
              }
            }
            ''',

            'p/Field.java', '''
            package test;
            public interface Field<T extends java.io.Serializable> extends Comparable<T> {
                public String getFieldTypeName();
                public String getName();
                public T getValue();
                public void setValue(T o);
            }
            ''',

            'p/Structure.java', '''
            package test;
            import java.util.Map;
            import java.nio.ByteBuffer;
            public interface Structure extends Map<String, Field<?>> {
               public void reset();
               public void setup(ByteBuffer clientBuff);
            }
            ''',

            'p/StructureBase.groovy', '''
            package test;
            import java.nio.ByteBuffer;
            @SuppressWarnings("unchecked")
            public class StructureBase implements Structure {
               protected final Structure str = null;
               StructureBase(Structure struct){
                   this.str = struct;
               }
               public void clear() {
                   str.clear()
               }
               public boolean containsKey(Object arg0) {
                   return str.containsKey(arg0);\n"+
               }
               public boolean containsValue(Object arg0) {
                   return str.containsValue(arg0);
               }
               public Set<java.util.Map.Entry<String, Field<?>>> entrySet() {
                   return Collections.unmodifiableSet(str.entrySet());
               }
               public Field<?> get(Object arg0) {
                   return str.get(arg0);
               }
               public boolean isEmpty() {
                   return str.isEmpty();
               }
               public Set<String> keySet() {
                   return Collections.unmodifiableSet(str.keySet());
               }
               public Field<?> put(String arg0, Field<?> arg1) {
                   return str.put(arg0, arg1);
               }
               public Object put(Object key, Object value) {
                   return str.put(key, value)
               }
               public void putAll(Map<? extends String, ? extends Field<?>> arg0) {
                   str.putAll(arg0);
               }
               public Field<?> remove(Object key) {
                   return str.remove(key);
               }
               public int size() {
                   return str.size();
               }
               public Collection<Field<?>> values() {
                   return Collections.unmodifiableCollection(str.values());
               }
               public void reset(){
                   str.reset();
               }
               public void setup(ByteBuffer buff) {
                   str.setup(buff);
               }
            }
            ''',

            'p/StructureBaseTest.groovy', '''
            package test;
            public class StructureBaseTest {
               public static void main(String[] args) {
                    Structure str = new StructureBase(new TestStructure());
                    str.put('test', new TestField());
                       def content = str.get('test');
                       if (!TestField.FIELD_NAME.equals(str.get('test').name)) {
                           System.out.println(\"Failed\");
                       } else {
                           System.out.println(\"Success\");
                       }
               }
            }
            ''',

            'p/TestField.java', '''
            package test;
            public class TestField implements Field<String> {
               public static final String FIELD_NAME = \"Test\";
               private StringBuilder buffer = new StringBuilder();
               private String value = null;
               public String getFieldTypeName() {
                   return String.class.getSimpleName();
               }
               public String getName() {
                   return FIELD_NAME;
               }
               public String getValue() {
                   if (null == value)
                       value = buffer.toString();
                   return value;
               }
               public void setValue(String o) {
                   value = o;
                   buffer.replace(0, buffer.length(), o);
               }
               public int compareTo(String arg0) {
                   return getValue().compareTo(arg0);
               }
            }
            ''',

            'p/TestStructure.java', '''
            package test;
            import java.nio.ByteBuffer;
            import java.util.HashMap;
            import java.util.Map;
            @SuppressWarnings("serial")
            public class TestStructure extends HashMap<String, Field<?>> implements Structure {
               public void reset() {
               }
               public void setup(ByteBuffer clientBuff) {
               }
               public Field<?> put(String arg0, Field<?> arg1) {
                   return super.put(arg0, arg1);
               }
               public void putAll(Map<? extends String, ? extends Field<?>> arg0) {
                   super.putAll(arg0);
               }
            }
            '''
        ]

        runConformTest(sources, "Success")
    }

    void testGenericsAndGroovyJava_GRE278_4() {
        String[] sources = [
            "Main.groovy",
            "public class Main {\n"+
            "  public static void main(String[]argv) {\n"+
            "    def content = new StructureBase().get('test');\n"+
            "    print 'test';\n"+
            "  }\n"+
            "}\n",

            "MyMap.java",
            "public interface MyMap<A,B> {" +
            "   B get(Object key);\n" +
            " }\n",

            "Structure.java",
            "public interface Structure extends MyMap<String, Integer> {\n"+
            "}\n",

            "StructureBase.groovy",
            "public class StructureBase implements Structure {\n"+
            "   public Integer get(Object key) {\n"+
            "       return null;\n"+
            "   }\n"+
            "}\n"
        ]

        runConformTest(sources, 'test')
    }

    void testGenericFields_JcallingG() {
        String[] sources = [
            "p/Code.java",
            "package p;\n"+
            "public class Code extends G<String> {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Code c = new Code();\n"+
            "    c.setField(\"success\");\n"+
            "    System.out.print(c.getField());\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "class G<T> { T field; }"
        ]

        runConformTest(sources, "success")
    }

    void testGenericFields_GcallingJ() {
        String[] sources = [
            "p/Code.groovy",
            "package p;\n"+
            "public class Code extends G<String> {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Code c = new Code();\n"+
            "    c.field=\"success\";\n"+
            "    System.out.print(c.field);\n"+
            "  }\n"+
            "}\n",

            "p/G.java",
            "package p;\n"+
            "class G<T> { public T field; }" // TODO why must this be public for the groovy code to see it?  If non public should it be instead defined as a property on the JDTClassNode rather than a field?
        ]

        runConformTest(sources, "success")
    }

    void testGroovyPropertyAccessorsGenerics() {
        String[] sources = [
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    for (Integer s: o.getB()) {\n"+
            "      System.out.print(s);\n"+
            "    }\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  List<Integer> b = [1,2,3]\n"+
            "}\n"
        ]

        runConformTest(sources, "123")
    }

    void testGroovyGenerics() {
        // GroovyBug: this surfaced the problem that the generics declarations are checked before resolution is complete -
        // had to change CompilationUnit so that resolve and checkGenerics are different stages in the SEMANTIC_ANALYSIS phase
        // otherwise it depends on whether the super type is resolved before the subtype has its generic decl checked
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/A.groovy",
            "package p;\n" +
            "public class A<T> {}\n"
        ]

        runConformTest(sources, "success")
    }

    public void testGreclipse1563() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Inter.java",
            "package ab;\n"+
            "public interface Inter {\n"+
            "    public Number getItem(Object itemId);\n"+
            "}\n",
            "Clazz.java",
            "package ab;\n"+
            "public abstract class Clazz<ITEM extends Number> implements Inter {\n"+
            "   public ITEM getItem(Object itemId) {\n"+
            "       return null;\n"+
            "   }\n"+
            "}\n",
            "GClazz.groovy",
            "package ab;\n"+
            "class GClazz extends Clazz<Number> {}"
        ]

        runConformTest(sources, "")
    }

    public void testGreclipse1563_2() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        String[] sources = [
            "Clazz.java",
            "package ab;\n"+
            "public abstract class Clazz<ITEM extends MyItem> implements Inter {\n"+
            "   public ITEM getItem(Object itemId) {\n"+
            "       return null;\n"+
            "   }\n"+
            "}\n",
            "Inter.java",
            "package ab;\n"+
            "public interface Inter {\n"+
            "    public MyItem getItem(Object itemId);\n"+
            "}\n",
            "MyItem.java",
            "package ab;\n"+
            "public class MyItem {}\n",
            "GClazz.groovy",
            "package ab;\n"+
            "class GClazz extends Clazz<MyItem> {}"
        ]

        runConformTest(sources, "")
    }

    void testExtendingGenerics_JavaExtendsGroovy() {
        // WMTW: GroovyCompilationUnit builds a correct representation of the groovy type A
        String[] sources = [
            "p/B.java",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/A.groovy",
            "package p;\n" +
            "public class A<T> {}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava() {
        // WMTW: JDT ClassNode builds a correct groovy representation of the A type
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T> {public void set(T t) { }}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava2() {
        // test when the upper bound is not just 'Object'
        // WMTW: notice I and Impl are classes and not interfaces, because right now only the superclass stuff is set up correctly for nodes.
        // In order for no error to occur we have to override getUnresolvedSuperClass() in our JDTClassNode so that the code in
        // GenericsVisitor.checkGenericsUsage() correctly determines Impl isDerivedFrom I
        // the rule seems to be coming out that there is no redirection from JDTClassNode, they are absolute
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<Impl> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.java",
            "package p; class I {}",

            "p/Impl.java",
            "package p; class Impl extends I {}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends I> {}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava3a() {
        // TODO create more variations around mixing types up (including generics bounds)
        // variation of above - the interface type is a java file and not a groovy file
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<Impl> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.java",
            "package p; interface I {}", // class->interface

            "p/Impl.java",
            "package p; class Impl implements I {}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends I> {}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava2b() {
        // WMTW: JDTClassNode correctly initializes interfaces based on binding interfaces
        // It needs the interface set for Impl to be defined correctly so that groovy can determine Impl extends I
        // test when the upper bound is not just 'Object'
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<Impl> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy", // java->groovy
            "package p; interface I {}", // class->interface

            "p/Impl.groovy",
            "package p; class Impl implements I {}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends I> {}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava3_ERROR() {
        // GRECLIPSE-430: the declaration of B violates the 'T extends I' specification of A
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.java",
            "package p; interface I {}",

            "p/Impl.java",
            "package p; class Impl implements I {}",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends I> {}\n"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\B.groovy (at line 2)\n" +
            "\tpublic class B extends A<String> {\n" +
            "\t                       ^\n" +
            "Groovy:The type String is not a valid substitute for the bounded parameter <T extends p.I>\n" +
            "----------\n"
        )
    }

    void testExtendingGenerics_GroovyExtendsJava4() {
        // see comments in worklog on 8-Jun-09
        // note this also tests that qualified references are converted correctly (generics info intact)
        if (isJRELevel(AbstractCompilerTest.F_1_8)) return

        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends java.util.ArrayList<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    B b = new B();\n"+
            "    b.add(\"abc\");\n"+
            "    print(b.get(0));\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "  void print(String msg) { print msg; }\n"+
            "}\n"
        ]

        runConformTest(sources, "abcsuccess")
    }

    void testExtendingGenerics_GroovyExtendsJava5() {
        if (isJRELevel(AbstractCompilerTest.F_1_8)) return

        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends java.util.ArrayList<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n"
        ]

        runConformTest(sources,"success")
    }

    void testExtendingGenerics_GroovyExtendsJava5a() {
        if (isJRELevel(AbstractCompilerTest.F_1_8)) return

        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends ArrayList<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava6() {
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T> {public void set(T t) { }}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava7() {
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends q.A<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B().set(\"abc\");\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "q/A.java",
            "package q;\n" +
            "public class A<T> {public void set(T t) { }}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava8() {
        // arrays
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<int[]> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B().foo([1,2,3]);\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends Object> {\n"+
            "  public void foo(T t) {}\n"+
            "}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava9() {
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends C {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B().foo([1,2,3]);\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/C.java",
            "package p;\n"+
            "public class C extends A<int[]> {}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends Object> {\n"+
            "  public void foo(T t) {}\n"+
            "}\n"
        ]

        runConformTest(sources, "success")
    }

    void testExtendingGenerics_GroovyExtendsJava10() {
        String[] sources = [
            "p/B.groovy",
            "package p;\n" +
            "public class B extends C<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B().foo([1,2,3],\"hello\");\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/C.java",
            "package p;\n"+
            "public class C<Q> extends A<int[],Q> {}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T extends Object,R> {\n"+
            "  public void foo(T t, R r) {}\n"+
            "}\n"
        ]

        runConformTest(sources, "success")
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
    void _testExtendingGenerics_GroovyExtendsJava11() {
        String[] sources = [
            'Main.groovy', '''\
            @groovy.transform.CompileStatic class Main {
              public static void main(String[] args) {
                def data = new MIData('V', 1, 'B')
                print 'no error'
              }
            }'''.stripIndent(),

            'MultiIndexed.java', '''\
            public interface MultiIndexed<PK, SK> {
              PK getPrimaryKey();
              SK[] getSecondaryKeys();
            }'''.stripIndent(),

            'MIData.groovy', '''\
            class MIData implements MultiIndexed<Integer, String> {
              final String value
              final Integer primaryKey
              final String[] secondaryKeys

              MIData(String val, Integer pk, String... sk) {
                this.value = val
                this.primaryKey = pk
                this.secondaryKeys = sk
              }
            }'''.stripIndent()
        ]

        runConformTest(sources, 'no error')
    }

    void testImplementingInterface_JavaExtendingGroovyGenericType() {
        String[] sources = [
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n"+
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public List m() { return null;}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  List m();\n"+
            "}\n"
        ]

        runConformTest(sources, "success")
    }

    void testImplementingInterface_JavaGenericsIncorrectlyExtendingGroovyGenerics() {
        String[] sources = [
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n"+
            "public class C extends groovy.lang.GroovyObjectSupport implements I<String> {\n"+
            "  public List<String> m() { return null;}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I<T extends Number> {\n" +
            "  List<T> m();\n"+
            "}\n"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 3)\n" +
            "\tpublic class C extends groovy.lang.GroovyObjectSupport implements I<String> {\n" +
            "\t                                                                    ^^^^^^\n" +
            "Bound mismatch: The type String is not a valid substitute for the bounded parameter <T extends Number> of the type I<T>\n" +
            "----------\n")
    }

    void testImplementingInterface_GroovyGenericsIncorrectlyExtendingJavaGenerics() {
        String[] sources = [
            "p/C.groovy",
            "package p;\n" +
            "public class C implements Iii<String> {\n"+
            "  public List<String> m() { return null;}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "import java.util.List;\n"+
            "public interface Iii<T extends Number> {\n" +
            "  List<T> m();\n"+
            "}\n"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.groovy (at line 2)\n" +
            "\tpublic class C implements Iii<String> {\n" +
            "\t                          ^^^^^^^^^^^^\n" +
            "Groovy:The type String is not a valid substitute for the bounded parameter <T extends java.lang.Number>\n" +
            "----------\n")
    }

    void testReferencingFieldsGenerics_JreferingToG() {
        String[] sources = [
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    OtherClass oClass = new OtherClass();\n"+
            "    for (String message: oClass.messages) {\n"+
            "      System.out.print(message);\n"+
            "    }\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n"+
            "public class OtherClass {\n" +
            "  public List<String> messages = new ArrayList<String>();\n"+ // auto imports of java.util
            "  public OtherClass() {\n"+
            "    messages.add(\"hello\");\n"+
            "    messages.add(\" \");\n"+
            "    messages.add(\"world\");\n"+
            "    messages.add(\"\\n\");\n"+
            "  }\n"+
            "}\n"
        ]

        runConformTest(sources, "hello world")
    }

    void testReferencingFieldsGenerics_GreferingToJ() {
        String[] sources = [
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    OtherClass oClass = new OtherClass();\n"+
            "    for (String message: oClass.messages) {\n"+
            "      System.out.print(message);\n"+
            "    }\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.java",
            "package p;\n"+
            "import java.util.*;\n"+
            "public class OtherClass {\n" +
            "  public List<String> messages = new ArrayList<String>();\n"+ // auto imports of java.util
            "  public OtherClass() {\n"+
            "    messages.add(\"hello\");\n"+
            "    messages.add(\" \");\n"+
            "    messages.add(\"world\");\n"+
            "    messages.add(\"\\n\");\n"+
            "  }\n"+
            "}\n"
        ]

        runConformTest(sources, "hello world")
    }

    void testHalfFinishedGenericsProgram() {
        String[] sources = [
            "Demo.groovy",
            "public class Demo {\n"+
            "\n"+
            "List myList;\n"+
            "\n"+
            "           def funkyMethod(Map map) {\n"+
            "               print \"Groovy!\"\n"+
            "       }\n"+
            "   }\n"+
            "\n"+
            "class MyMap<K,V> extends Map {\n"+
            "\n"+
            "}\n"
        ]

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
            "----------\n")
    }

    void testHalfFinishedGenericsProgramWithCorrectSuppression() {
        String[] sources = [
            "Demo.groovy",
            "public class Demo {\n"+
            "\n"+
            "@SuppressWarnings(\"rawtypes\")\n"+ // should cause no warnings
            "List myList;\n"+
            "}\n"
        ]

        runNegativeTest(sources, "")
    }

    void testHalfFinishedGenericsProgramWithCorrectSuppressionAtTheTypeLevel() {
        String[] sources = [
            "Demo.groovy",
            "@SuppressWarnings(\"rawtypes\")\n"+ // should cause no warnings
            "public class Demo {\n"+
            "\n"+
            "List myList;\n"+
            "}\n"
        ]

        runNegativeTest(sources, "")
    }

    void testHalfFinishedGenericsProgramWithUnnecessarySuppression() {
        String[] sources = [
            "Demo.groovy",
            "public class Demo {\n"+
            "\n"+
            "@SuppressWarnings(\"unchecked\")\n"+ // unnecessary suppression
            "List<String> myList;\n"+
            "}\n"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Demo.groovy (at line 3)\n" +
            "\t@SuppressWarnings(\"unchecked\")\n" +
            "\t                  ^^^^^^^^^^^\n" +
            "Unnecessary @SuppressWarnings(\"unchecked\")\n" +
            "----------\n")
    }

    void testHalfFinishedGenericsProgramWithSuppressionValueSpeltWrong() {
        String[] sources = [
            "Demo.groovy",
            "public class Demo {\n"+
            "\n"+
            "@SuppressWarnings(\"unchecked2\")\n"+ // spelt wrong
            "List<String> myList;\n"+
            "}\n"
        ]
        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Demo.groovy (at line 3)\n" +
            "\t@SuppressWarnings(\"unchecked2\")\n" +
            "\t                  ^^^^^^^^^^^^\n" +
            "Unsupported @SuppressWarnings(\"unchecked2\")\n" +
            "----------\n")
    }

    void testHalfFinishedGenericsProgramWithMultipleSuppressionValues() {
        String[] sources = [
            "Demo.groovy",
            "public class Demo {\n"+
            "\n"+
            "@SuppressWarnings([\"rawtypes\",\"cast\"])\n"+
            "List myList;\n"+
            "}\n"
        ]

        runNegativeTest(sources, "")
    }

    void testHalfFinishedGenericsProgramWithMultipleSuppressionValuesWithOneSpeltWrong() {
        String[] sources = [
            "Demo.groovy",
            "public class Demo {\n"+
            "\n"+
            "@SuppressWarnings([\"rawtypes\",\"cast2\"])\n"+
            "List myList;\n"+
            "}\n"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Demo.groovy (at line 3)\n" +
            "\t@SuppressWarnings([\"rawtypes\",\"cast2\"])\n" +
            "\t                              ^^^^^^^\n" +
            "Unsupported @SuppressWarnings(\"cast2\")\n" +
            "----------\n")
    }

    boolean isEclipse36() {
        Platform.getBundle('org.eclipse.jdt.core').getVersion().toString().startsWith('3.6')
    }

    void testJava7() {
        if (isEclipse36() || complianceLevel < ClassFileConstants.JDK1_7) return

        String[] sources = [
            "A.java",
            "import java.util.*;\n"+
            "public class A {\n"+
            "public static void main(String[]argv) {\n"+
            "  List<String> ls = new ArrayList<>();"+
            "  int i = 1_000_000;\n"+
            "  int b = 0b110101;\n"+
            "  try {\n"+
            "    foo();\n"+
            "  } catch (java.io.IOException | IllegalStateException re) {\n"+
            "  }\n"+
            "}\n"+
            "  public static void foo() throws java.io.IOException {}\n"+
            "}",
            "B.groovy",
            "print 'a'\n"
        ]

        runConformTest(sources, "")
    }

    void testJava7_2() {
        if (isEclipse36() || complianceLevel >= ClassFileConstants.JDK1_7) return
        // should fail if compliance level < 1.7

        String[] sources = [
            "A.java",
            "import java.util.*;\n"+
            "public class A {\n"+
            "public static void main(String[]argv) {\n"+
            "  List<String> ls = new ArrayList<>();"+
            "  int i = 1_000_000;\n"+
            "  int b = 0b110101;\n"+
            "}\n"+
            "  public static void foo() throws java.io.IOException {}\n"+
            "}",

            "B.groovy",
            "print 'a'\n"
        ]

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
            "----------\n")
    }

    void testJava7_3() {
        if (isEclipse36() || complianceLevel >= ClassFileConstants.JDK1_7) return
        // should fail if compliance level < 1.7

        String[] sources = [
            "A.java",
            "import java.util.*;\n"+
            "public class A {\n"+
            "public static void main(String[]argv) {\n"+
            "  try {\n"+
            "    foo();\n"+
            "  } catch (java.io.IOException | IllegalStateException re) {\n"+
            "  }\n"+
            "}\n"+
            "  public static void foo() throws java.io.IOException {}\n"+
            "}",

            "B.groovy",
            "print 'a'\n"
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.java (at line 6)\n" +
            "\t} catch (java.io.IOException | IllegalStateException re) {\n" +
            "\t         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Multi-catch parameters are not allowed for source level below 1.7\n" +
            "----------\n")
    }

    void testTurningOffGenericsWarnings() {
        Map options = getCompilerOptions()
        options.put(CompilerOptions.OPTIONG_GroovyFlags, "0")

//      runConformTest(new String[] {
//              "Assertions.groovy",
//              "import spock.lang.*\n"+
//              "@Speck\n"+
//              "class Assertions {\n"+
////                "  public static void main(String[] argv) { new Assertions().comparingXandY();}\n"+
//              "  def comparingXandY() {\n"+
//              "    def x = 1\n"+
//              "    def y = 2\n"+
//              "    \n"+
////                " print 'a'\n"+
//              "    expect:\n"+
//              "    x < y    // OK\n"+
//              "    x == y   // BOOM!\n"+
//              " }\n"+
//              "}"},
//              "----------\n" +
//              "1. ERROR in Assertions.groovy (at line 4)\n" +
//              "   public static void main(String[] argv) {\n" +
//              "   ^^\n" +
//              "Groovy:Feature methods must not be static @ line 4, column 2.\n" +
//              "----------\n",
//              null,
//              true,
//              null,
//              options,
//              null)
//      this.runNegativeTest(new String[] {
//          "p/X.groovy",
//          "package p;\n" +
//          "public class X {\n" +
//          "  List l = new ArrayList();\n" +
//          "  public static void main(String[] argv) {\n"+
//          "    print 'success'\n"+
//          "  }\n"+
//          "}\n"},
//          "----------\n" +
//          "1. ERROR in Assertions.groovy (at line 4)\n" +
//          "   public static void main(String[] argv) {\n" +
//          "   ^^\n" +
//          "Groovy:Feature methods must not be static @ line 4, column 2.\n" +
//          "----------\n",
//          null,
//          true,
//          null,
//          options,
//          null)
    }
}
