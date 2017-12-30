#ifndef SPIPIPELINESTAGE_HPP
#define SPIPIPELINESTAGE_HPP

#include "daqstage.hpp"
#include "wiringPiSPI.h"
#include <memory>
#include "constants.h"

using namespace OSIP;
using namespace std;

class SPIPipeline : public DAQStage<unsigned char>{

private:
	vector<char> m_FrameBuffer;

	int m_SPI;

public:

	void work() {
		m_SPI = wiringPiSPISetupMode(0, 16e6, 3);

		m_FrameBuffer.resize(ROW*HEIGHT);

		vector<unsigned long long> dim;
		dim.push_back(WIDTH);
		dim.push_back(HEIGHT);

		while(stopThread == false){
			//acquire data, check for bad byte
			//bcm2835_spi_transfern(rowBuffer.data(), ROW); //Acquire a row
			vector<unsigned char> rowBuffer(ROW);

			int retVal = wiringPiSPIDataRW(0, rowBuffer.data(), rowBuffer.size());

			unsigned char frameCount = rowBuffer.data()[1];

			if(frameCount < HEIGHT){
				Payload<unsigned char> payload(dim, make_shared<vector<unsigned char>>(rowBuffer), "Frame");
				sendPayload(payload);
			}
		}
	}

};


#endif SPIPIPELINESTAGE_HPP

