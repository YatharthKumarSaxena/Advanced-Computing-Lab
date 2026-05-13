#include <iostream>
#include <queue>
using namespace std;

class TreeNode{
public:
    int val;
    TreeNode* left;
    TreeNode* right;
    TreeNode(int val){
        this->val = val;
        this->left = NULL;
        this->right = NULL;
    }
    void preOrder(TreeNode* root);
    void postOrder(TreeNode* root);
    void inOrder(TreeNode* root);
    void levelOrder(TreeNode* root);
    int heightOfBT(TreeNode* root);
    int countNodes(TreeNode* root);
    int countLeafNodes(TreeNode* root);
    bool searchEle(TreeNode* root, int val);
    int diameter(TreeNode* root);
    bool isBalanced(TreeNode* root);
    TreeNode* findLCA(TreeNode* root,int a,int b);
    TreeNode* makeGraph();
};

TreeNode* TreeNode::findLCA(TreeNode* root,int a,int b){
    if(root==NULL)return NULL;
    if(root->val == a || root->val == b){
        return root;
    }
    TreeNode* leftLCA = findLCA(root->left,a,b);
    TreeNode* rightLCA = findLCA(root->right,a,b);
    if(leftLCA != NULL && rightLCA !=NULL){
        return root;
    }
    if(leftLCA != NULL){
        return leftLCA;
    }
    return rightLCA;
}

bool TreeNode::isBalanced(TreeNode* root){
    if(root == NULL)return true;
    int leftHeight = heightOfBT(root->left);
    int rightHeight = heightOfBT(root->right);
    if(abs(leftHeight-rightHeight)>1)return false;
    return isBalanced(root->left) && isBalanced(root->right);
}

int TreeNode::diameter(TreeNode* root){
    if(root==NULL)return 0;
    int leftHeight = heightOfBT(root->left);
    int rightHeight = heightOfBT(root->right);
    int currDiameter = leftHeight + rightHeight;
    int leftDiameter = diameter(root->left);
    int rightDiameter = diameter(root->right);
    return max(currDiameter,max(leftDiameter,rightDiameter));
}

class BST{
    TreeNode* root;
public:
    BST(){
        this->root = NULL;
    }
    void insertInBST(int val);
    TreeNode* deleteInBST(TreeNode* root, int val);
    bool searchInBST(int val);
    TreeNode* findLCA(int a,int b);
    TreeNode* getMin(TreeNode* root);
};

TreeNode* BST::getMin(TreeNode* root){
    if(root == NULL)return NULL;
    TreeNode* temp = root;
    while(temp->left)temp = temp->left;
    return temp;
}

TreeNode* BST:: findLCA(int a, int b){
    bool exist = searchInBST(a) && searchInBST(b);
    if(!exist)return NULL;
    TreeNode*temp = root;
    while(temp){
        if(temp->val>a && temp->val>b){
            temp = temp->left;
        }else if(temp->val<a && temp->val<b){
            temp = temp->right;
        }else{
            return temp;
        }
    }
    return NULL;
}

void BST::insertInBST(int val){
    TreeNode* newNode = new TreeNode(val);
    if(root==NULL){
        root = newNode;
    }else{
        TreeNode* temp = this->root;
        while(true){
            if(val<temp->val){
                if(temp->left==NULL){
                    temp->left=newNode;
                    break;
                }
                else temp = temp->left;
            }else{
                if(temp->right==NULL){
                    temp->right=newNode;
                    break;
                }
                else temp = temp->right;
            }
        }
    }
}

TreeNode* BST::deleteInBST(TreeNode* root,int val){
    if(root==NULL)return NULL;
    TreeNode* temp = root;
    if(temp->val > val){
        root->left = deleteInBST(root->left,val);
    }
    else if(temp->val < val){
        root->right = deleteInBST(root->right,val);
    }
    else{
        if(root->left == NULL && root->right == NULL){
            temp = root;
            delete root;
            return NULL;
        }
        else if(root->right == NULL){
            temp = root->left;
            delete root;
            return temp;
        }
        else if(root->left == NULL) {
            temp = root->right;
            delete root;
            return temp;
        }else {
            TreeNode* minNode = getMin(root->right);
            root->val = minNode->val;
            root->right = deleteInBST(root->right,minNode->val);
            return root;
        }
    }
    return root;
}

bool BST::searchInBST(int val){
    if(root==NULL)return false;
    TreeNode* temp = root;
    while(true){
        if(temp->val==val)return true;
        else if(temp->val<val){
            if(temp->right==NULL)return false;
            else temp = temp->right;
        }else{
            if(temp->left==NULL)return false;
            else temp = temp->left; 
        }
    }
}

