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

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

/**
 * Interface for handling a specific auto pair strategy when in a specific
 * partition. This makes it easier to add strategies for different partitions.
 * 
 * @author emp
 */
public interface IPairInPartitionStrategy {
	/**
	 * Is this strategy active?
	 * 
	 * @return
	 */
	public boolean isActive();

	/**
	 * The user pressed a key that inserts a character, check for auto pair
	 * completion.
	 * 
	 * @param document
	 * @param command
	 */
	public void doInsert(IDocument document, DocumentCommand command);

	/**
	 * The user pressed the backspace key, check for auto pair deletion.
	 * 
	 * @param document
	 * @param command
	 */
	public void doRemove(IDocument document, DocumentCommand command);
}
