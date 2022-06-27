package bifast.mock.inbound;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.library.iso20022.custom.BusinessMessage;

@Component
public class InboundCTRoute extends RouteBuilder{
	@Autowired BuildAERequestProcessor buildAERequest;
	@Autowired BuildCTRequestProcessor buildCTRequest;
	@Autowired BuildSttlProcessor buildSettlement;

	JacksonDataFormat busMesgJDF = new JacksonDataFormat(BusinessMessage.class);

	@Override
	public void configure() throws Exception {
		
		busMesgJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		busMesgJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
		busMesgJDF.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		busMesgJDF.setInclude("NON_NULL");
		busMesgJDF.setInclude("NON_EMPTY");


		from("direct:inb_ct").routeId("inbound_ct")
			.setExchangePattern(ExchangePattern.InOnly)
			.log("start kirim ct")

			.process(buildCTRequest)
			.setHeader("inb_ctRequest", simple("${body}"))
			
			.marshal(busMesgJDF)
			.log("CT Request: ${body}")
			
			.to("rest:post:?host={{komi.inbound-url}}"
					+ "&exchangePattern=InOnly"
						+ "&bridgeEndpoint=true"
					)
			.convertBodyTo(String.class)
			.log("CT Response: ${body}")
			.unmarshal(busMesgJDF)
			
			.log("Status CT: ${body.document.fiToFIPmtStsRpt.txInfAndSts[0].txSts}")
			.setHeader("inb_ctResponse", simple("${body}"))

			.filter().simple("${body.document.fiToFIPmtStsRpt.txInfAndSts[0].txSts} == 'ACTC'")
				.log("akan kirim settlement")
				.setExchangePattern(ExchangePattern.InOnly)
				.to("seda:inb_settlement")
			.end()
			
		;
		
		
		from("seda:inb_settlement").routeId("inbound_sttl")
			.delay(7000)
			.process(buildSettlement)
			.marshal(busMesgJDF)
			.log("Settlement: ${body}")	
	
			.to("rest:post:?host={{komi.inbound-url}}"
	//			+ "&exchangePattern=InOnly"
					+ "&bridgeEndpoint=true"
				)
			.convertBodyTo(String.class)
		;

	}

}
