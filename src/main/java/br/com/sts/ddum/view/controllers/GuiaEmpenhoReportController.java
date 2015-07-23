package br.com.sts.ddum.view.controllers;

import java.io.File;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIForm;

import org.primefaces.component.media.Media;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.entities.Unidade;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.UnidadeService;

@ManagedBean
@ViewScoped
public class GuiaEmpenhoReportController extends BaseReportController {

	private static final long serialVersionUID = 7416053011706409698L;

	private Responsavel responsavel;

	private Unidade unidade;

	private boolean detailed;

	private boolean emptyReport;

	private UIForm termoCompromissoReportForm;

	private Media guiaEmpenhoMedia;

	private StreamedContent reportStream;

	private String exercicio;

	@ManagedProperty("#{unidadeService}")
	private UnidadeService unidadeService;

	@PostConstruct
	public void init() {
		guiaEmpenhoMedia = new Media();
		reportStream = new DefaultStreamedContent();
		unidade = new Unidade();
		responsavel = new Responsavel();
	}

	public GuiaEmpenhoReportController() {
		detailed = true;
	}

	public void generateReportByTemplate() {

		if (getResponsavel().getId().intValue() != getUnidade()
				.getResponsavel().getId().intValue()) {
			addErrorMessage("O Responsável informado não corresponde ao Responsável da Unidade!");

			return;
		}
		String fileNameReport = String.format("GuiaEmpenho_%d_%d_%s",
				getResponsavel().getId(), getUnidade().getId(), getExercicio());
		getGuiaEmpenhoMedia().setValue(
				String.format("%s%s%s%s%s", File.separator, "reports",
						File.separator, fileNameReport, ".pdf"));
		// init();
	}

	public List<Unidade> autocompletarUnidade(String valor) {
		return unidadeService.autocompletarPorResponsavel(valor,
				getResponsavel().getId());
	}

	public void limparRelatorio() {
		getGuiaEmpenhoMedia().setValue(null);
	}

	public String[] getExercicios() {
		return UtilsModel.getExercicios(10);
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

	public Media getGuiaEmpenhoMedia() {
		return guiaEmpenhoMedia;
	}

	public void setGuiaEmpenhoMedia(Media guiaEmpenhoMedia) {
		this.guiaEmpenhoMedia = guiaEmpenhoMedia;
	}

	public String getExercicio() {
		return exercicio;
	}

	public void setExercicio(String exercicio) {
		this.exercicio = exercicio;
	}
}
