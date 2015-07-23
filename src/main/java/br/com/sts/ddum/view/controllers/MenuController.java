package br.com.sts.ddum.view.controllers;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import br.com.sts.ddum.view.utils.UtilsView;

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

		BuscarParametroRepasseController parametroRepasseController = UtilsView
				.getControllerInstance(BuscarParametroRepasseController.class);
		if (parametroRepasseController != null)
			parametroRepasseController.init();

	}

	public void responsavel() {
		setNumIndex(3);
		setIndex("/pages/responsavel/responsavel.xhtml");

		BuscarResponsavelController buscarResponsavelController = UtilsView
				.getControllerInstance(BuscarResponsavelController.class);
		if (buscarResponsavelController != null)
			buscarResponsavelController.init();
	}

	public void unidade() {
		setNumIndex(4);
		setIndex("/pages/unidade/unidade.xhtml");

		BuscarUnidadeController buscarUnidadeController = UtilsView
				.getControllerInstance(BuscarUnidadeController.class);
		if (buscarUnidadeController != null)
			buscarUnidadeController.init();
	}

	public void usuario() {
		setNumIndex(5);
		setIndex("/pages/usuario/usuario.xhtml");

		BuscarUserController buscarUserController = UtilsView
				.getControllerInstance(BuscarUserController.class);
		if (buscarUserController != null)
			buscarUserController.init();

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

	public void feriado() {
		setNumIndex(11);
		setIndex("/pages/feriado/feriado.xhtml");
	}

	public void auditoria() {
		setNumIndex(12);
		setIndex("/pages/auditoria/auditoria.xhtml");
	}

	public void guiaEmpenhoReport() {
		setNumIndex(13);
		setIndex("/pages/reports/guiaEmpenho/report.xhtml");
	}

	public void style() {
		setNumIndex(14);
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