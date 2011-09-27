import org.junit.Test
import org.junit.Assert

class GroovyTest {

	@Test
	void testMethod() {
		GroovyMain.main null
		Assert.assertTrue true
	}
}