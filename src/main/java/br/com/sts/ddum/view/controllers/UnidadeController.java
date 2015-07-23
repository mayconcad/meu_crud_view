package br.com.sts.ddum.view.controllers;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.omnifaces.util.Ajax;
import org.primefaces.component.media.Media;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

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
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.ConnectionConfigService;
import br.com.sts.ddum.service.interfaces.ParametroRepasseService;
import br.com.sts.ddum.service.interfaces.UnidadeService;
import br.com.sts.ddum.view.utils.UtilsView;

@Controller
@ViewScoped
public class UnidadeController extends BaseReportController {

	private static final long serialVersionUID = -4373225131597424442L;

	public final String COD_UNIDADE_QUERY = " WHERE cgp.unidade.cd_unidade = %s ";
	public final String COD_ATIVIDADE_QUERY = " AND cgp.atividade_projeto.cd_atividade_projeto = %s ";
	public final String COD_CONTA_QUERY = " AND cgp.plano_conta.cd_codigo_conta LIKE '%s%%' ";
	public final String COD_RECURSO_QUERY = " AND cgp.fonte_recurso.cd_fonte_recurso = %s ";

	private final String COD_NOME_UNIDADE_QUERY = " WHERE (CAST(cgp.unidade.cd_unidade AS TEXT) LIKE '%%%s%%' OR LOWER(cgp.unidade.nm_unidade) LIKE '%%%s%%') ";
	private final String COD_NOME_ATIVIDADE_QUERY = " AND (CAST(cgp.atividade_projeto.cd_atividade_projeto AS TEXT) LIKE '%%%s%%' OR LOWER(cgp.atividade_projeto.de_atividade_projeto) LIKE '%%%s%%') ";
	private final String COD_NOME_CONTA_QUERY = " AND (cgp.plano_conta.cd_codigo_conta  LIKE '%%%s%%' OR LOWER(cgp.plano_conta.de_titulo) LIKE '%%%s%%') ";
	private final String COD_NOME_RECURSO_QUERY = " AND (CAST(cgp.fonte_recurso.cd_fonte_recurso AS TEXT) LIKE '%%%s%%' OR LOWER(cgp.fonte_recurso.de_fonte_recurso) LIKE '%%%s%%') ";

	@Autowired
	private UnidadeService unidadeService;

	@Autowired
	private ParametroRepasseService parametroRepasseService;

	@Autowired
	private ConnectionConfigService connectionConfigService;

	private TermoCompromissoReportController reportController;

	private Unidade unidade;

	private ZonaLocalizacaoEnum zonaLocalizacao;

	private int quantAluno;

	private String valorRepasse;

	private Responsavel responsavel;

	private UnidadeContabil unidadeContabil;

	private AtividadeContabil atividadeContabil;

	private ContaContabil contaContabil;

	private FonteRecurso fonteRecurso;
	private ParametroRepasse parametroRepasse;

	private List<UnidadeContabil> unidadesContabil = new ArrayList<UnidadeContabil>();
	private List<AtividadeContabil> atividadesContabil = new ArrayList<AtividadeContabil>();
	private List<ContaContabil> contasContabil = new ArrayList<ContaContabil>();
	private List<FonteRecurso> recursosContabil = new ArrayList<FonteRecurso>();

	private SegmentoEnum segmento;

	private UIForm createUnidadeForm;

	private Media termoCompromissoMedia = new Media();

	@PostConstruct
	public void init() {
		responsavel = new Responsavel();
		unidadeContabil = new UnidadeContabil();
		atividadeContabil = new AtividadeContabil();
		contaContabil = new ContaContabil();
		fonteRecurso = new FonteRecurso();
		parametroRepasse = new ParametroRepasse();
		unidade = new Unidade();
		unidade.setUnidadeContabil(unidadeContabil);
		unidade.setParametroRepasse(parametroRepasse);
		unidade.setResponsavel(responsavel);
		conexaoBanco = connectionConfigService.obterConexaoBancoCGP();
		Ajax.update(":unidadeTabView");

	}

