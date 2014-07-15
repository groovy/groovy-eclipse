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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.internal.compiler.ast.*;

public class VariableLocator extends PatternLocator {

protected VariablePattern pattern;

public VariableLocator(VariablePattern pattern) {
	super(pattern);

	this.pattern = pattern;
}
public int match(Expression node, MatchingNodeSet nodeSet) { // interested in Assignment
	if (this.pattern.writeAccess) {
		if (this.pattern.readAccess) return IMPOSSIBLE_MATCH; // already checked the lhs in match(Reference...) before we reached here

		if (node instanceof Assignment) {
			Expression lhs = ((Assignment) node).lhs;
			if (lhs instanceof Reference)
				return matchReference((Reference) lhs, nodeSet, true);
		}
	} else if (this.pattern.readAccess || this.pattern.fineGrain != 0) {
		if (node instanceof Assignment && !(node instanceof CompoundAssignment)) {
			// the lhs of a simple assignment may be added in match(Reference...) before we reach here
			// for example, the fieldRef to 'this.x' in the statement this.x = x; is not considered a readAccess
			char[] lastToken = null;
			Expression lhs = ((Assignment) node).lhs;
			if (lhs instanceof QualifiedNameReference) {
				char[][] tokens = ((QualifiedNameReference)lhs).tokens;
				lastToken = tokens[tokens.length-1];
			}
			if (lastToken == null || matchesName(this.pattern.name, lastToken)) {
				nodeSet.removePossibleMatch(lhs);
				nodeSet.removeTrustedMatch(lhs);
			}
		}
	}
	return IMPOSSIBLE_MATCH;
}
public int match(Reference node, MatchingNodeSet nodeSet) { // interested in NameReference & its subtypes
	return (this.pattern.readAccess || this.pattern.fineGrain != 0)
		? matchReference(node, nodeSet, false)
		: IMPOSSIBLE_MATCH;
}
protected int matchReference(Reference node, MatchingNodeSet nodeSet, boolean writeOnlyAccess) {
	if (node instanceof NameReference) {
		if (this.pattern.name == null) {
			return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
		} else if (node instanceof SingleNameReference) {
			if (matchesName(this.pattern.name, ((SingleNameReference) node).token))
				return nodeSet.addMatch(node, POSSIBLE_MATCH);
		} else {
			QualifiedNameReference qNameRef = (QualifiedNameReference) node;
			char[][] tokens = qNameRef.tokens;
			if (writeOnlyAccess) {
				// in the case of the assigment of a qualified name reference, the match must be on the last token
				if (matchesName(this.pattern.name, tokens[tokens.length-1]))
					return nodeSet.addMatch(node, POSSIBLE_MATCH);
			} else {
				for (int i = 0, max = tokens.length; i < max; i++)
					if (matchesName(this.pattern.name, tokens[i]))
						return nodeSet.addMatch(node, POSSIBLE_MATCH);
			}
		}
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
