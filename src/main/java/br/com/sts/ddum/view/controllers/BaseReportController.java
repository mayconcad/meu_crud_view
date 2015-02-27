package br.com.sts.ddum.view.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import ar.com.fdvs.dj.domain.DynamicReport;
import br.com.sts.ddum.model.enums.ReportType;

public class BaseReportController extends BaseController {

	private static final long serialVersionUID = -5980436720345078022L;

	@SuppressWarnings("rawtypes")
	protected Map parameters = new HashMap();
	protected DynamicReport dynamicReport = new DynamicReport();
	protected ReportType reportType = ReportType.PDF;
	protected String reportPath = "";
	protected String logoPath = "";
	protected HttpSession session = null;

	public BaseReportController() {
		FacesContext context = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) context
				.getExternalContext().getContext();
		setReportPath(servletContext.getRealPath("/reports"));
		setLogoPath(servletContext.getRealPath("/resources/images"));
		session = (HttpSession) context.getExternalContext().getSession(false);
		session.setAttribute("logo", "");
		session.setAttribute("reportType", getReportType());
		session.setAttribute("reportPath", getReportPath());
		session.setAttribute("logoPath", getLogoPath());
	}

	@PostConstruct
	public void construct() {
	}

	protected void redirect(String page) {

		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext ec = ctx.getExternalContext();

		try {
			ec.redirect(ec.getRequestContextPath() + page);
		} catch (IOException ex) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
							.getMessage(), ex.getMessage()));
		}
	}

	public ReportType getReportType() {
		return reportType;
	}

	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	public String getReportPath() {
		return reportPath;
	}

	public void setReportPath(String reportPath) {
		this.reportPath = reportPath;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}
}
