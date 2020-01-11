/*******************************************************************************
 * Copyright (c) 2010, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *
 * Based on lookup3.c, by Bob Jenkins {@link "http://burtleburtle.net/bob/c/lookup3.c"}
 *
 * Here is the original comment by Bob Jenkins:
 * -------------------------------------------------------------------------------
 * lookup3.c, by Bob Jenkins, May 2006, Public Domain.
 *
 * These are functions for producing 32-bit hashes for hash table lookup.
 * hashword(), hashlittle(), hashlittle2(), hashbig(), mix(), and final()
 * are externally useful functions.  Routines to test the hash are included
 * if SELF_TEST is defined.  You can use this free for any purpose.  It's in
 * the public domain.  It has no warranty.
 *
 * You probably want to use hashlittle().  hashlittle() and hashbig()
 * hash byte arrays.  hashlittle() is is faster than hashbig() on
 * little-endian machines.  Intel and AMD are little-endian machines.
 * On second thought, you probably want hashlittle2(), which is identical to
 * hashlittle() except it returns two 32-bit hashes for the price of one.
 * You could implement hashbig2() if you wanted but I haven't bothered here.
 *
 * If you want to find a hash of, say, exactly 7 integers, do
 *   a = i1;  b = i2;  c = i3;
 *   mix(a, b, c);
 *   a += i4; b += i5; c += i6;
 *   mix(a, b, c);
 *   a += i7;
 *   finalMix(a, b, c);
 * then use c as the hash value.  If you have a variable length array of
 * 4-byte integers to hash, use hashword().  If you have a byte array (like
 * a character string), use hashlittle().  If you have several byte arrays, or
 * a mix of things, see the comments above hashlittle().
 *
 * Why is this so big?  I read 12 bytes at a time into 3 4-byte integers,
 * then mix those integers.  This is fast (you can do a lot more thorough
 * mixing with 12*3 instructions on 3 integers than you can with 3 instructions
 * on 1 byte), but shoehorning those bytes into integers efficiently is messy.
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

/**
 * Computes a 64-bit hash value of a character stream that can be supplied one chunk at a time.
 * Usage:
 * <pre>
 *   StreamHasher hasher = new StreamHasher();
 *   for (long offset = 0; offset < streamLength; offset += chunkLength) {
 *     hasher.addChunk(offset, chunkOfCharacters);
 *   }
 *   int64 hashValue = hasher.computeHash();
 * </pre>
 *
 * Based on lookup3.c by Bob Jenkins from {@link "http://burtleburtle.net/bob/c/lookup3.c"}
 */
public final class StreamHasher {
	private static final long SEED = 3141592653589793238L;  // PI
	private static final long EMPTY_STRING_HASH = new StreamHasher().computeHashInternal();

	long hashedOffset;  // Current position in the stream of characters.
	int state;  // Current position in the stream of characters modulo 6, or -1 after computeHash is called.
	int a;
	int b;
	int c;
	char previousCharacter;

	public StreamHasher() {
		// Set up the internal state.
		this.hashedOffset = 0;
		this.state = 0;
		this.a = this.b = this.c = (int) SEED;
		this.c += SEED >>> 32;
	}

	/**
	 * Adds a chunk of data to the hasher.
	 * @param chunk Contents of the chunk.
	 */
	public void addChunk(char[] chunk) {
		for (int pos = 0; pos < chunk.length; pos++, this.hashedOffset++) {
			char cc = chunk[pos];
			switch (this.state++) {
			case -1:
				throw new IllegalStateException("addChunk is called after computeHash."); //$NON-NLS-1$
			case 0:
			case 2:
			case 4:
				this.previousCharacter = cc;
				break;
			case 1:
				this.a += this.previousCharacter | (cc << 16);
				break;
			case 3:
				this.b += this.previousCharacter | (cc << 16);
				break;
			case 5:
				this.c += this.previousCharacter | (cc << 16);
				mix();
				this.state = 0;
				break;
			}
		}
	}

	/**
	 * Computes and returns the hash value. Must be called once after the last chunk.
	 * @return The hash value of the character stream.
	 */
	public long computeHash() {
		if (this.state < 0) {
			throw new IllegalStateException("computeHash method is called more than once."); //$NON-NLS-1$
		}
		return computeHashInternal() ^ EMPTY_STRING_HASH;
	}

	private long computeHashInternal() {
		switch (this.state) {
		case 1:
			this.a += this.previousCharacter;
			break;
		case 3:
			this.b += this.previousCharacter;
			break;
		case 5:
			this.c += this.previousCharacter;
			break;
		}
		this.state = -1;  // Protect against subsequent calls.
		finalMix();
		return (this.c & 0xFFFFFFFFL) | ((long) this.b << 32);
	}

