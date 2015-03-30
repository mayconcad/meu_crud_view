package br.com.sts.ddum.view.controllers;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;

import br.com.sts.ddum.model.entities.AtividadeContabil;
import br.com.sts.ddum.model.entities.ContaContabil;
import br.com.sts.ddum.model.entities.FonteRecurso;
import br.com.sts.ddum.model.entities.ParametroRepasse;
import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.entities.SegmentoEnum;
import br.com.sts.ddum.model.entities.Unidade;
import br.com.sts.ddum.model.entities.UnidadeContabil;
import br.com.sts.ddum.model.enums.QueryEnum;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.enums.ZonaLocalizacaoEnum;
import br.com.sts.ddum.service.interfaces.ParametroRepasseService;
import br.com.sts.ddum.service.interfaces.UnidadeService;

@ManagedBean
@ViewScoped
public class BuscarUnidadeController extends BaseController {

	private static final long serialVersionUID = 2883233373923310796L;

	private String valorRepasse;
	private String zonaLocalizacao;
	private Unidade unidadeBusca = new Unidade();
	private Unidade unidadeRemove = new Unidade();
	private Unidade unidadeEdite = new Unidade();
	private List<Unidade> unidades = new ArrayList<Unidade>();

	private Responsavel responsavel;

	private ContaContabil contaContabil;

	private AtividadeContabil atividadeContabil;

	private FonteRecurso fonteRecurso;

	private Statement createStatement = null;
	private ResultSet result = null;

	private Tab editTab;
	private Tab findTab;

	@ManagedProperty("#{unidadeService}")
	private UnidadeService unidadeService;

	@ManagedProperty("#{parametroRepasseService}")
	private ParametroRepasseService parametroRepasseService;

	private UnidadeController unidadeController;

	private SegmentoEnum segmento;

	@PostConstruct
	public void init() {

		unidadeEdite = new Unidade();
		unidadeEdite.setResponsavel(new Responsavel());
		unidadeEdite.setParametroRepasse(new ParametroRepasse());
		unidadeEdite.setUnidadeContabil(new UnidadeContabil());
		unidadeBusca = new Unidade();
		unidadeBusca.setResponsavel(new Responsavel());
		unidadeBusca.setParametroRepasse(new ParametroRepasse());
		unidadeBusca.setUnidadeContabil(new UnidadeContabil());
	}

	public void buscar() {
		Map<String, Object> params = new HashMap<String, Object>();
		if (unidadeBusca.getNome() != null)
			params.put("nome", unidadeBusca.getNome());
		if (unidadeBusca.getResponsavel() != null
				&& unidadeBusca.getResponsavel().getId() != null)
			params.put("responsavel.id", unidadeBusca.getResponsavel().getId());
		if (unidadeBusca.getUnidadeContabil() != null
				&& unidadeBusca.getUnidadeContabil().getId() != null)
			params.put("unidadeContabil.id", unidadeBusca.getUnidadeContabil()
					.getId());
		if (unidadeBusca.getParametroRepasse() != null
				&& unidadeBusca.getParametroRepasse().getId() != null)
			params.put("parametroRepasse.id", unidadeBusca
					.getParametroRepasse().getId());
		if (getValorRepasse() != null && !getValorRepasse().isEmpty())
			params.put("parametroRepasse.valorRepasse",
					convertStringToBigDecimal(getValorRepasse()));
		unidades = unidadeService.buscar(params);
	}

	private BigDecimal convertStringToBigDecimal(String valorRepasse) {
		return new BigDecimal(valorRepasse.replace("R$", "").replace(".", "")
				.replace(",", "."));
	}

	public List<UnidadeContabil> autocompletarUnidadeContabil(String valor) {
		return unidadeController.burcarUnidadeContabil(valor);
	}

	public List<ParametroRepasse> autocompletarParametroRepasse(String valor) {
		return parametroRepasseService.autocompletar(valor);
	}

	public List<Unidade> autocompletarUnidade(String valor) {
		return unidadeService.autocompletar(valor);
	}

	public List<AtividadeContabil> autocompletarAtividade(String valor) {

		unidadeController.setUnidadeContabil(unidadeEdite.getUnidadeContabil());

		List<AtividadeContabil> autocompletarAtividade = unidadeController
				.autocompletarAtividade(valor);
		unidadeController.setUnidadeContabil(new UnidadeContabil());
		return autocompletarAtividade;
	}

	public List<ContaContabil> autocompletarConta(String valor) {

		unidadeController.setAtividadeContabil(getAtividadeContabil());

		List<ContaContabil> autocompletarConta = unidadeController
				.autocompletarConta(valor);
		unidadeController.setAtividadeContabil(new AtividadeContabil());
		return autocompletarConta;
	}

	public List<FonteRecurso> autocompletarRecurso(String valor) {

		unidadeController.setContaContabil(getContaContabil());

		List<FonteRecurso> autocompletarRecurso = unidadeController
				.autocompletarRecurso(valor);
		unidadeController.setContaContabil(new ContaContabil());
		return autocompletarRecurso;
	}

