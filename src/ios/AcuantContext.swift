//
//  AcuantContext.swift
//  AcuantTest
//
//  Created by Sree Prasantha Ram Mulpuri on 16/11/21.
//

import Foundation

public class AcuantContext  {
    
    private static var command:CDVInvokedUrlCommand?
    
    static func setCommand(_command:CDVInvokedUrlCommand){
        command = _command
    }
    
    static func getCommand()->CDVInvokedUrlCommand{
        return command!;
    }

}
