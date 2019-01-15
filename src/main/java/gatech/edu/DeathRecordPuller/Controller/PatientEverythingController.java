package gatech.edu.DeathRecordPuller.Controller;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.hapi.ctx.FhirDstu3;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestOperationComponent;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IFhirVersion;
import ca.uhn.fhir.model.dstu2.FhirDstu2;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.Conformance.RestOperation;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IFetchConformanceUntyped;
import gatech.edu.DeathRecordPuller.Controller.config.PatientEverythingConfig;
import gatech.edu.common.FHIR.client.ClientService;

@CrossOrigin()
@RestController
public class PatientEverythingController {
	
	protected FhirContext ctx2;
	protected FhirContext ctx3;
	protected List<IGenericClient> fhirServers;
	protected IParser jsonParser2;
	protected IParser jsonParser3;
	protected ObjectMapper mapper;
	
	@Autowired
	public PatientEverythingController(PatientEverythingConfig config) {
		mapper = new ObjectMapper();
		fhirServers = new ArrayList<IGenericClient>();
		ctx2 = FhirContext.forDstu2();
		ctx2.getRestfulClientFactory().setSocketTimeout(60000);
		ctx2.getRestfulClientFactory().setConnectionRequestTimeout(36000);
		ctx2.getRestfulClientFactory().setConnectTimeout(36000);
		ctx2.getRestfulClientFactory().setPoolMaxTotal(10);
		ctx3 = FhirContext.forDstu3();
		ctx3.getRestfulClientFactory().setSocketTimeout(60000);
		ctx3.getRestfulClientFactory().setConnectionRequestTimeout(36000);
		ctx3.getRestfulClientFactory().setConnectTimeout(36000);
		ctx3.getRestfulClientFactory().setPoolMaxTotal(10);
		jsonParser2 = ctx2.newJsonParser();
		jsonParser3 = ctx3.newJsonParser();
		for(int i=0;i<config.getServerList().size();i++) {
			String serverName = config.getServerList().get(i);
			FhirContext ctx = ctx2;
			if(config.getFhirVersion().get(i).equals("stu3")) {
				ctx = ctx3;
			}
			fhirServers.add(ctx.getRestfulClientFactory().newGenericClient(serverName));
		}
	}
	
	@RequestMapping(value = "/Patient/{id}/$everything", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getPatientEverything(@PathVariable long id) throws JsonProcessingException{
		ca.uhn.fhir.model.dstu2.resource.Bundle returnBundleDstu2 = new ca.uhn.fhir.model.dstu2.resource.Bundle();
		Bundle returnBundleStu3 = new Bundle();
		for(IGenericClient endpoint: fhirServers) {
			IFhirVersion version = endpoint.getFhirContext().getVersion();
			if(version instanceof FhirDstu2) {
				if(serverSupportsOperationDstu2(endpoint,"everything")) {
					ca.uhn.fhir.model.dstu2.resource.Bundle newEntries = getEverythingDstu2(endpoint,id);
					for(Entry newEntry : newEntries.getEntry()) {
						returnBundleDstu2.addEntry(newEntry);
					}
				}
			}
			else if(version instanceof FhirDstu3) {
				if(serverSupportsOperationDstu3(endpoint,"everything")) {
					Bundle newEntries = getEverythingDstu3(endpoint,id);
					for(BundleEntryComponent newEntry : newEntries.getEntry()) {
						returnBundleStu3.addEntry(newEntry);
					}
				}
			}
		}
		ObjectNode finalMap = JsonNodeFactory.instance.objectNode();
		finalMap.put("dstu2", jsonParser2.encodeResourceToString(returnBundleDstu2));
		finalMap.put("stu3", jsonParser3.encodeResourceToString(returnBundleStu3));
		return new ResponseEntity<String>(mapper.writeValueAsString(finalMap), HttpStatus.OK);
	}
	
	public boolean serverSupportsOperationDstu2(IGenericClient client,String name) {
		Conformance conformance = client.capabilities().ofType(Conformance.class).execute();
		for(Conformance.Rest restComponent:conformance.getRest()) {
			for(RestOperation operation:restComponent.getOperation()) {
				if(operation.getName().equals("everything"))
					return true;
			}
		}
		return false;
	}
	
	public ca.uhn.fhir.model.dstu2.resource.Bundle getEverythingDstu2(IGenericClient client,long patientId) {
		ca.uhn.fhir.model.dstu2.resource.Parameters outParams = client.operation()
		.onInstance(new IdType("Patient",new Long(patientId)))
		.named("$everything")
		.withParameters(new ca.uhn.fhir.model.dstu2.resource.Parameters())
		.execute();
		
		return (ca.uhn.fhir.model.dstu2.resource.Bundle) outParams.getParameterFirstRep().getResource();
	}
	
	public boolean serverSupportsOperationDstu3(IGenericClient client,String name) {
		CapabilityStatement capabilities = client.capabilities().ofType(CapabilityStatement.class).execute();
		for(CapabilityStatementRestComponent restComponent:capabilities.getRest()) {
			if(restComponent.hasOperation()) {
				for(CapabilityStatementRestOperationComponent operComponent:restComponent.getOperation()) {
					if(operComponent.getName().equals(name))
						return true;
				}
			}
		}
		return false;
	}
	
	public Bundle getEverythingDstu3(IGenericClient client,long patientId) {
		Parameters outParams = client.operation()
		.onInstance(new IdType("Patient",new Long(patientId)))
		.named("$everything")
		.withParameters(new Parameters())
		.useHttpGet()
		.execute();
		
		return (Bundle) outParams.getParameterFirstRep().getResource();
	}
}
