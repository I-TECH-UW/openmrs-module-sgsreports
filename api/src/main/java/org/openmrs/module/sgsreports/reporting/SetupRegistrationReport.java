package org.openmrs.module.sgsreports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.sgsreports.reporting.library.BasePatientDataLibrary;
import org.openmrs.module.sgsreports.util.MetadataLookup;

public class SetupRegistrationReport {
	
	private BuiltInPatientDataLibrary builtInPatientData = new BuiltInPatientDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	private PersonAttributeType firstNextOfKinPhone;
	
	private PersonAttributeType secondNextOfKinPhone;
	
	private PersonAttributeType patientPhoneNumber;
	
	private PersonAttributeType clinicLocation;
	
	private List<VisitType> OPDvisitTypes = new ArrayList<VisitType>();
	
	private VisitType IPDvisitType = null;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "RegistrationReport.xls", "RegistrationReport.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:dataSet");
		props.put("sortWeight", "5000");
		design.setProperties(props);
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("OPD RegistrationReport.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("OPD Registration Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("OPD Registration Report");
		
		reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.setName("dataSet");
		dataSetDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		VisitCohortDefinition rowFilter = new VisitCohortDefinition();
		rowFilter.setVisitTypeList(OPDvisitTypes);
		rowFilter.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		rowFilter.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		
		dataSetDefinition.addRowFilter(rowFilter, "createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}");
		
		PatientIdentifierDataDefinition i = new PatientIdentifierDataDefinition();
		i.addType(MetadataLookup.getPatientIdentifierType("81433852-3f10-11e4-adec-0800271c1b75"));
		dataSetDefinition.addColumn("PATIENT_ID", i, (String) null);
		
		PreferredNameDataDefinition d = new PreferredNameDataDefinition();
		dataSetDefinition.addColumn("givenName", d, new HashMap<String, Object>(), new PropertyConverter(PersonName.class, "givenName"));
		dataSetDefinition.addColumn("middleName", d, new HashMap<String, Object>(), new PropertyConverter(PersonName.class, "middleName"));
		dataSetDefinition.addColumn("familyName", d, new HashMap<String, Object>(), new PropertyConverter(PersonName.class, "familyName"));
		dataSetDefinition.addColumn("Age", basePatientData.getAgeAtEndInYears(), new HashMap<String, Object>());
		dataSetDefinition.addColumn("M/F", builtInPatientData.getGender(), new HashMap<String, Object>());
		dataSetDefinition.addColumn("firstNextOfKinPhone", basePatientData.getPersonAttribute(firstNextOfKinPhone), new HashMap<String, Object>());
		dataSetDefinition.addColumn("secondNextOfKinPhone", basePatientData.getPersonAttribute(secondNextOfKinPhone), new HashMap<String, Object>());
		dataSetDefinition.addColumn("patientPhoneNumber", basePatientData.getPersonAttribute(patientPhoneNumber), new HashMap<String, Object>());
		dataSetDefinition.addColumn("clinicLocation", basePatientData.getPersonAttribute(clinicLocation), new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("startDate", "${startDate}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setupProperties() {
		IPDvisitType = MetadataLookup.getVisitType("4cf675c6-9c30-4fa6-a827-9003e326a359");
		OPDvisitTypes = Context.getVisitService().getAllVisitTypes();
		OPDvisitTypes.remove(IPDvisitType);
		firstNextOfKinPhone = MetadataLookup.getPersonAttributeType("93016867-67fd-4b2b-ab02-9adb697525dc");
		secondNextOfKinPhone = MetadataLookup.getPersonAttributeType("bda0e3bc-d390-4154-be02-c74de41c0e38");
		patientPhoneNumber = MetadataLookup.getPersonAttributeType("d16670f5-0862-4ef8-a249-7842ad3e151f");
		clinicLocation = MetadataLookup.getPersonAttributeType("d109216c-bbc3-4856-a740-86c7e6f88564");
	}
}
