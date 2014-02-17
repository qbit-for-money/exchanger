var currencyModule = angular.module("currency");

currencyModule.controller("CurrencyController", function($scope, $rootScope, currencyResource) {
	$rootScope.convertion = $rootScope.convertion || {};
	$rootScope.convertion.ltr = (typeof $rootScope.convertion.ltr == "boolean") ? $rootScope.convertion.ltr : true;
	$scope.panels = {
		left: {
			currency: $rootScope.orderInfo[$rootScope.convertion.ltr ? "inTransfer" : "outTransfer"].currency
		},
		right: {
			currency: $rootScope.orderInfo[$rootScope.convertion.ltr ? "outTransfer" : "inTransfer"].currency
		}
	};
	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
			if(currenciesResponse){
				$scope.currencies = currenciesResponse.currencies;
			}
		});
		
	var refreshTransfers = function() {
		if ($scope.convertion.ltr) {
			$rootScope.orderInfo.inTransfer.currency = $scope.panels.left.currency;
			$rootScope.orderInfo.outTransfer.currency = $scope.panels.right.currency;
		} else {
			$rootScope.orderInfo.inTransfer.currency = $scope.panels.right.currency;
			$rootScope.orderInfo.outTransfer.currency = $scope.panels.left.currency;	
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

