Note:

The canonical version of this source folder must be in 
org.codehuas.groovy.frameworkadapter.  But a copy of this folder must be in 
org.codehaus.groovy.eclipse.core.  The reason is that o.c.g.fa is a bundle fragment.  
In a running OSGi instance, this fragment attaches to org.eclipse.osgi and all 
bundles that require o.e.o will automatically get this fragment on its classpath. 
However, at compile time the classes are not available.  This is probably a pde 
bug and I need to raise it, but haven't had a chance yet.  In the meantime, we
must keep a copy of this source folder in o.c.g.e.core.

The org.codehaus.groovy.eclipse.frameworkadapter.util package must be copied, 
but the bundle activator in org.codehaus.groovy.eclipse.frameworkadapter should 
not. 