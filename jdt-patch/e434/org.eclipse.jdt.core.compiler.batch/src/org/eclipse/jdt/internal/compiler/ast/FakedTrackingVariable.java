/*******************************************************************************
 * Copyright (c) 2011, 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     Nikolay Metchev (nikolaymetchev@gmail.com) - Contributions for
 *								bug 411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.flow.FinallyFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * A faked local variable declaration used for keeping track of data flows of a
 * special variable. Certain events will be recorded by changing the null info
 * for this variable.
 *
 * See bug 349326 - [1.7] new warning for missing try-with-resources
 */
public class FakedTrackingVariable extends LocalDeclaration {

	private static final char[] UNASSIGNED_CLOSEABLE_NAME = "<unassigned Closeable value>".toCharArray(); //$NON-NLS-1$
	private static final char[] UNASSIGNED_CLOSEABLE_NAME_TEMPLATE = "<unassigned Closeable value from line {0}>".toCharArray(); //$NON-NLS-1$
	private static final char[] TEMPLATE_ARGUMENT = "{0}".toCharArray(); //$NON-NLS-1$

	// a call to close() was seen at least on one path:
	private static final int CLOSE_SEEN = 1;
	// the resource is shared with outside code either by
	// - passing as an arg in a method call or
	// - obtaining this from a method call or array reference
	// Interpret that we may or may not be responsible for closing
	private static final int SHARED_WITH_OUTSIDE = 2;
	// the resource is likely owned by outside code (owner has responsibility to close):
	// - obtained as argument of the current method, or via a field read
	// - stored into a field
	// - returned as the result of this method
	private static final int OWNED_BY_OUTSIDE = 4;
	// If close() is invoked from a nested method (inside a local type) report remaining problems only as potential:
	private static final int CLOSED_IN_NESTED_METHOD = 8;
	// explicit closing has been reported already against this resource:
	private static final int REPORTED_EXPLICIT_CLOSE = 16;
	// a location independent potential problem has been reported against this resource:
	private static final int REPORTED_POTENTIAL_LEAK = 32;
	// a location independent definitive problem has been reported against this resource:
	private static final int REPORTED_DEFINITIVE_LEAK = 64;
	// a local declarations that acts as the element variable of a foreach loop (should never suggest to use t-w-r):
	private static final int FOREACH_ELEMENT_VAR = 128;
	// - passed as an effectively final resource in t-w-r JDK 9 and above
	private static final int TWR_EFFECTIVELY_FINAL = 256;
	public MessageSend acquisition;

	public static boolean TEST_372319 = false; // see https://bugs.eclipse.org/372319

	/**
	 * Bitset of {@link #CLOSE_SEEN}, {@link #SHARED_WITH_OUTSIDE}, {@link #OWNED_BY_OUTSIDE}, {@link #CLOSED_IN_NESTED_METHOD}, {@link #REPORTED_EXPLICIT_CLOSE}, {@link #REPORTED_POTENTIAL_LEAK} and {@link #REPORTED_DEFINITIVE_LEAK}.
	 */
	private int globalClosingState = 0;

	private boolean useAnnotations = false;

	public static final int NOT_OWNED = -2;
	public static final int NOT_OWNED_PER_DEFAULT = -1;
	public static final int OWNED_PER_DEFAULT = 1;
	public static final int OWNED = 2;

	static int owningStateFromTagBits(long owningTagBits, int defaultState) {
		if (owningTagBits == TagBits.AnnotationOwning)
			return OWNED;
		if (owningTagBits == TagBits.AnnotationNotOwning)
			return NOT_OWNED;
		return defaultState;
	}

	/**
	 * One of {@link #NOT_OWNED}, {@link #NOT_OWNED_PER_DEFAULT}, {@link #OWNED_PER_DEFAULT}, {@link #OWNED}.
	 * A value of {@code 0} signals unknown state.
	 */
	public int owningState = 0;

	public LocalVariableBinding originalBinding; // the real local being tracked, can be null for preliminary track vars for allocation expressions
	public FieldBinding originalFieldBinding; // when tracking an @Owning field of resource type

	public FakedTrackingVariable innerTracker; // chained tracking variable of a chained (wrapped) resource
	public FakedTrackingVariable outerTracker; // inverse of 'innerTracker'

	MethodScope methodScope; // designates the method declaring this variable

	private HashMap<ASTNode,Integer> recordedLocations; // initially null, ASTNode -> Integer

	// temporary storage while analyzing "res = new Res();":
	private ASTNode currentAssignment; // temporarily store the assignment as the location for error reporting

	// if tracking var was allocated from a finally context, record here the flow context of the corresponding try block
	private FlowContext tryContext;
	// designate the exact scope where this FTV was created
	private BlockScope blockScope;
	// within try blocks remember in which contexts the local was re-assigned
	private Set<FlowContext> modificationContexts;

	public FakedTrackingVariable(LocalVariableBinding original, ASTNode location, FlowInfo flowInfo, FlowContext flowContext, int nullStatus, boolean useAnnotations) {
		this(original.name, location, original.declaringScope, flowInfo, flowContext, nullStatus, useAnnotations);
		this.methodScope = original.declaringScope.methodScope();
		this.originalBinding = original;
	}
	public FakedTrackingVariable(LocalVariableBinding original, BlockScope scope, ASTNode location, FlowInfo flowInfo, FlowContext flowContext, int nullStatus, boolean useAnnotations) {
		this(original.name, location, original.declaringScope, flowInfo, flowContext, nullStatus, useAnnotations);
		this.methodScope = original.declaringScope.methodScope();
		this.originalBinding = original;
		this.blockScope = scope;
	}
	public FakedTrackingVariable(FieldBinding original, BlockScope scope, ASTNode location, FlowInfo flowInfo, FlowContext flowContext, int nullStatus, boolean useAnnotations) {
		this(original.name, location, scope, flowInfo, flowContext, nullStatus, useAnnotations);
		this.originalFieldBinding = original;
		this.blockScope = scope;
	}
	private FakedTrackingVariable(char[] name, ASTNode location, BlockScope scope, FlowInfo flowInfo, FlowContext flowContext,
			int nullStatus, boolean useAnnotations) {
		super(name, location.sourceStart, location.sourceEnd);
		this.type = new SingleTypeReference(
				TypeConstants.OBJECT,
				((long)this.sourceStart <<32)+this.sourceEnd);
		this.useAnnotations = useAnnotations;
		// inside a finally block?
		while (flowContext != null) {
			if (flowContext instanceof FinallyFlowContext) {
				// yes -> connect to the corresponding try block:
				this.tryContext = ((FinallyFlowContext) flowContext).tryContext;
				break;
			}
			flowContext = flowContext.parent;
		}
		resolve(scope);
		if (nullStatus != 0)
			flowInfo.markNullStatus(this.binding, nullStatus); // mark that this flow has seen the resource
	}

	/* Create an unassigned tracking variable while analyzing an allocation expression: */
	private FakedTrackingVariable(BlockScope scope, ASTNode location, FlowInfo flowInfo, int nullStatus) {
		super(UNASSIGNED_CLOSEABLE_NAME, location.sourceStart, location.sourceEnd);
		this.type = new SingleTypeReference(
				TypeConstants.OBJECT,
				((long)this.sourceStart <<32)+this.sourceEnd);
		this.methodScope = scope.methodScope();
		this.originalBinding = null;
		this.useAnnotations = scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled;
		resolve(scope);
		if (nullStatus != 0)
			flowInfo.markNullStatus(this.binding, nullStatus); // mark that this flow has seen the resource
	}

	private void attachTo(LocalVariableBinding local) {
		local.closeTracker = this;
		this.originalBinding = local;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream)
	{ /* NOP - this variable is completely dummy, ie. for analysis only. */ }

	@Override
	public void resolve (BlockScope scope) {
		// only need the binding, which is used as reference in FlowInfo methods.
		this.binding = new LocalVariableBinding(
				this.name,
				scope.getJavaLangObject(),  // dummy, just needs to be a reference type
				0,
				false);
		this.binding.closeTracker = this;
		this.binding.declaringScope = scope;
		this.binding.setConstant(Constant.NotAConstant);
		this.binding.useFlag = LocalVariableBinding.USED;
		// use a free slot without assigning it:
		this.binding.id = scope.registerTrackingVariable(this);
	}

	/** retrieve a closetracker for local that signifies a certain risk of resource leak in flowInfo. */
	private static FakedTrackingVariable getRiskyCloseTrackerAt(LocalVariableBinding local, Scope scope, FlowInfo flowInfo) {
		if (local.closeTracker == null) {
			return null;
		}
		if (flowInfo.nullStatus(local.closeTracker.binding) != FlowInfo.UNKNOWN)
			return local.closeTracker;
		while (scope instanceof BlockScope) {
			FakedTrackingVariable tracker = ((BlockScope) scope).getCloseTrackerFor(local);
			if (tracker != null) {
				if (tracker.riskyNullStatusAt(flowInfo) != 0)
					return tracker;
				if (tracker.hasDefinitelyNoResource(flowInfo))
					return null;
			}
			scope = scope.parent;
		}
		return null;
	}

