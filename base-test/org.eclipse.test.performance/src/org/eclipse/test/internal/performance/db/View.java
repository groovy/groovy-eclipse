/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.test.internal.performance.db;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.eclipse.test.internal.performance.PerformanceTestPlugin;

/**
 * Dumps performance data to stdout.
 */
public class View {

    public static void main(String[] args) {

        Variations variations= PerformanceTestPlugin.getVariations();
        variations.put("config", "eclipseperfwin2_R3.3");  //$NON-NLS-1$//$NON-NLS-2$
        variations.put("build", "I200704%");  //$NON-NLS-1$//$NON-NLS-2$
        variations.put("jvm", "sun");  //$NON-NLS-1$//$NON-NLS-2$

		String scenarioPattern= "%RevertJavaEditorTest%"; //$NON-NLS-1$

        String seriesKey= PerformanceTestPlugin.BUILD;

        String outFile= null;
        // outfile= "/tmp/dbdump"; //$NON-NLS-1$
        PrintStream ps= null;
        if (outFile != null) {
            try {
                ps= new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
            } catch (FileNotFoundException e) {
                System.err.println("can't create output file"); //$NON-NLS-1$
            }
        }
        if (ps == null)
            ps= System.out;

        Scenario[] scenarios= DB.queryScenarios(variations, scenarioPattern, seriesKey, null);
        ps.println(scenarios.length + " Scenarios"); //$NON-NLS-1$
        ps.println();

        for (int s= 0; s < scenarios.length; s++)
            scenarios[s].dump(ps, PerformanceTestPlugin.BUILD);

        if (ps != System.out)
            ps.close();
    }
}
