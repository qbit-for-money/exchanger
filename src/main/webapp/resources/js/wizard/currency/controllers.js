var currencyModule = angular.module("wizard.currency");

currencyModule.controller("CurrencyController", function($scope, currencyResource, orderService, exchangesResource, convertAmount) {
	$scope.panels = {left: {}, right: {}};
	$scope.rates = {};

	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
		if (currenciesResponse && currenciesResponse.currencies) {
			var currencies = currenciesResponse.currencies;
			$scope.currencies = currencies;
			var orderInfo = orderService.get();
			for (var c = 0; c < currencies.length; c++) {
				var currency = currencies[c];
				if (orderInfo.inTransfer.currency && (currency.id === orderInfo.inTransfer.currency)) {
					$scope.panels.left.currency = currency;
				}
				if (orderInfo.outTransfer.currency && (currency.id === orderInfo.outTransfer.currency)) {
					$scope.panels.right.currency = currency;
				}
			}
			rates();
			refreshTransfers();
		}
	});

	function rates() {
		var btcToLtcResponse = exchangesResource.rate({from: $scope.currencies[1].id, to: $scope.currencies[2].id});
		btcToLtcResponse.$promise.then(function() {
			var amount = {};
			amount.cents = 0;
			amount.coins = 1;
			amount.centsInCoin = $scope.currencies[1].centsInCoin;
			var resultAmount = convertAmount(amount, btcToLtcResponse);
			$scope.rates.btcToLtc = resultAmount.coins + resultAmount.cents / resultAmount.centsInCoin;
		});

		var ltcToBtcResponse = exchangesResource.rate({from: $scope.currencies[2].id, to: $scope.currencies[1].id});
		ltcToBtcResponse.$promise.then(function() {
			var amount = {};
			amount.cents = 0;
			amount.coins = 1;
			amount.centsInCoin = $scope.currencies[2].centsInCoin;
			var resultAmount = convertAmount(amount, ltcToBtcResponse);
			$scope.rates.ltcToBtc = resultAmount.coins + resultAmount.cents / resultAmount.centsInCoin;
		});
	}


	/*$scope.selectCurrency = function(panelName, currency) {
	 if (!currency.supported) {
	 return;
	 }
	 var panel = $scope.panels[panelName];
	 var oppositePanel = $scope.panels[panelName === "left" ? "right" : "left"];
	 if (oppositePanel.currency && (oppositePanel.currency.id === currency.id)) {
	 oppositePanel.currency = panel.currency;
	 }
	 panel.currency = currency;
	 refreshTransfers();
	 };*/

	$scope.selectCurrency = function(currencyIndex) {
		var panelLeft = $scope.panels["left"];
		var panelRight = $scope.panels["right"];

		panelLeft.currency = $scope.currencies[currencyIndex];

		if (currencyIndex === 1) {
			panelRight.currency = $scope.currencies[2];
		} else {
			panelRight.currency = $scope.currencies[1];
		}

		refreshTransfers();
		$scope.goToNextStep();
	};

	function refreshTransfers() {
		var orderInfo = orderService.get();
		if ($scope.panels.left.currency) {
			orderInfo.inTransfer.currency = $scope.panels.left.currency.id;
			orderInfo.inTransfer.amount = createEmptyAmount($scope.panels.left.currency);
		}
		if ($scope.panels.right.currency) {
			orderInfo.outTransfer.currency = $scope.panels.right.currency.id;
			orderInfo.outTransfer.amount = createEmptyAmount($scope.panels.right.currency);
		}
	}
	function createEmptyAmount(currency) {
		return {coins: 0, cents: 0, centsInCoin: currency.centsInCoin};
	}
});

