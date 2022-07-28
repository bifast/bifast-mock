package bifast.mock.processor;

import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.pacs002.FIToFIPaymentStatusReportV10;
import bifast.mock.isoservice.MsgHeaderService;
import bifast.mock.isoservice.Pacs002MessageService;
import bifast.mock.isoservice.Pacs002Seed;
import bifast.mock.persist.CTResponse;

@Service
public class CreditResponseService {
	@Autowired private MsgHeaderService hdrService;
	@Autowired private Pacs002MessageService pacs002Service;
	@Autowired private UtilService utilService;

//    private static Logger logger = LoggerFactory.getLogger(CreditTransferResponseProcessor.class);

	BusinessMessage buildBusinessMessage (BusinessMessage bmInput, CTResponse ctResponse) throws Exception {
		
		String bizMsgId = utilService.genRfiBusMsgId("010", "02", bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
		String msgId = utilService.genMessageId("010", bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());

		Pacs002Seed seed = new Pacs002Seed();
		seed.setMsgId(msgId);

		seed.setStatus(ctResponse.getResponse());
		seed.setReason(ctResponse.getReason());
		seed.setCreditorName(ctResponse.getCreditorName());
		
		if ((null != bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf()) &&
			(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().size() > 0))
				seed.setAdditionalInfo(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().get(0));

		if (bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().size()>0) {

			if (null != bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().get(0).getEnvlp().getDtl().getCdtr()) {
			
				if (null != bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getTp()) {
					String crdtType = bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getTp();
					seed.setCreditorType(crdtType);
					if (crdtType.equals("01"))
						seed.setCreditorId(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr().getId().getPrvtId().getOthr().get(0).getId());
					else
						seed.setCreditorId(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr().getId().getOrgId().getOthr().get(0).getId());
					
				}
				
				if (null != bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getTwnNm()) 
					seed.setCreditorTown(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getTwnNm());

			}
		}
		
		seed.setCreditorResidentialStatus("01");

		FIToFIPaymentStatusReportV10 response = pacs002Service.creditTransferRequestResponse(seed, bmInput);
		
		GregorianCalendar gcal = new GregorianCalendar();
		XMLGregorianCalendar xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		response.getTxInfAndSts().get(0).getOrgnlTxRef().setIntrBkSttlmDt(xcal);

		BusinessApplicationHeaderV01 hdr = new BusinessApplicationHeaderV01();
		hdr = hdrService.getAppHdr("pacs.002.001.10", bizMsgId);
		hdr.setBizSvc("CLEAR");

		BusinessMessage busMesg = new BusinessMessage();
		Document doc = new Document();
		doc.setFiToFIPmtStsRpt(response);

		busMesg.setAppHdr(hdr);
		busMesg.setDocument(doc);

		return busMesg;
	}

}
