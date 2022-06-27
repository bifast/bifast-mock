package bifast.mock.isoservice;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.stereotype.Service;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.prxy002.BIAddtlCstmrInf;
import bifast.library.iso20022.prxy002.BISupplementaryData1;
import bifast.library.iso20022.prxy002.BISupplementaryDataEnvelope1;
import bifast.library.iso20022.prxy002.BISupplementaryDataEnvelopeDetail;
import bifast.library.iso20022.prxy002.ProxyRegistrationAccount1;
import bifast.library.iso20022.prxy002.BranchAndFinancialInstitutionIdentification5;
import bifast.library.iso20022.prxy002.FinancialInstitutionIdentification8;
import bifast.library.iso20022.prxy002.GenericFinancialIdentification1;
import bifast.library.iso20022.prxy002.GroupHeader60;
import bifast.library.iso20022.prxy002.OriginalGroupInformation3;
import bifast.library.iso20022.prxy002.Party12Choice;
import bifast.library.iso20022.prxy002.ProxyRegistrationResponse1;
import bifast.library.iso20022.prxy002.ProxyRegistrationResponseV01;
import bifast.library.iso20022.prxy002.ProxyRegistrationType1Code;
import bifast.library.iso20022.prxy002.ProxyStatusCode;
import bifast.library.iso20022.prxy002.ProxyStatusChoice;


@Service
public class Proxy002MessageService {

