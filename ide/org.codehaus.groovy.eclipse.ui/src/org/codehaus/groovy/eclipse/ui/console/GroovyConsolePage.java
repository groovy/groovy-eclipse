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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.IGroovyLogger;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.internal.console.ScrollLockAction;
import org.eclipse.ui.part.IPageSite;

/**
 *
 * @author andrew
 * @created Nov 24, 2010
 */
public class GroovyConsolePage extends TextConsolePage implements IGroovyLogger {

    private ScrollLockAction fScrollLockAction;

    private CloseConsoleAction fCloseConsoleAction;

    public GroovyConsolePage(TextConsole console, IConsoleView view) {
        super(console, view);
    }

    private String twodigit(int i) {
        String number = Integer.toString(i);
        if (number.length() < 2) {
            return new StringBuffer("0").append(number).toString();
        } else {
            return number.toString();
        }
    }

    public void log(final TraceCategory category, String message) {
        /*
         * This code no longer dependent on either java.util.DateFormat, nor its ICU4J
         * version, while avoiding the deprecated methods in java.util.Date, hence the
         * slightly convoluted manner of extracting the time from the given date.
         *
         * -spyoung
         */
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        StringBuffer time = new StringBuffer();
        time.append(twodigit(calendar.get(Calendar.HOUR_OF_DAY))).append(":");
        time.append(twodigit(calendar.get(Calendar.MINUTE))).append(":");
        time.append(twodigit(calendar.get(Calendar.SECOND)));
        time.append(" ").append(message).append("\n");
        final String txt = time.toString();
        //        final String txt = twodigit(calendar.get(Calendar.HOUR_OF_DAY)) + ":"  //$NON-NLS-1$
        //            + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + " " + message + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        this.getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                TextConsoleViewer viewer = getViewer();
                if (viewer != null) {
                    StyledText text = viewer.getTextWidget();
                    text.append(category.getPaddedLabel() + " : " + txt);
                    if (!fScrollLockAction.isChecked()) {
                        text.setTopIndex(text.getLineCount() - 1);
                    }
                }
            }
        });
    }

    /**
     * always returns true. Maybe later add capability to
     * disable log categories
     */
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
        TextConsoleViewer viewer = (TextConsoleViewer) getViewer();
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
