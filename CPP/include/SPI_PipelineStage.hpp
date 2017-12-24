#ifndef SPIPIPELINESTAGE_HPP
#define SPIPIPELINESTAGE_HPP

#include "daqstage.hpp"
#include "bcm2835.h"

#define WIDTH 80  //Number rows (u short)
#define HEIGTH 60 //Number columns (u short)
#define ROW 164   //Number of bytes in a row

using namespace OSIP;
using namespace std;

class SPIPipeline : public DAQStage<unsigned char>{

private:
	vector<char> m_FrameBuffer;

public:


	void workstage() {
		if(bcm2835_spi_begin() != 1){
			log("Error starting SPI");
			return;
		}

		m_FrameBuffer.resize(ROW*HEIGTH);

		bcm2835_spi_setChipSelectPolarity(BCM2835_SPI_CS0, 0);
		bcm2835_spi_setClockDivider(BCM2835_SPI_CLOCK_DIVIDER_16);
		bcm2835_spi_setDataMode(BCM2835_SPI_MODE3);
		bcm2835_spi_chipSelect(BCM2835_SPI_CS0);

		vector<char> rowBuffer(ROW);

		while(stopThread){
			//acquire data, check for bad byte
			bcm2835_spi_transfern(rowBuffer.data(), ROW); //Acquire a row

			unsigned short* p = (unsigned short*)rowBuffer.data();
			unsigned short frameCount = p[1];
			unsigned short packetCheck = p[0];

		}

		bcm2835_spi_end();
	}

};


#endif SPIPIPELINESTAGE_HPP

