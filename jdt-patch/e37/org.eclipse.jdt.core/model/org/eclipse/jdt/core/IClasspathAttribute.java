/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