	public void limparDados() {
		init();
		BuscarUnidadeController controllerInstance = UtilsView
				.getControllerInstance(BuscarUnidadeController.class);
		if (controllerInstance != null)
			controllerInstance.init();
	}

	public void gerarTermoCompromisso() {

		Responsavel responsavelLocal = new Responsavel();
		responsavelLocal.setId(unidade.getResponsavel().getId());
		responsavelLocal.setNome(unidade.getResponsavel().getNome());
		responsavelLocal.setCpf(unidade.getResponsavel().getCpf());
		responsavelLocal.setRg(unidade.getResponsavel().getRg());
		responsavelLocal.setCidade(unidade.getResponsavel().getCidade());
		responsavelLocal.setEndereco(unidade.getResponsavel().getEndereco());
		responsavelLocal.setMatriculaFuncional(unidade.getResponsavel()
				.getMatriculaFuncional());
		responsavelLocal.setBairro(unidade.getResponsavel().getBairro());

		Unidade unidadeLocal = new Unidade();
		unidadeLocal.setLogradouro(unidade.getLogradouro());
		unidadeLocal.setBairro(unidade.getBairro());
		unidadeLocal.setNome(unidade.getNome());
		unidadeLocal.setNumeroLogradouro(unidade.getNumeroLogradouro());
		ParametroRepasse parametro = new ParametroRepasse();
		parametro.setSegmento(getSegmento());
		unidadeLocal.setParametroRepasse(parametro);
		unidadeLocal.setBairro(unidade.getBairro());
		unidadeLocal.setId(unidade.getId());
		unidadeLocal.setResponsavel(responsavelLocal);

		criar(true);
		FacesContext currentInstance = FacesContext.getCurrentInstance();
		reportController = (TermoCompromissoReportController) currentInstance
				.getELContext()
				.getELResolver()
				.getValue(currentInstance.getELContext(), null,
						"termoCompromissoReportController");

		reportController.generateReportByTemplate(responsavelLocal,
				unidadeLocal);
	}

	public void criar(boolean gerarTermo) {

		if (usuarioSemPermissao("GESTOR"))
			return;

		getFonteRecurso().setId(null);
		getContaContabil().setId(null);
		getContaContabil().setFonteRecurso(getFonteRecurso());
		getAtividadeContabil().setId(null);
		getAtividadeContabil().setContaContabil(getContaContabil());
		getUnidadeContabil().setId(null);
		getUnidadeContabil().setAtividade(getAtividadeContabil());
		getParametroRepasse().setSegmento(getSegmento());

		try {
			unidade.setUnidadeContabil(getUnidadeContabil());
			unidade.setParametroRepasse(getParametroRepasse());
			if (getParametroRepasse().getSegmento().equals(SegmentoEnum.PMDDE))
				unidade.setZonaLocalizacao(getZonaLocalizacao());

			unidade = unidadeService.salvar(unidade);
		} catch (Exception e) {
			addErrorMessage(String.format(
					"%s \nConsulte o Suporte Técnico: %s",
					ResultMessages.ERROR_CRUD.getDescricao(), e.getMessage()));
			return;
		}
		addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
		if (gerarTermo) {
			// FacesContext currentInstance = FacesContext.getCurrentInstance();
			TermoCompromissoReportController reportController = UtilsView
					.getControllerInstance(TermoCompromissoReportController.class);
			// reportController = (TermoCompromissoReportController)
			// currentInstance
			// .getELContext()
			// .getELResolver()
			// .getValue(currentInstance.getELContext(), null,
			// "termoCompromissoReportController");

			reportController.generateReportByTemplate(unidade.getResponsavel(),
					unidade);
			getTermoCompromissoMedia().setValue(
					File.separator.concat("reports").concat(File.separator)
							.concat("TermoCompromisso.pdf"));
			carregarReportTab();
		}
		init();
		RequestContext.getCurrentInstance().update(":unidadeTabView");
	}

