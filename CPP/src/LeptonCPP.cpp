//============================================================================
// Name        : LeptonCPP.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include "Process.hpp"
#include "SPI_PipelineStage.hpp"
#include "wiringPi.h"

using namespace OSIP;
using namespace std;

int main() {
	wiringPiSetup();

	SPIPipeline s;
	Process p;

	s.connect(p.getInlet());

	s.start();
	p.start();

	while(true){
		std::this_thread::sleep_for(std::chrono::milliseconds(50));
	}
}
