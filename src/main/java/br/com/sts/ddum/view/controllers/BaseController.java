package br.com.sts.ddum.view.controllers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.omnifaces.util.Ajax;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;

import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.view.utils.UtilsView;

public class BaseController implements Serializable {

	private static final long serialVersionUID = 891153748337320506L;

	protected static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
	protected static final String CONEXAO_DEFAULT_RFISCAL = "jdbc:postgresql://127.0.0.1:5432/rfiscal";
	protected static final String SENHA = "postgres";
	protected static final String USUARIO = "postgres";

	protected Connection conexaoBanco = null;
	protected Statement createStatement = null;
	protected ResultSet result;

	private Tab editTab;
	private Tab findTab;

	public void closeEditTab(ActionEvent actionEvent) {
		if (getEditTab() != null) {
			getEditTab().setRendered(false);
			TabView parent = (TabView) getEditTab().getParent();
			int editIndex = parent.getChildren().indexOf(getEditTab());
			// parent.getChildren().remove(editIndex);
			parent.setActiveIndex(editIndex - 1);
			Ajax.update(parent.getId());
		}
	}

	public Tab getEditTab() {
		return editTab;
	}

	public void setEditTab(Tab editTab) {
		this.editTab = editTab;
	}

	public Tab getFindTab() {
		return findTab;
	}

	public void setFindTab(Tab findTab) {
		this.findTab = findTab;
	}

	protected void addErrorMessage(String componentId, String errorMessage) {
		addMessage(componentId, errorMessage, FacesMessage.SEVERITY_ERROR);
	}

	protected void addErrorMessage(String errorMessage) {
		addErrorMessage(null, errorMessage);
	}

	protected void addInfoMessage(String componentId, String infoMessage) {
		addMessage(componentId, infoMessage, FacesMessage.SEVERITY_INFO);
	}

	protected void addInfoMessage(String infoMessage) {
		addInfoMessage(null, infoMessage);
	}

	protected boolean usuarioSemPermissao() {
		LoginBean controllerInstance = UtilsView
				.getControllerInstance(LoginBean.class);
		if (!controllerInstance.getPrincipalRole().equals("ADMIN")) {
			addErrorMessage(ResultMessages.ERROR_ONLY_ADMIN_OPERATION
					.getDescricao());
			return true;
		}
		return false;
	}

	protected boolean usuarioSemPermissao(String role) {
		if (role != null) {
			LoginBean controllerInstance = UtilsView
					.getControllerInstance(LoginBean.class);
			if (!controllerInstance.getPrincipalRole().equals("ADMIN")
					&& !controllerInstance.getPrincipalRole().equals(role)) {
				addErrorMessage(String.format(String.format("%s",
						ResultMessages.ERROR_ONLY_ADMIN_AND_ROLE_OPERATION
								.getDescricao()), !"".equals(role) ? role : ""));
				return true;
			}
		}
		return false;
	}

	public boolean verificarPermissao() {

		if (usuarioSemPermissao())
			return false;

		RequestContext.getCurrentInstance().execute(
				"PF('confirmation').show();");
		return true;
	}

	private void addMessage(String componentId, String errorMessage,
			Severity severity) {
		FacesMessage message = new FacesMessage(errorMessage);
		message.setSeverity(severity);
		FacesContext.getCurrentInstance().addMessage(componentId, message);
	}
}