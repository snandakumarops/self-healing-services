package com.redhat.riskvalidationservice.routes;

import com.google.gson.Gson;
import com.redhat.riskvalidationservice.beans.CollectEventsStrategy;
import com.redhat.riskvalidationservice.beans.FilterStrategy;
import com.redhat.riskvalidationservice.beans.RiskValidationBean;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class RiskValidationRouteBuilder extends RouteBuilder {


	@Value("${ansible.tower.url}")
	String ansibleTowerUrl;



	private static final Logger LOG = Logger.getLogger(RiskValidationRouteBuilder.class.getName());

	@Value("${INVOKE_ANSIBLE_KAFKA_ROUTE}")
	private String invokeAnsibleKafkaRoute;

	@Value("${KAFKA_BOOTSTRAP_SERVERS}")
	private String kafkaBootstrap;


	public static final String ROUTE_ID_INVOKE_ANSIBLE_KAFKA = "ROUTE_ID_INVOKE_ANSIBLE_KAFKA";
	public static final String ROUTE_ID_PREPARE_ANSIBLE_REQUEST = "ROUTE_ID_PREPARE_ANSIBLE_REQUEST";
	public static final String ROUTE_ID_SEND_TO_TOWER = "ROUTE_ID_SEND_TO_TOWER";
	public static final String ROUTE_ID_READ_ANSIBLE_RESPONSE = "ROUTE_ID_READ_ANSIBLE_RESPONSE";
	public static final String ROUTE_ID_SEND_ANSIBLE_STATUS_KAFKA = "ROUTE_ID_SEND_ANSIBLE_STATUS_KAFKA";

	@Override
	public void configure() throws Exception {
		try {
			System.out.println("starting account validation service");


			KafkaComponent kafka = new KafkaComponent();
			kafka.setBrokers(kafkaBootstrap);
			this.getContext().addComponent("kafka", kafka);

			from(invokeAnsibleKafkaRoute)
				.id(ROUTE_ID_INVOKE_ANSIBLE_KAFKA)
				.routeId(ROUTE_ID_INVOKE_ANSIBLE_KAFKA)
				.bean(RiskValidationBean.class,"prepareAnsibleRequest").id(ROUTE_ID_PREPARE_ANSIBLE_REQUEST)
					.setHeader(Exchange.HTTP_METHOD, constant("POST"))
					.setHeader("Authorization",constant("Bearer 7vXblkwleZHU3PC4Hx4JlqhqDqYXok"))
					.setHeader("Content-Type",constant("application/json"))
					.toD("https4://"+ansibleTowerUrl+"/api/v2/job_templates/${header.apbName}/launch/").id(ROUTE_ID_SEND_TO_TOWER)
					.bean(RiskValidationBean.class,"readAnsibleResponse").id(ROUTE_ID_READ_ANSIBLE_RESPONSE)
					.to("kafka:"+"ansiblestat"+ "?brokers=" + kafkaBootstrap).id(ROUTE_ID_SEND_ANSIBLE_STATUS_KAFKA);







		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
