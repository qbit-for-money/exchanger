var amountModule = angular.module("wizard.amount");

amountModule.controller("AmountController", function($rootScope, $scope, wizardService, isTransferValid, restoreOrderInfoFromSession) {
	function validator() {
		var orderInfo = $rootScope.orderInfo;
		if (orderInfo && isTransferValid(orderInfo.inTransfer) && isTransferValid(orderInfo.outTransfer)) {
			return true;
		} else {
			// Show error on page
			$scope.$apply(function() {});
			return false;
		}
	}
	wizardService.registerValidator("amount", validator);
				
	// if there was a redirect, load stored order
	restoreOrderInfoFromSession();	
});