	public void remover() {

		if (usuarioSemPermissao())
			return;

		try {
			unidadeService.remover(unidadeRemove);
		} catch (Exception e) {
			addErrorMessage(String.format(
					"%s \nConsulte o Suporte Técnico: %s",
					ResultMessages.ERROR_CRUD.getDescricao(), e.getMessage()));
			return;
		}
		buscar();
		addInfoMessage(ResultMessages.DELETE_SUCESS.getDescricao());
	}

	public void editar(ActionEvent actionEvent) {

		try {
			unidadeEdite.getUnidadeContabil().setAtividade(
					getAtividadeContabil());
			unidadeEdite.getUnidadeContabil().getAtividade()
					.setContaContabil(getContaContabil());
			unidadeEdite.getUnidadeContabil().getAtividade().getContaContabil()
					.setFonteRecurso(getFonteRecurso());
			unidadeEdite.getParametroRepasse().setSegmento(getSegmento());
			unidadeService.atualizar(unidadeEdite);
		} catch (Exception e) {
			addErrorMessage(String.format(
					"%s \nConsulte o Suporte Técnico: %s",
					ResultMessages.ERROR_CRUD.getDescricao(),
					e.getLocalizedMessage()));
			return;
		}
		addInfoMessage(ResultMessages.UPDATE_SUCESS.getDescricao());
		loadToFind();
	}

	public void loadToFind() {
		getEditTab().setRendered(false);
		TabView parent = (TabView) getFindTab().getParent();
		int findIndex = parent.getChildren().indexOf(getFindTab());
		parent.setActiveIndex(findIndex);
	}

	public void carregar() {

		// getEditTab().setRendered(false);
		// parent.setActiveIndex(1);

		if (usuarioSemPermissao()) {

			RequestContext.getCurrentInstance().update(
					"unidadeTabView:buscarUnidadeForm");
			return;
		}

		getEditTab().setRendered(true);
		TabView parent = (TabView) getEditTab().getParent();
		int editIndex = parent.getChildren().indexOf(getEditTab());
		parent.setActiveIndex(editIndex);
		setSegmento(unidadeEdite.getParametroRepasse().getSegmento());
		setAtividadeContabil(unidadeEdite.getUnidadeContabil().getAtividade());
		setContaContabil(unidadeEdite.getUnidadeContabil().getAtividade()
				.getContaContabil());
		setFonteRecurso(unidadeEdite.getUnidadeContabil().getAtividade()
				.getContaContabil().getFonteRecurso());

		FacesContext currentInstance = FacesContext.getCurrentInstance();
		unidadeController = (UnidadeController) currentInstance
				.getELContext()
				.getELResolver()
				.getValue(currentInstance.getELContext(), null,
						"unidadeController");
		// unidadeController.setAtividadesContabil(Arrays.asList(unidadeEdite
		// .getUnidadeContabil().getAtividade()));
		// unidadeController.setContasContabil(Arrays.asList(unidadeEdite
		// .getUnidadeContabil().getAtividade().getContaContabil()));
		// unidadeController.setRecursosContabil(Arrays.asList(unidadeEdite
		// .getUnidadeContabil().getAtividade().getContaContabil()
		// .getFonteRecurso()));
	}

	public void carregarDados() {

		valorRepasse = new String();
		if (unidadeEdite.getParametroRepasse().getSegmento() == null) {
			unidadeEdite.setParametroRepasse(new ParametroRepasse());
			unidadeEdite.getParametroRepasse().setSegmento(SegmentoEnum.PMDDSO);
		}
	}

	public void carregarDotacaoOrcamento() {

		FacesContext currentInstance = FacesContext.getCurrentInstance();
		unidadeController = (UnidadeController) currentInstance
				.getELContext()
				.getELResolver()
				.getValue(currentInstance.getELContext(), null,
						"unidadeController");

		try {
			createStatement = unidadeController.getConexaoBanco()
					.createStatement();

			carregarUnidadeContabil();
			carregarAtividade();
			carregarConta();
			carregarRecurso();

			createStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			addErrorMessage(ResultMessages.ERROR_LOAD_DOTACAO.getDescricao());
		}

	}

	private void carregarUnidadeContabil() throws SQLException {
		String query = String.format(QueryEnum.UNIDADES_CONTABEIS.toString()
				.concat(unidadeController.COD_UNIDADE_QUERY), unidadeEdite
				.getParametroRepasse().getCodUnidade());
		result = createStatement.executeQuery(query);

		while (result.next()) {
			unidadeEdite.setUnidadeContabil(new UnidadeContabil(result
					.getInt(1), result.getInt(1) + "", result.getString(2)));
		}
		result.close();
	}

