package bifast.mock.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.mock.isoservice.SettlementHeaderService;
import bifast.mock.isoservice.SettlementMessageService;

@Component
public class SettlementProc implements Processor {
	@Autowired SettlementMessageService settlementMesgService;
	@Autowired SettlementHeaderService settlementHdrService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		BusinessMessage ctReq = exchange.getProperty("ctRequest", BusinessMessage.class);
		
		BusinessMessage settlementConf = new BusinessMessage();
		
		settlementConf.setAppHdr(settlementHdrService.getAppHdr());
		settlementConf.setDocument(new Document());
		settlementConf.getDocument().setFiToFIPmtStsRpt(settlementMesgService.SettlementConfirmation(ctReq));
		
		exchange.getMessage().setBody(settlementConf);
	
		if (ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().size()>0) {
			String addInfo = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().get(0).toLowerCase();
			if (addInfo.contains("reversal"))
				exchange.setProperty("prop_reversal", "YES");
			else
				exchange.setProperty("prop_reversal", "NO");
		}
	}

}
