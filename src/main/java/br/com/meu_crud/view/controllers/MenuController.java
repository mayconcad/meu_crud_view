package br.com.meu_crud.view.controllers;

import java.io.Serializable;

import javax.annotation.PostConstruct;
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

	private boolean expanded;

	public MenuController() {
		this.numIndex = 0;
	}

	@PostConstruct
	public void init() {
		this.numIndex = 0;
	}

	public void arquivo() {
		setNumIndex(1);
		setIndex("/pages/arquivo/arquivo.xhtml");

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
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
}