package br.com.sts.ddum.view.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;

import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;

import br.com.sts.ddum.model.entities.ParametroRepasse;
import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.entities.Unidade;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.enums.TipoContaEnum;
import br.com.sts.ddum.model.utils.JPAPersistence;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.ParametroRepasseService;
import br.com.sts.ddum.service.interfaces.ResponsavelService;
import br.com.sts.ddum.service.interfaces.UnidadeService;
import br.com.sts.ddum.view.utils.ValidateUtils;

@ManagedBean
@ViewScoped
public class BuscarResponsavelController extends BaseController {

	private static final long serialVersionUID = 2883233373923310796L;

	private String nome;
	private String matriculaFuncional;
	private Responsavel responsavelRemove;
	private Responsavel responsavelBusca;
	private Responsavel responsavelEdite;
	private List<Responsavel> responsaveis;

	private boolean desabilitaOperacao;

	@ManagedProperty("#{responsavelService}")
	private ResponsavelService responsavelService;

	@ManagedProperty("#{parametroRepasseService}")
	private ParametroRepasseService parametroRepasseService;

	@ManagedProperty("#{unidadeService}")
	private UnidadeService unidadeService;

	private ResponsavelController responsavelController;

	@PostConstruct
	public void init() {
		setDesabilitaOperacao(true);
		responsavelBusca = responsavelEdite = responsavelRemove = new Responsavel();
	}

	public void buscar() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("nome", responsavelBusca.getNome());
		params.put("matriculaFuncional",
				responsavelBusca.getMatriculaFuncional());
		params.put("rg", responsavelBusca.getRg());
		params.put("cpf",
				UtilsModel.convertFormatCPF(responsavelBusca.getCpf()));
		params.put("cargo", responsavelBusca.getCargo());
		responsaveis = responsavelService.buscar(params);
	}

	public List<Responsavel> buscar(Map<String, Object> params) {
		return responsavelService.buscar(params);
	}

	public void remover() {

		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("responsavel.id", responsavelRemove.getId());
			List<Unidade> unidades = unidadeService.buscar(params);
			if (!unidades.isEmpty()) {
				addErrorMessage("O Responsável está associado a uma Unidade Física e não pode ser removido.");
				return;
			}
			responsavelService.remover(responsavelRemove);
		} catch (Exception e) {
			if (e.getMessage().contains("duplicate key")) {
				EntityManager entityManager = JPAPersistence.getEntityManager();
				entityManager.getTransaction().begin();
				entityManager.remove(responsavelRemove);
				entityManager.getTransaction().commit();
			} else
				addErrorMessage(String.format(
						"%s \nConsulte o Suporte Técnico: %s",
						ResultMessages.ERROR_CRUD.getDescricao(),
						e.getMessage()));
		}
		buscar();
		addInfoMessage(ResultMessages.DELETE_SUCESS.getDescricao());
	}

	public void editar(ActionEvent actionEvent) {

		try {
			responsavelEdite.setCpf(UtilsModel
					.convertFormatCPF(responsavelEdite.getCpf()));
			if (!ValidateUtils.isValidCPF(responsavelEdite.getCpf())) {
				addErrorMessage(ResultMessages.INVALID_CPF.getDescricao());
				return;
			} else {
				List<ParametroRepasse> buscarTodos = parametroRepasseService
						.buscarTodos();
				if (buscarTodos != null
						&& !buscarTodos.isEmpty()
						&& !responsavelEdite
								.getCodigoBanco()
								.trim()
								.equals(buscarTodos.get(0).getCodBanco().trim())) {
					ParametroRepasse parametroRepasse = buscarTodos.get(0);
					addErrorMessage(String.format("%s%s - %s",
							ResultMessages.INVALID_COD_BANK.getDescricao(),
							parametroRepasse.getCodBanco(),
							parametroRepasse.getDescricaoBanco()));
					return;
				}
			}
			responsavelService.atualizar(responsavelEdite);
			addInfoMessage(ResultMessages.UPDATE_SUCESS.getDescricao());
		} catch (Exception e) {
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao());
			return;
		}
		loadToFind();
	}

	public void limparFiltroBusca() {
		responsavelBusca = new Responsavel();

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

		if (usuarioSemPermissao("GESTOR")) {

			RequestContext.getCurrentInstance().update(
					"responsavelTabView:buscarResponsavelForm");
			return;
		}

		getEditTab().setRendered(true);
		TabView parent = (TabView) getEditTab().getParent();
		int editIndex = parent == null ? 1 : parent.getChildren().indexOf(
				getEditTab());
		parent.setActiveIndex(editIndex);

		FacesContext currentInstance = FacesContext.getCurrentInstance();
		responsavelController = (ResponsavelController) currentInstance
				.getELContext()
				.getELResolver()
				.getValue(currentInstance.getELContext(), null,
						"responsavelController");

		responsavelController.setResponsaveis(Arrays.asList(responsavelEdite));
		// Ajax.update("responsavelTabView");
		RequestContext.getCurrentInstance().update("responsavelTabView");
		// RequestContext.getCurrentInstance().update(
		// ":responsavelTabView:updateResponsavelForm");

	}

	public TipoContaEnum[] tipoContas() {
		return TipoContaEnum.values();
	}

	public void desabilitarOperacao() {
		if (responsavelEdite != null && responsavelEdite.getTipoConta() != null)
			if (TipoContaEnum.POUPANCA.equals(responsavelEdite.getTipoConta()))
				setDesabilitaOperacao(true);
			else
				setDesabilitaOperacao(false);
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public boolean getDesabilitaOperacao() {
		return desabilitaOperacao;
	}

	public void setDesabilitaOperacao(boolean desabilitaOperacao) {
		this.desabilitaOperacao = desabilitaOperacao;
	}

	public String getMatriculaFuncional() {
		return matriculaFuncional;
	}

	public void setMatriculaFuncional(String matriculaFuncional) {
		this.matriculaFuncional = matriculaFuncional;
	}

	public Responsavel getResponsavelRemove() {
		return responsavelRemove;
	}

	public void setResponsavelRemove(Responsavel responsavelRemove) {
		this.responsavelRemove = responsavelRemove;
	}

	public Responsavel getResponsavelEdite() {
		return responsavelEdite;
	}

	public void setResponsavelEdite(Responsavel responsavelEdite) {
		this.responsavelEdite = responsavelEdite;
	}

	public List<Responsavel> getResponsaveis() {
		return responsaveis;
	}

	public void setResponsaveis(List<Responsavel> responsaveis) {
		this.responsaveis = responsaveis;
	}

	public ResponsavelService getResponsavelService() {
		return responsavelService;
	}

	public void setResponsavelService(ResponsavelService responsavelService) {
		this.responsavelService = responsavelService;
	}

	public ParametroRepasseService getParametroRepasseService() {
		return parametroRepasseService;
	}

	public void setParametroRepasseService(
			ParametroRepasseService parametroRepasseService) {
		this.parametroRepasseService = parametroRepasseService;
	}

	public UnidadeService getUnidadeService() {
		return unidadeService;
	}

	public void setUnidadeService(UnidadeService unidadeService) {
		this.unidadeService = unidadeService;
	}

	public Responsavel getResponsavelBusca() {
		return responsavelBusca;
	}

	public void setResponsavelBusca(Responsavel responsavelBusca) {
		this.responsavelBusca = responsavelBusca;
	}

}
