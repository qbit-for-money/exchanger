var mainMenuModule = angular.module("main-menu");

mainMenuModule.controller("MainMenuController", function($scope, $rootScope, currencyResource) {
	var constants = {
		directions : {
			ltr : "ltr",
			rtl : "rtl"
		},
		panels : {
			left : "left",
			right : "right"
		}
	};	
	$scope.constants = constants;
	
	$scope.convertion = {};
	$scope.convertion.direction = constants.directions.ltr;
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
		if (!(panel in constants.panels) || !item) {
			return; 
		}
		var scopePanel = $scope.convertion.panels[panel];
		var anotherPanel = (panel === constants.panels.left) ? 
				$scope.convertion.panels.right : $scope.convertion.panels.left;
		if (anotherPanel && (!anotherPanel.currency || anotherPanel.currency.id !== item.id)) {
			scopePanel.currency = item;
		}
	};
	
	$scope.toggleDirection = function() {
		if ($scope.convertion.direction === constants.directions.ltr) {
			$scope.convertion.direction = constants.directions.rtl;
		} else {
			$scope.convertion.direction = constants.directions.ltr;
		}
	};

	$rootScope.orderInfo = {};
});

mainMenuModule.directive("currencyPicker", function() {
	return {
		templateUrl: "resources/html/currency-picker.html"
	};
});

