package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.BooleanDt;

@ResourceDef(name = "MedicalExaminerContacted", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-MedicalExaminerContacted")
public class MedicalExaminerContacted extends Observation {
	private static final long serialVersionUID = 1L;

	// TODO: Add valueset checking value
	// https://nightingaleproject.github.io/fhir-death-record/guide/ValueSet-sdr-causeOfDeath-MannerOfDeathVS.html
	public MedicalExaminerContacted() {
		super();
		this.setCode(new CodeableConceptDt("http://loinc.org", "74497-9"));
	}

	public MedicalExaminerContacted(ResourceReferenceDt subject, BooleanDt valueBoolean) {
		this();
		this.setSubject(subject);
		this.setValue(valueBoolean);
	}

}