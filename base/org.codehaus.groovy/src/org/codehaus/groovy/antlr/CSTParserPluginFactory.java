package org.codehaus.groovy.antlr;

import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.ParserPluginFactory;

/**
 * Plugin factory creating plugin that reports CST parsing.
 * 
 * @author empovazan
 */
public class CSTParserPluginFactory extends ParserPluginFactory {
	private ICSTReporter cstReporter;

	public CSTParserPluginFactory(ICSTReporter cstReporter) {
		this.cstReporter = cstReporter;
	}

	public ParserPlugin createParserPlugin() {
		return new CSTParserPlugin(cstReporter);
	}
}
