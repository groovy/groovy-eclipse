import org.junit.Test

final class PogoTest {
    @Test
    void testBasics() {
        Pogo pogo = new Pogo()
        assert pogo.value == 0
        pogo.value = 1
        assert pogo.getValue() === 1
    }
}
