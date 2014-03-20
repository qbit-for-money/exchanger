var currencyModule = angular.module("wizard.currency");

currencyModule.run(function(orderService, wizardService, isAmountValid) {
	function validator() {
		var orderInfo = orderService.get();
		return (orderInfo && orderInfo.inTransfer && orderInfo.inTransfer.currency
				&& isAmountValid(orderInfo.inTransfer.amount)
				&& orderInfo.outTransfer && orderInfo.outTransfer.currency
				&& isAmountValid(orderInfo.outTransfer.amount));
	}
	wizardService.registerValidator("currency", validator);
});