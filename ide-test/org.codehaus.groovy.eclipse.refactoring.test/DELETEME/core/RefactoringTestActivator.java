/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author reto kleeb
 */
public class RefactoringTestActivator extends Plugin {
	
	// The shared instance
	private static RefactoringTestActivator plugin;
	
	// The plugins context
	private static BundleContext context;

	public RefactoringTestActivator() {
	}
	
	@Override
    public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		RefactoringTestActivator.context = context;
	}

	@Override
    public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RefactoringTestActivator getDefault() {
		return plugin;
	}
	
	/**
	 * 
	 * @param pluginName
	 * @return the path of the plugin
	 */
	public static String getPathOfPlugin(final String pluginName){
		String path = new String();
		for (final Bundle b : context.getBundles()){
			if(b.getSymbolicName().equals(pluginName)){
				path = b.getLocation();
				final int slashIndex = path.indexOf("/");
				path = "plugins" + path.substring(slashIndex);
			}
		}
		System.out.println( "RefactoringTestActivator.getPathOfPlugin(): " + pluginName + " -> " + path );
		return path;
	}
}
