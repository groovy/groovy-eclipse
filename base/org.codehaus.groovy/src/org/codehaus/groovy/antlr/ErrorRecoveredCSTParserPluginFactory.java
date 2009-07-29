package org.codehaus.groovy.antlr;

import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.ParserPluginFactory;

/**
 * Plugin factory creating plugin that reports on creation of error recovered ASTs.
 * 
 * @author empovazan
 */
public class ErrorRecoveredCSTParserPluginFactory extends ParserPluginFactory {
	private ICSTReporter cstReporter;

    public ErrorRecoveredCSTParserPluginFactory(ICSTReporter cstReporter) {
        this.cstReporter = cstReporter;
    }
    public ErrorRecoveredCSTParserPluginFactory() {
        this.cstReporter = null;
    }

	public ParserPlugin createParserPlugin() {
		return new ErrorRecoveredCSTParserPlugin(cstReporter);
	}
}