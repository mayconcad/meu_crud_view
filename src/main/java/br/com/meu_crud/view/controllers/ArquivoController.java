package br.com.meu_crud.view.controllers;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.springframework.stereotype.Controller;

import br.com.meu_crud.model.entities.Arquivo;
import br.com.meu_crud.service.interfaces.ArquivoService;

@ViewScoped
@Controller
public class ArquivoController extends BaseController {

	private static final long serialVersionUID = 7353097944080991591L;

	private Arquivo arquivo;

	@ManagedProperty("#{arquivoService}")
	private ArquivoService arquivoService;

	@PostConstruct
	public void init() {
		arquivo = new Arquivo();
	}

	public void criar() {
		arquivoService.salvar(arquivo);
		addInfoMessage("Cadastro Realizado com Sucesso!");
	}

	public Arquivo getArquivo() {
		return arquivo;
	}

	public void setArquivo(Arquivo arquivo) {
		this.arquivo = arquivo;
	}

	public ArquivoService getArquivoService() {
		return arquivoService;
	}

	public void setArquivoService(ArquivoService arquivoService) {
		this.arquivoService = arquivoService;
	}
}
