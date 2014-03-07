var currencyModule = angular.module("wizard.currency");

currencyModule.run(function($rootScope, wizardService) {
	function validator() {
		var orderInfo = $rootScope.orderInfo;
		return (orderInfo && orderInfo.inTransfer && orderInfo.inTransfer.currency
				&& orderInfo.outTransfer && orderInfo.outTransfer.currency) ? true : false;
	}
	wizardService.registerValidator("currency", validator);
});