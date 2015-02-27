package br.com.sts.ddum.view.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.WebProperties;
import br.com.sts.ddum.model.utils.ResourceBundleUtils;

@Controller
@Scope("singleton")
public class OptionsController {
	private final Map<String, SelectItem[]> options = new HashMap<String, SelectItem[]>();
	private final Map<String, List<Enum<?>>> enums = new HashMap<String, List<Enum<?>>>();

	@PostConstruct
	public void init() throws ClassNotFoundException {
		Properties properties = new Properties();
		try {
			InputStream systemResourceAsStream = Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(WebProperties.FILE_NAME);
			properties.load(systemResourceAsStream);
			loadOptions(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadOptions(Properties properties)
			throws ClassNotFoundException {
		final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);
		scanner.addIncludeFilter(new AssignableTypeFilter(Enum.class));

		String[] packages = properties.getProperty(WebProperties.ENUMS_PACKAGE,
				"").split("\n\\s*\n");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		for (String p : packages) {
			Set<BeanDefinition> beanDefinitions = scanner
					.findCandidateComponents(p);
			for (BeanDefinition beanDefinition : beanDefinitions) {
				Class<Enum> loadClass = (Class<Enum>) loader
						.loadClass(beanDefinition.getBeanClassName());
				String key = Character.toLowerCase(loadClass.getSimpleName()
						.charAt(0)) + loadClass.getSimpleName().substring(1);

				Enum[] enumConstants = loadClass.getEnumConstants();
				SelectItem[] selectItems = new SelectItem[enumConstants.length];
				for (int i = 0; i < selectItems.length; i++) {
					String description = ResourceBundleUtils
							.getLocalizedMessage(enumConstants[i].toString());
					selectItems[i] = new SelectItem(enumConstants[i],
							description);
				}
				options.put(key, selectItems);
				enums.put(key, Arrays.<Enum<?>> asList(enumConstants));
			}
		}
	}

	public Map<String, SelectItem[]> getOptions() {
		return options;
	}

	public List<Enum<?>> autocomplete(String query) {
		String enumName = (String) UIComponent
				.getCurrentComponent(FacesContext.getCurrentInstance())
				.getAttributes().get("enum");

		List<Enum<?>> result = new ArrayList<Enum<?>>();
		for (Enum<?> selectItem : enums.get(enumName)) {
			if (selectItem.toString().replace("-", "").toLowerCase()
					.contains(query.toLowerCase())) {
				result.add(selectItem);
			}
		}

		return result;
	}

}
