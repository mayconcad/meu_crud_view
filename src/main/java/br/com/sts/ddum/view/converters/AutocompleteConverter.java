package br.com.sts.ddum.view.converters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.EntityManager;
import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import br.com.sts.ddum.model.entities.BaseEntity;

@FacesConverter(value = "autocompleteConverter")
public class AutocompleteConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {

		if (value != null && !value.isEmpty() && !"null".equals(value)) {
			WebApplicationContext applicationContext = WebApplicationContextUtils
					.getWebApplicationContext((ServletContext) context
							.getExternalContext().getContext());

			EntityManager entityManager = (EntityManager) applicationContext
					.getBean(EntityManager.class);
			String entityClass = (String) component.getAttributes().get(
					"entityClass");
			Class<?> klass = null;
			if (entityClass == null)
				return "";
			try {
				klass = (Class<?>) (entityClass != null ? Thread
						.currentThread().getContextClassLoader()
						.loadClass(entityClass) : component.getAttributes()
						.get(this.getClass().getSimpleName()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				FacesContext
						.getCurrentInstance()
						.addMessage(
								null,
								new FacesMessage(
										"A propriedade 'entityClass' não foi informada ou está incorreta!!"));
				return "";
			}
			Object object = null;
			Pattern pattern = Pattern.compile("\\d");
			Matcher matcher = pattern.matcher(value);

			if (matcher.find()) {
				object = entityManager.find(klass, Long.parseLong(value));
			}
			return object == null ? "" : object;

		}
		return "";
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object object) {
		if (object != null)
			return String.valueOf(((BaseEntity) object).getId());
		else
			return null;
	}

}
