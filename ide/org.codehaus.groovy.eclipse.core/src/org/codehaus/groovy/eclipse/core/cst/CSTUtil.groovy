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
package org.codehaus.groovy.eclipse.core.cst
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.compiler.GroovyCompiler;
import org.codehaus.groovy.eclipse.core.compiler.GroovyCompilerConfiguration;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompilationReporter;
import org.codehaus.groovy.internal.antlr.parser.GroovyTokenTypes;
import org.eclipse.core.resources.IFile;
class CSTUtil 
implements IGroovyCompilationReporter 
{
	private def compiler = new GroovyCompiler()
	private GroovySourceAST cst  
	
	public void beginReporting(){}

	public void beginReporting(String fileName){}

	public void compilationError(String fileName, int line, int startCol, int endCol, String message, String stackTrace){}

	public void endReporting(){}

	public void endReporting(String fileName){}

	public void generatedAST(String fileName, ModuleNode moduleNode){}

	public void generatedCST(String fileName, GroovySourceAST newCst) {
		cst = newCst 
	}

	public void generatedClasses(String fileName, String[] classNames, String[] classFilePaths) {}

	GroovySourceAST getCST(IFile file){
		def config = new GroovyCompilerConfiguration()
		config.setBuildCST(true)
		compiler.compile(file.getLocation().toOSString(), config, this);
		return cst ; 
	}
	static def getObjBlock( iFile, type )
    {
        def cst = new CSTUtil().getCST( iFile )
		while( cst != null )
		{
		    def identifier = cst.childOfType( GroovyTokenTypes.IDENT )
		    if( type == "$identifier" )
		        break
		    cst = cst.nextSibling
		}
        if( !cst || !cst.childOfType( GroovyTokenTypes.OBJBLOCK ) )
            return null
        return cst.childOfType( GroovyTokenTypes.OBJBLOCK )
    }
}