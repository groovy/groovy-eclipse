/*******************************************************************************
 * Copyright (c) 2026 Hélios GILLES and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hélios GILLES - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import static org.eclipse.jdt.internal.core.JavaModelManager.trace;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntSupplier;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchNameEnvironment;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.ResourceCompilationUnit;

/**
 * What {@link SourceIndexer} needs to resolve the documents of a project: its name environment and a lookup
 * environment.
 * <p>
 * Both are costly to create and to fill. The index manager therefore keeps the last one, to resolve the next
 * documents of the same project as a compiler would resolve the units of a batch: an indexer
 * {@link IndexManager#acquireSourceIndexerEnvironment acquires} one to resolve a document, and
 * {@link IndexManager#releaseSourceIndexerEnvironment gives it back} once the document is indexed, or if it is not
 * to be. This class tells whether an environment can be reused.
 * </p>
 * <p>
 * A document is to be resolved as it would be with a new name environment and a new lookup environment:
 * </p>
 * <ul>
 * <li>the name environment is used as long as it is {@link #isUpToDate up to date};</li>
 * <li>the lookup environment is replaced by an empty one, see {@link #forgetTypes()}, when the types it knows
 * may not be the ones that a new one would find.</li>
 * </ul>
 */
public final class SourceIndexerEnvironment implements ITypeRequestor {

	/**
	 * The number of units after which the lookup environment is replaced, to limit what it retains. With 1, each
	 * document is resolved in its own lookup environment. Set with the system property {@value #MAX_UNITS_PROPERTY}.
	 */
	public static final String MAX_UNITS_PROPERTY = "jdt.core.indexManager.maxResolvedUnits"; //$NON-NLS-1$
	public static volatile int MAX_UNITS = Math.max(1, Integer.getInteger(MAX_UNITS_PROPERTY, 2000));

	/**
	 * Whether the index manager keeps an environment for the next document. If not, each document is resolved with
	 * a new name environment and a new lookup environment, as before environments were kept. Set with the system
	 * property {@value #KEEP_PROPERTY}.
	 */
	public static final String KEEP_PROPERTY = "jdt.core.indexManager.keepSourceEnvironment"; //$NON-NLS-1$
	public static volatile boolean KEEP = !"false".equalsIgnoreCase(System.getProperty(KEEP_PROPERTY)); //$NON-NLS-1$

	/**
	 * What a new environment would resolve a document of a project with, see {@link #snapshot}. Two snapshots are
	 * equal if their arrays have the same contents.
	 *
	 * @param changeCount the number of changes counted by the index manager, read after the rest
	 */
	record Snapshot(JavaProject project, IClasspathEntry[] resolvedClasspath, Map<String, String> projectOptions,
			org.eclipse.jdt.core.ICompilationUnit[] workingCopies, int changeCount) {
		@Override
		public boolean equals(Object object) {
			return object instanceof Snapshot other
					&& this.changeCount == other.changeCount
					&& this.project.equals(other.project)
					&& Arrays.equals(this.resolvedClasspath, other.resolvedClasspath)
					&& this.projectOptions.equals(other.projectOptions)
					&& Arrays.equals(this.workingCopies, other.workingCopies);
		}

		@Override
		public int hashCode() {
			return 31 * this.project.hashCode() + this.changeCount;
		}

		@Override
		public String toString() {
			return "Snapshot of " + this.project.getElementName() + " at change " + this.changeCount; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private final Snapshot snapshot;

	private final CompilerOptions options;
	private final ProblemReporter problemReporter;
	/** The parser of the unit being resolved: a parser keeps a state after a syntax error */
	private Parser parser;
	private JavaSearchNameEnvironment nameEnvironment;
	private LookupEnvironment lookupEnvironment;
	/** The units that were parsed, without their method bodies, to resolve other units, by file name. */
	private final Map<String, CompilationUnitDeclaration> acceptedUnits = new HashMap<>();
	/** The file names of the units that were resolved */
	private final Set<String> resolvedUnits = new HashSet<>();
	/** Whether the types that the lookup environment knows may differ from the ones a new one would find */
	private boolean typesMayDiffer;
	/**
	 * Whether resolving a unit failed in an unexpected way, other than with a type that is not on the classpath, in
	 * case the failure is the one of the name environment: the unit is resolved again with a new name environment if
	 * the lookup environment knew types, else this environment is not reused.
	 */
	private boolean failed;

	SourceIndexerEnvironment(Snapshot snapshot) {
		this.snapshot = snapshot;
		this.options = new CompilerOptions(snapshot.projectOptions());
		this.problemReporter = new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				this.options,
				new DefaultProblemFactory());
		this.nameEnvironment = newNameEnvironment();
		forgetTypes();
	}

	private JavaSearchNameEnvironment newNameEnvironment() {
		// Use a non model name environment to avoid locks, monitors and such.
		return new JavaSearchNameEnvironment(this.snapshot.project(), this.snapshot.workingCopies());
	}

	/**
	 * Replaces the lookup environment by an empty one, as a new environment has.
	 */
	private void forgetTypes() {
		this.lookupEnvironment = new LookupEnvironment(this, this.options, this.problemReporter, this.nameEnvironment);
		this.acceptedUnits.clear();
		this.resolvedUnits.clear();
		this.typesMayDiffer = false;
	}

	/**
	 * Reads what a new environment would resolve a document of the given project with.
	 *
	 * @param changeCount the number of changes counted by the index manager
	 */
	static Snapshot snapshot(JavaProject project, IntSupplier changeCount) throws JavaModelException {
		// Resolving the classpath may count as a change: it is resolved before the count is read
		IClasspathEntry[] resolvedClasspath = project.getResolvedClasspath();
		Map<String, String> projectOptions = project.getOptions(true);
		org.eclipse.jdt.core.ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager()
				.getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true/* add primary WCs */);
		// The count is read before a name environment is created: if what the name environment reads changes
		// meanwhile, the environment is not up to date for the next document
		return new Snapshot(project, resolvedClasspath, projectOptions, workingCopies, changeCount.getAsInt());
	}

