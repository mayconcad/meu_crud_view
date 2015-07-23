package br.com.sts.ddum.view.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.springsecurity.entities.User;
import br.com.sts.ddum.model.utils.UtilsModel;
import br.com.sts.ddum.service.interfaces.EmailService;
import br.com.sts.ddum.service.interfaces.UserService;

@Controller
@ViewScoped
public class RecuperaSenhaController extends BaseController {

	private static final long serialVersionUID = 4144343175697952209L;

	private static final String ADMIN_STS = "Administrador STS Informática";

	private static final String EMAIL_STS_DDUM = "suporte@stsinformatica.com";

	private static final String PASSWORD_EMAIL_STS = "stspiinf";

	private static final String RECUPERACAO_SENHA = "DDUM - Recuperação de Senha";

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserService userService;

	private String email;

	private boolean emailInvalido = false;;

	@PostConstruct
	public void init() {
		email = new String();

	}

	public void recuperarSenha() {

		// Properties props = new Properties();
		// /** Parâmetros de conexão com servidor Gmail 587 */
		// props.put("mail.smtp.host", "smtp.gmail.com");
		// props.put("mail.smtp.socketFactory.port", "465");
		// props.put("mail.smtp.starttls.enable", "true");
		// props.put("mail.smtp.socketFactory.class",
		// "javax.net.ssl.SSLSocketFactory");
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.port", "465");
		//
		// Session session = Session.getDefaultInstance(props,
		// new javax.mail.Authenticator() {
		// protected PasswordAuthentication getPasswordAuthentication() {
		// return new PasswordAuthentication("stsddum@gmail.com",
		// "stspiinf");
		// }
		// });
		//
		// /** Ativa Debug para sessão */
		// session.setDebug(true);
		//
		// try {
		//
		// Message message = new MimeMessage(session);
		// message.setFrom(new InternetAddress("stsddum@gmail.com")); //
		// Remetente
		//
		// Address[] toUser = InternetAddress // Destinatário(s)
		// .parse("mayconcad@gmail.com, maycon@stsinformatica.com, mayconcad@yahoo.com.br");
		//
		// message.setRecipients(Message.RecipientType.TO, toUser);
		// message.setSubject("Enviando email com JavaMail");// Assunto
		// message.setText("Enviei este email utilizando JavaMail com minha conta GMail!");
		// /** Método para enviar a mensagem criada */
		// Transport.send(message);
		//
		// System.out.println("Enviado!!!");
		//
		// } catch (MessagingException e) {
		// addErrorMessage("Deu TUdo Errado!!");
		// System.out.println("Deu tudo errado!!");
		// throw new RuntimeException(e);
		// }

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("email", getEmail());
		List<User> users = userService.buscar(params);
		if (users == null || users.isEmpty()) {
			addErrorMessage(ResultMessages.SENT_EMAIL_ERROR.getDescricao());
			setEmailInvalido(true);
			return;
		}

		String novaSenha = UtilsModel.gerarCaptcha(8);
		users.get(0).setPassword(novaSenha);
		try {

			emailService.sendEmail(ADMIN_STS, EMAIL_STS_DDUM,
					PASSWORD_EMAIL_STS, new String[] { getEmail(),
							"mayconcad@yahoo.com.br" }, "", RECUPERACAO_SENHA,
					users.get(0).getUsername(), novaSenha);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();// br.yahoo.com/
			addErrorMessage(ResultMessages.SENT_EMAIL_ERROR.getDescricao());
			setEmailInvalido(true);
			return;
		} catch (MessagingException e) {
			e.printStackTrace();
			addErrorMessage(ResultMessages.SENT_EMAIL_ERROR.getDescricao());
			setEmailInvalido(true);
			return;
		}
		userService.edite(users.get(0));
		addInfoMessage(ResultMessages.SENT_EMAIL.getDescricao());

		FacesContext context = FacesContext.getCurrentInstance();
		String url = context.getExternalContext().getRequestContextPath();
		try {
			context.getExternalContext().redirect(url + "/login.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		setEmailInvalido(false);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean EmailInvalido() {
		return emailInvalido;
	}

	public void setEmailInvalido(boolean emailInvalido) {
		this.emailInvalido = emailInvalido;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

}
