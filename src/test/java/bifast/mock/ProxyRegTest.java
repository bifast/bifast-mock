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

@CamelSpringBootTest
@EnableAutoConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class ProxyRegTest {
	@Autowired ProducerTemplate producerTemplate;
	@Autowired CamelContext camelContext;
	@Autowired TestUtilService utilService;

	@Test
    @Order(1)    
	public void postCT() throws Exception {
		
		String str = "{\"BusMsg\":{\"AppHdr\":{\"Fr\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}}},\"To\":{\"FIId\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"FASTIDJA\"}}}},\"BizMsgIdr\":\"20220726SIHBIDJ1710O0155572007\",\"MsgDefIdr\":\"prxy.001.001.01\",\"CreDt\":\"2022-07-26T08:26:12Z\"},\"Document\":{\"PrxyRegn\":{\"GrpHdr\":{\"MsgId\":\"20220726SIHBIDJ171055572007\",\"CreDtTm\":\"2022-07-26T15:26:12.566\",\"MsgSndr\":{\"Agt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"Acct\":{\"Id\":{\"Othr\":{\"Id\":\"112222334\"}}}}},\"Regn\":{\"RegnTp\":\"NEWR\",\"Prxy\":{\"Tp\":\"02\",\"Val\":\"bams13@gmail.com\"},\"PrxyRegn\":{\"DsplNm\":\"JOHN SMITH\",\"Agt\":{\"FinInstnId\":{\"Othr\":{\"Id\":\"SIHBIDJ1\"}}},\"Acct\":{\"Id\":{\"Othr\":{\"Id\":\"112222334\"}},\"Tp\":{\"Prtry\":\"CACC\"},\"Nm\":\"JOHN SMITH \"},\"ScndId\":{\"Tp\":\"01\",\"Val\":\"KTP-222044001\"}}},\"SplmtryData\":[{\"Envlp\":{\"Dtl\":{\"Cstmr\":{\"Tp\":\"01\",\"Id\":\"KTP-220440201\",\"RsdntSts\":\"01\",\"TwnNm\":\"0300\"}}}}]}}}}";
		Object ret = producerTemplate.sendBody("direct:receive", ExchangePattern.InOut, str);
		String resp = (String) ret;
		BusinessMessage bm = utilService.deSerializeBusinessMessage(resp);
		Assertions.assertInstanceOf(BusinessMessage.class, bm);
		Assertions.assertEquals("prxy.002.001.01", bm.getAppHdr().getMsgDefIdr());
		
	}


}
