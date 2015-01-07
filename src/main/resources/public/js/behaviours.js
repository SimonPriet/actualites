var actualitesBehaviours = {
	resources: {
		view: {
			right: 'net-atos-entng-actualites-controllers-ThreadController|getThread'
		},
		contrib: {
			right: 'net-atos-entng-actualites-controllers-InfoController|createDraft'
		},
		createPending: {
			right: 'net-atos-entng-actualites-controllers-InfoController|createPending'
		},
		createPublished: {
			right: 'net-atos-entng-actualites-controllers-InfoController|createPublished'
		},
		updateDraft: {
			right: 'net-atos-entng-actualites-controllers-InfoController|updateDraft'
		},
		updatePending: {
			right: 'net-atos-entng-actualites-controllers-InfoController|updatePending'
		},
		updatePublished: {
			right: 'net-atos-entng-actualites-controllers-InfoController|updatePublished'
		},
		submit: {
			right: 'net-atos-entng-actualites-controllers-InfoController|submit'
		},
		unsubmit: {
			right: 'net-atos-entng-actualites-controllers-InfoController|unsubmit'
		},
		publish: {
			right: 'net-atos-entng-actualites-controllers-InfoController|publish'
		},
		unpublish: {
			right: 'net-atos-entng-actualites-controllers-InfoController|unpublish'
		},
		editThread: {
			right: 'net-atos-entng-actualites-controllers-ThreadController|updateThread'
		},
		deleteThread: {
			right: 'net-atos-entng-actualites-controllers-ThreadController|deleteThread'
		},
		share: {
			right: 'net-atos-entng-actualites-controllers-ThreadController|shareThread'
		},
		trash: {
			right: 'net-atos-entng-actualites-controllers-InfoController|trash'
		},
		restore: {
			right: 'net-atos-entng-actualites-controllers-InfoController|restore'
		},
		delete: {
			right: 'net-atos-entng-actualites-controllers-InfoController|delete'
		},
		comment: {
			right: 'net-atos-entng-actualites-controllers-InfoController|comment'
		}
	},
	workflow: {
		admin: 'net.atos.entng.actualites.controllers.ThreadController|createThread'
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
			if(model.me.hasRight(rightsContainer, actualitesBehaviours.resources[behaviour]) || model.me.userId === resource.owner || model.me.userId === rightsContainer.owner){
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
						if(info.expiration_date && info.expiration_date.$date && 
								moment().isAfter(moment(info.expiration_date.$date))) {
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
							ownerName : info.unsername,
							owner : info.owner,
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