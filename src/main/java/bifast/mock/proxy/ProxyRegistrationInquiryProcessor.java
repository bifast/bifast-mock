package bifast.mock.proxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.custom.Document;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.prxy006.ProxyEnquiryResponseV01;
import bifast.mock.isoservice.MsgHeaderService;
import bifast.mock.isoservice.Proxy006MessageService;
import bifast.mock.isoservice.Proxy006Seed;
import bifast.mock.isoservice.Proxy006SeedAccount;
import bifast.mock.persist.AccountProxy;
import bifast.mock.persist.AccountProxyRepository;
import bifast.mock.processor.UtilService;

@Component
public class ProxyRegistrationInquiryProcessor implements Processor{

	@Autowired
	private MsgHeaderService hdrService;
	@Autowired
	private Proxy006MessageService proxy006MessageService;
	@Autowired
	private UtilService utilService;
	
	@Autowired
	AccountProxyRepository accountProxyRepository;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String bizMsgId = utilService.genRfiBusMsgId("610", "01", "INDOIDJA");
		String ap = utilService.genMessageId("610", "INDOIDJA");
		
		Proxy006Seed seed = new Proxy006Seed();
		
		BusinessMessage msg = exchange.getIn().getBody(BusinessMessage.class);
	
		List<AccountProxy> accountProxylist = new ArrayList<AccountProxy>();
		
		String scndIdTp = msg.getDocument().getPrxyNqryReq().getNqry().getScndId().getTp();
		String scndIdVal = msg.getDocument().getPrxyNqryReq().getNqry().getScndId().getVal();
		
		seed.setOrgnlMsgId(msg.getAppHdr().getBizMsgIdr());
		seed.setOrgnlMsgNmId(msg.getAppHdr().getMsgDefIdr());
		seed.setOrgnlCreDtTm(msg.getAppHdr().getCreDt());
		
		accountProxylist = accountProxyRepository.getListByScndIdTpAndByScndIdVal(scndIdTp,scndIdVal);
		
		System.out.println("Hasil enquiry ada " + accountProxylist.size() + " proxy.");
		if(accountProxylist.size() > 0) {

			List<Proxy006SeedAccount> proxy006SeedAccountList = new ArrayList<Proxy006SeedAccount>();
			for(AccountProxy data:accountProxylist) {

				Proxy006SeedAccount seedAcc =  new Proxy006SeedAccount();
				seedAcc.setRegnId(data.getReginId());
				seedAcc.setAccountName(data.getAccountName());
				seedAcc.setAccountNumber(data.getAccountNumber());
				seedAcc.setAccountType(data.getAccountType());
				seedAcc.setDisplayName(data.getDisplayName());
				seedAcc.setRegisterBank(data.getRegisterBank());
				seedAcc.setPrxyInfTp(data.getProxyType());
				seedAcc.setPrxyInfVal(data.getProxyVal());
				seedAcc.setAccountStatus(data.getAccountStatus());
				seedAcc.setAdditionalInfo("Terimakasih atas perhaitiannya");
				seedAcc.setCstmrId(data.getCstmrId());
				seedAcc.setCstmrTp(data.getCstmrTp());
				seedAcc.setCstmrTwnNm(data.getCstmrTwnNm());
				seedAcc.setCstmrRsdntSts(data.getCstmrRsdntSts());
				
				if (data.getAccountStatus().equals("ACTV")) {
					proxy006SeedAccountList.add(seedAcc);
				}
			}

			if (proxy006SeedAccountList.size()>0) {
				seed.setStatus("ACTC");
				seed.setReason("U000");
			}
			else {
				seed.setStatus("RJCT");
				seed.setReason("U804");
			}
			seed.setProxy006SeedAccountList(proxy006SeedAccountList);

		}else {
			seed.setStatus("RJCT");
			seed.setReason("U804");
		}
		
		ProxyEnquiryResponseV01 response = proxy006MessageService.proxyRegistrationInquiryResponse(seed, msg);

		BusinessApplicationHeaderV01 hdr = new BusinessApplicationHeaderV01();
		hdr = hdrService.getAppHdr("prxy.006.001.01", bizMsgId);
		Document doc = new Document();
		doc.setPrxyNqryRspn(response);
		BusinessMessage busMesg = new BusinessMessage();
		busMesg.setAppHdr(hdr);
		busMesg.setDocument(doc);

		exchange.getMessage().setBody(busMesg);
	}

}
