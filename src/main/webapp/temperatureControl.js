/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var tempValue = [0, 0];
var tempRange = [
        document.getElementById('lowerTemp'),
        document.getElementById('upperTemp')
    ];
var tempLabel = ['Lower: ', 'Upper: '];

var slider = document.getElementById('tempSlider');

noUiSlider.create(slider, {
        start: [20, 30],
        connect: true,
        range: {
                'min': 10,
                'max': 200
        }
});

slider.noUiSlider.on('update', function(values, handle){
    tempRange[handle].innerHTML = tempLabel[handle] + values[handle];
    tempValue[handle] = values[handle];
});
