package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;

@ResourceDef(name = "Facility", profile = "https://nightingaleproject.github.io/fhir-death-record/guide/StructureDefinition-sdr-decedent-DispositionFacility-extension.html")
public class Facility {
	private static final long serialVersionUID = 1L;

	@Child(name = "FacilityNameExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-FacilityName-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR FacilityName Extension")
	private String facilityNameExtension;
	@Child(name = "PostalAddressExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/shr-core-PostalAddress-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR PostalAddress Extension")
	private PostalAddress postalAddressExtension;

	public String getFacilityNameExtension() {
		return facilityNameExtension;
	}

	public void setFacilityNameExtension(String facilityNameExtension) {
		this.facilityNameExtension = facilityNameExtension;
	}

	public PostalAddress getPostalAddressExtension() {
		return postalAddressExtension;
	}

	public void setPostalAddressExtension(PostalAddress postalAddressExtension) {
		this.postalAddressExtension = postalAddressExtension;
	}

}
