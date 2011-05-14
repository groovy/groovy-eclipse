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
package org.eclipse.jdt.core.groovy.tests.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Some utility methods to stress test the reconciler
 * @author Andrew Eisenberg
 * @author Andy Clement
 * @created May 13, 2011
 */
public class ReconcilerUtils {

    public static class ReconcileResults {
        public Map<ICompilationUnit, Long> reconcileTimes = new HashMap<ICompilationUnit, Long>();
        
        public long getReconcileTime(ICompilationUnit unit) {
            return reconcileTimes.get(unit);
        }
        
        public long totalTime;
        
        public String toString() {
        	StringBuilder s = new StringBuilder();
        	s.append("Reconcile times for "+reconcileTimes.size()+" units\n");
        	long totaltime = 0L;
        	for (Map.Entry<ICompilationUnit,Long> entry: reconcileTimes.entrySet()) {
        		s.append(entry.getValue()+"ms "+entry.getKey().getElementName()+"\n");
        		totaltime+=entry.getValue();
        	}
        	s.append("Total time spent reconciling: "+totaltime+"ms\n");
        	return s.toString();
        }
        
        public long getTotalTimeSpentReconciling() {
        	long totaltime = 0L;
        	for (Map.Entry<ICompilationUnit,Long> entry: reconcileTimes.entrySet()) {
        		totaltime+=entry.getValue();
        	}
        	return totaltime;
        }
    }

    public static ReconcileResults reconcileAllCompilationUnits(IJavaProject project, boolean onlyGroovy) throws JavaModelException {
        List<ICompilationUnit> allUnits = findAllUnits(project, onlyGroovy);
        ReconcileResults results = new ReconcileResults();
        long totalTime = 0;
        for (ICompilationUnit unit : allUnits) {
            long startTime = System.currentTimeMillis();
            // this implicitly performs a reconcile
            unit.becomeWorkingCopy(null);
            long timeForUnit = System.currentTimeMillis() - startTime;
            results.reconcileTimes.put(unit, timeForUnit);
            totalTime += timeForUnit;
            unit.discardWorkingCopy();
        }
        return results;
    }

    private static List<ICompilationUnit> findAllUnits(IJavaProject project,
            boolean onlyGroovy) throws JavaModelException {
        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
        List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
        for (IPackageFragmentRoot root : roots) {
            if (!root.isReadOnly()) {
                for (IJavaElement child : root.getChildren()) {
                    if (child instanceof IPackageFragment) {
                        ICompilationUnit[] theseUnits = ((IPackageFragment) child).getCompilationUnits();
                        for (ICompilationUnit unit : theseUnits) {
                            if (unit instanceof GroovyCompilationUnit || !onlyGroovy) {
                                units.add(unit);
                            }
                        }
                    }
                }
            }
        }
        return units;
    }
}
