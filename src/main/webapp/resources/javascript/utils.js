String.prototype.replaceAll = function(de, para) {
	var str = this;
	var pos = str.indexOf(de);
	while (pos > -1) {
		str = str.replace(de, para);
		pos = str.indexOf(de);
	}
	return (str);
};

function getNumber() {
	currentValue = valueInput.jq.val();
	launchingValor.jq.val(currentValue.replace("R$ ", "").replaceAll(".", "")
			.replace(",", "."));
}

function setCurrencyMask() {
	$(function() {
		$('.currency').maskMoney({
			symbol : "R$ ",
			showSymbol : true,
			symbolStay : true,
			decimal : ",",
			thousands : ".",
			defaultZero : false
		});

	});
}

function setFocus(index) {
	index.jq.focus();
}

function setMaskInInput() {
	jQuery(function($) {
		$('input[type="text"]').setMask();
	});
}

var regexNumeros = /^(0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0|1])\/{1}(0[1-9]|[1-9]|1[0-2])\/{1}([1-2]\d{3})$/;
function validaDate(value) {
	if (!regexNumeros.test(value.input.val())) {
		value.input.val("");
	}
}

function renderer() {
	setTimeout(function() {

		$("div.ui-selectonemenu").each(function() {
			$(this).css("width", "");
		});

		$("label.ui-selectonemenu-label").each(function() {
			$(this).css("width", "97%");
		});

	});
}

function mascara(o, f) {
	v_obj = o;
	v_fun = f;
	setTimeout("execmascara()", 1);
}

function execmascara() {
	v_obj.value = v_fun(v_obj.value);
}

function currency(v) {
	v = v.replace(/\D/g, "");
	v = v.replace(/[0-9]{15}/, "Inválido");
	v = v.replace(/(\d{1})(\d{11})$/, "$1.$2"); // coloca ponto antes dos
	// ultimos 11 digitos
	v = v.replace(/(\d{1})(\d{8})$/, "$1.$2"); // coloca ponto antes dos
	// ultimos 8 digitos
	v = v.replace(/(\d{1})(\d{5})$/, "$1.$2"); // coloca ponto antes dos
	// ultimos 5 digitos
	v = v.replace(/(\d{1})(\d{1,2})$/, "$1,$2"); // coloca virgula antes dos
	// ultimos 2 digitos
	return v;
}

function cpf(v) {
	v = v.replace(/\D/g, "");
	v = v.replace(/[0-9]{11}/, "");
	v = v.replace(/(\d{1})(\d{11})$/, "$1.$2"); // coloca ponto antes dos
	// ultimos 11 digitos
	v = v.replace(/(\d{1})(\d{8})$/, "$1.$2"); // coloca ponto antes dos
	// ultimos 8 digitos
	v = v.replace(/(\d{1})(\d{5})$/, "$1.$2"); // coloca ponto antes dos
	// ultimos 5 digitos
	v = v.replace(/(\d{1})(\d{1,2})$/, "$1-$2"); // coloca traço antes dos
	// ultimos 2 digitos
	return v;
}

function number(v) {
	//nao aceita caracteres literais
	v = v.replace(/\D/g, "");
	v = v.replace(/[0-9]{15}/, "Inválido");
	return v;
}

function cep(v) {
	v = v.replace(/\D/g, "");
	v = v.replace(/[0-9]{8}/, "");
	v = v.replace(/(\d{1})(\d{1,3})$/, "$1-$2"); // coloca traço antes dos
	// ultimos 2 digitos
	return v;
}
//click do campo de texto referente ao nome do arquivo na tela de prestação de contas
function clickUploadFile() {
	$(PrimeFaces.escapeClientId("prestacaoContaTabView:arquivoFileUpload_input")).click();
}

//Traduz para o português palavras em inglês do componente calendar do primefaces
PrimeFaces.locales['pt'] = {  
        closeText: 'Fechar',  
        prevText: 'Anterior',  
        nextText: 'Próximo',  
        currentText: 'Começo',  
        monthNames: ['Janeiro','Fevereiro','Março','Abril','Maio','Junho','Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],  
        monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun', 'Jul','Ago','Set','Out','Nov','Dez'],  
        dayNames: ['Domingo','Segunda','Terça','Quarta','Quinta','Sexta','Sábado'],  
        dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'],  
        dayNamesMin: ['Dom','Seg','Ter','Qua','Qui','Sex','S&aacute;b'],  
        weekHeader: 'Semana',  
        firstDay: 1,  
        isRTL: false,  
        showMonthAfterYear: false,  
        yearSuffix: '',  
        timeOnlyTitle: 'Só Horas',  
        timeText: 'Tempo',  
        hourText: 'Hora',  
        minuteText: 'Minuto',  
        secondText: 'Segundo',  
        currentText: 'Data Atual',  
        ampm: false,  
        month: 'Mês',  
        week: 'Semana',  
        day: 'Dia',  
        allDayText : 'Todo Dia'  
    }