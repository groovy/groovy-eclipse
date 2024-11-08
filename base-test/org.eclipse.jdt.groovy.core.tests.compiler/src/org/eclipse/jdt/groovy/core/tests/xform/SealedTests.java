/*
 * Copyright 2009-2024 the original author or authors.
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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.Sealed}.
 */
public final class SealedTests extends GroovyCompilerTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastGroovy(40));
    }

    @Test
    public void testSealed1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "def car = new Car(2,'vin')\n" +
            "assert car.maxServiceIntervalInMonths == 12\n" +
            "assert car.maxDistanceBetweenServicesInKilometers == 100_000\n",

            "Serviceable.groovy",
            "@groovy.transform.Sealed(permittedSubclasses=[Car,Truck])\n" +
            "interface Serviceable {\n" +
            "  int getMaxDistanceBetweenServicesInKilometers()\n" +
            "  int getMaxServiceIntervalInMonths()\n" +
            "}\n",

            "Vehicle.groovy",
            "@groovy.transform.Sealed(permittedSubclasses=[Car,Truck])\n" +
            "abstract class Vehicle {\n" +
            "  final String registrationNumber\n" +
            "  Vehicle(String registrationNumber) {\n" +
            "    this.registrationNumber = registrationNumber\n" +
            "  }\n" +
            "}\n",

            "Car.groovy",
            "@groovy.transform.NonSealed\n" +
            "class Car extends Vehicle implements Serviceable {\n" +
            "  final int numberOfSeats\n" +
            "  Car(int numberOfSeats, String registrationNumber) {\n" +
            "    super(registrationNumber)\n" +
            "    this.numberOfSeats = numberOfSeats\n" +
            "  }\n" +
            "  final int maxDistanceBetweenServicesInKilometers = 100_000\n" +
            "  @Override int getMaxServiceIntervalInMonths() { return 12 }\n" +
            "}\n",

            "Truck.groovy",
            "final class Truck extends Vehicle implements Serviceable {\n" +
            "  final int loadCapacity\n" +
            "  Truck(int loadCapacity, String registrationNumber) {\n" +
            "    super(registrationNumber)\n" +
            "    this.loadCapacity = loadCapacity\n" +
            "  }\n" +
            "  final int maxDistanceBetweenServicesInKilometers = 100_000\n" +
            "  @Override int getMaxServiceIntervalInMonths() { return 18 }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testSealed2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.Sealed(permittedSubclasses=[Bar,Baz])\n" +
            "abstract class Foo {\n" +
            "}\n",

            "Bar.groovy",
            "class Bar {\n" + // missing "extends Foo"
            "}\n",

            "Baz.groovy",
            "class Baz extends Foo {\n" + // missing "final", "sealed" or "non-sealed"
            "}\n",

            "Boo.groovy",
            "class Boo extends Foo {\n" + // not permitted
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, !javaModelSealedSupport()
            ?
            "----------\n" +
            "1. ERROR in Boo.groovy (at line 1)\n" +
            "\tclass Boo extends Foo {\n" +
            "\t      ^^^\n" +
            "Groovy:The class 'Boo' is not a permitted subclass of the sealed class 'Foo'.\n" +
            "----------\n"
            :
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\t@groovy.transform.Sealed(permittedSubclasses=[Bar,Baz])\n" +
            "\t                                              ^^^\n" +
            (javaModelSealedSupport2()
            ? "Permitted type Bar does not declare Foo as a direct supertype\n"
            : "Permitted class Bar does not declare Foo as direct super class\n") +
            "----------\n" +
            "----------\n" +
            "1. ERROR in Baz.groovy (at line 1)\n" +
            "\tclass Baz extends Foo {\n" +
            "\t      ^^^\n" +
            "The class Baz with a sealed direct super" + (javaModelSealedSupport2() ? "type" : "class or a sealed direct superinterface") +
            " Foo should be declared either final, sealed, or non-sealed\n" +
            "----------\n" +
            "----------\n" +
            "1. ERROR in Boo.groovy (at line 1)\n" +
            "\tclass Boo extends Foo {\n" +
            "\t      ^^^\n" +
            "The class Boo with a sealed direct super" + (javaModelSealedSupport2() ? "type" : "class or a sealed direct superinterface") +
            " Foo should be declared either final, sealed, or non-sealed\n" +
            "----------\n" +
            "2. ERROR in Boo.groovy (at line 1)\n" +
            "\tclass Boo extends Foo {\n" +
            "\t      ^^^\n" +
            "Groovy:The class 'Boo' is not a permitted subclass of the sealed class 'Foo'.\n" +
            "----------\n" +
            "3. ERROR in Boo.groovy (at line 1)\n" +
            "\tclass Boo extends Foo {\n" +
            "\t                  ^^^\n" +
            (javaModelSealedSupport2()
            ? "The class Boo cannot extend the class Foo as it is not a permitted subtype of Foo\n"
            : "The type Boo extending a sealed class Foo should be a permitted subtype of Foo\n") +
            "----------\n");
    }

    @Test
    public void testSealed3() {
        assumeTrue(javaModelSealedSupport());

        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p\n" +
            "@groovy.transform.Sealed(permittedSubclasses=[Bar.class,p.Baz])\n" +
            "@groovy.transform.PackageScope abstract class Foo {\n" +
            "}\n",

            "p/Bar.java",
            "package p;\n" +
            "final class Bar extends Foo {\n" +
            "}\n",

            "p/Baz.java",
            "package p;\n" +
            "class Baz extends Foo {\n" + // missing "final", "sealed" or "non-sealed"
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\Baz.java (at line 2)\n" +
            "\tclass Baz extends Foo {\n" +
            "\t      ^^^\n" +
            "The class Baz with a sealed direct super" + (javaModelSealedSupport2() ? "type" : "class or a sealed direct superinterface") +
            " Foo should be declared either final, sealed, or non-sealed\n" +
            "----------\n");
    }

    // non-sealed without extends
    // sealed without permits

    private static boolean javaModelSealedSupport() {
        return org.eclipse.jdt.core.JavaCore.getPlugin().getBundle().getVersion()
                .compareTo(org.osgi.framework.Version.parseVersion("3.28")) >= 0;
    }

    private static boolean javaModelSealedSupport2() {
        return org.eclipse.jdt.core.JavaCore.getPlugin().getBundle().getVersion()
                .compareTo(org.osgi.framework.Version.parseVersion("3.40")) >= 0;
    }
}
