package com.test;

import org.junit.*;

public final class LombokDataTest {
    @Test
    public void testCodeGeneration() {
        LombokDataType obj = new LombokDataType(1, 2);
        Assert.assertEquals(1, obj.getOne());
        Assert.assertEquals(2, obj.getTwo());
    }
}
