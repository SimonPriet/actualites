/* Root Abstract Controller */
function ActualitesAbstractController($scope, template, route){

    this.initialize = this.initialize || function(){
        // to be overriden
    }

    // Thread display
    $scope.hasCurrentThread = function(){
        return ($scope.currentThread instanceof Thread);
    }

    // Info display
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

    $scope.reloadInfos = function(){
        var reloadFlag = true;
        $scope.currentThread.infos.on('sync', function(){
            if (reloadFlag === true) {
                $scope.infos = $scope.currentThread.infos;
                $scope.$apply("infos");
                reloadFlag = false;
            }
        });
    }
}
