routes.define(function($routeProvider){
    $routeProvider
        .when('/view/thread/:threadId', {
            action: 'viewThread'
        })
        .when('/view/thread/:threadId/info/:infoId', {
            action: 'viewInfo'
        })
        .when('/view/info/:infoId/comment/:commentId', {
            action: 'viewComment'
        })
        .otherwise({
        	action: 'main'
        });
});

function ActualitesController($scope, template, route, model){

    this.initialize = function(){
    	$scope.notFound = false;
    	
    	route({
    		// Routes viewThread, viewInfo adn viewComment are used by notifications
            viewThread: function(params){
            	model.threads.one('sync', function(){
            		var aThread = model.threads.find(function(thread){
    					return thread._id === parseInt(params.threadId);
    				});
    				if(aThread !== undefined){
    					$scope.notFound = false;
                        $scope.openThread(aThread._id, aThread.thread_title);
    				}
    				else{
						$scope.notFound = true;
                        template.open('error', '404');
    				}
				});
				model.threads.sync();
            },
            viewInfo: function(params){
            	model.infos.one('sync', function() {
    				if(params.infoId !== undefined) {
                        $scope.info = undefined;
                        $scope.info = model.infos.find(function(info){
                            return info._id === parseInt(params.infoId);
                        });
                        if ($scope.info !== undefined) {
                            if($scope.isInfoVisible($scope.info)) {
                                $scope.notFound = false;
                                template.open('main', 'single-info');
                            }
                            else {
                                $scope.notFound = true;
                                template.open('error', '401');
                            }
                        }
                        else {
                            $scope.notFound = true;
                            template.open('error', '404');
                        }
    				}
    				else {
    					$scope.notFound = true;
                        template.open('error', '404');
    				}
				});
				model.infos.sync();
            },
            viewComment: function(params){
            	model.infos.one('sync', function() {
    				if(params.infoId !== undefined) {
                        $scope.info = undefined;
                        $scope.info = model.infos.find(function(info){
                            return info._id === parseInt(params.infoId);
                        });
                        if ($scope.info !== undefined) {
                            if($scope.isInfoVisible($scope.info) && $scope.info.comments.all.length > 0) {
                                $scope.notFound = false;
                                $scope.display.commentInfo = $scope.info;
                                template.open('main', 'single-info');
                            }
                            else {
                                $scope.notFound = true;
                                template.open('error', '401');
                            }
                        }
                        else {
                            $scope.notFound = true;
                            template.open('error', '404');
                        }
    				}
    				else {
    					$scope.notFound = true;
                        template.open('error', '404');
    				}
				});
				model.infos.sync();
            },
            main: function(params){
            	template.open('main', 'infos-list');
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
        
        $scope.startDate = moment();
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
            });
        });

        $scope.loadedThreadsNumber += $scope.loadThreadsIncrement;
    };
    
	$scope.sortByIsHeadline = function(info) {
		return info.is_headline === true;
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
        if ($scope.thread_id && $scope.thread_id !== info.thread_id) {
           return false;
        }

        // For Published Infos, enforce publication and expiration dates if the user has not 'contrib' permission
        if (info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED && (info.myRights.contrib === undefined)) {
            if (info.hasPublicationDate === true) {
                if (moment().isBefore(getDateAsMoment(info.publication_date))) {
                    return false;
                }
            }
            if (info.hasExpirationDate === true) {
                if (moment().isAfter(getDateAsMoment(info.expiration_date).add(1, 'days'))) {
                    return false;
                }
            }
        }
        
        if(info.owner !== model.me.userId 
        		&& info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING 
        		&& info.thread != undefined && info.thread.myRights.publish === undefined
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
	};

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
		$scope.currentInfo = $.extend(true, {}, info);
		// setAsHeadline : temporary variable, to avoid view refresh (due to filter 'orderBy') when editing an info
		$scope.currentInfo.setAsHeadline = $scope.currentInfo.is_headline;
	};
	
    $scope.showDeleteInfo = function(info) {
    	$scope.infoToDelete = info;
    	$scope.display.showConfirmRemove = true;
    };
    
    $scope.cancelDeleteInfo = function() {
    	$scope.infoToDelete = undefined;
    	$scope.display.showConfirmRemove = false;
    };
    
    $scope.deleteInfo = function() {
    	$scope.infoToDelete.delete();
    	$scope.display.showConfirmRemove = false;
    	if($scope.info) {
    		$scope.openMainPage();
    	}
    };
    
    $scope.showShareInfo = function(info) {
    	$scope.infoToShare = info;
    	$scope.display.showInfoSharePanel = true;
    };
    
    $scope.cancelShareInfo = function() {
    	$scope.infoToShare = undefined;
    	$scope.display.showInfoSharePanel = false;
    };

    $scope.saveInfo = function(){
    	if($scope.info) {
    		template.open('main', 'single-info');
    	}
    	else {
    		template.open('main', 'infos-list');
    	}
    	if($scope.currentInfo._id) { // When updating an info, update field 'modified' for the front end
    		$scope.currentInfo.modified = new Object({ $date : moment().toISOString() });
    	}
    	if($scope.currentInfo.setAsHeadline !== undefined) {
        	$scope.currentInfo.is_headline = $scope.currentInfo.setAsHeadline;
    	}
		$scope.currentInfo.save();
    	if($scope.info) {
    		$scope.info.updateData($scope.currentInfo);
    	}
		$scope.currentInfo = undefined;
    };
    
    $scope.saveSubmitted = function(){
    	if($scope.info) {
    		template.open('main', 'single-info');
    	}
    	else {
    		template.open('main', 'infos-list');
    	}
    	if($scope.currentInfo.setAsHeadline !== undefined) {
        	$scope.currentInfo.is_headline = $scope.currentInfo.setAsHeadline;
    	}
		$scope.currentInfo.createPending();
    	if($scope.info) {
    		$scope.info.updateData($scope.currentInfo);
    	}
		$scope.currentInfo = undefined;
    };
    
    $scope.savePublished = function(){
    	if($scope.info) {
    		template.open('main', 'single-info');
    	}
    	else {
    		template.open('main', 'infos-list');
    	}
    	if($scope.currentInfo.setAsHeadline !== undefined) {
        	$scope.currentInfo.is_headline = $scope.currentInfo.setAsHeadline;
    	}
		$scope.currentInfo.createPublished();
    	if($scope.info) {
    		$scope.info.updateData($scope.currentInfo);
    	}
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
        return info && info._id && 
        (info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING ||
        	(info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT && $scope.canSkipPendingStatus(info))
		);
    };

	$scope.openThread = function(thread_id, thread_title){
		$scope.thread_id = thread_id;
		$scope.thread_title = thread_title;
	};

	$scope.closeThread = function(){
		$scope.thread_id = undefined;
		$scope.thread_title = undefined;
	};

    $scope.isInfoUnpublishable = function(info) {
        return info && info._id && info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;
    };

    /* Info Submit */
    $scope.isInfoSubmitable = function(info){
    	var result = false;
    	if(info && info._id && info.status === ACTUALITES_CONFIGURATION.infoStatus.DRAFT) {
    		result = true;
        	if($scope.canSkipPendingStatus(info)){
    			result = false;
    		}
    	}
    	return result;
    };

    $scope.isInfoSubmitted = function(info){
        return info && info.status === ACTUALITES_CONFIGURATION.infoStatus.PENDING;
    };

    $scope.getState = function(info){
    	if(info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED){
    		if(info.hasPublicationDate && moment().isBefore(getDateAsMoment(info.publication_date)) ){
    			// label (A venir)
    			return "actualites.edition.status.4" ;
    		}
    		if(info.hasExpirationDate && moment().isAfter(getDateAsMoment(info.expiration_date).add(1, 'days')) ){
    			// label (Expiree)
    			return "actualites.edition.status.5" ;
    		}
    		if(info.owner !== model.me.userId){
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
		template.open('main', 'thread-edit');
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
	};

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
    	var momentDate = getDateAsMoment(date);
		return moment(momentDate, ACTUALITES_CONFIGURATION.momentFormat).lang('fr').format('dddd DD MMM YYYY');
    };
    
    var getDateAsMoment = function(date){
    	var momentDate;
    	if(moment.isMoment(date)) {
    		momentDate = date;
    	}
    	else if (date.$date) {
			momentDate = moment(date.$date);
		} else if (typeof date === "number"){
			momentDate = moment.unix(date);
		} else {
			momentDate = moment(date);
		}
    	return momentDate;
    };
    
    // Functions to check rights
    $scope.checkThreadsRightsFilter = function(thread){
    	if(thread != undefined){
    		return thread.myRights.contrib !== undefined;
    	}
    	return false;
    };
    
	$scope.checkThreadContibRight = function(thread){
		if(thread != undefined && (thread.owner === model.me.userId || thread.myRights.contrib)){
			return true;
		}
		return false;
	};
	
	$scope.checkOneOrMoreThreadsContibRight = function(){
		var right = false;
		$scope.threads.forEach(function(item){
			if($scope.checkThreadContibRight(item)){
				right = true;
			}
		});
		return right;
	};
	
	$scope.checkThreadsAdminRight = function(){
		return model.me.workflow.actualites.admin;
	};
	
	$scope.hasRightsOnAllThreads = function(){
		var right = true;
		$scope.threads.forEach(function(item){
			if(!$scope.hasRightsOnThread(item)){
				right = false;
			}
		});
		return right;
	};
	
	$scope.hasRightsOnThread = function(thread){
		var right = false;
		if(thread != undefined 
			&& (thread.owner === model.me.userId 
			|| thread.myRights.editThread 
			|| thread.myRights.deleteThread 
			|| thread.myRights.share)
		){
			right = true;
		}
		return right;
	};
	
	$scope.canDeleteComment = function(info, comment){
		var right = false;
		if(comment.author === model.me.userId || (info.thread != undefined && info.thread.myRights.deleteThread)){
			right = true;
		}
		return right;
	};
	
	$scope.canComment = function(info){
		if(info.myRights.comment || (info.thread != undefined && info.thread.myRights.publish)){
			return true;
		}
		return false;
	};
	
	$scope.canPublish = function(thread){
		if(thread !== undefined){
			return (thread.myRights.publish !== undefined);
		}
		return false;
	};
	
	$scope.canSubmit = function(thread){
		if(thread !== undefined){
			return (thread.myRights.submit !== undefined);
		}
		return false;
	};

	$scope.canEditInfo = function(info){
		if(info && info.thread !== undefined && info.status !== undefined){
			switch(info.status){
			case ACTUALITES_CONFIGURATION.infoStatus.DRAFT:
				return (info.owner === model.me.userId);
			case ACTUALITES_CONFIGURATION.infoStatus.PENDING:
				return (info.owner === model.me.userId || info.thread.myRights.publish);
			case ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED:
				return (info.thread.myRights.publish);
			}
		}
		return false;
	};

	$scope.canDeleteInfo = function(info){
		if(info && info.thread !== undefined && info.status !== undefined){
			switch(info.status){
			case ACTUALITES_CONFIGURATION.infoStatus.DRAFT:
				return (info.owner === model.me.userId);
			case ACTUALITES_CONFIGURATION.infoStatus.PENDING:
				return (info.owner === model.me.userId || info.thread.myRights.delete);
			case ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED:
				return (info.thread.myRights.delete);
			}
		}
		return false;
	};
	
	$scope.canShareInfo = function(info){
		if(info !== undefined){
			if (info.owner === model.me.userId || 
				(info.thread != undefined && 
					(info.thread.owner === model.me.userId 
						|| $scope.canPublish(info.thread)))){
				return true;
			}
		}
		return false;
	};

	// A moderator can validate his own drafts (he does not need to go through status 'pending')
	$scope.canSkipPendingStatus = function(info){
		return (info && info.owner === model.me.userId && 
			info.thread && $scope.canPublish(info.thread));
	};
	
    this.initialize();
}