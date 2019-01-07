package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;

@ResourceDef(name = "TimingOfRecentPregnancyInRelationToDeath", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-TimingOfRecentPregnancyInRelationToDeath")
public class TimingOfRecentPregnancyInRelationToDeath extends Observation {
	private static final long serialVersionUID = 1L;

	// TODO: Add valueset checking value
	// https://nightingaleproject.github.io/fhir-death-record/guide/ValueSet-sdr-causeOfDeath-PregnancyStatusVS.html
	public TimingOfRecentPregnancyInRelationToDeath() {
		super();
		this.setCode(new CodeableConceptDt("http://loinc.org", "69442-2"));
	}

	public TimingOfRecentPregnancyInRelationToDeath(ResourceReferenceDt subject,
			CodeableConceptDt valueCodeableConcept) {
		this();
		this.setSubject(subject);
		this.setValue(valueCodeableConcept);
	}
}