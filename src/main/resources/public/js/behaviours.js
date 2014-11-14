var actualitesBehaviours = {
	resources: {
		view: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|getThread'
		},
		contrib: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|createDraft'
		},
		updateDraft: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|updateDraft'
		},
		updatePending: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|updatePending'
		},
		updatePublished: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|updatePublished'
		},
		submit: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|submit'
		},
		unsubmit: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|unsubmit'
		},
		publish: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|publish'
		},
		unpublish: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|unpublish'
		},
		editThread: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|updateThread'
		},
		deleteThread: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|deleteThread'
		},
		share: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|shareThread'
		},
		trash: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|trash'
		},
		restore: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|restore'
		},
		delete: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|delete'
		},
		comment: {
			right: 'net-atos-entng-actualites-controllers-ActualitesController|comment'
		}
	},
	workflow: {
		admin: 'net.atos.entng.actualites.controllers.ActualitesController|createThread'
	}
};

Behaviours.register('actualites', {
	behaviours: actualitesBehaviours,
	resource: function(resource){
		var rightsContainer = resource;
		if(resource.thread){
			rightsContainer = resource.thread;
		}
		if(!resource.myRights){
			resource.myRights = {};
		}

		for(var behaviour in actualitesBehaviours.resources){
			if(model.me.hasRight(rightsContainer, actualitesBehaviours.resources[behaviour]) || model.me.userId === resource.owner.userId || model.me.userId === rightsContainer.owner.userId){
				if(resource.myRights[behaviour] !== undefined){
					resource.myRights[behaviour] = resource.myRights[behaviour] && actualitesBehaviours.resources[behaviour];
				}
				else{
					resource.myRights[behaviour] = actualitesBehaviours.resources[behaviour];
				}
			}
		}
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
		return ['read', 'contrib', 'publish', 'manager', 'comment'];
	},
	
	// Used by component "linker" to load news
	loadResources: function(callback){
		http().get('/actualites/linker/infos').done(function(data) {
			var infosArray = _.map(data, function(thread){
				var infos = _.map(thread.infos, function(info){
					// Keep news that are published and not expired
					if(info.status === 3) {	
						if(info.expirationDate && info.expirationDate.$date && 
								moment().isAfter(moment(info.expirationDate.$date))) {
							return;
						}
						
						var threadIcon;
						if (typeof (thread.icon) === 'undefined' || thread.icon === '' ) {
							threadIcon = '/img/icons/glyphicons_036_file.png';
						}
						else {
							threadIcon = thread.icon + '?thumbnail=48x48';
						}
						
						return {
							title : info.title + ' [' + thread.title + ']',
							ownerName : info.owner.displayName,
							owner : info.owner.userId,
							icon : threadIcon,
							path : '/actualites#/view/thread/' + thread._id + '/info/' + info._id,
							id : info._id,
							thread_id : thread._id
						};
					}
				});
				
				return infos;
			});
			
			this.resources = _.compact(_.flatten(infosArray));
			this.resources = _.sortBy(this.resources, function(info) {
				return info.title.toLowerCase();
			});
			
			if(typeof callback === 'function'){
				callback(this.resources);
			}
		}.bind(this));
	}
	
});