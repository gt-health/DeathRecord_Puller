package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.valueset.ConditionClinicalStatusCodesEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;

@ResourceDef(name = "ContributedToDeathCondition", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-ContributedToDeathCondition")
public class ContributedToDeathCondition extends Condition {
	private static final long serialVersionUID = 1L;

	public ContributedToDeathCondition() {
		super();
		this.setClinicalStatus(ConditionClinicalStatusCodesEnum.ACTIVE);
	}

	public ContributedToDeathCondition(ResourceReferenceDt subject, DateTimeDt onset) {
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