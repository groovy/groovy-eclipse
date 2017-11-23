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
package org.codehaus.groovy23

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.Manifest

/**
 * This is a script to compute an updated list of exported packages for this bundle.
 * Run this simply as a Java application. It prints a list of all packages in the groovy-all jar.
 * Merged with any exported packages from the current MANIFEST.MF.
 * <p>
 * Paste this updated list into MANIFEST.MF to replace the old one. This ensures that there are
 * no osgi-hidden 'private' packages in groovy-all jar. These will cause problems with some
 * AST transforms accidentally picking up classes from the project's classpath instead of our
 * groovy-all jar.
 *
 * Note: this script is in a source folder compiled by eclipse, but isn't in build.properties. So
 * it won't be built by maven for CI and RELEASE builds. It is *not* part of Greclipse.
 *
 * @author Kris De Volder
 */

Set<String> packages = new TreeSet()

// read project's manifest
Manifest manifest = new Manifest()
InputStream is = new FileInputStream(new File('META-INF/MANIFEST.MF'))
try {
    manifest.read(is)
} finally {
    is.close()
}
String epa = manifest.getMainAttributes().getValue('Export-Package')
Collections.addAll(packages, epa.split('\\,(\\s)*'))

// read groovy-all jar's contents
JarFile jar = new JarFile(new File('lib/groovy-all-2.4.13.jar'))
try {
    Enumeration<JarEntry> entries = jar.entries()
    while (entries.hasMoreElements()) {
        String pathName = entries.nextElement().getName()
        if (pathName.endsWith('.class')) {
            int lastSlash = pathName.lastIndexOf('/')
            if (lastSlash >= 0) {
                String pkg = pathName.substring(0, lastSlash).replace('/', '.')
                packages.add(pkg)
            }
        }
    }
} finally {
    jar.close()
}

boolean first = true
for (String pkg : packages) {
    print(first ? 'Export-Package: ' : ',\n ')
    print(pkg)
    first = false
}
println()
