 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.types;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

public class Method extends Member implements Comparable {
	public static final Parameter[] NO_PARAMETERS = new Parameter[0];

	private final String returnType;

	private final Parameter[] parameters;

	public Method(int modifiers, String name, Parameter[] parameters, String returnType, ClassType declaringClass, boolean inferred) {
		super(Signature.createMethodSignature(collectParameterSignatures(parameters), returnType),
				modifiers, name, declaringClass, inferred);
		this.parameters = parameters;
		this.returnType = returnType;
		
		for (int i = 0; i < parameters.length; i++) {
			parameters[i].method = this;
		}
	}
	
	private static String[] collectParameterSignatures(Parameter[] parameters) {
		String[] results = new String[parameters.length];
		for (int i = 0; i < parameters.length; ++i) {
			results[i] = parameters[i].getSignature();
		}
		return results;
	}

	public String getReturnType() {
		return returnType;
	}
	
	public Parameter[] getParameters() {
		return parameters;
	}

	@Override
    public boolean equals(Object obj) {
	    return super.equals(obj) && returnType.equals(((Method) obj).returnType) && parametersEqual((Method) obj);
	}
	@Override
	public int hashCode() {
	    int code = super.hashCode();
	    for (int i = 0; i < parameters.length; ++i) {
	        code *= parameters.hashCode();
	    }
	    code *= returnType.hashCode();
	    return code;
	}

	 
	/**
     * @param obj
     * @return
     */
    private boolean parametersEqual(Method obj) {
        if (obj.parameters.length != parameters.length) {
            return false;
        }
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].equals(obj.parameters[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
	public boolean isSimilar(GroovyDeclaration rhs) {
		if (rhs == null || !(rhs instanceof Method)) {
			return false;
		}

		try {
		    // don't call super.  return types are not required for similarity
			Method method = (Method) rhs;
			if (!name.equals(method.name)) {
				return false;
			}

			if (parameters.length != method.parameters.length) {
				return false;
			}

			for (int i = 0; i < parameters.length; ++i) {
				if (!parameters[i].getSignature().equals(method.parameters[i].getSignature())) {
					return false;
				}
			}
			if (!declaringClass.equals(method.declaringClass)) {
			    // still might be similar if classes on the same hierarchy
			    // FIXADE M2 I really, really, really don't like this.
			    // This will only work if classes in the project are also 
			    // available in the workspace
		        try {
		            // If related, figure out which is super class of other.
		            Class cls = Class.forName(declaringClass.name);
		            Class clsOther = Class.forName(method.declaringClass.name);
		            if (cls.equals(clsOther)) {
		                // should not happen.  the declaring class should be equal already
		                return true;
		            } else if (cls.isAssignableFrom(clsOther)) {
		                return true;
		            } else if (clsOther.isAssignableFrom(cls)) {
		                return true;
		            }
		        } catch (ClassNotFoundException e) {
		            // Ignore - assume "equal" which is good enough for display purposes.
		            // For hashing
		            return true;
		        }
			    return false;
			}

		} catch (ClassCastException e) {
			return false;
		}
		return true;
	}


	@Override
    @SuppressWarnings("unchecked")
    public int compareTo(Object arg) {
	    if (! (arg instanceof Method)) {
	        return -1;
	    }
	    
		Method method = (Method) arg;
		int value = name.compareTo(method.name);
		if (value != 0) {
			return value;
		}

		value = parameters.length - method.parameters.length;

		if (value != 0) {
			return value;
		}

		// Note: at some stage with a decent type lookup database, the type resolution below will be avoided.
		try {
			// If related, figure out which is super class of other.
			Class cls = Class.forName(declaringClass.signature);
			Class clsOther = Class.forName(method.declaringClass.signature);
			if (cls.equals(clsOther)) {
				// Same parameters, same declaring class, same name - the same.
				// How can this be?
				if (parameters.length == method.parameters.length) {
					// TODO: emp - this seems like a bug, equal declaring classes?
					return 0;
				}
				// Just compare by first arg type alphabetically.
				return parameters[0].compareTo(method.parameters[0]);
			} else if (cls.isAssignableFrom(clsOther)) {
				// If cls is a super class, this it goes first.
				return 1;
			} else if (clsOther.isAssignableFrom(cls)) {
				// Else the other way around.
				return -1;
			} else {
				// Different unrelated classes. Compare by declaring type name.
				return declaringClass.compareTo(method.declaringClass);
			}
		} catch (ClassNotFoundException e) {
			// Ignore - assume "equal" which is good enough for display purposes.
			// For hashing
		}

		return 0;
	}

	@Override
    public String toString() {
		return "Method:" + name + "(" + getParameterString() + ")" + returnType + " - " + declaringClass;
	}

	private String getParameterString() {
		if (parameters.length == 0) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < parameters.length; ++i) {
			sb.append(parameters[i].getSignature()).append(' ').append(parameters[i].getName()).append(',');
		}

		return sb.substring(0, sb.length() - 1);
	}
	
    @Override
    public IJavaElement toJavaElement(GroovyProjectFacade project) {
        IJavaElement elt = getDeclaringClass().toJavaElement(project);
        if (elt != null && elt.getElementType() == IJavaElement.TYPE) {
            String[] paramTypes = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                paramTypes[i] = parameters[i].signature;
            }
            return ((IType) elt).getMethod(name, paramTypes);
        }
        return null;
    }
}