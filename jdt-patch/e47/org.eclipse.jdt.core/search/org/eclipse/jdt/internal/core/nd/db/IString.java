/*******************************************************************************
 * Copyright (c) 2006, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

/**
 * Interface for strings stored in the database. There is more than one string
 * format. This interface hides that fact. 
 * 
 * @author Doug Schaefer
 */
public interface IString {
	/**
	 * Get the offset of this IString record in the Nd
	 */
	public long getRecord();
	
	// strcmp equivalents
	/**
	 * Compare this IString record and the specified IString record
	 * @param string
	 * @param caseSensitive whether to compare in a case-sensitive way
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws IndexException
	 */
	public int compare(IString string, boolean caseSensitive) throws IndexException;
	
	/**
	 * Compare this IString record and the specified String object
	 * @param string
	 * @param caseSensitive whether to compare in a case-sensitive way
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws IndexException
	 */
	public int compare(String string, boolean caseSensitive) throws IndexException;
	
	/**
	 * Compare this IString record and the specified character array
	 * @param chars
	 * @param caseSensitive whether to compare in a case-sensitive way
	 * @return <ul><li> -1 if this &lt; chars
	 * <li> 0 if this == chars
	 * <li> 1 if this &gt; chars
	 * </ul>
	 * @throws IndexException
	 */
	public int compare(char[] chars, boolean caseSensitive) throws IndexException;

	/**
	 * Compare this IString record and the specified IString record in a case sensitive manner
	 * such that it is compatible with case insensitive comparison.
	 * @param string
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws IndexException
	 */
	public int compareCompatibleWithIgnoreCase(IString string) throws IndexException;

	/**
	 * Compare this IString record and the specified char array in a case sensitive manner
	 * such that it is compatible with case insensitive comparison.
	 * @param chars
	 * @return <ul><li> -1 if this &lt; string
	 * <li> 0 if this == string
	 * <li> 1 if this &gt; string
	 * </ul>
	 * @throws IndexException
	 */
	public int compareCompatibleWithIgnoreCase(char[] chars) throws IndexException;
	
	/**
	 * Compare this IString record and the specified character array
	 * @param name the name to compare to
	 * @param caseSensitive whether to compare in a case-sensitive way
	 * @return <ul><li> -1 if this &lt; chars
	 * <li> 0 if this has a prefix chars
	 * <li> 1 if this &gt; chars and does not have the prefix
	 * </ul>
	 * @throws IndexException
	 */
	public int comparePrefix(char[] name, boolean caseSensitive) throws IndexException;

	/**
	 * Get an equivalent character array to this IString record<p>
	 * <b>N.B. This method can be expensive: compare and equals can be used for
	 * efficient comparisons</b>
	 * @return an equivalent character array to this IString record
	 * @throws IndexException
	 */
	public char[] getChars() throws IndexException;
	
	/**
	 * Get an equivalent String object to this IString record<p>
	 * <b>N.B. This method can be expensive: compare and equals can be used for
	 * efficient comparisons</b>
	 * @return an equivalent String object to this IString record
	 * @throws IndexException
	 */
	public String getString() throws IndexException;
	
	/**
	 * Free the associated record in the Nd
	 * @throws IndexException
	 */
	public void delete() throws IndexException;

	/**
	 * @return the length of the string
	 */
	public int length();
}
