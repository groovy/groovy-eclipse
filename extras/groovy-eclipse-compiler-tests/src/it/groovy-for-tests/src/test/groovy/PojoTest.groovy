import spock.lang.Specification

final class PojoTest extends Specification {

    def setup() {
        // per-test setup
    }

    void 'value property has default'() {
      given:
        Pojo pojo = new Pojo()

      expect:
        pojo.value == 0
        pojo.getValue() == 0
    }

    void 'value property is read/write'() {
      given:
        Pojo pojo = new Pojo()
        pojo.value = 1

      expect:
        pojo.value == 1
        pojo.getValue() == 1
    }
}
