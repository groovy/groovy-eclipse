/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.builder;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.internal.core.ClasspathAccessRule;
import org.osgi.framework.Version;

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
public final class BuildAccessRulesTests extends BuilderTests {

    public static Test suite() {
        return buildTestSuite(BuildAccessRulesTests.class);
    }

    public BuildAccessRulesTests(String name) {
        super(name);
    }

    protected IPath src;

    private String problemFormat;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IPath projectPath = env.addProject("Project", "1.5");
        env.addGroovyNature("Project");
        env.setClasspath(projectPath, new IClasspathEntry[] {
            JavaCore.newSourceEntry(src = projectPath.append("src")),
            JavaCore.newContainerEntry(GroovyClasspathContainer.CONTAINER_ID),
            JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.5"),
                new IAccessRule[] {new ClasspathAccessRule(new Path("java/beans/**"), IAccessRule.K_NON_ACCESSIBLE)}, null, false) // create access restriction
        });
        env.createFolder(src);

        if (Platform.getBundle("org.eclipse.jdt.core").getVersion().compareTo(Version.parseVersion("3.10")) < 0) {
            problemFormat = "Problem : Access restriction: The type %s is not accessible due to restriction on required library ##" +
                                            " [ resource : </Project/src/Foo.groovy> range : <%d,%d> category : <150> severity : <2>]";
        } else {
            problemFormat = "Problem : Access restriction: The type '%s' is not API (restriction on required library '##')" +
                                            " [ resource : </Project/src/Foo.groovy> range : <%d,%d> category : <150> severity : <2>]";
        }
    }

    private void assertAccessRestriction(String source, String... types) {
        IPath foo = env.addGroovyClass(src, "Foo", source);
        fullBuild();

        // read back contents in case of line delimeters change or package statement addition or ...
        source = env.readTextFile(foo);

        List<String> problems = new ArrayList<String>();
        for (String type : types) {
            int offset = -1;
            while ((offset = source.indexOf(type.trim(), offset + 1)) != -1) {
                problems.add(String.format(problemFormat, type.trim(), offset, offset + type.length()));
            }
        }

        expectingProblemsFor(foo, problems);
    }

    //--------------------------------------------------------------------------

    public void testAccessForImport() {
        String source = "import java.beans.BeanDescriptor";

        assertAccessRestriction(source, "BeanDescriptor");
    }

    public void testAccessForExtends() {
        String source = "import java.beans.*\n" +
            "class Foo extends BeanDescriptor {}";

        assertAccessRestriction(source, "BeanDescriptor");
    }

    public void testAccessForImplements() {
        String source = "import java.beans.*\n" +
            "abstract class Foo implements BeanInfo {}";

        assertAccessRestriction(source, "BeanInfo "); // interface has +1 sloc...
    }

    public void testAccessForExtendsGenerics() {
        String source = "import java.beans.*\n" +
            "abstract class Foo extends ArrayList<BeanDescriptor> {}";

        assertAccessRestriction(source, "BeanDescriptor");
    }

    public void testAccessForImplementsGenerics() {
        String source = "import java.beans.*\n" +
            "abstract class Foo implements List<BeanInfo> {}";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForField() {
        String source = "import java.beans.*\n" +
            "class Foo { private BeanInfo info }";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForProperty() {
        String source = "import java.beans.*\n" +
            "class Foo { BeanInfo info }";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForFieldGenerics() {
        String source = "import java.beans.*\n" +
            "class Foo { private List<BeanInfo> info }";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForPropertyGenerics() {
        String source = "import java.beans.*\n" +
            "class Foo { List<BeanInfo> info }";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForLazyProperty() {
        String source = "import java.beans.*\n" +
            "abstract class Foo {\n" +
            "  @Lazy BeanInfo info = init()\n" +
            "  abstract def init()\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForMethodParameter() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  def meth(BeanInfo info) { }\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForMethodReturnType() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  BeanInfo meth() { }\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForMethodParameterGenerics() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  def meth(List<BeanInfo> info) { }\n" +
            "  }";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForMethodReturnTypeGenerics() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  List<BeanInfo> meth() { }\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }

    public void testAccessForLocalVariable() {
        String source = "import java.beans.*\n" +
            "class Foo {\n" +
            "  def meth() {\n" +
            "    BeanInfo info = null\n" +
            "  }\n" +
            "}";

        assertAccessRestriction(source, "BeanInfo");
    }
}
