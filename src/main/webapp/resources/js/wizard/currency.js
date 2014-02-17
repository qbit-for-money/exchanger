var currencyModule = angular.module("currency");

currencyModule.controller("CurrencyController", function($scope, $rootScope, currencyResource) {
	$scope.convertion = {};
	$scope.convertion.ltr = true;
	$scope.convertion.panels = {
		left: {},
		right: {}
	};
	$rootScope.transferIn = {};
	$rootScope.transferOut = {};
	
	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
			if(currenciesResponse){
				$scope.currencies = currenciesResponse.currencies;
			}
		});
		
	var refreshTransfers = function() {
		if ($scope.convertion.ltr) {
			$rootScope.transferIn.currency = $scope.convertion.panels.left.currency;
			$rootScope.transferOut.currency = $scope.convertion.panels.right.currency;
		} else {
			$rootScope.transferIn.currency = $scope.convertion.panels.right.currency;
			$rootScope.transferOut.currency = $scope.convertion.panels.left.currency;
		}
	};
	$scope.selectCurrency = function(panel, item) {
		if (panel && item) {
			panel.currency = item;
			refreshTransfers();
		}
	};
	
	$scope.toggleDirection = function() {
		$scope.convertion.ltr = !$scope.convertion.ltr;
		refreshTransfers();
	};
});

