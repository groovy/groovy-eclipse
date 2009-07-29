/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.debug;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jul 21, 2009
 * This class must go!
 * 
 * Use IToggleBreakpointsTargetExtension instead
 */
public class GroovyLineBreakpoint extends JavaLineBreakpoint {
	private static final String GROOVY_LINE_BREAKPOINT = "org.eclipse.debug.core.breakpointMarker"; //$NON-NLS-1$
	
	public GroovyLineBreakpoint(IResource resource, String typeName, int lineNumber, int charStart, int charEnd, int hitCount, boolean add, Map attributes) throws DebugException {
		super(resource, typeName, lineNumber, charStart, charEnd, hitCount, add, attributes, GROOVY_LINE_BREAKPOINT);
	}
	
	public static String getMarkerType() {
		return GROOVY_LINE_BREAKPOINT;
	} 

}
