package org.codehaus.groovy.eclipse.codeassist.completion.jdt;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.Member;
import org.codehaus.groovy.eclipse.core.types.MemberLookupRegistry;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Modifiers;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.Property;
import org.codehaus.groovy.eclipse.core.types.Type;
import org.codehaus.groovy.eclipse.core.types.impl.CategoryLookup;
import org.codehaus.groovy.eclipse.core.types.impl.ClassLoaderMemberLookup;
import org.codehaus.groovy.eclipse.core.types.impl.CompositeLookup;
import org.codehaus.groovy.eclipse.core.types.impl.GroovyProjectMemberLookup;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ParseException;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("nls")
public abstract class AbstractGroovyCompletionProcessor implements IJavaCompletionProposalComputer {
	
	/**
	 * Remove types that are equal. There is no control over if the different implementations of IMemberLookup will
	 * equal result, so this method merges the duplicates if they exist.
	 * 
	 * @param types
	 * @return
	 */
    protected Type[] mergeTypes(Type[] types) {
		if (types.length < 1) {
			return types;
		}
		
		Arrays.sort(types);
		
		List<Type> results = new ArrayList<Type>();
		results.add(types[0]);
		for (int i = 1; i < types.length; ++i) {
			if (!isSimilar(types[i-1], types[i])) {
				results.add(types[i]);
			}
		}
		
		removeCompilerMethods(results);
		
		// HACK: emp - must be base type. This is really wanting to be an interface at some stage.
		Class<?> cls = types[0].getClass();
		if (cls.getName().startsWith("org.codehaus.groovy.eclipse.core.types.Java")) {
			cls = cls.getSuperclass();
		}
		
		return (Type[]) results.toArray((Type[])Array.newInstance(cls, results.size()));
	}

    private boolean isSimilar(Type type1, Type type2) {
        if (type1 instanceof Method && type2 instanceof Method) {
            Method method1 = (Method) type1;
            Method method2 = (Method) type2;
            
            if (! method1.getName().equals(method2.getName())) {
                return false;
            }
            
            if (method1.getParameters().length != method2.getParameters().length) {
                return false;
            }
            
            Parameter[] params1 = method1.getParameters();
            Parameter[] params2 = method2.getParameters();
            for (int i = 0; i < params1.length; i++) {
                if (!params1[i].getSignature() .equals(params2[i].getSignature())) {
                    return false;
                }
            }
            return true;
        } else {
            return type1.equals(type2);
        }
    }
	
	/**
	 * class$ and super$ and this$ methods must be removed.
	 * @param results
	 */
	protected void removeCompilerMethods(List<Type> results) {
		for (Iterator<Type> iter  = results.iterator(); iter.hasNext();) {
			String name = iter.next().getName();
			if (name.startsWith("<clinit>") || name.startsWith("class$") || name.startsWith("super$") || name.startsWith("this$")) {
				iter.remove();
			}
		}
	}
	
    protected boolean isScriptOrClosureContext(ISourceCodeContext sourceContext) {
        try {
            return  sourceContext.getId().equals(ISourceCodeContext.CLOSURE_SCOPE) ||
                    ((ClassNode) sourceContext.getASTPath()[1]).isScript();
        } catch (Exception e) {
            // any reason for failure means we are not in a script
            return false;
        }
    }


