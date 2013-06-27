/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.groovy.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Jun 23, 2009
 * 
 *          Utility methods for dealing with Groovy content types
 */
@SuppressWarnings("restriction")
public class ContentTypeUtils {

	static class ChangeListener implements IContentTypeManager.IContentTypeChangeListener {
		public void contentTypeChanged(ContentTypeChangeEvent event) {
			// we can be more specific here, but content types change so rarely, that
			// I am not concerned about being overly eager to invalidate the cache
			GROOVY_LIKE_EXTENSIONS = null;
			JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS = null;
		}
	}

	static {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		if (contentTypeManager != null) {
			contentTypeManager.addContentTypeChangeListener(new ChangeListener());
		}
	}

	private ContentTypeUtils() {
		// uninstantiable
	}

	private static char[][] GROOVY_LIKE_EXTENSIONS;

	private static char[][] JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS;

	public static String GROOVY_SOURCE_CONTENT_TYPE = "org.eclipse.jdt.groovy.core.groovySource"; //$NON-NLS-1$

	/**
	 * Uses the Eclipse content type extension point to determine if a file is a groovy file. Taken from
	 * org.eclipse.jdt.internal.core.util.Util.isJavaLikeExtension
	 * 
	 * @param file name (absolute path or simple name is fine)
	 * @return true iff the file name is Groovy-like.
	 */
	public static boolean isGroovyLikeFileName(String name) {
		if (name == null)
			return false;
		return indexOfGroovyLikeExtension(name) != -1;
	}

	/**
	 * Uses the Eclipse content type extension point to determine if a file is a groovy file. Taken from
	 * org.eclipse.jdt.internal.core.util.Util.isJavaLikeExtension
	 * 
	 * @param file name (absolute path or simple name is fine)
	 * @return true iff the file name is Groovy-like.
	 */
	public final static boolean isGroovyLikeFileName(char[] fileName) {
		if (fileName == null)
			return false;
		int fileNameLength = fileName.length;
		char[][] javaLikeExtensions = getGroovyLikeExtensions();
		extensions: for (int i = 0, length = javaLikeExtensions.length; i < length; i++) {
			char[] extension = javaLikeExtensions[i];
			int extensionLength = extension.length;
			int extensionStart = fileNameLength - extensionLength;
			if (extensionStart - 1 < 0)
				continue;
			if (fileName[extensionStart - 1] != '.')
				continue;
			for (int j = 0; j < extensionLength; j++) {
				if (fileName[extensionStart + j] != extension[j])
					continue extensions;
			}
			return true;
		}
		return false;
	}

	/*
	 * Returns the index of the Groovy like extension of the given file name or -1 if it doesn't end with a known Java like
	 * extension. Note this is the index of the '.' even if it is not considered part of the extension. Taken from
	 * org.eclipse.jdt.internal.core.util.Util.indexOfJavaLikeExtension
	 */
	public static int indexOfGroovyLikeExtension(String fileName) {
		int fileNameLength = fileName.length();
		char[][] groovyLikeExtensions = getGroovyLikeExtensions();
		extensions: for (int i = 0, length = groovyLikeExtensions.length; i < length; i++) {
			char[] extension = groovyLikeExtensions[i];
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
			return dotIndex;
		}
		return -1;
	}

	/**
	 * Returns the registered Java like extensions. Taken from org.eclipse.jdt.internal.core.util.Util.getJavaLikeExtensions
	 */
	public static char[][] getGroovyLikeExtensions() {
		if (GROOVY_LIKE_EXTENSIONS == null) {
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			if (contentTypeManager == null) {
				// batch
				GROOVY_LIKE_EXTENSIONS = new char[][] { "groovy".toCharArray() };
				return GROOVY_LIKE_EXTENSIONS;
			}
			IContentType groovyContentType = contentTypeManager.getContentType(GROOVY_SOURCE_CONTENT_TYPE);
			HashSet<String> fileExtensions = new HashSet<String>();
			// content types derived from groovy content type should be included
			// (https://bugs.eclipse.org/bugs/show_bug.cgi?id=121715)
			IContentType[] contentTypes = contentTypeManager.getAllContentTypes();
			for (int i = 0, length = contentTypes.length; i < length; i++) {
				if (contentTypes[i].isKindOf(groovyContentType)) { // note that javaContentType.isKindOf(javaContentType) == true
					String[] fileExtension = contentTypes[i].getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
					for (int j = 0, length2 = fileExtension.length; j < length2; j++) {
						fileExtensions.add(fileExtension[j]);
					}
				}
			}
			int length = fileExtensions.size();
			char[][] extensions = new char[length][];
			extensions[0] = "groovy".toCharArray(); // ensure that "groovy" is first //$NON-NLS-1$
			int index = 1;
			Iterator<String> iterator = fileExtensions.iterator();
			while (iterator.hasNext()) {
				String fileExtension = iterator.next();
				if ("groovy".equals(fileExtension)) //$NON-NLS-1$
					continue;
				extensions[index++] = fileExtension.toCharArray();
			}
			GROOVY_LIKE_EXTENSIONS = extensions;
		}
		return GROOVY_LIKE_EXTENSIONS;
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
		char[] javaArr = "java".toCharArray(); //$NON-NLS-1$
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
			Util.log(null, "'java' not registered as a java-like extension"); //$NON-NLS-1$
		}
	}

	public static char[][] getJavaButNotGroovyLikeExtensions() {
		if (JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS == null) {
			initJavaLikeButNotGroovyLikeExtensions();
		}
		return JAVA_LIKE_BUT_NOT_GROOVY_LIKE_EXTENSIONS;
	}
}
