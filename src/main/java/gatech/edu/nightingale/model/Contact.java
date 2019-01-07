package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.primitive.StringDt;

@DatatypeDef(name = "Contact")
public class Contact {

	@Child(name = "relationship")
	private CodingDt relationship;
	@Child(name = "name")
	private StringDt name;
	@Child(name = "telecom")
	private ContactPointDt telecom;
	@Child(name = "addresss")
	private PostalAddress address;

	public CodingDt getRelationship() {
		return relationship;
	}

	public void setRelationship(CodingDt relationship) {
		this.relationship = relationship;
	}

	public StringDt getName() {
		return name;
	}

	public void setName(StringDt name) {
		this.name = name;
	}

	public ContactPointDt getTelecom() {
		return telecom;
	}

	public void setTelecom(ContactPointDt telecom) {
		this.telecom = telecom;
	}

	public PostalAddress getAddress() {
		return address;
	}

	public void setAddress(PostalAddress address) {
		this.address = address;
	}

}
