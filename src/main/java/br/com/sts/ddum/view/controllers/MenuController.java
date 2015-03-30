package br.com.sts.ddum.view.controllers;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "mainMenu")
@SessionScoped
// @RequestScoped
public class MenuController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3013537122197885450L;
	private String index;
	private int numIndex;

	public MenuController() {
		this.numIndex = 0;
	}

	public void parametroModulo() {
		setNumIndex(1);
		setIndex("/pages/parametroModulo/parametroModulo.xhtml");
	}

	public void parametroRepasse() {
		setNumIndex(2);
		setIndex("/pages/parametroRepasse/parametroRepasse.xhtml");
	}

	public void responsavel() {
		setNumIndex(3);
		setIndex("/pages/responsavel/responsavel.xhtml");
	}

	public void unidade() {
		setNumIndex(4);
		setIndex("/pages/unidade/unidade.xhtml");
	}

	public void usuario() {
		setNumIndex(5);
		setIndex("/pages/usuario/usuario.xhtml");
	}

	public void gerarRepasse() {
		setNumIndex(6);
		setIndex("/pages/repasse/repasse.xhtml");
	}

	public void prestacaoConta() {
		setNumIndex(7);
		setIndex("/pages/prestacaoConta/prestacaoConta.xhtml");
	}

	public void report() {
		setNumIndex(8);
		setIndex("/pages/reports/termoCompromisso/report.xhtml");
	}

	public void legislacaoReport() {
		setNumIndex(9);
		setIndex("/pages/reports/legislacao/legislacao.xhtml");
	}

	public void pesquisaPrecoReport() {
		setNumIndex(10);
		setIndex("/pages/reports/legislacao/pesquisaPreco.xhtml");
	}

	public void style() {
		setNumIndex(11);
		setIndex("/pages/style.xhtml");
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public int getNumIndex() {
		return numIndex;
	}

	public void setNumIndex(int numIndex) {
		this.numIndex = numIndex;
	}

}
