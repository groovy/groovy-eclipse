/*
 * Copyright 2009-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.ui.console;

import java.util.Calendar;
import java.util.Date;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.IGroovyLogger;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.internal.console.ScrollLockAction;
import org.eclipse.ui.part.IPageSite;

public class GroovyConsolePage extends TextConsolePage implements IGroovyLogger {

    private ScrollLockAction fScrollLockAction;

    private CloseConsoleAction fCloseConsoleAction;

    public GroovyConsolePage(TextConsole console, IConsoleView view) {
        super(console, view);
    }

    private static String twoDigit(int i) {
        String number = Integer.toString(i);
        if (number.length() < 2) {
            return "0" + number;
        } else {
            return number;
        }
    }

    @Override
    public void log(final TraceCategory category, String message) {
        Control control = getControl();
        if (control == null) return;

        Display display = control.getDisplay();
        if (display == null) return;

        /*
         * This code no longer dependent on either java.util.DateFormat, nor its ICU4J
         * version, while avoiding the deprecated methods in java.util.Date, hence the
         * slightly convoluted manner of extracting the time from the given date.
         *
         * -spyoung
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        StringBuilder buffer = new StringBuilder();
        buffer.append(category.getPaddedLabel()).append(" : ");
        buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY))).append(":");
        buffer.append(twoDigit(calendar.get(Calendar.MINUTE))).append(":");
        buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
        buffer.append(" ").append(message).append("\n");
        String string = buffer.toString();

        display.asyncExec(() -> {
            TextConsoleViewer viewer = getViewer();
            if (viewer != null) {
                StyledText text = viewer.getTextWidget();
                text.append(string);
                if (!fScrollLockAction.isChecked()) {
                    text.setTopIndex(text.getLineCount() - 1);
                }
            }
        });
    }

    /**
     * always returns true. Maybe later add capability to
     * disable log categories
     */
    @Override
    public boolean isCategoryEnabled(TraceCategory category) {
        return true;
    }

    @Override
    public void init(IPageSite pageSite) throws PartInitException {
        super.init(pageSite);
        GroovyLogManager.manager.addLogger(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fScrollLockAction != null) {
            fScrollLockAction.dispose();
            fScrollLockAction = null;
        }
        fCloseConsoleAction = null;
        GroovyLogManager.manager.removeLogger(this);
    }

    @Override
    protected TextConsoleViewer createViewer(Composite parent) {
        TextConsoleViewer viewer = new TextConsoleViewer(parent, (GroovyConsole) getConsole());
        viewer.setEditable(false);
        return viewer;
    }

    @Override
    protected void createActions() {
        super.createActions();
        fScrollLockAction = new ScrollLockAction(getConsoleView());
        fCloseConsoleAction = new CloseConsoleAction(getConsole());
        setAutoScroll(!fScrollLockAction.isChecked());
    }

    public void setAutoScroll(boolean scroll) {
        TextConsoleViewer viewer = getViewer();
        if (viewer != null) {
            fScrollLockAction.setChecked(!scroll);
        }
    }

    @Override
    protected void configureToolBar(IToolBarManager mgr) {
        super.configureToolBar(mgr);
        mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fScrollLockAction);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fCloseConsoleAction);
    }
}
