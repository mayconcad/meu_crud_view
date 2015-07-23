package br.com.sts.ddum.view.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.omnifaces.util.Ajax;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.confirmdialog.ConfirmDialog;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.media.Media;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.entities.Documento;
import br.com.sts.ddum.model.entities.Feriado;
import br.com.sts.ddum.model.entities.PrestacaoConta;
import br.com.sts.ddum.model.entities.Repasse;
import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.entities.Unidade;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.enums.StatusItemEnum;
import br.com.sts.ddum.model.repository.interfaces.DocumentoRepository;
import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.model.vos.EmpenhoVO;
import br.com.sts.ddum.model.vos.ItemPrestacaoContaVO;
import br.com.sts.ddum.model.vos.LiquidacaoVO;
import br.com.sts.ddum.model.vos.PagamentoVO;
import br.com.sts.ddum.model.vos.PrestacaoContaReportVO;
import br.com.sts.ddum.model.vos.PrestacaoContaTableVO;
import br.com.sts.ddum.service.interfaces.ConnectionConfigService;
import br.com.sts.ddum.service.interfaces.FeriadoService;
import br.com.sts.ddum.service.interfaces.PrestacaoContaService;
import br.com.sts.ddum.service.interfaces.RepasseService;
import br.com.sts.ddum.service.interfaces.ResponsavelService;
import br.com.sts.ddum.service.interfaces.UnidadeService;
import br.com.sts.ddum.service.interfaces.UserService;
import br.com.sts.ddum.view.uploads.UploadedFileUtil;
import br.com.sts.ddum.view.utils.UtilsView;

/**
 * @author developer
 *
 */
@Controller
@ViewScoped
public class PrestacaoContaController extends BaseReportController {

	private static final long serialVersionUID = -3922297442654647722L;

	@Autowired
	private UserService userService;

	@Autowired
	private RepasseService repasseService;

	@Autowired
	private ResponsavelService responsavelService;

	@Autowired
	private PrestacaoContaService prestacaoContaService;

	@Autowired
	private ConnectionConfigService connectionConfigService;

	@Inject
	private DocumentoRepository documentoRepository;

	@Autowired
	private FeriadoService feriadoService;

	@Autowired
	private UnidadeService unidadeService;

	// @PersistenceContext
	// private EntityManager entityManager;

	private PrestacaoConta prestacaoConta;

	private Date dataPrestacao;

	private String saldoDisponivel;

	private String saldoAberto;

	private String totalRepasse;

	private String valorDoc;

	private String valorTotal;

	private String messagemPrestacaoContas;

	private UploadedFile arquivoUploadFile;

	private InputText arquivoNomeFileUpload;

	private Media prestacaoContaMedia;

	private Media pdfView;

	private Documento documento;
	private Documento documentoEdite;
	private Documento documentoRemove;
	private Documento documentoExibe;

	private Statement createStatement;

	private Connection conexaoBancoCGP;

	private Repasse repasse;
	private Repasse repasseProximo;

	// private List<Documento> documentosSelected;

	private List<Documento> documentos;

	private List<EmpenhoVO> empenhos;

	private List<LiquidacaoVO> liquidacoes;

	private List<PagamentoVO> pagamentos;

	private Map<Long, BigDecimal> liqPrestContaMap;

	private Map<Long, BigDecimal> liqSaldoMap;

	private StreamedContent reportStream;

	private StreamedContent imageStream;

	// private DocumentoModel documentoModel = new DocumentoModel();

	private boolean possuiEmpenho = false;

	private boolean confirmaDocAtrasado = false;

	private ConfirmDialog confirmFinalizarDialog;

	private List<PrestacaoContaTableVO> prestContaTable;

	private Iterator<PrestacaoContaTableVO> prestContaIterator;

	private CommandButton commandButOneLiq;

	private Map<Integer, CommandButton> commandButMap;

	private Map<String, byte[]> arquivoByteMap;

	// atributo que armazena as liquidações sem prestação de contas
	List<Repasse> repassesPrestacao;

	// Atributos para o dialog de liquidações
	private LiquidacaoVO liqSelecionado;

	private PagamentoVO pagSelecionado;

	private PrestacaoConta prestSelecionado;

	private String itemSelected;

	private List<PrestacaoConta> prestacaoContas;

	private Dialog liqDialog;

	private boolean renderedLiq, renderedPag, renderedPrest;

	boolean semLiquidacaoMes = false;

	private Calendar calendar = Calendar.getInstance();

	private InputText valorDocText = new InputText(),
			tipoDoc = new InputText();

	private org.primefaces.component.calendar.Calendar prestContaData = new org.primefaces.component.calendar.Calendar();

	@PostConstruct
	public void init() {
		prestContaData.setStyleClass(null);
		tipoDoc.setStyleClass(null);
		valorDocText.setStyleClass(null);
		arquivoByteMap = new HashMap<String, byte[]>();
		repassesPrestacao = new ArrayList<Repasse>();
		prestSelecionado = new PrestacaoConta();
		pagSelecionado = new PagamentoVO();
		liqSelecionado = new LiquidacaoVO();
		prestacaoContas = new ArrayList<PrestacaoConta>();
		commandButMap = new HashMap<Integer, CommandButton>();
		commandButMap.put(1, commandButOneLiq);
		commandButOneLiq = new CommandButton();
		prestContaTable = new ArrayList<PrestacaoContaTableVO>();
		confirmaDocAtrasado = possuiEmpenho = false;
		empenhos = new ArrayList<EmpenhoVO>();
		liqSaldoMap = new HashMap<Long, BigDecimal>();
		liqPrestContaMap = new HashMap<Long, BigDecimal>();
		pdfView = prestacaoContaMedia = new Media();
		dataPrestacao = new Date();
		arquivoUploadFile = new UploadedFileUtil();
		prestacaoConta = new PrestacaoConta();
		repasse = repasseProximo = new Repasse();
		if (repasse.getValorRepasse() == null
				|| repasse.getValorRepasse().compareTo(BigDecimal.ZERO) == 0) {
			totalRepasse = saldoAberto = saldoDisponivel = "R$ 0,00";
		} else {
			totalRepasse = saldoDisponivel = UtilsModel
					.convertBigDecimalToString(repasse.getValorRepasse());
		}
		documento = documentoExibe = documentoEdite = documentoRemove = new Documento();
		documentos = new ArrayList<Documento>();
		liquidacoes = new ArrayList<LiquidacaoVO>();
		pagamentos = new ArrayList<PagamentoVO>();
		arquivoNomeFileUpload = new InputText();
		arquivoNomeFileUpload.setStyleClass(null);
		valorTotal = "R$ 0,00";
		itemSelected = messagemPrestacaoContas = valorDoc = new String();
		conexaoBancoCGP = connectionConfigService.obterConexaoBancoCGP();
		Ajax.update(":prestacaoContaTabView");
	}

	/**
	 * 
	 * @description inicio do novo trecho de código
	 * @param
	 */

