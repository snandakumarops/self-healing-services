package com.redhat.riskvalidationservice.beans;

import com.google.gson.Gson;
import com.redhat.riskvalidationservice.datamodels.ApbRuns;
import com.redhat.riskvalidationservice.datamodels.Example;
import com.redhat.riskvalidationservice.datamodels.SensuEvents;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.*;
import org.kie.dmn.core.util.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.WebTarget;
import java.util.*;
import java.util.logging.Logger;

@Service
public class RiskValidationBean {

	private static final Logger LOG = Logger.getLogger(RiskValidationBean.class.getName());

	private static final String KSESSION = "";

	private final KieContainer kieContainer;


	@Autowired
	public RiskValidationBean(KieContainer kieContainer) {
		LOG.info("Initializing a new session.");
		this.kieContainer = kieContainer;
	}

	public String prepareAnsibleRequest(Exchange exchange) {
		ApbRuns apbRuns = new Gson().fromJson(exchange.getIn().getBody().toString(),ApbRuns.class);
		exchange.getIn().setHeader("apbName",apbRuns.getApbName());
		String requestOb = "{\"limit\":\"node1\"}";
	    return requestOb;
    }


	public String readAnsibleResponse(String body) {
		Map<String,String> mapString = new Gson().fromJson(body,Map.class);
		return new Gson().toJson(mapString.get("job"));
	}




}
