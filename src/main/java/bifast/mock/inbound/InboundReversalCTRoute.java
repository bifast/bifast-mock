package bifast.mock.inbound;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.pacs008.BISupplementaryData1;
import bifast.library.iso20022.pacs008.BISupplementaryDataEnvelope1;
import bifast.library.iso20022.pacs008.BranchAndFinancialInstitutionIdentification6;
import bifast.library.iso20022.pacs008.CashAccount38;
import bifast.library.iso20022.pacs008.PartyIdentification135;
import bifast.library.iso20022.pacs008.SupplementaryDataEnvelope1;
import bifast.mock.inbound.pojo.CTResponsePojo;
import bifast.mock.inbound.pojo.PaymentRequestPojo;
import bifast.mock.inbound.pojo.RevCTPojo;
import bifast.mock.persist.CTResponse;
import bifast.mock.persist.CTResponseRepository;
import bifast.mock.processor.UtilService;

@Component
public class InboundReversalCTRoute extends RouteBuilder{
	@Autowired CTResponseRepository ctResponseRepo;
	@Autowired UtilService utilService;

	JacksonDataFormat busMesgJDF = new JacksonDataFormat(BusinessMessage.class);

	@Override
	public void configure() throws Exception {
	
		busMesgJDF.addModule(new JaxbAnnotationModule());  //supaya nama element pake annot JAXB (uppercasecamel)
		busMesgJDF.enableFeature(DeserializationFeature.UNWRAP_ROOT_VALUE);
		busMesgJDF.enableFeature(SerializationFeature.WRAP_ROOT_VALUE);
		busMesgJDF.setInclude("NON_NULL");
		busMesgJDF.setInclude("NON_EMPTY");


		from("direct:reversalct").routeId("reversal")
			.setExchangePattern(ExchangePattern.InOnly)
			.log("start direct:reversalct")

			.setHeader("hdr_revreq", simple("${body}"))
			
			.log("${body.endToEndId}")

			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					RevCTPojo revCt = exchange.getMessage().getBody(RevCTPojo.class);
					Optional<CTResponse> oCTResponse = ctResponseRepo.findByEndToEndId(revCt.getEndToEndId());
					if (oCTResponse.isPresent()) {
						CTResponse ctResponse = oCTResponse.get();
						String strCTReq = ctResponse.getJsonCtRequest();
						BusinessMessage ctReq = utilService.deserializeBusinessMessage(strCTReq);
						
						String bizmsgidr = utilService.genHubBusMsgId("011");
						
						BranchAndFinancialInstitutionIdentification6 cdtrAgt = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAgt();
						PartyIdentification135 cdtr = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr();
						CashAccount38 cdtrAcct = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct();
						
						ctReq.getAppHdr().setBizMsgIdr(bizmsgidr);
						ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getPmtId().setClrSysRef("001");
						
						ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setCdtr(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtr());
						ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setCdtrAcct(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAcct());
						ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setCdtrAgt(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAgt());
						
						ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setDbtr(cdtr);
						ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setDbtrAcct(cdtrAcct);
						ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setDbtrAgt(cdtrAgt);
						
						BISupplementaryData1 splmtryData = new BISupplementaryData1();
						splmtryData.setEnvlp(new SupplementaryDataEnvelope1());
						splmtryData.getEnvlp().setDtl(new BISupplementaryDataEnvelope1());
						splmtryData.getEnvlp().getDtl().setRltdEndToEndId(revCt.getEndToEndId());
						ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().add(splmtryData);
						
						exchange.getMessage().setBody(ctReq);
					}
				}
			})
			.marshal(busMesgJDF)
			.log("${body}")

			.to("rest:post:?host={{komi.inbound-url}}"
					+ "&exchangePattern=InOnly&bridgeEndpoint=true")

			.marshal(busMesgJDF)
			.log("${body}")

			;
	}

}
