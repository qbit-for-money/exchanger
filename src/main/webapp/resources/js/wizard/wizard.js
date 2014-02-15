var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($scope, $rootScope) {
	$rootScope.orderInfo = {
		inTransfer: {},
		outTransfer: {}
	};
});

