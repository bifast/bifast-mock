package bifast.mock.route;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class TransactionRoute extends RouteBuilder {
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
			.log(LoggingLevel.ERROR, "onException")
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
		            Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
					System.out.println(caused.getMessage());
				}
			})
			.log("${exchangeProperty.Exchange.EXCEPTION_CAUGHT}")
			.handled(true)
		;
		
		from("direct:transaction").routeId("transaction")

		.setExchangePattern(ExchangePattern.InOut)
		.convertBodyTo(String.class)
		.log("Terima: ${body}")
		.unmarshal(jsonBusinessMessageDataFormat)
		.setHeader("objRequest", simple("${body}"))
		
		.process(checkMessageTypeProcessor)
		.log("Message Type: ${header.msgType}")

		.setHeader("delay_ae", simple("{{komi.timeout-ae}}"))
		.setProperty("delay_ps", simple("{{komi.timeout-ps}}"))

		.choice()

			.when().simple("${header.msgType} == 'AccountEnquiryRequest'")
				.log("Akan process AE")
				.process(accountEnquiryResponseProcessor)
				.log("selesai process AE")
			.endChoice()

			.when().simple("${header.msgType} == 'CreditTransferRequest'")
				.log("Akan process CT")
				
				.process(creditTransferResponseProcessor)

				.setHeader("hdr_ctResponseObj",simple("${body}"))		
				
			.endChoice()

			.when().simple("${header.msgType} == 'PaymentStatusRequest'")
				.log("Akan proses paymentStatusResponseProcessor")
				.process(paymentStatusResponseProcessor)
				
				.filter().simple("${body} == null")
					.log("ga nemu payment status")
				.end()
			.endChoice()
			
			.when().simple("${header.msgType} == 'ReverseCreditTransferRequest'")
				.log("akan proses creditTransferResponseProcessor")
				.process(creditTransferResponseProcessor)
			.endChoice()

			.otherwise()	
				.log("Other message")
			.endChoice()
		
		.end()

		
		// .process(proxyResolutionResponseProcessor)
		.marshal(jsonBusinessMessageDataFormat)
		.log("Response mock: ${body}")
		.unmarshal(jsonBusinessMessageDataFormat)
		.removeHeaders("*")
	;		
	}

}
