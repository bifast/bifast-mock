package bifast.mock.route;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.mock.processor.BuildReversal;
import bifast.mock.processor.SettlementProcessor;

@Component
public class SettlementRoute extends RouteBuilder {
	
	@Autowired private BuildReversal buildReversal;
	@Autowired private SettlementProcessor settlementProcessor;
	
	JacksonDataFormat jsonBusinessMessageDataFormat = new JacksonDataFormat(BusinessMessage.class);

	private void configureJson() {
		jsonBusinessMessageDataFormat.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		jsonBusinessMessageDataFormat.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
		jsonBusinessMessageDataFormat.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		jsonBusinessMessageDataFormat.setInclude("NON_NULL");
		jsonBusinessMessageDataFormat.setInclude("NON_EMPTY");
	}

	
	@Override
	public void configure() throws Exception {

		configureJson();
		
	
		from("sql:select * from mock_pacs002 where result = 'ACTC' and sttl is null")
			.routeId("settlement")

			
			.setHeader("sttl_tableqry", simple("${body}"))
			.setHeader("sttl_id", simple("${body[ID]}"))

			.setBody(simple("${header.sttl_tableqry[FULL_MESSAGE]}"))
			.unmarshal(jsonBusinessMessageDataFormat)
			.setHeader("hdr_ctresponse", simple("${body}"))

			.log("${header.sttl_tableqry[RESULT]}")
	
			.setBody(simple("${header.sttl_tableqry[CT_REQUEST]}"))
			.unmarshal(jsonBusinessMessageDataFormat)
			.setHeader("hdr_ctrequest", simple("${body}"))

		
			.process(settlementProcessor)
		
			.marshal(jsonBusinessMessageDataFormat)
			.log("Submit settlement: ${body}")
			.doTry()
				.to("rest:post:?host={{komi.inbound-url}}"
					+ "&exchangePattern=InOnly"
//						+ "&bridgeEndpoint=true"
					)
				
	 		.endDoTry()
	    	.doCatch(Exception.class)
				.log("Send settlement error.")
		    	.log(LoggingLevel.ERROR, "${exception.stacktrace}")
	    	.end()

	    	.log("Reversal Flag: ${header.hdr_reversal}")
			.filter().simple("${header.hdr_reversal} == 'YES' ")
				.log("submit reversal message")
				.setBody(simple("${header.sttl_tableqry[CT_REQUEST]}"))
				.log("${body}")
				.unmarshal(jsonBusinessMessageDataFormat)
				
				.setExchangePattern(ExchangePattern.InOnly)
				.to("seda:reversal")
			.end()
			
			.to("sql:update mock_pacs002 set sttl = 'DONE' where id::varchar = :#${header.sttl_id}::varchar")
			.removeHeaders("sttl_*")

		;

		from("seda:reversal").routeId("sedareversal")
			.log("send reversal:")
			.marshal(jsonBusinessMessageDataFormat)
			.log("sebelum: ${body}")
			.unmarshal(jsonBusinessMessageDataFormat)
			.process(buildReversal)
			.delay(3000)
			.marshal(jsonBusinessMessageDataFormat)
			.log("sesudah: ${body}")
			.doTry()
				.to("rest:post:?host={{komi.inbound-url}}"
					+ "&exchangePattern=InOnly"
		//						+ "&bridgeEndpoint=true"
					)
				
				.endDoTry()
				.convertBodyTo(String.class)
				.log("Hasil reversal: ${body}")
			.doCatch(Exception.class)
				.log("Send reversal error.")
		    	.log(LoggingLevel.ERROR, "${exception.stacktrace}")
			.end()
	
		;
		
	}
	


}
