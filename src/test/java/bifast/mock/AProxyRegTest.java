package bifast.mock;

import java.util.Optional;

import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.mock.persist.AccountProxy;
import bifast.mock.persist.AccountProxyRepository;

@CamelSpringBootTest
@EnableAutoConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class AProxyRegTest {
	@Autowired AccountProxyRepository proxyRepo;
	@Autowired ProducerTemplate producerTemplate;
	@Autowired TestUtilService utilService;

//	@Test
    @Order(1)    
	public void ProxyResl() throws Exception {
		String str = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220727SIHBIDJ1610O0224240385\",\"MsgDefIdr\":\"prxy.003.001.01\",\"CreDt\":\"2022-07-26T23:44:00Z\"},\"Document\":{\"PrxyLookUp\":{\"GrpHdr\":{\"MsgId\":\"20220727SIHBIDJ161024240385\",\"CreDtTm\":\"2022-07-27T06:44:00.573\",\"MsgSndr\":{\"Agt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"Acct\":{\"Id\":{\"Othr\":{\"Id\":\"5022108100525\"}}}}},\"LookUp\":{\"PrxyOnly\":{\"LkUpTp\":\"PXRS\",\"Id\":\"20220727SIHBIDJ124240385\",\"PrxyRtrvl\":{\"Tp\":\"01\",\"Val\":\"0812233002\"}}}}}}}";
		Object ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, str);
		String resp = (String) ret;
		BusinessMessage bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("prxy.004.001.01", bm.getAppHdr().getMsgDefIdr());
		Assertions.assertEquals("ACTC", bm.getDocument().getPrxyLookUpRspn().getLkUpRspn().getRegnRspn().getPrxRspnSts().value());
		
		str = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220727SIHBIDJ1610O0224240386\",\"MsgDefIdr\":\"prxy.003.001.01\",\"CreDt\":\"2022-07-26T23:44:00Z\"},\"Document\":{\"PrxyLookUp\":{\"GrpHdr\":{\"MsgId\":\"20220727SIHBIDJ161024240386\",\"CreDtTm\":\"2022-07-27T06:44:00.573\",\"MsgSndr\":{\"Agt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"Acct\":{\"Id\":{\"Othr\":{\"Id\":\"5022108100526\"}}}}},\"LookUp\":{\"PrxyOnly\":{\"LkUpTp\":\"PXRS\",\"Id\":\"20220727SIHBIDJ124240385\",\"PrxyRtrvl\":{\"Tp\":\"01\",\"Val\":\"0811133444\"}}}}}}}";
		ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, str);
		resp = (String) ret;
		bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("prxy.004.001.01", bm.getAppHdr().getMsgDefIdr());
		Assertions.assertEquals("RJCT", bm.getDocument().getPrxyLookUpRspn().getLkUpRspn().getRegnRspn().getPrxRspnSts().value());
		Assertions.assertEquals("U804", bm.getDocument().getPrxyLookUpRspn().getLkUpRspn().getRegnRspn().getStsRsnInf().getPrtry());
	}

//	@Test
    @Order(2)    
	public void ProxyRegis() throws Exception {
		Optional<AccountProxy> oAcctPrx = proxyRepo.getValidByProxyTypeAndByProxyVal("02", "bams13@gmail.com");
		if (oAcctPrx.isPresent()) proxyRepo.delete(oAcctPrx.get());
		String str = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220726SIHBIDJ1710O0155572007\",\"MsgDefIdr\":\"prxy.001.001.01\",\"CreDt\":\"2022-07-26T08:26:12Z\"},\"Document\":{\"PrxyRegn\":{\"GrpHdr\":{\"MsgId\":\"20220726SIHBIDJ171055572007\",\"CreDtTm\":\"2022-07-26T15:26:12.566\",\"MsgSndr\":{\"Agt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"Acct\":{\"Id\":{\"Othr\":{\"Id\":\"112222334\"}}}}},\"Regn\":{\"RegnTp\":\"NEWR\",\"Prxy\":{\"Tp\":\"02\",\"Val\":\"bams13@gmail.com\"},\"PrxyRegn\":{\"DsplNm\":\"JOHN SMITH\",\"Agt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"Acct\":{\"Id\":{\"Othr\":{\"Id\":\"112222334\"}},\"Tp\":{\"Prtry\":\"CACC\"},\"Nm\":\"JOHN SMITH \"},\"ScndId\":{\"Tp\":\"01\",\"Val\":\"KTP-222044001\"}}},\"SplmtryData\":[{\"Envlp\":{\"Dtl\":{\"Cstmr\":{\"Tp\":\"01\",\"Id\":\"KTP-220440201\",\"RsdntSts\":\"01\",\"TwnNm\":\"0300\"}}}}]}}}}";

		Object ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, str);
		String resp = (String) ret;
		BusinessMessage bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("prxy.002.001.01", bm.getAppHdr().getMsgDefIdr());

		oAcctPrx = proxyRepo.getValidByProxyTypeAndByProxyVal("02", "BAMS13@GMAIL.COM");
		Assertions.assertNotNull(oAcctPrx.get());
		Assertions.assertEquals("ACTV", oAcctPrx.get().getAccountStatus());
	}


}
