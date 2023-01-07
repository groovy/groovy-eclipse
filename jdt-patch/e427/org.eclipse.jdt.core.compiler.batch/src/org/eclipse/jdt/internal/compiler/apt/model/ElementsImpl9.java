/*******************************************************************************
 * Copyright (c) 2006, 2021 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Igor Fedorenko - extracted from ElementsImpl
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.util.HashtableOfModule;

/**
 * Utilities for working with java9 language elements.
 * There is one of these for every ProcessingEnvironment.
 */
public class ElementsImpl9 extends ElementsImpl {

	public ElementsImpl9(BaseProcessingEnvImpl env) {
		super(env);
	}

	@Override
	public TypeElement getTypeElement(CharSequence name) {
		final char[][] compoundName = CharOperation.splitOn('.', name.toString().toCharArray());
		Set<? extends ModuleElement> allModuleElements = getAllModuleElements();
		for (ModuleElement moduleElement : allModuleElements) {
			TypeElement t = getTypeElement(compoundName, ((ModuleElementImpl) moduleElement).binding);
			if (t != null) {
				return t;
			}
		}
		return null;
	}

	@Override
	public TypeElement getTypeElement(ModuleElement module, CharSequence name) {
		ModuleBinding mBinding = ((ModuleElementImpl) module).binding;
		final char[][] compoundName = CharOperation.splitOn('.', name.toString().toCharArray());
		return getTypeElement(compoundName, mBinding);
	}

	private TypeElement getTypeElement(final char[][] compoundName, ModuleBinding mBinding) {
		LookupEnvironment le = mBinding == null ? _env.getLookupEnvironment() : mBinding.environment;
		ReferenceBinding binding = mBinding == null ? le.getType(compoundName) : le.getType(compoundName, mBinding);
		// If we didn't find the binding, maybe it's a nested type;
		// try finding the top-level type and then working downwards.
		if (null == binding) {
			ReferenceBinding topLevelBinding = null;
			int topLevelSegments = compoundName.length;
			while (--topLevelSegments > 0) {
				char[][] topLevelName = new char[topLevelSegments][];
				for (int i = 0; i < topLevelSegments; ++i) {
					topLevelName[i] = compoundName[i];
				}
				topLevelBinding = le.getType(topLevelName);
				if (null != topLevelBinding) {
					break;
				}
			}
			if (null == topLevelBinding) {
				return null;
			}
			binding = topLevelBinding;
			for (int i = topLevelSegments; null != binding && i < compoundName.length; ++i) {
				binding = binding.getMemberType(compoundName[i]);
			}
		}
		if (null == binding) {
			return null;
		}
		if ((binding.tagBits & TagBits.HasMissingType) != 0) {
			return null;
		}
		return new TypeElementImpl(_env, binding, null);
	}


	@Override
	public Origin getOrigin(Element e) {
		return Origin.EXPLICIT;
	}

	@Override
	public Origin getOrigin(AnnotatedConstruct c, AnnotationMirror a) {
		return Origin.EXPLICIT;
	}

	@Override
	public Origin getOrigin(ModuleElement m, ModuleElement.Directive directive) {
		return Origin.EXPLICIT;
	}

	@Override
	public boolean isBridge(ExecutableElement e) {
		MethodBinding methodBinding = (MethodBinding) ((ExecutableElementImpl) e)._binding;
		return methodBinding.isBridge();
	}

	@Override
	public ModuleElement getModuleOf(Element elem) {
		if (elem instanceof ModuleElement) {
			return (ModuleElement) elem;
		}
		Element parent = elem.getEnclosingElement();
		while (parent != null) {
			if (parent instanceof ModuleElement) {
				return (ModuleElement) parent;
			}
			parent = parent.getEnclosingElement();
		}
		return null;
	}

	@Override
	public ModuleElement getModuleElement(CharSequence name) {
		LookupEnvironment lookup = _env.getLookupEnvironment();
		ModuleBinding binding = lookup.getModule(name.length() == 0 ? ModuleBinding.UNNAMED : name.toString().toCharArray());
		//TODO: Surely there has to be a better way than calling toString().toCharArray()?
		if (binding == null) {
			return null;
		}
		return new ModuleElementImpl(_env, binding);
	}

	@Override
	public Set<? extends ModuleElement> getAllModuleElements() {
		LookupEnvironment lookup = _env.getLookupEnvironment();
		HashtableOfModule knownModules = lookup.knownModules;
		ModuleBinding[] modules = knownModules.valueTable;
		if (modules == null || modules.length == 0) {
			return Collections.emptySet();
		}
		Set<ModuleElement> mods = new HashSet<>(modules.length);
		for (ModuleBinding moduleBinding : modules) {
			if (moduleBinding == null)
				continue;
			ModuleElement element = (ModuleElement) _env.getFactory().newElement(moduleBinding);
			mods.add(element);
		}
		mods.add((ModuleElement) _env.getFactory().newElement(lookup.UnNamedModule));
		return mods;
	}

	@Override
	public
	PackageElement getPackageElement(ModuleElement module, CharSequence name) {
		ModuleBinding mBinding = ((ModuleElementImpl) module).binding;
		final char[][] compoundName = CharOperation.splitOn('.', name.toString().toCharArray());
		PackageBinding p = null;
		if (mBinding != null) {
			p = mBinding.getVisiblePackage(compoundName);
		} else {
			p = _env.getLookupEnvironment().createPackage(compoundName);
		}
		if (p == null || !p.isValidBinding())
			return null;
		return (PackageElement) _env.getFactory().newElement(p);
	}
	@Override
	public boolean isAutomaticModule(ModuleElement module) {
		ModuleBinding mBinding = ((ModuleElementImpl) module).binding;
        return mBinding.isAutomatic();
    }
}
