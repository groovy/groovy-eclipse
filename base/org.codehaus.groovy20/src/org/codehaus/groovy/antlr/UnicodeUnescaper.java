/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.antlr;


/**
 * GRECLIPSE-805 Support for unicode escape sequences
 * 
 * Ensures that the proper column number is returned 
 * taking into account unicode escape sequences
 * @author Andrew Eisenberg
 * @created Mar 3, 2011
 */

class NoEscaper extends UnicodeEscapingReader {

    public NoEscaper() {
		super(null, null);
	}

	public int getUnescapedUnicodeColumnCount() {
        return 0;
    }
    
    public int getUnescapedUnicodeOffsetCount() {
        return 0;
    }
    
}