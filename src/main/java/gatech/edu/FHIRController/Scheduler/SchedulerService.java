package gatech.edu.FHIRController.Scheduler;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.threeten.bp.ZonedDateTime;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.common.base.Optional;

import gatech.edu.STIECR.DB.model.ECRJob;

@Service
@Configuration
@ConfigurationProperties(prefix="scheduler")
public class SchedulerService {

	private static final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX); 
	private static final CronParser parser = new CronParser(cronDefinition);
	private String scriptFolder;
	private String scriptUser;
	public int addScheduleService(String cronString, ECRJob ecrjob) throws Exception{
		Cron myCron = parser.parse(cronString);
		ExecutionTime executionTime = ExecutionTime.forCron(myCron);
		Optional<ZonedDateTime> nextRun = executionTime.nextExecution(ZonedDateTime.now());
		if(nextRun.isPresent()) {
			
			DateTimeFormatter ISOFormatter = ISODateTimeFormat.dateTimeNoMillis();
			Date dateNextRun = ISOFormatter.parseDateTime(nextRun.get().toString()).toDate();
			ecrjob.setNextRunDate(dateNextRun);
		}
		File addJobShellScript = new File(scriptFolder,"addJob.sh");
		Process p = Runtime.getRuntime().exec("sh "+addJobShellScript.getPath()+" "+scriptUser+" "+myCron.toString()+" "+ecrjob.getReportId());
		return p.waitFor();
	}
	
	public int removeScheduleService(ECRJob ecrjob) throws Exception{
		File removeJobShellScript = new File(scriptFolder,"removeJob.sh");
		Process p = Runtime.getRuntime().exec("sh "+removeJobShellScript.getPath()+" "+scriptUser+" "+ecrjob.getReportId());
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