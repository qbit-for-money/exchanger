var amountModule = angular.module("wizard.amount");

amountModule.controller("AmountController", function($rootScope, $scope, restoreOrderInfoFromSession,
		exchangesResource, isAmountPositive, convertAmount, invRate) {
	// if there was a redirect, load stored order
	restoreOrderInfoFromSession();
	
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