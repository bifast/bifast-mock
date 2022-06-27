package bifast.mock.proxy;

import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.prxy001.ProxyRegistrationV01;
import bifast.mock.isoservice.Proxy002Seed;
import bifast.mock.persist.AccountProxy;
import bifast.mock.persist.AccountProxyRepository;
import bifast.mock.processor.UtilService;

@Service
public class ProxyRegistrationService {
	@Autowired AccountProxyRepository proxyRepo;
	@Autowired UtilService utilService;
	

	public Proxy002Seed newr (BusinessMessage bm) {
		Proxy002Seed seed = new Proxy002Seed();

		ProxyRegistrationV01 prxyRegn = bm.getDocument().getPrxyRegn();
		String proxyType = prxyRegn.getRegn().getPrxy().getTp();
		String proxyVal = prxyRegn.getRegn().getPrxy().getVal();
		Optional<AccountProxy> oAccountProxy = proxyRepo.findByProxyTypeAndProxyValAndAccountStatus(proxyType, proxyVal, "ACTV");

		seed.setMsgRcptAgtId(prxyRegn.getGrpHdr().getMsgSndr().getAgt().getFinInstnId().getOthr().getId());
		seed.setAgtId(prxyRegn.getRegn().getPrxyRegn().getAgt().getFinInstnId().getOthr().getId());

		if ((oAccountProxy.isEmpty()) ) {
				 
			Random rand = new Random();
			long random = (long)(rand.nextDouble()*10000000000L);
			seed.setRegnId(String.valueOf(random));
			seed.setCstmrId(prxyRegn.getRegn().getPrxyRegn().getScndId().getVal());
			seed.setCstmrRsdntSts(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getRsdntSts());
			seed.setCstmrTp(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTp());
			seed.setCstmrTwnNm(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTwnNm());
			
			seed.setReason("U000");
			seed.setStatus("ACTC");
			
			AccountProxy proxy = new AccountProxy();
			proxy.setAccountName(prxyRegn.getRegn().getPrxyRegn().getAcct().getNm());
			proxy.setAccountNumber(prxyRegn.getRegn().getPrxyRegn().getAcct().getId().getOthr().getId());
			proxy.setAccountStatus("ACTV");
			proxy.setAccountType(prxyRegn.getRegn().getPrxyRegn().getAcct().getTp().getPrtry());
			
			proxy.setCstmrId(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getId());
			proxy.setCstmrRsdntSts(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getRsdntSts());
			proxy.setCstmrTp(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTp());
			proxy.setCstmrTwnNm(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTwnNm());
			proxy.setDisplayName(prxyRegn.getRegn().getPrxyRegn().getDsplNm());
			proxy.setProxyType(prxyRegn.getRegn().getPrxy().getTp());
			proxy.setProxyVal(prxyRegn.getRegn().getPrxy().getVal());
			proxy.setReginId(String.valueOf(random));
			proxy.setRegisterBank(prxyRegn.getRegn().getPrxyRegn().getAgt().getFinInstnId().getOthr().getId());
			proxy.setScndIdTp(prxyRegn.getRegn().getPrxyRegn().getScndId().getTp());
			proxy.setScndIdVal(prxyRegn.getRegn().getPrxyRegn().getScndId().getVal());
			
			proxyRepo.save(proxy);
		}

		else {
			seed.setStatus("RJCT");
			seed.setReason("U808");
		}

		return seed;
	}
	
	public Proxy002Seed amnd (BusinessMessage bm) {
		Proxy002Seed seed = new Proxy002Seed();

		ProxyRegistrationV01 prxyRegn = bm.getDocument().getPrxyRegn();

		String regId = bm.getDocument().getPrxyRegn().getRegn().getPrxyRegn().getRegnId();
		Optional<AccountProxy> oAccountProxy = proxyRepo.findByReginId(regId);
		
		if (oAccountProxy.isEmpty()) {
			seed.setStatus("RJCT");
			seed.setReason("U804");
		}
		else {
			AccountProxy proxy = oAccountProxy.get();
	
			if (proxy.getAccountStatus().equals("ICTV")) {
				seed.setStatus("RJCT");
				seed.setReason("U804");
			}
			else if ((proxy.getAccountStatus().equals("SUSP")) || 
					(proxy.getAccountStatus().equals("SUSB"))) {
				seed.setStatus("RJCT");
				seed.setReason("U805");
			}
			
			else if (proxy.getAccountStatus().equals("ACTV")) {
				seed.setStatus("ACTC");
				seed.setReason("U000");
				
				seed.setRegnId(prxyRegn.getRegn().getPrxyRegn().getRegnId());
				seed.setCstmrId(prxyRegn.getRegn().getPrxyRegn().getScndId().getVal());
				seed.setCstmrRsdntSts(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getRsdntSts());
				seed.setCstmrTp(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTp());
				seed.setCstmrTwnNm(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTwnNm());

				proxy.setAccountName(prxyRegn.getRegn().getPrxyRegn().getAcct().getNm());
				proxy.setAccountNumber(prxyRegn.getRegn().getPrxyRegn().getAcct().getId().getOthr().getId());
				
				if (null != prxyRegn.getRegn().getPrxyRegn().getAcct().getTp())
					proxy.setAccountType(prxyRegn.getRegn().getPrxyRegn().getAcct().getTp().getPrtry());
				
				proxy.setCstmrId(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getId());
				proxy.setCstmrRsdntSts(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getRsdntSts());
				proxy.setCstmrTp(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTp());
				proxy.setCstmrTwnNm(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTwnNm());
				proxy.setDisplayName(prxyRegn.getRegn().getPrxyRegn().getDsplNm());
				proxy.setScndIdTp(prxyRegn.getRegn().getPrxyRegn().getScndId().getTp());
				proxy.setScndIdVal(prxyRegn.getRegn().getPrxyRegn().getScndId().getVal());
				
				proxyRepo.save(proxy);				
			}

		}
		return seed;
	}

