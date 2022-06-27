package bifast.mock.inbound;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.mock.inbound.pojo.PaymentRequestPojo;
import bifast.mock.inbound.pojo.AERequestPojo;
import bifast.mock.inbound.pojo.CTResponsePojo;
import bifast.mock.inbound.pojo.InboundMockWrapper;

@Component
public class InboundRoute extends RouteBuilder {
	
	@Autowired BuildAERequestProcessor buildAERequest;
	@Autowired BuildCTRequestProcessor buildCTRequest;

	JacksonDataFormat mockRequestWrapperJDF = new JacksonDataFormat(InboundMockWrapper.class);
	JacksonDataFormat pymtRequestJDF = new JacksonDataFormat(PaymentRequestPojo.class);
	JacksonDataFormat ctResponseJDF = new JacksonDataFormat(CTResponsePojo.class);
	JacksonDataFormat busMesgJDF = new JacksonDataFormat(BusinessMessage.class);
	JacksonDataFormat aeRequestJDF = new JacksonDataFormat(AERequestPojo.class);
	JacksonDataFormat paymtRequestJDF = new JacksonDataFormat(PaymentRequestPojo.class);

	private void configureJson() {
		mockRequestWrapperJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		mockRequestWrapperJDF.setInclude("NON_NULL");
		mockRequestWrapperJDF.setInclude("NON_EMPTY");
//		mockRequestWrapperJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);

		aeRequestJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		aeRequestJDF.setInclude("NON_NULL");
		aeRequestJDF.setInclude("NON_EMPTY");
		aeRequestJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);

		paymtRequestJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		paymtRequestJDF.setInclude("NON_NULL");
		paymtRequestJDF.setInclude("NON_EMPTY");
		paymtRequestJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);

		ctResponseJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		ctResponseJDF.setInclude("NON_NULL");
		ctResponseJDF.setInclude("NON_EMPTY");
		
		busMesgJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		busMesgJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
		busMesgJDF.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		busMesgJDF.setInclude("NON_NULL");
		busMesgJDF.setInclude("NON_EMPTY");

	}

	
	@Override
	public void configure() throws Exception {

		onException(Exception.class)
			.log("Inbound error.")
	    	.log(LoggingLevel.ERROR, "${exception.stacktrace}")
		;
		
		
		configureJson();
		
		restConfiguration()
			.component("servlet")
		;
		
		rest("/")
			.post("/inbound")
				.consumes("application/json")
				.to("direct:inbound")
		;
	

		from("direct:inbound").routeId("InboundMsg")
			.convertBodyTo(String.class)
			.log("Terima di mock")
			.log("${body}")
			
			.unmarshal(mockRequestWrapperJDF)

			.choice()
				.when().simple("${body.paymentRequest} != null")
					.log("PaymentRequest")
					.marshal(paymtRequestJDF)
					.unmarshal(paymtRequestJDF)
					.to("direct:payment")
				.when().simple("${body.aeRequest} != null")
					.log("AccountEnquiry")
					.marshal(aeRequestJDF)
					.unmarshal(aeRequestJDF)
					.to("direct:inb_ae")
				.when().simple("${body.ctRequest} != null")
					.log("CreditTransferRequest")
//					.to("direct:inb_ct")
				.otherwise()
					.log("oh oh")
			.end()
			
			.removeHeaders("*")
			.log("selesai proses")
		;
				
		
	}

}
