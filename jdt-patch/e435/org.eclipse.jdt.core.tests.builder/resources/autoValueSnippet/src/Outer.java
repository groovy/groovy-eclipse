import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Outer {

	public static Outer create(Inner value) {
		return new AutoValue_Outer(value);
	}

	public abstract Inner inner();

	@AutoValue
	public abstract static class Inner {

		public static Inner create(String value) {
			return new AutoValue_Outer_Inner(value);
		}

		public abstract String value();

	}

}