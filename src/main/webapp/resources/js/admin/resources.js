var adminModule = angular.module("admin");

adminModule.factory("adminResource", function($resource) {
	return $resource(window.context + "webapi/admin", {}, {
		getBalance: {method: "GET", url: window.context + "webapi/admin/:currency/balance"},
		getTransactionHisory: {method: "GET", url: window.context + "webapi/admin/:currency/transactions"},
		getTransactionHistoryByAddress: {method: "GET", url: window.context + "webapi/admin/:currency/transactionsByAddress"},
		sendMoney: {method: "POST"}
	});
});