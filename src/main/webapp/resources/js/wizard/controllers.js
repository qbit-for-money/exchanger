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
		resetValidationFail();
		resetActionFail();
		restoreOrderInfoFromSession();
		updateCurrentStepIndex();
	});
	updateCurrentStepIndex();

	$rootScope.goToNextStep = function() {
		resetValidationFail();
		if (wizardService.canGoToNextStep($location.path())) {
			var currentStep = wizardService.getStepByPath($location.path());
			var nextStep = wizardService.getNextStep($location.path());
			if (currentStep.action) {
				var promise = currentStep.action();
				promise.then(function() {
					resetActionFail();
					$location.path(nextStep.path);
				}, function() {
					$scope.actionFails = true;
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
	
	function resetValidationFail() {
		$scope.validationFails = false;
	}
	$scope.resetValidationFail = resetValidationFail;
	function resetActionFail() {
		$scope.actionFails = false;
	}
	$scope.resetActionFail = resetActionFail;
});