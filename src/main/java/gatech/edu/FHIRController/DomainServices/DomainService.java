package gatech.edu.FHIRController.DomainServices;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import gatech.edu.FHIRController.DomainServices.model.GetURLResponse;
import gatech.edu.FHIRController.DomainServices.model.LoginResponse;

@Service
public class DomainService {
        private static final Logger log = LoggerFactory.getLogger(DomainService.class);
	@Autowired protected ConnectionConfiguration connectionConfig;
	
	public List<URL> getURLs(String username) throws IllegalArgumentException, URISyntaxException, RestClientException, MalformedURLException{
		LoginResponse loginResponse = login(username);
		
		List<URL> returnList = new ArrayList<URL>();
		RestTemplate restTemplate = new RestTemplate();
		Map<String,String> uriGETURLVariables = new HashMap<String,String>();
		uriGETURLVariables.put("username", username);
		uriGETURLVariables.put("token", loginResponse.getToken());
		URI getURLURI = new URI(connectionConfig.getUrl() + "/URL");
		log.info("DOMAINSERIVCE --- URL GET PARAMETERS" + " username:" + uriGETURLVariables.get("username") + ",token:"+ uriGETURLVariables.get("token"));
		GetURLResponse getURLResponse = restTemplate.getForObject(getURLURI.toURL().toString() +"?domain="+uriGETURLVariables.get("username")+"&token="+uriGETURLVariables.get("token"), GetURLResponse.class); //Find better names please
		for(String stringURL : getURLResponse.getURLs()) {
			returnList.add(new URI(stringURL).toURL());
		}
		return returnList;
	}
	
	public List<URL> getURLs(String username,String domain) throws RestClientException, MalformedURLException, URISyntaxException{
		LoginResponse loginResponse = login(username);
		List<URL> returnList = new ArrayList<URL>();
		RestTemplate restTemplate = new RestTemplate();
		Map<String,String> uriGETURLVariables = new HashMap<String,String>();
		uriGETURLVariables.put("username", username);
		uriGETURLVariables.put("token", loginResponse.getToken());
		uriGETURLVariables.put("domain", domain);
		URI getURLURI = new URI(connectionConfig.getUrl() + "/URL");
		log.info("DOMAINSERIVCE --- URL GET PARAMETERS" + " username:" + uriGETURLVariables.get("username") + ",token:"+ uriGETURLVariables.get("token") + ",domain:" + uriGETURLVariables.get("domain"));
		GetURLResponse getURLResponse = restTemplate.getForObject(getURLURI.toURL().toString() +"?domain="+uriGETURLVariables.get("domain")+"&token="+uriGETURLVariables.get("token"), GetURLResponse.class); //Find better names please
		for(String stringURL : getURLResponse.getURLs()) {
			returnList.add(new URI(stringURL).toURL());
		}
		return returnList;
	}
	
	public LoginResponse login(String username) throws URISyntaxException, RestClientException, MalformedURLException {
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
		log.info("DOMAINSERIVCE --- LOGIN PARAMETERS" + " Username:" + uriLoginVariables.get("username") + ",Password:"+ uriLoginVariables.get("password"));
		URI loginURI = new URI(connectionConfig.getUrl() + "/login");
		LoginResponse loginResponse = restTemplate.getForObject(loginURI.toURL().toString() + "?username="+uriLoginVariables.get("username")+"&password="+uriLoginVariables.get("password"), LoginResponse.class);
		return loginResponse;
	}
}
