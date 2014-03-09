var amountModule = angular.module("wizard.amount");

amountModule.controller("AmountController", function($rootScope, $scope,
		restoreOrderInfoFromSession, moneyCustomModules, walletsResource,
		exchangesResource, isAmountPositive, convertAmount) {
	// if there was a redirect, load stored order
	restoreOrderInfoFromSession();
	
	var inTransfer = $rootScope.orderInfo.inTransfer;
	$scope.custom = moneyCustomModules.has(inTransfer.currency);
	
	function generateInAddress() {
		if (!$scope.custom) {
			var inTransfer = $rootScope.orderInfo.inTransfer;
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
	
	function updateOutAmount() {
		var inTransfer = $rootScope.orderInfo.inTransfer;
		var outTransfer = $rootScope.orderInfo.outTransfer;
		if (isAmountPositive(inTransfer.amount)) {
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
	}
	var destroyInAmountWatch = $rootScope.$watch("orderInfo.inTransfer.amount", updateOutAmount, true);
	
	$scope.$on("$destroy", function() {
		destroyInAmountWatch();
	});
});