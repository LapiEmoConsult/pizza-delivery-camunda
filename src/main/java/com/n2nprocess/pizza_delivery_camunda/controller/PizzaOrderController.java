package com.n2nprocess.pizza_delivery_camunda.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class PizzaOrderController {
    private static final Logger logger = LoggerFactory.getLogger(PizzaOrderController.class);

    @Autowired
    private ZeebeClient zeebeClient;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startPizzaOrder(@RequestBody Map<String, Object> orderData) {

        // Generate order ID if not provided
        String orderId = (String) orderData.getOrDefault("orderId", UUID.randomUUID().toString());

        // Prepare process variables
        Map<String, Object> variables = Map.of(
                "orderId", orderId,
                "customerName", orderData.get("customerName"),
                "pizzaType", orderData.get("pizzaType"),
                "deliveryAddress", orderData.get("deliveryAddress"),
                "orderAmount", orderData.get("orderAmount"),
                "customerPhone", orderData.get("customerPhone")
        );

        try {
            ProcessInstanceEvent processInstance = zeebeClient
                    .newCreateInstanceCommand()
                    .bpmnProcessId("pizza-delivery-process")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            logger.info("Started pizza delivery process for order: {} with process instance: {}", orderId, processInstance.getProcessInstanceKey());

            return ResponseEntity.ok(Map.of(
                    "orderId", orderId,
                    "processInstanceKey", processInstance.getProcessInstanceKey(),
                    "status", "Process started successfully"
            ));
        } catch (Exception e) {
            logger.error("Failed to start process for order: {}", orderId, e);

            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to start process: " + e.getMessage()
            ));
        }
    }
}
