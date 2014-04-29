var wizardModule = angular.module("wizard");

wizardModule.factory("wizardService", function() {
	var steps = [{id: "currency", path: "/steps/currency"},
		{id: "amount", path: "/steps/amount"},
		{id: "result", path: "/steps/result"}];

	function getSteps() {
		return steps;
	}

	function getStepByIndex(stepIndex) {
		if (isStepIndexValid(stepIndex)) {
			return steps[stepIndex];
		}
	}
	function getStepById(stepId) {
		var stepIndex = getStepIndexById(stepId);
		if (stepIndex !== -1) {
			return steps[stepIndex];
		}
	}
	function getStepIndexById(stepId) {
		for (var stepIndex = 0; stepIndex < steps.length; stepIndex++) {
			if (steps[stepIndex].id === stepId) {
				return stepIndex;
			}
		}
		return -1;
	}
	function getStepByPath(path) {
		var stepIndex = getStepIndexByPath(path);
		if (stepIndex !== -1) {
			return steps[stepIndex];
		}
	}
	function getStepIndexByPath(path) {
		for (var stepIndex = 0; stepIndex < steps.length; stepIndex++) {
			if (steps[stepIndex].path === path) {
				return stepIndex;
			}
		}
		return -1;
	}

	function canGoToStep(stepIndex) {
		if (!isStepIndexValid(stepIndex)) {
			return false;
		}
		if (stepIndex === 0) {
			return true;
		} 
		var prevStep = steps[stepIndex - 1];
		return (!prevStep.validator || prevStep.validator());
	}

	function canGoToNextStep(currentPath) {
		var currentStepIndex = getStepIndexByPath(currentPath);
		if (!isStepIndexValid(currentStepIndex) || !isStepIndexValid(currentStepIndex + 1)) {
			return false;
		}
		var currentStep = steps[currentStepIndex];
		if (currentStep.validator) {
			return currentStep.validator();
		} else {
			return true;
		}
	}
	function getNextStep(currentPath) {
		var currentStepIndex = getStepIndexByPath(currentPath);
		return steps[currentStepIndex + 1];
	}

	function canGoToPrevStep(currentPath) {
		var currentStepIndex = getStepIndexByPath(currentPath);
		return (isStepIndexValid(currentStepIndex) && isStepIndexValid(currentStepIndex - 1));
	}
	function getPrevStep(currentPath) {
		var currentStepIndex = getStepIndexByPath(currentPath);
		return steps[currentStepIndex - 1];
	}

	function isStepIndexValid(stepIndex) {
		return (angular.isNumber(stepIndex) && (stepIndex >= 0) && (stepIndex < steps.length));
	}

	function registerValidator(stepId, validator) {
		var step = getStepById(stepId);
		step.validator = validator;
	}

	function registerAction(stepId, action, actionFailMessage) {
		var step = getStepById(stepId);
		step.action = action;
		step.actionFailMessage = actionFailMessage;
	}

	return {
		getSteps: getSteps,
		getStepByIndex: getStepByIndex,
		getStepById: getStepById,
		getStepIndexById: getStepIndexById,
		getStepByPath: getStepByPath,
		getStepIndexByPath: getStepIndexByPath,
		canGoToStep: canGoToStep,
		canGoToNextStep: canGoToNextStep,
		getNextStep: getNextStep,
		canGoToPrevStep: canGoToPrevStep,
		getPrevStep: getPrevStep,
		isStepIndexValid: isStepIndexValid,
		registerValidator: registerValidator,
		registerAction: registerAction
	};
});