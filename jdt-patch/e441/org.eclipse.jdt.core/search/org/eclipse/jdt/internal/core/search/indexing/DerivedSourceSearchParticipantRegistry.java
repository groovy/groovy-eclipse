/*******************************************************************************
 * Copyright (c) 2026 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Arcadiy Ivanov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.DerivedSourceSearchParticipant;

/**
 * Registry for search participants contributed via the
 * {@code org.eclipse.jdt.core.derivedSourceSearchParticipant} extension point.
 * <p>
 * Each contributed participant declares a set of file extensions it handles.
 * The registry maps file extensions to eagerly-instantiated participant instances.
 * <p>
 * The registry listens for extension additions and removals via
 * {@link IRegistryEventListener}, so dynamically loaded or unloaded bundles
 * are reflected automatically without restart.
 */
public class DerivedSourceSearchParticipantRegistry implements IRegistryEventListener {

	private static final String EXTENSION_POINT_ID = JavaCore.PLUGIN_ID + ".derivedSourceSearchParticipant"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "derivedSourceSearchParticipant"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_FILE_EXTENSIONS = "fileExtensions"; //$NON-NLS-1$
	private static final String ATTR_LANGUAGE_ID = "languageId"; //$NON-NLS-1$

	private static final DerivedSourceSearchParticipantRegistry INSTANCE = new DerivedSourceSearchParticipantRegistry();

	/** file extension &rarr; instantiated participant. Guarded by {@code synchronized(INSTANCE)}. */
	private final Map<String, DerivedSourceSearchParticipant> participantsByExtension = new HashMap<>();

	/** file extension &rarr; LSP language identifier. Guarded by {@code synchronized(INSTANCE)}. */
	private final Map<String, String> languageIdsByExtension = new HashMap<>();

	private DerivedSourceSearchParticipantRegistry() {
		Platform.getExtensionRegistry().addListener(this, EXTENSION_POINT_ID);
		synchronized (this) {
			load();
		}
	}

