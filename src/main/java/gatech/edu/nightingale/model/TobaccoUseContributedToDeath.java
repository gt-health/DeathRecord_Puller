package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.BooleanDt;

@ResourceDef(name = "TobaccoUseContributedToDeath", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-TobaccoUseContributedToDeath")
public class TobaccoUseContributedToDeath extends Observation {
	private static final long serialVersionUID = 1L;

	public TobaccoUseContributedToDeath() {
		super();
		this.setCode(new CodeableConceptDt("http://loinc.org", "69443-0"));
	}

	public TobaccoUseContributedToDeath(ResourceReferenceDt subject, BooleanDt valueBoolean) {
		this();
		this.setSubject(subject);
		this.setValue(valueBoolean);
	}
}