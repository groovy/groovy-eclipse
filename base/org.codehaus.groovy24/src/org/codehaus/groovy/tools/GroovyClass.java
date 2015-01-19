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

package org.codehaus.groovy.tools;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;

// GRECLIPSE: start: extended to know classNode/source
public class GroovyClass
{
    public static final GroovyClass[] EMPTY_ARRAY = new GroovyClass[ 0 ];

    private String name;
    private byte[] bytes;
    private ClassNode classNode;
    private SourceUnit source;

    public GroovyClass(String name,
                       byte[] bytes, ClassNode classNode, SourceUnit source)
    {
        this.name  = name;
        this.bytes = bytes;
        this.classNode = classNode;
        this.source = source;
    }

    public String getName()
    {
        return this.name;
    }

    public byte[] getBytes()
    {
        return this.bytes;
    }
    
    public SourceUnit getSourceUnit() {
    	return source;
    }
    
    public ClassNode getClassNode() {
    	return classNode;
    }
}

