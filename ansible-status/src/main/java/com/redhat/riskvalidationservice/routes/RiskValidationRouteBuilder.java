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

	private static final Logger LOG = Logger.getLogger(RiskValidationRouteBuilder.class.getName());

	private String kafkaBootstrap = "my-cluster-kafka-brokers:9092";
	private String kafkaCreditTransferCreditorTopic = "sensu";
	private String consumerMaxPollRecords ="50000";
	private String consumerCount = "3";
	private String consumerSeekTo = "beginning";
	private String consumerGroup = "invokeansible";
	private String consumerGroup2 = "risk";


	@Value("${ansible.tower.url}")
	String ansibleTowerUrl;




	@Override
	public void configure() throws Exception {
		try {
			System.out.println("starting account validation service");


			KafkaComponent kafka = new KafkaComponent();
			kafka.setBrokers(kafkaBootstrap);
			this.getContext().addComponent("kafka", kafka);

			from("kafka:" + "ansiblestat" + "?brokers=" + kafkaBootstrap + "&maxPollRecords="
					+ consumerMaxPollRecords + "&seekTo=" + "end"
					+ "&groupId=" + consumerGroup).id("apbEvents")
                    .delay(20000)
			        .bean(RiskValidationBean.class,"prepareAnsibleRequest")
					.setHeader(Exchange.HTTP_METHOD, constant("GET"))
					.setHeader("Authorization",constant("Bearer xxxxxxxxxxxx"))
					.toD("https4://"+ansibleTowerUrl+"/api/v2/jobs/${header.jobId}/")
                    .bean(RiskValidationBean.class,"readAnsibleResponse")
                    .log("${body}")
                    .choice()
                    .when(header("jobId").isEqualTo(body()))
                    .to("kafka:"+"ansiblestat"+ "?brokers=" + kafkaBootstrap)
                    .otherwise()
                    .bean(RiskValidationBean.class,"checkStatusResponse")
                    .to("kafka:"+"statuschck"+ "?brokers=" + kafkaBootstrap);








		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
