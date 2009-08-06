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