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
package org.eclipse.jdt.groovy.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Utility methods for dealing with Groovy content types
 *
 * @author Andrew Eisenberg
 * @created Jun 23, 2009
 */
public class ContentTypeUtils {

    private ContentTypeUtils() {
        // uninstantiable
    }

    static {
        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        if (contentTypeManager != null) {
            contentTypeManager.addContentTypeChangeListener(new IContentTypeManager.IContentTypeChangeListener() {
                public void contentTypeChanged(ContentTypeChangeEvent event) {
                    // we can be more specific here, but content types change so rarely, that
                    // I am not concerned about being overly eager to invalidate the cache
                    GROOVY_FILE_NAMES = null;
                    GROOVY_LIKE_EXTENSIONS = null;
                    JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS = null;
                }
            });
        }
    }

    private static Set<String> GROOVY_FILE_NAMES;
    private static char[][] GROOVY_LIKE_EXTENSIONS;
    private static char[][] JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS;

    public static final String GROOVY_SOURCE_CONTENT_TYPE = "org.eclipse.jdt.groovy.core.groovySource";

    private static boolean noGroovyContentTypesErrorLogged = false; // To avoid spamming error into the log repeatedly.

    /**
     * Uses the Eclipse content type extension point to determine if a file is a groovy file.
     *
     * @param fileName (absolute path or simple name is fine)
     * @return {@code true} iff the file name is Groovy-like
     */
    public static boolean isGroovyLikeFileName(char[] fileName) {
        if (fileName != null && fileName.length > 0) {
            return isGroovyLikeFileName(String.valueOf(fileName));
        }
        return false;
    }

    /**
     * Uses the Eclipse content type extension point to determine if a file is a groovy file.
     *
     * @param fileName (absolute path or simple name is fine)
     * @return {@code true} iff the file name is Groovy-like
     */
    public static boolean isGroovyLikeFileName(String fileName) {
        if (fileName != null && fileName.length() > 0 && !fileName.endsWith(".java")) {
            if (indexOfGroovyLikeExtension(fileName) != -1) {
                return true;
            }
            if (GROOVY_FILE_NAMES == null) {
                GROOVY_FILE_NAMES = loadGroovyFileNames();
            }
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            return GROOVY_FILE_NAMES.contains(fileName);
        }
        return false;
    }

    /**
     * Returns the index of the Groovy like extension of the given file name or -1 if it doesn't end with a known Java like
     * extension. Note this is the index of the '.' even if it is not considered part of the extension. Adapted from
     * {@link org.eclipse.jdt.internal.core.util.Util#indexOfJavaLikeExtension}.
     */
    public static int indexOfGroovyLikeExtension(String fileName) {
        if (fileName != null) {
            int fileNameLength = fileName.length();
            if (fileNameLength > 0) {
                for (char[] extension : getGroovyLikeExtensions()) {
                    int offset = (fileNameLength - extension.length);
                    if (offset < 1 || fileName.charAt(offset - 1) != '.')
                        continue;
                    if (Arrays.equals(extension, fileName.substring(offset).toCharArray()))
                        return (offset - 1);
                }
            }
        }
        return -1;
    }

    /**
     * Returns the registered Java like extensions. Taken from org.eclipse.jdt.internal.core.util.Util.getJavaLikeExtensions
     */
    public static char[][] getGroovyLikeExtensions() {
        if (GROOVY_LIKE_EXTENSIONS == null) {
            Set<String> fileExtensions = loadGroovyFileExtensions();
            if (fileExtensions.isEmpty()) {
                // Shouldn't happen, but it seems it does.
                // See: STS-3936
                // Probably means user's workspace is already severely broken...
                // Still it is not nice to throw exceptions. So handle the case and log an error instead.
                if (!noGroovyContentTypesErrorLogged) {
                    noGroovyContentTypesErrorLogged = true;
                    Util.log(new IllegalStateException(
                            "No Groovy Content Types found. This shouldn't happen. Is the workspace metadata corrupted?"));
                }
                // Don't cache it. Maybe its only looking funky because we are looking 'too early'.
                return new char[][] { "groovy".toCharArray() };
            } else {
                int length = fileExtensions.size();
                char[][] extensions = new char[length][];
                extensions[0] = "groovy".toCharArray(); // ensure that "groovy" is first
                int index = 1;
                for (String fileExtension : fileExtensions) {
                    if ("groovy".equals(fileExtension))
                        continue;
                    extensions[index++] = fileExtension.toCharArray();
                }
                GROOVY_LIKE_EXTENSIONS = extensions;
            }
        }
        return GROOVY_LIKE_EXTENSIONS;
    }

    private static Set<String> loadGroovyFileExtensions() {
        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        if (contentTypeManager != null) {
            IContentType groovyContentType = contentTypeManager.getContentType(GROOVY_SOURCE_CONTENT_TYPE);
            Set<String> extensions = new HashSet<String>();
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=121715
            // content types derived from groovy content type should be included
            for (IContentType contentType : contentTypeManager.getAllContentTypes()) {
                if (contentType.isKindOf(groovyContentType)) { // note that contentType.isKindOf(javaContentType) == true
                    for (String fileExtension : contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)) {
                        extensions.add(fileExtension);
                    }
                }
            }
            return extensions;
        }
        return Collections.singleton("groovy");
    }

    private static Set<String> loadGroovyFileNames() {
        IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
        if (contentTypeManager != null) {
            Set<String> names = null;
            IContentType groovyContentType = contentTypeManager.getContentType(GROOVY_SOURCE_CONTENT_TYPE);
            for (IContentType contentType : contentTypeManager.getAllContentTypes()) {
                if (contentType.isKindOf(groovyContentType)) {
                    for (String fileName : contentType.getFileSpecs(IContentType.FILE_NAME_SPEC)) {
                        if (names == null) names = new TreeSet<String>();
                        names.add(fileName);
                    }
                }
            }
            if (names != null) return names;
        }
        return Collections.emptySet();
    }

    public static boolean isJavaLikeButNotGroovyLikeExtension(String fileName) {
        if (JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS == null) {
            initJavaLikeButNotGroovyLikeExtensions();
        }

        int fileNameLength = fileName.length();
        extensions: for (int i = 0, length = JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS.length; i < length; i++) {
            char[] extension = JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[i];
            int extensionLength = extension.length;
            int extensionStart = fileNameLength - extensionLength;
            int dotIndex = extensionStart - 1;
            if (dotIndex < 0)
                continue;
            if (fileName.charAt(dotIndex) != '.')
                continue;
            for (int j = 0; j < extensionLength; j++) {
                if (fileName.charAt(extensionStart + j) != extension[j])
                    continue extensions;
            }
            return true;
        }

        return false;
    }

    private static void initJavaLikeButNotGroovyLikeExtensions() {
        char[][] javaLikeExtensions = Util.getJavaLikeExtensions();
        char[][] groovyLikeExtensiosn = getGroovyLikeExtensions();
        List<char[]> interestingExtensions = new ArrayList<char[]>();
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
        int javaIndex = 0;
        char[] javaArr = "java".toCharArray();
        while (javaIndex < JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS.length) {
            if (Arrays.equals(javaArr, JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[javaIndex])) {
                break;
            }
            javaIndex++;
        }
        if (javaIndex < JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS.length) {
            JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[javaIndex] = JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[0];
            JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS[0] = javaArr;
        } else {
            Util.log(null, "'java' not registered as a java-like extension");
        }
    }

    public static char[][] getJavaButNotGroovyLikeExtensions() {
        if (JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS == null) {
            initJavaLikeButNotGroovyLikeExtensions();
        }
        return JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS;
    }
}
