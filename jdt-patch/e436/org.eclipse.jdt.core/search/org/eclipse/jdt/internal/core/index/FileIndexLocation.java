/*******************************************************************************
 * Copyright (c) 2011, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class FileIndexLocation extends IndexLocation {
	final File indexFile;
	final Path indexPath;

	public FileIndexLocation(File file) {
		super(file);
		this.indexFile = file;
		this.indexPath = computeIndexPath(file);
	}

	public FileIndexLocation(URL url, File file) {
		super(url);
		this.indexFile = file;
		this.indexPath = computeIndexPath(file);
	}

	public FileIndexLocation(File file, boolean participantIndex) {
		this(file);
		this.participantIndex = true;
	}

	@Override
	public boolean createNewFile() throws IOException {
		File directory = this.indexFile.getParentFile();
		if (directory != null && !directory.exists()) {
			directory.mkdirs();
		}
		// always call File#createNewFile() so that the IOException is thrown if there is a failure
		return this.indexFile.createNewFile();
	}

	@Override
	public boolean delete() {
		return this.indexFile.delete();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FileIndexLocation)) return false;
		return this.indexFile.equals(((FileIndexLocation) other).indexFile);
	}

	@Override
	public boolean exists() {
		return this.indexFile.exists();
	}

	@Override
	public String fileName() {
		return this.indexFile.getName();
	}

	@Override
	public File getIndexFile() {
		return this.indexFile;
	}

	@Override
	InputStream getInputStream() throws IOException {
		return new FileInputStream(this.indexFile);
	}

	private static Path computeIndexPath(File indexFile) {
		return new Path(indexFile.toString());
	}

	@Override
	public Path getIndexPath() {
		return this.indexPath;
	}

	@Override
	public int hashCode() {
		return this.indexFile.hashCode();
	}

	@Override
	public long lastModified() {
		return this.indexFile.lastModified();
	}

	@Override
	public long length() {
		return this.indexFile.length();
	}

	@Override
	public boolean startsWith(IPath path) {
		if (this.indexPath==null) {
			return false;
		}
		return path.isPrefixOf(this.indexPath);
	}

}
