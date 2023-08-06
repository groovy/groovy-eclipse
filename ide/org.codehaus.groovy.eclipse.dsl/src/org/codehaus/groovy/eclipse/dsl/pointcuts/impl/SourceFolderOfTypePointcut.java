/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import java.util.Collection;
import java.util.Collections;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Tests that the file of the current type of expression being analyzed is in
 * the given source folder
 *
 * Argument should be the workspace relative path to the source folder, using '/'
 * as a path separator.
 */
public class SourceFolderOfTypePointcut extends AbstractPointcut {

    public SourceFolderOfTypePointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        String sourceFolder = extractFileName(toType(toMatch), pattern);
        if (sourceFolder != null && sourceFolder.startsWith((String) getFirstArgument())) {
            return Collections.singleton(pattern.fullPathName);
        } else {
            return null;
        }
    }

    private ClassNode toType(Object toMatch) {
        if (toMatch instanceof ClassNode) {
            return (ClassNode) toMatch;
        } else if (toMatch instanceof AnnotatedNode) {
            return ((AnnotatedNode) toMatch).getDeclaringClass();
        } else {
            return null;
        }
    }

    private String extractFileName(ClassNode type, GroovyDSLDContext pattern) {
        if (type == null) {
            return null;
        }
        ClassNode redirect = type.redirect();
        if (redirect instanceof JDTClassNode) {
            JDTClassNode jdtClass = (JDTClassNode) redirect;
            char[] fileName = jdtClass.getJdtBinding().getFileName();
            if (fileName != null) {
                // now remove the project name
                int slashIndex = CharOperation.indexOf('/', fileName);
                if (slashIndex >= 0) {
                    // need the second slash
                    slashIndex = CharOperation.indexOf('/', fileName, slashIndex + 1);
                }
                if (slashIndex > 0) {
                    return String.valueOf(CharOperation.subarray(fileName, slashIndex + 1, fileName.length));
                }
            }
        } else {
            // check if this type is in the current file
            ModuleNode module = pattern.getCurrentScope().getEnclosingTypeDeclaration().getModule();
            if (module != null && module.getClasses().contains(redirect)) {
                return pattern.fullPathName;
            }
        }

        // will be "" for primitive and other core types loaded by ClassHelper.
        return "";
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String maybeStatus = allArgsAreStrings();
        if (maybeStatus != null) {
            throw new PointcutVerificationException(maybeStatus, this);
        }
        maybeStatus = hasOneArg();
        if (maybeStatus != null) {
            throw new PointcutVerificationException(maybeStatus, this);
        }
        super.verify();
    }
}
