/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
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
