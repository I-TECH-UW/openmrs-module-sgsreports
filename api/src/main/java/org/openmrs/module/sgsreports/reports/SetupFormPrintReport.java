package org.openmrs.module.sgsreports.reports;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.sgsreports.dataset.definition.FormPrintDataSetDefinition;
import org.openmrs.module.sgsreports.reports.library.BasePatientDataLibrary;
import org.openmrs.module.sgsreports.util.GlobalPropertiesManagement;
import org.openmrs.module.sgsreports.util.MetadataLookup;

public class SetupFormPrintReport {
	
	protected final static Log log = LogFactory.getLog(SetupFormPrintReport.class);
	
	private Concept orthopaedicFollowup;
	
	private Concept orthopaedicOperativeReport;
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	List<Concept> obsWeWant = null;
	
	List<ReportDesign> reportDesigns = null;
	
	public void setup() throws Exception {
		
		reportDesigns = new ArrayList<ReportDesign>();
		Path path = Paths.get("/opt/bahmni-web/etc/bahmniapps/clinical/variables.js");
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		reportDesigns.add(Helper.createRowPerPatientXlsOverviewReportDesign(rd, "orthopaedicFollowup.xls", "orthopaedicFollowup.xls_", null));
		reportDesigns.add(Helper.createRowPerPatientXlsOverviewReportDesign(rd, "orthopaedicOperativeReport.xls", "orthopaedicOperativeReport.xls_", null));
		
		StringBuilder builder = new StringBuilder();
		builder.append("var formAndReporDesignMap = { \"");
		String prefix = "";
		for (ReportDesign reportDesign : reportDesigns) {
			ReportDesign savedReportDesign = Helper.saveReportDesign(reportDesign);
			String FormConceptUuid = getFormConcept(savedReportDesign).getUuid();
			String designUui = savedReportDesign.getUuid();
			
			builder.append(prefix);
			prefix = "\",\"";
			builder.append(FormConceptUuid + "\":\"" + designUui);
		}
		builder.append("\"};");
		builder.append("\n");
		builder.append("var reportDefinitionUuid = \"" + rd.getUuid() + "\";");
		builder.append("\n");
		builder.append("var serverIpAddress = \"" + Context.getAdministrationService().getGlobalProperty("cchereports.serverIpAddress") + "\";");
		Files.write(path, builder.toString().getBytes());
		
	}
	
	public void delete() {
		List<String> designs = Arrays.asList("orthopaedicFollowup.xls_", "orthopaedicOperativeReport.xls_");
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if (designs.contains(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Form Print Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Form Print Report");
		reportDefinition.addParameter(new Parameter("encounterUUID", "Encounter UUID", String.class));
		reportDefinition.addParameter(new Parameter("parentObsUuid", "Parent Obs UUID", String.class));
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		FormPrintDataSetDefinition dsd = new FormPrintDataSetDefinition();
		dsd.setName("dataSet");
		dsd.setParameters(getParameters());
		
		SqlEncounterQuery rowFilter = new SqlEncounterQuery();
		Parameter encounterUUID = new Parameter("encounterUUID", "Encounter UUID", String.class);
		encounterUUID.setRequired(true);
		
		rowFilter.addParameter(encounterUUID);
		
		rowFilter.setQuery("select encounter_id from encounter where uuid=:encounterUUID");
		
		MappedParametersEncounterQuery q = new MappedParametersEncounterQuery(rowFilter, ObjectUtil.toMap("encounterUUID=encounterUUID"));
		dsd.addRowFilter(Mapped.mapStraightThrough(q));
		
		dsd.addColumn("enocunterDateTime", encounterData.getEncounterDatetime(), "");
		PatientIdentifierDataDefinition i = new PatientIdentifierDataDefinition();
		i.addType(Context.getPatientService().getPatientIdentifierType(2));
		dsd.addColumn("patientId", i, (String) null);
		
		PreferredNameDataDefinition d = new PreferredNameDataDefinition();
		
		dsd.addColumn("familyName", d, "", new PropertyConverter(PersonName.class, "familyName"));
		dsd.addColumn("middleName", d, "", new PropertyConverter(PersonName.class, "middleName"));
		dsd.addColumn("givenName", d, "", new PropertyConverter(PersonName.class, "givenName"));
		
		dsd.addColumn("age", basePatientData.getAgeAtEndInYears(), "");
		
		dsd.addColumn("birthDate", basePatientData.getBirthdate(), "");
		
		reportDefinition.addDataSetDefinition("dataSet", Mapped.mapStraightThrough(dsd));
		
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> reportParameters = new ArrayList<Parameter>();
		reportParameters.add(new Parameter("encounterUUID", "Encounter UUID", String.class));
		reportParameters.add(new Parameter("parentObsUuid", "Parent Obs UUID", String.class));
		return reportParameters;
	}
	
	private void setupProperties() {
		orthopaedicFollowup = MetadataLookup.getConcept("c5d9523e-4b31-4cb5-8869-548cc42f86f0");
		orthopaedicOperativeReport = MetadataLookup.getConcept("c4aff83c-b8d6-4172-8dc5-4d611c5c51d7");
	}
	
	private Concept getFormConcept(ReportDesign reportDesign) {
		if (reportDesign.getName().equalsIgnoreCase("orthopaedicFollowup.xls_")) {
			return orthopaedicFollowup;
		}
		if (reportDesign.getName().equalsIgnoreCase("orthopaedicOperativeReport.xls_")) {
			return orthopaedicOperativeReport;
		}
		return null;
	}
	
}
