var currencyModule = angular.module("wizard.currency");

currencyModule.controller("CurrencyController", function($scope, currencyResource, orderService, exchangesResource) {
	$scope.currencyPair = {};
	$scope.ratesMap = {};

	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
		if (currenciesResponse && currenciesResponse.currencies) {
			var currencies = currenciesResponse.currencies;
			$scope.currenciesMap = {};
			for (var i = 0; i < currencies.length; i++) {
				$scope.currenciesMap[currencies[i].id] = currencies[i];
				for (var j = 0; j < currencies.length; j++) {
					if (i !== j) {
						rates(currencies[i], currencies[j]);
					}
				}
			}
			var orderInfo = orderService.get();
			$scope.currencyPair.inCurrency = $scope.currenciesMap[orderInfo.inTransfer.currency];
			$scope.currencyPair.outCurrency = $scope.currenciesMap[orderInfo.outTransfer.currency];
			refreshTransfers();
		}
	});

	function rates(inCurrency, outCurrency) {
		var rate = exchangesResource.rate({from: inCurrency.id, to: outCurrency.id});
		rate.$promise.then(function() {
			$scope.ratesMap[inCurrency.code + "_" + outCurrency.code] = rate;
		});
	}

	$scope.selectCurrency = function(inCurrencyId, outCurrencyId) {
		var currencyPair = $scope.currencyPair;
		currencyPair.inCurrency = $scope.currenciesMap[inCurrencyId];
		currencyPair.outCurrency = $scope.currenciesMap[outCurrencyId];
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

