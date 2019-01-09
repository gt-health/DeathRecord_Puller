/*
 * EDRS Submission API
 * This is the swagger documentation for DeathDataHub's EDRS Submission API. This API's purpose is to allow Medical Examiners and coroners to submit a signed IngestDeathRecord to a remote Electronic Death Record System. Although this is part of the deathdatahub main platform, the parameters are complex enough to warrent it's own swagger doc file
 *
 * OpenAPI spec version: 1.0.0
 * Contact: Michael.Riley@gtri.gatech.edu
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package gatech.edu.DeathRecordPuller.EDRS.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * Information directly about the person certifying the deathrefer to table
 * http://hl7.org/fhir/STU3/v2/0360/2.7/index.html for a full list of
 * qualifications
 */
public class IngestCertifier {
	@SerializedName("name")
	private String name = null;

	/**
	 * Gets or Sets certifierType
	 */
	@JsonAdapter(CertifierTypeEnum.Adapter.class)
	public enum CertifierTypeEnum {
		CERTIFIER("Certifier"),

		PRONOUNCER_AND_CERTIFIER("Pronouncer and Certifier"),

		CORONER("Coroner"),

		MEDICAL_EXAMINER("Medical Examiner"),

		OTHER("Other");

		private String value;

		CertifierTypeEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static CertifierTypeEnum fromValue(String text) {
			for (CertifierTypeEnum b : CertifierTypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}

		public static class Adapter extends TypeAdapter<CertifierTypeEnum> {
			@Override
			public void write(final JsonWriter jsonWriter, final CertifierTypeEnum enumeration) throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public CertifierTypeEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return CertifierTypeEnum.fromValue(String.valueOf(value));
			}
		}
	}

	@SerializedName("certifierType")
	private CertifierTypeEnum certifierType = null;

	/**
	 * Gets or Sets qualification
	 */
	@JsonAdapter(QualificationEnum.Adapter.class)
	public enum QualificationEnum {
		MD("MD"),

		MDA("MDA"),

		MS("MS");

		private String value;

		QualificationEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static QualificationEnum fromValue(String text) {
			for (QualificationEnum b : QualificationEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}

		public static class Adapter extends TypeAdapter<QualificationEnum> {
			@Override
			public void write(final JsonWriter jsonWriter, final QualificationEnum enumeration) throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public QualificationEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return QualificationEnum.fromValue(String.valueOf(value));
			}
		}
	}

	@SerializedName("qualification")
	private QualificationEnum qualification = null;

	@SerializedName("address")
	private IngestAddress address = null;

	public IngestCertifier name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Get name
	 * 
	 * @return name
	 **/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IngestCertifier certifierType(CertifierTypeEnum certifierType) {
		this.certifierType = certifierType;
		return this;
	}

	/**
	 * Get certifierType
	 * 
	 * @return certifierType
	 **/

	public CertifierTypeEnum getCertifierType() {
		return certifierType;
	}

	public void setCertifierType(CertifierTypeEnum certifierType) {
		this.certifierType = certifierType;
	}

	public IngestCertifier qualification(QualificationEnum qualification) {
		this.qualification = qualification;
		return this;
	}

	/**
	 * Get qualification
	 * 
	 * @return qualification
	 **/

	public QualificationEnum getQualification() {
		return qualification;
	}

	public void setQualification(QualificationEnum qualification) {
		this.qualification = qualification;
	}

	public IngestCertifier address(IngestAddress address) {
		this.address = address;
		return this;
	}

	/**
	 * Get address
	 * 
	 * @return address
	 **/

	public IngestAddress getAddress() {
		return address;
	}

	public void setAddress(IngestAddress address) {
		this.address = address;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IngestCertifier certifier = (IngestCertifier) o;
		return Objects.equals(this.name, certifier.name) && Objects.equals(this.certifierType, certifier.certifierType)
				&& Objects.equals(this.qualification, certifier.qualification)
				&& Objects.equals(this.address, certifier.address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, certifierType, qualification, address);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Certifier {\n");

		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    certifierType: ").append(toIndentedString(certifierType)).append("\n");
		sb.append("    qualification: ").append(toIndentedString(qualification)).append("\n");
		sb.append("    address: ").append(toIndentedString(address)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
