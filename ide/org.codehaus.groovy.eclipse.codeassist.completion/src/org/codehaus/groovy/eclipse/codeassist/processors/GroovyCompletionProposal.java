 /*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.eclipse.codeassist.processors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.core.NameLookup;

public class GroovyCompletionProposal extends InternalCompletionProposal {

    /**
     * Named parameters are included in the proposal application after the
     * regular args
     */
    private char[][] namedParameterNames = CharOperation.NO_CHAR_CHAR;

    /**
     * Named parameters are included in the proposal application after the
     * regular args
     */
    private char[][] namedParameterTypeNames = CharOperation.NO_CHAR_CHAR;

    /**
     * Optional parameters are not included in the proposal application
     * Maybe they shouldn't even be here
     */
    private char[][] optionalParameterNames = CharOperation.NO_CHAR_CHAR;

    /**
     * Optional parameters are not included in the proposal application
     * Maybe they shouldn't even be here
     */
    private char[][] optionalParameterTypeNames = CharOperation.NO_CHAR_CHAR;

    /**
     * Regular parameters are the standard, non-named parameters
     */
    private char[][] regularParameterNames = CharOperation.NO_CHAR_CHAR;

    /**
     * Regular parameters are the standard, non-named parameters
     */
    private char[][] regularParameterTypeNames = CharOperation.NO_CHAR_CHAR;


    private boolean useExtraParameters = false;

    public GroovyCompletionProposal(int kind, int completionLocation) {
        super(kind, completionLocation);
    }

    public char[][] getNamedParameterNames() {
        return namedParameterNames;
    }
    public char[][] getNamedParameterTypeNames() {
        return namedParameterTypeNames;
    }
    public char[][] getOptionalParameterNames() {
        return optionalParameterNames;
    }
    public char[][] getOptionalParameterTypeNames() {
        return optionalParameterTypeNames;
    }
    public char[][] getRegularParameterNames() {
        if (useExtraParameters) {
            return regularParameterNames;
        } else {
            return findParameterNames(null);
        }
    }

    public char[][] getRegularParameterTypeNames() {
        if (useExtraParameters) {
            return regularParameterTypeNames;
        } else {
            return parameterTypeNames;
        }
    }

    @Override
    public void setAccessibility(int kind) {
        super.setAccessibility(kind);
    }

    public void setCompletionEngine(CompletionEngine completionEngine) {
        this.completionEngine = completionEngine;
    }

    @Override
    public void setDeclarationTypeName(char[] declarationTypeName) {
        super.setDeclarationTypeName(declarationTypeName);
    }

    public void setNamedParameterNames(char[][] namedParameterNames) {
        this.namedParameterNames = namedParameterNames;
    }

    public void setNamedParameterTypeNames(char[][] namedParameterTypeNames) {
        this.namedParameterTypeNames = namedParameterTypeNames;
    }

    public void setNameLookup(NameLookup lookup) {
        super.nameLookup = lookup;
    }

    public void setOptionalParameterNames(char[][] optionalParameterNames) {
        this.optionalParameterNames = optionalParameterNames;
    }

    public void setOptionalParameterTypeNames(char[][] optionalParameterTypeNames) {
        this.optionalParameterTypeNames = optionalParameterTypeNames;
    }

    @Override
    public void setPackageName(char[] packageName) {
        super.setPackageName(packageName);
    }

    /**
     * sets *all* parameter type names
     */
    @Override
    public void setParameterTypeNames(char[][] parameterTypeNames) {
        super.setParameterTypeNames(parameterTypeNames);
    }

    public void setRegularParameterNames(char[][] regularParameterNames) {
        useExtraParameters = true;
        this.regularParameterNames = regularParameterNames;
    }

    public void setRegularParameterTypeNames(char[][] regularParameterTypeNames) {
        useExtraParameters = true;
        this.regularParameterTypeNames = regularParameterTypeNames;
    }

    @Override
    public void setTypeName(char[] typeName) {
        super.setTypeName(typeName);
    }

    @Override
    protected void setDeclarationPackageName(char[] declarationPackageName) {
        super.setDeclarationPackageName(declarationPackageName);
    }

    @Override
    protected void setIsContructor(boolean isConstructor) {
        super.setIsContructor(isConstructor);
    }

    @Override
    public char[][] findParameterNames(IProgressMonitor monitor) {
        return super.findParameterNames(monitor);
    }

    public boolean hasParameters() {
        return super.getParameterTypeNames() != null && super.getParameterTypeNames().length > 0;
    }
}