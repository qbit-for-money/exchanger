var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($rootScope, $location, wizardService) {
	$rootScope.steps = wizardService.getSteps();
	
	function updateCurrentStepIndex() {
		var currentStepIndex = wizardService.getStepIndexByPath($location.path());
		if (wizardService.isStepIndexValid(currentStepIndex)) {
			if (wizardService.canGoToStep(currentStepIndex)) {
				$rootScope.currentStepIndex = currentStepIndex;
			} else {
				$rootScope.currentStepIndex = -1;
			}
		} else {
			$rootScope.currentStepIndex = 0;
		}
	}
	$rootScope.$on("$locationChangeSuccess", updateCurrentStepIndex);
	updateCurrentStepIndex();
	
	$rootScope.goToStep = function(stepIndex) {
		if (wizardService.canGoToStep(stepIndex)) {
			$location.path(wizardService.getStepByIndex(stepIndex).path);
		}
	};
	$rootScope.goToNextStep = function() {
		if (wizardService.canGoToNextStep($location.path())) {
			$location.path(wizardService.getNextStep($location.path()).path);
		}
	};
	$rootScope.goToPrevStep = function() {
		if (wizardService.canGoToPrevStep($location.path())) {
			$location.path(wizardService.getPrevStep($location.path()).path);
		}
	};
});
