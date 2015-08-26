package br.com.meu_crud.view.controllers;

import javax.faces.bean.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.meu_crud.model.entities.Arquivo;
import br.com.meu_crud.service.interfaces.ArquivoService;

@ViewScoped
@Controller
public class ArquivoController extends BaseController {

	private static final long serialVersionUID = 7353097944080991591L;

	private Arquivo arquivo;

	@Autowired
	private ArquivoService arquivoService;

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

}
