package org.eclipse.jdt.groovy.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Stack;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * This class maps type parameter names to resolved types
 */
public class GenericsMapper {

	/**
	 * Stack keeps track of all type parameterization up the type hierarchy
	 */
	private Stack<Map<String, ClassNode>> allGenerics = new Stack<Map<String, ClassNode>>();

	/**
	 * Creates a mapper for a particular resolved type tracing up the type hierarchy until the declaring type is reached. This is
	 * the public entry point for this class.
	 * 
	 * @param resolvedType unredirected type that has generic types already parameterized
	 * @param declaringType a type that is somewhere in resolvedType's hierarchy used to find the target of the mapping
	 * @return
	 */
	public static GenericsMapper gatherGenerics(ClassNode resolvedType, ClassNode declaringType) {
		ClassNode ucandidate = resolvedType.redirect();
		ClassNode rcandidate = resolvedType;
		GenericsType[] ugts;
		GenericsType[] rgts;
		GenericsMapper mapper = new GenericsMapper();

		LinkedHashSet<ClassNode> uHierarchy = new LinkedHashSet<ClassNode>();
		VariableScope.createTypeHierarchy(ucandidate, uHierarchy, false);
		Iterator<ClassNode> uIter = uHierarchy.iterator();
		LinkedHashSet<ClassNode> rHierarchy = new LinkedHashSet<ClassNode>();
		VariableScope.createTypeHierarchy(rcandidate, rHierarchy, true);
		Iterator<ClassNode> rIter = rHierarchy.iterator();

		// travel up the hierarchy
		while (uIter.hasNext() && rIter.hasNext()) {
			ucandidate = uIter.next();
			rcandidate = rIter.next();

			ugts = ucandidate.getGenericsTypes();
			ugts = ugts == null ? VariableScope.NO_GENERICS : ugts;
			rgts = rcandidate.getGenericsTypes();
			rgts = rgts == null ? VariableScope.NO_GENERICS : rgts;

			HashMap<String, ClassNode> resolved = new HashMap<String, ClassNode>(2, 1.0f);
			// for each generics type add to list
			for (int i = 0; i < rgts.length && i < ugts.length; i++) {
				// now try to resolve the parameter in the context of the
				// most recently visited type. If it doesn't exist, then
				// default to the resovled type
				resolved.put(ugts[i].getName(), mapper.resolveParameter(rgts[i], 0));
			}

			mapper.allGenerics.push(resolved);
			if (rcandidate.getName().equals(declaringType.getName())) {
				// don't need to travel up the whole hierarchy. We can stop at the declaring class
				break;
			}
		}

		return mapper;
	}

	boolean hasGenerics() {
		return !allGenerics.isEmpty() && allGenerics.peek().size() > 0;
	}

	/**
	 * takes this type or type parameter and determines what its type should be based on the type parameter resolution in the top
	 * level of the mapper
	 * 
	 * @param topGT
	 * @param depth ensure that we don't recur forever, bottom out after a certain depth
	 * @return
	 */
	public ClassNode resolveParameter(GenericsType topGT, int depth) {
		if (depth > 10) {
			// don't recur forever
			// FIXADE This problem is believed fixed. If this conidtional is never reached, then
			// we should be able to delete this section.
			Util.log(new Status(IStatus.WARNING, "org.eclipse.jdt.groovy.core", //$NON-NLS-1$
					"GRECLIPSE-1040: prevent infinite recursion when resolving type parameters on generics type: " + topGT)); //$NON-NLS-1$
			return topGT.getType();
		}

		if (allGenerics.isEmpty()) {
			return topGT.getType();
		}

		ClassNode origType = findParameter(topGT.getName(), topGT.getType());

		// now recur down all type parameters inside of this type
		// class Enum<E extends Enum<E>>
		if (origType.getGenericsTypes() != null) {
			origType = VariableScope.clone(origType);
			GenericsType[] genericsTypes = origType.getGenericsTypes();
			for (GenericsType genericsType : genericsTypes) {
				if (genericsType.getName().equals(topGT.getName())) {
					// avoid infinite loops
					// I still don't like this solution, but better than using a depth counter.
					continue;
				}
				genericsType.setType(findParameter(genericsType.getName(), resolveParameter(genericsType, depth + 1)));
				genericsType.setLowerBound(null);
				genericsType.setUpperBounds(null);
				genericsType.setName(genericsType.getType().getName());
			}
		}
		return origType;
	}

	/**
	 * finds the type of a parameter name in the highest level of the type hierarchy currently analyzed
	 * 
	 * @param parameterName
	 * @param defaultType type to return if parameter name doesn't exist
	 * @return
	 */
	ClassNode findParameter(String parameterName, ClassNode defaultType) {
		if (allGenerics.isEmpty()) {
			return defaultType;
		}
		ClassNode type = allGenerics.peek().get(parameterName);
		if (type == null) {
			return defaultType;
		}
		return type;
	}

}