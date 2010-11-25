/*
 * Copyright 2003-2010 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.TraceCategory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;


/**
 *
 * @author andrew
 * @created Nov 24, 2010
 */
public class GroovyConsole extends TextConsole {

    public final static String CONSOLE_TYPE = "GroovyEventTraceConsole"; //$NON-NLS-1$

    final static RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
    {
        List<IPredicateRule> rules = new ArrayList<IPredicateRule>(TraceCategory.values().length);
        for (TraceCategory category : TraceCategory.values()) {
            rules.add(new SingleLineRule(category.getPaddedLabel(), "", new Token(category.label)));
        }
        scanner.setPredicateRules(rules.toArray(new IPredicateRule[0]));
    }

    /**
     * Provides a partitioner for this console type
     */
    class GroovyEventTraceConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner {

        public GroovyEventTraceConsolePartitioner() {
            super(scanner, TraceCategory.stringValues());
            getDocument().setDocumentPartitioner(this);
        }

        public boolean isReadOnly(int offset) {
            return true;
        }

        public StyleRange[] getStyleRanges(int offset, int length) {
            ITypedRegion regions[] = computePartitioning(offset, length);
            StyleRange[] styles = new StyleRange[regions.length];
            for (int i = 0; i < regions.length; i++) {
                if (TraceCategory.CLASSPATH.label.equals(regions[i].getType())) {
                    styles[i] = new StyleRange(offset, length, Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN), null);
                } else if (TraceCategory.COMPILER.label.equals(regions[i].getType())) {
                    styles[i] = new StyleRange(offset, length, Display.getDefault().getSystemColor(SWT.COLOR_BLUE), null);
                } else if (TraceCategory.DSL.label.equals(regions[i].getType())) {
                    styles[i] = new StyleRange(offset, length, Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED), null);
                } else if (TraceCategory.REFACTORING.label.equals(regions[i].getType())) {
                    styles[i] = new StyleRange(offset, length, Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW), null);
                } else {
                    styles[i] = new StyleRange(offset, length, null, null);
                }
            }
            return styles;
        }
    }

    public GroovyConsole(String name, String consoleType, ImageDescriptor imageDescriptor, boolean autoLifecycle) {
        super(name, consoleType, imageDescriptor, autoLifecycle);
    }

    private GroovyEventTraceConsolePartitioner partitioner = new GroovyEventTraceConsolePartitioner();

    private IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT)) {
                setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
            }
        }
    };

    public GroovyConsole() {
        super("Groovy Event Trace Console", CONSOLE_TYPE, null, true);
        Font font = JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT);
        setFont(font);
        partitioner.connect(getDocument());
    }

    /**
     * @see org.eclipse.ui.console.AbstractConsole#init()
     */
    @Override
    protected void init() {
        JFaceResources.getFontRegistry().addListener(propertyListener);
    }

    /**
     * @see org.eclipse.ui.console.TextConsole#dispose()
     */
    @Override
    protected void dispose() {
        JFaceResources.getFontRegistry().removeListener(propertyListener);
        super.dispose();
    }

    /**
     * @see org.eclipse.ui.console.TextConsole#getPartitioner()
     */
    @Override
    protected IConsoleDocumentPartitioner getPartitioner() {
        return partitioner;
    }

    /**
     * @see org.eclipse.ui.console.TextConsole#createPage(org.eclipse.ui.console.IConsoleView)
     */
    @Override
    public IPageBookViewPage createPage(IConsoleView view) {
        return new GroovyConsolePage(this, view);
    }
}
