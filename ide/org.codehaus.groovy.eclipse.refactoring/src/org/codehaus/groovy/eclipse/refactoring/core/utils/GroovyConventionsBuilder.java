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
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import groovyjarjarantlr.Token;
import groovyjarjarantlr.TokenStream;
import groovyjarjarantlr.TokenStreamException;

import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.eclipse.refactoring.Activator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Klenk Michael mklenk@hsr.ch
 * @author Kempf Martin
 *
 * Class used to verify user input (making sure that no keywords etc are being used)
 */
public final class GroovyConventionsBuilder {

	public static final String CLASS = "class";
	public static final String VARIABLE = "variable";
	public static final String METHOD = "method";
	public static final String FIELD = "field";

	private List<String> names = new ArrayList<String>();
	private String element;
	private MultiStatus state = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, "", null);

	public GroovyConventionsBuilder(String name, String element) {
		this.names.add(name);
		this.element = element;
		validateNotNull();
	}

	public GroovyConventionsBuilder(List<String> names, String element) {
		this.names = names;
		this.element = element;
		validateNotNull();
	}

	public GroovyConventionsBuilder validateUpperCase(int status) {
		//warning if lower case
		for (String name : names) {
			if (name.length() > 0 && Character.isLowerCase(name.charAt(0))) {
				state.add(new Status(status, Activator.PLUGIN_ID,
						MessageFormat.format("This name is discouraged. According to convention, names of ''{0}'' should start with an uppercase letter.",element)));
			}
		}
		return this;
	}

	public GroovyConventionsBuilder validateLowerCase(int status) {
		//warning if upper case
		for (String name : names) {
			if (name.length() > 0 && Character.isUpperCase(name.charAt(0))) {
				state.add(new Status(status, Activator.PLUGIN_ID,
						MessageFormat.format("This name is discouraged. According to convention, names of ''{0}'' should start with a lowercase letter.",element)));
			}
		}
		return this;
	}

	public GroovyConventionsBuilder validateGroovyIdentifier() {
		// Test if the first and only Token is an Identifier
		for (String name : names) {
			List<Token> tokenList = tokenizeString(name);
            if (!(tokenList.size() == 1 && tokenList.get(0).getType() == GroovyTokenTypeBridge.IDENT)) {
				state.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						MessageFormat.format("''{0}'' is not a valid Groovy identifier", name)));
			}
		}
		return this;
	}

	public IStatus done() {
		return state;
	}

	private void validateNotNull() {
		for (String name : names) {
			if(name.length() == 0) {
				state.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						MessageFormat.format("Provide a ''{0}'' name", element)));
			}
		}
	}

	public static List<Token> tokenizeString(String name) {
		Reader input = new StringReader(name);
		GroovyLexer lexer = new GroovyLexer(input);
		TokenStream stream = (TokenStream) lexer.plumb();

		List<Token> tokenList = new Vector<Token>();

		Token token = null;
		try {
			while ((token = stream.nextToken()).getType() != Token.EOF_TYPE) {
				tokenList.add(token);
			}
		} catch (TokenStreamException e) {
			e.printStackTrace();
		}

		return tokenList;
	}
}
