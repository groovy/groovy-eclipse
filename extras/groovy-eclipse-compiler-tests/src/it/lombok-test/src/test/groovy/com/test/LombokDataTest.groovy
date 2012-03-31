package com.test

import org.junit.Test

class LombokDataTest {
	@Test void simpleTest() {
		def obj = new LombokDataClass(1, 2)
		assert obj.a == 1
		assert obj.b == 2
	}
}
