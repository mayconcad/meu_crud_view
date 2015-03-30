package br.com.sts.ddum.view.controllers;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.service.interfaces.UserService;

@Named
@Scope("request")
public class LoginController extends BaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8504219296841875713L;

	private String userName;

	private String password;

	private boolean error = false;

	@Inject
	private UserService userService;

	@Inject
	private AuthenticationManager authenticationManager;

	// @PostConstruct
	// public void init() {
	// password = userName = new String();
	// }

	public String login() {
		try {
			Authentication request = new UsernamePasswordAuthenticationToken(
					getUserName(), getPassword());

			Authentication result = authenticationManager.authenticate(request);
			SecurityContextHolder.getContext().setAuthentication(result);
		} catch (AuthenticationException e) {
			// UserDetails loadUserByUsername = userService
			// .loadUserByUsername(getUserName());
			// if (loadUserByUsername != null)
			// return "/pages/index?faces-redirect=true";
			this.error = true;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Usuário ou senha incorretos!",
							"Usuário ou senha incorretos!"));
			return null;
		}
		return "/pages/index?faces-redirect=true";
	}

	public void logout(boolean redirect) throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		String url = context.getExternalContext().getRequestContextPath();

		SecurityContextHolder.getContext().setAuthentication(null);
		SecurityContextHolder.clearContext();
		if (redirect)
			context.getExternalContext().redirect(url + "/pages/index.xhtml");
	}

	public String getUserName() {

		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		return userName == null ? auth == null
				|| auth.getName().equals("anonymousUser") ? null : auth
				.getName() : userName;
	}

	public String getPrincipalRole() {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		User user = (User) auth.getPrincipal();
		return user.getRoles().iterator().next().getName();
	}

	public User getCurrentUser() {
		return (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
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

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

}
