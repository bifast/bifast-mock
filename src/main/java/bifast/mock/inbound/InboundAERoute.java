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
import bifast.mock.inbound.pojo.AERequestPojo;

@Component
public class InboundAERoute extends RouteBuilder{
	@Autowired BuildAERequestProcessor buildAERequest;

	JacksonDataFormat aeRequestJDF = new JacksonDataFormat(AERequestPojo.class);
	JacksonDataFormat busMesgJDF = new JacksonDataFormat(BusinessMessage.class);

	@Override
	public void configure() throws Exception {
		aeRequestJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		aeRequestJDF.setInclude("NON_NULL");
		aeRequestJDF.setInclude("NON_EMPTY");
		aeRequestJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
//		aeRequestJDF.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		
		busMesgJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		busMesgJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
		busMesgJDF.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		busMesgJDF.setInclude("NON_NULL");
		busMesgJDF.setInclude("NON_EMPTY");


		from("direct:inb_ae").routeId("inbound_ae")
			.setExchangePattern(ExchangePattern.InOnly)
			.log("start direct:inb_ae")

			.process(buildAERequest)
			.marshal(busMesgJDF)
			.log("AE Request: ${body}")	
			
			.to("rest:post:?host={{komi.inbound-url}}"
					+ "&exchangePattern=InOnly"
						+ "&bridgeEndpoint=true"
					)

			.convertBodyTo(String.class)
			.log("AE Response: ${body}")
//			.removeHeaders("*")
//			.stop()
//			.unmarshal(busMesgJDF)
//

		;
	}

}
