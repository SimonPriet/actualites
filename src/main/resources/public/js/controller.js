 routes.define(function($routeProvider){
    $routeProvider
      .when('/thread/:threadId', {
        action: 'viewThread'
      })
  });

function ActualitesController($scope, template, route){

	route({
		viewThread: function(param){
			$scope.currentThread = new Thread({_id: param.threadId});
			$scope.currentThread.open();
		}
	});

	$scope.template = template;
	$scope.threads = model.threads.mixed;
	$scope.newThread = {};
	$scope.newInfo = {};
	$scope.currentThread = {};
	$scope.currentInfo = {};
	$scope.display = {showPanel: false};

	model.threads.on("mixed.change", function(){
    	$scope.$apply("threads");
    	$scope.$apply("currentThread");
    });


	$scope.createThread = function(){
		$scope.currentThread = new Thread;
		template.open('edition', 'thread-edit-form');
	}

	$scope.createInfo = function(){
		$scope.currentInfo = new Info;
		template.open('edition', 'info-edit-form')
	}

	$scope.editInfo = function(info){
		$scope.currentInfo = info;
		template.open('editinfo', 'info-edit-form')
	}

    $scope.saveThread = function(){
    	if ($scope.currentThread._id !== undefined) {
    		$scope.currentThread.save();
    	}
    	else {
	    	var newThread = new Thread;
	    	newThread.create($scope.currentThread);
	    	$scope.currentThread = {};
	    }
    	template.close('edition');
    }

    $scope.saveInfo = function(){
    	if ($scope.currentThread.infos.all.indexOf($scope.currentInfo) !== -1) {
    		$scope.currentThread.save();
    	}
    	else {
    		$scope.currentThread.addInfo($scope.currentInfo);
    	}
    	$scope.currentInfo = {};
    	template.close('edition');
    	template.close('editinfo');
    }

    $scope.hasCurrentThread = function(){
    	return ($scope.currentThread instanceof Thread);
    }

    $scope.selectThread = function(thread){
    	$scope.currentThread = thread;
    	thread.open();
    }

    $scope.removeInfo = function(info){
    	$scope.currentThread.infos.remove(info);
    	$scope.currentThread.save();
    }
}
