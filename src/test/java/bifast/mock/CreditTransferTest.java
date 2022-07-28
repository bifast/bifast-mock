package bifast.mock;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.mock.persist.AccountProxyRepository;
import bifast.mock.persist.CTResponse;
import bifast.mock.persist.CTResponseRepository;

@CamelSpringBootTest
@EnableAutoConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class CreditTransferTest {
	@Autowired CTResponseRepository ctRepo;
	@Autowired AccountProxyRepository proxyRepo;
	@Autowired ProducerTemplate producerTemplate;
	@Autowired CamelContext camelContext;
	@Autowired TestUtilService utilService;
   
	@Test
    @Order(1)    
	public void CrdTrnsActcSttl() throws Exception {
		// Credit Transfer Request
		String str = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220727SIHBIDJ1010O0223859375\",\"MsgDefIdr\":\"pacs.008.001.08\",\"CreDt\":\"2022-07-26T23:37:40Z\"},\"Document\":{\"FIToFICstmrCdtTrf\":{\"GrpHdr\":{\"MsgId\":\"20220727SIHBIDJ101023859375\",\"CreDtTm\":\"2022-07-27T06:37:40.184\",\"NbOfTxs\":\"1\",\"SttlmInf\":{\"SttlmMtd\":\"CLRG\"}},\"CdtTrfTxInf\":[{\"PmtId\":{\"EndToEndId\":\"20220727SIHBIDJ1010O0223859375\",\"TxId\":\"20220727SIHBIDJ101023859375\"},\"PmtTpInf\":{\"LclInstrm\":{\"Prtry\":\"02\"},\"CtgyPurp\":{\"Prtry\":\"01002\"}},\"IntrBkSttlmAmt\":{\"Value\":\"250000.00\",\"Ccy\":\"IDR\"},\"ChrgBr\":\"DEBT\",\"Dbtr\":{\"Nm\":\"ANANDA RIZKY\",\"Id\":{\"PrvtId\":{\"Othr\":[{\"Id\":\"3471136601960002\"}]}}},\"DbtrAcct\":{\"Id\":{\"Othr\":{\"Id\":\"2012110686590\"}},\"Tp\":{\"Prtry\":\"SVGS\"}},\"DbtrAgt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"CdtrAgt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"CENAIDJA\"}}},\"Cdtr\":{\"Nm\":\"ANDRIAN S\"},\"CdtrAcct\":{\"Id\":{\"Othr\":{\"Id\":\"112211333\"}},\"Tp\":{\"Prtry\":\"CACC\"}},\"RmtInf\":{\"Ustrd\":[\"0\"]},\"SplmtryData\":[{\"Envlp\":{\"Dtl\":{\"Dbtr\":{\"Tp\":\"01\"}}}}]}]}}}}";
		Object ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, str);
		String resp = (String) ret;
		BusinessMessage bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("pacs.002.001.10", bm.getAppHdr().getMsgDefIdr());
		Assertions.assertEquals("CLEAR", bm.getAppHdr().getBizSvc());
		Assertions.assertEquals("ACTC", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
		Assertions.assertEquals("U000", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getStsRsnInf().get(0).getRsn().getPrtry());

		Optional<CTResponse> oct = ctRepo.findByEndToEndId("20220727SIHBIDJ1010O0223859375");
		Assertions.assertTrue(oct.isPresent(), "CT response tidak tercatat");
		Assertions.assertEquals("ACTC", oct.get().getResponse());
		Assertions.assertEquals("CLEAR", oct.get().getBizSvc());
		Assertions.assertEquals("U000", oct.get().getReason());
		
		// Payment Status Request
		String psr = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220723SIHBIDJ1000O9976923580\",\"MsgDefIdr\":\"pacs.028.001.04\",\"CreDt\":\"2022-07-23T14:22:18Z\"},\"Document\":{\"FIToFIPmtStsReq\":{\"GrpHdr\":{\"MsgId\":\"20220723SIHBIDJ100076923580\",\"CreDtTm\":\"2022-07-23T21:22:18.159\"},\"TxInf\":[{\"OrgnlEndToEndId\":\"20220727SIHBIDJ1010O0223859375\"}]}}}}";
		ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, psr);
		resp = (String) ret;
		bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("pacs.002.001.10", bm.getAppHdr().getMsgDefIdr());
		Assertions.assertEquals("ACTC", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
		Assertions.assertEquals("U000", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getStsRsnInf().get(0).getRsn().getPrtry());
		
		// tunggu terjadi settlement
		TimeUnit.SECONDS.sleep(5);
		
		// Payment Status Request
		psr = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220723SIHBIDJ1000O9976923580\",\"MsgDefIdr\":\"pacs.028.001.04\",\"CreDt\":\"2022-07-23T14:22:18Z\"},\"Document\":{\"FIToFIPmtStsReq\":{\"GrpHdr\":{\"MsgId\":\"20220723SIHBIDJ100076923580\",\"CreDtTm\":\"2022-07-23T21:22:18.159\"},\"TxInf\":[{\"OrgnlEndToEndId\":\"20220727SIHBIDJ1010O0223859375\"}]}}}}";
		ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, psr);
		resp = (String) ret;
		bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("pacs.002.001.10", bm.getAppHdr().getMsgDefIdr());
		Assertions.assertEquals("STTL", bm.getAppHdr().getBizSvc());
		Assertions.assertEquals("ACSC", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
		
		oct = ctRepo.findByEndToEndId("20220727SIHBIDJ1010O0223859375");
		Assertions.assertEquals("STTL", oct.get().getBizSvc());
		Assertions.assertEquals("ACSC", oct.get().getResponse());

	}

	@Test
    @Order(2)    
	public void CrdTrnsRjct() throws Exception {
		// Credit Transfer Request
		String str = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220727SIHBIDJ1010O0223859380\",\"MsgDefIdr\":\"pacs.008.001.08\",\"CreDt\":\"2022-07-26T23:37:40Z\"},\"Document\":{\"FIToFICstmrCdtTrf\":{\"GrpHdr\":{\"MsgId\":\"20220727SIHBIDJ101023859380\",\"CreDtTm\":\"2022-07-27T06:37:40.184\",\"NbOfTxs\":\"1\",\"SttlmInf\":{\"SttlmMtd\":\"CLRG\"}},\"CdtTrfTxInf\":[{\"PmtId\":{\"EndToEndId\":\"20220727SIHBIDJ1010O0223859380\",\"TxId\":\"20220727SIHBIDJ101023859380\"},\"PmtTpInf\":{\"LclInstrm\":{\"Prtry\":\"02\"},\"CtgyPurp\":{\"Prtry\":\"01002\"}},\"IntrBkSttlmAmt\":{\"Value\":\"250000.00\",\"Ccy\":\"IDR\"},\"ChrgBr\":\"DEBT\",\"Dbtr\":{\"Nm\":\"ANANDA RIZKY\",\"Id\":{\"PrvtId\":{\"Othr\":[{\"Id\":\"3471136601960002\"}]}}},\"DbtrAcct\":{\"Id\":{\"Othr\":{\"Id\":\"2012110686590\"}},\"Tp\":{\"Prtry\":\"SVGS\"}},\"DbtrAgt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"CdtrAgt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"BRINIDJA\"}}},\"Cdtr\":{\"Nm\":\"SYAMSUL HADI\"},\"CdtrAcct\":{\"Id\":{\"Othr\":{\"Id\":\"112211777\"}},\"Tp\":{\"Prtry\":\"CACC\"}},\"RmtInf\":{\"Ustrd\":[\"0\"]},\"SplmtryData\":[{\"Envlp\":{\"Dtl\":{\"Dbtr\":{\"Tp\":\"01\"}}}}]}]}}}}";
		Object ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, str);
		String resp = (String) ret;
		BusinessMessage bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("pacs.002.001.10", bm.getAppHdr().getMsgDefIdr());
		Assertions.assertEquals("RJCT", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
		Assertions.assertEquals("U102", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getStsRsnInf().get(0).getRsn().getPrtry());
	}

}
