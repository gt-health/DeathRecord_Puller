package gatech.edu.FHIRController.PHCRClient;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import gatech.edu.STIECR.JSON.ECR;

@Service
@Configuration
@ConfigurationProperties(prefix="PHCR.client")
public class PHCRClientService {

	public String serverBaseURL;

	public String getserverBaseURL() {
		return serverBaseURL;
	}

	public void setserverBaseURL(String serverBaseURL) {
		this.serverBaseURL = serverBaseURL;
	}
	
	public ECR requestECRById(Integer id) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<ECR> response = restTemplate.exchange(serverBaseURL+"/ECR?id={id}", HttpMethod.GET, entity, ECR.class, id);
		return response.getBody();
	}
	
	public void putECR(ECR ecr) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<ECR> entity = new HttpEntity<>(ecr);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.put(serverBaseURL+"/ECR", entity);
	}
}