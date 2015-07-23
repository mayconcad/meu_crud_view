package br.com.sts.ddum.view.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.tabview.TabView;

import br.com.sts.ddum.model.entities.ParametroRepasse;
import br.com.sts.ddum.model.entities.SegmentoEnum;
import br.com.sts.ddum.model.enums.QueryEnum;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.ConnectionConfigService;
import br.com.sts.ddum.service.interfaces.ParametroRepasseService;
import br.com.sts.ddum.view.utils.UtilsView;

@ManagedBean
@ViewScoped
public class BuscarParametroRepasseController extends BaseController {

	private static final long serialVersionUID = 2883233373923310796L;

	private String valorRepasseEdite;

	private String valorRepasseBusca;

	private ParametroRepasse parametroRepasseBusca;

	private ParametroRepasse parametroRepasseRemove;

	private ParametroRepasse parametroRepasseEdite;

	private List<ParametroRepasse> parametrosRepasse = new ArrayList<ParametroRepasse>();

	@ManagedProperty("#{parametroRepasseService}")
	private ParametroRepasseService parametroRepasseService;

	// @Autowired
	@ManagedProperty("#{connectionConfigService}")
	private ConnectionConfigService connectionConfigService;

	@PostConstruct
	public void init() {
		conexaoBanco = connectionConfigService.obterConexaoBancoCGP();
		// .obterConexaoBanco(POSTGRESQL_DRIVER,
		// "jdbc:postgresql://67.23.240.183:5433/CGPPHB15",
		// USUARIO, SENHA);
		parametroRepasseBusca = parametroRepasseEdite = parametroRepasseRemove = new ParametroRepasse();
	}

	public void buscar() {
		parametrosRepasse.clear();
		LoginBean controllerInstance = UtilsView
				.getControllerInstance(LoginBean.class);
		if (controllerInstance.getPrincipalRole().equals("AUDITOR"))
			parametroRepasseBusca.setExercicio(2015);

		Map<String, Object> params = new HashMap<String, Object>();
		if (parametroRepasseBusca.getDescricao() != null
				&& !"".equals(parametroRepasseBusca.getDescricao()))
			params.put("descricao", parametroRepasseBusca.getDescricao());
		if (parametroRepasseBusca.getSegmento() != null
				&& !"".equals(parametroRepasseBusca.getSegmento()))
			params.put("segmento", parametroRepasseBusca.getSegmento());
		if (valorRepasseBusca != null && !"".equals(valorRepasseBusca))
			params.put("valorRepasse",
					convertStringToBigDecimal(valorRepasseBusca));
		if (parametroRepasseBusca.getCodUnidade() != null
				&& !"".equals(parametroRepasseBusca.getCodUnidade()))
			params.put("codUnidade", parametroRepasseBusca.getCodUnidade());
		if (parametroRepasseBusca.getCodAtividade() != null
				&& !"".equals(parametroRepasseBusca.getCodAtividade()))
			params.put("codAtividade", parametroRepasseBusca.getCodAtividade());
		if (parametroRepasseBusca.getCodElementoDespesa() != null
				&& !"".equals(parametroRepasseBusca.getCodElementoDespesa()))
			params.put("codElementoDespesa",
					parametroRepasseBusca.getCodElementoDespesa());
		if (parametroRepasseBusca.getCodFonteRecurso() != null
				&& !"".equals(parametroRepasseBusca.getCodFonteRecurso()))
			params.put("codFonteRecurso",
					parametroRepasseBusca.getCodFonteRecurso());
		if (parametroRepasseBusca.getExercicio() != 0)
			params.put("exercicio", parametroRepasseBusca.getExercicio());
		parametrosRepasse = parametroRepasseService.buscar(params);
	}

	private BigDecimal convertStringToBigDecimal(String valorRepasse) {
		return new BigDecimal(valorRepasse.replace("R$", "").replace(".", "")
				.replace(",", ".").trim());
	}

	private String convertBigDecimalToString(BigDecimal valorRepasse) {
		return valorRepasse.toString().replace(".", ",").trim();
	}

	public void remover() {
		try {
			if (usuarioSemPermissao())
				return;
			parametroRepasseService.remover(parametroRepasseRemove);
		} catch (Exception e) {
			addErrorMessage(String.format(
					"%s \nConsulte o Suporte Técnico: %s",
					ResultMessages.ERROR_CRUD.getDescricao(), e.getMessage()));
			return;
		}
		buscar();
		addInfoMessage(ResultMessages.DELETE_SUCESS.getDescricao());
	}

