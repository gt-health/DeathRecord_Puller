package gatech.edu.FHIR_Controller.FHIR.model;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import gatech.edu.PHCR_Controller.ECR.JSON.ECR;

public class FHIRToECRMapper {
	
	public ECR mapPatientIntoECR(ECR ecr,Patient patient) {
		ecr.getpatient().setsex(patient.getGender().toString());
		ecr.getpatient().setname(patient.getName().get(0).getGivenAsSingleString(),patient.getName().get(0).getFamilyAsSingleString());
		return ecr;
	}
}
