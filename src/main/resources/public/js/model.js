/* Constants */
var ACTUALITES_CONFIGURATION = {
	applicationName: 'actualites',
	infosCollectionName: 'infos',
	threadsCollectionName: 'threads',
	infoStatus: {
		DRAFT: 1,
		PENDING: 2,
		PUBLISHED: 3,
		TRASH: 0
	},
	threadMode: {
		SUBMIT: 0,
		DIRECT: 1
	},
	threadStatus: {
		DRAFT: 'draft',
		PENDING: 'pending',
		PUBLISHED: 'published',
		TRASH: 'trash'
	},
	threadFilters: {
		PUBLIC: 0,
		ALL: 1,
		STATES: 2
	},
	threadTypes: {
		latest: 0
	},
	momentFormat: "YYYY-MM-DD HH:mm.ss.SSS",
	statusNameFromId: function(statusId) {
		if (statusId === ACTUALITES_CONFIGURATION.infoStatus.DRAFT) {
			return ACTUALITES_CONFIGURATION.threadStatus.DRAFT;
		}
		else if (statusId === ACTUALITES_CONFIGURATION.infoStatus.PENDING) {
			return ACTUALITES_CONFIGURATION.threadStatus.PENDING;
		}
		else if (statusId === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED) {
			return ACTUALITES_CONFIGURATION.threadStatus.PUBLISHED;
		}
		else {
			return undefined;
		}
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

Info.prototype.load = function(thread, data){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id;
	if (data !== undefined) {
		resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + data._id;
	}
	var that = this;

	http().get(resourceUrl).done(function(content){
		that.updateData({
			title: content.title,
			status: content.status,
			publicationDate: content.publicationDate,
			hasPublicationDate: content.publicationDate === undefined ? false : true,
			expirationDate: content.expirationDate,
			hasExpirationDate: content.expirationDate === undefined ? false : true,
			content: content.content,
			loaded: true,
			action: undefined,
			comments: content.comments,
			modified: content.modified || this.modified,
			owner: {
				userId: content.owner.userId,
				displayName: content.owner.displayName
			},
			_id: content._id || this._id
		});
		that.trigger('change');
	}.bind(this))
}

Info.prototype.create = function(){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
	http().postJson('/actualites/thread/' + this.thread._id + '/info', this).done(function(e){
		model.latestThread.infos.sync();
	}.bind(this));
}

Info.prototype.saveModifications = function(){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this.thread._id + '/info/' + this._id + '/' + ActualitesService.statusNameFromId(this.status);

	var info = {
		title: this.title,
		publicationDate: this.publicationDate,
		expirationDate: this.expirationDate,
		content: this.content
	};
	http().putJson(resourceUrl, info);
};

Info.prototype.save = function(){
	if(this._id){
		this.saveModifications();
	}
	else{
		this.create();
	}
};


Info.prototype.submit = function(){
	http().put('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/submit');
};

Info.prototype.unsubmit = function(thread){
	http().put('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/unsubmit');
};

Info.prototype.publish = function(thread){
	http().put('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/publish');
};

Info.prototype.unpublish = function(thread){
	http().put('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/unpublish');
}


Info.prototype.trash = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/trash';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	});
}

Info.prototype.restore = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/restore';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	});
}

Info.prototype.delete = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id;
	http().delete(resourceUrl).done(function(){
		thread.infos.sync();
	});
}


Info.prototype.comment = function(thread, commentText){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/comment';
	var info = this;
	http().postJson(resourceUrl, commentText).done(function(){
		info.load(thread);
	}.bind(this));
}



/* Thread */
function Thread(){
	// type (optionnal, for static threads)
	// title
	// icon
	// mode
	// order
	// published
}

Thread.prototype.load = function(data){
	
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this._id;
	if (data !== undefined) {
		resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + data._id;
	}

	var that = this;
	http().get(resourceUrl).done(function(content){
		this.updateData({
			title: content.title,
			icon: content.icon,
			order: content.order,
			mode: content.mode,
			loaded: true,
			modified: content.modified || this.modified,
			owner: content.owner || this.owner,
			ownerName: content.ownerName || this.ownerName,
			_id: content._id || this._id
		});

		that.trigger('change');
	}.bind(this))
};

Thread.prototype.createThread = function(){
	this.mode = this.mode || ACTUALITES_CONFIGURATION.threadMode.SUBMIT;

	http().postJson('/actualites/threads', this).done(function(e){
		model.threads.sync();
	}.bind(this));
};

Thread.prototype.toJSON = function(){
	return {
		mode: this.mode,
		title: this.title,
		icon: this.icon
	}
}

Thread.prototype.saveModifications = function(){
	this.mode = this.mode || ACTUALITES_CONFIGURATION.threadMode.SUBMIT;
	http().putJson('/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this._id, this);
};

Thread.prototype.save = function(){
	if(this._id){
		this.saveModifications();
	}
	else{
		this.createThread();
	}
};

Thread.prototype.loadPublicInfos = function(){
	var resourceUrl;
	if (this.type === ACTUALITES_CONFIGURATION.threadTypes.latest) {
		resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/infos/public/ALL';
	}
	else {
		resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/infos/thread/' + this._id + '/public/ALL';
	}

	this.loadInfosInternal(resourceUrl);
}

Thread.prototype.loadAllInfos = function(statusFilter){
	var resourceUrl;
	if (statusFilter === undefined) {
		resourceUrl = '/actualites/thread/' + this._id + '/infos/ALL';
	}
	else {
		resourceUrl = '/actualites/thread/' + this._id + '/infos/' + statusFilter + '/ALL';
	}

	this.loadInfosInternal(resourceUrl);
}


Thread.prototype.loadInfosInternal = function(resourceUrl){
	var that = this;

	// Bind infos collection
	this.collection(Info, {
		behaviours: ACTUALITES_CONFIGURATION.applicationName,
		sync: function(){
			var collection = this;
			this.all = [];
			http().get(resourceUrl).done(function(data){
				// Prepare data
				_.each(data, function(thread){
					if (thread.infos !== undefined) {
						_.each(thread.infos, function(info){
							info.thread = thread._id;
							info.loaded = true;
						});
						collection.addRange(thread.infos);
					}
				});
				collection.trigger('sync');
			});
		}
	});

	// Trigger loading of infos
	this.infos.sync();
}

Thread.prototype.hasPublishedInfo = function(info){
	return (this.published[info._id] !== undefined);
}

Thread.prototype.pushPublishedInfo = function(info){
	if (info.hasPublicationDate) {
		this.published[info._id] = info.publicationDate;
	}
	else {
		this.published[info._id] = moment().format();
	}
};

Thread.prototype.remove = function(){
	http().delete('/actualites/thread/' + this._id);
};

model.build = function(){

	model.me.workflow.load(['actualites']);
	this.makeModels([Info, Thread]);

	this.latestThread = new Thread({
		type: ACTUALITES_CONFIGURATION.threadTypes.latest,
		title: ACTUALITES_CONFIGURATION.threadTypes.latest
	});

	this.collection(Thread, {
		behaviours: 'actualites',
		sync: function(){
			this.all = [];
			http().get('/actualites/threads').done(function(data){
				this.addRange(data);
				this.trigger('sync');
			}.bind(this));
		},
		removeSelection: function(){
			this.selection().forEach(function(thread){
				thread.remove();
			});

			Collection.prototype.removeSelection.call(this);
		}
	});
};
