/**
 * 
 */
package gatech.edu.FHIRController.Controller;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Patient.Link;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gatech.edu.FHIRController.DomainServices.ConnectionConfiguration;
import gatech.edu.FHIRController.DomainServices.DomainService;
import gatech.edu.FHIRController.PHCRClient.PHCRClientService;
import gatech.edu.STIECR.JSON.CodeableConcept;
import gatech.edu.STIECR.JSON.ECR;
import gatech.edu.STIECR.JSON.utils.ECRJsonConverter;
import gatech.edu.STIECR.controller.ControllerUtils;
import gatech.edu.common.FHIR.client.ClientService;
import junit.framework.TestCase;

/**
 * @author taylorde
 *
 */
public class TestFHIRController extends TestCase {

	private void tstRetrieveLinkedPatients() {
		
		FhirContext context = FhirContext.forDstu2();
		IGenericClient client = context.newRestfulGenericClient("http://localhost:21234/fhir");
		
		Bundle patients = client
							.search()
							.forResource(Patient.class)
							.and(Patient.IDENTIFIER.exactly().identifier("70120426"))
							.and(Patient.ORGANIZATION.hasId("PMLW|HNA"))
							.include(Patient.INCLUDE_LINK)
							.returnBundle(Bundle.class)
							.execute();
		
		Patient firstPat = (Patient)patients.getEntryFirstRep().getResource();
		
		for ( Link link : firstPat.getLink()) {
			{
				Patient pat = (Patient)link.getOther().getResource();
				System.out.println("Pat1 = " + ((pat == null) ? "null" : ((pat.getId() == null) ? "null id" : pat.getId().getValueAsString())));
				System.out.println("ID is Local = " + pat.getId().isLocal());
			}
			{
				Patient pat = (Patient)link.getOther().loadResource(client);
				System.out.println("Pat2 = " + pat.getId().getValueAsString());
				System.out.println("ID is Local = " + pat.getId().isLocal());
			}
		}
	}
}
