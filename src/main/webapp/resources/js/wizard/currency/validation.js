var currencyModule = angular.module("wizard.currency");

currencyModule.run(function($rootScope, wizardService) {
	function validator() {
		var orderInfo = $rootScope.orderInfo;
		return (orderInfo && orderInfo.inTransfer && orderInfo.inTransfer.currency
				&& orderInfo.inTransfer.amount && orderInfo.inTransfer.amount.centsInCoin
				&& orderInfo.outTransfer && orderInfo.outTransfer.currency
				&& orderInfo.outTransfer.amount && orderInfo.outTransfer.amount.centsInCoin) ? true : false;
	}
	wizardService.registerValidator("currency", validator);
});