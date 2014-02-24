var currencyModule = angular.module("wizard.currency");

currencyModule.controller("CurrencyController", function($scope, $rootScope, currencyResource, resetOrderInfo) {
	resetOrderInfo();
	
	$rootScope.convertion = $rootScope.convertion || {};
	$rootScope.convertion.ltr = (typeof $rootScope.convertion.ltr == "boolean") ? $rootScope.convertion.ltr : true;
	$scope.panels = {
		left: {currency: $rootScope.orderInfo[$rootScope.convertion.ltr ? "inTransfer" : "outTransfer"].currency},
		right: {currency: $rootScope.orderInfo[$rootScope.convertion.ltr ? "outTransfer" : "inTransfer"].currency}
	};
	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
		if (currenciesResponse) {
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
	
	var isCurrencySelectable = function(panelName, currency) {
		var result = false;
		if (panelName && currency) {
			var panel = $scope.panels[panelName];
			var oppositePanel = $scope.panels[panelName == "left" ? "right" : "left"];
			if (panel && oppositePanel) {
				result = true;
				if (oppositePanel.currency){
					result = oppositePanel.currency !== currency;
				}
			} 
		}
		return result;
	};

	$scope.selectCurrency = function(panel, item) {
		if (isCurrencySelectable(panel, item)) {
			$scope.panels[panel].currency = item;
			refreshTransfers();
		}
	};

	$scope.toggleDirection = function() {
		$scope.convertion.ltr = !$scope.convertion.ltr;
		refreshTransfers();
	};
});

