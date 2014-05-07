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

    // Dependencies
    $scope.template = template;

    // Configuration
    $scope.loadSize = 4;
    $scope.displayMode = 'latest';

    // Variables
    $scope.latestThread = model.latestThread;
    $scope.threads = model.threads.mixed;
    $scope.infos = {};
    $scope.loadTotal = 0;
    $scope.newInfo = {};
    $scope.currentThread = {};
    $scope.currentInfo = {};
    $scope.display = {showPanel: false};


    /* Thread display */
    $scope.showThread = function(thread){
        $scope.currentThread = thread;
        template.open('thread', 'thread-view');

        // Display modes
        $scope.showLatestInfos();
    }

    $scope.showThreadEdit = function(thread){
        $scope.currentThread = thread;
        template.open('thread', 'threads-edit-view');

        // Display mode
        $scope.showLatestInfos();
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

    $scope.showLatestInfos = function(){
        $scope.displayMode = 'latest';
        $scope.loadTotal = 0;
        
        $scope.currentThread.loadInfos();
        $scope.currentThread.on('loadInfos', function(){
            // Sort by latest modified
            $scope.infos = $scope.currentThread.infos.sortBy(function(info){ 
                return moment() - info.modified; });
            //$scope.$apply("infos");
            $scope.loadMoreInfos();
        });
    }

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
    }

    // Info detailled display
    $scope.backToThreads = function(){
        $scope.currentInfo = {};
        template.open('thread', 'threads-view');
    }


    // Default view
    $scope.showThread(model.latestThread);
}
