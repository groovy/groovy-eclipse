/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
 * A {@link ClassNode} where the {@link GenericsType} information is immutable.
 * Provides extra safety in the IDE.
 */
public class ImmutableClassNode extends ClassNode {

    private volatile boolean genericsInitialized;
    private volatile boolean writeProtected;

    public ImmutableClassNode(Class c) {
        super(c);
    }

    // ASTNode overrides:

    @Override
    public void setColumnNumber(int n) {}

    @Override
    public void setLastColumnNumber(int n) {}

    @Override
    public void setLastLineNumber(int n) {}

    @Override
    public void setLineNumber(int n) {}

    @Override
    public void setNodeMetaData(Object k, Object v) {}

    @Override
    public Object putNodeMetaData(Object k, Object v) {
        return getNodeMetaData(k);
    }

    @Override
    public void setSourcePosition(ASTNode n) {}

    @Override
    public void setStart(int i) {}

    @Override
    public void setEnd(int i) {}

    // AnnotatedNode overrides:

    @Override
    public void setNameStart(int i) {}

    @Override
    public void setNameEnd(int i) {}

    @Override
    public void setDeclaringClass(ClassNode cn) {}

    @Override
    public void setHasNoRealSourcePosition(boolean b) {}

    @Override
    public void setSynthetic(boolean b) {}

    // ClassNode overrides:

    @Override
    public List<MethodNode> getDeclaredMethods(String name) {
        if (lazyInitDone && !writeProtected) {
            synchronized (methods) {
                if (!writeProtected) {
                    writeProtected = true;
                    if (methods.map == null || methods.map.isEmpty()) {
                        methods.map = Collections.emptyMap();
                    } else {
                        for (Object key : methods.map.keySet()) {
                            List<MethodNode> list = methods.get(key);
                            methods.map.put(key, Collections.unmodifiableList(list));
                        }
                        methods.map = Collections.unmodifiableMap(methods.map);
                    }
                }
            }
        }
        return super.getDeclaredMethods(name);
    }

    @Override
    public void setAnnotated(boolean b) {}

    @Override
    protected void setCompileUnit(CompileUnit cu) {}

    @Override
    public void setEnclosingMethod(MethodNode mn) {}

    @Override
    public void setGenericsPlaceHolder(boolean b) {}

    @Override
    public void setHasInconsistentHierarchy(boolean b) {}

    //public void setInterfaces(ClassNode[] cn) {}

    @Override
    public void setModifiers(int bf) {}

    @Override
    public void setModule(ModuleNode mn) {}

    @Override
    public String setName(String s) {
        return getName();
    }

    @Override
    public void setNameStart2(int i) {}

    //public void setRedirect(ClassNode cn) {}

    @Override
    public void setSuperClass(ClassNode cn) {}

    @Override
    public void setScript(boolean b) {}

    @Override
    public void setScriptBody(boolean b) {}

    @Override
    public void setStaticClass(boolean b) {}

    @Override
    public void setSyntheticPublic(boolean b) {}

    //public void setUnresolvedSuperClass(ClassNode cn) {}

    @Override
    public void setUsingGenerics(boolean b) {}

    @Override
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        if (genericsInitialized && genericsTypes != super.getGenericsTypes()) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + getName());
        }
        if (genericsTypes != null) {
            GenericsType[] immutable = new GenericsType[genericsTypes.length];
            for (int i = 0, n = genericsTypes.length; i < n; i += 1) {
                immutable[i] = new ImmutableGenericsType(genericsTypes[i], getName());
            }
            genericsTypes = immutable;
        }
        super.setGenericsTypes(genericsTypes);
        genericsInitialized = true;
    }

    static class ImmutableGenericsType extends GenericsType {

        ImmutableGenericsType(GenericsType delegate, String typeName) {
            this.typeName = typeName;
            super.setType(delegate.getType());
            super.setName(delegate.getName());
            super.setPlaceholder(delegate.isPlaceholder());
            super.setResolved(delegate.isResolved());
            super.setWildcard(delegate.isWildcard());
            super.setLowerBound(delegate.getLowerBound());
            super.setUpperBounds(delegate.getUpperBounds());
        }

        private final String typeName;

        @Override
        public void setType(ClassNode type) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setPlaceholder(boolean placeholder) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setResolved(boolean res) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setName(String name) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setWildcard(boolean wildcard) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setUpperBounds(ClassNode[] bounds) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setLowerBound(ClassNode bound) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }
    }
}
