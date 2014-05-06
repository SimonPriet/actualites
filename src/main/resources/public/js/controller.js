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
            thread.open();
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

    $scope.isInfoVisible = function(info) {
        if (info.hasPublicationDate) {
            return (moment().unix() > moment(info.publicationDate).unix());
        }
        if (info.hasExpirationDate) {
            return (moment().unix() < moment(info.expirationDate).unix());
        }
        return true;
    }
}
