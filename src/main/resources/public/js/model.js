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

function Comment(){

}

function Info(data){
	if(!data || !data.thread){
		this.thread = model.threads.first();
	}

	this.collection(Comment);
	if(data && data.comments){
		this.comments.load(data.comments);
	}

}

Info.prototype.toJSON = function(){
	return {
		title: this.title,
		publicationDate: this.hasPublicationDate ? this.publicationDate : null,
		expirationDate: this.hasExpirationDate ? this.expirationDate : null,
		content: this.content
	};
}

Info.prototype.create = function(){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
	http().postJson('/actualites/thread/' + this.thread._id + '/info', this).done(function(e){
		model.infos.sync();
	}.bind(this));
}

Info.prototype.saveModifications = function(){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this.thread._id + '/info/' + this._id + '/' + ACTUALITES_CONFIGURATION.statusNameFromId(this.status);
	http().putJson(resourceUrl, this);
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
	this.status = ACTUALITES_CONFIGURATION.infoStatus.PENDING;
	http().put('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/submit');
};

Info.prototype.unsubmit = function(){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
	http().put('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/unsubmit');
};

Info.prototype.publish = function(){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;
	http().put('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/publish');
};

Info.prototype.unpublish = function(){
	this.status = ACTUALITES_CONFIGURATION.infoStatus.PENDING;
	http().put('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/unpublish');
}


Info.prototype.trash = function(){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this.thread._id + '/info/' + this._id + '/trash';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	});
}

Info.prototype.restore = function(){
	var resourceUrl = '/' + ACTUALITES_CONFIGURATION.applicationName + '/thread/' + this.thread._id + '/info/' + this._id + '/restore';	
	var info = this;
	http().put(resourceUrl).done(function(){
		info.load(thread);
	});
}

Info.prototype.delete = function(){
	http().delete('/actualites/thread/' + this.thread._id + '/info/' + this._id).done(function(){
		model.infos.sync();
	});
}


Info.prototype.comment = function(commentText){
	http().putJson('/actualites/thread/' + this.thread._id + '/info/' + this._id + '/comment', { comment: commentText });
	this.comments.push(new Comment({
		author: model.me.userId,
		authorName: model.me.username,
		comment: commentText,
		posted: moment()
	}));
};

function Thread(){
	// type (optionnal, for static threads)
	// title
	// icon
	// mode
	// order
	// published
}

Thread.prototype.load = function(data){
	var resourceUrl = '/actualites/thread/' + this._id;
	if (data !== undefined) {
		resourceUrl = '/actualites/thread/' + data._id;
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
};

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
		resourceUrl = '/actualites/infos/ALL';
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
};

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
	this.makeModels([Info, Thread, Comment]);

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

	this.collection(Info, {
		sync: function(){
			http().get('/actualites/infos/all').done(function(data){
				var that = this;
				this.all = [];
				data.forEach(function(thread){
					var infos = _.filter(thread.infos, function(info){
						return info.status > 1 || info.owner.userId === model.me.userId;
					});
					if (!infos || !infos.length) {
						return;
					}

					infos.forEach(function(info){
						info.thread = model.threads.findWhere({ _id: thread._id });

						if (info.publicationDate) {
							info.hasPublicationDate = true;
						}

						if (info.expirationDate) {
							info.hasExpirationDate = true;
						}
					});
					that.addRange(infos);
				});
				this.trigger('sync');
			}.bind(this))
		},
		behaviours: 'actualites'
	})
};
