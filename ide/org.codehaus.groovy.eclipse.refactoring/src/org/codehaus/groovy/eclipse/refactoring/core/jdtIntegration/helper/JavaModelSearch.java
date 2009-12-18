/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * Searches the Java Model of a given project with the specified pattern
 * 
 * @author Stefan Reinhard
 */
public class JavaModelSearch {

	private IJavaProject project;
	private SearchPattern pattern;

	public JavaModelSearch(IJavaProject project, SearchPattern pattern) {
		this.project = project;
		this.pattern = pattern;
	}

	/**
	 * Returns the first element found
	 * 
	 * @throws CoreException
	 */
	public <T> T searchFirst(Class<? extends T> type) throws CoreException {
		for (T first : searchAll(type)) {
			return first;
		}
		return null;
	}

	/**
	 * Searches for all elements matching the given Pattern and casts them to
	 * the parameterized Type.
	 * 
	 * @throws CoreException
	 */
	public <T> List<T> searchAll(final Class<? extends T> type) throws CoreException {
		Assert.isNotNull(pattern);
		Assert.isNotNull(project);
		SearchEngine engine = new SearchEngine();

		IJavaElement[] elements = { project.getJavaProject() };
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements, IJavaSearchScope.SOURCES);

		final LinkedList<T> results = new LinkedList<T>();

		SearchRequestor requestor = new SearchRequestor() {

			@Override
            @SuppressWarnings("unchecked")
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				Object element = match.getElement();
				if (type.isInstance(element)) {
					results.add((T)element);
				}
			}
		};

		SearchParticipant[] participants = new SearchParticipant[] { SearchEngine
				.getDefaultSearchParticipant() };

		engine.search(pattern, participants, scope, requestor,
				new NullProgressMonitor());

		return results;
	}

}
