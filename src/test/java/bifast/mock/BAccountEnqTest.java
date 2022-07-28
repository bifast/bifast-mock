package bifast.mock;

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

@CamelSpringBootTest
@EnableAutoConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class BAccountEnqTest {
	@Autowired AccountProxyRepository proxyRepo;
	@Autowired ProducerTemplate producerTemplate;
	@Autowired CamelContext camelContext;
	@Autowired TestUtilService utilService;

//	@Test
    @Order(3)    
	public void AETest1() throws Exception {
		String str = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220727SIHBIDJ1510O0224064377\",\"MsgDefIdr\":\"pacs.008.001.08\",\"CreDt\":\"2022-07-26T23:41:04Z\"},\"Document\":{\"FIToFICstmrCdtTrf\":{\"GrpHdr\":{\"MsgId\":\"20220727SIHBIDJ151024064377\",\"CreDtTm\":\"2022-07-27T06:41:04.596\",\"NbOfTxs\":\"1\",\"SttlmInf\":{\"SttlmMtd\":\"CLRG\"}},\"CdtTrfTxInf\":[{\"PmtId\":{\"EndToEndId\":\"20220727SIHBIDJ1510O0224064377\",\"TxId\":\"20220727SIHBIDJ151024064377\"},\"PmtTpInf\":{\"CtgyPurp\":{\"Prtry\":\"51002\"}},\"IntrBkSttlmAmt\":{\"Value\":\"10000000.00\",\"Ccy\":\"IDR\"},\"ChrgBr\":\"DEBT\",\"Dbtr\":{},\"DbtrAgt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"CdtrAgt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"CENAIDJA\"}}},\"Cdtr\":{},\"CdtrAcct\":{\"Id\":{\"Othr\":{\"Id\":\"112211333\"}}}}]}}}}";
		Object ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, str);
		String resp = (String) ret;
		BusinessMessage bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("pacs.002.001.10", bm.getAppHdr().getMsgDefIdr());
		Assertions.assertEquals("ACTC", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
		Assertions.assertEquals("U000", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getStsRsnInf().get(0).getRsn().getPrtry());

		str = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220727SIHBIDJ1510O0224064377\",\"MsgDefIdr\":\"pacs.008.001.08\",\"CreDt\":\"2022-07-26T23:41:04Z\"},\"Document\":{\"FIToFICstmrCdtTrf\":{\"GrpHdr\":{\"MsgId\":\"20220727SIHBIDJ151024064377\",\"CreDtTm\":\"2022-07-27T06:41:04.596\",\"NbOfTxs\":\"1\",\"SttlmInf\":{\"SttlmMtd\":\"CLRG\"}},\"CdtTrfTxInf\":[{\"PmtId\":{\"EndToEndId\":\"20220727SIHBIDJ1510O0224064377\",\"TxId\":\"20220727SIHBIDJ151024064377\"},\"PmtTpInf\":{\"CtgyPurp\":{\"Prtry\":\"51002\"}},\"IntrBkSttlmAmt\":{\"Value\":\"10000000.00\",\"Ccy\":\"IDR\"},\"ChrgBr\":\"DEBT\",\"Dbtr\":{},\"DbtrAgt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"CdtrAgt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"CENAIDJA\"}}},\"Cdtr\":{},\"CdtrAcct\":{\"Id\":{\"Othr\":{\"Id\":\"1122113331\"}}}}]}}}}";
		ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, str);
		resp = (String) ret;
		bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("pacs.002.001.10", bm.getAppHdr().getMsgDefIdr());
		Assertions.assertEquals("RJCT", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getTxSts());
		Assertions.assertEquals("U101", bm.getDocument().getFiToFIPmtStsRpt().getTxInfAndSts().get(0).getStsRsnInf().get(0).getRsn().getPrtry());

	}



}
