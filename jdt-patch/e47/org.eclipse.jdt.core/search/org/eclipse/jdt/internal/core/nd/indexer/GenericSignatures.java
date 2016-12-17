/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.indexer;

import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.SignatureWrapper;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

/**
 * Contains static factory methods for constructing {@link SignatureWrapper} from various types.
 */
public class GenericSignatures {
	private static final char[][] EMPTY_CHAR_ARRAY_ARRAY = new char[0][];

	public static SignatureWrapper getGenericSignature(IBinaryMethod next) {
		char[] signature = next.getGenericSignature();
		if (signature == null) {
			signature = next.getMethodDescriptor();
		}

		return new SignatureWrapper(signature);
	}

	/**
	 * Returns the generic signature for the given field. If the field has no generic signature, one is generated
	 * from the type's field descriptor.
	 */
	public static SignatureWrapper getGenericSignature(IBinaryType binaryType) {
		char[][] interfaces = binaryType.getInterfaceNames();
		if (interfaces == null) {
			interfaces = EMPTY_CHAR_ARRAY_ARRAY;
		}
		char[] genericSignature = binaryType.getGenericSignature();
		if (genericSignature == null) {
			int startIndex = binaryType.getSuperclassName() != null ? 3 : 0;
			char[][] toCatenate = new char[startIndex + (interfaces.length * 3)][];
			char[] prefix = new char[]{'L'};
			char[] suffix = new char[]{';'};

			if (binaryType.getSuperclassName() != null) {
				toCatenate[0] = prefix;
				toCatenate[1] = binaryType.getSuperclassName();
				toCatenate[2] = suffix;
			}

			for (int idx = 0; idx < interfaces.length; idx++) {
				int catIndex = startIndex + idx * 3;
				toCatenate[catIndex] = prefix;
				toCatenate[catIndex + 1] = interfaces[idx];
				toCatenate[catIndex + 2] = suffix;
			}

			genericSignature = CharArrayUtils.concat(toCatenate);
		}

		SignatureWrapper signatureWrapper = new SignatureWrapper(genericSignature);
		return signatureWrapper;
	}

	/**
	 * Returns the generic signature for the given field. If the field has no generic signature, one is generated
	 * from the type's field descriptor.
	 */
	static SignatureWrapper getGenericSignatureFor(IBinaryField nextField) {
		char[] signature = nextField.getGenericSignature();
		if (signature == null) {
			signature = nextField.getTypeName();
		}
		return new SignatureWrapper(signature);
	}

}
