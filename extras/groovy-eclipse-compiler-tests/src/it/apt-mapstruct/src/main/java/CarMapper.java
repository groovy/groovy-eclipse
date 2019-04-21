import static org.mapstruct.factory.Mappers.getMapper;

@org.mapstruct.Mapper
public interface CarMapper {
    CarMapper INSTANCE = getMapper(CarMapper.class);

    CarDTO carToCarDTO(Car car);
}
