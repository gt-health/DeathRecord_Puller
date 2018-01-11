package gatech.edu.FHIRController.DomainServices.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetURLResponse {
	
	@JsonProperty("domain")
	private String domain;
	@JsonProperty("URLs")
	private List<String> uRLs = null;

	@JsonProperty("domain")
	public String getDomain() {
	return domain;
	}

	@JsonProperty("domain")
	public void setDomain(String domain) {
	this.domain = domain;
	}

	@JsonProperty("URLs")
	public List<String> getURLs() {
	return uRLs;
	}

	@JsonProperty("URLs")
	public void setURLs(List<String> uRLs) {
	this.uRLs = uRLs;
	}
	
	@Override
	public String toString() {
		return "GetURLResponse [domain=" + domain + ", URLs=" + uRLs + "]";
	}
}
