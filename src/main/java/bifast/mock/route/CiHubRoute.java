package bifast.mock.route;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.mock.processor.AccountEnquiryResponseProcessor;
import bifast.mock.processor.CreditTransferResponseProcessor;
import bifast.mock.processor.OnRequestProcessor;
import bifast.mock.processor.PaymentStatusResponseProcessor;
import bifast.mock.processor.RejectMessageProcessor;

@Component
public class CiHubRoute extends RouteBuilder {
	
	@Autowired
	private OnRequestProcessor checkMessageTypeProcessor;
	@Autowired
	private AccountEnquiryResponseProcessor accountEnquiryResponseProcessor;
	@Autowired
	private CreditTransferResponseProcessor creditTransferResponseProcessor;
	@Autowired
	private PaymentStatusResponseProcessor paymentStatusResponseProcessor;
	@Autowired
	private RejectMessageProcessor rejectMessageProcessor;

	
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
		
		onException(IOException.class)
			.log("Client close connection")
			.handled(true)
		;
		
		restConfiguration()
			.component("servlet")
		;
		
		rest("/")
			.post("/cihub")
				.consumes("application/json")
				.to("direct:receive")
			.post("/cihub-proxy-regitrastion")
				.consumes("application/json")
				.to("direct:proxyRegistration")
		;
	

		from("direct:receive").routeId("ciconn")

			.setExchangePattern(ExchangePattern.InOut)
			.convertBodyTo(String.class)
			.log("Terima: ${body}")
			.unmarshal(jsonBusinessMessageDataFormat)
			.setHeader("objRequest", simple("${body}"))
			
			.log("Cek Msg Type Processor")
			.process(checkMessageTypeProcessor)
			.log("${header.msgType}")

			.setHeader("delay_ae", simple("{{komi.timeout-ae}}"))
			.setProperty("delay_ps", simple("{{komi.timeout-ps}}"))

			.choice()

				.when().simple("${header.msgType} == 'AccountEnquiryRequest'")
					.log("Akan process AE 1")
					.process(accountEnquiryResponseProcessor)

					.filter().simple("${header.hdr_account_no} startsWith '99' ")
						.process(rejectMessageProcessor)
						.unmarshal(jsonBusinessMessageDataFormat)
					.end()

					.filter().simple("${header.hdr_account_no} startsWith '77' ")
						.log("start 77")
						.delay(simple("${header.delay_ae}"))

						.removeHeaders("*")
						.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(504))
						.setBody(constant("Timeout"))
						.stop()
					.end()
					.log("selesai process AE")
				.endChoice()

				.when().simple("${header.msgType} == 'CreditTransferRequest'")
					.log("Akan process CT")
					
//					.delay(constant(30000))

					.process(creditTransferResponseProcessor)

					.setHeader("hdr_ctResponseObj",simple("${body}"))		
					
				.endChoice()

				.when().simple("${header.msgType} == 'PaymentStatusRequest'")
					.log("Akan proses paymentStatusResponseProcessor")
					.process(paymentStatusResponseProcessor)
					
//					.log("PS delay dulu")
//					.delay(simple("${exchangeProperty.delay_ps}"))
					
					.filter().simple("${body} == null")
						.log("ga nemu payment status")
					.end()
				.endChoice()
				
				.when().simple("${header.msgType} == 'ReverseCreditTransferRequest'")
					.log("akan proses creditTransferResponseProcessor")
					.process(creditTransferResponseProcessor)
				.endChoice()

				.when().simple("${header.msgType} == 'ProxyRegistrationRequest'")
					.to("direct:prxyregn")
				.endChoice()

				.when().simple("${header.msgType} == 'ProxyResolutionRequest'")
					.to("direct:prxyreso")
				.endChoice()
				
				.when().simple("${header.msgType} == 'ProxyRegistrationInquiryRequest'")
					.to("direct:prxyregninquiry")
				.endChoice()

				.otherwise()	
					.log("Other message")
				.endChoice()
			
			.end()

			.marshal(jsonBusinessMessageDataFormat)  // remark bila rejection

			// .process(proxyResolutionResponseProcessor)
			.log("Response mock: ${body}")
			.removeHeaders("*")
		;
		
	}

}
