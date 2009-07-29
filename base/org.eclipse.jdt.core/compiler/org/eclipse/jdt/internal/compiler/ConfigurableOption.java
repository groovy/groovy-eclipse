/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

/**
 * Generic option description, which can be modified independently from the
 * component it belongs to.
 * 
 * @deprecated backport 1.0 internal functionality
 */

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.compiler.CharOperation;

public class ConfigurableOption {
	private String componentName;
	private String optionName;
	private int id;

	private String category;
	private String name;
	private String description;
	private int currentValueIndex;
	private String[] possibleValues;

	// special value for <possibleValues> indicating that 
	// the <currentValueIndex> is the actual value
	public final static String[] NoDiscreteValue = {}; 
/**
 * INTERNAL USE ONLY
 *
 * Initialize an instance of this class according to a specific locale
 *
 * @param loc java.util.Locale
 */
public ConfigurableOption(
	String componentName, 
	String optionName, 
	Locale loc, 
	int currentValueIndex) {

	this.componentName = componentName;
	this.optionName = optionName;
	this.currentValueIndex = currentValueIndex;
		
	ResourceBundle resource = null;
	try {
		String location = componentName.substring(0, componentName.lastIndexOf('.'));
		resource = ResourceBundle.getBundle(location + ".options", loc); //$NON-NLS-1$
	} catch (MissingResourceException e) {
		category = "Missing ressources entries for" + componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
		name = "Missing ressources entries for"+ componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
		description = "Missing ressources entries for" + componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
		possibleValues = CharOperation.NO_STRINGS;
		id = -1;
	}
	if (resource == null) return;
	try {
		id = Integer.parseInt(resource.getString(optionName + ".number")); //$NON-NLS-1$
	} catch (MissingResourceException e) {
		id = -1;
	} catch (NumberFormatException e) {
		id = -1;
	}
	try {
		category = resource.getString(optionName + ".category"); //$NON-NLS-1$
	} catch (MissingResourceException e) {
		category = "Missing ressources entries for" + componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	try {
		name = resource.getString(optionName + ".name"); //$NON-NLS-1$
	} catch (MissingResourceException e) {
		name = "Missing ressources entries for"+ componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	try {
		StringTokenizer tokenizer = new StringTokenizer(resource.getString(optionName + ".possibleValues"), "|"); //$NON-NLS-1$ //$NON-NLS-2$
		int numberOfValues = Integer.parseInt(tokenizer.nextToken());
		if(numberOfValues == -1){
			possibleValues = NoDiscreteValue;
		} else {
			possibleValues = new String[numberOfValues];
			int index = 0;
			while (tokenizer.hasMoreTokens()) {
				possibleValues[index] = tokenizer.nextToken();
				index++;
			}
		}
	} catch (MissingResourceException e) {
		possibleValues = CharOperation.NO_STRINGS;
	} catch (NoSuchElementException e) {
		possibleValues = CharOperation.NO_STRINGS;
	} catch (NumberFormatException e) {
		possibleValues = CharOperation.NO_STRINGS;
	}
	try {
		description = resource.getString(optionName + ".description");  //$NON-NLS-1$
	} catch (MissingResourceException e) {
		description = "Missing ressources entries for"+ componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
/**
 * Return a String that represents the localized category of the receiver.
 * @return java.lang.String
 */
public String getCategory() {
	return category;
}
/**
 * Return a String that identifies the component owner (typically the qualified
 *	type name of the class which it corresponds to).
 *
 * e.g. "org.eclipse.jdt.internal.compiler.api.Compiler"
 *
 * @return java.lang.String
 */
public String getComponentName() {
	return componentName;
}
/**
 * Answer the index (in possibleValues array) of the current setting for this
 * particular option.
 *
 * In case the set of possibleValues is NoDiscreteValue, then this index is the
 * actual value (e.g. max line lenght set to 80).
 *
 * @return int
 */
public int getCurrentValueIndex() {
	return currentValueIndex;
}
/**
 * Return an String that represents the localized description of the receiver.
 *
 * @return java.lang.String
 */
public String getDescription() {
	return description;
}
/**
 * Internal ID which allows the configurable component to identify this particular option.
 *
 * @return int
 */
public int getID() {
	return id;
}
/**
 * Return a String that represents the localized name of the receiver.
 * @return java.lang.String
 */
public String getName() {
	return name;
}
/**
 * Return an array of String that represents the localized possible values of the receiver.
 * @return java.lang.String[]
 */
public String[] getPossibleValues() {
	return possibleValues;
}
/**
 * Change the index (in possibleValues array) of the current setting for this
 * particular option.
 *
 * In case the set of possibleValues is NoDiscreteValue, then this index is the
 * actual value (e.g. max line lenght set to 80).
 */
public void setValueIndex(int newIndex) {
	currentValueIndex = newIndex;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Configurable option for "); //$NON-NLS-1$ 
	buffer.append(this.componentName).append("\n"); //$NON-NLS-1$ 
	buffer.append("- category:			").append(this.category).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	buffer.append("- name:				").append(this.name).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	/* display current value */
	buffer.append("- current value:	"); //$NON-NLS-1$ 
	if (possibleValues == NoDiscreteValue){
		buffer.append(this.currentValueIndex);
	} else {
		buffer.append(this.possibleValues[this.currentValueIndex]);
	}
	buffer.append("\n"); //$NON-NLS-1$ 
	
	/* display possible values */
	if (possibleValues != NoDiscreteValue){
		buffer.append("- possible values:	["); //$NON-NLS-1$ 
		for (int i = 0, max = possibleValues.length; i < max; i++) {
			if (i != 0)
				buffer.append(", "); //$NON-NLS-1$ 
			buffer.append(possibleValues[i]);
		}
		buffer.append("]\n"); //$NON-NLS-1$ 
		buffer.append("- curr. val. index:	").append(currentValueIndex).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	buffer.append("- description:		").append(description).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
	/**
	 * Gets the optionName.
	 * @return Returns a String
	 */
	public String getOptionName() {
		return optionName;
	}
}
