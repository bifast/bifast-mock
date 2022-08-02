package bifast.mock.inbound;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.pacs008.BranchAndFinancialInstitutionIdentification6;
import bifast.library.iso20022.pacs008.CashAccount38;
import bifast.library.iso20022.pacs008.PartyIdentification135;
import bifast.mock.incomingtrns.RevCTNotFoundException;
import bifast.mock.incomingtrns.RevCTRequestDTO;
import bifast.mock.persist.CTResponse;
import bifast.mock.persist.CTResponseRepository;
import bifast.mock.processor.UtilService;

@Component
public class BuildReversalRequestProcessor implements Processor {

	@Autowired private CTResponseRepository ctResponseRepo;
	@Autowired private UtilService utilService;

//	private static Logger logger = LoggerFactory.getLogger(BuildReversalRequestProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		RevCTRequestDTO revCt = exchange.getMessage().getBody(RevCTRequestDTO.class);
		Optional<CTResponse> oCTResponse = ctResponseRepo.findByEndToEndId(revCt.getEndToEndId());
		if (oCTResponse.isPresent()) {
			CTResponse ctResponse = oCTResponse.get();
			String strCTReq = ctResponse.getJsonCtRequest();
			BusinessMessage ctReq = utilService.deserializeBusinessMessage(strCTReq);
			
			String bizmsgidr = utilService.genHubBusMsgId("011");
			ctReq.getAppHdr().setBizMsgIdr(bizmsgidr);
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getPmtId().setEndToEndId(bizmsgidr);
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getPmtId().setClrSysRef("001");
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().get(0).getEnvlp().getDtl().setRltdEndToEndId(revCt.getEndToEndId());
			
			BranchAndFinancialInstitutionIdentification6 cdtrAgt = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAgt();
			PartyIdentification135 cdtr = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr();
			CashAccount38 cdtrAcct = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct();
			
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setCdtr(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtr());
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setCdtrAcct(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAcct());
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setCdtrAgt(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAgt());
			
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setDbtr(cdtr);
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setDbtrAcct(cdtrAcct);
			ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).setDbtrAgt(cdtrAgt);
			
			exchange.getMessage().setBody(ctReq);
		}
		
		else {
			throw new RevCTNotFoundException("CreditTransfer Not Found.") ;
		}
		
	}

}
