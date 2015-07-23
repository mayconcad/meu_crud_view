package br.com.sts.ddum.view.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.sts.ddum.model.entities.BaseEntity;
import br.com.sts.ddum.model.entities.UnidadeContabil;
import br.com.sts.ddum.view.controllers.UnidadeController;

@FacesConverter(value = "unidadeContabilConverter")
public class UnidadeContabilConverter implements Converter {

	private UnidadeContabil object;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {

		if (value != null && !"".equals(value) && !"null".equals(value)) {
			UnidadeController baseController = (UnidadeController) context
					.getELContext()
					.getELResolver()
					.getValue(context.getELContext(), null, "unidadeController");
			try {
				object = baseController.getUnidadeById(Long.parseLong(value));
			} catch (NumberFormatException e) {
				return "";
			}
		}
		return object == null ? "" : object;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value != null && !"null".equals(value) && !"".equals(value)) {
			Long id = ((BaseEntity) value).getId();
			return id == null ? "" : String.valueOf(id);
		} else
			return null;
	}

}
