var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($scope, $rootScope) {
	$rootScope.orderInfo = {
		inTransfer: {
			type: "IN"
		},
		outTransfer: {
			type: "OUT"
		}
	};
});

