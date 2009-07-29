/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.test.internal.performance;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Variations;
import org.osgi.framework.BundleContext;


/**
 * @since 3.1
 */
public class PerformanceTestPlugin extends Plugin {
    
    public static final String CONFIG= "config"; //$NON-NLS-1$
	public static final String BUILD= "build"; //$NON-NLS-1$

	private static final String DEFAULT_DB_NAME= "perfDB"; //$NON-NLS-1$
	private static final String DEFAULT_DB_USER= "guest"; //$NON-NLS-1$
	private static final String DEFAULT_DB_PASSWORD= "guest"; //$NON-NLS-1$
	
	private static final String DB_NAME= "dbname"; //$NON-NLS-1$
	private static final String DB_USER= "dbuser"; //$NON-NLS-1$
	private static final String DB_PASSWD= "dbpasswd"; //$NON-NLS-1$

    /*
	 * New properties
	 */
    private static final String ECLIPSE_PERF_DBLOC= "eclipse.perf.dbloc"; //$NON-NLS-1$
    private static final String ECLIPSE_PERF_ASSERTAGAINST= "eclipse.perf.assertAgainst"; //$NON-NLS-1$
    private static final String ECLIPSE_PERF_CONFIG= "eclipse.perf.config"; //$NON-NLS-1$

	/**
	 * The plug-in ID
	 */
    public static final String PLUGIN_ID= "org.eclipse.test.performance"; //$NON-NLS-1$
    
	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR= 1;

	/**
	 * The shared instance.
	 */
	private static PerformanceTestPlugin fgPlugin;
	
	/* temporary code */
	private static boolean fgOldDBInitialized;
	private static boolean fgOldDB;	// true if we are talking to the old perfDB in Ottawa
		
	/**
	 * The constructor.
	 */
	public PerformanceTestPlugin() {
	    super();
		fgPlugin= this;
	}
	
	static boolean isOldDB() {
	    if (!fgOldDBInitialized) {
			String loc= getDBLocation();
			if (loc != null && loc.indexOf("relengbuildserv") >= 0) //$NON-NLS-1$
			    fgOldDB= true;
	        fgOldDBInitialized= true;
	    }
	    return fgOldDB;
	}
	
	public void stop(BundleContext context) throws Exception {
		DB.shutdown();
		super.stop(context);
	}
		
	/*
	 * Returns the shared instance.
	 */
	public static PerformanceTestPlugin getDefault() {
		return fgPlugin;
	}
	    
	/*
	 * -Declipse.perf.dbloc=net://localhost
	 */
	public static String getDBLocation() {
		String dbloc= System.getProperty(ECLIPSE_PERF_DBLOC);
		if (dbloc != null) {
		    Variations keys= new Variations();
		    keys.parsePairs(ECLIPSE_PERF_DBLOC + '=' + dbloc);
		    return keys.getProperty(ECLIPSE_PERF_DBLOC);
		}
		return null;
	}

	public static String getDBName() {
		String dbloc= System.getProperty(ECLIPSE_PERF_DBLOC);
		if (dbloc != null) {
		    Variations keys= new Variations();
		    keys.parsePairs(ECLIPSE_PERF_DBLOC + '=' + dbloc);
		    return keys.getProperty(DB_NAME, DEFAULT_DB_NAME);
		} 
	    return DEFAULT_DB_NAME;
	}

	public static String getDBUser() {
		String dbloc= System.getProperty(ECLIPSE_PERF_DBLOC);
		if (dbloc != null) {
		    Variations keys= new Variations();
		    keys.parsePairs(ECLIPSE_PERF_DBLOC + '=' + dbloc);
		    return keys.getProperty(DB_USER, DEFAULT_DB_USER);
		} 
	    return DEFAULT_DB_USER;
	}

	public static String getDBPassword() {
		String dbloc= System.getProperty(ECLIPSE_PERF_DBLOC);
		if (dbloc != null) {
		    Variations keys= new Variations();
		    keys.parsePairs(ECLIPSE_PERF_DBLOC + '=' + dbloc);
		    return keys.getProperty(DB_PASSWD, DEFAULT_DB_PASSWORD);
		} 
	    return DEFAULT_DB_PASSWORD;
	}
	
	/*
	 * -Declipse.perf.config=<varname1>=<varval1>;<varname2>=<varval2>;...;<varnameN>=<varvalN>
	 */
	public static Variations getVariations() {
	    Variations keys= new Variations();
		String configKey= System.getProperty(ECLIPSE_PERF_CONFIG);
		if (configKey != null)
		    keys.parsePairs(configKey);
	    return keys;
	}

	/*
	 * -Declipse.perf.assertAgainst=<varname1>=<varval1>;<varname2>=<varval2>;...;<varnameN>=<varvalN>
	 * Returns null if assertAgainst property isn't defined.
	 */
	public static Variations getAssertAgainst() {
		String assertKey= System.getProperty(ECLIPSE_PERF_ASSERTAGAINST);
		if (assertKey != null) {
		    Variations keys= getVariations();
		    if (keys == null)
		        keys= new Variations();
		    keys.parsePairs(assertKey);
		    return keys;
		}
	    return null;
	}
	
	// logging
		
	public static void logError(String message) {
		if (message == null)
			message= ""; //$NON-NLS-1$
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, message, null));
	}

	public static void logWarning(String message) {
		if (message == null)
			message= ""; //$NON-NLS-1$
		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, message, null));
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, "Internal Error", e)); //$NON-NLS-1$
	}
	
	public static void log(IStatus status) {
	    if (fgPlugin != null) {
	        fgPlugin.getLog().log(status);
	    } else {
	        switch (status.getSeverity()) {
	        case IStatus.ERROR:
		        System.err.println("Error: " + status.getMessage()); //$NON-NLS-1$
	            break;
	        case IStatus.WARNING:
		        System.err.println("Warning: " + status.getMessage()); //$NON-NLS-1$
	            break;
	        }
	        Throwable exception= status.getException();
	        if (exception != null)
	            exception.printStackTrace(System.err);
	    }
	}
}
