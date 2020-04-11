/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class RecordDeclaration extends TypeDeclaration {

	private Argument[] args;
	public int nRecordComponents;
	public boolean isLocalRecord;
	public static Set<String> disallowedComponentNames;
	static {
		disallowedComponentNames = new HashSet<>(6);
		disallowedComponentNames.add("clone"); //$NON-NLS-1$
		disallowedComponentNames.add("finalize"); //$NON-NLS-1$
		disallowedComponentNames.add("getClass"); //$NON-NLS-1$
		disallowedComponentNames.add("hashCode"); //$NON-NLS-1$
		disallowedComponentNames.add("notify");   //$NON-NLS-1$
		disallowedComponentNames.add("notifyAll");//$NON-NLS-1$
		disallowedComponentNames.add("toString"); //$NON-NLS-1$
		disallowedComponentNames.add("wait"); //$NON-NLS-1$
	}
	public RecordDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
		this.modifiers |= ExtraCompilerModifiers.AccRecord;
	}
	public RecordDeclaration(TypeDeclaration t) {
		super(t.compilationResult);
		this.modifiers = t.modifiers | ExtraCompilerModifiers.AccRecord;
		this.modifiersSourceStart = t.modifiersSourceStart;
		this.annotations = t.annotations;
		this.name = t.name;
		this.superInterfaces = t.superInterfaces;
		this.fields = t.fields;
		this.methods = t.methods;
		this.memberTypes = t.memberTypes;
		this.binding = t.binding;
		this.scope = t.scope;
		this.initializerScope = t.initializerScope;
		this.staticInitializerScope = t.staticInitializerScope;
		this.ignoreFurtherInvestigation = t.ignoreFurtherInvestigation;
		this.maxFieldCount = t.maxFieldCount;
		this.declarationSourceStart = t.declarationSourceStart;
		this.declarationSourceEnd = t.declarationSourceEnd;
		this.bodyStart = t.bodyStart;
		this.bodyEnd = t.bodyEnd;
		this.missingAbstractMethods = t.missingAbstractMethods; // TODO: Investigate whether this is relevant.
		this.javadoc = t.javadoc;
		this.allocation = t.allocation;
		this.enclosingType = t.enclosingType;
		this.typeParameters = t.typeParameters;
		this.sourceStart = t.sourceStart;
		this.sourceEnd = t.sourceEnd;
		this.restrictedIdentifierStart = t.restrictedIdentifierStart;
	}
	public ConstructorDeclaration getConstructor(Parser parser) {
		if (this.methods != null) {
			for (int i = this.methods.length; --i >= 0;) {
				AbstractMethodDeclaration am;
				if ((am = this.methods[i]).isConstructor()) {
					if (!CharOperation.equals(am.selector, this.name)) {
						// the constructor was in fact a method with no return type
						// unless an explicit constructor call was supplied
						ConstructorDeclaration c = (ConstructorDeclaration) am;
						if (c.constructorCall == null || c.constructorCall.isImplicitSuper()) { //changed to a method
							MethodDeclaration m = parser.convertToMethodDeclaration(c, this.compilationResult);
							this.methods[i] = m;
						}
					} else {
						if (am instanceof CompactConstructorDeclaration) {
							CompactConstructorDeclaration ccd = (CompactConstructorDeclaration) am;
							ccd.recordDeclaration = this;
							if (ccd.arguments == null)
								ccd.arguments = this.args;
							return ccd;
						}
						// now we are looking at a "normal" constructor
						if (this.args == null && am.arguments == null)
							return (ConstructorDeclaration) am;
					}
				}
			}
		}
		/* At this point we can only say that there is high possibility that there is a constructor
		 * If it is a CCD, then definitely it is there (except for empty one); else we need to check
		 * the bindings to say that there is a canonical constructor. To take care at binding resolution time.
		 */
		return null;
	}

	/** Returns an implicit canonical constructor, if any.
	 */
	public static ConstructorDeclaration getImplicitCanonicalConstructor(AbstractMethodDeclaration[] methods) {
		if (methods == null)
			return null;
		for (AbstractMethodDeclaration am : methods) {
			if (am instanceof ConstructorDeclaration && (am.bits & (ASTNode.IsCanonicalConstructor | ASTNode.IsImplicit)) != 0)
				return (ConstructorDeclaration) am;
		}
		return null;
	}
	@Override
	public ConstructorDeclaration createDefaultConstructor(boolean needExplicitConstructorCall, boolean needToInsert) {
		//Add to method'set, the default constuctor that just recall the
		//super constructor with no arguments
		//The arguments' type will be positionned by the TC so just use
		//the default int instead of just null (consistency purpose)

		ConstructorDeclaration constructor = new ConstructorDeclaration(this.compilationResult);
		constructor.bits |= ASTNode.IsCanonicalConstructor | ASTNode.IsImplicit;
		constructor.selector = this.name;
//		constructor.modifiers = this.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
		constructor.modifiers = this.modifiers & ClassFileConstants.AccPublic;
		constructor.modifiers |= ClassFileConstants.AccPublic; // JLS 14 8.10.5
		constructor.arguments = this.args;

		constructor.declarationSourceStart = constructor.sourceStart =
				constructor.bodyStart = this.sourceStart;
		constructor.declarationSourceEnd =
			constructor.sourceEnd = constructor.bodyEnd =  this.sourceStart - 1;

		//the super call inside the constructor
		if (needExplicitConstructorCall) {
			constructor.constructorCall = SuperReference.implicitSuperConstructorCall();
			constructor.constructorCall.sourceStart = this.sourceStart;
			constructor.constructorCall.sourceEnd = this.sourceEnd;
		}
	/* The body of the implicitly declared canonical constructor initializes each field corresponding
		 * to a record component with the corresponding formal parameter in the order that they appear
		 * in the record component list.*/
		List<Statement> statements = new ArrayList<>();
		int l = this.args != null ? this.args.length : 0;
		if (l > 0 && this.fields != null) {
			List<String> fNames = Arrays.stream(this.fields)
					.filter(f -> f.isARecordComponent)
					.map(f ->new String(f.name))
					.collect(Collectors.toList());
			for (int i = 0; i < l; ++i) {
				Argument arg = this.args[i];
				if (!fNames.contains(new String(arg.name)))
					continue;
				FieldReference lhs = new FieldReference(arg.name, 0);
				lhs.receiver = ThisReference.implicitThis();
				statements.add(new Assignment(lhs, new SingleNameReference(arg.name, 0), 0));
			}
		}
		constructor.statements = statements.toArray(new Statement[0]);

		//adding the constructor in the methods list: rank is not critical since bindings will be sorted
		if (needToInsert) {
			if (this.methods == null) {
				this.methods = new AbstractMethodDeclaration[] { constructor };
			} else {
				AbstractMethodDeclaration[] newMethods;
				System.arraycopy(
					this.methods,
					0,
					newMethods = new AbstractMethodDeclaration[this.methods.length + 1],
					1,
					this.methods.length);
				newMethods[0] = constructor;
				this.methods = newMethods;
			}
		}
		return constructor;
	}

	@Override
	public void generateCode(ClassFile enclosingClassFile) {
		super.generateCode(enclosingClassFile);
	}
	@Override
	public boolean isRecord() {
		return true;
	}
	@Override
	public StringBuffer printHeader(int indent, StringBuffer output) {
		printModifiers(this.modifiers, output);
		if (this.annotations != null) {
			printAnnotations(this.annotations, output);
			output.append(' ');
		}

		output.append("record "); //$NON-NLS-1$
		output.append(this.name);
		output.append('(');
		if (this.nRecordComponents > 0 && this.fields != null) {
			for (int i = 0; i < this.nRecordComponents; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				output.append(this.fields[i].type.getTypeName()[0]);
				output.append(' ');
				output.append(this.fields[i].name);
			}
		}
		output.append(')');
		if (this.typeParameters != null) {
			output.append("<");//$NON-NLS-1$
			for (int i = 0; i < this.typeParameters.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				this.typeParameters[i].print(0, output);
			}
			output.append(">");//$NON-NLS-1$
		}
		if (this.superInterfaces != null && this.superInterfaces.length > 0) {
			output.append(" implements "); //$NON-NLS-1$
			for (int i = 0; i < this.superInterfaces.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				this.superInterfaces[i].print(0, output);
			}
		}
		return output;
	}
	@Override
	public StringBuffer printBody(int indent, StringBuffer output) {
		output.append(" {"); //$NON-NLS-1$
		if (this.memberTypes != null) {
			for (int i = 0; i < this.memberTypes.length; i++) {
				if (this.memberTypes[i] != null) {
					output.append('\n');
					this.memberTypes[i].print(indent + 1, output);
				}
			}
		}
		if (this.fields != null) {
			for (int fieldI = 0; fieldI < this.fields.length; fieldI++) {
				if (this.fields[fieldI] != null) {
					output.append('\n');
					if (fieldI < this.nRecordComponents)
						output.append("/* Implicit */"); //$NON-NLS-1$ //TODO BETA_JAVA14: Move this to FD?
					this.fields[fieldI].print(indent + 1, output);
				}
			}
		}
		if (this.methods != null) {
			for (int i = 0; i < this.methods.length; i++) {
				if (this.methods[i] != null) {
					output.append('\n');
					AbstractMethodDeclaration amd = this.methods[i];
					if (amd instanceof MethodDeclaration && (amd.bits & ASTNode.IsImplicit) != 0)
						output.append("/* Implicit */\n"); //$NON-NLS-1$// TODO BETA_JAVA14: Move this to MD?
					amd.print(indent + 1, output);
				}
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
	public Argument[] getArgs() {
		return this.args;
	}
	public void setArgs(Argument[] args) {
		this.args = args;
	}
	public static void checkAndFlagRecordNameErrors(char[] typeName, ASTNode node, Scope skope) {
		if (CharOperation.equals(typeName, TypeConstants.RECORD_RESTRICTED_IDENTIFIER)) {
			if (skope.compilerOptions().sourceLevel == ClassFileConstants.JDK14) {
					skope.problemReporter().recordIsAReservedTypeName(node);
			}
		}
	}
	AbstractMethodDeclaration[] getMethod(char[] name1) {
		if (name1 == null || name1.length == 0 || this.methods == null)
			return null;
		List<AbstractMethodDeclaration> amList = new ArrayList<>(0);
		for (AbstractMethodDeclaration amd : this.methods) {
			if (CharOperation.equals(name1, amd.selector))
				amList.add(amd);
		}
		return amList.toArray(new AbstractMethodDeclaration[0]);
	}
}
