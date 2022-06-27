package bifast.mock.processor;

import java.util.HashMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.pacs002.AccountIdentification4Choice;
import bifast.library.iso20022.pacs002.CashAccount38;
import bifast.library.iso20022.pacs002.CashAccountType2Choice;
import bifast.library.iso20022.pacs002.GenericAccountIdentification1;
import bifast.mock.isoservice.SettlementHeaderService;
import bifast.mock.isoservice.SettlementMessageService;
import bifast.mock.persist.MockPacs002;
import bifast.mock.persist.MockPacs002Repository;

@Component
public class SettlementProcessor implements Processor {
	@Autowired SettlementMessageService settlementMesgService;
	@Autowired SettlementHeaderService settlementHdrService;

	@Autowired
	private MockPacs002Repository mockPacs002Repo;
	@Autowired
	private UtilService utilService;

	@Override
	public void process(Exchange exchange) throws Exception {
	
		
//		BusinessMessage in = exchange.getMessage().getHeader("hdr_ctresponse", BusinessMessage.class);
		BusinessMessage ctReq = exchange.getMessage().getHeader("hdr_ctrequest", BusinessMessage.class);
		

		String bizMsgId = utilService.genRfiBusMsgId("010", "02", "INDOIDJA");
		String msgId = utilService.genMessageId("010", "INDOIDJA");

		BusinessMessage settlementConf = new BusinessMessage();
		
		settlementConf.setAppHdr(settlementHdrService.getAppHdr("010", bizMsgId));
		
		settlementConf.setDocument(new Document());
		settlementConf.getDocument().setFiToFIPmtStsRpt(settlementMesgService.SettlementConfirmation(msgId, ctReq));
		
//		in.getAppHdr().setBizMsgIdr(bizMsgId);
//		in.getAppHdr().setBizSvc("STTL");
//		
//
//		String cdtrAcct = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct().getId().getOthr().getId();
//		
//		CashAccount38 cdtrAcctT = new CashAccount38();
//		cdtrAcctT.setId(new AccountIdentification4Choice());
//		cdtrAcctT.getId().setOthr(new GenericAccountIdentification1());
//		cdtrAcctT.getId().getOthr().setId(cdtrAcct);
//
//		cdtrAcctT.setTp(new CashAccountType2Choice());
//		cdtrAcctT.getTp().setPrtry("CACC");
//
//		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().setCdtrAcct(cdtrAcctT);
//
//		String dbtrAcct = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAcct().getId().getOthr().getId();
//		
//		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().setDbtrAcct(new CashAccount38());
//		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().getDbtrAcct().setId(new AccountIdentification4Choice());
//		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().getDbtrAcct().getId().setOthr(new GenericAccountIdentification1());
//		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().getDbtrAcct().getId().getOthr().setId(dbtrAcct);
//		
//		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().getDbtrAcct().setTp(new CashAccountType2Choice());
//		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().getDbtrAcct().getTp().setPrtry("CACC");
//		
//		String dbtrAgt = in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().getDbtrAgt().getFinInstnId().getOthr().getId();
//		String cdtrAgt = in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlTxRef().getCdtrAgt().getFinInstnId().getOthr().getId();
//		
//		System.out.println("Settlement dbtrAgt: " + dbtrAgt);
//		System.out.println("Settlement cdtrAgt: " + cdtrAgt);
//		
//		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).setTxSts("ACSC");
//
//		in.getDocument().getFiToFIPmtStsRpt().getGrpHdr().setMsgId(msgId);
//		
//		XMLGregorianCalendar orgnlDateTime = in.getDocument().getFiToFIPmtStsRpt().getGrpHdr().getCreDtTm();
//		in.getDocument().getFiToFIPmtStsRpt().getOrgnlGrpInfAndSts().get(0).setOrgnlCreDtTm(orgnlDateTime);
//
//		
//
////		if (in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getSplmtryData().size() == 0 ) {
////		
////			BISupplementaryData1 supplData = new BISupplementaryData1();
////			in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getSplmtryData().add(supplData);
////			
////			BISupplementaryDataEnvelope1 dataEnvl = new BISupplementaryDataEnvelope1();
////			in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getSplmtryData().get(0).setEnvlp(dataEnvl);
////		}
//		
////		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getSplmtryData().get(0).getEnvlp().setCdtrAgtAcct(cdtrAgtAcct);
////		in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getSplmtryData().get(0).getEnvlp().setDbtrAgtAcct(dbtrAgtAcct);
//
		exchange.getMessage().setBody(settlementConf);

		
		ObjectMapper map = new ObjectMapper();
		map.registerModule(new JaxbAnnotationModule());
		map.enable(SerializationFeature.WRAP_ROOT_VALUE);
	    map.setSerializationInclusion(Include.NON_NULL);

		HashMap<String, String> frTable = exchange.getMessage().getHeader("sttl_tableqry", HashMap.class);

		String strSttl = map.writeValueAsString(settlementConf);
		
		String msgName = frTable.get("ORGNL_MSG_NAME");
//		String msgName = ctRequest.getAppHdr().getMsgDefIdr();
		
		MockPacs002 pacs002 = new MockPacs002();
		pacs002.setBizMsgIdr(bizMsgId);
		pacs002.setFullMessage(strSttl);
		
		pacs002.setOrgnlEndToEndId(frTable.get("ORGNL_END_TO_END_ID"));
		pacs002.setOrgnlMsgId(frTable.get("ORGNL_MSG_ID"));
//		pacs002.setOrgnlEndToEndId(in.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlEndToEndId());
//		pacs002.setOrgnlMsgId(in.getDocument().getFiToFIPmtStsRpt().getOrgnlGrpInfAndSts().get(0).getOrgnlMsgId());

		pacs002.setOrgnlMsgName(msgName);
		pacs002.setSttl("DONE");
		pacs002.setResult("ACTC");

		pacs002.setTrxType("STTL");
		mockPacs002Repo.save(pacs002);

		
		if (ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().size()>0) {
			String addInfo = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().get(0);
			if (addInfo.equals("REVERSAL"))
				exchange.getMessage().setHeader("hdr_reversal", "YES");
			else
				exchange.getMessage().setHeader("hdr_reversal", "NO");
		}
			
	}

}
