var currencyModule = angular.module("wizard.currency");

currencyModule.controller("CurrencyController", function($scope, $rootScope, currencyResource, resetOrderInfo) {
	resetOrderInfo();
	
	$scope.panels = {left: {}, right: {}};
	
	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
		if (currenciesResponse && currenciesResponse.currencies) {
			var currencies = currenciesResponse.currencies;
			$scope.currencies = currencies;
			for (var c = 0; c < currencies.length; c++) {
				var currency = currencies[c];
				if ($rootScope.orderInfo.inTransfer.currency
						&& (currency.id === $rootScope.orderInfo.inTransfer.currency)) {
					$scope.panels.left.currency = currency;
				}
				if ($rootScope.orderInfo.outTransfer.currency
						&& (currency.id === $rootScope.orderInfo.outTransfer.currency)) {
					$scope.panels.right.currency = currency;
				}
			}
			refreshTransfers();
		}
	});
	
	function createEmptyAmount(currency) {
		return { coins: 0, cents: 0, centsInCoin: currency.centsInCoin };
	}
	function refreshTransfers() {
		if ($scope.panels.left.currency) {
			$rootScope.orderInfo.inTransfer.currency = $scope.panels.left.currency.id;
			$rootScope.orderInfo.inTransfer.amount = createEmptyAmount($scope.panels.left.currency);
		}
		if ($scope.panels.right.currency) {
			$rootScope.orderInfo.outTransfer.currency = $scope.panels.right.currency.id;
			$rootScope.orderInfo.outTransfer.amount = createEmptyAmount($scope.panels.right.currency);
		}
	};
	
	function isCurrencySelectable(panelName, currency) {
		var oppositePanel = $scope.panels[panelName === "left" ? "right" : "left"];
		return (!oppositePanel.currency || (currency.id !== oppositePanel.currency.id));
	};
	$scope.selectCurrency = function(panel, currency) {
		if (isCurrencySelectable(panel, currency)) {
			$scope.panels[panel].currency = currency;
			refreshTransfers();
		}
	};
});

