package gatech.edu.FHIRController.DomainServices.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetURLResponse {
	private String domain;
	private List<String> URLs;
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public List<String> getURLs() {
		return URLs;
	}
	public void setURLs(List<String> uRLs) {
		URLs = uRLs;
	}
	@Override
	public String toString() {
		return "GetURLResponse [domain=" + domain + ", URLs=" + URLs + "]";
	}
}
