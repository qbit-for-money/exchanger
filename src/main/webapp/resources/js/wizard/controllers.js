var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($rootScope, $scope, $location, wizardService,
		restoreOrderInfoFromSession) {
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
	$rootScope.$on("$locationChangeSuccess", function() {
		restoreOrderInfoFromSession();
		updateCurrentStepIndex();
	});
	updateCurrentStepIndex();

	$rootScope.goToNextStep = function() {
		if (wizardService.canGoToNextStep($location.path())) {
			var nextStep = wizardService.getNextStep($location.path());
			if (nextStep.action) {
				var promise = nextStep.action();
				promise.then(function() {
					$location.path(nextStep.path);
				});
			} else {
				$location.path(nextStep.path);
			}
		} else {
			$scope.validationFails = true;
		}
	};
	$rootScope.goToPrevStep = function() {
		if (wizardService.canGoToPrevStep($location.path())) {
			$location.path(wizardService.getPrevStep($location.path()).path);
		}
	};
	
	$scope.resetValidation = function() {
		$scope.validationFails = false;
	};
});