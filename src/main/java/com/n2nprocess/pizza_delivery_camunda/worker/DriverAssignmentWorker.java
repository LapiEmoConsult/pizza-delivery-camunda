package com.n2nprocess.pizza_delivery_camunda.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class DriverAssignmentWorker {
    private static final Logger logger = LoggerFactory.getLogger(DriverAssignmentWorker.class);
    private final Random random = new Random();
    private final List<String> availableDrivers = Arrays.asList(
            "Mike Johnson", "Sarah Wilson", "David Chen", "Emma Rodriguez", "Tom Anderson"
    );

    @JobWorker(type = "assign-driver")
    public void handleDriverAssignment(final JobClient client, final ActivatedJob job) {
        logger.info("Assigning driver for order: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String orderId = (String) variables.get("orderId");
        String deliveryAddress = (String) variables.get("deliveryAddress");

        // Simulate driver assignment logic
        DriverAssignment assignment = assignDriver(orderId, deliveryAddress);

        logger.info("Assigned driver {} for order {} to address {}", assignment.driverName, orderId, deliveryAddress);

        client.newCompleteCommand(job.getKey())
                .variables(Map.of(
                        "assignedDriver", assignment.driverName,
                        "estimatedDeliveryTime", assignment.estimatedDeliveryTime,
                        "driverContactNumber", assignment.contactNumber
                ))
                .send()
                .join();
    }

    private DriverAssignment assignDriver(String orderId, String deliveryAddress) {
        try {
            Thread.sleep(300); // Simulate assignment processing
            String driverName = availableDrivers.get(random.nextInt(availableDrivers.size()));
            int estimatedMinutes = 20 + random.nextInt(25); // 20-45 minutes
            String contactNumber = generateContactNumber();

            return new DriverAssignment(driverName, estimatedMinutes, contactNumber);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new DriverAssignment("Auto-Assignment Failed", 30, "N/A");
        }
    }

    private String generateContactNumber() {
        return String.format("+1-555-%04d", random.nextInt(10000));
    }

    private static class DriverAssignment {
        final String driverName;
        final int estimatedDeliveryTime;
        final String contactNumber;

        DriverAssignment(String driverName, int estimatedDeliveryTime, String contactNumber) {
            this.driverName = driverName;
            this.estimatedDeliveryTime = estimatedDeliveryTime;
            this.contactNumber = contactNumber;
        }
    }
}