	public Proxy002Seed port (BusinessMessage bm) {
		
		Proxy002Seed seed = new Proxy002Seed();
		
		String proxyType = bm.getDocument().getPrxyRegn().getRegn().getPrxy().getTp();
		String proxyVal = bm.getDocument().getPrxyRegn().getRegn().getPrxy().getVal();
		Optional<AccountProxy> oAccountProxy = proxyRepo.findByProxyTypeAndProxyVal(proxyType, proxyVal);

		if (oAccountProxy.isEmpty()) {
			seed.setStatus("RJCT");
			seed.setReason("U804");
		}
		
		else if (!(oAccountProxy.get().getAccountStatus().equals("ACTV"))) {
			seed.setStatus("RJCT");
			seed.setReason("U805");
		}
		else {
			AccountProxy proxy = oAccountProxy.get();

			seed.setAgtId("SIHBIDJ1");
			seed.setCstmrId(proxy.getCstmrId());
			seed.setCstmrRsdntSts(proxy.getCstmrRsdntSts());
			seed.setCstmrTp(proxy.getCstmrTp());
			seed.setCstmrTwnNm(proxy.getCstmrTwnNm());
			seed.setMsgRcptAgtId("SIHBIDJ1");
			seed.setRegnId(proxy.getReginId());

			seed.setStatus("ACTC");
			seed.setReason("U000");

			proxy.setRegisterBank("SIHBIDJ1");
			
			proxyRepo.save(proxy);
		}
		return seed;
	}

	public Proxy002Seed susp (BusinessMessage bm) {
		
		Proxy002Seed seed = new Proxy002Seed();

		ProxyRegistrationV01 prxyRegn = bm.getDocument().getPrxyRegn();

		String regId = prxyRegn.getRegn().getPrxyRegn().getRegnId();
		Optional<AccountProxy> oAccountProxy = proxyRepo.findByReginId(regId);

		if (oAccountProxy.isEmpty()) {
			seed.setStatus("RJCT");
			seed.setReason("U804");
		}
		
		else {
			AccountProxy proxy = oAccountProxy.get();
			String orgnBank = bm.getDocument().getPrxyRegn().getRegn().getPrxyRegn().getAgt().getFinInstnId().getOthr().getId();

			if (proxy.getAccountStatus().equals("ICTV")) {
				seed.setStatus("RJCT");
				seed.setReason("U804");
			}
			else if ((proxy.getAccountStatus().equals("SUSP")) || 
					(proxy.getAccountStatus().equals("SUSB"))) {
				seed.setStatus("RJCT");
				seed.setReason("U805");
			}
			
			else if (proxy.getRegisterBank().equals(orgnBank)) {
				seed.setStatus("ACTC");
				seed.setReason("U000");

				proxy.setAccountStatus(prxyRegn.getRegn().getRegnTp().value());
				proxyRepo.save(proxy);
			}
			else {
				System.out.println("Ini beda bank");
				seed.setStatus("RJCT");
				seed.setReason("U809");
			}

			seed.setRegnId(prxyRegn.getRegn().getPrxyRegn().getRegnId());
			seed.setCstmrId(prxyRegn.getRegn().getPrxyRegn().getScndId().getVal());
			seed.setCstmrRsdntSts(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getRsdntSts());
			seed.setCstmrTp(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTp());
			seed.setCstmrTwnNm(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTwnNm());

			seed.setAgtId(proxy.getRegisterBank());
			seed.setCstmrId(proxy.getCstmrId());
			seed.setCstmrRsdntSts(proxy.getCstmrRsdntSts());
			seed.setCstmrTp(proxy.getCstmrTp());
			seed.setCstmrTwnNm(proxy.getCstmrTwnNm());
			seed.setMsgRcptAgtId(proxy.getRegisterBank());
			seed.setRegnId(proxy.getReginId());


			
		}
		return seed;
	}

	
	public Proxy002Seed deac (BusinessMessage bm) {
		
		Proxy002Seed seed = new Proxy002Seed();
		
		ProxyRegistrationV01 prxyRegn = bm.getDocument().getPrxyRegn();

		String regId = prxyRegn.getRegn().getPrxyRegn().getRegnId();
		Optional<AccountProxy> oAccountProxy = proxyRepo.findByReginId(regId);

		if (oAccountProxy.isEmpty()) {
			seed.setStatus("RJCT");
			seed.setReason("U804");
		}
		else {
			AccountProxy proxy = oAccountProxy.get();
			String orgnBank = bm.getDocument().getPrxyRegn().getRegn().getPrxyRegn().getAgt().getFinInstnId().getOthr().getId();

			if (proxy.getAccountStatus().equals("ICTV")) {
				seed.setStatus("RJCT");
				seed.setReason("U804");
			}
			else if ((proxy.getAccountStatus().equals("SUSP")) || 
					(proxy.getAccountStatus().equals("SUSB"))) {
				seed.setStatus("RJCT");
				seed.setReason("U805");
			}
			
			else if (proxy.getRegisterBank().equals(orgnBank)) {
				seed.setStatus("ACTC");
				seed.setReason("U000");

				proxy.setAccountStatus("ICTV");
				proxyRepo.save(proxy);
			}
			else {
				System.out.println("Ini beda bank");
				seed.setStatus("RJCT");
				seed.setReason("U809");
			}

			seed.setRegnId(prxyRegn.getRegn().getPrxyRegn().getRegnId());
			seed.setCstmrId(prxyRegn.getRegn().getPrxyRegn().getScndId().getVal());
			seed.setCstmrRsdntSts(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getRsdntSts());
			seed.setCstmrTp(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTp());
			seed.setCstmrTwnNm(prxyRegn.getSplmtryData().get(0).getEnvlp().getDtl().getCstmr().getTwnNm());

			seed.setAgtId(proxy.getRegisterBank());
			seed.setCstmrId(proxy.getCstmrId());
			seed.setCstmrRsdntSts(proxy.getCstmrRsdntSts());
			seed.setCstmrTp(proxy.getCstmrTp());
			seed.setCstmrTwnNm(proxy.getCstmrTwnNm());
			seed.setMsgRcptAgtId(proxy.getRegisterBank());
			seed.setRegnId(proxy.getReginId());

		}
		return seed;
	}
	
