/**********************************************************************
 * Copyright 2008, 2013 Technical University Berlin, Germany and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.internal.compiler.util;

import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * Sorting utilities.
 * Originally developed for the <a href="http://www.eclipse.org/objectteams">Object Teams project</a>.
 */
public class Sorting {

	/**
	 * Topological sort for types
	 * Guarantee: supertypes come before subtypes.
	 */
	public static ReferenceBinding[] sortTypes(ReferenceBinding[] types) {
		int len = types.length;

		ReferenceBinding[] unsorted = new ReferenceBinding[len];
		ReferenceBinding[] sorted = new ReferenceBinding[len];
		System.arraycopy(types, 0, unsorted, 0, len);
		
		int o = 0;
		for(int i=0; i<len; i++)
			o = sort(unsorted, i, sorted, o);

		return sorted;
	}
	// Transfer input[i] and all its supers into output[o] ff.
	private static int sort(ReferenceBinding[] input, int i,
							ReferenceBinding[] output, int o)
	{
		if (input[i] == null)
			return o;

		ReferenceBinding superclass = input[i].superclass();
		o = sortSuper(superclass, input, output, o);

		ReferenceBinding[] superInterfaces = input[i].superInterfaces();
		for (int j=0; j<superInterfaces.length; j++) {
			o = sortSuper(superInterfaces[j], input, output, o);
		}

		// done with supers, now input[i] can safely be transferred:
		output[o++] = input[i];
		input[i] = null;
		return o;
	}
	// if superclass is within the set of types to sort,
	// transfer it and all its supers to output[o] ff.
	private static int sortSuper(ReferenceBinding superclass,
						  		 ReferenceBinding[] input,
						  		 ReferenceBinding[] output, int o)
	{
		if (superclass.id != TypeIds.T_JavaLangObject) {
			// search superclass within input:
			int j = 0;
			for(j=0; j<input.length; j++)
				if (input[j] == superclass)
					break;
			if (j < input.length)
				// depth first traversal:
				o = sort(input, j, output, o);
			// otherwise assume super was already transferred.
		}
		return o;
	}
}