import org.immutables.value.Value;

@Value.Immutable(copy = false)
@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE)
public interface Pojo {
    int getValue();
}
