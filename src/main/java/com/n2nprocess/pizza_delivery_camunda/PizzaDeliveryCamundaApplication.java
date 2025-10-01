package com.n2nprocess.pizza_delivery_camunda;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = {"classpath:pizza-delivery-process.bpmn"})
public class PizzaDeliveryCamundaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PizzaDeliveryCamundaApplication.class, args);
	}

}
