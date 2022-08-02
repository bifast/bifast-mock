package bifast.mock.processor;

import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import bifast.mock.persist.AccountProxy;
import bifast.mock.persist.AccountProxyRepository;
import bifast.mock.persist.CTResponse;
import bifast.mock.persist.CTResponseRepository;

@Component
public class CreditTransferResponseProcessor implements Processor{
	@Autowired private AccountProxyRepository accountRepo;
	@Autowired private CTResponseRepository CTRepo ;
	@Autowired private MsgHeaderService hdrService;
	@Autowired private CreditResponseService creditResponseService;
	@Autowired private Pacs002MessageService pacs002Service;
	@Autowired private UtilService utilService;
	
	@Value("${komi.timeout-ct}")
	private int delay;

    private static Logger logger = LoggerFactory.getLogger(CreditTransferResponseProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		
		BusinessMessage objRequest = exchange.getMessage().getBody(BusinessMessage.class);	

		exchange.setProperty("ctRequest", objRequest);
		exchange.setProperty("endtoendid", objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getPmtId().getEndToEndId());

    	String addInfo = "";
		if (null != objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf()) 
			addInfo = objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().get(0).toLowerCase();

		String norekCdtr = objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct().getId().getOthr().getId();
		String bank = objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAgt().getFinInstnId().getOthr().getId();
		exchange.getMessage().setHeader("hdr_account_no", norekCdtr);
	
		if (addInfo.contains("admi002")) {
	    	BusinessMessage msg = utilService.deserializeBusinessMessage(admi002());
			exchange.getMessage().setBody(msg);
		}

		else {

			Optional<AccountProxy> oAcct = accountRepo.findByAccountNumberAndRegisterBank(norekCdtr, bank);
			BusinessMessage resultMessg = null;

			CTResponse ctResponse = new CTResponse();
	        ctResponse.setBizSvc("CLEAR");
	        ctResponse.setEndToEndId(exchange.getProperty("endtoendid", String.class));
	        ctResponse.setJsonCtRequest(utilService.serializeBusinessMessage(objRequest));
	        
			if (oAcct.isPresent()) {
				AccountProxy account = oAcct.get();
		        ctResponse.setCreditorName(account.getAccountName());
		        if (account.getAccountStatus().equals("ACTV")) {
		        	ctResponse.setResponse("ACTC");
		        	ctResponse.setReason("U000");				
		        }
		        else {
		        	ctResponse.setResponse("RJCT");
		        	ctResponse.setReason("U102");		
		        }
			} 
			else {
	        	ctResponse.setResponse("RJCT");
	        	ctResponse.setReason("U101");
			}
			
	        CTRepo.save(ctResponse);

			resultMessg = creditResponseService.buildBusinessMessage(objRequest, ctResponse);
				
			if (addInfo.contains("cttimeout")) 
				TimeUnit.SECONDS.sleep(delay);

			exchange.getMessage().setBody(resultMessg);
			
			// kirim settlement jika ctResponse.getResponse = ACTC
			if (ctResponse.getResponse().equals("ACTC")) {
				TimeUnit.SECONDS.sleep(5);
	            FluentProducerTemplate pt = exchange.getContext().createFluentProducerTemplate();
	            pt.withExchange(exchange).to("seda:settlement?exchangePattern=InOnly&timeout=0").asyncSend();
			}
			
		}
	}
	
	BusinessMessage buildBusinessMessage (BusinessMessage bmInput, Optional<AccountProxy> oAccount) throws Exception {
		
		String bizMsgId = utilService.genRfiBusMsgId("010", "02", bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
		String msgId = utilService.genMessageId("010", bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());

		Pacs002Seed seed = new Pacs002Seed();
		seed.setMsgId(msgId);

		if (oAccount.isPresent()) {
			AccountProxy account = oAccount.get();
			logger.info("account " + account + " exists " + account.getAccountStatus());
			if (account.getAccountStatus().equals("ACTV")) {
				seed.setStatus("ACTC");
				seed.setReason("U000");				
				seed.setCreditorName(account.getAccountName());
			}
			else {
				seed.setStatus("RJCT");
				seed.setReason("U102");				
			}
		}
		else {
			seed.setStatus("RJCT");
			seed.setReason("U101");
			seed.setCreditorName(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr().getNm());
		}
		
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
	    
    private String admi002 () {
		String str = "{\n"
				+ "  \"BusMsg\" : {\n"
				+ "    \"AppHdr\" : {\n"
				+ "      \"Fr\" : {\n"
				+ "        \"FIId\" : {\n"
				+ "          \"FinInstnId\" : {\n"
				+ "            \"Othr\" : {\n"
				+ "              \"Id\" : \"FASTIDJA\"\n"
				+ "            }\n"
				+ "          }\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"To\" : {\n"
				+ "        \"FIId\" : {\n"
				+ "          \"FinInstnId\" : {\n"
				+ "            \"Othr\" : {\n"
				+ "              \"Id\" : \"INDOIDJA\"\n"
				+ "            }\n"
				+ "          }\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"BizMsgIdr\" : \"20210301FASTIDJA000ORB12345678\",\n"
				+ "      \"MsgDefIdr\" : \"admi.002.001.01\",\n"
				+ "      \"CreDt\" : \"2021-03-01T15:25:43Z\"\n"
				+ "    },\n"
				+ "    \"Document\" : {\n"
				+ "      \"MessageReject\" : {\n"
				+ "        \"RltdRef\" : {\n"
				+ "          \"Ref\" : \"20210301INDOIDJA0000000000026770339\"\n"
				+ "        },\n"
				+ "        \"Rsn\" : {\n"
				+ "          \"RjctgPtyRsn\" : \"0015\",\n"
				+ "          \"RjctnDtTm\" : \"2021-04-09T21:21:01\",\n"
				+ "          \"ErrLctn\" : \"StaxXmlSubFieldType\",\n"
				+ "          \"RsnDesc\" : \"cvc-complex-type.2.4.a: Invalid content was found starting with element '{\\\"urn:iso:std:iso:20022:tech:xsd:admn.001.001.01\\\":InstrId}'. One of '{\\\"urn:iso:std:iso:20022:tech:xsd:admn.001.001.01\\\":FnctnCd}' is expected.\",\n"
				+ "          \"AddtlData\" : \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\"?>\\n     <ns:BusMsg xmlns:ns=\\\"urn:iso\\\" xmlns:ns1=\\\"urn:iso:std:iso:20022:tech:xsd:admn.001.001.01\\\" xmlns:ns2=\\\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\\\">\\n      <ns:AppHdr>\\n       <ns2:CharSet>ABCD</ns2:CharSet>\\n       <ns2:Fr>\\n        <ns2:FIId>\\n         <ns2:FinInstnId>\\n          <ns2:ClrSysMmbId>\\n           <ns2:MmbId>test</ns2:MmbId>\\n          </ns2:ClrSysMmbId>\\n          <ns2:Othr>\\n           <ns2:Id>NWMGSGSG</ns2:Id>\\n          </ns2:Othr>\\n         </ns2:FinInstnId>\\n        </ns2:FIId>\\n       </ns2:Fr>\\n       <ns2:To>\\n        <ns2:FIId>\\n         <ns2:FinInstnId>\\n          <ns2:ClrSysMmbId>\\n           <ns2:MmbId>test</ns2:MmbId>\\n          </ns2:ClrSysMmbId>\\n          <ns2:Othr>\\n           <ns2:Id>CITENANT</ns2:Id>\\n          </ns2:Othr>\\n         </ns2:FinInstnId>\\n        </ns2:FIId>\\n       </ns2:To>\\n       <ns2:BizMsgIdr>20210409NWMGSGSGXXX0000000037992889</ns2:BizMsgIdr>\\n       <ns2:MsgDefIdr>'admn.001.001.01'</ns2:MsgDefIdr>\\n       <ns2:BizSvc>CI</ns2:BizSvc>\\n       <ns2:CreDt>2021-04-09T07:21:00Z</ns2:CreDt>\\n       <ns2:CpyDplct>CODU</ns2:CpyDplct>\\n       <ns2:PssblDplct>false</ns2:PssblDplct>\\n       <ns2:Prty>NORM</ns2:Prty>\\n       <ns2:Rltd>\\n        <ns2:Fr>\\n         <ns2:FIId>\\n          <ns2:FinInstnId>\\n           <ns2:ClrSysMmbId>\\n            <ns2:MmbId>test</ns2:MmbId>\\n           </ns2:ClrSysMmbId>\\n           <ns2:Othr>\\n            <ns2:Id>NWMGSGSG</ns2:Id>\\n           </ns2:Othr>\\n          </ns2:FinInstnId>\\n         </ns2:FIId>\\n        </ns2:Fr>\\n        <ns2:To>\\n         <ns2:FIId>\\n          <ns2:FinInstnId>\\n           <ns2:ClrSysMmbId>\\n            <ns2:MmbId>test</ns2:MmbId>\\n           </ns2:ClrSysMmbId>\\n           <ns2:Othr>\\n            <ns2:Id>CITENANT</ns2:Id>\\n           </ns2:Othr>\\n          </ns2:FinInstnId>\\n         </ns2:FIId>\\n        </ns2:To>\\n        <ns2:BizMsgIdr>20210409NWMGSGSGXXX0000000037992889</ns2:BizMsgIdr>\\n        <ns2:MsgDefIdr>'admn.001.001.01'</ns2:MsgDefIdr>\\n        <ns2:CreDt>2021-04-09T07:21:00Z</ns2:CreDt>\\n       </ns2:Rltd>\\n      </ns:AppHdr>\\n      <ns:Document>\\n       <ns:AdmnReq>\\n        <ns1:GrpHdr>\\n         <ns1:MsgId>20210409NWMGSGSGXXX0000000026770339</ns1:MsgId>\\n         <ns1:CreDtTm>2021-04-09T07:21:00Z</ns1:CreDtTm>\\n        </ns1:GrpHdr>\\n        <ns1:AdmnTxInf>\\n         <ns1:InstrId>20210409NWMGSGSGXXX0000000026770339</ns1:InstrId>\\n         <ns1:InstgAgt>\\n          <ns1:FinInstnId>\\n           <ns1:Othr>\\n            <ns1:Id>NWMGSGSG</ns1:Id>\\n           </ns1:Othr>\\n          </ns1:FinInstnId>\\n         </ns1:InstgAgt>\\n        </ns1:AdmnTxInf>\\n       </ns:AdmnReq>\\n      </ns:Document>\\n     </ns:BusMsg> \"\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";
		return str;
    }

}
