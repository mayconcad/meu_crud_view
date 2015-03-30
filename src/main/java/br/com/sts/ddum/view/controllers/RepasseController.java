package br.com.sts.ddum.view.controllers;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.omnifaces.util.Ajax;
import org.primefaces.component.media.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.entities.Repasse;
import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.entities.Unidade;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.model.vos.ReservaDotacaoVO;
import br.com.sts.ddum.service.interfaces.ConnectionConfigService;
import br.com.sts.ddum.service.interfaces.RepasseService;
import br.com.sts.ddum.service.interfaces.ResponsavelService;

@Controller
@ViewScoped
public class RepasseController extends BaseReportController {

	private static final long serialVersionUID = -3922297442654647722L;

	private final String DE_HISTORICO = "Empenho de suprimento de fundo para atender as necessidades de manutenção de bens móveis e imóveis conforme a lei 2.928, de 18 de agosto de 2014, que instituiu o Programa Municipal Dinheiro Direto nas Unidades Municipais - PMDDU";

	@Autowired
	private RepasseService repasseService;

	@Autowired
	private ResponsavelService responsavelService;

	@Autowired
	private ConnectionConfigService connectionConfigService;

	private Unidade unidade;

	private Repasse repasse;

	private String numeroProcessoLabel;

	private Statement createStatement;
	private Connection conexaoBancoCGP;

	private Media empenhoMedia;

	@PostConstruct
	public void init() {
		empenhoMedia = new Media();
		unidade = new Unidade();
		repasse = new Repasse();
		repasse.setDataEmissao(new Date());
		setNumeroProcessoLabel("2.928");
		conexaoBancoCGP = connectionConfigService.obterConexaoBancoCGP();
		Ajax.update(":repasseTabView");
	}

