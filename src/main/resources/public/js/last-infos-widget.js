var actualitesWidget = model.widgets.findWidget("actualites");

// Number of news displayed by the widget
var resultSize = 5;

http().get('/actualites/infos/last/' + resultSize).done(function(data){
	var enrichedInfos = _.chain(data.result).map(function(info){
		info.relativeDate = moment(info.date).lang('fr').fromNow();
		info.tooltip = lang.translate('actualites.widget.thread') + ' : ' + info.thread_title + 
			' | ' + lang.translate('actualites.widget.author') + ' : ' + info.username;
		return info;
	}).value();
	
	actualitesWidget.infos = enrichedInfos;
	model.widgets.apply();
});