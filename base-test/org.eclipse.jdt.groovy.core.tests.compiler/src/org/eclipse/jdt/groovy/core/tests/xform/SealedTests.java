/*
 * Copyright 2009-2021 the original author or authors.
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
            "}",

            "Truck.groovy",
            "final class Truck extends Vehicle implements Serviceable {\n" +
            "  final int loadCapacity\n" +
            "  Truck(int loadCapacity, String registrationNumber) {\n" +
            "    super(registrationNumber)\n" +
            "    this.loadCapacity = loadCapacity\n" +
            "  }\n" +
            "  final int maxDistanceBetweenServicesInKilometers = 100_000\n" +
            "  @Override int getMaxServiceIntervalInMonths() { return 18 }\n" +
            "}",
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
            "}",

            "Baz.groovy",
            "class Baz extends Foo {\n" + // missing "final", "sealed" or "non-sealed"
            "}",

            "Boo.groovy",
            "class Boo extends Foo {\n" + // not permitted
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Boo.groovy (at line 1)\n" +
            "\tclass Boo extends Foo {\n" +
            "\t      ^^^\n" +
            "Groovy:The class 'Boo' is not a permitted subclass of the sealed class 'Foo'.\n" +
            "----------\n");
    }

    // non-sealed without extends
    // sealed without permits
    // java extension
}
