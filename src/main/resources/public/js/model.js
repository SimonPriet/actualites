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
		main: ['published'],
		edition: ['draft', 'pending', 'published'],
		pending: ['pending'],
		drafts: ['draft'],
		trash: ['trash']
	},
	threadTypes: {
		latest: 0
	},
	momentFormat: "YYYY-MM-DD HH:mm.ss.SSS"
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

	http().get(resourceUrl).done(function(content){
		this.updateData({
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
	}.bind(this))
}

Info.prototype.create = function(thread, data){

	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info';

	if (data !== undefined) {
		this.updateData(data);
	}

	var info = {
		title: this.title,
		status: ACTUALITES_CONFIGURATION.infoStatus.DRAFT,
		publicationDate: this.publicationDate,
		expirationDate: this.expirationDate,
		content: this.content
	};
	http().postJson(resourceUrl, info).done(function(e){
		thread.infos.sync();
	}.bind(this));
}

Info.prototype.save = function(thread){

	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/' + ActualitesService.statusNameFromId(this.status);

	var info = {
		title: this.title,
		publicationDate: this.publicationDate,
		expirationDate: this.expirationDate,
		content: this.content
	};
	http().putJson(resourceUrl, info).done(function(e){
		this.load(thread);
	}.bind(this));
}


Info.prototype.submit = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/submit';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	}.bind(this));
}

Info.prototype.unsubmit = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/unsubmit';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	}.bind(this));
}

Info.prototype.publish = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/publish';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	}.bind(this));
}

Info.prototype.unpublish = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/unpublish';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	}.bind(this));
}


Info.prototype.trash = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/trash';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	}.bind(this));
}

Info.prototype.restore = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id + '/restore';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	}.bind(this));
}

Info.prototype.delete = function(thread){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + thread._id + '/info/' + this._id;
	http().delete(resourceUrl);
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

Thread.prototype.build = function(data){
	this.updateData(data);
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
}

Thread.prototype.save = function(){

	var that = this;
	var data = {
		title: this.title,
		icon: this.icon,
		order: this.order,
		mode : this.mode !== undefined ? this.mode : ACTUALITES_CONFIGURATION.threadMode.SUBMIT
	};

	http().putJson('/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this._id, data).done(function(e){
		that.trigger('change');
		model.threads.sync();
	});
}

Thread.prototype.create = function(data){
	
	var that = this;
	if (data !== undefined) {
		this.updateData(data);
	}

	var thread = {
		title: this.title,
		icon: this.icon,
		order: this.order,
		mode : this.mode !== undefined ? this.mode : ACTUALITES_CONFIGURATION.threadMode.SUBMIT
	};
	
	var blob = new Blob([JSON.stringify(thread)], { type: 'application/json'});
	http().postJson('/' + ACTUALITES_CONFIGURATION.applicationName + '/threads', thread).done(function(e){
		that.trigger('change');
		model.threads.sync();
	}.bind(this));
}

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
		resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this._id + '/ALL';
	}
	else {
		resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this._id + '/' + statusFilter + '/ALL';
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
					_.each(thread.infos, function(info){
						info.thread = thread._id;
					});
					collection.addRange(thread.infos);
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
}


ActualitesService = function() {

}

ActualitesService.prototype.loadThreadsCollection = function(model) {

	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/threads';
	
	model.collection(Thread, {
		behaviours: ACTUALITES_CONFIGURATION.applicationName,
		sync: function(){
			this.all = [];
			var collection = this;
			http().get(resourceUrl).done(function(data){
				collection.addRange(data);
				collection.trigger('sync');
			});
		}
	});
}

ActualitesService.prototype.statusNameFromId = function(statusId) {
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

ActualitesService.prototype.loadLatestThread = function(model) {
	model.latestThread = new Thread();
	model.latestThread.build({
		type: ACTUALITES_CONFIGURATION.threadTypes.latest,
		title: ACTUALITES_CONFIGURATION.threadTypes.latest
	});
}


/* Model Build */
model.build = function(){

	model.me.workflow.load([ACTUALITES_CONFIGURATION.applicationName]);
	this.makeModels([Info, Thread]);

	this.actualitesService = new ActualitesService();
	this.actualitesService.loadThreadsCollection(this);
};
