var mainMenuModule = angular.module('main-menu');

mainMenuModule.controller('MainMenuContoller', function($scope) {
	var items = [];
        for (var i = 0; i < 20; i++) {
		items.push("TEST" + i);
	}
	$scope.items = items;
	$scope.headers = {
		left: "From",
		right: "To"
	};
});

mainMenuModule.directive("qbCurrencyPicker", function() {
	return {
		templateUrl: "resources/html/currency-picker.html"
	};
});