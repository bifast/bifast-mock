package bifast.mock.isoservice;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

public class Proxy006Seed {
	
	private String msgId; 
	private String creDtTm;
	
	private String status; 
	private String reason;
	
	private String OrgnlMsgId;
	private String OrgnlMsgNmId;
	private XMLGregorianCalendar OrgnlCreDtTm;
    
	private List<Proxy006SeedAccount> proxy006SeedAccountList;
   
	private String cstmrTwnNm;

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getCreDtTm() {
		return creDtTm;
	}

	public void setCreDtTm(String creDtTm) {
		this.creDtTm = creDtTm;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOrgnlMsgId() {
		return OrgnlMsgId;
	}

	public void setOrgnlMsgId(String orgnlMsgId) {
		OrgnlMsgId = orgnlMsgId;
	}

	public String getOrgnlMsgNmId() {
		return OrgnlMsgNmId;
	}

	public void setOrgnlMsgNmId(String orgnlMsgNmId) {
		OrgnlMsgNmId = orgnlMsgNmId;
	}

	public XMLGregorianCalendar getOrgnlCreDtTm() {
		return OrgnlCreDtTm;
	}

	public void setOrgnlCreDtTm(XMLGregorianCalendar orgnlCreDtTm) {
		OrgnlCreDtTm = orgnlCreDtTm;
	}

	public List<Proxy006SeedAccount> getProxy006SeedAccountList() {
		return proxy006SeedAccountList;
	}

	public void setProxy006SeedAccountList(List<Proxy006SeedAccount> proxy006SeedAccountList) {
		this.proxy006SeedAccountList = proxy006SeedAccountList;
	}

	public String getCstmrTwnNm() {
		return cstmrTwnNm;
	}

	public void setCstmrTwnNm(String cstmrTwnNm) {
		this.cstmrTwnNm = cstmrTwnNm;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
    
	
	
}
