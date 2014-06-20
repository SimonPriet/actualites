/* Admin Controller */
function ActualitesAdminController($injector, $scope, template, route){

    this.initialize = function(){

        // Dependencies
        $scope.template = template;

        // Threads
        $scope.threads = model.threads;

        // Variables
        $scope.currentThread = {};
        $scope.display = {showPanel: false};

        // Default display
        model.threads.on('sync', function(){
            $scope.threads.forEach(function(thread){
                thread.load();
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

    $injector.invoke(ActualitesAbstractController, this, {
        $scope: $scope,
        template: template,
        route: route
    });

    this.initialize();
}
/*
module.directive('sharePanelPlus', function($compile){
    return {
        scope: {
            resources: '=',
            appPrefix: '='
        },
        restrict: 'E',
        templateUrl: '/' + ACTUALITES_CONFIGURATION.applicationName + '/public/template/share-panel-plus.html',
        link: function($scope, $element, $attributes){

        }
    }
});

function ShareActualites($injector, $rootScope, $scope, ui, _, lang){

    this.initialize = function(){
        var actionsConfiguration = {};

        http().get('/' + infraPrefix + '/public/json/sharing-rights.json').done(function(config){
            actionsConfiguration = config;
        });
    }

    $injector.invoke(Share, this, {
        $rootScope: $rootScope,
        $scope: $scope,
        ui: ui,
        _: _,
        lang: lang
    });

    this.initialize();
}
*/
