/* Constants */
var ACTUALITES_CONFIGURATION = {
	applicationName: 'actualites',
	infosCollectionName: 'infos',
	threadsCollectionName: 'threads',
	threadTypes: {
		latest: 0,
		mine: 1,
		trash: 2,
		pending: 3
	},
	threadFolders: {
		DRAFT: 'drafts',
		PENDING: 'pending',
		PUBLISHED: 'published',
		TRASH: 'trash'
	},
	threadFilters: {
		main: ['published'],
		edition: ['drafts', 'pending', 'published'],
		pending: ['pending'],
		drafts: ['drafts'],
		trash: ['trash']
	},
	infoStatus: {
		DRAFT: 0,
		PENDING: 1,
		PUBLISHED: 2,
		TRASH: 3
	},
	permissions: {
		contributor: 'org-entcore-workspace-service-WorkspaceService|updateDocument'
	}
};


/* Info */
function Info(){
	// thread (thread._id)
	// title
	// status
	// publicationDate
	// expirationDate
	// content
}

Info.prototype.load = function(data){
	var resourceUrl = '/workspace/document/' + this._id;
	if (data !== undefined) {
		resourceUrl = '/workspace/document/' + data._id;
	}

	http().get(resourceUrl).done(function(content){
		this.updateData({
			thread: content.thread,
			title: content.title,
			status: content.status,
			publicationDate: content.publicationDate,
			hasPublicationDate: content.publicationDate === undefined ? false : true,
			expirationDate: content.expirationDate,
			hasExpirationDate: content.expirationDate === undefined ? false : true,
			content: content.content,
			loaded: true,
			modified: content.modified || this.modified,
			owner: content.owner || this.owner,
			ownerName: content.ownerName || this.ownerName,
			_id: content._id || this._id
		});
	}.bind(this))
}

Info.prototype.create = function(thread, data){

	if (data !== undefined) {
		this.updateData(data);
	}

	var info = {
		thread: thread._id,
		title: this.title,
		status: this.status,
		publicationDate: this.publicationDate,
		expirationDate: this.expirationDate,
		content: this.content
	};
	var blob = new Blob([JSON.stringify(info)], { type: 'application/json'});
	var form = new FormData();
	form.append('blob', blob, info.title + '.json');
	http().postFile('/workspace/document?application=' + this.getApplicationInfosCollectionTag(), form).done(function(e){
		http().put('/workspace/documents/move/' + e._id + '/' + ACTUALITES_CONFIGURATION.threadFolders.DRAFT + '-' + thread._id).done(function(){
			thread.infos.sync();
		}.bind(this));
	}.bind(this));
}

Info.prototype.save = function(){

	var info = {
		_id: this._id,
		modified: this.modified,
		owner: this.owner,
		ownerName: this.ownerName,
		thread: this.thread,
		title: this.title,
		status: this.status,
		publicationDate: this.publicationDate,
		expirationDate: this.expirationDate,
		content: this.content
	};
	var blob = new Blob([JSON.stringify(info)], { type: 'application/json'});
	var form = new FormData();
	form.append('blob', blob, info.title + '.json');
	http().putFile('/workspace/document/' + this._id, form);

	// Permissions
	//var permissions = thread.shared;
	//this.updatePermissions(thread.shared);
}

Info.prototype.remove = function(thread){
	if(thread.type === ACTUALITES_CONFIGURATION.threadTypes.trash){
		http().delete('/workspace/document/' + this._id);
	}
	else{
		http().put('/workspace/document/trash/' + this._id);
	}
}

Info.prototype.submit = function(thread){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.PENDING;
	
	var info = this;
	http().put('/workspace/documents/move/' + info._id + '/' + ACTUALITES_CONFIGURATION.threadFolders.PENDING + '-' + thread._id).done(function(){
		this.updatePermissions(thread.shared).done(function(){
			info.save();
			thread.infos.sync();
		});
	}.bind(this));
}

Info.prototype.unsubmit = function(thread){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.DRAFT;

	var info = this;
	http().put('/workspace/documents/move/' + info._id + '/' + ACTUALITES_CONFIGURATION.threadFolders.DRAFT + '-' + thread._id).done(function(){
		this.clearPermissions().done(function(){
			info.save();
			thread.infos.sync();
		});
	}.bind(this));
}

