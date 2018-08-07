/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.field;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.internal.core.nd.IDestructable;
import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog.Tag;
import org.eclipse.jdt.internal.core.nd.util.MathUtils;

/**
 * Defines a data structure that will appear in the database.
 * <p>
 * There are three mechanisms for deleting a struct from the database:
 * <ul>
 * <li>Explicit deletion. This happens synchronously via manual calls to Nd.delete. Structs intended for manual
 *     deletion have refCounted=false and an empty ownerFields.
 * <li>Owner pointers. Such structs have one or more outbound pointers to an "owner" object. They are deleted
 *     asynchronously when the last owner pointer is deleted. The structs have refCounted=false and a nonempty
 *     ownerFields.
 * <li>Refcounting. Such structs are deleted asynchronously when all elements are removed from all of their ManyToOne
 *     relationships which are not marked as incoming owner pointers. Owner relationships need to be excluded from
 *     refcounting since they would always create cycles. These structs have refCounted=true.
 * </ul>
 * <p>
 * Structs deleted by refcounting and owner pointers are not intended to inherit from one another, but anything may
 * inherit from a struct that uses manual deletion and anything may inherit from a struct that uses the same deletion
 * mechanism.
 */
public final class StructDef<T> {
	Class<T> clazz;
	private StructDef<? super T> superClass;
	private Set<StructDef<?>> dependencies = new HashSet<>();
	private List<IField> fields = new ArrayList<>();
	private boolean doneCalled;
	private boolean offsetsComputed;
	private List<StructDef<? extends T>> dependents = new ArrayList<>();
	private int size;
	List<IDestructableField> destructableFields = new ArrayList<>();
	boolean refCounted;
	private List<IRefCountedField> refCountedFields = new ArrayList<>();
	private List<IRefCountedField> ownerFields = new ArrayList<>();
	boolean isAbstract;
	private ITypeFactory<T> factory;
	protected boolean hasUserDestructor;
	private DeletionSemantics deletionSemantics;
	final Tag destructTag;
	private boolean isNdNode;

	public static enum DeletionSemantics {
		EXPLICIT, OWNED, REFCOUNTED
	}

	private StructDef(Class<T> clazz) {
		this(clazz, null);
	}

	private StructDef(Class<T> clazz, StructDef<? super T> superClass) {
		this(clazz, superClass, Modifier.isAbstract(clazz.getModifiers()));
	}

	private StructDef(Class<T> clazz, StructDef<? super T> superClass, boolean isAbstract) {
		this.destructTag = ModificationLog.createTag("Destructing struct " + clazz.getSimpleName()); //$NON-NLS-1$
		this.clazz = clazz;
		this.isNdNode = NdNode.class.isAssignableFrom(clazz);
		this.superClass = superClass;
		if (this.superClass != null) {
			addDependency(this.superClass);
		}
		this.isAbstract = isAbstract;
		final String fullyQualifiedClassName = clazz.getName();

		final Constructor<T> constructor;
		if (!this.isAbstract) {
			try {
				constructor = clazz.getConstructor(new Class<?>[] { Nd.class, long.class });
			} catch (NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException("The node class " + fullyQualifiedClassName //$NON-NLS-1$
						+ " does not have an appropriate constructor for it to be used with Nd"); //$NON-NLS-1$
			}
		} else {
			constructor = null;
		}

		this.hasUserDestructor = IDestructable.class.isAssignableFrom(clazz);

		this.factory = new ITypeFactory<T>() {
			@Override
			public T create(Nd dom, long address) {
				if (StructDef.this.isAbstract) {
					throw new UnsupportedOperationException(
							"Attempting to instantiate abstract class" + fullyQualifiedClassName); //$NON-NLS-1$
				}

				try {
					return constructor.newInstance(dom, address);
				} catch (InvocationTargetException e) {
					Throwable target = e.getCause();

					if (target instanceof RuntimeException) {
						throw (RuntimeException) target;
					}

					throw new RuntimeException("Error in AutoTypeFactory", e); //$NON-NLS-1$
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("Error in AutoTypeFactory", e); //$NON-NLS-1$
				}
			}

			@Override
			public int getRecordSize() {
				return StructDef.this.size();
			}

			@Override
			public boolean hasDestructor() {
				return StructDef.this.hasUserDestructor || hasDestructableFields(); 
			}

			@Override
			public Class<?> getElementClass() {
				return StructDef.this.clazz;
			}

			@Override
			public void destruct(Nd nd, long address) {
				checkNotMutable();
				Database db = nd.getDB();
				db.getLog().start(StructDef.this.destructTag);
				try {
					if (StructDef.this.hasUserDestructor) {
						IDestructable destructable = (IDestructable)create(nd, address);
						destructable.destruct();
					}
					destructFields(nd, address);
				} finally {
					db.getLog().end(StructDef.this.destructTag);
				}
			}

			@Override
			public void destructFields(Nd dom, long address) {
				StructDef.this.destructFields(dom, address);
			}

			@Override
			public boolean isReadyForDeletion(Nd dom, long address) {
				return StructDef.this.isReadyForDeletion(dom, address);
			}
			
			@Override
			public DeletionSemantics getDeletionSemantics() {
				return StructDef.this.getDeletionSemantics();
			}
		};
	}

