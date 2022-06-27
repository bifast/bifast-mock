package bifast.mock.inbound;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.mock.isoservice.SettlementHeaderService;
import bifast.mock.isoservice.SettlementMessageService;
import bifast.mock.processor.UtilService;

@Component
public class BuildSttlProcessor implements Processor {

	@Autowired private SettlementHeaderService sttlHeaderService;
	@Autowired private SettlementMessageService sttlBodyService;
	@Autowired private UtilService utilService;

	@Override
	public void process(Exchange exchange) throws Exception {
	
		BusinessMessage ctReq = exchange.getMessage().getHeader("inb_ctRequest", BusinessMessage.class);
		
		String bizMsgId = utilService.genRfiBusMsgId("010", "02", "INDOIDJA");
		String msgId = utilService.genMessageId("010", "INDOIDJA");

		BusinessMessage settlementConf = new BusinessMessage();
		
		settlementConf.setAppHdr(sttlHeaderService.getAppHdr("010", bizMsgId));
		
		settlementConf.setDocument(new Document());
		settlementConf.getDocument().setFiToFIPmtStsRpt(sttlBodyService.SettlementConfirmation(msgId, ctReq));

		exchange.getMessage().setBody(settlementConf);

		
	}

}
