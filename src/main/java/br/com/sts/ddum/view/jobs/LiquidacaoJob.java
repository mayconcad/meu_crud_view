package br.com.sts.ddum.view.jobs;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import br.com.sts.ddum.model.entities.Feriado;
import br.com.sts.ddum.model.entities.Repasse;
import br.com.sts.ddum.model.entities.Responsavel;
import br.com.sts.ddum.model.entities.Unidade;
import br.com.sts.ddum.model.enums.StatusItemEnum;
import br.com.sts.ddum.service.interfaces.FeriadoService;
import br.com.sts.ddum.service.interfaces.RepasseService;
import br.com.sts.ddum.service.interfaces.ResponsavelService;
import br.com.sts.ddum.service.interfaces.UnidadeService;
import br.com.sts.ddum.view.controllers.SpringContextUtils;

public class LiquidacaoJob extends QuartzJobBean {

	// @Override
	// public void execute(JobExecutionContext context)
	// throws JobExecutionException {
	//
	// ApplicationContext applicationContext = new
	// ClassPathXmlApplicationContext(
	// "quartz-context.xml");
	//
	// LiquidacaoService bean = (LiquidacaoService) applicationContext
	// .getBean("LiquidacaoServiceJob");
	//
	// try {
	// if (controllerInstance == null)
	// controllerInstance = UtilsView
	// .getControllerInstance(FeriadoController.class);
	//
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// logger.error("Erro ao tentar localizar ApplicationContext. " +
	// e1.getjob.getDescription()Message(), e1);
	// }
	// if (controllerInstance != null && userServiceImpl != null
	// || repasseService != null || responsavelService != null)
	// System.out.println("conseguiu instancia");

	// if( applicationContext!=null ){
	// executeTaskControl = (ExecuteTaskControl)
	// applicationContext.getBean("executeTaskControlImpl");
	// }
	//
	// try {
	// executeTaskControl.execute();
	// } catch (Exception e) {
	// logger.error(e);
	// }
	// }

	// try {
	// Se houver repasse será criado uma liquidação para o mesmo
	// LoginBean controllerInstance = UtilsView
	// .getControllerInstance(LoginBean.class);
	// User currentUser = (User) controllerInstance.getCurrentUser();
	// Map<String, Object> params = new HashMap<String, Object>();
	// params.put("user.id", currentUser.getId());
	// if (responsavelService == null)
	// return;
	// List<Responsavel> responsaveis = responsavelService.buscar(params);
	// if (responsaveis != null && !responsaveis.isEmpty()) {
	//
	// for (Responsavel responsavel : responsaveis) {
	//
	// params.clear();
	// if (responsavel != null)
	// params.put("unidade.responsavel.id",
	// responsavel.getId());
	//
	// List<Repasse> repasses = repasseService.buscar(params);
	// if (repasses != null && !repasses.isEmpty()) {
	// setRepasse(repasses.get(0));
	// }

	/**
	 * @Description Gera um repasse automático para o responsável pela unidade
	 *              com base no valor da prestação de conta
	 */
	// if (repasseService == null)
	// return;
	//
	// System.out.println("Serviço instanciado!!!");
	//
	// long obterProximoNumeroEmpenho = repasseService
	// .obterProximoNumeroEmpenho(getRepasse()
	// .getUnidade().getParametroRepasse()
	// .getCodUnidade());
	//
	// if (getRepasse() != null
	// && getRepasse().getUnidade() != null
	// && getRepasse().getUnidade().getParametroRepasse() != null)
	// System.out.println("próximo número empenho: "
	// + obterProximoNumeroEmpenho);
	//
	// }
	// }
	// repasseService.repasseAutomatico(getRepasse(), getRepasse()
	// .getUnidade().getResponsavel(), getRepasse().getUnidade()
	// .getParametroRepasse().getValorRepasse(), getRepasse()
	// .getPrestacaoConta() == null ? BigDecimal.ZERO
	// : getRepasse().getPrestacaoConta().getValor(),
	// obterProximoNumeroEmpenho, false);

