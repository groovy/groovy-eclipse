 /*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.astviews

import groovy.transform.PackageScope

import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ASTNode

@PackageScope class TreeNodeFactory {

    private static final List<Class> forceDefaultNode = [
        org.codehaus.groovy.ast.CompileUnit,
        org.codehaus.groovy.ast.DynamicVariable,
        org.codehaus.groovy.ast.Parameter,
        org.codehaus.groovy.ast.Variable,
        org.codehaus.groovy.ast.VariableScope,
        org.codehaus.groovy.control.SourceUnit,
    ].asImmutable()

    private static final Object NULL_VALUE = new Object()

    @PackageScope static ITreeNode createTreeNode(ITreeNode parent, Object value, String displayName) {
        if (value == null) {
            value = NULL_VALUE
        }
        // The displayName attributes are set after the constructor in the code below because
        // displayName relies on the value object being set before it.  Since groovy does not
        // honor the order of the attributes in the constructor declaration, value is null when
        // setDisplayName() is called and the ASTViewer becomes frustrating.
        if (value instanceof ASTNode || forceDefaultNode.contains(value.getClass())) {
            def node = new DefaultTreeNode(parent: parent, value: value)
            node.displayName = displayName
            return node
        } else if (value instanceof List || value instanceof Object[]) {
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
        return obj.toString()
    }

    @PackageScope static String toString(Class cls) {
        // If o is Integer, then Integer.toString() tries to call either of the static toString() methods of Integer.
        // There may be other classes with static toString(args) methods. This is the same code as in Class.toString().
        return (cls.isInterface() ? 'interface ' : (cls.isPrimitive() ? '' : 'class ')) + cls.name
    }
}

@PackageScope abstract class TreeNode implements ITreeNode {
    private static final ITreeNode[] NO_CHILDREN = new ITreeNode[0]

    ITreeNode parent
    Object value
    String displayName
    Boolean leaf
    ITreeNode[] children = null

    void setDisplayName(String name) {
        def mappedName = MapTo.names(value)
        if (mappedName) {
            name = "$name - $mappedName"
        }
        displayName = name
    }

    ITreeNode[] getChildren() {
        if (children == null) {
            children = loadChildren()
            if (children == null) {
                children = NO_CHILDREN
            }
        }
        return children
    }

    boolean isLeaf() {
        if (leaf == null) {
            leaf = (getChildren().length == 0)
        }
        return leaf
    }

    abstract ITreeNode[] loadChildren()
}

@PackageScope class DefaultTreeNode extends TreeNode {
    ITreeNode[] loadChildren() {
        def methods = value.class.getMethods()
        methods = methods?.findAll { (it.name.startsWith('get') && it.parameterTypes.length == 0) || it.name == 'redirect' }
        def children = methods?.findResults { method ->
            def name = method.name == 'redirect' ? method.name : method.name[3..-1]
            name = name[0].toLowerCase() + name[1..-1]
            try {
                return TreeNodeFactory.createTreeNode(this, value."${method.name}"(), name)
            } catch (GroovyBugError e) {
                // Some getters are not for us.
                return null
            } catch (NullPointerException e) {
                // For some reason ClassNode.getAbstractMethods() has a problem - ClassNode.superclass is null.
                return null
            } catch (ClassCastException e) {
                // DeclarationExpression.getTupleExpression() will return a CCE on Groovy 18+ if the expression is not a Tuple expression
                return null
            }
        }
        if (children == null) {
            children = NO_CHILDREN
        }
        return children
    }
}

// This includes object arrays.
@PackageScope class CollectionTreeNode extends TreeNode {
    ITreeNode[] loadChildren() {
        return value.collect {
            def name = StringUtil.toString(it)
            if (name.indexOf('@') != -1) {
                name = it.getClass().canonicalName
            }
            TreeNodeFactory.createTreeNode(this, it, name)
        } as ITreeNode[]
    }
}

@PackageScope class MapTreeNode extends TreeNode {
    ITreeNode[] loadChildren() {
        return value.collect { k, v ->
            TreeNodeFactory.createTreeNode(this, v, k)
        } as ITreeNode[]
    }
}

@PackageScope class AtomTreeNode implements ITreeNode {
    ITreeNode parent
    Object value
    String displayName

    boolean isLeaf() {
        true
    }

    ITreeNode[] getChildren() {
        TreeNode.NO_CHILDREN
    }

    void setDisplayName(String name) {
        def mappedName = MapTo.names(value)
        if (mappedName) {
            name = "$name - $mappedName"
        }
        if (value instanceof String) {
            displayName = "$name : '${StringUtil.toString(value)}'".toString()
        } else {
            def valueName = StringUtil.toString(value)
            if (valueName.indexOf('@') != -1) {
                valueName = value.getClass().canonicalName
            }
            displayName = "$name : ${StringUtil.toString(valueName)}".toString()
        }
    }
}
