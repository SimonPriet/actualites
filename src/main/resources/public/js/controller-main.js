routes.define(function($routeProvider){
    $routeProvider
        .when('/thread/:threadId', {
            action: 'viewThread'
        })
});

/* Main Controller */
function ActualitesMainController($injector, $scope, template, route){

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

	    // Configuration
	    $scope.loadSize = 4;
	    $scope.displayMode = 'latest';

		$scope.loadedThreadsNumber = 0;
		$scope.loadThreadsIncrement = 10;

	    // Special threads
	    $scope.latestThread = model.latestThread;
	    // Threads
        $scope.threads = model.threads.mixed;

        // Variables
	    $scope.infos = {};
	    $scope.loadTotal = 0;
	    $scope.currentThread = {};
	    $scope.currentInfo = {};
        $scope.newComment = {};
	    $scope.display = {showPanel: false, emptyThread: false, showCommentsPanel: false, showComments: false};

	    // Default display
		$scope.selectThread(model.latestThread);
	};

	model.threads.mixed.on('sync', function(){
		$scope.loadNextThreads();
	});

	/* Thread display */
    $scope.selectThread = function(thread){
        $scope.currentThread = thread;
        template.open('thread', 'thread-view');

        // Display modes
        $scope.showLatestInfos();
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

    $scope.getInfoThread = function(info){
        return ($scope.threads.filter(function(thread){
            return thread._id === info.thread;
        })[0].title);
    };

    /* Comments */
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

	$injector.invoke(ActualitesAbstractController, this, {
		$scope: $scope,
		template: template,
		route: route
	});

	this.initialize();
}