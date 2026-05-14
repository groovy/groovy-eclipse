package ${package};

import org.junit.Assert;
import org.junit.Test;

public final class JavaTest {
	@Test
	public void testMethod() {
		JavaMain.main(new String[0]);
		Assert.assertTrue(true);
	}
}
