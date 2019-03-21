/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * A classpath attribute defines a name/value pair that can be persisted with a classpath entry. Such an attribute
 * can be created using the factory method {@link JavaCore#newClasspathAttribute(String, String) newClasspathAttribute(String name, String value)}.
 *
 * @see JavaCore#newContainerEntry(
 *			org.eclipse.core.runtime.IPath containerPath,
 *			IAccessRule[] accessRules,
 *			IClasspathAttribute[] extraAttributes,
 *			boolean isExported)
 * @see JavaCore#newLibraryEntry(
 *			org.eclipse.core.runtime.IPath path,
 *			org.eclipse.core.runtime.IPath sourceAttachmentPath,
 *			org.eclipse.core.runtime.IPath sourceAttachmentRootPath,
 *			IAccessRule[] accessRules,
 *			IClasspathAttribute[] extraAttributes,
 *			boolean isExported)
 * @see JavaCore#newProjectEntry(
 *			org.eclipse.core.runtime.IPath path,
 *			IAccessRule[] accessRules,
 *			boolean combineAccessRestrictions,
 *			IClasspathAttribute[] extraAttributes,
 *			boolean isExported)
 * @see JavaCore#newSourceEntry(
 * 			org.eclipse.core.runtime.IPath path,
 * 			org.eclipse.core.runtime.IPath[] inclusionPatterns,
 * 			org.eclipse.core.runtime.IPath[] exclusionPatterns,
 * 			org.eclipse.core.runtime.IPath specificOutputLocation,
 * 			IClasspathAttribute[] extraAttributes)
 * @see JavaCore#newVariableEntry(
 *			org.eclipse.core.runtime.IPath variablePath,
 *			org.eclipse.core.runtime.IPath variableSourceAttachmentPath,
 *			org.eclipse.core.runtime.IPath variableSourceAttachmentRootPath,
 *			IAccessRule[] accessRules,
 *			IClasspathAttribute[] extraAttributes,
 *			boolean isExported)
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IClasspathAttribute {

	/**
	 * Constant for the name of the javadoc location attribute.
	 * 
	 * <p>The value for this attribute has to be the string representation of a URL.</p>
	 *
	 * @since 3.1
	 */
	String JAVADOC_LOCATION_ATTRIBUTE_NAME = "javadoc_location"; //$NON-NLS-1$
	
	/**
	 * Constant for the name of the index location attribute.
	 * 
	 * <p>The value for this attribute has to be the string representation of a URL.
	 * It should point to an existing index file in a folder or a jar. The URL can also be of platform protocol.</p>
	 * 
	 * @since 3.8
	 */
	String INDEX_LOCATION_ATTRIBUTE_NAME = "index_location"; //$NON-NLS-1$

	/**
	 * Constant for the name of the encoding to be used for source attachments.
	 * 
	 * <p>The value of this attribute has to be a string representation of a valid encoding. The encoding
	 * for a source attachment is determined in the following order: </p>
	 *
	 * <ul>
	 * <li>	Encoding explicitly set on the source file (java or zip), i.e. <code>org.eclipse.core.resources.IFile#getCharset(false)</code> </li>
	 * <li>	Encoding set on the corresponding classpath entry </li>
	 * <li> If the source attachment is a folder, then the encoding determined by the file content if detectable </li>
	 * <li> If the source attachment is in the workspace, then the encoding of the enclosing resources</li>
	 * </ul>
	 *
	 * @see org.eclipse.core.resources.IFile#getCharset()
	 * @since 3.8
	 */
	String SOURCE_ATTACHMENT_ENCODING = "source_encoding"; //$NON-NLS-1$

	/**
	 * Constant for the name of the ignore optional compile problems attribute.
	 * This attribute is valid only for classpath entries describing source folders.
	 * The possible values for this attribute are <code>"true"</code> or
	 * <code>"false"</code>. When not present, <code>"false"</code> is assumed.
	 * If the value of this attribute is <code>"true"</code>, all optional problems
	 * from the source folder described by this classpath entry will not be reported
	 * by the compiler.
	 *
	 * @since 3.8
	 */
	String IGNORE_OPTIONAL_PROBLEMS = "ignore_optional_problems"; //$NON-NLS-1$

	/**
	 * Constant for the name of the optional attribute. The possible values
	 * for this attribute are <code>"true"</code> or <code>"false"</code>.
	 * When not present, <code>"false"</code> is assumed.
	 * If the value of this attribute is <code>"true"</code>, the classpath entry
	 * is optional. If the underlying resource or jar file doesn't exist, no error
	 * is reported and the classpath entry is ignored.
	 *
	 * @since 3.2
	 */
	String OPTIONAL = "optional"; //$NON-NLS-1$

	/**
	 * Constant for the name of the module attribute. The possible values
	 * for this attribute are <code>"true"</code> or <code>"false"</code>.
	 * When not present, <code>"false"</code> is assumed.
	 * If the value of this attribute is <code>"true"</code>, the classpath
	 * entry is considered to be on the module path and will be treated as a
	 * regular named module or as an automatic module.
	 *
	 * @since 3.14
	 */
	String MODULE = "module"; //$NON-NLS-1$

	/**
	 * Constant for the name of the add-exports attribute.
	 * 
	 * <p>The value of this attribute must adhere to the syntax of <code>javac's</code>
	 * {@code --add-exports} command line option: {@code <source-module>/<package>=<target-module>(,<target-module>)*}.
	 * Multiple such options are packed as a ':' separated list into a single classpath attribute.
	 * The given exports will be added at compile time.</p>
	 * <p>Classpath entries with this attribute should also have a {@link #MODULE} attribute
	 * with value <code>"true"</code>.</p>
	 * 
	 * @since 3.14
	 */
	String ADD_EXPORTS = "add-exports"; //$NON-NLS-1$

	/**
	 * Constant for the name of the add-reads attribute.
	 * 
	 * <p>The value of this attribute must adhere to the syntax of <code>javac's</code>
	 * {@code --add-reads} command line option: {@code <source-module>=<target-module>}.
	 * The given reads edge will be added at compile time.</p>
	 * 
	 * @since 3.14
	 */
	String ADD_READS = "add-reads"; //$NON-NLS-1$

	/**
	 * Constant for the name of the patch-module attribute.
	 * 
	 * <p>The value of this attribute must be the name of a module defined in the
	 * classpath entry, to which this attribute is attached.</p>
	 * 
	 * <p>This attribute is supported for classpath entries of kind
	 * {@link IClasspathEntry#CPE_CONTAINER}, {@link IClasspathEntry#CPE_LIBRARY}
	 * and {@link IClasspathEntry#CPE_PROJECT}.
	 * A classpath entry having this attribute must also have the
	 * {@link #MODULE} attribute with value <code>"true"</code>.</p>
	 *
	 * @since 3.14
	 */
	String PATCH_MODULE = "patch-module"; //$NON-NLS-1$

	/**
	 * Constant for the name of the limit-modules attribute.
	 * 
	 * <p>The value of this attribute must be a comma-separated list of names of modules
	 * defined in the classpath entry, to which this attribute is attached.
	 * The set of modules observable through this entry will be limited to
	 * the transitive closure of modules in this list.</p>
	 * 
	 * <p>This attribute is supported for classpath entries of kind
	 * {@link IClasspathEntry#CPE_CONTAINER}.
	 * A classpath entry having this attribute must also have the
	 * {@link #MODULE} attribute with value <code>"true"</code>.</p>
	 *
	 * @since 3.14
	 */
	String LIMIT_MODULES = "limit-modules"; //$NON-NLS-1$

	/**
	 * Constant of the name of the module-main-class attribute.
	 * The classpath entry holding this attribute must refer to a source folder
	 * containing the implementation of a module.
	 * 
	 * <p>The value of this attribute must be the name of a class defined in this module.
	 * It will be used for generating the <code>ModuleMainClass</code> attribute
	 * in <code>module-info.class</code>.</p>
	 * 
	 * @since 3.14
	 */
	String MODULE_MAIN_CLASS = "module-main-class"; //$NON-NLS-1$

	/**
	 * Constant for the name of the external annotation path attribute. 
	 * 
	 * <p>The value for this attribute has to be the string representation of a path.
	 * It should point to an existing directory where external annotations can be
	 * found to support annotation based null analysis involving 3rd party libraries.</p>
	 * 
	 * @since 3.11
	 */
	String EXTERNAL_ANNOTATION_PATH = "annotationpath"; //$NON-NLS-1$

	/**
	 * Returns the name of this classpath attribute.
	 *
	 * @return the name of this classpath attribute.
	 * @since 3.1
	 */
	String getName();

	/**
	 * Returns the value of this classpath attribute.
	 *
	 * @return the value of this classpath attribute.
	 * @since 3.1
	 */
	String getValue();

}
