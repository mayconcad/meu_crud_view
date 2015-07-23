package br.com.sts.ddum.view.controllers;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

import br.com.sts.ddum.model.utils.UtilsModel;

@ManagedBean(name = "captchaBean")
@SessionScoped
public class CaptchaBean extends BaseController {

	private static final long serialVersionUID = 8491827861465529538L;

	private String captcha;

	private String inputCaptcha;

	@PostConstruct
	public void init() {
		inputCaptcha = new String();
		setCaptcha(UtilsModel.gerarCaptcha(8));
	}

	public void checar(ActionEvent e) {
		// FacesContext.getCurrentInstance().addMessage(null,
		// new FacesMessage(FacesMessage.SEVERITY_INFO, null, null));
		// "Seu Código está correto!"
	}

	public boolean validarCaptcha() {
		if (!getCaptcha().equals(getInputCaptcha())) {
			addErrorMessage("Os caracteres digitados não conferem! \nDigite-os corretamente.");
			setCaptcha(UtilsModel.gerarCaptcha(8));
			setInputCaptcha("");
			return false;
		}
		return true;
	}

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getInputCaptcha() {
		return inputCaptcha;
	}

	public void setInputCaptcha(String inputCaptcha) {
		this.inputCaptcha = inputCaptcha;
	}
}
