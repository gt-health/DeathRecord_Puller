package gatech.edu.nightingale.model;

import java.util.List;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Composition;

@ResourceDef(name = "DeathRecordContents", profile = "https://nightingaleproject.github.io/fhir-death-record/guide/StructureDefinition-sdr-deathRecord-DeathRecordContents.html")
public class DeathRecordContents extends Composition {
	private static final long serialVersionUID = 1L;

	DeathRecordContents() {
		super();
		this.setType(new CodeableConceptDt("http://loinc.org", "64297-5"));
		this.addSection();
	}

	public Section addSection() {
		Section section = new Section();
		section.setCode(new CodeableConceptDt("http://loinc.org", "64297-5"));
		return section;
	}

	public void addEntry(ResourceReferenceDt reference) {
		List<ResourceReferenceDt> entries = this.getSection().get(0).getEntry();
		entries.add(reference);
	}
}
