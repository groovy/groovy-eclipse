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

import org.objectweb.asm.Opcodes;

/**
 * Represents an import statement of a single class
 * 
 * author Jochen Theodorou
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision: 7922 $
 */
public class ImportNode extends AnnotatedNode implements Opcodes {

    private final ClassNode type;
    private final String alias;
    
    // GRECLIPSE: ability to handle null types
    private String packageName;
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getPackageName() {
        return packageName;
    }
    // GRECLIPSE: end
    
    
    public ImportNode(ClassNode type, String alias) {
        this.type = type;
        this.alias = alias;
    }
    
    /**
     * @return the text display of this import
     */
    public String getText() {
        // GRECLIPSE: ability to handle null types
        // old:
//      return type.getName();
        // new:
        if (type == null) {
            return "import " + packageName + ".*";
        }
        // GRECLIPSE: end
        if (alias == null || alias.length() == 0) {
            return "import " + type.getName();
        }
        else {
            return "import " + type.getName() + " as " + alias;
        }
    }
    
    public String getAlias() {
        return alias;
    }

    public ClassNode getType() {
        return type;
    }
    
    public String getClassName() {
        // GRECLIPSE: ability to handle null types
        // old:
//    	return type.getName();
        // new:
    	return type == null ? null :type.getName();
    	// GRECLIPSE: end
    }

}
