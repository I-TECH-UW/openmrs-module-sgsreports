package org.openmrs.module.sgsreports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.sgsreports.dataset.definition.EncounterAndObsDataSetDefinition22;
import org.openmrs.module.sgsreports.reporting.library.BasePatientDataLibrary;
import org.openmrs.module.sgsreports.reporting.library.BuiltInEncounterDataLibrary2;
import org.openmrs.module.sgsreports.util.MetadataLookup;

public class SetupDiagnosisSearchReport {
	
	private Concept codedDiagnosis;
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	BuiltInEncounterDataLibrary2 encounterData2 = new BuiltInEncounterDataLibrary2();
	
	protected final static Log log = LogFactory.getLog(SetupDiagnosisSearchReport.class);
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "diagnosisSearchReport.xls", "diagnosisSearchReport.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:10,dataset:dataSet");
		props.put("sortWeight", "5000");
		design.setProperties(props);
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("diagnosisSearchReport.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Diagnosis Search Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Diagnosis Search Report");
		reportDefinition.addParameter(new Parameter("startDate", "From Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "To Date", Date.class));
		
		Parameter diagnosisName = new Parameter("diagnosis", "Diagnosis Name", Concept.class);
		diagnosisName.setRequired(false);
		reportDefinition.addParameter(diagnosisName);
		
		Parameter doctor = new Parameter("doctor", "Doctor", User.class);
		doctor.setRequired(false);
		reportDefinition.addParameter(doctor);
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		EncounterAndObsDataSetDefinition22 dsd = new EncounterAndObsDataSetDefinition22();
		dsd.setName("dataSet");
		dsd.setParameters(getParameters());
		
		SqlEncounterQuery defaultFilter = new SqlEncounterQuery();
		defaultFilter.setParameters(getQueryParameters());
		defaultFilter.setQuery("select encounter_id from obs where concept_id = " + codedDiagnosis.getConceptId() + " and obs_datetime >=:onOrAfter and obs_datetime <=:onOrBefore and voided=0");
		
		MappedParametersEncounterQuery defaultFilterMapp = new MappedParametersEncounterQuery(defaultFilter, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate,diagnosis=diagnosis,doctor=doctor"));
		defaultFilterMapp.setName("defaultFilter");
		dsd.addRowFilter(Mapped.mapStraightThrough(defaultFilterMapp));
		
		SqlEncounterQuery noDiagnosiFilter = new SqlEncounterQuery();
		noDiagnosiFilter.setParameters(getQueryParameters());
		noDiagnosiFilter.setQuery("select encounter_id from obs where concept_id = " + codedDiagnosis.getConceptId() + " and creator=:doctor and obs_datetime >=:onOrAfter and obs_datetime <=:onOrBefore and voided=0");
		
		MappedParametersEncounterQuery noDiagnosisFilterMapp = new MappedParametersEncounterQuery(noDiagnosiFilter, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate,diagnosis=diagnosis,doctor=doctor"));
		noDiagnosisFilterMapp.setName("noDiagnosisFilter");
		dsd.addRowFilter(Mapped.mapStraightThrough(noDiagnosisFilterMapp));
		
		SqlEncounterQuery noDoctoriFilter = new SqlEncounterQuery();
		noDoctoriFilter.setParameters(getQueryParameters());
		noDoctoriFilter.setQuery("select encounter_id from obs where concept_id = " + codedDiagnosis.getConceptId() + " and value_coded=:diagnosis and obs_datetime >=:onOrAfter and obs_datetime <=:onOrBefore and voided=0");
		
		MappedParametersEncounterQuery noDoctorFilterMapp = new MappedParametersEncounterQuery(noDoctoriFilter, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate,diagnosis=diagnosis,doctor=doctor"));
		noDoctorFilterMapp.setName("noDoctorFilter");
		dsd.addRowFilter(Mapped.mapStraightThrough(noDoctorFilterMapp));
		
		SqlEncounterQuery withAllParamsFilter = new SqlEncounterQuery();
		withAllParamsFilter.setParameters(getQueryParameters());
		withAllParamsFilter.setQuery("select encounter_id from obs where concept_id = " + codedDiagnosis.getConceptId() + " and value_coded=:diagnosis and creator=:doctor and obs_datetime >=:onOrAfter and obs_datetime <=:onOrBefore and voided=0");
		
		MappedParametersEncounterQuery withAllParamsFilterMapp = new MappedParametersEncounterQuery(withAllParamsFilter, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate,diagnosis=diagnosis,doctor=doctor"));
		withAllParamsFilterMapp.setName("withAllParamsFilter");
		dsd.addRowFilter(Mapped.mapStraightThrough(withAllParamsFilterMapp));
		
		PatientIdentifierDataDefinition identifier = new PatientIdentifierDataDefinition();
		identifier.addType(Context.getPatientService().getPatientIdentifierTypeByUuid("8d79403a-c2cc-11de-8d13-0010c6dffd0f"));
		dsd.addColumn("ET", identifier, (String) null);
		
		dsd.addColumn("familyName", basePatientData.getPreferredFamilyName(), "");
		dsd.addColumn("givenName", basePatientData.getPreferredGivenName(), "");
		dsd.addColumn("DIAGNOSIS_DATE", encounterData.getEncounterDatetime(), "");
		dsd.addColumn("DOCTOR", encounterData2.getCreatorName(), "");
		
		reportDefinition.addDataSetDefinition("dataSet", Mapped.mapStraightThrough(dsd));
		
	}
	
	private void setupProperties() {
		codedDiagnosis = MetadataLookup.getConcept("81c7149b-3f10-11e4-adec-0800271c1b75");
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("startDate", "From Date", Date.class));
		l.add(new Parameter("endDate", "To Date", Date.class));
		
		Parameter diagnosis = new Parameter("diagnosis", "Diagnosis Name", Concept.class);
		diagnosis.setRequired(false);
		l.add(diagnosis);
		
		Parameter doctor = new Parameter("doctor", "Doctor", User.class);
		doctor.setRequired(false);
		l.add(doctor);
		return l;
	}
	
	public List<Parameter> getQueryParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("onOrAfter", "On Or After", Date.class));
		l.add(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		Parameter diagnosis = new Parameter("diagnosis", "Diagnosis Name", Concept.class);
		diagnosis.setRequired(false);
		l.add(diagnosis);
		
		Parameter doctor = new Parameter("doctor", "Doctor", User.class);
		doctor.setRequired(false);
		l.add(doctor);
		return l;
	}
	
}
