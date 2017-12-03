import org.junit.Test

final class PogoTest {
    @Test
    void testBasics() {
        Pogo pogo = new Pogo(pattern: ~/abc/)
        assert pogo.pattern.pattern() == 'abc'
    }
}
