 routes.define(function($routeProvider){
    $routeProvider
        .when('/thread/:threadId', {
            action: 'viewThread'
        })
  });


function ActualitesController($scope, template, route){

    route({
        viewThread: function(param){
            $scope.showThread(new Thread({_id: param.threadId}));
            $scope.currentThread.open();
        }
    });

    // Dependancies
    $scope.template = template;

    // Configuration
    $scope.loadSize = 4;
    $scope.infoLoadSize = 4;
    $scope.displayMode = 'latest';

    // Variables
    $scope.loadTotal = 0;
    $scope.newThread = {};
    $scope.newInfo = {};
    $scope.currentThread = {};
    $scope.currentInfo = {};
    $scope.display = {showPanel: false};

    // Default view
    template.open('main', 'threads-view');

    model.threads.on('mixed.sync', function(){
        $scope.showLatestThreads();
        $scope.loadMoreThreads();
    });

    model.threads.on("mixed.change", function(){
        $scope.$apply("threads");
        $scope.$apply("currentThread");
    });

    /* Page presentations */
    $scope.showLatestThreads = function(){
        $scope.displayMode = 'latest';
        // Sort by latest modified
        $scope.threads = model.threads.mixed.sortBy(function(thread){ return moment() - thread.modified; });
    }

    $scope.showMyThreadsFirst = function(){
        $scope.displayMode = 'mine';
        // Filter by owner is me
        $scope.threads = model.threads.mine;
    }

    $scope.loadMoreThreads = function(){
        $scope.loadTotal = $scope.loadTotal + $scope.loadSize;
        // Open 'loadTotal' threads only
        var i = 0;
        $scope.threads.forEach(function(thread){
            if (i++ === $scope.loadTotal) {
                return;
            }
            thread.open()
            thread.on('change', function(){
                $scope.$apply("threads");
            });
        });
    }

    $scope.backToThreads = function(){
        $scope.currentThread = {};
        template.open('main', 'threads-view');
    }

    /* Thread display */
    $scope.showThread = function(thread){
        $scope.currentThread = thread;
        template.open('main', 'thread-view');
    }

    $scope.hasCurrentThread = function(){
        return ($scope.currentThread instanceof Thread);
    }

    $scope.hasCurrentInfo = function(){
        return ($scope.currentInfo instanceof Info);
    }
}


function ActualitesEditionController($scope, template, route){

	route({
		viewThread: function(param){
			$scope.selectThread(new Thread({_id: param.threadId}));
		}
	});

	$scope.template = template;
	$scope.threads = model.threads.mixed;
	$scope.newThread = {};
	$scope.newInfo = {};
	$scope.currentThread = {};
    $scope.currentPrivateThread = {};
	$scope.currentInfo = {};
	$scope.display = {showPanel: false};

	model.threads.on("mixed.change", function(){
    	$scope.$apply("threads");
    	$scope.$apply("currentThread");
    });

    /* Thread display */
    $scope.selectThread = function(thread){
    	$scope.currentThread = thread;
        /*
        $scope.currentPrivateThread = model.privateThreads.find(function(item){
            return item._id = thread._id;
        });
        */
        thread.open();
        template.open('infos_edition', 'infos-edit-view')
    }

    $scope.hasCurrentThread = function(){
        return ($scope.currentThread instanceof Thread);
    }

    $scope.hasCurrentInfo = function(){
        return ($scope.currentInfo instanceof Info);
    }


    /* Thread and Info edition */
    $scope.createThread = function(){
        $scope.currentThread = new Thread;
        /*
        $scope.currentPrivateThread = new PrivateThread;
        $scope.currentThread.privateId = $scope.currentPrivateThread._id;
        */
        template.open('thread_edition', 'thread-edit-form');
    }

    $scope.createInfo = function(){
        $scope.currentInfo = new Info;
        template.open('infos_edition', 'info-edit-form')
    }

    $scope.editInfo = function(info){
        $scope.currentInfo = info;
        template.open('infos_edition', 'info-edit-form')
    }

    $scope.saveThread = function(){
        if ($scope.currentThread._id !== undefined) {
            //model.threads.mixed.load(model.threads.sortBy(function(thread){ return moment() - thread.modified}));
            $scope.currentThread.save();
        }
        else {
            var newThread = new Thread;
            newThread.create($scope.currentThread);
            $scope.currentThread = {};
        }
        template.close('thread_edition');
    }

    $scope.saveInfo = function(){
        $scope.currentInfo.modificationDate = moment();

        if ($scope.currentInfo.hasPublicationDate === undefined || $scope.currentInfo.hasPublicationDate === false) {
            $scope.currentInfo.publicationDate = undefined;
        }

        if ($scope.currentInfo.hasExpirationDate === undefined || $scope.currentInfo.hasExpirationDate === false) {
            $scope.currentInfo.expirationDate = undefined;
        }

        if ($scope.currentThread.infos.all.indexOf($scope.currentInfo) !== -1) {
            $scope.currentThread.infos.load($scope.currentThread.infos.sortBy(function(info){ return moment() - moment(info.modificationDate); }));
            $scope.currentThread.save();
        }
        else {
            $scope.currentThread.addInfo($scope.currentInfo);
        }
        $scope.currentInfo = {};
        template.close('infos_edition');
    }

    $scope.removeInfo = function(info){
    	$scope.currentThread.infos.remove(info);
    	$scope.currentThread.save();
    }
}
