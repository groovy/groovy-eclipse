package org.codehaus.groovy.eclipse.maven.testing

@Singleton(lazy=false)
class WithNonStrictSingleton {
	
	WithNonStrictSingleton() {
		//this bad!
	}

}
