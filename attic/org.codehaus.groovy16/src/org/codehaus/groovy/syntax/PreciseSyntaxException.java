/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.syntax;
// FIXASC (groovychange) new type - could be merged into existing SyntaxException
/**
 * A more precise form of SyntaxException that is aware of the precise start/end offsets.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("serial")
public class PreciseSyntaxException extends SyntaxException {

	private int startOffset;
	private int endOffset;

	public PreciseSyntaxException(String message, int line, int col, int startOffset, int endOffset) {
		super(message, line, col);
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	public int getStartOffset() {
		return startOffset;
	}
	
	public int getEndOffset() {
		return endOffset;
	}

}
