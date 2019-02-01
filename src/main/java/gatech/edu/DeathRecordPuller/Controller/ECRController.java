package gatech.edu.DeathRecordPuller.Controller;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gatech.edu.DeathRecordPuller.service.ECRService;
import gatech.edu.STIECR.JSON.ECR;

@CrossOrigin()
@RestController
public class ECRController {
	
	protected FhirContext ctx3;
	protected IParser jsonParser3;
	@Autowired
	PatientEverythingController patientEverythingController;
	@Autowired
	ECRService ecrService;
	
	public ECRController(){
		ctx3 = FhirContext.forDstu3();
		jsonParser3 = ctx3.newJsonParser();
	}
	
	@RequestMapping(value = "/ECR", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<List<ECR> > getECRUsingIDService(@RequestParam() String id, @RequestParam(required = false) String lastName,
			@RequestParam(required = false) String firstName,@RequestParam(required = false) String zipCode,
			@RequestParam(required = false) String diagnosisCode, @RequestParam(required = false) Integer page) throws JsonProcessingException{
		String jsonResults = patientEverythingController.getPatientEverythingIdServiceEnabled(id, "Jefferson County Coroner/Medical Examiner Office", "", lastName, firstName).getBody();
		Bundle fhirRecords = (Bundle)jsonParser3.parseResource(jsonResults);
		ECR ecr = ecrService.ecrFromFHIRRecords(fhirRecords);
		ecr.setECRId(id);
		List<ECR> returnList = new ArrayList<ECR>();
		returnList.add(ecr);
		return new ResponseEntity<List<ECR> >(returnList,HttpStatus.OK);
	}
	
}
