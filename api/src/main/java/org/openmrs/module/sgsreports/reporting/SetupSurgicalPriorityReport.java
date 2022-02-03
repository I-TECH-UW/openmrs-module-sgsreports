package org.openmrs.module.sgsreports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
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
import org.openmrs.module.sgsreports.dataset.definition.SurgicalPriorityDataSetDefinition;
import org.openmrs.module.sgsreports.reporting.library.BasePatientDataLibrary;
import org.openmrs.module.sgsreports.util.MetadataLookup;

public class SetupSurgicalPriorityReport {
	
	private Concept clinicVisitSurgeoForm;
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	protected final static Log log = LogFactory.getLog(SetupSurgicalPriorityReport.class);
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "surgicalPriorityReport.xls", "surgicalPriorityReport.xls_", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset: surgicalPriorityReportDataSet");
		props.put("sortWeight", "5000");
		design.setProperties(props);
		Helper.saveReportDesign(design);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("surgicalPriorityReport.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Surgical Priority Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Surgical Priority Report");
		reportDefinition.setParameters(getParameters());
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		SurgicalPriorityDataSetDefinition dsd = new SurgicalPriorityDataSetDefinition();
		dsd.setName("surgicalPriorityReportDataSet");
		dsd.setParameters(getParameters());
		
		SqlEncounterQuery patientsWithClinicVisitSurgeonFrom = new SqlEncounterQuery();
		patientsWithClinicVisitSurgeonFrom.setParameters(getQueryParameters());
		patientsWithClinicVisitSurgeonFrom.setQuery("select encounter_id from obs where concept_id = " + clinicVisitSurgeoForm.getConceptId() + " and obs_datetime >=:onOrAfter and obs_datetime <=:onOrBefore and voided=0");
		
		MappedParametersEncounterQuery patientsWithClinicVisitSurgeonFromFilterMapp = new MappedParametersEncounterQuery(patientsWithClinicVisitSurgeonFrom, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
		patientsWithClinicVisitSurgeonFromFilterMapp.setName("noDoctorFilter");
		dsd.addRowFilter(Mapped.mapStraightThrough(patientsWithClinicVisitSurgeonFromFilterMapp));
		
		PatientIdentifierDataDefinition i = new PatientIdentifierDataDefinition();
		i.addType(Context.getPatientService().getPatientIdentifierTypeByUuid("81433852-3f10-11e4-adec-0800271c1b75"));
		dsd.addColumn("ID", i, (String) null);
		
		dsd.addColumn("familyName", basePatientData.getPreferredFamilyName(), "");
		dsd.addColumn("middleName", basePatientData.getPreferredMiddleName(), "");
		dsd.addColumn("givenName", basePatientData.getPreferredGivenName(), "");
		
		reportDefinition.addDataSetDefinition("surgicalPriorityReportDataSet", Mapped.mapStraightThrough(dsd));
		
	}
	
	private void setupProperties() {
		clinicVisitSurgeoForm = MetadataLookup.getConcept("8e5cada3-2b8d-39e4-711e-bd02f69f3e33");
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("startDate", "From Date", Date.class));
		l.add(new Parameter("endDate", "To Date", Date.class));
		return l;
	}
	
	public List<Parameter> getQueryParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("onOrAfter", "On Or After", Date.class));
		l.add(new Parameter("onOrBefore", "On Or Before", Date.class));
		return l;
	}
	
}
