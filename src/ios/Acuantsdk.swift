import Foundation
import UIKit
import AcuantImagePreparation
import AcuantCamera
import AcuantCommon
import AcuantHGLiveness
import AVFoundation

@objc(Acuantsdk)
public class Acuantsdk : CDVPlugin {
    
    private var capturedImage: UIImage?
     
    private var command:CDVInvokedUrlCommand?
    
    var progressView : AcuantProgressView!

    
    private var isScanDocument: Bool = false

    
    private func resetData(){
        capturedImage = nil
    }
    
    @objc func scanDocument(_ command: CDVInvokedUrlCommand) {
        self.command = command;
        AcuantContext.setCommand(_command: command)
        self.isScanDocument = true
        self.process()
    }

    @objc func takeSelfie(_ command: CDVInvokedUrlCommand) {
        self.command = command;
        self.isScanDocument = false
        self.process()
    }
}
//ImagePreparation - START =============
extension Acuantsdk{
    
    func displayError(message:String){
        DispatchQueue.main.async{
            let alert = UIAlertController(title: "Error", message: message, preferredStyle: UIAlertController.Style.alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertAction.Style.default, handler: nil))
            self.viewController?.present(alert, animated: true, completion: nil)
        }
    }
    
    func display(title:String, message:String,action:UIAlertAction){
        DispatchQueue.main.async{
            let alert = UIAlertController(title: title, message: message, preferredStyle: UIAlertController.Style.alert)
            alert.addAction(action)
            self.viewController?.present(alert, animated: true, completion: nil)
        }
    }
    
    func start() {
        let args : NSDictionary = self.command?.argument(at: 0) as! NSDictionary
        let username = args.value(forKey: "id_username") as! String
        let password = args.value(forKey: "id_password") as! String
        let acasEndpoint = args.value(forKey: "acas_endpoint") as! String
        let idEndpoint = args.value(forKey: "acas_endpoint") as! String
        
        Credential.setUsername(username: username)
        Credential.setPassword(password: password)
        //Credential.setSubscription(subscription: "xxxxxx")
        let endpoints = Endpoints()
        //endpoints.frmEndpoint = "https://frm.acuant.net"
        //endpoints.healthInsuranceEndpoint = "https://medicscan.acuant.net"
        endpoints.idEndpoint = idEndpoint //"https://services.assureid.net"
        endpoints.acasEndpoint = acasEndpoint //"https://eu.acas.acuant.net"
        Credential.setEndpoints(endpoints: endpoints)
    }
    
    private func initialize(){
        let initalizer: IAcuantInitializer = AcuantInitializer()
        let packages : Array<IAcuantPackage> = [ImagePreparationPackage()]
        
        _ = initalizer.initialize(packages:packages){ [weak self]
            error in
            
            DispatchQueue.main.async {
                if let self = self{
                    if(error == nil){
                        self.hideProgressView()
                        self.resetData()
                        if(self.isScanDocument){
                            self.showDocumentCaptureCamera()
                        }else{
                            self.showKeylessHGLiveness()
                        }
                    }else{
                        self.hideProgressView()
                        if let msg = error?.errorDescription {
                            self.displayError(message: "\(error!.errorCode) : " + msg)
                        }
                    }
                    
                }
            }
        }
    }
    
    func process(){
        //self.progressView = AcuantProgressView(frame: (self.controller?.view.frame)!, center: (self.controller?.view.center)!)
        self.showProgressView(text:  "Initializing...")
        self.start()
        self.getToken()
    }

    func getToken(){
        let task = AcuantTokenService().getTask(){
            token in

            DispatchQueue.main.async {
                if let success = token{
                    if Credential.setToken(token: success){
                            self.initialize()
                    }
                    else{
                        //INVALID TOKEN DISPLAY ERROR
                        let pluginResult:CDVPluginResult = CDVPluginResult.init(status: CDVCommandStatus_ERROR)
                        self.commandDelegate.send(pluginResult, callbackId: self.command?.callbackId)
                    }
                }
                else{
                    //NO TOKEN AVAILABLE DISPLAY ERROR
                    let pluginResult:CDVPluginResult = CDVPluginResult.init(status: CDVCommandStatus_ERROR)
                    self.commandDelegate.send(pluginResult, callbackId: self.command?.callbackId)
                }
            }
        }
        
        task?.resume()
    }
    
    
    
 
    private func showProgressView(text:String = ""){
        /*
        DispatchQueue.main.async {
            self.progressView.messageView.text = text
            self.progressView.startAnimation()
            self.controller?.view.addSubview(self.progressView)
        }
         */
    }
    
    private func hideProgressView(){
        /*
        DispatchQueue.main.async {
            self.progressView.stopAnimation()
            self.progressView.removeFromSuperview()
        }
         */
    }
    
    
    private func cropImage(image:Image, callback: @escaping (AcuantImage?) -> ()){
        if image.image != nil {
            self.showProgressView(text: "Processing...")
            DispatchQueue.global().async {
                ImagePreparation.evaluateImage(data: CroppingData.newInstance(image: image)) {image,_ in
                    DispatchQueue.main.async {
                        self.hideProgressView()
                        callback(image)
                    }
                }
            }
        }
    }
    
    private func convertImageToBase64String (img: UIImage) -> String {
        return img.jpegData(compressionQuality: 1)?.base64EncodedString() ?? ""
    }
}
//ImagePreparation - END =============

