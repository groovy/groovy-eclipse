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
package org.codehaus.groovy.eclipse.editor;

import static org.codehaus.groovy.eclipse.core.util.MapUtil.newMap;

import java.util.Map;

import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;

/**
 * Strategy for automatically closing 'enclosing' pairs in Groovy code. This
 * strategy handles '' "" '''''' """""" () and [].
 * 
 * @author emp
 */
public class AutoEnclosingPairStrategy implements IAutoEditStrategy {
	private static Map<String, IPairInPartitionStrategy> mapPartitionToStrategy = newMap();


	static {
		mapPartitionToStrategy.put(IJavaPartitions.JAVA_CHARACTER,
				new CharacterPartitionPairStrategy());
		mapPartitionToStrategy.put(IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
				new SingleLineStringPartitionPairStrategy());
		mapPartitionToStrategy.put(GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS,
				new MultiLineStringPartitionPairStrategy());
	}

	public AutoEnclosingPairStrategy() {
	}

	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
        try {
    		if (command.getCommandCount() == 1) {
    		    if (document instanceof IDocumentExtension3) {
    		        String partition = ((IDocumentExtension3) document).getPartition(IJavaPartitions.JAVA_PARTITIONING, command.offset, true).getType();
    		        if (partition != null) {
    		            IPairInPartitionStrategy strategy = mapPartitionToStrategy
    		            .get(partition);
    		            if (strategy == null)
    		                return;
    		            
    		            if (strategy.isActive() == false)
    		                return;
    		            
    		            if (command.length == 0) {
    		                strategy.doInsert(document, command);
    		            } else if (command.length == 1) {
    		                strategy.doRemove(document, command);
    		            }
    		        }
    		    }
    		}
        } catch (BadPartitioningException e) {
            // do nothing
        } catch (BadLocationException e) {
            // do nothing
        }
	}
}
