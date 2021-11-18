cordova.define("cordova-plugin-acuantsdk.Acuantsdk", function(require, exports, module) {
    var exec = require('cordova/exec');
    var _e = null;
    var createMask = function(){
        _e = document.createElement('DIV');
        _e.className = "acuant-cover";
        _e.style.position = "fixed";
        _e.style.top = "0";
        _e.style.width = "100vw";
        _e.style.height = "100vh";
        _e.style.zIndex = "999999";
        _e.style.backgroundColor = "#000";
        document.body.appendChild(_e);
    }
        
    var removeMask = function(){
        _e.remove();
    }
    
    exports.scanDocument = function (credentials, success, error) {
        createMask();
        function doSuccess(result){
            removeMask();
            success(result);
        }
        function doError(result){
            removeMask();
            error(result);
        }
        exec(doSuccess, doError, 'acuantsdk', "scanDocument", [credentials]);
    };
    
    exports.takeSelfie = function (credentials, success, error) {
        createMask();
        function doSuccess(result){
            removeMask();
            success(result);
        }
        function doError(result){
            removeMask();
            error(result);
        }
        exec(doSuccess, doError, 'acuantsdk', "takeSelfie", [credentials]);
    };
});
    