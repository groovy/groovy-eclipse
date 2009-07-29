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
import java.io.FileWriter;

class MainClass {
	static void main(String [] args){
		def tempFileName = args[0]
		FileWriter writer = new FileWriter(tempFileName)
		for (arg in args){writer.write(arg)}
		writer.write('the end')
		writer.close()
	}
}