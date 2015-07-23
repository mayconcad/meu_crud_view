package br.com.sts.ddum.view.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;

import org.omnifaces.util.Ajax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.springsecurity.entities.Role;
import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.service.interfaces.ResponsavelService;
import br.com.sts.ddum.service.interfaces.RoleService;
import br.com.sts.ddum.service.interfaces.UserService;
import br.com.sts.ddum.view.utils.UtilsView;

import com.google.common.collect.Sets;

@Controller
@ViewScoped
public class UserController extends BaseController {

	private static final long serialVersionUID = 6005013660763485425L;

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private ResponsavelService responsavelService;

	private User user;

	private String login;

	private String senha;

	private boolean enableAutocomplete;

	private Role role;

	private Responsavel responsavel;

	public UserController() {
		responsavel = new Responsavel();
		role = new Role();
		user = new User();
		login = "";
		senha = "";
	}

	@PostConstruct
	public void init() {
		responsavel = new Responsavel();
		role = new Role();
		user = new User();
		login = senha = new String();
		Ajax.update(":usuarioTabView");
	}

	public void limparDados() {
		init();
		BuscarUserController controllerInstance = UtilsView
				.getControllerInstance(BuscarUserController.class);
		if (controllerInstance != null)
			controllerInstance.init();
	}

	public void save() {

		LoginBean controllerInstance = UtilsView
				.getControllerInstance(LoginBean.class);
		if (!controllerInstance.getPrincipalRole().equals("ADMIN")
				&& !controllerInstance.getPrincipalRole().equals("GESTOR")) {
			addErrorMessage(ResultMessages.ERROR_ONLY_ADMIN_AND_GESTOR_OPERATION
					.getDescricao());
			return;
		}

		boolean possuiResponsavel = responsavel != null
				&& responsavel.getId() != null && responsavel.getId() > 0;

		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		this.user.setRoles(Sets.newHashSet(roles));
		this.user.setUsername(getLogin());
		this.user.setPassword(getSenha());
		this.user.setCreatedAt(new Date());
		this.user.setAtivo(true);
		this.user.setId(null);
		if (possuiResponsavel) {
			this.user.setName(responsavel.getNome());
			List<Responsavel> responsaveis = new ArrayList<Responsavel>();
			responsaveis.add(responsavel);
			this.user.setResponsavel(responsavel);
		}
		try {
			userService.save(this.user);
		} catch (Exception e) {
			if (e.getMessage() != null
					&& e.getMessage().contains("user_username_ativo_key")) {
				addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao()
						+ " O item já existe!");
				return;
			} else if (e.getMessage() != null
					&& e.getMessage().contains("user_username_key")) {
				User userInativo = userService.loadByUsername(getLogin());
				userInativo.setUsername(getLogin());
				userInativo.setPassword(getSenha());
				userInativo.setCreatedAt(new Date());
				userInativo.setEmail(user.getEmail());
				userInativo.setName(user.getName());
				userInativo.setRoles(Sets.newHashSet(getRole()));
				userInativo.setAtivo(true);
				if (responsavel != null && responsavel.getId() != null
						&& responsavel.getId() > 0) {
					userInativo.setName(responsavel.getNome());
					this.user.setResponsavel(responsavel);
				}
				userService.edite(userInativo);
				addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
				init();
				return;
			}

			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao()
					+ e.getLocalizedMessage());
			return;
		}
		this.user = new User();
		this.responsavel = new Responsavel();
		this.role = new Role();
		login = senha = new String();

		addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
	}

	public void enableField() {
		if (role.getName() != null
				&& role.getName().trim().equals("RESPONSÁVEL"))
			enableAutocomplete = true;
		else
			enableAutocomplete = false;
	}

	public List<Role> autocompletarRole(String valor) {
		return roleService.autocompletar(valor);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public ResponsavelService getResponsavelService() {
		return responsavelService;
	}

	public void setResponsavelService(ResponsavelService responsavelService) {
		this.responsavelService = responsavelService;
	}

	public RoleService getRoleService() {
		return roleService;
	}

	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Responsavel getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(Responsavel responsavel) {
		this.responsavel = responsavel;
	}

	public boolean getEnableAutocomplete() {
		return enableAutocomplete;
	}

	public void setEnableAutocomplete(boolean enableAutocomplete) {
		this.enableAutocomplete = enableAutocomplete;
	}

}
