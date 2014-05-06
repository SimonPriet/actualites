/* Info */
function Info(){

}

/* PublicationThread */
function Thread(){
	// title
	// theme
	this.collection(Info);
}

Thread.prototype.create = function(data){
	this.updateData(data);
	this.save();
}

Thread.prototype.addInfo = function(info){
	var newInfo = new Info(info);
	this.infos.push(newInfo);
	this.infos.load(this.infos.sortBy(function(info){ return moment() - moment(info.modificationDate); }));
	this.save();
}

Thread.prototype.createFromPrivateThread = function(data){
	this.title = data.title;
	this.save();
}

Thread.prototype.updateFromPrivateThread = function(privateThread){
	this.title = privateThread.title;
	this.save();
}


/* PrivateThread */
function PrivateThread(){
	// title
	// theme
	// publicationThreadId
	this.collection(Info);
}

PrivateThread.prototype.create = function(data){
	this.updateData(data);
	//this.publicationThreadId = undefined;
	this.save();

}

PrivateThread.prototype.addInfo = function(info){
	var newInfo = new Info(info);
	this.infos.push(newInfo);
	this.infos.load(this.infos.sortBy(function(info){ return moment() - moment(info.modificationDate); }));
	this.save();
}


/* ThreadPolicy */
function ThreadPolicy(){

}


/* ThreadTheme */
function ThreadTheme(){
	// title
	// color
}


/* Model Build */
model.build = function(){

	model.me.workflow.load(['actualites']);

	this.makeModels([Thread, PrivateThread, Info, ThreadPolicy, ThreadTheme]);
	this.makePermanent(Thread);
	this.makePermanent(ThreadPolicy);
	this.makePermanent(PrivateThread);
};
