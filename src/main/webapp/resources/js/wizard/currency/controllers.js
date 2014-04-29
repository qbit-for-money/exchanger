var currencyModule = angular.module("wizard.currency");

currencyModule.controller("CurrencyController", function($scope, currencyResource, orderService, exchangesResource) {
	$scope.currencyPair = {};
	$scope.rates = {};

	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
		if (currenciesResponse && currenciesResponse.currencies) {
			var currencies = currenciesResponse.currencies;
			$scope.currencies = {};
			for (var i = 0; i < currencies.length; i++) {
				$scope.currencies[currencies[i].id] = currencies[i];
				for (var j = 0; j < currencies.length; j++) {
					if (i !== j) {
						rates(currencies[i], currencies[j]);
					}
				}
			}
			var orderInfo = orderService.get();
			$scope.currencyPair.inCurrency = $scope.currencies[orderInfo.inTransfer.currency];
			$scope.currencyPair.outCurrency = $scope.currencies[orderInfo.outTransfer.currency];
			refreshTransfers();
		}
	});

	function rates(inCurrency, outCurrency) {
		var rate = exchangesResource.rate({from: inCurrency.id, to: outCurrency.id});
		rate.$promise.then(function() {
			$scope.rates[inCurrency.code + "_" + outCurrency.code] = rate;
		});
	}

	$scope.selectCurrency = function(inCurrencyId, outCurrencyId) {
		var currencyPair = $scope.currencyPair;
		currencyPair.inCurrency = $scope.currencies[inCurrencyId];
		currencyPair.outCurrency = $scope.currencies[outCurrencyId];
		refreshTransfers();
		$scope.goToNextStep();
	}

	function refreshTransfers() {
		var orderInfo = orderService.get();
		if ($scope.currencyPair.inCurrency) {
			orderInfo.inTransfer.currency = $scope.currencyPair.inCurrency.id;
			orderInfo.inTransfer.amount = createEmptyAmount($scope.currencyPair.inCurrency);
		}
		if ($scope.currencyPair.outCurrency) {
			orderInfo.outTransfer.currency = $scope.currencyPair.outCurrency.id;
			orderInfo.outTransfer.amount = createEmptyAmount($scope.currencyPair.outCurrency);
		}
	}
	function createEmptyAmount(currency) {
		return {coins: 0, cents: 0, centsInCoin: currency.centsInCoin};
	}
});

