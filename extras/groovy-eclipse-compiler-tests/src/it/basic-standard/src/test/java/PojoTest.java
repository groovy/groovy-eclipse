import org.junit.*;

public final class PojoTest {
    @Test
    public void testBasics() {
        Pojo pojo = new Pojo();
        Assert.assertEquals(0, pojo.getValue());
        pojo.setValue(1);
        Assert.assertEquals(1, pojo.getValue());
    }
}
