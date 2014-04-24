var actualitesBehaviours = {
	workflow: {
		edit: 'fr.wseduc.actualites.controllers.ActualitesController|viewEdit',
		publish: 'fr.wseduc.actualites.controllers.ActualitesController|publish'
	}
};

Behaviours.register('actualites', {
	workflow: function(){
		var workflow = { };
		var documentsWorkflow = actualitesBehaviours.workflow;
		for(var prop in documentsWorkflow){
			if(model.me.hasWorkflow(documentsWorkflow[prop])){
				workflow[prop] = true;
			}
		}

		return workflow;
	}
});