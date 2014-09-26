routes.define(function($routeProvider){
    $routeProvider
        .when('/view/thread/:threadId', {
            action: 'viewThread'
        })
        .when('/view/thread/:threadId/info/:infoId', {
            action: 'viewInfo'
        })
});

function ActualitesController($scope, template, route, model){

    this.initialize = function(){
    	$scope.notFound = false;
    	
    	route({
    		// Routes viewThread and viewInfo are used by notifications
            viewThread: function(params){
            	model.threads.one('sync', function(){
            		var aThread = model.threads.find(function(thread){
    					return thread._id === params.threadId;
    				});
    				if(aThread === undefined){
    					$scope.notFound = true;
    					template.open('error', '404');
    				}
    				else{
						if($scope.checkThreadsAdminRight()){
							$scope.notFound = false;
							$scope.threadsView();
						}
						else{
							$scope.notFound = true;
							template.open('error', '401');
						}
    				}
				});
            },
            viewInfo: function(params){
            	model.threads.one('sync', function(){
            		var aThread = model.threads.find(function(thread){
    					return thread._id === params.threadId;
    				});
    				if(aThread === undefined){
    					$scope.notFound = true;
    					template.open('error', '404');
    				}
    				else{
    					if($scope.checkThreadsAdminRight()){
    						$scope.infos.one('sync', function(){
        						$scope.info = undefined;
        						$scope.info = model.infos.find(function(info){
        							return info._id === params.infoId;
        						});
        						if($scope.info === undefined){
        							$scope.notFound = true;
        							template.open('error', '404');
        						}
        						else{
        							if($scope.isInfoVisible($scope.info)){
        								$scope.notFound = false;
										$scope.setFilter($scope.info.status);
										template.open('main', 'single-info');
        							}
        							else{
        								$scope.notFound = true;
        								template.open('error', '401');
        							}
        						}
        					});
						}
						else{
							$scope.notFound = true;
							template.open('error', '401');
						}
    				}
				});
            }
        });

        // Model
        $scope.template = template;
        $scope.me = model.me;
        $scope.threads = model.threads;
        $scope.threadFilters = [
            {label: "public", value: ACTUALITES_CONFIGURATION.threadFilters.PUBLIC},
            {label: "all", value: ACTUALITES_CONFIGURATION.threadFilters.ALL}
        ];

        $scope.threadFilter = ACTUALITES_CONFIGURATION.threadFilters.PUBLIC;

        // Variables
        $scope.infos = model.infos;
        $scope.currentInfo = new Info();
        $scope.newComment = new Comment();
        $scope.display = {
			emptyThread: false,
			showCommentsPanel: false,
			showComments: false,
			show1: true,
			show2: true,
			show3: true
		};
        
        $scope.appPrefix = 'actualites';
        $scope.currentThread = {};

        // View initialization
		template.open('threadsView', 'threads-view');
        template.open('comments', 'info-comments');
        template.open('infoEdit', 'info-edit');
        template.open('infoView', 'info-view');
		template.open('filters', 'filters');
		template.open('main', 'infos-list');
    };

    $scope.hasCurrentThread = function(){
        return (($scope.currentThread instanceof Thread) && ($scope.currentThread.type !== ACTUALITES_CONFIGURATION.threadTypes.latest));
    };

    $scope.loadNextThreads = function(){
        _.first(model.threads.mixed.rest($scope.loadedThreadsNumber), $scope.loadThreadsIncrement).forEach(function(thread){
            thread.open();
            thread.on('change', function(){
                $scope.$apply('threads');
            })
        });

        $scope.loadedThreadsNumber += $scope.loadThreadsIncrement;
    };
    
    $scope.isInfoPublished = function(info) {
        return info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;
    };

    $scope.isInfoVisible = function(info) {
        // Selected Filters
        if (! $scope.display['show' + info.status]) {
           return false;
        }
        // Selected Thread
        if ($scope.thread && $scope.thread !== info.thread) {
           return false;
        }

        // For Published Infos, enforce publication and expiration dates if the user has not 'contrib' permission
        if (info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED && (info.myRights.contrib === undefined)) {
            if (info.publicationDate !== undefined && info.publicationDate !== null) {
                if (moment().unix() < moment(info.publicationDate).unix()) {
                    return false;
                }
            }
            if (info.expirationDate !== undefined && info.expirationDate !== null) {
                if (moment().unix() > moment(info.expirationDate).unix()) {
                    return false;
                }
            }
        }
        
        if(info.owner.userId !== model.me.userId 
        		&& info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING 
        		&& info.thread.myRights.publish === undefined
          ){
        	return false;
        }
        return true;
    };

    $scope.openMainPage = function(){
    	delete $scope.info;
    	delete $scope.currentInfo;
    	window.location.hash = '';
		template.open('main', 'infos-list');
	}

    /* Info Edition */
    $scope.infoExists = function(info) {
        return (info._id !== undefined);
    };

    $scope.isInfoEditable = function(info) {
        return info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
    };

    $scope.createInfo = function(info){
		$scope.currentInfo = new Info();
		template.open('main', 'info-create');
    };

	$scope.editInfo = function(info){
		$scope.currentInfo = info;
	};
	
    $scope.showDeleteInfo = function(info) {
    	$scope.infoToDelete = info;
    	$scope.display.showConfirmRemove = true;
    }
    
    $scope.cancelDeleteInfo = function() {
    	$scope.infoToDelete = undefined;
    	$scope.display.showConfirmRemove = false;
    }
    
    $scope.deleteInfo = function() {
    	$scope.infoToDelete.delete();
    	$scope.display.showConfirmRemove = false;
    	if($scope.info) {
    		$scope.openMainPage();
    	}
    }

    $scope.saveInfo = function(){
    	if($scope.info) {
    		template.open('main', 'single-info');
    	}
    	else {
    		template.open('main', 'infos-list');
    	}
		$scope.currentInfo.save();
		$scope.currentInfo = undefined;
    };

    $scope.cancelEditInfo = function(){
    	if($scope.info) {
    		template.open('main', 'single-info');
    	}
    	else {
    		template.open('main', 'infos-list');
    	}
        $scope.currentInfo = undefined;
    };

    /* Info Publication */
    $scope.isInfoPublishable = function(info) {
        return info && info._id && info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING;
    };

	$scope.openThread = function(thread){
		$scope.thread = thread;
	};

	$scope.closeThread = function(){
		$scope.thread = undefined;
	};

    $scope.isInfoUnpublishable = function(info) {
        return info && info._id && info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;
    };

    /* Info Submit */
    $scope.isInfoSubmitable = function(info){
        return info && info._id && info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
    };

    $scope.isInfoSubmitted = function(info){
        return info && info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING;
    };

    $scope.getState = function(info){
    	if(info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED){
    		if(info.hasPublicationDate && (moment().unix() < moment(info.publicationDate).unix())){
    			// label (A venir)
    			return "actualites.edition.status.4" ;
    		}
    		if(info.expirationDate && (moment().unix() > moment(info.expirationDate).unix())){
    			// label (Expiree)
    			return "actualites.edition.status.5" ;
    		}
    		if(info.owner.userId !== model.me.userId){
    			return "actualites.edition.status.empty";
    		}
    	}
    	return "actualites.edition.status." + info.status;
    };
	
	$scope.setFilter = function(state){
    	switch(state) {
			case ACTUALITES_CONFIGURATION.infoStatus.DRAFT:
				$scope.display.show1 = true;
				$scope.display.show2 = false;
				$scope.display.show3 = false;
				break;
			case ACTUALITES_CONFIGURATION.infoStatus.PENDING:
				$scope.display.show1 = false;
				$scope.display.show2 = true;
				$scope.display.show3 = false;
				break;
			case ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED:
				$scope.display.show1 = false;
				$scope.display.show2 = false;
				$scope.display.show3 = true;
				break;
			default:
				$scope.display.show1 = true;
				$scope.display.show2 = true;
				$scope.display.show3 = true;
		}
    };

    /* Info Delete */
    $scope.isInfoDeletable = function(info) {
        return info && info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
    };

    /* Comments */
    $scope.hasInfoComments = function(info){
        return (info.comments !== undefined && info.comments.length > 0);
    };

    $scope.getInfoCommentsStatus = function(info, showComments){
        if (showComments) {
            return 'close';
        }
        if ($scope.hasInfoComments(info)) {
            return 'many';
        }
        return 'none';
    };

    $scope.postInfoComment = function(info){
        if ((! _.isString($scope.newComment.comment)) || ($scope.newComment.comment.trim() === "")) {
            return;
        }

        info.comment($scope.newComment.comment);
		$scope.newComment = new Comment();

    };
    
    // Threads
    $scope.threadsView = function(){
		template.open('main', 'threads-view');
	};
	
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
			model.threads.forEach(function(item){
				if($scope.hasRightsOnThread(item)){
					item.selected = true;
				}
			});
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

    /* Util */
    $scope.formatDate = function(date){
    	var momentDate;
		if (date instanceof Object) {
			momentDate = moment(date.$date);
		} else {
			momentDate = moment(date);
		}
		return moment(momentDate, ACTUALITES_CONFIGURATION.momentFormat).lang('fr').format('dddd DD MMM YYYY');
    };

    $scope.checkThreadsRightsFilter = function(category){
    	return category.myRights.submit !== undefined;
	};
	
	$scope.checkThreadsSubmitRight = function(){
		var right = false;
		$scope.threads.forEach(function(item){
			if(item.myRights.submit){
				right = true;
			}
		});
		return right;
	};
	
	$scope.checkThreadsAdminRight = function(){
		return model.me.workflow.actualites.admin;
	}
	
	$scope.hasRightsOnAllThreads = function(){
		var right = false;
		$scope.threads.forEach(function(item){
			if(item.myRights.editThread || item.myRights.deleteThread || item.myRights.share){
				right = true;
			}
		});
		return right;
	};
	
	$scope.hasRightsOnThread = function(thread){
		var right = false;
		if(thread.myRights.editThread || thread.myRights.deleteThread || thread.myRights.share){
			right = true;
		}
		return right;
	};

    this.initialize();
}