var actualitesBehaviours = {
	resources: {
		view: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|getThread'
		},
		contrib: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|createDraft'
		},
		updateDraft: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|updateDraft'
		},
		updatePending: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|updatePending'
		},
		updatePublished: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|updatePublished'
		},
		submit: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|submit'
		},
		unsubmit: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|unsubmit'
		},
		publish: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|publish'
		},
		unpublish: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|unpublish'
		},
		editThread: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|updateThread'
		},
		deleteThread: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|deleteThread'
		},
		share: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|shareThread'
		},
		trash: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|trash'
		},
		restore: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|restore'
		},
		delete: {
			right: 'fr.wseduc.actualites.controllers.ActualitesController|delete'
		}
	},
	workflow: {
		edit: 'fr.wseduc.actualites.controllers.ActualitesController|viewEdit',
		admin: 'fr.wseduc.actualites.controllers.ActualitesController|viewAdmin'
	}
};

Behaviours.register('actualites', {
	behaviours: actualitesBehaviours,
	resource: function(resource){
		if(!resource.myRights){
			resource.myRights = {};
		}

		for(var behaviour in actualitesBehaviours.resources){
			if(model.me.hasRight(resource, actualitesBehaviours.resources[behaviour]) || model.me.userId === resource.owner.userId){
				if(resource.myRights[behaviour] !== undefined){
					resource.myRights[behaviour] = resource.myRights[behaviour] && actualitesBehaviours.resources[behaviour];
				}
				else{
					resource.myRights[behaviour] = actualitesBehaviours.resources[behaviour];
				}
			}
		}
		/*
		if(model.me.userId === resource.owner.userId){
			resource.myRights.manage = actualitesBehaviours.resources[behaviour];
		}
		*/
		return resource;
	},
	workflow: function(){
		var workflow = { };
		var actualitesWorkflow = actualitesBehaviours.workflow;
		for(var prop in actualitesWorkflow){
			if(model.me.hasWorkflow(actualitesWorkflow[prop])){
				workflow[prop] = true;
			}
		}

		return workflow;
	},
	resourceRights: function(){
		return ['view', 'contribute', 'publish', 'manage']
	}
});