//MARK: - AcuantCamera: HGLivenessDelegate
extension Acuantsdk:HGLivenessDelegate{
    public func liveFaceCaptured(image: UIImage?) {
        self.viewController.dismiss(animated: false)
        self.capturedImage = image
        let resultImage = self.convertImageToBase64String(img: self.capturedImage!)
        print(resultImage)
        let pluginResult:CDVPluginResult = CDVPluginResult.init(status: CDVCommandStatus_OK, messageAs: resultImage)
        self.commandDelegate.send(pluginResult, callbackId: self.command?.callbackId)
    }
    
    public func showKeylessHGLiveness(){
        DispatchQueue.main.async{
            let liveFaceViewController = FaceLivenessCameraController()
            liveFaceViewController.delegate = self
            //Optionally override to change refresh rate
            //liveFaceViewController.frameRefreshSpeed = 10
            //self.controller?.navigationController?.pushViewController(liveFaceViewController, animated: false)
            self.viewController?.present(liveFaceViewController, animated: false, completion: nil)
        }
    }
}
//MARK: - AcuantCamera: CameraCaptureDelegate

extension Acuantsdk: CameraCaptureDelegate {

    public func setCapturedImage(image: Image, barcodeString: String?) {
        self.viewController.dismiss(animated: false)
        cropImage(image: image) { croppedImage in
            if(croppedImage == nil){
                //SEND ERROR
                let pluginResult:CDVPluginResult = CDVPluginResult.init(status: CDVCommandStatus_ERROR)
                self.commandDelegate.send(pluginResult, callbackId: self.command?.callbackId)
            } else{
                self.capturedImage = croppedImage!.image
                let resultImage = self.convertImageToBase64String(img: self.capturedImage!)
                print(resultImage)
                let pluginResult:CDVPluginResult = CDVPluginResult.init(status: CDVCommandStatus_OK, messageAs: resultImage)
                self.commandDelegate.send(pluginResult, callbackId: self.command?.callbackId)
            }
        }
    }
    
    func showDocumentCaptureCamera(){
        //handler in .requestAccess is needed to process user's answer to our request
        AVCaptureDevice.requestAccess(for: .video) { [weak self] success in
            if success { // if request is granted (success is true)
                DispatchQueue.main.async {
                    let options = CameraOptions(digitsToShow: 2, autoCapture:true, hideNavigationBar: true)
                    let documentCameraController = DocumentCameraController.getCameraController(delegate:self!, cameraOptions: options)
                    self!.viewController?.present(documentCameraController, animated: false)
                }
            } else { // if request is denied (success is false)
                DispatchQueue.main.async{
                    // Create Alert
                    let alert = UIAlertController(title: "Camera", message: "Camera access is absolutely necessary to use this app", preferredStyle: .alert)
                    // Add "OK" Button to alert, pressing it will bring you to the settings app
                    alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { action in
                        UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                    }))
                    // Show the alert with animation
                    self!.viewController?.present(alert, animated: true)
                }
            }
        }
    }
    
   
   
}
