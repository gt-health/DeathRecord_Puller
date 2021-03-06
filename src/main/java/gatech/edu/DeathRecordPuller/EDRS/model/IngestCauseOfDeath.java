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
import java.time.ZonedDateTime;

/**
 * CauseOfDeath
 */
public class IngestCauseOfDeath {
	@SerializedName("code")
	private String code = null;

	@SerializedName("beginTime")
	private ZonedDateTime beginTime = null;

	@SerializedName("endTime")
	private ZonedDateTime endTime = null;

	public IngestCauseOfDeath code(String code) {
		this.code = code;
		return this;
	}

	/**
	 * Get code
	 * 
	 * @return code
	 **/

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public IngestCauseOfDeath beginTime(ZonedDateTime beginTime) {
		this.beginTime = beginTime;
		return this;
	}

	/**
	 * Get beginTime
	 * 
	 * @return beginTime
	 **/

	public ZonedDateTime getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(ZonedDateTime beginTime) {
		this.beginTime = beginTime;
	}

	public IngestCauseOfDeath endTime(ZonedDateTime endTime) {
		this.endTime = endTime;
		return this;
	}

	/**
	 * Get endTime
	 * 
	 * @return endTime
	 **/

	public ZonedDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(ZonedDateTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IngestCauseOfDeath causeOfDeath = (IngestCauseOfDeath) o;
		return Objects.equals(this.code, causeOfDeath.code) && Objects.equals(this.beginTime, causeOfDeath.beginTime)
				&& Objects.equals(this.endTime, causeOfDeath.endTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, beginTime, endTime);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CauseOfDeath {\n");

		sb.append("    code: ").append(toIndentedString(code)).append("\n");
		sb.append("    beginTime: ").append(toIndentedString(beginTime)).append("\n");
		sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
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
