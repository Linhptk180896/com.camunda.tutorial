package academy.camunda;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


import academy.handler.CreditCardServiceHandler;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;


public class PaymentApplication {

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

            final Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("reference", "C8_12345");
            variables.put("amount", Double.valueOf(100.00));
            variables.put("cardNumber", "1234567812345678");
            variables.put("cardExpiry", "12/2023");
            variables.put("cardCVC", "123");

            //Add a new Create Instance Command beneath the map of process variables
            ProcessInstanceEvent processInstanceEvent = client.newCreateInstanceCommand()
                    .bpmnProcessId("paymentProcess")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            processInstanceKey = processInstanceEvent.getProcessInstanceKey();
            processDefinitionKey = processInstanceEvent.getProcessDefinitionKey();

            final JobWorker creditCardWorker =
                    client.newWorker()
                            .jobType("chargeCreditCard")
                            .handler(new CreditCardServiceHandler())
                            .timeout(Duration.ofSeconds(10).toMillis())
                            .open();
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}


