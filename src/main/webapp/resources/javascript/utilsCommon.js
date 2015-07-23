//Troca a função do enter pela função do tab, assim a cada tecla enter o cursor muda para proximo input como se fosse um tab

$(document)
		.ready(
				function() {
					jQuery('body')
							.on(
									'keydown',
									'input, select, option, textarea, button',
									function(e) {
										var self = $(this), form = self
												.parents('form:eq(0)'), focusable, next;
										
										if (e.keyCode == 13) {
											focusable = form
													.find(
															'input,a,select,option,textarea, button')
													.filter(':visible');

											ignoreField(focusable, this);
											// if(next.length &&
											// focusable.get(nextIndex).isContentEditable
											// == false){
											// next=focusable.eq(nextIndex+1);
											// next.focus();
											// }
											// console.log(next)
											// else if (next.length) {
											// next.focus();
											// }
											// else {
											// form.submit();
											// }
											return false;
										}
									});
				});

function ignoreField(focusable, obj) {

	var nextIndex = focusable.index(obj) + 1;
	next = focusable.eq(nextIndex);
	nextField = focusable.get(nextIndex);
	// nodeValue com o valor: ui-helper-hidden-accessible indica que é o botão
	// de um componente select e não executa o evento de enter
	// classname com o valor: ui-button ui-widget ui-state-default
	// ui-corner-right ui-button-icon-only indica que é um botão de um
	// autocomplete e
	if (next.length
			&& nextField != null
			&& (nextField.className == 'ui-button ui-widget ui-state-default ui-corner-right ui-button-icon-only') || (nextField.className == '')) {
//		código para pegar os combobox, pois estes possuem o nodevalue abaixo
//		nextField.offsetParent.attributes[0].nodeValue == 'ui-helper-hidden-accessible' || 
		ignoreField(focusable, next);
		// next = focusable.eq(nextIndex + 1);
		// next.focus();
	} else if (next.length) {
		next.focus();
	}
}

$(document).ready(function() {
	jQuery('body').on('click', 'button', function(e) {
		
		var self = $(this), form = self.parents('form:eq(0)'), focusable, next;
		focusable = form.find('input').filter(':visible');
		
		var indexObject = focusable.index(this);
		next = focusable.eq(indexObject);
//		next.value

	});
	
	//Remove as classes listadas abaixo do componente que possui a classe .ui-fileupload-content
//	$(".ui-fileupload-content").removeClass("ui-fileupload-content ui-widget-content ui-corner-bottom");
});
