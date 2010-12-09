/*
 * Copyright 2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

/**
 * Concrete implementation of a problem type.
 * 
 * @author Nieraj Singh
 * 
 */
public class ProblemType implements IProblemType {

	private String markerType;
	private int problemID;

	public ProblemType(String markerType, int problemID) {
		this.markerType = markerType;
		this.problemID = problemID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IProblemType#getMarkerType
	 * ()
	 */
	public String getMarkerType() {
		return markerType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.IProblemType#getProblemID
	 * ()
	 */
	public int getProblemID() {
		return problemID;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((markerType == null) ? 0 : markerType.hashCode());
		result = prime * result + problemID;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProblemType other = (ProblemType) obj;
		if (markerType == null) {
			if (other.markerType != null)
				return false;
		} else if (!markerType.equals(other.markerType)) {
			return false;
		}
		if (problemID != other.problemID) {
			return false;
		}
		return true;
	}

}
