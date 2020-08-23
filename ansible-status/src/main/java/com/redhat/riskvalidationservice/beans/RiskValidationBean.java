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
	    String jobId = new Gson().fromJson(exchange.getIn().getBody().toString(),String.class);
	    Double jobNo = Double.parseDouble(jobId);
	    exchange.getIn().setHeader("jobId",jobNo.intValue());
	    return "";
    }


	public String readAnsibleResponse(String body) {
		String response = null;
		Map<String,String> mapString = new Gson().fromJson(body,Map.class);
		if(mapString.get("status").equals("running")) {
			Double jobNo = Double.parseDouble(new Gson().toJson(mapString.get("id")));
			response= String.valueOf(jobNo.intValue());
		} else {
			response= new Gson().toJson(mapString.get("status"));
		}
		return response;
	}

	public String checkStatusResponse(String body) {
		System.out.println(body);
		return body;
	}




}
