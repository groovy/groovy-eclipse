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

/**
 * Abstract implementation with some generally useful methods.
 * 
 * @author emp
 */
public abstract class AbstractPairInPartitionStrategy implements
		IPairInPartitionStrategy {
	/**
	 * Check if the sequence contains any of the characters in the samples.
	 * 
	 * @param seq
	 * @param samples
	 * @return True if a partial match exists, else false.
	 */
	protected boolean partiallyMatchesOne(String seq, char[] samples) {
		for (int i = 0; i < samples.length; ++i) {
			if (seq.indexOf(samples[i]) != -1)
				return true;
		}
		return false;
	}

	/**
	 * Check if the sequence is equal to one of the samples.
	 * 
	 * @param seq
	 * @param samples
	 * @return
	 */
	protected boolean matchesOne(String seq, String[] samples) {
		for (int i = 0; i < samples.length; ++i) {
			if (seq.equals(samples[i]))
				return true;
		}
		return false;
	}
}
