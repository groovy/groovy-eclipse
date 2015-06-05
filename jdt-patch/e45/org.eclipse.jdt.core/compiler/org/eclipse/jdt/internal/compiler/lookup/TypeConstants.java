/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 358903 - Filter practically unimportant resource leak warnings
 *								bug 381445 - [compiler][resource] Can the resource leak check be made aware of Closeables.closeQuietly?
 *								bug 400421 - [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
 *								bug 382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
 *								Bug 405569 - Resource leak check false positive when using DbUtils.closeQuietly
 *								Bug 427199 - [1.8][resource] avoid resource leak warnings on Streams that have no resource
 *								Bug 425183 - [1.8][inference] make CaptureBinding18 safe
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *    Jesper S Moller - Contributions for
 *								Bug 405066 - [1.8][compiler][codegen] Implement code generation infrastructure for JSR335
 *								Bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *    Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                              Bug 405104 - [1.8][compiler][codegen] Implement support for serializeable lambdas
 *    Ulrich Grave <ulrich.grave@gmx.de> - Contributions for
 *                              bug 386692 - Missing "unused" warning on "autowired" fields
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

// TODO should rename into TypeNames (once extracted last non name constants)
public interface TypeConstants {

	char[] JAVA = "java".toCharArray(); //$NON-NLS-1$
	char[] JAVAX = "javax".toCharArray(); //$NON-NLS-1$
	char[] LANG = "lang".toCharArray(); //$NON-NLS-1$
	char[] IO = "io".toCharArray(); //$NON-NLS-1$
	char[] UTIL = "util".toCharArray(); //$NON-NLS-1$
	char[] ZIP = "zip".toCharArray(); //$NON-NLS-1$
	char[] ANNOTATION = "annotation".toCharArray(); //$NON-NLS-1$
	char[] REFLECT = "reflect".toCharArray(); //$NON-NLS-1$
	char[] LENGTH = "length".toCharArray(); //$NON-NLS-1$
	char[] CLONE = "clone".toCharArray(); //$NON-NLS-1$
	char[] EQUALS = "equals".toCharArray(); //$NON-NLS-1$
	char[] GETCLASS = "getClass".toCharArray(); //$NON-NLS-1$
	char[] HASHCODE = "hashCode".toCharArray(); //$NON-NLS-1$
	char[] OBJECT = "Object".toCharArray(); //$NON-NLS-1$
	char[] MAIN = "main".toCharArray(); //$NON-NLS-1$
	char[] SERIALVERSIONUID = "serialVersionUID".toCharArray(); //$NON-NLS-1$
	char[] SERIALPERSISTENTFIELDS = "serialPersistentFields".toCharArray(); //$NON-NLS-1$
	char[] READRESOLVE = "readResolve".toCharArray(); //$NON-NLS-1$
	char[] WRITEREPLACE = "writeReplace".toCharArray(); //$NON-NLS-1$
	char[] READOBJECT = "readObject".toCharArray(); //$NON-NLS-1$
	char[] WRITEOBJECT = "writeObject".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_LANG_OBJECT = "java.lang.Object".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_LANG_ENUM = "java.lang.Enum".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_LANG_ANNOTATION_ANNOTATION = "java.lang.annotation.Annotation".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_IO_OBJECTINPUTSTREAM = "java.io.ObjectInputStream".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_IO_OBJECTOUTPUTSTREAM = "java.io.ObjectOutputStream".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_IO_OBJECTSTREAMFIELD = "java.io.ObjectStreamField".toCharArray(); //$NON-NLS-1$
	char[] ANONYM_PREFIX = "new ".toCharArray(); //$NON-NLS-1$
	char[] ANONYM_SUFFIX = "(){}".toCharArray(); //$NON-NLS-1$
    char[] WILDCARD_NAME = { '?' };
    char[] WILDCARD_SUPER = " super ".toCharArray(); //$NON-NLS-1$
    char[] WILDCARD_EXTENDS = " extends ".toCharArray(); //$NON-NLS-1$
    char[] WILDCARD_MINUS = { '-' };
    char[] WILDCARD_STAR = { '*' };
    char[] WILDCARD_PLUS = { '+' };
    char[] WILDCARD_CAPTURE_NAME_PREFIX = "capture#".toCharArray(); //$NON-NLS-1$
    char[] WILDCARD_CAPTURE_NAME_SUFFIX = "-of ".toCharArray(); //$NON-NLS-1$
    char[] WILDCARD_CAPTURE_SIGNABLE_NAME_SUFFIX = "capture-of ".toCharArray(); //$NON-NLS-1$
	char[] WILDCARD_CAPTURE = { '!' };
	char[] CAPTURE18 = { '^' };
	char[] BYTE = "byte".toCharArray(); //$NON-NLS-1$
	char[] SHORT = "short".toCharArray(); //$NON-NLS-1$
	char[] INT = "int".toCharArray(); //$NON-NLS-1$
	char[] LONG = "long".toCharArray(); //$NON-NLS-1$
	char[] FLOAT = "float".toCharArray(); //$NON-NLS-1$
	char[] DOUBLE = "double".toCharArray(); //$NON-NLS-1$
	char[] CHAR = "char".toCharArray(); //$NON-NLS-1$
	char[] BOOLEAN = "boolean".toCharArray(); //$NON-NLS-1$
	char[] NULL = "null".toCharArray(); //$NON-NLS-1$
	char[] VOID = "void".toCharArray(); //$NON-NLS-1$
    char[] VALUE = "value".toCharArray(); //$NON-NLS-1$
    char[] VALUES = "values".toCharArray(); //$NON-NLS-1$
    char[] VALUEOF = "valueOf".toCharArray(); //$NON-NLS-1$
    char[] UPPER_SOURCE = "SOURCE".toCharArray(); //$NON-NLS-1$
    char[] UPPER_CLASS = "CLASS".toCharArray(); //$NON-NLS-1$
    char[] UPPER_RUNTIME = "RUNTIME".toCharArray(); //$NON-NLS-1$
	char[] ANNOTATION_PREFIX = "@".toCharArray(); //$NON-NLS-1$
	char[] ANNOTATION_SUFFIX = "()".toCharArray(); //$NON-NLS-1$
    char[] TYPE = "TYPE".toCharArray(); //$NON-NLS-1$
    char[] UPPER_FIELD = "FIELD".toCharArray(); //$NON-NLS-1$
    char[] UPPER_METHOD = "METHOD".toCharArray(); //$NON-NLS-1$
    char[] UPPER_PARAMETER = "PARAMETER".toCharArray(); //$NON-NLS-1$
    char[] UPPER_CONSTRUCTOR = "CONSTRUCTOR".toCharArray(); //$NON-NLS-1$
    char[] UPPER_LOCAL_VARIABLE = "LOCAL_VARIABLE".toCharArray(); //$NON-NLS-1$
    char[] UPPER_ANNOTATION_TYPE = "ANNOTATION_TYPE".toCharArray(); //$NON-NLS-1$
    char[] UPPER_PACKAGE = "PACKAGE".toCharArray(); //$NON-NLS-1$
    char[] ANONYMOUS_METHOD = "lambda$".toCharArray(); //$NON-NLS-1$
    char[] DESERIALIZE_LAMBDA = "$deserializeLambda$".toCharArray(); //$NON-NLS-1$
    char[] LAMBDA_TYPE = "<lambda>".toCharArray(); //$NON-NLS-1$
    
	// jsr308
	char[] TYPE_USE_TARGET  = "TYPE_USE".toCharArray(); //$NON-NLS-1$
	char[] TYPE_PARAMETER_TARGET = "TYPE_PARAMETER".toCharArray(); //$NON-NLS-1$
    
    // common 3rd party package components:
    char[] ORG = "org".toCharArray(); //$NON-NLS-1$
    char[] ECLIPSE = "eclipse".toCharArray(); //$NON-NLS-1$
    char[] CORE = "core".toCharArray(); //$NON-NLS-1$
    char[] RUNTIME = "runtime".toCharArray(); //$NON-NLS-1$
    char[] APACHE = "apache".toCharArray(); //$NON-NLS-1$
    char[] COMMONS = "commons".toCharArray(); //$NON-NLS-1$
    char[] LANG3 = "lang3".toCharArray(); //$NON-NLS-1$
    char[] COM = "com".toCharArray(); //$NON-NLS-1$
    char[] GOOGLE = "google".toCharArray(); //$NON-NLS-1$
    char[] JDT = "jdt".toCharArray(); //$NON-NLS-1$
    char[] INTERNAL = "internal".toCharArray(); //$NON-NLS-1$
    char[] COMPILER = "compiler".toCharArray(); //$NON-NLS-1$
    char[] LOOKUP = "lookup".toCharArray(); //$NON-NLS-1$
    char[] TYPEBINDING = "TypeBinding".toCharArray(); //$NON-NLS-1$
    char[] DOM = "dom".toCharArray(); //$NON-NLS-1$
    char[] ITYPEBINDING = "ITypeBinding".toCharArray(); //$NON-NLS-1$
    char[] SPRING = "springframework".toCharArray(); //$NON-NLS-1$
    
	// Constant compound names
	char[][] JAVA_LANG = {JAVA, LANG};
	char[][] JAVA_IO = {JAVA, IO};
	char[][] JAVA_LANG_ANNOTATION_ANNOTATION = {JAVA, LANG, ANNOTATION, "Annotation".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ASSERTIONERROR = {JAVA, LANG, "AssertionError".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_CLASS = {JAVA, LANG, "Class".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_CLASSNOTFOUNDEXCEPTION = {JAVA, LANG, "ClassNotFoundException".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_CLONEABLE = {JAVA, LANG, "Cloneable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ENUM = {JAVA, LANG, "Enum".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_EXCEPTION = {JAVA, LANG, "Exception".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ERROR = {JAVA, LANG, "Error".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ILLEGALARGUMENTEXCEPTION = {JAVA, LANG, "IllegalArgumentException".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ITERABLE = {JAVA, LANG, "Iterable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_NOCLASSDEFERROR = {JAVA, LANG, "NoClassDefError".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_OBJECT = {JAVA, LANG, OBJECT};
	char[][] JAVA_LANG_STRING = {JAVA, LANG, "String".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_STRINGBUFFER = {JAVA, LANG, "StringBuffer".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_STRINGBUILDER = {JAVA, LANG, "StringBuilder".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_SYSTEM = {JAVA, LANG, "System".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_RUNTIMEEXCEPTION = {JAVA, LANG, "RuntimeException".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_THROWABLE = {JAVA, LANG, "Throwable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_REFLECT_CONSTRUCTOR = {JAVA, LANG, REFLECT, "Constructor".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_IO_PRINTSTREAM = {JAVA, IO, "PrintStream".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_IO_SERIALIZABLE = {JAVA, IO, "Serializable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_BYTE = {JAVA, LANG, "Byte".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_SHORT = {JAVA, LANG, "Short".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_CHARACTER = {JAVA, LANG, "Character".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_INTEGER = {JAVA, LANG, "Integer".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_LONG = {JAVA, LANG, "Long".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_FLOAT = {JAVA, LANG, "Float".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_DOUBLE = {JAVA, LANG, "Double".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_BOOLEAN = {JAVA, LANG, "Boolean".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_VOID = {JAVA, LANG, "Void".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_UTIL_COLLECTION = {JAVA, UTIL, "Collection".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_UTIL_ITERATOR = {JAVA, UTIL, "Iterator".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_UTIL_OBJECTS = {JAVA, UTIL, "Objects".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_DEPRECATED = {JAVA, LANG, "Deprecated".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_DOCUMENTED = {JAVA, LANG, ANNOTATION, "Documented".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_INHERITED = {JAVA, LANG, ANNOTATION, "Inherited".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_REPEATABLE = {JAVA, LANG, ANNOTATION, "Repeatable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_OVERRIDE = {JAVA, LANG, "Override".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_FUNCTIONAL_INTERFACE = {JAVA, LANG, "FunctionalInterface".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_RETENTION = {JAVA, LANG, ANNOTATION, "Retention".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_SUPPRESSWARNINGS = {JAVA, LANG, "SuppressWarnings".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_TARGET = {JAVA, LANG, ANNOTATION, "Target".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_RETENTIONPOLICY = {JAVA, LANG, ANNOTATION, "RetentionPolicy".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_ELEMENTTYPE = {JAVA, LANG, ANNOTATION, "ElementType".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_REFLECT_FIELD = new char[][] {JAVA, LANG, REFLECT, "Field".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_REFLECT_METHOD = new char[][] {JAVA, LANG, REFLECT, "Method".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_IO_CLOSEABLE = new char[][] { JAVA, IO, "Closeable".toCharArray()};//$NON-NLS-1$
	char[][] JAVA_IO_OBJECTSTREAMEXCEPTION = new char[][] { JAVA, IO, "ObjectStreamException".toCharArray()};//$NON-NLS-1$
	char[][] JAVA_IO_EXTERNALIZABLE = {JAVA, IO, "Externalizable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_IO_IOEXCEPTION = new char[][] { JAVA, IO, "IOException".toCharArray()};//$NON-NLS-1$
	char[][] JAVA_IO_OBJECTOUTPUTSTREAM = new char[][] { JAVA, IO, "ObjectOutputStream".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_IO_OBJECTINPUTSTREAM = new char[][] { JAVA, IO, "ObjectInputStream".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_NIO_FILE_FILES = new char[][] { JAVA, "nio".toCharArray(), "file".toCharArray(), "Files".toCharArray() };   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	// javax.rmi.CORBA.Stub
	char[][] JAVAX_RMI_CORBA_STUB = new char[][] {
			JAVAX,
			"rmi".toCharArray(), //$NON-NLS-1$
			"CORBA".toCharArray(), //$NON-NLS-1$
			"Stub".toCharArray(), //$NON-NLS-1$
	};
	char[][] JAVA_LANG_SAFEVARARGS =  {JAVA, LANG, "SafeVarargs".toCharArray()}; //$NON-NLS-1$
	char[] INVOKE = "invoke".toCharArray(); //$NON-NLS-1$
	char[][] JAVA_LANG_INVOKE_METHODHANDLE_POLYMORPHICSIGNATURE = { // Signature while parsing binary file
			JAVA,
			LANG,
			INVOKE,
			"MethodHandle".toCharArray(), //$NON-NLS-1$
			"PolymorphicSignature".toCharArray() //$NON-NLS-1$
	};
	char[][] JAVA_LANG_INVOKE_METHODHANDLE_$_POLYMORPHICSIGNATURE = { // Signature while parsing source file
			JAVA,
			LANG,
			INVOKE,
			"MethodHandle$PolymorphicSignature".toCharArray() //$NON-NLS-1$
	};
	char[][] JAVA_LANG_INVOKE_LAMBDAMETAFACTORY = {JAVA, LANG, INVOKE, "LambdaMetafactory".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_INVOKE_SERIALIZEDLAMBDA = {JAVA, LANG, INVOKE, "SerializedLambda".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_INVOKE_METHODHANDLES = {JAVA, LANG, INVOKE, "MethodHandles".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_AUTOCLOSEABLE =  {JAVA, LANG, "AutoCloseable".toCharArray()}; //$NON-NLS-1$
	char[] CLOSE = "close".toCharArray(); //$NON-NLS-1$
	// known helper functions for closing a Closeable (all receive a Closeable as their first argument):
	public static class CloseMethodRecord {
		public char[][] typeName;
		public char[] selector;
		public int numCloseableArgs;
		public CloseMethodRecord(char[][] typeName, char[] selector, int num) {
			this.typeName = typeName;
			this.selector = selector;
			this.numCloseableArgs = num;
		}
	}
	char[][] GUAVA_CLOSEABLES = { COM, GOOGLE, "common".toCharArray(), IO, "Closeables".toCharArray() }; //$NON-NLS-1$ //$NON-NLS-2$
	char[][] APACHE_IOUTILS = { ORG, APACHE, COMMONS, IO, "IOUtils".toCharArray() }; //$NON-NLS-1$
	char[][] APACHE_DBUTILS = { ORG, APACHE, COMMONS, "dbutils".toCharArray(), "DbUtils".toCharArray() }; //$NON-NLS-1$ //$NON-NLS-2$
	char[] CLOSE_QUIETLY = "closeQuietly".toCharArray(); //$NON-NLS-1$
	CloseMethodRecord[] closeMethods = new CloseMethodRecord[] {
		new CloseMethodRecord(GUAVA_CLOSEABLES, CLOSE_QUIETLY, 1),
		new CloseMethodRecord(GUAVA_CLOSEABLES, CLOSE, 1),
		new CloseMethodRecord(APACHE_IOUTILS, CLOSE_QUIETLY, 1),
		new CloseMethodRecord(APACHE_DBUTILS, CLOSE, 1),
		new CloseMethodRecord(APACHE_DBUTILS, CLOSE_QUIETLY, 3), // closeQuietly(Connection,Statement,ResultSet) 
		new CloseMethodRecord(APACHE_DBUTILS, "commitAndClose".toCharArray(), 1), //$NON-NLS-1$
		new CloseMethodRecord(APACHE_DBUTILS, "commitAndCloseQuietly".toCharArray(), 1), //$NON-NLS-1$
		new CloseMethodRecord(APACHE_DBUTILS, "rollbackAndClose".toCharArray(), 1), //$NON-NLS-1$
		new CloseMethodRecord(APACHE_DBUTILS, "rollbackAndCloseQuietly".toCharArray(), 1), //$NON-NLS-1$
	};
	// white lists of closeables:
	char[][] JAVA_IO_WRAPPER_CLOSEABLES = new char[][] {
		"BufferedInputStream".toCharArray(), //$NON-NLS-1$
		"BufferedOutputStream".toCharArray(), //$NON-NLS-1$
		"BufferedReader".toCharArray(), //$NON-NLS-1$
		"BufferedWriter".toCharArray(), //$NON-NLS-1$
		"InputStreamReader".toCharArray(), //$NON-NLS-1$
		"PrintWriter".toCharArray(),  //$NON-NLS-1$
		"LineNumberReader".toCharArray(), //$NON-NLS-1$
		"DataInputStream".toCharArray(), //$NON-NLS-1$
		"DataOutputStream".toCharArray(), //$NON-NLS-1$
		"ObjectInputStream".toCharArray(), //$NON-NLS-1$
		"ObjectOutputStream".toCharArray(), //$NON-NLS-1$
		"FilterInputStream".toCharArray(), //$NON-NLS-1$
		"FilterOutputStream".toCharArray(), //$NON-NLS-1$
		"DataInputStream".toCharArray(), //$NON-NLS-1$
		"DataOutputStream".toCharArray(), //$NON-NLS-1$
		"PushbackInputStream".toCharArray(), //$NON-NLS-1$
		"SequenceInputStream".toCharArray(), //$NON-NLS-1$
		"PrintStream".toCharArray(), //$NON-NLS-1$
		"PushbackReader".toCharArray(), //$NON-NLS-1$
		"OutputStreamWriter".toCharArray(), //$NON-NLS-1$
	};
	char[][] JAVA_UTIL_ZIP_WRAPPER_CLOSEABLES = new char[][] {
		"GZIPInputStream".toCharArray(), //$NON-NLS-1$
		"InflaterInputStream".toCharArray(), //$NON-NLS-1$
		"DeflaterInputStream".toCharArray(), //$NON-NLS-1$
		"CheckedInputStream".toCharArray(), //$NON-NLS-1$
		"ZipInputStream".toCharArray(), //$NON-NLS-1$
		"JarInputStream".toCharArray(), //$NON-NLS-1$
		"GZIPOutputStream".toCharArray(), //$NON-NLS-1$
		"InflaterOutputStream".toCharArray(), //$NON-NLS-1$
		"DeflaterOutputStream".toCharArray(), //$NON-NLS-1$
		"CheckedOutputStream".toCharArray(), //$NON-NLS-1$
		"ZipOutputStream".toCharArray(), //$NON-NLS-1$
		"JarOutputStream".toCharArray(), //$NON-NLS-1$
	};
	char[][][] OTHER_WRAPPER_CLOSEABLES = new char[][][] {
		{JAVA, "security".toCharArray(), "DigestInputStream".toCharArray()}, //$NON-NLS-1$ //$NON-NLS-2$
		{JAVA, "security".toCharArray(), "DigestOutputStream".toCharArray()}, //$NON-NLS-1$ //$NON-NLS-2$
		{JAVA, "beans".toCharArray(), "XMLEncoder".toCharArray()}, //$NON-NLS-1$ //$NON-NLS-2$
		{JAVA, "beans".toCharArray(), "XMLDecoder".toCharArray()}, //$NON-NLS-1$ //$NON-NLS-2$
		{JAVAX, "sound".toCharArray(), "sampled".toCharArray(), "AudioInputStream".toCharArray()}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	};
	char[][] JAVA_IO_RESOURCE_FREE_CLOSEABLES = new char[][] {			
		"StringReader".toCharArray(), //$NON-NLS-1$
		"StringWriter".toCharArray(), //$NON-NLS-1$
		"ByteArrayInputStream".toCharArray(), //$NON-NLS-1$
		"ByteArrayOutputStream".toCharArray(), //$NON-NLS-1$
		"CharArrayReader".toCharArray(), //$NON-NLS-1$
		"CharArrayWriter".toCharArray(), //$NON-NLS-1$
		"StringBufferInputStream".toCharArray(), //$NON-NLS-1$
	};
	char[][] RESOURCE_FREE_CLOSEABLE_STREAM = new char[][] {
		JAVA, UTIL, "stream".toCharArray(), "Stream".toCharArray() //$NON-NLS-1$ //$NON-NLS-2$
	};
	
	// different assertion utilities:
	char[] ASSERT_CLASS = "Assert".toCharArray(); //$NON-NLS-1$
	char[][] ORG_ECLIPSE_CORE_RUNTIME_ASSERT = new char[][] { ORG, ECLIPSE, CORE, RUNTIME, ASSERT_CLASS };
	// ... methods:
	char[] IS_NOTNULL = "isNotNull".toCharArray(); //$NON-NLS-1$
	
	char[] JUNIT = "junit".toCharArray(); //$NON-NLS-1$
	char[] FRAMEWORK = "framework".toCharArray(); //$NON-NLS-1$
	char[][] JUNIT_FRAMEWORK_ASSERT = new char[][] { JUNIT, FRAMEWORK, ASSERT_CLASS };
	char[][] ORG_JUNIT_ASSERT = new char[][] { ORG, JUNIT, ASSERT_CLASS };
	// ... methods:
	char[] ASSERT_NULL = "assertNull".toCharArray(); //$NON-NLS-1$
	char[] ASSERT_NOTNULL = "assertNotNull".toCharArray(); //$NON-NLS-1$
	char[] ASSERT_TRUE = "assertTrue".toCharArray(); //$NON-NLS-1$
	char[] ASSERT_FALSE = "assertFalse".toCharArray(); //$NON-NLS-1$
	
	char[] VALIDATE_CLASS = "Validate".toCharArray(); //$NON-NLS-1$
	char[][] ORG_APACHE_COMMONS_LANG_VALIDATE = new char[][] { ORG, APACHE, COMMONS, LANG, VALIDATE_CLASS };
	char[][] ORG_APACHE_COMMONS_LANG3_VALIDATE = new char[][] { ORG, APACHE, COMMONS, LANG3, VALIDATE_CLASS };
	char[][] ORG_ECLIPSE_JDT_INTERNAL_COMPILER_LOOKUP_TYPEBINDING = new char[][] { ORG, ECLIPSE, JDT, INTERNAL, COMPILER, LOOKUP, TYPEBINDING };
	char[][] ORG_ECLIPSE_JDT_CORE_DOM_ITYPEBINDING = new char[][] { ORG, ECLIPSE, JDT, CORE, DOM, ITYPEBINDING };

	// ... methods:
	char[] IS_TRUE = "isTrue".toCharArray(); //$NON-NLS-1$
	char[] NOT_NULL = "notNull".toCharArray(); //$NON-NLS-1$
	
	char[][] COM_GOOGLE_COMMON_BASE_PRECONDITIONS = new char[][] { 
			COM, GOOGLE, "common".toCharArray(), "base".toCharArray(), "Preconditions".toCharArray() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// ... methods:
	char[] CHECK_NOT_NULL = "checkNotNull".toCharArray(); //$NON-NLS-1$
	char[] CHECK_ARGUMENT = "checkArgument".toCharArray(); //$NON-NLS-1$
	char[] CHECK_STATE = "checkState".toCharArray(); //$NON-NLS-1$

	// ... methods in java.util.Objects:
	char[] REQUIRE_NON_NULL = "requireNonNull".toCharArray(); //$NON-NLS-1$
	
	// different @Inject annotations are relevant for @NonNull fields
	char[] INJECT_PACKAGE = "inject".toCharArray(); //$NON-NLS-1$
	char[] INJECT_TYPE = "Inject".toCharArray(); //$NON-NLS-1$
	char[][] JAVAX_ANNOTATION_INJECT_INJECT = new char[][] { JAVAX, INJECT_PACKAGE, INJECT_TYPE };
	char[][] COM_GOOGLE_INJECT_INJECT = new char[][] {COM, GOOGLE, INJECT_PACKAGE, INJECT_TYPE };
	//    detail for the above:
	char[] OPTIONAL = "optional".toCharArray(); //$NON-NLS-1$

	// Spring @Autowired annotation
	char [] AUTOWIRED = "Autowired".toCharArray();  //$NON-NLS-1$
	char [] BEANS = "beans".toCharArray();  //$NON-NLS-1$
	char [] FACTORY = "factory".toCharArray(); //$NON-NLS-1$
	char[][] ORG_SPRING_AUTOWIRED = new char[][] {ORG, SPRING, BEANS, FACTORY, ANNOTATION, AUTOWIRED};
	char[] REQUIRED = "required".toCharArray(); //$NON-NLS-1$

	// Constraints for generic type argument inference
	int CONSTRAINT_EQUAL = 0;		// Actual = Formal
	int CONSTRAINT_EXTENDS = 1;	// Actual << Formal
	int CONSTRAINT_SUPER = 2;		// Actual >> Formal

	// Constants used to perform bound checks
	int OK = 0;
	int UNCHECKED = 1;
	int MISMATCH = 2;

	// Synthetics
	char[] INIT = "<init>".toCharArray(); //$NON-NLS-1$
	char[] CLINIT = "<clinit>".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_SWITCH_ENUM_TABLE = "$SWITCH_TABLE$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ENUM_VALUES = "ENUM$VALUES".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ASSERT_DISABLED = "$assertionsDisabled".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_CLASS = "class$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_OUTER_LOCAL_PREFIX = "val$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ENCLOSING_INSTANCE_PREFIX = "this$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ACCESS_METHOD_PREFIX =  "access$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ENUM_CONSTANT_INITIALIZATION_METHOD_PREFIX =  " enum constant initialization$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_STATIC_FACTORY =  "<factory>".toCharArray(); //$NON-NLS-1$

	// constants in enum org.eclipse.jdt.annotation.DefaultLocation:
	char[] DEFAULT_LOCATION__PARAMETER = "PARAMETER".toCharArray(); //$NON-NLS-1$
	char[] DEFAULT_LOCATION__RETURN_TYPE = "RETURN_TYPE".toCharArray(); //$NON-NLS-1$
	char[] DEFAULT_LOCATION__FIELD = "FIELD".toCharArray(); //$NON-NLS-1$
	char[] DEFAULT_LOCATION__TYPE_ARGUMENT = "TYPE_ARGUMENT".toCharArray(); //$NON-NLS-1$
	char[] DEFAULT_LOCATION__TYPE_PARAMETER = "TYPE_PARAMETER".toCharArray(); //$NON-NLS-1$
	char[] DEFAULT_LOCATION__TYPE_BOUND = "TYPE_BOUND".toCharArray(); //$NON-NLS-1$
	char[] DEFAULT_LOCATION__ARRAY_CONTENTS = "ARRAY_CONTENTS".toCharArray(); //$NON-NLS-1$


	// synthetic package-info name
	public static final char[] PACKAGE_INFO_NAME = "package-info".toCharArray(); //$NON-NLS-1$
}
