package bifast.mock.incomingtrns;

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
import bifast.mock.inbound.BuildReversalRequestProcessor;
import bifast.mock.persist.CTResponseRepository;
import bifast.mock.processor.UtilService;

@Component
public class InboundReversalCTRoute extends RouteBuilder{
	@Autowired BuildReversalRequestProcessor buildReversalRequestProc;
	@Autowired CTResponseRepository ctResponseRepo;
	@Autowired UtilService utilService;

	JacksonDataFormat busMesgJDF = new JacksonDataFormat(BusinessMessage.class);
	JacksonDataFormat revResponseJDF = new JacksonDataFormat(RevCTResponseDTO.class);

	@Override
	public void configure() throws Exception {
	
		busMesgJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		busMesgJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
		busMesgJDF.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		busMesgJDF.setInclude("NON_NULL");
		busMesgJDF.setInclude("NON_EMPTY");

		revResponseJDF.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		revResponseJDF.setInclude("NON_NULL");
		revResponseJDF.setInclude("NON_EMPTY");

		
		onException(bifast.mock.incomingtrns.RevCTNotFoundException.class)
			.log(LoggingLevel.ERROR, "${exception.stacktrace}")
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					RevCTResponseDTO resp = new RevCTResponseDTO();
					resp.setEndToEndId(exchange.getMessage().getHeader("revct_e2eid", String.class));
					resp.setResponse("RJCT");
					exchange.getMessage().setBody(resp);
				}
			})
			.marshal(revResponseJDF)
			.removeHeaders("revct_*")
			.handled(true);

		from("direct:reversalct").routeId("reversal")
			.log("start direct:reversalct")

			.setHeader("revct_e2eid", simple("${body.endToEndId}"))

			.process(buildReversalRequestProc)
			.setHeader("inb_ctRequest", simple("${body}"))

			.marshal(busMesgJDF)
			.log("Reversal request: ${body}")

			.to("rest:post:?host={{komi.inbound-url}}"
					+ "&exchangePattern=InOnly&bridgeEndpoint=true")
			.convertBodyTo(String.class)
			.log("Reversal response: ${body}")
			.unmarshal(busMesgJDF)
			
			.filter().simple("${body.document.fiToFIPmtStsRpt.txInfAndSts[0].txSts} == 'ACTC'")
				.log("akan kirim settlement")
				.setExchangePattern(ExchangePattern.InOnly)
				.to("seda:inb_settlement")
			.end()

			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					BusinessMessage revCtResp = exchange.getMessage().getBody(BusinessMessage.class);
					revCtResp.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts();
					RevCTResponseDTO resp = new RevCTResponseDTO();
					resp.setEndToEndId(exchange.getMessage().getHeader("revct_e2eid", String.class));
					resp.setResponse(revCtResp.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
					exchange.getMessage().setBody(resp);
				}
			})
			.marshal(revResponseJDF)

			.log("${body}")

			;
	}

}