	public void carregarReportTab() {
		getReportTab().setRendered(true);
		TabView parent = (TabView) getReportTab().getParent();
		int reportIndex = parent.getChildren().indexOf(getReportTab());
		if (!getEditTab().isRendered())
			--reportIndex;
		parent.setActiveIndex(reportIndex);
	}

	public void showDialog(ActionEvent event) {
		RequestContext.getCurrentInstance().execute(
				"PF('confirmationTermo').show()");

	}

	public void carregarDados() {

		parametroRepasse = new ParametroRepasse();
		valorRepasse = new String();
		if (getSegmento() == null)
			setSegmento(SegmentoEnum.PMDDSO);
	}

	public void carregarDotacaoOrcamento() {

		try {
			createStatement = conexaoBanco.createStatement();

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
				.concat(COD_UNIDADE_QUERY), parametroRepasse.getCodUnidade());
		result = createStatement.executeQuery(query);

		while (result.next()) {
			setUnidadeContabil(new UnidadeContabil(result.getInt(1),
					result.getInt(1) + "", result.getString(2)));
		}
		result.close();
	}

	private void carregarAtividade() throws SQLException {
		String query = String.format(QueryEnum.ATIVIDADES_CONTABEIS.toString()
				.concat(COD_ATIVIDADE_QUERY), parametroRepasse.getCodUnidade(),
				parametroRepasse.getCodAtividade());
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
				.concat(COD_CONTA_QUERY), parametroRepasse.getCodAtividade(),
				UtilsModel.pegarSeisCaracteres(parametroRepasse
						.getCodElementoDespesa()));
		result = createStatement.executeQuery(query);

