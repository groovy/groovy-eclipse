import org.junit.*;

public final class PojoTest {
    @Test
    public void testBasics() {
        Pojo pojo = new Pojo();
        Assert.assertEquals(null, pojo.getValue());
        pojo.setValue(" xx ");
        Assert.assertEquals("xx", pojo.getValue());
    }
}
