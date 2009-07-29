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
package org.codehaus.groovy.eclipse.core.context;

/**
 * Interface implemented by instances that are aware of some context. The single
 * {@link #setSourceCodeContext(ISourceCodeContext)} method is called whenever a context changes. The method is not
 * guaranteed to be called at all, so implementers must check that is has in fact been set.
 * 
 * @author empovazan
 */
public interface ISourceCodeContextAware {
	public void setSourceCodeContext(ISourceCodeContext context);
}
