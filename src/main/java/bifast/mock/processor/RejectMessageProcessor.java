package bifast.mock.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class RejectMessageProcessor implements Processor {
    
	@Override
	public void process(Exchange exchange) throws Exception {
		
		
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
		
		exchange.getIn().setBody(str);
		
	}
}
