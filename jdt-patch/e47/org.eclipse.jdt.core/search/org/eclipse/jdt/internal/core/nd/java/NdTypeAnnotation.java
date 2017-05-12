/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.core.nd.IDestructable;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldPointer;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdTypeAnnotation extends NdAnnotation implements IDestructable {
	public static final FieldByte TARGET_TYPE;
	public static final FieldByte TARGET_ARG0;
	public static final FieldByte TARGET_ARG1;
	public static final FieldByte PATH_LENGTH;
	public static final FieldPointer PATH;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeAnnotation> type;

	static {
		type = StructDef.create(NdTypeAnnotation.class, NdAnnotation.type);
		TARGET_TYPE = type.addByte();
		TARGET_ARG0 = type.addByte();
		TARGET_ARG1 = type.addByte();
		PATH_LENGTH = type.addByte();
		PATH = type.addPointer();
		type.done();
	}

	private static final byte[] NO_TYPE_PATH = new byte[0];

	public NdTypeAnnotation(Nd nd, long address) {
		super(nd, address);
	}

	public void setPath(byte[] path) {
		freePath();
		PATH_LENGTH.put(this.nd, this.address, (byte) path.length);
		if (path.length > 0) {
			long pathArray = this.nd.getDB().malloc(path.length, Database.POOL_MISC);
			PATH.put(this.nd, this.address, pathArray);
			this.nd.getDB().putBytes(pathArray, path, path.length);
		}
	}

	public void setTargetInfo(int arg) {
		TARGET_ARG0.put(getNd(), this.address, (byte)((arg >> 8) & 0xff));
		TARGET_ARG1.put(getNd(), this.address, (byte)(arg & 0xff));
	}

	public byte getTargetInfoArg0() {
		return TARGET_ARG0.get(getNd(), this.address);
	}

	public byte getTargetInfoArg1() {
		return TARGET_ARG1.get(getNd(), this.address);
	}

	public int getTarget() {
		int arg0 = TARGET_ARG0.get(getNd(), this.address) & 0xff;
		int arg1 = TARGET_ARG1.get(getNd(), this.address) & 0xff;
		int result = (arg0 << 8) | arg1;
		return result;
	}

	public void setTargetInfo(byte arg0, byte arg1) {
		TARGET_ARG0.put(getNd(), this.address, arg0);
		TARGET_ARG1.put(getNd(), this.address, arg1);
	}

	/**
	 * @param targetType one of the constants from {@link AnnotationTargetTypeConstants}
	 */
	public void setTargetType(int targetType) {
		TARGET_TYPE.put(getNd(), this.address, (byte)targetType);
	}

	/**
	 * @return one of the constants from {@link AnnotationTargetTypeConstants}
	 */
	public int getTargetType() {
		return TARGET_TYPE.get(getNd(), this.address);
	}

	public byte[] getTypePath() {
		long pathPointer = PATH.get(getNd(), this.address);
		if (pathPointer == 0) {
			return NO_TYPE_PATH;
		}
		int pathLength = PATH_LENGTH.get(getNd(), this.address);
		byte[] result = new byte[pathLength];
		getNd().getDB().getBytes(pathPointer, result);
		return result;
	}

	@Override
	public void destruct() {
		freePath();
	}

	private void freePath() {
		long pathPointer = PATH.get(this.nd, this.address);
		this.nd.getDB().free(pathPointer, Database.POOL_MISC);
	}
}
