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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class GroovyTypeDeclaration extends TypeDeclaration {

    public List<PropertyNode> properties;

    // The Groovy ClassNode that gave rise to this type declaration
    private ClassNode classNode;

    public GroovyTypeDeclaration(CompilationResult compilationResult, ClassNode classNode) {
        super(compilationResult);
        this.classNode = classNode;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    // FIXASC Is this always what we want to do - are there any other implications?

    @Override
    public void parseMethods(Parser parser, CompilationUnitDeclaration unit) {
    // prevent Groovy types from having their methods re-parsed
    //	if (parser instanceof MultiplexingCommentRecorderParser) {
    //		super.parseMethods(parser, unit);
    //	}
    }

    @Override
    public boolean isScannerUsableOnThisDeclaration() {
        return false;
    }

    // FIXASC end

    //--------------------------------------------------------------------------

    public BlockScope enclosingScope;

    /**
     * Anonymous types that are declared in this type's methods
     */
    private GroovyTypeDeclaration[] anonymousTypes;

    public GroovyTypeDeclaration[] getAnonymousTypes() {
        return anonymousTypes;
    }

    public void addAnonymousType(GroovyTypeDeclaration anonymousType) {
        if (anonymousTypes == null) {
            anonymousTypes = new GroovyTypeDeclaration[] { anonymousType };
        } else {
            GroovyTypeDeclaration[] newTypes = new GroovyTypeDeclaration[anonymousTypes.length + 1];
            System.arraycopy(anonymousTypes, 0, newTypes, 0, anonymousTypes.length);
            newTypes[anonymousTypes.length] = anonymousType;
            anonymousTypes = newTypes;
        }
    }

    /**
     * Fixes the super types of anonymous inner classes These kinds of classes are always constructed so that they extend the super
     * type, even if the super type is an interface. This is because during parse time we don't know if the super type is a class or
     * interface, se we need to wait until after the resolve phase to fix this.
     */
    public void fixAnonymousTypeBinding(GroovyCompilationUnitScope groovyCompilationUnitScope) {
        if ((this.bits & ASTNode.IsAnonymousType) != 0) {
            if (classNode.getInterfaces() != null && classNode.getInterfaces().length == 1
                    && classNode.getSuperClass().getName().equals("java.lang.Object")) {

                this.superInterfaces = new TypeReference[] { this.superclass };
                this.binding.superInterfaces = new ReferenceBinding[] { (ReferenceBinding) this.superclass.resolvedType };
                this.superclass = null;

            }
        }
        if (anonymousTypes != null) {
            fixAnonymousTypeDeclarations(anonymousTypes, groovyCompilationUnitScope);
        }
    }

    private void fixAnonymousTypeDeclarations(GroovyTypeDeclaration[] types, Scope parentScope) {
        for (GroovyTypeDeclaration type : types) {
            GroovyClassScope anonScope = new GroovyClassScope(parentScope, type);
            type.scope = anonScope;
            if (type.anonymousTypes != null) {
                fixAnonymousTypeDeclarations(type.anonymousTypes, anonScope);
            }
        }
    }
}