	/**
	 * A new environment answers the state of the project when it resolves a document. This one only does if
	 * nothing changed since it was created. The index manager counts the changes to the resources, to the buffers
	 * and to the structures of the working copies, and to the classpath of any project, as the name environment
	 * also reads the ones of the projects that this one requires. The classpath of the project, its options and
	 * the working copies are compared with the ones this environment was created with.
	 */
	boolean isUpToDate(Snapshot current) {
		return this.snapshot.equals(current);
	}

	/**
	 * Prepares this environment to resolve the next document, as it is given back to the index manager.
	 *
	 * @return <code>false</code> if it is not to be reused, as resolving a unit failed in an unexpected way
	 */
	boolean prepareForReuse() {
		if (this.failed) {
			return false;
		}
		if (this.typesMayDiffer) {
			forgetTypes();
		}
		return true;
	}

	/**
	 * Parses and resolves the given unit, of which only the methods with functional expressions keep their
	 * statements. The unit is partly resolved only if its resolution fails.
	 */
	CompilationUnitDeclaration resolve(ICompilationUnit unit) {
		if (this.acceptedUnits.size() + this.resolvedUnits.size() >= MAX_UNITS) {
			forgetTypes();
		}
		boolean done = false;
		try {
			CompilationUnitDeclaration resolved = resolveWithKnownTypes(unit);
			if (resolved == null) {
				if (this.failed) {
					// as a new environment would: with a new name environment, in case the failure was its own
					this.nameEnvironment = newNameEnvironment();
					this.failed = false;
				}
				forgetTypes();
				resolved = resolveWithKnownTypes(unit);
			}
			if (this.lookupEnvironment.hasMissingTypes()) {
				// Where a type is missing, the resolution of a unit fails, or not, whether the type is already
				// known to be missing: do not keep the lookup environment, which knows it now.
				this.typesMayDiffer = true;
			}
			done = true;
			return resolved;
		} finally {
			if (!done) {
				// anything that resolveWithKnownTypes does not catch, e.g. while parsing, or an Error
				this.failed = true;
			}
		}
	}

	/**
	 * @return the unit, or <code>null</code> if it cannot be resolved with the types that the lookup environment
	 *         knows as it would be with an empty one, which always resolves it
	 */
	private CompilationUnitDeclaration resolveWithKnownTypes(ICompilationUnit unit) {
		boolean isNew = this.acceptedUnits.isEmpty() && this.resolvedUnits.isEmpty();
		String fileName = new String(unit.getFileName());
		if (!this.resolvedUnits.add(fileName)) {
			return null;
		}
		// A normal parser: the indexing parser swallows several nodes
		this.parser = new Parser(this.problemReporter, false);
		this.parser.reportOnlyOneSyntaxError = true;
		this.parser.scanner.taskTags = null;
		CompilationUnitDeclaration parsedUnit = this.acceptedUnits.remove(fileName);
		boolean isAccepted = parsedUnit != null;
		if (isAccepted) {
			// The unit is known already, as another one refers to it: complete it, as a compiler does.
			if (!(parsedUnit.compilationResult.compilationUnit instanceof ResourceCompilationUnit)) {
				// a working copy, the contents of which may not be the indexed ones
				return null;
			}
			if (!hasAllTypeBindings(parsedUnit)) {
				// a type of the same name was known already, from another unit
				return null;
			}
		} else {
			// Resolve the document in the module of its project, like the units it refers to:
			// as a unit of the unnamed module, it would not see the types of the module.
			ICompilationUnit unitInModule = new BasicCompilationUnit(unit.getContents(), null, fileName, this.snapshot.project());
			parsedUnit = this.parser.parse(unitInModule, new CompilationResult(unitInModule, 0, 0, this.options.maxProblemsPerUnit));
			if (!isAnswerOfNameEnvironment(parsedUnit, true)) {
				// do not keep the lookup environment, which is about to know the types of this unit
				this.typesMayDiffer = true;
			}
		}
		try {
			if (isAccepted) {
				this.parser.getMethodBodies(parsedUnit);
				reduceParseTree(parsedUnit);
			} else {
				reduceParseTree(parsedUnit);
				this.lookupEnvironment.buildTypeBindings(parsedUnit, null);
				if (!isNew && !hasAllTypeBindings(parsedUnit)) {
					// a type of the same name is known already, from another unit
					return null;
				}
				this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
			}
			if (parsedUnit.scope != null) {
				parsedUnit.scope.faultInTypes();
				parsedUnit.resolve();
			}
		} catch (RuntimeException e) {
			if (!(e instanceof AbortCompilation)) {
				// Not the expected failure of a type that is not on the classpath: the name environment may have
				// failed, e.g. with a closed archive. A known type is retried with a new one; else what is
				// resolved of the unit is indexed all the same, but the environment is not reused.
				this.failed = true;
			}
			this.typesMayDiffer = true;
			if (!isNew) {
				// it may fail later with an empty lookup environment, which does not know the missing types yet
				return null;
			}
			// what is resolved of the unit is indexed
			if (JobManager.VERBOSE) {
				trace("", e); //$NON-NLS-1$
			}
		}
		return parsedUnit;
	}

