package br.com.sts.ddum.view.controllers;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import br.com.sts.ddum.service.interfaces.LiquidacaoService;
import br.com.sts.ddum.view.jobs.LiquidacaoJob;

//as duas anotações abaixo executam ao realizar o deploy da aplicação
@ApplicationScoped
@Component
public class LiquidacaoJobController extends BaseController {

	private static final long serialVersionUID = -4538100498368440848L;

	// @Inject
	// RepasseService repasseService;
	//
	// @Autowired
	// ResponsavelService responsavelService;

	@PostConstruct
	public void init() {
		ApplicationContext applicationContext = SpringContextUtils
				.getApplicationContext();
		if (applicationContext != null) {
			LiquidacaoService bean = applicationContext
					.getBean(LiquidacaoService.class);
			try {
				bean.agendar("0 0 2 ? * *", LiquidacaoJob.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void agendar() throws Exception {
		// System.out.println("acesso ao metodo agendar");

		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler s = sf.getScheduler();

		// define o job a ser executado de acordo com o agendamento
		JobDetail job = JobBuilder.newJob(LiquidacaoJob.class).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity("simple")
				// 0 0 1 1 * * ?
				// executa tod dia 1º de cada mês
				// segundo, minuto, hora, dia do mes, dia da semana e anno 0 0 0
				// 1 * ?
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
				.startNow().build();

		s.scheduleJob(job, trigger);
		s.start();

		try {
			// Thread.sleep(90L * 1000L);

		} catch (Exception e) {

		}
		// s.shutdown(true);
	}
	// public RepasseService getRepasseService() {
	// return repasseService;
	// }
	//
	// public void setRepasseService(RepasseService repasseService) {
	// this.repasseService = repasseService;
	// }
	//
	// public ResponsavelService getResponsavelService() {
	// return responsavelService;
	// }
	//
	// public void setResponsavelService(ResponsavelService responsavelService)
	// {
	// this.responsavelService = responsavelService;
	// }

}