/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.testplugin;

import junit.framework.Assert;

import org.eclipse.core.resources.IResource;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;


/**
 * Tests a resource or Java element and all its children
 * for mixed line delimiters and throws an AFE in such a
 * case.
 *
 * @since 3.1
 */
public class MixedLineDelimiterDetector  {

//	private static final int MAC_LD= 0;
//	private static final int MIXED_LD= 1;
//	private static final int UNIX_LD= 2;
//	private static final int WINDOWS_LD= 3;
//
//	private static class ResultCollector implements ITextSearchResultCollector {
//
//		private List fResult;
//		private IProgressMonitor fProgressMonitor= new NullProgressMonitor();
//
//		public ResultCollector(List result) {
//			fResult= result;
//		}
//
//		public void aboutToStart() throws CoreException {
//			// do nothing;
//		}
//
//		public void accept(IResourceProxy proxy, int start, int length) throws CoreException {
//			fResult.add(proxy.requestResource());
//		}
//
//		public void done() throws CoreException {
//			// do nothing;
//		}
//
//		public IProgressMonitor getProgressMonitor() {
//			return fProgressMonitor;
//		}
//	}
//
//	private static class LineDelimiterLocator extends MatchLocator {
//
//		protected static final int fgLF= '\n';
//		protected static final int fgCR= '\r';
//
//		protected int fPushbackChar;
//		protected boolean fPushback;
//		private int fLineDelimiter= -1;
//
//		public LineDelimiterLocator(int lineDelimiter) throws PatternSyntaxException {
//			super("", false, false); // hack - parameters are never used
//			fLineDelimiter= lineDelimiter;
//		}
//
//		public boolean isEmpty() {
//			return false;
//		}
//
//		private BufferedReader getBufferedReader(IFile file) throws CoreException, UnsupportedEncodingException {
//			return new BufferedReader(getReader(file));
//		}
//
//		private Reader getReader(IFile file) throws UnsupportedEncodingException, CoreException{
//			return new InputStreamReader(file.getContents(false), ResourcesPlugin.getEncoding());
//		}
//
//		protected int readLine(BufferedReader reader, StringBuffer sb) throws IOException {
//			int ch= -1;
//			if (fPushback) {
//				ch= fPushbackChar;
//				fPushback= false;
//			}
//			else
//				ch= reader.read();
//			while (ch != -1) {
//				if (ch == fgLF)
//					return UNIX_LD;
//				if (ch == fgCR) {
//					ch= reader.read();
//					if (ch == fgLF)
//						return WINDOWS_LD;
//
//					fPushbackChar= ch;
//					fPushback= true;
//					return MAC_LD;
//				}
//				sb.append((char)ch);
//				ch= reader.read();
//			}
//			return -1;
//		}
//
//		public void locateMatches(IProgressMonitor progressMonitor, CharSequence searchInput, ITextSearchResultCollector collector, IResourceProxy proxy) throws CoreException {
//			IFile file= (IFile)proxy.requestResource();
//			if (file.getContentDescription() == null || !FileBuffers.getTextFileBufferManager().isTextFileLocation(file.getFullPath(), true))
//				return;
//
//			try {
//
//				BufferedReader reader= getBufferedReader(file);
//				boolean eof= false;
//				int detectedLineDelimiter= -1;
//				fPushback= false;
//
//				try {
//					while (!eof) {
//						StringBuffer sb= new StringBuffer(200);
//						int lineDelimiter= readLine(reader, sb);
//
//						eof= (lineDelimiter == -1);
//
//						if (detectedLineDelimiter == -1)
//							detectedLineDelimiter= lineDelimiter;
//
//						if (!eof && lineDelimiter != detectedLineDelimiter) {
//							detectedLineDelimiter= MIXED_LD;
//							break;
//						}
//
//						if (progressMonitor.isCanceled())
//							throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled);
//					}
//					if (fLineDelimiter == detectedLineDelimiter)
//						collector.accept(proxy, 0, 0);
//				} finally {
//					if (reader != null)
//						reader.close();
//				}
//			} catch (IOException e) {
//				String[] args= { e.toString(), file.getFullPath().makeRelative().toString()};
//				String message= Messages.format(SearchMessages.TextSearchVisitor_error, args);
//				throw new CoreException(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, Platform.PLUGIN_ERROR, message, e));
//			} finally {
//			}
//		}
//	}

	public static void assertNoMixedLineDelimiters(IJavaElement elem) {
		IResource resource;
		if (elem instanceof IJavaProject)
			resource= ((IJavaProject)elem).getProject();
		else
			resource= elem.getResource();

		assertNoMixedLineDelimiters(resource);
	}
	public static void assertNoMixedLineDelimiters(IResource resource) {
		IResource[] mixedResources= findFilesWithMixedLineDelimiters(resource);
		Assert.assertTrue(mixedResources == null || mixedResources.length == 0);
	}

	/**
	 * Finds files with mixed line delimiters.
	 * 
	 * @param resource the resource to search
	 * @return the resources with mixed line delimiters
	 */
	private static IResource[] findFilesWithMixedLineDelimiters(final IResource resource) {
//		final List result= new ArrayList(5);
//		ResultCollector collector= new ResultCollector(result);
//		TextSearchEngine engine= new TextSearchEngine();
//		SearchScope scope= SearchScope.newSearchScope("", new IResource[] { resource }); //$NON-NLS-1$
//		engine.search(scope, false, collector, new LineDelimiterLocator(MIXED_LD));
//		return (IResource[])result.toArray(new IResource[result.size()]);

		Assert.fail("support currently disabled");

		return new IResource[0];
	}

}
