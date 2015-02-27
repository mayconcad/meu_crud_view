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

import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;

import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.enums.TipoContaEnum;
import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.ResponsavelService;
import br.com.sts.ddum.view.utils.UtilsView;
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

	@ManagedProperty("#{responsavelService}")
	private ResponsavelService responsavelService;

	private ResponsavelController responsavelController;

	@PostConstruct
	public void init() {
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

	public void remover() {
		try {
			responsavelService.remover(responsavelRemove);
		} catch (Exception e) {
			addErrorMessage(String.format("%s \nConsulte o Analista: %s",
					ResultMessages.ERROR_CRUD.getDescricao(), e.getMessage()));
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
			} else if (!"001".equals(responsavelEdite.getCodigoBanco().trim())) {
				addErrorMessage("Código do Banco deve ser 001 - Banco do Brasil!");
				return;
			}
			responsavelService.atualizar(responsavelEdite);
			addInfoMessage(ResultMessages.UPDATE_SUCESS.getDescricao());
		} catch (Exception e) {
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao());
			return;
		}
		loadToFind();
	}

	public void loadToFind() {
		getEditTab().setRendered(false);
		TabView parent = (TabView) getFindTab().getParent();
		int findIndex = parent.getChildren().indexOf(getFindTab());
		parent.setActiveIndex(findIndex);
	}

	public void carregar() {
		getEditTab().setRendered(false);
		TabView parent = (TabView) getEditTab().getParent();
		parent.setActiveIndex(1);
		LoginBean controllerInstance = UtilsView
				.getControllerInstance(LoginBean.class);

		User currentUser = controllerInstance.getCurrentUser();

		if (responsavelEdite.getUser() == null
				|| (responsavelEdite.getUser() != null
						&& responsavelEdite.getUser().getId().intValue() != currentUser
								.getId().intValue() && !controllerInstance
						.getPrincipalRole().equals("ADMIN"))) {
			addErrorMessage(ResultMessages.PermissionsMessage.PERMISSION_NOT_EDIT
					.permissaoUsuario(currentUser.getUsername()));
			RequestContext.getCurrentInstance().update(
					"responsavelTabView:buscarResponsavelForm");

			return;
		}
		getEditTab().setRendered(true);
		int editIndex = parent.getChildren().indexOf(getEditTab());
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

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
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

	public Responsavel getResponsavelBusca() {
		return responsavelBusca;
	}

	public void setResponsavelBusca(Responsavel responsavelBusca) {
		this.responsavelBusca = responsavelBusca;
	}

}
