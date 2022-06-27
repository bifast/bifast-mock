package bifast.mock.processor;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bifast.mock.isoservice.Lib2Config;
import bifast.mock.persist.MockNames;
import bifast.mock.persist.MockNamesRepository;

@Service
public class UtilService {

	@Autowired
	private Lib2Config config;
	@Autowired
	private MockNamesRepository mockNamesRepo;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

//	public Integer getCounter () {
//        Integer intNow = Integer.valueOf(LocalDateTime.now().format(formatter));
//        
//		Optional<InboundCounter> optCnt = inboundCounterRepo.findById(intNow); 
//		if (optCnt.isPresent()) {
//			InboundCounter msgCnt = optCnt.get();
//			msgCnt.setLastNumber(msgCnt.getLastNumber()+1);
//			inboundCounterRepo.save(msgCnt);
//			return (msgCnt.getLastNumber());
//		}
//		else {
//			InboundCounter msgCnt = new InboundCounter(intNow, 50000001);
//			inboundCounterRepo.save(msgCnt);
//			return (50000001);
//		}
//	}

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
		return strToday + config.getBicode() +  trxType + "H03" + strCounter;
	}
	
	public String genRfiBusMsgId (String trxType, String channel, String bankCode ) {
		String strToday = LocalDateTime.now().format(formatter);
		DecimalFormat df = new DecimalFormat("00000000");
		String strCounter = df.format(getInboundCounter());
		String msgId = strToday + bankCode + trxType + "R" + channel + strCounter;
		return msgId;
	}

	public String genBizMsgId (String oldId, String trxType) {
		String newId = oldId.substring(22, 30);
		String newDt = oldId.substring(0,8);
		return newDt + config.getBankcode() + trxType + "R99" + newId;
	}
	
	public String getFullName () {

		List<MockNames> names = mockNamesRepo.findAll();

		Random rand = new Random();
        int posbl1 = rand.nextInt(names.size());
        int posbl2 = rand.nextInt(names.size());
        if (posbl1 == posbl2)
        	posbl2 = rand.nextInt(names.size());

        String name = names.get(posbl1).getName() + " " + names.get(posbl2).getName();
        return name;
		
	}

}
