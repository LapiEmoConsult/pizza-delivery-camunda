package com.n2nprocess.pizza_delivery_camunda.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class PaymentWorker {
    private static final Logger logger = LoggerFactory.getLogger(PaymentWorker.class);

    private final Random random = new Random();

    @JobWorker(type = "process-payment")
    public void handlePaymentProcessing(final JobClient client, final ActivatedJob job) {
        logger.info("Processing payment for order: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String orderId = (String) variables.get("orderId");
        Double orderAmount = (Double) variables.get("orderAmount");

        // Simulate payment processing logic
        boolean paymentSuccessful = simulatePaymentProcessing(orderId, orderAmount);

        if (paymentSuccessful) {
            logger.info("Payment successful for order: {}", orderId);

            client.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "paymentStatus", "COMPLETED",
                            "paymentTimestamp", System.currentTimeMillis()
                    ))
                    .send()
                    .join();
        } else {
            logger.error("Payment failed for order: {}", orderId);

            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Payment processing failed")
                    .send()
                    .join();
        }
    }

    private boolean simulatePaymentProcessing(String orderId, Double amount) {
        // Simulate external payment service call
        try {
            Thread.sleep(1000); // Simulate network delay
            return random.nextDouble() > 0.1; // 90% success rate
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
