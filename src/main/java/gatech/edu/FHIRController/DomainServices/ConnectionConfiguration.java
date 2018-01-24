package gatech.edu.FHIRController.DomainServices;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="domainservice")
public class ConnectionConfiguration {
	@Autowired
	private Environment env;
	
	private String url = "";
	private String defaultUsername = "";
	private final Map<String, String> passwords = new HashMap<>();
	
	public Map<String, String> getPasswords() {
		return passwords;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getDefaultUsername() {
		return defaultUsername;
	}
	public void setDefaultUsername(String defaultUsername) {
		this.defaultUsername = defaultUsername;
	}
}
