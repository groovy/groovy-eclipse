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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class GroovyTypeDeclaration extends TypeDeclaration {

    public GroovyTypeDeclaration(CompilationResult compilationResult, ClassNode classNode) {
        super(compilationResult);
        this.classNode = classNode;
    }

    /**
     * Returns the Groovy ClassNode that gave rise to this type declaration.
     */
    public ClassNode getClassNode() {
        return classNode;
    }

    private final ClassNode classNode;

    protected BlockScope enclosingScope;

    protected List<PropertyNode> properties;

    // FIXASC Is this always what we want to do - are there any other implications?
    @Override
    public void parseMethods(Parser parser, CompilationUnitDeclaration unit) {
        // prevent Groovy types from having their methods re-parsed
        /*if (parser instanceof MultiplexingCommentRecorderParser) {
            super.parseMethods(parser, unit);
        }*/
    }

    @Override
    public boolean isScannerUsableOnThisDeclaration() {
        return false;
    }
    // FIXASC end

    //--------------------------------------------------------------------------

    private GroovyTypeDeclaration[] anonymousTypes;

    private static final GroovyTypeDeclaration[] NO_TYPES = {};

    public GroovyTypeDeclaration[] getAnonymousTypes() {
        return (anonymousTypes != null ? anonymousTypes : NO_TYPES);
    }

    public void addAnonymousType(GroovyTypeDeclaration anonymousType) {
        anonymousTypes = (GroovyTypeDeclaration[]) ArrayUtils.add(getAnonymousTypes(), anonymousType);
    }

    /**
     * Fixes the super types of anonymous inner classes These kinds of classes are always constructed so that they extend the super
     * type, even if the super type is an interface. This is because during parse time we don't know if the super type is a class or
     * interface, se we need to wait until after the resolve phase to fix this.
     */
    public void fixAnonymousTypeBinding() {
        if (classNode.getInterfaces() != null &&
            classNode.getInterfaces().length == 1 &&
            classNode.getSuperClass().getName().equals("java.lang.Object")) {

            superInterfaces = new TypeReference[] {superclass};
            binding.superInterfaces = new ReferenceBinding[] {
                (ReferenceBinding) superclass.resolvedType,
            };
            superclass = null;
        }
    }
}
