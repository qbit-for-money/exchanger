var amountModule = angular.module("wizard.amount");

amountModule.run(function(orderService, wizardService, isOrderValid) {
	function validator() {
		return isOrderValid(orderService.get());
	}
	wizardService.registerValidator("amount", validator);
});