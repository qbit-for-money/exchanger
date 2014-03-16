var amountModule = angular.module("wizard.amount");

amountModule.controller("AmountController", function($rootScope, $scope,
		wizardService, orderService,
		moneyCustomModules, walletsResource,
		exchangesResource, isAmountPositive, convertAmount) {
	
	function createOrder() {
		return orderService.create();
	}
	wizardService.registerAction("amount", createOrder);
	
	$scope.custom = moneyCustomModules.has(orderService.get().inTransfer.currency);
	
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
	
	function updateOutAmount() {
		var orderInfo = orderService.get();
		var inTransfer = orderInfo.inTransfer;
		var outTransfer = orderInfo.outTransfer;
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