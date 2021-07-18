<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/sgsreports/jquery.js" />
<!-- <script type="text/javascript">
	var $j = jQuery.noConflict(); 
</script> -->
<script type="text/javascript">
function msgreg(){
document.getElementById('msg').innerHTML="<div id='openmrs_msg'>Registering...</div>";
exit();
}
function msgrem(){
	document.getElementById('msg').innerHTML="<div id='openmrs_msg'>Removing...</div>";
	exit();
	}
</script>
<style>
table.reports {
	border-collapse: collapse;
	border: 1px solid blue;
	width: 100%;
}

.reports td {
	border-collapse: collapse;
	border: 1px solid blue;
}

.reports .tableheaders {
	font-weight: bold;
	background-color: #B0C4DE;
}

.reports .tabletd {
	font-weight: bold;
	background-color: #EEE;
}

.reports .alt {
	background-color: #B0C4DE;
}

.reports .altodd {
	background-color: #EEE;
}

.reports .hover {
	background-color: #DED;
}

.reports .althover {
	background-color: #EFE;
}
</style>
<script type="text/javascript">
$(document).ready(function(){
	$('tr:even').addClass('alt');
	$('tr:even').hover(
			function(){$(this).addClass('hover')},
			function(){$(this).removeClass('hover')}
	);	
	$('tr:odd').addClass('altodd');
	$('tr:odd').hover(
			function(){$(this).addClass('althover')},
			function(){$(this).removeClass('althover')}
	);
});
</script>
<div id="msg"></div>
<h2>Register/Remove Sustainable Global Surgery Reports</h2>

<br />
<br />

<table class="reports" style="width: 100%;">
	<tr class="tableheaders">
		<td>Categories</td>
		<td>Report Name</td>
		<td>Run</td>
		<td colspan="2"><center>Action</center></td>
	</tr>
	<tr>
		<td rowspan="12" class="tabletd">Sustainable Global Surgery Reports</td>
		<td>OR Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_OR_Report.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_OR_Report.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>Surgical Procedure Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_surgical_procedure_report.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_surgical_procedure_report.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>Diagnosis Search Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_diagnosis_search_report.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_diagnosis_search_report.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>M and M Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_m_and_m_Report.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_m_and_m_Report.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
		<tr>
		<td>Form Print Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_formPrintReport.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_formPrintReport.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>Activity Progress Monitoring Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_Activity_Progress_Monitoring.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_Activity_Progress_Monitoring.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>Registration Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_registration_report.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_registration_report.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>Ward Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_Ward_Report.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_Ward_Report.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>Weekly Report - Private Patients</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_WeeklyReportPrivatePatients.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_WeeklyReportPrivatePatients.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>Weekly Report - General Patients</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_WeeklyReportGeneralPatients.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_WeeklyReportGeneralPatients.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>Hospital Monthly Indicators Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_hospitalMonthlyIndicatorsReport.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_hospitalMonthlyIndicatorsReport.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>
	<tr>
		<td>OPD Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_OPD_Report.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_OPD_Report.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>

	<tr>
		<td rowspan="1" class="tabletd">General Reports</td>
		<td>ED Report</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_ED_Report.form"
			onclick=msgreg(this)>(Re) register</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_ED_Report.form"
			onclick=msgrem(this)>Remove</a></td>
	</tr>


	<tr class="tableheaders">
		<td colspan="2">All Reports</td>
		<td>Central</td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/register_allReports.form"
			onclick=msgreg(this)>Register All</a></td>
		<td><a
			href="${pageContext.request.contextPath}/module/sgsreports/remove_allReports.form"
			onclick=msgrem(this)>Remove All</a></td>
	</tr>



</table>
<%@ include file="/WEB-INF/template/footer.jsp"%>