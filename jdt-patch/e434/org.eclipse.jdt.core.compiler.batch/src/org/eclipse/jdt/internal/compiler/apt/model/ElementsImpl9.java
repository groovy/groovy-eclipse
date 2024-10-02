/*******************************************************************************
 * Copyright (c) 2006, 2023 BEA Systems, Inc. and others
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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BinaryModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;
import org.eclipse.jdt.internal.compiler.tool.PathFileObject;
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
		LookupEnvironment le = mBinding == null ? this._env.getLookupEnvironment() : mBinding.environment;
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
		return new TypeElementImpl(this._env, binding, null);
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
		LookupEnvironment lookup = this._env.getLookupEnvironment();
		ModuleBinding binding = lookup.getModule(name.length() == 0 ? ModuleBinding.UNNAMED : name.toString().toCharArray());
		//TODO: Surely there has to be a better way than calling toString().toCharArray()?
		if (binding == null) {
			return null;
		}
		return new ModuleElementImpl(this._env, binding);
	}

	@Override
	public Set<? extends ModuleElement> getAllModuleElements() {
		LookupEnvironment lookup = this._env.getLookupEnvironment();
		HashtableOfModule knownModules = lookup.knownModules;
		ModuleBinding[] modules = knownModules.valueTable;
		if (modules == null || modules.length == 0) {
			return Collections.emptySet();
		}
		Set<ModuleElement> mods = new HashSet<>(modules.length);
		for (ModuleBinding moduleBinding : modules) {
			if (moduleBinding == null)
				continue;
			ModuleElement element = (ModuleElement) this._env.getFactory().newElement(moduleBinding);
			mods.add(element);
		}
		mods.add((ModuleElement) this._env.getFactory().newElement(lookup.UnNamedModule));
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
			p = this._env.getLookupEnvironment().createPackage(compoundName);
		}
		if (p == null || !p.isValidBinding())
			return null;
		return (PackageElement) this._env.getFactory().newElement(p);
	}
	@Override
	public boolean isAutomaticModule(ModuleElement module) {
		ModuleBinding mBinding = ((ModuleElementImpl) module).binding;
        return mBinding.isAutomatic();
    }
	@Override
	public javax.tools.JavaFileObject getFileObjectOf(Element element) {
		switch(element.getKind()) {
			case INTERFACE:
			case CLASS:
			case ENUM:
			case RECORD:
			case ANNOTATION_TYPE:
				TypeElementImpl elementImpl = (TypeElementImpl) element;
				ReferenceBinding refBinding = (ReferenceBinding) elementImpl._binding;
				if (!refBinding.isBinaryBinding()) {
					TypeElementImpl outer = (TypeElementImpl) getOutermostTypeElement(element);
					refBinding = (ReferenceBinding) outer._binding;
				}
				return getFileObjectForType(refBinding);
			case MODULE:
				ModuleElementImpl moduleEl = (ModuleElementImpl) element;
				ModuleBinding mBinding = (ModuleBinding) moduleEl._binding;
				if (mBinding instanceof SourceModuleBinding) {
					SourceModuleBinding sourceModule = (SourceModuleBinding) mBinding;
					return getSourceJavaFileObject(sourceModule.scope.referenceContext());
				} else if (mBinding instanceof BinaryModuleBinding) {
					BinaryModuleBinding binaryBinding = (BinaryModuleBinding) mBinding;
					if (binaryBinding.path != null) {
						return new PathFileObject(Path.of(binaryBinding.path), Kind.CLASS, Charset.defaultCharset());
					}
				}
				break;
			case PACKAGE:
				PackageElementImpl packEl = (PackageElementImpl) element;
				PackageBinding pBinding = (PackageBinding) packEl._binding;
				Binding typeOrPackage = pBinding.getTypeOrPackage(TypeConstants.PACKAGE_INFO_NAME, pBinding.enclosingModule, true);
				if (typeOrPackage != null) {
					return getFileObjectForType((TypeBinding) typeOrPackage);
				}
				break;
			case PARAMETER:
			case LOCAL_VARIABLE:
			case FIELD:
			case RECORD_COMPONENT:
			case ENUM_CONSTANT:
			case METHOD:
			case CONSTRUCTOR:
				if (element.getEnclosingElement() != null) {
					return getFileObjectOf(element.getEnclosingElement());
				}
				break;
			default:
				break;
		}
		return null;
	}
	private JavaFileObject getFileObjectForType(TypeBinding binding) {
		if (binding instanceof SourceTypeBinding) {
			SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) binding;
			ReferenceContext referenceContext = sourceTypeBinding.scope.referenceContext();
			return getSourceJavaFileObject(referenceContext);
		} else if(binding instanceof BinaryTypeBinding) {
			BinaryTypeBinding binaryBinding = (BinaryTypeBinding) binding;
			if (binaryBinding.path != null) {
				Path of = Path.of(binaryBinding.path);
				if (Files.exists(of)) {
					return new PathFileObject(of, Kind.CLASS, Charset.defaultCharset());
				}
			}
		}
		return null;
	}

	@SuppressWarnings("resource") // fileManager is not created, must not be closed
	private JavaFileObject getSourceJavaFileObject(ReferenceContext referenceContext) {
		JavaFileManager fileManager = this._env.getFileManager();
		if (fileManager instanceof EclipseFileManager) {
			EclipseFileManager eFileManager = (EclipseFileManager) fileManager;
			CompilationResult compilationResult = referenceContext.compilationResult();
			String fileName = new String(compilationResult.fileName);
			File f = new File(fileName);
			if (f.exists()) {
				Iterator<? extends JavaFileObject> objects = eFileManager.getJavaFileObjects(f).iterator();
				if (objects.hasNext()) {
					return objects.next();
				}
			}
		} else {
			throw new UnsupportedOperationException();
		}
		return null;
	}
	@Override
    public boolean isCanonicalConstructor(ExecutableElement e) {
		MethodBinding methodBinding = (MethodBinding) ((ExecutableElementImpl) e)._binding;
		return methodBinding.isCanonicalConstructor();
    }
	@Override
    public boolean isCompactConstructor(ExecutableElement e) {
		MethodBinding methodBinding = (MethodBinding) ((ExecutableElementImpl) e)._binding;
        return methodBinding.isCompactConstructor();
    }
}
