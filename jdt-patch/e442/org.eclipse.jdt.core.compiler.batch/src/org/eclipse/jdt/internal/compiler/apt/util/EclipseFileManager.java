/*******************************************************************************
 * Copyright (c) 2006, 2022 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.apt.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;

/**
 * Implementation of the Standard Java File Manager
 */
public class EclipseFileManager extends org.eclipse.jdt.internal.compiler.tool.EclipseFileManager {


	public EclipseFileManager(Locale locale, Charset charset) {
		super(locale, charset);
	}

	@Override
	public Iterable<? extends File> getLocation(Location location) {
		/* XXX there are strange differences regarding module name handling with super class
		if (location instanceof LocationWrapper) {
			return getFiles(((LocationWrapper) location).getPaths());
		}
		LocationWrapper loc = this.locationHandler.getLocation(location, ""); //$NON-NLS-1$
		if (loc == null) {
			return null;
		}
		return getFiles(loc.getPaths());
		*/
		return super.getLocation(location);
	}

	@Override
	public boolean hasLocation(Location location) {
		/* XXX there are strange differences regarding module name handling with super class
		try {
			return getLocationForModule(location, "") != null; //$NON-NLS-1$
		} catch (IOException e) {
			// nothing to do
		}
		return false;
		*/
		return super.hasLocation(location);
	}

	@Override
	public void setLocation(Location location, Iterable<? extends File> files) throws IOException {
		/* XXX there are strange differences regarding module name handling with super class
		if (location.isOutputLocation() && files != null) {
			// output location
			int count = 0;
			for (Iterator<? extends File> iterator = files.iterator(); iterator.hasNext(); ) {
				iterator.next();
				count++;
			}
			if (count != 1) {
				throw new IllegalArgumentException("output location can only have one path");//$NON-NLS-1$
			}
		}
		this.locationHandler.setLocation(location, "", getPaths(files)); //$NON-NLS-1$
		*/
		super.setLocation(location, files);
	}

	@Override
	public void setLocationForModule(Location location, String moduleName, Collection<? extends Path> paths) throws IOException {
		/* XXX there are strange differences regarding module name handling with super class
		validateModuleLocation(location, moduleName);
		this.locationHandler.setLocation(location, moduleName, paths);
		if (location == StandardLocation.MODULE_SOURCE_PATH) {
			LocationWrapper wrapper = this.locationHandler.getLocation(StandardLocation.CLASS_OUTPUT, moduleName);
			if (wrapper == null) {
				wrapper = this.locationHandler.getLocation(StandardLocation.CLASS_OUTPUT, ""); //$NON-NLS-1$
				if (wrapper != null) {
					Iterator<? extends Path> iterator = wrapper.getPaths().iterator();
					if (iterator.hasNext()) {
						// Per module output location is always a singleton list
						Path path = iterator.next().resolve(moduleName);
						this.locationHandler.setLocation(StandardLocation.CLASS_OUTPUT, moduleName, Collections.singletonList(path));
					}
				}
			}
		}
		*/
		super.setLocationForModule(location, moduleName, paths);
	}
}
