package org.eclipse.jdt.core.tests.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the target test suite tests preview Java features. In certain cases
 * a test is not meant to be run with a future JRE. For e.g., if the highest supported
 * version by ECJ is 24, running certain tests, especially those related to preview
 * features, may produce unexpected results when run with a JRE 25. Authors of test
 * can mark such tests with this annotation and allow them to be skipped under
 * conditions such as above.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreviewTest {}