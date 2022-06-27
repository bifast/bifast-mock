package bifast.mock.processor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.pacs002.FIToFIPaymentStatusReportV10;
import bifast.mock.isoservice.MsgHeaderService;
import bifast.mock.isoservice.Pacs002MessageService;
import bifast.mock.isoservice.Pacs002Seed;
import bifast.mock.persist.AccountProxy;
import bifast.mock.persist.AccountProxyRepository;
import bifast.mock.persist.MockPacs002;
import bifast.mock.persist.MockPacs002Repository;

@Component
public class CreditTransferResponseProcessor implements Processor{
	@Autowired private AccountProxyRepository accountRepo;
	@Autowired private MsgHeaderService hdrService;
    @Autowired private MockPacs002Repository mockPacs002Repo;
	@Autowired private Pacs002MessageService pacs002Service;
	@Autowired private UtilService utilService;
	
	@Value("${komi.timeout-ct}")
	private int delay;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		BusinessMessage objRequest = exchange.getMessage().getHeader("objRequest", BusinessMessage.class);		
		
    	ObjectMapper map4ctreq = new ObjectMapper();
    	map4ctreq.registerModule(new JaxbAnnotationModule());
    	map4ctreq.enable(SerializationFeature.WRAP_ROOT_VALUE);
    	map4ctreq.setSerializationInclusion(Include.NON_NULL);
    	String strCTReq = map4ctreq.writeValueAsString(objRequest);

		System.out.println(strCTReq);
    	
    	String addInfo = "";
		if (null != objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf()) 
			addInfo = objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getRmtInf().getUstrd().get(0).toLowerCase();

		String norekCdtr = objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct().getId().getOthr().getId();
		String bank = objRequest.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAgt().getFinInstnId().getOthr().getId();
		exchange.getMessage().setHeader("hdr_account_no", norekCdtr);
	
		if (norekCdtr.startsWith("99")) {
			String str = admi002();
	    	ObjectMapper map = new ObjectMapper();
	    	map.registerModule(new JaxbAnnotationModule());
	    	map.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
	    	map.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	    	BusinessMessage msg = map.readValue(str, BusinessMessage.class);
			exchange.getMessage().setBody(msg);
		}

		else {

			Optional<AccountProxy> oAcct = accountRepo.findByAccountNumberAndRegisterBank(norekCdtr, bank);
			BusinessMessage resultMessg = buildBusinessMessage (objRequest, oAcct);
			saveCreditResponse(objRequest, resultMessg, strCTReq);
	
			if ((addInfo.equals("timeout")) || (addInfo.contains("cttimeout"))) {
			    try
			    {
			    	System.out.println("delay dulu selama " + delay);
			    	LocalDateTime dt1 = LocalDateTime.now();
			        Thread.sleep(delay);
			        Long duration = Duration.between(LocalDateTime.now(), dt1).getSeconds();
			        System.out.println("Oke : " + duration);
			        resultMessg.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getStsRsnInf().get(0).getRsn().setPrtry("U900");
			    }
			    catch(InterruptedException ex)
			    {
			        Thread.currentThread().interrupt();
			    }
			}
			
			exchange.getMessage().setBody(resultMessg);
			
		}
		
	}
	
	BusinessMessage buildBusinessMessage (BusinessMessage bmInput, Optional<AccountProxy> oAccount) throws Exception {
		
		String bizMsgId = utilService.genRfiBusMsgId("010", "02", bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
		String msgId = utilService.genMessageId("010", bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());

		Pacs002Seed seed = new Pacs002Seed();
		seed.setMsgId(msgId);

		if (oAccount.isPresent()) {
			AccountProxy account = oAccount.get();
			System.out.println("account " + account + " exists " + account.getAccountStatus());
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
	
//	BusinessMessage buildTimeoutResponse (BusinessMessage bmInput) throws Exception {
//		
//		String bizMsgId = utilService.genRfiBusMsgId("010", "02", bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
//		String msgId = utilService.genMessageId("010", bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
//
//		Pacs002Seed seed = new Pacs002Seed();
//		seed.setMsgId(msgId);
//
//		seed.setStatus("RJCT");
//		seed.setReason("U900");
//		seed.setCreditorName(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr().getNm());
//		
//		seed.setCreditorType("01");
//		seed.setCreditorId(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr().getId().getPrvtId().getOthr().get(0).getId());
//		seed.setCreditorResidentialStatus("01");
//		seed.setCreditorTown(bmInput.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getSplmtryData().get(0).getEnvlp().getDtl().getCdtr().getTwnNm());
//
//		FIToFIPaymentStatusReportV10 response = pacs002Service.creditTransferRequestResponse(seed, bmInput);
//		
//		GregorianCalendar gcal = new GregorianCalendar();
//		XMLGregorianCalendar xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
//		response.getTxInfAndSts().get(0).getOrgnlTxRef().setIntrBkSttlmDt(xcal);
//
//		BusinessApplicationHeaderV01 hdr = new BusinessApplicationHeaderV01();
//		hdr = hdrService.getAppHdr(bmInput.getAppHdr().getFr().getFIId().getFinInstnId().getOthr().getId(), 
//									"pacs.002.001.10", bizMsgId);
//		hdr.setBizSvc("CLEAR");
//		hdr.getFr().getFIId().getFinInstnId().getOthr().setId(bmInput.getAppHdr().getTo().getFIId().getFinInstnId().getOthr().getId());
//
//		BusinessMessage busMesg = new BusinessMessage();
//		Document doc = new Document();
//		doc.setFiToFIPmtStsRpt(response);
//
//		busMesg.setAppHdr(hdr);
//		busMesg.setDocument(doc);
//
//		return busMesg;
//	}

    public void saveCreditResponse (BusinessMessage requestMsg, BusinessMessage responseMsg, String strCTReq) throws Exception {

    	ObjectMapper map = new ObjectMapper();
    	map.registerModule(new JaxbAnnotationModule());
    	map.enable(SerializationFeature.WRAP_ROOT_VALUE);
	    map.setSerializationInclusion(Include.NON_NULL);

    	String respAsString = map.writeValueAsString(responseMsg);
    	System.out.println(respAsString);
    	
        // simpan sbg history
		MockPacs002 pacs002 = new MockPacs002();
		pacs002.setBizMsgIdr(responseMsg.getAppHdr().getBizMsgIdr());

		pacs002.setFullMessage(respAsString);
		pacs002.setCtRequest(strCTReq);
        pacs002.setOrgnlEndToEndId(responseMsg.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getOrgnlEndToEndId());
		
        pacs002.setOrgnlMsgId(responseMsg.getDocument().getFiToFIPmtStsRpt().getOrgnlGrpInfAndSts().get(0).getOrgnlMsgId());
		
        String orgnlMsgName = responseMsg.getDocument().getFiToFIPmtStsRpt().getOrgnlGrpInfAndSts().get(0).getOrgnlMsgNmId();
        pacs002.setOrgnlMsgName(orgnlMsgName);
        pacs002.setResult(responseMsg.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
        pacs002.setCdtrAcct(requestMsg.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtrAcct().getId().getOthr().getId());
        pacs002.setDbtrAcct(requestMsg.getDocument().getFiToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtrAcct().getId().getOthr().getId());
        
        pacs002.setTrxType("CLEAR");

        mockPacs002Repo.save(pacs002);
        
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
