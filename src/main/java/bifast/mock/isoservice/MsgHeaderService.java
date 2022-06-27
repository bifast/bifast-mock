package bifast.mock.isoservice;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bifast.library.iso20022.head001.BranchAndFinancialInstitutionIdentification5;
import bifast.library.iso20022.head001.BusinessApplicationHeaderV01;
import bifast.library.iso20022.head001.FinancialInstitutionIdentification8;
import bifast.library.iso20022.head001.GenericFinancialIdentification1;
import bifast.library.iso20022.head001.Party9Choice;
import bifast.mock.processor.UtilService;

@Service
public class MsgHeaderService {
	@Autowired private UtilService utilService;
	@Autowired private Lib2Config config;
	
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

	public BusinessApplicationHeaderV01 appHdr(String msgType, String bizMsgId) {
		
        		
		BusinessApplicationHeaderV01 appHdr = new BusinessApplicationHeaderV01();
		
		Party9Choice fr = new Party9Choice();
		fr.setFIId(new BranchAndFinancialInstitutionIdentification5());
		fr.getFIId().setFinInstnId(new FinancialInstitutionIdentification8());
		fr.getFIId().getFinInstnId().setOthr(new GenericFinancialIdentification1());
		fr.getFIId().getFinInstnId().getOthr().setId(config.getBicode());
		appHdr.setFr(fr);

		Party9Choice to = new Party9Choice();
		to.setFIId(new BranchAndFinancialInstitutionIdentification5());
		to.getFIId().setFinInstnId(new FinancialInstitutionIdentification8());
		to.getFIId().getFinInstnId().setOthr(new GenericFinancialIdentification1());
		to.getFIId().getFinInstnId().getOthr().setId(config.getBankcode());
		appHdr.setTo(to);

		appHdr.setMsgDefIdr(msgType);
		appHdr.setBizMsgIdr(bizMsgId);
		
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		
		XMLGregorianCalendar xcal;
		try {
//			gcal.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND);
			xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			appHdr.setCreDt(xcal);
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return appHdr;
	}


	public BusinessApplicationHeaderV01 getAppHdr(String msgType, String bizMsgId) {
		
		BusinessApplicationHeaderV01 appHdr = appHdr( msgType,  bizMsgId);
		return appHdr;
	}

	public BusinessApplicationHeaderV01 genAppHdr(String isoMsgCode, String msgType, String instRole) {
		
		String bizmsgid = "";
		
		if (instRole.equals("H")) {
			bizmsgid = utilService.genHubBusMsgId(msgType);
			
		}

		BusinessApplicationHeaderV01 appHdr = getAppHdr( msgType,  bizmsgid);
		return appHdr;
	}

	
}
