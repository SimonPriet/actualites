routes.define(function($routeProvider){
    $routeProvider
        .when('/thread/:threadId', {
            action: 'viewThread'
        })
});

/* Edition Controller */
function ActualitesEditionController($injector, $scope, template, route){

    this.initialize = function(){

        route({
            viewThread: function(param){
                $scope.selectThread(new Thread({_id: param.threadId}));
                $scope.currentThread.open();
            }
        });

        // Dependencies
        $scope.template = template;
		template.open('threadsList', 'threads-list');

        // Threads
        $scope.threads = model.threads.mixed;

		$scope.loadThreadsIncrement = 10;

        // Variables
        $scope.infos = {};
        $scope.currentThread = {};
        $scope.currentInfo = {};
        $scope.display = {showPanel: false, emptyThread: false, showCommentsPanel: false, showComments: false};

        // Default display : first thread
        model.threads.on('mixed.sync', function(){
            $scope.selectThread(model.threads.mixed.first());
			$scope.loadNextThreads();
        });
    };

    // Thread display
    $scope.selectThread = function(thread){
        $scope.currentThread = thread;
        template.open('thread', 'threads-edit-view');

        // Display mode
        $scope.showInfos();
    };

    $scope.showInfos = function(){
        // Load infos for Edition view
        $scope.currentThread.loadInfos(ACTUALITES_CONFIGURATION.threadFilters.edition);

        // On Sync refresh
        $scope.currentThread.infos.on('sync', function(){
            // Sort by latest modified
            $scope.infos = $scope.currentThread.infos.sortBy(function(info){ 
                return moment() - moment(info.modified, ACTUALITES_CONFIGURATION.momentFormat); });

            if ($scope.currentThread.infos.empty()) {
                $scope.display.emptyThread = true;
                $scope.$apply("infos");
            }
            else {
                $scope.display.emptyThread = false;
            }

            $scope.infos.forEach(function(info){
                info.load();
                info.on('change', function(){
                    $scope.$apply("infos");
                });
            });

            $location.hash('actualites.container.threads');
            $anchorScroll();
        });
    }


    // Thread Share
    $scope.shareThread = function(){
        $scope.display.showPanel = true;
    }

    $scope.saveShareThread = function(){
        $scope.currentThread.save();
        $scope.cancelShareThread();
    }

    $scope.cancelShareThread = function(){
        $scope.display.showPanel = false;
    }


    // Info display
    $scope.selectInfo = function(info){
        $scope.currentInfo = info;
    };

    // Info Edition
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
        }
        else {
            // Info edition
            $scope.currentInfo = info;
        }
        template.open('thread', 'info-edit-form');
    };

    $scope.saveInfo = function(info){
        if (info._id === undefined) {
            // Info creation
            var newInfo = new Info();
            newInfo.create($scope.currentThread, info);
        }
        else {
            // Info edition
            info.save($scope.currentThread);
        }

        $scope.cancelEditInfo();
    }

    $scope.isInfoDeletable = function(info) {
        return (info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT || info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING);
    };

    $scope.deleteInfo = function(info){
        info.remove($scope.currentThread);
        $scope.cancelEditInfo();
    };

    $scope.cancelEditInfo = function(){
        $scope.currentInfo = {};
        template.open('thread', 'threads-edit-view');
    };


    /* Info Publication */
    $scope.isInfoPublishable = function(info) {
        return ((info._id !== undefined) && (info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING)
            && model.me.userId === $scope.currentThread.owner);
    }

    $scope.isInfoUnpublishable = function(info) {
        return ((info._id !== undefined) && (info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED)
            && model.me.userId === $scope.currentThread.owner);    
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


    /* Inheritance */
    $injector.invoke(ActualitesAbstractController, this, {
        $scope: $scope,
        template: template,
        route: route
    });

    this.initialize();
}