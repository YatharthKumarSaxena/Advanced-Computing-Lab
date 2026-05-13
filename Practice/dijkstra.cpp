#include <iostream>
#include <vector>
#include <queue>
#include <algorithm>
#include <climits>
using namespace std;

void doDijkstra(vector<vector<int>> graph,int src){
    int n = graph.size();
    bool* visited = new bool[n];
    for(int i=0;i<n;i++){
        visited[i] = false;
    }
    int* dist = new int[n];
    for(int i=0;i<n;i++){
        dist[i] = 9999;
    }
    dist[src] = 0;
    for(int c=0;c<n;c++){
        int u=-1;
        for(int i=0;i<n;i++){
            if(!visited[i] && dist[i]!=9999){
                if(u == -1 || dist[i]<dist[u]){
                    u = i;
                }
            }
        }
        if(u==-1)break;
        visited[u]=true;
        // Relax Nodes
        for(int v=0;v<n;v++){
            if(!visited[v] && graph[u][v]!=9999){
                if(dist[v]!=9999) dist[v]=min(dist[v],dist[u]+graph[u][v]);
                else dist[v] = dist[u]+graph[u][v];
            }
        }
    }
    for(int i=0;i<n;i++){
        cout<<"Distance from Node "<<src<<" to "<<" Node "<<i<<" is: "<<dist[i]<<endl;
    }
}

void doBellmanFord(vector<vector<int>> graph,int src){
    int n = graph.size();
    int* dist = new int[n];
    for(int i=0;i<n;i++){
        dist[i]=9999;
    }
    dist[src]=0;
    for(int k=0;k<n-1;k++){
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                if(graph[i][j]!=9999 && dist[i]!=9999){
                    int newDist = dist[i]+graph[i][j];
                    if(dist[j]!=9999)dist[j]=min(dist[j],newDist);
                    else dist[j]=newDist;
                }
            }
        }
    }
    bool negativeCycle = false;
    for(int i=0;i<n;i++){
        for(int j=0;j<n;j++){
            if(graph[i][j]!=9999 && dist[i]!=9999 && dist[j]!=9999){
                if(dist[j] > dist[i]+graph[i][j]){
                    negativeCycle = true;
                    break;
                }   
            }
        }
        if(negativeCycle)break;
    }
    if(negativeCycle){
        cout<<"Negative Cycle detected\n";
    }else{
        for(int i=0;i<n;i++){
            cout<<"Distance from Node "<<src<<" to "<<" Node "<<i<<" is: "<<dist[i]<<endl;
        }
    }
}

void doPrims(vector<vector<int>> graph,int src){
    int n = graph.size();
    int* keys = new int[n];
    bool* visited = new bool[n];
    int* parent = new int[n];
    for(int i=0;i<n;i++){
        keys[i]=9999;
        visited[i]=false;
        parent[i]=-1;
    }
    keys[src]=0;
    for(int c=0;c<n;c++){
        int u=-1;
        for(int i=0;i<n;i++){
            if(!visited[i] && keys[i]!=9999){
                if(u==-1 || keys[i]<keys[u]){
                    u=i;
                }
            }
        }
        if(u==-1)break;
        visited[u]=true;
        for(int v=0;v<n;v++){
            if(graph[u][v]!=9999 && !visited[v]){
                if(keys[v]>graph[u][v]){
                    keys[v]=graph[u][v];
                    parent[v]=u;
                }
            }
        }
    }
    int totalCost=0;
    for(int i=0;i<n;i++){
        if(parent[i]!=-1){
            cout<<parent[i]<<" - "<<i<<" : "<<keys[i]<<endl;
            totalCost+=keys[i];
        }
    }
    cout<<"Total Cost Of MST: "<<totalCost;
}

