package bifast.mock.proxy;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.prxy002.ProxyRegistrationResponseV01;
import bifast.mock.isoservice.MsgHeaderService;
import bifast.mock.isoservice.Proxy002MessageService;
import bifast.mock.isoservice.Proxy002Seed;
import bifast.mock.processor.UtilService;

@Component
public class ProxyRegistrationProcessor implements Processor{

	@Autowired private MsgHeaderService hdrService;
	@Autowired private Proxy002MessageService proxy002MessageService;
	@Autowired private ProxyRegistrationService proxyRegService;
	@Autowired private UtilService utilService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String bizMsgId = utilService.genRfiBusMsgId("710", "01", "INDOIDJA");
		String msgId = utilService.genMessageId("710", "INDOIDJA");

		Proxy002Seed seed = null;
		
		BusinessMessage msg = exchange.getIn().getBody(BusinessMessage.class);

		String regType = msg.getDocument().getPrxyRegn().getRegn().getRegnTp().value();
		String proxyType = msg.getDocument().getPrxyRegn().getRegn().getPrxy().getTp();
		String proxyVal = msg.getDocument().getPrxyRegn().getRegn().getPrxy().getVal();
		if (proxyType.equals("02"))
			msg.getDocument().getPrxyRegn().getRegn().getPrxy().setVal(proxyVal.toUpperCase());

		if (regType.equals("NEWR")) {
			seed = proxyRegService.newr(msg);
		}
		else if (regType.equals("AMND")) {
			seed = proxyRegService.amnd(msg);
		}
		else if (regType.equals("SUSP")) 
			seed = proxyRegService.susp(msg);			
		else if (regType.equals("SUSB")) 
			seed = proxyRegService.susp(msg);			
		
		else if (regType.equals("DEAC")) {
			seed = proxyRegService.deac(msg);			

		}
		else if (regType.equals("PORT")) {
			seed = proxyRegService.port(msg);			

		}
		else if (regType.equals("ACTB")) {
			seed = proxyRegService.actv(msg);		
		}
		else if (regType.equals("ACTV")) {
			seed = proxyRegService.actv(msg);		
		}

		
		seed.setMsgId(msgId);

		ProxyRegistrationResponseV01 response = proxy002MessageService.proxyRegistrationResponse(seed, msg);
		
		BusinessApplicationHeaderV01 hdr = new BusinessApplicationHeaderV01();
		hdr = hdrService.getAppHdr("prxy.002.001.01", bizMsgId);
		Document doc = new Document();
		doc.setPrxyRegnRspn(response);
		BusinessMessage busMesg = new BusinessMessage();
		busMesg.setAppHdr(hdr);
		busMesg.setDocument(doc);

		exchange.getMessage().setBody(busMesg);
	}
	
}
