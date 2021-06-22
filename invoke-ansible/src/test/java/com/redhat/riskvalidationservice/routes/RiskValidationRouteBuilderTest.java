package com.redhat.riskvalidationservice.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.redhat.riskvalidationservice.beans.RiskValidationBean;

@TestPropertySource( properties={ "" } )
@RunWith( CamelSpringRunner.class )
@ContextConfiguration( locations= { "classpath:spring/camel-context.xml" } )
public class RiskValidationRouteBuilderTest {

	@Autowired
	private CamelContext camelContext;

	@Produce( uri="direct:ROUTE_ID_INVOKE_ANSIBLE_KAFKA" )
	private ProducerTemplate producerTemplate;
	
	@Spy
	@Autowired
	private RiskValidationBean riskValidationBean;

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test() throws Exception {
		camelContext.removeRoute(RiskValidationRouteBuilder.ROUTE_ID_INVOKE_ANSIBLE_KAFKA);
		camelContext.getRouteDefinition(RiskValidationRouteBuilder.ROUTE_ID_INVOKE_ANSIBLE_KAFKA).adviceWith(camelContext, new AdviceWithRouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				this.weaveById(RiskValidationRouteBuilder.ROUTE_ID_PREPARE_ANSIBLE_REQUEST).replace().bean(riskValidationBean, "prepareAnsibleRequest");
				this.weaveById(RiskValidationRouteBuilder.ROUTE_ID_SEND_TO_TOWER).remove();
				this.weaveById(RiskValidationRouteBuilder.ROUTE_ID_READ_ANSIBLE_RESPONSE).replace().bean(riskValidationBean, "readAnsibleResponse");
				this.weaveById(RiskValidationRouteBuilder.ROUTE_ID_SEND_ANSIBLE_STATUS_KAFKA).remove();
			}
		});
		producerTemplate.sendBody("{\"check\":{\"metadata\":{\"check-1\":\"\"}}, \"entity\":{\"metadata\":{\"name\":\"host-1\"}}, \"apbName\":\"playbook-1\"}");
		producerTemplate.sendBody("{\"check\":{\"metadata\":{\"check-2\":\"\"}}, \"entity\":{\"metadata\":{\"name\":\"host-2\"}}, \"apbName\":\"playbook-1\"}");

		MockEndpoint.assertIsSatisfied(camelContext);
		Mockito.verify(riskValidationBean, Mockito.times(2)).prepareAnsibleRequest(Mockito.any());
		Mockito.verify(riskValidationBean, Mockito.times(2)).readAnsibleResponse(Mockito.any());
	}

}
