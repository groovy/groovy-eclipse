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
package org.codehaus.groovy.eclipse.core.search;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
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
 * Search requestor that finds synthetic accessors
 *
 * @author andrew
 * @created Oct 3, 2011
 */
public class SyntheticAccessorSearchRequestor {
    /**
     * This class delegates to an actual {@link IMethod} for most calls
     * It also ensures that {@link JavaModelException}s are not thrown
     * unless an unsupported operation is called.
     *
     * @author andrew
     * @created Oct 5, 2011
     */
    public class MethodWrapper implements IMethod {

        private final IMethod delegate;

        private final String[] parameterNames;

        public MethodWrapper(IMethod method, String[] parameterNames) {
            delegate = method;
            this.parameterNames = parameterNames;
        }

        public String[] getCategories() throws JavaModelException {
            return ((IMember) delegate.getParent()).getCategories();
        }

        public IClassFile getClassFile() {
            return delegate.getClassFile();
        }

        public ICompilationUnit getCompilationUnit() {
            return delegate.getCompilationUnit();
        }

        public IType getDeclaringType() {
            return delegate.getDeclaringType();
        }

        public int getFlags() throws JavaModelException {
            return ((IMember) delegate.getParent()).getFlags();
        }

        public ISourceRange getJavadocRange() throws JavaModelException {
            return ((IMember) delegate.getParent()).getJavadocRange();
        }

        public int getOccurrenceCount() {
            return delegate.getOccurrenceCount();
        }

        public ITypeRoot getTypeRoot() {
            return delegate.getTypeRoot();
        }

        public IType getType(String name, int occurrenceCount) {
            return delegate.getType(name, occurrenceCount);
        }

        public boolean isBinary() {
            return delegate.isBinary();
        }

        // synthetic method, but pretend that it exists
        public boolean exists() {
            return true;
        }

        public IJavaElement getAncestor(int ancestorType) {
            return delegate.getAncestor(ancestorType);
        }

        public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
            return delegate.getParent().getAttachedJavadoc(monitor);
        }

        public IResource getCorrespondingResource() throws JavaModelException {
            return delegate.getParent().getCorrespondingResource();
        }

        public int getElementType() {
            return delegate.getElementType();
        }

        public String getHandleIdentifier() {
            return delegate.getHandleIdentifier();
        }

        public IJavaModel getJavaModel() {
            return delegate.getJavaModel();
        }

        public IJavaProject getJavaProject() {
            return delegate.getJavaProject();
        }

        public IOpenable getOpenable() {
            return delegate.getOpenable();
        }

        public IJavaElement getParent() {
            return delegate.getParent();
        }

        public IPath getPath() {
            return delegate.getPath();
        }

        public IJavaElement getPrimaryElement() {
            return delegate.getPrimaryElement();
        }

        public IResource getResource() {
            return delegate.getResource();
        }

        public ISchedulingRule getSchedulingRule() {
            return delegate.getSchedulingRule();
        }

        public IResource getUnderlyingResource() throws JavaModelException {
            return delegate.getParent().getUnderlyingResource();
        }

        public boolean isReadOnly() {
            return delegate.isReadOnly();
        }

        public boolean isStructureKnown() throws JavaModelException {
            return delegate.getParent().isStructureKnown();
        }

        public Object getAdapter(Class adapter) {
            return delegate.getAdapter(adapter);
        }

        public String getSource() throws JavaModelException {
            return ((ISourceReference) delegate.getParent()).getSource();
        }

        public ISourceRange getSourceRange() throws JavaModelException {
            return ((ISourceReference) delegate.getParent()).getSourceRange();
        }

        public ISourceRange getNameRange() throws JavaModelException {
            return ((IMethod) delegate.getParent()).getNameRange();
        }

