package br.com.sts.ddum.view.controllers;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.service.interfaces.AuthenticationService;

@ManagedBean
@RequestScoped
public class LoginBean extends BaseController {

	private static final long serialVersionUID = 4661688222410469654L;

	@ManagedProperty(value = "#{authenticationService}")
	private AuthenticationService authenticationService;

	private String userName;
	private String password;

	private boolean error = false;

	public String login() {
		boolean success = authenticationService.login(userName, password);
		error = false;
		if (!success) {
			this.error = true;

			FacesMessage facesMessage = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "", "Login ou senha inválidos");
			FacesContext.getCurrentInstance().addMessage(null, facesMessage);
			return "falhaLogin";
		}

		return "sucessoLogin";
	}

	public void logout() throws IOException {
		authenticationService.logout();

		FacesContext context = FacesContext.getCurrentInstance();
		String url = context.getExternalContext().getRequestContextPath();

		// SecurityContextHolder.getContext().setAuthentication(null);
		// SecurityContextHolder.clearContext();
		context.getExternalContext().redirect(url + "/pages/index.xhtml");
		// return "login";
	}

	public User getCurrentUser() {
		return (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
	}

	public String getPrincipalRole() {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		User user = (User) auth.getPrincipal();
		return user.getRoles().get(0).getName();
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

}