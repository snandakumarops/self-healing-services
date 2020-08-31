package com.redhat.riskvalidationservice.routes;

import com.google.gson.Gson;
import com.redhat.riskvalidationservice.beans.CollectEventsStrategy;
import com.redhat.riskvalidationservice.beans.FilterStrategy;
import com.redhat.riskvalidationservice.beans.RiskValidationBean;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
import org.springframework.beans.factory.annotation.Value;

import java.util.logging.Logger;

public class RiskValidationRouteBuilder extends RouteBuilder {


	@Value("${ansible.tower.url}")
	String ansibleTowerUrl;



	private static final Logger LOG = Logger.getLogger(RiskValidationRouteBuilder.class.getName());

	private String kafkaBootstrap = "my-cluster-kafka-brokers:9092";
	private String kafkaCreditTransferCreditorTopic = "sensu";
	private String consumerMaxPollRecords ="50000";
	private String consumerCount = "3";
	private String consumerSeekTo = "beginning";
	private String consumerGroup = "invokeansible";
	private String consumerGroup2 = "risk";




	@Override
	public void configure() throws Exception {
		try {
			System.out.println("starting account validation service");


			KafkaComponent kafka = new KafkaComponent();
			kafka.setBrokers(kafkaBootstrap);
			this.getContext().addComponent("kafka", kafka);

			from("kafka:" + "event-decision" + "?brokers=" + kafkaBootstrap + "&maxPollRecords="
					+ consumerMaxPollRecords + "&seekTo=" + "beginning"
					+ "&groupId=" + consumerGroup).id("apbEvents")
			.bean(RiskValidationBean.class,"prepareAnsibleRequest")
					.setHeader(Exchange.HTTP_METHOD, constant("POST"))
					.setHeader("Authorization",constant("Bearer vbQEgODL9AMGkq2dMwQSyi0Gb7rxxw"))
					.setHeader("Content-Type",constant("application/json"))
					.toD("https4://"+ansibleTowerUrl+"/api/v2/job_templates/${header.apbName}/launch/")
					.bean(RiskValidationBean.class,"readAnsibleResponse")
					.to("kafka:"+"ansiblestat"+ "?brokers=" + kafkaBootstrap);







		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
