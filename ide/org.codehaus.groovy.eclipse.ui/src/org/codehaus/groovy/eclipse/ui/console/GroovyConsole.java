/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.ui.console;

import java.util.Arrays;

import org.codehaus.groovy.eclipse.TraceCategory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

public class GroovyConsole extends TextConsole {

    public static final String CONSOLE_TYPE = "GroovyEventTraceConsole"; //$NON-NLS-1$

    private static final RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
    static {
        scanner.setPredicateRules(Arrays.stream(TraceCategory.values()).map(category ->
            new SingleLineRule(category.getPaddedLabel(), "", new Token(category.label))
        ).toArray(IPredicateRule[]::new));
    }

    private IConsoleDocumentPartitioner partitioner = new GroovyEventTraceConsolePartitioner();

    private IPropertyChangeListener propertyListener = event -> {
        String property = event.getProperty();
        if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT)) {
            setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
        }
    };

    public GroovyConsole() {
        super("Groovy Event Trace Console", CONSOLE_TYPE, null, true);
        setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
        partitioner.connect(getDocument());
    }

    @Override
    protected void init() {
        JFaceResources.getFontRegistry().addListener(propertyListener);
    }

    @Override
    protected void dispose() {
        JFaceResources.getFontRegistry().removeListener(propertyListener);
        super.dispose();
    }

    @Override
    public IPageBookViewPage createPage(IConsoleView view) {
        return new GroovyConsolePage(this, view);
    }

    @Override
    protected IConsoleDocumentPartitioner getPartitioner() {
        return partitioner;
    }

    //--------------------------------------------------------------------------

    /**
     * Provides a partitioner for this console type
     */
    private class GroovyEventTraceConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner {

        GroovyEventTraceConsolePartitioner() {
            super(scanner, TraceCategory.stringValues());
            getDocument().setDocumentPartitioner(this);
        }

        @Override
        public boolean isReadOnly(int offset) {
            return true;
        }

        @Override
        public StyleRange[] getStyleRanges(int offset, int length) {
            ColorRegistry registry = JFaceResources.getColorRegistry();
            return Arrays.stream(computePartitioning(offset, length)).map(ITypedRegion::getType).map(type -> {
                Color color;
                if (type.equals(TraceCategory.CLASSPATH.label)) {
                    color = registry.get(JFacePreferences.ERROR_COLOR);
                } else if (type.equals(TraceCategory.COMPILER.label) ||
                        type.equals(TraceCategory.AST_TRANSFORM.label)) {
                    color = registry.get(JFacePreferences.COUNTER_COLOR);
                } else if (type.equals(TraceCategory.CODE_SELECT.label) ||
                        type.equals(TraceCategory.CONTENT_ASSIST.label)) {
                    color = registry.get(JFacePreferences.DECORATIONS_COLOR);
                } else if (type.equals(TraceCategory.DSL.label)) {
                    color = Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA);
                } else {
                    color = registry.get(JFacePreferences.INFORMATION_FOREGROUND_COLOR);
                }
                return new StyleRange(offset, length, color, null);
            }).toArray(StyleRange[]::new);
        }
    }
}
