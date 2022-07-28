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
public class SettlementHeaderService {
	@Autowired private UtilService utilService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

	public BusinessApplicationHeaderV01 getAppHdr() {
		
        		
		BusinessApplicationHeaderV01 appHdr = new BusinessApplicationHeaderV01();
		
		appHdr.setFr(new Party9Choice());
		appHdr.getFr().setFIId(new BranchAndFinancialInstitutionIdentification5());
		appHdr.getFr().getFIId().setFinInstnId(new FinancialInstitutionIdentification8());
		appHdr.getFr().getFIId().getFinInstnId().setOthr(new GenericFinancialIdentification1());
		appHdr.getFr().getFIId().getFinInstnId().getOthr().setId("FASTIDJA");
		
		appHdr.setTo(new Party9Choice());
		appHdr.getTo().setFIId(new BranchAndFinancialInstitutionIdentification5());
		appHdr.getTo().getFIId().setFinInstnId(new FinancialInstitutionIdentification8());
		appHdr.getTo().getFIId().getFinInstnId().setOthr(new GenericFinancialIdentification1());
		appHdr.getTo().getFIId().getFinInstnId().getOthr().setId("SIHBIDJI");

		appHdr.setMsgDefIdr("pacs.002.001.10");

		appHdr.setBizMsgIdr(utilService.genRfiBusMsgId("010", "02", "FASTIDJA"));	
		appHdr.setBizSvc("STTL");
			
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		
		XMLGregorianCalendar xcal;
		try {
			xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			appHdr.setCreDt(xcal);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		
		return appHdr;
	}



}
