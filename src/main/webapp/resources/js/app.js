angular.module("common", ["ngResource"]);

angular.module("order", ["ngResource"]);
angular.module("main-menu", ["ngRoute", "angular-carousel", "ngResource", "common", "order"]);

angular.module("main", ["common", "ngRoute", "ui.bootstrap", "main-menu", "order"]);
