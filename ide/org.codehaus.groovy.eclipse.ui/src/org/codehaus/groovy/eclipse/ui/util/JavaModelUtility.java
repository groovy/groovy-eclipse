/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.ui.util;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This class exists to provide methods that were available in org.eclipse.jdt.internal.corext.util.JavaModelUtil,
 * but were discontinued as of Eclipse 3.4.  This is the risk you run using internal classes.
 * @author ervin
 *
 */
public class JavaModelUtility
{
    /**
     * Returns the fully qualified name of the given type using '.' as separators.
     * This is a replace for IType.getFullyQualifiedTypeName
     * which uses '$' as separators. As '$' is also a valid character in an id
     * this is ambiguous. JavaCore PR: 1GCFUNT
     */
    public static String getFullyQualifiedName( final IType type ) 
    {
        try 
        {
            if( type.isBinary() && !type.isAnonymous() ) 
            {
                final IType declaringType= type.getDeclaringType();
                if( declaringType != null ) 
                    return getFullyQualifiedName( declaringType ) + '.' + type.getElementName();
            }
        } 
        catch( final JavaModelException e ) {}
        return type.getFullyQualifiedName( '.' );
    }
    /** 
     * Finds a type by its qualified type name (dot separated).
     * @param jproject The java project to search in
     * @param fullyQualifiedName The fully qualified name (type name with enclosing type names and package (all separated by dots))
     * @return The type found, or null if not existing
     */ 
    public static IType findType( final IJavaProject jproject, 
                                  final String fullyQualifiedName ) 
    throws JavaModelException 
    {
        //workaround for bug 22883
        final IType type = jproject.findType( fullyQualifiedName );
        if( type != null )
            return type;
        final IPackageFragmentRoot[] roots= jproject.getPackageFragmentRoots();
        for( int i= 0; i < roots.length; i++ ) 
        {
            final IPackageFragmentRoot root= roots[ i ];
            final IType foundType= findType( root, fullyQualifiedName );
            if( foundType != null && foundType.exists() )
                return foundType;
        }   
        return null;
    }
    private static IType findType( final IPackageFragmentRoot root, 
                                   final String fullyQualifiedName ) 
    throws JavaModelException
    {
        final IJavaElement[] children = root.getChildren();
        for( int i = 0; i < children.length; i++ )
        {
            final IJavaElement element = children[ i ];
            if( element.getElementType() == IJavaElement.PACKAGE_FRAGMENT )
            {
                final IPackageFragment pack = ( IPackageFragment )element;
                if( !fullyQualifiedName.startsWith( pack.getElementName() ) )
                    continue;
                IType type = findType( pack, fullyQualifiedName );
                if( type != null && type.exists() )
                    return type;
            }
        }
        return null;
    }
    private static IType findType( final IPackageFragment pack, 
                                   final String fullyQualifiedName ) 
    throws JavaModelException
    {
        final ICompilationUnit[] cus = pack.getCompilationUnits();
        for( int i = 0; i < cus.length; i++ )
        {
            final ICompilationUnit unit = cus[ i ];
            final IType type = findType( unit, fullyQualifiedName );
            if( type != null && type.exists() )
                return type;
        }
        return null;
    }
    private static IType findType( final ICompilationUnit cu, 
                                   final String fullyQualifiedName ) 
    throws JavaModelException
    {
        final IType[] types = cu.getAllTypes();
        for( int i = 0; i < types.length; i++ )
        {
            final IType type = types[ i ];
            if( getFullyQualifiedName( type ).equals( fullyQualifiedName ) )
                return type;
        }
        return null;
    }
}
