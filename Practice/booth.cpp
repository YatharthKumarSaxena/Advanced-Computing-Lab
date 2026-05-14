#include <iostream>
#include <bitset>
using namespace std;

int main(){
    int m,q,q1=0,a=0;
    int n = 4;
    cin>>m>>q;
    int count = n;
    while(count--){
        int q0 = q&1;
        if(q0==1 && q1==0){
            a=a-m;
        }
        if(q0==0 && q1==1){
            a=a+m;
        }
        int tempQ = q;
        q=(q>>1)|(a&1)<<(n-1);
        q1=tempQ&1;
        a=a>>1;
    }
    cout<<"Results in Decimal: "<<((a<<n)|q)<<endl;

}