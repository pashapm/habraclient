class pollFormClass {
	var lastForm = null;
	
	function getResult(form, action, id) {
		var elems = form.elements;
		var checked = new Array();
		var check_idx = 0;
		for(i = 0; i < elems.length; i++) {
			if(elems[i].checked) { 
				checked[check_idx] = elems[i].value;
				check_idx++;
			}
		}
		
		lastForm = form;
	
		js.pollVote(id, action, checked);
	}
	
	function updateData(data) {
		lastForm.parentNode.innerHTML = data;
	}
}

var pollForm = new pollFormClass();

