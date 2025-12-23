/*
 * Copyright 2009-2025 the original author or authors.
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

import static org.eclipse.jdt.core.Flags.AccAbstract;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.transform.ASTTransformation;

class IntersectionType extends ClassNode {

    final List<ClassNode> types;

    IntersectionType(final List<ClassNode> types) {
        super(makeName(types), AccAbstract, findSuper(types), findFaces(types), MixinNode.EMPTY_ARRAY);
        this.types = types;
    }

    private static String makeName(final List<ClassNode> types) {
        StringJoiner sj = new StringJoiner(" | ", "(", ")");
        for (ClassNode t : types) sj.add(t.toString(false));
        return sj.toString();
    }

    private static ClassNode findSuper(final List<ClassNode> types) {
        var upper = WideningCategories.lowestUpperBound(types);
        if (upper instanceof WideningCategories.LowestUpperBoundClassNode) {
            upper = upper.getUnresolvedSuperClass();
        } else if (upper.isInterface()) {
            upper = ClassHelper.OBJECT_TYPE;
        }
        return upper;
    }

    private static ClassNode[] findFaces(final List<ClassNode> types) {
        var upper = WideningCategories.lowestUpperBound(types);
        if (upper.isInterface()) return new ClassNode[]{upper};
        return Optional.ofNullable(upper.getUnresolvedInterfaces(false)).orElse(ClassNode.EMPTY_ARRAY);
    }

    //--------------------------------------------------------------------------

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
        return types.stream().map(ClassNode::getNameWithoutPackage).collect(Collectors.joining(" | ", "(", ")"));
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
        if (getUnresolvedInterfaces(false) == null) {
            super.setInterfaces(cn);
            return;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMixins(MixinNode[] mn) {
        if (getMixins() == null) {
            super.setMixins(mn);
            return;
        }
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
        if (getUnresolvedSuperClass(false) == null) {
            super.setSuperClass(cn);
            return;
        }
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
