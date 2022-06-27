package bifast.mock.isoservice;

import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.stereotype.Service;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.library.iso20022.prxy004.AccountIdentification4Choice;
import bifast.library.iso20022.prxy004.CashAccount40;
import bifast.library.iso20022.prxy004.CashAccountType2ChoiceProxy;
import bifast.library.iso20022.prxy004.GenericAccountIdentification1;
import bifast.library.iso20022.prxy004.ProxyDefinition1;
import bifast.library.iso20022.prxy004.ProxyLookUpAccount1;
import bifast.library.iso20022.prxy004.ProxyLookUpRegistration1;
import bifast.library.iso20022.prxy004.ProxyLookUpResponse1;
import bifast.library.iso20022.prxy004.ProxyLookUpResponseV01;
import bifast.library.iso20022.prxy004.BIAddtlCstmrInf;
import bifast.library.iso20022.prxy004.BISupplementaryData1;
import bifast.library.iso20022.prxy004.BISupplementaryDataEnvelope1;
import bifast.library.iso20022.prxy004.BranchAndFinancialInstitutionIdentification5;
import bifast.library.iso20022.prxy004.FinancialInstitutionIdentification8;
import bifast.library.iso20022.prxy004.GenericFinancialIdentification1;
import bifast.library.iso20022.prxy004.GroupHeader60;
import bifast.library.iso20022.prxy004.OriginalGroupInformation3;
import bifast.library.iso20022.prxy004.Party12Choice;
import bifast.library.iso20022.prxy004.ProxyStatusChoice;
import bifast.library.iso20022.prxy004.ProxyStatusCode;
import bifast.library.iso20022.prxy004.SupplementaryDataEnvelope1;

