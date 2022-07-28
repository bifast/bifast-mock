package bifast.mock.persist;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CT_RESPONSE")
public class CTResponse {

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Column(name="bizsvc")
    private String bizSvc;
    @Column(name="endtoendid")
    private String endToEndId;
    
    @Column(name="json_request", length=5000)
    private String jsonCtRequest;
//    @Column(name="json_response",columnDefinition="CLOB(10K)")
//    private String jsonCtResponse;
    
    private String response;
    private String cdtrAcct;
    
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	public String getBizSvc() {
		return bizSvc;
	}
	public void setBizSvc(String bizSvc) {
		this.bizSvc = bizSvc;
	}
	public String getEndToEndId() {
		return endToEndId;
	}
	public void setEndToEndId(String endToEndId) {
		this.endToEndId = endToEndId;
	}
	public String getJsonCtRequest() {
		return jsonCtRequest;
	}
	public void setJsonCtRequest(String jsonCtRequest) {
		this.jsonCtRequest = jsonCtRequest;
	}
//	public String getJsonCtResponse() {
//		return jsonCtResponse;
//	}
//	public void setJsonCtResponse(String jsonCtResponse) {
//		this.jsonCtResponse = jsonCtResponse;
//	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getCdtrAcct() {
		return cdtrAcct;
	}
	public void setCdtrAcct(String cdtrAcct) {
		this.cdtrAcct = cdtrAcct;
	}

    
}
