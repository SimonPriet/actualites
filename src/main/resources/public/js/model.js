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
	this.save();

}


model.build = function(){
	model.me.workflow.load(['actualites']);

	this.makeModels([Thread, Info]);
	this.makePermanent(Thread);
};