Info.prototype.publish = function(thread){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;

	var info = this;
	http().put('/workspace/documents/move/' + info._id + '/' + ACTUALITES_CONFIGURATION.threadFolders.PUBLISHED + '-' + thread._id).done(function(){
		this.updatePermissions(thread.shared).done(function(){
			info.save();
			thread.infos.sync();
		});
	}.bind(this));
}

Info.prototype.unpublish = function(thread){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.DRAFT;

	var info = this;
	http().put('/workspace/documents/move/' + info._id + '/' + ACTUALITES_CONFIGURATION.threadFolders.DRAFT + '-' + thread._id).done(function(){
		this.clearPermissions().done(function(){
			info.save();
			thread.infos.sync();
		});
	}.bind(this));
}

Info.prototype.hasPermissions = function(){
	if (this.shared === undefined || this.shared.length === 0) {
		return false;
	}
	return true;
}

Info.prototype.isShareable = function(){
	return info.owner === model.me.userId;
}

Info.prototype.clearPermissions = function(){
	var deferred = $.Deferred();

	if ((! this.isShareable) || (! this.hasPermissions())) {
		return deferred.resolve().promise();
	}

	// Remove all permissions
	var count = this.shared.length;
	var that = this;
	_.each(this.shared, function(permission){
		var data = undefined;
        if(permission.userId !== undefined) {
        	data = { userId: permission.userId };
        }
        else if (permission.groupId !== undefined) {
        	data = { groupId: permission.groupId };
        }

        if (data !== undefined) {
        	http().put('/workspace/share/remove/' + that._id, http().serialize(data)).done(function(){
        		count--;
	        	if (count <= 0) {
	        		deferred.resolve();
	        	}	
        	});
        }
    });

    return deferred.promise();
}

Info.prototype.addPermissions = function(permissions){
	var deferred = $.Deferred();

	if ((! this.isShareable) || permissions === undefined || permissions.length === 0) {
		return deferred.resolve().promise();
	}

	// Add all permissions
	var count = permissions.length;
	var that = this;
	_.each(permissions, function(permission){
		// Prepare data
		var data = {};
		data.actions = [];
		_.each(permission, function(value, key){
			if (key === 'userId') {
				data.userId = value;
			}
			else if (key === 'groupId') {
				data.groupId = value;
			}
			else {
				data.actions.push(key);
			}
		});

		http().put('/workspace/share/json/' + that._id, http().serialize(data)).done(function(){
			count--;
        	if (count <= 0) {
        		deferred.resolve();
        	}	
		});
	});

	return deferred.promise();
}

Info.prototype.updatePermissions = function(permissions) {
	var deferred = $.Deferred();

	var info = this;
	this.clearPermissions().done(function(){
		info.addPermissions(permissions).done(function(){
			deferred.resolve();
		});
	});

	return deferred.promise();
}

Info.prototype.getApplicationInfosCollectionTag = function(){
	return ACTUALITES_CONFIGURATION.applicationName + '-' + ACTUALITES_CONFIGURATION.infosCollectionName;
}



/* Thread */
function Thread(){
	// type (optionnal, for static threads)
	// title
	// icon
	// color
	// order
}

Thread.prototype.build = function(data){
	this.updateData(data);
}

Thread.prototype.load = function(data){
	// useless...
	http().get('/workspace/document/' + data._id).done(function(content){
		this.updateData({
			title: content.title,
			icon: content.icon,
			color: content.color,
			order: content.order,
			loaded: true,
			modified: content.modified || this.modified,
			owner: content.owner || this.owner,
			ownerName: content.ownerName || this.ownerName,
			_id: content._id || this._id
		});

		if(content.pages){
			this.pages.load(content.pages);
		}
	}.bind(this))
}

Thread.prototype.create = function(data){
	this.updateData(data);
	this.save();
}

