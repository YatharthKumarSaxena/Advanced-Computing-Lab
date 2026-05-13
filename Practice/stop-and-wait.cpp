#include <iostream>
#include <ctime>
#include <string>
#include <thread>
#include <chrono>
#include <cstdlib>

using namespace std;

string currTime(){
    time_t now = time(0);
    char* dt = ctime(&now);
    return string(dt).substr(0,24);
}

void delay(int ms){
    this_thread::sleep_for(chrono::milliseconds(ms));
}

int main(){
    srand(time(0));
    int totalFrames,corrProb,timeoutTime;
    cin>>totalFrames>>corrProb>>timeoutTime;
    int i=0;
    while(i<totalFrames){
        int sendDelay = rand()%1000+200;
        delay(sendDelay);
        cout<<"[ "<<currTime()<<" ] "<<"Sender sending "<<i<<" to Reciever"<<endl;
        int recDelay = rand()%100;
        delay(recDelay);
        bool timeOut = recDelay>=timeoutTime;
        if(timeOut){
            cout<<"[ "<<currTime()<<" ] "<<"TimeOut for "<<i<<" th Packet"<<endl;
        }else{
            bool isCorrupt = rand()%100>corrProb;
            if(isCorrupt){
                cout<<"[ "<<currTime()<<" ] "<<i<<"th Packet found Corrupted"<<endl;
            }else{
                cout<<"[ "<<currTime()<<" ] "<<i<<"th Packet Recieved by Reciever Successfully"<<endl;
                i++;
            }
        }
    }
    cout<<"All Total "<<totalFrames<<" Frames recieved successfully\n";
}