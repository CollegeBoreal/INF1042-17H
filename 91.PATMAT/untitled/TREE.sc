void printTree(node *tree)
{
  if(!tree) return;

  if(tree->left)  printTree(tree->left);

  printf("Cle = %d\n", tree->key);

  if(tree->right) printTree(tree->right);
}