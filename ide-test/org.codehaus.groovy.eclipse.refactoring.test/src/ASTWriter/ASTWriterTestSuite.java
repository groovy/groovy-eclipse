/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package ASTWriter;

import java.io.File;
import java.util.List;
import junit.framework.TestSuite;
import tests.BaseTestSuite;

public class ASTWriterTestSuite 
extends BaseTestSuite
{
    public static TestSuite suite()
    {
        final TestSuite ts = new TestSuite( "AST_Writer_Suite" );
        final List< File > files = getFileList( "/ASTWriterFiles", "AST_Writer_Test_" );
        for( final File file : files )
            ts.addTest( new ASTWriterTestCase( file.getName(), file ) );
        return ts;
    }
}
