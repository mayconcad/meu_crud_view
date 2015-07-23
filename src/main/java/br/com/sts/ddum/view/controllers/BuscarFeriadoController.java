package br.com.sts.ddum.view.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.primefaces.component.tabview.TabView;

import br.com.sts.ddum.model.entities.Feriado;
import br.com.sts.ddum.model.enums.ResultMessages;
import br.com.sts.ddum.model.enums.TipoFeriadoEnum;
import br.com.sts.ddum.service.interfaces.FeriadoService;
import br.com.sts.ddum.view.controllers.FeriadoController.MesesAno;

@ManagedBean
@ViewScoped
public class BuscarFeriadoController extends BaseController {

	private static final long serialVersionUID = 2883233373923310796L;

	private Feriado feriadoBusca;

	private Feriado feriadoRemove;

	private Feriado feriadoEdite;

	private List<Feriado> feriados = new ArrayList<Feriado>();

	private MesesAno mes, mesBusca;

	private String dia, diaBusca;

	@ManagedProperty("#{feriadoService}")
	private FeriadoService feriadoService;

	@PostConstruct
	public void init() {
		feriadoBusca = feriadoEdite = feriadoRemove = new Feriado();
		dia = "";
		diaBusca = "";
		mes = null;
		mesBusca = null;
	}

	public void buscar() {
		feriados.clear();
		Map<String, Object> params = new HashMap<String, Object>();
		if (feriadoBusca.getDescricao() != null
				&& !"".equals(feriadoBusca.getDescricao()))
			params.put("descricao", feriadoBusca.getDescricao());

		if (getDiaBusca() != null && getDiaBusca() != "")
			params.put("dia", Byte.parseByte(getDiaBusca()));

		if (getMesBusca() != null && !"".equals(getMesBusca()))
			params.put("mes", getMesBusca().ordinal());

		if (feriadoBusca.getAno() != 0)
			params.put("ano", feriadoBusca.getAno());
		feriados = feriadoService.buscar(params);
	}

	public void remover() {
		try {
			if (usuarioSemPermissao())
				return;
			feriadoService.remover(feriadoRemove);
		} catch (Exception e) {
			addErrorMessage(String.format(
					"%s \nConsulte o Suporte Técnico: %s",
					ResultMessages.ERROR_CRUD.getDescricao(), e.getMessage()));
			return;
		}
		buscar();
		addInfoMessage(ResultMessages.DELETE_SUCESS.getDescricao());
	}

	public void editar(ActionEvent actionEvent) {
		try {
			feriadoEdite.setMes((byte) getMes().ordinal());
			feriadoEdite.setDia(Byte.valueOf(getDia()));
			feriadoService.atualizar(feriadoEdite);
			addInfoMessage(ResultMessages.UPDATE_SUCESS.getDescricao());
		} catch (Exception e) {
			addErrorMessage(String.format(
					"%s \nConsulte o Suporte Técnico: %s",
					ResultMessages.ERROR_CRUD.getDescricao(),
					e.getLocalizedMessage()));
		}

		loadToFind();
	}

	public void limparFiltroBusca() {
		feriadoBusca = new Feriado();
		mesBusca = null;
		diaBusca = "";
	}

	public TipoFeriadoEnum[] getTipos() {
		return TipoFeriadoEnum.values();
	}

	public MesesAno[] getMeses() {
		return MesesAno.values();
	}

	public String[] getDias() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, feriadoEdite.getMes());
		int ultimoDiaMes = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		String dias[] = new String[ultimoDiaMes];
		for (int index = 0; index < ultimoDiaMes; index++) {
			dias[index] = (index + 1) + "";
		}
		return dias;
	}

	public String[] getDiasBusca() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, feriadoBusca.getMes());
		int ultimoDiaMes = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		String dias[] = new String[ultimoDiaMes];
		for (int index = 0; index < ultimoDiaMes; index++) {
			dias[index] = (index + 1) + "";
		}
		return dias;
	}

	public void loadToFind() {
		getEditTab().setRendered(false);
		TabView parent = (TabView) getFindTab().getParent();
		int findIndex = parent.getChildren().indexOf(getFindTab());
		parent.setActiveIndex(findIndex);
	}

	public void carregar() {
		getEditTab().setRendered(true);
		TabView parent = (TabView) getEditTab().getParent();
		int editIndex = parent.getChildren().indexOf(getEditTab());
		parent.setActiveIndex(editIndex);
		setMes(getMesAno(feriadoEdite.getMes()));
		setDia(feriadoEdite.getDia() + "");
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

	public Feriado getFeriadoRemove() {
		return feriadoRemove;
	}

	public void setFeriadoRemove(Feriado feriadoRemove) {
		this.feriadoRemove = feriadoRemove;
	}

	public Feriado getFeriadoEdite() {
		return feriadoEdite;
	}

	public void setFeriadoEdite(Feriado feriadoEdite) {
		this.feriadoEdite = feriadoEdite;
	}

	public FeriadoService getFeriadoService() {
		return feriadoService;
	}

	public void setFeriadoService(FeriadoService feriadoService) {
		this.feriadoService = feriadoService;
	}

	public List<Feriado> getFeriados() {
		return feriados;
	}

	public void setFeriados(List<Feriado> feriados) {
		this.feriados = feriados;
	}

	public Feriado getFeriadoBusca() {
		return feriadoBusca;
	}

	public void setFeriadoBusca(Feriado feriadoBusca) {
		this.feriadoBusca = feriadoBusca;
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

	public MesesAno getMesBusca() {
		return mesBusca;
	}

	public void setMesBusca(MesesAno mesBusca) {
		this.mesBusca = mesBusca;
	}

	public String getDiaBusca() {
		if (diaBusca == null)
			diaBusca = "1";
		return diaBusca;
	}

	public void setDiaBusca(String diaBusca) {
		this.diaBusca = diaBusca;
	}

}
