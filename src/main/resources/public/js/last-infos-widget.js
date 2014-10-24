var actualitesWidget = model.widgets.findWidget("actualites");

var resultSize = 5;

http().get('/actualites/infos/last/' + resultSize).done(function(threads){
	actualitesWidget.threads = threads;
	model.widgets.apply();
});