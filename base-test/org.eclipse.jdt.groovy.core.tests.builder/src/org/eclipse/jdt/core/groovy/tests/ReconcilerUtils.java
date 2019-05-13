/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.groovy.core.util.JavaConstants;

/**
 * Some utility methods to test the reconciler.
 */
public class ReconcilerUtils {

    private ReconcilerUtils() {}

    //--------------------------------------------------------------------------

    public static Set<IProblem> reconcile(ICompilationUnit unit) throws JavaModelException {
        unit.becomeWorkingCopy(null);
        try {
            SimpleWorkingCopyOwner owner = new SimpleWorkingCopyOwner();
            SimpleProgressMonitor monitor = new SimpleProgressMonitor("reconcile");
            unit.reconcile(JavaConstants.AST_LEVEL, true, owner, monitor);
            monitor.waitForCompletion();
            return owner.problems;
        } finally {
            unit.discardWorkingCopy();
        }
    }

    public static ReconcileResults reconcileAllCompilationUnits(IJavaProject project, boolean onlyGroovy) throws JavaModelException {
        List<ICompilationUnit> allUnits = findAllUnits(project, onlyGroovy);
        ReconcileResults results = new ReconcileResults();
        for (ICompilationUnit unit : allUnits) {
            long startTime = System.currentTimeMillis();
            // this implicitly performs a reconcile
            unit.becomeWorkingCopy(null);
            long timeForUnit = System.currentTimeMillis() - startTime;
            results.reconcileTimes.put(unit, timeForUnit);
            unit.discardWorkingCopy();
        }
        return results;
    }

    private static List<ICompilationUnit> findAllUnits(IJavaProject project, boolean onlyGroovy) throws JavaModelException {
        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
        List<ICompilationUnit> units = new ArrayList<>();
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

    public static ICompilationUnit findCompilationUnit(IJavaProject project, String name) throws JavaModelException {
        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
        for (IPackageFragmentRoot root : roots) {
            if (!root.isReadOnly()) {
                for (IJavaElement child : root.getChildren()) {
                    if (child instanceof IPackageFragment) {
                        ICompilationUnit[] theseUnits = ((IPackageFragment) child).getCompilationUnits();
                        for (ICompilationUnit unit : theseUnits) {
                            if (unit.getResource().getName().equals(name)) {
                                return unit;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------

    public static class ReconcileResults {
        public final Map<ICompilationUnit, Long> reconcileTimes = new LinkedHashMap<>();

        public long getReconcileTime(ICompilationUnit unit) {
            return reconcileTimes.get(unit);
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("Reconcile times for " + reconcileTimes.size() + " units\n");
            long totaltime = 0L;
            for (Map.Entry<ICompilationUnit, Long> entry : reconcileTimes.entrySet()) {
                s.append(entry.getValue() + "ms " + entry.getKey().getElementName() + "\n");
                totaltime += entry.getValue();
            }
            s.append("Total time spent reconciling: " + totaltime + "ms\n");
            return s.toString();
        }

        public long getTotalTimeSpentReconciling() {
            long totaltime = 0L;
            for (Map.Entry<ICompilationUnit, Long> entry : reconcileTimes.entrySet()) {
                totaltime += entry.getValue();
            }
            return totaltime;
        }
    }

    public static class SimpleWorkingCopyOwner extends WorkingCopyOwner {
        public final Set<IProblem> problems = new LinkedHashSet<>();
        @Override
        public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
            return new IProblemRequestor() {
                @Override
                public void acceptProblem(IProblem problem) {
                    problems.add(problem);
                }

                @Override
                public void beginReporting() {
                }

                @Override
                public void endReporting() {
                }

                @Override
                public boolean isActive() {
                    return true;
                }
            };
        }
    }
}
