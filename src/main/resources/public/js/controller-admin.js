/* Admin Controller */
function ActualitesAdminController($injector, $scope, template, route){

    this.initialize = function(){

        // Dependencies
        $scope.template = template;

        // Variables
        $scope.threads = model.threads.mixed;
        $scope.currentThread = {};
        $scope.display = {showPanel: false};

        // Default display
        model.threads.on('mixed.sync', function(){
            $scope.threads.forEach(function(thread){
                thread.open();
                thread.on('change', function(){
                    $scope.$apply("threads");
                });
            });
            $scope.showThreads();
        });
    }

    $scope.showThreads = function(){
        template.open('threads', 'threads-view');
    }

    $scope.createThread = function(thread){
        if (thread === undefined) {
            $scope.currentThread = new Thread();
        }
        else {
            $scope.currentThread = thread;
        }
        template.open('threads', 'thread-edit-form');
    }

    $scope.saveThread = function(){

    }

    $scope.cancelEditThread = function(){
        $scope.currentThread = {};
        $scope.showThreads();
    }

    $injector.invoke(ActualitesAbstractController, this, {
        $scope: $scope,
        template: template,
        route: route
    });

    this.initialize();
}