	/** Must be called while holding {@code synchronized(this)}. */
	private void load() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID);
		// group config elements by identity so each participant class is instantiated once
		Map<IConfigurationElement, Set<String>> extensionsByConfig = new HashMap<>();
		for (IConfigurationElement element : elements) {
			if (!ELEMENT_NAME.equals(element.getName())) continue;
			String fileExtensions = element.getAttribute(ATTR_FILE_EXTENSIONS);
			if (fileExtensions == null || fileExtensions.isBlank()) continue;
			for (String ext : fileExtensions.split(",")) { //$NON-NLS-1$
				String trimmed = ext.trim().toLowerCase();
				if (!trimmed.isEmpty()) {
					extensionsByConfig.computeIfAbsent(element, k -> new LinkedHashSet<>()).add(trimmed);
				}
			}
		}
		for (Map.Entry<IConfigurationElement, Set<String>> entry : extensionsByConfig.entrySet()) {
			IConfigurationElement config = entry.getKey();
			Set<String> exts = entry.getValue();
			try {
				Object instance = config.createExecutableExtension(ATTR_CLASS);
				if (instance instanceof DerivedSourceSearchParticipant sp) {
					String languageId = config.getAttribute(ATTR_LANGUAGE_ID);
					for (String ext : exts) {
						DerivedSourceSearchParticipant existing = this.participantsByExtension.put(ext, sp);
						if (existing != null && existing != sp) {
							ILog.get().warn("Duplicate derivedSourceSearchParticipant registration for extension '" //$NON-NLS-1$
									+ ext + "': '" + config.getAttribute(ATTR_ID) //$NON-NLS-1$
									+ "' overrides previous participant"); //$NON-NLS-1$
						}
						String langId = languageId != null && !languageId.isBlank()
								? languageId.trim() : ext;
						this.languageIdsByExtension.put(ext, langId);
					}
				} else {
					ILog.get().error("derivedSourceSearchParticipant '" + config.getAttribute(ATTR_ID) //$NON-NLS-1$
							+ "' class does not extend DerivedSourceSearchParticipant: " //$NON-NLS-1$
							+ config.getAttribute(ATTR_CLASS));
				}
			} catch (CoreException e) {
				ILog.get().error("Could not instantiate derivedSourceSearchParticipant: '" //$NON-NLS-1$
						+ config.getAttribute(ATTR_ID) + "'", e); //$NON-NLS-1$
			}
		}
	}

	private synchronized void reload() {
		this.participantsByExtension.clear();
		this.languageIdsByExtension.clear();
		load();
	}

	/**
	 * Disposes this registry by removing its extension registry listener.
	 * <p>
	 * This should be called during JavaCore/JavaModelManager shutdown to
	 * avoid the extension registry retaining references to this classloader
	 * after the {@code org.eclipse.jdt.core} bundle is stopped or updated.
	 */
	public void dispose() {
		Platform.getExtensionRegistry().removeListener(this);
	}

	/**
	 * Disposes the singleton instance of this registry.
	 * <p>
	 * Intended to be invoked from JavaCore/JavaModelManager shutdown.
	 */
	public static void disposeInstance() {
		INSTANCE.dispose();
	}

	/**
	 * Returns the search participant registered for the given file extension,
	 * or {@code null} if none.
	 *
	 * @param fileExtension file extension without dot, e.g. {@code "kt"}
	 * @return the search participant, or {@code null}
	 */
	public static DerivedSourceSearchParticipant getParticipant(String fileExtension) {
		if (fileExtension == null) return null;
		synchronized (INSTANCE) {
			return INSTANCE.participantsByExtension.get(fileExtension.toLowerCase());
		}
	}

	/**
	 * Returns all contributed search participants. Does not include the default
	 * Java search participant.
	 *
	 * @return array of contributed participants (may be empty, never null)
	 */
	public static DerivedSourceSearchParticipant[] getContributedParticipants() {
		synchronized (INSTANCE) {
			Set<DerivedSourceSearchParticipant> unique = new LinkedHashSet<>(INSTANCE.participantsByExtension.values());
			return unique.toArray(new DerivedSourceSearchParticipant[0]);
		}
	}

	/**
	 * Returns the LSP language identifier for the given file extension,
	 * or {@code null} if no search participant is registered for it.
	 * <p>
	 * If the participant's extension point registration includes a
	 * {@code languageId} attribute, that value is returned. Otherwise,
	 * the file extension itself is used as the language identifier.
	 *
	 * @param fileExtension file extension without dot, e.g. {@code "kt"}
	 * @return the language identifier (e.g. {@code "kotlin"}), or {@code null}
	 */
	public static String getLanguageId(String fileExtension) {
		if (fileExtension == null) return null;
		synchronized (INSTANCE) {
			return INSTANCE.languageIdsByExtension.get(fileExtension.toLowerCase());
		}
	}

	/**
	 * Returns whether the given file extension has a registered search participant.
	 *
	 * @param fileExtension file extension without dot
	 * @return true if a participant is registered for this extension
	 */
	public static boolean hasParticipant(String fileExtension) {
		if (fileExtension == null) return false;
		synchronized (INSTANCE) {
			return INSTANCE.participantsByExtension.containsKey(fileExtension.toLowerCase());
		}
	}

	/**
	 * Resets the registry, forcing a fresh reload from the extension registry.
	 * Intended for testing only.
	 */
	public static void reset() {
		INSTANCE.reload();
	}

	/**
	 * Returns the file extension from a file name, or {@code null} if none.
	 *
	 * @param fileName the file name (e.g. {@code "Foo.kt"})
	 * @return the extension without dot (e.g. {@code "kt"}), or {@code null}
	 */
	public static String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex < 0) return null;
		return fileName.substring(dotIndex + 1);
	}

	@Override
	public void added(IExtension[] extensions) {
		reload();
	}

	@Override
	public void removed(IExtension[] extensions) {
		reload();
	}

	@Override
	public void added(IExtensionPoint[] extensionPoints) {
		// not applicable — we listen for extension additions, not extension point additions
	}

	@Override
	public void removed(IExtensionPoint[] extensionPoints) {
		// not applicable
	}
}
