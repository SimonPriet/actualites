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
                $scope.selectThread(new Thread({_id: param.threadId}));
            }
        });

        // Model
        $scope.template = template;
        $scope.me = model.me;
        $scope.threads = model.threads;
        $scope.threadFilters = [
            {label: "public", value: ACTUALITES_CONFIGURATION.threadFilters.PUBLIC},
            {label: "all", value: ACTUALITES_CONFIGURATION.threadFilters.ALL}
        ]

        // Defaults
        $scope.loadThreadsIncrement = 10;
        $scope.threadFilter = ACTUALITES_CONFIGURATION.threadFilters.PUBLIC;

        // Variables
        $scope.infos = model.latestThread.infos;
        $scope.currentThread = {};
        $scope.currentInfo = new Info();
        $scope.newComment = {};
        $scope.display = {showPanel: false, emptyThread: false, showCommentsPanel: false, showComments: false};

        // View initialization
        template.open('threadsList', 'threads-list');
        template.open('comments', 'info-comments');
        template.open('infoEdit', 'info-edit');
        template.open('infoView', 'info-view');
		template.open('filters', 'filters');

        // Default display : Latest Thread
        $scope.selectLatestThread();
    };

    /* Thread display */
    $scope.selectThread = function(thread){
        $scope.currentThread = thread;
        template.open('thread', 'infos-list');

        $scope.refreshThread();
    };

    $scope.refreshThread = function(){
        // Load infos
        // Bizarre : $scope.threadFilter vs this.threadFilter ...
        $scope.threadFilter = this.threadFilter
        if (this.threadFilter === ACTUALITES_CONFIGURATION.threadFilters.PUBLIC) {
            $scope.currentThread.loadPublicInfos();
        }
        else {
            $scope.currentThread.loadAllInfos();
        }

        $scope.showInfos();
    };

    $scope.selectLatestThread = function() {
        $scope.threadFilter = ACTUALITES_CONFIGURATION.threadFilters.PUBLIC;
        $scope.currentThread = model.latestThread;
        template.open('thread', 'infos-list');

        // Load only public infos
        $scope.currentThread.loadPublicInfos();
        $scope.showInfos();
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


    /* Infos list */
    $scope.showInfos = function(filter){
        // On Sync refresh
        $scope.currentThread.infos.on('sync', function(){
            // Sort by latest modified
            $scope.infos = $scope.currentThread.infos.sortBy(function(info){ 
                return moment() - moment(info.modified, ACTUALITES_CONFIGURATION.momentFormat); });

            if ($scope.currentThread.infos.empty()) {
                $scope.display.emptyThread = true;
            }
            else {
                $scope.display.emptyThread = false;
            }

            $scope.$apply("infos");
        });
    };


    /* Info display */
    $scope.selectInfo = function(info){
        $scope.currentInfo = info;
        info.on('change', function(){
            $scope.$apply("infos");
        });
    };

    $scope.hasCurrentInfo = function(){
        return ($scope.currentInfo instanceof Info);
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
        if (info === undefined) {
            // Info creation
            $scope.currentInfo = new Info();
            $scope.currentInfo.status = ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
            template.open('thread', 'info-create')
        }
        else {
            // Info edition
            $scope.currentInfo = info;
        }
    };

    $scope.saveInfo = function(){
    	template.open('thread', 'infos-list');
		$scope.currentInfo.save();
    };

    $scope.cancelEditInfo = function(){
		template.open('thread', 'infos-list');
        $scope.currentInfo = undefined;
    };

    /* Info Publication */
    $scope.isInfoPublishable = function(info) {
        return ((info._id !== undefined) && (info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING));
    }

    $scope.isInfoUnpublishable = function(info) {
        return ((info._id !== undefined) && (info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED));    
    }
    
    $scope.publishInfo = function(info){
        info.loaded = false;
        info.action = "publish";
        info.publish($scope.currentThread);
    };

    $scope.unpublishInfo = function(info){
        info.loaded = false;
        info.action = "unpublish";
        info.unpublish($scope.currentThread);
    };
    
    $scope.switchPublish = function(info){
        if($scope.isInfoPublishable(info)){
            $scope.publishInfo(info);
        }
        else{
            $scope.unpublishInfo(info);
        }
    };


    /* Info Submit */
    $scope.isInfoSubmitable = function(info){
        return (info._id !== undefined) && (info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT);
    };

    $scope.isInfoSubmited = function(info){
        return info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING;
    };

    $scope.submitInfo = function(info){
        info.loaded = false;
        info.action = "submit";
        info.submit($scope.currentThread);
    };

    $scope.unsubmitInfo = function(info){
        info.loaded = false;
        info.action = "unsubmit";
        info.unsubmit($scope.currentThread);
    };


    /* Info Trash */
    $scope.isInfoTrashable = function(info){
        return (model.me.userId === info.owner.userId) && (info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT);
    };

    $scope.isInfoRestorable = function(info){
        return (model.me.userId === info.owner.userId) && (info.status === ACTUALITES_CONFIGURATION.infoStatus.TRASH);
    };

    $scope.trashInfo = function(info){
        info.loaded = false;
        info.action = "trash";
        info.trash($scope.currentThread);
    };

    $scope.restoreInfo = function(info){
        info.loaded = false;
        info.action = "restore";
        info.restore($scope.currentThread);
    };

    /* Info Delete */
    $scope.isInfoDeletable = function(info) {
        return (info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT);
    };

    $scope.deleteInfo = function(info){
        $scope.currentThread.infos.remove(info);
        $scope.cancelEditInfo();
    };


    /* Thread Share */
    $scope.shareThread = function(){
        $scope.display.showPanel = true;
    };

    $scope.saveShareThread = function(){
        $scope.currentThread.save();
        $scope.cancelShareThread();
    };

    $scope.cancelShareThread = function(){
        $scope.display.showPanel = false;
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

        info.comment($scope.newComment.comment).done(function(){
            if (info.comments === undefined) {
                info.comments = [];
            }

            info.comments.push({
                author: model.me.userId,
                authorName: model.me.username,
                comment: $scope.newComment.comment,
                posted: undefined
            });

            $scope.newComment = {};
            $scope.$apply("currentInfo");
        });
    };


    /* Util */
    $scope.formatDate = function(date){
        var momentDate = moment(date);
        if (momentDate.isValid()) {
            return momentDate.lang('fr').format('dddd DD MMM YYYY');
        }
        else {
            return moment(date, ACTUALITES_CONFIGURATION.momentFormat).lang('fr').format('dddd DD MMM YYYY');
        }
    };

    this.initialize();
}