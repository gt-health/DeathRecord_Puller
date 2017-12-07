package gatech.edu.FHIRController.DomainServices;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import gatech.edu.FHIRController.DomainServices.model.GetURLResponse;
import gatech.edu.FHIRController.DomainServices.model.LoginResponse;

@Service
public class DomainService {
	@Autowired protected ConnectionConfiguration connectionConfig;
	
	public List<URL> getDomains(String username) throws IllegalArgumentException, URISyntaxException, RestClientException, MalformedURLException{
		List<URL> returnList = new ArrayList<URL>();
		RestTemplate restTemplate = new RestTemplate();
		String password = "";
		try {
			password = connectionConfig.getPasswords().get(username);
		}
		catch(NullPointerException e) {
			throw new IllegalArgumentException("Missing pre-loaded password from properties file for username "+username);
		}
		Map<String,String> uriLoginVariables = new HashMap<String,String>();
		uriLoginVariables.put("username", username);
		uriLoginVariables.put("password", password);
		URI loginURI = new URI(connectionConfig.getUrl() + "/login");
		LoginResponse loginResponse = restTemplate.getForObject(loginURI.toURL().toString(), LoginResponse.class, uriLoginVariables);
		
		Map<String,String> uriGETURLVariables = new HashMap<String,String>();
		uriGETURLVariables.put("username", username);
		uriGETURLVariables.put("token", loginResponse.getToken());
		URI getURLURI = new URI(connectionConfig.getUrl() + "/URL");
		
		GetURLResponse getURLResponse = restTemplate.getForObject(getURLURI.toURL().toString(), GetURLResponse.class, uriGETURLVariables); //Find better names please
		for(String stringURL : getURLResponse.getURLs()) {
			returnList.add(new URI(stringURL).toURL());
		}
		return returnList;
	}
}
