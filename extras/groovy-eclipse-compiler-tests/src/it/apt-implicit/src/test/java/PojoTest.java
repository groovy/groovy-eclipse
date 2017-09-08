import org.junit.*;

public final class PojoTest {
    @Test
    public void testBasics() {
        Pojo pojo = ImmutablePojo.builder().value(1).build();
        Assert.assertEquals(1, pojo.getValue());
    }
}
