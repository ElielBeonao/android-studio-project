package ca.uottawa.testnovigrad.fwk;

public class Filter {

    private String fieldName;
    private String operator;
    private Object value;

    public Filter(String fieldName, String operator, Object value) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
}
