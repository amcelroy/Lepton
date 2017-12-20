/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var testROI = "<doc>";
testROI = testROI + "<ROI id=\"0\" TempBounds=\"20,185\">5,5,25,25</ROI>";
testROI = testROI + "\r\n" + "<ROI id=\"1\" TempBounds=\"20,185\">30,5,50,25</ROI>";
testROI = testROI + "</doc>";

var debug = 0;

function drawROIs(config){
    var canvas = document.getElementById("leptonCanvas");    
    var ctx = document.getElementById("leptonCanvas").getContext('2d');
    var scaleX = leptonWidth / canvas.width;
    var scaleY = leptonHeight / canvas.height;    
    
    //remove old rects
    //ctx.remove
    
    var parser = new DOMParser();
    
    if(debug == 0){
        var xmlDoc = parser.parseFromString(config, "text/xml");
    }else{
        var xmlDoc = parser.parseFromString(testROI, "text/xml");
    }
    
    var ROIList = xmlDoc.getElementsByTagName("ROI");
    ctx.font = "18px Arial";
    ctx.strokeStyle = "blue";
    ctx.lineWidth = 2;
    ctx.fillStyle = "blue";
    
    for(i = 0; i < ROIList.length; i++){
        var coords = ROIList[i].innerHTML.split(',');
        var x1 = coords[0] / scaleX;
        var y1 = coords[1] / scaleY;
        var x2 = coords[2] / scaleX;
        var y2 = coords[3] / scaleY;
        var width = (coords[2] - coords[0]) / scaleX;
        var height =(coords[3] - coords[1]) / scaleY;
        ctx.rect(x1, y1, width, height);
        ctx.stroke();
        ctx.fillText(ROIList[i].getAttribute("id"), x1 + width/2, y1 + height/2);
    }
    
    //ctx.stroke();
}


