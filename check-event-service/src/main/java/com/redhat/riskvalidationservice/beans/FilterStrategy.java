package com.redhat.riskvalidationservice.beans;

import com.google.gson.Gson;
import com.redhat.riskvalidationservice.datamodels.ApbRuns;
import com.redhat.riskvalidationservice.datamodels.Example;
import com.redhat.riskvalidationservice.datamodels.SensuEvents;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.kie.dmn.api.core.*;

import java.util.*;

public class FilterStrategy implements AggregationStrategy {

    static Map<String,List<ApbRuns>> runs = new HashMap<>();
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {


        System.out.println("hello");


        if (newExchange.getFromRouteId().equals("readSensu")) {

            System.out.println("inside validate");
            DMNRuntime dmnRuntime = RuleSessionFactory.createDMNRuntime();
            Example example = new Gson().fromJson(newExchange.getIn().getBody().toString(), Example.class);

            String namespace = "https://kiegroup.org/dmn/_C57E89DD-6F36-4590-809A-0B8E742F2676";
            String modelName = "ProcessFailureDMN";
            DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);


            SensuEvents sensuEvents = new SensuEvents();

            newExchange.setProperty("sensuId",example.getId());

            sensuEvents.setCheckType(example.getCheck().getMetadata().getName());
            sensuEvents.setEventDate(example.getCheck().getExecuted());
            sensuEvents.setHostName(example.getEntity().getSystem().getHostname());
            sensuEvents.setStatus(String.valueOf(example.getCheck().getStatus()));

            System.out.println(example.getCheck().getMetadata().getName());

            System.out.println(new Gson().toJson(sensuEvents));
            HashMap<String, List<ApbRuns>> aggregateMap = null;

            DMNContext dmnContext = dmnRuntime.newContext();
            dmnContext.set("SensuEvents", sensuEvents);
            if (null != runs) {
                dmnContext.set("ApbRuns", runs.get(example.getEntity().getSystem().getHostname()));
                System.out.println(runs.get(example.getEntity().getSystem().getHostname()));
            } else {
                dmnContext.set("ApbRuns", new ArrayList<>());
            }


            dmnContext.set("Frequency",2);
            dmnContext.set("Interval",1);

            DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);

            DMNDecisionResult resultOffer = dmnResult.getDecisionResultByName("Invoke?");
            DMNDecisionResult playbook = dmnResult.getDecisionResultByName("Playbook");

            System.out.println("invoke" + resultOffer.getResult());
            System.out.println("playbook" + playbook.getResult());


            if (playbook.getResult() != null) {
                ApbRuns apb = new ApbRuns();
                apb.setApbName(playbook.getResult().toString());
                apb.setCheckName(sensuEvents.getCheckType());
                apb.setHostName(sensuEvents.getHostName());
                apb.setRunDate(new Date().getTime());


                newExchange.getIn().setBody(new Gson().toJson(apb));

            } else {
                newExchange.getIn().setBody(null);
            }



        } else {
            Map<String,List<ApbRuns>> map = newExchange.getIn().getBody(Map.class);

            for (String key: map.keySet()) {
                if(null != runs.get(key)){
                    runs.get(key).addAll(map.get(key));
                }else {
                    runs.put(key,map.get(key));
                }
            }
            return null;
        }
        return newExchange;
    }


}
