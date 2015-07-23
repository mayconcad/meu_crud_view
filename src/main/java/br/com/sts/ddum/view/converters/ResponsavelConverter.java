package br.com.sts.ddum.view.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.sts.ddum.model.entities.BaseEntity;
import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.view.controllers.ResponsavelController;

@FacesConverter(value = "responsavelConverter")
public class ResponsavelConverter implements Converter {

	private Responsavel object;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {

		if (value != null && !"".equals(value) && !"null".equals(value)) {
			ResponsavelController baseController = (ResponsavelController) context
					.getELContext()
					.getELResolver()
					.getValue(context.getELContext(), null,
							"responsavelController");
			try {
				object = baseController.getResponsavelById(Long
						.parseLong(value));
			} catch (NumberFormatException e) {
				return value == null ? "" : value;
			}
		}
		return object == null ? "" : object;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value != null)
			try {
				Long id = ((BaseEntity) value).getId();
				return id == null ? "" : String.valueOf(id);
			} catch (NumberFormatException e) {
				return ((String) value);
			}
		else
			return null;
	}

}
