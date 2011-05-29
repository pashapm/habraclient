var pollFormClass = function() {
	this.lastForm = null;
	
	this.sendData = function (form, action, id) {
		var elems = form.elements;
		var checked = new Array();
		var check_idx = 0;
		for(i = 0; i < elems.length; i++) {
			if(elems[i].checked) { 
				checked[check_idx] = parseInt(elems[i].value);
				check_idx++;
			}
		}
		
		this.lastForm = form;
		js.pollVote(parseInt(id), action, checked);
	}
	
	this.updateData = function (data) {
		this.lastForm.parentNode.innerHTML = data;
	}
}

var pollForm = new pollFormClass();


