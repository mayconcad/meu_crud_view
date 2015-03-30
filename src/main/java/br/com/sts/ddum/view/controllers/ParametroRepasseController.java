package br.com.sts.ddum.view.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.omnifaces.util.Ajax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.entities.ParametroRepasse;
import br.com.sts.ddum.model.entities.SegmentoEnum;
import br.com.sts.ddum.model.enums.QueryEnum;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.ConnectionConfigService;
import br.com.sts.ddum.service.interfaces.ParametroRepasseService;

@Controller
@ViewScoped
public class ParametroRepasseController extends BaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4806821894303435065L;

	@Autowired
	private ParametroRepasseService parametroRepasseService;

	@Autowired
	private ConnectionConfigService connectionConfigService;

	private ParametroRepasse parametroRepasse = new ParametroRepasse();

	// o atributo referenciado na visão deve estar com máscara R$
	// ##.###,##
	private String valorRepasse;

	@PostConstruct
	public void init() {
		valorRepasse = new String();
		parametroRepasse = new ParametroRepasse();
		conexaoBanco = connectionConfigService.obterConexaoBancoCGP();
		// .obterConexaoBanco(POSTGRESQL_DRIVER,
		// "jdbc:postgresql://67.23.240.183:5433/CGPPHB15",
		// USUARIO, SENHA);
		Ajax.update(":parametroRepasseTabView");

	}

	public void criar() {

		try {
			createStatement = conexaoBanco.createStatement();

			String query = String.format(
					QueryEnum.ParametroRepasse.DOTACAO_ORCAMENTO_ID.toString(),
					parametroRepasse.getCodUnidade(), parametroRepasse
							.getCodAtividade(), StringUtils.rightPad(UtilsModel
							.pegarSeisCaracteres(parametroRepasse
									.getCodElementoDespesa()), 12, "0"),
					parametroRepasse.getCodFonteRecurso(), parametroRepasse
							.getExercicio());

			result = createStatement.executeQuery(query);

			while (result.next()) {
				parametroRepasse.setCodDotacao(result.getInt(1));
			}

			result.close();
			createStatement.close();

			if (parametroRepasse.getCodDotacao() == 0l) {
				addErrorMessage("A dotação informada não existe, confira os valores campos!");
				return;
			}

			parametroRepasse
					.setValorRepasse(convertStringToBigDecimal(valorRepasse));
			parametroRepasseService.salvar(parametroRepasse);

		} catch (SQLException e) {
			e.printStackTrace();
			addErrorMessage(ResultMessages.ERROR_LOAD_DOTACAO.getDescricao());
			return;
		} catch (DataIntegrityViolationException e) {
			addErrorMessage("O item já está cadastrado");
			return;
		} catch (Exception e) {
			addErrorMessage(String.format("%s \nConsulte o Suporte Técnico: %s",
					ResultMessages.ERROR_CRUD.getDescricao(),
					e.getLocalizedMessage()));
			return;
		}
		addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
		init();
	}

	private BigDecimal convertStringToBigDecimal(String valorRepasse) {
		return new BigDecimal(valorRepasse.replace("R$", "").replace(".", "")
				.replace(",", ".").trim());
	}

	public SegmentoEnum[] getSegmentos() {
		return SegmentoEnum.values();
	}

	public ParametroRepasseService getParametroRepasseService() {
		return parametroRepasseService;
	}

	public void setParametroRepasseService(
			ParametroRepasseService parametroRepasseService) {
		this.parametroRepasseService = parametroRepasseService;
	}

	public ParametroRepasse getParametroRepasse() {
		return parametroRepasse;
	}

	public void setParametroRepasse(ParametroRepasse parametroRepasse) {
		this.parametroRepasse = parametroRepasse;
	}

	public String getValorRepasse() {
		return valorRepasse;
	}

	public void setValorRepasse(String valorRepasse) {
		this.valorRepasse = valorRepasse;
	}

	public ConnectionConfigService getConnectionConfigService() {
		return connectionConfigService;
	}

	public void setConnectionConfigService(
			ConnectionConfigService connectionConfigService) {
		this.connectionConfigService = connectionConfigService;
	}
}