	/**
	 * If expression resolves to a value of type AutoCloseable answer the variable that tracks closing of that local.
	 * Covers two cases:
	 * <ul>
	 * <li>value is a local variable reference, create tracking variable it if needed.
	 * <li>value is an allocation expression, return a preliminary tracking variable if set.
	 * </ul>
	 * @return a new {@link FakedTrackingVariable} or null.
	 */
	public static FakedTrackingVariable getCloseTrackingVariable(Expression expression, FlowInfo flowInfo, FlowContext flowContext, boolean useAnnotations) {
		if (flowInfo.reachMode() != FlowInfo.REACHABLE)
			return null;
		while (true) {
			if (expression instanceof CastExpression)
				expression = ((CastExpression) expression).expression;
			else if (expression instanceof Assignment)
				expression = ((Assignment) expression).expression;
			else if (expression instanceof ConditionalExpression) {
				return getMoreUnsafeFromBranches((ConditionalExpression)expression, flowInfo,
										branch -> getCloseTrackingVariable(branch, flowInfo, flowContext, useAnnotations));
			} else if (expression instanceof SwitchExpression se) {
				for (Expression re : se.resultExpressions()) {
					FakedTrackingVariable fakedTrackingVariable = getCloseTrackingVariable(re, flowInfo, flowContext, useAnnotations);
					if (fakedTrackingVariable != null) {
						return fakedTrackingVariable;
					}
				}
				return null;
			}
			else
				break;
		}
		FieldBinding fieldBinding = null;
		if (expression instanceof SingleNameReference) {
			SingleNameReference name = (SingleNameReference) expression;
			if (name.binding instanceof LocalVariableBinding) {
				LocalVariableBinding local = (LocalVariableBinding)name.binding;
				if (local.closeTracker != null)
					return local.closeTracker;
				if (!isCloseableNotWhiteListed(expression.resolvedType))
					return null;
				if ((local.tagBits & TagBits.IsResource) != 0)
					return null;
				// tracking var doesn't yet exist. This happens in finally block
				// which is analyzed before the corresponding try block
				Statement location = local.declaration;
				local.closeTracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, FlowInfo.UNKNOWN, useAnnotations);
				if (local.isParameter()) {
					local.closeTracker.globalClosingState |= OWNED_BY_OUTSIDE;
					// status of this tracker is now UNKNOWN
				}
				return local.closeTracker;
			} else if (name.binding instanceof FieldBinding) {
				fieldBinding = (FieldBinding) name.binding;
			}
		} else if (expression instanceof FieldReference) {
			FieldReference fieldReference = (FieldReference) expression;
			if (fieldReference.receiver.isThis())
				fieldBinding = fieldReference.binding;
		} else if (expression instanceof AllocationExpression) {
			// return any preliminary tracking variable from analyseCloseableAllocation
			return ((AllocationExpression) expression).closeTracker;
		} else if (expression instanceof MessageSend) {
			// return any preliminary tracking variable from analyseCloseableAcquisition
			return ((MessageSend) expression).closeTracker;
		}
		if (fieldBinding != null)
			return fieldBinding.closeTracker;
		return null;
	}

	/**
	 * Before analyzing an assignment of this shape: <code>singleName = new Allocation()</code>
	 * connect any tracking variable of the LHS with the allocation on the RHS.
	 * Also the assignment is temporarily stored in the tracking variable in case we need to
	 * report errors because the assignment leaves the old LHS value unclosed.
	 * In this case the assignment should be used as the error location.
	 *
	 * @param location the assignment/local declaration being analyzed
	 * @param local the local variable being assigned to
	 * @param rhs the rhs of the assignment resp. the initialization of the local variable declaration.
	 * 		<strong>Precondition:</strong> client has already checked that the resolved type of this expression is either a closeable type or NULL.
	 */
	public static FakedTrackingVariable preConnectTrackerAcrossAssignment(ASTNode location, LocalVariableBinding local, Expression rhs, FlowInfo flowInfo, boolean useAnnotations) {
		FakedTrackingVariable closeTracker = null;
		if (containsAllocation(rhs)) {
			closeTracker = local.closeTracker;
			if (closeTracker == null) {
				if (rhs.resolvedType != TypeBinding.NULL) { // not NULL means valid closeable as per method precondition
					closeTracker = new FakedTrackingVariable(local, location, flowInfo, null, FlowInfo.UNKNOWN, useAnnotations);
					if (local.isParameter()) {
						closeTracker.globalClosingState |= OWNED_BY_OUTSIDE;
					}
				}
			}
			if (closeTracker != null) {
				closeTracker.currentAssignment = location;
				preConnectTrackerAcrossAssignment(location, local, flowInfo, closeTracker, rhs, useAnnotations);
			}
		} else if (rhs instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) rhs;
			closeTracker = local.closeTracker;
			if (closeTracker != null) {
				closeTracker = handleReassignment(flowInfo, closeTracker, location);
			}
			if (rhs.resolvedType != TypeBinding.NULL // not NULL means valid closeable as per method precondition
					&& (!rhs.resolvedType.hasTypeBit(TypeIds.BitResourceFreeCloseable)
						|| isBlacklistedMethod(rhs))) {
				if (closeTracker == null)
					closeTracker = new FakedTrackingVariable(local, location, flowInfo, null, FlowInfo.UNKNOWN, useAnnotations);
				messageSend.closeTracker = closeTracker;
				closeTracker.currentAssignment = location;
			}
			if (closeTracker != null) {
				if (messageSend.binding != null && ((messageSend.binding.tagBits & TagBits.AnnotationNotOwning) == 0))
					closeTracker.owningState = OWNED;
			}
		} else if (rhs instanceof CastExpression cast) {
			preConnectTrackerAcrossAssignment(location, local, cast.expression, flowInfo, useAnnotations);
		}
		return closeTracker;
	}

	private static boolean containsAllocation(SwitchExpression location) {
		for (Expression re : location.resultExpressions()) {
			if (containsAllocation(re))
				return true;
		}
		return false;
	}
	private static boolean containsAllocation(ASTNode location) {
		if (location instanceof AllocationExpression) {
			return true;
		} else if (location instanceof ConditionalExpression) {
			ConditionalExpression conditional = (ConditionalExpression) location;
			return containsAllocation(conditional.valueIfTrue) || containsAllocation(conditional.valueIfFalse);
		} else if (location instanceof SwitchExpression) {
			return containsAllocation((SwitchExpression) location);
		} else if (location instanceof CastExpression) {
			return containsAllocation(((CastExpression) location).expression);
		} else if (location instanceof MessageSend) {
			if (isFluentMethod(((MessageSend) location).binding))
				return containsAllocation(((MessageSend) location).receiver);
		}
		return false;
	}

	private static void preConnectTrackerAcrossAssignment(ASTNode location, LocalVariableBinding local, FlowInfo flowInfo,
			FakedTrackingVariable closeTracker, Expression expression, boolean useAnnotations) {
		if (expression instanceof AllocationExpression) {
			preConnectTrackerAcrossAssignment(location, local, flowInfo, (AllocationExpression) expression, closeTracker, useAnnotations);
		} else if (expression instanceof ConditionalExpression) {
			preConnectTrackerAcrossAssignment(location, local, flowInfo, (ConditionalExpression) expression, closeTracker, useAnnotations);
		}  else if (expression instanceof SwitchExpression) {
			preConnectTrackerAcrossAssignment(location, local, flowInfo, (SwitchExpression) expression, closeTracker, useAnnotations);
		} else if (expression instanceof CastExpression) {
			preConnectTrackerAcrossAssignment(location, local, ((CastExpression) expression).expression, flowInfo, useAnnotations);
		} else if (expression instanceof MessageSend) {
			if (isFluentMethod(((MessageSend) expression).binding))
				preConnectTrackerAcrossAssignment(location, local, ((MessageSend) expression).receiver, flowInfo, useAnnotations);
		}
	}

	private static void preConnectTrackerAcrossAssignment(ASTNode location, LocalVariableBinding local, FlowInfo flowInfo,
			ConditionalExpression conditional, FakedTrackingVariable closeTracker, boolean useAnnotations) {
		preConnectTrackerAcrossAssignment(location, local, flowInfo, closeTracker, conditional.valueIfFalse, useAnnotations);
		preConnectTrackerAcrossAssignment(location, local, flowInfo, closeTracker, conditional.valueIfTrue, useAnnotations);
	}

	private static void preConnectTrackerAcrossAssignment(ASTNode location, LocalVariableBinding local, FlowInfo flowInfo,
			SwitchExpression se, FakedTrackingVariable closeTracker, boolean useAnnotations) {
		for (Expression re : se.resultExpressions()) {
			preConnectTrackerAcrossAssignment(location, local, flowInfo, closeTracker, re, useAnnotations);
		}
	}

	private static void preConnectTrackerAcrossAssignment(ASTNode location, LocalVariableBinding local, FlowInfo flowInfo,
			AllocationExpression allocationExpression, FakedTrackingVariable closeTracker, boolean useAnnotations) {
		allocationExpression.closeTracker = closeTracker;
		if (allocationExpression.arguments != null && allocationExpression.arguments.length > 0) {
			// also push into nested allocations, see https://bugs.eclipse.org/368709
			Expression firstArg = allocationExpression.arguments[0];
			if (isCloseableNotWhiteListed(firstArg.resolvedType)) {
				FakedTrackingVariable inner = preConnectTrackerAcrossAssignment(location, local, firstArg, flowInfo, useAnnotations);
				if (inner != closeTracker && closeTracker.innerTracker == null)
					closeTracker.innerTracker = inner;
			}
		}
	}

	/**
	 * Compute/assign a tracking variable for a freshly allocated closeable value, using information from our white lists.
	 * See  Bug 358903 - Filter practically unimportant resource leak warnings
	 */
	public static void analyseCloseableAllocation(BlockScope scope, FlowInfo flowInfo, FlowContext flowContext, AllocationExpression allocation) {
		if (flowInfo.reachMode() != FlowInfo.REACHABLE)
			return;
		// client has checked that the resolvedType is an AutoCloseable, hence the following cast is safe:
		if (allocation.resolvedType.hasTypeBit(TypeIds.BitResourceFreeCloseable)) {
			// remove unnecessary attempts (closeable is not relevant)
			if (allocation.closeTracker != null) {
				allocation.closeTracker.withdraw();
				allocation.closeTracker = null;
			}
		} else if (allocation.resolvedType.hasTypeBit(TypeIds.BitWrapperCloseable)) {
			boolean isWrapper = true;
			if (allocation.arguments != null &&  allocation.arguments.length > 0) {
				// find the wrapped resource represented by its tracking var:
				FakedTrackingVariable innerTracker = findCloseTracker(scope, flowInfo, allocation.arguments[0]);
				if (innerTracker != null) {
					FakedTrackingVariable currentInner = innerTracker;
					do {
						if (currentInner == allocation.closeTracker)
							return; // self wrap (res = new Res(res)) -> neither change (here) nor remove (below)
						// also check for indirect cycles, see https://bugs.eclipse.org/368709
						currentInner = currentInner.innerTracker;
					} while (currentInner != null);
					int newStatus = FlowInfo.NULL;
					if (allocation.closeTracker == null) {
						allocation.closeTracker = new FakedTrackingVariable(scope, allocation, flowInfo, FlowInfo.UNKNOWN); // no local available, closeable is unassigned
					} else {
						if (scope.finallyInfo != null) {
							// inject results from analysing a finally block onto the newly connected wrapper
							int finallyStatus = scope.finallyInfo.nullStatus(allocation.closeTracker.binding);
							if (finallyStatus != FlowInfo.UNKNOWN)
								newStatus = finallyStatus;
						}
					}
					if (allocation.closeTracker.innerTracker != null && allocation.closeTracker.innerTracker != innerTracker) {
						innerTracker = pickMoreUnsafe(allocation.closeTracker.innerTracker, innerTracker, flowInfo);
					}
					allocation.closeTracker.innerTracker = innerTracker;
					innerTracker.outerTracker = allocation.closeTracker;
					flowInfo.markNullStatus(allocation.closeTracker.binding, newStatus);
					if (newStatus != FlowInfo.NULL) {
						// propagate results from a finally block also into nested resources:
						FakedTrackingVariable currentTracker = innerTracker;
						while (currentTracker != null) {
							flowInfo.markNullStatus(currentTracker.binding, newStatus);
							currentTracker.globalClosingState |= allocation.closeTracker.globalClosingState;
							currentTracker = currentTracker.innerTracker;
						}
					}
					return; // keep chaining wrapper (by avoiding to fall through to removeTrackingVar below)
				} else {
					if (!isAnyCloseable(allocation.arguments[0].resolvedType)) {
						isWrapper = false; // argument is not closeable
					}
				}
			} else {
				isWrapper = false; // no argument
			}
			// successful wrapper detection has exited above, let's see why that failed
			if (isWrapper) {
				// remove unnecessary attempts (wrapper has no relevant inner)
				if (allocation.closeTracker != null) {
					allocation.closeTracker.withdraw();
					allocation.closeTracker = null;
				}
			} else {
				// allocation does not provide a resource as the first argument -> don't treat as a wrapper
				handleRegularResource(scope, flowInfo, flowContext, allocation);
			}
		} else { // regular resource
			handleRegularResource(scope, flowInfo, flowContext, allocation);
		}
	}

	/**
	 * Check if a message send acquires a closeable from its receiver, see:
	 * Bug 463320 - [compiler][resource] potential "resource leak" problem disappears when local variable inlined
	 */
	public static FlowInfo analyseCloseableAcquisition(BlockScope scope, FlowInfo flowInfo, FlowContext flowContext, MessageSend acquisition) {
		if (isFluentMethod(acquisition.binding)) {
			// share the existing close tracker of the receiver (if any):
			acquisition.closeTracker = findCloseTracker(scope, flowInfo, acquisition.receiver);
			return flowInfo;
		}
		// client has checked that the resolvedType is an AutoCloseable, hence the following cast is safe:
		if (acquisition.resolvedType.hasTypeBit(TypeIds.BitResourceFreeCloseable)
				&& !isBlacklistedMethod(acquisition)) {
			// remove unnecessary attempts (closeable is not relevant)
			if (acquisition.closeTracker != null) {
				acquisition.closeTracker.withdraw();
				acquisition.closeTracker = null;
			}
			return flowInfo;
		} else { // regular resource
			FakedTrackingVariable tracker = acquisition.closeTracker;
			if (scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled) {
				long owningTagBits = acquisition.binding.tagBits & TagBits.AnnotationOwningMASK;
				int initialNullStatus = (owningTagBits == TagBits.AnnotationNotOwning) ? FlowInfo.NON_NULL : FlowInfo.NULL;
				if (tracker == null) {
					acquisition.closeTracker =
							tracker = new FakedTrackingVariable(scope, acquisition, flowInfo, initialNullStatus); // no local available, closeable is unassigned
					tracker.owningState = owningStateFromTagBits(owningTagBits, OWNED_PER_DEFAULT);
				} else {
					flowInfo.markNullStatus(tracker.binding, initialNullStatus);
				}
				tracker.acquisition = acquisition;
				return flowInfo;
			}
			if (tracker != null) {
				// pre-connected tracker means: directly assigning the acquisition to a local, forget special treatment:
				// (in the unannotated case the pre-connected tracker has no valuable information)
				tracker.withdraw();
				acquisition.closeTracker = null;
				return flowInfo;
			} else {
				tracker = new FakedTrackingVariable(scope, acquisition, flowInfo, FlowInfo.UNKNOWN); // no local available, closeable is unassigned
				acquisition.closeTracker = tracker;
			}
			tracker.acquisition = acquisition;
			FlowInfo outsideInfo = flowInfo.copy();
			outsideInfo.markAsDefinitelyNonNull(tracker.binding);
			tracker.markNullStatus(flowInfo, flowContext, FlowInfo.NULL);
			return FlowInfo.conditional(outsideInfo, flowInfo);
		}
	}

	static boolean isFluentMethod(MethodBinding binding) {
		if (binding.isStatic())
			return false;
		ReferenceBinding declaringClass = binding.declaringClass;
		while (declaringClass != null) {
			if (declaringClass.equals(binding.returnType)) {
				for (char[][] compoundName : TypeConstants.FLUENT_RESOURCE_CLASSES) {
					if (CharOperation.equals(compoundName, declaringClass.compoundName))
						return true;
				}
				return false;
			}
			declaringClass = declaringClass.superclass();
		}
		return false;
	}

	private static FakedTrackingVariable getMoreUnsafeFromBranches(ConditionalExpression conditionalExpression,
			FlowInfo flowInfo, Function<Expression,FakedTrackingVariable> retriever)
	{
		FakedTrackingVariable trackerIfTrue = retriever.apply(conditionalExpression.valueIfTrue);
		FakedTrackingVariable trackerIfFalse = retriever.apply(conditionalExpression.valueIfFalse);
		if (trackerIfTrue == null)
			return trackerIfFalse;
		if (trackerIfFalse == null)
			return trackerIfTrue;
		return pickMoreUnsafe(trackerIfTrue, trackerIfFalse, flowInfo);
	}

	private static FakedTrackingVariable pickMoreUnsafe(FakedTrackingVariable tracker1, FakedTrackingVariable tracker2, FlowInfo info) {
		// whichever of the two trackers has stronger indication to be leaking will be returned,
		// the other one will be removed from the scope (considered to be merged into the former).
		int status1 = info.nullStatus(tracker1.binding);
		int status2 = info.nullStatus(tracker2.binding);
		if (status1 == FlowInfo.NULL || status2 == FlowInfo.NON_NULL) return pick(tracker1, tracker2);
		if (status1 == FlowInfo.NON_NULL || status2 == FlowInfo.NULL) return pick(tracker2, tracker1);
		if ((status1 & FlowInfo.POTENTIALLY_NULL) != 0) return pick(tracker1, tracker2);
		if ((status2 & FlowInfo.POTENTIALLY_NULL) != 0) return pick(tracker2, tracker1);
		return pick(tracker1, tracker2);
	}

	private static FakedTrackingVariable pick(FakedTrackingVariable tracker1, FakedTrackingVariable tracker2) {
		tracker2.withdraw();
		return tracker1;
	}

	private static void handleRegularResource(BlockScope scope, FlowInfo flowInfo, FlowContext flowContext, AllocationExpression allocation) {
		FakedTrackingVariable presetTracker = allocation.closeTracker;
		LocalVariableBinding local = null;
		if (presetTracker != null && presetTracker.originalBinding != null) {
			if (presetTracker.isInFinallyBlockOf(flowContext) && presetTracker.recordFirstModification(flowContext)) {
				// not a re-assignment after the one seen within finally, but this excuse is valid only once.
			} else {
				local = presetTracker.originalBinding;
				// the current assignment forgets a previous resource in the LHS, may cause a leak
				// report now because handleResourceAssignment can't distinguish this from a self-wrap situation
				handleReassignment(flowInfo, presetTracker, presetTracker.currentAssignment);
				if (local != null && !presetTracker.hasDefinitelyNoResource(flowInfo) && presetTracker.currentAssignment instanceof Assignment) {
					boolean useAnnotations = scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled;
					allocation.closeTracker =
							local.closeTracker =
							new FakedTrackingVariable(local, scope, presetTracker.currentAssignment, flowInfo, flowContext, FlowInfo.NULL, useAnnotations);
					if (useAnnotations)
						allocation.closeTracker.owningState = OWNED;
					// if finally closes the given local, this may affect any resource bound to the same local
					// (but re-assignment over an existing unclosed resource will be recorded (at-location) in handleReassignment() above).
					FlowInfo enclosingFinallyInfo = null;
					Scope current = scope;
					while (current instanceof BlockScope && enclosingFinallyInfo == null) {
						enclosingFinallyInfo = ((BlockScope) current).finallyInfo;
						current = current.parent;
					}
					if (enclosingFinallyInfo != null) {
						int finallyNullStatus = enclosingFinallyInfo.nullStatus(presetTracker.binding);
						enclosingFinallyInfo.markNullStatus(local.closeTracker.binding, finallyNullStatus);
					}
					presetTracker.markNullStatus(flowInfo, flowContext, FlowInfo.UNKNOWN); // no longer relevant in this flow
				}
			}
		} else {
			allocation.closeTracker = new FakedTrackingVariable(scope, allocation, flowInfo, FlowInfo.UNKNOWN); // no local available, closeable is unassigned
		}
		allocation.closeTracker.markNullStatus(flowInfo, flowContext, FlowInfo.NULL);
	}

	/** Was this FTV created from the finally block of the current context? */
	private boolean isInFinallyBlockOf(FlowContext flowContext) {
		if (this.tryContext != null) {
			do {
				if (flowContext == this.tryContext)
					return true;
				flowContext = flowContext.parent;
			} while (flowContext != null);
		}
		return false;
	}

	/** Try to remember flowContext as the first one where this FTV is being modified. */
	private boolean recordFirstModification(FlowContext flowContext) {
		if (this.modificationContexts == null) {
			this.modificationContexts = new HashSet<>();
			this.modificationContexts.add(flowContext);
			return true;
		}
		FlowContext current = flowContext;
		while (current != null) {
			if (this.modificationContexts.contains(current))
				return false;
			current = current.parent;
		}
		return this.modificationContexts.add(flowContext);
	}

	private static FakedTrackingVariable handleReassignment(FlowInfo flowInfo, FakedTrackingVariable existingTracker, ASTNode location) {
		int riskyStatus = existingTracker.riskyNullStatusAt(flowInfo);
		if (riskyStatus != 0
				&& !(location instanceof LocalDeclaration))	// forgetting old val in local decl is syntactically impossible
		{
			existingTracker.recordErrorLocation(location, riskyStatus);
			return null; // stop using this tracker after re-assignment
		}
		return existingTracker;
	}

	/** Find an existing tracking variable for the argument of an allocation for a resource wrapper. */
	private static FakedTrackingVariable findCloseTracker(BlockScope scope, FlowInfo flowInfo, Expression arg)
	{
		while (arg instanceof Assignment) {
			Assignment assign = (Assignment)arg;
			LocalVariableBinding innerLocal = assign.localVariableBinding();
			if (innerLocal != null) {
				// nested assignment has already been processed
				return innerLocal.closeTracker;
			} else {
				arg = assign.expression; // unwrap assignment and fall through
			}
		}
		if (arg instanceof SingleNameReference) {
			// is allocation arg a reference to an existing closeable?
			LocalVariableBinding local = arg.localVariableBinding();
			if (local != null) {
				return local.closeTracker;
			}
		} else if (arg instanceof AllocationExpression) {
			// nested allocation
			return ((AllocationExpression)arg).closeTracker;
		} else if (arg instanceof MessageSend) {
			return ((MessageSend) arg).closeTracker;
		}
		return null; // not a tracked expression
	}

	/**
	 * Given the rhs of an assignment or local declaration has a (Auto)Closeable type (or null), setup for leak analysis now:
	 * Create or re-use a tracking variable, and wire and initialize everything.
	 * @param scope scope containing the assignment
	 * @param upstreamInfo info without analysis of the rhs, use this to determine the status of a resource being disconnected
	 * @param flowInfo info with analysis of the rhs, use this for recording resource status because this will be passed downstream
	 * @param location where to report warnigs/errors against
	 * @param rhs the right hand side of the assignment, this expression is to be analyzed.
	 *			The caller has already checked that the rhs is either of a closeable type or null.
	 * @param local the local variable into which the rhs is being assigned
	 */
	public static void handleResourceAssignment(BlockScope scope, FlowInfo upstreamInfo, FlowInfo flowInfo, FlowContext flowContext, ASTNode location, Expression rhs, LocalVariableBinding local)
	{
		// does the LHS (local) already have a tracker, indicating we may leak a resource by the assignment?
		FakedTrackingVariable previousTracker = getRiskyCloseTrackerAt(local, scope, upstreamInfo);
		FakedTrackingVariable disconnectedTracker = null;
		if (previousTracker != null) {
			// assigning to a variable already holding an AutoCloseable, has it been closed before?
			if (previousTracker.riskyNullStatusAt(upstreamInfo) != 0) // only if previous value may be relevant
				disconnectedTracker = previousTracker; // report error below, unless we have a self-wrap assignment
		} else {
			previousTracker = local.closeTracker; // not yet risky, but may still be releavant below
		}

		boolean useAnnotations = scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled;

		rhsAnalyis:
		if (rhs.resolvedType != TypeBinding.NULL) {
			// new value is AutoCloseable, start tracking, possibly re-using existing tracker var:
			FakedTrackingVariable rhsTrackVar = getCloseTrackingVariable(rhs, flowInfo, flowContext, useAnnotations);
			if (rhsTrackVar != null) {								// 1. if RHS has a tracking variable...
				if (local.closeTracker == null) {
					// null shouldn't occur but let's play safe:
					if (rhsTrackVar.originalBinding != null) {
						local.closeTracker = rhsTrackVar;			//		a.: let fresh LHS share it
					} else if (rhsTrackVar.originalFieldBinding != null) {
						local.closeTracker = rhsTrackVar;
					}
					if (rhsTrackVar.currentAssignment == location) {
						// pre-set tracker from lhs - passed from outside (or foreach)?
						// now it's a fresh resource
						rhsTrackVar.globalClosingState &= ~(SHARED_WITH_OUTSIDE|OWNED_BY_OUTSIDE|FOREACH_ELEMENT_VAR);
					}
				} else {
					if (rhs instanceof AllocationExpression || rhs instanceof ConditionalExpression || rhs instanceof SwitchExpression || rhs instanceof MessageSend) {
						if (rhsTrackVar == disconnectedTracker)
							return;									// 		b.: self wrapper: res = new Wrap(res); -> done!
						if (local.closeTracker == rhsTrackVar
								&& rhsTrackVar.isNotOwned()) {
																	// 		c.: assigning a fresh resource (pre-connected alloc)
																	//			to a local previously holding an alien resource -> start over
							local.closeTracker = new FakedTrackingVariable(local, scope, location, flowInfo, flowContext, FlowInfo.NULL, useAnnotations);
							// still check disconnectedTracker below
							break rhsAnalyis;
						}
					}
					rhsTrackVar.attachTo(local);					//		d.: conflicting LHS and RHS, proceed with recordErrorLocation below
				}
				// keep close-status of RHS unchanged across this assignment
			} else if (previousTracker != null) {					// 2. re-use tracking variable from the LHS?
				FlowContext currentFlowContext = flowContext;
				checkReuseTracker : {
					if (previousTracker.tryContext != null) {
						while (currentFlowContext != null) {
							if (previousTracker.tryContext == currentFlowContext) {
								// "previous" location was the finally block of the current try statement.
								// -> This is not a re-assignment.
								// see https://bugs.eclipse.org/388996
								break checkReuseTracker;
							}
							currentFlowContext = currentFlowContext.parent;
						}
					}
					// re-assigning from a fresh value, mark as not-closed again:
					if ((previousTracker.globalClosingState & (SHARED_WITH_OUTSIDE|OWNED_BY_OUTSIDE|FOREACH_ELEMENT_VAR)) == 0
							&& flowInfo.hasNullInfoFor(previousTracker.binding)) // avoid spilling info into a branch that doesn't see the corresponding resource
						previousTracker.markNullStatus(flowInfo, flowContext, FlowInfo.NULL);
					local.closeTracker = analyseCloseableExpression(scope, flowInfo, flowContext, useAnnotations, local, location, rhs, previousTracker);
				}
			} else {												// 3. no re-use, create a fresh tracking variable:
				rhsTrackVar = analyseCloseableExpression(scope, flowInfo, flowContext, useAnnotations, local, location, rhs, null);
				if (rhsTrackVar != null) {
					rhsTrackVar.attachTo(local);
					if (!useAnnotations) {
						// a fresh resource, mark as not-closed:
						if ((rhsTrackVar.globalClosingState & (SHARED_WITH_OUTSIDE|OWNED_BY_OUTSIDE|FOREACH_ELEMENT_VAR)) == 0)
							rhsTrackVar.markNullStatus(flowInfo, flowContext, FlowInfo.NULL);
					}
// TODO(stephan): this might be useful, but I could not find a test case for it:
//					if (flowContext.initsOnFinally != null)
//						flowContext.initsOnFinally.markAsDefinitelyNonNull(trackerBinding);
				}
			}
		}

		if (disconnectedTracker != null) {
			if (disconnectedTracker.innerTracker != null && disconnectedTracker.innerTracker.binding.declaringScope == scope) {
				// discard tracker for the wrapper but keep the inner:
				disconnectedTracker.innerTracker.outerTracker = null;
				scope.pruneWrapperTrackingVar(disconnectedTracker);
			} else {
				int upstreamStatus = disconnectedTracker.riskyNullStatusAt(upstreamInfo);
				if (upstreamStatus != 0)
					disconnectedTracker.recordErrorLocation(location, upstreamStatus);
			}
		}
	}

	public int riskyNullStatusAt(FlowInfo info) {
		if (hasDefinitelyNoResource(info))
			return 0;
		int nullStatus = getNullStatusAggressively(this.binding, info);
		if ((nullStatus & (FlowInfo.UNKNOWN | FlowInfo.NON_NULL)) == 0)
			return nullStatus;
		return 0;
	}

	/**
	 * When assigning an rhs of an (Auto)Closeable type (or null) to a field, inspect annotations
	 * to find out if the assignment assigns ownership to the instance (rather than current method).
	 * @param scope scope containing the assignment
	 * @param flowInfo info with analysis of the rhs, use this for recording resource status because this will be passed downstream
	 * @param location where to report warnigs/errors against
	 * @param rhs the right hand side of the assignment, this expression is to be analyzed.
	 *			The caller has already checked that the rhs is either of a closeable type or null.
	 */
	public static void handleResourceFieldAssignment(BlockScope scope, FlowInfo flowInfo, FlowContext flowContext, ASTNode location, Expression rhs)
	{
		boolean useAnnotations = scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled;

		if (rhs.resolvedType != TypeBinding.NULL) {
			// new value is AutoCloseable, start tracking, possibly re-using existing tracker var:
			FakedTrackingVariable rhsTrackVar = getCloseTrackingVariable(rhs, flowInfo, flowContext, useAnnotations);
			if (rhsTrackVar != null) {
				if (useAnnotations) {
					if (location instanceof Assignment) {
						Expression lhs = ((Assignment) location).lhs;
						FieldBinding field = null;
						// only consider access to field of the current instance:
						if (lhs instanceof SingleNameReference) {
							field = ((SingleNameReference) lhs).fieldBinding();
						} else if (lhs instanceof FieldReference) {
							FieldReference fieldReference = (FieldReference) lhs;
							if (fieldReference.receiver.isThis())
								field = fieldReference.binding;
						}
						if (field != null&& (field.tagBits & TagBits.AnnotationNotOwning) == 0) { // assignment to @NotOwned has no meaning
							if ((field.tagBits & TagBits.AnnotationOwning) != 0) {
								rhsTrackVar.markNullStatus(flowInfo, flowContext, FlowInfo.NON_NULL);
							} else {
								rhsTrackVar.markAsShared();
							}
						}
					}
				}
// TODO(stephan): this might be useful, but I could not find a test case for it:
//					if (flowContext.initsOnFinally != null)
//						flowContext.initsOnFinally.markAsDefinitelyNonNull(trackerBinding);
			}
		}
	}
	/**
	 * Analyze structure of a closeable expression, matching (chained) resources against our white lists.
	 * @param scope scope of the expression
	 * @param flowInfo where to record close status
	 * @param useAnnotations is annotation based resource analysis enabled
	 * @param local local variable to which the closeable is being assigned
	 * @param location where to flag errors/warnings against
	 * @param expression expression to be analyzed
	 * @param previousTracker when analyzing a re-assignment we may already have a tracking variable for local,
	 *  		which we should then re-use
	 * @return a tracking variable associated with local or null if no need to track
	 */
	private static FakedTrackingVariable analyseCloseableExpression(BlockScope scope, FlowInfo flowInfo, FlowContext flowContext, boolean useAnnotations,
									LocalVariableBinding local, ASTNode location, Expression expression, FakedTrackingVariable previousTracker)
	{
		// unwrap uninteresting nodes:
		while (true) {
			if (expression instanceof Assignment)
				expression = ((Assignment)expression).expression;
			else if (expression instanceof CastExpression)
				expression = ((CastExpression) expression).expression;
			else
				break;
		}
		if (expression instanceof Literal) {
			return null;
		}
		// delegate to components:
		if (expression instanceof ConditionalExpression) {
			return getMoreUnsafeFromBranches((ConditionalExpression) expression, flowInfo,
						branch -> analyseCloseableExpression(scope, flowInfo, flowContext, useAnnotations,
																local, location, branch, previousTracker));
		} else if (expression instanceof SwitchExpression se) {
			FakedTrackingVariable mostRisky = null;
			for (Expression result : se.resultExpressions()) {
				FakedTrackingVariable current = analyseCloseableExpression(scope, flowInfo, flowContext, useAnnotations,
						local, location, result, previousTracker);
				if (mostRisky == null)
					mostRisky = current;
				else
					mostRisky = pickMoreUnsafe(mostRisky, current, flowInfo);
			}
			return mostRisky;
		}

		boolean isResourceProducer = false;
		if (expression.resolvedType instanceof ReferenceBinding) {
			ReferenceBinding resourceType = (ReferenceBinding) expression.resolvedType;
			if (resourceType.hasTypeBit(TypeIds.BitResourceFreeCloseable)) {
				if (isBlacklistedMethod(expression))
					isResourceProducer = true;
				else
					return null; // (a) resource-free closeable: -> null
			}
		}

		if (local == null) {
			FakedTrackingVariable tracker = null;
			if (useAnnotations && (expression.bits & RestrictiveFlagMASK) == Binding.FIELD) {
				// field read
				FieldBinding fieldBinding = ((Reference) expression).lastFieldBinding();
				long owningBits = 0;
				if (fieldBinding != null) {
					owningBits = fieldBinding.getAnnotationTagBits() & TagBits.AnnotationOwningMASK;
				}
				int status = FlowInfo.UNKNOWN;
				if (owningBits == TagBits.AnnotationOwning) {
					status = FlowInfo.NON_NULL;
				} else if (owningBits == TagBits.AnnotationNotOwning) {
					status = FlowInfo.POTENTIALLY_NULL;
				}
				tracker = new FakedTrackingVariable(local, scope, location, flowInfo, flowContext, status, useAnnotations);
				tracker.owningState = NOT_OWNED;
			}
			return tracker;
		}

		// analyze by node type:
		if (expression instanceof AllocationExpression) {
			// allocation expressions already have their tracking variables analyzed by analyseCloseableAllocation(..)
			FakedTrackingVariable tracker = ((AllocationExpression) expression).closeTracker;
			if (tracker != null && tracker.originalBinding == null) {
				// tracker without original binding (unassigned closeable) shouldn't reach here but let's play safe
				return null;
			}
			return tracker;
		} else if (expression instanceof MessageSend
				|| expression instanceof ArrayReference)
		{
			int initialNullStatus = 0;
			if (isBlacklistedMethod(expression)) {
				initialNullStatus = FlowInfo.NULL;
			} else if (useAnnotations) {
				initialNullStatus = getNullStatusFromMessageSend(expression);
			}
			if (initialNullStatus != 0)
				return new FakedTrackingVariable(local, location, flowInfo, flowContext, initialNullStatus, useAnnotations);

			// we *might* be responsible for the resource obtained
			FakedTrackingVariable tracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, FlowInfo.POTENTIALLY_NULL, useAnnotations); // shed some doubt
			if (!isResourceProducer && !useAnnotations)
				tracker.globalClosingState |= SHARED_WITH_OUTSIDE;
			return tracker;
		} else if (
				(expression.bits & RestrictiveFlagMASK) == Binding.FIELD
				||((expression instanceof QualifiedNameReference)
						&& ((QualifiedNameReference) expression).isFieldAccess()))
		{
			if (!useAnnotations) {
				// responsibility for this resource probably lies at a higher level
				FakedTrackingVariable tracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, FlowInfo.UNKNOWN, useAnnotations);
				tracker.globalClosingState |= OWNED_BY_OUTSIDE;
				// leave state as UNKNOWN, the bit OWNED_BY_OUTSIDE will prevent spurious warnings
				return tracker;
			}
		}

		if (local.closeTracker != null)
			// (c): inner has already been analyzed: -> re-use track var
			return local.closeTracker;
		FakedTrackingVariable newTracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, FlowInfo.UNKNOWN, useAnnotations);
		LocalVariableBinding rhsLocal = expression.localVariableBinding();
		if (rhsLocal != null && rhsLocal.isParameter()) {
			newTracker.globalClosingState |= OWNED_BY_OUTSIDE;
		}
		return newTracker;
	}

	private static boolean isBlacklistedMethod(Expression expression) {
		if (expression instanceof MessageSend) {
			MethodBinding method = ((MessageSend) expression).binding;
			if (method != null && method.isValidBinding())
				// for all methods in java.nio.file.Files that return a resource (Stream) it really needs closing
				return CharOperation.equals(method.declaringClass.compoundName, TypeConstants.JAVA_NIO_FILE_FILES);
		}
		return false;
	}

	/* pre: usesOwningAnnotations. */
	protected static int getNullStatusFromMessageSend(Expression expression) {
		if (expression instanceof MessageSend) {
			if ((((MessageSend) expression).binding.tagBits & TagBits.AnnotationNotOwning) != 0)
				return FlowInfo.NON_NULL;
			return FlowInfo.NULL; // per default assume responsibility to close
		}
		return 0;
	}

	public static void cleanUpAfterAssignment(BlockScope currentScope, int lhsBits, Expression expression) {
		// remove all remaining track vars with no original binding

		boolean useAnnotations = currentScope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled;
		if (useAnnotations && (lhsBits & Binding.FIELD) != 0) {
			return;
		}

		// unwrap uninteresting nodes:
		while (true) {
			if (expression instanceof Assignment)
				expression = ((Assignment)expression).expression;
			else if (expression instanceof CastExpression)
				expression = ((CastExpression) expression).expression;
			else
				break;
		}
		if (expression instanceof AllocationExpression) {
			FakedTrackingVariable tracker = ((AllocationExpression) expression).closeTracker;
			if (tracker != null && tracker.originalBinding == null) {
				tracker.withdraw();
				((AllocationExpression) expression).closeTracker = null;
			}
		} else if (expression instanceof MessageSend) {
			FakedTrackingVariable tracker = ((MessageSend) expression).closeTracker;
			if (tracker != null && tracker.originalBinding == null) {
				tracker.withdraw();
				((MessageSend) expression).closeTracker = null;
			}
		} else {
			// assignment passing a local into a field?
			LocalVariableBinding local = expression.localVariableBinding();
			if (local != null
					&& ((lhsBits & Binding.FIELD) != 0)
					&& !useAnnotations
					&& local.closeTracker != null) {
				local.closeTracker.withdraw(); // TODO: may want to use local.closeTracker.markPassedToOutside(..,true)
			}
		}
	}

	/**
	 * Unassigned closeables are not visible beyond their enclosing statement, immediately report and remove after each statement.
	 * @param returnMissingOwning at a return statement this signals {@code true} when the enclosing method lacks an {@code @Owning} annotation.
	 */
	public static void cleanUpUnassigned(BlockScope scope, ASTNode location, FlowInfo flowInfo, boolean returnMissingOwning) {
		if (!scope.hasResourceTrackers()) return;
		boolean useAnnotations = scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled;
		location.traverse(new ASTVisitor() {
				@Override
				public boolean visit(MessageSend messageSend, BlockScope skope) {
					FakedTrackingVariable closeTracker = messageSend.closeTracker;
					handle(closeTracker, flowInfo, messageSend, skope);
					return true;
				}
				@Override
				public boolean visit(AllocationExpression allocation, BlockScope skope) {
					if (handle(allocation.closeTracker, flowInfo, allocation, skope))
						allocation.closeTracker = null;
					return true;
				}

				/** @return has the tracker been withdrawn? */
				protected boolean handle(FakedTrackingVariable closeTracker, FlowInfo flow, ASTNode loc, BlockScope skope) {
					if (closeTracker != null && closeTracker.originalBinding == null && closeTracker.originalFieldBinding == null) {
						int nullStatus = closeTracker.riskyNullStatusAt(flow);
						if (nullStatus != 0) {
							int reportFlag = closeTracker.reportError(skope.problemReporter(), loc, nullStatus);
							closeTracker.markAllConnected(ftv -> ftv.globalClosingState |= reportFlag);
						} else if (returnMissingOwning && useAnnotations) {
							skope.problemReporter().shouldMarkMethodAsOwning(location);
						}
						closeTracker.withdraw();
						return true;
					}
					return false;
				}
			},
			scope);
	}

	/** Answer wither the given type binding is a subtype of java.lang.AutoCloseable. */
	public static boolean isAnyCloseable(TypeBinding typeBinding) {
		return typeBinding instanceof ReferenceBinding
			&& typeBinding.hasTypeBit(TypeIds.BitAutoCloseable|TypeIds.BitCloseable);
	}

	/** Answer wither the given type binding is a subtype of java.lang.AutoCloseable. */
	public static boolean isCloseableNotWhiteListed(TypeBinding typeBinding) {
		if (typeBinding instanceof ReferenceBinding) {
			ReferenceBinding referenceBinding = (ReferenceBinding)typeBinding;
			return referenceBinding.hasTypeBit(TypeIds.BitAutoCloseable|TypeIds.BitCloseable)
					&& !referenceBinding.hasTypeBit(TypeIds.BitResourceFreeCloseable);
		}
		return false;
	}

	public int findMostSpecificStatus(FlowInfo flowInfo, BlockScope currentScope, BlockScope locationScope) {
		int status = FlowInfo.UNKNOWN;
		FakedTrackingVariable currentTracker = this;
		// loop as to consider wrappers (per white list) encapsulating an inner resource.
		while (currentTracker != null) {
			LocalVariableBinding currentVar = currentTracker.binding;
			int currentStatus = getNullStatusAggressively(currentVar, flowInfo);
			if (locationScope != null) // only check at method exit points
				currentStatus = mergeCloseStatus(locationScope, currentStatus, currentVar, currentScope);
			if (currentStatus == FlowInfo.NON_NULL) {
				status = currentStatus;
				break; // closed -> stop searching
			} else if (status == FlowInfo.NULL || status == FlowInfo.UNKNOWN) {
				status = currentStatus; // improved although not yet safe -> keep searching for better
			}
			currentTracker = currentTracker.innerTracker;
		}
		return status;
	}

	/**
	 * Get the null status looking even into unreachable flows
	 * @return one of the constants FlowInfo.{NULL,POTENTIALLY_NULL,POTENTIALLY_NON_NULL,NON_NULL}.
	 */
	private int getNullStatusAggressively(LocalVariableBinding local, FlowInfo flowInfo) {
		if (flowInfo == FlowInfo.DEAD_END) {
			return FlowInfo.UNKNOWN;
		}
		int reachMode = flowInfo.reachMode();
		int status = 0;
		try {
			// unreachable flowInfo is too shy in reporting null-issues, temporarily forget reachability:
			if (reachMode != FlowInfo.REACHABLE)
				flowInfo.tagBits &= ~FlowInfo.UNREACHABLE;
			status = flowInfo.nullStatus(local);
			if (TEST_372319) { // see https://bugs.eclipse.org/372319
				try {
					Thread.sleep(5); // increase probability of concurrency bug
				} catch (InterruptedException e) { /* nop */ }
			}
		} finally {
			// reset
			flowInfo.tagBits |= reachMode;
		}
		// at this point some combinations are not useful so flatten to a single bit:
		if ((status & FlowInfo.NULL) != 0) {
			if ((status & (FlowInfo.NON_NULL | FlowInfo.POTENTIALLY_NON_NULL)) != 0)
				return FlowInfo.POTENTIALLY_NULL; 	// null + doubt = pot null
			return FlowInfo.NULL;
		} else if ((status & FlowInfo.NON_NULL) != 0) {
			if ((status & FlowInfo.POTENTIALLY_NULL) != 0)
				return FlowInfo.POTENTIALLY_NULL;	// non-null + doubt = pot null
			return FlowInfo.NON_NULL;
		} else if ((status & FlowInfo.POTENTIALLY_NULL) != 0) {
			return FlowInfo.POTENTIALLY_NULL;
		} else if (status == FlowInfo.UNKNOWN) {
			// if unassigned resource (not having an originalBinding) is not withdrawn it is unclosed:
			if (this.originalBinding == null && this.originalFieldBinding == null)
				return FlowInfo.NULL;
		}
		return status;
	}

	public int mergeCloseStatus(BlockScope currentScope, int status, LocalVariableBinding local, BlockScope outerScope) {
		// get the most suitable null status representing whether resource 'binding' has been closed
		// start at 'currentScope' and potentially travel out until 'outerScope'
		// at each scope consult any recorded 'finallyInfo'.
		if (status != FlowInfo.NON_NULL) {
			if (currentScope.finallyInfo != null) {
				int finallyStatus = currentScope.finallyInfo.nullStatus(local);
				if (finallyStatus == FlowInfo.NON_NULL)
					return finallyStatus;
				if (finallyStatus != FlowInfo.NULL && currentScope.finallyInfo.hasNullInfoFor(local)) // neither is NON_NULL, but not both are NULL => call it POTENTIALLY_NULL
					status = FlowInfo.POTENTIALLY_NULL;
			}
			if (currentScope != outerScope && currentScope.parent instanceof BlockScope)
				return mergeCloseStatus(((BlockScope) currentScope.parent), status, local, outerScope);
		}
		return status;
	}

	/** Mark that this resource is closed locally. */
	public void markClose(FlowInfo flowInfo, FlowContext flowContext) {
		markAllConnected(current -> {
			flowInfo.markAsDefinitelyNonNull(current.binding);
			current.globalClosingState |= CLOSE_SEEN;
			flowContext.markFinallyNullStatus(current.binding, FlowInfo.NON_NULL);
		});
	}

	public void markNullStatus(FlowInfo flowInfo, FlowContext flowContext, int status) {
		markAllConnected(current -> {
			flowInfo.markNullStatus(current.binding, status);
			flowContext.markFinallyNullStatus(current.binding, status);
		});
	}

	public void markOwnedByOutside(FlowInfo flowInfo, FlowContext flowContext) {
		markAllConnected(current -> {
			flowInfo.markAsDefinitelyNonNull(current.binding);
			flowContext.markFinallyNullStatus(current.binding, FlowInfo.NON_NULL);
			current.globalClosingState = FakedTrackingVariable.OWNED_BY_OUTSIDE;
		});
	}

	public void markAllConnected(Consumer<FakedTrackingVariable> operation) {
		FakedTrackingVariable current = this;
		do {
			operation.accept(current);
			current = current.innerTracker;
		} while (current != null);
		current = this.outerTracker;
		while (current != null) {
			operation.accept(current);
			current = current.outerTracker;
		}
	}

	/** Mark that this resource is closed from a nested method (inside a local class). */
	public void markClosedInNestedMethod() {
		this.globalClosingState |= CLOSED_IN_NESTED_METHOD;
	}

	public boolean isClosedInNestedMethod() {
		return (this.globalClosingState & CLOSED_IN_NESTED_METHOD) != 0;
	}

	/** Mark that this resource is closed from a try-with-resource with the tracking variable being effectively final). */
	public void markClosedEffectivelyFinal() {
		this.globalClosingState |= TWR_EFFECTIVELY_FINAL;
	}
	/**
	 * Mark that this resource is passed to some outside code
	 * (as argument to a method/ctor call or as a return value from the current method),
	 * and thus should be considered as potentially closed.
	 * @param owned should the resource be considered owned by some outside?
	 */
	public static FlowInfo markPassedToOutside(BlockScope scope, Expression expression, FlowInfo flowInfo, FlowContext flowContext, boolean owned) {

		FakedTrackingVariable trackVar = getCloseTrackingVariable(expression, flowInfo, flowContext,
				scope.compilerOptions().isAnnotationBasedResourceAnalysisEnabled);
		if (trackVar != null) {
			// insert info that the tracked resource *may* be closed (by the target method, i.e.)
			FlowInfo infoResourceIsClosed = owned ? flowInfo : flowInfo.copy();
			int flag = owned ? OWNED_BY_OUTSIDE : SHARED_WITH_OUTSIDE;
			trackVar.markAllConnected(ftv -> {
				ftv.globalClosingState |= flag;
				if (scope.methodScope() != ftv.methodScope)
					ftv.globalClosingState |= CLOSED_IN_NESTED_METHOD;
				ftv.markNullStatus(flowInfo, flowContext, FlowInfo.NON_NULL);
			});
			if (owned) {
				return infoResourceIsClosed; // don't let downstream signal any problems on this flow
			} else {
				return FlowInfo.conditional(flowInfo, infoResourceIsClosed).unconditionalCopy(); // only report potential problems on this flow
			}
		}
		return flowInfo;
	}

	public static void markForeachElementVar(LocalDeclaration local) {
		if (local.binding != null && local.binding.closeTracker != null) {
			local.binding.closeTracker.globalClosingState |= FOREACH_ELEMENT_VAR;
		}
	}

	/**
	 * Iterator for a set of FakedTrackingVariable, which dispenses the elements
	 * according to the priorities defined by enum {@link Stage}.
	 * Resources whose outer is owned by an enclosing scope are never answered,
	 * unless we are analysing on behalf of an exit (return/throw).
	 */
	public static class IteratorForReporting implements Iterator<FakedTrackingVariable> {

		private final Set<FakedTrackingVariable> varSet;
		private final Scope scope;
		private final boolean atExit;

		private Stage stage;
		private Iterator<FakedTrackingVariable> iterator;
		private FakedTrackingVariable next;

		enum Stage {
			/** 1. prio: all top-level resources, ie., resources with no outer. */
			OuterLess,
			/** 2. prio: resources whose outer has already been processed (element of the same varSet). */
			InnerOfProcessed,
			/** 3. prio: resources whose outer is not owned by any enclosing scope. */
			InnerOfNotEnclosing,
			/** 4. prio: when analysing on behalf of an exit point: anything not picked before. */
			AtExit
		}

		public IteratorForReporting(List<FakedTrackingVariable> variables, Scope scope, boolean atExit) {
			this.varSet = new HashSet<>(variables);
			this.scope = scope;
			this.atExit = atExit;
			setUpForStage(Stage.OuterLess);
		}
		@Override
		public boolean hasNext() {
			FakedTrackingVariable trackingVar;
			switch (this.stage) {
				case OuterLess:
					while (this.iterator.hasNext()) {
						trackingVar = this.iterator.next();
						if (trackingVar.outerTracker == null)
							return found(trackingVar);
					}
					setUpForStage(Stage.InnerOfProcessed);
					//$FALL-THROUGH$
				case InnerOfProcessed:
					while (this.iterator.hasNext()) {
						trackingVar = this.iterator.next();
						FakedTrackingVariable outer = trackingVar.outerTracker;
						if (outer.binding.declaringScope == this.scope && !this.varSet.contains(outer))
							return found(trackingVar);
					}
					setUpForStage(Stage.InnerOfNotEnclosing);
					//$FALL-THROUGH$
				case InnerOfNotEnclosing:
					searchAlien: while (this.iterator.hasNext()) {
						trackingVar = this.iterator.next();
						FakedTrackingVariable outer = trackingVar.outerTracker;
						if (!this.varSet.contains(outer)) {
							Scope outerTrackerScope = outer.binding.declaringScope;
							Scope currentScope = this.scope;
							while ((currentScope = currentScope.parent) instanceof BlockScope) {
								if (outerTrackerScope == currentScope)
									break searchAlien;
							}
							return found(trackingVar);
						}
					}
					setUpForStage(Stage.AtExit);
					//$FALL-THROUGH$
				case AtExit:
					if (this.atExit && this.iterator.hasNext())
						return found(this.iterator.next());
					return false;
				default: throw new IllegalStateException("Unexpected Stage "+this.stage); //$NON-NLS-1$
			}
		}
		private boolean found(FakedTrackingVariable trackingVar) {
			this.iterator.remove();
			this.next = trackingVar;
			return true;
		}
		private void setUpForStage(Stage nextStage) {
			this.iterator = this.varSet.iterator();
			this.stage = nextStage;
		}
		@Override
		public FakedTrackingVariable next() {
			return this.next;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Answer true if we know for sure that no resource is bound to this variable
	 * at the point of 'flowInfo'.
	 */
	public boolean hasDefinitelyNoResource(FlowInfo flowInfo) {
		if (this.originalBinding == null) return false; // shouldn't happen but keep quiet.
		if (flowInfo.isDefinitelyNull(this.originalBinding)) {
			return true;
		}
		if (!(flowInfo.isDefinitelyAssigned(this.originalBinding)
				|| flowInfo.isPotentiallyAssigned(this.originalBinding))) {
			return true;
		}
		return false;
	}

	public boolean isClosedInFinallyOfEnclosing(BlockScope scope) {
		BlockScope currentScope = scope;
		while (true) {
			if (currentScope.finallyInfo != null
					&& currentScope.finallyInfo.isDefinitelyNonNull(this.binding)) {
				return true; // closed in enclosing finally
			}
			if (!(currentScope.parent instanceof BlockScope)) {
				return false;
			}
			currentScope = (BlockScope) currentScope.parent;
		}
	}

	public boolean isClosedInFinally() {
		return this.blockScope != null && isClosedInFinallyOfEnclosing(this.blockScope);
	}

	/**
	 * If current is the same as 'returnedResource' or a wrapper thereof,
	 * mark as reported and return true, otherwise false.
	 *
	 * When using {@code @Owning} annotation, do not mark as reported, to proceed to precise analysis
	 */
	public boolean isResourceBeingReturned(FakedTrackingVariable returnedResource, boolean useOwningAnnotation) {
		FakedTrackingVariable current = this;
		do {
			if (current == returnedResource) {
				if (!useOwningAnnotation)
					this.globalClosingState |= REPORTED_DEFINITIVE_LEAK;
				return true;
			}
			current = current.innerTracker;
		} while (current != null);
		return false;
	}

	public void withdraw() {
		// must unregister at the declaringScope, note that twr resources are owned by the scope enclosing the twr
		this.binding.declaringScope.removeTrackingVar(this);
		if (this.acquisition != null && this.acquisition.closeTracker == this)
			this.acquisition.closeTracker = null;
	}

	public void recordErrorLocation(ASTNode location, int nullStatus) {
		if (isNotOwned()) {
			return;
		}
		if (this.recordedLocations == null)
			this.recordedLocations = new HashMap<>();
		this.recordedLocations.put(location, Integer.valueOf(nullStatus));
	}

	public boolean reportRecordedErrors(Scope scope, int mergedStatus, boolean atDeadEnd) {
		FakedTrackingVariable current = this;
		while (current.globalClosingState == 0) {
			current = current.innerTracker;
			if (current == null) {
				// no relevant state found -> report:
				if (atDeadEnd && neverClosedAtLocations())
					mergedStatus = FlowInfo.NULL;
				if ((mergedStatus & (FlowInfo.NULL|FlowInfo.POTENTIALLY_NULL|FlowInfo.POTENTIALLY_NON_NULL)) != 0) {
					reportError(scope.problemReporter(), null, mergedStatus);
					return true;
				} else {
					break;
				}
			}
		}
		boolean hasReported = false;
		if (this.recordedLocations != null) {
			int reportFlags = 0;
			for (Entry<ASTNode, Integer> entry : this.recordedLocations.entrySet()) {
				reportFlags |= reportError(scope.problemReporter(), entry.getKey(), entry.getValue().intValue());
				hasReported = true;
			}
			if (reportFlags != 0) {
				// after all locations have been reported, mark as reported to prevent duplicate report via an outer wrapper
				current = this;
				do {
					current.globalClosingState |= reportFlags;
				} while ((current = current.innerTracker) != null);
			}
		}
		return hasReported;
	}

	public boolean hasRecordedLocations() {
		return this.recordedLocations != null;
	}

	private boolean neverClosedAtLocations() {
		if (this.recordedLocations != null) {
			for (Object value : this.recordedLocations.values())
				if (!value.equals(FlowInfo.NULL))
					return false;
		}
		return true;
	}

	public int reportError(ProblemReporter problemReporter, ASTNode location, int nullStatus) {
		if (isNotOwned()) {
			return 0; // TODO: should we still propagate some flags??
		}
		// which degree of problem?
		boolean isPotentialProblem = false;
		if (nullStatus == FlowInfo.NULL) {
			if ((this.globalClosingState & CLOSED_IN_NESTED_METHOD) != 0)
				isPotentialProblem = true;
		} else if ((nullStatus & (FlowInfo.POTENTIALLY_NULL|FlowInfo.POTENTIALLY_NON_NULL)) != 0) {
			isPotentialProblem = true;
		}
		// report:
		if (isPotentialProblem) {
			if ((this.globalClosingState & (REPORTED_POTENTIAL_LEAK|REPORTED_DEFINITIVE_LEAK)) != 0)
				return 0;
//			if ((this.globalClosingState & (ACQUIRED_FROM_OUTSIDE)) != 0
//					&& location instanceof ReturnStatement
//					&& ((ReturnStatement) location).expression == this.acquisition)
//				return 0; // directly returning a resource acquired from a message send: don't assume responsibility
			problemReporter.potentiallyUnclosedCloseable(this, location);
		} else {
			if ((this.globalClosingState & (REPORTED_DEFINITIVE_LEAK)) != 0)
				return 0;
			problemReporter.unclosedCloseable(this, location);
		}
		// propagate flag to inners:
		int reportFlag = isPotentialProblem ? REPORTED_POTENTIAL_LEAK : REPORTED_DEFINITIVE_LEAK;
		if (location == null) { // if location != null flags will be set after the loop over locations
			markAllConnected(current -> current.globalClosingState |= reportFlag);
		}
		return reportFlag;
	}

	public void reportExplicitClosing(ProblemReporter problemReporter) {
		if (this.originalBinding != null && this.originalBinding.isParameter())
			return;
		if ((this.globalClosingState & CLOSE_SEEN) == 0)
			return;
		if ((this.globalClosingState & (TWR_EFFECTIVELY_FINAL|OWNED_BY_OUTSIDE|REPORTED_EXPLICIT_CLOSE|FOREACH_ELEMENT_VAR)) == 0) { // can't use t-w-r for OWNED_BY_OUTSIDE
			if (this.originalFieldBinding != null && this.blockScope instanceof MethodScope) {
				AbstractMethodDeclaration method = ((MethodScope) this.blockScope).referenceMethod();
				if (method.binding != null && method.binding.isClosingMethod()) {
					return; // this is the canonical close method, nothing to warn about.
				}
			}
			this.globalClosingState |= REPORTED_EXPLICIT_CLOSE;
			problemReporter.explicitlyClosedAutoCloseable(this);
		}
	}

	public String nameForReporting(ASTNode location, ReferenceContext referenceContext) {
		if (this.name == UNASSIGNED_CLOSEABLE_NAME) {
			if (location != null && referenceContext != null) {
				CompilationResult compResult = referenceContext.compilationResult();
				if (compResult != null) {
					int[] lineEnds = compResult.getLineSeparatorPositions();
					int resourceLine = Util.getLineNumber(this.sourceStart, lineEnds , 0, lineEnds.length-1);
					int reportLine = Util.getLineNumber(location.sourceStart, lineEnds , 0, lineEnds.length-1);
					if (resourceLine != reportLine) {
						char[] replacement = Integer.toString(resourceLine).toCharArray();
						return String.valueOf(CharOperation.replace(UNASSIGNED_CLOSEABLE_NAME_TEMPLATE, TEMPLATE_ARGUMENT, replacement));
					}
				}
			}
		}
		if (this.originalFieldBinding != null)
			return String.valueOf(CharOperation.concat(ConstantPool.This, this.name, '.'));
		return String.valueOf(this.name);
	}

	public void markAsShared() {
		this.globalClosingState |= SHARED_WITH_OUTSIDE;
	}

	public boolean isShared() {
		return (this.globalClosingState & SHARED_WITH_OUTSIDE) != 0;
	}

	protected boolean isNotOwned() {
		if (this.useAnnotations)
			return this.owningState < 0;
		return (this.globalClosingState & OWNED_BY_OUTSIDE) != 0;
	}
	public boolean closeSeen() {
		return (this.globalClosingState & CLOSE_SEEN) != 0;
	}
}
