/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.core.utils

import org.codehaus.groovy.antlr.*;
import org.eclipse.jface.text.*;

/**
 * Reads parts of a file
 * @author reto kleeb
 */
class FilePartReader {
	
	private static String getLineInFile(filename, line){
		File fileObject = openFile(filename)
		fileObject.readLines().get(line-1)
	}
	
	private static getWords(line, range){
		def relevantPieceOfLine = line.getAt(range)
		relevantPieceOfLine = relevantPieceOfLine.replaceAll(/\(/, " ( ")
		relevantPieceOfLine = relevantPieceOfLine.replaceAll(/\)/, " ) ")
		relevantPieceOfLine.tokenize()
	}
	
	public static String readBackwardsFromCoordinate(IDocument doc, LineColumn coord){
		//make sure that no one reads from a file with impossible coordinates
		if(doc != null && coord.getLine() > 0 && coord.getColumn() > 0) {
			int lineLength = doc.getLineLength(coord.getLine()-1)
			int offset = doc.getLineOffset(coord.getLine()-1)
			String line = doc.get(offset, lineLength)
			return getWords(line,(0..coord.getColumn()-2))[-1]
		} else {
			return " "
		}
	}
	
	public static String readForwardFromCoordinate(IDocument doc, LineColumn coord){
		//make sure that no one reads from a file with impossible coordinates
		if(doc != null && coord.getLine() > 0 && coord.getColumn() > 0) {
			int lineLength = doc.getLineLength(coord.getLine()-1)
			int offset = doc.getLineOffset(coord.getLine()-1)
			String line = doc.get(offset, lineLength)
			return getWords(line,(coord.getColumn()-1..-1))[0]
		} else {
			return " "
		}
		
	}
	
	/*
	 * DEPRECATED, no filenames anymore
	public static String readBackwardsFromCoordinate(filename, Coord){
		def relevantLine = getLineInFile(filename, Coord.getLine())
		return getWords(relevantLine,(0..Coord.getColumn()) )[-1]
	}
	
	public static String readForwardFromCoordinate(filename, Coord){
		def relevantLine = getLineInFile(filename, Coord.getLine())
		return getWords(relevantLine,(Coord.getColumn()..-1))[0]
	}*/
	
	private static boolean validateCoords(Top, Bottom){
		if(Top.getLine() > Bottom.getLine()) {
			return false
		}
		if(Top.getLine() == Bottom.getLine()){
			if(Top.getColumn() >= Bottom.getColumn() ){
				return false
			}
		}
		return true
	}
	
	private static File openFile(filename){
		File fileObject = new File(filename);
		
		if(	! (fileObject.exists() && fileObject.isFile() && 
				fileObject.canRead() && !(fileObject.isDirectory()))){
				println "Error Reading File"
				return null
		}else{
			return fileObject
		}
	}
	
	public static String readPartsOfAFile(filename, Top, Bottom){
		
		if(!validateCoords(Top, Bottom)){
			return "IMPOSSIBLE COORDINATES"
		}
		
		int LineCounter = 0
		StringBuilder sb = new StringBuilder()
		
		try{
			File fileObject = openFile(filename)
			String LineDelimiter = getLineDelimiter(fileObject)
			
			int TopLine = Top.getLine()-1
			int BottomLine = Bottom.getLine()-1
			
			def LineArray = fileObject.readLines().getAt(TopLine..BottomLine)
	
			LineArray.each{line ->
				LineCounter++
				if(TopLine == BottomLine){
					return sb.append(line[Top.getColumn()..Bottom.getColumn()])
				}
				else if(LineCounter == 1){
					sb.append(line[Top.getColumn()..-1] + LineDelimiter)
				}
				else if(LineCounter == LineArray.size()){
					sb.append(line[0..Bottom.getColumn()] + LineDelimiter)
					return sb
				}
				else{
					sb.append(line + LineDelimiter)
				}
			}
		}	
		catch(Exception e){
			return "Couldn't read file contents at this position"
		}
		return sb.toString()
	
	}
	
	public static final String DEFAULT_LINE_DELIMITER = System.getProperty("line.separator");
	
	public static String getLineDelimiter(File file){

		def content = file.getText()
		def lineDelimiter = DEFAULT_LINE_DELIMITER
		int index = 0
		for (currentChar in content) {
			if(currentChar == '\r'){
				if(content[index+1] == '\n'){ 
					lineDelimiter = "\r\n"
					break
				}
				lineDelimiter =  "\r"		//mac os 9
				break
			}
			else if(currentChar == '\n'){
				lineDelimiter = "\n"
				break
			}
			index++;
		}
		return lineDelimiter;
	}
}