        public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor)
                throws JavaModelException {
            // will throw exception
            delegate.copy(container, sibling, rename, replace, monitor);
        }

        public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
            // will throw exception
            delegate.delete(force, monitor);
        }

        public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor)
                throws JavaModelException {
            // will throw exception
            delegate.move(container, sibling, rename, replace, monitor);
        }

        public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException {
            // will throw exception
            delegate.rename(name, replace, monitor);
        }

        public IJavaElement[] getChildren() throws JavaModelException {
            return new IJavaElement[0];
        }

        public boolean hasChildren() throws JavaModelException {
            return false;
        }

        public IAnnotation getAnnotation(String name) {
            return delegate.getAnnotation(name);
        }

        public IAnnotation[] getAnnotations() throws JavaModelException {
            return ((IAnnotatable) delegate.getParent()).getAnnotations();
        }

        public IMemberValuePair getDefaultValue() throws JavaModelException {
            return null;
        }

        public String getElementName() {
            return delegate.getElementName();
        }

        public String[] getExceptionTypes() throws JavaModelException {
            return new String[0];
        }

        public String[] getTypeParameterSignatures() throws JavaModelException {
            return new String[0];
        }

        public ITypeParameter[] getTypeParameters() throws JavaModelException {
            return new ITypeParameter[0];
        }

        public int getNumberOfParameters() {
            return delegate.getNumberOfParameters();
        }

        public ILocalVariable[] getParameters() throws JavaModelException {
            return new ILocalVariable[0];
        }

        public String getKey() {
            return delegate.getKey();
        }

        public String[] getParameterNames() throws JavaModelException {
            return parameterNames;
        }

        public String[] getParameterTypes() {
            return delegate.getParameterTypes();
        }

        public String[] getRawParameterNames() throws JavaModelException {
            return parameterNames;
        }

        public String getReturnType() throws JavaModelException {
            // will throw exception
            return delegate.getReturnType();
        }

        public String getSignature() throws JavaModelException {
            // will throw exception
            return delegate.getSignature();
        }

        public ITypeParameter getTypeParameter(String name) {
            return delegate.getTypeParameter(name);
        }

        public boolean isConstructor() throws JavaModelException {
            return false;
        }

        public boolean isMainMethod() throws JavaModelException {
            return false;
        }

        public boolean isResolved() {
            return false;
        }

        public boolean isSimilar(IMethod method) {
            return delegate.isSimilar(method);
        }

        public boolean reallyExists() {
            return delegate.exists();
        }

    }

    private class Requestor extends SearchRequestor {
        private final ISearchRequestor uiRequestor;

        public Requestor(ISearchRequestor uiRequestor) {
            this.uiRequestor = uiRequestor;
        }

        @Override
        public void acceptSearchMatch(SearchMatch match) throws CoreException {
            uiRequestor.acceptMatch(match);
        }
    }

    public void findSyntheticMatches(IJavaElement element, ISearchRequestor uiRequestor, IProgressMonitor monitor)
            throws CoreException {
        // findSyntheticMatches(element, IJavaSearchConstants.REFERENCES, new
        // SearchParticipant[] { new JavaSearchParticipant() },
        // SearchEngine.createJavaSearchScope(new IJavaElement[] { element }),
        // uiRequestor, monitor);
        findSyntheticMatches(element, IJavaSearchConstants.REFERENCES, new SearchParticipant[] { new JavaSearchParticipant() },
                SearchEngine.createWorkspaceScope(), uiRequestor, monitor);
    }

    public void findSyntheticMatches(IJavaElement element, int limitTo, SearchParticipant[] participants,
            IJavaSearchScope scope, ISearchRequestor uiRequestor, IProgressMonitor monitor) throws CoreException {
        if (!isInteresting(element)) {
            return;
        }

        // the declaration is synthetic, so OK to ignore
        if (limitTo == IJavaSearchConstants.DECLARATIONS) {
            return;
        }

        SearchPattern pattern = createPattern(element);
        if (pattern == null) {
            return;
        }

        Requestor requestor = new Requestor(uiRequestor);
        SearchEngine engine = new SearchEngine();
        engine.search(pattern, participants, scope, requestor, monitor);
    }

    private SearchPattern createPattern(IJavaElement element) throws JavaModelException {
        List<IJavaElement> toSearch = new ArrayList<IJavaElement>(4);
        toSearch.add(findSyntheticMember(element, "is"));
        toSearch.add(findSyntheticMember(element, "get"));
        toSearch.add(findSyntheticMember(element, "set"));
        toSearch.add(findSyntheticProperty(element));
        SearchPattern pattern = null;
        for (IJavaElement searchElt : toSearch) {
            if (searchElt != null) {
                SearchPattern newPattern = SearchPattern.createPattern(searchElt, IJavaSearchConstants.ALL_OCCURRENCES
                        | IJavaSearchConstants.IGNORE_RETURN_TYPE);
                if (pattern == null) {
                    pattern = newPattern;
                } else {
                    pattern = SearchPattern.createOrPattern(pattern, newPattern);
                }
            }
        }
        return pattern;
    }

    private boolean isInteresting(IJavaElement element) {
        return element instanceof IMember && GroovyNature.hasGroovyNature(element.getJavaProject().getProject());
    }

    private IMethod findSyntheticMember(IJavaElement element, String prefix) throws JavaModelException {
        if (element.getElementType() != IJavaElement.FIELD) {
            return null;
        }
        IType parent = (IType) element.getParent();
        String[] sigs;
        String[] names;
        if (prefix.equals("set")) {
            sigs = new String[] { ((IField) element).getTypeSignature() };
            names = new String[] { element.getElementName() };
        } else {
            sigs = new String[0];
            names = new String[0];
        }
        MethodWrapper method = new MethodWrapper(parent.getMethod(convertName(prefix, element.getElementName()), sigs), names);
        // only return if method doesn't exist since otherwise, this method would not be synthetic
        return method.reallyExists() ? null : method;
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

        String fieldName = "" + Character.toLowerCase(name.charAt(prefixLength)) + name.substring(prefixLength+1);
        IType parent = (IType) element.getParent();
        IField field = parent.getField(fieldName);
        // only return if field doesn't exist since otherwise, this method would
        // not be synthetic
        return field.exists() && Flags.isProtected(field.getFlags()) ? null : field;
    }

    private String convertName(String prefix, String elementName) {
        return prefix + Character.toUpperCase(elementName.charAt(0)) + elementName.subSequence(1, elementName.length());
    }
}
