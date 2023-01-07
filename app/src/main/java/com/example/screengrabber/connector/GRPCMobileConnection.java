package com.example.screengrabber.connector;
import android.support.v4.os.IResultReceiver;

import com.mobilecontrol.DeviceDispatchingGrpc;
import com.mobilecontrol.DeviceInfo;
import com.mobilecontrol.ServerResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GRPCMobileConnection extends DeviceDispatchingGrpc.DeviceDispatchingImplBase
{
    private DeviceDispatchingGrpc.DeviceDispatchingBlockingStub _stub;

    public GRPCMobileConnection(){
        CreateStub();
    }

    public void CreateStub(){
        try{
            ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("188.232.100.63",5001)
                .usePlaintext()
                .build();
            _stub = DeviceDispatchingGrpc.newBlockingStub(managedChannel);
        }
        catch(Exception ex)
        { }
    }

    public String SendInfo(){
        ServerResponse response = null;

        try{
            DeviceInfo deviceInfo = DeviceInfo.newBuilder()
                    .setIdDevice("test_id")
                    .setWidthPixels(111)
                    .setHeightPixels(111)
                    .setDpiPixels(111)
                    .setScreenBase64("test_ScreenBase64")
                    .setFormatScreen("test_FormatScreen")
                    .build();

            response =  _stub.gotScreen(deviceInfo);
        }
        catch(Exception ex)
        {

        }

        return "OK";
    }
}
