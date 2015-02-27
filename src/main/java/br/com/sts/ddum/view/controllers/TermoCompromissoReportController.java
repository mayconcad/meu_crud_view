package br.com.sts.ddum.view.controllers;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIForm;

import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.entities.Unidade;

@ManagedBean
@ViewScoped
public class TermoCompromissoReportController extends BaseReportController {

	private static final long serialVersionUID = 7416053011706409698L;

	private Responsavel responsavel;

	private Unidade unidade;

	private boolean detailed;

	private boolean emptyReport;

	private UIForm termoCompromissoReportForm;

	@PostConstruct
	public void init() {
		responsavel = new Responsavel();
		unidade = new Unidade();
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
		session.setAttribute("reportInline", reportInline);
		session.setAttribute("reportType", getReportType());
		session.setAttribute("parameters", parameters);

		redirect("/ReportData");
		init();
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
}
