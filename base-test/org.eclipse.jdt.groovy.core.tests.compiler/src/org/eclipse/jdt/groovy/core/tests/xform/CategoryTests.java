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
package org.eclipse.jdt.groovy.core.tests.xform;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.lang.Category}.
 */
public final class CategoryTests extends GroovyCompilerTestSuite {

    @Test
    public void testCategory0() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "use(NumberCategory) {\n" +
            "  def dist = 300.meters\n" +
            "  \n" +
            "  assert dist instanceof Distance\n" +
            "  assert dist.toString() == '300m'\n" +
            "  print dist.toString()\n" +
            "}\n",

            "Distance.groovy",
            "final class Distance {\n" +
            "  def number\n" +
            "  String toString() { \"${number}m\" }\n" +
            "}\n",

            "NumberCategory.groovy",
            "class NumberCategory {\n" +
            "  static Distance getMeters(Number self) {\n" +
            "    new Distance(number: self)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "300m");
    }

    @Test
    public void testCategory1() {
        //@formatter:off
        String[] sources = {
            "Demo.groovy",
            "use(NumberCategory) {\n" +
            "  def dist = 300.meters\n" +
            "  \n" +
            "  assert dist instanceof Distance\n" +
            "  assert dist.toString() == \"300m\"\n" +
            "  print dist.toString()\n" +
            "}\n",

            "Distance.groovy",
            "final class Distance {\n" +
            "  def number\n" +
            "  String toString() { \"${number}m\" }\n" +
            "}\n",

            "NumberCategory.groovy",
            "@Category(Number) class NumberCategory {\n" +
            "  Distance getMeters() {\n" +
            "    new Distance(number: this)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "300m");
    }

    @Test
    public void testCategory2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "assert new Plane().fly() ==\n" +
            "       \"I'm the Concorde and I fly!\"\n" +
            "assert new Submarine().dive() ==\n" +
            "       \"I'm the Yellow Submarine and I dive!\"\n" +
            "\n" +
            "assert new JamesBondVehicle().fly() ==\n" +
            "       \"I'm the James Bond's vehicle and I fly!\"\n" +
            "assert new JamesBondVehicle().dive() ==\n" +
            "       \"I'm the James Bond's vehicle and I dive!\"\n" +
            "print new JamesBondVehicle().dive();\n",

            "FlyingAbility.groovy",
            "@Category(Vehicle) class FlyingAbility {\n" +
            "    def fly() { \"I'm the ${name} and I fly!\" }\n" +
            "}\n",

            "DivingAbility.groovy",
            "@Category(Vehicle) class DivingAbility {\n" +
            "    def dive() { \"I'm the ${name} and I dive!\" }\n" +
            "}\n",

            "Vehicle.java",
            "interface Vehicle {\n" +
            "    String getName();\n" +
            "}\n",

            "Submarine.groovy",
            "@Mixin(DivingAbility)\n" +
            "class Submarine implements Vehicle {\n" +
            "    String getName() { \"Yellow Submarine\" }\n" +
            "}\n",

            "Plane.groovy",
            "@Mixin(FlyingAbility)\n" +
            "class Plane implements Vehicle {\n" +
            "    String getName() { \"Concorde\" }\n" +
            "}\n",

            "JamesBondVehicle.groovy",
            "@Mixin([DivingAbility, FlyingAbility])\n" +
            "class JamesBondVehicle implements Vehicle {\n" +
            "    String getName() { \"James Bond's vehicle\" }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "I'm the James Bond's vehicle and I dive!");
    }

    @Test // not a great test, needs work
    public void testCategory_STS3822() {
        //@formatter:off
        String[] sources = {
            "bad.groovy",
            "@Category(C.class) \n" +
            "@ScriptMixin(C.class)\n" +
            "class Bad {\n" +
            "  @Override\n" +
            "  public String toString()\n" +
            "  { return \"Bad [takeI()=\" + takeI() + \"]\"; }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:@groovy.lang.Category must define \'value\' which is the class to apply this category to @ line 1, column 1.\n" +
            "----------\n" +
            "2. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t          ^\n" +
            "Groovy:unable to find class \'C.class\' for annotation attribute constant\n" +
            "----------\n" +
            "3. ERROR in bad.groovy (at line 1)\n" +
            "\t@Category(C.class) \n" +
            "\t          ^^^^^^^\n" +
            "Groovy:Only classes and closures can be used for attribute \'value\' in @groovy.lang.Category\n" +
            "----------\n" +
            "4. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t^^^^^^^^^^^^\n" +
            "Groovy:class ScriptMixin is not an annotation in @ScriptMixin\n" +
            "----------\n" +
            "5. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t ^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class ScriptMixin for annotation\n" +
            "----------\n" +
            "6. ERROR in bad.groovy (at line 2)\n" +
            "\t@ScriptMixin(C.class)\n" +
            "\t             ^\n" +
            "Groovy:unable to find class \'C.class\' for annotation attribute constant\n" +
            "----------\n" +
            "7. ERROR in bad.groovy (at line 4)\n" +
            "\t@Override\n" +
            "\t^^^^^^^^^\n" +
            "Groovy:Method \'toString\' from class \'Bad\' does not override method from its superclass or interfaces but is annotated with @Override.\n" +
            "----------\n");
    }
}
