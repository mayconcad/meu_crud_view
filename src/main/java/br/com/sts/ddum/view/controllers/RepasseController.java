package br.com.sts.ddum.view.controllers;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.omnifaces.util.Ajax;
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

@Controller
@ViewScoped
public class RepasseController extends BaseController {

	private static final long serialVersionUID = -3922297442654647722L;

	private final String DE_HISTORICO = "Empenho de suprimento de fundo para atender as necessidades de manutenção de bens móveis e imóveis conforme a lei 2.928, de 18 de agosto de 2014, que instituiu o Programa Municipal Dinheiro Direto nas Unidades Municipais - PMDDU";

	@Autowired
	private RepasseService repasseService;

	@Autowired
	private ConnectionConfigService connectionConfigService;

	private Unidade unidade;

	private Repasse repasse;

	private Statement createStatement;
	private Connection conexaoBancoCGP;

	@PostConstruct
	public void init() {
		unidade = new Unidade();
		repasse = new Repasse();
		conexaoBancoCGP = connectionConfigService.obterConexaoBancoCGP();
		Ajax.update(":repasseTabView");
	}

	public void gerarRepasse() {

		// Responsavel credor = null;
		// try {
		// credor = obterCredor(repasse.getUnidade().getResponsavel());
		// } catch (SQLException e) {
		// e.printStackTrace();
		// addErrorMessage("Não foi possível registrar o reponsável como credor!\n"
		// + e.getLocalizedMessage());
		// return;
		// }

		long proximoNumeroEmpenho = repasseService
				.obterProximoNumeroEmpenho(repasse.getUnidade()
						.getUnidadeContabil());
		// int exercicio = repasse.getUnidade().getParametroRepasse()
		// .getExercicio();
		// String codConta = StringUtils.rightPad(repasse.getUnidade()
		// .getParametroRepasse().getCodElementoDespesa(), 12, "0");
		// BigDecimal valorEmpenho = getValorEmpenho(repasse.getUnidade()
		// .getParametroRepasse().getValorRepasse());
		// BigDecimal saldoDotacao = calculoSaldoDotacao(repasse.getUnidade(),
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
		try {
			repasseService.repasseAutomatico(repasse, proximoNumeroEmpenho);
		} catch (IllegalArgumentException e) {
			addErrorMessage(e.getMessage());
			return;
		} catch (Exception e) {
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao());
			return;
		}

		init();
		addInfoMessage(String.format(
				"Foi gerado o número ( %s ) para o Empenho",
				proximoNumeroEmpenho));

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
				"SELECT * FROM cgp.credor WHERE cd_cpfcnpj LIKE '%s'",
				responsavel.getCpf());

		createStatement = conexaoBancoCGP.createStatement();

		result = createStatement.executeQuery(queryBuscaCredor);

		while (result.next()) {
			result.close();
			return responsavel;
		}
		String queryInseriCredor = String
				.format("INSERT INTO cgp.credor(cd_credor, de_razao_social, cd_cpfcnpj, tp_credor, st_credor,de_endereco, de_bairro, de_cidade, cd_uf, dt_cadastro, cd_nivel_integracao, dt_registro) VALUES((SELECT MAX(c.cd_credor) + 1 FROM cgp.credor c), '%s', '%s', %d, %d, '%s', '%s', '%s', '%s', '%s', %d, '%s')",
						responsavel.getNome(), responsavel.getCpf(), 0, 0,
						responsavel.getEndereco(), responsavel.getBairro(),
						responsavel.getCidade(), responsavel.getUf(),
						sdf.format(new Date()), 1, sdf.format(new Date()));
		createStatement.executeUpdate(queryInseriCredor);
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
}
