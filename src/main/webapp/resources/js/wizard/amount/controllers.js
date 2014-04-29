var amountModule = angular.module("wizard.amount");

amountModule.controller("AmountController", function($rootScope, $scope, delayedProxy,
	wizardService, orderService, currencyResource,
	moneyCustomModules, walletsResource, $interval,
	exchangesResource, isAmountValid, convertAmount) {
		
	function createOrder() {
		var newOrderInfo = orderService.create();
		return newOrderInfo.$promise;
	}
	wizardService.registerAction("amount", createOrder, function(ex) {
		if (ex && ex.data && (ex.data.indexOf("OrderTestException") > 0)) {
			return "Not enough money in the system buffer";
		} else {
			return "Can't create order.";
		}
	});

	$scope.custom = moneyCustomModules.has(orderService.get().inTransfer.currency);

	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
		if (currenciesResponse && currenciesResponse.currencies) {
			var currencies = currenciesResponse.currencies;
			$scope.currencies = [];
			for (var i = 0; i < currencies.length; i++) {
				$scope.currencies[currencies[i].id] = currencies[i].code;
			}
			updateBalance();
		}
	});

	function generateInAddress() {
		if (!$scope.custom) {
			var orderInfo = orderService.get();
			var inTransfer = orderInfo.inTransfer;
			var walletAddress = walletsResource.generateAddress({currency: inTransfer.currency});
			walletAddress.$promise.then(function() {
				inTransfer.address = walletAddress.address;
				$scope.inAddressPlaceholder = "Wallet address";
			}, function() {
				inTransfer.address = null;
				$scope.inAddressPlaceholder = "Error generating address...";
			});
		}
	}
	$scope.generateInAddress = generateInAddress;
	generateInAddress();
	updateRate();

	function updateRate() {
		var orderInfo = orderService.get();
		var inTransfer = orderInfo.inTransfer;
		var outTransfer = orderInfo.outTransfer;
		var rate = exchangesResource.rate({
			from: inTransfer.currency,
			to: outTransfer.currency
		});

		rate.$promise.then(function() {
			$scope.rate = rate;
		});
	}

	function updateBalance() {
		var orderInfo = orderService.get();
		var outTransfer = orderInfo.outTransfer;
		$scope.outCurrencyCode = $scope.currencies[outTransfer.currency];
		var balance = walletsResource.getBalance({currency: outTransfer.currency});
		balance.$promise.then(function() {
			$scope.balance = balance;
		});
	}
	
	$scope.updateBalanceTimerId = $interval(updateBalance, 5000);

	var outAmountUpdateInProgress = false;
	function updateOutAmount() {
		if (inAmountUpdateInProgress) {
			inAmountUpdateInProgress = false;
			return;
		}
		outAmountUpdateInProgress = true;
		var orderInfo = orderService.get();
		var inTransfer = orderInfo.inTransfer;
		var outTransfer = orderInfo.outTransfer;
		if (isAmountValid(inTransfer.amount)) {
			var rate = exchangesResource.rate({
				from: inTransfer.currency,
				to: outTransfer.currency
			});
			rate.$promise.then(function() {
				outTransfer.amount = convertAmount(inTransfer.amount, rate);
			});
		} else {
			outTransfer.amount.coins = 0;
			outTransfer.amount.cents = 0;
		}
		updateRate();
		updateBalance();
	}
	var destroyInAmountWatch = $rootScope.$watch("orderInfo.inTransfer.amount",
		delayedProxy(updateOutAmount, 500), true);
	var inAmountUpdateInProgress = false;
	function updateInAmount() {
		if (outAmountUpdateInProgress) {
			outAmountUpdateInProgress = false;
			return;
		}
		inAmountUpdateInProgress = true;
		var orderInfo = orderService.get();
		var outTransfer = orderInfo.outTransfer;
		var inTransfer = orderInfo.inTransfer;
		if (isAmountValid(outTransfer.amount)) {
			var rate = exchangesResource.rate({
				from: outTransfer.currency,
				to: inTransfer.currency
			});
			rate.$promise.then(function() {
				inTransfer.amount = convertAmount(outTransfer.amount, rate);
			});
		} else {
			inTransfer.amount.coins = 0;
			inTransfer.amount.cents = 0;
		}
		updateRate();
		updateBalance();
	}
	var destroyOutAmountWatch = $rootScope.$watch("orderInfo.outTransfer.amount",
		delayedProxy(updateInAmount, 500), true);

	$scope.$on("$destroy", function() {
		destroyInAmountWatch();
		destroyOutAmountWatch();
		if ($scope.updateBalanceTimerId) {
			$interval.cancel($scope.updateBalanceTimerId);
		}
	});
});