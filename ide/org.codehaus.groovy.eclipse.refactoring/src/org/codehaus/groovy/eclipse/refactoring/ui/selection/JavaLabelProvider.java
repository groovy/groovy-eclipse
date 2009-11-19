/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.selection;

import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.TypeResolver;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * @author Stefan Reinhard
 */
public class JavaLabelProvider extends LabelProvider {

//	public Image getImage(Object element) {
//		Image img = null;
//		if (element instanceof IMember) {
//			try {
//				int modifier = ((IMember)element).getFlags();
//				if (Flags.isPublic(modifier)) {
//					img = GroovyPluginImages.get(GroovyPluginImages.IMG_MISC_PUBLIC);
//				} else if (Flags.isProtected(modifier)) {
//					img = GroovyPluginImages.get(GroovyPluginImages.IMG_MISC_PROTECTED);
//				} else if (Flags.isPrivate(modifier)) {
//					img = GroovyPluginImages.get(GroovyPluginImages.IMG_MISC_PRIVATE);
//				} else {
//					img = GroovyPluginImages.get(GroovyPluginImages.IMG_MISC_DEFAULT);
//				}
//			} catch (JavaModelException e) {
//				e.printStackTrace();
//			}
//		}
//		return img;
//	}

	public String getText(Object element) {
		try {
			if (element instanceof IMethod) {
				return getMethodSignature((IMethod)element);
			} else if (element instanceof IField) {
				return getFieldSignature((IField)element);
			} else {
				return new JavaElementLabelProvider().getText(element);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return element.toString();
	}

	/**
	 * Returns the fully qualified path of the given field
	 * @param field 
	 * @throws JavaModelException
	 */
	public static String getFieldSignature(IField field) throws JavaModelException {
		IType declaring = field.getDeclaringType();
		if (declaring != null) {
			return declaring.getElementName() + "."
					+ field.getElementName() + ": "
					+ TypeResolver.getName(field.getTypeSignature(), declaring);
		} else {
			return field.getElementName();
		}
	}
	
	/**
	 * Returns a complete signature including the return type of the given method
	 * @param method
	 * @throws JavaModelException
	 */
	public static String getMethodSignature(IMethod method) throws JavaModelException {
		IType declaring = method.getDeclaringType();
		StringBuilder sig = new StringBuilder(declaring.getElementName());
		sig.append(".");
		sig.append(method.getElementName());
		sig.append("(");
		for (int i = 0; i < method.getNumberOfParameters(); i++) {
			String name = TypeResolver.getName(method.getParameterTypes()[i], declaring);
			sig.append(name);
			if (i < method.getNumberOfParameters()-1) {
				sig.append(",");
			}
		}
		sig.append("): ");
		sig.append(TypeResolver.getName(method.getReturnType(), declaring));
		String signature = sig.toString();
		return signature;
	}
}
