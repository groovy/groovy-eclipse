/*
 * Copyright 2009-2023 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import static org.eclipse.jdt.internal.compiler.util.SuffixConstants.EXTENSION_java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Utility methods for dealing with Groovy content types.
 */
public class ContentTypeUtils {

    private ContentTypeUtils() {}

    /**
     * Returns the registered Gradle-like extensions.
     */
    public static char[][] getGradleLikeExtensions() {
        if (GRADLE_LIKE_EXTENSIONS == null) {
            IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
            if (contentTypeManager != null) {
                IContentType gradleContentType = contentTypeManager.getContentType(GRADLE_SCRIPT_CONTENT_TYPE);
                String[] gradleFileExtensions = gradleContentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);

                GRADLE_LIKE_EXTENSIONS = toCharArrayArray(Arrays.asList(gradleFileExtensions));
            } else {
                GRADLE_LIKE_EXTENSIONS = new char[][] {"gradle".toCharArray()};
            }
        }
        return GRADLE_LIKE_EXTENSIONS;
    }

    /**
     * Returns the registered Groovy-like extensions.
     */
    public static char[][] getGroovyLikeExtensions() {
        if (GROOVY_LIKE_EXTENSIONS == null) {
            IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
            if (contentTypeManager != null) {
                Set<String> extensions = new TreeSet<>((s1, s2) -> {
                    if (s1.equals("groovy")) return -1;
                    if (s2.equals("groovy")) return +1;
                    return s1.compareTo(s2);
                });
                IContentType groovyContentType = contentTypeManager.getContentType(GROOVY_SOURCE_CONTENT_TYPE);
                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=121715
                // content types derived from Groovy content type should be included
                for (IContentType contentType : contentTypeManager.getAllContentTypes()) {
                    if (contentType.isKindOf(groovyContentType)) {
                        for (String fileExtension : contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)) {
                            extensions.add(fileExtension);
                        }
                    }
                }
                if (extensions.isEmpty()) {
                    // Shouldn't happen, but it seems it does.  See: STS-3936
                    // Probably means user's workspace is already severely broken...
                    // Still it is not nice to throw exceptions. So handle the case and log an error instead.
                    if (!noGroovyContentTypesErrorLogged) {
                        noGroovyContentTypesErrorLogged = true;
                        Util.log(new IllegalStateException("No Groovy Content Types found. This shouldn't happen. Is the workspace metadata corrupted?"));
                    }
                    // Don't cache it. Maybe its only looking funky because we are looking 'too early'.
                    return new char[][] {"groovy".toCharArray()};
                }
                GROOVY_LIKE_EXTENSIONS = toCharArrayArray(extensions);
            } else {
                GROOVY_LIKE_EXTENSIONS = new char[][] {"groovy".toCharArray()};
            }
        }
        return GROOVY_LIKE_EXTENSIONS;
    }

    /**
     * Returns the registered Java-like extensions (excluding the Groovy-like ones).
     */
    public static char[][] getJavaButNotGroovyLikeExtensions() {
        if (JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS == null) {
            char[][] javaLikeExtensions = Platform.getContentTypeManager() != null ? Util.getJavaLikeExtensions() : new char[][] {EXTENSION_java.toCharArray()};
            char[][] groovyLikeExtensiosn = getGroovyLikeExtensions();
            List<char[]> interestingExtensions = new ArrayList<>();
            for (char[] javaLike : javaLikeExtensions) {
                boolean found = false;
                for (char[] groovyLike : groovyLikeExtensiosn) {
                    if (Arrays.equals(javaLike, groovyLike)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    interestingExtensions.add(javaLike);
                }
            }
            JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS = interestingExtensions.toArray(new char[interestingExtensions.size()][]);

            // ensure "java" is first
            char[] javaChars = EXTENSION_java.toCharArray(); int javaIndex = 0;
            while (javaIndex < JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS.length) {
                if (Arrays.equals(javaChars, JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[javaIndex])) {
                    break;
                }
                javaIndex += 1;
            }
            if (javaIndex < JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS.length) {
                JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[javaIndex] = JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[0];
                JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[0] = javaChars;
            } else {
                Util.log(null, "'java' not registered as a java-like extension");
            }
        }
        return JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS;
    }

    /**
     * Determines if the given file is a Gradle script file.
     *
     * @param fileName absolute path or simple name is fine
     * @return {@code true} iff the file name is Gradle-like
     */
    public static boolean isGradleLikeFileName(char[] fileName) {
        if (fileName != null && fileName.length > 0) {
            return isGradleLikeFileName(new CharArraySequence(fileName));
        }
        return false;
    }

    /**
     * Determines if the given file is a Gradle script file.
     *
     * @param fileName absolute path or simple name is fine
     * @return {@code true} iff the file name is Gradle-like
     */
    public static boolean isGradleLikeFileName(CharSequence fileName) {
        if (fileName != null && fileName.length() > 0) {
            if (endsWithAny(fileName, getGradleLikeExtensions())) {
                return true;
            }
            // TODO: Gradle file names
        }
        return false;
    }

    /**
     * Determines if the given file is a Groovy source file.
     *
     * @param fileName absolute path or simple name is fine
     * @return {@code true} iff the file name is Groovy-like
     */
    public static boolean isGroovyLikeFileName(char[] fileName) {
        if (fileName != null && fileName.length > 0) {
            return isGroovyLikeFileName(new CharArraySequence(fileName));
        }
        return false;
    }

    /**
     * Determines if the given file is a Groovy source file.
     *
     * @param fileName absolute path or simple name is fine
     * @return {@code true} iff the file name is Groovy-like
     */
    public static boolean isGroovyLikeFileName(CharSequence fileName) {
        if (fileName != null && fileName.length() > 0) {
            if (endsWithAny(fileName, getGroovyLikeExtensions())) {
                return true;
            }
            if (GROOVY_FILE_NAMES == null) {
                GROOVY_FILE_NAMES = loadGroovyFileNames();
            }
            fileName = fileName.subSequence(
                lastIndexOf(fileName, '/') + 1, fileName.length());
            return GROOVY_FILE_NAMES.contains(fileName.toString());
        }
        return false;
    }

    /**
     * Determines if the given file is a Java source file.
     *
     * @param fileName absolute path or simple name is fine
     * @return {@code true} iff the file name is Java-like (but not Groovy-like)
     */
    public static boolean isJavaLikeButNotGroovyLikeFileName(CharSequence fileName) {
        if (fileName != null && fileName.length() > 0) {
            if (endsWithAny(fileName, getJavaButNotGroovyLikeExtensions())) {
                return true;
            }
        }
        return false;
    }

    //--------------------------------------------------------------------------

    static {
        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        if (contentTypeManager != null) {
            contentTypeManager.addContentTypeChangeListener(event -> {
                // can be more specific here, but content types change so rarely, that
                // I am not concerned about being overly eager to invalidate the cache
                GROOVY_FILE_NAMES = null;
                GROOVY_LIKE_EXTENSIONS = null;
                GRADLE_LIKE_EXTENSIONS = null;
                JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS = null;
            });
        }
    }

    private static Set<String> GROOVY_FILE_NAMES;
    private static char[][] GROOVY_LIKE_EXTENSIONS;
    private static char[][] GRADLE_LIKE_EXTENSIONS;
    private static char[][] JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS;

    private static final String GRADLE_SCRIPT_CONTENT_TYPE = "org.eclipse.jdt.groovy.core.gradleScript";
    private static final String GROOVY_SOURCE_CONTENT_TYPE = "org.eclipse.jdt.groovy.core.groovySource";

    private static boolean noGroovyContentTypesErrorLogged; // to avoid spamming error into the log repeatedly

    private static Set<String> loadGroovyFileNames() {
        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        if (contentTypeManager != null) {
            Set<String> names = null;
            IContentType groovyContentType = contentTypeManager.getContentType(GROOVY_SOURCE_CONTENT_TYPE);
            for (IContentType contentType : contentTypeManager.getAllContentTypes()) {
                if (contentType.isKindOf(groovyContentType)) {
                    for (String fileName : contentType.getFileSpecs(IContentType.FILE_NAME_SPEC)) {
                        if (names == null) names = new TreeSet<>();
                        names.add(fileName);
                    }
                }
            }
            if (names != null) return names;
        }
        return Collections.emptySet();
    }

    //--------------------------------------------------------------------------

    private static boolean endsWithAny(CharSequence sequence, char[]... extensions) {
        int length = sequence.length();
        for (char[] extension : extensions) {
            int offset = (length - extension.length);
            if (offset < 1 || sequence.charAt(offset - 1) != '.') {
                continue;
            }
            if (sequence.subSequence(offset, length).toString().equals(String.valueOf(extension))) {
                return true;
            }
        }
        return false;
    }

    private static int lastIndexOf(CharSequence sequence, char character) {
        for (int i = sequence.length() - 1; i >= 0; i -= 1) {
            if (sequence.charAt(i) == character) {
                return i;
            }
        }
        return -1;
    }

    private static char[][] toCharArrayArray(Collection<String> strings) {
        int i = 0, n = strings.size();
        char[][] arrays = new char[n][];
        for (String string : strings) {
            arrays[i++] = string.toCharArray();
        }
        return arrays;
    }
}