	private void carregarAtividade() throws SQLException {
		String query = String.format(QueryEnum.ATIVIDADES_CONTABEIS.toString()
				.concat(unidadeController.COD_ATIVIDADE_QUERY), unidadeEdite
				.getParametroRepasse().getCodUnidade(), unidadeEdite
				.getParametroRepasse().getCodAtividade());
		result = createStatement.executeQuery(query);

		while (result.next()) {
			setAtividadeContabil(new AtividadeContabil(result.getInt(1),
					result.getInt(1) + "", result.getString(2),
					result.getInt(3) + ""));
		}
		result.close();
	}

	private void carregarConta() throws SQLException {
		String query = String.format(QueryEnum.CONTAS_CONTABEIS.toString()
				.concat(unidadeController.COD_CONTA_QUERY), unidadeEdite
				.getParametroRepasse().getCodAtividade(),
				pegarSeisCaracteres(unidadeEdite.getParametroRepasse()
						.getCodElementoDespesa()));
		result = createStatement.executeQuery(query);

		while (result.next()) {
			setContaContabil(new ContaContabil(Long.parseLong(result
					.getString(1)), result.getString(1), result.getString(2)));
		}
		result.close();
	}

	private String pegarSeisCaracteres(String codElementoDespesa) {
		return codElementoDespesa.substring(0, 6);
	}

	private void carregarRecurso() throws SQLException {
		String query = String.format(QueryEnum.RECURSOS_CONTABEIS.toString()
				.concat(unidadeController.COD_RECURSO_QUERY),
				pegarSeisCaracteres(unidadeEdite.getParametroRepasse()
						.getCodElementoDespesa()), unidadeEdite
						.getParametroRepasse().getCodFonteRecurso());
		result = createStatement.executeQuery(query);

		while (result.next()) {
			setFonteRecurso(new FonteRecurso(result.getInt(1), result.getInt(1)
					+ "", result.getString(2)));
		}
		result.close();
	}

	public SegmentoEnum[] getTipos() {
		return SegmentoEnum.values();
	}

	public ZonaLocalizacaoEnum[] getZonas() {
		return ZonaLocalizacaoEnum.values();
	}

	public Unidade getUnidadeRemove() {
		return unidadeRemove;
	}

	public void setUnidadeRemove(Unidade unidadeRemove) {
		this.unidadeRemove = unidadeRemove;
	}

	public Unidade getUnidadeEdite() {
		return unidadeEdite;
	}

	public void setUnidadeEdite(Unidade unidadeEdite) {
		this.unidadeEdite = unidadeEdite;
	}

	public List<Unidade> getUnidades() {
		return unidades;
	}

	public void setUnidades(List<Unidade> unidades) {
		this.unidades = unidades;
	}

	public UnidadeService getUnidadeService() {
		return unidadeService;
	}

	public void setUnidadeService(UnidadeService unidadeService) {
		this.unidadeService = unidadeService;
	}

	public String getValorRepasse() {
		return valorRepasse;
	}

	public void setValorRepasse(String valorRepasse) {
		this.valorRepasse = valorRepasse;
	}

	public Unidade getUnidadeBusca() {
		return unidadeBusca;
	}

	public void setUnidadeBusca(Unidade unidadeBusca) {
		this.unidadeBusca = unidadeBusca;
	}

	public Tab getEditTab() {
		return editTab;
	}

	public void setEditTab(Tab editTab) {
		this.editTab = editTab;
	}

	public Tab getFindTab() {
		return findTab;
	}

	public void setFindTab(Tab findTab) {
		this.findTab = findTab;
	}

	public Responsavel getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(Responsavel responsavel) {
		this.responsavel = responsavel;
	}

	public ParametroRepasseService getParametroRepasseService() {
		return parametroRepasseService;
	}

	public void setParametroRepasseService(
			ParametroRepasseService parametroRepasseService) {
		this.parametroRepasseService = parametroRepasseService;
	}

	public String getZonaLocalizacao() {
		return zonaLocalizacao;
	}

	public void setZonaLocalizacao(String zonaLocalizacao) {
		this.zonaLocalizacao = zonaLocalizacao;
	}

	public SegmentoEnum getSegmento() {
		return segmento;
	}

	public void setSegmento(SegmentoEnum segmento) {
		this.segmento = segmento;
	}

	public ContaContabil getContaContabil() {
		return contaContabil;
	}

	public void setContaContabil(ContaContabil contaContabil) {
		this.contaContabil = contaContabil;
	}

	public AtividadeContabil getAtividadeContabil() {
		return atividadeContabil;
	}

	public void setAtividadeContabil(AtividadeContabil atividadeContabil) {
		this.atividadeContabil = atividadeContabil;
	}

	public FonteRecurso getFonteRecurso() {
		return fonteRecurso;
	}

	public void setFonteRecurso(FonteRecurso fonteRecurso) {
		this.fonteRecurso = fonteRecurso;
	}

	public UnidadeController getUnidadeController() {
		return unidadeController;
	}

	public void setUnidadeController(UnidadeController unidadeController) {
		this.unidadeController = unidadeController;
	}

}
