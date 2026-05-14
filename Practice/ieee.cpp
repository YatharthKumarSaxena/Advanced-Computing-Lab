#include <iostream>
#include <bitset>
using namespace std;

union IEEE{
    double f;
    unsigned long long i;
};

int main(){
    IEEE num;
    cin>>num.f;
    bitset<64> bits(num.i);
    cout<<"IEEE 754: "<<bits<<endl;
    cout<<"Sign Bit: "<<bits[63]<<endl;
    cout<<"Exponent: ";
    for(int i=62;i>=52;i--){
        cout<<bits[i];
    }
    cout<<"\nMantissa: ";
    for(int i=51;i>=0;i--){
        cout<<bits[i];
    }
}