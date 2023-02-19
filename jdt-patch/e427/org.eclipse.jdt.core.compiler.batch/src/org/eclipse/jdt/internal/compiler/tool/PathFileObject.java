/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *	 IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;

/**
 * Implementation of a Java file object that corresponds to a file on the file system
 */
public class PathFileObject implements JavaFileObject {
	Path path;
	private Charset charset;
	private Kind kind;

	public PathFileObject(Path path, Kind kind, Charset charset) {
		this.path = path;
		this.kind = kind;
		this.charset = charset;
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileObject#getAccessLevel()
	 */
	@Override
	public Modifier getAccessLevel() {
		// cannot express multiple modifier
		if (getKind() != Kind.CLASS) {
			return null;
		}
		ClassFileReader reader = null;
		try {
			reader = readFromPath(this.path);
		} catch (ClassFormatException e) {
			// ignore
		} catch (IOException e) {
			String error = "Failed to read access level from " + this.path; //$NON-NLS-1$
			if (JRTUtil.PROPAGATE_IO_ERRORS) {
				throw new IllegalStateException(error, e);
			} else {
				System.err.println(error);
				e.printStackTrace();
			}
		}
		if (reader == null) {
			return null;
		}
		final int accessFlags = reader.accessFlags();
		if ((accessFlags & ClassFileConstants.AccPublic) != 0) {
			return Modifier.PUBLIC;
		}
		if ((accessFlags & ClassFileConstants.AccAbstract) != 0) {
			return Modifier.ABSTRACT;
		}
		if ((accessFlags & ClassFileConstants.AccFinal) != 0) {
			return Modifier.FINAL;
		}
		return null;
	}
	private ClassFileReader readFromPath(Path p) throws ClassFormatException, IOException {
		return new ClassFileReader(this.path.toUri(), Files.readAllBytes(this.path), this.path.getFileName().toString().toCharArray());
	}
	/* (non-Javadoc)
	 * @see javax.tools.JavaFileObject#getNestingKind()
	 */
	@Override
	public NestingKind getNestingKind() {
		switch(this.kind) {
			case SOURCE :
				return NestingKind.TOP_LEVEL;
			case CLASS :
				ClassFileReader reader = null;
				try {
					reader = readFromPath(this.path);
				} catch (ClassFormatException e) {
					// ignore
				} catch (IOException e) {
					String error = "Failed to read access nesting kind from " + this.path; //$NON-NLS-1$
					if (JRTUtil.PROPAGATE_IO_ERRORS) {
						throw new IllegalStateException(error, e);
					} else {
						System.err.println(error);
						e.printStackTrace();
					}
				}
				if (reader == null) {
					return null;
				}
				if (reader.isAnonymous()) {
					return NestingKind.ANONYMOUS;
				}
				if (reader.isLocal()) {
					return NestingKind.LOCAL;
				}
				if (reader.isMember()) {
					return NestingKind.MEMBER;
				}
				return NestingKind.TOP_LEVEL;
			default:
				return null;
		}
	}

	/**
	 * @see javax.tools.FileObject#delete()
	 */
	@Override
	public boolean delete() {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PathFileObject)) {
			return false;
		}
		PathFileObject PathFileObject = (PathFileObject) o;
		return PathFileObject.toUri().equals(this.path.toUri());
	}

	/**
	 * @see javax.tools.FileObject#getCharContent(boolean)
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return Util.getCharContents(this, ignoreEncodingErrors,
				org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(this.path.toFile()), this.charset.name());
	}

	/**
	 * @see javax.tools.FileObject#getLastModified()
	 */
	@Override
	public long getLastModified() {
		return this.path.toFile().lastModified();
	}

	@Override
	public String getName() {
		return this.path.toString();
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

	/**
	 * @see javax.tools.FileObject#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		return Files.newInputStream(this.path);
	}

	/**
	 * @see javax.tools.FileObject#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		return Files.newOutputStream(this.path);
	}

	/**
	 * @see javax.tools.FileObject#openReader(boolean)
	 */
	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return Files.newBufferedReader(this.path);
	}

	/**
	 * @see javax.tools.FileObject#openWriter()
	 */
	@Override
	public Writer openWriter() throws IOException {
		return Files.newBufferedWriter(this.path);
	}

	@Override
	public String toString() {
		return this.path.toString();
	}

	@Override
	public URI toUri() {
		return this.path.toUri();
	}

	@Override
	public Kind getKind() {
		return this.kind;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind k) {
		String fileName = this.path.getFileName().toString();
		return fileName.endsWith(simpleName + k.extension);
	}
}
