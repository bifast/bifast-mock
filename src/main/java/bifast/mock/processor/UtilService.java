package bifast.mock.processor;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import bifast.library.iso20022.custom.BusinessMessage;
import bifast.mock.isoservice.Lib2Config;

@Service
public class UtilService {

	@Autowired
	private Lib2Config config;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

	public Integer getInboundCounter () {
		
		Random rand = new Random();
        int posbl4 = rand.nextInt(2000);

		return Integer.valueOf(posbl4);
	}

	public String genMessageId (String trxType, String bankCode) {
		String strToday = LocalDateTime.now().format(formatter);
		DecimalFormat df = new DecimalFormat("00000000");
		String strCounter = df.format(getInboundCounter());
		String msgId = strToday + bankCode + trxType + strCounter;
		return msgId;
	}
	

	public String genHubBusMsgId (String trxType) {
		String strToday = LocalDateTime.now().format(formatter);
		DecimalFormat df = new DecimalFormat("00000000");
		String strCounter = df.format(getInboundCounter());
		return strToday + config.getBicode() +  trxType + "H99" + strCounter;
	}
	
	public String genRfiBusMsgId (String trxType, String channel, String bankCode ) {
		String strToday = LocalDateTime.now().format(formatter);
		DecimalFormat df = new DecimalFormat("00000000");
		String strCounter = df.format(getInboundCounter());
		String msgId = strToday + bankCode + trxType + "R" + channel + strCounter;
		return msgId;
	}

	public String serializeBusinessMessage (BusinessMessage bm) throws JsonProcessingException {
		ObjectMapper map = new ObjectMapper();
		map.registerModule(new JaxbAnnotationModule());
		map.enable(SerializationFeature.WRAP_ROOT_VALUE);
	    map.setSerializationInclusion(Include.NON_NULL);
		String resp = map.writeValueAsString(bm);
		return resp;
	}

	public BusinessMessage deserializeBusinessMessage (String str) throws JsonProcessingException {
    	ObjectMapper map = new ObjectMapper();
    	map.registerModule(new JaxbAnnotationModule());
    	map.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
    	map.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    	BusinessMessage msg = map.readValue(str, BusinessMessage.class);
    	return msg;
	}


}
