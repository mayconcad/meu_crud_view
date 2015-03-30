package br.com.sts.ddum.view.controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

@ManagedBean(name = "captchaBean")
@SessionScoped
public class CaptchaBean {
	public void checar(ActionEvent e) {
		// FacesContext.getCurrentInstance().addMessage(null,
		// new FacesMessage(FacesMessage.SEVERITY_INFO, null, null));
		// "Seu Código está correto!"
	}
}
