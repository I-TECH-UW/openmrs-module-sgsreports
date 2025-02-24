package org.openmrs.module.sgsreports.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.sgsreports.reports.SetupDiagnosisSearchReport;
import org.openmrs.module.sgsreports.reports.SetupFormPrintReport;
import org.openmrs.module.sgsreports.reports.SetupHospitalMonthlyIndicatorsReport;
import org.openmrs.module.sgsreports.reports.SetupMAndMReport;
import org.openmrs.module.sgsreports.reports.SetupRegistrationReport;
import org.openmrs.module.sgsreports.reports.SetupSurgicalPriorityReport;
import org.openmrs.module.sgsreports.reports.SetupSurgicalProcedureReport;
import org.openmrs.module.sgsreports.reports.SetupWeeklyReportGeneralPatients;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SGSSetupReportsFormController {
	
	public Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/sgsreports/sgsreports", method = RequestMethod.GET)
	public void manage() {
	}
	
	@RequestMapping("/module/sgsreports/register_surgical_priority_report")
	public ModelAndView registerSurgicalPriorityReport() throws Exception {
		new SetupSurgicalPriorityReport().setup();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_surgical_priority_report")
	public ModelAndView removeSurgicalPriorityReport() throws Exception {
		new SetupSurgicalPriorityReport().delete();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_surgical_procedure_report")
	public ModelAndView registerAccomplishmentsReport() throws Exception {
		new SetupSurgicalProcedureReport().setup();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_surgical_procedure_report")
	public ModelAndView removeAccomplishmentsReport() throws Exception {
		new SetupSurgicalProcedureReport().delete();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_OR_Report")
	public ModelAndView registerORReport() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_OR_Report")
	public ModelAndView removeORReport() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_m_and_m_Report")
	public ModelAndView registerMAndMReport() throws Exception {
		new SetupMAndMReport().setup();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_m_and_m_Report")
	public ModelAndView removeMAndMReport() throws Exception {
		new SetupMAndMReport().delete();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_Activity_Progress_Monitoring")
	public ModelAndView registerActivityProgressMonitoring() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_Activity_Progress_Monitoring")
	public ModelAndView removeActivityProgressMonitoring() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_registration_report")
	public ModelAndView registerRegistrationReport() throws Exception {
		new SetupRegistrationReport().setup();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove__registration_report")
	public ModelAndView removeRegistrationReport() throws Exception {
		new SetupRegistrationReport().delete();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_Ward_Report")
	public ModelAndView registerWardReport() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_Ward_Report")
	public ModelAndView removeWardReport() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_WeeklyReportGeneralPatients")
	public ModelAndView registerWeeklyReportGeneralPatientsReport() throws Exception {
		new SetupWeeklyReportGeneralPatients().setup();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_WeeklyReportGeneralPatients")
	public ModelAndView removeWeeklyReportGeneralPatientsReport() throws Exception {
		new SetupWeeklyReportGeneralPatients().delete();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_hospitalMonthlyIndicatorsReport")
	public ModelAndView registerHospitalMonthlyIndicatorsReport() throws Exception {
		new SetupHospitalMonthlyIndicatorsReport().setup();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_hospitalMonthlyIndicatorsReport")
	public ModelAndView removeHospitalMonthlyIndicatorsReport() throws Exception {
		new SetupHospitalMonthlyIndicatorsReport().delete();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_diagnosis_search_report")
	public ModelAndView registerDiagnosisSearchReport() throws Exception {
		new SetupDiagnosisSearchReport().setup();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_diagnosis_search_report")
	public ModelAndView removeDiagnosisSearchReport() throws Exception {
		new SetupDiagnosisSearchReport().delete();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_OPD_Report")
	public ModelAndView registerOPDReport() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_OPD_Report")
	public ModelAndView removeOPDReport() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_ED_Report")
	public ModelAndView registerEDReport() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_ED_Report")
	public ModelAndView removeEDReport() throws Exception {
		
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/register_formPrintReport")
	public ModelAndView registerFormPrintReport() throws Exception {
		new SetupFormPrintReport().setup();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_formPrintReport")
	public ModelAndView removeFormPrintReport() throws Exception {
		new SetupFormPrintReport().delete();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
}
