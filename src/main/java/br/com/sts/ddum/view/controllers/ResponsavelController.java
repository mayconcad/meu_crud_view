package br.com.sts.ddum.view.controllers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;

import org.omnifaces.util.Ajax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.enums.QueryEnum;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.enums.TipoContaEnum;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.ConnectionConfigService;
import br.com.sts.ddum.service.interfaces.ResponsavelService;
import br.com.sts.ddum.view.utils.UtilsView;
import br.com.sts.ddum.view.utils.ValidateUtils;

@Controller
@ViewScoped
public class ResponsavelController extends BaseController {

	private static final long serialVersionUID = 3560165433007813089L;

	private enum BancoFolhaEnum {

		EFETIVOS("Efetivos"), COMISSIONADOS("Comissionados"), OUTROS("Outros");

		private String descricao;

		private BancoFolhaEnum(String descricao) {
			this.descricao = descricao;
		}

		@Override
		public String toString() {
			return this.descricao;
		}
	}

	protected static final String FIREBIRD_DRIVER = "org.firebirdsql.jdbc.FBDriver";
	protected static final String URL = String
			.format(
			// "jdbc:firebirdsql:192.10.0.10/3050:%shome%sfirebird%s",
			"jdbc:firebirdsql:192.168.1.2/3050:%shome%sfirebird%sgcs-efetivos-phb.fdb",
					File.separator, File.separator, File.separator);
	// "jdbc:firebirdsql:192.10.0.10/3050:%shome%sfirebird%s",
	protected static final String SENHA = "masterkey";
	protected static final String USUARIO = "SYSDBA";

	private final String MATR_CPF_NOME_RESP_QUERY = " WHERE (servidor.matricula_funcional LIKE '%%%s%%' OR LOWER(servidor.nome_servidor) LIKE '%%%s%%' OR servidor.cpf LIKE '%%%s%%') ";

	@Autowired
	private ResponsavelService responsavelService;

	@Autowired
	private ConnectionConfigService connectionConfigService;

	private Responsavel responsavel = new Responsavel();

	private List<Responsavel> responsaveis = new ArrayList<Responsavel>();

	private BancoFolhaEnum bancoFolha;
	private BancoFolhaEnum conexaoAtualBancoFolha;

	private Connection conexaoBanco = null;
	private Statement createStatement = null;
	private ResultSet result = null;

	@PostConstruct
	public void init() {
		responsavel = new Responsavel();
		setBancoFolha(BancoFolhaEnum.EFETIVOS);
		setConexaoAtualBancoFolha(BancoFolhaEnum.EFETIVOS);
		conexaoBanco = connectionConfigService.obterConexaoBancoFOLHA();
		// obterConexaoBanco(FIREBIRD_DRIVER, URL,
		// USUARIO, SENHA);
		Ajax.update(":responsavelTabView");
	}

	public void criar() {
		try {
			LoginBean controllerInstance = UtilsView
					.getControllerInstance(LoginBean.class);
			if (controllerInstance.getPrincipalRole().equals("RESPONSÁVEL")) {
				addErrorMessage("Usuário sem permissão para realizar a operação!");
				return;
			}
			responsavel
					.setCpf(UtilsModel.convertFormatCPF(responsavel.getCpf()));
			if (!ValidateUtils.isValidCPF(responsavel.getCpf())) {
				addErrorMessage(ResultMessages.INVALID_CPF.getDescricao());
				return;
			} else if (!"001".equals(responsavel.getCodigoBanco().trim())) {
				addErrorMessage("Código do Banco deve ser 001 - Banco do Brasil!");
				return;
			}
			responsavelService.salvar(responsavel);
			addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
			init();
		} catch (Exception e) {
			addErrorMessage(String.format("%s \nConsulte o Analista: %s",
					ResultMessages.ERROR_CRUD.getDescricao(),
					e.getLocalizedMessage()));
		}
	}

