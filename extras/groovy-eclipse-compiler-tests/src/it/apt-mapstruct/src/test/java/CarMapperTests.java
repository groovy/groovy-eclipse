import org.junit.*;

public final class CarMapperTests {
    @Test
    public void testCarToCarDTO() {
        Car car = new Car();
        car.setId(1);
        car.setName("Ford Focus");

        CarDTO dto = CarMapper.INSTANCE.carToCarDTO(car);

        Assert.assertEquals(dto.getId(), 1);
        Assert.assertEquals(dto.getName(), "Ford Focus");
    }
}
