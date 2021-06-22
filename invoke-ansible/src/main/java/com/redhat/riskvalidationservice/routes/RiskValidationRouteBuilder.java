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

import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


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

			HttpComponent httpComponent = createCustomHttp4Component();
			this.getContext().addComponent("https4", httpComponent);

			from(invokeAnsibleKafkaRoute)
				.id(ROUTE_ID_INVOKE_ANSIBLE_KAFKA)
				.routeId(ROUTE_ID_INVOKE_ANSIBLE_KAFKA)
				.bean(RiskValidationBean.class,"prepareAnsibleRequest").id(ROUTE_ID_PREPARE_ANSIBLE_REQUEST)
					.setHeader(Exchange.HTTP_METHOD, constant("POST"))
					.setHeader("Authorization",constant("Bearer HZi06ABZxcW1KQhD6t5ffK99l3HKpu"))
					.setHeader("Content-Type",constant("application/json"))
					.toD("https4://"+ansibleTowerUrl+"/api/v2/job_templates/${header.apbName}/launch/").id(ROUTE_ID_SEND_TO_TOWER)
					.bean(RiskValidationBean.class,"readAnsibleResponse").id(ROUTE_ID_READ_ANSIBLE_RESPONSE)
					.to("kafka:"+"ansiblestat"+ "?brokers=" + kafkaBootstrap).id(ROUTE_ID_SEND_ANSIBLE_STATUS_KAFKA);







		}catch (Exception e) {
			e.printStackTrace();
		}


	}

	public HttpComponent createCustomHttp4Component() {
		HttpComponent httpComponent = new HttpComponent();

		httpComponent.setX509HostnameVerifier(AllowAllHostnameVerifier.INSTANCE);


		TrustManagersParameters trustManagersParameters = new TrustManagersParameters();
		X509ExtendedTrustManager extendedTrustManager = new InsecureX509TrustManager();
		trustManagersParameters.setTrustManager(extendedTrustManager);

		SSLContextParameters sslContextParameters = new SSLContextParameters();
		sslContextParameters.setTrustManagers(trustManagersParameters);
		httpComponent.setSslContextParameters(sslContextParameters);

		return httpComponent;
	}

	private static class InsecureX509TrustManager extends X509ExtendedTrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

		}

		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

		}

		@Override
		public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

		}

		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

		}

		@Override
		public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

		}

		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}


}
