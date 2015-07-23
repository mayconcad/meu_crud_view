package br.com.sts.ddum.view.controllers;

import javax.faces.bean.ViewScoped;

import org.primefaces.context.RequestContext;
import org.springframework.stereotype.Controller;

@Controller
@ViewScoped
public class IdleViewExpired {

	public void idleExpired() {
		RequestContext.getCurrentInstance().execute("PF('idleDialog').show()");
	}

}
