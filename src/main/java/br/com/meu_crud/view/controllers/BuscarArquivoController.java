package br.com.meu_crud.view.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;

import org.primefaces.component.tabview.TabView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.meu_crud.model.entities.Arquivo;
import br.com.meu_crud.service.interfaces.ArquivoService;

@ViewScoped
@Controller
public class BuscarArquivoController extends BaseController {

	private static final long serialVersionUID = 7353097944080991591L;

	private Arquivo arquivoBusca;

	private Arquivo arquivoEdita;

	private Arquivo arquivoRemove;

	private List<Arquivo> arquivos;

	@Autowired
	private ArquivoService arquivoService;

	@PostConstruct
	public void init() {
		arquivos = new ArrayList<Arquivo>();
		arquivoBusca = new Arquivo();
	}

	public void buscar() {
		Map<String, Object> params = new HashMap<String, Object>();
		arquivos = arquivoService.buscar(params);
	}

	public void editar() {

		arquivoService.atualizar(arquivoEdita);
		addInfoMessage("Atualização Realizada com Sucesso!");
		loadToFind();
	}

	public void remover() {
		arquivoService.remover(arquivoRemove);
		addInfoMessage("Remoção Realizada com Sucesso!");
	}

	public void carregar() {

		getEditTab().setRendered(true);
		TabView parent = (TabView) getEditTab().getParent();
		int editIndex = parent == null ? 1 : parent.getChildren().indexOf(
				getEditTab());
		parent.setActiveIndex(editIndex);
	}

	public void loadToFind() {
		getEditTab().setRendered(false);
		TabView parent = (TabView) getFindTab().getParent();
		int findIndex = parent.getChildren().indexOf(getFindTab());
		parent.setActiveIndex(findIndex);
	}

	public ArquivoService getArquivoService() {
		return arquivoService;
	}

	public void setArquivoService(ArquivoService arquivoService) {
		this.arquivoService = arquivoService;
	}

	public Arquivo getArquivoBusca() {
		return arquivoBusca;
	}

	public void setArquivoBusca(Arquivo arquivoBusca) {
		this.arquivoBusca = arquivoBusca;
	}

	public Arquivo getArquivoEdita() {
		return arquivoEdita;
	}

	public void setArquivoEdita(Arquivo arquivoEdita) {
		this.arquivoEdita = arquivoEdita;
	}

	public Arquivo getArquivoRemove() {
		return arquivoRemove;
	}

	public void setArquivoRemove(Arquivo arquivoRemove) {
		this.arquivoRemove = arquivoRemove;
	}

	public List<Arquivo> getArquivos() {
		return arquivos;
	}

	public void setArquivos(List<Arquivo> arquivos) {
		this.arquivos = arquivos;
	}
}