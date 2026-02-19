#include <iostream>
#include <unistd.h>  // Unix System Calls
#include <sys/shm.h>  // Shared Memory
#include <sys/wait.h>  // To Produce Delay
#include <semaphore.h>  // POSIX Semaphores
#include <ctime>   // For Timestamp
// #include <cstdlib>

using namespace std;

// Shared structure
struct SharedData{
    int in; // Producer Write Index
    int out; // Consumer Read Index
    int bufferSize;
    sem_t empty;  // Counts empty slots
    sem_t full;
    sem_t indexMutex; // Mutual exclusion lock
};

// Timestamp function
string getTimeStamp(){
    time_t now = time(0); // Current epoch time.
    tm *ltm = localtime(&now);  // Convert to local time struct.

    char buffer[20];  // Store formatted time.
    strftime(buffer, sizeof(buffer), "%H:%M:%S", ltm);  // Time format: HH:MM:SS
    return string(buffer);
}

int main(){

    int bufferSize, producers, consumers;

    cout<<"Enter Buffer Size: ";
    cin>>bufferSize;

    cout<<"Enter Number of Producers: ";
    cin>>producers;

    cout<<"Enter Number of Consumers: ";
    cin>>consumers;

    // Shared memory allocation
    int shmSize = sizeof(SharedData) + bufferSize * sizeof(int) + bufferSize * sizeof(sem_t);  // In Bytes

    // Create shared memory segment
    int shmid = shmget(IPC_PRIVATE, shmSize, IPC_CREAT | 0666);
    // Attach memory to process address space
    void* ptr = shmat(shmid, NULL, 0);

    // Initialize shared data
    SharedData* data = (SharedData*) ptr; // Type Casting
    int* buffer = (int*)(data + 1);
    sem_t* slotMutex = (sem_t*)(buffer + bufferSize);

    // Initialize indexes
    data->in = 0;
    data->out = 0;
    data->bufferSize = bufferSize;

    // Initialize semaphores
    sem_init(&data->empty, 1, bufferSize); // Process Shared, not thread shared
    sem_init(&data->full, 1, 0);
    sem_init(&data->indexMutex, 1, 1);

    for(int i=0;i<bufferSize;i++)
        sem_init(&slotMutex[i], 1, 1);

    // Producers
    for(int p=0;p<producers;p++){

        if(fork()==0){

            // Set Starting Point i.e Seed
            srand(getpid());   // Unique random per producer

            while(true){

                sem_wait(&data->empty);

                sem_wait(&data->indexMutex);
                int index=data->in;
                data->in=(data->in+1)%data->bufferSize;
                sem_post(&data->indexMutex);

                sem_wait(&slotMutex[index]);

                int item=rand()%100;  // Pseudo-random item between 0-99
                buffer[index]=item;

                cout<<"["
                    <<getTimeStamp()
                    <<"] Producer PID "
                    <<getpid()
                    <<" → Produced "
                    <<item
                    <<" at Buffer["
                    <<index
                    <<"]"
                    <<endl;

                sem_post(&slotMutex[index]);
                sem_post(&data->full);

                // Pause AFTER signaling 
                usleep(400000);   // 0.4 sec
            }

            exit(0);
        }
    }

    // Consumers
    for(int c=0;c<consumers;c++){

        if(fork()==0){

            while(true){

                sem_wait(&data->full);

                sem_wait(&data->indexMutex);
                int index=data->out;
                data->out=(data->out+1)%data->bufferSize;
                sem_post(&data->indexMutex);

                sem_wait(&slotMutex[index]);

                int item=buffer[index];

                cout<<"["
                    <<getTimeStamp()
                    <<"] Consumer PID "
                    <<getpid()
                    <<" → Consumed "
                    <<item
                    <<" from Buffer["
                    <<index
                    <<"]"
                    <<endl;

                sem_post(&slotMutex[index]);
                sem_post(&data->empty);

                // Pause AFTER signaling
                usleep(700000);   // 0.7 sec
            }

            exit(0);
        }
    }

    // Parent waits (infinite anyway)
    for(int i=0;i<producers+consumers;i++)
        wait(NULL);

    // Cleanup (never reached in infinite run)
    sem_destroy(&data->empty);
    sem_destroy(&data->full);
    sem_destroy(&data->indexMutex);

    for(int i=0;i<bufferSize;i++)
        sem_destroy(&slotMutex[i]);

    shmdt(ptr);
    shmctl(shmid, IPC_RMID, NULL);

    return 0;
}
