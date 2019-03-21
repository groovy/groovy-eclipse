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
package org.codehaus.groovy.eclipse.core.search;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;

/**
 * Java search requestor that finds synthetic accessors.  That is, searching for
 * 'Foo.getBar()' references will yield matches for 'foo.bar', or vice versa.
 */
public class SyntheticAccessorSearchRequestor {

    public void findSyntheticMatches(IJavaElement element, ISearchRequestor uiRequestor, IProgressMonitor monitor) throws CoreException {
        findSyntheticMatches(element, IJavaSearchConstants.REFERENCES | IJavaSearchConstants.IGNORE_RETURN_TYPE, new SearchParticipant[] {new JavaSearchParticipant()}, SearchEngine.createWorkspaceScope(), uiRequestor, monitor);
    }

    public void findSyntheticMatches(IJavaElement element, int limitTo, SearchParticipant[] participants, IJavaSearchScope scope, final ISearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
        if (!(element instanceof IMember) || !GroovyNature.hasGroovyNature(element.getJavaProject().getProject())) {
            return;
        }

        SearchPattern pattern = createPattern(element, limitTo);
        if (pattern == null) {
            return;
        }

        // if looking for setter matches, make sure the synthetic member is non-final
        final boolean checkFlags = (element.getElementType() == IJavaElement.METHOD && element.getElementName().startsWith("set"));

        new SearchEngine().search(pattern, participants, scope, new SearchRequestor() {
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
                if (!checkFlags || !Flags.isFinal(((IMember) match.getElement()).getFlags())) {
                    requestor.acceptMatch(match);
                }
            }
        }, monitor);
    }

    private SearchPattern createPattern(IJavaElement element, int limitTo) throws JavaModelException {
        IJavaElement[] toSearch = {
            findSyntheticProperty(element),
            findSyntheticMember(element, "is"),
            findSyntheticMember(element, "get"),
            findSyntheticMember(element, "set"),
        };

        SearchPattern pattern = null;
        for (IJavaElement searchElt : toSearch) {
            if (searchElt != null) {
                SearchPattern newPattern = SearchPattern.createPattern(searchElt, limitTo);
                if (pattern == null) {
                    pattern = newPattern;
                } else {
                    pattern = SearchPattern.createOrPattern(pattern, newPattern);
                }
            }
        }
        return pattern;
    }

    private IMethod findSyntheticMember(IJavaElement element, String prefix) throws JavaModelException {
        if (element.getElementType() != IJavaElement.FIELD) {
            return null;
        }

        IField field = (IField) element;
        boolean isser = prefix.equals("is");
        boolean setter = prefix.equals("set");

        if (setter && Flags.isFinal(field.getFlags())) {
            return null;
        }
        if (isser && !field.getTypeSignature().equals("Z")) {
            return null;
        }

        String name = prefix + MetaClassHelper.capitalize(field.getElementName());
        String[] params = !setter ? new String[0] : new String[] {field.getTypeSignature()};

        final IMethod method = ((IType) field.getParent()).getMethod(name, params);
        // only return if method doesn't exist since otherwise, this method wouldn't be synthetic
        return method.exists() ? null : syntheticMemberProxy(IMethod.class, method, setter ? "V" : field.getTypeSignature());
    }

    private IField findSyntheticProperty(IJavaElement element) throws JavaModelException {
        if (element.getElementType() != IJavaElement.METHOD) {
            return null;
        }
        String name = element.getElementName();
        if (name.length() <= 2) {
            return null;
        }
        int prefixLength;
        if (name.startsWith("is")) {
            prefixLength = 2;
        } else {
            if (name.length() == 3) {
                return null;
            }
            prefixLength = 3;
        }

        final IField field = ((IType) element.getParent()).getField(Introspector.decapitalize(name.substring(prefixLength)));
        // only return if field doesn't exist since otherwise, this method wouldn't be synthetic
        return field.exists() ? null : syntheticMemberProxy(IField.class, field, "Z"); // type signature appears unused
    }

    //--------------------------------------------------------------------------

    /**
     * Delegates to an actual {@link IMember} for most calls. Also ensures that
     * {@link JavaModelException}s are not thrown for typical operations.
     */
    @SuppressWarnings("unchecked")
    private static <T extends IMember> T syntheticMemberProxy(Class<T> face, final IMember delegate, final String signature) {
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[] {face}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getReturnType") ||
                        method.getName().equals("getTypeSignature")) {
                    return signature;
                } else if (method.getName().equals("getTypeParameters")) {
                    return new ITypeParameter[0];
                } else if (method.getName().equals("getFlags")) {
                    return Flags.AccPublic;
                } else if (method.getName().equals("exists")) {
                    return Boolean.TRUE;
                }
                try {
                    return method.invoke(delegate, args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        });
    }
}
