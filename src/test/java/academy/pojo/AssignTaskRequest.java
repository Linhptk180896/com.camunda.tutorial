package academy.pojo;

import java.util.List;
import java.util.Map;

public class AssignTaskRequest {
    public List<Map<String, Object>> variables;
    public String name;
    public String value;


    public List<Map<String, Object>> getVariables() {
        return variables;
    }

    public void setVariables(List<Map<String, Object>> variables) {
        this.variables = variables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
