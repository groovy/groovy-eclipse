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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
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

    @Override
    public ClassScope newClassScope(Scope outer) {
        return new GroovyClassScope(outer, this);
    }

    @Override
    public void resolve() {
        if (memberTypes != null && memberTypes.length > 0) {
            for (TypeDeclaration type : memberTypes) {
                type.resolve(scope);
            }
        }
        for (AbstractMethodDeclaration method : methods) {
            method.resolve(scope);
            if (method.isClinit()) {
                method.resolveStatements();
            }
        }
        for (FieldDeclaration field : fields) {
            if (field instanceof Initializer) {
                field.resolve(field.isStatic() ? staticInitializerScope : initializerScope);
            } else {
                // fields resolved earlier in GroovyClassScope
            }
        }
    }

    //--------------------------------------------------------------------------

    private GroovyTypeDeclaration[] anonymousTypes;

    private static final GroovyTypeDeclaration[] NO_TYPES = {};

    public GroovyTypeDeclaration[] getAnonymousTypes() {
        return (anonymousTypes != null ? anonymousTypes : NO_TYPES);
    }

    public void addAnonymousType(GroovyTypeDeclaration anonymousType) {
        anonymousTypes = (GroovyTypeDeclaration[]) ArrayUtils.add(getAnonymousTypes(), anonymousType);
    }
}