	public Proxy002Seed actv (BusinessMessage bm) {
		
		Proxy002Seed seed = new Proxy002Seed();
		
		ProxyRegistrationV01 prxyRegn = bm.getDocument().getPrxyRegn();

		String regId = prxyRegn.getRegn().getPrxyRegn().getRegnId();
		Optional<AccountProxy> oAccountProxy = proxyRepo.findByReginId(regId);

		if (oAccountProxy.isEmpty()) {
			System.out.println("tidak ketemu");
			seed.setStatus("RJCT");
			seed.setReason("U804");
		}

		else {
			System.out.println("ketemu");
			AccountProxy proxy = oAccountProxy.get();
			String orgnBank = prxyRegn.getRegn().getPrxyRegn().getAgt().getFinInstnId().getOthr().getId();

			// beda bank
			if (!(proxy.getRegisterBank().equals(orgnBank)) ) {
				if (proxy.getAccountStatus().equals("ICTV")) 
					seed.setReason("U804");
				else
					seed.setReason("U809");
				
				seed.setStatus("RJCT");
			}
			
			else if (proxy.getAccountStatus().equals("ACTV")) {
				seed.setStatus("RJCT");
				seed.setReason("U805");
			}
			
			else if (proxy.getAccountStatus().equals("ICTV")) {
				seed.setStatus("RJCT");
				seed.setReason("U804");
			}			

			else if (proxy.getAccountStatus().equals("SUSP")) {
				if (prxyRegn.getRegn().getRegnTp().value().equals("ACTB")) {
					seed.setStatus("RJCT");
					seed.setReason("U805");
				}
				else {
					seed.setStatus("ACTC");
					seed.setReason("U000");
					proxy.setAccountStatus("ACTV");
					proxyRepo.save(proxy);
				}
			}	
			else if (proxy.getAccountStatus().equals("SUSB")) {

				if (prxyRegn.getRegn().getRegnTp().value().equals("ACTV")) {
					seed.setStatus("RJCT");
					seed.setReason("U811");
				}
				else {
					seed.setStatus("ACTC");
					seed.setReason("U000");
					proxy.setAccountStatus("ACTV");
					proxyRepo.save(proxy);
				}
			}

			seed.setRegnId(prxyRegn.getRegn().getPrxyRegn().getRegnId());
			seed.setCstmrId(prxyRegn.getRegn().getPrxyRegn().getScndId().getVal());
					
			seed.setAgtId(proxy.getRegisterBank());
			seed.setCstmrId(proxy.getCstmrId());
			seed.setCstmrRsdntSts(proxy.getCstmrRsdntSts());
			seed.setCstmrTp(proxy.getCstmrTp());
			seed.setCstmrTwnNm(proxy.getCstmrTwnNm());
			seed.setMsgRcptAgtId(proxy.getRegisterBank());
			seed.setRegnId(proxy.getReginId());

		}
		return seed;
	}

}
