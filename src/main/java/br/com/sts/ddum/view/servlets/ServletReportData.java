package br.com.sts.ddum.view.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import ar.com.fdvs.dj.domain.DynamicReport;
import br.com.sts.ddum.model.enums.ReportType;
import br.com.sts.ddum.model.utils.DataValidatorUtils;

@WebServlet(name = "Report", urlPatterns = { "/ReportData" })
public class ServletReportData extends HttpServlet {

	private static final long serialVersionUID = -2732096537498385076L;
	private static final String PATH_REPORT_JASPER = String.format(
			"%sreports%s", File.separator, File.separator);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ServletOutputStream outputStream = null;

		try {
			JasperPrint jasperPrint = null;
			HttpSession session = request.getSession(true);

			// Retorna o valor do atributo setado no controller
			Collection dataList = (Collection) session.getAttribute("dataList");
			String reportName = (String) session.getAttribute("reportName");
			reportName = "TermoCompromisso";
			JRBeanCollectionDataSource dataSource = DataValidatorUtils
					.hasValue(dataList) ? new JRBeanCollectionDataSource(
					dataList) : null;
			Map parameters = (Map) session.getAttribute("parameters");
			ReportType type = (ReportType) session.getAttribute("reportType");
			boolean reportInline = (boolean) session
					.getAttribute("reportInline");
			DynamicReport report = (DynamicReport) session
					.getAttribute("dynamicReport");
			String reportFileName = (String) session
					.getAttribute("reportFileName");

			// if (reportFileName != null && !reportFileName.isEmpty()) {
			JasperReport jasperReport = (JasperReport) JRLoader
					.loadObjectFromFile(String.valueOf(
							session.getAttribute("reportPath")).concat(
							File.separator.concat("TermoCompromisso.jasper")));
			jasperPrint = JasperFillManager.fillReport(jasperReport,
					parameters, dataSource != null ? dataSource
							: new JREmptyDataSource());
			// } else
			// jasperPrint = JasperFillManager.fillReport(DynamicJasperHelper
			// .generateJasperReport(report,
			// new ClassicLayoutManager(), new HashMap()),
			// parameters, dataSource);

			byte bytes[] = null;
			JRExporter exporter = null;
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			switch (type) {

			case PDF:
				exporter = new JRPdfExporter();
				response.setContentType("application/pdf");
				reportName = reportName.concat(".pdf");
				break;
			case HTML:
				exporter = new JRHtmlExporter();
				response.setContentType("text/html");
				reportName = reportName.concat(".html");
				request.getSession().setAttribute(
						ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE,
						jasperPrint);
				exporter.setParameter(JRExporterParameter.OUTPUT_WRITER,
						response.getWriter());

				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
				break;
			case XLS:
				exporter = new JRXlsExporter();
				response.setContentType("application/xls");
				reportName = reportName.concat(".xls");
				exporter.setParameter(
						JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, false);
				exporter.setParameter(
						JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, false);
				exporter.setParameter(
						JRXlsExporterParameter.IS_DETECT_CELL_TYPE, true);
				exporter.setParameter(
						JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
						true);
				exporter.setParameter(
						JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET,
						Integer.decode("65000"));
				exporter.setParameter(
						JRXlsExporterParameter.IS_IMAGE_BORDER_FIX_ENABLED,
						true);
				exporter.setParameter(
						JRXlsExporterParameter.IGNORE_PAGE_MARGINS, true);
				exporter.setParameter(
						JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,
						true);

				break;
			case CVS:
				exporter = new JRCsvExporter();
				response.setContentType("application/cvs");
				reportName = reportName.concat(".cvs");
				break;
			case TXT:
				exporter = new JRTextExporter();
				response.setContentType("application/txt");
				reportName = reportName.concat(".txt");
				exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH,
						new Float(5.2));
				exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT,
						new Float(15.1));
				exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT,
						new Integer(842));
				exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH,
						new Integer(595));
				break;
			case RTF:
				exporter = new JRRtfExporter();
				response.setContentType("text/rtf");
				reportName = reportName.concat(".rtf");
				break;

			}
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output);
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING,
					"ISO-8859-1");

			exporter.exportReport();
			bytes = output.toByteArray();

			response.setCharacterEncoding("ISO-8859-1");
			response.setContentLength(bytes.length);

			outputStream = response.getOutputStream();
			if (reportInline) {
				response.setHeader("Content-Disposition",
						String.format("%s%s", "inline; filename=", reportName));
				JasperExportManager.exportReportToPdfStream(jasperPrint,
						outputStream);
			} else {
				response.setHeader("Content-Disposition", String.format("%s%s",
						"attachment; filename=", reportName));
				outputStream.write(bytes);
			}
			outputStream.flush();
		} catch (JRException e) {
			Logger.getLogger(ServletReportData.class.getName()).log(
					Level.SEVERE, null, e);
		} finally {
			if (outputStream != null)
				outputStream.close();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			processRequest(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			processRequest(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}
}
