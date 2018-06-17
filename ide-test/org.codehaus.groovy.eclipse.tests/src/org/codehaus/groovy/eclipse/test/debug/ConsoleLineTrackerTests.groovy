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
package org.codehaus.groovy.eclipse.test.debug

import groovy.transform.PackageScope

import org.codehaus.groovy.eclipse.launchers.GroovyConsoleLineTracker
import org.codehaus.groovy.eclipse.launchers.GroovyConsoleLineTracker.AmbiguousFileLink
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.core.resources.IFile
import org.eclipse.debug.core.model.IProcess
import org.eclipse.debug.core.model.IStreamMonitor
import org.eclipse.debug.core.model.IStreamsProxy
import org.eclipse.debug.ui.console.FileLink
import org.eclipse.debug.ui.console.IConsole
import org.eclipse.jdt.groovy.core.util.ReflectionUtils
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.eclipse.ui.console.IHyperlink
import org.eclipse.ui.console.IOConsoleOutputStream
import org.eclipse.ui.console.IPatternMatchListener
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Tests that breakpoint locations are as expected.
 */
final class ConsoleLineTrackerTests extends GroovyEclipseTestSuite {

    GroovyConsoleLineTracker lineTracker
    MockConsole console
    IDocument doc

    @Before
    void setUp() {
        doc = new Document()
        console = new MockConsole(doc)
        lineTracker = new GroovyConsoleLineTracker()
        lineTracker.init(console)
    }

    @Test
    void testNoLink() {
        addGroovySource('', 'Bar', 'f')
        String contents = 'ahdhjkfsfds'
        doc.set(contents)
        lineTracker.lineAppended(new Region(0, contents.length()))
        Assert.assertNull('Should not have found any hyperlinks', console.getLastLink())
    }

    @Test
    void testLink() {
        addGroovySource('', 'Bar', 'f')
        String contents = 'at f.Bar.run(Bar.groovy:2)'
        doc.set(contents)
        lineTracker.lineAppended(new Region(0, contents.length()))
        Assert.assertNotNull('Should have found a hyperlink', console.getLastLink())
        FileLink link = (FileLink) console.getLastLink()
        IFile file = ReflectionUtils.getPrivateField(FileLink.class, 'fFile', link)
        Assert.assertTrue('File should exist', file.isAccessible())
        Assert.assertEquals('File name is wrong', 'Bar.groovy', file.getName())
    }

    @Test
    void testAmbiguousLink() {
        addGroovySource('', 'Baz', 'f')
        addGroovySource('', 'Baz', 'f', addSourceFolder('other'))
        String contents = 'at f.Baz.run(Baz.groovy:2)'
        doc.set(contents)
        lineTracker.lineAppended(new Region(0, contents.length()))
        Assert.assertNotNull('Should have found a hyperlink', console.getLastLink())
        FileLink link = (FileLink) console.getLastLink()
        Object file = ReflectionUtils.getPrivateField(FileLink.class, 'fFile', link)
        Assert.assertNull('File should be null since the selection is ambiguous', file)

        IFile[] files = ReflectionUtils.getPrivateField(AmbiguousFileLink.class, 'files', link)

        Assert.assertEquals('Should have found 2 files', 2, files.length)
        Assert.assertEquals('File name is wrong', 'Baz.groovy', files[0].getName())
        Assert.assertEquals('File name is wrong', 'Baz.groovy', files[1].getName())
    }
}

@SuppressWarnings('deprecation')
@PackageScope class MockConsole implements IConsole {

    private Map<IHyperlink, IRegion> regionLinkMap = [:]
    private IHyperlink lastLink = null

    private IDocument doc

    MockConsole(IDocument doc) {
        this.doc = doc
    }

    void addLink(org.eclipse.debug.ui.console.IConsoleHyperlink link, int offset, int length) {
    }

    void connect(IStreamsProxy streamsProxy) {
    }

    void connect(IStreamMonitor streamMonitor, String streamIdentifer) {
    }

    IDocument getDocument() {
        return doc
    }

    IProcess getProcess() {
        return null
    }

    IRegion getRegion(org.eclipse.debug.ui.console.IConsoleHyperlink link) {
        return null
    }

    void addLink(IHyperlink link, int offset, int length) {
        regionLinkMap.put(link, new Region(offset, length))
        lastLink = link
    }

    void addPatternMatchListener(IPatternMatchListener matchListener) {
    }

    IRegion getRegion(IHyperlink link) {
        return regionLinkMap.get(link)
    }

    IHyperlink getLastLink() {
        return lastLink
    }

    IOConsoleOutputStream getStream(String streamIdentifier) {
        return null
    }

    void removePatternMatchListener(IPatternMatchListener matchListener) {
    }
}
