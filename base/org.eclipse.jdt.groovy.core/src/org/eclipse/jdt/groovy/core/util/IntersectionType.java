/*
 * Copyright 2009-2022 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.transform.ASTTransformation;

class IntersectionType extends ClassNode {

    final List<ClassNode> types;

    IntersectionType(List<ClassNode> types) {
        super(string(types), 0, lowestUpperBound(types));
        this.types = types;
    }

    private static String string(List<ClassNode> types) {
        StringJoiner sj = new StringJoiner(" or ", "(", ")");
        for (ClassNode cn: types) sj.add(cn.toString(false));
        return sj.toString();
    }

    //

    @Override
    public void lazyClassInit() {
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public boolean hasPackageName() {
        return false;
    }

    @Override
    public String getNameWithoutPackage() {
        return getName();
    }

    @Override
    public ClassNode getPlainNodeReference() {
        throw new UnsupportedOperationException();
    }

    public ClassNode getPlainNodeReference(boolean b) {
        throw new UnsupportedOperationException();
    }

    //

    @Override
    public List<FieldNode> getFields() {
        return Collections.emptyList();
    }

    @Override
    public List<MethodNode> getMethods() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertyNode> getProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<ConstructorNode> getDeclaredConstructors() {
        return Collections.emptyList();
    }

    @Override
    public List<Statement> getObjectInitializerStatements() {
        return Collections.emptyList();
    }

    //--------------------------------------------------------------------------

    // ASTNode overrides:

    @Override
    public void setColumnNumber(int n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastColumnNumber(int n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastLineNumber(int n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLineNumber(int n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object putNodeMetaData(Object k, Object v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSourcePosition(ASTNode n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStart(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnd(int i) {
        throw new UnsupportedOperationException();
    }

    // AnnotatedNode overrides:

    @Override
    public void setNameStart(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNameEnd(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDeclaringClass(ClassNode cn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHasNoRealSourcePosition(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSynthetic(boolean b) {
        throw new UnsupportedOperationException();
    }

    // ClassNode overrides:

    @Override
    public void setAnnotated(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setCompileUnit(CompileUnit cu) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnclosingMethod(MethodNode mn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenericsPlaceHolder(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenericsTypes(GenericsType[] gt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHasInconsistentHierarchy(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInterfaces(ClassNode[] cn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setModifiers(int bf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setModule(ModuleNode mn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String setName(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNameStart2(int i) {
        throw new UnsupportedOperationException();
    }

    public void setPermittedSubclasses(List<ClassNode> ps) {
        throw new UnsupportedOperationException();
    }

    /*public void setRecordComponents(List<RecordComponentNode> rc) {
        throw new UnsupportedOperationException();
    }*/

    @Override
    public void setSuperClass(ClassNode cn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScript(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScriptBody(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStaticClass(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSyntheticPublic(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUnresolvedSuperClass(ClassNode cn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUsingGenerics(boolean b) {
        throw new UnsupportedOperationException();
    }

    //

    @Override
    public void addAnnotation(AnnotationNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConstructor(ConstructorNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addField(FieldNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFieldFirst(FieldNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMethod(MethodNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMixin(MixinNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addProperty(PropertyNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addStaticInitializerStatements(List<Statement> list, boolean init) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTransform(Class<? extends ASTTransformation> cls, ASTNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTypeAnnotations(List<AnnotationNode> list) {
        throw new UnsupportedOperationException();
    }

    //

    @Override
    public void removeConstructor(ConstructorNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeMethod(MethodNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeField(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void renameField(String oldName, String newName) {
        throw new UnsupportedOperationException();
    }
}
