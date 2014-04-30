var wizardModule = angular.module("wizard");

wizardModule.controller("WizardController", function($rootScope, $scope, $location, wizardService, orderService) {
	angular.element("#wizard-container").removeClass("hide-all");
	$rootScope.steps = wizardService.getSteps();

	function redirectToResultIfActiveOrderExists() {
		var currentStep = wizardService.getStepByPath($location.path());
		if (currentStep && (currentStep.id === "result")) {
			return;
		}
		var orderInfo = orderService.get();
		if (orderInfo && orderInfo.status) {
			var resultStep = wizardService.getStepById("result");
			$location.path(resultStep.path);
		}
	}
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
	$rootScope.isResultGoBackEnabled = function() {
		var orderInfo = orderService.get();
		if (!orderInfo.status || (orderInfo.status === "SUCCESS" || orderInfo.status === "CANCELED")) {
			return true;
		} else {
			return false;
		}
	};

	$rootScope.$on("$locationChangeSuccess", function() {
		resetValidationFail();
		resetActionFail();
		updateCurrentStepIndex();
		redirectToResultIfActiveOrderExists();
		alert($scope);
		
	});
	$scope.$on("order-loaded", redirectToResultIfActiveOrderExists);
	updateCurrentStepIndex();
	redirectToResultIfActiveOrderExists();

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
					if (currentStep.actionFailMessage) {
						$scope.actionMessage = currentStep.actionFailMessage.apply(this, arguments);
					} else {
						$scope.actionMessage = "Action fails.";
					}
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

	$rootScope.goToStep = function(id) {
		if ($rootScope.isResultGoBackEnabled()) {
			if (wizardService.canGoToStep(id)) {
				if (wizardService.getStepIndexByPath($location.path()) <= id) {
					return;
				}
				$location.path(wizardService.getStepByIndex(id).path);
			}
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