package academy.camunda;

import academy.handler.CreditCardServiceHandler;
import academy.handler.ServiceTaskHandler;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


public class SimpleModel {

    private static final String ZEEBE_ADDRESS = "bd33d8d7-04e0-4484-9d49-3235ad899a99.ont-1.zeebe.camunda.io:443";
    private static final String ZEEBE_CLIENT_ID = "t8zhcNtRzIw4Nz~CopppLP6pzF9VgsQ5";
    private static final String ZEEBE_CLIENT_SECRET = "~R2sOdP0O.aESEYbxui5iMSYfHrtsrUcgejwIs_dkn5zUEd7qvUkUHfCf~0ufOpr";
    private static final String ZEEBE_AUTHORIZATION_SERVER_URL = "https://login.cloud.camunda.io/oauth/token";
    private static final String ZEEBE_TOKEN_AUDIENCE = "zeebe.camunda.io";

    private static long processInstanceKey;
    private static long processDefinitionKey;

    public static void main(String[] args) {
        final OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
                .authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL).audience(ZEEBE_TOKEN_AUDIENCE)
                .clientId(ZEEBE_CLIENT_ID).clientSecret(ZEEBE_CLIENT_SECRET).build();

        try (final ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress(ZEEBE_ADDRESS)
                .credentialsProvider(credentialsProvider)
                .build()) {
            System.out.println("Connected to: " + client.newTopologyRequest().send().join());


            //Add a new Create Instance Command
            ProcessInstanceEvent processInstanceEvent = client.newCreateInstanceCommand()
                    .bpmnProcessId("simplemodel")
                    .latestVersion()
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
                            .jobType("serviceTask")
                            .handler(new ServiceTaskHandler())
                            .timeout(Duration.ofSeconds(10).toMillis())
                            .open();
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ;

    }

}


