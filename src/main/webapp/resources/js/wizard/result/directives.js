var resultModule = angular.module("wizard.result");

resultModule.directive('selectOnMouseEnter', function($rootScope) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			element.on('mouseenter', function() {
				this.select();
				$rootScope.resultGoBackDisabled = false;
			});
		}
	};
});


