package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;

@ResourceDef(name = "DatePronouncedDead", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-DatePronouncedDead")
public class DatePronouncedDead extends Observation {
	private static final long serialVersionUID = 1L;

	public DatePronouncedDead() {
		super();
		this.setCode(new CodeableConceptDt("http://loinc.org", "80616-6"));
	}

	public DatePronouncedDead(ResourceReferenceDt subject, DateTimeDt valueDateTime) {
		this();
		this.setSubject(subject);
		this.setValue(valueDateTime);
	}

}