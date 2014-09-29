module.directive('datePickerActu', function($compile){
	return {
		scope: {
			ngModel: '=',
			ngChange: '&',
			minDate: '=',
			past: '=',
			expObject: '=',
			exp: '='
		},
		transclude: true,
		replace: true,
		restrict: 'E',
		template: '<input ng-transclude type="text" data-date-format="dd/mm/yyyy"  />',
		link: function($scope, $element, $attributes){
			$scope.$watch('ngModel', function(newVal){
				if($scope.ngModel === null){
					$scope.ngModel = moment();
				}
				$element.val(moment($scope.ngModel).format('DD/MM/YYYY'));
				if($scope.past !== undefined && $scope.past === true){
					if($scope.minDate === undefined){
						$scope.minDate = moment();
					}
					if(moment($scope.minDate).unix() > moment($scope.ngModel).unix()){
						$element.val(moment($scope.minDate).format('DD/MM/YYYY'));
						$scope.ngModel = $scope.minDate;
					}
				}
				if($scope.exp !== undefined && $scope.exp === true){
					if(moment($scope.expObject).unix() < moment($scope.ngModel).unix()){
						$scope.expObject = $scope.ngModel;
					}
				}
			});
			loader.asyncLoad('/' + infraPrefix + '/public/js/bootstrap-datepicker.js', function(){
				$element.datepicker({
						dates: {
							months: moment.months(),
							monthsShort: moment.monthsShort(),
							days: moment.weekdays(),
							daysShort: moment.weekdaysShort(),
							daysMin: moment.weekdaysMin()
						}
					})
					.on('changeDate', function(){
						setTimeout(function(){
							var date = $element.val().split('/');
							var temp = date[0];
							date[0] = date[1];
							date[1] = temp;
							date = date.join('/');
							$scope.ngModel = new Date(date);
							$scope.$apply('ngModel');
							$scope.$parent.$eval($scope.ngChange);
							$scope.$parent.$apply();
						}, 10);

						$(this).datepicker('hide');
					});
				$element.datepicker('hide');
			});

			$element.on('focus', function(){
				var that = this;
				$(this).parents('form').on('submit', function(){
					$(that).datepicker('hide');
				});
				$element.datepicker('show');
			});

			$element.on('change', function(){
				var date = $element.val().split('/');
				var temp = date[0];
				date[0] = date[1];
				date[1] = temp;
				date = date.join('/');
				$scope.ngModel = new Date(date);
				$scope.$apply('ngModel');
				$scope.$parent.$eval($scope.ngChange);
				$scope.$parent.$apply();
			});
		}
	}
});