package academy.handler;

import academy.service.ServiceTaskService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

import java.util.HashMap;
import java.util.Map;

public class ServiceTaskHandler implements JobHandler{
    ServiceTaskService serviceTaskService = new ServiceTaskService();

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {

        final Map<String, Object> inputVariables = job.getVariablesAsMap();
        final String name = (String) inputVariables.get("name");
        final String age = (String) inputVariables.get("age");


        //Instantiate and invoke the CreditCardService from the Handler
        final String confirmation = serviceTaskService.serviceTask(name, age);
        System.out.println("CreditCardServiceHandler - confirmation: " + confirmation);

        final Map<String, Object> outputVariables = new HashMap<String, Object>();
        outputVariables.put("confirmation", confirmation);
        //After the CreditCardService has been invoked, we need to tell Zeebe
        //that the Job has been completed. Add the following statement to the end of the handle method:
        client.newCompleteCommand(job.getKey()).variables(outputVariables).send().join();

    }

}
