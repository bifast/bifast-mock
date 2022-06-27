package bifast.mock.processor;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.pacs002.FIToFIPaymentStatusReportV10;
import bifast.mock.isoservice.MsgHeaderService;
import bifast.mock.isoservice.Pacs002MessageService;
import bifast.mock.isoservice.Pacs002Seed;
import bifast.mock.persist.AccountProxy;
import bifast.mock.persist.AccountProxyRepository;

@Component
public class AccountEnquiryResponseProcessor implements Processor {
	@Autowired AccountProxyRepository accountRepo;
	@Autowired
	private MsgHeaderService hdrService;
	@Autowired
	private Pacs002MessageService pacs002Service;
	@Autowired
	private UtilService utilService;
	
	@Override
	public void process(Exchange exchange) throws Exception {


		BusinessMessage msg = exchange.getIn().getBody(BusinessMessage.class);
		String bizMsgId = utilService.genRfiBusMsgId("510", "02", msg.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
		String msgId = utilService.genMessageId("510", msg.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());

		String acctNo = msg.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct().getId().getOthr().getId();
//		String bank = msg.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId();
		String bank = msg.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAgt().getFinInstnId().getOthr().getId();
		exchange.getMessage().setHeader("hdr_account_no", acctNo);

		AccountProxy account = null;
		Optional<AccountProxy> oAcct = accountRepo.findByAccountNumberAndRegisterBank(acctNo, bank);
		
		Pacs002Seed seed = new Pacs002Seed();
		seed.setMsgId(msgId);
		seed.setCreditorAccountNo(acctNo);

		if (oAcct.isPresent()) {
			account = oAcct.get();
			
			seed.setStatus("ACTC");
			seed.setReason("U000");				
			
			seed.setCreditorName(account.getAccountName());
			seed.setCreditorAccountIdType(account.getAccountType());
			seed.setCreditorType(account.getCstmrTp());
			seed.setCreditorId(account.getCstmrId());
			seed.setCreditorTown(account.getCstmrTwnNm());
			seed.setCreditorResidentialStatus(account.getCstmrRsdntSts());
		}
		
		else {
			seed.setStatus("RJCT");
			seed.setReason("U101");
			seed.setCreditorAccountIdType("OTHR");
		}
		
		
		FIToFIPaymentStatusReportV10 response = pacs002Service.accountEnquiryResponse(seed, msg);
		
		BusinessApplicationHeaderV01 hdr = new BusinessApplicationHeaderV01();
		hdr = hdrService.getAppHdr("pacs.002.001.10", bizMsgId);

		Document doc = new Document();
		doc.setFiToFIPmtStsRpt(response);
		
		BusinessMessage busMesg = new BusinessMessage();
		busMesg.setAppHdr(hdr);
		busMesg.setDocument(doc);
		
		exchange.getMessage().setBody(busMesg);
		
	}

}
