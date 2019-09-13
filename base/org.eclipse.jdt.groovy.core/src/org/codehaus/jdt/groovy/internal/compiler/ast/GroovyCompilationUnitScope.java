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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.builder.NameEnvironment;

/**
 * A subtype of CompilationUnitScope that allows us to override some methods and
 * prevents JDT doing some checks that groovy will be doing anyway (or that JDT
 * should be prevented from doing on groovy type declarations).
 */
public class GroovyCompilationUnitScope extends CompilationUnitScope {

    private static final char[][] javaLang = TypeConstants.JAVA_LANG;
    // Matches ResolveVisitor - these are the additional automatic imports for groovy files
    private static final char[][] javaIo = CharOperation.splitOn('.', "java.io".toCharArray());
    private static final char[][] javaNet = CharOperation.splitOn('.', "java.net".toCharArray());
    private static final char[][] javaUtil = CharOperation.splitOn('.', "java.util".toCharArray());
    private static final char[][] groovyLang = CharOperation.splitOn('.', "groovy.lang".toCharArray());
    private static final char[][] groovyUtil = CharOperation.splitOn('.', "groovy.util".toCharArray());

    private static final char[][] javaMathBigDecimal = CharOperation.splitOn('.', "java.math.BigDecimal".toCharArray());
    private static final char[][] javaMathBigInteger = CharOperation.splitOn('.', "java.math.BigInteger".toCharArray());
    /*   */ static final char[][] GROOVY_LANG_METACLASS = CharOperation.splitOn('.', "groovy.lang.MetaClass".toCharArray());
    /*   */ static final char[][] GROOVY_LANG_GROOVYOBJECT = CharOperation.splitOn('.', "groovy.lang.GroovyObject".toCharArray());

    public GroovyCompilationUnitScope(GroovyCompilationUnitDeclaration compilationUnitDeclaration, LookupEnvironment lookupEnvironment) {
        super(compilationUnitDeclaration, lookupEnvironment);
        if (lookupEnvironment.nameEnvironment instanceof NameEnvironment) {
            ((NameEnvironment) lookupEnvironment.nameEnvironment).avoidAdditionalGroovyAnswers = true;
        }
    }

    private boolean isScript;

    public boolean isScript() {
        return isScript;
    }

    public void setIsScript(boolean isScript) {
        this.isScript = isScript;
    }

    private ImportBinding[] defaultGroovyImports;

    @Override
    protected ImportBinding[] getDefaultImports() {
        if (defaultGroovyImports == null) {
            List<ImportBinding> importBindings = new ArrayList<>(8);
            Collections.addAll(importBindings, super.getDefaultImports()); // picks up 'java.lang'

            // augment with the Groovy on-demand imports
            importBindings.add(new ImportBinding(javaIo, true, environment.getPackage(javaIo, module()), null));
            importBindings.add(new ImportBinding(javaNet, true, environment.getPackage(javaNet, module()), null));
            importBindings.add(1, new ImportBinding(javaUtil, true, environment.getPackage(javaUtil, module()), null));
            importBindings.add(new ImportBinding(groovyLang, true, environment.getPackage(groovyLang, module()), null));
            importBindings.add(new ImportBinding(groovyUtil, true, environment.getPackage(groovyUtil, module()), null));

            // and specific imports for BigDecimal and BigInteger
            importBindings.add(new ImportBinding(javaMathBigDecimal, false, environment.getType(javaMathBigDecimal), null));
            importBindings.add(new ImportBinding(javaMathBigInteger, false, environment.getType(javaMathBigInteger), null));

            /* See https://github.com/groovy/groovy-eclipse/issues/256 and https://issues.apache.org/jira/browse/GROOVY-8063
             *
             * @interface Anno { Class value() }
             *
             * @Anno(value=Inner) // Inner cannot be resolved -- I think this is correct behavior; below enables resolution
             * class Outer {
             *   static class Inner {}
             * }
             */
            //for (SourceTypeBinding topLevelType : topLevelTypes) {
            //    if (topLevelType.hasMemberTypes()) // add synthetic import to help resolve inner types
            //        importBindings.add(new ImportBinding(topLevelType.compoundName, true, topLevelType, null));
            //}

            defaultGroovyImports = importBindings.toArray(new ImportBinding[importBindings.size()]);
        }
        return defaultGroovyImports;
    }

    @Override
    protected ClassScope buildClassScope(Scope parent, TypeDeclaration typeDecl) {
        return new GroovyClassScope(parent, typeDecl);
    }

    @Override
    protected void buildTypeBindings(AccessRestriction accessRestriction) {
        super.buildTypeBindings(accessRestriction);
    }

