#include <iostream>
#include <thread>
#include <chrono>
#include <cstdlib>
#include <string>

using namespace std;

void delay(int ms)
{
    this_thread::sleep_for(chrono::milliseconds(ms));
}

string currentTime()
{
    time_t now = time(0);
    char *dt = ctime(&now);
    return string(dt).substr(0, 24);
}

int main()
{
    srand(time(0));
    int frames, windowSize, probLoss;
    cin >> frames >> windowSize >> probLoss;
    int seqMode = windowSize + 1;
    int i = 0;
    int base = 0;
    int lastAck = -1;
    while (base < frames && i < frames)
    {
        cout << "[" << currentTime() << "] "<< "Current Frame Window: " << base << " -> " << min(base + windowSize - 1, frames - 1) << endl;
        bool timeOut;
        while (i < base + windowSize && i < frames)
        {
            delay(rand() % 100);
            int procFrame = i % seqMode;
            cout << "[" << currentTime() << "] "<< "Sender able to send " << procFrame << "th Frame\n";
            i++;
        }
        for (int k = base; k < i; k++)
        {
            delay(rand() % 100);
            int procFrame = k % seqMode;
            timeOut = (rand() % 100) < probLoss;
            if (timeOut)
            {
                lastAck = k - 1;
                cout << "[" << currentTime() << "] "<< "\nReciever not able to recieve " << procFrame << "th Frame and Give Ack: " << lastAck << endl;
                break;
            }
            else
            {
                lastAck = k;
                cout << "[" << currentTime() << "] "<< "\nReciever able to recieve " << procFrame << "th Frame and Give Ack: " << lastAck << endl;
            }
        }
        i = lastAck + 1;
        base = i;
    }
    cout << "[" << currentTime() << "] "<< "All Frames sent by GBN Successfully\n";
    return 0;
}