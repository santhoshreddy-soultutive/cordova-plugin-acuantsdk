var exec = require('cordova/exec');

exports.scanDocument = function (credentials, success, error) {
    exec(success, error, 'acuantsdk', "scanDocument", [credentials]);
};

exports.takeSelfie = function (credentials, success, error) {
    exec(success, error, 'acuantsdk', "takeSelfie", [credentials]);
};