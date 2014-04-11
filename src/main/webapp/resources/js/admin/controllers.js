var adminModule = angular.module("admin");

adminModule.controller("AdminController", function($rootScope, $scope, adminResource, currencyResource, walletsResource, isAmountPositive, stringToAmount, amountToNumber) {
	var currenciesResponse = currencyResource.findAll();
	currenciesResponse.$promise.then(function() {
		if (currenciesResponse && currenciesResponse.currencies) {
			var currencies = currenciesResponse.currencies;
			$scope.currencies = currencies;
			$scope.currencies.splice(0, 1);
			$scope.currency = currencies[0];
			refreshBalance();
			resetAmountToSend();
			//refreshTransactions();
			generateInAddress();
		}
	});

	function generateInAddress() {
		var walletAddress = walletsResource.generateAddress({currency: $scope.currency.id});
		walletAddress.$promise.then(function() {
			$scope.inAddress = walletAddress.address;
		}, function() {
			$scope.inAddress = null;
		});
	}

	function refreshTransactions() {
		var transactionsResponse = adminResource.getWalletTransactions({currency: $scope.currency.id});
		$scope.transactions = [];
		transactionsResponse.$promise.then(function() {
			if (transactionsResponse && transactionsResponse.transactions) {
				var transacts = transactionsResponse.transactions;
				for (var i = 0; i < transacts.length; i++) {

					transacts[i].amount = amountToNumber(transacts[i].amount);
					transacts[i].amountSentToMe = amountToNumber(transacts[i].amountSentToMe);
					transacts[i].amountSentFromMe = amountToNumber(transacts[i].amountSentFromMe);
					if (transacts[i].amountSentToMe < transacts[i].amountSentFromMe) {
						transacts[i].amount = transacts[i].amount * -1;
					}
					$scope.transactions.push(transacts[i]);
				}
			}
		});
	}


	function refreshBalance() {
		var walletBalance = adminResource.getBalance({currency: $scope.currency.id});
		walletBalance.$promise.then(function() {
			if (walletBalance) {
				$scope.balance = walletBalance.coins + walletBalance.cents / walletBalance.centsInCoin;
			}
		});
	}
	function resetAmountToSend() {
		var amount = {coins: 0, cents: 0, centsInCoin: $scope.currency.centsInCoin};
		$scope.amountToSend = amountToNumber(amount);
	}

	$scope.sendMoney = function() {
		if (!$scope.amountToSend || !$scope.currency || !$scope.outAddress) {
			return;
		}		
		var amount = stringToAmount($scope.amountToSend, $scope.currency.centsInCoin);
		if (isAmountPositive(amount)) {
			var amountTransfer = {};
			amountTransfer.amount = amount;
			amountTransfer.currency = $scope.currency.id;
			amountTransfer.address = $scope.outAddress;
			adminResource.sendMoney({}, amountTransfer);
		}
	};

	$scope.selectCurrency = function(currency) {
		if (currency) {
			$scope.currency = currency;
			refreshBalance();
			resetAmountToSend();
			refreshTransactions();
			generateInAddress();
		}
	};
});
