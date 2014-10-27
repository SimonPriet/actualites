var actualitesWidget = model.widgets.findWidget("actualites");

// Number of news displayed by the widget
var resultSize = 5;

http().get('/actualites/infos/last/' + resultSize).done(function(threads){
	var enrichedThreads = _.chain(threads).map(function(thread){
		thread.infos.relativeDate = moment(thread.infos.date.$date).lang('fr').fromNow();
		thread.infos.tooltip = lang.translate('actualites.widget.thread') + ' : ' + thread.title + 
			' | ' + lang.translate('actualites.widget.author') + ' : ' + thread.infos.owner.displayName;
		return thread;
	}).value();
	
	actualitesWidget.threads = enrichedThreads;
	model.widgets.apply();
});