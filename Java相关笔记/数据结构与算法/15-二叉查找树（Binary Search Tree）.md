# 二叉查找树（Binary Search Tree）

## 二叉查找树的定义

二叉查找树是二叉树中最常见的一种类型，也叫二叉搜索树。二叉查找树是为了实现快速查找而生的。不过，它不仅仅支持快速查找一个数据，还支持快速插入、删除一个数据。

二叉查找树要求，在树中的任意一个节点，其左子树中的每个节点的值，都要小于这个节点的值，而右子树节点的值都大于这个节点的值。

![二叉查找树示例](https://static001.geekbang.org/resource/image/f3/ae/f3bb11b6d4a18f95aa19e11f22b99bae.jpg)

## 二叉查找树的查找操作

二叉查找树的查找过程是，先取根节点，如果它等于要查找的数据，那就返回。如果要查找的数据小于根节点，那么就在左子树中递归查找；如果查找的数据大于根节点，就在右子树中递归查找。

```java
/**
 * 查找指定数据
 * @param t
 * @return
 */
public Node find(T t) {
    Node p = root;
    while (p != null) {
        if(p.data.equals(t)) {
            return p;
        }else if(p.data.compareTo(t) > 0) {
            p = p.left;
        }else {
            p = p.right;
        }
    }
    return null;
}
```

## 二叉查找树的插入操作

二叉查找的插入操作，新插入的数据一般都是在叶子节点上，从根节点开始，依次比较要插入的数据和节点的大小关系。如果要插入的数据比节点的数据大，并且节点的右子数为空，就将新数据直接插到右节点的位置；如果不为空，就再遍历右子树，查找插入位置。同理，如果新插入的数据比节点的数据大，并且节点的左子树为空，就将新数据插入到左子节点的位置；如果不为空，就再递归遍历左子树，查找插入位置。

```java
/**
 * 插入数据
 * @param t
 */
public void insert(T t) {
    Node value = new Node(t);
    if(root == null) {
        root = value;
        return;
    }
    Node p = root;
    while (p != null) {
        if(p.data.compareTo(value.data) > 0) {
            if(p.left == null) {
                p.left = value;
                return;
            }else {
                p = p.left;
            }
        }else {
            if(p.right == null) {
                p.right = value;
                return;
            }else {
                p = p.right;
            }
        }
    }
}
```

## 二叉查找树的删除操作

二叉查找树的删除操作，相比于查找和插入操作比较复杂。删除操作根据删除节点的子节点的个数不同，需要不同的处理方式。

* 如果删除的节点没有子节点，只需要直接将父节点指向的位置设置为null就可以了。

* 如果删除的节点只有一个子节点（只有左子节点或右子节点），只需要更新父节点中指向要删除节点的指针，让它指向删除节点的子节点就可以了。

* 如果删除的节点有两个子节点，需要找到这个节点的右子树中的最小节点，把它替换到要删除的节点上。然后再删除这个最小节点。

实际上，关于二叉查找树的删除操作，还有个非常简单、取巧的方法，就是单纯将要删除的节点标记为“已删除”，但是并不真正从树中将这个节点去掉。这样原本删除的节点还需要存储在内存中，比较浪费内存空间，但是删除操作就变得简单了很多。而且，这种处理方法也并没有增加插入、查找操作代码实现的难度。

```java
/**
 * 删除指定元素
 * @param t
 */
public void delete(T t) {
    Node node = root;
    Node pNode = null;
    while (node != null) {
        if(node.data.compareTo(t) == 0) {
            break;
        }else if(node.data.compareTo(t) > 0) {
            pNode = node;
            node = node.left;
        }else {
            pNode = node;
            node = node.right;
        }
    }
    if(node == null) {
        return;
    }
    if(node.left != null && node.right != null) {
        // 在右树中查询最小的左节点
        Node minNode = node.right;
        Node minPNode = node;
        while (minNode.left != null){
            minPNode = minNode;
            minNode = minNode.left;
        }
        node.data = minNode.data; // 将minNode的数据替换到node中
        node = minNode; // 下面就变成了删除minNode了
        pNode = minPNode;
    }
    Node child = null;
    if(node.left != null) {
        child = node.left;
    }else if(node.right != null) {
        child = node.right;
    }else {
        child = null;
    }
    if(pNode == null) {
        root = child;
    }else if(pNode.left == node) {
        pNode.left = child;
    }else {
        pNode.right = child;
    }
}
```

## 二叉查找树的其他操作

除了插入、删除、查找操作之外，二叉查找树中还可以支持**快速地查找最大节点和最小节点、前驱节点和后继节点**。

二叉查找树除了支持上面几个操作之外，还有一个重要的特性，就是**中序遍历二叉查找树，可以输出有序的数据序列，时间复杂度是 O(n)，非常高效。**

## 支持重复数据的二叉查找树

在实际软件开发中，在二叉查找树中存储的，是一个包含很多字段的对象。利用对象的某个字段最为键值（key）来构建二叉查找树。**把对象中的其他字段叫做卫星数据**。

**如果存储的两个对象键值相同，这种情况该怎么处理呢？**

* 第一种处理方式比较容易，二叉查找树中的每个节点不仅回存储一个数据，通过链表和支持动态扩容的数据等数据结构，把值相同的数据都存储在同一个节点上。

* 第二种方式，每个节点仍然值存储一个数据，在查找插入过程中，如果碰到要插入的数据和节点的值相同的情况，将要插入的数据放到这个节点的右子树，也就是把这个要插入的数据当作一个大于这个节点的值来处理。
  当要查找数据的时候，遇到值相同的节点，并不停止查找操作，而是继续在右子树中查找，直到遇到叶子节点，才停止。这样就可以把键值等于要查找值的所有节点都找出来。

  对于删除操作，也需要先查找到每个要删除的节点，然后再按前面讲的删除操作的方法，依次删除。

## 二叉查找树的时间复杂度分析

对于二叉查找树，最坏情况下退化成一个链表，这个时候的查找时间复杂度为O(N)。

最理想情况下，二叉查找树是一个完全二叉树（或满二叉树）。这个时候不管操作是插入、删除还是查找，**时间复杂度其实都和树的高度成正比，也就是O(height)**。既然这样，现在问题就转变成另外一个了，也就是，如何求一棵包含 n 个节点的完全二叉树的高度？

树的高度等于最大层减一，包含n个节点的完全二叉树，第一层包含一个节点，第二层包含两个节点，第三层包含四个节点，所以第K层包含2^K-1^节点。对于完全二叉树，最后一层的节点个数比较特殊，假设最大成为L，那么n满足下面的条件：

```text
n >= 1+2+4+8+...+2^(L-2)+1
n <= 1+2+4+8+...+2^(L-2)+2^(L-1)
```

借助等比数列的求和公式，可以计算出，L 的范围是[log~2~(n+1), log~2~n +1]。完全二叉树的层数小于等于 log~2~n +1，也就是说，完全二叉树的高度小于等于 log~2~n。

## 二叉查找树相比于散列表有什么优势

散列表的插入、删除、查找操作的时间复杂度可以做到常量级的O(1)，非常高效。而二叉查找树在比较平衡的情况下，插入、删除、查找操作时间复杂度才是O(logN)，相对散列表，好像没有什么优势，那为什么还需要用二叉树？

* 1.散列表中的数据是无序存储的，如果要输出有序的数据，需要先进行排序。而对于二叉查找树来说，只需要中序遍历就可以在O(N)的时间复杂度内，输出有序的数据。

* 2.散列表扩容耗时很多，而且当遇到散列冲突时，性能不稳定，尽管二叉查找树的性能不稳定，但是在工程中，我们最常用的平衡二叉查找树的性能非常稳定，时间复杂度稳定在 O(logn)。

* 3.笼统地来说，尽管散列表的查找等操作的时间复杂度是常量级的，但因为哈希冲突的存在，这个常量不一定比 logn 小，所以实际的查找速度可能不一定比 O(logn) 快。加上哈希函数的耗时，也不一定就比平衡二叉查找树的效率高。

* 4.散列表的构造比二叉查找树要复杂，需要考虑的东西很多。比如散列函数的设计，冲突的解决方式，扩容，缩容等。平衡二叉查找树只需要考虑平衡性这一个问题，而且这个问题的解决方案比较成熟、固定。

* 5.为了避免过多的散列冲突，散列表装载因子不能太大，特别是基于开放寻址法解决冲突的散列表，不然会浪费一定的存储空间。

综合这几点，平衡二叉查找树在某些方面还是优于散列表的，所以，这两者的存在并不冲突。在实际的开发过程中，需要结合具体的需求来选择使用哪一个。

## 问题

* 二叉树删除时，如果待删除节点有两个子节点，能否用左子树中的最大值来替换待删除节点呢？

  是可以的，如果是用顺序存储，更容易浪费内存。更容易变成非完全二叉树。

* 如何通过编程，求出一棵给定二叉树的确切高度呢？

  确定二叉树高度有两种思路：第一种是深度优先思想的递归，分别求左右子树的高度。当前节点的高度就是左右子树中较大的那个+1；第二种可以采用层次遍历的方式，每一层记录都记录下当前队列的长度，这个是队尾，每一层队头从0开始。然后每遍历一个元素，队头下标+1。直到队头下标等于队尾下标。这个时候表示当前层遍历完成。每一层刚开始遍历的时候，树的高度+1。最后队列为空，就能得到树的高度。

  ```java
  /**
     * 获取二叉树的高度
     * 实现方式一：深度优先思想的递归，分别求左右子树的高度。当前节点的高度就是左右子树中较大的那个+1；
     * @param root
     * @return
     */
    public static int getHeightOne(Node root) {
        if(root == null) {
            return 0;
        }
        if(root.left == null && root.right == null) {
            return 0;
        }
        return Math.max(getHeightOne(root.left), getHeightOne(root.right)) + 1;
    }

    /**
     * 获取二叉树的高度
     * 实现方式二：采用层次遍历的方式
     * @param root
     * @return
     */
    public static int getHeightTwo(Node root) {
        if(root == null) {
            return 0;
        }
        Queue<Node> queue = new LinkedList<>();
        queue.offer(root);
        int floor = 1;
        int front = 0; // 队头
        int rear = queue.size();// 队尾

        while (!queue.isEmpty()){
            Node poll = queue.poll();
            if(poll.right != null){
                queue.offer(poll.right);
            }
            if(poll.left != null) {
                queue.offer(poll.left);
            }
            front ++;
            if(front == rear && !queue.isEmpty()) {
                floor ++;
                rear = queue.size();
                front = 0;
            }
        }
        return floor - 1;
    }
  ```
