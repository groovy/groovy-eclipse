/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.astviews

import java.beans.Introspector
import java.lang.reflect.Method

import groovy.transform.PackageScope

import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.ProcessingUnit

@PackageScope class TreeNodeFactory {

    private static final List<Class> forceDefaultNode = [
        org.codehaus.groovy.ast.CompileUnit,
        org.codehaus.groovy.ast.DynamicVariable,
        org.codehaus.groovy.ast.VariableScope,
        org.codehaus.groovy.control.CompilerConfiguration,
        org.codehaus.groovy.control.SourceUnit,
        org.codehaus.jdt.groovy.control.EclipseSourceUnit,
    ].asImmutable()

    private static final Object NULL_VALUE = new Object()

    @PackageScope static ITreeNode createTreeNode(ITreeNode parent, Object value, String displayName) {
        if (value == null) {
            value = NULL_VALUE
        }
        // The displayName attributes are set after the constructor in the code below because
        // displayName relies on the value object being set before it.  Since groovy does not
        // honor the order of attributes in the constructor declaration, value is null when
        // setDisplayName() is called and the ASTViewer becomes frustrating.
        if (value instanceof ASTNode || value.class in forceDefaultNode) {
            def node = new DefaultTreeNode(parent: parent, value: value)
            node.displayName = displayName
            return node
        } else if (value instanceof Iterable || value instanceof Iterator || value instanceof Object[]) {
            def node = new CollectionTreeNode(parent: parent, value: value)
            node.displayName = displayName
            return node
        } else if (value instanceof Map) {
            def node = new MapTreeNode(parent: parent, value: value)
            node.displayName = displayName
            return node
        } else {
            def node = new AtomTreeNode(parent: parent, value: value)
            node.displayName = displayName
            return node
        }
    }
}

@PackageScope class StringUtil {
    @PackageScope static String toString(Object obj) {
        return TreeNodeFactory.NULL_VALUE.is(obj) ? 'null' : Objects.toString(obj)
    }

    @PackageScope static String toString(Class type) {
        // If o is Integer, then Integer.toString() tries to call either of the static toString() methods of Integer.
        // There may be other classes with static toString(args) methods. This is the same code as in Class.toString().
        return (type.isInterface() ? 'interface ' : (type.isPrimitive() ? '' : 'class ')) + type.name
    }
}

@PackageScope abstract class TreeNode implements ITreeNode {
    protected static final ITreeNode[] NO_CHILDREN = new ITreeNode[0]

    ITreeNode parent
    Object value
    String displayName
    Boolean leaf
    ITreeNode[] children

    void setDisplayName(String name) {
        def mappedName = MapTo.names(value)
        if (mappedName) {
            name = "$name - $mappedName"
        }
        displayName = name
    }

    @Override
    ITreeNode[] getChildren() {
        if (children == null) {
            children = loadChildren()
            if (children == null) {
                children = NO_CHILDREN
            }
        }
        return children
    }

    @Override
    boolean isLeaf() {
        if (leaf == null) {
            leaf = (getChildren().length == 0)
        }
        return leaf
    }

    abstract ITreeNode[] loadChildren()
}

@PackageScope class DefaultTreeNode extends TreeNode {
    @Override
    ITreeNode[] loadChildren() {
        List<Method> methods = value.class.methods.findAll { method ->
            method.parameterCount == 0 && method.name =~ /^(is|get|redirect)/ && method.declaringClass.name != 'java.lang.Object'
        }
        // remove some redundant methods
        if (value instanceof ClassNode) {
            methods = methods.findAll { method ->
                !(method.name ==~ /get(AbstractMethods|AllDeclaredMethods|AllInterfaces|DeclaredMethodsMap|FieldIndex|Module|NameWithoutPackage|PackageName|PlainNodeReference|Text)|isRedirectNode/)
            }
        } else if (value instanceof ModuleNode) {
            methods = methods.findAll { method ->
                !(method.name ==~ /get(PackageName|ScriptClassDummy)/)
            }
        } else if (value instanceof ProcessingUnit) {
            methods = methods.findAll { method ->
                !(method.name ==~ /get(AST|CST)/)
            }
        }
        if (value instanceof ASTNode) {
            methods = methods.findAll { method ->
                !(method.name ==~ /get(Instance|MetaDataMap)/)
            }
        }

        Collection<ITreeNode> children = methods.findResults { method ->
            String name = method.name
            if (name.startsWith('is')) {
                name = Introspector.decapitalize(name.substring(2))
            } else if (name.startsWith('get')) {
                name = Introspector.decapitalize(name.substring(3))
            }
            try {
                Object value = this.value."${method.name}"()
                if ((name != 'text' && !this.value.is(value)) ||
                        (name == 'text' && !(value =~ /^<not implemented /))) {
                    return TreeNodeFactory.createTreeNode(this, value, name)
                }
            } catch (GroovyBugError | ClassCastException | NullPointerException ignore) {
            }
            return null
        }

        return (children as ITreeNode[]).sort(true) { it.displayName }
    }
}

// This includes object arrays.
@PackageScope class CollectionTreeNode extends TreeNode {
    @Override
    ITreeNode[] loadChildren() {
        return value.collect {
            def name = StringUtil.toString(it)
            if (name.indexOf('@') != -1) {
                name = it.class.canonicalName
            }
            TreeNodeFactory.createTreeNode(this, it, name)
        } as ITreeNode[]
    }
}

@PackageScope class MapTreeNode extends TreeNode {
    @Override
    ITreeNode[] loadChildren() {
        return value.collect { k, v ->
            TreeNodeFactory.createTreeNode(this, v, k as String)
        } as ITreeNode[]
    }
}

@PackageScope class AtomTreeNode implements ITreeNode {
    ITreeNode parent
    Object value
    String displayName
    final boolean leaf = true
    final ITreeNode[] children = TreeNode.NO_CHILDREN

    void setDisplayName(String name) {
        def mappedName = MapTo.names(value)
        if (mappedName) {
            name = "$name - $mappedName"
        }
        if (value instanceof String) {
            displayName = "$name : '$value'"
        } else {
            def valueString = StringUtil.toString(value)
            if (valueString.indexOf('@') != -1) {
                valueString = value.class.canonicalName
            }
            displayName = "$name : $valueString"
        }
    }
}