	public ProxyRegistrationResponseV01 proxyRegistrationResponse (Proxy002Seed seed, 
			BusinessMessage orgnlMessage) 
			throws DatatypeConfigurationException {
		
		
		ProxyRegistrationResponseV01 proxy002 = new ProxyRegistrationResponseV01();
		
		// GrpHdr
		proxy002.setGrpHdr(new GroupHeader60());
		proxy002.getGrpHdr().setMsgId(seed.getMsgId());
		
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
		XMLGregorianCalendar xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		proxy002.getGrpHdr().setCreDtTm(xcal);
		
		// GrpHdr/msgRcpt
		proxy002.getGrpHdr().setMsgRcpt(new Party12Choice());
		proxy002.getGrpHdr().getMsgRcpt().setAgt(new BranchAndFinancialInstitutionIdentification5());
		proxy002.getGrpHdr().getMsgRcpt().getAgt().setFinInstnId(new FinancialInstitutionIdentification8());
		proxy002.getGrpHdr().getMsgRcpt().getAgt().getFinInstnId().setOthr(new GenericFinancialIdentification1());
		proxy002.getGrpHdr().getMsgRcpt().getAgt().getFinInstnId().getOthr().setId(seed.getMsgRcptAgtId());
				
		// OrgnlGrpInf
		proxy002.setOrgnlGrpInf(new OriginalGroupInformation3());
		proxy002.getOrgnlGrpInf().setOrgnlMsgId(orgnlMessage.getAppHdr().getBizMsgIdr());
		
		proxy002.getOrgnlGrpInf().setOrgnlMsgNmId(orgnlMessage.getAppHdr().getMsgDefIdr());
		
		proxy002.getOrgnlGrpInf().setOrgnlCreDtTm(orgnlMessage.getDocument().getPrxyRegn().getGrpHdr().getCreDtTm());
		
		// RegnRspn
		proxy002.setRegnRspn(new ProxyRegistrationResponse1());
		
		if(seed.getStatus().equals("ACTC")) 
			proxy002.getRegnRspn().setPrxRspnSts(ProxyStatusCode.ACTC);
		else 
			proxy002.getRegnRspn().setPrxRspnSts(ProxyStatusCode.RJCT);
		
		proxy002.getRegnRspn().setStsRsnInf(new ProxyStatusChoice());
		proxy002.getRegnRspn().getStsRsnInf().setPrtry(seed.getReason());
				
		// RegnRspn / OrgnlRegnTp
		if (orgnlMessage.getDocument().getPrxyRegn().getRegn().getRegnTp().value().equals("NEWR"))
			proxy002.getRegnRspn().setOrgnlRegnTp(ProxyRegistrationType1Code.NEWR);

		else if (orgnlMessage.getDocument().getPrxyRegn().getRegn().getRegnTp().value().equals("AMND"))
			proxy002.getRegnRspn().setOrgnlRegnTp(ProxyRegistrationType1Code.AMND);

		else if (orgnlMessage.getDocument().getPrxyRegn().getRegn().getRegnTp().value().equals("DEAC"))
			proxy002.getRegnRspn().setOrgnlRegnTp(ProxyRegistrationType1Code.DEAC);
		
		else if (orgnlMessage.getDocument().getPrxyRegn().getRegn().getRegnTp().value().equals("SUSP"))
			proxy002.getRegnRspn().setOrgnlRegnTp(ProxyRegistrationType1Code.SUSP);
		else if (orgnlMessage.getDocument().getPrxyRegn().getRegn().getRegnTp().value().equals("SUSB"))
			proxy002.getRegnRspn().setOrgnlRegnTp(ProxyRegistrationType1Code.SUSB);

		else if (orgnlMessage.getDocument().getPrxyRegn().getRegn().getRegnTp().value().equals("ACTV"))
			proxy002.getRegnRspn().setOrgnlRegnTp(ProxyRegistrationType1Code.ACTV);

		else if (orgnlMessage.getDocument().getPrxyRegn().getRegn().getRegnTp().value().equals("PORT"))
			proxy002.getRegnRspn().setOrgnlRegnTp(ProxyRegistrationType1Code.PORT);

		// +RegnRspn / ++PrxyRegn
		proxy002.getRegnRspn().getPrxyRegn().add(new ProxyRegistrationAccount1());
		proxy002.getRegnRspn().getPrxyRegn().get(0).setRegnId(seed.getRegnId());
		
		// +RegnRspn / ++PrxyRegn / +++Agt
		proxy002.getRegnRspn().getPrxyRegn().get(0).setAgt(new BranchAndFinancialInstitutionIdentification5());
		proxy002.getRegnRspn().getPrxyRegn().get(0).getAgt().setFinInstnId(new FinancialInstitutionIdentification8());
		proxy002.getRegnRspn().getPrxyRegn().get(0).getAgt().getFinInstnId().setOthr(new GenericFinancialIdentification1());
		proxy002.getRegnRspn().getPrxyRegn().get(0).getAgt().getFinInstnId().getOthr().setId(seed.getAgtId());

		// +SplmtryData

		// RegnRspn / PrxyRegn /+++SplmtryData
		Boolean splmntExists = false;
		
		if ((null != seed.getCstmrTp()) ||
		    (null != seed.getCstmrId()) ||
		    (null != seed.getCstmrRsdntSts()) ||
		    (null != seed.getCstmrTwnNm())) {
			
			splmntExists = true;
		}

		if (splmntExists) {

			List<BISupplementaryData1> suppl = new ArrayList<>();
			proxy002.setSplmtryData(suppl);
			proxy002.getSplmtryData().add(new BISupplementaryData1());
			proxy002.getSplmtryData().get(0).setEnvlp(new BISupplementaryDataEnvelopeDetail());
			proxy002.getSplmtryData().get(0).getEnvlp().setDtl(new BISupplementaryDataEnvelope1());
			
			BIAddtlCstmrInf addtlCstmrInf = new BIAddtlCstmrInf();

			if (null != seed.getCstmrId())
				addtlCstmrInf.setId(seed.getCstmrId());
			if (null != seed.getCstmrTp())
				addtlCstmrInf.setTp(seed.getCstmrTp());
			if (null != seed.getCstmrTwnNm())
				addtlCstmrInf.setTwnNm(seed.getCstmrTwnNm());
			if (null != seed.getCstmrRsdntSts())
				addtlCstmrInf.setRsdntSts(seed.getCstmrRsdntSts());
			
			proxy002.getSplmtryData().get(0).getEnvlp().getDtl().setCstmr(addtlCstmrInf);
			
		}			
		
		return proxy002;
	}

}
