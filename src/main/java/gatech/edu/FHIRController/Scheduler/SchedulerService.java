package gatech.edu.FHIRController.Scheduler;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

@Service
@Configuration
@ConfigurationProperties(prefix="scheduler")
public class SchedulerService {

	private static final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX); 
	private static final CronParser parser = new CronParser(cronDefinition);
	private String scriptFolder;
	private String scriptUser;
	public int addScheduleService(String cronString) throws Exception{
		Cron myCron = parser.parse(cronString);
		File addJobShellScript = new File(scriptFolder,"addJob.sh");
		Process p = Runtime.getRuntime().exec("sh "+addJobShellScript.getPath()+" "+scriptUser+" "+myCron.toString());
		return p.waitFor();
	}
	
	public int removeScheduleService(String cronString) throws Exception{
		Cron myCron = parser.parse(cronString);
		File removeJobShellScript = new File(scriptFolder,"addJob.sh");
		Process p = Runtime.getRuntime().exec("sh "+removeJobShellScript.getPath()+" "+scriptUser+" "+myCron.toString());
		return p.waitFor();
	}
	
	public String getScriptFolder() {
		return scriptFolder;
	}

	public void setScriptFolder(String scriptFolder) {
		this.scriptFolder = scriptFolder;
	}

	public String getScriptUser() {
		return scriptUser;
	}

	public void setScriptUser(String scriptUser) {
		this.scriptUser = scriptUser;
	}
}