package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;

import junit.framework.TestCase;

public class StringLiteralTest extends TestCase {
	public void testAppend() {
		StringLiteral l1 = new StringLiteral(new char[] { 'a' }, 2, 3, 4);
		StringLiteral l2 = new StringLiteral(new char[] { 'b' }, 5, 6, 7);
		StringLiteral l3 = new StringLiteral(new char[] { 'c' }, 8, 9, 10);
		ExtendedStringLiteral extendWithString = l1.extendWith(l2).extendWith(l3);
		ExtendedStringLiteral extendWithString2 = l1.extendWith(l2.extendWith(l3));
		StringLiteralConcatenation extendsWithString = l1.extendsWith(l2).extendsWith(l3);
		StringLiteralConcatenation extendsWithString23 = l2.extendsWith(l3);
		StringLiteralConcatenation extendsWithString3 = l1.extendsWith(extendsWithString23);
		ExtendedStringLiteral extendWithChar = l1.extendWith(new CharLiteral("'\\n'".toCharArray(), 5, 6))
				.extendWith(new CharLiteral("'\\t'".toCharArray(), 8, 9));
		List<StringLiteral> allLiterals = List.of(extendWithString, extendWithString2, extendsWithString, extendsWithString3);
		for (StringLiteral l : allLiterals) {
			String expected = (l == extendWithChar) ? "a\n\t" : "abc";
			assertEquals(expected, new String(l.source()));
			assertEquals(2, l.sourceStart);
			assertEquals(9, l.sourceEnd);
			assertEquals(4 - 1, l.getLineNumber());
			l.computeConstant();
			assertEquals(expected, l.constant.stringValue());
		}

		StringLiteralConcatenation extendsWithString2 = l1.extendsWith(l2);
		StringLiteral[] literals2 = extendsWithString2.getLiterals();
		assertEquals(2, literals2.length);
		assertEquals(l1, literals2[0]);
		assertEquals(l2, literals2[1]);
		StringLiteral[] literals = extendsWithString.getLiterals();
		assertEquals(3, literals.length);
		assertEquals(l1, literals[0]);
		assertEquals(l2, literals[1]);
		assertEquals(l3, literals[2]);
		StringLiteral[] literals3 = extendsWithString3.getLiterals();
		// undocumented why extendsWith(StringLiteralConcatenation) does not flatten the StringLiteralConcatenation - just keep it as is:
		assertEquals(2, literals3.length);
		assertEquals(l1, literals3[0]);
		assertEquals(extendsWithString23, literals3[1]);
	}
}