    /**
     * Ensures Groovy types implement {@code groovy.lang.GroovyObject}.
     */
    @Override
    public void augmentTypeHierarchy() {
        for (SourceTypeBinding topLevelType : topLevelTypes) {
            if (!topLevelType.isInterface() && !topLevelType.isAnnotationType() && topLevelType.superInterfaces != null) {
                CompilationUnitScope unitScope = compilationUnitScope();
                unitScope.recordQualifiedReference(GROOVY_LANG_GROOVYOBJECT);
                ReferenceBinding groovyLangObjectBinding = unitScope.environment.getResolvedType(GROOVY_LANG_GROOVYOBJECT, this);
                if (!topLevelType.implementsInterface(groovyLangObjectBinding, true)) {
                    topLevelType.superInterfaces = (ReferenceBinding[]) ArrayUtils.add(topLevelType.superInterfaces, groovyLangObjectBinding);
                }
            }
        }
    }

    @Override
    protected void checkPublicTypeNameMatchesFilename(TypeDeclaration typeDecl) {
    }

    @Override
    protected boolean reportPackageIsNotExpectedPackage(CompilationUnitDeclaration compUnitDecl) {
        // check for parser recovery of an incomplete package statement or a script with no package statement before reporting name mismatch
        char[][] declaredPackage = (compUnitDecl.currentPackage != null ? compUnitDecl.currentPackage.tokens : CharOperation.NO_CHAR_CHAR);
        if (!CharOperation.equals(declaredPackage, new char[][] {new char[] {'?'}}) && !(declaredPackage.length == 0 && isScript)) {
            problemReporter().packageIsNotExpectedPackage(compUnitDecl);
            return true;
        }
        return false;
    }

    @Override
    protected void checkParameterizedTypes() {
    }

    @Override
    public boolean reportInvalidType(TypeReference typeReference, TypeBinding resolvedType) {
        if (resolvedType instanceof ProblemReferenceBinding) {
            ProblemReferenceBinding problemRefBinding = (ProblemReferenceBinding) resolvedType;
            if (problemRefBinding.problemId() == ProblemReasons.Ambiguous) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void reportImportProblem(ImportReference importReference, Binding importBinding) {
        // no eclipse 'not found' imports for groovy types (in case grab satisfies them)
        if (importBinding instanceof ProblemReferenceBinding &&
                ((ProblemReferenceBinding) importBinding).problemId() == ProblemReasons.NotFound) {
            return;
        }
        super.reportImportProblem(importReference, importBinding);
    }

    @Override
    public boolean canSeeEverything() {
        return true;
    }

    @Override
    public boolean checkTargetCompatibility() {
        return false;
    }

    @Override
    protected boolean canBeSeenBy(ReferenceBinding type, PackageBinding fPackage) {
        return true;
    }

    @Override
    public boolean scannerAvailable() {
        return false;
    }

    /**
     * This method is designed to be called when two bindings have been discovered, it will determine which is the right answer or
     * return null if there is no right answer (and an ambiguous binding message will be reported). If in here it means two star
     * imports have found a type. One might be a groovy.util style input (i.e. a 'built in' import), and one a 'normal' import that
     * was actually expressed in the source code. Whether the newly found type was discovered via an import expressed in the import
     * is determined by the 'isDeclaredImport' flag. If that is true we just have to check whether the originally found type uses
     * one of the special names. If the original type doesn't use a 'special name' then we allow it to override the newly found
     * value and return it.<br>
     * This code does not yet allow for the originallyFound import to be also found via a declared import (e.g. if the user is daft
     * enough to 'import groovy.util.*' - making a change to pass that information through would be more disruptive.
     *
     * @param newlyFound the binding found after the first one was discovered
     * @param originallyFound the binding found initially
     * @param isDeclaredImport indicates if the 'temp' binding was found using a real import from the source code (rather than an
     *        'injected' one)
     */
    @Override
    public ReferenceBinding selectBinding(ReferenceBinding newlyFound, ReferenceBinding originallyFound, boolean isDeclaredImport) {
        if (isDeclaredImport) {
            // This means 'temp' is found via a real import reference in the source code, let's take a closer look at 'type'
            if (originallyFound.fPackage != null) {
                char[][] packageName = originallyFound.fPackage.compoundName;
                if (CharOperation.equals(javaLang,   packageName) ||
                    CharOperation.equals(javaUtil,   packageName) ||
                    CharOperation.equals(javaIo,     packageName) ||
                    CharOperation.equals(javaNet,    packageName) ||
                    CharOperation.equals(groovyLang, packageName) ||
                    CharOperation.equals(groovyUtil, packageName)) {

                    return newlyFound;
                } else {
                    // Groovy rule: if the originally found one is via a declared import, which it must be
                    // if we are here, use it in preference to the newlyFound one.
                    return originallyFound;
                }
            }
        }
        return null;
    }

    @Override
    public void verifyMethods(MethodVerifier verifier) {
        if (!verified) {
            synchronized (this) {
                if (!verified) {
                    verified = true;
                    super.verifyMethods(verifier);
                }
            }
        }
    }
    private volatile boolean verified;
}