	public void editar(ActionEvent actionEvent) {

		if (usuarioSemPermissao())
			return;
		try {
			createStatement = conexaoBanco.createStatement();

			String query = String.format(
					QueryEnum.ParametroRepasse.DOTACAO_ORCAMENTO_ID.toString(),
					parametroRepasseEdite.getCodUnidade(),
					parametroRepasseEdite.getCodAtividade(),
					StringUtils.rightPad(UtilsModel
							.pegarSeisCaracteres(parametroRepasseEdite
									.getCodElementoDespesa()), 12, "0"),
					parametroRepasseEdite.getCodFonteRecurso(),
					parametroRepasseEdite.getExercicio());

			result = createStatement.executeQuery(query);

			while (result.next()) {
				parametroRepasseEdite.setCodDotacao(result.getInt(1));
			}

			result.close();
			createStatement.close();

			if (parametroRepasseEdite.getCodDotacao() == 0l) {
				addErrorMessage("A dotação informada não existe, confira os valores campos!");
				return;
			}

			parametroRepasseEdite
					.setValorRepasse(convertStringToBigDecimal(getValorRepasseEdite()));
			parametroRepasseService.atualizar(parametroRepasseEdite);
			addInfoMessage(ResultMessages.UPDATE_SUCESS.getDescricao());
		} catch (Exception e) {
			addErrorMessage(String.format(
					"%s \nConsulte o Suporte Técnico: %s",
					ResultMessages.ERROR_CRUD.getDescricao(),
					e.getLocalizedMessage()));
		}

		loadToFind();
	}

	public void limparFiltroBusca() {
		parametroRepasseBusca = new ParametroRepasse();
		valorRepasseBusca = new String();
	}

	public SegmentoEnum[] getSegmentos() {
		return SegmentoEnum.values();
	}

	public void loadToFind() {
		getEditTab().setRendered(false);
		TabView parent = (TabView) getFindTab().getParent();
		int findIndex = parent.getChildren().indexOf(getFindTab());
		parent.setActiveIndex(findIndex);
	}

	public void carregar() {
		if (usuarioSemPermissao("AUDITOR"))
			return;

		getEditTab().setRendered(true);
		TabView parent = (TabView) getEditTab().getParent();
		int editIndex = parent.getChildren().indexOf(getEditTab());
		parent.setActiveIndex(editIndex);
		setValorRepasseEdite(convertBigDecimalToString(parametroRepasseEdite
				.getValorRepasse()));
	}

	public String getValorRepasseBusca() {
		return valorRepasseBusca;
	}

	public void setValorRepasseBusca(String valorRepasseBusca) {
		this.valorRepasseBusca = valorRepasseBusca;
	}

	public String getValorRepasseEdite() {
		return valorRepasseEdite;
	}

	public void setValorRepasseEdite(String valorRepasseEdite) {
		this.valorRepasseEdite = valorRepasseEdite;
	}

	public ParametroRepasse getParametroRepasseRemove() {
		return parametroRepasseRemove;
	}

	public void setParametroRepasseRemove(
			ParametroRepasse parametroRepasseRemove) {
		this.parametroRepasseRemove = parametroRepasseRemove;
	}

	public ParametroRepasse getParametroRepasseEdite() {
		return parametroRepasseEdite;
	}

	public void setParametroRepasseEdite(ParametroRepasse parametroRepasseEdite) {
		this.parametroRepasseEdite = parametroRepasseEdite;
	}

	public ParametroRepasseService getParametroRepasseService() {
		return parametroRepasseService;
	}

	public void setParametroRepasseService(
			ParametroRepasseService parametroRepasseService) {
		this.parametroRepasseService = parametroRepasseService;
	}

	public List<ParametroRepasse> getParametrosRepasse() {
		return parametrosRepasse;
	}

	public void setParametrosRepasse(List<ParametroRepasse> parametrosRepasse) {
		this.parametrosRepasse = parametrosRepasse;
	}

	public ParametroRepasse getParametroRepasseBusca() {
		return parametroRepasseBusca;
	}

	public void setParametroRepasseBusca(ParametroRepasse parametroRepasseBusca) {
		this.parametroRepasseBusca = parametroRepasseBusca;
	}

	public ConnectionConfigService getConnectionConfigService() {
		return connectionConfigService;
	}

	public void setConnectionConfigService(
			ConnectionConfigService connectionConfigService) {
		this.connectionConfigService = connectionConfigService;
	}
}
