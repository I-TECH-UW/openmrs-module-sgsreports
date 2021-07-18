package org.openmrs.module.sgsreports.web.controller;

import org.openmrs.module.sgsreports.util.CleanReportingTablesAndRegisterAllReports;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SGSRegisterRemoveAllReportsFormController {
	
	@RequestMapping("/module/sgsreports/register_allReports")
	public ModelAndView registerAllReports() throws Exception {
		CleanReportingTablesAndRegisterAllReports.registerReports();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
	@RequestMapping("/module/sgsreports/remove_allReports")
	public ModelAndView removeAllReports() throws Exception {
		CleanReportingTablesAndRegisterAllReports.cleanTables();
		return new ModelAndView(new RedirectView("sgsreports.form"));
	}
	
}
