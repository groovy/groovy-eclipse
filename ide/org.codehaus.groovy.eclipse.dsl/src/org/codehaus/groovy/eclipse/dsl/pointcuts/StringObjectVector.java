/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.pointcuts;

/**
 * Borrowed from JDT and adapted
 * 
 * @author andrew
 * @created Feb 10, 2011
 */
public final class StringObjectVector {

	static int INITIAL_SIZE = 10;

	public int size;
	public int maxSize;
	private String[] names;
	private Object[] elements;

	public StringObjectVector(int initialSize) {
		this.maxSize = initialSize > 0 ? initialSize : INITIAL_SIZE;
		this.size = 0;
		this.elements = new Object[this.maxSize];
		this.names = new String[this.maxSize];
	}

	public void add(String newName, Object newElement) {

		if (this.size == this.maxSize) { // knows that size starts <= maxSize
			System.arraycopy(this.elements, 0, (this.elements = new Object[this.maxSize *= 2]), 0, this.size);
			System.arraycopy(this.names, 0, (this.names = new String[this.maxSize]), 0, this.size);
		}
		this.names[this.size] = newName;
		this.elements[this.size++] = newElement;
	}


	/**
	 * Equality check
	 */
	public boolean contains(Object element) {
        if (element == null) {
            for (int i = this.size; --i >= 0;)
                if (this.elements[i] == null)
                    return true;
        } else {
            for (int i = this.size; --i >= 0;)
                if (element.equals(this.elements[i]))
                    return true;
        }
		return false;
	}

	/**
	 * Equality check
	 */
	public boolean containsName(String name) {
	    if (name == null) {
            for (int i = this.size; --i >= 0;)
                if (this.names[i] == null)
                    return true;
	    } else {
    	    for (int i = this.size; --i >= 0;)
    	        if (name.equals(this.names[i]))
    	            return true;
	    }
	    return false;
	}
	
	public Object elementAt(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
		return this.elements[index];
	}

	public Object find(String name) {
        if (name == null) {
            for (int i = this.size; --i >= 0;) {
                if (this.names[i] == null) {
                    return this.elements[i];
                }
            }
        } else {
            for (int i = this.size; --i >= 0;) {
                if (name.equals(this.names[i])) {
                    return this.elements[i];
                }
            }
        }
        return null;
	}

	public String toString() {

		String s = ""; //$NON-NLS-1$
		for (int i = 0; i < this.size; i++)
			s += this.names[i] + ":" + this.elements[i].toString() + "\n"; //$NON-NLS-1$
		return s;
	}

    public String nameAt(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return this.names[index];
    }
    
    public Object[] getElements() {
        Object[] res = new Object[size];
        System.arraycopy(this.elements, 0, res, 0, size);
        return res;
    }
    public String[] getNames() {
        String[] res = new String[size];
        System.arraycopy(this.names, 0, res, 0, size);
        return res;
    }
}
