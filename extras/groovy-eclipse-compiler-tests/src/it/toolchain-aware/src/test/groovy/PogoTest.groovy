import org.junit.Test

final class PogoTest {
    @Test
    void testBasics() {
        Pogo pogo = new Pogo()
        assert pogo.value == null
        pogo.value = ' xx '
        assert pogo.value == 'xx'
    }
}
