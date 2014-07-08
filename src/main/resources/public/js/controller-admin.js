/* Admin Controller */
function ActualitesAdminController($scope, template, route){

    this.initialize = function(){

        $scope.appPrefix = 'actualites';

        // Dependencies
        $scope.template = template;

        // Threads
        $scope.threads = model.threads;

        // Variables
        $scope.currentThread = {};
        $scope.display = {showPanel: false};

        // Default display
        model.threads.on('sync', function(){
            $scope.$apply("threads");
            $scope.showThreads();
        });

        // $scope.$apply("threads");
    }

    // Thread display
    $scope.hasCurrentThread = function(){
        return (($scope.currentThread instanceof Thread) && ($scope.currentThread.type !== ACTUALITES_CONFIGURATION.threadTypes.latest));
    };

    $scope.showThreads = function(){
        template.open('threads', 'threads-view');
    }

    // Thread edition
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

        $scope.cancelEditThread();
    }

    $scope.cancelEditThread = function(){
        $scope.currentThread = {};
        $scope.showThreads();
    }

    $scope.shareThread = function(thread){
        $scope.currentThread = thread;
        $scope.display.showPanel = true;
    };

	$scope.editThread = function(thread){
		$scope.currentThread = thread;
	}

    // Util
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
