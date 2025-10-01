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
public class QualityCheckWorker {
    private static final Logger logger = LoggerFactory.getLogger(QualityCheckWorker.class);

    private final Random random = new Random();

    @JobWorker(type = "quality-check")
    public void handleQualityCheck(final JobClient client, final ActivatedJob job) {

        logger.info("Performing quality check for order: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String orderId = (String) variables.get("orderId");

        // Simulate quality check logic
        QualityCheckResult result = performQualityCheck(orderId);

        logger.info("Quality check result for order {}: {}", orderId, result.status);

        client.newCompleteCommand(job.getKey())
                .variables(Map.of(
                        "qualityStatus", result.status,
                        "qualityScore", result.score,
                        "qualityNotes", result.notes
                ))
                .send()
                .join();
    }

    private QualityCheckResult performQualityCheck(String orderId) {
        try {
            Thread.sleep(500); // Simulate inspection time

            int score = 80 + random.nextInt(20); // Score between 80-100
            String status = score >= 85 ? "PASSED" : "NEEDS_REVIEW";
            String notes = score >= 95 ? "Excellent quality" :
                    score >= 85 ? "Good quality" : "Minor issues detected";

            return new QualityCheckResult(status, score, notes);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new QualityCheckResult("ERROR", 0, "Quality check interrupted");
        }
    }

    private static class QualityCheckResult {
        final String status;
        final int score;
        final String notes;

        QualityCheckResult(String status, int score, String notes) {
            this.status = status;
            this.score = score;
            this.notes = notes;
        }
    }
}
