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
        if ($scope.currentThread._id === undefined) {
            var newThread = new Thread();
            newThread.create($scope.currentThread);
        }
        else {
            $scope.currentThread.save();
        }

        $scope.$scope.cancelEditThread();
    }

    $scope.cancelEditThread = function(){
        $scope.currentThread = {};
        $scope.showThreads();
    }

    $scope.shareThread = function(thread){
        $scope.currentThread = thread;
        $scope.display.showPanel = true;
    }

    $scope.saveShareThread = function(){
        $scope.currentThread.save();
        $scope.cancelShareThread();
    }

    $scope.cancelShareThread = function(){
        $scope.currentThread = {};
        $scope.display.showPanel = false;
    }

    $injector.invoke(ActualitesAbstractController, this, {
        $scope: $scope,
        template: template,
        route: route
    });

    this.initialize();
}