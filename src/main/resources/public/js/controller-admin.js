/* Admin Controller */
function ActualitesAdminController($scope, template, route, model){

    this.initialize = function(){

        $scope.appPrefix = 'actualites';

        // Dependencies
        $scope.template = template;
		template.open('main', 'threads-view');

        // Threads
        $scope.threads = model.threads;

        // Variables
        $scope.currentThread = {};
        $scope.display = {showPanel: false};
    };

    // Thread display
    $scope.hasCurrentThread = function(){
        return (($scope.currentThread instanceof Thread) && ($scope.currentThread.type !== ACTUALITES_CONFIGURATION.threadTypes.latest));
    };

	$scope.newThreadView = function(){
		$scope.currentThread = new Thread();
		template.open('main', 'thread-edit')
	};

	$scope.editSelectedThread = function(){
		$scope.currentThread = model.threads.selection()[0];
		model.threads.deselectAll();
		template.open('main', 'thread-edit');
	};

	$scope.switchAllThreads = function(){
		if($scope.display.selectAllThreads){
			model.threads.selectAll();
		}
		else{
			model.threads.deselectAll();
		}
	}

    $scope.saveThread = function(){
       	$scope.currentThread.save();
        template.open('main', 'threads-view');
		$scope.currentThread = undefined;
    };

    $scope.cancelEditThread = function(){
        $scope.currentThread = undefined;
		template.open('main', 'threads-view');
    };

    // Util
    $scope.formatDate = function(date){
    	var momentDate;
		if (date instanceof Object) {
			momentDate = moment(date.$date);
		} else {
			momentDate = moment(date);
		}
		return moment(momentDate, ACTUALITES_CONFIGURATION.momentFormat).lang('fr').format('dddd DD MMM YYYY');
    };

    this.initialize();
}
