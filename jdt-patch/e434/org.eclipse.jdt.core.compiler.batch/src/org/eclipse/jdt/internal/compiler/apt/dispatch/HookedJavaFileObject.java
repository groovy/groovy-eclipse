/*******************************************************************************
 * Copyright (c) 2006, 2023 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileObject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * A delegating JavaFileObject that hooks the close() methods of the Writer
 * or OutputStream objects that it produces, and notifies the annotation
 * dispatch manager when a new compilation unit is produced.
 */
public class HookedJavaFileObject extends
		ForwardingJavaFileObject<JavaFileObject>
{
	// A delegating Writer that passes all commands to its contained Writer,
	// but hooks close() to notify the annotation dispatch manager of the new unit.
	private class ForwardingWriter extends Writer {
		private final Writer _w;
		ForwardingWriter(Writer w) {
			this._w = w;
		}
		@Override
		public Writer append(char c) throws IOException {
			return this._w.append(c);
		}
		@Override
		public Writer append(CharSequence csq, int start, int end)
				throws IOException {
			return this._w.append(csq, start, end);
		}
		@Override
		public Writer append(CharSequence csq) throws IOException {
			return this._w.append(csq);
		}
		// This is the only interesting method - it has to notify the
		// dispatch manager of the new file.
		@Override
		public void close() throws IOException {
			this._w.close();
			closed();
		}
		@Override
		public void flush() throws IOException {
			this._w.flush();
		}
		@Override
		public void write(char[] cbuf) throws IOException {
			this._w.write(cbuf);
		}
		@Override
		public void write(int c) throws IOException {
			this._w.write(c);
		}
		@Override
		public void write(String str, int off, int len)
				throws IOException {
			this._w.write(str, off, len);
		}
		@Override
		public void write(String str) throws IOException {
			this._w.write(str);
		}
		@Override
		public void write(char[] cbuf, int off, int len)
		throws IOException {
			this._w.write(cbuf, off, len);
		}
		@Override
		protected Object clone() throws CloneNotSupportedException {
			return new ForwardingWriter(this._w);
		}
		@Override
		public int hashCode() {
			return this._w.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ForwardingWriter other = (ForwardingWriter) obj;
			if (this._w == null) {
				if (other._w != null)
					return false;
			} else if (!this._w.equals(other._w))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "ForwardingWriter wrapping " + this._w.toString(); //$NON-NLS-1$
		}
	}

	// A delegating Writer that passes all commands to its contained Writer,
	// but hooks close() to notify the annotation dispatch manager of the new unit.
	private class ForwardingOutputStream extends OutputStream {
		private final OutputStream _os;

		ForwardingOutputStream(OutputStream os) {
			this._os = os;
		}

		@Override
		public void close() throws IOException {
			this._os.close();
			closed();
		}
		@Override
		public void flush() throws IOException {
			this._os.flush();
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this._os.write(b, off, len);
		}
		@Override
		public void write(byte[] b) throws IOException {
			this._os.write(b);
		}
		@Override
		public void write(int b) throws IOException {
			this._os.write(b);
		}
		@Override
		protected Object clone() throws CloneNotSupportedException {
			return new ForwardingOutputStream(this._os);
		}
		@Override
		public int hashCode() {
			return this._os.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ForwardingOutputStream other = (ForwardingOutputStream) obj;
			if (this._os == null) {
				if (other._os != null)
					return false;
			} else if (!this._os.equals(other._os))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "ForwardingOutputStream wrapping " + this._os.toString(); //$NON-NLS-1$
		}
	}

	/**
	 * The Filer implementation that we need to notify when a new file is created.
	 */
	protected final BatchFilerImpl _filer;

	/**
	 * The name of the file that is created; this is passed to the CompilationUnit constructor,
	 * and ultimately to the java.io.File constructor, so it is a normal pathname, just like
	 * what would be on the compiler command line.
	 */
	protected final String _fileName;



	/**
	 * A compilation unit is created when the writer or stream is closed.  Only do this once.
	 */
	private boolean _closed = false;

	private final String _typeName;

	public HookedJavaFileObject(JavaFileObject fileObject, String fileName, String typeName, BatchFilerImpl filer) {
		super(fileObject);
		this._filer = filer;
		this._fileName = fileName;
		this._typeName = typeName;
	}

	@SuppressWarnings("resource") // ForwardingOutputStream forwards close() too
	@Override
	public OutputStream openOutputStream() throws IOException {
		return new ForwardingOutputStream(super.openOutputStream());
	}

	@SuppressWarnings("resource") // ForwardingWriter forwards close() too
	@Override
	public Writer openWriter() throws IOException {
		return new ForwardingWriter(super.openWriter());
	}

	protected void closed() {
		if (!this._closed) {
			this._closed = true;
			//TODO: support encoding
			switch(this.getKind()) {
				case SOURCE :
					CompilationUnit unit = new CompilationUnit(null, this._fileName, null /* encoding */, null, this._filer._env.shouldIgnoreOptionalProblems(this._fileName.toCharArray()), null);
					this._filer.addNewUnit(unit);
					break;
				case CLASS :
					IBinaryType binaryType = null;
					try {
						binaryType = ClassFileReader.read(this._fileName);
					} catch (ClassFormatException e) {
						/* When the annotation processor produces garbage, javac seems to show some resilience, by hooking the source type,
						   which since is resolved can answer annotations during discovery - Not sure if this sanctioned by the spec, to be taken
						   up with Oracle. Here we mimic the bug, see that addNewClassFile is simply collecting ReferenceBinding's, so adding
						   a SourceTypeBinding works just fine.
						*/
						ReferenceBinding type = this._filer._env._compiler.lookupEnvironment.getType(CharOperation.splitOn('.', this._typeName.toCharArray()));
						if (type != null)
							this._filer.addNewClassFile(type);
					} catch (IOException e) {
						// ignore
					}
					if (binaryType != null) {
						char[] name = binaryType.getName();
						ReferenceBinding type = this._filer._env._compiler.lookupEnvironment.getType(CharOperation.splitOn('/', name));
						if (type != null && type.isValidBinding()) {
							if (type.isBinaryBinding()) {
								this._filer.addNewClassFile(type);
							} else {
								BinaryTypeBinding binaryBinding = new BinaryTypeBinding(type.getPackage(), binaryType, this._filer._env._compiler.lookupEnvironment, true);
								this._filer.addNewClassFile(binaryBinding);
							}
						}
					}
					break;
				case HTML:
				case OTHER:
					break;
			}
		}
	}
}
