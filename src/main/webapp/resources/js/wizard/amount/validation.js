var amountModule = angular.module("wizard.amount");

amountModule.run(function($rootScope, wizardService, isOrderValid) {
	function validator() {
		return isOrderValid($rootScope.orderInfo);
	}
	wizardService.registerValidator("amount", validator);
});