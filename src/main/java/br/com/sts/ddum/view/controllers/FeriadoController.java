package br.com.sts.ddum.view.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;

import org.omnifaces.util.Ajax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.sts.ddum.model.entities.Feriado;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.enums.TipoFeriadoEnum;
import br.com.sts.ddum.service.interfaces.FeriadoService;
import br.com.sts.ddum.view.utils.UtilsView;

@Controller
@ViewScoped
public class FeriadoController extends BaseController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4806821894303435065L;

	@Autowired
	private FeriadoService feriadoService;

	private Feriado feriado, feriadoEdite, feriadoRemove = new Feriado();

	private List<Feriado> feriados;

	private MesesAno mes;

	private String dia;

	@PostConstruct
	public void init() {

		feriado = feriadoEdite = feriadoRemove = new Feriado();
		feriados = new ArrayList<Feriado>();
		dia = "";
		mes = MesesAno.JAN;
		Ajax.update(":feriadoTabView");

	}

	public void limparDados() {
		init();
		BuscarFeriadoController controllerInstance = UtilsView
				.getControllerInstance(BuscarFeriadoController.class);
		if (controllerInstance != null)
			controllerInstance.init();
	}

	public void criar() {

		try {
			feriadoService.salvarTodos(getFeriados());
		} catch (Exception e) {
			if (e.getMessage().contains("feriado_descricao_dia_mes_key")) {
				addErrorMessage("Já existe um Feriado com esta descrição!");
				return;
			}
			addErrorMessage(ResultMessages.ERROR_CRUD.getDescricao());
			return;
		}
		addInfoMessage(ResultMessages.CREATE_SUCESS.getDescricao());
		init();
	}

	public void addFeriados() {

		feriado.setAtivo(true);
		feriado.setMes((byte) getMes().ordinal());
		feriado.setDia(Byte.valueOf(getDia()));
		getFeriados().add(feriado);
		feriado = new Feriado();
	}

	public void editarFeriado() {
		getFeriados().remove(feriadoEdite);
		setMes(getMesAno(feriadoEdite.getMes()));
		setDia(feriadoEdite.getDia() + "");
	}

	public void removerFeriado() {
		getFeriados().remove(feriadoRemove);
	}

	public TipoFeriadoEnum[] getTipos() {
		return TipoFeriadoEnum.values();
	}

	public MesesAno[] getMeses() {
		return MesesAno.values();
	}

	public String[] getDias() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, feriado.getMes());
		int ultimoDiaMes = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		String dias[] = new String[ultimoDiaMes];
		for (int index = 0; index < ultimoDiaMes; index++) {
			dias[index] = (index + 1) + "";
		}
		return dias;
	}

	public FeriadoService getFeriadoService() {
		return feriadoService;
	}

	public void setFeriadoService(FeriadoService feriadoService) {
		this.feriadoService = feriadoService;
	}

	public Feriado getFeriado() {
		return feriado;
	}

	public void setFeriado(Feriado feriado) {
		this.feriado = feriado;
	}

	public List<Feriado> getFeriados() {
		return feriados;
	}

	public void setFeriados(List<Feriado> feriados) {
		this.feriados = feriados;
	}

	public MesesAno getMes() {
		return mes;
	}

	public void setMes(MesesAno mes) {
		this.mes = mes;
	}

	public String getDia() {
		if (dia == null)
			dia = "1";
		return dia;
	}

	public void setDia(String dia) {
		this.dia = dia;
	}

	public Feriado getFeriadoEdite() {
		return feriadoEdite;
	}

	public void setFeriadoEdite(Feriado feriadoEdite) {
		this.feriadoEdite = feriadoEdite;
	}

	public Feriado getFeriadoRemove() {
		return feriadoRemove;
	}

	public void setFeriadoRemove(Feriado feriadoRemove) {
		this.feriadoRemove = feriadoRemove;
	}

	public enum MesesAno {
		JAN("Janeiro"), FEV("Fevereiro"), MAR("Março"), ABR("Abril"), MAI(
				"Maio"), JUN("Junho"), JUL("Julho"), AGO("Agosto"), SET(
				"Setembro"), OUT("Outubro"), NOV("Novembro"), DEZ("Dezembro"), ;

		private String descricao;

		private MesesAno(String descricao) {
			this.descricao = descricao;
		}

		public String getDescricao() {
			return descricao;
		}
	}

	public MesesAno getMesAno(byte mes) {
		switch (mes) {
		case 0:
			return MesesAno.JAN;
		case 1:
			return MesesAno.FEV;
		case 2:
			return MesesAno.MAR;
		case 3:
			return MesesAno.ABR;
		case 4:
			return MesesAno.MAI;

		case 5:
			return MesesAno.JUN;

		case 6:
			return MesesAno.JUL;
		case 7:
			return MesesAno.AGO;
		case 8:
			return MesesAno.SET;
		case 9:
			return MesesAno.OUT;
		case 10:
			return MesesAno.NOV;
		case 11:
			return MesesAno.DEZ;
		}
		return MesesAno.JAN;
	}
}
