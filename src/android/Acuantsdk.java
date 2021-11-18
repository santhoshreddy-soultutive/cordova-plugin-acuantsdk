package com.soultutive.cordova.plugins.acuantsdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.acuant.acuantcamera.camera.AcuantCameraActivity;
import com.acuant.acuantcamera.camera.AcuantCameraOptions;
import com.acuant.acuantcamera.constant.Constants;
import com.acuant.acuantcamera.initializer.MrzCameraInitializer;
import com.acuant.acuantcommon.initializer.AcuantInitializer;
import com.acuant.acuantcommon.initializer.IAcuantPackageCallback;
import com.acuant.acuantcommon.model.Credential;
import com.acuant.acuantcommon.model.Error;
import com.acuant.acuantechipreader.initializer.EchipInitializer;
import com.acuant.acuantfacecapture.FaceCaptureActivity;
import com.acuant.acuantimagepreparation.AcuantImagePreparation;
import com.acuant.acuantimagepreparation.background.EvaluateImageListener;
import com.acuant.acuantimagepreparation.initializer.ImageProcessorInitializer;
import com.acuant.acuantimagepreparation.model.AcuantImage;
import com.acuant.acuantimagepreparation.model.CroppingData;
import com.acuant.acuantipliveness.AcuantIPLiveness;
import com.acuant.acuantipliveness.facialcapture.model.FacialSetupResult;
import com.acuant.acuantipliveness.facialcapture.service.FacialSetupLisenter;
import com.acuant.acuantpassiveliveness.AcuantPassiveLiveness;
import com.acuant.acuantpassiveliveness.model.PassiveLivenessData;
import com.acuant.acuantpassiveliveness.model.PassiveLivenessResult;
import com.acuant.acuantpassiveliveness.service.PassiveLivenessListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import java.lang.Thread;

import  com.soultutive.cordova.plugins.acuantsdk.LocalConstants;

/**
 * This class echoes a string called from JavaScript.
 */
public class Acuantsdk extends CordovaPlugin {

    private CordovaInterface cordovaInterface;
    public CallbackContext callbackContext;

