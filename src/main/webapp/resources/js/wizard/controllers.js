var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($rootScope, $scope, $location, wizardService) {
	$scope.steps = wizardService.getSteps();

	function updateCurrentStepIndex() {
		var currentStepIndex = wizardService.getStepIndexByPath($location.path());
		$scope.currentStepIndex = (wizardService.isStepIndexValid(currentStepIndex) ? currentStepIndex : 0);
	}
	$scope.$on("$locationChangeSuccess", updateCurrentStepIndex);
	updateCurrentStepIndex();

	$scope.goToStep = function(stepIndex) {
		if (wizardService.canGoToStep($location.path(), stepIndex)) {
			$location.path(wizardService.getStepByIndex(stepIndex).path);
		}
	};
	$rootScope.goToNextStep = function() {
		if (wizardService.canGoToNextStep($location.path())) {
			$location.path(wizardService.getNextStep($location.path()).path);
		}
	};
	$scope.goToPrevStep = function() {
		if (wizardService.canGoToPrevStep($location.path())) {
			$location.path(wizardService.getPrevStep($location.path()).path);
		}
	};
});