	protected Image getImageForType(Type type) {
		if (type instanceof LocalVariable) {
			return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LOCAL_VARIABLE);
		}
		if (type instanceof Field) {
			if (test(type.getModifiers(), Modifiers.ACC_PUBLIC)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
			} else if (test(type.getModifiers(), Modifiers.ACC_PROTECTED)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PROTECTED);
			} else if (test(type.getModifiers(), Modifiers.ACC_PRIVATE)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PRIVATE);
			}
			return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_DEFAULT);
		} else if (type instanceof Property) {
			// TODO: need compound icon with 'r' and 'w' on it to indicate property and access.
			if (test(type.getModifiers(), Modifiers.ACC_PUBLIC)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
			} else if (test(type.getModifiers(), Modifiers.ACC_PROTECTED)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PROTECTED);
			} else if (test(type.getModifiers(), Modifiers.ACC_PRIVATE)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PRIVATE);
			}
			return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_DEFAULT);
		} else if (type instanceof Method) {
			if (test(type.getModifiers(), Modifiers.ACC_PUBLIC)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
			} else if (test(type.getModifiers(), Modifiers.ACC_PROTECTED)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);
			} else if (test(type.getModifiers(), Modifiers.ACC_PRIVATE)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE);
			}
			return JavaPluginImages.get(JavaPluginImages.IMG_MISC_DEFAULT);
		}
		return null;
	}

	protected boolean test(int flags, int mask) {
		return (flags & mask) != 0;
	}

	protected String createReplaceString(Type method) {
		return method.getName() + "()";
	}
	
	protected StyledString createDisplayString(Member member) {
		StringBuffer sb = new StringBuffer(member.getName());
		sb.append(" ").append(toSimpleTypeName(member.getSignature())).append(" - ").append(
				toSimpleTypeName(member.getDeclaringClass().getSignature()));
		return new StyledString(sb.toString());
	}

	protected char[] createDisplayString(Method method) {
		StringBuffer sb = new StringBuffer(method.getName() + "(");
		String returnType = method.getReturnType();
		if (returnType != null) {
			returnType = toSimpleTypeName(returnType);
		} else {
			returnType = "void";
		}
		sb.append(createParameterListString(method)).append(")");
		sb.append(" ").append(returnType);
		sb.append(" - ").append(toSimpleTypeName(method.getDeclaringClass().getSignature()));

		return sb.toString().toCharArray();
	}

	protected String createParameterListString(Method method) {
		StringBuffer sb = new StringBuffer();
		Parameter[] parameter = method.getParameters();
		if (parameter.length == 0) {
			return "";
		}
		for (int i = 0; i < parameter.length; ++i) {
			sb.append(toSimpleTypeName(parameter[i].getSignature())).append(" ").append(parameter[i].getName())
					.append(", ");
		}
		return sb.substring(0, sb.length() - 2);
	}

	protected String toSimpleTypeName( final String t ) {
	    String type = t;
		if (type.charAt(0) == '[') {
			type = Signature.toString(type);
		}
		int ix = type.lastIndexOf('.');
		if (ix != -1) {
			return type.substring(ix + 1);
		}
		return type;
	}

	/**
	 * @param javaContext
	 * @param contexts 
	 * @param inScriptOrClosure 
	 * @return The lookup or null if it could not be created.
	 */
	protected IMemberLookup createMemberLookup(GroovyProjectFacade project, ISourceCodeContext[] contexts) {
		CategoryLookup categoryLookup = new CategoryLookup();
        GroovyProjectMemberLookup classNodeLookup = new GroovyProjectMemberLookup(project);
            
        IMemberLookup registeredLookups = MemberLookupRegistry.createMemberLookup(contexts);
        ClassLoaderMemberLookup classLookup = new ClassLoaderMemberLookup(project.getProjectClassLoader());
        return new CompositeLookup(new IMemberLookup[] { 
                classNodeLookup, classLookup, categoryLookup, registeredLookups });
	}
	
	protected String findCompletionExpression(ExpressionFinder finder, int offset, ISourceBuffer buffer) {
		try{
			return finder.findForCompletions(buffer, offset - 1);
		} catch (ParseException e) {
            // can ignore.  probably just invalid code that is being completed at
            GroovyCore.trace("Cannot complete code:" + e.getMessage());
		}
		return null;
	}

	protected ISourceCodeContext[] createContexts(ModuleNode moduleNode, ISourceBuffer buffer, int offset) {
		SourceCodeContextFactory factory = new SourceCodeContextFactory();
		return factory.createContexts(buffer, moduleNode, new Region(offset, 0));
	}

	protected ModuleNode getCurrentModuleNode(JavaContentAssistInvocationContext context) {
	    return ((GroovyCompilationUnit) context.getCompilationUnit()).getModuleNode();
	}

    public String getErrorMessage() {
        return null;
    }

    public void sessionEnded() {
        // do nothing
    }

    public void sessionStarted() {
        // do nothing
    }
    
    private static final List<IContextInformation> NO_CONTEXTS= Collections.emptyList();
    public List<IContextInformation> computeContextInformation(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return NO_CONTEXTS;
    }

}
