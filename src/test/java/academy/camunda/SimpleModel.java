package academy.camunda;

import Utils.FileUtils;
import academy.handler.ServiceTaskHandler;
import academy.pojo.AssignTaskRequest;
import academy.pojo.GetTaskListRequest;
import academy.pojo.ProcessInstanceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.camunda.common.auth.*;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.json.Json;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.time.Duration;
import java.util.*;


//@ZeebeProcessTest
public class SimpleModel {

    private static final String ZEEBE_ADDRESS = "bd33d8d7-04e0-4484-9d49-3235ad899a99.ont-1.zeebe.camunda.io:443";
    private static final String ZEEBE_CLIENT_ID = "t8zhcNtRzIw4Nz~CopppLP6pzF9VgsQ5";
    private static final String ZEEBE_CLIENT_SECRET = "~R2sOdP0O.aESEYbxui5iMSYfHrtsrUcgejwIs_dkn5zUEd7qvUkUHfCf~0ufOpr";
    private static final String ZEEBE_AUTHORIZATION_SERVER_URL = "https://login.cloud.camunda.io/oauth/token";
    private static final String ZEEBE_TOKEN_AUDIENCE = "zeebe.camunda.io";

    private static final String CAMUNDA_CLUSTER_ID = "bd33d8d7-04e0-4484-9d49-3235ad899a99";
    private static final String CAMUNDA_CLUSTER_REGION = "ont-1";
    private static final String CAMUNDA_CREDENTIALS_SCOPES = "Zeebe,Operate,Optimize,Tasklist";
    private static final String CAMUNDA_TASKLIST_BASE_URL = "https://ont-1.tasklist.camunda.io/bd33d8d7-04e0-4484-9d49-3235ad899a99";
    private static final String CAMUNDA_OPTIMIZE_BASE_URL = "https://ont-1.optimize.camunda.io/bd33d8d7-04e0-4484-9d49-3235ad899a99";
    private static final String CAMUNDA_OPERATE_BASE_URL = "https://ont-1.operate.camunda.io/bd33d8d7-04e0-4484-9d49-3235ad899a99";
    private static final String CAMUNDA_OAUTH_URL = "https://login.cloud.camunda.io/oauth/token";

    private static long processInstanceKey;
    private static long processDefinitionKey;
    public static String operateAccessToken;
    public static String tasklistAccessToken;
    public static String username;


    @Test

    public void verify_that_token_goes_through_events() {
        final OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
                .authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL).audience(ZEEBE_TOKEN_AUDIENCE)
                .clientId(ZEEBE_CLIENT_ID).clientSecret(ZEEBE_CLIENT_SECRET).build();

