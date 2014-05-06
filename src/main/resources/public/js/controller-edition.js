 routes.define(function($routeProvider){
    $routeProvider
        .when('/thread/:threadId', {
            action: 'viewThread'
        })
  });


 function ActualitesEditionController($scope, template, route){

	route({
		viewThread: function(param){
			$scope.selectThread(new Thread({_id: param.threadId}));
		}
	});

	/* Status constants */
	$scope.infoStatus = {
		DRAFT: 0,
		PENDING: 1,
		PUBLISHED: 2
	};

    $scope.pendingPrivateThread = undefined;
    $scope.pendingPublicationThread = undefined;
    $scope.pendingInfo = undefined;

	$scope.template = template;
	$scope.threads = model.privateThreads.mixed;
	$scope.newThread = {};
	$scope.newInfo = {};
	$scope.currentThread = {};
    $scope.currentPublicationThread = {};
	$scope.currentInfo = {};
	$scope.display = {showPanel: false};

    /* Events */
	model.privateThreads.on("mixed.change", function(){
    	$scope.$apply("threads");
    	$scope.$apply("currentThread");
    });

    model.privateThreads.on("mine.change", function(){
        if ($scope.pendingPrivateThread !== undefined) {
            $scope.currentThread = model.privateThreads.mine.last();

            // Creation du Thread de publication
            var newPublicationThread = new Thread;
            newPublicationThread.createFromPrivateThread($scope.currentThread);
            $scope.pendingPublicationThread = newPublicationThread;

            $scope.pendingPrivateThread = undefined;
        }
    });

    model.threads.on("mine.change", function(){
        if ($scope.pendingPublicationThread !== undefined) {

            $scope.currentPublicationThread = model.threads.mine.last();

            // Liaison PrivateThread - PublicationThread
            if ($scope.currentThread.publicationThreadId === undefined) {
                $scope.currentThread.publicationThreadId = $scope.currentPublicationThread._id;
            }
            $scope.currentThread.save();

            $scope.pendingPublicationThread = undefined;
        }
    });

    template.open('threads_edition', 'threads-edit-view');

    /* Thread display */
    $scope.selectThread = function(thread){
    	$scope.currentThread = thread;
        thread.open();

        thread.on('change', function(){
            $scope.currentPublicationThread = model.threads.mixed.find(function(publicationThread){ 
                return publicationThread._id === thread.publicationThreadId; 
            });
            $scope.currentPublicationThread.open();
        });

        template.open('infos_edition', 'infos-edit-view');
    }

    $scope.hasCurrentThread = function(){
        return ($scope.currentThread instanceof PrivateThread);
    }

    $scope.hasCurrentPublicationThread = function(){
        return ($scope.currentPublicationThread instanceof Thread);
    }    

    $scope.hasCurrentInfo = function(){
        return ($scope.currentInfo instanceof Info);
    }


    /* Thread Edition */
    $scope.createThread = function(){
        $scope.currentThread = new PrivateThread;
        template.open('thread_edition', 'thread-edit-form');
    }

    $scope.editThread = function(){
        template.open('thread_edition', 'thread-edit-form');
    }

    $scope.saveThread = function(){
        if ($scope.currentThread._id !== undefined) {
            // Modification du PrivateThread
            $scope.currentThread.save();

            // Modification du PublicationThread
            $scope.currentPublicationThread.updateFromPrivateThread($scope.currentThread);
        }
        else {
        	// Creation du PrivateThread
            var newThread = new PrivateThread;
            newThread.create($scope.currentThread);
            $scope.pendingPrivateThread = newThread;
            $scope.currentThread = {};
        }
        template.close('thread_edition');
    }

    $scope.deleteThread = function(){
        $scope.currentThread.remove();
        $scope.currentPublicationThread.remove();
    }


    /* Info Edition */
    $scope.createInfo = function(){
        $scope.currentInfo = new Info;
        $scope.currentInfo.status = $scope.infoStatus.DRAFT;
        template.open('infos_edition', 'info-edit-form')
    }

    $scope.editInfo = function(info){
        $scope.currentInfo = info;
        template.open('infos_edition', 'info-edit-form')
    }

    $scope.saveInfo = function(){
        $scope.currentInfo.modificationDate = moment();

        // Remove publication date if disabled
        if ($scope.currentInfo.hasPublicationDate === undefined || $scope.currentInfo.hasPublicationDate === false) {
            $scope.currentInfo.publicationDate = undefined;
        }

        // Remove expiration date if disabled
        if ($scope.currentInfo.hasExpirationDate === undefined || $scope.currentInfo.hasExpirationDate === false) {
            $scope.currentInfo.expirationDate = undefined;
        }

        if ($scope.currentThread.infos.all.indexOf($scope.currentInfo) !== -1) {
            // Sort the collection by modificationDate and save
            $scope.currentThread.infos.load($scope.currentThread.infos.sortBy(function(info){ return moment() - moment(info.modificationDate); }));
            $scope.currentThread.save();
        }
        else {
            // Add the new info to the collection (and sort by modificationDate)
            $scope.currentThread.addInfo($scope.currentInfo);
        }
        $scope.currentInfo = {};
        template.close('infos_edition');
    }

    $scope.publishInfo = function(info){
        
        // Lookup the Info in the PublicationThread
        if ($scope.currentPublicationThread.infos.find(function(publishedInfo){
            return publishedInfo.$$hashKey === info.$$hashKey;
        }) !== undefined) {
            // Update the PublicationThread and sort by modificationThread
            $scope.currentPublicationThread.infos.load($scope.currentPublicationThread.infos.sortBy(function(info){ return moment() - moment(info.modificationDate); }));
            $scope.currentPublicationThread.save();
        }
        else {
            // Add the Info the the PublicationThread
            $scope.currentPublicationThread.addInfo(info);
        }
        // Switch the status
        info.status = $scope.infoStatus.PUBLISHED;
        $scope.currentThread.save();
    }

    $scope.unpublishInfo = function(info){
        // Lookup the Info in the PublicationThread
        var publishedInfo = $scope.currentPublicationThread.infos.find(function(publishedInfo){
            return publishedInfo.$$hashKey === info.$$hashKey;
        });

        if (publishedInfo !== undefined) {
            // Remove the info from the PublicationThread
            $scope.currentPublicationThread.infos.remove(publishedInfo);
            $scope.currentPublicationThread.save();
        }

        // Swicth the status
        info.status = $scope.infoStatus.DRAFT;
        $scope.currentThread.save();
    }

    $scope.removeInfo = function(info){
    	$scope.currentThread.infos.remove(info);
    	$scope.currentThread.save();
    }
}