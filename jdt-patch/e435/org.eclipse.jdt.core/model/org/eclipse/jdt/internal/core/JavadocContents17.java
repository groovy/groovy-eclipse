/*******************************************************************************
 * Copyright (c) 2024 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import static org.eclipse.jdt.internal.core.ExternalJavadocSupport.CONSTRUCTOR_NAME;
import static org.eclipse.jdt.internal.core.ExternalJavadocSupport.SECTION_PREFIX_START;
import static org.eclipse.jdt.internal.core.ExternalJavadocSupport.SECTION_SUFFIX;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

/**
 * Javadoc tool format compatible with Java 17 and later versions (up to 21 including)
 */
public class JavadocContents17 extends JavadocContents {

	JavadocContents17(BinaryType type, String content) {
		super(type, content);
	}

	@Override
	protected int[] getAnchorIndex(int fromIndex) {
		int index = CharOperation.indexOf(SECTION_PREFIX_START, this.content, false, fromIndex);
		if (index != -1) {
			return new int[]{index, SECTION_PREFIX_START.length};
		}
		return new int[]{-1, -1};
	}

	@Override
	protected Range computeChildRange(int sectionStart, char[] anchor, int indexOfBottom) {
		Range range = null;

		// try to find the bottom of the section
		if (indexOfBottom != -1) {
			// try to find the end of the section
			// <section class="detail" id="publicField">|
			int javadocStart = sectionStart + anchor.length + 1;
			int indexOfEndSection = CharOperation.indexOf(SECTION_SUFFIX, this.content, false, javadocStart);
			if (indexOfEndSection != -1) {
				int javadocEnd = indexOfEndSection == -1 ? indexOfBottom : Math.min(indexOfEndSection, indexOfBottom);
				range = new Range(javadocStart, javadocEnd);
			} else {
				// the anchor has no suffix
				range = UNKNOWN_FORMAT;
			}
		} else {
			// the detail section has no bottom
			range = UNKNOWN_FORMAT;
		}

		return range;
	}

	@Override
	protected String getMethodName(BinaryMethod method) throws JavaModelException {
		String methodName;
		if (method.isConstructor()) {
			methodName = CONSTRUCTOR_NAME;
		} else {
			methodName = method.getElementName();
		}
		return methodName;
	}

	@Override
	protected String createSignatureAnchor(BinaryMethod method, String methodName, IBinaryMethod info)
			throws JavaModelException {
		String anchor = super.createSignatureAnchor(method, methodName, info);
		anchor = anchor.replaceAll(" ", "");  //$NON-NLS-1$//$NON-NLS-2$
		return anchor;
	}

}
