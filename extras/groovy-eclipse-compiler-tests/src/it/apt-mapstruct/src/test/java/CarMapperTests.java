import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class CarMapperTests {
    @Test
    public void testCarToCarDTO() {
        Car car = new Car();
        car.setId(1);
        car.setName("Ford Focus");

        CarDTO dto = CarMapper.INSTANCE.carToCarDTO(car);

        assertEquals(dto.getId(), 1);
        assertEquals(dto.getName(), "Ford Focus");
    }
}
