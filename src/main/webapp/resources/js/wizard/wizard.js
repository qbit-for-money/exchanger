var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($scope, wizardService, $location) {
	$scope.steps = wizardService.getSteps();
	$scope.getStepIndexByPath = wizardService.getStepIndexByPath;
	$scope.$on('$locationChangeSuccess', function() {
		$scope.currentStepIndex = wizardService.getStepIndexByPath($location.path());
	});
	$scope.setCurrentStep = function(stepIndex) {
		if (!angular.isNumber(stepIndex)) {
			return;
		}
		if (wizardService.getStepIndexByPath($location.path()) >= stepIndex) {
			$location.path($scope.steps[stepIndex].path);
		}
	};
	$scope.goToPreviousStep = function() {
		var currentStepIndex = wizardService.getStepIndexByPath($location.path());
		if (currentStepIndex > 0) {
			$location.path($scope.steps[currentStepIndex - 1].path);
		}
	};
	$scope.goToNextStep = function() {
		var currentStepIndex = wizardService.getStepIndexByPath($location.path());
		if (currentStepIndex + 1 < $scope.steps.length) {
			$location.path($scope.steps[currentStepIndex + 1].path);
		}
	};
});

wizardModule.factory("wizardService", function() {
	var steps = [
		{title: "Choose Currency", path: "/steps/currency"},
		{title: "Enter Amount", path: "/steps/amount"},
		{title: "View Result", path: "/steps/result"}
	];
	var getSteps = function() {
		return steps;
	};
	var getStepIndexByPath = function(path) {
		for (var i = 0; i < steps.length; i++) {
			if (steps[i].path === path) {
				return i;
			}
		}
	};
	return {
		getSteps: getSteps,
		getStepIndexByPath: getStepIndexByPath
	};
});
