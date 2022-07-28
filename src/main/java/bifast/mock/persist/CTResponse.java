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
    private String creditorName;
    
    private String response;
    private String reason;
    
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
	public String getCreditorName() {
		return creditorName;
	}
	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
    
    
}
