package br.com.meu_crud.view.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.meu_crud.model.entities.Usuario;
import br.com.meu_crud.service.interfaces.AuthenticationService;
import br.com.meu_crud.service.interfaces.UserService;

@ManagedBean
@RequestScoped
// @SessionScoped
public class LoginBean extends BaseController {

	private static final long serialVersionUID = 4661688222410469654L;

	@ManagedProperty(value = "#{authenticationService}")
	private AuthenticationService authenticationService;

	@ManagedProperty(value = "#{userServiceImpl}")
	private UserService userServiceImpl;

	private String userName;
	private String password;

	private boolean error = false;

	public void login() {

		boolean success = authenticationService.login(userName, password);
		error = false;

		FacesContext context = FacesContext.getCurrentInstance();
		String url = context.getExternalContext().getRequestContextPath();

		if (!success) {
			this.error = true;

			FacesMessage facesMessage = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "", "Login ou senha inválidos");
			FacesContext.getCurrentInstance().addMessage(null, facesMessage);
			SecurityContextHolder.getContext().setAuthentication(null);
			SecurityContextHolder.clearContext();
			return;
			// return "falhaLogin";
		}
		try {
			context.getExternalContext().redirect(url);
			// context.getExternalContext().redirect(url +
			// "/pages/index.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
		// return "sucessoLogin";
	}

	public void logoutLogin(ActionEvent actionEvent) {
		authenticationService.logout();
	}

	public void logout() throws IOException {
		authenticationService.logout();

		FacesContext context = FacesContext.getCurrentInstance();
		String url = context.getExternalContext().getRequestContextPath();

		context.getExternalContext().redirect(url + "/login.xhtml");
	}

	public void logout(boolean redirect) throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		String url = context.getExternalContext().getRequestContextPath();

		SecurityContextHolder.getContext().setAuthentication(null);
		SecurityContextHolder.clearContext();
		if (redirect)
			context.getExternalContext().redirect(url + "/login.xhtml");
	}

	public void recuperarSenha() {
		FacesContext context = FacesContext.getCurrentInstance();
		String url = context.getExternalContext().getRequestContextPath();

		try {

			context.getExternalContext().redirect(url + "/recuperaSenha.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Usuario getCurrentUser() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", ((Usuario) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal()).getUsername());
		List<Usuario> list = userServiceImpl.buscar(params);
		return list == null ? new Usuario() : list.get(0);
	}

	public String getUserName() {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		return userName == null ? auth == null
				|| auth.getName().equals("anonymousUser") ? null : auth
				.getName() : userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public UserService getUserServiceImpl() {
		return userServiceImpl;
	}

	public void setUserServiceImpl(UserService userServiceImpl) {
		this.userServiceImpl = userServiceImpl;
	}
}