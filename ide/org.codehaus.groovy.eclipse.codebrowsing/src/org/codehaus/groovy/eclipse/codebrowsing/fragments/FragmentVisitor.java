/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.fragments;

/**
 * Visits an {@link IASTFragment}. Override appropriate methods.
 * Return false if the visit should be completed.
 *
 * @author andrew
 * @created Jun 5, 2010
 */
public abstract class FragmentVisitor {

    public boolean previsit(IASTFragment fragment) {
        return true;
    }

    public boolean visit(PropertyExpressionFragment fragment) {
        return true;
    }

    public boolean visit(MethodCallFragment fragment) {
        return true;
    }

    public boolean visit(SimpleExpressionASTFragment fragment) {
        return true;
    }

    public boolean visit(BinaryExpressionFragment fragment) {
        return true;
    }

    public boolean visit(EnclosingASTNodeFragment fragment) {
        return true;
    }

    public boolean visit(EmptyASTFragment fragment) {
        return true;
    }
}
