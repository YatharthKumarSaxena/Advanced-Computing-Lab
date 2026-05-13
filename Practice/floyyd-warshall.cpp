#include <iostream>
#include <vector>
using namespace std;

void floydWarshall(vector<vector<int>> &dist)
{
    int n = dist.size();
    for (int k = 0; k < n; k++)
    {
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                if(i==j)continue;
                else if(dist[i][j] == -1 && dist[i][k] != -1 && dist[k][j] != -1){
                    dist[i][j] = dist[i][k] + dist[k][j];
                }
                else if (dist[i][j] != -1 && dist[i][k] != -1 && dist[k][j] != -1){
                    dist[i][j] = min(dist[i][j],dist[i][k]+dist[k][j]);
                }
            }
        }
    }
}

int main()
{
    int n;
    cin >> n;
    vector<vector<int>> dist(n, vector<int>(n));
    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < n; j++)
        {
            cin >> dist[i][j];
        }
    }

    for(int i=0;i<n;i++){
        for(int j=0;j<n;j++){
            if(i==j){
                dist[i][j] = 0;
            }
        }
    }

    floydWarshall(dist);

    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < n; j++)
        {
            cout << dist[i][j] << " ";
        }
        cout << endl;
    }
}