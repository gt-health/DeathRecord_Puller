package gatech.edu.FHIR_Controller.FHIR.Client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

@RestController
public class TestClient {

	@Autowired
	public TestClient() {
		
	}
	@RequestMapping(value = "/Test", method = RequestMethod.GET)
	public ResponseEntity<String> test() {
		FhirContext ctx = FhirContext.forDstu2();
		String serverBase = "https://open-ic.epic.com/FHIR/api/FHIR/DSTU2/";
		
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);
		IParser jsonParser = ctx.newJsonParser().setPrettyPrint(true);
		
		Bundle results = client.search().forResource(Patient.class).where(Patient.FAMILY.matches().value("Argonaut")).and(Patient.GIVEN.matches().value("Jason")).returnBundle(Bundle.class).execute();
		
		ResponseEntity<String> response = new ResponseEntity<String>(jsonParser.encodeResourceToString(results), HttpStatus.OK);
		return response;
	}
}