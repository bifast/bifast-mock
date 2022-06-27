package bifast.mock.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.mock.persist.MockPacs002;
import bifast.mock.persist.MockPacs002Repository;

@Component
public class CreditResponseStoreProcessor implements Processor {

    @Autowired
    private MockPacs002Repository mockPacs002Repo;

    @Override
    public void process(Exchange exchange) throws Exception {

        BusinessMessage objRequest = exchange.getMessage().getHeader("objRequest", BusinessMessage.class);		
        BusinessMessage responseMsg = exchange.getMessage().getHeader("hdr_ctResponseObj", BusinessMessage.class);
        String fullMsg = exchange.getMessage().getBody(String.class);

        // simpan sbg history
			MockPacs002 pacs002 = new MockPacs002();
			pacs002.setBizMsgIdr(responseMsg.getAppHdr().getBizMsgIdr());
	
			pacs002.setFullMessage(fullMsg);
			
	        pacs002.setOrgnlEndToEndId(responseMsg.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlEndToEndId());
			
	        pacs002.setOrgnlMsgId(responseMsg.getDocument().getFiToFIPmtStsRpt().getOrgnlGrpInfAndSts().get(0).getOrgnlMsgId());
			
	        String orgnlMsgName = responseMsg.getDocument().getFiToFIPmtStsRpt().getOrgnlGrpInfAndSts().get(0).getOrgnlMsgNmId();
	        pacs002.setOrgnlMsgName(orgnlMsgName);
	        pacs002.setResult(responseMsg.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
	        pacs002.setCdtrAcct(objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct().getId().getOthr().getId());
	        pacs002.setDbtrAcct(objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAcct().getId().getOthr().getId());
	        
            pacs002.setTrxType("CLEAR");

	        mockPacs002Repo.save(pacs002);
        
    }
    
}
