/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */ 

function doneLoading(){
    var x = 0;
}

function openLog(uri){
    var colors = new Array();
    colors.push("#008000");
    colors.push("#808080");
    colors.push("#FF0000");
    colors.push("#FFFF00");
    colors.push("#800000");
    colors.push("#1E90FF");
    colors.push("#FA8072");
    colors.push("#ADD8E6");
    colors.push("#FFB6C1");
    colors.push("#EE82EE");
    colors.push("#FFA500");
    colors.push("#008B8B");
    
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open("GET", uri, true);
    xmlhttp.onload = function(e) {        
        if (xmlhttp.readyState === 4) {
            if (xmlhttp.status === 200) {
                var f = xmlhttp.responseText;
                var lines = f.split("\n");
                var roiList = new Array();
                var time = new Array();
                for(var i = 0; i < lines.length; i+=10){
                    var data = lines[i].split('\t');
                  
                    if(i !== 0){
                        time.push(data[0]);
                    }
                  
                    for(var j = 2; j < data.length - 1; j++){
                        //0: Time
                        //1: Relays on
                        //2 - N: ROIs
                    
                        if(i !== 0){
                            //Data
                            var datum = data[j].split(',');

                            if(datum.length === 2){                        
                                roiList[j - 2].temp.push(parseFloat(datum[0]));
                                roiList[j - 2].std.push(parseFloat(datum[1]));
                            }
                        }else{
                            //First Line
                            var roi = { name: data[j], temp: new Array(), std: new Array() };
                            roiList.push(roi);
                        }                      
                    }
                }
                
                var checkTime = time.length;
                var checkROI = roiList[0].temp.length;
                for(var i = 0; i < roiList.length; i++){
                    checkROI = Math.min(checkROI, roiList[i].temp.length);
                }
                
                if(checkROI < checkTime){
                    for(var i = 0; i < checkTime - checkROI; i++){
                        time.pop();
                    }
                }
              
              //Data loaded, ready to plot
              //Step 1: Dynamically add plots
                var b = document.getElementById("body");
                var canvas = "<canvas id=\"canvas\"></canvas>";
                b.innerHTML += canvas;
                
                var config = {
                    type: 'line',
                    backgroundColor: "rgba(1, 0, 0, 0.1)",
                    borderColor: "rgba(1, 0, 0, 0.0)",
                    data: {
                        labels: time,
                        datasets: [{
                            label: roiList[0].name,
                            data: roiList[0].temp,  
                            fill: false,
                            pointRadius: 1,
                            backgroundColor: colors[0],
                        }]
                    },
                    options: {
                        //showLines: true,
                        elements:{
                            point: {
                                radius: 0,
                                hitRadius: 10                                
                            }
                        },                        
                        tooltips: {
                            mode: 'index',
                            intersect: false,
                        },
                        hover: {
                            mode: 'nearest',
                            intersect: true
                        },
                        scales: {
                            xAxes: [{
                                display: true,
                                ticks: {
                                    beginAtZero: true,
                                },
                                scaleLabel: {
                                    display: true,
                                    labelString: 'Time'
                                }
                            }],
                            yAxes: [{
                                display: true,
                                ticks: {
                                    beginAtZero: false,
                                    //suggestedMax: 25,
                                },
                                scaleLabel: {
                                    display: true,
                                    labelString: 'Temperature (Celcius)'
                                }
                            }]
                        }
                    }
                }
                
                var ctx = document.getElementById("canvas").getContext("2d");
                window.tempChart = new Chart(ctx, config);
              
                for(var i = 1; i < roiList.length; i++){
                    var selectedColor;
                    
                    if(i >= colors.length - 1){
                        selectedColor = colors[colors.length - 1 - i];
                    }else{
                        selectedColor = colors[i];
                    }
                    
                    
                    var dataset = {
                        label: roiList[i].name,
                        data: roiList[i].temp,
                        fill: false,
                        pointRadius: 1,
                        backgroundColor: selectedColor,
                    };
                    window.tempChart.data.datasets.push(dataset);
                }                
                
                window.tempChart.update();
              
            } else {
              console.error(xmlhttp.statusText);
            }
        } 
    }
    xmlhttp.send();
}   