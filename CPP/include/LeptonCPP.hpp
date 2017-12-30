#ifndef __LEPTONCPP_H__
#define __LEPTONCPP_H__

#include "constants.h"
#include "SPI_PipelineStage.hpp"
#include "Process.hpp"
#include <iostream>
#include "wiringPi.h"

using namespace OSIP;
using namespace std;


class LeptonCPP {

private:

	Process p;

	SPIPipeline s;

public:
	LeptonCPP();

};


#endif
