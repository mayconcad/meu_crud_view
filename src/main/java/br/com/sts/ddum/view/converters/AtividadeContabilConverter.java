package br.com.sts.ddum.view.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.sts.ddum.domain.entities.AtividadeContabil;
import br.com.sts.ddum.domain.entities.BaseEntity;
import br.com.sts.ddum.view.controllers.UnidadeController;

@FacesConverter(value = "atividadeContabilConverter")
public class AtividadeContabilConverter implements Converter {

	private AtividadeContabil object;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {

		if (value != null && !"".equals(value) && !"null".equals(value)) {
			UnidadeController baseController = (UnidadeController) context
					.getELContext()
					.getELResolver()
					.getValue(context.getELContext(), null, "unidadeController");
			try {
				object = baseController.getAtividadeContabilById(Long
						.parseLong(value));
			} catch (NumberFormatException e) {
				return "";
			}
		}
		return object == null ? "" : object;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value != null)
			return String.valueOf(((BaseEntity) value).getId());
		else
			return null;
	}

}
