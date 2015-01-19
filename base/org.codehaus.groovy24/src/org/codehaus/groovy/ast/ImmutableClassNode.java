/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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

import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.GroovyBugError;

/**
 * GRECLIPSE
 * A {@link ClassNode} where the {@link GenericsType} information
 * is immutable.  Provides extra safety in the IDE.
 * 
 * @author Andrew Eisenberg
 * @created Jul 20, 2011
 */
class ImmutableClassNode extends ClassNode {
    
    class ImmutableGenericsType extends GenericsType {
        ImmutableGenericsType(GenericsType delegate) {
            super.setType(delegate.getType());
            super.setPlaceholder(delegate.isPlaceholder());
            super.setResolved(delegate.isResolved());
            super.setName(delegate.getName());
            super.setWildcard(delegate.isWildcard());
            super.setUpperBounds(delegate.getUpperBounds());
            super.setLowerBound(delegate.getLowerBound());
        }

        @Override
        public void setType(ClassNode type) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + ImmutableClassNode.this.getName());
        }

        @Override
        public void setPlaceholder(boolean placeholder) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + ImmutableClassNode.this.getName());
        }

        @Override
        public void setResolved(boolean res) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + ImmutableClassNode.this.getName());
        }

        @Override
        public void setName(String name) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + ImmutableClassNode.this.getName());
        }

        @Override
        public void setWildcard(boolean wildcard) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + ImmutableClassNode.this.getName());
        }

        @Override
        public void setUpperBounds(ClassNode[] bounds) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + ImmutableClassNode.this.getName());
        }

        @Override
        public void setLowerBound(ClassNode bound) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + ImmutableClassNode.this.getName());
        }
    }
    
    private boolean genericsInitialized = false;
	private boolean writeProtected = false;
    
    public ImmutableClassNode(Class c) {
        super(c);
    }

    @Override
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        if (genericsInitialized && genericsTypes != this.genericsTypes) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + this.getName());
        }
        
        if (genericsTypes != null) {
            GenericsType[] immutable = new GenericsType[genericsTypes.length];
            for (int i = 0; i < genericsTypes.length; i++) {
                immutable[i] = new ImmutableGenericsType(genericsTypes[i]);
            }
            genericsTypes = immutable;
        }
        super.setGenericsTypes(genericsTypes);
        genericsInitialized = true;
    }
    
    @Override
    public List<MethodNode> getDeclaredMethods(String name) {
// Tried this first, but is not enough to catch the culprits who mutate the 'immutable' class!
// some parties must be getting their hands on the data directly.
//    	List<MethodNode> methods = super.getDeclaredMethods(name);
//    	if (methods!=null) {
//    		//Protection against clients who try to modify the list behind our backs.
//    		return Collections.unmodifiableList(methods);
//    	}
//    	return null;
    	
    	//So... more drastic measures are in order. Replace the entire data structure with an immutable copy
    	// the first time we see it after its been initialized.
    	if (lazyInitDone && !writeProtected) {
    		enableWriteProtection();
    	}
    	return super.getDeclaredMethods(name);
    }

	private synchronized void enableWriteProtection() {
		for (Object key : methods.map.keySet()) {
			List<MethodNode> l = methods.get(key);
			methods.map.put(key, Collections.unmodifiableList(l));
		}
		methods.map = Collections.unmodifiableMap(methods.map);
		writeProtected = true;
	}
    
}