        try (final ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress(ZEEBE_ADDRESS)
                .credentialsProvider(credentialsProvider)
                .build()) {
            System.out.println("Connected to: " + client.newTopologyRequest().send().join());

            final Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("name", "Linh");
            variables.put("age", "20");

            //Add a new Create Instance Command
            ProcessInstanceEvent processInstanceEvent = client.newCreateInstanceCommand()
                    .bpmnProcessId("simpleModel")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            processInstanceKey = processInstanceEvent.getProcessInstanceKey();
            processDefinitionKey = processInstanceEvent.getProcessDefinitionKey();
            System.out.println("processInstanceKey = " + processInstanceKey);
            System.out.println("processDefinitionKey = " + processDefinitionKey);

            // create a Job Worker using the Job Handler that was implemented in the previous section.
            // The Job Worker will process any task with the serviceTask type and will execute a Job Handler for that task.
            final JobWorker serviceTaskWorker =
                    client.newWorker()
                            .jobType("serviceTask1")
                            .handler(new ServiceTaskHandler())
                            .timeout(Duration.ofSeconds(10).toMillis())
                            .open();
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String tokenRequest;
        if (operateAccessToken == null || operateAccessToken.isEmpty()) {
            //Get Access Token
            tokenRequest = "tokenOperateRequest.json";
            Map<String, String> jsonFileData = FileUtils.getInstance().readJsonFileToMap(tokenRequest);
            Response accessTokenResponse = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(jsonFileData)
                    .post(CAMUNDA_OAUTH_URL);

            operateAccessToken = accessTokenResponse.jsonPath().getString("access_token");

            System.out.println("operateAccessToken = " + operateAccessToken);
        }

        //Read username from properties file
        Properties properties = new Properties();
        InputStream inputStream;
        try {
            String filePath = System.getProperty("user.dir");
            inputStream = new FileInputStream(filePath + "/src/test/resources/LOCAL.properties");
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        username = properties.getProperty("username");

        //Verify events that token goes through

        // Service task
        String requestBody;
        //Approach 1: Use String.format to get json
//        String processInstanceRequest = "processInstanceRequest.json";
////            Map<String, String> requestBody = FileUtils.getInstance().readJsonFileUsingInputStream(processInstanceRequest);
//        String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + processInstanceRequest;
//        ObjectMapper objectMapper = new ObjectMapper();
//        InputStream inputStream;
//        try {
//            inputStream = new FileInputStream(filePath);
//            requestBody = String.format(objectMapper.readTree(inputStream).toString(), processInstanceKey, processInstanceKey);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        //Approach 2: Use pojo
//        requestBody = FileUtils.getInstance().readJsonFileToString("processInstanceRequest.json", processInstanceKey, processInstanceKey);
        Object requestData;
        requestData = new ProcessInstanceRequest();
        ((ProcessInstanceRequest) requestData).setProcessInstanceId(Long.toString(processInstanceKey));
        ((ProcessInstanceRequest) requestData).setTreePath(Long.toString(processInstanceKey));
        ((ProcessInstanceRequest) requestData).setPageSize(50);
        List<Map<String, Object>> requestDataListMap = new ArrayList<>();
        Map<String, Object> requestDataMap = new HashMap<>();
        requestDataMap.put("processInstanceId", ((ProcessInstanceRequest) requestData).getProcessInstanceId());
        requestDataMap.put("treePath", ((ProcessInstanceRequest) requestData).getTreePath());
        requestDataMap.put("pageSize", ((ProcessInstanceRequest) requestData).getPageSize());
        requestDataListMap.add(requestDataMap);
        ((ProcessInstanceRequest) requestData).setQueries(requestDataListMap);
        Response flowNodeInstances = RestAssured.given()
                .header("Authorization", "Bearer " + operateAccessToken)
                .header("content-type", "application/json")
                .body(requestData)
                .post(CAMUNDA_OPERATE_BASE_URL + "/api/flow-node-instances");
        Assert.assertEquals("", "startEvent", flowNodeInstances.jsonPath().getString(processInstanceKey + ".children[0].flowNodeId"));
        Assert.assertEquals("", "COMPLETED", flowNodeInstances.jsonPath().getString(processInstanceKey + ".children[0].state"));

        //User task:
        //1. Get Task list access token
        if (tasklistAccessToken == null || tasklistAccessToken.isEmpty()) {
            tokenRequest = "tokenTasklistRequest.json";
            Map<String, String> jsonFileData = FileUtils.getInstance().readJsonFileToMap(tokenRequest);
            Response taskListAccessTokenResponse = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(jsonFileData)
                    .post(CAMUNDA_OAUTH_URL);

            tasklistAccessToken = taskListAccessTokenResponse.jsonPath().getString("access_token");
            System.out.println("tasklistAccessToken = " + tasklistAccessToken);
        }
        //2. Get user tasks that are available
        Object getTaskListRequest;
        getTaskListRequest = new GetTaskListRequest();
        Map<String, Object> sortMap = new HashMap<>();
        ((GetTaskListRequest) getTaskListRequest).setField("creationTime");
        ((GetTaskListRequest) getTaskListRequest).setOrder("DESC");
        ((GetTaskListRequest) getTaskListRequest).setPageSize(50);
        ((GetTaskListRequest) getTaskListRequest).setState("CREATED");
        sortMap.put("field", ((GetTaskListRequest) getTaskListRequest).getField());
        sortMap.put("order", ((GetTaskListRequest) getTaskListRequest).getOrder());
        List<Map<String, Object>> sortMapList = new ArrayList<>();
        sortMapList.add(sortMap);
        ((GetTaskListRequest) getTaskListRequest).setSort(sortMapList);
//        Response taskListResponse = RestAssured.given()
//                .header("Authorization", "Bearer " + tasklistAccessToken)
//                .contentType(ContentType.JSON)
//                .body(getTaskListRequest)
//                .post(CAMUNDA_TASKLIST_BASE_URL + "/v1/tasks/search");
        String theFirstTaskListId = null;

        List<Map<String, Object>> listMap = RestAssured.given()
                .header("Authorization", "Bearer " + tasklistAccessToken)
                .contentType(ContentType.JSON)
                .body(getTaskListRequest)
                .post(CAMUNDA_TASKLIST_BASE_URL + "/v1/tasks/search")
                .as(new TypeRef<>() {
                });
        for (Map<String, Object> map : listMap) {
            if (map.get("processDefinitionKey").equals(Long.toString(processDefinitionKey))
                    && map.get("processInstanceKey").equals(Long.toString(processInstanceKey))) {
                theFirstTaskListId = map.get("id").toString();
            }
        }

        //3. Click on btn assign to me
        //case 1: Call API
//        Object assignTaskRequest = new AssignTaskRequest();
//        ((AssignTaskRequest) assignTaskRequest).setName("assignee");
//        ((AssignTaskRequest) assignTaskRequest).setValue("\\\"linh linh\\\"");
//        Map<String, Object> variablesMap = new HashMap<>();
//        variablesMap.put("name", ((AssignTaskRequest) assignTaskRequest).getName());
//        variablesMap.put("value", ((AssignTaskRequest) assignTaskRequest).getValue());
//        List<Map<String, Object>> variablesMapList = new ArrayList<>();
//        variablesMapList.add(variablesMap);
//        ((AssignTaskRequest) assignTaskRequest).setVariables(variablesMapList);

//        Response assignTaskResponse = RestAssured.given()
//                .header("Authorization", "Bearer " + tasklistAccessToken)
//                .contentType(ContentType.JSON)
//                .pathParam("taskListId", theFirstTaskListId)
//                .body(assignTaskRequest)
//                .patch(CAMUNDA_TASKLIST_BASE_URL + "/v1/tasks/{taskListId}/assign");

        //cae 2: Use TaskList client
        // Get tasklist authentication
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.addProduct(Product.TASKLIST, new JwtCredential(ZEEBE_CLIENT_ID, ZEEBE_CLIENT_SECRET, "tasklist.camunda.io", "https://login.cloud.camunda.io/oauth/token"));
        Authentication auth = SaaSAuthentication.builder().jwtConfig(jwtConfig).build();
        System.out.println("tasklist auth =" + auth);
        //Create client
        CamundaTaskListClient client;
        try {
            client = CamundaTaskListClient.builder().taskListUrl(CAMUNDA_TASKLIST_BASE_URL).shouldReturnVariables().shouldLoadTruncatedVariables().authentication(auth).build();
            TaskList taskList;
            taskList = client.getTasks(false, TaskState.CREATED, 50);
            for(Task task : taskList) {
                //assign task to me
                if (task.getProcessDefinitionKey().equals(Long.toString(processDefinitionKey))
                        && task.getProcessInstanceKey().equals(Long.toString(processInstanceKey))){
                    client.claim(task.getId(), username);
                    Assert.assertEquals("", "CREATED", task.getTaskState().toString());
                    Assert.assertEquals("", username, task.getAssignee());
                }
            }
        } catch (TaskListException e) {
            throw new RuntimeException(e);
        }


        //4. Fill data and click on complete task
//        Response completeTaskResponse = RestAssured.given()
//                .header("Authorization", "Bearer " + tasklistAccessToken)
//                .contentType(ContentType.JSON)
//                .pathParam("taskListId", theFirstTaskListId)
//                .body(assignTaskRequest)
//                .patch(CAMUNDA_TASKLIST_BASE_URL + "/v1/tasks/{taskListId}/complete");
//        Assert.assertEquals("", "COMPLETED", completeTaskResponse.jsonPath().getString("taskState"));

    }
}


