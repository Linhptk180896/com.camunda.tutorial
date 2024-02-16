package academy.handler;

import academy.service.CreditCardService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

import java.util.HashMap;
import java.util.Map;

public class CreditCardServiceHandler implements JobHandler{
    CreditCardService creditCardService = new CreditCardService();

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {

        final Map<String, Object> inputVariables = job.getVariablesAsMap();
        final String reference = (String) inputVariables.get("reference");
        final Double amount =  (Double) inputVariables.get("amount");
        final String cardNumber = (String) inputVariables.get("cardNumber");
        final String cardExpiry = (String) inputVariables.get("cardExpiry");
        final String cardCVC =  (String) inputVariables.get("cardCVC");

        //Instantiate and invoke the CreditCardService from the Handler
        final String confirmation = creditCardService.chargeCreditCard(reference, amount, cardNumber, cardExpiry, cardCVC);
        System.out.println("CreditCardServiceHandler - confirmation: " + confirmation);

        final Map<String, Object> outputVariables = new HashMap<String, Object>();
        outputVariables.put("confirmation", confirmation);
        //After the CreditCardService has been invoked, we need to tell Zeebe
        //that the Job has been completed. Add the following statement to the end of the handle method:
        client.newCompleteCommand(job.getKey()).variables(outputVariables).send().join();

    }


}