	/**
	 * Computes a 64-bit hash value of a String. The resulting hash value
	 * is zero if the string is empty.
	 *
	 * @param str The string to hash.
	 * @return The hash value.
	 */
	public static long hash(String str) {
		StreamHasher hasher = new StreamHasher();
		hasher.addChunk(str.toCharArray());
		return hasher.computeHash();
	}

	/**
	 * Mixes three 32-bit values reversibly.
	 *
	 * This is reversible, so any information in a, b, c before mix() is
	 * still in a, b, c after mix().
     *
     * If four pairs of a, b, c inputs are run through mix(), or through
	 * mix() in reverse, there are at least 32 bits of the output that
	 * are sometimes the same for one pair and different for another pair.
	 * This was tested for:
	 * * pairs that differed by one bit, by two bits, in any combination
	 *   of top bits of a, b, c, or in any combination of bottom bits of
	 *   a, b, c.
	 * * "differ" is defined as +, -, ^, or ~^.  For + and -, I transformed
	 *   the output delta to a Gray code (a ^ (a >> 1)) so a string of 1's
	 *   (as is commonly produced by subtraction) look like a single 1-bit
	 *   difference.
	 * * the base values were pseudo-random, all zero but one bit set, or
	 *   all zero plus a counter that starts at zero.
     *
	 * Some k values for my "a -= c; a ^= Integer.rotateLeft(c, k); c += b;"
	 * arrangement that satisfy this are
	 *     4  6  8 16 19  4
	 *     9 15  3 18 27 15
	 *    14  9  3  7 17  3
	 * Well, "9 15 3 18 27 15" didn't quite get 32 bits diffing
	 * for "differ" defined as + with a one-bit base and a two-bit delta.
	 * I used http://burtleburtle.net/bob/hash/avalanche.html to choose
	 * the operations, constants, and arrangements of the variables.
     *
	 * This does not achieve avalanche.  There are input bits of a, b, c
	 * that fail to affect some output bits of a, b, c, especially of a.
	 * The most thoroughly mixed value is c, but it doesn't really even
	 * achieve avalanche in c.
     *
	 * This allows some parallelism.  Read-after-writes are good at doubling
	 * the number of bits affected, so the goal of mixing pulls in the opposite
	 * direction as the goal of parallelism.  I did what I could.  Rotates
	 * seem to cost as much as shifts on every machine I could lay my hands
	 * on, and rotates are much kinder to the top and bottom bits, so I used
	 * rotates.
	 */
	private void mix() {
		this.a -= this.c;  this.a ^= Integer.rotateLeft(this.c, 4);  this.c += this.b;
		this.b -= this.a;  this.b ^= Integer.rotateLeft(this.a, 6);  this.a += this.c;
		this.c -= this.b;  this.c ^= Integer.rotateLeft(this.b, 8);  this.b += this.a;
		this.a -= this.c;  this.a ^= Integer.rotateLeft(this.c, 16); this.c += this.b;
		this.b -= this.a;  this.b ^= Integer.rotateLeft(this.a, 19); this.a += this.c;
		this.c -= this.b;  this.c ^= Integer.rotateLeft(this.b, 4);  this.b += this.a;
	}

	/**
	 * Final mixing of 3 32-bit values a, b, c into c
     *
	 * Pairs of a, b, c values differing in only a few bits will usually
	 * produce values of c that look totally different.  This was tested for
	 * * pairs that differed by one bit, by two bits, in any combination
	 *   of top bits of a, b, c, or in any combination of bottom bits of
	 *   a, b, c.
	 * * "differ" is defined as +, -, ^, or ~^.  For + and -, I transformed
	 *   the output delta to a Gray code (a ^ (a >> 1)) so a string of 1's (as
	 *   is commonly produced by subtraction) look like a single 1-bit
	 *   difference.
	 * * the base values were pseudo-random, all zero but one bit set, or
	 *   all zero plus a counter that starts at zero.
	 *
	 * These constants passed:
	 *  14 11 25 16 4 14 24
	 *  12 14 25 16 4 14 24
	 * and these came close:
	 *   4  8 15 26 3 22 24
	 *  10  8 15 26 3 22 24
	 *  11  8 15 26 3 22 24
	 */
	private void finalMix() {
		this.c ^= this.b; this.c -= Integer.rotateLeft(this.b, 14);
		this.a ^= this.c; this.a -= Integer.rotateLeft(this.c, 11);
		this.b ^= this.a; this.b -= Integer.rotateLeft(this.a, 25);
		this.c ^= this.b; this.c -= Integer.rotateLeft(this.b, 16);
		this.a ^= this.c; this.a -= Integer.rotateLeft(this.c, 4);
		this.b ^= this.a; this.b -= Integer.rotateLeft(this.a, 14);
		this.c ^= this.b; this.c -= Integer.rotateLeft(this.b, 24);
	}
}
