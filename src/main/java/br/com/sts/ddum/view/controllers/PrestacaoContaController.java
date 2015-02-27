package br.com.sts.ddum.view.controllers;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.omnifaces.util.Ajax;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.entities.Documento;
import br.com.sts.ddum.model.entities.PrestacaoConta;
import br.com.sts.ddum.model.entities.Repasse;
import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.ConnectionConfigService;
import br.com.sts.ddum.service.interfaces.PrestacaoContaService;
import br.com.sts.ddum.service.interfaces.RepasseService;
import br.com.sts.ddum.service.interfaces.ResponsavelService;
import br.com.sts.ddum.service.interfaces.UserService;
import br.com.sts.ddum.view.uploads.UploadedFileUtil;
import br.com.sts.ddum.view.utils.UtilsView;

@Controller
@ViewScoped
public class PrestacaoContaController extends BaseController {

	private static final long serialVersionUID = -3922297442654647722L;

	@Autowired
	private PrestacaoContaService prestacaoContaService;

	@Autowired
	private UserService userService;

	@Autowired
	private RepasseService repasseService;

	@Autowired
	private ResponsavelService responsavelService;

	@Autowired
	private ConnectionConfigService connectionConfigService;

	private PrestacaoConta prestacaoConta;

	private Date dataPrestacao;

	private BigDecimal saldoAberto;

	private BigDecimal totalRepasse;

	private String valorDoc;

	private BigDecimal valorTotal;

	private UploadedFile arquivoUploadFile;

	private InputText arquivoNomeFileUpload;

	private Documento documento;
	private Documento documentoEdite;
	private Documento documentoRemove;

	private List<Documento> documentos;

	private Statement createStatement;
	private Connection conexaoBancoCGP;

	private Repasse repasse;
	private Repasse repasseProximo;

	@PostConstruct
	public void init() {
		dataPrestacao = new Date();
		arquivoUploadFile = new UploadedFileUtil();
		prestacaoConta = new PrestacaoConta();
		repasse = repasseProximo = new Repasse();
		if (repasse.getValorRepasse() == null
				|| repasse.getValorRepasse().compareTo(BigDecimal.ZERO) == 0) {
			totalRepasse = saldoAberto = new BigDecimal(10000);
		} else {
			totalRepasse = saldoAberto = repasse.getValorRepasse();
		}
		documento = documentoEdite = documentoRemove = new Documento();
		documentos = new ArrayList<Documento>();
		arquivoNomeFileUpload = new InputText();
		arquivoNomeFileUpload.setStyleClass(null);
		valorTotal = BigDecimal.ZERO;
		valorDoc = new String();
		conexaoBancoCGP = connectionConfigService.obterConexaoBancoCGP();
		Ajax.update(":prestacaoContaTabView");
	}

	public void setRepasseSemPrestacaoConta() {

		this.init();
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
		if (responsavel != null)
			params.put("unidade.responsavel.id", responsavel.getId());
		List<Repasse> repasses = repasseService.buscar(params);

		for (Repasse repasseItem : repasses) {
			params.clear();
			params.put("repasse.id", repasseItem.getId());
			if (prestacaoContaService.buscar(params).isEmpty()) {
				this.repasse = repasseItem;
				break;
			}
		}

	}

	public void registrar() {

		if (!UtilsModel.possuiValorValido(getDocumentos())) {
			addErrorMessage("Pelo menos um documento deve ser informado!");
			return;
		}

		// prestacaoConta.setRepasse(getRepasse());
		List<Documento> asList = new ArrayList<Documento>(getDocumentos());
		prestacaoConta.setDocumentos(asList);
		try {
			prestacaoConta.setSaldoDisponivel(getSaldoAberto());
			prestacaoContaService.salvar(prestacaoConta);
		} catch (Exception e) {
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao());
			return;
		}
		addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
		totalRepasse = saldoAberto = BigDecimal.TEN;
		valorTotal = BigDecimal.ZERO;
		init();
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

	public void adicionarArquivo() {
		arquivoNomeFileUpload.setStyleClass(null);
		if (this.arquivoUploadFile == null
				|| this.arquivoUploadFile.getFileName() == null) {
			addErrorMessage("Pelo menos um arquivo de tipos(pdf,png,jpeg ou jpg) deve ser informado");
			arquivoNomeFileUpload.setValid(false);
			return;
		}

		documento.setTamanho(arquivoUploadFile.getSize());
		String caminho = FacesContext.getCurrentInstance().getExternalContext()
				.getRealPath(File.separator);
		documento.setCaminho(caminho);
		documento.setArquivo(arquivoUploadFile.getContents());
		documento.setNome(arquivoUploadFile.getFileName());

		documento.setContentType(arquivoUploadFile.getContentType());
		documento.setAtivo(true);
		documento.setNomeOriginal(String.format("%s%s%s", caminho,
				File.separator, arquivoUploadFile.getFileName()));
		documento.setValor(UtilsModel.convertStringToBigDecimal(getValorDoc()));

		saldoAberto = saldoAberto.subtract(documento.getValor());
		if (saldoAberto.compareTo(BigDecimal.ZERO) < 0) {
			addErrorMessage("Saldo insuficiente para o valor informado!");
			saldoAberto = saldoAberto.add(documento.getValor());
			return;
		}

		valorTotal = valorTotal.add(documento.getValor());
		documentos.add(documento);
		documento = new Documento();
		arquivoUploadFile = new UploadedFileUtil();
		valorDoc = new String();
	}

	public void editarDoc() {
		setDocumento(documentoEdite);
		valorTotal = valorTotal.subtract(documentoEdite.getValor());
		saldoAberto = saldoAberto.add(documentoEdite.getValor());
		setValorDoc(converterForCurrency(documentoEdite.getValor().toString()));
		arquivoUploadFile = new UploadedFileUtil(documentoEdite.getTamanho(),
				documentoEdite.getNome(), null, documentoEdite.getArquivo(),
				documentoEdite.getTipo());
	}

	public void removerDoc() {
		valorTotal = valorTotal.subtract(documentoRemove.getValor());
		saldoAberto = saldoAberto.add(documentoRemove.getValor());
	}

	private String converterForCurrency(String valor) {
		return valor = valor.replace("R$", "").replace(".", ",").trim();
	}

	public List<Repasse> autocompleteRepasse(String valor) {
		return repasseService.autocompletar(valor);
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

	public BigDecimal getSaldoAberto() {
		return saldoAberto;
	}

	public void setSaldoAberto(BigDecimal saldoAberto) {
		this.saldoAberto = saldoAberto;
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

	public BigDecimal getTotalRepasse() {
		return totalRepasse;
	}

	public void setTotalRepasse(BigDecimal totalRepasse) {
		this.totalRepasse = totalRepasse;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
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
}
