var wizardModule = angular.module("wizard");

wizardModule.factory("wizardService", function() {
	var steps = [
		{title: "Currency", path: "/steps/currency"},
		{title: "Amount", path: "/steps/amount"},
		{title: "Result", path: "/steps/result"}
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