@Service
public class Proxy004MessageService {
	
	
	public ProxyLookUpResponseV01 proxyResolutionResponse (Proxy004Seed seed, 
															BusinessMessage orgnlMessage) 
															throws DatatypeConfigurationException {
		
		
		//Response
		ProxyLookUpResponseV01 response = new ProxyLookUpResponseV01();
		
		response.setGrpHdr(new GroupHeader60());
		response.getGrpHdr().setMsgId(seed.getMsgId());
		
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTimeZone(TimeZone.getTimeZone(ZoneOffset.systemDefault()));
		XMLGregorianCalendar xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		response.getGrpHdr().setCreDtTm(xcal);
		
		// GrpHdr / ++MsgRcpt / Agt/ FinInstnId/ Othr /Id
		response.getGrpHdr().setMsgRcpt(new Party12Choice());
		response.getGrpHdr().getMsgRcpt().setAgt(new BranchAndFinancialInstitutionIdentification5());
		response.getGrpHdr().getMsgRcpt().getAgt().setFinInstnId(new FinancialInstitutionIdentification8());
		response.getGrpHdr().getMsgRcpt().getAgt().getFinInstnId().setOthr(new GenericFinancialIdentification1());
		response.getGrpHdr().getMsgRcpt().getAgt().getFinInstnId().getOthr().setId("INDOIDJA");
				
		//OrgnlGrpInf / ++OrgnlMsgId
		response.setOrgnlGrpInf(new OriginalGroupInformation3());
		response.getOrgnlGrpInf().setOrgnlMsgId(orgnlMessage.getDocument().getPrxyLookUp().getGrpHdr().getMsgId());
		//+OrgnlGrpInf / ++OrgnlMsgNmId
		response.getOrgnlGrpInf().setOrgnlMsgNmId(orgnlMessage.getAppHdr().getMsgDefIdr());
		//+OrgnlGrpInf / ++OrgnlCreDtTm
		response.getOrgnlGrpInf().setOrgnlCreDtTm(orgnlMessage.getAppHdr().getCreDt());

		//++LkUpRspn / ++OrgnlId
		response.setLkUpRspn(new ProxyLookUpResponse1());
		response.getLkUpRspn().setOrgnlId(orgnlMessage.getDocument().getPrxyLookUp().getLookUp().getPrxyOnly().getId());
				  
		  
		//+LkUpRspn/++OrgnlPrxyRtrvl
		response.getLkUpRspn().setOrgnlPrxyRtrvl(new ProxyDefinition1());

		response.getLkUpRspn().getOrgnlPrxyRtrvl().setTp(orgnlMessage.getDocument().getPrxyLookUp().getLookUp().getPrxyOnly().getPrxyRtrvl().getTp());
		response.getLkUpRspn().getOrgnlPrxyRtrvl().setVal(orgnlMessage.getDocument().getPrxyLookUp().getLookUp().getPrxyOnly().getPrxyRtrvl().getVal());
		 
		//+LkUpRspn/++RegnRspn
		response.getLkUpRspn().setRegnRspn(new ProxyLookUpRegistration1());
		
		//+LkUpRspn/++RegnRspn/+++PrxRspnSts	
		if(seed.getStatus().equals("ACTC")) 
			response.getLkUpRspn().getRegnRspn().setPrxRspnSts(ProxyStatusCode.ACTC);
		else
			response.getLkUpRspn().getRegnRspn().setPrxRspnSts(ProxyStatusCode.RJCT);

		response.getLkUpRspn().getRegnRspn().setStsRsnInf(new ProxyStatusChoice());
		response.getLkUpRspn().getRegnRspn().getStsRsnInf().setPrtry(seed.getReason());;
		
		//+LkUpRspn/++RegnRspn/+++Prxy
		response.getLkUpRspn().getRegnRspn().setPrxy(new ProxyDefinition1());
		response.getLkUpRspn().getRegnRspn().getPrxy().setTp(orgnlMessage.getDocument().getPrxyLookUp().getLookUp().getPrxyOnly().getPrxyRtrvl().getTp());
		response.getLkUpRspn().getRegnRspn().getPrxy().setVal(orgnlMessage.getDocument().getPrxyLookUp().getLookUp().getPrxyOnly().getPrxyRtrvl().getVal());

		//+LkUpRspn/++RegnRspn/+++Regn
		response.getLkUpRspn().getRegnRspn().setRegn(new ProxyLookUpAccount1());
		
		response.getLkUpRspn().getRegnRspn().getRegn().setRegnId(seed.getRegnId());
		response.getLkUpRspn().getRegnRspn().getRegn().setDsplNm(seed.getDisplayName());
		
		//+LkUpRspn/++RegnRspn/+++Regn/++++Agt/+++++FinInstnId/++++++Othr/+++++++Id
		response.getLkUpRspn().getRegnRspn().getRegn().setAgt(new BranchAndFinancialInstitutionIdentification5());
		response.getLkUpRspn().getRegnRspn().getRegn().getAgt().setFinInstnId(new FinancialInstitutionIdentification8());
		response.getLkUpRspn().getRegnRspn().getRegn().getAgt().getFinInstnId().setOthr(new GenericFinancialIdentification1());
		response.getLkUpRspn().getRegnRspn().getRegn().getAgt().getFinInstnId().getOthr().setId(seed.getRegisterBank());
		
		//+LkUpRspn/++RegnRspn/+++Regn/++++Acct
		response.getLkUpRspn().getRegnRspn().getRegn().setAcct(new CashAccount40());
		response.getLkUpRspn().getRegnRspn().getRegn().getAcct().setId(new AccountIdentification4Choice());
		response.getLkUpRspn().getRegnRspn().getRegn().getAcct().getId().setOthr(new GenericAccountIdentification1());
		
		response.getLkUpRspn().getRegnRspn().getRegn().getAcct().getId().getOthr().setId(seed.getAccountNumber());
		
		response.getLkUpRspn().getRegnRspn().getRegn().getAcct().setTp(new CashAccountType2ChoiceProxy());
		response.getLkUpRspn().getRegnRspn().getRegn().getAcct().getTp().setPrtry(seed.getAccountType());;

		response.getLkUpRspn().getRegnRspn().getRegn().getAcct().setNm(seed.getAccountName());
		
		//+SplmtryData
		if ((null != seed.getCstmrId()) ||
			(null != seed.getCstmrTp()) ||
			(null != seed.getCstmrRsdntSts()) ||
			(null != seed.getCstmrTwnNm()) ) {
			
			response.getSplmtryData().add(new BISupplementaryData1());
			response.getSplmtryData().get(0).setEnvlp(new SupplementaryDataEnvelope1());
			response.getSplmtryData().get(0).getEnvlp().setDtl(new BISupplementaryDataEnvelope1());
			
			BIAddtlCstmrInf cstmr = new BIAddtlCstmrInf();
			if (null != seed.getCstmrId())
				cstmr.setId(seed.getCstmrId());
			if (null != seed.getCstmrTp())
				cstmr.setTp(seed.getCstmrTp());
			if (null != seed.getCstmrTwnNm())
				cstmr.setTwnNm(seed.getCstmrTwnNm());
			if (null != seed.getCstmrRsdntSts())
				cstmr.setRsdntSts(seed.getCstmrRsdntSts());
			
			response.getSplmtryData().get(0).getEnvlp().getDtl().setCstmr(cstmr);
		}
		
		return response;
	}

}
