var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($scope, wizardService, $location) {
	$scope.steps = wizardService.getSteps();
	$scope.getStepIndexByPath = wizardService.getStepIndexByPath;
	$scope.goToPreviousStep = function() {
		var currentStepIndex = wizardService.getStepIndexByPath($location.path());
		if (currentStepIndex > 0) {
			$location.path($scope.steps[currentStepIndex - 1].path);
		}
	};
	$scope.goToNextStep = function() {
		var currentStepIndex = wizardService.getStepIndexByPath($location.path());
		if (currentStepIndex < $scope.steps.length - 1) {
			$location.path($scope.steps[currentStepIndex + 1].path);
		}
	};
});

wizardModule.factory("wizardService", function() {
	var steps = [
		{ title: "Choose Currency", path: "/steps/currency"}, 
		{ title: "Enter Amount", path: "/steps/amount" },
		{ title : "View Result", path: "/steps/result" }		
	];
	var getStepIndexByPath = function(path) {
		for (var i = 0; i < steps.length; i++) {
			if (steps[i].path === path) return i;
		}
	};
	return {
		getSteps: function(){return steps;},
		getStepIndexByPath: getStepIndexByPath
	};
});