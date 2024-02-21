package academy.pojo;

import java.util.List;
import java.util.Map;

public class ProcessInstanceRequest {
    public List<Map<String, Object>> queries;

    public String processInstanceId;

    public String treePath;
    public long pageSize;

    public List<Map<String, Object>> getQueries() {
        return queries;
    }

    public void setQueries(List<Map<String, Object>> queries) {
        this.queries = queries;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getTreePath() {
        return treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }


}
