package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.valueset.ConditionClinicalStatusCodesEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;

@ResourceDef(name = "CauseOfDeathCondition", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-CauseOfDeathCondition")
public class CauseOfDeathCondition extends Condition {
	private static final long serialVersionUID = 1L;

	public CauseOfDeathCondition() {
		super();
		this.setClinicalStatus(ConditionClinicalStatusCodesEnum.ACTIVE);
	}

	public CauseOfDeathCondition(ResourceReferenceDt subject, DateTimeDt onset) {
		this();
		this.setPatient(subject);
		this.setOnset(onset);
	}

	public ResourceReferenceDt getSubject() {
		return this.getPatient();
	}

	public void setSubject(ResourceReferenceDt subject) {
		this.setPatient(subject);
	}
}