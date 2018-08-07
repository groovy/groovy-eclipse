/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	public final static int

	ERROR_SYMBOL = 128,
					MAX_NAME_LENGTH = 41,
					NUM_STATES = 1152,

					NT_OFFSET = 128,
					SCOPE_UBOUND = 290,
					SCOPE_SIZE = 291,
					LA_STATE_OFFSET = 16451,
					MAX_LA = 1,
					NUM_RULES = 851,
					NUM_TERMINALS = 128,
					NUM_NON_TERMINALS = 388,
					NUM_SYMBOLS = 516,
					START_STATE = 895,
					EOFT_SYMBOL = 60,
					EOLT_SYMBOL = 60,
					ACCEPT_ACTION = 16450,
					ERROR_ACTION = 16451;
}
