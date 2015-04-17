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
		.when('/default', {
			action: 'main'
		})
        .otherwise({
        	redirectTo: '/default'
        });
});

function ActualitesController($scope, template, route, model, $location){

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
					$scope.info = undefined;
					$scope.info = model.infos.find(function(info){
						return info._id === parseInt(params.infoId);
					});
					if ($scope.info !== undefined) {
						if($scope.info.allow('view')) {
							$scope.notFound = false;
							$scope.info.expanded = true;
							$scope.info.thread.selected = true;
							setTimeout(function(){
								window.location.href = window.location.href;
							},100);
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
                            if($scope.info.allow('view') && $scope.info.comments.all.length > 0) {
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
            	template.open('main', 'main');
				model.one('threads.sync', function(){
					model.threads.selectAll();
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
        
        $scope.startDate = moment();
        $scope.appPrefix = 'actualites';
        $scope.currentThread = {};

        // View initialization
		template.open('threadsView', 'threads-view');
        template.open('comments', 'info-comments');
        template.open('infoEdit', 'info-edit');
        template.open('infoView', 'info-view');
		template.open('filters', 'filters');
		template.open('main', 'main');
    };
    
	$scope.sortByIsHeadline = function(info) {
		return info.is_headline;
	};

    $scope.openMainPage = function(){
    	delete $scope.info;
    	delete $scope.currentInfo;
    	window.location.hash = '';
    	template.open('main', 'main');
	};

	$scope.allowForSelection = function(action){
		return _.filter(model.infos.selection(), function(info){
			return !info.allow(action);
		}).length === 0;
	};

	$scope.editInfo = function(info){
		info.edit = true;
		info.expanded = true;
	};

    $scope.createInfo = function(info){
		$scope.currentInfo = new Info();
		template.open('createInfo', 'info-create');
    };
    
    $scope.showShareInfo = function(info) {
    	$scope.display.showInfoSharePanel = true;
    };
    
    $scope.cancelShareInfo = function() {
    	$scope.display.showInfoSharePanel = false;
    };

    $scope.saveDraft = function(){
    	template.close('createInfo');
		$scope.currentInfo.save();
		$scope.currentInfo = new Info();
    };
    
    $scope.saveSubmitted = function(){
    	template.close('createInfo');
		$scope.currentInfo.createPending();
		$scope.currentInfo = new Info();
    };
    
    $scope.savePublished = function(){
    	template.close('createInfo');
		$scope.currentInfo.createPublished();
		$scope.currentInfo = new Info();
    };

	$scope.cancelCreateInfo = function(){
		template.close('createInfo');
		$scope.currentInfo = new Info();
	};

	$scope.openThread = function(thread_id){
		$scope.threads.deselectAll();
		$scope.threads.findWhere({ _id: thread_id }).selected = true;
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

    /* Comments */
    $scope.hasInfoComments = function(info){
        return (info.comments !== undefined && info.comments.length > 0);
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
		model.threads.deselectAll();
		template.open('main', 'threads-view');
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
		return moment(momentDate, ACTUALITES_CONFIGURATION.momentFormat).lang('fr').calendar();
    };
	
	$scope.oneContribRight = function(){
		return model.threads.find(function(thread){
			return thread.myRights.contrib;
		});
	};

	//hack to avoid infinite digest
	var _threadsInSelection = [];
	$scope.threadsInSelection = function(){
		var selectionThreads = model.infos.map(function(info){
			return info.thread;
		});
		if(selectionThreads.length !== _threadsInSelection.length){
			_threadsInSelection = selectionThreads;
		}
		return _threadsInSelection;
	};

	$scope.filterByThreads = function(info){
		return $scope.display['show' + info.status]
			&& _.findWhere($scope.threads.selection(), { _id: info.thread_id })
			&& info.allow('view');
	};
	
    this.initialize();
}