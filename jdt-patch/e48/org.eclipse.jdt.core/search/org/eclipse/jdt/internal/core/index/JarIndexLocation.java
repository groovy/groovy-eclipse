/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class JarIndexLocation extends IndexLocation {
	private JarFile jarFile = null;
	private JarEntry jarEntry = null;
	private URL localUrl;

	public JarIndexLocation(URL url, URL localUrl2) {
		super(url);
		this.localUrl = localUrl2;
	}

	public boolean createNewFile() throws IOException {
		return false;
	}

	public void close() {
		if (this.jarFile != null) {
			try {
				this.jarFile.close();
			} catch (IOException e) {
				// ignore
			}
			this.jarFile = null;
		}
	}

	public boolean delete() {
		return false;
	}

	public boolean equals(Object other) {
		if (!(other instanceof JarIndexLocation)) return false;
		return this.localUrl.equals(((JarIndexLocation) other).localUrl);
	}

	public boolean exists() {
		try {
			if (this.jarFile == null) {
				JarURLConnection connection = (JarURLConnection) this.localUrl.openConnection();
				connection.setUseCaches(false);
				JarFile file = connection.getJarFile();
				if (file == null)
					return false;
				file.close();
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public String fileName() {
		return null;
	}

	public File getIndexFile() {
		return null;
	}

	InputStream getInputStream() throws IOException {
		if (this.jarFile == null) {
			JarURLConnection connection = (JarURLConnection) this.localUrl.openConnection();
			connection.setUseCaches(false);
			this.jarFile = connection.getJarFile();
			this.jarEntry = connection.getJarEntry();
		}
		if (this.jarFile == null || this.jarEntry == null)
			return null;
		return this.jarFile.getInputStream(this.jarEntry);
	}

	public String getCanonicalFilePath() {
		return null;
	}

	public long lastModified() {
		return -1;
	}

	public long length() {
		return -1;
	}

	public boolean startsWith(IPath path) {
		return (path.isPrefixOf(new Path(this.localUrl.getPath())));
	}

}
