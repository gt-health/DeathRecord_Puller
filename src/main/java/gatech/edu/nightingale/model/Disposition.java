package gatech.edu.nightingale.model;

import org.hl7.fhir.dstu3.model.CodeableConcept;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;

@ResourceDef(name = "Disposition", profile = "https://nightingaleproject.github.io/fhir-death-record/guide/StructureDefinition-sdr-decedent-Disposition-extension.html")
public class Disposition {
	private static final long serialVersionUID = 1L;

	@Child(name = "DispositionTypeExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-DispositionType-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR DispositionType Extension")
	private CodeableConcept dispositionTypeExtension;
	@Child(name = "FacilityNameExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-DispositionFacility-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR DispositionFacility Extension")
	private Facility dispositionFacilityExtension;
	@Child(name = "FuneralFacilityExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-FuneralFacility-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR FuneralFacility Extension")
	private Facility funeralFacilityExtension;

	public CodeableConcept getDispositionTypeExtension() {
		return dispositionTypeExtension;
	}

	public void setDispositionTypeExtension(CodeableConcept dispositionTypeExtension) {
		this.dispositionTypeExtension = dispositionTypeExtension;
	}

	public Facility getDispositionFacilityExtension() {
		return dispositionFacilityExtension;
	}

	public void setDispositionFacilityExtension(Facility dispositionFacilityExtension) {
		this.dispositionFacilityExtension = dispositionFacilityExtension;
	}

	public Facility getFuneralFacilityExtension() {
		return funeralFacilityExtension;
	}

	public void setFuneralFacilityExtension(Facility funeralFacilityExtension) {
		this.funeralFacilityExtension = funeralFacilityExtension;
	}
}
