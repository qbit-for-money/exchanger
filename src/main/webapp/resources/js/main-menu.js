var mainMenuModule = angular.module('main-menu');

mainMenuModule.controller('MainMenuController', function($scope, currencyService) {
	$scope.convertion = {};
	$scope.convertion.l2r = true;
	$scope.convertion.panels = {
		left: {},
		right: {}
	};

	$scope.currencies = currencyService.findAll();
	
	$scope.selectCurrency = function(panel, item) {
		if (!panel || !item) { return; }
		
		var scopePanel = $scope.convertion.panels[panel];
		if (scopePanel) { scopePanel.currency = item; }
	};
});

mainMenuModule.directive("currencyPicker", function() {
	return {
		templateUrl: "resources/html/currency-picker.html"
	};
});

