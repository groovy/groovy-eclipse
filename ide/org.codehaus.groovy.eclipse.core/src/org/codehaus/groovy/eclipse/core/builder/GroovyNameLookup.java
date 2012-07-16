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

package org.codehaus.groovy.eclipse.core.builder;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.IJavaElementRequestor;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Aug 18, 2009
 *
 * A NameLookup for Groovy classes to help look for non-tope level types and member types
 */
public class GroovyNameLookup extends NameLookup {


    public GroovyNameLookup(NameLookup other) {
        this(new IPackageFragmentRoot[0], new HashtableOfArrayToObject(), new ICompilationUnit[0], new HashMap());
        this.packageFragmentRoots = (IPackageFragmentRoot[]) ReflectionUtils.getPrivateField(NameLookup.class, "packageFragmentRoots", other);
        this.packageFragments = (HashtableOfArrayToObject) ReflectionUtils.getPrivateField(NameLookup.class, "packageFragments", other);
        this.typesInWorkingCopies = (HashMap) ReflectionUtils.getPrivateField(NameLookup.class, "typesInWorkingCopies", other);
        this.rootToResolvedEntries = (Map) ReflectionUtils.getPrivateField(NameLookup.class, "rootToResolvedEntries", other);
    }

    public GroovyNameLookup(IPackageFragmentRoot[] packageFragmentRoots,
            HashtableOfArrayToObject packageFragments,
            ICompilationUnit[] workingCopies, Map rootToResolvedEntries) {
        super(packageFragmentRoots, packageFragments, workingCopies,
                rootToResolvedEntries);
    }

    /**
     * Copied from parent class
     * Changes marked with // GROOVY begin and // GROOVY end
     */
    @Override
    protected void seekTypesInSourcePackage(
            String name,
            IPackageFragment pkg,
            int firstDot,
            boolean partialMatch,
            String topLevelTypeName,
            int acceptFlags,
            IJavaElementRequestor requestor) {

        long start = -1;
        if (VERBOSE)
            start = System.currentTimeMillis();
        try {
            if (!partialMatch) {
                try {
                    IJavaElement[] compilationUnits = pkg.getChildren();
                    for (int i = 0, length = compilationUnits.length; i < length; i++) {
                        if (requestor.isCanceled())
                            return;
                        // GROOVY begin
                        // removed statements that continue if type is not the same name as the compilation unit
                        ICompilationUnit cu = (ICompilationUnit) compilationUnits[i];
                        IType[] allTypes = cu.getAllTypes();
                        IType type = cu.getType(name);
                        if (
                                // GROOVY begin
                                type.exists() &&
                                // GROOVY end
                                acceptType(type, acceptFlags, true/*a source type*/)) { // accept type checks for existence
                            requestor.acceptType(type);
                            break;  // since an exact match was requested, no other matching type can exist
                        }

                        // now look for member types

                        String mainType = cu.getElementName();
                        int dotIndex = mainType.indexOf('.');
                        mainType = mainType.substring(0, dotIndex);
                        type = cu.getType(mainType);
                        if (type.exists()) {
                            type = getMemberType(type, name, firstDot);
                            if (
                                    // GROOVY begin
                                    type.exists() &&
                                    // GROOVY end
                                    acceptType(type, acceptFlags, true/*a source type*/)) { // accept type checks for existence
                                requestor.acceptType(type);
                                break;  // since an exact match was requested, no other matching type can exist
                            }
                        }
                        // GROOVY end
                    }
                } catch (JavaModelException e) {
                    // package doesn't exist -> ignore
                }
            } else {
                try {
                    String cuPrefix = firstDot == -1 ? name : name.substring(0, firstDot);
                    IJavaElement[] compilationUnits = pkg.getChildren();
                    for (int i = 0, length = compilationUnits.length; i < length; i++) {
                        if (requestor.isCanceled())
                            return;
                        IJavaElement cu = compilationUnits[i];
                        if (!cu.getElementName().toLowerCase().startsWith(cuPrefix))
                            continue;
                        try {
                            IType[] types = ((ICompilationUnit) cu).getTypes();
                            for (int j = 0, typeLength = types.length; j < typeLength; j++)
                                seekTypesInTopLevelType(name, firstDot, types[j], requestor, acceptFlags);
                        } catch (JavaModelException e) {
                            // cu doesn't exist -> ignore
                        }
                    }
                } catch (JavaModelException e) {
                    // package doesn't exist -> ignore
                }
            }
        } finally {
            if (VERBOSE)
                this.timeSpentInSeekTypesInSourcePackage += System.currentTimeMillis()-start;
        }
    }


    private IType getMemberType(IType type, String name, int dot) {
        type = type.getType(name);
        return type;
    }


    /**
     * Copied from parent class
     * Changes marked with // GROOVY begin and // GROOVY end
     */
    @Override
    protected void seekTypesInBinaryPackage(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor) {
        long start = -1;
        if (VERBOSE)
            start = System.currentTimeMillis();
        try {
            // GROOVY begin
            // ensure ends with .class
            if (!name.endsWith(".class")) {
                name += ".class";
            }
            // GROOVY end
            if (!partialMatch) {
                // exact match
                if (requestor.isCanceled()) return;
                ClassFile classFile =  (ClassFile) pkg.getClassFile(name);
                if (classFile.existsUsingJarTypeCache()) {
                    IType type = classFile.getType();
                    if (acceptType(type, acceptFlags, false/*not a source type*/)) {
                        requestor.acceptType(type);
                    }
                }

                // GROOVY begin
                // class file may still exist as an inner type
                IJavaElement[] classFiles= null;
                try {
                    classFiles= pkg.getChildren();
                } catch (JavaModelException npe) {
                    return; // the package is not present
                }
                for (IJavaElement elt : classFiles) {
                    classFile = (ClassFile) elt;
                    if (classFile.getElementName().endsWith("$" + name)) {
                        IType type = classFile.getType();
                        if (acceptType(type, acceptFlags, false/*not a source type*/)) {
                            requestor.acceptType(type);
                        }
                    }
                }
                // GROOVY end

            } else {
                IJavaElement[] classFiles= null;
                try {
                    classFiles= pkg.getChildren();
                } catch (JavaModelException npe) {
                    return; // the package is not present
                }
                int length= classFiles.length;
                String unqualifiedName = name;
                int index = name.lastIndexOf('$');
                if (index != -1) {
                    //the type name of the inner type
                    unqualifiedName = Util.localTypeName(name, index, name.length());
                    // unqualifiedName is empty if the name ends with a '$' sign.
                    // See http://dev.eclipse.org/bugs/show_bug.cgi?id=14642
                }
                int matchLength = name.length();
                for (int i = 0; i < length; i++) {
                    if (requestor.isCanceled())
                        return;
                    IJavaElement classFile= classFiles[i];
                    // MatchName will never have the extension ".class" and the elementName always will.
                    String elementName = classFile.getElementName();
                    if (elementName.regionMatches(true /*ignore case*/, 0, name, 0, matchLength)) {
                        IType type = ((ClassFile) classFile).getType();
                        String typeName = type.getElementName();
                        if (typeName.length() > 0 && !Character.isDigit(typeName.charAt(0))) { //not an anonymous type
                            if (nameMatches(unqualifiedName, type, true/*partial match*/) && acceptType(type, acceptFlags, false/*not a source type*/))
                                requestor.acceptType(type);
                        }
                    }
                }
            }
        } finally {
            if (VERBOSE)
                this.timeSpentInSeekTypesInBinaryPackage += System.currentTimeMillis()-start;
        }
    }

}
