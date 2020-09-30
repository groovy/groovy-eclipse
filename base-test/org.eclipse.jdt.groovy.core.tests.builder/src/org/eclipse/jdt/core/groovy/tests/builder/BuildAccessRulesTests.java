/*
 * Copyright 2009-2020 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.builder;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for build path access rules (i.e. restrictions placed on classpath entries).
 * <p>
 * Example:<pre>
 * &lt;classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER">
 *   &lt;accessrules>
 *     &lt;accessrule kind="nonaccessible" pattern="java/beans/**"/>
 *   &lt;/accessrules>
 * &lt;/classpathentry>
 * </pre>
 */
public final class BuildAccessRulesTests extends BuilderTestSuite {

    private IPath prj, src;
    private String problemFormat;

    @Before
    public void setUp() throws Exception {
        prj = env.addProject("Project");
        src = env.getPackageFragmentRootPath(prj, "src");
        env.setClasspath(prj, new IClasspathEntry[] {
            JavaCore.newSourceEntry(src),
            GroovyRuntime.newGroovyClasspathContainerEntry(false, false, null),
            JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath(),
                new IAccessRule[] {JavaCore.newAccessRule(new Path("java/beans/**"), IAccessRule.K_NON_ACCESSIBLE)}, null, false),
        });
        fullBuild(prj);

        problemFormat = "Problem : Access restriction: The type '%s' is not API (restriction on required library '##')" +
                                    " [ resource : </Project/src/Foo.groovy> range : <%d,%d> category : <150> severity : <2>]";
    }

    private void assertAccessRestriction(String source, String... types) {
        List<String> problems = new ArrayList<>();
        for (String type : types) {
            int offset = -1;
            while ((offset = source.indexOf(type.trim(), offset + 1)) != -1) {
                problems.add(String.format(problemFormat, last(type.split("\\.")).trim(), offset, offset + type.length()));
            }
        }

        IPath foo = env.addGroovyClass(src, "Foo", source);
        incrementalBuild(prj);

        expectingProblemsFor(foo, problems);
    }

    //--------------------------------------------------------------------------

    @Test
    public void testAccessForImport() {
        String source = "import java.beans.BeanDescriptor\n";

        assertAccessRestriction(source, "java.beans.BeanDescriptor");
    }

    @Test
    public void testAccessForExtends() {
        String source = "import java.beans.*\n" +
            "class Foo extends BeanDescriptor {\n" +
            "}";

        assertAccessRestriction(source, "BeanDescriptor");
    }

    @Test
    public void testAccessForImplements() {
        String source = "import java.beans.*\n" +
            "abstract class Foo implements BeanInfo {\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForExtendsGenerics() {
        String source = "import java.beans.*\n" +
            "abstract class Foo extends ArrayList<BeanDescriptor> {\n" +
            "}";

        assertAccessRestriction(source, "BeanDescriptor");
    }

    @Test
    public void testAccessForImplementsGenerics() {
        String source = "import java.beans.*\n" +
            "abstract class Foo implements List<BeanInfo> {\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForField() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  private BeanInfo info\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForProperty() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  BeanInfo info\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForFieldGenerics() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  private List<BeanInfo> info\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForPropertyGenerics() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  List<BeanInfo> info\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForLazyProperty() {
        String source = "import java.beans.*\n" +
            "abstract class Foo {\n" +
            "  @Lazy BeanInfo info = init()\n" +
            "  abstract def init()\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForMethodParameter() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  def meth(BeanInfo info) {}\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForMethodReturnType() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  BeanInfo meth() {}\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForMethodParameterGenerics() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  def meth(List<BeanInfo> info) {}\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForMethodReturnTypeGenerics() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  List<BeanInfo> meth() {}\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    @Test
    public void testAccessForLocalVariable() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  def meth() {\n" +
            "    BeanInfo info = null\n" +
            "    println info\n" +
            "  }\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }
}
