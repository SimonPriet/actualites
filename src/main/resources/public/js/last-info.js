var actualitesWidget = model.widgets.findWidget("actualites");

function Info(){

}

function Thread(){
	this.collection(Info);	
}

model.makeModel(Info);
model.makeModel(Thread);
model.makePermanent(Thread, {fromApplication: 'actualites'});

model.on('threads.sync', function(){
	var thread = model.threads.mixed.all.sort(function(a, b){
		return b.modified.unix() - a.modified.unix()
	})[0];

	if (thread !== undefined) {
		thread.open();

		thread.on('change', function(){
			actualitesWidget.info = thread.infos.all[thread.infos.all.length - 1];
			model.widgets.apply();
		});
	}
});