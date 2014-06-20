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
		$scope.me = model.me;
		template.open('threadsList', 'threads-list');
		template.open('comments', 'info-comments');
		template.open('infoEdit', 'info-edit');
		template.open('infoView', 'info-view');
		$scope.newComment = {};

        // Threads
        $scope.threads = model.threads.mixed;

		$scope.loadThreadsIncrement = 10;

        // Variables
        $scope.infos = {};
        $scope.currentThread = {};
        $scope.currentInfo = {};
        $scope.display = {showPanel: false, emptyThread: false, showCommentsPanel: false, showComments: false};

		$scope.selectThread(model.latestThread);

        // Default display : first thread
        model.threads.on('mixed.sync', function(){
            $scope.showLatestInfos();
			$scope.loadNextThreads();
        });
    };

    // Thread display
    $scope.selectThread = function(thread){
        $scope.currentThread = thread;
        template.open('thread', 'infos-list');

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
			template.open('thread', 'info-create-form')
        }
        else {
            // Info edition
            $scope.currentInfo = info;
        }
    };

    $scope.saveInfo = function(info){
        if (info._id === undefined) {
            // Info creation
            var newInfo = new Info();
            newInfo.create($scope.currentThread, info);
			template.open('thread', 'infos-list');
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
		$scope.currentThread.infos.remove(info);
        $scope.cancelEditInfo();
    };

    $scope.cancelEditInfo = function(){
        $scope.currentInfo = undefined;
    };


    /* Info Publication */
    $scope.isInfoPublishable = function(info) {
        return ((info._id !== undefined) && (info.status !== ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED)
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

	$scope.showLatestInfos = function(){
		$scope.displayMode = 'latest';
		$scope.loadTotal = 0;

		// Load infos for Main View
		$scope.currentThread.loadInfos(ACTUALITES_CONFIGURATION.threadFilters.main);

		$scope.currentThread.infos.on('sync', function(){
			// Sort by latest modified
			$scope.infos = $scope.currentThread.infos.sortBy(function(info){
				return moment() - moment(info.modified, ACTUALITES_CONFIGURATION.momentFormat);
			});

			if ($scope.currentThread.infos.empty()) {
				$scope.display.emptyThread = true;
				$scope.$apply("infos");
			}
			else {
				$scope.display.emptyThread = false;
			}

			$scope.loadMoreInfos();
		});
	};

	$scope.loadMoreInfos = function(){
		$scope.loadTotal = $scope.loadTotal + $scope.loadSize;
		// Open 'loadTotal' threads only
		var i = 0;
		$scope.infos.forEach(function(info){
			if (i++ === $scope.loadTotal) {
				return;
			}
			info.load();
			info.on('change', function(){
				$scope.$apply("infos");
			});
		});
	};

    /* Inheritance */
    $injector.invoke(ActualitesAbstractController, this, {
        $scope: $scope,
        template: template,
        route: route
    });

    this.initialize();
}