Thread.prototype.loadInfos = function(filters){
	var thread = this;

	// Resources depending on Thread type and filters
	var resourceUrls = [];
	if (this.type === ACTUALITES_CONFIGURATION.threadTypes.latest) {
		resourceUrls.push('/workspace/documents' + '?application=' + this.getApplicationInfosCollectionTag());
	}
	else {
		_.each(filters, function(filter){
			resourceUrls.push('/workspace/documents/' + filter + '-' + thread._id + '?application=' + thread.getApplicationInfosCollectionTag());
		});
	}

	// Bind infos collection
	this.collection(Info, {
		behaviours: 'workspace',
		sync: function(){
			var collection = this;
			var iterations = resourceUrls.length;
			this.all = [];
			_.each(resourceUrls, function(resourceUrl){
				http().get(resourceUrl).done(function(data){
					collection.addRange(_.filter(data, function(doc){
						// Select Json objects
						var ok = doc.metadata['content-type'] === 'application/json';

						// Filter on folders
						if (filters !== undefined && _.isString(doc.folder)) {
							return (ok && (_.indexOf(filters, doc.folder.split('-')[0]) !== -1));
						}
						return ok;
					}));
					iterations--;
					if (iterations <= 0){
						collection.trigger('sync');
					}
				})
			});
		},
		remove: function(){
			var collection = this;
			this.selection().forEach(function(info){
				if(_.isString(info.folder) && (info.folder.split('-')[0] === ACTUALITES_CONFIGURATION.threadFolders.TRASH)) {
					http().delete('/workspace/document/' + info._id).done(function(){
						collection.sync();
					});
				}
				else{
					http().put('/workspace/documents/move/' + e._id + '/' + ACTUALITES_CONFIGURATION.threadFolders.TRASH + '-' + thread._id).done(function(){
						collection.sync();
					});
				}
			});
			this.removeSelection();
		}
	});

	// Trigger loading of infos
	this.infos.sync();
}

Thread.prototype.getSharingPermissionsForInfo = function(permissions, info){
	var deferred = $.Deferred();

	if (this.shared !== undefined) {
		permissions = this.shared;
	}
	var managerPermission = {userId: this.owner};
	model.workspaceService.getFullPermissionsForActorForResource(managerPermission, info).done(function(){
		permissions.push(managerPermission);
		deferred.resolve();
	});
	return deferred.promise();
}

Thread.prototype.getApplicationInfosCollectionTag = function(){
	return ACTUALITES_CONFIGURATION.applicationName + '-' + ACTUALITES_CONFIGURATION.infosCollectionName;
}


/* Workspace Service */
WorkspaceService = function(){	
}

WorkspaceService.prototype.getFullPermissionsForActorForResource = function(permission, resource){
	var deferred = $.Deferred();

	http().get('/workspace/share/json/' + resource._id).done(function(data){
		_.each(data.actions, function(action){
			_.each(action.name, function(name){
				if (permission[name] === undefined) {
					permission[name] = true;
				}
			});
		});
		deferred.resolve();
	});
	return deferred.promise();
}

WorkspaceService.prototype.getManagersForResource = function(resource){
	return ([resource.owner]);
}

WorkspaceService.prototype.isManagersForResource = function(actor, resource){
	return (actor === resource.owner);
}

WorkspaceService.prototype.getContributorsForResource = function(resource){
	var actors = [resource.owner];

	if (resource.shared === undefined) {
		return actors;
	}

	_.each(resource.shared, function(share){
		if (share[ACTUALITES_CONFIGURATION.permissions.contributor] === true) {
			if (share['userId'] !== undefined) {
				actors.push(share['userId']);
			}
			else if (share['groupId'] !== undefined) {
				actors.push(share['groupId']);
			}
		}
	});
	return actors;
}


/* Model Build */
model.build = function(){

	model.me.workflow.load(['actualites']);
	this.makeModels([Info, Thread]);
	this.makePermanent(Thread);
	// Info is not using the Permanent System

	this.workspaceService = new WorkspaceService();

	this.latestThread = new Thread();
	this.latestThread.build({
		type: ACTUALITES_CONFIGURATION.threadTypes.latest,
		title: ACTUALITES_CONFIGURATION.threadTypes.latest
	});
};
