public class Pojo {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = (value != null ? value.strip() : null);
    }
}
