 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import java.util.HashMap;
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
	private static Map<String, IPairInPartitionStrategy> mapPartitionToStrategy = new HashMap<String, IPairInPartitionStrategy>();


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