	// } catch (IllegalArgumentException e) {
	// e.printStackTrace();
	// throw new IllegalArgumentException(
	// "Ocorreu um erro durante a geração automática da Liquidação. Consulte o Suporte Técnico.");
	// } catch (SQLException e) {
	// e.printStackTrace();
	// throw new IllegalArgumentException(
	// "Ocorreu um erro durante a geração automática da Liquidação. Consulte o Suporte Técnico.");
	// }

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {

		// para o primeiro repasse a data do repasse deve equivale ao
		// primeiro dia útil do mês
		Calendar dataAtual = Calendar.getInstance();
		dataAtual.setTime(new Date());

		// Calendar calendar = Calendar.getInstance();
		// calendar.setTime(repasse.getDataEmissao());
		byte dia = ((byte) dataAtual.get(Calendar.DAY_OF_MONTH));
		byte mes = ((byte) (dataAtual.get(Calendar.MONTH) + 1));

		ApplicationContext applicationContext = SpringContextUtils
				.getApplicationContext();

		FeriadoService feriadoService = applicationContext
				.getBean(FeriadoService.class);
		// verifica se a data do empenho é equivamente ao primeiro dia
		// util do mês, caso contrário a data para liquidação será
		// primeiro dia útil do próximo mês
		// if (dia == dataAtual.get(Calendar.DAY_OF_MONTH)) {
		// if (1 == dia) {
		Feriado feriado = feriadoService.buscarPorDiaMes(dia, mes);
		if (feriado != null) {
			dia = obterDiaUtilMes(++dia, mes, feriadoService);
		}

		ResponsavelService responsavelService = applicationContext
				.getBean(ResponsavelService.class);

		Map<String, Object> params = new HashMap<String, Object>();

		UnidadeService unidadeService = applicationContext
				.getBean(UnidadeService.class);

		RepasseService repasseService = applicationContext
				.getBean(RepasseService.class);

		List<Responsavel> responsaveis = responsavelService.buscar(params);
		Set<IllegalArgumentException> illegalExceptions = new HashSet<IllegalArgumentException>();

		forResponsavel: for (Responsavel responsavel : responsaveis) {
			params.put("responsavel.id", responsavel.getId());
			List<Unidade> unidades = unidadeService.buscar(params);
			if (unidades != null && !unidades.isEmpty()) {
				params.clear();
				params.put("unidade.responsavel.id", responsavel.getId());
				params.put("unidade.id", unidades.get(0).getId());
				// params.put("prestacaoConta.finalizada", false);
				// params.put("prestacaoConta", null);
				dataAtual.set(Calendar.DAY_OF_MONTH,
						dataAtual.getActualMaximum(Calendar.DAY_OF_MONTH));
				params.put("maxDate", dataAtual.getTime());
				// params.put("numeroLiquidacao", 0l);

				List<Repasse> repasses = repasseService.buscar(params);
				if (repasses == null || repasses.isEmpty()) {
					illegalExceptions
							.add(new IllegalArgumentException(
									"Para gerar a próxima liquidação é necessário prestar conta da liquidação em aberto"));
					continue;
				} else {

					for (Repasse repasse : repasses) {

						if (repasse != null
								&& repasse.getPrestacaoConta() != null
								&& StatusItemEnum.REJEITADA.equals(repasse
										.getPrestacaoConta().getStatus()))
							continue forResponsavel;
					}
				}

				dataAtual.set(Calendar.DAY_OF_MONTH, dia);
				dataAtual.set(Calendar.MONTH, --mes);
				repasses.get(0).setDataEmissao(dataAtual.getTime());
				repasseService.liquidacaoAutomatico(repasses.get(0),
						responsavel, repasses.get(0).getValorRepasse(),
						BigDecimal.ZERO, repasses.get(0).getNumeroEmpenho(),
						false);
				System.out.println("Liquidação do Job Executado com sucesso!!");
			}

		}
		// }

		System.out.println("Job Executado com sucesso!!");
	}

	private byte obterDiaUtilMes(byte dia, byte mes,
			FeriadoService feriadoService) {

		Feriado feriado = feriadoService.buscarPorDiaMes(dia, mes);
		if (feriado != null)
			return obterDiaUtilMes(++dia, mes, feriadoService);

		return dia;
	}
}