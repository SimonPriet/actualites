 routes.define(function($routeProvider){
    $routeProvider
        .when('/thread/:threadId', {
            action: 'viewThread'
        })
  });


function ActualitesController($scope, template, route){

    route({
        viewThread: function(param){
            $scope.selectThread(new Thread({_id: param.threadId}));
            $scope.currentThread.open();
        }
    });

    // Dependencies
    $scope.template = template;

    // Configuration
    $scope.loadSize = 4;
    $scope.displayMode = 'latest';

    // Variables
    $scope.latestThread = model.latestThread;
    $scope.threads = model.threads.mixed;
    $scope.infos = {};
    $scope.loadTotal = 0;
    $scope.currentThread = {};
    $scope.currentInfo = {};
    $scope.display = {showPanel: false};


    /* Thread display */
    $scope.selectThread = function(thread){
        $scope.currentThread = thread;
        template.open('thread', 'thread-view');

        // Display modes
        $scope.showLatestInfos();
    }

    $scope.selectThreadEdit = function(thread){
        $scope.currentThread = thread;
        template.open('thread', 'threads-edit-view');

        // Display mode
        $scope.showLatestInfos();
    }    

    $scope.hasCurrentThread = function(){
        return ($scope.currentThread instanceof Thread);
    }

    $scope.showLatestInfos = function(){
        $scope.displayMode = 'latest';
        $scope.loadTotal = 0;
        
        $scope.currentThread.loadInfos();
        $scope.currentThread.on('loadInfos', function(){
            // Sort by latest modified
            $scope.infos = $scope.currentThread.infos.sortBy(function(info){ 
                return moment() - info.modified; });
            //$scope.$apply("infos");
            $scope.loadMoreInfos();
        });
    }

    $scope.loadMoreInfos = function(){
        $scope.loadTotal = $scope.loadTotal + $scope.loadSize;
        // Open 'loadTotal' threads only
        var i = 0;
        $scope.infos.forEach(function(info){
            if (i++ === $scope.loadTotal) {
                return;
            }
            info.load();
            info.on('change', function(){
                $scope.$apply("infos");
            });
        });
    }


    // Info detailled display
    $scope.selectInfo = function(info){
        $scope.currentInfo = info;
    }

    $scope.hasCurrentInfo = function(){
        return ($scope.currentInfo instanceof Info);
    }

    $scope.isInfoPublished = function(info) {
        return info.status === ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;
    }

    $scope.isInfoPublishable = function(info) {
        return (info._id !== undefined) && (info.status !== ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED);
    }

    $scope.isInfoVisible = function(info) {
        if (info.hasPublicationDate) {
            return (moment().unix() > moment(info.publicationDate).unix());
        }
        if (info.hasExpirationDate) {
            return (moment().unix() < moment(info.expirationDate).unix());
        }
        return $scope.isInfoPublished(info);
    }

    $scope.infoExists = function(info) {
        return (info._id !== undefined);
    }


    // Info Edition
    $scope.createInfo = function(info){
        if (info === undefined) {
            // Info creation
            $scope.currentInfo = new Info();
            $scope.currentInfo.status = ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
        }
        else {
            // Info edition
            $scope.currentInfo = info;
        }
        template.open('thread', 'info-edit-form');
    }

    $scope.saveInfo = function(info){
        if (info._id === undefined) {
            // Info creation
            var newInfo = new Info();
            newInfo.create($scope.currentThread, info);
        }
        else {
            // Info edition
            info.save();
        }

        $scope.reloadInfos();
        $scope.cancelEditInfo();
    }

    $scope.publishInfo = function(info){
        info.status = ACTUALITES_CONFIGURATION.infoStatus.PUBLISHED;
        info.save();
        $scope.reloadInfos();
    }

    $scope.unpublishInfo = function(info){
        info.status = ACTUALITES_CONFIGURATION.infoStatus.DRAFT;
        info.save();
        $scope.reloadInfos();
    }

    $scope.deleteInfo = function(info){
        info.remove($scope.currentThread);
        $scope.reloadInfos();
        $scope.cancelEditInfo();
    }

    $scope.cancelEditInfo = function(){
        $scope.currentInfo = {};
        template.open('thread', 'threads-edit-view');
    }

    $scope.reloadInfos = function(){
        var reloadFlag = true;
        $scope.currentThread.infos.on('sync', function(){
            if (reloadFlag === true) {
                $scope.infos = $scope.currentThread.infos;
                $scope.apply("infos");
                reloadFlag = false;
            }
        });
    }


    // Default display
    $scope.selectThread(model.latestThread);
}