		while (result.next()) {
			setContaContabil(new ContaContabil(Long.parseLong(result
					.getString(1)), result.getString(1), result.getString(2)));
		}
		result.close();
	}

	private void carregarRecurso() throws SQLException {
		String query = String.format(QueryEnum.RECURSOS_CONTABEIS.toString()
				.concat(COD_RECURSO_QUERY), UtilsModel
				.pegarSeisCaracteres(parametroRepasse.getCodElementoDespesa()),
				parametroRepasse.getCodFonteRecurso());
		result = createStatement.executeQuery(query);

		while (result.next()) {
			setFonteRecurso(new FonteRecurso(result.getInt(1), result.getInt(1)
					+ "", result.getString(2)));
		}
		result.close();
	}

	public List<UnidadeContabil> burcarUnidadeContabil(String valor) {

		unidadesContabil.clear();
		String query = null;
		try {
			createStatement = conexaoBanco.createStatement();

			if (valor != null && !"".equals(valor)) {
				query = String.format(QueryEnum.UNIDADES_CONTABEIS.toString()
						.concat(COD_NOME_UNIDADE_QUERY), valor, valor);
			} else {
				query = QueryEnum.UNIDADES_CONTABEIS.toString();
			}

			result = createStatement.executeQuery(query);

			while (result.next()) {
				unidadesContabil.add(new UnidadeContabil(result.getInt(1),
						result.getInt(1) + "", result.getString(2)));

			}
			result.close();
			createStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return unidadesContabil;
	}

	public UnidadeContabil getUnidadeById(long id) {
		for (UnidadeContabil uc : getUnidadesContabil()) {
			if (uc.getId() == id)
				return uc;
		}
		return null;
	}

	public List<AtividadeContabil> autocompletarAtividade(String valor) {

		atividadesContabil.clear();
		try {
			createStatement = conexaoBanco.createStatement();

			String query = null;
			if (valor != null && !"".equals(valor)) {
				query = String.format(QueryEnum.ATIVIDADES_CONTABEIS.toString()
						.concat(COD_NOME_ATIVIDADE_QUERY), unidadeContabil
						.getCodigo(), valor, valor);
			} else {
				query = String.format(
						QueryEnum.ATIVIDADES_CONTABEIS.toString(),
						unidadeContabil.getCodigo());
			}

			result = createStatement.executeQuery(query);

			while (result.next()) {
				atividadesContabil.add(new AtividadeContabil(result.getInt(1),
						result.getInt(1) + "", result.getString(2), result
								.getInt(3) + ""));

			}
			result.close();
			createStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return atividadesContabil;
	}

	public AtividadeContabil getAtividadeContabilById(long id) {
		for (AtividadeContabil uc : getAtividadesContabil()) {
			if (uc.getId() == id)
				return uc;
		}
		return null;
	}

	public List<ContaContabil> autocompletarConta(String valor) {

		contasContabil.clear();
		try {
			createStatement = conexaoBanco.createStatement();

			String query = null;
			if (valor != null && !"".equals(valor)) {
				query = String.format(QueryEnum.CONTAS_CONTABEIS.toString()
						.concat(COD_NOME_CONTA_QUERY), atividadeContabil
						.getCodigo(), valor, valor);
			} else {
				query = String.format(QueryEnum.CONTAS_CONTABEIS.toString(),
						atividadeContabil.getCodigo());
			}

			result = createStatement.executeQuery(query);

			while (result.next()) {
				contasContabil.add(new ContaContabil(Long.parseLong(result
						.getString(1)), result.getString(1), result
						.getString(2)));

			}
			result.close();
			createStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return contasContabil;
	}

	public ContaContabil getContaContabilById(long id) {
		for (ContaContabil uc : getContasContabil()) {
			if (uc.getId() == id)
				return uc;
		}
		return null;
	}

	public List<FonteRecurso> autocompletarRecurso(String valor) {

		recursosContabil.clear();
		try {
			createStatement = conexaoBanco.createStatement();

			String query = null;
			if (valor != null && !"".equals(valor)) {
				query = String.format(QueryEnum.RECURSOS_CONTABEIS.toString()
						.concat(COD_NOME_RECURSO_QUERY), contaContabil
						.getCodigo(), valor, valor);
			} else {
				query = String.format(QueryEnum.RECURSOS_CONTABEIS.toString(),
						contaContabil.getCodigo());
			}

			result = createStatement.executeQuery(query);

			while (result.next()) {
				recursosContabil.add(new FonteRecurso(result.getInt(1), result
						.getInt(1) + "", result.getString(2)));

			}
			result.close();
			createStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return recursosContabil;
	}

	public void ativaValorParametro() {

	}

	public List<Unidade> autocompletar(String valor) {
		return unidadeService.autocompletar(valor);
	}

	public List<ParametroRepasse> autocompletarParametro(String valor) {
		Map<String, Object> params = new HashMap<String, Object>();
		if (valor != null && !"".equals(valor))
			params.put("valor", valor);
		params.put("segmento", getSegmento() == null ? SegmentoEnum.PMDDSO
				: getSegmento());
		return parametroRepasseService.autocompletar(params);

	}

	public FonteRecurso getFonteRecursoById(long id) {
		for (FonteRecurso uc : getRecursosContabil()) {
			if (uc.getId() == id)
				return uc;
		}
		return null;
	}

	public Connection getConexaoBanco() {
		return conexaoBanco;
	}

	public void setConexaoBanco(Connection conexaoBanco) {
		this.conexaoBanco = conexaoBanco;
	}

	public ConnectionConfigService getConnectionConfigService() {
		return connectionConfigService;
	}

	public void setConnectionConfigService(
			ConnectionConfigService connectionConfigService) {
		this.connectionConfigService = connectionConfigService;
	}

	public UnidadeContabil getUnidadeContabil() {
		return unidadeContabil;
	}

	public void setUnidadeContabil(UnidadeContabil unidadeContabil) {
		this.unidadeContabil = unidadeContabil;
	}

	public AtividadeContabil getAtividadeContabil() {
		return atividadeContabil;
	}

	public void setAtividadeContabil(AtividadeContabil atividadeContabil) {
		this.atividadeContabil = atividadeContabil;
	}

	public ContaContabil getContaContabil() {
		return contaContabil;
	}

	public void setContaContabil(ContaContabil contaContabil) {
		this.contaContabil = contaContabil;
	}

	public FonteRecurso getFonteRecurso() {
		return fonteRecurso;
	}

	public void setFonteRecurso(FonteRecurso fonteRecurso) {
		this.fonteRecurso = fonteRecurso;
	}

	public List<UnidadeContabil> getUnidadesContabil() {
		return unidadesContabil;
	}

	public void setUnidadesContabil(List<UnidadeContabil> unidadesContabil) {
		this.unidadesContabil = unidadesContabil;
	}

	public List<AtividadeContabil> getAtividadesContabil() {
		return atividadesContabil;
	}

	public void setAtividadesContabil(List<AtividadeContabil> atividadesContabil) {
		this.atividadesContabil = atividadesContabil;
	}

	public List<ContaContabil> getContasContabil() {
		return contasContabil;
	}

	public void setContasContabil(List<ContaContabil> contasContabil) {
		this.contasContabil = contasContabil;
	}

	public List<FonteRecurso> getRecursosContabil() {
		return recursosContabil;
	}

	public void setRecursosContabil(List<FonteRecurso> recursosContabil) {
		this.recursosContabil = recursosContabil;
	}

	public UnidadeService getUnidadeServiceImpl() {
		return unidadeService;
	}

	public void setUnidadeServiceImpl(UnidadeService unidadeServiceImpl) {
		this.unidadeService = unidadeServiceImpl;
	}

	public Unidade getUnidade() {
		return unidade;
	}

	public void setUnidade(Unidade unidade) {
		this.unidade = unidade;
	}

	public SegmentoEnum[] getTipos() {
		return SegmentoEnum.values();
	}

	public ZonaLocalizacaoEnum[] getZonas() {
		return ZonaLocalizacaoEnum.values();
	}

	public ZonaLocalizacaoEnum getZonaLocalizacao() {
		return zonaLocalizacao;
	}

	public void setZonaLocalizacao(ZonaLocalizacaoEnum zonaLocalizacao) {
		this.zonaLocalizacao = zonaLocalizacao;
	}

	public int getQuantAluno() {
		return quantAluno;
	}

	public void setQuantAluno(int quantAluno) {
		this.quantAluno = quantAluno;
	}

	public String getValorRepasse() {
		return valorRepasse;
	}

	public void setValorRepasse(String valorRepasse) {
		this.valorRepasse = valorRepasse;
	}

	public UnidadeService getUnidadeService() {
		return unidadeService;
	}

	public void setUnidadeService(UnidadeService unidadeService) {
		this.unidadeService = unidadeService;
	}

	public Responsavel getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(Responsavel responsavel) {
		this.responsavel = responsavel;
	}

	public ParametroRepasse getParametroRepasse() {
		return parametroRepasse;
	}

	public void setParametroRepasse(ParametroRepasse parametroRepasse) {
		this.parametroRepasse = parametroRepasse;
	}

	public SegmentoEnum getSegmento() {
		return segmento;
	}

	public void setSegmento(SegmentoEnum segmento) {
		this.segmento = segmento;
	}

	public ParametroRepasseService getParametroRepasseService() {
		return parametroRepasseService;
	}

	public void setParametroRepasseService(
			ParametroRepasseService parametroRepasseService) {
		this.parametroRepasseService = parametroRepasseService;
	}

	public UIForm getCreateUnidadeForm() {
		return createUnidadeForm;
	}

	public Media getTermoCompromissoMedia() {
		return termoCompromissoMedia;
	}

	public void setTermoCompromissoMedia(Media termoCompromissoMedia) {
		this.termoCompromissoMedia = termoCompromissoMedia;
	}

	public void setCreateUnidadeForm(UIForm createUnidadeForm) {
		this.createUnidadeForm = createUnidadeForm;
	}

	public TermoCompromissoReportController getReportController() {
		return reportController;
	}

	public void setReportController(
			TermoCompromissoReportController reportController) {
		this.reportController = reportController;
	}

}
