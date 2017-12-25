#ifndef SPIPIPELINESTAGE_HPP
#define SPIPIPELINESTAGE_HPP

#include "daqstage.hpp"
#include "wiringPiSPI.h"

#define WIDTH 80  //Number rows (u short)
#define HEIGTH 60 //Number columns (u short)
#define ROW 164   //Number of bytes in a row

using namespace OSIP;
using namespace std;

class SPIPipeline : public DAQStage<unsigned char>{

private:
	vector<char> m_FrameBuffer;

	int m_SPI;
public:


	void workStage() {
		m_SPI = wiringPiSPISetupMode(0, 16e6, 3);

		m_FrameBuffer.resize(ROW*HEIGTH);

		vector<unsigned char> rowBuffer(ROW);

		while(stopThread == false){
			//acquire data, check for bad byte
			//bcm2835_spi_transfern(rowBuffer.data(), ROW); //Acquire a row

			int retVal = wiringPiSPIDataRW(0, rowBuffer.data(), rowBuffer.size());

			unsigned short* p = (unsigned short*)rowBuffer.data();
			unsigned short frameCount = p[1];
			unsigned short packetCheck = p[0];

			for(int i = 0; i < rowBuffer.size(); i++){
				cout << rowBuffer[i] << " ";
			}

			cout << endl;
		}
	}

};


#endif SPIPIPELINESTAGE_HPP