int TreeNode::heightOfBT(TreeNode* root){
    if(root == NULL)return 0;
    return (1+max(heightOfBT(root->left),heightOfBT(root->right)));
}

int TreeNode::countLeafNodes(TreeNode* root){
    if(root==NULL)return 0;
    if(root->left==NULL && root->right==NULL)return 1;
    return countLeafNodes(root->left)+countLeafNodes(root->right);
}

int TreeNode::countNodes(TreeNode* root){
    if(root == NULL)return 0;
    return 1+countNodes(root->left)+countNodes(root->right);
}

bool TreeNode::searchEle(TreeNode* root,int val){
    if(root==NULL)return false;
    if(root->val == val)return true;
    return searchEle(root->left,val) || searchEle(root->right,val);
}

TreeNode* TreeNode::makeGraph(){
    int n,rootVal;
    cin>>n>>rootVal;
    TreeNode* root = new TreeNode(rootVal);

    int i=1;
    while(i<n){
        string loc;
        cin>>loc;
        TreeNode* temp = root;
        for(int j=0;j<loc.length()-1;j++){
            if(loc[j]=='R'){
                temp = temp->right;
            }else {
                temp = temp->left;
            }
        }
        int val;
        cin>>val;
        TreeNode* node = new TreeNode(val);
        if(loc[loc.length()-1] == 'R')temp->right = node;
        else temp->left = node;
        i++;
    }
    return root;
}

void TreeNode::preOrder(TreeNode* root){
    if(root==NULL)return;
    cout<<root->val;
    preOrder(root->left);
    preOrder(root->right);
}

void TreeNode::inOrder(TreeNode* root){
    if(root==NULL)return;
    inOrder(root->left);
    cout<<root->val;
    inOrder(root->right);
}

void TreeNode::postOrder(TreeNode* root){
    if(root==NULL)return;
    postOrder(root->left);
    postOrder(root->right);
    cout<<root->val;
}

void TreeNode::levelOrder(TreeNode* root){
    if(root==NULL)return;
    queue<TreeNode*>qu;
    qu.push(root);
    while(!qu.empty()){
        TreeNode* node = qu.front();
        qu.pop();
        cout<<node->val<<" ";
        if(node->left)qu.push(node->left);
        if(node->right)qu.push(node->right);
    }
    cout<<endl;
    return;
}

int main(){

    TreeNode helper(0);

    cout<<"Create Binary Tree\n";

    /*
        INPUT FORMAT:

        n rootValue

        path value

        Example:
        7 1
        L 2
        R 3
        LL 4
        LR 5
        RL 6
        RR 7
    */

    TreeNode* root =
    helper.makeGraph();

    cout<<"\nPreorder:\n";
    helper.preOrder(root);

    cout<<"\n\nInorder:\n";
    helper.inOrder(root);

    cout<<"\n\nPostorder:\n";
    helper.postOrder(root);

    cout<<"\n\nLevel Order:\n";
    helper.levelOrder(root);

    cout<<"\nHeight Of Tree: ";
    cout<<helper.heightOfBT(root);

    cout<<"\n\nTotal Nodes: ";
    cout<<helper.countNodes(root);

    cout<<"\n\nLeaf Nodes: ";
    cout<<helper.countLeafNodes(root);

    cout<<"\n\nDiameter: ";
    cout<<helper.diameter(root);

    cout<<"\n\nBalanced Tree? ";
    if(helper.isBalanced(root))
        cout<<"YES";
    else
        cout<<"NO";

    cout<<"\n\nSearch 5: ";
    if(helper.searchEle(root,5))
        cout<<"Found";
    else
        cout<<"Not Found";

    TreeNode* lca =
    helper.findLCA(root,4,5);

    cout<<"\n\nLCA of 4 and 5: ";

    if(lca)
        cout<<lca->val;
    else
        cout<<"NULL";

    // ==========================
    // BST TESTING
    // ==========================

    BST bst;

    bst.insertInBST(50);
    bst.insertInBST(30);
    bst.insertInBST(70);
    bst.insertInBST(20);
    bst.insertInBST(40);
    bst.insertInBST(60);
    bst.insertInBST(80);

    cout<<"\n\nBST Search 60: ";

    if(bst.searchInBST(60))
        cout<<"Found";
    else
        cout<<"Not Found";

    TreeNode* bstLCA =
    bst.findLCA(20,40);

    cout<<"\n\nBST LCA of 20 and 40: ";

    if(bstLCA)
        cout<<bstLCA->val;
    else
        cout<<"NULL";

    cout<<"\n\nDelete 70 from BST\n";

    bst.deleteInBST(NULL,70);

    cout<<"\nProgram Completed Successfully\n";

    return 0;
}

