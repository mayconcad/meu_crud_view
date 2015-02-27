package br.com.sts.ddum.view.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;

import org.primefaces.component.tabview.TabView;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.springsecurity.entities.Role;
import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.service.interfaces.AclService;
import br.com.sts.ddum.service.interfaces.UserService;
import br.com.sts.ddum.view.utils.UtilsView;

@Controller
@ViewScoped
public class BuscarUserController extends BaseController {

	private static final long serialVersionUID = 6005013660763485425L;

	@Inject
	private UserService userService;

	@ManagedProperty("#{aclServiceImpl}")
	private AclService aclService;

	private User userBusca;
	private User userRemove;
	private User userEdite;

	private Date dataInicial;
	private Date dataFinal;

	private String usernameEdite;
	private String passwordEdite;

	private Role roleEdite;

	private List<User> usuarios = new ArrayList<User>();

	@PostConstruct
	public void init() {
		usuarios = new ArrayList<User>();
		roleEdite = new Role();
		this.userBusca = this.userEdite = this.userRemove = new User();
		usernameEdite = passwordEdite = new String();
	}

	public void editar(ActionEvent actionEvent) {

		List<Role> roles = new ArrayList<Role>();
		roles.add(roleEdite);
		this.userEdite.setRoles(roles);
		this.userEdite.setUsername(getUsernameEdite());
		this.userEdite.setPassword(getPasswordEdite());
		this.userEdite.setAtivo(true);
		try {
			userService.edite(this.userEdite);
		} catch (Exception e) {
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao()
					+ e.getLocalizedMessage());
			return;
		}

		addInfoMessage(ResultMessages.UPDATE_SUCESS.getDescricao());
		loadToFind();
		this.userEdite = new User();
	}

	public void buscar() {

		usuarios.clear();
		Map<String, Object> params = new HashMap<String, Object>();
		if (userBusca.getName() != null && !"".equals(userBusca.getName()))
			params.put("name", userBusca.getName());
		if (userBusca.getUsername() != null
				&& !"".equals(userBusca.getUsername()))
			params.put("username", userBusca.getUsername());
		if (getDataInicial() != null)
			params.put("dataInicial", getDataInicial());
		if (getDataFinal() != null)
			params.put("dataFinal", getDataFinal());
		String principalRole = UtilsView.getControllerInstance(
				LoginController.class).getPrincipalRole();
		if (principalRole != null && !principalRole.equals("ADMIN"))
			params.put("principalRole",
					UtilsView.getControllerInstance(LoginController.class)
							.getPrincipalRole());
		usuarios = userService.buscar(params);
	}

	public void loadToFind() {
		getEditTab().setRendered(false);
		TabView parent = (TabView) getFindTab().getParent();
		int findIndex = parent.getChildren().indexOf(getFindTab());
		parent.setActiveIndex(findIndex);
	}

	public void carregar() {
		getEditTab().setRendered(true);
		TabView parent = (TabView) getEditTab().getParent();
		int editIndex = parent.getChildren().indexOf(getEditTab());
		parent.setActiveIndex(editIndex);
		setUsernameEdite(userEdite.getUsername());
		// setSenha(userEdite.getPassword());
		setRoleEdite(userEdite.getRoles().get(0));
	}

	public void remover() {
		userService.remove(userRemove);
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public User getUserBusca() {
		return userBusca;
	}

	public void setUserBusca(User userBusca) {
		this.userBusca = userBusca;
	}

	public User getUserRemove() {
		return userRemove;
	}

	public void setUserRemove(User userRemove) {
		this.userRemove = userRemove;
	}

	public User getUserEdite() {
		return userEdite;
	}

	public void setUserEdite(User userEdite) {
		this.userEdite = userEdite;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public List<User> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<User> usuarios) {
		this.usuarios = usuarios;
	}

	public String getUsernameEdite() {
		return usernameEdite;
	}

	public void setUsernameEdite(String username) {
		this.usernameEdite = username;
	}

	public String getPasswordEdite() {
		return passwordEdite;
	}

	public void setPasswordEdite(String password) {
		this.passwordEdite = password;
	}

	public Role getRoleEdite() {
		return roleEdite;
	}

	public void setRoleEdite(Role roleEdite) {
		this.roleEdite = roleEdite;
	}

}
