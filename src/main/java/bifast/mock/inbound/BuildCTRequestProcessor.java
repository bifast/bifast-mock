package bifast.mock.inbound;

import java.math.BigDecimal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.pacs002.PaymentTransaction110;
import bifast.mock.inbound.pojo.PaymentRequestPojo;
import bifast.mock.isoservice.MsgHeaderService;
import bifast.mock.isoservice.Pacs008MessageService;
import bifast.mock.isoservice.Pacs008Seed;
import bifast.mock.processor.UtilService;

@Component
public class BuildCTRequestProcessor implements Processor {

	@Autowired
	private MsgHeaderService appHeaderService;
	@Autowired
	private Pacs008MessageService pacs008MessageService;
	@Autowired
	private UtilService utilService;

	private static Logger logger = LoggerFactory.getLogger(BuildCTRequestProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		logger.info("BuildCTRequestProcessor ");

		PaymentRequestPojo inbRequest = exchange.getMessage().getHeader("hdr_pymtreq", PaymentRequestPojo.class);
		BusinessMessage aeResponse = exchange.getProperty("aeresponse", BusinessMessage.class);
		
		PaymentTransaction110 crdtInfo = aeResponse.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0);
		Pacs008Seed seedCreditTrn = new Pacs008Seed();

		
		String msgType = "010";
			
		BusinessApplicationHeaderV01 hdr = new BusinessApplicationHeaderV01();

		String bizMsgId = utilService.genRfiBusMsgId("010", "01", "BMNDIDJA" );
		
		String msgId = utilService.genMessageId("010", "BMNDIDJA");

		seedCreditTrn.setMsgId(msgId);
		seedCreditTrn.setAmount(new BigDecimal(inbRequest.getAmount()));
		seedCreditTrn.setBizMsgId(bizMsgId);
		
		seedCreditTrn.setCategoryPurpose("01");
		
		seedCreditTrn.setChannel("01");
		
		seedCreditTrn.setCrdtAccountNo(inbRequest.getCreditorAccountNo());		
		seedCreditTrn.setCrdtAccountType(crdtInfo.getOrgnlTxRef().getCdtrAcct().getTp().getPrtry());
		seedCreditTrn.setCrdtName(crdtInfo.getOrgnlTxRef().getCdtr().getPty().getNm());
		
		if (crdtInfo.getSplmtryData().size()>0) {
			seedCreditTrn.setCrdtId(crdtInfo.getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getId());
			seedCreditTrn.setCrdtType(crdtInfo.getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getTp());
			seedCreditTrn.setCrdtResidentStatus(crdtInfo.getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getRsdntSts());
			seedCreditTrn.setCrdtTownName(crdtInfo.getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getTwnNm());
		}
		
		seedCreditTrn.setDbtrAccountNo(inbRequest.getDebtorAccountNo());
		seedCreditTrn.setDbtrAccountType("SVGS");
		seedCreditTrn.setDbtrName(inbRequest.getDebtorName());
		seedCreditTrn.setDbtrId("9999333339");
		seedCreditTrn.setDbtrType("01"); 
		seedCreditTrn.setDbtrResidentStatus("01");
		seedCreditTrn.setDbtrTownName("0300");

		seedCreditTrn.setOrignBank("BMNDIDJA");
		seedCreditTrn.setPaymentInfo("");
		seedCreditTrn.setRecptBank("SIHBIDJ1");
		
		seedCreditTrn.setPaymentInfo(inbRequest.getPaymentInfo());
		seedCreditTrn.setTrnType("010");
		
		BusinessMessage busMsg = new BusinessMessage();

		hdr = appHeaderService.getAppHdr("pacs.008.001.08", bizMsgId);
		busMsg.setAppHdr(hdr);

		Document doc = new Document();
		doc.setFiToFICstmrCdtTrf(pacs008MessageService.creditTransferRequest(seedCreditTrn));
		
		busMsg.setDocument(doc);

		exchange.getIn().setBody(busMsg);
		
	}

}
