package br.com.sts.ddum.view.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.omnifaces.util.Ajax;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.confirmdialog.ConfirmDialog;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.media.Media;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.entities.Documento;
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
public class AuditarPrestacaoContaController extends BaseReportController {

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

	private Media pdfView0, pdfView1, pdfView2, pdfView3, pdfView4, pdfView5,
			pdfView6, pdfView7, pdfView8, pdfView9, pdfView10, pdfView11;

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

	private StreamedContent imageStream0, imageStream1, imageStream2,
			imageStream3, imageStream4, imageStream5, imageStream6,
			imageStream7, imageStream8, imageStream9, imageStream10,
			imageStream11;

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

	// Dados relacionados a auditoria da prestação de contas

	private List<String> prestContaTitulo;

	private PrestacaoConta[] prestacaoContasArray;

	private PrestacaoConta prestacaoContaAudit;

	private Tab auditarTabUm, auditarTabDois, auditarTabTres, auditarTabQuatro,
			auditarTabCinco, auditarTabSeis, auditarTabSete, auditarTabOito,
			auditarTabNove, auditarTabDez, auditarTabOnze, auditarTabDoze;

	private StatusItemEnum statusUm, statusDois, statusTres, statusQuatro,
			statusCinco, statusSeis, statusSete, statusOito, statusNove,
			statusDez, statusOnze, statusDoze;

	@PostConstruct
	public void init() {
		prestContaTitulo = popularTipoComNulos();
		arquivoByteMap = new HashMap<String, byte[]>();
		repassesPrestacao = new ArrayList<Repasse>();
		prestSelecionado = new PrestacaoConta();
		pagSelecionado = new PagamentoVO();
		liqSelecionado = new LiquidacaoVO();
		prestacaoContas = popularPrestContaComNulos();
		commandButMap = new HashMap<Integer, CommandButton>();
		commandButMap.put(1, commandButOneLiq);
		commandButOneLiq = new CommandButton();
		prestContaTable = new ArrayList<PrestacaoContaTableVO>();
		// imageStream0 = imageStream1 = imageStream2 = imageStream3 =
		// imageStream4 = imageStream5 = imageStream6 = imageStream7 =
		// imageStream8 = imageStream9 = imageStream10 = imageStream11 = new
		// DefaultStreamedContent();
		confirmaDocAtrasado = possuiEmpenho = false;
		empenhos = new ArrayList<EmpenhoVO>();
		liqSaldoMap = new HashMap<Long, BigDecimal>();
		liqPrestContaMap = new HashMap<Long, BigDecimal>();
		// pdfView0 = pdfView1 = pdfView2 = pdfView3 = pdfView4 = pdfView5 =
		// pdfView6 = pdfView7 = pdfView8 = pdfView9 = pdfView10 = pdfView11 =
		prestacaoContaMedia = new Media();
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

	public void carregar(int index) {
		if (index > 0)
			--index;
		Tab auditarTab = getTaByIndex(index);
		if (auditarTab != null) {
			auditarTab.setRendered(false);
			TabView parent = (TabView) auditarTab.getParent();
			int editIndex = parent.getChildren().indexOf(auditarTab);
			editIndex = obterRealIndex(parent, editIndex);
			auditarTab.setRendered(true);
			parent.setActiveIndex(editIndex);
			Ajax.update(parent.getId());
		}
		getPrestContaTitulo().remove(index);
		getPrestContaTitulo().add(index,
				String.format("%s %d", "Prest.Conta ", index + 1));
		getPrestacaoContas().add(index, prestacaoContaAudit);
		carregarStatus(index, prestacaoContaAudit.getStatus());
	}

	private void carregarStatus(int index, StatusItemEnum status) {

		switch (index) {
		case 0:
			setStatusUm(status);
			return;
		case 1:
			setStatusDois(status);
			return;
		case 2:
			setStatusTres(status);
			return;
		case 3:
			setStatusQuatro(status);
			return;
		case 4:
			setStatusCinco(status);
			return;
		case 5:
			setStatusSeis(status);
			return;
		case 6:
			setStatusSete(status);
			return;
		case 7:
			setStatusOito(status);
			return;
		case 8:
			setStatusNove(status);
			return;
		case 9:
			setStatusDez(status);
			return;
		case 10:
			setStatusOnze(status);
			return;
		case 11:
			setStatusDoze(status);
			return;
		}
	}

	private StatusItemEnum obterStatus(int indexPrestConta) {

		switch (indexPrestConta) {
		case 0:
			return getStatusUm();
		case 1:
			return getStatusDois();
		case 2:
			return getStatusTres();
		case 3:
			return getStatusQuatro();
		case 4:
			return getStatusCinco();
		case 5:
			return getStatusSeis();
		case 6:
			return getStatusSete();
		case 7:
			return getStatusOito();
		case 8:
			return getStatusNove();
		case 9:
			return getStatusDez();
		case 10:
			return getStatusOnze();
		case 11:
			return getStatusDoze();
		}
		return null;
	}

	private int obterRealIndex(UIComponent parent, int editIndex) {
		if (!parent.getChildren().get(editIndex - 1).isRendered())
			return (editIndex - 1) == 0 ? editIndex : obterRealIndex(parent,
					editIndex - 1);

		if (!parent.getChildren().get(editIndex).isRendered())
			return editIndex;

		if (!parent.getChildren().get(editIndex + 1).isRendered())
			return editIndex + 1;

		return editIndex;

	}

	public void atualizar(int index) {
		try {
			PrestacaoConta prestacaoConta = getPrestacaoContas().get(index);

			prestacaoConta.setStatus(obterStatus(index));
			prestacaoContaService.atualizar(prestacaoConta);
		} catch (Exception e) {
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao());
			return;
		}
		addInfoMessage(ResultMessages.UPDATE_SUCESS.getDescricao());
		closeTab(index);
	}

