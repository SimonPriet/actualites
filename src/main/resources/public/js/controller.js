routes.define(function($routeProvider){
    $routeProvider
        .when('/thread/:threadId', {
            action: 'viewThread'
        })
});

function ActualitesController($scope, template, route, model){

    this.initialize = function(){

        route({
            viewThread: function(param){
            }
        });

        // Model
        $scope.template = template;
        $scope.me = model.me;
        $scope.threads = model.threads;
        $scope.threadFilters = [
            {label: "public", value: ACTUALITES_CONFIGURATION.threadFilters.PUBLIC},
            {label: "all", value: ACTUALITES_CONFIGURATION.threadFilters.ALL}
        ];

        $scope.threadFilter = ACTUALITES_CONFIGURATION.threadFilters.PUBLIC;

        // Variables
        $scope.infos = model.infos;
        $scope.currentInfo = new Info();
        $scope.newComment = new Comment();
        $scope.display = {
			emptyThread: false,
			showCommentsPanel: false,
			showComments: false,
			show3: true
		};

        // View initialization
        template.open('comments', 'info-comments');
        template.open('infoEdit', 'info-edit');
        template.open('infoView', 'info-view');
		template.open('filters', 'filters');
		template.open('main', 'infos-list');
    };

    $scope.hasCurrentThread = function(){
        return (($scope.currentThread instanceof Thread) && ($scope.currentThread.type !== ACTUALITES_CONFIGURATION.threadTypes.latest));
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


    /* Info Edition */
    $scope.infoExists = function(info) {
        return (info._id !== undefined);
    };

    $scope.isInfoEditable = function(info) {
        return info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
    };

    $scope.createInfo = function(info){
		$scope.currentInfo = new Info();
		template.open('main', 'info-create');
    };

	$scope.editInfo = function(info){
		$scope.currentInfo = info;
	};

    $scope.saveInfo = function(){
    	template.open('main', 'infos-list');
		$scope.currentInfo.save();
		$scope.currentInfo = undefined;
    };

    $scope.cancelEditInfo = function(){
		template.open('main', 'infos-list');
        $scope.currentInfo = undefined;
    };

    /* Info Publication */
    $scope.isInfoPublishable = function(info) {
        return info && info._id && info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING;
    };

	$scope.openThread = function(thread){
		$scope.thread = thread;
	};

	$scope.closeThread = function(){
		$scope.thread = undefined;
	};

    $scope.isInfoUnpublishable = function(info) {
        return info && info._id && info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;
    };

    /* Info Submit */
    $scope.isInfoSubmitable = function(info){
        return info && info._id && info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
    };

    $scope.isInfoSubmitted = function(info){
        return info && info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING;
    };

    /* Info Delete */
    $scope.isInfoDeletable = function(info) {
        return info && info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
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

    $scope.postInfoComment = function(info){
        if ((! _.isString($scope.newComment.comment)) || ($scope.newComment.comment.trim() === "")) {
            return;
        }

        info.comment($scope.newComment.comment);
		$scope.newComment = new Comment();

    };

    /* Util */
    $scope.formatDate = function(date){
        return moment(date).format('D MMMM YYYY');
    };

    this.initialize();
}