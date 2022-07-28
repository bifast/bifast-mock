package bifast.mock.inbound;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.mock.isoservice.SettlementHeaderService;
import bifast.mock.isoservice.SettlementMessageService;

@Component
public class BuildSttlProcessor implements Processor {

	@Autowired private SettlementHeaderService sttlHeaderService;
	@Autowired private SettlementMessageService sttlBodyService;

	@Override
	public void process(Exchange exchange) throws Exception {
	
		BusinessMessage ctReq = exchange.getMessage().getHeader("inb_ctRequest", BusinessMessage.class);
		
		BusinessMessage settlementConf = new BusinessMessage();
		
		settlementConf.setAppHdr(sttlHeaderService.getAppHdr());
		
		settlementConf.setDocument(new Document());
		settlementConf.getDocument().setFiToFIPmtStsRpt(sttlBodyService.SettlementConfirmation(ctReq));

		exchange.getMessage().setBody(settlementConf);

		
	}

}
