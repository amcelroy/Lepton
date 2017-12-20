/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var leptonWidth = 80;
var leptonHeight = 60;
var g_mouseX = 0;
var g_mouseY = 0;

var rLUT = [ 0, 0, 0, 0, 1, 1, 1, 1, 1, 4, 7, 10, 13, 16, 20, 23, 27, 30, 33, 36, 39, 42, 45, 48, 52, 55, 58, 61, 65, 68, 71, 74, 78, 81, 84, 87, 90, 93, 96, 99, 103, 106, 110, 113, 117, 120, 123, 126, 130, 133, 136, 139, 142, 145, 148, 151, 155, 158, 161, 164, 168, 171, 174, 177, 181, 184, 187, 190, 193, 196, 200, 203, 207, 210, 213, 216, 220, 223, 226, 229, 233, 236, 239, 242, 245, 247, 250, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255 ];
var gLUT = [ 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 6, 9, 12, 16, 19, 22, 25, 29, 32, 35, 38, 42, 45, 48, 51, 55, 58, 61, 64, 68, 71, 74, 77, 81, 84, 87, 90, 94, 97, 100, 103, 106, 109, 112, 115, 119, 122, 126, 129, 133, 136, 139, 142, 145, 148, 151, 154, 158, 161, 164, 167, 171, 174, 177, 180, 184, 187, 190, 193, 197, 200, 203, 206, 209, 212, 216, 219, 223, 226, 229, 232, 236, 239, 242, 245, 249, 250, 252, 253, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255 ];
var bLUT = [ 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 4, 6, 9, 12, 15, 19, 22, 25, 28, 32, 35, 38, 41, 45, 48, 51, 54, 58, 61, 64, 67, 71, 74, 77, 80, 84, 87, 90, 93, 97, 100, 103, 106, 110, 113, 116, 119, 122, 125, 128, 131, 135, 138, 142, 145, 149, 152, 155, 158, 161, 164, 167, 170, 174, 177, 180, 183, 187, 190, 193, 196, 200, 203, 206, 209, 213, 216, 219, 222, 225, 228, 232, 235, 239, 242, 245, 248, 252, 252, 253, 254, 255, 255, 255, 255, 255, 255, 255, 255 ];


var wsUri = "ws://" + document.location.host + document.location.pathname;
var websocket = new WebSocket(wsUri);
websocket.binaryType = "arraybuffer";

websocket.onerror = function(evt) { onError(evt); };

window.onload = function() {
    var canvas = document.getElementById("leptonCanvas");
    canvas.addEventListener('mousemove', updateXYTemp);
    
    //Test
    drawROIs();
}

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

//var output = document.getElementById("output");
websocket.onopen = function(evt) { onOpen(evt); listROIFiles(); };
websocket.onmessage = function(evt) {onMessage(evt); };

function writeToScreen(message) {
    //output.innerHTML += message + "<br>";
}

function onOpen(evt) {
    websocket.send("Requesting connection...");
}

function clickUploadButton(){
    websocket.send("upload-config:" + document.getElementById("configTextArea").value);
}

function clickSaveROIButton(){
    var date = Date();
    date = date.replace(new RegExp(' ', 'g'), "_");
    date = date.replace(new RegExp(':', 'g'), ".");
    var roiFilename = prompt("Please name of ROI log:", date + ".txt");
    websocket.send("logroi:" + roiFilename);
}

function clickStopSavingButton(){
    websocket.send("stopSaving");
}

function clickFFCButton(){
    websocket.send("ffc"); 
}

function updateXYTemp(evt){
    var canvas = document.getElementById("leptonCanvas");
    var Xtxt = document.getElementById("X");
    var Ytxt = document.getElementById("Y");
    
    var scaleX = leptonWidth / canvas.clientWidth;
    var scaleY = leptonHeight / canvas.clientHeight;
    
    Xtxt.innerHTML = "X: " + (evt.layerX*scaleX).toFixed(0);
    Ytxt.innerHTML = "Y: " + (evt.layerY*scaleY).toFixed(0);
    
    g_mouseX = evt.layerX*scaleX;
    g_mouseY = evt.layerY*scaleY;
    
    g_mouseX = parseInt(g_mouseX);
    g_mouseY = parseInt(g_mouseY);
}

