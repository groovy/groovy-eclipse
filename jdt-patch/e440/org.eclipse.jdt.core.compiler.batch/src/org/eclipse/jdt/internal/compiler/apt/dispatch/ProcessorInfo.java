/*******************************************************************************
 * Copyright (c) 2006, 2011 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Cached information associated with a {@link Processor} in the context
 * of annotation processor dispatch.
 * <p>
 * This class supports inclusion in a collection by implementing
 * equals() and hashCode().  Its concept of identity is based on
 * the class object of the Processor that it wraps; so, for instance,
 * it is not possible to have a Set that contains more than one
 * instance of a particular Processor class.  In fact, it is possible
 * to have more than one instance of a Processor if there are multiple
 * build threads, but within the context of a particular dispatch
 * manager, there will only be one of any given Processor class.
 */
public class ProcessorInfo {
	final Processor _processor;
	final Set<String> _supportedOptions;
	final SourceVersion _supportedSourceVersion;

	private final Pattern _supportedAnnotationTypesPattern;
	private final boolean _supportsStar;
	private boolean _hasBeenCalled;

	/**
	 * Create a ProcessorInfo wrapping a particular Processor. The Processor must already have been
	 * initialized (that is,
	 * {@link Processor#init(javax.annotation.processing.ProcessingEnvironment)} must already have
	 * been called). Its getSupportedXXX() methods will be called and the results will be cached.
	 */
	public ProcessorInfo(Processor p)
	{
		this._processor = p;
		this._hasBeenCalled = false;
		this._supportedSourceVersion = p.getSupportedSourceVersion();
		this._supportedOptions = p.getSupportedOptions();
		Set<String> supportedAnnotationTypes = p.getSupportedAnnotationTypes();

		boolean supportsStar = false;
		if (null != supportedAnnotationTypes && !supportedAnnotationTypes.isEmpty()) {
			StringBuilder regex = new StringBuilder();
			Iterator<String> iName = supportedAnnotationTypes.iterator();
			while (true) {
				String name = iName.next();
				supportsStar |= "*".equals(name);  //$NON-NLS-1$
				String escapedName1 = name.replace(".", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
				String escapedName2 = escapedName1.replace("*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
				regex.append(escapedName2);
				if (!iName.hasNext()) {
					break;
				}
				regex.append('|');
			}
			this._supportedAnnotationTypesPattern = Pattern.compile(regex.toString());
		}
		else {
			this._supportedAnnotationTypesPattern = null;
		}
		this._supportsStar = supportsStar;
	}

	/**
	 * Compute the subset of <code>annotations</code> that are described by <code>annotationTypes</code>,
	 * and determine whether the processor should be called.  A processor will be called if it has
	 * any annotations to process, or if it supports "*", or if it was called in a previous round.
	 * If the return value of this method is true once for a given processor, then it will always be true on
	 * subsequent calls.
	 *
	 * @param annotations a set of annotation types
	 * @param result an empty modifiable set, which upon return will contain a subset of <code>annotations</code>, which may be empty but will not be null.
	 * @return true if the processor should be called on this round.
	 */
	public boolean computeSupportedAnnotations(Set<TypeElement> annotations, Set<TypeElement> result)
	{
		if (null != annotations && !annotations.isEmpty() && null != this._supportedAnnotationTypesPattern) {
			for (TypeElement annotation : annotations) {
				Matcher matcher = this._supportedAnnotationTypesPattern.matcher(annotation.getQualifiedName().toString());
				if (matcher.matches()) {
					result.add(annotation);
				}
			}
		}
		boolean call = this._hasBeenCalled || this._supportsStar || !result.isEmpty();
		this._hasBeenCalled |= call;
		return call;
	}

	/**
	 * @return true if the processor included "*" among its list of supported annotations.
	 */
	public boolean supportsStar()
	{
		return this._supportsStar;
	}

	/**
	 * Must be called at the beginning of a build to ensure that no information is
	 * carried over from the previous build.  In particular, processors are
	 * required to be called on every round after the round in which they are
	 * first called; this method resets the "has been called" flag.
	 */
	public void reset()
	{
		this._hasBeenCalled = false;
	}

	@Override
	public int hashCode() {
		return this._processor.getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ProcessorInfo other = (ProcessorInfo) obj;
		if (!this._processor.getClass().equals(other._processor.getClass()))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return this._processor.getClass().getName();
	}

	/**
	 * @return a string representing the set of supported annotation types, in a format
	 * suitable for debugging.  The format is unspecified and subject to change.
	 */
	public String getSupportedAnnotationTypesAsString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Iterator<String> iAnnots = this._processor.getSupportedAnnotationTypes().iterator();
		boolean hasNext = iAnnots.hasNext();
		while (hasNext) {
			sb.append(iAnnots.next());
			hasNext = iAnnots.hasNext();
			if (hasNext) {
				sb.append(',');
			}
		}
		sb.append(']');
		return sb.toString();
	}
}

