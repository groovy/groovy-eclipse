 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.debug;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.groovy.eclipse.launchers.GroovyConsoleLineTracker;
import org.codehaus.groovy.eclipse.launchers.GroovyConsoleLineTracker.AmbiguousFileLink;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;

/**
 * @author Andrew Eisenberg
 *
 * Tests that breakpoint locations are as expected
 *
 */
public class ConsoleLineTrackerTests extends EclipseTestCase {
    MockConsole console;
    GroovyConsoleLineTracker lineTracker;
    IDocument doc;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        doc = new Document();
        console = new MockConsole(doc);
        lineTracker = new GroovyConsoleLineTracker();
        lineTracker.init(console);
        testProject.addNature(GroovyNature.GROOVY_NATURE);
    }
    
    public void testNoLink() throws Exception {
        testProject.createGroovyTypeAndPackage("f", "Bar.groovy", "");
        String contents = "ahdhjkfsfds";
        doc.set(contents);
        lineTracker.lineAppended(new Region(0, contents.length()));
        assertNull("Should not have found any hyperlinks", console.getLastLink());
    }
    public void testLink() throws Exception {
        testProject.createGroovyTypeAndPackage("f", "Bar.groovy", "");
        String contents = "at f.Bar.run(Bar.groovy:2)";
        doc.set(contents);
        lineTracker.lineAppended(new Region(0, contents.length()));
        assertNotNull("Should have found a hyperlink", console.getLastLink());
        FileLink link = (FileLink) console.getLastLink();
        IFile file = (IFile) ReflectionUtils.getPrivateField(FileLink.class, "fFile", link);
        assertTrue("File should exist", file.isAccessible());
        assertEquals("File name is wrong", "Bar.groovy", file.getName());
    }
    public void testAmbiguousLink() throws Exception {
        testProject.createGroovyTypeAndPackage("f", "Bar.groovy", "");
        testProject.createOtherSourceFolder();
        testProject.getProject().getFolder("other/f").create(true, true, null);
        testProject.getProject().getFile("other/f/Bar.groovy").create(new ByteArrayInputStream(new byte[0]), true, null);
        
        String contents = "at f.Bar.run(Bar.groovy:2)";
        doc.set(contents);
        lineTracker.lineAppended(new Region(0, contents.length()));
        assertNotNull("Should have found a hyperlink", console.getLastLink());
        FileLink link = (FileLink) console.getLastLink();
        Object file = ReflectionUtils.getPrivateField(FileLink.class, "fFile", link);
        assertNull("File should be null since the selection is ambiguous", file);
        
        IFile[] files = (IFile[]) ReflectionUtils.getPrivateField(AmbiguousFileLink.class, "files", link);
        
        assertEquals("Should have found 2 files", 2, files.length);
        assertEquals("File name is wrong", "Bar.groovy", files[0].getName());
        assertEquals("File name is wrong", "Bar.groovy", files[1].getName());
    }
    
}

class MockConsole implements IConsole {

    private Map<IHyperlink, IRegion> regionLinkMap = new HashMap<IHyperlink, IRegion>();
    private IHyperlink lastLink = null;

    private IDocument doc;
    
    public MockConsole(IDocument doc) {
        this.doc = doc;
    }
    
    public void addLink(IConsoleHyperlink link, int offset, int length) {
    }

    public void connect(IStreamsProxy streamsProxy) {
    }

    public void connect(IStreamMonitor streamMonitor, String streamIdentifer) {
    }

    public IDocument getDocument() {
        return doc;
    }

    public IProcess getProcess() {
        return null;
    }

    public IRegion getRegion(IConsoleHyperlink link) {
        return null;
    }

    public void addLink(IHyperlink link, int offset, int length) {
        regionLinkMap.put(link, new Region(offset, length));
        lastLink = link;
    }

    public void addPatternMatchListener(IPatternMatchListener matchListener) {
        
    }

    public IRegion getRegion(IHyperlink link) {
        return regionLinkMap.get(link);
    }
    
    public IHyperlink getLastLink() {
        return lastLink;
    }

    public IOConsoleOutputStream getStream(String streamIdentifier) {
        return null;
    }

    public void removePatternMatchListener(IPatternMatchListener matchListener) {
        
    }
    
}