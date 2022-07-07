package bifast.mock.isoapt;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.mock.isoapt.pojo.AccountEnquiryRequest;
import bifast.mock.isoapt.pojo.AccountEnquiryResponse;
import bifast.mock.isoapt.pojo.CIFRequest;
import bifast.mock.isoapt.pojo.CIFResponse;
import bifast.mock.isoapt.pojo.CreditRequest;
import bifast.mock.isoapt.pojo.CreditResponse;
import bifast.mock.isoapt.pojo.DebitRequest;
import bifast.mock.isoapt.pojo.DebitResponse;
import bifast.mock.isoapt.pojo.SettlementRequest;
import bifast.mock.isoapt.pojo.SettlementResponse;
import bifast.mock.isoapt.processor.AccountEnquiryProcessor;
import bifast.mock.isoapt.processor.CIFProcessor;
import bifast.mock.isoapt.processor.CreditRequestProcessor;
import bifast.mock.isoapt.processor.DebitRequestProcessor;


@Component
public class IsoAdapterRoute extends RouteBuilder {
	@Autowired private CIFProcessor cifPrc;
	@Autowired private CreditRequestProcessor creditRequestPrc;
	@Autowired private DebitRequestProcessor debitRequestPrc;
	@Autowired private AccountEnquiryProcessor accountEnquiryPrc;
	@Autowired private SettlementRequestProcessor settlementRequestPrc;

	JacksonDataFormat aeRequestJDF = new JacksonDataFormat(AccountEnquiryRequest.class);
	JacksonDataFormat aeResponseJDF = new JacksonDataFormat(AccountEnquiryResponse.class);
	JacksonDataFormat cifRequestJDF = new JacksonDataFormat(CIFRequest.class);
	JacksonDataFormat cifResponseJDF = new JacksonDataFormat(CIFResponse.class);
	JacksonDataFormat debitRequestJDF = new JacksonDataFormat(DebitRequest.class);
	JacksonDataFormat debitResponseJDF = new JacksonDataFormat(DebitResponse.class);
	JacksonDataFormat creditRequestJDF = new JacksonDataFormat(CreditRequest.class);
	JacksonDataFormat creditResponseJDF = new JacksonDataFormat(CreditResponse.class);
	JacksonDataFormat settlementRequestJDF = new JacksonDataFormat(SettlementRequest.class);
	JacksonDataFormat settlementResponseJDF = new JacksonDataFormat(SettlementResponse.class);

	@Override
	public void configure() throws Exception {
		
		cifRequestJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		cifRequestJDF.setInclude("NON_NULL");
		cifRequestJDF.setInclude("NON_EMPTY");
		cifResponseJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		cifResponseJDF.setInclude("NON_NULL");
		cifResponseJDF.setInclude("NON_EMPTY");

		debitRequestJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		debitRequestJDF.setInclude("NON_NULL");
		debitRequestJDF.setInclude("NON_EMPTY");
		debitResponseJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		debitResponseJDF.setInclude("NON_NULL");
		debitResponseJDF.setInclude("NON_EMPTY");
		
		creditRequestJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		creditRequestJDF.setInclude("NON_NULL");
		creditRequestJDF.setInclude("NON_EMPTY");
		creditResponseJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		creditResponseJDF.setInclude("NON_NULL");
		creditResponseJDF.setInclude("NON_EMPTY");
		
		settlementRequestJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		settlementRequestJDF.setInclude("NON_NULL");
		settlementRequestJDF.setInclude("NON_EMPTY");
		settlementResponseJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		settlementResponseJDF.setInclude("NON_NULL");
		settlementResponseJDF.setInclude("NON_EMPTY");

		restConfiguration()
			.component("servlet")
		;
		
		rest("/adapter")
			.post("/emailphonelist")
				.consumes("application/json")
				.to("direct:cif")

			.post("/accountinquiry")
				.consumes("application/json")
				.to("direct:accountenquiry")
				
			.post("/debit")
				.consumes("application/json")
				.to("direct:debit")
				
			.post("/debitreversal")
				.consumes("application/json")
				.to("direct:debitreversal")
				
			.post("/credit")
				.consumes("application/json")
				.to("direct:credit")
				
			.post("/settlement")
				.consumes("application/json")
				.to("direct:settlement")
		;

		from("direct:cif").routeId("cb.cif")
			.convertBodyTo(String.class)
			.log("CustInfo request: ${body}")
			.unmarshal(cifRequestJDF)
			.process(cifPrc)
			.marshal(cifResponseJDF)
			;

		from("direct:accountenquiry").routeId("cb.accountenquiry")
			.convertBodyTo(String.class)
			.log("AccountEnquiry: ${body}")
			.unmarshal(aeRequestJDF)
			.process(accountEnquiryPrc)
			.marshal(aeResponseJDF)
			;
		
		from("direct:debit").routeId("cb.debit")
			.convertBodyTo(String.class)
			.log("Debit request: ${body}")
			.unmarshal(debitRequestJDF)
			.process(debitRequestPrc)
			.marshal(debitResponseJDF)
			;

		from("direct:debitreversal").routeId("cb.debitreversal")
			.convertBodyTo(String.class)
			.log("DebitReversal request: ${body}")
			.unmarshal(debitRequestJDF)
			.process(debitRequestPrc)
			.marshal(debitResponseJDF)
			;

		from("direct:credit").routeId("cb.credit")
			.convertBodyTo(String.class)
			.log("Credit request: ${body}")
			.unmarshal(creditRequestJDF)
			.process(creditRequestPrc)
			.marshal(creditResponseJDF)
			.log("Credit response: ${body}")

			;

		from("direct:settlement").routeId("cb.settlement")
			.convertBodyTo(String.class)
			.log("Settlement: ${body}")
			.unmarshal(settlementRequestJDF)
			.process(settlementRequestPrc)
			.marshal(settlementResponseJDF)
			;

	}

}
