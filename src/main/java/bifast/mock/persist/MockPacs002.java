package bifast.mock.persist;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MockPacs002 {
    
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;
    private String bizMsgIdr;
    private String orgnlMsgId;
    private String orgnlMsgName;
    private String trxType;
    private String orgnlEndToEndId;
    @Column(length=5000)
    private String fullMessage;
    @Column(length=5000)
    private String ctRequest;
    private String result;
    private String sttl;
    private String cdtrAcct;
    private String dbtrAcct;


    public String getTrxType() {
        return trxType;
    }
    public void setTrxType(String trxType) {
        this.trxType = trxType;
    }
    

    public Long getId() {
        return Id;
    }
    public void setId(Long id) {
        Id = id;
    }
    public String getBizMsgIdr() {
        return bizMsgIdr;
    }
    public void setBizMsgIdr(String bizMsgIdr) {
        this.bizMsgIdr = bizMsgIdr;
    }
    public String getOrgnlMsgId() {
        return orgnlMsgId;
    }
    public void setOrgnlMsgId(String orgnlMsgId) {
        this.orgnlMsgId = orgnlMsgId;
    }
    public String getOrgnlMsgName() {
        return orgnlMsgName;
    }
    public void setOrgnlMsgName(String orgnlMsgName) {
        this.orgnlMsgName = orgnlMsgName;
    }
    public String getOrgnlEndToEndId() {
        return orgnlEndToEndId;
    }
    public void setOrgnlEndToEndId(String orgnlEndToEndId) {
        this.orgnlEndToEndId = orgnlEndToEndId;
    }
    public String getFullMessage() {
        return fullMessage;
    }
    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getCtRequest() {
		return ctRequest;
	}
	public void setCtRequest(String ctRequest) {
		this.ctRequest = ctRequest;
	}
	public String getCdtrAcct() {
		return cdtrAcct;
	}
	public void setCdtrAcct(String cdtrAcct) {
		this.cdtrAcct = cdtrAcct;
	}
	public String getDbtrAcct() {
		return dbtrAcct;
	}
	public void setDbtrAcct(String dbtrAcct) {
		this.dbtrAcct = dbtrAcct;
	}
	public String getSttl() {
		return sttl;
	}
	public void setSttl(String sttl) {
		this.sttl = sttl;
	}




}