	public void addDependency(StructDef<?> newDependency) {
		if (newDependency.hasIndirectDependent(new HashSet<>(), this)) {
			throw new IllegalArgumentException("Circular dependency detected. Struct " //$NON-NLS-1$
					+ getStructName() + " and struct " + newDependency.getStructName()  //$NON-NLS-1$
					+ " both depend on one another"); //$NON-NLS-1$
		}
		if (this.dependencies.add(newDependency)) {
			this.superClass.dependents.add(this);
		}
	}

	private boolean hasIndirectDependent(Set<StructDef<?>> visited, StructDef<?> structDef) {
		for (StructDef<?> next : this.dependents) {
			if (!visited.add(next)) {
				continue;
			}
			if (next.equals(structDef)) {
				return true;
			}
			if (next.hasIndirectDependent(visited, structDef)) {
				return true;
			}
		}
		return false;
	}

	public Class<T> getStructClass() {
		return this.clazz;
	}

	@Override
	public String toString() {
		return this.clazz.getName();
	}

	public static <T> StructDef<T> createAbstract(Class<T> clazz) {
		return new StructDef<T>(clazz, null, true);
	}

	public static <T> StructDef<T> createAbstract(Class<T> clazz, StructDef<? super T> superClass) {
		return new StructDef<T>(clazz, superClass, true);
	}

	public static <T> StructDef<T> create(Class<T> clazz) {
		return new StructDef<T>(clazz);
	}

	public static <T> StructDef<T> create(Class<T> clazz, StructDef<? super T> superClass) {
		return new StructDef<T>(clazz, superClass);
	}

	protected boolean isReadyForDeletion(Nd dom, long address) {
		List<IRefCountedField> toIterate = Collections.EMPTY_LIST;
		switch (this.deletionSemantics) {
			case EXPLICIT: return false;
			case OWNED: toIterate = this.ownerFields; break;
			case REFCOUNTED: toIterate = this.refCountedFields; break;
		}

		for (IRefCountedField next : toIterate) {
			if (next.hasReferences(dom, address)) {
				return false;
			}
		}

		final StructDef<? super T> localSuperClass = StructDef.this.superClass;
		if (localSuperClass != null && localSuperClass.deletionSemantics != DeletionSemantics.EXPLICIT) {
			return localSuperClass.isReadyForDeletion(dom, address);
		}
		return true;
	}

	protected boolean hasDestructableFields() {
		return (!StructDef.this.destructableFields.isEmpty() || 
				(StructDef.this.superClass != null && StructDef.this.superClass.hasDestructableFields()));
	}

	public DeletionSemantics getDeletionSemantics() {
		return this.deletionSemantics;
	}

