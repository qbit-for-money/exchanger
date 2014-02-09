var mainMenuModule = angular.module("main-menu");

mainMenuModule.controller("MainMenuController", function($scope, currencyResource) {
	$scope.convertion = {};
	$scope.convertion.l2r = true;
	$scope.convertion.panels = {
		left: {},
		right: {}
	};

	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
			if(currenciesResponse){
				$scope.currencies = currenciesResponse.currencies;
			}
		});
			
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

