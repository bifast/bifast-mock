package bifast.mock.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import bifast.library.iso20022.custom.BusinessMessage;

@Component
public class BuildReversal implements Processor{
	@Autowired private UtilService utilService;

	@Override
	public void process(Exchange exchange) throws Exception {

		BusinessMessage ctReq = exchange.getMessage().getBody(BusinessMessage.class);
		
	    ObjectMapper objectMapper = new ObjectMapper();

		BusinessMessage ctReversal = objectMapper.readValue(objectMapper.writeValueAsString(ctReq), BusinessMessage.class);;

		String bicFrom = ctReq.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId();
		String bicTo = ctReq.getAppHdr().getFr().getFIId().getFinInstnId().getOthr().getId();
		String orgnlTxId = ctReq.getDocument().getFiToFICstmrCdtTrf().getGrpHdr().getMsgId();
		
		System.out.print("bicFr: " + bicFrom);
		System.out.print("bicTo: " + bicTo);
	
		String bizMsgId = utilService.genRfiBusMsgId("011", "02", bicFrom);
		String msgId = utilService.genMessageId("011", bicFrom);

//		ctReversal.getAppHdr().setMsgDefIdr("pacs.008.001.08");
		
		System.out.println("bizMsgId: " + orgnlTxId);
		ctReversal.getAppHdr().setBizMsgIdr(bizMsgId);
		ctReversal.getDocument().getFiToFICstmrCdtTrf().getGrpHdr().setMsgId(msgId);
		
		ctReversal.getAppHdr().getFr().getFIId().getFinInstnId().getOthr().setId(bicFrom);
		ctReversal.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().setId(bicTo);

		ctReversal.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getPmtId().setEndToEndId(bizMsgId);

		String orgnlBizmsgid = ctReq.getAppHdr().getBizMsgIdr();
		ctReversal.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getPmtId().setTxId(orgnlBizmsgid);
		
		ctReversal.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0)
			.setDbtr(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr());
		
		ctReversal.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0)
			.setCdtr(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtr());
		
		ctReversal.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0)
			.setDbtrAcct(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct());

		ctReversal.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0)
			.setCdtrAcct(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAcct());
		
		ctReversal.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0)
			.setCdtrAgt(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAgt());

		ctReversal.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0)
			.setDbtrAgt(ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAgt());
		
		exchange.getMessage().setBody(ctReversal);
	}

}