	public void prestarContas() {

		// verifica a integridade dos documentos informados
		if (!confirmaDocAtrasado && !possuiDocumentosValidos(getDocumentos()))
			return;
		if (getPagamentos() != null && !getPagamentos().isEmpty()) {
			PagamentoVO pagamentoVO = getPagamentos().get(
					prestacaoConta.getMesOrdinal());
			if (pagamentoVO == null) {
				addErrorMessage("Não há um Pagamento para a Liquidação, assim não é possivel Prestar Contas");
				return;
			}
			if (UtilsModel.removerHorarioData(new Date()).before(
					UtilsModel.removerHorarioData(pagamentoVO.getDataTipo()))) {
				addErrorMessage("A Data da Prestação de Conta não pode ser inferior à Data do Pagamento.");
				return;
			}
		} else {
			addErrorMessage("Não há um Pagamento para a Liquidação, assim não é possivel Prestar Contas");
			return;
		}

		List<Documento> asList = new ArrayList<Documento>(getDocumentos());
		prestacaoConta.setAtivo(true);
		prestacaoConta.setDocumentos(asList);
		prestacaoConta.setDataPrestacao(new Date());
		calendar.setTime(prestacaoConta.getDataPrestacao());
		// Atualização do mês ordinal caso a prestação de contas sejá feita
		// posterior ao mês do pagamento
		prestacaoConta.setMesOrdinal(calendar.get(Calendar.MONTH));
		prestacaoConta.setValor(UtilsModel
				.convertStringToBigDecimal(getValorTotal()));
		prestacaoConta.setSaldoDisponivel(repasse.getValorRepasse()
				.add(repasse.getSaldoAberto())
				.subtract(prestacaoConta.getValor()));
		prestacaoConta.setFinalizada(true);
		prestacaoConta.setStatus(StatusItemEnum.EM_ANALISE);
		prestacaoConta.setRepasse(repasse);

		// Verifica se há pagamento para esta liquidação uma vez, que quem
		// liquida é o registro de pagamento
		try {
			ResultSet resultSet = createStatement
					.executeQuery(String
							.format("SELECT DISTINCT p.nu_pagamento, p.vl_pagamento, p.dt_pagamento FROM cgp.pagamento p WHERE p.nu_empenho = %d AND p.nu_liquidacao = %d AND p.cd_orgao = %s AND p.cd_unidade = %s AND p.cd_atividade_projeto = %s AND p.cd_fonte_recurso = %s AND p.nu_exercicio = %d AND ( p.st_pagamento = '0' OR p.st_pagamento = '1') ORDER BY p.dt_pagamento",
									repasse.getNumeroEmpenho(),
									repasse.getNumeroLiquidacao(), repasse
											.getUnidade().getUnidadeContabil()
											.getAtividade().getCodOrgao(),
									repasse.getUnidade().getParametroRepasse()
											.getCodUnidade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodAtividade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodFonteRecurso(),
									repasse.getExercicio()));

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(prestacaoConta.getDataPrestacao());

			long numPagamento = 0l;
			BigDecimal valor = BigDecimal.ZERO;
			Date data = null;
			while (resultSet.next()) {
				numPagamento = resultSet.getLong(1);
				valor = resultSet.getBigDecimal(2);
				data = resultSet.getDate(3);
				break;
			}
			resultSet.close();

			if (numPagamento == 0l) {
				addErrorMessage("Não há um Pagamento para a Liquidação, assim não é possivel Prestar Contas");
				RequestContext.getCurrentInstance().execute("topo()");
				return;
			}
			prestacaoConta.setDataPagamento(data);
			prestacaoConta.setValorPagamento(valor);
			prestacaoConta.setNumeroPagamento(numPagamento);

			Repasse repasseSemPrestacao = getRepassesSemPrestacao().get(
					getRepassesSemPrestacao().size() - 1);
			calendar.setTime(repasseSemPrestacao.getDataEmissao());

			Repasse proxRepasse = new Repasse();
			// agendar próximo repasse com base última liquidação
			// definir data da próxima liquidação
			// paramsFeriado.put("dia", value);
			byte dia = ((byte) calendar.get(Calendar.DAY_OF_MONTH));
			byte mes = ((byte) (calendar.get(Calendar.MONTH) + 1));

			// verifica se a data do empenho é equivamente ao primeiro dia
			// util do mês, caso contrário a data para liquidação será
			// primeiro dia útil do próximo mês
			if (dia == 1) {
				Feriado feriado = feriadoService.buscarPorDiaMes(dia, mes);
				if (feriado != null) {
					++mes;
					int diaUtilMes = obterDiaUtilMes(1, mes);
					calendar.set(calendar.get(Calendar.YEAR), --mes, diaUtilMes);
					proxRepasse.setDataEmissao(calendar.getTime());
				} else {
					calendar.set(calendar.get(Calendar.YEAR), mes, dia);
					proxRepasse.setDataEmissao(calendar.getTime());
				}
			} else {
				++mes;
				int diaUtilMes = obterDiaUtilMes(1, mes);
				calendar.set(calendar.get(Calendar.YEAR), --mes, diaUtilMes);
				proxRepasse.setDataEmissao(calendar.getTime());
			}

			proxRepasse.setAtivo(repasse.isAtivo());
			proxRepasse.setCodAplicacao(repasse.getCodAplicacao());
			proxRepasse.setExercicio(repasse.getExercicio());
			proxRepasse.setHistorico(repasse.getHistorico());
			proxRepasse.setNaturezaEmpenho(repasse.getNaturezaEmpenho());
			proxRepasse.setNumeroEmpenho(repasse.getNumeroEmpenho());
			proxRepasse.setNumeroProcesso(repasse.getNumeroProcesso());
			proxRepasse.setTipoCredito(repasse.getTipoCredito());
			proxRepasse.setTipoEmpenho(repasse.getTipoEmpenho());
			proxRepasse.setTipoMeta(repasse.getTipoMeta());
			proxRepasse.setUnidade(repasse.getUnidade());
			proxRepasse.setValorEmpenho(repasse.getValorEmpenho());
			proxRepasse.setDataEmpenho(repasse.getDataEmpenho());
			proxRepasse.setValorRepasse(prestacaoConta.getValor());
			proxRepasse.setSaldoAberto(repasse.getValorRepasse()
					.add(repasse.getSaldoAberto())
					.subtract(prestacaoConta.getValor()));

			// registro de uma prestação de contas
			PrestacaoConta prestConta = prestacaoContaService
					.salvar(prestacaoConta);

			// criar um repasse - empenho - liquidacao
			// repasseService.repasseAutomatico(proxRepasse, proxRepasse
			// .getUnidade().getResponsavel(), UtilsModel
			// .convertStringToBigDecimal(getValorTotal()), prestConta
			// .getValor(), repasseService
			// .obterProximoNumeroEmpenho(proxRepasse.getUnidade()
			// .getParametroRepasse().getCodUnidade()), false);

			repasse.setPrestacaoConta(prestConta);
			repasseService.atualizar(repasse);
			repasseService.salvar(proxRepasse);

			// preparndo dados para o relatório
			ArrayList<PrestacaoContaReportVO> contas = new ArrayList<PrestacaoContaReportVO>();
			contas.add(new PrestacaoContaReportVO());
			contas.get(0).setDocumentos(prestacaoConta.getDocumentos());
			contas.get(0).setEmpenhos(getEmpenhos());
			LiquidacaoVO liquidacaoVO = getLiquidacoes().get(
					repasse.getPrestacaoConta().getMesOrdinal());
			liquidacaoVO.setNumPrestConta(liquidacaoVO.getNumero() + "");

			BigDecimal valorLiquidacao = UtilsModel
					.convertStringToBigDecimal(liquidacaoVO.getValor());

			BigDecimal saldoTotal = valorLiquidacao.add(repasse
					.getSaldoAberto());

			BigDecimal saldoAtual = saldoTotal.subtract(prestacaoConta
					.getValor());

			liquidacaoVO.setSaldo(UtilsModel
					.convertBigDecimalToString(saldoAtual));
			liquidacaoVO.setValorPrestConta(UtilsModel
					.convertBigDecimalToString(prestacaoConta.getValor()));
			contas.get(0).setLiquidacoes(Arrays.asList(liquidacaoVO));

			generateReportStream(liquidacaoVO.getValor(),
					UtilsModel.convertBigDecimalToString(saldoTotal),
					UtilsModel.convertBigDecimalToString(saldoAtual),
					UtilsModel.convertBigDecimalToString(repasse
							.getSaldoAberto()), valorTotal,
					liquidacaoVO.getNumPrestConta(), dataPrestacao, repasse
							.getUnidade().getResponsavel(), contas);

			getPrestacaoContaMedia().setValue("/reports/PrestacaoContas.pdf");

			carregarReportTab();

		} catch (Exception e) {
			prestacaoContaService.remover(prestacaoConta);
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao()
					+ e.getLocalizedMessage());
			RequestContext.getCurrentInstance().execute("topo()");
			return;
		}
		addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
		carregarDados(false);

	}

	public void carregarDados(boolean cleanReport) {

		// redirect("/ReportData");
		if (cleanReport)
			closeEditTab(null);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		this.init();
		if (cleanReport)
			getPrestacaoContaMedia().setValue(null);
		LoginBean controllerInstance = UtilsView
				.getControllerInstance(LoginBean.class);
		User currentUser = (User) controllerInstance.getCurrentUser();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("user.id", currentUser.getId());
		List<User> users = userService.buscar(params);
		// List<Responsavel> responsaveis = responsavelService.buscar(params);
		if (currentUser.getResponsavel() == null) {
			addErrorMessage("A operação não pode ser realizada, pois o usuário não é um Responsável!");
			RequestContext.getCurrentInstance().execute("topo()");
			return;
		}

		Responsavel responsavel = currentUser.getResponsavel();

		params.clear();
		Calendar dataAtual = Calendar.getInstance();
		dataAtual.setTime(new Date());
		if (responsavel != null) {
			params.put("responsavel.id", responsavel.getId());
			List<Unidade> unidades = unidadeService.buscar(params);
			params.clear();
			params.put("unidade.responsavel.id", responsavel.getId());
			params.put("unidade.id", unidades.get(0).getId());
			// params.put("prestacaoConta.finalizada", false);
			// dataAtual.set(Calendar.DAY_OF_MONTH,
			// dataAtual.getActualMaximum(Calendar.DAY_OF_MONTH));
			// params.put("maxDate", dataAtual.getTime());
		}

		repassesPrestacao = repasseService.buscar(params);
		if (repassesPrestacao == null || repassesPrestacao.isEmpty()) {
			addErrorMessage("A operação não pode ser realizada, não existe Empenho para este Responsável!");
			RequestContext.getCurrentInstance().execute("topo()");
			return;
		}
		// pega o primeiro repasse/liquidação que não possui prestação de contas
		Repasse repasseItem = repassesPrestacao.get(0);
		calendar.setTime(repasseItem.getDataEmissao());

		// verifica se o mes do repasse corresponde ao mes da prestacao de conta
		semLiquidacaoMes = false;
		if (calendar.get(Calendar.MONTH) != dataAtual.get(Calendar.MONTH))
			semLiquidacaoMes = true;

		params.clear();
		params.put("repasse.id", repasseItem.getId());
		List<PrestacaoConta> list = prestacaoContaService.buscar(params);
		if (list.isEmpty()) {
			this.repasse = repasseItem;
			totalRepasse = saldoDisponivel = UtilsModel
					.convertBigDecimalToString(this.repasse.getValorRepasse());
		} else {
			this.repasse = repasseItem;
			this.prestacaoConta = repasse.getPrestacaoConta();

			setDocumentos(repasse.getPrestacaoConta().getDocumentos());

			totalRepasse = UtilsModel.convertBigDecimalToString(list.get(0)
					.getValor());
			saldoAberto = UtilsModel.convertBigDecimalToString(list.get(0)
					.getSaldoDisponivel());
			saldoDisponivel = UtilsModel.convertBigDecimalToString(list.get(0)
					.getSaldoDisponivel().add(list.get(0).getValor()));
		}

		if (repasse.getId() == null || repasse.getId() == 0l) {
			addErrorMessage("Não existe Liquidação Ativo para este Responsável!");
			RequestContext.getCurrentInstance().execute("topo()");
			setPossuiEmpenho(false);
			return;
		}

		// Statement createStatement;
		String[] valoresEmpenho = getValoresEmpenhoLiquidacao(
				repasse.getNumeroEmpenho(), repasse.getExercicio());
		BigDecimal saldoPag = BigDecimal.ZERO;

		try {
			createStatement = conexaoBancoCGP.createStatement();
			// traz apenas liquidação de estejam pagas por isso o uso da tabela
			// pagamento
			// TODO Adicionar trecho na instrução sql para buscar as liquidações
			// que possuem pagamento
			ResultSet resultSet = createStatement
					.executeQuery(String
							.format("SELECT DISTINCT l.nu_liquidacao, l.dt_liquidacao, l.vl_liquidacao FROM cgp.liquidacao l WHERE l.cd_orgao = %s AND l.cd_unidade = %s AND l.cd_atividade_projeto = %s AND l.cd_fonte_recurso = %s AND l.nu_exercicio = %d AND l.nu_empenho = %d AND l.nu_processo = '%s'",
									repasse.getUnidade().getUnidadeContabil()
											.getAtividade().getCodOrgao(),
									repasse.getUnidade().getParametroRepasse()
											.getCodUnidade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodAtividade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodFonteRecurso(),
									repasse.getExercicio(),
									repasse.getNumeroEmpenho(), "2928"));

			PrestacaoContaTableVO prestContaTableLiqVO = new PrestacaoContaTableVO();
			getPrestacaoContas().addAll(popularPrestContaComNulos());
			getLiquidacoes().addAll(popularLiqComNulos());
			getPagamentos().addAll(popularPagComNulos());
			prestContaTableLiqVO.getItens().addAll(popularItemComNulos());

			Calendar calendar = Calendar.getInstance();
			Set<Long> liquidacoesIds = new HashSet<Long>();
			StringBuilder numLiquidacoes = new StringBuilder("(0,");

			while (resultSet.next()) {
				LiquidacaoVO liqVO = new LiquidacaoVO();
				liqVO.setNumero(resultSet.getLong(1));

				// duas próximas linhas agrupam ids das liquidações para buscas
				// posteriores
				liquidacoesIds.add(liqVO.getNumero());
				numLiquidacoes.append(liqVO.getNumero()).append(" ,");

				liqVO.setNumPrestConta(liqPrestContaMap.containsKey(liqVO
						.getNumero()) ? liqVO.getNumero() + "" : " - ");
				Date date = resultSet.getDate(2);
				calendar.setTime(date);
				liqVO.setData(sdf.format(date));
				liqVO.setMesOrdinal(calendar.get(Calendar.MONTH));
				liqVO.setValor(UtilsModel.convertBigDecimalToString(resultSet
						.getBigDecimal(3)));
				liqVO.setValorPrestConta(liqPrestContaMap.isEmpty()
						|| !liqPrestContaMap.containsKey(liqVO.getNumero()) ? "R$ 0,00"
						: UtilsModel.convertBigDecimalToString(liqPrestContaMap
								.get(liqVO.getNumero())));
				liqVO.setSaldo(liqSaldoMap.isEmpty()
						|| !liqSaldoMap.containsKey(liqVO.getNumero()) ? "R$ 0,00"
						: UtilsModel
								.convertBigDecimalToString(obterSaldoAcumulado(liqVO
										.getNumero())));
				int indexConta = liqVO.getMesOrdinal();
				prestContaTableLiqVO.getItens().remove(indexConta);
				prestContaTableLiqVO.getItens().add(
						indexConta,
						new ItemPrestacaoContaVO(liqVO.getNumero(), UtilsModel
								.convertStringToBigDecimal(liqVO.getValor()),
								date, indexConta, StatusItemEnum.EM_ANALISE));
				liqVO.setStatus(StatusItemEnum.APROVADA);
				int indexLiq = liqVO.getMesOrdinal();
				getLiquidacoes().remove(indexLiq);
				getLiquidacoes().add(indexLiq, liqVO);
			}
			// for (LiquidacaoVO liq : getLiquidacoes()) {
			// if (liq != null)
			// numLiquidacoes.append(liq.getNumero()).append(" ,");
			// }
			numLiquidacoes.replace(numLiquidacoes.length() - 1,
					numLiquidacoes.length(), ")");

			// trecho que busca as prestacões de conta para as liquidacões caso
			// esta exista
			if (!liquidacoesIds.isEmpty()) {
				List<PrestacaoConta> prestContas = prestacaoContaService
						.buscar(repasse.getNumeroEmpenho(), liquidacoesIds);
				if (prestContas != null && !prestContas.isEmpty())
					for (PrestacaoConta prest : prestContas) {
						getPrestacaoContas().remove(prest.getMesOrdinal());
						getPrestacaoContas().add(prest.getMesOrdinal(), prest);
					}
			}

			resultSet.close();
			// AND p.dt_estorno is null
			resultSet = createStatement
					.executeQuery(String
							.format("SELECT DISTINCT p.nu_pagamento, p.nu_liquidacao, p.vl_pagamento, p.dt_pagamento, p.st_pagamento FROM cgp.pagamento p WHERE p.nu_empenho = %d AND p.nu_liquidacao IN %s AND p.cd_orgao = %d AND p.cd_unidade = %d AND p.cd_atividade_projeto = %d AND p.cd_fonte_recurso = %d AND p.nu_exercicio = %d AND ( p.st_pagamento = '0' OR p.st_pagamento = '1') ORDER BY p.dt_pagamento",
									repasse.getNumeroEmpenho(),
									numLiquidacoes.toString(),
									Integer.parseInt(repasse.getUnidade()
											.getUnidadeContabil()
											.getAtividade().getCodOrgao()),
									Integer.parseInt(repasse.getUnidade()
											.getParametroRepasse()
											.getCodUnidade()),
									Integer.parseInt(repasse.getUnidade()
											.getParametroRepasse()
											.getCodAtividade()),
									Integer.parseInt(repasse.getUnidade()
											.getParametroRepasse()
											.getCodFonteRecurso()),
									repasse.getExercicio()));

			boolean contemPagamento = false;
			int indexPag = 0;
			while (resultSet.next()) {
				contemPagamento = true;
				PagamentoVO pagVO = new PagamentoVO();
				pagVO.setNumero(resultSet.getLong(1));
				pagVO.setExercicio(repasse.getExercicio());
				pagVO.setNumeroEmpenho(repasse.getNumeroEmpenho());
				pagVO.setNumeroLiquidacao(resultSet.getLong(2));

				BigDecimal valorPag = resultSet.getBigDecimal(3);
				saldoPag = saldoPag.add(valorPag);
				pagVO.setValor(UtilsModel.convertBigDecimalToString(valorPag));
				Date date = resultSet.getDate(4);
				calendar.setTime(date);
				pagVO.setDataTipo(date);
				pagVO.setData(sdf.format(date));
				pagVO.setMesOrdinal(calendar.get(Calendar.MONTH));
				if (resultSet.getInt(5) == 1) {
					pagVO.setSaldo(liqSaldoMap.isEmpty()
							|| !liqSaldoMap.containsKey(pagVO
									.getNumeroLiquidacao()) ? "R$ 0,00"
							: UtilsModel
									.convertBigDecimalToString(obterSaldoAcumulado(
											pagVO.getNumeroLiquidacao())
											.subtract(valorPag)));
				} else
					pagVO.setSaldo(liqSaldoMap.isEmpty()
							|| !liqSaldoMap.containsKey(pagVO
									.getNumeroLiquidacao()) ? "R$ 0,00"
							: UtilsModel
									.convertBigDecimalToString(obterSaldoAcumulado(pagVO
											.getNumeroLiquidacao())));
				indexPag = pagVO.getMesOrdinal();
				getPagamentos().remove(indexPag);
				getPagamentos().add(indexPag, pagVO);
			}

			// Adição do indice do último registro de pagamento para obteção da
			// data de pagamento quando este for acessado no método
			// prestarContas
			this.prestacaoConta.setMesOrdinal(indexPag);

			resultSet.close();

			Collections.sort(prestContaTableLiqVO.getItens(),
					new Comparator<ItemPrestacaoContaVO>() {

						@Override
						public int compare(ItemPrestacaoContaVO o1,
								ItemPrestacaoContaVO o2) {
							if (o1 != null && o2 != null)
								return o1.compareTo(o2);
							return 0;
						}
					});
			if (contemPagamento) {
				Collections.sort(getPagamentos(),
						new Comparator<PagamentoVO>() {

							@Override
							public int compare(PagamentoVO o1, PagamentoVO o2) {
								if (o1 != null && o2 != null)
									return o1.compareTo(o2);
								return 0;
							}
						});
				contemPagamento = false;
			}
			prestContaTable.add(prestContaTableLiqVO);
		} catch (SQLException e) {
			e.printStackTrace();
			addErrorMessage(e.getLocalizedMessage());
			RequestContext.getCurrentInstance().execute("topo()");
			return;
		}

		List<ItemPrestacaoContaVO> itens = prestContaTable.get(0).getItens();

		for (ItemPrestacaoContaVO itemVO : itens) {
			if (itemVO != null)
				if (!StatusItemEnum.APROVADA.equals(itemVO.getStatus())) {
					// CommandButton commandButton = commandButMap.get(itemVO
					// .getNumero());
					commandButOneLiq.setStyle("background-color:red");

				}
		}

		prestContaIterator = prestContaTable.iterator();
		// prestContaIterator.
		// prestContaTable.get(0).getItens().listIterator().nextIndex()
		Collections.sort(getLiquidacoes(), new Comparator<LiquidacaoVO>() {

			@Override
			public int compare(LiquidacaoVO o1, LiquidacaoVO o2) {
				if (o1 != null && o2 != null)
					return o1.compareTo(o2);
				return 0;
			}
		});

		EmpenhoVO empVO = new EmpenhoVO();
		empVO.setNumero(repasse.getNumeroEmpenho() + "");
		empVO.setData(sdf.format(repasse.getDataEmpenho()));
		empVO.setValor(UtilsModel.convertBigDecimalToString(repasse
				.getValorEmpenho()));

		empVO.setValorTotalRepasse(valoresEmpenho[0]);
		empVO.setValorTotalPrestConta(valoresEmpenho[1]);
		empVO.setSaldo(UtilsModel.convertBigDecimalToString(repasse
				.getUnidade().getParametroRepasse().getValorRepasse()));
		empVO.setValorTotalPagamento(UtilsModel
				.convertBigDecimalToString(saldoPag));

		getEmpenhos().add(empVO);

		setPossuiEmpenho(true);

	}

	/**
	 * @description fim do novo trecho de código
	 * @param
	 */

	public void setRepasseSemPrestacaoConta(boolean clearReport) {

		// if (true) {
		// addErrorMessage("Esta funcionalidade ainda não está disponível!");
		// return;
		// }
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		this.init();
		if (clearReport)
			getPrestacaoContaMedia().setValue(null);
		LoginBean controllerInstance = UtilsView
				.getControllerInstance(LoginBean.class);
		User currentUser = (User) controllerInstance.getCurrentUser();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("user.id", currentUser.getId());
		List<Responsavel> responsaveis = responsavelService.buscar(params);
		if (responsaveis == null || responsaveis.isEmpty()) {
			addErrorMessage("A operação não pode ser realizada, pois o usuário não é um Responsável!");
			return;
		}

		Responsavel responsavel = responsaveis.get(0);

		params.clear();
		if (responsavel != null) {
			params.put("responsavel.id", responsavel.getId());
			List<Unidade> unidades = unidadeService.buscar(params);
			params.clear();
			params.put("unidade.responsavel.id", responsavel.getId());
			params.put("unidade.id", unidades.get(0).getId());
		}

		repassesPrestacao = repasseService.buscarSemPrestacao(params);
		if (repassesPrestacao == null || repassesPrestacao.isEmpty()) {
			addErrorMessage("A operação não pode ser realizada, não existe Empenho para este Responsável!");
			return;
		}
		// pega o primeiro repasse/liquidação que não possui prestação de contas
		Repasse repasseItem = repassesPrestacao.get(0);
		params.clear();
		params.put("repasse.id", repasseItem.getId());
		List<PrestacaoConta> list = prestacaoContaService.buscar(params);
		if (list.isEmpty()) {
			this.repasse = repasseItem;
			totalRepasse = saldoDisponivel = UtilsModel
					.convertBigDecimalToString(this.repasse.getValorRepasse());
		} else {
			this.repasse = repasseItem;
			this.prestacaoConta = repasse.getPrestacaoConta();

			setDocumentos(repasse.getPrestacaoConta().getDocumentos());

			totalRepasse = UtilsModel.convertBigDecimalToString(list.get(0)
					.getValor());
			saldoAberto = UtilsModel.convertBigDecimalToString(list.get(0)
					.getSaldoDisponivel());
			saldoDisponivel = UtilsModel.convertBigDecimalToString(list.get(0)
					.getSaldoDisponivel().add(list.get(0).getValor()));
		}

		if (repasse.getId() == null || repasse.getId() == 0l) {
			addErrorMessage("Não existe Repasse Ativo para este Responsável!");
			setPossuiEmpenho(false);
			return;
		}

		// Statement createStatement;
		String[] valoresEmpenho = getValoresEmpenhoLiquidacao(
				repasse.getNumeroEmpenho(), repasse.getExercicio());

		try {
			createStatement = conexaoBancoCGP.createStatement();
			// traz apenas liquidação de estejam pagas por isso o uso da tabela
			// pagamento
			// TODO Adicionar trecho na instrução sql para buscar as liquidações
			// que possuem pagamento
			ResultSet resultSet = createStatement
					.executeQuery(String
							.format("SELECT DISTINCT l.nu_liquidacao, l.dt_liquidacao, l.vl_liquidacao FROM cgp.liquidacao l WHERE l.cd_orgao = %s AND l.cd_unidade = %s AND l.cd_atividade_projeto = %s AND l.cd_fonte_recurso = %s AND l.nu_exercicio = %d AND l.nu_empenho = %d AND l.nu_processo = '%s'",
									repasse.getUnidade().getUnidadeContabil()
											.getAtividade().getCodOrgao(),
									repasse.getUnidade().getParametroRepasse()
											.getCodUnidade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodAtividade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodFonteRecurso(),
									repasse.getExercicio(),
									repasse.getNumeroEmpenho(), "2928"));

			PrestacaoContaTableVO prestContaTableLiqVO = new PrestacaoContaTableVO();
			getPrestacaoContas().addAll(popularPrestContaComNulos());
			getLiquidacoes().addAll(popularLiqComNulos());
			getPagamentos().addAll(popularPagComNulos());
			prestContaTableLiqVO.getItens().addAll(popularItemComNulos());

			Calendar calendar = Calendar.getInstance();

			while (resultSet.next()) {
				LiquidacaoVO liqVO = new LiquidacaoVO();
				liqVO.setNumero(resultSet.getLong(1));
				// trecho que busca a prestacao de conta para a liquidacão caso
				// esta exista
				params.clear();
				params.put("numeroEmpenho", repasse.getNumeroEmpenho());
				params.put("numeroLiquidacao", liqVO.getNumero());
				params.put("finalizada", true);
				List<PrestacaoConta> prestContas = prestacaoContaService
						.buscar(params);
				if (prestContas != null && !prestContas.isEmpty())
					getPrestacaoContas().add(
							prestContas.get(0).getMesOrdinal(),
							prestContas.get(0));
				liqVO.setNumPrestConta(liqPrestContaMap.containsKey(liqVO
						.getNumero()) ? liqVO.getNumero() + "" : " - ");
				Date date = resultSet.getDate(2);
				calendar.setTime(date);
				liqVO.setData(sdf.format(date));
				liqVO.setMesOrdinal(calendar.get(Calendar.MONTH));
				liqVO.setValor(UtilsModel.convertBigDecimalToString(resultSet
						.getBigDecimal(3)));
				liqVO.setValorPrestConta(liqPrestContaMap.isEmpty()
						|| !liqPrestContaMap.containsKey(liqVO.getNumero()) ? "R$ 0,00"
						: UtilsModel.convertBigDecimalToString(liqPrestContaMap
								.get(liqVO.getNumero())));
				liqVO.setSaldo(liqSaldoMap.isEmpty()
						|| !liqSaldoMap.containsKey(liqVO.getNumero()) ? "R$ 0,00"
						: UtilsModel
								.convertBigDecimalToString(obterSaldoAcumulado(liqVO
										.getNumero())));
				int indexConta = liqVO.getMesOrdinal();
				prestContaTableLiqVO.getItens().remove(indexConta);
				prestContaTableLiqVO.getItens().add(
						indexConta,
						new ItemPrestacaoContaVO(liqVO.getNumero(), UtilsModel
								.convertStringToBigDecimal(liqVO.getValor()),
								date, StatusItemEnum.EM_ANALISE));
				liqVO.setStatus(StatusItemEnum.EM_ANALISE);
				int indexLiq = liqVO.getMesOrdinal();
				getLiquidacoes().remove(indexLiq);
				getLiquidacoes().add(indexLiq, liqVO);
			}
			boolean contemPagamento = false;
			StringBuilder numLiquidacoes = new StringBuilder("(");
			for (LiquidacaoVO liq : getLiquidacoes()) {
				if (liq != null)
					numLiquidacoes.append(liq.getNumero()).append(" ,");
			}
			numLiquidacoes.replace(numLiquidacoes.length() - 1,
					numLiquidacoes.length(), ")");

			resultSet.close();
			resultSet = createStatement
					.executeQuery(String
							.format("SELECT DISTINCT p.nu_pagamento, p.nu_liquidacao, p.vl_pagamento, p.dt_pagamento FROM cgp.pagamento p WHERE p.nu_empenho = %d AND p.nu_liquidacao IN %s AND p.cd_orgao = %s AND p.cd_unidade = %s AND p.cd_atividade_projeto = %s AND p.cd_fonte_recurso = %s AND p.nu_exercicio = %d",
									repasse.getNumeroEmpenho(),
									numLiquidacoes.toString(), repasse
											.getUnidade().getUnidadeContabil()
											.getAtividade().getCodOrgao(),
									repasse.getUnidade().getParametroRepasse()
											.getCodUnidade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodAtividade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodFonteRecurso(),
									repasse.getExercicio()));

			while (resultSet.next()) {
				contemPagamento = true;
				PagamentoVO pagVO = new PagamentoVO();
				pagVO.setNumero(resultSet.getLong(1));
				pagVO.setExercicio(repasse.getExercicio());
				pagVO.setNumeroEmpenho(repasse.getNumeroEmpenho());
				pagVO.setNumeroLiquidacao(resultSet.getLong(2));
				pagVO.setValor(UtilsModel.convertBigDecimalToString(resultSet
						.getBigDecimal(3)));
				Date date = resultSet.getDate(4);
				calendar.setTime(date);
				pagVO.setData(sdf.format(date));
				pagVO.setMesOrdinal(calendar.get(Calendar.MONTH));
				int indexPag = pagVO.getMesOrdinal();
				getPagamentos().remove(indexPag);
				getPagamentos().add(indexPag, pagVO);
			}
			resultSet.close();

			Collections.sort(prestContaTableLiqVO.getItens(),
					new Comparator<ItemPrestacaoContaVO>() {

						@Override
						public int compare(ItemPrestacaoContaVO o1,
								ItemPrestacaoContaVO o2) {
							if (o1 != null && o2 != null)
								return o1.compareTo(o2);
							return 0;
						}
					});
			if (contemPagamento) {
				Collections.sort(getPagamentos());
				contemPagamento = false;
			}
			prestContaTable.add(prestContaTableLiqVO);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		List<ItemPrestacaoContaVO> itens = prestContaTable.get(0).getItens();

		for (ItemPrestacaoContaVO itemVO : itens) {
			if (itemVO != null)
				if (!StatusItemEnum.APROVADA.equals(itemVO.getStatus())) {
					// CommandButton commandButton = commandButMap.get(itemVO
					// .getNumero());
					commandButOneLiq.setStyle("background-color:red");

				}
		}

		prestContaIterator = prestContaTable.iterator();
		// prestContaIterator.
		// prestContaTable.get(0).getItens().listIterator().nextIndex()
		Collections.sort(getLiquidacoes(), new Comparator<LiquidacaoVO>() {

			@Override
			public int compare(LiquidacaoVO o1, LiquidacaoVO o2) {
				if (o1 != null && o2 != null)
					return o1.compareTo(o2);
				return 0;
			}
		});

		EmpenhoVO empVO = new EmpenhoVO();
		empVO.setNumero(repasse.getNumeroEmpenho() + "");
		empVO.setData(sdf.format(repasse.getDataEmpenho()));
		empVO.setValor(UtilsModel.convertBigDecimalToString(repasse
				.getValorEmpenho()));

		empVO.setValorTotalRepasse(valoresEmpenho[0]);
		empVO.setValorTotalPrestConta(valoresEmpenho[1]);
		empVO.setSaldo(UtilsModel.convertBigDecimalToString(repasse
				.getUnidade().getParametroRepasse().getValorRepasse()));

		getEmpenhos().add(empVO);

		setPossuiEmpenho(true);
	}

	private List<LiquidacaoVO> popularLiqComNulos() {
		List<LiquidacaoVO> list = new ArrayList<LiquidacaoVO>();
		for (int x = 0; x <= 11; x++) {
			list.add(x, null);
		}
		return list;
	}

	private List<PagamentoVO> popularPagComNulos() {
		List<PagamentoVO> list = new ArrayList<PagamentoVO>();
		for (int x = 0; x <= 11; x++) {
			list.add(x, null);
		}
		return list;
	}

	private List<PrestacaoConta> popularPrestContaComNulos() {
		List<PrestacaoConta> list = new ArrayList<PrestacaoConta>();
		for (int x = 0; x <= 11; x++) {
			list.add(x, null);
		}
		return list;
	}

	private List<ItemPrestacaoContaVO> popularItemComNulos() {
		List<ItemPrestacaoContaVO> list = new ArrayList<ItemPrestacaoContaVO>();
		for (int x = 0; x <= 11; x++) {
			list.add(x, null);
		}
		return list;
	}

	public void showDialogLiq(String index) {

		int indice = Integer.parseInt(index);
		liqSelecionado = liquidacoes.get(indice);
		setItemSelected("Liquidação");
		Map<String, Object> opcoes = new HashMap<String, Object>();
		opcoes.put("modal", true);
		opcoes.put("resizable", false);
		opcoes.put("contentHeight", 470);
		// liqDialog.setModal(true);
		RequestContext.getCurrentInstance().execute("PF('liqDialog').show()");
	}

	public void showDialogPag(String index) {

		int indice = Integer.parseInt(index);
		pagSelecionado = getPagamentos().get(indice);
		setItemSelected("Pagamento");
		// Map<String, Object> opcoes = new HashMap<String, Object>();
		// opcoes.put("modal", true);
		// opcoes.put("resizable", false);
		// opcoes.put("contentHeight", 470);

		RequestContext.getCurrentInstance().execute("PF('pagDialog').show()");
	}

	public void showDialogPrest(String index) {

		int indice = Integer.parseInt(index);
		prestSelecionado = getPrestacaoContas().get(indice);
		setItemSelected("Prestação de Conta");
		// Map<String, Object> opcoes = new HashMap<String, Object>();
		// opcoes.put("modal", true);
		// opcoes.put("resizable", false);
		// opcoes.put("contentHeight", 470);

		RequestContext.getCurrentInstance().execute("PF('prestDialog').show()");
	}

	public void visualizarDetalhe(String index) {
		if (index != null) {
			int indice = Integer.parseInt(index);
			switch (indice) {
			case 0:
				setRenderedLiq(true);
				return;
			case 1:
				setRenderedPag(true);
				return;
			case 2:
				setRenderedPrest(true);
			}
		}
	}

	public void fecharDetalhe(String index) {
		if (index != null) {
			int indice = Integer.parseInt(index);
			switch (indice) {
			case 0:
				setRenderedLiq(false);
				return;
			case 1:
				setRenderedPag(false);
				return;
			case 2:
				setRenderedPrest(false);
			}
		}
	}

	private String[] getValoresEmpenhoLiquidacao(long numeroEmpenho,
			int exercicio) {

		String[] valores = new String[3];
		BigDecimal valorRepasse = BigDecimal.ZERO, valorPrestConta = BigDecimal.ZERO;

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("numeroEmpenho", numeroEmpenho);
		params.put("exercicio", exercicio);
		params.put("numeroLiquidacaoZero", false);
		List<Repasse> repasses = repasseService
				.buscarPorNumLiquidacaoAsc(params);
		for (Repasse repasse : repasses) {
			valorRepasse = valorRepasse.add(repasse.getValorRepasse());
			valorPrestConta = valorPrestConta.add(calcularPrestContas(repasse));
		}

		valores[0] = UtilsModel.convertBigDecimalToString(valorRepasse);
		valores[1] = UtilsModel.convertBigDecimalToString(valorPrestConta);
		valores[2] = UtilsModel.convertBigDecimalToString(valorRepasse
				.subtract(valorPrestConta));
		return valores;
	}

	private BigDecimal calcularPrestContas(Repasse repasse) {
		BigDecimal valor = BigDecimal.ZERO;
		if (repasse.getPrestacaoConta() != null
				&& repasse.getPrestacaoConta().isFinalizada()) {
			valor = repasse.getPrestacaoConta().getValor();
			liqPrestContaMap.put(repasse.getNumeroLiquidacao(),
					valor == null ? BigDecimal.ZERO : repasse
							.getPrestacaoConta().getValor());
		}
		liqSaldoMap.put(repasse.getNumeroLiquidacao(),
				repasse.getValorRepasse());

		return valor;
	}

	private BigDecimal obterSaldoAcumulado(long numero) {

		BigDecimal saldoAnterior = BigDecimal.ZERO;
		for (long i = 1; i <= numero; i++) {
			if (liqSaldoMap.get(i) != null)
				saldoAnterior = saldoAnterior.add(liqSaldoMap.get(i));
		}
		return saldoAnterior;
	}

	public void mostrarDialogPrestConta() {
		calendar.setTime(new Date());
		calendar.set(Calendar.DAY_OF_MONTH,
				calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

		// verifica se a data da prestação de conta é posterior a data, se for é
		// aberto um dialog para confirmação do registro
		if (UtilsModel.removerHorarioData(new Date()).after(
				UtilsModel.removerHorarioData(calendar.getTime()))) {
			setMessagemPrestacaoContas("Prestação de contas realizada fora do prazo sujeita o responsável às penalidades da lei.");

			RequestContext.getCurrentInstance().execute(
					"PF('confirmFimDialogAtrasado').show()");
			return;
		}
		if (possuiDocAtrasado(prestacaoConta)) {
			RequestContext.getCurrentInstance().execute(
					"PF('confirmDialogDocAtrasado').show()");
			return;
		}

		RequestContext.getCurrentInstance().execute(
				"PF('confirmFinalizarDialog').show()");

		setMessagemPrestacaoContas("");
	}

	private boolean possuiDocAtrasado(PrestacaoConta prestacaoConta) {

		if (prestacaoConta.getDocumentos() != null
				&& !prestacaoConta.getDocumentos().isEmpty())
			for (Documento doc : prestacaoConta.getDocumentos()) {
				if (UtilsModel.removerHorarioData(
						prestacaoConta.getDataPrestacao()).before(
						UtilsModel.removerHorarioData(doc.getData())))
					return true;
			}
		return false;
	}

	public void registrar() {

		// if (true) {
		// addErrorMessage("Esta funcionalidade ainda não está disponível!");
		// return;
		// }

		if (!confirmaDocAtrasado && !possuiDocumentosValidos(getDocumentos()))
			return;

		List<Documento> asList = new ArrayList<Documento>(getDocumentos());
		prestacaoConta.setDocumentos(asList);
		try {
			prestacaoConta.setDataPrestacao(new Date());
			prestacaoConta.setValor(UtilsModel
					.convertStringToBigDecimal(getValorTotal()));
			prestacaoConta.setSaldoDisponivel(repasse.getValorRepasse()
					.add(repasse.getSaldoAberto())
					.subtract(prestacaoConta.getValor()));
			prestacaoConta.setFinalizada(true);
			prestacaoConta.setRepasse(repasse);

			ResultSet resultSet = createStatement
					.executeQuery(String
							.format("SELECT DISTINCT p.nu_pagamento, p.vl_pagamento, p.dt_pagamento FROM cgp.pagamento p WHERE p.nu_empenho = %d AND p.nu_liquidacao = %d AND p.cd_orgao = %s AND p.cd_unidade = %s AND p.cd_atividade_projeto = %s AND p.cd_fonte_recurso = %s AND p.nu_exercicio = %d",
									repasse.getNumeroEmpenho(),
									repasse.getNumeroLiquidacao(), repasse
											.getUnidade().getUnidadeContabil()
											.getAtividade().getCodOrgao(),
									repasse.getUnidade().getParametroRepasse()
											.getCodUnidade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodAtividade(), repasse
											.getUnidade().getParametroRepasse()
											.getCodFonteRecurso(),
									repasse.getExercicio()));

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(prestacaoConta.getDataPrestacao());

			long numPagamento = 0l;
			BigDecimal valor = BigDecimal.ZERO;
			Date data = null;
			while (resultSet.next()) {
				numPagamento = resultSet.getLong(1);
				valor = resultSet.getBigDecimal(2);
				data = resultSet.getDate(3);

				// DateTime dtOther = new
				// DateTime(DateTime.parse("2014-06-15T08:00:55Z"))
				DateTime dtPrestConta = new DateTime(
						prestacaoConta.getDataPrestacao()); // pega data e hora
															// atual
				DateTime dtLiq = new DateTime(data);
				Duration duracao = new Duration(dtLiq, dtPrestConta);
				if (duracao.getStandardDays() > 30) {
					addErrorMessage("A data de prestação de contas excedeu o prazo de 30 dias a partir da data do pagamento");
					return;
				}
				// calendar.setTime(date);
			}

			prestacaoConta.setNumeroPagamento(numPagamento);

			Repasse repasseSemPrestacao = getRepassesSemPrestacao().get(
					getRepassesSemPrestacao().size() - 1);
			calendar.setTime(repasseSemPrestacao.getDataEmissao());

			Repasse proxRepasse = new Repasse();
			// agendar próximo repasse com base última liquidação
			// definir data da próxima liquidação
			// paramsFeriado.put("dia", value);
			Map<String, Object> paramsFeriado = new HashMap<String, Object>();
			int dia = calendar.get(Calendar.DAY_OF_MONTH);
			int mes = (calendar.get(Calendar.MONTH) + 1);
			int ano = calendar.get(Calendar.YEAR);

			int diaUtil = obterDiaUtilMes(dia, mes);

			calendar.set(ano, mes, diaUtil);
			proxRepasse.setDataEmissao(calendar.getTime());
			proxRepasse.setAtivo(repasse.isAtivo());
			proxRepasse.setCodAplicacao(repasse.getCodAplicacao());
			proxRepasse.setExercicio(repasse.getExercicio());
			proxRepasse.setHistorico(repasse.getHistorico());
			proxRepasse.setNaturezaEmpenho(repasse.getNaturezaEmpenho());
			proxRepasse.setNumeroEmpenho(repasse.getNumeroEmpenho());
			proxRepasse.setNumeroProcesso(repasse.getNumeroProcesso());
			proxRepasse.setTipoCredito(repasse.getTipoCredito());
			proxRepasse.setTipoEmpenho(repasse.getTipoEmpenho());
			proxRepasse.setTipoMeta(repasse.getTipoMeta());
			proxRepasse.setUnidade(repasse.getUnidade());
			proxRepasse.setValorEmpenho(repasse.getValorEmpenho());
			proxRepasse.setDataEmpenho(repasse.getDataEmpenho());
			proxRepasse.setValorRepasse(repasse.getValorRepasse());
			proxRepasse.setSaldoAberto(repasse.getValorRepasse()
					.add(repasse.getSaldoAberto())
					.subtract(prestacaoConta.getValor()));

			// registro de uma prestação de contas
			PrestacaoConta prestConta = prestacaoContaService
					.salvar(prestacaoConta);

			// criar um repasse - empenho - liquidacao
			repasseService.repasseAutomatico(proxRepasse, proxRepasse
					.getUnidade().getResponsavel(), UtilsModel
					.convertStringToBigDecimal(getValorTotal()), prestConta
					.getValor(), null, repasseService
					.obterProximoNumeroEmpenho(proxRepasse.getUnidade()
							.getParametroRepasse().getCodUnidade()), false);

			repasse.setPrestacaoConta(prestConta);
			repasseService.atualizar(repasse);
			// preparndo dados para o relatório
			ArrayList<PrestacaoContaReportVO> contas = new ArrayList<PrestacaoContaReportVO>();
			contas.add(new PrestacaoContaReportVO());
			contas.get(0).setDocumentos(prestacaoConta.getDocumentos());
			contas.get(0).setEmpenhos(getEmpenhos());
			LiquidacaoVO liquidacaoVO = getLiquidacoes().get(
					repasse.getPrestacaoConta().getMesOrdinal());
			liquidacaoVO.setNumPrestConta(liquidacaoVO.getNumero() + "");

			BigDecimal valorLiquidacao = UtilsModel
					.convertStringToBigDecimal(liquidacaoVO.getValor());

			BigDecimal saldoTotal = valorLiquidacao.add(repasse
					.getSaldoAberto());

			BigDecimal saldoAtual = saldoTotal.subtract(prestacaoConta
					.getValor());

			liquidacaoVO.setSaldo(UtilsModel
					.convertBigDecimalToString(saldoAtual));
			liquidacaoVO.setValorPrestConta(UtilsModel
					.convertBigDecimalToString(prestacaoConta.getValor()));
			contas.get(0).setLiquidacoes(Arrays.asList(liquidacaoVO));

			generateReportStream(liquidacaoVO.getValor(),
					UtilsModel.convertBigDecimalToString(saldoTotal),
					UtilsModel.convertBigDecimalToString(saldoAtual),
					UtilsModel.convertBigDecimalToString(repasse
							.getSaldoAberto()), valorTotal,
					liquidacaoVO.getNumPrestConta(), dataPrestacao, proxRepasse
							.getUnidade().getResponsavel(), contas);

			getPrestacaoContaMedia().setValue("/reports/PrestacaoContas.pdf");

			carregarReportTab();

		} catch (Exception e) {
			prestacaoContaService.remover(prestacaoConta);
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao()
					+ e.getLocalizedMessage());
			return;
		}
		addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
		// AQUI DEVE-SE CARREGAR VALORES DE OUTRO REPASSE CASO O RESPONSÁVEL
		// TENHA
		setRepasseSemPrestacaoConta(false);
		// totalRepasse = saldoAberto = "R$ 0,00";
		// valorTotal = "R$ 0,00";
		// init();
	}

	private int obterDiaUtilMes(int dia, int mes) {

		Feriado feriado = feriadoService
				.buscarPorDiaMes((byte) dia, (byte) mes);
		if (feriado != null)
			return obterDiaUtilMes(dia + 1, mes);

		return dia;
	}

	private boolean possuiDocumentosValidos(List<Documento> documentos) {
		if (!UtilsModel.possuiValorValido(getDocumentos())) {
			addErrorMessage("Pelo menos um documento deve ser informado!");
			RequestContext.getCurrentInstance().scrollTo("messages");
			RequestContext.getCurrentInstance().execute("topo()");
			return false;
		}
		return true;
	}

	public void uploadDoArquivoEvent(FileUploadEvent event) {
		arquivoNomeFileUpload.setStyleClass(null);
		// if (event.getFile().getSize() > 3072000) {
		// arquivoNomeFileUpload.setValid(false);
		// addErrorMessage("Tamanho Máximo do Arquivo é de 3 MB");
		// return;
		// }
		this.arquivoUploadFile = event.getFile();
	}

	public void salvarArquivo() {
		if (possuiObrigatorioNulo(documento, valorDoc)) {
			return;
		}

		if (semLiquidacaoMes) {
			addErrorMessage("A Prestação de Contas para este mês já foi finalizada ou não existe!");
			return;
		}

		arquivoNomeFileUpload.setStyleClass(null);
		if (this.arquivoUploadFile == null
				|| this.arquivoUploadFile.getFileName() == null) {
			addErrorMessage("Pelo menos um arquivo de tipos (pdf,png,jpeg ou jpg) deve ser informado");
			arquivoNomeFileUpload.setValid(false);
			return;
		}

		documento.setTamanho(arquivoUploadFile.getSize());

		ServletContext servletContext = (ServletContext) FacesContext
				.getCurrentInstance().getExternalContext().getContext();
		String caminho = servletContext.getRealPath(File.separator);

		documento.setCaminho(caminho);
		documento.setArquivo(arquivoUploadFile.getContents());
		documento.setNome(arquivoUploadFile.getFileName());

		documento.setContentType(arquivoUploadFile.getContentType());
		documento.setAtivo(true);
		documento.setNomeOriginal(String.format("%s%s%s", caminho,
				File.separator, arquivoUploadFile.getFileName()));
		documento.setValor(UtilsModel.convertStringToBigDecimal(getValorDoc()));

		BigDecimal saldoBigDec = UtilsModel.convertStringToBigDecimal(
				getValorTotal()).add(documento.getValor());
		saldoDisponivel = UtilsModel.convertBigDecimalToString(repasse
				.getUnidade().getParametroRepasse().getValorRepasse()
				.subtract(saldoBigDec));

		if (saldoBigDec.compareTo(getRepasse().getUnidade()
				.getParametroRepasse().getValorRepasse()) > 0) {
			addErrorMessage("O Valor informado para o documento excede o Saldo a Prestar Contas!");
			RequestContext.getCurrentInstance().execute("topo()");
			// saldoDisponivel =
			// UtilsModel.convertBigDecimalToString(saldoBigDec
			// .add(documento.getValor()));
			return;
		}
		if (documento.getNumero() == null || "".equals(documento.getNumero()))
			documento.setNumero(gerarNumeroIncremental());

		valorTotal = UtilsModel.convertBigDecimalToString(UtilsModel
				.convertStringToBigDecimal(valorTotal)
				.add(documento.getValor()));
		documentos.add(documento);
		// adicionando ao mapa o array de bytes mapeadas pelo numero do
		// documento
		arquivoByteMap.put(documento.getNumero(), documento.getArquivo());

		prestacaoConta.setDocumentos(documentos);
		prestacaoConta.setSaldoDisponivel(UtilsModel
				.convertStringToBigDecimal(getSaldoDisponivel()));
		prestacaoConta.setDataPrestacao(new Date());
		prestacaoConta.setValor(UtilsModel
				.convertStringToBigDecimal(getValorTotal()));
		prestacaoConta.setFinalizada(false);
		prestacaoConta.setRepasse(repasse);

		if (prestacaoConta.getId() != null && prestacaoConta.getId() > 0)
			prestacaoConta = prestacaoContaService.atualizar(prestacaoConta);
		else
			prestacaoConta = prestacaoContaService.salvar(prestacaoConta);

		for (Documento doc : prestacaoConta.getDocumentos()) {
			escreverArquivo(doc, arquivoByteMap.get(doc.getNumero()));
		}

		prestacaoConta = prestacaoContaService.atualizar(prestacaoConta);

		// documentoModel.add(documento);

		documento = new Documento();
		arquivoUploadFile = new UploadedFileUtil();
		valorDoc = new String();
		RequestContext.getCurrentInstance().update(
				"prestacaoContaTabView:prestacaoContaForm");
		// FacesContext context = FacesContext.getCurrentInstance();
		// ServletContext servletContext = (ServletContext) context
		// .getExternalContext().getContext();
		// File file1 = new
		// File(servletContext.getRealPath("/resources/images/"),
		// getArquivoUploadFile().getFileName());
		// try {
		// FileOutputStream fos = new FileOutputStream(file1);
		// fos.write(getArquivoUploadFile().getContents());
		// fos.close();
		//
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	private boolean possuiObrigatorioNulo(Documento documento, String valorDoc) {

		boolean possuiObrigatorioNulo = false;
		prestContaData.setStyleClass(null);
		tipoDoc.setStyleClass(null);
		valorDocText.setStyleClass(null);
		if (documento.getData() == null) {
			prestContaData.setStyleClass(" ui-state-error");
			addErrorMessage("Campo Data: O preenchimento é obrigatório.");
			possuiObrigatorioNulo = true;
		}
		if (documento.getTipo() == null || "".equals(documento.getTipo())) {
			tipoDoc.setStyleClass(" ui-state-error");
			addErrorMessage("Campo Tipo Doc: O preenchimento é obrigatório.");
			possuiObrigatorioNulo = true;
		}
		if (valorDoc == null || "".equals(valorDoc)) {
			valorDocText.setStyleClass(" ui-state-error");
			addErrorMessage("Campo Valor: O preenchimento é obrigatório.");
			possuiObrigatorioNulo = true;
		}
		return possuiObrigatorioNulo;
	}

	private void escreverArquivo(Documento documento, byte[] arquivo) {

		if (arquivo != null && arquivo != null) {
			try {
				if (documento.getId() != null) {
					StringBuilder filePath = new StringBuilder();
					String caminho = documento.getCaminho();
					if (!caminho.contains(documento.getId() + "")) {
						filePath.append(caminho).append(File.separator)
								.append("reports").append(File.separator);
						caminho = filePath.append(documento.getId())
								.append(File.separator).toString();
					}
					if (documento.getId() != 0l
							&& !Paths.get(caminho).toFile().exists()) {
						new File(caminho).mkdir();
					}

					documento.setCaminho(caminho);
					// atualizar a prestação para atualizar o caminho do
					// documento e
					// nao
					// somente o documento
					// documentoRepository.save(documento);

					File file = new File(caminho.concat(documento.getNome())
							.toString());

					FileOutputStream in = new FileOutputStream(file);
					in.write(arquivo);
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				addErrorMessage("Ocorreu um erro durante a visualização do arquivo!");
			}
		}
	}

	public void visualizarArquivo() {

		getPdfView().setValue(null);
		if (documentoExibe.getNome().contains("pdf")) {
			String[] split = documentoExibe.getCaminho().split("\\/");
			// escreverArquivo(documentoExibe, 0l);documentoExibe
			setImageStream(new DefaultStreamedContent());
			getPdfView()
					.setValue(
							File.separator.concat("reports".concat(File.separator
									.concat(split[split.length - 1]
											.concat(File.separator
													.concat(documentoExibe
															.getNome()))))));
			// setReportStream(defaultStreamedContent);
		} else {
			InputStream resourceAsStream = null;
			try {
				resourceAsStream = new ByteArrayInputStream(
						imageToByte(documentoExibe.getCaminho().concat(
								documentoExibe.getNome())));
			} catch (IOException e) {
				e.printStackTrace();
			}

			DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent(
					resourceAsStream, documentoExibe.getContentType());
			// setReportStream(null);
			setImageStream(new DefaultStreamedContent());
			setImageStream(defaultStreamedContent);
		}
		RequestContext.getCurrentInstance().update("imageStreamDialog");
		RequestContext.getCurrentInstance().execute(
				"PF('imageStreamDialog').show()");

	}

	private byte[] imageToByte(String image) throws IOException {
		InputStream is = null;
		byte[] buffer = null;
		is = new FileInputStream(image);
		buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		return buffer;
	}

	/**
	 * @description gera um número incremental par ao documento do qual o número
	 *              não foi informado, deve-se seguir o padrão diames-001 e
	 *              assim incrementando
	 * @return String
	 */
	private String gerarNumeroIncremental() {
		Calendar date = Calendar.getInstance();
		date.setTime(new Date());

		if (!documentos.isEmpty()) {
			Integer maiorNumero = 1;
			ArrayList<String> numeros = new ArrayList<String>();
			for (Documento documento : documentos) {
				if (documento.getNumero().contains("-")) {
					String[] split = documento.getNumero().split("-");
					String numeroIncrem = split[1].trim();
					if (!"".equals(numeroIncrem)) {
						numeros.add(numeroIncrem);
					}
				}
			}
			if (!numeros.isEmpty()) {
				Collections.sort(numeros);
				maiorNumero = Integer.parseInt(numeros.get(numeros.size() - 1));
				maiorNumero = maiorNumero + 1;
			}
			return String.format("%s%s - 00%d", StringUtils.leftPad(
					date.get(Calendar.DAY_OF_MONTH) + "", 2, "0"), StringUtils
					.leftPad((date.get(Calendar.MONTH) + 1) + "", 2, "0"),
					maiorNumero);
		} else
			return String.format("%s%s - 001", StringUtils.leftPad(
					date.get(Calendar.DAY_OF_MONTH) + "", 2, "0"), StringUtils
					.leftPad((date.get(Calendar.MONTH) + 1) + "", 2, "0"));
	}

	public void editarDoc() {
		setDocumento(documentoEdite);
		// documentoModel.remove(documentoEdite);
		valorTotal = UtilsModel.convertBigDecimalToString(UtilsModel
				.convertStringToBigDecimal(valorTotal).subtract(
						documentoEdite.getValor()));
		saldoDisponivel = UtilsModel.convertBigDecimalToString(UtilsModel
				.convertStringToBigDecimal(saldoDisponivel).add(
						documentoEdite.getValor()));
		setValorDoc(converterForCurrency(documentoEdite.getValor().toString()));
		arquivoUploadFile = new UploadedFileUtil(documentoEdite.getTamanho(),
				documentoEdite.getNome(), null, documentoEdite.getArquivo(),
				documentoEdite.getTipo());
	}

	public void removerDoc() {
		Calendar date = Calendar.getInstance();
		date.setTime(new Date());

		valorTotal = UtilsModel.convertBigDecimalToString(UtilsModel
				.convertStringToBigDecimal(valorTotal).subtract(
						documentoRemove.getValor()));
		saldoDisponivel = UtilsModel.convertBigDecimalToString(UtilsModel
				.convertStringToBigDecimal(saldoDisponivel).add(
						documentoRemove.getValor()));

		if (!documentos.isEmpty()) {
			Integer maiorNumero = 1;
			ArrayList<Documento> docs = new ArrayList<Documento>();
			Iterator<Documento> itDocs = documentos.iterator();
			while (itDocs.hasNext()) {
				Documento nextDoc = itDocs.next();
				if (!deleteDir(new File(
						String.format("%s%s%s", nextDoc.getCaminho(),
								File.separator, nextDoc.getNome())))) {
					addErrorMessage("Houve um erro durante a remoção do documento! Contate o Suporte.");
					return;
				}
				if (nextDoc.getNumero().contains("-")) {
					itDocs.remove();
					nextDoc.setNumero(String.format("%s%s - 00%d",
							date.get(Calendar.DAY_OF_MONTH),
							date.get(Calendar.MONTH) + 1, maiorNumero));
					docs.add(nextDoc);
					maiorNumero = maiorNumero + 1;
				} else
					docs.add(nextDoc);
			}

			documentos.addAll(docs);
			// documentoModel = new DocumentoModel();
			// documentoModel = new DocumentoModel(documentos);
		}

	}

	public static boolean deleteDir(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// Agora o diretório está vazio, restando apenas deletá-lo.
		return dir.delete();
	}

	public void generateReportStream(String totalRepasse, String saldoTotal,
			String saldoAtual, String saldoAnterior, String valor,
			String numeroPrestConta, Date dataPrestacao, Responsavel credor,
			List<PrestacaoContaReportVO> contas) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		FacesContext context = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) context
				.getExternalContext().getContext();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("valor", valor);
		parameters.put("repasse", totalRepasse);
		parameters.put("saldoAtual", saldoAtual);
		parameters.put("saldoTotal", saldoTotal);
		parameters.put("saldoAnterior", saldoAnterior);
		parameters.put("numeroPrestConta", numeroPrestConta);
		parameters.put("dataPrestacao", sdf.format(dataPrestacao));
		parameters.put(
				"agencia",
				String.format("%s-%s", credor.getNumeroAgencia(),
						credor.getDigitoConta()));
		parameters.put(
				"conta",
				String.format("%s-%s", credor.getNumeroConta(),
						credor.getDigitoAgencia()));
		parameters.put("local",
				"Parnaíba - PI, ".concat(sdf.format(dataPrestacao)));
		parameters.put("prefeitura", "Prefeitura Municipal de Parnaíba");
		// String url = context.getExternalContext().getRequestContextPath();

		FileInputStream logoInputStream = null;
		try {
			logoInputStream = new FileInputStream(
					new File(
							servletContext
									.getRealPath("/resources/images/bandeira-parnaiba.300x225.jpg")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		parameters.put("logo", logoInputStream);

		// contas.add(prestacaoConta);
		generateStremedReport(servletContext.getRealPath(File.separator.concat(
				"reports").concat(File.separator)), "PrestacaoContas",
				"PrestacaoContas", contas, parameters);
	}

	private String converterForCurrency(String valor) {
		return valor = valor.replace("R$", "").replace(".", ",").trim();
	}

	public List<Repasse> autocompleteRepasse(String valor) {
		return repasseService.autocompletar(valor);
	}

	public void carregarReportTab() {
		getEditTab().setRendered(true);
		TabView parent = (TabView) getEditTab().getParent();
		int findIndex = parent.getChildren().indexOf(getEditTab());
		parent.setActiveIndex(findIndex);
	}

	public void limparRelatorio() {
		getPrestacaoContaMedia().setValue(null);
	}

	public String getLiqStyle(int index) {
		int indice = 0;
		// if (index == null)
		// indice = liquidacoes.indexOf(liqSelecionado);
		// else
		indice = index;
		if (prestContaTable.get(0).getItens().size() > indice) {
			ItemPrestacaoContaVO itemVO = prestContaTable.get(0).getItens()
					.get(indice);
			if (itemVO != null)
				if (StatusItemEnum.REJEITADA.equals(itemVO.getStatus()))
					return "backRed";
			if (StatusItemEnum.APROVADA.equals(itemVO.getStatus()))
				return "backGreen";
		}
		return "backBlue";
	}

	public String getPagStyle(int index) {
		int indice = 0;
		// if (index == null)
		// indice = liquidacoes.indexOf(liqSelecionado);
		// else
		indice = index;
		if (getPagamentos().size() > indice) {
			PagamentoVO itemVO = getPagamentos().get(indice);
			if (itemVO != null)
				if (StatusItemEnum.REJEITADA.equals(itemVO.getStatus()))
					return "backRed";
			if (StatusItemEnum.APROVADA.equals(itemVO.getStatus()))
				return "backGreen";
		}
		return "backBlue";
	}

	public String getPrestStyle(int index) {
		int indice = 0;
		// if (index == null)
		// indice = liquidacoes.indexOf(liqSelecionado);
		// else
		indice = index;
		if (getPrestacaoContas().size() > indice) {
			PrestacaoConta item = getPrestacaoContas().get(indice);
			if (item != null)
				if (StatusItemEnum.REJEITADA.equals(item.getStatus()))
					return "backRed";
			if (StatusItemEnum.APROVADA.equals(item.getStatus()))
				return "backGreen";
		}
		return "backBlue";
	}

	public PrestacaoContaService getPrestacaoContaService() {
		return prestacaoContaService;
	}

	public void setPrestacaoContaService(
			PrestacaoContaService prestacaoContaService) {
		this.prestacaoContaService = prestacaoContaService;
	}

	public ResponsavelService getResponsavelService() {
		return responsavelService;
	}

	public void setResponsavelService(ResponsavelService responsavelService) {
		this.responsavelService = responsavelService;
	}

	public RepasseService getRepasseService() {
		return repasseService;
	}

	public void setRepasseService(RepasseService repasseService) {
		this.repasseService = repasseService;
	}

	public Date getDataPrestacao() {
		return dataPrestacao;
	}

	public void setDataPrestacao(Date dataPrestacao) {
		this.dataPrestacao = dataPrestacao;
	}

	public String getSaldoAberto() {
		return saldoAberto;
	}

	public void setSaldoAberto(String saldoAberto) {
		this.saldoAberto = saldoAberto;
	}

	public String getSaldoDisponivel() {
		return saldoDisponivel;
	}

	public void setSaldoDisponivel(String saldoDisponivel) {
		this.saldoDisponivel = saldoDisponivel;
	}

	public UploadedFile getArquivoUploadFile() {
		return arquivoUploadFile;
	}

	public void setArquivoUploadFile(UploadedFile arquivoUploadFile) {
		this.arquivoUploadFile = arquivoUploadFile;
	}

	public InputText getArquivoNomeFileUpload() {
		return arquivoNomeFileUpload;
	}

	public void setArquivoNomeFileUpload(InputText arquivoNomeFileUpload) {
		this.arquivoNomeFileUpload = arquivoNomeFileUpload;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public List<Documento> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<Documento> documentos) {
		// if (documentos != null) {
		// documentoModel = new DocumentoModel(documentos);
		// setDocumentosSelected(documentos);
		// }
		BigDecimal valor = BigDecimal.ZERO;
		for (Documento doc : documentos)
			valor = valor.add(doc.getValor());
		setValorTotal(UtilsModel.convertBigDecimalToString(valor));
		this.documentos = documentos;
	}

	public PrestacaoConta getPrestacaoConta() {
		return prestacaoConta;
	}

	public void setPrestacaoConta(PrestacaoConta prestacaoConta) {
		this.prestacaoConta = prestacaoConta;
	}

	public Documento getDocumentoEdite() {
		return documentoEdite;
	}

	public void setDocumentoEdite(Documento documentoEdite) {
		this.documentoEdite = documentoEdite;
	}

	public Documento getDocumentoRemove() {
		return documentoRemove;
	}

	public void setDocumentoRemove(Documento documentoRemove) {
		this.documentoRemove = documentoRemove;
	}

	public String getTotalRepasse() {
		return totalRepasse;
	}

	public void setTotalRepasse(String totalRepasse) {
		this.totalRepasse = totalRepasse;
	}

	public String getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(String valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getValorDoc() {
		return valorDoc;
	}

	public void setValorDoc(String valorDoc) {
		this.valorDoc = valorDoc;
	}

	public Repasse getRepasse() {
		return repasse;
	}

	public void setRepasse(Repasse repasse) {
		this.repasse = repasse;
	}

	public Repasse getRepasseProximo() {
		return repasseProximo;
	}

	public void setRepasseProximo(Repasse repasseProximo) {
		this.repasseProximo = repasseProximo;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public StreamedContent getReportStream() {
		return reportStream;
	}

	public void setReportStream(StreamedContent reportStream) {
		this.reportStream = reportStream;
	}

	public Media getPrestacaoContaMedia() {
		return prestacaoContaMedia;
	}

	public void setPrestacaoContaMedia(Media prestacaoContaMedia) {
		this.prestacaoContaMedia = prestacaoContaMedia;
	}

	public Documento getDocumentoExibe() {
		return documentoExibe;
	}

	public void setDocumentoExibe(Documento documentoExibe) {
		this.documentoExibe = documentoExibe;
	}

	public StreamedContent getImageStream() {
		return imageStream;
	}

	public void setImageStream(StreamedContent imageStream) {
		this.imageStream = imageStream;
	}

	public Media getPdfView() {
		return pdfView;
	}

	public void setPdfView(Media pdfView) {
		this.pdfView = pdfView;
	}

	public boolean isPossuiEmpenho() {
		return possuiEmpenho;
	}

	public void setPossuiEmpenho(boolean possuiEmpenho) {
		this.possuiEmpenho = possuiEmpenho;
	}

	public List<EmpenhoVO> getEmpenhos() {
		return empenhos;
	}

	public void setEmpenhos(List<EmpenhoVO> empenhos) {
		this.empenhos = empenhos;
	}

	public List<LiquidacaoVO> getLiquidacoes() {
		return liquidacoes;
	}

	public void setLiquidacoes(List<LiquidacaoVO> liquidacoes) {
		this.liquidacoes = liquidacoes;
	}

	public Map<Long, BigDecimal> getLiqPrestContaMap() {
		return liqPrestContaMap;
	}

	public void setLiqPrestContaMap(Map<Long, BigDecimal> liqPrestContaMap) {
		this.liqPrestContaMap = liqPrestContaMap;
	}

	public Map<Long, BigDecimal> getLiqSaldoMap() {
		return liqSaldoMap;
	}

	public void setLiqSaldoMap(Map<Long, BigDecimal> liqSaldoMap) {
		this.liqSaldoMap = liqSaldoMap;
	}

	public boolean isConfirmaDocAtrasado() {
		return confirmaDocAtrasado;
	}

	public void setConfirmaDocAtrasado(boolean confirmaDocAtrasado) {
		this.confirmaDocAtrasado = confirmaDocAtrasado;
	}

	public ConfirmDialog getConfirmFinalizarDialog() {
		return confirmFinalizarDialog;
	}

	public void setConfirmFinalizarDialog(ConfirmDialog confirmFinalizarDialog) {
		this.confirmFinalizarDialog = confirmFinalizarDialog;
	}

	// public DocumentoModel getDocumentoModel() {
	// return documentoModel;
	// }
	//
	// public void setDocumentoModel(DocumentoModel documentoModel) {
	// this.documentoModel = documentoModel;
	// }

	public String getMessagemPrestacaoContas() {
		return messagemPrestacaoContas;
	}

	public void setMessagemPrestacaoContas(String messagemPrestacaoContas) {
		this.messagemPrestacaoContas = messagemPrestacaoContas;
	}

	public List<PrestacaoContaTableVO> getPrestContaTable() {
		return prestContaTable;
	}

	public void setPrestContaTable(List<PrestacaoContaTableVO> prestContaTable) {
		this.prestContaTable = prestContaTable;
	}

	public Iterator<PrestacaoContaTableVO> getPrestContaIterator() {
		return prestContaIterator;
	}

	public void setPrestContaIterator(
			Iterator<PrestacaoContaTableVO> prestContaIterator) {
		this.prestContaIterator = prestContaIterator;
	}

	public CommandButton getCommandButOneLiq() {
		return commandButOneLiq;
	}

	public void setCommandButOneLiq(CommandButton commandButOneLiq) {
		this.commandButOneLiq = commandButOneLiq;
	}

	public Map<Integer, CommandButton> getCommandButMap() {
		return commandButMap;
	}

	public void setCommandButMap(Map<Integer, CommandButton> commandButMap) {
		this.commandButMap = commandButMap;
	}

	public LiquidacaoVO getLiqSelecionado() {
		return liqSelecionado;
	}

	public void setLiqSelecionado(LiquidacaoVO liqSelecionado) {
		this.liqSelecionado = liqSelecionado;
	}

	public String getItemSelected() {
		return itemSelected;
	}

	public void setItemSelected(String itemSelected) {
		this.itemSelected = itemSelected;
	}

	public List<PrestacaoConta> getPrestacaoContas() {
		return prestacaoContas;
	}

	public void setPrestacaoContas(List<PrestacaoConta> prestacaoContas) {
		this.prestacaoContas = prestacaoContas;
	}

	public List<PagamentoVO> getPagamentos() {
		return pagamentos;
	}

	public void setPagamentos(List<PagamentoVO> pagamentos) {
		this.pagamentos = pagamentos;
	}

	public PagamentoVO getPagSelecionado() {
		return pagSelecionado;
	}

	public void setPagSelecionado(PagamentoVO pagSelecionado) {
		this.pagSelecionado = pagSelecionado;
	}

	public PrestacaoConta getPrestSelecionado() {
		return prestSelecionado;
	}

	public void setPrestSelecionado(PrestacaoConta prestSelecionado) {
		this.prestSelecionado = prestSelecionado;
	}

	public Dialog getLiqDialog() {
		return liqDialog;
	}

	public void setLiqDialog(Dialog liqDialog) {
		this.liqDialog = liqDialog;
	}

	public boolean isRenderedLiq() {
		return renderedLiq;
	}

	public void setRenderedLiq(boolean renderedLiq) {
		this.renderedLiq = renderedLiq;
	}

	public boolean isRenderedPag() {
		return renderedPag;
	}

	public void setRenderedPag(boolean renderedPag) {
		this.renderedPag = renderedPag;
	}

	public boolean isRenderedPrest() {
		return renderedPrest;
	}

	public void setRenderedPrest(boolean renderedPrest) {
		this.renderedPrest = renderedPrest;
	}

	public List<Repasse> getRepassesSemPrestacao() {
		return repassesPrestacao;
	}

	public void setRepassesSemPrestacao(List<Repasse> repassesSemPrestacao) {
		this.repassesPrestacao = repassesSemPrestacao;
	}

	public FeriadoService getFeriadoService() {
		return feriadoService;
	}

	public void setFeriadoService(FeriadoService feriadoService) {
		this.feriadoService = feriadoService;
	}

	public UnidadeService getUnidadeService() {
		return unidadeService;
	}

	public void setUnidadeService(UnidadeService unidadeService) {
		this.unidadeService = unidadeService;
	}

	public InputText getValorDocText() {
		return valorDocText;
	}

	public void setValorDocText(InputText valorDocText) {
		this.valorDocText = valorDocText;
	}

	public InputText getTipoDoc() {
		return tipoDoc;
	}

	public void setTipoDoc(InputText tipoDoc) {
		this.tipoDoc = tipoDoc;
	}

	public org.primefaces.component.calendar.Calendar getPrestContaData() {
		return prestContaData;
	}

	public void setPrestContaData(
			org.primefaces.component.calendar.Calendar prestContaData) {
		this.prestContaData = prestContaData;
	}

	/**
	 * @author developer
	 * @descrição Inner Class representa o modelo que será manipulado na
	 *            datatable
	 */
	// final class DocumentoModel extends ListDataModel<Documento> implements
	// SelectableDataModel<Documento> {
	//
	// public DocumentoModel() {
	// }
	//
	// public DocumentoModel(List<Documento> docs) {
	// super(docs);
	// }
	//
	// public void add(Documento doc) {
	// List<Documento> docs = (List<Documento>) getWrappedData();
	// if (docs == null) {
	// docs = new ArrayList<Documento>();
	// docs.add(doc);
	// } else
	// docs.add(doc);
	// setWrappedData(docs);
	// }
	//
	// public void remove(Documento doc) {
	// List<Documento> docs = (List<Documento>) getWrappedData();
	// docs.remove(doc);
	// }
	//
	// @Override
	// public Documento getRowData(String rowKey) {
	//
	// List<Documento> docs = (List<Documento>) getWrappedData();
	//
	// for (Documento doc : docs) {
	// if (doc.getId().equals(rowKey))
	// return doc;
	// }
	// return null;
	// }
	//
	// @Override
	// public Object getRowKey(Documento doc) {
	//
	// return doc.getId();
	// }
	//
	// }
}