	public void closeTab(int index) {
		Tab tabClose = getTaByIndex(index);
		if (tabClose != null) {
			tabClose.setRendered(false);
			TabView parent = (TabView) tabClose.getParent();
			int editIndex = parent.getChildren().indexOf(tabClose);
			// parent.getChildren().remove(editIndex);
			if (editIndex > 0)
				parent.setActiveIndex(editIndex - 1);
			Ajax.update(parent.getId());

		}
	}

	private Tab getTaByIndex(int index) {
		switch (index) {
		case 0:
			return getAuditarTabUm();
		case 1:
			return getAuditarTabDois();
		case 2:
			return getAuditarTabTres();
		case 3:
			return getAuditarTabQuatro();
		case 4:
			return getAuditarTabCinco();
		case 5:
			return getAuditarTabSeis();
		case 6:
			return getAuditarTabSete();
		case 7:
			return getAuditarTabOito();
		case 8:
			return getAuditarTabNove();
		case 9:
			return getAuditarTabDez();
		case 10:
			return getAuditarTabOnze();
		case 11:
			return getAuditarTabDoze();

		}
		return null;
	}

	public void carregarDados(boolean cleanReport) {

		// redirect("/ReportData");
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
		List<Responsavel> responsaveis = responsavelService.buscar(params);
		if (responsaveis == null || responsaveis.isEmpty()) {
			addErrorMessage("A operação não pode ser realizada, pois o usuário não é um Responsável!");
			return;
		}

		Responsavel responsavel = responsaveis.get(0);

		params.clear();
		Calendar dataAtual = Calendar.getInstance();
		dataAtual.setTime(new Date());
		if (responsavel != null) {
			params.put("responsavel.id", responsavel.getId());
			List<Unidade> unidades = unidadeService.buscar(params);
			params.clear();
			params.put("unidade.responsavel.id", responsavel.getId());
			params.put("unidade.id", unidades.get(0).getId());
			params.put("prestacaoConta.finalizada", false);
			dataAtual.set(Calendar.DAY_OF_MONTH,
					dataAtual.getActualMaximum(Calendar.DAY_OF_MONTH));
			params.put("maxDate", dataAtual.getTime());
		}

		repassesPrestacao = repasseService.buscar(params);
		if (repassesPrestacao == null || repassesPrestacao.isEmpty()) {
			addErrorMessage("A operação não pode ser realizada, não existe Empenho para este Responsável!");
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
			addErrorMessage("Não existe Repasse Ativo para este Responsável!");
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
						: UtilsModel.convertBigDecimalToString(liqSaldoMap
								.get(liqVO.getNumero())));
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
			List<PrestacaoConta> prestContas = prestacaoContaService.buscar(
					repasse.getNumeroEmpenho(), liquidacoesIds);
			if (prestContas != null && !prestContas.isEmpty())
				for (PrestacaoConta prest : prestContas) {
					getPrestacaoContas().remove(prest.getMesOrdinal());
					getPrestacaoContas().add(prest.getMesOrdinal(), prest);
				}

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

			boolean contemPagamento = false;
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
			addErrorMessage(e.getLocalizedMessage());
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
						: UtilsModel.convertBigDecimalToString(liqSaldoMap
								.get(liqVO.getNumero())));
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

