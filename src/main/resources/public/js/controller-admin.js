 routes.define(function($routeProvider){
    $routeProvider
        .when('/thread/:threadId', {
            action: 'viewThread'
        })
  });


 function ActualitesAdminController($scope, template, route){

    route({
        viewThread: function(param){
            $scope.selectThread(new Thread({_id: param.threadId}));
        }
    });

    $scope.template = template;
    $scope.threads = model.privateThreads.mixed;
    $scope.newThread = {};
    $scope.newInfo = {};
    $scope.currentThread = {};
    $scope.currentInfo = {};
    $scope.display = {showPanel: false};

    model.threads.on("mixed.change", function(){
        $scope.$apply("threads");
        $scope.$apply("currentThread");
    });

    template.open('threads_edition', 'threads-edit-view');

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
        return ($scope.currentThread instanceof PrivateThread);
    }
    
}