/* Root Abstract Controller */
function ActualitesAbstractController($scope, template, route){

    this.initialize = this.initialize || function(){
        // to be overriden
    };

    // Thread display
    $scope.hasCurrentThread = function(){
        return ($scope.currentThread instanceof Thread);
    };

    // Info display
    $scope.isInfoPublished = function(info) {
        return info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;
    };

    $scope.isInfoVisible = function(info) {
        if (info.hasPublicationDate) {
            return (moment().unix() > moment(info.publicationDate).unix());
        }
        if (info.hasExpirationDate) {
            return (moment().unix() < moment(info.expirationDate).unix());
        }
        return $scope.isInfoPublished(info);
    };

    $scope.formatDate = function(date){
        var momentDate = moment(date);
        if (momentDate.isValid()) {
            return momentDate.lang('fr').format('dddd DD MMM YYYY');
        }
        else {
            return moment(date, ACTUALITES_CONFIGURATION.momentFormat).lang('fr').format('dddd DD MMM YYYY');
        }
    };

    /* Comments */
    $scope.hasInfoComments = function(info){
        return (info.comments !== undefined && info.comments.length > 0);
    };

    $scope.getInfoCommentsStatus = function(info, showComments){
        if (showComments) {
            return 'close';
        }
        if ($scope.hasInfoComments(info)) {
            return 'many';
        }
        return 'none';
    };

    $scope.hasCurrentInfo = function(){
        return ($scope.currentInfo instanceof Info);
    };

	$scope.loadNextThreads = function(){
		_.first(model.threads.mixed.rest($scope.loadedThreadsNumber), $scope.loadThreadsIncrement).forEach(function(thread){
			thread.open();
			thread.on('change', function(){
				$scope.$apply('threads');
			})
		});

		$scope.loadedThreadsNumber += $scope.loadThreadsIncrement;
	};
}
