#include <iostream>
#include <vector>

using namespace std;

bool hasCycle(vector<vector<int>>& graph, vector<bool>& visited, int node, int parent){
    visited[node] = true;
    int n = graph.size();
    for(int i=0;i<n;i++){
        if(graph[node][i]){
            if(!visited[i]){
                if(hasCycle(graph,visited,i,node))return true;
            } 
            else if(i != parent)return true;
        }
    }
    return false;
}

void doDFS(vector<vector<int>>& graph, vector<bool>& visited,int node){
    int n = graph.size();
    for(int i=0;i<n;i++){
        if(!visited[i])break;
        if(visited[i] && i==n-1){
            return;
        }
    }
    visited[node] = true;
    cout<<node<<" ";
    for(int i=0;i<n;i++){
        if(graph[node][i]){
            if(!visited[i]){
                doDFS(graph,visited,i);
            }
        }
    }
}

void findConnComp(vector<vector<int>>& graph, vector<bool>& visited){
    int n = graph.size();
    int connComp = 0;
    for(int i=0;i<n;i++){
        if(!visited[i]){
            cout<<"Connected Component "<<connComp+1<<" is given below:- \n";
            doDFS(graph,visited,i);
            connComp += 1;
        }
    }
}

int main(){
    int n,start;
    cin>>n>>start;
    vector<vector<int>> graph(n,vector<int>(n,0));
    for(int i=0;i<n;i++){
        for(int j=0;j<n;j++){
            cin>>graph[i][j];
        }
    }
    vector<bool> visited(n,false);
    bool ans = hasCycle(graph,visited,start,-1);
    if(ans){
        cout<<"Cycle is detected\n";
    }else{
        cout<<"Cycle is not detected\n";
    }
    for(int i=0;i<n;i++){
        visited[i] = false;
    }
    cout<<"\nDFS Traversal is given below:-\n";
    doDFS(graph,visited,start);
    for(int i=0;i<n;i++){
        visited[i] = false;
    }
    cout<<"\nConnected Components is given below:-\n";
    findConnComp(graph,visited);
    return 0;
}