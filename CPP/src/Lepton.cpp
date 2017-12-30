/*
 * Lepton.cpp
 *
 *  Created on: Dec 29, 2017
 *      Author: parallels
 */

#include "LeptonCPP.hpp"

#include <sys/socket.h>
#include <netinet/in.h>
#include <iostream>

LeptonCPP::LeptonCPP(){
	s.connect(p.getInlet());

	s.start();
	p.start();
}

int main(int argv, char** argc){

	sockaddr_in sock;

	int sockfd;

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if(sockfd < 0){
		cout << "Error allocated socket" << endl;
		return 1;
	}

	bzero((char *) &sock, sizeof(sock));
	sock.sin_family = AF_INET;
	sock.sin_addr.in_addr = INADDR_LOOPBACK;
	sock.sin_port = htons(56000);

	int err = bind(sockfd, (struct sockaddr*)&sock, sizeof(sock)) < 0);
	if(err < 0){
		cout << "Error binding socket" <<endl;
		return 2;
	}

	listen(sockfd, 1000);

	while(true){
		//accept


	}
}


