import org.immutables.value.Value;

@Value.Immutable(copy = false)
public interface Pojo {
    int getValue();
}
