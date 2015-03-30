package br.com.sts.ddum.view.controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import ar.com.fdvs.dj.domain.DynamicReport;
import br.com.sts.ddum.model.enums.ReportType;
import br.com.sts.ddum.model.utils.DataValidatorUtils;

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
		if (session != null) {
			session.setAttribute("logo", "");
			session.setAttribute("reportType", getReportType());
			session.setAttribute("reportPath", getReportPath());
			session.setAttribute("logoPath", getLogoPath());
		}
	}

	@PostConstruct
	public void construct() {
	}

	public StreamedContent generateStremedReport(String reportFilePath,
			String reportFileName, List list, Map<String, Object> params) {

		JasperReport jasperReport = null;
		JRExporter exporter = new JRPdfExporter();

		String localFile = "";
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		DefaultStreamedContent defaultStreamedContent = null;

		try {
			jasperReport = (JasperReport) JRLoader
					.loadObjectFromFile(reportFilePath.concat(File.separator)
							.concat(reportFileName.concat(".jasper")));
			JRBeanCollectionDataSource dataSource = DataValidatorUtils
					.hasValue(list) ? new JRBeanCollectionDataSource(list)
					: null;

			JasperPrint jasperPrint = JasperFillManager.fillReport(
					jasperReport, params, dataSource != null ? dataSource
							: new JREmptyDataSource());

			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output);
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING,
					"ISO-8859-1");

			exporter.exportReport();
			localFile = reportFilePath.concat(File.separator).concat(
					reportFileName.concat(".pdf"));
			JasperExportManager.exportReportToPdfFile(jasperPrint, localFile);

			FacesContext context = FacesContext.getCurrentInstance();
			ServletContext servletContext = (ServletContext) context
					.getExternalContext().getContext();
			InputStream resourceAsStream = new FileInputStream(
					servletContext.getRealPath(File.separator.concat("reports")
							.concat(File.separator).concat(reportFileName)
							.concat(".pdf")));

			defaultStreamedContent = new DefaultStreamedContent(
					resourceAsStream, "application/pdf");

		} catch (JRException e) {
			e.printStackTrace();
			addErrorMessage(String.format(
					"Ocorreu um erro durante a geração do relatório %s \n %s",
					localFile, e.getLocalizedMessage()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			addErrorMessage(String.format(
					"Ocorreu um erro durante a geração do relatório %s \n %s",
					localFile, e.getLocalizedMessage()));
		}
		return defaultStreamedContent;
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
