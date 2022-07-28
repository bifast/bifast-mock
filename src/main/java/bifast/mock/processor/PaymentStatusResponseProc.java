package bifast.mock.processor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.pacs002.FIToFIPaymentStatusReportV10;
import bifast.mock.isoservice.MsgHeaderService;
import bifast.mock.isoservice.Pacs002MessageService;
import bifast.mock.isoservice.Pacs002Seed;
import bifast.mock.persist.CTResponse;
import bifast.mock.persist.CTResponseRepository;

@Component
public class PaymentStatusResponseProc implements Processor{

	@Autowired private CTResponseRepository  ctRepo;
	@Autowired private UtilService utilService;
	@Autowired private MsgHeaderService hdrService;
	@Autowired private Pacs002MessageService pacs002Service;
	@Autowired ProducerTemplate producerTemplate;
	@Autowired private CreditTransferResponseProcessor ctResponseProc;

	@Value("${komi.timeout-ps}")
	private int delay;

	@Override
	public void process(Exchange exchange) throws Exception {

		BusinessMessage psRequest = exchange.getMessage().getHeader("objRequest", BusinessMessage.class);
		String reqEndToEndId = psRequest.getDocument().getFIToFIPmtStsReq().getTxInf().get(0).getOrgnlEndToEndId();

		System.out.println("Cari history dengan no OrgnlEndtoend: " + reqEndToEndId);
		Optional<CTResponse> oct = ctRepo.findByEndToEndId(reqEndToEndId);

		if (oct.isPresent()) {
			CTResponse ctResponse = oct.get();
			BusinessMessage ctReq = utilService.deserializeBusinessMessage(ctResponse.getJsonCtRequest());
			
	    	String addInfo = "";
			if (null != ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf()) 
				addInfo = ctReq.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().get(0).toLowerCase();

			if ((addInfo.equals("timeout")) || (addInfo.contains("pstimeout"))) {
				System.out.println("PS delay dulu selama " +  delay + " ms");
				TimeUnit.SECONDS.sleep(delay);
			}

//			producerTemplate.getDefaultEndpoint()
			
			Exchange ex = producerTemplate.send(ctResponseProc);

			BusinessMessage ctResp = ex.getMessage().getBody(BusinessMessage.class);
			exchange.getMessage().setBody(utilService.serializeBusinessMessage(ctResp));
		}

		else  { // nggak ketemu juga 
				System.out.println("ga nemu ah");
				BusinessMessage bm = notFoundCTResponse(psRequest);
				exchange.getMessage().setBody(bm);
		}
		
	}
	
	BusinessMessage notFoundCTResponse (BusinessMessage psRequest) {
		String bizMsgId = utilService.genRfiBusMsgId("010", "02", 
								psRequest.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
		String msgId = utilService.genMessageId("010",
								psRequest.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
		BusinessMessage busMesg = new BusinessMessage();
		
		Pacs002Seed seed = new Pacs002Seed();
		seed.setStatus("RJCT");
		seed.setReason("U106");
		seed.setAdditionalInfo("Credit Transfer Request Not Found");
		seed.setMsgId(msgId);

		FIToFIPaymentStatusReportV10 response = new FIToFIPaymentStatusReportV10();
		try {
			response = pacs002Service.notFoundPaymentStatusResponse(seed, psRequest);

			BusinessApplicationHeaderV01 hdr = new BusinessApplicationHeaderV01();
			hdr = hdrService.getAppHdr("pacs.002.001.10", bizMsgId);
			hdr.setBizSvc("CLEAR");
			
			Document doc = new Document();
			doc.setFiToFIPmtStsRpt(response);
			busMesg.setAppHdr(hdr);
			busMesg.setDocument(doc);

		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return busMesg;

	}

}
