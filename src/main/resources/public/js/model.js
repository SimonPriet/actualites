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
	trashFolderTag: 'Trash',
	infoStatus: {
		DRAFT: 0,
		PENDING: 1,
		PUBLISHED: 2
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
		http().put('/workspace/documents/move/' + e._id + '/' + thread._id).done(function(){
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
}

Info.prototype.remove = function(thread){
	if(thread.type === ACTUALITES_CONFIGURATION.threadTypes.trash){
		http().delete('/workspace/document/' + this._id);
	}
	else{
		http().put('/workspace/document/trash/' + this._id);
	}
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

Thread.prototype.loadInfos = function(){
	var thread = this;
	var resourceUrl = '/workspace/documents/' + this._id + '?application=' + this.getApplicationInfosCollectionTag();
	if (this.type === ACTUALITES_CONFIGURATION.threadTypes.latest) {
		resourceUrl = '/workspace/documents' + '?application=' + this.getApplicationInfosCollectionTag();
	}

	// Bind infos collection
	this.collection(Info, {
		behaviours: 'workspace',
		sync: function(){
			http().get(resourceUrl).done(function(data){
				this.load(_.filter(data, function(doc){
					var ok = doc.metadata['content-type'] === 'application/json';
					if(thread.type !== ACTUALITES_CONFIGURATION.threadTypes.trash){
						return ok && doc.folder !== ACTUALITES_CONFIGURATION.trashFolderTag;
					};
					return ok;
				}));
				thread.trigger('loadInfos');
			}.bind(this))
		},
		remove: function(){
			this.selection().forEach(function(info){
				if(thread.type === ACTUALITES_CONFIGURATION.threadTypes.trash){
					http().delete('/workspace/document/' + info._id);
				}
				else{
					http().put('/workspace/document/trash/' + info._id);
				}
			});
			this.removeSelection();
		}
	});

	// Trigger loading of infos
	this.infos.sync();
}

Thread.prototype.getApplicationInfosCollectionTag = function(){
	return ACTUALITES_CONFIGURATION.applicationName + '-' + ACTUALITES_CONFIGURATION.infosCollectionName;
}


/* Model Build */
model.build = function(){

	model.me.workflow.load(['actualites']);
	this.makeModels([Info, Thread]);
	this.makePermanent(Thread);
	// Info is not using the Permanent System

	this.latestThread = new Thread();
	this.latestThread.build({
		type: ACTUALITES_CONFIGURATION.threadTypes.latest,
		title: ACTUALITES_CONFIGURATION.threadTypes.latest
	});
};
