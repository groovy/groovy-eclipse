/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.core;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=445122
 */
public class SystemPropertyCleaner {

	public static void clean() {
		Properties props = System.getProperties();
		Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Object, Object> e = iter.next();
			Object k = e.getKey();
			Object v = e.getValue();
			if (k instanceof String && v instanceof String) {
				//ok
			} else {
				//System.out.println("deleting "+k+" = ("+className(v)+") "+v);
				iter.remove();
			}
		}
	}

//	private static String className(Object v) {
//		if (v!=null) {
//			return v.getClass().getName();
//		}
//		return null;
//	}
	
}
