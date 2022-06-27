package bifast.mock.proxy;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.prxy004.ProxyLookUpResponseV01;
import bifast.mock.isoservice.MsgHeaderService;
import bifast.mock.isoservice.Proxy004MessageService;
import bifast.mock.isoservice.Proxy004Seed;
import bifast.mock.persist.AccountProxy;
import bifast.mock.persist.AccountProxyRepository;
import bifast.mock.processor.UtilService;


@Component
public class ProxyResolutionResponseProc implements Processor{

	@Autowired
	private MsgHeaderService hdrService;
	@Autowired
	private Proxy004MessageService proxy004MessageService;
	
	@Autowired
	private UtilService utilService;
	
	@Autowired
	AccountProxyRepository accountProxyRepository;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		BusinessMessage msg = exchange.getIn().getBody(BusinessMessage.class);

		String bizMsgId = utilService.genRfiBusMsgId("610", "01", "INDOIDJA");
		String msgId = utilService.genMessageId("610", "INDOIDJA");

		Proxy004Seed seed = new Proxy004Seed();
		seed.setMsgId(msgId);
		
		String proxyType = msg.getDocument().getPrxyLookUp().getLookUp().getPrxyOnly().getPrxyRtrvl().getTp();
		String proxyVal =  msg.getDocument().getPrxyLookUp().getLookUp().getPrxyOnly().getPrxyRtrvl().getVal();
		if (proxyType.equals("02"))
			proxyVal = proxyVal.toUpperCase();

		
		seed.setOrgnlPrxyRqstrTp(proxyType);
		seed.setOrgnlPrxyRqstrVal(proxyVal);

		Optional<AccountProxy> oAccountProxy = accountProxyRepository.getByProxyTypeAndByProxyValAndNotInactive(proxyType, proxyVal);
		
		if (oAccountProxy.isEmpty()) {
			System.out.println("empty");
			seed.setStatus("RJCT");
			seed.setReason("U804");
		}
		
		else {
			AccountProxy proxy = oAccountProxy.get();

			seed.setPrxyRtrvlTp(proxy.getProxyType());
			seed.setPrxyRtrvlVal(proxy.getProxyVal());

			if (proxy.getAccountStatus().equals("ACTV")) {
				seed.setStatus("ACTC");
				seed.setReason("U000");
				
				seed.setDisplayName(proxy.getDisplayName());
				seed.setRegisterBank(proxy.getRegisterBank());
				seed.setRegnId(proxy.getReginId());
				seed.setAccountNumber(proxy.getAccountNumber());
				seed.setAccountType(proxy.getAccountType());

				seed.setAccountName(proxy.getAccountName());
				seed.setCstmrId(proxy.getCstmrId());
				seed.setCstmrTp(proxy.getCstmrTp());
				seed.setCstmrRsdntSts(proxy.getCstmrRsdntSts());
				seed.setCstmrTwnNm(proxy.getCstmrTwnNm());
			}
			else if (proxy.getAccountStatus().equals("SUSB")) {
				seed.setStatus("RJCT");
				seed.setReason("U811");

			}
			else {
				seed.setStatus("RJCT");
				seed.setReason("U805");
			}

		}
				
		ProxyLookUpResponseV01 response = proxy004MessageService.proxyResolutionResponse(seed, msg);
		
		BusinessApplicationHeaderV01 hdr = new BusinessApplicationHeaderV01();
		hdr = hdrService.getAppHdr("prxy.004.001.01", bizMsgId);
		
		Document doc = new Document();
		doc.setPrxyLookUpRspn(response);
		BusinessMessage busMesg = new BusinessMessage();
		busMesg.setAppHdr(hdr);
		busMesg.setDocument(doc);

		exchange.getMessage().setBody(busMesg);
		
	}

}