	private <T> List<T> popularTipoComNulos() {
		List<T> list = new ArrayList<T>();
		for (int x = 0; x <= 11; x++) {
			list.add(x, null);
		}
		return list;
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
		Map<String, Object> opcoes = new HashMap<String, Object>();
		opcoes.put("modal", true);
		opcoes.put("resizable", false);
		opcoes.put("contentHeight", 470);

		RequestContext.getCurrentInstance().execute("PF('pagDialog').show()");
	}

	public void showDialogPrest(String index) {

		int indice = Integer.parseInt(index);
		prestSelecionado = getPrestacaoContas().get(indice);
		setItemSelected("Prestação de Conta");
		Map<String, Object> opcoes = new HashMap<String, Object>();
		opcoes.put("modal", true);
		opcoes.put("resizable", false);
		opcoes.put("contentHeight", 470);

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
			BigDecimal saldoAnterior = liqSaldoMap.get(repasse
					.getNumeroLiquidacao() - 1);
			BigDecimal saldoAtual = repasse.getValorRepasse();
			// .subtract(repasse.getPrestacaoConta().getValor());
			liqSaldoMap.put(
					repasse.getNumeroLiquidacao(),
					saldoAnterior == null ? saldoAtual : saldoAnterior
							.add(saldoAtual));
		}
		return valor;
	}

	public void mostrarDialogPrestConta() {
		calendar.setTime(repasse.getDataEmissao());
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
		RequestContext.getCurrentInstance().execute(
				"PF('confirmFinalizarDialog').show()");

		setMessagemPrestacaoContas("");
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

	public void visualizarArquivo(int index) {

		Media obterRealMedia = obterRealMedia(index);
		if (obterRealMedia == null)
			obterRealMedia = new Media();
		obterRealMedia.setValue(null);
		if (documentoExibe.getNome().contains("pdf")) {
			StringBuilder localFile = new StringBuilder();
			String[] split = documentoExibe.getCaminho().split("\\/");
			// escreverArquivo(documentoExibe, 0l);documentoExibe
			injetarRealStream(index, new DefaultStreamedContent());
			String caminho = localFile.append(File.separator).append("reports")
					.append(File.separator).append(split[split.length - 1])
					.append(File.separator).append(documentoExibe.getNome())
					.toString();
			obterRealMedia.setValue(caminho);
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
			injetarRealStream(index, new DefaultStreamedContent());
			injetarRealStream(index, defaultStreamedContent);
		}
		String dialog = "imageStreamDialog" + index;
		Ajax.update(dialog);
		dialog = "PF('".concat(dialog).concat("').show()");
		RequestContext.getCurrentInstance().execute(dialog);

	}

	private Media obterRealMedia(int index) {

		switch (index) {
		case 0:
			return getPdfView0();
		case 1:
			return getPdfView1();
		case 2:
			return getPdfView2();
		case 3:
			return getPdfView3();
		case 4:
			return getPdfView4();
		case 5:
			return getPdfView5();
		case 6:
			return getPdfView6();
		case 7:
			return getPdfView7();
		case 8:
			return getPdfView8();
		case 9:
			return getPdfView9();
		case 10:
			return getPdfView10();
		case 11:
			return getPdfView11();
		}

		return getPdfView0();

	}

	private void injetarRealStream(int index, StreamedContent stream) {

		switch (index) {
		case 0:
			setImageStream0(stream);
			break;
		case 1:
			setImageStream1(stream);
			break;
		case 2:
			setImageStream2(stream);
			break;
		case 3:
			setImageStream3(stream);
			break;
		case 4:
			setImageStream4(stream);
			break;
		case 5:
			setImageStream5(stream);
			break;
		case 6:
			setImageStream6(stream);
			break;
		case 7:
			setImageStream7(stream);
			break;
		case 8:
			setImageStream8(stream);
			break;
		case 9:
			setImageStream9(stream);
			break;
		case 10:
			setImageStream10(stream);
			break;
		case 11:
			setImageStream11(stream);
		}

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

	public String getLiqStyle(String index) {
		int indice = 0;
		if (index == null)
			indice = liquidacoes.indexOf(liqSelecionado);
		else
			indice = Integer.parseInt(index);
		if (prestContaTable.get(0).getItens().size() > indice) {
			ItemPrestacaoContaVO itemVO = prestContaTable.get(0).getItens()
					.get(indice);
			if (itemVO != null
					&& !StatusItemEnum.APROVADA.equals(itemVO.getStatus()))
				return "backRed";
		}
		return "backGreen";
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

	public StreamedContent getImageStream0() {
		return imageStream0;
	}

	public void setImageStream0(StreamedContent imageStream0) {
		this.imageStream0 = imageStream0;
	}

	public StreamedContent getImageStream1() {
		return imageStream1;
	}

	public void setImageStream1(StreamedContent imageStream1) {
		this.imageStream1 = imageStream1;
	}

	public StreamedContent getImageStream2() {
		return imageStream2;
	}

	public void setImageStream2(StreamedContent imageStream2) {
		this.imageStream2 = imageStream2;
	}

	public StreamedContent getImageStream3() {
		return imageStream3;
	}

	public void setImageStream3(StreamedContent imageStream3) {
		this.imageStream3 = imageStream3;
	}

	public StreamedContent getImageStream4() {
		return imageStream4;
	}

	public void setImageStream4(StreamedContent imageStream4) {
		this.imageStream4 = imageStream4;
	}

	public StreamedContent getImageStream5() {
		return imageStream5;
	}

	public void setImageStream5(StreamedContent imageStream5) {
		this.imageStream5 = imageStream5;
	}

	public StreamedContent getImageStream6() {
		return imageStream6;
	}

	public void setImageStream6(StreamedContent imageStream6) {
		this.imageStream6 = imageStream6;
	}

	public StreamedContent getImageStream7() {
		return imageStream7;
	}

	public void setImageStream7(StreamedContent imageStream7) {
		this.imageStream7 = imageStream7;
	}

	public StreamedContent getImageStream8() {
		return imageStream8;
	}

	public void setImageStream8(StreamedContent imageStream8) {
		this.imageStream8 = imageStream8;
	}

	public StreamedContent getImageStream9() {
		return imageStream9;
	}

	public void setImageStream9(StreamedContent imageStream9) {
		this.imageStream9 = imageStream9;
	}

	public StreamedContent getImageStream10() {
		return imageStream10;
	}

	public void setImageStream10(StreamedContent imageStream10) {
		this.imageStream10 = imageStream10;
	}

	public StreamedContent getImageStream11() {
		return imageStream11;
	}

	public void setImageStream11(StreamedContent imageStream11) {
		this.imageStream11 = imageStream11;
	}

	public Media getPdfView0() {
		return pdfView0;
	}

	public void setPdfView0(Media pdfView0) {
		this.pdfView0 = pdfView0;
	}

	public Media getPdfView1() {
		return pdfView1;
	}

	public void setPdfView1(Media pdfView1) {
		this.pdfView1 = pdfView1;
	}

	public Media getPdfView2() {
		return pdfView2;
	}

	public void setPdfView2(Media pdfView2) {
		this.pdfView2 = pdfView2;
	}

	public Media getPdfView3() {
		return pdfView3;
	}

	public void setPdfView3(Media pdfView3) {
		this.pdfView3 = pdfView3;
	}

	public Media getPdfView4() {
		return pdfView4;
	}

	public void setPdfView4(Media pdfView4) {
		this.pdfView4 = pdfView4;
	}

	public Media getPdfView5() {
		return pdfView5;
	}

	public void setPdfView5(Media pdfView5) {
		this.pdfView5 = pdfView5;
	}

	public Media getPdfView6() {
		return pdfView6;
	}

	public void setPdfView6(Media pdfView6) {
		this.pdfView6 = pdfView6;
	}

	public Media getPdfView7() {
		return pdfView7;
	}

	public void setPdfView7(Media pdfView7) {
		this.pdfView7 = pdfView7;
	}

	public Media getPdfView8() {
		return pdfView8;
	}

	public void setPdfView8(Media pdfView8) {
		this.pdfView8 = pdfView8;
	}

	public Media getPdfView9() {
		return pdfView9;
	}

	public void setPdfView9(Media pdfView9) {
		this.pdfView9 = pdfView9;
	}

	public Media getPdfView10() {
		return pdfView10;
	}

	public void setPdfView10(Media pdfView10) {
		this.pdfView10 = pdfView10;
	}

	public Media getPdfView11() {
		return pdfView11;
	}

	public void setPdfView11(Media pdfView11) {
		this.pdfView11 = pdfView11;
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

	public List<String> getPrestContaTitulo() {
		return prestContaTitulo;
	}

	public void setPrestContaTitulo(List<String> prestContaTitulo) {
		this.prestContaTitulo = prestContaTitulo;
	}

	public PrestacaoConta[] getPrestacaoContasArray() {
		return prestacaoContasArray;
	}

	public void setPrestacaoContasArray(PrestacaoConta[] prestacaoContasArray) {
		this.prestacaoContasArray = prestacaoContasArray;
	}

	public PrestacaoConta getPrestacaoContaAudit() {
		return prestacaoContaAudit;
	}

	public void setPrestacaoContaAudit(PrestacaoConta prestacaoContaAudit) {
		this.prestacaoContaAudit = prestacaoContaAudit;
	}

	public Tab getAuditarTabUm() {
		return auditarTabUm;
	}

	public void setAuditarTabUm(Tab auditarTabUm) {
		this.auditarTabUm = auditarTabUm;
	}

	public Tab getAuditarTabDois() {
		return auditarTabDois;
	}

	public void setAuditarTabDois(Tab auditarTabDois) {
		this.auditarTabDois = auditarTabDois;
	}

	public Tab getAuditarTabTres() {
		return auditarTabTres;
	}

	public void setAuditarTabTres(Tab auditarTabTres) {
		this.auditarTabTres = auditarTabTres;
	}

	public Tab getAuditarTabQuatro() {
		return auditarTabQuatro;
	}

	public void setAuditarTabQuatro(Tab auditarTabQuatro) {
		this.auditarTabQuatro = auditarTabQuatro;
	}

	public Tab getAuditarTabCinco() {
		return auditarTabCinco;
	}

	public void setAuditarTabCinco(Tab auditarTabCinco) {
		this.auditarTabCinco = auditarTabCinco;
	}

	public Tab getAuditarTabSeis() {
		return auditarTabSeis;
	}

	public void setAuditarTabSeis(Tab auditarTabSeis) {
		this.auditarTabSeis = auditarTabSeis;
	}

	public Tab getAuditarTabSete() {
		return auditarTabSete;
	}

	public void setAuditarTabSete(Tab auditarTabSete) {
		this.auditarTabSete = auditarTabSete;
	}

	public Tab getAuditarTabOito() {
		return auditarTabOito;
	}

	public void setAuditarTabOito(Tab auditarTabOito) {
		this.auditarTabOito = auditarTabOito;
	}

	public Tab getAuditarTabNove() {
		return auditarTabNove;
	}

	public void setAuditarTabNove(Tab auditarTabNove) {
		this.auditarTabNove = auditarTabNove;
	}

	public Tab getAuditarTabDez() {
		return auditarTabDez;
	}

	public void setAuditarTabDez(Tab auditarTabDez) {
		this.auditarTabDez = auditarTabDez;
	}

	public Tab getAuditarTabOnze() {
		return auditarTabOnze;
	}

	public void setAuditarTabOnze(Tab auditarTabOnze) {
		this.auditarTabOnze = auditarTabOnze;
	}

	public Tab getAuditarTabDoze() {
		return auditarTabDoze;
	}

	public void setAuditarTabDoze(Tab auditarTabDoze) {
		this.auditarTabDoze = auditarTabDoze;
	}

	public StatusItemEnum getStatusUm() {
		return statusUm;
	}

	public void setStatusUm(StatusItemEnum statusUm) {
		this.statusUm = statusUm;
	}

	public StatusItemEnum getStatusDois() {
		return statusDois;
	}

	public void setStatusDois(StatusItemEnum statusDois) {
		this.statusDois = statusDois;
	}

	public StatusItemEnum getStatusTres() {
		return statusTres;
	}

	public void setStatusTres(StatusItemEnum statusTres) {
		this.statusTres = statusTres;
	}

	public StatusItemEnum getStatusQuatro() {
		return statusQuatro;
	}

	public void setStatusQuatro(StatusItemEnum statusQuatro) {
		this.statusQuatro = statusQuatro;
	}

	public StatusItemEnum getStatusCinco() {
		return statusCinco;
	}

	public void setStatusCinco(StatusItemEnum statusCinco) {
		this.statusCinco = statusCinco;
	}

	public StatusItemEnum getStatusSeis() {
		return statusSeis;
	}

	public void setStatusSeis(StatusItemEnum statusSeis) {
		this.statusSeis = statusSeis;
	}

	public StatusItemEnum getStatusSete() {
		return statusSete;
	}

	public void setStatusSete(StatusItemEnum statusSete) {
		this.statusSete = statusSete;
	}

	public StatusItemEnum getStatusOito() {
		return statusOito;
	}

	public void setStatusOito(StatusItemEnum statusOito) {
		this.statusOito = statusOito;
	}

	public StatusItemEnum getStatusNove() {
		return statusNove;
	}

	public void setStatusNove(StatusItemEnum statusNove) {
		this.statusNove = statusNove;
	}

	public StatusItemEnum getStatusDez() {
		return statusDez;
	}

	public void setStatusDez(StatusItemEnum statusDez) {
		this.statusDez = statusDez;
	}

	public StatusItemEnum getStatusOnze() {
		return statusOnze;
	}

	public void setStatusOnze(StatusItemEnum statusOnze) {
		this.statusOnze = statusOnze;
	}

	public StatusItemEnum getStatusDoze() {
		return statusDoze;
	}

	public void setStatusDoze(StatusItemEnum statusDoze) {
		this.statusDoze = statusDoze;
	}
}