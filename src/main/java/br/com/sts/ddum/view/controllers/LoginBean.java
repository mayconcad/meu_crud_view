package br.com.sts.ddum.view.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.service.interfaces.AuthenticationService;
import br.com.sts.ddum.service.interfaces.LiquidacaoService;
import br.com.sts.ddum.service.interfaces.UserService;
import br.com.sts.ddum.view.utils.UtilsView;

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

	@ManagedProperty(value = "#{liquidacaoServiceJob}")
	LiquidacaoService liquidacaoServiceJob;

	// @PostConstruct
	// public void init() {
	// try {
	// LiquidacaoJobController controllerInstance = UtilsView
	// .getControllerInstance(LiquidacaoJobController.class);
	// controllerInstance.agendar();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public void login() {
		CaptchaBean controllerInstance = UtilsView
				.getControllerInstance(CaptchaBean.class);
		if (!controllerInstance.validarCaptcha())
			return;
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
			context.getExternalContext().redirect(url + "/pages/index.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
		// return "sucessoLogin";
	}

	public void logout() throws IOException {
		authenticationService.logout();

		limparControllers();

		FacesContext context = FacesContext.getCurrentInstance();
		String url = context.getExternalContext().getRequestContextPath();

		// SecurityContextHolder.getContext().setAuthentication(null);
		// SecurityContextHolder.clearContext();
		context.getExternalContext().redirect(url + "/pages/index.xhtml");
		// return "login";
	}

	private void limparControllers() {
		UserController userController = UtilsView
				.getControllerInstance(UserController.class);
		userController.init();

		BuscarUserController buscarUserController = UtilsView
				.getControllerInstance(BuscarUserController.class);
		buscarUserController.init();

		ParametroRepasseController parametroRepasseController = UtilsView
				.getControllerInstance(ParametroRepasseController.class);
		parametroRepasseController.init();

		BuscarParametroRepasseController buscarParametroRepasseController = UtilsView
				.getControllerInstance(BuscarParametroRepasseController.class);
		buscarParametroRepasseController.init();

		UnidadeController unidadeController = UtilsView
				.getControllerInstance(UnidadeController.class);
		unidadeController.init();

		BuscarUnidadeController buscarUnidadeController = UtilsView
				.getControllerInstance(BuscarUnidadeController.class);
		buscarUnidadeController.init();

		BuscarResponsavelController buscarResponsavelController = UtilsView
				.getControllerInstance(BuscarResponsavelController.class);
		buscarResponsavelController.init();

		ResponsavelController responsavelController = UtilsView
				.getControllerInstance(ResponsavelController.class);
		responsavelController.init();

	}

	public User getCurrentUser() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", ((User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal()).getUsername());
		List<User> list = userServiceImpl.buscar(params);
		return list == null ? new User() : list.get(0);
	}

	public String getPrincipalRole() {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		User user = (User) auth.getPrincipal();
		return user.getRoles().iterator().next().getName();
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

	public LiquidacaoService getLiquidacaoServiceJob() {
		return liquidacaoServiceJob;
	}

	public void setLiquidacaoServiceJob(LiquidacaoService liquidacaoServiceJob) {
		this.liquidacaoServiceJob = liquidacaoServiceJob;
	}

}