	private boolean areAllDependenciesResolved() {
		for (StructDef<?> next : this.dependencies) {
			if (!next.areOffsetsComputed()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Call this once all the fields have been added to the struct definition and it is
	 * ready to use.
	 */
	public void done() {
		if (this.doneCalled) {
			throw new IllegalStateException("May not call done() more than once"); //$NON-NLS-1$
		}
		this.doneCalled = true;

		if (areAllDependenciesResolved()) {
			computeOffsets();
		}
	}

	public void add(IField toAdd) {
		checkMutable();

		this.fields.add(toAdd);
	}

	public void addDestructableField(IDestructableField field) {
		checkMutable();

		this.destructableFields.add(field);
	}

	public StructDef<T> useStandardRefCounting() {
		checkMutable();

		this.refCounted = true;
		return this;
	}

	public void addRefCountedField(IRefCountedField result) {
		checkMutable();

		this.refCountedFields.add(result);
	}

	public void addOwnerField(IRefCountedField result) {
		checkMutable();

		this.ownerFields.add(result);
	}

	public boolean areOffsetsComputed() {
		return this.offsetsComputed;
	}

	public int size() {
		checkNotMutable();
		return this.size;
	}

	void checkNotMutable() {
		if (!this.offsetsComputed) {
			throw new IllegalStateException("Must call done() before using the struct"); //$NON-NLS-1$
		}
	}

	private void checkMutable() {
		if (this.doneCalled) {
			throw new IllegalStateException("May not modify a StructDef after done() has been called"); //$NON-NLS-1$
		}
	}

	/**
	 * Invoked on all StructDef after both {@link #done()} has been called on the struct and
	 * {@link #computeOffsets()} has been called on every dependency of this struct.
	 */
	private void computeOffsets() {
		int offset = this.superClass == null ? 0 : this.superClass.size();

		for (IField next : this.fields) {
			offset = MathUtils.roundUpToNearestMultiple(offset, next.getAlignment());
			next.setOffset(offset);
			offset += next.getRecordSize();
		}

		this.size = offset;
		if (this.refCounted) {
			this.deletionSemantics = DeletionSemantics.REFCOUNTED;
		} else {
			if (!this.ownerFields.isEmpty()) {
				this.deletionSemantics = DeletionSemantics.OWNED;
			} else if (this.superClass != null) {
				this.deletionSemantics = this.superClass.deletionSemantics;
			} else {
				this.deletionSemantics = DeletionSemantics.EXPLICIT;
			}
		}
		// Now verify that the deletion semantics of this struct are compatible with the deletion
		// semantics of its superclass
		if (this.superClass != null && this.deletionSemantics != this.superClass.deletionSemantics) {
			if (this.superClass.deletionSemantics != DeletionSemantics.EXPLICIT) {
				throw new IllegalStateException("A class (" + this.clazz.getName() + ") that uses "  //$NON-NLS-1$//$NON-NLS-2$
					+ this.deletionSemantics.toString() + " deletion semantics may not inherit from a class " //$NON-NLS-1$
					+ "that uses " + this.superClass.deletionSemantics.toString() + " semantics");  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		
		this.offsetsComputed = true;

		for (StructDef<? extends T> next : this.dependents) {
			if (next.doneCalled) {
				next.computeOffsets();
			}
		}
	}

	public FieldPointer addPointer() {
		FieldPointer result = new FieldPointer(getStructName(), this.fields.size());
		add(result);
		return result;
	}

	public FieldShort addShort() {
		FieldShort result = new FieldShort(getStructName(), this.fields.size());
		add(result);
		return result;
	}

	public FieldInt addInt() {
		FieldInt result = new FieldInt(getStructName(), this.fields.size());
		add(result);
		return result;
	}

	public FieldLong addLong() {
		FieldLong result = new FieldLong(getStructName(), this.fields.size());
		add(result);
		return result;
	}

	public FieldString addString() {
		FieldString result = new FieldString(getStructName(), this.fields.size());
		add(result);
		addDestructableField(result);
		return result;
	}

	public FieldDouble addDouble() {
		FieldDouble result = new FieldDouble(getStructName(), this.fields.size());
		add(result);
		return result;
	}

	public FieldFloat addFloat() {
		FieldFloat result = new FieldFloat(getStructName(), this.fields.size());
		add(result);
		return result;
	}

	public String getStructName() {
		return this.clazz.getSimpleName();
	}

	public FieldByte addByte() {
		FieldByte result = new FieldByte(getStructName(), this.fields.size());
		add(result);
		return result;
	}

	public FieldChar addChar() {
		FieldChar result = new FieldChar(getStructName(), this.fields.size());
		add(result);
		return result;
	}

	public <F> Field<F> add(ITypeFactory<F> factory1) {
		Field<F> result = new Field<>(factory1, getStructName(), this.fields.size());
		add(result);
		if (result.factory.hasDestructor()) {
			this.destructableFields.add(result);
		}
		return result;
	}

	public ITypeFactory<T> getFactory() {
		return this.factory;
	}

	void destructFields(Nd dom, long address) {
		for (IDestructableField next : StructDef.this.destructableFields) {
			next.destruct(dom, address);
		}

		if (this.superClass != null) {
			this.superClass.destructFields(dom, address);
		}
	}

	public boolean isNdNode() {
		return this.isNdNode;
	}

	public int getNumFields() {
		return this.fields.size();
	}
}