	/**
	 * Returns whether the name environment answers the given unit for each of its types. It may answer another one
	 * if several units of the classpath declare a type of the same name, and none for a secondary type or for a
	 * type that is not named after its unit. Other units are then to refer to what the name environment answers,
	 * not to the types of this unit, which this environment knows once it has built them.
	 *
	 * @param isDocument whether the unit is a document, which is read from its resource: the name environment then
	 *                   has to answer the resource too, and not a working copy
	 */
	private boolean isAnswerOfNameEnvironment(CompilationUnitDeclaration unit, boolean isDocument) {
		if (unit.types != null) {
			ICompilationUnit compilationUnit = unit.compilationResult.compilationUnit;
			char[][] packageName = unit.currentPackage == null ? CharOperation.NO_CHAR_CHAR : unit.currentPackage.tokens;
			char[] module = compilationUnit.getModuleName() == null ? ModuleBinding.ANY : compilationUnit.getModuleName();
			for (TypeDeclaration type : unit.types) {
				NameEnvironmentAnswer answer = this.nameEnvironment.findType(type.name, packageName, module);
				ICompilationUnit answered = answer == null ? null : answer.getCompilationUnit();
				if (answered == null || !CharOperation.equals(answered.getFileName(), unit.getFileName())) {
					return false;
				}
				if (isDocument && !(answered instanceof ResourceCompilationUnit)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean hasAllTypeBindings(CompilationUnitDeclaration unit) {
		if (unit.types != null) {
			for (TypeDeclaration type : unit.types) {
				if (type.binding == null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Called prior to the unit being resolved. Reduce the parse tree where possible.
	 */
	private static void reduceParseTree(CompilationUnitDeclaration unit) {
		// remove statements from methods that have no functional interface types.
		TypeDeclaration[] types = unit.types;
		for (int i = 0, l = types == null ? 0 : types.length; i < l; i++)
			purgeMethodStatements(types[i]);
	}

	private static void purgeMethodStatements(TypeDeclaration type) {
		AbstractMethodDeclaration[] methods = type.methods;
		for (int j = 0, length = methods == null ? 0 : methods.length; j < length; j++) {
			AbstractMethodDeclaration method = methods[j];
			/*
			 * In case the method defines a local type, skip purging the method body.
			 * We don't know if the local type defines a method that uses a method reference or a lambda.
			 * See:
			 *   https://github.com/eclipse-jdt/eclipse.jdt.core/issues/432
			 *   https://bugs.eclipse.org/bugs/show_bug.cgi?id=566435
			 */
			if (method != null && (method.bits & (ASTNode.HasFunctionalInterfaceTypes | ASTNode.HasLocalType)) == 0) {
				method.statements = null;
				method.javadoc = null;
			}
		}

		TypeDeclaration[] memberTypes = type.memberTypes;
		if (memberTypes != null)
			for (TypeDeclaration memberType : memberTypes)
				purgeMethodStatements(memberType);
	}

	@Override
	public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
	}

	@Override
	public void accept(ICompilationUnit unit, AccessRestriction accessRestriction) {
		CompilationResult unitResult = new CompilationResult(unit, 1, 1, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = this.parser.dietParse(unit, unitResult);
		if (!isAnswerOfNameEnvironment(parsedUnit, false)) {
			// The name environment does not answer this unit for one of the types it declares, e.g. a secondary
			// type. Do not keep the lookup environment, which is about to know them all.
			this.typesMayDiffer = true;
		}
		this.lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
		this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
		this.acceptedUnits.put(new String(unit.getFileName()), parsedUnit);
	}

	@Override
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		ISourceType sourceType = sourceTypes[0];
		while (sourceType.getEnclosingType() != null)
			sourceType = sourceType.getEnclosingType();
		SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) sourceType;
		IType type = elementInfo.getHandle();
		ICompilationUnit sourceUnit = (ICompilationUnit) type.getCompilationUnit();
		accept(sourceUnit, accessRestriction);
	}
}
