var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($rootScope, $scope, $location, wizardService) {
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
	$rootScope.goToNextStep = function() {
		var currentStepIndex = wizardService.getStepIndexByPath($location.path());
		if (currentStepIndex + 1 < $scope.steps.length) {
			$location.path($scope.steps[currentStepIndex + 1].path);
		}
	};
});