class DSU{
    int capacity;
    int* par;
    int* rank;
public:
    DSU(int capacity){
        this->capacity = capacity;
        par = new int[capacity];
        rank = new int[capacity];
        for(int i=0;i<capacity;i++){
            par[i]=i;
            rank[i]=1;
        }
    }
    int getParent(int ele);
    int getSize();
    void unionGroups(int eleA, int eleB);
    int getRank(int ele);

};

int DSU::getParent(int ele){
    return par[ele] = (ele==par[ele])? ele : getParent(par[ele]);
}

int DSU::getRank(int ele){
    return this->rank[ele];
}

int DSU::getSize(){
    return this->capacity;
}

void DSU::unionGroups(int eleA,int eleB){
    int parA = getParent(eleA);
    int parB = getParent(eleB);
    int rankA = getRank(parA);
    int rankB = getRank(parB);
    if(rankA >= rankB){
        this->par[parB]=parA;
        this->rank[parA]++;
    }else{
        this->par[parA]=parB;
        this->rank[parB]++;
    }
    return;
}

class Edges{
public:
    int u;
    int v;
    int w;
    Edges(int u,int v,int w){
        this->u = u;
        this->v = v;
        this->w = w;
    }
};

bool compareEdges(Edges a, Edges b){
    return a.w < b.w;
}

void doKruskal(vector<vector<int>> graph,int src){
    int n = graph.size();
    vector<Edges> edges;
    int totalEdges = 0;
    for(int i=0;i<n;i++){
        for(int j=i+1;j<n;j++){
            if(graph[i][j] != 9999){
                Edges e = Edges(i,j,graph[i][j]);
                edges.push_back(e);
                totalEdges++;
            }
        }
    }
    sort(edges.begin(),edges.end(),compareEdges);
    DSU dsu(n);
    int totalCost = 0;
    for(int i=0;i<totalEdges;i++){
        int u = edges[i].u;
        int v = edges[i].v;
        int w = edges[i].w;
        int parU = dsu.getParent(u);
        int parV = dsu.getParent(v);
        if(parU != parV){
            cout<<"Edge from "<<u<<" to "<<v<<" : "<<w<<endl;
            totalCost += w;
            dsu.unionGroups(u,v);
        }
    }
    cout<<"MST Cost = "<<totalCost;
}

void doKahnsTopo(vector<vector<int>> graph){
    int n = graph.size();
    vector<int>indegrees(n,0);
    for(int i=0;i<n;i++){
        for(int j=0;j<n;j++){
            if(graph[i][j]!=9999){
                indegrees[j]++;
            }
        }
    }
    queue<int>qu;
    vector<int>topo;
    for(int i=0;i<n;i++){
        if(indegrees[i]==0){
            qu.push(i);
        }
    }
    while(!qu.empty()){
        int ele = qu.front();
        qu.pop();
        topo.push_back(ele);
        for(int i=0;i<n;i++){
            if(graph[ele][i]!=9999){
                indegrees[i]--;
                if(indegrees[i]==0)qu.push(i);
            }
        }
    }
    if(topo.size()<n){
        cout<<"Cycle Exists in Your DAG\n";
    }else{
        cout<<"Topological Sort is given below: \n";
        for(int i=0;i<n;i++){
            if(i!=n-1)cout<<topo[i]<<" -> ";
            else cout<<topo[i];
        }
    }
}

int main(){
    int n,source;
    cin>>n>>source;
    vector<vector<int>> graph(n,vector<int>(n,9999));
    for(int i=0;i<n;i++){
        for(int j=0;j<n;j++){
            cin>>graph[i][j];
        }
    }
    cout<<"\nDijkstra Algorithm:"<<endl;
    doDijkstra(graph,source);
    cout<<"\nBellman Ford Algorithm:"<<endl;
    doBellmanFord(graph,source);
    cout<<"\nPrims Algorithm:"<<endl;
    doPrims(graph,source);
    cout<<"\nKruskal Algorithm:"<<endl;
    doKruskal(graph,source);
    cout<<"\nKahn's Topological Sort:"<<endl;
    doKahnsTopo(graph);
    return 0;
}