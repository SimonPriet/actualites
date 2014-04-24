function Info(){

}


function Thread(){
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


function PrivateThread(){
	this.collection(Info);
}

PrivateThread.prototype.create = function(data){

	this.updateData(data);
	this.save();

}

PrivateThread.prototype.addInfo = function(info){
	
	var newInfo = new Info(info);
	this.infos.push(newInfo);
	this.save();

}


model.build = function(){
	model.me.workflow.load(['actualites']);

	this.makeModels([Thread, PrivateThread, Info]);
	this.makePermanent(Thread);
	this.makePermanent(PrivateThread);
};
