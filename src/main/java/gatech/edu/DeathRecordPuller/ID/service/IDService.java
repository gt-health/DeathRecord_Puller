package gatech.edu.DeathRecordPuller.ID.service;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import gatech.edu.DeathRecordPuller.ID.model.IDEntry;

@Service
@Configuration
@ConfigurationProperties(prefix="IDService")
@Primary
public class IDService {
	String idServiceURL;
	RestTemplate restTemplate;
	public IDService() {
		restTemplate = new RestTemplate();
	}
	
	public IDEntry getIDEntry(Integer id) {
		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(idServiceURL).path("manage/").path(id.toString()).build();
		IDEntry output = restTemplate.getForEntity(uriComponents.toUri(), IDEntry.class).getBody();
		return output;
	}
}