    private static final String SCAN_DOCUMENT = "scanDocument";
    private static final String TAKE_SELFIE = "takeSelfie";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cordovaInterface = cordova;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                initializeAcuantSdk(callbackContext, action, args);
            }
        });
        return true;
    }

    private void initializeAcuantSdk(CallbackContext callbackContext, String action, JSONArray args) {
        try {
            Object object = args.get(0);
            JSONObject credentials = (JSONObject) object;

            IAcuantPackageCallback acuantPackageCallback = new IAcuantPackageCallback() {
                @Override
                public void onInitializeSuccess() {
                    if(SCAN_DOCUMENT.equals(action)) {
                        showDocumentCaptureCamera();
                    }else if(TAKE_SELFIE.equals(action)){
                        showFrontCamera();
                    }
                }
    
                @Override
                public void onInitializeFailed(List<? extends Error> list) {
                    callbackContext.error("Exception-9999");
                }
            };

            try{
                Credential.init(credentials.getString("id_username"),
                        credentials.getString("id_password"),
                        "",
                        "",
                        "",
                        "",
                        credentials.getString("liveness_endpoint"),
                        credentials.getString("acas_endpoint"),
                        "");
                
                AcuantInitializer.initialize("",
                        cordovaInterface.getActivity(),
                        Arrays.asList(new ImageProcessorInitializer()),
                        acuantPackageCallback);

            } catch(Exception e) {
//            Log.e("Acuant Error", e.toString());
//            showAcuDialog(e.toString())
                e.printStackTrace();
            }
            
        }catch(Exception e){
            e.printStackTrace();
            callbackContext.error("Exception-9999");
        }
    }

    private void showDocumentCaptureCamera(){
        try{
            // OPEN ACUANT CAMERA
            Intent cameraIntent = new Intent(cordovaInterface.getActivity(), AcuantCameraActivity.class);
            cameraIntent.putExtra(Constants.ACUANT_EXTRA_CAMERA_OPTIONS,
                    new AcuantCameraOptions.DocumentCameraOptionsBuilder().build()
            );

            this.cordova.startActivityForResult((CordovaPlugin) this, cameraIntent, LocalConstants.REQUEST_CAMERA_PHOTO);

            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            this.callbackContext.sendPluginResult(r);

        }catch(Exception e){
            this.callbackContext.error("Exception-9001");
            PluginResult r = new PluginResult(PluginResult.Status.ERROR);
            this.callbackContext.sendPluginResult(r);
        }
    }

    private void showFrontCamera(){
        try{
            // OPEN ACUANT CAMERA
            Intent cameraIntent = new Intent(cordovaInterface.getActivity(), FaceCaptureActivity.class);
//            cameraIntent.putExtra(Constants.ACUANT_EXTRA_CAMERA_OPTIONS,
//                    new AcuantCameraOptions.DocumentCameraOptionsBuilder().build()
//            );

            this.cordova.startActivityForResult((CordovaPlugin) this, cameraIntent, LocalConstants.REQUEST_CAMERA_FACE_CAPTURE);

            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            this.callbackContext.sendPluginResult(r);

        }catch(Exception e){
            this.callbackContext.error("Exception-9001");
            PluginResult r = new PluginResult(PluginResult.Status.ERROR);
            this.callbackContext.sendPluginResult(r);
        }
    }

    private void showIPLiveness() {
        AcuantIPLiveness.getFacialSetup(new FacialSetupLisenter() {

            @Override
            public void onDataReceived(FacialSetupResult facialSetupResult) {

            }

            @Override
            public void onError(int i, String s) {

            }

        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CallbackContext thisContext = this.callbackContext;

        if (requestCode == LocalConstants.REQUEST_CAMERA_PHOTO && resultCode == AcuantCameraActivity.RESULT_SUCCESS_CODE) {
             String url = data.getStringExtra(Constants.ACUANT_EXTRA_IMAGE_URL);
            if (url != null) {
                AcuantImagePreparation.INSTANCE.evaluateImage(this.cordovaInterface.getContext(), new CroppingData(url), new EvaluateImageListener(){

                    @Override
                    public void onSuccess(AcuantImage acuantImage) {
                        System.out.println("SUCCESS");
                        String capturedImage = getBase64String(acuantImage.getImage());
                        thisContext.success(capturedImage);
                    }

                    @Override
                    public void onError(Error error) {
                        System.out.println("FAILURE");
                        thisContext.error("Exception-9002");
                    }
                });
            }
        }else if(requestCode == LocalConstants.REQUEST_CAMERA_FACE_CAPTURE && resultCode == FaceCaptureActivity.RESPONSE_SUCCESS_CODE) {
            String url = data.getStringExtra(FaceCaptureActivity.OUTPUT_URL);
            if (url != null) {
                byte bytes[] = readFromFile(url);
                Bitmap capturedSelfieImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                String capturedImage = getBase64String(capturedSelfieImage);
                thisContext.success(capturedImage);

                /*
                JSONObject result = new JSONObject();
                try {
                    result.put("RESP_TYPE", "IMAGE");
                    result.put("IMAGE", capturedImage);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("CAPTURE SUCCESS!!!");
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                pluginResult.setKeepCallback(true); // keep callback
                callbackContext.sendPluginResult(pluginResult);

                PassiveLivenessData plData = new PassiveLivenessData((Bitmap) capturedSelfieImage);
                AcuantPassiveLiveness.processFaceLiveness(plData, new PassiveLivenessListener(){

                    @Override
                    public void passiveLivenessFinished(PassiveLivenessResult result) {
                        String facialLivelinessResultString = "";
                        switch (result.getLivenessAssessment()){
                            case Live:
                                facialLivelinessResultString = "Facial Liveliness: live";
                                break;
                            case NotLive:
                                facialLivelinessResultString = "Facial Liveliness: Not Live";
                                break;
                            case PoorQuality:
                                facialLivelinessResultString = "Facial Liveliness: Poor Quality";
                                break;
                            default:
                                facialLivelinessResultString = "Facial Liveliness: Check Failed";
                                break;
                        }

                        JSONObject matchResult = new JSONObject();
                        try {
                            matchResult.put("RESP_TYPE", "MATCH_RESULT");
                            matchResult.put("MATCH_RESULT", facialLivelinessResultString);
                        }catch(Exception e){

                        }
                        System.out.println("LIVELINESS MATCH DONE !!!");
                        thisContext.success(matchResult);
                    }
                });*/
            }else{
                // CAPTUREED IMAGE NULL
                System.out.println("FAILURE");
                thisContext.error("Exception-9003");
            }
        }
    }

    private byte[] readFromFile(String fileUrl) {
        byte bytes[] = new byte[0];
        try {
            File photoFile = new File(fileUrl);
            bytes = FileUtils.readFileToByteArray(photoFile);
            photoFile.delete();
        }catch(Exception e){
            e.printStackTrace();
        }
        return bytes;
    }

    private String getBase64String(Bitmap bitmap1) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}