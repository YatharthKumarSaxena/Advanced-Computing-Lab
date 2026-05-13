#include <iostream>
#include <vector>
#include <algorithm>
#include <queue>

using namespace std;

void doBFS(vector<vector<int>> &v, int start)
{
    cout << "BFS Traversal is given below:-\n";
    int n = v.size();
    bool visited[n];
    for (int i = 0; i < n; i++)
    {
        visited[i] = false;
    }
    queue<int> qu;
    qu.push(start);
    cout << start << endl;
    visited[start] = true;
    while (!qu.empty())
    {
        int firstEle = qu.front();
        qu.pop();
        for (int j = 0; j < n; j++)
        {
            if (v[firstEle][j] != 0 && visited[j] == false)
            {
                qu.push(j);
                visited[j] = true;
                cout << j << " ";
            }
        }
        cout << endl;
    }
}

void doBFSInSnake(vector<vector<int>> &v, int start){
    cout<<"Snake Traversal is given below:- \n";
    int n = v.size();
    bool visited[n];
    for(int i=0;i<n;i++){
        visited[i] = false;
    }
    queue<int>qu;
    qu.push(start);
    visited[start] = true;
    bool reversePath = true;
    cout<<start<<endl;
    while(!qu.empty()){
        vector<int>levels;
        int firstEle = qu.front();
        qu.pop();
        for(int i=0;i<n;i++){
            if(v[firstEle][i] != 0 && visited[i] == false){
                qu.push(i);
                visited[i] = true;
                levels.push_back(i);
            }
        }
        if(reversePath){
            reverse(levels.begin(),levels.end());
        }
        reversePath = !reversePath;
        for(int i=0;i<levels.size();i++){
            cout<<levels[i]<<" ";
        }
        cout<<endl;
    }
}

int main()
{
    int n, start;
    cin >> n >> start;
    vector<vector<int>> dist(n, vector<int>(n));
    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < n; j++)
        {
            cin >> dist[i][j];
        }
    }

    doBFS(dist, start);

    doBFSInSnake(dist, start);
    return 0;
}