var amountModule = angular.module("wizard.amount");

amountModule.run(function($rootScope, wizardService, isTransferValid) {
	function validator() {
		var orderInfo = $rootScope.orderInfo;
		return (orderInfo && isTransferValid(orderInfo.inTransfer) && isTransferValid(orderInfo.outTransfer)) ? true : false;
	}
	wizardService.registerValidator("amount", validator);
});