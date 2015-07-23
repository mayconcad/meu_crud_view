package br.com.sts.ddum.view.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.primefaces.component.media.Media;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.entities.Unidade;
import br.com.sts.ddum.service.interfaces.UnidadeService;

@ManagedBean
@ViewScoped
public class TermoCompromissoReportController extends BaseReportController {

	private static final long serialVersionUID = 7416053011706409698L;

	private Responsavel responsavel;

	private Unidade unidade;

	private boolean detailed;

	private boolean emptyReport;

	private UIForm termoCompromissoReportForm;

	private Media termoCompromissoMedia;

	private StreamedContent reportStream;

	@ManagedProperty("#{unidadeService}")
	private UnidadeService unidadeService;

	@PostConstruct
	public void init() {
		termoCompromissoMedia = new Media();
		reportStream = new DefaultStreamedContent();
		unidade = new Unidade();
		responsavel = new Responsavel();
	}

	public TermoCompromissoReportController() {
		detailed = true;
	}

	public void generateReportByTemplate(Responsavel responsavel,
			Unidade unidade) {
		boolean reportInline = true;
		if (responsavel != null && unidade != null) {
			setResponsavel(responsavel);
			setUnidade(unidade);
			reportInline = false;
		}
		if (getResponsavel().getId().intValue() != getUnidade()
				.getResponsavel().getId().intValue()) {
			addErrorMessage("O Responsável informado não corresponde ao Responsável da Unidade!");

			return;
		}
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("local", "Parnaíba - PI");
		parameters.put("responsavel", getResponsavel().getNome());
		parameters.put("cpf", getResponsavel().getCpf());
		parameters.put("rg", getResponsavel().getRg());
		parameters.put("enderecoResponsavel", String.format("%s - %s - %s",
				getResponsavel().getEndereco(), getResponsavel().getBairro(),
				getResponsavel().getCidade()));
		parameters.put("unidadeFisica", getUnidade().getNome());

		parameters.put("enderecoUnidade", String.format("%s - %s - %s",
				getUnidade().getLogradouro(), getUnidade()
						.getNumeroLogradouro(), getUnidade().getBairro()));
		parameters.put("segmento", getUnidade().getParametroRepasse()
				.getSegmento().toString());

		FacesContext context = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) context
				.getExternalContext().getContext();
		generateStremedReport(servletContext.getRealPath(File.separator.concat(
				"reports").concat(File.separator)), "TermoCompromisso",
				"TermoCompromisso", new ArrayList(), parameters);

		getTermoCompromissoMedia().setValue(
				File.separator.concat("reports").concat(File.separator)
						.concat("TermoCompromisso.pdf"));
		// if (session != null) {
		// session.setAttribute("reportInline", reportInline);
		// session.setAttribute("reportType", getReportType());
		// session.setAttribute("parameters", parameters);
		// }
		//
		// redirect("/ReportData");
		init();
	}

	public List<Unidade> autocompletarUnidade(String valor) {
		return unidadeService.autocompletarPorResponsavel(valor,
				getResponsavel().getId());
	}

	public void limparRelatorio() {
		getTermoCompromissoMedia().setValue(null);
	}

	public boolean getDetailed() {
		return detailed;
	}

	public void setDetailed(boolean detailed) {
		this.detailed = detailed;
	}

	public boolean isEmptyReport() {
		return emptyReport;
	}

	public void setEmptyReport(boolean emptyReport) {
		this.emptyReport = emptyReport;
	}

	public Responsavel getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(Responsavel responsavel) {
		this.responsavel = responsavel;
	}

	public Unidade getUnidade() {
		return unidade;
	}

	public void setUnidade(Unidade unidade) {
		this.unidade = unidade;
	}

	public UIForm getTermoCompromissoReportForm() {
		return termoCompromissoReportForm;
	}

	public void setTermoCompromissoReportForm(UIForm termoCompromissoReportForm) {
		this.termoCompromissoReportForm = termoCompromissoReportForm;
	}

	public UnidadeService getUnidadeService() {
		return unidadeService;
	}

	public void setUnidadeService(UnidadeService unidadeService) {
		this.unidadeService = unidadeService;
	}

	public Media getTermoCompromissoMedia() {
		return termoCompromissoMedia;
	}

	public void setTermoCompromissoMedia(Media termoCompromissoMedia) {
		this.termoCompromissoMedia = termoCompromissoMedia;
	}
}
