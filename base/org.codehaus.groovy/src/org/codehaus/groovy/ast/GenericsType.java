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

package org.codehaus.groovy.ast;

/**
 * This class is used to describe generic type signatures
 * for ClassNodes. 
 * @author Jochen Theodorou
 * @see ClassNode
 */
public class GenericsType extends ASTNode {
	/**
	 * Andys observations:
	 * - this is used to represent either a type variable with bounds (T extends I) or type parameter value (<String>).  In the former I think
	 * just the name and bounds are used, whilst in the latter the type is used.  If the name is used then it is a placeholder.
	 * but why does the first constructor take a set of parameters that wouldn't seem to make sense together?
	 * 
	 */
	// GRECLIPSE: start: five from private to protected and first two non-final 
    protected ClassNode[] upperBounds;
    protected ClassNode lowerBound;
    protected ClassNode type;
    protected String name;
    protected boolean placeholder;
    private boolean resolved;
    private boolean wildcard;
    
    public GenericsType(ClassNode type, ClassNode[] upperBounds, ClassNode lowerBound) {
        this.type = type;
        this.name = type.isGenericsPlaceHolder() ? type.getUnresolvedName() : type.getName();
        this.upperBounds = upperBounds;
        this.lowerBound = lowerBound;
        placeholder = type.isGenericsPlaceHolder();
        resolved = false;
    }
    // GRECLIPSE: start
    public GenericsType() {}
    // end
        
    public GenericsType(ClassNode basicType) {
        this(basicType,null,null);
    }

    public ClassNode getType() {
        return type;
    }
    
    public void setType(ClassNode type) {
        this.type = type;
    }
    
    public String toString() {
        String ret = name;
        if (upperBounds!=null) {
            ret += " extends ";
            for (int i = 0; i < upperBounds.length; i++) {
                ret += upperBounds[i].toString();
                if (i+1<upperBounds.length) ret += " & ";
            }
        } else if (lowerBound!=null) {
            ret += " super "+lowerBound;
        }
        return ret;
    }

    public ClassNode[] getUpperBounds() {
        return upperBounds;
    }
    
    public String getName(){
        return name;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
        type.setGenericsPlaceHolder(true);
    }
    
    public boolean isResolved() {
        return resolved || placeholder;
    }
    
    public void setResolved(boolean res) {
        resolved = res;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }
    
    public ClassNode getLowerBound() {
        return lowerBound;
    }
    
    // GRECLIPSE: start
	public void setUpperBounds(ClassNode[] bounds) {
		this.upperBounds = bounds;		
	}

	public void setLowerBound(ClassNode bound) {
		this.lowerBound = bound;		
	}
	// end
}