	public List<Responsavel> autocompletarBDFolha(String valor) {

		responsaveis.clear();
		String query = null;
		try {
			createStatement = getConexaoBanco().createStatement();

			if (valor != null && !"".equals(valor)) {
				query = String.format(
						QueryEnum.Responsavel.RESPONSAVEL_FIREBIRD.toString()
								.concat(MATR_CPF_NOME_RESP_QUERY), valor,
						valor, valor);
			} else {
				query = QueryEnum.Responsavel.RESPONSAVEL_FIREBIRD.toString();
			}

			result = createStatement.executeQuery(query);

			while (result.next()) {

				Responsavel responsavel = new Responsavel();
				responsavel.setIdExterno(result.getInt(1));
				responsavel.setId(Long.parseLong(result.getString(2)));
				responsavel.setMatriculaFuncional(result.getString(2));

				responsavel.setCpf(result.getString(3));
				responsavel.setRg(result.getString(4));
				responsavel.setOrgaoExpedidor(result.getString(5));
				responsavel.setDataExpedicao(result.getString(6));
				responsavel.setNome(result.getString(7));
				responsavel.setCodigoBanco(result.getString(8));
				responsavel.setNumeroConta(result.getString(9));
				responsavel
						.setTipoConta(result.getString(10) == "0" ? TipoContaEnum.CORRENTE
								: TipoContaEnum.POUPANCA);
				responsavel.setDigitoConta(result.getString(11));
				responsavel.setDigitoAgencia(result.getString(12));
				responsavel.setEndereco(result.getString(13));
				responsavel.setBairro(result.getString(14));
				responsavel.setCidade(result.getString(15));
				responsavel.setUf(result.getString(16));
				responsavel.setIdCargo(result.getInt(17));
				responsavel.setCargo(result.getString(18));
				responsavel.setNumeroAgencia(result.getString(19));

				responsaveis.add(responsavel);

			}
			result.close();
			createStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return responsaveis;
	}

	public void carregarBancoFolha() {

		switch (bancoFolha) {
		case EFETIVOS: {

			try {

				if (!BancoFolhaEnum.EFETIVOS.descricao
						.equals(getConexaoAtualBancoFolha().toString())
						&& getConexaoBanco() != null
						&& !getConexaoBanco().isClosed())
					getConexaoBanco().close();

				Class.forName(FIREBIRD_DRIVER);
				setConexaoBanco(DriverManager.getConnection(String.format(
						"%sefetivos%sgcs.fdb", URL, File.separator), USUARIO,
						SENHA));
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			setConexaoAtualBancoFolha(bancoFolha);
			break;
		}

		case COMISSIONADOS: {

			try {

				if (!BancoFolhaEnum.COMISSIONADOS.descricao
						.equals(getConexaoAtualBancoFolha().toString())
						&& getConexaoBanco() != null
						&& !getConexaoBanco().isClosed())
					getConexaoBanco().close();

				Class.forName(FIREBIRD_DRIVER);
				setConexaoBanco(DriverManager.getConnection(String.format(
						"%sportaria%sgcs.fdb", URL, File.separator), USUARIO,
						SENHA));
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			setConexaoAtualBancoFolha(bancoFolha);
			break;
		}
		default: {

			try {

				if (!BancoFolhaEnum.OUTROS.descricao
						.equals(getConexaoAtualBancoFolha().toString())
						&& getConexaoBanco() != null
						&& !getConexaoBanco().isClosed())
					getConexaoBanco().close();

				Class.forName(FIREBIRD_DRIVER);
				setConexaoBanco(DriverManager.getConnection(String.format(
						"%sagentes%sgcs.fdb", URL, File.separator), USUARIO,
						SENHA));
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			setConexaoAtualBancoFolha(bancoFolha);
			break;

		}
		}
	}

	public Responsavel getResponsavelById(long id) {
		for (Responsavel uc : getResponsaveis()) {
			if (uc.getId() == id)
				return uc;
		}
		return null;
	}

	public BancoFolhaEnum[] bancosFolha() {
		return BancoFolhaEnum.values();
	}

	public void limparDependencias() {

		responsavel.setIdExterno(0);
		responsavel.setId(null);
		responsavel.setMatriculaFuncional(null);

		responsavel.setCpf(null);
		responsavel.setRg(null);
		responsavel.setOrgaoExpedidor(null);
		responsavel.setDataExpedicao(null);
		responsavel.setNome(null);
		responsavel.setCodigoBanco(null);
		responsavel.setNumeroConta(null);
		responsavel.setTipoConta(TipoContaEnum.POUPANCA);
		responsavel.setDigitoConta(null);
		responsavel.setDigitoAgencia(null);
		responsavel.setEndereco(null);
		responsavel.setBairro(null);
		responsavel.setCidade(null);
		responsavel.setUf(null);
		responsavel.setIdCargo(0);
		responsavel.setCargo(null);
		responsavel.setNumeroAgencia(null);
	}

	public TipoContaEnum[] tipoContas() {
		return TipoContaEnum.values();
	}

	public List<Responsavel> autocompletar(String valor) {
		return responsavelService.autocompletar(valor);
	}

	public ResponsavelService getResponsavelService() {
		return responsavelService;
	}

	public void setResponsavelService(ResponsavelService responsavelService) {
		this.responsavelService = responsavelService;
	}

	public Responsavel getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(Responsavel responsavel) {
		this.responsavel = responsavel;
	}

	public ConnectionConfigService getConnectionConfigService() {
		return connectionConfigService;
	}

	public void setConnectionConfigService(
			ConnectionConfigService connectionConfigService) {
		this.connectionConfigService = connectionConfigService;
	}

	public Connection getConexaoBanco() {
		return conexaoBanco;
	}

	public void setConexaoBanco(Connection conexaoBanco) {
		this.conexaoBanco = conexaoBanco;
	}

	public Statement getCreateStatement() {
		return createStatement;
	}

	public void setCreateStatement(Statement createStatement) {
		this.createStatement = createStatement;
	}

	public ResultSet getResult() {
		return result;
	}

	public void setResult(ResultSet result) {
		this.result = result;
	}

	public List<Responsavel> getResponsaveis() {
		return responsaveis;
	}

	public void setResponsaveis(List<Responsavel> responsaveis) {
		this.responsaveis = responsaveis;
	}

	public BancoFolhaEnum getBancoFolha() {
		return bancoFolha;
	}

	public void setBancoFolha(BancoFolhaEnum bancoFolha) {
		this.bancoFolha = bancoFolha;
	}

	public BancoFolhaEnum getConexaoAtualBancoFolha() {
		return conexaoAtualBancoFolha;
	}

	public void setConexaoAtualBancoFolha(BancoFolhaEnum conexaoAtualBancoFolha) {
		this.conexaoAtualBancoFolha = conexaoAtualBancoFolha;
	}
}
