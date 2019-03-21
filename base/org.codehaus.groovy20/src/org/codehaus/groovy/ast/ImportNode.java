/*
 * Copyright 2003-2010 the original author or authors.
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

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;

/**
 * Represents an import statement of a single class
 * <p/>
 * @author Jochen Theodorou
 * @author Paul King
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class ImportNode extends AnnotatedNode implements Opcodes {

    private final ClassNode type;
    private final String alias;
    private final String fieldName;
    // TODO use PackageNode instead here?
    private final String packageName;
    private final boolean isStar;
    private final boolean isStatic;

    /**
     * Represent an import of an entire package, i.e.&nbsp;import package.Classname
     *
     * @param type  the referenced class
     * @param alias optional alias
     */
    public ImportNode(ClassNode type, String alias) {
        this.type = type;
        this.alias = alias;
        this.isStar = false;
        this.isStatic = false;
        this.packageName = null;
        this.fieldName = null;
    }

    /**
     * Represent an import of an entire package, i.e.&nbsp;import package.*
     *
     * @param packageName the name of the package
     */
    public ImportNode(String packageName) {
        this.type = null;
        this.alias = null;
        this.isStar = true;
        this.isStatic = false;
        this.packageName = packageName;
        this.fieldName = null;
    }

    /**
     * Represent a static import of a Class, i.e.&nbsp;import static package.Classname.*
     *
     * @param type the referenced class
     */
    public ImportNode(ClassNode type) {
        this.type = type;
        this.alias = null;
        this.isStar = true;
        this.isStatic = true;
        this.packageName = null;
        this.fieldName = null;
    }

    /**
     * Represent a static import of a field or method, i.e.&nbsp;import static package.Classname.name
     *
     * @param type      the referenced class
     * @param fieldName the field name
     * @param alias     optional alias
     */
    public ImportNode(ClassNode type, String fieldName, String alias) {
        this.type = type;
        this.alias = alias;
        this.isStar = false;
        this.isStatic = true;
        this.packageName = null;
        this.fieldName = fieldName;
    }

    /**
     * @return the text display of this import
     */
    public String getText() {
        String typeName = getClassName();
        if (isStar && !isStatic) {
            return "import " + packageName + "*";
        }
        if (isStar) {
            return "import static " + typeName + ".*";
        }
        if (isStatic) {
            if (alias != null && alias.length() != 0 && !alias.equals(fieldName)) {
                return "import static " + typeName + "." + fieldName + " as " + alias;
            }
            return "import static " + typeName + "." + fieldName;
        }
        if (alias == null || alias.length() == 0) {
            return "import " + typeName;
        }
        return "import " + typeName + " as " + alias;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isStar() {
        return isStar;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getAlias() {
        return alias;
    }

    public ClassNode getType() {
        return type;
    }

    public String getClassName() {
        return type == null ? null : type.getName();
    }

    public void visit(GroovyCodeVisitor visitor) {
    }

    // GRECLIPSE add
    private boolean unresolvable;
    private ConstantExpression aliasExpr;
    private ConstantExpression fieldNameExpr;

    public void markAsUnresolvable() {
        unresolvable = true;
    }

    public boolean isUnresolvable() {
        return unresolvable;
    }

    public ConstantExpression getAliasExpr() {
        return aliasExpr;
    }

    public void setAliasExpr(ConstantExpression aliasExpr) {
        this.aliasExpr = aliasExpr;
    }

    public ConstantExpression getFieldNameExpr() {
        return fieldNameExpr;
    }

    public void setFieldNameExpr(ConstantExpression fieldNameExpr) {
        this.fieldNameExpr = fieldNameExpr;
    }

    // getType().getStart() is unreliable because ClassNode instances are reused for well-known types
    public int getTypeStart() {
        int start = -1;
        if (type != null) {
            if (type.getEnd() > 0) {
                start = type.getStart();
            } else {
                ClassExpression expr = (ClassExpression) getNodeMetaData(ClassExpression.class);
                if (expr != null) start = expr.getStart();
            }
        }
        return start;
    }

    // getType().getEnd() is unreliable because ClassNode instances are reused for well-known types
    public int getTypeEnd() {
        int end = -1;
        if (type != null) {
            if (type.getEnd() > 0) {
                end = type.getEnd();
            } else {
                ClassExpression expr = (ClassExpression) getNodeMetaData(ClassExpression.class);
                if (expr != null) end = expr.getEnd();
            }
        }
        return end;
    }

    public String toString() {
        return super.toString() + '[' + getText() + ']';
    }
    // GRECLIPSE end
}