function clickGetLogButton(){
    
}

function toggleRelays(state){
    if(state == 1){
        websocket.send("enablerelays");
    }else{
        websocket.send("disablerelays");
    }
}

function listROIFiles(){
    websocket.send("listroilogs");
}

function viewROIFile(filename){
    sessionStorage.setItem('roiFile', "/ROI/" + filename);    
    window.location = "roi.html";
}

function onMessage(evt){
    if(typeof evt.data === "string"){
        var parsedJSON = JSON.parse(evt.data);
        switch(parsedJSON.type){
            case "settings":
                leptonWidth = parsedJSON.width;
                leptonHeight = parsedJSON.height;
                break;
            case "config":
                document.getElementById("configTextArea").value = parsedJSON.config;
                
                //Add rectangles to canvas
                drawROIs(parsedJSON.config);
                
                break;
                
            case "roifiles":
                var select = document.getElementById("roiFileBox"); 
                while (select.firstChild) {
                    select.removeChild(select.firstChild);
                }
                var tabs = parsedJSON.files.split('\t');
                
                var el = document.createElement("option");
                el.textContent = "Select File";
                el.value = "Select File";
                select.appendChild(el);
                
                for(var i = 0; i < tabs.length; i++) {
                    var opt = tabs[i];
                    el = document.createElement("option");
                    el.textContent = opt;
                    el.value = opt;
                    select.appendChild(el);
                }
                break;
                
            case "status":
                document.getElementById("FPATemp").innerHTML = "FPA Temp: " + parsedJSON.FPATemp + " K";
                document.getElementById("AUXTemp").innerHTML = "AUX Temp: " + parsedJSON.AUXTemp + " K";
                document.getElementById("Uptime").innerHTML = "Uptime: " + parsedJSON.Uptime + " H";
                break;
                
//            case "message":
//                var mesgBox = document.getElementById("MessageBox");
//                mesgBox.value = parsedJSON.message + "\r\n" + mesgBox.value;
//                break;
                    
                    
        }
    }else{
        var TempTxt = document.getElementById("Temp");
        var lepCan = document.getElementById("leptonCanvas");
        var lepCtx = document.getElementById("leptonCanvas").getContext('2d');
        
        lepCan.width = lepCan.offsetWidth;
        lepCan.height = lepCan.offsetHeight;
    
    
        var buff = new Uint8Array(evt.data); 
        var dv = new DataView(buff.buffer);
        var low = tempValue[0];
        var high = tempValue[1];
        var rgba = new Uint8ClampedArray(leptonWidth*leptonHeight*4);
        for(y = 0; y < leptonHeight; y++){
            for(x = 0; x < leptonWidth; x++){                
                var lindex = y*leptonWidth + x;
                var fl = dv.getFloat32(lindex*4);
                var f = Math.round(255.0 * (fl - low) / (high - low));
                rgba[y*(leptonWidth*4) + 4*x] = rLUT[f];
                rgba[y*(leptonWidth*4) + 4*x + 1] = gLUT[f];
                rgba[y*(leptonWidth*4) + 4*x + 2] = bLUT[f];
                rgba[y*(leptonWidth*4) + 4*x + 3] = 255;
                
                if(y == g_mouseY && x == g_mouseX){
                    TempTxt.innerHTML = "Temp: " + fl.toFixed(2);
                }
            }
        }
        
        //Package data for painting to canvas
        var imgData = new ImageData(rgba, leptonWidth, leptonHeight);
        
        //render to a hidden canvas, needed to stretch image
        var hiddenCanvas = document.createElement('canvas');
        hiddenCanvas.width = leptonWidth;
        hiddenCanvas.height = leptonHeight;
        var hiddenCtx = hiddenCanvas.getContext('2d');
        hiddenCtx.putImageData(imgData, 0, 0);
            
        //Paint to visible canvas, stretching as needed
        lepCtx.drawImage(hiddenCanvas, 0, 0, leptonWidth, leptonHeight, 
                                    0, 0, lepCan.width, lepCan.height);
                                    
        drawROIs(document.getElementById("configTextArea").value);
    }
}