	public void gerarRepasse() {
		if (true) {
			addErrorMessage("Esta funcionalidade ainda não está disponível!");
			return;
		}

		Responsavel credor = new Responsavel();
		try {
			createStatement = conexaoBancoCGP.createStatement();
			credor = obterCredor(repasse.getUnidade().getResponsavel());

		} catch (SQLException e) {
			e.printStackTrace();
			addErrorMessage("Não foi possível registrar o reponsável como credor!\n"
					+ e.getLocalizedMessage());
			return;
		}

		try {

			long proximoNumeroEmpenho = repasseService
					.obterProximoNumeroEmpenho(repasse.getUnidade()
							.getParametroRepasse().getCodUnidade());
			// int exercicio = repasse.getUnidade().getParametroRepasse()
			// .getExercicio();
			// String codConta = StringUtils.rightPad(repasse.getUnidade()
			// .getParametroRepasse().getCodElementoDespesa(), 12, "0");
			// BigDecimal valorEmpenho = getValorEmpenho(repasse.getUnidade()
			// .getParametroRepasse().getValorRepasse());
			// BigDecimal saldoDotacao =
			// calculoSaldoDotacao(repasse.getUnidade(),
			// repasse.getDataEmissao());

			// if (valorEmpenho.compareTo(saldoDotacao) > 0) {
			// addErrorMessage("Não foi possível gerar o Repasse. O valor do empenho é superior ao saldo da dotação");
			// return;
			// }

			// repasse.setExercicio(exercicio);
			// repasse.setCodAplicacao("100");
			// repasse.setDataEmissao(new Date());
			// repasse.setHistorico(DE_HISTORICO);
			// repasse.setNaturezaEmpenho(NaturezaEmpenhoEnum.EMPENHO_SUPRIMENTO_FUNDO);
			// repasse.setTipoCredito(TipoCreditoEnum.ORCAMENTARIO);
			// repasse.setTipoEmpenho(TipoEmpenhoEnum.GLOBAL);
			// repasse.setTipoMeta(TipoMetaEnum.OUTRAS);
			// repasse.setValorEmpenho(valorEmpenho);
			// repasse.setValorLiquidacao(repasse.getUnidade().getParametroRepasse()
			// .getValorRepasse());
			// repasse.setValorRepasse(repasse.getUnidade().getParametroRepasse()
			// .getValorRepasse());

			// repasse.setNumeroEmpenho(proximoNumeroEmpenho);

			// gerarEmpenho(credor, proximoNumeroEmpenho, exercicio, codConta,
			// valorEmpenho, saldoDotacao);
			//
			repasse.setNumeroProcesso(Long.parseLong(getNumeroProcessoLabel()
					.replace(".", "")));
			repasseService.repasseAutomatico(repasse, credor, repasse
					.getUnidade().getParametroRepasse().getValorRepasse(),
					proximoNumeroEmpenho, true);

			addInfoMessage(String.format(
					"Foi gerado um Empenho com o número ( %s )",
					proximoNumeroEmpenho));

			generateReportStream(repasse.getNumeroEmpenho(),
					repasse.getDataEmissao(), repasse.getExercicio(),
					repasse.getNumeroProcesso(),
					UtilsModel.convertBigDecimalToString(repasse
							.getValorEmpenho()), repasse.getTipoCredito()
							.getDescricao(), repasse.getTipoMeta()
							.getDescricao(), repasse.getTipoEmpenho()
							.getDescricao(), repasse.getNaturezaEmpenho()
							.getDescricao(), String.format("%s - %s", repasse
							.getUnidade().getParametroRepasse()
							.getCodElementoDespesa(), repasse.getUnidade()
							.getUnidadeContabil().getAtividade()
							.getContaContabil().getDescricao()), String.format(
							"%s - %s",
							repasse.getUnidade().getParametroRepasse()
									.getCodFonteRecurso(), repasse.getUnidade()
									.getUnidadeContabil().getAtividade()
									.getContaContabil().getFonteRecurso()
									.getDescricao()), String.format("%d - %s",
							credor.getCodigoCredor(), credor.getNome()),
					repasse.getCodAplicacao(), repasse.getHistorico());
			getEmpenhoMedia().setValue(
					File.separator.concat("reports").concat(
							File.separator.concat("GuiaEmpenho.pdf")));

			// credor =
			// buildUpdateCredor(repasse.getUnidade().getResponsavel());
			// Long id = credor.getId();
			// Responsavel responsavel = responsavelService.carregar(repasse
			// .getUnidade().getResponsavel().getId());
			// responsavel.setCodigoCredor(repasse.getUnidade().getResponsavel()
			// .getCodigoCredor());
			// responsavelService.atualizar(responsavel);

		} catch (IllegalArgumentException e) {
			addErrorMessage(e.getMessage());
			return;
		} catch (Exception e) {
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao());
			return;
		}
		responsavelService.salvar(repasse.getUnidade().getResponsavel());
		init();

	}

	private Responsavel buildUpdateCredor(Responsavel credor) {
		Responsavel responsavel = new Responsavel();
		responsavel.setAtivo(repasse.getUnidade().getResponsavel().isAtivo());
		responsavel
				.setBairro(repasse.getUnidade().getResponsavel().getBairro());
		responsavel.setCargo(repasse.getUnidade().getResponsavel().getCargo());
		responsavel.setCidade(repasse.getUnidade().getResponsavel().getCargo());
		responsavel.setCodigoCredor(credor.getCodigoCredor());
		responsavel.setCodigoBanco(repasse.getUnidade().getResponsavel()
				.getCodigoBanco());
		responsavel.setCpf(repasse.getUnidade().getResponsavel().getCpf());
		responsavel.setDataExpedicao(repasse.getUnidade().getResponsavel()
				.getDataExpedicao());
		responsavel.setDataInicial(repasse.getUnidade().getResponsavel()
				.getDataInicial());
		responsavel.setDataFinal(repasse.getUnidade().getResponsavel()
				.getDataFinal());
		responsavel.setDigitoAgencia(repasse.getUnidade().getResponsavel()
				.getDigitoAgencia());
		responsavel.setDigitoConta(repasse.getUnidade().getResponsavel()
				.getDigitoAgencia());
		responsavel.setEndereco(repasse.getUnidade().getResponsavel()
				.getEndereco());
		responsavel.setId(repasse.getUnidade().getResponsavel().getId());
		responsavel.setIdCargo(repasse.getUnidade().getResponsavel()
				.getCargoId());
		responsavel.setIdExterno(repasse.getUnidade().getResponsavel()
				.getExternoId());
		responsavel.setMatriculaFuncional(repasse.getUnidade().getResponsavel()
				.getMatriculaFuncional());
		responsavel.setNome(repasse.getUnidade().getResponsavel().getNome());
		responsavel.setNumeroAgencia(repasse.getUnidade().getResponsavel()
				.getNumeroAgencia());
		responsavel.setNumeroConta(repasse.getUnidade().getResponsavel()
				.getNumeroConta());
		responsavel.setOrgaoExpedidor(repasse.getUnidade().getResponsavel()
				.getOrgaoExpedidor());
		responsavel.setRg(repasse.getUnidade().getResponsavel().getRg());
		responsavel.setTelefone(repasse.getUnidade().getResponsavel()
				.getTelefone());
		responsavel.setTipoConta(repasse.getUnidade().getResponsavel()
				.getTipoConta());
		responsavel.setUf(repasse.getUnidade().getResponsavel().getUf());

		return responsavel;
	}

	private void generateReportStream(long numEmpenho, Date dataEmissao,
			int exercicio, long numProcesso, String valorEmpenho,
			String tipoCredito, String tipoMeta, String tipoEmpenho,
			String naturezaEmpenho, String elemDespesa, String fonteRecurso,
			String credor, String codAplicacao, String historico) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("numEmpenho", numEmpenho);
		parameters.put("dataEmissao", sdf.format(dataEmissao));
		parameters.put("exercicio", exercicio);
		parameters.put("numProcesso", numProcesso);
		parameters.put("valorEmpenho", valorEmpenho);
		parameters.put("tipoCredito", tipoCredito);
		parameters.put("tipoMeta", tipoMeta);
		parameters.put("tipoEmpenho", tipoEmpenho);
		parameters.put("naturezaEmpenho", naturezaEmpenho);
		parameters.put("elemDespesa", elemDespesa);
		parameters.put("fonteRecurso", fonteRecurso);
		parameters.put("credor", credor);
		parameters.put("codAplicacao",
				String.format("%s - %s", codAplicacao, "Geral"));
		parameters.put("historico", historico);
		parameters.put("local", "Parnaíba - PI");

		FacesContext context = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) context
				.getExternalContext().getContext();

		generateStremedReport(servletContext.getRealPath(File.separator.concat(
				"reports").concat(File.separator)), "GuiaEmpenho",
				new ArrayList(), parameters);

	}

	public void carregarResponsavelAtivo() {
		if (repasse.getUnidade().getResponsavel().getDataFinal() != null
				&& new Date().after(repasse.getUnidade().getResponsavel()
						.getDataFinal())) {
			addErrorMessage(ResultMessages.ResponsavelMessage.INVALID_REPONSAVEL
					.getDescricao());
			return;
		}
	}

	public void limparRelatorio() {
		getEmpenhoMedia().setValue(null);
		init();
	}

	public List<Repasse> buscar(Map<String, Object> params) {
		return repasseService.buscar(params);
	}

	private void gerarEmpenho(Responsavel credor, long proximoNumeroEmpenho,
			int exercicio, String codConta, BigDecimal valorEmpenho,
			BigDecimal saldoDotacao) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		// @formatter:off
		String query = "INSERT INTO cgp.empenho( cd_entidade, cd_orgao, cd_unidade,"
				+ " cd_atividade_projeto, nu_exercicio, nu_empenho, cd_categoria_conta,"
				+ " cd_codigo_conta, cd_tipo_meta, cd_tipo_empenho, cd_fonte_recurso,"
				+ " cd_credor, data_emissao, valor_empenho, cd_tipo_credito,"
				+ " cd_natureza_empenho, nu_processo, st_anulacao, vl_saldo_anterior,"
				+ " de_historico, rowid_orcamento, cd_interveniente, nu_reserva,"
				+ " cd_aplicacao, st_passivo, dt_registro ) VALUES("
				+ String.format(
						"%d, '%s', '%s', '%s', %d, %d, %d, '%s', %d, %d, '%s', '%s', '%s', %s, %d, %d, %d, %d, %s, '%s', %d, %d, %d, %d, %d, '%s' )",
						1, repasse.getUnidade().getUnidadeContabil()
								.getAtividade().getCodOrgao(), repasse
								.getUnidade().getParametroRepasse()
								.getCodUnidade(), repasse.getUnidade()
								.getParametroRepasse().getCodAtividade(),
						exercicio, proximoNumeroEmpenho, 4, codConta, repasse
								.getTipoMeta().OUTRAS.getNumero(), repasse
								.getTipoEmpenho().GLOBAL.getNumero(), repasse
								.getUnidade().getParametroRepasse()
								.getCodFonteRecurso(), credor.getCodigoBanco(),
						sdf.format(repasse.getDataEmissao()), UtilsModel
								.convertStringToBigDecimal(valorEmpenho
										.toString()),
						repasse.getTipoCredito().ORCAMENTARIO.getNumero(),
						repasse.getNaturezaEmpenho().EMPENHO_SUPRIMENTO_FUNDO
								.getNumero(), repasse.getNumeroProcesso(), 0,
						UtilsModel.convertStringToBigDecimal(saldoDotacao
								.toString()), DE_HISTORICO, repasse
								.getUnidade().getParametroRepasse()
								.getCodDotacao(), 0,
						/* "Não é obrigtorio" */0, 100, 0, sdf.format(repasse
								.getDataEmissao()));

		try {
			createStatement.executeUpdate(query);

			createStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			addErrorMessage("Ocorreu um erro durante a geração do Empenho! "
					+ e.getLocalizedMessage());
			return;
		}
	}

	/**
	 * @description caso o responsável passado como parâmetro não seja um credor
	 *              este deverá ser registrado como credor no banco cgp, caso
	 *              contrário este parâmetro será retornado
	 * @param responsavel
	 * @return
	 * @throws SQLException
	 */
	private Responsavel obterCredor(Responsavel responsavel)
			throws SQLException {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String queryBuscaCredor = String.format(
				"SELECT cd_credor FROM cgp.credor WHERE cd_cpfcnpj LIKE '%s'",
				responsavel.getCpf());

		result = createStatement.executeQuery(queryBuscaCredor);

		while (result.next()) {
			responsavel.setCodigoCredor(result.getLong(1));
			result.close();
			return responsavel;
		}
		String queryInseriCredor = String
				.format("INSERT INTO cgp.credor(cd_credor, nu_rg, de_orgao_rg, dt_rg, de_razao_social, cd_cpfcnpj, tp_credor, st_credor,de_endereco, de_bairro, de_cidade, cd_uf, cd_cep, de_lotacao, dt_cadastro, cd_nivel_integracao, dt_registro) VALUES((SELECT MAX(c.cd_credor) + 1 FROM cgp.credor c), '%s', '%s', '%s', %d, %d, '%s', '%s', '%s', '%s', '%s', %d, '%s')",
						responsavel.getRg(), responsavel.getOrgaoExpedidor(),
						responsavel.getDataExpedicao(), responsavel.getNome(),
						responsavel.getCpf(), 0, 0, responsavel.getEndereco(),
						responsavel.getBairro(), responsavel.getCidade(),
						responsavel.getUf(), responsavel.getCep(),
						responsavel.getLotacao(), sdf.format(new Date()), 1,
						sdf.format(new Date()));
		createStatement.executeUpdate(queryInseriCredor);

		String queryContaCredor = String
				.format("INSERT INTO cgp.conta_credor(cd_credor, cd_banco, cd_agencia, dv_agencia, cd_conta, dv_conta, cd_operacao, dt_registro) VALUES ((SELECT MAX(c.cd_credor) FROM cgp.credor c WHERE c.cd_cpfcnpj = '%s'), '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
						responsavel.getCpf(), responsavel.getCodigoBanco(),
						responsavel.getNumeroAgencia(),
						responsavel.getDigitoAgencia(),
						responsavel.getNumeroConta(),
						responsavel.getDigitoConta(),
						responsavel.getOperacao(),
						sdf.format(responsavel.getDataInicial()));
		createStatement.executeQuery(queryContaCredor);

		result = createStatement.executeQuery(queryBuscaCredor);
		while (result.next()) {
			responsavel.setCodigoCredor(result.getLong(1));
			result.close();
			return responsavel;
		}
		result.close();
		return responsavel;
	}

	private BigDecimal getValorEmpenho(BigDecimal valorRepasse) {

		Calendar date = Calendar.getInstance();
		date.setTime(new Date());

		DateTime dataAtual = new DateTime(date.get(Calendar.YEAR),
				date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH),
				date.get(Calendar.HOUR), date.get(Calendar.MINUTE),
				date.get(Calendar.SECOND), date.get(Calendar.MILLISECOND));

		DateTime dataFinal = new DateTime(date.get(Calendar.YEAR), 12, 31, 23,
				59, 59, 59);

		Months monthsBetween = Months.monthsBetween(dataAtual, dataFinal);

		return valorRepasse.multiply(new BigDecimal(
				monthsBetween.getMonths() + 1), new MathContext(2));
	}

	private BigDecimal calculoSaldoDotacao(Unidade unidade, Date dataEmissao) {

		BigDecimal saldoDotacao = BigDecimal.ZERO;
		Calendar date = Calendar.getInstance();
		date.setTime(dataEmissao);

		try {
			BigDecimal valorFixado = getValorFixado(String
					.format("SELECT vl_fixado FROM cgp.orcamento WHERE nu_exercicio = %d AND cd_codigo_conta = '%s' AND cd_atividade_projeto = '%s' AND cd_fonte_recurso = '%s' ",
							unidade.getParametroRepasse().getExercicio(),
							StringUtils.rightPad(UtilsModel
									.pegarSeisCaracteres(unidade
											.getParametroRepasse()
											.getCodElementoDespesa()), 12, "0"),
							unidade.getParametroRepasse().getCodAtividade(),
							unidade.getParametroRepasse().getCodFonteRecurso()));

			BigDecimal valorSuplementacoes = getValorSuplementacoes(String
					.format("SELECT SUM(vl_atualizacao) FROM cgp.alteracao_orcamentaria WHERE cd_tipo_registro = 0 AND st_alteracao = 0 AND rowid_orcamento_sup = %d AND EXTRACT(MONTH FROM dt_atualizacao) <= %d AND EXTRACT(YEAR FROM dt_atualizacao) = %d",
							unidade.getParametroRepasse().getCodDotacao(),
							date.get(Calendar.MONTH) + 1,
							date.get(Calendar.YEAR)));

			BigDecimal valorAnulacoes = getValorAnulacoes(String
					.format("SELECT sum(vl_atualizacao) FROM cgp.alteracao_orcamentaria WHERE cd_tipo_registro = 1 AND st_alteracao = 0 AND rowid_orcamento_sup = %d AND EXTRACT(MONTH FROM dt_atualizacao) <= %d AND EXTRACT(YEAR FROM dt_atualizacao) = %d",
							unidade.getParametroRepasse().getCodDotacao(),
							date.get(Calendar.MONTH) + 1,
							date.get(Calendar.YEAR)));

			BigDecimal valorEmpenhadoEmitido = getEmpenhadoEmitidos(String
					.format("SELECT sum(valor_empenho) FROM cgp.empenho WHERE ( (st_anulacao = 1) OR (st_anulacao = 0) ) AND rowid_orcamento = %d and extract(month from data_emissao) <= %d and extract(year from data_emissao) = %d and cd_unidade = %s",
							unidade.getParametroRepasse().getCodDotacao(), date
									.get(Calendar.MONTH) + 1, date
									.get(Calendar.YEAR), unidade
									.getUnidadeContabil().getId()));

			BigDecimal valorEmpenhadoAnulado = getEmpenhadoAnulados(String
					.format("select sum(valor_empenho) from cgp.empenho where st_anulacao = 2 and rowid_orcamento = %d and extract(month from data_emissao) <= %d and extract(year from data_emissao) = %d and cd_unidade = '%s'",
							unidade.getParametroRepasse().getCodDotacao(), date
									.get(Calendar.MONTH) + 1, date
									.get(Calendar.YEAR), unidade
									.getUnidadeContabil().getId()));

			BigDecimal totalEmpenhado = valorEmpenhadoEmitido
					.subtract(valorEmpenhadoAnulado);

			BigDecimal totalReservas = getTotalReservas(repasse.getUnidade()
					.getParametroRepasse().getCodDotacao(),
					date.get(Calendar.MONTH) + 1, date.get(Calendar.YEAR));

			saldoDotacao = valorFixado.add(valorSuplementacoes).subtract(
					valorAnulacoes.add(totalEmpenhado).add(totalReservas));

		} catch (SQLException e) {
			e.printStackTrace();
			return BigDecimal.ZERO;
		}

		return saldoDotacao;
	}

	private BigDecimal getTotalReservas(long idDotacao, int mes, int exercicio)
			throws SQLException {

		BigDecimal valorAcomuladoReserva = BigDecimal.ZERO;

		BigDecimal valorTotalEmpenhadoReserva = BigDecimal.ZERO;

		String query = String
				.format("Select nu_reserva,vl_reserva, st_reserva from cgp.reserva_dotacao where st_reserva in (0,1) and rowid_orcamento = %d and extract(month from dt_reserva) <= %d and extract(year from dt_reserva) = %d",
						idDotacao, mes, exercicio);
		result = createStatement.executeQuery(query);

		Set<ReservaDotacaoVO> reservas = new HashSet<ReservaDotacaoVO>();
		while (result.next()) {
			reservas.add(new ReservaDotacaoVO(result.getLong(1), result
					.getBigDecimal(2), result.getInt(3)));
		}
		result.close();

		for (ReservaDotacaoVO reserva : reservas) {
			if (reserva.getStatusReserva() == 0)
				valorAcomuladoReserva = valorAcomuladoReserva.add(reserva
						.getValor());
			else {
				BigDecimal valorEmpenhadoReserva = BigDecimal.ZERO;
				BigDecimal valorAnuladoReserva = BigDecimal.ZERO;
				ResultSet subResult = createStatement
						.executeQuery(String
								.format("select sum(valor_empenho) from cgp.empenho where ((st_anulacao = 1) Or (st_anulacao = 0) ) and nu_reserva = %d",
										reserva.getNumReserva()));

				while (subResult.next()) {
					BigDecimal valorEmpenho = subResult.getBigDecimal(1);
					if (valorEmpenho != null)
						valorEmpenhadoReserva = valorEmpenhadoReserva
								.add(valorEmpenho);
				}
				subResult.close();
				ResultSet subResultAnulacao = createStatement
						.executeQuery(String
								.format("select sum(valor_empenho) from cgp.empenho where st_anulacao = 2 and nu_reserva = %d",
										reserva.getNumReserva()));
				while (subResultAnulacao.next()) {
					BigDecimal valorEmpenho = subResultAnulacao
							.getBigDecimal(1);
					if (valorEmpenho != null)
						valorAnuladoReserva = valorAnuladoReserva
								.add(valorEmpenho);
				}

				valorTotalEmpenhadoReserva = valorTotalEmpenhadoReserva.add(
						valorEmpenhadoReserva).subtract(valorAnuladoReserva);
				subResultAnulacao.close();
			}
		}

		return valorAcomuladoReserva = valorAcomuladoReserva
				.add(valorTotalEmpenhadoReserva);
	}

	private BigDecimal getEmpenhadoAnulados(String query) throws SQLException {

		result = createStatement.executeQuery(query);

		while (result.next()) {
			return new BigDecimal(result.getInt(1));
		}

		return BigDecimal.ZERO;
	}

	private BigDecimal getEmpenhadoEmitidos(String query) throws SQLException {

		result = createStatement.executeQuery(query);

		while (result.next()) {
			return new BigDecimal(result.getInt(1));
		}

		return BigDecimal.ZERO;
	}

	private BigDecimal getValorAnulacoes(String query) throws SQLException {

		result = createStatement.executeQuery(query);

		while (result.next()) {
			return new BigDecimal(result.getInt(1));
		}

		return BigDecimal.ZERO;
	}

	private BigDecimal getValorSuplementacoes(String query) throws SQLException {

		result = createStatement.executeQuery(query);

		while (result.next()) {
			return new BigDecimal(result.getInt(1));
		}

		return BigDecimal.ZERO;
	}

	private BigDecimal getValorFixado(String query) throws SQLException {

		result = createStatement.executeQuery(query);

		while (result.next()) {
			return new BigDecimal(result.getInt(1));
		}

		return BigDecimal.ZERO;
	}

	public Unidade getUnidade() {
		return unidade;
	}

	public void setUnidade(Unidade unidade) {
		this.unidade = unidade;
	}

	public RepasseService getRepasseService() {
		return repasseService;
	}

	public void setRepasseService(RepasseService repasseService) {
		this.repasseService = repasseService;
	}

	public Repasse getRepasse() {
		return repasse;
	}

	public void setRepasse(Repasse repasse) {
		this.repasse = repasse;
	}

	public String getNumeroProcessoLabel() {
		return numeroProcessoLabel;
	}

	public void setNumeroProcessoLabel(String numeroProcessoLabel) {
		this.numeroProcessoLabel = numeroProcessoLabel;
	}

	public Media getEmpenhoMedia() {
		return empenhoMedia;
	}

	public void setEmpenhoMedia(Media empenhoMedia) {
		this.empenhoMedia = empenhoMedia;
	}

	public ResponsavelService getResponsavelService() {
		return responsavelService;
	}

	public void setResponsavelService(ResponsavelService responsavelService) {
		this.responsavelService = responsavelService;
	}
}
