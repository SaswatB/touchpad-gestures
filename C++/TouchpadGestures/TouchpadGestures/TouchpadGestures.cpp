/*
 * Copyright 2013 Saswat Bhattacharya
 * This file is part of Touchpad Gestures.
 * 
 * Touchpad Gestures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Touchpad Gestures is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Touchpad Gestures.  If not, see <http://www.gnu.org/licenses/>.
 */

// TouchpadGestures.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <fstream>
#include <sstream>
#include <stdio.h>
#include <stdlib.h>
#include <SynKit.h>
#include <vector>
#include <wchar.h>
#include <winuser.h>
#include <iostream>

void send_keys(std::vector<int> keys);

int _tmain(int argc, char* argv[]) {
	CoInitialize(NULL);

	std::cout << "Started\n" << std::flush;

	bool debug = false;
	if(argc == 2 && argv[1] == "-d") {
		debug = true;
	}

	std::vector<int> fing3tap;
	std::vector<int> fing3up;
	std::vector<int> fing3down;
	std::vector<int> fing3left;
	std::vector<int> fing3right;
	std::vector<int> fing4tap;
	std::vector<int> fing4up;
	std::vector<int> fing4down;
	std::vector<int> fing4left;
	std::vector<int> fing4right;

	int sensitivity = 10;

	bool keysEmpty = true;//Check to make sure some hotkeys are enabled

	//char* f = "C:\\Users\\Saswat\\Dropbox\\Documents\\Visual Studio 2012\\Projects\\TouchpadGestures\\test.txt";
	/*if(argc > 0 && argv[0] != "") {
		if(debug) {
			printf("Config File: ");
			printf(argv[0]);
			printf("\n");
		}
		f = argv[0];
	}*/

	char* f = "config";

	std::ifstream file(f); 

	if(debug)printf("Configuration File Status: %d\n",file.good()); //== 1 if file is found
	
	char str[100]; 
	while (file.getline(str, 100)) {
		if(debug) {
			printf(str);
			printf("\n");
		}
        // Process str
		std::string s(str);
		if(str[0] == '\n' || str[1] == '\n') {//blank line
			continue;
		}
		if(str[1] == '/') {//comment
			continue;
		}
		if(str[0] == 's' && str[1] == ':') {
			sensitivity = std::stoi(s.substr(3, s.length()));
		}

		if(str[0] == 'f' && str[1] == ':') {
			s = s.substr(3, s.length());

			std::vector<int> numbers;

			std::stringstream ss(s);
			char item[10];
			while (ss.getline(item, 100, ',')) {
				numbers.push_back(std::stoi(item));//item);
			}
			//delete item;

			if(numbers.size() > 2) {
				int fingers = numbers[0];//std::stoi(numbers[0]);
				int gesture = numbers[1];//std::stoi(numbers[1]);
				std::vector<int>* storage = nullptr;
				bool prob = false;
				if(fingers == 3) {
					if(gesture == 0) {//3 finger tap
						storage = &fing3tap;
					} else if(gesture == 1) {//3 finger swipe up
						storage = &fing3up;
					} else if(gesture == 2) {//3 finger swipe down
						storage = &fing3down;
					} else if(gesture == 3) {//3 finger swipe left
						storage = &fing3left;
					} else if(gesture == 4) {//3 finger swipe right
						storage = &fing3right;
					} else {//problem
						prob = true;
					}
				} else if(fingers == 4) {
					if(gesture == 0) {//4 finger tap
						storage = &fing4tap;
					} else if(gesture == 1) {//4 finger swipe up
						storage = &fing4up;
					} else if(gesture == 2) {//4 finger swipe down
						storage = &fing4down;
					} else if(gesture == 3) {//4 finger swipe left
						storage = &fing4left;
					} else if(gesture == 4) {//4 finger swipe right
						storage = &fing4right;
					} else {//problem
						prob = true;
					}
				} else {//problem
					prob = true;
				}

				if(!prob && storage != nullptr && storage->size() == 0) {
					for(unsigned int index = 2; index < numbers.size(); index++) {
						storage->push_back(numbers[index]);//std::stoi(numbers[index]));
						keysEmpty = false;
					}
					storage->shrink_to_fit();
				}

			}

		}
    }

	//delete file;
	//delete str;
	//delete f;
	//delete argv;

	if(debug) {
		printf("sensitivity: %d\n", sensitivity);
		std::vector<int> keys[] = {fing3tap, fing3up, fing3down, fing3left, fing3right, fing4tap, fing4up, fing4down, fing4left, fing4right};
		for(int index = 0; index < 10; index++) {
			printf("keys %d:\n", index);
			for(unsigned int c = 0; c < keys[index].size(); c++) {
				printf("  %d = %d\n", c, keys[index][c]);
			}
		}
		//delete keys;
	}
	if(keysEmpty) {
		std::cout << "No Hotkeys Assigned: "<< argv[0] << ".\n" << std::flush;
		exit(-1);
	}


	ISynAPI *api;

	if(CoCreateInstance(_uuidof(SynAPI), 0, CLSCTX_INPROC_SERVER, _uuidof(ISynAPI), (void **) &api) || api->Initialize()) {
		std::cout << "Could not obtain a Synaptics API object.\n" << std::flush;
		exit(-1);
	}

	ISynDevice *dev = 0;
	long handle = -1;
	if ((api->FindDevice(SE_ConnectionAny, SE_DeviceTouchPad, &handle) &&
		api->FindDevice(SE_ConnectionAny, SE_DeviceStyk, &handle) &&
		api->FindDevice(SE_ConnectionAny, SE_DeviceAny, &handle)) || 
		api->CreateDevice(handle, &dev)) {
		std::cout << "Unable to find a Synaptics Device.\n" << std::flush;
		exit(-1);
	}


	HANDLE event = 0;
	for (int i = 0; ; i++) {
		wchar_t name[32];
		swprintf(name, -1, L"SynCom%d", i);
		event = CreateEvent(0, 0, 0, name);
		if (GetLastError() != ERROR_ALREADY_EXISTS)
			break;
	}
	
	api->SetEventNotification(event);
	dev->SetEventNotification(event);

	std::cout << "Running.\n" << std::flush;

	SynPacket packet;
	int three_counter = 0;
	int four_counter = 0;
	int max_dx = 0;
	int max_dy = 0;
	bool sleep = false;
	for(;;) {
		WaitForSingleObject(event, INFINITE);

		for(;;) {
			long lParameter = 0;
			HRESULT result = api->GetEventParameter(&lParameter);
			if (result != S_OK)
				break;

			if(debug)printf("API event parameter: %d.\n", lParameter);

			if(lParameter == 1) {
				dev->SetEventNotification(event);
			}
		}

		for (;;) {
			HRESULT result = dev->LoadPacket(packet);
			if (result == SYNE_FAIL) {
				break;
			}
				
			long fing_num = packet.GetLongProperty(SP_ExtraFingerState)&0x0000000f;

			long dx = packet.XMickeys();
			long dy = packet.YMickeys();

			if(debug) printf("SN: %d, Fstate: %.8x, Fc: %.8x, dx: %d, dy: %d.\n", packet.SequenceNumber(), packet.FingerState(), fing_num, packet.XMickeys(), packet.YMickeys());
			
			//Handle 3 fingers
			if(fing_num == 3) {
				three_counter += 1;
				if(abs(dx) > abs(max_dx)) {
					max_dx = dx;
				}

				if(abs(dy) > abs(max_dy)) {
					max_dy = dy;
				}
			} else if(fing_num > 3) {
				three_counter = 0;
			} else {
				if (three_counter > 1 && three_counter < 2*sensitivity) {

					if(abs(max_dy) > abs(max_dx)) {
						max_dx = 0;
					}
					
					if(max_dx > 3*sensitivity) {//Right
						send_keys(fing3right);
					} else if(max_dx < -3*sensitivity) {//Left
						send_keys(fing3left);
					} else if(max_dy > 3*sensitivity) {//Up
						send_keys(fing3up);
					} else if(max_dy < -3*sensitivity) {//Down
						send_keys(fing3down);
					} else if(three_counter< sensitivity && abs(max_dx) < 2 && abs(max_dy) < 2) {//Tap
						send_keys(fing3tap);
					}
				}

				three_counter = 0;
			}

			//Handle 4 fingers
			if(fing_num == 4) {
				four_counter += 1;
				if(abs(dx) > abs(max_dx)) {
					max_dx = dx;
				}

				if(abs(dy) > abs(max_dy)) {
					max_dy = dy;
				}
			} else if(fing_num > 4) {
				four_counter = 0;
			} else {
				if (four_counter > 1 && four_counter < 2*sensitivity) {

					if(abs(max_dy) > abs(max_dx)) {
						max_dx = 0;
					}
					
					if(max_dx > 3*sensitivity) {//Right
						send_keys(fing4right);
					} else if(max_dx < -3*sensitivity) {//Left
						send_keys(fing4left);
					} else if(max_dy > 3*sensitivity) {//Up
						send_keys(fing4up);
					} else if(max_dy < -3*sensitivity) {//Down
						send_keys(fing4down);
					} else if(four_counter < sensitivity && abs(max_dx) < 2 && abs(max_dy) < 2) {//Tap
						send_keys(fing4tap);
					}
				}

				four_counter = 0;
			}

			if((three_counter == 0 || three_counter > 2*sensitivity) && (four_counter == 0 || four_counter > 2*sensitivity)) {
				max_dx = 0;
				max_dy = 0;
			}


		}

	}

	dev->Release();
	api->Release();

	return 0;
}

void send_keys(std::vector<int> keys) {
	//printf("Gesture Detected.\n");
	for (unsigned int n = 0; n < keys.size(); n++) {
		keybd_event(keys[n], 0x45, 0, 0);
	}

	for (int n = keys.size()-1; n >= 0; n--) {
		keybd_event(keys[n], 0x45, KEYEVENTF_KEYUP, 0);
	}
}